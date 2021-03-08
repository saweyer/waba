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
 * SerialPort accesses a device's serial port.
 * <p>
 * Serial port access is only available when running under a native WabaVM, it
 * is not supported when running under Java.
 * <p>
 * When a serial port is created, an attempt is made to open the port.
 * If the open attempt is successful, a call to isOpen()
 * will return true and the port will remain open until close() is called.
 * If close() is never called, the port will be closed when the object
 * is garbage collected.
 * <p>
 *
 * Here is an example showing data being written and read from a serial port:
 *
 * <pre>
 * SerialPort port = new SerialPort(0, 9600);
 * if (!port.isOpen())
 *   return;
 * byte buf[] = new byte[10];
 * buf[0] = 3;
 * buf[1] = 7;
 * port.writeBytes(buf, 0, 2);
 * int count = port.readBytes(buf, 0, 10);
 * if (count == 10)
 *   ...
 * port.close();
 * </pre>
 */

public class SerialPort extends Stream
{
/**
 * Opens a serial port. The number passed is the number of the
 * serial port for devices with multiple serial ports. Port number
 * 0 defines the "default port" of a given type. For Windows CE
 * and PalmPilot devices, you should pass 0 as the port number to 
 * access the device's single serial port.
 * <p>
 * On Windows devices, port numbers map to COM port numbers.
 * For example, serial port 2 maps to "COM2:".
 * <p>
 * Here is an example showing how to open the serial port of a
 * PalmPilot device at 9600 baud with settings of 8 bits,
 * no partity and one stop bit (8/N/1):
 * <pre>
 * SerialPort port = new SerialPort(0, 9600, 8, false, 1);
 * </pre>
 * Here is an example of opening serial port COM2: on a Windows device:
 * <pre>
 * SerialPort port = new SerialPort(2, 57600, 8, false, 1);
 * </pre>
 * No serial XON/XOFF flow control (commonly called software flow control)
 * is used and RTS/CTS flow control (commonly called hardware flow control)
 * is turn on by default on all platforms but Windows CE. The parity setting
 * is a boolean. If false, no parity is used. If true, "even" parity is used.
 *
 * @param number port number
 * @param baudRate baud rate
 * @param bits bits per char [5 to 8]
 * @param parity true for even parity, false for no parity
 * @param stopBits number of stop bits
 * @see #setFlowControl
 */
public SerialPort(int number, int baudRate, int bits, boolean parity, int stopBits)
	{
	_nativeCreate(number, baudRate, bits, parity, stopBits);
	}

/** 
 * Open a serial port with settings of 8 bits, no parity and 1 stop bit.
 * These are the most commonly used serial port settings.
 */
public SerialPort(int number, int baudRate)
	{
	this(number, baudRate, 8, false, 1);
	}

private native void _nativeCreate(int number, int baudRate,
	int bits, boolean parity, int stopBits);

/**
 * Closes the port. Returns true if the operation is successful
 * and false otherwise.
 */
public native boolean close();


/**
 * Returns true if the port is open and false otherwise. This can
 * be used to check if opening the serial port was successful.
 */
public native boolean isOpen();


/**
 * Turns RTS/CTS flow control (hardware flow control) on or off.
 * @param on pass true to set flow control on and false to set it off
 */
public native boolean setFlowControl(boolean on);


/**
 * Sets the timeout value for read operations. The value specifies
 * the number of milliseconds to wait from the time of last activity
 * before timing out a read operation. Passing a value of 0 sets
 * no timeout causing any read operation to return immediately with
 * or without data. The default timeout is 100 milliseconds. This
 * method returns true if successful and false if the value passed
 * is negative or the port is not open.
 * @param millis timeout in milliseconds
 */
public native boolean setReadTimeout(int millis);


/**
 * Reads bytes from the port into a byte array. Returns the
 * number of bytes actually read or -1 if an error prevented the read
 * operation from occurring. The read will timeout if no activity
 * takes place within the timeout value for the port.
 * @param buf the byte array to read data into
 * @param start the start position in the byte array
 * @param count the number of bytes to read
 * @see #setReadTimeout
 */
public native int readBytes(byte buf[], int start, int count);


/**
 * Returns the number of bytes currently available to be read from the
 * serial port's queue. This method only works under PalmOS and not WinCE
 * due to limitations in the Win32 CE API. Under Win32 and Java,
 * this method will always return -1.
 */
public native int readCheck();


/**
 * Writes to the port. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring. If data
 * can't be written to the port and flow control is on, the write
 * operation will time out and fail after approximately 2 seconds.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
public native int writeBytes(byte buf[], int start, int count);

}