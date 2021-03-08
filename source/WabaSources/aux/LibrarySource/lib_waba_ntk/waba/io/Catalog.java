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
 * Catalog is a collection of records commonly referred to as a database
 * on small devices.
 * <p>
 * Here is an example showing data being read from records in a catalog:
 *
 * <pre>
 * Catalog c = new Catalog("MyCatalog", Catalog.READ_ONLY);
 * if (!c.isOpen())
 *   return;
 * int count = c.getRecordCount();
 * byte b[] = new byte[10];
 * for (int i = 0; i < count; i++)
 *   {   
 *   c.setRecord(i);
 *   c.readBytes(b, 0, 10);
 *   ...
 *   }
 * c.close();
 * </pre>
 */

public class Catalog extends Stream
{
/** Read-only open mode. */
public static final int READ_ONLY  = 1;
/** Write-only open mode. */
public static final int WRITE_ONLY = 2;
/** Read-write open mode. */
public static final int READ_WRITE = 3; // READ | WRITE
/** Create open mode. Used to create a database if one does not exist. */
public static final int CREATE = 4;


/**
 * Opens a catalog with the given name and mode. If mode is CREATE, the 
 * catalog will be created if it does not exist.
 * <p>
 * For PalmOS: A PalmOS creator id and type can be specified by appending
 * a 4 character creator id and 4 character type to the name seperated
 * by periods. For example:
 * <pre>
 * Catalog c = new Catalog("MyCatalog.CRTR.TYPE", Catalog.CREATE);
 * </pre>
 * Will create a PalmOS database with the name "MyCatalog", creator id
 * of "CRTR" and type of "TYPE".
 * <p>
 * If no creator id and type is specified, the creator id will default
 * to the creator id of current waba program and the type will default
 * to "DATA".
 * <p>
 * Under PalmOS, the name of the catalog must be 31 characters or less,
 * not including the creator id and type. Windows CE supports a 32
 * character catalog name but to maintain compatibility with PalmOS,
 * you should use 31 characters maximum for the name of the catalog.
 * @param name catalog name
 * @param mode one of READ_ONLY, WRITE_ONLY, READ_WRITE or CREATE
 */
public Catalog(String name, int mode)
	{
	_nativeCreate(name, mode);
	}

private native void _nativeCreate(String name, int mode);


/**
 * Adds a record to the end of the catalog. If this operation is successful,
 * the position of the new record is returned and the current position is
 * set to the new record. If it is unsuccessful the current position is
 * unset and -1 is returned.
 * @param size the size in bytes of the record to add
 */
public native int addRecord(int size);


/**
 * Resizes a record. This method changes the size (in bytes) of the current record.
 * The contents of the existing record are preserved if the new size is larger
 * than the existing size. If the new size is less than the existing size, the
 * contents of the record are also preserved but truncated to the new size.
 * Returns true if the operation is successful and false otherwise.
 * @param size the new size of the record
 */
public native boolean resizeRecord(int size);


/**
 * Closes the catalog. Returns true if the operation is successful and false
 * otherwise.
 */
public native boolean close();


/**
 * Deletes the catalog. Returns true if the operation is successful and false
 * otherwise.
 */
public native boolean delete();


/**
 * Returns the complete list of existing catalogs. If no catalogs exist, this
 * method returns null.
 */
public static native String []listCatalogs();


/**
 * Deletes the current record and sets the current record position to -1.
 * The record is immediately removed from the catalog and all subsequent
 * records are moved up one position.
 */
public native boolean deleteRecord();


/**
 * Returns the number of records in the catalog or -1 if the catalog is not open.
 */
public native int getRecordCount();


/**
 * Returns the size of the current record in bytes or -1 if there is no
 * current record.
 */
public native int getRecordSize();


/**
 * Returns true if the catalog is open and false otherwise. This can
 * be used to check if opening or creating a catalog was successful.
 */
public native boolean isOpen();


/**
 * Sets the current record position and locks the given record. The value
 * -1 can be passed to unset and unlock the current record. If the operation
 * is succesful, true is returned and the read/write cursor is set to
 * the beginning of the record. Otherwise, false is returned.
 */
public native boolean setRecordPos(int pos);


/**
 * Reads bytes from the current record into a byte array. Returns the
 * number of bytes actually read or -1 if an error prevented the
 * read operation from occurring. After the read is complete, the location of
 * the cursor in the current record (where read and write operations start from)
 * is advanced the number of bytes read.
 * @param buf the byte array to read data into
 * @param start the start position in the array
 * @param count the number of bytes to read
 */
public native int readBytes(byte buf[], int start, int count);


/**
 * Advances the cursor in the current record a number of bytes. The cursor
 * defines where read and write operations start from in the record. Returns
 * the number of bytes actually skipped or -1 if an error occurs.
 * @param count the number of bytes to skip
 */
public native int skipBytes(int count);


/**
 * Writes to the current record. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring.
 * After the write is complete, the location of the cursor in the current record
 * (where read and write operations start from) is advanced the number of bytes
 * written.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public native int writeBytes(byte buf[], int start, int count);

}