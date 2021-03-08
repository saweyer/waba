/*
Copyright (C) 1998, 1999, 2000 Wabasoft

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later version. 

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details. 

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
Cambridge, MA 02139, USA. 
*/

// NOTE: General rule for native functions.. don't hold a pointer across any
// call that could garbage collect. For example, this isn't good:
//
// ptr = WOBJ_arrayStart(array)
// ...
// string = createString(..)
// ptr[0]
//
// since the createString() could GC, the ptr inside of array could be invalid
// after the call since a GC would move memory around. Instead, use:
//
// ptr = WOBJ_arrayStart(array)
// ...
// string = createString(..)
// ...
// ptr = WOBJ_arrayStart(array)
// ptr[0]
//
// to recompute the pointer after the possible GC

// NOTE: If you subclass a class with an object destroy function, you must
// explicity call your superclasses object destroy function.

static void GraphicsDestroy(WObject obj);
static void ImageDestroy(WObject obj);
static void CatalogDestroy(WObject obj);
static void SocketDestroy(WObject obj);
static void SerialPortDestroy(WObject obj);

static Var ImageCreate(Var stack[]);

ClassHook classHooks[] =
	{
	{ "waba/fx/Graphics", NULL, 11},
	{ "waba/fx/Image", ImageDestroy, 1 },
	{ "waba/io/Catalog", CatalogDestroy, 7 },
	{ "waba/io/Socket", SocketDestroy, 2 }, 
	{ "waba/io/SerialPort", SerialPortDestroy, 2 }, 
	{ NULL, NULL }
	};

//
// Rect
//
// var[0] = Class
// var[1] = int x
// var[2] = int y
// var[3] = int width
// var[4] = int height

#define WOBJ_RectX(o) (objectPtr(o))[1].intValue
#define WOBJ_RectY(o) (objectPtr(o))[2].intValue
#define WOBJ_RectWidth(o) (objectPtr(o))[3].intValue
#define WOBJ_RectHeight(o) (objectPtr(o))[4].intValue

//
// Control
//
// var[0] = Class
// var[1] = int x
// var[2] = int y
// var[3] = int width
// var[4] = int height

#define WOBJ_ControlX(o) (objectPtr(o))[1].intValue
#define WOBJ_ControlY(o) (objectPtr(o))[2].intValue
#define WOBJ_ControlWidth(o) (objectPtr(o))[3].intValue
#define WOBJ_ControlHeight(o) (objectPtr(o))[4].intValue

//
// Window
//

static Var WindowCreate(Var stack[])
	{
	WObject win;
	Var v;

	win = stack[0].obj;
	WOBJ_ControlX(win) = 0;
	WOBJ_ControlY(win) = 0;
	WOBJ_ControlWidth(win) = 160;
	WOBJ_ControlHeight(win) = 160;
	v.obj = 0;
	return v;
	}

//
// MainWindow
//

static Var MainWinCreate(Var stack[])
	{
	Var v;

	globalMainWin = stack[0].obj;
	v.obj = 0;
	return v;
	}

static Var MainWinExit(Var stack[])
	{
	Var v;

	postStopEvent(); // see nmpalm_b.c
	v.obj = 0;
	return v;
	}

static Var MainWinSetTimerInterval(Var stack[])
	{
	Var v;

	globalTimerInterval = stack[1].intValue;
	globalTimerStart = getTimeStamp();
	v.obj = 0;
	return v;
	}

//
// Surface
//

#define SURF_MAINWIN 1
#define SURF_IMAGE 2

static WClass *mainWinClass = 0;
static WClass *imageClass = 0;

static int SurfaceGetType(WObject surface)
	{
	WClass *wclass;

	if (surface == 0)
		return 0;

	// cache class pointers for performance
	if (!mainWinClass)
		mainWinClass = getClass(createUtfString("waba/ui/MainWindow"));
	if (!imageClass)
		imageClass = getClass(createUtfString("waba/fx/Image"));

	wclass = WOBJ_class(surface);
	if (compatible(wclass, mainWinClass))
		return SURF_MAINWIN;
	if (compatible(wclass, imageClass))
		return SURF_IMAGE;
	return 0;
	}

//
// Font
//
// var[0] = Class
// var[1] = String name
// var[2] = int size
// var[3] = int style
//

#define WOBJ_FontName(o) (objectPtr(o))[1].obj
#define WOBJ_FontStyle(o) (objectPtr(o))[2].intValue
#define WOBJ_FontSize(o) (objectPtr(o))[3].intValue 
#define Font_PLAIN 0
#define Font_BOLD 1

static FontID getPalmFontID(WObject font)
	{
	if (WOBJ_FontStyle(font) == Font_BOLD)
		return boldFont;
	return stdFont;
	}

//
// FontMetrics
//
// var[0] = Class
// var[1] = Font
// var[2] = Surface
// var[3] = int ascent
// var[4] = int descent
// var[5] = int leading
//

#define WOBJ_FontMetricsFont(o) (objectPtr(o))[1].obj
#define WOBJ_FontMetricsSurface(o) (objectPtr(o))[2].obj
#define WOBJ_FontMetricsAscent(o) (objectPtr(o))[3].intValue
#define WOBJ_FontMetricsDescent(o) (objectPtr(o))[4].intValue
#define WOBJ_FontMetricsLeading(o) (objectPtr(o))[5].intValue

static Var FontMetricsCreate(Var stack[])
	{
	FontID fontID, prevFontID;
	WObject font, fontMetrics, surface;
	int32 ascent, descent;
	Var v;

	fontMetrics = stack[0].obj;
	font = WOBJ_FontMetricsFont(fontMetrics);
	surface = WOBJ_FontMetricsSurface(fontMetrics);
	if (font == 0 || surface == 0)
		{
		WOBJ_FontMetricsAscent(fontMetrics) = 0;
		WOBJ_FontMetricsDescent(fontMetrics) = 0;
		WOBJ_FontMetricsLeading(fontMetrics) = 0;
		v.obj = 0;
		return v;
		}
	// surface is unused and only 2 fonts are supported
	fontID = getPalmFontID(font);
	prevFontID = FntSetFont(fontID);
	ascent = (int32)FntBaseLine();
	descent = (int32)FntDescenderHeight();
	WOBJ_FontMetricsAscent(fontMetrics) = ascent;
	WOBJ_FontMetricsDescent(fontMetrics) = descent;
	WOBJ_FontMetricsLeading(fontMetrics) = (int32)FntLineHeight() - ascent - descent;
	FntSetFont(prevFontID);
	v.obj = fontMetrics;
	return v;
	}

#define FM_STRINGWIDTH 1
#define FM_CHARARRAYWIDTH 2
#define FM_CHARWIDTH 3

static Var FontMetricsGetWidth(int type, Var stack[])
	{
	FontID fontID, prevFontID;
	WObject font, fontMetrics, surface;
	Var v;
	int32 width;

	fontMetrics = stack[0].obj;
	font = WOBJ_FontMetricsFont(fontMetrics);
	surface = WOBJ_FontMetricsSurface(fontMetrics);
	if (font == 0 || surface == 0)
		{
		v.intValue = 0;
		return v;
		}
	// surface is unused and only 2 fonts are supported
	fontID = getPalmFontID(font);
	prevFontID = FntSetFont(fontID);
	switch (type)
		{
		case FM_CHARWIDTH:
			{
			Char ch;

			ch = (Char)stack[1].intValue;
			width = (int32)FntCharWidth(ch);
			break;
			}
		case FM_STRINGWIDTH:
		case FM_CHARARRAYWIDTH:
			{
			WObject string, charArray;
			int32 start, count;
			uint16 *chars;

			width = 0;
			if (type == FM_STRINGWIDTH)
				{
				string = stack[1].obj;
				if (string == 0)
					break;
				charArray = WOBJ_StringCharArrayObj(string);
				if (charArray == 0)
					break;
				start = 0;
				count = WOBJ_arrayLen(charArray);
				}
			else // FM_CHARARRAYWIDTH
				{
				charArray = stack[1].obj;
				start = stack[2].intValue;
				count = stack[3].intValue;
				if (arrayRangeCheck(charArray, start, count) == 0)
					break; // array null or range invalid
				}
			chars = (uint16 *)WOBJ_arrayStart(charArray);
			chars = &chars[start];
			while (count > 0)
				{
				char buf[40];
				int32 i, n;

				n = sizeof(buf);
				if (n > count)
					n = count;
				for (i = 0; i < n; i++)
					buf[i] = (char)chars[i];
				width += (int32)FntCharsWidth(buf, (Word)count);
				count -= n;
				chars += n;
				}
			break;
			}
		}
	FntSetFont(prevFontID);
	v.intValue = width;
	return v;
	}

