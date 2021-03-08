package wababin.pkg;

//import java.io.*; // UnsupportedEncodingException, DataOutputStream
import java.util.*; // Vector, Date?
import java.text.*;   // SimpleDateFormat, ParseException
import wababin.*;     // Warp.exit, PkgFile.argAttribs

/**
 * used by PkgFile via Warp/Exegen to read, check, modify a Newton .pkg
 *
 * @author     <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version    1.50.0N
 *
 * !!!Nc 10 Feb 2001 first version
 * !!!Nf 08 May 2001
 * ?? 1.2 Vector.add, .get instead of .addElement, .elementAt
**/
public class PkgUtils  {
  public static final boolean DEBUG = false;

  public static final int ARRAY_TYPE = 0x41;
  public static final int FRAME_TYPE = 0x43;
  public static final int DATA_TYPE  = 0x40;
  public static final int NIL_REF    =  0x2;
  public static final int TRUE_REF   = 0x1A;
  public static final int SYMBOL_REF = 0x55552;

  // these hardcoded values SPECIAL CONSTANTS are derived via extractPackagede
  // *** values are replaced, ** modified in some way
  // HEADER
//public static final int copyLen1Offset  =   22; // (word)
  public static final int copyLen1        =  114; // overwrite with blanks (don't change len)
  public static final int pkgNameOffset   =   24; // (word)
  public static final int pkgName         =  198; // E*** copyOff1+copyLen1
  public static final int pkgNameLenOffset=   26; // unicode; includes null
  public static final int pkgNameLen      =  128; // E*** unicode; includes null
  public static final int createTimeOffset=   32; // E*** secs since 1/4/1904
  public static final String[] createTime = {"28 Jan 2001  8:27:00", // from Newton:PkgInfo
                                              "1 Feb 2001 19:30:00"};
  public static final long[] createTimeMS = {980688420000L,
                                             981073800000L}; // corresponds to createTime
  public static final int hdrLenOffset    =   44;
  public static final int hdrLen          =  408; // no change
  public static final int partLenOffset   =   56; // dup@60
  public static final int[] partLen       = {5060, 2268}; // W,E***
  public static final int pkgLenOffset    =   28;
  public static final int[] pkgLen        = {hdrLen+partLen[0], hdrLen+partLen[1]}; // W,E***

  public static final int copyOff1        =   84; // + [20: 0]
  public static final int copyOff2        =  326; // *** pkgName + pkgNameLen
  public static final int copyLen2        =   76; // *** overwrite with blanks (don't change len)

  // PART
  public static final int partFrame       =  424; // partFrame (same for app/lib)
  public static final int appSlot         =    0; // E***  pkgSymbol
  public static final int[] textSlot      =  {1, 4}; // E***  appName
//public static final int iconSlot        =    2; // see bitsData

//public static final int appSymbol       =  ???; // see partFrame.app(pkgSymbol), theForm.appSymbol, appArgs.uniqueSymbol, (DeletionScript)
//public static final int appNameStr      =  ???; // see partFrame.text, theFormFrame.appMainName&appName, appArgs.title
  public static final int[] stringSymbol  =  {736,1352}; // W 'string
//public static final int iconFrame       =  ???; // bitsSlot=0. see bitsData
  public static final int[] bitsData      =  {852, 2384}; // E**  lib not used but incl for checking

  public static final int[] theFormFrame  = {1144, 1784}; // appContext etc. in theForm/partData
  public static final int[] appContextSlot=   {12, 1}; // W*** 12th slot in theFormFrame; ptr=4536(old appContext frame)
  public static final int appMainNameSlot =   14; // E***
  public static final int[] appNameSlot   =   {15, 3}; // E***
//public static final int appRequiresSlot =   16; // see appRequiresArray
  public static final int appSymbolSlot   =   17; // E***

