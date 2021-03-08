package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class Symbol extends Data {
  public static final int HEADERSIZE = 16;
  public static final String SYMBOLCLASS = "_SyMbOl_";
  public static final String NILOBJECT   = "_NiL_";
  public static final String TRUEOBJECT  = "_TrUe_";

  String ref;
  boolean vquote = false;

  public Symbol(String sym) {
    //offset = off;
    ref = sym;
    char ch;
    for (int i=0; i < ref.length(); i++) {
      ch = ref.charAt(i);
      if (! (Character.isLetter(ch) || ch == '_' || (i > 0 && Character.isDigit(ch)))) {
        vquote = true;
        break;
      };
    };
  }
  public int objSize() {
    return HEADERSIZE + ref.length() + 1; // +null
  }

  public void stuffHeader(byte[] data, int offset) {
    super.stuffHeader  (data, offset);
    PkgUtils.StuffLong (data, offset+ 8, PkgUtils.SYMBOL_REF); // class
    PkgUtils.StuffLong (data, offset+12, getHash());
  }
  public void stuff(byte[] data, int offset) {
    stuffHeader(data, offset);
    PkgUtils.StuffString(data, offset+HEADERSIZE, ref, true);
  }

  public int getHash() { // Formats.pdf doc.  need an UNsigned int -- so use long for now
    int val = 0;
    for (int i=0; i < ref.length(); i++)
      val += (byte) Character.toUpperCase(ref.charAt(i));
    return (int) (val * 2654435769L);
   }

  public String getSymbol() {
    return ref;
  }
  public boolean IsSymbol() {
    return ref.equals(SYMBOLCLASS);
  }
/*
  public boolean IsNil() {
    return ref.equals(NILOBJECT);
  }
  public boolean IsTrue() {
    return ref.equals(TRUEOBJECT);
  }
  public boolean equals(String sym) {
    return ref.equalsIgnoreCase(sym);
  }
*/
  public boolean equals(Symbol sym) {
    return ref.equalsIgnoreCase(sym.getSymbol());
  }
  public String toString(int level) {
    if (ref.equals(NILOBJECT)) //(IsNil())
      return "nil"; // no '
    if (IsSymbol())
      return "<symbol>";
    if (ref.equals(TRUEOBJECT)) // (IsTrue())
      return "true";  // no '
    if (level == 0)
      return ref;
    if (vquote)
      return "'|" + ref + "|";
    return "'" + ref;
  }
}
