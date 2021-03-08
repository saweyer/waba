package wababin;

import java.io.*;
import java.util.*;
import java.net.URL;

/**
 * This class describes a single file that goes into a warp file.
 * This will usually be a '.class' or a '.bmp' file.
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Robert Nielsen</A>,
 * @version    1.0.1 1 October 1999
 *
 * !!!N modifications by S. Weyer
**/
public class InputFile
{
  static final char dirSeparator = File.pathSeparatorChar;

  static File[] classPath;

  /** the relative path of this file.  Corresponds to the full package name of class files */
  String relativePath;

  /** the file object to the file itself */
  File file;

  static
  {
    String pathstr = System.getProperty("java.class.path");
    // Count the number of path separators
    int i=0;
    int n=0;
    int j=0;
    while ((i = pathstr.indexOf(dirSeparator, i)) != -1)
    {
      n++;
      i++;
    }
    // Build the class path
    File[] path = new File[n+1];
    int len = pathstr.length();
    for (i = n = 0; i < len; i = j + 1)
    {
      if ((j = pathstr.indexOf(dirSeparator, i)) == -1)
        j = len;
      if (i != j)
      {
        File file = new File(pathstr.substring(i, j));
        if (file.isDirectory())
          path[n++]=file;
      }
    }
    // Trim class path to exact size
    classPath = new File[n];
    System.arraycopy(path, 0, classPath, 0, n);
  }

  /**
   * Constructs a new InputFile with the given relative path from the current dir.
   * @param path the path to the file
   */
  public InputFile(String path)
  {
    this(new File(path),path);
  }

  /**
   * Constructs a new InputFile with the given file and path.
   * @param file the file itself
   * @param relativePath the relative pathname of the file.
   */
  public InputFile(File file,String relativePath)
  {
    this.file=file;
    this.relativePath=relativePath.replace('\\','/');
  }

  /**
   * Gets the name of this file as it is to be stored under
   * in the warp file.  This includes the path to it's package
   * @returns the name
   */
  public String getName()
  {
    return relativePath;
  }

  /**
   * Gets the length of this file
   * @returns the length in bytes
   */
  public int getFileLength()
  {
    return (int)file.length();
  }