static Var FontMetricsGetStringWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_STRINGWIDTH, stack);
	}

static Var FontMetricsGetCharArrayWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_CHARARRAYWIDTH, stack);
	}

static Var FontMetricsGetCharWidth(Var stack[])
	{
	return FontMetricsGetWidth(FM_CHARWIDTH, stack);
	}

// Debugging Memory Sizes
#ifdef DEBUGMEMSIZE
static void debugMemSize()
	{
	char buf[40];
	RectangleType rect;

	xmemzero(buf, 40);

	// add class heap info
	buf[xstrlen(buf)] = 'c';
	buf[xstrlen(buf)] = ':';
	StrIToA(&buf[xstrlen(buf)], classHeapUsed);
	buf[xstrlen(buf)] = '/';
	StrIToA(&buf[xstrlen(buf)], classHeapSize);

	buf[xstrlen(buf)] = ' ';

	// add object heap info
	buf[xstrlen(buf)] = 'o';
	buf[xstrlen(buf)] = ':';
	StrIToA(&buf[xstrlen(buf)], heap.memSize - getUnusedMem());
	buf[xstrlen(buf)] = '/';
	StrIToA(&buf[xstrlen(buf)], heap.memSize);

	rect.topLeft.x = 80;
	rect.topLeft.y = 0;
	rect.extent.x = 80;
	rect.extent.y = 15;
	WinEraseRectangle(&rect,0);
	WinDrawChars(buf, xstrlen(buf), 80, 0);
	}
#endif

//
// Image
//
// var[0] = Class
// var[1] = width
// var[2] = height
// var[3] = hook var - winHandle
//

#define WOBJ_ImageWidth(o) (objectPtr(o))[1].intValue
#define WOBJ_ImageHeight(o) (objectPtr(o))[2].intValue
#define WOBJ_ImageWinHandle(o) (objectPtr(o))[3].refValue

static Var ImageFree(Var stack[])
	{
	WObject image;
	Var v;

	image = stack[0].obj;
	ImageDestroy(image);
	WOBJ_ImageWidth(image) = 0;
	WOBJ_ImageHeight(image) = 0;
	v.obj = 0;
	return v;
	}

static void ImageLoadBMP(WObject image, uchar *p);

static Var ImageSetPixels(Var stack[]);

static Var ImageLoad(Var stack[])
	{
	WObject image;
	UtfString path;
	WObject pathString;
	uint16 pathLen;
	uchar *p;
	Var v;

	image = stack[0].obj;
	// NOTE: we don't have to free an existing bitmap because this is only called
	// from an image constructor
	WOBJ_ImageWinHandle(image) = NULL;
	WOBJ_ImageWidth(image) = 0;
	WOBJ_ImageHeight(image) = 0;
	pathString = stack[1].obj;
	v.obj = 0;
	path = stringToUtf(pathString, STU_USE_STATIC);
	if (path.len == 0)
		return v;
	pathLen = path.len;	
	p = lockWarpRec(path.str, path.len, NULL);
	if (p == NULL)
		return v;
	ImageLoadBMP(image, p);
	unlockWarpRec(p, pathLen);
	return v;
	}

static Var ImageCreate(Var stack[])
	{
	WObject image;
	int32 width, height;
	WinHandle imgHandle;
	Word error;
	Var v;

	image = stack[0].obj;
	width = WOBJ_ImageWidth(image);
	height = WOBJ_ImageHeight(image);
	if (width > 0 && height > 0)
		{
		imgHandle = WinCreateOffscreenWindow((SWord)width, (SWord)height,
			screenFormat, &error);
		if (error != 0)
			imgHandle = NULL;
		}
	else
		imgHandle = NULL;
	WOBJ_ImageWinHandle(image) = imgHandle;
	v.obj = 0;
	return v;
	}

static void ImageDestroy(WObject image)
	{
	WinHandle winHandle;

	winHandle = WOBJ_ImageWinHandle(image);
	if (winHandle == NULL)
		return;
	WinDeleteWindow(winHandle, false);
	WOBJ_ImageWinHandle(image) = NULL;
	}

static void drawScanline(int y, int width, int bpp, uchar *p)
	{
	int mask, firstBit, step;
	int byt, bit, x, x1, x2;

	if (bpp == 1)
		{
		mask = 0x1;
		firstBit = 7;
		step = 1;
		}
	else if (bpp == 4)
		{
		mask = 0x08;
		firstBit = 4;
		step = 4;
		}
	else // bpp == 8
		{
		mask = 0x80;
		firstBit = 0;
		step = 8;
		}
	bit = firstBit;
	byt = *p++;
	x1 = -1;
	x2 = -1;
	x = 0;
	while (1)
		{
		if (((mask << bit) & byt) == 0)
			{
			// white
			if (x2 != -1)
				WinEraseLine(x1, y, x2, y);
			x1 = -1;
			x2 = -1;
			}
		else
			{
			// black
			if (x2 == -1)
				{
				x1 = x;
				x2 = x;
				}
			else
				x2 = x;
			}
		if (++x >= width)
			break;
		if (bit == 0)
			{
			bit = firstBit;
			byt = *p++;
			}
		else
			bit -= step;
		}
	if (x2 != -1)
		WinEraseLine(x1, y, x2, y);
	}

static Var ImageSetPixels(Var stack[])
	{
	WObject image, pixelsArray;
	int32 i, bpp, bytesPerRow, numRows, y, imageWidth;
	WinHandle imgHandle, oldWinHandle;
	RectangleType rect;
	uchar *p;
	Var v;

	v.obj = 0;
	image = stack[0].obj;
	bpp = stack[1].intValue;
	// NOTE: colorMapArray = stack[2] unused
	bytesPerRow = stack[3].intValue;
	numRows = stack[4].intValue;
	y = stack[5].intValue;
	pixelsArray = stack[6].obj;

	// validate parameters
	imgHandle = WOBJ_ImageWinHandle(image);
	if (imgHandle == NULL)
		return v;
	imageWidth = WOBJ_ImageWidth(image);
	if ((bytesPerRow * 8) / bpp < imageWidth)
		return v;
	if (pixelsArray == 0)
		return v;
	if (WOBJ_arrayLen(pixelsArray) < bytesPerRow * numRows)
		return v;
	if (bpp != 1 && bpp != 4 && bpp != 8)
		return v;
	oldWinHandle = WinGetDrawWindow();
	WinSetDrawWindow(imgHandle);
	rect.topLeft.x = 0;
	rect.topLeft.y = y;
	rect.extent.x = imageWidth;
	rect.extent.y = numRows;
	WinDrawRectangle(&rect, 0);
	p = (uchar *)WOBJ_arrayStart(pixelsArray);
	for (i = 0; i < numRows; i++)
		{
		drawScanline(y + i, imageWidth, bpp, p);
		p += bytesPerRow;
		}
	WinSetDrawWindow(oldWinHandle);
	}

// Intel-architecture getUInt32 - these are not #defines to make the executable smaller

static uint32 inGetUInt32(uchar *b)
	{
	return (uint32)( (uint32)b[3]<<24 | (uint32)b[2]<<16 | (uint32)b[1]<<8 | (uint32)b[0] );
	}

static uint32 inGetUInt16(uchar *b)
	{
	return (uint32)( (uint16)b[1]<<8 | (uint16)b[0] );
	}

