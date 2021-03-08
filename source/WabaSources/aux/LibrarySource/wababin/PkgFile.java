package wababin;

import java.io.*;
import java.util.*; // Date, Vector, Calendar, Comparator
//import java.text.*;   // SimpleDateFormat, ParseException

import wababin.pkg.*;

/**
 * this creates a Newton .pkg file
 * by copying/modifying an application or library template file
 * Warp writes classes and converted bitmaps
 * Exegen writes args, icon, symbols/names
 *
 * @author     <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version    1.50.0N
 *
 * 14 Mar 2001: extName (different Extras name, e.g., lib)
 * 1.50.0Nc 10 Feb 2001: first version
 * !!!Ng  29 May 2001: appReqPkgs
**/

/**
 * ?? list return string w/ args and mainClassName
 *
 * ?? check existing symbols (avoid conflicts w/ existing pkg names, or duplicates)
 * ?? share boundsrects (many icons w/ same size)
 *
 * note: initial approach replaced most existing objects (symbols, arrays, strings)
 * in-place by shortening object length; this works ok in header (e.g., pkgSymbol)
 * however, in a part, this causes problems during install;
 * so, it seems to be NOT ok to change an object header (e.g., len)
 * but it is ok to change its contents -- e.g., bytes or actual array/frame refs.
 *
 * so, current approach is to create new symbols, etc. at end of package
 * and patch the object refs in various frames/arrays
 * it still appends new class and bitmap objects at end of package
 *
 * wababin/pkg/apptemplate__.pkg (APPTEMPLATE_PKG)
 * wababin/pkg/libtemplate__.pkg (LIBTEMPLATE_PKG)
 * if new versions, run pkg0 (pkg0L) to list pkg contents
 * possibly recompile with DEBUG=true
 * make sure there are no "not found"
 * edit in PkgFile (see SPECIAL CONSTANTS), recompile
 *
 * Warp (pkg1, pkg1x, pkg1L, pkg1xL)
 * 1. copies "template" into tmpData
 * 2. app: maybe overwrites appRequires array
 * 3. patches appContext to point to new frame
 * 4. compute sizes of classes; app: convert bitmaps and calc sizes
 * 5. fix size of part/package
 * 6. copy tmpData to yourapp_.pkg (for Exegen later)
 * 7. append classes; app: convert bitmaps
 *
 * Exegen (pkg2, pkg2x, pkg2L, pkg2Lx)
 * 1. copy first part of yourapp_.pkg into tmpFile (same len as APPTEMPLATE_PKG)
 * 2. replace/shorten various items (and lengths if approp):
 *    pkgSymbol (header) [replace/shorten symbol]
 *    appSymbol, DeletionScript, appArgs.uniqueSymbol [replace/shorten symbol]
 *    appName, text, appArgs.title [replace/shorten string]
 *    appArgs [replace int/nil]
 *    toolCopyStr, devCopyStr [replace/shorten string]
 *    creationDate [replace int]
 *    icon [replace binary; same len]
 * 3. create yourapp.pkg; copy tmpFile
 * 4. copy remainder of _yourapp.pkg
 * 5. delete _yourapp.pkg
 *
**/
public class PkgFile extends WarpFile implements ExegenFile {
  static final boolean DEBUG = false;
  static final int SORT_FRAMEMAP = 20;
  boolean lib = false;
  int itmp;

  /** the extension of this file */
  public static final String WARP_EXT  = ".pkg~";
  public static final String EXEGEN_EXT= ".pkg";
  public static final String APPTEMPLATE_PKG = "apptemplate__" + EXEGEN_EXT;
  public static final String LIBTEMPLATE_PKG = "libtemplate__" + EXEGEN_EXT;
  public static final String TEMPLATE_DIR = "wababin/pkg/";

  public static final String[] argAttribs = {  // GUI.htm, PkgUtils.extract
    "width",        "height",
    "classHeapSize","objectHeapSize",
    "vmStackSize",  "nmStackSize",
  };

  // inherits warpFile, getFile
  // does not implement list, readHeader, readFiles

  //protected PkgFile() {}

