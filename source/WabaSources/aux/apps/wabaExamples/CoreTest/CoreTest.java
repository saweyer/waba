/*

CoreTest.java

Copyright (c) 1998, 1999 Wabasoft 

Wabasoft grants you a non-exclusive license to use, modify and re-distribute
this program provided that this copyright notice and license appear on all
copies of the software.

Software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR
IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
HEREBY EXCLUDED. THE ENTIRE RISK ARISING OUT OF USING THE SOFTWARE IS ASSUMED
BY THE LICENSEE. 

WABASOFT AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING SOFTWARE.
IN NO EVENT WILL WABASOFT OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL
OR PUNITIVE DAMAGES, HOWEVER CAUSED AN REGARDLESS OF THE THEORY OF LIABILITY,
ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF WABASOFT HAS
BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

*/

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;

/**
 * This program tests some of the core functionality of the waba.fx
 * and waba.sys packages.
 */

public class CoreTest extends MainWindow
{
Graphics drawg = new Graphics(this);
Font boldFont = new Font("Helvetica", Font.BOLD, 12);
Font plainFont = new Font("Helvetica", Font.PLAIN, 12);
int page = 1;

// for the animated image
Image image = new Image(20, 20);
Image wabaImage, setImage;
boolean inErase = false;
int imageX, imageY, maxImageX, maxImageY, stepX, stepY;
Timer timer;

static final int GRID_PAGE    = 1;
static final int LINES_PAGE   = 2;
static final int METRICS_PAGE = 3;
static final int POLYGON_PAGE = 4;
static final int COPY_PAGE    = 5;
static final int DRAWOP_PAGE  = 6;
static final int TIME_PAGE    = 7;
static final int CONVERT_PAGE = 8;
static final int IMAGE_PAGE   = 9;
static final int SOUND_PAGE   = 10;
static final int DONE_PAGE    = 11;

public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		{
		// turn off any timer set by another page
		if (timer != null)
			removeTimer(timer);
		page++;
		if (page == DONE_PAGE + 1)
			exit(0);
		if (page == IMAGE_PAGE)
			initImage();
		repaint();
		if (page == TIME_PAGE)
			timer = addTimer(1000);
		if (page == IMAGE_PAGE)
			timer = addTimer(10);
		}
	else if (event.type == ControlEvent.TIMER)
		{
		if (page == IMAGE_PAGE)
			{
			// animate twice each frame
			animateImage();
			animateImage();
			}
		else if (page == TIME_PAGE)
			drawTimeBox(drawg);
		}	
	}

private void drawTestBox(Graphics g, String name)
	{
	int screenWidth = this.width;
	int screenHeight = this.height;

	// draw black outline box
	int w = 100;
	int h = 50;
	int x = (screenWidth - w) / 2;
	int y = (screenHeight - h) / 2;
	g.setColor(0, 0, 0);
	g.fillRect(x, y, w, h);
	x += 2;
	y += 2;
	w -= 4;
	h -= 4;
	g.setColor(255, 255, 255);
	g.fillRect(x, y, w, h);

	// draw test number (page number) centered
	FontMetrics fm = new FontMetrics(plainFont, this);
	g.setColor(0, 0, 0);
	String testNum = "Test #" + page;
	int tx = x + (w - fm.getTextWidth(testNum)) / 2;
	g.setFont(plainFont);
	g.drawText(testNum, tx, y);

	// draw name centered
	tx = x + (w - fm.getTextWidth(name)) / 2;
	g.drawText(name, tx, y + 20);
	}

private void drawGrid(Graphics g)
	{
	int screenWidth = this.width;
	int screenHeight = this.height;

	g.setColor(0, 0, 0);
	int nHoriz = screenWidth / 10;
	for (int i = 0; i < nHoriz; i += 2)
		{
		g.drawText(Convert.toString(i * 10), i * 10, 0);
		g.drawLine(i * 10, 0, i * 10, screenHeight - 1);
		}
	g.drawLine(screenWidth - 1, 0, screenWidth - 1, screenHeight - 1);
	int nVert = screenHeight / 10;
	for (int i = 0; i < nVert; i += 2)
		{
		g.drawText(Convert.toString(i * 10), 0, i * 10);
		g.drawLine(0, i * 10, screenWidth - 1, i * 10);
		}
	g.drawLine(0, screenHeight - 1, screenWidth - 1, screenHeight - 1);
	drawTestBox(g, "Coordinates");
	}

