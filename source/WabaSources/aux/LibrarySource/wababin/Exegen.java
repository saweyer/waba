package wababin;

import java.io.*;

/**
 * Generates launcher apps for PalmOS and WinCE for launching Waba programs.
 * Is equivalent to the waba exegen.exe program.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 *
 * !!!N Newton version by S. Weyer based on 1.40.0
 * !!!Na 23 Dec 2000: first version
 * !!!Nb 12 Jan 2001: MakeBinaryFromHex, version, title, uniqueSymbol
 * !!!Nc 10 Feb 2001: PkgFile(.pkg); NTKFile writes args/icon; LnkFile; formats
**/
public class Exegen
{
  public static final String EXEGEN_VERSION = "Exegen 1.40.0Ng"; // !!!Nb

  public static final String[] argCmds = {"/w", "/h", "/l", "/m", "/s", "/t"}; // for GUI, PkgUtils

  /** the path to the waba application on WinCE devices */
//private static final String ceWabaPath = "\\Program Files\\waba\\waba.exe";

  // defaults for memory allocation
  public static final int DEFAULT_CLASS_HEAP_SIZE=14000;  // !!!Nc (public)
  public static final int DEFAULT_OBJECT_HEAP_SIZE=8000;  // !!!Nc
  public static final int DEFAULT_STACK_SIZE=1500;        // !!!Nc
  public static final int DEFAULT_NATIVE_STACK_SIZE=300;  // !!!Nc

  public static final int genFormats= 0x7; // default: pdb, wrp, pkg !!!Nc
  public static final int prcFormat = 0x1;
  public static final int lnkFormat = 0x2;
  public static final int pkgFormat = 0x4;
  public static final int ntkFormat = 0x8;
  public static final int libFormat = 0x10;

  /** should we suppress non error messages? */
  public static boolean quiet=false;

  public static void errExit(String msg, int code) { // !!!Nc
    System.err.println("ERROR: " + msg);
    System.exit(code);
  }

