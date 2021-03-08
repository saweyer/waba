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
 * MainWindow is the main window of a UI based application.
 * <p>
 * All Waba programs with a user-interface must have a main window.
 * <p>
 * Here is an example showing a basic application:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onStart()
 *  {
 *  ... initialization code ...
 *  Label label = new Label("Name:");
 *  label.setRect(..);
 *  add(label);
 *  }
 * }
 * </pre>
 */

public class MainWindow extends Window
{

Timer timers;
Font _cachedFont;
FontMetrics _cachedFontMetrics;
public static final Font defaultFont = 
new Font("Helvetica", Font.PLAIN, 12);

/*
    new Font(Font.nameForUse(Font.DEFAULT_USE),
             Font.sizeForUse(Font.DEFAULT_USE),
             Font.styleForUse(Font.DEFAULT_USE));
*/
static MainWindow _mainWindow;

/** Constructs a main window. */
public MainWindow()
	{
	_mainWindow = this;
	_nativeCreate();
	}

private native void _nativeCreate();

/**
 * Notifies the application that it should stop executing and exit. It will
 * exit after executing any pending events. If the underlying system supports
 * it, the exitCode passed is returned to the program that started the app.
 */
public native void exit(int exitCode);


/** Returns the MainWindow of the current application. */
public static MainWindow getMainWindow()
	{
	return _mainWindow;
	}

/** Returns the font metrics for a given font. */
public FontMetrics getFontMetrics(Font font)
	{
	if (font == _cachedFont)
		return _cachedFontMetrics;
	_cachedFont = font;
	_cachedFontMetrics = new FontMetrics(font, this);
	return _cachedFontMetrics;
	}

/**
 * Adds a timer to a control. This method is protected, the public
 * method to add a timer to a control is the addTimer() method in
 * the Control class.
 */
protected Timer addTimer(Control target, int millis)
	{
	Timer t = new Timer();
	t.target = target;
	t.millis = millis;
	t.lastTick = Vm.getTimeStamp();
	t.next = timers;
	timers = t;
	_onTimerTick();
	return t;
	}

/**
 * Removes a timer. This method returns true if the timer was found
 * and removed and false if the given timer could not be found.
 */
public boolean removeTimer(Timer timer)
	{
	if (timer == null)
		return false;
	Timer t = timers;
	Timer prev = null;
	while (t != timer)
		{
		if (t == null)
			return false;
		prev = t;
		t = t.next;
		}
	if (prev == null)
		timers = t.next;
	else
		prev.next = t.next;
	_onTimerTick();
	return true;
	}

/**
 * Called just before an application exits.
 */
public void onExit()
	{
	}

/**
 * Called when an application starts. Initialization code is usually either placed
 * in this method or simply in the app's constructor. This method is called
 * just after the app's constructor is called.
 */
public void onStart()
	{
	}

/**
 * Called by the VM to process timer interrupts. This method is not private
 * to prevent the compiler from removing it during optimization.
 */
public void _onTimerTick()
	{
	int minInterval = 0;
	int now = Vm.getTimeStamp();
	Timer timer = timers;
	while (timer != null)
		{
		int diff = now - timer.lastTick;
		if (diff < 0)
			diff += (1 << 30); // wrap around - max stamp is (1 << 30)
		int interval;
		if (diff >= timer.millis)
			{
			// post TIMER event
			Control c = timer.target;
			_controlEvent.type = ControlEvent.TIMER;
			_controlEvent.target = c;
			c.postEvent(_controlEvent);
			timer.lastTick = now;
			interval = timer.millis;
			}
		else
			interval = timer.millis - diff;
		if (interval < minInterval || minInterval == 0)
			minInterval = interval;
		timer = timer.next;
		}
	_setTimerInterval(minInterval);
	if (needsPaint)
		_doPaint(paintX, paintY, paintWidth, paintHeight);
	}

/**
 * Called to set the VM's timer interval. This method is not public,
 * you should use the addTimer() method in the Control class to
 * create a timer.
 */
protected native void _setTimerInterval(int milliseconds);

}
