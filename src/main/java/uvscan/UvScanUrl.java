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

import utility.Configuration;

/**
 * Model class to store all attributes related to url
 * 
 * @author aaggarwa
 *
 */
public class UvScanUrl {

	public enum UrlPassLevel {
		Passed, PassedWithException, Failed
	}

	public enum UrlExceptionLevel {
		None, UnlocalizedBaseUrl, UnlocalizedRedirectedUrl, IgnorePatternMatching, Delete
	}

	public enum UrlPatternMatchLevel {
		IceMatch, AlternateParam_Match, FallBack_Match, Random_Match, Pattern_MisMatch, None
	}

	private String stringID;

	private String baseUrl;
	private String redirectedUrl;

	private String configUrlPattern;
	private String configRedirectedUrlPattern;
	private String configEngUrl;
	private String configEngRedirectedUrl;

	private String baseUrlPatternMatchResult;
	private String redirectedUrlPatternMatchResult;

	private String patternRemarks;

	private String userRemarks;

	private Boolean followsConfigUrlPattern;
	private Boolean followsConfigRedirectedUrlPattern;

	private Boolean followsExactConfigUrlPattern;
	private Boolean followsExactConfigRedirectedUrlPattern;

	private Boolean isNew;
	private Boolean isDeleted;
	private Boolean isErrorPage;

	private UrlExceptionLevel exceptionType;

	private Boolean isBaseUrlLocalized;
	private Boolean isRedirectedUrlLocalized;

	private int responseCode;

	private String exceptionOccurred;

	/**
	 * Constructor used for instantiation for urls found in strings retrived form
	 * ALF
	 * 
	 * @param stringID
	 * @param baseUrl
	 */
	public UvScanUrl(String stringID, String baseUrl) {
		this.stringID = stringID;
		this.baseUrl = baseUrl;

		followsConfigUrlPattern = false;
		followsConfigRedirectedUrlPattern = false;

		followsExactConfigUrlPattern = false;
		followsExactConfigRedirectedUrlPattern = false;

		isBaseUrlLocalized = false;
		isRedirectedUrlLocalized = false;

		isDeleted = false;
		isErrorPage = true;

		exceptionType = UrlExceptionLevel.None;

		baseUrlPatternMatchResult = "";
		redirectedUrlPatternMatchResult = "";

		exceptionOccurred = "";
	}

	/**
	 * Constructor used for instantiation for urls found in patterns file
	 * 
	 * @param stringID
	 * @param configUrlPattern
	 * @param configRedirectedUrlPattern
	 * @param configEngUrl
	 * @param configEngRedirectedUrl
	 */
	public UvScanUrl(String stringID, String configUrlPattern, String configRedirectedUrlPattern, String configEngUrl,
			String configEngRedirectedUrl, String patternRemarks, String userRemarks) {
		this.stringID = stringID;
		this.baseUrl = configUrlPattern;
		this.redirectedUrl = configRedirectedUrlPattern;
		this.configUrlPattern = configUrlPattern;
		this.configRedirectedUrlPattern = configRedirectedUrlPattern;
		this.configEngUrl = configEngUrl;
		this.configEngRedirectedUrl = configEngRedirectedUrl;

		this.patternRemarks = patternRemarks;
		this.userRemarks = userRemarks;

		followsConfigUrlPattern = false;
		followsConfigRedirectedUrlPattern = false;

		followsExactConfigUrlPattern = false;
		followsExactConfigRedirectedUrlPattern = false;

		isBaseUrlLocalized = false;
		isRedirectedUrlLocalized = false;

		// isNew=false;
		isDeleted = false;
		isErrorPage = true;

		exceptionType = UrlExceptionLevel.None;

		baseUrlPatternMatchResult = "";
		redirectedUrlPatternMatchResult = "";

		exceptionOccurred = "";
	}

	/**
	 * function to write certain attribute values
	 */
	public String toString() {
		String UrlValues = "" + this.stringID + ";" + this.baseUrl + ";" + this.redirectedUrl + ";" + this.responseCode
				+ ";" + this.exceptionOccurred + ";" + this.configUrlPattern + ";" + this.configRedirectedUrlPattern
				+ ";" + this.configEngUrl + ";" + this.configEngRedirectedUrl + ";" + this.isErrorPage + ";"
				+ this.isDeleted + ";" + this.baseUrlPatternMatchResult + ";" + this.redirectedUrlPatternMatchResult
				+ ";" + this.isBaseUrlLocalized + ";" + this.isRedirectedUrlLocalized + ";"
				+ this.followsConfigUrlPattern + ";" + this.followsConfigRedirectedUrlPattern + ";";
		return UrlValues;
	}

