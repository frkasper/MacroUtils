package macroutils;

import java.awt.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.common.graph.*;
import star.coupledflow.*;
import star.dualmesher.*;
import star.energy.*;
import star.flow.*;
import star.keturb.*;
import star.material.*;
import star.meshing.*;
import star.metrics.*;
import star.motion.*;
import star.vof.*;
import star.prismmesher.*;
import star.radiation.common.*;
import star.radiation.s2s.*;
import star.resurfacer.*;
import star.saturb.*;
import star.segregatedenergy.*;
import star.segregatedflow.*;
import star.solidmesher.*;
import star.surfacewrapper.*;
import star.trimmer.*;
import star.turbulence.*;
import star.viewfactors.*;
import star.vis.*;
import star.walldistance.*;

/**
 * <b>Macro Utils</b> -- <i>"Your MACRO to write MACROS"</i>.<p>
 *
 * <b>Macro Utils</b> is a set of useful methods to assist the process of writing macros in
 * STAR-CCM+.<p>
 *
 * <b>How to use it?</b>
 * <ol>
 * <li> Store {@see #} in a subfolder called <u>macro</u>:<p>
 * - E.g.: <u>C:\work\macro\{@see #}java</u>
 * <p>
 * <li> In STAR-CCM+, go to <i>Menu -> Tools -> Options -> Environment</i>:<p>
 * - Under <i>User Macro classpath</i> put <u>C:\work</u><p>
 * <p>
 * - Alternatively, launch STAR-CCM+ in the command line as:<p>
 * > <u>starccm+ -classpath "C:\work"</u><p>
 *
 * <li> In another macro, just reference {@see #}, to benefit from its methods: E.g:<p>
 * <pre><code>
 *  package macro;
 *  public class MyMacro extends {@see #} {
 *      public void execute() {
 *          _initUtils();
 *          genVolumeMesh();
 *          removeInvalidCells();
 *          runCase();
 *          _finalize();
 *      }
 * } </code></pre></ol><p>
 * <b>Requirements:</b> <u>STAR-CCM+ v7.06 libs</u>. If there are methods not available in older libs,
 * <b>Macro Utils</b> will not work and there will be an error when playing in STAR-CCM+. The usage of
 * NetBeans is strongly suggested for writing your macros with <b>Macro Utils</b>.
 * @since STAR-CCM+ v7.02
 * @author Fabio Kasper
 * @version 2.3 Mar 10, 2013.
 */
public class MacroUtils extends StarMacro {

  /**
   * Initialize Macro Utils. This method is <b>mandatory</b>.
   */
  public void execute() { }

  /**
   * Initialize Macro Utils. This method is <b>mandatory</b>. It all starts here.
   */
  public void _initUtils() {
    /***************************************************************/
    /* Remember to initialize 'sim' before calling everything else */
    sim = getActiveSimulation();
    sayLoud("Macro Utils started.", true);
    /***************************************************************/
    printAction("Storing necessary variables");
    simFile = new File(sim.getSessionPath());
    simTitle = sim.getPresentationName();
    simPath = sim.getSessionDir();
    cadPath = new File(simPath, "CAD");
    dbsPath = new File(simPath, "DBS");
    saySimOverview();
    if (colorByRegion) {
        partColouring = colourByRegion;
    }
    addTags();
    prettifyLogo();
    updateMeshContinuaVector();
    updateOrCreateNewUnits(true);
    try {
        csys0 = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    } catch (Exception e) {}
    lab0 = (LabCoordinateSystem) csys0;
  }

  /**
   * Finalize Macro Utils. This method is <i>optional</i> but it has useful stuff.
   */
  public void _finalize() {
    prettifyMe();
    cleanUpTemporaryCameraViews();
    updateSolverSettings();
    if(!saveIntermediates || savedWithSuffix < 1){
        saveSim(simTitle);
    }
    sayLoud("Macro Utils finished.", true);
  }

  /****************************************************
   *
   * Useful Methods Area
   *
   ****************************************************/

  /**
   * Adds the Solution Time as an Annotation into the Scene.
   *
   * @param scene given Scene.
   * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
   */
  public void addAnnotation_SolutionTime(Scene scene, double[] pos) {
    PhysicalTimeAnnotation pta = ((PhysicalTimeAnnotation) sim.getAnnotationManager().getObject("Solution Time"));
    SolutionStateAnnotationProp solutionStateAnnotationProp_0 =
      (SolutionStateAnnotationProp) scene.getAnnotationPropManager().createPropForAnnotation(pta);
    solutionStateAnnotationProp_0.setPosition(new DoubleVector(new double[] {pos[0], pos[1], 0.0}));
  }

