/*

Web.java

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
import waba.io.*;
import waba.sys.*;

/**
 * A progam that displays the text of a web page and lets a user scroll
 * using the page up and down keys or buttons.
 */

public class Web extends MainWindow
{
Edit urlEdit;
Button goB;
WebPage webPage;

public Web()
	{
	Label label = new Label("http://", Label.RIGHT);
	label.setRect(0, 2, 30, 16);
	add(label);

	urlEdit = new Edit();
	urlEdit.setText("www.wabasoft.com");
	urlEdit.setRect(30, 0, 95, 18);
	add(urlEdit);

	goB = new Button("Go");
	goB.setRect(130, 2, 30, 16);
	add(goB);

	webPage = new WebPage();
	webPage.setRect(0, 20, 160, 140);
	add(webPage);
	}

public void onEvent(Event event)
	{
	if (event.type == ControlEvent.PRESSED && event.target == goB)
		{
		String url = urlEdit.getText();
		webPage.load(url);
		}
	else if (event.type == KeyEvent.KEY_PRESS)
		{
		// any page up or down in program scrolls the web page
		KeyEvent ke = (KeyEvent)event;
		if (ke.key == IKeys.PAGE_UP)
			webPage.pageUp();
		else if (ke.key == IKeys.PAGE_DOWN)
			webPage.pageDown();
		}
	}
}
