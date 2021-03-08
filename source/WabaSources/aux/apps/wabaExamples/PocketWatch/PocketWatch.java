/*

PocketWatch.java

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
 * A program that draws an animated pocket watch.
 */

public class PocketWatch extends MainWindow
{
Graphics drawg;
Timer timer;
Image watchImage;
int watchX, watchY;
Image bufferImage;
Graphics bufferG;

public PocketWatch()
	{
	// create drawing objects
	drawg = new Graphics(this);

	// load watch image and create image buffer
	watchImage = new Image("watch.bmp");
	int watchWidth = watchImage.getWidth();
	int watchHeight = watchImage.getHeight();
	watchX = (this.width - watchWidth) / 2;
	watchY = (this.height - watchHeight) / 2;
	bufferImage = new Image(watchWidth, watchHeight);
	bufferG = new Graphics(bufferImage);

	if (timer == null)
		{
		// update every second
		timer = addTimer(1000);
		}
	}

static float sin[] =
  { 
  0.0f, 0.17364818f, 0.34202015f, 0.5f,
  0.64278764f, 0.76604444f, 0.8660254f, 0.9396926f,
  0.9848077f, 1.0f, 0.9848077f, 0.9396926f,
  0.8660254f, 0.76604444f, 0.64278764f, 0.5f,
  0.34202015f, 0.17364818f, 0.0f, -0.17364818f,
  -0.34202015f, -0.5f, -0.64278764f, -0.76604444f,
  -0.8660254f, -0.9396926f, -0.9848077f, -1.0f,
  -0.9848077f, -0.9396926f, -0.8660254f, -0.76604444f,
  -0.64278764f, -0.5f, -0.34202015f, -0.17364818f,
  0.0f
  };

/**
 * Calculates the sin of a value in degrees. The value passed
 * must be in the range of 0...360
 * The degrees walk clockwise instead of counterclockwise to
 * match the clock.
 */
static float qsin(int deg)
	{
	// get the nearest sin we have in the table
	int d10 = deg / 10;
	float low = sin[d10];
	int weight = deg % 10;
	if (weight == 0)
		return low;

	// we don't have an exact match so calculate a weighted
	// average of the low nearest sin and high nearest sin
	float high = sin[d10 + 1];
	float wf = (float)weight;
	return (low * (10f - wf) + high * wf) / 10.0f;
	}

/**
 * Calculates the cos of a value in degrees. The degrees walk
 * clockwise instead of counterclockwise to match the clock.
 */
static float qcos(int deg)
	{
	return qsin((deg + 90) % 360);
	}

/** Draws a clock hand. */
static void drawHand(Graphics g, int x, int y, int w, int h,
	int deg, boolean doTriangle)
	{
	// calculate cx, cy (center of box)
	int cx = x + w / 2;
	int cy = y + h / 2;

	// calculate width and height factors
	float hw = (float)((float)w / 2.0f);
	float hh = (float)((float)h / 2.0f);

	// calculate x2, y2
	int x2 = x + (int)(hw + qcos(deg) * hw + .5f);
	int y2 = y + (int)(hh + qsin(deg) * hh + .5f);

	if (!doTriangle)
		{
		g.drawLine(cx, cy, x2, y2);
		return;
		}
	// calc x1, y1 for triangle
	deg = (deg + 90) % 360;
	int x1 = x + (int)(hw + qcos(deg) * 1.75f + .5f);
	int y1 = y + (int)(hh + qsin(deg) * 1.75f + .5f);

	// calc x3, y3 for triangle
	deg = (deg + 180) % 360;
	int x3 = x + (int)(hw + qcos(deg) * 1.75f + .5f);
	int y3 = y + (int)(hh + qsin(deg) * 1.75f + .5f);

	// draw outer triangle
	g.drawLine(x1, y1, x2, y2);
	g.drawLine(x2, y2, x3, y3);
	g.drawLine(x3, y3, x1, y1);

	// fill triangle in a bit to make the hand darker
	g.drawLine((x1 + x3) / 2, (y1 + y3) / 2, x2, y2);
	g.drawLine((x1 * 3 + x3) / 4, (y1 * 3 + y3) / 4, x2, y2);
	g.drawLine((x1 + x3 * 3) / 4, (y1 + y3 * 3) / 4, x2, y2);
	}

public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		exit(0);
	else if (event.type == ControlEvent.TIMER)
		updateClock(drawg);
	}

void updateClock(Graphics g)
	{
	Time time = new Time();
	bufferG.drawImage(watchImage, 0, 0);
	bufferG.setColor(0, 0, 0);

	// 63, 95 is the center of the watch dial

	// draw hour hand
	int x = 63 - 28;
	int y = 95 - 28;
	int w = 56;
	int h = 56;
	int deg = (((time.hour + 9)% 12) * 30) + (time.minute / 2);
	drawHand(bufferG, x, y, w, h, deg, true);

	// draw minute hand
	x = 63 - 40;
	y = 95 - 40;
	w = 80;
	h = 80;
	deg = ((time.minute + 45) % 60) * 6 + (time.second / 10);
	drawHand(bufferG, x, y, w, h, deg, true);

	// draw second hand
	x = 63 - 10;
	y = 121 - 10;
	w = 20;
	h = 20;
	deg = ((time.second + 45) % 60) * 6;
	drawHand(bufferG, x, y, w, h, deg, false);

	// draw dot in center
	bufferG.setColor(255, 255, 255);
	bufferG.fillRect(63, 95, 1, 1);

	// copy buffer to screen
	g.drawImage(bufferImage, watchX, watchY);
	}

public void onPaint(Graphics g)
	{
	// draw background in white
	g.setColor(255, 255, 255);
	g.fillRect(0, 0, this.width, this.height);

	updateClock(g);
	}
}
