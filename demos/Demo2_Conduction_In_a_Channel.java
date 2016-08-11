import macroutils.*;
import star.vis.*;

/**
 * Simple Demo of a 3D conduction in a channel.
 * 
 * Geometry:
 *          
 *         +---------------------------------------------------------------+
 *      D /                                                               /|
 *       /                               L                               / |
 *      +---------------------------------------------------------------+  |
 *      |                                                               |  +
 *    H |                                                               | / 
 *      |                                                               |/
 *      *---------------------------------------------------------------+
 *      O(0,0,0)
 * 
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo2_Conduction_In_a_Channel extends MacroUtils {

  private final double length = 1000;
  private final double height = 100;
  private final double depth = 50;
    
  public void execute() {
    _initUtils();
    simTitle = "Demo2_Channel";
    prep1_createPart();
    prep2_createRegion();
    prep3_BCsAndMesh();
    prep4_setPost();
    runCase(true);
    _finalize();
  }
  
  void prep1_createPart() {
    defCamView = readCameraView("myCam|5.003809e-01,1.413476e-02,4.204865e-03|-1.256561e+00,7.844162e-01,1.422375e+00|2.630476e-01,9.462717e-01,-1.880847e-01|2.280761e-01|1");
    cadBody = create3DCad_Block(coord0, new double[] {length, height, depth}, unit_mm);
    geometryParts.add(getGeometryPart(".*"));
  }
  
  void prep2_createRegion() {
    getPartSurface("x0").setPresentationName(bcHot);
    getPartSurface("x1").setPresentationName(bcCold);
    combinePartSurfaces(getPartSurfaces("y.*")).setPresentationName(bcWall);
    combinePartSurfaces(getPartSurfaces("z.*")).setPresentationName(bcSym);
    region = assignAllPartsToRegion();
  }
    
  void prep3_BCsAndMesh() {
    //-- Physics/Mesh settings
    mshBaseSize = depth / 5;
    mshTrimmerMaxCelSize = 100;
    urfSolidEnrgy = 1.0;
    //-- 
    autoMshOp = createMeshOperation_AutomatedMesh(geometryParts, getMeshers(true, false, TRIMMER, false), "My Mesh");
    physCont = createPhysicsContinua(_3D, _STEADY, _SOLID, _SEGREGATED, _INCOMPRESSIBLE, _THERMAL, _NOT_APPLICABLE, false, false, false);
    updateSolverSettings();
    //-- 
    bdry = getBoundary(bcHot);
    setBC_ConvectionWall(bdry, 50., 50);
    //-- 
    bdry = getBoundary(bcCold);
    setBC_ConvectionWall(bdry, 20., 15);
    //-- 
    setBC_Symmetry(getBoundary(bcSym));
    //-- 
    genVolumeMesh();
  }
  
  void prep4_setPost() {
    //-- Stopping Criteria
    createReportVolumeAverage(getRegion(".*"), varT, getFieldFunction(varT), defUnitTemp);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, 50);
    //-- Contour Plot
    namedObjects.addAll(getAllBoundaries());
    scene = createScene_Scalar(namedObjects, getFieldFunction(varT), defUnitTemp, true);
    ScalarDisplayer sd = (ScalarDisplayer) getDisplayer(scene, ".*");
    sd.setDisplayMeshBoolean(true);
    updEvent = createUpdateEvent_Iteration(50, 0);
    setUpdateEvent(scene, updEvent);
    openAllPlotsAndScenes();
  }
  
}
