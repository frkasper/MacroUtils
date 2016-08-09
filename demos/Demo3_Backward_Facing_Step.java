package macroutils;

/**
 * Simple Demo of the classic Backward Facing Step test case.
 * 
 * Geometry in XY plane:
 *                                         L_t
 *      +---------------------------------------------------------------+
 *    h |                                                               |
 *      +-----------+                                                   | H
 *                  |                                                   |
 *      * O(0,0,0)  +---------------------------------------------------+
 *                                         L_c
 * @author fabiok
 */
public class Demo3_Backward_Facing_Step extends MacroUtils {

  private final double L_t = 700;
  private final double L_c = 500;
  private final double h = 50;
  private final double H = 100;
  private final double depth = 5;
  private final double Re_h = 189;                   //-- Reynolds Number f(h)
    
  public void execute() {
    _initUtils();
    simTitle = "Demo3_BackwardFacingStep";
    saveIntermediates = false;
    prep1_createAndSplit();
    prep2_BCsAndMesh();
    prep3_setPost();
    runCase(true);
    _finalize();
  }
  
  void prep1_createAndSplit() {
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        return;
    }
    coord1 = new double[] {L_t, H, depth};
    simpleBlkPrt1 = createShapePartBlock(coord0, coord1, unit_mm, "Block1");
    simpleBlkPrt1.getPartSurfaces().iterator().next().setPresentationName(bcWall);
    coord2 = new double[] {L_t - L_c, H - h, depth};
    simpleBlkPrt2 = createShapePartBlock(coord0, coord2, unit_mm, "Block2");
    simpleBlkPrt2.getPartSurfaces().iterator().next().setPresentationName(bcStep + "_");
    vecGeomPrt.add(simpleBlkPrt1);
    vecGeomPrt.add(simpleBlkPrt2);
    //-- 
    double tol = 0.01;
    String regexStep = bcStep + ".*";
    String regexWall = bcWall + ".*";
    //-- Get the Channel PSs
    splitByAngle(simpleBlkPrt1.getPartSurfaces(), 80);
    partSrf = getPartSurface(simpleBlkPrt1, "Min", "X", tol, regexWall);
    partSrf.setPresentationName(bcInlet);
    partSrf = getPartSurface(simpleBlkPrt1, "Max", "X", tol, regexWall);
    partSrf.setPresentationName(bcOutlet);
    partSrf = getPartSurface(simpleBlkPrt1, "Min", "Y", tol, regexWall);
    partSrf.setPresentationName(bcBottom);
    partSrf = getPartSurface(simpleBlkPrt1, "Max", "Y", tol, regexWall);
    partSrf.setPresentationName(bcTop);
    partSrf = getPartSurface(simpleBlkPrt1, "Min", "Z", tol, ".*");
    partSrf.setPresentationName(bcSym1);
    partSrf = getPartSurface(simpleBlkPrt1, "Max", "Z", tol, ".*");
    partSrf.setPresentationName(bcSym2);
    //-- Get the Step and Upstream Channel PSs
    splitByAngle(simpleBlkPrt2.getPartSurfaces(), 80);
    partSrf = getPartSurface(simpleBlkPrt2, "Max", "Y", tol, regexStep);
    partSrf.setPresentationName(bcChannel);
    partSrf = getPartSurface(simpleBlkPrt2, "Max", "X", tol, regexStep);
    partSrf.setPresentationName(bcStep);
    //-- Demo Parts
    coord1 = new double[] {0.75 * L_t, 0, 0};
    coord2 = new double[] {1.1 * L_t, h, depth};
    vecGeomPrt2.add(createShapePartBlock(coord1, coord2, unit_mm, "BlockDemo"));
    coord1 = new double[] {0.75 * L_t, 0, 0};
    coord2 = new double[] {0.75 * L_t, 0, depth};
    vecGeomPrt2.add(createShapePartCylinder(coord1, coord2, h, h, unit_mm, "CylDemo"));
    //-- 
    //-- Mesh Operations
    mshOpPrt = meshOperationUniteParts(vecGeomPrt2);
    mshOpPrt = meshOperationSubtractParts(vecGeomPrt, simpleBlkPrt1);
    mshOpPrt.setPresentationName(regionName);
    //-- 
    assignPartToRegion(mshOpPrt, true);
    saveSimWithSuffix("a_created");
  }
  
  void prep2_BCsAndMesh() {
    if (queryVolumeMesh() != null) {
        sayLoud("Volume Mesh already exists. Skipping prep2...");
        return;
    }
    region = sim.getRegionManager().getRegions().iterator().next();
    //-- Mesh settings
    mshBaseSize = depth;
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
    //-- 
    bdry = getBoundary(bcInlet);
    double vel = Re_h * 1.855e-5 / (1.184 * h * defUnitLength.getConversion());
    setBC_VelocityMagnitudeInlet(bdry, vel, 0, 0.01, 1);
    //-- 
    bdry = getBoundary(bcOutlet);
    setBC_PressureOutlet(bdry, 0., 0., 0.01, 1);
    //-- 
    setBC_Symmetry(getBoundary(bcSym1));
    setBC_Symmetry(getBoundary(bcSym2));
    //-- 
    genVolumeMesh();
    createScene_Mesh().openScene();
    saveSimWithSuffix("c_meshed");
  }
  
  void prep3_setPost() {
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //-- Camera Settings (Recorded a Macro from GUI to get the Coordinates)
    camFocalPoint = new double[] {0.35335640807924795, 0.09846122537045113, -9.66031784488397E-5};
    camPosition = new double[] {0.35335640807924795, 0.09846122537045113, 1.3660254041915463};
    camViewUp = new double[] {0.0, 1.0, 0.0};
    defCamView = createCamView(camFocalPoint, camPosition, camViewUp, 0.16673195568938243, "MyCamera");
    //-- Stopping Criteria
    bdry = getBoundary(bcInlet);
    createReportMassFlowAverage(bdry, "P_in", varP, defUnitPress);
    createStoppingCriteria(repMon, 1e-5, 50, false);
    //-- Contour Plot
    vecObj.add(getBoundary(bcSym1));
    scene = createScene_Scalar(vecObj, varP, defUnitPress, true);
    //setUpdateFrequency(scene, 10);
    scene = createScene_Vector(vecObj, true);
    setUpdateFrequency(scene, 10);
    setUpdateFrequency(getPlot("Residual.*"), 20);
    getPlot("Residual.*").open();
    scene.openScene();
    //openAllPlotsAndScenes();
    saveSimWithSuffix("e_Ready");
  }
  
  private String bcBottom = "bottom";
  private String bcChannel = "channel";
  private String bcStep = "step";
  private String bcTop = "top";
  private String regionName = "Channel";
  
}
