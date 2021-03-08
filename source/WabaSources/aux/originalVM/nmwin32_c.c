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
// call that could garbage collect. For example, this is bad:
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

static void WindowDestroy(WObject obj);
static void MainWinDestroy(WObject obj);
static void GraphicsDestroy(WObject obj);
static void ImageDestroy(WObject obj);
static void CatalogDestroy(WObject obj);
static void FileDestroy(WObject obj);
static void SocketDestroy(WObject obj);
static void SerialPortDestroy(WObject obj);

ClassHook classHooks[] =
	{
	{ "waba/ui/Window", WindowDestroy, 1 },
	{ "waba/ui/MainWindow", NULL, 1 },
	{ "waba/fx/Graphics", GraphicsDestroy, 11 },
	{ "waba/fx/Image", ImageDestroy, 1 },
	{ "waba/io/Catalog", CatalogDestroy, 9 },
	{ "waba/io/File", FileDestroy, 1 },
	{ "waba/io/Socket", SocketDestroy, 1 }, 
	{ "waba/io/SerialPort", SerialPortDestroy, 1 }, 
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
// var[0] = Class
// var[n] = ...other locals...
// var[n + 1] = hook var - hWnd under Win32
//

// since Window inherits from other classes, we need to calculate the
// right base offset to start with when reading/writing to variables
static int _winHookOffset = -1;

#define WOBJ_WindowHWnd(o) (objectPtr(o))[_winHookOffset + 0].refValue

#define WOBJ_WindowHookVars 1

static Var WindowCreate(Var stack[])
	{
	WObject win;
	HWND hWnd;
	DWORD style;
	int width, height;
	RECT rect;
	Var v;
	TCHAR title[40];

	win = stack[0].obj;
	if (_winHookOffset == -1)
		{
		WClass *wc;

		wc = getClass(createUtfString("waba/ui/Window"));
		_winHookOffset = 1 + wc->numVars - WOBJ_WindowHookVars;
		}
	// copy the name of the main window to the title
		{
		uint16 n;
		UtfString className;
		WClass *wclass;

		wclass = WOBJ_class(win);
		className = getUtfString(wclass, wclass->classNameIndex);
		n = className.len + 1;
		if (n > 40)
			n = 40;
		asciiToUnicode(className.str, title, n);
		}
	width = g_mainWinWidth;
	height = g_mainWinHeight;
	style = WS_VISIBLE | WS_SYSMENU | WS_CAPTION;
#ifdef WINCE
	if (width <= 0 || height <= 0)
		{
		width = CW_USEDEFAULT;
		height = CW_USEDEFAULT;
		style = WS_VISIBLE; // no border when default size under CE
		}
	else
		// this is an attempt under CE to keep the window on top
		// of the background window, it doesn't seem to help
		style |= WS_EX_TOPMOST;
#else
	style |= WS_MINIMIZEBOX | WS_MAXIMIZEBOX;
	if (width <= 0 || height <= 0)
		{
		width = 300;
		height = 300;
		}
#endif
	if (width != CW_USEDEFAULT)
		{
		// given the size of the client area, figure out the window size needed
		rect.left = 0;
		rect.top = 0;
		rect.right = width;
		rect.bottom = height;
		AdjustWindowRectEx(&rect, style, FALSE, 0);
		width = rect.right - rect.left;
		height = rect.bottom - rect.top;
		}
	hWnd = CreateWindow(pszWndClassName, title, style, CW_USEDEFAULT, CW_USEDEFAULT,
		width, height, NULL, NULL, g_hInstance, NULL );
	// store hWnd in object
	WOBJ_WindowHWnd(win) = (void *)hWnd;
	// store the MainWindow object reference in the window
	SetWindowLong(hWnd, GWL_USERDATA, (LONG)win);
	// store the x, y, width, height
	GetClientRect(hWnd, &rect);
	WOBJ_ControlX(win) = rect.left;
	WOBJ_ControlY(win) = rect.top;
	WOBJ_ControlWidth(win) = rect.right - rect.left;
	WOBJ_ControlHeight(win) = rect.bottom - rect.top;
	v.obj = 0;
	return v;
	}

static void WindowDestroy(WObject mainWin)
	{
	HWND hWnd;

	hWnd = (HWND)(WOBJ_WindowHWnd(mainWin));
	DestroyWindow(hWnd);
	WOBJ_WindowHWnd(mainWin) = NULL;
	}

//
// MainWindow
//
// var[0] = Class
// var[n] = ...other locals...
// var[n + 1] = hook var - timerId under Win32
//

// since MainWindow inherits from other classes, we need to calculate the
// right base offset to start with when reading/writing to variables
static int _mainWinHookOffset = -1;

#define WOBJ_MainWinTimerId(o) (objectPtr(o))[_mainWinHookOffset + 0].intValue

#define WOBJ_MainWinHookVars 1

static Var MainWinCreate(Var stack[])
	{
	Var v;

	if (_mainWinHookOffset == -1)
		{
		WClass *wc;

		wc = getClass(createUtfString("waba/ui/MainWindow"));
		_mainWinHookOffset = 1 + wc->numVars - WOBJ_MainWinHookVars;
		}
	v.obj = 0;
	return v;
	}

static Var MainWinExit(Var stack[])
	{
	Var v;
	int exitCode;

	exitCode = stack[1].intValue;
	PostQuitMessage(exitCode);
	v.obj = 0;
	return v;
	}

static Var MainWinSetTimerInterval(Var stack[])
	{
	WObject mainWin;
	int32 millis;
	HWND hWnd;
	UINT timerId;
	Var v;

	v.obj = 0;
	// NOTE: Windows timers and clock have a resolution of 55ms. This
	// means that you can't set a timer interval to less than 55ms and
	// have it work properly. There isn't any good way around this since
	// other methods (Sleep(), GetCurrentTime(), etc.) all share the same
	// resolution problem. One possible solution is to have a interval
	// of 1ms, specify that there is no delay and the main event loop
	// would simply process events until there are none pending and call
	// the timer proc whenever there isn't anything pending. A Sleep(0)
	// would be added to give other processes time slices. But, for
	// simplicity, this code has not been put in place.
	mainWin = stack[0].obj;
	millis = stack[1].intValue;
	if (_winHookOffset == -1)
		return v; // sanity check
	hWnd = (HWND)(WOBJ_WindowHWnd(mainWin));
	timerId = (UINT)(WOBJ_MainWinTimerId(mainWin));
	if (timerId != 0)
		KillTimer(hWnd, timerId);
	if (millis > 0)
		timerId = SetTimer(hWnd, 1000, millis, NULL);
	else
		timerId = 0;
	WOBJ_MainWinTimerId(mainWin) = (int32)timerId;
	return v;
	}

//
// Image
//
// var[0] = Class
// var[1] = width
// var[2] = height
// var[3] = hook var - hBitmap
//

#define WOBJ_ImageWidth(o) (objectPtr(o))[1].intValue
#define WOBJ_ImageHeight(o) (objectPtr(o))[2].intValue
#define WOBJ_ImageHBitmap(o) (objectPtr(o))[3].refValue

static Var ImageCreate(Var stack[])
	{
	WObject image;
	int32 width, height;
	HBITMAP hBitmap;
	HDC hDC;
	Var v;

	image = stack[0].obj;
	width = WOBJ_ImageWidth(image);
	height = WOBJ_ImageHeight(image);
	hDC = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	if (width > 0 && height > 0)
		hBitmap = CreateCompatibleBitmap(hDC, width, height);
	else
		hBitmap = NULL;
	DeleteDC(hDC);
	WOBJ_ImageHBitmap(image) = hBitmap;
	v.obj = 0;
	return v;
	}

typedef struct
	{
	BITMAPINFOHEADER bmiHeader;
	RGBQUAD *colors;
	} MONOBITMAPINFO;

// Intel-architecture getUInt32
#define inGetUInt32(b) (uint32)( (uint32)((b)[3])<<24 | (uint32)((b)[2])<<16 | (uint32)((b)[1])<<8 | (uint32)((b)[0]) )
#define inGetUInt16(b) (uint16)( (uint16)((b)[1])<<8 | (uint16)((b)[0]) )

static void _ImageLoadBMP(WObject image, uchar *p)
	{
	uint32 bitmapOffset, infoSize, width, height, bpp;
	uint32 i, compression, numColors, scanlen;
	BITMAPINFO *bi;
	BITMAPINFOHEADER *bih;
	RGBQUAD *rgb;
	BYTE *b;
	uchar *ppv;
	HDC hDC;
	HBITMAP hBitmap;

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
	numColors = 1 << bpp;
	scanlen = ((width * bpp) + 7) / 8; // # bytes
	scanlen = ((scanlen + 3) / 4) * 4; // end on 32 bit boundry
	bi = (BITMAPINFO *)xmalloc(sizeof(BITMAPINFOHEADER) +
		numColors * sizeof(RGBQUAD));
	if (bi == NULL)
		return;

	// colormap
	//
	// 0-3 uint32 col[0]
	// 4-7 uint32 col[1]
	// ...
	rgb = bi->bmiColors;
	b = (BYTE *)&p[54];
	for (i = 0; i < numColors; i++)
		{
		rgb->rgbBlue = *b++;
		rgb->rgbGreen = *b++;
		rgb->rgbRed = *b++;
		rgb->rgbReserved = *b++;
		rgb++;
		}

	// create Windows bitmap
	bih = &bi->bmiHeader;
	bih->biSize = sizeof(BITMAPINFOHEADER);
	bih->biWidth = (LONG)width;
	bih->biHeight = (LONG)height; // positive for bottom-up
	bih->biPlanes = 1;
	bih->biBitCount = bpp;
	bih->biCompression = BI_RGB;
	bih->biSizeImage = 0;
	bih->biXPelsPerMeter = 1000000; // unused
	bih->biYPelsPerMeter = 1000000; // unused
	bih->biClrUsed = 0;
	bih->biClrImportant = 0;

	// NOTE: CreateDIBitmap() isn't available under CE
	ppv = NULL;
	hDC = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	hBitmap = CreateDIBSection(hDC, bi, DIB_RGB_COLORS, &ppv, NULL, 0);
	DeleteDC(hDC);
	xfree(bi);
	// copy pixels into section's image buffer
	if (hBitmap == NULL || ppv == NULL)
		return;
	xmemmove(ppv, &p[bitmapOffset], scanlen * height);
	WOBJ_ImageHBitmap(image) = hBitmap;
	WOBJ_ImageWidth(image) = width;
	WOBJ_ImageHeight(image) = height;
	}

static Var ImageSetPixels(Var stack[])
	{
	WObject image, colorMapArray, pixelsArray;
	int32 bitsPerPixel, bytesPerRow, numRows, y, numColors;
	int32 i, dibBytesPerRow, minBytesPerRow, imageWidth;
	uint32 *colorMap;
	uchar *pixels, *pSrc, *pDst;
	BITMAPINFO *bi;
	BITMAPINFOHEADER *bih;
	RGBQUAD *rgb;
	uchar *ppv;
	HDC hDC, hMemDC;
	HBITMAP hBitmap, hBlitBitmap;
	Var v;

	v.obj = 0;
	image = stack[0].obj;
	bitsPerPixel = stack[1].intValue;
	colorMapArray = stack[2].obj;
	bytesPerRow = stack[3].intValue;
	numRows = stack[4].intValue;
	y = stack[5].intValue;
	pixelsArray = stack[6].obj;

	// validate parameters
	if (colorMapArray == 0 || pixelsArray == 0)
		return v;
	numColors = WOBJ_arrayLen(colorMapArray);
	if (bitsPerPixel == 1 && numColors == 2)
		;
	else if (bitsPerPixel == 4 && numColors == 16)
		;
	else if (bitsPerPixel == 8 && numColors == 256)
		;
	else
		return v;
	if (WOBJ_arrayLen(pixelsArray) < bytesPerRow * numRows)
		return v;
	hBitmap = WOBJ_ImageHBitmap(image);
	if (hBitmap == NULL)
		return v;
	imageWidth = WOBJ_ImageWidth(image);

	// fill in bitmap info
	colorMap = (uint32 *)WOBJ_arrayStart(colorMapArray);
	pixels = (uchar *)WOBJ_arrayStart(pixelsArray);
	bi = (BITMAPINFO *)xmalloc(sizeof(BITMAPINFOHEADER) +
		numColors * sizeof(RGBQUAD));
	if (bi == NULL)
		return v;
	rgb = bi->bmiColors;
	for (i = 0; i < numColors; i++)
		{
		rgb->rgbBlue = colorMap[i] & 0xFF;
		rgb->rgbGreen = (colorMap[i] >> 8) & 0xFF;
		rgb->rgbRed = (colorMap[i] >> 16) & 0xFF;
		rgb->rgbReserved = 0;
		rgb++;
		}

	// create Windows bitmap
	bih = &bi->bmiHeader;
	bih->biSize = sizeof(BITMAPINFOHEADER);
	bih->biWidth = (LONG)imageWidth;
	bih->biHeight = (LONG)- numRows; // negative for top-down
	bih->biPlanes = 1;
	bih->biBitCount = bitsPerPixel;
	bih->biCompression = BI_RGB;
	bih->biSizeImage = 0;
	bih->biXPelsPerMeter = 1000000; // unused
	bih->biYPelsPerMeter = 1000000; // unused
	bih->biClrUsed = 0;
	bih->biClrImportant = 0;

	// NOTE: CreateDIBitmap() isn't available under CE
	ppv = NULL;
	hDC = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	hBlitBitmap = CreateDIBSection(hDC, bi, DIB_RGB_COLORS, &ppv, NULL, 0);
	DeleteDC(hDC);
	xfree(bi);
	if (hBlitBitmap == NULL || ppv == NULL)
		return v;

	// copy pixels into section's image buffer
	dibBytesPerRow = ((imageWidth * bitsPerPixel) + 7) / 8; // # bytes
	dibBytesPerRow = ((dibBytesPerRow + 3) / 4) * 4; // end on 32 bit boundry
	if (bytesPerRow > dibBytesPerRow)
		minBytesPerRow = dibBytesPerRow;
	else
		minBytesPerRow = bytesPerRow;
	pSrc = pixels;
	pDst = ppv;
	for (i = 0; i < numRows; i++)
		{
		xmemmove(pDst, pSrc, minBytesPerRow);
		pSrc += bytesPerRow;
		pDst += dibBytesPerRow;
		}

	// bitblit this section into the image
	hDC = CreateCompatibleDC(NULL);
	SelectObject(hDC, hBitmap);
	hMemDC = CreateCompatibleDC(hDC);
	SelectObject(hMemDC, hBlitBitmap);
	BitBlt(hDC, 0, y, imageWidth, numRows, hMemDC, 0, 0, SRCCOPY);
	DeleteDC(hMemDC);
	DeleteDC(hDC);
	DeleteObject(hBlitBitmap);
	}

static Var ImageLoad(Var stack[])
	{
	WObject image;
	UtfString path;
	WObject pathString;
	int freeNeeded;
	uchar *p;
	Var v;

	image = stack[0].obj;
	// NOTE: we don't have to free an existing bitmap because this is only called
	// from an image constructor
	WOBJ_ImageHBitmap(image) = NULL;
	WOBJ_ImageWidth(image) = 0;
	WOBJ_ImageHeight(image) = 0;
	freeNeeded = 0;
	pathString = stack[1].obj;
	v.obj = 0;
	// NOTE: we null terminate here since we call readFileIntoMemory()
	path = stringToUtf(pathString, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (path.len == 0)
		return v;
	// first try from memory
	p = loadFromMem(path.str, path.len, NULL);
	if (p == NULL)
		{
		p = readFileIntoMemory(path.str, 0, NULL);
		if (p == NULL)
			return v;
		freeNeeded = 1;
		}
	_ImageLoadBMP(image, p);
	if (freeNeeded)
		xfree(p);
	return v;
	}

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

static void ImageDestroy(WObject image)
	{
	HBITMAP hBitmap;

	hBitmap = WOBJ_ImageHBitmap(image);
	if (hBitmap == NULL)
		return;
	DeleteObject(hBitmap);
	WOBJ_ImageHBitmap(image) = NULL;
	}

//
// Socket
//
// var[0] = Class
// var[1] = hook var - Socket Id (int32)
//

// NOTE: The SOCKET type is an unsigned 32 bit quantity - this most closely
// matches the object type
#define WOBJ_SocketId(o) (objectPtr(o))[1].obj

static int32 _SocketClose(WObject socket)
	{
	SOCKET socketId;

	socketId = WOBJ_SocketId(socket);
	if (socketId == INVALID_SOCKET)
		return 0;
	WOBJ_SocketId(socket) = INVALID_SOCKET;
	if (closesocket(socketId) != 0)
		return 0;
	return 1;
	}

static Var SocketCreate(Var stack[])
	{
	WObject sock, host;
	int32 port, status;
	SOCKET socketId;
	UtfString s;
	unsigned long ipAddr;
	struct sockaddr_in sockAddr;
	struct hostent *hostEnt;
	Var v;

	v.obj = 0;
	sock = stack[0].obj;
	host = stack[1].obj;
	port = stack[2].intValue;
	WOBJ_SocketId(sock) = INVALID_SOCKET;

	if (win32WSAStarted == 0)
		{
		WSADATA wsaData;

		status = WSAStartup(MAKEWORD(2, 0), &wsaData);
		if (status == -1)
			return v;
		win32WSAStarted = 1;
		}

	xmemzero(&sockAddr, sizeof(sockAddr));
	sockAddr.sin_family = AF_INET;
	sockAddr.sin_port = htons((u_short)port);
	s = stringToUtf(host, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (s.len == 0)
		return v;
	ipAddr = inet_addr(s.str);
	if (ipAddr != INADDR_NONE)
		xmemmove(&sockAddr.sin_addr, &ipAddr, sizeof(unsigned long));
	else
		{
		hostEnt = gethostbyname(s.str);
		if (hostEnt == NULL)
			return v;
		xmemmove(&sockAddr.sin_addr, hostEnt->h_addr_list[0], hostEnt->h_length);
		}

	socketId = socket(AF_INET, SOCK_STREAM, 0);
	if (socketId == INVALID_SOCKET)
		return v;

	// NOTE: later, we should set the socket timout here and use a non-blocking
	// connect
	status = connect(socketId, (struct sockaddr *)&sockAddr, sizeof(sockAddr));
	if (status != 0)
		{
		closesocket(socketId);
		return v;
		}

	WOBJ_SocketId(sock) = socketId;
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
	if (WOBJ_SocketId(socket) == INVALID_SOCKET)
		v.intValue = 0;
	else
		v.intValue = 1;
	return v;
	}

static Var SocketSetReadTimeout(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SocketReadWriteBytes(Var stack[], int isRead)
	{
	WObject socket, byteArray;
	SOCKET socketId;
	int32 start, count, countSoFar, n;
	uchar *bytes;
	Var v;

	v.intValue = -1;
	socket = stack[0].obj;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	socketId = WOBJ_SocketId(socket);
	if (socketId == INVALID_SOCKET)
		return v; // socket not open
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	bytes = &bytes[start];
	countSoFar = 0;
	while (countSoFar < count)
		{
		if (isRead)
			n = recv(socketId, bytes, count - countSoFar, 0);
		else
			n = send(socketId, bytes, count - countSoFar, 0);
		if (n > 0)
			{
			countSoFar += n;
			bytes += n;
			}
		else if (n <= 0)
			{
			if (countSoFar != 0)
				v.intValue = countSoFar;
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
	Var v;
	int32 freq, duration;

	freq = stack[0].intValue;
	duration = stack[1].intValue;
#ifndef WINCE
	Beep(freq, duration);
#endif
	v.obj = 0;
	return v;
	}

static Var SoundBeep(Var stack[])
	{
	Var v;

	MessageBeep(0xFFFFFFFF);
	v.obj = 0;
	return v;
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

static HFONT createWin32Font(WObject font)
	{
	LOGFONT lf;
	HFONT hFont;
	TCHAR *name, nameBuf[LF_FACESIZE];
	int nWeight, nHeight;
	BYTE cItalic;

	if (font == 0)
		{
		// default font
		name = TEXT("Arial");
		nHeight = 12;
		nWeight = FW_NORMAL;
		cItalic = FALSE;
		}
	else
		{
		UtfString s;

		s = stringToUtf(WOBJ_FontName(font), STU_NULL_TERMINATE | STU_USE_STATIC);
		if (s.len == 0)
			name = TEXT("Arial");
		else
			{
			asciiToUnicode(s.str, nameBuf, LF_FACESIZE);
			name = nameBuf;
			}
		nHeight = WOBJ_FontSize(font);
		if (WOBJ_FontStyle(font) == Font_BOLD)
			nWeight = FW_BOLD;
		else
			nWeight = FW_NORMAL;
//		if (WOBJ_FontStyle(font) == ITALIC)
//			cItalic = TRUE;
//		else
			cItalic = FALSE;
		}
	lf.lfHeight = nHeight;
	lf.lfWidth = 0;
	lf.lfEscapement = 0;
	lf.lfOrientation = 0;
	lf.lfWeight = nWeight;
	lf.lfItalic = cItalic;
	lf.lfUnderline = FALSE;
	lf.lfStrikeOut = FALSE;
#ifdef WINCE
	lf.lfCharSet = 0;
#else
	lf.lfCharSet = OEM_CHARSET;
#endif
	lf.lfOutPrecision = OUT_DEFAULT_PRECIS;
	lf.lfClipPrecision = CLIP_DEFAULT_PRECIS;
	lf.lfQuality = DEFAULT_QUALITY;
	lf.lfPitchAndFamily = DEFAULT_PITCH | FF_DONTCARE;
	// NOTE: LF_FACESIZE is max size of lfFaceName
	lstrcpy(lf.lfFaceName, name);
	hFont = CreateFontIndirect(&lf);
	// NOTE: we call DeleteObject(hFont) when done with font. This is done
	// in GraphicsDestroy() and GraphicsSetFont().
	return hFont; 
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
	WObject font, fontMetrics, surface;
	Var v;
	TEXTMETRIC tm;
	HDC hDC;
	HFONT hFont, oldFont;

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

	// surface is unused - DC comes from screen
	hDC = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	hFont = createWin32Font(font);
	oldFont = SelectObject(hDC, hFont);
	GetTextMetrics(hDC, &tm);
	SelectObject(hDC, oldFont);
	DeleteObject(hFont);
	DeleteDC(hDC);

	WOBJ_FontMetricsAscent(fontMetrics) = tm.tmAscent;
	WOBJ_FontMetricsDescent(fontMetrics) = tm.tmDescent;
	WOBJ_FontMetricsLeading(fontMetrics) = tm.tmExternalLeading;
	v.obj = 0;
	return v;
	}

#define FM_STRINGWIDTH 1
#define FM_CHARARRAYWIDTH 2
#define FM_CHARWIDTH 3

static Var FontMetricsGetWidth(int type, Var stack[])
	{
	WObject font, fontMetrics, surface;
	Var v;
	HDC hDC;
	HFONT hFont, oldFont;
	int32 width;

	fontMetrics = stack[0].obj;
	font = WOBJ_FontMetricsFont(fontMetrics);
	surface = WOBJ_FontMetricsSurface(fontMetrics);
	if (font == 0 || surface == 0)
		{
		v.intValue = 0;
		return v;
		}
	// surface is unused - DC comes from screen
	hDC = CreateDC(TEXT("DISPLAY"), NULL, NULL, NULL);
	hFont = createWin32Font(font);
	oldFont = SelectObject(hDC, hFont);
	switch (type)
		{
		case FM_CHARWIDTH:
			{
			TCHAR ch;
			SIZE size;

			ch = (TCHAR)stack[1].intValue;
			// NOTE: GetCharWidth32() doesn't work under CE
			GetTextExtentPoint32(hDC, &ch, 1, &size);
			width = (int)size.cx;
			break;
			}
		case FM_STRINGWIDTH:
		case FM_CHARARRAYWIDTH:
			{
			WObject string, charArray;
			int32 start, count;
			uint16 *chars;
			SIZE size;

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
#ifdef WINCE
			GetTextExtentPoint32(hDC, (TCHAR *)chars, count, &size);
			width = (int)size.cx;
#else
			while (count > 0)
				{
				char buf[40];
				int32 i, n;

				n = sizeof(buf);
				if (n > count)
					n = count;
				for (i = 0; i < n; i++)
					buf[i] = (char)chars[i];
				GetTextExtentPoint32(hDC, buf, count, &size);
				width += (int32)size.cx;
				count -= n;
				chars += n;
				}
#endif
			break;
			}
		}
	SelectObject(hDC, oldFont);
	DeleteObject(hFont);
	DeleteDC(hDC);
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

//
// Surface
//

#define SURF_WINDOW 1
#define SURF_IMAGE 2

static WClass *windowClass = 0;
static WClass *imageClass = 0;

static int SurfaceGetType(WObject surface)
	{
	WClass *wclass;

	if (surface == 0)
		return 0;

	// cache class pointers for performance
	if (!windowClass)
		windowClass = getClass(createUtfString("waba/ui/Window"));
	if (!imageClass)
		imageClass = getClass(createUtfString("waba/fx/Image"));

	wclass = WOBJ_class(surface);
	if (compatible(wclass, windowClass))
		return SURF_WINDOW;
	if (compatible(wclass, imageClass))
		return SURF_IMAGE;
	return 0;
	}

//
// Graphics
//
// var[0] = Class
// var[1] = Surface
// var[2] = hook var - isValid flag
// var[3] = hook var - Win32 hWnd
// var[4] = hook var - Win32 hBitmap
// var[5] = hook var - Win32 hFont
// var[6] = hook var - Win32 hRgn (clip)
// var[7] = hook var - drawing op
// var[8] = hook var - Win32 rgb (COLORREF)
// var[9] = hook var - Win32 hPen
// var[10] = hook var - Win32 hBrush
// var[11] = hook var - x translation
// var[12] = hook var - y translation

#define WOBJ_GraphicsSurface(o) (objectPtr(o))[1].obj
#define WOBJ_GraphicsIsValid(o) (objectPtr(o))[2].intValue
#define WOBJ_GraphicsHWnd(o) (objectPtr(o))[3].refValue
#define WOBJ_GraphicsHBitmap(o) (objectPtr(o))[4].refValue
#define WOBJ_GraphicsHFont(o) (objectPtr(o))[5].refValue
#define WOBJ_GraphicsHRgn(o) (objectPtr(o))[6].refValue
#define WOBJ_GraphicsDrawOp(o) (objectPtr(o))[7].intValue
#define WOBJ_GraphicsRGB(o) (objectPtr(o))[8].intValue
#define WOBJ_GraphicsHPen(o) (objectPtr(o))[9].refValue
#define WOBJ_GraphicsHBrush(o) (objectPtr(o))[10].refValue
#define WOBJ_GraphicsTransX(o) (objectPtr(o))[11].intValue
#define WOBJ_GraphicsTransY(o) (objectPtr(o))[12].intValue

#define DRAW_OVER 1
#define DRAW_AND 2
#define DRAW_OR 3
#define DRAW_XOR 4

static Var GraphicsCreate(Var stack[])
	{
	WObject gr, surface;
	int32 surfaceType;
	HWND hWnd;
	HBITMAP hBitmap;
	Var v;

	// NOTE: We can't store a native Win32 DC in the Graphics object. In
	// Win32 graphics, single shared DC (without using SaveDC()) is
	// associated with a Win32 bitmap/window. For performance we tried using
	// CS_OWNDC but gave up on it due to what appear to be Win32 bugs. So,
	// we store all the state of the DC in the Graphics object and restore
	// it when we need to use it.
	gr = stack[0].obj;
	surface = WOBJ_GraphicsSurface(gr);
	surfaceType = SurfaceGetType(surface);
	hWnd = NULL;
	hBitmap = NULL;
	if (surfaceType == SURF_WINDOW)
		hWnd = WOBJ_WindowHWnd(surface);
	else if (surfaceType == SURF_IMAGE)
		hBitmap = WOBJ_ImageHBitmap(surface);

	WOBJ_GraphicsIsValid(gr) = 1;
	WOBJ_GraphicsHWnd(gr) = hWnd;
	WOBJ_GraphicsHBitmap(gr) = hBitmap;
	WOBJ_GraphicsHFont(gr) = (void *)createWin32Font(0);
	WOBJ_GraphicsHRgn(gr) = NULL;
	WOBJ_GraphicsDrawOp(gr) = DRAW_OVER;
	WOBJ_GraphicsRGB(gr) = (int32)RGB(0, 0, 0);
	WOBJ_GraphicsHPen(gr) = (void *)GetStockObject(BLACK_PEN);
	WOBJ_GraphicsHBrush(gr) = (void *)GetStockObject(BLACK_BRUSH);
	WOBJ_GraphicsTransX(gr) = 0;
	WOBJ_GraphicsTransY(gr) = 0;

	v.obj = 0;
	return v;
	}

static void GraphicsDestroy(WObject gr)
	{
	HFONT hFont;
	HRGN hRgn;
	HPEN hPen;
	HBRUSH hBrush;

	if (WOBJ_GraphicsIsValid(gr) == 0)
		return;
	WOBJ_GraphicsIsValid(gr) = 0;
	hFont = (HFONT)WOBJ_GraphicsHFont(gr);
	DeleteObject(hFont);
	hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
	if (hRgn != NULL)
		DeleteObject(hRgn);
	hPen = (HPEN)WOBJ_GraphicsHPen(gr);
	DeleteObject(hPen);
	hBrush = (HBRUSH)WOBJ_GraphicsHBrush(gr);
	DeleteObject(hBrush);
	}

static Var GraphicsFree(Var stack[])
	{
	WObject gr;
	Var v;

	gr = stack[0].obj;
	GraphicsDestroy(gr);
	v.obj = 0;
	return v;
	}

static Var GraphicsSetColor(Var stack[])
	{
	WObject gr;
	int32 r, g, b;
	static COLORREF black = RGB(0, 0, 0);
	static COLORREF white = RGB(255, 255, 255);
	Var v;

	gr = stack[0].obj;
	r = stack[1].intValue;
	g = stack[2].intValue;
	b = stack[3].intValue;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		{
		COLORREF rgb;
		HBRUSH hBrush;
		HPEN hPen;

		rgb = RGB(r, g, b);
		WOBJ_GraphicsRGB(gr) = (int32)rgb;

		// NOTE: According to the Win32 doc, it is not harmful to delete
		// stock object pens and brushes
		hPen = (HPEN)WOBJ_GraphicsHPen(gr);
		hBrush = (HBRUSH)WOBJ_GraphicsHBrush(gr);
		DeleteObject(hPen);
		DeleteObject(hBrush);

		if (rgb == black)
			{
			hPen = GetStockObject(BLACK_PEN);
			hBrush = GetStockObject(BLACK_BRUSH);
			}
		else if (rgb == white)
			{
			hPen = GetStockObject(WHITE_PEN);
			hBrush = GetStockObject(WHITE_BRUSH);
			}
		else
			{
			hPen = CreatePen(PS_SOLID, 1, rgb);
			hBrush = CreateSolidBrush(rgb);
			}
		WOBJ_GraphicsHPen(gr) = (void *)hPen;
		WOBJ_GraphicsHBrush(gr) = (void *)hBrush;
		}
	v.obj = 0;
	return v;
	}

static Var GraphicsSetFont(Var stack[])
	{
	WObject gr, font;
	Var v;

	gr = stack[0].obj;
	font = stack[1].obj;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		{
		HFONT hFont;

		hFont = (HFONT)WOBJ_GraphicsHFont(gr);
		DeleteObject(hFont);

		hFont = createWin32Font(font);
		WOBJ_GraphicsHFont(gr) = (void *)hFont;
		}
	v.obj = 0;
	return v;
	}

static Var GraphicsSetDrawOp(Var stack[])
	{
	WObject gr;
	int32 drawOp;
	Var v;

	gr = stack[0].obj;
	drawOp = stack[1].intValue;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		WOBJ_GraphicsDrawOp(gr) = drawOp;
	v.obj = 0;
	return v;
	}

static Var GraphicsSetClip(Var stack[])
	{
	WObject gr;
	int32 cx, cy, cw, ch;
	Var v;

	gr = stack[0].obj;
	// clip X and Y are stored in absolute coordinates
	cx = WOBJ_GraphicsTransX(gr) + stack[1].intValue;
	cy = WOBJ_GraphicsTransY(gr) + stack[2].intValue;
	cw = stack[3].intValue;
	ch = stack[4].intValue;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		{
		HRGN hRgn;

		hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
		if (hRgn != NULL)
			DeleteObject(hRgn);
		
		// NOTE: RectRgn is exclusive of bottom and right edges
		hRgn = CreateRectRgn(cx, cy, cx + cw, cy + ch);
		WOBJ_GraphicsHRgn(gr) = (void *)hRgn;
		}
	v.obj = 0;
	return v;
	}

typedef struct
	{
	RGNDATAHEADER rgh;
	RECT rect;
	} RECTRGN;

static Var GraphicsGetClip(Var stack[])
	{
	WObject gr, rect;
	HRGN hRgn;
	RECTRGN rRgn;
	DWORD count;
	Var v;

	v.obj = 0;
	gr = stack[0].obj;
	rect = stack[1].obj;
	if (rect == 0 || WOBJ_GraphicsIsValid(gr) != 1)
		return v;
	hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
	if (hRgn == NULL)
		return v;
	count = GetRegionData(hRgn, sizeof(RECTRGN), (RGNDATA *)&rRgn);
	// note: Win32 API says 1 is returned upon success but testing shows
	// that sizeof(RECTRGN) is returned upon success
	if (count != 1 && count != sizeof(RECTRGN))
		return v;
	WOBJ_RectX(rect) = rRgn.rect.left - WOBJ_GraphicsTransX(gr);
	WOBJ_RectY(rect) = rRgn.rect.top - WOBJ_GraphicsTransY(gr);
	WOBJ_RectWidth(rect) = rRgn.rect.right - rRgn.rect.left;
	WOBJ_RectHeight(rect) = rRgn.rect.bottom - rRgn.rect.top;
	v.obj = rect;
	return v;
	}

static Var GraphicsClearClip(Var stack[])
	{
	WObject gr;
	Var v;

	gr = stack[0].obj;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		{
		HRGN hRgn;

		hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
		if (hRgn != NULL)
			DeleteObject(hRgn);

		WOBJ_GraphicsHRgn(gr) = NULL;
		}
	v.obj = 0;
	return v;
	}

static Var GraphicsTranslate(Var stack[])
	{
	WObject gr;
	int32 transX, transY;
	Var v;

	gr = stack[0].obj;
	transX = WOBJ_GraphicsTransX(gr) + stack[1].intValue;
	transY = WOBJ_GraphicsTransY(gr) + stack[2].intValue;
	if (WOBJ_GraphicsIsValid(gr) == 1)
		{
		WOBJ_GraphicsTransX(gr) = transX;
		WOBJ_GraphicsTransY(gr) = transY;
		}
	v.obj = 0;
	return v;
	}

#define GR_FILLRECT   0
#define GR_DRAWLINE   1
#define GR_FILLPOLY   2
#define GR_DRAWCHARS  3
#define GR_DRAWSTRING 4
#define GR_DOTS       5
#define GR_COPYRECT   6
#define GR_DRAWCURSOR 7

static Var GraphicsDraw(int type, Var stack[])
	{
	WObject gr;
	HDC hDC;
	HWND hWnd;
	HBITMAP hBitmap;
	HRGN hRgn;
	int32 transX, transY;
	int32 drawOp;
	Var v;

	v.obj = 0;
	gr = stack[0].obj;
	if (WOBJ_GraphicsIsValid(gr) == 0)
		return v;

	hWnd = (HWND)WOBJ_GraphicsHWnd(gr);
	hBitmap = (HBITMAP)WOBJ_GraphicsHBitmap(gr);
	hDC = NULL;
	if (hWnd != NULL)
		hDC = GetDC(hWnd);
	else if (hBitmap != NULL)
		{
		// NOTE: NULL creates a DC compatible with the display
		hDC = CreateCompatibleDC(NULL);
		SelectObject(hDC, hBitmap);
		}
	if (hDC == NULL)
		return v;

	// NOTE: SelectClipRgn() makes a copy of the region so it can be
	// deleted even if it is selected into a DC (since the DC keeps a copy)
	hRgn = (HRGN)WOBJ_GraphicsHRgn(gr);
	SelectClipRgn(hDC, hRgn);

	transX = WOBJ_GraphicsTransX(gr);
	transY = WOBJ_GraphicsTransY(gr);
	SetViewportOrgEx(hDC, transX, transY, NULL);

	// NOTE: Win32 uses a value of 1 for white and 0 for black.
	// this is the opposite of what is supported by Waba - so
	// XOR becomes NOTXOR, AND becomes OR and OR becomes AND
	drawOp = WOBJ_GraphicsDrawOp(gr);
	switch(drawOp)
		{
		case DRAW_XOR:
			SetROP2(hDC, R2_NOTXORPEN); // ~(S ^ D)
			break;
		case DRAW_AND:
			SetROP2(hDC, R2_MERGEPEN); // S | D
			break;
		case DRAW_OR:
			SetROP2(hDC, R2_MASKPEN); // S & D
			break;
		default: // including DRAW_OVER
			SetROP2(hDC, R2_COPYPEN);
			break;
		}

	switch(type)
		{
		case GR_FILLRECT:
			{
			int32 x, y, w, h;
			HPEN hPen, oldPen;
			HBRUSH hBrush, oldBrush;

			x = stack[1].intValue;
			y = stack[2].intValue;
			w = stack[3].intValue;
			h = stack[4].intValue;
			hPen = (HPEN)WOBJ_GraphicsHPen(gr);
			oldPen = SelectObject(hDC, hPen);
			hBrush = (HBRUSH)WOBJ_GraphicsHBrush(gr);
			oldBrush = SelectObject(hDC, hBrush);
			Rectangle(hDC, x, y, x + w, y + h);
			// NOTE: We reselect the old pen and brush since we can't delete
			// a pen or brush while it is selected into a DC - see setColor()
			SelectObject(hDC, oldPen);
			SelectObject(hDC, oldBrush);
			break;
			}
		case GR_DRAWLINE:
			{
			POINT point[3];
			HPEN hPen, oldPen;

			// NOTE: Polyline doesn't draw the last point so we make
			// a third point which is 1 pixel away from the last point.
			point[0].x = stack[1].intValue;
			point[0].y = stack[2].intValue;
			point[1].x = stack[3].intValue;
			point[1].y = stack[4].intValue;
			point[2].x = stack[3].intValue + 1;
			point[2].y = stack[4].intValue;
			hPen = (HPEN)WOBJ_GraphicsHPen(gr);
			oldPen = SelectObject(hDC, hPen);
			Polyline(hDC, point, 3);
			// NOTE: See note in FILLRECT for why we select another pen
			SelectObject(hDC, oldPen);
			break;
			}
		case GR_FILLPOLY:
			{
			WObject xArray, yArray;
			int32 i, count, *x, *y;
			POINT *points;
			HPEN hPen, oldPen;
			HBRUSH hBrush, oldBrush;

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
			points = xmalloc(count * sizeof(POINT));
			if (points == NULL)
				break;
			for (i = 0; i < count; i++)
				{
				points[i].x = x[i];
				points[i].y = y[i];
				}
			hPen = (HPEN)WOBJ_GraphicsHPen(gr);
			oldPen = SelectObject(hDC, hPen);
			hBrush = (HBRUSH)WOBJ_GraphicsHBrush(gr);
			oldBrush = SelectObject(hDC, hBrush);
			Polygon(hDC, points, count);
			xfree(points);
			// NOTE: See note in FILLRECT for why we select here
			SelectObject(hDC, oldPen);
			SelectObject(hDC, oldBrush);
			break;
			}
		case GR_DRAWCHARS:
		case GR_DRAWSTRING:
			{
			WObject string, charArray;
			int32 x, y, start, count;
			uint16 *chars;
			RECT rect;
			COLORREF rgb;
			HFONT hFont, oldFont;

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
			rect.left = x;
			rect.top = y;
			rect.right = 32767;
			rect.bottom = 32767;
			SetBkMode(hDC, TRANSPARENT);
			rgb = (COLORREF)WOBJ_GraphicsRGB(gr);
			SetTextColor(hDC, rgb);
			hFont = WOBJ_GraphicsHFont(gr);
			oldFont = SelectObject(hDC, hFont);
#ifdef WINCE
			DrawText(hDC, chars, count, &rect,
				DT_NOCLIP | DT_NOPREFIX | DT_LEFT | DT_TOP | DT_SINGLELINE);
#else
			while (count > 0)
				{
				char buf[40];
				int32 i, n;

				n = sizeof(buf);
				if (n > count)
					n = count;
				for (i = 0; i < n; i++)
					buf[i] = (char)chars[i];
				DrawText(hDC, buf, n, &rect,
					DT_NOCLIP | DT_NOPREFIX | DT_LEFT | DT_TOP | DT_SINGLELINE);
				// calc rect and move ahead
				DrawText(hDC, buf, n, &rect,
					DT_CALCRECT | DT_NOCLIP | DT_NOPREFIX | DT_LEFT | DT_TOP | DT_SINGLELINE);
				rect.left = rect.right;
				rect.right = 32767;
				count -= n;
				chars += n;
				}
#endif
			SelectObject(hDC, oldFont);
			break;
			}
		case GR_DOTS:
			{
			int32 x1, y1, x2, y2;
			int32 x, y;
			POINT point[2];
			HPEN hPen, oldPen;

			x1 = stack[1].intValue;
			y1 = stack[2].intValue;
			x2 = stack[3].intValue;
			y2 = stack[4].intValue;
			hPen = (HPEN)WOBJ_GraphicsHPen(gr);
			oldPen = SelectObject(hDC, hPen);
			if (x1 == x2)
				{
				// vertical
				if (y1 > y2)
					{
					y = y1;
					y1 = y2;
					y2 = y;
					}
				point[0].x = x1;
				point[1].x = x1;
				for (; y1 <= y2; y1 += 2)
					{
					point[0].y = y1;
					point[1].y = y1 + 1;
					Polyline(hDC, point, 2);
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
				point[0].y = y1;
				point[1].y = y1;
				for (; x1 <= x2; x1 += 2)
					{
					point[0].x = x1;
					point[1].x = x1 + 1;
					Polyline(hDC, point, 2);
					}
				}
			// NOTE: See note in FILLRECT for why we select another pen
			SelectObject(hDC, oldPen);
			break;
			}
		case GR_COPYRECT:
			{
			WObject srcSurf, dstSurf;
			int x, y, w, h, dstX, dstY;
			int srcSurfaceType;
			DWORD dwRop;

			srcSurf = stack[1].obj;
			x = stack[2].intValue;
			y = stack[3].intValue;
			w = stack[4].intValue;
			h = stack[5].intValue;
			dstX = stack[6].intValue;
			dstY = stack[7].intValue;
			if (srcSurf == 0)
				break;
			// NOTE: see note above along side call to SetRop2() for why
			// these are opposite
			switch (drawOp)
				{
				case DRAW_AND:
					dwRop = SRCPAINT; // (S | D)
					break;
				case DRAW_OR:
					dwRop = SRCAND; // (S & D)
					break;
				case DRAW_XOR:
					dwRop = SRCINVERT; // (S ^ D) - inverted after the blit below
					break;
				default: // including DRAW_OVER:
					dwRop = SRCCOPY;
					break;
				}
			dstSurf = WOBJ_GraphicsSurface(gr);
			if (dstSurf == srcSurf)
				BitBlt(hDC, dstX, dstY, w, h, hDC, x, y, dwRop);
			else
				{
				srcSurfaceType = SurfaceGetType(srcSurf);
				if (srcSurfaceType == SURF_WINDOW)
					{
					HDC hSrcDC;
					HWND hSrcWnd;

					hSrcWnd = WOBJ_WindowHWnd(srcSurf);
					hSrcDC = GetDC(hSrcWnd);
					BitBlt(hDC, dstX, dstY, w, h, hSrcDC, x, y, dwRop);
					ReleaseDC(hSrcWnd, hSrcDC);
					}
				else if (srcSurfaceType == SURF_IMAGE)
					{
					HDC hMemDC;
					HBITMAP hBitmap;

					hBitmap = WOBJ_ImageHBitmap(srcSurf);
					if (hBitmap != NULL)
						{
						hMemDC = CreateCompatibleDC(hDC);
						SelectObject(hMemDC, hBitmap);
						BitBlt(hDC, dstX, dstY, w, h, hMemDC, x, y, dwRop);
						DeleteDC(hMemDC);
						}
					}
				}
			// complete by inverting the destination to make ~(S ^ D) for XOR
			if (drawOp == DRAW_XOR)
				BitBlt(hDC, dstX, dstY, w, h, hDC, dstX, dstY, DSTINVERT);
			break;
			}
		case GR_DRAWCURSOR:
			{
			int32 x, y, w, h;
			POINT point[3];
			HPEN oldPen;
			HBRUSH oldBrush;

			// an XOR fill rect
			x = stack[1].intValue;
			y = stack[2].intValue;
			w = stack[3].intValue;
			h = stack[4].intValue;
			oldPen = SelectObject(hDC, GetStockObject(BLACK_PEN));
			oldBrush = SelectObject(hDC, GetStockObject(BLACK_BRUSH));
			SetROP2(hDC, R2_NOTXORPEN); // ~(S ^ D)
			if (w == 1)
				{
				// Win32 has problem with XOR width=1 rects
				point[0].x = x;
				point[0].y = y;
				point[1].x = x;
				point[1].y = y + h - 1;
				point[2].x = x + 1;
				point[2].y = point[1].y;
				Polyline(hDC, point, 3);
				}
			else
				Rectangle(hDC, x, y, x + w, y + h);
			SelectObject(hDC, oldPen);
			SelectObject(hDC, oldBrush);
			break;
			}
		}
	if (hWnd != NULL)
		ReleaseDC(hWnd, hDC);
	else
		DeleteDC(hDC);
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
// SoundClip
// var[0] = Class
// var[1] = path
// var[2] = loaded
//

#define WOBJ_SoundClipPath(o) (objectPtr(o))[1].obj
#define WOBJ_SoundClipLoaded(o) (objectPtr(o))[2].intValue

static Var SoundClipPlay(Var stack[])
	{
	WObject sound, pathString;
	UtfString path;
	TCHAR pathBuf[256];
	Var v;

	sound = stack[0].obj;
	pathString = WOBJ_SoundClipPath(sound);
	path = stringToUtf(pathString, STU_NULL_TERMINATE | STU_USE_STATIC);
	asciiToUnicode(path.str, pathBuf, 256);
	v.intValue = sndPlaySound(pathBuf, SND_NODEFAULT | SND_NOSTOP | SND_ASYNC);
	return v;
	}

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
	WObject string, charArray;
	int32 i, isNeg, len, value;
	uint16 *chars;
	Var v;

	// NOTE: We do it all here instead of calling atoi() since it looks
	// like various versions of CE don't support atoi(). It's also faster
	// this way since we don't have to convert to a byte array.
	v.intValue = 0;
	string = stack[0].obj;
	if (string == 0)
		return v;
	charArray = WOBJ_StringCharArrayObj(string);
	if (charArray == 0)
		return v;
	chars = (uint16 *)WOBJ_arrayStart(charArray);
	len = WOBJ_arrayLen(charArray);
	isNeg = 0;
	if (len > 0 && chars[0] == '-')
		isNeg = 1;
	value = 0;
	for (i = isNeg; i < len; i++)
		{
		if (chars[i] < (uint16)'0' || chars[i] > (uint16)'9')
			return v;
		value = (value * 10) + ((int32)chars[i] - (int32)'0');
		}
	if (isNeg)
		value = -(value);
	v.intValue = value;
	return v;
	}

static Var ConvertIntToString(Var stack[])
	{
	Var v;
	char buf[20];

#ifdef WINCE
		{
		TCHAR tbuf[20];
		int i;

		wsprintf(tbuf, L"%d", stack[0].intValue);
		for (i = 0; i < 20; i++)
			{
			buf[i] = (char)tbuf[i];
			if (buf[i] == 0)
				break;
			}
		}
#else
	sprintf(buf, "%d", stack[0].intValue);
#endif
	v.obj = createString(buf);
	return v;
	}

static Var ConvertFloatToString(Var stack[])
	{
	Var v;
	char buf[40];
//	int len;

#ifdef WINCE
	// NOTE: This isn't nice. But wsprintf() doesn't have a %f option and
	// CE only has wsprintf.
		{
		TCHAR tbuf[40];
		int32 i, n;

		i = (int32)stack[0].floatValue;
		n = wsprintf(tbuf, L"%d", i);
		tbuf[n++] = '.';
		i = (int32)((stack[0].floatValue - (float32)i) * 100000.0);
		if (i < 0)
			i = - i;
		wsprintf(&tbuf[n], L"%.5d", i);
		for (i = 0; i < 60; i++)
			{
			buf[i] = (char)tbuf[i];
			if (buf[i] == 0)
				break;
			}
		}
#else
	//	_gcvt(stack[0].floatValue, 40 - 4, buf);
	//	len = xstrlen(buf);
	//	if (buf[len - 1] == '.')
	//		buf[len - 1] = 0;
	sprintf(buf, "%f", stack[0].floatValue);
#endif
	v.obj = createString(buf);
	return v;
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

/*
static Var ConvertStringToFloat(Var stack[])
	{
	WObject string, byteArray;
	uchar *bytes;
	Var v;

	v.floatValue = 0.0;
	string = stack[0].obj;
	if (string == 0)
		return v;
	.. stringToUtf..
	v.floatValue = (float32)atof((char *)s.str);
	return v;
	}
*/

//
// Catalog
//
// var[0] = Class
// var[1] = hook var - database handle (null if no open db)
// var[2] = hook var - database oid
// var[3] = hook var - current record position
// var[4] = hook var - current record oid
// var[5] = hook var - current record memory pointer
// var[6] = hook var - current record memory allocation pointer
// var[7] = hook var - length of current record
// var[8] = hook var - current offset in record
// var[9] = hook var - 1 if record has been modified, 0 otherwise
#define WOBJ_CatalogHandle(o) (objectPtr(o))[1].refValue
#define WOBJ_CatalogOID(o) (objectPtr(o))[2].refValue
#define WOBJ_CatalogCurRecPos(o) (objectPtr(o))[3].intValue
#define WOBJ_CatalogCurRecOid(o) (objectPtr(o))[4].refValue
#define WOBJ_CatalogCurRecPtr(o) (objectPtr(o))[5].refValue
#define WOBJ_CatalogCurRecAllocPtr(o) (objectPtr(o))[6].refValue
#define WOBJ_CatalogCurRecLen(o) (objectPtr(o))[7].intValue
#define WOBJ_CatalogCurRecOffset(o) (objectPtr(o))[8].intValue
#define WOBJ_CatalogCurRecModified(o) (objectPtr(o))[9].intValue

#define Catalog_READ_ONLY 1
#define Catalog_WRITE_ONLY 2
#define Catalog_READ_WRITE 3
#define Catalog_CREATE 4

#define Catalog_CEDBTYPE 4141

#ifndef WINCE
static Var CatalogCreate(Var stack[]) { Var v; v.obj = 0; return v; }
static void CatalogDestroy(WObject cat) {}
static Var CatalogIsOpen(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogClose(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogDelete(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogListCatalogs(Var stack[]) { Var v; v.obj = 0; return v; }
static Var CatalogResizeRecord(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogGetRecordSize(Var stack[]) { Var v; v.intValue = -1; return v; }
static Var CatalogGetRecordCount(Var stack[]) { Var v; v.intValue = -1; return v; }
static Var CatalogDeleteRecord(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogAddRecord(Var stack[]) { Var v; v.intValue = -1; return v; }
static Var CatalogSetRecordPos(Var stack[]) { Var v; v.intValue = 0; return v; }
static Var CatalogRead(Var stack[]) { Var v; v.intValue = -1; return v; }
static Var CatalogWrite(Var stack[]) { Var v; v.intValue = -1; return v; }
static Var CatalogSkipBytes(Var stack[]) { Var v; v.intValue = -1; return v; }
#else

static void _RecClose(WObject cat)
	{
	HANDLE hDb;
	int32 pos;

	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return;
	if (WOBJ_CatalogCurRecModified(cat) != 0)
		{
		CEOID recOid;
		CEPROPVAL props[2];
		CEBLOB blob;

		// fill in pos property
		props[0].propid = CEVT_UI4;
		props[0].wFlags = 0;
		props[0].val.ulVal = (ULONG)pos;
		// fill in blob property
		blob.dwCount = WOBJ_CatalogCurRecLen(cat);
		blob.lpb = WOBJ_CatalogCurRecPtr(cat);
		props[1].propid = CEVT_BLOB;
		props[1].wFlags = 0;
		props[1].val.blob = blob;
		recOid = (CEOID)WOBJ_CatalogCurRecOid(cat);
		// rewrite record
		CeWriteRecordProps(hDb, recOid, 2, props);
		}
	LocalFree(WOBJ_CatalogCurRecAllocPtr(cat));
	WOBJ_CatalogCurRecPos(cat) = -1;	
	}

static int32 _CatalogClose(WObject cat)
	{
	HANDLE hDb;

	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return 0;
	_RecClose(cat); // release any open records
	CloseHandle(hDb);
	WOBJ_CatalogHandle(cat) = (void *)INVALID_HANDLE_VALUE;
	return 1;
	}

static Var CatalogCreate(Var stack[])
	{
	UtfString s;
	WObject cat, name;
	int32 mode;
	CEOID dbOid;
	HANDLE hDb;
	TCHAR nameBuf[32];
	Var v;

	v.obj = 0;
	cat = stack[0].obj;
	name = stack[1].obj;
	mode = stack[2].intValue;
	WOBJ_CatalogHandle(cat) = (void *)INVALID_HANDLE_VALUE;
	WOBJ_CatalogCurRecPos(cat) = -1;
	s = stringToUtf(name, STU_NULL_TERMINATE | STU_USE_STATIC);
	if (s.len == 0)
		return v;
	// NOTE: 32 characters is the max name size under CE
	asciiToUnicode(s.str, nameBuf, 32);
	dbOid = 0;
	hDb = CeOpenDatabase(&dbOid, nameBuf, 0, 0, NULL);
	if (mode == Catalog_CREATE && hDb == INVALID_HANDLE_VALUE)
		{
		SORTORDERSPEC sort[1];

		// NOTE: When you add a record to a CE database, it doesn't
		// add it to the end if there is no sorting. It seems its arbitrary
		// where the record gets added if there is no sort. Since we
		// need to keep everything in order, we create an uint32 index
		// as the first property in each record and keep it sorted
		// in the order of the records.
		sort[0].propid = CEVT_UI4; // pos
		sort[0].dwFlags = 0; // ascending
		dbOid = CeCreateDatabase(nameBuf, Catalog_CEDBTYPE, 1, sort);
		if (dbOid == 0)
			return v;
		dbOid = 0;
		hDb = CeOpenDatabase(&dbOid, nameBuf, 0, 0, NULL);
		}
	if (hDb == INVALID_HANDLE_VALUE)
		return v;
	WOBJ_CatalogHandle(cat) = (void *)hDb;
	WOBJ_CatalogOID(cat) = (void *)dbOid;
	return v;
	}

static Var CatalogIsOpen(Var stack[])
	{
	WObject cat;
	HANDLE hDb;
	Var v;

	cat = stack[0].obj;
	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
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
	HANDLE hDb;
	CEOID dbOid;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return v;
	dbOid = (CEOID)WOBJ_CatalogOID(cat);
	_CatalogClose(cat);
	if (CeDeleteDatabase(dbOid) != TRUE)
		return v;
	v.intValue = 1;
	return v;
	}

static Var CatalogListCatalogs(Var stack[])
	{
	HANDLE hEnum;
	CEOID dbOid;
	CEOIDINFO oidInfo;
	WObject stringArray, *strings;
	TCHAR *name;
	int32 i, n;
	Var v;

	v.obj = 0;
	hEnum = CeFindFirstDatabase(Catalog_CEDBTYPE);
	if (hEnum == INVALID_HANDLE_VALUE)
		return v;
	n = 0;
	while (dbOid = CeFindNextDatabase(hEnum))
		n++;
	CloseHandle(hEnum);
	if (n == 0)
		return v;
	hEnum = CeFindFirstDatabase(Catalog_CEDBTYPE);
	if (hEnum == INVALID_HANDLE_VALUE)
		return v;
	stringArray = createArrayObject(1, n);
	if (pushObject(stringArray) == -1)
		goto freereturn;
	i = 0;
	while (dbOid = CeFindNextDatabase(hEnum))
		{
		// we need to recompute the start pointer each iteration
		// in case garbage collection during a string create causes
		// memory to move around
		strings = (WObject *)WOBJ_arrayStart(stringArray);
		if (CeOidGetInfo(dbOid, &oidInfo) != TRUE)
			name = TEXT("<invalid>");
		else
			name = oidInfo.infDatabase.szDbaseName;
		strings[i++] = createStringFromUnicode(name, lstrlen(name));
		if (i == n)
			break; // in case database was created after count
		}
	popObject(); // stringArray
	v.obj = stringArray;
freereturn:
	CloseHandle(hEnum);
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

static int32 _CatalogGetRecCount(WObject cat)
	{
	HANDLE hDb;
	CEOID dbOid;
	CEOIDINFO oidInfo;

	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return -1;
	dbOid = (CEOID)WOBJ_CatalogOID(cat);
	if (CeOidGetInfo(dbOid, &oidInfo) != TRUE)
		return -1;
	return (int32)oidInfo.infDatabase.wNumRecords;
	}

static Var CatalogGetRecordCount(Var stack[])
	{
	WObject cat;
	Var v;

	cat = stack[0].obj;
	v.intValue = _CatalogGetRecCount(cat);
	return v;
	}

static Var CatalogDeleteRecord(Var stack[])
	{
	WObject cat;
	int32 i, pos, count;
	HANDLE hDb;
	CEOID recOid;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return v;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return v;
	recOid = (CEOID)WOBJ_CatalogCurRecOid(cat);
	_RecClose(cat);
	count = _CatalogGetRecCount(cat);
	if (CeDeleteRecord(hDb, recOid) != TRUE)
		return v;
	for (i = pos + 1; i < count; i++)
		{
		CEPROPVAL posProp;
		DWORD idx;

		posProp.propid = CEVT_UI4;
		posProp.wFlags = 0;
		posProp.val.ulVal = (ULONG)i;
		recOid = CeSeekDatabase(hDb, CEDB_SEEK_VALUEFIRSTEQUAL, (DWORD)&posProp, &idx);
		if (recOid == 0)
			continue; // database has problems
		posProp.val.ulVal = (ULONG)(i - 1);
		CeWriteRecordProps(hDb, recOid, 1, &posProp);
		}
	v.intValue = 1;
	return v;
	}

static Var CatalogResizeRecord(Var stack[])
	{
	WObject cat;
	int32 size, pos, oldSize, copyLen;
	BYTE *lpb, *p;
	Var v;

	cat = stack[0].obj;
	size = stack[1].intValue;
	v.intValue = 0;
	if (size < 0)
		return v;
	pos = WOBJ_CatalogCurRecPos(cat);
	if (pos == -1)
		return v;
	lpb = (BYTE *)LocalAlloc(LMEM_FIXED, size);
	if (lpb == NULL)
		return v;
	// copy old record potion into new record
	oldSize = WOBJ_CatalogCurRecLen(cat);
	if (oldSize < size)
		copyLen = oldSize;
	else
		copyLen = size;
	p = WOBJ_CatalogCurRecPtr(cat);
	xmemmove(lpb, p, copyLen);
	// free old ptr and set new data
	LocalFree(WOBJ_CatalogCurRecAllocPtr(cat));
	WOBJ_CatalogCurRecPtr(cat) = lpb;
	WOBJ_CatalogCurRecAllocPtr(cat) = lpb;
	WOBJ_CatalogCurRecLen(cat) = size;
	WOBJ_CatalogCurRecModified(cat) = TRUE;
	v.intValue = 1;
	return v;
	}

static Var CatalogAddRecord(Var stack[])
	{
	WObject cat;
	HANDLE hDb;
	CEOID recOid;
	CEPROPVAL props[2];
	CEBLOB blob;
	int32 size, pos;
	Var v;

	v.intValue = -1;
	cat = stack[0].obj;
	size = stack[1].intValue;
	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return v;
	if (size < 0)
		return v;
	_RecClose(cat);
	pos = _CatalogGetRecCount(cat);
	if (pos == -1)
		return v;
	props[0].propid = CEVT_UI4;
	props[0].wFlags = 0;
	props[0].val.ulVal = (ULONG)pos;
	// allocate/zero a blob property
	blob.dwCount = size;
	blob.lpb = (BYTE *)LocalAlloc(LMEM_FIXED, size);
	if (blob.lpb == NULL)
		return v;
	xmemzero(blob.lpb, size);
	props[1].propid = CEVT_BLOB;
	props[1].wFlags = 0;
	props[1].val.blob = blob;
	// write the record
	recOid = CeWriteRecordProps(hDb, 0, 2, props);
	if (recOid == 0)
		{
		LocalFree(blob.lpb);
		return v;
		}
	WOBJ_CatalogCurRecPos(cat) = pos;
	WOBJ_CatalogCurRecOid(cat) = (void *)recOid;
	WOBJ_CatalogCurRecPtr(cat) = (void *)blob.lpb;
	WOBJ_CatalogCurRecAllocPtr(cat) = (void *)blob.lpb;
	WOBJ_CatalogCurRecLen(cat) = size;
	WOBJ_CatalogCurRecOffset(cat) = 0;
	WOBJ_CatalogCurRecModified(cat) = 0;
	v.intValue = pos;
	return v;
	}

static Var CatalogSetRecordPos(Var stack[])
	{
	WObject cat;
	int32 pos;
	HANDLE hDb;
	CEOID recOid;
	DWORD idx, size;
	WORD nProps;
	CEPROPVAL posProp, *props;
	Var v;

	v.intValue = 0;
	cat = stack[0].obj;
	pos = stack[1].intValue;
	hDb = (HANDLE)WOBJ_CatalogHandle(cat);
	if (hDb == INVALID_HANDLE_VALUE)
		return v;
	_RecClose(cat);
	if (pos < 0)
		return v;
	// seek to record
	posProp.propid = CEVT_UI4;
	posProp.wFlags = 0;
	posProp.val.ulVal = (ULONG)pos;
	recOid = CeSeekDatabase(hDb, CEDB_SEEK_VALUEFIRSTEQUAL, (DWORD)&posProp, &idx);
	if (recOid == 0)
		return v;
	// read record
	nProps = 0;
	props = NULL;
	recOid = CeReadRecordProps(hDb, CEDB_ALLOWREALLOC, &nProps,
		NULL, (LPBYTE *)&props, &size);
	if (!props)
		return v;
	if (recOid == 0 || nProps != 2 ||
		props[0].propid != CEVT_UI4 ||
		props[0].wFlags == CEDB_PROPNOTFOUND ||
		props[1].propid != CEVT_BLOB ||
		props[1].wFlags == CEDB_PROPNOTFOUND)
		{
		LocalFree(props);
		return v;
		}
	WOBJ_CatalogCurRecPos(cat) = pos;
	WOBJ_CatalogCurRecOid(cat) = (void *)recOid;
	WOBJ_CatalogCurRecPtr(cat) = (void *)props[1].val.blob.lpb;
	WOBJ_CatalogCurRecAllocPtr(cat) = (void *)props;
	WOBJ_CatalogCurRecLen(cat) = props[1].val.blob.dwCount;
	WOBJ_CatalogCurRecOffset(cat) = 0;
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
		xmemmove(&recBytes[recOffset], &bytes[start], count);
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
#endif

//
// File
//
// var[0] = Class
// var[1] = String name
// var[2] = int mode
// var[3] = hook var - Win32 handle

#define WOBJ_FileName(o) (objectPtr(o))[1].obj
#define WOBJ_FileMode(o) (objectPtr(o))[2].intValue
#define WOBJ_FileHandle(o) (objectPtr(o))[3].refValue

#define File_DONT_OPEN 0
#define File_READ_ONLY 1
#define File_WRITE_ONLY 2
#define File_READ_WRITE 3
#define File_CREATE 4

static TCHAR *_FileAllocLPCTSTR(WObject string, TCHAR *add)
	{
	WObject charArray;
	TCHAR *s;
	int32 len, addLen, i, j;
	int16 *chars;

	if (string == 0)
		return NULL;
	charArray = WOBJ_StringCharArrayObj(string);
	if (charArray == 0)
		return NULL;
	if (add != NULL)
		addLen = lstrlen(add);
	else
		addLen = 0;
	len = WOBJ_arrayLen(charArray);
	chars = (int16 *)WOBJ_arrayStart(charArray);
	s = (TCHAR *)xmalloc((len + addLen + 1) * sizeof(TCHAR));
	if (s == NULL)
		return NULL;
	j = 0;
	for (i = 0; i < len; i++)
		s[j++] = (TCHAR)chars[i];
	for (i = 0; i < addLen; i++)
		s[j++] = add[i];
	s[j] = 0;
	return s;
	}

static void _FileFreeLPCTSTR(LPCTSTR path)
	{
	if (path != NULL)
		xfree((void *)path);
	}

static int32 _FileClose(WObject file)
	{
	HANDLE fileH;

	fileH = WOBJ_FileHandle(file);
	if (fileH != INVALID_HANDLE_VALUE)
		CloseHandle(fileH);
	WOBJ_FileHandle(file) = INVALID_HANDLE_VALUE;
	return 1;
	}

static Var FileCreate(Var stack[])
	{
	WObject file;
	Var v;
	int32 fileMode;
	DWORD rwMode;
	DWORD createDis;
	LPCTSTR path;
	HANDLE fileH;

	file = stack[0].obj;
	v.obj = 0;
	fileMode = WOBJ_FileMode(file);
	rwMode = 0;
	if (fileMode == File_READ_ONLY)
		rwMode = GENERIC_READ;
	else if (fileMode == File_WRITE_ONLY)
		rwMode = GENERIC_WRITE;
	else if (fileMode == File_READ_WRITE || fileMode == File_CREATE)
		rwMode = GENERIC_READ | GENERIC_WRITE;
	if (fileMode == File_CREATE)
		createDis = OPEN_ALWAYS;
	else
		createDis = OPEN_EXISTING;
	WOBJ_FileHandle(file) = INVALID_HANDLE_VALUE;
	if (rwMode == 0)
		return v;
	path = _FileAllocLPCTSTR(WOBJ_FileName(file), NULL);
	if (path == NULL)
		return v;
	fileH = CreateFile(path, rwMode, FILE_SHARE_READ, NULL,
		createDis, FILE_ATTRIBUTE_NORMAL, NULL);
	_FileFreeLPCTSTR(path);
	WOBJ_FileHandle(file) = (void *)fileH;
	}

static void FileDestroy(WObject file)
	{
	_FileClose(file);
	}

static Var FileIsOpen(Var stack[])
	{
	WObject file;
	HANDLE fileH;
	Var v;

	file = stack[0].obj;
	fileH = WOBJ_FileHandle(file);
	v.intValue = 0;
	if (fileH == INVALID_HANDLE_VALUE)
		return v;
	v.intValue = 1;
	return v;
	}

static Var FileGetLength(Var stack[])
	{
	WObject file;
	HANDLE fileH;
	DWORD len;
	Var v;

	file = stack[0].obj;
	fileH = WOBJ_FileHandle(file);
	v.intValue = 0;
	if (fileH == INVALID_HANDLE_VALUE)
		return v;
	len = GetFileSize(fileH, NULL);
	if (len != 0xFFFFFFFF)
		v.intValue = len;
	return v;
	}

#define FILE_CREATE_DIR 1
#define FILE_IS_DIR 2
#define FILE_DELETE 3
#define FILE_RENAME 4
#define FILE_EXISTS 5

static Var FileOp(Var stack[], int op)
	{
	WObject file;
	LPCTSTR path;
	Var v;

	v.intValue = 0;
	file = stack[0].obj;
	path = _FileAllocLPCTSTR(WOBJ_FileName(file), NULL);
	if (path == NULL)
		return v;
	switch (op)
		{
		case FILE_CREATE_DIR:
			{
			if (CreateDirectory(path, NULL) == 0)
				v.intValue = 1;
			break;
			}
		case FILE_IS_DIR:
			{
			DWORD attr;

			attr = GetFileAttributes(path);
			if ((attr != 0xFFFFFFFF) && (attr & FILE_ATTRIBUTE_DIRECTORY))
				v.intValue = 1;
			break;
			}
		case FILE_DELETE:
			{
			int isDir;
			DWORD attr;

			_FileClose(file);
			isDir = 0;
			attr = GetFileAttributes(path);
			if ((attr != 0xFFFFFFFF) && (attr & FILE_ATTRIBUTE_DIRECTORY))
				isDir = 1;
			if (isDir)
				{
				if (RemoveDirectory(path) != 0)
					v.intValue = 1;
				}
			else
				{
				if (DeleteFile(path) != 0)
					v.intValue = 1;
				}
			break;
			}
		case FILE_RENAME:
			{
			LPCTSTR dstPath;

			dstPath = _FileAllocLPCTSTR(stack[1].obj, NULL);
			if (dstPath != NULL)
				{
				_FileClose(file);
				if (MoveFile(path, dstPath) != 0)
					v.intValue = 1;
				_FileFreeLPCTSTR(dstPath);
				}
			break;
			}
		case FILE_EXISTS:
			{
			DWORD attr;

			attr = GetFileAttributes(path);
			if (attr != 0xFFFFFFFF)
				v.intValue = 1;
			break;
			}
		}
	_FileFreeLPCTSTR(path);
	return v;
	}

static Var FileCreateDir(Var stack[])
	{
	return FileOp(stack, FILE_CREATE_DIR);
	}

static Var FileIsDir(Var stack[])
	{
	return FileOp(stack, FILE_IS_DIR);
	}

static Var FileDelete(Var stack[])
	{
	return FileOp(stack, FILE_DELETE);
	}

static Var FileRename(Var stack[])
	{
	return FileOp(stack, FILE_RENAME);
	}

static Var FileExists(Var stack[])
	{
	return FileOp(stack, FILE_EXISTS);
	}

static Var FileSeek(Var stack[])
	{
	WObject file;
	int32 pos;
	HANDLE fileH;
	Var v;

	v.intValue = 0;
	file = stack[0].obj;
	pos = stack[1].intValue;
	fileH = WOBJ_FileHandle(file);
	if (fileH == INVALID_HANDLE_VALUE)
		return v;
	if (SetFilePointer(fileH, pos, NULL, FILE_BEGIN) == (DWORD)pos)
		v.intValue = 1;
	return v;
	}

static Var FileClose(Var stack[])
	{
	Var v;

	v.intValue = _FileClose(stack[0].obj);
	return v;
	}

typedef struct FileListItemStruct
	{
	TCHAR *fileName;
	struct FileListItemStruct *next;
	} FileListItem;

static Var FileListDir(Var stack[])
	{
	WObject file, stringArray, *strings;
	TCHAR *path;
	HANDLE findH;
	WIN32_FIND_DATA findData;
	TCHAR *fileName;
	FileListItem *list, *item;
	int i, numItems;
	Var v;

	v.obj = 0;
	file = stack[0].obj;
	path = _FileAllocLPCTSTR(WOBJ_FileName(file), TEXT("/*"));
	if (path == NULL)
		return v;

	// read paths into linked list
	findH = FindFirstFile(path, &findData);
	_FileFreeLPCTSTR(path);
	if (findH == INVALID_HANDLE_VALUE)
		return v;
	list = NULL;
	numItems = 0;
	do	{
		fileName = findData.cFileName;
		if ((fileName[0] == '.' && fileName[1] == 0) ||
			(fileName[0] == '.' && fileName[1] == '.' && fileName[2] == 0))
			continue;
		item = (FileListItem *)xmalloc(sizeof(FileListItem));
		if (item == NULL)
			break;
		item->fileName = malloc((lstrlen(fileName) + 1) * sizeof(TCHAR));
		if (item->fileName == NULL)
			{
			free(item);
			break;
			}
		lstrcpy(item->fileName, findData.cFileName);
		item->next = list;
		list = item;
		numItems++;
		} while (FindNextFile(findH, &findData));
	FindClose(findH);

	// convert linked list into string array
	stringArray = createArrayObject(1, numItems);
	if (pushObject(stringArray) == -1)
		goto freereturn;
	i = numItems - 1;
	item = list;
	while (item)
		{
		// we need to recompute the start pointer each iteration
		// in case garbage collection during a string create causes
		// memory to move around
		strings = (WObject *)WOBJ_arrayStart(stringArray);
#ifdef WINCE
		strings[i--] = createStringFromUnicode(item->fileName,
			lstrlen(item->fileName));
#else
		strings[i--] = createString(item->fileName);
#endif
		item = item->next;
		}
	popObject(); // stringArray

freereturn:
	// free linked list
	while (list)
		{
		item = list;
		list = list->next;
		xfree(item->fileName);
		xfree(item);
		}

	v.obj = stringArray;
	return v;
	}

static Var FileReadWriteBytes(Var stack[], int isRead)
	{
	WObject file, byteArray;
	int32 start, count;
	uchar *bytes;
	HANDLE fileH;
	DWORD numRW;
	Var v;

	v.intValue = -1;
	file = stack[0].obj;
	fileH = WOBJ_FileHandle(file);
	if (fileH == INVALID_HANDLE_VALUE)
		return v;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	if (isRead)
		ReadFile(fileH, (LPVOID)&bytes[start], count, &numRW, NULL);
	else
		WriteFile(fileH, (LPVOID)&bytes[start], count, &numRW, NULL);
	v.intValue = numRW;
	return v;
	}

static Var FileRead(Var stack[])
	{
	return FileReadWriteBytes(stack, 1);
	}

static Var FileWrite(Var stack[])
	{
	return FileReadWriteBytes(stack, 0);
	}

//
// SerialPort
//
// var[0] = Class
// var[1] = hook var - Win32 Handle
//
#define WOBJ_SerialPortHandle(o) (objectPtr(o))[1].refValue

static int32 _SerialPortClose(WObject port)
	{
	HANDLE h;
	BOOL status;

	h = WOBJ_SerialPortHandle(port);
	if (h == NULL)
		return 0;
	status = CloseHandle(h);
	WOBJ_SerialPortHandle(port) = NULL;
	if (!status)
		return 0;
	return 1;
	}

static Var SerialPortCreate(Var stack[])
	{
	WObject port;
	int32 number, baudRate, bits, parity, stopBits;
	Var v;
	TCHAR buf[20];
	HANDLE h;
	COMMTIMEOUTS timeout;
	DCB dcb;

	v.obj = 0;
	port = stack[0].obj;
	number = stack[1].intValue;
	baudRate = stack[2].intValue;
	bits = stack[3].intValue;
	parity = stack[4].intValue;
	stopBits = stack[5].intValue;
	WOBJ_SerialPortHandle(port) = NULL;

	if (number == 0)
		number = 1;
#ifdef WINCE
	wsprintf(buf, L"COM%d:", number);
#else
	sprintf(buf, "COM%d:", number);
#endif
	h = CreateFile(buf, GENERIC_READ | GENERIC_WRITE, 0, NULL,
		OPEN_EXISTING, 0, NULL);
	if (h == INVALID_HANDLE_VALUE)
		return v;

	// set serial timeouts
	GetCommTimeouts(h, &timeout);
	timeout.ReadIntervalTimeout = 0;
	timeout.ReadTotalTimeoutMultiplier = 5;
	timeout.ReadTotalTimeoutConstant = 100;
	timeout.WriteTotalTimeoutMultiplier = 5;
	timeout.WriteTotalTimeoutConstant = 1000;
	SetCommTimeouts(h, &timeout);

	GetCommState(h, &dcb);
	if (baudRate == 0)
		baudRate = 9600;
	dcb.BaudRate = baudRate;
	dcb.ByteSize = bits;
	if (parity == 0)
		dcb.Parity = NOPARITY;
	else
		dcb.Parity = EVENPARITY;
	if (stopBits == 1)
		dcb.StopBits = ONESTOPBIT;
	else if (stopBits == 2)
		dcb.StopBits = TWOSTOPBITS;

	// NOTE: Handshaking is off by default for WindowsCE and on by
	// default for all other Win32 platforms
#ifdef WINCE
	dcb.fOutxCtsFlow = 0;
	dcb.fRtsControl = RTS_CONTROL_ENABLE;
#else
	dcb.fOutxCtsFlow = 1;
	dcb.fRtsControl = RTS_CONTROL_HANDSHAKE;
#endif
	dcb.fOutxDsrFlow = 0;
	dcb.fDtrControl = DTR_CONTROL_DISABLE;
	dcb.fDsrSensitivity = 0;
	dcb.fTXContinueOnXoff = 0;
	dcb.fOutX = 0;
	dcb.fInX = 0;
	dcb.fNull = 0;
	dcb.fAbortOnError = 0;
	if (SetCommState(h, &dcb) == 0)
		{
		CloseHandle(h);
		return v;
		}
	WOBJ_SerialPortHandle(port) = h;
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
	if (WOBJ_SerialPortHandle(port) == NULL)
		v.intValue = 0;
	else
		v.intValue = 1;
	return v;
	}

static Var SerialPortSetReadTimeout(Var stack[])
	{
	HANDLE h;
	WObject port;
	int32 millis;
	COMMTIMEOUTS timeout;
	Var v;

	v.intValue = 0;
	port = stack[0].obj;
	millis = stack[1].intValue;
	h = WOBJ_SerialPortHandle(port);
	if (millis < 0 || h == NULL)
		return v;
	GetCommTimeouts(h, &timeout);
	if (millis == 0)
		{
		timeout.ReadIntervalTimeout = MAXDWORD;
		timeout.ReadTotalTimeoutMultiplier = 0;
		timeout.ReadTotalTimeoutConstant = 0;
		}
	else
		{
		timeout.ReadIntervalTimeout = 0;
		timeout.ReadTotalTimeoutMultiplier = 5;
		timeout.ReadTotalTimeoutConstant = millis;
		}
	if (SetCommTimeouts(h, &timeout))
		return v;
	v.intValue = 1;
	return v;
	}

static Var SerialPortReadCheck(Var stack[])
	{
	Var v;

	v.intValue = -1;
	return v;
	}

static Var SerialPortSetFlowControl(Var stack[])
	{
	HANDLE h;
	WObject port;
	int32 flowOn;
	DCB dcb;
	Var v;

	v.intValue = 0;
	port = stack[0].obj;
	flowOn = stack[1].intValue;
	h = WOBJ_SerialPortHandle(port);
	if (h == NULL)
		return v;
	GetCommState(h, &dcb);
	if (flowOn)
		{
		dcb.fOutxCtsFlow = 1;
		dcb.fRtsControl = RTS_CONTROL_HANDSHAKE;
		}
	else
		{
		dcb.fOutxCtsFlow = 0;
		dcb.fRtsControl = RTS_CONTROL_ENABLE;
		}
	if (SetCommState(h, &dcb) == 0)
		return v;
	v.intValue = 1;
	return v;
	}

static Var SerialPortReadWriteBytes(Var stack[], int isRead)
	{
	WObject port, byteArray;
	int32 start, count;
	uchar *bytes;
	HANDLE h;
	DWORD numRW;
	Var v;

	v.intValue = -1;
	port = stack[0].obj;
	byteArray = stack[1].obj;
	start = stack[2].intValue;
	count = stack[3].intValue;
	h = WOBJ_SerialPortHandle(port);
	if (h == NULL)
		return v; // port not open
	if (arrayRangeCheck(byteArray, start, count) == 0)
		return v; // array null or range invalid
	bytes = (uchar *)WOBJ_arrayStart(byteArray);
	if (isRead)
		ReadFile(h, (LPVOID)&bytes[start], count, &numRW, NULL);
	else
		WriteFile(h, (LPVOID)&bytes[start], count, &numRW, NULL);
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
//

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
	SYSTEMTIME tm;

	time = stack[0].obj;
	GetLocalTime(&tm);
	WOBJ_TimeYear(time) = tm.wYear;
	WOBJ_TimeMonth(time) = tm.wMonth;
	WOBJ_TimeDay(time) = tm.wDay;
	WOBJ_TimeHour(time) = tm.wHour;
	WOBJ_TimeMinute(time) = tm.wMinute;
	WOBJ_TimeSecond(time) = tm.wSecond;
	WOBJ_TimeMillis(time) = tm.wMilliseconds;
	v.obj = 0;
	return v;
	}

//
// Vm
//

static Var VmGetTimeStamp(Var stack[])
	{
	Var v;

	v.intValue = getTimeStamp();
	return v;
	}

static Var VmIsColor(Var stack[])
	{
	Var v;

	v.intValue = 1;
	return v;
	}

static Var VmExec(Var stack[])
	{
	WObject pathString, argsString;
	UtfString path, args;
	int32 doWait;
	BOOL status;
#ifndef WINCE
	STARTUPINFO sInfo;
#endif
	PROCESS_INFORMATION pInfo;
	DWORD exitCode;
	Var v;
#ifdef WINCE
	TCHAR pathBuf[256], argsBuf[256];
#else
	char argsBuf[256];
#endif

	v.intValue = -1;
	pathString = stack[0].obj;
	argsString = stack[1].obj;
	doWait = stack[3].intValue;
	// NOTE: can't use static here since we call stringToUtf on both
	// path and args
	path = stringToUtf(pathString, STU_NULL_TERMINATE);
	if (path.len == 0)
		return v;
	args = stringToUtf(argsString, STU_NULL_TERMINATE | STU_USE_STATIC);
#ifdef WINCE
	asciiToUnicode(path.str, pathBuf, 256);
	asciiToUnicode(args.str, argsBuf, 256);
	status = CreateProcess(pathBuf, argsBuf, NULL, NULL, FALSE,
		0, NULL, NULL, NULL, &pInfo);
#else
	xmemzero(&sInfo, sizeof(sInfo));
	sInfo.cb = sizeof(sInfo);
	// NOTE: CreateProcess() is kind of screwey and the arguments parameter
	// is supposed to include the program name. But if the program name contains
	// a space, it gets confused. So, we use a program name of x here to keep it
	// from getting confused if there is a space in the program name.
	if (args.len + 3 > 256)
		return v;
	argsBuf[0] = 'x';
	argsBuf[1] = ' ';
	xstrncpy(&argsBuf[2], args.str, args.len);
	argsBuf[args.len + 2] = 0;
	status = CreateProcess(path.str, argsBuf, NULL, NULL, FALSE,
		NORMAL_PRIORITY_CLASS, NULL, NULL, &sInfo, &pInfo);
#endif
	if (!status)
		return v;
	CloseHandle(pInfo.hThread);
	if (doWait)
		{
		WaitForSingleObject(pInfo.hProcess, INFINITE);
		v.intValue = GetExitCodeProcess(pInfo.hProcess, &exitCode);
		}
	else
		v.intValue = 0;
	CloseHandle(pInfo.hProcess);
	return v;
	}

static Var VmSleep(Var stack[])
	{
	Var v;
	int32 millis;

	millis = stack[0].intValue;
	Sleep(millis);
	v.obj = 0;
	return v;
	}

static Var VmGetPlatform(Var stack[])
	{
	Var v;

#ifdef WINCE
	v.obj = createString("WinCE");
#else
	v.obj = createString("Win32");
#endif
	return v;
	}

static Var VmSetDeviceAutoOff(Var stack[])
	{
	Var v;

	v.intValue = 0;
	return v;
	}

static Var VmGetUserName(Var stack[])
	{
	Var v;
	TCHAR name[256];
	DWORD len, w;

	len = 256;
	w = WNetGetUser(NULL, name, &len);
	if (w != NO_ERROR)
		{
		v.obj = 0;
		return v;
		}
#ifdef WINCE
	v.obj = createStringFromUnicode(name, lstrlen(name));
#else
	v.obj = createString(name);
#endif
	return v;
	}
