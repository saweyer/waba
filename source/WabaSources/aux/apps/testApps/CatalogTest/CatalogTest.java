import waba.ui.*;
import waba.io.*;
import newton.*;
import waba.sys.*;

/*
To compile:

jikes CatalogTest.java
java wababin.Warp c /f 4 CatalogTest
java wababin.Exegen /f 4 CatalogTest CatalogTest CatalogTest
open -a PackType CatalogTest.pkg
*/


public class CatalogTest extends MainWindow
    {
    Button addSomething;
    Button deleteFirst;
    Button printAll;
    
    public void print(String s)
        {
        Ref.call("GetRoot").send("Notify",
            new Ref(4 /*kNotifyQAlert*/),
            new Ref("Waba Debug"),
            new Ref(s));
        }
    
    byte[] stringToBytes(String s)
        {
        char[] chars = s.toCharArray();
        byte[] bytes = new byte[chars.length*2];
        for(int x=0;x<chars.length;x++)
            {
            short c = (short)chars[x];
            bytes[x*2] = bytes[x*2+1] = (byte)chars[x];
            //bytes[x*2] = (byte)((c & 0xFF00) >>> 2);
            //bytes[x*2+1] = (byte)(c & 0x00FF);
            }
        return bytes;
        }
    
    String bytesToString(byte[] bytes)
        {
        char[] chars = new char[bytes.length/2];
        for(int x=0;x<chars.length;x++)
            {
            short c = 0;
            //c += bytes[x*2] << 2;
            //c += bytes[x*2+1];
            c = (short)bytes[x*2];
            if (c==0) c=(short)'%';
            chars[x] = (char) c;
            }
        return new String(chars);
        }
        
    public void onEvent(Event event)
        {
        if (event.type == ControlEvent.PRESSED)
            {
            if (event.target == addSomething)
                {
                String s = Convert.toString(Vm.getTimeStamp());
                int len = s.length()*2;
                print("Adding #" + s + "# length " + s.length()*2);
                c.addRecord(s.length()*2);
                c.writeBytes(stringToBytes(s),0,s.length()*2);
                }
            else if (event.target == deleteFirst)
                {
                c.setRecordPos(0);
                c.deleteRecord();
                }
            else if (event.target == printAll)
                {
                for(int x=0;x<c.getRecordCount();x++)
                    {
                    c.setRecordPos(x);
                    byte b[] = new byte[c.getRecordSize()];
                    c.readBytes(b,0,b.length);
                    print("" + x + "(" + b.length + ") #" + bytesToString(b) + "#");
                    }
                }
            }
        }
    
    Catalog c;
    
    public void onStart()
        {
        addSomething = new Button("Add");
        addSomething.setRect(10,10,80,30);
        add(addSomething);
        deleteFirst = new Button("Delete");
        deleteFirst.setRect(10,30,80,60);
        add(deleteFirst);
        printAll = new Button("Print");
        printAll.setRect(10,60,80,90);
        add(printAll);
        
       c = new Catalog("MyCatalog", Catalog.CREATE);
        if (!c.isOpen())
            {
            print("Catalog not open!");
            return;
            }
        }
    }