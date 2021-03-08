//
//  SerialTest2.java
//  
//
//  Created by sean on Wed May 23 2001.
//	Fixed by Paul June 6 2001
//

/*

jikes SerialTest2.java
java wababin.Warp c /f 4 SerialTest2
java wababin.Exegen /h 40 /w 100 /f 4 SerialTest2 SerialTest2 SerialTest2
open -a PackType SerialTest2.pkg

*/

import waba.io.*;
import waba.ui.*;
import waba.sys.*;
import newton.*;

public class SerialTest2 extends MainWindow
	{
	
	public void printBuffer(byte[]buf, int size, String msg)
		{
		String s = "";
		for(int x=0;x<size;x++)
			{
			int b = buf[x];
			if (b < 0) b += 256;
			s = s + b + " ";
			}
		s = s + "\r##";
		for(int x=0;x<size;x++)
			{
			int b = buf[x];
			if (b < 0) b += 256;
			s = s + (char)b;
		}
		s = s + "##\r";
		Ref.call("GetRoot").send("Notify",
			new Ref(4), new Ref(msg),
			new Ref(s));
	}
		
	public void blockingRead(byte[] buf, int size, Stream s)
	{
		int ptr = 0;
		int maxcount = 100;
		do
		{
			ptr += s.readBytes(buf,ptr,size-ptr);
			maxcount = maxcount - 1;
			if (maxcount == 0)
			{
				Ref.call("GetRoot").send("Notify",
					new Ref(4), new Ref("SerialTest2"),
					new Ref("Time out"));
				break;
			}
		} while (ptr<size);
	}

	public void onStart()
	{
		byte[] buf = new byte[5];
		SerialPort stream = new SerialPort(1, 9600, 8, false, 1);
		
		if (stream.isOpen())
		{
			// Ensure that the buffer is full of zeroes before passing it to blockingRead
			int index_i;
			for (index_i = 0; index_i < 5; index_i++)
			{
				buf[index_i] = 0;
			}
			blockingRead(buf,5,stream);
			printBuffer(buf, 5, "Did we get WMS2?");

			for (index_i = 0; index_i < 4; index_i++)
			{
				buf[index_i] = 0;
			}
			
			blockingRead(buf,4,stream);
			printBuffer(buf, 4, "Next 4...");
		} else {
			Ref.call("GetRoot").send("Notify",
				new Ref(4), new Ref("SerialTest2"),
				new Ref("The port cannot be open"));
		}
	}
}

// ============================================ //
// Our OS who art in CPU, UNIX be thy name.     //
//         Thy programs run, thy syscalls done, //
//         In kernel as it is in user!          //
// ============================================ //

