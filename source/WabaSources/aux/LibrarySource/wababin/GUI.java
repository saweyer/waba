package wababin;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;   // Vector
import java.lang.reflect.Method;

/**
 * provides a plain but relatively complete UI for Warp/Exegen
 *
 * @author     <A HREF="mailto:weyer@kagi.com">Steve Weyer</A>
 * @version    1.0.0N
 * !!!Nc      12 Feb 2001: first version
 * !!!Nd      13 Feb 2001. find .pkg from .jar; 1.2 Runtime.exec
 * !!!Ne      15 Feb 2001. COMPILE12; usage & compileName, etc.; expandCheck
 * !!!Nf      24 Feb 2001. writeHTML (only non-default params); expand uses sun.etc.
**/

/*
?? use icon.bmp field (for possibly diff name) instead of checkbox

?? Palm: 'list icon'; version #

?? tried invoking javac, jar, appletviewer via sun.xxx directly but
?? ran into various classpath and security violations
   see main, usage, Button (label), actionPerformed

?? other checks/constraints on command params
(size of #s, strings, etc.)

?? 1.2 Vector.add, .clear, .elementAt, .toArray()
?? instead of .addElement, .removeAllElements, .get, .toArray([])
?? problems for VM < 1.2: RunTime.exec, Arrays.sort (Comparator)

?? Exegen warpFile (3rd name) is name of PDB (if different from PRC)
*/