  /**
   * The main application
   */
  public static void main(String[] args)
  {
    if (args.length < 2)
      usage();

    String prcIcon=null;
    String prcCreator=null;
    String ceWarpDir=null;
    int defWidth = 0;
    int defHeight = 0;
    int classHeapSize = DEFAULT_CLASS_HEAP_SIZE;
    int objectHeapSize = DEFAULT_OBJECT_HEAP_SIZE;
    int stackSize = DEFAULT_STACK_SIZE;
    int nativeStackSize = DEFAULT_NATIVE_STACK_SIZE;
    int formats = genFormats;   // !!!Nc

    // parse options
    int i;
    for (i = 0; i < args.length; i++)
    {
      String arg = args[i];
      if (arg.charAt(0) != '/' || arg.length() < 2)
        break;
      switch(Character.toLowerCase(arg.charAt(1)))  // !!!Nc
      {
        case '?':
          usage();
          break;
        case 'q':
        //case 'Q':
          quiet = true;
          break;
        case 'c':
        //case 'C':
          if (++i == args.length)
            errExit("no creator id specified", -1); // !!!Nc
          prcCreator= arg = args[i];
          break;
        case 'i':
        //case 'I':
          if (++i == args.length)
            errExit("no icon file specified", -1);  // !!!Nc
          prcIcon = arg = args[i];
          break;
        case 'x': // !!!Nc,b
        //case 'X':
          if (++i == args.length)
            errExit("no extract file specified", -1);
          list(true, args[i], formats);
          System.exit(0);
          break;

        case 'p':
        //case 'P':
          if (++i == args.length)
            errExit("no directory specified for /p option", -1);  // !!!Nc
          ceWarpDir = args[i];
          break;
        case 'w':
        //case 'W':
        case 'h':
        //case 'H':
        case 'l':
        //case 'L':
        case 'm':
        //case 'M':
        case 's':
        //case 'S':
        case 't':
        //case 'T':
        case 'f':     // !!!Nc
        //case 'F":
          if (++i == args.length)
            errExit("no #", -1); // !!!Nc
          char c = Character.toLowerCase(arg.charAt(1));  // !!!Nc
          arg = args[i];
          int n=0;
          try
          {
            n= Integer.parseInt(arg);
          }
          catch(NumberFormatException e)
          {
            errExit("bad #: " + arg, -1); // !!!Nc
          }
          if      (c == 'l' /*|| c == 'L'*/) classHeapSize = n;
          else if (c == 'm' /*|| c == 'M'*/) objectHeapSize = n;
          else if (c == 's' /*|| c == 'S'*/) stackSize = n;
          else if (c == 't' /*|| c == 'T'*/) nativeStackSize = n;
          else if (c == 'w' /*|| c == 'W'*/) defWidth = n;
          else if (c == 'h' /*|| c == 'H'*/) defHeight = n;
          else if (c == 'f' /*|| c == 'F'*/) formats = n;   // !!!Nc
          break;
        default:
          errExit("unknown option " + arg, -1); // !!!Nc
      }
    }
    if (i == args.length)
      errExit("no output file specified", -1);  // !!!Nc
    String exefile = args[i++];
    String prcName=Utils.strip(exefile);


    String prcPath = "";
    if ((formats & prcFormat) != 0) {   // !!!Nc
      prcPath=exefile + ".prc";
    };
    String lnkPath = "";
    if ((formats & lnkFormat) != 0) {   // !!!Nc
      lnkPath=exefile + ".lnk";
      if (ceWarpDir == null)
        ceWarpDir="\\Program Files\\"+prcName;
    };
    String pkgPath = "";
    if ((formats & pkgFormat) != 0) {   // !!!Nc
      pkgPath = exefile + PkgFile.EXEGEN_EXT;
    };
    String ntkPath = "";
    if ((formats & ntkFormat) != 0) {   // !!!Nc
      ntkPath = exefile + NTKFile.EXEGEN_EXT;
    };

    if (i == args.length)
      errExit("no main window class specified", -1);  // !!!Nc

    // convert . to / in className to make it a class path
    String className = args[i++];

    if (className.endsWith(".class"))
      className=className.substring(0,className.length()-6);

    className=className.replace('.','/');

    if (i == args.length)
      errExit("no warp file specified", -1);  // !!!Nc
    String warpFile = args[i++];

    String warpExt=Utils.checkForExtension(warpFile);
    if (warpExt!=null)
      errExit("warp files should be specified without extensions such as "+warpExt, -1);  // !!!Nc
    String warpName=Utils.strip(warpFile);

    if (prcCreator==null & prcPath.length() > 0) // !!!Nc
      prcCreator=(new PdbFile(warpFile)).getCreator();

    if (i != args.length)
      errExit("extra arguments found at end of command " + args[i], -1); // !!!Nc

    if (!quiet)
    {
      copyright();

      System.out.println("output files: " + prcPath + ' ' + lnkPath +
        ' ' +ntkPath + ' ' + pkgPath); // !!!Nc,a
      System.out.println("class name: "+className);
      if (prcPath.length() > 0) {
        System.out.println("PalmOS PRC name: "+prcName);
        System.out.println("PalmOS PRC creator: "+prcCreator);
      };
      System.out.println("PalmOS PRC icon: " + ((prcIcon!=null) ? prcIcon : "<default>"));
      if (lnkPath.length() > 0)
        System.out.println("WindowsCE warp directory: "+ceWarpDir);
      System.out.println("class heap size: "+classHeapSize);
      System.out.println("object heap size: "+objectHeapSize);
      System.out.println("native stack size: "+nativeStackSize);
      System.out.println("stack size: "+stackSize);
    };

    // write out lnk file
    if (lnkPath.length() > 0) { // !!!Nc
      LnkFile lnk = new LnkFile(lnkPath);
      lnk.create(ceWarpDir + '\\' + warpName + WarpFile.WRP_EXT,
        classHeapSize, objectHeapSize,
        stackSize, nativeStackSize, className, prcIcon, defWidth, defHeight);
/*
    try
    {
      if (!quiet)
        System.out.println("...writing "+lnkPath);

      DataOutputStream dos=new DataOutputStream(new FileOutputStream(lnkPath));
      String lnk="\""+ceWabaPath+"\" /w "+defWidth+" /h "+defHeight+ " /l "+classHeapSize+
        " /m "+objectHeapSize+" /s "+stackSize+ " /t "+nativeStackSize+" "+className+" \""+
        ceWarpDir+"\\"+warpName+".wrp\"";
      // NOTE: the format of a CE .lnk shortcut file is:
      //
      // <path len>#<path>
      //
      // on one line with no spaces
      dos.writeBytes(lnk.length()+"#"+lnk);
      dos.close();
   }
    catch(FileNotFoundException fnfe)
    {
      errExit("can't open output file", -5);  // !!!Nc
    }
    catch(IOException ioe)
    {
      errExit("can't write to output file", -5);  // !!!Nc
    };
*/
    };
    if (pkgPath.length() > 0) { // !!!Nc
      // !!!Nc add args, icon to .pkg
      PkgFile pkg = new PkgFile(pkgPath, PkgFile.EXEGEN_EXT, (formats & libFormat) != 0);
      pkg.create(prcName, classHeapSize, objectHeapSize,
        stackSize, nativeStackSize, className, prcIcon, defWidth, defHeight);
    };

    if (ntkPath.length() > 0) { // !!!Nc
      // !!!Nc,a  write out Newton myapp.arg.txt
      NTKFile ntk = new NTKFile(ntkPath, NTKFile.EXEGEN_EXT, (formats & libFormat) != 0);
      ntk.create(prcName, classHeapSize, objectHeapSize,
        stackSize, nativeStackSize, className, prcIcon, defWidth, defHeight);
    };

    if (prcPath.length() > 0) { // !!!Nc
      // generate prc file. !!!Na moved since no icon.bmp caused exit!
      PrcFile prc=new PrcFile(prcPath);
      prc.create(prcName,prcCreator,classHeapSize,objectHeapSize,stackSize,nativeStackSize,className,prcIcon);
    };

    if (!quiet)
      System.out.println("...done");
  }

