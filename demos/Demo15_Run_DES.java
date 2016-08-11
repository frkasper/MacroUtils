import macroutils.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.vis.*;
import star.keturb.*;
import star.kwturb.*;
import star.turbulence.synturb.*;

/**
 * Runs the SST K-Omega DES model with IDDES formulation on a square channel under Re_L ~ 10k.
 * 
 * Notes:
 *    - If machine power is available (e.g.: >= 16 cores) run this macro in batch mode with a 
 *      mesh size of 1mm and using a R8 version of STAR-CCM+ (full double precision).
 *    - Residence time is around 0.1s. This macro will solve for 5 flows through.
 *    - Some inlet values are taken from a table ran in fully developed flow condition (periodic).
 * 
 * 
 * Geometry:
 *          
 *         +---------------------------------------------------------------+
 *      L /                                                               /|
 *       /                                                               / | L
 *      +---------------------------------------------------------------+  |
 *      |                                                               |  +
 *      |                                                               | / 
 *      |                                                               |/ 
 *      |             *-------------------------------------------------+
 *      |             | O(0,0,0)               H
 *      |             | 
 *      |             | 
 *      |             | V
 *      |             | 
 *      |             | 
 *      +-------------+ 
 *             L
 * 
 * @since Macro Utils v3.1.
 * @author Fabio Kasper
*/
public class Demo15_Run_DES extends MacroUtils {
    
  final double L = 50;
  final double V = 1.0 * L;
  final double H = 5.0 * L;
  double bsRANS = 6.;                   //-- Mesh Size in mm for RANS run.
  double bsDES = 2.;                    //-- Mesh Size in mm for DES run.
  double dtDES = 1e-4;                  //-- Timestep size in seconds for DES run.
  double mu = 1.855e-05;                //-- Viscosity.
  double rho0 = 1.204;                  //-- Reference Density.
  double tau = 0.1;                     //-- Approximate Residence Time in seconds.
  double maxTime = 5 * tau;             //-- Maximum Physical Time to be solved.

  public void execute() {
    _initUtils();
    simTitle = "Demo15";
    //--
    pre_RANS();
    post_RANS();
    runCase(true, true);
    saveSimWithSuffix("SS");
    hardCopyPictures(defPicResX, defPicResY, "SS_");
    //--
    pre_DES();
    post_DES();
    saveSimWithSuffix("DES_Ready");
    runCase(true, true);
    //--
    saveSim(simTitle);
    hardCopyPictures(defPicResX, defPicResY, "DES_");
  }
  
  void pre_RANS() {
    defCamView = readCameraView("cam1|5.530468e-02,-1.190976e-02,-7.199782e-02|4.282329e-01,2.739476e-01,5.776558e-01|-1.501948e-01,9.338231e-01,-3.246781e-01|8.359997e-02|1");
    if (hasValidVolumeMesh()) {
        sayLoud("Volume Mesh Found. Skipping Prep RANS");
        return;
    }
    cadBody = create3DCad_Block(new double[] {-L, -V, -L/2}, new double[] {0, L, L/2}, unit_mm, "Vertical");
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    getPartSurface(geomPrt, "y0").setPresentationName(bcInlet);
    geometryParts.add(geomPrt);
    cadBody = create3DCad_Block(new double[] {0, 0, -L/2}, new double[] {H, L, L/2}, unit_mm, "Horizontal");
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    geometryParts.add(geomPrt);
    getPartSurface(geomPrt, "x1").setPresentationName(bcOutlet);
    //-- Unite bodies
    mshOp = createMeshOperation_Unite(geometryParts);
    region = assignPartToRegion(getGeometryPart(mshOp.getPresentationName()));
    geometryParts2.addAll(region.getPartGroup().getObjects());
    //--
    mshBaseSize = bsRANS;
    mshOptCycles = 4;
    mshQualityThreshold = 0.7;
    prismsLayers = 6;
    prismsRelSizeHeight = 60;
    prismsNearCoreAspRat = 0.5;
    meshers = getMeshers(true, false, POLY, true);
    createMeshOperation_AutomatedMesh(geometryParts2, meshers, meshOpName);
    //--
    physCont = createContinua(_STEADY, _RKE_2LAYER, false);
    setSolverAggressiveURFs();
    //--
    bdry = getBoundary(".*" + bcInlet);
    setBC_VelocityMagnitudeInlet(bdry, 1, 20, 0.05, 10);
    bdry.getConditions().get(KeTurbSpecOption.class).setSelected(KeTurbSpecOption.INTENSITY_LENGTH_SCALE);
    table = createTable_File("TableFromPeriodicRun.csv");
    setBC_Values(bdry, table);
    //--
    setBC_PressureOutlet(getBoundary(".*" + bcOutlet), 0, 21, 0.05, 10);
    combineBoundaries(getBoundaries(".*\\...", false)).setPresentationName(bcWalls);
    //--
    genVolumeMesh();
    createScene_Mesh();
  }
  
