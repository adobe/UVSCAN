java -classpath "C:\Users\shrbhatt\eclipse-workspace\uv-test\bin;C:\Users\shrbhatt\Jars\*" uvscan.Start shrbhatt Photoshop ps_main "C:\Users\shrbhatt\eclipse-workspace\locales.txt" "C:\Users\shrbhatt\eclipse-workspace\uvscan" "C:\Users\shrbhatt\eclipse-workspace\Photoshop_x.x.csv"

rem Please change the paths according to your directory structure
rem
rem command: java -classpath "<arg1>;<arg2>" <arg3> <arg4> <arg5> <arg6> <arg7> <arg8> <arg9>

rem arg1= path-to- \bin	-	Which stores .class files after compiling (mentioned in compile.bat)
rem arg2= path-to- \jar_folder\*	-	 Jars (* refers to all jar files in dependencies-jar-folder)
rem arg3= package.MainClass	-	uvscan.Start
rem arg4= args[0]= ldap
rem arg5= args[1]= projectname
rem arg6= args[2]= releasename
rem arg7= args[3]= path-to- \locales.txt
rem arg8= args[4]= path-to- \results_directory
rem arg9= args[5]= path-to- \dict.csv	

rem locales.txt:  Please refer to /Sample_Data/locales.txt for sample locales file 

rem dict.csv: 	Please refer to /Sample_Data/Photoshop_x.x.csv for sample dict csv file 
rem :Header-	String Id	|	Locale	|	Core String	|	Localized String
rem :Data-		$$$/public/LensCorrect/LensProfileCreatorInfo	|	nl_NL	|	http://www.adobe.com/go/alpc	|	http://www.adobe.com/go/alpc_nl
rem :Data- 		*** Can enter data with any string with or without urls ***