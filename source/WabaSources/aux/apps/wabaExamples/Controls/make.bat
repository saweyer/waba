@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q Controls *.class
..\..\bin\exegen /q /i icon.bmp /w 160 /h 160 Controls Controls Controls

echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet Controls
echo :    appletviewer Controls.html
echo :
echo : To install the program on a PalmPilot, install the Controls.pdb and
echo : Controls.prc files. To install the program under Windows CE, install
echo : the Controls.wrp file in the \Program Files\Controls directory
echo : and then install the the Controls.lnk shortcut file where it can be
echo : executed.

set CLASSPATH=%OLDCLASSPATH%
 