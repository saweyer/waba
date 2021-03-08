/*
PROPOSED CHANGES

0. Made some functions optionally native, and some other functions optionally
   non-native, to ease implementation and optimization of this class.
   
1. Some functions are deprecated and replaced with "properly spelled"
   versions of the functions, to more closely approximate real java.awt.Graphics:
   
        free	-> 	dispose
        drawText->	drawString, drawChars

2. setColor(Color c) added.

3. drawArc, fillArc, drawRoundRect, fillRoundRect, drawOval, fillOval added.
    They're all based on drawArc and fillArc.  You will need to implement 
    drawArc and fillArc.  If your system doesn't support this natively, 
    one slow way to do it is to follow the code found in the PJA Graphics
    library at http://www.eteks.com/pja/en/   This library, btw, is also
    where I grabbed the drawRoundRect and fillRoundRect implementations.

4. drawLine(x,y,x,y) defined as MUST be drawing a point

5. fillPolygon(...) and drawPolygon(...) defined as MUST permit a polygon
   of two points (they should draw a simple line in this instance)

6. For people who can't support a native implementation of fillPolygon, a
   Java-only version is provided courtesy of the PJA Graphics Library.
   
7. Added drawPolyline(...), and defined drawPolygon(...) in terms of it.

// outstanding issue: how should we handle drawText etc.?  Presently the Palm
// draws a white background behind it.  This is not how Java does this; would
// it be better to instead define drawText as drawing letters directly on
// top of the existing background, without a rect first erasing the general text
// area?  Can the Palm support this?

*/



/*
Copyright (c) 1998, 1999 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/
    

package waba.fx;


/**
 * Graphics draws on a surface.
 * <p>
 * Surfaces are objects that implement the ISurface interface.
 * MainWindow and Image are both examples of surfaces.
 * <p>
 * Here is an example that uses Graphics to draw a line:
 *
 * <pre>
 * public class MyProgram extends MainWindow
 * {
 * public void onPaint(Graphics g)
 *  {
 *  g.setColor(0, 0, 255);
 *  g.drawLine(0, 0, 10, 10);
 * ...
 * </pre>
 */

