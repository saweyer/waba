/*

Controls.java

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

class ControlPage extends Container
{
Label label;
Button button;
Check check;
Radio radio;
Edit edit;

public ControlPage()
	{
	Label l = new Label("Label:", Label.RIGHT);
	l.setRect(5, 0, 40, 18);
	add(l);

	label = new Label("My Label");
	label.setRect(55, 0, 60, 18);
	add(label);

	l = new Label("Button:", Label.RIGHT);
	l.setRect(5, 25, 40, 18);
	add(l);

	button = new Button("My Button");
	button.setRect(55, 25, 60, 18);
	add(button);

	l = new Label("Check:", Label.RIGHT);
	l.setRect(5, 50, 40, 18);
	add(l);

	check = new Check("My Check");
	check.setRect(55, 50, 65, 18);
	add(check);

	l = new Label("Radio:", Label.RIGHT);
	l.setRect(5, 75, 40, 18);
	add(l);

	radio = new Radio("My Radio");
	radio.setRect(55, 75, 60, 18);
	add(radio);

	l = new Label("Edit:", Label.RIGHT);
	l.setRect(5, 100, 40, 18);
	add(l);

	edit = new Edit();
	edit.setText("My Edit");
	edit.setRect(55, 100, 60, 18);
	add(edit);
	}
}

class ContainerPage extends Container
{
TabBar tabBar;

public ContainerPage()
	{
	Label l = new Label("TabBar:");
	l.setRect(10, 0, 40, 18);
	add(l);
 	 
	tabBar = new TabBar();
	tabBar.add(new Tab("Yearly"));
	tabBar.add(new Tab("Monthly"));
	tabBar.add(new Tab("Daily"));
	tabBar.setRect(10, 20, 140, 20);
	add(tabBar);
	}
}

public class Controls extends MainWindow
{
Tab controlTab;
ControlPage controlPage;
Tab containerTab;
ContainerPage containerPage;
int pageShowing;

public Controls()
	{
	TabBar tabBar = new TabBar();
	controlTab = new Tab("Controls");
	tabBar.add(controlTab);
	containerTab = new Tab("Containers");
	tabBar.add(containerTab);
	tabBar.setRect(0, 0, this.width, 20);
	add(tabBar);

	controlPage = new ControlPage();
	controlPage.setRect(0, 30, this.width, this.height - 30);
	add(controlPage);
	}

public void onPaint(Graphics g)
	{
	g.setColor(0, 0, 0);
	g.drawLine(160, 0, 160, 160);
	g.drawLine(0, 160, 160, 160);
	}

public void onEvent(Event event)
	{
	if (event.type == ControlEvent.PRESSED)
		{
		if (event.target == controlTab)
			{
			if (containerPage != null)
				remove(containerPage);
			add(controlPage);
			}
		else if (event.target == containerTab)
			{
			if (containerPage == null)
				{
				containerPage = new ContainerPage();
				containerPage.setRect(0, 30, this.width, this.height - 30);
				}
			remove(controlPage);
			add(containerPage);
			}
		}
	}
}