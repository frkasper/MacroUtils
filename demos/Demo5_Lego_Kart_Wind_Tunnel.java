import macroutils.*;
import java.util.*;
import star.base.neo.*;
import star.common.*;

/**
 * Complete workflow from CAD to finish on a Lego Kart.
 * 
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo5_Lego_Kart_Wind_Tunnel extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo5_Lego_Kart";
    saveIntermediates = false;
    prep1_importGeometry();
    prep2_createTunnelWrapAndRegion();
    prep3_createBCsAndMesh();
    prep4_setPost();
    runCase(true);
    openAllPlotsAndScenes();
    _finalize();
  }
  
  void prep1_importGeometry() {
    vv1 = readCameraView("cam1|-2.491101e-03,1.572050e-02,-9.156700e-03|8.364881e-01,3.962117e-01,8.597759e-01|-2.296008e-01,9.533891e-01,-1.957871e-01|4.813384e-02|1");
    vv2 = readCameraView("cam2|-6.473358e-03,4.801700e-02,-3.333981e-02|1.001025e+00,4.801700e-02,-3.333981e-02|0.000000e+00,1.000000e+00,0.000000e+00|7.945790e-02|1");
    vv3 = readCameraView("cam3|-1.447091e-02,7.527911e-02,-2.683634e-01|1.812691e+00,1.271046e+00,2.099190e+00|-2.793358e-01,9.264438e-01,-2.523359e-01|3.358636e-01|1");
    defCamView = vv1;
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        return;
    }
    //-- Storing some useful Camera Views. Record a macro manually to extract the coordinates.
    importCADPart("LegoKart.x_b");
    createScene_Geometry("Kart").open(true);
    saveSimWithSuffix("a_import");
  }
  
  void prep2_createTunnelWrapAndRegion() {
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    //-- Save car boundaries for later, i.e., Block refinement.
    partSurfaces.addAll(getAllPartSurfaces());
    geometryObjects.addAll(partSurfaces);
    //-- Get Car Extents and Calculate Wind Tunnel Dimensions
    DoubleVector dvExtents = getExtents(partSurfaces);
    minX = dvExtents.get(0);        maxX = dvExtents.get(1);
    minY = dvExtents.get(2);        maxY = dvExtents.get(3);
    minZ = dvExtents.get(4);        maxZ = dvExtents.get(5);
    coord1 = new double[] {minX - 2 * (maxX-minX), minY + 1.25,             minZ - 6 * (maxZ-minZ)};
    coord2 = new double[] {maxX + 2 * (maxX-minX), maxY + 3 * (maxY-minY),  maxZ + 3 * (maxZ-minZ)};
    //-- 
    cadBody = create3DCad_Block(coord1, coord2, unit_mm, tunnelName);
    cadPrt = getCadPart(cadBody);
    getPartSurface(cadPrt, "z1").setPresentationName(bcInlet);
    getPartSurface(cadPrt, "z0").setPresentationName(bcOutlet);
    partSrf1 = getPartSurface(cadPrt, "y0");
    partSrf1.setPresentationName(bcGround);
    geometryObjects.add(partSrf1);
    partSurfaces2.addAll(getPartSurfaces(cadPrt, "y.*"));
    partSurfaces2.addAll(getPartSurfaces(cadPrt, "x.*"));
    partSrf2 = combinePartSurfaces(partSurfaces2);
    partSrf2.setPresentationName(bcWalls);
    //-- Mesh settings
    mshBaseSize = 5;                            //-- Use 3 if you have a better machine.
    prismsLayers = 2;
    prismsRelSizeHeight = 20;
    prismsNearCoreAspRat = 1.0;
    mshSrfSizeMin = 12.5;
    mshSrfCurvNumPoints = 48;
    mshTrimmerGrowthRate = TRIMMER_GROWTH_RATE_MEDIUM;
    mshTrimmerMaxCelSize = 8 * mshSrfSizeTgt;
    wrapMshOp = createMeshOperation_SurfaceWrapper(getAllLeafParts(), wrapName);
    setMeshBaseSize(wrapMshOp, 0.7 * mshBaseSize, unit_mm);
    geometryObjects2.add(cadPrt);
    mshOpSrfCtrl1 = createMeshOperation_CustomSurfaceControl(wrapMshOp, geometryObjects2, tunnelName, 0., 1600);
    //-- Create Contact Preventions
    createContactPrevention(wrapMshOp, geometryObjects, 1., unit_mm);
    geomPrt = getGeometryPart(wrapMshOp);
    region = assignPartToRegion(geomPrt);
    region.setPresentationName(tunnelName);
    //--
    geometryParts.add(geomPrt);
    meshers.addAll(getMeshers(true, true, TRIMMER, true));
    mshOp = createMeshOperation_AutomatedMesh(geometryParts, meshers, "Mesh of " + geomPrt.getPresentationName());
    geometryParts.clear();
    //-- Create Custom Surface Controls
    mshOpSrfCtrl2 = createMeshOperation_CustomSurfaceControl(mshOp, mshOpSrfCtrl1);
    setMeshPrismsParameters(mshOpSrfCtrl2, 3, 1.2, 2 * mshSrfSizeTgt);
    //-- Create Custom Volume Controls
    geometryParts.add(createShapePartBlock(partSurfaces, new double[] {-0.5, 0, -6}, new double[] {0.5, 1, 1}, "Mesh Refine"));
    createMeshOperation_CustomVolumetricControl(mshOp, geometryParts, "Mesh Refine", 2 * mshSrfSizeTgt);
    geometryParts.clear();
    geometryParts.add(createShapePartBlock(partSurfaces, new double[] {-0.2, 0, -2.5}, new double[] {0.2, 0.25, 0.25}, "Mesh Refine2"));
    createMeshOperation_CustomVolumetricControl(mshOp, geometryParts, "Mesh Refine 2", mshSrfSizeTgt);
    geometryParts.clear();
    geometryParts.add(createShapePartBlock(partSurfaces, new double[] {-0.05, 0, -0.5}, new double[] {0.05, 0.05, 0.025}, "Mesh Refine3"));
    createMeshOperation_CustomVolumetricControl(mshOp, geometryParts, "Mesh Refine 3", 0.5 * mshSrfSizeTgt);
    //--
    namedObjects.addAll(getAllPartSurfaces());
    namedObjects.removeAll(getPartSurfaces(geomPrt));
    scene = createScene_Geometry(namedObjects);
    scene.setPresentationName("Wind Tunnel");
    getDisplayer(scene, ".*").setOpacity(0.25);
    setSceneCameraView(scene, vv3);
    saveSimWithSuffix("b_region");
  }
  
  void prep3_createBCsAndMesh() {
    //-- Default Settings. It updates automatically.
    defUnitVel = unit_kph;
    region = getRegion(".*");
    if (hasValidVolumeMesh()) { 
        sayLoud("Volume Mesh found. Skipping prep3...");
        return;
    }
    //-- Physics Continua
    enableCQR = true;
    physCont = createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal();
    //-- Convert to Coupled Solver
    CFL = 150;
    cflRampEnd = 50;
    cflRampBegVal = 10;
    physCont = getPhysicsContinua(".*");
    enableCoupledSolver(physCont);
    setInitialCondition_Velocity(physCont, 0, 0, -50);
    //-- 
    setBC_VelocityMagnitudeInlet(getBoundary(tunnelName + ".*" + bcInlet), tunnelVel, 0., 0.02, 5.);
    setBC_PressureOutlet(getBoundary(tunnelName + ".*" + bcOutlet), 0., 0., 0.02, 1.);
    //--
    genVolumeMesh();
    saveSimWithSuffix("c_meshed");
  }
  
  void prep4_setPost() {
    namedObjects.clear(); namedObjects2.clear();
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //-- Mesh Scene with the Plane
    coord1 = new double[] {-7, 0, 0};
    plane = createSectionPlaneX(coord1, "Plane X");
    namedObjects.add(plane);
    namedObjects.add(getBoundary(tunnelName + ".*" + bcGround));
    namedObjects.addAll(getCarBoundaries());
    scene = createScene_Mesh(namedObjects);
    scene.setPresentationName("Volume Mesh");
    //-- Update Events
    updEvent1 = createUpdateEvent_Iteration(10, 0);
    updEvent2 = createUpdateEvent_Iteration(1, 20);
    //-- Contour Plots
    namedObjects.remove(plane);
    scene = createScene_Scalar(namedObjects, getFieldFunction(varPC), unit_Dimensionless, true);
    scene.setPresentationName("Pressure Kart");
    setUpdateEvent(scene, updEvent1);
    namedObjects2.add(plane);
    scene = createScene_Scalar(namedObjects2, getFieldFunction(varPC), unit_Dimensionless, true);
    scene.setPresentationName("Pressure Section");
    setUpdateEvent(scene, updEvent1);
    setSceneCameraView(scene, vv2);
    disp = createSceneDisplayer(scene, "Geometry", namedObjects);
    disp.setRepresentation(queryGeometryRepresentation());
    //-- Vector Plot
    scene2 = createScene_Vector(namedObjects2, true);
    scene2.setPresentationName("Vector Section");
    setUpdateEvent(scene, updEvent1);
    setSceneCameraView(scene2, vv2);
    disp = createSceneDisplayer(scene2, "Geometry", namedObjects);
    disp.setRepresentation(queryGeometryRepresentation());
    //-- Stopping Criteria
    bdry = getBoundary(tunnelName + ".*" + bcInlet);
    rep = createReportFrontalArea(getCarBoundaries(), "Frontal Area", new double[] {0, 1, 0}, new double[] {0, 0, -1});
    createReportForceCoefficient(getCarBoundaries(), "C_d", tunnelVel, rep.getReportMonitorValue(), new double[] {0, 0, -1});
    setUpdateEvent(repMon, updEvent2);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, 50);
    createReportForceCoefficient(getCarBoundaries(), "C_l", tunnelVel, rep.getReportMonitorValue(), new double[] {0, 1, 0});
    setUpdateEvent(repMon, updEvent2);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, 50);
    saveSimWithSuffix("e_Ready");
  }

  private ArrayList<Boundary> getCarBoundaries() {
    return getBoundaries("^((?!" + tunnelName + ").)*$", false);
  }
 
  double minX, maxX, minY, maxY, minZ, maxZ;
  private String tunnelName = "Tunnel";
  private String wrapName = "Kart Wrap";
  private double tunnelVel = 50;
  
}