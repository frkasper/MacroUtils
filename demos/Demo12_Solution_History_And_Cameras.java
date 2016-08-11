import macroutils.*;
import star.common.*;
import star.post.*;
import star.vis.*;

/**
 * Pseudo 2D Laminar Vortex Shedding under Reynolds of 500.
 * 
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
*/
public class Demo12_Solution_History_And_Cameras extends MacroUtils {

  public void execute() {

    _initUtils();
    simTitle = "Demo12_Solution_History_And_Cameras";
    //--
    vv1 = readCameraView("cam1|1.516797e-01,-4.188893e-03,-6.132604e-03|1.516797e-01,-4.188893e-03,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|1.165985e-01|1");
    vv2 = readCameraView("cam2|5.502414e-02,3.902467e-04,-1.586686e-04|5.502414e-02,3.902467e-04,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|4.309526e-02|1");
    vv3 = readCameraView("cam3|3.000000e-01,3.902467e-04,-1.586686e-04|3.000000e-01,3.902467e-04,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|4.309526e-02|1");
    defCamView = vv1;
    //--
    if (!(hasValidVolumeMesh() && isSimFile(simTitle + "_SS")) || !isUnsteady()) {
        pre();
        solveSS();
        postSS();
    }
    solveTRN();
    postTRN();
    _finalize();
    
  }
  
  void pre() {
    mshBaseSize = 5;
    prismsLayers = 4;
    prismsRelSizeHeight = 25;
    mshSrfSizeMin = 6.25;
    mshTrimmerMaxCelSize = 100.;
    //--
    denAir = 1.0;
    viscAir = 1e-5;
    physCont = createPhysicsContinua(_2D, _STEADY, _GAS, _SEGREGATED, _INCOMPRESSIBLE, _ISOTHERMAL, _LAMINAR, false, false, false);
    setMaterialProperty(physCont, "Air", varVisc, viscAir, unit_Pa_s);
    setMaterialProperty(physCont, "Air", varDen, denAir, unit_kgpm3);

    //--
    double w = 2 * mshBaseSize;
    cadBody = create3DCad_Block(new double[] {-150, -75, 0}, new double[] {400, 75, w}, unit_mm);
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    geometryParts.add(geomPrt);

    double r = 10., l = 2 * w;
    defTessOpt = TESSELATION_FINE;
    cadBody2 = create3DCad_Cylinder(r, l, new double[] {0, 0, -l/2}, unit_mm, Z);
    geomPrt2 = getGeometryPart(cadBody2.getPresentationName());
    geometryParts.add(geomPrt2);

    mshOpPrt = meshOperationSubtractParts(geometryParts, geomPrt);
    geometryParts2.add(mshOpPrt);
    createMeshOperation_BadgeFor2D(geometryParts2, "My 2D Badging Operation");
    region = assignPartToRegion(mshOpPrt, false);
    autoMshOp = createMeshOperation_AutomatedMesh(geometryParts2, getMeshers(true, false, TRIMMER, true), "My Mesh");
    geometryObjects.addAll(getPartSurfaces(mshOpPrt, cadBody.getPresentationName() + ".*"));
    mshOpSrfCtrl = createMeshOperation_CustomSurfaceControl(autoMshOp, geometryObjects, "No Prisms", 0, 0, 0);
    
    for (Boundary b : getBoundaries(".*", false)) {
        String name = b.getPresentationName();
        if (name.matches(".*y.")) {
            setBC_FreeSlipWall(b);
        }
        if (name.matches(".*z.")) {
            setBC_Symmetry(b);
        }
        if (name.matches(".*x0")) {
            setBC_VelocityMagnitudeInlet(b, Re * viscAir / (denAir * 2 * r * unit_mm.getConversion()), 0., 0., 0.);
        }
        if (name.matches(".*x1")) {
            setBC_PressureOutlet(b, 0., 0., 0., 0.);
        }
    }
    
    simpleBlkPrt1 = createShapePartBlock(new double[] {-5 * r, -4 * r, -l}, new double[] {200 * r, 4 * r, l}, unit_mm, "Level1");
    geometryParts.clear();
    geometryParts.add(simpleBlkPrt1);
    createMeshOperation_CustomVolumetricControl(autoMshOp, geometryParts, "Level1", 50.);
    simpleBlkPrt2 = createShapePartBlock(new double[] {-3 * r, -2.5 * r, -l}, new double[] {25 * r, 2.5 * r, l}, unit_mm, "Level2");
    geometryParts.clear();
    geometryParts.add(simpleBlkPrt2);
    createMeshOperation_CustomVolumetricControl(autoMshOp, geometryParts, "Level2", 25.);
    simpleBlkPrt3 = createShapePartBlock(new double[] {-0.25 * r, -1.5 * r, -l}, new double[] {4 * r, 1.5 * r, l}, unit_mm, "Level3");
    geometryParts.clear();
    geometryParts.add(simpleBlkPrt2);
    createMeshOperation_CustomVolumetricControl(autoMshOp, geometryParts, "Level3", 12.5);
    
    genVolumeMesh();
  }
  
