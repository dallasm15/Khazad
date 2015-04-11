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

package Data.Types;

import Data.DataBase;
import Data.DataLibrary;
import com.jme3.math.Vector3f;
import java.io.Serializable;
import nu.xom.Element;

/**
 *
 * @author Impaler
 */
public class ModelData extends DataBase implements Serializable {

	private static final long serialVersionUID = 1;
	private String FilePath;
	private Vector3f scale;
	private Vector3f rotate;

	public ModelData() {
	}

	@Override
	public boolean loadData(Element ModelEntry, DataLibrary Library) {
		Element Name = ModelEntry.getFirstChildElement("Name", ModelEntry.getNamespaceURI());
		String label = Name.getAttributeValue("Label");

		Element fileElement = ModelEntry.getFirstChildElement("File", ModelEntry.getNamespaceURI());
		if (fileElement != null)
			FilePath = fileElement.getAttributeValue("Path");

		Element scaleElement = ModelEntry.getFirstChildElement("Scale", ModelEntry.getNamespaceURI());
		if (scaleElement != null) {
			this.scale = new Vector3f(
					Float.parseFloat(scaleElement.getAttributeValue("X")),
					Float.parseFloat(scaleElement.getAttributeValue("Y")),
					Float.parseFloat(scaleElement.getAttributeValue("Z")));
		} else {
			this.scale = new Vector3f(1, 1, 1);
		}

		Element rotateElement = ModelEntry.getFirstChildElement("Rotate", ModelEntry.getNamespaceURI());
		if (rotateElement != null) {
			this.rotate = new Vector3f(
					Float.parseFloat(rotateElement.getAttributeValue("X")),
					Float.parseFloat(rotateElement.getAttributeValue("Y")),
					Float.parseFloat(rotateElement.getAttributeValue("Z")));
		} else {
			this.rotate = new Vector3f(0, 0, 0);
		}
		Library.indexEntry(label, this);
		return true;
	}

	public boolean postProcessing() {
		return true;
	}

	public String getFilePath() {
		return FilePath;
	}

	public Vector3f getScale() {
		return scale;
	}

	public Vector3f getRotate() {
		return rotate;
	}
}
