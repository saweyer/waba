package wababin;

import java.io.*;

/**
 * A launcher app for PalmOS devices.
 *
 * @author     <A HREF="mailto:rnielsen@cygnus.uwa.edu.au">Rob Nielsen</A>,
 * @version    1.0.0 12 June 1999
 *
 * !!!N modifications by S. Weyer. added list, isValid; fixed bug in loadBmpIcon
 */
public class PrcFile implements ExegenFile {  // !!!Nc
  /** the number of rows that the icon takes up in the prc file */
  private static final int ICON_ROWS=22;

  /** the number of bytes in each row of the icon in the prc file */
  private static final int ICON_BYTES_PER_ROW=4;

  // Offsets into the byte structure which defines the stub applcation
  static int creatorOffset = 64;
  static int launchOffset = 1219;
  static int bitmapOffset = 1512;
  static int nameOffset = 1774;

  /**
   * The base set of bytes for the application.  Various parts of this array are changed
   * to reflect the specific settings.  It's a bit ugly casting each value to a byte but
   * I couldn't find any other way to do it.
   */
  byte bytes[] =
      { (byte)
       83, (byte)116, (byte)117, (byte) 98, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  1, (byte)  0, (byte)  1, (byte)178, (byte)  6, (byte) 35, (byte)104, (byte)
      178, (byte)  6, (byte) 35, (byte)104, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
       97, (byte)112, (byte)112, (byte)108, (byte) 88, (byte) 89, (byte) 88, (byte) 89, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  7, (byte) 99, (byte)111, (byte)
      100, (byte)101, (byte)  0, (byte)  1, (byte)  0, (byte)  0, (byte)  0, (byte)150, (byte)100, (byte) 97, (byte)
      116, (byte) 97, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  4, (byte)136, (byte)116, (byte) 65, (byte)
       73, (byte) 66, (byte)  3, (byte)232, (byte)  0, (byte)  0, (byte)  5, (byte)216, (byte)116, (byte) 70, (byte)
       82, (byte) 77, (byte)  3, (byte)232, (byte)  0, (byte)  0, (byte)  6, (byte)104, (byte)116, (byte) 65, (byte)
       73, (byte) 78, (byte)  3, (byte)232, (byte)  0, (byte)  0, (byte)  6, (byte)238, (byte) 99, (byte)111, (byte)
      100, (byte)101, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  7, (byte) 14, (byte)116, (byte)118, (byte)
      101, (byte)114, (byte)  3, (byte)232, (byte)  0, (byte)  0, (byte)  7, (byte) 38, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  1, (byte) 72, (byte)122, (byte)  0, (byte)  4, (byte)  6, (byte)151, (byte)
        0, (byte)  0, (byte)  3, (byte) 86, (byte) 78, (byte)117, (byte) 47, (byte)  3, (byte) 79, (byte)239, (byte)
      255, (byte)214, (byte) 54, (byte) 47, (byte)  0, (byte) 54, (byte) 72, (byte)111, (byte)  0, (byte) 34, (byte)
       63, (byte) 60, (byte)  0, (byte)  1, (byte) 47, (byte) 60, (byte)112, (byte)115, (byte)121, (byte)115, (byte)
       78, (byte) 79, (byte)162, (byte)123, (byte) 32, (byte) 47, (byte)  0, (byte) 44, (byte)176, (byte)175, (byte)
        0, (byte) 60, (byte) 79, (byte)239, (byte)  0, (byte) 10, (byte)100, (byte)  0, (byte)  0, (byte)150, (byte)
       48, (byte)  3, (byte)  2, (byte) 64, (byte)  0, (byte) 12, (byte) 12, (byte) 64, (byte)  0, (byte) 12, (byte)
      102, (byte)  0, (byte)  0, (byte)130, (byte) 12, (byte)175, (byte)  2, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte) 34, (byte)100, (byte)118, (byte) 72, (byte)111, (byte)  0, (byte) 38, (byte) 72, (byte)111, (byte)
        0, (byte) 36, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 47, (byte) 60, (byte)109, (byte)101, (byte)
      109, (byte)114, (byte) 47, (byte) 60, (byte) 97, (byte)112, (byte)112, (byte)108, (byte) 72, (byte)111, (byte)
        0, (byte) 18, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 78, (byte) 79, (byte)160, (byte)120, (byte)
       74, (byte)175, (byte)  0, (byte) 62, (byte) 79, (byte)239, (byte)  0, (byte) 24, (byte)102, (byte) 20, (byte)
       72, (byte)109, (byte)254, (byte)230, (byte) 63, (byte) 60, (byte)  0, (byte) 29, (byte) 72, (byte)109, (byte)
      254, (byte)222, (byte) 78, (byte) 79, (byte)160, (byte)132, (byte) 79, (byte)239, (byte)  0, (byte) 10, (byte)
       74, (byte)175, (byte)  0, (byte) 38, (byte)103, (byte) 46, (byte) 66, (byte)167, (byte) 66, (byte)103, (byte)
       47, (byte) 47, (byte)  0, (byte) 44, (byte) 63, (byte) 47, (byte)  0, (byte) 42, (byte) 78, (byte) 79, (byte)
      160, (byte)167, (byte) 54, (byte)  0, (byte) 74, (byte) 67, (byte) 79, (byte)239, (byte)  0, (byte) 12, (byte)
      103, (byte) 20, (byte) 72, (byte)109, (byte)254, (byte)250, (byte) 63, (byte) 60, (byte)  0, (byte) 29, (byte)
       72, (byte)109, (byte)254, (byte)222, (byte) 78, (byte) 79, (byte)160, (byte)132, (byte) 79, (byte)239, (byte)
        0, (byte) 10, (byte) 48, (byte) 60, (byte)  5, (byte) 12, (byte) 96, (byte)  2, (byte)112, (byte)  0, (byte)
       79, (byte)239, (byte)  0, (byte) 42, (byte) 38, (byte) 31, (byte) 78, (byte)117, (byte) 72, (byte)231, (byte)
       16, (byte) 56, (byte) 79, (byte)239, (byte)255, (byte)216, (byte) 72, (byte)111, (byte)  0, (byte) 34, (byte)
       72, (byte)111, (byte)  0, (byte) 42, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 47, (byte) 60, (byte)
       87, (byte) 65, (byte) 66, (byte) 65, (byte) 47, (byte) 60, (byte) 76, (byte) 97, (byte)117, (byte)110, (byte)
       72, (byte)111, (byte)  0, (byte) 18, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 78, (byte) 79, (byte)
      160, (byte)120, (byte) 54, (byte)  0, (byte) 74, (byte) 67, (byte) 79, (byte)239, (byte)  0, (byte) 24, (byte)
      102, (byte) 14, (byte) 47, (byte) 47, (byte)  0, (byte) 34, (byte) 63, (byte) 47, (byte)  0, (byte) 42, (byte)
       78, (byte) 79, (byte)160, (byte) 66, (byte) 92, (byte) 79, (byte) 66, (byte) 39, (byte) 47, (byte) 60, (byte)
       76, (byte) 97, (byte)117, (byte)110, (byte) 47, (byte) 60, (byte) 87, (byte) 65, (byte) 66, (byte) 65, (byte)
       72, (byte)109, (byte)255, (byte) 98, (byte) 66, (byte)103, (byte) 78, (byte) 79, (byte)160, (byte) 65, (byte)
       54, (byte)  0, (byte) 74, (byte) 67, (byte) 79, (byte)239, (byte)  0, (byte) 16, (byte)103, (byte)  6, (byte)
       48, (byte)  3, (byte) 96, (byte)  0, (byte)  0, (byte)194, (byte) 63, (byte) 60, (byte)  0, (byte)  3, (byte)
       47, (byte) 60, (byte) 87, (byte) 65, (byte) 66, (byte) 65, (byte) 47, (byte) 60, (byte) 76, (byte) 97, (byte)
      117, (byte)110, (byte) 78, (byte) 79, (byte)160, (byte)117, (byte) 36, (byte) 72, (byte) 32, (byte) 10, (byte)
       79, (byte)239, (byte)  0, (byte) 10, (byte)102, (byte)  8, (byte) 48, (byte) 60, (byte)  5, (byte)  7, (byte)
       96, (byte)  0, (byte)  0, (byte)156, (byte) 66, (byte)111, (byte)  0, (byte) 32, (byte) 72, (byte)120, (byte)
        0, (byte) 80, (byte) 72, (byte)111, (byte)  0, (byte) 36, (byte) 47, (byte) 10, (byte) 78, (byte) 79, (byte)
      160, (byte) 85, (byte) 38, (byte) 72, (byte) 32, (byte) 11, (byte) 79, (byte)239, (byte)  0, (byte) 12, (byte)
      102, (byte)  6, (byte) 48, (byte) 60, (byte)  5, (byte)  7, (byte) 96, (byte)120, (byte) 47, (byte) 11, (byte)
       78, (byte) 79, (byte)160, (byte) 33, (byte) 40, (byte) 72, (byte) 72, (byte)120, (byte)  0, (byte) 80, (byte)
       72, (byte)109, (byte)255, (byte) 16, (byte) 66, (byte)167, (byte) 47, (byte) 12, (byte) 78, (byte) 79, (byte)
      160, (byte)118, (byte) 47, (byte) 11, (byte) 78, (byte) 79, (byte)160, (byte) 34, (byte) 31, (byte) 60, (byte)
        0, (byte)  1, (byte) 66, (byte)103, (byte) 47, (byte) 10, (byte) 78, (byte) 79, (byte)160, (byte) 94, (byte)
       47, (byte) 10, (byte) 78, (byte) 79, (byte)160, (byte) 74, (byte) 72, (byte)111, (byte)  0, (byte) 70, (byte)
       72, (byte)111, (byte)  0, (byte) 78, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 47, (byte) 60, (byte)
       87, (byte) 65, (byte) 66, (byte) 65, (byte) 47, (byte) 60, (byte) 97, (byte)112, (byte)112, (byte)108, (byte)
       72, (byte)111, (byte)  0, (byte) 54, (byte) 31, (byte) 60, (byte)  0, (byte)  1, (byte) 78, (byte) 79, (byte)
      160, (byte)120, (byte) 54, (byte)  0, (byte) 74, (byte) 67, (byte) 79, (byte)239, (byte)  0, (byte) 60, (byte)
      103, (byte)  4, (byte) 48, (byte)  3, (byte) 96, (byte) 22, (byte) 66, (byte)167, (byte) 63, (byte) 60, (byte)
      157, (byte)212, (byte) 47, (byte) 47, (byte)  0, (byte) 40, (byte) 63, (byte) 47, (byte)  0, (byte) 48, (byte)
       78, (byte) 79, (byte)160, (byte)167, (byte) 79, (byte)239, (byte)  0, (byte) 12, (byte) 79, (byte)239, (byte)
        0, (byte) 40, (byte) 76, (byte)223, (byte) 28, (byte)  8, (byte) 78, (byte)117, (byte) 47, (byte) 10, (byte)
       36, (byte)111, (byte)  0, (byte)  8, (byte) 48, (byte) 18, (byte)  4, (byte) 64, (byte)  0, (byte) 24, (byte)
      103, (byte)  6, (byte) 85, (byte) 64, (byte)103, (byte)  2, (byte) 96, (byte) 72, (byte) 78, (byte) 79, (byte)
      161, (byte)115, (byte) 47, (byte)  8, (byte) 78, (byte) 79, (byte)161, (byte)113, (byte) 63, (byte) 60, (byte)
        0, (byte) 60, (byte) 47, (byte) 60, (byte)  0, (byte) 35, (byte)  0, (byte)  8, (byte) 72, (byte)109, (byte)
      255, (byte)106, (byte) 78, (byte) 79, (byte)162, (byte) 32, (byte) 63, (byte) 60, (byte)  0, (byte) 73, (byte)
       47, (byte) 60, (byte)  0, (byte) 35, (byte)  0, (byte)  8, (byte) 72, (byte)109, (byte)255, (byte)142, (byte)
       78, (byte) 79, (byte)162, (byte) 32, (byte) 63, (byte) 60, (byte)  0, (byte) 86, (byte) 47, (byte) 60, (byte)
        0, (byte) 35, (byte)  0, (byte)  8, (byte) 72, (byte)109, (byte)255, (byte)178, (byte) 78, (byte) 79, (byte)
      162, (byte) 32, (byte)112, (byte)  1, (byte) 79, (byte)239, (byte)  0, (byte) 34, (byte) 96, (byte)  2, (byte)
      112, (byte)  0, (byte) 36, (byte) 95, (byte) 78, (byte)117, (byte) 72, (byte)231, (byte) 24, (byte) 32, (byte)
       79, (byte)239, (byte)255, (byte)230, (byte) 54, (byte) 47, (byte)  0, (byte) 42, (byte) 56, (byte) 47, (byte)
        0, (byte) 48, (byte) 74, (byte) 67, (byte)103, (byte)  6, (byte)112, (byte)  0, (byte) 96, (byte)  0, (byte)
        0, (byte)204, (byte) 63, (byte)  4, (byte) 47, (byte) 60, (byte)  2, (byte)  0, (byte)  0, (byte)  0, (byte)
       78, (byte)186, (byte)253, (byte)122, (byte) 54, (byte)  0, (byte) 74, (byte) 67, (byte) 92, (byte) 79, (byte)
      103, (byte)  8, (byte) 48, (byte) 67, (byte) 32, (byte)  8, (byte) 96, (byte)  0, (byte)  0, (byte)176, (byte)
       78, (byte)186, (byte)254, (byte) 48, (byte) 62, (byte)128, (byte) 74, (byte) 87, (byte)103, (byte) 10, (byte)
       63, (byte) 60, (byte)  3, (byte)232, (byte) 78, (byte) 79, (byte)161, (byte)155, (byte) 84, (byte) 79, (byte)
      118, (byte)  0, (byte) 96, (byte)  0, (byte)  0, (byte)142, (byte) 72, (byte)120, (byte)  0, (byte)100, (byte)
       72, (byte)111, (byte)  0, (byte)  6, (byte) 78, (byte) 79, (byte)161, (byte) 29, (byte) 72, (byte)111, (byte)
        0, (byte) 10, (byte) 78, (byte) 79, (byte)160, (byte)169, (byte) 74, (byte)  0, (byte) 79, (byte)239, (byte)
        0, (byte) 12, (byte)102, (byte)112, (byte) 72, (byte) 87, (byte) 72, (byte)111, (byte)  0, (byte)  6, (byte)
       66, (byte)167, (byte) 78, (byte) 79, (byte)161, (byte)191, (byte) 74, (byte)  0, (byte) 79, (byte)239, (byte)
        0, (byte) 12, (byte)102, (byte) 92, (byte) 48, (byte) 47, (byte)  0, (byte)  2, (byte)  4, (byte) 64, (byte)
        0, (byte)  9, (byte)103, (byte) 12, (byte)  4, (byte) 64, (byte)  0, (byte) 13, (byte)103, (byte) 62, (byte)
       83, (byte) 64, (byte)103, (byte) 24, (byte) 96, (byte) 60, (byte) 12, (byte)111, (byte)  3, (byte)233, (byte)
        0, (byte) 10, (byte)102, (byte)  2, (byte)118, (byte)  1, (byte) 72, (byte)111, (byte)  0, (byte)  2, (byte)
       78, (byte) 79, (byte)161, (byte)160, (byte) 88, (byte) 79, (byte) 96, (byte) 48, (byte) 56, (byte) 47, (byte)
        0, (byte) 10, (byte) 63, (byte)  4, (byte) 78, (byte) 79, (byte)161, (byte)111, (byte) 36, (byte) 72, (byte)
       47, (byte) 10, (byte) 78, (byte) 79, (byte)161, (byte)116, (byte) 72, (byte)122, (byte)254, (byte)222, (byte)
       47, (byte) 10, (byte) 78, (byte) 79, (byte)161, (byte)159, (byte) 79, (byte)239, (byte)  0, (byte) 14, (byte)
       96, (byte) 14, (byte)118, (byte)  1, (byte) 96, (byte) 10, (byte) 72, (byte)111, (byte)  0, (byte)  2, (byte)
       78, (byte) 79, (byte)161, (byte)160, (byte) 88, (byte) 79, (byte) 74, (byte) 67, (byte)103, (byte)  0, (byte)
      255, (byte)112, (byte)112, (byte)  0, (byte) 79, (byte)239, (byte)  0, (byte) 26, (byte) 76, (byte)223, (byte)
        4, (byte) 24, (byte) 78, (byte)117, (byte) 47, (byte)  3, (byte) 79, (byte)239, (byte)255, (byte)244, (byte)
       72, (byte) 87, (byte) 72, (byte)111, (byte)  0, (byte)  8, (byte) 72, (byte)111, (byte)  0, (byte) 16, (byte)
       78, (byte) 79, (byte)160, (byte)143, (byte) 54, (byte)  0, (byte) 74, (byte) 67, (byte) 79, (byte)239, (byte)
        0, (byte) 12, (byte)103, (byte) 26, (byte) 72, (byte)109, (byte)255, (byte)228, (byte) 63, (byte) 60, (byte)
        0, (byte) 60, (byte) 72, (byte)109, (byte)255, (byte)214, (byte) 78, (byte) 79, (byte)160, (byte)132, (byte)
      112, (byte)  0, (byte) 79, (byte)239, (byte)  0, (byte) 22, (byte) 38, (byte) 31, (byte) 78, (byte)117, (byte)
       32, (byte)111, (byte)  0, (byte)  8, (byte) 48, (byte) 40, (byte)  0, (byte)  6, (byte)  2, (byte) 64, (byte)
        0, (byte)  4, (byte)103, (byte) 16, (byte) 72, (byte)122, (byte)  0, (byte) 14, (byte) 72, (byte)122, (byte)
        0, (byte)  4, (byte)  6, (byte)151, (byte)  0, (byte)  0, (byte)  0, (byte) 64, (byte) 78, (byte)117, (byte)
       32, (byte)111, (byte)  0, (byte)  8, (byte) 63, (byte) 40, (byte)  0, (byte)  6, (byte) 47, (byte) 40, (byte)
        0, (byte)  2, (byte) 63, (byte) 16, (byte) 72, (byte)122, (byte)  0, (byte) 14, (byte) 72, (byte)122, (byte)
        0, (byte)  4, (byte)  6, (byte)151, (byte)255, (byte)255, (byte)254, (byte)164, (byte) 78, (byte)117, (byte)
       38, (byte)  0, (byte) 47, (byte) 47, (byte)  0, (byte)  8, (byte) 47, (byte) 47, (byte)  0, (byte) 16, (byte)
       47, (byte) 47, (byte)  0, (byte) 24, (byte) 78, (byte) 79, (byte)160, (byte)144, (byte) 32, (byte)  3, (byte)
       79, (byte)239, (byte)  0, (byte) 32, (byte) 38, (byte) 31, (byte) 78, (byte)117, (byte) 78, (byte)117, (byte)
        0, (byte)  0, (byte)  1, (byte) 68, (byte)255, (byte)255, (byte)254, (byte)222, (byte)133, (byte) 83, (byte)
      116, (byte)117, (byte) 98, (byte) 46, (byte) 99, (byte) 65, (byte)143, (byte) 67, (byte)111, (byte)117, (byte)
      108, (byte)100, (byte) 32, (byte)110, (byte)111, (byte)116, (byte) 32, (byte)102, (byte)105, (byte)110, (byte)
      100, (byte) 32, (byte) 97, (byte) 32, (byte)112, (byte) 65, (byte)145, (byte) 67, (byte)111, (byte)117, (byte)
      108, (byte)100, (byte) 32, (byte)110, (byte)111, (byte)116, (byte) 32, (byte)108, (byte) 97, (byte)117, (byte)
      110, (byte) 99, (byte)104, (byte) 32, (byte) 97, (byte) 32, (byte)112, (byte) 65, (byte)207, (byte)108, (byte)
       97, (byte)117, (byte)110, (byte) 99, (byte)104, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte)
       51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte)
       49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte)
       50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte)
       51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte)
       49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte)
       50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte)
       51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 51, (byte) 49, (byte) 50, (byte) 65, (byte)
      133, (byte) 76, (byte) 97, (byte)117, (byte)110, (byte) 99, (byte)104, (byte) 65, (byte)133, (byte) 73, (byte)
       39, (byte)109, (byte) 32, (byte)115, (byte)111, (byte) 32, (byte)114, (byte)148, (byte)121, (byte) 44, (byte)
       32, (byte) 98, (byte)117, (byte)116, (byte) 32, (byte)116, (byte)104, (byte)105, (byte)115, (byte) 32, (byte)
      112, (byte)114, (byte)111, (byte)103, (byte)114, (byte) 97, (byte)109, (byte) 32, (byte)110, (byte) 32, (byte)
      101, (byte)198, (byte)100, (byte)115, (byte) 32, (byte) 97, (byte)  0, (byte) 97, (byte) 32, (byte) 87, (byte)
       97, (byte) 98, (byte) 97, (byte) 32, (byte) 86, (byte)105, (byte)114, (byte)116, (byte)117, (byte) 97, (byte)
      108, (byte) 32, (byte) 77, (byte) 97, (byte) 99, (byte)104, (byte)105, (byte)110, (byte)101, (byte) 32, (byte)
      116, (byte)111, (byte) 32, (byte)114, (byte)117, (byte)110, (byte) 32, (byte) 97, (byte)110, (byte)100, (byte)
       32, (byte) 73, (byte)  0, (byte) 99, (byte) 97, (byte)110, (byte) 39, (byte)116, (byte) 32, (byte)102, (byte)
      105, (byte)110, (byte)100, (byte) 32, (byte)111, (byte)110, (byte)101, (byte) 32, (byte)111, (byte)110, (byte)
       32, (byte)116, (byte)104, (byte)105, (byte)115, (byte) 32, (byte)100, (byte)101, (byte)118, (byte)105, (byte)
       99, (byte)101, (byte) 46, (byte) 35, (byte) 32, (byte) 64, (byte)142, (byte) 83, (byte)116, (byte) 97, (byte)
      114, (byte)116, (byte)117, (byte)112, (byte) 67, (byte)111, (byte)100, (byte)101, (byte) 46, (byte) 99, (byte)
        0, (byte) 69, (byte) 32, (byte)114, (byte)141, (byte)111, (byte)114, (byte) 32, (byte)108, (byte) 97, (byte)
      117, (byte)110, (byte) 99, (byte)104, (byte)105, (byte)110, (byte)103, (byte) 32, (byte) 97, (byte) 32, (byte)
      112, (byte)135, (byte)108, (byte)105, (byte) 99, (byte) 97, (byte)116, (byte)105, (byte)111, (byte)110, (byte)
       64, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte) 40, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
       40, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte) 32, (byte)  0, (byte) 32, (byte)
        0, (byte)  4, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0,

      // default icon
      (byte)0, (byte)0, (byte)0, (byte)0,
      (byte)0, (byte)0, (byte)0, (byte)0,
      (byte)0, (byte)0, (byte)0, (byte)0,
      (byte)0, (byte)3, (byte)192, (byte)0,
      (byte)0, (byte)15, (byte)240, (byte)0,
      (byte)0, (byte)31, (byte)248, (byte)0,
      (byte)0, (byte)63, (byte)252, (byte)0,
      (byte)0, (byte)127, (byte)254, (byte)0,
      (byte)0, (byte)79, (byte)242, (byte)0,
      (byte)0, (byte)207, (byte)243, (byte)0,
      (byte)0, (byte)206, (byte)115, (byte)0,
      (byte)0, (byte)198, (byte)99, (byte)0,
      (byte)0, (byte)228, (byte)39, (byte)0,
      (byte)0, (byte)97, (byte)134, (byte)0,
      (byte)0, (byte)113, (byte)142, (byte)0,
      (byte)0, (byte)59, (byte)220, (byte)0,
      (byte)0, (byte)31, (byte)248, (byte)0,
      (byte)0, (byte)15, (byte)240, (byte)0,
      (byte)0, (byte)3, (byte)192, (byte)0,
      (byte)0, (byte)0, (byte)0, (byte)0,
      (byte)0, (byte)0, (byte)0, (byte)0,
      (byte)0, (byte)0, (byte)0, (byte)0,

  (byte)0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte) 18, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)160, (byte)  0, (byte)160, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        3, (byte)232, (byte)128, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte)  0, (byte)  2, (byte)  0, (byte) 24, (byte)226, (byte)204, (byte)  9, (byte)  3, (byte)
        0, (byte)  0, (byte)  0, (byte) 80, (byte)  1, (byte)  3, (byte)  0, (byte)  0, (byte)  0, (byte)108, (byte)
        0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)
        0, (byte)  0, (byte) 87, (byte) 97, (byte) 98, (byte) 97, (byte) 86, (byte) 77, (byte) 32, (byte) 77, (byte)
      105, (byte)115, (byte)115, (byte)105, (byte)110, (byte)103, (byte)  0, (byte)  0, (byte)  3, (byte)233, (byte)
        0, (byte)  2, (byte)  0, (byte)148, (byte)  0, (byte) 42, (byte)  0, (byte) 11, (byte)  0, (byte) 24, (byte)
      227, (byte)  8, (byte)201, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte) 67, (byte)108, (byte)
      111, (byte)115, (byte)101, (byte)  0, (byte) 78, (byte) 65, (byte) 77, (byte) 69, (byte) 53, (byte) 54, (byte)
       55, (byte) 56, (byte) 57, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte)
       55, (byte) 56, (byte) 57, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte)
       55, (byte) 56, (byte) 57, (byte) 48, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte)  0, (byte) 48, (byte)
        0, (byte)  0, (byte)  1, (byte) 40, (byte)  0, (byte)  0, (byte)  0, (byte)  8, (byte)  0, (byte)  0, (byte)
        0, (byte) 32, (byte)  0, (byte)  0, (byte) 63, (byte) 60, (byte)  0, (byte)  1, (byte)169, (byte)240, (byte)
       49, (byte) 46, (byte) 48, (byte)  0
      };

  /** the extension of this file */
  private static final String PRC_EXT=".prc";   // !!!Nb

  /**
   * Is this path a valid pdb file (ie does it end with .pdb?)
   * @returns true if it is, false otherwise
   */
  public static boolean isValid(String path)    // !!!Nb
  {
    return path.length()>4&&path.substring(path.length()-4).equalsIgnoreCase(PRC_EXT);
  }

  /** the path to this file */
  private String prcPath;

  /**
   * Constructs a new prcfile with the given path
   */
  public PrcFile(String path)
  {   if (!isValid(path))   // !!!Nc
      path+=PRC_EXT;

    prcPath=path;
  }

  /**
   * Creates the prcfile with the given settings
   * @param prcName the name of the application
   * @param prcCreator the creator of the application
   * @param classHeapSize the size of the class heap
   * @param objectHeapSize the size of the object heap
   * @param stackSize the size of the stack
   * @param nativeStackSize the size of the native stack
   * @param className the classname of the main window
   * @param prcIcon the path to the icon, or null to use the default
   */
  public void create(String prcName,String prcCreator,int classHeapSize,int objectHeapSize,
    int stackSize,int nativeStackSize,String className,String prcIcon)
  {
    if (bytes[0] != 'S')
      Exegen.errExit("bad bytes 0", -2); // !!!Nc
    writeString(prcName,0,false,30,0);

    if (bytes[64] != 'X')
      Exegen.errExit("bad bytes 1", -2); // !!!Nc

    writeString(prcCreator,64,false);

    // launch string is 60 chars (launch..)
    if (bytes[launchOffset] != 'l')
      Exegen.errExit("bad bytes 2", -3); // !!!Nc

    String launch="/l "+classHeapSize+" /m "+objectHeapSize+" /s "+stackSize+" /t "+nativeStackSize+" "+
      className+" "+prcCreator;
    if (launch.length() >= 80)
      Exegen.errExit("launch string too long", -8);  // !!!Nc
    writeString(launch,launchOffset,true,80,' ');

    // bitmap is 4 bytes of 22 rows
    if (bytes[bitmapOffset] != 0 || bytes[bitmapOffset + 1] != 0)
      Exegen.errExit("bad bytes 3", -4); // !!!Nc
    if (prcIcon != null)
    {
      if (prcIcon.endsWith(".bmp"))
        loadBmpIcon(prcIcon);
      else
        Exegen.errExit("unknown icon type "+ prcIcon, -1); // !!!Nc
    }

    // name is <= 30 chars
    if (bytes[nameOffset] != 'N')
      Exegen.errExit("bad bytes 4", -5); // !!!Nc

    writeString(prcName,nameOffset,true,30,' ');

    // write the file
    if (!Exegen.quiet)
      System.out.println("...writing "+prcPath);
    try
    {
      FileOutputStream fos=new FileOutputStream(prcPath);
      fos.write(bytes);
      fos.close();
    }
    catch (FileNotFoundException fnfe)
    {
      Exegen.errExit("can't open output file", -5);  // !!!Nc
    }
    catch (IOException ioe)
    {
      Exegen.errExit("can't write to file", -5);   // !!!Nc
    }
  }

  /**
   * Writes a string to the bytes array.
   * @param s the string to write
   * @param pos the position in the array to start
   * @param zeroTerminate true to add a zero byte after the end of the string, false to add nothing
   */
  private void writeString(String s,int pos,boolean zeroTerminate)
  {
    writeString(s,pos,zeroTerminate,0,0);
  }

  /**
   * Writes a string to the bytes array with some padding at the end
   * @param s the string to write
   * @param pos the position in the array to start
   * @param zeroTerminate true to add a zero byte after the end of the string, false to add nothing
   * @param len the total length of the space allocated
   * @param pad the character to pad at the end if the string doesn't use all the allocated space
   */
  private void writeString(String s,int pos,boolean zeroTerminate,int len,int pad)
  {
    byte[] b=s.getBytes();
    System.arraycopy(b,0,bytes,pos,b.length);
    int padStart=b.length;
    if (zeroTerminate)
      bytes[pos+(padStart++)]=(byte)0;
    for(int i=padStart;i<len;i++)
      bytes[pos+i]=(byte)pad;
  }

  /**
   * Loads the specified bitmap icon into the bytes array.
   * @param path the path to the bitmap
   */
  private void loadBmpIcon(String path)
  {
    // the number of image bits in each byte of the icon
    // there should be ICON_BYTES_PER_ROW values in this array
    // and the total should equal the width of the icon (22 usually)
    int v[] = { 3, 8, 8, 3 };
    try
    {
      DataInputStream dis=new DataInputStream(new FileInputStream(path));
      // validate bmp image
      if (!Utils.readString(dis,2).equals("BM"))
        Exegen.errExit(path+" is not a BMP file", -1);  // !!!Nc
      dis.skipBytes(8);

      int bmpBitmapOffset = Utils.readReverseInt(dis);
      int infoSize = Utils.readReverseInt(dis);
      if (infoSize != 40)
        Exegen.errExit("Unsupported old-style BMP format", -1);  // !!!Nc

      int width = Utils.readReverseInt(dis);
      int height = Utils.readReverseInt(dis);
      if (width != 22 || height != 22)
        Exegen.errExit(".bmp icon file is not 22x22 ("+width+","+height+")", -1); // !!!Nc
      dis.skipBytes(2);
      short bpp = Utils.readReverseShort(dis);
      if (bpp != 1)
        Exegen.errExit("BMP image is not black and white (1 bit per pixel)", -1); // !!!Nc
      int compression = Utils.readReverseInt(dis);
      if (compression != 0)
        Exegen.errExit("BMP uses unsupported compression type", -1); // !!!Nc
      dis.skipBytes(12);
      int colorsUsed = Utils.readReverseInt(dis);
      if (colorsUsed > 2)
        Exegen.errExit("BMP contains a color map", -1);  // !!!Nc
      dis.skipBytes(4);
//System.out.println("PrcIcon");
//System.out.println("bitmapOffset: " + bmpBitmapOffset + "; numColors: " + colorsUsed);

      // colorsUsed == 2
      // determine if colors in colormap are reversed
      int col1 = Utils.readReverseInt(dis);
      int col2 = Utils.readReverseInt(dis);
      boolean colorsReversed = (col1 == 0 && col2 != 0);
//System.out.println("invert: " + colorsReversed + ' ' + col1 + ' ' + col2);
      dis.skipBytes(bmpBitmapOffset-62);
      //... 7 255 255 224 ... ( 22 rows)

      int bmpScanLen = (width + 7) / 8; // # bytes
      bmpScanLen = ((bmpScanLen + 3) / 4) * 4; // end on 32 bit boundry

      int bitmapPos = bitmapOffset+(ICON_ROWS-1)*ICON_BYTES_PER_ROW;
      for (int y = 0; y < ICON_ROWS; y++)
      {
        int bbit = 7;
        int bbyte = dis.readUnsignedByte();
        if (colorsReversed)
          bbyte^=0xFF;
        int bbyteCnt=1;
        for (int col = 0; col < ICON_BYTES_PER_ROW; col++)
        {
          int obyte = 0;
          for (int vi = 0; vi < v[col]; vi++)
          {
            obyte<<=1;
            if (((1 << bbit) & bbyte) != 0)
              obyte |= 0x01;
            if (bbit == 0)
            {
              bbit = 7;
              bbyte=dis.readUnsignedByte();
              bbyteCnt++;
              if (colorsReversed)
                bbyte^=0xFF;
            }
            else
              bbit--;
          }
          if (col == ICON_BYTES_PER_ROW-1)
            obyte<<=(8-v[col]);
          bytes[bitmapPos+col] = (byte)obyte;
        }
        bitmapPos-=ICON_BYTES_PER_ROW;
        dis.skipBytes(bmpScanLen-bbyteCnt);
      }
    }
    catch (FileNotFoundException fnfe)
    {
      Exegen.errExit("can't read file "+path, -1); // !!!Nc
    }
    catch (IOException ioe)
    {
      Exegen.errExit("problem reading file "+path, -1); // !!!Nc
    }
  }

  /**
   * Extract the contents of this .prc file as parameters and icon
   */
  public String list (boolean extract) { // !!!Nb
    String result = "";
    try {
      DataInputStream dis = new DataInputStream(new FileInputStream(prcPath));

      dis.skipBytes(launchOffset);
      result = Utils.readCString(dis,80);
System.out.println(".prc params: " + result);

      if (extract) {
        dis.skipBytes(bitmapOffset - (launchOffset + result.length() + 1));
        int bhd = 62, blen = ICON_BYTES_PER_ROW * ICON_ROWS;
        // no need for 'reverse' for 0 or known hex values
        DataOutputStream bmp = new DataOutputStream(new FileOutputStream("icon.bmp"));
        bmp.writeBytes("BM");                 //  0: [2] signature
        Utils.writeReverseInt(bmp, bhd+blen); //  2: [4] total file size (=62+88)
        bmp.writeInt(0);                      //  6: [4] reserved(=0)
        Utils.writeReverseInt(bmp, bhd);      // 10: [4] offset(=62)
        Utils.writeReverseInt(bmp, 40);       // 14: [4] info(=40)
        Utils.writeReverseInt(bmp, 22);       // 18: [4] width(=22)
        Utils.writeReverseInt(bmp,ICON_ROWS); // 22: [4] height(=22)
        Utils.writeReverseShort(bmp, 1);      // 26: [2] planes(=1)
        Utils.writeReverseShort(bmp, 1);      // 28: [2] depth(=1)
        bmp.writeInt(0);                      // 30: [4] compression(=0)
        Utils.writeReverseInt(bmp, blen);     // 34: [4] data len(=88)
        bmp.writeInt(0);                      // 38: [4] x pixelsPerMeter
        bmp.writeInt(0);                      // 42: [4] y pixelsPerMeter
        Utils.writeReverseInt(bmp, 2);        // 46: [4] colors used
        bmp.writeInt(0);                      // 50: [4] color important?
        bmp.writeInt(0);                      // 54: [4] col1 colorTable (blue,red,green,reserved)
        bmp.writeInt(0xFFFFFF00);             // 58: [4] col2
                                              // 62(=bhd) [blen=88]: data
        // fix data: flipVertical(bot<->top), center(<<5), invert(b&w), reverse(endian)
        byte[] bmpb = new byte[blen];
        int row;
        for (int i=ICON_ROWS-1; i>=0; i--) {
          row = (dis.readInt() << 5) ^ 0xFFFFFFFF;
          for (int j=3; j>=0; j--) {
            bmpb[i*ICON_BYTES_PER_ROW + j] = (byte) row;
            row = row >> 8;
          };
        };
        bmp.write(bmpb);
        bmp.close();

        dis.skipBytes(nameOffset - (bitmapOffset + blen));
        //System.out.println("name: " + Utils.readCString(dis,30));
      };
      dis.close();
    }
    catch (FileNotFoundException fnfe)
    {
      Exegen.errExit("can't open file "+prcPath, -1); // !!!Nc
    }
    catch (IOException e)
    {
      Exegen.errExit("problem reading from file "+prcPath, -1); // !!!Nc
    };
    return result;
  }
}