  // appArgs: width, height, vmStackSize, nmStackSize, classHeapSize, objectHeapSize, uniqueSymbol, title
  public static final int appArgsFrame    = 4156; // E**
  public static final int uniqueSymbolSlot=    6; // E**  appSymbol
  public static final int titleSlot       =    7; // E**

//public static final int appSymbol       = ????; // see theFormFrame.appSymbol
  public static final int[] bitmapFrameMap= {4508, 2020}; // W [class, bounds,...]
  public static final int[] bitmapSymbol  = {4620, 2156}; // W 'bitmap
  public static final int[] boundsrectSymbol= {4644, 2180}; // W 'boundsrect
  public static final int[] pixelsSymbol  = {4672, 2208}; // W 'pixels
  public static final int[] javaSymbol    = {4696, 2232}; // W 'java
//public static final int appMainName     = ????; // see theForm.appMainName
  public static final int wabaSymbol = 2648;
  public static final int wextraSymbol = 4788;
  public static final int appRequiresArray= 4760; // W** '[waba, wextra, ...]  theFormFrame.appRequires
  public static final int appRequiresLen  = 4;    // apptemplate can hold up to 4 currently
// !!!Nf. moved to Warp
//  public static final String[] appReqPkgs = {"waba",  "wextra"};
//  public static final String[] appReqDirs = {"waba/", "extra/"};

  public static final int dscriptArray    = 5424; // DeletionScript literals: slot 0 (appSymbol)


  byte[] tmpData; // _apptemplate.pkg contents. need to be careful since this signed!
  int itmp;
  boolean lib;

  public PkgUtils(byte[] data, boolean libflag) {
    tmpData = data;
    itmp = (lib = libflag) ? 1 : 0;
  }
  public byte[] getData() {
    return tmpData;
  }

  // do this as some kind of hashtable?
  Vector pkgOffs;
  Vector pkgObjs;

  public Object getObject (Integer offset) {
    int pos = pkgOffs.indexOf(offset);
    if (pos >= 0) {
      Object obj = pkgObjs.elementAt(pos);
//if (DEBUG) debugVal(offset, "found("+pos+") " + obj);
      return obj;
    };
    return null;
  }
  public void saveObject(Integer offset, Object obj) {
    // if size gets too large, omit 'literals and 'instructions
//if (DEBUG) debugVal(offset, obj);
    pkgOffs.addElement(offset);
    pkgObjs.addElement(obj);
  }

  // these 'debug' functions would be much simpler/generic
  // but evidently it uses Object.equals rather than class-specific w/o cast
  // these are needed for list option (not removed w/ DEBUG)
  public int debugFrame(String str, int level) {
    Symbol sym = new Symbol(str);
     for (int i=0; i < pkgObjs.size(); i++) {
      Object obj = pkgObjs.elementAt(i);
      if (obj instanceof Frame &&   // obj.getClass() == Frame.class
        ((Frame) obj).findSlot(sym) >= 0)
          return debugVal(pkgOffs.elementAt(i), obj, level);
     };
   System.err.println("frame not found with: " + str);
   return -1;
  }
  public int debugArray(String str, int level) {
    Symbol sym = new Symbol(str);
    for (int i=0; i < pkgObjs.size(); i++) {
      Object obj = pkgObjs.elementAt(i);
      if (obj instanceof Array &&
        ((Array) obj).findSym(sym) >= 0)
        return debugVal(pkgOffs.elementAt(i), obj, level);
    };
    System.err.println("array not found with: " + str);
    return -1;
  }
  public int debugObj(Object obj) {
    int i = pkgObjs.indexOf(obj);
    if (i >= 0)
      return debugVal(pkgOffs.elementAt(i), obj, 1);
    System.err.println("obj not found: " + obj);
    return -1;
  }
  public int debugObj(int offset, int level) {
    int i = pkgOffs.indexOf(new Integer(offset));
    if (i >= 0)
      return debugVal(pkgOffs.elementAt(i), pkgObjs.elementAt(i), level);
   System.err.println("obj@offset not found: " + offset);
   return -1;
  }
  public int debugSym(String str) {
    Symbol sym = new Symbol(str);
    for (int i=0; i< pkgObjs.size(); i++) {
      Object obj = pkgObjs.elementAt(i);
      if (obj instanceof Symbol &&
        sym.equals((Symbol) obj))
        return debugVal(pkgOffs.elementAt(i), obj, 1);
     };
    System.err.println("sym not found: " + str);
    return -1;
  }
  public void debugAll() {
    int len = pkgOffs.size();
    System.out.println("\nSAVED OBJECTS: #" + len);
    Object obj;
    for (int i=0; i<len; i++)
      debugVal(pkgOffs.elementAt(i), pkgObjs.elementAt(i));
  }
  public static void debugVal(int offset, String name, int val) {
      System.out.println("[" + offset + "]:\t" + name + ": " + val);
  }
  public static void debugVal(int offset, String name, String val, int len) {
      System.out.println("[" + offset + "]:\t" + name + ": " + val +
        " (=" + len + ')');
  }
  public static int debugVal(Object offset, Object val) {
    return debugVal(offset, val, 1);
  }

