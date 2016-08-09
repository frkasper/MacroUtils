import macroutils.*;
  
/**
 * Simple Demo of a 3D laminar isothermal flow in a pipe.
 * 
 * Geometry:
 *                                       L  
 *      +---------------------------------------------------------------+
 *      |                                                               |
 *    r * O(0,0,0)                                                      |
 *      |                                                               |
 *      +---------------------------------------------------------------+
 * 
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */

public class Demo1_Flow_In_a_Pipe extends MacroUtils {

  private final double length = 500;
  private final double radius = 20;
    
  public void execute() {
    _initUtils();
    simTitle = "Demo1_Cylinder";
    prep1_createPart();
    prep2_createRegion();
    prep3_BCsAndMesh();
    prep4_setPost();
    runCase(true);
    _finalize();
  }
  
  void prep1_createPart() {
    defCamView = readCameraView("myCam|2.498938e-01,-1.450833e-02,-2.222717e-02|2.498938e-01,-1.450833e-02,9.690091e-01|0.000000e+00,1.000000e+00,0.000000e+00|1.463440e-01|1");
    defTessOpt = TESSELATION_FINE;
    cadBody = create3DCad_Cylinder(radius, length, coord0, unit_mm, X);
  }
  
  void prep2_createRegion() {
    getPartSurface("x0").setPresentationName(bcInlet);
    getPartSurface("x1").setPresentationName(bcOutlet);
    getPartSurface("Def.*").setPresentationName(bcWall);
    region = assignAllPartsToRegion();
  }
    
  void prep3_BCsAndMesh() {
    //-- Mesh settings
    mshBaseSize = radius / 8;
    prismsLayers = 3;
    mshSrfSizeMin = 75.;
    prismsRelSizeHeight = 30;
    //-- 
    createMeshContinua_PolyOnly();
    createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal();
    setSolverAggressiveURFs();
    //-- 
    setBC_VelocityMagnitudeInlet(getBoundary(bcInlet), 0.1, 0., 0., 0.);
    setBC_PressureOutlet(getBoundary(bcOutlet), 0., 0., 0., 0.);
    setMeshGeneralizedCylinderExtrusion_Constant(getBoundary(bcWall), 60);
    //-- 
    genVolumeMesh();
  }
  
  void prep4_setPost() {
    //-- Contour Plot
    plane = createSectionPlaneZ(coord0, "Plane Z");
    namedObjects.add(plane);
    createScene_Scalar(namedObjects, getFieldFunction(varVel), defUnitVel, true);
    //-- Stopping Criteria
    bdry = getBoundary(bcInlet);
    createReportMassFlowAverage(bdry, "Pressure Inlet", getFieldFunction(varP), unit_Pa);
    createStoppingCriteria(repMon, "Asymptotic", 0.001, 50);
    openAllPlotsAndScenes();
  }
  
}
