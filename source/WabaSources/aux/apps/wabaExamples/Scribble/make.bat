@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q Scribble *.class
..\..\bin\exegen /q /i icon.bmp Scribble Scribble Scribble

echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet Scribble
echo :    appletviewer Scribble.html
echo :
echo : To install the program on a PalmPilot, install the Scribble.pdb and
echo : Scribble.prc files. To install the program under Windows CE, install
echo : the Scribble.wrp file in the \Program Files\Scribble directory
echo : and then install the the Scribble.lnk shortcut file where it can be
echo : executed.

set CLASSPATH=%OLDCLASSPATH%
 