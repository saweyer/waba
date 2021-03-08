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

static int g_mainWinWidth = 0;
static int g_mainWinHeight = 0;
static DWORD g_startTime = 0;
static DWORD g_messageTime = 0;
static HINSTANCE g_hInstance;
static TCHAR *pszWndClassName = TEXT("WabaWndClass");

#define WARP_CORE_PATH "\\Program Files\\waba\\waba.wrp"

#define MAX_CLASSPATHS 20
static int numClassPaths = -1;
static char *classPaths[MAX_CLASSPATHS];

typedef struct
	{
	HANDLE fileH;
	HANDLE mapH;
	int viewIsMapped;
	uchar *ptr;
	} MemFile;

#define MAX_MEM_FILES 16
static MemFile memFiles[MAX_MEM_FILES];
static uint32 numMemFiles = 0;
static int memFileNotSupported = 0;
static int win32WSAStarted = 0;

static HFONT createWin32Font(WObject font);

#ifdef QUICKBIND
static int32 postPaintMethodMapNum = -1;
static int32 postEventMethodMapNum = -1;
static int32 onTimerTickMethodMapNum = -1;
#endif

static int asciiToUnicode(char *src, TCHAR *dst, int max)
	{
	int i;

	for (i = 0; i < max - 1; i++)
		{
		dst[i] = (TCHAR)src[i];
		if (!dst[i])
			return i;
		}
	dst[i] = 0;
	return i;
	}

// calls asciiToUnicode() and strips lead and trailing quotes
static void pathConvert(char *src, TCHAR *dst, int max)
	{
	int start, len;

	start = 0;
	if (src[0] == '"')
		start = 1;
	len = asciiToUnicode(&src[start], dst, max);
	if (len > 0 && dst[len - 1] == '"')
		dst[len - 1] = 0;
	}

#ifdef WINCE
static WObject createStringFromUnicode(TCHAR *s, uint32 len)
	{
	WObject obj, charArrayObj;
	uint16 *charStart;
	uint32 i;

	// create and fill char array
	charArrayObj = createArrayObject(5, len);
	if (!charArrayObj)
		return 0;
	if (pushObject(charArrayObj) == -1)
		return 0;
	charStart = (uint16 *)WOBJ_arrayStart(charArrayObj);
	for (i = 0; i < len; i++)
		charStart[i] =(uint16)s[i];
	// create String object and set char array
	obj = createObject(stringClass);
	if (obj != 0)
		WOBJ_StringCharArrayObj(obj) = charArrayObj;
	popObject(); // charArrayObj
	return obj;
	}
#endif

#ifdef WIN32
#ifndef WINCE
static void dumpStackTrace()
	{
	WClass *wclass;
	WClassMethod *method;
	UtfString className, methodName, methodDesc;
	uint32 i, n, stackPtr;

	if (vmStackPtr == 0)
		return;
	AllocConsole();
#ifdef DUMPERRORTRACE
	{
	char *msg;

	msg = errorMessages[vmStatus.errNum - 1];
	if (msg)
		cprintf("ERROR - %s\n", msg);
	cprintf("ERROR arg1: %s\n", vmStatus.arg1);
	cprintf("ERROR arg2: %s\n", vmStatus.arg2);
	}
#endif
	stackPtr = vmStackPtr;
	while (stackPtr != 0)
		{
		wclass = (WClass *)vmStack[--stackPtr].refValue;
		method = (WClassMethod *)vmStack[--stackPtr].refValue;
		className = getUtfString(wclass, wclass->classNameIndex);
		methodName = getUtfString(wclass, METH_nameIndex(method));
		methodDesc = getUtfString(wclass, METH_descIndex(method));

		for (i = 0; i < className.len; i++)
			cprintf("%c", className.str[i]);
		cprintf(".");
		for (i = 0; i < methodName.len; i++)
			cprintf("%c", methodName.str[i]);
		for (i = 0; i < methodDesc.len; i++)
			cprintf("%c", methodDesc.str[i]);
		cprintf("\n");

		if ((METH_accessFlags(method) & ACCESS_NATIVE) > 0)
			stackPtr -= vmStack[--stackPtr].intValue;
		else
			{
			n = METH_maxLocals(method) + METH_maxStack(method);
			for (i = 0; i < n; i++)
				cprintf("%d:%d ", i, vmStack[--stackPtr].intValue);
			cprintf("\n");
			}
		stackPtr -= 3;
		}
	}
