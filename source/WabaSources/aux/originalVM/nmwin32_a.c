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

#include <windows.h>
#include <stdlib.h>

#ifndef WINCE
#include <stdio.h>
#include <conio.h>
#endif

#ifdef WINCE
#include <winsock.h>
#include <winnetwk.h>
#endif

#undef FREE_ON_EXIT
#undef SECURE_CLASS_HEAP
#define LOCK_CLASS_HEAP
#define UNLOCK_CLASS_HEAP

#define uchar unsigned char
#define int32 int
#define uint32 unsigned int
#define float32 float
#define int16 short
#define uint16 unsigned short

//
// type converters
//

#define getUInt32(b) (uint32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getUInt16(b) (uint16)(((b)[0]<<8)|(b)[1])

#define getInt32(b) (int32)( (uint32)((b)[0])<<24 | (uint32)((b)[1])<<16 | (uint32)((b)[2])<<8 | (uint32)((b)[3]) )
#define getInt16(b) (int16)(((b)[0]<<8)|(b)[1])

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

// NOTE: The str, mem and malloc routines aren't documented in the reference
// manuals for CE, however, the methods do exist in the CE library and have
// prototypes, etc.

#define xstrncmp(s1, s2, n) strncmp(s1, s2, n)

#define xstrncpy(dst, src, n) strncpy(dst, src, n)

#define xstrlen(s) strlen(s)

#define xstrcat(dst, src) strcat(dst, src)

#define xmemmove(dst, src, size) memmove(dst, src, size)

#define xmemzero(mem, len) memset(mem, 0, len)

#define xmalloc(size) malloc(size);

#define xfree(ptr) free(ptr)

