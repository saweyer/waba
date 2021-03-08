package wababin.pkg;

// import java.io.*; // for pad?

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
**/
public class NewtonObject {
  //int offset;
  //public int getOffset() {return offset;}

  protected NewtonObject() {}

  public static final int HEADERSIZE = 12;
  public static final int CHUNKSIZE  =  4; // for padding

    public int objSize() {  // overridden in actual classes.
System.err.println("ERROR: NewtonObject.objSize !");
    return -1;
  }
  public int chunkSize() {  // default for Array, Frame
    return objSize();
  }
  public String toString() {
    return toString(0);
  }
  public String toString(int level) {
    return "<N.O.>??";
  }

//public void pad (DataOutputStream dos) throws IOException {} // only implemented by Data
//public void pad (byte[] data, int offset) {} // only implemented by Data

  // note: special handling needed for Integer, String when toString(level) called
  // see debugVal
}