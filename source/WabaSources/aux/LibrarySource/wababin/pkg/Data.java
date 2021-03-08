package wababin.pkg;

import java.io.*;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class Data extends NewtonObject {
  //protected Data() {}

  public int objSize() {  // overridden in actual classes.
System.err.println("ERROR: Data.objSize !");
    return -1;
  }
  public int chunkSize() {
    int len = objSize(), pad = len % CHUNKSIZE;
    if (pad == 0)
      return len;
    return len + CHUNKSIZE - pad;
  }

  // overridden by Binary, Symbol, String to add class etc.
  public void stuffHeader(byte[] data, int offset) {
    PkgUtils.StuffByte3(data, offset,   objSize());
    PkgUtils.StuffByte (data, offset+3, PkgUtils.DATA_TYPE);
    PkgUtils.StuffLong (data, offset+4, 0);  // filler
    // class, data in subclass
  }

  public void pad (DataOutputStream dos) throws IOException {
    int pad = objSize() % CHUNKSIZE;
    if (pad > 0)
      for (int i=0; i < CHUNKSIZE-pad; i++)
        dos.writeByte(0);
  }
  public void pad (byte[] data, int offset) {
    int pad = objSize() % CHUNKSIZE;
    if (pad > 0)
      for (int i=0; i < CHUNKSIZE-pad; i++)
        data[offset+i] = 0;
  }
}
