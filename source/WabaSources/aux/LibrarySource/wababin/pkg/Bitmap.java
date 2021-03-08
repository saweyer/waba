package wababin.pkg;

/**
 * @author  <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>,
 * @version 1.50.0N
 * 1.50.0Nc 10 Feb 2001 first version
 *
 * invert code to coordinate
 *   WabaTester.convertBitmap
 *   wababin/pkg/Bitmap.convertBitmap
 *   wababin/PrcFile.list, .loadBmpIcon
 *   waba/fx/Image.readBMP
 *   (this stuff was confusing!)
**/
public class Bitmap extends Frame {
  static final int headLen1x = 16;
  static final int headLen2x = 28;

  int headerOffset;     // 1.x/icon = headLen1x; 2.x = headLen2x
  int shareBounds = 0;  // > 0 if sharing existing boundsRect
  byte[] bits;          // converted bits
  int blen;             // bits.length (or result of bitmapSize)
  int itmp = -1;

//  protected Bitmap() {}

  public Bitmap(byte[] bmp) { // used by writeFileList for just objSize
    headerOffset = headLen2x;
    vlen = 5;
    // shareBounds        // ? share boundsrect
    blen = bitmapSize(bmp);
  }

  public Bitmap(byte[] bmp, boolean icon, int it) { // used by icon(header) and writeRecord
    itmp = it;
    if (icon) {
      headerOffset = headLen1x;
      vlen = 2;   // bits(=Binary), bounds(=Frame)  but not actually used currently
    }
    else {
      headerOffset = headLen2x;
      vlen = 5; // class(='bitmap), bounds(=Binary), data(=Binary),colordata(=nil),mask(=nil)
      // shareBounds        // ? share boundsrect
    };
    bits = convertBitmap(bmp, icon);
    blen = bits.length;
  }

  public void stuffBits(byte[] data, int offset) {
    if (headerOffset == headLen1x && blen == 128) // only used for icon (30x28)
      PkgUtils.StuffBytes(data, offset+Binary.HEADERSIZE+headLen1x, blen-headLen1x, bits, headLen1x);
  }

  // default chunkSize ok since there's no padding on any of these objects
  public int objSize() {
    if (headerOffset == headLen2x)
      return super.objSize() +                              // frame itself
        ((shareBounds>0) ? 0 : new Binary(8,0).objSize()) + // bounds (unless shared). cl not used
        new Binary(blen,0).objSize();                       // bits. cl not used
System.err.println("Bitmap.objSize -- 1.x?");
    return 0;
  }

  public void stuffHeader(byte[] data, int offset, int cl) {
System.err.println("Bitmap.stuffHeader ?");
  }

  // offset is within tmpData buffer. pkgOffset for creating proper pointers in pkg
  public void stuff(byte[] data, int offset, int pkgOffset) {
    int frameSize = super.objSize();
//PkgUtils.debugVal(pkgOffset+offset, "\tbitmap frame", frameSize);
    Binary boundsObj = null;
    Binary dataObj = new Binary(bits, PkgUtils.objRef(PkgUtils.pixelsSymbol[itmp]));
    int boundsOffset = offset+frameSize, dataOffset = boundsOffset;
    if (shareBounds > 0)
      boundsOffset = shareBounds; // share bounds
    else { // new bounds object
      boundsObj = new Binary(PkgUtils.ExtractBytes(bits,8,8),
        PkgUtils.objRef(PkgUtils.boundsrectSymbol[itmp]));
      dataOffset += boundsObj.objSize();
//PkgUtils.debugVal(pkgOffset+boundsOffset, "\tboundsObj", boundsObj.objSize());
    };
//PkgUtils.debugVal(pkgOffset+dataOffset, "\tdataObj", dataObj.objSize());

    // write bitmap frame, with refs to bounds and data binaries
    // don't use regular stuffHeader in order to parameterize objSize
    stuffHeader(data, offset, PkgUtils.objRef(PkgUtils.bitmapFrameMap[itmp]), frameSize); // size of bitmap frame only
    stuffVal(data, offset, 0, PkgUtils.objRef(PkgUtils.bitmapSymbol[itmp]));  // class:
    stuffVal(data, offset, 1, PkgUtils.objRef(pkgOffset+boundsOffset)); // bounds:
    stuffVal(data, offset, 2, PkgUtils.objRef(pkgOffset+dataOffset));   // data:
    stuffVal(data, offset, 3, PkgUtils.NIL_REF);              // colordata:
    stuffVal(data, offset, 4, PkgUtils.NIL_REF);              // mask:

    if (boundsObj != null)
      boundsObj.stuff(data, boundsOffset);
    dataObj.stuff(data, dataOffset);
  }