public class GUI extends Frame
    implements ActionListener, ItemListener, FocusListener  // for: Button; Checkbox; TextField
  {
  public static final boolean COMPILE12 = false;
  static final boolean DEBUG = false;
  public static final String GUI_VERSION = "GUI 1.0.0Nf";
  public static final int APP_WIDTH = 640;
  public static final int APP_HEIGHT = 420;
  private boolean inAnApplet = true;
  private Method exec3 = null;

  private Button dirButton = null;
  private TextField mainClassText;
  // default: filename
  private TextField dirText = null;
  // default: dir
  private String origDirName = ""; // user.dir
  private String origJarName = "";

  private Choice cmdChoice;
  private final int createChoice = 0;
  private final int listChoice = 1;
  private final int extractChoice = 2;
  private final int helpChoice = 3;
  private final int defaultChoice = createChoice;
  private final String[] cmdChoices = {"create", "list", "extract", "help"};
  private TextField titleText;
  // default: dir
  private Checkbox verboseCheck;
  private final boolean verboseDefault = false;
  private Checkbox expandCheck;
  private final boolean expandDefault = false;
  private Button defaultsButton;

  private final String javaCommand = "java";
  private Button compileButton;
  private static String compileName = "javac";
  private static String compileClass = "sun.tools.javac.Main";
  private TextField compileText;
  private final String compileDefault = "*.java";
  private Checkbox debugCheck;
  private final boolean debugDefault = false;
  private Button runAppButton;
  private final String wabaAppletClass = "waba.applet.Applet";

  private Button jarButton;
  private static String jarName = "jar";
  private static String jarClass = "sun.tools.jar.Main";
  private Checkbox compressCheck;
  private final boolean compressDefault = false;
  private TextField jarText;
  private final String jarDefault = "*.class *.bmp";
  private Button htmlButton;
  private static String appletviewerName = "appletviewer";
  private static String appletviewerClass = "sun.applet.Main";

  private Checkbox palmCheck;
  private final boolean palmDefault = true;
  private Checkbox winceCheck;
  private final boolean winceDefault = true;
  private Checkbox pkgCheck;
  private final boolean pkgDefault = true;
  private Checkbox ntkCheck;
  private final boolean ntkDefault = false;

  private Button warpButton;
  private static String warpClass = "wababin.Warp";
  private Checkbox recurCheck;
  private final boolean recurDefault = false;
  private TextField warpText;
  private final String warpDefault = "*.class";
  private TextField creatorText;
  private final String creatorDefault = "pdb/prc CrEaToR (/c)";

  private Button exegenButton;
  private static String exegenClass = "wababin.Exegen";
  private Checkbox iconCheck;
  private final boolean iconDefault = true;
  private TextField cepathText;
  private final String cepathDefault = "\\\\Program Files\\CEpath (/p)";
  private Checkbox libCheck;
  private final boolean libDefault = false;

  // Exegen-related args -- note important order is coordinated
  private final int NUMARGS = 6;
  private TextField[] argTextObj;
  private final String[] argLabels = {  // for ui
    "width",        "height",
    "class size",   "object size",
    "stack size",   "native stack size",
  };
  // PkgFile.argAttribs
/*public static final String[] argAttribs = {  // for .htm
    "width",            "height",
    "classSize",        "objectSize",
    "vmStackSizeText",  "nmStackSize",
  };
*/
  // Exegen.argCmds
//public static final String[] argExegenCmds = {"/w", "/h", "/l", "/m", "/s", "/t"}; // for Exegen, AppletViewer(2)

  public final String[] argDefaults = { // for init/reset, checking
    "0",  // width
    "0",  // height
    Integer.toString(Exegen.DEFAULT_CLASS_HEAP_SIZE),
    Integer.toString(Exegen.DEFAULT_OBJECT_HEAP_SIZE),
    Integer.toString(Exegen.DEFAULT_STACK_SIZE),
    Integer.toString(Exegen.DEFAULT_NATIVE_STACK_SIZE),
  };

  public final String[] argHelp = {
    "width of application's main window (0 defaults to platform preference)",
    "height of application's main window (0 defaults to platform preference)",
    "size of class heap",
    "size of object heap",
    "size of stack",
    "size of native stack",
  };

  private Label statusLabel = null;
  private final String statusDefault =
"for short info, select help in command menu, then tap a button, checkbox or field";
  private Label cmdLabel;
  private final String cmdDefault =
"(for more info, also select verbose, and look in Java output window)";

  private final boolean SAMEROW = false;
  private final boolean ENDROW = true;

  public void addObj(Component obj, GridBagLayout gridbag, GridBagConstraints gbc, boolean rowFlag) {
    if (rowFlag) // end?
      gbc.gridwidth = GridBagConstraints.REMAINDER; //end row
    gridbag.setConstraints(obj, gbc);
    if (obj instanceof Button)
      ((Button) obj).addActionListener(this);
    else if (obj instanceof Checkbox)
      ((Checkbox) obj).addItemListener(this);
    else if (obj instanceof TextField)
      ((TextField) obj).addFocusListener(this);
    else if (obj instanceof Choice)
      ((Choice) obj).addItemListener(this);
    add(obj);
    if (rowFlag) {  // end? new row
      gbc.weightx = 0.0;		   //reset to the default
      gbc.gridwidth = 1;
    }
  }

  public static void main(String args[]) {
    int len = args.length;
    if (len > 0 && args[0].equals("/?")) {
      usage();
      return;
     }

    String cmd, arg;
    for (int i=0; i < len; i+=2) {  // !!!Ne
      cmd = args[i];
      if (cmd.startsWith("/") && cmd.length() > 1 && i+1 < len) {
        arg = args[i+1];
        switch (cmd.charAt(1)) {
          case 'c':
            compileName = arg;
            break;
          case 'j':
            jarName = arg;
            break;
          case 'a':
            appletviewerName = arg;
            break;
/*
          case 'C':
            compileClass = arg;
            break;
          case 'J':
            jarClass = arg;
            break;
          case 'A':
            appletviewerClass = arg;
            break;
          case 'W':
            warpClass = arg;
            break;
          case 'E':
            exegenClass = arg;
            break;
*/
          default:
            System.err.println("unknown arg: " + cmd);
            cmd = "";
        }
        if (cmd.length() == 0)
          break;
      }
      else {
        System.err.println("not arg or missing val: " + cmd);
        break;
      }
    }
    GUI window = new GUI();
    window.inAnApplet = false; // doesn't help during init
  }

  public static void usage() { // !!!Ne
    System.out.println("Usage: java wababin.GUI [options]");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  /?   usage information");
    System.out.println("  /c   Set name of compiler program; default: " + compileName);
    System.out.println("  /j   Set name of jar app program: " + jarName);
    System.out.println("  /a   Set name of appletviewer program; default: " + appletviewerName);
/*
    System.out.println("  /C   Set name of compiler class; default: " + compileClass);
    System.out.println("  /J   Set name of jar app class: " + jarClass);
    System.out.println("  /A   Set name of appletviewer class; default: " + appletviewerClass);
    System.out.println("  /W   Set name of Warp class; default: " + warpClass);
    System.out.println("  /E   Set name of Exegen class; default: " + exegenClass);
*/
}

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    switch (infoflags) {
      case ALLBITS:
        setIconImage(img);
      case ERROR:
      case ABORT:
        return false;
    }
    return true;  // SOMEBITS, FRAMEBITS, WIDTH, HEIGHT
  }

  public GUI() {
    setTitle("Waba App Gen Tool/UI (" + GUI_VERSION + ")" );
    java.net.URL imgURL = getClass().getResource("/wababin/GUI.gif");
    if (imgURL != null)
      prepareImage(getToolkit().getImage(imgURL), this);  // createImage slightly better but 1.2+
      // imageUpdate is called async. later, setIconImage

if (DEBUG) {
    Enumeration props = System.getProperties().propertyNames();
    String propName;
    while (props.hasMoreElements()) {
      propName = (String) (props.nextElement());
      System.out.print(propName + ": ");
      System.out.println(System.getProperty(propName));
    }
    System.out.println("File.separator: " + File.separator);
    System.out.println("File.pathSeparator: " + File.pathSeparator);

    System.out.println("app template: " + ClassLoader.getSystemResource("wababin/pkg/" + PkgFile.APPTEMPLATE_PKG));
//    System.setProperty("user.dir", "C:\\waba\\steve\\src_waba\\PFB");
};

    if (! COMPILE12)
      try {
        Class[] params = {argHelp.getClass(), argHelp.getClass(), Class.forName("java.io.File")}; //String[],String[],File
        exec3 = Runtime.getRuntime().getClass().getDeclaredMethod("exec", params);
      }
      catch (Exception ex) {
        System.out.println("Runtime.exec(String[],String[],File) not defined -- using JDK 1.2 version");
      };

    try {
      origDirName = System.getProperty("user.dir");
    }
    catch (RuntimeException ex) {
      // probably in an applet, though can't test that flag
      origDirName = "C:\\waba\\myApp";
    };

    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(4,4,4,4);
    setLayout(gridbag);

// === rows
    if (COMPILE12 || exec3==null)
      addObj(new Label("Main Class, Dir"), gridbag, gbc, SAMEROW);
    else
      addObj(dirButton = new Button("Main Class/Dir..."), gridbag, gbc, SAMEROW);

    String appTitle = Utils.strip(origDirName);
    addObj(mainClassText = new TextField(appTitle), gridbag, gbc, SAMEROW);

    if (COMPILE12 || exec3==null)
      addObj(new Label(origDirName), gridbag, gbc, SAMEROW);
    else
      addObj(dirText = new TextField(origDirName), gridbag, gbc, SAMEROW);

    addObj(defaultsButton= new Button("Reset Defaults"), gridbag, gbc, ENDROW);

// ===
    cmdChoice = new Choice();
    for (int i=0; i < cmdChoices.length; i++)
      cmdChoice.add(cmdChoices[i]);
    //helpCheck = new Checkbox("help?      Title:", helpDefault);
    cmdChoice.select(defaultChoice);
    addObj(cmdChoice, gridbag, gbc, SAMEROW);

    addObj(titleText = new TextField(appTitle), gridbag, gbc, SAMEROW);

    expandCheck = new Checkbox("expand *.?", expandDefault);
    addObj(expandCheck, gridbag, gbc, SAMEROW);

    verboseCheck = new Checkbox("verbose output?", verboseDefault);
    addObj(verboseCheck, gridbag, gbc, ENDROW);

// ===
    addObj(compileButton = new Button("Compile (" + compileName + ')'), gridbag, gbc, SAMEROW);

    debugCheck = new Checkbox("omit debug? (-g)", debugDefault);
    addObj(debugCheck, gridbag, gbc, SAMEROW);

    addObj(compileText = new TextField(compileDefault), gridbag, gbc, SAMEROW);

    addObj(runAppButton = new Button("Run App"), gridbag, gbc, ENDROW);

// ===
    addObj(jarButton = new Button("Jar (" + jarName + ')'), gridbag, gbc, SAMEROW);

    compressCheck = new Checkbox("compressed?", compressDefault);
    addObj(compressCheck, gridbag, gbc, SAMEROW);

    addObj(jarText = new TextField(jarDefault), gridbag, gbc, SAMEROW);

    addObj(htmlButton = new Button("HTML; (" + appletviewerName + ')' +
      ((COMPILE12 || exec3==null) ? "" : "...")), gridbag, gbc, ENDROW);

// ===
    palmCheck = new Checkbox("Palm? (/f 1)", palmDefault);
    addObj(palmCheck, gridbag, gbc, SAMEROW);

    winceCheck = new Checkbox("WinCE? (/f 2)", winceDefault);
    addObj(winceCheck, gridbag, gbc, SAMEROW);

    pkgCheck = new Checkbox("Newton(.pkg) (/f 4)?", pkgDefault);
    addObj(pkgCheck, gridbag, gbc, SAMEROW);

    ntkCheck = new Checkbox("Newton(NTK)? (/f 8)", ntkDefault);
    addObj(ntkCheck, gridbag, gbc, ENDROW);

// ===
    addObj(warpButton = new Button("Warp"), gridbag, gbc, SAMEROW);

    recurCheck = new Checkbox("recur dir? (/r)", recurDefault);
    addObj(recurCheck, gridbag, gbc, SAMEROW);

    warpText = new TextField(warpDefault);
    addObj(warpText, gridbag, gbc, SAMEROW);

    creatorText = new TextField(creatorDefault);
    addObj(creatorText, gridbag, gbc, ENDROW);

 // ===
    addObj(exegenButton = new Button("Exegen"), gridbag, gbc, SAMEROW);

    iconCheck = new Checkbox("icon.bmp? (/i)", iconDefault);
    addObj(iconCheck, gridbag, gbc, SAMEROW);

    cepathText = new TextField(cepathDefault);
    addObj(cepathText, gridbag, gbc, SAMEROW);

    libCheck = new Checkbox("Newton lib? (/f 16)", libDefault);
    addObj(libCheck, gridbag, gbc, ENDROW);

// === (3 rows): width, height, class size, object size, heap size, native stack size
    argTextObj = new TextField[NUMARGS];
    for (int i=0; i < NUMARGS; i++) {
      addObj(new Label(argLabels[i] + " (" + Exegen.argCmds[i] + ' ' + argDefaults[i] + ')'),
        gridbag, gbc, SAMEROW);
      addObj(argTextObj[i] = new TextField(argDefaults[i]), gridbag, gbc, (i % 2) == 1); // odd: ENDROW=true
    };

// ===
    addObj(statusLabel = new Label(statusDefault), gridbag, gbc, ENDROW);
// ===
    addObj(cmdLabel = new Label(cmdDefault), gridbag, gbc, ENDROW);

// ===
    //pack();
    setSize(APP_WIDTH, APP_HEIGHT);
    setVisible(true);
    addWindowListener(new DWAdapter());	// for close
    // note: as an applet, this generates a class cast exception...
  }

  public void resetDefaults() {
    String appTitle = Utils.strip(origDirName);

    mainClassText.setText(appTitle);
    if (!COMPILE12 && exec3 != null)
      dirText.setText(origDirName);

    cmdChoice.select((inAnApplet) ? helpChoice : defaultChoice);
    titleText.setText(appTitle);
    verboseCheck.setState(verboseDefault);
    expandCheck.setState(expandDefault);

    debugCheck.setState(debugDefault);
    compileText.setText(compileDefault);

    compressCheck.setState(compressDefault);
    jarText.setText(jarDefault);

    palmCheck.setState(palmDefault);
    winceCheck.setState(winceDefault);
    pkgCheck.setState(pkgDefault);
    ntkCheck.setState(ntkDefault);

    recurCheck.setState(recurDefault);
    warpText.setText(warpDefault);
    creatorText.setText(creatorDefault);

    iconCheck.setState(iconDefault);
    cepathText.setText(cepathDefault);
    libCheck.setState(libDefault);

    for (int i=0; i < NUMARGS; i++)
      argTextObj[i].setText(argDefaults[i]);

    statusLabel.setText(statusDefault);
    cmdLabel.setText(cmdDefault);
  }

  public void itemStateChanged(ItemEvent ev) {
    Object obj = ev.getSource();
    boolean helpChecked = (cmdChoice.getSelectedIndex() == helpChoice);
    String helpStr = "", cmdTitle = "", cmdStr = "";
    if (helpChecked && obj instanceof Checkbox) {
      boolean defVal = false;
      cmdTitle = ((Checkbox) obj).getLabel();
      if (obj == verboseCheck) {
        helpStr = "display more execution/help info in command window";
        defVal = verboseDefault;
      }
      else if (obj == expandCheck) {
        helpStr = "expand *. before invoking command (Compile, Jar, Warp)";
        defVal = expandDefault;
      }
      else if (obj == debugCheck) {
        helpStr = "Compile: omit debug info to create smaller, final .class files";
        defVal = debugDefault;
      }
      else if (obj == compressCheck) {
        helpStr = "Jar: compress files for smaller web archive";
        defVal = compressDefault;
      }
      else if (obj == palmCheck) {
        helpStr = "Warp,Exegen: create/list/extract Palm .pdb, .prc files";
        defVal = palmDefault;
      }
      else if (obj == winceCheck) {
        helpStr = "Warp,Exegen: create/list/extract WinCE .wrp, .lnk files";
        defVal = winceDefault;
      }
      else if (obj == pkgCheck) {
        helpStr = "Warp,Exegen: create/list/extract Newton .pkg~, .pkg files";
        defVal = pkgDefault;
      }
      else if (obj == ntkCheck) {
        helpStr = "Warp,Exegen: create Newton ToolKit .cls.txt, .arg.txt files";
        defVal = ntkDefault;
      }
      else if (obj == recurCheck) {
        helpStr = "Warp: if a directory is specified in the files, recurse any subdirs";
        defVal = recurDefault;
      }
      else if (obj == iconCheck) {
        helpStr = "Exegen: include icon.bmp for Palm, Newton";
        defVal = iconDefault;
      }
      else if (obj == libCheck) {
        helpStr = "Newton: create library if checked; application otherwise";
        defVal = libDefault;
      };
      cmdStr = (helpStr.length() > 0) ? "default: " + new Boolean(defVal).toString() : "";
    }
    else if (helpChecked && obj == cmdChoice) {
      cmdTitle = "Command";
      helpStr = "select a command: help(all); create,list,extract(Jar,Warp,Exegen)";
      cmdStr = "default: " + cmdChoices[defaultChoice];
    };
    setStatus(helpStr, cmdStr);
    if (helpChecked && verboseCheck.getState()) {
      System.out.println("\n[help] " + cmdTitle + ": " + helpStr);
      System.out.println("\t" + cmdStr);
    };
  }

  public void focusLost  (FocusEvent ev) {}
  public void focusGained(FocusEvent ev) {
    Object obj = ev.getSource();
    // ?? cmdTitle for 'label'
    boolean helpChecked = (cmdChoice.getSelectedIndex() == helpChoice);
    String helpStr = "", appTitle = titleText.getText(), cmdStr = "";
    if (helpChecked && obj instanceof TextField) {
      if (obj == mainClassText) {
        helpStr = "Run,HTML,Exegen: name of main class for application";
        cmdStr = "(< 64 chars for Newton)";
      }
      else if (!COMPILE12 && obj == dirText)
        helpStr = "directory for application";

      else if (obj == titleText) {
        helpStr = "create: name of application/file without extension";
        cmdStr = "(< 31 chars for Palm)";
      }
      else if (obj == compileText) {
        helpStr = "Compile: .java source files to compile";
        cmdStr = "default: " + compileDefault;
      }
      else if (obj == jarText) {
        helpStr = "Jar: .class (and other) files to archive";
        cmdStr = "default: " + jarDefault;
      }
      else if (obj == creatorText) {
        helpStr = "Warp,Exegen: override generated 4-character PDB/PRC database creator";
        cmdStr = "currently: " + new PdbFile(appTitle).getCreator();
      }
      else if (obj == warpText) {
        helpStr = "Warp: .class (and optional .bmp) files to include";
        cmdStr = "default: " + warpDefault;
      }
      else if (obj == cepathText) {
        helpStr = "Exegen: full WinCE path to dir with .wrp file";
        cmdStr = "currently: \\Program Files\\" + appTitle;
      }
      else
        for (int i=0; i < NUMARGS; i++)
          if (obj == argTextObj[i]) {
            helpStr = argHelp[i];
            cmdStr = "default: " + argDefaults[i];
            break;
          };
    };
    setStatus(helpStr, cmdStr);
    if (helpChecked && verboseCheck.getState()) {
      System.out.println("\n[help] " + helpStr);
      System.out.println("\t" + cmdStr);
    };
  }

  public void actionPerformed(ActionEvent ev) {
    Object obj = ev.getSource();
    int curCmd = cmdChoice.getSelectedIndex();
    boolean helpChecked = (curCmd == helpChoice);
    boolean verboseChecked = verboseCheck.getState();
    Vector cmdVec = new Vector(25);
    String arg, helpStr = "";
    String mainClassName = mainClassText.getText();
    String appTitle = titleText.getText(), cmdTitle = ((Button) obj).getLabel();
    String dirName = (COMPILE12 || exec3==null) ? "" : dirText.getText();
    if (dirName.length() == 0)
      dirName = origDirName;
    if (!helpChecked && obj != dirButton) {
      assertMinLength(mainClassName,1);
      assertMinLength(appTitle,1);
      assertMinLength(dirName,3);  // ? reasonable min
    };
    File expandDir = null;
    if (expandCheck.getState())
      expandDir = new File(dirName);  // for stringPatternToVector
    String[] argVals = new String[NUMARGS]; // for Exegen, Jar
    for (int i=0; i < NUMARGS; i++) {
      arg = argVals[i] = argTextObj[i].getText();
      if (arg.length() > 0 && !helpChecked)
        assertInteger(arg);
    };

    int fmts = // for Warp, Exegen
      ((palmCheck.getState())  ? Warp.pdbFormat : 0) +
      ((winceCheck.getState()) ? Warp.wrpFormat : 0) +
      ((pkgCheck.getState())   ? Warp.pkgFormat : 0) +
      ((ntkCheck.getState())   ? Warp.ntkFormat : 0) +
      ((libCheck.getState())   ? Warp.libFormat : 0);
    if (fmts == 0)
      error("no Warp/Exegen format specified", "0");

    if (obj == defaultsButton) {
      if (helpChecked)
        helpStr = "restore original/default settings";
      else
        resetDefaults();
    }

    else if (!COMPILE12 && obj == dirButton) {
      if (helpChecked)
        helpStr = "set the application directory, initial mainClass and appTitle";
      else {
        FileDialog fd = new FileDialog(this, "Select a Waba app directory", FileDialog.LOAD);
        fd.setDirectory(dirName);
        //filter.setExtensions(new String[] {".java", ".class"});
        //fd.setFilenameFilter(filter); // doesn't work on Windows...
        fd.show();
        arg = fd.getFile();
        if (arg != null) { // null if Cancel
          dirText.setText(dirName = fd.getDirectory());
          mainClassName = Utils.strip(arg);
          mainClassText.setText(mainClassName);
          titleText.setText(appTitle = mainClassName);
            //(curCmd == listChoice || curCmd == extractChoice) ? arg : mainClassName);
        };
      };
    }

    else if (obj == compileButton) {
      if (curCmd == createChoice || helpChecked) {
        //cmdVec.addElement(javaCommand); cmdVec.addElement(compileClass);
        cmdVec.addElement(compileName);
        if (verboseChecked) {
          cmdVec.addElement("-verbose");
          cmdVec.addElement("-deprecation");
        };
        if (debugCheck.getState())
          cmdVec.addElement("-g:none");
        arg = compileText.getText();
        if (arg.length() == 0)
          arg = compileDefault;
        if (assertMinLength(arg, 5))
          stringPatternToVector(arg, " ", cmdVec, expandDir);
      };
      if (helpChecked)
        helpStr = "compile .java source files into .class files using " + compileName;
    }

    else if (obj == runAppButton) {
      cmdVec.addElement(javaCommand); cmdVec.addElement(wabaAppletClass);
      for (int i=0; i<2; i++)  // width, height
        if (defaultChanged(argDefaults[i], arg = argVals[i])) {
            cmdVec.addElement(Exegen.argCmds[i]); cmdVec.addElement(arg);
        };
      cmdVec.addElement(mainClassName.replace('/','.'));
      if (helpChecked)
        helpStr = "run a Waba main .class";
    }

    else if (obj == jarButton) {
      //cmdVec.addElement(javaCommand); cmdVec.addElement(jarClass);
      cmdVec.addElement(jarName);
      cmdVec.addElement(
        ((curCmd == listChoice) ? "t" : (curCmd == extractChoice) ? "x" : "c") +
        "f" +
        ((verboseChecked) ? "v" : "") +
        ((compressCheck.getState()) ? "" : "0M")
      );
      cmdVec.addElement(appTitle+".jar");
      arg = jarText.getText();
      if (arg.length() == 0)
        arg = jarDefault;
      if (assertMinLength(arg, 6))
        stringPatternToVector(arg, " ", cmdVec, expandDir);
      if (helpChecked)
        helpStr = "create .jar (Java ARchive) file for browser using " + jarName;
    }

    else if (obj == htmlButton) {
      String jarName = appTitle + ".jar";
      String htmName = appTitle + ".htm";
      //cmdVec.addElement(javaCommand); cmdVec.addElement(appletviewerClass);
      cmdVec.addElement(appletviewerName);
      cmdVec.addElement(arg = htmName);
      if (helpChecked)
        helpStr = "create .htm file with APPLET; " +
        ((COMPILE12 || exec3==null) ? "" : "copy .htm&.jar to a target directory; ") +
        "run " + appletviewerName;
      else if (curCmd == createChoice && !inAnApplet) {
        if (verboseChecked)
          System.out.println("...writing: " + htmName);
        mainClassName = mainClassName.replace('/','.');
        writeHTMLfile(appTitle, mainClassName, argVals, dirName);

        if (!COMPILE12 && exec3 != null) {
          FileDialog fd = new FileDialog(this, "Select directory containg waba.jar -- to copy .htm and .jar", FileDialog.LOAD);
          fd.setDirectory((origJarName.length() > 0) ? origJarName : dirName);
          fd.show();
          arg = fd.getFile();
          if (arg != null) { // null if Cancel
            String oldDirName = dirName;
            String newDirName = dirName = origJarName = fd.getDirectory();
            writeHTMLfile(appTitle, mainClassName, argVals, dirName); // write again to new dir
            // copy .jar
            try {
              DataOutputStream dos  = new DataOutputStream (new FileOutputStream(new File(newDirName, jarName)));
              PkgFile.writeStream(dos, new DataInputStream (new FileInputStream (new File(oldDirName, jarName))));
              dos.close();
              if (verboseChecked)
                System.out.println("...copied: " + jarName);
            }
            catch (FileNotFoundException fnfe) {
              error("can't create file", jarName);
              arg = null;
            }
            catch (IOException ioe) {
              error("problem writing to file", jarName);
              arg = null;
            };
          }; // cancel
        } // compile12
      } // create
      else // Extract, List
        arg = null;
      if (arg == null) // Cancel, Extract/List or error
        cmdVec.removeAllElements();
    }

    else if (obj == warpButton) {
      cmdVec.addElement(javaCommand); cmdVec.addElement(warpClass);
      cmdVec.addElement((curCmd == listChoice) ? "l" : (curCmd == extractChoice) ? "x" : "c");
      if (! verboseChecked)
        cmdVec.addElement("/q");
      cmdVec.addElement("/f"); cmdVec.addElement(Integer.toString(fmts));
      if (recurCheck.getState())
        cmdVec.addElement("/r");
      if ((fmts & Warp.pdbFormat) != 0 &&
        defaultChanged(creatorDefault, arg = creatorText.getText()) && assertLength(arg,4)) {
        cmdVec.addElement("/c"); cmdVec.addElement(arg);
      };
      cmdVec.addElement(appTitle);
      arg = warpText.getText();
      if (arg.length() == 0)
        arg = warpDefault;
      if (assertMinLength(arg, 6))
        stringPatternToVector(arg, " ", cmdVec, expandDir);
      if (helpChecked)
        helpStr = "create/list/extract warp file: .pdb(Palm), .wrp(WinCE), .pkg~(Newton tmp), .cls.txt(NTK)";
      else if (fmts == 0)
        return;
      // if extract, fix warpArg to reflect path\*.class ?
    }

    else if (obj == exegenButton) {
      if (curCmd == listChoice && fmts != 0) {
        arg = (dirName.endsWith(File.separator)) ? dirName : (dirName + File.separator);
        Exegen.list(false, arg + appTitle, fmts);
      }
      else {
        cmdVec.addElement(javaCommand); cmdVec.addElement(exegenClass);
        if (! verboseChecked)
          cmdVec.addElement("/q");
        cmdVec.addElement("/f"); cmdVec.addElement(Integer.toString(fmts));
        if (curCmd == extractChoice)
          cmdVec.addElement("/x");
        else {
          if (iconCheck.getState()) {
            cmdVec.addElement("/i"); cmdVec.addElement("icon.bmp");
          };
          if ((fmts & Exegen.prcFormat) != 0 &&
            defaultChanged(creatorDefault, arg = creatorText.getText()) && assertLength(arg,4)) {
            cmdVec.addElement("/c"); cmdVec.addElement(arg);
          };
          if ((fmts & Exegen.lnkFormat) != 0 &&
            defaultChanged(cepathDefault, arg = cepathText.getText()) && assertMinLength(arg,16)) { // ? reasonable min
            cmdVec.addElement("/p"); cmdVec.addElement(arg);
          };
          for (int i=0; i < NUMARGS; i++)
            if (defaultChanged(argDefaults[i], arg = argVals[i])) {
              cmdVec.addElement(Exegen.argCmds[i]); cmdVec.addElement(arg);
            };
        };
        cmdVec.addElement(appTitle); cmdVec.addElement(mainClassName); cmdVec.addElement(appTitle);
      };
      if (helpChecked)
        helpStr = "create/list/extract exegen file: .prc(Palm), .lnk(WinCE), .pkg(Newton), .arg.txt(NTK)";
      else if (fmts == 0)
        return;
      else if (curCmd == extractChoice) {
        arg = (dirName.endsWith(File.separator)) ? dirName : (dirName + File.separator);
        arg = Exegen.list(false, arg + appTitle, fmts);
        // /x arg ... mainname (creator or cePath)
//System.out.println("Exegen.extract: " + arg);
        String cmd;
        StringTokenizer tok = new StringTokenizer(arg, " ");
        while (tok.hasMoreTokens()) {
          cmd = tok.nextToken();
          if (cmd.startsWith("/")) {
            if (tok.hasMoreTokens())
              arg = tok.nextToken();
            else break;
            for (int i=0; i < NUMARGS; i++)
              if (cmd.equals(Exegen.argCmds[i])) {
                argTextObj[i].setText(arg);
                break;
              };
          }
          else if (fmts == Exegen.lnkFormat) {
            int pos = cmd.indexOf('\\');
            mainClassName = cmd.substring(1,pos);
            // this had broke after Program
            cmd += tok.nextToken("\"");
            cepathText.setText(cmd.substring(pos,cmd.lastIndexOf('\\')));
            break;
          }
          else {
            mainClassName = cmd; // prc, pkg
            if (fmts == Exegen.prcFormat)
              creatorText.setText(tok.nextToken());
            break;
          };
        }; // while
        mainClassText.setText(mainClassName);
      }; // extract
    }; // exegen

    int cmdLen = cmdVec.size();
/*
    if (cmdLen > 0 && !inAnApplet) { // !!!Nf
      // 1.2 String[] strVec = (String[]) (cmdVec.toArray(new String[cmdLen]));
      //System.arraycopy(cmdVec.toArray(),0, strVec,0,cmdLen);
      arg = (String) cmdVec.elementAt(0);
      if (expandDir != null && !(arg.equals(javaCommand))) {
        cmdVec.insertElementAt(javaCommand, 0);
        if (arg.equals(compileName)) {
          cmdVec.setElementAt(compileClass,1);
          cmdVec.insertElementAt(System.getProperty("java.class.path"), 2);
          cmdVec.insertElementAt("-classpath", 2);
        }
        else if (arg.equals(jarName))
          cmdVec.setElementAt(jarClass, 1);
        else if (arg.equals(appletviewerName))
          cmdVec.setElementAt(appletviewerClass, 1);
        cmdLen = cmdVec.size(); // exact len not important below, but just in case...
     };
*/
    String cmdStr = vectorToString(cmdVec, " ");
    if (helpChecked) {
      setStatus(helpStr, cmdStr);

      if (verboseChecked && cmdLen > 0) {
        System.out.println("\n[help] " + cmdTitle + ": " + helpStr);
        System.out.println("\t" +  cmdStr);
        cmdStr = "";     // preserve status line
        // run a command (if any) to get app's help
        cmdLen = (javaCommand.equals(cmdVec.elementAt(0))) ? 2 : 1;
      }
      else
        cmdLen = 0; // removeAllElements
      cmdVec.setSize(cmdLen);
    };

    // run a command line
    if (cmdLen > 0)
      new GUIThread(this, cmdTitle, cmdStr, cmdVec, dirName, exec3).start(); // !!!Ne
  }

  public void writeHTMLfile(String title, String mainClassName, String[] argVals, String dirName) {
    // ?? add/remove other libs e.g., wextra.jar
    String arg, fileName = title + ".htm";
    try {
      PrintStream ps = new PrintStream(new FileOutputStream(new File(dirName, fileName)));
      ps.println("<HTML><HEAD><TITLE>Waba: " + title + "</TITLE></HEAD>");
      ps.println("<!-- " + fileName + " generated by " + GUI_VERSION + ", on " + new Date().toString() + " -->");
      ps.println("<BODY><HR>");
      ps.println("<APPLET name=\"" + title + '"');
      ps.println("\tcode=\"waba/applet/Applet.class\" archive=\"" + title + ".jar,wextra.jar,waba.jar\"");
      for (int i=0; i < NUMARGS; i++)
        if (defaultChanged(argDefaults[i], arg = argVals[i]) ||
            (i < 2 && (arg = "160").length() > 0))  // width, height
          ps.println("\t" + PkgFile.argAttribs[i] + "=\"" + arg + '"');
      ps.println("\t>");
      ps.println("<PARAM name=\"appClass\" value=\"" + mainClassName + "\">");
      ps.println("</APPLET>");
      ps.println("<HR></BODY></HTML>");
      ps.close();
    }
    catch (FileNotFoundException fnfe) {
      error("can't create file", fileName);
    }
    catch (IOException ioe) {
      error("problem writing to file", fileName);
     };
  }

  public boolean defaultChanged(String defStr, String newStr) { // assume int
    return (newStr.length() > 0 && ! newStr.equals(defStr));
  }

  public boolean assertInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    }
    catch (NumberFormatException ex) {
      return error("not a number:", str);
    }
  }
  public boolean assertLength(String newStr, int len) {
    if (newStr.length() == len)
      return true;
    return error(newStr, "length not =" + len);
  }
  public boolean assertMaxLength(String newStr, int len) {
    if (newStr.length() <= len)
      return true;
    return error(newStr, "length >" + len);
  }
  public boolean assertMinLength(String newStr, int len) {
    if (newStr.length() >= len)
      return true;
    return error(newStr, "length <" + len);
  }

  public void setStatus(String msg1, String msg2) {
    statusLabel.setText(msg1);
    cmdLabel.setText(msg2);
  }
  public boolean error(String msg, String arg) {
    System.err.println("ERROR: " + msg + ' ' + arg);
    if (statusLabel != null)
      setStatus("ERROR:", msg+' '+arg);
    return false;
  }
