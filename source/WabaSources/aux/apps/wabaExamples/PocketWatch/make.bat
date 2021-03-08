@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q PocketWatch *.class watch.bmp
..\..\bin\exegen /q /w 160 /h 160 /m 2800 /i icon.bmp PocketWatch PocketWatch PocketWatch

echo :
echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet PocketWatch
echo :    appletviewer PocketWatch.html
echo :
echo : To install the program on a PalmPilot, install the PocketWatch.pdb and
echo : PocketWatch.prc files. To install the program under Windows CE, install
echo : the PocketWatch.wrp file in the \Program Files\PocketWatch directory
echo : and then install the the PocketWatch.lnk shortcut file where it can be
echo : executed.

set CLASSPATH=%OLDCLASSPATH%
