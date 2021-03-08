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

#include <Pilot.h>
#include <SysEvtMgr.h>
#include <SerialMgr.h>
#include <NetMgr.h>
#include <NewFloatMgr.h>
#include <DLServer.h>

#define FREE_ON_EXIT 1
#define SECURE_CLASS_HEAP 1

#ifdef SECURE_CLASS_HEAP
#define LOCK_CLASS_HEAP MemSemaphoreReserve(1);
#define UNLOCK_CLASS_HEAP MemSemaphoreRelease(1);
#else
#define LOCK_CLASS_HEAP ;
#define UNLOCK_CLASS_HEAP ;
#endif

#define uchar unsigned char
#define int32 long
#define uint32 unsigned long
#define float32 float
#define int16 short
#define uint16 unsigned short

//
// type converters
//

#undef FASTANDBIG

#ifdef FASTANDBIG

#define getUInt32(b) (uint32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getUInt16(b) (uint16)(((b)[0]<<8)|(b)[1])
#define getInt32(b) (int32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getInt16(b) (int16)(((b)[0]<<8)|(b)[1])

#else

static uint32 getUInt32(uchar *b)
	{
	return (uint32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) );
	}
static uint16 getUInt16(uchar *b)
	{
	return (uint16)(((b)[0]<<8)|(b)[1]);
	}
static int32 getInt32(uchar *b)
	{
	return (int32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) );
	}
static int16 getInt16(uchar *b)
	{
	return (int16)(((b)[0]<<8)|(b)[1]);
	}

#endif

static float32 getFloat32(uchar *buf)
	{
	uint32 i;
	float32 f;

	// we need to make sure we're aligned before casting
	i = ((uint32)buf[0] << 24) | ((uint32)buf[1] << 16) | ((uint32)buf[2] << 8) | (uint32)buf[3];
	f = *((float32 *)&i);
	return f;
	}

//
// x portability functions
//

#define xstrncmp(s1, s2, n) StrNCompare(s1, s2, n)

#define xstrncpy(dst, src, n) StrNCopy(dst, src, (ULong)n)

#define xstrlen(s) StrLen(s)

#define xstrcat(dst, src) StrCat(dst, src)

#define xmemmove(dst, src, size) MemMove(dst, src, size)

#define xmemzero(mem, len) MemSet(mem, len, (Byte)0)

static void *xmalloc(uint32 size)
	{
	VoidHand memH;
	uchar *ptr;

	// we stick the handle in the first bytes and then return a pointer
	// inside the memory block. Then when we free it, we can get the
	// memory handle back to unlock and free it without having to track it.
	memH = MemHandleNew(sizeof(VoidHand) + size);
	if (!memH)
		return NULL;
	ptr = MemHandleLock(memH);
	*((VoidHand *)ptr) = memH;
	ptr += sizeof(VoidHand);
	return ptr;
	}

static void xfree(void *p)
	{
	VoidHand memH;
	uchar *ptr;

	ptr = (uchar *)p - sizeof(VoidHand);
	memH = *((VoidHand *)ptr);
	MemHandleUnlock(memH);
	MemHandleFree(memH);
	}
