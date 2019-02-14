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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import utility.Configuration;

/**
 * Driver class for UV-SCAN
 * 
 * @author aaggarwa
 *
 */
public class UvScanInit {

	private static final Logger log = Logger.getLogger(UvScanInit.class);
	final private static String apiName = "UV-SCAN()";
	final private static String ConfigurationFilePath = "UvScanConfiguration.json";

	private static int TimeoutForUrlPingInMs;

	private String SessionID;
	private String AlfProductName;
	private String AlfCodeName;
	private List<String> Locales;

	public static String Results_file;
	private Map<String, RGMApiResultBean> UvScanResultBeanMap;

	private String uvScanResultFilePath;

	/**
	 * Default controller
	 */
	public UvScanInit() {
		UvScanResultBeanMap = new HashMap<String, RGMApiResultBean>();

		new Configuration(ConfigurationFilePath);

		TimeoutForUrlPingInMs = Integer.parseInt(Configuration.getConstant("TimeoutForUrlPingInMs"));
		uvScanResultFilePath = "";

	}

	/**
	 * Sets the ALF project details and locales list for UV-SCAN
	 * 
	 * @param sessionID
	 * @param alfProductName
	 * @param alfCodeName
	 * @param locales
	 */
	public UvScanInit(String alfProductName, String alfCodeName, List<String> locales, String results_file) {

		this.AlfProductName = alfProductName;
		this.AlfCodeName = alfCodeName;
		UvScanInit.Results_file = results_file;
		locales.add(0, "en_US");
		this.Locales = new ArrayList<String>(locales);

		UvScanResultBeanMap = new HashMap<String, RGMApiResultBean>(locales.size());

		new Configuration(ConfigurationFilePath);

		TimeoutForUrlPingInMs = Integer.parseInt(Configuration.getConstant("TimeoutForUrlPingInMs"));
		uvScanResultFilePath = "";

		log.info("RecievedParams for the AlfProductName:" + alfProductName + " , AlfCodeName:" + alfCodeName
				+ " , locale:" + this.Locales);
	}

	public String getUvScanResultFilePath() {
		return uvScanResultFilePath;
	}

	public void setUvScanResultFilePath(String uvScanResultFilePath) {
		this.uvScanResultFilePath = uvScanResultFilePath;
	}

