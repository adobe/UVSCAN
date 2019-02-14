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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class to handle configurations for UV-SCAN. Handles values of all constants
 * used in the application
 * 
 * @author aaggarwa
 *
 */
public class Configuration {

	private static final Logger log = Logger.getLogger(Configuration.class);

	static Map Constants = new HashMap();
	static String KeyNotFound = "Key_Not_Found";
	static String Nullkey = "Null_Found";

	final public static String[] AdobeErrorPageIdentifiers = { "Error returned: 404",
			"<title>Adobe - Error page</title>", "Sorry, this page is unavailable" };

	public static String FPS = File.separator;
	public static String countryPatternWord = "<country>";
	public static String languagePatternWord = "<language>";
	public static String localePatternWord = "<locale>";
	public static String localeHyphenPatternWord = "<localeHypen>";
	public static String Regex_For_Url_Parser = "\\(?\\b(http://|https://|www[.])[-A-Za-z0-9+&amp\\^\\$\\{\\};<>@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp\\^\\$\\{\\}<>;@#/%=~_()]";

	public static Map<String, JSONObject> localeCodes;

	public static String Url_Patterns = "(loc=<localeIdentifier>)$ , (loc=<localeIdentifier>&) , (locale=<localeIdentifier>)$ , (locale=<localeIdentifier>&) , (lang=<localeIdentifier>)$ , (lang=<localeIdentifier>&) , (/<localeIdentifier>/) , (/<localeIdentifier>)$ , (_<localeIdentifier>)$ , (.<localeIdentifier>)$ , (.<localeIdentifier>/)";

	public static boolean CheckUrlPattern = true;
	public static boolean CheckUrlRedirection = true;

	public static String locModificationStartDate = null;
	public static String locModificationEndDate = null;

	public static String locModificationStartDateValue = null;
	public static String locModificationEndDateValue = null;

	public static Map<String, Color> colorCodeForExceptions = new HashMap<String, Color>() {
		{
			put("UnlocalizedBaseUrl", new XSSFColor(java.awt.Color.orange));
			put("UnlocalizedRedirectedUrl", new XSSFColor(java.awt.Color.cyan));
			put("IgnorePatternMatching", new XSSFColor(java.awt.Color.yellow));
			put("Do not test", new XSSFColor(java.awt.Color.red));
		}
	};

	/*
	 * Default controller
	 */
	public Configuration() {

	}

