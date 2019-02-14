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

/**
 * Result bean for UV-SCAN Stores the project details count of urls in different
 * UrlPassLevel List of urls in different UrlPassLevel result of UV-SCAN for a
 * specific locale
 * 
 * @author aaggarwa
 *
 */
public class UvScanResultBean {

	private String AlfProductName;
	private String AlfCodeName;
	private String locale;

	private int totalCount = 0, passedCount = 0, passedWithExceptionCount = 0, failedCount = 0, newUrlCount = 0,
			deletedUrlCount = 0;

	private String passedWithExceptionUrls = "", failedUrls = "", newUrls = "", deletedUrls = "";

	private String resultMessage;
	private String lastUpdated;

	public UvScanResultBean() {

	}

	public String getAlfProductName() {
		return AlfProductName;
	}

	public void setAlfProductName(String alfProductName) {
		AlfProductName = alfProductName;
	}

	public String getAlfCodeName() {
		return AlfCodeName;
	}

	public void setAlfCodeName(String alfCodeName) {
		AlfCodeName = alfCodeName;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getPassedCount() {
		return passedCount;
	}

	public void setPassedCount(int passedCount) {
		this.passedCount = passedCount;
	}

	public int getPassedWithExceptionCount() {
		return passedWithExceptionCount;
	}

	public void setPassedWithExceptionCount(int passedWithExceptionCount) {
		this.passedWithExceptionCount = passedWithExceptionCount;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}

	public int getNewUrlCount() {
		return newUrlCount;
	}

	public void setNewUrlCount(int newUrlCount) {
		this.newUrlCount = newUrlCount;
	}

	public int getDeletedUrlCount() {
		return deletedUrlCount;
	}

	public void setDeletedUrlCount(int deletedUrlCount) {
		this.deletedUrlCount = deletedUrlCount;
	}

	public String getPassedWithExceptionUrls() {
		return passedWithExceptionUrls;
	}

	public void setPassedWithExceptionUrls(String passedWithExceptionUrls) {
		this.passedWithExceptionUrls = passedWithExceptionUrls;
	}

	public String getFailedUrls() {
		return failedUrls;
	}

	public void setFailedUrls(String failedUrls) {
		this.failedUrls = failedUrls;
	}

	public String getNewUrls() {
		return newUrls;
	}

	public void setNewUrls(String newUrls) {
		this.newUrls = newUrls;
	}

	public String getDeletedUrls() {
		return deletedUrls;
	}

	public void setDeletedUrls(String deletedUrls) {
		this.deletedUrls = deletedUrls;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		/*
		 * String resultBeanInString =
		 * ";Locale:"+this.getLocale()+";totalCount:"+this.getTotalCount()+
		 * ";passedCount:"+this.getPassedCount()+";passedWithExceptionCount:"
		 * +this.getPassedWithExceptionCount()+";failedCount:"+this.getFailedCount()+
		 * ";newUrlCount:"+this.getNewUrlCount()+";deletedUrlCount:"+this.
		 * getDeletedUrlCount()+";lastUpdated:"+this.getLastUpdated();
		 */
		String resultBeanInString = ";Locale:" + this.getLocale() + ";totalCount:" + this.getTotalCount()
				+ ";passedCount:" + this.getPassedCount() + ";passedWithExceptionCount:"
				+ this.getPassedWithExceptionCount() + ";failedCount:" + this.getFailedCount() + ";lastUpdated:"
				+ this.getLastUpdated();
		return resultBeanInString;
	}

}
