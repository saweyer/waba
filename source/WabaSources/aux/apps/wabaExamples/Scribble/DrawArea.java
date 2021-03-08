import waba.ui.*;
import waba.fx.*;

/**
 * A control that allows a user to draw using the pen or mouse.
 */

public class DrawArea extends Control
{
Graphics drawg;
int lastX, lastY;

public void onEvent(Event event)
	{
	if (drawg == null)
		{
		drawg = createGraphics();
		drawg.setClip(0, 0, this.width, this.height);
		}
	if (event.type == PenEvent.PEN_DOWN)
		{
		PenEvent penEvent = (PenEvent)event;
		lastX = penEvent.x;
		lastY = penEvent.y;
		}
	else if (event.type == PenEvent.PEN_DRAG)
		{
		PenEvent penEvent = (PenEvent)event;
		drawg.drawLine(lastX, lastY, penEvent.x, penEvent.y);
		lastX = penEvent.x;
		lastY = penEvent.y;
		}
	}

public void clear()
	{
	repaint();
	}

public void onPaint(Graphics g)
	{
	// erase draw area in white
	g.setColor(255, 255, 255);
	g.fillRect(0, 0, this.width, this.height);
	}
}
