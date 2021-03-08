/*
Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.

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
 * Font is the character font used when drawing text on a surface.
 * <p>
 * Fonts have a name, such as "Helvetica", a style and a point size (usually
 * around 10). It's important to note that many devices have an extremely
 * limited number of fonts. For example, most PalmPilot devices have only
 * two fonts: plain and bold. If the font specified can't be found during
 * drawing, the closest matching font will be used.
 * <p>
 * Here is an example showing text being drawn in a given font:
 *
 * <pre>
 * ...
 * Font font = new Font("Helvetica", Font.BOLD, 10);
 * }
 *
 * public void onPaint(Graphics g)
 * {
 * g.setFont(font);
 * g.drawText("Hello", 10, 10);
 * ...
 * </pre>
 */

public class Font
{
String name;
int style;
int size;

/** A plain font style. */
public static final int PLAIN  = 0;

/** A bold font style. */
public static final int BOLD   = 1;

/**
  * Creates a font of the given name, style and size. Font styles are defined
  * in this class.
  * @see #PLAIN
  * @see #BOLD
  * @see Graphics
  */
public Font(String name, int style, int size)
	{
	this.name = name;
	this.style = style;
	this.size = size;
	}

/*
protected java.awt.Font getAWTFont()
	{
	int style = 0;
	if (this.style == PLAIN)
		style = java.awt.Font.PLAIN;
	else if (this.style == BOLD)
		style = java.awt.Font.PLAIN;
	return new java.awt.Font(name, style, this.size - 3);
	}
*/

/** Returns the name of the font. */
public String getName()
	{
	return name;
	}

/** Returns the size of the font. */
public int getSize()
	{
	return size;
	}

/**
 * Returns the style of the font. Font styles are defined in this class.
 * @see #PLAIN
 * @see #BOLD
 */
public int getStyle()
	{
	return style;
	}
}
