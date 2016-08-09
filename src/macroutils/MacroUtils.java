/*
 * MACRO Utils v1d
 * Programmer: Fabio Kasper
 * Revision: Sept 20, 2012.
 *
 * Macro Utils is a set of useful methods to assist the process of writing macros in STAR-CCM+.
 *
 * Some methods might not be available in older versions. Started coding this Macro in v7.02.
 *
 * HOW TO USE IT?
 *
 * 1) Store MacroUtils_v?? in a subfolder called 'macro';
 *  E.g.: C:\work\macro\MacroUtils_v1d.java
 *
 * 2) In STAR-CCM+, go to Menu -> Tools -> Options -> Environment.
 *  Under 'User Macro classpath' put 'C:\work'
 *
 *  or, alternatively, launch STAR-CCM+ in the command line as:
 *  <starccm+ -classpath "C:\work">
 *
 * 3) In another macro, just reference MacroUtils, to benefit from its methods:
 *
 *      package macro;
 *
 *      import star.base.neo.*;
 *      import star.base.report.*;
 *      import star.common.*;
 *
 *      public class MyMacro extends MacroUtils_v1a {
 *          public void execute() {
 *              for(Region region : getAllRegions()){
 *                  hasPolyMesh(region);
 *                  hasTrimmerMesh(region);
 *              }
 *          }
 *      }
 *
 */
package macroutils;

import java.util.*;
import java.text.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.dualmesher.*;
import star.energy.*;
import star.flow.*;
import star.keturb.*;
import star.material.*;
import star.meshing.*;
import star.metrics.*;
import star.motion.*;
import star.prismmesher.*;
import star.radiation.common.*;
import star.radiation.s2s.*;
import star.resurfacer.*;
import star.segregatedenergy.*;
import star.segregatedflow.*;
import star.solidmesher.*;
import star.surfacewrapper.*;
import star.trimmer.*;
import star.turbulence.*;
import star.viewfactors.*;
import star.vis.*;
import star.walldistance.*;