private void drawLinesTextColors(Graphics g)
	{
	// draw box
	g.setColor(200, 200, 200);
	g.fillRect(0, 0, 40, 40);

	// draw outline
	g.setColor(0, 0, 0);
	g.drawRect(0, 0, 40, 40);

	// draw red and blue X (one pixel in from sides of box)
	g.setColor(255, 0, 0);
	g.drawLine(1, 1, 38, 38);
	g.setColor(0, 0, 255);
	g.drawLine(1, 38, 38, 1);

	// draw text
	g.setColor(0, 40, 0);
	g.drawText("This is some plain text", 40, 0);
	g.setFont(boldFont);

	String s = "is some bold text";
	char chars[] = s.toCharArray();
	String s2 = "This " + new String(chars);
	g.drawText(s2, 40, 15);

	s = "These are some chars";
	chars = s.toCharArray();
	g.drawText(chars, 0, chars.length, 40, 30);

	chars = new char[100];
	for (int i = 0; i < 100; i++)
		chars[i] = (char)((i % 10) + '0');
	g.drawText(chars, 0, chars.length, 40, 45);

	drawTestBox(g, "Lines, Text, Colors");
	}

private void drawFontMetrics(Graphics g)
	{
	// draw string with metrics
	String s = "String missing n";
	g.setFont(boldFont);
	g.setColor(0, 0, 0);
	g.drawText(s, 0, 0);
	FontMetrics fm = new FontMetrics(boldFont, this);
	int strWidth = fm.getTextWidth(s);
	g.setColor(0, 120, 0);
	int yoff = fm.getLeading();
	g.drawLine(0, yoff, strWidth, yoff);
	yoff += fm.getAscent();
	g.drawLine(0, yoff, strWidth, yoff);
	yoff += fm.getDescent();
	g.drawLine(0, yoff, strWidth, yoff);
	// erase n from string
	String sn = "String missi";
	strWidth = fm.getTextWidth(sn);
	g.setColor(255, 255, 255);
	int xoff = strWidth;
	g.drawText("n", xoff, 0);

	// draw line across char width of n
	int charWidth = fm.getCharWidth('n');
	g.setColor(0, 0, 0);
	g.drawLine(xoff, 3, xoff + charWidth, 3);
	drawTestBox(g, "Font Metrics");
	}

private void drawPolygons(Graphics g)
	{
	g.setColor(0, 0, 0);

	// outline triangle
	g.drawRect(10, 0, 40, 40);
	int x[] = new int[5];
	int y[] = new int[5];
	x[0] = 15; y[0] = 5;
	x[1] = 45; y[1] = 5;
	x[2] = 15; y[2] = 35;
	g.drawPolygon(x, y, 3);

	// outline star
	g.drawRect(60, 0, 40, 40);
	x[0] = 80; y[0] = 5;
	x[1] = 65; y[1] = 35;
	x[2] = 95; y[2] = 15;
	x[3] = 65; y[3] = 15;
	x[4] = 95; y[4] = 35;
	g.drawPolygon(x, y, 5);

	// outline square with triangle removed
	g.drawRect(110, 0, 40, 40);
	x[0] = 115; y[0] = 35;
	x[1] = 115; y[1] = 5;
	x[2] = 145; y[2] = 5;
	x[3] = 145; y[3] = 35;
	x[4] = 130; y[4] = 15;
	g.drawPolygon(x, y, 5);

	int by = height - 40;

	// filled triangle
	g.drawRect(10, by, 40, 40);
	x[0] = 15; y[0] = by + 5;
	x[1] = 45; y[1] = by + 5;
	x[2] = 15; y[2] = by + 35;
	g.fillPolygon(x, y, 3);

	// filled star
	g.drawRect(60, by, 40, 40);
	x[0] = 80; y[0] = by + 5;
	x[1] = 65; y[1] = by + 35;
	x[2] = 95; y[2] = by + 15;
	x[3] = 65; y[3] = by + 15;
	x[4] = 95; y[4] = by + 35;
	g.fillPolygon(x, y, 5);

	// filled square with triangle removed
	g.drawRect(110, by, 40, 40);
	x[0] = 115; y[0] = by + 35;
	x[1] = 115; y[1] = by + 5;
	x[2] = 145; y[2] = by + 5;
	x[3] = 145; y[3] = by + 35;
	x[4] = 130; y[4] = by + 15;
	g.fillPolygon(x, y, 5);

	drawTestBox(g, "Polygons");
	}

