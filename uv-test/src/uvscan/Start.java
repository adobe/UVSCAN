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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Scanner;


public class Start
{

/**
 * Main Function
 * @param args - user id, product name, release name, locales file path, dict file path
 * @throws IOException
 */
	public static void main(String args[]) throws IOException
	{
		List<String> locales= new ArrayList<String>();
		String ldap=args[0];
		String productname=args[1];
		String releasename=args[2];
		String locales_file=args[3];
		String results_file=args[4];
		String csvpath=args[5];
		Scanner s = new Scanner(new File(locales_file));
		while (s.hasNext()){
		    locales.add(s.next());
		}
		s.close();
		UvScanInit uv=new UvScanInit(productname,releasename,locales,results_file);
		uv.runUvScan(ldap,csvpath);
		
	}
}
