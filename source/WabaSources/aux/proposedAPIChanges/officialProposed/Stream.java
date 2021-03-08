/*
PROPOSED CHANGE SUMMARY:

<p> Stream has been modified to add two features sorely needed by the Waba
Stream class:
<dl><dt><b>A status flag</b><dd>
    Streams now indicate their status (including the particulars of errors)
    by setting a status flag, which can be read by the user to inform the
    user of current stream issues.  The status values are divided into three
    ranges:
        <ul><li>0: the stream is doing fine
        <li>-1 to -99: the stream had a fatal error and cannot proceed beyond
            the data it has provided.
        <li>-100 and below: the stream has had a temporary burp in operations.
            Try again in a little while.  These status conditions make possible
            non-blocking reads.
        </ul>
<dt><b>Timeouts and requests to unblock the event loop</b><dd>
    Streams can now time out, or demand that you set a timer and unblock the
    event loop.  This is because many small systems, such as Newtons, do not
    have synchronous (blocking) reads -- only <i>asynchronous</i> reads which require
    that the event loop be unblocked before the read can be processed.
    The old Stream class demanded synchronous reads, which are impossible to
    realistically provide on these systems.
</dl>

Additionally, there is a new Stream subclass, PushbackStream, which provides
pushback wrapper facilities.

*/

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
import waba.sys.Vm;

/**
 * Stream is the base class for all stream-based I/O classes.
 */

public abstract class Stream
{
/// PROPOSED CHANGE
// Added various STATUS constants so you can determine what error or
// other state the Stream is in.  This is necessary because Stream doesn't
// throw exceptions.  STATUS_READY is 0.  If the status is between -99
// and -1, then no data can be provided any more.  If the status is
// <= -100, then more data might be provided if you perform certain
// actions.


/** Ready Status.
    Indicates that the Stream is able to be read from or written to.
    */
public static final int STATUS_READY = 0;
/** End-of-file Status.
    Indicates that the Stream has been exhausted, and cannot be
    written to nor read from in the unit size you've requested.  If you are
    reading units larger than bytes (this is an integer or 
    object Stream, say), then it is possible that there are still
    bytes left in the Stream, but not enough to form another 
    unit for you to read.
    */
public static final int STATUS_EOF = -1;
/** Internal Error Status.
    Indicates that the Stream has encountered an internal error,
    and cannot read or write.
*/
public static final int STATUS_INTERNAL_ERROR = -2;
/** Closed Status.  The stream has been closed by you and is no longer useful. */
public static final int STATUS_CLOSED = -3;
/** Read-only Status.  An attempt was made to write to a read-only stream. */
public static final int STATUS_READ_ONLY = -4;
/** Not Opened Yet Status.
    Indicates that the Stream has not been set up yet and
    does not yet have any data to provide, nor can be written to.
    Precedes <tt>STATUS_READY</tt>.  This status can only occur for
    Stream objects which do not open the stream upon initialization.
    */
public static final int STATUS_NOT_OPENED_YET = -100;
/** Request to Unblock the Event Loop Status.  The Stream may be able to
    provide you with data to read, but only if you release the event loop
    (set a timer and return).  We suggest a 50ms timer or longer.
    This happens a lot on Newtons and
    other devices which cannot service serial port or socket access
    without having control of the event loop.  You may need to release
    the event loop several times before the stream will have data for you.
    */
public static final int STATUS_UNBLOCK = -101;
/** Timed Out Status.  A read request on the stream timed out.  However if
    you try again you may be able to get more data.  We suggest you set a
    timer and return again later, or otherwise give the user an option to cancel the
    process.
    */
public static final int STATUS_TIMED_OUT = -102;


/// PROPOSED CHANGE
// Add a status variable to Stream.  See the changes above.

/**
 * The present status of the Stream.  Should be protected
 * so PushbackStream works properly.
 */
protected int status = STATUS_READY;


/// PROPOSED CHANGE
// Add a status variable accessor to Stream.  See the changes above.

/**
 * Returns the present status of the Stream.
 */
public int getStatus() { return status; }

/// PROPOSED CHANGE
// readBytes(...), writeBytes(...) and close(...), among other
// stream methods, should properly set the status int.

/**
 * Reads bytes from the stream.  Returns the
 * number of bytes actually read or -1 if an error prevented the
 * read operation from occurring at all.  If the return value
 * is less than <tt>count</tt>, readBytes should set the status
 * to indicate what the error is that caused it to be unable 
 * to service the request. 
 * @param buf the byte array to read data into
 * @param start the start position in the array
 * @param count the number of bytes to read
 */
public abstract int readBytes(byte buf[], int start, int count);

/**
 * Writes bytes to the the stream. Returns the
 * number of bytes actually written or -1 if an error prevented the
 * write operation from occurring at all.  If the return value
 * is less than <tt>count</tt>, writeBytes should set the status
 * to indicate what the error is that caused it to be unable 
 * to service the request.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public abstract int writeBytes(byte buf[], int start, int count);

/**
 * Closes the stream. Returns true if the operation is successful
 * and false otherwise.  The status will be set to STATUS_CLOSED
 * by this operation.
 */
public abstract boolean close();
}


