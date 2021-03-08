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

/**
 * Label is a text label control.
 * <p>
 * Here is an example showing a label being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onStart()
 *  {
 *  Label label = new Label("Value:");
 *  label.setRect(10, 10, 80, 30);
 *  add(label);
 *  }
 * </pre>
 */

public class Label extends Control
{
/** Constant for left alignment. */
public static final int LEFT = 0;
/** Constant for center alignment. */
public static final int CENTER = 1;
/** Constant for right alignment. */
public static final int RIGHT = 2;

String text;
Font font;
int align;

/** Creates a label displaying the given text with left alignment. */
public Label(String text)
	{
	this(text, LEFT);
	}

/**
 * Creates a label displaying the given text with the given alignment.
 * @param text the text displayed
 * @param align the alignment
 * @see #LEFT
 * @see #RIGHT
 * @see #CENTER
 */
public Label(String text, int align)
	{
	this.text = text;
	this.align = align;
	this.font = MainWindow.defaultFont;
	}

/** Sets the text that is displayed in the label. */
public void setText(String text)
	{
	this.text = text;
	repaint();
	}

/** Gets the text that is displayed in the label. */
public String getText()
	{
	return text;
	}

/** Called by the system to draw the button. */
public void onPaint(Graphics g)
	{
	// draw label
	g.setColor(0, 0, 0);
	g.setFont(font);
	FontMetrics fm = getFontMetrics(font);
	int x = 0;
	int y = (this.height - fm.getHeight()) / 2;
	if (align == CENTER)
		x = (this.width - fm.getTextWidth(text)) / 2;
	else if (align == RIGHT)
		x = this.width - fm.getTextWidth(text);
	g.drawText(text, x, y);
	}
}
