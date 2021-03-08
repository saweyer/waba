package wababin;

import java.io.*;

/**
 * A warp file which packages up a number of input files (usually .class and
 * .bmp) into a single file, like a .zip or a .jar but with no compression.
 * This class describes the standard .wrp format but there is a derived class
 * PdbFile which formats the warp file in a form readable to PalmOS devices.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 *
 * !!!N modifications by S. Weyer
**/
public class WarpFile
{
  /** the extension of this file */
  public static final String WRP_EXT=".wrp";  // !!!Nc (public)

  /** the file object representing this warp file */
  protected File warpFile;

  /**
   * Constructs a new warp file.
   */
  protected WarpFile() {}

  /**
   * Constructs a new WarpFile with the given path.
   * @param path the path to this file.
   */
  public WarpFile(String path)
  {
    if (!isValid(path))
      path+=WRP_EXT;
    warpFile=new File(path);
  }

  /**
   * Is this path a valid pdb file (ie does it end with .wrp?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path)
  {
    return path!=null&&path.length()>4&&
      path.substring(path.length()-4).equalsIgnoreCase(WRP_EXT);
  }

  /**
   * Gets the filename of this warp file.
   * @param returns the file.
   */
  public String getFile()
  {
    return warpFile.toString();
  }

  /**
   * Creates this warp file with the given list of input files.
   * @param inputFiles a sorted array of files to add
   */
  public void create(InputFile[] inputFiles)
  {
    try
    {
      if (!Warp.quiet)
        System.out.println("...writing "+warpFile);
      DataOutputStream dos=new DataOutputStream(new FileOutputStream(warpFile));
      writeHeader(dos,inputFiles.length);
      writeFileList(dos,inputFiles);

      for (int i = 0; i < inputFiles.length; i++)
        writeRecord(dos,inputFiles[i]);

      dos.close();
      if (!Warp.quiet)
        System.out.println("...done");
    }
    catch (FileNotFoundException fnfe)
    {
      Warp.errExit("can't create file "+warpFile, -1); // !!!Nc
    }
    catch (IOException ioe)
    {
      Warp.errExit("problem writing to file "+warpFile, -1); // !!!Nc
    }
  }

  /**
   * Writes the header of this file
   * @param dos the output stream to write to
   * @param numInputFiles the number of input files in this warp file
   */
  protected void writeHeader(DataOutputStream dos,int numInputFiles) throws IOException
  {
    dos.writeBytes("Wrp1");
    dos.writeInt(numInputFiles);
  }

  /**
   * Writes the list of files contained in this warp file.  This consists of a list
   * of offsets in the file where each file starts.
   * @param dos the output stream to write to
   * @param inputFile the sorted array of input files.
   */
  protected void writeFileList(DataOutputStream dos,InputFile[] inputFiles) throws IOException
  {
    int recOffset=8+(inputFiles.length + 1) * 4;
    for (int i=0;i<inputFiles.length;i++)
    {
      dos.writeInt(recOffset);
      int pathLen = inputFiles[i].getName().length();
      int size = inputFiles[i].getFileLength();
      recOffset += 2 + pathLen + size;
    }
    dos.writeInt(recOffset);
  }

  /**
   * Writes an individual input file to this warp file.
   * @param dos the output stream to write to
   * @param inputFile the inputFile to write.
   */
  protected void writeRecord(DataOutputStream dos,InputFile inputFile) throws IOException
  {
    String name=inputFile.getName();
    if (!inputFile.exists())
      Warp.errExit("can't load file "+name, -1); // !!!Nc
    if (!Warp.quiet)
      System.out.println("...adding: "+name);
    dos.writeShort(name.length());
    dos.writeBytes(name);
    inputFile.writeFile(dos);
  }

  /**
   * List the contents of this warp file as a list of files and their sizes.
   */
  public String list(boolean extract) // !!!Nc,b. return type compat with Exegen.list (for PkgFile,NTKFile)
  {
    try
    {
      DataInputStream dis=new DataInputStream(new FileInputStream(warpFile));
      int numFiles=readHeader(dis);
      if (numFiles==-1)
      {
        dis.close();
        Warp.errExit("bad magic - file not a warp file "+warpFile, -1); // !!!Nc
      }

      if (!Warp.quiet)
      {
        Warp.copyright();
        System.out.println("file: "+warpFile);
      }
      System.out.println("record count: "+numFiles);
      System.out.println(((extract) ? "Extract" : "List") + " contents:"); // !!!Nb
      listFiles(dis,numFiles,extract);  // !!!Nb
      dis.close();
    }
    catch (FileNotFoundException fnfe)
    {
      Warp.errExit("can't open file "+warpFile, -1); // !!!Nc
    }
    catch (IOException e)
    {
      Warp.errExit("problem reading from file "+warpFile, -1); // !!!Nc
    };
    return "";    // ?? eventually return something
  }

  /**
   * Reads the header of the warp file.
   * @param dis the input stream to read from
   * @returns the number of files in this warp, or -1 if there is an error.
   */
  protected int readHeader(DataInputStream dis) throws IOException
  {
    if (Utils.readString(dis,4).equals("Wrp1"))
      return dis.readInt();
    else
      return -1;
  }

  /**
   * List the files in this warp file by reading the list of offsets and
   * then printing the name and size of each file pointed to.
   * @param dis the input stream to read from
   * @param numFiles the number of files to read
   */
  protected void listFiles(DataInputStream dis,int numFiles,boolean extract) throws IOException // !!!Nb
  {
    int[] offsets=new int[numFiles+1];
    for(int i = 0; i < numFiles+1; i++)
      offsets[i]=dis.readInt();
    listFiles(dis,numFiles,extract,offsets);  // !!!Nc
    dis.close();
  }

  // !!!Nc shared by WarpFile, PdbFile
  protected void listFiles(DataInputStream dis,int numFiles,boolean extract, int[] offsets) throws IOException {
    for (int i=0;i<numFiles;i++) {
      short pathLen=dis.readShort();
      String name=Utils.readString(dis,pathLen);
      int size =offsets[i+1]-offsets[i]-pathLen-2;
      System.out.println("  "+name+" ("+size+")");

      if (extract) {  // !!!Nc,b
        // write .class or .bmp file
        try {
        File xFile = new File(name);
        if (xFile.getParent() != null)
          xFile.getAbsoluteFile().getParentFile().mkdirs();
        PkgFile.writeStream(xFile, dis, size); // ?? define in Utils ?
        }
        catch (FileNotFoundException fnfe) {
          Warp.errExit("can't open file "+ name, -1);
        }
        catch (IOException e) {
          Warp.errExit("problem writing to file " + name, -1);
        }
      }
      else
        dis.skipBytes(size);
    };
  }
}