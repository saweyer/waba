/*

Scribble.java

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
 * A program that lets a user scribble.
 */

public class Scribble extends MainWindow
{
DrawArea drawArea;
Button closeButton, clearButton;

public Scribble()
	{
	Title title = new Title("Scribble");
	title.setRect(0, 0, this.width, 15);
	add(title);

	drawArea = new DrawArea();
	drawArea.setRect(0, 20, this.width, this.height - 40);
	add(drawArea);

	closeButton = new Button("Close");
	closeButton.setRect(0, this.height - 15, 44, 15);
	add(closeButton);

	clearButton = new Button("Clear");
	clearButton.setRect(46, this.height - 15, 44, 15);
	add(clearButton);
	}

public void onEvent(Event event)
	{
	if (event.type == ControlEvent.PRESSED)
		{
		if (event.target == closeButton)
			exit(0);
		else if (event.target == clearButton)
			drawArea.clear();
		}
	}
}