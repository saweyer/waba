// This version by Sean Luke, for use in autogenerating code the hash numbers and other code for part
// of wabanewt_c.cp

// original version by Guilherme Hazan, a very nice job!

import java.awt.*;
import java.applet.*;
import sun.io.*;
import java.util.*;

class Stuff { public String cn; public String mn; public String mp; public String nf; }

public class MethodsHash 
{

   static long genHashCode(String name)
   {
      long value =0;
      char []chars = name.toCharArray();
      for (int i = 0; i < chars.length; i++)
          value += (byte)chars[i];
      value = (value << 6) + chars.length;
      return value;
   }
   static Long nativeHash(String className, String methodName, String methodDesc)
   {
      long classHash = genHashCode(className) % 65536;
      long methodHash = (genHashCode(methodName) + genHashCode(methodDesc)) % 65536;
   	long l= (classHash << 16) + methodHash;
        return new Long(l);
   	//return(className+"_"+methodName+"_"+methodDesc+" -> "+l);
   }

    
   static void buildNativeTable(TreeMap map, String classname, String methodName, String methodParams, String nativeFunc)
    {
    Stuff stuff = new Stuff();
    stuff.cn = classname;
    stuff.mn = methodName;
    stuff.mp = methodParams;
    stuff.nf = nativeFunc;
    map.put(nativeHash(classname,methodName,methodParams),stuff);
    }
    
    static void printTable(TreeMap map)
        {
        Object[] keys = map.keySet().toArray();
        for(int x=0;x<keys.length;x++)
            System.out.println("        case " + x + ": ret = vm->" +
                ((Stuff)(map.get(keys[x]))).nf+"(stack); break;     // " + 
                    ((Stuff)(map.get(keys[x]))).cn + "." + ((Stuff)(map.get(keys[x]))).mn + ((Stuff)(map.get(keys[x]))).mp);
        for(int x=0;x<keys.length;x++)
            System.out.println("    { " + ((Long)keys[x]).longValue() + ", " + x + " },     // " +
                    ((Stuff)(map.get(keys[x]))).cn + "." + ((Stuff)(map.get(keys[x]))).mn + ((Stuff)(map.get(keys[x]))).mp + 
                    "     (" + ((Stuff)(map.get(keys[x]))).nf + ")");
        }
   
