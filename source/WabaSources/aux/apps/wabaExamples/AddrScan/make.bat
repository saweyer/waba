@echo off

set OLDCLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;..\..\classes;.

rem compile program, build warp files and launchers
javac *.java
..\..\bin\warp c /q AddrScan *.class
..\..\bin\exegen /q /w 160 /h 160 /i icon.bmp AddrScan AddrScan AddrScan

echo : To install the program on a PalmPilot, install the AddrScan.pdb and
echo : AddrScan.prc files.
echo :
echo : This program only runs on the PalmPilot since it accesses the PalmPilot
echo : Address book database.
echo :
echo : This program won't run under a Palm Personal since it requires
echo : 12K of dynamic memory and that is just over the limit available on a
echo : Palm Personal. It will run on a Palm Professional or Palm III.
set CLASSPATH=%OLDCLASSPATH%
 