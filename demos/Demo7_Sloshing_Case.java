package macroutils;

import star.common.*;
import star.vis.*;

public class Demo7_Sloshing_Case extends MacroUtils {
          
  public void execute() {
    _initUtils();
    simTitle = "Demo7_Sloshing";
    saveIntermediates = false;
    _updateSimulationVars();
    prep1_createRegion();
    prep2_PhysicsAndMesh();
    prep3_MotionAndPost();
    _finalize();
    runCase();
  }

  void _updateSimulationVars() {
    //-- Physics/Solver/Mesh settings
    mshBaseSize = W / 2;
    mshTrimmerMaxCelSize = 100;
    urfVel = 0.9;
    urfVOF = 0.9;
    urfP = 0.3;
    vofCFL_l = 1.5;
    vofCFL_u = 3.0;
    vofSharpFact = 0.25;
    trnTimestep = 0.001;
    trnInnerIter = 7;
    trnMaxTime = 8.;
  }
  
  void prep1_createRegion() {
    if (!sim.getRegionManager().isEmpty()) { 
        defCamView = getCameraView("myView");
        sayLoud("Region already created. Skipping prep1...");
        return;
    }
    String cam = "myView|5.322452e-02,4.655490e-02,-1.054080e-03|5.322452e-02,4.655490e-02,2.732051e-01|0.000000e+00,1.000000e+00,0.000000e+00|6.705117e-02";
    defCamView = readCameraView(cam);
    coord1 = new double[] {L, H, W};
    simpleBlkPrt = createShapePartBlock(coord0, coord1, unit_mm, "Block");
    partSrf = getPartSurface(simpleBlkPrt, ".*");
    partSrf.setPresentationName(bcWall);
    splitByAngle(partSrf, 80);
    partSrf = getPartSurface(simpleBlkPrt, "Min", "Z", 1.0, bcWall + ".*");
    partSrf.setPresentationName(bcSym1);
    partSrf = getPartSurface(simpleBlkPrt, "Max", "Z", 1.0, bcWall + ".*");
    partSrf.setPresentationName(bcSym2);
    combinePartSurfaces(getAllPartSurfaces(simpleBlkPrt, bcWall + ".*"), bcWall);
    region = assignAllPartsToRegion();
    setBC_Symmetry(getBoundary(bcSym1));
    setBC_Symmetry(getBoundary(bcSym2));
    saveSimWithSuffix("a_regionOK");
  }
    
  void prep2_PhysicsAndMesh() {
    region = getRegion(".*");
    if (queryVolumeMesh() != null) {
        sayLoud("Volume Mesh already exists. Skipping prep2...");
        return;
    }
    mshCont = createMeshContinua_Trimmer();
    disablePrismLayers(mshCont);
    disableSurfaceProximityRefinement(mshCont);
    physCont = createPhysics_AirWaterUnsteadySegregatedIncompressibleKEps2LyrIsothermal();
    updateSolverSettings();
    //-- 
    coord1 = new double[] {0, 0.5 * L, 0};     // Water Height
    coord2 = new double[] {0, 1, 0};
    createWave(physCont, coord1, coord2, coord0, coord0, true);
    //-- 
    genVolumeMesh();
    createScene_Mesh();
    saveSimWithSuffix("b_meshed");
  }
  
  void prep3_MotionAndPost() {
    String sceneName = "VOF";
    scene = getScene(sceneName);
    if (scene != null) {
        sayLoud("Motion and Postprocessing exists. Skipping prep3...");
        return;
    }
    createFieldFunction("Amplitude", "($Time <= 4) ? 0.01 : 0");
    createFieldFunction("Period", "0.5");   
    createFieldFunction("Omega", 2 * Math.PI + " / $Period");
    ff = createFieldFunction("MotionVel", "-$Amplitude * $Omega * sin($Omega * ($Time - 0.25 * $Period))");
    ff2 = createFieldFunction("MotionDispl", "$Amplitude * cos($Omega * ($Time - 0.25 * $Period))");
    createMotion_Translation("[$MotionVel, 0, 0]", region);
    //-- 
    //-- Some cool Reports/Plots
    createReportMaximum(region, "CFL_max", "Convective.*Number", unit_Dimensionless);
    createReportMassAverage(region, "CFL_avg", "Convective.*Number", unit_Dimensionless);
    createReportMaximum(region, "Vel_max", varVel, defUnitVel);
    createReportMaximum(region, "MotionVel", ff.getPresentationName(), defUnitVel);
    createReportMaximum(region, "MotionDispl", ff2.getPresentationName(), defUnitLength);
    //-- 
    //-- Scene setup
    vecObj.add(getBoundary(bcSym1));
    ff = getFieldFunction("Volume Frac.*Air");
    scene = createScene_Scalar(vecObj, ff, unit_Dimensionless, false);
    scene.setPresentationName(sceneName);
    setSceneCameraView(scene, defCamView);
    addAnnotation_SolutionTime(scene, new double[] {0.4, 0.9});
    ScalarDisplayer scalDisp = (ScalarDisplayer) getDisplayer(scene, ".*");
    scalDisp.getLegend().setHeight(0.1);
    scalDisp.setDisplayMeshBoolean(true);
    //-- Change Update Frequency / Save Pictures
    setUpdateFrequency(scene, 5);
    setSceneSaveToFile(scene, 1280, 720, 0);
    saveSimWithSuffix("c_ready");
  }
  
  double L = 100, H = L, W = 0.03 * L;     //- Length x Height x Width (mm)
  
}