  /**
   * Constructs a new PkgFile with the given path.
   * @param path the path to this file.
   */
  public PkgFile(String path, String ext, boolean libformat) {
    if (!isValid(path,ext))
      path+=ext;
    warpFile=new File(path);
    itmp = (lib = libformat) ? 1 : 0;
  }

  /**
   * Is this path a valid pkg file (ie does it end with .pkg?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path, String ext) {
    int elen = ext.length();
    return path!=null && path.length() > elen &&
      path.substring(path.length()-elen).equalsIgnoreCase(ext);
  }

  byte[] tmpData; // APPTEMPLATE_PKG (usually) contents. need to be careful since this signed!

  // read some of a file, return stream (for more reading later) -- used only for
  public DataInputStream fileContents(File tmpFile, byte[] data) throws IOException {
    DataInputStream dis = new DataInputStream (new FileInputStream(tmpFile));
    int i = 0, rc, len = data.length;
    while (i < len) {
      rc = dis.read(data,i,len-i);
      if (rc == -1)
        break;
      i+=rc;
    };
    if (i != len)  // !!!Nf
      throw new IOException("fileContents. expected: " + len + "; read: " + i);
    return dis;
  }

  // read (possibly partial) contents of a file
  public byte[] fileContents(String fileName, int len) throws IOException {
    File file = new File(fileName);
    if (len < 0)
      len = (int) (file.length());
    return fileContents(new FileInputStream(file), len);
  }

  // read (possibly partial) contents of a stream (File, or getSystemResourceAsStream)
  public byte[] fileContents(InputStream is, int len) throws IOException { // !!!Nd
    if (is == null)
      throw new FileNotFoundException("null stream");
    byte[] data = new byte[len];
    int i = 0, rc;
    while (i < len) {
      rc = is.read(data,i,len-i);
      if (rc == -1)
        break;
      i+=rc;
    };
    is.close();
    if (i != len)  // !!!Nf
      throw new IOException("fileContents. expected: " + len + "; read: " + i);
    return data;
  }

  /**
   * Creates this warp file with the given list of input files.
   * called by Warp
   * @param inputFiles a sorted array of files to add
   */
  public void create(InputFile[] inputFiles, Vector appReqPkgs) {
    try {
      if (!Warp.quiet)
        System.out.println("...writing " + warpFile);

      int offset = 0, len = inputFiles.length;
      DataOutputStream dos = new DataOutputStream(new FileOutputStream(warpFile));
      offset = writeHeader(dos, offset, len);
//if (DEBUG) PkgUtils.debugVal(offset, "writeHeader done", offset);
      offset = writeFileList(dos, offset, inputFiles, appReqPkgs);
//if (DEBUG) PkgUtils.debugVal(offset, "writeFileList done", offset);

      for (int i = 0; i < len; i++)
        offset = writeRecord(dos, offset, inputFiles, i);
//if (DEBUG) PkgUtils.debugVal(offset, "writeRecord done", offset);
      dos.close();

      // the result should be a valid package !!
      // ...but with template's package name, args, icon
      // upon open, it should fail to find missing mainClass
      // ... unless it fails first due to missing VM or required lib/

      // next: Exegen
      if (!Warp.quiet)
        System.out.println("...done");
    }
    catch (FileNotFoundException fnfe) {
      Warp.errExit(fnfe + ". can't create file "+warpFile, -1);
    }
    catch (IOException ioe) {
      Warp.errExit(ioe + ". problem writing to file "+warpFile, -1);
    };
  }


