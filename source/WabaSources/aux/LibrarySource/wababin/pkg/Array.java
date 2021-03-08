package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class Array extends NewtonObject {
  Object cl;
  Object[] vals;
  int vlen;

  public Array (Object[] objs, Object acl) {
    //offset = off;
    vals = objs;
    vlen = vals.length;
    cl = acl;
  }
  public Array (int len) {
    vlen = len;
  }
  public int objSize() {
    return HEADERSIZE + (vlen * 4);
  }

  public void stuffHeader(byte[] data, int offset, int cl) {
    PkgUtils.StuffByte3(data, offset,   objSize());
    PkgUtils.StuffByte (data, offset+3, PkgUtils.ARRAY_TYPE);
    PkgUtils.StuffLong (data, offset+4, 0);  // filler
    PkgUtils.StuffLong (data, offset+8, cl);
  }
  public void stuffVal(byte[] data, int offset, int i, int val) {
    if (i >= 0 && i < vlen)
      PkgUtils.StuffLong(data, offset + HEADERSIZE + (i*4), val);
    else
      System.err.println("Araay index@" + offset + " [" + i + ']');
  }
  public String toString(int level) {
    Object val;
    StringBuffer sb = new StringBuffer(50);
    sb.append('[');
    sb.append(cl);
    sb.append(": #");
    sb.append(vlen);
    if (level > 1) {
      sb.append('\n');
      for (int i=0; i < vlen; i++) {
        val = vals[i];
        if (val instanceof String)
          val = new UString((String) val, -1);
        sb.append('\t');
        sb.append(
          (val instanceof NewtonObject)
          ? ((NewtonObject) val).toString(level-1)  // ?? indent as param
          : val.toString()  // Integer?
          );
        sb.append('\n');
      };
    };
    sb.append(']');
    return sb.toString();
  }
  public Object[] toArray() {
    return vals;
  }
  public int findSym(Symbol sym) {
    for (int i=0; i < vlen; i++) {
      Object obj = vals[i];
      if (obj instanceof Symbol && sym.equals((Symbol) obj))
        return i;
    };
    return -1;
  }
}
