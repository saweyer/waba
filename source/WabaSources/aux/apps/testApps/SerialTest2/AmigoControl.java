/* 
*/

import waba.ui.*;
import waba.fx.*;
import waba.sys.*;
import extra.util.*;


public class AmigoControl  extends MainWindow{
  static final int CELLSIZE = 4;	// Try 4 for more cells
  static final int XSIZE = 160 / CELLSIZE;
  static final int YSIZE = 128 / CELLSIZE;
  static final int CENTERX = XSIZE / 2;
  static final int CENTERY = YSIZE / 2;
  static final int TITLEHEIGHT = 15;
  static final int BUTTONHEIGHT = 15;
  static final int BUTTONWIDTH  = 39;
  static final int ON = 1;
  static final int OFF = 0;
  static final int ratio = 2;
  
  byte Boards[][] = new byte[YSIZE+2][XSIZE+2];
  
  Graphics g;

  Button closeButton, clearButton, disconnectButton, connectButton, danceButton;
  Title title;
  Font plainFont = new Font("helvetica", Font.PLAIN, 12);

  AmigoBot robot = new AmigoBot();

  public AmigoControl()
  {
  	g = new Graphics(this);
        robot.mainGraphics = g;
	
	g.setColor(255,255,255);
	Rect rect = getRect();
	g.fillRect(0,0,width,height);
	g.setColor(0,0,0);

//	closeButton = new Button("Close");
//	closeButton.setRect(0, height - BUTTONHEIGHT, BUTTONWIDTH, BUTTONHEIGHT);
//	add(closeButton);
	
	clearButton = new Button("Clear");
	clearButton.setRect(0 , height - BUTTONHEIGHT, BUTTONWIDTH, BUTTONHEIGHT);
	add(clearButton);
	
	connectButton = new Button("Conn");
	connectButton.setRect(1 * (BUTTONWIDTH + 1), height - BUTTONHEIGHT, BUTTONWIDTH, BUTTONHEIGHT);
	add(connectButton);
	
	disconnectButton = new Button("Disconn");
	disconnectButton.setRect(2 * (BUTTONWIDTH + 1), height - BUTTONHEIGHT, BUTTONWIDTH, BUTTONHEIGHT);
	add(disconnectButton);

	danceButton = new Button("Dance");
	danceButton.setRect(3 * (BUTTONWIDTH + 1), height - BUTTONHEIGHT, BUTTONWIDTH, BUTTONHEIGHT);
	add(danceButton);

	title = new Title("AmigoControl");
	title.setRect(0, 0, width, TITLEHEIGHT);
	add(title);
   }


  public void onEvent(Event event)
  {
 	//if (event.type == ControlEvent.PRESSED)
    if (event.type == PenEvent.PEN_DOWN)
    { 		
      if (event.target == clearButton)
      {
        clearBoard();
   
        if (robot.connected)
          Boards[CENTERY][CENTERX] = ON;
	
	Rect rect = getRect();
	onPaint(0, TITLEHEIGHT, width, height - TITLEHEIGHT - BUTTONHEIGHT); // repaint
      } 

      else if (event.target == connectButton) 
      {
        if (!robot.connected)
        {	  
	  robot.connect();
	  robot.sonar(false);
	  robot.enable(true);
          clearBoard();	  
          Boards[CENTERY][CENTERX] = ON;
        }	
      } 

      else if (event.target == disconnectButton) 
      {
        if (robot.connected) robot.disconnect();

        Boards[CENTERY][CENTERX] = OFF;
      }

      else if (event.target == danceButton) 
      {
	for( short i = 0 ; i < 10 ; i++ )
	{
	  robot.sound(i);
	  robot.dhead( (short)(60 * (short)(2*(i%2)-1)) );
	  Vm.sleep(1000);
	}//for i
      }
      
      else 
      {
 	PenEvent penEvent = (PenEvent)event;
    	int x = penEvent.x / CELLSIZE + CENTERX;
    	int y = (penEvent.y  - TITLEHEIGHT) / CELLSIZE + CENTERY;

        if (robot.connected) moveTo(x,y);
      }//else
    }// if event.type == PenEvent.PEN_DOWN
  }//public void onEvent()


  void moveTo(int x, int y)
  {
    float dist = Maths.sqrt(x*x + y*y);
    short ang  = (short)(Maths.atan2(y,x) * 180/Maths.PI);
    
    robot.dhead(ang);
  }
  

  void drawCell(int x, int y, byte on)
  {
    if (on != 0) g.setColor(0,0,0);
    else g.setColor(255,255,255);

    g.fillRect( (short)((x-1)*CELLSIZE), 
    			(short)((y-1)*CELLSIZE + TITLEHEIGHT), 
    			(short)(CELLSIZE), 
    			(short)(CELLSIZE));

    g.setColor(0,0,0);
  }

  
  void clearBoard () 
  {
    for (int ycount = 1; ycount <= YSIZE; ycount++) 
    {
      for (int xcount = 1; xcount <= XSIZE; xcount++) 
        Boards[ycount][xcount] = OFF;
    }//for ycount 

//    drawCell(CENTERX,CENTERY,(byte)1);
  }//void clearBoard


  public void onPaint(int x, int y, int width, int height) 
  {
	// clear area
	g.setColor(255, 255, 255);
	g.fillRect(x, y, width, height);

	// draw contents
	if (y <TITLEHEIGHT) title.onPaint(g);
	Rect rect = getRect();
	if ((y + height) > (rect.height - BUTTONHEIGHT)) 
        {
	  closeButton.onPaint(g);
	  clearButton.onPaint(g);
	  connectButton.onPaint(g);
	  disconnectButton.onPaint(g);
	  danceButton.onPaint(g);
	}//if
	for (int ycount = 1; ycount <= YSIZE; ycount++) 
        {
          for (int xcount = 1; xcount <= XSIZE; xcount++) 
            if (Boards[ycount][xcount] != 0 ) 
             drawCell(xcount, ycount, (byte)ON);
        }//for ycount 
  }//public void onPaint
}//class