  public int bitmapSize(byte[] data) {
    // given a file length, calculate the size of final # of bits
    // without actually allocating or converting bits. assume !icon
    int offset = 0;
    int width  = PkgUtils.ExtractRLong(data, offset+18);
    int height = PkgUtils.ExtractRLong(data, offset+22);
    int depth  = PkgUtils.ExtractRWord(data, offset+28); // gray. other fields??
    int rowBytes = 4*((width*depth + 31) / 32);
    return headerOffset + (rowBytes*height);
  }

  public byte[] convertBitmap (byte[] data, boolean icon) {
    int offset = 0, len = data.length;

    int depth = PkgUtils.ExtractRWord(data, offset+28);	// gray. other fields??
    if ((icon && depth != 1) || ! PkgUtils.ExtractString(data,offset,2).equals("BM"))
      return null;

    //+0 = "BM"
    //+2L = len
    //+6L = 0 reserved
    int doffset = PkgUtils.ExtractRLong(data, offset+10);
    //+14L infoSize=40
    int width = PkgUtils.ExtractRLong(data, offset+18);
    int height = PkgUtils.ExtractRLong(data, offset+22);
    //+26W = 0 planes
    //+28L depth [see above]
    //+30L compression=0
    int dlen = PkgUtils.ExtractRLong(data, offset+34);
    //+38L  x pixelspermeter
    //+42L  y pixelspermeter
    //+46L colorsUsed=2
    int colorsUsed = 1 << depth;
//System.out.println("Bitmap");
//System.out.println("bitmapOffset: " + doffset + "; numColors: " + colorsUsed);
    //+50L colorImportant?
    //+54L col1
    int col1 = PkgUtils.ExtractRLong(data, offset+54);
    int col2 = PkgUtils.ExtractRLong(data, offset+58);
    //+58L col2 (blue,green,red,reserved) 255,255,255,0

    // always invert!?
    boolean invert = (colorsUsed == 2); // && col1 == 0 && col2 != 0);
//System.out.println("invert: " + invert + ' ' + col1 + ' ' + col2);

    if (dlen  == 0)
      dlen = ((len-doffset) / 4) * 4;
    if (icon) {
      width = 30;
      height = 28;
    };
    int rowBytes = 4*((width*depth + 31) / 32);
    byte[] b = new byte[headerOffset + (rowBytes*height)];
    for (int i=0; i < b.length; i++) // zero (just in case?)
      b[i] = 0;
    if (! icon) {
      // fix 2.x header
      PkgUtils.StuffWord(b,  2,headerOffset);// scanOffset
      PkgUtils.StuffByte(b, 16, 128);	// ?
      PkgUtils.StuffByte(b, 18,  16);	// ?
      PkgUtils.StuffByte(b, 19, depth);
      PkgUtils.StuffWord(b, 22, 72);	// resX
      PkgUtils.StuffWord(b, 24, 72);	// resY
    };
    PkgUtils.StuffWord(b,  4, rowBytes);
    PkgUtils.StuffWord(b, 12, height);	//8=top(0),10=left(0)
    PkgUtils.StuffWord(b, 14, width);

    int i, j, bt, btop = headerOffset, dtop = offset + doffset + dlen - rowBytes;

    for (i=0; i<dlen; i+=rowBytes)
      for (j=0; j<rowBytes; j++) {
        bt = PkgUtils.ExtractByte(data, dtop-i+j);
        PkgUtils.StuffByte(b, btop+i+j, (invert) ? bt ^ 0xFF : bt);
      };

    // and if it's an icon (22x22) we should center it and add "W" (or whatever) to right bottom corner (30x28)
    if (icon) {
      // center orig dev 22x22 icon by shifting 3+1 right; (was 3+2) ?mask rightmost 5
      for (i=btop; i<btop+dlen; i+=4)
        PkgUtils.StuffLong(b, i, (PkgUtils.ExtractLong(b,i) >>> 4) & 0x0FFFFFC0);

      // now, Bor the "W" (it might overlap ~2 bottom rows of original icon)
      int wlen = wabaBits.length, boff = b.length-wlen;

      // copy last 2 bytes of each row, copy 5 bits into byte2, store 8 bits into byte3
      for (i=2; i<wlen; i+=4) {
        PkgUtils.StuffByte(b, boff+i,   PkgUtils.ExtractByte(wabaBits, i) | PkgUtils.ExtractByte(b,boff+i) & 0xE0);
        PkgUtils.StuffByte(b, boff+i+1, PkgUtils.ExtractByte(wabaBits, i+1));
      };
    };
  return b;
  }

  static final byte[] wabaBits = { // 10 rows
	(byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0xFC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0xFC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0xEC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1B, (byte) 0xEC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1D, (byte) 0x5C,
        (byte) 0x00, (byte) 0x00, (byte) 0x1D, (byte) 0x5C,
        (byte) 0x00, (byte) 0x00, (byte) 0x1E, (byte) 0xBC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1E, (byte) 0xBC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0xFC,
        (byte) 0x00, (byte) 0x00, (byte) 0x1F, (byte) 0xFC,
  };
}
