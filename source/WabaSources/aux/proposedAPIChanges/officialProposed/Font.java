/*
PROPOSED CHANGE SUMMARY

1. Corrected metrics.  Presently Waba "pretends" that a 8-9 point font is
   actually a 12 point font.  It does this by adding 3-4 points the font
   size before rendering it.  As a result many Waba applications are hard
   coded to say "Helvetica 12" when in *fact* what they're getting is
   something approximating Helvetica 8 or 9.  I don't know why this decision
   was originally made, but it's not a good one.  It has got to be fixed.
   
   How to implement this in a backward-compatible fashion?  Well, the best
   I've been able to figure out is to deprecate (that's right) the use of
   new Font(name,style,size).  Instead, new Waba apps should use
   new Font(name,style,size,oldWaba=false).  It's not pretty, but it works
   without breaking existing apps.  Otherwise, we could go the pretty route
   and simply redefine Font, but it would break a lot of stuff. 
   
2. Platform-independent font usage.  Not all platforms have Helvetica.  Not
   all platforms have italic.  Not all platforms can render at 18 point.  But
   Waba programmers are presently hard-coding their applications with certain
   broken assumptions about the platform they're deploying on.  Here's how I
   propose to fix this:
   
        A. Font now can tell you what fonts it has available, by name.
           Further, it can tell you what sizes are common for a given name.
           Finally, it can tell you what styles are available for a given
           name and size.  This allows Waba applications to display
           font/size/style menus appropriate to the task.
        
        B. You can also request any arbitrary font, and check to see if it is
           "valid" (that is, reasonably renderable on the device).  If it is
           not valid, you can still use it, but understand that the device will
           attempt to approximate it to the best of its ability, which may not
           be very good.  For example: requesting a 72 point font on PalmOS
           will result in an "invalid" font.

        C. Unless you're presenting the user with font choice options, you
           should be using the "standard" fonts in your system, and furthermore
           should be resizing your widgets accordingly.  To help you here, Font
           can now provide you with fonts appropriate for certain common uses:
           "System" (a font appropriate for widget text display in your OS), 
           "User" (a font appropriate for user text display in your OS), and if
           you insist on hard-coding, "Basic" (PalmOS-sized), "Basic Bold" 
           (PalmOS-sized, bold), "Big" (a large, 18-point-ish or so font roughly
           approximating the "big" font on some Palm boxes I'm told)

3. Additional style options.  In addition to PLAIN and BOLD, I propose the
   following new style options, which may or may not be available on the target
   platform: ITALIC, UNDERLINE, OUTLINE, SUPERSCRIPT, SUBSCRIPT.  You can get
   any combination of these styles (BOLD+ITALIC+UNDERLINE) as well.
   SUBSCRIPT+SUPERSCRIPT=PLAIN.
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


/// PROPOSED CHANGE
// Add the styles ITALIC, UNDERLINE, OUTLINE, SUPERSCRIPT, and SUBSCRIPT,
// such that they can be ORed (added) together to form style permutations.

/** A plain font style. */
public static final int PLAIN  = 0;

/** A bold font style -- add this to other font styles to get mixtures. */
public static final int BOLD   = 1;

/** An italic font style -- add this to other font styles to get mixtures. */
public static final int ITALIC   = 2;

/** An underlined font style -- add this to other font styles to get mixtures. */
public static final int UNDERLINE   = 4;

/** An outlined font style -- add this to other font styles to get mixtures. */
public static final int OUTLINE   = 8;

/** A superscripted font style -- add this to other font styles to get mixtures. */
public static final int SUPERSCRIPT   = 128;

/** A subscripted font style -- add this to other font styles to get mixtures. */
public static final int SUBSCRIPT   = 256;


/// PROPOSED CHANGE
// Add usage constants so a user can request a font appropriate or
// a given use rather than requesting a font by an arbitrary font name.

/** A small unbolded font with metrics that approximately match the Palm's basic font. */
public static final int TINY_USE = 0;

/** A small bolded font with metrics that approximately match the Palm's basic bold font. */
public static final int TINY_BOLD_USE = 1;

/** A font, possibly bolded, to use for title text in widgets.   Note that the metrics of this font are not necessarily the metrics of the "standard" PalmOS font.  You will need to adjust your widget sizes to fit with the metrics of this font if you use it.  A good Waba program will use the SYSTEM_USE and USER_USE fonts rather than the TINY_USE font.*/
public static final int SYSTEM_USE = 2;

/** A font to use for text that the user creates.  Note that the metrics of this font are not necessarily the metrics of the "standard" PalmOS font.  You will need to adjust your widget sizes to fit with the metrics of this font if you use it.  A good Waba program will use the SYSTEM_USE and USER_USE fonts rather than the TINY_USE font.*/
public static final int USER_USE = 3;

/** A large, possibly bolded, font which approximately matches the "big" font on some Palm boxes. */
public static final int BIG_USE = 4;



/// PROPOSED CHANGE
// Deprecate Font(name, style, size) and replace with
// Font(name, style, size, oldWaba).  This ugly approach is the only way
// I could think of fixing a grievous Waba problem and still be backward
// compatible.  In essence, if oldWaba==true, then if the name is "Helvetica"
// and the size is <= 12, and the style is PLAIN or BOLD, then either 
// TINY_USE or TINY_BOLD_USE will be used instead.  Otherwise, the true
// requested font name, size, and style will be used.  This is because many
// Palm developers have expected Helvetica 12 (plain/bold) to be the 'Palm
// font', which is approximately Helvetica 8.5 (plain/bold).