  public static int debugVal(Object offset, Object val, int level) {
    if (val instanceof String)
      val = new UString((String) val, -1);
    System.out.println("[" + offset + "]:\t" +
      ((val instanceof NewtonObject) // except Integer, String  ?NewtonObject.class.isInstance(val)
      ? ((NewtonObject) val).toString(level)
      : val.toString()
      ));
    return ((Integer) offset).intValue();
  }
  public void assert(int offset, String s, int a, int b) {
    if (a != b)
      System.err.println("ASSERT: " + s + " [" + offset + "]: " + a + ' ' + b);
  }

  public Object[] extractPackage(boolean extract) { // returns [appArgs, String]
    int offset = 0, len, val;

    debugVal(offset, "PACKAGE HEADER. lib: " + lib, 0);

if (DEBUG) assert(20, "copyOff1", 0, ExtractWord(tmpData,20));
    //copyOff1 = 84. typically 'developer' copyright.
    val = ExtractWord(tmpData, offset = 22);    // 22. total length(dev+tool) copyright strings
    debugVal(offset, "copyLen1", val);
if (DEBUG) assert(offset, "copyLen1", copyLen1, val);

    val = ExtractWord(tmpData, offset = pkgNameOffset);// 24
    debugVal(offset, "pkgName", val);
if (DEBUG) assert(offset, "pkgNameOffset", pkgName, val+copyOff1);

    val = ExtractWord(tmpData, offset = pkgNameLenOffset);// 26
    debugVal(offset, "pkgNameLen", val);
if (DEBUG) assert(offset, "pkgNameLen", pkgNameLen, val);

    int thisPkgLen = ExtractLong(tmpData, offset = pkgLenOffset);      // 28
    debugVal(offset, "pkgLen", thisPkgLen);
if (DEBUG) assert(offset, "pkgLen", pkgLen[itmp], thisPkgLen);

    // 32(creation), 36(modified), 40(pad=0)
//  val = ExtractLong(tmpData, offset = createTimeOffset);  // 32
//if (DEBUG) debugVal(offset, "createTimeSecs", createTimeSecs);
    SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy h:mm:ss");
    try {
      long ms = df.parse(createTime[0]).getTime();  // date string for apptemplate__.pkg
      System.out.println("createTimeMS[0]: " + ms);
      ms = df.parse(createTime[1]).getTime();  // date string for apptemplate__.pkg
      System.out.println("createTimeMS[1]: " + ms);
    }
    catch (ParseException ex) {
      System.err.println("Date parse err: " + createTime);
    };

    int thisHdrLen = ExtractLong(tmpData, offset = hdrLenOffset);   // 44
    debugVal(offset, "hdrLen", thisHdrLen);
if (DEBUG) assert(offset, "hdrLen", hdrLen, thisHdrLen);

if (DEBUG) {
    val = ExtractLong(tmpData, offset = 48);    // 48
    debugVal(offset, "numParts", val);
if (DEBUG) assert(offset, "numParts", 1, val);

    debugVal(offset, "PART HEADER", 0);
    val = ExtractLong(tmpData,offset = 52);   // 52
    debugVal(offset, "partOffset", val);
if (DEBUG) assert(offset, "partOffset", 0, val);
    };

    val = ExtractLong(tmpData, offset = partLenOffset);     // 56
    debugVal(offset, "partLen", val);
if (DEBUG) assert(offset, "partLen",  partLen[itmp], val);
if (DEBUG) assert(offset, "partLen2", partLen[itmp], ExtractLong(tmpData, offset+4));
    // 60(partlen2),64(form),68(pad=0), 72(val81)

    // offset = copyOff1+copyOff2
    // this is either 4 char code(form) or 2nd tool copyright[NTK]
    int partSymOff = ExtractWord(tmpData, offset = 76);  // 76
    debugVal(offset, "partSymOff", partSymOff);

    int partSymLen = ExtractWord(tmpData, offset = 78);  // 78
    debugVal(offset, "partSymLen", partSymLen);
if (DEBUG) assert(offset, "partSymLen", partSymLen, 4);
    // 80(pad=0)

//if (DEBUG) debugVal(offset, "VAR STRING SECTION", 0);
// assumes 1 part !!!
    String copyStr1 = ExtractUniString(tmpData, offset = copyOff1, copyLen1);    // 84. developer copyright
    len = copyStr1.length() + 1;
    debugVal(offset, "copyStr1", copyStr1, len);
    offset+=(len*2);

    String pkgNameS = ExtractUniString(tmpData, offset, pkgNameLen);
    debugVal(offset, "pkgName", pkgNameS, pkgNameLen / 2);
    offset+=(pkgNameS.length()+1)*2;

    // tool copyright?
    String copyStr2 = ExtractCString(tmpData, offset, hdrLen-offset);
    debugVal(offset, "copyStr2", copyStr2, copyStr2.length());

    // 2nd? tool copyright or 4-char code (form, auto...) or
    String partSym = ExtractString(tmpData, offset = copyOff1+partSymOff, partSymLen);
    debugVal(offset, "partSym", partSym, partSymLen);

    pkgOffs = new Vector(100);
    pkgObjs = new Vector(100);

    extractChunk(tmpData, offset = thisHdrLen);

if (DEBUG) debugAll();  // list all pkgOffs, pkgObjs


    System.out.println("\nSPECIAL CONSTANTS");
    // this info should have printed out, but this consolidates
    // for updating/verifying
    offset = debugFrame("icon", 1); // partFrame
if (DEBUG) assert(offset, "partFrame", offset, partFrame);

//    debugArray("app", 2);  // 'app, 'text,
    offset = debugSym("string");       // "..."
if (DEBUG) assert(offset, "string", offset, stringSymbol[itmp]);
    offset = debugFrame("bits", 1);       // partFrame.icon
    if (offset > 0) { // binary obj
      offset = ExtractLong(tmpData, offset+Frame.HEADERSIZE) - 1;
      debugObj(offset, 1);
      if (DEBUG) assert(offset, "bits", offset, bitsData[itmp]);
    };

    offset = debugFrame("appContext", 1); // theForm/partData
if (DEBUG) assert(offset, "theFormFrame", offset, theFormFrame[itmp]);

    Frame theForm = (Frame) getObject(new Integer(offset));
    Frame appContext = (Frame) (theForm.getValue(appContextSlot[itmp]));
    Object[] argsAndMain = null;

    if (! lib) {
      offset = debugFrame("vmStackSize", 2);// appArgs
if (DEBUG) assert(offset, "appArgs", offset, appArgsFrame);
      argsAndMain = new Object[2];
      argsAndMain[0] = getObject(new Integer(offset));    // Frame
      argsAndMain[1] = theForm.getValue(appMainNameSlot); // String
    };

    offset = debugArray("colordata", 2);  // bitmap(framemap)
if (DEBUG) assert(offset, "bitmapFrameMap", offset, bitmapFrameMap[itmp]);
    offset = debugSym("bitmap");       // 'bitmap
if (DEBUG) assert(offset, "'bitmap", offset, bitmapSymbol[itmp]);
    offset = debugSym("boundsrect");   // 'boundsrect
if (DEBUG) assert(offset, "'boundsrect", offset, boundsrectSymbol[itmp]);
    offset = debugSym("pixels");       // 'pixels
if (DEBUG) assert(offset, "'pixels", offset, pixelsSymbol[itmp]);
    offset = debugSym("java");         // 'java
if (DEBUG) assert(offset, "'java", offset, javaSymbol[itmp]);

    if (! lib) {
    offset = debugSym("waba");
if (DEBUG) assert(offset, "waba", offset, wabaSymbol);
    offset = debugSym("wextra");
if (DEBUG) assert(offset, "wextra", offset, wextraSymbol);
    //debugArray("wextra", 2);     // appRequires
    offset = debugObj(appRequiresArray, 2);    // appRequires: []
if (DEBUG) assert(offset, "appRequires", offset, appRequiresArray);
    offset = debugArray("aM:waba", 2);     // DeletionScript.literals
if (DEBUG) assert(offset, "dscriptArray", offset, dscriptArray);
    };

    // testing
if (DEBUG) {
    System.out.println("\nMORE TESTS");
    if (! lib) {
      debugArray("wextra", 2);     // appRequires
      debugSym("HelloWorld");               // symbol in appContext. not in apptemplate
      debugArray("HelloWorld", 2);          // framemap. not in template
      debugObj("HelloWorld");               // title. not in apptemplate or Warp
      debugFrame("HelloWorld", 2); // frame in appContext. not in apptemplate
    };
    };
  return argsAndMain;
  }

