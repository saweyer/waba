@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q Web *.class
..\..\bin\exegen /q /w 160 /h 160 /m 7000 /i icon.bmp Web Web Web

echo :
echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet Web
echo :    appletviewer Web.html
echo :
echo : To install the program on a PalmPilot, install the Web.pdb and
echo : Web.prc files. To install the program under Windows CE, install
echo : the Web.wrp file in the \Program Files\Web directory
echo : and then install the the Web.lnk shortcut file where it can be
echo : executed.
echo :
echo : You will need an internet connection to run this program so it
echo : can get the web page it requests.

set CLASSPATH=%OLDCLASSPATH%
 