  // called by Exegen
  // like PrcFile -prcCreator, +defWidth, +defHeight
  public void create (String prcName, int classHeapSize, int objectHeapSize,
    int stackSize, int nativeStackSize, String className, String prcIcon,
    int defWidth, int defHeight) {
    try {
      if (!Exegen.quiet)
        System.out.println("...writing " + warpFile);

      int offset, len;
      String fileName = warpFile.getName(); // Exegen .pkg extension
      fileName = fileName.substring(0, fileName.length()-EXEGEN_EXT.length()) + WARP_EXT;
      // this reads header and initial part (and keeps stream open to copy classes later)
      tmpData = new byte[PkgUtils.pkgLen[itmp]];
      DataInputStream dis = fileContents(new File(fileName), tmpData); // get open stream

      // PACKAGE CREATION DATE (int)
      Calendar nowCal = Calendar.getInstance();
      Date nowDate = nowCal.getTime(); //new Date();
      offset = PkgUtils.createTimeOffset;
      PkgUtils.StuffLong(tmpData, offset,
        PkgUtils.ExtractLong(tmpData, offset) + // secs since Newton template pkg created
        (int) ((nowDate.getTime() - PkgUtils.createTimeMS[itmp]) / 1000L)); // secs since then

      // header: DEVELOPER COPYRIGHT (unicode)
      // rewrite in place, same length; adding blanks if necessary
      String oldStr, newStr;
      offset = PkgUtils.copyOff1;
      oldStr = PkgUtils.ExtractUniString(tmpData, offset, PkgUtils.copyLen1);
      len = oldStr.length();
//if (DEBUG) PkgUtils.debugVal(offset, "old dev copyright", oldStr, len);
      newStr = Integer.toString(nowCal.get(Calendar.YEAR)) + ", " + System.getProperty("user.name");
//if (DEBUG) PkgUtils.debugVal(offset, "replace dev copyright", newStr, newStr.length());
      if (newStr.length() <= len) {
        StringBuffer sb = new StringBuffer(len);
        sb.append(oldStr.charAt(0));  // copyright symbol
        sb.append(newStr);
        for (int i=newStr.length()+1; i < len; i++)
          sb.append(' ');
        PkgUtils.StuffUniString(tmpData, offset, sb.toString(), false); // overwrite. but keep trail blanks and orig null
      };

      offset = PkgUtils.ExtractLong(tmpData, PkgUtils.pkgLenOffset); // end of current file
      int ref = 0;
      Symbol appSymbol = null;
      UString appName = null, appMainName = null, extName = null;
      Frame frame = new Frame(20);  // dummy frame for patching

      // header: PACKAGE SYMBOL (unicode), part: APPSYMBOL (ascii)
      // rewrite in place, probably shortening
      newStr = className + ":waba"; // use for appSymbol later
      len = (newStr.length()+1) * 2;
//if (DEBUG) PkgUtils.debugVal(PkgUtils.pkgNameOffset, "new pkgSymbol", newStr, len);
      if (len <= PkgUtils.pkgNameLen) {
        // unicode 'packageSymbol' in header
        PkgUtils.StuffWord     (tmpData, PkgUtils.pkgNameLenOffset, len);
        PkgUtils.StuffUniString(tmpData, PkgUtils.pkgName, newStr, true); // null

        if (! lib) {
          // ascii 'appSymbol/pkgSymbol' referenced by:
          //  theForm.appSymbol, partFrame.app, appArgs.uniqueSymbol, (DeletionScript)
          appSymbol = new Symbol(newStr);
          ref = PkgUtils.objRef(offset);
          frame.stuffVal(tmpData, PkgUtils.partFrame,    PkgUtils.appSlot,          ref); // partFrame.app
          frame.stuffVal(tmpData, PkgUtils.theFormFrame[itmp], PkgUtils.appSymbolSlot,    ref); // theForm.appSymbol
          frame.stuffVal(tmpData, PkgUtils.appArgsFrame, PkgUtils.uniqueSymbolSlot, ref); // appArgs.uniqueSymbol
          frame.stuffVal(tmpData, PkgUtils.dscriptArray, 0,                         ref); // DeletionScript(literals) [array but same headersize]
          offset += appSymbol.chunkSize();
        };
      }
      else {
        System.err.println("pkgSymbol too long: " + newStr);
        len = PkgUtils.pkgNameLen;  // ?? error since .pkg will have old sym
        // it might be possible to make it longer in header (by shortening tool copyright)
        // but not possible in part itself
      };

      // header: TOOL COPYRIGHT (ascii)
      // this follows pkgName and since that's probably shorter
      // rewrite earlier, possibly shortening
      oldStr = PkgUtils.ExtractCString(tmpData, PkgUtils.copyOff2, PkgUtils.copyLen2);
      int copyOff2 = PkgUtils.pkgName + len; // new location
      len = oldStr.length();
      int pos = oldStr.indexOf("Weyer") + 5;
//if (DEBUG) PkgUtils.debugVal(PkgUtils.copyOff2, "old tool copyright", oldStr, len);
      newStr = oldStr.substring(0,pos) + "; " + Warp.WARP_VERSION + "; " + Exegen.EXEGEN_VERSION;
//if (DEBUG) PkgUtils.debugVal(copyOff2, "add tool copyright", newStr, newStr.length());
      // it's probably ok for it to be even longer (since pkgName shorter) (but low priority)
      if (newStr.length() < PkgUtils.copyLen2) // truncate, with null
        PkgUtils.StuffString(tmpData, copyOff2, newStr, true);

      // part: APPNAME (unicode) referenced by
      //   theForm.appName, partFrame.text, appArgs.title
      if (prcName.length() < 20) {
        appName = new UString(prcName, itmp);
        ref = PkgUtils.objRef(offset);
        frame.stuffVal(tmpData, PkgUtils.theFormFrame[itmp], PkgUtils.appNameSlot[itmp], ref); // theForm.appName
        offset += appName.chunkSize();
        if (lib) {
          extName = new UString("lib/" + prcName, itmp);
          ref = PkgUtils.objRef(offset);
          offset += extName.chunkSize();
        }
        else
          frame.stuffVal(tmpData, PkgUtils.appArgsFrame, PkgUtils.titleSlot,   ref); // appArgs.title
        // ref = appName(app) or extName(lib)
        frame.stuffVal(tmpData, PkgUtils.partFrame,    PkgUtils.textSlot[itmp],    ref); // partFrame.text
      };

      // part: APPMAINNAME (unicode)
      if (!lib && className.length() <= 68) {
        if (! prcName.equals(className)) {
          appMainName = new UString(className, itmp);
          ref = PkgUtils.objRef(offset);
          offset += appMainName.chunkSize();
        }; // otherwise, reuse appName
        frame.stuffVal(tmpData, PkgUtils.theFormFrame[itmp], PkgUtils.appMainNameSlot, ref); // theForm.appMainName
      };
      setLength(offset);  // new part and pkg lengths
      // write actual symbol and strings after writing header and rest of part

      // part: APPARGS [in order]: width, height, vmStackSize, nmStackSize,
      //  classHeapSize, objectHeapSize, uniqueSymbol, title
      if (! lib) {
        int[] args = {defWidth, defHeight, stackSize, nativeStackSize,
          classHeapSize, objectHeapSize}; // rewrite all immediates (not: uniqueSymbol, title)
        for (int i=0; i < args.length; i++)
          frame.stuffVal(tmpData, PkgUtils.appArgsFrame, i,
            (args[i] == 0) ? PkgUtils.NIL_REF : PkgUtils.intRef(args[i]));
        // uniqueSymbol fixed via appSymbol, title via appName
      };

      // part: ICON (binary)
      if (!lib && prcIcon != null && prcIcon.endsWith(".bmp") && new File(prcIcon).exists()) {
        byte[] bmpData = fileContents(prcIcon, -1);
        Bitmap bitmap = new Bitmap(bmpData, true, itmp);  // icon
        bitmap.stuffBits(tmpData, PkgUtils.bitsData[itmp]); // no obj or bitmap headers
      };

      DataOutputStream dos = new DataOutputStream(new FileOutputStream(warpFile));
      dos.write(tmpData);     // header & original part
      writeStream(dos, dis);  // remainder (classes,bitmaps) of interim _.pkg file. dis.close

      // now append actual appSymbol, appName, appMainName
      offset = 0; // recycle tmpData
      if (appSymbol != null) {
        appSymbol.stuff(tmpData, offset);
        offset += appSymbol.chunkSize();
      };
      if (appName != null) {
        appName.stuff(tmpData, offset);
        offset += appName.chunkSize();
      };
      if (extName != null) {  // lib
        extName.stuff(tmpData, offset);
        offset += extName.chunkSize();
      };
      if (appMainName != null) {
        appMainName.stuff(tmpData, offset);
        offset += appMainName.chunkSize();
      };
      dos.write(tmpData,0,offset);
      dos.close();
      //new File(fileName).deleteOnExit();  //??
    }
    catch (FileNotFoundException fnfe) {
      Exegen.errExit(fnfe + ". can't create file "+warpFile, -1);
    }
    catch (IOException ioe) {
      Exegen.errExit(ioe + ". problem writing to file "+warpFile, -1);
    };
  }