/*
  public static void errExit(String msg, int code) {
    error(msg);
    //System.exit(code);  // don't exit
  }
*/

  public String vectorToString (Vector objs, String delim) {
    StringBuffer sb = new StringBuffer(100);
    for (int i=0; i < objs.size(); i++) {
      if (i > 0)
        sb.append(delim);
      sb.append(objs.elementAt(i));
    };
    return sb.toString();
  }

  public void stringPatternToVector(String str, String delim, Vector vec, File expandDir) {
    StringTokenizer tok = new StringTokenizer(str, delim);
    String arg;
    String[] exp;
    while (tok.hasMoreTokens()) {
      arg = tok.nextToken();
      if (expandDir != null && arg.startsWith("*.")) {
        exp = expandDir.list(new FileExtensionFilter(arg.substring(1)));
        for (int i=0; i < exp.length; i++)
          vec.addElement(exp[i]);
      }
      else
        vec.addElement(arg);
    };
  }

/*
  public void arrayToVector (Vector vec, String prefix, Object[] objs) {
    for (int i=0; i < objs.length; i++)
      vec.addElement(prefix + objs[i]);
  }
*/
  class DWAdapter extends WindowAdapter {
    public void windowClosing(WindowEvent event) {
      if (inAnApplet)
        dispose();
      else
        System.exit(0);
    }
  }
}