/**
  * DO NOT USE THIS METHOD -- it only exists to be backward compatable with
  * many old Waba applications with incorrect metrics.  Instead, you should
  * use new Font(name,style,size,true), and pick a font which can actually
  * be provided by the platform (see getNames(), getSizes(), getStyles()).
  *
  * <p> Creates a font of the given name, style and size. Font styles are defined
  * in this class.  If the font name is "Helvetica", and the size is 12 or below,
  * then TINY_USE is substituted as the font, though the style remains intact.
  *
  * @see #PLAIN
  * @see #BOLD
  * @see #ITALIC
  * @see #UNDERLINE
  * @see #OUTLINE
  * @see #SUPERSCRIPT
  * @see #SUBSCRIPT
  * @see Graphics
  @deprecated
  */
public Font(String name, int style, int size)
	{
        this(name,style,size,true);
	}

/**
Try to use Font(int use) instead.
*/

public Font(String name, int style, int size, boolean oldWaba)
	{
        if (oldWaba && name.equals("Helvetica") && size <= 12)
            {
            if (style==BOLD) 
		setFont(nameForUse(TINY_BOLD_USE),
			styleForUse(TINY_BOLD_USE),
			sizeForUse(TINY_BOLD_USE));
            else if (style==PLAIN)
		setFont(nameForUse(TINY_USE),
			styleForUse(TINY_USE),
			sizeForUse(TINY_USE));
            else setFont(name,style,size);
            }
        else setFont(name,style,size);
	}

/// PROPOSED CHANGE
// Add Font(int use), nameForUse(use), sizeForUse(use), and
// styleForUse(use) so a developer can specify a font for a given
// use rather than having to specify an exact name.

public Font(int use)
        {
        setFont(nameForUse(use),styleForUse(use),sizeForUse(use));
        }

// NATIVE INFORMATION:
// HASH: 462212063
// C NAME: FontNameForUse
/** Returns the standard name for the given use. */
public static native String nameForUse(int use);

// NATIVE INFORMATION:
// HASH: 462175374
// C NAME: FontSizeForUse
/** Returns the standard size for the given use. */
public static native int sizeForUse(int use);

// NATIVE INFORMATION:
// HASH: 462182927
// C NAME: FontStyleForUse
/** Returns the standard style for the given use. */
public static native int styleForUse(int use);

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
  * @see #ITALIC
  * @see #UNDERLINE
  * @see #OUTLINE
  * @see #SUPERSCRIPT
  * @see #SUBSCRIPT
 */
public int getStyle()
	{
	return style;
	}

/** Sets new font info. 
  * @see #PLAIN
  * @see #BOLD
  * @see #ITALIC
  * @see #UNDERLINE
  * @see #OUTLINE
  * @see #SUPERSCRIPT
  * @see #SUBSCRIPT
*/
public void setFont(String _name, int _style, int _size )
    {
    name = _name;
    size = _size;
    style = _style;
    }

/// PROPOSED CHANGE
// add isValidFont(_name, _style, _size) which indicates if the font can
// be displayed exactly as defined.  Otherwise it will be "approximated"
// to the most similar displayable font for the platform

/** Returns true if this font can be displayed exactly as you have defined it.
    You can still create this
    font and display it even if this method returns false, 
    but the system will reduce the font to the "closest"
    displayable font.  This reduction is platform-dependent."
  * @see #PLAIN
  * @see #BOLD
  * @see #ITALIC
  * @see #UNDERLINE
  * @see #OUTLINE
  * @see #SUPERSCRIPT
  * @see #SUBSCRIPT
    */
// NATIVE INFORMATION:
// HASH: 462164002
// C NAME: FontIsValidFont
public static native boolean isValidFont(String _name, int _style, int _size);

/// PROPOSED CHANGE
// add getNames(), getSizes(String name), and getStyles(String name, int size)
// which provide lists of valid names, sizes for those names, and styles for
// the given names and sizes.  This permits a developer to present the user
// with a list of available fonts on the platform for the user to pick from.

// NATIVE INFORMATION:
// HASH: 462200861
// C NAME: FontGetNames
/** Returns a list of all valid font names.  Font names will be unique. */
public static native String[] getNames();

// NATIVE INFORMATION:
// HASH: 462207198
// C NAME: FontGetSizes
/** Returns a list of all common displayable sizes for a given font name.
Other sizes may be available -- you'll need to check those with isValidFont()
@see #isValidFont
*/
public static native int[] getSizes(String name);

// NATIVE INFORMATION:
// HASH: 462219424
// C NAME: FontGetStyles
/** Returns a list of styles for a given font name and size.  You may add
any of these provided styles together to create a valid combined style. 
For example, if the system returns [Font.BOLD,Font.ITALIC], 
you may assume that you can create Font.BOLD+Font.ITALIC.  Also, Font.PLAIN
should not appear in this list -- it is assumed to be always valid.*/
public static native int[] getStyles(String name, int size);
}
