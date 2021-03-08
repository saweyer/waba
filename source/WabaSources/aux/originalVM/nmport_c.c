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

#ifdef COMMENT
/*

If you're looking here, you've probably already looked at (or worked on) the
nm<platform>_b.c file. If you've gotten the _a and _b files working, this is
the easy part. This file contains all the native functions that are part of
the waba core framework. When you've added all the native functions then you're
done porting the VM.

The native functions that need to be ported are all those listed in the waba.c
native function table. There are stubs included in this file to make things
a bit easier than starting from scratch.

Some rules:

As a rule for native functions, don't hold a pointer across any call
that could garbage collect. For example, this is bad:

ptr = WOBJ_arrayStart(array)
...
string = createString(..)
ptr[0]

since the createString() could GC, the ptr inside of array could be invalid
after the call since a GC would move memory around. Instead, use:

ptr = WOBJ_arrayStart(array)
...
string = createString(..)
...
ptr = WOBJ_arrayStart(array)
ptr[0]

to recompute the pointer after the possible GC. The main thing to know there is
that when you call VM functions, it might garbage collect, so you want to 
recompute any pointers again after that function returns since the memory
locations might have changed. When you don't call VM functions that garbage
collect, there isn't a problem, because the VM is all single threaded. Things
don't move around on you unless you call a function that can garbage collect.

Another thing to note is that if you subclass a class with an object
destroy function, you must explicity call your superclasses objects
destroy functions. This isn't done automatically. See one of the objects that does
that in nmwin32_c.c for reference.

Before jumping into writing native functions, we need to look at 'classHooks'.
A classHooks array contains 'class hooks' like this:

ClassHook classHooks[] =
	{
	{ "waba/fx/Graphics", NULL, 11},
	{ "waba/fx/Image", ImageDestroy, 1 },
	{ "waba/io/Catalog", CatalogDestroy, 7 },
	{ "waba/io/Socket", SocketDestroy, 2 }, 
	{ "waba/io/SerialPort", SerialPortDestroy, 2 }, 
	{ NULL, NULL }
	};

You will need to define a classHooks array. Its an array of triples:

- a class name
- a object destroy function
- a number of native variables

You need a class hook when an object needs to allocate some native system
resource which it needs to use later on and/or needs to be freed.

For example, a Socket object will probably need to keep around a reference
to a socket descriptor around. And when a socket object is garbage
collected, it should close the socket.

So, it needs a classHook. In the above, the destroy method for a socket
is SocketDestroy and it will get called when the object is garbage
collected. The classHook for the Socket class above has 2 native
variables associated with it. Each of these native variables
are 32 bit values. One could hold a socket descriptor and the
other could hold something else. You can see nmpalm_c.c or nmwin32_c.c
for more on how that works for sockets.

So, how do you access a class hook variable or a variable that is in
a waba object? You can access a waba object's variables directly like this:

#define WOBJ_RectX(o) (objectPtr(o))[1].intValue

The objectPtr(o) gets a pointer to the data in an object and the [1]
references the second value in the object. The first value is a pointer
to the class of the object so the WOBJ_RectX(o) above is a macro that
pulls out the first actual variable in the object which is its X value.

If you look in the Rect class, you'll see that it looks like:

class Rect
{
int x;
int y;
..

So, the first variable - (objectPtr(o))[1].intValue is the x value.

The hook variables get stuck in after all the normal variables. If you look in the
nmwin32_c.c or nmpalm_c.c code, you'll see things like this:

//
// Graphics
//
// var[0] = Class
// var[1] = Surface
// var[2] = hook var - 1 for window surface and 2 for image
// var[3] = hook var - rgb
// var[4] = hook var - has clip
// var[5] = hook var - clipX
// ...

and then:

#define WOBJ_GraphicsSurface(o) (objectPtr(o))[1].obj
#define WOBJ_GraphicsSurfType(o) (objectPtr(o))[2].intValue
#define WOBJ_GraphicsRGB(o) (objectPtr(o))[3].intValue
#define WOBJ_GraphicsHasClip(o) (objectPtr(o))[4].intValue
#define WOBJ_GraphicsClipX(o) (objectPtr(o))[5].intValue
#define WOBJ_GraphicsClipY(o) (objectPtr(o))[6].intValue
#define WOBJ_GraphicsClipWidth(o) (objectPtr(o))[7].intValue

See how the hook variables start right after the last variable in the
class. The above are macros that access the value of a waba object in C code.
Making macros like the above makes the code easier to read.

With the macros, we can inspect a graphic's object clipX with:

int32 x = WObjectGraphicClipX(object);

Of course, if you change the Graphics object and add a new variable before the
surface variable or if you add a new variable in a base class, you need to
recompute those mappings or everything will get messed up.

The best way to port the native functions is to start out by making something
that doesn't work but does compile and then take code from the other native
VM's implementations and hack it up one class at a time. It's probably best
to start with the window and drawing classes so you can see something when
you start out.

*/
#endif COMMENT

