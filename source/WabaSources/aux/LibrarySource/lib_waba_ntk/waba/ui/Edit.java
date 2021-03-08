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
 * Edit is a text entry control.
 * <p>
 * Here is an example showing an edit control being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * Edit edit;
 *
 * public void onStart()
 *  {
 *  edit = new Edit();
 *  edit.setRect(10, 10, 80, 30);
 *  add(edit);
 *  }
 * </pre>
 */

public class Edit extends Control
{
Font font;
Graphics drawg; // only valid while the edit has focus
Timer blinkTimer; // only valid while the edit has focus

//NOTE: later these  should be bitflags in a state int in the Control class
boolean hasFocus = false;
boolean cursorShowing = false;

char chars[] = new char[4];
byte charWidths[] = new byte[4];
Rect clipRect; // allocated when used and reused in many cases (see code)
int totalCharWidth = 0;
int len = 0;

int insertPos;
int startSelectPos;
int xOffset;

public Edit()
	{
	this.font = MainWindow.defaultFont;
	clearPosState();
	}

private void clearPosState()
	{
	insertPos = 0;
	startSelectPos = -1;
	if (Vm.isColor())
		xOffset = 4;
	else
		xOffset = 1;
	}

private int XToCharPos(int x)
	{
	int cx = xOffset;
	for (int i = 0; i < len; i++)
		{
		int cw = charWidths[i];
		if (x <= cx + (cw / 2))
			return i;
		cx += cw;
		}
	return len;
	}

private int CharPosToX(int n)
	{
	int cx = xOffset;
	if (n > len)
		n = len;
	for (int i = 0; i < n; i++)
		cx += charWidths[i];
	return cx;
	}

/**
 * Returns the text displayed in the edit control.
 */
public String getText()
	{
	return new String(chars, 0, len);
	}

/**
 * Sets the text displayed in the edit control.
 */
public void setText(String s)
	{
	chars = s.toCharArray();
	len = chars.length;
	charWidths = null;
	totalCharWidth = 0;
	clearPosState();
	repaint();
	}

private void draw(Graphics g, boolean cursorOnly)
	{
	g.setFont(font);
	FontMetrics fm = getFontMetrics(font);
	if (charWidths == null)
		{
		charWidths = new byte[len];
		totalCharWidth = 0;
		for (int i = 0; i < len; i++)
			{
			int charWidth = fm.getCharWidth(chars[i]);
			charWidths[i] = (byte)charWidth;
			totalCharWidth += charWidth;
			}
		}
	int height = fm.getHeight();
	int xMin;
	int xMax;
	int y;
	if (Vm.isColor())
		{
		xMin = 4;
		xMax = this.width - 4 - 1;
		y = this.height - height - 3;
		}
	else
		{
		xMin = 1;
		xMax = this.width - 1 - 1;
		y = this.height - height - 1;
		}
	if (clipRect == null)
		clipRect = new Rect(0, 0, 0, 0);

	// get current clip rect and intersect with edit rect to set
	// a new clip to draw in
	int cx1 = xMin;
	int cy1 = y;
	int cx2 = xMax;
	int cy2 = y + height - 1;
	Rect clip = g.getClip(clipRect);
	if (clip != null)
		{
		// intersect current clip rect and edit rect
		if (cx1 < clip.x)
			cx1 = clip.x;
		if (cy1 < clip.y)
			cy1 = clip.y;
		int clipX2 = clip.x + clip.width - 1;
		if (cx2 > clipX2)
			cx2 = clipX2;
		int clipY2 = clip.y + clip.height - 1;
		if (cy2 > clipY2)
			cy2 = clipY2;
		}
	g.setClip(cx1, cy1, cx2 - cx1 + 1, cy2 - cy1 + 1);

	int x = xOffset;
	if (cursorOnly)
		;
	else if (startSelectPos == -1)
		{
		// draw unselected chars
		g.setColor(255, 255, 255);
		g.fillRect(xMin, y, xMax - xMin + 1, height);
		g.setColor(0, 0, 0);
		g.drawText(chars, 0, len, x, y);
		}
	else
		{
		// character regions are:
		// 0 to (sel1-1) .. sel1 to (sel2-1) .. sel2 to last_char
		int sel1 = startSelectPos;
		int sel2 = insertPos;
		if (sel1 > sel2)
			{
			int temp = sel1;
			sel1 = sel2;
			sel2 = temp;
			}
		int sel1X = CharPosToX(sel1);
		int sel2X = CharPosToX(sel2);

		// 0 to (sel1-1) .. sel2 to last_char
		g.setColor(255, 255, 255);
		g.fillRect(xMin, y, sel1X - xMin, height);
		g.fillRect(sel2X, y, xMax - sel2X + 1, height);
		g.setColor(0, 0, 0);
		g.drawText(chars, 0, sel1, x, y);
		g.drawText(chars, sel2, len - sel2, sel2X, y);

		// draw sel1 to (sel2-1)
		g.setColor(0, 0, 120);
		g.fillRect(sel1X, y, sel2X - sel1X, height);
		g.setColor(255, 255, 255);
		g.drawText(chars, sel1, sel2 - sel1, sel1X, y);
		}
	// restore clip rect
	if (clip == null)
		g.clearClip();
	else
		g.setClip(clip.x, clip.y, clip.width, clip.height);
	if (!cursorOnly)
		{
		// erase the space for the cursor at (xMin - 1)
		g.setColor(255, 255, 255);
		g.drawLine(xMin - 1, y, xMin - 1, y + height - 1);
		}

	if (hasFocus)
		{
		// draw cursor
		int cx = CharPosToX(insertPos) - 1;
		g.drawCursor(cx, y, 1, height);
		if (cursorOnly)
			cursorShowing = !cursorShowing;
		else
			cursorShowing = true;
		}
	else
		cursorShowing = false;
	}

/** Called by the system to pass events to the edit control. */
public void onEvent(Event event)
	{
	if (charWidths == null)
		return; // widths have not been initialized - not added to hierarchy
	boolean redraw = false;
	boolean extendSelect = false;
	boolean clearSelect = false;
	int newInsertPos = insertPos;
	switch (event.type)
		{
		case ControlEvent.TIMER:
			{
			draw(drawg, true);
			return;
			}
		case ControlEvent.FOCUS_IN:
			{
			drawg = createGraphics();
			hasFocus = true;
			redraw = true;
			blinkTimer = addTimer(350);
			break;
			}
		case ControlEvent.FOCUS_OUT:
			{
			hasFocus = false;
			clearPosState();
			newInsertPos = 0;
			redraw = true;
			removeTimer(blinkTimer);
			break;
			}
		case KeyEvent.KEY_PRESS:
			{
			KeyEvent ke = (KeyEvent)event;
			boolean isPrintable;
			if (ke.key < 65536 && (ke.modifiers & IKeys.ALT) == 0 &&
				(ke.modifiers & IKeys.CONTROL) == 0)
				isPrintable = true;
			else
				isPrintable = false;
			boolean isDelete = (ke.key == IKeys.DELETE);
			boolean isBackspace = (ke.key == IKeys.BACKSPACE);
			int del1 = -1;
			int del2 = -1;
			int sel1 = startSelectPos;
			int sel2 = insertPos;
			if (sel1 > sel2)
				{
				int temp = sel1;
				sel1 = sel2;
				sel2 = temp;
				}
			if (sel1 != -1 && (isPrintable || isDelete || isBackspace))
				{
				del1 = sel1;
				del2 = sel2 - 1;
				}
			else if (isDelete)
				{
				del1 = insertPos;
				del2 = insertPos;
				}
			else if (isBackspace)
				{
				del1 = insertPos - 1;
				del2 = insertPos - 1;
				}
			if (del1 >= 0 && del2 < len)
				{
				int deleteCount = del2 - del1 + 1;
				int numOnRight = len - del2 - 1;
				for (int i = del1; i <= del2; i++)
					totalCharWidth -= charWidths[i];
				if (numOnRight > 0)
					{
					Vm.copyArray(chars, del2 + 1, chars, del1, numOnRight);
					Vm.copyArray(charWidths, del2 + 1, charWidths, del1, numOnRight);
					}
				len -= deleteCount;
				newInsertPos = del1;
				redraw = true;
				clearSelect = true;
				}
			if (isPrintable)
				{
				// grow the array if required (grows by 8)
				if (len == chars.length)
					{
					char newChars[] = new char[len + 8];
					Vm.copyArray(chars, 0, newChars, 0, len);
					chars = newChars;
					byte newCharWidths[] = new byte[len + 8];
					Vm.copyArray(charWidths, 0, newCharWidths, 0, len);
					charWidths = newCharWidths;
					}
				char c = (char)ke.key;
				FontMetrics fm = getFontMetrics(font);
				int charWidth = fm.getCharWidth(c);
				if (newInsertPos != len)
					{
					int i = newInsertPos;
					int l = len - newInsertPos;
					Vm.copyArray(chars, i, chars, i + 1, l);
					Vm.copyArray(charWidths, i, charWidths, i + 1, l);
					}
				chars[newInsertPos] = c;
				charWidths[newInsertPos] = (byte)charWidth;
				len++;
				newInsertPos++;
				totalCharWidth += charWidth;
				redraw = true;
				clearSelect = true;
				}
			boolean isMove = false;
			switch (ke.key)
				{
				case IKeys.HOME:
				case IKeys.END:
				case IKeys.LEFT:
				case IKeys.RIGHT:
				case IKeys.UP:
				case IKeys.DOWN:
					{
					isMove = true;
					break;
					}
				}
			if (isMove)
				{
				if (ke.key == IKeys.HOME)
					newInsertPos = 0;
				else if (ke.key == IKeys.END)
					newInsertPos = len;
				else if (ke.key == IKeys.LEFT || ke.key == IKeys.UP)
					newInsertPos--;
				else if (ke.key == IKeys.RIGHT || ke.key == IKeys.DOWN)
					newInsertPos++;
				if (newInsertPos != insertPos)
					{
					if ((ke.modifiers & IKeys.SHIFT) > 0)
						extendSelect = true;
					else	
						clearSelect = true;
					}
				}
			break;
			}
		case PenEvent.PEN_DOWN:
			{
			PenEvent pe = (PenEvent)event;
			newInsertPos = XToCharPos(pe.x);
			if ((pe.modifiers & IKeys.SHIFT) > 0) // shift
				extendSelect = true;
			else
				clearSelect = true;
			break;
			}
		case PenEvent.PEN_DRAG:
			{
			PenEvent pe = (PenEvent)event;
			newInsertPos = XToCharPos(pe.x);
			if (newInsertPos != insertPos)
				extendSelect = true;
			break;
			}
		default:
			return;
		}
	if (extendSelect)
		{
		if (startSelectPos == -1)
			startSelectPos = insertPos;
		else if (newInsertPos == startSelectPos)
			startSelectPos = -1;
		redraw = true;
		}
	if (clearSelect && startSelectPos != -1)
		{
		startSelectPos = -1;
		redraw = true;
		}
	if (newInsertPos > len)
		newInsertPos = len;
	if (newInsertPos < 0)
		newInsertPos = 0;
	Graphics g = drawg;
	boolean insertChanged = (newInsertPos != insertPos);
	if (insertChanged && !redraw && cursorShowing)
		draw(g, true); // erase cursor at old insert position
	if (insertChanged)
		{
		int xMin;
		int xMax;
		if (Vm.isColor())
			{
			xMin = 4;
			xMax = this.width - 4 - 1;
			}
		else
			{
			xMin = 1;
			xMax = this.width - 1 - 1;
			}
		int x = CharPosToX(newInsertPos);
		if (x - 5 < xMin)
			{
			// characters hidden on left - jump
			xOffset += (xMin - x) + 20;
			if (xOffset > xMin)
				xOffset = xMin;
			redraw = true;
			}
		if (x + 5 > xMax)
			{
			// characters hidden on right - jump
			xOffset -= (x - xMax) + 20;
			if (xOffset < xMax - totalCharWidth)
				xOffset = xMax - totalCharWidth;
			redraw = true;
			}
		if (totalCharWidth < xMax - xMin && xOffset != xMin)
			{
			xOffset = xMin;
			redraw = true;
			}
		}
	insertPos = newInsertPos;
	if (redraw)
		draw(g, false);
	else if (insertChanged)
		draw(g, true); // draw cursor at new insert position
	if (event.type == ControlEvent.FOCUS_OUT)
		{
		drawg = null;
		clipRect = null;
		}
	}

/** Called by the system to draw the edit control. */
public void onPaint(Graphics g)
	{
	int width = this.width;
	int height = this.height;
	int x2 = width - 1;
	int y2 = height - 1;

	if (Vm.isColor())
		{
		// top, left
		g.setColor(130, 130, 130);
		g.drawLine(0, 0, x2 - 1, 0);
		g.drawLine(0, 0, 0, y2 - 1);

		// top, left shadow
		g.setColor(0, 0, 0);
		g.drawLine(1, 1, x2 - 2, 1);
		g.drawLine(1, 1, 1, y2 - 2);

		// bottom, right
		g.setColor(255, 255, 255);
		g.drawLine(0, y2, x2, y2);
		g.drawLine(x2, y2, x2, 0);

		g.setColor(255, 255, 255);
		g.fillRect(2, 2, width - 4, height - 4);
		}

	else
		{
		g.setColor(0, 0, 0);
		g.drawDots(0, y2, x2, y2);
		}
	draw(g, false);
	clipRect = null;
	}
}
