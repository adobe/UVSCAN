javac -classpath "C:\Users\shrbhatt\Jars\*" -sourcepath src -d bin src/utility/*.java
javac -classpath "C:\Users\shrbhatt\Jars\*" -sourcepath src -d bin src/uvscan/*.java


rem Please change the paths according to your directory structure
rem
rem command: javac -classpath <arg1> -sourcepath src -d <arg2> <arg3>

rem arg1= path-to- \jar_folder\*	-	  Jars (* refers to all jar files of folder containing dependencies-jar-folder)
rem arg2= ClassFilesDirectory	-	bin (created just under the parent directory)
rem arg3= path-to- \src\package<x>\*.java	-	(* refers to all java files in package<x>)

rem arg3: In the 1st command - path to src\utility\*.java 
rem arg3: In the 2nd command - path to src\uvscan\*.java 
