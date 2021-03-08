package wababin;

import java.util.*;  // Vector.   ??Arrays, Comparator

/**
 * Generates warp files for PalmOS and WinCE which contain .class and
 * .bmp files that make up an application.pkg
 * Is equivalent to the waba warp.exe program.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 *
 * !!!N Newton version by S. Weyer based on 1.50.0
 * !!!Na 23 Dec 2000  .pkg support
 * !!!Nb 12 Jan 2001 WARP_VERSION, CMD_EXTRACT (see WarpFile,PdbFile,NTKClsFile), don't include extra/
 * !!!Nc 10 Feb 2001 NTKFile, PkgFile
 * !!!Ng 29 May 2001 appReqPkgs
 *
 * ?? generalize EXT, isValid, etc. for WarpFile, PdbFile, NTKFile, PkgFile
 * ?? for Mac MRJ, set file types/creators?
**/
public class Warp
{
  public static final String WARP_VERSION = "Warp 1.50.0Ng"; // !!!Nb

  public static final int genFormats= 0x7; // default: pdb, wrp, pkg !!!Nc
  public static final int pdbFormat = 0x1;
  public static final int wrpFormat = 0x2;
  public static final int pkgFormat = 0x4;
  public static final int ntkFormat = 0x8;
  public static final int libFormat = 0x10;

  /** the create warp file command */
  public static final int CMD_CREATE=1;

  /** the list warp file command */
  public static final int CMD_LIST=2;

  /** the eXtract warp file command */
  public static final int CMD_EXTRACT=3;  // !!!Nb

  /** should we suppress non error messages? */
  public static boolean quiet;

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

    int cmd = 0;
    boolean extract = false;
    switch (Character.toLowerCase(args[0].charAt(0))) // !!!Nc
    {
      case 'c':
      //case 'C':
        cmd = CMD_CREATE;
        break;
      case 'l':
      //case 'L':
        cmd = CMD_LIST;
        break;
      case 'x':   // !!!Nb
      //case 'X':
        cmd = CMD_EXTRACT;
        extract = true;
        break;
      default:
        errExit("no command specified", -1);  // !!!Nc
    }

    // parse command line options
    boolean recurseDirs = false;
    String creator=null;
    Vector appReqPkgs = new Vector(4);

    int formats = genFormats;   // !!!Nc
    quiet=false;
    int i;
    for (i=1;i<args.length;i++)
    {
      String arg = args[i];
      if (arg.charAt(0) != '/' || arg.length() < 2)
        break;
      switch(Character.toLowerCase(arg.charAt(1)))  // !!!Nc
      {
        case '?':
          usage();
          break;
        case 'f':
        //case 'F":
          if (++i == args.length)
            errExit("no format# specified", -1);   // !!!Nc
          arg = args[i];
          try {
            formats = Integer.parseInt(arg);
          }
          catch(NumberFormatException e) {
            errExit("bad #", -1); // !!!Nc
          };
          break;
        case 'r':
        //case 'R':
          recurseDirs=true;
          break;
        case 'c':
        //case 'C':
          if (++i == args.length)
            errExit("no creator specified", -1);   // !!!Nc
          arg = args[i];
          if (arg.length() != 4)
            errExit("creator must be 4 characters",-1);   // !!!Nc
          creator = arg;
          break;
        case 'q':
        //case 'Q':
          quiet = true;
          break;
        case 'l': // !!!Ng
          for (int k=0; k<50; k++) {
            if (k >= wababin.pkg.PkgUtils.appRequiresLen)
              errExit("total libraries > " + wababin.pkg.PkgUtils.appRequiresLen, -1);
            if (i+1 >= args.length)
              break; // error -> no /f or warp file
            arg = args[i+1];
            if (arg.charAt(0) == '/')
              break;
            appReqPkgs.addElement(arg);
            i++;
          };
          break;
        default:
          errExit("unknown option "+arg, -1); // !!!Nc
        }
      }
    if (i == args.length)
      errExit("no warp file specified", -1);   // !!!Nc
    String warpFile = args[i];
    String warpExt;
    if (cmd == CMD_CREATE && (warpExt=Utils.checkForExtension(warpFile))!=null)
      errExit("when creating, don't specify an extension such as "+warpExt, -1);  // !!!Nc

