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