private void drawCopyAndClip(Graphics g)
	{
	// draw rect in 0, 20 box (indent 2 pixels on each side)
	// and draw white rect inside that
	g.setColor(0, 0, 0);
	g.fillRect(2, 2, 17, 17);
	g.setColor(255, 255, 255);
	g.fillRect(5, 5, 11, 11);

	// copy previous rectange to next box
	g.copyRect(this, 0, 0, 20, 20, 20, 0);

	// Draw same box as above but with x with line under it
	// utilizing clipping
	g.setClip(42, 2, 17, 17);
	g.setColor(0, 0, 0);
	g.fillRect(0, 0, 180, 180);
	g.setClip(45, 5, 11, 11);
	g.setColor(255, 255, 255);
	g.fillRect(0, 0, 180, 180);
	g.setColor(0, 0, 0);
	g.drawText("xxxx", 40, 0);
	g.drawLine(0, 14, 190, 14);
	g.clearClip();

	// draw with using some raster operations
	g.setDrawOp(Graphics.DRAW_OVER);
	g.setColor(0, 0, 0);
	g.drawLine(64, 2, 64, 18); 
	g.drawLine(62, 2, 66, 2);
	g.drawLine(62, 18, 66, 18);
	g.setDrawOp(Graphics.DRAW_XOR);
	g.drawLine(62, 8, 66, 8);
	g.setDrawOp(Graphics.DRAW_AND);
	g.drawLine(62, 14, 66, 14);
	g.setDrawOp(Graphics.DRAW_OVER);

	drawTestBox(g, "Copy & Clip");
	}

private void drawDrawOps(Graphics g)
	{
	// draw a 10 pixel box in the center of the 20x20 image
	Graphics ig = new Graphics(image);
	ig.setColor(255, 255, 255);
	ig.fillRect(0, 0, 20, 20);
	ig.setColor(0, 0, 0);
	ig.fillRect(5, 0, 10, 20);

	for (int i = 0; i < 4; i++)
		{
		int x = i * 25;
		int y = 5;
		// fill left half of box with line over top
		g.setColor(0, 0, 0);
		g.fillRect(x, y, 10, 20);
		g.drawLine(x, y - 1, x + 20, y - 1);
		// draw left half of line
		g.drawLine(x, y + 25, x + 10, y + 25);
		// copy the image with the 4 operations
		switch (i)
			{
			case 0: g.setDrawOp(Graphics.DRAW_OVER); break;
			case 1: g.setDrawOp(Graphics.DRAW_AND); break;
			case 2: g.setDrawOp(Graphics.DRAW_OR); break;
			case 3: g.setDrawOp(Graphics.DRAW_XOR); break;
			}
		g.drawImage(image, x, y);
		g.setColor(255, 255, 255);
		g.drawLine(x, y + 25, x + 4, y + 25);
		g.drawLine(x + 15, y + 25, x + 19, y + 25);
		g.setColor(0, 0, 0);
		g.drawLine(x + 5, y + 25, x + 14, y + 25);
		g.setDrawOp(Graphics.DRAW_OVER);
		}
	
	drawTestBox(g, "Draw Operations");
	}

private void drawTimeBox(Graphics g)
	{
	g.setColor(255, 255, 255);
	g.fillRect(0, 0, this.width, 50);

	g.setFont(plainFont);
	g.setColor(0, 0, 0);
	Time time = new Time();
	g.drawText("Date: " + time.month + "/" + time.day +
		"/" + time.year, 0, 0);
	g.drawText("Time: " + time.hour + ":" + time.minute +
		":" + time.second, 0, 15);
	g.drawText("Stamp: " + Vm.getTimeStamp(), 0, 30);
	}