   static String toHex(byte b)
   {
      if (b < 0) b += 256;
      String s = Integer.toHexString((int)b);
      return s.length()>4?s.substring(6):s;
   }
   public static void main(String []argv)
   { 
TreeMap map = new TreeMap();


buildNativeTable(map,"waba/fx/Graphics", "drawRect", "(IIII)V", "GraphicsDrawRect");
buildNativeTable(map,"waba/fx/Graphics", "drawPolygon", "([I[II)V", "GraphicsDrawPolygon");
buildNativeTable(map,"waba/fx/Graphics", "fillPolygon", "([I[II)V", "GraphicsFillPolygon");
buildNativeTable(map,"waba/fx/Graphics", "drawArc", "(IIIIII)V", "GraphicsDrawArc");
buildNativeTable(map,"waba/fx/Graphics", "fillArc", "(IIIIII)V", "GraphicsFillArc");


buildNativeTable(map,"waba/fx/Font", "nameForUse", "(I)Ljava/lang/String;", "FontNameForUse");
buildNativeTable(map,"waba/fx/Font", "sizeForUse", "(I)I", "FontSizeForUse");  
buildNativeTable(map,"waba/fx/Font", "styleForUse", "(I)I", "FontSizeForUse"); 
buildNativeTable(map,"waba/fx/Font", "isValidFont", "(Ljava/lang/String;II)Z", "FontIsValidFont");
buildNativeTable(map,"waba/fx/Font", "getNames", "()[Ljava/lang/String;", "FontGetNames");
buildNativeTable(map,"waba/fx/Font", "getSizes", "(Ljava/lang/String;)[I", "FontGetSizes");
buildNativeTable(map,"waba/fx/Font", "getStyles", "(Ljava/lang/String;I)[I", "FontGetStyles");
 buildNativeTable(map,"waba/sys/Vm","exec","(Ljava/lang/String;Ljava/lang/String;IZ)I", /*113969325*/ "VmExec");
buildNativeTable(map,"waba/sys/Vm","getTimeStamp","()I", /*113990543*/ "VmGetTimeStamp");
buildNativeTable(map,"waba/sys/Vm","copyArray","(Ljava/lang/Object;ILjava/lang/Object;II)Z", /*114004019*/ "copyArray");
buildNativeTable(map,"waba/sys/Vm","sleep","(I)V", /*114016841*/ "VmSleep");
buildNativeTable(map,"waba/sys/Vm","setDeviceAutoOff","(I)I", /*114019540*/ "VmSetDeviceAutoOff");
buildNativeTable(map,"waba/sys/Vm","getUserName","()Ljava/lang/String;", /*114021471*/ "VmGetUserName");
buildNativeTable(map,"waba/sys/Vm","getPlatform","()Ljava/lang/String;", /*114023839*/ "VmGetPlatform");
buildNativeTable(map,"waba/sys/Vm","isColor","()Z", /*114024842*/ "VmIsColor");
buildNativeTable(map,"waba/io/File","getLength","()I", /*340528908*/ "FileGetLength");
buildNativeTable(map,"waba/io/File","createDir","()Z", /*340529036*/ "FileCreateDir");
buildNativeTable(map,"waba/io/File","readBytes","([BII)I", /*340548368*/ "FileRead");
buildNativeTable(map,"waba/io/File","rename","(Ljava/lang/String;)Z", /*340553947*/ "FileRename");
buildNativeTable(map,"waba/io/File","_nativeCreate","()V", /*340555856*/ "FileCreate");
buildNativeTable(map,"waba/io/File","writeBytes","([BII)I", /*340557521*/ "FileWrite");
buildNativeTable(map,"waba/io/File","listDir","()[Ljava/lang/String;", /*340560348*/ "FileListDir");
buildNativeTable(map,"waba/io/File","seek","(I)Z", /*340567816*/ "FileSeek");
buildNativeTable(map,"waba/io/File","isDir","()Z", /*340568456*/ "FileIsDir");
buildNativeTable(map,"waba/io/File","close","()Z", /*340570184*/ "FileClose");
buildNativeTable(map,"waba/io/File","isOpen","()Z", /*340575817*/ "FileIsOpen");
buildNativeTable(map,"waba/io/File","delete","()Z", /*340576137*/ "FileDelete");
buildNativeTable(map,"waba/io/File","exists","()Z", /*340579017*/ "FileExists");
buildNativeTable(map,"waba/fx/Image","_nativeCreate","()V", /*781023312*/ "ImageCreate");
buildNativeTable(map,"waba/fx/Image","free","()V", /*781029959*/ "ImageFree");
buildNativeTable(map,"waba/fx/Image","setPixels","(I[IIII[B)V", /*781038420*/ "ImageSetPixels");
buildNativeTable(map,"waba/fx/Image","_nativeLoad","(Ljava/lang/String;)V", /*781052768*/ "ImageLoad");
buildNativeTable(map,"waba/fx/Sound","beep","()V", /*940413127*/ "SoundBeep");
buildNativeTable(map,"waba/fx/Sound","tone","(II)V", /*940424137*/ "SoundTone");
buildNativeTable(map,"waba/sys/Time","_nativeCreate","()V", /*969766992*/ "TimeCreate");
buildNativeTable(map,"waba/io/Socket","_nativeCreate","(Ljava/lang/String;I)V", /*1317941923*/ "SocketCreate");
buildNativeTable(map,"waba/io/Socket","readBytes","([BII)I", /*1317952272*/ "SocketRead");
buildNativeTable(map,"waba/io/Socket","writeBytes","([BII)I", /*1317961425*/ "SocketWrite");
buildNativeTable(map,"waba/io/Socket","setReadTimeout","(I)Z", /*1317972178*/ "SocketSetReadTimeout");
buildNativeTable(map,"waba/io/Socket","close","()Z", /*1317974088*/ "SocketClose");
buildNativeTable(map,"waba/io/Socket","isOpen","()Z", /*1317979721*/ "SocketIsOpen");
buildNativeTable(map,"waba/ui/Window","_nativeCreate","()V", /*1406040144*/ "WindowCreate");
buildNativeTable(map,"waba/io/Catalog","listCatalogs","()[Ljava/lang/String;", /*1661930913*/ "CatalogListCatalogs");
buildNativeTable(map,"waba/io/Catalog","addRecord","(I)I", /*1661934285*/ "CatalogAddRecord");
buildNativeTable(map,"waba/io/Catalog","skipBytes","(I)I", /*1661937741*/ "CatalogSkipBytes");
buildNativeTable(map,"waba/io/Catalog","_nativeCreate","(Ljava/lang/String;I)V", /*1661940387*/ "CatalogCreate");
buildNativeTable(map,"waba/io/Catalog","readBytes","([BII)I", /*1661950736*/ "CatalogRead");
buildNativeTable(map,"waba/io/Catalog","deleteRecord","()Z", /*1661951823*/ "CatalogDeleteRecord");
buildNativeTable(map,"waba/io/Catalog","setRecordPos","(I)Z", /*1661957200*/ "CatalogSetRecordPos");
buildNativeTable(map,"waba/io/Catalog","getRecordSize","()I", /*1661957392*/ "CatalogGetRecordSize");
buildNativeTable(map,"waba/io/Catalog","resizeRecord","(I)Z", /*1661958480*/ "CatalogResizeRecord");
buildNativeTable(map,"waba/io/Catalog","writeBytes","([BII)I", /*1661959889*/ "CatalogWrite");
buildNativeTable(map,"waba/io/Catalog","getRecordCount","()I", /*1661964433*/ "CatalogGetRecordCount");
buildNativeTable(map,"waba/io/Catalog","close","()Z", /*1661972552*/ "CatalogClose");
buildNativeTable(map,"waba/io/Catalog","isOpen","()Z", /*1661978185*/ "CatalogIsOpen");
buildNativeTable(map,"waba/io/Catalog","delete","()Z", /*1661978505*/ "CatalogDelete");
buildNativeTable(map,"waba/fx/Graphics","copyRect","(Lwaba/fx/ISurface;IIIIII)V", /*2182088099*/ "GraphicsCopyRect");
buildNativeTable(map,"waba/fx/Graphics","clearClip","()V", /*2182090124*/ "GraphicsClearClip");
buildNativeTable(map,"waba/fx/Graphics","setFont","(Lwaba/fx/Font;)V", /*2182094808*/ "GraphicsSetFont");
buildNativeTable(map,"waba/fx/Graphics","setDrawOp","(I)V", /*2182095437*/ "GraphicsSetDrawOp");
buildNativeTable(map,"waba/fx/Graphics","setClip","(IIII)V", /*2182096846*/ "GraphicsSetClip");
buildNativeTable(map,"waba/fx/Graphics","setColor","(III)V", /*2182099790*/ "GraphicsSetColor");
buildNativeTable(map,"waba/fx/Graphics","getClip","(Lwaba/fx/Rect;)Lwaba/fx/Rect;", /*2182102117*/ "GraphicsGetClip");
buildNativeTable(map,"waba/fx/Graphics","fillRect","(IIII)V", /*2182103055*/ "GraphicsFillRect");
buildNativeTable(map,"waba/fx/Graphics","drawRect","(IIII)V", /*2182103055*/ "GraphicsDrawRect");
buildNativeTable(map,"waba/fx/Graphics","drawLine","(IIII)V", /*2182103119*/ "GraphicsDrawLine");
buildNativeTable(map,"waba/fx/Graphics","translate","(II)V", /*2182103502*/ "GraphicsTranslate");
buildNativeTable(map,"waba/fx/Graphics","drawDots","(IIII)V", /*2182104271*/ "GraphicsDrawDots");
buildNativeTable(map,"waba/fx/Graphics","drawText","([CIIII)V", /*2182115089*/ "GraphicsDrawChars");
buildNativeTable(map,"waba/fx/Graphics","_nativeCreate","()V", /*2182117456*/ "GraphicsCreate");
buildNativeTable(map,"waba/fx/Graphics","drawCursor","(IIII)V", /*2182118865*/ "GraphicsDrawCursor");
buildNativeTable(map,"waba/fx/Graphics","setClipRect","(IIII)V", /*2182122322*/ "GraphicsSetClip");
buildNativeTable(map,"waba/fx/Graphics","free","()V", /*2182124103*/ "GraphicsFree");
buildNativeTable(map,"waba/fx/Graphics","fillPolygon","([I[II)V", /*2182132179*/ "GraphicsFillPolygon");
buildNativeTable(map,"waba/fx/Graphics","drawPolygon","([I[II)V", /*2182132179*/ "GraphicsDrawPolygon");
buildNativeTable(map,"waba/fx/Graphics","drawText","(Ljava/lang/String;II)V", /*2182138655*/ "GraphicsDrawString");
buildNativeTable(map,"waba/sys/Convert","toInt","(Ljava/lang/String;)I", /*2387628570*/ "ConvertStringToInt");
buildNativeTable(map,"waba/sys/Convert","toIntBitwise","(F)I", /*2387636560*/ "ConvertFloatToIntBitwise");
buildNativeTable(map,"waba/sys/Convert","toString","(C)Ljava/lang/String;", /*2387649437*/ "ConvertCharToString");
buildNativeTable(map,"waba/sys/Convert","toFloatBitwise","(I)F", /*2387649554*/ "ConvertIntToFloatBitwise");
buildNativeTable(map,"waba/sys/Convert","toString","(F)Ljava/lang/String;", /*2387649629*/ "ConvertFloatToString");
buildNativeTable(map,"waba/sys/Convert","toString","(I)Ljava/lang/String;", /*2387649821*/ "ConvertIntToString");
buildNativeTable(map,"waba/sys/Convert","toString","(Z)Ljava/lang/String;", /*2387650909*/ "ConvertBooleanToString");
buildNativeTable(map,"waba/fx/SoundClip","play","()Z", /*2584844359*/ "SoundClipPlay");
buildNativeTable(map,"waba/ui/MainWindow","_nativeCreate","()V", /*3037886544*/ "MainWinCreate");
buildNativeTable(map,"waba/ui/MainWindow","exit","(I)V", /*3037899400*/ "MainWinExit");
buildNativeTable(map,"waba/ui/MainWindow","_setTimerInterval","(I)V", /*3037919317*/ "MainWinSetTimerInterval");
buildNativeTable(map,"waba/io/SerialPort","readCheck","()I", /*3046245644*/ "SerialPortReadCheck");
buildNativeTable(map,"waba/io/SerialPort","readBytes","([BII)I", /*3046267664*/ "SerialPortRead");
buildNativeTable(map,"waba/io/SerialPort","writeBytes","([BII)I", /*3046276817*/ "SerialPortWrite");
buildNativeTable(map,"waba/io/SerialPort","setReadTimeout","(I)Z", /*3046287570*/ "SerialPortSetReadTimeout");
buildNativeTable(map,"waba/io/SerialPort","close","()Z", /*3046289480*/ "SerialPortClose");
buildNativeTable(map,"waba/io/SerialPort","setFlowControl","(Z)Z", /*3046290066*/ "SerialPortSetFlowControl");
buildNativeTable(map,"waba/io/SerialPort","isOpen","()Z", /*3046295113*/ "SerialPortIsOpen");
buildNativeTable(map,"waba/io/SerialPort","_nativeCreate","(IIIZI)V", /*3046299605*/ "SerialPortCreate");
buildNativeTable(map,"waba/fx/FontMetrics","getTextWidth","(Ljava/lang/String;)I", /*3511879649*/ "FontMetricsGetStringWidth");
buildNativeTable(map,"waba/fx/FontMetrics","getCharWidth","(C)I", /*3511903952*/ "FontMetricsGetCharWidth");
buildNativeTable(map,"waba/fx/FontMetrics","_nativeCreate","()V", /*3511908432*/ "FontMetricsCreate");
buildNativeTable(map,"waba/fx/FontMetrics","getTextWidth","([CII)I", /*3511921619*/ "FontMetricsGetCharArrayWidth");


// my stuff
buildNativeTable(map,"newton/NS","proto","(Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","NSProto");
buildNativeTable(map,"newton/NS","object","(ILnewton/Ref;)Lnewton/Ref;","NSObject");
buildNativeTable(map,"java/lang/Object","hashCode","()I","ObjectHashCode");
buildNativeTable(map,"waba/sys/Vm","getClassName","(Ljava/lang/Object;)Ljava/lang/String;","VmGetClassName");
buildNativeTable(map,"waba/sys/Vm","makeInstance","(Ljava/lang/String;)Ljava/lang/Object;","VmMakeInstance");
buildNativeTable(map,"newton/Ref","type","()I","RefType");
buildNativeTable(map,"newton/Ref","buildNil","()V","RefBuildNil");
buildNativeTable(map,"newton/Ref","buildInt","(I)V","RefBuildInt");
buildNativeTable(map,"newton/Ref","buildChar","(C)V","RefBuildChar");
buildNativeTable(map,"newton/Ref","buildString","(Ljava/lang/String;)V","RefBuildString");
buildNativeTable(map,"newton/Ref","buildSymbol","(Ljava/lang/String;)V","RefBuildSymbol");
buildNativeTable(map,"newton/Ref","buildFloat","(F)V","RefBuildFloat");
buildNativeTable(map,"newton/Ref","buildArray","(I)V","RefBuildArray");
buildNativeTable(map,"newton/Ref","buildBinary","(ILnewton/Ref;)V","RefBuildBinary");
buildNativeTable(map,"newton/Ref","buildFrame","()V","RefBuildFrame");
buildNativeTable(map,"newton/Ref","intValue","()I","RefIntValue");
buildNativeTable(map,"newton/Ref","floatValue","()F","RefFloatValue");
buildNativeTable(map,"newton/Ref","charValue","()C","RefCharValue");
buildNativeTable(map,"newton/Ref","symbolValue","()Ljava/lang/String;","RefSymbolValue");
buildNativeTable(map,"newton/Ref","stringValue","()Ljava/lang/String;","RefStringValue");
buildNativeTable(map,"newton/Ref","getClass","(Lnewton/Ref;)Lnewton/Ref;","RefGetClass");
buildNativeTable(map,"newton/Ref","getSlot","(ILnewton/Ref;)Lnewton/Ref;","RefGetArraySlot");
buildNativeTable(map,"newton/Ref","getSlot","(Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","RefGetFrameSlot");
buildNativeTable(map,"newton/Ref","getPath","(Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","RefGetPath");
buildNativeTable(map,"newton/Ref","getBytes","(III[B)I","RefGetBytes");
buildNativeTable(map,"newton/Ref","getLength","()I","RefGetLength");
buildNativeTable(map,"newton/Ref","setBytes","(III[B)I","RefSetBytes");
buildNativeTable(map,"newton/Ref","setLength","(I)I","RefSetLength");
buildNativeTable(map,"newton/Ref","push","(Lnewton/Ref;)Lnewton/Ref;","RefPush");
buildNativeTable(map,"newton/Ref","setClass","(Lnewton/Ref;)Lnewton/Ref;","RefSetClass");
buildNativeTable(map,"newton/Ref","setSlot","(ILnewton/Ref;)Lnewton/Ref;","RefSetArraySlot");
buildNativeTable(map,"newton/Ref","setSlot","(Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","RefSetFrameSlot");
buildNativeTable(map,"newton/Ref","call","([Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","RefCall");
buildNativeTable(map,"newton/Ref","send","(Lnewton/Ref;[Lnewton/Ref;Lnewton/Ref;)Lnewton/Ref;","RefSend");
buildNativeTable(map,"newton/Callback","_func","(Lnewton/Ref;)Lnewton/Ref;","CallbackFunc");

printTable(map);
      System.exit(0);
   }
   
}
