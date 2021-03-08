/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

package waba.sys;


/**
 * Vm contains various system level methods.
 * <p>
 * This class contains methods to copy arrays, obtain a timestamp,
 * sleep and get platform and version information.
 */

// NOTE:
// In the future, these methods may include getting unique object id's,
// getting object classes, sleep (for single threaded apps),
// getting amount of memory used/free, etc.
// The reason these methods should appear in this class and not somewhere
// like the Object class is because each method added to the Object class
// adds one more method to every object in the system.

public class Vm
{
private Vm()
	{
	}

/**
 * Copies the elements of one array to another array. This method returns
 * true if the copy is successful. It will return false if the array types
 * are not compatible or if either array is null. If the length parameter
 * would cause the copy to read or write past the end of one of the arrays,
 * an index out of range error will occur. If false is returned then no
 * copying has been performed.
 * @param srcArray the array to copy elements from
 * @param srcStart the starting position in the source array
 * @param dstArray the array to copy elements to
 * @param dstStart the starting position in the destination array
 * @param length the number of elements to copy
 */
public static native boolean copyArray(Object srcArray, int srcStart,
	Object dstArray, int dstStart, int length);



/**
 * Returns true if the system supports a color display and false otherwise.
 */
public static native boolean isColor();


/**
 * Returns a time stamp in milliseconds. The time stamp is the time
 * in milliseconds since some arbitrary starting time fixed when
 * the VM starts executing. The maximum time stamp value is (1 << 30) and
 * when it is reached, the timer will reset to 0 and will continue counting
 * from there.
 */
public static native int getTimeStamp();



/** Returns the platform the Virtual Machine is running under as a string. */
public static native String getPlatform();


/** 
 * Returns the username of the user running the Virutal Machine. Because of
 * Java's security model, this method will return null when called in a Java
 * applet. This method will also return null under most WinCE devices (that
 * will be fixed in a future release).
 */
public static native String getUserName();


/**
 * Returns the version of the Waba Virtual Machine. The major version is
 * base 100. For example, version 1.0 has value 100. Version 2.0 has a
 * version value of 200. A beta 0.8 VM will have version 80.
 */
public static int getVersion()
	{
	return 100;
	}

/**
 * Executes a command.
 * <p>
 * As an example, the following call could be used to run the command
 * "scandir /p mydir" under Java, Win32 or WinCE:
 * <pre>
 * int result = Vm.exec("scandir", "/p mydir", 0, true);
 * </pre>
 * This example executes the Scribble program under PalmOS:
 * <pre>
 * Vm.exec("Scribble", null, 0, false);
 * </pre>
 * This example executes the web clipper program under PalmOS, telling
 * it to display a web page by using launchCode 54 (CmdGoToURL).
 * <pre>
 * Vm.exec("Clipper", "http://www.yahoo.com", 54, true);
 * </pre>
 * The args parameter passed to this method is the arguments string
 * to pass to the program being executed.
 * <p>
 * The launchCode parameter is only used under PalmOS. Under PalmOS, it is
 * the launch code value to use when the Vm calls SysUIAppSwitch().
 * If 0 is passed, the default launch code (CmdNormalLaunch) is used to
 * execute the program.
 * <p>
 * The wait parameter passed to this method determines whether to execute
 * the command asynchronously. If false, then the method will return without
 * waiting for the command to complete execution. If true, the method will
 * wait for the program to finish executing and the return value of the
 * method will be the value returned from the application under Java, Win32
 * and WinCE.
 * <p>
 * Under PalmOS, the wait parameter is ignored since executing another
 * program terminates the running program.
 *
 * <p><b>VERSION 2: By Sean Luke
 *
 * @param command the command to execute
 * @param args command arguments
 * @param launchCode launch code for PalmOS applications
 * @param wait whether to wait for the command to complete execution before returning
 */
public static native int exec(String command, String args, int launchCode, boolean wait);


/**
 * Sets the device's "auto-off" time. This is the time in seconds where, if no
 * user interaction occurs with the device, it turns off. To keep the device always
 * on, pass 0. This method only works under PalmOS. The integer returned is
 * the previous auto-off time in seconds.
 */
public static native int setDeviceAutoOff(int seconds);


/**
 * Causes the VM to pause execution for the given number of milliseconds.
 * @param millis time to sleep in milliseconds
 */
public static native void sleep(int millis);


/** Returns the string version of the class name for this object.
This differs from Java's getClass() method because Waba does not
have the notion of class objects. You can use this class name in
the makeInstance() method. */
public static native String getClassName(Object obj);

/** Creates an instance of the provided class name and calls the default
constructor on it.  Returns the new instance; or null if there is no such
class or if classname is null. */
public static native Object makeInstance(String classname);

}