  void post_RANS() {
    bdry1 = getBoundary(".*" + bcInlet);
    bdry2 = getBoundary(".*" + bcOutlet);
    createReportPressureDrop(bdry1, bdry2);
    createStoppingCriteria(repMon, "Asymptotic", 0.01, 50);
    namedObjects.addAll(getAllBoundaries());
    ff1 = getFieldFunction(varP);
    scene1 = createScene_Scalar(namedObjects, ff1, unit_Pa, true);
    scene1.setPresentationName(ff1.getPresentationName());
    plane = createSectionPlaneZ(coord0, "Plane");
    namedObjects2.add(plane);
    scene2 = createScene_Vector(namedObjects2, false);
    disp = createSceneDisplayer(scene2, "Geometry", namedObjects);
    disp.setOpacity(0.1); 
  }
  
  void pre_DES() {
    physCont = getPhysicsContinua(".*");
    region = getRegion(".*");
    clearSolutionHistory();
    getStoppingCriteria("Pressure.*").setIsUsed(false);
    //--
    mshOp = getMeshOperation(meshOpName);
    setMeshBaseSize(mshOp, bsDES, unit_mm);
    genVolumeMesh();
    trnTimestep = dtDES;
    trn2ndOrder = true;
    trnInnerIter = 6;
    trnMaxTime = maxTime;
    urfP = 0.8;
    urfVel = urfFluidEnrgy = urfKOmega = 0.9;
    physCont2 = createContinua(_IMPLICIT_UNSTEADY, _DES_SST_KW_IDDES, true);
    region.setPhysicsContinuum(physCont2);
    updateSolverSettings();
    //-- Activate Synthetic Eddy Generation at Inlet if desired (at a higher CPU cost).
    bdry = getBoundary(".*" + bcInlet);
    bdry.getConditions().get(PseudoTurbulenceSpecOption.class).setSelected(PseudoTurbulenceSpecOption.INTENSITY_LENGTH_SCALE);
  }
  