static void ImageLoadBMP(WObject image, uchar *p)
	{
	uint32 bitmapOffset, infoSize, width, height, bpp;
	uint32 compression, scanlen;
	int y;
	uchar *ip;
	RectangleType rect;
	WinPtr winPtr;
	Word error;
	WinHandle imgHandle, oldWinHandle;

	// header (54 bytes)
	// 0-1   magic chars 'BM'
	// 2-5   uint32 filesize (not reliable)
	// 6-7   uint16 0
	// 8-9   uint16 0
	// 10-13 uint32 bitmapOffset
	// 14-17 uint32 info size
	// 18-21 int32  width
	// 22-25 int32  height
	// 26-27 uint16 nplanes
	// 28-29 uint16 bits per pixel
	// 30-33 uint32 compression flag
	// 34-37 uint32 image size in bytes
	// 38-41 int32  biXPelsPerMeter
	// 32-45 int32  biYPelsPerMeter
	// 46-49 uint32 colors used
	// 50-53 uint32 important color count

	if (p[0] != 'B' || p[1] != 'M')
		return; // not a BMP file
	bitmapOffset = inGetUInt32(&p[10]);
	infoSize = inGetUInt32(&p[14]);
	if (infoSize != 40)
		return; // old-style BMP
	width = inGetUInt32(&p[18]);
	height = inGetUInt32(&p[22]);
	if (width > 65535 || height > 65535)
		return; // bad width/height
	bpp = inGetUInt16(&p[28]);
	if (bpp != 1 && bpp != 4 && bpp != 8)
		return; // not a 2, 16 or 256 color image
	compression = inGetUInt32(&p[30]);
	if (compression != 0)
		return; // compressed image
	scanlen = (width * bpp + 7) / 8; // # bytes
	scanlen = ((scanlen + 3) / 4) * 4; // end on 32 bit boundry
	imgHandle = WinCreateOffscreenWindow((SWord)width, (SWord)height,
		screenFormat, &error);
	if (error != 0)
		return;
	WOBJ_ImageWinHandle(image) = imgHandle;
	WOBJ_ImageWidth(image) = width;
	WOBJ_ImageHeight(image) = height;

	oldWinHandle = WinGetDrawWindow();
	WinSetDrawWindow(imgHandle);
	rect.topLeft.x = 0;
	rect.topLeft.y = 0;
	rect.extent.x = width;
	rect.extent.y = height;
	WinDrawRectangle(&rect, 0);
	ip = &p[bitmapOffset];
	for (y = (int)height - 1; y >= 0; y--)
		{
		drawScanline(y, width, bpp, ip);
		ip += scanlen;
		}
	WinSetDrawWindow(oldWinHandle);
	}

//
// Graphics
//
// var[0] = Class
// var[1] = Surface
// var[2] = hook var - 1 for window surface and 2 for image
// var[3] = hook var - rgb
// var[4] = hook var - has clip
// var[5] = hook var - clipX
// var[6] = hook var - clipY
// var[7] = hook var - clipWidth
// var[8] = hook var - clipHeight
// var[9] = hook var - PALM Font ID
// var[10] = hook var - drawing op
// var[11] = hook var - x translation
// var[12] = hook var - y translation
//

#define WOBJ_GraphicsSurface(o) (objectPtr(o))[1].obj
#define WOBJ_GraphicsSurfType(o) (objectPtr(o))[2].intValue
#define WOBJ_GraphicsRGB(o) (objectPtr(o))[3].intValue
#define WOBJ_GraphicsHasClip(o) (objectPtr(o))[4].intValue
#define WOBJ_GraphicsClipX(o) (objectPtr(o))[5].intValue
#define WOBJ_GraphicsClipY(o) (objectPtr(o))[6].intValue
#define WOBJ_GraphicsClipWidth(o) (objectPtr(o))[7].intValue
#define WOBJ_GraphicsClipHeight(o) (objectPtr(o))[8].intValue
#define WOBJ_GraphicsFontID(o) (objectPtr(o))[9].refValue
#define WOBJ_GraphicsDrawOp(o) (objectPtr(o))[10].intValue
#define WOBJ_GraphicsTransX(o) (objectPtr(o))[11].intValue
#define WOBJ_GraphicsTransY(o) (objectPtr(o))[12].intValue

#define DRAW_OVER 1
#define DRAW_AND 2
#define DRAW_OR 3
#define DRAW_XOR 4

#define GR_FILLRECT   0
#define GR_DRAWLINE   1
#define GR_FILLPOLY   2
#define GR_DRAWCHARS  3
#define GR_DRAWSTRING 4
#define GR_DOTS       5
#define GR_COPYRECT   6
#define GR_DRAWCURSOR 7

static Var GraphicsCreate(Var stack[])
	{
	Var v;
	WObject gr, surface;

	gr = stack[0].obj;
	surface = WOBJ_GraphicsSurface(gr);
	WOBJ_GraphicsSurfType(gr) = SurfaceGetType(surface);
	WOBJ_GraphicsRGB(gr) = 0;
	WOBJ_GraphicsHasClip(gr) = 0;
	WOBJ_GraphicsFontID(gr) = (void *)stdFont;
	WOBJ_GraphicsDrawOp(gr) = DRAW_OVER;
	WOBJ_GraphicsTransX(gr) = 0;
	WOBJ_GraphicsTransY(gr) = 0;
	v.obj = 0;
	return v;
	}

#define GraphicsFree Return0Func

static Var GraphicsSetFont(Var stack[])
	{
	WObject gr, font;
	Var v;

	gr = stack[0].obj;
	font = stack[1].obj;
	WOBJ_GraphicsFontID(gr) = (void *)getPalmFontID(font);
	v.obj = 0;
	return v;
	}

static Var GraphicsSetColor(Var stack[])
	{
	Var v;
	WObject gr;
	int32 r, g, b;

	gr = stack[0].obj;
	r = stack[1].intValue;
	g = stack[2].intValue;
	b = stack[3].intValue;
	WOBJ_GraphicsRGB(gr) = (r & 0xFF) << 16 | ((g & 0xFF) << 8) | (b & 0xFF);
	v.obj = 0;
	return v;
	}

static Var GraphicsSetDrawOp(Var stack[])
	{
	Var v;
	WObject gr;
	int32 op;

	gr = stack[0].obj;
	op = stack[1].intValue;
	WOBJ_GraphicsDrawOp(gr) = op;
	v.obj = 0;
	return v;
	}

static Var GraphicsSetClip(Var stack[])
	{
	WObject gr;
	int32 transX, transY;
	Var v;


	gr = stack[0].obj;
	transX = WOBJ_GraphicsTransX(gr);
	transY = WOBJ_GraphicsTransY(gr);
	WOBJ_GraphicsHasClip(gr) = 1;
	// clip X and Y are stored in absolute coordinates
	WOBJ_GraphicsClipX(gr) = stack[1].intValue + transX;
	WOBJ_GraphicsClipY(gr) = stack[2].intValue + transY;
	WOBJ_GraphicsClipWidth(gr) = stack[3].intValue;
	WOBJ_GraphicsClipHeight(gr) = stack[4].intValue;
	v.obj = 0;
	return v;
	}

static Var GraphicsGetClip(Var stack[])
	{
	WObject gr, rect;
	Var v;

	v.obj = 0;
	gr = stack[0].obj;
	rect = stack[1].obj;
	if (rect == 0 || WOBJ_GraphicsHasClip(gr) != 1)
		return v;
	WOBJ_RectX(rect) = WOBJ_GraphicsClipX(gr) - WOBJ_GraphicsTransX(gr);
	WOBJ_RectY(rect) = WOBJ_GraphicsClipY(gr) - WOBJ_GraphicsTransY(gr);
	WOBJ_RectWidth(rect) = WOBJ_GraphicsClipWidth(gr);
	WOBJ_RectHeight(rect) = WOBJ_GraphicsClipHeight(gr);
	v.obj = rect;
	return v;
	}

static Var GraphicsClearClip(Var stack[])
	{
	WObject gr;
	Var v;

	gr = stack[0].obj;
	WOBJ_GraphicsHasClip(gr) = 0;
	v.obj = 0;
	return v;
	}

static Var GraphicsTranslate(Var stack[])
	{
	WObject gr;
	Var v;

	gr = stack[0].obj;
	WOBJ_GraphicsTransX(gr) += stack[1].intValue;
	WOBJ_GraphicsTransY(gr) += stack[2].intValue;
	v.obj = 0;
	return v;
	}

