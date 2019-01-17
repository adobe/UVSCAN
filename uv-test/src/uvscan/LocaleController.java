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
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

import utility.Configuration;
import utility.Utility;
import uvscan.UvScanUrl.UrlExceptionLevel;
import uvscan.UvScanUrl.UrlPatternMatchLevel;

/**
 * Class to handle UV-SCAN functionalities specific to a locale
 * @author aaggarwa
 *
 */
public class LocaleController {

	private static final Logger log =  Logger.getLogger(LocaleController.class);

	private GeneralController generalController;
	private String serverroot= UvScanInit.Results_file;
	private String AlfProductName ;
	private String  AlfCodeName ;
	private String  locale;

	private String curCountry ;
	private String curLanguage ;
	private String curAlternateCountry ;
	private String curBaseLanguage ;
	private String curBaseCountry ;

	private String curLocale ;	
	private String curLocaleHyphen ;
	private String curBaseLocale  ;
	private String curBaseLocaleHyphen;

	//In order of priority
	String []localeIdentifiers ;
	String []localeIdentifierNames ;
	
	Map<String, String> localeReplacementMapForPatternMatching ;
	String localeCombinedRegexForPatternMatching ;

	
	private HashMap <String, UvScanUrl> AllUrlsInProjectMap  ;

	private int totalCount=0,passedCount=0,passedWithExceptionCount=0,failedCount=0,newUrlCount=0 ,deletedUrlCount=0;

	private  String passedWithExceptionUrls = "" ,failedUrls = "" , newUrls = "",deletedUrls = "";

	String []urlPatternSearchList =new String[]{Configuration.countryPatternWord, Configuration.localePatternWord , Configuration.languagePatternWord, Configuration.localeHyphenPatternWord};

	String []replacementListForEng ;
	String []replacementListForIcematch ;
	String []replacementListForAlternateParam ;
	String []replacementListForFallBack ;
	String []replacementListForRandomComparison1 ;
	String []replacementListForRandomComparison2 ;
	String []replacementListForRandomComparison3 ;
	String []replacementListForRandomComparison4 ;
	
	String [][]urlPatternReplacementList;

	public static Map<String,JSONArray> jsonObjHits=new HashMap<String,JSONArray>();
	/**
	 * Default controller
	 */
	public LocaleController()
	{

	}

	/**
	 * Constructor to create controller specific for an ALF project and locale.
	 * @param alfProductName
	 * @param alfCodeName
	 * @param locale
	 * @param generalController--Controller shared among all locale controller to perform certain general functions and use shared resources 
	 */
	public LocaleController(String alfProductName , String  alfCodeName , String  locale , GeneralController generalController)
	{
		this.AlfProductName = alfProductName;
		this.AlfCodeName = alfCodeName;
		this.locale = locale;
		this.generalController = generalController;
		localeReplacementMapForPatternMatching = new HashMap<String, String>();
		localeCombinedRegexForPatternMatching = "";
		
		this.AllUrlsInProjectMap = new HashMap<String, UvScanUrl>();
		this.setLocaleParameters();
		this.setParametersForUrlPatternMatching();
	}


	public HashMap<String, UvScanUrl> getAllUrlsInProjectMap() {
		return AllUrlsInProjectMap;
	}

	public void setAllUrlsInProjectMap(
			HashMap<String, UvScanUrl> allUrlsInProjectMap) {
		
		this.AllUrlsInProjectMap = new HashMap<String, UvScanUrl>(allUrlsInProjectMap);
	}
	
		
/**
 * Function to set the variables curCountry,curLanguage , etc specific to locale
 */
	private void setLocaleParameters()
	{
		try
		{
			JSONObject jsonObj = null;
		if(generalController.getLocales().get(this.locale) != null)
			jsonObj = generalController.getLocales().get(this.locale);
		else
			jsonObj = generalController.getLocales().get("en_US");
	
			curCountry = jsonObj.getString("curCountry");
			curLanguage = jsonObj.getString("curLanguage");
			curAlternateCountry = jsonObj.getString("curAlternateCountry");
			curBaseLanguage = jsonObj.getString("curBaseLanguage");
			curBaseCountry = jsonObj.getString("curBaseCountry");
	
			curLocale = jsonObj.getString("curLocale");
			curLocaleHyphen = jsonObj.getString("curLocaleHyphen");
			curBaseLocale = jsonObj.getString("curBaseLocale");
			curBaseLocaleHyphen = jsonObj.getString("curBaseLocaleHyphen");
			
			localeIdentifiers = new String[]{curLocale , curLocaleHyphen , curBaseLocale ,
											curBaseLocaleHyphen, curCountry , curLanguage , curAlternateCountry , curBaseCountry ,
											curBaseLanguage};
			localeIdentifierNames = new String[]{Configuration.localePatternWord, 
												Configuration.localeHyphenPatternWord , Configuration.localePatternWord , 
												Configuration.localeHyphenPatternWord, Configuration.countryPatternWord , Configuration.languagePatternWord , 
												Configuration.countryPatternWord , Configuration.countryPatternWord , 
												Configuration.languagePatternWord};
	
			replacementListForIcematch =new String[]{curCountry , curLocale , curLanguage , curLocaleHyphen};
			replacementListForAlternateParam =new String[]{curAlternateCountry , curLocale , curLanguage , curLocaleHyphen};
			replacementListForFallBack =new String[]{ curBaseCountry, curBaseLocale , curBaseLanguage , curBaseLocaleHyphen};
			replacementListForRandomComparison1 =new String[]{ curCountry, curCountry , curCountry , curCountry};
			replacementListForRandomComparison2 =new String[]{ curLocale, curLocale , curLocale , curLocale};
			replacementListForRandomComparison3 =new String[]{ curLocaleHyphen, curLocaleHyphen , curLocaleHyphen , curLocaleHyphen};
			replacementListForRandomComparison4 =new String[]{ curLanguage, curLanguage , curLanguage , curLanguage};
			
			urlPatternReplacementList =  new String[][]{	replacementListForIcematch , 
														replacementListForAlternateParam , 
														replacementListForFallBack , 
														replacementListForRandomComparison1 , 
														replacementListForRandomComparison2 , 
														replacementListForRandomComparison3 , 
														replacementListForRandomComparison4 , 
													};
			
			JSONObject jsonObjEng = generalController.getLocales().get("en_US");
			replacementListForEng =new String[]{jsonObjEng.getString("curCountry"), jsonObjEng.getString("curLocale"), jsonObjEng.getString("curLanguage"), jsonObjEng.getString("curLocaleHyphen")};
		}
		catch(Exception e)
		{
			log.error("\n Problem in retrieving appropriate locale parameters\n\n",e);
		}
	
	}

/**
 * Function to set locale specific pattern string(regex) for URL pattern matching
 */
	private void setParametersForUrlPatternMatching()
	{

		for(String replacement : this.generalController.getReplacementMapForPatternMatching().keySet())
		{
			for(int i=this.localeIdentifiers.length -1 ; i >= 0; i--)
			{
				String key = replacement.replace("<localeIdentifier>",this.localeIdentifiers[i]);
				//log.info("key"+ key);
				String value = replacement.replace("<localeIdentifier>",this.localeIdentifierNames[i]);
				//log.info("value"+ value);
				localeReplacementMapForPatternMatching.put(key.toLowerCase() , value);	
			}
		}
		
		String combineRegex = this.generalController.getCombinedRegexForPatternMatching();
		for(int i=0; i < this.localeIdentifiers.length; i++)
		{
			localeCombinedRegexForPatternMatching += (combineRegex.replace("<localeIdentifier>", this.localeIdentifiers[i].toLowerCase())); 
		}
		if(localeCombinedRegexForPatternMatching.charAt(localeCombinedRegexForPatternMatching.length() - 1) == '|')
			localeCombinedRegexForPatternMatching = localeCombinedRegexForPatternMatching.substring(0, localeCombinedRegexForPatternMatching.length()-1);
	}


