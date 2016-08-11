package macroutils;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import star.base.neo.*;
import star.base.report.*;
import star.cadmodeler.*;
import star.common.*;
import star.common.graph.*;
import star.coupledflow.*;
import star.dualmesher.*;
import star.energy.*;
import star.extruder.*;
import star.flow.*;
import star.keturb.*;
import star.kwturb.*;
import star.mapping.*;
import star.material.*;
import star.meshing.*;
import star.metrics.*;
import star.motion.*;
import star.multiphase.*;
import star.post.*;
import star.vof.*;
import star.prismmesher.*;
import star.radiation.common.*;
import star.radiation.s2s.*;
import star.resurfacer.*;
import star.rsturb.*;
import star.saturb.*;
import star.segregatedenergy.*;
import star.segregatedflow.*;
import star.segregatedmultiphase.*;
import star.segregatedspecies.*;
import star.solidmesher.*;
import star.surfacewrapper.*;
import star.sweptmesher.*;
import star.trimmer.*;
import star.turbulence.*;
import star.viewfactors.*;
import star.vis.*;


/**
 * <b><span style="color:#0000FF">Macro Utils</b> -- <i>"Your MACRO to write MACROS"</i>.<p>
 *
 * <b><span style="color:#0000FF">Macro Utils</b> is a set of useful methods to assist the process
 * of writing macros in STAR-CCM+. One can think of it as a high level way of writing macros,
 * i.e., a high level API. Note that all macros recorded in STAR-CCM+ GUI are low level. This means
 * that if you have a slight different name in some object in your next simulation it is most likely
 * the original macro will fail. Macro Utils can help you with that in that sense.
 * <p>
 * Since v3.0 is being distributed as <u>MacroUtils.jar</u> file, <u>javadoc</u> folder
 * and <u>MacroUtils.java</u> source code.
 * <p>
 * The source code is only useful when one needs to modify, or learn/copy something.
 * Otherwise, when writing your own macros in NetBeans just stick to <u>MacroUtils.jar</u>.
 * <p><p>
 * <b>Setting up <u>MacroUtils.jar</u> in NetBeans</b> -- This is done only once.
 * <ol>
 * <li> Create a Library that points to <u>MacroUtils.jar</u>. This
 *      <a href="http://www.youtube.com/watch?v=H8lJVoa8ytI">video</a> can help;
 * <p>
 * <li> Create another Library that points to STAR-CCM+ libraries.
        * <a href="http://steve.cd-adapco.com">STEVE portal</a>
        * can help you with that;
 * <p>
 * <li> Add an import section in your child java and start coding.
 * <p>
 * <li> When you start typing a global variable or a method, and Javadoc is not available, click on
 *      the <i>Attach Javadoc</i> link and point to <u>javadoc</u> folder supplied by
 *      <b><span style="color:#0000FF">Macro Utils</b>.
 * </ol>
 * <p><p>
 * <b>How to use it in STAR-CCM+ after a .java is written dependant on <u>MacroUtils.jar</u>?</b>
 * <ol>
 * <li> Store your .java (e.g.: MyMacro.java) in a folder along with your .sim
 * (e.g.: myCase.sim) file and <u>MacroUtils.jar</u>;
 * <p>
 * <li> In STAR-CCM+ GUI, go to "Menu -> Tools -> Options -> Environment":
 * <p>
 * - Under "User Macro classpath" put . (yes, a single dot)<p>
 * <p>
 * - Alternatively, launch STAR-CCM+ from the command line using the following syntax:
 * <p>
 * > <code>starccm+ -classpath . -batch MyMacro.java myCase.sim</code>
 * </ol>
 * <p><p>
 * <b>Example of MyMacro.java</b>
 * <p>
 * <pre><code>
 * import {@link macroutils.}*;
 *
 * public class MyMacro extends MacroUtils {
 *
 *      public void execute() {
 *
 *          _initUtils();
 *
 *          genVolumeMesh();
 *
 *          updateObjectsToFVRepresentation();
 *
 *          runCase();
 *
 *          _finalize();
 *
 *      }
 *
 * }
 * </code></pre>
 * <p><p>
 * <b>Requirement:</b> <u>STAR-CCM+ v10.02 libs</u>
 * <p>
 * In case of existing methods not available in older libraries, <b><span style="color:#0000FF">Macro Utils</b>
 * will not work and there will be an error when playing in STAR-CCM+. The usage of NetBeans is
 * strongly suggested for writing your macros with <b><span style="color:#0000FF">Macro Utils</b>.
 * <p>
 * @since STAR-CCM+ v7.02, May of 2012
 * @author Fabio Kasper
 * @version 3.2, March 25, 2015.
 */
public class MacroUtils extends StarMacro {

  final private String MACROUTILSVERSION = "3.2";

  /**
   * Regular execute() method. If you execute Macro Utils, it will do the following.
   */
  public void execute() {
    _initUtils(false, false);
    sayLoud("Hi, This is Macro Utils.");
  }

  /**
   * Initialize Macro Utils. This method is <b>mandatory</b>. It begins here.
   * <p>
   * Alternatively, one can initialize with {@see #_initUtilsNonIntrusive} instead.
   */
  public void _initUtils() {
    _initUtils(true, verboseDebug);
  }

  /**
   * Initialize Macro Utils in non intrusive mode, i.e., it will not create or change Colormaps,
   * units, etc...
   * <p>
   * Alternatively, one can initialize in Intrusive mode. See {@see #_initUtilsNonIntrusive}.
   */
  public void _initUtilsNonIntrusive() {
    _initUtils(false, verboseDebug);
  }

  private void _initUtils(boolean addChangeStuff, boolean verboseOption) {
    sim = getActiveSimulation();
    /***************************************************************/
    /* Remember to initialize 'sim' before calling everything else */
    /***************************************************************/
    sayLoud("Starting " + getMacroUtilsVersion(), verboseOption);
    //--
    //-- Store essential variables
    //--
    printAction("Storing necessary variables", verboseOption);
    simFullPath = sim.getSessionPath();
    simFile = new File(simFullPath);
    simTitle = sim.getPresentationName();
    simPath = sim.getSessionDir();
    cadPath = new File(simPath, "CAD");
    dbsPath = new File(simPath, "DBS");
    saySimName(true);
    say("Simulation File: " + simFile.toString(), verboseOption);
    say("Simulation Path: " + simPath, verboseOption);
    try {
        csys0 = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    } catch (Exception e) {}
    lab0 = (LabCoordinateSystem) csys0;
    //--
    //--
    if (addChangeStuff) {
        addColormaps(verboseOption);
        addTags(verboseOption);
        prettifyLogo(false);
        updateUnits(verboseOption);
        //setAbortStoppingCriteria(false);      <--- Very dangerous.
    }
    printAction(getMacroUtilsVersion() + " initialized.");
  }

  /**
   * Finalize Macro Utils. This method is <i>optional</i> but it has useful stuff.
   */
  public void _finalize() {
    prettifyMe();
    cleanUpTemporaryCameraViews();
    updateSolverSettings();
    if (!saveIntermediates || savedWithSuffix < 1) {
        saveSim(simTitle);
    }
    sayLoud("Macro Utils finished.", true);
  }

  /**
   * Adds a custom Colormap to be used in Scalar Displayers.
   *
   * @param name given Colormap name.
   * @param arrayColors given Array of Colors. See {@see java.awt.Color} for details.
   * @return
   */
  public LookupTable addColormap(String name, Color[] arrayColors) {
    return addColormap(name, arrayColors, true);
  }

  /**
   * Adds a custom Colormap to be used in Scalar Displayers.
   *
   * @param name given Colormap name.
   * @param vc given ArrayList of Colors. See {@see java.awt.Color} for details.
   * @return
   */
  public LookupTable addColormap(String name, ArrayList<Color> vc) {
    return addColormap(name, vc, true);
  }

  /**
   * @param name
   * @param arrayColors
   * @param verboseOption
   * @return
   */
  private LookupTable addColormap(String name, Color[] arrayColors, boolean verboseOption) {
    return addColormap(name, new ArrayList(Arrays.asList(arrayColors)), verboseOption);
  }

  /**
   * @param name
   * @param vc
   * @param verboseOption
   * @return
   */
  private LookupTable addColormap(String name, ArrayList<Color> vc, boolean verboseOption) {
    if (sim.get(LookupTableManager.class).has(name)) {
        say("Colormap already exists: " + name, verboseOption);
        return sim.get(LookupTableManager.class).getObject(name);
    }
    UserLookupTable cmap = sim.get(LookupTableManager.class).createLookupTable();
    DoubleVector dv = new DoubleVector();
    for (Color c : vc) {
        dv.addAll(new DoubleVector(c.getColorComponents(null)));
        dv.add(1.0);
    }
    cmap.setPresentationName(name);
    cmap.setColorSpace(0);
    cmap.setValues(dv);
    say("Colormap created: " + name, verboseOption);
    return cmap;
  }

  /**
   * Add custom Colormaps.
   */
  private void addColormaps(boolean verboseOption) {
    printAction("Adding some custom Colormaps", verboseOption);
    final int rgb = 0;
    final int hsv = 1;
    final int lab = 2;
    final int div = 3;
    final int hsvw = 4;
    //--
    //-- Solid Colors
    //--
    addColormap("solid-black", new Color[] {Color.black}, verboseOption);
    addColormap("solid-blue", new Color[] {Color.blue}, verboseOption);
    addColormap("solid-blue-medium", new Color[] {colorBlueMedium}, verboseOption);
    addColormap("solid-dim-gray", new Color[] {colorDimGray}, verboseOption);
    addColormap("solid-ivory-black", new Color[] {colorIvoryBlack}, verboseOption);
    addColormap("solid-light-gray", new Color[] {colorLightGray}, verboseOption);
    addColormap("solid-red", new Color[] {Color.red}, verboseOption);
    addColormap("solid-slate-gray", new Color[] {colorSlateGray}, verboseOption);
    addColormap("solid-slate-gray-dark", new Color[] {colorSlateGrayDark}, verboseOption);
    addColormap("solid-wheat", new Color[] {colorWheat}, verboseOption);
    addColormap("solid-ultramarine", new Color[] {colorUltramarine}, verboseOption);
    //--
    //-- Gradients
    //--
    addColormap("ivory-black-2-white", new Color[] {colorIvoryBlack, Color.white}, verboseOption);
    addColormap("blue-2-white", new Color[] {Color.blue, Color.white}, verboseOption);
    addColormap("blue-2-yellow", new Color[] {colorBlue, Color.yellow}, verboseOption);
    addColormap("cool-warm2", new Color[] {Color.black, colorBlue, Color.gray, Color.red, Color.yellow, Color.white}, verboseOption);
    addColormap("flames1", new Color[] {colorUltramarine, Color.YELLOW, Color.black}, verboseOption);
    addColormap("flames2", new Color[] {colorNavy, colorDarkOrange, colorSlateGrayDark}, verboseOption);
    addColormap("paraview-black-body-radiation", new Color[] {Color.black, Color.red, Color.yellow, Color.white}, verboseOption);
    addColormap("red-2-white", new Color[] {Color.red, Color.white}, verboseOption);
    addColormap("red-2-yellow", new Color[] {Color.red, Color.yellow}, verboseOption);
    addColormap("vectors1", new Color[] {colorMint, Color.blue, colorPurple}, verboseOption);
    addColormap("hsv-vectors1", new Color[] {colorMint, Color.blue, colorPurple}, verboseOption).setColorSpace(hsv);
    addColormap("vectors2", new Color[] {Color.yellow, colorDarkGreen, colorPurple}, verboseOption);
    addColormap("hsv-vectors2", new Color[] {Color.yellow, colorDarkGreen, colorPurple}, verboseOption).setColorSpace(hsvw);
    addColormap("vectors3", new Color[] {colorIvory, colorDarkGreen, colorPurple}, verboseOption);
    addColormap("hsv-vectors3", new Color[] {colorIvory, colorDarkGreen, colorPurple}, verboseOption).setColorSpace(hsv);
    addColormap("vectors4", new Color[] {colorIvory, Color.magenta, Color.black}, verboseOption);
    addColormap("hsv-vectors4", new Color[] {colorIvory, Color.magenta, Color.black}, verboseOption).setColorSpace(hsvw);
    addColormap("vectors5", new Color[] {colorWhiteSmoke, Color.blue, colorLimeGreen}, verboseOption);
    addColormap("hsv-vectors5", new Color[] {colorWhiteSmoke, Color.blue, colorLimeGreen}, verboseOption).setColorSpace(hsvw);
    addColormap("ultramarine-2-white", new Color[] {colorUltramarine, Color.white}, verboseOption);
    //--
    //-- Cool Warm3
    ArrayList<Color> vc = new ArrayList<Color>();
    vc.add(Color.blue); vc.add(Color.gray); vc.add(Color.gray); vc.add(Color.gray); vc.add(Color.red);
    addColormap("cool-warm3", vc, verboseOption);
    sayOK(verboseOption);
  }

  /**
   * Add Tag.
   *
   * @param name
   * @param iv
   */
  private void addTag(String name, IntVector iv, boolean verboseOption) {
    if (sim.get(TagManager.class).has(name)) {
        say("Tag already exists: " + name, verboseOption);
        return;
    }
    UserTag ut = sim.get(TagManager.class).createNewUserTag(name);
    ut.getCustomizableIcon().setStarImagePixelData(iv);
    say("Tag created: " + name, verboseOption);
    TagClientServerObjectFilter tf = (TagClientServerObjectFilter) sim.get(GeometryFilterManager.class).createObjectFilter("TagClientServerObjectFilter");
    tf.setPresentationName(name + " Filter");
    tf.getTagGroup().setObjects(ut);
    say("   Filter: " + tf.getPresentationName(), verboseOption);
  }

  /**
   * Add Custom Simulation Tags
   */
  private void addTags(boolean verboseOption) {
    printAction("Adding Custom Simulation Tags", verboseOption);
    addTag("Black", new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 6644833, 6776419, 6842212, 208168804, 2120705379, -966433951, -60727203, -60990375, -967223211, 2119455569, 206523980, 5000009, 4802630, 4671044, 16777215, 16777215, 6644833, 6776163, 1063741027, -563648925, -6908783, -5592665, -5263700, -5658459, -6908526, -8882572, -565425335, 1061767238, 4605251, 4539458, 16777215, 16777215, 6513247, 1365599073, -8421766, -5790303, -5790301, -7106420, -8224900, -8685451, -8553608, -8093058, -8553608, -10790314, 1363362367, 4276286, 16777215, 16777215, 308305756, -211722147, -6250597, -7237749, -8093058, -8487816, -8882573, -9343124, -9803675, -9935261, -9540503, -8948109, -213959108, 306003002, 16777215, 16777215, -2074322088, -8092801, -7961472, -8487816, -8685195, -9014160, -9408918, -9803675, -10001054, -10198434, -10461605, -10264226, -10527141, -2076559050, 16777215, 16777215, -816425645, -8488329, -8882573, -9146002, -9408917, -9737882, -9935261, -10132640, -10330019, -10527399, -10724778, -10790570, -10593705, -818596814, 16777215, 16777215, -112111539, -9079695, -9737625, -9935004, -10000797, -10132383, -10329762, -10527141, -10724777, -10921899, -11119278, -11185071, -10856107, -265146322, 16777215, 16777215, -263501240, -9737883, -10330019, -10395812, -10527398, -10724777, -10856364, -11119535, -11251122, -11448501, -11645879, -11711673, -11316914, -215011797, 16777215, 16777215, -817478333, -10264225, -10461605, -10724778, -10856364, -11053743, -11251122, -11382964, -11645879, -11777466, -11909052, -11777466, -11711671, -819188952, 16777215, 16777215, -2075966913, -11119536, -10593191, -11053743, -11185585, -11382707, -11580086, -11711673, -11909052, -11975101, -12106431, -11711673, -12501189, -2077611738, 16777215, 16777215, 356466235, -214156487, -10987950, -11053744, -11514294, -11646136, -11843259, -11975101, -12106431, -12172224, -11909053, -12040638, -215472348, 355084582, 16777215, 16777215, 3881528, 1413036085, -12435139, -11579829, -11251635, -11711929, -12040894, -12172224, -12106687, -11777980, -12172481, -13422034, 1411917604, 2828839, 16777215, 16777215, 3881271, 3618356, 1160983088, -567201747, -12369346, -11777209, -11448757, -11580343, -12040381, -12895690, -567925470, 1210591012, 2763046, 2960425, 16777215, 16777215, 3815734, 3552563, 3355184, 204484396, -2127746007, -919918041, -14079963, -14211549, -920181213, -2128140765, 203958052, 2763046, 2894632, 3092011, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}), verboseOption);
    addTag("Blue", new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 8771304, 8837096, 8771304, 210032103, 2122437350, -964767772, -59061278, -59324705, -965623332, 2121055449, 208058070, 6402258, 6073296, 5744333, 16777215, 16777215, 8639718, 8639718, 1065538534, -561983003, -3611665, -2297864, -1246979, -1378307, -2888971, -5384728, -563957293, 1063038160, 5744333, 5284554, 16777215, 16777215, 8310756, 1367265252, -5187094, -2888972, -3019013, -4528391, -6169096, -6563081, -5709832, -4331526, -4727570, -8668455, 1364239050, 4627911, 16777215, 16777215, 310037217, -210122015, -3216907, -4791047, -6234376, -6628105, -6956553, -7416074, -7875594, -8269323, -7481866, -5514771, -213475897, 305895364, 16777215, 16777215, -2072722210, -4924691, -5775368, -6759433, -7022089, -7350281, -7678474, -8072458, -8531979, -8925964, -9385484, -9319692, -8732962, -2077191744, 16777215, 16777215, -814825766, -6169869, -7218953, -7678218, -7875338, -8137994, -8466187, -8794379, -9188364, -9713420, -10304269, -10566414, -10239510, -819688515, 16777215, 16777215, -110643242, -6956554, -8137995, -8400395, -8597516, -8860172, -9188365, -9516557, -9976078, -10501134, -11091727, -11616784, -11091985, -266763079, 16777215, 16777215, -262098734, -8794125, -9778957, -9976077, -10173198, -10370318, -10764047, -11223567, -11814160, -12339472, -12995601, -13520658, -13060627, -217153610, 16777215, 16777215, -816207410, -9320724, -10173198, -10632718, -10895375, -11223567, -11617552, -12142352, -12667409, -13192722, -13783314, -14111251, -13718556, -821790029, 16777215, 16777215, -2075024694, -10569762, -10632462, -11420687, -11814160, -12208144, -12536337, -12995857, -13520658, -13980179, -14439699, -14242066, -14902575, -2080344655, 16777215, 16777215, 356752326, -213935675, -11290138, -11944975, -12667409, -13061393, -13520658, -13914642, -14374163, -14701844, -14636051, -14572321, -218073936, 352350639, 16777215, 16777215, 3577026, 1412469184, -13197875, -12537887, -12929297, -13717523, -14308627, -14702356, -14833172, -14570003, -14835492, -16218430, 1409314990, 28589, 16777215, 16777215, 2723262, 2263484, 1159497403, -568949831, -14181166, -13784092, -13979153, -14176017, -14637342, -15494194, -570396241, 1207988398, 28333, 28076, 16777215, 16777215, 1935291, 1475513, 1081271, 202014133, -2130412877, -922716238, -16746831, -16747600, -922717777, -2130677586, 201355181, 28076, 28076, 28076, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}), verboseOption);
    addTag("Cross", new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -2905944, -3771533, -4373950, -4373950, -3771533, -2971480, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -4103839, -3192760, -1482399, -890262, -890262, -1548192, -3258810, -4103839, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -4432291, -2534573, -1, -2206122, -2206122, -2206122, -2206122, -1, -2798002, -4432291, -2565928, 2087283049, 390810443, 1112033352, -725631042, -3892326, -3521725, -1, -1, -1, -3324603, -3324603, -1, -1, -1, -3982019, -3958376, -726420558, 1112033352, 1548043589, -2302757, -4823964, -2732209, -5561565, -1, -526345, -1513240, -2171170, -2368549, -2236963, -6614767, -4837842, -4823964, -3355445, 1548043589, 1816281666, -3158065, -5425867, -2995638, -4706770, -6286573, -3223858, -3026479, -2697514, -2302756, -6352623, -6156015, -5365985, -5425867, -3947581, 1816281666, 1849572926, -3355444, -5688525, -3589317, -5368812, -5500655, -3026479, -2697514, -2302756, -1907998, -5500655, -5500655, -5170919, -5688525, -4144960, 1849572926, 1631205946, -3158064, -5349793, -4117974, -4714223, -3026479, -2697514, -2302756, -1907998, -1644826, -1381654, -4714223, -4974825, -5349793, -4144958, 1631205946, 1194800951, -676681044, -4549995, -5234142, -3026479, -2697514, -2302756, -5238511, -5238511, -1381654, -1118482, -1118482, -5563878, -4681580, -677404766, 1194800951, 422851636, -2041491117, -3223852, -5681584, -6154730, -2302756, -4845295, -3141359, -3141359, -4845295, -1118482, -6155244, -5681584, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -6075827, -7073776, -3862255, -2879215, -2879215, -3862255, -7073776, -6075827, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -4352096, -6138019, -7857894, -7857894, -6138019, -4352096, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}), verboseOption);
    addTag("Grey", new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16053492, 16053492, 16053492, 217314291, 2129851122, -957222415, -51318544, -51384337, -957419794, 2129456364, 216722154, 15263976, 15198183, 15066597, 16777215, 16777215, 15987699, 15987699, 1072952307, -554503438, -526345, -263173, -65794, -65794, -328966, -789517, -555095575, 1072162791, 15066597, 15000804, 16777215, 16777215, 15856113, 1374810609, -657931, -328966, -197380, -263173, -394759, -460552, -394759, -263173, -592138, -1250068, 1373955300, 14869218, 16777215, 16777215, 317780208, -202313488, -328966, -328966, -394759, -394759, -460552, -460552, -526345, -526345, -526345, -592138, -203234590, 316727520, 16777215, 16777215, -2064716050, -657931, -394759, -460552, -460552, -460552, -526345, -526345, -592138, -592138, -657931, -592138, -1118482, -2065768738, 16777215, 16777215, -806490643, -526345, -460552, -526345, -526345, -592138, -592138, -592138, -657931, -657931, -723724, -723724, -921103, -807609124, 16777215, 16777215, -102044950, -460552, -526345, -592138, -592138, -592138, -592138, -657931, -657931, -723724, -789517, -789517, -789517, -254092582, 16777215, 16777215, -253171480, -657931, -723724, -723724, -723724, -789517, -789517, -855310, -921103, -921103, -986896, -1052689, -986896, -203892520, 16777215, 16777215, -806951194, -789517, -723724, -789517, -789517, -855310, -855310, -921103, -986896, -986896, -1052689, -1052689, -1184275, -808003882, 16777215, 16777215, -2065373980, -1184275, -789517, -855310, -921103, -921103, -921103, -986896, -1052689, -1052689, -1118482, -1052689, -1776412, -2066360875, 16777215, 16777215, 367190754, -203366176, -1052689, -855310, -986896, -986896, -1052689, -1052689, -1118482, -1118482, -1052689, -1381654, -204155692, 366269652, 16777215, 16777215, 14671839, 1423892190, -1776412, -1250068, -986896, -1052689, -1118482, -1118482, -1118482, -1118482, -1513240, -2302756, 1423168467, 13816530, 16777215, 16777215, 14540253, 14474460, 1172036571, -556082470, -1710619, -1184275, -986896, -986896, -1315861, -1842205, -556477228, 1221841875, 13816530, 13816530, 16777215, 16777215, 14408667, 14342874, 14211288, 215472087, -2116626730, -908667178, -2763307, -2829100, -908798764, -2116824109, 215143122, 13816530, 13816530, 13816530, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}), verboseOption);
    addTag("Minus", new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -2905944, -3771533, -4373950, -4373950, -3771533, -2971480, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -4103839, -3192760, -1482399, -890262, -890262, -1548192, -3258810, -4103839, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -4432291, -2534573, -1613985, -2206122, -2206122, -2206122, -2206122, -1745571, -2798002, -4432291, -2565928, 2087283049, 390810443, 1112033352, -725631042, -3892326, -3521725, -2469294, -3324603, -3324603, -3324603, -3324603, -3324603, -3324603, -2732466, -3982019, -3958376, -726420558, 1112033352, 1548043589, -2302757, -4823964, -2732209, -4245705, -4245705, -4574927, -5232857, -5891044, -6285802, -6548974, -6548974, -4837842, -4823964, -3355445, 1548043589, 1816281666, -3158065, -5425867, -2995638, -460552, -2171170, -3223858, -3026479, -2302756, -1513240, -1118482, -1118482, -5365985, -5425867, -3947581, 1816281666, 1849572926, -3355444, -5688525, -3589317, -3092272, -3355444, -3026479, -2302756, -1513240, -1118482, -1118482, -1118482, -5170919, -5688525, -4144960, 1849572926, 1631205946, -3158064, -5349793, -4117974, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -4974825, -5349793, -4144958, 1631205946, 1194800951, -676681044, -4549995, -5234142, -3663848, -3927791, -3927791, -3927791, -3927791, -3927791, -3927791, -3927791, -5563878, -4681580, -677404766, 1194800951, 422851636, -2041491117, -3223852, -5681584, -4646117, -3075309, -3141359, -3141359, -3141359, -3141359, -3141359, -4778474, -5681584, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -6075827, -5959407, -3862255, -2879215, -2879215, -3862255, -5959407, -6075827, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -4352096, -6138019, -7857894, -7857894, -6138019, -4352096, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}), verboseOption);
    addTag("Plus", new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -6500190, -9779851, -12862389, -12862389, -9779851, -6500190, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -11485602, -12400045, -11149978, -10755220, -10755220, -11215771, -12465838, -11485602, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -11617702, -11873958, -11347357, -12794803, -1, -1, -12794803, -11413150, -12136873, -11617702, -2565928, 2087283049, 390810443, 1112033352, -725631042, -7289964, -12663731, -11939494, -12597424, -13321147, -1, -1, -13321147, -12597424, -12136873, -12992439, -7421550, -726420558, 1112033352, 1548043589, -2302757, -10701467, -12071337, -13781698, -13781698, -14242249, -1315861, -1907998, -15426267, -15557853, -15557853, -13979078, -10701467, -3355445, 1548043589, 1816281666, -3158065, -13652677, -12202923, -460552, -1973791, -2763307, -2631721, -2302756, -2039584, -1776412, -1513240, -14964180, -13652677, -3947581, 1816281666, 1849572926, -3355444, -13850313, -13319355, -2829100, -2894893, -2631721, -2302756, -2039584, -1776412, -1513240, -1250068, -15422428, -13850313, -4144960, 1849572926, 1631205946, -3158064, -11162531, -14501326, -15882722, -15882722, -15817953, -2039584, -1776412, -15817953, -15882722, -15882722, -15618527, -11162531, -4144958, 1631205946, 1194800951, -676681044, -8013940, -14701525, -15876834, -16271080, -16076261, -1776412, -1513240, -16076261, -16271080, -16271080, -15226845, -8079733, -677404766, 1194800951, 422851636, -2041491117, -3223852, -12803003, -15486431, -16334057, -16205799, -1513240, -1250068, -16205799, -16465387, -15749348, -12803003, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -13132738, -15884266, -16402157, -16530157, -16530157, -16402157, -15949803, -13132738, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -7817583, -11756206, -15694829, -15694829, -11756206, -7817583, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}), verboseOption);
    addTag("Stop", new IntVector(new int[] {16, 16, 0, 0, 0, 0, 1619823756, -1081308020, -7566196, -7566196, -7566196, -7566196, -1081308020, 1619823756, 0, 0, 0, 0, 0, 0, 546081932, -812872564, -6645094, -2829100, -921103, -1, -1, -921103, -2829100, -6645094, -812872564, 546081932, 0, 0, 0, 546081932, -276001652, -4737097, -1, -466206, -1267287, -1733492, -1733492, -1267287, -466206, -1, -4737097, -276001652, 546081932, 0, 0, -812872564, -4737097, -1, -1398616, -2398876, -2265490, -1333080, -1199438, -2265490, -2398876, -1398616, -1, -4737097, -812872564, 0, 1619823756, -6645094, -1, -1464409, -2595998, -2595998, -1330767, -1, -1, -1330767, -2595998, -2595998, -1464409, -1, -6645094, 1619823756, -1081308020, -2829100, -531999, -2793120, -2793120, -2793120, -2127737, -1, -1, -1729381, -2793120, -2793120, -2793120, -531999, -2829100, -1081308020, -7566196, -921103, -1661532, -2990242, -2990242, -2990242, -2791064, -1, -1, -2458244, -2990242, -2990242, -2990242, -1661532, -921103, -7566196, -7566196, -1, -2390396, -3187365, -3187365, -3187365, -3187365, -398358, -1, -3187365, -3187365, -3187365, -3187365, -2390396, -1, -7566196, -7566196, -1, -2521981, -3384231, -3384231, -3384231, -3384231, -1061685, -862506, -3384231, -3384231, -3384231, -3384231, -2521981, -1, -7566196, -7566196, -921103, -1989984, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -1989984, -921103, -7566196, -1081308020, -2829100, -663329, -3712939, -3712939, -3712939, -3248790, -199180, -1, -3049611, -3712939, -3712939, -3712939, -663329, -2829100, -1081308020, 1619823756, -6645094, -1, -2187106, -3910062, -3910062, -3180942, -1, -1, -2916227, -3910062, -3910062, -2187106, -1, -6645094, 1619823756, 0, -812872564, -4737097, -1, -2318435, -4106928, -4106928, -3378063, -3113092, -4106928, -4106928, -2318435, -1, -4737097, -812872564, 0, 0, 546081932, -276001652, -4737097, -1, -794658, -2384228, -3178886, -3178886, -2384228, -794658, -1, -4737097, -276001652, 546081932, 0, 0, 0, 546081932, -812872564, -6645094, -2829100, -921103, -1, -1, -921103, -2829100, -6645094, -812872564, 546081932, 0, 0, 0, 0, 0, 0, 1619823756, -1081308020, -7566196, -7566196, -7566196, -7566196, -1081308020, 1619823756, 0, 0, 0, 0}), verboseOption);
    addTag("Tick", new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -6500190, -9779851, -12862389, -12862389, -9779851, -6500190, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -11485602, -12400045, -11149978, -10755220, -10755220, -11215771, -12465838, -11485602, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -11617702, -11873958, -11347357, -11807908, -11807908, -11807908, -11807908, -11413150, -12136873, -11617702, -2565928, 2087283049, 390810443, 1112033352, -725631042, -7289964, -12663731, -11939494, -12597424, -12597424, -12597424, -12597424, -13321147, -1, -5903954, -12992439, -7421550, -726420558, 1112033352, 1548043589, -2302757, -10701467, -12071337, -13781698, -13189561, -13584063, -14307786, -14900179, -1907998, -1842205, -1579033, -13979078, -10701467, -3355445, 1548043589, 1816281666, -3158065, -13652677, -12202923, -460552, -14964948, -15687391, -15688159, -2171170, -1907998, -1644826, -15688415, -14964180, -13652677, -3947581, 1816281666, 1849572926, -3355444, -13850313, -5447758, -2763307, -1315861, -15817953, -2171170, -1907998, -1644826, -15818209, -15947747, -15422428, -13850313, -4144960, 1849572926, 1631205946, -3158064, -11162531, -14501326, -2763307, -2434342, -2171170, -1907998, -1644826, -15882722, -16076773, -16076773, -15618527, -11162531, -4144958, 1631205946, 1194800951, -676681044, -8013940, -14701525, -15684575, -2171170, -1907998, -1644826, -15947235, -16271080, -16271080, -16271080, -15226845, -8079733, -677404766, 1194800951, 422851636, -2041491117, -3223852, -12803003, -15486431, -15945955, -1644826, -16077029, -16465387, -16465387, -16465387, -15749348, -12803003, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -13132738, -15884266, -16402157, -16530157, -16530157, -16402157, -15949803, -13132738, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -7817583, -11756206, -15694829, -15694829, -11756206, -7817583, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}), verboseOption);
    addTag("Yellow", new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16637044, 16637044, 16637044, 217963123, 2130565233, -956508818, -50539925, -50540953, -956511901, 2130429278, 217825369, 16497492, 16430928, 16429644, 16777215, 16777215, 16636274, 16636274, 1073600881, -553855121, -137533, -69158, -1815, -1817, -70192, -140634, -553927595, 1073395537, 16429644, 16428612, 16777215, 16777215, 16569966, 1375524462, -139613, -70449, -3892, -5965, -8048, -8822, -7521, -5707, -138831, -277646, 1375383108, 16427578, 16777215, 16777215, 318558570, -201535126, -70967, -6484, -7795, -8568, -9085, -9603, -10377, -10894, -10112, -74590, -201676230, 318350638, 16777215, 16777215, -2063807387, -139346, -7789, -8826, -9086, -9602, -10119, -10636, -11410, -12184, -12959, -12447, -211344, -2064015582, 16777215, 16777215, -805583008, -74354, -9346, -9864, -10379, -10639, -11155, -11927, -12444, -13220, -13998, -14260, -80305, -805725674, 16777215, 16777215, -100941478, -9599, -10895, -11410, -11669, -12184, -12700, -13217, -13993, -14770, -15548, -16069, -15552, -252144374, 16777215, 16777215, -251938220, -12192, -13740, -14510, -14769, -15283, -15802, -16578, -17356, -18133, -19168, -19690, -18151, -201814016, 16777215, 16777215, -805653426, -79270, -14769, -15543, -16060, -16578, -17097, -17873, -18650, -19428, -20205, -20469, -85746, -805795072, 16777215, 16777215, -2063946171, -213429, -15034, -16837, -17356, -17874, -18392, -19169, -19945, -20721, -21497, -20217, -285695, -2064152832, 16777215, 16777215, 368683063, -201742798, -82116, -17360, -18395, -19170, -19945, -20464, -21240, -21501, -21247, -152573, -201882112, 368542720, 16777215, 16777215, 16359977, 1425645346, -350172, -215513, -18403, -19950, -21238, -21757, -21504, -20992, -218880, -420608, 1425507072, 16220672, 16777215, 16777215, 16358427, 16292116, 1173919245, -554134777, -284657, -85746, -19446, -19706, -152320, -352512, -554204160, 1224180480, 16220416, 16220160, 16777215, 16777215, 16291599, 16290567, 16289792, 217615872, -2114417920, -906524416, -555008, -555520, -906525696, -2114485504, 217547264, 16220160, 16220160, 16220160, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}), verboseOption);
  }

  /**
   * Creates a custom Unit.
   *
   * @param name given Unit name.
   * @param desc given Unit description.
   * @param conversion given Unit conversion factor.
   * @param dimensions given Dimensions.
   * @return The created Unit.
   */
  public Units addUnit(String name, String desc, double conversion, Dimensions dimensions) {
    return addUnit(name, desc, conversion, dimensions, true);
  }

  /**
   * Create Unit.
   *
   * @param name
   * @param desc
   * @param conversion
   * @param dim
   * @param verboseOption
   * @return Units
   */
  private Units addUnit(String name, String desc, double conversion, Dimensions dim, boolean verboseOption) {
    Units unit = getUnit(name, false);
    if (unit == null) {
        say(verboseOption, unitFormat, "Creating Unit", name, desc);
        UserUnits newUnit = sim.getUnitsManager().createUnits("Units");
        newUnit.setPresentationName(name);
        newUnit.setDescription(desc);
        newUnit.setConversion(conversion);
        newUnit.setDimensions(dim);
        return newUnit;
    }
    say(verboseOption, unitFormat, "Unit already exists", name, unit.getDescription());
    return unit;
  }

  /**
   * This is the same as {@see #updateObjectsToFVRepresentation}.
   */
  public void applyFVRepresentationToAllObjects() {
    updateObjectsToFVRepresentation();
  }

  /**
   * Assigns all Parts to a Region using One Boundary per Part Surface.
   *
   * @return Created Region.
   */
  public Region assignAllPartsToRegion() {
    printAction("Assigning All Parts to a Region");
    return assignPartsToRegions(getAllLeafParts(false), false, true, false, false).iterator().next();
  }

  /**
   * Assigns all Parts to a Region with the option to isolate the Default Boundary. In every Region
   * there is a default boundary. Sometimes it is useful to leave it alone/empty specially if one is
   * dealing with Mesh Operations or is frequently introducing new Parts in the Meshing pipeline.
   *
   * @param isolateDefBdry isolate the default boundary? It will be behave as a wall.
   * @return Created Region.
   */
  public Region assignAllPartsToRegion(boolean isolateDefBdry) {
    printAction("Assigning All Parts to a Region");
    return assignPartsToRegions(getAllLeafParts(false), false, true, isolateDefBdry, false).iterator().next();
  }

  /**
   * Assigns all Parts to Regions using <u>one Boundary per Part Surface</u>.
   *
   * @return ArrayList of created Regions.
   */
  public ArrayList<Region> assignAllPartsToRegions() {
    printAction("Assigning All Parts to Regions");
    return assignPartsToRegions(getAllLeafParts(false), false, false, false, false);
  }

  /**
   * Assigns all Parts to Regions.
   *
   * @param singleBoundary One Boundary per Part Surface?
   * @return ArrayList of created Regions.
   */
  public ArrayList<Region> assignAllPartsToRegions(boolean singleBoundary) {
    printAction("Assigning All Parts to Regions");
    return assignPartsToRegions(getAllLeafParts(false), singleBoundary, false, false, false);
  }

  /**
   * Assigns the given Part to a Region.
   *
   * @param gp given Geometry Part.
   * @return Created Region.
   */
  public Region assignPartToRegion(GeometryPart gp) {
    return (Region) assignPartsToRegions(getArrayList(gp), false, true, false, true).get(0);
  }

  /**
   * Assigns the given Part to a Region with the option to isolate the Default Boundary. In every Region
   * there is a default boundary. Sometimes it is useful to leave it alone/empty specially if one is
   * dealing with Mesh Operations or is frequently introducing new Parts in the Meshing pipeline.
   *
   * @param isolateDefBdry isolate the default boundary? It will be behave as a wall.
   * @param gp given Geometry Part.
   * @return Created Region.
   */
  public Region assignPartToRegion(GeometryPart gp, boolean isolateDefBdry) {
    return (Region) assignPartsToRegions(getArrayList(gp), false, true, isolateDefBdry, true).get(0);
  }

  /**
   * Assigns the given Parts to a Region.
   *
   * @param ag given Geometry Parts.
   * @return Created Region.
   */
  public Region assignPartsToRegion(ArrayList<GeometryPart> ag) {
    return assignPartsToRegions(ag, false, true, false, true).iterator().next();
  }

  /**
   * Assigns the given Parts to Regions.
   *
   * @param ag given Geometry Parts.
   * @param singleBoundary One Boundary per Part Surface?
   * @return ArrayList of created Regions.
   */
  public ArrayList<Region> assignPartsToRegions(ArrayList<GeometryPart> ag, boolean singleBoundary) {
    return assignPartsToRegions(ag, singleBoundary, false, false, true);
  }

  /**
   * @param ag
   * @param singleBoundary
   * @param singleRegion
   * @param isolateDefBdry
   * @param verboseOption
   * @return ArrayList<Region>
   */
  private ArrayList<Region> assignPartsToRegions(ArrayList<GeometryPart> ag,
            boolean singleBoundary, boolean singleRegion, boolean isolateDefBdry, boolean verboseOption) {
    String bdryMode, regionMode;
    if (singleRegion) {
        regionMode = "OneRegion";
        printAction("Assigning Parts to a Single Region", verboseOption);
    } else {
        regionMode = "OneRegionPerPart";
        printAction("Assigning Parts to Different Regions", verboseOption);
    }
    if (singleBoundary) {
        bdryMode = "OneBoundary";
    } else {
        bdryMode = "OneBoundaryPerPartSurface";
    }
    say("Boundary Mode: " + bdryMode);
    say("Number of Parts: " + ag.size());
    if (singleRegion && ag.size() == 1) {
        regionMode = "OneRegionPerPart";
    }
    for (GeometryPart gp : ag) {
        say("  " + gp.getPathInHierarchy());
    }
    ArrayList<Region> currentRegions = getRegions(".*", false);
    RegionManager regMngr = sim.getRegionManager();
    regMngr.newRegionsFromParts(ag, regionMode, bdryMode, "OneFeatureCurve", true);
    ArrayList<Region> createdRegions = getRegions(".*", false);
    createdRegions.removeAll(currentRegions);
    if (isolateDefBdry) {
        say("Isolating Default Boundaries...");
        for (Region reg : createdRegions) {
            say("  Region: " + reg.getPresentationName());
            Boundary defBdry = reg.getBoundaryManager().getDefaultBoundary();
            if (defBdry == null) {
                for (Boundary b : getAllBoundariesFromRegion(reg, false, true)) {
                    if (b.getIndex() == 1) {
                        defBdry = b;
                        break;
                    }
                }
            }
            if (defBdry == null) {
                say("     ERROR! Default boundary could not be isolated.");
            } else {
                String name = defBdry.getPresentationName();
                ArrayList<PartSurface> aps = new ArrayList(defBdry.getPartSurfaceGroup().getObjects());
                defBdry.getPartSurfaceGroup().removeObjects(aps);
                defBdry.setPresentationName("default boundary");
                Boundary newBdry = reg.getBoundaryManager().createEmptyBoundary(name);
                newBdry.getPartSurfaceGroup().addObjects(aps);
                say("     Default boundary isolated.");
            }
        }
    }
    say("Regions created: " + createdRegions.size(), verboseOption);
    return createdRegions;
  }

  /**
   * @param phC
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_SegrAirIdealGas(PhysicsContinuum phC) {
    ConstantDensityModel cdm = phC.getModelManager().getModel(ConstantDensityModel.class);
    phC.disableModel(cdm);
    phC.enable(IdealGasModel.class);
    phC.enable(SegregatedFluidTemperatureModel.class);
    return phC;
  }

  /**
   * @param phC
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_AirWaterEMP(PhysicsContinuum phC) {
    updateUnits(false);
    EulerianMultiPhaseModel empm = phC.getModelManager().getModel(EulerianMultiPhaseModel.class);
    ConstantMaterialPropertyMethod cmpm = null;
    Model model = null;
    for (int i = 1; i <= 2; i++) {
        EulerianPhase ep = empm.createPhase();
        if (i == 1) {
            ep.setPresentationName("Water");
            ep.enable(SinglePhaseLiquidModel.class);
            ep.enable(ConstantDensityModel.class);
            model = ep.getModelManager().getModel(SinglePhaseLiquidModel.class);
            cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
            setMatPropMeth(cmpm, denWater, defUnitDen);
            cmpm = getMatPropMeth_Const(model, DynamicViscosityProperty.class);
            setMatPropMeth(cmpm, viscWater, defUnitVisc);
        } else {
            ep.setPresentationName("Air");
            ep.enable(SinglePhaseGasModel.class);
            ep.enable(ConstantDensityModel.class);
            model = ep.getModelManager().getModel(SinglePhaseGasModel.class);
            cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
            setMatPropMeth(cmpm, denAir, defUnitDen);
            cmpm = getMatPropMeth_Const(model, DynamicViscosityProperty.class);
            setMatPropMeth(cmpm, viscAir, defUnitVisc);
        }
    }
    return phC;
  }

  /**
   * @param phC
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_SegrFlTemp(PhysicsContinuum phC) {
    phC.enable(SegregatedFluidTemperatureModel.class);
    updateUnits(false);
    setInitialCondition_T(phC, refT, false);
    return phC;
  }

  /**
   * @param phC
   * @param thermalExpansion
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_SegrFlBoussinesq(PhysicsContinuum phC, double thermalExpansion) {
    phC.enable(GravityModel.class);
    phC.enable(BoussinesqModel.class);
    updateUnits(false);
    Model model = phC.getModelManager().getModel(SingleComponentGasModel.class);
    ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(model, ThermalExpansionProperty.class);
    cmpm.getQuantity().setValue(thermalExpansion);
    return phC;
  }

  /**
   * @param phC
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_TurbKEps2Lyr(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(KEpsilonTurbulence.class);
    phC.enable(RkeTwoLayerTurbModel.class);
    phC.enable(KeTwoLayerAllYplusWallTreatment.class);
    updateUnits(false);
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, false);
    return phC;
  }

  /**
   * @param phC
   * @return PhysicsContinuum
   */
  private PhysicsContinuum changePhysics_TurbSA_AllWall(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(SpalartAllmarasTurbulence.class);
    phC.enable(SaTurbModel.class);
    phC.enable(SaAllYplusWallTreatment.class);
    updateUnits(false);
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, false);
    return phC;
  }

  /**
   * When using {@see #getCameraViews}, several temporary Camera Views are created. This method gets
   * rid of all of them.
   * <p>
   * This method is also called automatically in {@see #_finalize}.
   */
  public void cleanUpTemporaryCameraViews() {
    cleanUpTemporaryCameraViews(true);
  }

  private void cleanUpTemporaryCameraViews(boolean verboseOption) {
    ArrayList<VisView> colVV = getCameraViews(tempCamName + ".*", false);
    int size = colVV.size();
    if (colVV.size() > 0) {
        printAction("Removing Temporary Cameras", verboseOption);
        sim.getViewManager().removeObjects(colVV);
        say("Temporary Cameras Removed: " + size);
        sayOK(verboseOption);
    }
  }

  /**
   * This method clears the following:
   * <ol>
   * <li> All Scenes
   * <li> All Mesh Representations
   * <li> All Interfaces
   * <li> All Regions
   * <li> All Parts
   * </ol>
   * It is useful when creating simulation templates.
   */
  public void clearAllMeshesRegionsAndParts() {
    clearScenes();
    clearMeshes();
    clearMeshOperations();
    clearInterfaces();
    clearRegions();
    clearParts();
  }

  /**
   * Removes all Interfaces of the model.
   */
  public void clearInterfaces() {
    clearInterfaces(true);
  }

  /**
   * @param verboseOption
   */
  private void clearInterfaces(boolean verboseOption) {
    printAction("Removing all Interfaces", verboseOption);
    ArrayList<Interface> ai = new ArrayList(sim.getInterfaceManager().getObjects());
    if (ai.isEmpty()) {
        say("No Interfaces found.", verboseOption);
        return;
    }
    say("Removing " + ai.size() + " Interface(s)");
    sim.getInterfaceManager().deleteInterfaces(new Vector(ai));
    sayOK(verboseOption);
  }

  /**
   * Removes all Mesh Representations of the model.
   */
  public void clearMeshes() {
    clearMeshes(true);
  }

  /**
   * Removes the Mesh Representations of the model, except the Volume Mesh. Use
   *    {@see #clearMeshes_SurfaceRepresentations} otherwise.
   */
  public void clearMeshes_SurfaceRepresentations() {
    clearMeshes(false);
  }

  private void clearMeshes(boolean volMsh) {
    printAction("Removing all Mesh Representations");
    removeViewFactors();
    if (volMsh && hasValidVolumeMesh()) {
        try {
            sim.getRepresentationManager().removeObjects(queryVolumeMesh());
            say("Volume Mesh removed.");
        } catch (Exception e1) {
            say("ERROR removing Volume Mesh.");
        }
    }
    try {
        sim.getRepresentationManager().removeObjects(queryRemeshedSurface());
        say("Remeshed Surface Representation removed.");
    } catch (Exception e1) {
        say("Remeshed Surface Representation not found.");
    }
    try {
        sim.getRepresentationManager().removeObjects(queryWrappedSurface());
        say("Wrapped Surface Representation removed.");
    } catch (Exception e1) {
        say("Wrapped Surface Representation not found.");
    }
    try {
        sim.getRepresentationManager().removeObjects(queryInitialSurface());
        say("Initial Surface Representation removed.");
    } catch (Exception e1) {
        say("ERROR removing Initial Surface Representation.");
    }
  }

  /**
   * Removes all Operations of the model.
   */
  public void clearMeshOperations() {
    clearMeshOperations(true);
  }

  /**
   * @param verboseOption
   */
  private void clearMeshOperations(boolean verboseOption) {
    printAction("Removing all Mesh Operations", verboseOption);
    ArrayList<MeshOperation> mo = new ArrayList(sim.get(MeshOperationManager.class).getOrderedOperations());
    if (!mo.isEmpty()) {
        try {
            say("Removing Mesh Operations...");
            sim.get(MeshOperationManager.class).removeObjects(mo);
        } catch (Exception e0) {
            say("Error removing Leaf Parts. Skipping...");
        }
    }
  }

  /**
   * Removes all Regions of the model.
   */
  public void clearRegions() {
    printAction("Removing all Regions");
    ArrayList<Region> ar = getRegions(".*", false);
    if (ar.isEmpty()) {
        say("No Regions found.");
        return;
    }
    say("Removing " + ar.size() + " Region(s)...");
    sim.getRegionManager().removeRegions(new Vector(ar));
    sayOK();
  }

  /**
   * Removes all Geometry Parts of the model.
   */
  public void clearParts() {
    clearParts(true);
  }

  /**
   * @param verboseOption
   */
  private void clearParts(boolean verboseOption) {
    printAction("Removing all Parts", verboseOption);
    ArrayList<GeometryPart> gParts = getAllGeometryParts(false);
    if (!gParts.isEmpty()) {
        try {
            say("Removing Geometry Parts...");
            sim.get(SimulationPartManager.class).removeParts(gParts);
        } catch (Exception e0) {
            say("Error removing Leaf Parts. Skipping...");
        }
    }
    ArrayList<GeometryPart> leafParts = getAllLeafParts(false);
    if (!leafParts.isEmpty()) {
        try {
            say("Removing Leaf Parts...");
            sim.get(SimulationPartManager.class).removeParts(leafParts);
        } catch (Exception e1) {
            say("Error removing Leaf Parts. Skipping...");
        }
    }
    ArrayList<CompositePart> ac = getAllCompositeParts(false);
    if (!ac.isEmpty()) {
        try {
            say("Removing Composite Parts...");
            sim.get(SimulationPartManager.class).removeParts(ac);
        } catch (Exception e2) {
            say("Error removing Composite Parts. Skipping...");
        }
    }
    sayOK(verboseOption);
  }

  /**
   * Removes all Scenes of the model.
   */
  public void clearScenes() {
    printAction("Removing all Scenes");
    try{
        ArrayList<Scene> as = new ArrayList(sim.getSceneManager().getScenes());
        say("Removing " + as.size() + " Scene(s)");
        sim.getSceneManager().removeObjects(as);
        sayOK();
    } catch (Exception e) {
        say("No Scenes found.");
    }
  }

  /**
   * Clears the Solution and all Fields are reset.
   */
  public void clearSolution() {
    say("Clearing Solution...");
    sim.getSolution().clearSolution();
    sayOK();
  }

  /**
   * Clears only the Solution History. Fields are kept.
   */
  public void clearSolutionHistory() {
    clearSolution(true, false, false, false);
  }

  /**
   * This method gives you the ability to clears different areas of the Solution.
   *
   * @param clearHistory clear the Solution History?
   * @param clearFields clear the Solution Fields? All variables are reset.
   * @param resetMesh reset the Mesh?
   * @param clearLagrangianDem clear the Lagrangian/DEM Solution?
   */
  public void clearSolution(boolean clearHistory, boolean clearFields, boolean resetMesh,
                                                                    boolean clearLagrangianDem) {
    printAction("Clearing Solution");
    if (clearHistory) {
        say("Clearing Solution History...");
        sim.getSolution().clearSolution(Solution.Clear.History);
    }
    if (clearFields) {
        say("Clearing Solution Fields...");
        sim.getSolution().clearSolution(Solution.Clear.Fields);
    }
    if (resetMesh) {
        say("Resetting Mesh...");
        sim.getSolution().clearSolution(Solution.Clear.Mesh);
    }
    if (clearLagrangianDem) {
        say("Clearing Lagrangian/DEM Solution Fields...");
        sim.getSolution().clearSolution(Solution.Clear.LagrangianDem);
    }
    sayOK();
  }

  /**
   * @param simu
   * @param fnName
   * @param fnDef
   * @return UserFieldFunction
   */
  private UserFieldFunction cloneFieldFunction(Simulation simu, String fnName, String fnDef) {
    UserFieldFunction f = simu.getFieldFunctionManager().createFieldFunction();
    f.setPresentationName(fnName);
    f.setFunctionName(fnName);
    f.setDefinition(fnDef);
    return f;
  }

  /**
   * Combine all boundaries from a Region.
   *
   * @param r given Region.
   * @return Combined Boundary.
   */
  public Boundary combineBoundaries(Region r) {
    printAction("Combining All Boundaries from Region");
    sayRegion(r, true);
    return combineBoundaries(new ArrayList(getAllBoundariesFromRegion(r)), true);
  }

  /**
   * Combine several boundaries.
   *
   * @param ab given ArrayList of Boundaries. Make sure they all belong to the same Region.
   * @return combined Boundary.
   */
  public Boundary combineBoundaries(ArrayList<Boundary> ab) {
    printAction("Combining Boundaries");
    return combineBoundaries(ab, true);
  }

  private Boundary combineBoundaries(ArrayList<Boundary> ab, boolean verboseOption) {
    say("Boundaries provided: " + ab.size(), verboseOption);
    Boundary b0 = ab.get(0);
    if (ab.size() < 2) {
        say("Not enough boundaries to combine. Skipping...", verboseOption);
    } else {
        if (hasValidVolumeMesh()) {
            sim.getMeshManager().combineBoundaries(new Vector(ab));
        } else {
            ArrayList<PartSurface> alp = new ArrayList();
            for (Boundary b : ab) {
                sayBdry(b, true, false);
                ArrayList<PartSurface> alpb = new ArrayList(b.getPartSurfaceGroup().getObjects());
                alp.addAll(alpb);
                b.getPartSurfaceGroup().removeObjects(alpb);
            }
            ab.remove(b0);
            Region r = b0.getRegion();
            r.getBoundaryManager().removeObjects(ab);
            b0.getPartSurfaceGroup().addObjects(alp);
        }
        sayOK(verboseOption);
    }
    return b0;
  }

  /**
   * Combines Geometry Parts based on REGEX search criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @param combinePartSurfaces Combine all the Part Surfaces?
   * @return The combined Geometry Part.
   */
  public GeometryPart combineGeometryParts(String regexPatt, boolean combinePartSurfaces) {
    return combineGeometryParts(regexPatt, combinePartSurfaces, true);
  }

  private GeometryPart combineGeometryParts(String regexPatt, boolean combinePS, boolean verboseOption) {
    printAction(String.format("Combining Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<MeshPart> am = new ArrayList();
    ArrayList<GeometryPart> ag = getAllLeafParts(regexPatt, false);
    for (int i = 0; i < ag.size(); i++) {
        if (i == 0) {
            continue;
        }
        am.add((MeshPart) ag.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts((MeshPart) ag.get(0), am);
    CadPart c = (CadPart) ag.get(0);
    say("Combined into: " + c.getPathInHierarchy(), verboseOption);
    if (combinePS) {
        say("Combining Part Surfaces..", verboseOption);
        ArrayList<PartSurface> aps = new ArrayList(c.getPartSurfaces());
        int n = aps.size();
        c.combinePartSurfaces(aps);
        String name = c.getPartSurfaces().iterator().next().getPresentationName();
        say("Combined " + n + " Part Surfaces into: " + name);
    }
    sayOK(verboseOption);
    return c;
  }

  /**
   * Combines Leaf Mesh Parts based on REGEX search criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The combined Leaf Mesh Part.
   */
  public LeafMeshPart combineLeafMeshPartsByName(String regexPatt) {
    return combineLeafMeshPartsByName(regexPatt, true);
  }

  private LeafMeshPart combineLeafMeshPartsByName(String regexPatt, boolean verboseOption) {
    printAction(String.format("Combining Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<MeshPart> am = new ArrayList();
    ArrayList<LeafMeshPart> al = getAllLeafMeshParts(regexPatt, false);
    for (int i = 0; i < al.size(); i++) {
        if (i == 0) {
            continue;
        }
        am.add((MeshPart) al.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts(al.get(0), am);
    LeafMeshPart lmp = al.get(0);
    say("Combined into: " + lmp.getPathInHierarchy(), verboseOption);
    sayOK(verboseOption);
    return lmp;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param aps given Part Surfaces.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(ArrayList<PartSurface> aps, String renameTo) {
    PartSurface ps = combinePartSurfaces(aps, true);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param aps given Part Surfaces.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(ArrayList<PartSurface> aps) {
    return combinePartSurfaces(aps, true);
  }

  /**
   * Combine all Part Surfaces from a given Leaf Mesh Part.
   * Rename the combined Part Surface.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(LeafMeshPart leafMshPart, String renameTo) {
    PartSurface ps = combinePartSurfaces(leafMshPart);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine all Part Surfaces from a given Leaf Mesh Part.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(LeafMeshPart leafMshPart) {
    printAction("Getting all Part Surfaces");
    say("Leaf Mesh Part: " + leafMshPart.getPresentationName());
    ArrayList<PartSurface> aps = new ArrayList(leafMshPart.getPartSurfaceManager().getPartSurfaces());
    return combinePartSurfaces(aps, true);
  }

  /**
   * Combine all Part Surfaces searched by REGEX from a given Leaf Mesh Part.
   * Rename the combined Part Surface.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param regexPatt REGEX search pattern.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfacesByName(LeafMeshPart leafMshPart, String regexPatt, String renameTo) {
    PartSurface ps = combinePartSurfacesByName(leafMshPart, regexPatt);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine all Part Surfaces searched by REGEX from a given Leaf Mesh Part.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param regexPatt REGEX search pattern.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfacesByName(LeafMeshPart leafMshPart, String regexPatt) {
    printAction(String.format("Getting Part Surfaces by REGEX pattern: \"%s\"", regexPatt));
    say("Leaf Mesh Part: " + leafMshPart.getPresentationName());
    ArrayList<PartSurface> aps = new ArrayList<PartSurface>();
    for (PartSurface ps : leafMshPart.getPartSurfaceManager().getPartSurfaces()) {
        String name = ps.getPresentationName();
        if (name.matches(regexPatt)) {
            say("  Found: " + name);
            aps.add(ps);
        }
    }
    return combinePartSurfaces(aps, true);
  }

  private PartSurface combinePartSurfaces(ArrayList<PartSurface> alps, boolean verboseOption) {
    printAction("Combining Part Surfaces", verboseOption);
    String myPS = "___myPartSurface";
    say("Part Surfaces available: " + alps.size(), verboseOption);
    if (alps.size() == 1) {
        say("Nothing to combine.", verboseOption);
        return alps.iterator().next();
    }
    for (PartSurface ps : alps) {
        ps.setPresentationName(myPS);
    }
    //-- Combine faces
    GeometryPart gp = alps.iterator().next().getPart();
    if (isCadPart(gp)) {
        ((CadPart) gp).combinePartSurfaces(alps);
    } else if (isBlockPart(gp)) {
        ((SimpleBlockPart) gp).combinePartSurfaces(alps);
    } else if (isSimpleCylinderPart(gp)) {
        ((SimpleCylinderPart) gp).combinePartSurfaces(alps);
    } else if (isSolidModelPart(gp)) {
        ((SolidModelPart) gp).combinePartSurfaces(alps);
    //-- Leave Leaf Part for last.
    } else if (isLeafMeshPart(gp)) {
        ((LeafMeshPart) gp).combinePartSurfaces(alps);
    }
    //-- Reloop to make sure it finds the correct combined Part Surface
    alps = new ArrayList(gp.getPartSurfaces());
    PartSurface foundPS = null;
    for (PartSurface ps : alps) {
        //say(ps.getPresentationName());
        if (ps.getPresentationName().startsWith(myPS)) {
            //say("Found: " + ps.getPresentationName());
            foundPS = ps;
            break;
        }
    }
    say("Combined into: " + foundPS.getPresentationName(), verboseOption);
    sayOK(verboseOption);
    return foundPS;
  }

    /**
     *
     */
    public void convertAllInterfacesToIndirect() {
    printAction("Converting all Interfaces to Indirect");
    for (Object intrfObj : getAllInterfacesAsObjects()) {
        convertInterface_Direct2Indirect((DirectBoundaryInterface) intrfObj);
    }
    sayOK();
  }

    /**
     *
     */
    public void convertAllFluidSolidInterfacesToIndirect() {
    printAction("Converting all Fluid-Solid Direct Interfaces to Indirect");
    for (Object intrfObj : getAllInterfacesAsObjects()) {
        DirectBoundaryInterface i = (DirectBoundaryInterface) intrfObj;
        if (isFluidSolidInterface(i)) {
            convertInterface_Direct2Indirect(i);
        } else {
            say("Not Fluid-Solid interface. Skipping.");
        }
    }
    sayOK();
  }

    /**
     *
     * @param i
     */
    public void convertInterface_Direct2Indirect(DirectBoundaryInterface i) {
    say("Converting a Fluid-Solid Direct to Indirect type Interface");
    say("  Interface name: " + i.getPresentationName());
    Boundary b0 = i.getParentBoundary0();
    Boundary b1 = i.getParentBoundary1();
    say("");
    say("  Removing Direct Interface...");
    sim.getInterfaceManager().deleteInterfaces(i);
    say("  Creating Indirect Interface...");
    sim.getInterfaceManager().createIndirectInterface(b0, b1);
    sayOK();
  }

  /**
   * This method will convert the simulation to 2D <b>erasing the 3D Regions</b>.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void convertTo2D() {
    printAction("Converting Simulation to 2D");
    say("Reading Views from all Scenes");
    ArrayList<Scene> as = getScenes(".*", false);
    ArrayList<CameraStateInput> ac = new ArrayList();
    for (Scene scn : as) {
        ac.add(getCameraStateInput(scn));
    }
    sim.getMeshManager().convertTo2d(1E-06, new Vector(getRegions(".*", false)), true);
    say("Restoring Views for all Scenes");
//    for (Scene scn : as) {
//        scn.setCameraStateInput(ac.get(as.indexOf(scn)));
//    }
    for (Region r : getRegions(".*", false)) {
        r.setMeshContinuum(null);
    }
    clearMeshes_SurfaceRepresentations();
    clearMeshOperations(false);
    clearParts(false);
    say("Number of 2D Cells: " + queryVolumeMesh().getCellCount());
    sayOK();
  }

  /**
   * Creates a Block/Channel 3D-CAD model and creates a Part with 6 Part Surfaces inside, i.e.,
   *    x0, x1, y0, y1, z0 and z1.
   *
   * @param c1 given 3-components array with coordinates. E.g.: {0, -1, -10}.
   * @param c2 given 3-components array with coordinates. E.g.: {1, 1, 1}.
   * @param u given Units.
   * @return The Cad Body.
   */
  public Body create3DCad_Block(double[] c1, double[] c2, Units u) {
    return create3DCad_Block(c1, c2, u, "Channel", true);
  }

  /**
   * Creates a Block/Channel 3D-CAD model and creates a Part with 6 Part Surfaces inside, i.e.,
   *    x0, x1, y0, y1, z0 and z1. The default Tesselation option is used. See {@see #defTessOpt}.
   *
   * @param c1 given 3-components array with coordinates. E.g.: {0, -1, -10}.
   * @param c2 given 3-components array with coordinates. E.g.: {1, 1, 1}.
   * @param u given Units.
   * @param name given Cad Body name.
   * @return The Cad Body.
   */
  public Body create3DCad_Block(double[] c1, double[] c2, Units u, String name) {
    return create3DCad_Block(c1, c2, u, name, true);
  }

  private Body create3DCad_Block(double[] c1, double[] c2, Units u, String name, boolean verboseOption) {
    printAction("Creating a Block/Channel 3D-CAD Model");
    say(verboseOption, "Coordinate 1 = %s %s", retString(c1), u.getPresentationName());
    say(verboseOption, "Coordinate 2 = %s %s", retString(c2), u.getPresentationName());
    CadModel cm = sim.get(SolidModelManager.class).createSolidModel();
    cm.setPresentationName(name + " 3D-CAD Model");
    CanonicalSketchPlane csp = ((CanonicalSketchPlane) cm.getFeatureManager().getObject("XY"));
    TransformSketchPlane newPlane = cm.getFeatureManager().createTransformSketchPlane(csp);
    newPlane.getTranslationVector().setComponents(0., 0., c1[2]);
    newPlane.getTranslationVector().setUnits(u);
    cm.getFeatureManager().execute(newPlane);
    Sketch sketch = cm.getFeatureManager().createSketch(newPlane);
    cm.getFeatureManager().startSketchEdit(sketch);
    double f = u.getConversion();
    sketch.createRectangle(getDoubleVector(c1[0] * f, c1[1] * f), getDoubleVector(c2[0] * f, c2[1] * f));
    cm.getFeatureManager().stopSketchEdit(sketch, true);
    cm.getFeatureManager().rollForwardToEnd();
    Body body = create3DCad_Extrusion(cm, sketch, (c2[2] - c1[2]), u);
    body.setPresentationName(name);
    ((Face) body.getFaceManager().getObject("Face 1")).setNameAttribute("x0");
    ((Face) body.getFaceManager().getObject("Face 3")).setNameAttribute("x1");
    ((Face) body.getFaceManager().getObject("Face 4")).setNameAttribute("y0");
    ((Face) body.getFaceManager().getObject("Face 2")).setNameAttribute("y1");
    ((Face) body.getFaceManager().getObject("Face 6")).setNameAttribute("z0");
    ((Face) body.getFaceManager().getObject("Face 5")).setNameAttribute("z1");
    sim.get(SolidModelManager.class).endEditCadModel(cm);
    say("Creating a Part...", verboseOption);
    cm.createParts(new NeoObjectVector(new Object[] {body}), "SharpEdges", 30.0, defTessOpt, false, 1.0E-5);
    sayOK(verboseOption);
    return body;
  }

  /**
   * Creates a Cylinder 3D-CAD model and creates a Part using the default Tesselation option.
   * See {@see #defTessOpt}.
   *
   * @param r given Radius.
   * @param l given Length.
   * @param org given origin as a 3-components array with coordinates. E.g.: {0, -1, -10}.
   * @param u given Units.
   * @param dir given extrusion direction. Options: {@see #X}, {@see #Y} or {@see #Z}.
   * @return The Cad Body.
   */
  public Body create3DCad_Cylinder(double r, double l, double[] org, Units u, String dir) {
    return create3DCad_Cylinder(r, l, org, u, dir, "Cylinder", true);
  }

  /**
   * Creates a Cylinder 3D-CAD model and creates a Part.
   *
   * @param r given Radius.
   * @param l given Length.
   * @param org given origin as a 3-components array with coordinates. E.g.: {0, -1, -10}.
   * @param u given Units.
   * @param dir given extrusion direction. Options: {@see #X}, {@see #Y} or {@see #Z}.
   * @param name given Cad Body name.
   * @return The Cad Body.
   */
  public Body create3DCad_Cylinder(double r, double l, double[] org, Units u, String dir, String name) {
    return create3DCad_Cylinder(r, l, org, u, dir, name, true);
  }

  private Body create3DCad_Cylinder(double r, double l, double[] org, Units u, String dir,
                                                                String name, boolean verboseOption) {
    printAction("Creating a Cylinder 3D-CAD Model");
    say(verboseOption, "Radius = %g%s", r, u.getPresentationName());
    say(verboseOption, "%s Length = %g%s", dir.toUpperCase(), l, u.getPresentationName());
    double rC = r * u.getConversion();
    double[] offsetPlane = {0., 0., 0.};
    double[] sketchCircle = {0., 0.};
    CadModel cm = sim.get(SolidModelManager.class).createSolidModel();
    cm.setPresentationName(name + " 3D-CAD Model");
    String pln = cadDirs.replace(dir.toUpperCase(), "");
    if (dir.equals(X)) {
        offsetPlane[2] = org[0];
        sketchCircle[0] = org[1] * u.getConversion();
        sketchCircle[1] = org[2] * u.getConversion();
    }
    if (dir.equals(Y)) {
        offsetPlane[2] = org[1];
        sketchCircle[0] = org[2] * u.getConversion();
        sketchCircle[1] = org[0] * u.getConversion();
        pln = "ZX";
    }
    if (dir.equals(Z)) {
        offsetPlane[2] = org[2];
        sketchCircle[0] = org[0] * u.getConversion();
        sketchCircle[1] = org[1] * u.getConversion();
    }
    CanonicalSketchPlane csp = ((CanonicalSketchPlane) cm.getFeatureManager().getObject(pln));
    TransformSketchPlane newPlane = cm.getFeatureManager().createTransformSketchPlane(csp);
    newPlane.getTranslationVector().setComponents(offsetPlane[0], offsetPlane[1], offsetPlane[2]);
    newPlane.getTranslationVector().setUnits(u);
    cm.getFeatureManager().execute(newPlane);
    Sketch sketch = cm.getFeatureManager().createSketch(newPlane);
    cm.getFeatureManager().startSketchEdit(sketch);
    CircleSketchPrimitive circle = sketch.createCircle(new DoubleVector(sketchCircle), rC);
    PointSketchPrimitive pt = ((PointSketchPrimitive) sketch.getSketchPrimitiveManager().getObject("Point 1"));
    sketch.createFixationConstraint(pt);
    RadiusDimension rd = sketch.createRadiusDimension(circle, rC, u);
    rd.getRadius().createDesignParameter("Radius");
    cm.getFeatureManager().stopSketchEdit(sketch, true);
    cm.getFeatureManager().rollForwardToEnd();
    Body body = create3DCad_Extrusion(cm, sketch, l, u);
    body.setPresentationName(name);
    Face f0 = ((Face) body.getFaceManager().getObject("Face 3"));
    f0.setNameAttribute(dir.toLowerCase() + "0");
    Face f1 = ((Face) body.getFaceManager().getObject("Face 2"));
    f1.setNameAttribute(dir.toLowerCase() + "1");
    sim.get(SolidModelManager.class).endEditCadModel(cm);
    say("Creating a Part...", verboseOption);
    cm.createParts(new NeoObjectVector(new Object[] {body}), "SharpEdges", 30.0, defTessOpt, false, 1.0E-5);
    sayOK(verboseOption);
    return body;
  }

  private Body create3DCad_Extrusion(CadModel c, Sketch s, double l, Units u) {
    ExtrusionMerge em = c.getFeatureManager().createExtrusionMerge(s);
    ScalarQuantityDesignParameter emv = em.getDistance().createDesignParameter("Length");
    em.setDirectionOption(0);
    em.setExtrudedBodyTypeOption(0);
    emv.getQuantity().setUnits(u);
    emv.getQuantity().setValue(l);
    em.setDistanceOption(0);
    em.setCoordinateSystemOption(0);
    em.setDraftOption(0);
    em.setCoordinateSystem(lab0);
    em.setFace(null);
    em.setBody(null);
    em.setSketch(s);
    em.setPostOption(1);
    em.setExtrusionOption(0);
    c.getFeatureManager().execute(em);
    return c.getBodyManager().getBodies().iterator().next();
  }

  /**
   * Creates/Embeds an Annotation in a Scene.
   *
   * @param scn given Scene.
   * @param annot given Annotation.
   * @return The Annotation Property within the Scene.
   */
  public FixedAspectAnnotationProp createAnnotation(Scene scn, star.vis.Annotation annot) {
    printAction("Embedding an Annotation in a Scene");
    sayScene(scn, true);
    sayOK();
    return (FixedAspectAnnotationProp) scn.getAnnotationPropManager().createPropForAnnotation(annot);
  }

  /**
   * Creates a simple Annotation Text.
   *
   * @param name given Annotation name.
   * @param text given text.
   * @param height given font height. <b>Hint:</b> Use 0 to get the default value.
   * @return The Simple Annotation.
   */
  public SimpleAnnotation createAnnotation(String name, String text, double height) {
    return createAnnotation(name, text, height, new DoubleVector(), true);
  }

  /**
   * Creates a simple Annotation Text.
   *
   * @param name given Annotation name.
   * @param text given text.
   * @param height given font height. <b>Hint:</b> Use 0 to get the default value.
   * @param pos given 3-components of default position. E.g.: new double[] {0, 0, 0}
   * @return The Simple Annotation.
   */
  public SimpleAnnotation createAnnotation(String name, String text, double height, double[] pos) {
    return createAnnotation(name, text, height, new DoubleVector(pos), true);
  }

  private SimpleAnnotation createAnnotation(String n, String t, double h, DoubleVector dv, boolean verboseOption) {
    printAction("Creating a Simple Annotation text", verboseOption);
    sayString(t, verboseOption);
    if (sim.getAnnotationManager().has(n)) {
        say("Annotation already exists. Skipping creation...", verboseOption);
        return (SimpleAnnotation) getAnnotation(n, false);
    }
    SimpleAnnotation annot = sim.getAnnotationManager().createSimpleAnnotation();
    annot.setText(t);
    annot.setFont(fontDefault);
    if (h != 0.) {
        annot.setDefaultHeight(h);
    }
    if (!dv.isEmpty()) {
        annot.setDefaultPosition(dv);
    }
    annot.setPresentationName(n);
    sayOK(verboseOption);
    return annot;
  }

  private FixedAspectAnnotationProp createAnnotation(Scene scn, Report rep, String timeFmt,
                                                                double[] pos, boolean verboseOption) {
    printAction("Creating an Annotation from Report", verboseOption);
    say("Report: " + rep.getPresentationName(), verboseOption);
    ReportAnnotation ra = (ReportAnnotation) getAnnotation(rep.getPresentationName(), false);
    if (ra == null) {
        ra = sim.getAnnotationManager().createReportAnnotation(rep);
    }
    ra.setNumberFormat(timeFmt);
    if (pos.length == 2) {
        ra.setDefaultPosition(new DoubleVector(new double[] {pos[0], pos[1], 0.0}));
    } else if (pos.length == 3) {
        ra.setDefaultPosition(new DoubleVector(pos));
    } else {
        ra.setDefaultPosition(new DoubleVector(new double[] {0.0, 0.0, 0.0}));
    }
    FixedAspectAnnotationProp faap = null;
    if (scn != null) {
        sayScene(scn, verboseOption);
        faap = (FixedAspectAnnotationProp) scn.getAnnotationPropManager().createPropForAnnotation(ra);
    }
    sayOK(verboseOption);
    return faap;
  }

  private void createAnnotation_CFL(Scene scn, String name, String strngFmt, double[] pos,
                                                            String type, boolean verboseOption) {
    Report r;
    printAction("Creating a " + type + " CFL Report", verboseOption);
    if (getReport(name, false) != null) {
        say("Report already exists. Skipping duplicate creation...", verboseOption);
        sayOK(verboseOption);
        return;
    }
    FieldFunction cfl = getFieldFunction(varCFL, false);
    if (type.equals("MAX")) {
        r = createReportMaximum(new ArrayList<NamedObject>(getRegions(".*", false)),
                name, cfl, unit_Dimensionless, false);
    } else {
        r = createReportVolumeAverage(getRegions(".*", false), name, cfl, unit_Dimensionless, false);
    }
    createAnnotation(scn, r, strngFmt, pos, false);
    sayOK(verboseOption);
  }

  /**
   * Creates an Average CFL annotation (from all Regions) and puts it within a Scene.
   *
   * @param scn given Scene.
   * @param name given Annotation Name. E.g.: "maxCFL".
   * @param strngFmt  given string format.
   * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
   */
  public void createAnnotation_AvgCFL(Scene scn, String name, String strngFmt, double[] pos) {
    createAnnotation_CFL(scn, name, strngFmt, pos, "AVG", true);
  }

  /**
   * Creates a Maximum CFL annotation (from all Regions) and puts it within a Scene.
   *
   * @param scn given Scene.
   * @param name given Annotation Name. E.g.: "maxCFL".
   * @param strngFmt  given string format.
   * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
   */
  public void createAnnotation_MaxCFL(Scene scn, String name, String strngFmt, double[] pos) {
    createAnnotation_CFL(scn, name, strngFmt, pos, "MAX", true);
  }

  /**
   * Creates a Time annotation, in seconds and using a <i>%6.2f</i>  format, and puts it within a Scene.
   *
   * @param scn given Scene.
   * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
   */
  public void createAnnotation_Time(Scene scn, double[] pos) {
    createAnnotation_Time(scn, "Time", unit_s, "%6.2f", pos);
  }

  /**
   * Creates a Time annotation and puts it within a Scene.
   *
   * @param scn given Scene.
   * @param name given Annotation Name. E.g.: Time.
   * @param u given Units of time.
   * @param timeFmt given time format string.
   * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
   */
  public void createAnnotation_Time(Scene scn, String name, Units u, String timeFmt, double[] pos) {
    createAnnotation_Time(scn, name, u, timeFmt, pos, true);
  }

  /**
   * @param scn
   * @param name
   * @param u
   * @param timeFmt
   * @param pos
   * @param verboseOption
   */
  private void createAnnotation_Time(Scene scn, String name, Units u, String timeFmt,
                                                            double[] pos, boolean verboseOption) {
    star.vis.Annotation a = getAnnotation(name, false);
    Report r = null;
    if (a != null) {
        say("Annotation already exists. Skipping Annotation creation...", verboseOption);
        try {
            ((ReportAnnotation) a).setNumberFormat(timeFmt);
            scn.getAnnotationPropManager().getObject(a.getPresentationName()).setPosition(new DoubleVector(pos));
        } catch (Exception e) {}
        r = getReport(name);
        sayOK(verboseOption);
    } else {
        r = createReportExpression(name, u, dimTime, "$Time", false);
    }
    createAnnotation(scn, r, timeFmt, pos, false);
  }

  /**
   * Creates an Annotation Text in a Scene.
   *
   * @param scn given Scene.
   * @param text given text.
   * @param height given font height.
   * @param pos given 3-components of position. E.g.: new double[] {0, 0, 0}
   * @return The Annotation Property within the Scene.
   */
  public FixedAspectAnnotationProp createAnnotation_Text(Scene scn, String text, double height, double[] pos) {
    printAction("Creating a Simple Annotation text on a Scene");
    sayScene(scn, true);
    sayString(text, true);
    SimpleAnnotation annot = createAnnotation(text.replace(" ", ""), text, height, new DoubleVector(pos), false);
    sayOK();
    //-- Apparently found a bug in v8.06, if the Scene was created with MacroUtils v3.0 and it is open.
    //-- Workaround is to add the Annotation first, and then open the Scene.
    FixedAspectAnnotationProp f = (FixedAspectAnnotationProp) scn.getAnnotationPropManager().createPropForAnnotation(annot);
    return f;
  }

  /**
   * Creates a Camera View for use in the GUI of STAR-CCM+. The best way of using this method is to
   * record a macro and get the coordinates manually. Then feed into here.
   *
   * @param fp Focal Point: array of 3 components. E.g.: <i>{0, 0, 0}</i>
   * @param pos Position Coordinate. Same as {@param fp} above.
   * @param vu View Up. Same as {@param fp} definition.
   * @param ps Parallel Scale.
   * @param camName given Camera name.
   * @return The created Camera View.
   */
  public VisView createCameraView(double[] fp, double[] pos, double[] vu, double ps, String camName) {
    printAction("Creating a Camera View");
    VisView v = createCameraView(new DoubleVector(fp), new DoubleVector(pos), new DoubleVector(vu), ps);
    v.setPresentationName(camName);
    say(camName + " created.");
    sayOK();
    return v;
  }

  private VisView createCameraView(DoubleVector fp, DoubleVector pos, DoubleVector vu, double ps) {
    VisView v = sim.getViewManager().createView();
    v.setInput(fp, pos, vu, ps, 1, lab0, false);
    return v;
  }

  /**
   * Create a Cell Set for postprocessing.
   *
   * @param ao given ArrayList of NamedObjects.
   * @param name given name.
   * @return The Cell Set.
   */
  public CellSurfacePart createCellSet(ArrayList ao, String name) {
    printAction("Creating Cell Set with Objects");
    say("Objects: " + ao.size());
    for (Object obj : ao) {
        say("  " + obj.toString());
    }
    CellSurfacePart c = sim.getPartManager().createCellSurfacePart(new Vector(ao));
    c.setPresentationName(name);
    sayOK();
    return c;
  }

  /**
   * Creates an One Group Contact Prevention with the supplied Geometry Objects.
   *
   * @param mo given Mesh Operation.
   * @param ago given ArrayList of Geometry Objects.
   * @param val given search floor value.
   * @param u given units for the search floor.
   */
  public void createContactPrevention(MeshOperation mo, ArrayList<GeometryObject> ago, double val, Units u) {
    printAction("Creating a Contact Prevention between Parts");
    sayMeshOp(mo, true);
    if (!isSurfaceWrapperOperation(mo)) {
        say("This is not a Surface Wrapper Mesh Operation. Skipping...");
        return;
    }
    say("Number of Geometry Objects: " + ago.size());
    SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) mo;
    PartsOneGroupContactPreventionSet cp = swamo.getContactPreventionSet().createPartsOneGroupContactPreventionSet();
    cp.getPartSurfaceGroup().setObjects(ago);
    cp.getFloor().setUnits(u);
    cp.getFloor().setValue(val);
    say("Search Floor: " + val + u.getPresentationName());
    sayOK();
  }


  /**
   * Creates an One Group Contact Prevention in a Region with all its Boundaries. Excluding Interfaces.
   *
   * @param r given Region.
   * @param value given Search Floor value.
   * @param u given Search Floor unit.
   */
  public void createContactPrevention(Region r, double value, Units u) {
    ArrayList<Boundary> ar = (ArrayList<Boundary>) getAllBoundariesFromRegion(r, false, true);
    createContactPrevention(r, ar, value, u, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region with given Boundaries.
   *
   * @param r given Region.
   * @param ar given ArrayList of Boundaries.
   * @param value given Search Floor value.
   * @param u given Search Floor unit.
   */
  public void createContactPrevention(Region r, ArrayList<Boundary> ar, double value, Units u) {
    createContactPrevention(r, ar, value, u, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region, based on an Array of REGEX Patterns.
   *
   * @param r given Region.
   * @param regexPattArray given array of REGEX search patterns.
   * @param value given Search Floor value.
   * @param u given Search Floor unit.
   */
  public void createContactPrevention(Region r, String[] regexPattArray, double value, Units u) {
    ArrayList<Boundary> ar = getAllBoundaries(r, regexPattArray, false, true);
    createContactPrevention(r, ar, value, u, true);
  }

  private OneGroupContactPreventionSet createContactPrevention(Region r,
                    ArrayList<Boundary> ar, double value, Units u, boolean verboseOption) {
    printAction("Creating a Contact Prevention between boundaries", verboseOption);
    if (ar.size() < 2) {
        say("Input boundaries: " + ar.size(), verboseOption);
        say("ERROR! Input boundaries number MUST > 2. Skipping...", verboseOption);
        return null;
    }
    OneGroupContactPreventionSet cps = r.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(ar);
    cps.getFloor().setUnits(u);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + u.getPresentationName(), verboseOption);
    sayOK(verboseOption);
    return cps;
  }

  /**
   * Creates a Cylindrical Coordinate System using default length units. See {@see #defUnitLength}.
   *
   * @param org given 3-components double[] array for the Origin. E.g.: {0, 0, 0}.
   * @param b1 given 3-components double[] array for the Basis1. E.g.: {0, 1, 0}.
   * @param b2 given 3-components double[] array for the Basis2. E.g.: {0, 0, 1}.
   * @return The Coordinate System.
   */
  public CylindricalCoordinateSystem createCoordinateSystem_Cylindrical(double[] org,
                                                                    double[] b1, double[] b2) {
    return createCoordinateSystem_Cylindrical(org, b1, b2, true);
  }

  private CylindricalCoordinateSystem createCoordinateSystem_Cylindrical(double[] org,
                                                double[] b1, double[] b2, boolean verboseOption) {
    printAction("Creating a Cylindrical Coordinate System", verboseOption);
    CylindricalCoordinateSystem ccsys = lab0.getLocalCoordinateSystemManager().createLocalCoordinateSystem(CylindricalCoordinateSystem.class, "Cylindrical");
    Coordinate c = ccsys.getOrigin();
    c.setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(org));
    ccsys.setBasis0(new DoubleVector(b1));
    ccsys.setBasis1(new DoubleVector(b2));
    say(verboseOption, "%s created.", ccsys.getPresentationName());
    sayOK(verboseOption);
    return ccsys;
  }

  /**
   * Creates a single-value Isosurface.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param f given Field Function.
   * @param val given Isosurface value.
   * @param u given unit.
   * @return The created Isosurface.
   */
  public IsoPart createDerivedPart_Isosurface(ArrayList<NamedObject> ano, FieldFunction f,
                                                                            double val, Units u) {
    printAction("Creating an Isosurface");
    sayNamedObjects(ano, true);
    sayFieldFunction(f, true);
    NeoObjectVector where = new NeoObjectVector(ano.toArray());
    IsoPart ip = sim.getPartManager().createIsoPart(where, f);
    ip.setMode(0);
    ip.getSingleIsoValue().setValue(val);
    ip.getSingleIsoValue().setUnits(u);
    sayOK();
    return ip;
  }

  /**
   * Creates a Line Probe.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param c1 given coordinates using {@see #defUnitLength}. E.g.: new double[] {0., 0., 0.}
   * @param c2 given coordinates using {@see #defUnitLength}. E.g.: new double[] {0., 1., 0.}
   * @return The created Line.
   */
  public LinePart createDerivedPart_Line(ArrayList<NamedObject> ano, double[] c1, double[] c2, int res) {
    printAction("Creating a Line Probe");
    sayNamedObjects(ano, true);
    NeoObjectVector where = new NeoObjectVector(ano.toArray());
    DoubleVector from = new DoubleVector(c1);
    DoubleVector to = new DoubleVector(c2);
    LinePart lp = sim.getPartManager().createLinePart(where, from, to, res);
    lp.getPoint1Coordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, from);
    lp.getPoint2Coordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, to);
    sayOK();
    return lp;
  }

  /**
   * Creates a Point Probe.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param c given coordinates using {@see #defUnitLength}. E.g.: new double[] {0., 0., 0.}
   * @return The created Point.
   */
  public PointPart createDerivedPart_Point(ArrayList<NamedObject> ano, double[] c) {
    printAction("Creating a Point Probe");
    sayNamedObjects(ano, true);
    NeoObjectVector on = new NeoObjectVector(ano.toArray());
    DoubleVector where = new DoubleVector(c);
    PointPart pp = sim.getPartManager().createPointPart(on, where);
    pp.getPointCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, where);
    sayOK();
    return pp;
  }

  /**
   * Creates a Section Plane. This is similar to {@see #createSectionPlane()}.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param origin given origin coordinates using {@see #defUnitLength}. E.g.: new double[] {0., 0., 0.}
   * @param orientation  given normal orientation coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
   * @return The created Section Plane.
   */
  public PlaneSection createDerivedPart_SectionPlane(ArrayList<NamedObject> ano,
                                                            double[] origin, double[] orientation) {
    printAction("Creating a Section Plane");
    sayNamedObjects(ano, true);
    NeoObjectVector where = new NeoObjectVector(ano.toArray());
    DoubleVector vecOrient = new DoubleVector(orientation);
    DoubleVector vecOrigin = new DoubleVector(origin);
    DoubleVector vecOffsets = new DoubleVector(new double[] {0.0});
    PlaneSection p = (PlaneSection) sim.getPartManager().createImplicitPart(where, vecOrient, vecOrigin, 0, 1, vecOffsets);
    p.getOriginCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, vecOrigin);
    return p;
  }

  /**
   * Creates a Threshold Derived Part containing the input Objects. <b><u>Hint:</u></b> Use an ArrayList
   * to collect the Objects.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param f given Field Function.
   * @param min given Minimum value.
   * @param max given Maximum value.
   * @param u given variable Unit. If <i>null</i> get Default unit.
   * @return The Threshold part.
   */
  public ThresholdPart createDerivedPart_Threshold(ArrayList<NamedObject> ano,
                                        FieldFunction f, double min, double max, Units u) {
    printAction("Creating a Threshold Part");
    sayNamedObjects(ano, true);
    sayFieldFunction(f, true);
    say(retMinMaxString(min, max));
    DoubleVector vecMinMax = new DoubleVector(new double[] {min, max});
    ThresholdPart thr = sim.getPartManager().createThresholdPart(new NeoObjectVector(ano.toArray()), vecMinMax, u, f, 0);
    sayOK();
    return thr;
  }

  /**
   * Creates a Direct Interface Pair.
   *
   * @param b1 given Boundary 1.
   * @param b2 given Boundary 2.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(Boundary b1, Boundary b2) {
    return createDirectInterfacePair(b1, b2, true);
  }

  /**
   * Creates a Direct Interface Pair given a REGEX search pattern.  <p>
   * If it finds more than 2 boundaries, gets the first 2.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(String regexPatt) {
    printAction("Creating Direct Interface Pair by matching names in any regions");
    ArrayList<Boundary> ab = getBoundaries(regexPatt, false, true);
    if (ab.size() == 2) {
        say("Found 2 candidates. Interfacing:");
    } else if (ab.size() > 2) {
        say("Found more than 2 candidates. Interfacing the first two:");
    } else if (ab.size() < 1) {
        say("Could not find 2 candidates. Giving up...");
        return null;
    }
    DirectBoundaryInterface i = createDirectInterfacePair(ab.get(0), ab.get(1), false);
    sayInterface(i);
    sayOK();
    return i;
  }

  private DirectBoundaryInterface createDirectInterfacePair(Boundary b1, Boundary b2, boolean verboseOption) {
    printAction("Creating a Direct Interface Pair", verboseOption);
    DirectBoundaryInterface dbi = sim.getInterfaceManager().createDirectInterface(b1, b2, "In-place");
    dbi.getTopology().setSelected(InterfaceConfigurationOption.IN_PLACE);
    sayInterface(dbi, verboseOption, false);
    sayOK(verboseOption);
    return dbi;
  }

  /**
   * Creates a Scalar Field Function.
   *
   * @param name given name.
   * @param def given Field Function definition.
   * @param dim given Field Function dimensions. Use <b>null</b> for Dimensionless.
   * @return The Field Function
   */
  public FieldFunction createFieldFunction(String name, String def, Dimensions dim) {
    return createFieldFunction(name, def, dim, FF_SCALAR, true);
  }

  /**
   * Creates a Field Function.
   *
   * @param name given name.
   * @param def given Field Function definition.
   * @param dim given Field Function dimensions. Use <b>null</b> for Dimensionless.
   * @param type given Field Function type. Choices are: {@see #FF_ARRAY}, {@see #FF_POSITION},
   *    {@see #FF_SCALAR} or {@see #FF_VECTOR}.
   * @return The Field Function
   */
  public FieldFunction createFieldFunction(String name, String def, Dimensions dim, int type) {
    return createFieldFunction(name, def, dim, type, true);
  }

  private FieldFunction createFieldFunction(String name, String def, Dimensions dim, int type,
                                                                            boolean verboseOption) {
    printAction("Creating a Field Function", verboseOption);
    FieldFunction f = getFieldFunction(name, false);
    if (f != null) {
        say(name + "already exists.", verboseOption);
        return f;
    }
    say("Name: " + name, verboseOption);
    say("Definition: " + def, verboseOption);
    UserFieldFunction uff = sim.getFieldFunctionManager().createFieldFunction();
    uff.setPresentationName(name);
    uff.setFunctionName(name.replaceAll("( |\\(|\\)|)", ""));
//    uff.setFunctionName(name.replaceAll(" ", ""));
    uff.setDefinition(def);
    uff.getTypeOption().setSelected(type);
    if (!(dim == null | dim == unit_Dimensionless.getDimensions())) {
        uff.setDimensions(dim);
    }
    sayOK(verboseOption);
    return uff;
  }

  /**
   * Creates a Folder in the current {@see #simPath} path.
   *
   * @param foldername given folder name.
   * @return
   */
  public File createFolder(String foldername) {
    return createFolder(foldername, true);
  }

  /**
   * Creates a Folder in the current {@see #simPath} path.
   *
   * @param foldername given folder name.
   * @param verboseOption print output messages when inside a loop, for instance? Probably not.
   * @return
   */
  public File createFolder(String foldername, boolean verboseOption) {
    printAction("Creating a Folder", verboseOption);
    try {
        File folder = new File(simPath, foldername);
        if (folder.exists()) {
            say("Folder exists: " + folder.toString(), verboseOption);
            return folder;
        }
        if (folder.mkdir()) {
            say("Folder created: " + foldername, verboseOption);
            sayOK();
            return folder;
        }
    } catch (Exception e) {
        say("Error: " + e.getMessage(), verboseOption);
        say("Returning NULL!", verboseOption);
    }
    return null;
  }

    /**
     *
     * @param b1
     * @param b2
     * @return
     */
    public IndirectBoundaryInterface createIndirectInterfacePair(Boundary b1, Boundary b2) {
    printAction("Creating Indirect Interface Pair given two boundaries");
    IndirectBoundaryInterface i = sim.getInterfaceManager().createIndirectInterface(b1, b2);
    sayInterface(i);
    sayOK();
    return i;
  }

  /**
   * Creates a Continua with Poly + Embedded Thin Meshers. <p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>OFF</b>
   * <li> Prisms Layers are <b>OFF</b>
   * </ul>
   * @return The new Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum createMeshContinua_EmbeddedThinMesher() {
    MeshContinuum continua = createMeshContinuaPoly();
    enableEmbeddedThinMesher(continua);
    sayOK();
    return continua;
  }

  /**
   * Creates a Continua with Poly Mesher.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>ON</b>
   * <li> Prisms Layers are <b>ON</b>
   * </ul>
   * @return The new Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum createMeshContinua_PolyOnly() {
    MeshContinuum continua = createMeshContinuaPoly();
    enableSurfaceProximityRefinement(continua);
    enablePrismLayers(continua);
    sayOK();
    return continua;
  }

  /**
   * Creates a Continua with the <b>PURE</b> Poly Thin Mesher. It is intended for
   * pure conducting solids. Please see Documentation.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>OFF</b>
   * </ul>
   * @return The new Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum createMeshContinua_ThinMesher() {
    MeshContinuum continua = createMeshContinuaThinMesher();
    sayOK();
    return continua;
  }

  /**
   * Creates a Continua with the Trimmer Mesher.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>ON</b>
   * <li> Prisms Layers are <b>ON</b>
   * </ul>
   * @return The new Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum createMeshContinua_Trimmer() {
    MeshContinuum continua = createMeshContinuaTrimmer();
    enableSurfaceProximityRefinement(continua);
    enablePrismLayers(continua);
    sayOK();
    return continua;
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private MeshContinuum createMeshContinuaPoly() {
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNamePoly);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(DualMesherModel.class);
    disableSurfaceProximityRefinement(continua);

    setMeshBaseSize(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    setMeshTetPolyGrowthRate(continua, mshGrowthFactor);

    printMeshParameters(continua);
    return continua;
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private MeshContinuum createMeshContinuaThinMesher() {
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNameThinMesher);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(SolidMesherModel.class);
    disableSurfaceProximityRefinement(continua);
    continua.getModelManager().getModel(SolidMesherModel.class).setOptimize(true);

    setMeshBaseSize(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    continua.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);

    printMeshParameters(continua);
    return continua;
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private MeshContinuum createMeshContinuaTrimmer() {
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNameTrimmer);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(TrimmerMeshingModel.class);

    setMeshBaseSize(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshPerRegionFlag(continua);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    continua.getReferenceValues().get(MaximumCellSize.class).getRelativeSize().setPercentage(mshTrimmerMaxCelSize);
    //-- Growth Rate is not changed anymore sinc v2c
    printMeshParameters(continua);
    return continua;
  }

  /**
   * Creates an Automated Mesh Mesh Operation in a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @param am given ArrayList of Meshers. <u>Hint</u>: use with {@see #getMeshers}.
   * @return The Automated Mesh Operation.
   */
  public AutoMeshOperation createMeshOperation_AutomatedMesh(
                                                ArrayList<GeometryPart> ag, ArrayList<String> am) {
    return createMeshOperation_AutomatedMesh(ag, am, null);
  }

  /**
   * Creates an Automated Mesh Mesh Operation in a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @param am given ArrayList of Meshers. <u>Hint</u>: use with {@see #getMeshers}.
   * @param name given name for the Operation.
   * @return The Automated Mesh Operation.
   */
  public AutoMeshOperation createMeshOperation_AutomatedMesh(ArrayList<GeometryPart> ag,
                                                                ArrayList<String> am, String name) {
    printAction("Creating an Automated Mesh Operation");
    say("Number of Parts: " + ag.size());
    say("Meshers: " + am.toString());
    AutoMeshOperation amo = sim.get(MeshOperationManager.class).createAutoMeshOperation(am, ag);
    setMeshBaseSize(amo, mshBaseSize, defUnitLength, false);
    setMeshSurfaceSizes(amo, mshSrfSizeMin, mshSrfSizeTgt, false);
    setMeshPrismsParameters(amo, prismsLayers, prismsStretching, prismsRelSizeHeight);
    amo.getDefaultValues().get(SurfaceCurvature.class).getSurfaceCurvatureNumPts().setNumPointsAroundCircle(mshSrfCurvNumPoints);
    SurfaceProximity sp = amo.getDefaultValues().get(SurfaceProximity.class);
    sp.setNumPointsInGap(mshProximityPointsInGap);
    sp.getFloor().setUnits(defUnitLength);
    sp.getFloor().setValue(mshProximitySearchFloor);
    if (hasPolyMesher(amo)) {
        DualAutoMesher dam = ((DualAutoMesher) amo.getMeshers().getObject("Polyhedral Mesher"));
        dam.setTetOptimizeCycles(mshOptCycles);
        dam.setTetQualityThreshold(mshQualityThreshold);
    }
    if (hasTrimmerMesher(amo)) {
        amo.getDefaultValues().get(PartsSimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(mshTrimmerGrowthRate);
        amo.getDefaultValues().get(MaximumCellSize.class).getRelativeSize().setPercentage(mshTrimmerMaxCelSize);
    }
    if (name != null) {
        amo.setPresentationName(name);
    }
    say("Created: " + amo.getPresentationName());
    sayOK();
    return amo;
  }

  /**
   * Creates a Custom Surface Control for a Mesh Operation.
   *
   * @param mo given Mesh Operation.
   * @param ago given ArrayList of Geometry Objects.
   * @param name given name for the control.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   * @return The Custom Surface Control.
   */
  public SurfaceCustomMeshControl createMeshOperation_CustomSurfaceControl(MeshOperation mo,
          ArrayList<GeometryObject> ago, String name, double min, double tgt) {
    printAction("Creating a Custom Surface Mesh Control");
    sayMeshOp(mo, true);
    if (!(isAutoMeshOperation(mo) || isSurfaceWrapperOperation(mo))) {
        say("This Mesh Operation can not have Custom Controls. Skipping...");
        return null;
    }
    AutoMeshOperation amo = (AutoMeshOperation) mo;
    say("Number of Objects: " + ago.size());
    SurfaceCustomMeshControl scmc = amo.getCustomMeshControls().createSurfaceControl();
    scmc.setPresentationName(name);
    scmc.getGeometryObjects().setObjects(ago);
    setMeshSurfaceSizes(scmc, min, tgt, false);
    sayOK();
    return scmc;
  }

  /**
   * Creates a Custom Surface Control by Copying a Source Custom Surface Control from
   * another Mesh Operation.
   *
   * @param mo given Target Mesh Operation.
   * @param scmc given Surface Custom Mesh Control.
   * @return The Surface Custom Mesh Control.
   */
  public SurfaceCustomMeshControl createMeshOperation_CustomSurfaceControl(MeshOperation mo,
                                                                        SurfaceCustomMeshControl scmc) {
    printAction("Creating a Custom Surface Mesh Control");
    sayMeshOp(mo, true);
    String name = scmc.getPresentationName();
    say("Copying from: " + name);
    if (!(isAutoMeshOperation(mo) || isSurfaceWrapperOperation(mo))) {
        say("This Mesh Operation can not have Custom Controls. Skipping...");
        return null;
    }
    AutoMeshOperation amo = (AutoMeshOperation) mo;
    SurfaceCustomMeshControl scmc2 = amo.getCustomMeshControls().createSurfaceControl();
    scmc2.setPresentationName(name);
    scmc2.copyProperties(scmc);
    sayOK();
    return scmc2;
  }

  /**
   * Creates an Isotropic Custom Volumetric Control in a Mesh Operation with the given Parts.
   *
   * @param mo given Mesh Operation.
   * @param ag given ArrayList of Geometry Parts.
   * @param name given name for the Control.
   * @param relSize relative size in (<b>%</b>). Zero is ignored.
   */
  public void createMeshOperation_CustomVolumetricControl(MeshOperation mo,
                                        ArrayList<GeometryPart> ag, String name, double relSize) {
    createMeshOperation_CustomVolumetricControl(mo, ag, name, relSize, coord0);
  }

  /**
   * Creates a Custom Volumetric Control in a Mesh Operation with the given Parts. The control can
   * be Isotropic or Anisotropic (Trimmer only).
   *
   * @param mo given Mesh Operation.
   * @param ag given ArrayList of Geometry Parts.
   * @param name given name for the Control.
   * @param relSize relative size in (<b>%</b>). Zero is ignored.
   * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will are ignored.
   */
  public void createMeshOperation_CustomVolumetricControl(MeshOperation mo,
                        ArrayList<GeometryPart> ag, String name, double relSize, double[] relSizes) {
    printAction("Creating a Custom Volumetric Control in a Mesh Operation");
    AutoMeshOperation amo = (AutoMeshOperation) mo;
    sayMeshOp(amo, true);
    say("Number of Parts: " + ag.size());
    VolumeCustomMeshControl vcmc = amo.getCustomMeshControls().createVolumeControl();
    vcmc.setPresentationName(name);
    vcmc.getGeometryObjects().setObjects(ag);
    if (hasPolyMesher(amo) && relSize > 0) {
        vcmc.getCustomConditions().get(VolumeControlDualMesherSizeOption.class).setVolumeControlBaseSizeOption(true);
        vcmc.getCustomValues().get(VolumeControlSize.class).getRelativeSize().setPercentage(relSize);
    }
    TrimmerAnisotropicSize tas = null;
    if (hasTrimmerMesher(amo)) {
        if (relSize > 0) {
            vcmc.getCustomConditions().get(VolumeControlTrimmerSizeOption.class).setVolumeControlBaseSizeOption(true);
            vcmc.getCustomValues().get(VolumeControlSize.class).getRelativeSize().setPercentage(relSize);
        }
        if (relSizes[0] > 0 || relSizes[1] > 0 || relSizes[2] > 0) {
            vcmc.getCustomConditions().get(VolumeControlTrimmerSizeOption.class).setTrimmerAnisotropicSizeOption(true);
            tas = vcmc.getCustomValues().get(TrimmerAnisotropicSize.class);
        }
    }
    say("Isotropic Relative Size (%): " + relSize);
    if (hasTrimmerMesher(amo) && relSizes[0] > 0) {
        say("Relative Size X (%): " + relSizes[0]);
        tas.setXSize(true);
        tas.getRelativeXSize().setPercentage(relSizes[0]);
    }
    if (hasTrimmerMesher(amo) && relSizes[1] > 0) {
        say("Relative Size Y (%): " + relSizes[1]);
        tas.setYSize(true);
        tas.getRelativeYSize().setPercentage(relSizes[1]);
    }
    if (hasTrimmerMesher(amo) && relSizes[2] > 0) {
        say("Relative Size Z (%): " + relSizes[2]);
        tas.setZSize(true);
        tas.getRelativeZSize().setPercentage(relSizes[2]);
    }
    sayOK();
  }

  /**
   * Creates a Directed Mesh Operation in a Channel.
   * <p>
   * Note this method will only work in perfect squared cross sections.
   *
   * @param src given Source Part Surface.
   * @param tgt given Target Part Surface.
   * @param nX given number of points in X-direction.
   * @param nY given number of points in Y-direction.
   * @param nZ given number of points in Z-direction.
   * @return The Directed Mesh Operation.
   */
  public DirectedMeshOperation createMeshOperation_DM_Channel(PartSurface src, PartSurface tgt,
                                                                        int nX, int nY, int nZ) {
    return createMeshOperation_DM_Channel(src, tgt, nX, nY, nZ, true);
  }

  private DirectedMeshOperation createMeshOperation_DM_Channel(PartSurface src, PartSurface tgt,
                                                    int nX, int nY, int nZ, boolean verboseOption) {
    printAction("Creating a Directed Mesh Operation in a Channel", verboseOption);
    DirectedMeshOperation dmo = createMeshOperation_DMO(src, tgt, verboseOption);
    int isX = 0, isY = 0, isZ = 0;
    int nP1 = 2, nP2 = 2, nVol = 2;
    PatchCurve pcX = null, pcY = null, pcZ = null, pc1 = null, pc2 = null;

    DirectedMeshPartCollection dmpc = ((DirectedMeshPartCollection) dmo.getGuidedMeshPartCollectionManager().getObject(src.getPart().getPresentationName()));
    NeoObjectVector srcPSs = new NeoObjectVector(new Object[] {src});
    NeoObjectVector tgtPSs = new NeoObjectVector(new Object[] {tgt});
    dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc, srcPSs, tgtPSs);

    DirectedPatchSourceMesh patchMsh = dmo.getGuidedSurfaceMeshBaseManager().createPatchSourceMesh(srcPSs, dmpc);

    NeoProperty np = patchMsh.autopopulateFeatureEdges();
    //say("NeoProperty np = patchMsh.autopopulateFeatureEdges();");
    //say(np.getHashtable().toString());

    ArrayList<PatchCurve> pcs = getPatchCurves(patchMsh);

    double err = 0.05;
    for (PatchCurve p : pcs) {
        DoubleVector pts = p.getPoints();
        if (getRelativeError(pts.get(0), pts.get(3), true) <= err) {
            isX += 1;
            pcX = p;
            say(p.getPresentationName() + " is on X plane.");
        }
        if (getRelativeError(pts.get(1), pts.get(4), true) <= err) {
            isY += 1;
            pcY = p;
            say(p.getPresentationName() + " is on Y plane.");
        }
        if (getRelativeError(pts.get(2), pts.get(5), true) <= err) {
            isZ += 1;
            pcZ = p;
            say(p.getPresentationName() + " is on Z plane.");
        }
    }
    //say("X = %d; Y = %d; Z = %d.", isX, isY, isZ);

    if (isX == 4) {
        nVol = nX;
        pc1 = pcY;
        nP2 = nY;
        pc2 = pcZ;
        nP1 = nZ;
    } else if (isY == 4) {
        nVol = nY;
        pc1 = pcX;
        nP2 = nX;
        pc2 = pcZ;
        nP1 = nZ;
    } else if (isZ == 4) {
        nVol = nZ;
        pc1 = pcX;
        nP2 = nX;
        pc2 = pcY;
        nP1 = nY;
    }

    patchMsh.defineMeshPatchCurve(pc1, pc1.getStretchingFunction(), 0., 0., nP1, false, false);
    patchMsh.defineMeshPatchCurve(pc2, pc2.getStretchingFunction(), 0., 0., nP2, false, false);

    DirectedMeshDistribution dmd = dmo.getDirectedMeshDistributionManager().createDirectedMeshDistribution(getNeoObjectVector1(dmpc), "Constant");
    dmd.getDefaultValues().get(DirectedMeshNumLayers.class).setNumLayers(nVol);
    dmo.execute();
    sayOK();
    return dmo;
  }

  /**
   * Creates a Directed Mesh Operation in a Pipe, using an O-Grid structure.
   *
   * @param src given Source Part Surface.
   * @param tgt given Target Part Surface.
   * @param nT given number of points in the circumference, i.e., Theta direction.
   * @param nR given number of points radially.
   * @param nVol given number of points for the volume distribution.
   * @param rR given r/R distance for the O-Grid. E.x.: 0.5;
   * @param c given Cylindrical Coordinate System.
   * @return The Directed Mesh Operation.
   */
  public DirectedMeshOperation createMeshOperation_DM_Pipe(PartSurface src, PartSurface tgt,
                            int nT, int nR, int nVol, double rR, CoordinateSystem c) {
    return createMeshOperation_DM_Pipe(src, tgt, nT, nR, nVol, rR, c, true);
  }

  private DirectedMeshOperation createMeshOperation_DM_Pipe(PartSurface src, PartSurface tgt,
        int nT, int nR, int nVol, double rR, CoordinateSystem c, boolean verboseOption) {
    printAction("Creating a Directed Mesh Operation in a Pipe", verboseOption);
    if (!isCylindricalCSYS(c)) {
        say("Not a Cylindrical Coordinate System: " + c.getPresentationName());
        say("Directed Mesh not created.");
        return null;
    }
    DirectedMeshOperation dmo = createMeshOperation_DMO(src, tgt, verboseOption);
    CylindricalCoordinateSystem ccs = (CylindricalCoordinateSystem) c;

    DirectedMeshPartCollection dmpc = ((DirectedMeshPartCollection) dmo.getGuidedMeshPartCollectionManager().getObject(src.getPart().getPresentationName()));
    NeoObjectVector srcPSs = new NeoObjectVector(new Object[] {src});
    NeoObjectVector tgtPSs = new NeoObjectVector(new Object[] {tgt});
    dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc, srcPSs, tgtPSs);

    DirectedPatchSourceMesh patchMsh = dmo.getGuidedSurfaceMeshBaseManager().createPatchSourceMesh(srcPSs, dmpc);

    NeoProperty np = patchMsh.autopopulateFeatureEdges();
    //say("NeoProperty np = patchMsh.autopopulateFeatureEdges();");
    //say(np.getHashtable().toString());

    boolean isBackwards = createMeshOperation_DM_Pipe_buildExternalPatchCurves(patchMsh, ccs, false);
    if (isBackwards) {
        createMeshOperation_DM_Pipe_buildExternalPatchCurves(patchMsh, ccs, isBackwards);
    }
    ArrayList<PatchCurve> pcExts = getPatchCurves(patchMsh);
    PatchCurve pcInt = createMeshOperation_DM_Pipe_buildInternalPatchCurves(patchMsh, ccs, rR, isBackwards);

    if (pcInt == null) {
        return null;
    }

    patchMsh.defineMeshMultiplePatchCurves(getNeoObjectVector2(pcExts), nT, false);
    patchMsh.defineMeshMultiplePatchCurves(getNeoObjectVector1(pcInt), nR, false);
    if (dmSmooths > 0) {
        patchMsh.smoothPatchPolygonMesh(dmSmooths, 0.25, false);
    }

    DirectedMeshDistribution dmd = dmo.getDirectedMeshDistributionManager().createDirectedMeshDistribution(getNeoObjectVector1(dmpc), "Constant");
    dmd.getDefaultValues().get(DirectedMeshNumLayers.class).setNumLayers(nVol);
    dmo.execute();
    sayOK();
    return dmo;
  }

  private boolean createMeshOperation_DM_Pipe_buildExternalPatchCurves(
        DirectedPatchSourceMesh patchMsh, CylindricalCoordinateSystem c, boolean backwards) {
    double r = getRadius(patchMsh, c);
    say("Erasing original Patch Curves...");
    for (PatchCurve pc : getPatchCurves(patchMsh)) {
        patchMsh.deletePatchCurve(pc);
    }
    say("Rebuilding external Patch Curves...");
    //-- Building is always clock-wise (0, 90, 180, 270)
    ArrayList<PatchVertex> placedVs = new ArrayList();
    Vector3 p1 = c.transformCoordinate(new Vector3(r, 0., 0.));
    Vector3 p2 = c.transformCoordinate(new Vector3(r, 90. / 180. * Math.PI, 0.));
    Vector3 p3 = c.transformCoordinate(new Vector3(r, Math.PI, 0));
    Vector3 p4 = c.transformCoordinate(new Vector3(r, 270. / 180. * Math.PI, 0.));
    double[] angles = {0, 90, 180, 270};
    if (backwards) {
        angles = new double[] {0, 270, 180, 90};
        Vector3 p90 = new Vector3(p2);
        Vector3 p180 = new Vector3(p3);
        Vector3 p270 = new Vector3(p4);
        p2 = p270;
        p3 = p180;
        p4 = p90;
        say("Trying angles backwards order...");
    }
    DoubleVector dv = new DoubleVector();
    PatchVertex oldPv;
    for (int i = 0; i < angles.length - 1; i++) {
        say("   Creating Curve: %.0f to %.0f...", angles[i], angles[i+1]);
        switch (i) {
            case 0:
                dv = new DoubleVector(new double[] {p1.x, p1.y, p1.z, p2.x, p2.y, p2.z});
                patchMsh.createPatchCurve(null, null, dv,
                        new StringVector(new String[] {"ON_FEATURE_EDGE", "ON_FEATURE_EDGE"}));
                placedVs.addAll(getPatchVertices(patchMsh));
                continue;
            case 1:
                dv = new DoubleVector(new double[] {p3.x, p3.y, p3.z});
                break;
            case 2:
                dv = new DoubleVector(new double[] {p4.x, p4.y, p4.z});
                break;
        }
        oldPv = placedVs.get(placedVs.size() - 1);
        patchMsh.createPatchCurve(oldPv, null, dv, new StringVector(new String[] {"ON_FEATURE_EDGE"}));
        for (PatchVertex pv : getPatchVertices(patchMsh)) {
            if (!placedVs.contains(pv)) {
                placedVs.add(pv);
            }
        }
    }
    dv.clear();
    say("   Creating Curve: 270 to 0...");
    patchMsh.createPatchCurve(placedVs.get(0), placedVs.get(placedVs.size() - 1), dv, new StringVector(new String[] {}));
    if (placedVs.size() == 4) {
        return false;
    }
    say("Number of Vertices are not 4. Trying backwards...");
    return true;
  }

  private PatchCurve createMeshOperation_DM_Pipe_buildInternalPatchCurves(
        DirectedPatchSourceMesh patchMsh, CylindricalCoordinateSystem c, double rR, boolean backwards) {
    double r = getRadius(patchMsh, c);
    final double toRad = Math.PI / 180.;
    DoubleVector dv = new DoubleVector();
    say("Building internal Patch Curves...");
    //-- Building is always clock-wise (0, 90, 180, 270)
    ArrayList<PatchCurve> placedCs = getPatchCurves(patchMsh);
    ArrayList<PatchVertex> placedVs = getPatchVertices(patchMsh);
    say("   Placed Curves: " + placedCs.size());
    say("   Placed Vertices: " + placedVs.size());
    if (placedVs.size() != 4) {
        say("Number of Vertices are not 4. Please revise.");
        return null;
    }
    Vector3 p1 = c.transformCoordinate(new Vector3(r * rR, 0., 0.));
    Vector3 p2 = c.transformCoordinate(new Vector3(r * rR, 90. * toRad, 0.));
    Vector3 p3 = c.transformCoordinate(new Vector3(r * rR, 180 * toRad, 0));
    Vector3 p4 = c.transformCoordinate(new Vector3(r * rR, 270. * toRad, 0.));
    PatchVertex pv1 = placedVs.get(0);
    PatchVertex pv2 = placedVs.get(1);
    PatchVertex pv3 = placedVs.get(2);
    PatchVertex pv4 = placedVs.get(3);
    int[] angles = {0, 90, 180, 270};
    if (backwards) {
        say("Trying angles in backwards...");
        angles = new int[] {0, 270, 180, 90};
        Vector3 p90 = new Vector3(p2);
        Vector3 p180 = new Vector3(p3);
        Vector3 p270 = new Vector3(p4);
        p2 = p270;
        p3 = p180;
        p4 = p90;
    }
    //say(  pv1.getProjectedCoordinate().toString());
    //--
    PatchVertex pv = null;
    for (int i = 1; i <= 4; i++) {
        switch (i) {
            case 1:
                dv = new DoubleVector(new double[] {p1.x, p1.y, p1.z});
                pv = pv1;
                break;
            case 2:
                dv = new DoubleVector(new double[] {p2.x, p2.y, p2.z});
                pv = pv2;
                break;
            case 3:
                dv = new DoubleVector(new double[] {p3.x, p3.y, p3.z});
                pv = pv3;
                break;
            case 4:
                dv = new DoubleVector(new double[] {p4.x, p4.y, p4.z});
                pv = pv4;
                break;
        }
        //say("DV = " + dv.toString());
        //say("PV = " + pv.getCoordinate().toString());
        //say("PV = " + pv.toString());
        patchMsh.createPatchCurve(pv, null, dv, new StringVector(new String[] {"ON_SURFACE"}));
        PatchVertex p = (PatchVertex) getNewObject(placedVs, getPatchVertices(patchMsh));
        placedVs.add(p);
    }
    //--
    PatchCurve pcInt = (PatchCurve) getNewObject(placedCs, getPatchCurves(patchMsh));
    //--
    PatchVertex pv1r = placedVs.get(4);
    PatchVertex pv2r = placedVs.get(5);
    PatchVertex pv3r = placedVs.get(6);
    PatchVertex pv4r = placedVs.get(7);
    //--
    PatchVertex px = null, py = null;
    double delta = 90. / (dmDiv + 1);
    for (int deg : angles) {
        StringVector sv = new StringVector();
        dv.clear();
        say("   Creating O-Grid angle: %d...", deg);
        //--
        for (int i = 1; i <= dmDiv; i++) {
            double newR = dmOGF * r * rR;
            double newT = deg + i * delta;
            //say(String.format("r = %g     Theta = %g", newR, newT));
            Vector3 v3 = new Vector3(newR, newT * toRad, 0.);
            Vector3 pxy = new Vector3(c.transformCoordinate(v3));
            dv.addAll(new DoubleVector(new double[] {pxy.x, pxy.y, pxy.z}));
            sv.add("ON_SURFACE");
        }
        switch (deg) {
            case 0:
                px = pv1r;
                py = pv2r;
                break;
            case 90:
                px = pv2r;
                py = pv3r;
                break;
            case 180:
                px = pv3r;
                py = pv4r;
                break;
            case 270:
                px = pv4r;
                py = pv1r;
                break;
        }
        patchMsh.createPatchCurve(px, py, dv, sv);
    }
    return pcInt;
  }

  /**
   * Creates a Directed Mesh Operation using an existing Patch Mesh.
   *
   * @param src given Source Part Surface.
   * @param tgt given Target Part Surface.
   * @param nVol given number of points for the volume distribution.
   * @return The Directed Mesh Operation.
   */
  public DirectedMeshOperation createMeshOperation_DM_UseExisting(PartSurface src,
                                                                    PartSurface tgt, int nVol) {
    return createMeshOperation_DM_UseExisting(src, tgt, nVol, true);
  }

  private DirectedMeshOperation createMeshOperation_DM_UseExisting(PartSurface src,
                                            PartSurface tgt, int nVol, boolean verboseOption) {
    printAction("Creating a Directed Mesh Operation using an Existing Patch Mesh", verboseOption);
    DirectedMeshOperation dmo = createMeshOperation_DMO(src, tgt, verboseOption);

    DirectedMeshPartCollection dmpc =
      (DirectedMeshPartCollection) dmo.getGuidedMeshPartCollectionManager().getObjects().iterator().next();

    NeoObjectVector srcPSs = new NeoObjectVector(new Object[] {src});
    NeoObjectVector tgtPSs = new NeoObjectVector(new Object[] {tgt});

    dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc, srcPSs, tgtPSs);
    dmo.getGuidedSurfaceMeshBaseManager().createUseExistingSourceMesh(srcPSs, dmpc);
    dmo.getGuidedSurfaceMeshBaseManager().createDirectedSideMeshDistribution(new NeoObjectVector(new Object[] {dmpc}));

    DirectedSideMeshDistribution vol = ((DirectedSideMeshDistribution) dmo.getGuidedSurfaceMeshBaseManager().getObject("Volume Distribution"));
    vol.setNumLayers(nVol);
    dmo.stopEditingDirectedMeshOperation();

    sayOK(verboseOption);
    return dmo;
  }

  private DirectedMeshOperation createMeshOperation_DMO(PartSurface src,
                                                    PartSurface tgt, boolean verboseOption) {
    sayPart(src.getPart(), verboseOption);
    if (!isDirectedMeshable(src, tgt, verboseOption)) {
        return null;
    }
    DirectedMeshOperation dmo = (DirectedMeshOperation) sim.get(MeshOperationManager.class).createDirectedMeshOperation(getArrayList(src.getPart()));
    dmo.getSourceSurfaceGroup().add(src);
    dmo.getTargetSurfaceGroup().add(tgt);
    return dmo;
  }

  /**
   * Creates a Subtraction Mesh Operation between a set of Geometry Parts. This is the same as
   * {@see #meshOperationSubtractParts}, returning the Mesh Operation only not the resulting Part.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @param tgtGP given target  Geometry Part.
   * @return The Mesh Operation.
   */
  public MeshOperation createMeshOperation_Subtract(ArrayList<GeometryPart> ag, GeometryPart tgtGP) {
    return meshOperationSubtractParts(ag, tgtGP).getOperation();
  }

  /**
   * Creates a Surface Wrap Mesh Operation in a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @return Surface Wrapper The Mesh Operation.
   */
  public SurfaceWrapperAutoMeshOperation createMeshOperation_SurfaceWrapper(ArrayList<GeometryPart> ag) {
    return createMeshOperation_SurfaceWrapper(ag, null);
  }

  /**
   * Creates a Surface Wrap Mesh Operation in a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @param name given name for the Operation. The Part generated will have the same name too.
   * @return Surface Wrapper The Mesh Operation.
   */
  public SurfaceWrapperAutoMeshOperation createMeshOperation_SurfaceWrapper(
                                                      ArrayList<GeometryPart> ag, String name) {
    printAction("Creating a Surface Wrap Operation");
    say("Number of Parts: " + ag.size());
    SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation)
            sim.get(MeshOperationManager.class).createSurfaceWrapperAutoMeshOperation(ag);
    setMeshBaseSize(swamo, mshBaseSize, defUnitLength, false);
    setMeshSurfaceSizes(swamo, mshSrfSizeMin, mshSrfSizeTgt, false);
    swamo.getDefaultValues().get(SurfaceCurvature.class).getSurfaceCurvatureNumPts().setNumPointsAroundCircle(mshSrfCurvNumPoints);
    swamo.getDefaultValues().get(GlobalVolumeOfInterest.class).getVolumeOfInterestOption().setSelected(GlobalVolumeOfInterestOption.LARGEST_INTERNAL);
    swamo.getDefaultValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(mshWrapperFeatureAngle);
    if (name != null) {
        ((MeshOperationPart) sim.get(SimulationPartManager.class).getPart(swamo.getPresentationName())).setPresentationName(name);
        swamo.setPresentationName(name);
    }
    say("Created: " + swamo.getPresentationName());
    sayOK();
    return swamo;
  }

  /**
   * Creates an Unite Mesh Operation between a set of Geometry Parts. This is the same as
   * {@see #meshOperationUniteParts}, returning the Mesh Operation only not the resulting Part.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @return The Mesh Operation.
   */
  public MeshOperation createMeshOperation_Unite(ArrayList<GeometryPart> ag) {
    return meshOperationUniteParts(ag).getOperation();
  }

  /**
   *
   * @param cellSet
   * @return
   */
  public Scene createMeshSceneWithCellSet(CellSurfacePart cellSet) {
    printAction("Creating a Scene with Cell Set Mesh");
    say("Cell Set: " + cellSet.getPresentationName());
    Scene scn = sim.getSceneManager().createScene();
    scn.setPresentationName("Mesh Cell Set");
    PartDisplayer pd = scn.getDisplayerManager().createPartDisplayer("Cell Set");
    pd.initialize();
    pd.addPart(cellSet);
    pd.setSurface(true);
    pd.setMesh(true);
    pd.setColorMode(colourByRegion);
    sayOK();
    return scn;
  }

  /**
   * Creates a Volumetric Control in the given Part.
   *
   * @param continua given Mesh Continua.
   * @param gp given Geometry Part.
   * @param name given name.
   * @param relSize relative size in (<b>%</b>).
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void createMeshVolumetricControl(MeshContinuum continua, GeometryPart gp, String name, double relSize) {
    createMeshVolumetricControl(continua, getArrayList(gp), name, relSize);
  }

  /**
   * Creates an Anisotropic Volumetric Control in the given Part. <b>Only for Trimmer</b>.
   *
   * @param continua given Mesh Continua.
   * @param gp given Geometry Part.
   * @param name given name.
   * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will be ignored.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void createMeshVolumetricControl(MeshContinuum continua, GeometryPart gp, String name, double[] relSizes) {
    if (!isTrimmer(continua)) {
        return;
    }
    printAction("Creating an Anisotropic Volumetric Control in Mesh Continua");
    sayContinua(continua, true);
    sayPart(gp);
    VolumeSource volSrc = continua.getVolumeSources().createVolumeSource();
    volSrc.setPresentationName(name);
    volSrc.getPartGroup().setObjects(Arrays.asList(new GeometryPart[] {gp}));
    volSrc.get(MeshConditionManager.class).get(TrimmerSizeOption.class).setTrimmerAnisotropicSizeOption(true);
    TrimmerAnisotropicSize tas = volSrc.get(MeshValueManager.class).get(TrimmerAnisotropicSize.class);
    if (relSizes[0] > 0) {
        say("Relative Size X (%): " + relSizes[0]);
        tas.setXSize(true);
        tas.getRelativeXSize().setPercentage(relSizes[0]);
    }
    if (relSizes[1] > 0) {
        say("Relative Size Y (%): " + relSizes[1]);
        tas.setYSize(true);
        tas.getRelativeYSize().setPercentage(relSizes[1]);
    }
    if (relSizes[2] > 0) {
        say("Relative Size Z (%): " + relSizes[2]);
        tas.setZSize(true);
        tas.getRelativeZSize().setPercentage(relSizes[2]);
    }
    sayOK();
  }

  /**
   * Creates a Volumetric Control in the given Parts.
   *
   * @param continua given Mesh Continua.
   * @param ag given ArrayList of Geometry Parts.
   * @param name given name.
   * @param relSize relative size in (<b>%</b>).
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void createMeshVolumetricControl(MeshContinuum continua, ArrayList<GeometryPart> ag,
                                                                    String name, double relSize) {
    printAction("Creating a Volumetric Control in Mesh Continua");
    sayContinua(continua, true);
    say("Given Parts: ");
    for (GeometryPart gp : ag) {
        say("  " + gp.getPathInHierarchy());
    }
    VolumeSource volSrc = continua.getVolumeSources().createVolumeSource();
    volSrc.setPresentationName(name);
    volSrc.getPartGroup().setObjects(ag);
    if (isPoly(continua)) {
        volSrc.get(MeshConditionManager.class).get(VolumeSourceDualMesherSizeOption.class).setVolumeSourceDualMesherSizeOption(true);
    }
    if (isTrimmer(continua)) {
        volSrc.get(MeshConditionManager.class).get(TrimmerSizeOption.class).setTrimmerSizeOption(true);
    }
    VolumeSourceSize volSrcSize = volSrc.get(MeshValueManager.class).get(VolumeSourceSize.class);
    ((GenericRelativeSize) volSrcSize.getRelativeSize()).setPercentage(relSize);
    say("Relative Size (%): " + relSize);
    sayOK();
  }

  /**
   * Creates an Anisotropic Wake Refinement in the given Boundary. <b>Only for Trimmer</b>.
   *
   * @param continua given Mesh Continua.
   * @param b given Boundary.
   * @param distance given distance in default units. See {@see #defUnitLength}.
   * @param dir given 3-components direction of the refinement. E.g., in X: {1, 0, 0}.
   * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will be ignored.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void createMeshWakeRefinement(MeshContinuum continua, Boundary b, double distance,
                                                                double[] dir, double[] relSizes) {
    if (!isTrimmer(continua)) {
        return;
    }
    printAction("Creating a Mesh Wake Refinement");
    sayContinua(continua, true);
    sayBdry(b);
    WakeRefinementSet wrs = b.getRegion().get(MeshValueManager.class).get(WakeRefinementSetManager.class).createWakeRefinementSet();
    wrs.setPresentationName(b.getPresentationName());
    wrs.getDirection().setComponents(dir[0], dir[1], dir[2]);
    wrs.getBoundaryFeatureCurveGroup().setObjects(b);
    wrs.getGrowthRateOption().setSelected(GrowthRateOption.MEDIUM);
    wrs.getDistance().setUnits(defUnitLength);
    wrs.getDistance().setValue(distance);
    wrs.setWRTrimmerAnisotropicSizeOption(true);
    WRTrimmerAnisotropicSize wras = wrs.getWRTrimmerAnisotropicSize();
    wras.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.RELATIVE);
    if (relSizes[0] > 0) {
        say("Relative Size X (%): " + relSizes[0]);
        wras.setXSize(true);
        wras.getRelativeXSize().setPercentage(relSizes[0]);
    }
    if (relSizes[1] > 0) {
        say("Relative Size Y (%): " + relSizes[1]);
        wras.setYSize(true);
        wras.getRelativeYSize().setPercentage(relSizes[1]);
    }
    if (relSizes[2] > 0) {
        say("Relative Size Z (%): " + relSizes[2]);
        wras.setZSize(true);
        wras.getRelativeZSize().setPercentage(relSizes[2]);
    }
    sayOK();
  }

  /**
   * Creates the Monitor and a Plot from a Report. The {@see #repMon} and {@see #monPlot} variables
   * will be updated.
   *
   * @param rep given Report.
   * @param repName given Report name.
   * @param xAxisLabel given X-axis labelFmt. If <b>null</b> it is ignored.
   * @param yAxisLabel given Y-axis labelFmt. If <b>null</b> it is ignored.
   * @return The created Report Monitor, which is {@see #repMon}.
   */
  public ReportMonitor createMonitorAndPlotFromReport(Report rep, String repName,
                                                String xAxisLabel, String yAxisLabel) {
    repMon = rep.createMonitor();
    repMon.setPresentationName(repName);
    say("Created Monitor: %s", repName);

    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(repName);
    say("Created Plot: %s", repName);
    monPl.getMonitors().addObjects(repMon);

    Axes axes = monPl.getAxes();
    if (xAxisLabel != null) {
        axes.getXAxis().getTitle().setText(xAxisLabel);
    }
    if (yAxisLabel != null) {
        axes.getYAxis().getTitle().setText(yAxisLabel);
    }
    monPlot = monPl;
    return repMon;
  }

  /**
   * Creates a Field Mean Monitor on the selected Objects.
   * @param ano given ArrayList of NamedObjects.
   * @param f given Field Function.
   * @param ue given Update Event for trigger.
   * @return The Field Mean Monitor.
   */
  public FieldMeanMonitor createMonitor_FieldMean(ArrayList<NamedObject> ano,
                                                            FieldFunction f, UpdateEvent ue) {
    printAction("Creating a Field Mean Monitor");
    FieldMeanMonitor fmm = sim.getMonitorManager().createMonitor(FieldMeanMonitor.class);
    sayFieldFunction(f, true);
    sayNamedObjects(ano, true);
    fmm.setObjects(ano);
    fmm.setFieldFunction(f);
    setUpdateEvent(fmm.getStarUpdate(), ue);
    sayOK();
    return fmm;
  }

  /**
   * Creates a Translation Motion and assigns to a Region.<p>
   * Note the Translation Velocity will be given in default units. See {@see #defUnitVel}.
   *
   * @param definition
   * @param r given Region.
   * @return The create Motion.
   */
  public Motion createMotion_Translation(String definition, Region r) {
    printAction("Creating a Translation Motion");
    say("Definition: " + definition);
    sayRegion(r);
    TranslatingMotion tm = sim.get(MotionManager.class).createMotion(TranslatingMotion.class, "Translation");
    tm.getTranslationVelocity().setDefinition(definition);
    tm.getTranslationVelocity().setUnits(defUnitVel);
    ((MotionSpecification) r.getValues().get(MotionSpecification.class)).setMotion(tm);
    sayOK();
    return tm;
  }

  /**
   * Creates a Single Plot from the selected Report Monitors.
   *
   * @param colRP given ArrayList of Report Monitors.
   * @param plotName given Plot name.
   * @param pm given Plotable Monitor. Use with {@see #getIterationMonitor} or
   *    {@see #getPhysicalTimeMonitor} method.
   * @return The created Monitor Plot.
   */
  public MonitorPlot createPlot(ArrayList<ReportMonitor> colRP, String plotName, PlotableMonitor pm) {
    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(plotName);
    monPl.getMonitors().addObjects(colRP);

    Axes axes = monPl.getAxes();
    Axis _xx = axes.getXAxis();
    _xx.getTitle().setText(pm.getPresentationName());
    Axis _yy = axes.getYAxis();
    _yy.getTitle().setText(colRP.iterator().next().getMonitorDescription());

    return monPl;
  }

  /**
   * Creates a Single Plot XY type from the selected Objects.
   * @param ano given ArrayList of NamedObjects.
   * @param ffx given Field Function for the x-axis.
   * @param ux given units for the Field Function in the x-axis.
   * @param ffy given Field Function for the y-axis.
   * @param uy given units for the Field Function in the y-axis.
   * @return The created XY Plot.
   */
  public XYPlot createPlot_XY(ArrayList<NamedObject> ano, FieldFunction ffx, Units ux,
                                                                    FieldFunction ffy, Units uy) {
    printAction("Creating a XY Plot");
    sayNamedObjects(ano, true);
    if (isVector(ffx)) {
        ffx = ffx.getMagnitudeFunction();
    }
    if (isVector(ffy)) {
        ffy = ffy.getMagnitudeFunction();
    }
    XYPlot xypl = sim.getPlotManager().createXYPlot();
    xypl.refreshAndWait();
    xypl.getParts().setObjects(ano);
    AxisType xax = xypl.getXAxisType();
    xax.setMode(1);      //-- Scalar
    xax.setFieldFunction(ffx);
    xax.setUnits(ux);
    YAxisType yax = ((YAxisType) xypl.getYAxes().getDefaultAxis());
    yax.setFieldFunction(ffy);
    yax.setUnits(uy);
    InternalDataSet ids = ((InternalDataSet) yax.getDataSets().getObjects().iterator().next());
    ids.getSymbolStyle().setStyle(1);
    ids.getSymbolStyle().setSize(12);
    ids.getSymbolStyle().setColor(colorSlateGray);
    sayOK();
    return xypl;
  }

  /**
   * Creates a Surface Wrapper seed point in a Region.
   *
   * @param r given Region.
   * @param X given X coordinate.
   * @param Y given Y coordinate.
   * @param Z given Z coordinate.
   * @param u given unit.
   */
  public void createWrapperSeedPoint(Region r, double X, double Y, double Z, Units u) {
    setMeshWrapperVolumeSeedPoints(r);
    printAction("Creating a Wrapper Seed Point");
    sayRegion(r);
    say(String.format("Point Coordinate: %f, %f, %f [%s]", X, Y, Z, u.toString()));
    VolumeOfInterestSeedPoint seedPt = r.get(MeshValueManager.class).get(VolumeOfInterestSeedPointManager.class).createSeedPoint();
    seedPt.getCoordinates().setUnits(u);
    seedPt.getCoordinates().setComponents(X, Y, Z);
    say("Created: " + seedPt.getPresentationName());
    sayOK();
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air Boussinesq (for Buoyancy)
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedBoussinesqKEps2Lyr() {
    text = "Air / Steady State / Segregated Solver / Boussinesq / k-eps 2 Layers";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    double thermalExpansionAir = 0.00335;
    changePhysics_SegrFlTemp(phC);
    changePhysics_SegrFlBoussinesq(phC, thermalExpansionAir);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Ideal Gas Air
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIdealGasKEps2Lyr() {
    text = "Air / Steady State / Segregated Solver / Ideal Gas / k-eps 2 Layers";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_TurbKEps2Lyr(phC);
    changePhysics_SegrAirIdealGas(phC);
    updateReferenceValues(phC);
    setInitialCondition_Temperature(phC, refT);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Ideal Gas Air
   * <li> Segregated Solver
   * <li> Turbulent Spalart-Allmaras All Wall Y+
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIdealGasSA_AllWall() {
    text = "Air / Steady State / Segregated Solver / Ideal Gas / Spalart-Allmaras All Wall Y+";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_TurbSA_AllWall(phC);
    changePhysics_SegrAirIdealGas(phC);
    updateReferenceValues(phC);
    setInitialCondition_Temperature(phC, refT);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_SegrFlTemp(phC);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_SegrFlTemp(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Turbulent Spalart-Allmaras All Wall Y+
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleSA_AllWallIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Spalart-Allmaras All Wall Y+ / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_TurbSA_AllWall(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Unsteady (Transient)
   * <li> Air-Water Incompressible Eulerian Multiphase Model (VOF).
   * <li> VOF Waves
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Isothermal
   * </ul>
   *
   * <b>Note that Water is the primary Fluid.</b>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirWaterUnsteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Air-Water (VOF) / Unsteady / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_TRN_SegrVOF_Waves_Grav_Lam_CQR(text);
    changePhysics_AirWaterEMP(phC);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Unsteady (Transient)
   * <li> Air-Water Incompressible Eulerian Multiphase Model (VOF).
   * <li> VOF Waves
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * <b>Note that Water is the primary Fluid.</b>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirWaterUnsteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Air-Water (VOF) / Unsteady / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_TRN_SegrVOF_Waves_Grav_Lam_CQR(text);
    changePhysics_AirWaterEMP(phC);
    sayOK();
    updateReferenceValues(phC);
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density Aluminum
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AluminumSteadySegregated() {
    text = "Aluminum / Steady State / Segregated Solver";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD(noneString, text);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible general gas
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * @param den given fluid density in default units. See {@see #defUnitVel}.
   * @param visc given fluid viscosity. See {@see #defUnitVisc}.
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_FluidSteadySegregatedIncompressibleLaminarIsothermal(double den, double visc) {
    text = "Fluid / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    updateReferenceValues(phC);
    SingleComponentGasModel model = phC.getModelManager().getModel(SingleComponentGasModel.class);
    model.getMaterial().setPresentationName("Fluid");
    ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
    setMatPropMeth(cmpm, den, defUnitDen);
    cmpm = getMatPropMeth_Const(model, DynamicViscosityProperty.class);
    setMatPropMeth(cmpm, visc, defUnitVisc);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @param name given Solid name (E.g.: "mySteel").
   * @param den given Density in default Units.
   * @param k given Thermal Conductivity in default Units.
   * @param cp given Specific Heat in default Units.
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_SolidSteadySegregated(String name, double den, double k, double cp) {
    text = "Generic Solid / Steady State / Segregated Solver / Constant Properties";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD(noneString, text);
    SolidModel sldM = phC.getModelManager().getModel(SolidModel.class);
    Solid sld = ((Solid) sldM.getMaterial());
    sld.setPresentationName(name);
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) sld.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod());
    ConstantMaterialPropertyMethod cmpm2 = ((ConstantMaterialPropertyMethod) sld.getMaterialProperties().getMaterialProperty(ThermalConductivityProperty.class).getMethod());
    ConstantSpecificHeat ccp = ((ConstantSpecificHeat) sld.getMaterialProperties().getMaterialProperty(SpecificHeatProperty.class).getMethod());
    say("Name = " + name);
    say("Density = " + den + " " + cmpm.getQuantity().getUnits().getPresentationName());
    say("Thermal Conductivity = " + k + " " + cmpm2.getQuantity().getUnits().getPresentationName());
    say("Specific Heat = " + cp + " " + ccp.getQuantity().getUnits().getPresentationName());
    cmpm.getQuantity().setValue(den);
    cmpm2.getQuantity().setValue(k);
    ccp.getQuantity().setValue(cp);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density Steel (material library: UNSG101000)
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_SteelSteadySegregated() {
    text = "Steel / Steady State / Segregated Solver";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD("UNSG101000_Solid", text);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_SegrFlTemp(phC);
    changePhysics_TurbKEps2Lyr(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_SegrFlTemp(phC);
    updateReferenceValues(phC);
    sayOK();
    return phC;
  }

  private PhysicsContinuum createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(
                                            Class singleComponentClass, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum phC = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    phC.enable(ThreeDimensionalModel.class);
    phC.enable(SteadyModel.class);
    phC.enable(SingleComponentGasModel.class);
    phC.enable(SegregatedFlowModel.class);
    phC.enable(ConstantDensityModel.class);
    phC.enable(LaminarModel.class);
    if (enableCQR) {
        phC.enable(CellQualityRemediationModel.class);
    }
    setInitialCondition_P(phC, p0, false);
    setInitialCondition_Velocity(phC, v0[0], v0[1], v0[2], false);
    return phC;
  }

  private PhysicsContinuum createPhysics_3D_SS_SegrSLD(String solidMaterial, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum sldCont = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    sldCont.enable(ThreeDimensionalModel.class);
    sldCont.enable(SteadyModel.class);
    sldCont.enable(SolidModel.class);
    sldCont.enable(SegregatedSolidEnergyModel.class);
    sldCont.enable(ConstantDensityModel.class);
    if (enableCQR) {
        sldCont.enable(CellQualityRemediationModel.class);
    }
    setInitialCondition_T(sldCont, solidsT0, false);
    if (solidMaterial.matches(noneString)) { return sldCont; }
    SolidModel sm = sldCont.getModelManager().getModel(SolidModel.class);
    Solid solid = ((Solid) sm.getMaterial());
    MaterialDataBase matDB = sim.get(MaterialDataBaseManager.class).getMaterialDataBase("props");
    DataBaseMaterialManager dbMatMngr = matDB.getFolder("Solids");
    DataBaseSolid dbSld = ((DataBaseSolid) dbMatMngr.getMaterial(solidMaterial));
    Solid newSolid = (Solid) sm.replaceMaterial(solid, dbSld);
    return sldCont;
  }

  private PhysicsContinuum createPhysics_3D_TRN_SegrVOF_Waves_Grav_Lam_CQR(String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum vof = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    vof.enable(ThreeDimensionalModel.class);
    vof.enable(ImplicitUnsteadyModel.class);
    vof.enable(MultiPhaseMaterialModel.class);
    vof.enable(SegregatedVofModel.class);
    vof.enable(SegregatedVofFlowModel.class);
    vof.enable(VofWaveModel.class);
    vof.enable(GravityModel.class);
    vof.enable(LaminarModel.class);
    if (enableCQR) {
        vof.enable(CellQualityRemediationModel.class);
    }
    SegregatedVofModel vofM = vof.getModelManager().getModel(SegregatedVofModel.class);
    say("VOF Solver Parameters:");
    say("   Sharpening Factor: " + vofSharpFact);
    say("   Angle Factor: " + vofAngleFact);
    say("   CFL Lower: " + vofCFL_l);
    say("   CFL Upper: " + vofCFL_u);
    vofM.setSharpeningFactor(vofSharpFact);
    vofM.setAngleFactor(vofAngleFact);
    vofM.setCFL_l(vofCFL_l);
    vofM.setCFL_u(vofCFL_u);
  return vof;
  }

  /**
   * Creates a Physics Continua based on models choices.
   *
   * @param spc given Space model. Currently options: {@see #_2D} or {@see #_3D}.
   * @param time given Time model. Currently options: {@see #_STEADY} or {@see #_IMPLICIT_UNSTEADY}.
   * @param mat given Material model. Currently options: {@see #_GAS}, {@see #_LIQUID},
   *        {@see #_SOLID}, {@see #_VOF} or {@see #_VOF_AIR_WATER}.
   * @param slv given Solver strategy. Currently options: {@see #_SEGREGATED} or {@see #_COUPLED}.
   * @param eos given Equation of State. Currently options: {@see #_INCOMPRESSIBLE} or {@see #_IDEAL_GAS}.
   * @param enrgy given Energy model. Currently options: {@see #_ISOTHERMAL} or {@see #_THERMAL}.
   * @param visc given Viscous Regime. Currently options: {@see #_LAMINAR},
   *        {@see #_KE_STD}, {@see #_RKE_2LAYER},
   *        {@see #_KW_STD}, {@see #_KW_2008}, {@see #_SST_KW},
   *        {@see #_DES_SST_KW_IDDES} or {@see #_DES_SST_KW_DDES}.
   * @param grav enable Gravity?
   * @param cqr enable Cell Quality Remediation?
   * @param recom apply Recommendations based on the Selected Models? Hint: True.
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysicsContinua(int spc, int time, int mat, int slv, int eos,
          int enrgy, int visc, boolean grav, boolean cqr, boolean recom) {
    return createPhysicsContinua(spc, time, mat, slv, eos, enrgy, visc, grav, cqr, recom, true);
  }

  private PhysicsContinuum createPhysicsContinua(int spc, int time, int mat, int slv, int eos,
          int enrgy, int visc, boolean grav, boolean cqr, boolean recom, boolean verboseOption) {
    printAction("Creating Physics Continua", verboseOption);
    //--
    //-- Local variables
    PhysicsContinuum pc = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    ArrayList<String> s = new ArrayList();
    boolean useMinMod = false;                                  //-- MinMod Limiter method.
    //--
    //-- Space
    switch (spc) {
        case _2D:
            pc.enable(TwoDimensionalModel.class);
            s.add("2D");
            break;
        case _3D:
            pc.enable(ThreeDimensionalModel.class);
            s.add("3D");
            break;
        default:
            say("Invalid option for Space model. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Time
    switch (time) {
        case _STEADY:
            pc.enable(SteadyModel.class);
            s.add("Steady");
            break;
        case _IMPLICIT_UNSTEADY:
            pc.enable(ImplicitUnsteadyModel.class);
            s.add("Unsteady");
            break;
        case _EXPLICIT_UNSTEADY:
            pc.enable(ExplicitUnsteadyModel.class);
            s.add("Explicit Unsteady");
            useMinMod = true;
            break;
        default:
            say("Invalid option for Time model. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Material
    if (mat == _VOF || mat == _VOF_AIR_WATER) {
        pc.enable(MultiPhaseMaterialModel.class);
        pc.enable(MultiPhaseInteractionModel.class);
    }
    switch (mat) {
        case _GAS:
            pc.enable(SingleComponentGasModel.class);
            s.add("Gas");
            break;
        case _LIQUID:
            pc.enable(SingleComponentLiquidModel.class);
            s.add("Liquid");
            break;
        case _SOLID:
            pc.enable(SolidModel.class);
            s.add("Solid");
            break;
        case _VOF:
            s.add("VOF");
            break;
        case _VOF_AIR_WATER:
            s.add("VOF Air & Water");
            break;
        default:
            say("Invalid option for Material model. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Solver
    switch (slv) {
        case _SEGREGATED:
            pc.enable(SegregatedFlowModel.class);
            s.add("Segregated");
            break;
        case _COUPLED:
            pc.enable(CoupledFlowModel.class);
            s.add("Coupled");
            break;
        default:
            say("Invalid option for Solver model. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Equation of State
    switch (eos) {
        case _INCOMPRESSIBLE:
            pc.enable(ConstantDensityModel.class);
            s.add("Incompressible");
            break;
        case _IDEAL_GAS:
            pc.enable(IdealGasModel.class);
            s.add("Ideal Gas");
            enrgy = _THERMAL;
            break;
        default:
            say("Invalid option for Equation of State. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Energy
    switch (enrgy) {
        case _ISOTHERMAL:
            s.add("Isothermal");
            break;
        case _IDEAL_GAS:
            if (slv == _SEGREGATED) {
                pc.enable(SegregatedFluidTemperatureModel.class);
            } else if (slv == _COUPLED) {
                pc.enable(CoupledEnergyModel.class);
            }
            s.add("Thermal");
            break;
        default:
            say("Invalid option for Energy. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Viscous Regime
    if (visc > _LAMINAR) {
        pc.enable(TurbulentModel.class);
        if (visc < _DES_SST_KW_IDDES) {
            pc.enable(RansTurbulenceModel.class);
        }
    }
    if (visc >= _KE_STD && visc < _KW_STD) {
        pc.enable(KEpsilonTurbulence.class);
    }
    if (visc >= _KW_STD && visc < _DES_SST_KW_IDDES) {
        pc.enable(KOmegaTurbulence.class);
        if (visc < _SST_KW) {
            pc.enable(SkwTurbModel.class);
        }
    }
    if (visc >= _DES_SST_KW_IDDES) {
        pc.enable(DesTurbulenceModel.class);
        pc.enable(SstKwTurbDesModel.class);
        pc.enable(KwAllYplusWallTreatment.class);
        useMinMod = true;
    }
    switch (visc) {
        case _INVISCID:
            pc.enable(InviscidModel.class);
            s.add("Inviscid");
            useMinMod = true;
            break;
        case _LAMINAR:
            pc.enable(LaminarModel.class);
            s.add("Laminar");
            break;
        case _KE_STD:
            pc.enable(RkeTwoLayerTurbModel.class);
            s.add("Standard K-Epsilon high-Re");
            pc.enable(SkeTurbModel.class);
            pc.enable(KeHighYplusWallTreatment.class);
            break;
        case _RKE_2LAYER:
            pc.enable(RkeTwoLayerTurbModel.class);
            pc.enable(KeTwoLayerAllYplusWallTreatment.class);
            s.add("Realizable K-Epsilon 2-layer");
            break;
        case _KW_STD:
            s.add("Standard K-Omega low-Re");
            pc.enable(KwAllYplusWallTreatment.class);
            break;
        case _KW_2008:
            s.add("Revised 2008 K-Omega low-Re");
            pc.enable(KwAllYplusWallTreatment.class);
            SkwTurbModel skwtm = pc.getModelManager().getModel(SkwTurbModel.class);
            skwtm.getKwTurbFreeShearOption().setSelected(KwTurbFreeShearOption.CROSS_DIFFUSION);
            skwtm.setVortexStretching(true);
            skwtm.getKwTurbRealizabilityOption().setSelected(KwTurbRealizabilityOption.DURBIN);
            break;
        case _SST_KW:
            pc.enable(SstKwTurbModel.class);
            pc.enable(KwAllYplusWallTreatment.class);
            s.add("Menter SST low-Re");
            break;
        case _DES_SST_KW_IDDES:
            pc.getModelManager().getModel(SstKwTurbDesModel.class).getDesFormulationOption().setSelected(DesFormulationOption.IDDES);
            s.add("Menter SST IDDES");
            break;
        case _DES_SST_KW_DDES:
            pc.getModelManager().getModel(SstKwTurbDesModel.class).getDesFormulationOption().setSelected(DesFormulationOption.DDES);
            s.add("Menter SST DDES");
            break;
        default:
            say("Invalid option for Energy. Aborting...", verboseOption);
            return null;
    }
    //--
    //-- Miscellaneous
    if (grav) {
        pc.enable(GravityModel.class);
    }
    if (cqr) {
        pc.enable(CellQualityRemediationModel.class);
    }
    updateUnits(true);
    if (useMinMod) {
        //-- Trick learned from Development. Preferred gradient limiter for LES/DES simulations.
        printAction("Using MinMod Limiter Method...", verboseOption);
        GradientsModel gm = pc.getModelManager().getModel(GradientsModel.class);
        gm.addOtherLimitersOption();
        gm.getLimiterMethodOption().setSelected(LimiterMethodOption.MINMOD);
        gm.setUseTVBGradientLimiting(true);
        gm.setAcceptableFieldVariationFactor(0.1);
    }
    printAction("Setting Initial Conditions");
    setInitialCondition(pc, varP, p0, defUnitPress, false);
    setInitialCondition(pc, "Static Temperature", refT, defUnitTemp, false);
    setInitialCondition(pc, varTI, ti0, unit_Dimensionless, false);
    setInitialCondition(pc, varTVR, refT, unit_Dimensionless, false);
    setInitialCondition(pc, varTVS, tvs0, defUnitVel, false);
    setInitialCondition_Velocity(pc, v0[0], v0[1], v0[2], false);
    pc.setPresentationName(retStringBetweenBrackets(s.toString()).replaceAll(", ", " / "));
    if (recom) {
        printAction("Applying Recommendations based on the Models selected");
        enableExpertDriver();
        enableGridSequencing();
        updateSolverSettings();
    }
    printAction("Physics Continua created", verboseOption);
    say(pc.getPresentationName(), verboseOption);
    sayOK(verboseOption);
    return pc;
  }

  /**
   * Creates a Report based on an Expression.
   *
   * @param name given Report name.
   * @param u given units.
   * @param dim given dimensions. E.g.: {@see #dimVel}
   * @param def given Expression definition. E.g.: "$Time".
   * @return The new Report.
   */
  public Report createReportExpression(String name, Units u, Dimensions dim, String def) {
    return createReportExpression(name, u, dim, def, true);
  }

  /**
   * @param name
   * @param u
   * @param dimension
   * @param definition
   * @param verboseOption
   * @return Report
   */
  private Report createReportExpression(String name, Units u, Dimensions dimension,
                                                            String definition, boolean verboseOption) {
    printAction("Creating an Expression Report", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    ExpressionReport er = sim.getReportManager().createReport(ExpressionReport.class);
    say(verboseOption, "%s == %s (%s)", name, definition, u.getPresentationName());
    er.setPresentationName(name);
    er.setDimensions(dimension);
    er.setDefinition(definition);
    er.setUnits(u);
    sayOK(verboseOption);
    return er;
  }

  /**
   * Creates a Force Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param b given Boundary.
   * @param name given Report name.
   * @param direction
   */
  public void createReportForce(Boundary b, String name, double[] direction) {
    createReportForce(getArrayList(b), name, direction);
  }

  /**
   * Creates a Force Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ab given ArrayList of Boundaries.
   * @param name given Report name.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForce(ArrayList<Boundary> ab, String name, double[] direction) {
    createReportForce(ab, name, direction, true);
  }

  private Report createReportForce(ArrayList<Boundary> ab, String name,
                                                        double[] direction, boolean verboseOption) {
    printAction("Creating a Force Report on Boundaries", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayBoundaries(ab);
    ForceReport forceRep = sim.getReportManager().createReport(ForceReport.class);
    forceRep.setPresentationName(name);
    forceRep.getReferencePressure().setUnits(unit_Pa);
    forceRep.getReferencePressure().setValue(0.0);
    forceRep.setUnits(defUnitForce);
    forceRep.getParts().setObjects(ab);
    forceRep.getDirection().setComponents(direction[0], direction[1], direction[2]);
    String unitStr = forceRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(forceRep, name, null, "Force (" + unitStr + ")");
    return forceRep;
  }

  /**
   * Creates a Force Coefficient Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   * <p>
   * <b>The Reference Density will be taken from the Physics Continua associated with the Boundary.</b>
   * <p>
   * The Pressure Coefficient variable will be updated based on the supplied values.
   *
   * @param b given Boundary.
   * @param name given Report name.
   * @param refVel given Reference Velocity in default unit. See {@see #defUnitVel}.
   * @param refArea given Reference Area in default unit. See {@see #defUnitArea}.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForceCoefficient(Boundary b, String name, double refVel,
                                                                double refArea, double[] direction) {
    createReportForceCoefficient(getArrayList(b), name, refVel, refArea, direction);
  }

  /**
   * Creates a Force Coefficient Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated. <p>
   * <p>
   * <b>The Reference Density will be taken from the Physics Continua associated with the Boundary.</b>
   * <p>
   * The Pressure Coefficient variable will be updated based on the supplied values.
   *
   * @param ab given ArrayList of Boundaries.
   * @param name given Report name.
   * @param refVel given Reference Velocity in default unit. See {@see #defUnitVel}.
   * @param refArea given Reference Area in default unit. See {@see #defUnitArea}.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForceCoefficient(ArrayList<Boundary> ab, String name,
                                                double refVel, double refArea, double[] direction) {
    createReportForceCoefficient(ab, name, refVel, refArea, direction, true);
  }

  private Report createReportForceCoefficient(ArrayList<Boundary> ab, String name,
                                    double refVel, double refArea, double[] direction, boolean verboseOption) {
    printAction("Creating a Force Coefficient Report on Boundaries", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayBoundaries(ab, verboseOption);
    ForceCoefficientReport forceCoeffRep = sim.getReportManager().createReport(ForceCoefficientReport.class);
    forceCoeffRep.setPresentationName(name);
    //--
    //-- Reference Density Retrieve
    double den = refDen;
    Units u = unit_kgpm3;
    try {
        Boundary b = ab.iterator().next();
        say("Getting Reference Density from Boundary (Assuming it is a Gas): " + b.getPresentationName());
        PhysicsContinuum pC = b.getRegion().getPhysicsContinuum();
        Model model = pC.getModelManager().getModel(SingleComponentGasModel.class);
        ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
        u = cmpm.getQuantity().getUnits();
        den = cmpm.getQuantity().getValue();
        say("Got: : " + den + u.getPresentationName());
    } catch (Exception e) {
        say("Error getting Reference Density! Using " + den + u.getPresentationName());
    }
    forceCoeffRep.getReferenceDensity().setUnits(u);
    forceCoeffRep.getReferenceDensity().setValue(den);
    //--
    //-- Pressure Coefficient update.
    PressureCoefficientFunction pcp = ((PressureCoefficientFunction) getFieldFunction(varPC, false));
    //pcp.getReferencePressure().setValue(0.0);
    //pcp.getReferencePressure().setUnits(unit_Pa);
    pcp.getReferenceDensity().setValue(den);
    pcp.getReferenceDensity().setUnits(u);
    pcp.getReferenceVelocity().setValue(refVel);
    pcp.getReferenceVelocity().setUnits(defUnitVel);
    //--
    forceCoeffRep.getReferenceVelocity().setUnits(defUnitVel);
    forceCoeffRep.getReferenceVelocity().setValue(refVel);
    forceCoeffRep.getReferenceArea().setUnits(defUnitArea);
    forceCoeffRep.getReferenceArea().setValue(refArea);
    forceCoeffRep.getForceOption().setSelected(ForceReportForceOption.PRESSURE_AND_SHEAR);
    forceCoeffRep.getDirection().setComponents(direction[0], direction[1], direction[2]);
    forceCoeffRep.getReferencePressure().setUnits(unit_Pa);
    forceCoeffRep.getReferencePressure().setValue(0.0);
    forceCoeffRep.getParts().setObjects(ab);
    repMon = createMonitorAndPlotFromReport(forceCoeffRep, name, null, "Force Coefficient");
    return forceCoeffRep;
  }

  /**
   * Creates a Frontal Area Report in the selected Boundaries.
   *
   * @param ab given ArrayList of Boundaries.
   * @param name given Report name.
   * @param viewUp a 3-component array of the screen View Up. E.g.: if Y is showing up in the screen, then {0, 1, 0}.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   * @return The Frontal Area Report.
   */
  public Report createReportFrontalArea(ArrayList<Boundary> ab, String name,
                                                        double[] viewUp, double[] direction) {
    return createReportFrontalArea(ab, name, viewUp, direction, true);
  }

  private Report createReportFrontalArea(ArrayList<Boundary> ab, String name,
                                        double[] viewUp, double[] direction, boolean verboseOption) {
    printAction("Creating a Frontal Area Report on Boundaries", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayBoundaries(ab, verboseOption);
    say("View Up: " + retString(direction), verboseOption);
    say("Flow Direction: " + retString(direction), verboseOption);
    FrontalAreaReport rep = sim.getReportManager().createReport(FrontalAreaReport.class);
    rep.setPresentationName(name);
    rep.getViewUpCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(viewUp));
    rep.getNormalCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(direction));
    rep.getParts().setObjects(ab);
    rep.setUnits(defUnitArea);
    if (verboseOption) {
        rep.printReport();
    }
    sayOK(verboseOption);
    return rep;
  }

  /**
   * Creates a Mass Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param r given Region.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMassAverage(Region r, String name, FieldFunction f, Units u) {
    createReportMassAverage(getArrayList(r), name, f, u);
  }

  /**
   * Creates a Mass Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ar given ArrayList of Regions.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMassAverage(ArrayList<Region> ar, String name, FieldFunction f, Units u) {
    createReportMassAverage(ar, name, f, u, true);
  }

  /**
   * @param ar
   * @param name
   * @param f
   * @param unit
   * @param verboseOption
   * @return Report
   */
  private Report createReportMassAverage(ArrayList<Region> ar, String name, FieldFunction f,
                                                                Units u, boolean verboseOption) {
    printAction("Creating a Mass Average Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayRegions(ar);
    MassAverageReport massAvgRep = sim.getReportManager().createReport(MassAverageReport.class);
    massAvgRep.setScalar(f);
    massAvgRep.setUnits(u);
    massAvgRep.setPresentationName(name);
    massAvgRep.getParts().setObjects(ar);
    String yAxisLabel = "Mass Average of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(massAvgRep, name, null, yAxisLabel);
    return massAvgRep;
  }

  /**
   * Creates a Mass Flow Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param b given Boundary.
   * @param name given Report name.
   * @param u variable corresponding Unit.
   */
  public void createReportMassFlow(Boundary b, String name, Units u) {
    createReportMassFlow(getArrayList(b), name, u);
  }

  /**
   * Creates a Mass Flow Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ab given ArrayList of Boundaries.
   * @param name given Report name.
   * @param u variable corresponding Unit.
   */
  public void createReportMassFlow(ArrayList<Boundary> ab, String name, Units u) {
    createReportMassFlow(ab, name, u, true);
  }

  /**
   * @param ab
   * @param name
   * @param unit
   * @param verboseOption
   * @return Report
   */
  private Report createReportMassFlow(ArrayList<Boundary> ab, String name,
                                                    Units u, boolean verboseOption) {
    printAction("Creating a Mass Flow Report on Boundaries", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayBoundaries(ab);
    MassFlowReport mfRep = sim.getReportManager().createReport(MassFlowReport.class);
    mfRep.setPresentationName(name);
    mfRep.setUnits(u);
    mfRep.getParts().setObjects(ab);
    String unitStr = mfRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(mfRep, name, null, "Mass Flow (" + unitStr + ")");
    return mfRep;
  }

  /**
   * Creates a Mass Flow Average Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param b given Boundary.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMassFlowAverage(Boundary b, String name, FieldFunction f, Units u) {
    createReportMassFlowAverage(getArrayList(b), name, f, u);
  }

  /**
   * Creates a Mass Flow Average Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ab given ArrayList of Boundaries.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMassFlowAverage(ArrayList<Boundary> ab, String name, FieldFunction f, Units u) {
    createReportMassFlowAverage(ab, name, f, u, true, true);
  }

  private Report createReportMassFlowAverage(ArrayList<Boundary> ab, String name,
                                        FieldFunction f, Units u, boolean crPl, boolean verboseOption) {
    printAction("Creating a Mass Flow Average Report of " + f.getPresentationName() + " on Boundaries", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayBoundaries(ab);
    MassFlowAverageReport mfaRep = sim.getReportManager().createReport(MassFlowAverageReport.class);
    mfaRep.setScalar(f);
    mfaRep.setUnits(u);
    mfaRep.setPresentationName(name);
    mfaRep.getParts().setObjects(ab);
    String unitStr = u.getPresentationName();
    String yAxisLabel = "Mass Flow Average of " + f.getPresentationName() + " (" + unitStr + ")";
    if (crPl) {
        repMon = createMonitorAndPlotFromReport(mfaRep, name, null, yAxisLabel);
    }
    return mfaRep;
  }

  /**
   * Creates a Maximum Report in a STAR-CCM+ NamedObject.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param n given NamedObject.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMaximum(NamedObject n, String name, FieldFunction f, Units u) {
    createReportMaximum(getArrayList(n), name, f, u);
  }

  /**
   * Creates a Maximum Report in the selected STAR-CCM+ NamedObjects.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMaximum(ArrayList<NamedObject> ano, String name, FieldFunction f, Units u) {
    createReportMaximum(ano, name, f, u, true);
  }

  private Report createReportMaximum(ArrayList<NamedObject> ano, String name, FieldFunction f,
                                                                Units u, boolean verboseOption) {
    printAction("Creating a Maximum Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayNamedObjects(ano, true);
    MaxReport maxRep = sim.getReportManager().createReport(MaxReport.class);
    maxRep.setScalar(f);
    maxRep.setUnits(u);
    maxRep.setPresentationName(name);
    maxRep.getParts().setObjects(ano);
    String yAxisLabel = "Maximum of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(maxRep, name, null, yAxisLabel);
    return maxRep;
  }

  /**
   * Creates a Minimum Report in a STAR-CCM+ NamedObject.
   * <p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param n given NamedObject.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMinimum(NamedObject n, String name, FieldFunction f, Units u) {
    createReportMinimum(getArrayList(n), name, f, u);
  }

  /**
   * Creates a Minimum Report in the selected NamedObjects.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportMinimum(ArrayList<NamedObject> ano, String name, FieldFunction f, Units u) {
    createReportMinimum(ano, name, f, u, true);
  }

  /**
   * @param ano
   * @param name
   * @param f
   * @param u
   * @param verboseOption
   * @return Report
   */
  private Report createReportMinimum(ArrayList<NamedObject> ano, String name,
                                                    FieldFunction f, Units u, boolean verboseOption) {
    printAction("Creating a Minimum Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    MinReport minRep = sim.getReportManager().createReport(MinReport.class);
    minRep.setScalar(f);
    minRep.setUnits(u);
    minRep.setPresentationName(name);
    minRep.getParts().setObjects(ano);
    String yAxisLabel = "Minimum of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(minRep, name, null, yAxisLabel);
    return minRep;
  }

  /**
   * Creates a Pressure Drop Report. Definition: dP = P1 - P2 in default unit. See {@see #defUnitPress}.
   * <p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param b1 given Boundary 1.
   * @param b2 given Boundary 2.
   * @return The Pressure Drop Report.
   */
  public Report createReportPressureDrop(Boundary b1, Boundary b2) {
    printAction("Creating a Pressure Drop Report using Mass Flow Average");
    sayBdry(b1);
    sayBdry(b2);
    createReportMassFlowAverage(getArrayList(b1), "PressureBoundary1",
            getFieldFunction(varP, false), defUnitPress, false, false);
    createReportMassFlowAverage(getArrayList(b2), "PressureBoundary2",
            getFieldFunction(varP, false), defUnitPress, false, false);
    ExpressionReport dp = (ExpressionReport) createReportExpression("Pressure Drop",
            defUnitPress, dimPress,
            "$PressureBoundary1Report - $PressureBoundary2Report", false);
    say("Expression: " + dp.getDefinition());
    repMon = createMonitorAndPlotFromReport(dp, "Pressure Drop", null, null);
    return dp;
  }

  /**
   * Creates a Sum Report in the selected NamedObjects.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u given units.
   * @return The new Report.
   */
  public Report createReportSum(String name, ArrayList<NamedObject> ano, FieldFunction f, Units u) {
    return createReportSum(ano, name, f, u, true);
  }

  private Report createReportSum(ArrayList<NamedObject> ano, String name, FieldFunction f, Units u,
                                                                                    boolean verboseOption) {
    printAction("Creating an Sum Report", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    SumReport sr = sim.getReportManager().createReport(SumReport.class);
    sr.setObjects(ano);
    sr.setPresentationName(name);
    sr.setScalar(f);
    if (u != null) {
        sr.setUnits(u);
    }
    sayOK(verboseOption);
    return sr;
  }

  /**
   * Creates a Surface Average Report in a STAR-CCM+ NamedObject.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param n given NamedObject.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportSurfaceAverage(NamedObject n, String name, FieldFunction f, Units u) {
    createReportSurfaceAverage(getArrayList(n), name, f, u);
  }

  /**
   * Creates a Surface Average Report in the selected NamedObjects.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportSurfaceAverage(ArrayList<NamedObject> ano, String name, FieldFunction f, Units u) {
    createReportSurfaceAverage(ano, name, f, u, true);
  }

  /**
   * @param ano
   * @param name
   * @param f
   * @param u
   * @param verboseOption
   * @return Report
   */
  private Report createReportSurfaceAverage(ArrayList<NamedObject> ano, String name,
                                                    FieldFunction f, Units u, boolean verboseOption) {
    printAction("Creating a Surface Average Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    AreaAverageReport aar = sim.getReportManager().createReport(AreaAverageReport.class);
    aar.setScalar(f);
    aar.setUnits(u);
    aar.setPresentationName(name);
    aar.getParts().setObjects(ano);
    String yAxisLabel = "Surface Average of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(aar, name, null, yAxisLabel);
    return aar;
  }

  /**
   * Creates a Surface Uniformity Report in a STAR-CCM+ NamedObject.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param n given NamedObject.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportSurfaceUniformity(NamedObject n, String name, FieldFunction f, Units u) {
    createReportSurfaceUniformity(getArrayList(n), name, f, u);
  }

  /**
   * Creates a Surface Uniformity Report in the selected NamedObjects.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportSurfaceUniformity(ArrayList<NamedObject> ano, String name, FieldFunction f, Units u) {
    createReportSurfaceUniformity(ano, name, f, u, true);
  }

  /**
   * @param ano
   * @param name
   * @param f
   * @param u
   * @param verboseOption
   * @return Report
   */
  private Report createReportSurfaceUniformity(ArrayList<NamedObject> ano, String name,
                                                    FieldFunction f, Units u, boolean verboseOption) {
    printAction("Creating a Surface Uniformity Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    SurfaceUniformityReport sur = sim.getReportManager().createReport(SurfaceUniformityReport.class);
    sur.setScalar(f);
    sur.setUnits(u);
    sur.setPresentationName(name);
    sur.getParts().setObjects(ano);
    String yAxisLabel = "Surface Uniformity of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(sur, name, null, yAxisLabel);
    return sur;
  }

  /**
   * Creates a Volume Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param r given Region.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportVolumeAverage(Region r, String name, FieldFunction f, Units u) {
    createReportVolumeAverage(getArrayList(r), name, f, u);
  }

  /**
   * Creates a Volume Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@see #repMon} will still be updated.
   *
   * @param ar given ArrayList of Regions.
   * @param name given Report name.
   * @param f given Field Function.
   * @param u variable corresponding Unit.
   */
  public void createReportVolumeAverage(ArrayList<Region> ar, String name, FieldFunction f, Units u) {
    createReportVolumeAverage(ar, name, f, u, true);
  }

  /**
   * @param ar
   * @param name
   * @param f
   * @param u
   * @param verboseOption
   * @return Report
   */
  private Report createReportVolumeAverage(ArrayList<Region> ar, String name,
                                                    FieldFunction f, Units u, boolean verboseOption) {
    printAction("Creating a Volume Average Report of " + f.getPresentationName() + " on Regions", verboseOption);
    if (sim.getReportManager().has(name)) {
        say("Skipping... Report already exists: " + name);
        return sim.getReportManager().getReport(name);
    }
    sayRegions(ar);
    VolumeAverageReport volAvgRep = sim.getReportManager().createReport(VolumeAverageReport.class);
    volAvgRep.setScalar(f);
    volAvgRep.setUnits(u);
    volAvgRep.setPresentationName(name);
    volAvgRep.getParts().setObjects(ar);
    String yAxisLabel = "Volume Average of " + f.getPresentationName() + " (" + u.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(volAvgRep, name, null, yAxisLabel);
    return volAvgRep;
  }

  /**
   * Creates some useful Reports for debugging purposes. This method will create these Reports and Annotations:
   * <ul>
   * <li>Maximum Velocity on all Fluid Regions</li>
   * <li>Maximum Temperature on all Regions</li>
   * <li>Minimum Temperature on all Regions</li>
   * </ul>
   */
   // Implement later?
   //* @param onePerRegion if True, one Report per Region will be created. Otherwise, a single Report with all Regions.
  public void createReports_Debug() {
    printAction("Creating useful Debugging Reports");
    say("Creating Maximum Velocity Report...");
    createReportMaximum(new ArrayList<NamedObject>(getAllFluidRegions()),
            "maxVel", getFieldFunction(varVel, false), defUnitVel, false);
    if (hasEnergy()) {
        say("Creating Maximum and Minimum Temperature Reports...");
        createReportMaximum(new ArrayList<NamedObject>(getRegions(".*", false)),
                "maxT", getFieldFunction(varT, false), defUnitTemp, false);
        createReportMinimum(new ArrayList<NamedObject>(getRegions(".*", false)),
                "minT", getFieldFunction(varT, false), defUnitTemp, false);
    }
    sayOK();
  }

  /**
   * Creates some useful Unsteady Reports. This method will create these Reports and Annotations:
   * <ul>
   * <li>Maximum CFL on all Fluid Regions</li>
   * <li>Average CFL on all Fluid Regions</li>
   * <li><i>Time</i> variable as a custom Expression</li>
   * </ul>
   */
  public void createReports_Unsteady() {
    if (!isUnsteady()) return;
    printAction("Creating useful Unsteady Reports");
    say("Creating AVG and MAX CFL Reports...");
    createAnnotation_CFL(null, "AvgCFL", fmtCFL, coord0, "AVG", false);
    if (repMon == null) {
        say("   Already exists...");
    } else {
        repMon.getStarUpdate().getUpdateModeOption().setSelected(StarUpdateModeOption.TIMESTEP);
        MonitorPlot mp = monPlot;
        mp.setPresentationName("CFL Reports");
        createAnnotation_CFL(null, "MaxCFL", fmtCFL, coord0, "MAX", false);
        repMon.getStarUpdate().getUpdateModeOption().setSelected(StarUpdateModeOption.TIMESTEP);
        sim.getPlotManager().remove(monPlot);
        mp.setXAxisMonitor(getPhysicalTimeMonitor());
        mp.getMonitors().add(repMon);
        mp.getAxes().getYAxis().getTitle().setText(varCFL);
        mp.getAxes().getYAxis().setLogarithmic(true);
    }
    say("Creating Time Report...");
    createAnnotation_Time(null, "Time", defUnitTime, fmtTime, coord0, false);
    sayOK();
  }

    /**
     *
     * @param r
     * @param axis
     * @param origin
     * @param origUnit
     * @param rotValue
     * @param rotUnit
     */
    public void createRotatingReferenceFrameForRegion(Region r, double[] axis, double[] origin, Units origUnit,
                                    double rotValue, Units rotUnit) {
    RotatingMotion rm = sim.get(MotionManager.class).createMotion(RotatingMotion.class, "Rotation");
    rm.getAxisDirection().setComponents(axis[0], axis[1], axis[2]);
    rm.getAxisOrigin().setUnits(origUnit);
    rm.getAxisOrigin().setComponents(origin[0], origin[1], origin[2]);
    rm.getRotationRate().setUnits(rotUnit);
    rm.getRotationRate().setValue(rotValue);
    MotionSpecification ms = r.getValues().get(MotionSpecification.class);
    RotatingReferenceFrame rrf =
      ((RotatingReferenceFrame) sim.get(ReferenceFrameManager.class).getObject("ReferenceFrame for Rotation"));
    ms.setReferenceFrame(rrf);
  }

  /**
   * Creates an empty Scene.
   *
   * @return Created Scene.
   */
  public Scene createScene_Empty() {
    return createScene("Empty", new ArrayList<NamedObject>(), null, null, true);
  }

  /**
   * Creates a Geometry Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Geometry() {
    return createScene("Geometry", new ArrayList<NamedObject>(), null, null, true);
  }

  /**
   * Creates a Geometry Scene containing all Parts.
   *
   * @param name given Scene name.
   * @return Created Scene.
   */
  public Scene createScene_Geometry(String name) {
    Scene scn = createScene_Geometry();
    scn.setPresentationName(name);
    return scn;
  }

  private Scene createScene_Geometry(boolean verboseOption) {
    return createScene("Geometry", new ArrayList<NamedObject>(), null, null, false);
  }

  /**
   * Creates a Geometry Scene containing the input Objects. <b><u>Hint:</u></b> Use an ArrayList
   * to collect the Objects.
   *
   * @param ano given ArrayList of NamedObjects.
   * @return Created Scene.
   */
  public Scene createScene_Geometry(ArrayList<NamedObject> ano) {
    return createScene("Geometry", ano, null, null, true);
  }

  /**
   * Creates a Mesh Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Mesh() {
    return createScene("Mesh", new ArrayList<NamedObject>(), null, null, true);
  }

  /**
   * Creates a Mesh Scene containing all Parts.
   *
   * @param name given Scene name.
   * @return Created Scene.
   */
  public Scene createScene_Mesh(String name) {
    Scene scn = createScene_Mesh();
    scn.setPresentationName(name);
    return scn;
  }

  /**
   * Creates a Mesh Scene containing the input Objects. <b><u>Hint:</u></b> Use an ArrayList
   * to collect the Objects.
   *
   * @param ano given ArrayList of NamedObjects.
   * @return Created Scene.
   */
  public Scene createScene_Mesh(ArrayList<NamedObject> ano) {
    return createScene("Mesh", ano, null, null, true);
  }

  /**
   * Creates a Scalar Scene containing the input Objects. <b><u>Hint:</u></b> Use an ArrayList
   * to collect the Objects. E.g.: {@see #namedObjects}.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param f given Field Function. Use {@see #getFieldFunction} method as needed.
   * @param u given variable Unit.
   * @param smoothFilled Smooth Fill?
   * @return Created Scene.
   */
  public Scene createScene_Scalar(ArrayList<NamedObject> ano, FieldFunction f, Units u,
                                                                            boolean smoothFilled) {
    Scene scn = createScene("Scalar", ano, f, u, true);
    if (smoothFilled) {
        ScalarDisplayer sd = (ScalarDisplayer) scn.getDisplayerManager().getDisplayer("Scalar");
        sd.setFillMode(1);
    }
    return scn;
  }

  /**
   * Creates a Vector Scene containing the input Objects. <b><u>Hint:</u></b> Use an ArrayList
   * to collect the Objects.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param licOption Linear Integral Convolution option? <b>True</b> or <b>False</b>
   * @return Created Scene.
   */
  public Scene createScene_Vector(ArrayList<NamedObject> ano, boolean licOption) {
    Scene scn = createScene("Vector", ano, null, defUnitVel, true);
    if (licOption) {
        VectorDisplayer vd = (VectorDisplayer) scn.getDisplayerManager().getDisplayer("Vector");
        vd.setDisplayMode(1);
    }
    return scn;
  }

  private Scene createScene(String sceneType, ArrayList<NamedObject> ano,
                                                FieldFunction f, Units u, boolean verboseOption) {
    printAction("Creating a " + sceneType + " Scene", verboseOption);
    Scene scn = sim.getSceneManager().createScene(sceneType);
    scn.setPresentationName(sceneType);
    sayScene(scn, verboseOption);
    scn.initializeAndWait();
    //--
    ((PartDisplayer) scn.getCreatorDisplayer()).initialize();
    //--
    //-- Trick to address issues when running in batch and a Scalar Scene
    //-- Confirmed in v8.06.005. Need to apply a VisView too outside.
    //-- Happened in a custom case. But not on MacroUtils Demos? Odd.
    scn.getCurrentView().setInput(dv0, getDoubleVector(1, 0, 0), getDoubleVector(0, 0, 1), 1, 1);
    scn.resetCamera();
    //--
    if (isWindows()) {
        //-- I have found issues in some Linux machines.
        scn.setDepthPeel(true);
    } else {
        scn.setDepthPeel(false);
    }
    setSceneLogo(scn);
    setSceneBackgroundColor_Solid(scn, Color.white);
    if (!sceneType.equals("Empty")) {
        Displayer d = createSceneDisplayer(scn, sceneType, ano, f, u, verboseOption);
        setDisplayerEnhancements(d);
    }
    setSceneCameraView(scn, defCamView);
    //-- If there is a Camera with the Scene name why not using it?
    setSceneCameraView(scn, getCameraView(scn.getPresentationName(), false), false);
    sayOK(verboseOption);
    return scn;
  }

  /**
   * Adds a new Displayer into the Scene.
   *
   * @param scn given Scene.
   * @param sceneType given Type. It can be <b>"Geometry"</b> or <b>"Mesh"</b>.
   * @param ano given ArrayList of NamedObjects.
   * @return The new Displayer.
   */
  public Displayer createSceneDisplayer(Scene scn, String sceneType, ArrayList<NamedObject> ano) {
    return createSceneDisplayer(scn, sceneType, ano, null, null, true);
  }

  private Displayer createSceneDisplayer(Scene scn, String sceneType,
                    ArrayList<NamedObject> ano, FieldFunction f, Units u, boolean verboseOption) {
    Displayer d = null;
    PartDisplayer pd = null;
    ScalarDisplayer sd = null;
    VectorDisplayer vd = null;
    StreamDisplayer std = null;
    if (sceneType.equals("Empty")) {
        return null;
    }
    if (f != null && (isVector(f) || isPosition(f))) {
        f = f.getMagnitudeFunction();
    }
    printAction("Creating a " + sceneType + " Scene Displayer", verboseOption);
    if (sceneType.equals("Geometry") || sceneType.equals("Mesh")) {
        pd = scn.getDisplayerManager().createPartDisplayer(sceneType);
        pd.setColorMode(colourByPart);
        pd.setOutline(false);
        pd.setSurface(true);
        if (sceneType.equals("Mesh")) {
            pd.setMesh(true);
        }
        pd.initialize();
    } else if (sceneType.equals("Scalar")) {
        sd = scn.getDisplayerManager().createScalarDisplayer(sceneType);
        sd.initialize();
        sd.getScalarDisplayQuantity().setFieldFunction(f);
        if (u != null) {
            sd.getScalarDisplayQuantity().setUnits(u);
        }
    } else if (sceneType.equals("Streamline")) {
        std = scn.getDisplayerManager().createStreamDisplayer(sceneType);
        std.initialize();
        f = getFieldFunction(varVel, false);
        NeoObjectVector nov1 = new NeoObjectVector(getRegions(".*", false).toArray());
        NeoObjectVector nov2 = new NeoObjectVector(ano.toArray());
        StreamPart sp = sim.getPartManager().createStreamPart(nov1, nov2, f,
                postStreamlineResU, postStreamlineResV, 0);
        std.getParts().addParts(sp);
        std.getScalarDisplayQuantity().setFieldFunction(f.getMagnitudeFunction());
        std.getScalarDisplayQuantity().setUnits(defUnitVel);
        setDisplayerEnhancements(std);
    } else if (sceneType.equals("Vector")) {
        vd = scn.getDisplayerManager().createVectorDisplayer(sceneType);
        vd.initialize();
        vd.getVectorDisplayQuantity().setUnits(u);
    }
    say("Adding objects to Displayer...", verboseOption);
    if (ano.isEmpty()) {
        if (sim.getRegionManager().isEmpty()) {
            ano.addAll(getPartSurfaces(".*", false));
        } else {
            ano.addAll(getBoundaries(".*", false));
        }
    }
    for (Object obj : ano) {
        say("  " + obj.toString(), verboseOption);
    }
    if (sceneType.equals("Geometry") || sceneType.equals("Mesh")) {
        pd.addParts(ano);
        d = pd;
    } else if (sceneType.equals("Scalar")) {
        sd.addParts(ano);
        d = sd;
    } else if (sceneType.equals("Streamline")) {
        d = std;
    } else if (sceneType.equals("Vector")) {
        vd.addParts(ano);
        d = vd;
    }
    say("Objects added: " + ano.size(), verboseOption);
    if (scn.getDisplayerManager().getObjects().size() == 1) {
        d.setPresentationName(scn.getPresentationName());
    } else {
        d.setPresentationName(sceneType);
    }
    return d;
  }

  /**
   * Creates a Streamline Displayer in a Scene containing the input Objects as the Input Part.
   * <b><u>Hint:</u></b> Use an ArrayList to collect the Objects. E.g.: {@see #namedObjects}.
   * <p>
   * A Streamline Part is created too and will consider all Regions and the Velocity Vector.
   *
   * @param scn given Scene.
   * @param ano given ArrayList of NamedObjects.
   * @param tubes Would like to see tubes in the Streamlines? Control the tubes width with
   *    {@see #postStreamlinesTubesWidth} variable.
   * @return The Created Displayer.
   */
  public StreamDisplayer createSceneDisplayer_Streamline(Scene scn,
                                            ArrayList<NamedObject> ano, boolean tubes) {
    Displayer d = createSceneDisplayer(scn, "Streamline", ano, null, null, true);
    StreamDisplayer std = (StreamDisplayer) d;
    if (tubes) {
        std.setMode(2);
        std.setWidth(postStreamlinesTubesWidth);
    }
    return std;
  }

  /**
   * Creates a Section Plane using All Regions. This is similar to {@see #createDerivedPart_SectionPlane()}.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param orientation  given normal orientation coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlane(double[] origin, double[] orientation) {
    return createDerivedPart_SectionPlane(new ArrayList(getRegions(".*", false)), origin, orientation);
  }

  /**
   * Creates a Section Plane Normal to X direction using All Regions.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneX(double[] origin, String name) {
    PlaneSection pln = createSectionPlane(origin, new double[] {1., 0., 0.});
    say("Normal to X direction");
    pln.setPresentationName(name);
    sayOK();
    return pln;
  }

  /**
   * Creates a Section Plane Normal to Y direction using All Regions.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneY(double[] origin, String name) {
    PlaneSection pln = createSectionPlane(origin, new double[] {0., 1., 0.});
    say("Normal to Y direction");
    pln.setPresentationName(name);
    sayOK();
    return pln;
  }

  /**
   * Creates a Section Plane Normal to Z direction using All Regions.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneZ(double[] origin, String name) {
    PlaneSection pln = createSectionPlane(origin, new double[] {0., 0., 1.});
    say("Normal to Z direction");
    pln.setPresentationName(name);
    sayOK();
    return pln;
  }

  /**
   * Creates a Simple Block based on the relative dimensions of the given Part Surfaces. This method
   * collects Minimums and Maximums X, Y and Z and then computes Deltas
   * to be used for creating a block based on the extents of the supplied Part Surfaces.
   *
   * @param aps given Part Surfaces.
   * @param coordRelSize1 given 3-components array, relative to the collection. E.g.: {-1, 0, 0.5}
   *    will create the following coordinates: [minX - 1 * dX, minY, minZ + 0.5 * dZ].
   * @param coordRelSize2 given 3-components array, relative to the collection. E.g.: {1, 2, 3}
   *    will create the following coordinates: [maxX + 1 * dX, maxY + 2 * dY, maxZ + 3 * dX].
   * @param name given Block name.
   * @return The brand new Block Part.
   */
  public SimpleBlockPart createShapePartBlock(ArrayList<PartSurface> aps,
                    double[] coordRelSize1, double[] coordRelSize2, String name) {
    DoubleVector dvExtents = getExtents(aps);
    double minX = dvExtents.get(0);     double maxX = dvExtents.get(1);
    double minY = dvExtents.get(2);     double maxY = dvExtents.get(3);
    double minZ = dvExtents.get(4);     double maxZ = dvExtents.get(5);
    double dx = maxX - minX;
    double dy = maxY - minY;
    double dz = maxZ - minZ;
    double[] c1 = new double[] {minX+coordRelSize1[0]*dx, minY+coordRelSize1[1]*dy, minZ+coordRelSize1[2]*dz};
    double[] c2 = new double[] {maxX+coordRelSize2[0]*dx, maxY+coordRelSize2[1]*dy, maxZ+coordRelSize2[2]*dz};
    return createShapePartBlock(c1, c2, defUnitLength, name);
  }

  /**
   * Creates a Simple Block with the default Tesselation option. See {@see #defTessOpt}.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param u given units.
   * @param name given Block name.
   * @return The brand new Block Part.
   */
  public SimpleBlockPart createShapePartBlock(double[] coord1, double[] coord2, Units u, String name) {
    printAction("Creating a Simple Block Part");
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleBlockPart sbp = mpf.createNewBlockPart(sim.get(SimulationPartManager.class));
    sbp.setCoordinateSystem(lab0);
    Coordinate coordinate_0 = sbp.getCorner1();
    Coordinate r = sbp.getCorner2();
    coordinate_0.setCoordinate(u, u, u, new DoubleVector(coord1));
    r.setCoordinate(u, u, u, new DoubleVector(coord2));
    sbp.getTessellationDensityOption().setSelected(defTessOpt);
    sbp.setPresentationName(name);
    say("Created: " + sbp.getPresentationName());
    sayOK();
    return sbp;
  }

  /**
   * Creates a Simple Cone Part with the default Tesselation option. See {@see #defTessOpt}.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param r1 given radius 1.
   * @param r2 given radius 2.
   * @param u given units.
   * @param name given Cylinder name.
   * @return The brand new Cylinder Part.
   */
  public SimpleConePart createShapePartCone(double[] coord1, double[] coord2, double r1, double r2, Units u, String name) {
    printAction("Creating a Cone Simple Shape Part...");
    return (SimpleConePart) createShapePartCylinderCone(coord1, coord2, r1, r2, u, name);
  }

  /**
   * Creates a Simple Cylinder Part with the default Tesselation option. See {@see #defTessOpt}.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param r given radius.
   * @param u given units.
   * @param name given Cylinder name.
   * @return The brand new Cylinder Part.
   */
  public SimpleCylinderPart createShapePartCylinder(double[] coord1, double[] coord2, double r, Units u, String name) {
    printAction("Creating a Cylinder Simple Shape Part...");
    return (SimpleCylinderPart) createShapePartCylinderCone(coord1, coord2, r, r, u, name);
  }

  private SimpleCylinderConePart createShapePartCylinderCone(double[] coord1, double[] coord2, double r1, double r2,
                                                                        Units u, String name) {
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleCylinderConePart scp;
    if (r1 == r2) {
        scp = mpf.createNewCylinderPart(sim.get(SimulationPartManager.class));
    } else {
        scp = mpf.createNewConePart(sim.get(SimulationPartManager.class));
    }
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    scp.setCoordinateSystem(labCSYS);
    scp.getStartCoordinate().setCoordinate(u, u, u, new DoubleVector(coord1));
    scp.getEndCoordinate().setCoordinate(u, u, u, new DoubleVector(coord2));
    scp.getStartRadius().setValue(r1);
    scp.getStartRadius().setUnits(u);
    scp.getEndRadius().setValue(r2);
    scp.getEndRadius().setUnits(u);
    scp.getTessellationDensityOption().setSelected(defTessOpt);
    scp.setPresentationName(name);
    sayNamedObject(scp, true);
    sayOK();
    return scp;
  }

  /**
   * Creates a Simple Sphere Part. The origin is located in the Centroid of the given Part Surfaces.
   *
   * @param aps given Part Surfaces.
   * @param relSize the radius of sphere is given relative to the max(dx, dy, dz). E.g.: 5, is equivalent
   *                to 5 * max(dx, dy, dz).
   * @param name given Sphere name.
   * @return The brand new Sphere Part.
   */
  public SimpleSpherePart createShapePartSphere(ArrayList<PartSurface> aps, double relSize, String name) {
    DoubleVector dvExtents = getExtents(aps);
    double minX = dvExtents.get(0);     double maxX = dvExtents.get(1);
    double minY = dvExtents.get(2);     double maxY = dvExtents.get(3);
    double minZ = dvExtents.get(4);     double maxZ = dvExtents.get(5);
    double dx = maxX - minX;
    double dy = maxY - minY;
    double dz = maxZ - minZ;
    double[] coord = new double[] {minX + 0.5 * dx, minY + 0.5 * dy, minZ + 0.5 * dz};
    double radius = relSize * Math.max(Math.max(dx, dy), dz);
    return createShapePartSphere(coord, radius, defUnitLength, name);
  }

  /**
   * Creates a Simple Sphere Part with the default Tesselation option. See {@see #defTessOpt}.
   *
   * @param coord given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param r given radius.
   * @param u given unit.
   * @param name given Sphere name.
   * @return The brand new Sphere Part.
   */
  public SimpleSpherePart createShapePartSphere(double[] coord, double r, Units u, String name) {
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleSpherePart ssp = mpf.createNewSpherePart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    ssp.setCoordinateSystem(labCSYS);
    Coordinate coordinate_0 = ssp.getOrigin();
    coordinate_0.setCoordinate(u, u, u, new DoubleVector(coord));
    ssp.getRadius().setValue(r);
    ssp.getRadius().setUnits(u);
    ssp.getTessellationDensityOption().setSelected(defTessOpt);
    ssp.setPresentationName(name);
    return ssp;
  }

  /**
   * Creates a Solution History using the {@see #simTitle} name under {@see #simPath}.
   *
   * @param ano given ArrayList of NamedObjects.
   * @param aff given Array of Field Functions.
   * @return The Solution History.
   */
  public SolutionHistory createSolutionHistory(ArrayList<NamedObject> ano, ArrayList<FieldFunction> aff) {
    printAction("Creating a Solution History");
    File f = new File(simPath, simTitle + ".simh");
    SolutionHistory sh = sim.get(SolutionHistoryManager.class).createForFile(f.toString(), false);
    sh.getRegions().setObjects(ano);
    ArrayList<FieldFunction> vFF = new ArrayList();
    ArrayList<FieldFunction> sFF = new ArrayList();
    for (FieldFunction _ff : aff) {
        if (isVector(_ff)) {
            vFF.add(_ff);
        } else {
            sFF.add(_ff);
        }
    }
    sh.setVectors(new Vector(vFF));
    sh.setScalars(new Vector(sFF));
    sayOK();
    return sh;
  }

  /**
   * Creates a Solution View from a Solution History.
   *
   * @param sh given Solution History.
   * @return The Solution View.
   */
  public SolutionView createSolutionView(SolutionHistory sh) {
    printAction("Creating a Solution View History");
    SolutionViewManager svm = sim.get(SolutionViewManager.class);
    if (svm.has(sh.getPresentationName())) {
        say("Already exists. Skipping...");
        return svm.getSolutionView(sh.getPresentationName());
    }
    RecordedSolutionView rsv = sh.createRecordedSolutionView();
    say("Created: " + rsv.getPresentationName());
    sayOK();
    return rsv;
  }

  /**
   * Creates a Stopping Criteria from a Monitor.<p>
   * Types can be <b>Asymptotic</b>, <b>Minimum</b>, <b>Maximum</b>, <b>Standard Deviation</b> or
   * <b>Minimum Inner Iterations</b> (Transient only).
   *
   * @param mon given Monitor.
   * @param type use <b>Asymptotic</b>, <b>Min</b>, <b>Max</b>, <b>StdDev</b> or <b>MinInner</b>.
   * @param val given value.
   * @param samples how many samples (or iterations)? If using Min/Max, this input is ignored.
   * @return The Stopping Criteria.
   */
  public SolverStoppingCriterion createStoppingCriteria(Monitor mon, String type, double val, int samples) {
    String typeName = "";
    String stpCritName = "";
    Units monUnit = null;
    type = type.toUpperCase();
    MinimumInnerIterationStoppingCriterion minInnIt = null;
    MonitorIterationStoppingCriterionOption monCritOpt = null;
    MonitorIterationStoppingCriterion monItStpCrit = null;
    if (type.equalsIgnoreCase("MinInner")) {
        typeName = "Minimum Inner Iterations";
        minInnIt = sim.getSolverStoppingCriterionManager().createSolverStoppingCriterion(MinimumInnerIterationStoppingCriterion.class, "Minimum Inner Iterations");
        minInnIt.setMinimumNumberInnerIterations(samples);
        minInnIt.setIsUsed(true);
        minInnIt.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
        stpCritName = minInnIt.getPresentationName();
    } else {
        monItStpCrit = mon.createIterationStoppingCriterion();
        monItStpCrit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
        monCritOpt = (MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption();
        stpCritName = monItStpCrit.getPresentationName();
    }
    if (isResidual(mon)) {
        monUnit = ((ResidualMonitor) mon).getMonitoredValueUnits();
    } else if (isReport(mon)) {
        monUnit = ((ReportMonitor) mon).getMonitoredValueUnits();
    }
    if (type.equalsIgnoreCase("ASYMPTOTIC")) {
        typeName = "Asymptotic differences";
        monCritOpt.setSelected(MonitorIterationStoppingCriterionOption.ASYMPTOTIC);
        MonitorIterationStoppingCriterionAsymptoticType stpCritAsympt = (MonitorIterationStoppingCriterionAsymptoticType) monItStpCrit.getCriterionType();
        stpCritAsympt.getMaxWidth().setUnits(monUnit);
        stpCritAsympt.getMaxWidth().setValue(val);
        stpCritAsympt.setNumberSamples(samples);
    } else if (type.equals("MAX")) {
        typeName = "Maximum value";
        monCritOpt.setSelected(MonitorIterationStoppingCriterionOption.MAXIMUM);
        MonitorIterationStoppingCriterionMaxLimitType stpCritMax = (MonitorIterationStoppingCriterionMaxLimitType) monItStpCrit.getCriterionType();
        stpCritMax.getLimit().setUnits(monUnit);
        stpCritMax.getLimit().setValue(val);
    } else if (type.equalsIgnoreCase("MIN")) {
        typeName = "Minimum value";
        monCritOpt.setSelected(MonitorIterationStoppingCriterionOption.MINIMUM);
        MonitorIterationStoppingCriterionMinLimitType stpCritMin = (MonitorIterationStoppingCriterionMinLimitType) monItStpCrit.getCriterionType();
        stpCritMin.getLimit().setUnits(monUnit);
        stpCritMin.getLimit().setValue(val);
    } else if (type.equalsIgnoreCase("StdDev")) {
        typeName = "Standard Deviation";
        monCritOpt.setSelected(MonitorIterationStoppingCriterionOption.STANDARD_DEVIATION);
        MonitorIterationStoppingCriterionStandardDeviationType stpCritStdDev = (MonitorIterationStoppingCriterionStandardDeviationType) monItStpCrit.getCriterionType();
        stpCritStdDev.getStandardDeviation().setUnits(monUnit);
        stpCritStdDev.getStandardDeviation().setValue(val);
        stpCritStdDev.setNumberSamples(samples);
    }
    say("Created a Stopping Criterion based on %s.", typeName);
    say("   Name: %s", stpCritName);
    say("   %s: %g %s", typeName, val, monUnit);
    if (!type.matches("M.."))
        say("   Number of Samples: %d", samples);
    if (monCritOpt != null) {
        return (SolverStoppingCriterion) monItStpCrit;
    } else {
        return (SolverStoppingCriterion) minInnIt;
    }
  }

  /**
   * Creates a File Table to be used inside STAR-CCM+.
   *
   * @param filename given file which must be in {@see #simPath} folder.
   * @return The File Table.
   */
  public FileTable createTable_File(String filename) {
    printAction("Creating a File Table");
    FileTable ft = (FileTable) sim.getTableManager().createFromFile(new File(simPath, filename).toString());
    sayOK();
    return ft;
  }

  private DeltaMonitorUpdateEvent createUpdateEvent_DMUE(PlotableMonitor pm, double delta,
                                                Units u, String type, boolean acum, boolean verboseOption) {
    createUpdateEvent_Type(type, verboseOption);
    DeltaMonitorUpdateEvent dm = sim.getUpdateEventManager().createUpdateEvent(DeltaMonitorUpdateEvent.class);
    dm.setMonitor(pm);
    if (acum) {
        dm.getDeltaOption().setSelected(UpdateEventDeltaOption.ACCUMULATED);
    } else {
        dm.getDeltaOption().setSelected(UpdateEventDeltaOption.NONACCUMULATED);
    }
    dm.getDeltaThreshold().setValue(delta);
    dm.getDeltaThreshold().setUnits(u);
    return dm;
  }

  private FrequencyMonitorUpdateEvent createUpdateEvent_FMUE(PlotableMonitor pm, int freq,
                                                        int start, String type, boolean verboseOption) {
    createUpdateEvent_Type(type, verboseOption);
    FrequencyMonitorUpdateEvent ev = sim.getUpdateEventManager().createUpdateEvent(FrequencyMonitorUpdateEvent.class);
    ev.setMonitor(pm);
    ev.setSampleFrequency(freq);
    ev.setStartCount(start);
    sayOK(verboseOption);
    return ev;
  }

  private int createUpdateEvent_getLogic(int logicOption, boolean verboseOption) {
    if (logicOption == OR) {
        say("Logic type: OR", verboseOption);
        return UpdateEventLogicOption.OR;
    }
    if (logicOption == XOR) {
        say("Logic type: XOR", verboseOption);
        return UpdateEventLogicOption.XOR;
    }
    say("Logic type: AND", verboseOption);
    return UpdateEventLogicOption.AND;
  }

  private int createUpdateEvent_getOperator(int operOption, boolean verboseOption) {
    if (operOption == LE) {
        say("Operator type: LESS THAN OR EQUALS", verboseOption);
        return UpdateEventRangeOption.LESS_THAN_OR_EQUALS;
    }
    say("Operator type: GREATER THAN OR EQUALS", verboseOption);
    return UpdateEventRangeOption.GREATER_THAN_OR_EQUALS;
  }

  private LogicUpdateEvent createUpdateEvent_LUE(int logicOption, boolean verboseOption) {
    createUpdateEvent_Type("Logic", verboseOption);
    LogicUpdateEvent l = sim.getUpdateEventManager().createUpdateEvent(LogicUpdateEvent.class);
    l.getLogicOption().setSelected(createUpdateEvent_getLogic(logicOption, verboseOption));
    sayOK(verboseOption);
    return l;
  }

  private RangeMonitorUpdateEvent createUpdateEvent_RMUE(PlotableMonitor pm, int operOption,
                                                            double range, boolean verboseOption) {
    createUpdateEvent_Type("Range", verboseOption);
    RangeMonitorUpdateEvent ev = sim.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
    ev.setMonitor(pm);
    ev.getRangeOption().setSelected(createUpdateEvent_getOperator(operOption, verboseOption));
    ev.getRangeQuantity().setValue(range);
    sayOK(verboseOption);
    return ev;
  }

  /**
   * Creates an Update Event based on Delta Time.
   *
   * @param delta given Delta Time Frequency.
   * @param u given Delta Time unit.
   * @param acum <u>true</u> for Accumulated (Default) or <u>false</u> for Non-Accumulated.
   * @return The Update Event variable.
   */
  public DeltaMonitorUpdateEvent createUpdateEvent_DeltaTime(double delta, Units u, boolean acum) {
    return createUpdateEvent_DMUE(getPhysicalTimeMonitor(), delta, u, "Delta Time Frequency", acum, true);
  }

  /**
   * Creates an Update Event based on Iteration Frequency.
   *
   * @param freq given Iteration Frequency.
   * @param start given Start Iteration.
   * @return The Update Event variable.
   */
  public FrequencyMonitorUpdateEvent createUpdateEvent_Iteration(int freq, int start) {
    return createUpdateEvent_FMUE(getIterationMonitor(), freq, start, "Iteration Frequency", true);
  }

  /**
   * Creates a stand-alone Logic Update Event.
   *
   * @param logicOption given Logic Option ({@see #AND} (Default), {@see #OR} or {@see #XOR}).
   * @return The Update Event variable.
   */
  public LogicUpdateEvent createUpdateEvent_Logic(int logicOption) {
    return createUpdateEvent_LUE(logicOption, true);
  }

  /**
   * Creates an Logic Update Event based on the given Update Events.
   *
   * @param ues given Array of Update Events.
   * @param logicOption given Logic Option (<u>AND</u>, <u>OR</u> or <u>XOR</u>).
   * @return The Logic Update Event variable.
   */
  public LogicUpdateEvent createUpdateEvent_Logic(UpdateEvent[] ues, int logicOption) {
    createUpdateEvent_Type("Logic", true);
    LogicUpdateEvent l = createUpdateEvent_LUE(logicOption, false);
    for (UpdateEvent ue : ues) {
        say("Adding: " + ue.getPresentationName());
        createUpdateEvent_Logic(l, ue, logicOption, false);
    }
    sayOK();
    return l;
  }

  /**
   * Creates an Update Event within a Logic Update Event.
   *
   * @param lue given Logic Update Event to be populated.
   * @param ue given Update Event to be added to the Logic Update Event.
   * @param logicOption given Logic Option (<u>AND</u>, <u>OR</u> or <u>XOR</u>).
   */
  public void createUpdateEvent_Logic(LogicUpdateEvent lue, UpdateEvent ue, int logicOption) {
    createUpdateEvent_Logic(lue, ue, logicOption, true);
  }

  private void createUpdateEvent_Logic(LogicUpdateEvent lue, UpdateEvent ue, int logicOption, boolean verboseOption) {
    printAction("Adding an Update Event to a Logic Update Event", verboseOption);
    say(String.format("From: %s. To: %s.", ue.getPresentationName(), lue.getPresentationName()), verboseOption);
    UpdateEvent u = lue.getUpdateEventManager().createUpdateEvent(ue.getClass());
    lue.getLogicOption().setSelected(createUpdateEvent_getLogic(logicOption, true));
    u.copyProperties(ue);
    u.setPresentationName(ue.getPresentationName());
    sayOK(verboseOption);
  }

  /**
   * Creates an Update Event based on a Range.
   *
   * @param pm given Plotable Monitor. Use with {@see #getIterationMonitor} or
   *    {@see #getPhysicalTimeMonitor} method.
   * @param operOption {@see #GE} (Default) or {@see #LE}.
   * @param range given Range.
   * @return The Range Monitor Update Event variable.
   */
  public RangeMonitorUpdateEvent createUpdateEvent_Range(PlotableMonitor pm, int operOption, double range) {
    return createUpdateEvent_RMUE(pm, operOption, range, true);
  }

  /**
   * Creates an Update Event based on Time Step Frequency.
   *
   * @param freq given Time Step Frequency.
   * @param start given Start Time Step.
   * @return The Update Event variable.
   */
  public FrequencyMonitorUpdateEvent createUpdateEvent_TimeStep(int freq, int start) {
    return createUpdateEvent_FMUE(getPhysicalTimeMonitor(), freq, start, "Time Step Frequency", true);
  }

  private void createUpdateEvent_Type(String type, boolean verboseOption) {
    printAction("Creating an Update Event", verboseOption);
    say("Type: " + type, verboseOption);
  }

  /**
   * Creates a Simple Transform for Visualization (Vis Transform).
   * <p>
   * When providing values to Method, <b>null</b> values are ignored. Units used are
   * {@see #defUnitLength} and {@see #defUnitAngle}.
   *
   * @param org given 3-components array with origin coordinates (<b>null</b> is ignored). E.g.: {0, 0, -10}.
   * @param rot given 3-components array with the rotation axis (<b>null</b> is ignored). E.g.: {0, 1, 0}.
   * @param angle given rotation angle in {@see #defUnitAngle}.
   * @param tran given 3-components array with translation coordinates (<b>null</b> is ignored). E.g.: {12, 0, -5}.
   * @param scale given 3-components array with scaling factors (<b>null</b> is ignored). E.g.: {1, 2, 1}.
   * @param name given name.
   * @return The Simple Transform.
   */
  public SimpleTransform createVisTransform_Simple(double[] org, double[] rot, double angle,
                                                        double[] tran, double[] scale, String name) {
    printAction("Creating a Simple Transform");
    SimpleTransform st = sim.getTransformManager().createSimpleTransform();
    st.setPresentationName(name);
    if (org != null) {
        st.getRotationOriginCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(org));
    }
    if (rot != null) {
        st.getRotationAxisCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(rot));
    }
    st.getRotationAngleQuantity().setUnits(defUnitAngle);
    st.getRotationAngleQuantity().setValue(angle);
    if (tran != null) {
        st.getTranslationCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, new DoubleVector(tran));
    }
    if (scale != null) {
        st.setScale(new DoubleVector(scale));
    }
    say("Created: " + st.getPresentationName());
    sayOK();
    return st;
  }

  /**
   * Creates a Flat VOF Wave. Useful for Initial Conditions using the VOF model.
   *
   * @param phC given Physics Continua.
   * @param ptLvl given Point on Level. A 3-components array in Length dimensions. {@see #defUnitLength}.
   * @param vertDir given Vertical Direction. A 3-components array.
   * @param curr given Current. A 3-component array in Velocity dimensions. {@see #defUnitVel}.
   * @param wind given Wind. A 3-component array in Velocity dimensions. {@see #defUnitVel}.
   * @param applyIC apply the Wave Field Functions as Initial Conditions? This is recommended.
   * @return The Flat VOF Wave
   */
  public FlatVofWave createWave(PhysicsContinuum phC, double[] ptLvl, double[] vertDir,
                                                    double[] curr, double[] wind, boolean applyIC) {
    return createWave(phC, ptLvl, vertDir, curr, wind, applyIC, true);
  }

  private FlatVofWave createWave(PhysicsContinuum phC, double[] ptLvl, double[] vertDir,
                            double[] curr, double[] wind, boolean applyIC, boolean verboseOption) {
    printAction("Creating a Flat VOF Wave", verboseOption);
    VofWaveModel wm = phC.getModelManager().getModel(VofWaveModel.class);
    FlatVofWave flatWave = wm.getVofWaveManager().createVofWave(FlatVofWave.class, "FlatVofWave");
    flatWave.getPointOnLevel().setComponents(ptLvl[0], ptLvl[1], ptLvl[2]);
    flatWave.getPointOnLevel().setUnits(defUnitLength);
    flatWave.getVerticalDirection().setComponents(vertDir[0], vertDir[1], vertDir[2]);
    flatWave.getCurrent().setComponents(curr[0], curr[1], curr[2]);
    flatWave.getCurrent().setUnits(defUnitVel);
    flatWave.getWind().setComponents(wind[0], wind[1], wind[2]);
    flatWave.getWind().setUnits(defUnitVel);
    say(verboseOption, "  Point On Level: %s %s", retString(ptLvl), defUnitLength.getPresentationName());
    say(verboseOption, "  Vertical Direction: %s", retString(vertDir));
    say(verboseOption, "  Current: %s %s", retString(curr), defUnitVel.getPresentationName());
    say(verboseOption, "  Wind: %s %s", retString(wind), defUnitVel.getPresentationName());
    //--
    //-- Try to Set Light and Heavy Fluids Automatically
    ArrayList<Double> dens = new ArrayList<Double>();
    EulerianMultiPhaseModel empm = phC.getModelManager().getModel(EulerianMultiPhaseModel.class);
    ConstantMaterialPropertyMethod cmpm = null;
    for (Object obj : empm.getPhaseManager().getObjects()) {
        EulerianPhase phase = (EulerianPhase) obj;
        for (Model model : phase.getModelManager().getObjects()) {
            if (model.getBeanDisplayName().matches("Gas|Liquid")) {
                cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
                dens.add(cmpm.getQuantity().getValue());
            }
        }
    }
    flatWave.getLightFluidDensity().setValue(Math.min(dens.get(0), dens.get(1)));
    flatWave.getLightFluidDensity().setUnits(defUnitDen);
    flatWave.getHeavyFluidDensity().setValue(Math.max(dens.get(0), dens.get(1)));
    flatWave.getHeavyFluidDensity().setUnits(defUnitDen);
    //--
    //-- Apply Wave Initial Conditions
    if (applyIC) {
        InitialPressureProfile ipp = phC.getInitialConditions().get(InitialPressureProfile.class);
        ipp.setMethod(FunctionScalarProfileMethod.class);
        ipp.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(flatWave.getPressureFF());
        VolumeFractionProfile vfp = phC.getInitialConditions().get(VolumeFractionProfile.class);
        vfp.setMethod(CompositeArrayProfileMethod.class);
        ScalarProfile sp0 = vfp.getMethod(CompositeArrayProfileMethod.class).getProfile(0);
        sp0.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile sp1 = vfp.getMethod(CompositeArrayProfileMethod.class).getProfile(1);
        sp1.setMethod(FunctionScalarProfileMethod.class);
        sp0.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(flatWave.getVofHeavyFF());
        sp1.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(flatWave.getVofLightFF());
        VelocityProfile vpp = phC.getInitialConditions().get(VelocityProfile.class);
        vpp.setMethod(FunctionVectorProfileMethod.class);
        vpp.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(flatWave.getVelocityFF());
    }
    //--
    sayOK(verboseOption);
    return flatWave;
  }

    /**
     *
     * @param r
     * @param T
     * @param u
     */
    public void customizeInitialTemperatureConditionForRegion(Region r, double T, Units u) {
    printAction("Customizing a Region Initial Condition");
    sayRegion(r);
    say("Value: " + T + u.getPresentationName());
    r.getConditions().get(InitialConditionOption.class).setSelected(InitialConditionOption.REGION);
    StaticTemperatureProfile temp = r.get(RegionInitialConditionManager.class).get(StaticTemperatureProfile.class);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(u);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    sayOK();
  }

    /**
     *
     * @param r
     * @param numLayers
     */
    public void customizeThinMesherForRegion(Region r, int numLayers) {
    printAction("Customizing a Region Thin Mesher Parameter");
    sayRegion(r);
    say("Thin Mesher Layers: " + numLayers);
    r.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(SolidMesherRegionOption.CUSTOM_VALUES);
    r.get(MeshValueManager.class).get(ThinSolidLayers.class).setLayers(numLayers);
    sayOK();
  }

  public void debugResidualsAndCorrections() {
    leaveTemporaryStorage = true;
    String preffix = "Absolute of ";
    updateSolverSettings(false);
    ArrayList<FieldFunction> ffs = getFieldFunctions(".*Residual.*", false);
    ffs.addAll(getFieldFunctions(".*Correction$", false));
    ffs.addAll(getFieldFunctions(".*Enthalpy Correction.*", false));
    printAction("Creating Absolute Field Functions");
    for (FieldFunction f : ffs) {
        String newFF = preffix + f.getPresentationName();
        if (sim.getFieldFunctionManager().has(newFF) || f.getPresentationName().startsWith(preffix)) {
            say("Already exists: " + f.getPresentationName());
            continue;
        }
        FieldFunction f2 = createFieldFunction(newFF, String.format("abs(${%s})",
                f.getFunctionName()), f.getDimensions(), FF_SCALAR, false);
        say("Created: " + f2.getPresentationName());
    }
  }

  /**
   * This methods deletes files or directories. Be careful on how using it.
   *
   * @param f given file. See {@see java.io.File}.
   */
  void deleteFile(File f) {
    String type = "file";
    if (f.isDirectory()) {
        type = "folder";
        for (File c : f.listFiles()) {
            deleteFile(c);
        }
    }
    if (f.delete()) {
        say("Deleted %s: %s",  type, f);
    } else {
        say("Failed to delete %s: %s", type, f);
    }
  }

  /**
   * Disables Auto Save functionality.
   */
  public void disableAutoSave() {
    printAction("Disabling Auto Save");
    sim.getSimulationIterator().getAutoSave().getStarUpdate().setEnabled(false);
    sayOK();
  }

  /**
   * Disables the Embedded Thin Mesher.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableEmbeddedThinMesher(MeshContinuum continua) {
    printAction("Disabling Embedded Thin Mesher");
    sayContinua(continua, true);
    continua.disableModel(continua.getModelManager().getModel(SolidMesherSubModel.class));
    sayOK();
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of a Region.
   *
   * @param r given Region.
   */
  public void disableFeatureCurveSizeOnRegion(Region r) {
    disableFeatureCurveSizeOnRegion(r, true);
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of the Regions searched by REGEX.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void disableFeatureCurveSizeOnRegions(String regexPatt) {
    printAction(String.format("Disabling Feature Curves Mesh Size on all " +
                                "Regions by REGEX pattern: \"%s\"", regexPatt));
    for (Region _reg : getRegions(regexPatt, false)) {
        disableFeatureCurveSizeOnRegion(_reg, false);
        sayRegion(_reg, true);
    }
    sayOK();
  }

  private void disableFeatureCurveSizeOnRegion(Region r, boolean verboseOption) {
    printAction("Disabling Custom Feature Curve Mesh Size", verboseOption);
    sayRegion(r, verboseOption);
    ArrayList<FeatureCurve> af = new ArrayList(r.getFeatureCurveManager().getFeatureCurves());
    for (FeatureCurve fc : af) {
        try{
            fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        } catch (Exception e) {
            say("  Error disabling " + fc.getPresentationName(), verboseOption);
        }
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Continua from a Region.
   *
   * @param r given Region.
   */
  public void disableMeshContinua(Region r) {
    disableMeshContinua(r, true);
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private void disableMeshContinua(Region r, boolean verboseOption) {
    printAction("Disabling Mesh Continua", verboseOption);
    sayRegion(r, verboseOption);
    try{
        sayContinua(r.getMeshContinuum(), true);
        r.getMeshContinuum().erase(r);
    } catch (Exception e) {
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Per Region Flag in a Mesh Continua.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableMeshPerRegion(MeshContinuum continua) {
    printAction("Unsetting Mesh Continua as \"Per-Region Meshing\"");
    sayContinua(continua, true);
    continua.setMeshRegionByRegion(false);
    sayOK();
  }

  private void disablePhysicsContinua(Region r, boolean verboseOption) {
    printAction("Disabling Physics Continua", verboseOption);
    sayRegion(r, verboseOption);
    try{
        sayContinua(r.getPhysicsContinuum(), true);
        r.getPhysicsContinuum().erase(r);
    } catch (Exception e) {
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disable the Prisms Layers on a given Boundary.
   *
   * @param b given Boundary.
   */
  public void disablePrismLayers(Boundary b) {
    disablePrismLayers(b, true);
  }

  /**
   * Disable the Prisms Layers on all Boundaries that match the search criteria.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void disablePrismLayers(String regexPatt) {
    printAction(String.format("Disabling Prism Layers on all Boundaries " +
                                "by REGEX pattern: \"%s\"", regexPatt));
    printLine();
    for (Boundary b : getBoundaries(regexPatt, false, false)) {
        say("");
        disablePrismLayers(b, true);
        say("");
    }
    printLine();
    sayOK();
  }

  /**
   * Disables the Prism Layers.
   *
   * @param continua given Mesh Continua.
   */
  public void disablePrismLayers(Continuum continua) {
    disablePrismLayers(continua, true);
  }

    /**
     *
     * @param continua
     * @param verboseOption
     */
    public void disablePrismLayers(Continuum continua, boolean verboseOption) {
    if (!hasPrismLayerMesher(continua)) {
        return;
    }
    printAction("Disabling Prism Layers", verboseOption);
    sayContinua(continua, true);
    continua.disableModel(continua.getModelManager().getModel(PrismMesherModel.class));
    sayOK(verboseOption);
  }

  private void disablePrismLayers(Boundary b, boolean verboseOption) {
    say("Disabling Prism Layers on Boundary...", verboseOption);
    sayBdry(b);
    try {
        b.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
        sayOK(verboseOption);
    } catch (Exception e1) {
        say("Warning! Could not disable Prism in Boundary.", verboseOption);
    }
  }

    /**
     *
     * @param r
     */
    public void disableRadiationS2S(Region r) {
    printAction("Disabling Radiation Surface to Surface (S2S)");
    sayRegion(r);
    r.getConditions().get(RadiationTransferOption.class).setOptionEnabled(false);
    sayOK();
  }

  /**
   * Disables a Region, i.e., unsets its Physics and Mesh Continuas.
   *
   * @param r given Region.
   */
  public void disableRegion(Region r) {
    printAction("Disabling a Region");
    sayRegion(r);
    disableMeshContinua(r, false);
    disablePhysicsContinua(r, false);
    sayOK();
  }

    /**
     *
     * @param b
     */
    public void disableSurfaceSizeOnBoundary(Boundary b) {
    say("Disable Surface Mesh Size on Boundary: " + b.getPresentationName());
    try {
        b.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not disable as Boundary. Trying to do in the Interface Parent");
        try {
            DirectBoundaryInterfaceBoundary ib = (DirectBoundaryInterfaceBoundary) b;
            DirectBoundaryInterface i = ib.getDirectBoundaryInterface();
            say("Interface: " + i.getPresentationName());
            i.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
            sayOK();
        } catch (Exception e2) {
            say("ERROR! Please review settings. Skipping this Boundary.");
            say(e2.getMessage() + "\n");
        }
    }
  }

  /**
   * Disables the Surface Proximity Refinement.
   *
   * @param no given NamedObject. It can be a Mesh Continua or Mesh Operation.
   */
  public void disableSurfaceProximityRefinement(NamedObject no) {
    printAction("Disabling Surface Proximity Refinement");
    if (isMeshContinua(no)) {
        MeshContinuum mc = (MeshContinuum) no;
        sayContinua(mc, true);
        mc.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    }
    if (isAutoMeshOperation(no)) {
        AutoMeshOperation amo = (AutoMeshOperation) no;
        sayMeshOp(amo, true);
        ((ResurfacerAutoMesher) amo.getMeshers().getObject("Surface Remesher")).setDoProximityRefinement(false);
    }
    sayOK();
  }

  /**
   * Disables the Surface Remesher.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableSurfaceRemesher(MeshContinuum continua) {
    printAction("Disabling Surface Remesher");
    sayContinua(continua, true);
    continua.disableModel(continua.getModelManager().getModel(ResurfacerMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Remesher Automatic Repair.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableSurfaceRemesherAutomaticRepair(MeshContinuum continua) {
    printAction("Disabling Remesher Automatic Surface Repair");
    if (!isRemesh(continua)) {
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayContinua(continua, true);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    sayOK();
  }

  /**
   * Disables the Surface Remesher Project to CAD feature.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableSurfaceRemesherProjectToCAD(MeshContinuum continua) {
    printAction("Disabling Project to CAD");
    sayContinua(continua, true);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(false);
    continua.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(false);
    sayOK();
  }

  /**
   * Disables the Surface Wrapper.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableSurfaceWrapper(MeshContinuum continua) {
    printAction("Disabling Surface Wrapper");
    sayContinua(continua, true);
    continua.disableModel(continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Wrapper GAP closure.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void disableWrapperGapClosure(MeshContinuum continua) {
    printAction("Disabling Wrapper Gap Closure");
    sayContinua(continua, true);
    continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(false);
    sayOK();
  }

    /**
     *
     * @param r
     */
    public void disableThinMesherOnRegion(Region r) {
    printAction("Disabling Thin Mesher on Region");
    int opt = SolidMesherRegionOption.DISABLE;
    sayRegion(r);
    try{
        r.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(opt);
        sayOK();
    } catch (Exception e) {
        say("ERROR! Skipping...\n");
    }
  }

  /**
   * Enables the Coupled Solver by changing the Segregated Solver within a Physics Continua. It enables
   * both Expert Driver and Grid Sequencing Initialization.
   *
   * @param phC given Physics Continua.
   */
  public void enableCoupledSolver(PhysicsContinuum phC) {
    enableCoupledSolver(phC, true, true);
  }

  /**
   * Enables the Coupled Solver by changing the Segregated Solver within a Physics Continua.
   *
   * @param ed enable Expert Driver?
   * @param gs enable Grid Sequencing Initialization?
   * @param phC given Physics Continua.
   */
  public void enableCoupledSolver(PhysicsContinuum phC, boolean ed, boolean gs) {
    printAction("Enabling Coupled Solver");
    SegregatedFlowModel segrSlv = phC.getModelManager().getModel(SegregatedFlowModel.class);
    say("Segregated Solver disabled...");
    phC.disableModel(segrSlv);
    phC.enable(CoupledFlowModel.class);
    if (hasEnergy()) {
        SegregatedFluidTemperatureModel sftm = phC.getModelManager().getModel(SegregatedFluidTemperatureModel.class);
        phC.disableModel(sftm);
        phC.enable(CoupledEnergyModel.class);
    }
    CoupledImplicitSolver cis = ((CoupledImplicitSolver) getSolver(CoupledImplicitSolver.class));
    say("Coupled Solver enabled...");
    say("  CFL: " + CFL);
    cis.setCFL(CFL);
    cis.getAMGLinearSolver().setConvergeTol(amgConvTol);
    if (ed) {
        enableExpertDriver(false);
    }
    if (rampCFL) {
        cis.getRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
        LinearRampCalculator lrc = ((LinearRampCalculator) cis.getRampCalculatorManager().getCalculator());
        lrc.setStartIteration(cflRampBeg);
        lrc.setEndIteration(cflRampEnd);
        lrc.setInitialRampValue(cflRampBegVal);
        say("  CFL Ramp activated: " + CFL);
        say("     Start Iteration: " + cflRampBeg);
        say("     End Iteration: " + cflRampEnd);
        say("     Initial CFL : " + cflRampBegVal);
    }
    if (gs) {
        enableGridSequencing(false);
    }
    sayOK();
  }

  /**
   * Associate a Continua to a Region. It can be a Mesh or Physics Continua.
   *
   * @param continua given Continua.
   * @param r given Region.
   */
  public void enableContinua(Continuum continua, Region r) {
    printAction("Enabling Continua...");
    sayContinua(continua, true);
    sayRegion(r);
    continua.add(r);
    sayOK();
  }

   /**
    *
    * @param continua
    * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
    */
    public void enableEmbeddedThinMesher(MeshContinuum continua) {
    printAction("Enabling Embedded Thin Mesher");
    sayContinua(continua, true);
    say("Embedded Thin Mesher overview:");
    continua.enable(SolidMesherSubModel.class);
    //continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    disableSurfaceProximityRefinement(continua);
    SolidMesherSubModel sldSubMdl = continua.getModelManager().getModel(SolidMesherSubModel.class);
    sldSubMdl.setOptimize(true);
    say("  Optimizer ON");
    if (thinMeshIsPolyType) {
        sldSubMdl.getPrismType().setSelected(PrismTypeValue.POLYGONAL);
        say("  Prisms Type: POLYGONAL");
    } else {
        sldSubMdl.getPrismType().setSelected(PrismTypeValue.TRIANGULAR);
        say("  Prisms Type: TRIANGULAR");
    }
    continua.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);
    say("  Thin Layers: " + thinMeshLayers);
    sayOK();
  }

  /**
   * Enables the Expert Solution Driver for the Coupled Flow Solver.
   */
  public void enableExpertDriver() {
    enableExpertDriver(true);
  }

  private void enableExpertDriver(boolean verboseOption) {
    if (!hasCoupledImplicitFlow()) {
        return;
    }
    printAction("Enabling Expert Solution Driver", verboseOption);
    if (isUnsteady()) {
        say("This simulation is Unsteady. Will not enable Expert Driver for this...");
        return;
    }
    CoupledImplicitSolver cis = ((CoupledImplicitSolver) getSolver(CoupledImplicitSolver.class));
    cis.getSolutionDriverManager().getExpertDriverOption().setSelected(ExpertDriverOption.EXPERT_DRIVER);
    ExpertDriverCoupledSolver edcs = ((ExpertDriverCoupledSolver) cis.getSolutionDriverManager().getDriver());
    edcs.setStartIteration(cflRampBeg);
    edcs.setEndIteration(cflRampEnd);
    edcs.setInitialRampValue(cflRampBegVal);
    edcs.setRelativeResidualCutoff(1.0E-20);
    edcs.setEnhancedDissipationStartTransition(edStartTrans);
    edcs.setEnhancedDissipationEndTransition(edEndTrans);
    //-- Just to be sure.
    rampCFL = false;
    say("Expert Driver enabled.");
    sayOK(verboseOption);
  }

  /**
   * Enables the Generalized Cylinder Meshing Model.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void enableGeneralizedCylinderModel(MeshContinuum continua) {
    enableGeneralizedCylinderModel(continua, true);
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private void enableGeneralizedCylinderModel(MeshContinuum continua, boolean verboseOption) {
    printAction("Enabling Generalized Cylinder Model", verboseOption);
    sayContinua(continua, verboseOption);
    if (!hasPolyMesher(continua)) {
        say("Generalized Cylinder model only available for Poly meshes. Skipping...", verboseOption);
        return;
    }
    if (!hasGenCylModel(continua)) {
        continua.enable(GenCylModel.class);
    }
    sayOK(verboseOption);
  }

  /**
   * Enables the Grid Sequencing Initialization for the Coupled Flow Solver.
   */
  public void enableGridSequencing() {
    enableGridSequencing(true);
  }

  private void enableGridSequencing(boolean verboseOption) {
    if (!hasCoupledImplicitFlow()) {
        return;
    }
    printAction("Enabling Grid Sequencing Initialization...", verboseOption);
    CoupledImplicitSolver cis = ((CoupledImplicitSolver) getSolver(CoupledImplicitSolver.class));
    cis.getExpertInitManager().getExpertInitOption().setSelected(ExpertInitOption.GRID_SEQ_METHOD);
    if (!cis.getSolutionDriverManager().isEmpty()) {
        gsiCFL = cflRampBegVal;
        say("Expert Driver detected. Using the same initial CFL ramp: " + gsiCFL);
    }
    GridSequencingInit gridSeqInit = ((GridSequencingInit) cis.getExpertInitManager().getInit());
    say("Grid Sequencing Initialization enabled.", verboseOption);
    say("  Max Levels: " + gsiMaxLevels);
    say("  Max Iterations: " + gsiMaxIterations);
    say("  Convergence Tolerance: " + gsiConvTol);
    say("  CFL: " + gsiCFL);
    gridSeqInit.setMaxGSLevels(gsiMaxLevels);
    gridSeqInit.setMaxGSIterations(gsiMaxIterations);
    gridSeqInit.setConvGSTol(gsiConvTol);
    gridSeqInit.setGSCfl(gsiCFL);
    sayOK(verboseOption);
  }
    /**
     * This method will assume Prism Layers only on Fluid Regions
     *
     * @param continua given Mesh Continua.
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enablePrismLayers(MeshContinuum continua) {
    printAction("Enabling Prism Layers");
    sayContinua(continua, true);
    if (prismsLayers <= 0 || prismsRelSizeHeight <= 0.) {
        say("Prisms disabled.");
        disablePrismLayers(continua, false);
        return;
    }
    printPrismsParameters();
    ArrayList<Interface> ai = new ArrayList(sim.getInterfaceManager().getObjects());

    continua.enable(PrismMesherModel.class);
    PrismMesherModel pmm = continua.getModelManager().getModel(PrismMesherModel.class);
    NumPrismLayers npl = continua.getReferenceValues().get(NumPrismLayers.class);
    PrismLayerStretching pls = continua.getReferenceValues().get(PrismLayerStretching.class);
    PrismThickness pt = continua.getReferenceValues().get(PrismThickness.class);
    GenericRelativeSize pgrs = ((GenericRelativeSize) pt.getRelativeSize());
    npl.setNumLayers(prismsLayers);
    pls.setStretching(prismsStretching);
    pgrs.setPercentage(prismsRelSizeHeight);
    pmm.setMinimumThickness(prismsMinThickn);
    pmm.setLayerChoppingPercentage(prismsLyrChoppPerc);
    pmm.setNearCoreLayerAspectRatio(prismsNearCoreAspRat);

    say("Disabling Prisms on Solid Regions");
    for (Region r : getRegions(".*", false)) {
        MeshContinuum mshC = r.getMeshContinuum();
        //if (r.getMeshContinuum() == null) {
        if (!r.isMeshing() || continua != mshC) {
            say("  Skipping: " + r.getPresentationName());
            continue;
        }
        if (isFluid(r)) {
            say("  Region ON: " + r.getPresentationName());
            r.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DEFAULT);
        } else if (isSolid(r)) {
            r.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
            say("  Region OFF: " + r.getPresentationName());
        }
    }
    say("Enabling Prisms on Fluid-Solid interfaces with same Mesh Continua");
    int n = 0;
    int k = 0;
    InterfacePrismsOption ipo = null;
    for (Interface i : ai) {
        String name = i.getPresentationName();
        if (isFluid(i.getRegion0()) && isFluid(i.getRegion1())) {
            say("  Prism OFF: " + name + " (Fluid-Fluid Interface)");
            k++;
            continue;
        }
        if (isFluid(i.getRegion0()) || isFluid(i.getRegion1())) {
            try {
                ipo = i.get(MeshConditionManager.class).get(InterfacePrismsOption.class);
                ipo.setPrismsEnabled(true);
                say("  Prism ON: " + name);
                n++;
            } catch (Exception e) {
                continue;
            }
        }
    }
    say("Fluid-Solid interfaces with Prisms enabled: " + n);
    say("Fluid-Fluid interfaces skipped: " + k);
    sayOK();
  }

  /**
   * Enables Surface to Surface Radiation model.
   *
   * @param phC given Physics Continua.
   */
  public void enableRadiationS2S(PhysicsContinuum phC) {
    printAction("Enabling Radiation Surface to Surface (S2S)");
    sayContinua(phC, true);
    phC.enable(RadiationModel.class);
    phC.enable(S2sModel.class);
    phC.enable(ViewfactorsCalculatorModel.class);
    phC.enable(GrayThermalRadiationModel.class);
    GrayThermalRadiationModel gtrm = phC.getModelManager().getModel(GrayThermalRadiationModel.class);
    RadiationTemperature radT = gtrm.getThermalEnvironmentManager().get(RadiationTemperature.class);
    radT.getEnvRadTemp().setUnits(defUnitTemp);
    radT.getEnvRadTemp().setValue(refT);
  }

    /**
     *
     * @param continua
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableSurfaceProximityRefinement(MeshContinuum continua) {
    printAction("Enabling Surface Proximity Refinement");
    sayContinua(continua, true);
    say("Proximity settings overview: ");
    say("  Proximity Number of Points in Gap: " + mshProximityPointsInGap);
    say("  Proximity Search Floor: " + mshProximitySearchFloor + defUnitLength.toString());
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(true);
    SurfaceProximity sp = continua.getReferenceValues().get(SurfaceProximity.class);
    sp.setNumPointsInGap(mshProximityPointsInGap);
    sp.getFloor().setUnits(defUnitLength);
    sp.getFloor().setValue(mshProximitySearchFloor);
    sayOK();
  }

    /**
     *
     * @param continua
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableSurfaceRemesher(MeshContinuum continua) {
    printAction("Enabling Surface Remesher");
    sayContinua(continua, true);
    continua.enable(ResurfacerMeshingModel.class);
    sayOK();
  }

    /**
     *
     * @param continua
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableSurfaceRemesherAutomaticRepair(MeshContinuum continua) {
    printAction("Enabling Remesher Automatic Surface Repair");
    if (!isRemesh(continua)) {
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayContinua(continua, true);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(true);
    sayOK();
  }

    /**
     *
     * @param continua
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableSurfaceRemesherProjectToCAD(MeshContinuum continua) {
    printAction("Enabling Project to CAD");
    sayContinua(continua, true);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(true);
    continua.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(true);
    sayOK();
  }

    /**
     *
     * @param continua
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableSurfaceWrapper(MeshContinuum continua) {
    printAction("Enabling Surface Wrapper");
    sayContinua(continua, true);
    say("Surface Wrapper settings overview: ");
    say("  Geometric Feature Angle (deg): " + mshWrapperFeatureAngle);
    say("  Wrapper Scale Factor (%): " + mshWrapperScaleFactor);
    continua.enable(SurfaceWrapperMeshingModel.class);
    setMeshWrapperFeatureAngle(continua, mshWrapperFeatureAngle);
    setMeshWrapperScaleFactor(continua, mshWrapperScaleFactor);
    sayOK();
  }

  /**
   * Converts an Implicit Unsteady to a Steady State simulation .
   *
   * @param phC given Physics Continua.
   */
  public void enableSteadyState(PhysicsContinuum phC) {
    enableSteadyUnsteady(phC, 1, true);
  }

  /**
   * Convert a Steady State simulation into Implicit Unsteady.
   *
   * @param phC given Physics Continua.
   */
  public void enableUnsteady(PhysicsContinuum phC) {
    enableSteadyUnsteady(phC, 2, true);
  }

  /**
   * Plays with Steady/Unsteady analysis.
   *
   * @param phC given Physics Continua.
   * @param type 0 - Leave; 1 - Steady; 2 - Unsteady.
   * @param verboseOption
   */
  private void enableSteadyUnsteady(PhysicsContinuum phC, int type, boolean verboseOption) {
    String s = "";
    Class disable = null;
    Class enable = null;
    switch (type) {
        case 0:
            return;
        case 1:
            s = "Steady State";
            disable = ImplicitUnsteadyModel.class;
            enable = SteadyModel.class;
            break;
        case 2:
            s = "Implicit Unsteady";
            disable = SteadyModel.class;
            enable = ImplicitUnsteadyModel.class;
            break;
    }
    printAction("Enabling " + s + " analysis", verboseOption);
    if (phC == null) {
        say("Physics Continua not provided. Skipping...", verboseOption);
        return;
    }
    sayContinua(phC, true);
    if ((type == 2 && isUnsteady(phC)) || (type == 1 && isSteady(phC))) {
        say("Already " + s + " simulation...", verboseOption);
    } else {
        phC.disableModel(phC.getModelManager().getModel(disable));
        phC.enable(enable);
    }
    sayOK(verboseOption);
  }

    /**
     *
     * @param continua
     * @param gapSize
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void enableWrapperGapClosure(MeshContinuum continua, double gapSize) {
    printAction("Enabling Wrapper Gap Closure");
    sayContinua(continua, true);
    say("  Gap Closure Size (%): " + gapSize);
    continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(true);
    continua.getReferenceValues().get(GapClosureSize.class).getRelativeSize().setPercentage(gapSize);
    sayOK();
  }

  private void exportMeshAndData(Simulation simu, String file, ArrayList<Region> ar,
                        ArrayList<FieldFunction> colFF, boolean firstExp, boolean verboseOption) {
    printAction("Exporting Mesh and Data", verboseOption);
    ImportManager im = simu.getImportManager();
    //-- Trick for first export only.
    if (firstExp) {
        colFF.add(colFF.iterator().next());
    }
    for (Region r : ar) {
        say("Exporting Region: " + r.getPresentationName(), verboseOption);
    }
    for (FieldFunction f : colFF) {
        say("Exporting Field Function: " + f.getPresentationName(), verboseOption);
    }
    boolean appendToFile = false;
//    boolean dataAtVertices = true;
    boolean dataAtVertices = false;
    im.export(file, ar, emptyNeoObjVec, emptyNeoObjVec, colFF, appendToFile, dataAtVertices);
    sayOK(verboseOption);
  }

  private void exportMeshedRegions(String subDirName, SurfaceRep sr) {
    say("DBS files in Path: " + dbsPath.toString());
    File dbsSubPath = new File(dbsPath, subDirName);
    if (!dbsPath.exists()) {
        dbsPath.mkdirs();
    }
    if (!dbsSubPath.exists()) {
        dbsSubPath.mkdirs();
    }
    for (SurfaceRepRegion srr : sr.getSurfaceRepRegionManager().getObjects()) {
        File fPath = new File(dbsSubPath, getStringForFilename(srr.getPresentationName()) + ".dbs");
        say("Writing: " + fPath);
        srr.exportDbsRegion(fPath.toString(), 1, "");
    }
  }

  /**
   * Exports the meshed Regions to DBS files. Each Region will have a specific .dbs file. The mesh is
   * exported by the order of availability.
   * <ol>
   * <li> Remeshed Representation
   * <li> Wrapped Representation
   * </ol>
   * @param subDirName given subfolder name to store the DBS files. It is stored by default in a folder
   * defined by {@see #dbsPath} variable.
   */
  public void exportMeshedRegionsToDBS(String subDirName) {
    printAction("Exporting Meshed Regions to DBS files");
    say("Querying Surface Mesh Representation");
    if (hasValidSurfaceMesh()) {
        say("Remeshed Regions found. Using this one.");
        exportRemeshedRegionsToDBS(subDirName);
    } else if (hasValidWrappedMesh()) {
        say("Wrapped Regions found. Using this one.");
        exportWrappedRegionsToDBS(subDirName);
    } else {
        say("No Valid Mesh representation found. Skipping.");
    }
    sayOK();
  }

  /**
   * Exports the Plot as CSV with option to sort data.
   *
   * @param plt given Plot.
   * @param csvName given CSV name.
   */
  private void exportPlot(StarPlot plt, String csvName, boolean sortData) {
    String name = csvName;
    if (!name.toLowerCase().contains(".csv")) {
        name += ".csv";
    }
    if (sortData && isXYPlot(plt)) {
        XYPlot p = (XYPlot) plt;
        YAxisType y = (YAxisType) p.getYAxes().getDefaultAxis();
        InternalDataSet id = (InternalDataSet) y.getDataSets().getDataSets().iterator().next();
        id.setNeedsSorting(true);
    }
    plt.export(new File(simPath, name), ",");
    say("CSV Exported: " + name);
  }

  /**
   * Exports the Remeshed Regions to DBS files. Each Region will have a specific .dbs file.
   *
   * @param subDirName given subfolder name to store the DBS files. It is stored by default in a folder
   * defined by {@see #dbsPath} variable.
   */
  public void exportRemeshedRegionsToDBS(String subDirName) {
    printAction("Exporting All Remeshed Regions to DBS files");
    exportMeshedRegions(subDirName, queryRemeshedSurface());
  }

  /**
   * Exports the Wrapped Regions to DBS files. Each Region will have a specific .dbs file.
   *
   * @param subDirName given subfolder name to store the DBS files. It is stored by default in a folder
   * defined by {@see #dbsPath} variable.
   */
  public void exportWrappedRegionsToDBS(String subDirName) {
    printAction("Exporting All Wrapped Regions to DBS files");
    exportMeshedRegions(subDirName, queryWrappedSurface());
  }

  /**
   * The Grid Convergence Index (GCI) method below implements a variation of Celik et al. paper, i.e., F1, F2 and
   * F3 are the coarse, medium and fine solutions respectively.
   * <p>
   * For more information, see {@see #evalGCI2}.
   *
   * @param h given array of doubles containing grid sizes in the <b>following order:
   *    <u>coarse (F1), medium (F2) and fine (F3) grid values</u></b>.
   * @param f given array of doubles containing solution values in the same order above.
   * @param grids given array of strings containing the grid names in the same order as others.
   * @return An array with doubles in the form {GCI12, GCI23, Order (p), F23_Extrapolated, E12_a, E23_a}. More
   *    information as follows:
   *    <ul>
   *    <li><b>GCI<i>ij</i></b> are the Grid Convergence Indexes;
   *    <li><b>Order</b> is the Apparent Order;
   *    <li><b>F23_Extrapolated</b> is the Exact solution according to Richardson Extrapolation;
   *    <li><b>E<i>ij</i>_a</b> are the Approximate Errors (Relative Errors) between meshes <i>i</i> and <i>j</i>.
   */
  public double[] evalGCI(double[] h, double[] f, String[] grids) {
    return evalGCI(h, f, grids, true);
  }

  private double[] evalGCI(double[] h, double[] f, String[] grids, boolean verboseOption) {
    double f1 = f[0], f2 = f[1], f3 = f[2];
    double e12 = f1 - f2, e23 = f2 - f3;
    double r12 = h[0] / h[1], r23 = h[1] / h[2];
    double beta = evalGCI_getBeta(r23, r12, e12, e23, 1);
    if (e23 / e12 < 0 || beta < 0) {
        sayWarning("Oscillatory convergence detected in GCI calculation...",
                "To avoid a NaN, beta will be used as absolute in the Apparent Order calculation (p)...");
        gciOscilConv++;
    }
    double p = evalGCI_getP(r23, r12, e12, e23, false);
    //say("p = " + p);
    double f23_extr = evalGCI_getExtrapolation(f3, f2, r23, p);
    double e12_a = getRelativeError(f2, f1, true);
    double e23_a = getRelativeError(f3, f2, true);
    double e23_extr = getRelativeError(f23_extr, f3, true);
    double gci12 = evalGCI_getGCI(1.25, e12_a, r12, p);
    double gci23 = evalGCI_getGCI(1.25, e23_a, r23, p);
    if (Math.abs(gci23) > 10.0) {
        say("Overflow detected. Being verbose...");
        String fmt = "%12s, %12s, %12s, %12s, %12s, %12s";
        say(String.format(fmt, "r12", "r23", "e23", "e12", "beta", "p"));
        say(String.format(fmt.replaceAll("s", "g"), r12, r23, e23, e12, beta, p));
        verboseOption = true;
    }
    printAction("GCI Overview (Coarse to Fine -- Paper variation)", verboseOption);
    String fmt = "%s: %12g --> %s (%s - Grid Size = %g__)".replace("__", defUnitLength.getPresentationName());
    say(String.format(fmt, "F1", f1, grids[0], "Coarse", h[0]), verboseOption);
    say(String.format(fmt, "F2", f2, grids[0], "Medium", h[1]), verboseOption);
    say(String.format(fmt, "F3", f3, grids[0], "Fine", h[2]), verboseOption);
    printLine(verboseOption);
    fmt = "%-30s = %12g";
    String fmtP = fmt.replace("g", ".2f%%");
    say(String.format(fmtP, "GCI23", gci23), verboseOption);
    say(String.format(fmtP, "GCI12", gci12), verboseOption);
    say(String.format(fmt, "Apparent Order", p), verboseOption);
    say(String.format(fmt, "Extrapolated (E23_extr)", f23_extr), verboseOption);
    say(String.format(fmtP, "Approximate Error (E23_a)", e23_a), verboseOption);
    say(String.format(fmtP, "Extrapolated Error (E23_extr)", e23_extr), verboseOption);
    printLine(verboseOption);
    return new double[] {gci12, gci23, p, f23_extr, e12_a, e23_a};
  }

  /**
   * Evaluates the Grid Convergence Index (GCI) method below for a series of grids. For more information, see
   * {@see #evalGCI(double[], double[], java.lang.String[])}.
   *
   * @param gridSizes given ArrayList of Doubles containing grid sizes in the <b>following order:
   *    <u>coarse (F1), medium (F2) and fine (F3) grid values</u></b>.
   * @param vals given ArrayList of Doubles containing solution values in the same order above.
   * @param grids given ArrayList of Strings containing the grid names in the same order as others.
   */
  public void evalGCI(ArrayList<Double> gridSizes, ArrayList<Double> vals, ArrayList<String> grids) {
    String fmt  = "%-35s %12g %12g %12.3f %12.2f %12g";
    String fmtS = "%-35s %12s %12s %12s %12s %12s";
    ArrayList<String> toSay = new ArrayList();
    String size = String.format("Size (%s)", defUnitLength.getPresentationName());
    toSay.add(String.format(fmtS, "Grid Name", "Value", size, "GCI (%)", "ORDER", "EXACT"));
    sayLoud("Assessing Grid Convergence Index", true);
    double gci = 0., p = 0., fe = 0.;
    for (double val : vals) {
        int i = vals.indexOf(val);
        if (i >= 2) {
            double[] _sizes = {gridSizes.get(i), gridSizes.get(i-1), gridSizes.get(i-2)};
            double[] _vals = {vals.get(i), vals.get(i-1), vals.get(i-2)};
            String[] _grids = {grids.get(i),grids.get(i-1), grids.get(i-2)};
            double[] gciRes = evalGCI(_sizes, _vals, _grids, false);
            gci = gciRes[1];
            p = gciRes[2];
            fe = gciRes[3];
        }
        toSay.add(String.format(fmt, grids.get(i), vals.get(i), gridSizes.get(i), gci, p, fe));
    }
    for (String s : toSay) {
        say(s);
    }
    sayOK();
  }

  /**
   * Calculates the Grid Convergence Index for 3 sim files, using the Roache's approach.
   * <p>
   * <b>Procedure:</b> <ul>
   * <li> The finest grid must be loaded and active in STAR-CCM+;
   * <li> Pick a Plot to evaluate;
   * <li> The 2 other sim files will be loaded in background and the CSV files will be exported from the Plots;
   * <li> Coarser solutions will be projected on the CSV of the finest grid;
   * <li> GCI is then calculated and the original Plot will be changed.
   * </ul>
   *
   * <b>Notes:</b> <ul>
   * <li> This method will checkout a second license of STAR-CCM+;
   * <li> The reliability of this method depends on the accuracy of how the solution is projected;
   * <li> Method is limited to sorted data in the Plot;
   * <li> If there is more than one DataSet in the plot, only the first X-Y pair will be used;
   * <li> This method is based on {@see #evalGCI(double[], double[], java.lang.String[])}.
   * </ul>
   *
   * @param plt given Plot.
   * @param simFiles array of sim files. Currently limited to 2 and must be ordered the same
   *    way as required by {@see #evalGCI(double[], double[], java.lang.String[])}:
   *    e.g.: {"coarseGrid.sim" , "mediumGrid.sim"}. In addition, must be in the same path as
   *    {@see #simPath} variable. If more arguments are provided, only the first 2 will be used
   *    as the macro will take the current simulation as the 3rd one, i.e., the fine grid.
   */
  public void evalGCI(StarPlot plt, String[] simFiles) {
    evalGCI(plt, retFiles(simFiles), true);
  }

  /**
   * Calculates the Grid Convergence Index for 3 sim files, using the Roache's approach.
   * <p>
   * <b>Procedure:</b> <ul>
   * <li> The finest grid must be loaded and active in STAR-CCM+;
   * <li> Pick a Plot to evaluate;
   * <li> The 2 other sim files will be loaded in background and the CSV files will be exported from the Plots;
   * <li> Coarser solutions will be projected on the CSV of the finest grid;
   * <li> GCI is then calculated and the original Plot will be changed.
   * </ul>
   *
   * <b>Notes:</b> <ul>
   * <li> This method will checkout a second license of STAR-CCM+;
   * <li> The reliability of this method depends on the accuracy of how the solution is projected;
   * <li> Method is limited to sorted data in the Plot;
   * <li> If there is more than one DataSet in the plot, only the first X-Y pair will be used;
   * <li> This method is based on {@see #evalGCI(double[], double[], java.lang.String[])}.
   * </ul>
   *
   * @param plt given Plot.
   * @param simFiles ArrayList of @{see java.io.File} containing the sim files. Currently limited to 2
   *    and the coarse grid must have index 0. If more items are inside the ArrayList, only the
   *    first 2 will be used as the macro will take the current simulation as the 3rd one, i.e.,
   *    the fine grid.
   */
  public void evalGCI(StarPlot plt, ArrayList<File> simFiles) {
    evalGCI(plt, simFiles, true);
  }

  private void evalGCI(StarPlot plt, ArrayList<File> simFiles, boolean verboseOption) {
    printAction("Performing Roache's GCI calculation on a Plot", verboseOption);
    if (!isXYPlot(plt)) {
        say("Currently limited to X-Y Plots only.");
        return;
    }
    //-- F1 to F3 == Coarse to Fine
    double[] x1 = null, y1 = null, x2 = null, y2 = null, x3 = null, y3 = null, y1p = null, y2p = null;
    ArrayList<String> als = new ArrayList();
    ArrayList<Double> hs = new ArrayList();
    ArrayList plots = new ArrayList();
    als.add(sim.getPresentationName());
    hs.add(evalGCI_getGridSize(sim, false));
    String pltName = plt.getPresentationName();
    plt = evalGCI_preExport(sim, plt);
    //-- Export CSVs
    exportPlot(plt, sim.getPresentationName() + ".csv", true);
    for (File sf : new File[] {simFiles.get(1), simFiles.get(0)}) {
        say("Working on File: " + sf.getAbsoluteFile(), verboseOption);
        //--
        Simulation sim2 = new Simulation(sf.toString());
        hs.add(0, evalGCI_getGridSize(sim2, false));
        StarPlot plt2 = sim2.getPlotManager().getPlot(pltName);
        als.add(0, sim2.getPresentationName());
        plt2 = evalGCI_preExport(sim2, plt2);
        exportPlot(plt2, sim2.getPresentationName() + ".csv", true);
        sim2.kill();
        //--
    }
    //-- Read CSVs
    for (String name : als) {
        String csv = name + ".csv";
        int n = als.indexOf(name);
        String[] data = readFile(new File(simPath, csv));
        say("Reading F%d: %s", (n+1), csv);
        switch (n) {
            case 0:
                x1 = retValsOnColumn(data, gciCsvReadColX);
                y1 = retValsOnColumn(data, gciCsvReadColY);
                break;
            case 1:
                x2 = retValsOnColumn(data, gciCsvReadColX);
                y2 = retValsOnColumn(data, gciCsvReadColY);
                break;
            case 2:
                x3 = retValsOnColumn(data, gciCsvReadColX);
                y3 = retValsOnColumn(data, gciCsvReadColY);
                break;
        }
    }
    //-- Project coarser data into finer Grid
    say("Projecting data...");
    y1p = retProjected(x3, x1, y1);
    y2p = retProjected(x3, x2, y2);
    //-- GCI
    say("Calculating GCI...");
    String[] grids = als.toArray(new String[als.size()]);
    double[] hss = {hs.get(0), hs.get(1), hs.get(2)};       //-- Ugly ArrayList to double[].
    double[] gci12 = new double[y3.length], gci23 = new double[y3.length],
            e12_a = new double[y3.length], e23_a = new double[y3.length],
            gciP = new double[y3.length], gciExtr = new double[y3.length];
    double p_sum = 0.;
    gciOscilConv = 0;
    double gciLimit = 10.0;  //-- 1000% error. Should be skipped.
    for (int i = 0; i < y3.length; i++) {
        say("Station %4d / %d. X = %g.", (i+1), y3.length, x3[i]);
        double[] gci = evalGCI(hss, new double[] {y1p[i], y2p[i], y3[i]}, grids, false);
        gciP[i] = gci[2];
        p_sum += gciP[i];
        e12_a[i] = gci[4];
        e23_a[i] = gci[5];
    }
    double p_ave = p_sum / y3.length;
    say("Reweightning GCI...");
    for (int i = 0; i < y3.length; i++) {
        double r12 = hss[0] / hss[1], r23 = hss[1] / hss[2];
        gci12[i] = evalGCI_getGCI(1.25, e12_a[i], r12, p_ave);
        gci23[i] = evalGCI_getGCI(1.25, e23_a[i], r23, p_ave);
        gciExtr[i] = evalGCI_getExtrapolation(y3[i], y2p[i], r23, p_ave);
        if (Math.abs(gci23[i]) > gciLimit) {
            say("Overflow detected. Exact will be assumed as F3");
            gciExtr[i] = y3[i];
        }
    }
    if (gciOscilConv > 0) {
        printLine();
        say("Oscillatory convergence found in %d Stations (%.0f%%).", gciOscilConv, (100. * gciOscilConv / y3.length));
        printLine();
    }
    //-- Write an absolute GCI CSV.
    ArrayList<String> data = new ArrayList();
    ArrayList<String> data2 = new ArrayList();
    data2.add(String.format("X, GCI23"));
    for (int i = 0; i < x3.length; i++) {
        if (Math.abs(gci23[i]) > gciLimit) {
            continue;
        }
        data2.add(String.format("%g,%g", x3[i], (1. + Math.abs(gci23[i])) * y3[i]));
        data2.add(String.format("%g,%g", x3[i], (1. - Math.abs(gci23[i])) * y3[i]));
    }
    File newCsv = new File(simPath, "AbsoluteGCI.csv");
    writeData(newCsv, data2, false);
    say("New CSV file written: " + newCsv.getName());
    FileTable it2 = (FileTable) sim.getTableManager().createFromFile(newCsv.toString());
    //-- Changing the original Plot and adding stuff.
    say("Changing Plot: " + plt.getPresentationName());
    XYPlot xy = (XYPlot) plt;
    XYPlot xy2 = sim.getPlotManager().createXYPlot();
    //--
    String s = "GCI23";
    xy2.copyProperties(xy);
    xy2.setPresentationName(plt.getPresentationName() + " - " + s);
    xy2.getXAxisType().setUnits(xy.getXAxisType().getUnits());
    YAxisType y = xy2.getYAxes().getDefaultAxis();
    y.setUnits(xy.getYAxes().getDefaultAxis().getUnits());
    y.setFieldFunction(xy.getYAxes().getDefaultAxis().getFieldFunction());
    //--
    InternalDataSet ids = (InternalDataSet) y.getDataSets().getDataSets().iterator().next();
    ids.setNeedsSorting(true);
    ids.setPresentationName(sim.getPresentationName());
    ids.setSeriesName(sim.getPresentationName());
    ids.getSymbolStyle().setColor(colorDarkOrange);
    //--
    ExternalDataSet eds = sim.getDataSetManager().createExternalDataSet(xy);
    xy2.getDataSetGroup().addObjects(eds);
    xy2.getExternal().add(eds);
    eds.setTable(it2);
    eds.setXValuesName("X");
    eds.setYValuesName(s);
    eds.setPresentationName(s);
    eds.setSeriesName(s);
    eds.getSymbolStyle().setColor(colorSlateGrayDark);
    eds.getSymbolStyle().setSize(2 * ids.getSymbolStyle().getSize());
    eds.getSymbolStyle().setSpacing(ids.getSymbolStyle().getSpacing());
    eds.getSymbolStyle().setStyle(5);
    //--
    plots.add(xy2.getPresentationName());
    say("Creating new Plots...");
    //-- Write a GCI CSV with original and projected data.
    data.clear();
    data.add("X, GCI12, GCI23, Order, E12_a, E23_a, X1, F1, X2, F2, X3, F3, F1P, F2P, F3EXACT");
    String sx1, sf1, sx2, sf2;
    for (int i = 0; i < x3.length; i++) {
        try {
            sx1 = String.format("%g", x1[i]);
            sf1 = String.format("%g", y1[i]);
        } catch (Exception e) {
            sx1 = "null";
            sf1 = "null";
        }
        try {
            sx2 = String.format("%g", x2[i]);
            sf2 = String.format("%g", y2[i]);
        } catch (Exception e) {
            sx2 = "null";
            sf2 = "null";
        }
        data.add(String.format("%g,%g,%g,%g,%g,%g,%s,%s,%s,%s,%g,%g,%g,%g,%g",
                x3[i], gci12[i], gci23[i], gciP[i], e12_a[i], e23_a[i], sx1, sf1, sx2, sf2,
                x3[i], y3[i], y1p[i], y2p[i], gciExtr[i]));
    }
    File gciCsv = new File(simPath, "GCI.csv");
    writeData(gciCsv, data, false);
    say("Written GCI CSV file: " + gciCsv.getName());
    FileTable it = (FileTable) sim.getTableManager().createFromFile(gciCsv.toString());
    //-- F1, F2 & F3 plots
    xy = evalGCI_createPlot("Original Grids Solutions", it, "X1,X2,X3".split(","), "F1,F2,F3".split(","), xy2);
    plots.add(xy.getPresentationName());
    xy = evalGCI_createPlot("Projected Grids Solutions", it, "X3,X3,X3,X3".split(","), "F1P,F2P,F3,F3EXACT".split(","), xy2);
    plots.add(xy.getPresentationName());
    prettifyPlots();
    for (Object o : plots) {
        getPlot((String) o, true).open();
    }
    sayOK(verboseOption);
  }

  private XYPlot evalGCI_createPlot(String name, FileTable it, String[] xx, String[] yy,
                                                        XYPlot xyOrig) {
    XYPlot xy = sim.getPlotManager().createXYPlot();
    xy.setPresentationName(name);
    YAxisType y = xyOrig.getYAxes().getDefaultAxis();
    y.setUnits(xy.getYAxes().getDefaultAxis().getUnits());
    InternalDataSet ids = (InternalDataSet) y.getDataSets().getDataSets().iterator().next();
    xy.getAxes().copyProperties(xyOrig.getAxes());
    for (int i = 0; i < yy.length; i++) {
        String s = yy[i].trim();
        ExternalDataSet eds = sim.getDataSetManager().createExternalDataSet(xy);
        xy.getDataSetGroup().addObjects(eds);
        xy.getExternal().add(eds);
        eds.setTable(it);
        eds.setXValuesName(xx[i].trim());
        eds.setYValuesName(s);
        eds.setPresentationName(s);
        eds.setSeriesName(s);
        eds.getSymbolStyle().setStyle(ids.getSymbolStyle().getStyle());
        eds.getSymbolStyle().setSize(ids.getSymbolStyle().getSize());
        eds.getSymbolStyle().setSpacing(ids.getSymbolStyle().getSpacing());
        switch (i) {
            case 0:
                eds.getSymbolStyle().setColor(colorNavy);
                break;
            case 1:
                eds.getSymbolStyle().setColor(colorDarkGreen);
                break;
            case 2:
                eds.getSymbolStyle().setColor(colorDarkOrange);
                break;
            case 3:
                eds.getSymbolStyle().setColor(Color.red);
                break;
        }
    }
    return xy;
  }

  private double evalGCI_getBeta(double r21, double r32, double e32, double e21, double p) {
    return e32 / e21 * (Math.pow(r21, p) - 1.0) / (Math.pow(r32, p) - 1.0);
  }

  private double evalGCI_getExtrapolation(double f2, double f1, double r, double p) {
    double rp = Math.pow(r, p);
    return (rp * f2 - f1) / (rp - 1.0);
  }

  private double evalGCI_getGCI(double fs, double e_a, double r, double p) {
    return fs * e_a / (Math.pow(r, p) - 1.0);
  }

  /**
   * Gets the current grid size for the GCI calculation. Only useful with {@see #evalGCI}.
   *
   * @return The grid size in the {@see #defUnitLength} unit.
   */
  public double evalGCI_getGridSize() {
    return evalGCI_getGridSize(sim, true);
  }

  private double evalGCI_getGridSize(Simulation s, boolean verboseOption) {
    printAction("Getting current grid size", verboseOption);
    Simulation s0 = sim;
    sim = s;
    double cells = queryVolumeMesh().getCellCount();
    Report r = createReportSum(new ArrayList(getRegions(".*")), "_sumVolumeCells",
            getFieldFunction("Volume", false), unit_m3, false);
    double h = Math.cbrt(r.getReportMonitorValue() / cells) * unit_m.getConversion() / defUnitLength.getConversion();
    say(verboseOption, "Grid size = %g%s",  h, defUnitLength.getPresentationName(), verboseOption);
    sim = s0;
    sayOK(verboseOption);
    return h;
  }

  private double evalGCI_getP(double r21, double r32, double e32, double e21, boolean verboseOption) {
    double om = 0.5;
    double p = evalGCI_getBeta(r21, r32, e32, e21, 1);
    boolean hadNaN = false;
    for (int i = 1; i <= 50; i++) {
        double beta = evalGCI_getBeta(r21, r32, e32, e21, p);
        if (e32 / e21 < 0 || beta < 0) {
            beta = Math.abs(beta);
        }
        double p1 = om * p + (1.0 - om) * Math.log(beta) / Math.log(r21);
        if (Double.isNaN(beta) || Double.isNaN(p1)) {
            hadNaN = true;
            say("WARNING!!! NaN caught in GCI Apparent Order calculation (p)...");
            String fmt = "%12s, %12s, %12s, %12s, %12s, %12s, %12s";
            say(String.format(fmt, "r21", "r32", "e32", "e21", "beta", "p", "p1"));
            say(String.format(fmt.replaceAll("s", "g"), r21, r32, e32, e21, beta, p, p1));
            verboseOption = true;
        }
        say(verboseOption, "Iter %02d:", i);
        say(verboseOption, "  p  = %g", p);
        say(verboseOption, "  p1 = %g", p1);
        double tol = Math.abs((p1 - p) / p);
        p = p1;
        if (tol <= 1e-5 || hadNaN) {
            break;
        }
    }
    return p;
  }

  /**
   * This method is in conjunction with {@see #evalGCI(star.common.StarPlot, java.util.ArrayList)} and it only works
   * along with an @Override call. Useful for adding some code before exporting the CSV file.
   *
   * It is invoked prior to exporting the CSV file from the Plot.
   *
   * @param s given Simulation.
   * @return A StarPlot. In case one needs to create a brand new on the fly.
   */
  public StarPlot evalGCI_preExport(Simulation s, StarPlot plt) {
    //-- Use it with @override
    return plt;
  }

  /**
   * The GCI method below implements exactly as it is given in the Paper, i.e., F1, F2 and F3 are
   * the fine, medium and coarse solutions respectively.
   * <p>
   * <b>References</b>:
   * <p>
   * Celik, et al., 2008. <u>Procedure for Estimation and Reporting of Uncertainty
   *    Due to Discretization in CFD Applications</u>. Journal of Fluids Engineering. Vol. 130.
   * <p>
   * Roache, P. J., 1997. <u>Quantification of Uncertainty in Computational Fluid Dynamics.</u>.
   *    Annu. Rev. Fluid. Mech. 29:123-60.
   *
   * @param h given array of doubles containing grid sizes in the <b>following order:
   *    <u>fine (F1), medium (F2) and coarse (F3) grid values</u></b>.
   * @param f given array of doubles containing solution values in the same order above.
   * @param grids given array of strings containing the grid names in the same order as others.
   * @return An array with doubles in the form {GCI21, GCI32, Order (p), F21_Extrapolated}.
   */
  public double[] evalGCI2(double[] h, double[] f, String[] grids) {
    return evalGCI2(h, f, grids, true);
  }

  private double[] evalGCI2(double[] h, double[] f, String[] grids, boolean verboseOption) {
    double f1 = f[0], f2 = f[1], f3 = f[2];
    double e21 = f2 - f1, e32 = f3 - f2;
    double r21 = h[1] / h[0], r32 = h[2] / h[1];
    double p = evalGCI_getP(r21, r32, e32, e21, false);
    double r21p = Math.pow(r21, p);
    double r32p = Math.pow(r32, p);
    double f21_extr = (r21p * f1 - f2) / (r21p - 1.0);
    double e21_a = getRelativeError(f1, f2, true);
    double e32_a = getRelativeError(f2, f3, true);
    double e21_extr = getRelativeError(f21_extr, f1, true);
    double gci21 = 1.25 * e21_a / (r21p - 1.0);
    double gci32 = 1.25 * e32_a / (r32p - 1.0);
    if (Double.isNaN(gci21) || Double.isNaN(gci32)) {
        say("WARNING!!! NaN caught in GCI calculation...");
        String fmt = "%12s, %12s, %12s, %12s, %12s, %12s, %12s";
        say(String.format(fmt, "F1", "F2", "F3", "GCI21", "GCI32", "p", "F21Extrapolated"));
        say(String.format(fmt.replaceAll("s", "g"), f1, f2, f3, gci21, gci32, p, f21_extr));
    }
    printAction("GCI Overview (Fine to Coarse -- Paper original)", verboseOption);
    say(String.format("F1: %12g --> %s (Fine)", f1, grids[0]), verboseOption);
    say(String.format("F2: %12g --> %s (Medium)", f2, grids[1]), verboseOption);
    say(String.format("F3: %12g --> %s (Coarse)", f3, grids[2]), verboseOption);
    say(String.format("Grid Sizes: %s", retString(h)), verboseOption);
    printLine(verboseOption);
    String fmt = "%-30s = %12g";
    String fmtP = fmt.replace("g", ".2f%%");
    say(String.format(fmtP, "GCI21", gci21), verboseOption);
    say(String.format(fmtP, "GCI32", gci32), verboseOption);
    say(String.format(fmt, "Apparent Order", p), verboseOption);
    say(String.format(fmt, "Extrapolated (f21_extr)", f21_extr), verboseOption);
    say(String.format(fmtP, "Approximate Error (E21_a)", e21_a), verboseOption);
    say(String.format(fmtP, "Extrapolated Error (E21_extr)", e21_extr), verboseOption);
    printLine(verboseOption);
    return new double[] {gci21, gci32, p, f21_extr};
  }

  /**
   * Evaluates a simple Linear Regression equation in the form: <b>y = a * x + b</b>.
   * @param xx given independent variable interval. E.g.: { x0, x1 }.
   * @param yy given dependent variable interval. E.g.: { y0, y1 }.
   * @param x given independent variable value for y(x) to be evaluated.
   * @return Value at y(x). Results are clipped: y0 &lt= y(x) &lt= y1.
   */
  public double evalLinearRegression(double[] xx, double[] yy, double x) {
    return evalLinearRegression(xx, yy, x, true, true);
  }

  /**
   * Evaluates a simple Linear Regression equation in the form: <b>y = a * x + b</b>.
   * @param xx given independent variable interval. E.g.: { x0, x1 }.
   * @param yy given dependent variable interval. E.g.: { y0, y1 }.
   * @param x given independent variable value for y(x) to be evaluated.
   * @param verboseOption Print to standard output?
   * @return Value at y(x). Results are clipped: y0 &lt= y(x) &lt= y1.
   */
  public double evalLinearRegression(double[] xx, double[] yy, double x, boolean verboseOption) {
    return evalLinearRegression(xx, yy, x, true, verboseOption);
  }

  private double evalLinearRegression(double[] xx, double[] yy, double x,
                                                        boolean clipOption, boolean verboseOption) {
    printAction("Evaluating a Simple Regression: y = a * x + b", verboseOption);
    double a, b, y;
    a = (yy[1] - yy[0]) / (xx[1] - xx[0]);
    b = yy[1] - a * xx[1];
    y = a * x + b;
    say(String.format("xx = {%g, %g}; yy = {%g, %g}", xx[0], xx[1], yy[0], yy[1]), verboseOption);
    say("a = " + a, verboseOption);
    say("b = " + b, verboseOption);
    say(String.format("y(%g) = %g", x, y), verboseOption);
    if (clipOption) {
        if (x <= xx[0]) {
            say(String.format("  x <= x0. Clipped to y(%g) = %g", xx[0], yy[0]), verboseOption);
            y = yy[0];
        }
        if (x >= xx[1]) {
            say(String.format("  x >= x1. Clipped to y(%g) = %g", xx[1], yy[1]), verboseOption);
            y = yy[1];
        }
    }
    sayOK(verboseOption);
    return y;
  }

  /**
   * Finds Part Contacts.
   *
   * @param tol given absolute tolerance in meters.
   */
  public void findAllPartsContacts(double tol) {
    printAction("Finding All Part Contacts");
    say("Tolerance (m): " + tol);
    queryGeometryRepresentation().findPartPartContacts(getAllLeafPartsAsMeshParts(), tol);
    sayOK();
  }

  /**
   * This is the {@see #genSurfaceMesh} method.
   */
  public void generateSurfaceMesh() {
    genSurfaceMesh();
  }

  /**
   * This is the {@see #genVolumeMesh} method.
   */
  public void generateVolumeMesh() {
    genVolumeMesh();
  }

  /**
   * Generates the Surface Mesh.
   */
  public void genSurfaceMesh() {
    if (skipMeshGeneration) { return; }
    printAction("Generating Surface Mesh");
    sim.get(MeshPipelineController.class).generateSurfaceMesh();
  }

  /**
   * Generates the Volume Mesh.
   */
  public void genVolumeMesh() {
    if (skipMeshGeneration) {
        return;
    }
    printAction("Generating Volume Mesh");
    sim.get(MeshPipelineController.class).generateVolumeMesh();
    meshedInMacroUtils = true;
    hasBeenMeshed++;
    sayOK();
    printMeshDiagnostics();
  }

  /**
   * Get all Boundaries from all Regions.
   *
   * @return All Boundaries.
   */
  public ArrayList<Boundary> getAllBoundaries() {
    return getAllBoundaries(true);
  }

  /**
   * Get all Boundaries from all Regions based on an Array of REGEX Patterns.
   *
   * @param regexPattArray given array of REGEX search patterns.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found.
   */
  public ArrayList<Boundary> getAllBoundaries(String[] regexPattArray, boolean skipInterfaces) {
    return getAllBoundaries(regexPattArray, true, skipInterfaces);
  }

  private ArrayList<Boundary> getAllBoundaries(boolean verboseOption) {
    say("Getting all Boundaries from all Regions...", verboseOption);
    ArrayList<Boundary> ab = new ArrayList<Boundary>();
    for (Region r : getRegions(".*", false)) {
        ab.addAll(r.getBoundaryManager().getBoundaries());
    }
    say("Boundaries found: " + ab.size(), verboseOption);
    return ab;
  }


  private ArrayList<Boundary> getAllBoundaries(Region r, String regexPatt,
                                    boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Region by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    sayRegion(r, verboseOption);
    ArrayList<Boundary> chosenBdrys = (ArrayList<Boundary>) getAllBoundariesFromRegion(r, false, skipInterfaces);
    ArrayList<Boundary> retBdrys = (ArrayList<Boundary>) chosenBdrys.clone();
    for (Boundary b : chosenBdrys) {
        if (b.getRegion() != r) {
            retBdrys.remove(b);
        }
    }
    say("Boundaries found by REGEX: " + retBdrys.size(), verboseOption);
    return (ArrayList<Boundary>) retBdrys;
  }

  private ArrayList<Boundary> getAllBoundaries(String[] regexPattArray, boolean verboseOption,
                                                                            boolean skipInterfaces) {
    return getAllBoundaries(null, regexPattArray, verboseOption, skipInterfaces);
  }

  private ArrayList<Boundary> getAllBoundaries(Region r, String[] regexPattArray,
                                                    boolean verboseOption, boolean skipInterfaces) {
    if (r != null) {
        say("Getting all Boundaries from all Regions by an Array of REGEX pattern", verboseOption);
    } else {
        say("Getting all Boundaries from a Region by an Array of REGEX pattern", verboseOption);
        sayRegion(r, verboseOption);
    }
    ArrayList<Boundary> allBdrys = new ArrayList<Boundary>();
    for (int i = 0; i < regexPattArray.length; i++) {
        ArrayList<Boundary> chosenBdrys = new ArrayList<Boundary>();
        for (Boundary b : getBoundaries(regexPattArray[i], false, skipInterfaces)) {
            if (r != null && b.getRegion() != r) { continue; }
            chosenBdrys.add(b);
        }
        say(String.format("Boundaries found by REGEX pattern: \"%s\" == %d", regexPattArray[i],
                chosenBdrys.size()), verboseOption);
        allBdrys.addAll(chosenBdrys);
        chosenBdrys.clear();
    }
    say("Overal Boundaries found by REGEX: " + allBdrys.size(), verboseOption);
    return (ArrayList<Boundary>) allBdrys;
  }

  /**
   * Get all boundaries from a Region.
   *
   * @param r given Region.
   * @return All Boundaries.
   */
  public ArrayList<Boundary> getAllBoundariesFromRegion(Region r) {
    return getAllBoundariesFromRegion(r, true, false);
  }

  /**
   * Get all boundaries from a Region. Option to skip Interface Boundaries.
   *
   * @param r given Region.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public ArrayList<Boundary> getAllBoundariesFromRegion(Region r, boolean skipInterfaces) {
    return getAllBoundariesFromRegion(r, true, skipInterfaces);
  }

  private ArrayList<Boundary> getAllBoundariesFromRegion(Region r, boolean verboseOption, boolean skipInterfaces) {
    ArrayList<Boundary> ab = new ArrayList(r.getBoundaryManager().getBoundaries());
    ArrayList<Boundary> bdriesNotInterface = new ArrayList<Boundary>();
    for (Boundary b : ab) {
        if (isInterface(b)) { continue; }
        bdriesNotInterface.add(b);
    }
    if (skipInterfaces) {
        say("Getting all Boundaries but Skip Interfaces from Region: " + r.getPresentationName(), verboseOption);
        say("Non-Interface Boundaries found: " + bdriesNotInterface.size(), verboseOption);
        return (ArrayList<Boundary>) bdriesNotInterface;
    } else {
        say("Getting all Boundaries from Region: " + r.getPresentationName(), verboseOption);
        say("Boundaries found: " + ab.size(), verboseOption);
        return ab;
    }
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collected Boundaries.
   */
  public ArrayList<Boundary> getAllBoundariesFromRegionsByName(String regexPatt) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, false);
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public ArrayList<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean skipInterfaces) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, skipInterfaces);
  }

  private ArrayList<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Regions searched by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Boundary> allBdrysFromRegions = new ArrayList<Boundary>();
    for (Region reg : getRegions(regexPatt, false)) {
        allBdrysFromRegions.addAll(getAllBoundariesFromRegion(reg, false, skipInterfaces));
    }
    say("Boundaries found by REGEX: " + allBdrysFromRegions.size(), verboseOption);
    return (ArrayList<Boundary>) allBdrysFromRegions;
  }

  /**
   * Get all Composite Parts.
   *
   * @return All Composite Parts.
   */
  public ArrayList<CompositePart> getAllCompositeParts() {
    return getAllCompositeParts(true);
  }

  /**
   * Get all Composite Parts base on REGEX criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Matched Composite Parts.
   */
  public ArrayList<CompositePart> getAllCompositeParts(String regexPatt) {
    return getAllCompositeParts(regexPatt, true);
  }

  private ArrayList<CompositePart> getAllCompositeParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Composite Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<CompositePart> alcp = new ArrayList<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (cp.getPresentationName().matches(regexPatt)) {
            say("  Found: " + cp.getPresentationName(), verboseOption);
            alcp.add(cp);
        }
    }
    say("Composite Parts found by REGEX: " + alcp.size(), verboseOption);
    return alcp;
  }

  private ArrayList<CompositePart> getAllCompositeParts(boolean verboseOption) {
    say("Getting all Composite Parts...", verboseOption);
    ArrayList<CompositePart> alcpAll = new ArrayList<CompositePart>();
    for (GeometryPart gp : getAllGeometryParts(false)) {
        if (!isCompositePart(gp)) { continue; }
        ArrayList<CompositePart> alcp = new ArrayList<CompositePart>();
        for (CompositePart cp : getCompositeChildren((CompositePart) gp, alcp, false)) {
            say("  Composite Part Found: " + cp.getPresentationName(), verboseOption);
        }
        alcpAll.addAll(alcp);
    }
    say("Composite Parts found: " + alcpAll.size(), verboseOption);
    return alcpAll;
  }

  /**
   * Get all Feature Curves from all Regions.
   *
   * @return All Feature Curves.
   */
  public ArrayList<FeatureCurve> getAllFeatureCurves() {
    return getAllFeatureCurves(true);
  }

  private ArrayList<FeatureCurve> getAllFeatureCurves(boolean verboseOption) {
    say("Getting all Feature Curves...", verboseOption);
    ArrayList<FeatureCurve> alfc = new ArrayList<FeatureCurve>();
    for (Region r : getRegions(".*", verboseOption)) {
        alfc.addAll(getFeatureCurves(r, false));
    }
    say("All Feature Curves: " + alfc.size(), verboseOption);
    return alfc;
  }

  /**
   * Get all Regions that have a Solid Continua.
   *
   * @return All fluid regions.
   */
  public ArrayList<Region> getAllFluidRegions() {
    ArrayList<Region> alr = new ArrayList<Region>();
    for (Region reg : getRegions(".*", false)) {
        if (isFluid(reg)) {
            alr.add(reg);
        }
    }
    return alr;
  }

  /**
   * Get all Geometry Parts.
   *
   * @return All Geometry Parts.
   */
  public ArrayList<GeometryPart> getAllGeometryParts() {
    return getAllGeometryParts(true);
  }

  /**
   * Get all Geometry Parts based on REGEX.
   *
   * @param regexPatt REGEX search pattern.
   * @return All Geometry Parts found.
   */
  public ArrayList<GeometryPart> getAllGeometryParts(String regexPatt) {
    return getAllGeometryParts(regexPatt, true);
  }

  private ArrayList<GeometryPart> getAllGeometryParts(boolean verboseOption) {
    say("Getting all Geometry Parts...", verboseOption);
    ArrayList<GeometryPart> ag = new ArrayList(sim.get(SimulationPartManager.class).getParts());
    say("Geometry Parts found: " + ag.size(), verboseOption);
    return ag;
  }

  private ArrayList<GeometryPart> getAllGeometryParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<GeometryPart> ag = new ArrayList<GeometryPart>();
    for (GeometryPart gp : getAllGeometryParts(false)) {
        if (gp.getPresentationName().matches(regexPatt)) {
            say("  Found: " + gp.getPresentationName(), verboseOption);
            ag.add(gp);
        }
    }
    say("Geometry Parts found by REGEX: " + ag.size(), verboseOption);
    return ag;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public ArrayList<Interface> getAllInterfaces() {
    return getAllInterfaces(true);
  }

  /**
   * Get all Interfaces between 2 Regions.
   *
   * @param region0 given first Region.
   * @param region1 given second Region.
   * @return ArrayList of Interfaces shared between both Regions.
   */
  public ArrayList<Interface> getAllInterfaces(Region region0, Region region1) {
    ArrayList<Interface> intrfVec = new ArrayList<Interface>();
    say("Getting all Interfacess between 2 Regions...");
    sayRegion(region0);
    sayRegion(region1);
    Integer r0 = region0.getIndex();
    Integer r1 = region1.getIndex();
    for (Interface i : getAllInterfaces(false)) {
        Integer n0 = i.getRegion0().getIndex();
        Integer n1 = i.getRegion1().getIndex();
        if (Math.min(r0,r1) == Math.min(n0,n1) && Math.max(r0,r1) == Math.max(n0,n1)) {
            intrfVec.add(i);
            say("  Interface found: " + i);
        }
    }
    say("Interfaces found: " + intrfVec.size());
    return intrfVec;
  }

  private ArrayList<Interface> getAllInterfaces(boolean verboseOption) {
    say("Getting all Interfaces...", verboseOption);
    ArrayList<Interface> ai = new ArrayList(sim.get(InterfaceManager.class).getObjects());
    say("Interfaces found: " + ai.size(), verboseOption);
    return ai;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public ArrayList<Object> getAllInterfacesAsObjects() {
    return getAllInterfacesAsObjects(true);
  }

  private ArrayList<Object> getAllInterfacesAsObjects(boolean verboseOption) {
    say("Getting all Interfaces...", verboseOption);
    ArrayList<Object> ao = new ArrayList(sim.getInterfaceManager().getChildren());
    say("Interfaces found: " + ao.size(), verboseOption);
    return ao;
  }

  /**
   * Get all Leaf Mesh Parts in the model.
   *
   * @return ArrayList of Leaf Mesh Parts.
   */
  public ArrayList<LeafMeshPart> getAllLeafMeshParts() {
    return getAllLeafMeshParts(true);
  }

  /**
   * Get all Leaf Mesh Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return ArrayList of Leaf Mesh Parts.
   */
  public ArrayList<LeafMeshPart> getAllLeafMeshParts(String regexPatt) {
    return getAllLeafMeshParts(regexPatt, true);
  }

  private ArrayList<LeafMeshPart> getAllLeafMeshParts(boolean verboseOption) {
    say("Getting all Leaf Mesh Parts...", verboseOption);
    ArrayList<LeafMeshPart> al = new ArrayList<LeafMeshPart>();
    ArrayList<GeometryPart> ag = new ArrayList(sim.get(SimulationPartManager.class).getLeafParts());
    for (GeometryPart gp : ag) {
        if (isLeafMeshPart(gp)) {
            say("Found: " + gp.getPresentationName(), verboseOption);
            al.add((LeafMeshPart) gp);
        }
    }
    say("Leaf Mesh Parts found: " + al.size(), verboseOption);
    return al;
  }

  private ArrayList<LeafMeshPart> getAllLeafMeshParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<LeafMeshPart> lmpVec = new ArrayList<LeafMeshPart>();
    for (LeafMeshPart lmp : getAllLeafMeshParts(false)) {
        if (lmp.getPresentationName().matches(regexPatt)) {
            say("Found: " + lmp.getPresentationName(), verboseOption);
            lmpVec.add(lmp);
        }
    }
    say("Leaf Mesh Parts found by REGEX: " + lmpVec.size(), verboseOption);
    return lmpVec;
  }

  /**
   * Get all Leaf Parts in the model.
   *
   * @return ArrayList of Geometry Parts.
   */
  public ArrayList<GeometryPart> getAllLeafParts() {
    return getAllLeafParts(true);
  }

  /**
   * Get all Leaf Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return ArrayList of Geometry Parts.
   */
  public ArrayList<GeometryPart> getAllLeafParts(String regexPatt) {
    return getAllLeafParts(regexPatt, true);
  }

  private ArrayList<GeometryPart> getAllLeafParts(boolean verboseOption) {
    say("Getting all Leaf Parts...", verboseOption);
    ArrayList<GeometryPart> ag = new ArrayList(sim.get(SimulationPartManager.class).getLeafParts());
    say("Leaf Parts found: " + ag.size(), verboseOption);
    return ag;
  }

  private ArrayList<GeometryPart> getAllLeafParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    ArrayList<GeometryPart> agp = new ArrayList<GeometryPart>();
    for (GeometryPart gp : getAllLeafParts(false)) {
        if (gp.getPathInHierarchy().matches(regexPatt)) {
            say("  Found: " + gp.getPathInHierarchy(), verboseOption);
            agp.add(gp);
        }
    }
    say("Leaf Parts found by REGEX: " + agp.size(), verboseOption);
    return agp;
  }

  private ArrayList<CadPart> getAllLeafPartsAsCadParts() {
    ArrayList<CadPart> alcp = new ArrayList<CadPart>();
    for (GeometryPart gp : getAllLeafParts(false)) {
        alcp.add((CadPart) gp);
    }
    return alcp;
  }

  private ArrayList<MeshPart> getAllLeafPartsAsMeshParts() {
    ArrayList<MeshPart> am = new ArrayList<MeshPart>();
    for (GeometryPart gp : getAllLeafParts(false)) {
        am.add((MeshPart) gp);
    }
    return am;
  }

  /**
   * Get all Part Surfaces from the model.
   *
   * @return All Part Surfaces.
   */
  public ArrayList<PartSurface> getAllPartSurfaces() {
    return getPartSurfaces(".*", true);
  }

  /**
   * Get all Mesh Continuas.
   *
   * @return All Mesh Continuas.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public ArrayList<MeshContinuum> getAllMeshContinuas() {
    return getAllMeshContinuas(true);
  }

  @Deprecated // in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
  private ArrayList<MeshContinuum> getAllMeshContinuas(boolean verboseOption) {
    say("Getting all Mesh Continuas...", verboseOption);
    ArrayList<MeshContinuum> vecMC = new ArrayList<MeshContinuum>();
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if (cont.getBeanDisplayName().equals("MeshContinum")) {
            vecMC.add((MeshContinuum) cont);
        }
    }
    say("Mesh Continuas found: " + vecMC.size(), verboseOption);
    return vecMC;
  }

  /**
   * Get all Physics Continuas.
   *
   * @return All Physics Continuas.
   */
  public ArrayList<PhysicsContinuum> getAllPhysicsContinuas() {
    return getAllPhysicsContinuas(true);
  }

  private ArrayList<PhysicsContinuum> getAllPhysicsContinuas(boolean verboseOption) {
    say("Getting all Physics Continuas...", verboseOption);
    ArrayList<PhysicsContinuum> vecPC = new ArrayList<PhysicsContinuum>();
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if (cont.getBeanDisplayName().equals("PhysicsContinum")) {
            vecPC.add((PhysicsContinuum) cont);
        }
    }
    say("All Physics Continuas: " + vecPC.size(), verboseOption);
    return vecPC;
  }

  /**
   * Get all Regions.
   *
   * @return All regions.
   */
  public ArrayList<Region> getAllRegions() {
    return getRegions(".*");
  }

  /**
   * Get all Regions that have a Solid Continua.
   *
   * @return All solid regions.
   */
  public ArrayList<Region> getAllSolidRegions() {
    ArrayList<Region> alr = new ArrayList<Region>();
    for (Region reg : getRegions(".*", false)) {
        if (isSolid(reg)) {
            alr.add(reg);
        }
    }
    return alr;
  }

  /**
   * Returns the first match of an Annotation, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return An Annotation.
   */
  public star.vis.Annotation getAnnotation(String regexPatt) {
    return getAnnotation(regexPatt, true);
  }

  private star.vis.Annotation getAnnotation(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Annotation by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (star.vis.Annotation ant : sim.getAnnotationManager().getObjects()) {
        if (ant.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + ant.getPresentationName(), verboseOption);
            return ant;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Gets an ArrayList from an object. Cast to the desired the variable if necessary.
   *
   * @param obj given JAVA Object.
   * @return An ArrrayList<Object>.
   */
  public ArrayList getArrayList(Object obj) {
    return new ArrayList(Arrays.asList(new Object[] {obj}));
  }

  /**
   * Get Boundaries from all Regions based on REGEX search.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found by REGEX.
   */
  public ArrayList<Boundary> getBoundaries(String regexPatt, boolean skipInterfaces) {
    return getBoundaries(regexPatt, true, skipInterfaces);
  }

  private ArrayList<Boundary> getBoundaries(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting Boundaries from all Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Boundary> chosenBdrys = new ArrayList<Boundary>();
    for (Boundary b : getAllBoundaries(false)) {
        if (isInterface(b) && skipInterfaces) { continue; }
        if (b.getPresentationName().matches(regexPatt)) {
            chosenBdrys.add(b);
        }
    }
    say("Boundaries found by REGEX: " + chosenBdrys.size(), verboseOption);
    return (ArrayList<Boundary>) chosenBdrys;
  }

  /**
   * Loop in all Boundaries and returns the first match given the REGEX pattern. <b>Note:</b> It
   * will skip Interface Boundaries.
   *
   * @param regexPatt REGEX search pattern.
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt) {
    return getBoundary(regexPatt, true, true);
  }

  /**
   * Loop in all Boundaries and returns the first match given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt, boolean skipInterfaces) {
    return getBoundary(regexPatt, true, skipInterfaces);
  }

  /**
   * @param regexPatt
   * @param verboseOption
   * @param skipInterfaces
   * @return Boundary
   */
  private Boundary getBoundary(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Boundary> ab = getBoundaries(regexPatt, false, skipInterfaces);
    if (ab.isEmpty()) {
        say("Got NULL.\n");
        return null;
    } else {
        say("Got by REGEX: " + ab.get(0).getPresentationName(), verboseOption);
        return ab.get(0);
    }
  }

  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   * <b>Note:</b> It will skip Interface Boundaries.
   *
   * @param r given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region r, String regexPatt) {
    return getBoundary(r, regexPatt, true, true);
  }

  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   *
   * @param r given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region r, String regexPatt, boolean skipInterfaces) {
    return getBoundary(r, regexPatt, true, skipInterfaces);
  }

  /**
   * @param r
   * @param regexPatt
   * @param verboseOption
   * @param skipInterfaces
   * @return Boundary
   */
  private Boundary getBoundary(Region r, String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    sayRegion(r);
    try {
        Boundary foundBdry = r.getBoundaryManager().getBoundary(regexPatt);
        if (foundBdry != null) {
            say("Found: " + foundBdry.getPresentationName(), verboseOption);
            return foundBdry;
        }
    } catch (Exception e) { }
    ArrayList<Boundary> ab = getAllBoundaries(r, regexPatt, false, skipInterfaces);
    return getBoundary(ab, regexPatt, verboseOption, skipInterfaces);
  }

  private Boundary getBoundary(ArrayList<Boundary> ab, String regexPatt, boolean verboseOption,
                                                                            boolean skipInterfaces) {
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    if (ab.size() > 0) {
        say("Got by REGEX: " + ab.get(0).getPresentationName(), verboseOption);
        return ab.get(0);
    } else {
        say("Got NULL.", verboseOption);
        return null;
    }
  }

  /**
   * Gets a single CAD Part.
   *
   * @param name given name of the CAD Part.
   * @return CAD Part.
   */
  public CadPart getCadPart(String name) {
    return getCadPart(name, true);
  }

  /**
   * Gets a single CAD Part from a 3D-CAD body. Assumes the 3D-CAD Model has a single body.
   *
   * @param bd given 3D-CAD body.
   * @return CAD Part.
   */
  public CadPart getCadPart(Body bd) {
    return getCadPart(bd.getPresentationName(), false);
  }

  private CadPart getCadPart(String name, boolean verboseOption) {
    say("Getting CadPart by name match...", verboseOption);
    for (GeometryPart gp : getAllLeafParts(false)) {
        if (gp.getPresentationName().equals(name)) {
            say("Found: " + name);
            return (CadPart) gp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  private CameraStateInput getCameraStateInput(Scene scn) {
    CurrentView cv = scn.getCurrentView();
    return new CameraStateInput(cv.getFocalPoint(), cv.getPosition(), cv.getViewUp(),
        cv.getParallelScale(), cv.getProjectionMode());
  }

  /**
   * Returns the first match of a Camera View, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Camera View.
   */
  public VisView getCameraView(String regexPatt) {
    return getCameraView(regexPatt, true);
  }

  private VisView getCameraView(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Camera by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<VisView> alVV  = getCameraViews(regexPatt, false);
    if (alVV.isEmpty()) {
        say("Got NULL.", verboseOption);
        return null;
    }
    say("Got by REGEX: " + alVV.get(0).getPresentationName(), verboseOption);
    return alVV.get(0);
  }

  /**
   * Reads all available Camera Views and print them in the log file.
   * Cameras are stored using the following format:
   * <ul><li>Name|FocalPointVector|PositionVector|ViewUpVector|ParallelScale</li></ul>
   *
   * @return A big String with all Cameras separated by semicolons (<b>;</b>).
   */
  public String getCameraViews() {
    return getCameraViews(true);
  }

  private String getCameraViews(boolean verboseOption) {
    String camsString = "";
    //-- Prior to Macro Utils v3;
    //-- Fmt is Name|FocalPointVector|PositionVector|ViewUpVector|ParallelScale
    //-- After Macro Utils v3;
    //-- Fmt is Name|FocalPointVector|PositionVector|ViewUpVector|ParallelScale|ProjectionMode
    printAction("Dumping the Cameras Overview", verboseOption);
    ArrayList<VisView> av = new ArrayList(sim.getViewManager().getObjects());
    say("Cameras Found: " + av.size(), verboseOption);
    say("Camera Format: " + camFormat, verboseOption);
    for (VisView v : av) {
        String name = v.getPresentationName();
        DoubleVector fp = v.getFocalPoint();
        DoubleVector pos = v.getPosition();
        DoubleVector vu = v.getViewUp();
        double ps = v.getParallelScale();
        int pm = v.getProjectionMode();
        String cam = String.format(camFormat, name, fp.get(0), fp.get(1), fp.get(2),
                                                pos.get(0), pos.get(1), pos.get(2),
                                                vu.get(0), vu.get(1), vu.get(2), ps, pm);
        say(cam);
        camsString += cam + camSplitCharBetweenCams;    //-- split char
    }
    sayOK(verboseOption);
    return camsString;
  }

  /**
   * Returns the matches of Camera Views, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A ArrayList of Camera Views.
   */
  public ArrayList<VisView> getCameraViews(String regexPatt) {
    return getCameraViews(regexPatt, true);
  }

  private ArrayList<VisView> getCameraViews(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Cameras by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<VisView> vecVV = new ArrayList<VisView>();
    for (VisView v : sim.getViewManager().getObjects()) {
        if (v.getPresentationName().matches(regexPatt)) {
            say("Got: " + v.getPresentationName(), verboseOption);
            vecVV.add(v);
        }
    }
    say("Total Cameras found: " + vecVV.size(), verboseOption);
    return vecVV;
  }

  /**
   * Performs a linear interpolation between 2 cameras and generate the intermediate views.
   *
   * @param cam1 given Camera 1.
   * @param cam2 given Camera 2.
   * @param nSteps given number of wanted Cameras in between.
   * @return The ordered Vector of the transition Cameras, from Cam1 to Cam2. Vector size is
   * {@param nSteps} + 2.
   */
  public ArrayList<VisView> getCameraViews(VisView cam1, VisView cam2, int nSteps) {
    return getCameraViews(cam1, cam2, nSteps, true);
  }

  /**
   * Performs a spline interpolation between the given cameras and generate the intermediate views.
   *
   * @param cams given ArrayList of Cameras.
   * @param nSteps given number of wanted Cameras in between.
   * @return The ordered Vector of the transition Cameras. Vector size is {@param nSteps} + 1.
   */
  public ArrayList<VisView> getCameraViews(ArrayList<VisView> cams, int nSteps) {
    printAction("Interpolating Camera Views");
    say("Given Cameras: " + cams.size());
    if (cams.size() == 2) {
        say("Calling Linear Interpolator...");
        return getCameraViews(cams.get(0), cams.get(1), nSteps, true);
    }
    say("Calling Spline Interpolator...");
    //--
    ArrayList<VisView> av = new ArrayList<VisView>();
    ArrayList<Double> _x = new ArrayList<Double>(), _f = new ArrayList<Double>();
    DoubleVector dvFP = cams.get(0).getFocalPoint();
    DoubleVector dvPos = cams.get(0).getPosition();
    DoubleVector dvVU = cams.get(0).getViewUp();
    //--
    int n_delta = nSteps / (cams.size() - 1);
    //--
    for (int k = 0; k <= nSteps; k++) {
    //-- Create Temporary Cameras
        VisView v = sim.getViewManager().createView();
        v.copyProperties(cams.get(0));
        v.setPresentationName(String.format("%s_Spline_%dcams_%04d", tempCamName,
                                                            cams.size(), k));
        say("Generating: " + v.getPresentationName());
        for (int j = 0; j < 3; j++) {
            //--
            say("  Processing Focal Point ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < cams.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(cams.get(i).getFocalPoint().get(j));
            }
            double[][] splFPs = retSpline(_x, _f);
            dvFP.setElementAt(retSplineValue(splFPs, k), j);
            //--
            say("  Processing Position ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < cams.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(cams.get(i).getPosition().get(j));
            }
            double[][] splPos = retSpline(_x, _f);
            dvPos.setElementAt(retSplineValue(splPos, k), j);
            //--
            say("  Processing View Up ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < cams.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(cams.get(i).getViewUp().get(j));
            }
            double[][] splVUs = retSpline(_x, _f);
            dvVU.setElementAt(retSplineValue(splVUs, k), j);
        }
        //--
        say("  Processing Parallel Scale");
        _x.clear();
        _f.clear();
        for (int i = 0; i < cams.size(); i++) {
            _x.add((double) i * n_delta);
            _f.add(cams.get(i).getParallelScale());
        }
        double[][] splPS = retSpline(_x, _f);
        double newPS = retSplineValue(splPS, k);
        v.setFocalPoint(dvFP);
        v.setPosition(dvPos);
        v.setViewUp(dvVU);
        v.setParallelScale(newPS);
        av.add(v);
    }
    say("Cameras processed: " + av.size());
    sayOK();
    return av;
  }

  private ArrayList<VisView> getCameraViews(VisView cam1, VisView cam2, int nSteps, boolean verboseOption) {
    printAction("Linear Interpolation between 2 Camera Views", verboseOption);
    say("Camera 1: " + cam1.getPresentationName(), verboseOption);
    say("Camera 2: " + cam2.getPresentationName(), verboseOption);
    say("Number of Steps: " + nSteps, verboseOption);
    ArrayList<VisView> av = new ArrayList<VisView>();
    nSteps = Math.max(nSteps, 2);
    for (int i = 1; i <= nSteps; i++) {
        VisView v = sim.getViewManager().createView();
        v.copyProperties(cam1);
        v.setPresentationName(String.format("%s_%s_%s_%d_%04d", tempCamName,
                        cam1.getPresentationName(), cam2.getPresentationName(), nSteps, i));
        say("Generating: " + v.getPresentationName(), verboseOption);
        DoubleVector dv = retIncrement(cam1.getFocalPoint(), cam2.getFocalPoint(), i, nSteps);
        v.setFocalPoint(dv);
        dv = retIncrement(cam1.getPosition(), cam2.getPosition(), i, nSteps);
        v.setPosition(dv);
        dv = retIncrement(cam1.getViewUp(), cam2.getViewUp(), i, nSteps);
        v.setViewUp(dv);
        v.setParallelScale(retIncrement(cam1.getParallelScale(), cam2.getParallelScale(), i, nSteps));
        av.add(v);
    }
    av.add(0, cam1);
    av.add(cam2);
    say("Returning " + av.size() + " Camera Views.", verboseOption);
    sayOK(verboseOption);
    return av;
  }

  /**
   * Macro Utils internal. Get a Class based on String.
   *
   * @param cl given class name.
   * @return Class.
   */
  private Class getClass(String cl) {
    Class c = null;
    if (cl.equals(varCp)) c = SpecificHeatProperty.class;
    if (cl.equals(varDen)) c = ConstantDensityProperty.class;
    if (cl.equals(varK)) c = ThermalConductivityProperty.class;
    if (cl.equals(varPrTurb)) c = TurbulentPrandtlNumberProperty.class;
    if (cl.equals(varVisc)) c = DynamicViscosityProperty.class;
    return c;
  }

  private BaseSize getClassBaseSize(NamedObject no) {
    BaseSize bs = null;
    if (isMeshOperation(no) || isSurfaceWrapperOperation(no)) {
        bs = ((AutoMeshOperation) no).getDefaultValues().get(BaseSize.class);
    } else if (isMeshContinua(no)) {
        bs = ((MeshContinuum) no).getReferenceValues().get(BaseSize.class);
    }
    return bs;
  }

  /**
   * Returns the first match of a Colormap, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Colormap.
   */
  public LookupTable getColormap(String regexPatt) {
    return getColormap(regexPatt, true);
  }

  private LookupTable getColormap(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Colormap by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<LookupTable> alc = getColormaps(regexPatt, false);
    if (!alc.isEmpty()) {
        LookupTable l = alc.iterator().next();
        say("Got by REGEX: " + l.getPresentationName(), verboseOption);
        return l;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

 /**
   * Returns all the Colormaps matching the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Colormaps.
   */
  public ArrayList<LookupTable> getColormaps(String regexPatt) {
    return getColormaps(regexPatt, true);
  }

  private ArrayList<LookupTable> getColormaps(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Colormaps by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<LookupTable> vecC = new ArrayList<LookupTable>();
    for (LookupTable c : sim.get(LookupTableManager.class).getObjects()) {
        if (c.getPresentationName().matches(regexPatt)) {
            say("  Found: " + c.getPresentationName(), verboseOption);
            vecC.add(c);
        }
    }
    say("Colormaps found: " + vecC.size(), verboseOption);
    return vecC;
  }

  /**
   * Get all the children of a Composite Part.
   *
   * @param compPrt given Composite Part.
   * @param alcp given ArrayList of Composite Part.
   * @return Vector of the children Composite Parts.
   */
  public ArrayList<CompositePart> getCompositeChildren(CompositePart compPrt, ArrayList<CompositePart> alcp) {
    return getCompositeChildren(compPrt, alcp, true);
  }

  private ArrayList<CompositePart> getCompositeChildren(CompositePart compPrt, ArrayList<CompositePart> alcp, boolean verboseOption) {
    for (GeometryPart gp : compPrt.getChildParts().getParts()) {
        if (!isCompositePart(gp)) { continue; }
        say("Child Part: " + ((CompositePart) gp).getPathInHierarchy(), verboseOption);
        alcp.add((CompositePart) gp);
        getCompositeChildren((CompositePart) gp, alcp, verboseOption);
    }
    return alcp;
  }

  /**
   * Loop over all Composite Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Composite Part.
   */
  public CompositePart getCompositePart(String regexPatt) {
    return getCompositePart(regexPatt, true);
  }

  private CompositePart getCompositePart(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Composite Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    //--
    //-- Trying to address the limitation of not being possible to get the 1st level Composite.
    GeometryPart gp = getAllGeometryParts(regexPatt, false).iterator().next();
    if (isCompositePart(gp, false)) {
        CompositePart cp = (CompositePart) gp;
        say("Got by REGEX: " + cp.getPathInHierarchy(), verboseOption);
        return cp;
    }
    //--
    for (CompositePart cp : getAllCompositeParts(false)) {
        //say(cp.getPathInHierarchy());
        if (cp.getPathInHierarchy().matches(regexPatt)) {
            say("Got by REGEX: " + cp.getPathInHierarchy(), verboseOption);
            return cp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Gets in which level of hierarchy is the given Composite Part.
   *
   * @param cp given Composite Part.
   * @return Level of hierarchy. (E.g.: Master assembly is level 1).
   */
  public int getCompositePartLevel(CompositePart cp) {
    say("Composite Part: " + cp.getPathInHierarchy());
    int level = getCompositePartParentLevel(cp, 1, true);
    say(cp.getPresentationName() + " is level " + level + ".");
    return level;
  }

  private int getCompositePartParentLevel(CompositePart cp, int level, boolean verboseOption) {
    try{
        CompositePart parent = (CompositePart) cp.getParentPart();
        say("  Level " + level + ". Parent: " + parent.getPresentationName(), verboseOption);
        level++;
        level = getCompositePartParentLevel(parent, level, verboseOption);
    } catch (Exception e) { }
    return level;
  }

  /**
   * Returns the first match of a Coordinate System, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Coordinate System.
   */
  public CoordinateSystem getCoordinateSystem(String regexPatt) {
    return (CoordinateSystem) getNamedObject(regexPatt,
            new ArrayList(lab0.getLocalCoordinateSystemManager().getObjects()), true);
  }

  private Continuum getContinua(String regexPatt, String type, boolean verboseOption) {
    say(String.format("Getting a %s Continua by REGEX pattern: \"%s\"", type, regexPatt), verboseOption);
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if (cont.getPresentationName().matches(regexPatt)) {
            if (!type.equals("Mesh") && isMeshContinua(cont)) {
                continue;
            }
            if (!type.equals("Physics") && isPhysicsContinua(cont)) {
                continue;
            }
            say("Found: " + cont.getPresentationName(), verboseOption);
            return cont;
        }
    }
    say("Nothing found. Returning NULL!", verboseOption);
    return null;
  }

  /**
   * Returns the first match of a Derived Part, given the REGEX pattern. Note this is the same
   * method as {@see #getPart}.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Part.
   */
  public Part getDerivedPart(String regexPatt) {
    return getPart(regexPatt);
  }

    /**
     *
     * @param r1
     * @param r2
     * @return
     */
    public DirectBoundaryInterface getDirectBoundaryInterfaceBetween2Regions(Region r1, Region r2) {
    DirectBoundaryInterface intrfP = null;
    for (Interface i : sim.getInterfaceManager().getObjects()) {
        try{
            intrfP = (DirectBoundaryInterface) i;
        } catch (Exception e) { continue; }
        if (intrfP.getRegion0() == r1 && intrfP.getRegion1() == r2 ||
           intrfP.getRegion0() == r2 && intrfP.getRegion1() == r1) {
            return intrfP;
        }
    }
    return intrfP;
  }

    /**
     *
     * @param intrfName
     * @return
     */
    public DirectBoundaryInterface getDirectBoundaryInterfaceByName(String intrfName) {
    return ((DirectBoundaryInterface) sim.getInterfaceManager().getInterface(intrfName));
  }

  /**
   * Returns the first match of a Displayer, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Displayer.
   */
  public Displayer getDisplayer(String regexPatt) {
    return getDisplayer(regexPatt, true);
  }

  /**
   * Returns the first match of a Displayer belonging to a Scene, given the REGEX pattern.
   *
   * @param scn given Scene.
   * @param regexPatt REGEX search pattern.
   * @return A Displayer.
   */
  public Displayer getDisplayer(Scene scn, String regexPatt) {
    return getDisplayer(scn, regexPatt, true);
  }

  private Displayer getDisplayer(Scene scn, String regexPatt, boolean verboseOption) {
    return (Displayer) getNamedObject(regexPatt,
            new ArrayList(scn.getDisplayerManager().getObjects()), verboseOption);
  }

  private Displayer getDisplayer(String regexPatt, boolean verboseOption) {
    say(verboseOption, "Getting Displayer by REGEX pattern: \"%s\"", regexPatt);
    Displayer d = getDisplayers(regexPatt, false).iterator().next();
    say("Got by REGEX: " + d.getPresentationName(), verboseOption);
    return d;
  }

  /**
   * Returns all Displayers from all Scenes matching the given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A ArrayList of Displayers.
   */
  public ArrayList<Displayer> getDisplayers(String regexPatt) {
    return getDisplayers(regexPatt, true);
  }

  private ArrayList<Displayer> getDisplayers(String regexPatt, boolean verboseOption) {
    ArrayList<Displayer> vd = new ArrayList<Displayer>();
    say(verboseOption, "Getting Displayers by REGEX pattern: \"%s\"", regexPatt);
    for (Scene scn : sim.getSceneManager().getObjects()) {
        for (Displayer d : scn.getDisplayerManager().getObjects()) {
            if (d.getPresentationName().matches(regexPatt)) {
                say("  Found: " + d.getPresentationName(), verboseOption);
                vd.add(d);
            }
        }
    }
    say("Displayers found: " + vd.size(), verboseOption);
    return vd;
  }

  private DoubleVector getDoubleVector(double ... floats) {
    return new DoubleVector(floats);
  }

  /**
   * Get the Geometric Range from a ArrayList of Part Surfaces. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param aps given ArrayList of Part Surfaces.
   * @return A DoubleVector with 9 components in the following order
   * (minX, maxX, minY, maxY, minZ, maxZ, dX, dY, dZ).
   */
  public DoubleVector getExtents(ArrayList<PartSurface> aps) {
    return queryStats(aps);
  }

  /**
   * Get the Geometric Range from a Composite Part. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param cp given Composite Part.
   * @return A DoubleVector with 9 components in the following order
   * (minX, maxX, minY, maxY, minZ, maxZ, dX, dY, dZ).
   */
  public DoubleVector getExtents(CompositePart cp) {
    return queryStats(getPartSurfaces(cp, false));
  }

  /**
   * Get the Geometric Range from a Geometry Part. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param gp given Geometry Part.
   * @return A DoubleVector with 9 components in the following order
   * (minX, maxX, minY, maxY, minZ, maxZ, dX, dY, dZ).
   */
  public DoubleVector getExtents(GeometryPart gp) {
    return queryStats(new ArrayList(gp.getPartSurfaces()));
  }

    /**
     *
     * @param r
     * @param name
     * @return
     */
    public FeatureCurve getFeatureCurve(Region r, String name) {
    return r.getFeatureCurveManager().getFeatureCurve(name);
  }

  /**
   * Get all the Feature Curves from a Region.
   *
   * @param r given Region.
   * @return All Feature Curves from that Region.
   */
  public ArrayList<FeatureCurve> getFeatureCurves(Region r) {
    return getFeatureCurves(r, true);
  }

  private ArrayList<FeatureCurve> getFeatureCurves(Region r, boolean verboseOption) {
    say("Getting Feature Curves...", verboseOption);
    sayRegion(r, verboseOption);
    ArrayList<FeatureCurve> vfc = (ArrayList<FeatureCurve>) r.getFeatureCurveManager().getFeatureCurves();
    say("All Feature Curves: " + vfc.size(), verboseOption);
    return vfc;
  }

  /**
   * Loop over all Field Functions and returns the first match, given the REGEX pattern.<p>
   * Note the search will be done on Function Name first and then on its name on GUI,
   * i.e., the PresentationName.
   *
   * @param regexPatt  REGEX search pattern.
   * @return Field Function.
   */
  public FieldFunction getFieldFunction(String regexPatt) {
    return getFieldFunction(regexPatt, true);
  }

  private FieldFunction getFieldFunction(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Field Function by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (FieldFunction f : sim.getFieldFunctionManager().getObjects()) {
        if (f.getFunctionName().matches(regexPatt)) {
            say("Got by REGEX: " + f.getFunctionName(), verboseOption);
            return f;
        }
        if (f.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + f.getPresentationName(), verboseOption);
            return f;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Loop over the Field Functions and return the matches, given the REGEX pattern.<p>
   * Note the search will be done on Function Name first and then on its name on GUI,
   * i.e., the PresentationName.
   *
   * @param regexPatt REGEX search pattern.
   * @return ArrayList of Field Functions.
   */
  public ArrayList<FieldFunction> getFieldFunctions(String regexPatt) {
    return getFieldFunctions(regexPatt, true);
  }

  private ArrayList<FieldFunction> getFieldFunctions(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Field Functions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<FieldFunction> vff = new ArrayList<FieldFunction>();
    for (FieldFunction f : sim.getFieldFunctionManager().getObjects()) {
        if (f.getFunctionName().matches(regexPatt)) {
            say("  Found: " + f.getPresentationName(), verboseOption);
            vff.add(f);
            continue;
        }
        if (f.getPresentationName().matches(regexPatt)) {
            say("  Found: " + f.getPresentationName(), verboseOption);
            vff.add(f);
            continue;
        }
    }
    say("Field Functions found: " + vff.size(), verboseOption);
    return vff;
  }

  /**
   * Gets the Geometry Part from a Surface Wrapper Operation.
   *
   * @param swamo given Surface Wrapper Mesh Operation.
   * @return The Geometry Part.
   */
  public GeometryPart getGeometryPart(SurfaceWrapperAutoMeshOperation swamo) {
    return swamo.getOutputParts().getObjects().iterator().next();
  }

  /**
   * Loop over all Geometry Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt  REGEX search pattern.
   * @return Geometry Part.
   */
  public GeometryPart getGeometryPart(String regexPatt) {
    say(String.format("Getting Geometry Part by REGEX pattern: \"%s\"", regexPatt), true);
    ArrayList<GeometryPart> agp = getAllGeometryParts(regexPatt, false);
    if (agp.isEmpty()) {
        say("Got NULL.");
        return null;

    }
    GeometryPart gp = agp.get(0);
    sayPart(gp);
    return gp;
  }

  /**
   * Gets the current Iteration.
   *
   * @return The current iteration number.
   */
  public int getIteration() {
    return sim.getSimulationIterator().getCurrentIteration();
  }

  /**
   * Gets the Iteration Monitor. It is a type of {@see PlotableMonitor}.
   *
   * @return The Iteration Monitor.
   */
  public IterationMonitor getIterationMonitor() {
    return ((IterationMonitor) sim.getMonitorManager().getMonitor("Iteration"));
  }

  /**
   * Loop over all Leaf Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Leaf Mesh Part.
   */
  public LeafMeshPart getLeafMeshPart(String regexPatt) {
    return getLeafMeshPart(regexPatt, true);
  }

  private LeafMeshPart getLeafMeshPart(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Leaf Mesh Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (LeafMeshPart lmp : getAllLeafMeshParts(regexPatt, false)) {
        say("Got by REGEX: " + lmp.getPresentationName(), verboseOption);
        return lmp;
    }
    say("Got NULL.");
    return null;
  }

  /**
   * Loop over all Leaf Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Geometry Part.
   */
  public GeometryPart getLeafPart(String regexPatt) {
    return getLeafPart(regexPatt, true);
  }

  private GeometryPart getLeafPart(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Leaf Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (GeometryPart gp : getAllLeafParts(regexPatt, false)) {
        say("Got by REGEX: " + gp.getPathInHierarchy(), verboseOption);
        return gp;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Get the Legend from a Displayer, if applicable.
   *
   * @param d given Displayer. E.g.: Scalar, Vector or Streamline Displayer.
   * @return The Legend.
   */
  public Legend getLegend(Displayer d) {
    return getLegend(d, true);
  }

  private Legend getLegend(Displayer d, boolean verboseOption) {
    if (isScalar(d)) {
        return ((ScalarDisplayer) d).getLegend();
    } else if (isStreamline(d)) {
        return ((StreamDisplayer) d).getLegend();
    } else if (isVector(d)) {
        return ((VectorDisplayer) d).getLegend();
    }
    say("There is no Legend in Displayer: " + d.getPresentationName(), verboseOption);
    return null;
  }

  /**
   * Get Macro Utils version and build number.
   * @return Version and Build number.
   */
  private String getMacroUtilsVersion() {
    String fmt = "Macro Utils " + MACROUTILSVERSION + " build %s";
    String bld = "Unknown";
    try {
        ResourceBundle rb = ResourceBundle.getBundle("version");
        bld = rb.getString("BUILD");
    } catch (MissingResourceException e) { }
    return String.format(fmt, bld);
  }

  /**
   * Macro Utils internal. Get a Model based on String.
   *
   * @param pc given Physics Continua.
   * @return Model.
   */
  private Model getMaterialModel(PhysicsContinuum pc) {
    Model m = null;
    if (hasModel(pc, "Gas")) m = pc.getModelManager().getModel(SingleComponentGasModel.class);
    if (hasModel(pc, "Liquid")) m = pc.getModelManager().getModel(SingleComponentLiquidModel.class);
    if (hasModel(pc, "Solid")) m = pc.getModelManager().getModel(SolidModel.class);
    return m;
  }

  private ConstantMaterialPropertyMethod getMatPropMeth_Const(Model model, Class propClass) {
    MaterialPropertyMethod mpm = null;
    if (model.getClass().getName().matches(".*SingleComponentGasModel$")) {
        SingleComponentGasModel scgm = (SingleComponentGasModel) model;
        Gas gas = (Gas) scgm.getMaterial();
        mpm = gas.getMaterialProperties().getMaterialProperty(propClass).getMethod();
    }
    if (model.getClass().getName().matches(".*SingleComponentLiquidModel$")) {
        SingleComponentLiquidModel sclm = (SingleComponentLiquidModel) model;
        Liquid liq = (Liquid) sclm.getMaterial();
        mpm = liq.getMaterialProperties().getMaterialProperty(propClass).getMethod();
    }
    if (model.getClass().getName().matches(".*SinglePhaseGasModel$")) {
        SinglePhaseGasModel spgm = (SinglePhaseGasModel) model;
        SinglePhaseGas spg = (SinglePhaseGas) spgm.getMaterial();
        mpm = spg.getMaterialProperties().getMaterialProperty(propClass).getMethod();
    }
    if (model.getClass().getName().matches(".*SinglePhaseLiquidModel$")) {
        SinglePhaseLiquidModel splm = (SinglePhaseLiquidModel) model;
        SinglePhaseLiquid spl = (SinglePhaseLiquid) splm.getMaterial();
        mpm = spl.getMaterialProperties().getMaterialProperty(propClass).getMethod();
    }
    return (ConstantMaterialPropertyMethod) mpm;
  }

  /**
   * Gets the Mesh Base Size from a Mesh Operation or Continua, if applicable.
   *
   * @param mo given Named Object.
   * @param verboseOption Print to standard output?
   * @return The Base Size value in the {@see #defUnitLength} unit.
   */
  public double getMeshBaseSize(NamedObject no, boolean verboseOption) {
    printAction("Getting the Mesh Base Size", verboseOption);
    sayNamedObject(no, verboseOption);
    BaseSize bs = getClassBaseSize(no);
    double val = 0.0;
    if (bs != null) {
        Units u = bs.getUnits();
        val = bs.getValue() * u.getConversion() / defUnitLength.getConversion();
    }
    say("Base Size: " + val + defUnitLength.getPresentationName(), verboseOption);
    return val;
  }

  /**
   * Get a Mesh Continua based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum getMeshContinua(String regexPatt) {
    return (MeshContinuum) getContinua(regexPatt, "Mesh", true) ;
  }

  /**
   * Get a Mesh Operation based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Mesh Operation.
   */
  public MeshOperation getMeshOperation(String regexPatt) {
    return (MeshOperation) getNamedObject(regexPatt,
            new ArrayList(sim.get(MeshOperationManager.class).getObjects()), true);

  }

  /**
   * This method helps getting the Meshers for an Automated Mesh Operation.
   *
   * @param sm enable Surface Remesher? True or False.
   * @param smar enable Automatic Surface Repair for Remesher? True or False.
   * @param vm given Volume Mesher choice. Currently can be {@see #POLY}, {@see #TRIMMER}
   *    or {@see #TETRA}.
   * @param pl enable Prism Layers? True or False.
   * @return An ArrayList of Strings. Useful with {@see #createMeshOperation_AutomatedMesh}.
   */
  public ArrayList<String> getMeshers(boolean sm, boolean smar, int vm, boolean pl) {
    ArrayList<String> m = new ArrayList();
    if (smar) {
        sm = true;
    }
    if (sm) {
        m.add("star.resurfacer.ResurfacerAutoMesher");
    }
    if (smar) {
        m.add("star.resurfacer.AutomaticSurfaceRepairAutoMesher");
    }
    if (vm == POLY) {
        m.add("star.dualmesher.DualAutoMesher");
    } else if (vm == TRIMMER) {
        m.add("star.trimmer.TrimmerAutoMesher");
    } else if (vm == TETRA) {
        m.add("star.delaunaymesher.DelaunayAutoMesher");
    }
    if (pl) {
        m.add("star.prismmesher.PrismAutoMesher");
    }
    return m;
  }

  /**
   * Loop over all Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Mesh Part.
   */
  public MeshPart getMeshPart(String regexPatt) {
    return getMeshPart(regexPatt, true);
  }

  private MeshPart getMeshPart(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    ArrayList<MeshPart> am = getMeshParts(regexPatt, false);
    if (am.size() > 0) {
        say("Got by REGEX: " + am.get(0).getPathInHierarchy(), verboseOption);
        return am.get(0);
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Returns all Mesh Parts based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return ArrayList of Mesh Parts.
   */
  public ArrayList<MeshPart> getMeshParts(String regexPatt) {
    return getMeshParts(regexPatt, true);
  }

  private ArrayList<MeshPart> getMeshParts(String regexPatt, boolean verboseOption) {
    ArrayList<MeshPart> am = new ArrayList<MeshPart>();
    say(String.format("Getting Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (MeshPart mp : getAllLeafPartsAsMeshParts()) {
        if (mp.getPathInHierarchy().matches(regexPatt)) {
            say("  Found: " + mp.getPathInHierarchy(), verboseOption);
            am.add(mp);
        }
    }
    say("Mesh Parts found: " + am.size(), verboseOption);
    return am;
  }

  /**
   * Returns the first match of a Monitor, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Monitor.
   */
  public Monitor getMonitor(String regexPatt) {
    return getMonitor(regexPatt, true);
  }

  private Monitor getMonitor(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Monitor by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Monitor> colMon = getMonitors(regexPatt, false);
    if (!colMon.isEmpty()) {
        Monitor mon = colMon.iterator().next();
        say("Got by REGEX: " + mon.getPresentationName(), verboseOption);
        return mon;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

 /**
   * Returns all the Monitors matching the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Monitors.
   */
  public ArrayList<Monitor> getMonitors(String regexPatt) {
    return getMonitors(regexPatt, true);
  }

  private ArrayList<Monitor> getMonitors(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Monitors by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Monitor> vecMon = new ArrayList<Monitor>();
    for (Monitor mon : sim.getMonitorManager().getObjects()) {
        if (mon.getPresentationName().matches(regexPatt)) {
            say("  Found: " + mon.getPresentationName(), verboseOption);
            vecMon.add(mon);
        }
    }
    say("Monitors found: " + vecMon.size(), verboseOption);
    return vecMon;
  }

  private NamedObject getNamedObject(String regexPatt, ArrayList<NamedObject> ano, boolean verboseOption) {
    if (!ano.isEmpty()) {
        say(String.format("Getting %s by REGEX pattern: \"%s\"",
                ano.get(0).getClass().getName(), regexPatt), verboseOption);
        for (NamedObject no : ano) {
            if (no.getPresentationName().matches(regexPatt)) {
                say("Got by REGEX: " + no.getPresentationName(), verboseOption);
                return no;
            }
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  private Object getNewObject(ArrayList objOld, ArrayList objNew) {
    for (Object o : objNew) {
        if (!objOld.contains(o)) {
            return o;
        }
    }
    return null;
  }

  private NeoObjectVector getNeoObjectVector1(ClientServerObject cso) {
    return new NeoObjectVector(new ClientServerObject[] {cso});
  }

  private NeoObjectVector getNeoObjectVector2(ArrayList al) {
    return new NeoObjectVector(al.toArray());
  }

  /**
   * Get all the N'th level Composite Parts. <p>
   * E.g.: Master Assembly == Level 1; Sub Assembly == Level 2; etc...
   *
   * @param nthLevel given wanted level.
   * @return ArrayList of all Composite Parts at N'th level.
   */
  public ArrayList<CompositePart> getNthLevelCompositeParts(int nthLevel) {
    ArrayList<CompositePart> vecNthLevel = new ArrayList<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (getCompositePartLevel(cp) == nthLevel) {
            vecNthLevel.add(cp);
        }
    }
    return vecNthLevel;
  }

  private String getParentName(NamedObject no) {
    String s = no.getParent().getBeanDisplayName();
    if (s.endsWith("ies")) {
        return s.replace("ies", "y");
    } else if (s.endsWith("s")) {
        return s.replace("s", "");
    }
    return s;
  }

  /**
   * Returns the first match of a Part, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Part.
   */
  public Part getPart(String regexPatt) {
    return (Part) getNamedObject(regexPatt,
            new ArrayList(sim.getPartManager().getObjects()), true);
  }

  /**
   * Loops through all Part Curves and returns the first match, given the REGEX pattern.
   *
   * @param gp
   * @param regexPatt REGEX search pattern.
   * @return The Part Curve.
   */
  public PartCurve getPartCurve(GeometryPart gp, String regexPatt) {
    say(String.format("Getting Part Curve by REGEX pattern: \"%s\"", regexPatt));
    PartCurve pc = getPartCurves(gp, regexPatt, false).iterator().next();
    say("Got by REGEX: " + pc.getPresentationName());
    return pc;
  }

  /**
   * Returns the Part Curves from all Geometry Parts based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return ArrayList of Part Curves.
   */
  public ArrayList<PartCurve> getPartCurves(String regexPatt) {
    say(String.format("Getting Part Curves by REGEX pattern: \"%s\"", regexPatt));
    ArrayList<PartCurve> vecPC = new ArrayList<PartCurve>();
    for (GeometryPart gp : getAllLeafParts(false)) {
        vecPC.addAll(getPartCurves(gp, regexPatt, false));
    }
    say("Part Curves found: " + vecPC.size());
    return vecPC;
  }

  /**
   * Returns all Part Curves based on REGEX pattern.
   *
   * @param gp
   * @param regexPatt REGEX search pattern.
   * @return ArrayList of Part Curves.
   */
  public ArrayList<PartCurve> getPartCurves(GeometryPart gp, String regexPatt) {
    return getPartCurves(gp, regexPatt, true);
  }

  private ArrayList<PartCurve> getPartCurves(GeometryPart gp, String regexPatt, boolean verboseOption) {
    say(String.format("Getting Part Curves by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<PartCurve> vecPC = new ArrayList<PartCurve>();
    for (PartCurve pc : gp.getPartCurves()) {
        if (pc.getPresentationName().matches(regexPatt)) {
            say("  Found: " + pc.getPresentationName(), verboseOption);
            vecPC.add(pc);
        }
    }
    say("Part Curves found: " + vecPC.size(), verboseOption);
    return vecPC;
  }

  /**
   * Get all the Part Curves from a Geometry Part.
   *
   * @param gp given Geometry Part.
   * @return ArrayList of Part Curves.
   */
  public ArrayList<PartCurve> getPartCurves(GeometryPart gp) {
    return new ArrayList(gp.getPartCurves());
  }

  /**
   * Returns all Parts based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return ArrayList of Parts.
   */
  public ArrayList<Part> getParts(String regexPatt) {
    return getParts(regexPatt, true);
  }

  private ArrayList<Part> getParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Part> vecP = new ArrayList<Part>();
    for (Part p : sim.getPartManager().getObjects()) {
        if (p.getPresentationName().matches(regexPatt)) {
            say("  Found: " + p.getPresentationName(), verboseOption);
            vecP.add(p);
        }
    }
    say("Parts found: " + vecP.size(), verboseOption);
    return vecP;
  }

  /**
   * Get a Part Surface that matches the REGEX pattern. All Part Surfaces are used.
   * <p>
   * Only the Part Surface name will be taken into account.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(String regexPatt) {
    return getPartSurfaces(regexPatt, true).get(0);
  }

  /**
   * Gets a Part Surface from a Geometry Part based on REGEX pattern.
   *
   * @param gp given Geometry Part.
   * @param regexPatt
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String regexPatt) {
    return getPartSurface(gp, regexPatt, true);
  }

  private PartSurface getPartSurface(GeometryPart gp, String regexPatt, boolean verboseOption) {
    say(String.format("Getting a Part Surface by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (PartSurface ps : gp.getPartSurfaces()) {
        if (ps.getPresentationName().matches(regexPatt)) {
            say("  Found: " + ps.getPresentationName(), verboseOption);
            return ps;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops in all Part Surfaces from Part and returns
   * the first it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String rangeType, String what, double tol) {
    PartSurface ps = (PartSurface) queryPartSurfaces(new ArrayList(gp.getPartSurfaces()), rangeType, what, tol * defUnitLength.getConversion()).get(0);
    say("Returning: " + ps.getPresentationName());
    return ps;
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops into specific Part Surfaces according
   * to REGEX criteria and returns the first it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    PartSurface ps = queryPartSurfaces(getPartSurfaces(gp, regexPatt), rangeType, what, tol * defUnitLength.getConversion()).get(0);
    say("Returning: " + ps.getPresentationName());
    return ps;
  }

  /**
   * Get all Part Surfaces that matches the REGEX search.
   * <p>
   * Only the Part Surface name will be taken into account.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(String regexPatt) {
    return getPartSurfaces(regexPatt, true);
  }

  private ArrayList<PartSurface> getPartSurfaces(String regexPatt, boolean verboseOption) {
    ArrayList<PartSurface> psVec = new ArrayList<PartSurface>();
    if (regexPatt.equals(".*")) {
        say("Getting all Part Surfaces from All Leaf Parts", verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    }
    for (GeometryPart gp : getAllLeafParts(false)) {
        psVec.addAll(getPartSurfaces(gp, regexPatt, false));
    }
    say("Total Part Surfaces found: " + psVec.size(), verboseOption);
    if (!regexPatt.matches(".*")) {
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }

  /**
   * Get all the Part Surfaces from a Composite Part.
   *
   * @param cp given Composite Part.
   * @return ArrayList of Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(CompositePart cp) {
    return getPartSurfaces(cp, true);
  }

  private ArrayList<PartSurface> getPartSurfaces(CompositePart cp, boolean verboseOption) {
    ArrayList<PartSurface> psVec = new ArrayList<PartSurface>();
    say("Getting all Part Surfaces from a Composite Part", verboseOption);
    say ("CP: " + cp.getPathInHierarchy());
    for (GeometryPart gp : cp.getLeafParts()) {
        say ("GP: " + gp.getPathInHierarchy());
        if (isCompositePart(gp)) {
            psVec.addAll(getPartSurfaces((CompositePart) gp, false));
            continue;
        }
        psVec.addAll(gp.getPartSurfaces());
    }
    say("Total Part Surfaces found: " + psVec.size(), verboseOption);
    return psVec;
  }

  /**
   * Get all the Part Surfaces from a Geometry Part.
   *
   * @param gp given Geometry Part.
   * @return ArrayList of Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(GeometryPart gp) {
    return new ArrayList(gp.getPartSurfaces());
  }

  /**
   * Get all Part Surfaces that matches the REGEX pattern from a Geometry Part .
   *
   * @param gp given Geometry Part.
   * @param regexPatt given REGEX search pattern.
   * @return ArrayList of Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(GeometryPart gp, String regexPatt) {
    return getPartSurfaces(gp, regexPatt, true);
  }

  private ArrayList<PartSurface> getPartSurfaces(GeometryPart gp, String regexPatt, boolean verboseOption) {
    ArrayList<PartSurface> psVec = new ArrayList<PartSurface>();
    sayPart(gp, verboseOption);
    if (regexPatt.equals(".*")) {
        say("Getting all Part Surfaces from Part...", verboseOption);
        psVec.addAll(gp.getPartSurfaces());
        say("Part Surfaces found: " + psVec.size(), verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
        for (PartSurface ps : gp.getPartSurfaces()) {
            if (ps.getPresentationName().matches(regexPatt)) {
                say("  Found: " + ps.getPresentationName(), verboseOption);
                psVec.add(ps);
            }
        }
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }


/**
   * Gets a Part Surface based on a Geometric Range. Loops in all Part Surfaces from Part and returns
   * all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what, double tol) {
    return queryPartSurfaces(new ArrayList(gp.getPartSurfaces()), rangeType, what, tol * defUnitLength.getConversion());
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops into specific Part Surfaces according
   * to REGEX criteria and returns all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(getPartSurfaces(gp, regexPatt), rangeType, what, tol * defUnitLength.getConversion());
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the ArrayList of Part Surfaces provided.
   * Loops in them and returns the first it can find.
   *
   * @param aps given ArrayList of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(ArrayList<PartSurface> aps, String rangeType, String what, double tol) {
    PartSurface ps = queryPartSurfaces(aps, rangeType, what, tol * defUnitLength.getConversion()).get(0);
    say("Returning: " + ps.getPresentationName());
    return ps;
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the ArrayList of Part Surfaces provided.
   * Loops in them and returns all it can find.
   *
   * @param aps given ArrayList of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@see #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public ArrayList<PartSurface> getPartSurfaces(ArrayList<PartSurface> aps, String rangeType, String what, double tol) {
    return queryPartSurfaces(aps, rangeType, what, tol * defUnitLength.getConversion());
  }

  private ArrayList<PatchCurve> getPatchCurves(DirectedPatchSourceMesh patchMsh) {
      return new ArrayList(patchMsh.getPatchCurveManager().getObjects());
  }

  private ArrayList<PatchVertex> getPatchVertices(DirectedPatchSourceMesh patchMsh) {
      return new ArrayList(patchMsh.getPatchVertexManager().getObjects());
  }

  /**
   * Gets the current physical time.
   *
   * @return The current physical time.
   */
  public double getPhysicalTime() {
    return sim.getSolution().getPhysicalTime();
  }

  /**
   * Gets the Physical Time Monitor. It is a type of {@see PlotableMonitor}.
   *
   * @return The Physical Time Monitor.
   */
  public PhysicalTimeMonitor getPhysicalTimeMonitor() {
    return ((PhysicalTimeMonitor) sim.getMonitorManager().getMonitor("Physical Time"));
  }

  /**
   * Get a Physics Continua based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Mesh Continua.
   */
  public PhysicsContinuum getPhysicsContinua(String regexPatt) {
    return (PhysicsContinuum) getContinua(regexPatt, "Physics", true) ;
//    return getPhysicsContinua(regexPatt, true);
  }

  /**
   * Returns the first match of a Plot, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Plot.
   */
  public StarPlot getPlot(String regexPatt) {
    return getPlot(regexPatt, true);
  }

  private StarPlot getPlot(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Scene by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        if (sp.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + sp.getPresentationName(), verboseOption);
            return sp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

 /**
   * Returns all the Plots matching the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Plots.
   */
  public ArrayList<StarPlot> getPlots(String regexPatt) {
    return getPlots(regexPatt, true);
  }

  private ArrayList<StarPlot> getPlots(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Plots by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<StarPlot> vecSP = new ArrayList<StarPlot>();
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        if (sp.getPresentationName().matches(regexPatt)) {
            say("  Found: " + sp.getPresentationName(), verboseOption);
            vecSP.add(sp);
        }
    }
    say("Plots found: " + vecSP.size(), verboseOption);
    return vecSP;

  }

  /**
   * @param sp StarPlot
   * @return PlotUpdate
   */
  private PlotUpdate getPlotUpdate(StarPlot sp) {
    if (isResidual(sp)) {
        return ((ResidualPlot) sp).getPlotUpdate();
    }
    if (isMonitorPlot(sp)) {
        return ((MonitorPlot) sp).getPlotUpdate();
    }
    if (isXYPlot(sp)) {
        return ((XYPlot) sp).getPlotUpdate();
    }
    return null;
  }

  private double getRadius(DirectedPatchSourceMesh patchMsh, CylindricalCoordinateSystem c) {
    double maxR = 0.;
    for (PatchVertex pv : getPatchVertices(patchMsh)) {
        Vector3 xyz = getVector3(pv, c);
        //say(pv.getPresentationName() + " in Cyl CSYS: " + xyz.toString());
        maxR = Math.max(maxR, xyz.x);
    }
    say("Pipe Radius: " + maxR + "m.");
    return maxR;
  }

  /**
   * Returns the first match of Region, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Region.
   */

  public Region getRegion(String regexPatt) {
    return (Region) getNamedObject(regexPatt,
            new ArrayList(sim.getRegionManager().getObjects()), true);
  }

  /**
   * Get the Regions based on REGEX search.
   *
   * @param regexPatt search REGEX pattern.
   * @return Found regions.
   */
  public ArrayList<Region> getRegions(String regexPatt) {
    return getRegions(regexPatt, true);
  }

  private ArrayList<Region> getRegions(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Region> ar = new ArrayList();
    for (Region reg : sim.getRegionManager().getRegions()) {
        if (reg.getPresentationName().matches(regexPatt)) {
            say("  Found: " + reg.getPresentationName(), verboseOption);
            ar.add(reg);
        }
    }
    say("Regions found by REGEX: " + ar.size(), verboseOption);
    return ar;
  }

  /**
   * Gets the relative error between 2 numbers.
   *
   * @param f1 given number 1.
   * @param f2 given number 2.
   * @param absolute Absolute relative error?
   * @return The relative error.
   */
  public double getRelativeError(double f1, double f2, boolean absolute) {
    double relErr = (f1 - f2) / (f2 + SN);
    if (absolute) {
        return Math.abs(relErr);
    }
    return relErr;
  }

  /**
   * Returns the first match of a Report, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Report.
   */
  public Report getReport(String regexPatt) {
    return getReport(regexPatt, true);
  }

  private Report getReport(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Report by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Report> colRep = getReports(regexPatt, false);
    if (!colRep.isEmpty()) {
        Report rep = colRep.iterator().next();
        say("Got by REGEX: " + rep.getPresentationName(), verboseOption);
        return rep;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

 /**
   * Returns all the Reports matching the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Reports.
   */
  public ArrayList<Report> getReports(String regexPatt) {
    return getReports(regexPatt, true);
  }

  private ArrayList<Report> getReports(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Reports by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Report> vecMon = new ArrayList<Report>();
    for (Report rep : sim.getReportManager().getObjects()) {
        if (rep.getPresentationName().matches(regexPatt)) {
            say("  Found: " + rep.getPresentationName(), verboseOption);
            vecMon.add(rep);
        }
    }
    say("Monitors found: " + vecMon.size(), verboseOption);
    return vecMon;
  }

//  private Profile getProfile(ConditionManager cm, String name, boolean verboseOption) {
  private Profile getProfile(ClientServerObjectManager csom, String name, boolean verboseOption) {
    if (!csom.has(name)) {
        say(verboseOption, "'%s' does not have a '%s' value. Returning NULL...",
            csom.getPresentationName(), name);
        return null;
    }
    return (Profile) csom.getObject(name);
  }

  private Profile getProfile(Boundary b, String name, boolean verboseOption) {
    if (!b.getValues().has(name)) {
        say(verboseOption, "Boundary '%s' does not have a '%s' in Physics Values. Returning NULL...",
            b, name);
        return null;
    }
    return (Profile) b.getValues().getObject(name);
  }

  /**
   * Get the Scalar Display Quantity from a Displayer, if applicable.
   *
   * @param d given Displayer. E.g.: Scalar or Streamline Displayer.
   * @return The ScalarDisplayQuantity.
   */
  public ScalarDisplayQuantity getScalarDisplayQuantity(Displayer d) {
    return getScalarDisplayQuantity(d, true);
  }

  private ScalarDisplayQuantity getScalarDisplayQuantity(Displayer d, boolean verboseOption) {
    if (isScalar(d)) {
        return ((ScalarDisplayer) d).getScalarDisplayQuantity();
    } else if (isStreamline(d)) {
        return ((StreamDisplayer) d).getScalarDisplayQuantity();
    }
    say("There is no ScalarDisplayQuantity in Displayer: " + d.getPresentationName(), verboseOption);
    return null;
  }

  private ScalarProfile getScalarProfile(Boundary b, String name, boolean verboseOption) {
    return (ScalarProfile) getProfile(b, name, verboseOption);
  }

  private ScalarProfile getScalarProfile(ClientServerObjectManager csom, String name, boolean verboseOption) {
    return (ScalarProfile) getProfile(csom, name, verboseOption);
  }

  /**
   * Returns the first match of a Scene, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Scene.
   */
  public Scene getScene(String regexPatt) {
    return getScene(regexPatt, true);
  }

  private Scene getScene(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Scene by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Scene> colSCN = getScenes(regexPatt, false);
    if (!colSCN.isEmpty()) {
        Scene scn = colSCN.iterator().next();
        say("Got by REGEX: " + scn.getPresentationName(), verboseOption);
        setSceneLogo(scn);
        return scn;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

 /**
   * Returns all the Scenes matching the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The Scenes.
   */
  public ArrayList<Scene> getScenes(String regexPatt) {
    return getScenes(regexPatt, true);
  }

  private ArrayList<Scene> getScenes(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Scenes by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<Scene> vecSCN = new ArrayList<Scene>();
    for (Scene scn : sim.getSceneManager().getObjects()) {
        if (scn.getPresentationName().matches(regexPatt)) {
            say("  Found: " + scn.getPresentationName(), verboseOption);
            vecSCN.add(scn);
        }
    }
    say("Scenes found: " + vecSCN.size(), verboseOption);
    return vecSCN;
  }

  /**
   * @param scn Scene.
   * @return SceneUpdate.
   */
  private SceneUpdate getSceneUpdate(Scene scn) {
    return scn.getSceneUpdate();
  }

  /**
   * Returns the first match of a Solution History, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Solution History.
   */
  public SolutionHistory getSolutionHistory(String regexPatt) {
    return (SolutionHistory) getNamedObject(regexPatt,
            new ArrayList(sim.get(SolutionHistoryManager.class).getObjects()), true);
  }

  /**
   * Returns the first match of a Solution Representation, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Solution Representation.
   */
  public SolutionRepresentation getSolutionRepresentation(String regexPatt) {
    return (SolutionRepresentation) getNamedObject(regexPatt,
            new ArrayList(sim.getRepresentationManager().getObjects()), true);
  }

  /**
   * Returns the first match of a Solution View, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Solution View.
   */
  public SolutionView getSolutionView(String regexPatt) {
    return (SolutionView) getNamedObject(regexPatt,
            new ArrayList(sim.get(SolutionViewManager.class).getObjects()), true);
  }

  /**
   * Gets a specific Solver from STAR-CCM+ API.
   *
   * @param solver given Solver Class name.
   * @return The Solver. Casting the variable might be necessary. Returns <b>null</b> if it is not available.
   */
  public Solver getSolver(Class solver) {
    try {
        return sim.getSolverManager().getSolver(solver);
    } catch (Exception e) {
        return null;
    }
  }

  /**
   * Get the maximum inner iterations for the unsteady simulation.
   *
   * @return Maximum inner iterations.
   */
  public int getSolverMaxInnerIterations() {
    printAction("Getting Maximum Number of Inner Iterations");
    int n = ((InnerIterationStoppingCriterion) getStoppingCriteria("Maximum Inner Iterations.*",
                                        false)).getMaximumNumberInnerIterations();
    say("Max Inner Iterations: " + n);
    sayOK();
    return n;
  }
  /**
   * Get the maximum physical time for the unsteady simulation, in seconds.
   *
   * @return Maximum Physical Time in seconds.
   */
  public double getSolverMaxPhysicalTimestep() {
    printAction("Getting Maximum Physical Timestep in seconds");
    PhysicalTimeStoppingCriterion stpCrit = (PhysicalTimeStoppingCriterion) getStoppingCriteria("Maximum Physical Time.*", false);
    Units unit = stpCrit.getMaximumTime().getUnits();
    double d = stpCrit.getMaximumTime().getValue() * unit.getConversion();
    say("Maximum Physical Time: %gs", d);
    sayOK();
    return d;
  }

  /**
   * Get the Physical Timestep for the unsteady simulation, in seconds.
   *
   * @return Physical Timestep in seconds.
   */
  public double getSolverPhysicalTimestep() {
    printAction("Getting Physical Timestep in seconds");
    ImplicitUnsteadySolver trn = ((ImplicitUnsteadySolver) getSolver(ImplicitUnsteadySolver.class));
    Units unit = trn.getTimeStep().getUnits();
    double d = trn.getTimeStep().getValue() * unit.getConversion();
    say("Timestep: %gs", d);
    sayOK();
    return d;
  }

  /**
   * Get the Physical Timestep definition for the unsteady simulation.
   *
   * @return Physical Timestep definition.
   */
  public String getSolverPhysicalTimestepDefinition() {
    printAction("Getting Physical Timestep definition");
    ImplicitUnsteadySolver trn = ((ImplicitUnsteadySolver) getSolver(ImplicitUnsteadySolver.class));
    String s = trn.getTimeStep().getDefinition();
    say("Timestep: %s", s);
    sayOK();
    return s;
  }

  /**
   * Returns if the <b>ABORT</b> file Stopping Criteria is satisfied.
   * @return True or False.
   */
  public boolean getStatus_AbortStoppingCriteria() {
    AbortFileStoppingCriterion stp = (AbortFileStoppingCriterion) getStoppingCriteria("Stop File.*", false);
    if (stp.getIsSatisfied() && stp.getIsUsed()) {
        say("ABORT Stopping Criteria satisfied...");
        return true;
    }
    return false;
  }

  /**
   * Returns the first match of a Stopping Criteria, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Stopping Criteria.
   */
  public SolverStoppingCriterion getStoppingCriteria(String regexPatt) {
    return getStoppingCriteria(regexPatt, true);
  }

  private SolverStoppingCriterion getStoppingCriteria(String regexPatt, boolean verboseOption) {
    return (SolverStoppingCriterion) getNamedObject(regexPatt,
            new ArrayList(sim.getSolverStoppingCriterionManager().getObjects()), verboseOption);
  }

  /**
   * Modifies a string in order to be used for filenames, i.e., eliminates
   * special characters (<i>= / #</i>, etc...). Spaces are replaced by underscores.
   *
   * @param base given base String.
   * @return modified String.
   */
  public String getStringForFilename(String base) {
    return base.replace(" ", "_").replace("=", "").replace("/", "").replace("#", "");
  }

  private String getStringForFilename(NamedObject n) {
    return getStringForFilename(n.getPresentationName());
  }

  /**
   * Get the local time.
   *
   * @return String with local time.
   */
  public String getTime() {
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
  }

  /**
   * Loop over all Units and returns the exact match.<p>
   * Note the search will be done in in FunctionName first, and then in its name on GUI,
   * i.e., the PresentationName.
   *
   * @param name given unit name.
   * @return Unit.
   */
  public Units getUnit(String name) {
    return getUnit(name, true);
  }

  private Units getUnit(String name, boolean verboseOption) {
    if (name.equals("")) {
        return ((Units) sim.getUnitsManager().getObject(""));
    }
    say(verboseOption, "Getting Unit by exact match: \"%s\"", name);
    for (Units u : sim.getUnitsManager().getObjects()) {
        if (u.getPresentationName().equals(name) || u.getDescription().equals(name)) {
            say("Got: " + u.getPresentationName(), verboseOption);
            return u;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  private int getVer() {
    //-- Return the version in a xyy integer format.
    double verD = Double.valueOf(getVersion().replaceAll("\\....$", ""));
    int ver = (int) (verD * 100);
    say("Version checker in Integer: " + ver);
    return ver;
  }

  private Vector3 getVector3(PatchVertex pv, CylindricalCoordinateSystem c) {
    return c.transformLabCoordinate(new Vector3(pv.getCoordinate().toDoubleArray()));
  }

  /**
   * Get the Vector Display Quantity from a Vector Displayer.
   *
   * @param d given Displayer.
   * @return The VectorDisplayQuantity.
   */
  public VectorDisplayQuantity getVectorDisplayQuantity(Displayer d) {
    return getVectorDisplayQuantity(d, true);
  }

  private VectorDisplayQuantity getVectorDisplayQuantity(Displayer d, boolean verboseOption) {
    if (isVector(d)) {
        return ((VectorDisplayer) d).getVectorDisplayQuantity();
    }
    say("There is no VectorDisplayQuantity in Displayer: " + d.getPresentationName(), verboseOption);
    return null;
  }

  private VectorProfile getVectorProfile(Boundary b, String name, boolean verboseOption) {
    return (VectorProfile) getProfile(b, name, verboseOption);
  }

  /**
   * Gets the STAR-CCM+ version.
   *
   * @return The code version in a x.yy.zzz string format.
   */
  public String getVersion() {
    return sim.getStarVersion().getString("ReleaseNumber");
  }

  /**
   * @return ViewfactorsCalculatorSolver.
   */
  private ViewfactorsCalculatorSolver getViewFactorCalcSolver() {
    return ((ViewfactorsCalculatorSolver) sim.getSolverManager().getSolver(ViewfactorsCalculatorSolver.class));
  }

  /**
   * Returns the first match of a Vis Transform, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Vis Transform.
   */
  public VisTransform getVisTransform(String regexPatt) {
    return getVisTransform(regexPatt, true);
  }

  private VisTransform getVisTransform(String regexPatt, boolean verboseOption) {
    return (VisTransform) getNamedObject(regexPatt,
            new ArrayList(sim.getTransformManager().getObjects()), verboseOption);
  }

  /**
   * Groups the Regions by Media (Fluid or Solid) within the GUI.
   */
  public void groupRegionsByMedia() {
    printAction("Grouping Regions by Media: Fluid or Solid");
    ArrayList<Region> ar1 = new ArrayList<Region>();
    ArrayList<Region> ar2 = new ArrayList<Region>();
    for (Region r : getRegions(".*", false)) {
        if (isFluid(r)) {
            ar1.add(r);
        } else {
            ar2.add(r);
        }
    }
    say("Fluid Regions: " + ar1.size());
    say("Solid Regions: " + ar2.size());
    sim.getRegionManager().getGroupsManager().groupObjects("Fluid Regions", ar1, false);
    sim.getRegionManager().getGroupsManager().groupObjects("Solid Regions", ar2, true);
    sayOK();
  }

  /**
   * Saves a picture from the Scene. This method will use the current Scene size.
   * <p>The picture will be saved on {@see #simPath} folder.
   *
   * @param scn given Scene.
   * @param picName given picture name. File extension is optional. If it does not find any,
   * a PNG will be saved.
   */
  public void hardCopyPicture(Scene scn, String picName) {
    hardCopyPicture(scn, picName, 0, 0);
  }

  /**
   * Saves a picture from the Scene with a given resolution.
   * <p>The picture will be saved on {@see #simPath} folder.
   *
   * @param scn given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picName given picture name. File extension is optional. If it does not find any,
   * a PNG will be saved.
   */
  public void hardCopyPicture(Scene scn, String picName, int resx, int resy) {
    String ext = retFilenameExtension(picName);
    if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
        say("File extension provided: " + ext.toUpperCase());
    } else {
        say("File extension not provided. Using PNG.");
        picName = picName + ".png";
    }
    if (picName.contains(simPath)) {
        hardCopyPicture(scn, new File(picName), resx, resy, true);
    } else {
        hardCopyPicture(scn, new File(simPath, picName), resx, resy, true);
    }
  }

  /**
   * Saves a picture from the Scene with a given resolution.
   *
   * @param scn given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picFile given picture as a {@see java.io.File}.
   * @param verboseOption Print to standard output?
   */
  public void hardCopyPicture(Scene scn, File picFile, int resx, int resy, boolean verboseOption) {
    hardCopyPicture(scn, picFile, resx, resy, 1, verboseOption);
  }

  private void hardCopyPicture(Scene scn, File picFile, int resx, int resy, int mag, boolean verboseOption) {
    printAction("Saving a picture", verboseOption);
    if (scn == null) {
        say("Skipping NULL...");
    }
    if (resx == 0 || resy == 0) {
        try {
            resx = scn.getSize()[0];
            resy = scn.getSize()[1];
        } catch (Exception e) {
            resx = 1000;
            resy = 800;
        }
    }
    String picFolder = picFile.getParent();
    if (!new File(picFolder).isDirectory()) {
        say("Pic Folder does not exist: " + picFolder);
        new File(picFolder).mkdir();
        say("Pic Folder created.");
    }
    say("Scene: " + scn.getPresentationName(), verboseOption);
    scn.printAndWait(picFile, mag, resx, resy);
    say("Saved: " + picFile.getName(), verboseOption);
    sayOK(verboseOption);
    }

  /**
   * Saves a picture from the Plot with a given resolution.
   * <p>The picture will be saved on {@see #simPath} folder.
   *
   * @param plot given StarPlot.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picName given picture name. File extension is optional. If it does not find any,
   * a PNG will be saved.
   */
  public void hardCopyPicture(StarPlot plot, String picName, int resx, int resy) {
    String ext = retFilenameExtension(picName);
    if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
        say("File extension provided: " + ext.toUpperCase());
    } else {
        say("File extension not provided. Using PNG.");
        picName = picName + ".png";
        ext = "png";
    }
    hardCopyPicture(plot, new File(simPath, picName), resx, resy, true);
  }

  /**
   * Saves a picture from the Plot with a given resolution.
   *
   * @param plot given StarPlot.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picFile given picture as a {@see java.io.File}.
   */
  public void hardCopyPicture(StarPlot plot, File picFile, int resx, int resy) {
    hardCopyPicture(plot, picFile, resx, resy, true);
  }

  private void hardCopyPicture(StarPlot plot, File picFile, int resx, int resy, boolean verboseOption) {
    printAction("Saving a picture", verboseOption);
    say("Plot: " + plot.getPresentationName(), verboseOption);
    plot.encode(picFile.toString(), resx, resy);
    say("Saved: " + picFile.getName(), verboseOption);
    sayOK(verboseOption);
  }

  /**
   * This method will loop through all Plots and Scenes and will print them to PNG files. The
   * pictures will be based on {@see #simTitle} variable and resolution will be {@see #defPicResX}
   * x {@see #defPicResY}.
   */
  public void hardCopyPictures() {
    hardCopyPictures(defPicResX, defPicResY, "", "");
  }

  /**
   * This method will loop through all Plots and Scenes and will print them to PNG files. The
   * pictures will be based on {@see #simTitle} variable.
   *
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   */
  public void hardCopyPictures(int resx, int resy) {
    hardCopyPictures(resx, resy, "");
  }

  /**
   * This method will loop through all Plots and Scenes and will print them to PNG files. The
   * pictures will be based on {@see #simTitle} variable.
   *
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param preffix given preffix for the filenames.
   */
  public void hardCopyPictures(int resx, int resy, String preffix) {
    hardCopyPictures(resx, resy, preffix, "");
  }

  /**
   * This method will loop through all Plots and Scenes and will print them to PNG files. The
   * pictures will be based on {@see #simTitle} variable.
   *
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param preffix given preffix for the filenames.
   * @param suffix given suffix for the filenames.
   */
  public void hardCopyPictures(int resx, int resy, String preffix, String suffix) {
    printAction("Saving Pictures from Plots and Scenes");
    say("Looping through Plots...");
    String name;
    for (StarPlot plt : getPlots(".*", false)) {
        name = preffix + getStringForFilename(plt.getPresentationName()) + suffix +  ".png";
        say("   Saving picture: %s...", name);
        hardCopyPicture(plt, new File(simPath, name), resx, resy, false);
    }
    say("Looping through Scenes...");
    for (Scene scn : getScenes(".*", false)) {
        name = preffix + getStringForFilename(scn.getPresentationName()) + suffix + ".png";
        say("   Saving picture: %s...", name);
        hardCopyPicture(scn, new File(simPath, name), resx, resy, 1, false);
    }
    sayOK();
  }

  /**
   * Hard copy pictures on Saved Scenes upon initialization. Most likely at iter = 0.
   * <p>
   * Basic condition is that solution must not be initialized. This method will do it automatically.
   */
  public void hardCopyPicturesAtInitialization() {
    if (!isUnsteady()) {
        return;
    }
    printAction("Hardcopying pictures at iteration 0");
    if (sim.getSolution().isInitialized() || getIteration() > 0) {
        if (getIteration() > 0) {
            say("Current Iteration: " + getIteration());
        }
        say("Solution is already initialized. Skipping...");
        return;
    }
    initializeSolution(false);
    for (Scene scn : getScenes(".*", false)) {
        if (!isSaving(scn)) {
            continue;
        }
        SceneUpdate su = getSceneUpdate(scn);
        String curDir = su.getAnimationFilePath().toString();
        if (curDir.equals("")) {
            curDir = simPath;
        } else if (!curDir.contains(simPath)) {
            curDir = new File(simPath, curDir).toString();
        }
        File picDir = new File(curDir);
        if (!picDir.exists()) {
            picDir.mkdir();
        }
        File picFile = new File(curDir, getStringForFilename(scn.getPresentationName() + "_iter0.png"));
        say("Saving Scene: " + scn.getPresentationName());
        hardCopyPicture(scn, picFile, su.getXResolution(), su.getYResolution(), false);
        say("Saved to: " + picFile.toString());
    }
    sayOK();
  }

  private boolean hasBeanDisplayName(NamedObject no, String s) {
    if (no.getBeanDisplayName().equals(s)) {
        return true;
    }
    return false;
  }

  private boolean hasClassName(NamedObject no, String s) {
    if (no.getClass().getName().equals(s)) {
        return true;
    }
    return false;
  }

  /**
   * Is the Simulation solving for Coupled Explicit Flow?
   *
   * @return True or False.
   */
  public boolean hasCoupledExplicitFlow() {
    return isBoolean(sim.getSolverManager().has("Coupled Explicit"));
  }

  /**
   * Is the Simulation solving for Coupled Implicit Flow?
   *
   * @return True or False.
   */
  public boolean hasCoupledImplicitFlow() {
    return isBoolean(sim.getSolverManager().has("Coupled Implicit"));
  }

  /**
   * Does the Region have Deleted Cells due Remove Invalid Cells?
   *
   * @param r given Region.
   * @return True or False.
   */
  public boolean hasDeletedCells(Region r) {
    return hasDeletedCells(r, true);
  }

  private boolean hasDeletedCells(Region r, boolean verboseOption) {
    sayRegion(r, verboseOption);
    say("Has came from Remove Invalid Cells command?", verboseOption);
    if (r.getPresentationName().matches("^Cells deleted.*")) {
        sayAnswerYes(verboseOption);
        return true;
    }
    sayAnswerNo(verboseOption);
    return false;
  }

  /**
   * Does the simulation have an Eulerian Multiphase (EMP) or Volume of Fluid model running?
   *
   * @return True or False.
   */
  public boolean hasEMP() {
    return isBoolean(sim.getSolverManager().has("Multiphase Segregated Flow"));
  }

  /**
   * Is the Simulation solving for Energy?
   *
   * @return True or False.
   */
  public boolean hasEnergy() {
    for (Monitor m : ((ResidualPlot) getPlot("Residuals", false)).getObjects()) {
        if (m.getPresentationName().equals("Energy")) {
            return true;
        }
    }
    return false;
  }

  /**
   * Does the current Simulation have Geometry Parts?
   *
   * @return True or False.
   */
  public boolean hasGeometryParts() {
    return hasGeometryParts(true);
  }

  private boolean hasGeometryParts(boolean verboseOption) {
    saySimName(verboseOption);
    say("Has Geometry Parts?", verboseOption);
    if (sim.getGeometryPartManager().isEmpty()) {
        sayAnswerNo(verboseOption);
        return false;
    }
    sayAnswerYes(verboseOption);
    return true;
  }

  private boolean hasGenCylModel(Continuum continua) {
    return isBoolean(continua.getModelManager().has("Generalized Cylinder"));
  }

  private boolean hasModel(PhysicsContinuum pc, String model) {
    if (pc.getModelManager().has(model)) {
        return true;
    }
    return false;
  }

  private boolean hasMesher(AutoMeshOperation amo, String mesherName) {
    return isBoolean(amo.getMeshers().has(mesherName));
  }

  private boolean hasMeshSettings(NamedObject no, boolean verboseOption) {
    if (!(isMeshContinua(no) || isMeshOperation(no))) {
        say("Not a Mesh Continua or Operation.");
        return false;
    }
    if (isMeshOperation(no) && !(isAutoMeshOperation(no) || isSurfaceWrapperOperation(no))) {
        say("This Mesh Operation can not have Custom Controls.", verboseOption);
        return false;
    }
    return true;
  }

  private boolean hasParentBeanDisplayName(NamedObject no, String s) {
    if (no.getParent().getBeanDisplayName().equals(s)) {
        return true;
    }
    return false;
  }

  /**
   * Does the Region have a Polyhedral mesh?
   *
   * @param r given Region.
   * @return True or False.
   */
  public boolean hasPolyMesh(Region r) {
    if (hasDeletedCells(r, false) ) { return false; }
    if (r.getMeshContinuum() == null) { return false; }
    if (isPoly(r.getMeshContinuum())) {
        sayRegion(r);
        say(" Has Poly mesh.");
        return true;
    }
    return false;
  }

  private boolean hasPolyMesher(Continuum continua) {
    return isBoolean(continua.getModelManager().has("Polyhedral Mesher"));
  }

  private boolean hasPolyMesher(AutoMeshOperation amo) {
    return hasMesher(amo, "Polyhedral Mesher");
  }

  private boolean hasPrismLayerMesher(Continuum continua) {
    return isBoolean(continua.getModelManager().has("Prism Layer Mesher"));
  }

  private boolean hasPrismLayerMesher(AutoMeshOperation amo) {
    return hasMesher(amo, "Prism Layer Mesher");
  }

  /**
   * Does the Boundary have Radiation Profile?
   *
   * @param b given Boundary.
   * @return True or False.
   */
  public boolean hasRadiationBC(Boundary b) {
    return isBoolean(b.getValues().has("Surface Emissivity"));
  }

  /**
   * Is the Simulation solving for Segregated Energy?
   *
   * @return True or False.
   */
  public boolean hasSegregatedEnergy() {
    return isBoolean(sim.getSolverManager().has("Segregated Energy"));
  }

  /**
   * Is the Simulation solving for Segregated Flow?
   *
   * @return True or False.
   */
  public boolean hasSegregatedFlow() {
    return isBoolean(sim.getSolverManager().has("Segregated Flow"));
  }

  /**
   * Has the Simulation Stopped?
   *
   * @return True or False.
   */
  public boolean hasStopped() {
    if (getStatus_AbortStoppingCriteria() || stopMacroUtils) {
        say("Simulation has stopped...");
        return true;
    }
    return false;
  }

  /**
   * Has the Simulation a Solution, i.e., Fields were Initialized?
   *
   * @return True or False.
   */
  public boolean hasSolution() {
    if (sim.getSolution().isInitialized() || getIteration() > 0) {
        return true;
    }
    return false;
  }

  /**
   * Does the Region have a Trimmer mesh?
   *
   * @param r given Region.
   * @return True or False.
   */
  public boolean hasTrimmerMesh(Region r) {
    if (hasDeletedCells(r)) { return false; }
    if (r.getMeshContinuum() == null) { return false; }
    if (isTrimmer(r.getMeshContinuum())) {
        sayRegion(r);
        say(" Has Trimmer mesh.");
        return true;
    }
    return false;
  }

  private boolean hasTrimmerMesher(AutoMeshOperation amo) {
    return hasMesher(amo, "Trimmed Cell Mesher");
  }

  /**
   * Does the Simulation have a Valid Surface Mesh?
   *
   * @return True or False.
   */
   public boolean hasValidSurfaceMesh() {
    if (queryRemeshedSurface() == null) {
        return false;
    }
    return true;
  }

  /**
   * Does the Simulation have a Valid Volume Mesh?
   *
   * @return True or False.
   */
  public boolean hasValidVolumeMesh() {
    if (queryVolumeMesh() == null) {
        return false;
    }
    return true;
  }

  /**
   *
   * @return
   */
  public boolean hasValidWrappedMesh() {
    if (queryWrappedSurface() == null) {
        return false;
    }
    return true;
  }

  /**
   * Does the simulation have a View Factors?
   *
   * @return True or False.
   */
  public boolean hasViewFactors() {
    return isBoolean(sim.getSolverManager().has("View Factors Calculator"));
  }

  /**
   * Does the simulation have a VOF model running?
   *
   * @return True or False.
   */
  public boolean hasVOF() {
    return isBoolean(sim.getSolverManager().has("Segregated VOF"));
  }

    /**
     *
     * @param subDirName
     */
    public void importAllDBSFromSubDirName(String subDirName) {
    printAction("Importing the DBS files.");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("From Path: " + dbsSubPath.toString());
    String[] fileNames = dbsSubPath.list(dbsFilter);
    ArrayList<String> filesVect = new ArrayList<String>();
    File dbsFile = null;
    for (String fileName : fileNames) {
        dbsFile = new File(dbsSubPath, fileName);
        filesVect.add(dbsFile.toString());
    }
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    prtImpMngr.importDbsParts(filesVect, "OneSurfacePerPatch", true, "OnePartPerFile", false, unit_m, 1);
  }

  /**
   * Imports a CAD file using the default Tesselation option. See {@see #defTessOpt}. It assumes
   * the file is inside {@see #simPath}. Informing the Path might be necessary.
   *
   * @param part given CAD file with extension. E.g.: "CAD\\machine.prt"
   */
  public void importCADPart(String part) {
    importCADPart(new File(simPath, part), true);
  }

  /**
   * Imports a CAD file using the default Tesselation option. See {@see #defTessOpt}.
   *
   * @param cadFile given CAD in {@see java.io.File} format.
   */
  public void importCADPart(File cadFile) {
    importCADPart(cadFile, true);
  }

  private void importCADPart(File cadFile, boolean verboseOption) {
    printAction("Importing CAD Part", verboseOption);
    say("File: " + cadFile.toString(), verboseOption);
    if (!cadFile.exists()) {
        say("File not found!", verboseOption);
    }
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    prtImpMngr.importCadPart(cadFile.toString(), "SharpEdges", mshSharpEdgeAngle, defTessOpt, false, false);
    sayOK(verboseOption);
  }

  private Region importMeshAndData(String file, boolean verboseOption) {
    printAction("Importing Mesh and Data", verboseOption);
    ArrayList<Region> alr = new ArrayList<Region>();
    alr.addAll(getRegions(".*", false));

    ImportManager im = sim.getImportManager();
    say("Importing: " + file, verboseOption);
    im.importMeshFiles(file);

    Region srcReg = null;
    for (Region r : getRegions(".*", false)) {
        if (alr.contains(r)) {
            continue;
        }
        srcReg = r;
        say("New Region Imported: " + srcReg.getPresentationName(), verboseOption);
    }
    if (srcReg == null) {
        say("New Region Not found...", verboseOption);
        return null;
    }

    return srcReg;
  }


  /**
   * Get All CAD Parts in the model and Imprint them using the CAD method.
   */
  public void imprintAllPartsByCADMethod() {
    printAction("Imprinting All Parts by CAD Method");
    //- Gerou Free Edges
    //sim.get(MeshActionManager.class).imprintCadParts(getAllLeafPartsAsMeshParts(), "CAD");
    Object[] arrayObj = getAllLeafPartsAsCadParts().toArray();
    sim.get(MeshActionManager.class).imprintCadParts(new NeoObjectVector(arrayObj), "CAD");
    sayOK();
  }

    /**
     *
     * @param tolerance
     */
    public void imprintAllPartsByDiscreteMethod(double tolerance) {
    printAction("Imprinting All Parts by Discrete Method");
    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafPartsAsMeshParts(), "Discrete", tolerance);
    sayOK();
  }

    /**
     *
     * @param am
     */
    public void imprintPartsByCADMethod(ArrayList<MeshPart> am) {
    printAction("Imprinting Parts by CAD Method");
    say("Parts: ");
    for (MeshPart mp : am) {
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintCadParts(am, "CAD");
//    sim.get(MeshActionManager.class).imprintDiscreteParts(am, "CAD", 1.0E-4);
    sayOK();
  }

    /**
     *
     * @param am
     * @param tolerance
     */
    public void imprintPartsByDiscreteMethod(ArrayList<MeshPart> am, double tolerance) {
    printAction("Imprinting Parts by Discrete Method");
    say("Parts: ");
    for (MeshPart mp : am) {
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintDiscreteParts(am, "Discrete", tolerance);
    sayOK();
  }

  /**
   * Initialize Meshing.
   */
  public void initializeMeshing() {
    printAction("Initializing Mesh Pipeline");
    MeshPipelineController mpc = sim.get(MeshPipelineController.class);
    mpc.initializeMeshPipeline();
    sayOK();
  }

  /**
   * Initialize Solution.
   */
  public void initializeSolution() {
    initializeSolution(true);
  }

  /**
   * @param verboseOption
   */
  private void initializeSolution(boolean verboseOption) {
    printAction("Initializing Solution", verboseOption);
    sim.getSolution().initializeSolution();
    sayOK(verboseOption);
  }

  /**
   * Intersects Two Objects by Discrete Mode.
   *
   * Usually, the Object is a Geometry Part.
   *
   * @param obj1 object 1.
   * @param obj2 object 2.
   * @param renameTo the returned Leaf Mesh Part will be renamed to this.
   * @return A brand new Leaf Mesh Part.
   */
  public LeafMeshPart intersect2PartsByDiscrete(Object obj1, Object obj2, String renameTo) {
    printAction("Intersecting 2 Parts (obj1 'intersection' obj2)");
    say("Object 1: " + obj1.toString());
    say("Object 2: " + obj2.toString());
    MeshActionManager mshActMngr = sim.get(MeshActionManager.class);
    MeshPart mp = mshActMngr.intersectParts(new NeoObjectVector(new Object[] {obj1, obj2}), "Discrete");
    mp.setPresentationName(renameTo);
    say("Returning: " + mp.getPathInHierarchy());
    return (LeafMeshPart) mp;
  }

  private boolean isAutoMeshOperation(NamedObject no) {
    return hasClassName(no, "star.meshing.AutoMeshOperation");
  }

  /** Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isBlockPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Block Part");
  }

  private boolean isBoolean(boolean b) {
    if (b) {
        return true;
    }
    return false;
  }

  /** Is this Geometry Part a CAD Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCadPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "CAD Part");
  }

  /** Is this Geometry Part a Composite Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCompositePart(GeometryPart gp) {
    return isCompositePart(gp, true);
  }

  private boolean isCompositePart(GeometryPart gp, boolean verboseOption) {
    boolean isCP = hasBeanDisplayName(gp, "Composite Part");
    if (isCP)
        say("Geometry Part is a Composite Part", verboseOption);
    return isCP;
  }

  private boolean isCylindricalCSYS(CoordinateSystem c) {
    return hasClassName(c, "star.common.CylindricalCoordinateSystem");
  }

  private boolean isDirectedMeshable(PartSurface src, PartSurface tgt, boolean verboseOption) {
    if (src.getPart() != tgt.getPart()) {
        sayPart(tgt.getPart(), verboseOption);
        say("Source and Target Part Surfaces must be on the same Part. Skipping...", verboseOption);
        return false;
    }
    if (!( isCadPart(src.getPart()) || isSolidModelPart(src.getPart()))) {
        say("Limited to CAD Parts only. Skipping...", verboseOption);
        return false;
    }
    return true;
  }

  /**
   * Is this an Explicit Unsteady simulation?
   *
   * @return True or False.
   */
  public boolean isExplicitUnsteady() {
    if (sim.getSolverManager().has("Explicit Unsteady")) {
        return true;
    }
    return false;
  }

  /**
   * Does the file exist in the {@see #simPath} folder?
   *
   * @param filename given filename.
   * @return True if it is a file. False it is not.
   */
  public boolean isFile(String filename) {
    if (new File(simPath, filename).exists()) return true;
    return false;
  }

  /**
   * Does this Boundary belong a Direct Boundary Interface?
   *
   * @param b
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Boundary b) {
    return hasBeanDisplayName(b, "DirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Direct Boundary Interface?
   *
   * @param i given interface pair.
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Interface i) {
    return hasBeanDisplayName(i, "Direct Boundary Interface");
  }

  /**
   * Does this Boundary belongs to Fluid Region?
   *
   * @param b given boundary.
   * @return True or False.
   */
  public boolean isFluid(Boundary b) {
    return isFluid(b.getRegion());
  }

  /**
   * Is this a Fluid Region?
   *
   * @param r given Region.
   * @return True or False.
   */
  public boolean isFluid(Region r) {
    if (r.getRegionType().toString().equals("Fluid Region")) return true;
    return false;
  }

  /**
   * Is this a Fluid-Solid (Region-Region) Interface?
   *
   * @param i given interface pair.
   * @return True or False.
   */
  public boolean isFluidSolidInterface(Interface i) {
    Region reg0 = i.getRegion0();
    Region reg1 = i.getRegion1();
    if (isFluid(reg0) && isSolid(reg1)) return true;
    if (isFluid(reg1) && isSolid(reg0)) return true;
    return false;
  }

  /**
   * Is this a Histogram Plot?
   *
   * @param p given Plot.
   * @return True or False.
   */
  public boolean isHistogramPlot(StarPlot p) {
    return hasClassName(p, "star.common.HistogramPlot");
  }

  /**
   * Is this an Implicit Unsteady simulation?
   *
   * @return True or False.
   */
  public boolean isImplicitUnsteady() {
    boolean hasImpUnst = sim.getSolverManager().has("Implicit Unsteady");
    return hasImpUnst;
  }

  /**
   * Is this an Indirect Boundary Interface?
   *
   * @param b given boundary.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Boundary b) {
    return hasBeanDisplayName(b, "IndirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Mapped Indirect Interface?
   *
   * @param i given interface pair.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Interface i) {
    return hasBeanDisplayName(i, "Indirect Boundary Interface");
  }

  /**
   * Is this an Interface Boundary?
   *
   * @param b given boundary.
   * @return True or False.
   */
  public boolean isInterface(Boundary b) {
    if (isDirectBoundaryInterface(b) || isIndirectBoundaryInterface(b)) return true;
    return false;
  }

  /**
   * Is this an Interface Boundary shared by the given Regions, irrespective their order?
   *
   * @param b given boundary.
   * @param reg1 given Region 1.
   * @param reg2 given Region 2.
   * @return True or False.
   */
  public boolean isInterface(Boundary b, Region reg1, Region reg2) {
    Region r1 = null, r2 = null;
    boolean isInt = false;
    if (isDirectBoundaryInterface(b)) {
        DirectBoundaryInterfaceBoundary dbib = (DirectBoundaryInterfaceBoundary) b;
        r1 = dbib.getBoundaryInterface().getRegion0();
        r2 = dbib.getBoundaryInterface().getRegion1();
    }
    if (isIndirectBoundaryInterface(b)) {
        IndirectBoundaryInterfaceBoundary ibib = (IndirectBoundaryInterfaceBoundary) b;
        r1 = ibib.getBoundaryInterface().getRegion0();
        r2 = ibib.getBoundaryInterface().getRegion1();
    }
    if (r1 == reg1 && r2 == reg2 || r1 == reg2 && r2 == reg1) isInt = true;
    return isInt;
  }

  /**
   * Is this Geometry Part a Leaf Mesh Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isLeafMeshPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Leaf Mesh Part");
  }

  private boolean isLinux() {
    return (OS.indexOf("nux") >= 0);
  }

  /**
   * Is this a Mesh Continua?
   *
   * @param no given NamedObject.
   * @return True or False.
   */
  public boolean isMeshContinua(NamedObject no) {
    return hasClassName(no, "star.common.MeshContinuum");
  }

  /**
   * Does this Boundary belong to a meshing Region?
   *
   * @param b given boundary.
   * @return True or False.
   */
  public boolean isMeshing(Boundary b) {
    return isMeshing(b, false);
  }

  private boolean isMeshing(Boundary b, boolean verboseOption) {
    sayBdry(b, verboseOption);
    if (b.getRegion().isMeshing()) return true;
    say("Region not meshing. Skipping...\n", verboseOption);
    return false;
  }

  /**
   * Is this a Mesh Operation?
   *
   * @param no given NamedObject.
   * @return True or False.
   */
  public boolean isMeshOperation(NamedObject no) {
    if (no.getClass().getName().matches("star.meshing.*Operation")) {
        return true;
    }
    return false;
  }

  /**
   * Is this Geometry Part a Mesh Operation Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isMeshOperationPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Mesh Operation Part");
  }

  private boolean isMonitor(NamedObject no) {
    return hasParentBeanDisplayName(no, "Monitors");
  }

  /**
   * Is this a Monitor Plot?
   *
   * @param p given Plot.
   * @return True or False.
   */
  public boolean isMonitorPlot(StarPlot p) {
    return hasClassName(p, "star.common.MonitorPlot");
  }

  /**
   * Is the Scene open?
   *
   * @param scn given Scene.
   * @return True or False.
   */
  public boolean isOpen(Scene scn) {
    if (scn.isShowing()) return true;
    return false;
  }

  /**
   * Is this a Part Displayer?
   *
   * @param d given Displayer.
   * @return True or False.
   */
  public boolean isPart(Displayer d) {
    return hasClassName(d, "star.vis.PartDisplayer");
  }

  /**
   * Is this a Physics Continua?
   *
   * @param no given NamedObject.
   * @return True or False.
   */
  public boolean isPhysicsContinua(NamedObject no) {
    return hasClassName(no, "star.common.PhysicsContinuum");
  }

  /**
   * Is this a Physics Continua?
   *
   * @param continua given Continua.
   * @return True or False.
   * @deprecated in v3.2. Use the other one.
   */
  public boolean isPhysics(Continuum continua) {
    return isPhysicsContinua(continua);
  }

  /**
   * Is this a Poly Mesh Continua?
   *
   * @param continua given Continua.
   * @return True or False.
   */
  public boolean isPoly(Continuum continua) {
    if (continua.getEnabledModels().containsKey("star.dualmesher.DualMesherModel")) return true;
    return false;
  }

  /**
   * Is this a Position Field Function?
   *
   * @param ff
   * @return True or False.
   */
  public boolean isPosition(FieldFunction ff) {
    if (ff.getType().toString().equals("Position")) return true;
    return false;
  }

  /**
   * Has this Mesh Continua a Remesher Model?
   *
   * @param continua given Continua.
   * @return True or False.
   */
  public boolean isRemesh(Continuum continua) {
    if (continua.getEnabledModels().containsKey("star.resurfacer.ResurfacerMeshingModel")) return true;
    return false;
  }

  /**
   * Is this a Report Monitor?
   *
   * @param m given Monitor.
   * @return True or False.
   */
  public boolean isReport(Monitor m) {
    return hasClassName(m, "star.base.report.ReportMonitor");
  }

  /**
   * Is this a Residual Monitor?
   *
   * @param m given Monitor.
   * @return True or False.
   */
  public boolean isResidual(Monitor m) {
    return hasClassName(m, "star.common.ResidualMonitor");
  }

  /**
   * Is this a Residual Plot?
   *
   * @param p given Plot.
   * @return True or False.
   */
  public boolean isResidual(StarPlot p) {
    return hasClassName(p, "star.common.ResidualPlot");
  }

  /**
   * Is this Scene saving files for Animation?
   *
   * @param scn given Scene.
   * @return True or False.
   */
  public boolean isSaving(Scene scn) {
    return getSceneUpdate(scn).getSaveAnimation();
  }

  /**
   * Is this Scene saving files for Animation?
   *
   * @param sp given Plot.
   * @return True or False.
   */
  public boolean isSaving(StarPlot sp) {
    return getPlotUpdate(sp).getSaveAnimation();
  }

  /**
   * Is this a Scalar Displayer?
   *
   * @param d given Displayer.
   * @return True or False.
   */
  public boolean isScalar(Displayer d) {
    return hasClassName(d, "star.vis.ScalarDisplayer");
  }

  /**
   * Is this a Scalar Field Function?
   *
   * @param ff given Field Function.
   * @return True or False.
   */
  public boolean isScalar(FieldFunction ff) {
    if (ff.getType().toString().equals("Scalar")) return true;
    return false;
  }

  private boolean isScene(NamedObject no) {
    return hasBeanDisplayName(no, "Scene");
  }

  /**
   * Is this filename a sim file in the current {@see #simPath}?
   *
   * @param filename given file name. Extension is optional.
   * @return True or False.
   */
  public boolean isSimFile(String filename) {
    File f = new File(simPath, filename.replace(".sim", "") + ".sim");
    if (f.exists()) {
        return true;
    }
    return false;
  }

    /**
   * Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleBlockPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Block Part");
  }

  /** Is this Geometry Part a Simple Cylinder Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleCylinderPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Cylinder Part");
  }

  /** Is this Geometry Part a Solid Model Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleSolidModelPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Solid Model Part");
  }

  /**
   * Does this Boundary belong to a Solid Region?
   *
   * @param b given Boundary.
   * @return True or False.
   */
  public boolean isSolid(Boundary b) {
    return isSolid(b.getRegion());
  }

  /**
   * Is this a Solid Region?
   *
   * @param r given Region.
   * @return True or False.
   */
  public boolean isSolid(Region r) {
    if (r.getRegionType().toString().equals("Solid Region")) return true;
    return false;
  }

  /** Is this Geometry Part a Solid Model Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSolidModelPart(GeometryPart gp) {
    return hasBeanDisplayName(gp, "Solid Model Part");
  }

  private boolean isSolutionHistory(NamedObject no) {
    return hasParentBeanDisplayName(no, "Solution Histories");
  }

  /**
   * Is this a Streamline Displayer?
   *
   * @param d given Displayer.
   * @return True or False.
   */
  public boolean isStreamline(Displayer d) {
    return hasClassName(d, "star.vis.StreamDisplayer");
  }

  private boolean isSurfaceWrapperOperation(NamedObject no) {
    return hasBeanDisplayName(no, "Surface Wrapper Operation");
  }

  private boolean isStarPlot(NamedObject no) {
    return hasParentBeanDisplayName(no, "Plots");
  }

  /**
   * Is this a Steady State simulation?
   *
   * @return True or False.
   */
  public boolean isSteadyState() {
    return !isUnsteady();
  }

  /**
   * Is this a Steady State Continua?
   *
   * @return True or False.
   */
  public boolean isSteady(Continuum continua) {
    if (continua.getModelManager().has("Steady")) {
        return true;
    }
    return false;
  }
  /**
   * Is this a Trimmer Mesh Continua?
   *
   * @param continua given Continua.
   * @return True or False.
   */
  public boolean isTrimmer(Continuum continua) {
    if (continua.getEnabledModels().containsKey("star.trimmer.TrimmerMeshingModel")) return true;
    return false;
  }

  /**
   * Is this an Unsteady simulation?
   *
   * @return True or False.
   */
  public boolean isUnsteady() {
    return isImplicitUnsteady() || isExplicitUnsteady();
  }

  /**
   * Is this an Unsteady Continua?
   *
   * @return True or False.
   */
  public boolean isUnsteady(Continuum continua) {
    return !isSteady(continua);
  }

  /**
   * Is this a Vector Displayer?
   *
   * @param d given Displayer.
   * @return True or False.
   */
  public boolean isVector(Displayer d) {
    return hasClassName(d, "star.vis.VectorDisplayer");
  }

  /**
   * Is this a Vector Field Function?
   *
   * @param ff
   * @return True or False.
   */
  public boolean isVector(FieldFunction ff) {
    if (ff.getType().toString().equals("Vector")) return true;
    return false;
  }

  /**
   * Is this a XY Plot?
   *
   * @param p given Plot.
   * @return True or False.
   */
  public boolean isXYPlot(StarPlot p) {
    return hasClassName(p, "star.common.XYPlot");
  }

  /**
   * Is this a Wrapper Mesh Continua?
   *
   * @param continua given Mesh Continua.
   * @return True or False.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public boolean isWrapper(MeshContinuum continua) {
    if (continua.getEnabledModels().containsKey("star.surfacewrapper.SurfaceWrapperMeshingModel"))
        return true;
    return false;
  }

  /**
   * Is this a Wall Boundary?
   *
   * @param b given boundary.
   * @return True or False.
   */
  public boolean isWall(Boundary b) {
    String t1 = "Wall";
    String t2 = "Contact Interface Boundary";
    String type = b.getBoundaryType().toString();
    if (type.equals(t1) || type.equals(t2)) return true;
    return false;
  }

  private boolean isWindows() {
    return (OS.indexOf("win") >= 0);
  }

  private void mapData_Volume(Region src, Region tgt, FieldFunction f, boolean mapByCell, boolean verboseOption) {
    printAction("Mapping Volume Data");
    say("From Region: " + src.getPresentationName(), verboseOption);
    say("To Region: " + tgt.getPresentationName(), verboseOption);
    sayFieldFunction(f, verboseOption);
    VolumeDataMapper vdm = sim.get(DataMapperManager.class).createMapper(VolumeDataMapper.class, "Volume Data Mapper");
    vdm.setPresentationName("Volume Data Mapper from " + src.getPresentationName());
    vdm.getSourceParts().setObjects(src);
    vdm.setUpdateAvailableFields(true);
    vdm.setScalarFieldFunctions(new NeoObjectVector(new Object[] {f}));
    VolumeTargetSpecification vts = ((VolumeTargetSpecification) vdm.getTargetSpecificationManager().getObject("Volume 1"));
    vts.getTargetParts().setObjects(tgt);
    if (mapByCell) {
        vts.setTargetStencil(2);
    }
    vdm.setConservationCorrection(false);
    //vts.setMappedValueLimiter(2);
    //vts.setInterpolationMethod(0);
    vdm.mapData();
    sayOK(verboseOption);
  }

  /**
   * Creates an Unite Mesh Operation between a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @return The Mesh Operation Part.
   */
  public MeshOperationPart meshOperationUniteParts(ArrayList<GeometryPart> ag) {
    return meshOperation("Unite", ag, null, true);
  }

  /**
   * Creates a Subtraction Mesh Operation between a set of Geometry Parts.
   *
   * @param ag given ArrayList of Geometry Parts.
   * @param tgtGP given target  Geometry Part.
   * @return The Mesh Operation Part.
   */
  public MeshOperationPart meshOperationSubtractParts(ArrayList<GeometryPart> ag, GeometryPart tgtGP) {
    return meshOperation("Subtract", ag, tgtGP, true);
  }

  private MeshOperationPart meshOperation(String type, ArrayList<GeometryPart> ag, GeometryPart tgtGP, boolean verboseOption) {
    printAction("Creating a Mesh Operation: " + type, verboseOption);
    sayParts(ag, verboseOption);
    String opName = null;
    if (type.equals("Subtract")) {
        SubtractPartsOperation spo = (SubtractPartsOperation) sim.get(MeshOperationManager.class).createSubtractPartsOperation();
        spo.getInputGeometryObjects().setObjects(ag);
        spo.setTargetPart((MeshPart) tgtGP);
        spo.execute();
        //say(tgtGP.getBeanDisplayName());
        //say(spo.getChildren().toString());
        opName = retStringBetweenBrackets(spo.getOutputPartNames());
        //say(spo.getOutputParts().getChildren().toString());
        //say(spo.getChildren().iterator().next());
    } else if (type.equals("Unite")) {
        UnitePartsOperation upo = (UnitePartsOperation) sim.get(MeshOperationManager.class).createUnitePartsOperation();
        upo.getInputGeometryObjects().setObjects(ag);
        upo.execute();
        opName = retStringBetweenBrackets(upo.getOutputPartNames());
    }
    return ((MeshOperationPart) sim.get(SimulationPartManager.class).getPart(opName));
  }

  /**
   * Opens all Plots in the Simulation.
   */
  public void openAllPlots() {
    printAction("Opening All Plots");
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        if (sp.getGraph().isShowing()) {
            say("Plot is open: %s", sp.getPresentationName());
            continue;
        }
        say("Opening Plot: %s", sp.getPresentationName());
        sp.open();
    }
    sayOK();
  }

  /**
   * As simple as opening all Plots and Scenes available in the simulation. See {@see #openAllPlots}
   * and {@see #openAllScenes}.
   */
  public void openAllPlotsAndScenes() {
    openAllPlots();
    openAllScenes();
  }

  /**
   * Opens all Scenes in the Simulation.
   */
  public void openAllScenes() {
    printAction("Opening All Scenes");
    for (Scene scn : sim.getSceneManager().getScenes()) {
        if (scn.isShowing()) {
          say("Scene is open: %s", scn.getPresentationName());
            continue;
        }
        say("Opening Scene: %s", scn.getPresentationName());
        scn.openScene();
    }
    sayOK();
  }

  /**
   * Opens a Monitor Plot in STAR-CCM+ GUI.
   *
   * @param monPl given Monitor Plot.
   */
  public void openPlot(MonitorPlot monPl) {
    StarPlot sp = sim.getPlotManager().getPlot(monPl.getPresentationName());
    sp.open();
  }

  /**
   * Plays a JAVA Macro.
   *
   * @param macro filename to be played without extension.
   */
  public void playMacro(String macro) {
    String macroFile = macro;
    if (!macro.matches(".*java$")) {
        macroFile = macro + ".java";
    }
    printAction("MACRO: " + macroFile);
    new StarScript(sim, new java.io.File(resolvePath(macroFile))).play();
  }

  /**
   * Flies over between two cameras and print pictures in between. The current picture number
   * is controlled by {@see #postCurFrame}. Picture resolution is controlled by {@see #defPicResX}
   * and {@see #defPicResY}.
   *
   * @param scn given Scene.
   * @param v1 given Camera 1.
   * @param v2 given Camera 2.
   * @param frames given number of frames to be generated. It updates {@see #postFrames} variable.
   */
  public void postFlyOverAndSavePics(Scene scn, VisView v1, VisView v2, int frames) {
    postFlyOverAndSavePics(scn, v1, v2, frames, new ArrayList(), null);
  }

  /**
   * Flies over between two cameras and print pictures in between. The current picture number
   * is controlled by {@see #postCurFrame}. Picture resolution is controlled by {@see #defPicResX}
   * and {@see #defPicResY}.
   *
   * @param scn given Scene.
   * @param v1 given Camera 1.
   * @param v2 given Camera 2.
   * @param frames given number of frames to be generated. It updates {@see #postFrames} variable.
   * @param rsv given Recorded Solution View.
   */
  public void postFlyOverAndSavePics(Scene scn, VisView v1, VisView v2, int frames,
                                                                        RecordedSolutionView rsv) {
    postFlyOverAndSavePics(scn, v1, v2, frames, new ArrayList(), rsv);
  }

  public void postFlyOverAndSavePics(Scene scn, ArrayList<VisView> cams) {
    postFlyOverAndSavePics(scn, null, null, cams.size() - 1, cams, null);
  }

  private void postFlyOverAndSavePics(Scene scn, VisView v1, VisView v2, int frames,
                                                ArrayList<VisView> cams, RecordedSolutionView rsv) {
    postFrames = frames;
    int maxState = 10000;
    if (rsv != null) {
        maxState = rsv.getMaxStateIndex();
    }
    String cam1 = "null";
    String cam2 = "null";
    int postOldFrame = postCurFrame;
    if (postCurFrame > maxState) {
        say("Finished frames.");
        return;
    }
    if (v1 != null) {
        setSceneCameraView(scn, v1, verboseDebug);
        cam1 = v1.getPresentationName();
    }
    if (v1 != null && v2 != null) {
        cam2 = v2.getPresentationName();
        cams.addAll(getCameraViews(v1, v2, frames, verboseDebug));
    }
    for (int i = 0; i < frames; i++) {
        int picNumber = i + postOldFrame;
        if (rsv != null) {
            //printLine();
            //say("Setting: " + rsv.getStateName());
            //say("State Index: " + rsv.getStateIndex());
            rsv.setStateIndex(rsv.getStateIndex() + 1);
        }
        if (!cams.isEmpty()) {
            setSceneCameraView(scn, cams.get(i+1), verboseDebug);
        }
        String picName = String.format("pic%04d_Cam_%s_to_%s.png", picNumber, cam1, cam2);
        File picFolder = createFolder("pics_" + simTitle, false);
        if (picFolder != null && postOldFrame == 0 && i == 0) {
            say("Folder created: " + picFolder.toString());
        }
        say("Printing: %s", picName);
        postFlyOverAndSavePics_prePrintPicture();
        hardCopyPicture(scn, new File(picFolder, picName), defPicResX, defPicResY, verboseDebug);
        postCurFrame ++;
    }
    postFlyOverAndSavePics_postPrintPicture();
    postCurFrame = postOldFrame + frames;
    cleanUpTemporaryCameraViews(verboseDebug);
  }

  /**
   * This method is in conjunction with {@see #postFlyOverAndSavePics} and it only works along with
   * an @Override. Useful for changing Displayer opacities, colors and other local stuff.
   *
   * It is invoked within the loop prior to printing the pictures.
   */
  public void postFlyOverAndSavePics_prePrintPicture() {
    //-- Use with @override
  }

  /**
   * This method is in conjunction with {@see #postFlyOverAndSavePics} and it only works along with
   * an @Override. Useful for changing global stuff.
   *
   * It is invoked outside the loop after the last printed picture.
   */
  public void postFlyOverAndSavePics_postPrintPicture() {
    //-- Use with @override
  }

  private void prettifyAnnotations(boolean verboseOption) {
    say("Prettifying Text Annotations...", verboseOption);
    for (star.vis.Annotation a : sim.getAnnotationManager().getObjects()) {
        boolean isLogo = hasClassName(a, "star.vis.LogoAnnotation");
        boolean isPlot = hasClassName(a, "star.vis.PlotImage");
        boolean isScn = hasClassName(a, "star.vis.SceneAnnotation");
        boolean isSimple = hasClassName(a, "star.vis.SimpleAnnotation");
        if (isLogo || isPlot || isScn) {
            continue;
        }
        say("   Annotation: " + a.getPresentationName());
        if (isSimple) {
            a.setFont(fontSimpleAnnotations);
            continue;
        } else {
            a.setFont(fontRepAnnotations);
        }
    }
  }

  private void prettifyLogo(boolean verboseOption) {
    say("Prettifying Logo Annotation...", verboseOption);
    ((LogoAnnotation) getAnnotation("Logo", false)).setOpacity(0.8);
  }

  /**
   * This method makes the Scenes and Plots look fancier.
   */
  public void prettifyMe() {
    prettifyMe(true);
  }

  private void prettifyMe(boolean verboseOption) {
    printAction("Prettifying Sim file", verboseOption);
    prettifyAnnotations(verboseOption);
    prettifyLogo(verboseOption);
    prettifyMonitors(verboseOption);
    prettifyHistograms(verboseOption);
    prettifyPlots(fontTitle, fontOther, verboseOption);
    prettifyScenes(verboseOption);
    sayOK(verboseOption);
  }

  private void prettifyHistograms(boolean verboseOption) {
    say("Prettifying Histograms...", verboseOption);
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        if (!isHistogramPlot(sp)) {
            continue;
        }
        if (sp.getPresentationName().startsWith("#")) {
            say("Skipping Histogram: " + sp.getPresentationName(), verboseOption);
            continue;
        }
        HistogramPlot hpl = (HistogramPlot) sp;
        say("   Histogram: " + hpl.getPresentationName(), verboseOption);
        HistogramAxisType hat = hpl.getXAxisType();
        hat.setNumberOfBin(20);
        String newName = hat.getBinFunction().getFieldFunction().getPresentationName() + " Histogram";
        say("   New Name: " + newName, verboseOption);
        hpl.setPresentationName(newName);
        hpl.setTitle(newName);
        hpl.setTitleFont(fontTitle);
        hpl.getDataSet().getFillStyle().setColor(colorSlateGray);
        hpl.getDataSet().getFillStyle().setBackgroundColor(colorSlateGray);
        hpl.getAxes().getXAxis().getTitle().setFont(fontOther);
        hpl.getAxes().getXAxis().getLabels().setFont(fontOther);
        hpl.getAxes().getYAxis().getTitle().setFont(fontOther);
        hpl.getAxes().getYAxis().getLabels().setFont(fontOther);
        hpl.getAxes().getYAxis().getGrid().setVisible(true);
        hpl.getAxes().getYAxis().getGrid().setColor(colorLightGray);
        hpl.getAxes().getYAxis().getGrid().setPattern(2);
    }
  }

  private void prettifyMonitors(boolean verboseOption) {
    String endsWith = " Monitor";
    say("Prettifying Monitors...", verboseOption);
    for (Monitor mon : sim.getMonitorManager().getObjects()) {
        say("   Monitor: " + mon.getPresentationName(), verboseOption);
        if (mon.getPresentationName().endsWith(endsWith)) {
            String newName = mon.getPresentationName().replace(endsWith, "");
            say("      New Name: " + newName, verboseOption);
            mon.setPresentationName(newName);
        }
        if (hasEMP() && isResidual(mon)) {
            ((ResidualMonitor) mon).getNormalizeOption().setSelected(MonitorNormalizeOption.OFF);
            say("      Normalization is now OFF.", verboseOption);
        }
    }
  }

  /**
   * This specific method makes the Plots look fancier in Presentations. It raises the Font sizes a little more
   * than the {@see #prettifyMe()} method.
   */
  public void prettifyPlots() {
    Font newTitle = new Font(fontTitle.getName(), fontTitle.getStyle(), fontTitle.getSize() + 8);
    Font newOthers = new Font(fontOther.getName(), fontOther.getStyle(), fontOther.getSize() + 4);
    prettifyPlots(newTitle, newOthers, true);
  }

  private void prettifyPlots(Font title, Font others, boolean verboseOption) {
    say("Prettifying Plots...");
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        prettifyPlot(sp, title, others, verboseOption);
    }
  }

  private void prettifyPlot(StarPlot sp, Font title, Font others, boolean verboseOption) {
    int thickness = 2;
    String name = sp.getPresentationName();
    String[] endsWith = {" Plot", " Monitor"};
    if (isHistogramPlot(sp)) {
        return;
    }
    if (name.startsWith("#")) {
        say("Skipping Plot: " + name, verboseOption);
        return;
    }
    say("   Plot: " + name, verboseOption);
    for (String s : endsWith) {
        if (name.endsWith(s)) {
            if (name.contains("-")) {
                continue;
            }
            String newName = name.replace(s, "");
            say("   New Name: " + newName, verboseOption);
            sp.setPresentationName(newName);
        }
    }
    sp.setTitleFont(title);
    sp.getLegend().setMapLineStyle(true);
    sp.getLegend().setFont(others);
    sp.getAxes().getXAxis().getLabels().setFont(others);
    sp.getAxes().getXAxis().getTitle().setFont(others);
    sp.getAxes().getXAxis().getGrid().setColor(colorLightGray);
    sp.getAxes().getXAxis().getGrid().setPattern(2);
    sp.getAxes().getYAxis().getLabels().setFont(others);
    sp.getAxes().getYAxis().getTitle().setFont(others);
    sp.getAxes().getYAxis().getGrid().setColor(colorLightGray);
    sp.getAxes().getYAxis().getGrid().setPattern(2);
    ArrayList<DataSet> ad = new ArrayList(sp.getDataSetGroup().getDataSets());
    for (DataSet ds : ad) {
        String dsName = ds.getPresentationName();
        String status = "Not Updated!";
        LineStyle ls = ds.getLineStyle();
        SymbolStyle ss = ds.getSymbolStyle();
        if (ad.size() == 1) {
            status = "Updated.";
            ls.setColor(colorSlateGray);
        }
        if (ls.getWidth() != thickness) {
            status = "Updated.";
            ls.setWidth(thickness);
        }
        if (isResidual(sp) && hasEMP()) {
            if (dsName.equals("Continuity")) {
                ss.setStyle(3);
            } else if (dsName.contains("momentum")) {
                ss.setStyle(9);
            } else if (dsName.contains("Tdr") || dsName.contains("Tke")) {
                ls.setStyle(3);
            } else if (dsName.startsWith("Granular")) {
                ss.setStyle(2);
            } else if (dsName.contains("-stress")) {
                ss.setStyle(5);
            }
            ss.setSpacing(2);
            status = "Updated.";
        }
        say("      " + dsName + ": " + status, verboseOption);
    }
  }

  private void prettifyScenes(boolean verboseOption) {
    say("Prettifying Scenes...", verboseOption);
    //-- Scenes starting with '#' will be skipped...
    for (Scene scn : getScenes(".*", false)) {
        if (scn.getPresentationName().startsWith("#")) {
            say("Skipping Scene: " + scn.getPresentationName(), verboseOption);
            continue;
        }
        say("Setting a white Background on Scene: " + scn.getPresentationName(), verboseOption);
        setSceneBackgroundColor_Solid(scn, Color.white);
        //-- Change Color of Axis
        scn.setAxesTextColor(new DoubleVector(colorSlateGrayDark.getColorComponents(null)));
    }
    for (Displayer d : getDisplayers(".*", false)) {
        if (d.getScene().getPresentationName().startsWith("#")) {
            continue;
        }
        if (isScalar(d) || isVector(d) || isStreamline(d)) {
            sayDisplayer(d);
            setDisplayerEnhancements(d);
        }
    }
  }

  /**
   * Prints an action with a fancy frame.
   *
   * @param text message to be printed.
   */
  public void printAction(String text) {
    printAction(text, true);
  }

  /**
   * Prints an action with a fancy frame.
   *
   * @param text message to be printed.
   * @param verboseOption Print to standard output?
   */
  public void printAction(String text, boolean verboseOption) {
    updateSimVar();
    say("", verboseOption);
    printLine(verboseOption);
    say("+ " + getTime(), verboseOption);
    say("+ " + text, verboseOption);
    printLine(verboseOption);
  }

  private void printAction(String text, NamedObject no, boolean verboseOption) {
    printAction(text, verboseOption);
    sayNamedObject(no, verboseOption);
  }

  /**
   * Prints a fancy frame with text in it.
   *
   * @param text message to be printed.
   */
  public void printFrame(String text) {
    printFrame(text, true);
  }

  private void printFrame(String text, boolean verboseOption) {
    updateSimVar();
    say("", verboseOption);
    printLine("#", verboseOption);
    say("## ", verboseOption);
    say("## " + text, verboseOption);
    say("## ", verboseOption);
    printLine("#", verboseOption);
  }

  /**
   * Prints a line.
   */
  public void printLine() {
    printLine(true);
  }

  /**
   * Prints a line 'n' times.
   *
   * @param n how many times the line will be printed.
   */
  public void printLine(int n) {
    printLine(n, true);
  }

  private void printLine(boolean verboseOption) {
    printLine(1, verboseOption);
  }

  private void printLine(int n, boolean verboseOption) {
    for (int i = 1; i <= n; i++) {
        printLine("-", verboseOption);
    }
  }

  private void printLine(String _char, boolean verboseOption) {
    String cc = _char;
    if ( _char.equals("-") ) {
        cc = "+";
    }
    say(cc + retRepeatString(_char, 120), verboseOption);
  }

  /**
   * An overview of the mesh diagnostics.
   */
  public void printMeshDiagnostics() {
    if (hasBeenMeshed > 1) {
        //-- Issue detailed in CCMP-59794
        return;
    }
    sayLoud("PRINTING FULL MESH DIAGNOSTICS");
    queryVolumeMesh().generateMeshReport(getRegions(".*", false));
    sleep(2000); //-- Output gets messed with the Stats...
    sayOK();
  }

  /**
   * An overview of the mesh parameters.
   * @param continua
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void printMeshParameters(MeshContinuum continua) {
    ReferenceValueManager rvm = continua.getReferenceValues();
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**       M E S H   P A R A M E T E R S   O V E R V I E W         **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Mesh Continua: " + continua.getPresentationName());
    say("**");
    say("*******************************************************************");
    say("**");
    say("** Base Size (%s): %g", defUnitLength.getPresentationName(),  rvm.get(BaseSize.class).getValue());
    say("**");
    say("** Surface Size Relative Min (%): " +
            rvm.get(SurfaceSize.class).getRelativeMinimumSize().getPercentage());
    say("** Surface Size Relative Tgt (%): " +
            rvm.get(SurfaceSize.class).getRelativeTargetSize().getPercentage());
    say("**");
    say("** Feature Curve Relative Min (%): " + featCurveMeshMin);
    say("** Feature Curve Relative Tgt (%): " + featCurveMeshTgt);
    say("**");
    if (isPoly(continua)) {
        say("** Mesh Growth Factor: " + mshGrowthFactor);
        say("**");
    }
    if (isTrimmer(continua)) {
        say("** Maximum Cell Size: " + mshTrimmerMaxCelSize);
        say("**");
    }
    say("*******************************************************************");
    say("");
  }

  /**
   * Prints an overview of the Prism Mesh Parameters.
   */
  public void printPrismsParameters() {
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**     P R I S M S   P A R A M E T E R S   O V E R V I E W       **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Number of Layers: " + prismsLayers);
    say("** Relative Height (%): " + prismsRelSizeHeight);
    say("**");
    say("** Minimum Thickness (%): " + prismsMinThickn);
    say("** Stretching Ratio: " + prismsStretching);
    say("** Layer Chopping (%): " + prismsLyrChoppPerc);
    say("** Near Core Layer Aspect Ratio: " + prismsNearCoreAspRat);
    say("**");
    say("*******************************************************************");
    say("");
  }

  /**
   * Prints all available Plots and Scenes as PNG files. This is the same method as {@see #hardCopyPictures}.
   *
   * @param x given picture resolution (width).
   * @param y given picture resolution (height).
   */
  public void printPlotsAndScenes(int x, int y) {
    hardCopyPictures(x, y);
  }

  /**
   * Prints all available Reports to text files in {@see #simPath} with an <u>.out</u> extension.
   */
  public void printReports() {
    printAction("Printing all Reports to text files");
    for (Report r : getReports(".*", false)) {
        r.printReport(new File(simPath, getStringForFilename(r.getPresentationName()) + ".out"), false);
        say("Saved: " + r.getPresentationName());
    }
    sayOK();
  }

  /**
   * Get the Geometry Part Representation.
   *
   * @return Geometry Part Representation.
   */
  public PartRepresentation queryGeometryRepresentation() {
      return ((PartRepresentation) sim.getRepresentationManager().getObject("Geometry"));
  }

  /**
   * Get the Import Surface Representation.
   *
   * @return Import Surface Representation.
   */
  public SurfaceRep queryImportRepresentation() {
      return querySurfaceRep("Import");
  }

  /**
   * Get the Initial Surface Representation.
   *
   * @return Initial Surface Representation.
   */
  public SurfaceRep queryInitialSurface() {
    return querySurfaceRep("Initial Surface");
  }

  /** When querying by AREA it will give the PSs always based on first element (0). **/
  private ArrayList<PartSurface> queryPartSurfaces(ArrayList<PartSurface> aps, String rangeType,
                                                                        String what, double tol) {
    if (aps.isEmpty()) {
        say("No Part Surfaces Provided for Querying. Returning NULL!");
        return null;
    }
    //--
    //-- Some declarations first.
    final String rangeOpts[] = {"MIN", "MAX"};
    ArrayList<PartSurface> aps2 = new ArrayList<PartSurface>();
    ArrayList<Double> ada = new ArrayList<Double>();
    ArrayList<GeometryPart> agp = new ArrayList<GeometryPart>();
    Vector choices = new Vector(Arrays.asList(xyzCoord));
    DoubleVector labMinXYZ = null, labMaxXYZ = null, psMinXYZ = null, psMaxXYZ = null;
    int rangeChoice = -1, whatChoice = -1;
    boolean proceed = true;
    //--
    //-- Headers
    printLine(2);
    printAction("Querying Part Surfaces: " + rangeType + " " + what);
    for (PartSurface ps : aps) {
        say("  " + ps.getPresentationName());
        GeometryPart gp = ps.getPart();
        if (agp.contains(gp)) { continue; }
        agp.add(gp);
    }
    printLine(2);
    //--
    //-- Just some checkings before moving on
    try {
        rangeChoice = Arrays.asList(rangeOpts).indexOf(rangeType.toUpperCase());
    } catch (Exception e) {
        proceed = false;
    }
    try {
        choices.add("AREA");
        whatChoice = choices.indexOf(what.toUpperCase());
    } catch (Exception e) {
        proceed = false;
    }
    if (!proceed) {
        say("Got NULL!");
        return null;
    }
    //--
    //-- Init Widget
    Scene scn = createScene_Geometry(false);
    PartSurfaceMeshWidget psmw = queryGeometryRepresentation().startSurfaceMeshWidget(scn);
    psmw.setActiveParts(agp);
    queryPartSurfaces_initPartSurfaceMeshWidget(psmw);
    //--
    //-- Add the Part Surfaces
    NeoObjectVector psObjs = new NeoObjectVector(aps.toArray());
    for (GeometryPart gp : agp) {
        if (isSimpleCylinderPart(gp)) {
            ((SimpleCylinderPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isSimpleBlockPart(gp)) {
            ((SimpleBlockPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isMeshOperationPart(gp)) {
            ((MeshOperationPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isCadPart(gp)) {
            ((CadPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        }
    }
    //--
    //-- Init Query
    SurfaceMeshWidgetSelectController smwsc = psmw.getControllers().getController(SurfaceMeshWidgetSelectController.class);
    smwsc.selectPartSurfaces(psObjs);
    SurfaceMeshWidgetQueryController smwqc = psmw.getControllers().getController(SurfaceMeshWidgetQueryController.class);
    NeoProperty retRange = null;
    NeoProperty retArea = null;
    //--
    //-- Overall Stats
    printLine(4);
    say("Global Info: " + agp.size() + " Geometry Part(s)");
    printLine(2);
    if (what.toUpperCase().equals("AREA")) {
        retArea = smwqc.queryFaceArea();
    } else {
        retRange = smwqc.queryFaceGeometricRange();
        labMinXYZ = retRange.getDoubleVector("LabMinRange");
        labMaxXYZ = retRange.getDoubleVector("LabMaxRange");
    }
    printLine(4);
    //--
    double val = 0.0;
    for (PartSurface ps : aps) {
        //--
        if (!agp.contains(ps.getPart())) { continue; }
        if (!aps.contains(ps)) { continue; }
        //--
        smwsc.clearSelected();
        smwsc.selectPartSurface(ps);
        printLine();
        sayPartSurface(ps);
        printLine();
        //--
        //-- If querying by AREA
        if (what.toUpperCase().equals("AREA")) {
            retArea = smwqc.queryFaceArea();
            val = retArea.getDouble("TotalFaceArea");
            say("Face Area: " + val);
            if (aps2.isEmpty()) {
                aps2.add(ps);
                ada.add(val);
            } else {
                int i = 0;
                for (Iterator<Double> it = new ArrayList(ada).iterator(); it.hasNext();) {
                    Double storedVal = it.next();
                    //--                    ** Avoid duplicates **
                    if (val > storedVal && !aps2.contains(ps)) {
                        aps2.add(i, ps);
                        ada.add(i, val);
                    //--                        ** Avoid duplicates **
                    } else if (!it.hasNext() && !aps2.contains(ps)) {
                        aps2.add(ps);
                        ada.add(val);
                    } //-- Higher AREAs go first.
                    i++;
                }
            }
        //--
        //-- Otherwise, i.e., X, Y, Z
        } else {
            boolean isLocal = false;
            boolean isGlobal = false;
            retRange = smwqc.queryFaceGeometricRange();
            psMinXYZ = retRange.getDoubleVector("LabMinRange");
            psMaxXYZ = retRange.getDoubleVector("LabMaxRange");
            isLocal = retDiff(psMinXYZ.get(whatChoice), psMaxXYZ.get(whatChoice), tol);
            if (isLocal) {
                say("Found Local " + what.toUpperCase() + " = " + psMinXYZ.get(whatChoice));
            }
            //say("Range Choice: " + rangeChoice);
            switch (rangeChoice) {
                case 0:
                    isGlobal = retDiff(psMinXYZ.get(whatChoice), labMinXYZ.get(whatChoice), tol);
                    val = psMinXYZ.get(whatChoice);
                    break;
                case 1:
                    isGlobal = retDiff(psMaxXYZ.get(whatChoice), labMaxXYZ.get(whatChoice), tol);
                    val = psMaxXYZ.get(whatChoice);
                    break;
            }
            if (isLocal && isGlobal) {
                say(String.format("Found Global %s %s = ", rangeOpts[rangeChoice], choices.get(whatChoice)) + val);
                aps2.add(ps);
            }
        }
        say("");
    }
    if (choices.get(whatChoice).equals("AREA") && rangeOpts[rangeChoice].equals("MIN")) {
        Collections.reverse(aps2);
    }
    psmw.stop();
    sim.getSceneManager().deleteScene(scn);
    printLine();
    /** say("aps2 size: " + aps2.size());
    if (what.equals("AREA")) {
        say("vecAREA size: " + ada.size());
    } */
    say(String.format("Found %d Part Surfaces matching %s %s:", aps.size(), rangeOpts[rangeChoice], choices.get(whatChoice)));
    for (PartSurface ps : aps2) {
        say("  " + ps.getPresentationName());
    }
    printLine();
    return aps2;
  }

  private void queryPartSurfaces_initPartSurfaceMeshWidget(PartSurfaceMeshWidget psmw) {
    psmw.startSurfaceMeshDiagnostics();
    psmw.startSurfaceMeshRepair();
    psmw.startMergeImprintController();
    psmw.startIntersectController();
    psmw.startLeakFinderController();
    psmw.startSurfaceMeshQueryController();
    SurfaceMeshWidgetDiagnosticsController smwdc = psmw.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    smwdc.setCheckPiercedFaces(false);
    smwdc.setPiercedFacesActive(false);
    smwdc.setCheckPoorQualityFaces(false);
    smwdc.setPoorQualityFacesActive(false);
    smwdc.setMinimumFaceQuality(0.01);
    smwdc.setCheckCloseProximityFaces(false);
    smwdc.setCloseProximityFacesActive(false);
    smwdc.setMinimumFaceProximity(0.05);
    smwdc.setCheckFreeEdges(false);
    smwdc.setFreeEdgesActive(false);
    smwdc.setCheckNonmanifoldEdges(false);
    smwdc.setNonmanifoldEdgesActive(false);
    smwdc.setCheckNonmanifoldVertices(false);
    smwdc.setNonmanifoldVerticesActive(false);
    smwdc.setCheckFeatureNumberEdges(true);
    smwdc.setCheckFeatureLength(false);
    smwdc.setCheckFeatureAngle(true);
    smwdc.setCheckFeatureOpenCurve(true);
    smwdc.setCheckFeatureSmallRegions(false);
    smwdc.setMinNumberEdges(5);
    smwdc.setMinFeatureLength(1.0E-4);
    smwdc.setMaxFeatureAngle(121.0);
    smwdc.setMinFeatureRegionArea(1.0E-4);
    smwdc.setCheckSoftFeatureErrors(false);
    smwdc.setSoftFeatureErrorsActive(false);
    smwdc.setCheckHardFeatureErrors(false);
    smwdc.setHardFeatureErrorsActive(false);
    //smwdc.changeDisplayedThresholds(new NeoObjectVector(new Object[] {}), true);
  }

  /**
   * Get the Remeshed Surface Representation.
   *
   * @return Remeshed Surface Representation.
   */
  public SurfaceRep queryRemeshedSurface() {
    return querySurfaceRep("Remeshed Surface");
  }

  private DoubleVector queryStats(ArrayList<PartSurface> aps) {
    if (aps.isEmpty()) {
        say("No Part Surfaces Provided for Querying. Returning NULL!");
        return null;
    }
    //--
    //ArrayList<PartSurface> aps2 = new ArrayList<PartSurface>();
    DoubleVector labMinXYZ = null, labMaxXYZ = null, labDeltaXYZ = null;
    DoubleVector labMinMaxXYZ = new DoubleVector();
    //--
    //-- Headers
    printLine(2);
    printAction("Querying Part Surfaces Range: " + aps.size() + " object(s).");
    ArrayList<GeometryPart> agp = new ArrayList<GeometryPart>();
    for (PartSurface ps : aps) {
        say("  " + ps.getPresentationName());
        GeometryPart gp = ps.getPart();
        if (agp.contains(gp)) { continue; }
        agp.add(gp);
    }
    printLine(2);
    //--
    //-- Init Widget
    Scene scn = createScene_Geometry(false);
    PartSurfaceMeshWidget psmw = queryGeometryRepresentation().startSurfaceMeshWidget(scn);
    psmw.setActiveParts(agp);
    queryPartSurfaces_initPartSurfaceMeshWidget(psmw);
    //--
    //-- Add the Part Surfaces
    NeoObjectVector psObjs = new NeoObjectVector(aps.toArray());
    for (GeometryPart gp : agp) {
        if (isSimpleCylinderPart(gp)) {
            ((SimpleCylinderPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isSimpleBlockPart(gp)) {
            ((SimpleBlockPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isMeshOperationPart(gp)) {
            ((MeshOperationPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        } else if (isCadPart(gp)) {
            ((CadPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
        }
    }
    //--
    //-- Init Query
    SurfaceMeshWidgetSelectController smwsc = psmw.getControllers().getController(SurfaceMeshWidgetSelectController.class);
    smwsc.selectPartSurfaces(psObjs);
    SurfaceMeshWidgetQueryController smwqc = psmw.getControllers().getController(SurfaceMeshWidgetQueryController.class);
    NeoProperty retRange = null;
    //--
    //-- Overall Stats
    retRange = smwqc.queryFaceGeometricRange();
    labMinXYZ = retRange.getDoubleVector("LabMinRange");
    labMaxXYZ = retRange.getDoubleVector("LabMaxRange");
    labDeltaXYZ = retRange.getDoubleVector("XYZComponents");
    //--
    //-- labMinMaxXYZ == { x0, x1, y0, y1, z0, z1, dx, dy, dz }
    //--
    for (int i = 0; i < labMinXYZ.size(); i++) {
        labMinMaxXYZ.add(labMinXYZ.get(i) / defUnitLength.getConversion());
        labMinMaxXYZ.add(labMaxXYZ.get(i) / defUnitLength.getConversion());
    }
    //-- Add Deltas
    for (int i = 0; i < 3; i++) {
        labMinMaxXYZ.add(labDeltaXYZ.get(i) / defUnitLength.getConversion());
    }
    //say("### DEBUG:");
    //say(retRange.getHashtable().toString());
    //say(labMinXYZ.toString());
    //say(labMaxXYZ.toString());
    //say(labDeltaXYZ.toString());
    //say(labMinMaxXYZ.toString());
    //say("#################");
    //--
    psmw.stop();
    sim.getSceneManager().deleteScene(scn);
    printLine(2);
    return labMinMaxXYZ;
  }

  private SurfaceRep querySurfaceRep(String name) {
    if (sim.getRepresentationManager().has(name)) {
        return (SurfaceRep) sim.getRepresentationManager().getObject(name);
    }
    return null;
  }

  /**
   * Get the Wrapped Surface Representation.
   *
   * @return Wrapped Surface Representation.
   */
  public SurfaceRep queryWrappedSurface() {
    return querySurfaceRep("Wrapped Surface");
  }

  private Units queryUnit(String unitString, boolean verboseOption) {
    Units unit = getUnit(unitString, false);
    if (unit != null) {
        say(verboseOption, unitFormat, "Unit read", unitString, unit.getDescription());
        return unit;
    }
    say(verboseOption, unitFormat, "Unit not read", unitString, "");
    return null;
  }

  /**
   * Get the Finite Volume Representation.
   *
   * @return Finite Volume Representation.
   */
  public FvRepresentation queryVolumeMesh() {
    if (sim.getRepresentationManager().has("Volume Mesh")) {
        return (FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh");
    }
    return null;
  }

  /**
   * Reads a Camera string generated by {@see #getCameraViews} and then creates a new Camera View.
   *
   * @param cam given camera string.
   * @return The created Camera View.
   */
  public VisView readCameraView(String cam) {
    return readCameraView(cam, ".*", true);
  }

  private VisView readCameraView(String cam, String regexPatt, boolean verboseOption) {
    printAction("Reading a Camera View", verboseOption);
    say("Camera string: " + cam, verboseOption);
    String[] props = cam.split("\\" + camSplitCharBetweenFields);
    if (!props[0].matches(regexPatt)) {
        return null;
    }
    VisView oldCam = getCameraView(props[0], false);
    if (oldCam != null) {
        say("Already exists. Skipping...", verboseOption);
        return oldCam;
    }
    VisView newCam = sim.getViewManager().createView();
    newCam.setPresentationName(props[0]);
    //--
    DoubleVector dv = new DoubleVector(coord0);
    //--
    for (int i = 1; i <= 3; i++) {
        String[] items = props[i].split(",");
        for (int j = 0; j < items.length; j++) {
            dv.setElementAt(Double.valueOf(items[j]), j);
        }
        switch(i) {
            case 1:
                newCam.setFocalPoint(dv);
                say("Focal Point read: " + dv.toString());
                break;
            case 2:
                newCam.setPosition(dv);
                say("Position read: " + dv.toString());
                break;
            case 3:
                newCam.setViewUp(dv);
                say("View Up read: " + dv.toString());
                break;
        }
    }
    double ps = Double.valueOf(props[4]);
    newCam.setParallelScale(ps);
    say("Parallel Scale read: " + ps);
    try {
        int pm = Integer.parseInt(props[5]);
        newCam.setProjectionMode(pm);
        say("Projection Mode read: " + pm);
    } catch (Exception e) {
        say("Could not read Projection Mode. Error parsing it or value not found.");
    }
    sayCamera(newCam, verboseOption);
    return newCam;
  }

  /**
   * Creates all Camera Views stored in a file.<p>
   * Note that the Cameras must be in the same format as defined in {@see #getCameraViews}.
   *
   * @param filename given filename. File will be read from {@see #simPath} folder.
   */
  public void readCameraViews(String filename) {
    readCameraViews(filename, ".*", true);
  }

  /**
   * Creates the Camera Views stored in a file, given a REGEX criteria.<p>
   * Note that the Cameras must be in the same format as defined in {@see #getCameraViews}.
   *
   * @param filename given filename. File will be read from {@see #simPath} folder.
   * @param regexPatt given REGEX search pattern.
   */
  public void readCameraViews(String filename, String regexPatt) {
    readCameraViews(filename, regexPatt, true);
  }

  private void readCameraViews(String filename, String regexPatt, boolean verboseOption) {
    printAction("Reading Camera Views", verboseOption);
    File camFile = new File(simPath, filename);
    String camData = readData(camFile);
    say("Camera File: %s", camFile.getAbsolutePath(), verboseOption);
    String[] cams = camData.split("\n");
    for (int i = 0; i < cams.length; i++) {
        say("Processing Camera View: " + (i+1));
        try {
            VisView v = readCameraView(cams[i], regexPatt, false);
            if (v == null) {
                say("   String ignored by REGEX criteria.");
                say("   Camera format is: " + camFormat);
                say("   String given is: " + cams[i]);
                say("");
            } else {
                sayCamera(v);
            }
        } catch (Exception e) {
            //say(e.getMessage());
            //say(e.toString());
            say("   Unable to process Camera View.");
        }
    }
  }

  /**
   * @param file given File.
   * @return Data within as String.
   */
  private String readData(File file) {
    StringBuilder contents = new StringBuilder();
    try {
        BufferedReader input =  new BufferedReader(new FileReader(file));
        try {
            String line = null;
            while (( line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    }
    return contents.toString();
  }

  /**
   * @param file given File.
   * @return String[] split by new line.
   */
  private String[] readFile(File file) {
    String data = readData(file);
    return data.split("\\n");
  }

  private void rebuildCompositeChildren(CompositePart cp, String splitChar) {
    CompositePart newCP = null;
    say("Looking in: " + cp.getPresentationName());
    String splitChar0 = splitChar;
    if (splitChar.equals("\\") || splitChar.equals("|")) {
        splitChar = "\\" + splitChar;
    }
    for (GeometryPart gp : cp.getLeafParts()) {
        String name = gp.getPresentationName();
        String[] splitName = name.split(splitChar);
        //say("Split Lenght: " + splitName.length);
        if (splitName.length <= 1) { continue; }
        String name0 = splitName[0];
        String gpNewName = name.replaceFirst(name0 + splitChar, "");
        if (gpNewName.equals(name)) {
            say("");
            say(name);
            say("Old name == New name. Skipping...");
            say("");
            continue;
        }
        try{
            newCP = (CompositePart) cp.getChildParts().getPart(name0);
        } catch (Exception e) {
            say("Creating Composite Part: " + name0);
            newCP = cp.getChildParts().createCompositePart();
            newCP.setPresentationName(name0);
        }
        gp.setPresentationName(gpNewName);
            say("Parenting: ");
            say("  + " + newCP.getPresentationName());
            say("  |---+ " + gp.getPresentationName());
            sayOldNameNewName(name, gpNewName);
            gp.reparent(newCP.getChildParts());
            rebuildCompositeChildren(newCP, splitChar0);
    }
  }

  /**
   * Tries to rebuild the hierarchy of a Composite Part based on a split character.
   *
   * @param cp given Composite Part.
   * @param splitChar given split character. E.g.: |, +, etc...
   */
  public void rebuildCompositeHierarchy(CompositePart cp, String splitChar) {
    printAction("Rebuilding Composite Assembly Hierarchy based on a split character");
    say("Composite Part: " + cp.getPresentationName());
    rebuildCompositeChildren(cp, splitChar);
    sayOK();
  }

  /**
   * Rebuilds all interfaces in the model.
   */
  public void rebuildAllInterfaces() {
    printAction("Rebuilding all Interfaces of the Model");
    Vector abD = new Vector();
    Vector abD_tol = new Vector();
    Vector abI = new Vector();
    Vector abI_tol = new Vector();
    ArrayList<Boundary> abD0 = new ArrayList<Boundary>();
    ArrayList<Boundary> abD1 = new ArrayList<Boundary>();
    ArrayList<Boundary> abI0 = new ArrayList<Boundary>();
    ArrayList<Boundary> abI1 = new ArrayList<Boundary>();
    Boundary b0, b1;
    DirectBoundaryInterface dbi;
    IndirectBoundaryInterface ibi;
    say("Looping over all interfaces and reading data...");
    for (Interface i : getAllInterfaces(false)) {
        String name = i.getPresentationName();
        String beanName = i.getBeanDisplayName();
        say(String.format("  Reading: %-40s[%s]", name, beanName));
        if (isDirectBoundaryInterface(i)) {
            dbi = (DirectBoundaryInterface) i;
            abD.add(name);
            abD_tol.add(dbi.getValues().get(InterfaceToleranceCondition.class).getTolerance());
            abD0.add(dbi.getParentBoundary0());
            abD1.add(dbi.getParentBoundary1());
        }
        if (isIndirectBoundaryInterface(i)) {
            ibi = (IndirectBoundaryInterface) i;
            abI.add(name);
            abI_tol.add(ibi.getValues().get(MappedInterfaceToleranceCondition.class).getProximityTolerance());
            abI0.add(ibi.getParentBoundary0());
            abI1.add(ibi.getParentBoundary1());
        }
    }
    clearInterfaces(false);
    say("Recreating " + abD.size() + " Direct Interfaces...");
    for (int i = 0; i < abD.size(); i++) {
        String name = (String) abD.get(i);
        double tol = (Double) abD_tol.get(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = abD0.get(i);
        b1 = abD1.get(i);
        dbi = sim.getInterfaceManager().createDirectInterface(b0, b1);
        dbi.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
        dbi.setPresentationName(name);
    }
    say("Recreating " + abI.size() + " Indirect Interfaces...");
    for (int i = 0; i < abI.size(); i++) {
        String name = (String) abI.get(i);
        double tol = (Double) abI_tol.get(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = abI0.get(i);
        b1 = abI1.get(i);
        ibi = sim.getInterfaceManager().createIndirectInterface(b0, b1);
        ibi.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
        ibi.setPresentationName(name);
    }
    sayOK();
  }

  /**
   * Removes all Part Contacts.
   */
  public void removeAllPartsContacts() {
    printAction("Removing All Parts Contacts");
    for (GeometryPart gp : getAllLeafParts()) {
        //CadPart cp = (CadPart) gp;
        ArrayList<PartContact> ap = new ArrayList(sim.get(SimulationPartManager.class).getPartContactManager().getObjects());
        for (PartContact pc : ap) {
            sim.get(SimulationPartManager.class).getPartContactManager().remove(pc);
        }
    }
    sayOK();
  }

  /**
   * Removes a CAD part. <i>Note that it must belong to a Composite Part.</i>
   *
   * @param name part name
   */
  public void removeCadPart(String name) {
    printAction("Removing Cad Part: " + name);
    try {
        CadPart cp = getCadPart(name);
        CompositePart cmp = (CompositePart) cp.getParentPart();
        cmp.getChildParts().removePart(cp);
        say("Removed: " + cp.getPathInHierarchy());
    } catch (Exception e) {
        say("CadPart not found: " + name);
    }
    sayOK();
  }

  /**
   * Removes a Composite Part.
   *
   * @param cp given Composite Part.
   */
  public void removeCompositePart(CompositePart cp) {
    removeCompositePart(cp, true);
  }

  private int removeCompositePart(CompositePart cp, boolean verboseOption) {
    printAction("Removing a Composite Part", verboseOption);
    try{
        CompositePart parent = ((CompositePart) cp.getParentPart());
        say("Removing Composite: " + cp.getPresentationName());
        parent.getChildParts().remove(cp);
        sayOK(verboseOption);
        return 0;
    } catch (Exception e) {
        say("ERROR! Could not remove Composite Part.");
    }
    return 1;
  }

  /**
   * Removes a group of Composite Parts.
   *
   * @param alcp given ArrayList of Composite Parts.
   */
  public void removeCompositeParts(ArrayList<CompositePart> alcp) {
    removeCompositeParts(alcp, true);
  }

  /**
   * Removes a group of Composite Parts based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void removeCompositeParts(String regexPatt) {
    printAction(String.format("Removing Composite Parts based on REGEX pattern: \"%s\"", regexPatt ));
    int n = 0;
    for (CompositePart cp : getAllCompositeParts(regexPatt, false)) {
        if (removeCompositePart(cp, false) == 0) {
            n++;
        }
    }
    say("Composite Parts removed: " + n);
    sayOK();
  }

  private void removeCompositeParts(ArrayList<CompositePart> alcp, boolean verboseOption) {
    printAction("Removing Composite Parts", verboseOption);
    int n = 0;
    say("Composite Parts to be removed: " + alcp.size());
    for (CompositePart cp : alcp) {
        int ret = removeCompositePart(cp, false);
        if (ret == 0) { n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK(verboseOption);
  }

  /**
   * Removes a Displayer from a Scene.
   *
   * @param d given Displayer.
   */
  public void removeDisplayer(Displayer d) {
    printAction("Removing a Displayer");
    say("Displayer: " + d.getPresentationName());
    say("From Scene: " + d.getScene().getPresentationName());
    d.getScene().getDisplayerManager().deleteChildren(new NeoObjectVector((new Object[] {d})));
    sayOK();
  }

  /**
   * Removes all the Feature Curves of a Region. Useful when using the Surface Wrapper.
   *
   * @param r given Region.
   */
  public void removeFeatureCurves(Region r) {
    printAction("Removing all Feature Curves");
    sayRegion(r);
    r.getFeatureCurveManager().deleteChildren(r.getFeatureCurveManager().getFeatureCurves());
    sayOK();
  }

  /**
   * Removes a Geometry Part.
   *
   * @param gp given Geometry Part.
   */
  public void removeGeometryPart(GeometryPart gp) {
    removeGeometryPart(gp, true);
  }

  private void removeGeometryPart(GeometryPart gp, boolean verboseOption) {
    printAction("Removing a Geometry Part", verboseOption);
    say("Geometry Part: " + gp.getPresentationName());
    if (isCompositePart(gp)) {
        removeCompositePart((CompositePart) gp, false);
    } else {
        try{
            sim.get(SimulationPartManager.class).remove(gp);
            say("Geometry Part removed.");
        } catch(Exception e) {
            say("ERROR! Could not remove Geometry Part.");
        }
    }
  }

    /**
     *
     * @param regexPatt
     */
    public void removeGeometryParts(String regexPatt) {
    printAction(String.format("Removing Geometry Parts based on REGEX criteria: \"%s\"", regexPatt));
    for (GeometryPart gp : getAllGeometryParts(regexPatt, false)) {
        removeGeometryPart(gp, false);
    }
    sayOK();
  }

  /**
   * Removes the Invalid Cells in the model.
   */
  public void removeInvalidCells() {
    ArrayList<Region> regionsPoly = new ArrayList<Region>();
    ArrayList<Region> regionsTrimmer = new ArrayList<Region>();
    ArrayList<Region> fvRegions = new ArrayList<Region>();
    for (Region r : getRegions(".*", false)) {
        if (hasPolyMesh(r)) {
            regionsPoly.add(r);
            continue;
        }
        if (hasTrimmerMesh(r)) {
            regionsTrimmer.add(r);
            continue;
        }
        fvRegions.add(r);
    }
    /* Removing From fvRepresentation */
    if (fvRegions.size() > 0) {
        printAction("Removing Invalid Cells from Regions using Default Parameters");
        say("Number of Regions: " + fvRegions.size());
        sim.getMeshManager().removeInvalidCells(new NeoObjectVector(fvRegions.toArray()),
                minFaceValidity, minCellQuality, minVolChange, minContigCells,
                minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Poly Meshes */
    if (regionsPoly.size() > 0) {
        printAction("Removing Invalid Cells from Poly Meshes");
        say("Number of Regions: " + regionsPoly.size());
        if (aggressiveRemoval) {
            minFaceValidity = 0.95;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(new NeoObjectVector(regionsPoly.toArray()),
                minFaceValidity, minCellQuality, minVolChange, minContigCells,
                minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Trimmer Meshes */
    if (regionsTrimmer.size() > 0) {
        printAction("Removing Invalid Cells from Trimmer Meshes");
        say("Number of Regions: " + regionsTrimmer.size());
        if (aggressiveRemoval) {
            minFaceValidity = 0.51;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(new NeoObjectVector(regionsTrimmer.toArray()),
                minFaceValidity, minCellQuality, minVolChange, minContigCells,
                minConFaceAreas, minCellVolume);
        sayOK();
    }
  }

  /**
   * Removes a Leaf Mesh Part.
   *
   * @param lmp given Leaf Mesh Part.
   */
  public void removeLeafMeshPart(LeafMeshPart lmp) {
    removeLeafMeshPart(lmp, true);
  }

  private void removeLeafMeshPart(LeafMeshPart lmp, boolean verboseOption) {
    String name = lmp.getPresentationName();
    printAction("Removing Leaf Mesh Part: " + name, verboseOption);
    sim.get(SimulationPartManager.class).removePart(lmp);
    say("Removed: " + name);
  }

  /**
   * Removes a group of Leaf Mesh Parts.
   *
   * @param colLMP given ArrayList of Leaf Mesh Parts.
   */
  public void removeLeafMeshParts(ArrayList<LeafMeshPart> colLMP) {
    removeLeafMeshParts(colLMP, true);
  }

  /**
   * Removes all Leaf Mesh Parts given REGEX search pattern.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void removeLeafMeshParts(String regexPatt) {
    printAction(String.format("Removing Leaf Meshs Part by REGEX pattern: \"%s\"", regexPatt));
    removeLeafMeshParts(getAllLeafMeshParts(regexPatt, false), true);
  }

  private void removeLeafMeshParts(ArrayList<LeafMeshPart> colLMP, boolean verboseOption) {
    printAction("Removing Leaf Mesh Parts", verboseOption);
    say("Leaf Mesh Parts to be removed: " + colLMP.size(), verboseOption);
    for (LeafMeshPart lmp : colLMP) {
        removeLeafMeshPart(lmp, false);
    }
    sayOK(verboseOption);
  }

  /**
   * Removes all Leaf Parts given REGEX search pattern.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void removeLeafParts(String regexPatt) {
    removeLeafParts(regexPatt, true);
  }

  private void removeLeafParts(String regexPatt, boolean verboseOption) {
    printAction(String.format("Remove Leaf Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    ArrayList<GeometryPart> colLP = getAllLeafParts(regexPatt, false);
    say("Leaf Parts to be removed: " + colLP.size());
    for (GeometryPart gp : colLP) {
        removeGeometryPart(gp, false);
    }
    sayOK(verboseOption);
  }

    /**
     *
     * @param refPart
     * @param vecPart
     */
    public void removePartsContacts(GeometryPart refPart, ArrayList<GeometryPart> vecPart) {
    printAction("Removing Parts Contacts");
    say("Reference Part: " + refPart.getPathInHierarchy());
    say("Contacting Parts: ");
    for (GeometryPart gp : vecPart) {
        say("  " + gp.getPathInHierarchy());
    }
    sim.get(SimulationPartManager.class).getPartContactManager().removePartContacts(refPart, vecPart);
    sayOK();
  }

  /**
   * Removes a Geometry Part that belongs to a Composite.
   *
   * @param gp
   */
  public void removeGeometryPartInComposite(GeometryPart gp) {
    removeGeometryPartInComposite(gp, true);
  }

  /**
   * Removes Geometry Parts that belongs to Composite Parts according to REGEX search.
   *
   * @param regexPatt given search criteria.
   */
  public void removeGeometryPartInComposite(String regexPatt) {
    printAction("Removing Geometry Parts that belong to Composite Parts");
    for (GeometryPart gp : getAllLeafParts(regexPatt, false)) {
        say("Removing: " + gp.getPathInHierarchy());
        removeGeometryPartInComposite(gp, false);
    }
    sayOK();
  }

  private void removeGeometryPartInComposite(GeometryPart gp, boolean verboseOption) {
    printAction("Removing a Geometry Part which belongs to a Composite Part", verboseOption);
    say("Removing: " + gp.getPathInHierarchy(), verboseOption);
    ((CompositePart) gp.getParentPart()).getChildParts().removePart(gp);
    sayOK(verboseOption);
  }

  /**
   * Removes View Factors.
   */
  public void removeViewFactors() {
    if (!hasViewFactors()) {
        return;
    }
    getViewFactorCalcSolver().deleteViewfactors();
    say("View Factors removed.");
  }
  /**
   * Renames the Interfaces according to their Parent Regions.
   */
  public void renameInterfaces() {
    printAction("Renaming Interfaces");
    String fmt = "%s/%s";
    for (Interface i : sim.getInterfaceManager().getObjects()) {
        Region r0 = null, r1 = null;
        if (isDirectBoundaryInterface(i)) {
            DirectBoundaryInterface dbi = (DirectBoundaryInterface) i;
            r0 = dbi.getRegion0();
            r1 = dbi.getRegion1();
        }
        if (isIndirectBoundaryInterface(i)) {
            IndirectBoundaryInterface idbi = (IndirectBoundaryInterface) i;
            r0 = idbi.getRegion0();
            r1 = idbi.getRegion1();
        }
        if (r0 == null) {
            continue;
        }
        String s0 = i.getPresentationName();
        i.setPresentationName(String.format(fmt, r0.getPresentationName(), r1.getPresentationName()));
        sayOldNameNewName(s0, i.getPresentationName());
    }
    sayOK();
  }

  /**
   * Renames Part Surfaces based on 2 REGEX search patterns. <p>
   * It looks for all Part Surfaces that has both search Strings and rename it accordingly.
   *
   * @param hasString1 given REGEX search pattern 1.
   * @param hasString2 given REGEX search pattern 2.
   * @param renameTo given new name of the found Part Surfaces.
   */
  public void renamePartSurfaces(String hasString1, String hasString2, String renameTo) {
    printAction("Renaming Part Surface(s)");
    for (PartSurface ps : getPartSurfaces(".*", false)) {
        String name = ps.getPresentationName();
        if (name.matches(hasString1) && name.matches(hasString2)) {
            sayOldNameNewName(name, renameTo);
            ps.setPresentationName(renameTo);
        }
    }
    sayOK();
  }

  /**
   * Searches for a Boundary in given Region and rename it to a new name.
   *
   * @param r given Region.
   * @param regexPatt given REGEX search pattern.
   * @param renameTo given new Boundary name.
   */
  public void renameBoundary(Region r, String regexPatt, String renameTo) {
    renameBoundary(r, regexPatt, renameTo, true);
  }

  private void renameBoundary(Region r, String regexPatt, String renameTo, boolean verboseOption) {
    printAction("Renaming Boundary", verboseOption);
    sayRegion(r);
    Boundary b = getBoundary(r, regexPatt, false);
    sayOldNameNewName(b.getPresentationName(), renameTo);
    b.setPresentationName(renameTo);
    sayOK(verboseOption);
  }

  /**
   * When the Boundary names are preceeded by the Region name, this method will remove the prefix from
   * all Boundary names.
   *
   * @param r given Region.
   */
  public void resetBoundaryNames(Region r) {
    printAction("Removing Prefixes from Boundary Names");
    sayRegion(r);
    for (Boundary b : r.getBoundaryManager().getBoundaries()) {
        String name = b.getPresentationName();
        String newName = name.replace(r.getPresentationName() + ".", "");
        sayOldNameNewName(name, newName);
        b.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets the Interface names to show the dependent Region names and types. <p>
   * E.g.: <i>F-S: Pipe <-> Metal</i> means that is a Fluid-Solid Interface between Pipe and Metal
   * Regions.
   */
  public void resetInterfaceNames() {
    printAction("Resetting Interface Names");
    ArrayList<Interface> colInt = getAllInterfaces(false);
    say("Number of Interfaces: " + colInt.size());
    for (Interface i : colInt) {
        String name0 = i.getRegion0().getPresentationName();
        String type0 = "F";
        if (isSolid(i.getRegion0())) {
            type0 = "S";
        }
        String name1 = i.getRegion1().getPresentationName();
        String type1 = "F";
        if (isSolid(i.getRegion1())) {
            type1 = "S";
        }
        String newName = String.format("%s-%s: %s <-> %s", type0, type1, name0, name1);
        sayOldNameNewName(i.getPresentationName(), newName);
        i.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets all Solver Settings to the default values.
   */
  public void resetSolverSettings() {
    printAction("Resetting All URFs");
    maxIter = maxIter0;
    urfP = urfP0;
    urfVel = urfVel0;
    urfKEps = urfKEps0;
    urfKOmega = urfKEps0;
    urfFluidEnrgy = urfFluidEnrgy0;
    urfSolidEnrgy = urfSolidEnrgy0;
    urfRS = urfRS0;
    urfSpecies = urfSpecies0;
    urfVOF = urfVOF0;
    urfVolFrac = urfVolFrac0;
    urfKEpsTurbVisc = urfKOmegaTurbVisc = urfRSTurbVisc = 1.0;
    updateSolverSettings();
  }

  /**
   * Resets the Surface Remesher to default conditions.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void resetSurfaceRemesher(MeshContinuum continua) {
    enableSurfaceRemesher(continua);
    enableSurfaceProximityRefinement(continua);
    enableSurfaceRemesherAutomaticRepair(continua);
    enableSurfaceRemesherProjectToCAD(continua);
  }

  /**
   * Reset the Vis Transform to default conditions.
   *
   * @param d given Displayer.
   */
  public void resetVisTransform(Displayer d) {
    setVisTransform(d, getVisTransform("Identity", false));
  }

  /**
   * Retesselate the CAD Part to Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToFine(CadPart part) {
    printAction("ReTesselating a Part To Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.FINE);
    sayOK();
  }

  /**
   * Retesselate the CAD Part to Very Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToVeryFine(CadPart part) {
    printAction("ReTesselating a Part To Very Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.VERY_FINE);
    sayOK();
  }

  private void reTesselateCadPart(CadPart part, int type) {
    sayPart(part);
    part.getTessellationDensityOption().setSelected(type);
    part.getCadPartEdgeOption().setSelected(CadPartEdgeOption.SHARP_EDGES);
    part.setSharpEdgeAngle(mshSharpEdgeAngle);
    part.tessellate();
    sayOK();
  }

  /**
   * Parse CSV lines.
   *
   * @param data String[]
   * @param i which column
   * @return double[] of data
   */
  private double[] retValsOnColumn(String[] data, int i) {
    ArrayList<Double> ard = new ArrayList<Double>();
    Double val;
    for (String line : data) {
        if (line.contains("\"")) {
            continue;
        }
        String[] cols = line.split(",");
        try {
            val = new Double(cols[i]);
        } catch (NumberFormatException e) {
            continue;
        }
        ard.add(val);
    }
    //say(ard.toString());
    return retDouble(ard.toArray(new Double[ard.size()]));
  }

  /**
   * Returns whether the absolute difference between 2 doubles is within the tolerance.
   */
  private boolean retDiff(double d1, double d2, double tol) {
    if (Math.abs(d1 - d2) <= tol) {
        return true;
    }
    return false;
  }

  /**
   * Converts ArrayList<Double> to double[].
   * @param array given ArrayList<Double>.
   * @return double[]
   */
  private double[] retDouble(ArrayList<Double> alr) {
    return retDouble(alr.toArray(new Double[alr.size()]));
  }

  /**
   * Converts double[] to Double[].
   * @param array given double[]
   * @return Double[]
   */
  private Double[] retDouble(double[] array) {
    Double[] nd = new Double[array.length];
    for (int i = 0; i < array.length; i++) {
        nd[i] = array[i];
    }
    return nd;
  }

  /**
   * Converts Double[] to double[].
   * @param array given Double[]
   * @return double[]
   */
  private double[] retDouble(Double[] array) {
    double[] nd = new double[array.length];
    for (int i = 0; i < array.length; i++) {
        nd[i] = array[i];
    }
    return nd;
  }

  /** Returns whether the relative error between 2 doubles is within a tolerance. */
  private boolean retError(double d1, double d2, double tol) {
    double diff = d1 - d2;
    double div = d2;
    if (diff == 0.) {
        div = 1;                // Any number
    } else if (d1*d2 == 0.) {
        div = d1 + d2;          // Any of the two
    }
    double error = diff/div;
    //say("d1 = " + d1);
    //say("d2 = " + d2);
    //say("Error: " + error);
    if (Math.abs(error) <= tol) {
        return true;
    }
    return false;
  }

  /**
   * @param files String[] {}
   * @return ArrayList<File>
   */
  private ArrayList<File> retFiles(String[] files) {
    ArrayList<File> af = new ArrayList<File>();
    for (String file : files) {
        af.add(new File(simPath, file));
    }
    return af;
  }

  private String retFilenameExtension(String filename) {
    //----------------------------------------------
    //-- Copied from Apache FilenameUtils method.
    //----------------------------------------------
    if (filename == null) {
        return null;
    }
    int index = retIndexOfExtension(filename);
    if (index == -1) {
        return "";
    } else {
        return filename.substring(index + 1);
    }
  }

  private DoubleVector retIncrement(DoubleVector dv1, DoubleVector dv2, int step, int totSteps) {
    DoubleVector dv = (DoubleVector) dv1.clone();
    for (int i = 0; i < dv.size(); i++) {
        double d1 = dv1.get(i);
        double delta = (dv2.get(i) - dv1.get(i)) / totSteps;
        dv.setElementAt(d1 + step * delta, i);
    }
    return dv;
  }

  private double retIncrement(double d1, double d2, int step, int totSteps) {
    return (d1 + step * (d2 - d1) / totSteps);
  }

  private int retIndexOfExtension(String filename) {
    //----------------------------------------------
    //-- Copied from Apache FilenameUtils method.
    //----------------------------------------------
    if (filename == null) return -1;
    int extensionPos = filename.lastIndexOf(".");
    int lastSeparator = retIndexOfLastSeparator(filename);
    return (lastSeparator > extensionPos ? -1 : extensionPos);
  }

  private int retIndexOfLastSeparator(String filename) {
    //----------------------------------------------
    //-- Copied from Apache FilenameUtils method.
    //----------------------------------------------
    char UNIX_SEPARATOR = '/';
    char WINDOWS_SEPARATOR = '\\';
    if (filename == null) return -1;
    int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
    int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
    return Math.max(lastUnixPos, lastWindowsPos);
   }

  private String retMinTargetString(double min, double tgt) {
    //String minS = String.format("%g%%", min);
    return retMinMaxString("Min/Target", "%.2f%%", min, tgt);
  }

  private String retMinMaxString(double min, double max) {
    return retMinMaxString("Min/Max", "%g", min, max);
  }

  private String retMinMaxString(String baseString, String numFmt, double min, double max) {
    String base = String.format("%s = %s/%s", baseString, numFmt, numFmt);
    return String.format(base, min, max);
  }

  private int retNumberOfParentParts(ArrayList<PartSurface> aps) {
    ArrayList<GeometryPart> agp = new ArrayList<GeometryPart>();
    for (PartSurface ps : aps) {
        if (!agp.contains(ps.getPart())) {
            agp.add(ps.getPart());
        }
    }
    return agp.size();
  }

  /**
   * Project coarser data into finer Grid. Data must be sorted first.
   *
   * @param xx0 double[] of finer x values
   * @param xx1 double[] of coarser x values
   * @param yy1 double[] of coarser y values
   * @return double[] of projected coarser y values into finer x
   */
  private double[] retProjected(double[] xx0, double[] xx1, double[] yy1) {
    double[] yy1p = new double[xx0.length];
    //say("yy1p length " + yy1p.length);
    //say("xx1 length " + xx1.length);
    for (int i = 0; i < xx0.length; i++) {
        Double x0 = xx0[i];
        double[] range = retRange(x0, xx1);
        //say(String.format("### Got Range: {%6.2f  //  %6.2f  //   %6.2f}", range[0], x0, range[1]));
        int i0 = Arrays.asList(retDouble(xx1)).indexOf(range[0]);
        int i1 = Arrays.asList(retDouble(xx1)).indexOf(range[1]);
        yy1p[i] = evalLinearRegression(new double[] {range[0], range[1]}, new double[] {yy1[i0], yy1[i1]}, x0, false, false);
    }
    return yy1p;
  }

  /**
   * Return the coarser data pair in range to the finer given point. Assumes sorted data.
   *
   * @param x0 finer point.
   * @param xx coarser points.
   * @return double[] range data pair.
   */
  private double[] retRange(double x0, double[] xx) {
    //-- Eliminate Last occurrence.
    for (int i = 0; i < xx.length - 1; i++) {
        double x1 = xx[i];
        double x2 = xx[i+1];
        //say(String.format("   Working Range (i=%2d): %6.2f <= %6.2f <= %6.2f ???", i, x1, x0, x2));
        if (x0 >= x1 && x0 <= x2) {
            return new double[] {x1, x2};
        }
        if (x0 >= xx[xx.length -1]) {
            return new double[] {xx[xx.length -2], xx[xx.length -1]};
        }
    }
    return new double[] {xx[0], xx[1]};
  }

  private String retRepeatString(String str0, int times) {
    return new String(new char[times]).replace("\0", str0);
  }

  //-- This variable is necessary for the Spline methods. Do not touch it.
  private int last_interval;
  //--
  private double[][] retSpline(ArrayList<Double> vecXX, ArrayList<Double> vecFF) {
    //----------------------------------------------------------------
    //-- CREDITS
    //----------------------------------------------------------------
    //--
    //-- This method was downloaded on December, 2012, from a website
    //-- belonging to Dr. Jon Squire, Adjunct Faculty
    //-- http://www.csee.umbc.edu/~squire/
    //--
    //-- It was quickly adapted to fit into this Macro.
    //--
    //----------------------------------------------------------------
    //--
    //-- Trick converting ArrayList<Double> to double[]
    double[] xx = new double[vecXX.size()];
    double[] ff = new double[vecFF.size()];
    for (int i = 0; i < vecFF.size(); i++) {
        xx[i] = vecXX.get(i);
        ff[i] = vecFF.get(i);
    }
    //--
    int n = vecXX.size() ;
    double fp1, fpn, h, p;
    final double zero = 0.0, two = 2.0, three = 3.0;
    boolean uniform = true;
    boolean debug = false;
    last_interval = 0;
    double[] x = new double[n];
    double[] f = new double[n];
    double[] b = new double[n];
    double[] c = new double[n];
    double[] d = new double[n];
    for (int i=0; i<n; i++) {
      x[i] = xx[i];
      f[i] = ff[i];
      if (debug) say("Spline data x["+i+"]="+x[i]+", f[]="+f[i]);
    }
    //-- Calculate coefficients for the tri-diagonal system: store
    //-- sub-diagonal in b, diagonal in d, difference quotient in c.
    b[0] = x[1]-x[0];
    c[0] = (f[1]-f[0])/b[0];
    d[0] = two*b[0];
    for (int i=1; i<n-1; i++) {
       b[i] = x[i+1]-x[i];
       if (Math.abs(b[i]-b[0])/b[0]>1.0E-5) uniform = false;
       c[i] = (f[i+1]-f[i])/b[i];
       d[i] = two*(b[i]+b[i-1]);
    }
    d[n-1] = two*b[n-2];
    //-- Calculate estimates for the end slopes.  Use polynomials
    //-- interpolating data nearest the end.
    fp1 = c[0]-b[0]*(c[1]-c[0])/(b[0]+b[1]);
    if (n>3) fp1 = fp1+b[0]*((b[0]+b[1])*(c[2]-c[1])/
                  (b[1]+b[2])-c[1]+c[0])/(x[3]-x[0]);
    fpn = c[n-2]+b[n-2]*(c[n-2]-c[n-3])/(b[n-3]+b[n-2]);
    if (n>3) fpn = fpn+b[n-2]*(c[n-2]-c[n-3]-(b[n-3]+
                  b[n-2])*(c[n-3]-c[n-4])/(b[n-3]+b[n-4]))/(x[n-1]-x[n-4]);
    //--
    //-- Calculate the right-hand-side and store it in c.
    c[n-1] = three*(fpn-c[n-2]);
    for (int i=n-2; i>0; i--)
       c[i] = three*(c[i]-c[i-1]);
    c[0] = three*(c[0]-fp1);
    //--
    //-- Solve the tridiagonal system.
    for (int k=1; k<n; k++) {
       p = b[k-1]/d[k-1];
       d[k] = d[k]-p*b[k-1];
       c[k] = c[k]-p*c[k-1];
    }
    c[n-1] = c[n-1]/d[n-1];
    for (int k=n-2; k>=0; k--)
       c[k] = (c[k]-b[k]*c[k+1])/d[k];
    //--
    //-- Calculate the coefficients defining the spline.
    h = x[1]-x[0];
    for (int i=0; i<n-1; i++) {
       h = x[i+1]-x[i];
       d[i] = (c[i+1]-c[i])/(three*h);
       b[i] = (f[i+1]-f[i])/h-h*(c[i]+h*d[i]);
    }
    b[n-1] = b[n-2]+h*(two*c[n-2]+h*three*d[n-2]);
    if (debug) say("spline coefficients");
    return new double[][] {f, x, b, c, d};
  }

  private double retSplineValue(double[][] splineCoeffs, double t) {
    int interval; // index such that t>=x[interval] and t<x[interval+1]
    double[] f = splineCoeffs[0];
    double[] x = splineCoeffs[1];
    double[] b = splineCoeffs[2];
    double[] c = splineCoeffs[3];
    double[] d = splineCoeffs[4];
    int n = f.length;
    //-- Search for correct interval for t.
    interval = last_interval; // heuristic
    if (t>x[n-2])
       interval = n-2;
    else if (t >= x[last_interval])
       for (int j=last_interval; j<n&&t>=x[j]; j++) interval = j;
    else
       for (int j=last_interval; t<x[j]; j--) interval = j-1;
    last_interval = interval; // class variable for next call
    //-- Evaluate cubic polynomial on [x[interval] , x[interval+1]].
    double dt = t-x[interval];
    double s = f[interval]+dt*(b[interval]+dt*(c[interval]+dt*d[interval]));
    return s;
  }

  private String retStringBetweenBrackets(String text) {
    java.util.regex.Pattern patt = java.util.regex.Pattern.compile(".*\\[(.*)\\]");
    java.util.regex.Matcher matcher = patt.matcher(text);
    boolean matchFound = matcher.find();
    if (matchFound) {
        //say(matcher.group(1));
        return matcher.group(1);
        //for (int i=0; i<=matcher.groupCount(); i++) {
        //    String groupStr = matcher.group(i);
        //    say(matcher.group(i));
        //}
    }
    return noneString;
  }

  private String retStringBoundaryAndRegion(Boundary b) {
    Region r = b.getRegion();
    return r.getPresentationName() + "\\" + b.getPresentationName();
  }

  private String retString(double[] array, String fmt) {
    ArrayList<String> als = new ArrayList();
    for (int i = 0; i < array.length; i++) {
        als.add(String.format(fmt, array[i]));
    }
    return retStringBetweenBrackets(als.toString());
  }

  private String retString(double[] array) {
    String strng = "" + array[0];
    for (int i = 1; i < array.length; i++) {
        strng += ", " + array[i];
    }
    return strng;
  }

  private String retTemp(double T) {
    return "Temperature: " + T + defUnitTemp.getPresentationName();
  }

  /**
   * Runs the case.
   */
  public void runCase() {
    runCase(0, false);
  }

  /**
   * Runs or Steps the case for the given number of iterations.
   *
   * @param n If n > 0: step n iterations; If n == 0: just run.
   */
  public void runCase(int n) {
    runCase(n, false);
  }

  /**
   * Runs or Steps the case for the given number of iterations.
   *
   * @param n If n > 0: step n iterations; If n == 0: just run.
   * @param prettifyOpt Prettify me before running?
   */
  public void runCase(int n, boolean prettifyOpt) {
    printAction("Running the case");
    if (!hasValidVolumeMesh()) {
        say("No volume mesh found. Skipping run.");
        return;
    }
    setAbortStoppingCriteria(true);
    //hardCopyPicturesAtInitialization();
    updateViewFactors();
    if (prettifyOpt) {
        prettifyMe(false);
    }
    if (n > 0) {
        say("Running " + n + " iterations of the case");
        sayCellCount(true);
        sim.getSimulationIterator().step(n);
    } else {
        if (isUnsteady()) {
            ((StepStoppingCriterion) getStoppingCriteria("Maximum Steps.*", false)).setIsUsed(false);
        }
        sayCellCount(true);
        sim.getSimulationIterator().run();
    }
  }

  /**
   * Runs the case.
   *
   * @param prettifyOpt Prettify me before running?
   */
  public void runCase(boolean prettifyOpt) {
    runCase(0, prettifyOpt);
  }

  /**
   * Runs the case.
   *
   * @param prettifyOpt Prettify me before running?
   * @param normOffOpt Disable Normalization of the Residual Monitors?
   */
  public void runCase(boolean prettifyOpt, boolean normOffOpt) {
    if (normOffOpt) {
        setMonitorsNormalizationOFF();
    }
    runCase(prettifyOpt);
  }

  /**
   * Print something to output/log file.
   *
   * @param msg message to be printed.
   */
  public void say(String msg) {
    say(msg, true);
  }

  /**
   * Print something to output/log file using a {@see String#format} syntax.
   *
   * @param format message to be printed using.
   * @param args arguments used in the format.
   * @see  java.lang.String
   * @see  java.util.Formatter
   */
  public void say(String format, Object ... args) {
    say(true, format, args);
  }

  //-- This is the only method that verboseOptions comes first when calling it.
  private void say(boolean verboseOption, String format, Object ... args) {
    String msg = new Formatter().format(format, args).toString();
    say(msg, verboseOption);
  }

  /**
   * Print something to output/log file.
   *
   * @param msg message to be printed.
   * @param verboseOption Print to standard output?
   */
  public void say(String msg, boolean verboseOption) {
    if (!verboseOption) { return; }
    sim.println(sayPreffixString + " " + msg);
  }

  private void sayAnswerNo(boolean verboseOption) {
    say("  NO", verboseOption);
  }

  private void sayAnswerYes(boolean verboseOption) {
    say("  YES", verboseOption);
  }

  private void sayBdry(Boundary b) {
    sayBdry(b, true);
  }

  private void sayBdry(Boundary b, boolean verboseOption) {
    sayBdry(b, verboseOption, false);
  }

  private void sayBdry(Boundary b, boolean verboseOption, boolean beanDisplayOption) {
    String bb = b.getPresentationName();
    String rr = b.getRegion().getPresentationName();
    say("Boundary: " + bb + "\t[Region: " + rr + "]", verboseOption);
    if (!beanDisplayOption) {
        return;
    }
    say("Bean Display Name is \"" + b.getBeanDisplayName() + "\".", verboseOption);
  }

  private void sayBoundaries(ArrayList<Boundary> boundaries) {
    sayBoundaries(boundaries, true);
  }

  private void sayBoundaries(ArrayList<Boundary> boundaries, boolean verboseOption) {
    say("Number of Boundaries: " + boundaries.size(), verboseOption);
    for (Boundary b : boundaries) {
        sayBdry(b, verboseOption, false);
    }
  }

  /**
   * Prints an overview of the Camera View.
   *
   * @param cam given Camera View.
   */
  public void sayCamera(VisView cam) {
    sayCamera(cam, true);
  }

  private void sayCamera(VisView cam, boolean verboseOption) {
    say("Camera Overview: " + cam.getPresentationName(), verboseOption);
    say("   Focal Point: " + retStringBetweenBrackets(cam.getFocalPoint().toString()), verboseOption);
    say("   Position: " + retStringBetweenBrackets(cam.getPosition().toString()), verboseOption);
    say("   View Up: " + retStringBetweenBrackets(cam.getViewUp().toString()), verboseOption);
    say("   Parallel Scale: " + cam.getParallelScale(), verboseOption);
    say("   Projection Mode: " + cam.getProjectionMode(), verboseOption);
    say("", verboseOption);
  }

  private void sayContinua(Continuum continua, boolean verboseOption) {
    String contName = "Physics";
    if (isMeshContinua(continua)) {
        contName = "Mesh";
    }
    say(contName + " Continua: " + continua.getPresentationName(), verboseOption);
  }

  private void sayCellCount(boolean verboseOption) {
    if (hasValidVolumeMesh()) {
        say(String.format("Cell Count: %,d", queryVolumeMesh().getCellCount()), verboseOption);
        say(String.format("Face Count: %,d", queryVolumeMesh().getInteriorFaceCount()), verboseOption);
        say(String.format("Vertex Count: %,d", queryVolumeMesh().getVertexCount()), verboseOption);
    }
  }

  /**
   * Prints a small info on the Displayer.
   *
   * @param d given Displayer.
   */
  public void sayDisplayer(Displayer d) {
    String className = d.getClass().getName().replace("star.vis.", "").replace("Disp", " Disp");
    String dispName = d.getPresentationName();
    String scnName = d.getScene().getPresentationName();
    String suffix = ".";
    ScalarDisplayQuantity sdq = getScalarDisplayQuantity(d, false);
    VectorDisplayQuantity vdq = getVectorDisplayQuantity(d, false);
    if (sdq != null) {
        suffix = String.format(" is showing '%s'.", sdq.getFieldFunction().getPresentationName());
    } else if (vdq != null) {
        suffix = String.format(" is showing '%s'.", vdq.getFieldFunction().getPresentationName());
    }
    say("%s '%s' on Scene '%s'%s", className, dispName, scnName, suffix);
  }

  /**
   * Says something in a fancy frame. This is the same as {@see #printAction}.
   *
   * @param text message to be said.
   */
  public void sayFrame(String text) {
    printAction(text, true);
  }

  private void sayFieldFunction(FieldFunction f, boolean verboseOption) {
    say("Field Function: " + f.getPresentationName(), verboseOption);
    say("   Function Name: " + f.getFunctionName(), verboseOption);
  }

  private void sayInterface(Interface i) {
    sayInterface(i, true, true);
  }

  private void sayInterface(Interface i, boolean verboseOption, boolean beanDisplayOption) {
    say("Interface: " + i.getPresentationName(), verboseOption);
    if (beanDisplayOption) {
        say("Bean Display Name is \"" + i.getBeanDisplayName() + "\".", verboseOption);
    }
    sayInterfaceSides(i, verboseOption);
  }

  /**
   * Prints both sides of an Interface.
   *
   * @param i given Interface.
   */
  public void sayInterfaceSides(Interface i) {
    sayInterfaceSides(i, true);
  }

  private void sayInterfaceSides(Interface i, boolean verboseOption) {
    String side1 = null, side2 = null;
    if (isDirectBoundaryInterface(i)) {
        side1 = retStringBoundaryAndRegion(((DirectBoundaryInterface) i).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((DirectBoundaryInterface) i).getParentBoundary1());
    }
    if (isIndirectBoundaryInterface(i)) {
        side1 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) i).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) i).getParentBoundary1());
    }
    say("  Side1: " + side1, verboseOption);
    say("  Side2: " + side2, verboseOption);
  }

  /**
   * Print something in Capital Letters.
   *
   * @param msg message to be said.
   */
  public void sayLoud(String msg) {
    sayLoud(msg, false);
  }

  private void sayLoud(String msg, boolean showTime) {
    printLine("#", true);
    printLine("#", true);
    say("#  " + msg.toUpperCase(), true);
    if (showTime) say("#  " + getTime(), true);
    printLine("#", true);
    printLine("#", true);
  }

  private void sayPrismsParameters(int numLayers, double stretch, double relSize, boolean verboseOption) {
    say("  Number of Layers: " + numLayers, verboseOption);
    say("  Stretch Factor: " + String.format("%.2f",stretch), verboseOption);
    say("  Height Relative Size: " + String.format("%.2f%%", relSize), verboseOption);
  }

  private void sayMeshOp(MeshOperation mo, boolean verboseOption) {
    say("Mesh Operation: " + mo.getPresentationName(), verboseOption);
  }

  /**
   * Says the Presentation Name for a given object.
   *
   * @param no given STAR-CCM+ NamedObject.
   */
  public void sayNamedObject(NamedObject no) {
    sayNamedObject(no, true);
  }

  private void sayNamedObject(NamedObject no, boolean verboseOption) {
    if (no == null) {
        say("Named Object is NULL", verboseOption);
        return;
    }
    say(verboseOption, "%s name: %s", getParentName(no), no.getPresentationName());
  }

  private void sayNamedObjects(ArrayList<NamedObject> ano, boolean verboseOption) {
    say("Number of NamedObjects: " + ano.size(), verboseOption);
    for (NamedObject n : ano) {
        say("  " + n.getPresentationName(), verboseOption);
    }
  }

  /**
   * It's OK!
   */
  public void sayOK() {
    sayOK(true);
  }

  /**
   * It's OK!
   *
   * @param verboseOption Print to standard output?
   */
  public void sayOK(boolean verboseOption) {
    say("OK!\n", verboseOption);
  }

  private void sayOldNameNewName(String name, String newName) {
    say("  Old name: " + name);
    say("  New name: " + newName);
    say("");
  }

  private void sayPart(GeometryPart gp) {
    sayPart(gp, true);
  }

  private String sayPart(GeometryPart gp, boolean verboseOption) {
    String toSay = "Part: ";
    if (isCadPart(gp)) {
        toSay += ((CadPart) gp).getPathInHierarchy();
    }
    if (isLeafMeshPart(gp)) {
        toSay += ((LeafMeshPart) gp).getPathInHierarchy();
    }
    if (isSimpleBlockPart(gp)) {
        toSay += ((SimpleBlockPart) gp).getPathInHierarchy();
    }
    if (isSolidModelPart(gp)) {
        toSay += ((SolidModelPart) gp).getPathInHierarchy();
    }
    say(toSay, verboseOption);
    return toSay;
  }

  private void sayParts(ArrayList<GeometryPart> ag, boolean verboseOption) {
    say("Number of Parts: " + ag.size(), verboseOption);
    for (GeometryPart gp : ag) {
        say("  " + sayPart(gp, false), verboseOption);
    }
  }

  private void sayPartSurface(PartSurface ps) {
    sayPartSurface(ps, true);
  }

  private void sayPartSurface(PartSurface ps, boolean verboseOption) {
    say("Part Surface: " + ps.getPresentationName(), verboseOption);
    if (isCadPart(ps.getPart())) {
        say("CAD Part: " + ((CadPart) ps.getPart()).getPathInHierarchy(), verboseOption);
    }
    if (isLeafMeshPart(ps.getPart())) {
        say("Leaf Mesh Part: " + ((LeafMeshPart) ps.getPart()).getPathInHierarchy(), verboseOption);
    }
  }

  private void sayPlot(StarPlot sp, boolean verboseOption) {
    say("Plot: " + sp.getPresentationName(), verboseOption);
  }

  private void sayRegion(Region r) {
    sayRegion(r, true);
  }

  private void sayRegion(Region r, boolean verboseOption) {
    say("Region: " + r.getPresentationName(), verboseOption);
  }

  private void sayRegions(ArrayList<Region> regions) {
    say("Number of Regions: " + regions.size());
    for (Region reg : regions) {
        say("  " + reg.getPresentationName());
    }
  }

  private void sayScene(Scene scn, boolean verboseOption) {
    say("Scene: " + scn.getPresentationName(), verboseOption);
  }

  private void saySimName(boolean verboseOption) {
    say("Simulation Name: " + sim.getPresentationName(), verboseOption);
  }

  private void sayString(String strng, boolean verboseOption) {
    say(String.format("String: \"%s\"", strng), verboseOption);
  }

  /**
   * Prints the current STAR-CCM+ version.
   */
  public void sayVersion() {
    String version = getVersion();
    say("Version: " + version);
    //String[] versionDetails = version.split("\\.");
    //int IV = Integer.parseInt(versionDetails[0]) * 100 + Integer.parseInt(versionDetails[1]);
    //say("Version: " + IV);
  }

  private void sayWarning(String ... messages) {
    say("WARNING!!!");
    for (int i = 0; i < messages.length; i++) {
        say("   " + messages[i]);
    }
  }

  /**
   * Saves the simulation file using the current name.
   */
  public void saveSim() {
    saveSim(sim.getPresentationName());
  }

  /**
   * Saves the simulation file using a custom name.
   *
   * The {@see #simTitle} variable will be updated.
   *
   * @param name given name.
   */
  public void saveSim(String name) {
    saveSim(name, true);
  }

  /**
   * Saves the simulation file using a custom name.
   *
   * @param updSimTitle update the {@see #simTitle} variable?
   * @param name given name.
   */
  public void saveSim(String name, boolean updSimTitle) {
    String newName = name + ".sim";
    printAction("Saving: " + newName);
    sim.saveState(new File(simPath, newName).toString());
    if (updSimTitle) {
        simTitle = sim.getPresentationName();
    }
  }

  /**
   * Saves the simulation file appending a suffix. <p>
   *
   * The basic name is given using the {@see #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   */
  public void saveSimWithSuffix(String suffix) {
    if (!saveIntermediates) { return; }
    String newName = simTitle + "_" + suffix;
    saveSim(newName, false);
    savedWithSuffix++;
  }

  /**
   * Saves the simulation file appending a suffix with the option to force saving
   * intermediate files. <p>
   *
   * The basic name is given using the {@see #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   * @param forceOption save intermediate simulation files as well?
   *                    Depends on {@see #saveIntermediates}
   */
  public void saveSimWithSuffix(String suffix, boolean forceOption) {
    boolean interm = saveIntermediates;
    if (forceOption) {
        saveIntermediates = true;
    }
    saveSimWithSuffix(suffix);
    saveIntermediates = interm;
  }

  /**
   * Enables/Disables the STOP FILE (<i>ABORT</i>) stopping criteria.
   *
   * @param toggle True or False. Default is True (enabled).
   */
  public void setAbortStoppingCriteria(boolean toggle) {
    SolverStoppingCriterion stp = getStoppingCriteria("Stop File.*", false);
    if (stp == null) {
        return;
    }
    ((AbortFileStoppingCriterion) stp).setIsUsed(toggle);
  }

  /**
   * Set the Auto Save functionality.
   *
   * @param ue given Update Event.
   * @param maxSavedFiles how many saved sim files to keep?
   */
  public void setAutoSave(UpdateEvent ue, int maxSavedFiles) {
    printAction("Setting Auto Save Options");
    say("Update Event: " + ue.getPresentationName());
    AutoSave as = sim.getSimulationIterator().getAutoSave();
    as.setMaxAutosavedFiles(maxSavedFiles);
    StarUpdate su = as.getStarUpdate();
    su.setEnabled(true);
    su.getUpdateModeOption().setSelected(StarUpdateModeOption.EVENT);
    as.getStarUpdate().getEventUpdateFrequency().setUpdateEvent(ue);
    sayOK();
  }

  /**
   * Set the Auto Save functionality. If the simulation is unsteady, the parameters are based on
   * the Physical time.
   *
   * @param maxSavedFiles how many saved sim files to keep?
   * @param startAt given start of savings. Iterations when Steady, Time in
   *    {@see #defUnitTime} when unsteady.
   * @param atEvery saving frequency. Iterations when Steady, Time in
   *    {@see #defUnitTime} when unsteady.
   */
  public void setAutoSave(int maxSavedFiles, double startAt, double atEvery) {
    setAutoSave(maxSavedFiles, startAt, atEvery, true);
  }

  private void setAutoSave(int maxSavedFiles, double startAt, double atEvery, boolean verboseOption) {
    printAction("Setting Auto Save Options", verboseOption);
    AutoSave as = sim.getSimulationIterator().getAutoSave();
    as.setMaxAutosavedFiles(maxSavedFiles);
    StarUpdate su = as.getStarUpdate();
    su.setEnabled(true);
    if (isUnsteady()) {
        say(verboseOption, "Start at: %g (%s)", startAt, defUnitTime.getPresentationName());
        say(verboseOption, "At Every: %g (%s)", atEvery, defUnitTime.getPresentationName());
        su.getUpdateModeOption().setSelected(StarUpdateModeOption.DELTATIME);
        DeltaTimeUpdateFrequency dtUpd = su.getDeltaTimeUpdateFrequency();
        dtUpd.getStartQuantity().setValue(startAt);
        dtUpd.getStartQuantity().setUnits(defUnitTime);
        dtUpd.getDeltaTime().setValue(atEvery);
        dtUpd.getDeltaTime().setUnits(defUnitTime);
    } else {
        say(verboseOption, "Start at: %d", startAt);
        say(verboseOption, "At Every: %d", atEvery);
        su.getUpdateModeOption().setSelected(StarUpdateModeOption.ITERATION);
        IterationUpdateFrequency iUpd = su.getIterationUpdateFrequency();
        iUpd.setStart((int) startAt);
        iUpd.setIterations((int) atEvery);
    }
    sayOK(verboseOption);
  }

  /**
   * Sets the Wall Boundary as Adiabatic.
   *
   * @param b given Boundary.
   */
  public void setBC_AdiabaticWall(Boundary b) {
    printAction("Setting BC as Adiabatic Wall");
    sayBdry(b);
    b.setBoundaryType(WallBoundary.class);
    b.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.ADIABATIC);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Constant Temperature.
   *
   * @param b given Boundary.
   * @param T given Temperature in default units. See {@see #defUnitTemp}.
   */
  public void setBC_ConstantTemperatureWall(Boundary b, double T) {
    printAction("Setting BC as Constant Temperature Wall", b, true);
    b.setBoundaryType(WallBoundary.class);
    b.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.TEMPERATURE);
    setBC_Values(b, "Static Temperature", T, defUnitTemp, false);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Convection Heat Transfer type.
   *
   * @param b given Boundary.
   * @param T given Ambient Temperature in default units. See {@see #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@see #defUnitHTC}.
   */
  public void setBC_ConvectionWall(Boundary b, double T, double htc) {
    setBC_ConvectionWall(b, T, htc, true);
  }

  /**
   * Sets the Wall Boundary as Environment Heat Transfer type.
   *
   * @param b given Boundary.
   * @param T given Ambient Temperature in default units. See {@see #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@see #defUnitHTC}.
   * @param emissivity given Emissivity dimensionless.
   * @param transmissivity given Transmissivity dimensionless.
   * @param externalEmissivity given External Emissivity dimensionless.
   * @deprecated in v3.2. Needs refactoring. Radiation is gone.
   */
  public void setBC_EnvironmentWall(Boundary b, double T, double htc, double emissivity,
                                                double transmissivity, double externalEmissivity) {
    printAction("Setting BC as an Environment Wall", b, true);
    setBC_ConvectionWall(b, T, htc, false);
    if (hasRadiationBC(b)) {
        //say("  External Emissivity: " + externalEmissivity);
        ////setRadiationParametersS2S(b, emissivity, transmissivity);
        b.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.ENVIRONMENT);
        ExternalEmissivityProfile eemP = b.getValues().get(ExternalEmissivityProfile.class);
        eemP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(externalEmissivity);
    } else {
        say("  Radiation Settings not available. Skipping...");
        return;
    }
    printLine(3);
    say("Environment BC set.");
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Free Slip.
   *
   * @param b given Boundary.
   */
  public void setBC_FreeSlipWall(Boundary b) {
    printAction("Setting BC as a Free Slip Wall", b, true);
    b.setBoundaryType(WallBoundary.class);
    b.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.SLIP);
    sayOK();
  }

  /**
   * Applies a Field Function in a Physics Value of a Boundary.
   *
   * <b>Note:</b> Current method is limited to Scalar values only.
   *
   * @param b given boundary.
   * @param name given Physics Values. E.g.: Pressure, Temperature, etc...
   * @param f given Field Function.
   */
  public void setBC_FieldFunction(Boundary b, String name, FieldFunction f) {
    printAction("Applying a Field Function on a Boundary", b, true);
    say("Field Function used at '%s' is '%s'...",
            b.getPresentationName(), f.getPresentationName());
    ScalarProfile sp = getScalarProfile(b, name, true);
    sp.setMethod(FunctionScalarProfileMethod.class);
    sp.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(f);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Free Stream.
   *
   * @param b given Boundary.
   * @param dir given 3-components direction of the flow. E.g., in X: {1, 0, 0}.
   * @param mach given Mach number.
   * @param P given Pressure in default units. See {@see #defUnitPress}.
   * @param T given Static Temperature in default units. See {@see #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_FreeStream(Boundary b, double[] dir, double mach, double P, double T,
                                                                        double ti, double tvr) {
    printAction("Setting BC as a Free Stream", b, true);
    b.setBoundaryType(FreeStreamBoundary.class);
    setBC_Values(b, "Flow Direction", dir, unit_Dimensionless, false);
    setBC_Values(b, "Mach Number", mach, unit_Dimensionless, false);
    setBC_Values(b, "Pressure", P, defUnitPress, false);
    setBC_Values(b, "Static Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Turbulence Intensity", ti, unit_Dimensionless, false);
    setBC_Values(b, "Turbulent Viscosity Ratio", tvr, unit_Dimensionless, false);
    sayOK();
  }

  /**
   * Sets a Boundary as Mass Flow Rate Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param b given Boundary.
   * @param mfr given Mass Flow Rate in default units. See {@see #defUnitMFR}.
   * @param T given Static Temperature in default units. See {@see #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_MassFlowRateInlet(Boundary b, double mfr, double T, double ti, double tvr) {
    printAction("Setting BC as Mass Flow Rate Inlet", b, true);
    b.setBoundaryType(MassFlowBoundary.class);
    setBC_Values(b, "Mass Flow Rate", mfr, defUnitMFR, false);
    setBC_Values(b, "Total Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Turbulence Intensity", ti, unit_Dimensionless, false);
    setBC_Values(b, "Turbulent Viscosity Ratio", tvr, unit_Dimensionless, false);
    sayOK();
  }

  /**
   * Sets a Boundary as Pressure Outlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param b given Boundary.
   * @param P given Static Pressure in default units. See {@see #defUnitTemp}.
   * @param T given Static Temperature in default units. See {@see #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_PressureOutlet(Boundary b, double P, double T, double ti, double tvr) {
    printAction("Setting BC as Pressure Outlet", b, true);
    b.setBoundaryType(PressureBoundary.class);
    setBC_Values(b, "Static Pressure", P, defUnitPress, false);
    setBC_Values(b, "Static Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Turbulence Intensity", ti, unit_Dimensionless, false);
    setBC_Values(b, "Turbulent Viscosity Ratio", tvr, unit_Dimensionless, false);
    sayOK();
  }

  /**
   * Sets a Boundary as Stagnation Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param b given Boundary.
   * @param P given Total Pressure in default units. See {@see #defUnitPress}.
   * @param T given Total Temperature in default units. See {@see #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_StagnationInlet(Boundary b, double P, double T, double ti, double tvr) {
    printAction("Setting BC as Stagnation Inlet", b, true);
    b.setBoundaryType(StagnationBoundary.class);
    setBC_Values(b, "Total Pressure", P, defUnitPress, false);
    setBC_Values(b, "Total Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Turbulence Intensity", ti, unit_Dimensionless, false);
    setBC_Values(b, "Turbulent Viscosity Ratio", tvr, unit_Dimensionless, false);
    sayOK();
  }

  /**
   * Sets a Boundary as Symmetry.
   *
   * @param b given Boundary.
   */
  public void setBC_Symmetry(Boundary b) {
    printAction("Setting BC as Symmetry", b, true);
    b.setBoundaryType(SymmetryBoundary.class);
    sayOK();
  }

  /**
   * Sets a Boundary as Velocity Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param b given Boundary.
   * @param vel given Velocity Magnitude in default units. See {@see #defUnitVel}.
   * @param T given Static Temperature in default units. See {@see #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_VelocityMagnitudeInlet(Boundary b, double vel, double T,
                                                                double ti, double tvr) {
    printAction("Setting BC as Velocity Inlet", b, true);
    b.setBoundaryType(InletBoundary.class);
    setBC_Values(b, "Velocity Magnitude", vel, defUnitVel, false);
    setBC_Values(b, "Static Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Turbulence Intensity", ti, unit_Dimensionless, false);
    setBC_Values(b, "Turbulent Viscosity Ratio", tvr, unit_Dimensionless, false);
    sayOK();
  }

  private void setBC_ConvectionWall(Boundary b, double T, double htc, boolean verboseOption) {
    printAction("Setting BC as Convection Wall", verboseOption);
    sayBdry(b, verboseOption);
    b.setBoundaryType(WallBoundary.class);
    b.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.CONVECTION);
    setBC_Values(b, "Ambient Temperature", T, defUnitTemp, false);
    setBC_Values(b, "Heat Transfer Coefficient", htc, defUnitHTC, false);
    sayOK(verboseOption);
  }

  /**
   * Applies a Physics Value to a Boundary.
   *
   * <b>Note:</b> Current method is limited to Constant Scalar values only.
   *
   * @param b given boundary.
   * @param name given Physics Values. E.g.: Pressure, Temperature, etc...
   * @param val given value.
   * @param u given units.
   */
  public void setBC_Values(Boundary b, String name, double val, Units u) {
    setBC_Values(b, name, val, u, true);
  }

  private void setBC_Values(Boundary b, String name, double val, Units u, boolean verboseOption) {
    printAction("Setting a Constant Boundary Value", b, verboseOption);
    ScalarProfile sp = getScalarProfile(b, name, verboseOption);
    setProfile(sp, val, u);
    sayOK(verboseOption);
  }

  /**
   * Applies Physics Values to a Boundary.
   *
   * <b>Note:</b> Current method is limited to Constant Vector values only.
   *
   * @param b given boundary.
   * @param name given Physics Values. E.g.: Volume Fraction, Flow Direction, etc...
   * @param vals given values.
   * @param u given units.
   */
  public void setBC_Values(Boundary b, String name, double[] vals, Units u) {
    setBC_Values(b, name, vals, u, true);
  }

  private void setBC_Values(Boundary b, String name, double[] vals, Units u, boolean verboseOption) {
    printAction("Setting Constant Vector Boundary Values", b, verboseOption);
    VectorProfile vp = getVectorProfile(b, name, verboseOption);
    setProfile(vp, vals, u);
    sayOK(verboseOption);
  }

  /**
   * Sets the Boundary values according to elements given in a Table.
   * <p>
   * <b>Notes:</b> <ul>
   * <li> The columns to be mapped must have the same name as are given the Physics Values tree.
   *    E.g.: "Velocity Magnitude", "Turbulent Viscosity Ratio", etc...;
   * <li> Current method is limited to Scalar values and XYZ tables only.
   * </ul>
   * @param b given Boundary.
   * @param t given Table.
   */
  public void setBC_Values(Boundary b, Table t) {
    printAction("Setting Boundary Values from a Table");
    sayBdry(b);
    say("Table: " + t.getPresentationName());
    say("Number of Boundary Physics Values: " + b.getValues().getObjects().size());
    say("Number of Columns in Table: " + t.getColumnDescriptors().size());
    ArrayList<String> cols = new ArrayList();
    for (ColumnDescriptor cd : t.getColumnDescriptors()) {
        cols.add(cd.getColumnName());
    }
    for (ClientServerObject cso : b.getValues().getObjects()) {
        String name = cso.getPresentationName();
        if (cols.contains(name)) {
            say("Setting to Table (x,y,z): " + name);
            ScalarProfile p = (ScalarProfile) cso;
            p.setMethod(XyzTabularScalarProfileMethod.class);
            p.getMethod(XyzTabularScalarProfileMethod.class).setTable(t);
            p.getMethod(XyzTabularScalarProfileMethod.class).setData(name);
        }
    }
    sayOK();
  }

  /**
   * Sets a custom Colormap to a given Displayer, if applicable.
   *
   * @param d given Displayer.
   * @param cmap given Colormap.
   */
  public void setColormap(Displayer d, LookupTable cmap) {
    printAction("Setting a custom Colormap");
    sayDisplayer(d);
    Legend l = getLegend(d);
    if (l == null) {
        say("Not applicable...");
        return;
    }
    say("Colormap: " + cmap.getPresentationName());
    l.setLookupTable(cmap);
    sayOK();
  }

  private void setDisplayerEnhancements(Displayer d) {
    //-- STAR-CCM+ defaults.
    final int defColorLabels = 6;
    final double defLabelHeight = 0.11;
    final double defLabelWidth = 0.6;
    final String defLabelFmt = "%-#6.5g";
    //--
    int colorLabels = defColorLabels;
    int colorLevels = 128;
    double height = 0.15;
    double width = 0.7;
    String labelFmt = "%-#6.3g";
    String label_0dec = "%.0f";
    String label_1dec = "%.1f";
    String label_2dec = "%.2f";
    //DoubleVector dvPos = new DoubleVector(new double[] {0.23, 0.05}); // Prior to v3a
    DoubleVector dvPos = new DoubleVector(new double[] {0.2, 0.01});
    Legend leg = getLegend(d, false);
    ScalarDisplayQuantity sdq = getScalarDisplayQuantity(d, false);
    if (sdq != null) {
        sdq.setClip(false);
        FieldFunction f = sdq.getFieldFunction();
        String fName = f.getPresentationName();
        Units u = sdq.getUnits();
        boolean ismmH2O = u == unit_mmH2O;
        boolean isPa = u == unit_Pa;
        boolean isPC = fName.matches(varPC);
        boolean isP = fName.matches(".*Pressure.*");
        boolean isT = fName.matches(".*Temperature.*");
        boolean isTVR = fName.matches(varTVR);
        boolean isVF = fName.matches("Volume.*Fraction.*");
        boolean isYP = f == getFieldFunction(varYplus, false);
        if (isVF && hasVOF()) {
            colorLabels = 3;
            width = 0.4;
            height = 0.125;
            dvPos = new DoubleVector(new double[] {0.323, 0.01});
        }
        if (ismmH2O) {
            labelFmt = label_0dec;
        } else if (isVF || isT || isTVR || isPa || isYP) {
            labelFmt = label_1dec;
        } else if (isPC  || isP) {
            labelFmt = label_2dec;
        }
    }
    if (leg != null) {
        if (!leg.getLabelFormat().equals(defLabelFmt)) {
            labelFmt = leg.getLabelFormat();
        }
        if (leg.getNumberOfLabels() != defColorLabels) {
            colorLabels = leg.getNumberOfLabels();
        }
        if (leg.getWidth() != defLabelWidth) {
            width = leg.getWidth();
        }
        if (leg.getHeight()!= defLabelHeight) {
            height = leg.getHeight();
        }
    }
    setDisplayerEnhancements_Colors(leg, colorLevels, colorLabels, height, width);
    setDisplayerEnhancements_Defaults(leg, labelFmt, dvPos);
  }

  private void setDisplayerEnhancements_Colors(Legend leg, int lvl, int lbl, double h, double w) {
    if (leg == null) {
        return;
    }
    leg.setNumberOfLabels(lbl);
    leg.setWidth(w);
    leg.setHeight(h);
    leg.setLevels(lvl);
    leg.setShadow(false);
    if (defColormap != null) {
      leg.setLookupTable(defColormap);
    }
 }

  private void setDisplayerEnhancements_Defaults(Legend leg, String fmt, DoubleVector dvPos) {
    if (leg == null) {
        return;
    }
    String labelDef = "%-#6.5g";
    DoubleVector dvDefPos = new DoubleVector(new double[] {0.3, 0.05});
    boolean isStrml = isStreamline(leg.getDisplayer());
    if (leg.getLabelFormat().equals(labelDef)) {
        leg.setLabelFormat(fmt);
    }
    if (leg.getPositionCoordinate().equals(dvDefPos) || isStrml) {
        leg.setPositionCoordinate(dvPos);
    }
  }

    /**
     *
     * @param min
     * @param tgt
     */
    public void setGlobalFeatureCurveMeshRefinement(double min, double tgt) {
    printAction("Setting global mesh refinement on all Feature Curves");
    for (Region r : getRegions(".*", false)) {
        sayRegion(r);
        ArrayList<FeatureCurve> afc = new ArrayList(r.getFeatureCurveManager().getFeatureCurves());
        for (FeatureCurve featCurve : afc) {
            setMeshFeatureCurveSizes(featCurve, min, tgt);
        }
    }
  }

  /**
   * Applies an Initial Condition to a Variable Physics Value to a Boundary.
   *
   * <b>Note:</b> Current method is limited to Constant Scalar values only.
   *
   * @param pc given Physics Continua.
   * @param name given Initial Conditions. E.g.: Pressure, Temperature, etc...
   * @param val given value.
   * @param u given units.
   */
  public void setInitialCondition(PhysicsContinuum pc, String name, double val, Units u) {
    setInitialCondition(pc, name, val, u, true);
  }

  private void setInitialCondition(PhysicsContinuum pc, String name, double val, Units u,
                                                                            boolean verboseOption) {
    printAction("Setting Initial Conditions", verboseOption);
    setProfile(getScalarProfile(pc.getInitialConditions(), name, verboseOption), val, u);
    sayOK(verboseOption);
  }

  private void setInitialCondition_P(PhysicsContinuum phC, double press, boolean verboseOption) {
    printAction("Setting Initial Conditions for Pressure", verboseOption);
    InitialPressureProfile ipp = phC.getInitialConditions().get(InitialPressureProfile.class);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(press);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
  }

  private void setInitialCondition_T(PhysicsContinuum phC, double temp, boolean verboseOption) {
    printAction("Setting Initial Conditions for Temperature", verboseOption);
    sayContinua(phC, true);
    say(retTemp(temp), verboseOption);
    StaticTemperatureProfile stp = phC.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(temp);
  }

  /**
   * Set Initial Conditions for Temperature.
   *
   * @param phC given Physics Continua
   * @param T0 initial Temperature in default unit. See {@see #defUnitTemp}.
   */
  public void setInitialCondition_Temperature(PhysicsContinuum phC, double T0) {
    setInitialCondition_T(phC, T0, true);
    sayOK();
  }

  /**
   * Set Initial Conditions for Turbulence (RANS 2-equation models).
   *
   * @param phC given Physics Continua
   * @param tvs0 initial Turbulent Velocity Scale.
   * @param ti0 initial Turbulent Intensity.
   * @param tvr0 initial Turbulent Viscosity Ratio.
   */
  public void setInitialCondition_Turbulence(PhysicsContinuum phC, double tvs0, double ti0, double tvr0) {
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, true);
    sayOK();
  }

  private void setInitialCondition_TVS_TI_TVR(PhysicsContinuum phC, double tvs0, double ti0, double tvr0, boolean verboseOption) {
    printAction("Setting Initial Conditions for Turbulence", verboseOption);
    sayContinua(phC, true);
    if (phC.getInitialConditions().has("Turbulent Velocity Scale")) {
        say("Turbulent Velocity Scale: " + tvs0, verboseOption);
        TurbulentVelocityScaleProfile tvs = phC.getInitialConditions().get(TurbulentVelocityScaleProfile.class);
        tvs.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvs0);
    }
    if (phC.getInitialConditions().has("Turbulent Intensity")) {
    say("Turbulent Intensity: " + ti0, verboseOption);
    TurbulenceIntensityProfile tip = phC.getInitialConditions().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti0);
    }
    if (phC.getInitialConditions().has(varTVR)) {
        say("Turbulent Viscosity Ratio: " + tvr0, verboseOption);
        TurbulentViscosityRatioProfile tvrp = phC.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
        tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr0);
    }
  }

  /**
   * Set Initial Conditions for Velocity in default units. See {@see #defUnitVel}.
   *
   * @param phC given Physics Continua
   * @param Vx given X-Velocity component.
   * @param Vy given Y-Velocity component.
   * @param Vz given Z-Velocity component.
   */
  public void setInitialCondition_Velocity(PhysicsContinuum phC, double Vx, double Vy, double Vz) {
    setInitialCondition_Velocity(phC, Vx, Vy, Vz, true);
    sayOK();
  }

  private void setInitialCondition_Velocity(PhysicsContinuum phC, double Vx, double Vy, double Vz, boolean verboseOption) {
    printAction("Setting Initial Conditions for Velocity Components", verboseOption);
    sayContinua(phC, true);
    say("Velocity X: " + Vx, verboseOption);
    say("Velocity Y: " + Vy, verboseOption);
    say("Velocity Z: " + Vz, verboseOption);
    VelocityProfile vp = phC.getInitialConditions().get(VelocityProfile.class);
    if (defUnitVel != null) {
        vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setUnits(defUnitVel);
    }
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(Vx, Vy, Vz);
  }

    /**
     *
     * @param i
     * @param tol
     */
    public void setInterfaceTolerance(Interface i, double tol) {
    String intrfType = i.getBeanDisplayName();
    printAction("Setting tolerance for a " + intrfType);
    sayInterface(i);
    say("  Tolerance: " + tol);
    if (isDirectBoundaryInterface(i)) {
        i.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
    }
    if (isIndirectBoundaryInterface(i)) {
        i.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
    }
    sayOK();
  }

  /**
   * Sets a Constant Material Property for a given media.
   *
   * <b>Currently, only single-phase is supported.</b>
   *
   * @param pc given Physics Continua.
   * @param mat given material name. E.g.: H2O.
   * @param prop given property. E.g.: Density.
   * @param val given value.
   * @param u given Units. Use <b>null</b> to use preferred units.
   */
  public void setMaterialProperty(PhysicsContinuum pc, String mat, String prop, double val, Units u) {
    printAction("Setting Material Property");
    ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(getMaterialModel(pc), getClass(prop));
    setMatPropMeth(cmpm, val, u);
    sayOK();
  }

  private void setMatPropMeth(ConstantMaterialPropertyMethod cmpm, double val, Units u) {
    say("Setting a %s %s for %s", cmpm.getPresentationName(),
            cmpm.getMaterialProperty().getPresentationName(),
            cmpm.getMaterialProperty().getParent().getParent().getPresentationName());
    cmpm.getQuantity().setValue(val);
    String us = "";
    if (u != null) {
        us = u.getPresentationName();
        cmpm.getQuantity().setUnits(u);
    }
    say("  Value: %g %s", val, us);
  }

  /**
   * Sets the Automatic Surface Repair Parameters for the Remesher.
   *
   * @param continua given Mesh Continua.
   * @param minProx given Minimum Proximity. Default is 5%.
   * @param minQual given Minimum Quality. Default is 1%.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshAutomaticSurfaceRepairParameters(MeshContinuum continua, double minProx, double minQual) {
    if (!isRemesh(continua)) return;
    say("Setting Automatic Surface Repair Parameters:");
    say("   Minimum Proximity: " + minProx);
    say("   Minimum Quality: " + minQual);
    AutomaticSurfaceRepair asr = continua.getReferenceValues().get(AutomaticSurfaceRepair.class);
    AutomaticSurfaceRepairMinimumProximity asrmp = asr.getAutomaticSurfaceRepairMinimumProximity();
    asrmp.setAutomaticSurfaceRepairMinimumProximity(minProx);
    AutomaticSurfaceRepairMinimumQuality asrmq = asr.getAutomaticSurfaceRepairMinimumQuality();
    asrmq.setAutomaticSurfaceRepairMinimumQuality(minQual);
    sayOK();
  }

  /**
   * Specifies the Base Mesh Size for a Mesh Operation or Continua.
   *
   * @param no given Named Object.
   * @param val reference size.
   * @param u given units.
   */
  public void setMeshBaseSize(NamedObject no, double val, Units u) {
    setMeshBaseSize(no, val, u, true);
  }

  private void setMeshBaseSize(NamedObject no, double val, Units u, boolean verboseOption) {
    printAction("Setting the Mesh Base Size", verboseOption);
    sayNamedObject(no, verboseOption);
    BaseSize bs = getClassBaseSize(no);
    if (bs == null) {
        return;
    }
    bs.setValue(val);
    bs.setUnits(u);
    say("Base Size: " + val + u.getPresentationName(), verboseOption);
    sayOK(verboseOption);
  }

  /**
   * Set a custom Mesh Surface Size for a Feature Curve.
   *
   * @param fc given Feature Curve.
   * @param min given Minimum size.
   * @param tgt give Target size.
   */
  public void setMeshFeatureCurveSizes(FeatureCurve fc, double min, double tgt) {
    say("Custom Feature Curve Mesh Size: " + fc.getPresentationName());
    sayRegion(fc.getRegion());
    try {
        fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = fc.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, true);
    } catch (Exception e) {
        say("ERROR! Please review settings. Skipping this Feature Curve.");
        say(e.getMessage());
        return;
    }
    sayOK();
  }

  /**
   * Set a custom Mesh Surface Size all Feature Curves based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @param min given Minimum size.
   * @param tgt give Target size.
   */
  public void setMeshFeatureCurvesSizes(String regexPatt, double min, double tgt) {
    printAction(String.format("Setting Mesh Feature Curves by REGEX pattern: \"%s\"", regexPatt));
    int n = 0;
    for (Region reg : getRegions(regexPatt, false)) {
        for (FeatureCurve fc : getFeatureCurves(reg, false)) {
            setMeshFeatureCurveSizes(fc, min, tgt);
            n++;
        }
    }
    say("Feature Curves changed: " + n);
    printLine();
  }

  /**
   * Enables the Generalized Cylinder Model Constant Extrusion.
   *
   * @param b given boundary.
   * @param layers given number of layers.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshGeneralizedCylinderExtrusion_Constant(Boundary b, int layers) {
    printAction("Activating Generalized Cylinder Model");
    sayBdry(b);
    say("Constant Extrusion Layers: " + layers);
    enableGeneralizedCylinderModel(b.getRegion().getMeshContinuum(), false);
    GenCylOption gco = b.get(MeshConditionManager.class).get(GenCylOption.class);
    gco.getType().setSelected(GenCylExtrusionType.CONSTANT);
    b.get(MeshValueManager.class).get(GenCylConstantExtrusionValues.class).setNumLayers(layers);
    sayOK();
  }

  /**
   * Sets a Per-Region meshing on a Continua.
   *
   * @param continua given Mesh Continua.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshPerRegionFlag(MeshContinuum continua) {
    printAction("Setting Mesh Continua as \"Per-Region Meshing\"");
    sayContinua(continua, true);
    continua.setMeshRegionByRegion(true);
    sayOK();
  }

  /**
   * Sets Default Prism Mesh parameters for a Mesh Operation.
   *
   * @param mo given Mesh Operation.
   * @param numLayers given number of prisms.
   * @param stretch given prism stretch relation.
   * @param relSize given relative size in (%).
   */
  public void setMeshPrismsParameters(MeshOperation mo, int numLayers, double stretch, double relSize) {
    sayMeshOp(mo, true);
    if (!isAutoMeshOperation(mo)) {
        say("This Mesh Operation can not have Prism Layers Controls. Skipping...");
        return;
    }
    AutoMeshOperation amo = (AutoMeshOperation) mo;
    if (!hasPrismLayerMesher(amo)) {
        return;
    }
    sayPrismsParameters(numLayers, stretch, relSize, true);
    PrismAutoMesher pam = ((PrismAutoMesher) amo.getMeshers().getObject("Prism Layer Mesher"));
    pam.setMinimumThickness(prismsMinThickn);
    pam.setGapFillPercentage(prismsGapFillPerc);
    pam.setLayerChoppingPercentage(prismsLyrChoppPerc);
    pam.setNearCoreLayerAspectRatio(prismsNearCoreAspRat);
    amo.getDefaultValues().get(NumPrismLayers.class).setNumLayers(numLayers);
    amo.getDefaultValues().get(PrismLayerStretching.class).setStretching(stretch);
    amo.getDefaultValues().get(PrismThickness.class).getRelativeSize().setPercentage(relSize);
    sayOK();
  }

  /**
   * Set custom Prism Mesh parameters for a boundary.
   *
   * @param b given Boundary.
   * @param numLayers given number of prisms.
   * @param stretch given prism stretch relation.
   * @param relSize given relative size in (%).
   */
  public void setMeshPrismsParameters(Boundary b, int numLayers, double stretch, double relSize) {
    sayBdry(b);
    sayPrismsParameters(numLayers, stretch, relSize, true);
    if (isDirectBoundaryInterface(b)) {
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) b;
        intrfBdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        intrfBdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        intrfBdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(intrfBdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
    } else if (isIndirectBoundaryInterface(b)) {
        say("Prisms not available here. Skipping...");
    } else {
        b.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        b.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        b.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(b.get(MeshValueManager.class).get(PrismThickness.class), relSize);
    }
    sayOK();
  }

  /**
   * Set custom Prism Mesh parameters for a Mesh Continua.
   *
   * @param continua given Mesh Continua.
   * @param numLayers given number of prisms.
   * @param stretch given prism stretch relation.
   * @param relSize given relative size in (%).
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshPrismsParameters(MeshContinuum continua, int numLayers, double stretch, double relSize) {
    printAction("Changing Prism Layers Parameters");
    sayContinua(continua, true);
    sayPrismsParameters(numLayers, stretch, relSize, true);
    continua.getReferenceValues().get(NumPrismLayers.class).setNumLayers(numLayers);
    continua.getReferenceValues().get(PrismLayerStretching.class).setStretching(stretch);
    continua.getReferenceValues().get(PrismThickness.class).getRelativeSize().setPercentage(relSize);
    sayOK();
  }

  /**
   * Sets Prism Mesh parameters for a Custom Surface Mesh Control.
   *
   * @param scmc given Surface Custom Mesh Control.
   * @param numLayers given number of prisms. Zero is ignored.
   * @param stretch given prism stretch relation. Zero is ignored.
   * @param relSize given relative size in (%). Zero is ignored.
   */
  public void setMeshPrismsParameters(SurfaceCustomMeshControl scmc, int numLayers, double stretch, double relSize) {
    say("Custom Surface Mesh Control: " + scmc.getPresentationName(), true);
    sayPrismsParameters(numLayers, stretch, relSize, true);
    PartsCustomizePrismMesh pcpm = scmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
    pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.CUSTOMIZE);
    PartsCustomizePrismMeshControls pcpmc = pcpm.getCustomPrismControls();
    if (numLayers > 0) {
        pcpmc.setCustomizeNumLayers(true);
        scmc.getCustomValues().get(CustomPrismValuesManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
    }
    if (stretch > 0) {
        pcpmc.setCustomizeStretching(true);
        scmc.getCustomValues().get(CustomPrismValuesManager.class).get(PrismLayerStretching.class).setStretching(stretch);
    }
    if (relSize > 0) {
        pcpmc.setCustomizeTotalThickness(true);
        scmc.getCustomValues().get(CustomPrismValuesManager.class).get(PrismThickness.class).getRelativeSize().setPercentage(relSize);
    }
    sayOK();
  }

  private void setMeshPrismsThickness(PrismThickness prismThick, double relSize) {
    ((GenericRelativeSize) prismThick.getRelativeSize()).setPercentage(relSize);
  }

  /**
   * Sets the Surface Growth Rate for the Trimmer. Available options implemented in JAVA API are:
   * <ul>
   * <li>SurfaceGrowthRateOption.VERYSLOW</li>
   * <li>SurfaceGrowthRateOption.SLOW</li>
   * <li>SurfaceGrowthRateOption.MEDIUM</li>
   * <li>SurfaceGrowthRateOption.FAST</li>
   * </ul>
   *
   * @param b given Boundary.
   * @param growthRate the integer given by one of the variables above.
   */
  public void setMeshSurfaceGrowthRate(Boundary b, int growthRate) {
    printAction("Setting Custom Surface Growth on Trimmer Mesh");
    sayBdry(b);
    b.get(MeshConditionManager.class).get(CustomSurfaceGrowthRateOption.class).setEnabled(true);
    b.get(MeshValueManager.class).get(CustomSimpleSurfaceGrowthRate.class).getSurfaceGrowthRateOption().setSelected(growthRate);
    sayOK();
  }

  /**
   * Specifies Surface Mesh Sizes for a Mesh Operation.
   *
   * @param mo given Mesh Operation.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(MeshOperation mo, double min, double tgt) {
    setMeshSurfaceSizes(mo, min, tgt, true);
  }

  private void setMeshSurfaceSizes(MeshOperation mo, double min, double tgt, boolean verboseOption) {
    printAction("Setting Surface Sizes");
    sayMeshOp(mo, verboseOption);
    if (!(isAutoMeshOperation(mo) || isSurfaceWrapperOperation(mo))) {
        say("This Mesh Operation can not have Custom Controls. Skipping...");
        return;
    }
    AutoMeshOperation amo = (AutoMeshOperation) mo;
    say(retMinTargetString(min, tgt), verboseOption);
    PartsTargetSurfaceSize ptss = amo.getDefaultValues().get(PartsTargetSurfaceSize.class);
    ((GenericRelativeSize) ptss.getRelativeSize()).setPercentage(tgt);
    PartsMinimumSurfaceSize pmss = amo.getDefaultValues().get(PartsMinimumSurfaceSize.class);
    ((GenericRelativeSize) pmss.getRelativeSize()).setPercentage(min);
     sayOK(verboseOption);
  }

  /**
   * Specifies Surface Mesh Sizes for a Boundary.
   *
   * @param b given Boundary.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Boundary b, double min, double tgt) {
    setMeshSurfaceSizes(b, min, tgt, true);
  }

  /**
   * Specifies Surface Mesh Sizes for a Custom Mesh Control.
   *
   * @param cmc given Custom Mesh Control.
   * @param min minimum relative size (%) must be greater than zero. Otherwise it assumes
   *    the parent value.
   * @param tgt target relative size (%) must be greater than zero. Otherwise it assumes
   *    the parent value.
   */
  public void setMeshSurfaceSizes(CustomMeshControl cmc, double min, double tgt) {
    setMeshSurfaceSizes(cmc, min, tgt, true);
  }

  private void setMeshSurfaceSizes(CustomMeshControl cmc, double min, double tgt, boolean verboseOption) {
    printAction("Setting Surface Sizes", verboseOption);
    say("Mesh Control: " + cmc.getPresentationName(), verboseOption);
    say(retMinTargetString(min, tgt), verboseOption);
    if (min > 0) {
        cmc.getCustomConditions().get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.CUSTOM);
        PartsMinimumSurfaceSize pmss = cmc.getCustomValues().get(PartsMinimumSurfaceSize.class);
        ((GenericRelativeSize) pmss.getRelativeSize()).setPercentage(min);
    }
    if (tgt > 0) {
        cmc.getCustomConditions().get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.CUSTOM);
        PartsTargetSurfaceSize ptss = cmc.getCustomValues().get(PartsTargetSurfaceSize.class);
        ((GenericRelativeSize) ptss.getRelativeSize()).setPercentage(tgt);
    }
     sayOK(verboseOption);
  }

  /**
   * Specify Surface Mesh Sizes for an Interface.
   *
   * @param i given Interface.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Interface i, double min, double tgt) {
    sayInterface(i);
    if (isDirectBoundaryInterface(i)) {
        DirectBoundaryInterface dbi = (DirectBoundaryInterface) i;
        dbi.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = i.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, true);
    } else {
        say("Not a Direct Boundary Interface. Skipping...");
    }
    sayOK();
  }

  /**
   * Loop through all boundaries in the Region and specify Surface Mesh Sizes for them.
   *
   * @param r given Region.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Region r, double min, double tgt) {
    printAction("Setting Mesh Sizes in a Region");
    sayRegion(r);
    say("  " + retMinTargetString(min, tgt));
    printLine();
    for (Boundary b : getAllBoundariesFromRegion(r, false, false)) {
        if (!setMeshSurfaceSizes(b, min, tgt, false)) {
            say("Skipped!  " + b.getPresentationName());
            continue;
        }
        say("OK!  " + b.getPresentationName());
    }
    printLine();
    sayOK();
  }

  private boolean setMeshSurfaceSizes(Boundary b, double min, double tgt, boolean verboseOption) {
    sayBdry(b, verboseOption, true);
    if (!isMeshing(b, verboseOption)) {
        say("Region has no Mesh Continua. Skipping...", verboseOption);
        return false;
    } else if (isIndirectBoundaryInterface(b)) {
        say("Skipping...", verboseOption);
        return false;
    } else if (isDirectBoundaryInterface(b)) {
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) b;
        DirectBoundaryInterface i = intrfBdry.getDirectBoundaryInterface();
        i.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = i.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, verboseOption);
    } else {
        b.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = b.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, verboseOption);
    }
    sayOK(verboseOption);
    return true;
  }

  private void setMeshSurfaceSizes(SurfaceSize srfSize, double min, double tgt, boolean verboseOption) {
    say("  " + retMinTargetString(min, tgt), verboseOption);
    srfSize.getRelativeMinimumSize().setPercentage(min);
    srfSize.getRelativeTargetSize().setPercentage(tgt);
  }

    /**
     *
     * @param continua
     * @param numPoints
     * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
     */
    public void setMeshCurvatureNumberOfPoints(MeshContinuum continua, double numPoints) {
    printAction("Setting Mesh Continua Surface Curvature");
    sayContinua(continua, true);
    say("  Points/Curve: " + numPoints);
    continua.getReferenceValues().get(SurfaceCurvature.class).getSurfaceCurvatureNumPts().setNumPointsAroundCircle(numPoints);
    sayOK();
  }

  /**
   * Specify Surface Mesh Sizes for a Mesh Continuum.
   *
   * @param continua given Mesh Continua.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshSurfaceSizes(MeshContinuum continua, double min, double tgt) {
    printAction("Setting Mesh Continua Surface Sizes");
    sayContinua(continua, true);
    SurfaceSize srfSize = continua.getReferenceValues().get(SurfaceSize.class);
    setMeshSurfaceSizes(srfSize, min, tgt, true);
    sayOK();
  }

  /**
   * Sets a Mesh Volume Growth Factor for the Poly Mesh.
   *
   * @param continua given Mesh Continua.
   * @param growthFactor given Growth Factor.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshTetPolyGrowthRate(MeshContinuum continua, double growthFactor) {
    printAction("Setting Mesh Volume Growth Factor");
    sayContinua(continua, true);
    say("  Growth Factor: " + growthFactor);
    continua.getReferenceValues().get(VolumeMeshDensity.class).setGrowthFactor(growthFactor);
    sayOK();
  }

  /**
   * Sets the Mesh Trimmer Size To Prism Thickness Ratio.
   *
   * @param continua given Mesh Continua.
   * @param sizeThicknessRatio given Size Thickness Ratio. <i>Default is 5</i>.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshTrimmerSizeToPrismThicknessRatio(MeshContinuum continua, double sizeThicknessRatio) {
    printAction("Setting Mesh Trimmer Size To Prism Thickness Ratio");
    sayContinua(continua, true);
    MaxTrimmerSizeToPrismThicknessRatio ptr = continua.getReferenceValues().get(MaxTrimmerSizeToPrismThicknessRatio.class);
    ptr.setLimitCellSizeByPrismThickness(true);
    say("  Size Thickness Ratio: " + sizeThicknessRatio);
    ptr.getSizeThicknessRatio().setNeighboringThicknessMultiplier(sizeThicknessRatio);
    sayOK();
  }

  /**
   * Sets the Surface Wrapper Feature Angle.
   *
   * @param continua given Mesh Continua.
   * @param featAngle
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshWrapperFeatureAngle(MeshContinuum continua, double featAngle) {
    printAction("Setting Wrapper Feature Angle");
    sayContinua(continua, true);
    say("Feature Angle: " + featAngle + " deg");
    continua.getReferenceValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(featAngle);
    sayOK();
  }

  /**
   * Sets the Surface Wrapper Scale Factor.
   *
   * @param continua given Mesh Continua.
   * @param scaleFactor given Scale Factor. E.g.: 70.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public void setMeshWrapperScaleFactor(MeshContinuum continua, double scaleFactor) {
    printAction("Setting Wrapper Scale Factor");
    sayContinua(continua, true);
    if (scaleFactor < 1) {
        say("Warning! Scale Factor < 1. Multiplying by 100.");
        scaleFactor *= 100.;
    }
    say("Scale Factor: " + scaleFactor);
    continua.getReferenceValues().get(SurfaceWrapperScaleFactor.class).setScaleFactor(scaleFactor);
    sayOK();
  }

    /**
     *
     * @param r
     */
    public void setMeshWrapperVolumeExternal(Region r) {
    printAction("Setting Wrapping Region as EXTERNAL in VOLUME OF INTEREST");
    sayRegion(r);
    r.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.EXTERNAL);
    sayOK();
  }

    /**
     *
     * @param r
     */
    public void setMeshWrapperVolumeLargestInternal(Region r) {
    printAction("Setting Wrapping Region as LARGEST INTERNAL in VOLUME OF INTEREST");
    sayRegion(r);
    r.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.LARGEST_INTERNAL);
    sayOK();
  }

    /**
     *
     * @param r
     */
    public void setMeshWrapperVolumeSeedPoints(Region r) {
    printAction("Setting Wrapping Region as SEED POINTS in VOLUME OF INTEREST");
    sayRegion(r);
    r.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.SEED_POINT);
    sayOK();
  }

  /**
   * Disables the Normalization Option in all Residual Monitors. The Residuals shown are the RMS
   * values for all cells.
   */
  public void setMonitorsNormalizationOFF() {
    printAction("Disabling Residual Monitors Normalization");
    for (Monitor mon : sim.getMonitorManager().getObjects()) {
        say("   Monitor: " + mon.getPresentationName());
        if (isResidual(mon)) {
            ((ResidualMonitor) mon).getNormalizeOption().setSelected(MonitorNormalizeOption.OFF);
            say("      Normalization is now OFF.");
        }
    }
  }

  private void setProfile(ScalarProfile sp, double val, Units u) {
    if (sp == null) {
        return;
    }
    sp.setMethod(ConstantScalarProfileMethod.class);
    sp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(val);
    sp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(u);
    say("%s: %g %s", sp.getPresentationName(), val, u.getPresentationName());
  }

  private void setProfile(VectorProfile vp, double[] vals, Units u) {
    if (vp == null) {
        return;
    }
    vp.setMethod(ConstantVectorProfileMethod.class);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setConstant(vals);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setUnits(u);
    say(vp.getPresentationName() + ": " + retString(vals) + u.getPresentationName());
  }

  /**
   * Updates a Plot or Scene to Saves Pictures with a given resolution. Control the updates using
   * {@see #setUpdateFrequency}.<p>
   * Pictures will be saved on {@see #simPath} under a folder called <b>pics_<i>ObjectName</i></b>.
   *
   * @param no given Named Object. It can be a Plot or Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   */
  public void setSaveToFile(NamedObject no, int resx, int resy) {
    printAction("Setting a Scene to Save Pictures");
    sayNamedObject(no, true);
    String picName = "pics_" + getStringForFilename(no);
    if (isScene(no)) {
        setSaveToFile(((Scene) no).getSceneUpdate(), picName, resx, resy);
    } else if (isStarPlot(no)) {
        setSaveToFile(((UpdatePlot) no).getPlotUpdate(), picName, resx, resy);
    }
    sayOK();
  }

  private void setSaveToFile(WindowUpdate wu, String picName, int resx, int resy) {
    wu.setSaveAnimation(true);
    wu.setAnimationFilePath(new File(simPath, picName));
    wu.setAnimationFilenameBase("pic");
    wu.setXResolution(resx);
    wu.setYResolution(resy);
  }

  /**
   * Sets the Scene Background as Gradient colors.
   *
   * @param scn given Scene.
   * @param color1 given Color1.
   * @param color2 given Color2.
   */
  public void setSceneBackgroundColor_Gradient(Scene scn, Color color1, Color color2) {
    say("Setting Background as Gradient on " + scn.getPresentationName());
    scn.setBackgroundColorMode(1);
    GradientBackgroundColor gbc = scn.getGradientBackgroundColor();
    gbc.setColorColor1(color1);
    gbc.setColorColor2(color2);
    gbc.setMode(0);
    scn.setBackgroundColorMode(0);
  }

  /**
   * Sets the Scene Background as Solid color.
   *
   * @param scn given Scene.
   * @param color given Color.
   */
  public void setSceneBackgroundColor_Solid(Scene scn, Color color) {
    say("Setting Background as Solid Color on " + scn.getPresentationName());
    scn.setBackgroundColorMode(0);
    scn.getSolidBackgroundColor().setColorColor(color);
  }

  /**
   * Sets the Camera View in the Scene.
   *
   * @param scn given Scene.
   * @param cameraView given Camera View setup.
   */
  public void setSceneCameraView(Scene scn, VisView cameraView) {
    setSceneCameraView(scn, cameraView, true);
  }

  /**
   * Sets the Camera View in the Scene.
   *
   * @param scn given Scene.
   * @param cameraView given Camera View setup.
   * @param verboseOption Print to standard output?
   */
  public void setSceneCameraView(Scene scn, VisView cameraView, boolean verboseOption) {
    if (cameraView == null) {
        return;
    }
    say("Applying Camera View: " + cameraView.getPresentationName(), verboseOption);
    scn.getCurrentView().setInput(new CameraStateInput(cameraView), cameraView.getCoordinateSystem(), true);
    scn.getCurrentView().setProjectionMode(cameraView.getProjectionMode());
  }

  /**
   * Updates the Scene to Saves Pictures with a given resolution. Control the updates using
   * {@see #setUpdateFrequency} or {@see #setSceneUpdateFrequency}.<p>
   * Pictures will be saved on {@see #simPath} under a folder called <b>pics_<i>SceneName</i></b>.
   *
   * @param scn given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picType given picture type. 0 for PNG; 1 for JPEG.
   */
  public void setSceneSaveToFile(Scene scn, int resx, int resy, int picType) {
    printAction("Setting a Scene to Save Pictures");
    say("Scene: " + scn.getPresentationName());
    setSaveToFile(scn, resx, resy);
    scn.getSceneUpdate().setAnimationFileFormat(picType);
    sayOK();
  }

  /**
   * @param scn
   */
  private void setSceneLogo(Scene scn) {
    try {
        ((FixedAspectAnnotationProp) scn.getAnnotationPropManager().getAnnotationProp("Logo")).setLocation(1);
    } catch (Exception e) {}
  }

  /**
   * Sets aggressive under-relaxation factors (URFs) for the Segregated solvers. Good for steady
   * state analyses. If one is simulating Unsteady, it will set all URFs to unity. <u>Make sure the
   * global CFL &lt 1 condition is respected</u>. Otherwise fluctuations and/or divergence can occur.
   */
  public void setSolverAggressiveURFs() {
    urfVel = urfEnergy = urfKEps = urfKOmega = 0.9;
    urfP = 0.1;
    if (isUnsteady()) {
        urfVel = urfEnergy = urfKEps = urfKOmega = urfP = urfVOF = 1.0;
    }
    updateSolverSettings();
  }

  /**
   * Set the maximum number of inner iterations.
   *
   * @param n given inner iterations.
   */
  public void setSolverMaxInnerIterations(int n) {
    setSolverMaxInnerIterations(n, true);
  }

  private void setSolverMaxInnerIterations(int n, boolean verboseOption) {
    printAction("Setting Maximum Number of Inner Iterations", verboseOption);
    say("Max Inner Iterations: " + n, verboseOption);
    ((InnerIterationStoppingCriterion) getStoppingCriteria("Maximum Inner Iterations.*",
                                        false)).setMaximumNumberInnerIterations(n);
    sayOK(verboseOption);
  }

  /**
   * Set the maximum number of iterations in Simulation.
   *
   * @param n given number of iterations.
   */
  public void setSolverMaxIterations(int n) {
    setSolverMaxIterations(n, true);
  }

  private void setSolverMaxIterations(int n, boolean verboseOption) {
    SolverStoppingCriterion stc = getStoppingCriteria("Maximum Steps.*", false);
    if (stc == null) {
        return;
    }
    printAction("Setting Maximum Number of Iterations", verboseOption);
    say("Max Iterations: " + n, verboseOption);
    maxIter = n;
    ((StepStoppingCriterion) stc).setMaximumNumberSteps(n);
    sayOK(verboseOption);
  }

  /**
   * Set the maximum physical time for the unsteady simulation.
   *
   * @param maxTime
   * @param u
   */
  public void setSolverMaxPhysicalTimestep(double maxTime, Units u) {
    setSolverMaxPhysicalTimestep(maxTime, u, true);
  }

  private void setSolverMaxPhysicalTimestep(double maxTime, Units u, boolean verboseOption) {
    printAction("Setting Maximum Physical Timestep", verboseOption);
    PhysicalTimeStoppingCriterion stpCrit = (PhysicalTimeStoppingCriterion) getStoppingCriteria("Maximum Physical Time.*", false);
    stpCrit.getMaximumTime().setUnits(u);
    stpCrit.getMaximumTime().setValue(maxTime);
    say(verboseOption, "Maximum Physical Time: %g %s", maxTime, u.getPresentationName());
    sayOK(verboseOption);
  }

  /**
   * Set a constant Physical timestep for the unsteady solver.
   *
   * @param val given value in default units. See {@see #defUnitTime}.
   */
  public void setSolverPhysicalTimestep(double val) {
    setSolverPhysicalTimestep(val, null, true);
  }

  /**
   * Set a variable Physical timestep for the unsteady solver, using a Definition.
   *
   * @param definition
   */
  public void setSolverPhysicalTimestep(String definition) {
    setSolverPhysicalTimestep(0, definition, true);
  }

  private void setSolverPhysicalTimestep(double val, String def, boolean verboseOption) {
    printAction("Setting Physical Timestep", verboseOption);
    if (!isUnsteady()) {
        say("Not Unsteady.");
        return;
    }
    ImplicitUnsteadySolver trn = ((ImplicitUnsteadySolver) getSolver(ImplicitUnsteadySolver.class));
    if (val != 0) {
        say("Timestep: " + val, verboseOption);
        trn.getTimeStep().setValue(val);
    } else if (def != null) {
        say("Timestep: " + def, verboseOption);
        trn.getTimeStep().setDefinition(def);
    }
    trn.getTimeStep().setUnits(defUnitTime);
    String timeDiscr = "1st Order";
    if (trn2ndOrder) {
        trn.getTimeDiscretizationOption().setSelected(TimeDiscretizationOption.SECOND_ORDER);
        timeDiscr = "2nd Order";
    }
    say("Time Discretization: " + timeDiscr, verboseOption);
    sayOK(verboseOption);
  }

  /**
   * Updates the Simulation name.
   *
   * @param newSimTitle given new name.
   */
  public void setSimTitle(String newSimTitle) {
    simTitle = newSimTitle;
  }

  /**
   * Sets an Update Event to an Object, if applicable. Object can be any Plot, Monitor, Scene or
   * Solution History.
   *
   * @param no given Named Object.
   * @param ue given Update Event.
   */
  public void setUpdateEvent(NamedObject no, UpdateEvent ue) {
    setUpdateEvent(no, ue, true);
  }

  private void setUpdateEvent(NamedObject no, UpdateEvent ue, boolean verboseOption) {
    printAction("Setting an Update Event", verboseOption);
    sayNamedObject(no, true);
    say("Event: " + ue.getPresentationName(), verboseOption);
    if (isStarPlot(no)) {
        setUpdateEvent(((UpdatePlot) no).getPlotUpdate(), ue);
    } else if (isScene(no)) {
        setUpdateEvent(((Scene) no).getSceneUpdate(), ue);
    } else if (isMonitor(no)) {
        setUpdateEvent(((Monitor) no).getStarUpdate(), ue);
    } else if (isSolutionHistory(no)) {
        setUpdateEvent(((SolutionHistory) no).getUpdate(), ue);
    }
    sayOK(verboseOption);
  }

  private void setUpdateEvent(StarUpdate su, UpdateEvent ue) {
    su.getUpdateModeOption().setSelected(StarUpdateModeOption.EVENT);
    su.getEventUpdateFrequency().setUpdateEvent(ue);
  }

  /**
   * Sets a Vis Transform on a Displayer.
   *
   * @param d given Displayer.
   * @param vt given Vis Transform.
   */
  public void setVisTransform(Displayer d, VisTransform vt) {
    if (d == null || vt == null) {
        return;
    }
    d.setVisTransform(vt);
  }

  /**
   * Sleeps for a while.
   * @param ms the length of time to sleep in milliseconds.
   */
  public void sleep(int ms) {
    try {
      Thread.sleep(ms);   // <-- Freezes the GUI?
      //Thread.currentThread().sleep(ms);
    } catch (Exception e) {}
  }

  /**
   * Splits a Part Surface by a given angle.
   *
   * @param ps given Part Surface.
   * @param splitAngle given Split Angle.
   */
  public void splitByAngle(PartSurface ps, double splitAngle) {
    splitByAngle(ps, splitAngle, true);
  }

  /**
   * Splits the given Part Surfaces by an angle.
   *
   * @param aps given ArrayList of Part Surfaces.
   * @param splitAngle given Split Angle.
   */
  public void splitByAngle(ArrayList<PartSurface> aps, double splitAngle) {
    printAction("Splitting Part Surfaces by Angle");
    for (PartSurface ps : aps) {
        sayPartSurface(ps);
        splitByAngle(ps, splitAngle, false);
    }
    sayOK();
  }

  private void splitByAngle(PartSurface ps, double splitAngle, boolean verboseOption) {
    say("Splitting Part Surface by Angle:", verboseOption);
    sayPartSurface(ps, verboseOption);
    say("  Angle: " + splitAngle);
    ArrayList<PartSurface> aps = new ArrayList<PartSurface>();
    aps.add(ps);
    if (isCadPart(ps.getPart())) {
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), splitAngle);
    }
    if (isBlockPart(ps.getPart())) {
        SimpleBlockPart sbp = (SimpleBlockPart) ps.getPart();
        sbp.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), splitAngle);
    }
    if (isSimpleCylinderPart(ps.getPart())) {
        SimpleCylinderPart scp = (SimpleCylinderPart) ps.getPart();
        scp.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), splitAngle);
    }
    if (isLeafMeshPart(ps.getPart())) {
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), splitAngle);
    }
    if (isMeshOperationPart(ps.getPart())) {
        MeshOperationPart mop = (MeshOperationPart) ps.getPart();
        mop.getPartSurfaceManager().splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), splitAngle);
    }
    sayOK(verboseOption);
  }

  /**
   * Splits a Part Surface into Non-Contiguous pieces.
   *
   * @param ps give Part Surface.
   * @return ArrayList of the new splitted Part Surfaces.
   */
  public ArrayList<PartSurface> splitByNonContiguous(PartSurface ps) {
    return splitByNonContiguous(ps, true);
  }

  /**
   * Splits a collection of Part Surfaces into Non-Contiguous pieces.
   *
   * @param aps given ArrayList of Part Surfaces.
   */
  public void splitByNonContiguous(ArrayList<PartSurface> aps) {
    printAction("Splitting Non Contiguous Part Surfaces");
    say("Given Part Surfaces: " + aps.size());
    int n = 0;
    int sum = 0;
    for (PartSurface ps : aps) {
        sum += splitByNonContiguous(ps, false).size();
        n++;
    }
    say("Overall new Part Surfaces created: " + sum);
    printLine();
  }

  private ArrayList<PartSurface> splitByNonContiguous(PartSurface ps, boolean verboseOption) {
    printAction("Splitting Non Contiguous Part Surface", verboseOption);
    sayPartSurface(ps);
    String name0 = ps.getPresentationName();
    String mySplit = "__splitFrom__" + name0;
    ps.setPresentationName(mySplit);
    ArrayList<PartSurface> aps = new ArrayList<PartSurface>();
    Object[] objArr = {ps};
    if (isCadPart(ps.getPart())) {
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    if (isLeafMeshPart(ps.getPart())) {
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    for (PartSurface ps1 : ps.getPart().getPartSurfaces()) {
        if (ps == ps1) {
            aps.add(0, ps1);
        }
        if (ps1.getPresentationName().matches(mySplit + ".*")) {
            aps.add(ps1);
        }
    }
    for (Iterator<PartSurface> it = aps.iterator(); it.hasNext();) {
        it.next().setPresentationName(name0);
    }
    sayOK(verboseOption);
    return aps;
  }

  /**
   * Splits the Geometry Part Surfaces by its own Part Curves.
   *
   * @param gp given Geometry Part.
   * @return The new Part Surfaces.
   */
  public  ArrayList<PartSurface> splitByPartCurves(GeometryPart gp) {
    return splitByPartCurves(getPartSurfaces(gp), getPartCurves(gp), true);
  }

  /**
   * Splits a collection of Part Surfaces by another collection of Part Curves.
   *
   * @param aps given ArrayList of Part Surfaces.
   * @param apc given ArrayList of Part Curves.
   * @return The new Part Surfaces.
   */
  public  ArrayList<PartSurface> splitByPartCurves(ArrayList<PartSurface> aps, ArrayList<PartCurve> apc) {
    return splitByPartCurves(aps, apc, true);
  }

  private ArrayList<PartSurface> splitByPartCurves(ArrayList<PartSurface> aps,
                                        ArrayList<PartCurve> apc, boolean verboseOption) {
    printAction("Splitting Part Surfaces by Part Curves", verboseOption);
    //--
    //Vector vecOrigNames = new Vector();
    String tmpName = "__tmpSplitPS__";
    //--
    say("Part Surfaces given: " + aps.size(), verboseOption);
    for (PartSurface ps : aps) {
        say("  " + ps.getPresentationName());
        //vecOrigNames.add(ps.getPresentationName());
        ps.setPresentationName(tmpName + ps.getPresentationName());
    }
    //--
    say("Part Curves given: " + apc.size(), verboseOption);
    for (PartCurve pc : apc) {
        say("  " + pc.getPresentationName());
    }
    //--
    //-- Single Part check
    if (retNumberOfParentParts(aps) > 1) {
        say("Please provide a Collection within a single Geometry Part", verboseOption);
        say("Returning NULL!", verboseOption);
        return null;
    }
    //--
    GeometryPart gp = aps.iterator().next().getPart();
    if (isCadPart(gp)) {
        CadPart cp = (CadPart) gp;
        cp.getPartSurfaceManager().splitPartSurfacesByPartCurves(new NeoObjectVector(aps.toArray()),
                new NeoObjectVector(apc.toArray()));
    }
    if (isLeafMeshPart(gp)) {
        LeafMeshPart lmp = (LeafMeshPart) gp;
        lmp.getPartSurfaceManager().splitPartSurfacesByPartCurves(new NeoObjectVector(aps.toArray()),
                new NeoObjectVector(apc.toArray()));
    }
    //--
    //-- Renaming back to original
    ArrayList<PartSurface> aps2 = getPartSurfaces(gp, "^" + tmpName + ".*");
    say("Returning " + aps2.size() + " new Part Surfaces.", verboseOption);
    for (PartSurface ps : aps2) {
        ps.setPresentationName(ps.getPresentationName().replace(tmpName, ""));
        say("  " + ps.getPresentationName(), verboseOption);
    }
    sayOK(verboseOption);
    return aps2;
  }

  /**
   * Split a Non-Contiguous r.
   *
   * @param r given Region.
   * @deprecated in v3c. Not used anymore.
   */
  public void splitRegionNonContiguous(Region r) {
    printAction("Splitting Non Contiguous Regions");
    MeshContinuum mshc = null;
    PhysicsContinuum phc = null;
    sayRegion(r);
    Object[] objArr = {r};
    sim.getMeshManager().splitNonContiguousRegions(new NeoObjectVector(objArr), minConFaceAreas);
    // Loop into the generated Regions created by Split
    for (Region reg : getRegions("^" + r.getPresentationName() + " \\d{1,2}", false)) {
        if (r == reg) { continue; }
        mshc = reg.getMeshContinuum();
        if (mshc != null) { mshc.erase(reg); }
        phc = reg.getPhysicsContinuum();
        if (phc != null) { phc.erase(reg); }
        say("  Disabled Region: " + reg.getPresentationName());
    }
    sayOK();
  }

  /**
   * Converts a given string into a wildcard REGEX pattern.
   *
   * @param text given string or text.
   * @return The returned string will be <b>.*text.*</b>.
   */
  public String str2regex(String text) {
    return ".*" + text + ".*";
  }

  /**
   * Shows a warning dialog.
   *
   * @param message given text to be displayed.
   */
  public void showWarning(String message) {
    JOptionPane.showMessageDialog(null, message, "WARNING!", JOptionPane.WARNING_MESSAGE);
  }

  private Units updateDefaultUnit(Units u1, Units u2, String descr, boolean verboseOption) {
    Units defUnit = null;
    String defDescr = "Not initialized or not available";
    if (u1 == null) {
        defUnit = u2;
    } else {
        defUnit = u1;
    }
    try {
        defDescr = defUnit.getPresentationName();
    } catch (Exception e) {}
    say("Default Unit " + descr + ": " + defDescr, verboseOption);
    return defUnit;
  }

  private void updateDefaultUnits(boolean verboseOption) {
    printAction("Updating Default Units", verboseOption);
    defUnitAccel = updateDefaultUnit(defUnitAccel, unit_mps2, "Acceleration", verboseOption);
    defUnitAngle = updateDefaultUnit(defUnitAngle, unit_deg, "Angle", verboseOption);
    defUnitArea = updateDefaultUnit(defUnitArea, unit_m2, "Area", verboseOption);
    defUnitDen = updateDefaultUnit(defUnitDen, unit_kgpm3, "Density", verboseOption);
    defUnitForce = updateDefaultUnit(defUnitForce, unit_N, "Force", verboseOption);
    defUnitHTC = updateDefaultUnit(defUnitHTC, unit_Wpm2K, "Heat Transfer Coefficient", verboseOption);
    defUnitLength = updateDefaultUnit(defUnitLength, unit_mm, "Length", verboseOption);
    defUnitMFR = updateDefaultUnit(defUnitMFR, unit_kgps, "Mass Flow Rate", verboseOption);
    defUnitPress = updateDefaultUnit(defUnitPress, unit_Pa, "Pressure", verboseOption);
    defUnitTemp = updateDefaultUnit(defUnitTemp, unit_C, "Temperature", verboseOption);
    defUnitTime = updateDefaultUnit(defUnitTime, unit_s, "Time", verboseOption);
    defUnitVel = updateDefaultUnit(defUnitVel, unit_mps, "Velocity", verboseOption);
    defUnitVisc = updateDefaultUnit(defUnitVisc, unit_Pa_s, "Viscosity", verboseOption);
    defUnitVolume = updateDefaultUnit(defUnitVolume, unit_m3, "Volume", verboseOption);
    sayOK(verboseOption);
  }

  private void updateReferenceValues(PhysicsContinuum phC) {
    updateUnits(false);
    printAction("Updating Reference Values");
    sayContinua(phC, true);
    if (phC.getReferenceValues().has("Gravity")) {
        say("  Gravity: " + retString(gravity) + " " + defUnitAccel.getPresentationName());
        phC.getReferenceValues().get(Gravity.class).setComponents(gravity[0], gravity[1], gravity[2]);
        phC.getReferenceValues().get(Gravity.class).setUnits(defUnitAccel);
    }
    if (phC.getReferenceValues().has("Reference Altitude")) {
        say("  Reference Altitude: " + retString(refAlt) + " " + defUnitLength.getPresentationName());
        phC.getReferenceValues().get(ReferenceAltitude.class).setComponents(refAlt[0], refAlt[1], refAlt[2]);
        phC.getReferenceValues().get(ReferenceAltitude.class).setUnits(defUnitLength);
    }
    if (phC.getReferenceValues().has("Reference Density")) {
        say("  Reference Density: " + refDen + " " + defUnitDen.getPresentationName());
        phC.getReferenceValues().get(ReferenceDensity.class).setValue(refDen);
        phC.getReferenceValues().get(ReferenceDensity.class).setUnits(defUnitDen);
    }
    if (phC.getReferenceValues().has("Reference Pressure")) {
        say("  Reference Pressure: " + refP + " " + defUnitPress.getPresentationName());
        phC.getReferenceValues().get(ReferencePressure.class).setValue(refP);
        phC.getReferenceValues().get(ReferencePressure.class).setUnits(defUnitPress);

    }
    if (phC.getReferenceValues().has("Reference Temperature")) {
        say("  Reference Temperature: " + refT + " " + defUnitTemp.getPresentationName());
        phC.getReferenceValues().get(ReferenceTemperature.class).setUnits(defUnitTemp);
        phC.getReferenceValues().get(ReferenceTemperature.class).setValue(refT);
    }
    if (phC.getReferenceValues().has("Maximum Allowable Temperature")) {
        say("  Maximum Allowable Temperature: " + clipMaxT + " " + defUnitTemp.getPresentationName());
        phC.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(defUnitTemp);
        phC.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    }
    if (phC.getReferenceValues().has("Minimum Allowable Temperature")) {
        say("  Minimum Allowable Temperature: " + clipMinT + " " + defUnitTemp.getPresentationName());
        phC.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(defUnitTemp);
        phC.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    }
    sayOK();
  }

  private void updateSimVar() {
    if (sim != null) {
        return;
    }
    sim = getActiveSimulation();
    updateSolverSettings();
  }

  /**
   * Updates all the objects within the simulation file to the Finite Volume Representation. This
   * method is useful when generating the mesh remotely.
   */
  public void updateObjectsToFVRepresentation() {
    if (!meshedInMacroUtils) {
        printMeshDiagnostics();
    }
    updateObjectsToFVRepresentation(null);
  }

  /**
   * Updates all the objects within the simulation file to the Finite Volume Representation. This
   * method is useful when generating the mesh remotely.
   *
   * @param regexPatt except the Displayers found in this REGEX pattern search. They will be assumed
   * as the Initial Surface Representation.
   */
  public void updateObjectsToFVRepresentation(String regexPatt) {
    printAction("Updating all objects FV Representation");
    FvRepresentation fv = queryVolumeMesh();
    sayCellCount(true);
    sim.getSceneManager().applyRepresentation(fv);
    sim.getPlotManager().applyRepresentation(fv);
    sim.getTableManager().applyRepresentation(fv);
    sim.getReportManager().applyRepresentation(fv);
    if (regexPatt != null) {
        for (Displayer dsp : getDisplayers(regexPatt, false)) {
            if (queryGeometryRepresentation() != null) {
                dsp.setRepresentation(queryGeometryRepresentation());
            } else {
                dsp.setRepresentation(queryInitialSurface());
            }
        }
    }
    sayOK();
  }

  /**
   * Updates all Solver Settings. E.g.: Relaxation Factors, Linear Ramps, etc... Currently, the
   * following variables can be set:<ul>
   * <li>Segregated: {@see #urfVel}, {@see #urfP}, {@see #urfFluidEnrgy}, {@see #urfSolidEnrgy},
   * {@see #urfVOF}, {@see #urfKEps}, {@see #rampURF_Vel}, {@see #rampURF_}, {@see #rampURF_T},
   * and some others...;</li>
   * <li>Steady State: {@see #maxIter};</li>
   * <li>Unsteady: {@see #trnTimestep}, {@see #trnInnerIter}, {@see #trnMaxTime}.</li>
   * </ul>
   */
  public void updateSolverSettings() {
    updateSolverSettings(true);
  }

  private void updateSolverSettings(boolean updData) {
    if (sim.getSolverManager().isEmpty()) {
        return;
    }
    printAction("Updating Solver Settings");
    if (leaveTemporaryStorage) {
        say("Temporary Storage enabled");
    }
    if (updData) {
        if (isUnsteady()) {
            ((StepStoppingCriterion) getStoppingCriteria("Maximum Steps.*", false)).setIsUsed(false);
            ((AbortFileStoppingCriterion) getStoppingCriteria("Stop File.*", false)).setInnerIterationCriterion(false);
            if (isImplicitUnsteady()) {
                setSolverMaxInnerIterations(trnInnerIter, false);
                setSolverPhysicalTimestep(trnTimestep, null, false);
                String timeDiscr = "1st Order";
                if (trn2ndOrder) {
                    timeDiscr = "2nd Order";
                }
                say("Time Discretization: " + timeDiscr);
                say("Maximum Inner Iterations: %d", trnInnerIter);
                say("Physical Timestep: %g %s", trnTimestep, defUnitTime.getPresentationName());
            }
            setSolverMaxPhysicalTimestep(trnMaxTime, defUnitTime, false);
            say("Maximum Physical Time: %g %s", trnMaxTime, defUnitTime.getPresentationName());
        } else {
            setSolverMaxIterations(maxIter, false);
            say("Maximum Number of Iterations: %d", maxIter);
        }
    }
    if (hasCoupledExplicitFlow()) {
        CoupledExplicitSolver ces = ((CoupledExplicitSolver) getSolver(CoupledExplicitSolver.class));
        if (leaveTemporaryStorage) {
            ces.setLeaveTemporaryStorage(leaveTemporaryStorage);
        }
    }
    if (hasCoupledImplicitFlow()) {
        CoupledImplicitSolver cis = ((CoupledImplicitSolver) getSolver(CoupledImplicitSolver.class));
        if (leaveTemporaryStorage) {
            cis.setLeaveTemporaryStorage(leaveTemporaryStorage);
        }
        if (updData) {
            cis.setCFL(CFL);
            say("CFL: " + CFL);
            updateSolverSettings_AMG(cis.getAMGLinearSolver(), amgConvTol, amgVerbosity);
        }
    }
    if (hasEMP()) {
        SegregatedMultiPhaseSolver empSlv = ((SegregatedMultiPhaseSolver) getSolver(SegregatedMultiPhaseSolver.class));
        if (leaveTemporaryStorage) {
            empSlv.setLeaveTemporaryStorage(leaveTemporaryStorage);
        }
        MultiPhaseVelocitySolver empVelSlv = empSlv.getVelocitySolver();
        MultiPhasePressureSolver emppSlv = empSlv.getPressureSolver();
        if (updData) {
            empVelSlv.setUrf(urfPhsCplVel);
            emppSlv.setUrf(urfP);
            say("URF Phase Couple Velocity: %g", urfPhsCplVel);
            say("URF Pressure: %g", urfP);
            updateSolverSettings_AMG(empVelSlv.getAMGLinearSolver(), amgConvTol, amgVerbosity);
            updateSolverSettings_AMG(emppSlv.getAMGLinearSolver(), amgConvTol, amgVerbosity);
        }
    }
    if (hasSegregatedFlow()) {
        SegregatedFlowSolver sfs = ((SegregatedFlowSolver) getSolver(SegregatedFlowSolver.class));
        if (leaveTemporaryStorage) {
            sfs.setLeaveTemporaryStorage(leaveTemporaryStorage);
        }
        VelocitySolver vSolv = sfs.getVelocitySolver();
        PressureSolver pSolv = sfs.getPressureSolver();
        if (updData) {
            vSolv.setUrf(urfVel);
            pSolv.setUrf(urfP);
            say("URF Velocity: %g", urfVel);
            updateSolverSettings_Ramp(pSolv.getRampCalculatorManager(),
                    "Velocity", rampURF_Vel, urfRampVelIterBeg, urfRampVelIterEnd, urfRampVelBeg);
            say("URF Pressure: %g", urfP);
            updateSolverSettings_Ramp(pSolv.getRampCalculatorManager(),
                    "Pressure", rampURF_P, urfRampPressIterBeg, urfRampPressIterEnd, urfRampPressBeg);
            updateSolverSettings_AMG(pSolv.getAMGLinearSolver(), amgConvTol, amgVerbosity);
            updateSolverSettings_AMG(vSolv.getAMGLinearSolver(), amgConvTol, amgVerbosity);
        }
    }
    if (hasSegregatedEnergy()) {
        SegregatedEnergySolver enrgySolv = ((SegregatedEnergySolver) getSolver(SegregatedEnergySolver.class));
        if (leaveTemporaryStorage) {
            enrgySolv.setLeaveTemporaryStorage(leaveTemporaryStorage);
        }
        enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        if (updData) {
            if (urfEnergy > 0) {
                say("Using the same Energy URF for both Fluids and Solids...");
                urfFluidEnrgy = urfSolidEnrgy = urfEnergy;
            }
            enrgySolv.setFluidUrf(urfFluidEnrgy);
            enrgySolv.setSolidUrf(urfSolidEnrgy);
            say("URF Fluid Energy: %g", urfFluidEnrgy);
            updateSolverSettings_Ramp(enrgySolv.getFluidRampCalculatorManager(),
                    "Fluid Energy", rampURF_T, urfRampFlIterBeg, urfRampFlIterEnd, urfRampFlBeg);
            say("URF Solid Energy: %g", urfSolidEnrgy);
            updateSolverSettings_Ramp(enrgySolv.getFluidRampCalculatorManager(),
                    "Solid Energy", rampURF_T, urfRampSldIterBeg, urfRampSldIterEnd, urfRampSldBeg);
            updateSolverSettings_AMG(enrgySolv.getAMGLinearSolver(), amgConvTol, amgVerbosity);
        }
    }
    updateSolverSettings(GranularTemperatureTransportSolver.class, "Granular Temperature", urfGranTemp, updData);
    updateSolverSettings(KeTurbSolver.class, "K-Epsilon Turbulence", urfKEps, updData);
    updateSolverSettings(KeTurbViscositySolver.class, "K-Epsilon Turbulent Viscosity", urfKEpsTurbVisc, updData);
    updateSolverSettings(KwTurbSolver.class, "K-Omega Turbulence", urfKOmega, updData);
    updateSolverSettings(KwTurbViscositySolver.class, "K-Omega Turbulent Viscosity", urfKOmegaTurbVisc, updData);
    updateSolverSettings(RsTurbSolver.class, "Reynolds Stress Turbulence", urfRS, updData);
    updateSolverSettings(RsTurbViscositySolver.class, "Reynolds Stress Turbulent Viscosity", urfRSTurbVisc, updData);
    updateSolverSettings(SegregatedSpeciesSolver.class, "Segregated Species", urfSpecies, updData);
    updateSolverSettings(SegregatedVofSolver.class, "Segregated VOF", urfVOF, updData);
    updateSolverSettings(VolumeFractionSolver.class, "Volume Fraction", urfVolFrac, updData);
    sayOK();
  }

  private void updateSolverSettings(Class cl, String hasStr, double urf, boolean updData) {
    Solver slv = getSolver(cl);
    if (slv == null || !sim.getSolverManager().has(hasStr) || !updData) {
        return;
    }
    say("URF %s: %g", hasStr, urf);
    if (hasStr.endsWith("Turbulent Viscosity")) {
        TurbViscositySolver tvs = (TurbViscositySolver) slv;
        tvs.setViscosityUrf(urf);
        return;
    }
    ScalarSolverBase ssb = (ScalarSolverBase) slv;
    if (leaveTemporaryStorage) {
        ssb.setLeaveTemporaryStorage(leaveTemporaryStorage);
    }
    ssb.setUrf(urf);
    ssb.getAMGLinearSolver().setConvergeTol(amgConvTol);
    ssb.getAMGLinearSolver().setVerbosity(amgVerbosity);
  }

  /**
   * Updates AMG stuff.
   */
  private void updateSolverSettings_AMG(AMGLinearSolver amg, double conv, int verb) {
    amg.setConvergeTol(conv);
    amg.setVerbosity(verb);
  }

  private void updateSolverSettings_Ramp(RampCalculatorManager rcm, String msg, boolean opt, int i0, int i1, double urf0) {
    if (!opt) {
        return;
    }
    rcm.getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
    LinearRampCalculator lrc = (LinearRampCalculator) rcm.getCalculator();
    lrc.setStartIteration(i0);
    lrc.setEndIteration(i1);
    lrc.setInitialRampValue(urf0);
    say("Linear Ramp %s:", msg);
    say("   Start/End Iteration: %d/%d", i0, i1);
    say("   Initial URF: %g", urf0);
  }

  /**
   * Private Adding/Updating Units
   * @param verboseOption
   */
  private void updateUnits(boolean verboseOption) {
    printAction("Adding/Updating Units", verboseOption);
    unit_C = queryUnit("C", verboseOption);
    unit_deg = queryUnit("deg", verboseOption);
    unit_Dimensionless = queryUnit("", verboseOption);
    unit_F = queryUnit("F", verboseOption);
    unit_K = queryUnit("K", verboseOption);
    unit_gal = queryUnit("gal", verboseOption);
    unit_kg = queryUnit("kg", verboseOption);
    unit_h = queryUnit("hr", verboseOption);
    unit_m = queryUnit("m", verboseOption);
    unit_m2 = queryUnit("m^2", verboseOption);
    unit_m3 = queryUnit("m^3", verboseOption);
    unit_min = queryUnit("min", verboseOption);
    unit_mm = queryUnit("mm", verboseOption);
    unit_mm2 = queryUnit("mm^2", verboseOption);
    unit_mm3 = queryUnit("mm^3", verboseOption);
    unit_N = queryUnit("N", verboseOption);
    unit_kph = queryUnit("kph", verboseOption);
    unit_kgpm3 = queryUnit("kg/m^3", verboseOption);
    unit_kgps = queryUnit("kg/s", verboseOption);
    unit_kmol = queryUnit("kmol", verboseOption);
    unit_mps = queryUnit("m/s", verboseOption);
    unit_mps2 = queryUnit("m/s^2", verboseOption);
    unit_rpm = queryUnit("rpm", verboseOption);
    unit_Pa = queryUnit("Pa", verboseOption);
    unit_Pa_s = queryUnit("Pa-s", verboseOption);
    unit_radps = queryUnit("radian/s", verboseOption);
    unit_s = queryUnit("s", verboseOption);
    unit_Wpm2K = queryUnit("W/m^2-K", verboseOption);
    /*    CUSTOM UNITS      */
    dimDensity.setMass(1);
    dimDensity.setVolume(-1);
    dimForce.setForce(1);
    dimLength.setLength(1);
    dimMass.setMass(1);
    dimMassFlow.setMass(1);
    dimMassFlow.setTime(-1);
    dimMolFlow.setQuantity(1);
    dimMolFlow.setTime(-1);
    dimPress.setPressure(1);
    dimTime.setTime(1);
    dimVel.setVelocity(1);
    dimVisc.setPressure(1);
    dimVisc.setTime(1);
    dimVolFlow.setVolume(1);
    dimVolFlow.setTime(-1);
    /*    DENSITY UNITS [M/V]    */
    unit_gpcm3 = addUnit("g/cm^3", "gram per cubic centimeter", 1000, dimDensity);
    /*    FORCE UNITS [M*L/T^2]    */
    unit_kN = addUnit("kN", "kilonewton", 1000, dimForce);
    /*    MASS UNITS [M]    */
    unit_g = addUnit("g", "gram", 0.001, dimMass);
    /*    MASS FLOW UNITS [M/T]    */
    unit_kgph = addUnit("kg/h", "kilogram per hour", 1./3600, dimMassFlow);
    unit_kgpmin = addUnit("kg/min", "kilogram per minute", 1./60, dimMassFlow);
    unit_gpmin = addUnit("g/min", "gram per minute", 0.001/60, dimMassFlow);
    unit_gps = addUnit("g/s", "gram per second", 0.001, dimMassFlow);
    /*    MOLECULAR FLOW UNITS [Mol/T]    */
    unit_kmolps = addUnit("kmol/s", "kilogram-mol per second", 1.0, dimMolFlow);
    /*    PRESSURE UNITS [P]    */
    //--- http://www.sensorsone.co.uk/pressure-units-conversion.html
    //--- http://www.onlineconversion.com/pressure.htm
    unit_cmH2O = addUnit("cmH2O", "centimeter of water", 98.0665, dimPress);
    unit_cmHg = addUnit("cmHg", "centimeter of mercury", 1333.2239, dimPress);
    unit_dynepcm2 = addUnit("dyne/cm^2", "dyne per square centimeter", 0.1, dimPress);
    unit_kPa = addUnit("kPa", "kilopascal", 1000, dimPress);
    unit_mbar = addUnit("mbar", "millibar", 100, dimPress);
    unit_mmH2O = addUnit("mmH2O", "millimeter of water", 9.80665, dimPress);
    unit_mmHg = addUnit("mmHg", "millimeter of mercury", 133.32239, dimPress);
    unit_uPa = addUnit("uPa", "micropascal", 1e-6, dimPress);
    /*    TIME UNITS [T]    */
    unit_ms = addUnit("ms", "milliseconds", 0.001, dimTime);
    /*    VELOCITY UNITS [L/T]    */
    unit_kt = addUnit("kt", "knot", 1852./3600., dimVel);
    unit_mmps = addUnit("mm/s", "millimeter per second", 0.001, dimVel);
    unit_umps = addUnit("um/s", "micrometer per second", 1e-6, dimVel);
    /*    VISCOSITY UNITS [P*T]    */
    unit_P = addUnit("P", "Poise", 0.1, dimVisc);
    unit_cP = addUnit("cP", "centipoise", 0.001, dimVisc);
    /*    VOLUMETRIC FLOW RATE UNITS [V/T]    */
    unit_galps = addUnit("gal/s", "gallons per second", 0.00378541, dimVolFlow);
    unit_galpmin = addUnit("gal/min", "gallons per minute", 0.00378541/60, dimVolFlow);
    unit_lph = addUnit("l/h", "liter per hour", 0.001/3600, dimVolFlow);
    unit_lpmin = addUnit("l/min", "liter per minute", 0.001/60, dimVolFlow);
    unit_lps = addUnit("l/s", "liter per second", 0.001, dimVolFlow);
    unit_m3ph = addUnit("m^3/h", "cubic meter per hour", 1./3600, dimVolFlow);
    sayOK(verboseOption);
    updateDefaultUnits(verboseOption);
  }

  /**
   * Updates the View Factors if applicable.
   */
  public void updateViewFactors() {
    if (!hasViewFactors()) {
        return;
    }
    ViewfactorsCalculatorSolver vfc = getViewFactorCalcSolver();
    if (!vfc.hasValidViewfactors()) {
        vfc.generatePatches();
        vfc.calculateViewfactors();
    }
  }

  /**
   * Stores all the Camera Views into a filename. The Cameras will be read from
   * {@see #getCameraViews}.
   *
   * @param filename given filename. File will be saved in {@see #simPath} folder.
   */
  public void writeCameraViews(String filename) {
    writeCameraViews(filename, getCameraViews(false), true);
  }

  /**
   * Stores the given Camera Views into a filename.
   *
   * @param filename given filename. File will be saved in {@see #simPath} folder.
   * @param cameras given Cameras in the format defined in {@see #getCameraViews}.
   */
  public void writeCameraViews(String filename, String cameras) {
    writeCameraViews(filename, cameras, true);
  }

  /**
   * @param filename
   * @param cameras
   * @param verboseOption
   */
  private void writeCameraViews(String filename, String cameras, boolean verboseOption) {
    printAction("Writing Camera Views", verboseOption);
    ArrayList<String> als = new ArrayList<String>();
    try {
        als = new ArrayList(Arrays.asList(cameras.split(camSplitCharBetweenCams)));
    } catch (Exception e) {
        say("ERROR! Could not read Cameras. Make sure the correct data is provided.");
        return;
    }
    say("Cameras given: " + als.size());
    if (als.isEmpty()) {
        say("Correct data not provided. Nothing will be written.");
        return;
    }
    writeData(new File(simPath, filename), als, verboseOption);
  }

  /**
   * Writes String data to a file.
   *
   * @param filename given filename. File will be saved in {@see #simPath} folder.
   * @param data given array of Strings.
   */
  public void writeData(String filename, String[] data) {
    writeData(new File(simPath, filename), new ArrayList(Arrays.asList(data)), true);
  }

  /**
   * Writes String data to a file.
   *
   * @param f given {@see java.io.File}.
   * @param als given ArrayList of Strings.
   * @param verboseOption Print to standard output?
   */
  public void writeData(File f, ArrayList<String> als, boolean verboseOption) {
    say("Writing contents to a file...", verboseOption);
    say("  File: " + f.getAbsolutePath(), verboseOption);
    BufferedWriter fileWriter = null;
    try {
        if (f.exists()) {
            say("  Already exists. Overwriting...", verboseOption);
        }
        fileWriter = new BufferedWriter(new FileWriter(f));
        for (String s : als) {
            fileWriter.write(s);
            fileWriter.newLine();
        }
        fileWriter.close();
        say("  Saved.", verboseOption);
    } catch (IOException ex) {
        say("  Could not save file. Exiting...", verboseOption);
    } finally {
        try {
            fileWriter.close();
         } catch (IOException ex) {
            say("  Could not close file. It might be corrupt or unusable.", verboseOption);
         }
     }
  }

  /***********************************************************
   ***********************************************************
   *   V A R I A B L E    D E F I N I T I O N S    A R E A   *
   ***********************************************************
   ***********************************************************/

  //--
  //--  Private Definitions
  //--
  private boolean meshedInMacroUtils = false;
  private int colourByPart = 4;
  private int colourByRegion = 2;
  private int gciOscilConv = 0;
  private int hasBeenMeshed = 0;
  private int postStreamlineResU = 5;
  private int postStreamlineResV = 5;
  private int savedWithSuffix = 0;
  private String fontAnnot = "Lucida Sans";
  //private String fontAnnot = "Courier New";   // Mismatch between Windows and Linux systems.
  //private String fontAnnot = "Meiryo UI";     // Some Linux systems do not have this installed.
  private String noneString = "none";
  private String tempCamName = "_tmpCam";
  private String sayPreffixString = "[*]";

  //--
  //--  Private Filename Filters
  //--

  private FilenameFilter dbsFilter = new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
        // Return files ending with dbs -- with ignore case group (?i)
        return name.matches("(?i).*dbs");
    }
  };

  //--
  //-- Immutable definitions
  //--
  final private Object[] emptyObj = new Object[] {};
  final private NeoObjectVector emptyNeoObjVec = new NeoObjectVector(emptyObj);
  final private String cadDirs = "XYZ";
  final private String camSplitCharBetweenCams = ";";
  final private String camSplitCharBetweenFields = "|";
  final private String camVecs = retRepeatString("%.6e,%.6e,%.6e ", 3);
  final private String camFormatOld = ("%s " + camVecs + "%.6e").replaceAll(" ", camSplitCharBetweenFields);
  final private String camFormat = ("%s " + camVecs + "%.6e %d").replaceAll(" ", camSplitCharBetweenFields);
  final private String unitFormat = "%s: %s (%s)";
  final private String fmtCFL = "%4.2f";
  final private String fmtTime = "%6.3f";
  final private String OS = System.getProperty("os.name").toLowerCase();
//  final private VisView vv0 = new VisView(null)
  final private CameraStateInput csi0 = new CameraStateInput(new double[] {0,0,0},
            new double[] {1,0,0}, new double[] {0,0,1}, 1.0, 1);

  //--
  //-- Immutable Public definitions
  //--

  /** Special Macro Utils variable for creating a Physics Continua Model in Space: Two-Dimensional. */
  final public int _2D = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model in Space: Three-Dimensional. */
  final public int _3D = 3;

  /** Special Macro Utils variable for creating a Physics Continua Model in Time: Steady State. */
  final public int _STEADY = 1;

  /** Special Macro Utils variable for creating a Physics Continua Model in Time: Unsteady. */
  final public int _IMPLICIT_UNSTEADY = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model in Time: Explicit Unsteady. */
  final public int _EXPLICIT_UNSTEADY = 3;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model in Material: Single
   * Component Gas.
   */
  final public int _GAS = 1;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model in Material: Single
   * Component Liquid.
   */
  final public int _LIQUID = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model in Material: Solid. */
  final public int _SOLID = 3;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model in Material: Volume of
   * Fluid (VOF). No phases created.
   */
  final public int _VOF = 4;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model in Material: Volume of
   * Fluid (VOF) with Air and Water.
   */
  final public int _VOF_AIR_WATER = 41;

  /** Special Macro Utils variable for creating a Physics Continua Model with the Segregated Solver. */
  final public int _SEGREGATED = 1;

  /** Special Macro Utils variable for creating a Physics Continua Model with the Coupled Solver. */
  final public int _COUPLED = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model with a Constant Density fluid. */
  final public int _INCOMPRESSIBLE = 1;

  /** Special Macro Utils variable for creating a Physics Continua Model with an Ideal Gas fluid. */
  final public int _IDEAL_GAS = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model with an Isothermal fluid. */
  final public int _ISOTHERMAL = 1;

  /** Special Macro Utils variable for creating a Physics Continua Model with Heat Transfer. */
  final public int _THERMAL = 2;

  /** Special Macro Utils variable for creating a Physics Continua Model with Inviscid viscous regime. */
  final public int _INVISCID = 0;

  /** Special Macro Utils variable for creating a Physics Continua Model with Laminar viscous regime. */
  final public int _LAMINAR = 1;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Standard K-Epsilon high y+ wall model.
   */
  final public int _KE_STD = 20;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Realizable K-Epsilon 2 layer all y+ wall model.
   */
  final public int _RKE_2LAYER = 21;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Standard Wilcox K-Omega all y+ wall model, 1998 revised version.
   */
  final public int _KW_STD = 30;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Wilcox K-Omega all y+ wall model, 2006 revised version (published in 2008).
   */
  final public int _KW_2008 = 31;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Menter SST K-Omega all y+ wall model.
   */
  final public int _SST_KW = 38;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Menter Detached Eddy Simulation (IDDES formulation) SST K-Omega all y+ wall model.
   */
  final public int _DES_SST_KW_IDDES = 50;

  /**
   * Special Macro Utils variable for creating a Physics Continua Model with Turbulent viscous
   * regime with the Menter Detached Eddy Simulation (DDES formulation) SST K-Omega all y+ wall model.
   */
  final public int _DES_SST_KW_DDES = 51;

  /** Origin coordinates (0, 0, 0). */
  final public double[] coord0 = {0., 0., 0.};

  /** Just a small number. 1E-20 should be it. */
  final public double SN = 1E-20;

  /** Choice for <u>AND</u> logic type, when using Macro Utils. */
  final public int AND = 1;

  /** Choice for a Array type Field Function when using Macro Utils. */
  final public int FF_ARRAY = FieldFunctionTypeOption.ARRAY;

  /** Choice for a Position type Field Function when using Macro Utils. */
  final public int FF_POSITION = FieldFunctionTypeOption.POSITION;

  /** Choice for a Scalar type Field Function when using Macro Utils. */
  final public int FF_SCALAR = FieldFunctionTypeOption.SCALAR;

  /** Choice for a Vector type Field Function when using Macro Utils. */
  final public int FF_VECTOR = FieldFunctionTypeOption.VECTOR;

  /** Choice for <u>GREATER THAN OR EQUALS</u> operator type, when using Macro Utils. */
  final public int GE = 1;

  /** Choice for <u>LESS THAN OR EQUALs</u> operator type, when using Macro Utils. */
  final public int LE = 2;

  /** Choice for <u>OR</u> logic type, when using Macro Utils. */
  final public int OR = 2;

  /** Special Macro Utils variable to select the Polyhedral technology when volume meshing in Parts. */
  final public int POLY = 1;

  /** Coarse Tessellation option when working with Parts. It is based on {@see TessellationDensityOption}. */
  final public int TESSELATION_COARSE = TessellationDensityOption.COARSE;

  /** Fine Tessellation option when working with Parts. It is based on {@see TessellationDensityOption}. */
  final public int TESSELATION_FINE = TessellationDensityOption.FINE;

  /** Medium Tessellation option when working with Parts. It is based on {@see TessellationDensityOption}. */
  final public int TESSELATION_MEDIUM = TessellationDensityOption.MEDIUM;

  /** Very Coarse Tessellation option when working with Parts. It is based on {@see TessellationDensityOption}. */
  final public int TESSELATION_VERY_COARSE = TessellationDensityOption.VERY_COARSE;

  /** Very Fine Tessellation option when working with Parts. It is based on {@see TessellationDensityOption}. */
  final public int TESSELATION_VERY_FINE = TessellationDensityOption.VERY_FINE;

  /** Special Macro Utils variable to select the Tetrahedral technology when volume meshing in Parts. */
  final public int TETRA = 3;

  /** Special Macro Utils variable to select the Trimmer technology when volume meshing in Parts. */
  final public int TRIMMER = 2;

  /** Special Macro Utils variable to select the Trimmer Volume Growth Rate. */
  final public int TRIMMER_GROWTH_RATE_FAST = PartsGrowthRateOption.FAST;

  /** Special Macro Utils variable to select the Trimmer Volume Growth Rate. */
  final public int TRIMMER_GROWTH_RATE_MEDIUM = PartsGrowthRateOption.MEDIUM;

  /** Special Macro Utils variable to select the Trimmer Volume Growth Rate. */
  final public int TRIMMER_GROWTH_RATE_SLOW = PartsGrowthRateOption.SLOW;

  /** Special Macro Utils variable to select the Trimmer Volume Growth Rate. */
  final public int TRIMMER_GROWTH_RATE_VERYSLOW = PartsGrowthRateOption.VERYSLOW;

  /** String referring to X direction. */
  final public String X = "X";

  /** Choice for <u>XOR</u> logic type, when using Macro Utils. */
  final public int XOR = 3;

  /** String referring to Y direction. */
  final public String Y = "Y";

  /** String referring to Z direction. */
  final public String Z = "Z";

  /** CFL variable name inside STAR-CCM+. */
  final public String varCFL = "Convective Courant Number";

  /** Density variable name inside STAR-CCM+. */
  final public String varDen = "Density";

  /** Thermal Conductivity variable name inside STAR-CCM+. */
  final public String varK = "Thermal Conductivity";

  /** Pressure variable name inside STAR-CCM+. */
  final public String varP = "Pressure";

  /** Turbulent Prandtl Number variable name inside STAR-CCM+. */
  final public String varPrTurb = "Turbulent Prandtl Number";

  /** Pressure Coefficient variable name inside STAR-CCM+. */
  final public String varPC = "Pressure Coefficient";

  /** Specific Heat variable name inside STAR-CCM+. */
  final public String varCp = "Specific Heat";

  /** Temperature variable name inside STAR-CCM+. */
  final public String varT = "Temperature";

  /** Turbulence Intensity variable name inside STAR-CCM+. Note this is not a Field Function. */
  final public String varTI = "Turbulence Intensity";

  /** Turbulent Viscosity Ratio variable name inside STAR-CCM+. */
  final public String varTVR = "Turbulent Viscosity Ratio";

  /** Turbulent Velocity Scale variable name inside STAR-CCM+. Note this is not a Field Function. */
  final public String varTVS = "Turbulent Velocity Scale";

  /** Velocity variable name inside STAR-CCM+. */
  final public String varVel = "Velocity";

  /** Dynamic Viscosity variable name inside STAR-CCM+. */
  final public String varVisc = "Dynamic Viscosity";

  /** Wall Y+ variable name inside STAR-CCM+. */
  final public String varYplus = "WallYplus";

  /** XYZ Coordinates as a String Array (X, Y, Z). Immutable. */
  final public String[] xyzCoord = {"X", "Y", "Z"};

  //--
  //-- Public API variables from STAR-CCM+.
  //--

  /** Some useful Global Variables: Auto Mesh Operations. */
  public MeshOperation autoMshOp = null, autoMshOp1 = null, autoMshOp2 = null, autoMshOp3 = null;

  /** A Global Boundary variable. Useful somewhere. */
  public Boundary bdry = null, bdry1 = null, bdry2 = null, bdry3 = null;

  /** A Global 3D-CAD Body variable. Useful somewhere. */
  public Body cadBody = null, cadBody1 = null, cadBody2 = null, cadBody3 = null;

  /** A Global CadModel variable. Useful somewhere. */
  public CadModel cadModel = null, cadModel1 = null, cadModel2 = null, cadModel3 = null;

  /** A Global CadPart variable. Useful somewhere. */
  public CadPart cadPrt = null, cadPrt1 = null, cadPrt2 = null, cadPrt3 = null;

  /** A Global CellSurfacePart variable. Useful somewhere. */
  public CellSurfacePart cellSet = null, cellSrf = null;

  /** A Global CompositePart variable. Useful somewhere. */
  public CompositePart compPart = null, compPart1 = null, compPart2 = null, compPart3 = null;

  /** The Original Laboratory Coordinate System. Useful somewhere. */
  public CoordinateSystem csys0 = null;

  /** Some useful Global Variables: Coordinate System. */
  public CoordinateSystem csys = null, csys1 = null, csys2 = null, csys3 = null;

  /** Some useful Global Variables: Delta Monitor Update Event. */
  public DeltaMonitorUpdateEvent updEventDelta = null, updEventDelta1 = null, updEventDelta2 = null, updEventDelta3 = null;

  /** Some useful Global Variables: Displayer. */
  public Displayer disp = null, disp1 = null, disp2 = null, disp3 = null;

  /** Just an empty DoubleVector. Useful somewhere. */
  public DoubleVector dv0 = new DoubleVector(coord0);

  /** A Global Feature Curve variable. Useful somewhere. */
  public FeatureCurve featCrv = null;

  /** Some useful Global Variables: Field Function. */
  public FieldFunction ff = null, ff1 = null, ff2 = null, ff3 = null;

  /** Some useful Global Variables: Frequency Monitor Update Event. */
  public FrequencyMonitorUpdateEvent updEventFreq = null, updEventFreq1 = null, updEventFreq2 = null, updEventFreq3 = null;

  /** Some useful Global Variables: Geometry Parts. */
  public GeometryPart geomPrt = null, geomPrt1 = null, geomPrt2 = null, geomPrt3 = null;

  /** A Global Interface variable. Useful somewhere. */
  public Interface intrf = null, intrf1 = null, intrf2 = null, intrf3 = null;

  /** The Original Laboratory Coordinate System. Useful somewhere. */
  public LabCoordinateSystem lab0 = null;

  /** Some useful Global Variables: Leaf Mesh Parts. */
  public LeafMeshPart leafMshPrt = null, leafMshPrt1 = null, leafMshPrt2 = null, leafMshPrt3 = null;

  /** Some useful Global Variables: Lines. */
  public LinePart line = null, line1 = null, line2 = null;

  /** Some useful Global Variables: Logic Update Event. */
  public LogicUpdateEvent updEventLogic = null, updEventLogic1 = null, updEventLogic2 = null, updEventLogic3 = null;

  /** Default Colormap for Scalar Displayers. */
  public LookupTable defColormap = null;

  /**
   * Some useful Global Variables: Mesh Continuas.
   * @deprecated in v3c as STAR-CCM+ is being focused in Parts Based Mesh operations.
   */
  public MeshContinuum mshCont = null, mshCont1 = null, mshCont2 = null;

  /** Some useful Global Variables: Mesh Operations. */
  public MeshOperation mshOp = null, mshOp1 = null, mshOp2 = null, mshOp3 = null;

  /** Some useful Global Variables: Mesh Operation Parts. */
  public MeshOperationPart mshOpPrt = null, mshOpPrt1 = null, mshOpPrt2 = null, mshOpPrt3 = null;

  /** Some useful Global Variables: Monitors. */
  public Monitor mon = null, mon1 = null, mon2 = null;

  /** Global MacroUtils variable for storing a Monitor Plot. Useful when creating a Report.
   * E.g.: {@see #createReportMassFlow}
   */
  public MonitorPlot monPlot = null;

  /** Some useful Global Variables: Planes. */
  public PlaneSection plane = null, plane1 = null, plane2 = null;

  /** Some useful Global Variables: Part Curves. */
  public PartCurve partCrv = null, partCrv1 = null, partCrv2 = null, partCrv3 = null;

  /** Some useful Global Variables: Part Surfaces. */
  public PartSurface partSrf = null, partSrf1 = null, partSrf2 = null, partSrf3 = null;

  /** Some useful Global Variables: Points. */
  public PointPart point = null, point1 = null, point2 = null, point3 = null, point4 = null, point5 = null;

  /** Some useful Global Variables: Physics Continuas. */
  public PhysicsContinuum physCont = null, physCont1 = null, physCont2 = null, physCont3 = null;

  /** Some useful Global Variables: Range Monitor Update Event. */
  public RangeMonitorUpdateEvent updEventRange = null, updEventRange1 = null, updEventRange2 = null, updEventRange3 = null;

  /** Some useful Global Variables: Recorded Solution View. */
  public RecordedSolutionView recSolView = null, recSolView1 = null, recSolView2 = null, recSolView3 = null;

  /** Some useful Global Variables: Regions. */
  public Region region = null, region1 = null, region2 = null, region3 = null;

  /** Some useful Global Variables: Reports. */
  public Report rep = null, rep1 = null, rep2 = null, rep3 = null;

  /** Global MacroUtils variable for storing a Report Monitor. Useful when creating a Report.
   * E.g.: {@see #createReportMassFlow}
   */
  public ReportMonitor repMon = null;

  /** Some useful Global Variables: Scenes. */
  public Scene scene = null, scene1 = null, scene2 = null, scene3 = null;

  /** Some useful Global Variables: Simple Block Parts. */
  public SimpleBlockPart simpleBlkPrt = null, simpleBlkPrt1 = null, simpleBlkPrt2 = null, simpleBlkPrt3 = null;

  /**
   * This variable is the current session in STAR-CCM+. It is the first variable initialized in
   * {@see #}. If not initialized, an error will occur.
   */
  public Simulation sim = null;

  /** Some useful Global Variables: Simple Cylinder Parts. */
  public SimpleCylinderPart simpleCylPrt = null, simpleCylPrt1 = null, simpleCylPrt2 = null, simpleCylPrt3 = null;

  /** Some useful Global Variables: Simple Sphere Parts. */
  public SimpleSpherePart simpleSphPrt = null, simpleSphPrt1 = null, simpleSphPrt2 = null, simpleSphPrt3 = null;

  /** A Global SolidModelPart variable. Useful somewhere. */
  public SolidModelPart sldPrt = null, sldPrt1 = null, sldPrt2 = null, sldPrt3 = null;

  /** Some useful Global Variables: Solution History. */
  public SolutionHistory solHist = null, solHist1 = null, solHist2 = null, solHist3 = null;

  /** Some useful Global Variables: Solution Representation. */
  public SolutionRepresentation solRepr = null, solRepr1 = null, solRepr2 = null, solRepr3 = null;

  /** Some useful Global Variables: Solution View. */
  public SolutionView solView = null, solView1 = null, solView2 = null, solView3 = null;

  /** Some useful Global Variables: Star Plots. */
  public StarPlot starPlot = null, starPlot1 = null, starPlot2 = null;

  /** Some useful Global Variables: Mesh Operation Custom Surface Controls. */
  public SurfaceCustomMeshControl mshOpSrfCtrl = null, mshOpSrfCtrl1 = null, mshOpSrfCtrl2 = null, mshOpSrfCtrl3 = null;

  /** Some useful Global Variables: Surface Wrapper Mesh Operation. */
  public SurfaceWrapperAutoMeshOperation wrapMshOp = null, wrapMshOp1 = null, wrapMshOp2 = null, wrapMshOp3 = null;

  /** Some useful Global Variables: Tables. */
  public Table table = null, table1 = null, table2 = null, table3 = null;

  /** Some useful Global Variables: Update Event. */
  public UpdateEvent updEvent = null, updEvent1 = null, updEvent2 = null, updEvent3 = null;

  /** Some useful Global Variables: Visualization View (Camera View). */
  public VisView camView = null, camView1 = null, camView2 = null, camView3 = null;

  /** Default Camera when creating new Scenes. For more see {@see #createCamView}. */
  public VisView defCamView = null;

  /** Some useful Global Variables: Visualization View (Camera View). */
  public VisView vv = null, vv1 = null, vv2 = null, vv3 = null, vv4 = null, vv5 = null, vv6 = null;

  //--
  //-- Public ArrayList's
  //--

  /** Useful Variable for storing Doubles. */
  public ArrayList<Double> doubles = new ArrayList(), doubles2 = new ArrayList();

  /** Useful Variable for storing Files. */
  public ArrayList<File> files = new ArrayList(), files2 = new ArrayList();

  /** Useful Variable for storing Strings. */
  public ArrayList<String> strings = new ArrayList(), strings2 = new ArrayList();

  //--
  //-- Public ArrayList's from STAR-CCM+ API
  //--

  /** Useful Variable for storing Boundaries. */
  public ArrayList<Boundary> boundaries = new ArrayList(), boundaries2 = new ArrayList();

  /** Useful Variable for storing Cad Bodies. */
  public ArrayList<Body> cadBodies = new ArrayList(), cadBodies2 = new ArrayList();

  /** Useful Variable for storing Cad Models. */
  public ArrayList<Body> cadModels = new ArrayList(), cadModels2 = new ArrayList();

  /** Useful Variable for storing Cad Parts. */
  public ArrayList<CadPart> cadParts = new ArrayList(), cadParts2 = new ArrayList();

  /** Useful Variable for storing Colors. */
  public ArrayList<Color> colors = new ArrayList(), colors2 = new ArrayList();

  /** Useful Variable for storing Composite Parts. */
  public ArrayList<CompositePart> compositeParts = new ArrayList(), compositeParts2 = new ArrayList();

  /** Useful Variable for storing Double Vectors. */
  public DoubleVector doubleVector = new DoubleVector(), doubleVector2 = new DoubleVector();

  /** Useful Variable for storing Field Functions. */
  public ArrayList<FieldFunction> fieldFunctions = new ArrayList(), fieldFunctions2 = new ArrayList();

  /** Useful Variable for storing Geometry Objects (STAR-CCM+ Geometry Object). */
  public ArrayList<GeometryObject> geometryObjects = new ArrayList(), geometryObjects2 = new ArrayList();

  /** Useful Variable for storing Geometry Parts. */
  public ArrayList<GeometryPart> geometryParts = new ArrayList(), geometryParts2 = new ArrayList();

  /** Useful Variable for storing Leaf Mesh Parts. */
  public ArrayList<LeafMeshPart> leafMeshParts = new ArrayList(), leafMeshParts2 = new ArrayList();

  /** Useful Variable for storing Meshers. */
  public ArrayList<String> meshers = new ArrayList(), meshers2 = new ArrayList();

  /** Useful Variable for storing Mesh Parts. */
  public ArrayList<MeshPart> meshParts = new ArrayList(), meshParts2 = new ArrayList();

  /** Useful Variable for storing Monitors. */
  public ArrayList<Monitor> monitors = new ArrayList(), monitors2 = new ArrayList();

  /** Useful Variable for storing Named Objects (STAR-CCM+ Object). */
  public ArrayList<NamedObject> namedObjects = new ArrayList(), namedObjects2 = new ArrayList();

  /** Useful Variable for storing Objects. */
  public ArrayList<Object> objects = new ArrayList(), objects2 = new ArrayList();

  /** Useful Variable for storing Part Curves. */
  public ArrayList<PartCurve> partCurves = new ArrayList(), partCurves2 = new ArrayList();

  /** Useful Variable for storing Part Surfaces. */
  public ArrayList<PartSurface> partSurfaces = new ArrayList(), partSurfaces2 = new ArrayList();

  /** Useful Variable for storing Planes. */
  public ArrayList<PlaneSection> planes = new ArrayList(), planes2 = new ArrayList();

  /** Useful Variable for storing Regions. */
  public ArrayList<Region> regions = new ArrayList(), regions2 = new ArrayList();

  /** Useful Variable for storing Reports. */
  public ArrayList<Report> reports = new ArrayList(), reports2 = new ArrayList();

  /** Useful Variable for storing Scenes. */
  public ArrayList<Scene> scenes = new ArrayList(), scenes2 = new ArrayList();

  /** Useful Variable for storing Camera Views. */
  public ArrayList<VisView> cameras = new ArrayList(), cameras2 = new ArrayList();

  //--
  //-- Public Booleans
  //--

  /** Enable Cell Quality Remediation when creating Physics Continua with Macro Utils?. Default = yes. */
  public boolean enableCQR = true;

  /** Prints extra messages when using Macro Utils. Useful for debugging. */
  public boolean verboseDebug = false;

  /**
   * Enable temporary storage in Solvers? Useful when debugging and with {@see #updateSolverSettings}.
   * Default = false.
   */
  public boolean leaveTemporaryStorage = false;

  /** Save intermediate files when troubleshooting a macro? Useful with {@see #saveSimWithSuffix}. Default = yes. */
  public boolean saveIntermediates = true;

  /** Useful variable for querying if the Simulation has stopped or not. */
  public boolean stopMacroUtils = false;


  //--
  //-- Public Colors. Based on java.awt.Color().
  //--

  /** Blue according to {@see java.awt.Color} class. */
  public Color colorBlue = Color.blue;

  /** Blue Medium according to {@see java.awt.Color} class. */
  public Color colorBlueMedium = new Color(0, 0, 205);

  /** Dark Green according to {@see java.awt.Color} class. */
  public Color colorDarkGreen = new Color(0, 100, 0);

  /** Dark Orange according to {@see java.awt.Color} class. */
  public Color colorDarkOrange = new Color(255, 140, 0);

  /** Dim Gray according to {@see java.awt.Color} class. */
  public Color colorDimGray = new Color(105, 105, 105);

  /** Ivory according to {@see java.awt.Color} class. */
  public Color colorIvory = new Color(255, 255, 240);

  /** Ivory Black according to {@see java.awt.Color} class. */
  public Color colorIvoryBlack = new Color(41, 36, 33);

  /** Light Gray according to {@see java.awt.Color} class. */
  public Color colorLightGray = Color.lightGray;

  /** Lime Green according to {@see java.awt.Color} class. */
  public Color colorLimeGreen = new Color(50, 205, 50);

  /** Mint according to {@see java.awt.Color} class. */
  public Color colorMint = new Color(189, 252, 201);

  /** Navy according to {@see java.awt.Color} class. */
  public Color colorNavy = new Color(0, 0, 128);

  /** Purple according to {@see java.awt.Color} class. */
  public Color colorPurple = new Color(160, 32, 240);

  /** Slate Gray according to {@see java.awt.Color} class. */
  public Color colorSlateGray = new Color(112, 128, 144);

  /** Slate Gray Dark according to {@see java.awt.Color} class. */
  public Color colorSlateGrayDark = new Color(47, 79, 79);

  /** Ultramarine according to {@see java.awt.Color} class. */
  public Color colorUltramarine = new Color(18, 10, 143);

  /** Wheat according to {@see java.awt.Color} class. */
  public Color colorWheat = new Color(245, 222, 179);

  /** White according to {@see java.awt.Color} class. */
  public Color colorWhite = Color.white;

  /** White Smoke according to {@see java.awt.Color} class. */
  public Color colorWhiteSmoke = new Color(245, 245, 245);

  //--
  //-- A few fonts based on java.awt.Font().
  //--

  // Check this "Dialog-italic-24"
  /** Default Font used in STAR-CCM+. */
  public Font fontDefault = new Font("Dialog", Font.ITALIC, 24);

  /** Font used on Titles, when {@see #prettifyMe()} is used. */
  public Font fontTitle = new Font("SansSerif", Font.PLAIN, 18);

  /** Font used everywhere else, when {@see #prettifyMe()} is used. */
  public Font fontOther = new Font("SansSerif", Font.PLAIN, 14);

  /** Font used in Report Annotations, when {@see #prettifyMe()} is used. */
  //public Font fontRepAnnotations = new Font(fontAnnot, Font.ITALIC+Font.BOLD, 24);
  public Font fontRepAnnotations = new Font(fontAnnot, Font.PLAIN, 24);

  /** Font used in Text Annotations, when {@see #prettifyMe()} is used. */
  public Font fontSimpleAnnotations = fontRepAnnotations;

  //--
  //-- Public Dimensions
  //--

  /** Dimensions of Density. Definition in {@see #updateUnits}. */
  public Dimensions dimDensity = new Dimensions();

  /** Dimensions of Force. Definition in {@see #updateUnits}. */
  public Dimensions dimForce = new Dimensions();

  /** Dimensions of Length. Definition in {@see #updateUnits}. */
  public Dimensions dimLength = new Dimensions();

  /** Dimensions of Mass. Definition in {@see #updateUnits}. */
  public Dimensions dimMass = new Dimensions();

  /** Dimensions of Mass Flow. Definition in {@see #updateUnits}. */
  public Dimensions dimMassFlow = new Dimensions();

  /** Dimensions of Molecular Flow. Definition in {@see #updateUnits}. */
  public Dimensions dimMolFlow = new Dimensions();

  /** Dimensions of Pressure. Definition in {@see #updateUnits}. */
  public Dimensions dimPress = new Dimensions();

  /** Dimensions of Time. Definition in {@see #updateUnits}. */
  public Dimensions dimTime = new Dimensions();

  /** Dimensions of Velocity. Definition in {@see #updateUnits}. */
  public Dimensions dimVel = new Dimensions();

  /** Dimensions of Viscosity. Definition in {@see #updateUnits}. */
  public Dimensions dimVisc = new Dimensions();

  /** Dimensions of Volumetric Flow. Definition in {@see #updateUnits}. */
  public Dimensions dimVolFlow = new Dimensions();

  //--
  //-- Public doubles
  //--

  /** Directed Mesh O-Grid Factor, when using {@see #createMeshOperation_DM_Pipe}. Change with care.
   * Default = 1.0/1.10.
   */
  public double dmOGF = 1. / 1.10;

  /** Macro Utils variable for controlling the Tubes width when using a Streamline Displayer.
   * Default = 0.005;
   */
  public double postStreamlinesTubesWidth = 0.005;

  //--
  //-- Public double[]s
  //--

  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  public double[] camFocalPoint = {0., 0., 0.};

  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  public double[] camPosition = {0., 0., 0.};

  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  public double[] camViewUp = {0., 0., 0.};

  /** Useful predefined variable for storing Coordinates. */
  public double[] coord1 = {0., 0., 0.};

  /** Useful predefined variable for storing Coordinates. */
  public double[] coord2 = {0., 0., 0.};

  /** Useful predefined variable for storing Coordinates. */
  public double[] coord3 = {0., 0., 0.};

  /** Useful predefined variable for storing Coordinates. */
  public double[] coord4 = {0., 0., 0.};

  /** Good variable when using with {@see #evalLinearRegression}. */
  public double[] xx = new double[] {};

  /** Good variable when using with {@see #evalLinearRegression}. */
  public double[] yy = new double[] {};

  //--
  //-- Public File definitions
  //--

  /** A path containing CAD files. */
  public File cadPath = null;

  /** A path containing DBS files according {@see java.io.File} definition. */
  public File dbsPath = null;

  /** Current simulation File according {@see java.io.File} definition. */
  public File simFile = null;

  //--
  //-- Public int's definitions
  //--

  /** Special Macro Utils variable for a Picture Resolution in X (Width). Default = 1280. */
  public int defPicResX = 1280;

  /** Special Macro Utils variable for a Picture Resolution in X (Width). Default = 720. */
  public int defPicResY = 720;

  /** Directed Mesh Construction points subdivisions in O-Grid, when using
   *    {@see #createMeshOperation_DM_Pipe}. Change with care. Default = 0. */
  public int dmDiv = 0;

  /** Directed Mesh Smooths in Patch mesh, when using {@see #createMeshOperation_DM_Pipe}. Default = 0. */
  public int dmSmooths = 0;

  /**
   * Macro Utils variable for setting the default Tesselation method. Current options are:
   * {@see #TESSELATION_VERY_COARSE}, {@see #TESSELATION_COARSE}, {@see #TESSELATION_MEDIUM},
   * {@see #TESSELATION_FINE} or {@see #TESSELATION_VERY_FINE}.
   * <p>
   * Default is {@see #TESSELATION_MEDIUM}.
   */
  public int defTessOpt = TESSELATION_MEDIUM;

  /** Default reading X column in a CSV when performing GCI calculations with
   *    {@see #calcGCI}. Default = 0. */
  public int gciCsvReadColX = 0;

  /** Default reading Y column in a CSV when performing GCI calculations with
   *    {@see #calcGCI}. Default = 1. */
  public int gciCsvReadColY = 1;

  /** Macro Utils variable for controlling current frame number. Use with {@see #postFlyOverAndSavePics}. */
  public int postCurFrame = 0;

  /** Macro Utils variable for controlling the number of wanted frames. Use with {@see #postFlyOverAndSavePics}. */
  public int postFrames = 0;

  //--
  //-- Public Mesh Parameters
  //--

  /** Default Mesh Feature Angle. */
  public double mshSharpEdgeAngle = 30;

  /** This is more for debugging purposes. */
  public boolean skipMeshGeneration = false;

  /** The Thin Mesher should be Poly? */
  public boolean thinMeshIsPolyType = true;

  /** Minimum Feature Curve Relative Size. Default = 25%. */
  public double featCurveMeshMin = 25;

  /** Target Feature Curve Relative Size. Default = 100%.*/
  public double featCurveMeshTgt = 100;

  /** Mesh Base Reference Size in default units. See {@see #defUnitLength}.*/
  public double mshBaseSize = 3.0;

  /** Mesh Growth Factor for Tets/Polys. Default = 1.0 */
  public double mshGrowthFactor = 1.0;

  /** Volume Mesh Number of Optimization cycles. Default = 1. */
  public int mshOptCycles = 1;

  /** Number of Points in Gap when using Mesh Proximity. */
  public double mshProximityPointsInGap = 2.0;

  /** Search floor in default units for Mesh Proximity. See {@see #defUnitLength}. */
  public double mshProximitySearchFloor = 0.0;

  /** Volume Mesh Quality Threshold. Default = 0.4 */
  public double mshQualityThreshold = 0.4;

  /** Surface Mesh Number of Points per Circle. Default = 36. */
  public int mshSrfCurvNumPoints = 36;

  /** Surface Mesh Growth Rate. Default = 1.3. */
  public double mshSrfGrowthRate = 1.3;

  /** Surface Mesh Minimum Relative Size. Default = 25%. */
  public double mshSrfSizeMin = 25;

  /** Surface Mesh Target Relative Size. Default = 100%. */
  public double mshSrfSizeTgt = 100;

  /** Maximum Trimmer Relative Size. Default = 10000%. */
  public double mshTrimmerMaxCelSize = 10000;

  /** Trimmer Volume Growth Rate when meshing in Parts. Default is {@see #TRIMMER_GROWTH_RATE_FAST}. */
  public int mshTrimmerGrowthRate = TRIMMER_GROWTH_RATE_FAST;

  /** Surface Wrapper Feature Angle. Default = 30deg. */
  public double mshWrapperFeatureAngle = 30.;

  /** Surface Wrapper Scale factor. Default = 100%. */
  public double mshWrapperScaleFactor = 100.;

  /** Prism Layers Gap Fill Percentage. Default is 25%. */
  public double prismsGapFillPerc = 25;

  /** Prism Layers Chopping Percentage. Default is 50% in STAR-CCM+. In Macro Utils is 10%. */
  public double prismsLyrChoppPerc = 10.0;

  /** Prism Layers Minimum Thickness. Default is 10% in STAR-CCM+. In Macro Utils is 1%. */
  public double prismsMinThickn = 1.0;

  /** Prism Layers Near Core Aspect Ratio (NCLAR). Default is 0.0 in STAR-CCM+. In Macro Utils is 0.5. */
  public double prismsNearCoreAspRat = 0.5;

  /** Prism Layers Relative Size. If 0%, prism layers are disabled. Default = 30%. */
  public double prismsRelSizeHeight = 30.;

  /** Prism Stretch Ratio. Default = 1.5. */
  public double prismsStretching = 1.5;

  /** Number of Prism Layers. If 0, prism layers are disabled. Default = 2. */
  public int prismsLayers = 2;

  /** Number of layers when using the Thin Mesher. Default = 2.*/
  public int thinMeshLayers = 2;

  //--
  //-- Public Physics stuff
  //--

  /** Gravity vector defined as double. {0, -Y, 0} in m/s^2. Change if needed. */
  public double[] gravity = {0., -9.81, 0.};

  /** Initial Pressure value in default units. See {@see #defUnitPress}. */
  public double p0 = 0.0;

  /** Initial Turbulent Intensity for RANS Models. Default = 0.05. */
  public double ti0 = 0.05;

  /** Initial Turbulent Viscosity Ratio for RANS Models. Default = 10. */
  public double tvr0 = 10.0;

  /** Initial Velocity Scale for RANS Turbulence Models in default units. See {@see #defUnitVel}. */
  public double tvs0 = 0.5;

  /** Initial Velocity Array in default units. See {@see #defUnitVel}. */
  public double[] v0 = {0., 0., 0.};

  /** Minimum clipping Temperature in default units. */
  public double clipMinT = -50;

  /** Maximum clipping Temperature in default units. */
  public double clipMaxT = 3000;

  /** Density of Air in default units. See {@see #defUnitDen} */
  public double denAir = 1.18415;

  /** Density of Water in default units. See {@see #defUnitDen} */
  public double denWater = 997.561;

  /** Initial Temperature value for Fluids in default units. */
  public double fluidT0 = 22.;

  /** Initial Temperature value for Solids in default units. */
  public double solidsT0 = 60.;

  /** Reference Altitude array of values in default units. See {@see #defUnitLength}. */
  public double[] refAlt = new double[] {0, 0, 0};

  /** Reference Density value in default units. See {@see #defUnitDen}. */
  public double refDen = denAir;

  /** Reference Pressure value in default units. See {@see #defUnitPress}. */
  public double refP = 101325.;

  /** Reference Temperature value in default units. See {@see #defUnitTemp}. */
  public double refT = fluidT0;

  /** Radiation Emissivity. Default = 0.8. */
  public double radEmissivity = 0.8;

  /** Radiation Transmissivity. Default = 0. */
  public double radTransmissivity = 0.;

  /** Radiation Sharp Angle. Default = 150. */
  public double radSharpAngle = 150.;

  /** Radiation Patch Proportion of Faces. Default = 100%. */
  public double radPatchProp = 100.;

  /** Viscosity of Air in default units. See {@see #defUnitVisc} */
  public double viscAir = 1.85508E-5;

  /** Viscosity of Water in default units. See {@see #defUnitVisc} */
  public double viscWater = 8.8871E-4;

  //--
  //-- Public Remove Invalid Cells Settings
  //--

  /** Aggressive Removal when using Remove Invalid Cells? */
  public boolean aggressiveRemoval = false;

  /** Minimum Face Validity when using Remove Invalid Cells. */
  public double minFaceValidity = 0.51;

  /** Minimum Cell Quality metric when using Remove Invalid Cells. */
  public double minCellQuality = 1e-8;

  /** Minimum Volume Change metric when using Remove Invalid Cells. */
  public double minVolChange = 1e-10;

  /** Minimum Continuous Cell metric when using Remove Invalid Cells. */
  public int minContigCells = 1;

  /** Minimum Connected Face Areas metric when using Remove Invalid Cells. */
  public double minConFaceAreas = 0.;

  /** Minimum Cell Volume metric when using Remove Invalid Cells. */
  public double minCellVolume = 0.;

  //--
  //-- Public Solver Parameters.
  //--

  /** Default 1000 Maximum Iterations. */
  public final int maxIter0 = 1000;

  /** Default URF for Velocity. Default = 0.7. */
  public final double urfVel0 = 0.7;

  /** Default URF for Pressure. Default = 0.3. */
  public final double urfP0 = 0.3;

  /** Default URF for Fluid Energy. Default = 0.9. */
  public final double urfFluidEnrgy0 = 0.9;

  /** Default URF for Granular Temperature. Default = 0.6. */
  public final double urfGranTemp0 = 0.6;

  /** Default URF for Phase Couple Velocity. Default = 0.7. */
  public final double urfPhsCplVel0 = 0.7;

  /** Default URF for Solid Energy. Default = 0.99. */
  public final double urfSolidEnrgy0 = 0.99;

  /** Default URF for K-Epsilon or K-Omega models. Default = 0.8. */
  public final double urfKEps0 = 0.8;

  /** Default URF for the Reynolds Stress Model. Default = 0.6. */
  public final double urfRS0 = 0.6;

  /** Default URF for Species. Default = 0.9. */
  public final double urfSpecies0 = 0.9;

  /** Default URF for VOF. Default = 0.9. */
  public final double urfVOF0 = 0.9;

  /** Default URF for Volume Fraction. */
  public final double urfVolFrac0 = 0.5;

  /** AMG convergence tolerance. Default = 0.1. */
  public double amgConvTol = 0.1;

  /** AMG verbosity. Default = 0. (0 = None; 1 = Low; 2 = High; 3 - Diagnostics). */
  public int amgVerbosity = 0;

  /** Courant Number for the Coupled Solver. Default = 5. */
  public double CFL = 5;

  /**
   * CFL Ramp Beginning Iteration for the Coupled Solver. This value is also used when Expert Driver
   * is enabled. Default = 1.
   */
  public int cflRampBeg = 1;

  /**
   * CFL Ramp Ending Iteration for the Coupled Solver. This value is also used when Expert Driver
   * is enabled. Default = 25.
   */
  public int cflRampEnd = 25;

  /**
   * Initial CFL for Ramping the Coupled Solver. This value is also used when Expert Driver
   * is enabled. Default = 0.25.
   */
  /** Initial CFL for Ramping the Coupled Solver. */
  public double cflRampBegVal = 0.25;

  /** Enhanced Dissipation End Transition for the Expert Driver in Coupled Solver. Default = 100. */
  public int edEndTrans = 100;

  /** Enhanced Dissipation Start Transition for the Expert Driver in Coupled Solver. Default = 1. */
  public int edStartTrans = 1;

  /** Grid Sequencing Initialization Max Levels for the Coupled Solver. Default = 10. */
  public int gsiMaxLevels = 10;

  /** Grid Sequencing Initialization Iterations per level for the Coupled Solver. Default = 50. */
  public int gsiMaxIterations = 50;

  /** Grid Sequencing Initialization Convergence Tolerance for the Coupled Solver. Default = 0.05 */
  public double gsiConvTol = 0.05;

  /** Grid Sequencing Initialization Courant number for the Coupled Solver. Default = 5.0 */
  public double gsiCFL = 5.0;

  /** Maximum Iterations. Default {@see #maxIter0}. */
  public int maxIter = maxIter0;

  /** Ramp the Under Relaxation Factor for Courant? */
  public boolean rampCFL = false;

  /** Ramp the Under Relaxation Factor for Pressure? */
  public boolean rampURF_P = false;

  /** Ramp the Under Relaxation Factor for Velocity? */
  public boolean rampURF_Vel = false;

  /** Ramp the Under Relaxation Factor for Temperature? */
  public boolean rampURF_T = false;

  /** Second Order discretization on time when Unsteady? Default = false. */
  public boolean trn2ndOrder = false;

  /** Maximum Inner Iterations when using Unsteady. */
  public int trnInnerIter = 15;

  /** Maximum Physical time when using Unsteady. See {@see #defUnitTime}. */
  public double trnMaxTime = 10.;

  /** Physical time step when using Unsteady. See {@see #defUnitTime}. */
  public double trnTimestep = 0.001;

  /** URF for Energy. Use this value for changing both Fluid and Solid URFs simultaneously.
   *    Otherwise, use {@see #urfFluidEnrgy} and/or {@see #urfSolidEnrgy} individually.
   *    Default = 0 (unused). */
  public double urfEnergy = 0.;

  /** URF for Fluid Energy. Default: {@see #urfFluidEnrgy0}. */
  public double urfFluidEnrgy = urfFluidEnrgy0;

  /** URF for Granular Temperature. Default: {@see #urfGranTemp0}. */
  public double urfGranTemp = urfGranTemp0;

  /** URF for K-Epsilon. Default: {@see #urfKEps0}. */
  public double urfKEps = urfKEps0;

  /** URF for K-Epsilon Turbulent Viscosity. Default: 1.0. */
  public double urfKEpsTurbVisc = 1.0;

  /** URF for K-Omega. Default: {@see #urfKEps0}. */
  public double urfKOmega = urfKEps0;

  /** URF for K-Omega Turbulent Viscosity. Default: 1.0. */
  public double urfKOmegaTurbVisc = 1.0;

  /** URF for Phase Couple Velocity. Default: {@see #urfPhsCplVel0}. */
  public double urfPhsCplVel = urfPhsCplVel0;

  /** URF for Pressure. Default: {@see #urfP0}. */
  public double urfP = urfP0;

  /** URF for Reynolds Stress. Default: {@see #urfRS0}. */
  public double urfRS = urfRS0;

  /** URF for Reynolds Stress Turbulent Viscosity. Default: 1.0. */
  public double urfRSTurbVisc = 1.0;

  /** Initial URF for Ramping Fluid Energy. */
  public double urfRampFlBeg = 0.6;

  /** Initial Iteration for Ramping URF in Fluid Energy. */
  public int urfRampFlIterBeg = 100;

  /** Final Iteration for Ramping URF in Fluid Energy. */
  public int urfRampFlIterEnd = 1000;

  /** Initial URF for Ramping Pressure. */
  public double urfRampPressBeg = 0.1;

  /** Initial Iteration for Ramping URF in Pressure. */
  public int urfRampPressIterBeg = 100;

  /** Final Iteration for Ramping URF in Pressure. */
  public int urfRampPressIterEnd = 1000;

  /** Initial URF for Ramping Velocity. */
  public double urfRampVelBeg = 0.1;

  /** Initial Iteration for Ramping URF in Velocity. */
  public int urfRampVelIterBeg = 100;

  /** Final Iteration for Ramping URF in Velocity. */
  public int urfRampVelIterEnd = 1000;

  /** Initial URF for Ramping Solid Energy. */
  public double urfRampSldBeg = 0.7;

  /** Initial Iteration for Ramping URF in Solid Energy. */
  public int urfRampSldIterBeg = 100;

  /** Final Iteration for Ramping URF in Solid Energy. */
  public int urfRampSldIterEnd = 1000;

  /** URF for Solid Energy. Default: {@see #urfSolidEnrgy0}. */
  public double urfSolidEnrgy = urfSolidEnrgy0;

  /** URF for Species. Default: {@see #urfSpecies0}. */
  public double urfSpecies = urfSpecies0;

  /** URF for Velocity. Default: {@see #urfVel0}. */
  public double urfVel = urfVel0;

  /** URF for Volume Fraction. Default: {@see #urfKEps0}. */
  public double urfVolFrac = urfVolFrac0;

  /** URF for VOF. Default: {@see #urfVOF0}. */
  public double urfVOF = urfVOF0;

  /** VOF Sharpening Factor. */
  public double vofSharpFact = 0.0;

  /** VOF Angle Factor. */
  public double vofAngleFact = 0.05;

  /** VOF CFL Lower limit for the HRIC scheme. */
  public double vofCFL_l = 0.5;

  /** VOF CFL Upper limit for the HRIC scheme. */
  public double vofCFL_u = 1.0;

  //--
  //-- Public Strings
  //--

  /** Useful name for a Boundary Condition. */
  public String bcBottom = "bottom";

  /** Useful name for a Boundary Condition. */
  public String bcChannel = "channel";

  /** Useful name for a Boundary Condition. */
  public String bcCold = "cold";

  /** STAR-CCM+ likes to call Part Surfaces coming from 3D-CAD Parts as <b>Default</b>, right? */
  public String bcDefault = "Default";

  /** Useful name for a Boundary Condition. */
  public String bcFloor = "floor";

  /** STAR-CCM+ likes to call Part Surfaces as <b>Faces</b>, right? */
  public String bcFaces = "Faces";

  /** Useful name for a Boundary Condition. */
  public String bcGround = "ground";

  /** Useful name for a Boundary Condition. */
  public String bcHot = "hot";

  /** Useful name for a Boundary Condition. */
  public String bcInlet = "inlet";

  /** Useful name for a Boundary Condition. */
  public String bcOutlet = "outlet";

  /** Useful name for a Boundary Condition. */
  public String bcSym = "symmetry";

  /** Useful name for a Boundary Condition. */
  public String bcSym1 = "symmetry1";

  /** Useful name for a Boundary Condition. */
  public String bcSym2 = "symmetry2";

  /** Useful name for a Boundary Condition. */
  public String bcTop = "top";

  /** Useful name for a Boundary Condition. */
  public String bcWall = "wall";

  /** Useful name for a Boundary Condition. */
  public String bcWalls = "walls";

  /** Useful name for a Continua. */
  public String contNameAir = "Air";

  /** Useful name for a Continua. */
  public String contNameAirBoussinesq = "Air Boussinesq";

  /** Useful name for a Continua. */
  public String contNameAluminum = "Aluminum";

  /** Useful name for a Continua. */
  public String contNameSteel = "Steel";

  /** Useful name for a Continua. */
  public String contNamePoly = "Poly Mesh";

  /** Useful name for a Continua. */
  public String contNameTrimmer = "Trimmer Mesh";

  /** Useful name for a Continua. */
  public String contNameThinMesher = "Thin Mesher";

  /** Useful name for a Continua. */
  public String contNameWater = "Water";

  /** Useful name for a sub-folder. */
  public String dbsSubDir = "parts";

  /** Useful name for a Boundary Condition. */
  public String inlet = bcInlet;

  /** Useful name for a Boundary Condition or an Interface. */
  public String intrfName = "Interface";

  /** Useful name for a Boundary Condition. */
  public String outlet = bcOutlet;

  /** Simulation title. It is used when saving. See {@see #saveSim}. */
  public String simTitle = null;

  /** Full Simulation path with file and extension. E.g.: <i>/home/user/mySimFile.sim</i>. */
  public String simFullPath = null;

  /** Simulation path. It is used when saving. See {@see #saveSim}. */
  public String simPath = null;

  /** Just strings. Useful somewhere. */
  public String string = "", string1 = "", string2 = "", string3 = "", text = "";

  /** Useful name for a Boundary Condition. */
  public String wall = bcWall;

  /** Useful name for a Boundary Condition. */
  public String walls = bcWalls;

  //--
  //-- Public Units
  //--

  /** Default unit of Acceleration, when using Macro Utils. Default is {@see #unit_mps2}. */
  public Units defUnitAccel = null;

  /** Default unit of Angle, when using Macro Utils. Default is {@see #unit_deg}. */
  public Units defUnitAngle = null;

  /** Default unit of Area, when using Macro Utils. Default is {@see #unit_m2}. */
  public Units defUnitArea = null;

  /** Default unit of Density, when using Macro Utils. Default is {@see #unit_kgpm3}. */
  public Units defUnitDen = null;

  /** Default unit of Force, when using Macro Utils. Default is {@see #unit_N}. */
  public Units defUnitForce = null;

  /** Default unit of Heat Transfer Coefficient, when using Macro Utils. Default is {@see #unit_Wpm2K}. */
  public Units defUnitHTC = null;

  /** Default unit of Length, when using Macro Utils. Default is {@see #unit_mm}. */
  public Units defUnitLength = null;

  /** Default unit of Mass Flow Rate, when using Macro Utils. Default is {@see #unit_kgps}. */
  public Units defUnitMFR = null;

  /** Default unit of Pressure, when using Macro Utils. Default is {@see #unit_Pa}. */
  public Units defUnitPress = null;

  /** Default unit of Temperature, when using Macro Utils. Default is {@see #unit_C}. */
  public Units defUnitTemp = null;

  /** Default unit of Time, when using Macro Utils. Default is {@see #unit_s}. */
  public Units defUnitTime = null;

  /** Default unit of Velocity, when using Macro Utils. Default is {@see #unit_mps}. */
  public Units defUnitVel = null;

  /** Default unit of Viscosity, when using Macro Utils. Default is {@see #unit_Pa_s}. */
  public Units defUnitVisc = null;

  /** Default unit of Volume, when using Macro Utils. Default is {@see #unit_m3}. */
  public Units defUnitVolume = null;

  /** Celsius unit (Temperature). */
  public Units unit_C = null;

  /** CentiPoise unit (Viscosity). */
  public Units unit_cP = null;

  /** Centimeter of Water unit (Pressure). */
  public Units unit_cmH2O = null;

  /** Centimeter of Mercury unit (Pressure). */
  public Units unit_cmHg = null;

  /** Dimensionless unit. */
  public Units unit_Dimensionless = null;

  /** Fahrenheit unit (Temperature). */
  public Units unit_F = null;

  /** Degree unit (Angle). */
  public Units unit_deg = null;

  /** Dyne per Square Centimeter unit (Pressure). */
  public Units unit_dynepcm2 = null;

  /** Gallon unit (Volume). */
  public Units unit_gal = null;

  /** Gallons per Second unit (Volume/Time). */
  public Units unit_galps = null;

  /** Gallons per Minute unit (Volume/Time). */
  public Units unit_galpmin = null;

  /** Gram unit (Mass). */
  public Units unit_g = null;

  /** Gram per Minute unit (Mass/Time). */
  public Units unit_gpmin = null;

  /** Gram per Second unit (Mass/Time). */
  public Units unit_gps = null;

  /** Kelvin unit (Temperature). */
  public Units unit_K = null;

  /** kiloNewton (Force). */
  public Units unit_kN = null;

  /** kiloPascal unit (Pressure). */
  public Units unit_kPa = null;

  /** Hour unit (Time). */
  public Units unit_h = null;

  /** Kilogram unit (Mass). */
  public Units unit_kg = null;

  /** Kilogram per Hour unit (Mass/Time). */
  public Units unit_kgph = null;

  /** Gram per Cubic Centimeter unit (Density). */
  public Units unit_gpcm3 = null;

  /** Kilogram per Cubic Meter unit (Density). */
  public Units unit_kgpm3 = null;

  /** Kilogram per Second unit (Mass/Time). */
  public Units unit_kgps = null;

  /** Kilogram per Minute unit (Mass/Time). */
  public Units unit_kgpmin = null;

  /** Kilogram-mole (Quantity). */
  public Units unit_kmol = null;

  /** Kilogram-mole per second (Quantity/Time). */
  public Units unit_kmolps = null;

  /** Kilometer per Hour unit (Velocity). */
  public Units unit_kph = null;

  /** Knot unit (Velocity). */
  public Units unit_kt = null;

  /** Liter per Hour unit (Volume/Time). */
  public Units unit_lph = null;

  /** Liter per Minute unit (Volume/Time). */
  public Units unit_lpmin = null;

  /** Liter per Second unit (Volume/Time). */
  public Units unit_lps = null;

  /** Meter unit (Length). */
  public Units unit_m = null;

  /** Square Meter unit (Area). */
  public Units unit_m2 = null;

  /** Cubic Meter unit (Volume). */
  public Units unit_m3 = null;

  /** Cubic Meter per Hour unit (Volume/Time). */
  public Units unit_m3ph = null;

  /** Millibar unit (Pressure). */
  public Units unit_mbar = null;

  /** Minute unit (Time). */
  public Units unit_min = null;

  /** Millimeter unit (Length). */
  public Units unit_mm = null;

  /** Square Millimeter unit (Area). */
  public Units unit_mm2 = null;

  /** Cubic Millimeter unit (Volume). */
  public Units unit_mm3 = null;

  /** Millimeter of Water unit (Pressure). */
  public Units unit_mmH2O = null;

  /** Millimeter of Mercury unit (Pressure). */
  public Units unit_mmHg = null;

  /** Milliseconds (Time). */
  public Units unit_ms = null;

  /** Meter per Second unit (Velocity). */
  public Units unit_mps = null;

  /** Micrometer per Second unit (Velocity). */
  public Units unit_umps = null;

  /** Millimeter per Second unit (Velocity). */
  public Units unit_mmps = null;

  /** Meter per Square Second unit (Velocity / Time). */
  public Units unit_mps2 = null;

  /** Newton (Force). */
  public Units unit_N = null;

  /** Poise unit (Viscosity). */
  public Units unit_P = null;

  /** Pascal unit (Pressure). */
  public Units unit_Pa = null;

  /** Pascal x Second unit (Viscosity). */
  public Units unit_Pa_s = null;

  /** Radian per Second unit (Angular Velocity). */
  public Units unit_radps = null;

  /** Rotation per Minute unit (Angular Velocity). */
  public Units unit_rpm = null;

  /** Second unit (Time). */
  public Units unit_s = null;

  /** microPascal unit (Pressure). */
  public Units unit_uPa = null;

  /** Watt per Square Meter x Kelvin unit (Heat Transfer Coefficient). */
  public Units unit_Wpm2K = null;

}