	/**
	 * Returns the RGMApiResultBean map for all locales for which UV-SCAN is
	 * initiated
	 * 
	 * @return Map of RGMApiResultBean with locale as key
	 */
	public void runUvScan(String ldap, String csvpath) {

		try {

			long CallStartTime = System.currentTimeMillis();
			String userLdap = ldap;

			Object[] localesToBeProceessed = this.Locales.toArray();
			log.info("No of locales to be proccessed " + localesToBeProceessed.length);

			log.info("Instantiating general controller");
			GeneralController generalController = new GeneralController(this.AlfProductName, this.AlfCodeName);

			log.info("Setting general configurations and settings(localeIdentifiers, ExceptionUrlsList, etc. )");
			generalController.SetUvScanConfiguration(this.Locales);

			LocaleController en_US_LocaleController = new LocaleController(this.AlfProductName, this.AlfCodeName,
					"en_US", generalController);

			for (int i = 1; i < localesToBeProceessed.length; i++) {

				long startTime = System.currentTimeMillis();
				log.info("Inside Server " + apiName + " for locale " + localesToBeProceessed[i] + " at " + startTime
						+ " miliseconds");

				log.info("Intstantiating locale controller for " + localesToBeProceessed[i]);
				LocaleController localeController = new LocaleController(this.AlfProductName, this.AlfCodeName,
						localesToBeProceessed[i].toString(), generalController);

				if (i == 1) {

					log.info("extracting strings for en_US and 1st locale " + localesToBeProceessed[i]);

					localeController.getJSON(csvpath);

					HashMap<String, UvScanUrl> ResourceFileUrlsMap = localeController
							.getUrlsFromLuceneSearchString(true);

					log.info("Urls extracted for en_US and 1st locale " + localesToBeProceessed[i] + " :"
							+ ResourceFileUrlsMap.size());

					HashMap<String, UvScanUrl> EngUrlMapFromProductResources = generalController.EngUrlMapFromProductResources;
					if (Configuration.CheckUrlRedirection) {
						log.info("redirecting en_US  and 1st locale " + localesToBeProceessed[i]
								+ " Urls and storing response");
						ResourceFileUrlsMap = localeController.getUrlResponse(TimeoutForUrlPingInMs);

						EngUrlMapFromProductResources = localeController.getUrlResponseforEng(TimeoutForUrlPingInMs);
					}
					en_US_LocaleController.setAllUrlsInProjectMap(EngUrlMapFromProductResources);
				} else {
					log.info("extracting Urls for " + localesToBeProceessed[i]);

					HashMap<String, UvScanUrl> ResourceFileUrlsMap = localeController
							.getUrlsFromLuceneSearchString(false);

					log.info("Urls extracted for locale " + localesToBeProceessed[i] + " :"
							+ ResourceFileUrlsMap.size());

					if (Configuration.CheckUrlRedirection) {
						log.info("redirecting " + localesToBeProceessed[i] + " Urls and storing response");
						localeController.getUrlResponse(TimeoutForUrlPingInMs);
					}
				}

				log.info("performing pattern matching for " + localesToBeProceessed[i]);

				localeController.performPatternMatching(TimeoutForUrlPingInMs);

				log.info("generating report for " + localesToBeProceessed[i]);

				localeController.generateReport();

				log.info("getting result Bean");
				UvScanResultBean uvScanbean = localeController.getUvScanResultBean();

				////////////// UV-Scan analytics/////////////////////////////
				String logForAnalytics = "LDAP:" + userLdap + ";SessionID:" + this.SessionID + ";AlfProductName:"
						+ this.AlfProductName + ";AlfCodeName:" + this.AlfCodeName;
				log.info("UV-Scan run by user - Final;" + (logForAnalytics + uvScanbean.toString()));

				long timeTaken = System.currentTimeMillis() - startTime;
				log.info("Total time by Server for " + apiName + " for locale " + localesToBeProceessed[i] + " is "
						+ timeTaken + " miliseconds");

				RGMApiResultBean result = getRGMApiResultBeanForUvscan(uvScanbean);
				result.setInitiated(startTime);
				result.setApiTime(timeTaken);
				result.setReportUrl(Configuration.getConstant("UvScanServiceReportUrl"));

				UvScanResultBeanMap.put(localesToBeProceessed[i].toString(), result);

			}

			long startTime = System.currentTimeMillis();
			log.info("performing pattern matching for en_US");

			en_US_LocaleController.performPatternMatching(TimeoutForUrlPingInMs);

			log.info("generating report for en_US");

			en_US_LocaleController.generateReport();

			log.info("getting result Bean en_US");
			UvScanResultBean uvScanbean = en_US_LocaleController.getUvScanResultBean();

			////////////// UV-Scan analytics/////////////////////////////
			String logForAnalytics = "LDAP:" + userLdap + ";SessionID:" + this.SessionID + ";AlfProductName:"
					+ this.AlfProductName + ";AlfCodeName:" + this.AlfCodeName;
			log.info("UV-Scan run by user - Final;" + (logForAnalytics + uvScanbean.toString()));

			log.info("chk" + uvScanbean.toString());
			long timeTaken = System.currentTimeMillis() - startTime;
			log.info("Total time by Server for " + apiName + " for locale en_US is " + timeTaken + " miliseconds");

			RGMApiResultBean result = getRGMApiResultBeanForUvscan(uvScanbean);
			result.setInitiated(startTime);
			result.setApiTime(timeTaken);
			result.setReportUrl(Configuration.getConstant("UvScanServiceReportUrl"));

			UvScanResultBeanMap.put("en_US", result);

			log.info("Updating Patterns file");
			generalController.writeUrlPatternsFile();

			log.info("Generating final report");
			generalController.generateFinalReportByUrl();

			Map<String, UvScanResultBean> sortedMap = new TreeMap<String, UvScanResultBean>(
					generalController.LocaleMapBean);
			for (Map.Entry<String, UvScanResultBean> val : sortedMap.entrySet()) {
				UvScanResultBean uvb = val.getValue();
				logForAnalytics = "LDAP:" + userLdap + ";SessionID:" + this.SessionID + ";AlfProductName:"
						+ this.AlfProductName + ";AlfCodeName:" + this.AlfCodeName;
				log.info("UV-Scan run by user - Master;" + (logForAnalytics + uvb.toString()));
			}
			this.setUvScanResultFilePath(
					UvScanInit.Results_file + "\\" + Configuration.getConstant("UvScanResultsDirName") + "/"
							+ Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileName") + "."
							+ Configuration.getConstant("UvScan_LocaleVsUrl_Result_File_FileExt"));

			long CallEndTime = System.currentTimeMillis();
			log.info("Total time taken by  " + apiName + "  for all locale  is " + (CallEndTime - CallStartTime)
					+ " miliseconds");
			// return UvScanResultBeanMap;
		} catch (Exception e) {
			log.error("General exception occurred", e);
			// return this.getDefaultRGMApiResultBeanForUvscan();
		}
	}

	/**
	 * sets value of RGMApiResultBean based on UvScanResultBean attribute values
	 * 
	 * @param uvBean
	 * @return RGMApiResultBean
	 */
	public static RGMApiResultBean getRGMApiResultBeanForUvscan(UvScanResultBean uvBean) {

		RGMApiResultBean result = new RGMApiResultBean();

		try {

			if (uvBean == null) {
				throw new Exception("Unknown UvScan result encountered.");
			}

			if (uvBean.getFailedCount() == 0) {
				// result.setHttpStatus(HttpURLConnection.HTTP_OK);
				result.setErrorTitle(uvBean.getResultMessage());
			} else {
				// result.setHttpStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
				result.setErrorTitle(uvBean.getResultMessage());
			}

		} catch (Exception ex) {
			// result.setHttpStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
			result.setErrorTitle(
					Configuration.getConstant("UvScanResultMessageForExceptionScenario") + ex.getMessage());
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
		}
		return result;
	}

	/**
	 * sets value of RGMApiResultBean when UvScanResultBean is null or exception
	 * occurs
	 * 
	 * @return Map of RGMApiResultBean with locale as key
	 */
	public Map<String, RGMApiResultBean> getDefaultRGMApiResultBeanForUvscan() {
		if (UvScanResultBeanMap == null)
			UvScanResultBeanMap = new HashMap<String, RGMApiResultBean>();

		for (String locale : this.Locales) {
			if (!UvScanResultBeanMap.containsKey(locale)) {
				RGMApiResultBean result = getRGMApiResultBeanForUvscan(null);
				result.setInitiated(new Long(0));
				result.setApiTime(new Long(0));
				result.setReportUrl(Configuration.getConstant("UvScanServiceReportUrl"));
				UvScanResultBeanMap.put(locale, result);
			} else
				continue;
		}

		return UvScanResultBeanMap;
	}

}
