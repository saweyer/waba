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
 * IKeys is an interface containing values for special keys and modifiers.
 * <p>
 * Below is an example of IKeys being used.
 *
 * <pre>
 * public void onEvent(Event event)
 *  {
 *  if (event.type == KeyEvent.KEY_PRESS)
 *   {
 *   KeyEvent ke = (KeyEvent)event;
 *   if ((ke.modifiers & CONTROL) > 0)
 *      ... control key was held down
 *   if (ke.key == PAGE_DOWN)
 *      ... page down key pressed
 * </pre>
 */
public interface IKeys
{
// NOTE: The WabaVM indexes directly to these values

/** modifier for alt key */
public static final int ALT      = (1 << 0);
/** modifier for control key */
public static final int CONTROL  = (1 << 1);
/** modifier for shift key */
public static final int SHIFT    = (1 << 2);

/** special key */
public static final int PAGE_UP   = 75000;
/** special key */
public static final int PAGE_DOWN = 75001;
/** special key */
public static final int HOME      = 75002;
/** special key */
public static final int END       = 75003;
/** special key */
public static final int UP        = 75004;
/** special key */
public static final int DOWN      = 75005;
/** special key */
public static final int LEFT      = 75006;
/** special key */
public static final int RIGHT     = 75007;
/** special key */
public static final int INSERT    = 75008;
/** special key */
public static final int ENTER     = 75009;
/** special key */
public static final int TAB       = 75010;
/** special key */
public static final int BACKSPACE = 75011;
/** special key */
public static final int ESCAPE    = 75012;
/** special key */
public static final int DELETE    = 75013;
/** special key */
public static final int MENU      = 75014;
/** special key */
public static final int COMMAND   = 75015;
}