  public static int objRef(int offset) {
    return offset+1;
  }
  public static int intRef(int val) {
    return val << 2;
  }
  public Object extractVPUM (byte[] data, int offset) {
    int ref = ExtractLong(data, offset);
//if (DEBUG) debugVal(offset, "VPUM", "0x" + Integer.toHexString(ref), ref);
    int bits2 = ref & 0x3;

    if (bits2 == 0) {
// if (DEBUG) debugVal(offset, "Integer", ref >> 2);
      return new Integer (ref >>> 2);  // INTEGER
    };
    if (bits2 == 1)
      /*if (ref > pkgLen) // e.g., reprocessing an appended template (appContext)
        return new Symbol(Symbol.NILOBJECT);
      else*/ return extractChunk(data, ref-1);    // Binary(Symbol,String,Data), Array, Frame

    if (bits2 == 3) {
//if (DEBUG) debugVal(offset, "MagicPtr", ref >>> 2);
      return new MagicPtr(ref >>> 2); // MAGIC PTR
    };

    // immediate
    if (ref == NIL_REF) {
//if (DEBUG) debugVal(offset, "nil", 0);
      return new Symbol(Symbol.NILOBJECT);  // rather than null or false
    };
    if (ref == TRUE_REF) {
//if (DEBUG) debugVal(offset, "true", 0);
      return new Symbol(Symbol.TRUEOBJECT);   // rather than Boolean
    };
    if (ref == SYMBOL_REF) {
//if (DEBUG) debugVal(offset, "SYMBOL", 0);
      return new Symbol(Symbol.SYMBOLCLASS);
    };
    if ((ref & 0xF) == 0xA) {
//if (DEBUG) debugVal(offset, "Char", ref >>> 4);
      return new Character((char) (ref >>> 4));
    };
//if (DEBUG) debugVal(offset, "VPUM: OTHER??", "0x" + Integer.toHexString(ref), ref);
    // ?? weak array, weird immediate
    return new OtherImmed(ref);
  }