  public static String list(boolean extract, String name, int formats) { // !!!Nc. used in main and by GUI
    ExegenFile efile = null;
    String result = ""; // return any. use single format for specific values
//System.out.println("exegen formats: " + formats);
    if ((formats & prcFormat) != 0 || PrcFile.isValid(name))
      result = (efile = new PrcFile(name)).list(extract);
    if ((formats & lnkFormat) != 0 || LnkFile.isValid(name))
      result = (efile = new LnkFile(name)).list(extract);
    if ((formats & pkgFormat) != 0 ||
        PkgFile.isValid(name, PkgFile.EXEGEN_EXT))  // !!!Nc
      result = (efile = new PkgFile(name, PkgFile.EXEGEN_EXT, (formats & libFormat) != 0)).list(extract);
    if ((formats & ntkFormat) != 0 || NTKFile.isValid(name, NTKFile.EXEGEN_EXT))
      result = (efile = new NTKFile(name, NTKFile.EXEGEN_EXT, (formats & libFormat) != 0)).list(extract);
    if (efile == null)
      errExit("unrecog file to extract: " + name, -1); // !!!Nc
    return result;
  }

  /**
   * Print the copyright notice
   */
  private static void copyright()
  {
    System.out.println("Waba Launch Executable Generator for Java, Version " + EXEGEN_VERSION); // !!!Nb
    System.out.println("Copyright (C) Rob Nielsen 1999. All rights reserved");
    System.out.println("Newton modifications: Copyright (C) S. Weyer 2001. All rights reserved"); // !!!Nb
    System.out.println();
  }

  /**
   * Print usage information and quit
   */
  private static void usage()
  {
    copyright();
    System.out.println("Usage: java Exegen [options] exefile main-window-class warpfile");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  /?   Displays usage text");
    System.out.println("  /c   Override and assign PDC creator (e.g. /c CrTr)");
    System.out.println("  /f   Specify format flags: 1(prc) + 2(lnk) + 4(pkg) + 8(ntk) + 16(Newton lib)"); // !!!Nc
    System.out.println("  /h   Assign height of application's main window");
    System.out.println("  /i   Assign PalmOS PRC icon (e.g. /i sample.bmp)");
    System.out.println("  /l   Assign size of class heap (e.g. /l 10000)");
    System.out.println("  /m   Assign size of object heap (e.g. /m 20000)");
    System.out.println("  /p   Full path to directory containing warp file under WindowsCE");
    System.out.println("  /q   Quiet mode (no output except for errors)");  // !!!Nc
    System.out.println("  /s   Assign size of stack (e.g. /s 2000)");
    System.out.println("  /t   Assign size of native stack (e.g. /t 50)");
    System.out.println("  /w   Assign width of application's main window");
    System.out.println("  /x   eXtracts parameter info/icon.bmp (PRC only); lists app info(PKG only)"); // !!!Nc
    System.out.println();
    System.out.println("This program generates a WindowsCE application shortcut .lnk file,");
    System.out.println("a PalmOS .prc application, and Newton .pkg and NTK .arg.txt files.");   // !!!Nc,b
    System.out.println(".lnk and .prc files are used to launch (start up) a Waba program.");
    System.out.println(".pkg file can be installed/run directly on Newton;");              // !!!Nc
    System.out.println(".arg.txt is used to build a Newton application with NTK.");   // !!!Nb
    System.out.println();
    System.out.println("File extensions are generated automatically. If you specify myapp as");
    System.out.println("the exefile, myapp.lnk, myapp.prc, myapp.pkg, myapp.arg.txt will be created."); // !!!Nc,b
    System.out.println();
    System.out.println("The /w and /h parameters define the default width and height of the");
    System.out.println("application's window. The value of 0 for either will cause the main window");
    System.out.println("to appear at a default size which is different on each platform.");
    System.out.println();
    System.out.println("The /p parameter defines the full path to the directory which will contain");
    System.out.println("the warp file under WindowsCE. This path is placed in the shortcut (.lnk)");
    System.out.println("file so the application will know where to find it's warp file.");
    System.out.println();
    System.out.println("For PalmOS, if no icon is defined, a black box is used. Any icon given must");
    System.out.println("be in .bmp format. A PalmOS PRC creator and PRC name will be assigned based");
    System.out.println("on the warpfile and exefile respectively. The exefile must be 30 characters");
    System.out.println("or less.");
    System.out.println();
    System.out.println("The sizes specified are used by the WabaVM to determine how much memory");
    System.out.println("to allocate for the app. The size of the class heap defaults to "+(DEFAULT_CLASS_HEAP_SIZE / 1000)+"K");
    System.out.println("The size of the object heap defaults to "+(DEFAULT_OBJECT_HEAP_SIZE / 1000)+"K. The size of the stack");
    System.out.println("defaults to "+DEFAULT_STACK_SIZE+" bytes. The size of the native stack defaults to "+DEFAULT_NATIVE_STACK_SIZE+" bytes.");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("java Exegen /i s.bmp /p \"\\Program Files\\Scribble\" Scribble ScribbleApp scribble");
    System.out.println("java Exegen /w 160 /h 160 /m 20000 Calc CalcWindow calc");
    System.exit(0);
  }
}