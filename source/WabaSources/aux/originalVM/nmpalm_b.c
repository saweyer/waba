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

// globals

#define NET_NOT_READY_FOR_OPEN 0
#define NET_READY_FOR_OPEN 1
#define NET_IS_OPEN 2
#define NET_OPEN_FAILED 3

static WObject globalMainWin = 0;
static int32 globalTimerInterval = 0;
static int32 globalTimerStart = 0;
static int classDbCount = 0;
static DmOpenRef *classDbList = 0;
static ULong appCreatorId;
static Word globalSocketLibRefNum;
static int globalNetState = NET_NOT_READY_FOR_OPEN;
#ifdef QUICKBIND
static int32 postEventMethodMapNum = -1;
static int32 onTimerTickMethodMapNum = -1;
#endif

static Long millisToTicks(int32 millis)
	{
	int32 tps;
	
	if (millis <= 0)
		return 0;
	millis = millis % (int32)((1L << 30) / 1000L);
	tps = (int32)SysTicksPerSecond();
	return (millis * tps) / 1000L;
	}

static int32 getTimeStamp()
	{
	static uint32 tps;
	uint32 stamp;

	if (tps == 0)
		tps = (uint32)SysTicksPerSecond();
	// NOTE: Timestamp must be in range from 0..(1 << 30)
	stamp = (uint32)((TimGetTicks() * 1000L) / tps);
	stamp = stamp % (uint32)(1L << 30);
	return (int32)stamp;
	}

static void postStopEvent()
	{
	EventType event;

	// NOTE: Here we launch the "launcher". If we simply
	// queued an appStopEvent to stop, it would return
	// to the calling application which was the stub that
	// executed the vm, causing it to rerun the same program
	event.eType = keyDownEvent; 
	event.data.keyDown.chr = launchChr;
	event.data.keyDown.modifiers = commandKeyMask;
	EvtAddEventToQueue(&event);
	}

static void drawErrorWin()
	{
	int i, y;
	RectangleType rect;
	FontID savedFont;
	char *msg;

	WinEraseWindow();

	// draw error message
	y = 25;
	WinDrawChars("An error was found in the program", 33, 5, y);
	y += 12;
	WinDrawChars("being run by the WabaVM.", 24, 5, y);
	y += 16;
	WinDrawChars("Error:", 6, 5, y);
	msg = errorMessages[vmStatus.errNum - 1];
	WinDrawChars(msg, xstrlen(msg), 31, y);
	y += 12;
	msg = vmStatus.arg1;
	if (msg[0])
		{
		WinDrawChars(msg, xstrlen(msg), 31, y);
		y += 12;
		}
	msg = vmStatus.arg2;
	if (msg[0])
		{
		WinDrawChars(msg, xstrlen(msg), 31, y);
		y += 12;
		}
	y += 4;
	if (vmStatus.className[0] || vmStatus.methodName[0])
		{
		WinDrawChars("In:", 3, 5, y);
		msg = vmStatus.className;
		WinDrawChars(msg, xstrlen(msg), 31, y);
		y += 12;
		msg = vmStatus.methodName;
		WinDrawChars(msg, xstrlen(msg), 31, y);
		y += 12;
		}
	y += 4;
	if (vmStatus.errNum != ERR_CantAccessCoreClasses)
		WinDrawChars("Please notify the program's author.", 35, 5, y);
	else
		WinDrawChars("The WabaVM is not fully installed.", 34, 5, y);
	
	// draw line across top
	rect.topLeft.x = 0;
	rect.topLeft.y = 12;
	rect.extent.x = 160;
	rect.extent.y = 2;
	WinDrawRectangle(&rect, 0);

	// draw rounded rect at top for title
	rect.topLeft.x = 0;
	rect.topLeft.y = 0;
	rect.extent.x = 92;
	rect.extent.y = 13;
	WinDrawRectangle(&rect, 3);

	// draw title
	savedFont = FntSetFont(boldFont);
	WinEraseChars("WabaVM Notice", 13, 5, 2);
	FntSetFont(savedFont);

	if (vmStatus.errNum != ERR_IncompatibleDevice)
		{
		// draw Stop Program button
		rect.topLeft.x = 2;
		rect.topLeft.y = 148;
		rect.extent.x = 72;
		rect.extent.y = 11;
		WinDrawRectangleFrame(roundFrame, &rect);
		WinDrawChars("Stop Program", 12, 10, 148);
		}
	}

