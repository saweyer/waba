import waba.ui.*;
import waba.io.*;
import newton.*;
import waba.sys.*;

/*
SocketTest

Tests Steve's Sockets
-- Sean
*/

/*
To compile:

javac SocketTest.java
java wababin.Warp c /f 4 SocketTest
java wababin.Exegen /h 40 /w 100 /f 4 SocketTest SocketTest SocketTest
open -a PackType SocketTest.pkg
*/


public class SocketTest extends MainWindow
    {
    public void onStart()
        {
        String s = "seanl\r\n";  // seanlCRLF
        Socket socket = new Socket("jifsan.cs.umd.edu",79);  // finger
        if (!socket.isOpen())
            return;
        byte obuf[] = new byte[7];	// seanlCRLF
        byte buf[] = new byte[1024];	// receiving buffer
        for(int x=0;x<s.length();x++)
            obuf[x] = (byte)s.charAt(x);  // convert to ascii
        socket.writeBytes(obuf, 0, 7);
        int count = socket.readBytes(buf,0,1024);
        char[] c = new char[count];
        for(int x=0;x<count;x++)
            c[x] = (char)buf[x];
        socket.close();
        Ref.call("GetRoot").send("Notify",
                    new Ref(4 /*kNotifyQAlert*/),
                    new Ref(s),
                    new Ref(new String(c)));
        }
    }