  public Object extractChunk(byte[] data, int offset) {
    Integer origOffset = new Integer(offset);
    Object obj = getObject(origOffset);
    if (obj != null)
      return obj;

    int len   = ExtractByte3(data, offset);
    int ctype = ExtractByte (data, offset+3), i;
    String ctypeStr = "";
    if (ctype == DATA_TYPE)
      ctypeStr = "DATA";
    else if (ctype == ARRAY_TYPE)
      ctypeStr = "ARRAY";
    else if (ctype == FRAME_TYPE)
      ctypeStr = "FRAME";
    else { // unrecog object
      debugVal(offset, "ctype? " + ctype, "len?", len );
if (DEBUG) debugAll();
      Warp.errExit("extractChunk", -1);
    };

//if (DEBUG) debugVal(offset, "chunk", ctypeStr, len);
    // len/ctype, pad
    offset+=8;
    //int iarg = ExtractLong(data, offset);
    Object arg = extractVPUM(data, offset);
    Symbol objClass;
    offset+=4;
    len-=12;

    if (ctype == DATA_TYPE) {  // DATA
      objClass = (Symbol) arg;
      if (objClass.IsSymbol()) { // SYMBOL
        //int curHash = ExtractLong(data, offset);
        offset+=4; // skip hash
        len-=4;
        String sym = ExtractCString(data, offset, len);
//if (DEBUG) debugVal(origOffset.intValue(), "Symbol", "'" + sym, sym.length());
        obj = new Symbol(sym);
        //int myHash = ((Symbol) obj).getHash();
//if (DEBUG) System.out.println(sym +"; pkghash: " + curHash + "; calc: " + myHash + "; same: " + (curHash==myHash));
      }
      else if (objClass.equals(new Symbol("string"))) { // STRING
        String str = ExtractUniString(data, offset, len); // UString except for debugObj??
//if (DEBUG) debugVal(origOffset.intValue(), "String", "\"" + str + "\"", str.length());
        obj = str;
      }
      else {
//if (DEBUG) debugVal(origOffset.intValue(), "Binary", objClass.getSymbol(), len);
        obj = new Binary(ExtractBytes(data, offset, len), objClass);
      };
      saveObject(origOffset, obj);
    }

    else if (ctype == ARRAY_TYPE) {  // ARRAY
      //objClass = (Symbol) arg;  ?? =0 for 1st array
      int alen = len / 4;
//if (DEBUG) debugVal(origOffset.intValue(), "Array", arg.toString(), alen);
      Object[] vals = new Object[alen];
      obj = new Array(vals, arg);
      saveObject(origOffset, obj);
      for (i=0; i<alen; i++) {
//if (DEBUG) debugVal(offset, "Array[]", i);
        vals[i] = extractVPUM(data, offset);
        offset+=4;
      };
    }

    else if (ctype == FRAME_TYPE) {  // FRAME
      int alen = len / 4;
//if (DEBUG) debugVal(origOffset.intValue(), "Frame", arg.getClass().toString(), alen);
      Object[] slots = ((Array) arg).toArray();
//if (DEBUG) debugVal(offset, "#slots", slots.length);

      Object[] vals = new Object[alen];
      obj = new Frame(slots,vals); // ok to ignore array class?
      saveObject(origOffset, obj);
      for (i=0; i<alen; i++) {
// if (DEBUG) debugVal(offset, "Frame{}", ((Frame) obj).getSlot(i).toString(), i);
        vals[i] = extractVPUM(data,offset);
        offset+=4;
      };
    };
    return obj;
  }

