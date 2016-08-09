/*
 * MACRO Utils v1c
 * Programmer: Fabio Kasper
 * Revision: Jul 19, 2012.
 *
 * Macro Utils is a set of useful methods to assist the process of writing macros in STAR-CCM+.
 *
 * Some methods might not be available in older versions. Started coding this Macro in v7.02.
 *
 * HOW TO USE IT?
 *
 * 1) Store MacroUtils_v?? in a subfolder called 'macro';
 *  E.g.: C:\work\macro\MacroUtils_v1a.java
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
import star.resurfacer.*;
import star.segregatedenergy.*;
import star.segregatedflow.*;
import star.solidmesher.*;
import star.surfacewrapper.*;
import star.trimmer.*;
import star.turbulence.*;
import star.vis.*;

public class MacroUtils extends StarMacro {
  /***************************************************
   * Global definitions
   ***************************************************/
  boolean colorByRegion = true;
  boolean fineTesselationOnImport = false;
  boolean saveIntermediates = true;
  boolean verbose = true;
  Boundary bdry = null;
  Boundary bdry1 = null;
  Boundary bdry2 = null;
  Boundary bdry3 = null;
  CadPart cadPrt = null;
  CellSurfacePart cellSet = null;
  CompositePart compPart = null;
  CompositePart compPart1 = null;
  CompositePart compPart2 = null;
  CompositePart compPart3 = null;
  DirectBoundaryInterface intrfPair = null;
  double mshSharpEdgeAngle = 30;
  double[] camFocalPoint = {0., 0., 0.};
  double[] camPosition = {0., 0., 0.};
  double[] camViewUp = {0., 0., 0.};
  double[] coord1 = {0., 0., 0.};
  double[] coord2 = {0., 0., 0.};
  double[] point = {0., 0., 0.};
  File cadPath = null;
  File dbsPath = null;
  File myFile = null;
  File simFile = null;
  GeometryPart geomPrt = null;
  int autoSaveMaxFiles = 2;
  int autoSaveFrequencyIter = 1000;
  int colourByPart = 4;
  int colourByRegion = 2;
  int partColouring = colourByPart;
  int picResX = 800;
  int picResY = 600;
  int savedWithSuffix = 0;
  Interface intrf = null;
  LeafMeshPart leafMshPrt = null;
  LeafMeshPart leafMshPrt1 = null;
  LeafMeshPart leafMshPrt2 = null;
  LeafMeshPart leafMshPrt3 = null;
  MeshContinuum mshCont = null;
  MeshContinuum mshCont1 = null;
  MeshContinuum mshCont2 = null;
  PartSurface partSrf = null;
  PlaneSection plane = null;
  PlaneSection plane1 = null;
  PlaneSection plane2 = null;
  PhysicsContinuum physCont = null;
  PhysicsContinuum physCont1 = null;
  PhysicsContinuum physCont2 = null;
  PhysicsContinuum physCont3 = null;
  Region region = null;
  Region region1 = null;
  Region region2 = null;
  Region region3 = null;
  ReportMonitor repMon = null;
  ReportMonitor repMon1 = null;
  ReportMonitor repMon2 = null;
  ReportMonitor repMon3 = null;
  Scene scene = null;
  Scene scene1 = null;
  Scene scene2 = null;
  SimpleCylinderPart simpleCylPrt = null;
  SimpleCylinderPart simpleCylPrt1 = null;
  SimpleCylinderPart simpleCylPrt2 = null;
  SimpleCylinderPart simpleCylPrt3 = null;
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
  String string = "";
  String string1 = "";
  String string2 = "";
  String string3 = "";
  String text = "";
  String wall = "wall";
  String walls = "walls";
  Simulation sim = null;
  Units unit_C = null;
  Units unit_cmH2O = null;
  Units unit_mmH2O = null;
  Units unit_g = null;
  Units unit_gpmin = null;                          // See definitions on initialize()
  Units unit_gps = null;
  Units unit_kgph = null;
  Units unit_kph = null;
  Units unit_kgpmin = null;
  Units unit_lph = null;
  Units unit_lpmin = null;
  Units unit_lps = null;
  Units unit_m = null;
  Units unit_mm = null;
  Units unit_mps = null;
  Units unit_Pa = null;
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
  /***************************************************
   * Preprocessing and Meshing
   ***************************************************/
  boolean thinMeshIsPolyType = true;
  double featCurveMeshMin = 10;
  double featCurveMeshTgt = 50;
  double mshBaseSize = 3.0;                     // mm
  double mshGrowthFactor = 1.25;
  double mshOpsTol = 1e-4;                      // m
  double mshProximityPointsInGap = 2.0;
  double mshProximitySearchFloor = 0.0;         // mm
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
  boolean rampURF = true;
  int maxIter = 10000;
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
  Vector<Boundary> vecBdry = new Vector<Boundary>();
  Vector<GeometryPart> vecGeomPrt = new Vector<GeometryPart>();
  Vector<MeshContinuum> vecMeshContinua = new Vector<MeshContinuum>();
  Vector<PhysicsContinuum> vecPhysicsContinua = new Vector<PhysicsContinuum>();
  Vector vecObj = new Vector();
  Vector objVec = new Vector();
  MeshContinuum mshContPoly = null;
  MeshContinuum mshContTrimmer = null;
  FilenameFilter dbsFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
        // Return files ending with dbs
        return name.matches(".*dbs");
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
    simName = simFile.getName();
    try {
        simTitle = simName.substring(0, simName.lastIndexOf("."));
    } catch (Exception e) {
        simTitle = simName;
    }
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
     * Units section
     */
    unit_C = ((Units) sim.getUnitsManager().getObject("C"));
    unit_m = ((Units) sim.getUnitsManager().getObject("m"));
    unit_mm = ((Units) sim.getUnitsManager().getObject("mm"));
    unit_kph = ((Units) sim.getUnitsManager().getObject("kph"));    // km/h
    unit_mps = ((Units) sim.getUnitsManager().getObject("m/s"));
    unit_rpm = ((Units) sim.getUnitsManager().getObject("rpm"));
    unit_Pa = ((Units) sim.getUnitsManager().getObject("rpm"));
    /*    CUSTOM UNITS      */
    int[] massList = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] massFlowList = {1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] pressureList = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] volFlowList = {0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    /*    MASS UNITS [M]    */
    unit_g = addUnit("g", "gram", 0.001, massList);
    /*    MASS FLOW UNITS [M/T]    */
    unit_kgph = addUnit("kg/h", "kilogram per hour", 1/3600, massFlowList);
    unit_kgpmin = addUnit("kg/min", "kilogram per minute", 1/60, massFlowList);
    unit_gpmin = addUnit("g/min", "gram per minute", 1E-3/60, massFlowList);
    unit_gps = addUnit("g/s", "gram per second", 1E-3, massFlowList);
    /*    VOLUMETRIC FLOW UNITS [V/T]    */
    unit_lph = addUnit("l/h", "liter per hour", 1E-3/3600, volFlowList);
    unit_lpmin = addUnit("l/min", "liter per minute", 1E-3/60, volFlowList);
    unit_lps = addUnit("l/s", "liter per second", 1E-3, volFlowList);
    /*    PRESSURE UNITS [P]    */
    //--- http://www.sensorsone.co.uk/pressure-units-conversion.html
    addUnit("cmH2O", "cm of water", 98.0665, pressureList);
    addUnit("mmH2O", "mm of water", 9.80665, pressureList);
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

  public PhysicsContinuum _chPhys_SegrFlTemp(PhysicsContinuum physCont) {
    physCont.enable(SegregatedFluidTemperatureModel.class);
    physCont.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    physCont.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    StaticTemperatureProfile stp = physCont.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(fluidT0);
    return physCont;
  }

  public PhysicsContinuum _chPhys_SegrFlBoussinesq(PhysicsContinuum physCont, double thermalExpansion) {
    physCont.enable(GravityModel.class);
    physCont.enable(BoussinesqModel.class);
    SingleComponentGasModel sgm = physCont.getModelManager().getModel(SingleComponentGasModel.class);
    Gas gas = ((Gas) sgm.getMaterial());
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ThermalExpansionProperty.class).getMethod());
    cmpm.getQuantity().setValue(thermalExpansion);
    physCont.getReferenceValues().get(Gravity.class).setComponents(gravity[0], gravity[1], gravity[2]);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setValue(fluidT0);
    return physCont;
  }

  public PhysicsContinuum _chPhys_TurbKEps2Lyr(PhysicsContinuum physCont) {
    try{
        physCont.disableModel(physCont.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    physCont.enable(TurbulentModel.class);
    physCont.enable(RansTurbulenceModel.class);
    physCont.enable(KEpsilonTurbulence.class);
    physCont.enable(RkeTwoLayerTurbModel.class);
    physCont.enable(KeTwoLayerAllYplusWallTreatment.class);
    TurbulentVelocityScaleProfile tvs = physCont.getInitialConditions().get(TurbulentVelocityScaleProfile.class);
    tvs.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvs0);
    TurbulenceIntensityProfile tip = physCont.getInitialConditions().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti0);
    TurbulentViscosityRatioProfile tvrp = physCont.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr0);
    return physCont;
  }

  public MeshContinuum _crMsh_Poly(){
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNamePoly);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(DualMesherModel.class);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    mshCont.getReferenceValues().get(BaseSize.class).setUnits(unit_mm);
    mshCont.getReferenceValues().get(BaseSize.class).setValue(mshBaseSize);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    mshCont.getReferenceValues().get(VolumeMeshDensity.class).setGrowthFactor(mshGrowthFactor);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContPoly = mshCont;
    return mshCont;
  }

  public MeshContinuum _crMsh_ThinMesher() {
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNameThinMesher);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(SolidMesherModel.class);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    mshCont.getModelManager().getModel(SolidMesherModel.class).setOptimize(true);
    mshCont.getReferenceValues().get(BaseSize.class).setUnits(unit_mm);
    mshCont.getReferenceValues().get(BaseSize.class).setValue(mshBaseSize);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    mshCont.getReferenceValues().get(ThinSolidLayers.class).setLayers(thinMeshLayers);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContPoly = mshCont;
    return mshCont;
  }

  public MeshContinuum _crMsh_Trimmer() {
    printAction("Creating Mesh Continua");
    mshCont = sim.getContinuumManager().createContinuum(MeshContinuum.class);
    mshCont.setPresentationName(contNameTrimmer);
    mshCont.enable(ResurfacerMeshingModel.class);
    mshCont.enable(TrimmerMeshingModel.class);
    mshCont.getReferenceValues().get(BaseSize.class).setUnits(unit_mm);
    mshCont.getReferenceValues().get(BaseSize.class).setValue(mshBaseSize);
    mshCont.getReferenceValues().get(MaximumCellSize.class).getRelativeSize().setPercentage(mshTrimmerMaxCelSize);
    setMeshSurfaceSizes(mshCont, mshSrfSizeMin, mshSrfSizeTgt);
    int i = getTrimmerGrowthRate(mshTrimmerGrowthRate);
    mshCont.getReferenceValues().get(SimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(i);

    printMeshParameters();
    vecMeshContinua.add(mshCont);
    mshContTrimmer = mshCont;
    setMeshPerRegionFlag(mshCont);
    return mshCont;
  }

  public PhysicsContinuum _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(Class singleComponentClass, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum physCont = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    physCont.enable(ThreeDimensionalModel.class);
    physCont.enable(SteadyModel.class);
    physCont.enable(singleComponentClass);
    physCont.enable(SegregatedFlowModel.class);
    physCont.enable(ConstantDensityModel.class);
    physCont.enable(LaminarModel.class);
    physCont.enable(CellQualityRemediationModel.class);
    InitialPressureProfile ipp = physCont.getInitialConditions().get(InitialPressureProfile.class);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(p0);
    VelocityProfile vp = physCont.getInitialConditions().get(VelocityProfile.class);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(v0[0], v0[1], v0[2]);
    return physCont;
  }

  public PhysicsContinuum _crPhys_3D_SS_SegrSLD(String solidMaterial, String text) {
    printAction("Creating Physics Continua");
    say(text);
    PhysicsContinuum sldCont = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    sldCont.enable(ThreeDimensionalModel.class);
    sldCont.enable(SteadyModel.class);
    sldCont.enable(SolidModel.class);
    sldCont.enable(SegregatedSolidEnergyModel.class);
    sldCont.enable(ConstantDensityModel.class);
    sldCont.enable(CellQualityRemediationModel.class);
    sldCont.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(unit_C);
    sldCont.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    sldCont.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(unit_C);
    sldCont.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    StaticTemperatureProfile stp = sldCont.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(solidsT0);
    if(solidMaterial.matches(noneString)){ return sldCont; }
    SolidModel sm = sldCont.getModelManager().getModel(SolidModel.class);
    Solid solid = ((Solid) sm.getMaterial());
    MaterialDataBase matDB = sim.get(MaterialDataBaseManager.class).getMaterialDataBase("props");
    DataBaseMaterialManager dbMatMngr = matDB.getFolder("Solids");
    DataBaseSolid dbSld = ((DataBaseSolid) dbMatMngr.getMaterial(solidMaterial));
    Solid newSolid = (Solid) sm.replaceMaterial(solid, dbSld);
    return sldCont;
  }

  public Units addUnit(String name, String desc, double conversion, int[] dimensionList) {
    UserUnits newUnit = getActiveSimulation().getUnitsManager().createUnits("Units");
    newUnit.setPresentationName(name);
    newUnit.setDescription(desc);
    newUnit.setConversion(conversion);
    newUnit.setDimensionsVector(new IntVector(dimensionList));
    return newUnit;
  }

//  public void assignAllGeometryPartsToRegions(){
//    printAction("Assigning all the Geometry Parts to Regions");
//    Collection<GeometryPart> allParts = getAllGeometryParts();
//    sim.getRegionManager().newRegionsFromParts(allParts, "OneRegionPerPart",
//            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
//  }
//
//  public void assignAllCadPartsToRegions(){
//    printAction("Assigning all Cad Parts to Regions");
//    NeoObjectVector neoObjVec = new NeoObjectVector(getAllLeafPartsAsCadParts().toArray());
//    sim.getRegionManager().newRegionsFromParts(neoObjVec, "OneRegionPerPart",
//            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
//  }

  public void assignAllLeafPartsToRegions(){
    printAction("Assigning all Leaf Parts to Regions");
    sim.getRegionManager().newRegionsFromParts(getAllLeafParts(), "OneRegionPerPart",
            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
  }

  public void assignAllLeafPartsToOneSingleRegion(){
    printAction("Assigning all Leaf Parts to a Single Region");
    sim.getRegionManager().newRegionsFromParts(getAllLeafParts(), "OneRegion",
            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
    region = getRegion("Region 1");
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

  public void assignRegionToMeshContinua(Region region, MeshContinuum mshCont){
    printAction("Assigning a Region to a Mesh Continua");
    say("Region: " + region.getPresentationName());
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.add(region);
  }

  public void assignRegionToPhysicsContinua(Region region, PhysicsContinuum physCont){
    printAction("Assigning a Region to a Physics Continua");
    say("Region: " + region.getPresentationName());
    say("Physics Continua: " + physCont.getPresentationName());
    physCont.add(region);
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
    if(1==1){ return; }  /** STILL WORKING ON THIS **/
    printAction("Cleaning Up Simulation file to save space");
    say("\nDisabling Mesh Continua in All Regions");
    for(Region region : getAllRegions()){
        MeshContinuum mshCont = region.getMeshContinuum();
        if(mshCont == null){ continue; }
        sim.getContinuumManager().eraseFromContinuum(new NeoObjectVector(new Object[] {region}), mshCont);
    }
    say("\nClearing Surface Mesh Representations");
    clearMeshes();
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
    } catch (Exception e1) {
        say("Remeshed Surface Representation not found.");
    }
    try{
        sim.getRepresentationManager().removeObjects(queryWrappedSurface());
    } catch (Exception e1) {
        say("Wrapped Surface Representation not found.");
    }
    try{
        sim.getRepresentationManager().removeObjects(queryInitialSurface());
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
    say("Region: " + region.getPresentationName());
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
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(vecB);
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString());
    sayOK();
  }

  public DirectBoundaryInterface createDirectInterfacePairByName(String regexPatt){
    printAction("Creating Direct Interface Pair by matching names in any regions");
    DirectBoundaryInterface intrf = null;
    Vector<Boundary> vecBdry = (Vector) getAllBoundariesByName(regexPatt);
    Boundary bdry1 = null;
    Boundary bdry2 = null;
    Region region = null;
    if(vecBdry.size() == 2){
        say("Found 2 candidates. Interfacing:");
    } else if(vecBdry.size() > 2){
        say("Found more than 2 candidates. Interfacing the first two:");
    } else if(vecBdry.size() < 1) {
        say("Could not find 2 candidates. Giving up...");
        return null;
    }
    bdry1 = vecBdry.get(0);
    bdry2 = vecBdry.get(1);
    say("  Side 1: " + getStringBoundaryAndRegion(bdry1));
    say("  Side 2: " + getStringBoundaryAndRegion(bdry2));
    intrf = sim.getInterfaceManager().createDirectInterface(bdry1, bdry2, "In-place");
    sayOK();
    return intrf;
  }

  public void createMeshContinua_EmbeddedThinMesher(){
    mshCont = _crMsh_Poly();
    enableEmbeddedThinMesher(mshCont);
    sayOK();
  }

  public void createMeshContinua_PolyOnly(){
    mshCont = _crMsh_Poly();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
  }

  public void createMeshContinua_ThinMesher() {
    mshCont = _crMsh_ThinMesher();
    sayOK();
  }

  public void createMeshContinua_Trimmer() {
    mshCont = _crMsh_Trimmer();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
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
    printAction("Creating a Volumetric Control in Mesh Continua: ");
    say("Continua: " + mshCont.getPresentationName());
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

    return repMon;
  }

  public void createPhysics_AirSteadySegregatedBoussinesqKEps2Lyr() {
    text = "Air / Steady State / Segregated Solver / Boussinesq / k-eps 2 Layer";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_SegrFlTemp(physCont);
    _chPhys_SegrFlBoussinesq(physCont, 0.00335);
    _chPhys_TurbKEps2Lyr(physCont);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_AirSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_AirSteadySegregatedIncompressibleLaminarThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    _chPhys_SegrFlTemp(physCont);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_AluminumSteadySegregated() {
    text = "Aluminum / Steady State / Segregated Solver";
    physCont = _crPhys_3D_SS_SegrSLD(noneString, text);
    vecPhysicsContinua.add(physCont);
  }

  public void createPhysics_SteelSteadySegregated() {
    text = "Steel / Steady State / Segregated Solver";
    physCont = _crPhys_3D_SS_SegrSLD("UNSG101000_Solid", text);
    vecPhysicsContinua.add(physCont);
  }

  public void createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Isothermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_TurbKEps2Lyr(physCont);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Thermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_TurbKEps2Lyr(physCont);
    _chPhys_SegrFlTemp(physCont);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_WaterSteadySegregatedIncompressibleLaminarThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    physCont = _crPhys_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    _chPhys_SegrFlTemp(physCont);
    vecPhysicsContinua.add(physCont);
    sayOK();
  }

  public void createPhysics_AirSteadySegregatedBoussinesqKEps2Lyr_BAK() {
    printAction("Creating Physics Continua");
    say("Air / Steady State / Segregated Solver / Boussinesq Thermal / k-epsilon 2 Layers");
    PhysicsContinuum physCont = sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
    physCont.setPresentationName(contNameAirBoussinesq);
    physCont.enable(ThreeDimensionalModel.class);
    physCont.enable(SteadyModel.class);
    physCont.enable(SingleComponentGasModel.class);
    physCont.enable(SegregatedFlowModel.class);
    physCont.enable(ConstantDensityModel.class);
    physCont.enable(TurbulentModel.class);
    physCont.enable(RansTurbulenceModel.class);
    physCont.enable(KEpsilonTurbulence.class);
    physCont.enable(RkeTwoLayerTurbModel.class);
    physCont.enable(KeTwoLayerAllYplusWallTreatment.class);
    physCont.enable(GravityModel.class);
    physCont.enable(SegregatedFluidTemperatureModel.class);
    physCont.enable(BoussinesqModel.class);
    physCont.enable(CellQualityRemediationModel.class);
    SingleComponentGasModel sgm = physCont.getModelManager().getModel(SingleComponentGasModel.class);
    Gas gas = ((Gas) sgm.getMaterial());
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ThermalExpansionProperty.class).getMethod());
    cmpm.getQuantity().setValue(0.00335);
    physCont.getReferenceValues().get(Gravity.class).setComponents(gravity[0], gravity[1], gravity[2]);
    physCont.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    physCont.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setUnits(unit_C);
    physCont.getReferenceValues().get(ReferenceTemperature.class).setValue(fluidT0);
    StaticTemperatureProfile stp = physCont.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(fluidT0);
    TurbulenceIntensityProfile tip = physCont.getInitialConditions().get(TurbulenceIntensityProfile.class);
    TurbulentViscosityRatioProfile tvrp = physCont.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti0);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr0);
    VelocityProfile vp = physCont.getInitialConditions().get(VelocityProfile.class);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(v0[0], v0[1], v0[2]);
    sayOK();
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
    PrimitiveFieldFunction ff = getPrimitiveFieldFunction(var);
    if(var.equalsIgnoreCase("VelMag") || var.equalsIgnoreCase("Vel Mag")){
        scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionMagnitude(ff));
    } else if(var.equalsIgnoreCase("VelX") || var.equalsIgnoreCase("Vel X")){
        scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 0));
    } else if(var.equalsIgnoreCase("VelY") || var.equalsIgnoreCase("Vel Y")){
        scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 1));
    } else if(var.equalsIgnoreCase("VelZ") || var.equalsIgnoreCase("Vel Z")){
        scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 2));
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
    say("Region: " + region.getPresentationName());
    say("Value: " + T + unit.toString());
    region.getConditions().get(InitialConditionOption.class).setSelected(InitialConditionOption.REGION);
    StaticTemperatureProfile temp = region.get(RegionInitialConditionManager.class).get(StaticTemperatureProfile.class);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit);
    temp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    sayOK();
  }

  public void customizeThinMesherForRegion(Region region, int numLayers){
    printAction("Customizing a Region Thin Mesher Parameter");
    say("Region: " + region.getPresentationName());
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

  public void disableFeatureCurveSizeOnRegion(Region region){
    printAction("Disabling Custom Feature Curve Mesh Size");
    say("Region: " + region.getPresentationName());
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

  public void disablePrismsOnBoundary(Boundary bdry){
    say("Disable Prism Layers on Boundary: " + getStringBoundaryAndRegion(bdry));
    try {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not disable Prism in Boundary.");
    }
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

  public void disableSurfaceRemesher(MeshContinuum mshCont){
    printAction("Disabling Surface Remesher");
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.disableModel(mshCont.getModelManager().getModel(ResurfacerMeshingModel.class));
    sayOK();
  }

  public void disableSurfaceWrapper(MeshContinuum mshCont){
    printAction("Disabling Surface Wrapper");
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.disableModel(mshCont.getModelManager().getModel(SurfaceWrapperMeshingModel.class));
    sayOK();
  }

  public void disableThinMesherOnRegion(Region region){
    printAction("Disabling Thin Mesher on Region");
    int opt = SolidMesherRegionOption.DISABLE;
    say("Region: " + region.getPresentationName());
    try{
        region.get(MeshConditionManager.class).get(SolidMesherRegionOption.class).setSelected(opt);
        sayOK();
    } catch (Exception e){
        say("ERROR! Moving on.\n");
    }
  }

  public void enableEmbeddedThinMesher(MeshContinuum mshCont){
    printAction("Enabling Embedded Thin Mesher");
    say("Mesh Continua: " + mshCont.getPresentationName());
    say("Embedded Thin Mesher overview:");
    mshCont.enable(SolidMesherSubModel.class);
    //mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAutomaticSurfaceRepair(false);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
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
  }

  public void enablePrismLayers(MeshContinuum mshCont){
    /*
     * This method will assume Prism Layers only on Fluid Regions
     */
    printAction("Enabling Prism Layers");
    say("Mesh Continua: " + mshCont.getPresentationName());
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
        if(!region.isMeshing()){
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
    say("\n");
  }

  public void enableSurfaceProximityRefinement(MeshContinuum mshCont){
    printAction("Enabling Surface Proximity Refinement");
    say("Mesh Continua: " + mshCont.getPresentationName());
    say("Proximity settings overview: ");
    say("  Proximity Number of Points in Gap: " + mshProximityPointsInGap);
    say("  Proximity Search Floor (mm): " + mshProximitySearchFloor);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(true);
    SurfaceProximity sp = mshCont.getReferenceValues().get(SurfaceProximity.class);
    sp.setNumPointsInGap(mshProximityPointsInGap);
    sp.getFloor().setUnits(unit_mm);
    sp.getFloor().setValue(mshProximitySearchFloor);
    sayOK();
  }

  public void enableSurfaceRemesher(MeshContinuum mshCont){
    printAction("Enabling Surface Remesher");
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.enable(ResurfacerMeshingModel.class);
    sayOK();
  }

  public void enableSurfaceWrapper(MeshContinuum mshCont){
    printAction("Enabling Surface Wrapper");
    say("Mesh Continua: " + mshCont.getPresentationName());
    say("Surface Wrapper settings overview: ");
    say("  Geometric Feature Angle (deg): " + mshWrapperFeatureAngle);
    say("  Wrapper Scale Factor (%): " + mshWrapperScaleFactor);
    mshCont.enable(SurfaceWrapperMeshingModel.class);
    mshCont.getReferenceValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(mshWrapperFeatureAngle);
    mshCont.getReferenceValues().get(SurfaceWrapperScaleFactor.class).setScaleFactor(mshWrapperScaleFactor);
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

  public void genSurfaceMesh() {
    printAction("Generating Surface Mesh");
    sim.get(MeshPipelineController.class).generateSurfaceMesh();
    createMeshScene().setPresentationName("Surface Mesh");
    // Checking Mesh Quality right after generated
    checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions();
  }

  public void genVolumeMesh() {
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
    say("Region: " + region.getPresentationName());
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
    CompositePart comPrt0 = null;
    Vector<CompositePart> compPrtCol = new Vector<CompositePart>();
    for(GeometryPart gp : getAllGeometryParts()){
        String partName = gp.getPresentationName();
        //say("Part Name: " + partName);
        try { comPrt0 = ((CompositePart) gp); }
        catch (Exception e0) { continue; }
        Vector<CompositePart> vecCompPart = new Vector<CompositePart>();
        for (CompositePart cp : getCompositeChildren(comPrt0, vecCompPart)){
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

  public Collection<PartSurface> getAllPartSurfacesFromLeafParts(){
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    for(GeometryPart gp : getAllLeafParts()){
        vecPS.addAll(gp.getPartSurfaces());
    }
    return vecPS;
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
    CompositePart childPrt = null;
    Collection<GeometryPart> childPrts = compPrt.getChildParts().getParts();
    for(GeometryPart gp : childPrts){
        //String gpName = gp.getPresentationName();
        try { childPrt = ((CompositePart) gp); }
        catch (Exception e) { continue; }
        //say("Child Part: " + childPrt.getPresentationName());
        vecCP.add(childPrt);
        getCompositeChildren(childPrt, vecCP);
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

  public Collection<FeatureCurve> getFeatureCurvesFromRegion(Region region){
    return region.getFeatureCurveManager().getFeatureCurves();
  }

  public VectorMagnitudeFieldFunction getFieldFuncionComponent(PrimitiveFieldFunction ff, int i){
    return (VectorMagnitudeFieldFunction) ff.getComponentFunction(i);
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

  public PartSurface getPartSurfaceFromGeometryPart(GeometryPart gp, String name){
    for(PartSurface ps : gp.getPartSurfaces()){
        if(ps.getPresentationName().equals(name)){
            return ps;
        }
    }
    return null;
  }

  public PrimitiveFieldFunction getPrimitiveFieldFunction(String var){
      return ((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction(var));
  }

  public PhysicsContinuum getPhysicsContinuaByName(String name){
      return ((PhysicsContinuum) sim.getContinuumManager().getContinuum(name));
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

  public boolean hasPolyMesh(Region region){
    if(hasDeletedCells(region)){ return false; }
    if(isPoly(region.getMeshContinuum())){
        sayV("Region: " + region.getPresentationName());
        sayV(" Has Poly mesh.\n");
        return true;
    }
    return false;
  }

  public boolean hasTrimmerMesh(Region region){
    if(hasDeletedCells(region)){ return false; }
    if(isTrimmer(region.getMeshContinuum())){
        sayV("Region: " + region.getPresentationName());
        sayV(" Has Trimmer mesh.\n");
        return true;
    }
    return false;
  }

  public boolean hasValidVolumeMesh(){
    if(queryVolumeMesh() == null){
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
//    NeoObjectVector vec = (NeoObjectVector) getAllLeafPartsAsMeshParts();
//    NeoObjectVector vec = new NeoObjectVector(getAllLeafPartsAsMeshParts().toArray());
//    sim.get(MeshActionManager.class).imprintCadParts(vec, "CAD");
    sim.get(MeshActionManager.class).imprintCadParts(getAllLeafPartsAsMeshParts(), "CAD");
//    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafPartsAsCadParts(), "CAD", 1.0E-4);
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

  public boolean isFluid(Region region){
    if(region.getRegionType().toString().equals("Fluid Region")){
        return true;
    }
    return false;
  }

  public boolean isFluidSolidInterface(DirectBoundaryInterface intrfPair){
    Region reg0 = intrfPair.getRegion0();
    Region reg1 = intrfPair.getRegion1();
    if(isFluid(reg0) && isSolid(reg1)) { return true; }
    if(isFluid(reg1) && isSolid(reg0)) { return true; }
    return false;
  }

  public boolean isInterface(Boundary bdry){
    String bdryType = bdry.getBeanDisplayName();
    String i1 = "DirectBoundaryInterfaceBoundary";
    String i2 = "IndirectBoundaryInterfaceBoundary";
    if(bdryType.equals(i1) || bdryType.equals(i2)){ return true; }
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

  public SurfaceRep queryInitialSurface(){
    try{
        return ((SurfaceRep) sim.getRepresentationManager().getObject("Initial Surface"));
    } catch (Exception e){
        return null;
    }
  }

//  public Collection<MeshContinuum> queryMeshContinuas(){
//    Vector<MeshContinuum> vecMC = new Vector<MeshContinuum>();
//    for(Continuum cont : sim.getContinuumManager().getObjects()){
//        say(cont.getAsStringArg());
//        say(cont.getBeanDisplayName());
//        say(cont.getEnabledModels().toString());
//    }
//    return null;
//  }

  public SurfaceRep queryRemeshedSurface(){
    try{
        return ((SurfaceRep) sim.getRepresentationManager().getObject("Remeshed Surface"));
    } catch (Exception e){
        return null;
    }
  }

  public SurfaceRep queryWrappedSurface(){
    try{
        return ((SurfaceRep) sim.getRepresentationManager().getObject("Wrapped Surface"));
    } catch (Exception e){
        return null;
    }
  }

  public FvRepresentation queryVolumeMesh(){
    try{
        return ((FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh"));
    } catch (Exception e){
        return null;
    }
  }

  public void printAction(String text) {
    if(sim == null){
        sim = getActiveSimulation();
    }
    sim.println("");
    printLine();
    sim.println("+ " + getTime());
    sim.println("+ " + text);
    printLine();
  }

  public void printLine(){
    sim.println("+-----------------------------------------------------------------------------");
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
    setVerboseOn();
    if (remPrtVec.isEmpty()) {
        say(String.format("No Composite Part was found with REGEX criteria: \"%s\"", regexPatt));
        return;
    }
    for(CompositePart cP : remPrtVec){
        try{
            CompositePart parent = ((CompositePart) cP.getParentPart());
            say("Removing Composite: " + cP.getPresentationName());
            parent.getChildParts().remove(cP);
        } catch (Exception e){ }
    }
  }

  public void removeInvalidCells(){
    Vector<Region> regionsPoly = new Vector<Region>();
    Vector<Region> regionsTrimmer = new Vector<Region>();
    setVerboseOff();
    for(Region region : getAllRegions()){
        if(hasPolyMesh(region)){ regionsPoly.add(region); }
        if(hasTrimmerMesh(region)){ regionsTrimmer.add(region); }
    }
    setVerboseOn();
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
    //sim.get(SimulationPartManager.class).removeParts(new NeoObjectVector(new Object[] {lmp}));
  }

  public void removeLeafMeshPartsByName(String regexPatt){
    printAction(String.format("Remove Leaf Meshs Part by REGEX pattern: \"%s\"", regexPatt));
    Collection<LeafMeshPart> colLMP = getAllLeafMeshPartsByName(regexPatt);
    say("Leaf Mesh Parts to be removed: " + colLMP.size());
    sim.get(SimulationPartManager.class).removeParts(colLMP);
    say("Removed");
    say("");
  }

  public void removeLeafPartsByName(String regexPatt){
    printAction(String.format("Remove Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    Collection<GeometryPart> colLP = getAllLeafPartsByName(regexPatt);
    say("Leaf Parts to be removed: " + colLP.size());
    sim.get(SimulationPartManager.class).removeObjects(colLP);
    //sim.get(SimulationPartManager.class).removeParts(colLP);
    say("Removed");
    say("");
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
    say("Region: "+ region.getPresentationName());
    getBoundaryInRegion(region, bdryName).setPresentationName(bdryNewName);
    sayOldNameNewName(bdryName, bdryNewName);
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

  public void sayA(String msg){
    // Say Auxiliary
    sim.println(sayPreffixString + "[aux] " + msg);
  }

  public void sayOK(){
    say("OK!\n");
  }

  public void sayOldNameNewName(String name, String newName){
    say("  Old name: " + name);
    say("  New name: " + newName);
    say("");
  }

  public void sayV(String msg){
    if(verbose) { sayA(msg); }
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
    String name = bdry.getPresentationName();
    printAction("Setting BC as Stagnation Inlet: " + name);
    bdry.setBoundaryType(StagnationBoundary.class);
    TotalTemperatureProfile ttp = bdry.getValues().get(TotalTemperatureProfile.class);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_ConvectionWall(Boundary bdry, double T, double htc){
    String name = bdry.getPresentationName();
    printAction("Setting BC as Convection Wall: " + name);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.CONVECTION);
    AmbientTemperatureProfile atp = bdry.getValues().get(AmbientTemperatureProfile.class);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    HeatTransferCoefficientProfile htcp = bdry.getValues().get(HeatTransferCoefficientProfile.class);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(htc);
    sayOK();
  }

  public void setBC_FreeSlipWall(Boundary bdry){
    String name = bdry.getPresentationName();
    printAction("Setting BC as a Free Slip Wall: " + name);
    bdry.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.SLIP);
    sayOK();
  }

  public void setBC_PressureOutlet(Boundary bdry, double T, double ti, double tvr){
    String name = bdry.getPresentationName();
    printAction("Setting BC as Pressure Outlet: " + name);
    bdry.setBoundaryType(PressureBoundary.class);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_VelocityInlet(Boundary bdry, double T, double vel, double ti, double tvr){
    String name = bdry.getPresentationName();
    printAction("Setting BC as Velocity Inlet: " + name);
    bdry.setBoundaryType(InletBoundary.class);
    VelocityMagnitudeProfile vmp = bdry.getValues().get(VelocityMagnitudeProfile.class);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(vel);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  public void setBC_TI_and_TVR(Boundary bdry, double ti, double tvr){
    TurbulenceIntensityProfile tip = bdry.getValues().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti);
    TurbulentViscosityRatioProfile tvrp = bdry.getValues().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr);
  }

  public void setBC_StaticTemperature(Boundary bdry, double T){
    StaticTemperatureProfile stp = bdry.getValues().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(unit_C);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
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
        say("Region: " + region.getPresentationName());
        Collection<FeatureCurve> colFC = region.getFeatureCurveManager().getFeatureCurves();
        for(FeatureCurve featCurve : colFC) {
            setMeshFeatureCurveSizes(featCurve, min, tgt);
        }
    }
  }

  public void setMeshBoundaryPrismSizes(Boundary bdry, int numLayers, double stretch, double relSize) {
    say("Custom Boundary Prism Layer: " + bdry.getPresentationName());
    say("  Number of Layers: " + numLayers);
    say("  Stretch Factor: " + String.format("%.2f",stretch));
    say("  Height Relative Size: " + String.format("%.2f%%", relSize));
    try{
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        bdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        bdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        PrismThickness prismThick = bdry.get(MeshValueManager.class).get(PrismThickness.class);
        ((GenericRelativeSize) prismThick.getRelativeSize()).setPercentage(relSize);
        sayOK();
    } catch (Exception e1) {
        say("ERROR! Please review settings. Skipping this Boundary.");
        say(e1.getMessage() + "\n");
    }
  }

  public void setMeshBoundarySurfaceSizes(Boundary bdry, double min, double tgt){
    say("Custom Surface Mesh Size: " + bdry.getPresentationName());
    say("  " + getMinTargetString(min, tgt));
    try {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = bdry.get(MeshValueManager.class).get(SurfaceSize.class);
        srfSize.getRelativeMinimumSize().setPercentage(min);
        srfSize.getRelativeTargetSize().setPercentage(tgt);
        sayOK();
    } catch (Exception e1) {
        say("Warning! Could not set as Boundary. Trying to set in the Interface Parent");
        try{
            DirectBoundaryInterfaceBoundary intrf = (DirectBoundaryInterfaceBoundary) bdry;
            DirectBoundaryInterface intrfP = intrf.getDirectBoundaryInterface();
            say("Interface: " + intrfP.getPresentationName());
            setMeshDirectBoundaryInterfaceSurfaceSizes(intrfP, min, tgt);
        } catch (Exception e2) {
            say("ERROR! Please review settings. Skipping this Boundary.");
            say(e2.getMessage() + "\n");
        }
    }
  }

  public void setMeshDirectBoundaryInterfaceSurfaceSizes(DirectBoundaryInterface intrf, double min, double tgt){
    printAction("Custom Surface Mesh Size for Interface: " + intrf.getPresentationName());
    say("Interfacing Regions: ");
    say("  " + intrf.getRegion0().getPresentationName());
    say("  " + intrf.getRegion1().getPresentationName());
    say("");
    say("  " + getMinTargetString(min, tgt));
    say("");
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
    say("  " + getMinTargetString(min, tgt));
    try {
        fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = fc.get(MeshValueManager.class).get(SurfaceSize.class);
        srfSize.getRelativeMinimumSize().setPercentage(min);
        srfSize.getRelativeTargetSize().setPercentage(tgt);
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
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.setMeshRegionByRegion(true);
    sayOK();
  }

  public void setMeshSurfaceGrowthRate(Boundary bdry, int growthRate){
    /*
     * This method only works with Trimmer. Options:
        SurfaceGrowthRateOption.VERYSLOW;
        SurfaceGrowthRateOption.SLOW;
        SurfaceGrowthRateOption.MEDIUM
        SurfaceGrowthRateOption.FAST
     */
    printAction("Setting Custom Surface Growth on Trimmer Mesh");
    say("Boundary: " + bdry.getPresentationName());
    bdry.get(MeshConditionManager.class).get(CustomSurfaceGrowthRateOption.class).setEnabled(true);
    bdry.get(MeshValueManager.class).get(CustomSimpleSurfaceGrowthRate.class).getSurfaceGrowthRateOption().setSelected(growthRate);
    sayOK();
  }

  public void setMeshSurfaceSizes(MeshContinuum mshCont, double min, double tgt){
    printAction("Setting Mesh Continua Surface Sizes");
    say("Mesh Continua: " + mshCont.getPresentationName());
    SurfaceSize srfSize = mshCont.getReferenceValues().get(SurfaceSize.class);
    srfSize.getRelativeMinimumSize().setPercentage(min);
    srfSize.getRelativeTargetSize().setPercentage(tgt);
    say("  " + getMinTargetString(min, tgt));
    sayOK();
  }

  public void setSolverSettings(){
    printAction("Setting Solver Settings");
    printSolverSettings();

    ((StepStoppingCriterion)
        sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps")).setMaximumNumberSteps(maxIter);

    SegregatedFlowSolver flowSolv = ((SegregatedFlowSolver) sim.getSolverManager().getSolver(SegregatedFlowSolver.class));
    PressureSolver pSolv = flowSolv.getPressureSolver();
    SegregatedEnergySolver enrgySolv = ((SegregatedEnergySolver) sim.getSolverManager().getSolver(SegregatedEnergySolver.class));
    KeTurbSolver keSolv = ((KeTurbSolver) sim.getSolverManager().getSolver(KeTurbSolver.class));

    flowSolv.getVelocitySolver().setUrf(urfVel);
    pSolv.setUrf(urfP);
    enrgySolv.setFluidUrf(urfFluidEnrgy);
    enrgySolv.setSolidUrf(urfSolidEnrgy);
    keSolv.setUrf(urfKEps);

    if(!rampURF) { return; }
    enrgySolv.getFluidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
    LinearRampCalculator rampFl = ((LinearRampCalculator) enrgySolv.getFluidRampCalculatorManager().getCalculator());

    rampFl.setStartIteration(urfRampFlIterBeg);
    rampFl.setEndIteration(urfRampFlIterEnd);
    rampFl.setInitialRampValue(urfRampFlBeg);

    enrgySolv.getSolidRampCalculatorManager().getRampCalculatorOption().setSelected(RampCalculatorOption.LINEAR_RAMP);
    LinearRampCalculator rampSld = ((LinearRampCalculator) enrgySolv.getSolidRampCalculatorManager().getCalculator());

    rampSld.setStartIteration(urfRampSldIterBeg);
    rampSld.setEndIteration(urfRampSldIterEnd);
    rampSld.setInitialRampValue(urfRampSldBeg);

  }

  public void setVerboseOff(){
    verbose = false;
  }

  public void setVerboseOn(){
    verbose = true;
  }

  public void splitNonContiguousPartSurfacesByName(String regexPatt){
    printAction("Splitting Non Contiguous Part Surfaces");
//    sim.get(PartSurfaceManager.class).splitNonContiguousPartSurfaces((Vector) getAllPartSurfacesByName(regexPatt));
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    for(PartSurface ps : getAllPartSurfacesByName(regexPatt)){
        vecPS.clear();
        vecPS.add(ps);
        CadPart cadPart = (CadPart) ps.getPart();
        cadPart.getPartSurfaceManager().splitNonContiguousPartSurfaces(vecPS);
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
    say("Mesh Continua: " + mshCont.getPresentationName());
    mshCont.setMeshRegionByRegion(false);
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

}
