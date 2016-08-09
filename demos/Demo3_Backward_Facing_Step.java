import macroutils.*;

/**
 * Simple pseudo 2D Demo of the classic Backward Facing Step test case.
 * 
 * Geometry in XY plane:
 *                                         L_t
 *      +---------------------------------------------------------------+
 *    h |                                                               |
 *      +-----------+                                                   | H
 *                  |                                                   |
 *      * O(0,0,0)  +---------------------------------------------------+
 *                                         L_c
 * 
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo3_Backward_Facing_Step extends MacroUtils {

  private final double L_t = 700;
  private final double L_c = 500;
  private final double h = 50;
  private final double H = 100;
  private final double depth = 0.25;
  private final double Re_h = 189;                   //-- Reynolds Number f(h)
    
  public void execute() {
    _initUtils();
    simTitle = "Demo3_BackwardFacingStep";
    prep1_createParts();
    prep2_BCsAndMesh();
    prep3_setPost();
    runCase(true);
    _finalize();
  }
  
  void prep1_createParts() {
    coord1 = new double[] {L_t, H, depth};
    cadBody1 = create3DCad_Block(coord0, coord1, unit_mm, "Block1");
    cadPrt1 = getCadPart(cadBody1);
    getPartSurface(cadPrt1, "x0").setPresentationName(bcInlet);
    getPartSurface(cadPrt1, "x1").setPresentationName(bcOutlet);
    getPartSurface(cadPrt1, "y1").setPresentationName(bcTop);
    getPartSurface(cadPrt1, "y0").setPresentationName(bcBottom);
    combinePartSurfaces(getPartSurfaces(cadPrt1, "z.*")).setPresentationName(bcSym);
    //--
    coord2 = new double[] {L_t - L_c, H - h, depth};
    cadBody2 = create3DCad_Block(coord0, coord2, unit_mm, "Block2");
    cadPrt2 = getCadPart(cadBody2);
    getPartSurface(cadPrt2, "x1").setPresentationName(bcStep);
    getPartSurface(cadPrt2, "y1").setPresentationName(bcStep);
    combinePartSurfaces(getPartSurfaces(cadPrt2, bcStep + ".*")).setPresentationName(bcStep);
    //--
    geometryParts.add(cadPrt1);
    geometryParts.add(cadPrt2);
    //-- 
    //-- Mesh Operations
    mshOpPrt = meshOperationSubtractParts(geometryParts, cadPrt1);
    mshOpPrt.setPresentationName(regionName);
    //-- 
    region = assignPartToRegion(mshOpPrt);
  }
  
  void prep2_BCsAndMesh() {
    //-- Mesh settings
    mshBaseSize = depth * 10.;
    mshSrfSizeMin = 100;
    mshSrfSizeTgt = 100;
    mshTrimmerMaxCelSize = 100;
    prismsLayers = 3;
    prismsRelSizeHeight = 50;
    //-- 
    mshCont = createMeshContinua_Trimmer();
    disableSurfaceProximityRefinement(mshCont);
    setMeshTrimmerSizeToPrismThicknessRatio(mshCont, 2.0);
    createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal();
    setSolverAggressiveURFs();
    //-- 
    bdry = getBoundary(".*" + bcInlet);
    double vel = Re_h * 1.855e-5 / (1.184 * h * defUnitLength.getConversion());
    setBC_VelocityMagnitudeInlet(bdry, vel, 0, 0.01, 1);
    //-- 
    bdry = getBoundary(".*" + bcOutlet);
    setBC_PressureOutlet(bdry, 0., 0., 0.01, 1);
    //-- 
    setBC_Symmetry(getBoundary(".*" + bcSym));
    //-- 
    genVolumeMesh();
  }
  
  void prep3_setPost() {
    defCamView = readCameraView("myCam|3.502958e-01,3.866198e-02,-8.724052e-04|3.502958e-01,3.866198e-02,1.366150e+00|0.000000e+00,1.000000e+00,0.000000e+00|1.986988e-01|1");
    //-- Stopping Criteria
    bdry = getBoundary(".*" + bcInlet);
    createReportMassFlowAverage(bdry, "P_in", getFieldFunction(varP), defUnitPress);
    createStoppingCriteria(repMon, "Asymptotic", 1e-5, 50);
    updEvent1 = createUpdateEvent_Iteration(1, 10);
    setUpdateEvent(repMon, updEvent1);
    //-- Contour Plot
    namedObjects.add(getBoundary(".*" + bcSym));
    scene1 = createScene_Scalar(namedObjects, getFieldFunction("Turb.*Visc.*Ratio"), unit_Dimensionless, true);
    scene2 = createScene_Vector(namedObjects, true);
    updEvent2 = createUpdateEvent_Iteration(10, 0);
    setUpdateEvent(scene1, updEvent2);
    setUpdateEvent(scene2, updEvent2);
    openAllPlotsAndScenes();
  }
  
  private String bcStep = "step";
  private String regionName = "Channel";
  
}