#endif
#endif

static int32 getTimeStamp()
	{
	static uint32 maxStamp = 1 << 30;
	static uint32 maxLow = 0xFFFFFFFF / 10000;
	uint32 millis;
	SYSTEMTIME tm;
	FILETIME ftm;

	GetSystemTime(&tm);
	SystemTimeToFileTime(&tm, &ftm);
	millis = ((ftm.dwHighDateTime % 10000) * maxLow) + (ftm.dwLowDateTime / 10000);
	millis = millis % (uint32)maxStamp;
	return (int32)millis;
	}

static void postErrorDialog(HWND hWnd)
	{
	TCHAR buf[256];
	int len;
	char *msg;

	len = asciiToUnicode("An error was found in the program being\n"
		"run by the WabaVM.\n\n"
		"Error: ", buf, 256);
	msg = errorMessages[vmStatus.errNum - 1];
	if (msg)
		{
		len += asciiToUnicode(msg, &buf[len], 256 - len);
		buf[len++] = ' ';
		}
	msg = vmStatus.arg1;
	if (msg[0])
		{
		len += asciiToUnicode(msg, &buf[len], 256 - len);
		buf[len++] = ' ';
		}
	msg = vmStatus.arg2;
	if (msg[0])
		{
		len += asciiToUnicode(msg, &buf[len], 256 - len);
		buf[len++] = ' ';
		}
	if (vmStatus.className[0] || vmStatus.methodName[0])
		len += asciiToUnicode("in\n", &buf[len], 256 - len);
	msg = vmStatus.className;
	if (msg[0])
		len += asciiToUnicode(msg, &buf[len], 256 - len);
	msg = vmStatus.methodName;
	if (msg[0])
		{
		buf[len++] = '.';
		len += asciiToUnicode(msg, &buf[len], 256 - len);
		}
	buf[len] = 0;
	MessageBox(hWnd, buf, TEXT("Program Error"), MB_ICONEXCLAMATION);
	PostQuitMessage(0);
	}