public class Graphics
{
private ISurface surface;


/**
 * The constant for a draw operation that draws the source over
 * the destination.
 */
public static final int DRAW_OVER = 1;

/**
 * The constant for a draw operation that AND's the source with the
 * destination. Commonly used to create image masks.
 */
public static final int DRAW_AND = 2;

/**
 * The constant for a draw operation that OR's the source with the
 * destination. Commonly used with image masks.
 */
public static final int DRAW_OR = 3;

/**
 * The constant for a draw operation that XOR's the source with the
 * destination.
 */
public static final int DRAW_XOR = 4;

/**
 * Constructs a graphics object which can be used to draw on the given
 * surface. For the sake of the methods in this class, the given surface
 * is known as the "current surface".
 * <p>
 * If you are trying to create a graphics object for drawing in a subclass
 * of control, use the createGraphics() method in the Control class. It
 * creates a graphics object and translated the origin to the origin of
 * the control.
 */
public Graphics(ISurface surface)
	{
	this.surface = surface;
	_nativeCreate();
	}


private native void _nativeCreate();

/**
 * Clears the current clipping rectangle. This allows drawing to occur
 * anywhere on the current surface.
 */
public native void clearClip();


/**
 * Copies a rectangular area from a surface to the given coordinates on
 * the current surface. The copy operation is performed by combining
 * pixels according to the setting of the current drawing operation.
 * The same surface can be used as a source and destination to
 * implement quick scrolling.
 * <p>
 * Not all combinations of surfaces are supported on all platforms.
 * PalmOS has problems copying from an Window surface to an Image and
 * between two Image surfaces. Java doesn't allow copying from an
 * Window surface to an Image.
 *
 * @param surface the surface to copy from
 * @param x the source x coordinate
 * @param y the source y coordinate
 * @param width the width of the area on the source surface
 * @param height the height of the area on the source surface
 * @param dstX the destination x location on the current surface
 * @param dstY the destination y location on the current surface
 * @see #setDrawOp
 */
public native void copyRect(ISurface surface, int x, int y,
	int width, int height, int dstX, int dstY);


/// PROPOSED CHANGE
// Replace free() with dispose() and deprecate free() to be more
// compatible with the JDK.

/**
 * Frees any system resources (native device contexts) associated with the
 * graphics object. After calling this function, the graphics object can no
 * longer be used to draw. Calling this method is not required since any
 * system resources allocated will be freed when the object is garbage
 * collected. However, if a program uses many graphics objects, free()
 * should be called whenever one is no longer needed to prevent allocating
 * too many system resources before garbage collection can occur.
@deprecated
 */
public native void free();

public final void dispose() { free(); }

/// PROPOSED CHANGE
// Replace drawText(...) with drawChars(...) and deprecate drawText(...)
// to be more compatible with the JDK.

/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text's bounding box.
 * @param chars the character array to display
 * @param start the start position in array
 * @param count the number of characters to display
 * @param x the left coordinate of the text's bounding box
 * @param y the top coordinate of the text's bounding box
 @deprecated
  */
public native void drawText(char chars[], int start, int count, int x, int y);

/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text's bounding box.
 * @param chars the character array to display
 * @param start the start position in array
 * @param count the number of characters to display
 * @param x the left coordinate of the text's bounding box
 * @param y the top coordinate of the text's bounding box
 */
public void drawChars(char chars[], int start, int count, int x, int y)
    { drawText(chars,start,count,x,y); }

/** Draws an image at the given x and y coordinates.*/
public void drawImage(Image image, int x, int y)
	{
	copyRect(image, 0, 0, image.getWidth(), image.getHeight(), x, y);
	}

/** Draws a cursor by XORing the given rectangular area on the surface.
  * Since it is XORed, calling the method a second time with the same
  * parameters will erase the cursor.
  */
public native void drawCursor(int x, int y, int width, int height);


/**
 * Draws a line at the given coordinates. The drawing includes both
 * endpoints of the line.  This means that to draw a single point,
 * you can simply do drawLine(x,y,x,y);
 */
public native void drawLine(int x1, int y1, int x2, int y2);


/// SUGGESTED CHANGE
// drawRect(...) should be optionally native.  Since Graphics.java
// isn't added to the warp file, this shouldn't be an issue.

/**
 * Draws a rectangle at the given coordinates.
 */
// NATIVE INFORMATION:
// HASH: 2182103503
// C NAME: GraphicsDrawRect
public native void drawRect(int x, int y, int width, int height);
/*
// I have commented out this version, which is slow, and replaced
// it with a native call -- Sean
public void drawRect(int x, int y, int width, int height)
	{
	// NOTE: only valid for drawing rects with width >=1, height >= 1
	int x2 = x + width - 1;
	int y2 = y + height - 1;
	drawLine(x, y, x2 - 1, y);
	drawLine(x2, y, x2, y2 - 1);
	drawLine(x2, y2, x + 1, y2);
	drawLine(x, y2, x, y + 1);
	}
*/

/**
 * Draws a dotted line at the given coordinates. Dotted lines must
 * be either horizontal or vertical, they can't be drawn at arbitrary angles.
 */
public native void drawDots(int x1, int y1, int x2, int y2);


/// PROPOSED CHANGE
// drawPolygon(...) must be guaranteed to draw a line if there are only
// two points in the polygon array.  That's how the AWT handles it. 

/// PROPOSED CHANGE
// The non-native version of drawPolygon has been rewritten in terms
// of drawPolyline. 

/// SUGGESTED CHANGE
// drawPolygon(...) should be optionally native.  Since Graphics.java
// isn't added to the warp file, this shouldn't be an issue.

/**
 * Draws the outline of a polygon with the given coordinates.
 * The polygon is automatically closed, you should not duplicate
 * the start point to close the polygon.
 * @param x x vertex coordinates
 * @param y y vertex coordinates
 * @param count number of vertices
 */
// NATIVE INFORMATION:
// HASH: 2182132627
// C NAME: GraphicsDrawPolygon
public native void drawPolygon(int x[], int y[], int count);

/*
// I have commented out this version, which is slow, and replaced
// it with a native call -- Sean
public void drawPolygon(int x[], int y[], int count)
	{
        drawPolyline(x,y,count);  // draw the polyline
	if (count > 2)
            drawLine(x[i], y[i], x[0], y[0]);  // close up
	}
*/


/// PROPOSED CHANGE
// Added drawPolyline, which does not connect the first and last
// points in the array.  It's a useful feature and trivially
// implemented feature in JDK 1.1, and doesn't add much to Graphics
// especially if drawPolygon is defined in terms of it (see above).

public void drawPolyline(int x[], int y[], int count)
	{
	if (count < 2)   // bug noted by Sean, it used to be 3
		return;
	int i = 0;
	for (; i < count - 1; i++)
		drawLine(x[i], y[i], x[i + 1], y[i + 1]);
	}


/// PROPOSED CHANGE
// fillPolygon(...) must be guaranteed to draw a line if there are only
// two points in the polygon array.  That's how the AWT handles it. 

/// SUGGESTED CHANGE
// fillPolygon(...) should be optionally non-native.  Since Graphics.java
// isn't added to the warp file, this shouldn't be an issue.
// I give an (admittedly slow) Java version of the code below, totally
// untested but I think it works correctly. 

/**
 * Draws a filled polygon with the given coordinates.
 * The polygon is automatically closed, you should not duplicate
 * the start point to close the polygon. The polygon is filled
 * according to Jordan's rule - a point is inside if a horizontal
 * line to the point intersects the polygon an odd number of times.
 * This function is not implemented for the PalmOS VM. Under PalmOS only
 * the outline of the polygon is drawn.
 * @param x x vertex coordinates
 * @param y y vertex coordinates
 * @param count number of vertices
 */
public native void fillPolygon(int x[], int y[], int count);

/*
  // v1.1 : Added contains () method : it's the same code as
  // Polygon.inside () of JDK 1.1 method except it doesn't use float type.
private boolean contains(int xPoints [], int yPoints [], int nPoints, int x, int y)
  {
      int hits = 0;
      int ySave = 0;

      // Find a vertex that's not on the halfline
      int i = 0;
      while (i < nPoints && yPoints [i] == y)
        i++;

      // Walk the edges of the polygon
      for (int n = 0; n < nPoints; n++)
      {
        int j = (i + 1) % nPoints;

        int dx = xPoints [j] - xPoints [i];
        int dy = yPoints [j] - yPoints [i];

        // Ignore horizontal edges completely
        if (dy != 0)
        {
          // Check to see if the edge intersects
          // the horizontal halfline through (x, y)
          int rx = x - xPoints [i];
          int ry = y - yPoints [i];

          // Deal with edges starting or ending on the halfline
          if (yPoints [j] == y && xPoints [j] >= x)
            ySave = yPoints [i];

          if (yPoints[i] == y && xPoints[i] >= x)
            if ((ySave > y) != (yPoints[j] > y))
              hits--;

          // Changed Polygon.inside () method here without float
          if (   ry * dy >= 0
              && (   ry <= dy && ry >= 0
                  || ry >= dy && ry <= 0)
              && roundDiv (dx * ry, dy) >= rx)
            hits++;
        }

        i = j;
      }

      // Inside if number of intersections odd
      return (hits % 2) != 0;
  }

  public void fillPolygon (int xPoints[], int yPoints[], int nPoints)
  {
    int furthestLeft;
    int furthestRight;
    int topmost;
    int bottommost;
    
    if (nPoints < 2) return;  // we should fill polygonal lines
    if (nPoints==2) return drawLine(xpoints[0],ypoints[0],xpoints[1],ypoints[1]);
    
    furthestLeft=furthestRight=xPoints[0];
    topmost=bottommost=yPoints[0];
    for(int x=0;x<nPoints;x++)
        {
        if (xPoints[x]<furthestLeft) furthestLeft=xPoints[x];
        else if xPoints[x]>furthestRight) furthestRight=xPoints[x];
        if (yPoints[x]<topmost) topmost=yPoints[x];
        else if yPoints[x]>bottommost) bottommost=yPoints[x];
        }
    
    for (int y = topmost; y <= bottommost; y++)
        for (int x = furthestLeft; x <= furthestRight; x++)
          if (contains (xPointsCopy, yPointsCopy, nPoints, x, y))
            drawLine (x, y, x, y);  // draw the point
  }
/*


/// PROPOSED CHANGE:
// drawText(...) should be replaced with drawString(...) and deprecated,
// to be more compatible with the JDK.

/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text.
 @deprecated
 */
public native void drawText(String s, int x, int y);

/**
 * Draws text at the given coordinates. The x and y coordinates specify
 * the upper left hand corner of the text.
 */
public final void drawString(String s, int x, int y)
    { drawText(s,x,y); }

/**
 * Fills a rectangular area with the current color.
 */
public native void fillRect(int x, int y, int width, int height);


/**
 * Sets a clipping rectangle. Anything drawn outside of the rectangular
 * area specified will be clipped. Setting a clip overrides any previous clip.
 */
public native void setClip(int x, int y, int width, int height);


/**
 * Sets the x, y, width and height coordinates in the rectangle passed
 * to the current clip coordinates. To reduce the use of temporary objects
 * during drawing, this method does not allocate its own rectangle
 * object. If there is no current clip, null will be returned and
 * the rectangle passed will remain unchanged. Upon success, the
 * rectangle passed to the method will be returned.
 */
public native Rect getClip(Rect r);


/**
 * Sets the current color for drawing operations.
 * @param r the red value (0..255)
 * @param g the green value (0..255)
 * @param b the blue value (0..255)
 */
public native void setColor(int r, int g, int b);


/// PROPOSED CHANGE:
// Added setColor(Color c) to be more compatible with the JDK.
// the Java code is exactly as below.

/**
 * Sets the current color for drawing operations.
 * @param c the color
 */
public void setColor(Color c)
    {
    setColor(c.getRed(),c.getGreen(),c.getBlue());
    }

/**
 * Sets the drawing operation. The setting determines the raster
 * operation to use when drawing lines, rectangles, text and
 * images on the current surface. It also determines how pixels are
 * combined when copying one surface to another. The setting of
 * DRAW_OVER, where any drawing simply draws over the pixels on
 * a surface, is the default.
 * <p>
 * Not all operations are supported on all platforms. When used with
 * Java, DRAW_OVER is supported for all types of drawing and DRAW_XOR
 * is supported for drawing lines, rectangles, text and images.
 * However, DRAW_XOR is not supported when copying surface areas and
 * the DRAW_AND and DRAW_OR operations aren't supported at all under
 * Java.
 * <p>
 * PalmOS platforms supports all the drawing operations when drawing
 * images and copying surface regions. However, only the DRAW_OVER
 * operation is supported when drawing lines, rectangles and text.
 * If you need to use the XOR drawing operation for drawing lines
 * under PalmOS, you can draw the line into an image and then draw
 * the image with an XOR drawing operation.
 * <p>
 * Win32 and Windows CE platforms support all the drawing operations
 * except when drawing text. Only DRAW_OVER is supported when drawing
 * text. If you need to draw XOR'ed text, you can draw the text into
 * an image and then draw the image with an XOR draw operation.
 * <p>
 * When calculating the result of XOR, AND and OR drawing, the value
 * of the color black is all 1's (fully set) in binary and white is
 * all 0's (fully unset).
 *
 * @param op drawing operation
 * @see #DRAW_OVER
 * @see #DRAW_AND
 * @see #DRAW_OR
 * @see #DRAW_XOR
 */
public native void setDrawOp(int drawOp);


/** Sets the current font for operations that draw text. */
public native void setFont(Font font);


/**
 * Translates the origin of the current coordinate system by the given
 * x and y values.
 */
public native void translate(int x, int y);

/// PROPOSED CHANGE:
// Added drawRoundRect(...) to be more compatible with the JDK.
// the Java code is exactly as below.

/**
 * Draws a Round Rect 
 */ 
  public void drawRoundRect (int x, int y, int width, int height,
                             int arcWidth, int arcHeight)
  {
    drawLine (x + arcWidth / 2, y, x + width - arcWidth / 2, y);
    drawLine (x, y + arcHeight / 2, x, y + height - arcHeight / 2);
    drawLine (x + arcWidth / 2, y + height, x + width - arcWidth / 2, y + height);
    drawLine (x + width, y + arcHeight / 2, x + width, y + height - arcHeight / 2);

    drawArc (x, y, arcWidth, arcHeight, 90, 90);
    drawArc (x + width - arcWidth, y, arcWidth, arcHeight, 0, 90);
    drawArc (x, y + height + - arcHeight, arcWidth, arcHeight, 180, 90);
    drawArc (x + width - arcWidth, y + height + - arcHeight, arcWidth, arcHeight, 270, 90);
  }


/// PROPOSED CHANGE:
// Added fillRoundRect(...) to be more compatible with the JDK.
// the Java code is exactly as below.
/**
 * Fills a Round Rect 
 */ 
  public void fillRoundRect (int x, int y, int width, int height,
                             int arcWidth, int arcHeight)
  {
    fillRect (x + arcWidth / 2, y, width - arcWidth + 1, height);
    fillRect (x, y + arcHeight / 2 - 1, arcWidth / 2, height - arcHeight);
    fillRect (x + width - arcWidth / 2, y + arcHeight / 2 - 1, arcWidth / 2, height - arcHeight);

    fillArc (x, y, arcWidth - 1, arcHeight - 1, 90, 90);
    fillArc (x + width - arcWidth, y, arcWidth - 1, arcHeight - 1, 0, 90);
    fillArc (x, y + height + - arcHeight, arcWidth - 1, arcHeight - 1, 180, 90);
    fillArc (x + width - arcWidth, y + height + - arcHeight, arcWidth - 1, arcHeight - 1, 270, 90);
  }


/// PROPOSED CHANGE:
// Added drawOval(...) to be more compatible with the JDK.
// the Java code is exactly as below.

