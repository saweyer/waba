/*

Title.java

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
 * A control that displays an application's title.
 */

public class Title extends Control
{
String name;
Font font;

public Title(String name)
	{
	this.name = name;
	this.font = new Font("Helvetica", Font.BOLD, 12);
	}

public void onPaint(Graphics g)
	{
	// draw line across
	g.setColor(0, 0, 0);
	int y = this.height - 1;
	g.drawLine(0, y, this.width, y);
	y--;
	g.drawLine(0, y, this.width, y);

	// draw title
	FontMetrics fm = getFontMetrics(font);
	int boxWidth = fm.getTextWidth(name) + 8;
	g.fillRect(0, 0, boxWidth, y);
	g.setColor(255, 255, 255);
	g.setFont(font);
	g.drawText(name, 4, 2);
	}
}
