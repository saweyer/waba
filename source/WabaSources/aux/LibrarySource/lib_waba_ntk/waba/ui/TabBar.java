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
 * TabBar is a bar of tabs.
 * <p>
 * Here is an example showing a tab bar being used:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * Tab tabOne;
 * Tab tabTwo;
 *
 * public void onStart()
 *  {
 *  TabBar tabBarar = new TabBar();
 *  tabOne = new Tab("One");
 *  tabBar.add(tabOne);
 *  tabTwo = new Tab("Two");
 *  tabBar.add(tabTwo);
 *  tabBar.setRect(10, 10, 80, 30);
 *  add(tabBar);
 *  }
 *
 * public void onEvent(Event event)
 *  {
 *  if (event.type == ControlEvent.PRESSED &&
 *      event.target == tabOne)
 *   {
 *   ... handle tab one being pressed
 * </pre>
 */

public class TabBar extends Container
{
Font font;
Tab activeTab;

/** Constructs a tab bar control. */
public TabBar()
	{
	this.font = MainWindow.defaultFont;
	}

/** Adds a tab to the tab bar.*/
public void add(Control control)
	{
	if (activeTab == null)
		activeTab = (Tab)control;
	super.add(control);
	}

private void drawTab(Graphics g, boolean hasLeft, boolean hasRight,
	int x1, int y1, int x2, int y2)
	{
	boolean isColor = Vm.isColor();

	if (isColor)
		g.setColor(255, 255, 255);
	// left side and hatch dot
	if (hasLeft)
		{
		g.drawLine(x1, y2 - 1, x1, y1 + 2);
		g.drawLine(x1 + 1, y1 + 1, x1 + 1, y1 + 1);
		}

	// top line
	g.drawLine(x1 + 2, y1, x2 - 2, y1);

	// right hatch dot and side
	if (isColor)
		g.setColor(0, 0, 0);
	if (hasRight)
		{
		g.drawLine(x2 - 1, y1 + 1, x2 - 1, y1 + 1);
		g.drawLine(x2, y1 + 2, x2, y2 - 1);
		if (isColor)
			{
			g.setColor(100, 100, 100);
			g.drawLine(x2 - 1, y1 + 2, x2 - 1, y2 - 1);
			}			
		}
	}

private Tab drawOrHitCheck(boolean draw, Graphics g, int hitX, int hitY)
	{
	boolean isColor = Vm.isColor();
	FontMetrics fm = getFontMetrics(font);
	int height = fm.getHeight() + 6;
	int y = this.height - height;
	int x = 2;
	Control child = children;
	Control prevChild = null;
	while (child != null)
		{
		Tab tab = (Tab)child;
		String label = tab.text;
		int width = fm.getTextWidth(label) + 6;

		int x1 = x;
		int x2 = x + width + 4;
		int y1 = y + 2;
		int y2 = y + height - 1;
		if (child == activeTab)
			{
			x1 -= 2;
			x2 += 2;
			y1 -= 2;
			y2 += 1;
			int by = this.height - 1;
			if (draw)
				{
				if (isColor)
					g.setColor(255, 255, 255);
				if (x1 != 0)
					g.drawLine(0, by, x1 - 1, by);
				if (x2 != this.width - 1)
					g.drawLine(x2 + 1, by, this.width - 1, by);
				}
			}

		if (draw)
			{
			g.setColor(0, 0, 0);
			g.drawText(label, x + 5, y1 + 3);
			}

		boolean hasLeft = true;
		boolean hasRight = true;
		if (prevChild == activeTab)
			hasLeft = false;
		else if (child.next == activeTab)
			hasRight = false;

		if (draw)
			drawTab(g, hasLeft, hasRight, x1, y1, x2, y2);
		else
			{
			if (!hasLeft)
				x1 += 2;
			else if (!hasRight)
				x2 -= 2;
			if (hitX >= x1 && hitX <= x2 && hitY >= y1 && hitY <= y2)
				return (Tab)child;
			}
		x += width + 4 + 1;
		prevChild = child;
		child = child.next;
		}
	return null;
	}

/**
 * Sets the currently active tab. A PRESSED event will be posted to
 * the given tab if it is not the currently active tab.
 */
public void setActiveTab(Tab tab)
	{
	if (tab != activeTab)
		{
		activeTab = tab;
		repaint();
		postEvent(new ControlEvent(ControlEvent.PRESSED, tab));
		}
	}

/** Called by the system to pass events to the tab bar control. */
public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		{
		PenEvent pe = (PenEvent)event;
		Tab tab = drawOrHitCheck(false, null, pe.x, pe.y);
		if (tab != null)
			setActiveTab(tab);
		}
	}

/** Called by the system to draw the tab bar. */
public void onPaint(Graphics g)
	{
	g.setFont(font);
	g.setColor(0, 0, 0);
	drawOrHitCheck(true, g, 0, 0);
	}
}
