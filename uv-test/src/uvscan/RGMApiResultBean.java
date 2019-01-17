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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import utility.Configuration;
import utility.Utility;

public class RGMApiResultBean {
	String message;
	public void setErrorTitle(String message)
	{
		this.message=message;
	}
	public void setInitiated(long startTime) {
		// TODO Auto-generated method stub
		
	}
	public void setApiTime(long timeTaken) {
		// TODO Auto-generated method stub
		
	}
	public void setReportUrl(String constant) {
		// TODO Auto-generated method stub
		
	}
}
