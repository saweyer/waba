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
import waba.sys.*;

/**
 * Welcome is the welcome application.
 * <p>
 * This is the default program run when none is specified or when the VM needs
 * a program to run to show that the VM is functioning on a device.
 */

public class Welcome extends MainWindow
{
Font boldFont;
Font plainFont;
String version = "version ";

/** Constructs the welcome application. */
public Welcome()
	{
	boldFont = new Font("Helvetica", Font.BOLD, 12);
	plainFont = new Font("Helvetica", Font.PLAIN, 12);
	int v = Vm.getVersion();
	version += (v / 100) + "." + (v % 100) + " for " + Vm.getPlatform();
	}

/** Called by the system to pass events to the application. */
public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		exit(0);
	}

/** Called by the system to draw the application. */
public void onPaint(Graphics g)
	{
	int x = 0;
	int y = 0;

	FontMetrics boldFontMetrics = getFontMetrics(boldFont);
	FontMetrics plainFontMetrics = getFontMetrics(plainFont);

	// draw title
	String title = "Waba Virtual Machine";
	x = (this.width - boldFontMetrics.getTextWidth(title)) / 2;
	y = this.height / 2 - 60;
	g.setColor(0, 0, 0);
	g.setFont(boldFont);	
	g.drawText(title, x, y);
	y += boldFontMetrics.getHeight();

	// draw verion
	x = (this.width - plainFontMetrics.getTextWidth(version)) / 2;
	g.setColor(0, 0, 0);
	g.setFont(plainFont);
	g.drawText(version, x, y);

	// draw status
	String status = "WabaVM installed and ready";
	y += 40;
	x = (this.width - plainFontMetrics.getTextWidth(status)) / 2;
	g.drawText(status, x, y);

	// draw url
	String url = "http://www.wabasoft.com";
	y += 50;
	int sw = plainFontMetrics.getTextWidth(url);
	x = (this.width - sw) / 2;
	g.setColor(0, 0, 0);
	int sh = plainFontMetrics.getHeight();
	g.fillRect(x - 2, y, sw + 4, sh);
	g.setColor(255, 255, 255);
	g.setFont(plainFont);
	g.drawText(url, x, y);
	}
}