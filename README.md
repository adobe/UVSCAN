# UVSCAN

## Introduction

UVScan (URL Verification Scan) is a tool to Verify & Validate URL structure and redirection. URLs are checked for valid URL pattern (base & redirected) across all locales against established expected results and for 404 & other error page redirection. For newly found URLs basic validations are performed.

### Installation

1. Clone the repository.

2. Download and install JDK>8 and set as Path under the Environment Variables in the Control Panel.

### Usage

1. Copy UvScanConfiguration.json from /uv-test/src/utility/ to /uv-test/bin/utility/

2. Add locales in a txt file (locales.txt). For example:  /uv-test/Sample_Data/locales.txt.

3. Place all strings with/without URLs in a csv file containing records with the following headers -  "String Id", "Core String", "Localized String", "Locale". For example:  /uv-test/Sample_Data/Photoshop_x.x.csv

4. To compile the project - run "compile.bat" (change the paths accordingly)

    Compile.bat: 
            
             javac -classpath <arg1> -sourcepath src -d <arg2> <arg3>

             arg1= path-to- \jar_folder\*	-	(* refers to all jar files in dependencies-jar-folder)
             arg2= Class_Files_Directory	-	bin (created just under the parent directory of this file)
             arg3= path-to- \src\package<x>\*.java	-	(* refers to all java files in package<x>)

             arg3: In the 1st command - path to src\utility\*.java 
             arg3: In the 2nd command - path to src\uvscan\*.java 
     
    For example:
    
                javac -classpath Jars/* -sourcepath src -d bin src/utility/*.java
                
                javac -classpath Jars/* -sourcepath src -d bin src/uvscan/*.java


5. To run the project - run "run.bat" (read comments & change the paths accordingly)

    Run.bat:  
          
          java -classpath "<arg1>;<arg2>" <arg3> <arg4> <arg5> <arg6> <arg7> <arg8> <arg9>

          arg1= path-to- \build	-	Which stores .class files after compiling (mentioned in compile.bat)
          arg2= path-to- \jar_folder\*	-       Jars (* refers to all jar files of folder containing dependencies-jar-folder)
          arg3= package.MainClass	-	uvscan.Start
          arg4= args[0]= ldap
          arg5= args[1]= projectname
          arg6= args[2]= releasename
          arg7= args[3]= path-to- \locales.txt
          arg8= args[4]= path-to- \results_directory
          arg9= args[5]= path-to- \dict.csv	
          
          locales.txt: Please refer to \Sample_data\locales.txt for sample locales file 
          dict.csv:    Please refer to \Sample_data\Photoshop_x.x.csv for sample csv 
          Header-      String Id	|	Locale	|	Core String	|	Localized String
          Data-	$$$/public/LensCorrect/LensProfileCreatorInfo	|	nl_NL	|	http://www.adobe.com/go/alpc	|	http://www.adobe.com/go/alpc_nl
          Data-         *** Can enter any string with or without urls ***
          
    For example: 
                
                java -classpath bin/;Jars/* uvscan.Start username Photoshop ps_main Sample_Data/locales.txt "" Sample_Data/Photoshop_x.x.csv
                
6. Check the results in the <results_directory>/UVSCAN/Results/FinalReportVsUrl.xlsx. Read the [Analyzing-UVSCAN-Result](uv-test/Analyzing-UVSCAN-Result.txt) for more information.


### Contributing

Contributions are welcomed! Read the [Contributing Guide](.github/CONTRIBUTING.md) for more information.

### Licensing

This project is licensed under the Apache V2 License. See [LICENSE](LICENSE) for more information.

### Authors and Maintainers

* Abhay Aggarwal
* Saurav Gupta
* Alice Ng
* Tushar Rajvanshi
* Shrirupa Bhattacharjee
