/* Copyright 2010 Kenneth 'Impaler' Ferland

 This file is part of Khazad.

 Khazad is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Khazad is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Khazad.  If not, see <http://www.gnu.org/licenses/> */

package pathFinding;

import java.util.ArrayList;
import java.util.BitSet;

import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

import Core.Clock;
import Core.Main;

import Map.GameMap;
import Map.MapCoordinate;
import Map.Direction;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import com.jme3.app.state.AbstractAppState;
import java.io.Serializable;

/**
 * Master Controller for all pathfinding operations. All pathfinding is done on
 * abstractions of the Map structure called Grids rather then the real map, these
 * abstractions are specialized for a specific Movement Modality. Map changes
 * have to be pushed here to keep the Grids accurate.
 *
 * Navagator objects which are embeded in moving entites which then
 * request paths from PathFinder. Each incoming Path request is matched to an
 * appropriate Grid instance and then an appropriate PathAlgorim instance is
 * created, given the Grid refrence and submitted to the threadPool.
 *
 * @author Impaler
 */
public class PathFinding extends AbstractAppState {

	private static final long serialVersionUID = 1;
	static PathFinding Singleton = null;
	Clock PathingTimer;
	//AStar AstarImplementation;
	PathAlgorithm HeriarchialAstarImplementation;
	//KhazadGrid MapGrid;
	ConcurrentHashMap<MovementModality, GridInterface> Grids;
	Heuristic ManhattenHeuristic;
	Heuristic EuclideanHeuristic;
	Heuristic MaxDimensionHeuristic;
	Heuristic DijkstraHeuristic;
	Heuristic DiagonalHeuristic;
	ArrayList<Pool> PoolList;
	ExecutorService Executor;
	public PathTester Tester;
	// pathfinding stats
	private int GraphReads;
	private int ExpandedNodes;

	private PathFinding() {
		ManhattenHeuristic = new Heuristic.Manhatten();
		EuclideanHeuristic = new Heuristic.Euclidean();
		MaxDimensionHeuristic = new Heuristic.MaxDimension();
		DijkstraHeuristic = new Heuristic.Dijkstra();
		DiagonalHeuristic = new Heuristic.Diagonal();

		PoolList = new ArrayList<Pool>();
		Grids = new ConcurrentHashMap<MovementModality, GridInterface>();
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		super.initialize(stateManager, app);
		Main core = (Main) app;
		Executor = core.getThreadPool();
	}

	public static PathFinding getSingleton() {
		if (Singleton == null)
			Singleton = new PathFinding();

		return Singleton;
	}

	Pool ProvidePool() {  // Simple but effective
		for (Pool TargetPool : PoolList) {
			if (!TargetPool.isInUse()) {
				TargetPool.setInUse(true);
				return TargetPool;
			}
		}

		// No Pools available so Create a new one
		Pool NewPool = new Pool();
		NewPool.setInUse(true);
		PoolList.add(NewPool);
		return NewPool;
	}

	public void createMapAbstraction(GameMap TargetMap) {
		MovementModality BasicPawn = new MovementModality(MovementModality.MovementType.MOVEMENT_TYPE_WALK, 1, 1);
		KhazadGrid MainGrid = new KhazadGrid(TargetMap, BasicPawn);
		Grids.put(BasicPawn, MainGrid);

		Tester = new PathTester();
		Tester.Initialize(this);
	}

	public void editMapAbstractions(MapCoordinate[] Coordinates) {
		for (GridInterface Grid : Grids.values()) {
			Grid.dirtyMapCoordinate(Coordinates.clone());
		}
	}

	void deleteMapAbstractions() {
		Grids = null;
	}

	public Future findFuturePath(MovementModality MovementType, MapCoordinate StartCoords, MapCoordinate GoalCoords) {
		GridInterface TargetGrid = getModalityGrid(MovementType);
		if (TargetGrid != null) {
			if (TargetGrid.contains(StartCoords) && TargetGrid.contains(GoalCoords)) {
				if (isPathPossible(MovementType, StartCoords, GoalCoords)) {
					AStar PathTask = new AStar(TargetGrid);
					PathTask.assignNodePool(ProvidePool());
					PathTask.setModality(MovementType);
					PathTask.setHeuristics(ManhattenHeuristic, EuclideanHeuristic);
					PathTask.setEndPoints(StartCoords, GoalCoords);

					return Executor.submit(PathTask);
				}
			}
		}

		return null;
	}