	/**
	 * Function to set URL patterns and then perform pattern matching
	 * @param timeoutForUrlPing- Timeout set for the URL ping in milliseconds
	 * @return
	 */
	public HashMap<String, UvScanUrl> performPatternMatching(int timeoutForUrlPing) 
	{

		this.generalController.UrlMapFromPatternsFile = setUrlPatterns( generalController.UrlMapFromPatternsFile , timeoutForUrlPing);
		
		if(Configuration.CheckUrlPattern)
			performUrlPatternMatching();
		
		return this.AllUrlsInProjectMap;
	}

	
	/**
	 * Function to get strings from lucene in size of blockSize and get urls from the string
	 * @param offset
	 * @param blockSize
	 * @param getEngUrls
	 * @return HashMap <String, UvScanUrl> : Map of StringID and Url from lucene search string
	 */
	public HashMap <String, UvScanUrl> getUrlsFromLuceneSearchString(boolean getEngUrlsAlso) 
	{
		HashMap <String, UvScanUrl> UrlsInProjectMap = null;

		UrlsInProjectMap = new HashMap<String, UvScanUrl>();
		UrlsInProjectMap = getUrlsFromLuceneJsonString(getEngUrlsAlso);

		
		this.AllUrlsInProjectMap.putAll(UrlsInProjectMap);
		
		
		UrlsInProjectMap.clear();
		UrlsInProjectMap = null;
		
		

		return this.AllUrlsInProjectMap;
	}

	
	/**
	 * Function to parse Lucene Json string and get Urls from them
	 * @param allStringsOfProject - Lucene Json string
	 * @param getEngUrlsAlso - if true Eng Urls will also be retrieved from core string and stored in generalController
	 * @return
	 */
	private HashMap <String, UvScanUrl> getUrlsFromLuceneJsonString(boolean getEngUrlsAlso) 
	{

		HashMap <String, UvScanUrl> UrlsInProjectMap = new HashMap<String, UvScanUrl>();
		try
		{
			JSONArray jsonObjHitsArray	 =  LocaleController.jsonObjHits.get(this.locale);
			System.out.println(LocaleController.jsonObjHits.get(this.locale));
			log.info("No of String object in lucene output json "+jsonObjHitsArray.length());
		
			for(int i =0 ; i< jsonObjHitsArray.length() ; i++)
			{
				JSONObject jsonObjSource = jsonObjHitsArray.getJSONObject(i);
				
				String coreString  = jsonObjSource.getString("Core String");
				String locString  = jsonObjSource.getString("Localized String");
				String stringID  = jsonObjSource.getString("String Id");

				System.out.println(coreString+" "+locString+" "+stringID);
				UvScanUrl newUrl= null;

				ArrayList<String> LocurlsFound = Utility.getUrlFromString(locString);
				System.out.println(locString+" "+LocurlsFound);
				if(LocurlsFound.size()==1 )
				{
					newUrl = new UvScanUrl(stringID , LocurlsFound.get(0)) ;
					newUrl.setExceptionType(this.generalController.getUrlExceptionLevel(stringID,this.locale));
					UrlsInProjectMap.put(stringID, newUrl);

				}else if(LocurlsFound.size() > 1 )
				{
					//log.info("more than 1");
					for(int j=0 ;j<LocurlsFound.size() ; j++)
					{
						String ID = stringID+"("+(j+1)+")" ;
						newUrl = new UvScanUrl(ID , LocurlsFound.get(j));
						newUrl.setExceptionType(this.generalController.getUrlExceptionLevel(stringID,this.locale));

						UrlsInProjectMap.put(ID, newUrl);
					}
				}
				else
				{
					//log.info("could not detect search");
				}
				
				if(getEngUrlsAlso)
				{
					ArrayList<String> EngurlsFound = Utility.getUrlFromString(coreString);

					if(EngurlsFound.size()==1 )
					{
						newUrl = new UvScanUrl(stringID , EngurlsFound.get(0)) ;
						newUrl.setExceptionType(this.generalController.getUrlExceptionLevel(stringID,"en_US"));
						generalController.EngUrlMapFromProductResources.put(stringID, newUrl);

					}else if(EngurlsFound.size() > 1 )
					{
						for(int j=0 ;j<EngurlsFound.size() ; j++)
						{
							String ID = stringID+"("+(j+1)+")" ;
							newUrl = new UvScanUrl(ID , EngurlsFound.get(j));
							newUrl.setExceptionType(this.generalController.getUrlExceptionLevel(stringID,"en_US"));

							generalController.EngUrlMapFromProductResources.put(ID, newUrl);
						}
					}
				}
				
			}
		}catch(Exception e)
		{
			log.error("Exception in parsing Json for extracting urls",e);
		}

		return UrlsInProjectMap ;
	}


	/**
	 * Function to traverse the Url map and update the map with Url response 
	 * @param timeout - timeout for Url ping in milliseconds
	 * @return HashMap <String, UvScanUrl>
	 */
	public HashMap <String, UvScanUrl> getUrlResponse(int timeout)
	{

		log.info("Generating response for "+this.AllUrlsInProjectMap.size()+" urls");
		for (Map.Entry<String,UvScanUrl> val : this.AllUrlsInProjectMap.entrySet())
		{
			UvScanUrl url = val.getValue() ;
			String urlID = val.getKey() ;

			if(url.getExceptionType().equals(UrlExceptionLevel.Delete))
				continue;
			UvScanUrl urlContainingResponse  = generateUrlResponse(url ,timeout, 2);
			this.AllUrlsInProjectMap.put(urlID, urlContainingResponse);
		}	
		return this.AllUrlsInProjectMap;
	}

	/**
	 * Function similar to above(getUrlResponse()) specific for en_US  
	 * @param timeout - timeout for Url ping in milliseconds
	 * @return HashMap <String, UvScanUrl>
	 */
	public HashMap <String, UvScanUrl> getUrlResponseforEng(int timeout)
	{

		log.info("Generating response for "+this.generalController.EngUrlMapFromProductResources.size()+" urls");
		for (Map.Entry<String,UvScanUrl> val : this.generalController.EngUrlMapFromProductResources.entrySet())
		{
			UvScanUrl url = val.getValue() ;
			String urlID = val.getKey() ;

			if(url.getExceptionType().equals(UrlExceptionLevel.Delete))
				continue;
			
			UvScanUrl urlContainingResponse  = generateUrlResponse(url ,timeout,2);
			this.generalController.EngUrlMapFromProductResources.put(urlID, urlContainingResponse);
		}	
		return this.generalController.EngUrlMapFromProductResources;
	}

