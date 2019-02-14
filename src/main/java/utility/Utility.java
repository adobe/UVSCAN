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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import java.util.zip.ZipFile;
import org.apache.poi.openxml4j.util.ZipSecureFile;

import uvscan.UvScanUrl;
import uvscan.UvScanUrl.UrlExceptionLevel;

/**
 * Utility class to containing functions to perform atomic tasks
 * 
 * @author aaggarwa
 *
 */
public class Utility {

	private static final Logger log = Logger.getLogger(Utility.class);

	/**
	 * Extracts the Url from a string using regex
	 * 
	 * @param UrlString
	 * @return list of urls found in string
	 */
	public static ArrayList<String> getUrlFromString(String urlString) {

		String UrlParserRegex = Configuration.Regex_For_Url_Parser;

		Pattern urlPattern = Pattern.compile(UrlParserRegex,
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

		Matcher matcher = urlPattern.matcher(urlString);
		String extractedUrl = "";
		ArrayList<String> UrlsFound = new ArrayList<String>();

		while (matcher.find()) {
			int matchStart = matcher.start(1);
			int matchEnd = matcher.end();

			while ((urlString.charAt(matchEnd - 1) == ')') || (urlString.charAt(matchEnd - 1) == '.'))
				matchEnd--;

			extractedUrl = urlString.substring(matchStart, matchEnd);

			extractedUrl = extractedUrl.trim();

			int lastIndex = extractedUrl.length();
			while ((extractedUrl.charAt(lastIndex - 1) == ')') || (extractedUrl.charAt(lastIndex - 1) == '.'))
				lastIndex--;

			if (extractedUrl.indexOf(',') >= 0)
				lastIndex = extractedUrl.indexOf(',');

			extractedUrl = extractedUrl.substring(0, lastIndex);

			UrlsFound.add(extractedUrl);
		}

		return UrlsFound;
	}

	/**
	 * Function to check if url string is proper or not and converts into a proper
	 * url. Right now just prepend the protocol ,if missing
	 * 
	 * @param url
	 * @return a properly formatted url string
	 */
	public static String getformattedUrl(String url) {
		String formattedUrl = url;

		if ((formattedUrl == null) || (formattedUrl.equals("")))
			return "";

		if (!(formattedUrl.startsWith("https://") || formattedUrl.startsWith("http://")))
			formattedUrl = Configuration.getConstant("HTTP_Protocol") + formattedUrl;

		return formattedUrl;

	}

	/**
	 * Utility function to remove a character from the end of a string
	 * 
	 * @param word
	 * @param trailingchar
	 * @return formatted String
	 */
	public static String removeStringFromEnd(String word, char trailingchar) {
		if ((word == null) || word.equals(""))
			return "";

		if (word.charAt(word.length() - 1) == trailingchar) {
			return word.substring(0, word.length() - 1);
		} else
			return word;

	}

	/**
	 * Utility function to recursively trim character from beginning and end
	 * 
	 * @param word
	 * @param trailingchar
	 * @return formatted String
	 */
	public static String trimLeadingAndTrailing(String word, char trailingchar) {
		if ((word == null) || word.equals("") || (word.length() < 1))
			return "";

		while (word.charAt(word.length() - 1) == trailingchar) {
			word = word.substring(0, word.length() - 1);
			if ((word == null) || word.equals("") || (word.length() < 1))
				return "";
		}
		while (word.charAt(0) == trailingchar) {
			word = word.substring(1, word.length());
			if ((word == null) || word.equals("") || (word.length() < 1))
				return "";
		}

		return word;

	}

	/**
	 * 
	 * Utility function to replace substring matching the regex with the value in
	 * replacement map
	 * 
	 * @param text
	 *            - String in which substitution needs to be done
	 * @param patternRegex
	 *            - regex to find pattern in the string
	 * @param replacements-
	 *            Replacement map use to replace matching substring with their
	 *            corresponding notations in the map
	 * @return
	 */
	public static String getSubsitutedString(String text, String patternRegex, Map<String, String> replacements) {
		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(patternRegex, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);
		// log.info(text);
		// log.info(patternRegex);
		while (m.find()) {
			// log.info("match");
			// log.info(replacements.get(m.group().toLowerCase())+"
			// "+m.group().toLowerCase());
			m.appendReplacement(sb, replacements.get(m.group().toLowerCase()));
		}
		m.appendTail(sb);

		// log.info(sb.toString());
		return sb.toString();
	}

	/**
	 * Function to return appropriate exception level based on color code
	 * 
	 * @param colorCode
	 *            - color code of the excel cell in report
	 * @return UrlExceptionLevel
	 */
	public static UrlExceptionLevel getExceptionTypeFromColorCode(Color colorCode) {
		UrlExceptionLevel exceptionType = UrlExceptionLevel.None;
		if (colorCode == null)
			return exceptionType;

		if (colorCode.equals(Configuration.colorCodeForExceptions.get("UnlocalizedBaseUrl")))
			return UrlExceptionLevel.UnlocalizedBaseUrl;

		else if (colorCode.equals(Configuration.colorCodeForExceptions.get("UnlocalizedRedirectedUrl")))
			return UrlExceptionLevel.UnlocalizedRedirectedUrl;

		else if (colorCode.equals(Configuration.colorCodeForExceptions.get("IgnorePatternMatching")))
			return UrlExceptionLevel.IgnorePatternMatching;

		else if (colorCode.equals(Configuration.colorCodeForExceptions.get("Do not test")))
			return UrlExceptionLevel.Delete;

		return exceptionType;
	}

	/**
	 * Function to return appropriate color code based on the exception level of Url
	 * 
	 * @param exceptionType
	 *            - exception level of Url
	 * @return Color
	 */
	public static Color getColorCodeFromExceptionType(UrlExceptionLevel exceptionType) {
		Color colorCode = null;

		if ((exceptionType == null) || (exceptionType.equals(UrlExceptionLevel.None)))
			return colorCode;

		else if (exceptionType.equals(UrlExceptionLevel.UnlocalizedBaseUrl))
			return Configuration.colorCodeForExceptions.get("UnlocalizedBaseUrl");

		else if (exceptionType.equals(UrlExceptionLevel.UnlocalizedRedirectedUrl))
			return Configuration.colorCodeForExceptions.get("UnlocalizedRedirectedUrl");

		else if (exceptionType.equals(UrlExceptionLevel.IgnorePatternMatching))
			return Configuration.colorCodeForExceptions.get("IgnorePatternMatching");

		else if (exceptionType.equals(UrlExceptionLevel.Delete))
			return Configuration.colorCodeForExceptions.get("Do not test");

		return colorCode;
	}

	//////////////////////////////////////////// Utility functions for Excel
	//////////////////////////////////////////// read-write///////////////////////////////////////////////////////////

	/**
	 * Function to read an excel sheet
	 * 
	 * @param excelFilePath
	 *            - full path of the excel file to be read
	 * @param rowNo
	 *            - No of rows of excel to be read , use -1 to read all rows
	 * @return - Arraylist of arraylist of ExcelCell object representing an excel
	 *         sheet
	 */
	public static ArrayList<ArrayList<ExcelCell>> ReadFromExcelFile(String excelFilePath, int rowNo) {

		FileInputStream inputStream = null;
		Workbook workbook = null;
		ArrayList<ArrayList<ExcelCell>> rowArraylist = new ArrayList<ArrayList<ExcelCell>>();

		try {

			inputStream = new FileInputStream(new File(excelFilePath));

			// ZipHelper.openZipStream(inputStream);
			ZipSecureFile.setMinInflateRatio(0);
			workbook = getWorkbookForReading(inputStream, excelFilePath);
			Sheet configurationSheet = workbook
					.getSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_ConfigSheetName"));
			Iterator<Row> iterator = configurationSheet.iterator();
			int rowsRead = 0;

			// Ignore header row
			iterator.next();

			for (int n = 0; n < 4; n++) {
				Row excelRow = iterator.next();
				Cell excelCell = excelRow.getCell(0);
				String exceptionName = (String) getCellValue(excelCell);

				excelCell = excelRow.getCell(1);
				CellStyle cellStyle = excelCell.getCellStyle();
				if (cellStyle != null)
					Configuration.colorCodeForExceptions.put(exceptionName, cellStyle.getFillForegroundColorColor());
			}

			while (iterator.hasNext()) {
				Row excelRow = iterator.next();
				Cell excelCell = excelRow.getCell(0);
				String validationName = (String) getCellValue(excelCell);
				validationName = (validationName == null ? "" : validationName.toLowerCase());

				validationName = validationName.trim();

				excelCell = excelRow.getCell(1);
				String validationValue = (String) getCellValue(excelCell);
				validationValue = (validationValue == null ? "" : validationValue.toLowerCase());

				if (validationName.contains("validate url pattern"))
					Configuration.CheckUrlPattern = validationValue.contains("yes") ? true : false;

				else if (validationName.contains("validate url redirection"))
					Configuration.CheckUrlRedirection = validationValue.contains("yes") ? true : false;

				else if (!(validationValue.equals(""))) {
					if (validationName.contains("loc modification start date")) {
						Configuration.locModificationStartDateValue = validationValue;

						validationValue = getDateInExpectedFormat(validationValue);
						if (!validationValue.equals(""))
							Configuration.locModificationStartDate = validationValue;

						if (!Configuration.locModificationStartDateValue.toLowerCase().contains("today"))
							Configuration.locModificationStartDateValue = Configuration.locModificationStartDate;
					} else if (validationName.contains("loc modification end  date")) {
						Configuration.locModificationEndDateValue = validationValue;

						validationValue = getDateInExpectedFormat(validationValue);
						if (!validationValue.equals(""))
							Configuration.locModificationEndDate = validationValue;

						if (!Configuration.locModificationEndDateValue.toLowerCase().contains("today"))
							Configuration.locModificationEndDateValue = Configuration.locModificationEndDate;
					}
				}
			}

			Sheet reportSheet = workbook
					.getSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_Report1SheetName"));
			iterator = reportSheet.iterator();

			while (iterator.hasNext()) {

				ArrayList<ExcelCell> cellArraylist = new ArrayList<ExcelCell>();

				Row excelRow = iterator.next();
				int noOfColumns = excelRow.getLastCellNum();

				int i = 0;
				while ((i < noOfColumns)) {

					Cell excelCell = excelRow.getCell(i);
					i++;

					String cellValue = "";
					Color color = null;
					if (excelCell != null) {
						cellValue = (String) getCellValue(excelCell);

						CellStyle cellStyle = excelCell.getCellStyle();
						color = (cellStyle == null ? null : cellStyle.getFillForegroundColorColor());
					}
					cellArraylist.add(new ExcelCell(cellValue, color));
				}

				rowsRead++;
				rowArraylist.add(cellArraylist);

				if ((rowNo != -1) && (rowNo == rowsRead))
					break;
			}

			workbook.close();
			inputStream.close();

		} catch (Exception e) {
			log.error("Exception in ReadFromExcel" + e.toString(), e);
		} finally {
			try {
				if (workbook != null)
					workbook.close();
				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/inputStream !!!", e);
			}
		}

		return rowArraylist;
	}

	/**
	 * Function to return Style Map based on exceptions
	 * 
	 * @param workbook
	 * @return Map<Color,XSSFCellstyle>
	 */
	private static Map<Color, XSSFCellStyle> getStyleMap(Workbook workbook) {
		Map<Color, XSSFCellStyle> styleMap = new HashMap<Color, XSSFCellStyle>();
		XSSFCellStyle style = null;
		for (String exceptionType : Configuration.colorCodeForExceptions.keySet()) {
			style = (XSSFCellStyle) workbook.createCellStyle();
			style.setWrapText(true);
			Color cellColor = Configuration.colorCodeForExceptions.get(exceptionType);
			styleMap.put(cellColor,
					applyAndgetCustomStyle(style, (XSSFColor) cellColor, BorderStyle.THIN, java.awt.Color.black));
		}
		style = (XSSFCellStyle) workbook.createCellStyle();
		style.setWrapText(true);
		Color cellColor = new XSSFColor(java.awt.Color.white);
		styleMap.put(cellColor,
				applyAndgetCustomStyle(style, (XSSFColor) cellColor, BorderStyle.THIN, java.awt.Color.black));
		return styleMap;
	}

	/**
	 * Function to read Master UrlvsLocale sheet
	 * 
	 * @param excelFilePath
	 *            - full path of the excel file to be read
	 * @param rowNo
	 *            - No of rows of excel to be read , use -1 to read all rows
	 * @return - Arraylist of arraylist of ExcelCell object representing an excel
	 *         sheet
	 */
	public static ArrayList<ArrayList<ExcelCell>> ReadFromConfigFile(String excelFilePath, int rowNo) {

		FileInputStream inputStream = null;
		Workbook workbook = null;
		ArrayList<ArrayList<ExcelCell>> rowArraylist = new ArrayList<ArrayList<ExcelCell>>();
		int rowsRead = 0;

		try {
			inputStream = new FileInputStream(new File(excelFilePath));

			ZipSecureFile.setMinInflateRatio(0);
			workbook = getWorkbookForReading(inputStream, excelFilePath);

			Sheet reportSheet = workbook
					.getSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Master_File_ReportSheetName"));
			Iterator<Row> iterator = reportSheet.iterator();

			while (iterator.hasNext()) {

				ArrayList<ExcelCell> cellArraylist = new ArrayList<ExcelCell>();

				Row excelRow = iterator.next();
				int noOfColumns = excelRow.getLastCellNum();

				int i = 0;
				while ((i < noOfColumns)) {

					Cell excelCell = excelRow.getCell(i);
					i++;

					String cellValue = "";
					Color color = null;
					if (excelCell != null) {
						cellValue = (String) getCellValue(excelCell);

						CellStyle cellStyle = excelCell.getCellStyle();
						color = (cellStyle == null ? null : cellStyle.getFillForegroundColorColor());
					}
					cellArraylist.add(new ExcelCell(cellValue, color));
				}

				rowsRead++;
				rowArraylist.add(cellArraylist);

				if ((rowNo != -1) && (rowNo == rowsRead))
					break;
			}

			workbook.close();
			inputStream.close();

		} catch (Exception e) {
			log.error("Exception in ReadFromExcel" + e.toString(), e);
		} finally {
			try {
				if (workbook != null)
					workbook.close();
				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/inputStream !!!", e);
			}
		}

		return rowArraylist;
	}

	public static ArrayList<ArrayList<ExcelCell>> ReadDateFromConfigFile(String excelFilePath, int rowNo) {

		FileInputStream inputStream = null;
		Workbook workbook = null;
		ArrayList<ArrayList<ExcelCell>> rowArraylist = new ArrayList<ArrayList<ExcelCell>>();
		int rowsRead = 0;

		try {
			inputStream = new FileInputStream(new File(excelFilePath));

			ZipSecureFile.setMinInflateRatio(0);
			workbook = getWorkbookForReading(inputStream, excelFilePath);

			Sheet reportSheet = workbook
					.getSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Date_File_ReportSheetName"));
			Iterator<Row> iterator = reportSheet.iterator();

			while (iterator.hasNext()) {

				ArrayList<ExcelCell> cellArraylist = new ArrayList<ExcelCell>();

				Row excelRow = iterator.next();
				int noOfColumns = excelRow.getLastCellNum();

				int i = 0;
				while ((i < noOfColumns)) {

					Cell excelCell = excelRow.getCell(i);
					i++;

					String cellValue = "";
					Color color = null;
					if (excelCell != null) {
						cellValue = (String) getCellValue(excelCell);

						CellStyle cellStyle = excelCell.getCellStyle();
						color = (cellStyle == null ? null : cellStyle.getFillForegroundColorColor());
					}
					cellArraylist.add(new ExcelCell(cellValue, color));
				}

				rowsRead++;
				rowArraylist.add(cellArraylist);

				if ((rowNo != -1) && (rowNo == rowsRead))
					break;
			}

			workbook.close();
			inputStream.close();

		} catch (Exception e) {
			log.error("Exception in ReadFromExcel" + e.toString(), e);
		} finally {
			try {
				if (workbook != null)
					workbook.close();
				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/inputStream !!!", e);
			}
		}

		return rowArraylist;
	}

	/**
	 * Function to read Master DetailedReport Sheet
	 * 
	 * @param excelFilePath
	 *            - full path of the excel file to be read
	 * @param rowNo
	 *            - No of rows of excel to be read , use -1 to read all rows
	 * @return - Arraylist of arraylist of ExcelCell object representing an excel
	 *         sheet
	 */
	public static ArrayList<ArrayList<ExcelCell>> ReadFromDetailConfigFile(String excelFilePath, int rowNo) {

		FileInputStream inputStream = null;
		Workbook workbook = null;
		ArrayList<ArrayList<ExcelCell>> rowArraylist = new ArrayList<ArrayList<ExcelCell>>();
		int rowsRead = 0;

		try {
			inputStream = new FileInputStream(new File(excelFilePath));

			ZipSecureFile.setMinInflateRatio(0);
			workbook = getWorkbookForReading(inputStream, excelFilePath);

			Sheet reportSheet = workbook
					.getSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Detail_File_ReportSheetName"));
			Iterator<Row> iterator = reportSheet.iterator();

			while (iterator.hasNext()) {

				ArrayList<ExcelCell> cellArraylist = new ArrayList<ExcelCell>();

				Row excelRow = iterator.next();
				int noOfColumns = excelRow.getLastCellNum();

				int i = 0;
				while ((i < noOfColumns)) {

					Cell excelCell = excelRow.getCell(i);
					i++;

					String cellValue = "";
					Color color = null;
					if (excelCell != null) {
						cellValue = (String) getCellValue(excelCell);

						CellStyle cellStyle = excelCell.getCellStyle();
						color = (cellStyle == null ? null : cellStyle.getFillForegroundColorColor());
					}
					cellArraylist.add(new ExcelCell(cellValue, color));
				}

				rowsRead++;
				rowArraylist.add(cellArraylist);

				if ((rowNo != -1) && (rowNo == rowsRead))
					break;
			}

			workbook.close();
			inputStream.close();

		} catch (Exception e) {
			log.error("Exception in ReadFromExcel" + e.toString(), e);
		} finally {
			try {
				if (workbook != null)
					workbook.close();
				if (inputStream != null)
					inputStream.close();

			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/inputStream !!!", e);
			}
		}

		return rowArraylist;
	}

	/**
	 * Function to Write UV-scan Pattern in excel format
	 * 
	 * @param dataForSheet1
	 *            Arraylist of arraylist of ExcelCell object representing an excel
	 *            sheet for sheet1
	 * @param detailedSheet
	 *            Arraylist of arraylist of ExcelCell object representing an excel
	 *            sheet for sheet2
	 * @param excelFilePath
	 *            - full path of the excel file to be written
	 */
	public static void WriteToPatternFile(ArrayList<ArrayList<ExcelCell>> dataForSheet1,
			ArrayList<ArrayList<ExcelCell>> detailedSheet, ArrayList<ArrayList<ExcelCell>> dateSheet,
			String excelFilePath) {
		Workbook workbook = null;
		try {
			short h = 276;
			workbook = getWorkbookForWriting(excelFilePath);

			Sheet reportSheet = workbook
					.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Master_File_ReportSheetName"));
			reportSheet.setDefaultRowHeight(h);
			int rowNo = 0;
			XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
			Map<Color, XSSFCellStyle> styleMap = null;
			styleMap = getStyleMap(workbook);
			Color cellColor = null;
			for (ArrayList<ExcelCell> row : dataForSheet1) {
				Row excelRow = reportSheet.createRow(rowNo++);

				int columnNo = 0;
				for (ExcelCell cell : row) {
					Cell excelCell = excelRow.createCell(columnNo);
					columnNo++;

					cellColor = cell.getForegroundColorObject();

					if (cellColor == null)
						cellColor = new XSSFColor(java.awt.Color.white);

					try {
						excelCell.setCellValue(cell.getCellValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					excelCell.setCellStyle(styleMap.get(cellColor));
				}
			}

			Sheet reportdetailedSheet = workbook
					.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Detail_File_ReportSheetName"));
			reportdetailedSheet.setDefaultRowHeight(h);
			rowNo = 0;
			style = (XSSFCellStyle) workbook.createCellStyle();
			for (ArrayList<ExcelCell> row : detailedSheet) {
				Row excelRow = reportdetailedSheet.createRow(rowNo++);

				int columnNo = 0;
				for (ExcelCell cell : row) {
					Cell excelCell = excelRow.createCell(columnNo);
					columnNo++;

					cellColor = cell.getForegroundColorObject();

					if (cellColor == null)
						cellColor = new XSSFColor(java.awt.Color.white);

					try {
						excelCell.setCellValue(cell.getCellValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					excelCell.setCellStyle(styleMap.get(cellColor));
				}
			}

			Sheet reportDateSheet = workbook
					.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Date_File_ReportSheetName"));
			reportDateSheet.setDefaultRowHeight(h);
			rowNo = 0;
			style = (XSSFCellStyle) workbook.createCellStyle();
			for (ArrayList<ExcelCell> row : dateSheet) {
				Row excelRow = reportDateSheet.createRow(rowNo++);

				int columnNo = 0;
				for (ExcelCell cell : row) {
					Cell excelCell = excelRow.createCell(columnNo);
					columnNo++;

					cellColor = cell.getForegroundColorObject();

					if (cellColor == null)
						cellColor = new XSSFColor(java.awt.Color.white);

					try {
						excelCell.setCellValue(cell.getCellValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					excelCell.setCellStyle(styleMap.get(cellColor));
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);

			long startTime = System.currentTimeMillis();
			log.info("Calling a method to upload a file to a remote server at " + startTime);
			try {
				byte[] bytes = baos.toByteArray();
				FileOutputStream fos = new FileOutputStream(excelFilePath);
				fos.write(bytes);
				fos.close();

			} catch (IOException ioe) {
				log.error("\nIOException occured while uploading file to server " + excelFilePath, ioe);
			}
			log.info("File has been uploaded in " + (System.currentTimeMillis() - startTime) + " miliseconds");

			workbook.close();

		} catch (Exception e) {
			log.error("Exception in WriteToExcel " + e.toString(), e);
			e.printStackTrace();
		} finally {
			try {
				if (workbook != null)
					workbook.close();
			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/outputStream !!!", e);
			}
		}
	}

	private static Workbook addReadme(Workbook workbook) throws ParseException {
		Sheet configurationSheet = workbook
				.createSheet(Configuration.getConstant("Display_Message_For_Readme_Heading"));
		short h = 276;
		configurationSheet.setDefaultRowHeight(h);
		int rowNo = 0;

		Row excelRow = configurationSheet.createRow(rowNo++);
		Cell excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Configurations");
		XSSFFont font = (XSSFFont) workbook.createFont();
		font.setBold(true);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		style.setFont(font);
		excelCell.setCellStyle(style);
		String display_msg = "";
		for (int i = 1; i <= 4; i++) {
			display_msg = "Display_Message_For_Readme_Body" + i;
			excelRow = configurationSheet.createRow(rowNo++);
			excelCell = excelRow.createCell(0);
			excelCell = excelRow.createCell(1);
			excelCell.setCellValue(Configuration.getConstant(display_msg));
		}

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Report Analysis");
		font = (XSSFFont) workbook.createFont();
		font.setBold(true);
		style = (XSSFCellStyle) workbook.createCellStyle();
		style.setFont(font);
		excelCell.setCellStyle(style);

		for (int i = 5; i <= 8; i++) {
			display_msg = "Display_Message_For_Readme_Body" + i;
			excelRow = configurationSheet.createRow(rowNo++);
			excelCell = excelRow.createCell(0);
			excelCell = excelRow.createCell(1);
			excelCell.setCellValue(Configuration.getConstant(display_msg));
		}

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Testing Help URLS with UV Scan");
		font = (XSSFFont) workbook.createFont();
		font.setBold(true);
		style = (XSSFCellStyle) workbook.createCellStyle();
		style.setFont(font);
		excelCell.setCellStyle(style);

		for (int i = 9; i <= 10; i++) {
			display_msg = "Display_Message_For_Readme_Body" + i;
			excelRow = configurationSheet.createRow(rowNo++);
			excelCell = excelRow.createCell(0);
			excelCell = excelRow.createCell(1);
			excelCell.setCellValue(Configuration.getConstant(display_msg));
		}
		return workbook;
	}

	/**
	 * Function to Write UV-scan Master File in excel format
	 * 
	 * @param workbook
	 * @return workbook - Workbook on which Configurations (1st Sheet) is written
	 */
	private static Workbook addFirstSheetFinal(Workbook workbook) throws ParseException {

		workbook = addReadme(workbook);
		Sheet configurationSheet = workbook
				.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_ConfigSheetName"));
		short h = 276;
		configurationSheet.setDefaultRowHeight(h);

		int rowNo = 0;
		Row excelRow = configurationSheet.createRow(rowNo++);
		Cell excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Exception Type ");
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));
		excelCell.setCellStyle(style);

		excelCell = excelRow.createCell(1);
		excelCell.setCellValue("Fill Color");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));
		excelCell.setCellStyle(style);

		Map<String, Color> sortedMap = new TreeMap<String, Color>(Configuration.colorCodeForExceptions);
		for (String exceptionType : sortedMap.keySet()) {
			excelRow = configurationSheet.createRow(rowNo++);
			excelCell = excelRow.createCell(0);
			excelCell.setCellValue(exceptionType);
			style = (XSSFCellStyle) workbook.createCellStyle();
			excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

			excelCell = excelRow.createCell(1);
			style = (XSSFCellStyle) workbook.createCellStyle();
			excelCell.setCellStyle(
					applyAndgetCustomStyle(style, (XSSFColor) Configuration.colorCodeForExceptions.get(exceptionType),
							BorderStyle.THIN, java.awt.Color.black));
		}

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("");
		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("");

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Validations");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelCell = excelRow.createCell(1);
		excelCell.setCellValue("To be Performed");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Validate Url Pattern");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelCell = excelRow.createCell(1);
		excelCell.setCellValue((Configuration.CheckUrlPattern ? "Yes" : "No"));
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Validate Url Redirection");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelCell = excelRow.createCell(1);
		excelCell.setCellValue((Configuration.CheckUrlRedirection ? "Yes" : "No"));
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("");
		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("");

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Loc Modification Start Date (mm/dd/yyyy)");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelCell = excelRow.createCell(1);
		if (Configuration.locModificationStartDateValue.toLowerCase().contains("today"))
			excelCell.setCellValue(Configuration.locModificationStartDateValue);
		else
			excelCell.setCellValue(
					new SimpleDateFormat("dd-MM-yyyy").parse(Configuration.locModificationStartDateValue));
		style = (XSSFCellStyle) workbook.createCellStyle();
		CreationHelper createHelper = workbook.getCreationHelper();
		style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));

		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelRow = configurationSheet.createRow(rowNo++);
		excelCell = excelRow.createCell(0);
		excelCell.setCellValue("Loc Modification End  Date (mm/dd/yyyy)");
		style = (XSSFCellStyle) workbook.createCellStyle();
		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));

		excelCell = excelRow.createCell(1);
		if (Configuration.locModificationEndDateValue.toLowerCase().contains("today"))
			excelCell.setCellValue(Configuration.locModificationEndDateValue);
		else
			excelCell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").parse(Configuration.locModificationEndDateValue));

		style = (XSSFCellStyle) workbook.createCellStyle();
		createHelper = workbook.getCreationHelper();
		style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));

		excelCell.setCellStyle(applyAndgetCustomStyle(style, null, BorderStyle.THIN, java.awt.Color.black));
		return workbook;
	}

	/**
	 * Function to Write UV-scan Master File in excel format
	 * 
	 * @param workbook
	 * @param dataforSheet1-
	 *            UrlvsLocale status
	 * @return workbook - Workbook on which 2nd Sheet is written
	 */
	private static Workbook addSecondSheetFinal(Workbook workbook, ArrayList<ArrayList<ExcelCell>> dataForSheet1) {
		short h = 276;
		Row excelRow = null;
		Cell excelCell = null;
		Sheet reportSheet = workbook
				.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_Report1SheetName"));
		reportSheet.setDefaultRowHeight(h);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		int rowNo = 0;
		Color cellColor = null;
		Map<Color, XSSFCellStyle> styleMap = null;
		styleMap = getStyleMap(workbook);
		for (ArrayList<ExcelCell> row : dataForSheet1) {
			excelRow = reportSheet.createRow(rowNo++);

			int columnNo = 0;
			for (ExcelCell cell : row) {
				excelCell = excelRow.createCell(columnNo);
				columnNo++;

				cellColor = cell.getForegroundColorObject();
				boolean hasException = false;
				if (cellColor != null) {
					for (String exceptionType : Configuration.colorCodeForExceptions.keySet()) {
						if (cellColor.equals(Configuration.colorCodeForExceptions.get(exceptionType)))
							hasException = true;
					}
				}
				if (cellColor == null || !hasException)
					cellColor = new XSSFColor(java.awt.Color.white);
				try {
					excelCell.setCellValue(cell.getCellValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
				excelCell.setCellStyle(styleMap.get(cellColor));
			}
		}
		return workbook;
	}

	/**
	 * Function to Write UV-scan Master File in excel format
	 * 
	 * @param workbook
	 * @param detailedSheet-
	 *            Detailed Report of UrlvsStatus
	 * @return workbook - Workbook on which 3rd Sheet is written
	 */
	private static Workbook addThirdSheet(Workbook workbook, ArrayList<ArrayList<ExcelCell>> detailedSheet) {
		short h = 276;
		Row excelRow = null;
		Cell excelCell = null;
		Color cellColor = null;
		Sheet reportdetailedSheet = workbook
				.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Detail_File_ReportSheetName"));
		reportdetailedSheet.setDefaultRowHeight(h);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		int rowNo = 0;
		Map<Color, XSSFCellStyle> styleMap = null;
		styleMap = getStyleMap(workbook);
		for (ArrayList<ExcelCell> row : detailedSheet) {
			excelRow = reportdetailedSheet.createRow(rowNo++);
			int columnNo = 0;
			for (ExcelCell cell : row) {
				excelCell = excelRow.createCell(columnNo);
				columnNo++;

				cellColor = cell.getForegroundColorObject();

				boolean hasException = false;
				if (cellColor != null) {
					for (String exceptionType : Configuration.colorCodeForExceptions.keySet()) {
						if (cellColor.equals(Configuration.colorCodeForExceptions.get(exceptionType)))
							hasException = true;
					}
				}
				if (cellColor == null || !hasException)
					cellColor = new XSSFColor(java.awt.Color.white);

				try {
					excelCell.setCellValue(cell.getCellValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
				excelCell.setCellStyle(styleMap.get(cellColor));
			}
		}
		return workbook;
	}

	/**
	 * Function to Write UV-scan Master File in excel format
	 * 
	 * @param workbook
	 * @param dataForSheet2-
	 *            LocaleWiseStatus
	 * @return workbook - Workbook on which 4th Sheet is written
	 */
	private static Workbook addFourthSheet(Workbook workbook, ArrayList<ArrayList<ExcelCell>> dataForSheet2) {
		short h = 276;
		Row excelRow = null;
		Cell excelCell = null;
		Color cellColor = null;
		Sheet reportSheet = workbook
				.createSheet(Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_Report2SheetName"));
		reportSheet.setDefaultRowHeight(h);
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		int rowNo = 0;
		for (ArrayList<ExcelCell> row : dataForSheet2) {
			excelRow = reportSheet.createRow(rowNo++);

			int columnNo = 0;
			for (ExcelCell cell : row) {
				excelCell = excelRow.createCell(columnNo);
				columnNo++;

				style.setWrapText(true);

				cellColor = cell.getForegroundColorObject();

				boolean hasException = false;
				if (cellColor != null) {
					for (String exceptionType : Configuration.colorCodeForExceptions.keySet()) {
						if (cellColor.equals(Configuration.colorCodeForExceptions.get(exceptionType)))
							hasException = true;
					}
				}
				if (cellColor == null || !hasException)
					cellColor = new XSSFColor(java.awt.Color.white);
				try {
					excelCell.setCellValue(cell.getCellValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
				excelCell.setCellStyle(
						applyAndgetCustomStyle(style, (XSSFColor) cellColor, BorderStyle.THIN, java.awt.Color.black));
			}
		}
		return workbook;
	}

	/**
	 * Function to Write UV-scan report in excel format
	 * 
	 * @param dataForSheet1
	 *            Arraylist of arraylist of ExcelCell object representing an excel
	 *            sheet for sheet1
	 * @param dataForSheet2
	 *            Arraylist of arraylist of ExcelCell object representing an excel
	 *            sheet for sheet2
	 * @param excelFilePath
	 *            - full path of the excel file to be written
	 */

	public static void WriteToExcelFile(ArrayList<ArrayList<ExcelCell>> dataForSheet1,
			ArrayList<ArrayList<ExcelCell>> detailedSheet, ArrayList<ArrayList<ExcelCell>> dataForSheet2,
			String excelFilePath) {
		Workbook workbook = null;
		try {

			workbook = getWorkbookForWriting(excelFilePath);
			workbook = addFirstSheetFinal(workbook);
			workbook = addSecondSheetFinal(workbook, dataForSheet1);
			workbook = addThirdSheet(workbook, detailedSheet);
			workbook = addFourthSheet(workbook, dataForSheet2);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);

			long startTime = System.currentTimeMillis();
			log.info("Calling a method to upload a file to a remote server at " + startTime);
			try {
				byte[] bytes = baos.toByteArray();
				FileOutputStream fos = new FileOutputStream(excelFilePath);
				fos.write(bytes);
				fos.close();
			} catch (IOException ioe) {
				log.error("\nIOException occured while uploading file to server " + excelFilePath, ioe);
			}
			log.info("File has been uploaded in " + (System.currentTimeMillis() - startTime) + " miliseconds");

			workbook.close();

		} catch (Exception e) {
			log.error("Exception in WriteToExcelFile " + e.toString(), e);
			e.printStackTrace();
		} finally {
			try {
				if (workbook != null)
					workbook.close();
			} catch (IOException e) {
				log.error("Error while flushing/closing workbook/outputStream !!!", e);
			}
		}
	}

	/**
	 * Function to add a custom style to an excell cell
	 * 
	 * @param style
	 *            - XSSFCellStyle object on which more style attributes are applied
	 * @param cellColor
	 *            - ForegroundColor for the excel cell
	 * @param bdStyle
	 *            - Border style for the excel cell
	 * @param bordercolor
	 *            - Border color for the excel cell
	 * @return - Updated XSSFCellStyle object
	 */
	public static XSSFCellStyle applyAndgetCustomStyle(XSSFCellStyle style, XSSFColor cellColor, BorderStyle bdStyle,
			java.awt.Color bordercolor) {
		if (cellColor != null) {
			style.setFillForegroundColor((XSSFColor) cellColor);
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);

		style.setBorderColor(BorderSide.LEFT, new XSSFColor(bordercolor));
		style.setBorderColor(BorderSide.RIGHT, new XSSFColor(bordercolor));
		style.setBorderColor(BorderSide.TOP, new XSSFColor(bordercolor));
		style.setBorderColor(BorderSide.BOTTOM, new XSSFColor(bordercolor));

		style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
		return style;
	}

	public static Workbook getWorkbookForReading(FileInputStream inputStream, String excelFilePath) throws IOException {
		Workbook workbook = null;

		if (excelFilePath.endsWith("xlsx")) {
			workbook = new XSSFWorkbook(inputStream);
		} else if (excelFilePath.endsWith("xls")) {
			workbook = new HSSFWorkbook(inputStream);
		} else {
			throw new IllegalArgumentException("The specified file is not Excel file");
		}
		return workbook;
	}

	public static Workbook getWorkbookForWriting(String excelFilePath) throws IOException {
		Workbook workbook = null;

		if (excelFilePath.endsWith("xlsx")) {
			workbook = new SXSSFWorkbook();
		} else if (excelFilePath.endsWith("xls")) {
			workbook = new HSSFWorkbook();
		} else {
			throw new IllegalArgumentException("The specified file is not Excel file");
		}
		return workbook;
	}

	public static String getCellValue(Cell cell) {

		if (cell == null)
			return "";

		switch (cell.getCellTypeEnum()) {
		case STRING:
			return cell.getStringCellValue();

		case BOOLEAN:
			return cell.getBooleanCellValue() + "";

		case NUMERIC: {
			if (HSSFDateUtil.isCellDateFormatted(cell))
				return cell.getDateCellValue() + "";

			return cell.getNumericCellValue() + "";
		}
		case _NONE:
			return "";
		default:
			return "";

		}
	}

	public static String getDateInExpectedFormat(String originalDate) {

		String formattedDate = "";
		try {
			DateFormat originalFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");
			DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date date = null;
			if (originalDate.contains("today")) {
				date = new Date();
			} else {
				date = originalFormat.parse(originalDate);
			}
			formattedDate = targetFormat.format(date);

		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}

		return formattedDate;
	}
}