	public MapPath profilePath(MovementModality MovementType, MapCoordinate StartCoords, MapCoordinate GoalCoords, PathTester.Profile TargetProfile) {
		GridInterface TargetGrid = getModalityGrid(MovementType);
		if (TargetGrid != null) {
			if (TargetGrid.contains(StartCoords) && TargetGrid.contains(GoalCoords)) {
				if (isPathPossible(MovementType, StartCoords, GoalCoords)) {
					PathingTimer.start();
					AStar PathTask = new AStar(TargetGrid);
					PathTask.assignNodePool(ProvidePool());
					PathTask.setModality(MovementType);
					PathTask.setHeuristics(ManhattenHeuristic, EuclideanHeuristic);
					PathTask.setEndPoints(StartCoords, GoalCoords);
					MapPath FoundPath = PathTask.call();

					TargetProfile.PathTimeCost = PathingTimer.stop();

					TargetProfile.ProfiledPath = FoundPath;

					if (FoundPath == null) {
						TargetProfile.ResultCode = ProfileResultCode.PATH_CODE_FAILURE_UNKNOWN;
					} else {
						TargetProfile.ResultCode = ProfileResultCode.PATH_CODE_SUCCESS;
					}

					TargetProfile.PathGraphReads = PathTask.getGraphReads();
					TargetProfile.PathExpandedNodes = PathTask.getExpandedNodes();

					return FoundPath;
				} else {
					TargetProfile.ResultCode = ProfileResultCode.PATH_CODE_FAILURE_NO_CONNECTION;
					return null;
				}
			} else {
				TargetProfile.ResultCode = ProfileResultCode.PATH_CODE_FAILURE_INVALID_LOCATION;
				return null;
			}
		} else {
			TargetProfile.ResultCode = ProfileResultCode.PATH_CODE_FAILUTE_UNITIALIZED;
			return null;
		}
	}

	public boolean isPathPossible(MovementModality MovementType, MapCoordinate StartCoords, MapCoordinate GoalCoords) {
		GridInterface TargetGrid = getModalityGrid(MovementType);
		if (TargetGrid != null) {
			return TargetGrid.isPathPossible(MovementType, StartCoords, GoalCoords);
		}
		return false;
	}

	GridInterface getModalityGrid(MovementModality MovementType) {
		return Grids.get(MovementType);
	}

	public BitSet getDirectionFlags(MapCoordinate Coordinates, MovementModality Modality) {
		GridInterface TargetGrid = getModalityGrid(Modality);
		if (TargetGrid != null) {
			return TargetGrid.getDirectionEdgeSet(Coordinates);
		}
		return null;
	}

	boolean contains(MapCoordinate Coordinates, MovementModality Modality) {
		GridInterface TargetGrid = getModalityGrid(Modality);
		if (TargetGrid != null) {
			return TargetGrid.contains(Coordinates);
		}
		return false;
	}

	public float getEdgeCost(MapCoordinate TestCoords, Direction DirectionType, MovementModality Modality) {
		GridInterface TargetGrid = getModalityGrid(Modality);
		if (TargetGrid != null) {
			return TargetGrid.getEdgeCost(TestCoords, DirectionType);
		}
		return -1;
	}

	public int getConnectivityZone(MapCoordinate TestCoords, MovementModality Modality) {
		GridInterface TargetGrid = getModalityGrid(Modality);
		if (TargetGrid != null) {
			return TargetGrid.getConnectivityZone(TestCoords);
		}
		return 0;
	}

	public int getGraphReads() {
		return GraphReads;
	}

	public void setGraphReads(int GraphReads) {
		this.GraphReads = GraphReads;
	}

	public void incrementGraphReads(int GraphReads) {
		this.GraphReads += GraphReads;
	}

	public int getExpandedNodes() {
		return ExpandedNodes;
	}

	public void setExpandedNodes(int ExpandedNodes) {
		this.ExpandedNodes = ExpandedNodes;
	}

	public void incrementExpandedNodes(int ExpandedNodes) {
		this.ExpandedNodes += ExpandedNodes;
	}
}
