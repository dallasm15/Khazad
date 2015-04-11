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

package Game;

import Map.MapCoordinate;
import com.jme3.math.Vector3f;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * Base class for any object that has a location on the map and is potentially
 * visible to the player.
 *
 * Implementations: Pawn
 *
 * @author Impaler
 */
public abstract class Actor extends Temporal implements Serializable {

	private static final long serialVersionUID = 1;
	// The location for gameplay logic purposes
	MapCoordinate LocationCoordinates;
	boolean Visible;
	boolean Hidden;
	// Used by Rendering
	private transient boolean Dirty;
	private String ModelID;

	public Actor(int id, MapCoordinate SpawnLocation, String modelID) {
		this.ID = id;

		LocationCoordinates = SpawnLocation;
		this.ModelID = modelID;
		Dirty = true;
	}

	public void setLocation(MapCoordinate NewPosition) {
		LocationCoordinates = NewPosition;
		setDirty(true);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		// default deserialization
		ois.defaultReadObject();
		// fix transients
		setDirty(true);
	}

	public boolean isDirty() {
		return Dirty;
	}

	public MapCoordinate getLocation() {
		return LocationCoordinates;
	}

	void setVisible(boolean NewValue) {
		if (Visible != NewValue) {
			Visible = NewValue;
			//ActorNode. (NewValue);
			setDirty(true);
		}
	}

	public String getModelID() {
		return ModelID;
	}

	public void setDirty(boolean Dirty) {
		this.Dirty = Dirty;
	}
}
