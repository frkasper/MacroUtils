import macroutils.*;
import star.vis.*;

/**
 * Simple pseudo 2D Demo of a sloshing case in a tank.
 * 
 * Geometry in XY plane:
 *                  L  
 *      +-----------------------+
 *      |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *    H |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *      *-----------------------+
 *      O(0,0,0)
 * 
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo7_Sloshing_Case extends MacroUtils {
          
  public void execute() {
    _initUtils();
    simTitle = "Demo7_Sloshing";
    _updateSimulationVars();
    prep1_createRegion();
    prep2_PhysicsAndMesh();
    prep3_MotionAndPost();
    saveSim(simTitle + "_preRun", false);
    runCase(true);
    _finalize();
  }

  void _updateSimulationVars() {
    //-- Physics/Solver/Mesh settings
    mshBaseSize = 2.;
    W = mshBaseSize;
    mshTrimmerMaxCelSize = 100;
    urfVel = 0.9;
    urfVOF = 0.9;
    urfP = 0.6;
    vofSharpFact = 0.10;
    trnInnerIter = 5;
    trnMaxTime = 8.;
  }
  
  void prep1_createRegion() {
    defCamView = readCameraView("myView|4.478788e-02,4.233814e-02,1.889347e-03|4.478788e-02,4.233814e-02,2.732051e-01|0.000000e+00,1.000000e+00,0.000000e+00|5.817755e-02|1");
    cadBody = create3DCad_Block(coord0, new double[] {L, H, W}, unit_mm);
    combinePartSurfaces(getPartSurfaces("z.*")).setPresentationName(bcSym);
    partSurfaces.addAll(getPartSurfaces("x.*"));
    partSurfaces.addAll(getPartSurfaces("y.*"));
    combinePartSurfaces(partSurfaces).setPresentationName(bcWalls);
    region = assignAllPartsToRegion();
    setBC_Symmetry(getBoundary(bcSym));
  }
    
  void prep2_PhysicsAndMesh() {
    geometryParts.addAll(region.getPartGroup().getObjects());
    mshOp = createMeshOperation_AutomatedMesh(geometryParts, getMeshers(true, false, TRIMMER, false), "Mesh");
    disableSurfaceProximityRefinement(mshOp);
    physCont = createPhysics_AirWaterUnsteadySegregatedIncompressibleKEps2LyrIsothermal();
    updateSolverSettings();
    //-- 
    coord1 = new double[] {0, 0.5 * L, 0};     // Water Height
    coord2 = new double[] {0, 1, 0};
    createWave(physCont, coord1, coord2, coord0, coord0, true);
    //-- 
    genVolumeMesh();
    createScene_Mesh();
  }
  
  void prep3_MotionAndPost() {
    createFieldFunction("Amplitude", "($Time <= 4) ? 0.01 : 0", null);
    createFieldFunction("dt", "($Time <= 5) ? 0.00125 : 0.0025", null);
    setSolverPhysicalTimestep("$dt");
    createFieldFunction("Period", "0.5", null);   
    createFieldFunction("Omega", 2 * Math.PI + " / $Period", null);
    ff = createFieldFunction("MotionVel", "-$Amplitude * $Omega * sin($Omega * ($Time - 0.25 * $Period))", null);
    ff2 = createFieldFunction("MotionDispl", "$Amplitude * cos($Omega * ($Time - 0.25 * $Period))", null);
    createMotion_Translation("[$MotionVel, 0, 0]", region);
    //-- Some cool Reports/Plots
    createReports_Unsteady();
    createReportMaximum(region, "MotionVel", ff, defUnitVel);
    createReportMaximum(region, "MotionDispl", ff2, defUnitLength);
    //-- Scene setup
    namedObjects.add(getBoundary(bcSym));
    ff = getFieldFunction("Volume Frac.*Air");
    defColormap = getColormap("flames1");
    scene = createScene_Scalar(namedObjects, ff, unit_Dimensionless, false);
    scene.setPresentationName("VOF");
    createAnnotation_Time(scene, "Time", unit_s, "%3.1f", new double[] {0.01, 0.5});
    ScalarDisplayer scalDisp = (ScalarDisplayer) getDisplayer(scene, ".*");
    scalDisp.setDisplayMeshBoolean(true);
    //-- Change Update Frequency / Save Pictures
    updEvent = createUpdateEvent_DeltaTime(0.005, unit_s, false);
    setUpdateEvent(scene, updEvent);
    setSceneSaveToFile(scene, 1280, 720, 0);
  }
  
  double L = 100, H = L, W = 0.;     //- Length x Height x Width (mm)
  
}
