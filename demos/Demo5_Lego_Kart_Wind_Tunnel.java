package macroutils;

import java.util.*;
import star.base.neo.*;
import star.common.*;

public class Demo5_Lego_Kart_Wind_Tunnel extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo5_LegoKart";
    saveIntermediates = false;
    prep1_importGeometry();
    prep2_createRegion();
    prep3_BCsAndMesh();
    prep4_setPost();
    runCase(true);
    _finalize();
  }
  
  void prep1_importGeometry() {
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        defCamView = sim.getViewManager().getObject(camName);
        camView2 = sim.getViewManager().getObject(camName + "2");
        return;
    }
    //-- Storing some useful Camera Views. Record a macro manually to extract the coordinates.
    camFocalPoint = new double[] {-0.018866810523014532, -0.007397846678836056, -0.043861133047594425};
    camPosition = new double[] {0.1550959625395927, 0.2142501216686642, 0.20680788401219294};
    camViewUp = new double[] {-0.21351729658162824, 0.8006582139177919, -0.5597828047970432};
    defCamView = createCameraView(camFocalPoint, camPosition, camViewUp, 0.07317047312477107, camName);
    defCamView.setPresentationName(camName);
    camFocalPoint = new double[] {0.0010252636934728887, 0.03250292453056392, -0.011798889793666584};
    camPosition = new double[] {1.7973505896593542, 0.03250292453056392, -0.011798889793666556};
    camViewUp = new double[] {0.0, 1.0, 0.0};
    camView2 = createCameraView(camFocalPoint, camPosition, camViewUp, 0.07028763658591237, camName + "2");
    camView2.setPresentationName(camName + "2");
    importCADPart("LegoKart.x_b");
    scene = createScene_Geometry();
    scene.setPresentationName("Kart");
    scene.openScene();
    saveSimWithSuffix("a_import");
  }
  
  void prep2_createRegion() {
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    //-- Get Car Extents and Calculate Wind Tunnel Dimensions
    DoubleVector dvExtents = getExtents(getAllPartSurfaces());
    double minX = dvExtents.get(0);     double maxX = dvExtents.get(1);
    double minY = dvExtents.get(2);     double maxY = dvExtents.get(3);
    double minZ = dvExtents.get(4);     double maxZ = dvExtents.get(5);
    coord1 = new double[] {minX - 2 * (maxX-minX), minY + 1.25,             minZ - 6 * (maxZ-minZ)};
    coord2 = new double[] {maxX + 2 * (maxX-minX), maxY + 3 * (maxY-minY),  maxZ + 3 * (maxZ-minZ)};
    //-- 
    simpleBlkPrt = createShapePartBlock(coord1, coord2, unit_mm, tunnelName);
    partSrf = getPartSurface(simpleBlkPrt, ".*");
    partSrf.setPresentationName(bcWalls);
    splitByAngle(partSrf, 85);
    partSrf = getPartSurface(simpleBlkPrt.getPartSurfaces(), "Min", "Y", 1.);
    partSrf.setPresentationName(bcFloor);
    partSrf = getPartSurface(simpleBlkPrt.getPartSurfaces(), "Max", "Y", 1.);
    partSrf.setPresentationName(bcTop);
    partSrf = getPartSurface(simpleBlkPrt.getPartSurfaces(), "Max", "Z", 1.);
    partSrf.setPresentationName(bcInlet);
    partSrf = getPartSurface(simpleBlkPrt.getPartSurfaces(), "Min", "Z", 1.);
    partSrf.setPresentationName(bcOutlet);
    combinePartSurfaces(getAllPartSurfaces(simpleBlkPrt, bcWalls + ".*"), bcWalls);
    region = assignAllPartsToRegion();
    region.setPresentationName(tunnelName);
    //-- Create Volume Control Blocks
    coord1 = new double[] {minX - 0.5 * (maxX-minX), minY,                      minZ - 6 * (maxZ-minZ)};
    coord2 = new double[] {maxX + 0.5 * (maxX-minX), maxY + 0.7 * (maxY-minY),  maxZ + 1 * (maxZ-minZ)};
    simpleBlkPrt = createShapePartBlock(coord1, coord2, unit_mm, "Mesh Refine");
    coord1 = new double[] {minX - 0.1 * (maxX-minX), minY,                      minZ - 2 * (maxZ-minZ)};
    coord2 = new double[] {maxX + 0.1 * (maxX-minX), maxY + 0.2 * (maxY-minY),  maxZ + 0.1 * (maxZ-minZ)};
    simpleBlkPrt2 = createShapePartBlock(coord1, coord2, unit_mm, "Mesh Refine2");
    //--
    //-- Mesh settings
    mshBaseSize = 3;
    prismsLayers = 2;
    prismsRelSizeHeight = 20;
    prismsNearCoreAspRat = 0.7;
    mshTrimmerMaxCelSize = 10 * mshSrfSizeTgt;
    mshWrapperScaleFactor = 70;
    mshCont = createMeshContinua_Trimmer();
    setMeshTrimmerSizeToPrismThicknessRatio(mshCont, 5);
    createMeshVolumetricControl(mshCont, simpleBlkPrt, "Mesh Refine", 2 * mshSrfSizeTgt);
    createMeshVolumetricControl(mshCont, simpleBlkPrt2, "Mesh Refine2", mshSrfSizeTgt);
    //-- Physics Continua
    physCont = createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal();
    //-- 
    vecObj1.addAll(getAllPartSurfaces());
    scene = createScene_Geometry(vecObj1);
    scene.setPresentationName("Wind Tunnel");
    scene.makeTransparent(true);
    scene.resetCamera();
    scene.openScene();
    saveSimWithSuffix("b_regionOK");
  }
    
  void prep3_BCsAndMesh() {
    //-- Default Settings. It updates automatically.
    defUnitVel = unit_kph;
    //-- 
    if (queryVolumeMesh() != null) {
        sayLoud("Volume Mesh already exists. Skipping prep3...");
        return;
    }
    //-- Convert to Coupled Solver
    CFL = 35;
    rampCFL = true;
    cflRampEnd = 150;
    cflRampBegVal = 15;
    physCont = getPhysicsContinua(".*");
    enableCoupledSolver(physCont);
    setInitialCondition_Velocity(physCont, 0, 0, -50);
    //-- 
    region = getRegion(".*");
    for (Boundary bdry : getAllBoundaries()) {
        if (bdry.getPresentationName().startsWith(tunnelName)) { 
            setMeshSurfaceSizes(bdry, mshSrfSizeMin, 10 * mshSrfSizeTgt);
            setMeshPrismsParameters(bdry, 3, 1.2, 100);
        }
    }
    bdry = getBoundary(tunnelName + ".*" + bcInlet);
    disablePrismLayers(bdry);
    setBC_VelocityMagnitudeInlet(bdry, tunnelVel, 0., 0.02, 5.);
    
    bdry = getBoundary(tunnelName + ".*" + bcOutlet);
    disablePrismLayers(bdry);
    setBC_PressureOutlet(bdry, 0., 0., 0.02, 1.);
    //--
    //-- Enable Wrapper and Create Contact Preventions
    enableSurfaceWrapper(mshCont);
    removeFeatureCurves(region);
    vecObj.addAll(getCarBoundaries());
    bdry = getBoundary(tunnelName + ".*" + bcFloor);
    vecObj.add(bdry);
    vecObj2.addAll(getCarBoundaries());
    createContactPrevention(region, vecObj, 1., unit_mm);
    //--
    genVolumeMesh();
    coord1 = new double[] {-7, 0, 0};
    plane = createSectionPlaneX(coord1, "Plane X");
    vecObj2.add(plane);
    scene = createScene_Mesh(vecObj2);
    scene.setPresentationName("Volume Mesh");
    setSceneCameraView(scene, camView2);
    scene.openScene();
    saveSimWithSuffix("c_meshed");
  }
  
  void prep4_setPost() {
    vecObj.clear(); vecObj2.clear();
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //-- Contour & Vector Plots
    vecObj.addAll(getCarBoundaries());
    bdry = getBoundary(tunnelName + ".*" + bcFloor);
    vecObj.add(bdry);
    scene = createScene_Scalar(vecObj, getFieldFunction(varP), defUnitPress, true);
    scene.setPresentationName("Pressure Kart");
    vecObj2.add(sim.getPartManager().getObject("Plane X"));
    scene = createScene_Scalar(vecObj2, getFieldFunction(varP), defUnitPress, true);
    scene.setPresentationName("Pressure Section");
    setSceneCameraView(scene, camView2);
    scene2 = createScene_Vector(vecObj2, true);
    scene2.setPresentationName("Vector Section");
    setSceneCameraView(scene2, camView2);
    vecObj2.clear();
    vecObj2.addAll(getCarBoundaries());
    disp = createSceneDisplayer(scene, "Geometry", vecObj2);
    disp.setRepresentation(queryGeometryRepresentation());
    disp = createSceneDisplayer(scene2, "Geometry", vecObj2);
    disp.setRepresentation(queryGeometryRepresentation());
    //-- Stopping Criteria
    bdry = getBoundary(tunnelName + ".*" + bcInlet);
    createReportMassFlowAverage(bdry, "dP Tunnel", getFieldFunction(varP), unit_Pa);
    createStoppingCriteria(repMon, "Asymptotic", 0.2, 100);
    createReportForce(getCarBoundaries(), "F_y Car", new double[] {0, -1, 0});
    createStoppingCriteria(repMon, "Asymptotic", 0.002, 100);
    createReportForceCoefficient(getCarBoundaries(), "C_d Car", tunnelVel, 0.077, new double[] {0, 0, -1});
    createStoppingCriteria(repMon, "Asymptotic", 0.002, 100);
    openAllScenes();
    saveSimWithSuffix("e_Ready");
  }

  private Vector<Boundary> getCarBoundaries() {
    return (Vector) getBoundaries("^((?!" + tunnelName + ").)*$", false);
  }
  
  private String camName = "myCam";
  private String tunnelName = "Tunnel";
  private double tunnelVel = 50;
  
}