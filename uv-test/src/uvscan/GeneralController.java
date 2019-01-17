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


package uvscan;


import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.ZipOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.collections.list.TreeList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Color;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import utility.Configuration;
import utility.ExcelCell;
import utility.Utility;
import uvscan.UvScanUrl.UrlExceptionLevel;

/**
 * Class to handle UV-SCAN functionalities common for all locales
 * @author aaggarwa
 *
 */
public class GeneralController {

	private static final Logger log =  Logger.getLogger(GeneralController.class);

	private String AlfProductName ;
	private String  AlfCodeName ;
	private HashMap<String, JSONObject> Locales;
	HashMap <String, UvScanUrl> UrlMapFromPatternsFile ;
	HashMap <String, UvScanUrl> ConfigUrlMapFromPatternsFile ;
	HashMap <String, UvScanUrl> EngUrlMapFromProductResources;
	private static String UvScanServerPath ;

	private	HashMap<String , Map<String,UrlExceptionLevel>> UrlVsLocaleExceptionMap;
	HashMap<String , UvScanResultBean> LocaleMapBean;
	HashMap<String , UrlMap> RecordsForLocaleVsUrlReportMap;
	HashMap<String , UrlMap> ConfigRecordsForLocaleVsUrlReportMap;
	HashMap<String , UrlMap> DetailedBaseForLocaleVsUrlReportMap;
	HashMap<String , UrlMap> DetailedRedirectionForLocaleVsUrlReportMap;
	HashMap<String , UrlMap> ConfigDetailedBaseForLocaleVsUrlReportMap;
	HashMap<String , UrlMap> ConfigDetailedRedirectionForLocaleVsUrlReportMap;
	HashMap<String , String> DateMap;
	HashMap<String , LastRunMap> LastRunDateMap;
	private String results_file;
	List<UvScanResultBean> UvScanResultBeanMap;
	
	private Map<String, String> replacementMapForPatternMatching;
	private	String combinedRegexForPatternMatching;
	int helpResources;
	
/**
 * Default Controller
 */
	public GeneralController()
	{
		UvScanServerPath = Configuration.getConstant("UvScan_FolderName_OnServer");
	}

	
/**
 * Constructor to create controller specific for an ALF project.
 * @param AlfProductName
 * @param AlfCodeName
 * @param locales
 */
	public GeneralController(String alfProductName , String  alfCodeName)
	{
		this.AlfProductName = alfProductName;
		this.AlfCodeName = alfCodeName;
		this.results_file= UvScanInit.Results_file;
		this.Locales = new HashMap<String, JSONObject>();
		this.UrlMapFromPatternsFile = new HashMap<String, UvScanUrl>();
		this.ConfigUrlMapFromPatternsFile = new HashMap<String, UvScanUrl>();
		this.EngUrlMapFromProductResources = new HashMap <String, UvScanUrl>();

		UvScanServerPath = Configuration.getConstant("UvScan_FolderName_OnServer");
		
		this.LocaleMapBean = new HashMap<String , UvScanResultBean>();
		this.UrlVsLocaleExceptionMap = new HashMap<String , Map<String,UrlExceptionLevel>>();
		this.RecordsForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.ConfigRecordsForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.DetailedBaseForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.DetailedRedirectionForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.ConfigDetailedBaseForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.ConfigDetailedRedirectionForLocaleVsUrlReportMap = new HashMap<String , UrlMap>();
		this.DateMap = new HashMap<String ,String>();
		this.LastRunDateMap = new HashMap<String , LastRunMap>();
		this.UvScanResultBeanMap = new ArrayList<UvScanResultBean>();
		this.replacementMapForPatternMatching =  new HashMap <String, String>();
		this.combinedRegexForPatternMatching = "";
		
	}


	public HashMap<String, JSONObject> getLocales() {
		return Locales;
	}
	
	
	public void setLocales(HashMap<String, JSONObject> locales) {
		Locales = locales;
	}


	public Map<String, String> getReplacementMapForPatternMatching() {
		return replacementMapForPatternMatching;
	}


	public String getCombinedRegexForPatternMatching() {
		return combinedRegexForPatternMatching;
	}


/**
 * Function to get path of UV-SCAN directory on server
 * @return path of directory
 */
	public String getUvScanDir()
	{		
		String serverRootPath =results_file;
		String UvScanDir = serverRootPath + "\\" + UvScanServerPath + Configuration.FPS;

		return UvScanDir;
	}

	
/**
 * Function to get path of directory specific for a project containing result and configuration files 
 * @return path of directory
 */
	public String getUvScanDirPath()
	{
		String UvScanRelativeDir = getRelativeUvScanDirPath();

		String UvScanDir = getUvScanDir() + UvScanRelativeDir;

		return UvScanDir;
	}

/**
 * Function to get relative path of directory specific for a project containing result and configuration files 
 * @return relative path of directory
 */
	public String getRelativeUvScanDirPath()
	{
		String UvScanRelativeDir = this.AlfProductName+Configuration.FPS+this.AlfCodeName+Configuration.FPS;
		
		UvScanRelativeDir = UvScanRelativeDir.replaceAll(" ", "_");
		
		return UvScanRelativeDir;
	}

	
/**
 * Function to get path of result directory specific for a project containing result files 
 * @return path of directory
 */
	public String getUvScanResultsDir()
	{
		return getUvScanDirPath()+Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS;
	}

	
/**
 * Function to get path of configuration directory specific for a project containing configuration files 
 * @return path of directory
 */	
	public String getUvScanConfigDir()
	{

		return getUvScanDirPath()+Configuration.getConstant("UvScanConfigDirName")+Configuration.FPS;
	}


/**
 * Get the exception level of a url
 * @param url
 * @return UrlExceptionLevel
 * @return updated  UvScanUrl object 
 */
	public UrlExceptionLevel getUrlExceptionLevel(String urlID , String locale)
	{
		Map<String, UrlExceptionLevel> map = this.UrlVsLocaleExceptionMap.get(urlID);
		if(map==null)
			return UrlExceptionLevel.None;
		else if(map.get(locale) == null)
			return UrlExceptionLevel.None;
		else
			return map.get(locale);
		}
	
	
/**
 * Function to perform the startup tasks for UV-SCAN , creation of configuration and result folder & files
 * 														Reading Url patterns file and loading the PatternMap
 */
	public void  SetUvScanConfiguration(List<String> locales)
	{
		log.info(" inside SetUvScanConfiguration ");

		createDirForUVSCAN();
		String UrlPatternFilePath = getUvScanDirPath() +Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS+Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileName")+"."+Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileExt");
		String ConfigFilePath = getUvScanDirPath() +Configuration.getConstant("UvScanConfigDirName")+Configuration.FPS+Configuration.getConstant("UvScan_Config_FileName")+"."+Configuration.getConstant("UvScan_Config_FileExt");
		String PatternFilePath = getUvScanDirPath() +Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS+Configuration.getConstant("UvScan_PatternsReport_FileName")+"."+Configuration.getConstant("UvScan_PatternsReport_FileExt");
		this.setLocaleParameters(locales);
		