	/**
	 * Add to the map values of all the constants in the configuration file. ALso
	 * sets values of certain static variables that are used too often
	 * 
	 * @param ConfigFile
	 *            - File containing json mapping for all constant used in the
	 *            application
	 */
	public Configuration(String configFile) {
		setConstants(configFile);
		countryPatternWord = getConstant("countryPatternWord");
		languagePatternWord = getConstant("languagePatternWord");
		localePatternWord = getConstant("localePatternWord");
		localeHyphenPatternWord = getConstant("localeHyphenPatternWord");

		locModificationStartDate = "01-01-1970";
		locModificationEndDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

		locModificationStartDateValue = "01-01-1970";
		locModificationEndDateValue = "Today";

		try {

			localeCodes = new HashMap<String, JSONObject>() {
				{
					put("es_ES", new JSONObject(
							"{curLocale:es_ES , curCountry:es , curAlternateCountry:es , curLanguage:es , curBaseLocale:es_ES , curBaseCountry:es , curBaseLanguage:es , curLocaleHyphen:es-ES , curBaseLocaleHyphen:es-ES}"));
					put("fr_FR", new JSONObject(
							"{curLocale:fr_FR , curCountry:fr , curAlternateCountry:fr , curLanguage:fr , curBaseLocale:fr_FR , curBaseCountry:fr , curBaseLanguage:fr , curLocaleHyphen:fr-FR , curBaseLocaleHyphen:fr-FR}"));
					put("it_IT", new JSONObject(
							"{curLocale:it_IT , curCountry:it , curAlternateCountry:it , curLanguage:it , curBaseLocale:it_IT , curBaseCountry:it , curBaseLanguage:it , curLocaleHyphen:it-IT , curBaseLocaleHyphen:it-IT}"));
					put("de_DE", new JSONObject(
							"{curLocale:de_DE , curCountry:de , curAlternateCountry:de , curLanguage:de , curBaseLocale:de_DE , curBaseCountry:de , curBaseLanguage:de , curLocaleHyphen:de-DE , curBaseLocaleHyphen:de-DE}"));
					put("hu_HU", new JSONObject(
							"{curLocale:hu_HU , curCountry:hu , curAlternateCountry:hu , curLanguage:hu , curBaseLocale:hu_HU , curBaseCountry:hu , curBaseLanguage:hu , curLocaleHyphen:hu-HU , curBaseLocaleHyphen:hu-HU}"));
					put("pl_PL", new JSONObject(
							"{curLocale:pl_PL , curCountry:pl , curAlternateCountry:pl , curLanguage:pl , curBaseLocale:pl_PL , curBaseCountry:pl , curBaseLanguage:pl , curLocaleHyphen:pl-PL , curBaseLocaleHyphen:pl-PL}"));
					put("ru_RU", new JSONObject(
							"{curLocale:ru_RU , curCountry:ru , curAlternateCountry:ru , curLanguage:ru , curBaseLocale:ru_RU , curBaseCountry:ru , curBaseLanguage:ru , curLocaleHyphen:ru-RU , curBaseLocaleHyphen:ru-RU}"));
					put("nl_NL", new JSONObject(
							"{curLocale:nl_NL , curCountry:nl , curAlternateCountry:nl , curLanguage:nl , curBaseLocale:nl_NL , curBaseCountry:nl , curBaseLanguage:nl , curLocaleHyphen:nl-NL , curBaseLocaleHyphen:nl-NL}"));
					put("fi_FI", new JSONObject(
							"{curLocale:fi_FI , curCountry:fi , curAlternateCountry:fi , curLanguage:fi , curBaseLocale:fi_FI , curBaseCountry:fi , curBaseLanguage:fi , curLocaleHyphen:fi-FI , curBaseLocaleHyphen:fi-FI}"));
					put("tr_TR", new JSONObject(
							"{curLocale:tr_TR , curCountry:tr , curAlternateCountry:tr , curLanguage:tr , curBaseLocale:tr_TR , curBaseCountry:tr , curBaseLanguage:tr , curLocaleHyphen:tr-TR , curBaseLocaleHyphen:tr-TR}"));
					put("da_DK", new JSONObject(
							"{curLocale:da_DK , curCountry:dk , curAlternateCountry:dk , curLanguage:da , curBaseLocale:da_DK , curBaseCountry:dk , curBaseLanguage:da , curLocaleHyphen:da-DK , curBaseLocaleHyphen:da-DK}"));
					put("ja_JP", new JSONObject(
							"{curLocale:ja_JP , curCountry:jp , curAlternateCountry:jp , curLanguage:ja , curBaseLocale:ja_JP , curBaseCountry:jp , curBaseLanguage:ja , curLocaleHyphen:ja-JP , curBaseLocaleHyphen:ja-JP}"));
					put("ko_KR", new JSONObject(
							"{curLocale:ko_KR , curCountry:kr , curAlternateCountry:kr , curLanguage:ko , curBaseLocale:ko_KR , curBaseCountry:kr , curBaseLanguage:ko , curLocaleHyphen:ko-KR , curBaseLocaleHyphen:ko-KR}"));
					put("pt_BR", new JSONObject(
							"{curLocale:pt_BR , curCountry:br , curAlternateCountry:br , curLanguage:pt , curBaseLocale:pt_BR , curBaseCountry:br , curBaseLanguage:pt , curLocaleHyphen:pt-BR , curBaseLocaleHyphen:pt-BR}"));
					put("sv_SE", new JSONObject(
							"{curLocale:sv_SE , curCountry:se , curAlternateCountry:se , curLanguage:sv , curBaseLocale:sv_SE , curBaseCountry:se , curBaseLanguage:sv , curLocaleHyphen:sv-SE , curBaseLocaleHyphen:sv-SE}"));
					put("nb_NO", new JSONObject(
							"{curLocale:nb_NO , curCountry:no , curAlternateCountry:no , curLanguage:nb , curBaseLocale:nb_NO , curBaseCountry:no , curBaseLanguage:nb , curLocaleHyphen:nb-NO , curBaseLocaleHyphen:nb-NO}"));
					put("uk_UA", new JSONObject(
							"{curLocale:uk_UA , curCountry:ua , curAlternateCountry:ua , curLanguage:uk , curBaseLocale:uk_UA , curBaseCountry:ua , curBaseLanguage:uk , curLocaleHyphen:uk-UA , curBaseLocaleHyphen:uk-UA}"));
					put("cs_CZ", new JSONObject(
							"{curLocale:cs_CZ , curCountry:cz , curAlternateCountry:cz , curLanguage:cs , curBaseLocale:cs_CZ , curBaseCountry:cz , curBaseLanguage:cs , curLocaleHyphen:cs-CZ , curBaseLocaleHyphen:cs-CZ}"));
					put("zh_TW", new JSONObject(
							"{curLocale:zh_TW , curCountry:tw , curAlternateCountry:zh , curLanguage:zh , curBaseLocale:zh_TW , curBaseCountry:zh , curBaseLanguage:zh , curLocaleHyphen:zh-TW , curBaseLocaleHyphen:zh-TW}"));
					put("zh_CN", new JSONObject(
							"{curLocale:zh_CN , curCountry:cn , curAlternateCountry:zh , curLanguage:zh , curBaseLocale:zh_CN , curBaseCountry:zh , curBaseLanguage:zh , curLocaleHyphen:zh-CN , curBaseLocaleHyphen:zh-CN}"));
					put("fr_CA", new JSONObject(
							"{curLocale:fr_CA , curCountry:ca , curAlternateCountry:ca_fr , curLanguage:fr , curBaseLocale:fr_FR , curBaseCountry:fr , curBaseLanguage:fr , curLocaleHyphen:fr-CA , curBaseLocaleHyphen:fr-FR}"));
					put("fr_MA", new JSONObject(
							"{curLocale:fr_MA , curCountry:mena_fr , curAlternateCountry:mena_fr , curLanguage:fr , curBaseLocale:fr_FR , curBaseCountry:fr , curBaseLanguage:fr , curLocaleHyphen:fr-MA , curBaseLocaleHyphen:fr-FR}"));
					put("es_MX", new JSONObject(
							"{curLocale:es_MX , curCountry:mx , curAlternateCountry:es_MX , curLanguage:es , curBaseLocale:es_ES , curBaseCountry:es , curBaseLanguage:es , curLocaleHyphen:es-MX , curBaseLocaleHyphen:es-ES}"));
					put("en_GB", new JSONObject(
							"{curLocale:en_GB , curCountry:uk , curAlternateCountry:en_GB , curLanguage:en , curBaseLocale:en_GB , curBaseCountry:en , curBaseLanguage:en , curLocaleHyphen:en-GB , curBaseLocaleHyphen:en-GB}"));
					put("en_IL", new JSONObject(
							"{curLocale:en_IL , curCountry:mena_en , curAlternateCountry:il_he , curLanguage:en , curBaseLocale:en_IL , curBaseCountry:en , curBaseLanguage:en , curLocaleHyphen:en-IL , curBaseLocaleHyphen:en-IL}"));
					put("en_AE", new JSONObject(
							"{curLocale:en_AE , curCountry:mena_en , curAlternateCountry:mena_ar , curLanguage:en , curBaseLocale:en_AE , curBaseCountry:en , curBaseLanguage:en , curLocaleHyphen:en-AE , curBaseLocaleHyphen:en-AE}"));
					put("en_US", new JSONObject(
							"{curLocale:en_US , curCountry:en , curAlternateCountry:en , curLanguage:en , curBaseLocale:en_US , curBaseCountry:en , curBaseLanguage:en , curLocaleHyphen:en-US , curBaseLocaleHyphen:en-US}"));
				}
			};
		} catch (JSONException e) {
			log.error("Could not initialize localeCodes map", e);
		}

	}