  // 1 byte=8 bits. unsigned
  public static int ExtractByte (byte[] data, int offset) {
    return data[offset] & 0xFF;
  }
  public static void StuffByte (byte[] data, int offset, int val) {
    data[offset] = (byte) val;
  }

  // 2 bytes. Newton "word" = Java "short"
  public static int ExtractWord (byte[] data, int offset) {
    return
      ((data[offset]   & 0xFF) << 8) +
       (data[offset+1] & 0xFF);
  }
  public static int ExtractRWord (byte[] data, int offset) {
    return
      ((data[offset+1] & 0xFF) << 8) +
       (data[offset]   & 0xFF);
  }
  public static void StuffWord (byte[] data, int offset, int val) {
    data[offset]   = (byte) (val >>> 8);
    data[offset+1] = (byte) val;
  }

  // 3 bytes. used only for 3-byte len at start of a chunk
  public static int ExtractByte3 (byte[] data, int offset) {
    return
      ((data[offset]   & 0xFF) << 16) +
      ((data[offset+1] & 0xFF) <<  8) +
       (data[offset+2] & 0xFF);
 }
  public static void StuffByte3 (byte[] data, int offset, int val) {
    data[offset]   = (byte) (val >>> 16);
    data[offset+1] = (byte) (val >>> 8);
    data[offset+2] = (byte) (val);
  }

  // 4 bytes. Newton "long" = Java "int"
  public static int ExtractLong (byte[] data, int offset) {
    return
      ((data[offset]   & 0xFF) << 24) +
      ((data[offset+1] & 0xFF) << 16) +
      ((data[offset+2] & 0xFF) <<  8) +
       (data[offset+3] & 0xFF);
  }
  public static int ExtractRLong (byte[] data, int offset) {
    return
      ((data[offset+3] & 0xFF) << 24) +
      ((data[offset+2] & 0xFF) << 16) +
      ((data[offset+1] & 0xFF) <<  8) +
       (data[offset]   & 0xFF);
  }
  public static void StuffLong (byte[] data, int offset, int val) {
    data[offset]   = (byte) (val >>> 24);
    data[offset+1] = (byte) (val >>> 16);
    data[offset+2] = (byte) (val >>>  8);
    data[offset+3] = (byte) val;
  }