static void drawMainWin()
	{
	WObject mainWinObj;
	WClass *vclass;
	WClassMethod *method;
	Var params[7];

	mainWinObj = globalMainWin;
	if (!mainWinObj)
		return;
	WinEraseWindow();
	vclass = WOBJ_class(mainWinObj); // get runtime class
	method = getMethod(vclass, createUtfString("_doPaint"),
		createUtfString("(IIII)V"), &vclass);
	if (method == NULL)
		return;
	params[0].obj = mainWinObj;
	params[1].intValue = 0; // x
	params[2].intValue = 0; // y
	params[3].intValue = 160; // width
	params[4].intValue = 160; // height
	executeMethod(vclass, method, params, 5);
	}

static void timerCheck()
	{
	WObject mainWinObj;
	int32 now, diff;
	WClass *vclass;
	WClassMethod *method;
	Var params[1];

	mainWinObj = globalMainWin;
	if (!mainWinObj)
		return;
	now = getTimeStamp();
	diff = now - globalTimerStart;
	if (diff < 0)
		diff += (1L << 30); // max stamp is (1 << 30)
	if (diff < globalTimerInterval)
		return;
	vclass = WOBJ_class(mainWinObj); // get runtime class
#ifdef QUICKBIND
	method = getMethodByMapNum(vclass, &vclass,
		(uint16)onTimerTickMethodMapNum);
#else
	method = getMethod(vclass, createUtfString("_onTimerTick"),
		createUtfString("()V"), &vclass);
#endif
	if (method != NULL)
		{
		params[0].obj = mainWinObj;
		executeMethod(vclass, method, params, 1);
		}
	globalTimerStart = now;
	}

static void handleErrorWinEvent(EventPtr eventP)
	{
	int32 x, y;

	// error window is showing, button click exits
	if (eventP->eType == penDownEvent)
		{
		x = eventP->screenX;
		y = eventP->screenY;
		if (x >= 2 && x < 74 && y >= 148 && y < 159)
			postStopEvent();
		}
	}

static void handleMainWinEvent(EventPtr eventP)
	{
	WObject mainWinObj;
	WClass *vclass;
	WClassMethod *method;
	Var params[7];

	mainWinObj = globalMainWin;
	if (!mainWinObj)
		return;
	switch (eventP->eType) 
		{
		case penUpEvent:
		case penDownEvent:
		case penMoveEvent:
			{
			int32 type, x, y;

			if (eventP->eType == penDownEvent)
				type = 200; // PenEvent.PEN_DOWN
			else if (eventP->eType == penUpEvent)
				type = 202; // PenEvent.PEN_UP
			else
				type = 201; // PenEvent.PEN_MOVE
			x = eventP->screenX;
			y = eventP->screenY;
			vclass = WOBJ_class(mainWinObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass,
				(uint16)postEventMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_postEvent"),
				createUtfString("(IIIIII)V"), &vclass);
#endif
			if (method != NULL)
				{
				params[0].obj = mainWinObj;
				params[1].intValue = type; // type
				params[2].intValue = 0; // key
				params[3].intValue = x; // x
				params[4].intValue = y; // y
				params[5].intValue = 0; // modifiers
				params[6].intValue = 0; // timeStamp
				executeMethod(vclass, method, params, 7);
				}
			break;
			}
		case keyDownEvent:
			{
			int32 type, key;
			Word chr;
			type = 100; // KeyEvent.KEY_PRESS
			chr = eventP->data.keyDown.chr;
			key = 0;
			switch (chr)
				{
				// NOTE: these should go somewhere:
				//		nextFieldChr
				//		prevFieldChr
				//		linefeedChr
				case pageUpChr:    key = 75000; break; // PAGE_UP
				case pageDownChr:  key = 75001; break; // PAGE_DOWN
//				case :             key = 75002; break; // HOME
//				case :             key = 75003; break; // END
				case upArrowChr:   key = 75004; break; // UP
				case downArrowChr: key = 75005; break; // DOWN
				case leftArrowChr: key = 75006; break; // LEFT
				case rightArrowChr:key = 75007; break; // RIGHT
//				case :             key = 75008; break; // INSERT
				case returnChr:    key = 75009; break; // ENTER
				case tabChr:       key = 75010; break; // TAB
				case backspaceChr: key = 75011; break; // BACKSPACE
				case escapeChr:    key = 75012; break; // ESCAPE
//				case :             key = 75013; break; // DELETE
				case menuChr:      key = 75014; break; // MENU
				case commandChr:   key = 75015; break; // COMMAND
				}
			if (!key)
				{
				if (chr > 255)
					break;
				key = chr;
				}
			vclass = WOBJ_class(mainWinObj); // get runtime class
#ifdef QUICKBIND
			method = getMethodByMapNum(vclass, &vclass,
				(uint16)postEventMethodMapNum);
#else
			method = getMethod(vclass, createUtfString("_postEvent"),
				createUtfString("(IIIIII)V"), &vclass);
#endif
			if (method != NULL)
				{
				params[0].obj = mainWinObj;
				params[1].intValue = type; // type
				params[2].intValue = key; // key
				params[3].intValue = 0; // x
				params[4].intValue = 0; // y
				params[5].intValue = 0; // modifiers
				params[6].intValue = 0; // timeStamp
				executeMethod(vclass, method, params, 7);
				}
			break;
			}
		default:
			break;
		}
	}

