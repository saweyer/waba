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

import waba.fx.*;
import waba.sys.Vm;


/**
 * Window is a "floating" top-level window. This class is not functional
 * enough to be used for dialogs and other top-level windows, it 
 * currently exists only as a base class for the MainWindow class.
 */

public class Window extends Container implements ISurface
{

protected static KeyEvent _keyEvent = new KeyEvent();
protected static PenEvent _penEvent = new PenEvent();
protected static ControlEvent _controlEvent = new ControlEvent();

boolean needsPaint;
int paintX, paintY, paintWidth, paintHeight;
Graphics _g;
Control _focus;
boolean _inPenDrag;

/** Constructs a window. */
public Window()
	{
	_nativeCreate();
	}

private native void _nativeCreate();


/**
 * Sets focus to the given control. When a user types a key, the control with
 * focus get the key event. At any given time, only one control in a window
 * can have focus. Calling this method will cause a FOCUS_OUT control event
 * to be posted to the window's current focus control (if one exists)
 * and will cause a FOCUS_IN control event to be posted to the new focus
 * control.
 */
public void setFocus(Control c)
	{
	if (_focus != null)
		{
		_controlEvent.type = ControlEvent.FOCUS_OUT;
		_controlEvent.target = _focus;
		_focus.postEvent(_controlEvent);
		}
	_focus = c;
	if (c != null)
		{
		_controlEvent.type = ControlEvent.FOCUS_IN;
		_controlEvent.target = c;
		c.postEvent(_controlEvent);
		}
	}

/**
 * Returns the focus control for this window.
 * @see waba.ui.Window#setFocus
 */
public Control getFocus()
	{
	return _focus;
	}

/**
 * Adds a damage rectangle to the current list of areas that need
 * repainting.
 */
protected void damageRect(int x, int y, int width, int height)
	{
	if (needsPaint)
		{
		int ax = x + width;
		int ay = y + height;
		int bx = paintX + paintWidth;
		int by = paintY + paintHeight;
		if (paintX < x)
			x = paintX;
		if (paintY < y)
			y = paintY;
		if (ax > bx)
			width = ax - x;
		else
			width = bx - x;
		if (ay > by)
			height = ay - y;
		else
			height = by - y;
		}
	paintX = x;
	paintY = y;
	paintWidth = width;
	paintHeight = height;
	needsPaint = true;
	}

/**
 * Called by the VM to post key and pen events.
 */
public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
	{
	Event event;

	if (type == KeyEvent.KEY_PRESS)
		{
		_keyEvent.type = type;
		_keyEvent.key = key;
		_keyEvent.modifiers = modifiers;
		event = _keyEvent;
		if (_focus == null)
			_focus = this;
		}
	else
		{
		// set focus to new control
		if (type == PenEvent.PEN_DOWN)
			{
			Control c = findChild(x, y);
			if (c != _focus)
				setFocus(c);
			_inPenDrag = true;
			}
		else if (type == PenEvent.PEN_MOVE && _inPenDrag)
			type = PenEvent.PEN_DRAG;
		else if (type == PenEvent.PEN_UP)
			_inPenDrag = false;

		_penEvent.type = type;
		_penEvent.x = x;
		_penEvent.y = y;

		// translate x, y to coordinate system of target
		Control c = _focus;
		while (c != null)
			{
			_penEvent.x -= c.x;
			_penEvent.y -= c.y;
			c = c.parent;
			}

		_penEvent.modifiers = modifiers;
		event = _penEvent;
		}
	event.target = _focus;
	event.timeStamp = timeStamp;
	if (_focus != null)
		_focus.postEvent(event);
	if (needsPaint)
		_doPaint(paintX, paintY, paintWidth, paintHeight);
	}

/**
 * Called by the VM to repaint an area.
 */
public void _doPaint(int x, int y, int width, int height)
	{
	if (_g == null)
		_g = new Graphics(this);

	// clear background
	_g.setClip(x, y, width, height);
	if (Vm.isColor())
		_g.setColor(200, 200, 200);
	else
		_g.setColor(255, 255, 255);
	_g.fillRect(x, y, width, height);
	onPaint(_g);
	_g.clearClip();
	paintChildren(_g, x, y, width, height);

	if (needsPaint)
		{
		int ax = x + width;
		int ay = y + height;
		int bx = paintX + paintWidth;
		int by = paintY + paintHeight;
		if (x <= paintX && y <= paintY && ax >= bx && ay >= by)
			needsPaint = false;
		}
	}
}
