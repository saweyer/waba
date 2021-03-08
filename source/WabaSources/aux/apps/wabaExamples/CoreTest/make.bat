@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q CoreTest *.class test.bmp
..\..\bin\exegen /q /i icon.bmp CoreTest CoreTest CoreTest

echo :
echo : To run this program under Java, make sure you're CLASSPATH environment
echo : variable contains the waba classes directory and the current directory
echo : and execute either one of the following commands:
echo :
echo :    java waba.applet.Applet CoreTest
echo :    appletviewer CoreTest.html
echo :
echo : Note: We have seen some drawing problems when running this program
echo : under some JDK 1.1 Win32 Java Virtual Machines. The drawing problems
echo : appear to be caused by bugs in the JDK VM's drawing code.
echo :
echo : To install the program on a PalmPilot, install the CoreTest.pdb and
echo : CoreTest.prc files. To install the program under Windows CE, install
echo : the CoreTest.wrp file in the \Program Files\CoreTest directory
echo : and then install the the CoreTest.lnk shortcut file where it can be
echo : executed.

set CLASSPATH=%OLDCLASSPATH%