  public void setLength(int pkgLen) { // used by both Warp and Exegen
    // fix only 3 header fields: part(2) and pkg lengths
    int partLen = pkgLen-PkgUtils.hdrLen;
    PkgUtils.StuffLong(tmpData, PkgUtils.partLenOffset,   partLen);
    PkgUtils.StuffLong(tmpData, PkgUtils.partLenOffset+4, partLen); // it's stored twice!
    PkgUtils.StuffLong(tmpData, PkgUtils.pkgLenOffset,    pkgLen);
  }

  /**
   * Writes the header of this file
   * @param dos the output stream to write to
   * @param numInputFiles the number of input files in this warp file
   */
  protected int writeHeader(DataOutputStream dos, int offset, int numInputFiles) throws IOException {
    // this just _reads_ header & default template part
    tmpData = fileContents(
      ClassLoader.getSystemResourceAsStream(TEMPLATE_DIR +  // getWabaPkgPath() !!!Nd
        ((lib) ? LIBTEMPLATE_PKG : APPTEMPLATE_PKG)),
      offset = PkgUtils.pkgLen[itmp]);

    // do 'real' work in writeFileList, writeRecord
    return offset; // always add at end of template
  }

  Object[][] fileSymbols;

  /**
   * Writes the list of files contained in this warp file.  This consists of a list
   * of offsets in the file where each file starts.
   * @param dos the output stream to write to
   * @param inputFile the sorted array of input files.
   */
  protected int writeFileList(DataOutputStream dos, int offset, InputFile[] inputFiles, Vector appReqPkgs) throws IOException {
    // first check that all files exist, and skip any in appReqPkgs
    InputFile inputFile;
    int i, nskip = 0, len = inputFiles.length, type = -1;
    fileSymbols = new Object[len][4];

    // note: strip doesn't work for our 'double' extension
    String name, warpName = warpFile.getName(), appReqPkg, appReqDir;
    warpName = warpName.substring(0, warpName.length() - WARP_EXT.length());
    for (i=0; i < len; i++) {
      inputFile = inputFiles[i];
      name = inputFile.getName();
      if (!inputFile.exists())
        Warp.errExit("can't load file "+name, -1);
      if (name.endsWith(".class")) {
        type = 1;
        name = name.substring(0,name.length()-6);
        for (int k=0; k < appReqPkgs.size(); k++) { // !!!Ng
          appReqPkg = (String) (appReqPkgs.elementAt(k));
          appReqDir = (appReqPkg.equals("wextra")) ? "extra/" : appReqPkg + '/'; // wextra for compat
          if (name.startsWith(appReqDir)) {
            type = 0; // skip
            nskip++;
            break;
          };
        };
      }
      else if (name.endsWith(".bmp") || name.endsWith(".wbm"))
        if (name.equals("icon.bmp")) {
          type = 0;
          nskip++;   // !!!Ne. not a lib but keep out of frame size
        }
        else
          type = 2;
      else
        Warp.errExit("unrecognized file type: " + name, -1);

      fileSymbols[i] = new Object[] {
        new Integer(new Symbol(name).getHash()),// for sort (if needed)
        new Integer(type),                      // 0 (skip lib & icon.bmp), 1 (class), 2 (bmp)
        new Integer(i),                         // original order in inputFiles
        name,                                   // String (w/o .class)
      };
    };

    if ((len-nskip) > SORT_FRAMEMAP) {
      // let this work for JDK < 1.2 (e.g., MRJ) -- borrow 'simple sort' from InputFile
      // this could also delete 'skipped' entries (and set nskip = 0)?
      // Arrays.sort(fileSymbols, new SymbolComparator()); -- SymbolComparator would need to be fixed
      boolean changed = true;
      int size = len, comp; // sort all, incl. skip entries
      Object[] entry1, entry2;
      int hash1, hash2; // these are signed, but need to do UNsigned comparison!
      while (changed) {
        changed=false;
        i = 0;
        while (i < size-1) {
          entry1 = fileSymbols[i];
          hash1 = ((Integer) entry1[0]).intValue();
          entry2 = fileSymbols[i+1];
          hash2 = ((Integer) entry2[0]).intValue();
          if (hash1 == hash2)
            comp = 0;
          else if ((hash1 > 0 && hash2 > 0) || (hash1 < 0 && hash2 < 0)) // same sign
            comp = (hash1 > hash2) ? 1 : -1;
          else // diff sign. - after +
            comp = (hash1 < 0) ? 1 : -1;
          if (comp > 0) {
            fileSymbols[i]   = entry2; // swap
            fileSymbols[i+1] = entry1;
            changed = true;;
          };
          i++;
        };
        size--;
      };
if (DEBUG) {
System.out.println("\nafter sort: ");
  for(int x=0; x<len; x++)
    System.out.println(Integer.toString(x) + ": [" + fileSymbols[x][2] +
      "]; hash=" + fileSymbols[x][0]);
};
    };


//if (DEBUG) PkgUtils.debugVal(appRequiresArray, "old appRequires len",
//      (PkgUtils.ExtractByte(tmpData, appRequiresArray+2)-12) / 4);

    // !!!Ng rewrite appRequires array (only for apps)
    if (! lib) {
      int appReqSym;
      for (int k=0; k < PkgUtils.appRequiresLen; k++) {
        if (k >= appReqPkgs.size())
          appReqSym = PkgUtils.NIL_REF;
        else {
          appReqPkg = (String) (appReqPkgs.elementAt(k));
          if (appReqPkg.equals("waba"))
            appReqSym = PkgUtils.objRef(PkgUtils.wabaSymbol);
          else if (appReqPkg.equals("wextra"))
            appReqSym = PkgUtils.objRef(PkgUtils.wextraSymbol);
          else { // create a new symbol (write object later)
System.out.println("creating new symbol: " + appReqPkg + " @ " + offset);
            appReqSym = PkgUtils.objRef(offset);
            offset += new Symbol(appReqPkg).chunkSize();
          };
        };
        new Array(PkgUtils.appRequiresLen).stuffVal(tmpData, PkgUtils.appRequiresArray, k, appReqSym);
      };
    };

//if (DEBUG) PkgUtils.debugVal(appContextSlot, "old appContext", PkgUtils.ExtractLong(tmpData, appContextSlot)-1);
//if (DEBUG) PkgUtils.debugVal(appContextSlot, "new appContext", offset);
    new Frame(17).stuffVal(tmpData, PkgUtils.theFormFrame[itmp], PkgUtils.appContextSlot[itmp], PkgUtils.objRef(offset));  // patch to new frame

    Frame appContext = new Frame(len-nskip);
    int frameSize = appContext.objSize();  // frame size in bytes
    byte[] frameBytes = new byte[frameSize];
if (DEBUG) PkgUtils.debugVal(offset, "frame", frameSize);
    offset += frameSize;
    appContext.stuffHeader(frameBytes, 0, PkgUtils.objRef(offset));  // // ptr to frameMap(immed next)

    Array frameMap = new Array(len+1-nskip);  // slots(framemap)
    int mapSize = frameMap.objSize();
    byte[] mapBytes = new byte[mapSize];
if (DEBUG) PkgUtils.debugVal(offset, "frameMap", mapSize);
    offset += mapSize;
    frameMap.stuffHeader(mapBytes, 0,    PkgUtils.intRef((len-nskip) > SORT_FRAMEMAP ? 1 : 0)); // class. 1=sorted
    frameMap.stuffVal   (mapBytes, 0, 0, PkgUtils.NIL_REF);   // no supermap

    int saveOffset = offset, symSize, objSize, fileLen;
    int slot = 0;    // separate counter since i might skip
    for (i=0; i < len; i++) {
      type = ((Integer) (fileSymbols[i][1])).intValue();
      if (type == 0)  // lib file?
        continue;     // skip

      inputFile = inputFiles[((Integer) (fileSymbols[i][2])).intValue()];
      name = (String) (fileSymbols[i][3]);

      symSize = new Symbol(name).chunkSize();
if (DEBUG) PkgUtils.debugVal(offset, "name", name, symSize);
      frameMap.stuffVal(mapBytes, 0, slot+1, PkgUtils.objRef(offset)); // ptr to future Symbol
      offset += symSize;

      fileLen = inputFile.getFileLength();
      objSize = (type == 1)
        ? new Binary(fileLen, 0).chunkSize()  // class
        : new Bitmap(fileContents(name,30)).chunkSize();  // just enough to estimate size. share??
if (DEBUG) PkgUtils.debugVal(offset, "obj", objSize);
      appContext.stuffVal(frameBytes, 0, slot, PkgUtils.objRef(offset)); // ptr to future obj
      offset += objSize;
      slot++;
    };

    setLength(offset);  // fix part and pkg lengths

    // now, write the header (and earlier part) !
    dos.write(tmpData);

    // !!!Ng now, write any _new_ appRequires symbols (refs written earlier)
    if (! lib)
      for (int k=0; k < appReqPkgs.size(); k++) {
        appReqPkg = (String) (appReqPkgs.elementAt(k));
        if (! appReqPkg.equals("waba") && ! appReqPkg.equals("wextra")) {
System.out.println("writing new symbol: " + appReqPkg);
          byte[] symBytes = new byte[Symbol.HEADERSIZE];
          Symbol symObj = new Symbol(appReqPkg);
          symObj.stuffHeader(symBytes, 0);
          dos.write(symBytes);
          dos.writeBytes(appReqPkg); // same as StuffCString
          dos.writeByte(0);     // null term
          symObj.pad(dos);
        };
      };

    // now, write the appContext frame and framemap (new part)
    dos.write(frameBytes);
    dos.write(mapBytes);

    // writeRecord goes back to write each symbol,val (just literals and easy pointers)
    return saveOffset;
  }