static long FAR PASCAL MainWndProc(HWND hWnd, UINT msg, WPARAM wParam, LONG lParam)
	{
	WObject winObj;
	WClass *vclass;
	WClassMethod *method;
	uint32 timeStamp;
	Var params[7];

	winObj = (WObject)GetWindowLong(hWnd, GWL_USERDATA);
	if (vmStatus.errNum > 0 || winObj == 0)
		return DefWindowProc(hWnd, msg, wParam, lParam);
	// NOTE: GetMessageTime() is not available under WinCE
	timeStamp = (uint32)(g_messageTime - g_startTime);
	method = NULL;
	switch(msg)
		{
		case WM_PAINT:
			{
			HDC hDC;
			PAINTSTRUCT ps;

			hDC = BeginPaint(hWnd, &ps);
			vclass = WOBJ_class(winObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass, (uint16)postPaintMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_doPaint"),
				createUtfString("(IIII)V"), &vclass);
#endif
			if (method != NULL)
				{
				RECT *rect;

				rect = &ps.rcPaint;
				params[0].obj = winObj;
				// x, y, width, height
				params[1].intValue = ps.rcPaint.left;
				params[2].intValue = ps.rcPaint.top;
				params[3].intValue = ps.rcPaint.right - ps.rcPaint.left;
				params[4].intValue = ps.rcPaint.bottom - ps.rcPaint.top;
				executeMethod(vclass, method, params, 5);
				}
			EndPaint(hWnd, &ps);
			break;
			}
		case WM_LBUTTONDOWN:
		case WM_LBUTTONUP:
		case WM_MOUSEMOVE:
			{
			int32 type, x, y;

			if (msg == WM_LBUTTONDOWN)
				{
				SetCapture(hWnd);
				type = 200; // PenEvent.PEN_DOWN
				}
			else if (msg == WM_LBUTTONUP)
				{
				ReleaseCapture();
				type = 202; // PenEvent.PEN_UP
				}
			else
				type = 201; // PenEvent.PEN_MOVE
			x = (int32)((int16)LOWORD(lParam));
			y = (int32)((int16)HIWORD(lParam));
			vclass = WOBJ_class(winObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass, (uint16)postEventMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_postEvent"),
				createUtfString("(IIIIII)V"), &vclass);
#endif
			if (method != NULL)
				{
				params[0].obj = winObj;
				params[1].intValue = type; // type
				params[2].intValue = 0; // key
				params[3].intValue = x; // x
				params[4].intValue = y; // y
				params[5].intValue = 0; // modifiers
				params[6].intValue = timeStamp; // timeStamp
				executeMethod(vclass, method, params, 7);
				}
			break;
			}
		case WM_CHAR:
		case WM_KEYDOWN:
			{
			int32 type, key, mod;

			type = 100; // KeyEvent.KEY_PRESS
			key = 0;
			if (msg == WM_CHAR)
				{
				key = wParam;
				switch (key)
					{
					case 8:  key = 0; break; // BACKSPACE
					case 9:  key = 0; break; // TAB
					case 13: key = 0; break; // RETURN
					case 27: key = 0; break; // ESCAPE
					case 127:key = 0; break; // CONTROL-BACKSPACE
					}
				}
			else
				{
				switch (wParam)
					{
					case VK_PRIOR: key = 75000; break; // IKeys.PAGE_UP
					case VK_NEXT:  key = 75001; break; // IKeys.PAGE_DOWN
					case VK_HOME:  key = 75002; break; // IKeys.HOME
					case VK_END:   key = 75003; break; // IKeys.END
					case VK_UP:    key = 75004; break; // IKeys.UP
					case VK_DOWN:  key = 75005; break; // IKeys.DOWN
					case VK_LEFT:  key = 75006; break; // IKeys.LEFT
					case VK_RIGHT: key = 75007; break; // IKeys.RIGHT
					case VK_INSERT:key = 75008; break; // IKeys.INSERT
					case VK_RETURN:key = 75009; break; // IKeys.ENTER
					case VK_TAB:   key = 75010; break; // IKeys.TAB
					case VK_BACK:  key = 75011; break; // IKeys.BACKSPACE
					case VK_ESCAPE:key = 75012; break; // IKeys.ESCAPE
					case VK_DELETE:key = 75013; break; // IKeys.DELETE
					}
				}
			if (!key)
				break;
			mod = 0;
			if (GetKeyState(VK_CONTROL) & 0x80)
				mod |= (1 << 1); // IKeys.CONTROL
			if (GetKeyState(VK_SHIFT) & 0x80)
				mod |= (1 << 2); // IKeys.SHIFT
			vclass = WOBJ_class(winObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass, (uint16)postEventMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_postEvent"),
				createUtfString("(IIIIII)V"), &vclass);
#endif
			if (method != NULL)
				{
				params[0].obj = winObj;
				params[1].intValue = type; // type
				params[2].intValue = key; // key
				params[3].intValue = 0; // x
				params[4].intValue = 0; // y
				params[5].intValue = mod; // modifiers
				params[6].intValue = timeStamp; // timeStamp
				executeMethod(vclass, method, params, 7);
				}
			break;
			}
		case WM_TIMER:
			// NOTE: This routine will only get called for main windows since
			// only those call SetTimer()
			vclass = WOBJ_class(winObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass, (uint16)onTimerTickMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_onTimerTick"),
				createUtfString("()V"), &vclass);
#endif
			if (method != NULL)
				{
				params[0].obj = winObj;
				executeMethod(vclass, method, params, 1);
				}
			break;
		case WM_CLOSE:
			PostQuitMessage(0);
			return 0;
		default:
			return DefWindowProc(hWnd, msg, wParam, lParam);
		}
	if (vmStatus.errNum > 0)
		postErrorDialog(hWnd);
	return 0L;
	}

