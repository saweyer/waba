package wababin;

import java.io.*;
import java.util.Date;

/**
 * A warp file that saves in the PalmOS .pdb format.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 *
 * !!!N modifications by S. Weyer
**/
public class PdbFile extends WarpFile
{
  /** PDB version */
  private static final int VERSION=1;

  /** the extension of this file */
  private static final String PDB_EXT=".pdb";

  /** the name of this application */
  private String name=null;

  /** the 4 letter creator id for this file */
  private String creator=null;

  /**
   * Constructs a new PdbFile with the given path and default creator id
   * @param path the path to this file.
   */
  public PdbFile(String path)
  {
    this(path,null);
  }

  /**
   * Constructs a new PdbFile with the given path and creator id
   * @param path the path to this file.
   * @param creator the 4 letter creator id to use.
   */
  public PdbFile(String path,String creator)
  {
    if (!isValid(path))
      path+=PDB_EXT;
    warpFile=new File(path);
    this.creator=creator;
  }

  /**
   * Is this path a valid pdb file (ie does it end with .pdb?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path)
  {
    return path.length()>4&&path.substring(path.length()-4).equalsIgnoreCase(PDB_EXT);
  }

  /**
   * Gets the PDB version that this class writes out.
   * @returns the version
   */
  public int getVersion()
  {
    return VERSION;
  }

  /**
   * Gets the name of this pdb file by stripping it out of the pathname.
   * @returns the name
   */
  public String getName()
  {
    if (name==null)
    {
      name=Utils.strip(warpFile.toString());
      if (name.length()>30)
        Warp.errExit("warp file name must be less than 31 characters", -1); // !!!Nc
    }
    return name;
  }

  /**
   * Gets the creator of this pdb file.  If one was specified in the constructor, this
   * is returned, otherwise one is generated from the name.
   * @returns the creator
   */
  public String getCreator()
  {
    if (creator==null)
    {
      String name=getName();
      int i;
      int n = name.length();
      int hash = 0;
      byte[] creat=new byte[4];
      for (i = 0; i < n; i++)
        hash += (byte)name.charAt(i);
      for (i = 0; i < 4; i++)
      {
        creat[i] = (byte)((hash % 26) + 'a');
        if ((hash & 64)>0)
          creat[i] += ('A'-'a');
        hash = hash / 2;
      }
      creator=new String(creat);
    }
    return creator;
  }

  /**
   * Writes the header of this file
   * @param dos the output stream to write to
   * @param numInputFiles the number of input files in this warp file
   */
  protected void writeHeader(DataOutputStream dos,int numInputFiles) throws IOException
  {
    // we append a ! to the end of the name. Under PalmOS, if we have two
    // databases with the same name, they overwrite each other. The PRC file
    // has the same name as options.name so we append a ! to the warp resource
    // PDB name to make it different
    String name=getName()+"!";
    dos.writeBytes(name);

    // add some padding
    dos.write(new byte[32-name.length()]);

    // attributes
    // NOTE: tried to set 0x10 to overwrite existing but that flag
    // is apparently documented incorrectly and is actually the reset
    // when load flag. In any case 0x00 allows overwrite of existing.
    dos.writeByte(0x00);
    dos.writeByte(0x00);

    // set version
    dos.writeShort(VERSION);

    long timeX=new Date().getTime()/1000; // seconds since 1970
    timeX += 66 * 365 * 24 * 60 * 60; // rough add of 66 years

    // creation time
    dos.writeInt((int)timeX);

    // modification time
    dos.writeInt((int)timeX);

    // more padding
    dos.write(new byte[16]);

    // database type
    dos.writeBytes("Wrp1");

    // creator
    dos.writeBytes(getCreator());

    // more padding
    dos.write(new byte[8]);

    // # records
    dos.writeShort(numInputFiles);
  }

  /**
   * Writes the list of files contained in this warp file.  This consists of a list
   * of offsets in the file where each file starts.
   * @param dos the output stream to write to
   * @param inputFile the sorted array of input files.
   */
  protected void writeFileList(DataOutputStream dos,InputFile[] inputFiles) throws IOException
  {
    int recOffset = 78 + (inputFiles.length * 8) + 2;
    for (int i=0;i<inputFiles.length;i++)
    {
      dos.writeInt(recOffset);
      dos.writeInt(0);
      int pathLen = inputFiles[i].getName().length();
      int size = inputFiles[i].getFileLength();
      recOffset += 2 + pathLen + size;
    }

    // write 2 byte filler
    dos.writeByte(0x00);
    dos.writeByte(0x00);
  }

  /**
   * Reads the header of the warp file.
   * @param dis the input stream to read from
   * @returns the number of files in this warp, or -1 if there is an error.
   */
  protected int readHeader(DataInputStream dis) throws IOException
  {
    dis.skipBytes(60);
    if (!Utils.readString(dis,4).equals("Wrp1"))
      return -1;
    dis.skipBytes(12);
    return dis.readShort();
  }

  /**
   * List the files in this warp file by reading the list of offsets and
   * then printing the name and size of each file pointed to.
   * @param dis the input stream to read from
   * @param numFiles the number of files to read
   */
  protected void listFiles(DataInputStream dis,int numFiles, boolean extract) throws IOException // !!!Nb
  {
    int[] offsets=new int[numFiles+1]; // !!!Nc. +1
    for(int i = 0; i < numFiles; i++)
    {
      offsets[i]=dis.readInt();
      dis.readInt();
    };
    dis.readShort();

    // !!!Nc. with this, same code as used by WarpFile.listFiles
    offsets[numFiles] = (int)warpFile.length();
    listFiles(dis,numFiles,extract,offsets); // !!!Nc
    dis.close();
  }
}