	/**
	 * Function to ping the url and store its response code , redirected Url and certain parameters depending on them
	 * @param url - UvScanUrl url object
	 * @param timeout - timeout for Url ping in milliseconds
	 * @return updated UvScanUrl url object with response
	 */
	private UvScanUrl generateUrlResponse(UvScanUrl url , int timeout, int trial)
	{
		if(trial<0)
			return url;
		int ResponseCode ;
		String FinalRedirectedUrl = null;
		String ContentOnUrlPage = null;
		
		if(timeout == 0)
			timeout = Integer.parseInt(Configuration.getConstant("TimeoutForUrlPingInMs"));
		if(url.getExceptionType().equals(UrlExceptionLevel.Delete))
			return url;
		String baseUrl = Utility.getformattedUrl(url.getBaseUrl());
		Response response=null;
		try
		{
			String userAgent = Configuration.getConstant("USER_AGENT_HEADER");
			String redirectUrl="";
			while(true)
			{
				response = Jsoup.connect(baseUrl).timeout(timeout).ignoreHttpErrors(true).followRedirects(true).ignoreContentType(true).header("User-Agent", userAgent).execute();
				if (response.hasHeader("location")) {
					redirectUrl = response.header("location");
					baseUrl=redirectUrl;
				}
				else
				break;
			}
			ContentOnUrlPage = response.parse().text();
			FinalRedirectedUrl = response.url().toString();
			ResponseCode = response.statusCode();
			
		
			boolean isAdobeErrorPage = checkIfAdobeErrorPage(FinalRedirectedUrl , ResponseCode , ContentOnUrlPage);
			url.setRedirectedUrl(FinalRedirectedUrl);
			url.setResponseCode(ResponseCode) ;			
			url.setIsErrorPage(isAdobeErrorPage) ;
			if(isAdobeErrorPage)
				url.setRedirectedUrlPatternMatchResult(url
						.getRedirectedUrlPatternMatchResult()
						+ (Configuration.getConstant("Display_Message_For_ErrorPage_Redirection")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			else if(ResponseCode!=200)
			{
				url.setIsErrorPage(true) ;
				url.setRedirectedUrlPatternMatchResult(url
						.getRedirectedUrlPatternMatchResult()
						+ ("Error - Status Code"+ResponseCode+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));

			}
		}catch(UnknownHostException  unknownHostExp)
		{
			log.debug("\n Url Exception occured while hitting link "+baseUrl+"\n\n"+unknownHostExp.getMessage());	
			
			url.setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR) ;			
			url.setIsErrorPage(true) ;
			url.setRedirectedUrlPatternMatchResult(url
					.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			url.setRedirectedUrl(Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection"));
			url.setExceptionOccurred(unknownHostExp.toString());
		}catch( MalformedURLException malformUrlExp)
		{
			log.debug("\n Url Exception occured while hitting link "+baseUrl+"\n\n"+malformUrlExp.getMessage());	
			
			url.setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR) ;			
			url.setIsErrorPage(true) ;
			url.setRedirectedUrlPatternMatchResult(url
					.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			url.setRedirectedUrl(Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection"));
			url.setExceptionOccurred(malformUrlExp.toString());
		}
		catch(org.jsoup.HttpStatusException statusExp)
		{
			statusExp.getUrl();
			log.debug("\n HttpStatusException Exception occured while hitting link "+baseUrl+"\n\n"+statusExp.getMessage());	
			
			url.setResponseCode(statusExp.getStatusCode()) ;			
			url.setIsErrorPage(true) ;
			url.setRedirectedUrlPatternMatchResult(url
					.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			url.setRedirectedUrl(Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection"));
			url.setExceptionOccurred(statusExp.toString());
			
		}catch(IOException IOExp)
		{
				log.debug("\n SocketTimeoutException occured while hitting link "+baseUrl+"\n\n"+IOExp.getMessage());	
				if(trial==0) {
				url.setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR) ;			
				url.setIsErrorPage(true) ;
				url.setRedirectedUrlPatternMatchResult(url
						.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Timeout_Error")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
				url.setRedirectedUrl(Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection"));
				url.setExceptionOccurred(IOExp.toString());
				}
				generateUrlResponse(url ,timeout*2,trial-1);
		}catch(Exception generalExp)
		{
				log.debug("\n General exception occured while hitting link "+baseUrl+"\n\n"+generalExp.getMessage());	
		
				url.setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR) ;			
				url.setIsErrorPage(true) ;
				url.setRedirectedUrlPatternMatchResult(url
						.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
				url.setRedirectedUrl(Configuration.getConstant("Display_Message_For_Unsuccessfull_Redirection"));
				url.setExceptionOccurred(generalExp.toString());
				generateUrlResponse(url ,timeout*2,trial-1);
		}
		return url;
	}

/**
 * Function checks if the url is an Adobe error page depending on url Address,status code and its content
 * @param url - UvScanUrl url object
 * @param responseCode
 * @param contentOnUrlPage
 * @return boolean - if errorPage or not 
 */
	private static boolean checkIfAdobeErrorPage(String url , int responseCode , String contentOnUrlPage)
	{

		if(url.equals(Configuration.getConstant("Adobe_ErrorPage_UrlAddress")))
			return true;
		else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND)
			return true;
		else if (contentOnUrlPage != null)
		{
			for(String identifiers : Configuration.AdobeErrorPageIdentifiers)
			{
				if(contentOnUrlPage.contains(identifiers))
					return true;
			}
		}

		return false;
	}

/**
 * Function to set URL patterns from the UrlPatternMap
 * @param UrlMapFromPatternsFile - Map containing 
 * @param timeoutForUrlPing
 * @return HashMap <String, UvScanUrl> - updated UrlPatternMap
 */
	private  HashMap <String, UvScanUrl> setUrlPatterns( HashMap <String, UvScanUrl> urlMapFromPatternsFile , int timeoutForUrlPing)
	{
		log.info("setting patterns for pattern matching from stored patterns , no of patterns "+urlMapFromPatternsFile.size());

		for (Map.Entry<String,UvScanUrl> val : urlMapFromPatternsFile.entrySet())
		{
			UvScanUrl urlPattern = val.getValue();
			String urlPatternID  = val.getKey();
			UvScanUrl resourceFileUrl =  this.AllUrlsInProjectMap.get(urlPatternID);

			if(resourceFileUrl == null)
			{
				UvScanUrl u = new UvScanUrl(urlPatternID ,urlPattern.getConfigUrlPattern() , urlPattern.getConfigRedirectedUrlPattern() , urlPattern.getConfigEngUrl() , urlPattern.getConfigEngRedirectedUrl() , urlPattern.getPatternRemarks() , urlPattern.getUserRemarks());
				u.setExceptionType(this.generalController.getUrlExceptionLevel(urlPatternID,this.locale));

				if(u.getExceptionType().equals(UrlExceptionLevel.Delete))
					continue;
				if((urlPatternID.toLowerCase()).equals(urlPattern.getConfigEngUrl().toLowerCase()))
				{
					u.setIsDeleted(false);
				}
				else if((urlPatternID.toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))
				{
					u.setBaseUrl(u.getConfigUrlPattern());
					//u = getConfigUrlPattern(u);
					//u.setIsNew(true);
					u.setIsDeleted(false);
				}
				else
				{
					u.setIsDeleted(true);
				}
				u.setIsNew(urlPattern.getIsNew());
				
				if( (u.getConfigUrlPattern() == null) || (u.getConfigUrlPattern().equalsIgnoreCase("")) )
				{
					u.setConfigUrlPattern("");
					u.setBaseUrlPatternMatchResult(u
							.getBaseUrlPatternMatchResult()
							+ (Configuration.getConstant("Display_Message_For_Improper_BaseURLPattern")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));

					u.setPatternRemarks("Unlocalized" +";"+Configuration.getConstant("Display_Message_ColumnDelimiter")+"Improper Pattern"+";"+Configuration.getConstant("Display_Message_ColumnDelimiter"));
				}
				else
				{
					if( (u.getPatternRemarks() == null) || (u.getPatternRemarks().equalsIgnoreCase("")) )
					{	
						u.setBaseUrl(applyLocaleIdentifiers(u.getConfigUrlPattern() , urlPatternSearchList , replacementListForIcematch));
						//u = getConfigUrlPattern(u);
					}
					else
					{
						u.setBaseUrl(applyLocaleIdentifiers(u.getConfigUrlPattern() , urlPatternSearchList , replacementListForIcematch));
						u.setIsNew(false);
					}

					if(this.locale.equals("en_US"))
						u.setBaseUrl(u.getConfigEngUrl());
					
					
					if(Configuration.CheckUrlRedirection)
						u = generateUrlResponse(u , timeoutForUrlPing, 2);
				}

				this.AllUrlsInProjectMap.put(urlPatternID, u);
				urlMapFromPatternsFile.put(urlPatternID, u);
			}
			else
			{
				
				this.AllUrlsInProjectMap.get(urlPatternID).setIsNew(urlPattern.getIsNew());				
				this.AllUrlsInProjectMap.get(urlPatternID).setIsDeleted(false);
				this.AllUrlsInProjectMap.get(urlPatternID).setExceptionType(this.generalController.getUrlExceptionLevel(urlPatternID,this.locale));
				if((this.AllUrlsInProjectMap.get(urlPatternID).getExceptionType()).equals(UrlExceptionLevel.Delete))
					continue;
				UvScanUrl tempUrl = this.AllUrlsInProjectMap.get(urlPatternID);
				
				if( (urlPattern.getConfigUrlPattern() == null)||(urlPattern.getConfigUrlPattern().equalsIgnoreCase(""))
						||(urlPattern.getConfigEngUrl() == null)||(urlPattern.getConfigEngUrl().equalsIgnoreCase(""))
						||(Configuration.CheckUrlRedirection
						&&
						((urlPattern.getConfigRedirectedUrlPattern() == null)||(urlPattern.getConfigRedirectedUrlPattern().equalsIgnoreCase(""))
						||(urlPattern.getConfigEngRedirectedUrl() == null)||(urlPattern.getConfigEngRedirectedUrl().equalsIgnoreCase(""))
						||(urlPattern.getPatternRemarks() == null) || (urlPattern.getPatternRemarks().equalsIgnoreCase(""))
						))
						)
				{
					tempUrl = getConfigUrlPattern(tempUrl);
					tempUrl.setIsNew(true);	

					this.AllUrlsInProjectMap.get(urlPatternID).setConfigUrlPattern(tempUrl.getConfigUrlPattern()) ;
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigRedirectedUrlPattern(tempUrl.getConfigRedirectedUrlPattern());
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigEngUrl(tempUrl.getConfigEngUrl()) ;
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigEngRedirectedUrl(tempUrl.getConfigEngRedirectedUrl());
					
					this.AllUrlsInProjectMap.get(urlPatternID).setIsNew(true);	
					urlMapFromPatternsFile.put(urlPatternID, tempUrl);
				}
				else
				{
					//log.info("chk"+urlPatternID+" "+urlPattern.getConfigUrlPattern());
					//log.info(urlPattern.getConfigRedirectedUrlPattern());
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigUrlPattern(urlPattern.getConfigUrlPattern()) ;
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigRedirectedUrlPattern(urlPattern.getConfigRedirectedUrlPattern()) ;
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigEngUrl(urlPattern.getConfigEngUrl()) ;
					this.AllUrlsInProjectMap.get(urlPatternID).setConfigEngRedirectedUrl(urlPattern.getConfigEngRedirectedUrl()) ;					
					this.AllUrlsInProjectMap.get(urlPatternID).setIsNew(false);	
				}
			}
		}

		for (Map.Entry<String,UvScanUrl> val : this.AllUrlsInProjectMap.entrySet())
		{
			UvScanUrl url = val.getValue();
			String urlPatternID  = val.getKey();
			//log.info(url.getIsNew());
			//log.info(urlPatternID+" "+url);
			if(url.getIsNew() == null)
			{
				url.setIsNew(true);
				url.setIsDeleted(false);
				url = getConfigUrlPattern(this.AllUrlsInProjectMap.get(urlPatternID));
				this.AllUrlsInProjectMap.put(urlPatternID, url);

				urlMapFromPatternsFile.put(url.getStringID(), url);
			}
		}	
		return urlMapFromPatternsFile;
	}

/**
 * 	Each url in urlMap , checked for the pattern match and based on that attributes values are set
 * @return HashMap <String, UvScanUrl> - updated urlMap
 */
	private  HashMap <String, UvScanUrl> performUrlPatternMatching()
	{

		log.info("Now performing patterns matching , for "+this.AllUrlsInProjectMap.size()+" urls ");

		for (Map.Entry<String,UvScanUrl> val : this.AllUrlsInProjectMap.entrySet())
		{
			UvScanUrl url = val.getValue() ;
			String urlID = val.getKey() ;
			
			if( url.getExceptionType().equals(UrlExceptionLevel.Delete) || url.getExceptionType().equals(UrlExceptionLevel.IgnorePatternMatching))
				continue;
				
			if((url.getBaseUrl() == null)||(url.getBaseUrl().equalsIgnoreCase("")))
				continue;

			String matchTypeResultForBaseUrl ="";
			String matchTypeResultForRedirectedUrl ="";

			if(url.getBaseUrl().equals(url.getConfigEngUrl()))
			{
				matchTypeResultForBaseUrl = Configuration.getConstant("Display_Message_For_Eng_Match_BaseUrl");
				url.setIsBaseUrlLocalized(this.locale.equals("en_US"));
				url.setFollowsConfigUrlPattern(this.locale.equals("en_US"));
				url.setFollowsExactConfigUrlPattern(this.locale.equals("en_US"));
			}
			else
			{
				UrlPatternMatchLevel matchTypeBaseUrl = getUrlPatternMatchType(url.getBaseUrl() , url.getConfigUrlPattern());
				matchTypeResultForBaseUrl = getMatchResultForBaseUrl(matchTypeBaseUrl);

				if(matchTypeBaseUrl.equals(UrlPatternMatchLevel.IceMatch))
					url.setFollowsExactConfigUrlPattern(true);
				else
					url.setFollowsExactConfigUrlPattern(false);

				if(matchTypeBaseUrl.equals(UrlPatternMatchLevel.Pattern_MisMatch)||matchTypeBaseUrl.equals(UrlPatternMatchLevel.None))
					url.setFollowsConfigUrlPattern(false);
				else
					url.setFollowsConfigUrlPattern(true);

				url.setIsBaseUrlLocalized(true);
			}

			url.setBaseUrlPatternMatchResult(url
					.getBaseUrlPatternMatchResult() + (matchTypeResultForBaseUrl+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			
			if(!Configuration.CheckUrlRedirection)
				continue;
			
			if((url.getRedirectedUrl() == null)||(url.getRedirectedUrl().equalsIgnoreCase("")) || url.getIsErrorPage())
				continue;//Do nothing
			
			else if(url.getRedirectedUrl().equals(url.getConfigEngRedirectedUrl()))
			{
				matchTypeResultForRedirectedUrl = Configuration.getConstant("Display_Message_For_Eng_Match_RedirectedUrl");
				url.setIsRedirectedUrlLocalized(this.locale.equals("en_US"));
				url.setFollowsConfigRedirectedUrlPattern(this.locale.equals("en_US"));
				url.setFollowsExactConfigRedirectedUrlPattern(this.locale.equals("en_US"));
			}
			else
			{
				UrlPatternMatchLevel matchTypeRedirectedUrl = getUrlPatternMatchType(url.getRedirectedUrl() , url.getConfigRedirectedUrlPattern());
				matchTypeResultForRedirectedUrl = getMatchResultForRedirectionUrl(matchTypeRedirectedUrl);

				if(matchTypeRedirectedUrl.equals(UrlPatternMatchLevel.IceMatch))
					url.setFollowsExactConfigRedirectedUrlPattern(true);
				else
					url.setFollowsExactConfigRedirectedUrlPattern(false);

				if(matchTypeRedirectedUrl.equals(UrlPatternMatchLevel.Pattern_MisMatch) || matchTypeRedirectedUrl.equals(UrlPatternMatchLevel.None))
					url.setFollowsConfigRedirectedUrlPattern(false);
				else
					url.setFollowsConfigRedirectedUrlPattern(true);

				url.setIsRedirectedUrlLocalized(true);
			}

			if(!url.getFollowsExactConfigRedirectedUrlPattern() && !url.getFollowsConfigRedirectedUrlPattern())
			{
				UvScanUrl presumedUrl = doesLocalizeUrlExists(url);
				if(presumedUrl !=null)
					matchTypeResultForRedirectedUrl = matchTypeResultForRedirectedUrl +"; "+"Localized Redirection Url exists : "+presumedUrl.getRedirectedUrl();
			}
			url.setRedirectedUrlPatternMatchResult(url
					.getRedirectedUrlPatternMatchResult() + (matchTypeResultForRedirectedUrl+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));

			this.AllUrlsInProjectMap.put(urlID, url);
		}		

		return this.AllUrlsInProjectMap;
	}

	/**
	 * Function to get the level of match a urlString has with the urlPatternString
	 * @param urlString
	 * @param urlPatternString
	 * @return UrlPatternMatchLevel
	 */
	private  UrlPatternMatchLevel getUrlPatternMatchType(String urlString , String urlPatternString)
	{

		if((urlPatternString == null)||(urlPatternString.equalsIgnoreCase("")))
			return UrlPatternMatchLevel.None;


		if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForIcematch))
			return UrlPatternMatchLevel.IceMatch;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForAlternateParam))
			return UrlPatternMatchLevel.AlternateParam_Match;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForFallBack))
			return UrlPatternMatchLevel.FallBack_Match;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForRandomComparison1))
			return UrlPatternMatchLevel.Random_Match;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForRandomComparison2))
			return UrlPatternMatchLevel.Random_Match;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForRandomComparison3))
			return UrlPatternMatchLevel.Random_Match;
		else if(verifyUrlPattern(urlString, urlPatternString, urlPatternSearchList, replacementListForRandomComparison4))
			return UrlPatternMatchLevel.Random_Match;
		else
			return UrlPatternMatchLevel.Pattern_MisMatch;


	}