	public String getStringID() {
		return this.stringID;
	}

	public void setStringID(String stringID) {
		this.stringID = stringID;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getRedirectedUrl() {
		return redirectedUrl;
	}

	public void setRedirectedUrl(String redirectedUrl) {
		this.redirectedUrl = redirectedUrl;
	}

	public String getConfigUrlPattern() {
		return configUrlPattern;
	}

	public void setConfigUrlPattern(String configUrlPattern) {
		this.configUrlPattern = configUrlPattern;
	}

	public String getConfigRedirectedUrlPattern() {
		return configRedirectedUrlPattern;
	}

	public void setConfigRedirectedUrlPattern(String configRedirectedUrlPattern) {
		this.configRedirectedUrlPattern = configRedirectedUrlPattern;
	}

	public String getConfigEngUrl() {
		return configEngUrl;
	}

	public void setConfigEngUrl(String configEngUrl) {
		this.configEngUrl = configEngUrl;
	}

	public String getConfigEngRedirectedUrl() {
		return configEngRedirectedUrl;
	}

	public void setConfigEngRedirectedUrl(String configEngRedirectedUrl) {
		this.configEngRedirectedUrl = configEngRedirectedUrl;
	}

	public String getBaseUrlPatternMatchResult() {
		return baseUrlPatternMatchResult;
	}

	public void setBaseUrlPatternMatchResult(String baseUrlPatternMatchResult) {
		this.baseUrlPatternMatchResult = baseUrlPatternMatchResult;
	}

	public String getRedirectedUrlPatternMatchResult() {
		return redirectedUrlPatternMatchResult;
	}

	public void setRedirectedUrlPatternMatchResult(String redirectedUrlPatternMatchResult) {
		this.redirectedUrlPatternMatchResult = redirectedUrlPatternMatchResult;
	}

	public String getPatternRemarks() {
		return patternRemarks;
	}

	public void setPatternRemarks(String patternRemarks) {
		this.patternRemarks = patternRemarks;
	}

	public String getUserRemarks() {
		return userRemarks;
	}

	public void setUserRemarks(String userRemarks) {
		this.userRemarks = userRemarks;
	}

	public Boolean getFollowsConfigUrlPattern() {
		return followsConfigUrlPattern;
	}

	public void setFollowsConfigUrlPattern(Boolean followsConfigUrlPattern) {
		this.followsConfigUrlPattern = followsConfigUrlPattern;
	}

	public Boolean getFollowsConfigRedirectedUrlPattern() {
		return followsConfigRedirectedUrlPattern;
	}

	public void setFollowsConfigRedirectedUrlPattern(Boolean followsConfigRedirectedUrlPattern) {
		this.followsConfigRedirectedUrlPattern = followsConfigRedirectedUrlPattern;
	}

	public Boolean getFollowsExactConfigUrlPattern() {
		return followsExactConfigUrlPattern;
	}

	public void setFollowsExactConfigUrlPattern(Boolean followsExactConfigUrlPattern) {
		this.followsExactConfigUrlPattern = followsExactConfigUrlPattern;
	}

	public Boolean getFollowsExactConfigRedirectedUrlPattern() {
		return followsExactConfigRedirectedUrlPattern;
	}

	public void setFollowsExactConfigRedirectedUrlPattern(Boolean followsExactConfigRedirectedUrlPattern) {
		this.followsExactConfigRedirectedUrlPattern = followsExactConfigRedirectedUrlPattern;
	}

	public Boolean getIsNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Boolean getIsErrorPage() {
		return isErrorPage;
	}

	public void setIsErrorPage(Boolean isErrorPage) {
		this.isErrorPage = isErrorPage;
	}

	public UrlExceptionLevel getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(UrlExceptionLevel exceptionType) {
		this.exceptionType = exceptionType;
	}

	public Boolean getIsBaseUrlLocalized() {
		return isBaseUrlLocalized;
	}

	public void setIsBaseUrlLocalized(Boolean isBaseUrlLocalized) {
		this.isBaseUrlLocalized = isBaseUrlLocalized;
	}

	public Boolean getIsRedirectedUrlLocalized() {
		return isRedirectedUrlLocalized;
	}

	public void setIsRedirectedUrlLocalized(Boolean isRedirectedUrlLocalized) {
		this.isRedirectedUrlLocalized = isRedirectedUrlLocalized;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getExceptionOccurred() {
		return exceptionOccurred;
	}

	public void setExceptionOccurred(String exceptionOccurred) {
		this.exceptionOccurred = exceptionOccurred;
	}

	/**
	 * 
	 * @return String to be displayed for url redirecting to 404
	 */
	public String isErrorPage() {

		return (this.isErrorPage ? Configuration.getConstant("Display_Message_For_ErrorPage")
				: Configuration.getConstant("Display_Message_For_Not_ErrorPage"));
	}

	/**
	 * 
	 * @return String to be displayed for according to url
	 *         status(New/Existing/Deleted)
	 */
	public String isNewUrl() {
		if (this.isNew)
			return Configuration.getConstant("Display_Message_For_NewURl");
		else if (this.isDeleted)
			return Configuration.getConstant("Display_Message_For_DeletedUrl");
		else
			return Configuration.getConstant("Display_Message_For_ExistingUrl");
	}

	/**
	 * Function evalutes various attributes for the url and decides its UrlPassLevel
	 * 
	 * @return String to be displayed for according to url UrlPassLevel
	 */
	public String getUrlPassStatus() {

		UrlPassLevel FinalResultForBaseUrl;
		UrlPassLevel FinalResultForRedirectionUrl;
		UrlPassLevel FinalResultForUrl;

		if (Configuration.CheckUrlRedirection && this.isErrorPage)
			return Configuration.getConstant("Display_Message_For_Failed_Url");

		if (!Configuration.CheckUrlPattern)
			return Configuration.getConstant("Display_Message_For_Passed_Url");

		if (Configuration.CheckUrlRedirection && this.exceptionType.equals(UrlExceptionLevel.IgnorePatternMatching)) {
			if (this.isErrorPage)
				return Configuration.getConstant("Display_Message_For_Failed_Url");
			else
				return Configuration.getConstant("Display_Message_For_Passed_Url");
		}

		if (this.isBaseUrlLocalized) {
			if (this.followsExactConfigUrlPattern)
				FinalResultForBaseUrl = UrlPassLevel.Passed;
			else if (this.followsConfigUrlPattern)
				FinalResultForBaseUrl = UrlPassLevel.PassedWithException;
			else
				FinalResultForBaseUrl = UrlPassLevel.Failed;

		} else {
			if (this.getExceptionType().equals(UrlExceptionLevel.UnlocalizedBaseUrl))
				FinalResultForBaseUrl = UrlPassLevel.Passed;
			else if (this.configUrlPattern.equals(this.configEngUrl))
				FinalResultForBaseUrl = UrlPassLevel.PassedWithException;
			else
				FinalResultForBaseUrl = UrlPassLevel.Failed;
		}

		if (Configuration.CheckUrlRedirection) {
			if (this.isRedirectedUrlLocalized) {
				if (this.followsExactConfigRedirectedUrlPattern)
					FinalResultForRedirectionUrl = UrlPassLevel.Passed;
				else if (this.followsConfigRedirectedUrlPattern)
					FinalResultForRedirectionUrl = UrlPassLevel.PassedWithException;
				else
					FinalResultForRedirectionUrl = UrlPassLevel.Failed;

			} else {
				if ((this.getExceptionType().equals(UrlExceptionLevel.UnlocalizedBaseUrl))
						|| this.getExceptionType().equals(UrlExceptionLevel.UnlocalizedRedirectedUrl))
					FinalResultForRedirectionUrl = UrlPassLevel.Passed;
				else if (this.configRedirectedUrlPattern.equals(this.configEngRedirectedUrl))
					FinalResultForRedirectionUrl = UrlPassLevel.PassedWithException;
				else
					FinalResultForRedirectionUrl = UrlPassLevel.Failed;
			}
		} else
			FinalResultForRedirectionUrl = UrlPassLevel.Passed;

		if (FinalResultForRedirectionUrl.equals(FinalResultForBaseUrl))
			FinalResultForUrl = FinalResultForBaseUrl;

		else if (FinalResultForBaseUrl.equals(UrlPassLevel.Failed)
				|| FinalResultForRedirectionUrl.equals(UrlPassLevel.Failed))
			FinalResultForUrl = UrlPassLevel.Failed;

		else if (FinalResultForBaseUrl.equals(UrlPassLevel.PassedWithException)
				|| FinalResultForRedirectionUrl.equals(UrlPassLevel.PassedWithException))
			FinalResultForUrl = UrlPassLevel.PassedWithException;

		else
			FinalResultForUrl = UrlPassLevel.Passed;

		if (FinalResultForUrl.equals(UrlPassLevel.Passed))
			return Configuration.getConstant("Display_Message_For_Passed_Url");
		else if (FinalResultForUrl.equals(UrlPassLevel.PassedWithException))
			return Configuration.getConstant("Display_Message_For_PassedWithException_Url");
		else
			return Configuration.getConstant("Display_Message_For_Failed_Url");
	}

}
