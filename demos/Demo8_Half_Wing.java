package macroutils;

import java.util.*;
import star.common.*;

public class Demo8_Half_Wing extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo8_HalfWing";
    saveIntermediates = false;
    prep1_importGeometryAndSplit();
    prep2_createRegionBCsAndMesh();
    prep3_setPost();
    openAllPlotsAndScenes();
    runCase();
    _finalize();
  }
  
  void prep1_importGeometryAndSplit() {
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        geomPrt = getGeometryPart(".*");
        return;
    }
    importCADPart("WING.x_b", 5);
    readCameraViews("wingCams.txt");
    //-- Split the single Part Surface by its Part Curve
    geomPrt = getGeometryPart(".*");
    geomPrt.setPresentationName("WING");
    splitByPartCurves(geomPrt.getPartSurfaces(), geomPrt.getPartCurves());
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
    createScene_Geometry().openScene();
    saveSimWithSuffix("a_import");
  }

  void prep2_createRegionBCsAndMesh() {
    defCamView = sim.getViewManager().getObject(camName + "1");
    camView2 = sim.getViewManager().getObject(camName + "2");
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    //--
    //-- Mesh settings
    mshBaseSize = 40;
    mshSrfSizeMin = 6.25;
    mshSrfSizeTgt = 100. * Math.pow(2, 7);  // Geometric progression of ratio 2, ie., how Trimmer grows.
    mshSrfCurvNumPoints = 48;
    mshSrfGrowthRate = 1.15;
    prismsLayers = 3;
    prismsRelSizeHeight = 40;
    prismsNearCoreAspRat = 0.7;
    mshTrimmerMaxCelSize = mshSrfSizeTgt;
    mshCont = createMeshContinua_Trimmer();
    //--
    //-- How big respective to the biggest length scale (dx, dy or dz)
    double farFieldRelSize = 10; 
    //--
    //-- Sphere is created relative to the biggest length scale (dx, dy or dz)
    simpleSphPrt = createShapePartSphere(geomPrt.getPartSurfaces(), farFieldRelSize, regionName);
    //--
    //-- Block is created relative to each length scale (dx, dy and dz)
    double[] blkCorner1 = {-2 * farFieldRelSize, -50 * farFieldRelSize, -50 * farFieldRelSize};
    double[] blkCorner2 = {-0.5, 50 * farFieldRelSize, 50 * farFieldRelSize};
    simpleBlkPrt = createShapePartBlock(geomPrt.getPartSurfaces(), blkCorner1, blkCorner2, bcSym);
    mshOpPrt = meshOperationSubtractParts(getAllGeometryParts(), simpleSphPrt);
    region = assignPartToRegion(mshOpPrt);
    region.setPresentationName(regionName);
    //--
    //-- Volumetric Controls
    blkCorner1 = new double[] {-.1, -30, -100};
    blkCorner2 = new double[] {4, 30, 100};
    simpleBlkPrt = createShapePartBlock(geomPrt.getPartSurfaces(), blkCorner1, blkCorner2, "VC1");
    blkCorner1 = new double[] {-.1, -5, -100};
    blkCorner2 = new double[] {1, 5, 15};
    simpleBlkPrt1 = createShapePartBlock(geomPrt.getPartSurfaces(), blkCorner1, blkCorner2, "VC2");
    blkCorner1 = new double[] {-.1, -1, -10};
    blkCorner2 = new double[] {0.25, 1, 0.75};
    simpleBlkPrt2 = createShapePartBlock(geomPrt.getPartSurfaces(), blkCorner1, blkCorner2, "VC3");
    blkCorner1 = new double[] {-.1, -.25, -3};
    blkCorner2 = new double[] {0.0625, .5, 0.125};
    simpleBlkPrt3 = createShapePartBlock(geomPrt.getPartSurfaces(), blkCorner1, blkCorner2, "VC4");
    createMeshVolumetricControl(mshCont, simpleBlkPrt, "VC1", mshSrfSizeTgt / 4);
    createMeshVolumetricControl(mshCont, simpleBlkPrt1, "VC2", new double[] {800, 400, 1600});
    createMeshVolumetricControl(mshCont, simpleBlkPrt2, "VC3", new double[] {400, 100, 400});
    createMeshVolumetricControl(mshCont, simpleBlkPrt3, "VC4", new double[] {200, 25, 200});
    //-- 
    //-- Create Physics Continua and convert to Coupled Solver
    refT = 22.;
    physCont = createPhysics_AirSteadySegregatedIdealGasSA_AllWall();
    CFL = 25;
    rampCFL = true;
    cflRampBeg = 10;
    cflRampEnd = 75;
    cflRampBegVal = 5;
    enableCoupledSolver(physCont);
    setInitialCondition_Velocity(physCont, 0, 0, -10);
    //**************************************** 
    //-- BOUNDARY CONDITIONS
    //**************************************** 
    //-- 
    //-- Free Stream
    bdry = getBoundary(".*" + regionName + ".*");
    setBC_FreeStream(bdry, flowDirection, 0.0288, refT, ti0, tvr0);
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
    vecObj.addAll(getWingBoundaries());
    vecObj.add(getBoundary(".*" + bcSym + ".*"));
    //--
    //-- Mesh Scene
    coord1 = new double[] {0, 0, -300};
    plane = createSectionPlaneZ(coord1, "Plane Z");
    vecObj2.add(plane);
    cellSet = createCellSet(vecObj2, "Cells on Plane Z");
    vecObj.add(cellSet);
    scene = createScene_Mesh(vecObj);
    scene.setPresentationName("Volume Mesh");
    setSceneCameraView(scene, defCamView);
    scene.openScene();
    //--
    //-- Contour & Vector Plots
    vecObj.remove(cellSet);
    scene = createScene_Scalar(vecObj, getFieldFunction(varP), unit_Pa, true);
    scene.setPresentationName("Pressure Wing");
    setSceneCameraView(scene, defCamView);
    scene2 = createScene_Vector(vecObj, true);
    scene2.setPresentationName("Vector Wing");
    setSceneCameraView(scene2, defCamView);
    //--
    //-- Stopping Criteria
    int stopIter = 50;
    createReportMaximum(region, "Max_Vel", getFieldFunction(varVel), defUnitVel);
    createStoppingCriteria(repMon, "Asymptotic", 0.05, stopIter);
    createReportForce(getWingBoundaries(), "F_y Wing", new double[] {0, 1, 0});
    createStoppingCriteria(repMon, "Asymptotic", 0.01, stopIter);
    createReportForce(getWingBoundaries(), "F_z Wing", flowDirection);
    createStoppingCriteria(repMon, "Asymptotic", 0.01, stopIter);
    saveSimWithSuffix("e_Ready");
  }

  private Vector<Boundary> getWingBoundaries() {
    return (Vector) getBoundaries(".*wing.*", false);
  }
  
  //-- 
  //-- Boundary Condition Names and other Misc stuff
  //-- 
  private String bcWing = "wing body";
  private String bcAirfoil = "wing airfoil";
  private String bcAirfoil2 = "wing airfoil NotUsed";
  private String bcTrail = "wing trail";
  private String camName = "wingCam";
  private String regionName = "FarField";
  private double[] flowDirection = {0, 0, -1};
 
}