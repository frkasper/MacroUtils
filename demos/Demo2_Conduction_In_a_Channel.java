package macroutils;

public class Demo2_Conduction_In_a_Channel extends MacroUtils {

  private final double length = 1000;
  private final double height = 100;
  private final double depth = 10;
    
  public void execute() {
    _initUtils();
    simTitle = "Demo2_Channel";
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
    string = "myCam|5.189667e-01,3.166401e-02,1.624661e-03|5.189667e-01,3.166401e-02,-1.900342e+00|0.000000e+00,1.000000e+00,0.000000e+00|2.515100e-01";
    readCameraView(string);
    coord1 = new double[] {0, 0, 0};
    coord2 = new double[] {length, height, depth};
    simpleBlkPrt = createShapePartBlock(coord1, coord2, unit_mm, "Block");
    simpleBlkPrt.getPartSurfaces().iterator().next().setPresentationName(bcWall);
    splitByAngle(simpleBlkPrt.getPartSurfaces(), 80);
    saveSimWithSuffix("a_created");
  }
  
  void prep2_createRegion() {
    defCamView = getCameraView("myCam");
    if (!sim.getRegionManager().isEmpty()) { 
        sayLoud("Region already created. Skipping prep2...");
        return;
    }
    String regexSearch = bcWall + ".*";
    geomPrt = getAllGeometryParts().iterator().next();
    partSrf = getPartSurface(geomPrt, "Min", "X", 1.0, regexSearch);
    partSrf.setPresentationName(bcHot);
    partSrf = getPartSurface(geomPrt, "Max", "X", 1.0, regexSearch);
    partSrf.setPresentationName(bcCold);
    partSrf = getPartSurface(geomPrt, "Min", "Z", 1.0, regexSearch);
    partSrf.setPresentationName(bcSym1);
    partSrf = getPartSurface(geomPrt, "Max", "Z", 1.0, regexSearch);
    partSrf.setPresentationName(bcSym2);
    combinePartSurfaces(getAllPartSurfaces(geomPrt, regexSearch), bcWall);
    region = assignAllPartsToRegion();
    saveSimWithSuffix("b_regionOK");
  }
    
  void prep3_BCsAndMesh() {
    if (queryVolumeMesh() != null) {
        sayLoud("Volume Mesh already exists. Skipping prep3...");
        return;
    }
    region = getRegion(".*");
    //-- Physics/Mesh settings
    mshBaseSize = depth / 2;
    mshTrimmerMaxCelSize = 100;
    urfSolidEnrgy = 0.999999999;
    //-- 
    mshCont = createMeshContinua_Trimmer();
    disablePrismLayers(mshCont);
    disableSurfaceProximityRefinement(mshCont);
    createPhysics_SteelSteadySegregated();
    updateSolverSettings();
    //-- 
    bdry = getBoundary(bcHot);
    setBC_ConvectionWall(bdry, 50., 50);
    //-- 
    bdry = getBoundary(bcCold);
    setBC_ConvectionWall(bdry, 20., 15);
    //-- 
    setBC_Symmetry(getBoundary(bcSym1));
    setBC_Symmetry(getBoundary(bcSym2));
    //-- 
    genVolumeMesh();
    createScene_Mesh().openScene();
    saveSimWithSuffix("c_meshed");
  }
  
  void prep4_setPost() {
    if (!sim.getReportManager().isEmpty()) {
        sayLoud("Post-processing already exists. Skipping prep4...");
        return;
    }
    //-- Stopping Criteria
    createReportVolumeAverage(getRegion(".*"), varT, varT, defUnitTemp);
    createStoppingCriteria(repMon, "Asymptotic", 0.005, 50);
    //-- Contour Plot
    vecObj.add(getBoundary(bcSym1));
    scene = createScene_Scalar(vecObj, getFieldFunction(varT), defUnitTemp, true);
    setUpdateFrequency(scene, 50);
    openAllPlots();
    openAllScenes();
    saveSimWithSuffix("e_Ready");
  }
  
}