class GUIThread extends Thread {
  static final int SLEEP_MSEC = 100;
  static final int BUF_SIZE = 200;
  GUI caller;
  String cmdLine;
  Vector cmdVec;
  String dirName;
  Method exec3;

  boolean stop = false;
  private BufferedInputStream procErr = null, procOut = null;
  private Process proc = null;

  GUIThread (GUI gui, String title, String line, Vector cmds, String dir, Method meth) {
    super(title);   //  currently, name of button, e.g., Compile, Warp, etc.
    caller = gui;   // just for setStatus
    cmdLine = line; // expanded command line
    cmdVec = cmds;
    dirName = dir;
    exec3 = meth;
  }
  public void start() {
    if (cmdLine.length() > 0) { // not help?
      caller.setStatus("starting: ", cmdLine);
      System.out.println("\nstart: " + cmdLine);
    };
    Runtime run = Runtime.getRuntime();
    String[] args = new String[cmdVec.size()];
    cmdVec.copyInto(args);

 /*   if (GUI.COMPILE12 || exec3 == null) { // !!!Nf,e,d
      try {
        Class mainClass = Class.forName((String) cmdVec.elementAt(1));
        cmdVec.removeElementAt(0); cmdVec.removeElementAt(0); // java, mainClass
        args = new String[cmdVec.size()];
        cmdVec.copyInto(args);
        //mainClass.main(cmdStrA);  would require a cast
        Method mainMeth = mainClass.getDeclaredMethod("main", new Class[] {args.getClass()});
System.out.println(mainMeth);
        mainMeth.invoke(null, new Object[]{args}); // mainClass not nec for static
        }
      // ClassNotFoundException, NoSuchMethodException, InstantiationException
      // IllegalAccessException, InvocationTargetException
      catch (Exception ex) {
        System.err.println(ex);
        ex.printStackTrace(System.err);
      };
      stop = true;
    }
*/

 //System.out.println("java.class.path: " + System.getProperty("java.class.path"));
//new String[]{"user.dir="+dirName}
//System.out.println("dirName= "+dirName);
    try {
      proc = (GUI.COMPILE12 || exec3 == null)  // !!!Ne,d
        ? run.exec(args)    // 1.2
        //: run.exec(args,null, new File(dirName)); // 1.3
        // this should avoid pre-1.3 compiler errorh
        : (Process) exec3.invoke(run, new Object[]{args, null, new File(dirName)});
      InputStream is = proc.getErrorStream();
      if (is != null)
        procErr = new BufferedInputStream(is);
      is = proc.getInputStream();
      if (is != null)
        procOut = new BufferedInputStream(is);
      super.start();
    }
    catch (Exception ex) {
      caller.error("thread:", getName() + ex); // Thread's name
    };
  }

