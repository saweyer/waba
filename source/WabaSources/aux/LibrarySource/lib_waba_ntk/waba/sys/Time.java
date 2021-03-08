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
 * Time identifies a date and time.
 * <p>
 * Here is an example of Time being used to display the current date:
 *
 * <pre>
 * Time t = new Time();
 * ...
 * g.drawText("Today is " + t.year + "/" + t.month + "/" + t.day);
 * </pre>
 */

public class Time
{
/** The year as its full set of digits (year 2010 is 2010). */
public int year;

/** The month in the range of 1 to 12. */
public int month;

/** The day in the range of 1 to the last day in the month. */
public int day;

/** The hour in the range of 0 to 23. */
public int hour;

/** The minute in the range of 0 to 59. */
public int minute;

/** The second in the range of 0 to 59. */
public int second;

/** Milliseconds in the range of 0 to 999. */
public int millis;

/**
 * Constructs a time object set to the current date and time.
 */
public Time()
	{
	_nativeCreate();
	}

private native void _nativeCreate();

}