// WHEN PORTING: You'll probably need classHooks for these things when you get
// things going. Here we've allocate no hook variables for each object but have
// assigned some object destructor functions so when you actually hook some
// data in there, you'll need to set the 0 values to something else

static void GraphicsDestroy(WObject obj);
static void ImageDestroy(WObject obj);
static void CatalogDestroy(WObject obj);
static void SocketDestroy(WObject obj);
static void SerialPortDestroy(WObject obj);

ClassHook classHooks[] =
	{
	{ "waba/fx/Graphics", NULL, 0 },
	{ "waba/fx/Image", ImageDestroy, 0 },
	{ "waba/io/Catalog", CatalogDestroy, 0 },
	{ "waba/io/Socket", SocketDestroy, 0 }, 
	{ "waba/io/SerialPort", SerialPortDestroy,  }, 
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
	Var v;

	v.obj = 0;
	return v;
	}

//
// MainWindow
//

static Var MainWinCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var MainWinExit(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var MainWinSetTimerInterval(Var stack[])
	{
	Var v;

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

//
// FontMetrics
//

static Var FontMetricsCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

#define FM_STRINGWIDTH 1
#define FM_CHARARRAYWIDTH 2
#define FM_CHARWIDTH 3

static Var FontMetricsGetWidth(int type, Var stack[])
	{
	Var v;

	v.obj = 0;
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
// Image
//

static Var ImageFree(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ImageLoad(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ImageCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static void ImageDestroy(WObject image)
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ImageSetPixels(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static void ImageLoadBMP(WObject image, uchar *p)
	{
	Var v;

	v.obj = 0;
	return v;
	}

//
// Graphics
//

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

	v.obj = 0;
	return v;
	}

#define GraphicsFree Return0Func

static Var GraphicsSetFont(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsSetColor(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsSetDrawOp(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsSetClip(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsGetClip(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsClearClip(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsTranslate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var GraphicsDraw(int type, Var stack[])
	{
	Var v;

	v.obj = 0;
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

// WHEN PORTING: Note, this was just another way of stubbing these functions out

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

static Var SocketCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static void SocketDestroy(WObject socket)
	{
	}

static Var SocketClose(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SocketIsOpen(Var stack[])
	{
	Var v;

	v.obj = 0;
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
	Var v;

	v.obj = 0;
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

	v.obj = 0;
	return v;
	}

static Var SoundBeep(Var stack[])
	{
	Var v;

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
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertIntToFloatBitwise(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertStringToInt(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertIntToString(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertFloatToString(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertCharToString(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var ConvertBooleanToString(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

//
// Catalog
//

static Var CatalogCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogIsOpen(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static void CatalogDestroy(WObject cat)
	{
	}

static Var CatalogClose(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogDelete(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogListCatalogs(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogGetRecordSize(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogGetRecordCount(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogDeleteRecord(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogResizeRecord(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogAddRecord(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogSetRecordPos(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogReadWriteBytes(Var stack[], int isRead)
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogRead(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogWrite(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var CatalogSkipBytes(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

//
// Time
//

static Var TimeCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

//
// SerialPort
//

static Var SerialPortCreate(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static void SerialPortDestroy(WObject port)
	{
	}

static Var SerialPortIsOpen(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SerialPortSetReadTimeout(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SerialPortReadCheck(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SerialPortSetFlowControl(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var SerialPortReadWriteBytes(Var stack[], int isRead)
	{
	Var v;

	v.obj = 0;
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
	Var v;

	v.obj = 0;
	return v;
	}

//
// Vm
//

static Var VmIsColor()
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmGetTimeStamp(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmExec(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmSleep(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmGetPlatform(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmSetDeviceAutoOff(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}

static Var VmGetUserName(Var stack[])
	{
	Var v;

	v.obj = 0;
	return v;
	}
