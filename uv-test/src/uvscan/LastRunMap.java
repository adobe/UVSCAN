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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps the Url Ids' with their LastRunDate for each locale
 * @author shrbhatt
 *
 */
public class LastRunMap {
	
	private Map<String,String> urlmap;
	public LastRunMap()
	{
		this.urlmap= new HashMap<String, String>();
	}
	
	public void setById(String key, String value)
	{
		this.urlmap.put(key,value);
	}
	
	public String getById(String key)
	{
		return this.urlmap.get(key);
	}
}