public class MacroUtils extends StarMacro {
  /***************************************************
   * Global definitions
   ***************************************************/
  boolean colorByRegion = true;
  boolean checkMeshQualityUponSurfaceMeshGeneration = false;
  boolean checkMeshQualityUponSurfaceMeshImport = false;
  boolean createMeshSceneUponSurfaceMeshGeneration = false;
  boolean fineTesselationOnImport = false;
  boolean saveIntermediates = true;
  boolean singleBoundary = false;
  boolean verbose = true;
  Boundary bdry = null, bdry1 = null, bdry2 = null, bdry3 = null;
  CadPart cadPrt = null, cadPrt1 = null, cadPrt2 = null, cadPrt3 = null;
  CellSurfacePart cellSet = null;
  CompositePart compPart = null, compPart1 = null, compPart2 = null, compPart3 = null;
  DirectBoundaryInterface intrfPair = null, intrfPair1 = null, intrfPair2 = null, intrfPair3 = null;
  double mshSharpEdgeAngle = 30;
  double[] camFocalPoint = {0., 0., 0.};
  double[] camPosition = {0., 0., 0.};
  double[] camViewUp = {0., 0., 0.};
  double[] coord1 = {0., 0., 0.};
  double[] coord2 = {0., 0., 0.};
  double[] point = {0., 0., 0.};
  FeatureCurve featCrv = null;
  File cadPath = null;
  File dbsPath = null;
  File myFile = null;
  File simFile = null;
  GeometryPart geomPrt = null, gp1 = null, gp2 = null, gp3 = null, gp4 = null;
  int autoSaveMaxFiles = 2;
  int autoSaveFrequencyIter = 1000;
  int colourByPart = 4;
  int colourByRegion = 2;
  int partColouring = colourByPart;
  int picResX = 800;
  int picResY = 600;
  int savedWithSuffix = 0;
  Interface intrf = null, intrf1 = null, intrf2 = null, intrf3 = null;
  LeafMeshPart leafMshPrt = null, leafMshPrt1 = null, leafMshPrt2 = null, leafMshPrt3 = null;
  MeshContinuum mshCont = null, mshCont1 = null, mshCont2 = null;
  PartSurface partSrf = null;
  PlaneSection plane = null, plane1 = null, plane2 = null;
  PhysicsContinuum physCont = null, physCont1 = null, physCont2 = null, physCont3 = null;
  Region region = null, region1 = null, region2 = null, region3 = null;
  ReportMonitor repMon = null, repMon1 = null, repMon2 = null, repMon3 = null;
  Scene scene = null, scene1 = null, scene2 = null;
  SimpleCylinderPart simpleCylPrt = null, simpleCylPrt1 = null, simpleCylPrt2 = null, simpleCylPrt3 = null;
  String autoSaveSeparator = "_backupIter";
  String contNameAir = "Air";
  String contNameAirBoussinesq = "Air Boussinesq";
  String contNameAluminum = "Aluminum";
  String contNameSteel = "Steel";
  String contNamePoly = "Poly Mesh";
  String contNameTrimmer = "Trimmer Mesh";
  String contNameThinMesher = "Thin Mesher";
  String contNameWater = "Water";
  String dbsSubDir = "parts";
  String noneString = "none";
  String sayPreffixString = "[*]";
  String simName = null;
  String simTitle = null;
  String simPath = null;
  String string = "", string1 = "", string2 = "", string3 = "", text = "";
  String varP = "Pressure";
  String varT = "Temperature";
  String wall = "wall";
  String walls = "walls";
  Simulation sim = null;
  /***************************************************
   * Physics
   ***************************************************/
  Units defUnitTemp = null;
  Units defUnitLength = null;
  Units defUnitPress = null;
  Units unit_C = null;
  Units unit_cP = null;
  Units unit_cmH2O = null;
  Units unit_F = null;
  Units unit_dynepcm2 = null;
  Units unit_g = null;
  Units unit_gpmin = null;                          // See definitions on initialize()
  Units unit_gps = null;
  Units unit_K = null;
  Units unit_kgph = null;
  Units unit_kph = null;
  Units unit_kgpmin = null;
  Units unit_lph = null;
  Units unit_lpmin = null;
  Units unit_lps = null;
  Units unit_m = null;
  Units unit_mm = null;
  Units unit_mmH2O = null;
  Units unit_mps = null;
  Units unit_P = null;
  Units unit_Pa = null;
  Units unit_Pa_s = null;
  Units unit_rpm = null;
  /***************************************************
   * Physics
   ***************************************************/
  double[] gravity = {0., -9.81, 0.};           // m/s^2
  double p0 = 0.0;                              // Pa
  double ti0 = 0.05;                            // turbulent intensity
  double tvr0 = 1.0;                            // turbulent viscosity ratio
  double tvs0 = 0.5;                            // m/s turbulent velocity scale
  double[] v0 = {0., 0., 0.};                   // m/s
  double clipMinT = -50;                        // deg C
  double clipMaxT = 3000;                       // deg C
  double fluidT0 = 22.;                         // deg C
  double solidsT0 = 60.;                        // deg C
  double Pref = 101325.;                        // Pa - Reference Pressure
  double Tref = 22.;                            // deg C - Reference Temperature
  double radEmissivity = 0.8;                   // default
  double radTransmissivity = 0.;                // default
  double radSharpAngle = 150.;                  // default
  double radPatchProp = 100.;                   // default
  /***************************************************
   * Preprocessing and Meshing
   ***************************************************/
  boolean skipMeshGeneration = false;
  boolean thinMeshIsPolyType = true;
  boolean tempMeshSizeSkip = false;
  double featCurveMeshMin = 10;
  double featCurveMeshTgt = 50;
  double mshBaseSize = 3.0;                     // mm
  double mshGrowthFactor = 1.0;                 // default = 1.0
  double mshOpsTol = 1e-4;                      // m
  double mshProximityPointsInGap = 2.0;
  double mshProximitySearchFloor = 0.0;         // mm
  double mshSrfCurvNumPoints = 36;              // points / curve
  double mshSrfSizeMin = 25;                    // (%)
  double mshSrfSizeTgt = 100;                   // (%)
  double mshTrimmerMaxCelSize = 10000;          // (%)
  double mshWrapperFeatureAngle = 30.;          // degrees
  double mshWrapperScaleFactor = 100.;          // (%)
  double prismsLyrChoppPerc = 25.0;             // (%)
  double prismsMinThickn = 5.0;                 // (%)
  double prismsNearCoreAspRat = 0.5;
  double prismsRelSizeHeight = 30.;             // (%)
  double prismsStretching = 1.2;
  int intrfInt = 3;
  int maxInterfaces = 10000;
  int prismsLayers = 1;
  int thinMeshLayers = 2;
  String mshTrimmerGrowthRate = "medium";
  /***************************************************
   * Remove Invalid Cells Settings
   ***************************************************/
  boolean aggressiveRemoval = false;
  double minFaceValidity = 0.51;
  double minCellQuality = 1e-8;
  double minVolChange = 1e-10;
  int minContigCells = 1;
  double minConFaceAreas = 0.;
  double minCellVolume = 0.;
  /***************************************************
   * Solver Settings
   ***************************************************/
  boolean rampURF = false;
  int maxIter = 1000;                   // default = 1000
  double urfVel = 0.7;                  // default = 0.7
  double urfP = 0.3;                    // default = 0.3
  double urfFluidEnrgy = 0.8;           // default = 0.9
  double urfSolidEnrgy = 0.9;           // default = 0.99
  double urfKEps = 0.7;                 // default = 0.8
  double urfRampFlBeg = 0.6;
  double urfRampSldBeg = 0.7;
  int urfRampFlIterBeg = 100;
  int urfRampFlIterEnd = 1000;
  int urfRampSldIterBeg = 100;
  int urfRampSldIterEnd = 1000;
  /***************************************************
   * Miscellaneous definitions
   ***************************************************/
  String[] arrayPartSurfacesSplitAngleException = {};
  Vector<Boundary> vecBdry = new Vector<Boundary>();
  Vector<Boundary> vecBdry1 = new Vector<Boundary>();
  Vector<Boundary> vecBdry2 = new Vector<Boundary>();
  Vector<Boundary> vecBdry3 = new Vector<Boundary>();
  Vector<CadPart> vecCadPrt = new Vector<CadPart>();
  Vector<CadPart> vecCadPrt1 = new Vector<CadPart>();
  Vector<CadPart> vecCadPrt2 = new Vector<CadPart>();
  Vector<CadPart> vecCadPrt3 = new Vector<CadPart>();
  Vector<CompositePart> vecCompPrt = new Vector<CompositePart>();
  Vector<CompositePart> vecCompPrt1 = new Vector<CompositePart>();
  Vector<CompositePart> vecCompPrt2 = new Vector<CompositePart>();
  Vector<CompositePart> vecCompPrt3 = new Vector<CompositePart>();
  Vector<GeometryPart> vecGeomPrt = new Vector<GeometryPart>();
  Vector<GeometryPart> vecGeomPrt1 = new Vector<GeometryPart>();
  Vector<GeometryPart> vecGeomPrt2 = new Vector<GeometryPart>();
  Vector<GeometryPart> vecGeomPrt3 = new Vector<GeometryPart>();
  Vector<LeafMeshPart> vecLeafMshPrt = new Vector<LeafMeshPart>();
  Vector<MeshPart> vecMshPrt = new Vector<MeshPart>();
  Vector<MeshPart> vecMshPrt1 = new Vector<MeshPart>();
  Vector<MeshPart> vecMshPrt2 = new Vector<MeshPart>();
  Vector<MeshPart> vecMshPrt3 = new Vector<MeshPart>();
  Vector<MeshContinuum> vecMeshContinua = new Vector<MeshContinuum>();
  Vector<PartCurve> vecPartCrv = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv1 = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv2 = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv3 = new Vector<PartCurve>();
  Vector<PartSurface> vecPartSrf = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf1 = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf2 = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf3 = new Vector<PartSurface>();
  Vector<PhysicsContinuum> vecPhysicsContinua = new Vector<PhysicsContinuum>();
  Vector<Region> vecReg = new Vector<Region>();
  Vector<ReportMonitor> vecRepMon = new Vector<ReportMonitor>();
  Vector vecObj = new Vector();
  Vector vecObj2 = new Vector();
  Vector objVec = new Vector();
  int sayLevel = 1;
  MeshContinuum mshContPoly = null;
  MeshContinuum mshContTrimmer = null;
  FilenameFilter dbsFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
        // Return files ending with dbs -- with ignore case group (?i)
        return name.matches("(?i).*dbs");
    }
  };

  public void execute() {
    for(Region region : getAllRegions()){
        hasPolyMesh(region);
        hasTrimmerMesh(region);
    }
  }

  public void initialize() {
    /***************************************************************/
    /* Remember to initialize 'sim' before calling everything else */
    sim = getActiveSimulation();
    /***************************************************************/
    printAction("Storing necessary variables");
    simFile = new File(sim.getSessionPath());
    simTitle = sim.getPresentationName();
    simPath = simFile.getParent();
    cadPath = new File(simPath, "CAD");
    dbsPath = new File(simPath, "DBS");
    say("Simulation File: " + simFile.toString());
    say("Simulation Name: " + simTitle);
    say("Simulation Path: " + simPath);
    if (colorByRegion){
        partColouring = colourByRegion;
    }
    updateMeshContinuaVector();
    /*
     * Units section -- See corresponding method below
     */
    updateOrCreateNewUnits();
  }

  public void runCase(){
    runCase(0);
  }

  public void runCase(int n){
    if(!hasValidVolumeMesh()){
        printAction("Running the case");
        say("No volume mesh found. Skipping run.");
        return;
    }
    if(n > 0){
        printAction("Running " + n + " iterations of the case");
        sim.getSimulationIterator().step(n);
    } else {
        printAction("Running the case");
        sim.getSimulationIterator().run();
    }
  }

  public void finalize(){
    printAction("DONE!");
    if(!saveIntermediates || savedWithSuffix < 1){
        saveSim(simTitle);
    }
  }

  /****************************************************
   *
   * Useful Methods Area
   *
   ****************************************************/

  private PhysicsContinuum _chPhys_SegrFlTemp(PhysicsContinuum phC) {
    phC.enable(SegregatedFluidTemperatureModel.class);
    phC.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(defUnitTemp);
    phC.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    phC.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(defUnitTemp);
    phC.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    StaticTemperatureProfile stp = phC.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(fluidT0);
    return phC;
  }

  private PhysicsContinuum _chPhys_SegrFlBoussinesq(PhysicsContinuum phC, double thermalExpansion) {
    phC.enable(GravityModel.class);
    phC.enable(BoussinesqModel.class);
    SingleComponentGasModel sgm = phC.getModelManager().getModel(SingleComponentGasModel.class);
    Gas gas = ((Gas) sgm.getMaterial());
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ThermalExpansionProperty.class).getMethod());
    cmpm.getQuantity().setValue(thermalExpansion);
    updatePhysicsGravityAndReferenceTemperature(phC);
    return phC;
  }

  private  PhysicsContinuum _chPhys_TurbKEps2Lyr(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(KEpsilonTurbulence.class);
    phC.enable(RkeTwoLayerTurbModel.class);
    phC.enable(KeTwoLayerAllYplusWallTreatment.class);
    TurbulentVelocityScaleProfile tvs = phC.getInitialConditions().get(TurbulentVelocityScaleProfile.class);
    tvs.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvs0);
    TurbulenceIntensityProfile tip = phC.getInitialConditions().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti0);
    TurbulentViscosityRatioProfile tvrp = phC.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr0);
    return phC;
  }

  private MeshContinuum _crMsh_Poly(){
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNamePoly);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(DualMesherModel.class);
    disableSurfaceProximityRefinement(mshCont);

    setMeshBaseSize(mshCont, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(mshCont, mshSrfCurvNumPoints);
    setMeshTetPolyGrowthRate(mshCont, mshGrowthFactor);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContPoly = mshCont;
    return mshCont;
  }

  private MeshContinuum _crMsh_ThinMesher() {
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNameThinMesher);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(SolidMesherModel.class);
    disableSurfaceProximityRefinement(mshCont);
    mshCont.getModelManager().getModel(SolidMesherModel.class).setOptimize(true);

    setMeshBaseSize(mshCont, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshCurvatureNumberOfPoints(mshCont, mshSrfCurvNumPoints);
    mshCont.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContPoly = mshCont;
    return mshCont;
  }

  private MeshContinuum _crMsh_Trimmer() {
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNameTrimmer);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(TrimmerMeshingModel.class);

    setMeshBaseSize(mshCont, mshBaseSize, defUnitLength);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    setMeshPerRegionFlag(mshCont);
    setMeshCurvatureNumberOfPoints(mshCont, mshSrfCurvNumPoints);
    mshCont.getReferenceValues().get(MaximumCellSize.class).getRelativeSize().setPercentage(mshTrimmerMaxCelSize);
    int i = getTrimmerGrowthRate(mshTrimmerGrowthRate);
    mshCont.getReferenceValues().get(SimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(i);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContTrimmer = mshCont;
    return mshCont;
  }

  private PhysicsContinuum _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(Class singleComponentClass, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum phC = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    phC.enable(ThreeDimensionalModel.class);
    phC.enable(SteadyModel.class);
    phC.enable(singleComponentClass);
    phC.enable(SegregatedFlowModel.class);
    phC.enable(ConstantDensityModel.class);
    phC.enable(LaminarModel.class);
    phC.enable(CellQualityRemediationModel.class);
    InitialPressureProfile ipp = phC.getInitialConditions().get(InitialPressureProfile.class);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(p0);
    VelocityProfile vp = phC.getInitialConditions().get(VelocityProfile.class);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(v0[0], v0[1], v0[2]);
    updateOrCreateNewUnits();
    return phC;
  }

  private PhysicsContinuum _crPhys_3D_SS_SegrSLD(String solidMaterial, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum sldCont = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    sldCont.enable(ThreeDimensionalModel.class);
    sldCont.enable(SteadyModel.class);
    sldCont.enable(SolidModel.class);
    sldCont.enable(SegregatedSolidEnergyModel.class);
    sldCont.enable(ConstantDensityModel.class);
    sldCont.enable(CellQualityRemediationModel.class);
    sldCont.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(defUnitTemp);
    sldCont.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    sldCont.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(defUnitTemp);
    sldCont.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    StaticTemperatureProfile stp = sldCont.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(solidsT0);
    if(solidMaterial.matches(noneString)){ return sldCont; }
    SolidModel sm = sldCont.getModelManager().getModel(SolidModel.class);
    Solid solid = ((Solid) sm.getMaterial());
    MaterialDataBase matDB = sim.get(MaterialDataBaseManager.class).getMaterialDataBase("props");
    DataBaseMaterialManager dbMatMngr = matDB.getFolder("Solids");
    DataBaseSolid dbSld = ((DataBaseSolid) dbMatMngr.getMaterial(solidMaterial));
    Solid newSolid = (Solid) sm.replaceMaterial(solid, dbSld);
    updateOrCreateNewUnits();
    return sldCont;
  }

  public Units addUnit(String name, String desc, double conversion, int[] dimensionList) {
    UserUnits newUnit;
    try{
        newUnit = (UserUnits) sim.getUnitsManager().getUnits(name);
        say("Unit already exists: " + name);
    } catch (Exception e){
        say("Creating Unit: " + name);
        newUnit = sim.getUnitsManager().createUnits("Units");
        newUnit.setPresentationName(name);
        newUnit.setDescription(desc);
        newUnit.setConversion(conversion);
        newUnit.setDimensionsVector(new IntVector(dimensionList));
    }
    return newUnit;
  }

  public void assignAllLeafPartsToRegions(){
    printAction("Assigning all Leaf Parts to Regions");
    sim.getRegionManager().newRegionsFromParts(getAllLeafParts(), "OneRegionPerPart",
            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
  }

  public void assignLeafPartsToRegions(Collection<GeometryPart> colGP){
    printAction("Assigning Leaf Parts to Different Regions");
    say("Number of Leaf Parts: " + colGP.size());
    for(GeometryPart gp : colGP){
        say("  " + gp.getPathInHierarchy());
    }
    String bdryMode = "OneBoundaryPerPartSurface";
    if(singleBoundary){
        bdryMode = "OneBoundary";
    }
    RegionManager regMan = sim.getRegionManager();
    regMan.newRegionsFromParts(colGP, "OneRegionPerPart", null, bdryMode,
                                        null, "OneFeatureCurve", null, true);
  }

  public Region assignAllLeafPartsToOneSingleRegion(){
    printAction("Assigning all Leaf Parts to a Single Region");
    String bdryMode = "OneBoundaryPerPartSurface";
    if(singleBoundary){
        bdryMode = "OneBoundary";
    }
    RegionManager regMan = sim.getRegionManager();
    Collection<GeometryPart> colGP = getAllLeafParts();
    String regMode = "OneRegion";
    Region region0 = null;
    if(colGP.size() == 1){
        region0 = sim.getRegionManager().createEmptyRegion();
        region0.getBoundaryManager().remove(region0.getBoundaryManager().getDefaultBoundary());
    } else {
        regMode = "OneRegionPerPart";
    }
    regMan.newRegionsFromParts(colGP, regMode, region0, bdryMode, null, "OneFeatureCurve", null, false);
    return region0;
  }

  public void assignLeafMeshPartToRegion(LeafMeshPart lmp){
    printAction("Assigning '" + lmp.getPresentationName() + "' to a Region");
    sim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[] {lmp}),
        "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve",
        null, false);
  }

  public void assignLeafMeshPartToRegionByName(String lmpName){
    LeafMeshPart lmp = ((LeafMeshPart) sim.get(SimulationPartManager.class).getPart(lmpName));
    assignLeafMeshPartToRegion(lmp);
  }

  public Region assignLeafPartsToOneSingleRegion(Collection<GeometryPart> colGP){
    printAction("Assigning Leaf Parts to a Single Region");
    say("Number of Leaf Parts: " + colGP.size());
    String regMode = "OneRegion";
    Region region0 = null;
    if(colGP.size() == 1){
        regMode = "OneRegionPerPart";
    } else {
        region0 = sim.getRegionManager().createEmptyRegion();
        region0.getBoundaryManager().remove(region0.getBoundaryManager().getDefaultBoundary());
    }
    for(GeometryPart gp : colGP){
        say("  " + gp.getPathInHierarchy());
    }
    String bdryMode = "OneBoundaryPerPartSurface";
    if(singleBoundary){
        bdryMode = "OneBoundary";
    }
    RegionManager regMan = sim.getRegionManager();
    regMan.newRegionsFromParts(colGP, regMode, region0, bdryMode, null, "OneFeatureCurve", null, false);
    return region0;
  }

  @Deprecated
  public void assignRegionToMeshContinua(Region region, MeshContinuum mshCont){
    enableMeshContinua(mshCont, region);
  }

  @Deprecated
  public void assignRegionToPhysicsContinua(Region region, PhysicsContinuum physCont){
    enablePhysicsContinua(physCont, region);
  }

  public void changeSceneCamView(Scene scene, double[] focalPoint, double[] position,
                                                    double[] viewUp, double parallelScale){
    int projMode = 1;
    CurrentView cv = scene.getCurrentView();
    DoubleVector vecFP = new DoubleVector(focalPoint);
    DoubleVector vecPos = new DoubleVector(position);
    DoubleVector vecVU = new DoubleVector(viewUp);
    cv.setInput(vecFP, vecPos, vecVU, parallelScale, projMode);
  }

  private void checkFreeEdgesAndNonManifolds(SurfaceMeshWidgetDiagnosticsController diagCtrl,
                                                SurfaceMeshWidgetRepairController repairCtrl){
    diagCtrl.setCheckFreeEdges(true);
    diagCtrl.setFreeEdgesActive(true);
    diagCtrl.setCheckNonmanifoldEdges(true);
    diagCtrl.setNonmanifoldEdgesActive(true);
    diagCtrl.setCheckNonmanifoldVertices(true);
    diagCtrl.setNonmanifoldVerticesActive(true);

    diagCtrl.runDiagnostics();
    int freeEdg = diagCtrl.getNumFreeEdges();
    int nonManEdg = diagCtrl.getNumNonmanifoldEdges();
    int nonManVert = diagCtrl.getNumNonmanifoldVertices();
    int maxHoles = 100;

    if(freeEdg > 0){
        say("****************************************************");
        say("**                   WARNING!!!                   **");
        say("**                                                **");
        say("**               FREE EDGES FOUND!                **");
        say("****************************************************");
        if(freeEdg < 100) {
            say("Attempting to auto-fill holes.");
            diagCtrl.selectFreeEdges();
            repairCtrl.holeFillSelectedEdges();
            say("Rerunning Diagnostics...");
            diagCtrl.runDiagnostics();
            freeEdg = diagCtrl.getNumFreeEdges();
            nonManEdg = diagCtrl.getNumNonmanifoldEdges();
            nonManVert = diagCtrl.getNumNonmanifoldVertices();
            say("Number of Free Edges: " + int2string(freeEdg));
            say("Number of Non-Manifold Edges: " + int2string(nonManEdg));
            say("Number of Non-Manifold Vertices: " + int2string(nonManVert));
        } else {
            say("WARNING! Too many holes found in the model. More than " + maxHoles);
            say("Giving up!");
        }
    }
  }

  public void checkFreeEdgesAndNonManifoldsOnLeafMeshPart(LeafMeshPart lmp) {
    printAction("Checking For Free Edges and Non-Manifold Edges & Vertices");
    say("Checking Leaf Mesh Part: " + lmp.getPresentationName());

    Scene scene_1 = sim.getSceneManager().createScene("Repair Surface");
    scene_1.initializeAndWait();

    // Calling in Geometry Representation. Just to be sure
    PartRepresentation partRep = queryGeometryRepresentation();
    PartSurfaceMeshWidget prtSrfMshWidget = partRep.startSurfaceMeshWidget(scene_1);
    prtSrfMshWidget.setActiveParts(new NeoObjectVector(new Object[] {lmp}));

    prtSrfMshWidget.startSurfaceMeshDiagnostics();
    prtSrfMshWidget.startSurfaceMeshRepair();

    SurfaceMeshWidgetDiagnosticsController diagCtrl =
      prtSrfMshWidget.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    SurfaceMeshWidgetRepairController repairCtrl =
      prtSrfMshWidget.getControllers().getController(SurfaceMeshWidgetRepairController.class);

    Collection<PartSurface> colPS = lmp.getPartSurfaceManager().getPartSurfaces();
    lmp.getPartSurfacesSharingPatches(prtSrfMshWidget, ((Vector) colPS), new NeoObjectVector(new Object[] {}));

    checkFreeEdgesAndNonManifolds(diagCtrl, repairCtrl);

    prtSrfMshWidget.stop();
    sim.getSceneManager().deleteScenes(new NeoObjectVector(new Object[] {scene_1}));
    sayOK();
  }

  public void checkFreeEdgesAndNonManifoldsOnParts(){
    printAction("Checking for Free Edges and Non-Manifolds Edges/Vertices on All Parts");
    for(LeafMeshPart lmp : getAllLeafMeshParts()){
        checkFreeEdgesAndNonManifoldsOnLeafMeshPart(lmp);
    }
  }

  public void checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions() {
    printAction("Checking For Free Edges and Non-Manifold Edges & Vertices");
    say("Checking Surface Mesh in All regions");

    setVerboseOff();
    Vector<Region> vecReg = new Vector<Region>();
    for(Region region : getAllRegions()){
        if(isRemesh(region.getMeshContinuum())){
            vecReg.add(region);
        }
    }
    if(vecReg.size() == 0){
        say("No Remeshing Regions to be checked.");
        return;
    }

    Scene scene_1 = sim.getSceneManager().createScene("Repair Surface");
    scene_1.initializeAndWait();

    SurfaceMeshWidget srfMshWidget = queryRemeshedSurface().startSurfaceMeshWidget(scene_1);
    srfMshWidget.setActiveRegions((Vector) getAllRegions());
    setVerboseOn();

    srfMshWidget.startSurfaceMeshDiagnostics();
    srfMshWidget.startSurfaceMeshRepair();
    srfMshWidget.startMergeImprintController();

    SurfaceMeshWidgetDiagnosticsController diagCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    SurfaceMeshWidgetRepairController repairCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetRepairController.class);
    SurfaceMeshWidgetDisplayController srfMshWidgetDispCtrl =
      srfMshWidget.getControllers().getController(SurfaceMeshWidgetDisplayController.class);
    SurfaceMeshWidgetDisplayer srfMshWidgetDisplayer =
      ((SurfaceMeshWidgetDisplayer) scene_1.getDisplayerManager().getDisplayer("Widget displayer 1"));

    srfMshWidgetDispCtrl.showAllFaces();
    srfMshWidgetDisplayer.initialize();

    checkFreeEdgesAndNonManifolds(diagCtrl, repairCtrl);

    srfMshWidget.stop();
    sim.getSceneManager().deleteScenes(new NeoObjectVector(new Object[] {scene_1}));
    sayOK();
  }

  public void cleanUpSimulationFile(){
    /*
     * This method cleans everything but the Volume Mesh, and Regions, of course.
     */
    /**************************************************
     * STILL WORKING ON THIS
     **************************************************/
    printAction("Cleaning Up Simulation file to save space");
    say("\nDisabling Mesh Continua in All Regions");
    for(Region region : getAllRegions()){
        MeshContinuum mshCont = region.getMeshContinuum();
        if(mshCont == null){ continue; }
        sim.getContinuumManager().eraseFromContinuum(new NeoObjectVector(new Object[] {region}), mshCont);
    }
    say("\nClearing Surface Mesh Representations");
    clearMeshes();
    clearParts();       // WATCH OUT ON THIS
  }

  public void clearAllMeshesRegionsAndParts() {
    setVerboseOff();
    clearScenes();
    clearMeshes();
    clearInterfaces();
    clearRegions();
    clearParts();
    setVerboseOn();
  }

  public void clearInterfaces() {
    printAction("Removing all Interfaces");
    Collection<Interface> colIntrf = sim.getInterfaceManager().getObjects();
    if(colIntrf.isEmpty()) {
        say("No Interfaces found.");
        return;
    }
    say("Removing " + colIntrf.size() + " Interface(s)");
    sim.getInterfaceManager().deleteInterfaces((Vector) colIntrf);
    sayOK();
  }

  public void clearMeshes() {
    printAction("Removing all Mesh Representations");
    try{
        sim.getRepresentationManager().removeObjects(queryRemeshedSurface());
        say("Remeshed Surface Representation removed.");
    } catch (Exception e1) {
        say("Remeshed Surface Representation not found.");
    }
    try{
        sim.getRepresentationManager().removeObjects(queryWrappedSurface());
        say("Wrapped Surface Representation removed.");
    } catch (Exception e1) {
        say("Wrapped Surface Representation not found.");
    }
    try{
        sim.getRepresentationManager().removeObjects(queryInitialSurface());
        say("Initial Surface Representation removed.");
    } catch (Exception e1) {
        say("ERROR removing Initial Surface Representation.");
    }
  }

  public void clearRegions() {
    printAction("Removing all Regions");
    Collection<Region> colReg = getAllRegions();
    if(colReg.isEmpty()) {
        say("No Regions found.");
        return;
    }
    say("Removing " + colReg.size() + " Region(s)");
    sim.getRegionManager().removeRegions((Vector) colReg);
    sayOK();
  }

  public void clearParts() {
    printAction("Removing all Parts");
    Collection<GeometryPart> gParts = getAllGeometryParts();
    if (!gParts.isEmpty()) {
        try {
            say("Removing Geometry Parts...");
            sim.get(SimulationPartManager.class).removeParts(gParts);
            sayOK();
        } catch (Exception e0) {
            say("Error removing Leaf Parts. Moving on.");
        }
    }
    Collection<GeometryPart> leafParts = getAllLeafParts();
    if (!leafParts.isEmpty()) {
        try {
            say("Removing Leaf Parts...");
            sim.get(SimulationPartManager.class).removeParts(leafParts);
            sayOK();
        } catch (Exception e1) {
            say("Error removing Leaf Parts. Moving on.");
        }
    }
    Vector<CompositePart> compParts = ((Vector) getAllCompositeParts());
    if (!compParts.isEmpty()) {
        try {
            say("Removing Composite Parts...");
            sim.get(SimulationPartManager.class).removeParts(compParts);
            sayOK();
        } catch (Exception e2) {
            say("Error removing Composite Parts. Moving on.");
        }
    }
  }

  public void clearScenes(){
    printAction("Removing all Scenes");
    try{
        Collection<Scene> colSc = sim.getSceneManager().getScenes();
        say("Removing " + colSc.size() + " Scene(s)");
        sim.getSceneManager().removeObjects(colSc);
        sayOK();
    } catch (Exception e){
        say("No Scenes found.");
    }
  }

  public Boundary combineAllBoundaries(Region region){
    printAction("Combining All Boundaries from Region");
    sayRegion(region);
    Vector<Boundary> vecBdry = (Vector) getAllBoundariesFromRegion(region);
    say("Boundaries found: " + vecBdry.size());
    if(vecBdry.size() < 2){
        say("Not enough boundaries to combine. Skipping...");
    } else {
        sim.getMeshManager().combineBoundaries(vecBdry);
    }
    return region.getBoundaryManager().getDefaultBoundary();
  }

  public Boundary combineAllBoundaries(Vector<Boundary> vecBdry){
    printAction("Combining All Boundaries");
    say("Boundaries provided: " + vecBdry.size());
    if(vecBdry.size() < 2){
        say("Not enough boundaries to combine. Skipping...");
    } else {
        sim.getMeshManager().combineBoundaries(vecBdry);
    }
    return vecBdry.firstElement();
  }

  public void combineCompositePartsByName(String regexPatt, boolean combinePartSurfaces){
    printAction(String.format("Combining Composite Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<GeometryPart> vecLP = (Vector) getAllLeafPartsByName(regexPatt);
    for(int i = 0; i < vecLP.size(); i++){
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecLP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts((MeshPart) vecLP.firstElement(), vecMP);
    say("Combined into: " + vecLP.firstElement().getPathInHierarchy());
    if(combinePartSurfaces){
        say("\nCombining Part Surfaces");
        cadPrt = (CadPart) vecLP.firstElement();
        Collection<PartSurface> colPS = cadPrt.getPartSurfaces();
        cadPrt.combinePartSurfaces(colPS);
        say("Combined " + colPS.size() + " Part Surfaces into 1.");
    }
    sayOK();
  }

  public void combineLeafMeshPartsByName(String regexPatt){
    printAction(String.format("Combining Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<LeafMeshPart> vecLMP = (Vector) getAllLeafMeshPartsByName(regexPatt);
    for(int i = 0; i < vecLMP.size(); i++){
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecLMP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts(vecLMP.firstElement(), vecMP);
    say("Combined into: " + vecLMP.firstElement().getPresentationName());
    sayOK();
  }

  public PartSurface combinePartSurfaces(Collection<PartSurface> colPS, String renameTo){
    printAction("Combining Part Surfaces");
    String myPS = "___myPartSurface";
    say("Part Surfaces available: " + colPS.size());
    for(PartSurface ps : colPS){
        ps.setPresentationName(myPS);
    }
    // Combine faces
    LeafMeshPart lmf = (LeafMeshPart) colPS.iterator().next().getPart();
    lmf.combinePartSurfaces(colPS);
    // Reloop to make sure it finds the correct Part Surface
    colPS = lmf.getPartSurfaceManager().getPartSurfaces();
    PartSurface foundPS = null;
    for(PartSurface ps : colPS){
        String name = ps.getPresentationName();
        if(name.startsWith(myPS)){
            foundPS = ps;
            ps.setPresentationName(renameTo);
            break;
        }
    }
    say("Combined into: " + foundPS.getPresentationName());
    sayOK();
    return foundPS;
  }

  public PartSurface combinePartSurfaces(LeafMeshPart lmf, String regexPatt, String renameTo){
    printAction("Combining Part Surfaces");
    String myPS = "___myPartSurface";
    say("Leaf Mesh Part: " + lmf.getPresentationName());
    sayA(String.format("Getting Part Surfaces by REGEX pattern: \"%s\"", regexPatt));
    Collection<PartSurface> colPS = lmf.getPartSurfaceManager().getPartSurfaces();
    say("Part Surfaces available: " + colPS.size());
    Vector<PartSurface> vectPS = new Vector<PartSurface>();
    for(PartSurface ps : colPS){
        if(ps.getPresentationName().matches(regexPatt)){
            ps.setPresentationName(myPS);
            vectPS.add(ps);
        }
    }
    say("Part Surfaces found by REGEX: " + vectPS.size());
    // Combine faces
    lmf.combinePartSurfaces(vectPS);
    // Reloop to make sure it finds the correct Part Surface
    colPS = lmf.getPartSurfaceManager().getPartSurfaces();
    PartSurface foundPS = null;
    for(PartSurface ps : colPS){
        String name = ps.getPresentationName();
        if(name.startsWith(myPS)){
            foundPS = ps;
            ps.setPresentationName(renameTo);
            break;
        }
    }
    say("Combined into: " + foundPS.getPresentationName());
    sayOK();
    return foundPS;
  }

  public void convertAllInterfacesToIndirect(){
    printAction("Converting all Interfaces to Indirect");
    for(Object intrfObj : getAllInterfacesAsObjects()){
        convertInterface_Direct2Indirect((DirectBoundaryInterface) intrfObj);
    }
    sayOK();
  }

  public void convertAllFluidSolidInterfacesToIndirect(){
    printAction("Converting all Fluid-Solid Direct Interfaces to Indirect");
    for(Object intrfObj : getAllInterfacesAsObjects()){
        DirectBoundaryInterface intrfPair = (DirectBoundaryInterface) intrfObj;
        if(isFluidSolidInterface(intrfPair)){
            convertInterface_Direct2Indirect(intrfPair);
        } else {
            say("Not Fluid-Solid interface. Skipping.");
        }
    }
    sayOK();
  }

  public void convertInterface_Direct2Indirect(DirectBoundaryInterface intrfPair){
    say("Converting a Fluid-Solid Direct to Indirect type Interface");
    say("  Interface name: " + intrfPair.getPresentationName());
    Boundary b0 = intrfPair.getParentBoundary0();
    Boundary b1 = intrfPair.getParentBoundary1();
    say("");
    say("  Removing Direct Interface...");
    sim.getInterfaceManager().deleteInterfaces(intrfPair);
    say("  Creating Indirect Interface...");
    sim.getInterfaceManager().createIndirectInterface(b0, b1);
    sayOK();
  }

  public CellSurfacePart createCellSet(Vector objVector, String name){
    printAction("Creating Cell Set with Objects");
    say("Objects: " + objVector.size());
    for(Object obj : objVector){
        say("  " + obj.toString());
    }
    CellSurfacePart cellSet = sim.getPartManager().createCellSurfacePart(objVector);
    cellSet.setPresentationName(name);
    sayOK();
    return cellSet;
  }

  public void createContactPreventionBetweenAllBoundaries(Region region, double value, Units unit){
    printAction("Creating a Contact Prevention between All Boundaries");
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(getAllBoundariesFromRegion(region));
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString());
    sayOK();
  }

  public void createContactPreventionBetweenAllParts(Region region, double value, Units unit){
    /*
     * Use this with care. It can swaps the machine memory easily.
     */
    printAction("Creating a Contact Prevention between All Parts Belonging to a Region");
    sayRegion(region);
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    for(GeometryPart gp : getAllLeafParts()){
        say(gp.getPresentationName());
        if(gp.hasRegion() == region){
            vecGP.add(gp);
        }
    }
    cps.getPartGroup().setObjects(vecGP);
    /*
     * The code is not accepting only Parts Contacts, so All Boundaries will be made contacts too.
     */
    cps.getBoundaryGroup().setObjects(getAllBoundariesFromRegion(region));
    /*
     */
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString());
    sayOK();
  }

  public void createContactPreventionBetweenBoundaries(Region region, Vector<Boundary> vecBdries, double value, Units unit){
    printAction("Creating a Contact Prevention between boundaries");
    say("Input boundaries: " + vecBdries.size());
    for (Boundary bdry : vecBdries) {
        sayBdry(bdry);
    }
    if(vecBdries.size() < 2){
        say("ERROR! Input boundaries number MUST > 2. Skipping...");
        return;
    }
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(vecBdries);
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString());
    sayOK();
  }

  public void createContactPreventionBetweenBoundariesByName(Region region, String[] regexPattArray, double value, Units unit){
    printAction("Creating a Contact Prevention between boundaries");
    say("Looking for boundaries matching: ");
    Vector<Boundary> vecB = new Vector<Boundary>();
    for (int i = 0; i < regexPattArray.length; i++) {
        String srchFor = regexPattArray[i];
        say("  " + srchFor);
        vecB.addAll(getAllBoundariesByName(srchFor));
        say("");
    }
    if(vecB.size() < 2){
        say("Input boundaries: " + vecB.size());
        say("ERROR! Input boundaries number MUST > 2. Skipping...");
        return;
    }
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(vecB);
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString());
    sayOK();
  }

  public DirectBoundaryInterface createDirectInterfacePair(Boundary bdry1, Boundary bdry2){
    printAction("Creating Direct Interface Pair given two boundaries");
    DirectBoundaryInterface intrfPair = sim.getInterfaceManager().createDirectInterface(bdry1, bdry2, "In-place");
    intrfPair.getTopology().setSelected(InterfaceConfigurationOption.IN_PLACE);
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  public DirectBoundaryInterface createDirectInterfacePairByName(String regexPatt){
    printAction("Creating Direct Interface Pair by matching names in any regions");
    Vector<Boundary> vecBdry = (Vector) getAllBoundariesByName(regexPatt);
    if(vecBdry.size() == 2){
        say("Found 2 candidates. Interfacing:");
    } else if(vecBdry.size() > 2){
        say("Found more than 2 candidates. Interfacing the first two:");
    } else if(vecBdry.size() < 1) {
        say("Could not find 2 candidates. Giving up...");
        return null;
    }
    Boundary bdry1 = vecBdry.get(0);
    Boundary bdry2 = vecBdry.get(1);
    DirectBoundaryInterface intrfPair = sim.getInterfaceManager().createDirectInterface(bdry1, bdry2, "In-place");
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  public FeatureCurve createFeatureCurveEmpty(Region region, String nameItAs) {
    printAction("Creating an Empty Feature Curve");
    sayRegion(region);
    FeatureCurve fc = region.getFeatureCurveManager().createEmptyFeatureCurve();
    fc.setPresentationName(nameItAs);
    sayOK();
    return fc;
  }

  public Scene createGeometryScene(){
    printAction("Creating a Geometry Scene");
    sim.getSceneManager().createGeometryScene("__Geometry Scene", "Outline", "Geometry", 1);
    Scene scene = sim.getSceneManager().getScene("__Geometry Scene 1");
    PartDisplayer pd1 = ((PartDisplayer) scene.getCreatorDisplayer());
    pd1.initialize();
    PartDisplayer pd0 = ((PartDisplayer) scene.getDisplayerManager().getDisplayer("Geometry 1"));
    pd0.initialize();
    pd0.setColorMode(colourByPart);
    sayOK();
    return scene;
  }

  public IndirectBoundaryInterface createIndirectInterfacePair(Boundary bdry1, Boundary bdry2){
    printAction("Creating Indirect Interface Pair given two boundaries");
    IndirectBoundaryInterface intrfPair = sim.getInterfaceManager().createIndirectInterface(bdry1, bdry2);
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  public MeshContinuum createMeshContinua_EmbeddedThinMesher(){
    mshCont = _crMsh_Poly();
    enableEmbeddedThinMesher(mshCont);
    sayOK();
    return mshCont;
  }

  public MeshContinuum createMeshContinua_PolyOnly(){
    mshCont = _crMsh_Poly();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
    return mshCont;
  }

  public MeshContinuum createMeshContinua_ThinMesher() {
    mshCont = _crMsh_ThinMesher();
    sayOK();
    return mshCont;
  }

  public MeshContinuum createMeshContinua_Trimmer() {
    mshCont = _crMsh_Trimmer();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
    return mshCont;
  }

  public Scene createMeshScene(){
    printAction("Creating a Mesh Scene");
    sim.getSceneManager().createGeometryScene("__Mesh Scene", "Outline", "Mesh", 3);
    Scene scene = sim.getSceneManager().getScene("__Mesh Scene 1");
    //scene.initializeAndWait();
    PartDisplayer pd1 = ((PartDisplayer) scene.getCreatorDisplayer());
    pd1.initialize();
    PartDisplayer pd0 = ((PartDisplayer) scene.getDisplayerManager().getDisplayer("Mesh 1"));
    pd0.initialize();
    pd0.setColorMode(colourByPart);
    sayOK();
    return scene;
  }

  public Scene createMeshSceneWithCellSet(CellSurfacePart cellSet){
    printAction("Creating a Scene with Cell Set Mesh");
    say("Cell Set: " + cellSet.getPresentationName());
    Scene scene = sim.getSceneManager().createScene();
    scene.setPresentationName("Mesh Cell Set");
    PartDisplayer pd = scene.getDisplayerManager().createPartDisplayer("Cell Set");
    pd.initialize();
    pd.addPart(cellSet);
    pd.setSurface(true);
    pd.setMesh(true);
    pd.setColorMode(partColouring);
    sayOK();
    return scene;
  }

  public Scene createMeshSceneWithObjects(Collection<NamedObject> objects){
    printAction("Creating a Mesh Scene with Objects");
    say("Objects:");
    Scene scene = sim.getSceneManager().createScene();
    scene.setPresentationName("Mesh");
    scene.getDisplayerManager().createPartDisplayerTask("Geometry", -1, 1);
    PartDisplayer pd = scene.getDisplayerManager().createPartDisplayer("Mesh", -1, 1);
    pd.initialize();
    for(Object obj : objects){
        say("  " + obj.toString());
        pd.getParts().addPart((NamedObject) obj);
    }
    pd.setSurface(true);
    pd.setMesh(true);
    pd.setColorMode(4);
    sayOK();
    return scene;
  }

  public void createMeshVolumetricControl(MeshContinuum mshCont, Collection<GeometryPart> colGP,
                                                                    String nickName, double relSize){
    printAction("Creating a Volumetric Control in Mesh Continua");
    sayMeshContinua(mshCont);
    say("Given Parts: ");
    for(GeometryPart gp : colGP){
        say("  " + gp.getPathInHierarchy());
    }
    VolumeSource volSrc = mshCont.getVolumeSources().createVolumeSource();
    volSrc.setPresentationName(nickName);
    volSrc.getPartGroup().setObjects(colGP);
    if(isPoly(mshCont)){
        volSrc.get(MeshConditionManager.class).get(VolumeSourceDualMesherSizeOption.class).setVolumeSourceDualMesherSizeOption(true);
    }
    if(isTrimmer(mshCont)){
        volSrc.get(MeshConditionManager.class).get(TrimmerSizeOption.class).setTrimmerSizeOption(true);
    }
    VolumeSourceSize volSrcSize = volSrc.get(MeshValueManager.class).get(VolumeSourceSize.class);
    ((GenericRelativeSize) volSrcSize.getRelativeSize()).setPercentage(relSize);
    say("Relative Size (%): " + relSize);
    sayOK();
  }

  public ReportMonitor createMonitorAndPlotFromReport(Report rep, String repName,
                                                String xAxisLabel, String yAxisLabel){
    ReportMonitor repMon = rep.createMonitor();
    repMon.setPresentationName(repName);

    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(repName);
    monPl.getMonitors().addObjects(repMon);

    Axes axes_1 = monPl.getAxes();
    Axis axis_2 = axes_1.getXAxis();
    AxisTitle axisTitle_2 = axis_2.getTitle();
    axisTitle_2.setText(xAxisLabel);
    Axis axis_3 = axes_1.getYAxis();
    AxisTitle axisTitle_3 = axis_3.getTitle();
    axisTitle_3.setText(yAxisLabel);
    // Add Monitor to the Global Vector
    vecRepMon.add(repMon);
    return repMon;
  }

  public MonitorPlot createPlotFromVectorRepMon(String plotName){
    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(plotName);
    monPl.getMonitors().addObjects(vecRepMon);

    Axes axes = monPl.getAxes();
    Axis xx = axes.getXAxis();
    xx.getTitle().setText(vecRepMon.firstElement().getXAxisName());
    Axis yy = axes.getYAxis();
    yy.getTitle().setText(vecRepMon.firstElement().getMonitorDescription());

    vecRepMon.clear();      // Clear Vector after flushing
    return monPl;
  }


  public void createWrapperSeedPoint(Region region, double X, double Y, double Z, Units unit){
    setMeshWrapperVolumeSeedPoints(region);
    printAction("Creating a Wrapper Seed Point");
    sayRegion(region);
    say(String.format("Point Coordinate: %f, %f, %f [%s]", X, Y, Z, unit.toString()));
    VolumeOfInterestSeedPoint seedPt = region.get(MeshValueManager.class).get(VolumeOfInterestSeedPointManager.class).createSeedPoint();
    seedPt.getCoordinates().setUnits(unit);
    seedPt.getCoordinates().setComponents(X, Y, Z);
    say("Created: " + seedPt.getPresentationName());
    sayOK();
  }

  public PhysicsContinuum createPhysics_AirSteadySegregatedBoussinesqKEps2Lyr() {
    text = "Air / Steady State / Segregated Solver / Boussinesq / k-eps 2 Layers";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_SegrFlTemp(phC);
    _chPhys_SegrFlBoussinesq(phC, 0.00335);
    _chPhys_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Isothermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Thermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_TurbKEps2Lyr(phC);
    _chPhys_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_AluminumSteadySegregated() {
    text = "Aluminum / Steady State / Segregated Solver";
    PhysicsContinuum phC = _crPhys_3D_SS_SegrSLD(noneString, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_SolidSteadySegregated(String name, double den, double k, double cp) {
    text = "Generic Solid / Steady State / Segregated Solver / Constant Properties";
    PhysicsContinuum phC = _crPhys_3D_SS_SegrSLD(noneString, text);
    SolidModel sldM = phC.getModelManager().getModel(SolidModel.class);
    Solid sld = ((Solid) sldM.getMaterial());
    sld.setPresentationName(name);
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) sld.getMaterialProperties().getMaterialProperty(ConstantDensityProperty.class).getMethod());
    ConstantMaterialPropertyMethod cmpm2 = ((ConstantMaterialPropertyMethod) sld.getMaterialProperties().getMaterialProperty(ThermalConductivityProperty.class).getMethod());
    ConstantSpecificHeat ccp = ((ConstantSpecificHeat) sld.getMaterialProperties().getMaterialProperty(SpecificHeatProperty.class).getMethod());
    say("Name = " + name);
    say("Density = " + den + " " + cmpm.getQuantity().getUnits().toString());
    say("Thermal Conductivity = " + k + " " + cmpm2.getQuantity().getUnits().toString());
    say("Specific Heat = " + cp + " " + ccp.getQuantity().getUnits().toString());
    cmpm.getQuantity().setValue(den);
    cmpm2.getQuantity().setValue(k);
    ccp.getQuantity().setValue(cp);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_SteelSteadySegregated() {
    text = "Steel / Steady State / Segregated Solver";
    PhysicsContinuum phC = _crPhys_3D_SS_SegrSLD("UNSG101000_Solid", text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Isothermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Thermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_TurbKEps2Lyr(phC);
    _chPhys_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  public void createRepMassAvgAndStopCriterion_StdDev(Region region, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    printAction("Creating a Mass Average report and Stopping Criterion");
    say("What: " + var);
    say("Where: " + region.getPresentationName());
    MassAverageReport massAvgRep = sim.getReportManager().createReport(MassAverageReport.class);
    massAvgRep.setScalar(getPrimitiveFieldFunction(var));
    massAvgRep.setUnits(unit);
    massAvgRep.setPresentationName(reportNickname);
    massAvgRep.getParts().setObjects(region);
    String yAxisLabel = "Mass Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(massAvgRep, reportNickname, "Iteration", yAxisLabel);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  public void createRepMaxOnAllRegions(String reportNickname, String var, Units unit){
    printAction("Creating a Maximum report of " + var + " on all Regions");
    MaxReport maxRep = sim.getReportManager().createReport(MaxReport.class);
    maxRep.setScalar(getPrimitiveFieldFunction(var));
    maxRep.setUnits(unit);
    maxRep.setPresentationName(reportNickname);
    maxRep.getParts().setObjects(getAllRegions());
    String yAxisLabel = "Maximum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(maxRep, reportNickname, "Iteration", yAxisLabel);
  }

  public void createRepMFlowAndStopCriterion_StdDev(Boundary bdry, String reportNickname,
                                                                double stdDev, int samples){
    printAction("Creating a Mass Flow report and Stopping Criterion");
    say("Where: " + bdry.getRegion().getPresentationName() + "\\" + bdry.getPresentationName());
    MassFlowReport mfRep = sim.getReportManager().createReport(MassFlowReport.class);
    mfRep.setPresentationName(reportNickname);
    mfRep.getParts().setObjects(bdry);
    String unitStr = mfRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(mfRep, reportNickname, "Iteration", "Mass Flow (" + unitStr + ")");
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  public void createRepMFlowAvgAndStopCriterion_StdDev(Boundary bdry, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    printAction("Creating a Mass Flow Average report and Stopping Criterion");
    say("What: " + var);
    say("Where: " + bdry.getRegion().getPresentationName() + "\\" + bdry.getPresentationName());
    MassFlowAverageReport mfaRep = sim.getReportManager().createReport(MassFlowAverageReport.class);
    mfaRep.setScalar(getPrimitiveFieldFunction(var));
    mfaRep.setUnits(unit);
    mfaRep.setPresentationName(reportNickname);
    mfaRep.getParts().setObjects(bdry);
    String unitStr = unit.getPresentationName();
    String yAxisLabel = "Mass Flow Average of " + var + " (" + unitStr + ")";
    repMon = createMonitorAndPlotFromReport(mfaRep, reportNickname, "Iteration", yAxisLabel);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  public void createRepMinOnAllRegions(String reportNickname, String var, Units unit){
    printAction("Creating a Minimum report of " + var + " on all Regions");
    MinReport minRep = sim.getReportManager().createReport(MinReport.class);
    minRep.setScalar(getPrimitiveFieldFunction(var));
    minRep.setUnits(unit);
    minRep.setPresentationName(reportNickname);
    minRep.getParts().setObjects(getAllRegions());
    String yAxisLabel = "Minimum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(minRep, reportNickname, "Iteration", yAxisLabel);
  }

  public void createRepVolAvgAndStopCriterion_StdDev(Region region, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    printAction("Creating a Volume Average report and Stopping Criterion");
    say("What: " + var);
    say("Where: " + region.getPresentationName());
    VolumeAverageReport volAvgRep = sim.getReportManager().createReport(VolumeAverageReport.class);
    volAvgRep.setScalar(getPrimitiveFieldFunction(var));
    volAvgRep.setUnits(unit);
    volAvgRep.setPresentationName(reportNickname);
    volAvgRep.getParts().setObjects(region);
    String yAxisLabel = "Volume Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(volAvgRep, reportNickname, "Iteration", yAxisLabel);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  public void createRotatingReferenceFrameForRegion(Region region, double[] axis, double[] origin, Units origUnit,
                                    double rotValue, Units rotUnit){
    RotatingMotion rm = sim.get(MotionManager.class).createMotion(RotatingMotion.class, "Rotation");
    rm.getAxisDirection().setComponents(axis[0], axis[1], axis[2]);
    rm.getAxisOrigin().setUnits(origUnit);
    rm.getAxisOrigin().setComponents(origin[0], origin[1], origin[2]);
    rm.getRotationRate().setUnits(rotUnit);
    rm.getRotationRate().setValue(rotValue);
    MotionSpecification ms = region.getValues().get(MotionSpecification.class);
    RotatingReferenceFrame rrf =
      ((RotatingReferenceFrame) sim.get(ReferenceFrameManager.class).getObject("ReferenceFrame for Rotation"));
    ms.setReferenceFrame(rrf);
  }

  public Scene createScalarSceneWithObjects(Collection<NamedObject> objects, String var, Units unit,
                                            String sceneName, boolean smoothFilled){
    printAction("Creating a Scalar Scene with Objects");
    say("Variable: " + var + " in [" + unit.toString() + "]");
    say("Objects:");
    Scene scene = sim.getSceneManager().createScene();
    scene.setPresentationName(sceneName);
    ScalarDisplayer scalDisp = scene.getDisplayerManager().createScalarDisplayer("Scalar");
    scalDisp.initialize();
    for(Object obj : objects){
        say("  " + obj.toString());
        scalDisp.getParts().addPart((NamedObject) obj);
    }
    if(smoothFilled){
        scalDisp.setFillMode(1);
    }
    PrimitiveFieldFunction ff = null;
    try{
        ff = getPrimitiveFieldFunction(var);
    } catch (Exception e){ }
    if(var.matches("^Vel.*")){
        ff = getPrimitiveFieldFunction("Velocity");
        if(var.equalsIgnoreCase("Velocity") || var.equalsIgnoreCase("VelMag") || var.equalsIgnoreCase("Vel Mag")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionMagnitude(ff));
        } else if(var.equalsIgnoreCase("VelX") || var.equalsIgnoreCase("Vel X")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 0));
        } else if(var.equalsIgnoreCase("VelY") || var.equalsIgnoreCase("Vel Y")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 1));
        } else if(var.equalsIgnoreCase("VelZ") || var.equalsIgnoreCase("Vel Z")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 2));
        }
    } else if(var.matches("^WallShear.*")){
        ff = getPrimitiveFieldFunction("WallShearStress");
        if(var.equalsIgnoreCase("WallShearStress") || var.equalsIgnoreCase("WallShearMag") || var.equalsIgnoreCase("WallShear Mag")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionMagnitude(ff));
        }
    } else {
        scalDisp.getScalarDisplayQuantity().setFieldFunction(ff);
    }
    scalDisp.getScalarDisplayQuantity().setUnits(unit);
    sayOK();
    return scene;
  }

  public PlaneSection createSectionPlane(double[] origin, double[] orientation){
    printAction("Creating a Section Plane");
    Vector<Object> where = (Vector) getAllRegions();
    DoubleVector vecOrient = new DoubleVector(orientation);
    DoubleVector vecOrigin = new DoubleVector(origin);
    DoubleVector vecOffsets = new DoubleVector(new double[] {0.0});
    return (PlaneSection) sim.getPartManager().createImplicitPart(where, vecOrient, vecOrigin, 0, 1, vecOffsets);
  }

  public PlaneSection createSectionPlaneX(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {1., 0., 0.});
    say("Normal to X direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  public PlaneSection createSectionPlaneY(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 1., 0.});
    say("Normal to Y direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  public PlaneSection createSectionPlaneZ(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 0., 1.});
    say("Normal to Z direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  public SimpleCylinderPart createShapePartCylinder(double[] coord1, double[] coord2, double r1, double r2, Units unit, String name){
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleCylinderPart scp = mpf.createNewCylinderPart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    scp.setCoordinateSystem(labCSYS);
    scp.getRadius().setUnits(unit);
    scp.getEndRadius().setUnits(unit);
    Coordinate coordinate_0 = scp.getStartCoordinate();
    Coordinate coordinate_1 = scp.getEndCoordinate();
    coordinate_0.setCoordinate(unit, unit, unit, new DoubleVector(coord1));
    coordinate_1.setCoordinate(unit, unit, unit, new DoubleVector(coord2));
    scp.getRadius().setValue(r1);
    scp.getEndRadius().setValue(r2);
    scp.getTessellationDensityOption().setSelected(TessellationDensityOption.MEDIUM);
    scp.setPresentationName(name);
    return scp;
  }

  public void createStoppingCriteriaFromReportMonitor_StdDev(ReportMonitor repMon, double stdDev, int samples){
    say("Creating a Stopping Criterion based on Standard Deviation");
    say("  Standard Deviation: " + stdDev + " " + repMon.getMonitoredValueUnits());
    say("  Number of Samples: " + samples);
    if(stdDev == 0 || samples == 0){
        say("Got a null value. Skipping creation. Just monitoring.");
        return;
    }
    MonitorIterationStoppingCriterion monItStpCrit = repMon.createIterationStoppingCriterion();
    ((MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.STANDARD_DEVIATION);
    monItStpCrit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
    MonitorIterationStoppingCriterionStandardDeviationType stpCritStdDev =
      ((MonitorIterationStoppingCriterionStandardDeviationType) monItStpCrit.getCriterionType());
    stpCritStdDev.getStandardDeviation().setUnits(repMon.getMonitoredValueUnits());
    stpCritStdDev.getStandardDeviation().setValue(stdDev);
    stpCritStdDev.setNumberSamples(samples);
  }

  public void customizeInitialTemperatureConditionForRegion(Region region, double T, Units unit){
    printAction("Customizing a Region Initial Condition");
    sayRegion(region);
    say("Value: " + T + unit.toString());
    region.getConditions().get(InitialConditionOption.class).setSelected(InitialConditionOption.REGION);
    StaticTemperatureProfile temp = region.get(RegionInitialConditionManager.class).get(StaticTemperatureProfile.class);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    sayOK();
  }

  public void customizeThinMesherForRegion(Region region, int numLayers){
    printAction("Customizing a Region Thin Mesher Parameter");
    sayRegion(region);
    say("Thin Mesher Layers: " + numLayers);
    region.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(SolidMesherRegionOption.CUSTOM_VALUES);
    region.get(MeshValueManager.class).get(ThinSolidLayers.class).setLayers(numLayers);
    sayOK();
  }

  public void debugImprinting_Between2ndLevels(){
    printAction("Debugging the Parts. Imprinting between 2nd Level Sub assemblies");
    Vector<CompositePart> vecCP = get2ndLevelCompositeParts();
    for(int i = 0; i < vecCP.size() - 1; i++){
        CompositePart cp_i = vecCP.get(i);
        for(int j = i+1; j < vecCP.size(); j++){
            Vector<MeshPart> vecMP = new Vector<MeshPart>();
            CompositePart cp_j = vecCP.get(j);
            printAction("Imprinting CAD Parts");
            vecMP.addAll(getMeshPartsByName(cp_i.getPathInHierarchy().replace("|", ".") + ".*"));
            vecMP.addAll(getMeshPartsByName(cp_j.getPathInHierarchy().replace("|", ".") + ".*"));
            say("Sub assembly 1: " + cp_i.getPathInHierarchy());
            say("Sub assembly 2: " + cp_j.getPathInHierarchy());
            imprintPartsByCADMethod(vecMP);
            say("\n");
        }
    }
  }

  public void debugImprinting_BetweenCompositeParts_PartByPart(CompositePart compPart1, CompositePart compPart2){
    printAction("Debugging the Parts. Imprinting between 2 Sub assemblies -- Part by Part");
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    vecMP.addAll(getMeshPartsByName(compPart1.getPathInHierarchy().replace("|", ".") + ".*"));
    vecMP.addAll(getMeshPartsByName(compPart2.getPathInHierarchy().replace("|", ".") + ".*"));
    for(int i = 0; i < vecMP.size() - 1; i++){
        for(int j = i+1; j < vecMP.size(); j++){
            printAction("Imprinting CAD Parts");
            say("Mesh Part 1: " + vecMP.get(i).getPathInHierarchy());
            say("Mesh Part 2: " + vecMP.get(j).getPathInHierarchy());
            Vector<MeshPart> vecImpr = new Vector<MeshPart>();
            vecImpr.add(vecMP.get(i));
            vecImpr.add(vecMP.get(j));
            imprintPartsByCADMethod(vecImpr);
            say("\n");
        }
    }
  }

  public void debugImprinting_Every2ndLevel(){
    printAction("Debugging the Parts. Imprinting every 2nd Level Sub assembly");
    for(CompositePart cp : get2ndLevelCompositeParts()){
        printAction("Imprinting CAD Parts");
        say("Sub assembly: " + cp.getPresentationName());
        imprintPartsByCADMethod(getMeshPartsByName(cp.getPathInHierarchy().replace("|", ".") + ".*"));
        say("\n");
    }
  }

  public void disableEmbeddedThinMesher(MeshContinuum mshCont){
    printAction("Disabling Embedded Thin Mesher");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(SolidMesherSubModel.class));
    sayOK();
  }

  public void disableFeatureCurveSizeOnRegion(Region region){
    printAction("Disabling Custom Feature Curve Mesh Size");
    sayRegion(region);
    Collection<FeatureCurve> colFC = region.getFeatureCurveManager().getFeatureCurves();
    for(FeatureCurve fc : colFC) {
        try{
            fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        } catch (Exception e){
            say("  Error disabling " + fc.getPresentationName());
        }
    }
    sayOK();
  }

  public void disableFeatureCurveSizeOnRegions(String regexPatt){
    printAction(String.format("Disabling Feature Curves Mesh Size on all " +
                                "Regions by REGEX pattern: \"%s\"", regexPatt));
    for(Region reg : getAllRegionsByName(regexPatt)){
        disableFeatureCurveSizeOnRegion(reg);
    }
  }

  public void disableMeshContinua(Region region){
    printAction("Disabling Mesh Continua");
    sayRegion(region);
    try{
        sayMeshContinua(region.getMeshContinuum());
        region.getMeshContinuum().erase(region);
    } catch (Exception e){
        say("Already disabled.");
    }
    sayOK();
  }

  public void disablePrismsOnBoundary(Boundary bdry){
    say("Disable Prism Layers on Boundary: " + getStringBoundaryAndRegion(bdry));
    try {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not disable Prism in Boundary.");
    }
  }

  public void disableRadiationS2S(Region region){
    printAction("Disabling Radiation Surface to Surface (S2S)");
    sayRegion(region);
    region.getConditions().get(RadiationTransferOption.class).setOptionEnabled(false);
    sayOK();
  }

  public void disableSurfaceSizeOnBoundary(Boundary bdry){
    say("Disable Surface Mesh Size on Boundary: " + bdry.getPresentationName());
    try {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not disable as Boundary. Trying to do in the Interface Parent");
        try{
            DirectBoundaryInterfaceBoundary intrf = (DirectBoundaryInterfaceBoundary) bdry;
            DirectBoundaryInterface intrfP = intrf.getDirectBoundaryInterface();
            say("Interface: " + intrfP.getPresentationName());
            intrfP.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
            sayOK();
        } catch (Exception e2) {
            say("ERROR! Please review settings. Skipping this Boundary.");
            say(e2.getMessage() + "\n");
        }
    }
  }

  public void disableSurfaceProximityRefinement(MeshContinuum mshCont){
    printAction("Disabling Surface Proximity Refinement");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    sayOK();
  }

  public void disableSurfaceRemesher(MeshContinuum mshCont){
    printAction("Disabling Surface Remesher");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(ResurfacerMeshingModel.class));
    sayOK();
  }

  public void disableSurfaceRemesherAutomaticRepair(MeshContinuum mshCont){
    printAction("Disabling Remesher Automatic Surface Repair");
    if(!isRemesh(mshCont)){
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    sayOK();
  }

  public void disableSurfaceRemesherProjectToCAD(MeshContinuum mshCont){
    printAction("Disabling Project to CAD");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(false);
    mshCont.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(false);
    sayOK();
  }

  public void disableSurfaceWrapper(MeshContinuum mshCont){
    printAction("Disabling Surface Wrapper");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(SurfaceWrapperMeshingModel.class));
    sayOK();
  }

  public void disableWrapperGapClosure(MeshContinuum mshCont){
    printAction("Disabling Wrapper Gap Closure");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(false);
    sayOK();
  }

  public void disableThinMesherOnRegion(Region region){
    printAction("Disabling Thin Mesher on Region");
    int opt = SolidMesherRegionOption.DISABLE;
    sayRegion(region);
    try{
        region.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(opt);
        sayOK();
    } catch (Exception e){
        say("ERROR! Moving on.\n");
    }
  }

  public void enableEmbeddedThinMesher(MeshContinuum mshCont){
    printAction("Enabling Embedded Thin Mesher");
    sayMeshContinua(mshCont);
    say("Embedded Thin Mesher overview:");
    mshCont.enable(SolidMesherSubModel.class);
    //mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    disableSurfaceProximityRefinement(mshCont);
    SolidMesherSubModel sldSubMdl = mshCont.getModelManager().getModel(SolidMesherSubModel.class);
    sldSubMdl.setOptimize(true);
    say("  Optimizer ON");
    if(thinMeshIsPolyType){
        sldSubMdl.getPrismType().setSelected(PrismTypeValue.POLYGONAL);
        say("  Prisms Type: POLYGONAL");
    } else {
        sldSubMdl.getPrismType().setSelected(PrismTypeValue.TRIANGULAR);
        say("  Prisms Type: TRIANGULAR");
    }
    mshCont.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);
    say("  Thin Layers: " + thinMeshLayers);
    sayOK();
  }

  public void enableMeshContinua(MeshContinuum mshCont, Region region){
    printAction("Enabling Mesh Continua");
    sayRegion(region);
    sayMeshContinua(mshCont);
    mshCont.add(region);
    sayOK();
  }

  public void enablePhysicsContinua(PhysicsContinuum physCont, Region region){
    printAction("Enabling Physics Continua");
    sayRegion(region);
    sayPhysicsContinua(physCont);
    physCont.add(region);
    sayOK();
  }

  public void enablePrismLayers(MeshContinuum mshCont){
    /*
     * This method will assume Prism Layers only on Fluid Regions
     */
    printAction("Enabling Prism Layers");
    sayMeshContinua(mshCont);
    printPrismsParameters();
    Collection<Interface> intrfcs = sim.getInterfaceManager().getObjects();

    mshCont.enable(PrismMesherModel.class);
    PrismMesherModel pmm = mshCont.getModelManager().getModel(PrismMesherModel.class);
    NumPrismLayers npl = mshCont.getReferenceValues().get(NumPrismLayers.class);
    PrismLayerStretching pls = mshCont.getReferenceValues().get(PrismLayerStretching.class);
    PrismThickness pt = mshCont.getReferenceValues().get(PrismThickness.class);
    GenericRelativeSize pgrs = ((GenericRelativeSize) pt.getRelativeSize());
    npl.setNumLayers(prismsLayers);
    pls.setStretching(prismsStretching);
    pgrs.setPercentage(prismsRelSizeHeight);
    pmm.setMinimumThickness(prismsMinThickn);
    pmm.setLayerChoppingPercentage(prismsLyrChoppPerc);
    pmm.setNearCoreLayerAspectRatio(prismsNearCoreAspRat);

    say("Disabling Prisms on Solid Regions");
    for(Region region : getAllRegions()){
        MeshContinuum mshC = region.getMeshContinuum();
        //if(region.getMeshContinuum() == null){
        if(!region.isMeshing() || mshCont != mshC){
            say("  Skipping: " + region.getPresentationName());
            continue;
        }
        if(isFluid(region)){
            say("  Region ON: " + region.getPresentationName());
            region.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DEFAULT);
        } else if(isSolid(region)) {
            region.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
            say("  Region OFF: " + region.getPresentationName());
        }
    }
    say("Enabling Prisms on Fluid-Solid interfaces with same Mesh Continua");
    int n = 0;
    int k = 0;
    InterfacePrismsOption ipo = null;
    for(Interface intrf : intrfcs){
        String name = intrf.getPresentationName();
        if(isFluid(intrf.getRegion0()) && isFluid(intrf.getRegion1())){
            say("  Prism OFF: " + name + " (Fluid-Fluid Interface)");
            k++;
            continue;
        }
        if(isFluid(intrf.getRegion0()) || isFluid(intrf.getRegion1())){
            try{
                ipo = intrf.get(MeshConditionManager.class).get(InterfacePrismsOption.class);
                ipo.setPrismsEnabled(true);
                say("  Prism ON: " + name);
                n++;
            } catch (Exception e){ continue; }
        }
    }
    say("Fluid-Solid interfaces with Prisms enabled: " + n);
    say("Fluid-Fluid interfaces skipped: " + k);
    sayOK();
  }

  public void enableRadiationS2S(PhysicsContinuum physCont){
    printAction("Enabling Radiation Surface to Surface (S2S)");
    sayPhysicsContinua(physCont);
    physCont.enable(RadiationModel.class);
    physCont.enable(S2sModel.class);
    physCont.enable(ViewfactorsCalculatorModel.class);
    physCont.enable(GrayThermalRadiationModel.class);
    GrayThermalRadiationModel gtrm = physCont.getModelManager().getModel(GrayThermalRadiationModel.class);
    RadiationTemperature radT = gtrm.getThermalEnvironmentManager().get(RadiationTemperature.class);
    radT.getEnvRadTemp().setUnits(defUnitTemp);
    radT.getEnvRadTemp().setValue(Tref);
  }

  public void enableSurfaceProximityRefinement(MeshContinuum mshCont){
    printAction("Enabling Surface Proximity Refinement");
    sayMeshContinua(mshCont);
    say("Proximity settings overview: ");
    say("  Proximity Number of Points in Gap: " + mshProximityPointsInGap);
    say("  Proximity Search Floor (mm): " + mshProximitySearchFloor);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(true);
    SurfaceProximity sp = mshCont.getReferenceValues().get(SurfaceProximity.class);
    sp.setNumPointsInGap(mshProximityPointsInGap);
    sp.getFloor().setUnits(defUnitLength);
    sp.getFloor().setValue(mshProximitySearchFloor);
    sayOK();
  }

  public void enableSurfaceRemesher(MeshContinuum mshCont){
    printAction("Enabling Surface Remesher");
    sayMeshContinua(mshCont);
    mshCont.enable(ResurfacerMeshingModel.class);
    sayOK();
  }

  public void enableSurfaceRemesherAutomaticRepair(MeshContinuum mshCont){
    printAction("Enabling Remesher Automatic Surface Repair");
    if(!isRemesh(mshCont)){
        say("Surface Remesher not enabled. Skipping");
        return;
    }
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(true);
    sayOK();
  }

  public void enableSurfaceRemesherProjectToCAD(MeshContinuum mshCont){
    printAction("Enabling Project to CAD");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(true);
    mshCont.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(true);
    sayOK();
  }

  public void enableSurfaceWrapper(MeshContinuum mshCont){
    printAction("Enabling Surface Wrapper");
    sayMeshContinua(mshCont);
    say("Surface Wrapper settings overview: ");
    say("  Geometric Feature Angle (deg): " + mshWrapperFeatureAngle);
    say("  Wrapper Scale Factor (%): " + mshWrapperScaleFactor);
    mshCont.enable(SurfaceWrapperMeshingModel.class);
    mshCont.getReferenceValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(mshWrapperFeatureAngle);
    setMeshWrapperScaleFactor(mshCont, mshWrapperScaleFactor);
    sayOK();
  }

  public void enableWrapperGapClosure(MeshContinuum mshCont, double gapSize){
    printAction("Enabling Wrapper Gap Closure");
    sayMeshContinua(mshCont);
    say("  Gap Closure Size (%): " + gapSize);
    mshCont.getModelManager().getModel(SurfaceWrapperMeshingModel.class).setDoGapClosure(true);
    mshCont.getReferenceValues().get(GapClosureSize.class).getRelativeSize().setPercentage(gapSize);
    sayOK();
  }

  public void exportMeshedRegionsToDBS(String subDirName) {
    printAction("Exporting Meshed Regions to DBS files");
    say("Querying Surface Mesh Representation");
    if(hasValidSurfaceMesh()){
        say("Remeshed Regions found. Using this one.");
        exportRemeshedRegionsToDBS(subDirName);
    } else if(hasValidWrappedMesh()){
        say("Wrapped Regions found. Using this one.");
        exportWrappedRegionsToDBS(subDirName);
    } else {
        say("No Valid Mesh representation found. Skipping.");
    }
    sayOK();
  }

  public void exportRemeshedRegionsToDBS(String subDirName) {
    printAction("Exporting All Remeshed Regions to DBS files");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("To Path: " + dbsSubPath.toString());
    if (!dbsPath.exists()) { dbsPath.mkdirs(); }
    if (!dbsSubPath.exists()) { dbsSubPath.mkdirs(); }
    for(SurfaceRepRegion srfPart : getAllRemeshedRegions()) {
        String name = srfPart.getPresentationName();
        name = name.replace(" ", "_");
        name = name.replace(".", "+");
        File fPath = new File(dbsSubPath, name + ".dbs");
        sim.println("Writing: " + fPath);
        srfPart.exportDbsRegion(fPath.toString(), 1, "");
    }
  }

  public void exportWrappedRegionsToDBS(String subDirName) {
    printAction("Exporting All Wrapped Regions to DBS files");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("To Path: " + dbsSubPath.toString());
    if (!dbsPath.exists()) { dbsPath.mkdirs(); }
    if (!dbsSubPath.exists()) { dbsSubPath.mkdirs(); }
    for(SurfaceRepRegion srfPart : getAllWrappedRegions()) {
        String name = srfPart.getPresentationName();
        name = name.replace(" ", "_");
        name = name.replace(".", "+");
        File fPath = new File(dbsSubPath, name + ".dbs");
        sim.println("Writing: " + fPath);
        srfPart.exportDbsRegion(fPath.toString(), 1, "");
    }
  }

  public void findAllPartsContacts(double tol_meters){
    printAction("Finding All Part Contacts");
    say("Tolerance (m): " + tol_meters);
    queryGeometryRepresentation().findPartPartContacts(getAllLeafPartsAsMeshParts(), tol_meters);
    sayOK();
  }

  public void freezeKeTurbSolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(KeTurbSolver.class);
    ((KeTurbSolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  public void freezeKeTurbViscositySolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(KeTurbViscositySolver.class);
    ((KeTurbViscositySolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  private void freezePrintMessage(boolean option, String solver){
    String msg = "Freezing ";
    if(!option){ msg = msg.replace("Free", "Unfree"); }
    printAction(msg + solver);
    sayOK();
  }

  public void freezeS2SSolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(S2sSolver.class);
    ((S2sSolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  public void freezeSegregatedFlowSolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(SegregatedFlowSolver.class);
    ((SegregatedFlowSolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  public void freezeSegregatedEnergySolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(SegregatedEnergySolver.class);
    ((SegregatedEnergySolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  public void freezeViewfactorsCalculatorSolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(ViewfactorsCalculatorSolver.class);
    ((ViewfactorsCalculatorSolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
    if(!option){
        ((ViewfactorsCalculatorSolver) solver).calculateViewfactors();
    }
  }

  public void freezeWallDistanceSolver(boolean option){
    Solver solver = sim.getSolverManager().getSolver(WallDistanceSolver.class);
    ((WallDistanceSolver) solver).setFrozen(option);
    freezePrintMessage(option, solver.getPresentationName());
  }

  public void genSurfaceMesh() {
    if(skipMeshGeneration){ return; }
    printAction("Generating Surface Mesh");
    sim.get(MeshPipelineController.class).generateSurfaceMesh();
    if(createMeshSceneUponSurfaceMeshGeneration){
        createMeshScene().setPresentationName("Surface Mesh");
    }
    if(checkMeshQualityUponSurfaceMeshGeneration){
        // Checking Mesh Quality right after generated
        checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions();
    }
  }

  public void genVolumeMesh() {
    if(skipMeshGeneration){ return; }
    printAction("Generating Volume Mesh");
    sim.get(MeshPipelineController.class).generateVolumeMesh();
  }

  public Vector<CompositePart> get2ndLevelCompositeParts(){
    Vector<CompositePart> vecCP2ndLevel = new Vector<CompositePart>();
    setVerboseOff();
    for(CompositePart cp : getAllCompositeParts()){
        if(getCompositePartLevel(cp) == 2){
            vecCP2ndLevel.add(cp);
        }
    }
    setVerboseOn();
    return vecCP2ndLevel;
  }

  public Collection<Boundary> getAllBoundaries() {
    sayA("Getting all Boundaries available...");
    Vector<Boundary> allBdrys = new Vector<Boundary>();
    for(Region region : getAllRegions()) {
        Collection<Boundary> bdrys = region.getBoundaryManager().getBoundaries();
        for(Boundary bdry : bdrys) {
            allBdrys.add(bdry);
        }
    }
    sayA("Boundaries found: " + allBdrys.size());
    return ((Collection<Boundary>) allBdrys);
  }

  public Collection<Boundary> getAllBoundariesByName(String regexPatt) {
    sayA(String.format("Getting all Boundaries from all Regions by REGEX pattern: \"%s\"", regexPatt));
    Vector<Boundary> chosenBdrys = new Vector<Boundary>();
    for(Boundary bdry : getAllBoundaries()){
        if(bdry.getPresentationName().matches(regexPatt)){
            chosenBdrys.add(bdry);
        }
    }
    sayA("Boundaries found by REGEX: " + chosenBdrys.size());
    say("");
    return (Collection<Boundary>) chosenBdrys;
  }

  public Collection<Boundary> getAllBoundariesFromRegion(Region region) {
    sayA("Getting all Boundaries from Region: " + region.getPresentationName());
    Collection<Boundary> colBdry = region.getBoundaryManager().getBoundaries();
    sayA("Boundaries found: " + colBdry.size());
    return colBdry;
  }

  public Collection<Boundary> getAllBoundariesFromRegionSkipInterfaces(Region region) {
    say("Getting All Boundaries but Skip Interfaces");
    sayRegion(region);
    Vector<Boundary> allBdrysWithoutInterfaces = new Vector<Boundary>();
    for(Boundary bdry : getAllBoundariesFromRegion(region)){
        if(isInterface(bdry)){ continue; }  // Skip interfaces
        allBdrysWithoutInterfaces.add(bdry);
    }
    sayA("True boundaries Collected by REGEX: " + allBdrysWithoutInterfaces.size());
    say("");
    return (Collection<Boundary>) allBdrysWithoutInterfaces;
  }

  public Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt) {
    sayA(String.format("Getting all Boundaries from Regions by REGEX pattern: \"%s\"", regexPatt));
    Vector<Boundary> allBdrysFromRegions = new Vector<Boundary>();
    for(Region reg : getAllRegionsByName(regexPatt)){
        allBdrysFromRegions.addAll(getAllBoundariesFromRegion(reg));
    }
    sayA("Boundaries found by REGEX: " + allBdrysFromRegions.size());
    say("");
    return (Collection<Boundary>) allBdrysFromRegions;
  }

  public Collection<Boundary> getAllBoundariesFromRegionsByNameSkipInterfaces(String regexPatt) {
    say("Getting All Boundaries by REGEX but Skip Interfaces");
    Vector<Boundary> allBdrysWithoutInterfaces = new Vector<Boundary>();
    for(Boundary bdry : getAllBoundariesFromRegionsByName(regexPatt)){
        if(isInterface(bdry)){ continue; }  // Skip interfaces
        allBdrysWithoutInterfaces.add(bdry);
    }
    sayA("True boundaries Collected by REGEX: " + allBdrysWithoutInterfaces.size());
    say("");
    return (Collection<Boundary>) allBdrysWithoutInterfaces;
  }

  public Collection<CompositePart> getAllCompositeParts() {
    sayA("Getting all Composite Parts...");
    Vector<CompositePart> compPrtCol = new Vector<CompositePart>();
    for(GeometryPart gp : getAllGeometryParts()){
        String partName = gp.getPresentationName();
        //say("Part Name: " + partName);
        if(!isCompositePart(gp)) { continue; }
        Vector<CompositePart> vecCompPart = new Vector<CompositePart>();
        for (CompositePart cp : getCompositeChildren((CompositePart) gp, vecCompPart)){
            sayV("  Composite Part Found: " + cp.getPresentationName());
        }
        compPrtCol.addAll(vecCompPart);
    }
    sayA("Composite Parts found: " + compPrtCol.size());
    return compPrtCol;
  }

  public Collection<FeatureCurve> getAllFeatureCurves(){
    sayA("Getting all Feature Curves...");
    Vector<FeatureCurve> vecFC = new Vector<FeatureCurve>();
    for(Region region : getAllRegions()){
        vecFC.addAll(getFeatureCurvesFromRegion(region));
    }
    sayA("All Feature Curves: " + vecFC.size());
    return vecFC;
  }

  public Collection<GeometryPart> getAllGeometryParts(){
    sayA("Getting all Geometry Parts...");
    Collection<GeometryPart> colGP = sim.get(SimulationPartManager.class).getParts();
    sayA("Geometry Parts found: " + colGP.size());
    return colGP;
  }

  public Collection<GeometryPart> getAllGeometryPartsByName(String regexPatt){
    sayA(String.format("Getting all Geometry Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<GeometryPart> gpVec = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllGeometryParts()){
        if(gp.getPresentationName().matches(regexPatt)){
            sayV("Found: " + gp.getPresentationName());
            gpVec.add(gp);
        }
    }
    sayA("Geometry Parts found by REGEX: " + gpVec.size());
    return gpVec;
  }

  public Collection<Interface> getAllInterfaces(){
    sayA("Getting all Interfaces...");
    Collection<Interface> colIntrfcs = sim.get(InterfaceManager.class).getObjects();
    sayA("Interfaces found: " + colIntrfcs.size());
    return colIntrfcs;
  }

  public Collection<Object> getAllInterfacesAsObjects(){
    sayA("Getting all Interfaces...");
    Collection<Object> colIntrfcs = sim.getInterfaceManager().getChildren();
    sayA("Interfaces found: " + colIntrfcs.size());
    return colIntrfcs;
  }

  public Collection<LeafMeshPart> getAllLeafMeshParts(){
    sayA("Getting all Leaf Mesh Parts...");
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    for(GeometryPart gp : colLP){
        try{
            lmpVec.add((LeafMeshPart) gp);
        } catch(Exception e){ continue; }
    }
    sayA("Leaf Mesh Parts found: " + lmpVec.size());
    return lmpVec;
  }

  public Collection<LeafMeshPart> getAllLeafMeshPartsByName(String regexPatt){
    sayA(String.format("Getting all Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
    for(LeafMeshPart lmp : getAllLeafMeshParts()){
        if(lmp.getPresentationName().matches(regexPatt)){
            sayV("Found: " + lmp.getPresentationName());
            lmpVec.add(lmp);
        }
    }
    sayA("Leaf Mesh Parts found by REGEX: " + lmpVec.size());
    return lmpVec;
  }

  public Collection<GeometryPart> getAllLeafParts(){
    sayA("Getting all Leaf Parts: ");
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    sayA("Leaf Parts found: " + colLP.size());
    return colLP;
  }

  public Collection<GeometryPart> getAllLeafPartsByName(String regexPatt){
    sayA(String.format("Getting all Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllLeafParts()){
        if(gp.getPathInHierarchy().matches(regexPatt)){
            sayV("Found: " + gp.getPathInHierarchy());
            vecGP.add(gp);
        }
    }
    sayA("Leaf Parts found by REGEX: " + vecGP.size());
    say("");
    return vecGP;
  }

  public Vector<CadPart> getAllLeafPartsAsCadParts(){
    Vector<CadPart> vecCP = new Vector<CadPart>();
    for(GeometryPart gp : getAllLeafParts()){
        vecCP.add((CadPart) gp);
    }
    return vecCP;
  }

  public Vector<MeshPart> getAllLeafPartsAsMeshParts(){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    for(GeometryPart gp : getAllLeafParts()){
        vecMP.add((MeshPart) gp);
    }
    return vecMP;
  }

  public Collection<PartSurface> getAllPartSurfacesByName(String regexPatt){
    sayA(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt));
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    for(PartSurface ps : getAllPartSurfacesFromLeafParts()){
        if(ps.getPresentationName().matches(regexPatt)){
            sayV("Found: " + ps.getPresentationName());
            psVec.add(ps);
        }
    }
    sayA("Part Surfaces found by REGEX: " + psVec.size());
    return psVec;
  }

  public Collection<PartSurface> getAllPartSurfacesFromLeafMeshPart(LeafMeshPart lmp){
    return lmp.getPartSurfaces();
  }

  public Collection<PartSurface> getAllPartSurfacesFromLeafMeshPartByName(LeafMeshPart lmp, String regexPatt){
    sayA(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt));
    sayA("Leaf Mesh Part: " + lmp.getPresentationName());
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    for(PartSurface ps : getAllPartSurfacesFromLeafMeshPart(lmp)){
        if(ps.getPresentationName().matches(regexPatt)){
            sayV("Found: " + ps.getPresentationName());
            psVec.add(ps);
        }
    }
    sayA("Part Surfaces found by REGEX: " + psVec.size());
    return psVec;
  }

  public Collection<PartSurface> getAllPartSurfacesFromLeafParts(){
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    for(GeometryPart gp : getAllLeafParts()){
        vecPS.addAll(gp.getPartSurfaces());
    }
    return vecPS;
  }

  public Collection<MeshContinuum> getAllMeshContinuas(){
    sayA("Getting all Mesh Continuas...");
    Vector<MeshContinuum> vecMC = new Vector<MeshContinuum>();
    for(Continuum cont : sim.getContinuumManager().getObjects()){
        if(cont.getBeanDisplayName().equals("MeshContinum")){
            vecMC.add((MeshContinuum) cont);
        }
    }
    sayA("All Mesh Continuas: " + vecMC.size());
    return vecMC;
  }

  public Collection<PhysicsContinuum> getAllPhysicsContinuas(){
    sayA("Getting all Physics Continuas...");
    Vector<PhysicsContinuum> vecPC = new Vector<PhysicsContinuum>();
    for(Continuum cont : sim.getContinuumManager().getObjects()){
        if(cont.getBeanDisplayName().equals("PhysicsContinum")){
            vecPC.add((PhysicsContinuum) cont);
        }
    }
    sayA("All Physics Continuas: " + vecPC.size());
    return vecPC;
  }

  public Collection<Region> getAllRegions() {
    sayA("Getting all Regions...");
    Collection<Region> colReg = sim.getRegionManager().getRegions();
    sayA("All Regions: " + colReg.size());
    return colReg;
  }

  public Collection<Region> getAllRegionsByName(String regexPatt) {
    sayA(String.format("Getting all Regions by REGEX pattern: \"%s\"", regexPatt));
    Vector<Region> vecReg = new Vector<Region>();
    for(Region reg : getAllRegions()){
        if(reg.getPresentationName().matches(regexPatt)){
            sayV("Found: " + reg.getPresentationName());
            vecReg.add(reg);
        }
    }
    sayA("Regions found by REGEX: " + vecReg.size());
    say("");
    return vecReg;
  }

  public Collection<SurfaceRepRegion> getAllRemeshedRegions(){
    sayA("Getting all Remeshed Regions...");
    SurfaceRep srfRep = queryRemeshedSurface();
    Collection<SurfaceRepRegion> colRemshReg = srfRep.getSurfaceRepRegionManager().getObjects();
    sayA("All Remeshed Regions: " + colRemshReg.size());
    return colRemshReg;
  }

  public Collection<SurfaceRepRegion> getAllWrappedRegions(){
    sayA("Getting all Wrapped Regions...");
    SurfaceRep srfRep = queryWrappedSurface();
    Collection<SurfaceRepRegion> colWrappedReg = srfRep.getSurfaceRepRegionManager().getObjects();
    sayA("All Wrapped Regions: " + colWrappedReg.size());
    return colWrappedReg;
  }

  public Boundary getBoundaryByName(String regexPatt){
    /*
     * Loop in all Boundaries and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    Boundary foundBdry = null;
    for(Boundary bdry : getAllBoundaries()){
        if(bdry.getPresentationName().matches(regexPatt)){
            foundBdry = bdry;
            break;
        }
    }
    setVerboseOn();
    if(foundBdry != null) {
        sayA("Got by REGEX: " + foundBdry.getPresentationName());
        say("");
        return foundBdry;
    } else {
        sayA("Got NULL.\n");
        return null;
    }
  }

  public Boundary getBoundaryInRegion(Region region, String name){
      return region.getBoundaryManager().getBoundary(name);
  }

  public Boundary getBoundaryInRegionByName(Region region, String regexPatt){
    /*
     * Loop in all Boundaries in the provided Region and returns the first match, given the name pattern
     */
    sayA("Region: " + region.getPresentationName());
    sayA(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    Boundary foundBdry = null;
    for(Boundary bdry : getAllBoundariesFromRegion(region)){
        if(bdry.getPresentationName().matches(regexPatt)){
            foundBdry = bdry;
            break;
        }
    }
    setVerboseOn();
    if(foundBdry != null) {
        sayA("Got by REGEX: " + foundBdry.getPresentationName());
        say("");
        return foundBdry;
    } else {
        sayA("Got NULL.\n");
        return null;
    }
  }

  public CadPart getCadPart(String name){
    sayA("Getting CadPart by name match: " + name);
    for(GeometryPart gp : getAllLeafParts()){
        if (gp.getPresentationName().equals(name)) {
            sayA("Got " + name);
            return (CadPart) gp;
        }
    }
    sayA("Got NULL!");
    return null;
  }

  public Vector<CompositePart> getCompositeChildren(CompositePart compPrt, Vector<CompositePart> vecCP){
    for(GeometryPart gp : compPrt.getChildParts().getParts()){
        //String gpName = gp.getPresentationName();
        if(!isCompositePart(gp)) { continue; }
        //say("Child Part: " + childPrt.getPresentationName());
        vecCP.add((CompositePart) gp);
        getCompositeChildren((CompositePart) gp, vecCP);
    }
    return vecCP;
  }

  public int getCompositeParent(CompositePart compPart, int level){
    try{
        CompositePart parent = (CompositePart) compPart.getParentPart();
        sayV("  Level " + level + ". Parent: " + parent.getPresentationName());
        level++;
        level = getCompositeParent(parent, level);
    } catch (Exception e) { }
    return level;
  }

  public CompositePart getCompositeParentPart(String name){
    return (CompositePart) sim.get(SimulationPartManager.class).getPart(name);
  }

  public CompositePart getCompositePartByName(String regexPatt){
    /*
     * Loop in all Composite Parts and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Composite Part by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    CompositePart foundCP = null;
    for(CompositePart cp : getAllCompositeParts()){
        //say(cp.getPresentationName());
        if(cp.getPresentationName().matches(regexPatt)){
            foundCP = cp;
            break;
        }
    }
    setVerboseOn();
    if(foundCP != null) {
        sayA("Got by REGEX: " + foundCP.getPresentationName());
        say("");
        return foundCP;
    } else {
        sayA("Got NULL.\n");
        return null;
    }
  }

  public int getCompositePartLevel(CompositePart compPart){
    sayV("Composite Part: " + compPart.getPathInHierarchy());
    int level = getCompositeParent(compPart, 1);
    sayV(compPart.getPresentationName() + " is level " + level + ".\n");
    return level;
  }

  public void getDependentInterfaceAndEraseIt(Boundary bdry){
    Interface intrf = (DirectBoundaryInterface) bdry.getDependentInterfaces().firstElement();
    printAction("Erasing an Interface");
    say("Name: " + intrf.getPresentationName());
    say("Between Regions: ");
    say("  " + intrf.getRegion0().getPresentationName());
    say("  " + intrf.getRegion1().getPresentationName());
    sim.getInterfaceManager().deleteInterfaces(intrf);
    sayOK();
  }

  public DirectBoundaryInterface getDirectBoundaryInterfaceBetween2Regions(Region r1, Region r2){
    DirectBoundaryInterface intrfP = null;
    for(Interface intrf : sim.getInterfaceManager().getObjects()){
        try{
            intrfP = (DirectBoundaryInterface) intrf;
        } catch (Exception e) { continue; }
        if(intrfP.getRegion0() == r1 && intrfP.getRegion1() == r2 ||
           intrfP.getRegion0() == r2 && intrfP.getRegion1() == r1){
            return intrfP;
        }
    }
    return intrfP;
  }

  public DirectBoundaryInterface getDirectBoundaryInterfaceByName(String intrfName){
    return ((DirectBoundaryInterface) sim.getInterfaceManager().getInterface(intrfName));
  }

  public FeatureCurve getFeatureCurve(Region region, String name){
    return region.getFeatureCurveManager().getFeatureCurve(name);
  }

  public Collection<FeatureCurve> getFeatureCurvesFromRegion(Region region){
    return region.getFeatureCurveManager().getFeatureCurves();
  }

  public VectorComponentFieldFunction getFieldFuncionComponent(PrimitiveFieldFunction ff, int i){
    return (VectorComponentFieldFunction) ff.getComponentFunction(i);
  }

  public VectorMagnitudeFieldFunction getFieldFuncionMagnitude(PrimitiveFieldFunction ff){
    return (VectorMagnitudeFieldFunction) ff.getMagnitudeFunction();
  }

  public Collection<String> getInterfacesBetweenLeafMeshParts(LeafMeshPart lmp1, LeafMeshPart lmp2,
                                                        String renameTo, boolean combinePartFaces){
    Vector<String> vecIntrfc = new Vector<String>();
    printLine();
    sayA("Getting Interfaces (or common names) between Two Leaf Mesh Parts:");
    printLine();
    sayA("  Part 1: " + lmp1.getPresentationName());
    sayA("  Part 2: " + lmp2.getPresentationName());
    Vector<PartSurface> vecPS1 = new Vector<PartSurface>();
    Vector<PartSurface> vecPS2 = new Vector<PartSurface>();
    for(PartSurface ps1 : getAllPartSurfacesFromLeafMeshPart(lmp1)){
        for(PartSurface ps2 : getAllPartSurfacesFromLeafMeshPart(lmp2)){
            if(ps1.getPresentationName().equals(ps2.getPresentationName())){
                if(ps1.getPresentationName().equals("Faces")){
                    // Attention to this detail. Skip Surfaces with no Interfaces
                    continue;
                }
                ps1.setPresentationName(renameTo);
                ps2.setPresentationName(renameTo);
                vecIntrfc.add(ps1.getPresentationName());
                vecPS1.add(ps1);
                vecPS2.add(ps2);
            }
        }
    }
    sayA("");
    if(combinePartFaces && vecPS1.size() > 0){
        sayA("Combining Faces on Both Parts");
        sayA("  Working on Part: " + lmp1.getPresentationName());
        lmp1.combinePartSurfaces(vecPS1);
        sayA("  Working on Part: " + lmp2.getPresentationName());
        lmp2.combinePartSurfaces(vecPS2);
    }
    sayA("\n");
    return vecIntrfc;
  }

  public LeafMeshPart getLeafMeshPartByName(String regexPatt){
    /*
     * Loop in all Leaf Mesh Parts and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Leaf Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    LeafMeshPart foundLMP = null;
    for(LeafMeshPart lmp : getAllLeafMeshParts()){
        if(lmp.getPresentationName().matches(regexPatt)){
            foundLMP = lmp;
            break;
        }
    }
    setVerboseOn();
    if(foundLMP != null) {
        sayA("Got by REGEX: " + foundLMP.getPresentationName());
        return foundLMP;
    } else {
        sayA("Got NULL.");
        return null;
    }
  }

  public GeometryPart getLeafPartByName(String regexPatt){
    /*
     * Loop in all Leaf Parts and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Leaf Part by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    GeometryPart foundLP = null;
    for(GeometryPart gp : getAllLeafParts()){
        if(gp.getPathInHierarchy().matches(regexPatt)){
            foundLP = gp;
            break;
        }
    }
    setVerboseOn();
    if(foundLP != null) {
        sayA("Got by REGEX: " + foundLP.getPathInHierarchy());
        return foundLP;
    } else {
        sayA("Got NULL.");
        return null;
    }
  }

  public MeshContinuum getMeshContinua(String name) {
      return ((MeshContinuum) sim.getContinuumManager().getContinuum(name));
  }

  public MeshPart getMeshPartByName(String regexPatt){
    /*
     * Loop in all Mesh Parts and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    boolean verb = verbose;
    setVerboseOff();
    MeshPart foundMP = null;
    for(MeshPart mp : getAllLeafPartsAsMeshParts()){
        if(mp.getPathInHierarchy().matches(regexPatt)){
            foundMP = mp;
            break;
        }
    }
    verbose = verb;
    if(foundMP != null) {
        sayA("Got by REGEX: " + foundMP.getPathInHierarchy());
        return foundMP;
    } else {
        sayA("Got NULL.");
        return null;
    }
  }

  public Vector<MeshPart> getMeshPartsByName(String regexPatt){
    /*
     * Loop in all Mesh Parts and returns the matches, given the name pattern
     */
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    sayA(String.format("Getting Mesh Parts by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    for(MeshPart mp : getAllLeafPartsAsMeshParts()){
        //say(mp.getPathInHierarchy());
        if(mp.getPathInHierarchy().matches(regexPatt)){
            vecMP.add(mp);
        }
    }
    setVerboseOn();
    sayA("Mesh Parts found: " + vecMP.size());
    return vecMP;
  }

  public String getMinTargetString(double min, double tgt){
    String minS = String.format("%.2f%%", min);
    String tgtS = String.format("%.2f%%", tgt);
    return "Min/Target = " + minS + "/" + tgtS;
  }

  public PartCurve getPartCurveFromGeometryPart(GeometryPart gp, String name){
    for(PartCurve pc : gp.getPartCurves()){
        if(pc.getPresentationName().equals(name)){
            return pc;
        }
    }
    return null;
  }

  @Deprecated
  public PartSurface getPartSurfaceFromGeometryPart(GeometryPart gp, String name){
    for(PartSurface ps : gp.getPartSurfaces()){
        if(ps.getPresentationName().equals(name)){
            return ps;
        }
    }
    return null;
  }

  public PartSurface getPartSurface(CadPart cp, String name){
    return cp.getPartSurfaceManager().getPartSurface(name);
  }

  public PartSurface getPartSurface(LeafMeshPart lmp, String name){
    return lmp.getPartSurfaceManager().getPartSurface(name);
  }

  public PrimitiveFieldFunction getPrimitiveFieldFunction(String var){
      return ((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction(var));
  }

  public PhysicsContinuum getPhysicsContinua(String name){
      return ((PhysicsContinuum) sim.getContinuumManager().getContinuum(name));
  }

  public PhysicsContinuum getPhysicsContinuaByName(String regexPatt){
    printAction(String.format("Getting Physics Continua by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    for(Object obj : sim.getContinuumManager().getChildren()){
        try{
            PhysicsContinuum phc = (PhysicsContinuum) obj;
            if(phc.getPresentationName().matches(regexPatt)){
                say("Found: " + phc.getPresentationName());
                return phc;
            }
        } catch (Exception e){ continue; }
    }
    setVerboseOn();
    say("Found NULL.");
    return null;
  }

  public Region getRegion(String regionName){
    return sim.getRegionManager().getRegion(regionName);
  }

  public Region getRegionByName(String regexPatt){
    /*
     * Returns the first match of Region, given the name pattern
     */
    sayA(String.format("Getting Region by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    Region foundReg = null;
    for(Region reg : getAllRegions()){
        if(reg.getPresentationName().matches(regexPatt)){
            foundReg = reg;
            break;
        }
    }
    setVerboseOn();
    if(foundReg != null) {
        sayA("Got: " + foundReg.getPresentationName());
        return foundReg;
    } else {
        sayA("Got NULL.");
        return null;
    }
  }

  public String getStringBetweenBrackets(String text) {
    Pattern patt = Pattern.compile(".*\\[(.*)\\]");
    Matcher matcher = patt.matcher(text);
    boolean matchFound = matcher.find();
    if (matchFound) {
        //say(matcher.group(1));
        return matcher.group(1);
        //for (int i=0; i<=matcher.groupCount(); i++) {
        //    String groupStr = matcher.group(i);
        //    say(matcher.group(i));
        //}
    }
    return noneString;
  }

  public String getStringBoundaryAndRegion(Boundary bdry){
    Region region = bdry.getRegion();
    return region.getPresentationName() + "\\" + bdry.getPresentationName();
  }

  public String getTime() {
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
  }

  public int getTrimmerGrowthRate(String growthRate){
    int VERYSLOW = 0;
    int SLOW = 1;
    int MEDIUM = 2;
    int FAST = 3;
    if(growthRate.equalsIgnoreCase("veryslow")){
        return VERYSLOW;
    }
    if(growthRate.equalsIgnoreCase("slow")){
        return SLOW;
    }
    if(growthRate.equalsIgnoreCase("medium")){
        return MEDIUM;
    }
    if(growthRate.equalsIgnoreCase("fast")){
        return FAST;
    }
    return MEDIUM;
  }

  public void groupRegionsByMedia(){
    printAction("Grouping Regions by Media: Fluid or Solid");
    Vector<Region> vecReg1 = new Vector<Region>();
    Vector<Region> vecReg2 = new Vector<Region>();
    for(Region region : getAllRegions()){
        if(isFluid(region)){
            vecReg1.add(region);
        } else {
            vecReg2.add(region);
        }
    }
    say("Fluid Regions: " + vecReg1.size());
    say("Solid Regions: " + vecReg2.size());
    sim.getRegionManager().getGroupsManager().groupObjects("Fluid Regions", vecReg1, false);
    sim.getRegionManager().getGroupsManager().groupObjects("Solid Regions", vecReg2, true);
    sayOK();
  }

  public void hardCopyPicture(Scene scene, String picName) {
    printAction("Saving a picture");
    if (!picName.matches(".*png$") || !picName.matches(".*jpg$") || !picName.matches(".*jpeg$")) {
        picName = picName + ".png";
    }
    scene.printAndWait(resolvePath(picName), 1, picResX, picResY);
    sayOK();
    }

  public boolean hasDeletedCells(Region region){
    if(region.getPresentationName().matches("^Cells deleted.*")){
        return true;
    }
    return false;
  }

  public boolean hasInterface(Boundary bdry){
      if(bdry.getDependentInterfaces().size() > 0){
          return true;
      }
    return false;
  }

  public boolean hasGeometryParts(){
    say("Querying if '" + sim.getPresentationName() + "' has parts");
    if(sim.getGeometryPartManager().isEmpty()){
        say("  Simulation has no Geometry Parts\n");
        return false;
    }
    say("  Simulation has Geometry Parts\n");
    return true;
  }

  public boolean hasPolyMesh(Region region){
    if(hasDeletedCells(region)){ return false; }
    if(region.getMeshContinuum() == null){ return false; }
    if(isPoly(region.getMeshContinuum())){
        sayV("Region: " + region.getPresentationName());
        sayV(" Has Poly mesh.\n");
        return true;
    }
    return false;
  }

  public boolean hasRadiationBC(Boundary bdry){
    try{
        bdry.getValues().get(EmissivityProfile.class);
        return true;
    } catch (Exception e){
        return false;
    }
  }

  public boolean hasTrimmerMesh(Region region){
    if(hasDeletedCells(region)){ return false; }
    if(region.getMeshContinuum() == null){ return false; }
    if(isTrimmer(region.getMeshContinuum())){
        sayV("Region: " + region.getPresentationName());
        sayV(" Has Trimmer mesh.\n");
        return true;
    }
    return false;
  }

  public boolean hasValidSurfaceMesh(){
    if(queryRemeshedSurface() == null){
        return false;
    }
    return true;
  }

  public boolean hasValidVolumeMesh(){
    if(queryVolumeMesh() == null){
        return false;
    }
    return true;
  }

  public boolean hasValidWrappedMesh(){
    if(queryWrappedSurface() == null){
        return false;
    }
    return true;
  }

  public void importAllDBSFromSubDirName(String subDirName) {
    printAction("Importing the DBS files.");
    File dbsSubPath = new File(dbsPath, subDirName);
    say("From Path: " + dbsSubPath.toString());
    String[] fileNames = dbsSubPath.list(dbsFilter);
    Vector<String> filesVect = new Vector<String>();
    File dbsFile = null;
    for(String fileName : fileNames){
        dbsFile = new File(dbsSubPath, fileName);
        filesVect.add(dbsFile.toString());
    }
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    prtImpMngr.importDbsParts(filesVect, "OneSurfacePerPatch", true, "OnePartPerFile", false, unit_m, 1);
    /*
     * Auto check the imported Parts.
     */
    if(!checkMeshQualityUponSurfaceMeshImport){ return; }
    checkFreeEdgesAndNonManifoldsOnParts();
  }

  public void importCADPart(String part){
    printAction("Importing CAD Part");
    String fName = resolvePath((new File(cadPath, part)).toString());
    say("File: " + fName);
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    int type = TessellationDensityOption.MEDIUM;
    if(fineTesselationOnImport){
        type = TessellationDensityOption.FINE;
    }
    prtImpMngr.importCadPart(fName, "SharpEdges", mshSharpEdgeAngle, type, false, false);
  }

  public void imprintAllPartsByCADMethod(){
    printAction("Imprinting All Parts by CAD Method");
    //- Gerou Free Edges
    //sim.get(MeshActionManager.class).imprintCadParts(getAllLeafPartsAsMeshParts(), "CAD");
    Object[] arrayObj = getAllLeafPartsAsCadParts().toArray();
    sim.get(MeshActionManager.class).imprintCadParts(new NeoObjectVector(arrayObj), "CAD");
    sayOK();
  }

  public void imprintAllPartsByDiscreteMethod(double tolerance){
    printAction("Imprinting All Parts by Discrete Method");
    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafPartsAsMeshParts(), "Discrete", tolerance);
    sayOK();
  }

  public void imprintPartsByCADMethod(Vector<MeshPart> vecMP){
    printAction("Imprinting Parts by CAD Method");
    say("Parts: ");
    for(MeshPart mp : vecMP){
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintCadParts(vecMP, "CAD");
//    sim.get(MeshActionManager.class).imprintDiscreteParts(vecMP, "CAD", 1.0E-4);
    sayOK();
  }

  public void imprintPartsByDiscreteMethod(Vector<MeshPart> vecMP, double tolerance){
    printAction("Imprinting Parts by Discrete Method");
    say("Parts: ");
    for(MeshPart mp : vecMP){
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintDiscreteParts(vecMP, "Discrete", tolerance);
    sayOK();
  }

  public void initializeMeshing(){
    printAction("Initializing Mesh Pipeline");
    MeshPipelineController mpc = sim.get(MeshPipelineController.class);
    mpc.initializeMeshPipeline();
    sayOK();
  }

  public String int2string(int number){
      return String.valueOf(number);
  }

  public LeafMeshPart intersect2PartsByDiscrete(Object obj1, Object obj2, String renameTo){
    printAction("Intersecting 2 Parts (obj1 'intersection' obj2)");
    say("Object 1: " + obj1.toString());
    say("Object 2: " + obj2.toString());
    MeshActionManager mshActMngr = sim.get(MeshActionManager.class);
    MeshPart mp = mshActMngr.intersectParts(new NeoObjectVector(new Object[] {obj1, obj2}), "Discrete");
    mp.setPresentationName(renameTo);
    say("Returning: " + mp.getPathInHierarchy());
    return (LeafMeshPart) mp;
  }

  @Deprecated
  public boolean isCompositePart_old(GeometryPart gp){
    try {
        CompositePart cp = (CompositePart) gp;
        return true;
    } catch (Exception e0) {
        return false;
    }
  }

  private boolean isBeanDisplayName(Boundary bdry, String whatIs){
    if(bdry.getBeanDisplayName().equals(whatIs)){ return true; }
    return false;
  }

  private boolean isBeanDisplayName(GeometryPart gp, String whatIs){
    if(gp.getBeanDisplayName().equals(whatIs)){ return true; }
    return false;
  }

  private boolean isBeanDisplayName(Interface intrfPair, String whatIs){
    if(intrfPair.getBeanDisplayName().equals(whatIs)){ return true; }
    return false;
  }

  public boolean isCadPart(GeometryPart gp){
    return isBeanDisplayName(gp, "CAD Part");
  }

  public boolean isCompositePart(GeometryPart gp){
    return isBeanDisplayName(gp, "Composite Part");
  }

  public boolean isCylinderPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Cylinder Part");
  }

  public boolean isDirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "DirectBoundaryInterfaceBoundary");
  }

  public boolean isDirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Direct Boundary Interface");
  }

  public boolean isFluid(Boundary bdry){
    return isFluid(bdry.getRegion());
  }

  public boolean isFluid(Region region){
    if(region.getRegionType().toString().equals("Fluid Region")){
        return true;
    }
    return false;
  }

  @Deprecated
  public boolean isFluidSolidInterface(DirectBoundaryInterface intrfPair){
    Region reg0 = intrfPair.getRegion0();
    Region reg1 = intrfPair.getRegion1();
    if(isFluid(reg0) && isSolid(reg1)) { return true; }
    if(isFluid(reg1) && isSolid(reg0)) { return true; }
    return false;
  }

  public boolean isFluidSolidInterface(Interface intrfPair){
    Region reg0 = intrfPair.getRegion0();
    Region reg1 = intrfPair.getRegion1();
    if(isFluid(reg0) && isSolid(reg1)) { return true; }
    if(isFluid(reg1) && isSolid(reg0)) { return true; }
    return false;
  }

  public boolean isIndirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "IndirectBoundaryInterfaceBoundary");
  }

  public boolean isIndirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Indirect Boundary Interface");
  }

  public boolean isInterface(Boundary bdry){
    if(isDirectBoundaryInterface(bdry) || isIndirectBoundaryInterface(bdry)){ return true; }
    return false;
  }

  public boolean isLeafMeshPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Leaf Mesh Part");
  }

  public boolean isMeshing(Boundary bdry){
    return isMeshing(bdry, true);
  }

  private boolean isMeshing(Boundary bdry, boolean quietOption){
    if(quietOption) { sayBdry(bdry); }
    if(bdry.getRegion().isMeshing()){
        return true;
    }
    if(quietOption) { say("Region not meshing. Skipping...\n"); }
    return false;
  }

  public boolean isPoly(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.dualmesher.DualMesherModel")){
        return true;
    }
    return false;
  }

  public boolean isRemesh(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.resurfacer.ResurfacerMeshingModel")){
        return true;
    }
    return false;
  }

  public boolean isSolid(Boundary bdry){
    return isSolid(bdry.getRegion());
  }

  public boolean isSolid(Region region){
    if(region.getRegionType().toString().equals("Solid Region")){
        return true;
    }
    return false;
  }

  public boolean isTrimmer(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.trimmer.TrimmerMeshingModel")){
        return true;
    }
    return false;
  }

  public boolean isWrapper(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.surfacewrapper.SurfaceWrapperMeshingModel")){
        return true;
    }
    return false;
  }

  public boolean isWallBoundary(Boundary bdry){
    String t1 = "Wall";
    String t2 = "Contact Interface Boundary";
    String type = bdry.getBoundaryType().toString();
    if(type.equals(t1) || type.equals(t2)){ return true; }
    return false;
  }

  public void playMacro(String macro) {
    String macroFile = macro;
    if (!macro.matches(".*java$")) {
        macroFile = macro + ".java";
    }
    printAction("MACRO: " + macroFile);
    new StarScript(sim, new java.io.File(resolvePath(macroFile))).play();
  }

  public PartRepresentation queryGeometryRepresentation(){
      return ((PartRepresentation) sim.getRepresentationManager().getObject("Geometry"));
  }

  public SurfaceRep queryImportRepresentation(){
      return querySurfaceRep("Import");
  }

  public SurfaceRep queryInitialSurface(){
    return querySurfaceRep("Initial Surface");
  }

  public SurfaceRep queryRemeshedSurface(){
    return querySurfaceRep("Remeshed Surface");
  }

  private SurfaceRep querySurfaceRep(String name){
    if(sim.getRepresentationManager().has(name)){
        return (SurfaceRep) sim.getRepresentationManager().getObject(name);
    }
    return null;
  }

  public SurfaceRep queryWrappedSurface(){
    return querySurfaceRep("Wrapped Surface");
  }

  public FvRepresentation queryVolumeMesh(){
    if(sim.getRepresentationManager().has("Volume Mesh")){
        return (FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh");
    }
    return null;
  }

  public void printAction(String text) {
    if(sim == null){
        sim = getActiveSimulation();
    }
    say("");
    printLine();
    say("+ " + getTime());
    say("+ " + text);
    printLine();
  }

  public void printFrame(String text) {
    if(sim == null){
        sim = getActiveSimulation();
    }
    say("");
    say("########################################################################################");
    say("########################################################################################");
    say("## ");
    say("## " + text.toUpperCase());
    say("## ");
    say("## " + getTime());
    say("## ");
    say("########################################################################################");
    say("########################################################################################");
    say("");
  }

  public void printLine(){
    say("+-----------------------------------------------------------------------------");
  }

  public void printLine(int n_times){
    for (int i = 1; i <= n_times; i++) {
        printLine();
    }
  }

  public void printMeshParameters(){
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**       M E S H   P A R A M E T E R S   O V E R V I E W         **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Mesh Continua: " + mshCont.getPresentationName());
    say("**");
    say("*******************************************************************");
    say("**");
    say("** Base Size (mm): " + mshBaseSize);
    say("**");
    say("** Surface Size Relative Min (%): " + mshSrfSizeMin);
    say("** Surface Size Relative Tgt (%): " + mshSrfSizeTgt);
    say("**");
    say("** Feature Curve Relative Min (%): " + featCurveMeshMin);
    say("** Feature Curve Relative Tgt (%): " + featCurveMeshTgt);
    say("**");
    if(isPoly(mshCont)){
        say("** Mesh Growth Factor: " + mshGrowthFactor);
        say("**");
    }
    if(isTrimmer(mshCont)){
        say("** Maximum Cell Size: " + mshTrimmerMaxCelSize);
        say("** Mesh Growth Rate: " + mshTrimmerGrowthRate.toUpperCase());
        say("**");
    }
    say("*******************************************************************");
    say("");
  }

  public void printPrismsParameters(){
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**     P R I S M S   P A R A M E T E R S   O V E R V I E W       **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Number of Layers: " + prismsLayers);
    say("** Relative Height (%): " + prismsRelSizeHeight);
    say("**");
    say("** Minimum Thickness (%): " + prismsMinThickn);
    say("** Stretching Ratio: " + prismsStretching);
    say("** Layer Chopping (%): " + prismsLyrChoppPerc);
    say("** Near Core Layer Aspect Ratio: " + prismsNearCoreAspRat);
    say("**");
    say("*******************************************************************");
    say("");
  }

  public void printSolverSettings(){
    say("");
    say("*******************************************************************");
    say("**                                                               **");
    say("**       S O L V E R   S E T T I N G S   O V E R V I E W         **");
    say("**                                                               **");
    say("*******************************************************************");
    say("**");
    say("** Maximum Number of Iterations: " + maxIter);
    say("**");
    say("** URF Velocity: " + urfVel);
    say("** URF Pressure: " + urfP);
    say("** URF Fluid Energy: " + urfFluidEnrgy);
    say("** URF Solid Energy: " + urfSolidEnrgy);
    say("** URF K-Epsilon: " + urfKEps);
    say("**");
    if(rampURF){
        say("** Linear Ramp Fluid Energy:");
        say("**   Start/End Iteration: " + urfRampFlIterBeg + "/" + urfRampFlIterEnd);
        say("**   Initial URF: " + urfRampFlBeg);
        say("**");
        say("** Linear Ramp Solid Energy:");
        say("**   Start/End Iteration: " + urfRampSldIterBeg + "/" + urfRampSldIterEnd);
        say("**   Initial URF: " + urfRampSldBeg);
        say("**");
    }
    say("*******************************************************************");
    say("");
  }

  public void rebuildCompositeHierarchy(CompositePart compPart, String splitChar){
    printAction("Rebuilding Composite Assembly Hierarchy based on a split character");
    say("Composite Part: " + compPart.getPresentationName());
    _reparentChildren(compPart, splitChar);
    sayOK();
  }

  private void _reparentChildren(CompositePart cp, String splitChar){
    CompositePart newCP = null;
    sayA("Looking in: " + cp.getPresentationName());
    String splitChar0 = splitChar;
    if(splitChar.equals("\\") || splitChar.equals("|")){
        splitChar = "\\" + splitChar;
    }
    //Vector<CompositePart> vecCP = new Vector<CompositePart>();
    for(GeometryPart gp : cp.getLeafParts()){
        String name = gp.getPresentationName();
        String[] splitName = name.split(splitChar);
        //say("Split Lenght: " + splitName.length);
        if(splitName.length <= 1) { continue; }
        String name0 = splitName[0];
        String gpNewName = name.replaceFirst(name0 + splitChar, "");
        if(gpNewName.equals(name)){
            say("");
            say(name);
            say("Old name == New name. Skipping...");
            say("");
            continue;
        }
        try{
            newCP = (CompositePart) cp.getChildParts().getPart(name0);
        } catch (Exception e){
            sayA("Creating Composite Part: " + name0);
            newCP = cp.getChildParts().createCompositePart();
            newCP.setPresentationName(name0);
        }
        gp.setPresentationName(gpNewName);
//        if(gpNewName.split(splitChar).length > 1){
            sayA("Parenting: ");
            sayA("  + " + newCP.getPresentationName());
            sayA("  |---+ " + gp.getPresentationName());
            sayOldNameNewName(name, gpNewName);
            gp.reparent(newCP.getChildParts());
//        }
//        if(gpNewName.matches(".*" + splitChar + ".*")){
            _reparentChildren(newCP, splitChar0);
//        }
    }
  }

  public void removeAllPartsContacts(){
    printAction("Removing All Parts Contacts");
    for(GeometryPart gp : getAllLeafParts()){
        CadPart cp = (CadPart) gp;
        Collection<PartContact> contacts = sim.get(SimulationPartManager.class).getPartContactManager().getObjects();
        for(PartContact pc : contacts){
            sim.get(SimulationPartManager.class).getPartContactManager().remove(pc);
        }
    }
    sayOK();
  }

  public void removeCadPart(String name){
    /*
     * Attention, CadPart must belong to a Composite
     */
    printAction("Removing Cad Part: " + name);
    cadPrt = getCadPart(name);
    compPart = (CompositePart) cadPrt.getParentPart();
    compPart.getChildParts().removePart(cadPrt);
    say("Removed: " + cadPrt.getPathInHierarchy());
    if(cadPrt == null){
        say("CadPart not found: " + name);
    }
    sayOK();
  }

  public int removeCompositePart(CompositePart compPart){
    printAction("Removing a Composite Part");
    say("Name: " + compPart.getPresentationName());
    try{
        CompositePart parent = ((CompositePart) compPart.getParentPart());
        say("Removing Composite: " + compPart.getPresentationName());
        parent.getChildParts().remove(compPart);
        sayOK();
        return 0;
    } catch (Exception e){
        say("ERROR! Could not remove Composite Part");
        return 1;
    }
  }

  public void removeCompositeParts(Vector<CompositePart> vecCompParts){
    printAction("Removing Composite Parts");
    int n = 0;
    say("Composite Parts to be removed: " + vecCompParts.size());
    for(CompositePart cp : vecCompParts){
        int ret = removeCompositePart(cp);
        if(ret == 0){ n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK();
  }

  public void removeCompositePartsByName(String regexPatt){
    printAction(String.format("Removing Composite Parts based on REGEX criteria: \"%s\"", regexPatt ));
    setVerboseOff();
    Vector<CompositePart> remPrtVec = new Vector<CompositePart>();
    for(CompositePart cP : getAllCompositeParts()){
        String name = cP.getPresentationName();
        if (name.matches(regexPatt)) {
            say("Marking Composite: " + name);
            remPrtVec.add(cP);
        }
    }
    say("Composite Parts marked for removal: " + remPrtVec.size());
    setVerboseOn();
    if (remPrtVec.isEmpty()) {
        say(String.format("No Composite Part was found with REGEX criteria: \"%s\"", regexPatt));
        return;
    }
    int n = 0;
    for(CompositePart cP : remPrtVec){
        try{
            CompositePart parent = ((CompositePart) cP.getParentPart());
            say("Removing Composite: " + cP.getPresentationName());
            parent.getChildParts().remove(cP);
            n++;
        } catch (Exception e){ }
    }
    say("Composite Parts succesfully removed: " + n);
    sayOK();
  }

  public void removeGeometryPartsByName(String regexPatt){
    printAction(String.format("Removing Geometry Parts based on REGEX criteria: \"%s\"", regexPatt ));
    setVerboseOff();
    for(GeometryPart gp : getAllGeometryPartsByName(regexPatt)){
        String name = gp.getPresentationName();
        try{
            sim.get(SimulationPartManager.class).remove(gp);
            say("Geometry Part removed: " + name);
        } catch(Exception e){
            say("Could not remove as Geometry Part: " + name);
        }
        try{
            say("Trying to remove as Composite Part");
            removeCompositePart((CompositePart) gp);
        } catch(Exception e){
            say("Could not remove as Composite Part: " + name);
        }
    }
    sayOK();
  }

  public void removeInvalidCells(){
    Vector<Region> regionsPoly = new Vector<Region>();
    Vector<Region> regionsTrimmer = new Vector<Region>();
    Vector<Region> fvRegions = new Vector<Region>();
    setVerboseOff();
    for(Region region : getAllRegions()){
        if(hasPolyMesh(region)){
            regionsPoly.add(region);
            continue;
        }
        if(hasTrimmerMesh(region)){
            regionsTrimmer.add(region);
            continue;
        }
        fvRegions.add(region);
    }
    setVerboseOn();
    /* Removing From fvRepresentation */
    if(fvRegions.size() > 0){
        printAction("Removing Invalid Cells from Regions using Default Parameters");
        say("Number of Regions: " + fvRegions.size());
        sim.getMeshManager().removeInvalidCells(fvRegions, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Poly Meshes */
    if(regionsPoly.size() > 0){
        printAction("Removing Invalid Cells from Poly Meshes");
        say("Number of Regions: " + regionsPoly.size());
        if(aggressiveRemoval){
            minFaceValidity = 0.95;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(regionsPoly, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
    /* Removing From Trimmer Meshes */
    if(regionsTrimmer.size() > 0){
        printAction("Removing Invalid Cells from Trimmer Meshes");
        say("Number of Regions: " + regionsTrimmer.size());
        if(aggressiveRemoval){
            minFaceValidity = 0.51;
            minCellQuality = 1.e-5;
            minVolChange = 1.e-4;
            minContigCells = 100;
            minConFaceAreas = 1.e-8;
        }
        sim.getMeshManager().removeInvalidCells(regionsTrimmer, minFaceValidity, minCellQuality,
            minVolChange, minContigCells, minConFaceAreas, minCellVolume);
        sayOK();
    }
  }

  public void removeLeafMeshPart(LeafMeshPart lmp){
    printAction("Removing Leaf Mesh Part: " + lmp.getPresentationName());
    sim.get(SimulationPartManager.class).removePart(lmp);
    sayOK();
  }

  public void removeLeafMeshParts(Collection<LeafMeshPart> colLMP){
    printAction("Removing Leaf Mesh Parts");
    say("Leaf Mesh Parts to be removed: " + colLMP.size());
    for(LeafMeshPart lmp : colLMP){
        say("  " + lmp.getPathInHierarchy());
    }
    sim.get(SimulationPartManager.class).removeParts(colLMP);
    sayOK();
  }

  public void removeLeafMeshPartsByName(String regexPatt){
    printAction(String.format("Remove Leaf Meshs Part by REGEX pattern: \"%s\"", regexPatt));
    Collection<LeafMeshPart> colLMP = getAllLeafMeshPartsByName(regexPatt);
    say("Leaf Mesh Parts to be removed: " + colLMP.size());
    for(LeafMeshPart lmp : colLMP){
        say("  " + lmp.getPathInHierarchy());
    }
    sim.get(SimulationPartManager.class).removeParts(colLMP);
    sayOK();
  }

  public void removeLeafPartsByName(String regexPatt){
    printAction(String.format("Remove Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    Collection<GeometryPart> colLP = getAllLeafPartsByName(regexPatt);
    say("Leaf Parts to be removed: " + colLP.size());
    for(GeometryPart gp : colLP){
        say("  " + gp.getPathInHierarchy());
    }
    sim.get(SimulationPartManager.class).removeObjects(colLP);
    sayOK();
  }

  public void removePartsContacts(GeometryPart refPart, Vector<GeometryPart> vecPart){
    printAction("Removing Parts Contacts");
    say("Reference Part: " + refPart.getPathInHierarchy());
    say("Contacting Parts: ");
    for(GeometryPart gp : vecPart){
        say("  " + gp.getPathInHierarchy());
    }
    sim.get(SimulationPartManager.class).getPartContactManager().removePartContacts(refPart, vecPart);
    sayOK();
  }

  public void removeGeometryPartInComposite(GeometryPart gp){
    printAction("Removing a Geometry Part which belongs to a Composite Part");
    say("Removing: " + gp.getPathInHierarchy());
    ((CompositePart) gp.getParentPart()).getChildParts().removePart(gp);
    sayOK();
  }

  public void renameInterfacesOnParts(){
    printAction("Renaming internal faces acording to interfaces");
    for(GeometryPart part : getAllGeometryParts()) {
        LeafMeshPart leafPart = ((LeafMeshPart) part);
        Collection<PartSurface> partSurfaces = leafPart.getPartSurfaces();
        if(partSurfaces.size() > 0){
            say("Geometry Part: " + part.getPresentationName());
        }
        for(PartSurface srf : partSurfaces) {
            String name = srf.getPresentationName();
            String newName = getStringBetweenBrackets(name);
            if(!newName.equals(noneString)) {
                // Skip the interfaces
                sayOldNameNewName(name, newName);
                srf.setPresentationName(newName);
            }
        }
        say("");
        //break;
    }
  }

  public void renamePartSurfacesBasedOnSearchStrings(String hasString1, String hasString2, String renameTo){
    printAction("Renaming Part Surface(s)");
    for(PartSurface ps : getAllPartSurfacesFromLeafParts()){
        if(ps.getPresentationName().matches(hasString1) && ps.getPresentationName().matches(hasString2)){
            sayOldNameNewName(ps.getPresentationName(), renameTo);
            ps.setPresentationName(renameTo);
            say("");
        }
    }
    sayOK();
  }

  public void renameBoundaryInRegion(Region region, String bdryName, String bdryNewName){
    printAction("Renaming Boundary");
    sayRegion(region);
    getBoundaryInRegion(region, bdryName).setPresentationName(bdryNewName);
    sayOldNameNewName(bdryName, bdryNewName);
  }

  public void resetBoundaryNames(Region region){
    printAction("Resetting Boundary Names");
    sayRegion(region);
    for(Boundary bdry : region.getBoundaryManager().getBoundaries()){
        String name = bdry.getPresentationName();
        String newName = name.replace(region.getPresentationName() + ".", "");
        sayOldNameNewName(name, newName);
        bdry.setPresentationName(newName);
    }
    sayOK();
  }

  public void resetSurfaceRemesher(MeshContinuum mshCont){
    enableSurfaceRemesher(mshCont);
    enableSurfaceProximityRefinement(mshCont);
    enableSurfaceRemesherAutomaticRepair(mshCont);
    enableSurfaceRemesherProjectToCAD(mshCont);
  }

  public void reTesselateCadPartToFine(CadPart part){
    printAction("ReTesselating a Part To Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.FINE);
    sayOK();
  }

  public void reTesselateCadPartToVeryFine(CadPart part){
    printAction("ReTesselating a Part To Very Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.VERY_FINE);
    sayOK();
  }

  public void reTesselateCadPart(CadPart part, int type){
    say("Part: " + part.getPathInHierarchy());
    part.getTessellationDensityOption().setSelected(type);
    part.getCadPartEdgeOption().setSelected(CadPartEdgeOption.SHARP_EDGES);
    part.setSharpEdgeAngle(mshSharpEdgeAngle);
    part.tessellate();
    sayOK();
  }

  public void say(String msg){
    sim.println(sayPreffixString + " " + msg);
  }

  private void sayA(String msg){
    // Say Auxiliary
    sayLevel++;
    say(msg.replace(sayPreffixString, sayPreffixString+"[aux]"));
    sayLevel--;
  }

  public void sayBdry(Boundary bdry){
    String b = bdry.getPresentationName();
    String r = bdry.getRegion().getPresentationName();
    say("Boundary: " + b + "\t[Region: " + r + "]");
    say("Bean Display Name is \"" + bdry.getBeanDisplayName() + "\".");
  }

  private void sayInterface(Interface intrfPair){
    say("Interface: " + intrfPair.getPresentationName());
    say("Bean Display Name is \"" + intrfPair.getBeanDisplayName() + "\".");
    sayInterfaceSides(intrfPair);
  }

  private void sayInterfaceSides(Interface intrfPair){
    String side1 = null, side2 = null;
    if(isDirectBoundaryInterface(intrfPair)){
        side1 = getStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = getStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    if(isIndirectBoundaryInterface(intrfPair)){
        side1 = getStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = getStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    say("  Side1: " + side1);
    say("  Side2: " + side2);
  }

  private void sayMeshContinua(MeshContinuum mshCont){
    say("Mesh Continua: " + mshCont.getPresentationName());
  }

  public void sayOK(){
    say("OK!\n");
  }

  private void sayOldNameNewName(String name, String newName){
    say("  Old name: " + name);
    say("  New name: " + newName);
    say("");
  }

  private void sayPartSurface(PartSurface ps){
    say("Part Surface: " + ps.getPresentationName());
    if(isCadPart(ps.getPart())){
        say("CAD Part: " + ((CadPart) ps.getPart()).getPathInHierarchy());
    }
    if(isLeafMeshPart(ps.getPart())){
        say("Leaf Mesh Part: " + ((LeafMeshPart) ps.getPart()).getPathInHierarchy());
    }
  }

  private void sayPhysicsContinua(PhysicsContinuum physCont){
    say("Physics Continua: " + physCont.getPresentationName());
  }

  private void sayRegion(Region region){
    say("Region: " + region.getPresentationName());
  }

  private void sayV(String msg){
    if(verbose) { sayA(msg); }
  }

  public void saveSim(){
    saveSim(sim.getPresentationName());
  }

  public void saveSim(String name){
    String newName = name + ".sim";
    printAction("Saving: " + newName);
    sim.saveState(new File(simPath, newName).toString());
  }

  public void saveSimWithSuffix(String suffix){
    if(!saveIntermediates){ return; }
    String newName = simTitle + "_" + suffix;
    saveSim(newName);
    savedWithSuffix++;
  }

  public void saveSimWithSuffix(String suffix, boolean forceOption){
    boolean interm = saveIntermediates;
    if(forceOption){
        saveIntermediates = true;
    }
    saveSimWithSuffix(suffix);
    saveIntermediates = interm;
  }

  public void setAutoSave(){
    printAction("Setting Auto Save Options");
    AutoSave as = sim.getSimulationIterator().getAutoSave();
    as.getTriggerOption().setSelected(AutoSaveTriggerOption.ITERATION);
    as.setEnabled(true);
    as.setAutoSaveBatch(true);
    as.setAutoSaveMesh(false);
    as.setMaxAutosavedFiles(autoSaveMaxFiles);
    as.setTriggerFrequency(autoSaveFrequencyIter);
    as.setSeparator(autoSaveSeparator);
    as.setFormatWidth(6);
  }

  public void setBC_StagnationInlet(Boundary bdry, double T, double ti, double tvr){
    printAction("Setting BC as Stagnation Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(StagnationBoundary.class);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_ConstantTemperatureWall(Boundary bdry, double T){
    printAction("Setting BC as Constant Temperature Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.TEMPERATURE);
    setBC_StaticTemperature(bdry, T);
    sayOK();
  }

  public void setBC_ConvectionWall(Boundary bdry, double T, double htc){
    printAction("Setting BC as Convection Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.CONVECTION);
    AmbientTemperatureProfile atp = bdry.getValues().get(AmbientTemperatureProfile.class);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    HeatTransferCoefficientProfile htcp = bdry.getValues().get(HeatTransferCoefficientProfile.class);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(htc);
    sayOK();
  }

  public void setBC_EnvironmentWall(Boundary bdry, double T, double htc, double emissivity,
                                                double transmissivity, double externalEmissivity){
    setBC_ConvectionWall(bdry, T, htc);
    printLine(3);
    printAction("Setting BC as an Environment Wall");
    sayBdry(bdry);
    if(hasRadiationBC(bdry)){
        say("  External Emissivity: " + externalEmissivity);
        setRadiationParametersS2S(bdry, emissivity, transmissivity);
        bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.ENVIRONMENT);
        ExternalEmissivityProfile eemP = bdry.getValues().get(ExternalEmissivityProfile.class);
        eemP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(externalEmissivity);
    } else {
        say("  Radiation Settings not available. Skipping...");
        return;
    }
    printLine(3);
    say("Environment BC set.");
    sayOK();
  }

  public void setBC_FreeSlipWall(Boundary bdry){
    printAction("Setting BC as a Free Slip Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.SLIP);
    sayOK();
  }

  public void setBC_MassFlowInlet(Boundary bdry, double T, double mfr, double ti, double tvr){
    printAction("Setting BC as Mass Flow Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(MassFlowBoundary.class);
    MassFlowRateProfile mfrp = bdry.getValues().get(MassFlowRateProfile.class);
    mfrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(mfr);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_PressureOutlet(Boundary bdry, double T, double ti, double tvr){
    printAction("Setting BC as Pressure Outlet");
    sayBdry(bdry);
    bdry.setBoundaryType(PressureBoundary.class);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_Symmetry(Boundary bdry){
    printAction("Setting BC as Symmetry");
    sayBdry(bdry);
    bdry.setBoundaryType(SymmetryBoundary.class);
    sayOK();
  }

  public void setBC_VelocityInlet(Boundary bdry, double T, double vel, double ti, double tvr){
    printAction("Setting BC as Velocity Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(InletBoundary.class);
    VelocityMagnitudeProfile vmp = bdry.getValues().get(VelocityMagnitudeProfile.class);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(vel);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  private void setBC_TI_and_TVR(Boundary bdry, double ti, double tvr){
    TurbulenceIntensityProfile tip = bdry.getValues().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti);
    TurbulentViscosityRatioProfile tvrp = bdry.getValues().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr);
  }

  private void setBC_StaticTemperature(Boundary bdry, double T){
    StaticTemperatureProfile stp = bdry.getValues().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  private void setBC_TotalTemperature(Boundary bdry, double T){
    TotalTemperatureProfile ttp = bdry.getValues().get(TotalTemperatureProfile.class);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  public void setGlobalBoundaryMeshRefinement(double min, double tgt){
    printAction("Setting global mesh refinement on all Boundaries");
    for(Boundary bdry : getAllBoundaries()){
        if(isInterface(bdry)) { continue; }
        setMeshBoundarySurfaceSizes(bdry, min, tgt);
    }
  }

  public void setGlobalFeatureCurveMeshRefinement(double min, double tgt) {
    printAction("Setting global mesh refinement on all Feature Curves");
    for(Region region : getAllRegions()){
        sayRegion(region);
        Collection<FeatureCurve> colFC = region.getFeatureCurveManager().getFeatureCurves();
        for(FeatureCurve featCurve : colFC) {
            setMeshFeatureCurveSizes(featCurve, min, tgt);
        }
    }
  }

  public void setInterfaceTolerance(Interface intrfPair, double tol) {
    String intrfType = intrfPair.getBeanDisplayName();
    printAction("Setting tolerance for a " + intrfType);
    sayInterface(intrfPair);
    say("  Tolerance: " + tol);
    if(isDirectBoundaryInterface(intrfPair)){
        intrfPair.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
    }
    if(isIndirectBoundaryInterface(intrfPair)){
        intrfPair.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
    }
    sayOK();
  }

  public void setMeshBoundaryPrismSizes(Boundary bdry, int numLayers, double stretch, double relSize) {
    if(tempMeshSizeSkip){ return; }
    printAction("Setting Custom Boundary Prism Layer");
    if(!isMeshing(bdry, false)){ return; }
    setMeshPrismsParameters(bdry, numLayers, stretch, relSize);
  }

  public void setMeshBoundarySurfaceSizes(Boundary bdry, double min, double tgt){
    if(tempMeshSizeSkip){ return; }
    printAction("Setting Custom Surface Mesh Size");
    setMeshSurfaceSize(bdry, min, tgt);
  }

  @Deprecated
  public void setMeshDirectBoundaryInterfaceSurfaceSizes(DirectBoundaryInterface intrfPair, double min, double tgt){
    printAction("Custom Surface Mesh Size for Interface");
    sayInterface(intrfPair);
    say("  " + getMinTargetString(min, tgt));
    try {
        intrf.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrf.get(MeshValueManager.class).get(SurfaceSize.class);
        srfSize.getRelativeMinimumSize().setPercentage(min);
        srfSize.getRelativeTargetSize().setPercentage(tgt);
    } catch (Exception e) {
        say("ERROR! Please review settings. Skipping this Interface.");
        say(e.getMessage());
    }
    sayOK();
  }

  public void setMeshFeatureCurveSizes(FeatureCurve fc, double min, double tgt){
    say("Custom Feature Curve Mesh Size: " + fc.getPresentationName());
    try {
        fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = fc.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSize(srfSize, min, tgt);
    } catch (Exception e) {
        say("ERROR! Please review settings. Skipping this Feature Curve.");
        say(e.getMessage());
    }
    sayOK();
  }

  public void setMeshFeatureCurvesSizesByRegionName(String regexPatt, double min, double tgt){
    printAction(String.format("Setting Mesh Feature Curves by REGEX pattern: \"%s\"", regexPatt));
    for(Region reg : getAllRegionsByName(regexPatt)) {
        for(FeatureCurve fc : getFeatureCurvesFromRegion(reg)){
            setMeshFeatureCurveSizes(fc, min, tgt);
        }
    }
  }

  public void setMeshPerRegionFlag(MeshContinuum mshCont){
    printAction("Setting Mesh Continua as \"Per-Region Meshing\"");
    sayMeshContinua(mshCont);
    mshCont.setMeshRegionByRegion(true);
    sayOK();
  }

  private void setMeshPrismsParameters(Boundary bdry, int numLayers, double stretch, double relSize){
    sayBdry(bdry);
    say("  Number of Layers: " + numLayers);
    say("  Stretch Factor: " + String.format("%.2f",stretch));
    say("  Height Relative Size: " + String.format("%.2f%%", relSize));
    if(isDirectBoundaryInterface(bdry)){
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) bdry;
        try {
            intrfBdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
            intrfBdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
            intrfBdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
            setMeshPrismsThickness(intrfBdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
        } catch (Exception e) {
            say("Prisms not available here. Skipping...");
        }
    } else if(isIndirectBoundaryInterface(bdry)){
        say("Prisms not available here. Skipping...");
    } else {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        bdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        bdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(bdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
    }
    sayOK();
  }

  private void setMeshPrismsThickness(PrismThickness prismThick, double relSize){
    ((GenericRelativeSize) prismThick.getRelativeSize()).setPercentage(relSize);
  }

  public void setMeshSurfaceGrowthRate(Boundary bdry, int growthRate){
    /*
      This method only works with Trimmer. Options:
            SurfaceGrowthRateOption.VERYSLOW;
            SurfaceGrowthRateOption.SLOW;
            SurfaceGrowthRateOption.MEDIUM
            SurfaceGrowthRateOption.FAST
     */
    printAction("Setting Custom Surface Growth on Trimmer Mesh");
    sayBdry(bdry);
    bdry.get(MeshConditionManager.class).get(CustomSurfaceGrowthRateOption.class).setEnabled(true);
    bdry.get(MeshValueManager.class).get(CustomSimpleSurfaceGrowthRate.class).getSurfaceGrowthRateOption().setSelected(growthRate);
    sayOK();
  }

  private void setMeshSurfaceSize(Boundary bdry, double min, double tgt){
    sayBdry(bdry);
    if(!isMeshing(bdry, false)){
        say("Region has no Mesh Continua. Skipping...");
    } else if (isIndirectBoundaryInterface(bdry)){
        say("Skipping...");
    } else if(isDirectBoundaryInterface(bdry)){
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) bdry;
        DirectBoundaryInterface intrfPair = intrfBdry.getDirectBoundaryInterface();
        intrfPair.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrfPair.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSize(srfSize, min, tgt);
    } else {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = bdry.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSize(srfSize, min, tgt);
    }
    sayOK();
  }

  private void setMeshSurfaceSize(SurfaceSize srfSize, double min, double tgt){
    say("  " + getMinTargetString(min, tgt));
    srfSize.getRelativeMinimumSize().setPercentage(min);
    srfSize.getRelativeTargetSize().setPercentage(tgt);
  }

  public void setMeshBaseSize(MeshContinuum mshCont, double baseSize, Units unit){
    printAction("Setting Mesh Continua Base Size");
    sayMeshContinua(mshCont);
    say("  Base Size: " + baseSize + unit.toString());
    mshCont.getReferenceValues().get(BaseSize.class).setUnits(unit);
    mshCont.getReferenceValues().get(BaseSize.class).setValue(baseSize);
    sayOK();
  }

  public void setMeshCurvatureNumberOfPoints(MeshContinuum mshCont, double numPoints){
    printAction("Setting Mesh Continua Surface Curvature");
    sayMeshContinua(mshCont);
    say("  Points/Curve: " + numPoints);
    mshCont.getReferenceValues().get(SurfaceCurvature.class).getSurfaceCurvatureNumPts().setNumPointsAroundCircle(numPoints);
    sayOK();
  }

  public void setMeshSurfaceSizes(MeshContinuum mshCont, double min, double tgt){
    printAction("Setting Mesh Continua Surface Sizes");
    sayMeshContinua(mshCont);
    SurfaceSize srfSize = mshCont.getReferenceValues().get(SurfaceSize.class);
    setMeshSurfaceSize(srfSize, min, tgt);
    sayOK();
  }

  public void setMeshTetPolyGrowthRate(MeshContinuum mshCont, double growthFactor){
    printAction("Setting Mesh Volume Growth Factor");
    sayMeshContinua(mshCont);
    say("  Growth Factor: " + growthFactor);
    mshCont.getReferenceValues().get(VolumeMeshDensity.class).setGrowthFactor(growthFactor);
    sayOK();
  }

  public void setMeshWrapperScaleFactor(MeshContinuum mshCont, double scaleFactor){
    printAction("Setting Wrapper Scale Factor");
    sayMeshContinua(mshCont);
    if(scaleFactor < 1){
        say("Warning! Scale Factor < 1. Multiplying by 100.");
        scaleFactor *= 100.;
    }
    say("Scale Factor: " + scaleFactor);
    mshCont.getReferenceValues().get(SurfaceWrapperScaleFactor.class).setScaleFactor(scaleFactor);
    sayOK();
  }

  public void setMeshWrapperVolumeExternal(Region region){
    printAction("Setting Wrapping Region as EXTERNAL in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.EXTERNAL);
    sayOK();
  }

  public void setMeshWrapperVolumeLargestInternal(Region region){
    printAction("Setting Wrapping Region as LARGEST INTERNAL in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.LARGEST_INTERNAL);
    sayOK();
  }

  public void setMeshWrapperVolumeSeedPoints(Region region){
    printAction("Setting Wrapping Region as SEED POINTS in VOLUME OF INTEREST");
    sayRegion(region);
    region.get(MeshConditionManager.class).get(VolumeOfInterestOption.class).setSelected(VolumeOfInterestOption.SEED_POINT);
    sayOK();
  }

  private void setRadiationEmissivityTransmissivity(Boundary bdry, double emissivity, double transmissivity){
    printAction("S2S Radiation Parameters on Boundary");
    sayBdry(bdry);
    if(!hasRadiationBC(bdry)){
        say("  Radiation Settings not available. Skipping...");
        return;
    }
    EmissivityProfile emP;
    TransmissivityProfile trP = null;
    if(isDirectBoundaryInterface(bdry)){
        DirectBoundaryInterfaceBoundary intrfBdryD = (DirectBoundaryInterfaceBoundary) bdry;
        emP = intrfBdryD.getValues().get(EmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    } else if(isIndirectBoundaryInterface(bdry)){
        IndirectBoundaryInterfaceBoundary intrfBdryI = (IndirectBoundaryInterfaceBoundary) bdry;
        emP = intrfBdryI.getValues().get(EmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    } else {
        emP = bdry.getValues().get(EmissivityProfile.class);
        trP = bdry.getValues().get(TransmissivityProfile.class);
        setRadiationEmissTransmiss(emP, emissivity, trP, transmissivity);
    }
  }

  private void setRadiationEmissTransmiss(EmissivityProfile emP, double em, TransmissivityProfile trP, double tr){
    say("  Emissivity: " + em);
    emP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(em);
    if(trP == null){ return; }
    say("  Transmissivity: " + tr);
    trP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tr);
  }

  public void setRadiationParametersS2S(Region region, double angle, double patchProportion){
    printAction("S2S Radiation Parameters on Region");
    sayRegion(region);
    say("  Sharp Angle: " + angle);
    say("  Face/Patch Proportion: " + patchProportion);
    SharpAngle sharpAngle = region.getValues().get(SharpAngle.class);
    sharpAngle.setSharpAngle(angle);
    PatchPerBFaceProportion patchProp = region.getValues().get(PatchPerBFaceProportion.class);
    patchProp.setPatchPerBFaceProportion(patchProportion);
    sayOK();
  }

  public void setRadiationParametersS2S(Boundary bdry, double emissivity, double transmissivity){
    setRadiationEmissivityTransmissivity(bdry, emissivity, transmissivity);
    sayOK();
  }

  public void setRadiationParametersS2S(Boundary bdry, double emissivity, double transmissivity, double temperature){
    setRadiationEmissivityTransmissivity(bdry, emissivity, transmissivity);
    say("  Radiation Temperature: " + temperature);
    try{
        RadiationTemperatureProfile rtP = bdry.getValues().get(RadiationTemperatureProfile.class);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(temperature);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    } catch (Exception e){
        say("ERROR! Radiation Temperature not applicable on: " + bdry.getPresentationName());
    }
    sayOK();
  }

  public void setSimMaxIterationStoppingCriteria(int n){
    ((StepStoppingCriterion)
        sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps")).setMaximumNumberSteps(n);
    maxIter = n;
  }

  public void setVerboseOff(){
    verbose = false;
  }

  public void setVerboseOn(){
    verbose = true;
  }

  public void splitPartSurfaceByAngle(CompositePart compPrt, double splitAngle){
    printAction("Splitting Part Surfaces by Angle");
    say("Composite Part: " + compPrt.getPresentationName());
    List list = Arrays.asList(arrayPartSurfacesSplitAngleException);
    for(GeometryPart gp : compPrt.getLeafParts()){
        say("Part: " + gp.getPathInHierarchy());
        for(PartSurface ps : gp.getPartSurfaces()){
            if(list.contains(ps.getPresentationName())){ return; }
            splitPartSurfaceByAngle(ps, splitAngle);
        }
    }
    sayOK();
  }

  public void splitPartSurfaceByAngle(GeometryPart geomPrt, double splitAngle){
    printAction("Splitting Part Surfaces by Angle");
    List list = Arrays.asList(arrayPartSurfacesSplitAngleException);
    say("Geometry Part: " + geomPrt.getPathInHierarchy());
    for(PartSurface ps : geomPrt.getPartSurfaces()){
        if(list.contains(ps.getPresentationName())){ return; }
        splitPartSurfaceByAngle(ps, splitAngle);
    }
    sayOK();
  }

  public void splitPartSurfaceByAngle(PartSurface ps, double splitAngle){
    say("Splitting Part Surface by Angle:");
    sayPartSurface(ps);
    say("  Angle: " + splitAngle);
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    vecPS.add(ps);
    if(isCadPart(ps.getPart())){
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if(isCylinderPart(ps.getPart())){
        SimpleCylinderPart scp = (SimpleCylinderPart) ps.getPart();
        scp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if(isLeafMeshPart(ps.getPart())){
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    sayOK();
  }

  public Vector<PartSurface> splitPartSurfacesNonContiguous(PartSurface ps){
    printAction("Splitting Non Contiguous Part Surface");
    sayPartSurface(ps);
    String name0 = ps.getPresentationName();
    String mySplit = "__splitFrom__" + name0;
    ps.setPresentationName(mySplit);
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    Object[] objArr = {ps};
    if(isCadPart(ps.getPart())){
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    if(isLeafMeshPart(ps.getPart())){
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    for(PartSurface ps1 : ps.getPart().getPartSurfaces()){
        if(ps == ps1){
            ps1.setPresentationName(name0);
            vecPS.insertElementAt(ps1, 0);
        }
        if(ps1.getPresentationName().matches(mySplit + ".*")){
            vecPS.add(ps1);
        }
    }
    sayOK();
    return vecPS;
  }

  public void splitPartSurfacesNonContiguousByName(String regexPatt){
    printAction("Splitting Non Contiguous Part Surfaces");
    printLine(3);
    for(PartSurface ps : getAllPartSurfacesByName(regexPatt)){
        splitPartSurfacesNonContiguous(ps);
    }
    printLine(3);
    sayOK();
  }

  public void splitRegionNonContiguous(Region region){
    printAction("Splitting Non Contiguous Regions");
    MeshContinuum mshc = null;
    PhysicsContinuum phc = null;
    sayRegion(region);
    Object[] objArr = {region};
    sim.getMeshManager().splitNonContiguousRegions(new NeoObjectVector(objArr), minConFaceAreas);
    // Loop into the generated Regions created by Split
    for(Region reg : getAllRegionsByName("^" + region.getPresentationName() + " \\d{1,2}")){
        if(region == reg){ continue; }
        mshc = reg.getMeshContinuum();
        if(mshc != null){ mshc.erase(reg); }
        phc = reg.getPhysicsContinuum();
        if(phc != null){ phc.erase(reg); }
        say("  Disabled Region: " + reg.getPresentationName());
    }
    sayOK();
  }

  public LeafMeshPart subtract2PartsByDiscrete(Object srcObj, GeometryPart tgtPart, boolean combinePartSurfaces){
    printAction("Subtracting 2 Parts (target = target - source)");
    say("Source Part: " + ((GeometryPart) srcObj).getPathInHierarchy());
    say("Target Part: " + tgtPart.getPathInHierarchy());
    MeshActionManager mshActMngr = sim.get(MeshActionManager.class);
    MeshPart mp = mshActMngr.subtractParts(new NeoObjectVector(new Object[] {srcObj, tgtPart}), (MeshPart) tgtPart, "Discrete");
    if(combinePartSurfaces){
        Collection<PartSurface> colPS = mp.getPartSurfaceManager().getPartSurfaces();
        mp.combinePartSurfaces(colPS);
        colPS = mp.getPartSurfaceManager().getPartSurfaces();
        for(PartSurface ps : colPS){
            ps.setPresentationName("Faces");
        }
        say("Part Surfaces combined");
    }
    say("Returning: " + mp.getPathInHierarchy());
    return (LeafMeshPart) mp;
  }

  public String str2regex(String text){
    return ".*" + text + ".*";
  }

  public void turnOffRegion(Region region){
    printAction("Turning OFF Mesh and Physic Continuas");
    say("Changing Region: " + region.getPresentationName());
    region.getMeshContinuum().erase(region);
    region.getPhysicsContinuum().erase(region);
  }

  public void unsetMeshPerRegionFlag(MeshContinuum mshCont){
    printAction("Unsetting Mesh Continua as \"Per-Region Meshing\"");
    sayMeshContinua(mshCont);
    mshCont.setMeshRegionByRegion(false);
    sayOK();
  }

  private Units getUnit(String unitString){
    try{
        Units unit = ((Units) sim.getUnitsManager().getObject(unitString));
        say("Unit read: " + unit.toString());
        return unit;
    } catch (Exception e) {
        say("Unit not read: " + unitString);
        return null;
    }
  }

  public void updateOrCreateNewUnits(){
    printAction("Updating/Creating Units");
    unit_C = getUnit("C");
    unit_F = getUnit("F");
    unit_K = getUnit("K");
    unit_m = getUnit("m");
    unit_mm = getUnit("mm");
    unit_kph = getUnit("kph");    // km/h
    unit_mps = getUnit("m/s");
    unit_rpm = getUnit("rpm");
    unit_Pa = getUnit("Pa");
    unit_Pa_s = getUnit("Pa-s");
    /*    CUSTOM UNITS      */
    int[] massList = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] massFlowList = {1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] pressureList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] velList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] viscList = {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] volFlowList = {0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    /*    MASS UNITS [M]    */
    unit_g = addUnit("g", "gram", 0.001, massList);
    /*    MASS FLOW UNITS [M/T]    */
    unit_kgph = addUnit("kg/h", "kilogram per hour", 1/3600, massFlowList);
    unit_kgpmin = addUnit("kg/min", "kilogram per minute", 1/60, massFlowList);
    unit_gpmin = addUnit("g/min", "gram per minute", 1E-3/60, massFlowList);
    unit_gps = addUnit("g/s", "gram per second", 1E-3, massFlowList);
    /*    PRESSURE UNITS [P]    */
    //--- http://www.sensorsone.co.uk/pressure-units-conversion.html
    unit_cmH2O = addUnit("cmH2O", "cm of water", 98.0665, pressureList);
    unit_dynepcm2 = addUnit("dyne/cm^2", "dyne per square centimeter", 0.1, pressureList);
    unit_mmH2O = addUnit("mmH2O", "mm of water", 9.80665, pressureList);
    /*    VISCOSITY UNITS [P*T]    */
    unit_P = addUnit("P", "Poise", 1E-1, viscList);
    unit_cP = addUnit("cP", "centiPoise", 1E-3, viscList);
    /*    VOLUMETRIC FLOW UNITS [V/T]    */
    unit_lph = addUnit("l/h", "liter per hour", 1E-3/3600, volFlowList);
    unit_lpmin = addUnit("l/min", "liter per minute", 1E-3/60, volFlowList);
    unit_lps = addUnit("l/s", "liter per second", 1E-3, volFlowList);
    //- Assigning default units
    defUnitTemp = unit_C;
    defUnitLength = unit_mm;
    defUnitPress = unit_Pa;
    sayOK();
  }

  public void updateMeshContinuaVector(){
    printAction("Querying number of Mesh Continuas");
    vecMeshContinua = sim.getContinuumManager().getObjectsOf(MeshContinuum.class);
    say("Found: " + vecMeshContinua.size());
    if(vecMeshContinua.size() > 0){
        for(int i = 0; i < vecMeshContinua.size(); i++){
            say("  " + vecMeshContinua.get(i).getPresentationName());
        }
    }
  }

  public void updatePhysicsGravityAndReferenceTemperature(PhysicsContinuum physCont){
    printAction("Updating Gravity Vector and Referente Temperature");
    sayPhysicsContinua(physCont);
    say("  Gravity: " + gravity.toString());
    say("  Refence Temperature: " + Tref + defUnitTemp.getPresentationName());
    physCont.getReferenceValues().get(Gravity.class).setComponents(gravity[0], gravity[1], gravity[2]);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setUnits(defUnitTemp);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setValue(Tref);
    sayOK();
  }

  public void updatePhysicsReferencePressure(PhysicsContinuum physCont){
    printAction("Updating Referente Pressure");
    sayPhysicsContinua(physCont);
    say("  Refence Pressure: " + Pref + defUnitPress.getPresentationName());
    physCont.getReferenceValues().get(ReferencePressure.class).setUnits(defUnitPress);
    physCont.getReferenceValues().get(ReferencePressure.class).setValue(Pref);
    sayOK();
  }

  public void updateSolverSettings(){
    printAction("Setting Solver Settings");
    printSolverSettings();

    setSimMaxIterationStoppingCriteria(maxIter);

    SegregatedFlowSolver flowSolv = ((SegregatedFlowSolver) sim.getSolverManager().getSolver(SegregatedFlowSolver.class));
    flowSolv.getVelocitySolver().setUrf(urfVel);
    PressureSolver pSolv = flowSolv.getPressureSolver();
    pSolv.setUrf(urfP);

    SegregatedEnergySolver enrgySolv = null;
    try{
        enrgySolv = ((SegregatedEnergySolver) sim.getSolverManager().getSolver(SegregatedEnergySolver.class));
        enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.NO_RAMP);
        enrgySolv.setFluidUrf(urfFluidEnrgy);
        enrgySolv.setSolidUrf(urfSolidEnrgy);
    } catch (Exception e){}

    try{
        KeTurbSolver keSolv = ((KeTurbSolver) sim.getSolverManager().getSolver(KeTurbSolver.class));
        keSolv.setUrf(urfKEps);
    } catch (Exception e){}

    if(!rampURF) { return; }
    enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
    enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
    LinearRampCalculator rampFl = ((LinearRampCalculator) enrgySolv.getFluidRampCalculatorManager().getCalculator());
    LinearRampCalculator rampSld = ((LinearRampCalculator) enrgySolv.getSolidRampCalculatorManager().getCalculator());

    rampFl.setStartIteration(urfRampFlIterBeg);
    rampFl.setEndIteration(urfRampFlIterEnd);
    rampFl.setInitialRampValue(urfRampFlBeg);
    rampSld.setStartIteration(urfRampSldIterBeg);
    rampSld.setEndIteration(urfRampSldIterEnd);
    rampSld.setInitialRampValue(urfRampSldBeg);
  }

}
