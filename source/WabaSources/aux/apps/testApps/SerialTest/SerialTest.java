//import waba.fx.*;     // Graphics
// import waba.io.*;
// import waba.sys.*;    // Convert, Vm
// import waba.ui.*;
// import waba.lang.*;
//import waba.util.*;   // Vector

import waba.sys.*;
import waba.ui.*;
import waba.io.*;
import extra.ui.*;

/**
 * @author <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>
 * Copyright (c) 2001, S. Weyer. All right reserved.
 * @version waba 1.0
 * !!!a ?? Mar 2001
 * <A HREF="http://www.kagi.com/weyer/#Waba">Steve's other Waba apps</A>
 *
 * @modified by <a href="mailto:pguyot@kallisys.net">Paul Guyot</a>
**/

public class SerialTest extends MainWindow {
  private static final boolean DEBUG = false;

  private static final int COMPHT  = 15;
  private static final int COMPWID = 38;
  private static final int COMPSEP = 2;

  private Button openButton;
  private Button readButton;
  private Button writeButton;
  //private Button closeButton;
  private Label readText;
  private Edit writeText;
  private List portList;
  private List speedList;
  private List numBytesList;
  private SerialPort _serial = null;

  public SerialTest() {
    int objX = 0, objY = 0, objWid = COMPWID+5, listWid = COMPWID;

    Title title = new Title("SerialTest");
    title.setRect(objX, objY, this.width, COMPHT);
    add(title);

    objX = 80;
    Label lbl = new Label("weyer@kagi.com");
    lbl.setRect(objX,objY,this.width-objX,COMPHT);
    add(lbl);

    objX = 0;
    objY += COMPHT + COMPSEP;
    openButton = new Button("Open");
    openButton.setRect(objX, objY, objWid, COMPHT);
    add(openButton);

    objX += objWid + COMPSEP;
    portList = new List(new String[]{"0","1","2","3"});
    portList.setRect(objX,objY,listWid-5,COMPHT);
    add(portList);

    objX += listWid-5 + COMPSEP;
    speedList = new List(new String[]{"9600","57600"});
    speedList.setRect(objX,objY,listWid,COMPHT);
    add(speedList);

    objX += listWid + COMPSEP;
    numBytesList = new List(new String[]{"5","10","20"});
    numBytesList.setRect(objX,objY,listWid,COMPHT);
    add(numBytesList);

    objX = 0;
    objY += COMPHT + COMPSEP;
    readButton = new Button("Read: 5");
    readButton.setRect(objX,objY,objWid,COMPHT);
    add(readButton);

    objX += objWid + COMPSEP;
    readText = new Label("chars read");
    readText.setRect(objX,objY,this.width-objX,COMPHT);
    add(readText);

    objX = 0;
    objY += COMPHT + COMPSEP;
    writeButton = new Button("Write");
    writeButton.setRect(objX,objY,objWid,COMPHT);
    add(writeButton);

    objX += objWid + COMPSEP;
    writeText = new Edit();
    writeText.setText("chars to write");
    writeText.setRect(objX,objY,this.width-objX,COMPHT);
    add(writeText);
/*
    objX = 0;
    objY += COMPHT + COMPSEP;
    closeButton = new Button("Close");
    closeButton.setRect(objX,objY,objWid,COMPHT);
    add(closeButton);
*/
  }

  public void onEvent(Event ev) {
    Object obj = ev.target;
    int evType = ev.type;
    if (evType == ControlEvent.PRESSED) {
      if (obj == openButton) // open/close
        if (_serial == null) {
          _serial = new SerialPort(portList.getSelectedIndex(), Convert.toInt(speedList.getSelected()));
          if (_serial.isOpen())
            openButton.setText("Close");
          else
            _serial = null;
        }
        else {
          _serial.close();
          _serial = null;
          openButton.setText("Open");
        }
      else if (obj == readButton && _serial != null) {
        int count = Convert.toInt(numBytesList.getSelected());
        byte[] buf = new byte[count];
        count = _serial.readBytes(buf,0,count);
        char[] chs = new char[count];
		// I don't use copy array, but I try a for loop.
		int i;
		for (i = 0; i < count; i++)
		{
			chs[i] = (char) buf[i];
		}
//		Vm.copyArray(buf,0,chs,0,count);
        readText.setText(new String(chs,0,count));
      }
      else if (obj == writeButton && _serial != null) {
        char[] chs = writeText.getText().toCharArray();
        int count = chs.length;
        byte[] buf = new byte[count];
		// I don't use copy array, but I try a for loop.
		int i;
		for (i = 0; i < count; i++)
		{
			buf[i] = (byte) chs[i];
		}
//		Vm.copyArray(chs,0,buf,0,count);
        _serial.writeBytes(buf,0,count);
      }
      else if (obj == numBytesList)
        readButton.setText("Read: " + numBytesList.getSelected());
    }
  }
}
