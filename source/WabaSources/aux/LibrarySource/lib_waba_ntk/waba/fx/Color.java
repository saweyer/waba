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

package waba.fx;

/**
 * Color represents a color.
 * <p>
 * A color is defined as a mixture of red, green and blue color values.
 * Each value is in the range of 0 to 255 where 0 is darkest and 255 is brightest.
 * For example, Color(255, 0, 0) is the color red.
 * <p>
 * Here are some more examples:
 * <ul>
 * <li>Color(0, 0, 0) is black
 * <li>Color(255, 255, 255) is white
 * <li>Color(255, 0, 0 ) is red
 * <li>Color(0, 255, 0) is green
 * <li>Color(0, 0, 255) is blue
 * </ul>
 */

public class Color
{
int red;
int green;
int blue;

/**
 * Constructs a color object with the given red, green and blue values.
 * @param red the red value in the range of 0 to 255
 * @param green the green value in the range of 0 to 255
 * @param blue the blue value in the range of 0 to 255
 */
public Color(int red, int green, int blue)
	{
	this.red = red;
	this.green = green;
	this.blue = blue;
	}

/** Returns the blue value of the color. */
public int getBlue()
	{
	return blue;
	}

/** Returns the green value of the color. */
public int getGreen()
	{
	return green;
	}

/** Returns the red value of the color. */
public int getRed()
	{
	return red;
	}
}