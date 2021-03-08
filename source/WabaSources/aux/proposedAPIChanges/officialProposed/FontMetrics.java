/*
PROPOSED CHANGE SUMMARY:
0. Widen permissions on the font member instance, to be more compatible
with existing JDK code.
1. Add functions which have more compatible names to existing JDK code,
and deprecate the older functions.
2. Ask why we are including surfaces in the FontMetrics object.  Does anyone
really use them?
*/


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
 * FontMetrics computes font metrics including string width and height.
 * <p>
 * FontMetrics are usually used to obtain information about the widths and
 * heights of characters and strings when drawing text on a surface.
 * A FontMetrics object references a font and surface since fonts may have
 * different metrics on different surfaces.
 * <p>
 * Here is an example that uses FontMetrics to get the width of a string:
 *
 * <pre>
 * ...
 * Font font = new Font("Helvetica", Font.BOLD, 10);
 * FontMetrics fm = getFontMetrics();
 * String s = "This is a line of text.";
 * int stringWidth = fm.getTextWidth(s);
 * ...
 * </pre>
 */

public class FontMetrics
{
/// PROPOSED CHANGE
// make font protected rather than package, to be compatible
// with the JDK 1.0

protected Font font;
ISurface surface;
int ascent;
int descent;
int leading;

/// QUERY
// Why do we need surface for FontMetrics?  Our only surfaces are
// images and windows -- are there actually differences in rendering
// between them on *any* environments expected?  I doubt it.  We might
// consider doing away with it.

/**
 * Constructs a font metrics object referencing the given font and surface.
 * <p>
 * If you are trying to create a font metrics object in a Control subclass,
 * use the getFontMetrics() method in the Control class.
 * @see waba.ui.Control#getFontMetrics(waba.fx.Font font)
 */
public FontMetrics(Font font, ISurface surface)
	{
	this.font = font;
	this.surface = surface;
	_nativeCreate();
	}

private native void _nativeCreate();

/**
 * Returns the ascent of the font. This is the distance from the baseline
 * of a character to its top.
 */
public int getAscent()
	{
	return ascent;
	}


/// PROPOSED CHANGE
// deprecate getCharWidth(char c) and replace with charWidth(char c)
// which is compatible with JDK 1.0

/**
 * Returns the width of the given character in pixels.
@deprecated
 */
public native int getCharWidth(char c);
public int charWidth(char c) { return getCharWidth(c); }


/**
 * Returns the descent of a font. This is the distance from the baseline
 * of a character to the bottom of the character.
 */
public int getDescent()
	{
	return descent;
	}

/**
 * Returns the height of the referenced font. This is equal to the font's
 * ascent plus its descent. This does not include leading (the space between lines).
 */
public int getHeight()
	{
	return ascent + descent;
	}

/**
 * Returns the external leading which is the space between lines.
 */
public int getLeading()
	{
	return leading;
	}


/// PROPOSED CHANGE
// deprecate getTextWidth(String s) and replace with stringWidth(String s)
// which is compatible with JDK 1.0

/**
 * Returns the width of the given text string in pixels.
 @deprecated
 */
public native int getTextWidth(String s);
public int stringWidth(String s) { return getTextWidth(s); }


/// PROPOSED CHANGE
// deprecate getTextWidth(char chars[], int start, int count)
// and replace with charWidth(char chars[], int start, int count)
// which is compatible with JDK 1.0

/**
 * Returns the width of the given text in pixels.
 * @param chars the text character array
 * @param start the start position in array
 * @param count the number of characters
 @deprecated
 */
public native int getTextWidth(char chars[], int start, int count);
public int charWidth(char chars[], int start, int count) { return getTextWidth(chars,start,count); }

}