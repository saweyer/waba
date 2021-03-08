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
 * Radio is a radio control.
 * <p>
 * Here is an example showing a radio being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * Radio radio;
 *
 * public void onStart()
 *  {
 *  radio = new Button("Check me");
 *  radio.setRect(10, 10, 80, 30);
 *  add(radio);
 *  }
 * 
 * public void onEvent(Event event)
 *  {
 *  if (event.type == ControlEvent.PRESSED &&
 *      event.target == radio)
 *   {
 *   ... handle radio being pressed
 * </pre>
 */

public class Radio extends Control
{
String text;
Font font;
boolean checked;

/** Creates a radio control displaying the given text. */
public Radio(String text)
	{
	this.text = text;
	this.font = MainWindow.defaultFont;
	checked = false;
	}

/** Gets the text displayed in the radio. */
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

/** Called by the system to pass events to the radio control. */
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

private final static int coords[] =
	{
	// dark grey top
	4, 0, 7, 0,
	2, 1, 3, 1,
	8, 1, 9, 1,
	// dark grey left
	0, 4, 0, 7,
	1, 2, 1, 3,
	1, 8, 1, 9,
	// black top
	4, 1, 7, 1,
	2, 2, 3, 2,
	8, 2, 9, 2,
	// black left
	1, 4, 1, 7,
	2, 3, 2, 3,
	2, 8, 2, 8,
	// light grey bottom
	2, 9, 3, 9,
	8, 9, 9, 9,
	4, 10, 7, 10,
	// light grey right
	9, 3, 9, 3,
	9, 8, 9, 8,
	10, 4, 10, 7,
	// bottom white
	2, 10, 3, 10,
	8, 10, 9, 10,
	4, 11, 7, 11,
	// right white
	10, 2, 10, 3,
	10, 8, 10, 9,
	11, 4, 11, 7
	};

private static final int greyColors[] =
	{
	130, 130, 130,
	0, 0, 0,
	200, 200, 200,
	255, 255, 255
	};

private static final int bwColors[] =
	{
	255, 255, 255,
	0, 0, 0,
	0, 0, 0,
	255, 255, 255,
	};

/** Draws the radio circle graphic at the given position. */
public static void drawRadio(Graphics g, boolean checked, int x, int y)
	{
	// white center
	g.setColor(255, 255, 255);
	g.fillRect(x + 2, y + 2, 8, 8);

	int colors[];
	if (Vm.isColor())
		colors = greyColors;
	else
		colors = bwColors;
	int i = 0;
	for (int j = 0; j < 4; j++)
		{
		int cidx = j * 3;
		g.setColor(colors[cidx], colors[cidx + 1], colors[cidx + 2]);
		for (int k = 0; k < 6; k++)
			{
			int x1 = x + coords[i++];
			int y1 = y + coords[i++];
			int x2 = x + coords[i++];
			int y2 = y + coords[i++];
			g.drawLine(x1, y1, x2, y2);
			}		
		}

	if (checked)
		{
		g.setColor(0, 0, 0);
		g.fillRect(x + 5, y + 4, 2, 4);
		g.drawLine(x + 4, y + 5, x + 4, y + 6);
		g.drawLine(x + 7, y + 5, x + 7, y + 6);
		}
	}

/** Called by the system to draw the radio control. */
public void onPaint(Graphics g)
	{
	int y = (this.height - 12) / 2;
	drawRadio(g, checked, 0, y);

	// draw label
	g.setColor(0, 0, 0);
	g.setFont(font);
	FontMetrics fm = getFontMetrics(font);
	int fontHeight = fm.getHeight();
	y = (this.height - fontHeight) / 2;
	g.drawText(text, 16, y);
	}
}