  void post_DES() {
    vv1 = getCameraView(".*");
    vv2 = readCameraView("cam2|1.841383e-02,2.223453e-02,-3.292230e-05|1.841383e-02,2.223453e-02,7.419417e-01|0.000000e+00,1.000000e+00,0.000000e+00|4.180352e-02|1");
    LookupTable cmap1 = getColormap("vectors5");
    LookupTable cmap2 = getColormap("hsv-vectors2");
    //-- Useful Reports and Update Events.
    createReports_Unsteady();
    updEvent1 = createUpdateEvent_DeltaTime(5 * dtDES, unit_s, true);
    updEvent1.setPresentationName("Pics");
    updEvent2 = createUpdateEvent_Range(getPhysicalTimeMonitor(), GE, 2 * tau);
    updEvent2.setPresentationName("Monitors");
    updEvent3 = createUpdateEvent_TimeStep(1000, 0);
    updEvent3.setPresentationName("AutoSave");
    //--
    setUpdateEvent(getMonitor("Pressure Drop"), updEvent1);
    namedObjects.addAll(getAllRegions());
    ff1 = createFieldFunction("My Q Criterion", "$Qcriterion * (1 - $MenterKwTurbF1)", null);
    ff2 = createFieldFunction("Courant Number on LES Portion", "$CourantNumber * (1 - $SaTurbDesFd)", null);
    //-- 
    //-- In order to use the above additional storage is needed.
    KwTurbSolver kw = (KwTurbSolver) getSolver(KwTurbSolver.class);
    kw.setLeaveTemporaryStorage(true);
    //-- 
    //-- If one is using a finer mesh (<= 2mm) it is better to tune the post processing.
    double isoVal = 5e4;
    double[] tvrRange = new double[] {0.0, 5.0};
    if (getMeshBaseSize(getMeshOperation("Poly.*"), true) <= 2.0) {
        isoVal = 1e5;
        tvrRange = new double[] {0.0, 2.0};
    }
    //-- 
    namedObjects2.clear();
    namedObjects2.add(createDerivedPart_Isosurface(namedObjects, ff1, isoVal, unit_Dimensionless));
    scene = createScene_Scalar(namedObjects2, getFieldFunction(varTVR), unit_Dimensionless, true);
    scenes.add(scene);
    scene.setPresentationName("Structures");
    fixScalarDisplayer(scene, false, tvrRange, 0, "%.0f", cmap2, dv0, vv1);
    namedObjects2 = getArrayList(getBoundary(bcWalls));
    disp = createSceneDisplayer(scene, "Geometry", namedObjects2);
    disp.setOpacity(0.1); 
    //-- 
    scene3 = createScene_Scalar(namedObjects2, getFieldFunction(varYplus), unit_Dimensionless, false);
    scene3.setPresentationName(varYplus);
    fixScalarDisplayer(scene3, false, new double[] {0, tvrRange[1]-1}, 0, "%.1f", cmap1, dv0, vv1);
    //-- 
    //-- Instantaneous and Mean Velocity Scalar Scenes
    ff3 = getFieldFunction(varVel);
    createMonitor_FieldMean(namedObjects, ff3.getMagnitudeFunction(), updEvent2);
    namedObjects2 = getArrayList(getDerivedPart("Plane"));
    scene = createScene_Scalar(namedObjects2, ff2, unit_Dimensionless, false);
    scene.setPresentationName("LES Timestep Assessment");
    String[] vars = {varVel, "Mean.*" + varVel + ".*", varTVR, "LES"};
    for (String var : vars) {
        double[] range = {0, 6};
        String fmt = "%.0f";
        LookupTable cmap = cmap2;
        if (var.equals("LES")) {
            scene = getScene(var + ".*");
            range[1] = 1;
            fmt = "%.1f";
        } else {
            ff = getFieldFunction(var);
            scene = createScene_Scalar(namedObjects2, ff, unit_mps, false);
            scene.setPresentationName(ff.getPresentationName());
            if (var.equals(varTVR)) {
                range[1] = tvrRange[1] + 1;
            }
            if (!var.contains("Mean")) {
                scenes.add(scene);
            }
        }
        scene.setAxesVisible(false);
        DoubleVector dv = new DoubleVector(new double[] {0.42, 0.025});
        fixScalarDisplayer(scene, true, range, 0.55, fmt, cmap, dv, vv2);
    }
    //--
    //-- Pressure Probe
    point = createDerivedPart_Point(namedObjects, new double[] {5, 10, 0.0});
    createReportMaximum(point, "P1", getFieldFunction(varP), unit_Pa);
    //-- Final make ups.
    scenes.add(getScene(varYplus));
    for (Scene scn : scenes) {
        setUpdateEvent(scn, updEvent1);
        setSceneSaveToFile(scn, defPicResX, defPicResY, 0);
        createAnnotation_Time(scn, new double[] {0.415, 0.92, 0.0});
    }
    for (Monitor m : getMonitors(".*")) {
        if (!isReport(m)) {
            continue;
        }
        setUpdateEvent(m, updEvent1);
    }
    setAutoSave(updEvent3, 3);
  }

  PhysicsContinuum createContinua(int type, int turb, boolean recom) {
    PhysicsContinuum pc = createPhysicsContinua(_3D, type, _GAS, 
            _SEGREGATED, _IDEAL_GAS, 0, turb, false, false, recom);
    setMaterialProperty(pc, "Air", varVisc, mu, unit_Pa_s);
    return pc;
  }

  void fixScalarDisplayer(Scene scn, boolean msh, double[] range, double w, 
                            String fmt, LookupTable cmap, DoubleVector dv, VisView cam) {
    printAction("Fixing Scalar Displayer for Scene: " + scn.getPresentationName());
    ScalarDisplayer sd = (ScalarDisplayer) getDisplayer(scn, "Scalar.*");
    sd.setDisplayMeshBoolean(msh);
    sd.getLegend().setLabelFormat(fmt);
    sd.getLegend().setLookupTable(cmap);
    if (range != null) {
        int i = (int) range[1];
        if (i == 1) i++;
        sd.getLegend().setNumberOfLabels(i + 1);
    }
    setSceneCameraView(scn, cam);
    if (range != null) {
        sd.getScalarDisplayQuantity().setAutoRange(false);
        sd.getScalarDisplayQuantity().setRange(range);
    }
    if (w > 0.) {
        sd.getLegend().setWidth(w);
        sd.getLegend().setPositionCoordinate(dv);
    }
  }
  
  String meshOpName = "Polys And Prisms";

}
