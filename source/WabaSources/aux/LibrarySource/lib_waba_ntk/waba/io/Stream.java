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

package waba.io;

/**
 * Stream is the base class for all stream-based I/O classes.
 */

public abstract class Stream
{
/**
 * Reads bytes from the stream. Returns the
 * number of bytes actually read or -1 if an error prevented the
 * read operation from occurring.
 * @param buf the byte array to read data into
 * @param start the start position in the array
 * @param count the number of bytes to read
 */
public abstract int readBytes(byte buf[], int start, int count);

/**
 * Writes bytes to the the stream. Returns the
 * number of bytes actually written or -1 if an error prevented the
 * write operation from occurring.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public abstract int writeBytes(byte buf[], int start, int count);

/**
 * Closes the stream. Returns true if the operation is successful
 * and false otherwise.
 */
public abstract boolean close();
}