	/**
	 * Function to set the map from configuration file
	 * 
	 * @param ConfigFile
	 *            - File containing json mapping for all constant used in the
	 *            application
	 */
	private void setConstants(String configFile) {

		BufferedReader br = null;

		try {
			String sCurrentLine = "";
			String AllLines = "";

			log.info("Reading Configuration file :" + configFile);

			InputStream i = new FileInputStream(
					new File(getClass().getClassLoader().getResource(configFile).getFile()));
			br = new BufferedReader(new InputStreamReader(i));

			br = new BufferedReader(new InputStreamReader(i));

			while ((sCurrentLine = br.readLine()) != null) {
				AllLines += sCurrentLine;
			}

			JSONObject jsonObj = new JSONObject(AllLines);

			Iterator itr = jsonObj.keys();
			log.info("No of constants read from Configuration file :" + jsonObj.length());

			while (itr.hasNext()) {
				String key = itr.next().toString();
				String value = (String) jsonObj.get(key);
				Constants.put(key, value);
			}

			log.info("Configuration Map  created...");

		} catch (IOException e) {
			log.error("IOException in setConstants...", e);
		} catch (Exception e2) {
			log.error("Exception in setConstants...", e2);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				log.error("IOException in setConstants while flushing buffers...", ex);
			}
		}
	}

	/**
	 * Function to retrieve value from the map
	 * 
	 * @param ID
	 * @return String - value of constant
	 */
	public static String getConstant(String ID) {
		if (Constants.containsKey(ID)) {
			if (Constants.get(ID) != null) {
				return Constants.get(ID).toString();
			} else {
				log.error("Key present in Map Constants with null value assigned, KEY :" + ID);
				return Nullkey;
			}
		} else {
			log.error("Key not present in Map Constants , KEY :" + ID);
			return KeyNotFound;
		}
	}

}