static uchar *readFileIntoMemory(char *path, int nullTerminate, uint32 *size)
	{
	HANDLE findH, fileH;
	WIN32_FIND_DATA findData;
	DWORD lenRead, len;
	uchar *p;
	TCHAR uniPath[128];

	if (nullTerminate != 0)
		nullTerminate = 1;
	pathConvert(path, uniPath, 128);
	fileH = CreateFile(uniPath, GENERIC_READ, FILE_SHARE_READ, NULL,
		OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if (fileH == INVALID_HANDLE_VALUE)
		return NULL;
	findH = FindFirstFile(uniPath, &findData);
	if (findH == INVALID_HANDLE_VALUE)
		{
		CloseHandle(fileH);
		return NULL;
		}
	len = findData.nFileSizeLow;
	if (size)
		*size = len + nullTerminate;
	p = (uchar *)xmalloc(len + nullTerminate);
	if (p != NULL)
		{
		ReadFile(fileH, p, len, &lenRead, NULL);
		if (len != lenRead)
			{
			xfree(p);
			p = NULL;
			}
		}
	else
		VmQuickError(ERR_CantAllocateMemory);
	if (p && nullTerminate)
		p[len] = 0;
	FindClose(findH);
	CloseHandle(fileH);
	return p;
	}

static void freeMemMapFile(MemFile *memFile)
	{
	if (memFile->viewIsMapped && memFile->ptr != NULL)
		UnmapViewOfFile(memFile->ptr);
	if (memFile->mapH != NULL)
		CloseHandle(memFile->mapH);
	if (memFile->fileH != INVALID_HANDLE_VALUE)
		CloseHandle(memFile->fileH);
	}

static uchar *memMapFile(char *path)
	{
	MemFile memFile;
	int mapped;
	TCHAR uniPath[128];

	if (numMemFiles == MAX_MEM_FILES)
		{
		MessageBox(NULL, TEXT("Too many warp files"), TEXT("Error"), MB_ICONEXCLAMATION);
		return NULL;
		}
	memFile.fileH = INVALID_HANDLE_VALUE;
	memFile.mapH = INVALID_HANDLE_VALUE;
	memFile.ptr = 0;
	mapped = 0;

	pathConvert(path, uniPath, 128);
	memFile.fileH = CreateFile(uniPath, GENERIC_READ, FILE_SHARE_READ, NULL,
		OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if (memFile.fileH != INVALID_HANDLE_VALUE)
		memFile.mapH = CreateFileMapping(memFile.fileH, NULL, PAGE_READONLY,
			0, 0, NULL);
	if (memFile.fileH != INVALID_HANDLE_VALUE && memFile.mapH == NULL)
		{
		// NOTE: Here we read the file into memory instead of memory
		// mapping it. This is to work around WindowsCE devices that do not
		// support "Page-In" - those devices can't memory map files that are
		// not created with CreateFileForMapping(). Its possible we could fix
		// this by using the CE routines to create a memory mapping file. We
		// could create a file for memory mapping and then copy the one on
		// the system over to it but that sounded dicey so we don't do it.
		memFileNotSupported = 1;
		memFile.viewIsMapped = 0;
		memFile.ptr = readFileIntoMemory(path, 0, NULL);
		}
	else
		{
		memFile.viewIsMapped = 1;
		if (memFile.mapH != NULL)
			memFile.ptr = MapViewOfFile(memFile.mapH, FILE_MAP_READ, 0, 0, 0);
		}
	if (memFile.ptr != NULL)
		if (!strncmp(memFile.ptr, "Wrp1", 4))
			mapped = 1;
	if (!mapped)
		{
		MessageBox(NULL, uniPath, TEXT("File Not Found"), MB_ICONEXCLAMATION);
		freeMemMapFile(&memFile);
		return NULL;
		}
	memFiles[numMemFiles++] = memFile;
	return memFile.ptr;
	}

static WObject startApp(char *cmdLine, BOOL *alreadyRunning)
	{
	int vmStackSize, nmStackSize, classHeapSize, objectHeapSize;
	char *className;
	int i;

	vmStackSize = 1500;
	nmStackSize = 300;
	classHeapSize = 14000;
	objectHeapSize = 8000;
	className = 0;
	*alreadyRunning = 0;

	// parse options
	i = 0;
	while (cmdLine[i] == '/')
		{
		char c;
		int32 value;

		c = cmdLine[++i];
		if (!c)
			break;
		if (cmdLine[++i] != ' ')
			break;
		i++;
		// parse integer - CE doesn't always have atoi()
		value = 0;
		while (cmdLine[i] >= '0' && cmdLine[i] <= '9')
			{
			value = (value * 10) + (cmdLine[i] - '0');
			i++;
			}
		if (c == 'l')
			classHeapSize = value;
		else if (c == 'm')
			objectHeapSize = value;
		else if (c == 's')
			vmStackSize = value;
		else if (c == 't')
			nmStackSize = value;
		else if (c == 'w')
			g_mainWinWidth = value;
		else if (c == 'h')
			g_mainWinHeight = value;
		while (cmdLine[i] == ' ')
			i++;
		}

	// parse class name
	className = &cmdLine[i];
	while (cmdLine[i] && cmdLine[i] != ' ')
		i++;
	if (cmdLine[i] == ' ')
		cmdLine[i++] = 0;

	// run "welcome" app (no app class specified)
	if (className[0] == 0)
		className = "waba/ui/Welcome";

#ifdef WINCE
	// memory map warp files
	memMapFile(WARP_CORE_PATH);
	while (cmdLine[i])
		{
		char *path, delim;

		while (cmdLine[i] == ' ')
			i++;
		delim = ' ';
		if (cmdLine[i] == '"')
			{
			delim = '"';
			i++;
			}
		path = &cmdLine[i];
		while (cmdLine[i] && cmdLine[i] != delim)
			i++;
		if (cmdLine[i] == delim)
			cmdLine[i++] = 0;
		memMapFile(path);
		}
#endif

#ifdef DEBUGCMDLINE
	AllocConsole();
	cprintf("mainWinWidth %d\n", g_mainWinWidth);
	cprintf("mainWinHeight %d\n", g_mainWinHeight);
	cprintf("vmStackSize %d\n", vmStackSize);
	cprintf("nmStackSize %d\n", nmStackSize);
	cprintf("classHeapSize %d\n", classHeapSize);
	cprintf("objectHeapSize %d\n", objectHeapSize);
	cprintf("className #%s#\n", className);
#endif
 
#ifdef WINCE
	// only allow one copy of the program to run at a time
		{
		TCHAR title[40];
		int n;
		HWND appWin;

		n = xstrlen(className) + 1;
		if (n > 40)
			n = 40;
		asciiToUnicode(className, title, n);
		appWin = FindWindow(pszWndClassName, title);
		if (appWin != NULL)
			{
			ShowWindow(appWin, SW_SHOWNORMAL);
			SetActiveWindow(appWin);
			BringWindowToTop(appWin);
			*alreadyRunning = 1;
			return 0;
			}
		}
#endif

	VmInit(vmStackSize, nmStackSize, classHeapSize, objectHeapSize);
	return VmStartApp(className);
	}

static void stopApp(WObject mainWinObj)
	{
	uint32 i;

	VmStopApp(mainWinObj);
	VmFree();

	// free memory mapped files
	for (i = 0; i < numMemFiles; i++)
		freeMemMapFile(&memFiles[i]);
	}

static void usage()
	{
#ifdef PRINTOPTIONS
	AllocConsole();

	cprintf("Waba (TM) Virtual Machine Version 1.0 Beta for 80x86\n");
	cprintf("Copyright (C) Wabasoft 1998. All rights reserved.\n");
	cprintf("\n");
	cprintf("Usage: waba [options] appclass [warpfile]\n");
	cprintf("\n");
	cprintf("Options:\n");
	cprintf("  /l   Assign size of class heap (e.g. /l 10000)\n");
	cprintf("  /m   Assign size of object heap (e.g. /m 20000)\n");
	cprintf("  /s   Assign size of stack (e.g. /s 2000)\n");
	cprintf("  /t   Assign size of native stack (e.g. /t 50)\n");
	cprintf("\n");
	cprintf("Example:\n");
	cprintf("  waba /m 20000 MyApp myapp.wrp\n");
	cprintf("\n");
#endif
	}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPTSTR lpCmdLine,
	int nCmdShow)
	{
	WObject mainWinObj;
	MSG msg;
	char *cmdLine;
	int cmdLen;
	BOOL alreadyRunning;

	g_hInstance = hInstance;
	if (!hPrevInstance)
		{
		WNDCLASS wc;

		xmemzero(&wc, sizeof(wc));
		wc.hInstance = g_hInstance;
		wc.lpfnWndProc = MainWndProc;
#ifdef WINCE
		wc.hCursor = NULL;
#else
		wc.hCursor = LoadCursor(NULL, IDC_ARROW);
#endif
		wc.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);
		wc.lpszClassName = pszWndClassName;
		if (!RegisterClass(&wc))
			return FALSE;
		}

	// NOTE: We need to make a copy of the command line since we modify it
	// when parsing the command line in startApp()
		{
		int i;

		if (lpCmdLine != NULL)
#ifdef WINCE
			cmdLen = lstrlen(lpCmdLine);
#else
			cmdLen = xstrlen(lpCmdLine);
#endif
		else
			cmdLen = 0;
		cmdLine = (char *)xmalloc(cmdLen + 1);
		if (!cmdLine)
			return -1;
		for (i = 0; i < cmdLen; i++)
			cmdLine[i] = (char)lpCmdLine[i];
		cmdLine[i] = 0;
		}

	mainWinObj = startApp(cmdLine, &alreadyRunning);
#ifdef NEVER
	if (memFileNotSupported && !cmdLine[0])
		MessageBox(NULL, TEXT("This Windows device does not fully support\n"
			"memory mapping. This means Waba programs will\n"
			"use more memory than they normally should."),
			TEXT("Device Not Optimal"), MB_ICONEXCLAMATION);
#endif
	xfree(cmdLine);
	usage();
#ifdef QUICKBIND
	if (mainWinObj != 0)
		{
		WClass *vclass;

		// cache method map numbers for commonly called methods
		vclass = WOBJ_class(mainWinObj);
		postPaintMethodMapNum = getMethodMapNum(vclass, createUtfString("_doPaint"),
			createUtfString("(IIII)V"), SEARCH_ALL);
		postEventMethodMapNum = getMethodMapNum(vclass, createUtfString("_postEvent"),
			createUtfString("(IIIIII)V"), SEARCH_ALL);
		onTimerTickMethodMapNum = getMethodMapNum(vclass, createUtfString("_onTimerTick"),
			createUtfString("()V"), SEARCH_ALL);
		if (postPaintMethodMapNum == -1 || postEventMethodMapNum == -1 ||
			onTimerTickMethodMapNum == -1)
			mainWinObj = 0;
		}
#endif
	if ((mainWinObj == 0 || vmStatus.errNum > 0) && !alreadyRunning)
		postErrorDialog(NULL);
	while (mainWinObj && GetMessage(&msg, NULL, 0, 0))
		{
		if (g_startTime == 0)
			g_startTime = msg.time;
		g_messageTime = msg.time;
		TranslateMessage(&msg);
		DispatchMessage(&msg);
		}
	stopApp(mainWinObj);
	if (win32WSAStarted)
		WSACleanup();
	return 0;
	}

