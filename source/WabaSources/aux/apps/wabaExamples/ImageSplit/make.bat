@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q ImageSplit *.class taki.bmp
..\..\bin\exegen /q /w 160 /h 160 /m 4000 /i icon.bmp ImageSplit ImageSplit ImageSplit

echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet ImageSplit
echo :    appletviewer ImageSplit.html
echo :
echo : To install the program on a PalmPilot, install the ImageSplit.pdb and
echo : ImageSplit.prc files. To install the program under Windows CE, install
echo : the ImageSplit.wrp file in the \Program Files\ImageSplit directory
echo : and then install the the ImageSplit.lnk shortcut file where it can be
echo : executed.

set CLASSPATH=%OLDCLASSPATH%
