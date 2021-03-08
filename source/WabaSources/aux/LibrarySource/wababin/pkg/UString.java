package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class UString extends Data {
  String ref;
  int itmp;

  public UString (String str, int i) {
    ref = str;
    itmp = i;
  }
  public int objSize() {  // unicode, +null bytes
    return HEADERSIZE + (ref.length()+1)*2;
  }

  public void stuffHeader (byte[] data, int offset) {
    super.stuffHeader  (data, offset);
    PkgUtils.StuffLong (data, offset+8, PkgUtils.objRef(PkgUtils.stringSymbol[itmp])); // class
  }

  public void stuff(byte[] data, int offset) {
    stuffHeader(data, offset);
    PkgUtils.StuffUniString(data, offset+HEADERSIZE, ref, true);
  }
/*
  public String getString() {
    return ref;
  }
*/
  public String toString(int level) {
    return '"' + ref + '"';   // ?? \ quote embedded "
  }
}