  /**
   * Writes this file to the given output stream.
   * @param os the stream to write to.
   */
  public void writeFile(OutputStream os)
  {
    try
    {
      InputStream is=new FileInputStream(file);
      byte[] buf=new byte[1024];
      int br;
      while((br=is.read(buf))!=-1)
        {
        os.write(buf,0,br);
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Compares the name of this InputFile to another one.  Used to
   * sort a list of InputFiles.  See java.lang.String.compareTo(String)
   * for details of the comparison used.
   * @param inputFile the file to compare to
   * @returns the result of the comparison
   */
  public int compareTo(InputFile inputFile)
  {
    return relativePath.compareTo(inputFile.relativePath);
  }

  /**
   * Does this InputFile actually exist?
   * @returns true if it does, false otherwise
   */
  public boolean exists()
  {
    return file.exists();
  }

  /**
   * A utility method to convert an array of relative paths to files into an array
   * of InputFiles.  Files are first looked for relative to the current dir, and then
   * in the classpath.  Basic wildcards like "files\\*.class" are supported as well as
   * directories eg. "extra\\util" which will grab all .class and .bmp files in the
   * extra\\util directory.  If recurseDirs is true, any subdirectories will also be added
   * @param files the array of paths
   * @param start the index to start in the array
   * @param num the number of paths to process
   * @param recurseDirs if a directory is specified, should any subdirectories also be added?
   */
  public static InputFile[] expandFiles(String[] files,int start,int num,boolean recurseDirs)
  {
    int i;
    Vector v=new Vector();
    for(i=0;i < num; i++)
      expand(v,files[start+i],recurseDirs);
    checkDependencies(v);

    // !!!Nc. moved the sort here in order to eliminate duplicates before copying to array!
    // Arrays.sort(array, comparator) would be simpler except
    // 1) it requires JDK 1.2+; 2) still should check/remove duplicates
    boolean changed = true;
    int comp, size = v.size();
    InputFile elem1, elem2;
    while (changed) {
      changed=false;
      i = 0;
      while (i < size-1) {
        elem1 = (InputFile)v.elementAt(i);
        elem2 = (InputFile)v.elementAt(i+1);
        comp = elem1.compareTo(elem2);
        if (comp == 0) {
          v.removeElementAt(i+1);
          size--;
          continue; // don't inc or 'change'
        }
        else if (comp > 0) {
          v.setElementAt(elem1, i+1); // swap
          v.setElementAt(elem2, i);
          changed = true;;
        };
        i++;
      };
      size--;
    };

    //return v.toArray(InputFile[v.size()]);  // >=1.2
    size = v.size();
    InputFile[] ret = new InputFile[size];
    for(i=0; i < size;i++)
      ret[i] = (InputFile) v.elementAt(i);
    return ret;
  }

  private static void checkDependencies(Vector v)
  {
    boolean warnings=false;
    for(int t=0;t<v.size();t++)
    {
      try
      {
        InputFile inf=(InputFile)v.elementAt(t);
        if (!inf.relativePath.endsWith(".class"))
          continue;
        DataInputStream in=new DataInputStream(new FileInputStream(inf.file));
        // Read the header
        int magic = in.readInt();
        if (magic != 0xCAFEBABE)
          throw new ClassFormatError("wrong magic!");
        int minor_version = in.readUnsignedShort();
        int version = in.readUnsignedShort();
        // Read the constant pool

        byte[] types = new byte[in.readUnsignedShort()];
        Object[] cpool = new Object[types.length];
        for (int i = 1 ; i < cpool.length ; i++)
        {
          switch(types[i] = in.readByte())
          {
            case 1: //CONSTANT_UTF8:
              cpool[i] = in.readUTF();
              break;
            case 3: //CONSTANT_INTEGER:
            case 4: //CONSTANT_FLOAT:
              in.skipBytes(4);
              break;
            case 5: //CONSTANT_LONG:
            case 6: //CONSTANT_DOUBLE:
              i++;
              in.skipBytes(8);
              break;
            case 7: //CONSTANT_CLASS:
            case 8: //CONSTANT_STRING:
              cpool[i] = new Integer(in.readUnsignedShort());
              break;
            case 9: //CONSTANT_FIELD:
            case 10: //CONSTANT_METHOD:
            case 11: //CONSTANT_INTERFACEMETHOD:
            case 12: //CONSTANT_NAMEANDTYPE:
              cpool[i] = new Integer((in.readUnsignedShort() << 16) | in.readUnsignedShort());
              break;
            case 0:
            default:
              throw new ClassFormatError("invalid constant type: " + (int)types[i]);
          }
        }
        in.close();
        for(int i=0;i<types.length;i++)
        {
          //System.out.println("no:"+i+" type:"+types[i]+" value:"+cpool[i]);
          if (types[i]==7) //CONSTANT_CLASS
          {
            String s=(String)cpool[((Integer)cpool[i]).intValue()];
            if (s.charAt(0)=='[')
            {
              int stindex=s.indexOf('L');
              if (stindex==-1)
                continue;
              s=s.substring(stindex+1,s.length()-1);
            }
            if (s.startsWith("java/")&&!s.equals("java/lang/String")&&
               !s.equals("java/lang/Object")&&!s.equals("java/lang/StringBuffer"))
            {
              if (!warnings)
              {
                System.err.println("*****************************************************");
                warnings=true;
              }
              System.err.println("Warning!  Reference to "+s.replace('/','.')+" in "+inf.relativePath);
            }
            if (s.startsWith("java/")||s.startsWith("waba/"))
              continue;
            s+=".class";
            int size=v.size();
            int j;
            for(j=0;j<size;j++)
              if (((InputFile)v.elementAt(j)).relativePath.equals(s))
                break;
            if (j>=size)
              expand(v,s,false);
          }
          else
          if (types[i]==8) //CONSTANT_STRING
          {
            String s=(String)cpool[((Integer)cpool[i]).intValue()];
            if (s.length()>=4&&
              s.substring(s.length()-4,s.length()).equalsIgnoreCase(".bmp"))
                expand(v,s,false);
          }
        }
      }
      catch(Exception e)
      {
        System.err.println(e);  // !!!Nc
      }
    }
    if (warnings)
      System.err.println("*****************************************************\n");
  }

  /**
   * Internal method for processing files from expandFiles() method.
   * @param v a Vector to add files to
   * @param file the file to process
   * @param recurseDirs should we recurse subdirs?
   */
  private static void expand(Vector v,String file,boolean recurseDirs)
  {
//System.out.println("file: " + file);
    String filter=null;
    int starInd=file.lastIndexOf("*");
    if (starInd!=-1)
    {
      filter=file.substring(starInd+1);
      file=file.substring(0,starInd);
    }
    File f=new File(file);
    for(int i=0;!f.exists()&&i<classPath.length;i++)
      f=new File(classPath[i],file);

    if (!f.exists())
      return;

    if (f.isFile())
      v.addElement(new InputFile(f,file));
    else
    {
      char ch=file.charAt(file.length()-1);
      if (ch!='\\'&&ch!='/')
        file=file+File.separator;
      String[] files=f.list();
      for(int i=0;i<files.length;i++)
        if ((filter!=null&&files[i].endsWith(filter))||files[i].endsWith(".class")||files[i].endsWith(".bmp"))
          v.addElement(new InputFile(new File(f,files[i]),file+files[i]));
        else
        if (filter==null&&recurseDirs)
        {
          File fdown=new File(f,files[i]);
          if (fdown.isDirectory())
            expand(v,file+files[i],true);
        }
    }
  }
}