  public void drawOval(int x, int y, int width, int height)
    {
    drawRoundRect(x,y,width,height,width,height);
    }
    
    
/// PROPOSED CHANGE:
// Added fillOval(...) to be more compatible with the JDK.
// the Java code is exactly as below.

  public void fillOval(int x, int y, int width, int height)
    {
    fillRoundRect(x,y,width,height,width,height);
    }
    
    
/// PROPOSED CHANGE:
// Added drawArc(...) to be more compatible with the JDK.  It's native,
// you'll need to implement it.  If you cannot, there are (slow) Java
// implementations available in the PJA Graphics Library
// http://www.eteks.com/pja/en/ but it'll take some massaging.

// NATIVE INFORMATION:
// HASH: 2182105168
// C NAME: GraphicsDrawArc
  public native void drawArc (int x, int y, int width, int height,
                       int start, int arcAngle);
                       
/// PROPOSED CHANGE:
// Added fillArc(...) to be more compatible with the JDK.  It's native,
// you'll need to implement it.  If you cannot, there are (slow) Java
// implementations available in the PJA Graphics Library
// http://www.eteks.com/pja/en/ but it'll take some massaging.

// NATIVE INFORMATION:
// HASH: 2182104720
// C NAME: GraphicsFillArc
  public native void fillArc (int  x, int y, int width, int height,
                       int start, int arcAngle);
}
