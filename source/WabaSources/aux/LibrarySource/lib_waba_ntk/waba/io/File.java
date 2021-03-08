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
 * File is a file or directory.
 * <p>
 * The File class will not work under the PalmPilot since it does not
 * contain a filesystem.
 * <p>
 * Here is an example showing data being read from a file:
 *
 * <pre>
 * File file = new File("/temp/tempfile", File.READ_ONLY);
 * if (!file.isOpen())
 *   return;
 * byte b[] = new byte[10];
 * file.readBytes(b, 0, 10);
 * file.close();
 * file = new File("/temp/tempfile", File.DONT_OPEN);
 * file.delete();
 * </pre>
 */

public class File extends Stream
{
/** Don't open the file mode. */
public static final int DONT_OPEN = 0;
/** Read-only open mode. */
public static final int READ_ONLY  = 1;
/** Write-only open mode. */
public static final int WRITE_ONLY = 2;
/** Read-write open mode. */
public static final int READ_WRITE = 3; // READ | WRITE
/** Create open mode. Used to create a file if one does not exist. */
public static final int CREATE = 4;

String path;
int mode;


/**
 * Opens a file with the given name and mode. If mode is CREATE, the 
 * file will be created if it does not exist. The DONT_OPEN mode
 * allows the exists(), rename(), delete(), listDir(), createDir()
 * and isDir() methods to be called without requiring the file to be
 * open for reading or writing.
 * @param path the file's path
 * @param mode one of DONT_OPEN, READ_ONLY, WRITE_ONLY, READ_WRITE or CREATE
 */
public File(String path, int mode)
	{
	this.path = path;
	this.mode = mode;
	_nativeCreate();
	}

private native void _nativeCreate();

/**
 * Closes the file. Returns true if the operation is successful and false
 * otherwise.
 */
public native boolean close();


/**
 * Returns true if the file is open for reading or writing and
 * false otherwise. This can be used to check if opening or
 * creating a file was successful.
 */
public native boolean isOpen();


/**
 * Creates a directory. Returns true if the operation is successful and false
 * otherwise.
 */
public native boolean createDir();


/**
 * Deletes the file or directory. Returns true if the operation is
 * successful and false otherwise.
 */
public native boolean delete();


/** Returns true if the file exists and false otherwise. */
public native boolean exists();


/** Returns the length of the file in bytes. If the file is not open
  * 0 will be returned.
  */
public native int getLength();


/** Return the file's path. */
public String getPath()
	{
	return path;
	}

/** Returns true if the file is a directory and false otherwise. */
public native boolean isDir();


/**
 * Lists the files contained in a directory. The strings returned are the
 * names of the files and directories contained within this directory.
 * This method returns null if the directory can't be read or if the
 * operation fails.
 */
public native String []listDir();


/**
 * Reads bytes from the file into a byte array. Returns the
 * number of bytes actually read or -1 if an error prevented the
 * read operation from occurring. After the read is complete, the location of
 * the file pointer (where read and write operations start from)
 * is advanced the number of bytes read.
 * @param buf the byte array to read data into
 * @param start the start position in the array
 * @param count the number of bytes to read
 */
public native int readBytes(byte b[], int off, int len);


/**
 * Writes to the file. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring.
 * After the write is complete, the file pointer (where read and
 * write operations start from) is advanced the number of bytes
 * written.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public native int writeBytes(byte b[], int off, int len);


/**
 * Renames the file. If the path given is in a different directory, the
 * existing file will be moved to the directory in the specified path.
 * Returns true if the renaming was successful and false otherwise.
 */
public native boolean rename(String path);


/**
 * Sets the file pointer for read and write operations to the given
 * position. The position passed is an absolute position, in bytes,
 * from the beginning of the file. To set the position to just after
 * the end of the file, you can call:
 * <pre>
 * file.seek(file.getLength());
 * </pre>
 * True is returned if the operation is successful and false otherwise.
 */
public native boolean seek(int pos);

}