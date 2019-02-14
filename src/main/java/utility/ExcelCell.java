/*
Copyright 2018 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
*/

package utility;

import org.apache.poi.ss.usermodel.Color;

/**
 * Class to store attributes related to MS-excel cell for UV-Scan report
 * 
 * @author aaggarwa
 *
 */
public class ExcelCell {

	String cellValue;
	Color foregroundColorObject;

	public ExcelCell() {

	}

	public ExcelCell(String cellValue, Color foregroundColorObject) {
		this.cellValue = cellValue;
		this.foregroundColorObject = foregroundColorObject;
	}

	public String getCellValue() {
		return cellValue;
	}

	public void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}

	public Color getForegroundColorObject() {
		return foregroundColorObject;
	}

	public void setForegroundColorObject(Color foregroundColorObject) {
		this.foregroundColorObject = foregroundColorObject;
	}

}