    i++;
    if (cmd == CMD_CREATE)
    {
      if (!quiet)
        copyright();
      // generate input file list - expand wildcards and get full paths
      InputFile[] inputFiles;
      if (i==args.length)
        inputFiles=InputFile.expandFiles(new String[]{warpFile+".class"},0,1,false);
      else
        inputFiles=InputFile.expandFiles(args,i,args.length-i,recurseDirs);

      if (inputFiles.length == 0)
        errExit("no input files specified",-1); // !!!Nc

      if (!quiet)
        System.out.print("warp files: ");

      PdbFile pdb = null;
      if ((formats & pdbFormat) != 0) {   // !!!Nc
        pdb = new PdbFile(warpFile,creator);
        if (!quiet)
          System.out.print(pdb.getFile() + ' ');
      };
      WarpFile wrp = null;
      if ((formats & wrpFormat) != 0) {  // !!!Nc
        wrp = new WarpFile(warpFile);
        if (!quiet)
          System.out.print(wrp.getFile() + ' ');
      };
      PkgFile pkg = null;
      if ((formats & pkgFormat) != 0) {   // !!!Nc
        pkg = new PkgFile(warpFile, PkgFile.WARP_EXT, (formats & libFormat) != 0);
        if (!quiet)
          System.out.print(pkg.getFile() + ' ');
      };
      NTKFile ntk = null;
      if ((formats & ntkFormat) != 0) {   // !!!Nc
        ntk = new NTKFile(warpFile, NTKFile.WARP_EXT, (formats & libFormat) != 0);
        if (!quiet)
          System.out.print(ntk.getFile() + ' ');
      };
      if (!quiet) {
        System.out.println();
        if (pdb != null) {
          System.out.println("PalmOS PDB name: "+pdb.getName());
          System.out.println("PalmOS PDB creator: "+pdb.getCreator());
          System.out.println("PalmOS PDB version: "+pdb.getVersion());
        };
      };
      // sort path names
      //sortInputFiles(inputFiles); // !!!Nc already done in expandFiles

      if (pdb != null)            // !!!Nc
        pdb.create(inputFiles);
      if (wrp != null)            // !!!Nc
        wrp.create(inputFiles);
      if (pkg != null)            // !!!Nc
        pkg.create(inputFiles,appReqPkgs);
      if (ntk != null)            // !!!Nc
        ntk.create(inputFiles,appReqPkgs);
    }
    else if (cmd == CMD_LIST || extract) { // !!!Nc,b
      list(extract, warpFile, formats);
    }
  }

  public static String list(boolean extract, String warpFile, int formats) { // !!!Nc. used in main and by GUI
      WarpFile wf = null;
      String result = "";
      if ((formats & pdbFormat) != 0 || PdbFile.isValid(warpFile))
        result = (wf = new PdbFile(warpFile)).list(extract);
      if ((formats & wrpFormat) != 0 || WarpFile.isValid(warpFile))
        result = (wf = new WarpFile(warpFile)).list(extract);
      if ((formats & pkgFormat) != 0 || PkgFile.isValid(warpFile, PkgFile.WARP_EXT))
        result = (wf = new PkgFile(warpFile, PkgFile.WARP_EXT, (formats & libFormat) != 0)).list(extract);
      if ((formats & ntkFormat) != 0 || NTKFile.isValid(warpFile, NTKFile.WARP_EXT))
        result = (wf = new NTKFile(warpFile, NTKFile.WARP_EXT, (formats & libFormat) != 0)).list(extract);
      if (wf == null)
        errExit("file does not have appropriate file extension: " + warpFile, -1);
      return result;
  }

  /**
   * Sorts the input files into alphabetic order.  I've just used a bubble sort
   * here as the number of files is likely to be small and I can't be bothered
   * remembering how to do a quicksort :)
   * @param inputFiles the unsorted array of files which are to be sorted.
   */
/* !!!Nc -- moved to InputFile.expandFiles in order to elim duplicates
  public static void sortInputFiles(InputFile[] inputFiles)
  {
    boolean changed=true;
    int j=inputFiles.length-1;
    while(changed)
    {
      changed=false;
      for(int i=0;i<j;i++)
      {
        if (inputFiles[i].compareTo(inputFiles[i+1])>0)
        {
          InputFile temp=inputFiles[i];
          inputFiles[i]=inputFiles[i+1];
          inputFiles[i+1]=temp;
          changed=true;;
        }
      }
      j--;
    }; // end while
  }
*/
  /**
   * Print the copyright notice
   */
  public static void copyright()
  {
    System.out.println("Waba Application Resource Packager for Java, Version " + WARP_VERSION); // !!!Nb
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
    System.out.println("Usage: java wababin.Warp command [options] warpfile [files]");
    System.out.println();
    System.out.println("Commands:");
    System.out.println("   c   Create new warp file");
    System.out.println("   l   List contents of a warp file");
    System.out.println("   x   extract contents of a warp file"); // !!!Nb
    System.out.println();
    System.out.println("Options:");
    System.out.println("  /?   Displays usage text");
    System.out.println("  /c   Override and assign PDB database creator (e.g. /c CrTr)");
    System.out.println("  /f   Specify format flags: 1(pdb) + 2(wrp) + 4(pkg) + 8(ntk) + 16(Newton lib)"); // !!!Nc
    System.out.println("  /l   Specify library/path pairs(Newton): libname path/ ... /f "); // !!!Nf
    System.out.println("  /r   If a directory is specified in the files, recurse any subdirs");
    System.out.println("  /q   Quiet mode (no output except for errors)");
    System.out.println();
    System.out.println("This program creates WindowsCE .wrp and PalmOS .pdb warp files,");
    System.out.println("and Newton _.pkg and NTK .cls.txt files.");  // !!!Nc,b
    System.out.println("For PalmOS, a PDB database name and PDB creator will be");
    System.out.println("generated automatically from the name of the warp file.");
    System.out.println("For Newton(.pkg), it used wababin/pkg/apptemplate__.pkg,"); // !!!Nc
    System.out.println("and it creates myapp_.pkg file (intermediate file for Exegen)");
    System.out.println();
    System.out.println("Warp will automatically check any class files for dependencies and add");
    System.out.println("these files so you will only need to specify the main class file and");
    System.out.println("everything else will be added automatically, even directly referenced");
    System.out.println(".bmp files. (ie. Image im=new Image(\"rob.bmp\"); )");
    System.out.println("If no input files are specified, it will look for a .class file with");
    System.out.println("the same name of the warp file you are creating.  ");
    System.out.println();
    System.out.println("Examples:");
    System.out.println("   java wababin.Warp c helloApp");
    System.out.println("   java wababin.Warp c helloApp *.class util\\*.class");
    System.out.println("   java wababin.Warp c helloApp *.class extra\\");
    System.out.println("   java wababin.Warp l helloApp.wrp");
    System.out.println("   java wababin.Warp l helloApp.pdb");
    System.out.println("   java wababin.Warp x helloApp.wrp");  // !!!Nb
    System.out.println("   java wababin.Warp x helloApp.pdb");  // !!!Nb
    System.exit(0);
  }
}