  void solveSS() {
    maxIter = 100;
    setSolverAggressiveURFs();
    runCase();
  }
  
  void solveTRN() {
    scene = getScene("Scalar");
    region = getRegion(".*");
    physCont = getPhysicsContinua(".*");
    namedObjects.clear();
    if(isUnsteady() && getPhysicalTime() > 0) {
        return;
    }
    //--
    clearSolutionHistory();
    //--
    trn2ndOrder = true;
    trnInnerIter = 6;
    trnTimestep = 0.005;
    trnMaxTime = 12.;
    urfVel = 0.9;
    urfP = 0.6;
    enableUnsteady(physCont);
    updateSolverSettings();
    createReports_Unsteady();
    setMonitorsNormalizationOFF();
    createReportForce(getBoundary("Cyl.*"), "Force", new double[] {1, 0, 0});
    prettifyMe();
    updEvent1 = createUpdateEvent_DeltaTime(0.01, unit_s, false);
    updEvent2 = createUpdateEvent_Range(getPhysicalTimeMonitor(), GE, 0.1);
    updEvent = createUpdateEvent_Logic(new UpdateEvent[] {updEvent1, updEvent2}, AND);
    namedObjects.add(region);
    fieldFunctions.add(getFieldFunction(varVel));
    solHist = createSolutionHistory(namedObjects, fieldFunctions);
    setUpdateEvent(repMon, updEvent);
    setUpdateEvent(solHist, updEvent);
    //setUpdateEvent(scene, updEvent);
    runCase();
    saveSim(simTitle + "_TRN", false);
  }
  
  void postSS() {    
    defColormap = getColormap("flames2");
    namedObjects.add(region);
    scene = createScene_Scalar(namedObjects, getFieldFunction(varVel), unit_mps, true);
    //scene.open(true);
    saveSim(simTitle + "_SS", false);
  }

  void postTRN() {    
    sd = (ScalarDisplayer) getDisplayer(scene, ".*");
    sd.getLegend().setLabelFormat("%.2f");
    sd.getScalarDisplayQuantity().setClip(false);
    sd.getScalarDisplayQuantity().setRange(new double[] {0., 0.4});
    //--
    solHist = getSolutionHistory(".*");
    recSolView = (RecordedSolutionView) createSolutionView(solHist);
    getDisplayer(scene, ".*").setRepresentation(recSolView.getRepresentation());
    printAction("Saving Pictures in several camera views...");
    postFlyOverAndSavePics(scene, vv1, null, 5 * fps, recSolView);
    postFlyOverAndSavePics(scene, vv1, vv2, 4 * fps, recSolView);
    postFlyOverAndSavePics(scene, null, null, 4 * fps, recSolView);
    sd.setDisplayMeshBoolean(true);
    postFlyOverAndSavePics(scene, null, null, 4 * fps, recSolView);
    postFlyOverAndSavePics(scene, vv2, vv3, 6 * fps, recSolView);
    postFlyOverAndSavePics(scene, null, null, 4 * fps, recSolView);
    postFlyOverAndSavePics(scene, vv3, vv1, 5 * fps, recSolView);
    postFlyOverAndSavePics(scene, null, null, 5 * fps, recSolView);
    sd.setDisplayMeshBoolean(false);
    postFlyOverAndSavePics(scene, null, null, recSolView.getMaxStateIndex() - postCurFrame + 1, recSolView);
  }

  int curFrame;
  int fps = 24;
  double Re = 500;
  ScalarDisplayer sd = null;
  
}