static uchar *loadFromMem(char *path, uint32 pathLen, uint32 *size)
	{
	uchar *baseP, *offP, *p;
	uint32 i, off, nextOff, top, bot, mid;
	uint32 nameLen, minLen, numRecs;
	int cmp;

	// look in memory mapped files
	for (i = 0; i < numMemFiles; i++)
		{
		baseP = memFiles[i].ptr;
		numRecs = getUInt32(baseP + 4);
		if (numRecs == 0)
			continue;
		// NOTE: We do a binary search to find the class. So, a search
		// for N classes occurs in O(nlogn) time.
		top = 0;
		bot = numRecs;
		while (1)
			{
			mid = (bot + top) / 2;
			offP = baseP + 8 + (mid * 4);
			off = getUInt32(offP);
			p = baseP + off;
			nameLen = getUInt16(p);
			p += 2;
			if (pathLen > nameLen)
				minLen = nameLen;
			else
				minLen = pathLen;
			cmp = xstrncmp(path, p, minLen);
			if (!cmp)
				{
				if (pathLen == nameLen)
					{
					if (size != NULL)
						{
						nextOff = getUInt32(offP + 4);
						*size = nextOff - off - nameLen - 2;
						}
					return p + nameLen;
					}
				if (pathLen > nameLen)
					cmp = 1;
				else
					cmp = -1;
				}
			if (mid == top)
				break; // not found
			if (cmp < 0)
				bot = mid;
			else
				top = mid;
			}
		}
	return NULL;
	}

