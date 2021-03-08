package waba.io;

public class PersistentStore extends Stream
{
/**
Creates a persistent store identified by pssymbol in the namespace of the 
application given by appSymbol.  If null is passed in for either
appSymbol or for psSymbol, then that symbol will be the value returned 
by Vm.appSymbol();

        -- note: Vm.appSymbol() is a new native method I propose which
        returns a unique identifier for this application.  It must not be
        non-null.
**/

public PersistentStore(String appSymbol, String psSymbol)
        {
        _nativeCreate(appSymbol==null ? Vm.appSymbol: appSymbol,
                      psSymbol==null ? Vm.appSymbol: psSymbol);
        }
public PersistentStore(String psSymbol) { this(null,psSymbol); }
private native void _nativeCreate( String appSymbol, String psSymbol);

/** Returns the human readable name for this Catalog, which may or
may not be presented to the user in lieu of the appSymbol and
psSymbol.  Note that not all operating systems can use this name or will
use it; indeed, some will simly ignore your attempts to set it, and always
return some predetermined value (like the psSymbol) when you get the
humanReadableName. */

public native String humanReadableName();
public native void setHumanReadableName(String name);

public static final int STATUS_INVALID = 1;  // the store cannot be accessed
public static final int STATUS_READ_ONLY = 2;  // the store can only be read  -- this
                                               // should also always turn on 							       // STATUS_RECORD_READ_ONLY

public static final int STATUS_CURSOR_BEFORE_FIRST = 4;  // cursor is before first record
public static final int STATUS_CURSOR_AFTER_LAST = 8;    // cursor is after last record
        // note, if STATUS_CURSOR_BEFORE_FIRST and STATUS_CURSOR_AFTER_LAST are both 	// true, then this of course indicates that the store is empty

public static final int STATUS_RECORD_READ_ONLY = 16; // the current record is read-only
public static final int STATUS_RECORD_INVALID = 32; 	// the record was invalidated,
                                                       // possibly by another process

/** Returns the status of the persistent store */
public native int status();

// Cursor scan functions -- return true if successful, otherwise see status.
public native boolean first();
public native boolean last();
public native boolean move(int num);  // moves *num* records forward or backward

/**
 * Returns the record symbol.
 */ 
public native String recordSymbol();

/**
 * Deletes the current record if it can.  The new position is not defined.
 */
public native boolean deleteRecord();

/**
 * Returns the number of records in the catalog, or -1 if failed.
 */
public native int getRecordCount();

/**
 * Returns the maximum permittable size of a catalog record.
 */ 
public native int getMaximumRecordSize();

/**
 * Returns the size of the current record in bytes or -1 if failed.
 */
public native int getRecordSize();

/**
 * Resizes a record. This method changes the size (in bytes) of the current record.
 * The contents of the existing record are preserved if the new size is larger
 * than the existing size.  New bytes are padded with 0. 
 * If the new size is less than the existing size, the
 * contents of the record are also preserved but truncated to the new size.
 * Returns true if the operation is successful and false otherwise.  If the
 * record is truncated to a size smaller than the current position in the stream,
 * then the position is set to just beyond the end of the newly-truncated record
 * (that is, writes will now extend the record).  Otherwise, the position in the
 * stream is unaffected by this operation.
 * @param size the new size of the record
 */
public native boolean resizeRecord(int size);

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
  <p><b><font color=red>NOTE: You should be able to skip negative bytes; this method should
  return simply the number of bytes skipped successfully, error or not.</font></b>
 * @param count the number of bytes to skip
 */
public native int skipBytes(int count);


/**
 * Writes to the current record. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring.
 * After the write is complete, the location of the cursor in the current record
 * (where read and write operations start from) is advanced the number of bytes
 * written.
  <p><b><font color=red>NOTE: if writing will go off the end of the record, we'll try to gracefully resize it.
 If it gets too big, then we issue an error.</font></b>
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public native int writeBytes(byte buf[], int start, int count);








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
  <p><b><font color=red>NOTE 1: No consideration was given to situations where you have multiple
  Catalog objects open pointing to the same catalog.  What if one deletes the
  catalog and others are pointing to it?  What if one deletes or changes a 
  record others are pointing to?  Locking is a problem.</font></b>

  <p><b><font color=blue>NOTE 2: The modes are not well thought-out.  Does the CREATE mode
    create a read-only, write-only, or read/write database?  In fact, the notion of modes
    is an artifact of a C heritage -- modes should not even exist.  Rather, when you
    construct a Catalog, it should create it if it's not already created, and then open
    it as READ_WRITE.  The only situation where you'd want to bother with modes is 
    if you have a database system that is able to obtain shared vs. exclusive locks, in which case
    it makes no sense to have a WRITE_ONLY mode.  Most decent databases along these lines
    open specific *records* with shared or exclusive locks, not whole catalog files anyway.
    I recommend that modes be eliminated entirely.  If your operating system permits locks
    on catalogs, and you can't obtain a lock, then creating the Catalog simply returns it
    in a "closed" state.  If you *can* obtain a lock, then creating the Catalog should
    obtain an exclusive lock on it.  Finally, if your operating system does not permit
    locks on catalogs, then you must realize that catalog records can and will change
    out from underneath you.</font></b>

  <p><b><font color=green>NOTE 3: Names are not well thought-out.  Clearly the Waba designer
    owner wanted to make it possible to open *any* catalog, owned by Waba or not.  However,
    in doing so he assumed that (1) the operating system uses "catalogs" at a fundamental
    level, and (2) such catalogs are lists of byte records.  Of course, for many OS's, #1
    is false, and for others (like NewtonOS) #2 is false -- on NewtonOS, catalogs are
    sophisticated databases of frame objects which can contain a variety of structured data, 
    not just bytes.  What all this means is that it's probably not a good thing to have
    a core Catalog class be so platform-specific.  If we keep Catalog, it should be
    a guaranteed permanent storage facility, but not wide open to all the "catalogs" in
    the file system.  This means that the name procedure should NOT permit stuff like
    specifying the PalmOS creator and type.  Instead, you should specify two names:
    
    <p><ol>
        <li> A short, human-readable name that the system can optionally present to the
           user in the user's file system.  For example, your catalog might have a
           human-name of "Santa Prefs" or "FATM High Scores".
        
         <li> A unique symbol which identifies your catalog within a namespace unique to
           your application.  The Waba system will modify this symbol as appropriate.
           For example, if your application is represented in the Newton as "foo",
           you call your catalog "bar", then Waba for the Newton will open a catalog
           called "waba:foo:bar".  Thus, you cannot see catalogs written by other
           applications.  Perhaps if you want to share a catalog with another Waba app,
           you could open a "global" catalog (global to Waba apps anyway).  If this global
           catalog is called "baz", then the Newton might open it as "wabaglobal:baz".
           We'd have to consider this facility further.
    </ol>
    
    <p>ListCatalogs() should list only those catalogs within the namespace of the
    application (or perhaps also the "global" catalogs within the namespace of Waba).
    
    <p>The system should make all its decisions internally about TYPE and CREATOR and all
    that mumbo-jumbo on an implementation level.  This should not be stuff that the Waba
    application should be able to get ahold of.  If you want a Waba app to be able to
    access such features (which should be strongly discouraged, as it would make the app
    non-cross platform), then you should make a *separate* catalog facility, like
    waba.palm.io.PalmCatalog or something.
    </font></b>

  <p><b><font color=red>NOTE 4:</font></b> Heavens!  Catalog does not have any test
  for read-only records.  It's entirely possible for a catalog to be partially read-only
  because the user has set a FLASH Ram card to be write protected, and the catalog exists
  partially on that card and partially elsewhere.
  
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
  <p><b><font color=red>NOTE: Not all systems can definitively add a record to the "end" of the catalog -- databases don't work like this.  For example, on the Newton you simply add entries, and you get no say over what order they appear; you can only control order with various indexed query mechanisms, but Waba is not capable of handling indexes.  The trouble here is that some Waba applications may wrongly assume that if they add 5 records to the soup, their positions will be precisely as they were added -- Nope! </font></b>
 */
public native int addRecord(int size);


/**
 * Resizes a record. This method changes the size (in bytes) of the current record.
 * The contents of the existing record are preserved if the new size is larger
 * than the existing size. If the new size is less than the existing size, the
 * contents of the record are also preserved but truncated to the new size.
 * Returns true if the operation is successful and false otherwise.
  <p><b><font color=red>NOTE: This oughta be a worthless method.  Writing should autoresize.</font></b>
  <p><b><font color=blue>NOTE: How does this effect the stream?  Do we reset to the beginning of the record?  Do we keep our current value?  What if the current value is beyond the new record length?  I am presently assuming that we keep the current value, unless it's beyond the record length, in which case we reset it to 0. </font></b>
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
  <p><b><font color=red>NOTE: A procedure should be available to get the maximum
  permitted size.</font></b>
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
  <p><b><font color=red>NOTE: The Newton does not have notions of shared or exclusive locks; thus locking and unlocking cannot be implemented.</font></b>
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
  <p><b><font color=red>NOTE: You should be able to skip negative bytes; this method should
  return simply the number of bytes skipped successfully, error or not.</font></b>
 * @param count the number of bytes to skip
 */
public native int skipBytes(int count);


/**
 * Writes to the current record. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring.
 * After the write is complete, the location of the cursor in the current record
 * (where read and write operations start from) is advanced the number of bytes
 * written.
  <p><b><font color=red>NOTE: if writing will go off the end of the record, we'll try to gracefully resize it.
 If it gets too big, then we issue an error.</font></b>
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public native int writeBytes(byte buf[], int start, int count);

}