  /**
   * Writes an individual input file to this warp file.
   * @param dos the output stream to write to
   * @param inputFile the inputFile to write.
   */
  protected int writeRecord(DataOutputStream dos, int offset, InputFile[] inputFiles, int i) throws IOException {
    String name = (String) (fileSymbols[i][3]);     // inputFile.getName();
    int type = ((Integer) (fileSymbols[i][1])).intValue();
    if (type == 0)  { // skip lib or icon.bmp
      if (!Warp.quiet)
        System.out.println("...skipping: "+name);
      return offset;
    };

    if (!Warp.quiet)
      System.out.println("...adding: "+name);

    InputFile inputFile = inputFiles[((Integer) (fileSymbols[i][2])).intValue()];

    byte[] objBytes = new byte[Symbol.HEADERSIZE];  // for symbol(16), binary(12), frame(12)
    Symbol symObj = new Symbol(name);
//if (DEBUG) PkgUtils.debugVal(offset, "nameO",  name, symObj.objSize());
if (DEBUG) PkgUtils.debugVal(offset, "name", name, symObj.chunkSize());
    symObj.stuffHeader(objBytes, 0);
    dos.write(objBytes);
    dos.writeBytes(name); // same as StuffCString
    dos.writeByte(0);     // null term
    symObj.pad(dos);
    offset+=symObj.chunkSize();

    if (type == 1) { // class
      Binary binObj = new Binary(inputFile.getFileLength(), PkgUtils.objRef(PkgUtils.javaSymbol[itmp]));
      int binSize = binObj.chunkSize();
//if (DEBUG) PkgUtils.debugVal(offset, name+'O',  binObj.objSize());
if (DEBUG) PkgUtils.debugVal(offset, name, binSize);
      binObj.stuffHeader(objBytes, 0);
      dos.write(objBytes, 0, Binary.HEADERSIZE); // less than Symbol
      inputFile.writeFile(dos); // copy entire file (or use writeStream)
      binObj.pad(dos);
      offset += binSize;
    }
    else { // bitmap (type==2)
      Bitmap bitObj = new Bitmap(fileContents(name, -1), false, itmp); // ?? share
      int bitSize = bitObj.chunkSize(); // == objSize
if (DEBUG) PkgUtils.debugVal(offset, name, bitSize);
      // recycle tmpData  (?? check to make sure big enough, e.g., majong?)
      bitObj.stuff(tmpData, 0, offset);
      dos.write(tmpData,0,bitSize);
      offset += bitSize;
    };

    return offset;
  }