static Var GraphicsDraw(int type, Var stack[])
	{
	WObject gr, surface;
	int32 surfaceType, drawOp, transX, transY;
	uint32 rgb;
	WinHandle winHandle, oldWinHandle;
	RectangleType oldClip;
	Var v;

	v.obj = 0;
	gr = stack[0].obj;
	rgb = WOBJ_GraphicsRGB(gr);
	if (rgb != 0)
		{
		int r, g, b;
		
		r = rgb >> 16;
		g = (rgb >> 8) & 0xFF;
		b = rgb & 0xFF;
		if (r < 127 || g < 127 || b < 127)
			rgb = 0;
		else
			rgb = 1;
		}
	surface = WOBJ_GraphicsSurface(gr);
	surfaceType = WOBJ_GraphicsSurfType(gr);
	if (surfaceType == SURF_MAINWIN)
		winHandle = WinGetDrawWindow();
	else if (surfaceType == SURF_IMAGE)
		{
		oldWinHandle = WinGetDrawWindow();
		winHandle = WOBJ_ImageWinHandle(surface);
		if (winHandle == NULL)
			return v;
		WinSetDrawWindow(winHandle);
		}
	else
		return v;

	// set clip if one exists
	if (WOBJ_GraphicsHasClip(gr) != 0)
		{
		RectangleType rect;

		WinGetClip(&oldClip);
		rect.topLeft.x = WOBJ_GraphicsClipX(gr);
		rect.topLeft.y = WOBJ_GraphicsClipY(gr);
		rect.extent.x = WOBJ_GraphicsClipWidth(gr);
		rect.extent.y = WOBJ_GraphicsClipHeight(gr);
		WinClipRectangle(&rect);
		WinSetClip(&rect);
		}

	transX = WOBJ_GraphicsTransX(gr);
	transY = WOBJ_GraphicsTransY(gr);

	drawOp = WOBJ_GraphicsDrawOp(gr);
	// NOTE: only DRAW_OVER is supported for drawing lines, rectangles and
	// text under PalmOS so drawOp is only used for GR_COPYRECT
	switch(type)
		{
		case GR_FILLRECT:
			{
			RectangleType rect;

			rect.topLeft.x = stack[1].intValue + transX;
			rect.topLeft.y = stack[2].intValue + transY;
			rect.extent.x = stack[3].intValue;
			rect.extent.y = stack[4].intValue;
			if (rect.extent.x <= 0 || rect.extent.y <= 0)
				break;
			if (rgb == 1)
				WinEraseRectangle(&rect, 0);
			else
				WinDrawRectangle(&rect, 0);
			break;
			}
		case GR_DRAWLINE:
			{
			if (rgb == 1)
				WinEraseLine(stack[1].intValue + transX, stack[2].intValue + transY,
					stack[3].intValue + transX, stack[4].intValue + transY);
			else
				WinDrawLine(stack[1].intValue + transX, stack[2].intValue + transY,
					stack[3].intValue + transX, stack[4].intValue + transY);
			break;
			}
		case GR_FILLPOLY:
			{
			WObject xArray, yArray;
			int32 i, count, *x, *y;

			// to save space, we don't have a full implementation of fillpoly, we
			// just draw the outline
			xArray = stack[1].obj;
			yArray = stack[2].obj;
			if (xArray == 0 || yArray == 0)
				break;
			x = (int32 *)WOBJ_arrayStart(xArray);
			y = (int32 *)WOBJ_arrayStart(yArray);
			count = stack[3].intValue;
			if (count < 3 || count > WOBJ_arrayLen(xArray) ||
				count > WOBJ_arrayLen(yArray))
				break;
			for (i = 0; i < count - 1; i++)
				WinDrawLine(x[i] + transX, y[i] + transY,
					x[i + 1] + transX, y[i + 1] + transY);
			WinDrawLine(x[0] + transX, y[0] + transY, x[i] + transX, y[i] + transY);
			break;
			}
		case GR_DRAWCHARS:
		case GR_DRAWSTRING:
			{
			WObject string, charArray;
			int32 start, count;
			uint16 *chars;
			int32 x, y, i, n;
			FontID fontID, prevFontID;
			char buf[40];

			if (type == GR_DRAWSTRING)
				{
				string = stack[1].obj;
				if (string == 0)
					break;
				x = stack[2].intValue;
				y = stack[3].intValue;
				charArray = WOBJ_StringCharArrayObj(string);
				if (charArray == 0)
					break;
				start = 0;
				count = WOBJ_arrayLen(charArray);
				}
			else
				{
				charArray = stack[1].obj;
				start = stack[2].intValue;
				count = stack[3].intValue;
				x = stack[4].intValue;
				y = stack[5].intValue;
				if (arrayRangeCheck(charArray, start, count) == 0)
					break; // array null or range invalid
				}
			chars = (uint16 *)WOBJ_arrayStart(charArray);
			chars = &chars[start];
			x += transX;
			y += transY;
			fontID = (FontID)WOBJ_GraphicsFontID(gr);
			prevFontID = FntSetFont(fontID);
			while (count > 0)
				{
				n = sizeof(buf);
				if (n > count)
					n = count;
				for (i = 0; i < n; i++)
					buf[i] = (char)chars[i];
				if (rgb == 1)
					WinEraseChars(buf, n, x, y);
				else
					WinDrawChars(buf, n, x, y);
				x += FntCharsWidth(buf, (Word)n);
				count -= n;
				chars += n;
				}
			FntSetFont(prevFontID);
			break;
			}
		case GR_DOTS:
			{
			int32 x1, y1, x2, y2;
			int32 x, y;

			x1 = stack[1].intValue + transX;
			y1 = stack[2].intValue + transY;
			x2 = stack[3].intValue + transX;
			y2 = stack[4].intValue + transY;
			if (x1 == x2)
				{
				// vertical
				if (y1 > y2)
					{
					y = y1;
					y1 = y2;
					y2 = y;
					}
				// NOTE: I tried WinFillLine() and could never get it to draw
				// just the pixels I wanted.
				for (; y1 <= y2; y1 += 2)
					{
					if (rgb == 1)
						WinEraseLine(x1, y1, x1, y1);
					else
						WinDrawLine(x1, y1, x1, y1);
					}
				}
			else if (y1 == y2)
				{
				// horitzontal
				if (x1 > x2)
					{
					x = x1;
					x1 = x2;
					x2 = x;
					}
				for (; x1 <= x2; x1 += 2)
					{
					if (rgb == 1)
						WinEraseLine(x1, y1, x1, y1);
					else
						WinDrawLine(x1, y1, x1, y1);
					}
				}
			break;
			}
		case GR_COPYRECT:
			{
			WObject srcSurf;
			RectangleType rect;
			int32 dstX, dstY;
			int srcSurfaceType;
			WinHandle srcWinHandle;
			ScrOperation mode;

			srcSurf = stack[1].obj;
			rect.topLeft.x = stack[2].intValue;
			rect.topLeft.y = stack[3].intValue;
			rect.extent.x = stack[4].intValue;
			rect.extent.y = stack[5].intValue;
			dstX = stack[6].intValue + transX;
			dstY = stack[7].intValue + transY;
			if (srcSurf == 0)
				break;
			// convert op to native op
			if (drawOp == DRAW_OVER)
				mode = scrCopy;
			else if (drawOp == DRAW_XOR)
				mode = scrXOR;
			else if (drawOp == DRAW_AND)
				mode = scrAND;
			else if (drawOp == DRAW_OR)
				mode = scrOR;
			if (surface == srcSurf)
				srcWinHandle = winHandle;
			else
				{
				srcSurfaceType = SurfaceGetType(srcSurf);
				srcWinHandle = 0;
				if (srcSurfaceType == SURF_MAINWIN)
					srcWinHandle = WinGetDrawWindow();
				else if (srcSurfaceType == SURF_IMAGE)
					srcWinHandle = WOBJ_ImageWinHandle(srcSurf);
				}
			if (srcWinHandle != NULL)
				WinCopyRectangle(srcWinHandle, winHandle, &rect, dstX, dstY, mode);
			break;
			}
		case GR_DRAWCURSOR:
			{
			int32 dstX, dstY;
			RectangleType rect;
			WinHandle imgHandle;
			Word error;

			// PalmOS only supports XOR for drawing images, so we create a
			// temporary empty image here and draw it XORed
			dstX = stack[1].intValue + transX;
			dstY = stack[2].intValue + transY;
			rect.topLeft.x = 0;
			rect.topLeft.y = 0;
			rect.extent.x = stack[3].intValue;
			rect.extent.y = stack[4].intValue;
			imgHandle = WinCreateOffscreenWindow(rect.extent.x, rect.extent.y,
				screenFormat, &error);
			if (error != 0)
				break;
			WinSetDrawWindow(imgHandle);
			WinDrawRectangle(&rect, 0);
			WinSetDrawWindow(winHandle);
			WinCopyRectangle(imgHandle, winHandle, &rect, dstX, dstY, scrXOR);
			WinDeleteWindow(imgHandle, false);
			break;
			}
		}
	if (WOBJ_GraphicsHasClip(gr) != 0)
		WinSetClip(&oldClip);
	if (surfaceType == SURF_IMAGE)
		WinSetDrawWindow(oldWinHandle);
	return v;
	}

static Var GraphicsFillRect(Var stack[])
	{
	return GraphicsDraw(GR_FILLRECT, stack);
	}

static Var GraphicsDrawLine(Var stack[])
	{
	return GraphicsDraw(GR_DRAWLINE, stack);
	}

static Var GraphicsFillPolygon(Var stack[])
	{
	return GraphicsDraw(GR_FILLPOLY, stack);
	}

static Var GraphicsDrawChars(Var stack[])
	{
	return GraphicsDraw(GR_DRAWCHARS, stack);
	}