  /**
   * Add Simulation Tags. Useful for filtering parts or boundaries.
   */
  public void addTags() {
    printAction("Adding Simulation Tags");
    int nTags = sim.getTagManager().getChildren().size();
    if (nTags > 0) {
        say("Tags already exist: " + nTags + ". Not adding anymore.");
        return;
    }
    UserTag ut = null;
    ut = sim.get(TagManager.class).createNewUserTag("Black");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 6644833, 6776419, 6842212, 208168804, 2120705379, -966433951, -60727203, -60990375, -967223211, 2119455569, 206523980, 5000009, 4802630, 4671044, 16777215, 16777215, 6644833, 6776163, 1063741027, -563648925, -6908783, -5592665, -5263700, -5658459, -6908526, -8882572, -565425335, 1061767238, 4605251, 4539458, 16777215, 16777215, 6513247, 1365599073, -8421766, -5790303, -5790301, -7106420, -8224900, -8685451, -8553608, -8093058, -8553608, -10790314, 1363362367, 4276286, 16777215, 16777215, 308305756, -211722147, -6250597, -7237749, -8093058, -8487816, -8882573, -9343124, -9803675, -9935261, -9540503, -8948109, -213959108, 306003002, 16777215, 16777215, -2074322088, -8092801, -7961472, -8487816, -8685195, -9014160, -9408918, -9803675, -10001054, -10198434, -10461605, -10264226, -10527141, -2076559050, 16777215, 16777215, -816425645, -8488329, -8882573, -9146002, -9408917, -9737882, -9935261, -10132640, -10330019, -10527399, -10724778, -10790570, -10593705, -818596814, 16777215, 16777215, -112111539, -9079695, -9737625, -9935004, -10000797, -10132383, -10329762, -10527141, -10724777, -10921899, -11119278, -11185071, -10856107, -265146322, 16777215, 16777215, -263501240, -9737883, -10330019, -10395812, -10527398, -10724777, -10856364, -11119535, -11251122, -11448501, -11645879, -11711673, -11316914, -215011797, 16777215, 16777215, -817478333, -10264225, -10461605, -10724778, -10856364, -11053743, -11251122, -11382964, -11645879, -11777466, -11909052, -11777466, -11711671, -819188952, 16777215, 16777215, -2075966913, -11119536, -10593191, -11053743, -11185585, -11382707, -11580086, -11711673, -11909052, -11975101, -12106431, -11711673, -12501189, -2077611738, 16777215, 16777215, 356466235, -214156487, -10987950, -11053744, -11514294, -11646136, -11843259, -11975101, -12106431, -12172224, -11909053, -12040638, -215472348, 355084582, 16777215, 16777215, 3881528, 1413036085, -12435139, -11579829, -11251635, -11711929, -12040894, -12172224, -12106687, -11777980, -12172481, -13422034, 1411917604, 2828839, 16777215, 16777215, 3881271, 3618356, 1160983088, -567201747, -12369346, -11777209, -11448757, -11580343, -12040381, -12895690, -567925470, 1210591012, 2763046, 2960425, 16777215, 16777215, 3815734, 3552563, 3355184, 204484396, -2127746007, -919918041, -14079963, -14211549, -920181213, -2128140765, 203958052, 2763046, 2894632, 3092011, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}));
    ut = sim.get(TagManager.class).createNewUserTag("Blue");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 8771304, 8837096, 8771304, 210032103, 2122437350, -964767772, -59061278, -59324705, -965623332, 2121055449, 208058070, 6402258, 6073296, 5744333, 16777215, 16777215, 8639718, 8639718, 1065538534, -561983003, -3611665, -2297864, -1246979, -1378307, -2888971, -5384728, -563957293, 1063038160, 5744333, 5284554, 16777215, 16777215, 8310756, 1367265252, -5187094, -2888972, -3019013, -4528391, -6169096, -6563081, -5709832, -4331526, -4727570, -8668455, 1364239050, 4627911, 16777215, 16777215, 310037217, -210122015, -3216907, -4791047, -6234376, -6628105, -6956553, -7416074, -7875594, -8269323, -7481866, -5514771, -213475897, 305895364, 16777215, 16777215, -2072722210, -4924691, -5775368, -6759433, -7022089, -7350281, -7678474, -8072458, -8531979, -8925964, -9385484, -9319692, -8732962, -2077191744, 16777215, 16777215, -814825766, -6169869, -7218953, -7678218, -7875338, -8137994, -8466187, -8794379, -9188364, -9713420, -10304269, -10566414, -10239510, -819688515, 16777215, 16777215, -110643242, -6956554, -8137995, -8400395, -8597516, -8860172, -9188365, -9516557, -9976078, -10501134, -11091727, -11616784, -11091985, -266763079, 16777215, 16777215, -262098734, -8794125, -9778957, -9976077, -10173198, -10370318, -10764047, -11223567, -11814160, -12339472, -12995601, -13520658, -13060627, -217153610, 16777215, 16777215, -816207410, -9320724, -10173198, -10632718, -10895375, -11223567, -11617552, -12142352, -12667409, -13192722, -13783314, -14111251, -13718556, -821790029, 16777215, 16777215, -2075024694, -10569762, -10632462, -11420687, -11814160, -12208144, -12536337, -12995857, -13520658, -13980179, -14439699, -14242066, -14902575, -2080344655, 16777215, 16777215, 356752326, -213935675, -11290138, -11944975, -12667409, -13061393, -13520658, -13914642, -14374163, -14701844, -14636051, -14572321, -218073936, 352350639, 16777215, 16777215, 3577026, 1412469184, -13197875, -12537887, -12929297, -13717523, -14308627, -14702356, -14833172, -14570003, -14835492, -16218430, 1409314990, 28589, 16777215, 16777215, 2723262, 2263484, 1159497403, -568949831, -14181166, -13784092, -13979153, -14176017, -14637342, -15494194, -570396241, 1207988398, 28333, 28076, 16777215, 16777215, 1935291, 1475513, 1081271, 202014133, -2130412877, -922716238, -16746831, -16747600, -922717777, -2130677586, 201355181, 28076, 28076, 28076, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}));
    ut = sim.get(TagManager.class).createNewUserTag("Cross");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -2905944, -3771533, -4373950, -4373950, -3771533, -2971480, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -4103839, -3192760, -1482399, -890262, -890262, -1548192, -3258810, -4103839, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -4432291, -2534573, -1, -2206122, -2206122, -2206122, -2206122, -1, -2798002, -4432291, -2565928, 2087283049, 390810443, 1112033352, -725631042, -3892326, -3521725, -1, -1, -1, -3324603, -3324603, -1, -1, -1, -3982019, -3958376, -726420558, 1112033352, 1548043589, -2302757, -4823964, -2732209, -5561565, -1, -526345, -1513240, -2171170, -2368549, -2236963, -6614767, -4837842, -4823964, -3355445, 1548043589, 1816281666, -3158065, -5425867, -2995638, -4706770, -6286573, -3223858, -3026479, -2697514, -2302756, -6352623, -6156015, -5365985, -5425867, -3947581, 1816281666, 1849572926, -3355444, -5688525, -3589317, -5368812, -5500655, -3026479, -2697514, -2302756, -1907998, -5500655, -5500655, -5170919, -5688525, -4144960, 1849572926, 1631205946, -3158064, -5349793, -4117974, -4714223, -3026479, -2697514, -2302756, -1907998, -1644826, -1381654, -4714223, -4974825, -5349793, -4144958, 1631205946, 1194800951, -676681044, -4549995, -5234142, -3026479, -2697514, -2302756, -5238511, -5238511, -1381654, -1118482, -1118482, -5563878, -4681580, -677404766, 1194800951, 422851636, -2041491117, -3223852, -5681584, -6154730, -2302756, -4845295, -3141359, -3141359, -4845295, -1118482, -6155244, -5681584, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -6075827, -7073776, -3862255, -2879215, -2879215, -3862255, -7073776, -6075827, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -4352096, -6138019, -7857894, -7857894, -6138019, -4352096, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}));
    ut = sim.get(TagManager.class).createNewUserTag("Grey");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16053492, 16053492, 16053492, 217314291, 2129851122, -957222415, -51318544, -51384337, -957419794, 2129456364, 216722154, 15263976, 15198183, 15066597, 16777215, 16777215, 15987699, 15987699, 1072952307, -554503438, -526345, -263173, -65794, -65794, -328966, -789517, -555095575, 1072162791, 15066597, 15000804, 16777215, 16777215, 15856113, 1374810609, -657931, -328966, -197380, -263173, -394759, -460552, -394759, -263173, -592138, -1250068, 1373955300, 14869218, 16777215, 16777215, 317780208, -202313488, -328966, -328966, -394759, -394759, -460552, -460552, -526345, -526345, -526345, -592138, -203234590, 316727520, 16777215, 16777215, -2064716050, -657931, -394759, -460552, -460552, -460552, -526345, -526345, -592138, -592138, -657931, -592138, -1118482, -2065768738, 16777215, 16777215, -806490643, -526345, -460552, -526345, -526345, -592138, -592138, -592138, -657931, -657931, -723724, -723724, -921103, -807609124, 16777215, 16777215, -102044950, -460552, -526345, -592138, -592138, -592138, -592138, -657931, -657931, -723724, -789517, -789517, -789517, -254092582, 16777215, 16777215, -253171480, -657931, -723724, -723724, -723724, -789517, -789517, -855310, -921103, -921103, -986896, -1052689, -986896, -203892520, 16777215, 16777215, -806951194, -789517, -723724, -789517, -789517, -855310, -855310, -921103, -986896, -986896, -1052689, -1052689, -1184275, -808003882, 16777215, 16777215, -2065373980, -1184275, -789517, -855310, -921103, -921103, -921103, -986896, -1052689, -1052689, -1118482, -1052689, -1776412, -2066360875, 16777215, 16777215, 367190754, -203366176, -1052689, -855310, -986896, -986896, -1052689, -1052689, -1118482, -1118482, -1052689, -1381654, -204155692, 366269652, 16777215, 16777215, 14671839, 1423892190, -1776412, -1250068, -986896, -1052689, -1118482, -1118482, -1118482, -1118482, -1513240, -2302756, 1423168467, 13816530, 16777215, 16777215, 14540253, 14474460, 1172036571, -556082470, -1710619, -1184275, -986896, -986896, -1315861, -1842205, -556477228, 1221841875, 13816530, 13816530, 16777215, 16777215, 14408667, 14342874, 14211288, 215472087, -2116626730, -908667178, -2763307, -2829100, -908798764, -2116824109, 215143122, 13816530, 13816530, 13816530, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}));
    ut = sim.get(TagManager.class).createNewUserTag("Minus");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -2905944, -3771533, -4373950, -4373950, -3771533, -2971480, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -4103839, -3192760, -1482399, -890262, -890262, -1548192, -3258810, -4103839, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -4432291, -2534573, -1613985, -2206122, -2206122, -2206122, -2206122, -1745571, -2798002, -4432291, -2565928, 2087283049, 390810443, 1112033352, -725631042, -3892326, -3521725, -2469294, -3324603, -3324603, -3324603, -3324603, -3324603, -3324603, -2732466, -3982019, -3958376, -726420558, 1112033352, 1548043589, -2302757, -4823964, -2732209, -4245705, -4245705, -4574927, -5232857, -5891044, -6285802, -6548974, -6548974, -4837842, -4823964, -3355445, 1548043589, 1816281666, -3158065, -5425867, -2995638, -460552, -2171170, -3223858, -3026479, -2302756, -1513240, -1118482, -1118482, -5365985, -5425867, -3947581, 1816281666, 1849572926, -3355444, -5688525, -3589317, -3092272, -3355444, -3026479, -2302756, -1513240, -1118482, -1118482, -1118482, -5170919, -5688525, -4144960, 1849572926, 1631205946, -3158064, -5349793, -4117974, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -5631727, -4974825, -5349793, -4144958, 1631205946, 1194800951, -676681044, -4549995, -5234142, -3663848, -3927791, -3927791, -3927791, -3927791, -3927791, -3927791, -3927791, -5563878, -4681580, -677404766, 1194800951, 422851636, -2041491117, -3223852, -5681584, -4646117, -3075309, -3141359, -3141359, -3141359, -3141359, -3141359, -4778474, -5681584, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -6075827, -5959407, -3862255, -2879215, -2879215, -3862255, -5959407, -6075827, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -4352096, -6138019, -7857894, -7857894, -6138019, -4352096, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}));
    ut = sim.get(TagManager.class).createNewUserTag("Plus");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -6500190, -9779851, -12862389, -12862389, -9779851, -6500190, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -11485602, -12400045, -11149978, -10755220, -10755220, -11215771, -12465838, -11485602, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -11617702, -11873958, -11347357, -12794803, -1, -1, -12794803, -11413150, -12136873, -11617702, -2565928, 2087283049, 390810443, 1112033352, -725631042, -7289964, -12663731, -11939494, -12597424, -13321147, -1, -1, -13321147, -12597424, -12136873, -12992439, -7421550, -726420558, 1112033352, 1548043589, -2302757, -10701467, -12071337, -13781698, -13781698, -14242249, -1315861, -1907998, -15426267, -15557853, -15557853, -13979078, -10701467, -3355445, 1548043589, 1816281666, -3158065, -13652677, -12202923, -460552, -1973791, -2763307, -2631721, -2302756, -2039584, -1776412, -1513240, -14964180, -13652677, -3947581, 1816281666, 1849572926, -3355444, -13850313, -13319355, -2829100, -2894893, -2631721, -2302756, -2039584, -1776412, -1513240, -1250068, -15422428, -13850313, -4144960, 1849572926, 1631205946, -3158064, -11162531, -14501326, -15882722, -15882722, -15817953, -2039584, -1776412, -15817953, -15882722, -15882722, -15618527, -11162531, -4144958, 1631205946, 1194800951, -676681044, -8013940, -14701525, -15876834, -16271080, -16076261, -1776412, -1513240, -16076261, -16271080, -16271080, -15226845, -8079733, -677404766, 1194800951, 422851636, -2041491117, -3223852, -12803003, -15486431, -16334057, -16205799, -1513240, -1250068, -16205799, -16465387, -15749348, -12803003, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -13132738, -15884266, -16402157, -16530157, -16530157, -16402157, -15949803, -13132738, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -7817583, -11756206, -15694829, -15694829, -11756206, -7817583, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}));
    ut = sim.get(TagManager.class).createNewUserTag("Stop");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 0, 0, 0, 0, 1619823756, -1081308020, -7566196, -7566196, -7566196, -7566196, -1081308020, 1619823756, 0, 0, 0, 0, 0, 0, 546081932, -812872564, -6645094, -2829100, -921103, -1, -1, -921103, -2829100, -6645094, -812872564, 546081932, 0, 0, 0, 546081932, -276001652, -4737097, -1, -466206, -1267287, -1733492, -1733492, -1267287, -466206, -1, -4737097, -276001652, 546081932, 0, 0, -812872564, -4737097, -1, -1398616, -2398876, -2265490, -1333080, -1199438, -2265490, -2398876, -1398616, -1, -4737097, -812872564, 0, 1619823756, -6645094, -1, -1464409, -2595998, -2595998, -1330767, -1, -1, -1330767, -2595998, -2595998, -1464409, -1, -6645094, 1619823756, -1081308020, -2829100, -531999, -2793120, -2793120, -2793120, -2127737, -1, -1, -1729381, -2793120, -2793120, -2793120, -531999, -2829100, -1081308020, -7566196, -921103, -1661532, -2990242, -2990242, -2990242, -2791064, -1, -1, -2458244, -2990242, -2990242, -2990242, -1661532, -921103, -7566196, -7566196, -1, -2390396, -3187365, -3187365, -3187365, -3187365, -398358, -1, -3187365, -3187365, -3187365, -3187365, -2390396, -1, -7566196, -7566196, -1, -2521981, -3384231, -3384231, -3384231, -3384231, -1061685, -862506, -3384231, -3384231, -3384231, -3384231, -2521981, -1, -7566196, -7566196, -921103, -1989984, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -3581353, -1989984, -921103, -7566196, -1081308020, -2829100, -663329, -3712939, -3712939, -3712939, -3248790, -199180, -1, -3049611, -3712939, -3712939, -3712939, -663329, -2829100, -1081308020, 1619823756, -6645094, -1, -2187106, -3910062, -3910062, -3180942, -1, -1, -2916227, -3910062, -3910062, -2187106, -1, -6645094, 1619823756, 0, -812872564, -4737097, -1, -2318435, -4106928, -4106928, -3378063, -3113092, -4106928, -4106928, -2318435, -1, -4737097, -812872564, 0, 0, 546081932, -276001652, -4737097, -1, -794658, -2384228, -3178886, -3178886, -2384228, -794658, -1, -4737097, -276001652, 546081932, 0, 0, 0, 546081932, -812872564, -6645094, -2829100, -921103, -1, -1, -921103, -2829100, -6645094, -812872564, 546081932, 0, 0, 0, 0, 0, 0, 1619823756, -1081308020, -7566196, -7566196, -7566196, -7566196, -1081308020, 1619823756, 0, 0, 0, 0}));
    ut = sim.get(TagManager.class).createNewUserTag("Tick");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 5197647, 5263440, 5395026, 5460819, 357848148, 1045779797, 1431655765, 1666536789, 1666536789, 1431655765, 1045779797, 357848148, 5460819, 5395026, 5263440, 5197647, 5197647, 5263440, 122835538, 1028870995, 2020832115, -758593336, -1381654, -1644826, -1644826, -1381654, -758659129, 2020766322, 1028870995, 122835538, 5263440, 5197647, 5197647, 122703952, 1297174865, -1583374433, -1644826, -6500190, -9779851, -12862389, -12862389, -9779851, -6500190, -1776413, -1583506019, 1297174865, 122703952, 5197647, 5131854, 1062096462, -1566728803, -2105377, -11485602, -12400045, -11149978, -10755220, -10755220, -11215771, -12465838, -11485602, -2302757, -1566991975, 1062096462, 5131854, 390810443, 2087480428, -2039585, -11617702, -11873958, -11347357, -11807908, -11807908, -11807908, -11807908, -11413150, -12136873, -11617702, -2565928, 2087283049, 390810443, 1112033352, -725631042, -7289964, -12663731, -11939494, -12597424, -12597424, -12597424, -12597424, -13321147, -1, -5903954, -12992439, -7421550, -726420558, 1112033352, 1548043589, -2302757, -10701467, -12071337, -13781698, -13189561, -13584063, -14307786, -14900179, -1907998, -1842205, -1579033, -13979078, -10701467, -3355445, 1548043589, 1816281666, -3158065, -13652677, -12202923, -460552, -14964948, -15687391, -15688159, -2171170, -1907998, -1644826, -15688415, -14964180, -13652677, -3947581, 1816281666, 1849572926, -3355444, -13850313, -5447758, -2763307, -1315861, -15817953, -2171170, -1907998, -1644826, -15818209, -15947747, -15422428, -13850313, -4144960, 1849572926, 1631205946, -3158064, -11162531, -14501326, -2763307, -2434342, -2171170, -1907998, -1644826, -15882722, -16076773, -16076773, -15618527, -11162531, -4144958, 1631205946, 1194800951, -676681044, -8013940, -14701525, -15684575, -2171170, -1907998, -1644826, -15947235, -16271080, -16271080, -16271080, -15226845, -8079733, -677404766, 1194800951, 422851636, -2041491117, -3223852, -12803003, -15486431, -15945955, -1644826, -16077029, -16465387, -16465387, -16465387, -15749348, -12803003, -3552816, -2041556911, 422851636, 3158064, 1211051823, -1400996222, -3026469, -13132738, -15884266, -16402157, -16530157, -16530157, -16402157, -15949803, -13132738, -3158054, -1401127807, 1211051823, 3158064, 3026478, 153889836, 1546004006, -1350993536, -2697500, -7817583, -11756206, -15694829, -15694829, -11756206, -7817583, -2697501, -1350993537, 1546004006, 153889836, 3026478, 3026478, 2894892, 152968734, 1326913303, -1841677763, -593058126, -2434325, -2434325, -2434325, -2434325, -593058126, -1841677763, 1326913303, 152968734, 2894892, 3026478, 3026478, 2894892, 1973790, 1250067, 487394573, 1410009867, 1946880779, -2029319413, -2029319413, 1946880779, 1410009867, 487394573, 1250067, 1973790, 2894892, 3026478}));
    ut = sim.get(TagManager.class).createNewUserTag("Yellow");
    ut.getCustomizableIcon().setStarImagePixelData(new IntVector(new int[] {16, 16, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16637044, 16637044, 16637044, 217963123, 2130565233, -956508818, -50539925, -50540953, -956511901, 2130429278, 217825369, 16497492, 16430928, 16429644, 16777215, 16777215, 16636274, 16636274, 1073600881, -553855121, -137533, -69158, -1815, -1817, -70192, -140634, -553927595, 1073395537, 16429644, 16428612, 16777215, 16777215, 16569966, 1375524462, -139613, -70449, -3892, -5965, -8048, -8822, -7521, -5707, -138831, -277646, 1375383108, 16427578, 16777215, 16777215, 318558570, -201535126, -70967, -6484, -7795, -8568, -9085, -9603, -10377, -10894, -10112, -74590, -201676230, 318350638, 16777215, 16777215, -2063807387, -139346, -7789, -8826, -9086, -9602, -10119, -10636, -11410, -12184, -12959, -12447, -211344, -2064015582, 16777215, 16777215, -805583008, -74354, -9346, -9864, -10379, -10639, -11155, -11927, -12444, -13220, -13998, -14260, -80305, -805725674, 16777215, 16777215, -100941478, -9599, -10895, -11410, -11669, -12184, -12700, -13217, -13993, -14770, -15548, -16069, -15552, -252144374, 16777215, 16777215, -251938220, -12192, -13740, -14510, -14769, -15283, -15802, -16578, -17356, -18133, -19168, -19690, -18151, -201814016, 16777215, 16777215, -805653426, -79270, -14769, -15543, -16060, -16578, -17097, -17873, -18650, -19428, -20205, -20469, -85746, -805795072, 16777215, 16777215, -2063946171, -213429, -15034, -16837, -17356, -17874, -18392, -19169, -19945, -20721, -21497, -20217, -285695, -2064152832, 16777215, 16777215, 368683063, -201742798, -82116, -17360, -18395, -19170, -19945, -20464, -21240, -21501, -21247, -152573, -201882112, 368542720, 16777215, 16777215, 16359977, 1425645346, -350172, -215513, -18403, -19950, -21238, -21757, -21504, -20992, -218880, -420608, 1425507072, 16220672, 16777215, 16777215, 16358427, 16292116, 1173919245, -554134777, -284657, -85746, -19446, -19706, -152320, -352512, -554204160, 1224180480, 16220416, 16220160, 16777215, 16777215, 16291599, 16290567, 16289792, 217615872, -2114417920, -906524416, -555008, -555520, -906525696, -2114485504, 217547264, 16220160, 16220160, 16220160, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215}));
    say("Tags added: " + (sim.getTagManager().getChildren().size()-nTags));
    sayOK();
  }

  /**
   * Creates a custom Unit.
   *
   * @param name given Unit name.
   * @param desc given Unit description.
   * @param conversion given Unit conversion factor.
   * @param dimensionList array of Dimension (<i>use a custom Macro to help it here</i>).
   * @return The created Unit.
   */
  public Units addUnit(String name, String desc, double conversion, int[] dimensionList) {
    return addUnit(name, desc, conversion, dimensionList, true);
  }

  private Units addUnit(String name, String desc, double conversion, int[] dimensionList,
                                                                    boolean verboseOption) {
    Units unit = getUnit(name, false);
    if (unit == null) {
        say(verboseOption, unitFormat, "Creating Unit", name, desc);
        UserUnits newUnit = sim.getUnitsManager().createUnits("Units");
        newUnit.setPresentationName(name);
        newUnit.setDescription(desc);
        newUnit.setConversion(conversion);
        newUnit.setDimensionsVector(new IntVector(dimensionList));
        return newUnit;
    }
    say(verboseOption, unitFormat, "Unit already exists", name, unit.getDescription());
    return unit;
  }

  /**
   * Assigns all Parts to a Region.
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
   * @return Collection of created Regions.
   */
  public Collection<Region> assignAllPartsToRegions() {
    printAction("Assigning All Parts to Regions");
    return assignPartsToRegions(getAllLeafParts(false), false, false, false, false);
  }

  /**
   * Assigns all Parts to Regions.
   *
   * @param singleBoundary One Boundary per Part Surface?
   * @return Collection of created Regions.
   */
  public Collection<Region> assignAllPartsToRegions(boolean singleBoundary) {
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
    return (Region) assignPartsToRegions(new NeoObjectVector(new Object[] {gp}), false, true, false, true).iterator().next();
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
    return (Region) assignPartsToRegions(new NeoObjectVector(new Object[] {gp}), false, true, isolateDefBdry, true).iterator().next();
  }

  /**
   * Assigns the given Parts to a Region.
   *
   * @param colGP given Geometry Parts.
   * @return Created Region.
   */
  public Region assignPartsToRegion(Collection<GeometryPart> colGP) {
    return assignPartsToRegions(colGP, false, true, false, true).iterator().next();
  }

  /**
   * Assigns the given Parts to Regions.
   *
   * @param colGP given Geometry Parts.
   * @param singleBoundary One Boundary per Part Surface?
   * @return Collection of created Regions.
   */
  public Collection<Region> assignPartsToRegions(Collection<GeometryPart> colGP, boolean singleBoundary) {
    return assignPartsToRegions(colGP, singleBoundary, false, false, true);
  }

  private Collection<Region> assignPartsToRegions(Collection<GeometryPart> colGP,
            boolean singleBoundary, boolean singleRegion, boolean isolateDefBdry, boolean verboseOption){
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
    say("Number of Parts: " + colGP.size());
    if (singleRegion && colGP.size() == 1) {
        regionMode = "OneRegionPerPart";
    }
    for (GeometryPart gp : colGP) {
        say("  " + gp.getPathInHierarchy());
    }
    Vector<Region> vecCurrentRegions = (Vector) getAllRegions(false);
    RegionManager regMngr = sim.getRegionManager();
    regMngr.newRegionsFromParts(colGP, regionMode, bdryMode, "OneFeatureCurve", true);
    Vector<Region> vecCreatedRegions = (Vector) getAllRegions(false);
    vecCreatedRegions.removeAll(vecCurrentRegions);
    if (isolateDefBdry) {
        say("Isolating Default Boundaries...");
        Boundary defBdry = null;
        for (Region reg : vecCreatedRegions) {
            say("  Region: " + reg.getPresentationName());
            defBdry = reg.getBoundaryManager().getDefaultBoundary();
            if (defBdry == null) {
                for (Boundary bdry : getAllBoundariesFromRegion(reg, false, true)) {
                    if (bdry.getIndex() == 1) {
                        defBdry = bdry;
                        break;
                    }
                }
            }
            if (defBdry == null) {
                say("     ERROR! Default boundary could not be isolated.");
            } else {
                String name = defBdry.getPresentationName();
                Collection<PartSurface> colPS = defBdry.getPartSurfaceGroup().getObjects();
                defBdry.getPartSurfaceGroup().removeObjects(colPS);
                defBdry.setPresentationName("default boundary");
                Boundary newBdry = reg.getBoundaryManager().createEmptyBoundary(name);
                newBdry.getPartSurfaceGroup().addObjects(colPS);
                say("     Default boundary isolated.");
            }
        }
    }
    say("Regions created: " + vecCreatedRegions.size(), verboseOption);
    return vecCreatedRegions;
  }

  private PhysicsContinuum changePhysics_SegrAirIdealGas(PhysicsContinuum phC) {
    ConstantDensityModel cdm = phC.getModelManager().getModel(ConstantDensityModel.class);
    phC.disableModel(cdm);
    phC.enable(IdealGasModel.class);
    phC.enable(SegregatedFluidTemperatureModel.class);
    return phC;
  }
  private PhysicsContinuum changePhysics_AirWaterEMP(PhysicsContinuum phC) {
    updateOrCreateNewUnits(false);
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

  private PhysicsContinuum changePhysics_SegrFlTemp(PhysicsContinuum phC) {
    phC.enable(SegregatedFluidTemperatureModel.class);
    updateOrCreateNewUnits(false);
    setInitialCondition_T(phC, refT, false);
    return phC;
  }

  private PhysicsContinuum changePhysics_SegrFlBoussinesq(PhysicsContinuum phC, double thermalExpansion) {
    phC.enable(GravityModel.class);
    phC.enable(BoussinesqModel.class);
    updateOrCreateNewUnits(false);
//    SingleComponentGasModel sgm = phC.getModelManager().getModel(SingleComponentGasModel.class);
//    Gas gas = ((Gas) sgm.getMaterial());
//    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ThermalExpansionProperty.class).getMethod());
    Model model = phC.getModelManager().getModel(SingleComponentGasModel.class);
    ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(model, ThermalExpansionProperty.class);
    cmpm.getQuantity().setValue(thermalExpansion);
    return phC;
  }

  private PhysicsContinuum changePhysics_TurbKEps2Lyr(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(KEpsilonTurbulence.class);
    phC.enable(RkeTwoLayerTurbModel.class);
    phC.enable(KeTwoLayerAllYplusWallTreatment.class);
    updateOrCreateNewUnits(false);
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, false);
    return phC;
  }

  private PhysicsContinuum changePhysics_TurbSA_AllWall(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(SpalartAllmarasTurbulence.class);
    phC.enable(SaTurbModel.class);
    phC.enable(SaAllYplusWallTreatment.class);
    updateOrCreateNewUnits(false);
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, false);
    return phC;
  }

  /* WORK LATER ON THIS */
  private void checkFreeEdgesAndNonManifolds(SurfaceMeshWidgetDiagnosticsController diagCtrl,
                                                SurfaceMeshWidgetRepairController repairCtrl){
    diagCtrl.setCheckFreeEdges(true);
    diagCtrl.setFreeEdgesActive(true);
    diagCtrl.setCheckNonmanifoldEdges(true);
    diagCtrl.setNonmanifoldEdgesActive(true);
    diagCtrl.setCheckNonmanifoldVertices(true);
    diagCtrl.setNonmanifoldVerticesActive(true);

    diagCtrl.runDiagnostics();
    int freeEdg = diagCtrl.getNumFreeEdges();
    int nonManEdg = diagCtrl.getNumNonmanifoldEdges();
    int nonManVert = diagCtrl.getNumNonmanifoldVertices();
    int maxHoles = 100;

    if (freeEdg > 0) {
        say("****************************************************");
        say("**                   WARNING!!!                   **");
        say("**                                                **");
        say("**               FREE EDGES FOUND!                **");
        say("****************************************************");
        if (freeEdg < 100 && autoCloseFreeEdges) {
            say("Attempting to auto-fill holes.");
            diagCtrl.selectFreeEdges();
            repairCtrl.holeFillSelectedEdges();
            say("Rerunning Diagnostics...");
            diagCtrl.runDiagnostics();
            freeEdg = diagCtrl.getNumFreeEdges();
            nonManEdg = diagCtrl.getNumNonmanifoldEdges();
            nonManVert = diagCtrl.getNumNonmanifoldVertices();
            say("Number of Free Edges: " + retString(freeEdg));
            say("Number of Non-Manifold Edges: " + retString(nonManEdg));
            say("Number of Non-Manifold Vertices: " + retString(nonManVert));
        } else {
            say("WARNING! Too many holes found in the model. More than " + maxHoles);
            say("Giving up!");
        }
    }
  }

  public void checkFreeEdgesAndNonManifoldsOnLeafMeshPart(LeafMeshPart lmp) {
    printAction("Checking For Free Edges and Non-Manifold Edges & Vertices");
    say("Checking Leaf Mesh Part: " + lmp.getPresentationName());

    Scene scene_1 = sim.getSceneManager().createScene("Repair Surface");
    scene_1.initializeAndWait();

    // Calling in Geometry Representation. Just to be sure
    PartRepresentation partRep = queryGeometryRepresentation();
    PartSurfaceMeshWidget prtSrfMshWidget = partRep.startSurfaceMeshWidget(scene_1);
    prtSrfMshWidget.setActiveParts(new NeoObjectVector(new Object[] {lmp}));

    prtSrfMshWidget.startSurfaceMeshDiagnostics();
    prtSrfMshWidget.startSurfaceMeshRepair();

    SurfaceMeshWidgetDiagnosticsController diagCtrl =
      prtSrfMshWidget.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    SurfaceMeshWidgetRepairController repairCtrl =
      prtSrfMshWidget.getControllers().getController(SurfaceMeshWidgetRepairController.class);

    Collection<PartSurface> colPS = lmp.getPartSurfaceManager().getPartSurfaces();
    lmp.getPartSurfacesSharingPatches(prtSrfMshWidget, ((Vector) colPS), new NeoObjectVector(new Object[] {}));

    checkFreeEdgesAndNonManifolds(diagCtrl, repairCtrl);

    prtSrfMshWidget.stop();
    sim.getSceneManager().deleteScenes(new NeoObjectVector(new Object[] {scene_1}));
    sayOK();
  }

  public void checkFreeEdgesAndNonManifoldsOnParts(){
    printAction("Checking for Free Edges and Non-Manifolds Edges/Vertices on All Parts");
    for(LeafMeshPart lmp : getAllLeafMeshParts()){
        checkFreeEdgesAndNonManifoldsOnLeafMeshPart(lmp);
    }
  }

  public void checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions() {
    printAction("Checking For Free Edges and Non-Manifold Edges & Vertices");
    say("Checking Surface Mesh in All regions");

    Vector<Region> vecReg = new Vector<Region>();
    for(Region region : getAllRegions()){
        if(isRemesh(region.getMeshContinuum())){
            vecReg.add(region);
        }
    }
    if(vecReg.size() == 0){
        say("No Remeshing Regions to be checked.");
        return;
    }

    Scene scene_1 = sim.getSceneManager().createScene("Repair Surface");
    scene_1.initializeAndWait();

    SurfaceMeshWidget srfMshWidget = queryRemeshedSurface().startSurfaceMeshWidget(scene_1);
    srfMshWidget.setActiveRegions((Vector) getAllRegions());

    srfMshWidget.startSurfaceMeshDiagnostics();
    srfMshWidget.startSurfaceMeshRepair();
    srfMshWidget.startMergeImprintController();

    SurfaceMeshWidgetDiagnosticsController diagCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    SurfaceMeshWidgetRepairController repairCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetRepairController.class);
    SurfaceMeshWidgetDisplayController srfMshWidgetDispCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetDisplayController.class);
    SurfaceMeshWidgetDisplayer srfMshWidgetDisplayer =
      ((SurfaceMeshWidgetDisplayer) scene_1.getDisplayerManager().getDisplayer("Widget displayer 1"));

    srfMshWidgetDispCtrl.showAllFaces();
    srfMshWidgetDisplayer.initialize();

    checkFreeEdgesAndNonManifolds(diagCtrl, repairCtrl);

    srfMshWidget.stop();
    sim.getSceneManager().deleteScenes(new NeoObjectVector(new Object[] {scene_1}));
    sayOK();
  }

  /**
   * This method cleans everything but the Volume Mesh and the FV Regions. It is useful for saving
   * hard drive space.
   */
  public void cleanUpSimulationFile(){
    /**************************************************
     * STILL WORKING ON THIS
     **************************************************/
    printAction("Cleaning Up Simulation file to save space");
    say("Disabling Mesh Continua in All Regions...");
    for (Region region : getAllRegions(false)) {
        MeshContinuum continua = region.getMeshContinuum();
        if (continua == null) { continue; }
        continua.erase(region);
        //sim.getContinuumManager().eraseFromContinuum(new NeoObjectVector(new Object[] {region}), continua);
    }
    clearMeshes();
    clearParts();       // WATCH OUT ON THIS. NEEDS IMPROVEMENT.
  }

  /**
   * When using {@see #getCameraViews}, several temporary Camera Views are created. This method gets
   * rid of all of them. <p>
   * This method is also called automatically in {@see #_finalize}.
   */
  public void cleanUpTemporaryCameraViews() {
    Collection<VisView> colVV = getCameraViews(tempCamName + ".*", false);
    int size = colVV.size();
    if (colVV.size() > 0) {
        printAction("Removing Temporary Cameras...");
        sim.getViewManager().removeObjects(colVV);
        say("Cameras Removed: " + size);
        sayOK();
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

  private void clearInterfaces(boolean verboseOption) {
    printAction("Removing all Interfaces", verboseOption);
    Collection<Interface> colIntrf = sim.getInterfaceManager().getObjects();
    if(colIntrf.isEmpty()) {
        say("No Interfaces found.", verboseOption);
        return;
    }
    say("Removing " + colIntrf.size() + " Interface(s)");
    sim.getInterfaceManager().deleteInterfaces((Vector) colIntrf);
    sayOK(verboseOption);
  }

  /**
   * Removes all Mesh Representations of the model.
   */
  public void clearMeshes() {
    printAction("Removing all Mesh Representations");
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
   * Removes all Regions of the model.
   */
  public void clearRegions() {
    printAction("Removing all Regions");
    Collection<Region> colReg = getAllRegions();
    if(colReg.isEmpty()) {
        say("No Regions found.");
        return;
    }
    say("Removing " + colReg.size() + " Region(s)...");
    sim.getRegionManager().removeRegions((Vector) colReg);
    sayOK();
  }

  /**
   * Removes all Geometry Parts of the model.
   */
  public void clearParts() {
    printAction("Removing all Parts");
    Collection<GeometryPart> gParts = getAllGeometryParts();
    if (!gParts.isEmpty()) {
        try {
            say("Removing Geometry Parts...");
            sim.get(SimulationPartManager.class).removeParts(gParts);
            sayOK();
        } catch (Exception e0) {
            say("Error removing Leaf Parts. Moving on.");
        }
    }
    Collection<GeometryPart> leafParts = getAllLeafParts();
    if (!leafParts.isEmpty()) {
        try {
            say("Removing Leaf Parts...");
            sim.get(SimulationPartManager.class).removeParts(leafParts);
            sayOK();
        } catch (Exception e1) {
            say("Error removing Leaf Parts. Moving on.");
        }
    }
    Vector<CompositePart> compParts = ((Vector) getAllCompositeParts());
    if (!compParts.isEmpty()) {
        try {
            say("Removing Composite Parts...");
            sim.get(SimulationPartManager.class).removeParts(compParts);
            sayOK();
        } catch (Exception e2) {
            say("Error removing Composite Parts. Moving on.");
        }
    }
  }

  /**
   * Removes all Scenes of the model.
   */
  public void clearScenes(){
    printAction("Removing all Scenes");
    try{
        Collection<Scene> colSc = sim.getSceneManager().getScenes();
        say("Removing " + colSc.size() + " Scene(s)");
        sim.getSceneManager().removeObjects(colSc);
        sayOK();
    } catch (Exception e){
        say("No Scenes found.");
    }
  }

  /**
   * Clears the Solution and all Fields are reset.
   */
  public void clearSolution() {
    clearSolution(true, true, true, true);
  }

  /**
   * Clears the Solution with an option of clearing only the Solution History.
   *
   * @param clearHistoryOnly clear only the History? Otherwise, everything is cleared.
   */
  public void clearSolution(boolean clearHistoryOnly) {
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
   * Combine all boundaries from a Region.
   *
   * @param region given Region.
   * @return combined boundary.
   */
  public Boundary combineBoundaries(Region region){
    printAction("Combining All Boundaries from Region");
    sayRegion(region);
    return combineBoundaries((Vector) getAllBoundariesFromRegion(region), true);
  }

  /**
   * Combine several boundaries.
   *
   * @param vecBdry given Vector of Boundaries.
   * @return combined boundary.
   */
  public Boundary combineBoundaries(Vector<Boundary> vecBdry){
    printAction("Combining Boundaries");
    return combineBoundaries(vecBdry, true);
  }

  private Boundary combineBoundaries(Vector<Boundary> vecBdry, boolean verboseOption){
    say("Boundaries provided: " + vecBdry.size(), verboseOption);
    for (Boundary bdry : vecBdry) {
        sayBdry(bdry, true, false);
    }
    if (vecBdry.size() < 2) {
        say("Not enough boundaries to combine. Skipping...", verboseOption);
    } else {
        sim.getMeshManager().combineBoundaries(vecBdry);
        sayOK(verboseOption);
    }
    return vecBdry.firstElement();
  }

  /**
   * Combines Geometry Parts based on REGEX search criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @param combinePartSurfaces Combine all the Part Surfaces?
   * @return The combined Geometry Part.
   */
  public GeometryPart combineGeometryParts(String regexPatt, boolean combinePartSurfaces){
    return combineGeometryParts(regexPatt, combinePartSurfaces, true);
  }

  private GeometryPart combineGeometryParts(String regexPatt, boolean combinePS, boolean verboseOption) {
    printAction(String.format("Combining Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<GeometryPart> vecGP = (Vector) getAllLeafParts(regexPatt, false);
    for (int i = 0; i < vecGP.size(); i++) {
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecGP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts((MeshPart) vecGP.firstElement(), vecMP);
    cadPrt = (CadPart) vecGP.firstElement();
    say("Combined into: " + cadPrt.getPathInHierarchy(), verboseOption);
    if (combinePS) {
        say("Combining Part Surfaces..", verboseOption);
        Collection<PartSurface> colPS = cadPrt.getPartSurfaces();
        int n = colPS.size();
        cadPrt.combinePartSurfaces(colPS);
        String name = cadPrt.getPartSurfaces().iterator().next().getPresentationName();
        say("Combined " + n + " Part Surfaces into: " + name);
    }
    sayOK(verboseOption);
    return cadPrt;
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
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<LeafMeshPart> vecLMP = (Vector) getAllLeafMeshParts(regexPatt, false);
    for (int i = 0; i < vecLMP.size(); i++) {
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecLMP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts(vecLMP.firstElement(), vecMP);
    LeafMeshPart lmp = vecLMP.firstElement();
    say("Combined into: " + lmp.getPathInHierarchy(), verboseOption);
    sayOK(verboseOption);
    return lmp;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(Collection<PartSurface> colPS, String renameTo){
    PartSurface ps = combinePartSurfaces(colPS, true);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(Collection<PartSurface> colPS){
    return combinePartSurfaces(colPS, true);
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
    Collection<PartSurface> colPS = leafMshPart.getPartSurfaceManager().getPartSurfaces();
    return combinePartSurfaces(colPS, true);
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
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    for (PartSurface ps : leafMshPart.getPartSurfaceManager().getPartSurfaces()) {
        String name = ps.getPresentationName();
        if (name.matches(regexPatt)) {
            say("  Found: " + name);
            vecPS.add(ps);
        }
    }
    return combinePartSurfaces(vecPS, true);
  }

  private PartSurface combinePartSurfaces(Collection<PartSurface> colPS, boolean verboseOption){
    printAction("Combining Part Surfaces", verboseOption);
    String myPS = "___myPartSurface";
    String name = colPS.iterator().next().getPresentationName();
    say("Part Surfaces available: " + colPS.size(), verboseOption);
    if (colPS.size() == 1) {
        say("Nothing to combine.", verboseOption);
        return colPS.iterator().next();
    }
    for (PartSurface ps : colPS) {
        ps.setPresentationName(myPS);
    }
    //-- Combine faces
    GeometryPart gp = colPS.iterator().next().getPart();
    if (isCadPart(gp)) {
        ((CadPart) gp).combinePartSurfaces(colPS);
    } else if (isBlockPart(gp)) {
        ((SimpleBlockPart) gp).combinePartSurfaces(colPS);
    } else if (isSimpleCylinderPart(gp)) {
        ((SimpleCylinderPart) gp).combinePartSurfaces(colPS);
    } else if (isLeafMeshPart(gp)) {
        ((LeafMeshPart) gp).combinePartSurfaces(colPS);
    }
    //-- Reloop to make sure it finds the correct combined Part Surface
    colPS = gp.getPartSurfaces();
    PartSurface foundPS = null;
    for (PartSurface ps : colPS) {
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

  public void convertAllInterfacesToIndirect(){
    printAction("Converting all Interfaces to Indirect");
    for(Object intrfObj : getAllInterfacesAsObjects()){
        convertInterface_Direct2Indirect((DirectBoundaryInterface) intrfObj);
    }
    sayOK();
  }

  public void convertAllFluidSolidInterfacesToIndirect(){
    printAction("Converting all Fluid-Solid Direct Interfaces to Indirect");
    for(Object intrfObj : getAllInterfacesAsObjects()){
        DirectBoundaryInterface intrfPair = (DirectBoundaryInterface) intrfObj;
        if(isFluidSolidInterface(intrfPair)){
            convertInterface_Direct2Indirect(intrfPair);
        } else {
            say("Not Fluid-Solid interface. Skipping.");
        }
    }
    sayOK();
  }

  public void convertInterface_Direct2Indirect(DirectBoundaryInterface intrfPair){
    say("Converting a Fluid-Solid Direct to Indirect type Interface");
    say("  Interface name: " + intrfPair.getPresentationName());
    Boundary b0 = intrfPair.getParentBoundary0();
    Boundary b1 = intrfPair.getParentBoundary1();
    say("");
    say("  Removing Direct Interface...");
    sim.getInterfaceManager().deleteInterfaces(intrfPair);
    say("  Creating Indirect Interface...");
    sim.getInterfaceManager().createIndirectInterface(b0, b1);
    sayOK();
  }

  /**
   * Puts the Solution Time within a Scene. Please use {@see #addAnnotation_SolutionTime} instead.
   */
  public void createAnnotation_SolutionTime() {
    //-- Empty
  }

  /**
   * Creates an Annotation Text in a Scene.
   *
   * @param scene given Scene.
   * @param text given text.
   * @param height given font height.
   * @param pos given position. E.g.: new double[] {0, 0, 0}
   * @return The Annotation Property within the Scene.
   */
  public FixedAspectAnnotationProp createAnnotation_Text(Scene scene, String text, double height, double[] pos) {
    SimpleAnnotation annot = sim.getAnnotationManager().createSimpleAnnotation();
    annot.setText(text);
    annot.setFontString("Dialog-italic-24");
    annot.setDefaultHeight(height);
    annot.setDefaultPosition(new DoubleVector(pos));
    annot.setPresentationName(text.replace(" ", ""));
    return (FixedAspectAnnotationProp) scene.getAnnotationPropManager().createPropForAnnotation(annot);
  }

  /**
   * Use {@see #createCameraView} instead.
   * @deprecated This method will be removed soon.
   */
  @Deprecated   // v2c
  public VisView createCamView(double[] fp, double[] pos, double[] vu, double ps, String camName) {
    return createCameraView(fp, pos, vu, ps, camName);
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
    VisView vv = createCameraView(new DoubleVector(fp), new DoubleVector(pos), new DoubleVector(vu), ps);
    vv.setPresentationName(camName);
    say(camName + " created.");
    sayOK();
    return vv;
  }

  private VisView createCameraView(DoubleVector fp, DoubleVector pos, DoubleVector vu, double ps) {
    VisView vv = sim.getViewManager().createView();
    vv.setInput(fp, pos, vu, ps, 1, lab0, false);
    return vv;
  }

  public CellSurfacePart createCellSet(Vector objVector, String name){
    printAction("Creating Cell Set with Objects");
    say("Objects: " + objVector.size());
    for(Object obj : objVector){
        say("  " + obj.toString());
    }
    CellSurfacePart cellSet = sim.getPartManager().createCellSurfacePart(objVector);
    cellSet.setPresentationName(name);
    sayOK();
    return cellSet;
  }

  /**
   * Creates an One Group Contact Prevention in a Region with all its Boundaries. Excluding Interfaces.
   *
   * @param region given Region.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPrevention(Region region, double value, Units unit){
    Vector<Boundary> vecBdries = (Vector<Boundary>) getAllBoundariesFromRegion(region, false, true);
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region with given Boundaries.
   *
   * @param region given Region.
   * @param vecBdries given Vector of Boundaries.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPrevention(Region region, Vector<Boundary> vecBdries, double value, Units unit){
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region, based on an Array of REGEX Patterns.
   *
   * @param region given Region.
   * @param regexPattArray given array of REGEX search patterns.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPrevention(Region region, String[] regexPattArray, double value, Units unit) {
    Vector<Boundary> vecBdries = (Vector<Boundary>) getAllBoundaries(region, regexPattArray, false, true);
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  private OneGroupContactPreventionSet createContactPrevention(Region region,
                    Vector<Boundary> vecBdries, double value, Units unit, boolean verboseOption){
    printAction("Creating a Contact Prevention between boundaries", verboseOption);
    if(vecBdries.size() < 2){
        say("Input boundaries: " + vecBdries.size(), verboseOption);
        say("ERROR! Input boundaries number MUST > 2. Skipping...", verboseOption);
        return null;
    }
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(vecBdries);
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString(), verboseOption);
    sayOK(verboseOption);
    return cps;
  }

  /**
   * Creates a Threshold Derived Part containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @param ff given Field Function.
   * @param min given Minimum value.
   * @param max given Maximum value.
   * @param unit given variable Unit. If <i>NULL</i> get Default unit.
   * @return The Threshold part.
   */
  public ThresholdPart createDerivedPart_Threshold(Collection<NamedObject> objects,
                                        FieldFunction ff, double min, double max, Units unit) {
    return createDerivedPart_Threshold(objects, ff, min, max, unit, true);
  }

  private ThresholdPart createDerivedPart_Threshold(Collection<NamedObject> objects,
                    FieldFunction ff, double min, double max, Units unit, boolean verboseOption) {
    printAction("Creating a Threshold Part", verboseOption);
    sayFieldFunction(ff, verboseOption);
    say(retMinMaxString(min, max), verboseOption);
    DoubleVector vecMinMax = new DoubleVector(new double[] {min, max});
    ThresholdPart thr = sim.getPartManager().createThresholdPart((Vector) objects, vecMinMax, unit, ff, 0);
    sayOK(verboseOption);
    return thr;

  }

  /**
   * Creates a Direct Interface Pair.
   *
   * @param bdry1 given Boundary 1.
   * @param bdry2 given Boundary 2.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(Boundary bdry1, Boundary bdry2){
    return createDirectInterfacePair(bdry1, bdry2, true);
  }

  /**
   * Creates a Direct Interface Pair given a REGEX search pattern.  <p>
   * If it finds more than 2 boundaries, gets the first 2.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(String regexPatt){
    printAction("Creating Direct Interface Pair by matching names in any regions");
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(regexPatt, false, true);
    if(vecBdry.size() == 2){
        say("Found 2 candidates. Interfacing:");
    } else if(vecBdry.size() > 2){
        say("Found more than 2 candidates. Interfacing the first two:");
    } else if(vecBdry.size() < 1) {
        say("Could not find 2 candidates. Giving up...");
        return null;
    }
    DirectBoundaryInterface intrfPair = createDirectInterfacePair(vecBdry.get(0), vecBdry.get(1), false);
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  private DirectBoundaryInterface createDirectInterfacePair(Boundary b1, Boundary b2, boolean verboseOption) {
    printAction("Creating a Direct Interface Pair", verboseOption);
    DirectBoundaryInterface intrfPair = sim.getInterfaceManager().createDirectInterface(b1, b2, "In-place");
    intrfPair.getTopology().setSelected(InterfaceConfigurationOption.IN_PLACE);
    sayInterface(intrfPair, verboseOption, false);
    sayOK(verboseOption);
    return intrfPair;
  }

  @Deprecated // v2c
  public FeatureCurve createFeatureCurveEmpty(Region region, String nameItAs) {
    printAction("Creating an Empty Feature Curve");
    sayRegion(region);
    FeatureCurve fc = region.getFeatureCurveManager().createEmptyFeatureCurve();
    fc.setPresentationName(nameItAs);
    sayOK();
    return fc;
  }

  /**
   * Creates a dimensionless Field Function.
   * @param name given name.
   * @param definition given Field Function definition.
   * @return The Field Function
   */
  public FieldFunction createFieldFunction(String name, String definition) {
    return createFieldFunction(name, definition, true);
  }

  private FieldFunction createFieldFunction(String name, String definition, boolean verboseOption) {
    printAction("Creating a Field Function", verboseOption);
    say("Name: " + name, verboseOption);
    say("Definition: " + definition, verboseOption);
    UserFieldFunction uff = sim.getFieldFunctionManager().createFieldFunction();
    uff.setPresentationName(name);
    uff.setFunctionName(name);
    uff.setDefinition(definition);
    sayOK(verboseOption);
    return uff;
  }

  /**
   * Creates a Folder in the current {@see #simPath} path.
   *
   * @param foldername given folder name.
   */
  public File createFolder(String foldername) {
    return createFolder(foldername, true);
  }

  /**
   * Creates a Folder in the current {@see #simPath} path.
   *
   * @param foldername given folder name.
   * @param verboseOption print output messages when inside a loop, for instance? Probably not.
   */
  public File createFolder(String foldername, boolean verboseOption) {
    printAction("Creating a Folder", verboseOption);
    File folder = null;
    try {
        folder = new File(simPath, foldername);
        boolean success = folder.mkdir();
        if (success)
            say("Folder created: " + foldername, verboseOption);
            sayOK();
    } catch (Exception e) {
        say("Error: " + e.getMessage(), verboseOption);
        say("Returning NULL!", verboseOption);
    }
    return folder;
  }

  public IndirectBoundaryInterface createIndirectInterfacePair(Boundary bdry1, Boundary bdry2){
    printAction("Creating Indirect Interface Pair given two boundaries");
    IndirectBoundaryInterface intrfPair = sim.getInterfaceManager().createIndirectInterface(bdry1, bdry2);
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  /**
   * Creates a Continua with Poly + Embedded Thin Meshers. <p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>OFF</b>
   * <li> Prisms Layers are <b>OFF</b>
   * </ul>
   * @return The new Mesh Continua.
   */
  public MeshContinuum createMeshContinua_EmbeddedThinMesher(){
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
   */
  public MeshContinuum createMeshContinua_PolyOnly(){
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
   */
  public MeshContinuum createMeshContinua_Trimmer() {
    MeshContinuum continua = createMeshContinuaTrimmer();
    enableSurfaceProximityRefinement(continua);
    enablePrismLayers(continua);
    sayOK();
    return continua;
  }

  private MeshContinuum createMeshContinuaPoly(){
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNamePoly);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(DualMesherModel.class);
    disableSurfaceProximityRefinement(continua);

    setMeshBaseSizes(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    setMeshTetPolyGrowthRate(continua, mshGrowthFactor);

    printMeshParameters(continua);
    return continua;
  }

  private MeshContinuum createMeshContinuaThinMesher() {
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNameThinMesher);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(SolidMesherModel.class);
    disableSurfaceProximityRefinement(continua);
    continua.getModelManager().getModel(SolidMesherModel.class).setOptimize(true);

    setMeshBaseSizes(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    continua.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);

    printMeshParameters(continua);
    return continua;
  }

  private MeshContinuum createMeshContinuaTrimmer() {
    printAction("Creating Mesh Continua");
    MeshContinuum continua = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    continua.setPresentationName(contNameTrimmer);
    continua.enable(ResurfacerMeshingModel.class);
    continua.enable(TrimmerMeshingModel.class);

    setMeshBaseSizes(continua, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(continua, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshPerRegionFlag(continua);
    setMeshCurvatureNumberOfPoints(continua, mshSrfCurvNumPoints);
    continua.getReferenceValues().get(MaximumCellSize.class).getRelativeSize().setPercentage(mshTrimmerMaxCelSize);
    //-- Growth Rate is not changed here anymore (v2c)
    //int i = getTrimmerGrowthRate(mshTrimmerGrowthRate);
    //mshCont.getReferenceValues().get(SimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(i);
    //--
    printMeshParameters(continua);
    return continua;
  }

  public Scene createMeshSceneWithCellSet(CellSurfacePart cellSet){
    printAction("Creating a Scene with Cell Set Mesh");
    say("Cell Set: " + cellSet.getPresentationName());
    Scene scene = sim.getSceneManager().createScene();
    scene.setPresentationName("Mesh Cell Set");
    PartDisplayer pd = scene.getDisplayerManager().createPartDisplayer("Cell Set");
    pd.initialize();
    pd.addPart(cellSet);
    pd.setSurface(true);
    pd.setMesh(true);
    pd.setColorMode(partColouring);
    sayOK();
    return scene;
  }

  public Scene createMeshSceneWithObjects(Collection<NamedObject> objects){
    printAction("Creating a Mesh Scene with Objects");
    say("Objects:");
    Scene scene = sim.getSceneManager().createScene();
    scene.setPresentationName("Mesh");
    scene.getDisplayerManager().createPartDisplayerTask("Geometry", -1, 1);
    PartDisplayer pd = scene.getDisplayerManager().createPartDisplayer("Mesh", -1, 1);
    pd.initialize();
    for(Object obj : objects){
        say("  " + obj.toString());
        pd.getParts().addPart((NamedObject) obj);
    }
    pd.setSurface(true);
    pd.setMesh(true);
    pd.setColorMode(4);
    sayOK();
    return scene;
  }

  /**
   * Creates a Volumetric Control in the given Part.
   *
   * @param continua given Mesh Continua.
   * @param gp given Geometry Part.
   * @param name given name.
   * @param relSize relative size in (<b>%</b>).
   */
  public void createMeshVolumetricControl(MeshContinuum continua, GeometryPart gp, String name, double relSize) {
    createMeshVolumetricControl(continua, Arrays.asList(new GeometryPart[] {gp}), name, relSize);
  }

  /**
   * Creates an Anisotropic Volumetric Control in the given Part. <b>Only for Trimmer</b>.
   *
   * @param continua given Mesh Continua.
   * @param gp given Geometry Part.
   * @param name given name.
   * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will be ignored.
   */
  public void createMeshVolumetricControl(MeshContinuum continua, GeometryPart gp, String name, double[] relSizes) {
    if (!isTrimmer(continua)) {
        return;
    }
    printAction("Creating an Anisotropic Volumetric Control in Mesh Continua");
    sayContinua(continua);
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
   * @param colGP given Collection of Geometry Parts.
   * @param name given name.
   * @param relSize relative size in (<b>%</b>).
   */
  public void createMeshVolumetricControl(MeshContinuum continua, Collection<GeometryPart> colGP,
                                                                    String name, double relSize) {
    printAction("Creating a Volumetric Control in Mesh Continua");
    sayContinua(continua);
    say("Given Parts: ");
    for(GeometryPart gp : colGP){
        say("  " + gp.getPathInHierarchy());
    }
    VolumeSource volSrc = continua.getVolumeSources().createVolumeSource();
    volSrc.setPresentationName(name);
    volSrc.getPartGroup().setObjects(colGP);
    if(isPoly(continua)) {
        volSrc.get(MeshConditionManager.class).get(VolumeSourceDualMesherSizeOption.class).setVolumeSourceDualMesherSizeOption(true);
    }
    if(isTrimmer(continua)) {
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
   * @param bdry given Boundary.
   * @param distance given distance in default units. See {@see #defUnitLength}.
   * @param dir given 3-components direction of the refinement. E.g., in X: {1, 0, 0}.
   * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will be ignored.
   */
  public void createMeshWakeRefinement(MeshContinuum continua, Boundary bdry, double distance,
                                                                double[] dir, double[] relSizes) {
    if (!isTrimmer(continua)) {
        return;
    }
    printAction("Creating a Mesh Wake Refinement");
    sayContinua(continua);
    sayBdry(bdry);
    WakeRefinementSet wrs = bdry.getRegion().get(MeshValueManager.class).get(WakeRefinementSetManager.class).createWakeRefinementSet();
    wrs.setPresentationName(bdry.getPresentationName());
    wrs.getDirection().setComponents(dir[0], dir[1], dir[2]);
    wrs.getBoundaryFeatureCurveGroup().setObjects(bdry);
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
   * Creates the Monitor and a Plot from a Report.
   *
   * @param rep given Report.
   * @param repName given Report name.
   * @param xAxisLabel given X-axis label.
   * @param yAxisLabel given Y-axis label.
   * @return The created Report Monitor.
   */
  public ReportMonitor createMonitorAndPlotFromReport(Report rep, String repName,
                                                String xAxisLabel, String yAxisLabel){
    ReportMonitor repMon = rep.createMonitor();
    repMon.setPresentationName(repName);
    say("Created Monitor: %s", repName);

    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(repName);
    say("Created Plot: %s", repName);
    monPl.getMonitors().addObjects(repMon);

    Axes axes_1 = monPl.getAxes();
    Axis axis_2 = axes_1.getXAxis();
    AxisTitle axisTitle_2 = axis_2.getTitle();
    axisTitle_2.setText(xAxisLabel);
    Axis axis_3 = axes_1.getYAxis();
    AxisTitle axisTitle_3 = axis_3.getTitle();
    axisTitle_3.setText(yAxisLabel);
    // Add Monitor to the Global Vector
    vecRepMon.add(repMon);
    monPlot = monPl;
    return repMon;
  }

  /**
   * Creates a Translation Motion and assigns to a Region.<p>
   * Note the Translation Velocity will be given in default units. See {@see #defUnitVel}.
   *
   * @param definition given motion equation. E.g.: "[$ff1, 2.0, 5 / $ff2]".
   * @param region given Region.
   * @return The create Motion.
   */
  public Motion createMotion_Translation(String definition, Region region) {
    printAction("Creating a Translation Motion");
    say("Definition: " + definition);
    sayRegion(region);
    TranslatingMotion tm = sim.get(MotionManager.class).createMotion(TranslatingMotion.class, "Translation");
    tm.getTranslationVelocity().setDefinition(definition);
    tm.getTranslationVelocity().setUnits(defUnitVel);
    ((MotionSpecification) region.getValues().get(MotionSpecification.class)).setMotion(tm);
    sayOK();
    return tm;
  }

  /**
   * Creates a Single Plot from the selected Report Monitors.
   *
   * @param colRP given Collection of Report Monitors.
   * @param plotName given Plot name.
   * @return The created Monitor Plot.
   */
  public MonitorPlot createPlot(Collection<ReportMonitor> colRP, String plotName){
    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(plotName);
    monPl.getMonitors().addObjects(colRP);

    Axes axes = monPl.getAxes();
    Axis xx = axes.getXAxis();
    xx.getTitle().setText(colRP.iterator().next().getXAxisName());
    Axis yy = axes.getYAxis();
    yy.getTitle().setText(colRP.iterator().next().getMonitorDescription());

    return monPl;
  }


  public void createWrapperSeedPoint(Region region, double X, double Y, double Z, Units unit){
    setMeshWrapperVolumeSeedPoints(region);
    printAction("Creating a Wrapper Seed Point");
    sayRegion(region);
    say(String.format("Point Coordinate: %f, %f, %f [%s]", X, Y, Z, unit.toString()));
    VolumeOfInterestSeedPoint seedPt = region.get(MeshValueManager.class).get(VolumeOfInterestSeedPointManager.class).createSeedPoint();
    seedPt.getCoordinates().setUnits(unit);
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
    say("Density = " + den + " " + cmpm.getQuantity().getUnits().toString());
    say("Thermal Conductivity = " + k + " " + cmpm2.getQuantity().getUnits().toString());
    say("Specific Heat = " + cp + " " + ccp.getQuantity().getUnits().toString());
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
//    if (getVer() > 800) {
//        phC.enable(SingleComponentGasModel.class);
//    } else {
//        phC.enable(singleComponentClass);
//    }
    phC.enable(SegregatedFlowModel.class);
    phC.enable(ConstantDensityModel.class);
    phC.enable(LaminarModel.class);
    phC.enable(CellQualityRemediationModel.class);
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
    sldCont.enable(CellQualityRemediationModel.class);
    setInitialCondition_T(sldCont, solidsT0, false);
    if(solidMaterial.matches(noneString)){ return sldCont; }
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
    vof.enable(CellQualityRemediationModel.class);
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
   * Creates a Force Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param bdry given Boundary.
   * @param reportName given Report name.
   */
  public void createReportForce(Boundary bdry, String reportName, double[] direction) {
    createReportForce(Arrays.asList(new Boundary[] {bdry}), reportName, direction);
  }

  /**
   * Creates a Force Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForce(Collection<Boundary> colBdy, String reportName, double[] direction) {
    createReportForce(colBdy, reportName, direction, true);
  }

  private void createReportForce(Collection<Boundary> colBdy, String reportName,
                                                        double[] direction, boolean verboseOption) {
    printAction("Creating a Force Report on Boundaries", verboseOption);
    sayBoundaries(colBdy);
    ForceReport forceRep = sim.getReportManager().createReport(ForceReport.class);
    forceRep.setPresentationName(reportName);
    forceRep.getReferencePressure().setUnits(unit_Pa);
    forceRep.getReferencePressure().setValue(0.0);
    forceRep.setUnits(defUnitForce);
    forceRep.getParts().setObjects(colBdy);
    forceRep.getDirection().setComponents(direction[0], direction[1], direction[2]);
    String unitStr = forceRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(forceRep, reportName, "Iteration", "Force (" + unitStr + ")");
  }

  /**
   * Creates a Force Coefficient Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated. <p>
   * <b>The Reference Density will be taken from the Physics Continua associated with the Boundary.</b>
   *
   * @param bdry given Boundary.
   * @param reportName given Report name.
   * @param refVel given Reference Velocity in default unit. See {@see #defUnitVel}.
   * @param refArea given Reference Area in default unit. See {@see #defUnitArea}.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForceCoefficient(Boundary bdry, String reportName, double refVel,
                                                                double refArea, double[] direction) {
    createReportForceCoefficient(Arrays.asList(new Boundary[] {bdry}), reportName, refVel, refArea, direction);
  }

  /**
   * Creates a Force Coefficient Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated. <p>
   * <b>The Reference Density will be taken from the Physics Continua of first Boundary
   * provided in the Collection.</b>
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param refVel given Reference Velocity in default unit. See {@see #defUnitVel}.
   * @param refArea given Reference Area in default unit. See {@see #defUnitArea}.
   * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0, 0}.
   */
  public void createReportForceCoefficient(Collection<Boundary> colBdy, String reportName,
                                                double refVel, double refArea, double[] direction) {
    createReportForceCoefficient(colBdy, reportName, refVel, refArea, direction, true);
  }

  private void createReportForceCoefficient(Collection<Boundary> colBdy, String reportName,
                                    double refVel, double refArea, double[] direction, boolean verboseOption) {
    printAction("Creating a Force Coefficient Report on Boundaries", verboseOption);
    sayBoundaries(colBdy);
    ForceCoefficientReport forceCoeffRep = sim.getReportManager().createReport(ForceCoefficientReport.class);
    forceCoeffRep.setPresentationName(reportName);
    //--
    //-- Reference Density Retrieve
    try {
        Boundary _bdry = colBdy.iterator().next();
        say("Getting Reference Density from Boundary (Assuming it is a Gas): " + _bdry.getPresentationName());
        PhysicsContinuum pC = _bdry.getRegion().getPhysicsContinuum();
//        Gas gas = (Gas) pC.getModelManager().getModel(SingleComponentGasModel.class).getMaterial();
//        ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod());
        Model model = pC.getModelManager().getModel(SingleComponentGasModel.class);
        ConstantMaterialPropertyMethod cmpm = getMatPropMeth_Const(model, ConstantDensityProperty.class);
        forceCoeffRep.getReferenceDensity().setUnits(cmpm.getQuantity().getUnits());
        forceCoeffRep.getReferenceDensity().setValue(cmpm.getQuantity().getValue());
        say("Got: : " + forceCoeffRep.getReferenceDensity().getValue() + forceCoeffRep.getReferenceDensity().getUnits().toString());
    } catch (Exception e) {
        say("Error getting Reference Density! Using 1 kg/m^3.");
        forceCoeffRep.getReferenceDensity().setUnits(unit_kgpm3);
        forceCoeffRep.getReferenceDensity().setValue(1.0);
    }
    forceCoeffRep.getReferenceVelocity().setUnits(defUnitVel);
    forceCoeffRep.getReferenceVelocity().setValue(refVel);
    forceCoeffRep.getReferenceArea().setUnits(defUnitArea);
    forceCoeffRep.getReferenceArea().setValue(refArea);
    forceCoeffRep.getForceOption().setSelected(ForceReportForceOption.PRESSURE_AND_SHEAR);
    forceCoeffRep.getDirection().setComponents(direction[0], direction[1], direction[2]);
    forceCoeffRep.getReferencePressure().setUnits(unit_Pa);
    forceCoeffRep.getReferencePressure().setValue(0.0);
    forceCoeffRep.getParts().setObjects(colBdy);
    String unitStr = forceCoeffRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(forceCoeffRep, reportName, "Iteration", "Force Coefficient");
  }

  /**
   * Creates a Mass Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Region.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassAverage(Region region, String reportName, String var, Units unit) {
    createReportMassAverage(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Mass Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassAverage(Collection<Region> colReg, String reportName, String var, Units unit) {
    createReportMassAverage(colReg, reportName, var, unit, true);
  }

  private void createReportMassAverage(Collection<Region> colReg, String reportName, String var,
                                                                Units unit, boolean verboseOption) {
    printAction("Creating a Mass Average Report of " + var + " on Regions", verboseOption);
    sayRegions(colReg);
    MassAverageReport massAvgRep = sim.getReportManager().createReport(MassAverageReport.class);
    massAvgRep.setScalar(getFieldFunction(var, false));
    massAvgRep.setUnits(unit);
    massAvgRep.setPresentationName(reportName);
    massAvgRep.getParts().setObjects(colReg);
    String yAxisLabel = "Mass Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(massAvgRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Mass Flow Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param bdry given Boundary.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlow(Boundary bdry, String reportName, String var, Units unit) {
    createReportMassFlow(Arrays.asList(new Boundary[] {bdry}), reportName, var, unit);
  }

  /**
   * Creates a Mass Flow Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlow(Collection<Boundary> colBdy, String reportName, String var, Units unit) {
    createReportMassFlow(colBdy, reportName, var, unit, true);
  }

  private void createReportMassFlow(Collection<Boundary> colBdy, String reportName,
                                                    String var, Units unit, boolean verboseOption) {
    printAction("Creating a Mass Flow Report of " + var + " on Boundaries", verboseOption);
    sayBoundaries(colBdy);
    MassFlowReport mfRep = sim.getReportManager().createReport(MassFlowReport.class);
    mfRep.setPresentationName(reportName);
    mfRep.getParts().setObjects(colBdy);
    String unitStr = mfRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(mfRep, reportName, "Iteration", "Mass Flow (" + unitStr + ")");
  }

  /**
   * Creates a Mass Flow Average Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param bdry  given Boundary.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlowAverage(Boundary bdry, String reportName, String var, Units unit) {
    createReportMassFlowAverage(Arrays.asList(new Boundary[] {bdry}), reportName, var, unit);
  }

  /**
   * Creates a Mass Flow Average Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlowAverage(Collection<Boundary> colBdy, String reportName,
                                                                        String var, Units unit) {
    createReportMassFlowAverage(colBdy, reportName, var, unit, true);
  }

  private void createReportMassFlowAverage(Collection<Boundary> colBdy, String reportName,
                                                    String var, Units unit, boolean verboseOption) {
    printAction("Creating a Mass Flow Average Report of " + var + " on Boundaries", verboseOption);
    sayBoundaries(colBdy);
    MassFlowAverageReport mfaRep = sim.getReportManager().createReport(MassFlowAverageReport.class);
    mfaRep.setScalar(getFieldFunction(var, false));
    mfaRep.setUnits(unit);
    mfaRep.setPresentationName(reportName);
    mfaRep.getParts().setObjects(colBdy);
    String unitStr = unit.getPresentationName();
    String yAxisLabel = "Mass Flow Average of " + var + " (" + unitStr + ")";
    repMon = createMonitorAndPlotFromReport(mfaRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Maximum Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMaximum(Region region, String reportName, String var, Units unit) {
    createReportMaximum(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Maximum Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMaximum(Collection<Region> colReg, String reportName, String var,
                                                                                    Units unit) {
    createReportMaximum(colReg, reportName, var, unit, true);
  }

  private void createReportMaximum(Collection<Region> colReg, String reportName, String var,
                                                                Units unit, boolean verboseOption) {
    printAction("Creating a Maximum Report of " + var + " on Regions", verboseOption);
    sayRegions(colReg);
    MaxReport maxRep = sim.getReportManager().createReport(MaxReport.class);
    maxRep.setScalar(getFieldFunction(var, false));
    maxRep.setUnits(unit);
    maxRep.setPresentationName(reportName);
    maxRep.getParts().setObjects(colReg);
    String yAxisLabel = "Maximum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(maxRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Minimum Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMinimum(Region region, String reportName, String var, Units unit) {
    createReportMinimum(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Minimum Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportMinimum(Collection<Region> colReg, String reportName, String var,
                                                                                    Units unit) {
    createReportMinimum(colReg, reportName, var, unit, true);
  }

  private void createReportMinimum(Collection<Region> colReg, String reportName,
                                                    String var, Units unit, boolean verboseOption) {
    printAction("Creating a Minimum Report of " + var + " on Regions", verboseOption);
    MinReport minRep = sim.getReportManager().createReport(MinReport.class);
    minRep.setScalar(getFieldFunction(var, false));
    minRep.setUnits(unit);
    minRep.setPresentationName(reportName);
    minRep.getParts().setObjects(colReg);
    String yAxisLabel = "Minimum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(minRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Volume Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportVolumeAverage(Region region, String reportName, String var, Units unit) {
    createReportVolumeAverage(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Volume Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given variable name using a REGEX search pattern.
   * @param unit variable corresponding Unit.
   */
  public void createReportVolumeAverage(Collection<Region> colReg, String reportName, String var, Units unit) {
    createReportVolumeAverage(colReg, reportName, var, unit, true);
  }

  private void createReportVolumeAverage(Collection<Region> colReg, String reportName,
                                                    String var, Units unit, boolean verboseOption) {
    printAction("Creating a Volume Average Report of " + var + " on Regions", verboseOption);
    sayRegions(colReg);
    VolumeAverageReport volAvgRep = sim.getReportManager().createReport(VolumeAverageReport.class);
    volAvgRep.setScalar(getFieldFunction(var, false));
    volAvgRep.setUnits(unit);
    volAvgRep.setPresentationName(reportName);
    volAvgRep.getParts().setObjects(colReg);
    String yAxisLabel = "Volume Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(volAvgRep, reportName, "Iteration", yAxisLabel);
  }

  public void createRotatingReferenceFrameForRegion(Region region, double[] axis, double[] origin, Units origUnit,
                                    double rotValue, Units rotUnit) {
    RotatingMotion rm = sim.get(MotionManager.class).createMotion(RotatingMotion.class, "Rotation");
    rm.getAxisDirection().setComponents(axis[0], axis[1], axis[2]);
    rm.getAxisOrigin().setUnits(origUnit);
    rm.getAxisOrigin().setComponents(origin[0], origin[1], origin[2]);
    rm.getRotationRate().setUnits(rotUnit);
    rm.getRotationRate().setValue(rotValue);
    MotionSpecification ms = region.getValues().get(MotionSpecification.class);
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
    return createScene("Empty", new Vector<NamedObject>(), true);
  }

  /**
   * Creates a Geometry Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Geometry() {
    return createScene("Geometry", new Vector<NamedObject>(), true);
  }

  private Scene createScene_Geometry(boolean verboseOption) {
    return createScene("Geometry", new Vector<NamedObject>(), false);
  }

  /**
   * Creates a Geometry Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @return Created Scene.
   */
  public Scene createScene_Geometry(Collection<NamedObject> objects) {
    return createScene("Geometry", objects, true);
  }

  /**
   * Creates a Mesh Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Mesh() {
    return createScene("Mesh", new Vector<NamedObject>(), true);
  }

  /**
   * Creates a Mesh Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @return Created Scene.
   */
  public Scene createScene_Mesh(Collection<NamedObject> objects) {
    return createScene("Mesh", objects, true);
  }

  /**
   * Creates a Scalar Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @param varName given function name inside the Field Functions. E.g.: Velocity,
   *                BoundaryHeatFlux, FaceFlux, etc.
   * @param unit given variable Unit. If <i>NULL</i> get Default unit.
   * @param smoothFilled Smooth Fill?
   * @return Created Scene.
   * @deprecated In v2c. Use the other method which has the Field Function as input. It is much
   * more effective as there is a {@see #getFieldFunction} method now.
   */
  public Scene createScene_Scalar(Collection<NamedObject> objects, String varName, Units unit,
                                                                            boolean smoothFilled) {
    FieldFunction _ff = getFieldFunction(varName, false);
    if(varName.matches("^Vel.*")) {
        _ff = sim.getFieldFunctionManager().getFunction("Velocity");
        if (varName.equalsIgnoreCase("Velocity") || varName.equalsIgnoreCase("VelMag") ||
                                                            varName.equalsIgnoreCase("Vel Mag")) {
            _ff = _ff.getMagnitudeFunction();
        } else if(varName.equalsIgnoreCase("VelX") || varName.equalsIgnoreCase("Vel X")){
            _ff = _ff.getComponentFunction(0);
        } else if(varName.equalsIgnoreCase("VelY") || varName.equalsIgnoreCase("Vel Y")){
            _ff = _ff.getComponentFunction(1);
        } else if(varName.equalsIgnoreCase("VelZ") || varName.equalsIgnoreCase("Vel Z")){
            _ff = _ff.getComponentFunction(2);
        }
    } else if(varName.matches("^WallShear.*")) {
        _ff = sim.getFieldFunctionManager().getFunction("WallShearStress");
        if (varName.equalsIgnoreCase("WallShearStress") ||
                varName.equalsIgnoreCase("WallShearMag") ||
                varName.equalsIgnoreCase("Wall Shear Mag")) {
            _ff = _ff.getMagnitudeFunction();
        }
    }
    return createScene_Scalar(objects, _ff, unit, smoothFilled);
  }

  /**
   * Creates a Scalar Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects. E.g.: {@see #vecObj}.
   *
   * @param objects given Collection of Objects.
   * @param ff given Field Function. Use {@see #getFieldFunction} method as needed.
   * @param unit given variable Unit.
   * @param smoothFilled Smooth Fill?
   * @return Created Scene.
   */
  public Scene createScene_Scalar(Collection<NamedObject> objects, FieldFunction ff, Units unit,
                                                                            boolean smoothFilled) {
    Scene scn = createScene("Scalar", objects, true);
    ScalarDisplayer scalDisp = (ScalarDisplayer) scn.getDisplayerManager().getDisplayer("Scalar");
    if(smoothFilled){
        scalDisp.setFillMode(1);
    }
    if (isVector(ff)) {
        ff = ff.getMagnitudeFunction();
    }
    scalDisp.getScalarDisplayQuantity().setFieldFunction(ff);
    scalDisp.getScalarDisplayQuantity().setUnits(unit);
    return scn;
  }

  /**
   * Creates a Vector Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @param licOption Linear Integral Convolution option? <b>True</b> or <b>False</b>
   * @return Created Scene.
   */
  public Scene createScene_Vector(Collection<NamedObject> objects, boolean licOption) {
    Scene scn = createScene("Vector", objects, true);
    VectorDisplayer vecDisp = (VectorDisplayer) scn.getDisplayerManager().getDisplayer("Vector");
    vecDisp.getVectorDisplayQuantity().setUnits(defUnitVel);
    if(licOption){
        vecDisp.setDisplayMode(1);
    }
    return scn;
  }

  private Scene createScene(String sceneType, Collection<NamedObject> objects, boolean verboseOption){
    printAction("Creating a " + sceneType + " Scene", verboseOption);
    String pdName = "__tmp__";
    String sceneName = pdName + sceneType;      // Trying to make an unique name
    sim.getSceneManager().setDebug(true);
    sim.getSceneManager().setVerbose(true);
    Scene scn = sim.getSceneManager().createScene(sceneType);
    scn.setPresentationName(sceneType);
    scn.initializeAndWait();
    //--
    ((PartDisplayer) scn.getHighlightDisplayer()).initialize();
    //--
    //-- Trick to address issues when running in batch and a Scalar Scene
//    if (sceneType.equals("Scalar")) {
//        scn.getCurrentView().setInput(dv0, dv0, dv0, 1, 1);
//    }
    //--
    if (defCamView != null) {
        setSceneCameraView(scn, defCamView);
    }
    setSceneLogo(scn);
    setSceneBackgroundColor_Solid(scn, Color.white);
    if (!sceneType.equals("Empty")) {
        Displayer disp = createSceneDisplayer(scn, sceneType, objects, verboseOption);
        setDisplayerEnhancements(disp);
    }
    sayOK(verboseOption);
    return scn;
  }

  /**
   * Adds a new Displayer into the Scene.
   *
   * @param scene given Scene.
   * @param sceneType given Type. It can be <b>Geometry</b>, <b>Mesh</b>, <b>Scalar</b> or <b>Vector</b>.
   * @param objects given Collection of Objects.
   * @return The new Displayer.
   */
  public Displayer createSceneDisplayer(Scene scene, String sceneType, Collection<NamedObject> objects) {
    return createSceneDisplayer(scene, sceneType, objects, true);
  }

  private Displayer createSceneDisplayer(Scene scene, String sceneType,
                                Collection<NamedObject> objects, boolean verboseOption) {
    Displayer displ = null;
    PartDisplayer partDisp = null;
    ScalarDisplayer scalDisp = null;
    VectorDisplayer vecDisp = null;
    if (sceneType.equals("Empty")) {
        return null;
    }
    printAction("Creating a " + sceneType + " Scene Displayer", verboseOption);
    if (sceneType.equals("Geometry") || sceneType.equals("Mesh")) {
        partDisp = scene.getDisplayerManager().createPartDisplayer(sceneType);
        partDisp.setColorMode(colourByPart);
        partDisp.setOutline(false);
        partDisp.setSurface(true);
        if (sceneType.equals("Mesh")) {
            partDisp.setMesh(true);
        }
        partDisp.initialize();
    } else if (sceneType.equals("Scalar")) {
        scalDisp = scene.getDisplayerManager().createScalarDisplayer(sceneType);
        scalDisp.initialize();
    } else if (sceneType.equals("Vector")) {
        vecDisp = scene.getDisplayerManager().createVectorDisplayer(sceneType);
        vecDisp.initialize();
    }
    say("Adding objects to Displayer...", verboseOption);
    if (objects.isEmpty()) {
        if(sim.getRegionManager().isEmpty()) {
            objects.addAll(getAllPartSurfaces(".*", false));
        } else {
            objects.addAll(getAllBoundaries(".*", false));
        }
    }
    for(Object obj : objects){
        say("  " + obj.toString(), verboseOption);
    }
    if (sceneType.equals("Geometry") || sceneType.equals("Mesh")) {
        partDisp.addParts(objects);
        displ = partDisp;
    } else if (sceneType.equals("Scalar")) {
        scalDisp.addParts(objects);
        displ = scalDisp;
    } else if (sceneType.equals("Vector")) {
        vecDisp.addParts(objects);
        displ = vecDisp;
    }
    say("Objects added: " + objects.size(), verboseOption);
    if (scene.getDisplayerManager().getObjects().size() == 1) {
        displ.setPresentationName(scene.getPresentationName());
    } else {
        displ.setPresentationName(sceneType);
    }
    return displ;
  }

  /**
   * Creates a Section Plane.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param orientation  given normal orientation coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlane(double[] origin, double[] orientation) {
    printAction("Creating a Section Plane");
    Vector<Object> where = (Vector) getAllRegions();
    DoubleVector vecOrient = new DoubleVector(orientation);
    DoubleVector vecOrigin = new DoubleVector(origin);
    DoubleVector vecOffsets = new DoubleVector(new double[] {0.0});
    PlaneSection plane = (PlaneSection) sim.getPartManager().createImplicitPart(where, vecOrient, vecOrigin, 0, 1, vecOffsets);
    plane.getOriginCoordinate().setCoordinate(defUnitLength, defUnitLength, defUnitLength, vecOrigin);
    return plane;
  }

  /**
   * Creates a Section Plane Normal to X direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneX(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {1., 0., 0.});
    say("Normal to X direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Section Plane Normal to Y direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneY(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 1., 0.});
    say("Normal to Y direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Section Plane Normal to Z direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneZ(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 0., 1.});
    say("Normal to Z direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Simple Block based on the relative dimensions of the given Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @param coordRelSize1 given 3-components array, relative to the collection.
   * @param coordRelSize2 given 3-components array, relative to the collection.
   * @param name given Block name.
   * @return The brand new Block Part.
   */
  public SimpleBlockPart createShapePartBlock(Collection<PartSurface> colPS,
                    double[] coordRelSize1, double[] coordRelSize2, String name) {
    DoubleVector dvExtents = getExtents(colPS);
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
   * Creates a Simple Block.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param unit given units.
   * @param name given Block name.
   * @return The brand new Block Part.
   */
  public SimpleBlockPart createShapePartBlock(double[] coord1, double[] coord2, Units unit, String name) {
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleBlockPart sbp = mpf.createNewBlockPart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    sbp.setCoordinateSystem(labCSYS);
    Coordinate coordinate_0 = sbp.getCorner1();
    Coordinate coordinate_1 = sbp.getCorner2();
    coordinate_0.setCoordinate(unit, unit, unit, new DoubleVector(coord1));
    coordinate_1.setCoordinate(unit, unit, unit, new DoubleVector(coord2));
    sbp.getTessellationDensityOption().setSelected(TessellationDensityOption.MEDIUM);
    sbp.setPresentationName(name);
    return sbp;
  }

  /**
   * Creates a Simple Cylinder Part.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param r1 given radius 1.
   * @param r2 given radius 2.
   * @param unit given units.
   * @param name given Cylinder name.
   * @return The brand new Cylinder Part.
   */
  public SimpleCylinderPart createShapePartCylinder(double[] coord1, double[] coord2, double r1, double r2, Units unit, String name){
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleCylinderPart scp = mpf.createNewCylinderPart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    scp.setCoordinateSystem(labCSYS);
    scp.getRadius().setUnits(unit);
    scp.getEndRadius().setUnits(unit);
    Coordinate coordinate_0 = scp.getStartCoordinate();
    Coordinate coordinate_1 = scp.getEndCoordinate();
    coordinate_0.setCoordinate(unit, unit, unit, new DoubleVector(coord1));
    coordinate_1.setCoordinate(unit, unit, unit, new DoubleVector(coord2));
    scp.getRadius().setValue(r1);
    scp.getRadius().setUnits(unit);
    scp.getEndRadius().setValue(r2);
    scp.getEndRadius().setUnits(unit);
    scp.getTessellationDensityOption().setSelected(TessellationDensityOption.MEDIUM);
    scp.setPresentationName(name);
    return scp;
  }

  /**
   * Creates a Simple Sphere Part. The origin is located in the Centroid of the given Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @param relSize the radius of sphere is given relative to the max(dx, dy, dz). E.g.: 5, is equivalent
   *                to 5 * max(dx, dy, dz).
   * @param name given Sphere name.
   * @return The brand new Sphere Part.
   */
  public SimpleSpherePart createShapePartSphere(Collection<PartSurface> colPS, double relSize, String name) {
    DoubleVector dvExtents = getExtents(colPS);
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
   * Creates a Simple Sphere Part.
   *
   * @param coord given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param r given radius.
   * @param unit given unit.
   * @param name given Sphere name.
   * @return The brand new Sphere Part.
   */
  public SimpleSpherePart createShapePartSphere(double[] coord, double r, Units unit, String name) {
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleSpherePart ssp = mpf.createNewSpherePart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    ssp.setCoordinateSystem(labCSYS);
    Coordinate coordinate_0 = ssp.getOrigin();
    coordinate_0.setCoordinate(unit, unit, unit, new DoubleVector(coord));
    ssp.getRadius().setValue(r);
    ssp.getRadius().setUnits(unit);
    ssp.getTessellationDensityOption().setSelected(TessellationDensityOption.MEDIUM);
    ssp.setPresentationName(name);
    return ssp;
  }

  /**
   * Creates a Stopping Criteria from a Report Monitor.<p>
   * Types can be <b>Asymptotic</b>, <b>Min</b>, <b>Max</b> or <b>Standard Deviation</b>.
   *
   * @param repMon given Report Monitor.
   * @param type use <b>Asymptotic</b>, <b>Min</b>, <b>Max</b> or <b>Standard Deviation</b>.
   * @param val given value.
   * @param samples how many samples (or iterations)? If using Min/Max, this input is ignored.
   * @return The Stopping Criteria.
   */
  public SolverStoppingCriterion createStoppingCriteria(ReportMonitor repMon, String type, double val, int samples) {
    String name = "";
    type = type.toUpperCase();
    MonitorIterationStoppingCriterion monItStpCrit = repMon.createIterationStoppingCriterion();
    monItStpCrit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
    MonitorIterationStoppingCriterionOption critOpt = (MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption();
    if (type.equalsIgnoreCase("ASYMPTOTIC")) {
        name = "Asymptotic differences";
        critOpt.setSelected(MonitorIterationStoppingCriterionOption.ASYMPTOTIC);
        MonitorIterationStoppingCriterionAsymptoticType stpCritAsympt = (MonitorIterationStoppingCriterionAsymptoticType) monItStpCrit.getCriterionType();
        stpCritAsympt.getMaxWidth().setUnits(repMon.getMonitoredValueUnits());
        stpCritAsympt.getMaxWidth().setValue(val);
        stpCritAsympt.setNumberSamples(samples);
    } else if (type.equals("MAX")) {
        name = "Maximum value";
        critOpt.setSelected(MonitorIterationStoppingCriterionOption.MAXIMUM);
        MonitorIterationStoppingCriterionMaxLimitType stpCritMax = (MonitorIterationStoppingCriterionMaxLimitType) monItStpCrit.getCriterionType();
        stpCritMax.getLimit().setUnits(repMon.getMonitoredValueUnits());
        stpCritMax.getLimit().setValue(val);
    } else if (type.equalsIgnoreCase("MIN")) {
        name = "Minimum value";
        critOpt.setSelected(MonitorIterationStoppingCriterionOption.MINIMUM);
        MonitorIterationStoppingCriterionMinLimitType stpCritMin = (MonitorIterationStoppingCriterionMinLimitType) monItStpCrit.getCriterionType();
        stpCritMin.getLimit().setUnits(repMon.getMonitoredValueUnits());
        stpCritMin.getLimit().setValue(val);
    } else if (type.equalsIgnoreCase("StdDev")) {
        name = "Standard Deviation";
        critOpt.setSelected(MonitorIterationStoppingCriterionOption.STANDARD_DEVIATION);
        MonitorIterationStoppingCriterionStandardDeviationType stpCritStdDev = (MonitorIterationStoppingCriterionStandardDeviationType) monItStpCrit.getCriterionType();
        stpCritStdDev.getStandardDeviation().setUnits(repMon.getMonitoredValueUnits());
        stpCritStdDev.getStandardDeviation().setValue(val);
        stpCritStdDev.setNumberSamples(samples);
    }
    say("Created a Stopping Criterion based on %s.", name);
    say("   Name: %s", monItStpCrit.getPresentationName());
    say("   %s: %g %s", name, val, repMon.getMonitoredValueUnits());
    if (!type.matches("M.."))
        say("   Number of Samples: %d", samples);
    return (SolverStoppingCriterion) monItStpCrit;
  }

  /**
   * Creates a Stopping Criteria from a Report Monitor.<p>
   * For now, the type can be <b>Asymptotic</b> or <b>Standard Deviation</b>
   *
   * @param repMon given Report Monitor.
   * @param val given value.
   * @param samples how many samples (or iterations)?
   * @param stdDev <b>True</b> for Standard Deviation; <b>False</b> for Asymptotic.
   * @deprecated In v2c. Use {@see #createStoppingCriteria(ReportMonitor, String, double, int)} instead.
   */
  public void createStoppingCriteria(ReportMonitor repMon, double val, int samples, boolean stdDev) {
    if (stdDev) {
        say("Creating a Stopping Criterion based on Standard Deviation.");
        say("  Standard Deviation: " + val + " " + repMon.getMonitoredValueUnits());
    } else {
        say("Creating a Stopping Criterion based on Asymptotic differences.");
        say("  Asymptotic: " + val + " " + repMon.getMonitoredValueUnits());
    }
    say("  Number of Samples: " + samples);
    if(val == 0 || samples == 0){
        say("Got NULL inputs. Skipping creation. Just monitoring.");
        return;
    }
    MonitorIterationStoppingCriterion monItStpCrit = repMon.createIterationStoppingCriterion();
    monItStpCrit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
    if (stdDev) {
        ((MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.STANDARD_DEVIATION);
        MonitorIterationStoppingCriterionStandardDeviationType stpCritStdDev = ((MonitorIterationStoppingCriterionStandardDeviationType) monItStpCrit.getCriterionType());
        stpCritStdDev.getStandardDeviation().setUnits(repMon.getMonitoredValueUnits());
        stpCritStdDev.getStandardDeviation().setValue(val);
        stpCritStdDev.setNumberSamples(samples);
    } else {
        ((MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.ASYMPTOTIC);
        MonitorIterationStoppingCriterionAsymptoticType stpCritAsympt = ((MonitorIterationStoppingCriterionAsymptoticType) monItStpCrit.getCriterionType());
        stpCritAsympt.getMaxWidth().setUnits(repMon.getMonitoredValueUnits());
        stpCritAsympt.getMaxWidth().setValue(val);
        stpCritAsympt.setNumberSamples(samples);
    }
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
    Vector<Double> dens = new Vector<Double>();
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

  public void customizeInitialTemperatureConditionForRegion(Region region, double T, Units unit){
    printAction("Customizing a Region Initial Condition");
    sayRegion(region);
    say("Value: " + T + unit.toString());
    region.getConditions().get(InitialConditionOption.class).setSelected(InitialConditionOption.REGION);
    StaticTemperatureProfile temp = region.get(RegionInitialConditionManager.class).get(StaticTemperatureProfile.class);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    sayOK();
  }

  public void customizeThinMesherForRegion(Region region, int numLayers){
    printAction("Customizing a Region Thin Mesher Parameter");
    sayRegion(region);
    say("Thin Mesher Layers: " + numLayers);
    region.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(SolidMesherRegionOption.CUSTOM_VALUES);
    region.get(MeshValueManager.class).get(ThinSolidLayers.class).setLayers(numLayers);
    sayOK();
  }

  /**
   * This method creates:<ul>
   * <li>Reports of Max Velocity and Min/Max Temperature (if apply)
   * <li>Stopping Criteria based on Max Velocity.
   * <li>Scalar Scene with an empty Threshold.
   * </ul>
   * It is useful for diagnosing the case prior to an eventual divergence.
   */
  public void debugCase() {
    printAction("Debug Utils: Adding some Reports and Stopping Criteria");
    SolverStoppingCriterion stpCrit = getStoppingCriteria("maxVel.*", false);
    double stopVal = 1e5;
    if (stpCrit == null) {
        say("Creating Max Vel Report.");
        createReportMaximum(getAllRegions(false), "maxVel", varVel, defUnitVel, false);
        ReportMonitor rm = (ReportMonitor) getMonitor("maxVel.*", false);
        SolverStoppingCriterion stp = createStoppingCriteria(rm, "Max", stopVal, 0);
        stp.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.OR);
        stp.setPresentationName("maxVel Limit");
        say("   Renamed to \"%s\".", stp.getPresentationName());
        ThresholdPart thr = createDerivedPart_Threshold((Vector) getAllRegions(false),
                    getFieldFunction(varVel, false).getMagnitudeFunction(), 0.1 * stopVal, stopVal,
                    defUnitVel, false);
        thr.setPresentationName("Threshold of Velocity");
        Vector<NamedObject> objs = new Vector<NamedObject>();
        objs.add(thr);
        Scene scn = createScene_Scalar(objs, varVel, defUnitVel, false);
        scn.setPresentationName("Threshold of Velocity");
    } else
        say("Stopping Criteria \"%s\" already exists.", stpCrit.getPresentationName());
    if (hasEnergy()) {
        if (getPlot("maxT.*", false) == null)
            createReportMaximum(getAllRegions(false), "maxT", varT, defUnitTemp);
        if (getPlot("minT.*", false) == null)
            createReportMaximum(getAllRegions(false), "minT", varT, defUnitTemp);
    }
    sayOK();
  }

  @Deprecated       // v2c Unused
  public void debugImprinting_Between2ndLevels(){
    printAction("Debugging the Parts. Imprinting between 2nd Level Sub assemblies");
    Vector<CompositePart> vecCP = (Vector) getNthLevelCompositeParts(2);
    for(int i = 0; i < vecCP.size() - 1; i++){
        CompositePart cp_i = vecCP.get(i);
        for(int j = i+1; j < vecCP.size(); j++){
            Vector<MeshPart> vecMP = new Vector<MeshPart>();
            CompositePart cp_j = vecCP.get(j);
            printAction("Imprinting CAD Parts");
            vecMP.addAll(getMeshParts(cp_i.getPathInHierarchy().replace("|", ".") + ".*"));
            vecMP.addAll(getMeshParts(cp_j.getPathInHierarchy().replace("|", ".") + ".*"));
            say("Sub assembly 1: " + cp_i.getPathInHierarchy());
            say("Sub assembly 2: " + cp_j.getPathInHierarchy());
            imprintPartsByCADMethod(vecMP);
            say("\n");
        }
    }
  }

  @Deprecated       // v2c Unused
  public void debugImprinting_BetweenCompositeParts_PartByPart(CompositePart compPart1, CompositePart compPart2){
    printAction("Debugging the Parts. Imprinting between 2 Sub assemblies -- Part by Part");
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    vecMP.addAll(getMeshParts(compPart1.getPathInHierarchy().replace("|", ".") + ".*"));
    vecMP.addAll(getMeshParts(compPart2.getPathInHierarchy().replace("|", ".") + ".*"));
    for(int i = 0; i < vecMP.size() - 1; i++){
        for(int j = i+1; j < vecMP.size(); j++){
            printAction("Imprinting CAD Parts");
            say("Mesh Part 1: " + vecMP.get(i).getPathInHierarchy());
            say("Mesh Part 2: " + vecMP.get(j).getPathInHierarchy());
            Vector<MeshPart> vecImpr = new Vector<MeshPart>();
            vecImpr.add(vecMP.get(i));
            vecImpr.add(vecMP.get(j));
            imprintPartsByCADMethod(vecImpr);
            say("\n");
        }
    }
  }

  /**
   * This method will Imprint by CAD every N'th level defined. It is mainly for debugging purposes.
   *
   * @param nthLevel given N'th level for sub-assemblies imprinting.
   */
  @Deprecated       // v2c Unused
  public void debugImprinting_EveryNthLevel(int nthLevel){
    printAction("Debugging the Parts. Imprinting every N\'th Level Sub assembly");
    for (CompositePart cp : getNthLevelCompositeParts(nthLevel)) {
        printAction("Imprinting CAD Parts");
        say("Sub assembly: " + cp.getPresentationName());
        imprintPartsByCADMethod((Vector) getMeshParts(cp.getPathInHierarchy().replace("|", ".") + ".*"));
        say("\n");
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
   * Disables the Embedded Thin Mesher.
   *
   * @param continua given Mesh Continua.
   */
  public void disableEmbeddedThinMesher(MeshContinuum continua){
    printAction("Disabling Embedded Thin Mesher");
    sayContinua(continua);
    continua.disableModel(continua.getModelManager().getModel(SolidMesherSubModel.class));
    sayOK();
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of a Region.
   *
   * @param region given Region.
   */
  public void disableFeatureCurveSizeOnRegion(Region region){
    disableFeatureCurveSizeOnRegion(region, true);
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of the Regions searched by REGEX.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void disableFeatureCurveSizeOnRegions(String regexPatt){
    printAction(String.format("Disabling Feature Curves Mesh Size on all " +
                                "Regions by REGEX pattern: \"%s\"", regexPatt));
    for (Region _reg : getAllRegions(regexPatt, false)) {
        disableFeatureCurveSizeOnRegion(_reg, false);
        sayRegion(_reg, true);
    }
    sayOK();
  }

  private void disableFeatureCurveSizeOnRegion(Region _reg, boolean verboseOption){
    printAction("Disabling Custom Feature Curve Mesh Size", verboseOption);
    sayRegion(_reg, verboseOption);
    Collection<FeatureCurve> colFC = _reg.getFeatureCurveManager().getFeatureCurves();
    for (FeatureCurve fc : colFC) {
        try{
            fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        } catch (Exception e){
            say("  Error disabling " + fc.getPresentationName(), verboseOption);
        }
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Continua from a Region.
   *
   * @param region given Region.
   */
  public void disableMeshContinua(Region region) {
    disableMeshContinua(region, true);
  }

  private void disableMeshContinua(Region region, boolean verboseOption){
    printAction("Disabling Mesh Continua", verboseOption);
    sayRegion(region, verboseOption);
    try{
        sayContinua(region.getMeshContinuum(), verboseOption);
        region.getMeshContinuum().erase(region);
    } catch (Exception e){
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Per Region Flag in a Mesh Continua.
   *
   * @param continua given Mesh Continua.
   */
  public void disableMeshPerRegion(MeshContinuum continua){
    printAction("Unsetting Mesh Continua as \"Per-Region Meshing\"");
    sayContinua(continua);
    continua.setMeshRegionByRegion(false);
    sayOK();
  }

  private void disablePhysicsContinua(Region region, boolean verboseOption){
    printAction("Disabling Physics Continua", verboseOption);
    sayRegion(region, verboseOption);
    try{
        sayContinua(region.getPhysicsContinuum(), verboseOption);
        region.getPhysicsContinuum().erase(region);
    } catch (Exception e){
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disable the Prisms Layers on a given Boundary.
   *
   * @param bdry given Boundary.
   */
  public void disablePrismLayers(Boundary bdry){
    disablePrismLayers(bdry, true);
  }

  /**
   * Disable the Prisms Layers on all Boundaries that match the search criteria.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void disablePrismLayers(String regexPatt){
    printAction(String.format("Disabling Prism Layers on all Boundaries " +
                                "by REGEX pattern: \"%s\"", regexPatt));
    printLine();
    for (Boundary bdry : getAllBoundaries(regexPatt, false, false)) {
        say("");
        disablePrismLayers(bdry, true);
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
  public void disablePrismLayers(MeshContinuum continua){
    printAction("Disabling Prism Layers");
    sayContinua(continua);
    continua.disableModel(continua.getModelManager().getModel(PrismMesherModel.class));
    sayOK();
  }

  private void disablePrismLayers(Boundary bdry, boolean verboseOption){
    say("Disabling Prism Layers on Boundary...", verboseOption);
    sayBdry(bdry);
    try {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
        sayOK(verboseOption);
    } catch (Exception e1) {
        say("Warning! Could not disable Prism in Boundary.", verboseOption);
    }
  }

  public void disableRadiationS2S(Region region){
    printAction("Disabling Radiation Surface to Surface (S2S)");
    sayRegion(region);
    region.getConditions().get(RadiationTransferOption.class).setOptionEnabled(false);
    sayOK();
  }

  /**
   * Disables a Region, i.e., unsets its Physics and Mesh Continuas.
   *
   * @param region given Region.
   */
  public void disableRegion(Region region){
    printAction("Disabling a Region");
    sayRegion(region);
    disableMeshContinua(region, false);
    disablePhysicsContinua(region, false);
    sayOK();
  }

  public void disableSurfaceSizeOnBoundary(Boundary bdry){
    say("Disable Surface Mesh Size on Boundary: " + bdry.getPresentationName());
    try {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not disable as Boundary. Trying to do in the Interface Parent");
        try{
            DirectBoundaryInterfaceBoundary intrf = (DirectBoundaryInterfaceBoundary) bdry;
            DirectBoundaryInterface intrfP = intrf.getDirectBoundaryInterface();
            say("Interface: " + intrfP.getPresentationName());
            intrfP.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
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
   * @param continua given Mesh Continua.
   */
  public void disableSurfaceProximityRefinement(MeshContinuum continua){
    printAction("Disabling Surface Proximity Refinement");
    sayContinua(continua);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    sayOK();
  }

  /**
   * Disables the Surface Remesher.
   *
   * @param continua given Mesh Continua.
   */
  public void disableSurfaceRemesher(MeshContinuum continua){
    printAction("Disabling Surface Remesher");
    sayContinua(continua);
    continua.disableModel(continua.getModelManager().getModel(ResurfacerMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Remesher Automatic Repair.
   *
   * @param continua given Mesh Continua.
   */
  public void disableSurfaceRemesherAutomaticRepair(MeshContinuum continua){
    printAction("Disabling Remesher Automatic Surface Repair");
    if(!isRemesh(continua)){
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayContinua(continua);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    sayOK();
  }

  /**
   * Disables the Surface Remesher Project to CAD feature.
   *
   * @param continua given Mesh Continua.
   */
  public void disableSurfaceRemesherProjectToCAD(MeshContinuum continua){
    printAction("Disabling Project to CAD");
    sayContinua(continua);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(false);
    continua.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(false);
    sayOK();
  }

  /**
   * Disables the Surface Wrapper.
   *
   * @param continua given Mesh Continua.
   */
  public void disableSurfaceWrapper(MeshContinuum continua){
    printAction("Disabling Surface Wrapper");
    sayContinua(continua);
    continua.disableModel(continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Wrapper GAP closure.
   *
   * @param continua given Mesh Continua.
   */
  public void disableWrapperGapClosure(MeshContinuum continua){
    printAction("Disabling Wrapper Gap Closure");
    sayContinua(continua);
    continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(false);
    sayOK();
  }

  public void disableThinMesherOnRegion(Region region){
    printAction("Disabling Thin Mesher on Region");
    int opt = SolidMesherRegionOption.DISABLE;
    sayRegion(region);
    try{
        region.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(opt);
        sayOK();
    } catch (Exception e){
        say("ERROR! Moving on.\n");
    }
  }

  /**
   * Enables the Coupled Solver by changing the Segregated Solver within a Physics Continua. Enables
   * the Grid Sequencing Initialization as well.
   *
   * @param phC given Physics Continua.
   */
  public void enableCoupledSolver(PhysicsContinuum phC) {
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
    CoupledImplicitSolver coupledSlv = ((CoupledImplicitSolver) sim.getSolverManager().getSolver(CoupledImplicitSolver.class));
    say("Coupled Solver enabled...");
    say("  CFL: " + CFL);
    coupledSlv.setCFL(CFL);
    coupledSlv.getExpertInitManager().getExpertInitOption().setSelected(ExpertInitOption.GRID_SEQ_METHOD);
    coupledSlv.getAMGLinearSolver().setConvergeTol(amgConvTol);
    if (rampCFL) {
        coupledSlv.getRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
        LinearRampCalculator lrc = ((LinearRampCalculator) coupledSlv.getRampCalculatorManager().getCalculator());
        lrc.setStartIteration(cflRampBeg);
        lrc.setEndIteration(cflRampEnd);
        lrc.setInitialRampValue(cflRampBegVal);
        say("  CFL Ramp activated: " + CFL);
        say("     Start Iteration: " + cflRampBeg);
        say("     End Iteration: " + cflRampEnd);
        say("     Initial CFL : " + cflRampBegVal);
    }
    say("Enabling Grid Sequencing Initialization...");
    GridSequencingInit gridSeqInit = ((GridSequencingInit) coupledSlv.getExpertInitManager().getInit());
    say("  Max Levels: " + gsiMaxLevels);
    say("  Max Iterations: " + gsiMaxIterations);
    say("  Convergence Tolerance: " + gsiConvTol);
    say("  CFL: " + gsiCFL);
    gridSeqInit.setMaxGSLevels(gsiMaxLevels);
    gridSeqInit.setMaxGSIterations(gsiMaxIterations);
    gridSeqInit.setConvGSTol(gsiConvTol);
    gridSeqInit.setGSCfl(gsiCFL);
    sayOK();
  }

  public void enableEmbeddedThinMesher(MeshContinuum continua){
    printAction("Enabling Embedded Thin Mesher");
    sayContinua(continua);
    say("Embedded Thin Mesher overview:");
    continua.enable(SolidMesherSubModel.class);
    //continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    disableSurfaceProximityRefinement(continua);
    SolidMesherSubModel sldSubMdl = continua.getModelManager().getModel(SolidMesherSubModel.class);
    sldSubMdl.setOptimize(true);
    say("  Optimizer ON");
    if(thinMeshIsPolyType){
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

  public void enableMeshContinua(MeshContinuum continua, Region region){
    printAction("Enabling Mesh Continua");
    sayRegion(region);
    sayContinua(continua);
    continua.add(region);
    sayOK();
  }

  public void enablePhysicsContinua(PhysicsContinuum phC, Region region){
    printAction("Enabling Physics Continua");
    sayRegion(region);
    sayContinua(phC);
    phC.add(region);
    sayOK();
  }

  public void enablePrismLayers(MeshContinuum continua){
    /*
     * This method will assume Prism Layers only on Fluid Regions
     */
    printAction("Enabling Prism Layers");
    sayContinua(continua);
    printPrismsParameters();
    Collection<Interface> intrfcs = sim.getInterfaceManager().getObjects();

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
    for(Region region : getAllRegions()){
        MeshContinuum mshC = region.getMeshContinuum();
        //if(region.getMeshContinuum() == null){
        if(!region.isMeshing() || continua != mshC){
            say("  Skipping: " + region.getPresentationName());
            continue;
        }
        if(isFluid(region)){
            say("  Region ON: " + region.getPresentationName());
            region.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DEFAULT);
        } else if(isSolid(region)) {
            region.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
            say("  Region OFF: " + region.getPresentationName());
        }
    }
    say("Enabling Prisms on Fluid-Solid interfaces with same Mesh Continua");
    int n = 0;
    int k = 0;
    InterfacePrismsOption ipo = null;
    for(Interface intrf : intrfcs){
        String name = intrf.getPresentationName();
        if(isFluid(intrf.getRegion0()) && isFluid(intrf.getRegion1())){
            say("  Prism OFF: " + name + " (Fluid-Fluid Interface)");
            k++;
            continue;
        }
        if(isFluid(intrf.getRegion0()) || isFluid(intrf.getRegion1())){
            try{
                ipo = intrf.get(MeshConditionManager.class).get(InterfacePrismsOption.class);
                ipo.setPrismsEnabled(true);
                say("  Prism ON: " + name);
                n++;
            } catch (Exception e){ continue; }
        }
    }
    say("Fluid-Solid interfaces with Prisms enabled: " + n);
    say("Fluid-Fluid interfaces skipped: " + k);
    sayOK();
  }

  public void enableRadiationS2S(PhysicsContinuum phC){
    printAction("Enabling Radiation Surface to Surface (S2S)");
    sayContinua(phC);
    phC.enable(RadiationModel.class);
    phC.enable(S2sModel.class);
    phC.enable(ViewfactorsCalculatorModel.class);
    phC.enable(GrayThermalRadiationModel.class);
    GrayThermalRadiationModel gtrm = phC.getModelManager().getModel(GrayThermalRadiationModel.class);
    RadiationTemperature radT = gtrm.getThermalEnvironmentManager().get(RadiationTemperature.class);
    radT.getEnvRadTemp().setUnits(defUnitTemp);
    radT.getEnvRadTemp().setValue(Tref);
  }

  public void enableSurfaceProximityRefinement(MeshContinuum continua){
    printAction("Enabling Surface Proximity Refinement");
    sayContinua(continua);
    say("Proximity settings overview: ");
    say("  Proximity Number of Points in Gap: " + mshProximityPointsInGap);
    say("  Proximity Search Floor (mm): " + mshProximitySearchFloor);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(true);
    SurfaceProximity sp = continua.getReferenceValues().get(SurfaceProximity.class);
    sp.setNumPointsInGap(mshProximityPointsInGap);
    sp.getFloor().setUnits(defUnitLength);
    sp.getFloor().setValue(mshProximitySearchFloor);
    sayOK();
  }

  public void enableSurfaceRemesher(MeshContinuum continua){
    printAction("Enabling Surface Remesher");
    sayContinua(continua);
    continua.enable(ResurfacerMeshingModel.class);
    sayOK();
  }

  public void enableSurfaceRemesherAutomaticRepair(MeshContinuum continua){
    printAction("Enabling Remesher Automatic Surface Repair");
    if(!isRemesh(continua)){
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayContinua(continua);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(true);
    sayOK();
  }

  public void enableSurfaceRemesherProjectToCAD(MeshContinuum continua){
    printAction("Enabling Project to CAD");
    sayContinua(continua);
    continua.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(true);
    continua.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(true);
    sayOK();
  }

  public void enableSurfaceWrapper(MeshContinuum continua){
    printAction("Enabling Surface Wrapper");
    sayContinua(continua);
    say("Surface Wrapper settings overview: ");
    say("  Geometric Feature Angle (deg): " + mshWrapperFeatureAngle);
    say("  Wrapper Scale Factor (%): " + mshWrapperScaleFactor);
    continua.enable(SurfaceWrapperMeshingModel.class);
    setMeshWrapperFeatureAngle(continua, mshWrapperFeatureAngle);
    setMeshWrapperScaleFactor(continua, mshWrapperScaleFactor);
    sayOK();
  }

  public void enableWrapperGapClosure(MeshContinuum continua, double gapSize){
    printAction("Enabling Wrapper Gap Closure");
    sayContinua(continua);
    say("  Gap Closure Size (%): " + gapSize);
    continua.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(true);
    continua.getReferenceValues().get(GapClosureSize.class).getRelativeSize().setPercentage(gapSize);
    sayOK();
  }

  public void exportMeshedRegionsToDBS(String subDirName) {
    printAction("Exporting Meshed Regions to DBS files");
    say("Querying Surface Mesh Representation");
    if(hasValidSurfaceMesh()){
        say("Remeshed Regions found. Using this one.");
        exportRemeshedRegionsToDBS(subDirName);
    } else if(hasValidWrappedMesh()){
        say("Wrapped Regions found. Using this one.");
        exportWrappedRegionsToDBS(subDirName);
    } else {
        say("No Valid Mesh representation found. Skipping.");
    }
    sayOK();
  }

  public void exportRemeshedRegionsToDBS(String subDirName) {
    printAction("Exporting All Remeshed Regions to DBS files");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("To Path: " + dbsSubPath.toString());
    if (!dbsPath.exists()) { dbsPath.mkdirs(); }
    if (!dbsSubPath.exists()) { dbsSubPath.mkdirs(); }
    for(SurfaceRepRegion srfPart : getAllRemeshedRegions()) {
        String name = srfPart.getPresentationName();
        name = name.replace(" ", "_");
        name = name.replace(".", "+");
        File fPath = new File(dbsSubPath, name + ".dbs");
        sim.println("Writing: " + fPath);
        srfPart.exportDbsRegion(fPath.toString(), 1, "");
    }
  }

  public void exportWrappedRegionsToDBS(String subDirName) {
    printAction("Exporting All Wrapped Regions to DBS files");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("To Path: " + dbsSubPath.toString());
    if (!dbsPath.exists()) { dbsPath.mkdirs(); }
    if (!dbsSubPath.exists()) { dbsSubPath.mkdirs(); }
    for(SurfaceRepRegion srfPart : getAllWrappedRegions()) {
        String name = srfPart.getPresentationName();
        name = name.replace(" ", "_");
        name = name.replace(".", "+");
        File fPath = new File(dbsSubPath, name + ".dbs");
        sim.println("Writing: " + fPath);
        srfPart.exportDbsRegion(fPath.toString(), 1, "");
    }
  }

  /**
   * Evaluates a simple Linear Regression equation in the form: <b>y = a * x + b<b>.
   * @param xx given independent variable interval. E.g.: { x0, x1 }.
   * @param yy given dependent variable interval. E.g.: { y0, y1 }.
   * @param x given independent variable value for y(x) to be evaluated.
   * @return Value at y(x). Results are clipped: y0 &lt= y(x) &lt= y1.
   */
  public double evalLinearRegression(double[] xx, double[] yy, double x) {
    return evalLinearRegression(xx, yy, x, true);
  }

  private double evalLinearRegression(double[] xx, double[] yy, double x, boolean verboseOption) {
    printAction("Evaluating a Simple Regression: y = a * x + b", verboseOption);
    double a, b, y;
    a = (yy[1] - yy[0]) / (xx[1] - xx[0]);
    b = yy[1] - a * xx[1];
    y = a * x + b;
    say(String.format("xx = {%g, %g}; yy = {%g, %g}", xx[0], xx[1], yy[0], yy[1]), verboseOption);
    say("a = " + a, verboseOption);
    say("b = " + b, verboseOption);
    say(String.format("y(%g) = %g", x, y), verboseOption);
    if (x <= xx[0]) {
        say(String.format("  x <= x0. Clipped to y(%g) = %g", xx[0], yy[0]), verboseOption);
        y = yy[0];
    }
    if (x >= xx[1]) {
        say(String.format("  x >= x1. Clipped to y(%g) = %g", xx[1], yy[1]), verboseOption);
        y = yy[1];
    }
    sayOK(verboseOption);
    return y;
  }

  public void findAllPartsContacts(double tol_meters){
    printAction("Finding All Part Contacts");
    say("Tolerance (m): " + tol_meters);
    queryGeometryRepresentation().findPartPartContacts((Vector) getAllLeafPartsAsMeshParts(), tol_meters);
    sayOK();
  }

  /**
   * Freezes/Unfreezes the K-Epsilon Turbulent 2-equation Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeKeTurbSolver(boolean freezeOption){
    freezeSolver(KeTurbSolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the K-Epsilon Turbulent Viscosity Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeKeTurbViscositySolver(boolean freezeOption){
    freezeSolver(KeTurbViscositySolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Surface to Surface (S2S) Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeS2SSolver(boolean freezeOption){
    freezeSolver(S2sSolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Segregated Energy Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeSegregatedEnergySolver(boolean freezeOption){
    freezeSolver(SegregatedEnergySolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Segregated Flow Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeSegregatedFlowSolver(boolean freezeOption){
    freezeSolver(SegregatedFlowSolver.class, freezeOption);
  }

  /**
   * Freeze/Unfreeze the View Factors Solver. Updates the View Factors upon unfreezing.
   *
   * @param freezeOption given option.
   */
  public void freezeViewfactorsCalculatorSolver(boolean freezeOption){
    Solver solver = freezeSolver(ViewfactorsCalculatorSolver.class, freezeOption);
    if(!freezeOption){
        ((ViewfactorsCalculatorSolver) solver).calculateViewfactors();
    }
  }

  /**
   * Freeze/Unfreeze the Wall Distance Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeWallDistanceSolver(boolean freezeOption){
    freezeSolver(WallDistanceSolver.class, freezeOption);
  }

  private Solver freezeSolver(Class solverClass, boolean freezeOption){
    Solver solver = sim.getSolverManager().getSolver(solverClass);
    String msg = "Freezing ";
    if(!freezeOption){ msg = msg.replace("Free", "Unfree"); }
    printAction(msg + solver);
    solver.setFrozen(freezeOption);
    sayOK();
    return solver;
  }

  /**
   * Generates the Surface Mesh.
   */
  public void genSurfaceMesh() {
    if(skipMeshGeneration){ return; }
    printAction("Generating Surface Mesh");
    sim.get(MeshPipelineController.class).generateSurfaceMesh();
    if(createMeshSceneUponSurfaceMeshGeneration){
        createScene_Mesh().setPresentationName("Surface Mesh");
    }
    if(checkMeshQualityUponSurfaceMeshGeneration){
        // Checking Mesh Quality right after generated
        checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions();
    }
  }

  /**
   * Generates the Volume Mesh.
   */
  public void genVolumeMesh() {
    if(skipMeshGeneration){ return; }
    printAction("Generating Volume Mesh");
    sim.get(MeshPipelineController.class).generateVolumeMesh();
  }

  /**
   * Get all Boundaries from all Regions.
   *
   * @return All Boundaries.
   */
  public Collection<Boundary> getAllBoundaries() {
    return getAllBoundaries(true);
  }

  /**
   * Get all Boundaries from all Regions based on REGEX.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found.
   */
  public Collection<Boundary> getAllBoundaries(String regexPatt, boolean skipInterfaces) {
    return getAllBoundaries(regexPatt, true, skipInterfaces);
  }

  /**
   * Get all Boundaries from all Regions based on an Array of REGEX Patterns.
   *
   * @param regexPattArray given array of REGEX search patterns.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found.
   */
  public Collection<Boundary> getAllBoundaries(String[] regexPattArray, boolean skipInterfaces) {
    return getAllBoundaries(regexPattArray, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundaries(boolean verboseOption) {
    say("Getting all Boundaries from all Regions...", verboseOption);
    Vector<Boundary> allBdrys = new Vector<Boundary>();
    for (Region region : getAllRegions(false)) {
        Collection<Boundary> bdrys = region.getBoundaryManager().getBoundaries();
        for (Boundary bdry : bdrys) {
            allBdrys.add(bdry);
        }
    }
    say("Boundaries found: " + allBdrys.size(), verboseOption);
    return ((Collection<Boundary>) allBdrys);
  }


  private Collection<Boundary> getAllBoundaries(Region region, String regexPatt,
                                    boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Region by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    sayRegion(region, verboseOption);
    Vector<Boundary> chosenBdrys = (Vector<Boundary>) getAllBoundariesFromRegion(region, false, skipInterfaces);
    Vector<Boundary> retBdrys = (Vector<Boundary>) chosenBdrys.clone();
    for (Boundary bdry : chosenBdrys) {
        if (bdry.getRegion() != region) {
            retBdrys.remove(bdry);
        }
    }
    say("Boundaries found by REGEX: " + retBdrys.size(), verboseOption);
    return (Collection<Boundary>) retBdrys;
  }

  private Collection<Boundary> getAllBoundaries(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from all Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> chosenBdrys = new Vector<Boundary>();
    for (Boundary bdry : getAllBoundaries(false)) {
        if (isInterface(bdry)) { continue; }
        if (bdry.getPresentationName().matches(regexPatt)) {
            chosenBdrys.add(bdry);
        }
    }
    say("Boundaries found by REGEX: " + chosenBdrys.size(), verboseOption);
    return (Collection<Boundary>) chosenBdrys;
  }

  private Collection<Boundary> getAllBoundaries(String[] regexPattArray, boolean verboseOption,
                                                                            boolean skipInterfaces) {
    return getAllBoundaries(null, regexPattArray, verboseOption, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundaries(Region region, String[] regexPattArray,
                                                    boolean verboseOption, boolean skipInterfaces) {
    if (region != null){
        say("Getting all Boundaries from all Regions by an Array of REGEX pattern", verboseOption);
    } else {
        say("Getting all Boundaries from a Region by an Array of REGEX pattern", verboseOption);
        sayRegion(region, verboseOption);
    }
    Vector<Boundary> allBdrys = new Vector<Boundary>();
    for (int i = 0; i < regexPattArray.length; i++) {
        Vector<Boundary> chosenBdrys = new Vector<Boundary>();
        for (Boundary bdry : getAllBoundaries(regexPattArray[i], false, skipInterfaces)){
            if (region != null && bdry.getRegion() != region) { continue; }
            chosenBdrys.add(bdry);
        }
        say(String.format("Boundaries found by REGEX pattern: \"%s\" == %d", regexPattArray[i],
                chosenBdrys.size()), verboseOption);
        allBdrys.addAll(chosenBdrys);
        chosenBdrys.clear();
    }
    say("Overal Boundaries found by REGEX: " + allBdrys.size(), verboseOption);
    return (Collection<Boundary>) allBdrys;
  }

  /**
   * Get all boundaries from a Region.
   *
   * @param region given Region.
   * @return All Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegion(Region region) {
    return getAllBoundariesFromRegion(region, true, false);
  }

  /**
   * Get all boundaries from a Region. Option to skip Interface Boundaries.
   *
   * @param region given Region.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegion(Region region, boolean skipInterfaces) {
    return getAllBoundariesFromRegion(region, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundariesFromRegion(Region region, boolean verboseOption, boolean skipInterfaces) {
    Collection<Boundary> colBdry = region.getBoundaryManager().getBoundaries();
    Vector<Boundary> bdriesNotInterface = new Vector<Boundary>();
    for (Boundary bdry : colBdry) {
        if (isInterface(bdry)) { continue; }
        bdriesNotInterface.add(bdry);
    }
    if (skipInterfaces) {
        say("Getting all Boundaries but Skip Interfaces from Region: " + region.getPresentationName(), verboseOption);
        say("Non-Interface Boundaries found: " + bdriesNotInterface.size(), verboseOption);
        return (Collection<Boundary>) bdriesNotInterface;
    } else {
        say("Getting all Boundaries from Region: " + region.getPresentationName(), verboseOption);
        say("Boundaries found: " + colBdry.size(), verboseOption);
        return colBdry;
    }
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, false);
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean skipInterfaces) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Regions searched by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> allBdrysFromRegions = new Vector<Boundary>();
    for (Region reg : getAllRegions(regexPatt, false)) {
        allBdrysFromRegions.addAll(getAllBoundariesFromRegion(reg, false, skipInterfaces));
    }
    say("Boundaries found by REGEX: " + allBdrysFromRegions.size(), verboseOption);
    return (Collection<Boundary>) allBdrysFromRegions;
  }

  /**
   * Get all Composite Parts.
   *
   * @return All Composite Parts.
   */
  public Collection<CompositePart> getAllCompositeParts() {
    return getAllCompositeParts(true);
  }

  /**
   * Get all Composite Parts base on REGEX criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Matched Composite Parts.
   */
  public Collection<CompositePart> getAllCompositeParts(String regexPatt) {
    return getAllCompositeParts(regexPatt, true);
  }

  private Collection<CompositePart> getAllCompositeParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Composite Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<CompositePart> vecCP = new Vector<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (cp.getPresentationName().matches(regexPatt)) {
            say("  Found: " + cp.getPresentationName(), verboseOption);
            vecCP.add(cp);
        }
    }
    say("Composite Parts found by REGEX: " + vecReg.size(), verboseOption);
    return vecCP;
  }

  private Collection<CompositePart> getAllCompositeParts(boolean verboseOption) {
    say("Getting all Composite Parts...", verboseOption);
    Vector<CompositePart> compPrtCol = new Vector<CompositePart>();
    for (GeometryPart gp : getAllGeometryParts(false)) {
        if (!isCompositePart(gp)) { continue; }
        Vector<CompositePart> vecCompPart = new Vector<CompositePart>();
        for (CompositePart cp : getCompositeChildren((CompositePart) gp, vecCompPart, false)) {
            say("  Composite Part Found: " + cp.getPresentationName(), verboseOption);
        }
        compPrtCol.addAll(vecCompPart);
    }
    say("Composite Parts found: " + compPrtCol.size(), verboseOption);
    return compPrtCol;
  }

  /**
   * Get all Feature Curves from all Regions.
   *
   * @return All Feature Curves.
   */
  public Collection<FeatureCurve> getAllFeatureCurves(){
    return getAllFeatureCurves(true);
  }

  private Collection<FeatureCurve> getAllFeatureCurves(boolean verboseOption){
    say("Getting all Feature Curves...", verboseOption);
    Vector<FeatureCurve> vecFC = new Vector<FeatureCurve>();
    for (Region region : getAllRegions(verboseOption)) {
        vecFC.addAll(getFeatureCurves(region, false));
    }
    say("All Feature Curves: " + vecFC.size(), verboseOption);
    return vecFC;
  }

  /**
   * Get all Geometry Parts.
   *
   * @return All Geometry Parts.
   */
  public Collection<GeometryPart> getAllGeometryParts(){
    return getAllGeometryParts(true);
  }

  /**
   * Get all Geometry Parts based on REGEX.
   *
   * @param regexPatt REGEX search pattern.
   * @return All Geometry Parts found.
   */
  public Collection<GeometryPart> getAllGeometryParts(String regexPatt){
    return getAllGeometryParts(regexPatt, true);
  }

  private Collection<GeometryPart> getAllGeometryParts(boolean verboseOption){
    say("Getting all Geometry Parts...", verboseOption);
    Collection<GeometryPart> colGP = sim.get(SimulationPartManager.class).getParts();
    say("Geometry Parts found: " + colGP.size(), verboseOption);
    return colGP;
  }

  private Collection<GeometryPart> getAllGeometryParts(String regexPatt, boolean verboseOption){
    say(String.format("Getting all Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<GeometryPart> gpVec = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllGeometryParts(false)){
        if(gp.getPresentationName().matches(regexPatt)){
            say("  Found: " + gp.getPresentationName(), verboseOption);
            gpVec.add(gp);
        }
    }
    say("Geometry Parts found by REGEX: " + gpVec.size(), verboseOption);
    return gpVec;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public Collection<Interface> getAllInterfaces(){
    return getAllInterfaces(true);
  }

  /**
   * Get all Interfaces between 2 Regions.
   *
   * @param region0 given first Region.
   * @param region1 given second Region.
   * @return Collection of Interfaces shared between both Regions.
   */
  public Collection<Interface> getAllInterfaces(Region region0, Region region1){
    Vector<Interface> intrfVec = new Vector<Interface>();
    say("Getting all Interfacess between 2 Regions...");
    sayRegion(region0);
    sayRegion(region1);
    Integer r0 = region0.getIndex();
    Integer r1 = region1.getIndex();
    for (Interface intrfPair : getAllInterfaces(false)) {
        Integer n0 = intrfPair.getRegion0().getIndex();
        Integer n1 = intrfPair.getRegion1().getIndex();
        if (Math.min(r0,r1) == Math.min(n0,n1) && Math.max(r0,r1) == Math.max(n0,n1)) {
            intrfVec.add(intrfPair);
            say("  Interface found: " + intrfPair);
        }
    }
    say("Interfaces found: " + intrfVec.size());
    return intrfVec;
  }

  private Collection<Interface> getAllInterfaces(boolean verboseOption){
    say("Getting all Interfaces...", verboseOption);
    Collection<Interface> colIntrfcs = sim.get(InterfaceManager.class).getObjects();
    say("Interfaces found: " + colIntrfcs.size(), verboseOption);
    return colIntrfcs;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public Collection<Object> getAllInterfacesAsObjects(){
    return getAllInterfacesAsObjects(true);
  }

  private Collection<Object> getAllInterfacesAsObjects(boolean verboseOption){
    say("Getting all Interfaces...", verboseOption);
    Collection<Object> colIntrfcs = sim.getInterfaceManager().getChildren();
    say("Interfaces found: " + colIntrfcs.size(), verboseOption);
    return colIntrfcs;
  }

  /**
   * Get all Leaf Mesh Parts in the model.
   *
   * @return Collection of Leaf Mesh Parts.
   */
  public Collection<LeafMeshPart> getAllLeafMeshParts(){
    return getAllLeafMeshParts(true);
  }

  /**
   * Get all Leaf Mesh Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Leaf Mesh Parts.
   */
  public Collection<LeafMeshPart> getAllLeafMeshParts(String regexPatt) {
    return getAllLeafMeshParts(regexPatt, true);
  }

  private Collection<LeafMeshPart> getAllLeafMeshParts(boolean verboseOption){
    say("Getting all Leaf Mesh Parts...", verboseOption);
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    for(GeometryPart gp : colLP){
        if (isLeafMeshPart(gp)) {
            say("Found: " + gp.getPresentationName(), verboseOption);
            lmpVec.add((LeafMeshPart) gp);
        }
    }
    say("Leaf Mesh Parts found: " + lmpVec.size(), verboseOption);
    return lmpVec;
  }

  private Collection<LeafMeshPart> getAllLeafMeshParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
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
   * @return Collection of Geometry Parts.
   */
  public Collection<GeometryPart> getAllLeafParts(){
    return getAllLeafParts(true);
  }

  /**
   * Get all Leaf Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Geometry Parts.
   */
  public Collection<GeometryPart> getAllLeafParts(String regexPatt){
    return getAllLeafParts(regexPatt, true);
  }

  private Collection<GeometryPart> getAllLeafParts(boolean verboseOption){
    say("Getting all Leaf Parts...", verboseOption);
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    say("Leaf Parts found: " + colLP.size(), verboseOption);
    return colLP;
  }

  private Collection<GeometryPart> getAllLeafParts(String regexPatt, boolean verboseOption){
    say(String.format("Getting all Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        if(gp.getPathInHierarchy().matches(regexPatt)){
            say("  Found: " + gp.getPathInHierarchy(), verboseOption);
            vecGP.add(gp);
        }
    }
    say("Leaf Parts found by REGEX: " + vecGP.size(), verboseOption);
    return vecGP;
  }

  private Collection<CadPart> getAllLeafPartsAsCadParts(){
    Vector<CadPart> vecCP = new Vector<CadPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        vecCP.add((CadPart) gp);
    }
    return vecCP;
  }

  private Collection<MeshPart> getAllLeafPartsAsMeshParts(){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        vecMP.add((MeshPart) gp);
    }
    return vecMP;
  }

  /**
   * Get all Part Surfaces from the model.
   *
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(){
    return getAllPartSurfaces(".*", true);
  }

  /**
   * Get all Part Surfaces from a Geometry Part.
   *
   * @param gp given Geometry Part.
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(GeometryPart gp){
    return gp.getPartSurfaces();
  }

  /**
   * Get all Part Surfaces from a Geometry Part by REGEX search.
   *
   * @param lmp given Geometry Part.
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(GeometryPart gp, String regexPatt){
    return getAllPartSurfaces(gp, regexPatt, false);
  }

  /**
   * Get all Part Surfaces from a Leaf Mesh Part.
   *
   * @param lmp given Leaf Mesh Part.
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(LeafMeshPart lmp){
    return lmp.getPartSurfaces();
  }

  /**
   * Get all Part Surfaces that matches the REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(String regexPatt){
    return getAllPartSurfaces(regexPatt, true);
  }

  private Collection<PartSurface> getAllPartSurfaces(String regexPatt, boolean verboseOption){
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    if (regexPatt.equals(".*")) {
        say("Getting all Part Surfaces from All Leaf Parts", verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    }
    for (GeometryPart gp : getAllLeafParts(false)) {
        psVec.addAll(getAllPartSurfaces(gp, regexPatt, false));
    }
    say("Total Part Surfaces found: " + psVec.size(), verboseOption);
    if (!regexPatt.matches(".*")) {
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }

  private Collection<PartSurface> getAllPartSurfaces(GeometryPart gp, String regexPatt, boolean verboseOption){
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    sayPart(gp, verboseOption);
    if (regexPatt.equals(".*")) {
        say("Getting all Part Surfaces from Part...", verboseOption);
        psVec.addAll(gp.getPartSurfaces());
        say("Part Surfaces found: " + psVec.size(), verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
        for(PartSurface ps : gp.getPartSurfaces()){
            if(ps.getPresentationName().matches(regexPatt)){
                say("  Found: " + ps.getPresentationName(), verboseOption);
                psVec.add(ps);
            }
        }
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }

  /**
   * Get all Mesh Continuas.
   *
   * @return All Mesh Continuas.
   */
  public Collection<MeshContinuum> getAllMeshContinuas(){
    return getAllMeshContinuas(true);
  }

  private Collection<MeshContinuum> getAllMeshContinuas(boolean verboseOption) {
    say("Getting all Mesh Continuas...", verboseOption);
    Vector<MeshContinuum> vecMC = new Vector<MeshContinuum>();
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if(cont.getBeanDisplayName().equals("MeshContinum")) {
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
  public Collection<PhysicsContinuum> getAllPhysicsContinuas() {
    return getAllPhysicsContinuas(true);
  }

  private Collection<PhysicsContinuum> getAllPhysicsContinuas(boolean verboseOption){
    say("Getting all Physics Continuas...", verboseOption);
    Vector<PhysicsContinuum> vecPC = new Vector<PhysicsContinuum>();
    for(Continuum cont : sim.getContinuumManager().getObjects()){
        if(cont.getBeanDisplayName().equals("PhysicsContinum")){
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
  public Collection<Region> getAllRegions() {
    return getAllRegions(true);
  }

  /**
   * Get all Regions searched by REGEX pattern.
   *
   * @param regexPatt search REGEX pattern.
   * @return Found regions.
   */
  public Collection<Region> getAllRegions(String regexPatt) {
    return getAllRegions(regexPatt, true);
  }

  private Collection<Region> getAllRegions(boolean verboseOption) {
    say("Getting all Regions...", verboseOption);
    Collection<Region> colReg = sim.getRegionManager().getRegions();
    say("All Regions: " + colReg.size(), verboseOption);
    return colReg;
  }

  private Collection<Region> getAllRegions(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Region> vecReg = new Vector<Region>();
    for (Region reg : getAllRegions(false)) {
        if (reg.getPresentationName().matches(regexPatt)) {
            say("  Found: " + reg.getPresentationName(), verboseOption);
            vecReg.add(reg);
        }
    }
    say("Regions found by REGEX: " + vecReg.size(), verboseOption);
    return vecReg;
  }

  /**
   * Get all Regions that has a Remeshed Surface Representation.
   *
   * @return All Remeshed Regions.
   */
  public Collection<SurfaceRepRegion> getAllRemeshedRegions(){
    return getAllRemeshedRegions(true);
  }

  private Collection<SurfaceRepRegion> getAllRemeshedRegions(boolean verboseOption){
    say("Getting all Remeshed Regions...", verboseOption);
    SurfaceRep srfRep = queryRemeshedSurface();
    Collection<SurfaceRepRegion> colRemshReg = srfRep.getSurfaceRepRegionManager().getObjects();
    say("All Remeshed Regions: " + colRemshReg.size(), verboseOption);
    return colRemshReg;
  }

  /**
   * Get all Regions that has a Wrapped Surface Representation.
   *
   * @return All Wrapped Regions.
   */
  public Collection<SurfaceRepRegion> getAllWrappedRegions(){
    return getAllWrappedRegions(true);
  }

  private Collection<SurfaceRepRegion> getAllWrappedRegions(boolean verboseOption){
    say("Getting all Wrapped Regions...", verboseOption);
    SurfaceRep srfRep = queryWrappedSurface();
    Collection<SurfaceRepRegion> colWrappedReg = srfRep.getSurfaceRepRegionManager().getObjects();
    say("All Wrapped Regions: " + colWrappedReg.size(), verboseOption);
    return colWrappedReg;
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
   * Loop in all Boundaries and returns the first match given the REGEX pattern. <b>Note:</b> It
   * will skip Interface Boundaries.
   *
   * @param regexPatt REGEX search pattern.
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt){
    return getBoundary(regexPatt, true, true);
  }

  /**
   * Loop in all Boundaries and returns the first match given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt, boolean skipInterfaces){
    return getBoundary(regexPatt, true, skipInterfaces);
  }

  private Boundary getBoundary(String regexPatt, boolean verboseOption, boolean skipInterfaces){
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(regexPatt, false, skipInterfaces);
    if (vecBdry.size() > 0) {
        say("Got by REGEX: " + vecBdry.firstElement().getPresentationName(), verboseOption);
        return vecBdry.firstElement();
    } else {
        say("Got NULL.\n");
        return null;
    }
  }

  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   * <b>Note:</b> It will skip Interface Boundaries.
   *
   * @param region given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region region, String regexPatt){
    return getBoundary(region, regexPatt, true, true);
  }

  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   *
   * @param region given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region region, String regexPatt, boolean skipInterfaces){
    return getBoundary(region, regexPatt, true, skipInterfaces);
  }

  private Boundary getBoundary(Region region, String regexPatt, boolean verboseOption, boolean skipInterfaces){
    sayRegion(region);
    try {
        Boundary foundBdry = region.getBoundaryManager().getBoundary(regexPatt);
        if (foundBdry != null) {
            say("Found: " + foundBdry.getPresentationName(), verboseOption);
            return foundBdry;
        }
    } catch (Exception e) { }
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(region, regexPatt, false, skipInterfaces);
    return getBoundary(vecBdry, regexPatt, verboseOption, skipInterfaces);
  }

  private Boundary getBoundary(Vector<Boundary> vecBdry, String regexPatt, boolean verboseOption,
                                                                            boolean skipInterfaces){
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    if (vecBdry.size() > 0) {
        say("Got by REGEX: " + vecBdry.firstElement().getPresentationName(), verboseOption);
        return vecBdry.firstElement();
    } else {
        say("Got NULL.", verboseOption);
        return null;
    }
  }

  /**
   * Get a single CAD Part.
   *
   * @param name given name of the CAD Part.
   * @return CAD Part.
   */
  public CadPart getCadPart(String name){
    return getCadPart(name, true);
  }

  private CadPart getCadPart(String name, boolean verboseOption){
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
    VisView vv = getCameraViews(regexPatt, verboseOption).iterator().next();
    say("Got by REGEX: " + vv.getPresentationName(), verboseOption);
    return vv;
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
    //-- Fmt is Name|FocalPointVector|PositionVector|ViewUpVector|ParallelScale
    printAction("Dumping the Cameras Overview", verboseOption);
    Collection<VisView> colVV = sim.getViewManager().getObjects();
    say("Cameras Found: " + colVV.size(), verboseOption);
    say("Camera Format: " + camFormat, verboseOption);
    for (VisView vv : colVV) {
        String name = vv.getPresentationName();
        DoubleVector fp = vv.getFocalPoint();
        DoubleVector pos = vv.getPosition();
        DoubleVector vu = vv.getViewUp();
        double ps = vv.getParallelScale();
        String cam = String.format(camFormat, name, fp.get(0), fp.get(1), fp.get(2),
                                                pos.get(0), pos.get(1), pos.get(2),
                                                vu.get(0), vu.get(1), vu.get(2), ps);
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
   * @return A Collection of Camera Views.
   */
  public Collection<VisView> getCameraViews(String regexPatt) {
    return getCameraViews(regexPatt, true);
  }

  private Collection<VisView> getCameraViews(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Cameras by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<VisView> vecVV = new Vector<VisView>();
    for (VisView vv : sim.getViewManager().getObjects()) {
        if (vv.getPresentationName().matches(regexPatt)) {
            say("Got: " + vv.getPresentationName(), verboseOption);
            vecVV.add(vv);
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
  public Vector<VisView> getCameraViews(VisView cam1, VisView cam2, int nSteps) {
    return getCameraViews(cam1, cam2, nSteps, true);
  }

  /**
   * Performs a spline interpolation between the given cameras and generate the intermediate views.
   *
   * @param vecCam given Vector of Cameras.
   * @param nSteps given number of wanted Cameras in between.
   * @return The ordered Vector of the transition Cameras. Vector size is {@param nSteps} + 1.
   */
  public Vector<VisView> getCameraViews(Vector<VisView> vecCam, int nSteps) {
    printAction("Interpolating Camera Views");
    say("Given Cameras: " + vecCam.size());
    if (vecCam.size() == 2) {
        say("Calling Linear Interpolator...");
        return getCameraViews(vecCam.get(0), vecCam.get(1), nSteps, true);
    }
    say("Calling Spline Interpolator...");
    //--
    Vector<VisView> vecAll = new Vector<VisView>();
    Vector<Double> _x = new Vector<Double>(), _f = new Vector<Double>();
    DoubleVector dvFP = vecCam.get(0).getFocalPoint();
    DoubleVector dvPos = vecCam.get(0).getPosition();
    DoubleVector dvVU = vecCam.get(0).getViewUp();
    //--
    int n_delta = nSteps / (vecCam.size() - 1);
    //--
    for (int k = 0; k <= nSteps; k++) {
    //-- Create Temporary Cameras
        VisView vv = sim.getViewManager().createView();
        vv.copyProperties(vecCam.get(0));
        vv.setPresentationName(String.format("%s_Spline_%dcams_%04d", tempCamName,
                                                            vecCam.size(), k));
        say("Generating: " + vv.getPresentationName());
        for (int j = 0; j < 3; j++) {
            //--
            say("  Processing Focal Point ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < vecCam.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(vecCam.elementAt(i).getFocalPoint().get(j));
            }
            double[][] splFPs = retSpline(_x, _f);
            dvFP.setElementAt(retSplineValue(splFPs, k), j);
            //--
            say("  Processing Position ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < vecCam.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(vecCam.elementAt(i).getPosition().get(j));
            }
            double[][] splPos = retSpline(_x, _f);
            dvPos.setElementAt(retSplineValue(splPos, k), j);
            //--
            say("  Processing View Up ID " + j);
            _x.clear();
            _f.clear();
            for (int i = 0; i < vecCam.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(vecCam.elementAt(i).getViewUp().get(j));
            }
            double[][] splVUs = retSpline(_x, _f);
            dvVU.setElementAt(retSplineValue(splVUs, k), j);
        }
        //--
        say("  Processing Parallel Scale");
        _x.clear();
        _f.clear();
        for (int i = 0; i < vecCam.size(); i++) {
            _x.add((double) i * n_delta);
            _f.add(vecCam.elementAt(i).getParallelScale());
        }
        double[][] splPS = retSpline(_x, _f);
        double newPS = retSplineValue(splPS, k);
        vv.setFocalPoint(dvFP);
        vv.setPosition(dvPos);
        vv.setViewUp(dvVU);
        vv.setParallelScale(newPS);
        vecAll.add(vv);
    }
    say("Cameras processed: " + vecAll.size());
    sayOK();
    return vecAll;
  }

  private Vector<VisView> getCameraViews(VisView cam1, VisView cam2, int nSteps, boolean verboseOption) {
    printAction("Linear Interpolation between 2 Camera Views", verboseOption);
    say("Camera 1: " + cam1.getPresentationName(), verboseOption);
    say("Camera 2: " + cam2.getPresentationName(), verboseOption);
    say("Number of Steps: " + nSteps, verboseOption);
    Vector<VisView> vecVV = new Vector<VisView>();
    if (nSteps < 2) { nSteps = 2; }
    DoubleVector dv = cam1.getFocalPoint();
    for (int i = 1; i <= nSteps; i++) {
        VisView vv = sim.getViewManager().createView();
        vv.copyProperties(cam1);
        vv.setPresentationName(String.format("%s_%s_%s_%d_%04d", tempCamName,
                        cam1.getPresentationName(), cam2.getPresentationName(), nSteps, i));
        say("Generating: " + vv.getPresentationName(), verboseOption);
        dv = retIncrement(cam1.getFocalPoint(), cam2.getFocalPoint(), i, nSteps);
        vv.setFocalPoint(dv);
        dv = retIncrement(cam1.getPosition(), cam2.getPosition(), i, nSteps);
        vv.setPosition(dv);
        dv = retIncrement(cam1.getViewUp(), cam2.getViewUp(), i, nSteps);
        vv.setViewUp(dv);
        vv.setParallelScale(retIncrement(cam1.getParallelScale(), cam2.getParallelScale(), i, nSteps));
        vecVV.add(vv);
    }
    vecVV.insertElementAt(cam1, 0);
    vecVV.add(cam2);
    say("Returning " + vecVV.size() + " Camera Views.", verboseOption);
    sayOK(verboseOption);
    return vecVV;
  }

  /**
   * Get all the children of a Composite Part.
   *
   * @param compPrt given Composite Part.
   * @param vecCP given Vector of Composite Part.
   * @return Vector of the children Composite Parts.
   */
  public Vector<CompositePart> getCompositeChildren(CompositePart compPrt, Vector<CompositePart> vecCP){
    return getCompositeChildren(compPrt, vecCP, true);
  }

  private Vector<CompositePart> getCompositeChildren(CompositePart compPrt, Vector<CompositePart> vecCP, boolean verboseOption){
    for(GeometryPart gp : compPrt.getChildParts().getParts()){
        if(!isCompositePart(gp)) { continue; }
        say("Child Part: " + ((CompositePart) gp).getPathInHierarchy(), verboseOption);
        vecCP.add((CompositePart) gp);
        getCompositeChildren((CompositePart) gp, vecCP, verboseOption);
    }
    return vecCP;
  }

  /**
   * Loop over all Composite Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Composite Part.
   */
  public CompositePart getCompositePart(String regexPatt){
    return getCompositePart(regexPatt, true);
  }

  private CompositePart getCompositePart(String regexPatt, boolean verboseOption){
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
   * @param compPart given Composite Part.
   * @return Level of hierarchy. (E.g.: Master assembly is level 1).
   */
  public int getCompositePartLevel(CompositePart compPart){
    say("Composite Part: " + compPart.getPathInHierarchy());
    int level = getCompositePartParentLevel(compPart, 1, true);
    say(compPart.getPresentationName() + " is level " + level + ".");
    return level;
  }

  private int getCompositePartParentLevel(CompositePart compPart, int level, boolean verboseOption){
    try{
        CompositePart parent = (CompositePart) compPart.getParentPart();
        say("  Level " + level + ". Parent: " + parent.getPresentationName(), verboseOption);
        level++;
        level = getCompositePartParentLevel(parent, level, verboseOption);
    } catch (Exception e) { }
    return level;
  }

  public void getDependentInterfaceAndEraseIt(Boundary bdry){
    Interface intrf = (DirectBoundaryInterface) bdry.getDependentInterfaces().firstElement();
    printAction("Erasing an Interface");
    say("Name: " + intrf.getPresentationName());
    say("Between Regions: ");
    say("  " + intrf.getRegion0().getPresentationName());
    say("  " + intrf.getRegion1().getPresentationName());
    sim.getInterfaceManager().deleteInterfaces(intrf);
    sayOK();
  }

  public DirectBoundaryInterface getDirectBoundaryInterfaceBetween2Regions(Region r1, Region r2){
    DirectBoundaryInterface intrfP = null;
    for(Interface intrf : sim.getInterfaceManager().getObjects()){
        try{
            intrfP = (DirectBoundaryInterface) intrf;
        } catch (Exception e) { continue; }
        if(intrfP.getRegion0() == r1 && intrfP.getRegion1() == r2 ||
           intrfP.getRegion0() == r2 && intrfP.getRegion1() == r1){
            return intrfP;
        }
    }
    return intrfP;
  }

  public DirectBoundaryInterface getDirectBoundaryInterfaceByName(String intrfName){
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
   * @param scene given Scene.
   * @param regexPatt REGEX search pattern.
   * @return A Displayer.
   */
  public Displayer getDisplayer(Scene scene, String regexPatt) {
    return getDisplayer(scene, regexPatt, true);
  }

  private Displayer getDisplayer(Scene scene, String regexPatt, boolean verboseOption) {
    say(verboseOption, "Getting Displayer by REGEX pattern: \"%s\"", regexPatt);
    say("Scene: " + scene.getPresentationName());
    for (Displayer disp : getDisplayers(regexPatt, false)) {
        if (disp.getScene() == scene) {
            say("Got by REGEX: " + disp.getPresentationName(), verboseOption);
            return disp;
        }
    }
    say("Got NULL!", verboseOption);
    return null;
  }

  private Displayer getDisplayer(String regexPatt, boolean verboseOption) {
    say(verboseOption, "Getting Displayer by REGEX pattern: \"%s\"", regexPatt);
    Displayer disp = getDisplayers(regexPatt, false).iterator().next();
    say("Got by REGEX: " + disp.getPresentationName(), verboseOption);
    return disp;
  }

  /**
   * Returns all Displayers from all Scenes matching the given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Collection of Displayers.
   */
  public Collection<Displayer> getDisplayers(String regexPatt) {
    return getDisplayers(regexPatt, true);
  }

  private Collection<Displayer> getDisplayers(String regexPatt, boolean verboseOption) {
    Vector<Displayer> vecDisp = new Vector<Displayer>();
    say(verboseOption, "Getting Displayers by REGEX pattern: \"%s\"", regexPatt);
    for (Scene scn : sim.getSceneManager().getObjects()) {
        for (Displayer disp : scn.getDisplayerManager().getObjects()) {
            if (disp.getPresentationName().matches(regexPatt)) {
                say("  Found: " + disp.getPresentationName(), verboseOption);
                vecDisp.add(disp);
            }
        }
    }
    say("Displayers found: " + vecDisp.size(), verboseOption);
    return vecDisp;
  }

  /**
   * Get the Geometric Range from a Collection of Part Surfaces. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param colPS given Collection of Part Surfaces.
   * @return A DoubleVector with the following order (minX, maxX, minY, maxY, minZ, maxZ).
   */
  public DoubleVector getExtents(Collection<PartSurface> colPS) {
    return queryStats(colPS);
  }

  /**
   * Get the Geometric Range from a Composite Part. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param cp given Composite Part.
   * @return A DoubleVector with the following order (minX, maxX, minY, maxY, minZ, maxZ).
   */
  public DoubleVector getExtents(CompositePart cp) {
    return queryStats(getPartSurfaces(cp, false));
  }

  /**
   * Get the Geometric Range from a Geometry Part. Note that the resulting output will
   * be given in default length units. See {@see #defUnitLength}.
   *
   * @param gp given Geometry Part.
   * @return A DoubleVector with the following order (minX, maxX, minY, maxY, minZ, maxZ).
   */
  public DoubleVector getExtents(GeometryPart gp) {
    return queryStats(gp.getPartSurfaces());
  }

  public FeatureCurve getFeatureCurve(Region region, String name){
    return region.getFeatureCurveManager().getFeatureCurve(name);
  }

  /**
   * Get all the Feature Curves from a Region.
   *
   * @param region given Region.
   * @return All Feature Curves from that Region.
   */
  public Collection<FeatureCurve> getFeatureCurves(Region region){
    return getFeatureCurves(region, true);
  }

  private Collection<FeatureCurve> getFeatureCurves(Region region, boolean verboseOption){
    say("Getting Feature Curves...", verboseOption);
    sayRegion(region, verboseOption);
    Vector<FeatureCurve> vecFC = (Vector<FeatureCurve>) region.getFeatureCurveManager().getFeatureCurves();
    say("All Feature Curves: " + vecFC.size(), verboseOption);
    return vecFC;
  }

  /**
   * Loop over all Field Functions and returns the first match, given the REGEX pattern.<p>
   * Note the search will be done in in FunctionName first, and then in its name on GUI,
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
    for (FieldFunction ff : sim.getFieldFunctionManager().getObjects()) {
        if (ff.getFunctionName().matches(regexPatt)) {
            say("Got by REGEX: " + ff.getFunctionName(), verboseOption);
            return ff;
        }
        if (ff.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + ff.getPresentationName(), verboseOption);
            return ff;
        }
    }
    say("Got NULL.");
    return null;
  }

  @Deprecated   //-- v2c
  private VectorComponentFieldFunction getFieldFuncionComponent(PrimitiveFieldFunction ff, int i){
    return (VectorComponentFieldFunction) ff.getComponentFunction(i);
  }

  @Deprecated   //-- v2c
  private VectorMagnitudeFieldFunction getFieldFuncionMagnitude(PrimitiveFieldFunction ff){
    return (VectorMagnitudeFieldFunction) ff.getMagnitudeFunction();
  }

  /**
   * Loop over all Geometry Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt  REGEX search pattern.
   * @return Geometry Part.
   */
  public GeometryPart getGeometryPart(String regexPatt){
    say(String.format("Getting Geometry Part by REGEX pattern: \"%s\"", regexPatt), true);
    GeometryPart gp = getAllGeometryParts(regexPatt, false).iterator().next();
    sayPart(gp);
    return gp;
  }

  /**
   * Loop over all Leaf Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Leaf Mesh Part.
   */
  public LeafMeshPart getLeafMeshPart(String regexPatt){
    return getLeafMeshPart(regexPatt, true);
  }

  private LeafMeshPart getLeafMeshPart(String regexPatt, boolean verboseOption){
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
  public GeometryPart getLeafPart(String regexPatt){
    return getLeafPart(regexPatt, true);
  }

  private GeometryPart getLeafPart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Leaf Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for(GeometryPart gp : getAllLeafParts(regexPatt, false)){
        say("Got by REGEX: " + gp.getPathInHierarchy(), verboseOption);
        return gp;
    }
    say("Got NULL.", verboseOption);
    return null;
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

  public MeshContinuum getMeshContinua(String name) {
      return ((MeshContinuum) sim.getContinuumManager().getContinuum(name));
  }

  /**
   * Loop over all Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Mesh Part.
   */
  public MeshPart getMeshPart(String regexPatt){
    return getMeshPart(regexPatt, true);
  }

  private MeshPart getMeshPart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    Vector<MeshPart> vecMP = (Vector) getMeshParts(regexPatt, false);
    if (vecMP.size() > 0) {
        say("Got by REGEX: " + vecMP.firstElement().getPathInHierarchy(), verboseOption);
        return vecMP.firstElement();
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Returns all Mesh Parts based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collection of Mesh Parts.
   */
  public Collection<MeshPart> getMeshParts(String regexPatt){
    return getMeshParts(regexPatt, true);
  }

  private Collection<MeshPart> getMeshParts(String regexPatt, boolean verboseOption){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    say(String.format("Getting Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (MeshPart mp : getAllLeafPartsAsMeshParts()) {
        if(mp.getPathInHierarchy().matches(regexPatt)){
            say("  Found: " + mp.getPathInHierarchy(), verboseOption);
            vecMP.add(mp);
        }
    }
    say("Mesh Parts found: " + vecMP.size(), verboseOption);
    return vecMP;
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
    for (Monitor mon : sim.getMonitorManager().getObjects()) {
        if (mon.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + mon.getPresentationName(), verboseOption);
            return mon;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Get all the N'th level Composite Parts. <p>
   * E.g.: Master Assembly == Level 1; Sub Assembly == Level 2; etc...
   *
   * @param nthLevel given wanted level.
   * @return Collection of all Composite Parts at N'th level.
   */
  public Collection<CompositePart> getNthLevelCompositeParts(int nthLevel){
    Vector<CompositePart> vecNthLevel = new Vector<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (getCompositePartLevel(cp) == nthLevel) {
            vecNthLevel.add(cp);
        }
    }
    return vecNthLevel;
  }

  /**
   * Loops through all Part Curves and returns the first match, given the REGEX pattern.
   *
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
   * Returns all Part Curves based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collection of Part Curves.
   */
  public Collection<PartCurve> getPartCurves(GeometryPart gp, String regexPatt) {
    return getPartCurves(gp, regexPatt, true);
  }

  private Collection<PartCurve> getPartCurves(GeometryPart gp, String regexPatt, boolean verboseOption) {
    say(String.format("Getting Part Curves by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<PartCurve> vecPC = new Vector<PartCurve>();
    for (PartCurve pc : gp.getPartCurves()) {
        if (pc.getPresentationName().matches(regexPatt)){
            say("  Found: " + pc.getPresentationName(), verboseOption);
            vecPC.add(pc);
        }
    }
    say("Part Curves found: " + vecPC.size(), verboseOption);
    return vecPC;
  }

  /**
   * Gets a Part Surface from a Geometry Part based on REGEX pattern.
   *
   * @param gp given Geometry Part.
   * @param name given REGEX search criteria.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String regexPatt) {
    return getPartSurface(gp, regexPatt, true);
  }

  private PartSurface getPartSurface(GeometryPart gp, String regexPatt, boolean verboseOption) {
    say(String.format("Getting a Part Surface by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (PartSurface ps : gp.getPartSurfaces()) {
        if(ps.getPresentationName().matches(regexPatt)) {
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
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String rangeType, String what, double tol) {
    PartSurface ps = queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion()).get(0);
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
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
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
   * Get all the Part Surfaces from a Composite Part.
   *
   * @param cp given Composite Part.
   * @return Collection of Part Surfaces.
   */
  public Collection<PartSurface> getPartSurfaces(CompositePart cp) {
    return getPartSurfaces(cp, true);
  }

  private Collection<PartSurface> getPartSurfaces(CompositePart cp, boolean verboseOption) {
    Vector<PartSurface> psVec = new Vector<PartSurface>();
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
   * Get Part Surfaces from a Geometry Part by REGEX search.
   *
   * @param gp given Geometry Part.
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Part Surfaces.
   */
  public Collection<PartSurface> getPartSurfaces(GeometryPart gp, String regexPatt) {
    return getAllPartSurfaces(gp, regexPatt, false);
  }

/**
   * Gets a Part Surface based on a Geometric Range. Loops in all Part Surfaces from Part and returns
   * all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what, double tol) {
    return queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion());
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops into specific Part Surfaces according
   * to REGEX criteria and returns all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(getPartSurfaces(gp, regexPatt), rangeType, what, tol * defUnitLength.getConversion());
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops in them and returns the first it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(Collection<PartSurface> colPS, String rangeType, String what, double tol) {
    PartSurface ps = queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion()).get(0);
    say("Returning: " + ps.getPresentationName());
    return ps;
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops in them and returns all it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(Collection<PartSurface> colPS, String rangeType, String what, double tol) {
    return queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion());
  }

  @Deprecated   //-- v2c
  public PrimitiveFieldFunction getPrimitiveFieldFunction(String var){
      return ((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction(var));
  }

  public PhysicsContinuum getPhysicsContinua(String regexPatt){
    return getPhysicsContinua(regexPatt, true);
  }

  private PhysicsContinuum getPhysicsContinua(String regexPatt, boolean verboseOption){
    printAction(String.format("Getting Physics Continua by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if (!cont.getBeanDisplayName().equals("PhysicsContinum")) {
            continue;
        }
        if (cont.getPresentationName().matches(regexPatt)) {
            say("Found: " + cont.getPresentationName(), verboseOption);
            return (PhysicsContinuum) cont;
        }
    }
    say("Found NULL.", verboseOption);
    return null;
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
   * Returns the first match of Region, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Region.
   */
  public Region getRegion(String regexPatt){
    return getRegion(regexPatt, true);
  }

  private Region getRegion(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Region by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Region region = null;
    try {
        region = sim.getRegionManager().getRegion(regexPatt);
        say("Got: " + region.getPresentationName());
        return region;
    } catch (Exception e) {
        Collection<Region> colReg = getAllRegions(regexPatt, false);
        if (colReg.size() > 0)
            region = getAllRegions(regexPatt, false).iterator().next();
    }
    if (region == null) {
        say("Got NULL.", verboseOption);
    } else {
        say("Got by REGEX: " + region.getPresentationName(), verboseOption);
    }
    return region;
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
    for (Report rep : sim.getReportManager().getObjects()) {
        if (rep.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + rep.getPresentationName(), verboseOption);
            return rep;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Returns the first match of a Report Monitor, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Report Monitor.
   */
  public ReportMonitor getReportMonitor(String regexPatt) {
    return getReportMonitor(regexPatt, true);
  }

  private ReportMonitor getReportMonitor(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Report Monitor by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (Monitor mon : sim.getMonitorManager().getMonitors()) {
        String name = mon.getPresentationName();
        String className = mon.getClass().getName();
        if (name.matches(regexPatt) && className.matches(".*ReportMonitor$")) {
            say("Got by REGEX: " + mon.getPresentationName(), verboseOption);
            return (ReportMonitor) mon;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Returns the first match of a Scene, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Scene.
   */
  public Scene getScene(String regexPatt) {
    return getScene(regexPatt, true);
  }

  private Scene getScene(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Scene by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Collection<Scene> colSCN = getScenes(regexPatt, false);
    if (!colSCN.isEmpty()) {
        Scene scn = colSCN.iterator().next();
        say("Got by REGEX: " + scn.getPresentationName(), verboseOption);
        setSceneLogo(scn);
        return scn;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  private Collection<Scene> getScenes(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Scenes by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Scene> vecSCN = new Vector<Scene>();
    for (Scene scn : sim.getSceneManager().getObjects()) {
        if (scn.getPresentationName().matches(regexPatt)){
            say("  Found: " + scn.getPresentationName(), verboseOption);
            vecSCN.add(scn);
        }
    }
    say("Scenes found: " + vecSCN.size(), verboseOption);
    return vecSCN;
  }



  private Solver getSolver(Class solver) {
    return sim.getSolverManager().getSolver(solver);
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
    say(String.format("Getting Stopping Criteria by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (SolverStoppingCriterion stp : sim.getSolverStoppingCriterionManager().getObjects()) {
        if (stp.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + stp.getPresentationName(), verboseOption);
            return stp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
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
    if (name == "")
        return ((Units) sim.getUnitsManager().getObject(""));
    say(verboseOption, "Getting Unit by exact match: \"%s\"", name);
    for (Units unit : sim.getUnitsManager().getObjects()) {
        if (unit.getPresentationName().equals(name) || unit.getDescription().equals(name)) {
            say("Got: " + unit.getPresentationName(), verboseOption);
            return unit;
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

  /**
   * Gets the STAR-CCM+ version.
   *
   * @return The code version in a x.yy.zzz string format.
   */
  public String getVersion() {
    return sim.getStarVersion().getString("ReleaseNumber");
  }

  public void groupRegionsByMedia(){
    printAction("Grouping Regions by Media: Fluid or Solid");
    Vector<Region> vecReg1 = new Vector<Region>();
    Vector<Region> vecReg2 = new Vector<Region>();
    for(Region region : getAllRegions()){
        if(isFluid(region)){
            vecReg1.add(region);
        } else {
            vecReg2.add(region);
        }
    }
    say("Fluid Regions: " + vecReg1.size());
    say("Solid Regions: " + vecReg2.size());
    sim.getRegionManager().getGroupsManager().groupObjects("Fluid Regions", vecReg1, false);
    sim.getRegionManager().getGroupsManager().groupObjects("Solid Regions", vecReg2, true);
    sayOK();
  }

  /**
   * Saves a picture from the Scene. This method will use the current Scene size.
   * <p>The picture will be saved on {@see #simPath} folder.
   *
   * @param scene given Scene.
   * @param picName given picture name. File extension is optional. If it does not find any,
   * a PNG will be saved.
   */
  public void hardCopyPicture(Scene scene, String picName) {
    hardCopyPicture(scene, picName, 0, 0);
  }

  /**
   * Saves a picture from the Scene with a given resolution.
   * <p>The picture will be saved on {@see #simPath} folder.
   *
   * @param scene given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picName given picture name. File extension is optional. If it does not find any,
   * a PNG will be saved.
   */
  public void hardCopyPicture(Scene scene, String picName, int resx, int resy) {
    String ext = retFilenameExtension(picName);
    if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
        say("File extension provided: " + ext.toUpperCase());
    } else {
        say("File extension not provided. Using PNG.");
        picName = picName + ".png";
    }
    hardCopyPicture(scene, new File(simPath, picName), resx, resy, true);
  }

  /**
   * Saves a picture from the Scene with a given resolution.
   *
   * @param scene given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picFile given picture File.
   * @param verboseOption print output messages when saving a picture? Probably not when, running.
   */
  public void hardCopyPicture(Scene scene, File picFile, int resx, int resy, boolean verboseOption) {
    hardCopyPicture(scene, picFile, resx, resy, 1, verboseOption);
  }

  private void hardCopyPicture(Scene scene, File picFile, int resx, int resy, int mag, boolean verboseOption) {
    printAction("Saving a picture", verboseOption);
    if (resx == 0 || resy == 0) {
        resx = scene.getSize()[0];
        resy = scene.getSize()[1];
    }
//////        if (!isOpen(scene))
//////            scene.initializeAndWait();
//////            scene.open(true);
//////            scene.notifySceneViewOpened();
////////            scene.openFromServer(true);
//////        scene.activate();
//////        scene.refreshSceneAndWait();
//////        scene.updateAndPrint();
//////        scene.NeedsFullRefresh();
//////        scene.NotifyViewChange();
//////        scene.getCurrentView().updateCurrentView();
//////        sleep(1000);
        scene.printAndWait(picFile, mag, resx, resy);
    say("Saved: " + picFile.getName(), verboseOption);
    sayOK(verboseOption);
    }

  /**
   * Does the Region have Deleted Cells due Remove Invalid Cells?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasDeletedCells(Region region) {
    return hasDeletedCells(region, true);
  }

  private boolean hasDeletedCells(Region region, boolean verboseOption){
    sayRegion(region, verboseOption);
    say("Has came from Remove Invalid Cells command?", verboseOption);
    if (region.getPresentationName().matches("^Cells deleted.*")) {
        sayAnswerYes(verboseOption);
        return true;
    }
    sayAnswerNo(verboseOption);
    return false;
  }

  /**
   * Is the Simulation solving for Energy?
   *
   * @return True or False.
   */
  public boolean hasEnergy() {
    for (Monitor mon : ((ResidualPlot) getPlot("Residuals", false)).getObjects()) {
        if (mon.getPresentationName().equals("Energy")) return true;
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

  /**
   * Does the Region have a Polyhedral mesh?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasPolyMesh(Region region){
    if (hasDeletedCells(region, false) ){ return false; }
    if (region.getMeshContinuum() == null) { return false; }
    if (isPoly(region.getMeshContinuum())) {
        sayRegion(region);
        say(" Has Poly mesh.");
        return true;
    }
    return false;
  }

  public boolean hasRadiationBC(Boundary bdry){
    try{
        bdry.getValues().get(EmissivityProfile.class);
        return true;
    } catch (Exception e){
        return false;
    }
  }

  /**
   * Does the Region have a Trimmer mesh?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasTrimmerMesh(Region region){
    if (hasDeletedCells(region)) { return false; }
    if (region.getMeshContinuum() == null) { return false; }
    if (isTrimmer(region.getMeshContinuum())) {
        sayRegion(region);
        say(" Has Trimmer mesh.");
        return true;
    }
    return false;
  }

  public boolean hasValidSurfaceMesh(){
    if(queryRemeshedSurface() == null){
        return false;
    }
    return true;
  }

  public boolean hasValidVolumeMesh(){
    if(queryVolumeMesh() == null){
        return false;
    }
    return true;
  }

  public boolean hasValidWrappedMesh(){
    if(queryWrappedSurface() == null){
        return false;
    }
    return true;
  }

  public void importAllDBSFromSubDirName(String subDirName) {
    printAction("Importing the DBS files.");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("From Path: " + dbsSubPath.toString());
    String[] fileNames = dbsSubPath.list(dbsFilter);
    Vector<String> filesVect = new Vector<String>();
    File dbsFile = null;
    for(String fileName : fileNames){
        dbsFile = new File(dbsSubPath, fileName);
        filesVect.add(dbsFile.toString());
    }
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    prtImpMngr.importDbsParts(filesVect, "OneSurfacePerPatch", true, "OnePartPerFile", false, unit_m, 1);
    /*
     * Auto check the imported Parts.
     */
    if(!checkMeshQualityUponSurfaceMeshImport){ return; }
    checkFreeEdgesAndNonManifoldsOnParts();
  }

  /**
   * Imports a CAD file using the MEDIUM tesselation density. It assumes the file is inside
   * {@see #simPath}. Informing the Path might be necessary.
   *
   * @param part given CAD file with extension. E.g.: "CAD\\machine.prt"
   */
  public void importCADPart(String part) {
    importCADPart(part, 3);
  }

  /**
   * Imports a CAD file using the chosen tesselation density. It assumes the file is inside
   * {@see #simPath}. Informing the Path might be necessary.
   *
   * @param part given CAD file with extension. E.g.: "CAD\\machine.prt"
   * @param tesselationOption given choice:
   *    1 - Very Coarse,
   *    2 - Coarse,
   *    3 - Medium,
   *    4 - Fine and
   *    5 - Very Fine. <u>Default is 3</u>.
   */
  public void importCADPart(String part, int tesselationOption) {
    importCADPart(new File(simPath, part), tesselationOption, true);
  }

  /**
   * Imports a CAD file using the chosen tesselation density.
   *
   * @param cadFile given CAD in {@see java.io.File} format.
   * @param tesselationOption given choice:
   *    1 - Very Coarse,
   *    2 - Coarse,
   *    3 - Medium,
   *    4 - Fine and
   *    5 - Very Fine. <u>Default is 3</u>.
   */
  public void importCADPart(File cadFile, int tesselationOption) {
    importCADPart(cadFile, tesselationOption, true);
  }

  private void importCADPart(File cadFile, int tessOpt, boolean verboseOption) {
    printAction("Importing CAD Part", verboseOption);
    say("File: " + cadFile.toString(), verboseOption);
    if (!cadFile.exists()) {
        say("File not found!", verboseOption);
    }
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    int type = TessellationDensityOption.MEDIUM;
    switch (tessOpt) {
        case 1:
            type = TessellationDensityOption.VERY_COARSE;
            break;
        case 2:
            type = TessellationDensityOption.COARSE;
            break;
        case 3:
            type = TessellationDensityOption.MEDIUM;
            break;
        case 4:
            type = TessellationDensityOption.FINE;
            break;
        case 5:
            type = TessellationDensityOption.VERY_FINE;
            break;
        default:
            break;
    }
    if(fineTesselationOnImport){
        type = TessellationDensityOption.FINE;
    }
    prtImpMngr.importCadPart(cadFile.toString(), "SharpEdges", mshSharpEdgeAngle, type, false, false);
    sayOK(verboseOption);
  }

  /**
   * Get All CAD Parts in the model and Imprint them using the CAD method.
   */
  public void imprintAllPartsByCADMethod(){
    printAction("Imprinting All Parts by CAD Method");
    //- Gerou Free Edges
    //sim.get(MeshActionManager.class).imprintCadParts(getAllLeafPartsAsMeshParts(), "CAD");
    Object[] arrayObj = getAllLeafPartsAsCadParts().toArray();
    sim.get(MeshActionManager.class).imprintCadParts(new NeoObjectVector(arrayObj), "CAD");
    sayOK();
  }

  public void imprintAllPartsByDiscreteMethod(double tolerance){
    printAction("Imprinting All Parts by Discrete Method");
    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafPartsAsMeshParts(), "Discrete", tolerance);
    sayOK();
  }

  public void imprintPartsByCADMethod(Vector<MeshPart> vecMP){
    printAction("Imprinting Parts by CAD Method");
    say("Parts: ");
    for(MeshPart mp : vecMP){
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintCadParts(vecMP, "CAD");
//    sim.get(MeshActionManager.class).imprintDiscreteParts(vecMP, "CAD", 1.0E-4);
    sayOK();
  }

  public void imprintPartsByDiscreteMethod(Vector<MeshPart> vecMP, double tolerance){
    printAction("Imprinting Parts by Discrete Method");
    say("Parts: ");
    for(MeshPart mp : vecMP){
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintDiscreteParts(vecMP, "Discrete", tolerance);
    sayOK();
  }

  public void initializeMeshing(){
    printAction("Initializing Mesh Pipeline");
    MeshPipelineController mpc = sim.get(MeshPipelineController.class);
    mpc.initializeMeshPipeline();
    sayOK();
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
  public LeafMeshPart intersect2PartsByDiscrete(Object obj1, Object obj2, String renameTo){
    printAction("Intersecting 2 Parts (obj1 'intersection' obj2)");
    say("Object 1: " + obj1.toString());
    say("Object 2: " + obj2.toString());
    MeshActionManager mshActMngr = sim.get(MeshActionManager.class);
    MeshPart mp = mshActMngr.intersectParts(new NeoObjectVector(new Object[] {obj1, obj2}), "Discrete");
    mp.setPresentationName(renameTo);
    say("Returning: " + mp.getPathInHierarchy());
    return (LeafMeshPart) mp;
  }

  private boolean isBeanDisplayName(Boundary bdry, String whatIs){
    if(bdry.getBeanDisplayName().equals(whatIs)) return true;
    return false;
  }

  private boolean isBeanDisplayName(GeometryPart gp, String whatIs){
    if(gp.getBeanDisplayName().equals(whatIs)) return true;
    return false;
  }

  private boolean isBeanDisplayName(Interface intrfPair, String whatIs){
    if(intrfPair.getBeanDisplayName().equals(whatIs)) return true;
    return false;
  }

  /** Is this Geometry Part a CAD Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCadPart(GeometryPart gp){
    return isBeanDisplayName(gp, "CAD Part");
  }

  /** Is this Geometry Part a Composite Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCompositePart(GeometryPart gp){
    return isCompositePart(gp, true);
  }

  private boolean isCompositePart(GeometryPart gp, boolean verboseOption){
    boolean isCP = isBeanDisplayName(gp, "Composite Part");
    if (isCP)
        say("Geometry Part is a Composite Part", verboseOption);
    return isCP;
  }

  /** Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isBlockPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Block Part");
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

  /** Is this Geometry Part a Simple Cylinder Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleCylinderPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Cylinder Part");
  }

  /**
   * Does this Boundary belong a Direct Boundary Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "DirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Direct Boundary Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Direct Boundary Interface");
  }

  /**
   * Does this Boundary belongs to Fluid Region?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isFluid(Boundary bdry){
    return isFluid(bdry.getRegion());
  }

  /**
   * Is this a Fluid Region?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean isFluid(Region region){
    if(region.getRegionType().toString().equals("Fluid Region")) return true;
    return false;
  }

  /**
   * Is this a Fluid-Solid (Region-Region) Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isFluidSolidInterface(Interface intrfPair){
    Region reg0 = intrfPair.getRegion0();
    Region reg1 = intrfPair.getRegion1();
    if(isFluid(reg0) && isSolid(reg1)) return true;
    if(isFluid(reg1) && isSolid(reg0)) return true;
    return false;
  }

  /**
   * Is this an Indirect Boundary Interface?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "IndirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Mapped Indirect Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Indirect Boundary Interface");
  }

  /**
   * Is this an Interface Boundary?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isInterface(Boundary bdry){
    if(isDirectBoundaryInterface(bdry) || isIndirectBoundaryInterface(bdry)) return true;
    return false;
  }

  /**
   * Is this an Interface Boundary shared by the given Regions, irrespective their order?
   *
   * @param bdry given boundary.
   * @param reg1 given Region 1.
   * @param reg2 given Region 2.
   * @return True or False.
   */
  public boolean isInterface(Boundary bdry, Region reg1, Region reg2) {
    Region r1 = null, r2 = null;
    boolean isInt = false;
    if (isDirectBoundaryInterface(bdry)) {
        DirectBoundaryInterfaceBoundary dbib = (DirectBoundaryInterfaceBoundary) bdry;
        r1 = dbib.getBoundaryInterface().getRegion0();
        r2 = dbib.getBoundaryInterface().getRegion1();
    }
    if (isIndirectBoundaryInterface(bdry)) {
        IndirectBoundaryInterfaceBoundary ibib = (IndirectBoundaryInterfaceBoundary) bdry;
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
  public boolean isLeafMeshPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Leaf Mesh Part");
  }

  /**
   * Does this Boundary belong to a meshing Region?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isMeshing(Boundary bdry){
    return isMeshing(bdry, false);
  }

  private boolean isMeshing(Boundary bdry, boolean verboseOption){
    sayBdry(bdry, verboseOption);
    if(bdry.getRegion().isMeshing()) return true;
    say("Region not meshing. Skipping...\n", verboseOption);
    return false;
  }

  /**
   * Is this Geometry Part a Mesh Operation Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isMeshOperationPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Mesh Operation Part");
  }

  /**
   * Is this a Monitor Plot?
   *
   * @param plot given Plot.
   * @return True or False.
   */
  public boolean isMonitorPlot(StarPlot plot) {
    if (plot.getClass().toString().matches(".*star.common.MonitorPlot$")) return true;
    return false;
  }

  /**
   * Is the Scene open?
   *
   * @param scene given Scene.
   * @return True or False.
   */
  public boolean isOpen(Scene scene){
    if (scene.isShowing()) return true;
    return false;
  }

  /**
   * Is this a Part Displayer?
   *
   * @param disp given Displayer.
   * @return True or False.
   */
  public boolean isPart(Displayer disp){
    if (disp.getClass().getName().equals("star.vis.PartDisplayer")) return true;
    return false;
  }

  /**
   * Is this a Poly Mesh Continua?
   *
   * @param continua given Mesh Continua.
   * @return True or False.
   */
  public boolean isPoly(MeshContinuum continua){
    if(continua.getEnabledModels().containsKey("star.dualmesher.DualMesherModel")) return true;
    return false;
  }

  /**
   * Has this Mesh Continua a Remesher Model?
   *
   * @param continua given Mesh Continua.
   * @return True or False.
   */
  public boolean isRemesh(MeshContinuum continua){
    if(continua.getEnabledModels().containsKey("star.resurfacer.ResurfacerMeshingModel")) return true;
    return false;
  }

  /**
   * Is this a Residual Plot?
   *
   * @param plot given Plot.
   * @return True or False.
   */
  public boolean isResidualPlot(StarPlot plot) {
    if (plot.getClass().toString().matches(".*star.common.ResidualPlot$")) return true;
    return false;
  }

  /**
   * Is this a Scalar Displayer?
   *
   * @param disp given Displayer.
   * @return True or False.
   */
  public boolean isScalar(Displayer disp){
    if (disp.getClass().getName().equals("star.vis.ScalarDisplayer")) return true;
    return false;
  }

  /**
   * Is this a Scalar Field Function?
   *
   * @param ff given Field Function.
   * @return True or False.
   */
  public boolean isScalar(FieldFunction ff){
    if (ff.getType().toString().equals("Scalar")) return true;
    return false;
  }

  /**
   * Is this a Streamline Displayer?
   *
   * @param disp given Displayer.
   * @return True or False.
   */
  public boolean isStreamline(Displayer disp){
    if (disp.getClass().getName().equals("star.vis.StreamDisplayer")) return true;
    return false;
  }

  /**
   * Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleBlockPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Block Part");
  }

  /**
   * Does this Boundary belong to a Solid Region?
   *
   * @param bdry given Boundary.
   * @return True or False.
   */
  public boolean isSolid(Boundary bdry){
    return isSolid(bdry.getRegion());
  }

  /**
   * Is this a Solid Region?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean isSolid(Region region){
    if(region.getRegionType().toString().equals("Solid Region")) return true;
    return false;
  }

  /**
   * Is this a Trimmer Mesh Continua?
   *
   * @param continua given Mesh Continua.
   * @return True or False.
   */
  public boolean isTrimmer(MeshContinuum continua){
    if (continua.getEnabledModels().containsKey("star.trimmer.TrimmerMeshingModel")) return true;
    return false;
  }

  /**
   * Is this a Unsteady simulation?
   *
   * @return True or False.
   */
  public boolean isUnsteady() {
    if (sim.getSolverManager().has("Implicit Unsteady")) return true;
    return false;
  }

  /**
   * Is this a Vector Displayer?
   *
   * @param disp given Displayer.
   * @return True or False.
   */
  public boolean isVector(Displayer disp){
    if (disp.getClass().getName().equals("star.vis.VectorDisplayer")) return true;
    return false;
  }

  /**
   * Is this a Vector Field Function?
   *
   * @param ff given Field Function.
   * @return True or False.
   */
  public boolean isVector(FieldFunction ff){
    if (ff.getType().toString().equals("Vector")) return true;
    return false;
  }

  /**
   * Is this a Wrapper Mesh Continua?
   *
   * @param continua given Mesh Continua.
   * @return True or False.
   */
  public boolean isWrapper(MeshContinuum continua){
    if(continua.getEnabledModels().containsKey("star.surfacewrapper.SurfaceWrapperMeshingModel"))
        return true;
    return false;
  }

  /**
   * Is this a Wall Boundary?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isWallBoundary(Boundary bdry){
    String t1 = "Wall";
    String t2 = "Contact Interface Boundary";
    String type = bdry.getBoundaryType().toString();
    if(type.equals(t1) || type.equals(t2)) return true;
    return false;
  }

  /**
   * Creates an Unite Mesh Operation between a set of Geometry Parts.
   *
   * @param colGP given Collection of Geometry Parts.
   * @return The Mesh Operation Part.
   */
  public MeshOperationPart meshOperationUniteParts(Collection<GeometryPart> colGP) {
    return meshOperation("Unite", colGP, null, true);
  }

  /**
   * Creates a Subtraction Mesh Operation between a set of Geometry Parts.
   *
   * @param colGP given Collection of Geometry Parts.
   * @param tgtGP given target  Geometry Part.
   * @return The Mesh Operation Part.
   */
  public MeshOperationPart meshOperationSubtractParts(Collection<GeometryPart> colGP, GeometryPart tgtGP) {
    return meshOperation("Subtract", colGP, tgtGP, true);
  }

  private MeshOperationPart meshOperation(String type, Collection<GeometryPart> colGP, GeometryPart tgtGP, boolean verboseOption) {
    printAction("Doing a Mesh Operation: " + type, verboseOption);
    sayParts(colGP, verboseOption);
    String opName = null;
    if (type.equals("Subtract")) {
        SubtractPartsOperation spo = (SubtractPartsOperation) sim.get(MeshOperationManager.class).createSubtractPartsOperation();
        spo.getInputGeometryObjects().setObjects(colGP);
        spo.setTargetPart((MeshPart) tgtGP);
        spo.execute();
        //say(tgtGP.getBeanDisplayName());
        //say(spo.getChildren().toString());
        opName = retStringBetweenBrackets(spo.getOutputPartNames());
        //say(spo.getOutputParts().getChildren().toString());
        //say(spo.getChildren().iterator().next());
    } else if (type.equals("Unite")) {
        UnitePartsOperation upo = (UnitePartsOperation) sim.get(MeshOperationManager.class).createUnitePartsOperation();
        upo.getInputGeometryObjects().setObjects(colGP);
        upo.setMergePartSurfaces(true);
        upo.setMergePartCurves(true);
        upo.execute();
        opName = retStringBetweenBrackets(upo.getOutputPartNames());
    }
    return ((MeshOperationPart) sim.get(SimulationPartManager.class).getPart(opName));
  }

  /**
   * Opens all Plots in the Simulation.
   */
  public void openAllPlots() {
    printAction("Opening All Plots...");
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        if (sp.getGraph().isDisplayable()) {
            say(sp.getPresentationName() + ": Opened.");
            continue;
        }
        say(sp.getPresentationName() + ": Opening...");
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
    printAction("Opening All Scenes...");
    for (Scene scene : sim.getSceneManager().getScenes()) {
        if (scene.isShowing()) {
            say(scene.getPresentationName() + ": Opened.");
            continue;
        }
        say(scene.getPresentationName() + ": Opening...");
        scene.openScene();
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

  private void prettifyLogo() {
    //-- Make the STAR-CCM+ logo more visible.
    say("Prettifying Logo Annotation...");
    ((LogoAnnotation) getAnnotation("Logo", false)).setOpacity(0.8);
  }

  /**
   * This method makes the Scenes and Plots look fancier.
   */
  public void prettifyMe() {
    printAction("Prettifying Sim file");
    prettifyLogo();
    prettifyPlots();
    prettifyScenes();
    sayOK();
  }

  private void prettifyPlots() {
    int thickness = 2;
    say("Prettifying Plots...");
    Font fontTitle = new java.awt.Font("SansSerif", 0, 18);
    Font fontOther = new java.awt.Font("SansSerif", 0, 14);
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        say("   Plot: " + sp.getPresentationName());
        sp.setTitleFont(fontTitle);
        sp.getLegend().setMapLineStyle(true);
        sp.getLegend().setFont(fontOther);
        sp.getAxes().getXAxis().getLabels().setFont(fontOther);
        sp.getAxes().getXAxis().getTitle().setFont(fontOther);
        sp.getAxes().getXAxis().getGrid().setColor(Color.lightGray);
        sp.getAxes().getXAxis().getGrid().setPattern(2);
        sp.getAxes().getYAxis().getLabels().setFont(fontOther);
        sp.getAxes().getYAxis().getTitle().setFont(fontOther);
        sp.getAxes().getYAxis().getGrid().setColor(Color.lightGray);
        sp.getAxes().getYAxis().getGrid().setPattern(2);
        for (DataSet ds : sp.getDataSetGroup().getDataSets()) {
            String status = "Not Updated!";
            if (ds.getLineStyle().getWidth() != thickness) {
                status = "Updated.";
                ds.getLineStyle().setWidth(thickness);
            }
            say("      " + ds.getPresentationName() + ": " + status);
        }
    }
  }

  private void prettifyScenes() {
    say("Prettifying Scenes...");
    for (Scene scn : getScenes(".*", false)) {
        say("Setting a white Background on Scene: " + scn.getPresentationName());
        setSceneBackgroundColor_Solid(scn, Color.white);
    }
    for (Displayer disp : getDisplayers(".*", false)) {
        if (isScalar(disp) || isVector(disp)) {
            sayDisplayer(disp);
            setDisplayerEnhancements(disp);
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

  private void printAction(String text, boolean verboseOption){
    updateSimVar();
    say("", verboseOption);
    printLine(verboseOption);
    say("+ " + getTime(), verboseOption);
    say("+ " + text, verboseOption);
    printLine(verboseOption);
  }

  /**
   * Prints a fancier frame with text in it.
   *
   * @param text message to be printed.
   */
  public void printFrame(String text){
    updateSimVar();
    say("");
    say("########################################################################################");
    say("########################################################################################");
    say("## ");
    say("## " + text.toUpperCase());
    say("## ");
    say("## " + getTime());
    say("## ");
    say("########################################################################################");
    say("########################################################################################");
    say("");
  }

  /**
   * Prints a line.
   */
  public void printLine(){
    printLine(true);
  }

  /**
   * Prints a line 'n' times.
   *
   * @param n how many times the line will be printed.
   */
  public void printLine(int n){
    printLine(n, true);
  }

  private void printLine(boolean verboseOption){
    printLine(1, verboseOption);
  }

  private void printLine(int n, boolean verboseOption){
    for (int i = 1; i <= n; i++) {
        printLine("-", verboseOption);
    }
  }

  private void printLine(String _char, boolean verboseOption) {
    String cc = _char;
    if ( _char.equals("-") ) {
        cc = "+";
    }
    //say(cc + new String(new char[80]).replace("\0", _char), verboseOption);
    say(cc + retRepeatString(_char, 80), verboseOption);
  }

  /**
   * An overview of the mesh parameters.
   */
  public void printMeshParameters(MeshContinuum continua){
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
    say("** Base Size (%s): %g", defUnitLength.toString(),  rvm.get(BaseSize.class).getValue());
    say("**");
    say("** Surface Size Relative Min (%): " +
            rvm.get(SurfaceSize.class).getRelativeMinimumSize().getPercentage());
    say("** Surface Size Relative Tgt (%): " +
            rvm.get(SurfaceSize.class).getRelativeTargetSize().getPercentage());
    say("**");
    say("** Feature Curve Relative Min (%): " + featCurveMeshMin);
    say("** Feature Curve Relative Tgt (%): " + featCurveMeshTgt);
    say("**");
    if(isPoly(continua)){
        say("** Mesh Growth Factor: " + mshGrowthFactor);
        say("**");
    }
    if(isTrimmer(continua)){
        say("** Maximum Cell Size: " + mshTrimmerMaxCelSize);
        say("** Mesh Growth Rate: " + mshTrimmerGrowthRate.toUpperCase());
        say("**");
    }
    say("*******************************************************************");
    say("");
  }

  /**
   * Prints an overview of the Prism Mesh Parameters.
   */
  public void printPrismsParameters(){
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
   * Prints an overview of the Solver Parameters.
   * @deprecated in v2c.
   */
  public void printSolverSettings(){
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**       S O L V E R   S E T T I N G S   O V E R V I E W         **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Maximum Number of Iterations: " + maxIter);
    say("**");
    say("** URF Velocity: " + urfVel);
    say("** URF Pressure: " + urfP);
    say("** URF Fluid Energy: " + urfFluidEnrgy);
    say("** URF Solid Energy: " + urfSolidEnrgy);
    say("** URF K-Epsilon: " + urfKEps);
    say("**");
    if(rampURF){
        say("** Linear Ramp Fluid Energy:");
        say("**   Start/End Iteration: " + urfRampFlIterBeg + "/" + urfRampFlIterEnd);
        say("**   Initial URF: " + urfRampFlBeg);
        say("**");
        say("** Linear Ramp Solid Energy:");
        say("**   Start/End Iteration: " + urfRampSldIterBeg + "/" + urfRampSldIterEnd);
        say("**   Initial URF: " + urfRampSldBeg);
        say("**");
    }
    say("*******************************************************************");
    say("");
  }

  /**
   * Get the Geometry Part Representation.
   *
   * @return Geometry Part Representation.
   */
  public PartRepresentation queryGeometryRepresentation(){
      return ((PartRepresentation) sim.getRepresentationManager().getObject("Geometry"));
  }

  /**
   * Get the Import Surface Representation.
   *
   * @return Import Surface Representation.
   */
  public SurfaceRep queryImportRepresentation(){
      return querySurfaceRep("Import");
  }

  /**
   * Get the Initial Surface Representation.
   *
   * @return Initial Surface Representation.
   */
  public SurfaceRep queryInitialSurface(){
    return querySurfaceRep("Initial Surface");
  }

  /** When querying by AREA it will give the PSs always based on first element (0). **/
  private Vector<PartSurface> queryPartSurfaces(Collection<PartSurface> colPS, String rangeType,
                                                                        String what, double tol) {
    if (colPS.size() == 0) {
        say("No Part Surfaces Provided for Querying. Returning NULL!");
        return null;
    }
    //--
    //-- Some declarations first.
    final String rangeOpts[] = {"MIN", "MAX"};
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    Vector<Double> vecArea = new Vector<Double>();
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    Vector choices = new Vector(Arrays.asList(xyzCoord));
    DoubleVector labMinXYZ = null, labMaxXYZ = null, psMinXYZ = null, psMaxXYZ = null;
    int rangeChoice = -1, whatChoice = -1;
    boolean proceed = true;
    //--
    //-- Headers
    printLine(2);
    printAction("Querying Part Surfaces: " + rangeType + " " + what);
    for (PartSurface ps : colPS) {
        say("  " + ps.getPresentationName());
        GeometryPart gp = ps.getPart();
        if (vecGP.contains(gp)) { continue; }
        vecGP.add(gp);
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
    scene = createScene_Geometry(false);
    PartSurfaceMeshWidget psmw = queryGeometryRepresentation().startSurfaceMeshWidget(scene);
    psmw.setActiveParts(vecGP);
    queryPartSurfaces_initPartSurfaceMeshWidget(psmw);
    //--
    //-- Add the Part Surfaces
    NeoObjectVector psObjs = new NeoObjectVector(colPS.toArray());
    for (GeometryPart gp : vecGP) {
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
    say("Global Info: " + vecGP.size() + " Geometry Part(s)");
    printLine(2);
    if(what.toUpperCase().equals("AREA")) {
        retArea = smwqc.queryFaceArea();
    } else {
        retRange = smwqc.queryFaceGeometricRange();
        labMinXYZ = retRange.getDoubleVector("LabMinRange");
        labMaxXYZ = retRange.getDoubleVector("LabMaxRange");
    }
    printLine(4);
    //--
    double val = 0.0;
    for (PartSurface ps : colPS) {
        //--
        if (!vecGP.contains(ps.getPart())) { continue; }
        if (!colPS.contains(ps)) { continue; }
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
            if (vecPS.isEmpty()) {
                vecPS.add(ps);
                vecArea.add(val);
            } else {
                int i = 0;
                for (Iterator<Double> it = ((Vector) vecArea.clone()).iterator(); it.hasNext();) {
                    Double storedVal = it.next();
                    //--                    ** Avoid duplicates **
                    if (val > storedVal && !vecPS.contains(ps)) {
                        vecPS.insertElementAt(ps, i);
                        vecArea.insertElementAt(val, i);
                    //--                        ** Avoid duplicates **
                    } else if (!it.hasNext() && !vecPS.contains(ps)) {
                        vecPS.add(ps);
                        vecArea.add(val);
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
                vecPS.add(ps);
            }
        }
        say("");
    }
    if(choices.get(whatChoice).equals("AREA") && rangeOpts[rangeChoice].equals("MIN")) {
        Collections.reverse(vecPS);
    }
    psmw.stop();
    sim.getSceneManager().deleteScene(scene);
    printLine();
    /** say("vecPS size: " + vecPS.size());
    if(what.equals("AREA")) {
        say("vecAREA size: " + vecArea.size());
    } */
    say(String.format("Found %d Part Surfaces matching %s %s:", vecPS.size(), rangeOpts[rangeChoice], choices.get(whatChoice)));
    for (PartSurface ps : vecPS) {
        say("  " + ps.getPresentationName());
    }
    printLine();
    return vecPS;
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
  public SurfaceRep queryRemeshedSurface(){
    return querySurfaceRep("Remeshed Surface");
  }

  private DoubleVector queryStats(Collection<PartSurface> colPS) {
    if (colPS.size() == 0) {
        say("No Part Surfaces Provided for Querying. Returning NULL!");
        return null;
    }
    //--
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    DoubleVector labMinXYZ = null, labMaxXYZ = null;
    DoubleVector labMinMaxXYZ = new DoubleVector();
    //--
    //-- Headers
    printLine(2);
    printAction("Querying Part Surfaces Range: " + colPS.size() + " object(s).");
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    for (PartSurface ps : colPS) {
        say("  " + ps.getPresentationName());
        GeometryPart gp = ps.getPart();
        if (vecGP.contains(gp)) { continue; }
        vecGP.add(gp);
    }
    printLine(2);
    //--
    //-- Init Widget
    scene = createScene_Geometry(false);
    PartSurfaceMeshWidget psmw = queryGeometryRepresentation().startSurfaceMeshWidget(scene);
    psmw.setActiveParts(vecGP);
    queryPartSurfaces_initPartSurfaceMeshWidget(psmw);
    //--
    //-- Add the Part Surfaces
    NeoObjectVector psObjs = new NeoObjectVector(colPS.toArray());
    for (GeometryPart gp : vecGP) {
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
    //--
    for (int i = 0; i < labMinXYZ.size(); i++) {
        labMinMaxXYZ.add(labMinXYZ.elementAt(i) / defUnitLength.getConversion());
        labMinMaxXYZ.add(labMaxXYZ.elementAt(i) / defUnitLength.getConversion());
    }
    //--
    psmw.stop();
    sim.getSceneManager().deleteScene(scene);
    printLine(2);
    return labMinMaxXYZ;
  }

  private SurfaceRep querySurfaceRep(String name){
    if(sim.getRepresentationManager().has(name)){
        return (SurfaceRep) sim.getRepresentationManager().getObject(name);
    }
    return null;
  }

  /**
   * Get the Wrapped Surface Representation.
   *
   * @return Wrapped Surface Representation.
   */
  public SurfaceRep queryWrappedSurface(){
    return querySurfaceRep("Wrapped Surface");
  }

  private Units queryUnit(String unitString, boolean verboseOption){
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
  public FvRepresentation queryVolumeMesh(){
    if(sim.getRepresentationManager().has("Volume Mesh")){
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
                break;
            case 2:
                newCam.setPosition(dv);
                break;
            case 3:
                newCam.setViewUp(dv);
                break;
        }
    }
    newCam.setParallelScale(Double.valueOf(props[4]));
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
    String camData = readData(new File(simPath, filename));
    String[] cams = camData.split("\n");
    for (int i = 0; i < cams.length; i++) {
        say("Processing Camera View: " + (i+1));
        try {
            VisView vv = readCameraView(cams[i], regexPatt, false);
            if (vv == null) {
                say("   String ignored by REGEX criteria.");
                say("   Camera format is: " + camFormat);
                say("   String given is: " + cams[i]);
                say("");
            } else {
                sayCamera(vv);
            }
        } catch (Exception e) {
            say("   Unable to process Camera View.");
        }
    }
  }

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

  private void rebuildCompositeChildren(CompositePart cp, String splitChar){
    CompositePart newCP = null;
    say("Looking in: " + cp.getPresentationName());
    String splitChar0 = splitChar;
    if(splitChar.equals("\\") || splitChar.equals("|")){
        splitChar = "\\" + splitChar;
    }
    for(GeometryPart gp : cp.getLeafParts()){
        String name = gp.getPresentationName();
        String[] splitName = name.split(splitChar);
        //say("Split Lenght: " + splitName.length);
        if(splitName.length <= 1) { continue; }
        String name0 = splitName[0];
        String gpNewName = name.replaceFirst(name0 + splitChar, "");
        if(gpNewName.equals(name)){
            say("");
            say(name);
            say("Old name == New name. Skipping...");
            say("");
            continue;
        }
        try{
            newCP = (CompositePart) cp.getChildParts().getPart(name0);
        } catch (Exception e){
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
   * @param compPart given Composite Part.
   * @param splitChar given split character. E.g.: |, +, etc...
   */
  public void rebuildCompositeHierarchy(CompositePart compPart, String splitChar){
    printAction("Rebuilding Composite Assembly Hierarchy based on a split character");
    say("Composite Part: " + compPart.getPresentationName());
    rebuildCompositeChildren(compPart, splitChar);
    sayOK();
  }

  /**
   * Rebuilds all interfaces in the model.
   */
  public void rebuildAllInterfaces() {
    printAction("Rebuilding all Interfaces of the Model");
    Vector vecBdryD = new Vector();
    Vector vecBdryD_tol = new Vector();
    Vector vecBdryI = new Vector();
    Vector vecBdryI_tol = new Vector();
    Vector<Boundary> vecBdryD0 = new Vector<Boundary>();
    Vector<Boundary> vecBdryD1 = new Vector<Boundary>();
    Vector<Boundary> vecBdryI0 = new Vector<Boundary>();
    Vector<Boundary> vecBdryI1 = new Vector<Boundary>();
    Boundary b0, b1;
    DirectBoundaryInterface dbi;
    IndirectBoundaryInterface ibi;
    say("Looping over all interfaces and reading data...");
    for(Interface intrf : getAllInterfaces(false)){
        String name = intrf.getPresentationName();
        String beanName = intrf.getBeanDisplayName();
        say(String.format("  Reading: %-40s[%s]", name, beanName));
        if (isDirectBoundaryInterface(intrf)) {
            dbi = (DirectBoundaryInterface) intrf;
            vecBdryD.add(name);
            vecBdryD_tol.add(dbi.getValues().get(InterfaceToleranceCondition.class).getTolerance());
            vecBdryD0.add(dbi.getParentBoundary0());
            vecBdryD1.add(dbi.getParentBoundary1());
        }
        if (isIndirectBoundaryInterface(intrf)) {
            ibi = (IndirectBoundaryInterface) intrf;
            vecBdryI.add(name);
            vecBdryI_tol.add(ibi.getValues().get(MappedInterfaceToleranceCondition.class).getProximityTolerance());
            vecBdryI0.add(ibi.getParentBoundary0());
            vecBdryI1.add(ibi.getParentBoundary1());
        }
    }
    clearInterfaces(false);
    say("Recreating " + vecBdryD.size() + " Direct Interfaces...");
    for (int i = 0; i < vecBdryD.size(); i++) {
        String name = (String) vecBdryD.elementAt(i);
        double tol = (Double) vecBdryD_tol.elementAt(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = vecBdryD0.elementAt(i);
        b1 = vecBdryD1.elementAt(i);
        dbi = sim.getInterfaceManager().createDirectInterface(b0, b1);
        dbi.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
        dbi.setPresentationName(name);
    }
    say("Recreating " + vecBdryI.size() + " Indirect Interfaces...");
    for (int i = 0; i < vecBdryI.size(); i++) {
        String name = (String) vecBdryI.elementAt(i);
        double tol = (Double) vecBdryI_tol.elementAt(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = vecBdryI0.elementAt(i);
        b1 = vecBdryI1.elementAt(i);
        ibi = sim.getInterfaceManager().createIndirectInterface(b0, b1);
        ibi.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
        ibi.setPresentationName(name);
    }
    sayOK();
  }

  /**
   * Removes all Part Contacts.
   */
  public void removeAllPartsContacts(){
    printAction("Removing All Parts Contacts");
    for(GeometryPart gp : getAllLeafParts()){
        CadPart cp = (CadPart) gp;
        Collection<PartContact> contacts = sim.get(SimulationPartManager.class).getPartContactManager().getObjects();
        for(PartContact pc : contacts){
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
  public void removeCadPart(String name){
    printAction("Removing Cad Part: " + name);
    CadPart cadPrt = getCadPart(name);
    CompositePart compPart = (CompositePart) cadPrt.getParentPart();
    compPart.getChildParts().removePart(cadPrt);
    say("Removed: " + cadPrt.getPathInHierarchy());
    if(cadPrt == null){
        say("CadPart not found: " + name);
    }
    sayOK();
  }

  /**
   * Removes a Composite Part.
   *
   * @param compPart given Composite Part.
   */
  public void removeCompositePart(CompositePart compPart){
    removeCompositePart(compPart, true);
  }

  private int removeCompositePart(CompositePart compPart, boolean verboseOption){
    printAction("Removing a Composite Part", verboseOption);
    try{
        CompositePart parent = ((CompositePart) compPart.getParentPart());
        say("Removing Composite: " + compPart.getPresentationName());
        parent.getChildParts().remove(compPart);
        sayOK(verboseOption);
        return 0;
    } catch (Exception e){
        say("ERROR! Could not remove Composite Part.");
    }
    return 1;
  }

  /**
   * Removes a group of Composite Parts.
   *
   * @param colCompParts given Collection of Composite Parts.
   */
  public void removeCompositeParts(Collection<CompositePart> colCompParts){
    removeCompositeParts(colCompParts, true);
  }

  /**
   * Removes a group of Composite Parts based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void removeCompositeParts(String regexPatt) {
    printAction(String.format("Removing Composite Parts based on REGEX pattern: \"%s\"", regexPatt ));
    int n = 0;
    for (CompositePart cP : getAllCompositeParts(regexPatt, false)){
        if (removeCompositePart(compPart, false) == 0) { n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK();
  }

  private void removeCompositeParts(Collection<CompositePart> colCompParts, boolean verboseOption){
    printAction("Removing Composite Parts", verboseOption);
    int n = 0;
    say("Composite Parts to be removed: " + colCompParts.size());
    for (CompositePart cp : colCompParts) {
        int ret = removeCompositePart(cp, false);
        if (ret == 0) { n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK(verboseOption);
  }

  /**
   * Removes a Displayer from a Scene.
   *
   * @param disp given Displayer.
   */
  public void removeDisplayer(Displayer disp) {
    printAction("Removing a Displayer");
    say("Displayer: " + disp.getPresentationName());
    say("From Scene: " + disp.getScene().getPresentationName());
    disp.getScene().getDisplayerManager().deleteChildren(new NeoObjectVector((new Object[] {disp})));
    sayOK();
  }

  /**
   * Removes all the Feature Curves of a Region. Useful when using the Surface Wrapper.
   *
   * @param region given Region.
   */
  public void removeFeatureCurves(Region region) {
    printAction("Removing all Feature Curves");
    sayRegion(region);
    region.getFeatureCurveManager().deleteChildren(region.getFeatureCurveManager().getFeatureCurves());
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
        } catch(Exception e){
            say("ERROR! Could not remove Geometry Part.");
        }
    }
  }

  public void removeGeometryParts(String regexPatt){
    printAction(String.format("Removing Geometry Parts based on REGEX criteria: \"%s\"", regexPatt));
    for (GeometryPart gp : getAllGeometryParts(regexPatt, false)) {
        removeGeometryPart(gp, false);
    }
    sayOK();
  }

  /**
   * Removes the Invalid Cells in the model.
   */
  public void removeInvalidCells(){
    Vector<Region> regionsPoly = new Vector<Region>();
    Vector<Region> regionsTrimmer = new Vector<Region>();
    Vector<Region> fvRegions = new Vector<Region>();
    for (Region region : getAllRegions(false)) {
        if(hasPolyMesh(region)){
            regionsPoly.add(region);
            continue;
        }
        if(hasTrimmerMesh(region)){
            regionsTrimmer.add(region);
            continue;
        }
        fvRegions.add(region);
    }
    /* Removing From fvRepresentation */
    if (fvRegions.size() > 0) {
        printAction("Removing Invalid Cells from Regions using Default Parameters");
        say("Number of Regions: " + fvRegions.size());
        sim.getMeshManager().removeInvalidCells(fvRegions, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Poly Meshes */
    if(regionsPoly.size() > 0){
        printAction("Removing Invalid Cells from Poly Meshes");
        say("Number of Regions: " + regionsPoly.size());
        if(aggressiveRemoval){
            minFaceValidity = 0.95;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(regionsPoly, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Trimmer Meshes */
    if(regionsTrimmer.size() > 0){
        printAction("Removing Invalid Cells from Trimmer Meshes");
        say("Number of Regions: " + regionsTrimmer.size());
        if(aggressiveRemoval){
            minFaceValidity = 0.51;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(regionsTrimmer, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
  }

  /**
   * Removes a Leaf Mesh Part.
   *
   * @param lmp given Leaf Mesh Part.
   */
  public void removeLeafMeshPart(LeafMeshPart lmp){
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
   * @param colLMP given Collection of Leaf Mesh Parts.
   */
  public void removeLeafMeshParts(Collection<LeafMeshPart> colLMP){
    removeLeafMeshParts(colLMP, true);
  }

  /**
   * Removes all Leaf Mesh Parts given REGEX search pattern.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void removeLeafMeshParts(String regexPatt){
    printAction(String.format("Removing Leaf Meshs Part by REGEX pattern: \"%s\"", regexPatt));
    removeLeafMeshParts(getAllLeafMeshParts(regexPatt, false), true);
  }

  private void removeLeafMeshParts(Collection<LeafMeshPart> colLMP, boolean verboseOption){
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
  public void removeLeafParts(String regexPatt){
    removeLeafParts(regexPatt, true);
  }

  private void removeLeafParts(String regexPatt, boolean verboseOption){
    printAction(String.format("Remove Leaf Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Collection<GeometryPart> colLP = getAllLeafParts(regexPatt, false);
    say("Leaf Parts to be removed: " + colLP.size());
    for (GeometryPart gp : colLP) {
        removeGeometryPart(gp, false);
    }
    sayOK(verboseOption);
  }

  public void removePartsContacts(GeometryPart refPart, Vector<GeometryPart> vecPart){
    printAction("Removing Parts Contacts");
    say("Reference Part: " + refPart.getPathInHierarchy());
    say("Contacting Parts: ");
    for(GeometryPart gp : vecPart){
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

  public void renameInterfacesOnParts(){
    printAction("Renaming internal faces acording to interfaces");
    for(GeometryPart part : getAllGeometryParts()) {
        LeafMeshPart leafPart = ((LeafMeshPart) part);
        Collection<PartSurface> partSurfaces = leafPart.getPartSurfaces();
        if(partSurfaces.size() > 0){
            say("Geometry Part: " + part.getPresentationName());
        }
        for(PartSurface srf : partSurfaces) {
            String name = srf.getPresentationName();
            String newName = retStringBetweenBrackets(name);
            if(!newName.equals(noneString)) {
                // Skip the interfaces
                sayOldNameNewName(name, newName);
                srf.setPresentationName(newName);
            }
        }
        say("");
        //break;
    }
  }

  /**
   * Renames Part Surfaces based on 2 REGEX search patterns. <p>
   * It looks for all Part Surfaces that has both search Strings and rename it accordingly.
   *
   * @param hasString1 given REGEX search pattern 1.
   * @param hasString2 given REGEX search pattern 2.
   * @param renameTo given new name of the found Part Surfaces.
   */
  public void renamePartSurfaces(String hasString1, String hasString2, String renameTo){
    printAction("Renaming Part Surface(s)");
    for (PartSurface ps : getAllPartSurfaces(".*", false)) {
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
   * @param region given Region.
   * @param regexPatt given REGEX search pattern.
   * @param renameTo given new Boundary name.
   */
  public void renameBoundary(Region region, String regexPatt, String renameTo) {
    renameBoundary(region, regexPatt, renameTo, true);
  }

  private void renameBoundary(Region region, String regexPatt, String renameTo, boolean verboseOption) {
    printAction("Renaming Boundary", verboseOption);
    sayRegion(region);
    Boundary bdry = getBoundary(region, regexPatt, false);
    sayOldNameNewName(bdry.getPresentationName(), renameTo);
    bdry.setPresentationName(renameTo);
    sayOK(verboseOption);
  }

  /**
   * When the Boundary names are preceeded by the Region name, this method will remove the prefix from
   * all Boundary names.
   *
   * @param region given Region.
   */
  public void resetBoundaryNames(Region region){
    printAction("Removing Prefixes from Boundary Names");
    sayRegion(region);
    for (Boundary bdry : region.getBoundaryManager().getBoundaries()) {
        String name = bdry.getPresentationName();
        String newName = name.replace(region.getPresentationName() + ".", "");
        sayOldNameNewName(name, newName);
        bdry.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets the Interface names to show the dependent Region names and types. <p>
   * E.g.: <i>F-S: Pipe <-> Metal</i> means that is a Fluid-Solid Interface between Pipe and Metal
   * Regions.
   */
  public void resetInterfaceNames() {
    printAction("Resetting Interface Names...");
    Collection<Interface> colInt = getAllInterfaces(false);
    say("Number of Interfaces: " + colInt.size());
    for (Interface intrf : colInt) {
        String name0 = intrf.getRegion0().getPresentationName();
        String type0 = "F";
        if (isSolid(intrf.getRegion0())) {
            type0 = "S";
        }
        String name1 = intrf.getRegion1().getPresentationName();
        String type1 = "F";
        if (isSolid(intrf.getRegion1())) {
            type1 = "S";
        }
        String newName = String.format("%s-%s: %s <-> %s", type0, type1, name0, name1);
        sayOldNameNewName(intrf.getPresentationName(), newName);
        intrf.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets all Solver Settings to the default values.
   */
  public void resetSolverSettings() {
    printAction("Resetting All URFs...");
    maxIter = maxIter0;
    urfP = urfP0;
    urfVel = urfVel0;
    urfKEps = urfKEps0;
    urfFluidEnrgy = urfFluidEnrgy0;
    urfSolidEnrgy = urfSolidEnrgy0;
    updateSolverSettings();
  }


  /**
   * Reset the Surface Remesher to default conditions.
   *
   * @param continua
   */
  public void resetSurfaceRemesher(MeshContinuum continua){
    enableSurfaceRemesher(continua);
    enableSurfaceProximityRefinement(continua);
    enableSurfaceRemesherAutomaticRepair(continua);
    enableSurfaceRemesherProjectToCAD(continua);
  }

  /**
   * Retesselate the CAD Part to Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToFine(CadPart part){
    printAction("ReTesselating a Part To Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.FINE);
    sayOK();
  }

  /**
   * Retesselate the CAD Part to Very Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToVeryFine(CadPart part){
    printAction("ReTesselating a Part To Very Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.VERY_FINE);
    sayOK();
  }

  private void reTesselateCadPart(CadPart part, int type){
    sayPart(part);
    part.getTessellationDensityOption().setSelected(type);
    part.getCadPartEdgeOption().setSelected(CadPartEdgeOption.SHARP_EDGES);
    part.setSharpEdgeAngle(mshSharpEdgeAngle);
    part.tessellate();
    sayOK();
  }

  /** Returns whether the absolute difference between 2 doubles is within the tolerance. */
  private boolean retDiff(double d1, double d2, double tol) {
    if (Math.abs(d1 - d2) <= tol) {
        return true;
    }
    return false;
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

  private String retFilenameExtension(String filename) {
    //----------------------------------------------
    //-- Copied from Apache FilenameUtils method.
    //----------------------------------------------
    if (filename == null) return null;
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
        double d1 = dv1.elementAt(i);
        double delta = (dv2.elementAt(i) - dv1.elementAt(i)) / totSteps;
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

  private String retMinMaxString(double min, double max){
    return retMinMaxString("Min/Max", "%g", min, max);
  }

  private String retMinMaxString(String baseString, String numFmt, double min, double max){
    String base = String.format("%s = %s/%s", baseString, numFmt, numFmt);
    return String.format(base, min, max);
  }

  private int retNumberOfParentParts(Collection<PartSurface> colPS) {
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    for (PartSurface ps : colPS) {
        if (!vecGP.contains(ps.getPart())) {
            vecGP.add(ps.getPart());
        }
    }
    return vecGP.size();
  }

  private String retRepeatString(String str0, int times) {
    return new String(new char[times]).replace("\0", str0);
  }

  //-- This variable is necessary for the Spline methods. Do not touch it.
  private int last_interval;
  //--
  private double[][] retSpline(Vector<Double> vecXX, Vector<Double> vecFF) {
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
    //-- Trick converting Vector<Double> to double[]
    double[] xx = new double[vecXX.size()];
    double[] ff = new double[vecFF.size()];
    for (int i = 0; i < vecFF.size(); i++) {
        xx[i] = vecXX.elementAt(i);
        ff[i] = vecFF.elementAt(i);
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
      if(debug) say("Spline data x["+i+"]="+x[i]+", f[]="+f[i]);
    }
    //-- Calculate coefficients for the tri-diagonal system: store
    //-- sub-diagonal in b, diagonal in d, difference quotient in c.
    b[0] = x[1]-x[0];
    c[0] = (f[1]-f[0])/b[0];
    d[0] = two*b[0];
    for(int i=1; i<n-1; i++) {
       b[i] = x[i+1]-x[i];
       if(Math.abs(b[i]-b[0])/b[0]>1.0E-5) uniform = false;
       c[i] = (f[i+1]-f[i])/b[i];
       d[i] = two*(b[i]+b[i-1]);
    }
    d[n-1] = two*b[n-2];
    //-- Calculate estimates for the end slopes.  Use polynomials
    //-- interpolating data nearest the end.
    fp1 = c[0]-b[0]*(c[1]-c[0])/(b[0]+b[1]);
    if(n>3) fp1 = fp1+b[0]*((b[0]+b[1])*(c[2]-c[1])/
                  (b[1]+b[2])-c[1]+c[0])/(x[3]-x[0]);
    fpn = c[n-2]+b[n-2]*(c[n-2]-c[n-3])/(b[n-3]+b[n-2]);
    if(n>3) fpn = fpn+b[n-2]*(c[n-2]-c[n-3]-(b[n-3]+
                  b[n-2])*(c[n-3]-c[n-4])/(b[n-3]+b[n-4]))/(x[n-1]-x[n-4]);
    //--
    //-- Calculate the right-hand-side and store it in c.
    c[n-1] = three*(fpn-c[n-2]);
    for(int i=n-2; i>0; i--)
       c[i] = three*(c[i]-c[i-1]);
    c[0] = three*(c[0]-fp1);
    //--
    //-- Solve the tridiagonal system.
    for(int k=1; k<n; k++) {
       p = b[k-1]/d[k-1];
       d[k] = d[k]-p*b[k-1];
       c[k] = c[k]-p*c[k-1];
    }
    c[n-1] = c[n-1]/d[n-1];
    for(int k=n-2; k>=0; k--)
       c[k] = (c[k]-b[k]*c[k+1])/d[k];
    //--
    //-- Calculate the coefficients defining the spline.
    h = x[1]-x[0];
    for(int i=0; i<n-1; i++) {
       h = x[i+1]-x[i];
       d[i] = (c[i+1]-c[i])/(three*h);
       b[i] = (f[i+1]-f[i])/h-h*(c[i]+h*d[i]);
    }
    b[n-1] = b[n-2]+h*(two*c[n-2]+h*three*d[n-2]);
    if(debug) say("spline coefficients");
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
    if(t>x[n-2])
       interval = n-2;
    else if (t >= x[last_interval])
       for(int j=last_interval; j<n&&t>=x[j]; j++) interval = j;
    else
       for(int j=last_interval; t<x[j]; j--) interval = j-1;
    last_interval = interval; // class variable for next call
    //-- Evaluate cubic polynomial on [x[interval] , x[interval+1]].
    double dt = t-x[interval];
    double s = f[interval]+dt*(b[interval]+dt*(c[interval]+dt*d[interval]));
    return s;
  }

  private String retStringBetweenBrackets(String text) {
    Pattern patt = Pattern.compile(".*\\[(.*)\\]");
    Matcher matcher = patt.matcher(text);
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

  private String retStringBoundaryAndRegion(Boundary bdry){
    Region region = bdry.getRegion();
    return region.getPresentationName() + "\\" + bdry.getPresentationName();
  }

  private String retString(int number){
      return String.valueOf(number);
  }

  private String retString(double[] array) {
    String strng = "" + array[0];
    for (int i = 1; i < array.length; i++) {
        strng += ", " + array[i];
    }
    return strng;
  }

  private String retTemp(double T){
    return "Temperature: " + T + defUnitTemp.getPresentationName();
  }

  /**
   * Runs the case.
   */
  public void runCase() {
    runCase(0);
  }

  /**
   * Runs/Steps the case for many iterations.
   *
   * @param n If n > 0: step n iterations; If n == 0: just run.
   */
  public void runCase(int n) {
    if (!hasValidVolumeMesh()) {
        printAction("Running the case");
        say("No volume mesh found. Skipping run.");
        return;
    }
    prettifyMe();
    if (n > 0) {
        printAction("Running " + n + " iterations of the case");
        sim.getSimulationIterator().step(n);
    } else {
        printAction("Running the case");
        sim.getSimulationIterator().run();
    }
  }

  /**
   * Runs the case for 10 iterations and then clear the history. It makes the Report Plots look
   * fancier. Note that the Solution is not erased, just the 10 iterations history.
   *
   * @param initCondWasher wash out the Initial Conditions?
   */
  public void runCase(boolean initCondWasher) {
    if (initCondWasher) {
        runCase(10);
        clearSolution(true);
    }
    runCase(0);
  }

  /**
   * Print something to output/log file.
   *
   * @param msg message to be printed.
   */
  public void say(String msg){
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

  private void say(String msg, boolean verboseOption){
    if(!verboseOption){ return; }
    sim.println(sayPreffixString + " " + msg);
  }

  private void sayAnswerNo(boolean verboseOption) {
    say("  NO", verboseOption);
  }

  private void sayAnswerYes(boolean verboseOption) {
    say("  YES", verboseOption);
  }

  private void sayBdry(Boundary bdry) {
    sayBdry(bdry, true);
  }

  private void sayBdry(Boundary bdry, boolean verboseOption) {
    sayBdry(bdry, true, true);
  }

  private void sayBdry(Boundary bdry, boolean verboseOption, boolean beanDisplayOption){
    String b = bdry.getPresentationName();
    String r = bdry.getRegion().getPresentationName();
    say("Boundary: " + b + "\t[Region: " + r + "]", verboseOption);
    if(!beanDisplayOption){ return; }
    say("Bean Display Name is \"" + bdry.getBeanDisplayName() + "\".", verboseOption);
  }

  private void sayBoundaries(Collection<Boundary> boundaries) {
    sayBoundaries(boundaries, true);
  }

  private void sayBoundaries(Collection<Boundary> boundaries, boolean verboseOption) {
    say("Number of Boundaries: " + boundaries.size(), verboseOption);
    for (Boundary bdry : boundaries) {
        sayBdry(bdry, verboseOption, false);
    }
  }

  /**
   * Prints a small info on the Displayer.
   *
   * @param disp given Displayer.
   */
  public void sayDisplayer(Displayer disp) {
    String className = disp.getClass().getName().replace("star.vis.", "").replace("Disp", " Disp");
    String dispName = disp.getPresentationName();
    String scnName = disp.getScene().getPresentationName();
    String suffix = ".";
    if (isScalar(disp)) {
        suffix = String.format(" is showing '%s'.",
            ((ScalarDisplayer) disp).getScalarDisplayQuantity().getFieldFunction().getPresentationName());
    } else if (isVector(disp)) {
        suffix = String.format(" is showing '%s'.",
            ((VectorDisplayer) disp).getVectorDisplayQuantity().getFieldFunction().getPresentationName());
    } else if (isStreamline(disp)) {
        suffix = String.format(" is showing '%s'.",
            ((StreamDisplayer) disp).getScalarDisplayQuantity().getFieldFunction().getPresentationName());
    }
    say("%s '%s' on Scene '%s'%s", className, dispName, scnName, suffix);
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
    say("", verboseOption);
  }

  private void sayContinua(MeshContinuum continua) {
    sayContinua(continua, true);
  }

  private void sayContinua(MeshContinuum continua, boolean verboseOption) {
    say("Mesh Continua: " + continua.getPresentationName(), verboseOption);
  }

  private void sayContinua(PhysicsContinuum phC){
    sayContinua(phC, true);
  }

  private void sayContinua(PhysicsContinuum phC, boolean verboseOption){
    say("Physics Continua: " + phC.getPresentationName(), verboseOption);
  }

  private void sayFieldFunction(FieldFunction ff, boolean verboseOption) {
    say("Field Function: " + ff.getPresentationName(), verboseOption);
    say("   Function Name: " + ff.getFunctionName(), verboseOption);
  }

  private void sayInterface(Interface intrfPair) {
    sayInterface(intrfPair, true, true);
  }

  private void sayInterface(Interface intrfPair, boolean verboseOption, boolean beanDisplayOption) {
    say("Interface: " + intrfPair.getPresentationName(), verboseOption);
    if(beanDisplayOption){
        say("Bean Display Name is \"" + intrfPair.getBeanDisplayName() + "\".", verboseOption);
    }
    sayInterfaceSides(intrfPair, verboseOption);
  }

  private void sayInterfaceSides(Interface intrfPair) {
    sayInterfaceSides(intrfPair, true);
  }

  private void sayInterfaceSides(Interface intrfPair, boolean verboseOption) {
    if(!verboseOption){ return; }
    String side1 = null, side2 = null;
    if(isDirectBoundaryInterface(intrfPair)){
        side1 = retStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    if(isIndirectBoundaryInterface(intrfPair)){
        side1 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    say("  Side1: " + side1);
    say("  Side2: " + side2);
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

  private void saySimName(boolean verboseOption) {
    say("Simulation Name: " + sim.getPresentationName(), verboseOption);
  }

  /**
   * Outputs a simple simulation overview.
   */
  private void saySimOverview() {
    saySimName(true);
    say("Simulation File: " + simFile.toString());
    say("Simulation Path: " + simPath);
  }

  /**
   * It's OK!
   */
  public void sayOK(){
    sayOK(true);
  }

  /**
   * It's OK!
   *
   * @param verboseOption should I really output that?
   */
  public void sayOK(boolean verboseOption){
    say("OK!\n", verboseOption);
  }

  private void sayOldNameNewName(String name, String newName){
    say("  Old name: " + name);
    say("  New name: " + newName);
    say("");
  }

  private void sayPart(GeometryPart gp) {
    sayPart(gp, true);
  }

  private String sayPart(GeometryPart gp, boolean verboseOption) {
    String toSay = "Part: ";
    if(isCadPart(gp)){
        toSay += ((CadPart) gp).getPathInHierarchy();
    }
    if(isLeafMeshPart(gp)){
        toSay += ((LeafMeshPart) gp).getPathInHierarchy();
    }
    if(isSimpleBlockPart(gp)){
        toSay += ((SimpleBlockPart) gp).getPathInHierarchy();
    }
    say(toSay, verboseOption);
    return toSay;
  }

  private void sayParts(Collection<GeometryPart> colGP, boolean verboseOption) {
    say("Number of Parts: " + colGP.size(), verboseOption);
    for (GeometryPart gp : colGP) {
        say("  " + sayPart(gp, false), verboseOption);
    }
  }

  private void sayPartSurface(PartSurface ps) {
    sayPartSurface(ps, true);
  }

  private void sayPartSurface(PartSurface ps, boolean verboseOption) {
    say("Part Surface: " + ps.getPresentationName(), verboseOption);
    if(isCadPart(ps.getPart())){
        say("CAD Part: " + ((CadPart) ps.getPart()).getPathInHierarchy(), verboseOption);
    }
    if(isLeafMeshPart(ps.getPart())){
        say("Leaf Mesh Part: " + ((LeafMeshPart) ps.getPart()).getPathInHierarchy(), verboseOption);
    }
  }

  private void sayRegion(Region region){
    sayRegion(region, true);
  }

  private void sayRegion(Region region, boolean verboseOption){
    say("Region: " + region.getPresentationName(), verboseOption);
  }

  private void sayRegions(Collection<Region> regions){
    say("Number of Regions: " + regions.size());
    for (Region reg : regions) {
        say("  " + reg.getPresentationName());
    }
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

  /**
   * Saves the simulation file using the current name.
   */
  public void saveSim(){
    saveSim(sim.getPresentationName());
  }

  /**
   * Saves the simulation file using a custom name.
   *
   * @param name given name.
   */
  public void saveSim(String name){
    String newName = name + ".sim";
    printAction("Saving: " + newName);
    sim.saveState(new File(simPath, newName).toString());
  }

  /**
   * Saves the simulation file appending a suffix. <p>
   *
   * The basic name is given using the {@link #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   */
  public void saveSimWithSuffix(String suffix){
    if(!saveIntermediates){ return; }
    String newName = simTitle + "_" + suffix;
    saveSim(newName);
    savedWithSuffix++;
  }

  /**
   * Saves the simulation file appending a suffix with the option to force saving
   * intermediate files. <p>
   *
   * The basic name is given using the {@link #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   * @param forceOption save intermediate simulation files as well?
   *                    Depends on {@link #saveIntermediates}
   */
  public void saveSimWithSuffix(String suffix, boolean forceOption){
    boolean interm = saveIntermediates;
    if(forceOption){
        saveIntermediates = true;
    }
    saveSimWithSuffix(suffix);
    saveIntermediates = interm;
  }

  public void setAutoSave(){
    printAction("Setting Auto Save Options");
    AutoSave as = sim.getSimulationIterator().getAutoSave();
    as.getTriggerOption().setSelected(AutoSaveTriggerOption.ITERATION);
    as.setEnabled(true);
    as.setAutoSaveBatch(true);
    as.setAutoSaveMesh(false);
    as.setMaxAutosavedFiles(autoSaveMaxFiles);
    as.setTriggerFrequency(autoSaveFrequencyIter);
    as.setSeparator(autoSaveSeparator);
    as.setFormatWidth(6);
  }

  /**
   * Sets the Wall Boundary as Constant Temperature.
   *
   * @param bdry given Boundary.
   * @param T given Temperature in default units. See {@link #defUnitTemp}.
   */
  public void setBC_ConstantTemperatureWall(Boundary bdry, double T){
    printAction("Setting BC as Constant Temperature Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.TEMPERATURE);
    setBC_StaticTemperature(bdry, T);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Convection Heat Transfer type.
   *
   * @param bdry given Boundary.
   * @param T given Ambient Temperature in default units. See {@link #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@link #defUnitHTC}.
   */
  public void setBC_ConvectionWall(Boundary bdry, double T, double htc){
    setBC_ConvectionWall(bdry, T, htc, true);
  }

  /**
   * Sets the Wall Boundary as Environment Heat Transfer type.
   *
   * @param bdry given Boundary.
   * @param T given Ambient Temperature in default units. See {@link #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@link #defUnitHTC}.
   * @param emissivity given Emissivity dimensionless.
   * @param transmissivity given Transmissivity dimensionless.
   * @param externalEmissivity given External Emissivity dimensionless.
   */
  public void setBC_EnvironmentWall(Boundary bdry, double T, double htc, double emissivity,
                                                double transmissivity, double externalEmissivity){
    printAction("Setting BC as an Environment Wall");
    sayBdry(bdry);
    setBC_ConvectionWall(bdry, T, htc, false);
    if(hasRadiationBC(bdry)){
        //say("  External Emissivity: " + externalEmissivity);
        setRadiationParametersS2S(bdry, emissivity, transmissivity);
        bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.ENVIRONMENT);
        ExternalEmissivityProfile eemP = bdry.getValues().get(ExternalEmissivityProfile.class);
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
   * @param bdry given Boundary.
   */
  public void setBC_FreeSlipWall(Boundary bdry){
    printAction("Setting BC as a Free Slip Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.SLIP);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Free Stream.
   *
   * @param bdry given Boundary.
   * @param dir given 3-components direction of the flow. E.g., in X: {1, 0, 0}.
   * @param mach given Mach number.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_FreeStream(Boundary bdry, double[] dir, double mach, double T,
                                                                    double ti, double tvr){
    printAction("Setting BC as a Free Stream");
    sayBdry(bdry);
    bdry.setBoundaryType(FreeStreamBoundary.class);
    setBC_FlowDirection(bdry, dir);
    setBC_MachNumber(bdry, mach);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Mass Flow Rate Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param mfr given Mass Flow Rate in default units. See {@link #defUnitMFR}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_MassFlowRateInlet(Boundary bdry, double mfr, double T, double ti, double tvr){
    printAction("Setting BC as Mass Flow Rate Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(MassFlowBoundary.class);
    setBC_MassFlowRate(bdry, mfr);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Pressure Outlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param P given Static Pressure in default units. See {@link #defUnitTemp}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless, if applicable.
   * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
   */
  public void setBC_PressureOutlet(Boundary bdry, double P, double T, double ti, double tvr){
    printAction("Setting BC as Pressure Outlet");
    sayBdry(bdry);
    bdry.setBoundaryType(PressureBoundary.class);
    setBC_StaticPressure(bdry, P);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Stagnation Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param P given Total Pressure in default units. See {@link #defUnitPress}.
   * @param T given Total Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_StagnationInlet(Boundary bdry, double P, double T, double ti, double tvr){
    printAction("Setting BC as Stagnation Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(StagnationBoundary.class);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Symmetry.
   *
   * @param bdry given Boundary.
   */
  public void setBC_Symmetry(Boundary bdry){
    printAction("Setting BC as Symmetry");
    sayBdry(bdry);
    bdry.setBoundaryType(SymmetryBoundary.class);
    sayOK();
  }

  /**
   * Sets a Boundary as Velocity Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param vel given Velocity Magnitude in default units. See {@link #defUnitVel}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_VelocityMagnitudeInlet(Boundary bdry, double vel, double T,
                                                                double ti, double tvr) {
    printAction("Setting BC as Velocity Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(InletBoundary.class);
    setBC_VelocityMagnitude(bdry, vel);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  private void setBC_ConvectionWall(Boundary bdry, double T, double htc, boolean verboseOption) {
    printAction("Setting BC as Convection Wall", verboseOption);
    sayBdry(bdry, verboseOption);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.CONVECTION);
    AmbientTemperatureProfile atp = bdry.getValues().get(AmbientTemperatureProfile.class);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    HeatTransferCoefficientProfile htcp = bdry.getValues().get(HeatTransferCoefficientProfile.class);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitHTC);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(htc);
    sayOK(verboseOption);
  }

  private void setBC_FlowDirection(Boundary bdry, double[] dir) {
    if (!bdry.getValues().has("Flow Direction")) {
      return;
    }
    FlowDirectionProfile fdp = bdry.getValues().get(FlowDirectionProfile.class);
    fdp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(dir[0], dir[1], dir[2]);
  }

  private void setBC_MachNumber(Boundary bdry, double mach) {
    if (!bdry.getValues().has("Mach Number")) {
      return;
    }
    MachNumberProfile mnp = bdry.getValues().get(MachNumberProfile.class);
    mnp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(mach);
  }

  private void setBC_MassFlowRate(Boundary bdry, double mfr) {
    if (!bdry.getValues().has("Mass Flow Rate")) {
      return;
    }
    MassFlowRateProfile mfrp = bdry.getValues().get(MassFlowRateProfile.class);
    mfrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitMFR);
    mfrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(mfr);
  }

  private void setBC_StaticPressure(Boundary bdry, double P) {
    if (!bdry.getValues().has("Pressure")) {
      return;
    }
    StaticPressureProfile spp = bdry.getValues().get(StaticPressureProfile.class);
    spp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
    spp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(P);
  }

  private void setBC_StaticTemperature(Boundary bdry, double T) {
    if (!bdry.getValues().has("Static Temperature")) {
      return;
    }
    StaticTemperatureProfile stp = bdry.getValues().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  private void setBC_TI_and_TVR(Boundary bdry, double ti, double tvr){
    if (bdry.getValues().has("Turbulence Intensity")) {
        TurbulenceIntensityProfile tip = bdry.getValues().get(TurbulenceIntensityProfile.class);
        tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti);
    }
    if (bdry.getValues().has("Turbulent Viscosity Ratio")) {
        TurbulentViscosityRatioProfile tvrp = bdry.getValues().get(TurbulentViscosityRatioProfile.class);
        tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr);
    }
  }

  private void setBC_TotalPressure(Boundary bdry, double P) {
    if (!bdry.getValues().has("Total Pressure")) {
      return;
    }
    TotalPressureProfile tpp = bdry.getValues().get(TotalPressureProfile.class);
    tpp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
    tpp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(P);
  }

  private void setBC_TotalTemperature(Boundary bdry, double T) {
    if (!bdry.getValues().has("Total Temperature")) {
      return;
    }
    TotalTemperatureProfile ttp = bdry.getValues().get(TotalTemperatureProfile.class);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  private void setBC_VelocityMagnitude(Boundary bdry, double vel) {
    if (!bdry.getValues().has("Velocity Magnitude")) {
      return;
    }
    VelocityMagnitudeProfile vmp = bdry.getValues().get(VelocityMagnitudeProfile.class);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitVel);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(vel);
  }

  private void setDisplayerEnhancements(Displayer disp) {
    double height = 0.15;
    double width = 0.7;
    String label = "%-#6.3g";
    String label_T = "%-#6.4g";
    double[] pos = new double[] {0.23, 0.05};
    if (isScalar(disp)) {
        ScalarDisplayer scalDisp = (ScalarDisplayer) disp;
        if (scalDisp.getScalarDisplayQuantity().getFieldFunctionName().matches(".*Temperature.*")) {
            label = label_T;
        }
        scalDisp.getLegend().setHeight(height);
        scalDisp.getLegend().setLabelFormat(label);
        scalDisp.getLegend().setWidth(width);
        scalDisp.getLegend().setPositionCoordinate(new DoubleVector(pos));
        scalDisp.getLegend().setShadow(false);
    } else if (isVector(disp)) {
        VectorDisplayer vectDisp = (VectorDisplayer) disp;
        vectDisp.getLegend().setHeight(height);
        vectDisp.getLegend().setLabelFormat(label);
        vectDisp.getLegend().setWidth(width);
        vectDisp.getLegend().setPositionCoordinate(new DoubleVector(pos));
        vectDisp.getLegend().setShadow(false);
    }
    setDisplayerColorBarLevels(disp, 128);
  }

  public void setDisplayerColorBarLevels(Displayer disp, int n) {
    if (isScalar(disp)) {
        ((ScalarDisplayer) disp).getLegend().setLevels(n);
    } else if (isVector(disp)) {
        ((VectorDisplayer) disp).getLegend().setLevels(n);
    }
  }

  public void setGlobalBoundaryMeshRefinement(double min, double tgt) {
    printAction("Setting global mesh refinement on all Boundaries");
    for(Boundary bdry : getAllBoundaries()){
        if(isInterface(bdry)) { continue; }
        setMeshSurfaceSizes(bdry, min, tgt);
    }
  }

  public void setGlobalFeatureCurveMeshRefinement(double min, double tgt) {
    printAction("Setting global mesh refinement on all Feature Curves");
    for(Region region : getAllRegions()){
        sayRegion(region);
        Collection<FeatureCurve> colFC = region.getFeatureCurveManager().getFeatureCurves();
        for(FeatureCurve featCurve : colFC) {
            setMeshFeatureCurveSizes(featCurve, min, tgt);
        }
    }
  }

  private void setInitialCondition_P(PhysicsContinuum phC, double press, boolean verboseOption) {
    printAction("Setting Initial Conditions for Pressure", verboseOption);
    InitialPressureProfile ipp = phC.getInitialConditions().get(InitialPressureProfile.class);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(press);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
  }

  private void setInitialCondition_T(PhysicsContinuum phC, double temp, boolean verboseOption) {
    printAction("Setting Initial Conditions for Temperature", verboseOption);
    sayContinua(phC, verboseOption);
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
    sayContinua(phC, verboseOption);
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
    if (phC.getInitialConditions().has("Turbulent Viscosity Ratio")) {
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
    sayContinua(phC, verboseOption);
    say("Velocity X: " + Vx, verboseOption);
    say("Velocity Y: " + Vy, verboseOption);
    say("Velocity Z: " + Vz, verboseOption);
    VelocityProfile vp = phC.getInitialConditions().get(VelocityProfile.class);
    if (defUnitVel != null) {
        vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setUnits(defUnitVel);
    }
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(Vx, Vy, Vz);
  }

  public void setInterfaceTolerance(Interface intrfPair, double tol) {
    String intrfType = intrfPair.getBeanDisplayName();
    printAction("Setting tolerance for a " + intrfType);
    sayInterface(intrfPair);
    say("  Tolerance: " + tol);
    if(isDirectBoundaryInterface(intrfPair)){
        intrfPair.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
    }
    if(isIndirectBoundaryInterface(intrfPair)){
        intrfPair.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
    }
    sayOK();
  }

  private void setMatPropMeth(ConstantMaterialPropertyMethod cmpm, double val, Units unit) {
    say("Setting a %s %s for %s", cmpm.getPresentationName(),
            cmpm.getMaterialProperty().getPresentationName(),
            cmpm.getMaterialProperty().getParent().getParent().getPresentationName());
    say("  Value: %g %s", val, unit.getPresentationName());
    cmpm.getQuantity().setValue(val);
    cmpm.getQuantity().setUnits(unit);
  }

  /**
   * Sets the Automatic Surface Repair Parameters for the Remesher.
   *
   * @param continua given Mesh Continua.
   * @param minProx given Minimum Proximity. Default is 5%.
   * @param minQual given Minimum Quality. Default is 1%.
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
   * Specifies the Reference Mesh Size for a Mesh Continuum.
   *
   * @param continua given Mesh Continua.
   * @param baseSize reference size.
   * @param unit given units.
   */
  public void setMeshBaseSizes(MeshContinuum continua, double baseSize, Units unit) {
    printAction("Setting Mesh Continua Base Size");
    sayContinua(continua);
    say("  Base Size: " + baseSize + unit.toString());
    continua.getReferenceValues().get(BaseSize.class).setUnits(unit);
    continua.getReferenceValues().get(BaseSize.class).setValue(baseSize);
    sayOK();
  }

  /**
   * @deprecated This method will be removed soon.
   */
  @Deprecated   // v2c
  public void setMeshBoundaryPrismSizes(Boundary bdry, int numLayers, double stretch, double relSize) {
    if(tempMeshSizeSkip){ return; }
    printAction("Setting Custom Boundary Prism Layer");
    if(!isMeshing(bdry, true)){ return; }
    setMeshPrismsParameters(bdry, numLayers, stretch, relSize);
  }

  /**
   * Set a custom Mesh Surface Size for a Feature Curve.
   *
   * @param fc given Feature Curve.
   * @param min given Minimum size.
   * @param tgt give Target size.
   */
  public void setMeshFeatureCurveSizes(FeatureCurve fc, double min, double tgt){
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
  public void setMeshFeatureCurvesSizes(String regexPatt, double min, double tgt){
    printAction(String.format("Setting Mesh Feature Curves by REGEX pattern: \"%s\"", regexPatt));
    int n = 0;
    for (Region reg : getAllRegions(regexPatt, false)) {
        for (FeatureCurve fc : getFeatureCurves(reg, false)) {
            setMeshFeatureCurveSizes(fc, min, tgt);
            n++;
        }
    }
    say("Feature Curves changed: " + n);
    printLine();
  }

  public void setMeshPerRegionFlag(MeshContinuum continua){
    printAction("Setting Mesh Continua as \"Per-Region Meshing\"");
    sayContinua(continua);
    continua.setMeshRegionByRegion(true);
    sayOK();
  }

  public void setMeshPrismsParameters(Boundary bdry, int numLayers, double stretch, double relSize){
    sayBdry(bdry);
    say("  Number of Layers: " + numLayers);
    say("  Stretch Factor: " + String.format("%.2f",stretch));
    say("  Height Relative Size: " + String.format("%.2f%%", relSize));
    if(isDirectBoundaryInterface(bdry)){
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) bdry;
        intrfBdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        intrfBdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        intrfBdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(intrfBdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
    } else if(isIndirectBoundaryInterface(bdry)){
        say("Prisms not available here. Skipping...");
    } else {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        bdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        bdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(bdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
    }
    sayOK();
  }

  private void setMeshPrismsThickness(PrismThickness prismThick, double relSize){
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
   * @param bdry given Boundary.
   * @param growthRate the integer given by one of the variables above.
   */
  public void setMeshSurfaceGrowthRate(Boundary bdry, int growthRate){
    printAction("Setting Custom Surface Growth on Trimmer Mesh");
    sayBdry(bdry);
    bdry.get(MeshConditionManager.class).get(CustomSurfaceGrowthRateOption.class).setEnabled(true);
    bdry.get(MeshValueManager.class).get(CustomSimpleSurfaceGrowthRate.class).getSurfaceGrowthRateOption().setSelected(growthRate);
    sayOK();
  }

  /**
   * Specify Surface Mesh Sizes for a Boundary.
   *
   * @param bdry given Boundary.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Boundary bdry, double min, double tgt){
    setMeshSurfaceSizes(bdry, min, tgt, true);
  }

  /**
   * Loop through all boundaries in the Region and specify Surface Mesh Sizes for them.
   *
   * @param region given Region.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Region region, double min, double tgt) {
    printAction("Setting Mesh Sizes in a Region");
    sayRegion(region);
    say("  " + retMinTargetString(min, tgt));
    printLine();
    for (Boundary bdry : getAllBoundariesFromRegion(region, false, false)) {
        if (!setMeshSurfaceSizes(bdry, min, tgt, false)) {
            say("Skipped!  " + bdry.getPresentationName());
            continue;
        }
        say("OK!  " + bdry.getPresentationName());
    }
    printLine();
    sayOK();
  }

  /**
   * Specify Surface Mesh Sizes for an Interface.
   *
   * @param intrfPair given Interface.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Interface intrfPair, double min, double tgt) {
    sayInterface(intrfPair);
    if (isDirectBoundaryInterface(intrfPair)) {
        DirectBoundaryInterface dbi = (DirectBoundaryInterface) intrfPair;
        dbi.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrfPair.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, true);
    } else {
        say("Not a Direct Boundary Interface. Skipping...");
    }
    sayOK();
  }


  private boolean setMeshSurfaceSizes(Boundary bdry, double min, double tgt, boolean verboseOption){
    sayBdry(bdry, verboseOption, true);
    if (!isMeshing(bdry, verboseOption)) {
        say("Region has no Mesh Continua. Skipping...", verboseOption);
        return false;
    } else if (isIndirectBoundaryInterface(bdry)) {
        say("Skipping...", verboseOption);
        return false;
    } else if(isDirectBoundaryInterface(bdry)) {
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) bdry;
        DirectBoundaryInterface intrfPair = intrfBdry.getDirectBoundaryInterface();
        intrfPair.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrfPair.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, verboseOption);
    } else {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = bdry.get(MeshValueManager.class).get(SurfaceSize.class);
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

  public void setMeshCurvatureNumberOfPoints(MeshContinuum continua, double numPoints){
    printAction("Setting Mesh Continua Surface Curvature");
    sayContinua(continua);
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
   */
  public void setMeshSurfaceSizes(MeshContinuum continua, double min, double tgt){
    printAction("Setting Mesh Continua Surface Sizes");
    sayContinua(continua);
    SurfaceSize srfSize = continua.getReferenceValues().get(SurfaceSize.class);
    setMeshSurfaceSizes(srfSize, min, tgt, true);
    sayOK();
  }

  /**
   * Sets a Mesh Volume Growth Factor for the Poly Mesh.
   *
   * @param continua given Mesh Continua.
   * @param growthFactor given Growth Factor.
   */
  public void setMeshTetPolyGrowthRate(MeshContinuum continua, double growthFactor){
    printAction("Setting Mesh Volume Growth Factor");
    sayContinua(continua);
    say("  Growth Factor: " + growthFactor);
    continua.getReferenceValues().get(VolumeMeshDensity.class).setGrowthFactor(growthFactor);
    sayOK();
  }

  /**
   * Sets the Mesh Trimmer Size To Prism Thickness Ratio.
   *
   * @param continua given Mesh Continua.
   * @param sizeThicknessRatio given Size Thickness Ratio. <i>Default is 5</i>.
   */
  public void setMeshTrimmerSizeToPrismThicknessRatio(MeshContinuum continua, double sizeThicknessRatio){
    printAction("Setting Mesh Trimmer Size To Prism Thickness Ratio...");
    sayContinua(continua);
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
   * @param scaleFactor given Feature Angle. E.g.: 35 degrees.
   */
  public void setMeshWrapperFeatureAngle(MeshContinuum continua, double featAngle) {
    printAction("Setting Wrapper Feature Angle");
    sayContinua(continua);
    say("Feature Angle: " + featAngle + " deg");
    continua.getReferenceValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(featAngle);
    sayOK();
  }

  /**
   * Sets the Surface Wrapper Scale Factor.
   *
   * @param continua given Mesh Continua.
   * @param scaleFactor given Scale Factor. E.g.: 70.
   */
  public void setMeshWrapperScaleFactor(MeshContinuum continua, double scaleFactor) {
    printAction("Setting Wrapper Scale Factor");
    sayContinua(continua);
    if(scaleFactor < 1){
        say("Warning! Scale Factor < 1. Multiplying by 100.");
        scaleFactor *= 100.;
    }
    say("Scale Factor: " + scaleFactor);
    continua.getReferenceValues().get(SurfaceWrapperScaleFactor.class).setScaleFactor(scaleFactor);
    sayOK();
  }

  public void setMeshWrapperVolumeExternal(Region region){
    printAction("Setting Wrapping Region as EXTERNAL in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.EXTERNAL);
    sayOK();
  }

  public void setMeshWrapperVolumeLargestInternal(Region region){
    printAction("Setting Wrapping Region as LARGEST INTERNAL in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.LARGEST_INTERNAL);
    sayOK();
  }

  public void setMeshWrapperVolumeSeedPoints(Region region){
    printAction("Setting Wrapping Region as SEED POINTS in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.SEED_POINT);
    sayOK();
  }

  private void setRadiationEmissivityTransmissivity(Boundary bdry, double emissivity, double transmissivity){
    printAction("S2S Radiation Parameters on Boundary");
    sayBdry(bdry);
    if(!hasRadiationBC(bdry)){
        say("  Radiation Settings not available. Skipping...");
        return;
    }
    EmissivityProfile emP;
    TransmissivityProfile trP = null;
    if(isDirectBoundaryInterface(bdry)){
        DirectBoundaryInterfaceBoundary intrfBdryD = (DirectBoundaryInterfaceBoundary) bdry;
        emP = intrfBdryD.getValues().get(EmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    } else if(isIndirectBoundaryInterface(bdry)){
        IndirectBoundaryInterfaceBoundary intrfBdryI = (IndirectBoundaryInterfaceBoundary) bdry;
        emP = intrfBdryI.getValues().get(EmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    } else {
        emP = bdry.getValues().get(EmissivityProfile.class);
        trP = bdry.getValues().get(TransmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    }
//    catch (Exception e){
//        say("ERROR! Radiation Settings not available. Skipping...");
//        say(e.getMessage());
//    }
  }

  private void setRadiationEmissTransmiss(EmissivityProfile emP, double em, TransmissivityProfile trP, double tr){
    say("  Emissivity: " + em);
    emP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(em);
    if(trP == null){ return; }
    say("  Transmissivity: " + tr);
    trP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tr);
  }

  public void setRadiationParametersS2S(Region region, double angle, double patchProportion){
    printAction("S2S Radiation Parameters on Region");
    sayRegion(region);
    say("  Sharp Angle: " + angle);
    say("  Face/Patch Proportion: " + patchProportion);
    SharpAngle sharpAngle = region.getValues().get(SharpAngle.class);
    sharpAngle.setSharpAngle(angle);
    PatchPerBFaceProportion patchProp = region.getValues().get(PatchPerBFaceProportion.class);
    patchProp.setPatchPerBFaceProportion(patchProportion);
    sayOK();
  }

  public void setRadiationParametersS2S(Boundary bdry, double emissivity, double transmissivity){
    setRadiationEmissivityTransmissivity(bdry, emissivity, transmissivity);
    sayOK();
  }

  public void setRadiationParametersS2S(Boundary bdry, double emissivity, double transmissivity, double temperature){
    setRadiationEmissivityTransmissivity(bdry, emissivity, transmissivity);
    say("  Radiation " + retTemp(temperature));
    try{
        RadiationTemperatureProfile rtP = bdry.getValues().get(RadiationTemperatureProfile.class);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(temperature);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    } catch (Exception e){
        say("ERROR! Radiation Temperature not applicable on: " + bdry.getPresentationName());
    }
    sayOK();
  }

  @Deprecated //in v2c
  private void setSceneBackgroundColor_Gradient(Scene scene, double[] color1, double[] color2) {
    scene.setBackgroundColorMode(1);
    GradientBackgroundColor gbc = scene.getGradientBackgroundColor();
    gbc.setColor1(new DoubleVector(color1));
    gbc.setColor2(new DoubleVector(color2));
    gbc.setMode(0);
    scene.setBackgroundColorMode(0);
  }

  private void setSceneBackgroundColor_Gradient(Scene scene, Color color1, Color color2) {
    scene.setBackgroundColorMode(1);
    GradientBackgroundColor gbc = scene.getGradientBackgroundColor();
    gbc.setColorColor1(color1);
    gbc.setColorColor2(color2);
    gbc.setMode(0);
    scene.setBackgroundColorMode(0);
  }

  @Deprecated //in v2c
  private void setSceneBackgroundColor_Solid(Scene scene, double[] color) {
    scene.setBackgroundColorMode(0);
    scene.getSolidBackgroundColor().setColor(new DoubleVector(color));
  }

  private void setSceneBackgroundColor_Solid(Scene scene, Color color) {
    scene.setBackgroundColorMode(0);
    scene.getSolidBackgroundColor().setColorColor(color);
  }

  /**
   * Sets the Camera View in the Scene.
   *
   * @param scene given Scene.
   * @param cameraView given Camera View setup.
   */
  public void setSceneCameraView(Scene scene, VisView cameraView) {
    if (cameraView == null) {
        return;
    }
    say("Applying Camera View: " + cameraView.getPresentationName());
    scene.getCurrentView().setInput(cameraView.getFocalPoint(), cameraView.getPosition(),
            cameraView.getViewUp(), cameraView.getParallelScale(), cameraView.getProjectionMode(),
            cameraView.getCoordinateSystem(), true);
  }

  /**
   * Updates the Scene to Saves Pictures with a given resolution. Control the updates using
   * {@see #setUpdateFrequency} or {@see #setSceneUpdateFrequency}.<p>
   * Pictures will be saved on {@see #simPath} under a folder called <b>pics_<i>SceneName</i></b>.
   *
   * @param scene given Scene.
   * @param resx given width pixel resolution.
   * @param resy given height pixel resolution.
   * @param picType given picture type. 0 for PNG; 1 for JPEG.
   */
  public void setSceneSaveToFile(Scene scene, int resx, int resy, int picType) {
    printAction("Setting a Scene to Save Pictures");
    say("Scene: " + scene.getPresentationName());
    SceneUpdate su = scene.getSceneUpdate();
    su.setSaveAnimation(true);
    su.setAnimationFilePath(new File(simPath, "pics_" + scene.getPresentationName()));
    su.setAnimationFilenameBase("pic");
    su.setAnimationFileFormat(picType);
    su.setXResolution(resx);
    su.setYResolution(resy);
    sayOK();
  }

  /**
   * Changes the Scene Update Frequency. If the simulation is <b>Steady State</b>, the update is given by
   * <u>Iterations</u>. If <b>Unsteady</b>, the given <u>Time Steps</u> will be used.
   *
   * @param scene given Scene.
   * @param n given update frequency Iterations (Steady) or Time Steps (Unsteady).
   */
  public void setSceneUpdateFrequency(Scene scene, int n) {
    setUpdateFrequency(scene, n);
  }

  private void setSceneLogo(Scene scene) {
    try {
        ((FixedAspectAnnotationProp) scene.getAnnotationPropManager().getAnnotationProp("Logo")).setLocation(1);
    } catch (Exception e) {}
  }

  /**
   * Set the maximum number of iterations in Simulation.
   *
   * @param n given number of iterations.
   */
  public void setSimMaxIterations(int n){
    setSimMaxIterations(n, true);
  }

  private void setSimMaxIterations(int n, boolean verboseOption){
    printAction("Setting Maximum Number of Iterations", verboseOption);
    say("Max Iterations: " + n, verboseOption);
    maxIter = n;
    ((StepStoppingCriterion) getStoppingCriteria("Maximum Steps", false)).setMaximumNumberSteps(n);
    sayOK(verboseOption);
  }

  /**
   * Set a constant Physical timestep for the unsteady solver.
   *
   * @param val given value in default units. See {@see #defUnitTime}.
   */
  public void setSolverPhysicalTimestep(double val) {
    setSolverPhysicalTimestep(val, true, "", false, true);
  }

  /**
   * Set a variable Physical timestep for the unsteady solver, using a Definition.
   *
   * @param definition given definition in default units (see {@see #defUnitTime}). The definition
   * usually comes from a Field Function. E.g.: "$bcMassFlow / 100".
   */
  public void setSolverPhysicalTimestep(String definition) {
    setSolverPhysicalTimestep(0, false, definition, true, true);
  }

  private void setSolverPhysicalTimestep(double val, boolean useVal, String def,
                                            boolean useDefinition, boolean verboseOption) {
    printAction("Setting Physical Timestep", verboseOption);
    if (!isUnsteady()) {
        say("Not Unsteady.");
        return;
    }
    ImplicitUnsteadySolver trn = ((ImplicitUnsteadySolver) getSolver(ImplicitUnsteadySolver.class));
    if (useVal) {
        say("Timestep: " + val, verboseOption);
        trn.getTimeStep().setValue(val);
    }
    if (useDefinition) {
        say("Timestep: " + def, verboseOption);
        trn.getTimeStep().setDefinition(def);
    }
    trn.getTimeStep().setUnits(defUnitTime);
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
   * Changes the Scene Update Frequency. If the simulation is <b>Steady State</b>, the update is given by
   * <u>Iterations</u>. If <b>Unsteady</b>, the given <u>Time Steps</u> will be used.
   *
   * @param scene given Scene.
   * @param n given update frequency Iterations (Steady) or Time Steps (Unsteady).
   */
  public void setUpdateFrequency(Scene scene, int n) {
    printAction("Setting Scene Update Frequency");
    if (isUnsteady()) {
        say("Time Step Frequency: " + n);
        SceneUpdate su = scene.getSceneUpdate();
        su.setUpdateMode(2);
        su.getTimeStepUpdateFrequency().setTimeSteps(n);
    } else {
        say("Iteration Frequency: " + n);
        scene.getSceneUpdate().getIterationUpdateFrequency().setIterations(n);
    }
    sayOK();
  }

  /**
   * Changes the Scene Update Frequency.
   *
   * @param scene given Scene.
   * @param iter given update frequency iterations.
   */
  public void setUpdateFrequency(StarPlot plot, int iter) {
    printAction("Setting Plot Update Frequency");
    say("Iteration Frequency: " + iter);
    if (isResidualPlot(plot))
        ((ResidualPlot) plot).getPlotUpdate().getIterationUpdateFrequency().setIterations(iter);
    if (isMonitorPlot(plot))
        ((MonitorPlot) plot).getPlotUpdate().getIterationUpdateFrequency().setIterations(iter);
    sayOK();
  }

  /**
   * Sleeps for a while.
   * @param ms the length of time to sleep in milliseconds.
   */
  public void sleep(int ms) {
    try {
      Thread.currentThread().sleep(ms);
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
   * @param colPS given Collection of Part Surfaces.
   * @param splitAngle given Split Angle.
   */
  public void splitByAngle(Collection<PartSurface> colPS, double splitAngle) {
    printAction("Splitting Part Surfaces by Angle");
    for (PartSurface ps : colPS) {
        sayPartSurface(ps);
        splitByAngle(ps, splitAngle, false);
    }
    sayOK();
  }

  private void splitByAngle(PartSurface ps, double splitAngle, boolean verboseOption) {
    say("Splitting Part Surface by Angle:", verboseOption);
    sayPartSurface(ps, verboseOption);
    say("  Angle: " + splitAngle);
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    vecPS.add(ps);
    if (isCadPart(ps.getPart())) {
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if (isBlockPart(ps.getPart())) {
        SimpleBlockPart sbp = (SimpleBlockPart) ps.getPart();
        sbp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if (isSimpleCylinderPart(ps.getPart())) {
        SimpleCylinderPart scp = (SimpleCylinderPart) ps.getPart();
        scp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if (isLeafMeshPart(ps.getPart())) {
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if (isMeshOperationPart(ps.getPart())) {
        MeshOperationPart mop = (MeshOperationPart) ps.getPart();
        mop.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    sayOK(verboseOption);
  }

  /**
   * Splits a Part Surface into Non-Contiguous pieces.
   *
   * @param ps give Part Surface.
   * @return Collection of the new splitted Part Surfaces.
   */
  public Collection<PartSurface> splitByNonContiguous(PartSurface ps) {
    return splitByNonContiguous(ps, true);
  }

  /**
   * Splits a collection of Part Surfaces into Non-Contiguous pieces.
   *
   * @param colPS given Collection of Part Surfaces.
   */
  public void splitByNonContiguous(Collection<PartSurface> colPS) {
    printAction("Splitting Non Contiguous Part Surfaces");
    say("Given Part Surfaces: " + colPS.size());
    int n = 0;
    int sum = 0;
    for (PartSurface ps : colPS) {
        sum += splitByNonContiguous(ps, false).size();
        n++;
    }
    say("Overall new Part Surfaces created: " + sum);
    printLine();
  }

  private Collection<PartSurface> splitByNonContiguous(PartSurface ps, boolean verboseOption) {
    printAction("Splitting Non Contiguous Part Surface", verboseOption);
    sayPartSurface(ps);
    String name0 = ps.getPresentationName();
    String mySplit = "__splitFrom__" + name0;
    ps.setPresentationName(mySplit);
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    Object[] objArr = {ps};
    if (isCadPart(ps.getPart())) {
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    if (isLeafMeshPart(ps.getPart())) {
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    for(PartSurface ps1 : ps.getPart().getPartSurfaces()) {
        if (ps == ps1) {
            vecPS.insertElementAt(ps1, 0);
        }
        if (ps1.getPresentationName().matches(mySplit + ".*")) {
            vecPS.add(ps1);
        }
    }
    for (Iterator<PartSurface> it = vecPS.iterator(); it.hasNext();) {
        it.next().setPresentationName(name0);
    }
    sayOK(verboseOption);
    return vecPS;
  }

  /**
   * Splits a collection of Part Surfaces by another collection of Part Curves.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param colPC given Collection of Part Curves.
   * @return The new Part Surfaces.
   */
  public  Collection<PartSurface> splitByPartCurves(Collection<PartSurface> colPS, Collection<PartCurve> colPC) {
    return splitByPartCurves(colPS, colPC, true);
  }

  private Collection<PartSurface> splitByPartCurves(Collection<PartSurface> colPS,
                                        Collection<PartCurve> colPC, boolean verboseOption) {
    printAction("Splitting Part Surfaces by Part Curves", verboseOption);
    //--
    Vector vecOrigNames = new Vector();
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    String tmpName = "__tmpSplitPS__";
    //--
    say("Part Surfaces given: " + colPS.size(), verboseOption);
    for (PartSurface ps : colPS) {
        say("  " + ps.getPresentationName());
        vecOrigNames.add(ps.getPresentationName());
        ps.setPresentationName(tmpName + ps.getPresentationName());
    }
    //--
    say("Part Curves given: " + colPC.size(), verboseOption);
    for (PartCurve pc : colPC) {
        say("  " + pc.getPresentationName());
    }
    //--
    //-- Single Part check
    if (retNumberOfParentParts(colPS) > 1) {
        say("Please provide a Collection within a single Geometry Part", verboseOption);
        say("Returning NULL!", verboseOption);
        return null;
    }
    //--
    GeometryPart gp = colPS.iterator().next().getPart();
    if (isCadPart(gp)) {
        CadPart cp = (CadPart) gp;
        cp.getPartSurfaceManager().splitPartSurfacesByPartCurves((Vector) colPS, (Vector) colPC);
    }
    if (isLeafMeshPart(gp)) {
        LeafMeshPart lmp = (LeafMeshPart) gp;
        lmp.getPartSurfaceManager().splitPartSurfacesByPartCurves((Vector) colPS, (Vector) colPC);
    }
    //--
    //-- Renaming back to original
    Collection<PartSurface> colPS2 = getPartSurfaces(gp, "^" + tmpName + ".*");
    say("Returning " + colPS2.size() + " new Part Surfaces.", verboseOption);
    for (PartSurface ps : colPS2) {
        ps.setPresentationName(ps.getPresentationName().replace(tmpName, ""));
        say("  " + ps.getPresentationName(), verboseOption);
    }
    sayOK(verboseOption);
    return vecPS;
  }

  /**
   * Split a Non-Contiguous region.
   *
   * @param region given Region.
   */
  public void splitRegionNonContiguous(Region region){
    printAction("Splitting Non Contiguous Regions");
    MeshContinuum mshc = null;
    PhysicsContinuum phc = null;
    sayRegion(region);
    Object[] objArr = {region};
    sim.getMeshManager().splitNonContiguousRegions(new NeoObjectVector(objArr), minConFaceAreas);
    // Loop into the generated Regions created by Split
    for (Region reg : getAllRegions("^" + region.getPresentationName() + " \\d{1,2}", false)){
        if (region == reg) { continue; }
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
  public String str2regex(String text){
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

  private Units updateDefaultUnit(Units unit1, Units unit2, String descr) {
    Units defUnit = null;
    String defDescr = "Not initialized or not available";
    if (unit1 == null) {
        defUnit = unit2;
    } else {
        defUnit = unit1;
    }
    try {
        defDescr = defUnit.toString();
    } catch (Exception e) {}
    say("Default Unit " + descr + ": " + defDescr);
    return defUnit;
  }

  private void updateOrCreateNewUnits(boolean verboseOption){
    printAction("Updating/Creating Units", verboseOption);
    unit_C = queryUnit("C", verboseOption);
    unit_Dimensionless = queryUnit("", verboseOption);
    unit_F = queryUnit("F", verboseOption);
    unit_K = queryUnit("K", verboseOption);
    unit_h = queryUnit("hr", verboseOption);
    unit_m = queryUnit("m", verboseOption);
    unit_m2 = queryUnit("m^2", verboseOption);
    unit_min = queryUnit("min", verboseOption);
    unit_mm = queryUnit("mm", verboseOption);
    unit_N = queryUnit("N", verboseOption);
    unit_kph = queryUnit("kph", verboseOption);
    unit_kgpm3 = queryUnit("kg/m^3", verboseOption);
    unit_kgps = queryUnit("kg/s", verboseOption);
    unit_mps = queryUnit("m/s", verboseOption);
    unit_mps2 = queryUnit("m/s^2", verboseOption);
    unit_rpm = queryUnit("rpm", verboseOption);
    unit_Pa = queryUnit("Pa", verboseOption);
    unit_Pa_s = queryUnit("Pa-s", verboseOption);
    unit_radps = queryUnit("radian/s", verboseOption);
    unit_s = queryUnit("s", verboseOption);
    unit_Wpm2K = queryUnit("W/m^2-K", verboseOption);
    /*    CUSTOM UNITS      */
    int[] massList = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] massFlowList = {1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] pressureList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] velList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] viscList = {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] volFlowList = {0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    /*    MASS UNITS [M]    */
    unit_g = addUnit("g", "gram", 0.001, massList);
    /*    MASS FLOW UNITS [M/T]    */
    unit_kgph = addUnit("kg/h", "kilogram per hour", 1/3600, massFlowList);
    unit_kgpmin = addUnit("kg/min", "kilogram per minute", 1/60, massFlowList);
    unit_gpmin = addUnit("g/min", "gram per minute", 1E-3/60, massFlowList);
    unit_gps = addUnit("g/s", "gram per second", 1E-3, massFlowList);
    /*    PRESSURE UNITS [P]    */
    //--- http://www.sensorsone.co.uk/pressure-units-conversion.html
    unit_cmH2O = addUnit("cmH2O", "cm of water", 98.0665, pressureList);
    unit_dynepcm2 = addUnit("dyne/cm^2", "dyne per square centimeter", 0.1, pressureList);
    unit_mmH2O = addUnit("mmH2O", "mm of water", 9.80665, pressureList);
    /*    VISCOSITY UNITS [P*T]    */
    unit_P = addUnit("P", "Poise", 1E-1, viscList);
    unit_cP = addUnit("cP", "centiPoise", 1E-3, viscList);
    /*    VOLUMETRIC FLOW UNITS [V/T]    */
    unit_lph = addUnit("l/h", "liter per hour", 1E-3/3600, volFlowList);
    unit_lpmin = addUnit("l/min", "liter per minute", 1E-3/60, volFlowList);
    unit_lps = addUnit("l/s", "liter per second", 1E-3, volFlowList);
    printLine();
    //-- Assigning default units if they haven't been initialized
    defUnitAccel = updateDefaultUnit(defUnitAccel, unit_mps2, "Acceleration");
    defUnitArea = updateDefaultUnit(defUnitArea, unit_m2, "Area");
    defUnitDen = updateDefaultUnit(defUnitDen, unit_kgpm3, "Density");
    defUnitForce = updateDefaultUnit(defUnitForce, unit_N, "Force");
    defUnitHTC = updateDefaultUnit(defUnitHTC, unit_Wpm2K, "Heat Transfer Coefficient");
    defUnitLength = updateDefaultUnit(defUnitLength, unit_mm, "Length");
    defUnitMFR = updateDefaultUnit(defUnitMFR, unit_kgps, "Mass Flow Rate");
    defUnitPress = updateDefaultUnit(defUnitPress, unit_Pa, "Pressure");
    defUnitTemp = updateDefaultUnit(defUnitTemp, unit_C, "Temperature");
    defUnitTime = updateDefaultUnit(defUnitTime, unit_s, "Temperature");
    defUnitVel = updateDefaultUnit(defUnitVel, unit_mps, "Velocity");
    defUnitVisc = updateDefaultUnit(defUnitVisc, unit_Pa_s, "Viscosity");
    printLine();
    sayOK();
  }

  public void updateMeshContinuaVector(){
    printAction("Querying number of Mesh Continuas");
    vecMeshContinua = sim.getContinuumManager().getObjectsOf(MeshContinuum.class);
    say("Found: " + vecMeshContinua.size());
    if(vecMeshContinua.size() > 0){
        for(int i = 0; i < vecMeshContinua.size(); i++){
            say("  " + vecMeshContinua.get(i).getPresentationName());
        }
    }
  }

  /**
   * It makes the Plots prettier. Updates all Lines in all Plots to the given Thickness. Useful
   * for making fancier plots.
   *
   * @param thickness the given Plot Line thickness/width.
   * @deprecated in v2c.
   */
  public void updatePlotsAppearance(int thickness) {
    thickness = Math.max(1, thickness);
    printAction("Updating the Plots Appearance.");
    say("Thickness: " + thickness);
    Font fontTitle = new java.awt.Font("SansSerif", 0, 18);
    Font fontOther = new java.awt.Font("SansSerif", 0, 14);
    for (StarPlot sp : sim.getPlotManager().getObjects()) {
        say("Plot Name: " + sp.getPresentationName());
        sp.setTitleFont(fontTitle);
        sp.getLegend().setMapLineStyle(true);
        sp.getLegend().setFont(fontOther);
        sp.getAxes().getXAxis().getLabels().setFont(fontOther);
        sp.getAxes().getXAxis().getTitle().setFont(fontOther);
        sp.getAxes().getYAxis().getLabels().setFont(fontOther);
        sp.getAxes().getYAxis().getTitle().setFont(fontOther);
        for (DataSet ds : sp.getDataSetGroup().getDataSets()) {
            String status = "Not Updated!";
            if (ds.getLineStyle().getWidth() != thickness) {
                status = "Updated.";
                ds.getLineStyle().setWidth(thickness);
            }
            say("  " + ds.getPresentationName() + ": " + status);
        }
    }
    sayOK();
  }

  @Deprecated // v2c
  public void updatePhysicsGravityAndReferenceTemperature(PhysicsContinuum phC){
    printAction("Updating Gravity Vector and Referente Temperature");
    sayContinua(phC);
    say("  Gravity: " + gravity.toString());
    say("  Refence " + retTemp(Tref));
    phC.getReferenceValues().get(Gravity.class).setComponents(gravity[0], gravity[1], gravity[2]);
    phC.getReferenceValues().get(ReferenceTemperature.class).setUnits(defUnitTemp);
    phC.getReferenceValues().get(ReferenceTemperature.class).setValue(Tref);
    sayOK();
  }

  @Deprecated // v2c
  public void updatePhysicsReferencePressure(PhysicsContinuum phC){
    printAction("Updating Referente Pressure");
    sayContinua(phC);
    say("  Refence Pressure: " + Pref + defUnitPress.getPresentationName());
    phC.getReferenceValues().get(ReferencePressure.class).setUnits(defUnitPress);
    phC.getReferenceValues().get(ReferencePressure.class).setValue(Pref);
    sayOK();
  }

  private void updateReferenceValues(PhysicsContinuum phC) {
    updateOrCreateNewUnits(false);
    printAction("Updating Referente Values");
    sayContinua(phC);
    //for (ClientServerObject cso : phC.getReferenceValues().getObjects()) {
    //    say(cso.getPresentationName());
    //}
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

  private void updateSimVar(){
    if(sim != null){ return; }
    sim = getActiveSimulation();
    updateSolverSettings();
  }

  /**
   * Updates all Solver Settings. E.g.: Relaxation Factors, Linear Ramps, etc... Currently, the
   * following variables can be set:<ul>
   * <li>Segregated: {@link #urfVel}, {@link #urfP}, {@link #urfFluidEnrgy}, {@link #urfSolidEnrgy},
   * {@link #urfVOF}, {@link #urfKEps}, {@link #rampURF}, and some others...;</li>
   * <li>Steady State: {@link #maxIter};</li>
   * <li>Unsteady: {@link #trnTimestep}, {@link #trnInnerIter}, {@link #trnMaxTime}.</li>
   * </ul>
   */
  public void updateSolverSettings(){
    if (sim.getSolverManager().isEmpty()) {
        return;
    }
    printAction("Updating Solver Settings");
    if (sim.getSolverManager().has("Implicit Unsteady")) {
        ((InnerIterationStoppingCriterion) getStoppingCriteria("Maximum Inner Iterations",
                                            false)).setMaximumNumberInnerIterations(trnInnerIter);
        ((PhysicalTimeStoppingCriterion) getStoppingCriteria("Maximum Physical Time",
                                                        false)).getMaximumTime().setValue(trnMaxTime);
        ((PhysicalTimeStoppingCriterion) getStoppingCriteria("Maximum Physical Time",
                                                        false)).getMaximumTime().setUnits(defUnitTime);
        ((StepStoppingCriterion) getStoppingCriteria("Maximum Steps", false)).setIsUsed(false);
        ImplicitUnsteadySolver ius = ((ImplicitUnsteadySolver) sim.getSolverManager().getSolver(ImplicitUnsteadySolver.class));
        ius.getTimeStep().setValue(trnTimestep);
        ius.getTimeStep().setUnits(defUnitTime);
        String timeDiscr = "1st Order";
        if (trn2ndOrder) {
            ius.getTimeDiscretizationOption().setSelected(TimeDiscretizationOption.SECOND_ORDER);
            timeDiscr = "2nd Order";
        }
        say("Time Discretization: " + timeDiscr);
        say("Maximum Inner Iterations: %d", trnInnerIter);
        say("Maximum Inner Iterations: %d", trnInnerIter);
        say("Maximum Physical Time: %g %s", trnMaxTime, defUnitTime.getPresentationName());
        say("Physical Timestep: %g %s", trnTimestep, defUnitTime.getPresentationName());
    } else {
        setSimMaxIterations(maxIter, false);
        say("Maximum Number of Iterations: %d", maxIter);
    }
    if (sim.getSolverManager().has("Segregated Flow")) {
        SegregatedFlowSolver flowSolv = ((SegregatedFlowSolver) sim.getSolverManager().getSolver(SegregatedFlowSolver.class));
        flowSolv.getVelocitySolver().setUrf(urfVel);
        flowSolv.getVelocitySolver().getAMGLinearSolver().setConvergeTol(amgConvTol);
        PressureSolver pSolv = flowSolv.getPressureSolver();
        pSolv.setUrf(urfP);
        pSolv.getAMGLinearSolver().setConvergeTol(amgConvTol);
        say("URF Velocity: %g", urfVel);
        say("URF Pressure: %g", urfP);
    }
    if (sim.getSolverManager().has("Segregated VOF")) {
        SegregatedVofSolver vofSolv = ((SegregatedVofSolver) sim.getSolverManager().getSolver(SegregatedVofSolver.class));
        vofSolv.setUrf(urfVOF);
        vofSolv.getAMGLinearSolver().setConvergeTol(amgConvTol);
        say("URF VOF: %g", urfVOF);
    }
    if (sim.getSolverManager().has("Segregated Energy")) {
        SegregatedEnergySolver enrgySolv = ((SegregatedEnergySolver) sim.getSolverManager().getSolver(SegregatedEnergySolver.class));
        enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        enrgySolv.setFluidUrf(urfFluidEnrgy);
        enrgySolv.setSolidUrf(urfSolidEnrgy);
        enrgySolv.getAMGLinearSolver().setConvergeTol(amgConvTol);
        say("URF Fluid Energy: %g", urfFluidEnrgy);
        say("URF Solid Energy: %g", urfSolidEnrgy);
        if(rampURF) {
            enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
            enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
            LinearRampCalculator rampFl = ((LinearRampCalculator) enrgySolv.getFluidRampCalculatorManager().getCalculator());
            LinearRampCalculator rampSld = ((LinearRampCalculator) enrgySolv.getSolidRampCalculatorManager().getCalculator());
            rampFl.setStartIteration(urfRampFlIterBeg);
            rampFl.setEndIteration(urfRampFlIterEnd);
            rampFl.setInitialRampValue(urfRampFlBeg);
            rampSld.setStartIteration(urfRampSldIterBeg);
            rampSld.setEndIteration(urfRampSldIterEnd);
            rampSld.setInitialRampValue(urfRampSldBeg);
            say("Linear Ramp Fluid Energy:");
            say("   Start/End Iteration: %d/%d", urfRampFlIterBeg, urfRampFlIterEnd);
            say("   Initial URF: %g", urfRampFlBeg);
            say("Linear Ramp Solid Energy:");
            say("   Start/End Iteration: %d/%d", urfRampSldIterBeg, urfRampSldIterEnd);
            say("   Initial URF: %g", urfRampSldBeg);
        }
    }
    if (sim.getSolverManager().has("K-Epsilon Turbulence")) {
        KeTurbSolver keSolv = ((KeTurbSolver) sim.getSolverManager().getSolver(KeTurbSolver.class));
        keSolv.setUrf(urfKEps);
        keSolv.getAMGLinearSolver().setConvergeTol(amgConvTol);
        say("URF K-Epsilon: %g", urfKEps);
    }
    sayOK();
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


  private void writeCameraViews(String filename, String cameras, boolean verboseOption) {
    printAction("Writing Camera Views", verboseOption);
    ArrayList<String> strArray = new ArrayList<String>();
    try {
        strArray = new ArrayList(Arrays.asList(cameras.split(camSplitCharBetweenCams)));
    } catch (Exception e) {
        say("ERROR! Could not read Cameras. Make sure the correct data is provided.");
        return;
    }
    say("Cameras given: " + strArray.size());
    if (strArray.size() == 0) {
        say("Correct data not provided. Nothing will be written.");
        return;
    }
    String[] cams = strArray.toArray(new String[strArray.size()]);
    writeData(new File(simPath, filename), cams, verboseOption);
  }

  /**
   * Writes String data to a file.
   *
   * @param filename given filename. File will be saved in {@see #simPath} folder.
   * @param data given array of Strings.
   */
  public void writeData(String filename, String[] data) {
    writeData(new File(simPath, filename), data, true);
  }

  private void writeData(File file, String[] data, boolean verboseOption) {
    say("Writing contents to a file...", verboseOption);
    say("  File: " + file.getAbsolutePath(), verboseOption);
    BufferedWriter fileWriter = null;
    try {
        if (file.exists()) {
            say("  Already exists. Overwriting...", verboseOption);
        }
        fileWriter = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < data.length; i++) {
            fileWriter.write(data[i]);
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

  /***************************************************
   * Private definitions
   ***************************************************/
  private String tempCamName = "_tmpCam";

  /***************************************************
   * Global definitions
   ***************************************************/
  boolean autoCloseFreeEdges = true;
  boolean colorByRegion = true;
  boolean checkMeshQualityUponSurfaceMeshGeneration = false;
  boolean checkMeshQualityUponSurfaceMeshImport = false;
  boolean createMeshSceneUponSurfaceMeshGeneration = false;
  @Deprecated //-- in v2c
  boolean fineTesselationOnImport = false;
  /** Save intermediate files when troubleshooting a macro? */
  boolean saveIntermediates = true;
  boolean singleBoundary = false;
  Boundary bdry = null, bdry1 = null, bdry2 = null, bdry3 = null;
  CadPart cadPrt = null, cadPrt1 = null, cadPrt2 = null, cadPrt3 = null;
  CellSurfacePart cellSet = null;
  CompositePart compPart = null, compPart1 = null, compPart2 = null, compPart3 = null;
  DirectBoundaryInterface intrfPair = null, intrfPair1 = null, intrfPair2 = null, intrfPair3 = null;
  double mshSharpEdgeAngle = 30;
  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  double[] camFocalPoint = {0., 0., 0.};
  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  double[] camPosition = {0., 0., 0.};
  /** Useful predefined variable for storing Camera Settings. See {@see #createCamView} for more. */
  double[] camViewUp = {0., 0., 0.};
  /** Useful predefined variable for storing Coordinates. */
  double[] coord1 = {0., 0., 0.};
  /** Useful predefined variable for storing Coordinates. */
  double[] coord2 = {0., 0., 0.};
  /** Useful predefined variable for storing Coordinates. */
  double[] coord3 = {0., 0., 0.};
  /** Useful predefined variable for storing Coordinates. */
  double[] coord4 = {0., 0., 0.};
  /** Useful predefined variable for storing Coordinates. */
  double[] point = {0., 0., 0.};
  FeatureCurve featCrv = null;
  /** A path containing CAD files. */
  File cadPath = null;
  /** A path containing DBS files. */
  File dbsPath = null;
  File myFile = null;
  File simFile = null;
  int autoSaveMaxFiles = 2;
  int autoSaveFrequencyIter = 1000;
  int colourByPart = 4;
  int colourByRegion = 2;
  int partColouring = colourByPart;
  /** Default Width resolution when hardcopying pictures with {@see #} */
  @Deprecated
  int picResX = 800;
  /** Default Height resolution when hardcopying pictures with {@see #} */
  @Deprecated
  int picResY = 600;
  int savedWithSuffix = 0;
  /** Some useful Global Variables: Displayer. */
  Displayer disp = null, disp1 = null, disp2 = null, disp3 = null;
  /** Some useful Global Variables: Field Function. */
  FieldFunction ff = null, ff1 = null, ff2 = null, ff3 = null;
  /** Some useful Global Variables: Geometry Parts. */
  GeometryPart geomPrt = null, geomPrt1 = null, geomPrt2 = null, geomPrt3 = null;
  /** Some useful Global Variables: Interfaces. */
  Interface intrf = null, intrf1 = null, intrf2 = null, intrf3 = null;
  /** Some useful Global Variables: Leaf Mesh Parts. */
  LeafMeshPart leafMshPrt = null, leafMshPrt1 = null, leafMshPrt2 = null, leafMshPrt3 = null;
  /** Some useful Global Variables: Mesh Continuas. */
  MeshContinuum mshCont = null, mshCont1 = null, mshCont2 = null;
  /** Some useful Global Variables: Mesh Operation Parts. */
  MeshOperationPart mshOpPrt = null, mshOpPrt1 = null, mshOpPrt2 = null, mshOpPrt3 = null;
  /** Some useful Global Variables: Monitors. */
  Monitor mon = null, mon1 = null, mon2 = null;
  /** Some useful Global Variables: Monitor Plots. */
  MonitorPlot monPlot = null, monPlot1 = null, monPlot2 = null;
  /** Some useful Global Variables: Part Curves. */
  PartCurve partCrv = null, partCrv1 = null, partCrv2 = null, partCrv3 = null;
  /** Some useful Global Variables: Part Surfaces. */
  PartSurface partSrf = null, partSrf1 = null, partSrf2 = null, partSrf3 = null;
  /** Some useful Global Variables: Planes. */
  PlaneSection plane = null, plane1 = null, plane2 = null;
  /** Some useful Global Variables: Physics Continuas. */
  PhysicsContinuum physCont = null, physCont1 = null, physCont2 = null, physCont3 = null;
  /** Some useful Global Variables: Regions. */
  Region region = null, region1 = null, region2 = null, region3 = null;
  /** Some useful Global Variables: Reports. */
  Report rep = null, rep1 = null, rep2 = null, rep3 = null;
  /** Some useful Global Variables: Report Monitors. */
  ReportMonitor repMon = null, repMon1 = null, repMon2 = null, repMon3 = null;
  /** Some useful Global Variables: Scenes. */
  Scene scene = null, scene1 = null, scene2 = null, scene3 = null;
  /** Some useful Global Variables: Simple Block Parts. */
  SimpleBlockPart simpleBlkPrt = null, simpleBlkPrt1 = null, simpleBlkPrt2 = null, simpleBlkPrt3 = null;
  /** Some useful Global Variables: Simple Cylinder Parts. */
  SimpleCylinderPart simpleCylPrt = null, simpleCylPrt1 = null, simpleCylPrt2 = null, simpleCylPrt3 = null;
  /** Some useful Global Variables: Simple Sphere Parts. */
  SimpleSpherePart simpleSphPrt = null, simpleSphPrt1 = null, simpleSphPrt2 = null, simpleSphPrt3 = null;
  /** Some useful Global Variables: Visualization View (Camera View). */
  VisView camView = null, camView1 = null, camView2 = null, camView3 = null;
  /** Some useful Global Variables: Visualization View (Camera View). */
  VisView vv = null, vv1 = null, vv2 = null, vv3 = null;
  String autoSaveSeparator = "_backupIter";
  String contNameAir = "Air";
  String contNameAirBoussinesq = "Air Boussinesq";
  String contNameAluminum = "Aluminum";
  String contNameSteel = "Steel";
  String contNamePoly = "Poly Mesh";
  String contNameTrimmer = "Trimmer Mesh";
  String contNameThinMesher = "Thin Mesher";
  String contNameWater = "Water";
  String dbsSubDir = "parts";
  String noneString = "none";
  String sayPreffixString = "[*]";
  @Deprecated //- v2c
  String simName = null;
  //--
  //-- Useful declarations.
  //--
  /** Useful Boundary Condition names declarations. */
  String bcBottom = "bottom";
  /** Useful Boundary Condition names declarations. */
  String bcChannel = "channel";
  /** Useful Boundary Condition names declarations. */
  String bcCold = "cold";
  /** Useful Boundary Condition names declarations. */
  String bcFloor = "floor";
  /** STAR-CCM+ likes to call Part Surfaces as <b>Faces</b>, right? */
  String bcFaces = "Faces";
  /** Useful Boundary Condition names declarations. */
  String bcHot = "hot";
  /** Useful Boundary Condition names declarations. */
  String bcInlet = "inlet";
  /** Useful Boundary Condition names declarations. */
  String bcOutlet = "outlet";
  /** Useful Boundary Condition names declarations. */
  String bcSym = "symmetry";
  /** Useful Boundary Condition names declarations. */
  String bcSym1 = "symmetry1";
  /** Useful Boundary Condition names declarations. */
  String bcSym2 = "symmetry2";
  /** Useful Boundary Condition names declarations. */
  String bcTop = "top";
  /** Useful Boundary Condition names declarations. */
  String bcWall = "wall";
  /** Useful Boundary Condition names declarations. */
  String bcWalls = "walls";
  /** Simulation title. It is used when saving. See {@see #saveSim}. */
  String simTitle = null;
  String simPath = null;
  String string = "", string1 = "", string2 = "", string3 = "", text = "";
  String inlet = bcInlet;
  String outlet = bcOutlet;
  String wall = bcWall;
  String walls = bcWalls;
  /**
   * This variable is the current session in STAR-CCM+. It is the first variable initialized in
   * {@see #}. If not initialized, an error will occur soon enough.
   */
  Simulation sim = null;
  /***************************************************
   * Physics
   ***************************************************/
  /** Default Camera when creating new Scenes. For more see {@see #createCamView}. */
  VisView defCamView = null;
  /** Default unit of Acceleration, when using {@link .}. */
  Units defUnitAccel = null;
  /** Default unit of Area, when using {@link .}. */
  Units defUnitArea = null;
  /** Default unit of Density, when using {@link .}. */
  Units defUnitDen = null;
  /** Default unit of Force, when using {@link .}. */
  Units defUnitForce = null;
  /** Default unit of Heat Transfer Coefficient, when using {@link .}. */
  Units defUnitHTC = null;
  /** Default unit of Length, when using {@link .}. */
  Units defUnitLength = null;
  /** Default unit of Mass Flow Rate, when using {@link .}. */
  Units defUnitMFR = null;
  /** Default unit of Pressure, when using {@link .}. */
  Units defUnitPress = null;
  /** Default unit of Temperature, when using {@link .}. */
  Units defUnitTemp = null;
  /** Default unit of Time, when using {@link .}. */
  Units defUnitTime = null;
  /** Default unit of Velocity, when using {@link .}. */
  Units defUnitVel = null;
  /** Default unit of Viscosity, when using {@link .}. */
  Units defUnitVisc = null;
  /** Celsius unit (Temperature). */
  Units unit_C = null;
  /** CentiPoise unit (Viscosity). */
  Units unit_cP = null;
  /** Centimeter of Water unit (Pressure). */
  Units unit_cmH2O = null;
  /** Dimensionless unit. */
  Units unit_Dimensionless = null;
  /** Fahrenheit unit (Temperature). */
  Units unit_F = null;
  /** Dyne per Square Centimeter unit (Pressure). */
  Units unit_dynepcm2 = null;
  /** Gram unit (Mass). */
  Units unit_g = null;
  /** Gram per Minute unit (Mass/Time). */
  Units unit_gpmin = null;
  /** Gram per Second unit (Mass/Time). */
  Units unit_gps = null;
  /** Kelvin unit (Temperature). */
  Units unit_K = null;
  /** Hour unit (Time). */
  Units unit_h = null;
  /** Kilometer per Hour unit (Velocity). */
  Units unit_kph = null;
  /** Kilogram per Hour unit (Mass/Time). */
  Units unit_kgph = null;
  /** Kilogram per Cubic Meter unit (Density). */
  Units unit_kgpm3 = null;
  /** Kilogram per Second unit (Mass/Time). */
  Units unit_kgps = null;
  /** Kilogram per Minute unit (Mass/Time). */
  Units unit_kgpmin = null;
  /** Liter per Hour unit (Volume/Time). */
  Units unit_lph = null;
  /** Liter per Minute unit (Volume/Time). */
  Units unit_lpmin = null;
  /** Liter per Second unit (Volume/Time). */
  Units unit_lps = null;
  /** Meter unit (Length). */
  Units unit_m = null;
  /** Square Meter unit (Area). */
  Units unit_m2 = null;
  /** Minute unit (Time). */
  Units unit_min = null;
  /** Millimeter unit (Length). */
  Units unit_mm = null;
  /** Millimeter of Water unit (Pressure). */
  Units unit_mmH2O = null;
  /** Meter per Second unit (Velocity). */
  Units unit_mps = null;
  /** Meter per Square Second unit (Velocity / Time). */
  Units unit_mps2 = null;
  /** Newton (Force). */
  Units unit_N = null;
  /** Poise unit (Viscosity). */
  Units unit_P = null;
  /** Pascal unit (Pressure). */
  Units unit_Pa = null;
  /** Pascal x Second unit (Viscosity). */
  Units unit_Pa_s = null;
  /** Radian per Second unit (Angular Velocity). */
  Units unit_radps = null;
  /** Rotation per Minute unit (Angular Velocity). */
  Units unit_rpm = null;
  /** Second unit (Time). */
  Units unit_s = null;
  /** Watt per Square Meter x Kelvin unit (Heat Transfer Coefficient). */
  Units unit_Wpm2K = null;
  /***************************************************
   * Physics
   ***************************************************/
  double[] gravity = {0., -9.81, 0.};           // m/s^2
  /** Initial Pressure value in default units. */
  double p0 = 0.0;                              // Pa
  /** Initial Turbulent Intensity for RANS Models. */
  double ti0 = 0.05;
  /** Initial Turbulent Viscosity Ratio for RANS Models. */
  double tvr0 = 10.0;
  /** Initial Velocity Scale for RANS Turbulence Models in default units. */
  double tvs0 = 0.5;
  /** Initial Velocity Array in default units. */
  double[] v0 = {0., 0., 0.};
  /** Minimum clipping Temperature in default units. */
  double clipMinT = -50;
  /** Maximum clipping Temperature in default units. */
  double clipMaxT = 3000;
  /** Density of Air in default units. See {@see #defUnitDen} */
  double denAir = 1.18415;
  /** Density of Water in default units. See {@see #defUnitDen} */
  double denWater = 997.561;
  /** Initial Temperature value for Fluids in default units. */
  double fluidT0 = 22.;
  /** Initial Temperature value for Solids in default units. */
  double solidsT0 = 60.;
  /** Reference Altitude array of values in default units. See {@see #defUnitLength}. */
  double[] refAlt = new double[] {0, 0, 0};
  /** Reference Density value in default units. See {@see #defUnitDen}. */
  double refDen = denAir;
  /** Reference Pressure value in default units. See {@see #defUnitPress}. */
  double refP = 101325.;
  /** Reference Temperature value in default units. See {@see #defUnitPress}. */
  double refT = 22.;
  /** Reference Pressure value in default units.
   * @deprecated in v2c.
   */
  double Pref = 101325.;
  /** Reference Temperature value in default units.
   * @deprecated in v2c.
   */
  double Tref = 22.;
  double radEmissivity = 0.8;                   // default
  double radTransmissivity = 0.;                // default
  double radSharpAngle = 150.;                  // default
  double radPatchProp = 100.;                   // default
  /** Viscosity of Air in default units. See {@see #defUnitVisc} */
  double viscAir = 1.85508E-5;
  /** Viscosity of Water in default units. See {@see #defUnitVisc} */
  double viscWater = 8.8871E-4;
  /***************************************************
   * Preprocessing and Meshing
   ***************************************************/
  boolean skipMeshGeneration = false;
  boolean thinMeshIsPolyType = true;
  boolean tempMeshSizeSkip = false;
  /** Minimum Feature Curve Relative Size (<b>%</b>).*/
  double featCurveMeshMin = 25;
  /** Target Feature Curve Relative Size (<b>%</b>).*/
  double featCurveMeshTgt = 100;
  /** Mesh Base (<i>Reference</i>) Size in default units. */
  double mshBaseSize = 3.0;                     // mm
  /** Mesh Growth Factor for Tets/Polys. (<i>default = 1.0</i> ) */
  double mshGrowthFactor = 1.0;
  double mshOpsTol = 1e-4;                      // m
  double mshProximityPointsInGap = 2.0;
  double mshProximitySearchFloor = 0.0;         // mm
  /** Surface Mesh Number of Points per Circle. (<i>default = 36</i> ) */
  int mshSrfCurvNumPoints = 36;              // points / curve
  /** Surface Mesh Growth Rate. (<i>default = 1.3</i> ) */
  double mshSrfGrowthRate = 1.3;
  /** Surface Mesh Minimum Relative Size (<b>%</b>). */
  double mshSrfSizeMin = 25;
  /** Surface Mesh Target Relative Size (<b>%</b>). */
  double mshSrfSizeTgt = 100;
  /** Maximum Trimmer Relative Size (<b>%</b>). */
  double mshTrimmerMaxCelSize = 10000;
  /** Surface Wrapper Feature Angle (<b>deg</b>). */
  double mshWrapperFeatureAngle = 30.;
  /** Surface Wrapper Scale factor in relative value. E.g.: (<i>70%</i>). */
  double mshWrapperScaleFactor = 100.;          // (%)
  double prismsLyrChoppPerc = 25.0;             // (%)
  double prismsMinThickn = 5.0;                 // (%)
  /** Prism Layers Near Core Aspect Ratio (NCLAR). Default = 0.0. */
  double prismsNearCoreAspRat = 0.5;
  /** Prism Layers Relative Size (<b>%</b>). */
  double prismsRelSizeHeight = 30.;             // (%)
  /** Prism Stretch Ratio. Default = 1.5. */
  double prismsStretching = 1.2;
  int intrfInt = 3;
  int maxInterfaces = 10000;
  /** How many Prism Layers? Default = 2.*/
  int prismsLayers = 2;
  /** How many layers for the Thin Mesher? Default = 2.*/
  int thinMeshLayers = 2;
  @Deprecated   // v2c
  String mshTrimmerGrowthRate = "medium";
  /***************************************************
   * Remove Invalid Cells Settings
   ***************************************************/
  boolean aggressiveRemoval = false;
  double minFaceValidity = 0.51;
  double minCellQuality = 1e-8;
  double minVolChange = 1e-10;
  int minContigCells = 1;
  double minConFaceAreas = 0.;
  double minCellVolume = 0.;
  /***************************************************
   * Solver Settings
   ***************************************************/
  /** AMG convergence tolerance. Default = 0.1. */
  final double amgConvTol = 0.01;
  /** Default Maximum Iterations */
  final int maxIter0 = 1000;
  /** Default URF for Velocity */
  final double urfVel0 = 0.8;
  /** Default URF for Pressure */
  final double urfP0 = 0.2;
  /** Default URF for Fluid Energy */
  final double urfFluidEnrgy0 = 0.9;
  /** Default URF for Solid Energy */
  final double urfSolidEnrgy0 = 0.99;
  /** Default URF for K-Epsilon */
  final double urfKEps0 = 0.8;
  /** Default URF for VOF */
  final double urfVOF0 = 0.9;
  /** Courant Number for the Coupled Solver. */
  double CFL = 5;
  /** CFL Ramp Beginning Iteration for the Coupled Solver. */
  int cflRampBeg = 1;
  /** CFL Ramp Ending Iteration for the Coupled Solver. */
  int cflRampEnd = 10;
  /** Initial CFL for Ramping the Coupled Solver. */
  double cflRampBegVal = 0.6;
  /** Grid Sequencing Initialization Max Levels for the Coupled Solver. Default = 10. */
  int gsiMaxLevels = 10;
  /** Grid Sequencing Initialization Iterations per level for the Coupled Solver. Default = 50. */
  int gsiMaxIterations = 50;
  /** Grid Sequencing Initialization Convergence Tolerance for the Coupled Solver. Default = 0.05 */
  double gsiConvTol = 0.05;
  /** Grid Sequencing Initialization Courant number for the Coupled Solver. Default = 5.0 */
  double gsiCFL = 5.0;
  /** Maximum Iterations */
  int maxIter = maxIter0;
  /** Ramp the Under Relaxation Factor for Courant? */
  boolean rampCFL = false;
  /** Ramp the Under Relaxation Factor for Temperature? */
  boolean rampURF = false;
  /** Second Order discretization on time when Unsteady? */
  boolean trn2ndOrder = true;
  /** Maximum Inner Iterations when using Unsteady. */
  int trnInnerIter = 15;
  /** Maximum Physical time when using Unsteady. See {@see #defUnitTime}. */
  double trnMaxTime = 10.;
  /** Physical time step when using Unsteady. See {@see #defUnitTime}. */
  double trnTimestep = 0.001;
  /** URF for Velocity. */
  double urfVel = urfVel0;
  /** URF for Pressure. */
  double urfP = urfP0;
  /** URF for Fluid Energy. */
  double urfFluidEnrgy = urfFluidEnrgy0;
  /** URF for Solid Energy. */
  double urfSolidEnrgy = urfSolidEnrgy0;
  /** URF for K-Epsilon. */
  double urfKEps = urfKEps0;
  /** Initial URF for Ramping Fluid Energy. */
  double urfRampFlBeg = 0.6;
  /** Initial URF for Ramping Solid Energy. */
  double urfRampSldBeg = 0.7;
  /** Initial Iteration for Ramping URF in Fluid Energy. */
  int urfRampFlIterBeg = 100;
  /** Final Iteration for Ramping URF in Fluid Energy. */
  int urfRampFlIterEnd = 1000;
  /** Initial Iteration for Ramping URF in Solid Energy. */
  int urfRampSldIterBeg = 100;
  /** Final Iteration for Ramping URF in Solid Energy. */
  int urfRampSldIterEnd = 1000;
  /** URF for VOF. */
  double urfVOF = urfVOF0;
  /** VOF Sharpening Factor. */
  double vofSharpFact = 0.0;
  /** VOF Angle Factor. */
  double vofAngleFact = 0.05;
  /** VOF CFL Lower limit for the HRIC scheme. */
  double vofCFL_l = 0.5;
  /** VOF CFL Upper limit for the HRIC scheme. */
  double vofCFL_u = 1.0;
  /***************************************************
   * Immutable definitions
   ***************************************************/
  final private String camSplitCharBetweenCams = ";";
  final private String camSplitCharBetweenFields = "|";
  final private String camVecs = retRepeatString("%.6e,%.6e,%.6e ", 3);
  final private String camFormat = ("%s " + camVecs + "%.6e").replaceAll(" ", camSplitCharBetweenFields);
  final private String unitFormat = "%s: %s (%s)";
  /** Origin coordinates (0, 0, 0). */
  final double[] coord0 = {0., 0., 0.};
  /** The Original Laboratory Coordinate System. Useful somewhere. */
  CoordinateSystem csys0 = null;
  /** Just an empty DoubleVector. Useful somewhere. */
  DoubleVector dv0 = new DoubleVector(coord0);
  /** The Original Laboratory Coordinate System. Useful somewhere. */
  LabCoordinateSystem lab0 = null;
  /** Just an empty Vector of Objects. Useful somewhere. */
  final private Object[] emptyObj = new Object[] {};
  /** Just an empty NeoObjectVector. Useful somewhere. */
  final private NeoObjectVector emptyNeoObjVec = new NeoObjectVector(emptyObj);
  /** Pressure variable name inside STAR-CCM+. */
  final String varP = "Pressure";
  /** Temperature variable name inside STAR-CCM+. */
  final String varT = "Temperature";
  /** Velocity variable name inside STAR-CCM+. */
  final String varVel = "Velocity";
  /** XYZ Coordinates as a String Array (X, Y, Z). Immutable. */
  final String[] xyzCoord = {"X", "Y", "Z"};
  /***************************************************
   * Miscellaneous definitions
   ***************************************************/
  String[] arrayPartSurfacesSplitAngleException = {};
  /** Useful Global Vector Variables for storing Boundaries. */
  Vector<Boundary> vecBdry = new Vector<Boundary>();
  /** Useful Global Vector Variables for storing Boundaries. */
  Vector<Boundary> vecBdry1 = new Vector<Boundary>();
  /** Useful Global Vector Variables for storing Boundaries. */
  Vector<Boundary> vecBdry2 = new Vector<Boundary>();
  /** Useful Global Vector Variables for storing Boundaries. */
  Vector<Boundary> vecBdry3 = new Vector<Boundary>();
  /** Useful Global Vector Variables for storing Cad Parts. */
  Vector<CadPart> vecCadPrt = new Vector<CadPart>();
  /** Useful Global Vector Variables for storing Cad Parts. */
  Vector<CadPart> vecCadPrt1 = new Vector<CadPart>();
  /** Useful Global Vector Variables for storing Cad Parts. */
  Vector<CadPart> vecCadPrt2 = new Vector<CadPart>();
  /** Useful Global Vector Variables for storing Cad Parts. */
  Vector<CadPart> vecCadPrt3 = new Vector<CadPart>();
  /** Useful Global Vector Variables for storing Composite Parts. */
  Vector<CompositePart> vecCompPrt = new Vector<CompositePart>();
  /** Useful Global Vector Variables for storing Composite Parts. */
  Vector<CompositePart> vecCompPrt1 = new Vector<CompositePart>();
  /** Useful Global Vector Variables for storing Composite Parts. */
  Vector<CompositePart> vecCompPrt2 = new Vector<CompositePart>();
  /** Useful Global Vector Variables for storing Composite Parts. */
  Vector<CompositePart> vecCompPrt3 = new Vector<CompositePart>();
  /** Useful Global Vector Variables for storing Geometry Parts. */
  Vector<GeometryPart> vecGeomPrt = new Vector<GeometryPart>();
  /** Useful Global Vector Variables for storing Geometry Parts. */
  Vector<GeometryPart> vecGeomPrt1 = new Vector<GeometryPart>();
  /** Useful Global Vector Variables for storing Geometry Parts. */
  Vector<GeometryPart> vecGeomPrt2 = new Vector<GeometryPart>();
  /** Useful Global Vector Variables for storing Geometry Parts. */
  Vector<GeometryPart> vecGeomPrt3 = new Vector<GeometryPart>();
  /** Useful Global Vector Variables for storing Leaf Mesh Parts. */
  Vector<LeafMeshPart> vecLeafMshPrt = new Vector<LeafMeshPart>();
  /** Useful Global Vector Variables for storing Mesh Parts. */
  Vector<MeshPart> vecMshPrt = new Vector<MeshPart>();
  /** Useful Global Vector Variables for storing Mesh Parts. */
  Vector<MeshPart> vecMshPrt1 = new Vector<MeshPart>();
  /** Useful Global Vector Variables for storing Mesh Parts. */
  Vector<MeshPart> vecMshPrt2 = new Vector<MeshPart>();
  /** Useful Global Vector Variables for storing Mesh Parts. */
  Vector<MeshPart> vecMshPrt3 = new Vector<MeshPart>();
  /**
   * Global Vector where all Mesh Continuas are stored when {@link .} is initialized.
   * @deprecated in v2c.
   */
  Vector<MeshContinuum> vecMeshContinua = new Vector<MeshContinuum>();
  /** Useful Global Vector Variables for storing Part Curves. */
  Vector<PartCurve> vecPartCrv = new Vector<PartCurve>();
  /** Useful Global Vector Variables for storing Part Curves. */
  Vector<PartCurve> vecPartCrv1 = new Vector<PartCurve>();
  /** Useful Global Vector Variables for storing Part Curves. */
  Vector<PartCurve> vecPartCrv2 = new Vector<PartCurve>();
  /** Useful Global Vector Variables for storing Part Curves. */
  Vector<PartCurve> vecPartCrv3 = new Vector<PartCurve>();
  /** Useful Global Vector Variables for storing Part Surfaces. */
  Vector<PartSurface> vecPartSrf = new Vector<PartSurface>();
  /** Useful Global Vector Variables for storing Part Surfaces. */
  Vector<PartSurface> vecPartSrf1 = new Vector<PartSurface>();
  /** Useful Global Vector Variables for storing Part Surfaces. */
  Vector<PartSurface> vecPartSrf2 = new Vector<PartSurface>();
  /** Useful Global Vector Variables for storing Part Surfaces. */
  Vector<PartSurface> vecPartSrf3 = new Vector<PartSurface>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<Region> vecReg = new Vector<Region>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<Region> vecReg1 = new Vector<Region>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<Region> vecReg2 = new Vector<Region>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<Region> vecReg3 = new Vector<Region>();
  /** Useful Global Vector Variables for storing Camera Views. */
  Vector<VisView> vecCam = new Vector<VisView>();
  /** Useful Global Vector Variables for storing Camera Views. */
  Vector<VisView> vecCam1 = new Vector<VisView>();
  /** Useful Global Vector Variables for storing Camera Views. */
  Vector<VisView> vecCam2 = new Vector<VisView>();
  /** Useful Global Vector Variables for storing Camera Views. */
  Vector<VisView> vecCam3 = new Vector<VisView>();
  /** Global Vector where a Physic Continua is stored every time one is created.
   * @deprecated in v2c.
   */
  Vector<PhysicsContinuum> vecPhysicsContinua = new Vector<PhysicsContinuum>();
  /** Useful Global Vector Variables for storing Report Monitors. */
  Vector<ReportMonitor> vecRepMon = new Vector<ReportMonitor>();
  /** Useful Global Vector Variables for storing Objects. */
  Vector vecObj = new Vector(), vecObj1 = new Vector(), vecObj2 = new Vector(), vecObj3 = new Vector();
  @Deprecated //-- v2c
  MeshContinuum mshContPoly = null;
  @Deprecated //-- v2c
  MeshContinuum mshContTrimmer = null;
  FilenameFilter dbsFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
        // Return files ending with dbs -- with ignore case group (?i)
        return name.matches("(?i).*dbs");
    }
  };
  /** Good variable when using with {@see #evalLinearRegression}. */
  double[] xx = new double[] {};
  /** Good variable when using with {@see #evalLinearRegression}. */
  double[] yy = new double[] {};
  /*
   * A few colors. Use java.awt.Color() instead. E.g:
   */
  /** White Smoke codes according to {@see java.awt.Color} class. */
  Color colorWhiteSmoke = new Color(245, 245, 245);
  @Deprecated   // in v2c
  double[] color_light_grey = new double[] {0.8274509906768799, 0.8274509906768799, 0.8274509906768799};
  @Deprecated   // in v2c
  double[] color_slate_grey = new double[] {0.43921568989753723, 0.501960813999176, 0.5647059082984924};
  @Deprecated   // in v2c
  double[] color_smoke = new double[] {0.9333333373069763, 0.9333333373069763, 0.9333333373069763};
  @Deprecated   // in v2c
  double[] color_white = new double[] {1, 1, 1};
  @Deprecated   // in v2c
  IntVector colorLightGray = new IntVector(new int[] {211, 211, 211});
}