		this.readUrlPatternsFromResultFile(UrlPatternFilePath);
		this.readUrlPatternsFromConfigFile(PatternFilePath);
		this.readDetailUrlPatternsFromConfigFile(PatternFilePath);
		this.readDateFromConfigFile(PatternFilePath);
		//this.setDetailUrlPatternsFromResultsFile(UrlPatternFilePath);
		
		this.setUVScanTestConfigurations(ConfigFilePath);
		this.setParametersForUrlPatternMatching();
	}


	
/**
 * Function to create configuration and result folder & files if not present already. Creates by copying from location containing sample files
 */
	private  void createDirForUVSCAN()
	{
		log.info(" executing CreateDirForUVSCAN ");

		try{
			String UvScanConfigDirName = Configuration.getConstant("UvScanConfigDirName");
			String UvScanResultsDirName = Configuration.getConstant("UvScanResultsDirName");
			String productDir = getUvScanDirPath();
			Path path = Paths.get(productDir+UvScanConfigDirName+Configuration.FPS);
			Files.createDirectories(path);
			
			//StorageUtilFactory.getStorageUtil().createDirectories(productDir+UvScanConfigDirName+Configuration.FPS);
			path = Paths.get(productDir+UvScanResultsDirName+Configuration.FPS);
			Files.createDirectories(path);
			//StorageUtilFactory.getStorageUtil().createDirectories(productDir+UvScanResultsDirName+Configuration.FPS);

			log.info(" Directories created successfully ");
			
		}catch(Exception e){
			log.error("Exception in createDirForUVSCAN ",e);
		}	  
	}
	
/**
 * Function to set configurable parameters for UV-Scan
 */
	private  void setUVScanTestConfigurations(String UVScanConfigFile)
	{
	//createUVScanTestConfigurationsFile(UVScanConfigFile);
	Properties UVScaProperties = new Properties();
	if(UVScaProperties != null){
		try {
			UVScaProperties.load(new FileInputStream(UVScanConfigFile));
			if((UVScaProperties.getProperty("Regex_For_Url_Parser") != null) && (!UVScaProperties.getProperty("Regex_For_Url_Parser").equals("")))
				Configuration.Regex_For_Url_Parser = UVScaProperties.getProperty("Regex_For_Url_Parser");
			
			if((UVScaProperties.getProperty("Url_Patterns") != null) && (!UVScaProperties.getProperty("Url_Patterns").equals("")))
			{
				Configuration.Url_Patterns = UVScaProperties.getProperty("Url_Patterns");
			}
			createUVScanTestConfigurationsFile(UVScanConfigFile);			
		}  catch(IOException ex){
			//log.error(PropertyReader.getMessage("PROP_LOADING_ERROR", new String[]{UVScanConfigFile}));
			log.info("Creating UVScanTestConfigurations file at location :"+UVScanConfigFile);
			createUVScanTestConfigurationsFile(UVScanConfigFile);
		}
	}
	}

