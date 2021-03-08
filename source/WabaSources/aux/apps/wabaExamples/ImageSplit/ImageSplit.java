/*

ImageSplit.java

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

/**
 * A program that draws an image and then splits it into 4 pieces.
 */

public class ImageSplit extends MainWindow
{
Graphics drawg = new Graphics(this);
Timer timer;
Image takiImage = null;
int imageX, imageY, imageWidth, imageHeight;
boolean firstTime = true;
boolean inAnimation = false;
int step = 0;
int dir = 2;

public void onEvent(Event event)
	{
	if (event.type == PenEvent.PEN_DOWN)
		{
		// first click animates, second exits
		if (!inAnimation)
			{
			inAnimation = true;
			repaint(); // erases please wait...
			timer = addTimer(10);
			}
		else
			exit(0);
		}
	else if (event.type == ControlEvent.TIMER)
		animateImage(drawg);
	}

private void loadImage()
	{
	takiImage = new Image("taki.bmp");
	imageWidth = takiImage.getWidth();
	imageHeight = takiImage.getHeight();	
	imageX = (this.width - imageWidth) / 2;
	imageY = (this.height - imageHeight) / 2;
	}

public void animateImage(Graphics g)
	{
	int iw2 = imageWidth / 2;
	int ih2 = imageHeight / 2;
	g.setColor(255, 255, 255);
	// erase trail
	if (dir > 0)
		{
		g.fillRect(imageX + iw2 - step, imageY, 2, ih2 + 1);
		g.fillRect(imageX + iw2, imageY + ih2 - step, iw2 + 1, 2);
		g.fillRect(imageX, imageY + ih2 + step, iw2 + 1, 2);
		g.fillRect(imageX + iw2 + step, imageY + ih2, 2, ih2 + 1);
		}
	else
		{
		g.fillRect(imageX - step - 2, imageY, 2, ih2 + 1);
		g.fillRect(imageX + iw2, imageY - step, iw2 + 1, 2);
		g.fillRect(imageX, imageY + ih2 + + ih2 + step, iw2 + 1, 2);
		g.fillRect(imageX + iw2 + iw2 + step, imageY + ih2, 2, ih2 + 1);
		}
	step += dir;
	if (step == 26 || step == 0)
		dir = - (dir);
	// draw image in 4 parts
	g.copyRect(takiImage, 0, 0, iw2, ih2, imageX - step, imageY);
	g.copyRect(takiImage, iw2, 0, iw2, ih2, imageX + iw2, imageY - step);
	g.copyRect(takiImage, 0, ih2, iw2, ih2, imageX, imageY + ih2 + step);
	g.copyRect(takiImage, iw2, ih2, iw2, ih2, imageX + iw2 + step, imageY + ih2);
	}

public void onPaint(Graphics g)
	{
	if (!inAnimation)
		{
		g.setColor(0, 0, 0);
		g.drawText("Click to continue...", 0, 0);
		}
	if (firstTime)
		{
		loadImage();
		firstTime = false;
		}
	if (!inAnimation)
		g.drawImage(takiImage, imageX, imageY);
	}
}