static Long calcEventTimeout()
		{
		Long timeout;

		if (globalTimerInterval <= 0)
			timeout = -1;
		else
			{
			int32 now, diff;

			// NOTE: calculate the ticks until the next interval
			now = getTimeStamp();
			diff = now - globalTimerStart;
			if (diff < 0)
				diff += (1L << 30); // max stamp is (1 << 30)
			timeout = globalTimerInterval - diff;
			if (timeout <= 0)
				timeout = 0;
			timeout = millisToTicks(timeout);
			if (timeout <= 0)
				timeout = 1;
			}
		return timeout;
		}

static int isostrncmp(uchar *s1, uchar *s2, uint16 n)
	{
	uint16 i;

	for (i = 0; i < n; i++)
		{
		if (s1[i] == s2[i])
			;
		else if (s1[i] < s2[i])
			return -1;
		else
			return 1;
		}
	return 0;
	}

static uchar *lockWarpRec(char *path, uint16 pathLen, uint32 *size)
	{
	uchar *p;
	VoidHand recHandle;
	VoidPtr recPtr;
	uint32 top, bot, mid, numRecs;
	uint16 i, nameLen, minLen;
	int cmp;

	for (i = 0; i < classDbCount; i++)
		{
		// binary search to find class
		numRecs = DmNumRecords(classDbList[i]);
		if (numRecs == 0)
			continue;
		top = 0;
		bot = numRecs;
		while (1)
			{
			mid = (bot + top) / 2;
			recHandle = DmQueryRecord(classDbList[i], mid);
			recPtr = MemHandleLock(recHandle);
			p = (uchar *)recPtr;
			nameLen = getUInt16(p);
			p += 2;
			if (pathLen > nameLen)
				minLen = nameLen;
			else
				minLen = pathLen;
			cmp = isostrncmp((uchar *)path, p, minLen);
			if (!cmp)
				{
				if (pathLen == nameLen)
					{
					if (size != NULL)
						*size = MemHandleSize(recHandle) - nameLen - 2;
					p += pathLen;
					return p;
					}
				if (pathLen > nameLen)
					cmp = 1;
				else
					cmp = -1;
				}
			MemHandleUnlock(recHandle);
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

static void unlockWarpRec(uchar *ptr, uint16 pathLen)
	{
	// NOTE: The pointer given is "inside" the record. To unlock the record
	// we need to find the original pointer locked which is at the front
	// of the path.
	MemPtrUnlock(ptr - pathLen - 2);
	}

static uchar *nativeLoadClass(UtfString className, uint32 *size)
	{
	uchar *p;
	char path[128];
	uint16 pathLen;

	// make full path by appending ".class"
	pathLen = className.len + 6;
	if (pathLen > 128)
		return NULL;
	xstrncpy(path, className.str, className.len);
	xstrncpy(&path[className.len], ".class", 6);
	// NOTE: We unlock the record locked here in stopApp()
	p = lockWarpRec(path, pathLen, size);
	if (p == NULL)
		return NULL;
	if (getUInt32(p) != (uint32)0xCAFEBABE)
		{
		VmError(ERR_BadClass, NULL, &className, NULL);
		unlockWarpRec(p, pathLen);
		return NULL;
		}
	return p;
	}

#ifdef SECURE_CLASS_HEAP
static DmOpenRef classHeapDmRef = 0;
static VoidHand classHeapRecH = 0;
static VoidPtr classHeapRecP = 0;

static void delClassHeapDb()
	{
	UInt cardNo;
	LocalID dbID;
	DmSearchStateType searchState;

	if (DmGetNextDatabaseByTypeCreator(true, &searchState, 
		'Chep', 'WABA', true, &cardNo, &dbID) == 0)
		DmDeleteDatabase(cardNo, dbID);
	}
#endif

static WObject startApp(int runApp)
	{
	char cmdLine[80], className[40];
	ULong vmStackSize, nmStackSize, classHeapSize, objectHeapSize;
	int i, j, n;

	// defaults
	vmStackSize = 1500;
	nmStackSize = 300;
	classHeapSize = 14000;
	objectHeapSize = 8000;

	// NOTE: We need VoidHand to fall on a byte boundry for our xmalloc()
	// routine to work correctly (see xmalloc())
	if (sizeof(VoidHand) % 4 != 0)
		{
		VmQuickError(ERR_SanityCheckFailed);
		return 0;
		}

	if (!runApp)
		{
		// run "welcome" app (no app class specified)
		xstrncpy(cmdLine, "waba/ui/Welcome", 18);
		cmdLine[18] = 0;
		}
	else
		{
		DmOpenRef launchRef;
		VoidHand recH;
		VoidPtr recP;

		// NOTE: we get the command line from a storage database. See
		// the launcher program for how and why this is done

		// read command line from db
		launchRef = DmOpenDatabaseByTypeCreator('Laun', 'WABA',
			dmModeReadWrite);
		if (!launchRef)
			return 0;
		recH = DmGetRecord(launchRef, 0);
		if (!recH)
			return 0;
		recP = MemHandleLock(recH);
		if (!recP)
			return 0;
		for (i = 0; i < 80; i++)
			cmdLine[i] = ((char *)recP)[i];
		MemHandleUnlock(recH);
		DmReleaseRecord(launchRef, 0, false);
		DmCloseDatabase(launchRef);
		// NOTE: we could delete the "Laun" database at this point
		}

	// parse options
	i = 0;
	while (cmdLine[i] == '/')
		{
		char c;
		ULong value;

		c = cmdLine[++i];
		if (!c)
			break;
		if (cmdLine[++i] != ' ')
			break;
		i++;
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
		while (cmdLine[i] == ' ')
			i++;
		}

	// parse class name
	j = 0;
	while (cmdLine[i] && cmdLine[i] != ' ')
		className[j++] = cmdLine[i++];
	className[j] = 0;

	// count warp creator ids
	j = i;
	n = 1;
	while (cmdLine[j] == ' ')
		{
		n++;
		j += 5;
		}

	// parse warp creator ids
	classDbList = xmalloc(sizeof(DmOpenRef) * n);
	if (classDbList == 0)
		{
		VmQuickError(ERR_CantAllocateMemory);
		return 0;
		}
	classDbList[0] = DmOpenDatabaseByTypeCreator('Wrp1', 'WABA',  dmModeReadOnly);
	if (classDbList[0] == 0)
		{
		VmQuickError(ERR_CantAccessCoreClasses);
		return 0;
		}
	classDbCount = 1;
	while (cmdLine[i] == ' ')
		{
		DmOpenRef dmRef;
		ULong creatorId;

		i++; // skip space
		creatorId = getUInt32((uchar *)&cmdLine[i]);
		// first creator id is program's creator id
		if (classDbCount == 1)
			appCreatorId = creatorId;
		dmRef = DmOpenDatabaseByTypeCreator('Wrp1', creatorId,
			dmModeReadOnly);
		if (dmRef == 0)
			{
			VmQuickError(ERR_CantAccessAppClasses);
			return 0;
			}
		classDbList[classDbCount++] = dmRef;
		i += 4;
		}

#ifdef SECURE_CLASS_HEAP
	delClassHeapDb();
	// create a db (ClsHeap) for the class heap
	if (DmCreateDatabase(0, "ClsHeap", 'WABA', 'Chep', false) == 0)
		{
		classHeapDmRef = DmOpenDatabaseByTypeCreator('Chep', 'WABA',
			dmModeReadWrite);
		if (classHeapDmRef != 0)
			{
			UInt i;

			i = 0;
			classHeapRecH = DmNewRecord(classHeapDmRef, &i, classHeapSize);
			if (classHeapRecH != 0)
				classHeapRecP = MemHandleLock(classHeapRecH);
			}
		}
	if (classHeapRecP == 0)
		{
		VmQuickError(ERR_CantAllocateMemory);
		return 0;
		}
	classHeap = classHeapRecP;
#endif
	VmInit(vmStackSize, nmStackSize, classHeapSize, objectHeapSize);
	return VmStartApp(className);
	}

static void stopApp(WObject mainWinObj)
	{
	WClass *wclass, *nextClass;
	uint32 i;

	VmStopApp(mainWinObj);
	if (vmInitialized) // set by VmInit()
		{
		// unlock all record pointers
		for (i = 0; i < CLASS_HASH_SIZE; i++)
			{
			wclass = classHashList[i];
			while (wclass != NULL)
				{
				UtfString className;

				// NOTE: The pointer to the record is before the byteRep
				// pointer because the class' absolute location comes first
				// in the record. The first bytes in the record are the
				// class name length (2) plus the class name plus the .class
				// extension of the class name.
				className = getUtfString(wclass, wclass->classNameIndex);
				nextClass = wclass->nextClass;
				unlockWarpRec(wclass->byteRep, className.len + 6); // + 6 for ".class"
				wclass = nextClass;
				}
			}
		VmFree();
		}
	for (i = 0; i < classDbCount; i++)
		DmCloseDatabase(classDbList[i]);
	if (classDbList != 0)
		xfree(classDbList);
#ifdef SECURE_CLASS_HEAP
	// close the class heap db
	if (classHeapRecP != 0)
		MemHandleUnlock(classHeapRecH);
	if (classHeapRecH != 0)
		DmReleaseRecord(classHeapDmRef, 0, false);
	if (classHeapDmRef != 0)
		DmCloseDatabase(classHeapDmRef);
	delClassHeapDb();
#endif
	}

DWord PilotMain(Word cmd, Ptr cmdPBP, Word launchFlags)
	{
	DWord romVersion;
	RectangleType rect;
	WObject mainWinObj;
	unsigned short err;
	int runApp;
	SndCommandType sndCmd;
	
	if (cmd != sysAppLaunchCmdNormalLaunch && cmd != 40404)
		return 0;

	// NOTE: set vmStatus to 0 since we may not call VmInit() if there
	// is an device compatibility error or error accessing a class database
	xmemzero((uchar *)&vmStatus, sizeof(vmStatus));

	// Check for a required version of PalmOS - currently PalmOS 2.0
	FtrGet(sysFtrCreator, sysFtrNumROMVersion, &romVersion);
	if (romVersion < 0x02000000)
		{
		if ((launchFlags & sysAppLaunchFlagNewGlobals) &&
			(launchFlags & sysAppLaunchFlagUIApp))
			{
			vmStatus.errNum = ERR_IncompatibleDevice;
			drawErrorWin();
			SysTaskDelay(millisToTicks(2000));
			// PalmOS (all versions) will continuously relaunch
			// unless we switch back to the launcher because this
			// program was launched by another program
			AppLaunchWithCommand(sysFileCDefaultApp, sysAppLaunchCmdNormalLaunch, NULL);
			}
		return sysErrRomIncompatible;
		}

	// draw hourglass
	rect.topLeft.x = 76;
	rect.topLeft.y = 73;
	rect.extent.x = 9;
	rect.extent.y = 14;
	WinDrawRectangle(&rect, 0);
	// top line
	WinEraseLine(77, 74, 83, 74);
	// left indent
	WinEraseLine(76, 77, 76, 82);
	WinEraseLine(77, 78, 77, 81);
	WinEraseLine(78, 79, 78, 80);
	// right indent
	WinEraseLine(82, 79, 82, 80);
	WinEraseLine(83, 78, 83, 81);
	WinEraseLine(84, 77, 84, 82);
	// dots	
	WinEraseLine(80, 80, 80, 80);
	WinEraseLine(80, 82, 80, 82);
	WinEraseLine(79, 83, 79, 83);
	WinEraseLine(81, 83, 81, 83);
	// top line
	WinEraseLine(77, 85, 83, 85);

	runApp = 0;
	if (cmd == 40404)
		runApp = 1;
	if (!runApp)
		{
		sndCmd.cmd = sndCmdFreqDurationAmp;
		sndCmd.param1 = 1000; // hertz
		sndCmd.param2 = 80; // milliseconds
		sndCmd.param3 = sndMaxAmp;
		SndDoCmd(NULL, &sndCmd, 0);
		sndCmd.param1 = 2000;
		SndDoCmd(NULL, &sndCmd, 0);
		sndCmd.param1 = 500;
		SndDoCmd(NULL, &sndCmd, 0);
		}
	mainWinObj = startApp(runApp);
	if (!runApp)
		{
		sndCmd.param1 = 400;
		SndDoCmd(NULL, &sndCmd, 0);
		sndCmd.param1 = 500;
		SndDoCmd(NULL, &sndCmd, 0);
		}

#ifdef QUICKBIND
	if (mainWinObj != 0)
		{
		WClass *vclass;

		// cache method map numbers for commonly called methods
		vclass = WOBJ_class(mainWinObj);
		postEventMethodMapNum = getMethodMapNum(vclass, createUtfString("_postEvent"),
			createUtfString("(IIIIII)V"), SEARCH_ALL);
		onTimerTickMethodMapNum = getMethodMapNum(vclass, createUtfString("_onTimerTick"),
			createUtfString("()V"), SEARCH_ALL);
		if (postEventMethodMapNum == -1 || onTimerTickMethodMapNum == -1)
			mainWinObj = 0;
		}
#endif

	if (mainWinObj == 0)
		VmQuickError(ERR_CantAccessAppClasses);

	// NOTE: we don't get any OS level window repaint events after the program has
	// started because PalmOS uses save-unders when it pops up windows on top of
	// this one (for alerts, launch win, etc.)
	if (vmStatus.errNum == 0)
		drawMainWin();
	else
		drawErrorWin();

	// NOTE: PalmOS gets unhappy and may crash if you create a socket outside of
	// the "main event loop" so we explicity disallow it
	globalNetState = NET_READY_FOR_OPEN;
	while (1) 
		{
		EventType event;
		Long timeout;

		timeout = calcEventTimeout();
		EvtGetEvent(&event, timeout);
		if (SysHandleEvent(&event)) 
			continue;
		if (MenuHandleEvent((void *)0, &event, &err)) 
			continue;
		if (event.eType == appStopEvent)
			break;
		if (timeout != -1)
			timerCheck();
		if (vmStatus.errNum == 0)
			{
			handleMainWinEvent(&event);
			// if error occured during processing, display it
			if (vmStatus.errNum > 0)
				drawErrorWin();
			}
		else
			handleErrorWinEvent(&event);
		}
	stopApp(mainWinObj);

	if (globalNetState == NET_IS_OPEN)
		NetLibClose(globalSocketLibRefNum, false);
	return 0;
	}

