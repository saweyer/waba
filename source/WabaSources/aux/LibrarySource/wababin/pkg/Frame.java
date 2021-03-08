package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class Frame extends NewtonObject {
  Object[] slots; // ideally Symbol[] but would need to change Array creation
  Object[] vals;
  int vlen;

  public Frame () {}

  public Frame (Object[] s, Object[] v) {
    //offset = off;
    slots = s;
    vals = v;
    vlen = vals.length;
  }
  public Frame (int len) {
    vlen = len;
  }
  public int objSize() {
    return HEADERSIZE + (vlen * 4);
  }
  public void stuffHeader(byte[] data, int offset, int cl, int size) {
    PkgUtils.StuffByte3(data, offset,   size);  // usually objSize except Bitmap
    PkgUtils.StuffByte (data, offset+3, PkgUtils.FRAME_TYPE);
    PkgUtils.StuffLong (data, offset+4, 0);  // filler
    PkgUtils.StuffLong (data, offset+8, cl);  // ptr to frameMap
  }

  public void stuffHeader(byte[] data, int offset, int cl) {
    stuffHeader(data, offset, cl, objSize());
  }

  public void stuffVal(byte[] data, int offset, int i, int val) {
     if (i >= 0 && i < vlen)
      PkgUtils.StuffLong(data, offset + HEADERSIZE + (i*4), val);
    else
      System.err.println("Frame index@" + offset + " [" + i + ']');
 }
  public int length() {
    return vlen;
  }
  public Object[] getSlots() {
    return slots;
  }
  public Object[] getVals() {
    return vals;
  }
  public String toString(int level) {
    StringBuffer sb = new StringBuffer(100);
    sb.append("{#");
    sb.append(vlen);
    if (level > 0) {
      Object val;
      String delim = ", ";
      if (level > 1) {
        delim = ",\n";
        sb.append('\n');
      }
      else
        sb.append(' ');
      for(int i=0; i < vlen; i++) {
        sb.append(getSlot(i));
        sb.append(':');
        if (level > 1) {
          val = vals[i];
          if (val instanceof String)
            val = new UString((String) val, -1);
          sb.append('\t');
          sb.append(
            (val instanceof NewtonObject)
            ? ((NewtonObject) val).toString(level-1)  // ?? indent as param
            : val.toString()  // Integer?
            );
        };
        sb.append(delim);
      };
    };
    sb.append('}');
    return sb.toString();
  }
 /*
  private Object getSlotMap(Object[] map, int pos, int offset) {
    int len = map.length-1;
    Object slot0 = map[0];  // Symbol(nil) or Array
    if (slot0.getClass() == Array.class) {
      Object obj = getSlotMap(((Array) slot0).toArray(), pos, offset); // Symbol or Integer(offset)
      if (obj.getClass() == Symbol.class)
        return obj;
      offset = ((Integer) obj).intValue();
    };
    if (pos-offset < len)
      return map[1+pos-offset];
    return new Integer(offset+len);
  }
*/
  public Symbol getSlot(int pos) {
    //return (Symbol) (getSlotMap(slots, pos, 0));
    int len = vlen, slen;
    Object[] curSlots = slots;
    while (pos >= 0) {
      slen = curSlots.length-1;
      if (len == slen)
        return (Symbol) (curSlots[1+pos]);
      curSlots = ((Array) (curSlots[0])).toArray();
      len-=slen;
      pos-=slen;
    };
  return null;
  }

  public Object getValue(String slot) {
    int pos = findSlot(new Symbol(slot));
    if (pos < 0)
      return null;
    return vals[pos];
  }
  public Object getValue(int pos) {
    return vals[pos];
  }

  public int findSlot(Symbol slot) {
    Object[] curSlots = slots;
    int pos = vlen-1;
    while (true) {
      for (int i=curSlots.length-1; i>=1; i--)
        if (slot.equals((Symbol) curSlots[i]))
          return pos;
        else pos--;
      if (curSlots[0] instanceof Array)
        curSlots = ((Array) (curSlots[0])).toArray();
      else break;
    };
    return -1;
  }
}
