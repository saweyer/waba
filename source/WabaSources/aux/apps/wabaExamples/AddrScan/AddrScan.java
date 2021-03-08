/*

AddrScan.java

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
 * AddrScan lets a user scan through the records in a PalmPilot address book.
 */

public class AddrScan extends MainWindow
{
AddrCatalog cat;
AddrPanel addrPanel;
Button closeButton, nextButton, prevButton;

public void onStart()
	{
	// open address catalog
	cat = new AddrCatalog();
	cat.setRecordPos(0);

	// create user-interface
	Title title = new Title("AddrScan");
	title.setRect(0, 0, this.width, 15);
	add(title);

	addrPanel = new AddrPanel(cat);
	addrPanel.setRect(5, 20, this.width - 10, this.height - 40);
	add(addrPanel);

	closeButton = new Button("Close");
	closeButton.setRect(0, this.height - 15, 44, 15);
	add(closeButton);

	prevButton = new Button("Prev");
	prevButton.setRect(46, this.height - 15, 44, 15);
	add(prevButton);

	nextButton = new Button("Next");
	nextButton.setRect(92, this.height - 15, 44, 15);
	add(nextButton);
	}

// moves to the next or previous record
void nextRecord(boolean backwards)
	{
	int pos = cat.getCurrentPos();
	boolean moved = false;
	if (backwards && pos != 0)
		{
		pos--;
		moved = true;
		}
	else if (!backwards && pos < cat.getRecordCount() - 1)
		{
		pos++;
		moved = true;
		}
	if (!moved)
		return;
	cat.setRecordPos(pos);
	addrPanel.repaint();
	}

public void onEvent(Event event)
	{
	if (event.type == ControlEvent.PRESSED)
		{
		if (event.target == closeButton)
			exit(0);
		else if (event.target == prevButton)
			nextRecord(true);
		else if (event.target == nextButton)
			nextRecord(false);
		}
	}
}