  static final int BUFLEN = 1024;

  // like InputFile.writeFile, except stream already open/skipped
  // used in create (copy remainder _.pkg); GUI:Run Applet (copy .jar)
  public static void writeStream(DataOutputStream dos, DataInputStream dis) throws IOException {
    byte[] buffer = new byte[BUFLEN];
    int br;
    while ((br=dis.read(buffer))!=-1)
      dos.write(buffer,0,br);
    dis.close();
  }

  // use for file copying, extract in PdbFile, WarpFile
  public static void writeStream(File file, DataInputStream dis, int len) throws IOException {
    DataOutputStream dos=new DataOutputStream(new FileOutputStream(file));

    byte[] buffer = new byte[BUFLEN];
    int br;
    while (len > 0 && (br = dis.read(buffer, 0, (len < BUFLEN)? len : BUFLEN))!=-1) {
      dos.write(buffer,0,br);
      len -= br;
    };

    dos.close();
  }
/*
  public String getWabaPkgPath() {
    char fileSep = File.separatorChar;  // between dirs, e.g., \  == System.getProperty("file.separator")
    char pathSep = File.pathSeparatorChar;  // between paths, e.g., ; == System.getProperty("path.separator")
    String classpath = System.getProperty("java.class.path");
    int spos, epos = classpath.indexOf("wextra" + fileSep + "classes");
    if (epos > 0) {
      spos = classpath.lastIndexOf(pathSep, epos) + 1;
      epos = classpath.indexOf(pathSep, epos);
      if (epos < 0)
        epos = classpath.length();
      if (classpath.charAt(epos) == fileSep)
        epos--;
      return classpath.substring(spos,epos) +
        fileSep + "wababin" + fileSep + "pkg" + fileSep;
    };
    return "";
  }
*/