/**
 * Function to write configurable parameters for UV-Scan to properties file
 */
	private  void createUVScanTestConfigurationsFile(String UVScanConfigFile)
	{
		Properties UVScaProperties = new Properties();
		FileOutputStream output = null;
		if(UVScaProperties != null){
			try {
				output = new FileOutputStream(UVScanConfigFile);

				UVScaProperties.setProperty("Regex_For_Url_Parser", Configuration.Regex_For_Url_Parser);
				UVScaProperties.setProperty("Url_Patterns", Configuration.Url_Patterns);

				UVScaProperties.store(output, null);			
			}catch(IOException ex){
				log.error("Unable to create UVScanTestConfigurations file at location :"+UVScanConfigFile,ex);
			}finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
/**
 * Function to load the variables curCountry,curLanguage , etc specific to locale
 */
	public void setLocaleParameters(List<String> locales)
	{
		String localeIdentifierConfigFilePath = this.getUvScanDirPath() +Configuration.getConstant("UvScanConfigDirName")+Configuration.FPS+Configuration.getConstant("UvScan_LocaleCodes_FileName")+"."+Configuration.getConstant("UvScan_LocaleCodes_FileExt");

		log.info("setting localeIdentifiers from file "+localeIdentifierConfigFilePath);

		Reader in =null;
		InputStream inputStream =null;
		CSVParser parser = null;
		JSONObject jsonObj = new JSONObject();

		try
		{
			inputStream=new FileInputStream(new File(localeIdentifierConfigFilePath));
			if(inputStream != null)
				in = new InputStreamReader(inputStream);
			
			CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');

			parser = new CSVParser(in, format);

			for (CSVRecord csvRecord : parser) 
			{
				String curLocale =csvRecord.get("Locale").trim();
				if(!locales.contains(curLocale))
					continue;

				String curCountry = csvRecord.get("Country").trim();
				String curLanguage = csvRecord.get("Language").trim();
				String curAlternateCountry = csvRecord.get("Alternate Locale").trim();
				String curBaseLanguage = csvRecord.get("Fallback Language").trim();
				String curBaseCountry = csvRecord.get("Fallback Country").trim();

				String curBaseLocale = csvRecord.get("Fallback Locale").trim();
				String curLocaleHyphen = curLocale.replaceAll("_","-");
				String curBaseLocaleHyphen = curBaseLocale.replaceAll("_","-");

				jsonObj = new JSONObject();
				jsonObj.put("curLocale", curLocale);
				jsonObj.put("curCountry", curCountry);
				jsonObj.put("curAlternateCountry", curAlternateCountry);
				jsonObj.put("curLanguage", curLanguage);
				jsonObj.put("curBaseLocale", curBaseLocale);
				jsonObj.put("curBaseCountry", curBaseCountry);
				jsonObj.put("curBaseLanguage", curBaseLanguage);
				jsonObj.put("curLocaleHyphen", curLocaleHyphen);
				jsonObj.put("curBaseLocaleHyphen", curBaseLocaleHyphen);

				this.Locales.put(curLocale, jsonObj);
				log.info("parameters loaded successfully");
			}
			JSONObject jsonObjEng = this.Locales.get("en_US");

			for(String locale : locales)
			{
				if(this.Locales.containsKey(locale))
					continue;
				else
					this.Locales.put(locale, jsonObjEng);
			}

		}catch(FileNotFoundException e1)
		{
			log.error("\n"+Configuration.getConstant("UvScan_LocaleCodes_FileName")+"."+Configuration.getConstant("UvScan_LocaleCodes_FileExt")+" not present in Config folder",e1);	
			log.error("\nCreate a file with name '"+Configuration.getConstant("UvScan_LocaleCodes_FileName")+"."+Configuration.getConstant("UvScan_LocaleCodes_FileExt")+"' and place it in Config Folder\n");	
			setLocaleCodesFromConfiguration(locales);
		}
		catch(Exception e2)
		{
			log.error("\n Problem in File Structure of "+Configuration.getConstant("UvScan_LocaleCodes_FileName")+"."+Configuration.getConstant("UvScan_LocaleCodes_FileExt"),e2);
			setLocaleCodesFromConfiguration(locales);
		}
		finally{
				try{
					if(parser!=null)
						parser.close();
					}
					catch(Exception e3){
						log.error("Error while flushing/closing csvparser !!!",e3);

					};
		}
	}

	/**
	 * Function to set locale codes from Configuration class
	 */
	private void setLocaleCodesFromConfiguration(List<String> locales)
	{
		this.Locales = new HashMap<String,JSONObject>();
		JSONObject jsonObjEng = Configuration.localeCodes.get("en_US");
		
		for(String locale : locales)
		{
			if(Configuration.localeCodes.containsKey(locale))
				this.Locales.put(locale, Configuration.localeCodes.get(locale));
			else
				this.Locales.put(locale, jsonObjEng);
		}
		createLocaleCodesFile();
	}
	
/**
 * Function to write LocaleCodes used for UV-Scan to a csv file
 */
	private void createLocaleCodesFile()
	{
		CSVPrinter csvFilePrinter = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer out = new BufferedWriter(new OutputStreamWriter(baos));

		Object [] FILE_HEADER = Configuration.getConstant("UvScan_LocaleCodes_File_Header").split(",");

		try{

			String RECORD_SEPARATOR = Configuration.getConstant("UvScan_LocaleCodes_File_RecordDelimiter");
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(RECORD_SEPARATOR);

			log.info("\n\nGenerating LocaleCodes File\n\n");

			csvFilePrinter = new CSVPrinter(out, csvFileFormat);

			csvFilePrinter.printRecord(FILE_HEADER);

			String ConsolidatedResultFile1Path = this.getUvScanDirPath() +Configuration.getConstant("UvScanConfigDirName")+Configuration.FPS+Configuration.getConstant("UvScan_LocaleCodes_FileName")+"."+Configuration.getConstant("UvScan_LocaleCodes_FileExt");
				
			for(Map.Entry<String,JSONObject> val : Configuration.localeCodes.entrySet())
			{
				String locale = val.getKey();
				JSONObject localeCodes = val.getValue();
				
				ArrayList<String> CsvRecord = new ArrayList<String>();
				CsvRecord.add(locale);
				CsvRecord.add(localeCodes.getString("curCountry"));
				CsvRecord.add(localeCodes.getString("curAlternateCountry"));
				CsvRecord.add(localeCodes.getString("curLanguage"));
				CsvRecord.add(localeCodes.getString("curBaseLocale"));
				CsvRecord.add(localeCodes.getString("curBaseCountry"));
				CsvRecord.add(localeCodes.getString("curBaseLanguage"));				
							
				csvFilePrinter.printRecord(CsvRecord);
			}
	
			if(csvFilePrinter!=null)
			{
				csvFilePrinter.close();
			}
			
			long startTime =  System.currentTimeMillis();			
			log.info("Calling a method to upload a LocaleCodes file to a remote ftp server at " + startTime);
			try{
				byte[] bytes = baos.toByteArray();
				  FileOutputStream fos = new FileOutputStream(ConsolidatedResultFile1Path);
					fos.write(bytes);
					fos.close();
			}catch(IOException ioe){
				log.error("\nIOException occured while uploading FTP file to server "+ConsolidatedResultFile1Path,ioe);
			}
			log.info("File has been uploaded in " + (System.currentTimeMillis() - startTime) + " miliseconds");

			
			log.info("CSV File LocaleCodes.csv updated successfully !!!");    	            

		}catch(Exception e)
		{
			log.error("\nException occured in File LocaleCodes.csv ",e);
		} finally {
			try{
				if(csvFilePrinter!=null)
				{
					csvFilePrinter.close();
				}
			} catch (IOException e) {
				log.error("Error while flushing/closing csvPrinter !!!",e);
			}
		}
		
	}
	
	
/**
 * Function to set general pattern string(regex) for URL pattern matching
 */
	private void setParametersForUrlPatternMatching()
	{
		String []regexArray = Configuration.Url_Patterns.trim().split(",");

		for(String regex : regexArray)
		{
			combinedRegexForPatternMatching += (regex.trim()+"|"); 
			String patternString = regex.substring(regex.indexOf("(") + 1, regex.lastIndexOf(")"));
			replacementMapForPatternMatching.put(patternString.trim(), patternString.trim());
		}
	}
	
private ArrayList<ArrayList<ExcelCell>> getThirdSheetMaster(Object [] FILE_HEADER, ArrayList<ExcelCell> header, ArrayList<String> locales)
{
	ArrayList<ArrayList<ExcelCell>> dateRecords = new ArrayList<ArrayList<ExcelCell>>();
	LastRunMap lrm=null;
	for(int i=1; i<FILE_HEADER.length; i++)
	{ 
		ExcelCell cell = new ExcelCell(FILE_HEADER[i].toString(), null);
		header.add(cell);
	}
	dateRecords.add(header);
	
	Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.UrlMapFromPatternsFile);
	for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
	{
		if(this.LastRunDateMap!=null)
		lrm= this.LastRunDateMap.get(val.getKey());
		if(lrm==null)
		lrm= new LastRunMap();
		for(String locale : this.Locales.keySet())
		{
			lrm.setById(locale, Utility.getDateInExpectedFormat("today"));
		}
		this.LastRunDateMap.put(val.getKey(), lrm);
	}
	sortedMap = new TreeMap<String, UvScanUrl>(this.ConfigUrlMapFromPatternsFile);
	for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
	{
		if(this.LastRunDateMap!=null)
		lrm=this.LastRunDateMap.get(val.getKey());
		ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
		
		record.add(new ExcelCell(val.getKey(),null));
		for(String locale : locales)
		{
			String date;
			if(lrm==null)
				date="01/01/1970";
			else
				date=lrm.getById(locale)!=null?lrm.getById(locale):"01/01/1970";
			record.add(new ExcelCell(date,null));
		}
		dateRecords.add(record);
	}
	return dateRecords;
}
	private ArrayList<ArrayList<ExcelCell>> getSecondSheetMaster(Object [] FILE_HEADER, ArrayList<ExcelCell> header, ArrayList<String> locales)
	{
		ArrayList<ArrayList<ExcelCell>> detailedRecordsForPatternReport = new ArrayList<ArrayList<ExcelCell>>();
		ExcelCell cell = null;
		for(int i=1; i<FILE_HEADER.length; i++)
		{ 
			cell = new ExcelCell(FILE_HEADER[i].toString()+" "+Configuration.getConstant("Base"), null);
			header.add(cell);
			cell = new ExcelCell(FILE_HEADER[i].toString()+" "+Configuration.getConstant("Redirection"), null);
			header.add(cell);
		}
		detailedRecordsForPatternReport.add(header);
		ArrayList<ArrayList<ExcelCell>> helpResourceUrls = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> untestedUrls = new ArrayList<ArrayList<ExcelCell>>();
		Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.UrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			this.ConfigUrlMapFromPatternsFile.put(val.getKey(), val.getValue());
		}
		sortedMap = new TreeMap<String, UvScanUrl>(this.ConfigUrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			String UrlID = val.getKey();
			UvScanUrl configUrl = val.getValue();
			ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
				record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			else record.add(new ExcelCell(UrlID,null));
			int untestedLocalesCount = 0;
			for(String locale : locales)
			{
				String cellValue = this.ConfigDetailedBaseForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				UrlExceptionLevel exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				Color colorCode = Utility.getColorCodeFromExceptionType(exceptionType);

				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					untestedLocalesCount++;
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
				}
				cellValue = this.ConfigDetailedRedirectionForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				colorCode = Utility.getColorCodeFromExceptionType(exceptionType);

				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					//untestedLocalesCount++;
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
				}
			}
			
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
			{
				helpResourceUrls.add(record);
				//this.helpResources=this.helpResources + 1;
			}
			else if(locales.size() == untestedLocalesCount)
				untestedUrls.add(record);
			else
				detailedRecordsForPatternReport.add(record);
		}
		detailedRecordsForPatternReport.addAll(helpResourceUrls);
		detailedRecordsForPatternReport.addAll(untestedUrls);
		return detailedRecordsForPatternReport;
	}
	
	private ArrayList<ArrayList<ExcelCell>> getThirdSheetFinal(Object [] FILE_HEADER)
	{
		ArrayList<ArrayList<ExcelCell>> recordsForStatusWiseReport = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ExcelCell> header = new ArrayList<ExcelCell>();
		ExcelCell cell= null;
		for(int i=0; i<FILE_HEADER.length; i++)
		{ 
			cell = new ExcelCell(FILE_HEADER[i].toString(), null);
			header.add(cell);
		}
		recordsForStatusWiseReport.add(header);

		for (UvScanResultBean resultBean : this.UvScanResultBeanMap)				
		{
			ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
			record.add(new ExcelCell(resultBean.getLocale(),null));
			record.add(new ExcelCell(resultBean.getTotalCount()+"",null));
			record.add(new ExcelCell(resultBean.getPassedCount()+"",null));
			record.add(new ExcelCell(resultBean.getPassedWithExceptionCount()+"",null));
			//record.add(new ExcelCell(resultBean.getPassedWithExceptionUrls(),null));
			record.add(new ExcelCell(resultBean.getFailedCount()+"",null));
			//record.add(new ExcelCell(resultBean.getFailedUrls(),null));
			//record.add(new ExcelCell(resultBean.getNewUrlCount()+"",null));
			//record.add(new ExcelCell(resultBean.getNewUrls(),null));
			//record.add(new ExcelCell(resultBean.getDeletedUrlCount()+"",null));
			//record.add(new ExcelCell(resultBean.getDeletedUrls(),null));
			
			recordsForStatusWiseReport.add(record);
		}
		return recordsForStatusWiseReport;
	}
	
	private ArrayList<ArrayList<ExcelCell>> getSecondSheetFinal(Object [] FILE_HEADER, ArrayList<String> locales)
	{
		ArrayList<ArrayList<ExcelCell>> detailedRecordsForUrlWiseReport = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ExcelCell> header = new ArrayList<ExcelCell>();

		ArrayList<ArrayList<ExcelCell>> helpResourceUrls = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> untestedUrls = new ArrayList<ArrayList<ExcelCell>>();
		ExcelCell cell = new ExcelCell(FILE_HEADER[0].toString(), null);
		header.add(cell);
		for(int i=1; i<FILE_HEADER.length; i++)
		{ 
			cell = new ExcelCell(FILE_HEADER[i].toString()+" "+Configuration.getConstant("Base"), null);
			header.add(cell);
			cell = new ExcelCell(FILE_HEADER[i].toString()+" "+Configuration.getConstant("Redirection"), null);
			header.add(cell);
		}
		detailedRecordsForUrlWiseReport.add(header);
		Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.UrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			String UrlID = val.getKey();
			UvScanUrl configUrl = val.getValue();
			ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
				record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			else record.add(new ExcelCell(UrlID,null));
			int untestedLocalesCount = 0;
			for(String locale : locales)
			{
				String cellValue = this.DetailedBaseForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				UrlExceptionLevel exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				Color colorCode = Utility.getColorCodeFromExceptionType(exceptionType);

				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					untestedLocalesCount++;
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
				}
				cellValue = this.DetailedRedirectionForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				colorCode = Utility.getColorCodeFromExceptionType(exceptionType);

				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
				}
			}
			
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
			{
				helpResourceUrls.add(record);
			}
			else if(locales.size() == untestedLocalesCount)
				untestedUrls.add(record);
			else
				detailedRecordsForUrlWiseReport.add(record);
		}
		detailedRecordsForUrlWiseReport.addAll(helpResourceUrls);
		detailedRecordsForUrlWiseReport.addAll(untestedUrls);
		return detailedRecordsForUrlWiseReport;
	}
	
	private ArrayList<ArrayList<ExcelCell>> getFirstSheetFinal(Object [] FILE_HEADER, ArrayList<String> locales)
	{
		ArrayList<ExcelCell> header = new ArrayList<ExcelCell>();
		ArrayList<ArrayList<ExcelCell>> recordsForUrlWiseReport = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> helpResourceUrls = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> untestedUrls = new ArrayList<ArrayList<ExcelCell>>();
		Map<String,String> mapKey= new HashMap<String,String>();
		for(int i=0; i<FILE_HEADER.length; i++)
		{ 
			ExcelCell cell = new ExcelCell(FILE_HEADER[i].toString(), null);
			header.add(cell);
		}
		recordsForUrlWiseReport.add(header);
		Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.UrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			String UrlID = val.getKey();
			UvScanUrl configUrl = val.getValue();
			ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
				record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			else record.add(new ExcelCell(UrlID,null));
			record.add(new ExcelCell(configUrl.getConfigUrlPattern(),null));
			record.add(new ExcelCell(configUrl.getConfigRedirectedUrlPattern(),null));
			record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			record.add(new ExcelCell(configUrl.getConfigEngRedirectedUrl(),null));
			record.add(new ExcelCell(configUrl.getPatternRemarks(),null));
			record.add(new ExcelCell(configUrl.getUserRemarks(),null));

			int untestedLocalesCount = 0;
			for(String locale : locales)
			{
				String cellValue = this.RecordsForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				UrlExceptionLevel exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				Color colorCode = Utility.getColorCodeFromExceptionType(exceptionType);
				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					untestedLocalesCount++;
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
				}
			}
			
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
			{
				helpResourceUrls.add(record);
			}
			else if(locales.size() == untestedLocalesCount)
				untestedUrls.add(record);
			else
				recordsForUrlWiseReport.add(record);
		}
		recordsForUrlWiseReport.addAll(helpResourceUrls);
		recordsForUrlWiseReport.addAll(untestedUrls);
		return recordsForUrlWiseReport;
	}
	
	private ArrayList<ArrayList<ExcelCell>> getFirstSheetMaster(Object [] FILE_HEADER, ArrayList<ExcelCell> header, ArrayList<String> locales)
	{
		ArrayList<ArrayList<ExcelCell>> recordsForPatternReport = new ArrayList<ArrayList<ExcelCell>>();
		for(int i=0; i<FILE_HEADER.length; i++)
		{ 
			ExcelCell cell = new ExcelCell(FILE_HEADER[i].toString(), null);
			header.add(cell);
		}
		recordsForPatternReport.add(header);
		ArrayList<ArrayList<ExcelCell>> helpResourceUrls = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> untestedUrls = new ArrayList<ArrayList<ExcelCell>>();
		Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.UrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			String UrlID = val.getKey();
			UvScanUrl configUrl = val.getValue();
			String now_locbase = configUrl.getConfigUrlPattern();
			String now_locredirect = configUrl.getConfigRedirectedUrlPattern();
			String now_engbase = configUrl.getConfigEngUrl();
			String now_engredirect = configUrl.getConfigEngRedirectedUrl();
			String pre_locbase = "";
			String pre_locredirect = "";
			String pre_engbase = "";
			String pre_engredirect = "";
			if(this.ConfigUrlMapFromPatternsFile.get(val.getKey())!=null)
			{
				UvScanUrl newConfigUrl=this.ConfigUrlMapFromPatternsFile.get(val.getKey());
				pre_locbase = newConfigUrl.getConfigUrlPattern();
				pre_locredirect = newConfigUrl.getConfigRedirectedUrlPattern();
				pre_engbase = newConfigUrl.getConfigEngUrl();
				pre_engredirect = newConfigUrl.getConfigEngRedirectedUrl();
			}
			if((!now_locbase.equals(pre_locbase))||(!now_locredirect.equals(pre_locredirect))||(!now_engbase.equals(pre_engbase))||(!now_engredirect.equals(pre_engredirect)))
				{
					this.DateMap.put(UrlID,Utility.getDateInExpectedFormat("today"));
				}
			else if((this.DateMap.get(UrlID)==null)||(this.DateMap.get(UrlID)==""))
				this.DateMap.put(UrlID,Utility.getDateInExpectedFormat("today"));
			this.ConfigUrlMapFromPatternsFile.put(val.getKey(), val.getValue());
			
		}
		final SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String currentGMTTime = sdf.format(new Date());
		sortedMap = new TreeMap<String, UvScanUrl>(this.ConfigUrlMapFromPatternsFile);
		for(Map.Entry<String,UvScanUrl> val :sortedMap.entrySet())
		{
			String UrlID = val.getKey();
			UvScanUrl configUrl = val.getValue();
			ArrayList<ExcelCell> record = new ArrayList<ExcelCell>();
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
				record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			else record.add(new ExcelCell(UrlID,null));
			record.add(new ExcelCell(configUrl.getConfigUrlPattern(),null));
			record.add(new ExcelCell(configUrl.getConfigRedirectedUrlPattern(),null));
			record.add(new ExcelCell(configUrl.getConfigEngUrl(),null));
			record.add(new ExcelCell(configUrl.getConfigEngRedirectedUrl(),null));
			record.add(new ExcelCell(configUrl.getPatternRemarks(),null));
			record.add(new ExcelCell(configUrl.getUserRemarks(),null));
			record.add(new ExcelCell(this.DateMap.get(UrlID),null));
			//record.add(new ExcelCell("",null));
			int untestedLocalesCount = 0;
			for(String locale : locales)
			{
				String cellValue = this.ConfigRecordsForLocaleVsUrlReportMap.get(locale).getById(UrlID);
				UrlExceptionLevel exceptionType = this.getUrlExceptionLevel(UrlID, locale);
				Color colorCode = Utility.getColorCodeFromExceptionType(exceptionType);
				UvScanResultBean urb = LocaleMapBean.get(locale);
				if(urb==null)
					urb= new UvScanResultBean();
				if(exceptionType.equals(UrlExceptionLevel.Delete))
				{
					untestedLocalesCount++;
					record.add(new ExcelCell("",colorCode));
				}
				else
				{
					record.add(new ExcelCell(cellValue,colorCode));
					/*log.info(ConfigRecordsForLocaleVsUrlReportMap.size());
					log.info(locale);
					log.info(UrlID);
					
					log.info(cellValue);*/
					if(cellValue!=null){
					if(cellValue.startsWith(Configuration.getConstant("Display_Message_For_PassedWithException_Url")))
						urb.setPassedWithExceptionCount(urb.getPassedWithExceptionCount()+1);
					else if(cellValue.startsWith(Configuration.getConstant("Display_Message_For_Passed_Url")))
						urb.setPassedCount(urb.getPassedCount()+1);
					else
						urb.setFailedCount(urb.getFailedCount()+1);
					urb.setTotalCount(urb.getTotalCount()+1);
					}
					
					urb.setLastUpdated(currentGMTTime);
					urb.setLocale(locale);
				}
				LocaleMapBean.put(locale,urb);
			}
			
			if((UrlID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
			{
				helpResourceUrls.add(record);
			}
			else if(locales.size() == untestedLocalesCount)
				untestedUrls.add(record);
			else
				recordsForPatternReport.add(record);
		}
		recordsForPatternReport.addAll(helpResourceUrls);
		recordsForPatternReport.addAll(untestedUrls);
		return recordsForPatternReport;
	}
	
	private ArrayList<String> getLocalesMaster(int len, ArrayList<ExcelCell> loc_header, ArrayList<String> locales )
	{
		for(int i=8;i<len;i++)
		{
			locales.remove(loc_header.get(i).getCellValue());
			locales.add(loc_header.get(i).getCellValue());
		}
		locales.remove("en_US");
		locales.add(0, "en_US");
		return locales;
	}
	
	private String getValueFromExcel(ArrayList<ExcelCell> record, HashMap<String,Integer> columnHeaderMap, String key)
	{
		ExcelCell cell = record.get(columnHeaderMap.get(key).intValue());
		String value = (cell == null ?"" : cell.getCellValue());
		return value;
	}
	private HashMap<String,Integer> readAllHeader(ArrayList<ExcelCell> header)
	{
		HashMap<String,Integer> columnHeaderMap = new HashMap<String, Integer>();
		for(int i=0; i<header.size(); i++)
		{ 
		   columnHeaderMap.put(header.get(i).getCellValue(),new Integer(i));
		}
		return columnHeaderMap;
	}
	
	private ArrayList<String> readLocalesFromHeaderStatus(ArrayList<ExcelCell> header)
	{
		ArrayList<String> locales = new ArrayList<String>();
		for(int i=0; i<header.size(); i++)
		{ 
		   if(i>=8)
		   locales.add(header.get(i).getCellValue());
		}
		return locales;
	}
	
	private ArrayList<String> readLocalesFromHeaderDate(ArrayList<ExcelCell> header)
	{
		ArrayList<String> locales = new ArrayList<String>();
		for(int i=2; i<header.size(); i++)
		{ 
		   locales.add(header.get(i).getCellValue());
		}
		return locales;
	}
	
	private ArrayList<String> readLocalesFromHeaderDetail(ArrayList<ExcelCell> header)
	{
		ArrayList<String> locales = new ArrayList<String>();
		for(int i=0; i<header.size(); i++)
		{ 
		   if(i%2==1)
		   locales.add(header.get(i).getCellValue().split(" ")[0]);
		}
		return locales;
	}
	
	private void readRecordsDetail(HashMap<String,Integer> columnHeaderMap, ArrayList<ArrayList<ExcelCell>> records, ArrayList<String> locales, ArrayList<ExcelCell> header)
	{
		UrlMap baseMapKey= null;
		UrlMap redirectionMapKey= null;
		for(String locale : locales)
		{
			baseMapKey= new UrlMap();
			redirectionMapKey= new UrlMap();
			for(int i=1; i<records.size(); i++)
			{
				ArrayList<ExcelCell> record = records.get(i);
				while(record.size() < header.size())
					record.add(new ExcelCell("", null));
				ExcelCell cell = record.get(columnHeaderMap.get("String ID").intValue());
				String ID = (cell == null ?"" : cell.getCellValue());
				baseMapKey.setById(ID,record.get(columnHeaderMap.get(locale+" "+Configuration.getConstant("Base")).intValue()).getCellValue());
				redirectionMapKey.setById(ID,record.get(columnHeaderMap.get(locale+" "+Configuration.getConstant("Redirection")).intValue()).getCellValue());
			}
			this.ConfigDetailedBaseForLocaleVsUrlReportMap.put(locale,baseMapKey);
			this.ConfigDetailedRedirectionForLocaleVsUrlReportMap.put(locale,redirectionMapKey);
		}
	}
	
	private void readDateRecords (HashMap<String,Integer> columnHeaderMap, ArrayList<ArrayList<ExcelCell>> records, ArrayList<String> locales, ArrayList<ExcelCell> header)
	{
		for(int i=1; i<records.size(); i++)
		{
			ArrayList<ExcelCell> record = records.get(i);
			while(record.size() < header.size())
				record.add(new ExcelCell("", null));
			
			String ID = getValueFromExcel(record, columnHeaderMap, "String ID");
			LastRunMap lrm=new LastRunMap();
			for(String locale : locales)
			{
				String date= getValueFromExcel(record, columnHeaderMap, locale);
				lrm.setById(locale,date);
			}
			this.LastRunDateMap.put(ID, lrm);
		}
	}
	
	private void readRecordsStatus(HashMap<String,Integer> columnHeaderMap, ArrayList<ArrayList<ExcelCell>> records, ArrayList<String> locales, ArrayList<ExcelCell> header)
	{
		UrlMap mapKey= null;
		for(int i=1; i<records.size(); i++)
		{
			ArrayList<ExcelCell> record = records.get(i);
			while(record.size() < header.size())
				record.add(new ExcelCell("", null));
			
			String ID = getValueFromExcel(record, columnHeaderMap, "String ID");
			String basePattern = getValueFromExcel(record, columnHeaderMap, "Base URL Pattern");
			String redirectionPattern = getValueFromExcel(record,columnHeaderMap, "Redirection URL Pattern");
			String engBaseUrl= getValueFromExcel(record,columnHeaderMap,"English Base URL");
			String engRedirectedUrl = getValueFromExcel(record,columnHeaderMap,"English Redirection URL");
			String patternRemarks = getValueFromExcel(record,columnHeaderMap, "Pattern Remarks");
			String userRemarks= getValueFromExcel(record,columnHeaderMap, "User Remarks");
			String lastupdated = getValueFromExcel(record,columnHeaderMap, "Last Updated");
			
			ExcelCell cell = null;
			this.DateMap.put(ID,lastupdated);
			UvScanUrl u = new UvScanUrl(ID , basePattern , redirectionPattern , engBaseUrl , engRedirectedUrl , patternRemarks , userRemarks);
			this.ConfigUrlMapFromPatternsFile.put(ID, u);
			
			Map<String,UrlExceptionLevel> map= null;
			if(this.UrlVsLocaleExceptionMap.get(ID)!=null)
				map=this.UrlVsLocaleExceptionMap.get(ID);
			else
				map=new HashMap<String,UrlExceptionLevel>();
			for(String locale : locales)
			{
				Color code = null;
				if(columnHeaderMap.get(locale)!=null)
				{
					cell = record.get(columnHeaderMap.get(locale).intValue());
					code = (cell == null? null : cell.getForegroundColorObject());
				}
				
				UrlExceptionLevel exceptionType = Utility.getExceptionTypeFromColorCode(code);
				if(map.get(locale)==null)//||map.get(locale)==UrlExceptionLevel.None)
				map.put(locale, exceptionType);
			}
			this.UrlVsLocaleExceptionMap.put(ID, map);
				
		}
		for(String locale : locales)
		{
			mapKey= new UrlMap();
			for(int i=1; i<records.size(); i++)
			{
				ArrayList<ExcelCell> record = records.get(i);
				while(record.size() < header.size())
					record.add(new ExcelCell("", null));
				String ID = getValueFromExcel(record,columnHeaderMap, "String ID");
				mapKey.setById(ID,record.get(columnHeaderMap.get(locale).intValue()).getCellValue());
			}
			this.ConfigRecordsForLocaleVsUrlReportMap.put(locale,mapKey);
		}
	}
	
	private void readRecordsFinal(HashMap<String,Integer> columnHeaderMap, ArrayList<ArrayList<ExcelCell>> records, ArrayList<ExcelCell> header)
	{
		for(int i=1; i<records.size(); i++)
		{
			ArrayList<ExcelCell> record = records.get(i);
			while(record.size() < header.size())
				record.add(new ExcelCell("", null));
			
			ExcelCell cell = null;
			
			String ID = getValueFromExcel(record, columnHeaderMap, "String ID");
			String basePattern = getValueFromExcel(record, columnHeaderMap, "Base URL Pattern");
			String redirectionPattern = getValueFromExcel(record,columnHeaderMap, "Redirection URL Pattern");
			String engBaseUrl= getValueFromExcel(record,columnHeaderMap,"English Base URL");
			String engRedirectedUrl = getValueFromExcel(record,columnHeaderMap,"English Redirection URL");
			String patternRemarks = getValueFromExcel(record,columnHeaderMap, "Pattern Remarks");
			//String patternRemarks = "";
			String userRemarks= getValueFromExcel(record,columnHeaderMap, "User Remarks");
			
			UvScanUrl u = new UvScanUrl(ID , basePattern , redirectionPattern , engBaseUrl , engRedirectedUrl , patternRemarks , userRemarks);
			this.UrlMapFromPatternsFile.put(ID, u);
			
			Map<String,UrlExceptionLevel> map= new HashMap<String,UrlExceptionLevel>();
			for(String locale : this.Locales.keySet())
			{
				Color code = null;
				
				if(columnHeaderMap.get(locale)!=null)
				{
					cell = record.get(columnHeaderMap.get(locale).intValue());
					code = (cell == null? null : cell.getForegroundColorObject());
				}
				
				UrlExceptionLevel exceptionType = Utility.getExceptionTypeFromColorCode(code);
				map.put(locale, exceptionType);
			}
			this.UrlVsLocaleExceptionMap.put(ID, map);
		}
	}
/**
 * Function to read Url patterns from result file , load the PatternMap
 * Also load the exception level of Urls which would be used to ignored them in the Url testing
 * @param filePath - FinalResult file path
 */
	private void  readUrlPatternsFromResultFile( String filePath)
	{
		log.info(" reading patterns from Result file");

		try
		{
		ArrayList<ArrayList<ExcelCell>> records = Utility.ReadFromExcelFile(filePath,-1);
	
		if((records == null) || records.size() == 0)
		{
			log.info("No patterns in the file\n");
			return;
		}
		log.info("No of Patterns read : "+(records.size() -1) );
		HashMap<String,Integer> columnHeaderMap = null;
		ArrayList<ExcelCell> header= records.get(0);
		columnHeaderMap = readAllHeader(header);
		readRecordsFinal(columnHeaderMap, records, header);
		
		log.info("Patterns read successfully\n");
		log.info("No of Patterns read : "+(records.size() -1) );
		}catch(Exception e)
		{
			log.error("\nException occured while reading ConsolidatedReportFile :"+filePath,e);
			return;
		}
	}

/**
 * Function to read existing Url patterns and UrlVsLocale records from Config file , load the ExistingPatternMap
 * 
 * String ID, Base URL Pattern, Redirection URL Pattern, English Base URL, English Redirection URL, Pattern Remarks, User Remarks,	 en_US, it_IT, ru_RU, ja_JP,....
 * $xyz		  locbasepattern	locredirectionpattern	 engbasepattern    engredirectionpattern	un/ localized	 userremarks	 Passed Failed Passed Failed....
 * 
 * @param filePath - MasterResult file path
 */
	private void  readUrlPatternsFromConfigFile( String filePath)
	{
		log.info(" reading patterns from Config file");
		ArrayList<String> locales= null;
		try
		{
		ArrayList<ArrayList<ExcelCell>> records = Utility.ReadFromConfigFile(filePath,-1);
	
		if((records == null) || records.size() == 0)
		{
			log.info("No patterns in the file\n");
			return;
		}

		ArrayList<ExcelCell> header= records.get(0);
		
		HashMap<String,Integer> columnHeaderMap = null;
		columnHeaderMap = readAllHeader(header);
		locales= readLocalesFromHeaderStatus(header);
		readRecordsStatus(columnHeaderMap,records,locales,header);
		
		log.info("Config read successfully\n");
		}catch(Exception e)
		{
			log.error("\nException occured while reading ConfigFile :"+filePath,e);
			return;
		}
	}
	
	private void  readDetailUrlPatternsFromConfigFile( String filePath)
	{
		log.info(" reading patterns from DetailConfig file");
		ArrayList<String> locales= null;
		try
		{
		ArrayList<ArrayList<ExcelCell>> records = Utility.ReadFromDetailConfigFile(filePath,-1);
	
		if((records == null) || records.size() == 0)
		{
			log.info("No patterns in the file\n");
			return;
		}
		ArrayList<ExcelCell> header= records.get(0);
		
		HashMap<String,Integer> columnHeaderMap = null;
		columnHeaderMap= readAllHeader(header);
		locales= readLocalesFromHeaderDetail(header);
		readRecordsDetail(columnHeaderMap, records, locales, header);
		
		log.info("Config read successfully\n");
		}catch(Exception e)
		{
			log.error("\nException occured while reading ConfigFile :"+filePath,e);
			return;
		}
	}
	
	private void  readDateFromConfigFile( String filePath)
	{
		log.info(" reading date from Config file");
		ArrayList<String> locales= null;
		try
		{
		ArrayList<ArrayList<ExcelCell>> records = Utility.ReadDateFromConfigFile(filePath,-1);
		
		if((records == null) || records.size() == 0)
		{
			log.info("No patterns in the file\n");
			return;
		}

		ArrayList<ExcelCell> header= records.get(0);
		HashMap<String,Integer> columnHeaderMap = null;
		columnHeaderMap = readAllHeader(header);
		locales= readLocalesFromHeaderDate(header);
		readDateRecords(columnHeaderMap, records, locales, header);
		
		log.info("Date read successfully\n");
		}catch(Exception e)
		{
			log.error("\nException occured while reading ConfigFile :"+filePath,e);
			return;
		}
	}

	
/**
* Function to write the updated url pattern map to the patterns file.
 */
	public void writeUrlPatternsFile()
	{	
		log.info("In updateUrlPatternsFile");    	            
		Object [] FILE_HEADER = Configuration.getConstant("UvScan_LocaleVsUrl_MasterResult_File_Header").split(",");
		String filePath = this.getUvScanDirPath() +Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS+Configuration.getConstant("UvScan_PatternsReport_FileName")+"."+Configuration.getConstant("UvScan_PatternsReport_FileExt");
		ArrayList<ArrayList<ExcelCell>> records = Utility.ReadFromConfigFile(filePath,-1);
		ArrayList<ArrayList<ExcelCell>> recordsForPatternReport = null;
		ArrayList<ArrayList<ExcelCell>> detailedRecordsForPatternReport = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<ArrayList<ExcelCell>> dateRecords = new ArrayList<ArrayList<ExcelCell>>();
		ArrayList<String> locales = new ArrayList<String>(this.Locales.keySet());
		ArrayList<ExcelCell> loc_header=new ArrayList<ExcelCell>();
		ArrayList<ExcelCell> header=new ArrayList<ExcelCell>();
		int len=0;
		if(records == null)
		{
			len=0;
		}
		else if(records.size()!=0)
		{
			loc_header= records.get(0);
			len=loc_header.size();
		}
		
		locales= getLocalesMaster(len, loc_header, locales);
		FILE_HEADER = ArrayUtils.addAll(FILE_HEADER, locales.toArray());  
		recordsForPatternReport = getFirstSheetMaster(FILE_HEADER, header, locales);
	
		/////////////////////////////////////////////////////////////////////////////////////////
		
		FILE_HEADER= new Object[]{"String ID"};
		FILE_HEADER = ArrayUtils.addAll(FILE_HEADER, locales.toArray());  
		header=new ArrayList<ExcelCell>();
		ExcelCell cell = new ExcelCell(FILE_HEADER[0].toString(), null);
		header.add(cell);
		detailedRecordsForPatternReport = getSecondSheetMaster(FILE_HEADER, header, locales);
		
		FILE_HEADER= new Object[]{"String ID"};
		FILE_HEADER = ArrayUtils.addAll(FILE_HEADER, locales.toArray());  
		header=new ArrayList<ExcelCell>();
		cell = new ExcelCell(FILE_HEADER[0].toString(), null);
		header.add(cell);
		dateRecords= getThirdSheetMaster(FILE_HEADER, header,locales);
		log.info(" Writing to Patterns file");
		
		Utility.WriteToPatternFile(recordsForPatternReport, detailedRecordsForPatternReport, dateRecords, filePath);
		
		log.info("UvScan_PatternsReport created successfully !!!");    	            
	}	
/**
* Function to generate the Final report containing results grouped by Urls and locales
 */
	public void generateFinalReportByUrl()
	{
		
		Object [] FILE_HEADER = Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_Header").split(",");
		ArrayList<String> locales = new ArrayList<String>(this.Locales.keySet());
	
		locales.remove("en_US");
		locales.add(0, "en_US");
		log.info("In generateFinalReport");    	              	            
		
		String ConsolidatedResultFilePath = this.getUvScanDirPath() +Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS+Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileName")+"."+Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileExt");

		////////////////////////////////////For 1st Report/////////////////////////////////////////////////
		
		FILE_HEADER = ArrayUtils.addAll(FILE_HEADER, locales.toArray());
		ArrayList<ArrayList<ExcelCell>> recordsForUrlWiseReport = null;
		recordsForUrlWiseReport= getFirstSheetFinal(FILE_HEADER, locales);
		
		////////////////////////////////////For 2nd Report/////////////////////////////////////////////////
		
		FILE_HEADER = new Object[]{"String ID"};		
		FILE_HEADER = ArrayUtils.addAll(FILE_HEADER, locales.toArray());
		ArrayList<ArrayList<ExcelCell>> detailedRecordsForUrlWiseReport = null;
		detailedRecordsForUrlWiseReport= getSecondSheetFinal(FILE_HEADER, locales);
		
		////////////////////////////////////For 3rd Report/////////////////////////////////////////////////
		
		FILE_HEADER = Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_Header").split(",");		
		//"UvScan_LocaleVsStatus_Result_File_Header":"Locale,Total,Passed Count,Passed with Exception Count,Passed with Exception URLs,Failed Count,Failed URLs,New URLs count,New URLs,Deleted URLs count,Deleted URLs",
		ArrayList<ArrayList<ExcelCell>> recordsForStatusWiseReport = null;
		recordsForStatusWiseReport= getThirdSheetFinal(FILE_HEADER);
		
		/////////////////////////////////////////////////////////////////////////////////////////
		log.info(" Writing patterns to Results file..");

		Utility.WriteToExcelFile(recordsForUrlWiseReport, detailedRecordsForUrlWiseReport, recordsForStatusWiseReport ,ConsolidatedResultFilePath);

		log.info("CSV file UvScan_LocaleVsUrl_Result_File created successfully !!!");    	            
	}
	
}
