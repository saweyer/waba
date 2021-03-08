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
 * Check is a check control.
 * <p>
 * Here is an example showing a check being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * Check check;
 *
 * public void onStart()
 *  {
 *  check = new Check("Check me");
 *  check.setRect(10, 10, 80, 30);
 *  add(check);
 *  }
 * 
 * public void onEvent(Event event)
 *  {
 *  if (event.type == ControlEvent.PRESSED &&
 *      event.target == check)
 *   {
 *   ... handle check being pressed
 * </pre>
 */

public class Check extends Control
{
String text;
Font font;
boolean checked;

/** Creates a check control displaying the given text. */
public Check(String text)
	{
	this.text = text;
	this.font = MainWindow.defaultFont;
	checked = false;
	}

/** Called by the system to pass events to the check control. */
public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		{
		checked = !checked;
		repaint();
		PenEvent pe = (PenEvent)event;
		if (pe.x >= 0 && pe.x < this.width && pe.y >= 0 && pe.y < this.height)
			postEvent(new ControlEvent(ControlEvent.PRESSED, this));
		}
	}

/** Gets the text displayed in the check. */
public String getText()
	{
	return text;
	}

/** Returns the checked state of the control. */
public boolean getChecked()
	{
	return checked;
	}

/** Sets the checked state of the control. */
public void setChecked(boolean checked)
	{
	if (this.checked == checked)
		return;
	this.checked = checked;
	repaint();
	}

/** Draws the check box graphic at the given position. */
public static void drawCheck(Graphics g, boolean checked, int x, int y)
	{
	int x1 = x;
	int y1 = y;
	int x2 = x + 12;
	int y2 = y + 12;
	if (Vm.isColor())
		{
		g.setColor(130, 130, 130);
		g.drawLine(x1, y1, x2 - 1, y);
		g.drawLine(x1, y1 + 1, x1, y2 - 1);
		g.setColor(0, 0, 0);
		g.drawLine(x1 + 1, y1 + 1, x2 - 2, y1 + 1);
		g.drawLine(x1 + 1, y1 + 2, x1 + 1, y2 - 2);
		g.setColor(200, 200, 200);
		g.drawLine(x1 + 1, y2 - 1, x2 - 1, y2 - 1);
		g.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 2);
		g.setColor(255, 255, 255);
		g.drawLine(x1, y2, x2, y2);
		g.drawLine(x2, y, x2, y2 - 1);
		g.fillRect(x + 2, y + 2, 9, 9);
		}
	else
		{
		g.setColor(0, 0, 0);
		g.drawRect(x1 + 1, y1 + 1, 11, 11);
		g.setColor(255, 255, 255);
		g.fillRect(x1 + 2, y1 + 2, 9, 9);
		}
	if (checked)
		{
		g.setColor(0, 0, 0);
		x1 = x + 3;
		y1 = y + 5;
		for (int i = 0; i < 7; i++)
			{
			g.drawLine(x1, y1, x1, y1 + 2);
			x1++;
			if (i < 2)
				y1++;
			else
				y1--;
			}
		}
	}

/** Called by the system to draw the check control. */
public void onPaint(Graphics g)
	{
	int y = (this.height - 13) / 2;
	drawCheck(g, checked, 0, y);

	// draw label
	g.setColor(0, 0, 0);
	g.setFont(font);
	FontMetrics fm = getFontMetrics(font);
	int fontHeight = fm.getHeight();
	y = (this.height - fontHeight) / 2;
	g.drawText(text, 17, y);
	}
}