static Var GraphicsDrawString(Var stack[])
	{
	return GraphicsDraw(GR_DRAWSTRING, stack);
	}

static Var GraphicsDrawDots(Var stack[])
	{
	return GraphicsDraw(GR_DOTS, stack);
	}

static Var GraphicsCopyRect(Var stack[])
	{
	return GraphicsDraw(GR_COPYRECT, stack);
	}

static Var GraphicsDrawCursor(Var stack[])
	{
	return GraphicsDraw(GR_DRAWCURSOR, stack);
	}

//
// File
//

#define FileGetLength Return0Func
#define FileCreateDir Return0Func
#define FileRead ReturnNeg1Func
#define FileCreate Return0Func
#define FileWrite ReturnNeg1Func
#define FileListDir Return0Func
#define FileIsDir Return0Func
#define FileClose Return0Func
#define FileDelete Return0Func
#define FileExists Return0Func
#define FileIsOpen Return0Func
#define FileSeek Return0Func
#define FileRename Return0Func

static Var Return0Func(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ReturnNeg1Func(Var stack[])
	{
	Var v;

	v.intValue = -1;
	return v;
	}

//
// Socket
//
// var[0] = Class
// var[1] = hook var - SocketRef
// var[2] = hook var - timeout (millis)
//

#define WOBJ_SocketRef(o) (objectPtr(o))[1].intValue
#define WOBJ_SocketTimeout(o) (objectPtr(o))[2].intValue

static int32 _SocketClose(WObject socket)
	{
	NetSocketRef socketRef;
	SDWord timeout;
	Err err;

	socketRef = (NetSocketRef)WOBJ_SocketRef(socket);
	if (socketRef == -1)
		return 0;
	WOBJ_SocketRef(socket) = -1;
	timeout = millisToTicks(1500);
	if (NetLibSocketClose(globalSocketLibRefNum, socketRef, timeout, &err) != 0)
		return 0;
	return 1;
	}

static Var SocketCreate(Var stack[])
	{
	WObject sock, host;
	int32 port;
	UtfString s;
	NetSocketRef socketRef;
	NetSocketAddrINType sockAddr;
	SDWord timeout;
	Err err;
	int status;
	Var v;

	v.obj = 0;
	sock = stack[0].obj;
	host = stack[1].obj;
	port = stack[2].intValue;
	WOBJ_SocketRef(sock) = -1;
	WOBJ_SocketTimeout(sock) = 1500;

	if (globalNetState == NET_NOT_READY_FOR_OPEN || globalNetState == NET_OPEN_FAILED)
		return v; // not ready or already failed - see note below
	if (globalNetState == NET_READY_FOR_OPEN)
		{
		DWord ifCreator;
		Word ifInstance;
		DWord version;
		Word refNum, ifErr;
		char *extra;

		err = FtrGet(netFtrCreator, netFtrNumVersion, &version);
		if (err == 0 )
			err = SysLibFind("Net.lib", &refNum);
		// NOTE: This code is commented out because it closes an open
		// net connection if it is called 
		//if (err == 0)
		//	err = NetLibIFGet(refNum, 0, &ifCreator, &ifInstance);
		if (err == 0)
			err = NetLibOpen(refNum, &ifErr);
		if (err == 0 || err == netErrAlreadyOpen)
			{
			if (ifErr != 0)
				NetLibClose(refNum, false);
			else
				{
				globalSocketLibRefNum = refNum;
				globalNetState = NET_IS_OPEN;
				}
			}
		if (globalNetState != NET_IS_OPEN)
			{
			// NOTE: I found that if you attempt to open the net library
			// and it fails and you try to open it again, PalmOS can reset.
			// So, we only try once during an application. You have to
			// restart if there is a problem, this keeps PalmOS from
			// resetting
			globalNetState = NET_OPEN_FAILED;
			return v;
			}
		}
	timeout = millisToTicks(1500);

	// set up sockAddr structure
	xmemzero((char *)&sockAddr, sizeof(sockAddr));
	sockAddr.family = netSocketAddrINET;
	sockAddr.port = NetHToNS((Word)port);
	s = stringToUtf(host, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (s.len == 0)
		return v;
	sockAddr.addr = NetLibAddrAToIN(globalSocketLibRefNum, s.str);
	if (sockAddr.addr == -1)
		{
		NetHostInfoBufType hostInfo;
		NetHostInfoPtr hostPtr;

		hostPtr = NetLibGetHostByName(globalSocketLibRefNum, s.str, &hostInfo,
			timeout, &err);
		if (hostPtr == NULL)
			return v;
		sockAddr.addr = *((NetIPAddr *)hostInfo.hostInfo.addrListP[0]);
		}
	socketRef = NetLibSocketOpen(globalSocketLibRefNum, netSocketAddrINET,
		netSocketTypeStream, 6, timeout, &err);
	if (socketRef == -1)
		return v;
	status = NetLibSocketConnect(globalSocketLibRefNum, socketRef,
		(NetSocketAddrType *)&sockAddr, sizeof(sockAddr), timeout, &err);
	if (status == -1)
		{
		NetLibSocketClose(globalSocketLibRefNum, socketRef, timeout, &err);
		return v;
		}

	//Boolean allUp;
	//Word asd;
	//NetLibConnectionRefresh(globalSocketLibRefNum, true, &allUp, &asd);

	WOBJ_SocketRef(sock) = socketRef;
	return v;
	}

static void SocketDestroy(WObject socket)
	{
	_SocketClose(socket);
	}

static Var SocketClose(Var stack[])
	{
	WObject socket;
	Var v;

	socket = stack[0].obj;
	v.intValue = _SocketClose(socket);
	return v;
	}

static Var SocketIsOpen(Var stack[])
	{
	WObject socket;
	Var v;

	socket = stack[0].obj;
	if (WOBJ_SocketRef(socket) == -1)
		v.intValue = 0;
	else
		v.intValue = 1;
	return v;
	}

static Var SocketSetReadTimeout(Var stack[])
	{
	WObject socket;
	int32 millis;
	Var v;

	socket = stack[0].obj;
	millis = stack[1].intValue;
	if (millis < 0)
		{
		v.intValue = 0;
		return v;
		}
	WOBJ_SocketTimeout(socket) = millis;
	v.intValue = 1;
	return v;
	}

static Var SocketReadWriteBytes(Var stack[], int isRead)
	{
	WObject socket, byteArray;
	NetSocketRef socketRef;
	int32 millis, start, count, countSoFar, n;
	SDWord timeout;
	uchar *bytes;
	Err err;
	Var v;
	
	v.intValue = -1;
	socket = stack[0].obj;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	socketRef = (NetSocketRef)WOBJ_SocketRef(socket);
	if (socketRef == -1)
		return v; // socket not open
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	bytes = &bytes[start];
	countSoFar = 0;
	millis = WOBJ_SocketTimeout(socket);
	timeout = millisToTicks(millis);
	while (countSoFar < count)
		{
		int chunkSize;
		chunkSize = count - countSoFar;
		if (chunkSize > 0x7000)
			chunkSize = 0x7000;
		if (isRead)
			{
			n = NetLibReceive(globalSocketLibRefNum, socketRef, bytes,
				chunkSize, 0, 0, 0, timeout, &err);
			if (chunkSize > 0 && (n == 0 || err == netErrSocketNotOpen))
				_SocketClose(socket); // server closed connection
			}
		else
			n = NetLibSend(globalSocketLibRefNum, socketRef, bytes,
				chunkSize, 0, NULL, 0, timeout, &err);
		if (n > 0)
			{
			countSoFar += n;
			bytes += n;
			}
		else if (n <= 0)
			{
			// NOTE: n == 0 under PalmOS when server closes connection
			if (countSoFar != 0)
				v.intValue = countSoFar;
			else if (n < 0 && err == netErrTimeout)
				v.intValue = 0;
			else
				v.intValue = -1;
			return v;
			}
		}
	v.intValue = count;
	return v;
	}

static Var SocketRead(Var stack[])
	{
	return SocketReadWriteBytes(stack, 1);
	}

static Var SocketWrite(Var stack[])
	{
	return SocketReadWriteBytes(stack, 0);
	}

//
// Sound
//
// all functions static

static Var SoundTone(Var stack[])
	{
	SndCommandType sndCmd;
	Var v;

	sndCmd.cmd = sndCmdFreqDurationAmp;
	sndCmd.param1 = stack[0].intValue; // freq
	sndCmd.param2 = stack[1].intValue; // duration
	sndCmd.param3 = sndMaxAmp;
	SndDoCmd(NULL, &sndCmd, 0);
	v.obj = 0;
	return v;
	}

static Var SoundBeep(Var stack[])
	{
	Var v;

	SndPlaySystemSound(sndInfo);
	v.obj = 0;
	return v;
	}

//
// SoundClip
//

#define SoundClipPlay Return0Func

//
// Convert
//

static Var ConvertFloatToIntBitwise(Var stack[])
	{
	return stack[0];
	}

static Var ConvertIntToFloatBitwise(Var stack[])
	{
	return stack[0];
	}

static Var ConvertStringToInt(Var stack[])
	{
	UtfString s;
	WObject string;
	Var v;

	v.intValue = 0;
	string = stack[0].obj;
	s = stringToUtf(string, STU_USE_STATIC | STU_NULL_TERMINATE);
	if (s.len == 0)
		return v;
	v.intValue = (int32)StrAToI(s.str);
	return v;
	}

static Var ConvertIntToString(Var stack[])
	{
	Var v;
	char buf[20];

	xmemzero((uchar *)buf, 20);
	StrIToA(buf, stack[0].intValue);
	v.obj = createString(buf);
	return v;
	}

static Var ConvertFloatToString(Var stack[])
	{
	Var v;
	FlpCompDouble cFloat;
	char buf[80];
	int len;

	cFloat.d = (double)stack[0].floatValue;
	FlpFToA(cFloat.fd, buf);
	len = xstrlen(buf);
	if (len > 3 && !xstrncmp(&buf[len - 3], "e00", 3))
		buf[len - 3] = 0;
	v.obj = createString(buf);
	return v;
#ifdef NEVER
	Var v;
	int32 i, len;
	char buf[60];

	xmemzero((uchar *)buf, 60);
	i = (int32)stack[0].floatValue;
	StrIToA(buf, i);
	len = xstrlen(buf);
	buf[len++] = '.';
	i = (int32)((stack[0].floatValue - (float32)i) * 100000.0);
	if (i < 0)
		i = - i;
	if (i < 10)
		buf[len++] = '0';
	if (i < 100)
		buf[len++] = '0';
	if (i < 1000)
		buf[len++] = '0';
	if (i < 10000)
		buf[len++] = '0';
	StrIToA(&buf[len], i);
	v.obj = createString(buf);
	return v;
#endif
	}

static Var ConvertCharToString(Var stack[])
	{
	Var v;
	char buf[2];

	buf[0] = (char)stack[0].intValue;
	buf[1] = 0;
	v.obj = createString(buf);
	return v;
	}

static Var ConvertBooleanToString(Var stack[])
	{
	Var v;
	char *s;

	if (stack[0].intValue == 0)
		s = "false";
	else
		s = "true";
	v.obj = createString(s);
	return v;
	}

//
// Catalog
//
// var[0] = Class
// var[1] = hook var - DmOpenRef (null if no open db)
// var[2] = hook var - current record pos (or -1 if none)
// var[3] = hook var - VoidHand current record handle
// var[4] = hook var - VoidPtr current record pointer
// var[5] = hook var - current offset in record
// var[6] = hook var - length of current record
// var[7] = hook var - 1 if record has been modified, 0 otherwise
#define WOBJ_CatalogDmRef(o) (objectPtr(o))[1].refValue
#define WOBJ_CatalogCurRecPos(o) (objectPtr(o))[2].intValue
#define WOBJ_CatalogCurRecHandle(o) (objectPtr(o))[3].refValue
#define WOBJ_CatalogCurRecPtr(o) (objectPtr(o))[4].refValue
#define WOBJ_CatalogCurRecOffset(o) (objectPtr(o))[5].intValue
#define WOBJ_CatalogCurRecLen(o) (objectPtr(o))[6].intValue
#define WOBJ_CatalogCurRecModified(o) (objectPtr(o))[7].intValue

#define Catalog_READ_ONLY 1
#define Catalog_WRITE_ONLY 2
#define Catalog_READ_WRITE 3
#define Catalog_CREATE 4

static void _RecClose(WObject cat)
	{
	DmOpenRef dmRef;
	VoidHand recH;
	int32 pos;
	Boolean dirty;

	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return; // no current record
	recH = WOBJ_CatalogCurRecHandle(cat);
	MemHandleUnlock(recH);
	dmRef = WOBJ_CatalogDmRef(cat);
	if (WOBJ_CatalogCurRecModified(cat) != 0)
		dirty = true;
	else
		dirty = false;
	DmReleaseRecord(dmRef, pos, dirty);
	WOBJ_CatalogCurRecPos(cat) = -1;	
	}

static int32 _CatalogClose(WObject cat)
	{
	DmOpenRef dmRef;

	_RecClose(cat); // release any open records
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		return 0;
	DmCloseDatabase(dmRef);
	WOBJ_CatalogDmRef(cat) = 0;
	return 1;
	}

static DmOpenRef _CatalogOpenDB(char *name, ULong dbCreator, ULong dbType)
	{
	DmSearchStateType state;
	Boolean first;
	UInt cardNo;
	LocalID dbID;
	char dbName[32];
	Err err;

	first = true;
	while (1)
		{
		err = DmGetNextDatabaseByTypeCreator(first, &state,
			dbType, dbCreator, false, &cardNo, &dbID);
		if (err == dmErrCantFind)
			return NULL;
	 	if (DmDatabaseInfo(cardNo, dbID, dbName, NULL,
			NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL) == 0)
			{
			if (!StrCompare(name, dbName))
				return DmOpenDatabase(cardNo, dbID, dmModeReadWrite);
			}
		first = false;
		}
	}

static Var CatalogCreate(Var stack[])
	{
	UtfString s;
	WObject cat, name;
	int32 mode;
	DmOpenRef dmRef;
	ULong dbType, dbCreator;
	Var v;

	v.obj = 0;
	cat = stack[0].obj;
	name = stack[1].obj;
	mode = stack[2].intValue;
	WOBJ_CatalogDmRef(cat) = 0;
	WOBJ_CatalogCurRecPos(cat) = -1;
	s = stringToUtf(name, STU_USE_STATIC | STU_NULL_TERMINATE);
	if (s.len == 0)
		return v;
	dbCreator = appCreatorId;
	dbType = 'DATA';
	if (s.len > 10 && s.str[s.len - 10] == '.' && s.str[s.len - 5] == '.')
		{
		s.str[s.len - 10] = 0;
		dbCreator = getUInt32((uchar *)&s.str[s.len - 9]);
		dbType = getUInt32((uchar *)&s.str[s.len - 4]);
		}
	dmRef = _CatalogOpenDB(s.str, dbCreator, dbType);
	if (mode == Catalog_CREATE && !dmRef)
		{
		if (DmCreateDatabase(0, s.str, dbCreator, dbType, false) != 0)
			return v;
		dmRef = _CatalogOpenDB(s.str, dbCreator, dbType);
		// set the backup bit on the database
		if (dmRef != 0)
			{
			LocalID dbID;
			UInt cardNo;
			Word attributes;

			DmOpenDatabaseInfo(dmRef, &dbID, NULL, NULL, &cardNo, NULL);
			DmDatabaseInfo(cardNo, dbID, NULL, &attributes,
				NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
			attributes |= dmHdrAttrBackup;
			DmSetDatabaseInfo(cardNo, dbID, NULL, &attributes,
				NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
			}
		}
	if (dmRef == 0)
		return v;
	WOBJ_CatalogDmRef(cat) = dmRef;
	return v;
	}

static Var CatalogIsOpen(Var stack[])
	{
	WObject cat;
	Var v;
	DmOpenRef dmRef;

	cat = stack[0].obj;
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		v.intValue = 0;
	else
		v.intValue = 1;
	return v;
	}

static void CatalogDestroy(WObject cat)
	{
	_CatalogClose(cat);
	}

static Var CatalogClose(Var stack[])
	{
	WObject cat;
	Var v;

	cat = stack[0].obj;
	v.intValue = _CatalogClose(cat);
	return v;
	}

static Var CatalogDelete(Var stack[])
	{
	WObject cat;
	DmOpenRef dmRef;
	LocalID localID;
	UInt cardNo;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		return v;
	DmOpenDatabaseInfo(dmRef, &localID, NULL, NULL, &cardNo, NULL);
	_CatalogClose(cat);
	if (DmDeleteDatabase(cardNo, localID) != 0)
		return v;
	v.intValue = 1;
	return v;
	}

static void _ULongToChars(ULong l, char *buf)
	{
	buf[0] = l >> 24;
	buf[1] = (l >> 16) & 0xFF;
	buf[2] = (l >> 8) & 0xFF;
	buf[3] = l & 0xFF;
	}

static Var CatalogListCatalogs(Var stack[])
	{
	WObject stringArray, *strings;
	DmSearchStateType state;
	Boolean first;
	UInt cardNo;
	LocalID dbID;
	ULong dbType, dbCreator;
	int n, len;
	char name[32 + 10];
	Var v;

	v.obj = 0;
	n = 0;
	first = true;
	while (DmGetNextDatabaseByTypeCreator(first,
		&state, 0, 0, false, &cardNo, &dbID) == 0)
		{
		first = false;
		n++;
		}
	if (n == 0)
		return v;
	stringArray = createArrayObject(1, n);
	if (pushObject(stringArray) == -1)
		return v;
	n = 0;
	first = true;
	while (DmGetNextDatabaseByTypeCreator(first, &state,
			0, 0, false, &cardNo, &dbID) == 0)
		{
		first = false;
		// we need to recompute the start pointer each iteration
		// in case garbage collection during a string create causes
		// memory to move around
		strings = (WObject *)WOBJ_arrayStart(stringArray);
		name[0] = 0;
	 	DmDatabaseInfo(cardNo, dbID, name, NULL, NULL, NULL, NULL,
			NULL, NULL, NULL, NULL, &dbType, &dbCreator);
		len = xstrlen(name);
		name[len++] = '.';
		_ULongToChars(dbCreator, &name[len]);
		len += 4;
		name[len++] = '.';
		_ULongToChars(dbType, &name[len]);
		len += 4;
		name[len++] = 0;
		strings[n++] = createString(name);
		}
	popObject(); // stringArray
	v.obj = stringArray;
	return v;
	}

static Var CatalogGetRecordSize(Var stack[])
	{
	WObject cat;
	int32 pos;
	Var v;

	cat = stack[0].obj;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		{
		v.intValue = -1;
		return v;
		}
	v.intValue = WOBJ_CatalogCurRecLen(cat);
	return v;
	}

static Var CatalogGetRecordCount(Var stack[])
	{
	WObject cat;
	DmOpenRef dmRef;
	Var v;

	cat = stack[0].obj;
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		{
		v.intValue = -1;
		return v;
		}
	v.intValue = DmNumRecords(dmRef);
	return v;
	}

static Var CatalogDeleteRecord(Var stack[])
	{
	WObject cat;
	int32 pos;
	DmOpenRef dmRef;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return v;
	_RecClose(cat);
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	// NOTE: Was DmDeleteRecord() but this didn't match with CE
	// and was too difficult to use in actual programs since it
	// didn't delete the record immediately
	if (DmRemoveRecord(dmRef, pos) != 0)
		return v;
	v.intValue = 1;
	return v;
	}

static Var CatalogResizeRecord(Var stack[])
	{
	WObject cat;
	DmOpenRef dmRef;
	VoidHand recH;
	VoidPtr recPtr;
	int32 pos, size;
	Var v;

	cat = stack[0].obj;
	size = stack[1].intValue;
	v.intValue = 0;
	if (size < 0)
		return v;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return v;
	_RecClose(cat);
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		return v;
	recH = DmResizeRecord(dmRef, pos, (ULong)size);
	if (recH == NULL)
		return v;
	recPtr = MemHandleLock(recH);
	if (recPtr == NULL)
		return v;
	WOBJ_CatalogCurRecPos(cat) = pos;
	WOBJ_CatalogCurRecHandle(cat) = recH;
	WOBJ_CatalogCurRecPtr(cat) = recPtr;
	WOBJ_CatalogCurRecLen(cat) = size;
	WOBJ_CatalogCurRecModified(cat) = true;
	v.intValue = 1;
	return v;
	}

static Var CatalogAddRecord(Var stack[])
	{
	WObject cat;
	DmOpenRef dmRef;
	VoidHand recH;
	VoidPtr recPtr;
	UInt pos;
	int32 size;
	Var v;

	cat = stack[0].obj;
	size = stack[1].intValue;
	v.intValue = -1;
	_RecClose(cat);
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		return v;
	pos = DmNumRecords(dmRef);
	recH = DmNewRecord(dmRef, &pos, size);
	if (!recH)
		return v;
	recPtr = MemHandleLock(recH);
	if (!recPtr)
		{
		DmDeleteRecord(dmRef, pos);
		return v;
		}
	WOBJ_CatalogCurRecPos(cat) = pos;
	WOBJ_CatalogCurRecHandle(cat) = recH;
	WOBJ_CatalogCurRecPtr(cat) = recPtr;
	WOBJ_CatalogCurRecOffset(cat) = 0;
	WOBJ_CatalogCurRecLen(cat) = size;
	WOBJ_CatalogCurRecModified(cat) = true;
	v.intValue = pos;
	return v;
	}

static Var CatalogSetRecordPos(Var stack[])
	{
	WObject cat;
	int32 pos, count;
	DmOpenRef dmRef;
	VoidHand recH;
	VoidPtr recPtr;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	pos = stack[1].intValue;
	_RecClose(cat);
	if (pos < 0)
		return v;
	dmRef = (DmOpenRef)WOBJ_CatalogDmRef(cat);
	if (dmRef == 0)
		return v;
	count = (int32)DmNumRecords(dmRef);
	if (pos >= count)
		return v;
	recH = DmGetRecord(dmRef, pos);
	if (!recH)
		return v;
	recPtr = MemHandleLock(recH);
	if (!recPtr)
		{
		DmReleaseRecord(dmRef, pos, false);
		return v;
		}
	WOBJ_CatalogCurRecPos(cat) = pos;
	WOBJ_CatalogCurRecHandle(cat) = recH;
	WOBJ_CatalogCurRecPtr(cat) = recPtr;
	WOBJ_CatalogCurRecOffset(cat) = 0;
	WOBJ_CatalogCurRecLen(cat) = MemHandleSize(recH);
	WOBJ_CatalogCurRecModified(cat) = 0;
	v.intValue = 1;
	return v;
	}

static Var CatalogReadWriteBytes(Var stack[], int isRead)
	{
	WObject cat, byteArray;
	int32 start, count, pos, recOffset;
	uchar *bytes, *recBytes;
	Var v;

	v.intValue = -1;
	cat = stack[0].obj;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return v; // no current record
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	recBytes = WOBJ_CatalogCurRecPtr(cat);
	recOffset = WOBJ_CatalogCurRecOffset(cat);
	if (recOffset + count > WOBJ_CatalogCurRecLen(cat))
		return v; // past end of record
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	if (isRead)
		xmemmove(&bytes[start], &recBytes[recOffset], count);
	else
		{
		if (DmWrite(recBytes, recOffset, &bytes[start], count) != 0)
			return v;
		WOBJ_CatalogCurRecModified(cat) = 1;
		}
	WOBJ_CatalogCurRecOffset(cat) = recOffset + count;
	v.intValue = count;
	return v;
	}

static Var CatalogRead(Var stack[])
	{
	return CatalogReadWriteBytes(stack, 1);
	}

static Var CatalogWrite(Var stack[])
	{
	return CatalogReadWriteBytes(stack, 0);
	}

static Var CatalogSkipBytes(Var stack[])
	{
	WObject cat;
	int32 count, pos, offset;
	Var v;

	v.intValue = -1;
	cat = stack[0].obj;
	count = stack[1].obj;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (count < 0 || pos == -1)
		return v;
	offset = WOBJ_CatalogCurRecOffset(cat);
	if (offset + count > WOBJ_CatalogCurRecLen(cat))
		return v;
	WOBJ_CatalogCurRecOffset(cat) += count;
	v.intValue = count;
	return v;
	}

//
// Time
//
// var[0] = Class
// var[1] = int year
// var[2] = int month
// var[3] = int day
// var[4] = int hour
// var[5] = int minute
// var[6] = int second
// var[7] = int millis

#define WOBJ_TimeYear(o) (objectPtr(o))[1].intValue
#define WOBJ_TimeMonth(o) (objectPtr(o))[2].intValue
#define WOBJ_TimeDay(o) (objectPtr(o))[3].intValue
#define WOBJ_TimeHour(o) (objectPtr(o))[4].intValue
#define WOBJ_TimeMinute(o) (objectPtr(o))[5].intValue
#define WOBJ_TimeSecond(o) (objectPtr(o))[6].intValue
#define WOBJ_TimeMillis(o) (objectPtr(o))[7].intValue

static Var TimeCreate(Var stack[])
	{
	Var v;
	WObject time;
	DateTimeType nowTM;

	time = stack[0].obj;
	TimSecondsToDateTime(TimGetSeconds(), &nowTM);
	WOBJ_TimeYear(time) = nowTM.year;
	WOBJ_TimeMonth(time) = nowTM.month;
	WOBJ_TimeDay(time) = nowTM.day;
	WOBJ_TimeHour(time) = nowTM.hour;
	WOBJ_TimeMinute(time) = nowTM.minute;
	WOBJ_TimeSecond(time) = nowTM.second;
	WOBJ_TimeMillis(time) = getTimeStamp() % 1000;
	v.obj = 0;
	return v;
	}

//
// SerialPort
//
// var[0] = Class
// var[1] = hook var - serial library reference number
// var[2] = hook var - read timeout in millis
//
#define WOBJ_SerialPortRefNum(o) (objectPtr(o))[1].intValue
#define WOBJ_SerialPortTimeout(o) (objectPtr(o))[2].intValue

static int32 _SerialPortClose(WObject port)
	{
	UInt refNum;

	if (WOBJ_SerialPortRefNum(port) == -1)
		return 0;
	refNum = (UInt)WOBJ_SerialPortRefNum(port);
	SerSendWait(refNum, -1); // flush buffer
	WOBJ_SerialPortRefNum(port) = -1;
	if (SerClose(refNum) != 0)
		return 0;
	return 1;
	}

static Var SerialPortCreate(Var stack[])
	{
	WObject port;
	int32 type, number, baudRate, bits, parity, stopBits;
	SerSettingsType settings;
	UInt refNum;
	Var v;

	v.obj = 0;
	port = stack[0].obj;
	number = stack[1].intValue;
	baudRate = stack[2].intValue;
	bits = stack[3].intValue;
	parity = stack[4].intValue;
	stopBits = stack[5].intValue;

	WOBJ_SerialPortRefNum(port) = -1;

	if (SysLibFind("Serial Library", &refNum) != 0)
		return v;
	if (baudRate == 0)
		baudRate = 9600;
	if (SerOpen(refNum, number, baudRate) != 0)
		return v;
	settings.baudRate = baudRate;
	settings.flags = serSettingsFlagRTSAutoM | serSettingsFlagCTSAutoM;
	if (bits == 8)
		settings.flags |= serSettingsFlagBitsPerChar8;
	else if (bits == 7)
		settings.flags |= serSettingsFlagBitsPerChar7;
	else if (bits == 6)
		settings.flags |= serSettingsFlagBitsPerChar6;
	else if (bits == 5)
		settings.flags |= serSettingsFlagBitsPerChar5;
	if (parity != 0)
		settings.flags |= serSettingsFlagParityEvenM;
	if (stopBits == 1)
		settings.flags |= serSettingsFlagStopBits1;
	else if (stopBits == 2)
		settings.flags |= serSettingsFlagStopBits2;
	settings.ctsTimeout = 2 * sysTicksPerSecond;
	if (SerSetSettings(refNum, &settings) != 0)
		{
		SerClose(refNum);
		return v;
		}
	WOBJ_SerialPortRefNum(port) = (int32)refNum;
	WOBJ_SerialPortTimeout(port) = millisToTicks(100);
	return v;
	}

static void SerialPortDestroy(WObject port)
	{
	_SerialPortClose(port);
	}

static Var SerialPortIsOpen(Var stack[])
	{
	WObject port;
	Var v;

	port = stack[0].obj;
	if (WOBJ_SerialPortRefNum(port) == -1)
		v.intValue = 0;
	else
		v.intValue = 1;
	return v;
	}

static Var SerialPortSetReadTimeout(Var stack[])
	{
	WObject port;
	int32 millis;
	Var v;

	v.intValue = 0;
	port = stack[0].obj;
	millis = stack[1].intValue;
	if (millis < 0 || WOBJ_SerialPortRefNum(port) == -1)
		return v;
	WOBJ_SerialPortTimeout(port) = millisToTicks(millis);
	v.intValue = 1;
	return v;
	}

static Var SerialPortReadCheck(Var stack[])
	{
	WObject port;
	UInt refNum;
	ULong numBytes;
	Var v;

	v.intValue = -1;
	port = stack[0].obj;
	if (WOBJ_SerialPortRefNum(port) == -1)
		return v; // port not open
	refNum = (UInt)WOBJ_SerialPortRefNum(port);
	if (SerReceiveCheck(refNum, &numBytes) != 0)
		return v;
	v.intValue = numBytes;
	return v;
	}

static Var SerialPortSetFlowControl(Var stack[])
	{
	WObject port;
	int32 flowOn;
	UInt refNum;
	SerSettingsType settings;
	Var v;

	v.intValue = 0;
	port = stack[0].obj;
	flowOn = stack[1].intValue;
	if (WOBJ_SerialPortRefNum(port) == -1)
		return v;
	refNum = (UInt)WOBJ_SerialPortRefNum(port);
	if (SerGetSettings(refNum, &settings) != 0)
		return v;
	settings.flags = serSettingsFlagBitsPerChar8 | serSettingsFlagStopBits1;
	if (flowOn == 1)
		settings.flags |= serSettingsFlagRTSAutoM | serSettingsFlagCTSAutoM;
	if (SerSetSettings(refNum, &settings) != 0)
		return v;
	v.intValue = 1;
	return v;
	}

static Var SerialPortReadWriteBytes(Var stack[], int isRead)
	{
	WObject port, byteArray;
	int32 start, count;
	uchar *bytes;
	ULong numRW;
	UInt refNum;
	Err err;
	Var v;

	v.intValue = -1;
	port = stack[0].obj;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	if (WOBJ_SerialPortRefNum(port) == -1)
		return v; // port not open
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	refNum = (UInt)WOBJ_SerialPortRefNum(port);
	if (isRead)
		numRW = SerReceive(refNum, (VoidPtr)&bytes[start], (ULong)count,
			WOBJ_SerialPortTimeout(port), &err);
	else
		numRW = SerSend(refNum, (VoidPtr)&bytes[start], (ULong)count, &err);
	if (err != 0)
		SerClearErr(refNum);
	v.intValue = numRW;
	return v;
	}

static Var SerialPortRead(Var stack[])
	{
	return SerialPortReadWriteBytes(stack, 1);
	}

static Var SerialPortWrite(Var stack[])
	{
	return SerialPortReadWriteBytes(stack, 0);
	}

static Var SerialPortClose(Var stack[])
	{
	WObject port;
	Var v;

	port = stack[0].obj;
	v.intValue = _SerialPortClose(port);
	return v;
	}

//
// Vm
//

static Var VmIsColor()
	{
	Var v;

	v.intValue = 0;
	return v;
	}

static Var VmGetTimeStamp(Var stack[])
	{
	Var v;

	v.intValue = getTimeStamp();
	return v;
	}

static Var VmExec(Var stack[])
	{
	WObject pathString, argsString;
	UtfString path, args;
	Var v;
	LocalID localID;
	int32 launchCode;
	CharPtr s;

	v.intValue = -1;
	pathString = stack[0].obj;
	argsString = stack[1].obj;
	launchCode = stack[2].intValue;
	// NOTE: we could use static here since we call stringToUtf twice
	// but before the second call, the path is no longer needed
	path = stringToUtf(pathString, STU_NULL_TERMINATE);
	if (path.len == 0)
		return v;
	localID = DmFindDatabase(0, path.str);
	if (!localID)
		return v;
	args = stringToUtf(argsString, STU_NULL_TERMINATE | STU_USE_STATIC);
	s = NULL;
	if (args.len > 0)
		{
		s = MemPtrNew(args.len + 1);
		StrCopy(s, args.str);
		MemPtrSetOwner(s, 0);
		}
	if (launchCode == 0)
		launchCode = sysAppLaunchCmdNormalLaunch;
	v.intValue = SysUIAppSwitch(0, localID, launchCode, s);
	if (v.intValue != 0 && s != NULL)
		MemPtrFree(s);
	return v;
	}

static Var VmSleep(Var stack[])
	{
	Var v;

	SysTaskDelay(millisToTicks(stack[0].intValue));
	v.obj = 0;
	return v;
	}

static Var VmGetPlatform(Var stack[])
	{
	Var v;

	v.obj = createString("PalmOS");
	return v;
	}

static Var VmSetDeviceAutoOff(Var stack[])
	{
	Var v;

	v.intValue = SysSetAutoOffTime(stack[0].intValue);
	return v;
	}

static Var VmGetUserName(Var stack[])
	{
	Var v;
	Err err;
	char name[dlkUserNameBufSize];
	
	err = DlkGetSyncInfo(NULL, NULL, NULL, name, NULL, NULL);
	if (err != 0)
		{
		v.obj = 0;
		return v;
		}	
	v.obj = createString(name);
	return v;
	}
