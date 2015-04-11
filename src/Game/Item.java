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

import Core.Utils;
import Data.DataManager;
import Data.Types.GivenNameGroupData;
import Job.*;
import Map.MapCoordinate;
import java.io.Serializable;

/**
 * The class representing the actually Dwarves and their livestock which are
 * responsive to the players Job commands.
 *
 * @author Impaler
 */
public class Item extends Actor implements Serializable {

	private static final long serialVersionUID = 1;

	// Family/clan relationships
	// Happyness/ moods/ hunger/ thirst/ wealth/ honor / other mutable status
	// personality/ tastes/ other fixed status
	// Proffessions / levels/ skills
	public Item(int id, MapCoordinate SpawnLocation, String modelID) {
		super(id, SpawnLocation, modelID);
	}

	@Override
	long wake(long CurrentTick) {
		return -1; // don't wake up
	}
}
