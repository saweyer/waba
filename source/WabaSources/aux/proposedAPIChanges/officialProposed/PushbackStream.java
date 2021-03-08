/**
PushbackStream wraps Stream objects with a pushback facility; this
is very useful for doing parsing.  It's also important
    for the "temporary burp" status situations -- the stream timed out, or
    the system needs to unblock the event loop to continue servicing requests,
    etc.  Let's say you need 10 bytes.  You make a call but only get 6 bytes,
    and check the status and discover that the stream had a temporary burp.
    The pushback buffer lets you "push back" the 6 bytes into the stream, so you
    can set a timer (or whatever) and come back later and try requesting the
    full 10 bytes again.  

Copyright (c) 2001 by Sean Luke.
*/

package waba.io;
import waba.sys.Vm;

public class PushbackStream extends Stream
{
protected Stream stream;
/** 
 * If true, then every time the pushback buffer is
 * completely tapped out, it will be deleted.  Setting this to true
 * is more efficient with memory but can be much slower.
 */
public boolean resetPushbackIfEmptied;

/**
 * The number of bytes in the pushback buffer
 */
protected int pushbackBufferSize=0;

/**
 * The pushback buffer
 */
protected byte[] pushbackBuffer=null;

/**
 * Creates a PushbackStream wrapped around Stream.
 * If resetPushbackIfEmptied==true, then every time the pushback buffer is
 * completely tapped out, it will be deleted.  Setting this to true
 * is more efficient with memory but can be much slower.
 */
 
public PushbackStream(Stream stream, boolean resetPushbackIfEmptied)
    {
    this.resetPushbackIfEmptied = resetPushbackIfEmptied;
    this.stream = stream;
    }
    
/**
 * Pushes back <tt>count</tt> bytes into the stream, starting at <tt>start</tt>.
 * @param buf the byte array to push back data from
 * @param start the start position in the array
 * @param count the number of bytes to push back
 */
 
public void pushbackBytes(byte buf[], int start, int count)
    {
    // we implement this using auto-expanding arrays rather than
    // waba.util.Vector because we want to be fast.
    
    // initialize if necessary
    if (pushbackBuffer==null) 
        {
        pushbackBuffer = new byte[count];
        pushbackBufferSize=0;
        }
    
    // expand if necessary
    if (pushbackBuffer.length-pushbackBufferSize < count)
        {
        byte[] newbuffer = new byte[pushbackBuffer.length*2];
		Vm.copyArray(newbuffer, 0, pushbackBuffer, 0, pushbackBufferSize);
        pushbackBuffer = newbuffer;    
        }
    
    // push back the bytes (in reverse order!)
    for(int x=start+count-1;x>=start;x--)
        pushbackBuffer[pushbackBufferSize++] = buf[x];
    }

/**
 * Pops at most <tt>count</tt> bytes and writes them into <tt>buf</tt>, 
 * starting at <tt>start</tt>.  Returns the number of bytes actually 
 * popped out of the buffer.  Fewer bytes than requested may be popped 
 * if the stream's pushback buffer doesn't hold that many.  
 * This function is primarily a helper function for readBytes(...).
 * @param buf the byte array to push back data from
 * @param start the start position in the array
 * @param count the number of bytes to push back
 */
protected int popBytes(byte buf[], int start, int count, boolean resetPushbackIfEmptied)
    {
    // does the buffer exist?
    if (pushbackBuffer==null) return 0;
    // read bytes -- in reverse order!
    if (count < pushbackBufferSize) count = pushbackBufferSize;
    for(int x=start;x<start+count;x++)
        buf[x] = pushbackBuffer[--pushbackBufferSize];
    // optionally reset the buffer
    if (pushbackBufferSize==0 && resetPushbackIfEmptied) pushbackBuffer=null;
    return count;
    }


/**
 * Reads bytes from the stream.  Returns the
 * number of bytes actually read or -1 if an error prevented the
 * read operation from occurring at all.  If the return value
 * is less than <tt>count</tt>, readBytes should set the status
 * to indicate what the error is that caused it to be unable 
 * to service the request.
 * <p><b>Note on resetting the pushback buffer.</b>  Resetting the
 * pushback buffer is a trade-off of speed for space which you will have to make.
 * The buffer automatically expands to hold the data filled with pushBytes(...),
 * so if you do not expect to push nearly that many bytes back into the 
 * buffer again, you may want to garbage-collect, which will set the buffer to
 * null.  The next time you call pushBytes(...) the buffer will be recreated at
 * some initial (small) size.  But the garbage collection and recreation is
 * slow, so it's your call, depending on the size and complexity of your
 * Waba application.  
 * @param buf the byte array to read data into
 * @param start the start position in the array
 * @param count the number of bytes to read
 */
public int readBytes(byte buf[], int start, int count)
    {
    // first pop some bytes into the buffer
    int result = popBytes(buf,start,count,resetPushbackIfEmptied);
    if (count > result && stream!=null) 
        return result + stream.readBytes(buf,start+result,count-result);
    else return result;
    }

public int writeBytes(byte buf[], int start, int count)
    {
    if (stream==null) return 0;
    else return stream.writeBytes(buf,start,count);
    }

public boolean close()
    {
    if (stream==null) return false;
    else return stream.close();
    }
    
public int getStatus() 
    {
    if (stream==null) return Stream.STATUS_INTERNAL_ERROR;
    int s = stream.getStatus();
    if (s < 0 && pushbackBufferSize>0)
           // there's an error, but we still return STATUS_READY
           // if we have something in the buffer
        return Stream.STATUS_READY;
    else return s;
    }
}