static uchar *nativeLoadClass(UtfString className, uint32 *size)
	{
	uchar *p;
	uint16 len, i;
	char path[128];

	// try loading from memory mapped files first
	// make full path by appending .class
	len = className.len + 6;
	if (len > 128)
		return NULL;
	xstrncpy(path, className.str, className.len);
	xstrncpy(&path[className.len], ".class", 6);
	p = loadFromMem(path, len, size);
	if (p != NULL)
		return p;

	// not found in memory mapped files, try loading it from the CLASSPATH
	if (numClassPaths == -1)
		{
#ifndef WINCE
		char *s, *sp;
#endif

		classPaths[0] = ".";
		numClassPaths = 1;
#ifndef WINCE
		s = getenv("CLASSPATH");
		if (s != NULL)
			{
			// NOTE: we duplicate the CLASSPATH here since strtok() modifies
			// it but we never explicitly free it, we let the OS free it when
			// the program exits. Also note we don't need to deal with UNICODE
			// since this section does not applicable to WinCE
			i = xstrlen(s);
			sp = xmalloc(i + 1);
			xstrncpy(sp, s, i);
			sp[i] = 0;
			s = sp;

			// parse through the elements of CLASSPATH		
			sp = strtok(s, ";");
			while (sp != NULL)
				{
				classPaths[numClassPaths++] = sp;
				if (numClassPaths == MAX_CLASSPATHS)
					break;
				sp = strtok(NULL, ";");
				}
			}
#endif
		}
	// NOTE: we never free the memory pointers we allocate here. We let the
	// OS clean up memory when the process exits. This works well but if we
	// ever do free these pointers, we need to make sure we differentiate
	// them from the memory mapped file pointers (nativeLoadClassFromMem())
	for (i = 0; i < numClassPaths; i++)
		{
		len = xstrlen(classPaths[i]);
		xstrncpy(path, classPaths[i], len);
		if (path[len] != '\\')
			xstrncpy(&path[len++], "\\", 1);
		xstrncpy(&path[len], className.str, className.len);
		len += className.len;
		xstrncpy(&path[len], ".class", 6);
		len += 6;
		path[len] = 0;
		p = readFileIntoMemory(path, 0, size);
		if (p != NULL)
			break;
		}
	if (p == NULL)
		return NULL;
	// validate
	if (getUInt32(p) != (uint32)0xCAFEBABE)
		{
		VmError(ERR_BadClass, NULL, &className, NULL);
		xfree(p);
		return NULL;
		}
	return p;
	}

