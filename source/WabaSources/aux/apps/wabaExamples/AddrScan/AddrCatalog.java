/*

AddrCatalog.java

Copyright (c) 1998, 1999 Wabasoft 

Wabasoft grants you a non-exclusive license to use, modify and re-distribute
this program provided that this copyright notice and license appear on all
copies of the software.

Software is provided "AS IS," without a warranty of any kind. ALL EXPRESS OR
IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
HEREBY EXCLUDED. THE ENTIRE RISK ARISING OUT OF USING THE SOFTWARE IS ASSUMED
BY THE LICENSEE. 

WABASOFT AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING SOFTWARE.
IN NO EVENT WILL WABASOFT OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL
OR PUNITIVE DAMAGES, HOWEVER CAUSED AN REGARDLESS OF THE THEORY OF LIABILITY,
ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF WABASOFT HAS
BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

*/

import waba.io.*;

/**
 * A class to access the PalmPilot address book catalog.
 */

public class AddrCatalog extends Catalog
{
boolean recordIsValid = false;
byte buf[];
int bufLen, bufPos;
int flags;
int recPos;
AddrRec rec;

public AddrCatalog()
	{
	// PalmPilot address book has creator id: addr and type: DATA
	super("AddressDB.addr.DATA", Catalog.READ_ONLY);
	rec = new AddrRec();
	}

public int getCurrentPos()
	{
	return recPos;
	}

public AddrRec getCurrentRec()
	{
	if (!recordIsValid)
		return null;
	return rec;
	}

public boolean setRecordPos(int pos)
	{
	recPos = pos;
	if (super.setRecordPos(pos) == false)
		{
		recordIsValid = false;
		return false;
		}

	// read record into buffer
	bufLen = getRecordSize();
	buf = new byte[bufLen];
	readBytes(buf, 0, bufLen);
	bufPos = 0;

	// parse record
	int options = readInt();
	for (int i = 0; i < 5; i++)
		{
		rec.phoneLabelId[i] = (options & 0xF);
		options >>>= 4;
		}
	int displayPhone = (options & 0xF) + 1;
	// flags determines which data exists in the record
	flags = readInt();
	bufPos++; // skip byte
	rec.name = readCString();
	rec.firstName = readCString();
	rec.company = readCString();
	for (int i = 0; i < 5; i++)
		rec.phones[i] = readCString();
	rec.address = readCString();
	rec.city = readCString();
	rec.state = readCString();
	rec.zipCode = readCString();
	rec.country = readCString();
	// since we have read the data into an internal data structure,
	// we can unlock the record in the underlying catalog and set buf
	// to null to allow it to be garbage collected (since it is not
	// a local variable)
	buf = null;
	recordIsValid = true;
	super.setRecordPos(-1);
	return true;
	}

// reads an int from the buffer
int readInt()
	{
	int n = bufPos;
	// Convert 4 bytes to an int. Notice the &0xFF to deal with the sign bit
	int i = (((buf[n]&0xFF) << 24) | ((buf[n + 1]&0xFF) << 16) |
		((buf[n + 2]&0xFF) << 8) | (buf[n + 3]&0xFF));
	bufPos = n + 4;
	return i;
	}

// read a C string from the buffer stored in PalmPilot format
String readCString()
	{
	boolean doRead = true;
	if ((flags & 0x1) != 1)
		doRead = false;		
	flags >>>= 1;
	if (!doRead)
		return null;
	int start = bufPos;
	int n = bufPos;
	while (buf[n] != 0)
		n++;
	int len = n - start;
	char c[] = new char[len];
	for (int i = 0; i < len; i++)
		c[i] = (char)buf[start + i];
	bufPos = n + 1;
	return new String(c, 0, len);
	}
}
