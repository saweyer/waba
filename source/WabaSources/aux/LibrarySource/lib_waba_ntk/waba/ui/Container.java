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
 * Container is a control that contains child controls.
 */

public class Container extends Control
{
/** The children of the container. */
protected Control children;
/** The tail of the children list. */
protected Control tail;

/**
 * Adds a child control to this container.
 */
public void add(Control control)
	{
	if (control.parent != null)
		control.parent.remove(control);
	// set children, next, prev, tail and parent
	control.next = null;
	if (children == null)
		children = control;
	else
		tail.next = control;
	control.prev = tail;
	tail = control;
	control.parent = this;
	// numChildren++;
	control.repaint();
	}

/**
 * Removes a child control from the container.
 */
public void remove(Control control)
	{
	if (control.parent != this)
		return;
	// set children, next, prev, tail and parent
	Control prev = control.prev;
	Control next = control.next;
	if (prev == null)
		children = next;
	else
		prev.next = next;
	if (next != null)
		next.prev = prev;
	if (tail == control)
		tail = prev;
	control.next = null;
	control.prev = null;
	// numChildren--;
	control.repaint();
	control.parent = null;
	}

/** Returns the child located at the given x and y coordinates. */
public Control findChild(int x, int y)
	{
	Container container;
	Control child;

	container = this;
	while (true)
		{
		// search tail to head since paint goes head to tail
		child = container.tail;
		while (child != null && !child.contains(x, y))
			child = child.prev;
		if (child == null)
			return container;
		if (!(child instanceof Container))
			return child;
		x -= child.x;
		y -= child.y;
		container = (Container)child;
		}
	}

/** Called by the system to draw the children of the container. */
public void paintChildren(Graphics g, int x, int y, int width, int height)
	{
	Control child = children;
	while (child != null)
		{
		int x1 = x;
		int y1 = y;
		int x2 = x + width - 1;
		int y2 = y + height - 1;
		int cx1 = child.x;
		int cy1 = child.y;
		int cx2 = cx1 + child.width - 1;
		int cy2 = cy1 + child.height - 1;
		// trivial clip
		if (x2 < cx1 || x1 > cx2 || y2 < cy1 || y1 > cy2)
			{
			child = child.next;
			continue;
			}
		if (x1 < cx1)
			x1 = cx1;
		if (y1 < cy1)
			y1 = cy1;
		if (x2 > cx2)
			x2 = cx2;
		if (y2 > cy2)
			y2 = cy2;
		g.setClip(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
		g.translate(cx1, cy1);
		child.onPaint(g);
		g.clearClip();
		if (child instanceof Container)
			{
			Container c = (Container)child;
			c.paintChildren(g, x1 - cx1, y1 - cy1, x2 - x1 + 1, y2 - y1 + 1);
			}
		g.translate(- cx1, - cy1);
		child = child.next;
		}
	}
}
