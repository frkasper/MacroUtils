package macroutils;

import star.base.neo.*;
import star.common.*;
import star.flow.*;
import star.meshing.*;
import star.vis.*;

public class Demo1_Flow_In_a_Pipe extends MacroUtils {

  private final double length = 500;
  private final double radius = 20;
    
  public void execute() {
    _initUtils();
    simTitle = "Demo1_Cylinder";
    saveIntermediates = false;
    prep1_createAndSplit();
    prep2_createRegion();
    prep3_BCsAndMesh();
    prep4_setPost();
    runCase();
    _finalize();
  }
  
  void prep1_createAndSplit() {
    if (!sim.getGeometryPartManager().isEmpty()) { 
        sayLoud("Geometry already created. Skipping prep1...");
        return;
    }
    coord1 = new double[] {0, 0, 0};
    coord2 = new double[] {length, 0, 0};
    simpleCylPrt = createShapePartCylinder(coord1, coord2, radius, radius, unit_mm, "Cyl");
    simpleCylPrt.getPartSurfaces().iterator().next().setPresentationName(bcWall);
    splitPartSurfaceByAngle(simpleCylPrt, 80);
    saveSimWithSuffix("a_created");
  }
  
  void prep2_createRegion() {
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    String regexSearch = bcWall + ".*";
    geomPrt = getAllGeometryParts().iterator().next();
    partSrf = getPartSurface(geomPrt, "Min", "X", 0.01, regexSearch);
    partSrf.setPresentationName(bcInlet);
    partSrf = getPartSurface(geomPrt, "Max", "X", 0.01, regexSearch);
    partSrf.setPresentationName(bcOutlet);
    combinePartSurfaces(getAllPartSurfaces(geomPrt, regexSearch), bcWall);
    region = assignAllPartsToRegion();
    saveSimWithSuffix("b_regionOK");
  }
    
  void prep3_BCsAndMesh() {
    if (queryVolumeMesh() != null) {
        sayLoud("Volume Mesh already exists. Skipping prep3...");
        return;
    }
    region = sim.getRegionManager().getRegions().iterator().next();
    //-- Mesh settings
    mshBaseSize = radius / 4;
    prismsLayers = 3;
    prismsRelSizeHeight = 30;
    mshTrimmerMaxCelSize = 100;
    //-- 
    createMeshContinua_Trimmer();
    createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal();
    
    bdry = getBoundary(bcInlet);
    setBC_VelocityMagnitudeInlet(bdry, 0.1, 0., 0., 0.);
    
    bdry = getBoundary(bcOutlet);
    setBC_PressureOutlet(bdry, 0., 0., 0., 0.);
    
    genVolumeMesh();
    createScene_Mesh().openScene();
    saveSimWithSuffix("c_meshed");
  }
  
  void prep4_setPost() {
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //-- Contour Plot
    plane = createSectionPlaneZ(coord0, "Plane Z");
    vecObj.add(plane);
    createScene_Scalar(vecObj, varVel, defUnitVel, true).openScene();
    //-- Stopping Criteria
    bdry = getBoundary(bcInlet);
    createReportMassFlowAverage(bdry, "Pressure Inlet", varP, unit_Pa);
    createStoppingCriteria(repMon, 0.01, 30, false);
    openPlot(monPlot);
    saveSimWithSuffix("e_Ready");
  }
  
}