private void drawTime(Graphics g)
	{
	// timer will call drawTimeBox() to keep time current
	drawTimeBox(g);
	drawTestBox(g, "Time & Timer");
	}

private void drawConvert(Graphics g)
	{
	g.setColor(0, 0, 0);
	g.drawText("10 = " + Convert.toInt("10"), 0, 0);
	g.drawText("1.0123456 = " + Convert.toString(1.0123456f), 0, 20);

	drawTestBox(g, "Convert");
	}

private void initImage()
	{
	// defaults for the animated image
	imageX = 0;
	imageY = 0;
	maxImageX = this.width - 20;
	maxImageY = this.height - 20;
	stepX = 8;
	stepY = 0;

	// draw into the animated image
	Graphics ig = new Graphics(image);
	ig.setColor(100, 0, 0);
	ig.fillRect(0, 0, 20, 20);
	ig.setColor(255, 255, 255);
	ig.fillRect(4, 4, 12, 12);
	ig.setColor(0, 0, 0);
	ig.fillRect(6, 6, 8, 8);

	setImage = new Image(10, 10);
	int bitsPerPixel = 1;
	int colorMap[] = new int[2];
	colorMap[0] = 0xFFFFFF; // white
	colorMap[1] = 0x0000FF; // blue
	int bytesPerRow = 2;
	int numRows = 2;
	byte pixels[] = new byte[bytesPerRow * numRows];
	for (int i = 0; i < pixels.length; i++)
		pixels[i] = (byte)0xAA;
	for (int y = 0; y < 10; y += numRows)
		setImage.setPixels(bitsPerPixel, colorMap,
			bytesPerRow, numRows, y, pixels);

	wabaImage = new Image("test.bmp");
	}

private void animateImage()
	{
	imageX += stepX;
	imageY += stepY;
	if (imageX < 0)
		{
		stepY = -8;
		stepX = 0;
		imageX = 0;
		}
	else if (imageX > maxImageX)
		{
		stepY = 8;
		stepX = 0;
		imageX = maxImageX;
		}
	if (imageY < 0)
		{
		stepX = 8;
		stepY = 0;
		imageY = 0;
		inErase = !inErase;
		}
	else if (imageY > maxImageY)
		{
		stepX = -8;
		stepY = 0;
		imageY = maxImageY;
		}
	if (!inErase)
		drawg.drawImage(image, imageX, imageY);
	else
		{
		drawg.setColor(255, 255, 255);
		drawg.fillRect(imageX, imageY, 20, 20);
		}
	}

private void drawImageAndTimer(Graphics g)
	{
	// timer takes care of drawing the animated image
	if (wabaImage != null)
		g.drawImage(wabaImage, 20, 20);
	g.drawImage(setImage, 40, 40);
	drawTestBox(g, "Image & Timer");
	}

private void drawSound(Graphics g)
	{
	drawTestBox(g, "Sound");
	Sound.beep();
	for (int i = 0; i < 24; i++)
		Sound.tone(2500 - (i * 100), 15);
	for (int i = 0; i < 25; i++)
		Sound.tone(40 + i % 13, 40);
	Sound.tone(2500, 100);
	Sound.tone(500, 150);
	Sound.tone(2500, 100);
	}

private void drawDone(Graphics g)
	{
	drawTestBox(g, "Done");
	}

public void onPaint(Graphics g)
	{
	switch (page)
		{
		case GRID_PAGE: drawGrid(g); break;
		case LINES_PAGE: drawLinesTextColors(g); break;
		case METRICS_PAGE: drawFontMetrics(g); break;
		case POLYGON_PAGE: drawPolygons(g); break;
		case COPY_PAGE: drawCopyAndClip(g); break;
		case DRAWOP_PAGE: drawDrawOps(g); break;
		case TIME_PAGE: drawTime(g); break;
		case CONVERT_PAGE: drawConvert(g); break;
		case IMAGE_PAGE: drawImageAndTimer(g); break;
		case SOUND_PAGE: drawSound(g); break;
		case DONE_PAGE: drawDone(g); break;
		}
	}
}