/**
 * Function to validate if urlString follows the pattern referred by  urlPatternString
 * @param urlString
 * @param urlPatternString
 * @param searchList
 * @param replacementList
 * @return boolean- whether url follows pattern or not
 */
	private static boolean verifyUrlPattern(String urlString , String urlPatternString , String []searchList, String []replacementList)
	{
		String modifiedUrlString = applyLocaleIdentifiers(urlPatternString, searchList, replacementList);
		if(modifiedUrlString.equalsIgnoreCase(urlString))
			return true;
		else
			return false;
	}

/**
 * Function to replace the locale specific notations in urlPatternString with the corresponding locale identifier to get localized url
 * @param urlPatternString
 * @param searchList - Contains list of locale specific notations
 * @param replacementList - Contains list of corresponding locale identifier
 * @return Localized Url
 */
	private static String applyLocaleIdentifiers(String urlPatternString , String []searchList, String []replacementList)
	{		
		if((urlPatternString==null)||(urlPatternString.equals("")))
			return "";

		for(int i=0 ; i<searchList.length && i<replacementList.length ; i++)
		{
			urlPatternString = StringUtils.replace(urlPatternString, searchList[i], replacementList[i]);
		}
		return urlPatternString;
	}

/**
 * Function to assign messages according to the match level of Base Url
 * @param matchType
 * @return message to be written in result file
 */
	private static String getMatchResultForBaseUrl(UrlPatternMatchLevel matchType)
	{
		
		switch(matchType)
		{
		case None:
			return "";

		case IceMatch:
			return Configuration.getConstant("Display_Message_For_IceMatch_BaseUrl");

		case AlternateParam_Match:
			return Configuration.getConstant("Display_Message_For_AlternateParam_Match_BaseUrl");

		case FallBack_Match:
			return Configuration.getConstant("Display_Message_For_FallBack_Match_BaseUrl");

		case Random_Match:
			return Configuration.getConstant("Display_Message_For_Random_Match_BaseUrl");

		case Pattern_MisMatch:
			return Configuration.getConstant("Display_Message_For_Pattern_MisMatch_BaseUrl");

		default:
			return Configuration.getConstant("Display_Message_For_Pattern_MisMatch_BaseUrl");

		}
	}

	/**
	 * Function to assign messages according to the match level of Redirected Url
	 * @param matchType
	 * @return message to be written in result file
	 */
	private static String getMatchResultForRedirectionUrl(UrlPatternMatchLevel matchType)
	{
		switch(matchType)
		{
		case None:
			return "";

		case IceMatch:
			return Configuration.getConstant("Display_Message_For_IceMatch_RedirectedUrl");

		case AlternateParam_Match:
			return Configuration.getConstant("Display_Message_For_AlternateParam_Match_RedirectedUrl");

		case FallBack_Match:
			return Configuration.getConstant("Display_Message_For_FallBack_Match_RedirectedUrl");

		case Random_Match:
			return Configuration.getConstant("Display_Message_For_Random_Match_RedirectedUrl");

		case Pattern_MisMatch:
			return Configuration.getConstant("Display_Message_For_Pattern_MisMatch_RedirectedUrl");

		default:
			return Configuration.getConstant("Display_Message_For_Pattern_MisMatch_RedirectedUrl");

		}
	}

