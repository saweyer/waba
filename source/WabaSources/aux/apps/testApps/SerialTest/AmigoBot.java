//
//  AmigoBot.java
//  
//
//  Created by sean on Wed May 23 2001.
//  Copyright (c) 2001 __CompanyName__. All rights reserved.
//

/*

jikes AmigoBot.java
java wababin.Warp c /f 4 AmigoBot
java wababin.Exegen /h 40 /w 100 /f 4 AmigoBot AmigoBot AmigoBot
open -a PackType AmigoBot.pkg

*/

import waba.io.*;
import waba.ui.*;
import waba.sys.*;
import newton.*;

public class AmigoBot extends MainWindow
    {
    
    /** The AmigoBot code for checksum (p. 22) is icky.
        Here's my best shot at doing the same thing in Java */
    public int checksum(byte[] data)
        {
        String s = "";
        int c=0, data1, data2;
        for(int x=0;x<data.length;x+=2)
            {
            data1 = data[x];  if (data1 < 0) data1 += 256;
            data2 = data[x+1];  if (data2 < 0) data2 += 256;
            c += ( (data1 << 8) | data2);
            c = c & 0xffff;
            s = s + Convert.toString(c) + " ";
            }
        if (data.length % 2 == 1)  // odd
            {
            data1 = data[data.length-1];  if (data1 < 0) data1 += 256;
            c = c ^ data1;
            s = s + Convert.toString(c) + " ";
            }
              //  Ref.call("GetRoot").send("Notify",
              //      new Ref(4 /*kNotifyQAlert*/),
               //     new Ref("Checksum data:"),
               //     new Ref(new String(s)));
        return c;
        }
    
    public byte[] printOut(byte[] data)
        {
        int checksum = checksum(data);

        String s = "";
        s = s + Convert.toString(HEADER[0]) + " " + Convert.toString(HEADER[1]) + " ";
        s = s + Convert.toString((byte)(data.length+2)) + " ";
        for(int x=0;x<data.length;x++)
            s = s + Convert.toString(data[x]) + " ";
        s = s + Convert.toString((byte)(checksum >>> 8))+ " ";
        s = s + Convert.toString((byte)(checksum & 0x00ff));
                Ref.call("GetRoot").send("Notify",
                    new Ref(4 /*kNotifyQAlert*/),
                    new Ref("Sent:"),
                    new Ref(new String(s)));
        return data;
        }
    
    public byte[] printIn(byte[] data)
        {
        String s = "";
        for(int x=0;x<data.length;x++)
            s = s + Convert.toString(data[x]) + " ";
                Ref.call("GetRoot").send("Notify",
                   new Ref(4 /*kNotifyQAlert*/),
                    new Ref("Got:"),
                    new Ref(new String(s))); 
        return data;
        }
    
    public static final byte[] HEADER = { (byte)(0xFA), (byte)(0xFB) };
    
    public void submit(byte[] data, Stream out)
        {
        int checksum = checksum(data);
        // write out the header
        out.writeBytes(HEADER,0,2);
        // write out the bytecount
        out.writeBytes(new byte[] {(byte)(data.length+2)},0,1); // remember this cannot exceed 200!
        // write out the data
        out.writeBytes(data,0,data.length);
        // write out the checksum
        out.writeBytes(new byte[] {(byte)(checksum >>> 8), (byte)(checksum & 0x00ff)}, 0, 2);  // reverse order
        }
        
        SerialPort stream;
    public void onStart()
        {
        stream = new SerialPort(1, 9600, 8, false, 1);
                    submit(new byte[ ] { 0, 0x3B /* doesn't matter */ }, stream);
                    stream.readBytes(buf, 0, 7);
        printIn(buf);
        submit(new byte[ ] { 0, 0x3B /* doesn't matter */ }, stream);
        stream.readBytes(buf, 0, 7);
             printIn(buf);
       submit(new byte[ ] { 1, 0x3B /* doesn't matter */ }, stream);
        stream.readBytes(buf, 0, 7);
            printIn(buf);
        submit(new byte[ ] { 2, 0x3B /* doesn't matter */ },stream);
        stream.readBytes(buf, 0, 7);
            printIn(buf);
        submit(new byte[ ] { 1, 0x3B /* doesn't matter */ },stream);
        submit(new byte[ ] { 90, 0x3B, 13, 0 },stream);
        submit(new byte[ ] { 90, 0x3B, 12, 0 },stream);
        submit(new byte[ ] { 90, 0x3B, 11, 0 },stream);
        submit(new byte[ ] { 90, 0x3B, 10, 0 },stream);
        submit(new byte[ ] { 90, 0x3B, 8, 0 },stream);

//        timer = addTimer(100);
//        timertick = 0;
        }
    
    public Timer timer;
    int timertick;
    byte[] buf = new byte[7];
    
  public void onEvent(Event event)
    {
    if (event.type == ControlEvent.TIMER)
        {
        timertick++;
        switch (timertick)
        {
            case 1:
                    submit(new byte[ ] { 0, 0x3B /* doesn't matter */ }, stream);
                    break;
             case 2:
                    stream.readBytes(buf, 0, 7);
        printIn(buf);
                    break;
            case 3:
        submit(new byte[ ] { 1, 0x3B /* doesn't matter */ }, stream);
                    break;
            case 4:
        stream.readBytes(buf, 0, 7);
            printIn(buf);
                    break;
            case 5:
        submit(new byte[ ] { 2, 0x3B /* doesn't matter */ },stream);
                    break;
            case 6:
        stream.readBytes(buf, 0, 7);
            printIn(buf);
                    break;
            case 7:
        submit(new byte[ ] { 1, 0x3B /* doesn't matter */ },stream);
                    break;
            case 8:
        submit(new byte[ ] { 90, 0x3B, 13, 0 },stream);
                    break;
            case 9:
        submit(new byte[ ] { 90, 0x3B, 12, 0 },stream);
                    break;
            case 10:
        submit(new byte[ ] { 90, 0x3B, 11, 0 },stream);
                    break;
            case 11:
        submit(new byte[ ] { 90, 0x3B, 10, 0 },stream);
                    break;
            case 12:
        submit(new byte[ ] { 90, 0x3B, 8, 0 },stream);
                    break;
       }
        }
    }
    
    }
