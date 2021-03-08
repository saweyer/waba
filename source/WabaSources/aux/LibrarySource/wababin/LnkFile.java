package wababin;

import java.io.*;


/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
 *
 * this was just embedded in Exegen
 * separated out for easier maintenance (and since there's a class to handle all other types)
 * and to support a simple extract
**/
public class LnkFile implements ExegenFile {
  /** the path to the waba application on WinCE devices */
  private static final String ceWabaPath = "\\Program Files\\waba\\waba.exe";
  private static final String LNK_EXT=".lnk";
  private String lnkPath;

  protected LnkFile() {}
  public LnkFile(String path) {
    if (!isValid(path))
      path+=LNK_EXT;
    lnkPath=path;
  }

  /**
   * Is this path a valid lnk file (ie does it end with .lnk?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path) {
    return path.length()>4 && path.substring(path.length()-4).equalsIgnoreCase(LNK_EXT);
  }

  public void create (String ceWarpName, int classHeapSize, int objectHeapSize,
    int stackSize, int nativeStackSize, String className, String prcIcon,
    int defWidth, int defHeight) {
    try {
      if (! Exegen.quiet)
        System.out.println("...writing "+lnkPath);

      DataOutputStream dos = new DataOutputStream(new FileOutputStream(lnkPath));
      String lnk =
         "\""  + ceWabaPath + '"' +
        " /w " + defWidth +
        " /h " + defHeight+
        " /l " + classHeapSize +
        " /m " + objectHeapSize +
        " /s " + stackSize +
        " /t " + nativeStackSize +
        " \""  + className + '\\' + ceWarpName + '"';
      // NOTE: the format of a CE .lnk shortcut file is:
      //  <path len>#<path>
      // on one line with no spaces
      dos.writeBytes(lnk.length() + '#' + lnk);
      dos.close();
    }
    catch(FileNotFoundException fnfe) {
      Exegen.errExit("can't open output file: " + lnkPath, -5);
    }
    catch(IOException ioe) {
      Exegen.errExit("can't write to output file: " + lnkPath, -5);
    };
  }

  public String list (boolean extract) { // !!!Nb
    // right now, list and extract same
    String result = "";
    try {
      File lnkFile = new File(lnkPath);
      byte[] sb = new byte[(int) (lnkFile.length())];
      FileInputStream fis = new FileInputStream(lnkFile);
      fis.read(sb);
      fis.close();
      String lnk = new String(sb);
      result = lnk.substring(lnk.indexOf('/'),lnk.length()); // !!!Nc
System.out.println(".lnk params: " + result);
    }
    catch(FileNotFoundException fnfe) {
      Exegen.errExit("can't open input file: " + lnkPath, -5);
    }
    catch(IOException ioe) {
      Exegen.errExit("can't read input file: " + lnkPath, -5);
    };
    return result;
  }
}
