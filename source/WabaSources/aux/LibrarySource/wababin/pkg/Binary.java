package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class Binary extends Data {
  // should clean this up eventually
  byte[] b = null;
  Symbol cl = null;
  int blen;
  int icl;

  public Binary(byte[] bb, Symbol dcl) {  // used by extractChunk
    b = bb;
    blen = bb.length;
    cl = dcl;
  }
  public Binary(byte[] bb, int dcl) {  // used by Bitmap
    b = bb;
    blen = bb.length;
    icl = dcl;
  }
  public Binary(int len, int dcl) { // used by PkgFile.stuff...
    blen = len;
    icl = dcl;
  }
  public int objSize() {
    return HEADERSIZE + blen;
  }
  public String toString(int level) {
    return "<" + cl.toString() + ": #" + blen + ">";
  }
  public void stuffHeader (byte[] data, int offset) {
    super.stuffHeader  (data, offset);
    PkgUtils.StuffLong (data, offset+8, icl);   // class
  }
  public void stuff(byte[] data, int offset) { // used only by Bitmap for bounds and data?
    stuffHeader(data, offset);
    PkgUtils.StuffBytes(data,offset+HEADERSIZE,blen, b,0);
    pad(data, blen);  // shouldn't be necessary
  }
}