  // 8-bit chars
  public static String ExtractString (byte[] data, int offset, int len) {
    return new String(data,offset,len);
  }

  public static void StuffString (byte[] data, int offset, String s, boolean addNull) {
    //s.getBytes(0, s.length(), data, offset);  // deprecated unfortunately
    int len = s.length();
    for(int i=0; i < len; i++)
      data[offset+i] = (byte) (s.charAt(i));
    if (addNull)
      data[offset+len] = 0; // null
  }

  // 8-bit chars. null terminated
  public static String ExtractCString (byte[] data, int offset, int len) {
    int i;
    for (i=0; i<len; i++)
      if (data[offset+i] == 0)
        break;
    return new String(data,offset,i);
  }

  // 16-bit (unicode) chars, possibly null terminated
  public static String ExtractUniString (byte[] data, int offset, int len) {
    int i;
    // len or null terminated; convert to unicode
    for (i=0; i<len; i+=2)
      if (data[offset+i+1] == 0)
        break;
    try {
      return new String(data,offset,i, "UTF-16BE");
    }
    catch (java.io.UnsupportedEncodingException ex) {
      System.err.println("ExtractUniString: encoding exception");
      return null;
    }
    catch (Exception ex) {
      // !!!Ne. ?? no unicode support, e.g, MRJ. just discard high bytes
      byte[] sb = new byte[len = i / 2]; // !!!Nf
      for (i=0; i < len; i++) {
        sb[i] = data[offset+1];
        offset+=2;
      }
      return new String(sb);
    }
  }

  public static void StuffUniString(byte[] data, int offset, String s, boolean addNull) {
    try {
      byte[] sb = s.getBytes("UTF-16BE");
      int len = sb.length;
      //BinaryMunger(data, offset, len, sb, 0, len);
      System.arraycopy(sb, 0, data, offset, len);
      if (addNull)
        data[offset+len] = data[offset+len+1] = 0;  // add null
   }
    catch (java.io.UnsupportedEncodingException ex) {
      System.err.println("StuffUniString: encoding exception");
    }
    catch (Exception ex) {  // !!!Ne
      // !!!Ne. ?? no unicode support, e.g, MRJ. just write 0 high bytes
      byte[] sb = s.getBytes();
      int len = sb.length;
      for (int i=0; i < len; i++) {
        data[offset] = 0;
        data[offset+1] = sb[i];
        offset+=2;
      };
      if (addNull)
        data[offset] = data[offset+1] = 0;  // add null
    };
  }

  public static byte[] ExtractBytes(byte[] data, int offset, int len) {
    //return BinaryMunger(new byte[len],0,len, data,offset,len);
    byte[] newData = new byte[len];
    System.arraycopy(data, offset, newData, 0, len);
    return newData;
  }
  public static void StuffBytes(byte[] data, int offset, int len, byte[] data2, int offset2) {
    //BinaryMunger(data,offset,len, data2,offset2,len);
    System.arraycopy(data2, offset2, data, offset, len);
  }
/*
  public static byte[] BinaryMunger(byte[] dest, int off1, int len1, byte[] src, int off2, int len2) {
    // just copy for now
    System.arraycopy(src, off2, dest, off1, len1);
    //for (int i=0; i<len1; i++) dest[off1+i] = src[off2+i];
    return dest;
  }
*/
}

class OtherImmed extends NewtonObject { // not used anywhere else
  int ref;

  public OtherImmed(int ptr) {
    ref = ptr;
  }
  public String toString(int level) { // ignore level
    return "?#0x" + Integer.toHexString(ref).toUpperCase();
  }
}

class MagicPtr extends NewtonObject {
  int ref;

  public MagicPtr(int ptr) {
    ref = ptr;
  }
  public String toString(int level) { // ignore level
    return "@" + ref;
  }
}





