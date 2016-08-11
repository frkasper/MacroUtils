import macroutils.*;
import java.util.*;
import star.common.*;

/**
 * Another complete workflow automated with Macro Utils. 3D flow over a wing.
 * 
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo8_Half_Wing extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo8_HalfWing";
    saveIntermediates = false;
    prep1_importGeometryAndSplit();
    prep2_createRegionBCsAndMesh();
    prep3_setPost();
    openAllPlotsAndScenes();
    setMonitorsNormalizationOFF();
    runCase(true);
    _finalize();
  }
  
  void prep1_importGeometryAndSplit() {
    vv1 = readCameraView("cam1|2.627790e-01,1.688267e-01,-8.768673e-02|3.042959e+00,2.949006e+00,2.692493e+00|0.000000e+00,1.000000e+00,0.000000e+00|1.257075e+00");
    vv2 = readCameraView("cam2|9.022700e-02,-5.806686e-03,-2.014223e-01|7.395474e-01,6.435137e-01,4.478981e-01|0.000000e+00,1.000000e+00,0.000000e+00|2.935941e-01");
    defCamView = vv1;
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        geomPrt = getGeometryPart(".*");
        return;
    }
    defTessOpt = TESSELATION_VERY_FINE;
    importCADPart("WING.x_b");
    //-- Split the single Part Surface by its Part Curve
    geomPrt = getGeometryPart(".*");
    geomPrt.setPresentationName("WING");
    splitByPartCurves(geomPrt);
    //-- Get Airfoils and other Wing BCs
    partSrf = getPartSurface(geomPrt, "Max", "X", 50);      // 50mm of tolerance should be good.
    partSrf.setPresentationName(bcAirfoil);
    partSrf = getPartSurface(geomPrt, "Min", "X", 50);
    partSrf.setPresentationName(bcAirfoil2);
    partSrf = getPartSurface(geomPrt, "Max", "Area", 50);
    partSrf.setPresentationName(bcWing);
    //-- Leftover is the trail.
    getPartSurface(geomPrt, "Faces.*").setPresentationName(bcTrail);
    //-- 
    saveSimWithSuffix("a_import");
  }

  void prep2_createRegionBCsAndMesh() {
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    //--
    //-- Mesh settings
    mshBaseSize = 30;
    mshSrfSizeMin = 1. / 16. * 100.;
    mshSrfSizeTgt = 64 * 100.;  // Geometric progression in a ratio of 2. This is how Trimmer grows.
    mshSrfCurvNumPoints = 60;
    mshSrfGrowthRate = 1.15;
    prismsLayers = 3;
    prismsRelSizeHeight = 30;
    mshTrimmerMaxCelSize = mshSrfSizeTgt;
    mshCont = createMeshContinua_Trimmer();
    //--
    //-- How big respective to the biggest length scale (dx, dy or dz)
    double farFieldRelSize = 10; 
    //--
    //-- Sphere is created relative to the biggest length scale (dx, dy or dz)
    simpleSphPrt = createShapePartSphere(getPartSurfaces(geomPrt), farFieldRelSize, regionName);
    //--
    //-- Block is created relative to each length scale (dx, dy and dz)
    double[] blkCorner1 = {-2 * farFieldRelSize, -50 * farFieldRelSize, -50 * farFieldRelSize};
    double[] blkCorner2 = {-0.5, 50 * farFieldRelSize, 50 * farFieldRelSize};
    simpleBlkPrt = createShapePartBlock(getPartSurfaces(geomPrt), blkCorner1, blkCorner2, bcSym);
    mshOpPrt = meshOperationSubtractParts(getAllGeometryParts(), simpleSphPrt);
    region = assignPartToRegion(mshOpPrt);
    region.setPresentationName(regionName);
    //--
    //-- Volumetric Controls
    blkCorner1 = new double[] {-.1, -30, -100};
    blkCorner2 = new double[] {4, 30, 100};
    simpleBlkPrt = createShapePartBlock(getPartSurfaces(geomPrt), blkCorner1, blkCorner2, "VC1");
    blkCorner1 = new double[] {-.1, -5, -100};
    blkCorner2 = new double[] {1, 5, 15};
    simpleBlkPrt1 = createShapePartBlock(getPartSurfaces(geomPrt), blkCorner1, blkCorner2, "VC2");
    blkCorner1 = new double[] {-.1, -1, -10};
    blkCorner2 = new double[] {0.25, 1, 0.75};
    simpleBlkPrt2 = createShapePartBlock(getPartSurfaces(geomPrt), blkCorner1, blkCorner2, "VC3");
    blkCorner1 = new double[] {-.1, -.25, -3};
    blkCorner2 = new double[] {0.0625, .5, 0.125};
    simpleBlkPrt3 = createShapePartBlock(getPartSurfaces(geomPrt), blkCorner1, blkCorner2, "VC4");
    createMeshVolumetricControl(mshCont, simpleBlkPrt, "VC1", 16 * 100.);
    createMeshVolumetricControl(mshCont, simpleBlkPrt1, "VC2", new double[] {800, 400, 1600});
    createMeshVolumetricControl(mshCont, simpleBlkPrt2, "VC3", new double[] {400, 100, 400});
    createMeshVolumetricControl(mshCont, simpleBlkPrt3, "VC4", new double[] {200, 25, 200});
    //-- 
    //-- Create Physics Continua and convert to Coupled Solver
    enableCQR = true;
    refT = 22.;
    physCont = createPhysics_AirSteadySegregatedIdealGasSA_AllWall();
    CFL = 150;
    cflRampEnd = 50;
    cflRampBegVal = 5;
    enableCoupledSolver(physCont, true, true);
    setInitialCondition_Velocity(physCont, 0, 0, -wingVel);
    //**************************************** 
    //-- BOUNDARY CONDITIONS
    //**************************************** 
    //-- 
    //-- Free Stream
    bdry = getBoundary(".*" + regionName + ".*");
    setBC_FreeStream(bdry, flowDirection, 0.0288, 0., refT, ti0, tvr0);
    //-- 
    //-- Trail
    bdry = getBoundary(".*" + bcTrail + ".*");
    setMeshSurfaceSizes(bdry, mshSrfSizeMin, mshSrfSizeMin);
    createMeshWakeRefinement(mshCont, bdry, 250, flowDirection, new double[] {0, mshSrfSizeMin, 100});
    //-- Airfoil
    bdry = getBoundary(".*" + bcAirfoil + ".*");
    setMeshSurfaceSizes(bdry, mshSrfSizeMin, 4 * mshSrfSizeMin);
    //-- Wing
    bdry = getBoundary(".*" + bcWing + ".*");
    setMeshSurfaceSizes(bdry, mshSrfSizeMin, 200.);
    //-- Symmetry
    bdry2 = getBoundary(".*" + bcSym + ".*");
    setBC_Symmetry(bdry2);
    //--
    genVolumeMesh();
    saveSimWithSuffix("b_meshed");
  }
  
  void prep3_setPost() {
    region = getRegion(".*");
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //--
    namedObjects.addAll(getWingBoundaries());
    namedObjects.add(getBoundary(".*" + bcSym + ".*"));
    //--
    //-- Mesh Scene
    coord1 = new double[] {0, 0, -300};
    plane = createSectionPlaneZ(coord1, "Plane Z");
    namedObjects2.add(plane);
    cellSet = createCellSet(namedObjects2, "Cells on Plane Z");
    namedObjects.add(cellSet);
    scene = createScene_Mesh(namedObjects);
    scene.setPresentationName("Volume Mesh");
    //-- Update Events
    updEvent1 = createUpdateEvent_Iteration(10, 0);
    updEvent2 = createUpdateEvent_Iteration(1, 20);
    //--
    //-- Contour & Vector Plots
    namedObjects.remove(cellSet);
    scene = createScene_Scalar(namedObjects, getFieldFunction(varPC), unit_Dimensionless, true);
    scene.setPresentationName("Pressure Coefficient Wing");
    setUpdateEvent(scene, updEvent1);
    scene2 = createScene_Vector(namedObjects, true);
    scene2.setPresentationName("Vector Wing");
    setUpdateEvent(scene, updEvent1);
    //--
    //-- Stopping Criteria
    int stopIter = 50;
    boundaries.addAll(getWingBoundaries());
    rep = createReportFrontalArea(boundaries, "Frontal Area", new double[] {0, 1, 0}, new double[] {0, 0, -1});
    rep2 = createReportFrontalArea(boundaries, "Upper Area", new double[] {0, 0, 1}, new double[] {0, -1, 0});
    createReportForceCoefficient(boundaries, "C_d", wingVel, rep.getReportMonitorValue(), new double[] {0, 0, -1});
    setUpdateEvent(repMon, updEvent2);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, stopIter);
    createReportForceCoefficient(boundaries, "C_l", wingVel, rep2.getReportMonitorValue(), new double[] {0, 1, 0});
    setUpdateEvent(repMon, updEvent2);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, stopIter);
    saveSimWithSuffix("e_Ready");
  }

  private ArrayList<Boundary> getWingBoundaries() {
    return getBoundaries(".*wing.*", false);
  }
  
  //-- 
  //-- Boundary Condition Names and other Misc stuff
  //-- 
  private String bcWing = "wing body";
  private String bcAirfoil = "wing airfoil";
  private String bcAirfoil2 = "wing airfoil NotUsed";
  private String bcTrail = "wing trail";
  private String regionName = "FarField";
  double wingVel = 10.;
  private double[] flowDirection = {0, 0, -1};
 
}