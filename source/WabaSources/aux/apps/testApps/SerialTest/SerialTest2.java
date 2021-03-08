//
//  AmigoBot.java
//  
//
//  Created by sean on Wed May 23 2001.
//  Copyright (c) 2001 __CompanyName__. All rights reserved.
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
    Label label;
    
    public void printBuffer(byte[]buf, int size, String msg)
        {
        String s = "";
        for(int x=0;x<size;x++)
            {
            int b = buf[x];
            if (b < 0) b += 256;
            s = s + b + " ";
            }
        s = s + "\n##";
        for(int x=0;x<size;x++)
            {
            int b = buf[x];
            if (b < 0) b += 256;
            s = s + (char)b;
            }
        s = s + "##\n";
        Ref.call("GetRoot").send("Notify",
            new Ref(4), new Ref(msg),
            new Ref(s));
        }
        
    public byte[] blockingRead(byte[] buf, int size, Stream s)
        {
        int ptr = 0;
        do
            {
            ptr = s.readBytes(buf,ptr,size-ptr);
            label.setText("Got: " + ptr);
            }
        while (ptr<size);
        return buf;
        }

    public void onStart()
        {
        // We start by making an ordinary Waba label
        label = new Label("Press:");
        label.setRect(10, 10, 80, 30);
        add(label);
        
        byte[] buf = new byte[5];
        SerialPort stream = new SerialPort(0, 9600, 8, false, 1);
        //stream.setReadTimeout(1000);  // 1 second timeout
        
        printBuffer(blockingRead(buf,5,stream), 5, "Did we get WMS2?");
            printBuffer(blockingRead(buf,4,stream), 4, "Next 4...");
        }
    }
