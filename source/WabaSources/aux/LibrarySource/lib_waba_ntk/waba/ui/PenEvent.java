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

package waba.ui;

/**
 * PenEvent is a pen down, up, move or drag event.
 * <p>
 * A mouse drag occurs when a mouse button is pressed down and the
 * mouse is moved. A mouse move occurs when the mouse is moved without
 * a button being held down.
 */

public class PenEvent extends Event
{
/** The event type for a pen or mouse down event. */
public static final int PEN_DOWN = 200;
/** The event type for a pen or mouse move event. */
public static final int PEN_MOVE = 201;
/** The event type for a pen or mouse up event. */
public static final int PEN_UP = 202;
/** The event type for a pen or mouse drag event. */
public static final int PEN_DRAG = 203;

/** The x location of the event. */
public int x;

/** The y location of the event. */
public int y;

/**
 * The state of the modifier keys when the event occured. This is a
 * OR'ed combination of the modifiers present in the IKeys interface.
 * @see IKeys
 */
public int modifiers;
}