  public void run() {
    int errRead = 0, outRead = 0, exitVal;
    byte[] buf = new byte[BUF_SIZE];
    while (! stop) {
      try {
        // need to check available, otherwise read can block (contrary to docs)
        if (procOut != null)
          while (procOut.available() > 0) {
            outRead = procOut.read(buf,0,BUF_SIZE);
            System.out.write(buf,0,outRead);
          };
        if (procErr != null)
          while (procErr.available() > 0) {
            errRead = procErr.read(buf,0,BUF_SIZE);
            System.err.write(buf,0,errRead);
          };
        try {
          exitVal = proc.exitValue();
//System.out.println("exitVal: " + exitVal);
          stop = true;
        }
        catch (IllegalThreadStateException ex) {
          // expected -- not yet terminated
        };

        sleep(SLEEP_MSEC);
      }
      catch (IOException ex) {
        caller.error("thread: " + getName(), "IO ex: " + ex); // Thread's name
        stop = true;
      }
      catch (InterruptedException ex) {
        caller.error("thread: " + getName(), "ex: " + ex);
        stop = true;
      };
    }; // end while

    if (cmdLine.length() > 0) { // not help?
      System.out.println("stop:  " + cmdLine);
      caller.setStatus("","");
    };
    try {
      if (procErr != null)
        procErr.close();
      if (procOut != null)
        procOut.close();
      if (proc != null)
        proc.destroy();
    }
    catch (IOException ex) {
      // ignore
    };
  }
}

class FileExtensionFilter implements FilenameFilter {
  String[] exts;

  FileExtensionFilter (String[] sexts) {
    exts = sexts;
  }
  FileExtensionFilter (String sext) {
    exts = new String[] {sext};
  }
  void setExtensions(String[] sexts) {
    exts = sexts;
  }
  public boolean accept(File dir, String name) {
    for (int i=0; i<exts.length; i++)
      if (name.endsWith(exts[i]))
        return true;
    return false;
  }
}