/**
 * Function to generate the pattern from a localized url by replacing locale specific identifier with notations
 * @param UvScanUrl url
 * @return UvScanUrl url
 */
	private UvScanUrl getConfigUrlPattern(UvScanUrl url)
	{		
		url.setConfigUrlPattern(this.generateUrlPattern(url.getBaseUrl()));
		
		if((url.getConfigUrlPattern() == null)||(url.getConfigUrlPattern().equals("")))
			url.setConfigUrlPattern(Configuration.getConstant("Pattern_Not_Created"));
		
		if(Configuration.CheckUrlRedirection)
		{
			if(url.getRedirectedUrl() == null  || url.getRedirectedUrl().equals(""))
				url = generateUrlResponse(url , 0, 2);	
			
			url.setConfigRedirectedUrlPattern(this.generateUrlPattern(url.getRedirectedUrl()));
			
			if((url.getConfigRedirectedUrlPattern() == null)||(url.getConfigRedirectedUrlPattern().equals(""))||(url.getConfigRedirectedUrlPattern().contains(Configuration.getConstant("Adobe_ErrorPage_UrlAddressPart"))||(url.getIsErrorPage())))
			{
				url.setConfigRedirectedUrlPattern(Configuration.getConstant("Pattern_Not_Created"));	
			}
		}		
		if( (url.getConfigEngUrl() == null) || (url.getConfigEngUrl().equalsIgnoreCase("")) )
		{
			UvScanUrl engUrl = generalController.EngUrlMapFromProductResources.get(url.getStringID());
			if(engUrl !=null)
			{
				url.setConfigEngUrl(generalController.EngUrlMapFromProductResources.get(url.getStringID()).getBaseUrl());
				url.setConfigEngRedirectedUrl(generalController.EngUrlMapFromProductResources.get(url.getStringID()).getRedirectedUrl());			
			}
			else if((url.getStringID().toLowerCase()).contains(Configuration.getConstant("HelpResource_Url_StringID").toLowerCase()))		
				url.setConfigEngUrl(applyLocaleIdentifiers(url.getConfigUrlPattern() , urlPatternSearchList , replacementListForEng));
			
			else
			{
				url.setConfigEngUrl(Configuration.getConstant("Pattern_Not_Created"));
				url.setConfigEngRedirectedUrl(Configuration.getConstant("Pattern_Not_Created"));	
				url.setBaseUrlPatternMatchResult(url
						.getBaseUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Improper_Eng_BaseURLPattern")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
				url.setRedirectedUrlPatternMatchResult(url
						.getRedirectedUrlPatternMatchResult() + (Configuration.getConstant("Display_Message_For_Improper_Eng_RedirectionURLPattern")+";"+Configuration.getConstant("Display_Message_ColumnDelimiter")));
			}
		}
		if(Configuration.CheckUrlRedirection)
		{
			UvScanUrl tempUrl = new UvScanUrl(url.getStringID(), url.getConfigEngUrl());
			tempUrl = generateUrlResponse(tempUrl , 0, 2);
			//log.info("red chk "+tempUrl.getRedirectedUrl());
			if((tempUrl.getRedirectedUrl() == null)||(tempUrl.getRedirectedUrl().equals(""))||(tempUrl.getRedirectedUrl().contains(Configuration.getConstant("Adobe_ErrorPage_UrlAddressPart"))||(tempUrl.getIsErrorPage())))
				url.setConfigEngRedirectedUrl(Configuration.getConstant("Pattern_Not_Created"));
			else 
				url.setConfigEngRedirectedUrl(tempUrl.getRedirectedUrl());	
		}
		

		if( 
			(url.getConfigUrlPattern() != null
			&& (url.getConfigUrlPattern().contains(Configuration.localePatternWord) || url.getConfigUrlPattern().contains(Configuration.localeHyphenPatternWord) || url.getConfigUrlPattern().contains(Configuration.countryPatternWord) || url.getConfigUrlPattern().contains(Configuration.languagePatternWord) )
				)
			&& (!Configuration.CheckUrlRedirection
			|| (url.getConfigRedirectedUrlPattern() != null
			&&	(url.getConfigRedirectedUrlPattern().contains(Configuration.localePatternWord) || url.getConfigRedirectedUrlPattern().contains(Configuration.localeHyphenPatternWord) || url.getConfigRedirectedUrlPattern().contains(Configuration.countryPatternWord) || url.getConfigRedirectedUrlPattern().contains(Configuration.languagePatternWord))
			))
			)
			url.setPatternRemarks("Localized");
		else
			url.setPatternRemarks("Unlocalized");

		if( (url.getConfigUrlPattern() == null
			||url.getConfigUrlPattern().equalsIgnoreCase(Configuration.getConstant("Pattern_Not_Created"))
			||url.getConfigEngUrl() == null 
			|| url.getConfigEngUrl().equalsIgnoreCase(Configuration.getConstant("Pattern_Not_Created"))
			)
			||(Configuration.CheckUrlRedirection
			&&(url.getConfigRedirectedUrlPattern() == null 
			|| url.getConfigRedirectedUrlPattern().equalsIgnoreCase(Configuration.getConstant("Pattern_Not_Created"))
			||url.getConfigEngRedirectedUrl() == null 
			|| url.getConfigEngRedirectedUrl().equalsIgnoreCase(Configuration.getConstant("Pattern_Not_Created"))
			))
			)
			url.setPatternRemarks(url.getPatternRemarks() +";"+Configuration.getConstant("Display_Message_ColumnDelimiter")+"Improper pattern");
		
		return url;
	}

/**
 * Function to replace locale specific identifier with notations from the given Url
 * @param url- Url string for which pattern needs to be generated
 * @return generated pattern from the url string
 */
	private String generateUrlPattern(String url)
	{
		String urlPattern = "";
		try
		{
			urlPattern = Utility.getSubsitutedString(url, localeCombinedRegexForPatternMatching, localeReplacementMapForPatternMatching);
		}catch(Exception e){
			log.error("Exception occured while creating pattern through regex for url :"+url ,e);
		}
		
		return urlPattern;
	}


/**
 * Function to determine if a localized Url exists on the web
 * @param UvScanUrl object containing config pattern 
 * @return UvScanUrl object having the results
 */
	private UvScanUrl doesLocalizeUrlExists(UvScanUrl url)
	{
		String stringID = url.getStringID();
		String redirectionUrlPattern = url.getConfigRedirectedUrlPattern();
		
		if( (redirectionUrlPattern == null) || (redirectionUrlPattern.equalsIgnoreCase(""))  
				|| !((redirectionUrlPattern.contains(Configuration.localePatternWord) || redirectionUrlPattern.contains(Configuration.localeHyphenPatternWord) || redirectionUrlPattern.contains(Configuration.countryPatternWord) || redirectionUrlPattern.contains(Configuration.languagePatternWord)) )
				)
				return null;
		
		for(int i=0 ;i<urlPatternReplacementList.length;i++)
		{
			String presumedRedirectionUrl = "";
			UvScanUrl presumedUrl = null;
			presumedRedirectionUrl = applyLocaleIdentifiers(redirectionUrlPattern,  urlPatternSearchList, urlPatternReplacementList[i]);
			presumedUrl = new UvScanUrl(stringID, presumedRedirectionUrl);
		
			if(!generateUrlResponse(presumedUrl, 0, 2).getIsErrorPage() && presumedRedirectionUrl.equals(presumedUrl.getRedirectedUrl()))
				return presumedUrl;
		}
		
		return null;
	}
	
	
/**
 * Function to generate the locale specific report. Url pass , fail , etc. counted and stored
 */
	public void generateReport()
	{
		log.info(" in generateReport ");
		String localeResultFilePath = generalController.getUvScanDirPath() +Configuration.getConstant("UvScanResultsDirName")+Configuration.FPS+this.locale+"."+Configuration.getConstant("UvScan_Locale_Result_FileExt");
		UrlMap mapKey= null;
		UrlMap configmapKey= null;
		UrlMap configDetailBaseMapKey= null;
		UrlMap configDetailRedirectionMapKey= null;
		UrlMap detailBaseMapKey= null;
		UrlMap detailRedirectionMapKey= null;
		

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter os = new OutputStreamWriter(baos);
		Writer out = new BufferedWriter(new OutputStreamWriter(baos));
	
		CSVPrinter csvFilePrinter = null;

		try
		{
			Object [] FILE_HEADER = Configuration.getConstant("UvScan_Locale_Result_File_Header").split(",");
			String RECORD_SEPARATOR = Configuration.getConstant("UvScan_Locale_Result_File_RecordDelimiter");
			CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(RECORD_SEPARATOR);

			log.info(" Writing to locale report for "+this.locale);

			//mapKey= new HashMap<String,String>();
			mapKey= new UrlMap();
			//detailRedirectionMapKey= new HashMap<String,String>();
			detailRedirectionMapKey= new UrlMap();
			//detailBaseMapKey= new HashMap<String,String>();
			detailBaseMapKey= new UrlMap();
			if(generalController.ConfigRecordsForLocaleVsUrlReportMap.get(this.locale)!=null)
				{
				configmapKey=generalController.ConfigRecordsForLocaleVsUrlReportMap.get(this.locale);
				}
			else
				configmapKey=new UrlMap();
			if(generalController.ConfigDetailedBaseForLocaleVsUrlReportMap.get(this.locale)!=null)
				{
				configDetailBaseMapKey=generalController.ConfigDetailedBaseForLocaleVsUrlReportMap.get(this.locale);
				}
			else
				configDetailBaseMapKey=new UrlMap();
			if(generalController.ConfigDetailedRedirectionForLocaleVsUrlReportMap.get(this.locale)!=null)
				{
				configDetailRedirectionMapKey=generalController.ConfigDetailedRedirectionForLocaleVsUrlReportMap.get(this.locale);
				}
			else
				configDetailRedirectionMapKey=new UrlMap();
			csvFilePrinter = new CSVPrinter(out, csvFileFormat);
			csvFilePrinter.printRecord(FILE_HEADER);     
			
			Map<String, UvScanUrl> sortedMap = new TreeMap<String, UvScanUrl>(this.AllUrlsInProjectMap);
			for (Map.Entry<String,UvScanUrl> val : sortedMap.entrySet())
			{
				UvScanUrl url = val.getValue() ;
				//String urlPatternID= val.getKey();
				if(url.getExceptionType().equals(UrlExceptionLevel.Delete))
					continue;
				//if((this.generalController.getUrlExceptionLevel(urlPatternID,this.locale)).equals(UrlExceptionLevel.Delete))
					
				ArrayList<String> CsvRecord = new ArrayList<String>();		          				
				String BaseUrlMatchResult = Utility.removeStringFromEnd(url.getBaseUrlPatternMatchResult() , '\n');
				String RedirectionUrlMatchResult = Utility.removeStringFromEnd(url.getRedirectedUrlPatternMatchResult() , '\n');
				String UrlStatus = url.getUrlPassStatus();
				CsvRecord.add(url.getStringID());
				CsvRecord.add(url.getConfigEngUrl());
				CsvRecord.add(url.getConfigUrlPattern());
				CsvRecord.add(url.getConfigRedirectedUrlPattern());
				CsvRecord.add(url.getBaseUrl());
				CsvRecord.add(url.getRedirectedUrl());
				CsvRecord.add(url.isErrorPage());
				CsvRecord.add(BaseUrlMatchResult);
				CsvRecord.add(RedirectionUrlMatchResult);
				CsvRecord.add(UrlStatus);
				CsvRecord.add(url.isNewUrl());

				csvFilePrinter.printRecord(CsvRecord);

				this.totalCount++;
				if(UrlStatus.equalsIgnoreCase(Configuration.getConstant("Display_Message_For_Passed_Url")))
				{
					this.passedCount++;
				}
				else if(UrlStatus.equalsIgnoreCase(Configuration.getConstant("Display_Message_For_PassedWithException_Url")))
				{
					this.passedWithExceptionCount++;
					this.passedWithExceptionUrls += url.getBaseUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter");
				}
				else if(UrlStatus.equalsIgnoreCase(Configuration.getConstant("Display_Message_For_Failed_Url")))
				{
					this.failedCount++;
					this.failedUrls += url.getBaseUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter");
				}

				if(url.getIsNew())
				{
					this.newUrlCount++;
					this.newUrls += url.getBaseUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter");
				}

				if(url.getIsDeleted())
				{
					this.deletedUrlCount++;
					this.deletedUrls += url.getBaseUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter");
				}


				/*String LocaleVsUrl_Result_File_Record = UrlStatus+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+
						url.getBaseUrlPatternMatchResult()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+
						url.getRedirectedUrlPatternMatchResult()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+
						url.getBaseUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+
						url.getRedirectedUrl()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+
						url.isNewUrl()+" Url"+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter");
				*/
				String LocaleVsUrl_Result_File_Record=UrlStatus+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+url.getBaseUrlPatternMatchResult()+Configuration.getConstant("UvScan_LocaleVsStatus_Result_File_ColumnDelimiter")+url.getRedirectedUrlPatternMatchResult();
				String LocaleVsUrl_Result_File_RecordAdjusted = LocaleVsUrl_Result_File_Record.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[null]*\r?\n", "");
				String LocaleVsUrl_Base_Record=url.getBaseUrl();
				String LocaleVsUrl_Base_RecordAdjusted = LocaleVsUrl_Base_Record.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[null]*\r?\n", "");
				String LocaleVsUrl_Redirected_Record=url.getRedirectedUrl();
				String LocaleVsUrl_Redirected_RecordAdjusted = LocaleVsUrl_Redirected_Record.replaceAll("(?m)^[ \t]*\r?\n", "").replaceAll("(?m)^[null]*\r?\n", "");
				/*String pre_record="";
				if(configmapKey.get(url.getStringID())!=null)
				{
					pre_record=configmapKey.get(url.getStringID());
					if(!pre_record.equals(LocaleVsUrl_Result_File_RecordAdjusted))
						this.newUrlCount++;
						
				}*/
				mapKey.setById(url.getStringID(), LocaleVsUrl_Result_File_RecordAdjusted);
				detailBaseMapKey.setById(url.getStringID(), LocaleVsUrl_Base_RecordAdjusted);
				detailRedirectionMapKey.setById(url.getStringID(), LocaleVsUrl_Redirected_RecordAdjusted);
				configmapKey.setById(url.getStringID(), LocaleVsUrl_Result_File_RecordAdjusted);
				configDetailBaseMapKey.setById(url.getStringID(), LocaleVsUrl_Base_RecordAdjusted);
				configDetailRedirectionMapKey.setById(url.getStringID(), LocaleVsUrl_Redirected_RecordAdjusted);
			}

			String key = this.locale;
			//generalController.RecordsForLocaleVsUrlReportMap.put(key,mapKey);
			generalController.RecordsForLocaleVsUrlReportMap.put(key,mapKey);
			generalController.ConfigRecordsForLocaleVsUrlReportMap.put(key,configmapKey);
			generalController.DetailedBaseForLocaleVsUrlReportMap.put(key,detailBaseMapKey);
			generalController.ConfigDetailedBaseForLocaleVsUrlReportMap.put(key,configDetailBaseMapKey);
			generalController.DetailedRedirectionForLocaleVsUrlReportMap.put(key,detailRedirectionMapKey);
			generalController.ConfigDetailedRedirectionForLocaleVsUrlReportMap.put(key,configDetailRedirectionMapKey);
			if(csvFilePrinter!=null)
			{
				csvFilePrinter.close();
			}
			

			
			long startTime =  System.currentTimeMillis();			
			log.info("Calling a method to upload a LEX file to a remote ftp server at " + startTime);
			try{
				byte[] bytes = baos.toByteArray();
				  FileOutputStream fos = new FileOutputStream(localeResultFilePath);
					fos.write(bytes);
					fos.close();
				//StorageUtilFactory.getStorageUtil().byteStreamUpload(baos.toByteArray(), localeResultFilePath);
			}catch(IOException ioe){
				log.error("\nIOException occured while uploading FTP file to server "+localeResultFilePath,ioe);
			}
			log.info("File has been uploaded in " + (System.currentTimeMillis() - startTime) + " miliseconds");
			
			log.info("locale specific CSV File updated successfully !!!");    	            

		}catch(IOException e)
		{
			log.error("\nException occured in File UvScan_Locale_Result_File ",e);
		} finally {
			try{
				baos.close();
				if(csvFilePrinter!=null)
				{
					csvFilePrinter.close();
				}
			} catch (IOException e) {
				log.error("Error while flushing/closing fileWriter/csvPrinter !!!",e);
			}
		}	
	}
	
/**
 * Function to populate the result bean from the calculated values
 * @return UvScanResultBean -representing result for a locale for url testing
 */
	public UvScanResultBean getUvScanResultBean()
	{
		UvScanResultBean urb = null;
		urb = new UvScanResultBean();

		if((this.AlfProductName == null)||(this.AlfCodeName == null)||(this.locale == null)||(this.totalCount < 0)||(this.passedCount < 0)
				||(this.passedWithExceptionCount < 0)||(this.failedCount < 0))

			return null;

		urb.setAlfProductName(this.AlfProductName);
		urb.setAlfCodeName(this.AlfCodeName);
		urb.setLocale(this.locale);
		urb.setTotalCount(this.totalCount);
		urb.setPassedCount(this.passedCount);
		urb.setPassedWithExceptionCount(this.passedWithExceptionCount);
		urb.setFailedCount(this.failedCount);
		urb.setPassedWithExceptionUrls(this.passedWithExceptionUrls);
		urb.setFailedUrls(this.failedUrls);
		urb.setDeletedUrlCount(this.deletedUrlCount);
		urb.setDeletedUrls(this.deletedUrls);
		urb.setNewUrlCount(this.newUrlCount);
		urb.setNewUrls(this.newUrls);

		final SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String currentGMTTime = sdf.format(new Date());

		urb.setLastUpdated(currentGMTTime);

		String serverHttpPath =serverroot + "\\";
		/*String ftpPropLocation = "C:\\Users\\shrbhatt\\Documents\\test\\";
		Properties ftpProperties = new Properties();
		if(ftpPropLocation != null){
			try {
				ftpProperties.load(new FileInputStream(ftpPropLocation));
				serverHttpPath = ftpProperties.getProperty("ALF_NFS_URL");
			}  catch(IOException ex){
				//log.error(PropertyReader.getMessage("PROP_LOADING_ERROR", new String[]{ftpPropLocation}));
			}
		}*/


		serverHttpPath = serverHttpPath.replaceAll("StringConsistencyChecker/", "");
		serverHttpPath = serverHttpPath.replaceAll("StringConsistencyChecker", "");
		String serverPath = serverHttpPath + Configuration.getConstant("UvScan_FolderName_OnServer") +Configuration.FPS+generalController.getRelativeUvScanDirPath();		

		serverPath = serverPath.replace('\\', '/');
		String resultMsg = Configuration.getConstant("UvScanResultMessage1");
		resultMsg = resultMsg+"Total Urls: "+this.totalCount;
		resultMsg = resultMsg+"\n"+"Passed Urls: "+this.passedCount;
		resultMsg = resultMsg+"\n"+"Passed with Exception Urls: "+this.passedWithExceptionCount;
		resultMsg = resultMsg+"\n"+"Failed Urls: "+this.failedCount;
		resultMsg = resultMsg+"\n"+Configuration.getConstant("UvScanResultMessage2")+"<a href="+serverPath+" target=\"_blank\" >"+Configuration.getConstant("UvScanResultMessage3")+"</a>";

		urb.setResultMessage(resultMsg);

		generalController.UvScanResultBeanMap.add(urb);
		return urb;
	}
	
/**
 * Function to convert csv to json array
 * @throws JSONException 
 */
	public void getJSON(String csvpath) throws IOException, JSONException
	{
		String defaultEncoding = "UTF-8";
		InputStream in = new FileInputStream(csvpath);
		BOMInputStream bOMInputStream = new BOMInputStream(in);
	    ByteOrderMark bom = bOMInputStream.getBOM();
	    String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
	    InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);
	 
	    CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',');

		CSVParser parser = new CSVParser(reader, format);
		for (CSVRecord record : parser) {
		    String id = record.get("String Id");
		    String core = record.get("Core String");
		    String loc = record.get("Localized String");
		    String locale = record.get("Locale");
		    JSONObject obj = new JSONObject();
		    obj.put("String Id",id);
		    obj.put("Core String", core);
		    obj.put("Localized String", loc);
		    JSONArray jArray = jsonObjHits.get(locale)!=null ? jsonObjHits.get(locale): new JSONArray();
		    jArray.put(obj);
		    jsonObjHits.put(locale,jArray);
		}
		parser.close();
		System.out.println(jsonObjHits.get(this.locale));
	}

}