  // useful to check a package, particularly if APPTEMPLATE_PKG changes to derive new constants
  public String list (boolean extract) {
//if (DEBUG) System.out.println("abs path: " + warpFile.getAbsolutePath());
//if (DEBUG) System.out.println("pkg path: " + getWabaPkgPath());
    String result = "";
    try {
      String fileName = warpFile.getName();
      if (fileName.equals(APPTEMPLATE_PKG) ||
          (fileName.equals(LIBTEMPLATE_PKG) && (lib=true)))
        tmpData = fileContents( // !!!ND getWabaPkgPath()
          ClassLoader.getSystemResourceAsStream(TEMPLATE_DIR + fileName), PkgUtils.pkgLen[itmp]);
      else
        tmpData = fileContents(fileName, -1);
      Object[] argsAndMain = new PkgUtils(tmpData, lib).extractPackage(extract);
      if (argsAndMain != null) {
        Frame appArgs = (Frame) argsAndMain[0];
        Object arg;
        for (int i=0; i < argAttribs.length; i++) {
          arg = appArgs.getValue(argAttribs[i]);
          if (i < 2 && ! (arg instanceof Integer)) // nil?
            arg = "0";
          result += Exegen.argCmds[i] + ' ' + arg + ' ';
        };
        result += (String) argsAndMain[1]; // appMainName
System.out.println(".pkg params: " + result);
      };
    }
    catch (FileNotFoundException fnfe) {
      Warp.errExit(fnfe + ". can't find file " + warpFile.getAbsolutePath(), -1);
    }
    catch (IOException ioe) {
      Warp.errExit(ioe + ". problem reading from file " + warpFile, -1);
    };
    return result;
  }
}