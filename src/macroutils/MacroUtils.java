package macroutils;

//import java.awt.*;            // Conflict with List.
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

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

/**
 * <b>Macro Utils</b> is a set of useful methods to assist the process of writing macros in
 * STAR-CCM+. Some methods might not be available in older versions. Started coding this Macro
 * in STAR-CCM+ v7.02.<p>
 *
 * <b>How to use it?</b>
 * <ol>
 * <li> Store MacroUtils in a subfolder called <u>macro</u>:<p>
 * - E.g.: <u>C:\work\macro\MacroUtils.java</u>
 * <p>
 * <li> In STAR-CCM+, go to <i>Menu -> Tools -> Options -> Environment</i>:<p>
 * - Under <i>User Macro classpath</i> put <u>C:\work</u><p>
 * <p>
 * - Alternatively, launch STAR-CCM+ in the command line as:<p>
 * > <u>starccm+ -classpath "C:\work"</u><p>
 *
 * <li> In another macro, just reference MacroUtils, to benefit from its methods: E.g:<p>
 * <pre><code>
 *  package macro;
 *  public class MyMacro extends MacroUtils {
 *      public void execute() {
 *          _initUtils();
 *          genVolumeMesh();
 *          removeInvalidCells();
 *          runCase();
 *          _finalize();
 *      }
 * } </code></pre></ol>
 * @since STAR-CCM+ v7.02
 * @author Fabio Kasper
 * @version 2.0 Nov 23, 2012
 */
public class MacroUtils extends StarMacro {

  /**
   * Initialize Macro Utils. This method is <b>mandatory</b>.
   */
  public void execute() { }

  /**
   * Initialize Macro Utils. This method is <b>mandatory</b>.
   */
  public void _initUtils() {
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
    saySimOverview();
    if (colorByRegion){
        partColouring = colourByRegion;
    }
    updateMeshContinuaVector();
    updateOrCreateNewUnits();
  }

  /**
   * Finalize Macro Utils. This method is <i>optional</i>.
   */
  public void _finalize(){
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

  /**
   * Creates a custom Unit.
   *
   * @param name given Unit name.
   * @param desc given Unit description.
   * @param conversion given Unit conversion factor.
   * @param dimensionList array of Dimension (<i>use a custom Macro to help it here</i>).
   * @return The created Unit.
   */
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

  @Deprecated
  public void assignAllLeafPartsToRegions(){
    printAction("Assigning all Leaf Parts to Regions");
    sim.getRegionManager().newRegionsFromParts(getAllLeafParts(), "OneRegionPerPart",
            null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve", null, true);
  }

  @Deprecated
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

  /**
   * Assigns all Parts to a Region.
   *
   * @return Created Region.
   */
  public Region assignAllPartsToRegion() {
    printAction("Assigning All Parts to a Region");
    return assignPartsToRegions(getAllLeafParts(false), false, true, false).iterator().next();
  }

  /**
   * Assigns the given Parts to a Region.
   *
   * @param colGP given Geometry Parts.
   * @return Created Region.
   */
  public Region assignAllPartsToRegion(Collection<GeometryPart> colGP) {
    return assignPartsToRegions(colGP, false, true, true).iterator().next();
  }

  /**
   * Assigns all Parts to Regions using <u>one Boundary per Part Surface</u>.
   *
   * @return Collection of created Regions.
   */
  public Collection<Region> assignAllPartsToRegions() {
    printAction("Assigning All Parts to Regions");
    return assignPartsToRegions(getAllLeafParts(false), false, false, false);
  }

  /**
   * Assigns all Parts to Regions.
   *
   * @param singleBoundary One Boundary per Part Surface?
   * @return Collection of created Regions.
   */
  public Collection<Region> assignAllPartsToRegions(boolean singleBoundary) {
    printAction("Assigning All Parts to Regions");
    return assignPartsToRegions(getAllLeafParts(false), singleBoundary, false, false);
  }

  /**
   * Assigns the given Parts to Regions.
   *
   * @param colGP given Geometry Parts.
   * @param singleBoundary One Boundary per Part Surface?
   * @return Collection of created Regions.
   */
  public Collection<Region> assignAllPartsToRegions(Collection<GeometryPart> colGP, boolean singleBoundary) {
    return assignPartsToRegions(colGP, singleBoundary, false, true);
  }

  @Deprecated
  public void assignLeafMeshPartToRegion(LeafMeshPart lmp){
    printAction("Assigning '" + lmp.getPresentationName() + "' to a Region");
    sim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[] {lmp}),
        "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null, "OneFeatureCurve",
        null, false);
  }

  @Deprecated
  public void assignLeafMeshPartToRegionByName(String lmpName){
    LeafMeshPart lmp = ((LeafMeshPart) sim.get(SimulationPartManager.class).getPart(lmpName));
    assignLeafMeshPartToRegion(lmp);
  }

  @Deprecated
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

  /**
   * Assigns the given Part to a Region.
   *
   * @param gp given Geometry Part.
   * @return Created Region.
   */
  public Region assignPartToRegion(GeometryPart gp) {
    return (Region) assignPartsToRegions(new NeoObjectVector(new Object[] {gp}), false, true, true).iterator().next();
  }

  private Collection<Region> assignPartsToRegions(Collection<GeometryPart> colGP,
                            boolean singleBoundary, boolean singleRegion, boolean verboseOption){
    String bdryMode, regionMode;
    if (singleRegion) {
        regionMode = "OneRegion";
        printAction("Assigning Parts to a Single Region", verboseOption);
    } else {
        regionMode = "OneRegionPerPart";
        printAction("Assigning Parts to Different Regions", verboseOption);
    }
    if (singleBoundary) {
        bdryMode = "OneBoundary";
    } else {
        bdryMode = "OneBoundaryPerPartSurface";
    }
    say("Boundary Mode: " + bdryMode);
    say("Number of Parts: " + colGP.size());
    if (singleRegion && colGP.size() == 1) {
        regionMode = "OneRegionPerPart";
    }
    for (GeometryPart gp : colGP) {
        say("  " + gp.getPathInHierarchy());
    }
    Vector<Region> vecCurrentRegions = (Vector) getAllRegions(false);
    RegionManager regMngr = sim.getRegionManager();
    regMngr.newRegionsFromParts(colGP, regionMode, bdryMode, "OneFeatureCurve", true);
    Vector<Region> vecCreatedRegions = (Vector) getAllRegions(false);
    vecCreatedRegions.removeAll(vecCurrentRegions);
    say("Regions created: " + vecCreatedRegions.size(), verboseOption);
    return vecCreatedRegions;
  }

  @Deprecated
  public void assignRegionToMeshContinua(Region region, MeshContinuum mshCont){
    enableMeshContinua(mshCont, region);
  }

  @Deprecated
  public void assignRegionToPhysicsContinua(Region region, PhysicsContinuum physCont){
    enablePhysicsContinua(physCont, region);
  }

  private PhysicsContinuum changePhysics_SegrFlTemp(PhysicsContinuum phC) {
    phC.enable(SegregatedFluidTemperatureModel.class);
    phC.getReferenceValues().get(MinimumAllowableTemperature.class).setUnits(defUnitTemp);
    phC.getReferenceValues().get(MinimumAllowableTemperature.class).setValue(clipMinT);
    phC.getReferenceValues().get(MaximumAllowableTemperature.class).setUnits(defUnitTemp);
    phC.getReferenceValues().get(MaximumAllowableTemperature.class).setValue(clipMaxT);
    setInitialCondition_T(phC, fluidT0, false);
    return phC;
  }

  private PhysicsContinuum changePhysics_SegrFlBoussinesq(PhysicsContinuum phC, double thermalExpansion) {
    phC.enable(GravityModel.class);
    phC.enable(BoussinesqModel.class);
    SingleComponentGasModel sgm = phC.getModelManager().getModel(SingleComponentGasModel.class);
    Gas gas = ((Gas) sgm.getMaterial());
    ConstantMaterialPropertyMethod cmpm = ((ConstantMaterialPropertyMethod) gas.getMaterialProperties().getMaterialProperty(ThermalExpansionProperty.class).getMethod());
    cmpm.getQuantity().setValue(thermalExpansion);
    updatePhysicsGravityAndReferenceTemperature(phC);
    return phC;
  }

  private PhysicsContinuum changePhysics_TurbKEps2Lyr(PhysicsContinuum phC) {
    try{
        phC.disableModel(phC.getModelManager().getModel(LaminarModel.class));
    } catch (Exception e) {}
    phC.enable(TurbulentModel.class);
    phC.enable(RansTurbulenceModel.class);
    phC.enable(KEpsilonTurbulence.class);
    phC.enable(RkeTwoLayerTurbModel.class);
    phC.enable(KeTwoLayerAllYplusWallTreatment.class);
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, false);
    return phC;
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

  /* WORK LATER ON THIS */
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

    if (freeEdg > 0) {
        say("****************************************************");
        say("**                   WARNING!!!                   **");
        say("**                                                **");
        say("**               FREE EDGES FOUND!                **");
        say("****************************************************");
        if (freeEdg < 100 && autoCloseFreeEdges) {
            say("Attempting to auto-fill holes.");
            diagCtrl.selectFreeEdges();
            repairCtrl.holeFillSelectedEdges();
            say("Rerunning Diagnostics...");
            diagCtrl.runDiagnostics();
            freeEdg = diagCtrl.getNumFreeEdges();
            nonManEdg = diagCtrl.getNumNonmanifoldEdges();
            nonManVert = diagCtrl.getNumNonmanifoldVertices();
            say("Number of Free Edges: " + retString(freeEdg));
            say("Number of Non-Manifold Edges: " + retString(nonManEdg));
            say("Number of Non-Manifold Vertices: " + retString(nonManVert));
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

  /**
   * This method cleans everything but the Volume Mesh and the FV Regions. It is useful for saving
   * hard drive space.
   */
  public void cleanUpSimulationFile(){
    /**************************************************
     * STILL WORKING ON THIS
     **************************************************/
    printAction("Cleaning Up Simulation file to save space");
    say("Disabling Mesh Continua in All Regions...");
    for (Region region : getAllRegions(false)) {
        MeshContinuum mshCont = region.getMeshContinuum();
        if (mshCont == null) { continue; }
        mshCont.erase(region);
        //sim.getContinuumManager().eraseFromContinuum(new NeoObjectVector(new Object[] {region}), mshCont);
    }
    clearMeshes();
    clearParts();       // WATCH OUT ON THIS. NEEDS IMPROVEMENT.
  }

  /**
   * This method clears the following:
   * <ol>
   * <li> All Scenes
   * <li> All Mesh Representations
   * <li> All Interfaces
   * <li> All Regions
   * <li> All Parts
   * </ol>
   * It is useful when creating simulation templates.
   */
  public void clearAllMeshesRegionsAndParts() {
    clearScenes();
    clearMeshes();
    clearInterfaces();
    clearRegions();
    clearParts();
  }

  /**
   * Removes all Interfaces of the model.
   */
  public void clearInterfaces() {
    clearInterfaces(true);
  }

  private void clearInterfaces(boolean verboseOption) {
    printAction("Removing all Interfaces", verboseOption);
    Collection<Interface> colIntrf = sim.getInterfaceManager().getObjects();
    if(colIntrf.isEmpty()) {
        say("No Interfaces found.", verboseOption);
        return;
    }
    say("Removing " + colIntrf.size() + " Interface(s)");
    sim.getInterfaceManager().deleteInterfaces((Vector) colIntrf);
    sayOK(verboseOption);
  }

  /**
   * Removes all Mesh Representations of the model.
   */
  public void clearMeshes() {
    printAction("Removing all Mesh Representations");
    try {
        sim.getRepresentationManager().removeObjects(queryRemeshedSurface());
        say("Remeshed Surface Representation removed.");
    } catch (Exception e1) {
        say("Remeshed Surface Representation not found.");
    }
    try {
        sim.getRepresentationManager().removeObjects(queryWrappedSurface());
        say("Wrapped Surface Representation removed.");
    } catch (Exception e1) {
        say("Wrapped Surface Representation not found.");
    }
    try {
        sim.getRepresentationManager().removeObjects(queryInitialSurface());
        say("Initial Surface Representation removed.");
    } catch (Exception e1) {
        say("ERROR removing Initial Surface Representation.");
    }
  }

  /**
   * Removes all Regions of the model.
   */
  public void clearRegions() {
    printAction("Removing all Regions");
    Collection<Region> colReg = getAllRegions();
    if(colReg.isEmpty()) {
        say("No Regions found.");
        return;
    }
    say("Removing " + colReg.size() + " Region(s)...");
    sim.getRegionManager().removeRegions((Vector) colReg);
    sayOK();
  }

  /**
   * Removes all Geometry Parts of the model.
   */
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

  /**
   * Removes all Scenes of the model.
   */
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

  /**
   * Combine all boundaries from a Region.
   *
   * @param region given Region.
   * @return combined boundary.
   */
  public Boundary combineBoundaries(Region region){
    printAction("Combining All Boundaries from Region");
    sayRegion(region);
    return combineBoundaries((Vector) getAllBoundariesFromRegion(region), true);
  }

  /**
   * Combine several boundaries.
   *
   * @param vecBdry given Vector of Boundaries.
   * @return combined boundary.
   */
  public Boundary combineBoundaries(Vector<Boundary> vecBdry){
    printAction("Combining Boundaries");
    return combineBoundaries(vecBdry, true);
  }

  private Boundary combineBoundaries(Vector<Boundary> vecBdry, boolean verboseOption){
    say("Boundaries provided: " + vecBdry.size(), verboseOption);
    for (Boundary bdry : vecBdry) {
        sayBdry(bdry, true, false);
    }
    if (vecBdry.size() < 2) {
        say("Not enough boundaries to combine. Skipping...", verboseOption);
    } else {
        sim.getMeshManager().combineBoundaries(vecBdry);
        sayOK(verboseOption);
    }
    return vecBdry.firstElement();
  }

  /**
   * Combines Geometry Parts based on REGEX search criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @param combinePartSurfaces Combine all the Part Surfaces?
   * @return The combined Geometry Part.
   */
  public GeometryPart combineGeometryParts(String regexPatt, boolean combinePartSurfaces){
    return combineGeometryParts(regexPatt, combinePartSurfaces, true);
  }

  private GeometryPart combineGeometryParts(String regexPatt, boolean combinePS, boolean verboseOption) {
    printAction(String.format("Combining Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<GeometryPart> vecGP = (Vector) getAllLeafParts(regexPatt, false);
    for (int i = 0; i < vecGP.size(); i++) {
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecGP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts((MeshPart) vecGP.firstElement(), vecMP);
    cadPrt = (CadPart) vecGP.firstElement();
    say("Combined into: " + cadPrt.getPathInHierarchy(), verboseOption);
    if (combinePS) {
        say("Combining Part Surfaces..", verboseOption);
        Collection<PartSurface> colPS = cadPrt.getPartSurfaces();
        int n = colPS.size();
        cadPrt.combinePartSurfaces(colPS);
        String name = cadPrt.getPartSurfaces().iterator().next().getPresentationName();
        say("Combined " + n + " Part Surfaces into: " + name);
    }
    sayOK(verboseOption);
    return cadPrt;
  }

  /**
   * Combines Leaf Mesh Parts based on REGEX search criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The combined Leaf Mesh Part.
   */
  public LeafMeshPart combineLeafMeshPartsByName(String regexPatt) {
    return combineLeafMeshPartsByName(regexPatt, true);
  }

  private LeafMeshPart combineLeafMeshPartsByName(String regexPatt, boolean verboseOption) {
    printAction(String.format("Combining Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    Vector<LeafMeshPart> vecLMP = (Vector) getAllLeafMeshParts(regexPatt, false);
    for (int i = 0; i < vecLMP.size(); i++) {
        if(i==0) { continue; }
        vecMP.add((MeshPart) vecLMP.get(i));
    }
    MeshPartFactory meshPartFactory = sim.get(MeshPartFactory.class);
    meshPartFactory.combineMeshParts(vecLMP.firstElement(), vecMP);
    LeafMeshPart lmp = vecLMP.firstElement();
    say("Combined into: " + lmp.getPathInHierarchy(), verboseOption);
    sayOK(verboseOption);
    return lmp;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(Collection<PartSurface> colPS, String renameTo){
    PartSurface ps = combinePartSurfaces(colPS, true);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine several Part Surfaces.
   *
   * @param colPS given Part Surfaces.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(Collection<PartSurface> colPS){
    return combinePartSurfaces(colPS, true);
  }

  /**
   * Combine all Part Surfaces from a given Leaf Mesh Part.
   * Rename the combined Part Surface.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(LeafMeshPart leafMshPart, String renameTo) {
    PartSurface ps = combinePartSurfaces(leafMshPart);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine all Part Surfaces from a given Leaf Mesh Part.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfaces(LeafMeshPart leafMshPart) {
    printAction("Getting all Part Surfaces");
    say("Leaf Mesh Part: " + leafMshPart.getPresentationName());
    Collection<PartSurface> colPS = leafMshPart.getPartSurfaceManager().getPartSurfaces();
    return combinePartSurfaces(colPS, true);
  }

  /**
   * Combine all Part Surfaces searched by REGEX from a given Leaf Mesh Part.
   * Rename the combined Part Surface.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param regexPatt REGEX search pattern.
   * @param renameTo combined Part Surface new name.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfacesByName(LeafMeshPart leafMshPart, String regexPatt, String renameTo) {
    PartSurface ps = combinePartSurfacesByName(leafMshPart, regexPatt);
    ps.setPresentationName(renameTo);
    return ps;
  }

  /**
   * Combine all Part Surfaces searched by REGEX from a given Leaf Mesh Part.
   *
   * @param leafMshPart given Leaf Mesh Part.
   * @param regexPatt REGEX search pattern.
   * @return combined Part Surface.
   */
  public PartSurface combinePartSurfacesByName(LeafMeshPart leafMshPart, String regexPatt) {
    printAction(String.format("Getting Part Surfaces by REGEX pattern: \"%s\"", regexPatt));
    say("Leaf Mesh Part: " + leafMshPart.getPresentationName());
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    for (PartSurface ps : leafMshPart.getPartSurfaceManager().getPartSurfaces()) {
        String name = ps.getPresentationName();
        if (name.matches(regexPatt)) {
            say("  Found: " + name);
            vecPS.add(ps);
        }
    }
    return combinePartSurfaces(vecPS, true);
  }

  private PartSurface combinePartSurfaces(Collection<PartSurface> colPS, boolean verboseOption){
    printAction("Combining Part Surfaces", verboseOption);
    String myPS = "___myPartSurface";
    String name = colPS.iterator().next().getPresentationName();
    say("Part Surfaces available: " + colPS.size(), verboseOption);
    if (colPS.size() == 1) {
        say("Nothing to combine.", verboseOption);
        return colPS.iterator().next();
    }
    for (PartSurface ps : colPS) {
        ps.setPresentationName(myPS);
    }
    //-- Combine faces
    GeometryPart gp = colPS.iterator().next().getPart();
    if (isCadPart(gp)) {
        ((CadPart) gp).combinePartSurfaces(colPS);
    } else if (isBlockPart(gp)) {
        ((SimpleBlockPart) gp).combinePartSurfaces(colPS);
    } else if (isSimpleCylinderPart(gp)) {
        ((SimpleCylinderPart) gp).combinePartSurfaces(colPS);
    } else if (isLeafMeshPart(gp)) {
        ((LeafMeshPart) gp).combinePartSurfaces(colPS);
    }
    //-- Reloop to make sure it finds the correct combined Part Surface
    colPS = gp.getPartSurfaces();
    PartSurface foundPS = null;
    for (PartSurface ps : colPS) {
        //say(ps.getPresentationName());
        if (ps.getPresentationName().startsWith(myPS)) {
            //say("Found: " + ps.getPresentationName());
            foundPS = ps;
            break;
        }
    }
    say("Combined into: " + foundPS.getPresentationName(), verboseOption);
    sayOK(verboseOption);
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

  /**
   * Creates an One Group Contact Prevention in a Region with all its Boundaries. Excluding Interfaces.
   *
   * @param region given Region.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPreventionBetweenAllBoundaries(Region region, double value, Units unit){
    Vector<Boundary> vecBdries = (Vector<Boundary>) getAllBoundariesFromRegion(region, false, true);
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region with given Boundaries.
   *
   * @param region given Region.
   * @param vecBdries given Vector of Boundaries.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPreventionBetweenBoundaries(Region region, Vector<Boundary> vecBdries, double value, Units unit){
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  /**
   * Creates an One Group Contact Prevention in a Region, based on an Array of REGEX Patterns.
   *
   * @param region given Region.
   * @param regexPattArray given array of REGEX search patterns.
   * @param value given Search Floor value.
   * @param unit given Search Floor unit.
   */
  public void createContactPreventionBetweenBoundariesByName(Region region, String[] regexPattArray, double value, Units unit) {
    Vector<Boundary> vecBdries = (Vector<Boundary>) getAllBoundaries(region, regexPattArray, false, true);
    createContactPrevention(region, vecBdries, value, unit, true);
  }

  private OneGroupContactPreventionSet createContactPrevention(Region region,
                    Vector<Boundary> vecBdries, double value, Units unit, boolean verboseOption){
    printAction("Creating a Contact Prevention between boundaries", verboseOption);
    if(vecBdries.size() < 2){
        say("Input boundaries: " + vecBdries.size(), verboseOption);
        say("ERROR! Input boundaries number MUST > 2. Skipping...", verboseOption);
        return null;
    }
    OneGroupContactPreventionSet cps = region.get(MeshValueManager.class).get(ContactPreventionSetManager.class).createOneGroupContactPreventionSet();
    cps.getBoundaryGroup().setObjects(vecBdries);
    cps.getFloor().setUnits(unit);
    cps.getFloor().setValue(value);
    say("Search Floor: " + value + unit.toString(), verboseOption);
    sayOK(verboseOption);
    return cps;
  }

  /**
   * Creates a Direct Interface Pair.
   *
   * @param bdry1 given Boundary 1.
   * @param bdry2 given Boundary 2.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(Boundary bdry1, Boundary bdry2){
    return createDirectInterfacePair(bdry1, bdry2, true);
  }

  /**
   * Creates a Direct Interface Pair given a REGEX search pattern.  <p>
   * If it finds more than 2 boundaries, gets the first 2.
   *
   * @param regexPatt given REGEX search pattern.
   * @return The created Interface pair.
   */
  public DirectBoundaryInterface createDirectInterfacePair(String regexPatt){
    printAction("Creating Direct Interface Pair by matching names in any regions");
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(regexPatt, false, true);
    if(vecBdry.size() == 2){
        say("Found 2 candidates. Interfacing:");
    } else if(vecBdry.size() > 2){
        say("Found more than 2 candidates. Interfacing the first two:");
    } else if(vecBdry.size() < 1) {
        say("Could not find 2 candidates. Giving up...");
        return null;
    }
    DirectBoundaryInterface intrfPair = createDirectInterfacePair(vecBdry.get(0), vecBdry.get(1), false);
    sayInterface(intrfPair);
    sayOK();
    return intrfPair;
  }

  private DirectBoundaryInterface createDirectInterfacePair(Boundary b1, Boundary b2, boolean verboseOption) {
    printAction("Creating a Direct Interface Pair", verboseOption);
    DirectBoundaryInterface intrfPair = sim.getInterfaceManager().createDirectInterface(b1, b2, "In-place");
    intrfPair.getTopology().setSelected(InterfaceConfigurationOption.IN_PLACE);
    sayInterface(intrfPair, verboseOption, false);
    sayOK(verboseOption);
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

  @Deprecated
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

  /**
   * Creates a Continua with Poly + Embedded Thin Meshers. <p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>OFF</b>
   * <li> Prisms Layers are <b>OFF</b>
   * </ul>
   * @return The new Mesh Continua.
   */
  public MeshContinuum createMeshContinua_EmbeddedThinMesher(){
    mshCont = createMeshContinuaPoly();
    enableEmbeddedThinMesher(mshCont);
    sayOK();
    return mshCont;
  }

  /**
   * Creates a Continua with Poly Mesher.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>ON</b>
   * <li> Prisms Layers are <b>ON</b>
   * </ul>
   * @return The new Mesh Continua.
   */
  public MeshContinuum createMeshContinua_PolyOnly(){
    mshCont = createMeshContinuaPoly();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
    return mshCont;
  }

  /**
   * Creates a Continua with the <b>PURE</b> Poly Thin Mesher. It is intended for
   * pure conducting solids. Please see Documentation.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>OFF</b>
   * </ul>
   * @return The new Mesh Continua.
   */
  public MeshContinuum createMeshContinua_ThinMesher() {
    mshCont = createMeshContinuaThinMesher();
    sayOK();
    return mshCont;
  }

  /**
   * Creates a Continua with the Trimmer Mesher.<p>
   * Note: <ul>
   * <li> Surface Proximity for the Surface Remesher is <b>ON</b>
   * <li> Prisms Layers are <b>ON</b>
   * </ul>
   * @return The new Mesh Continua.
   */
  public MeshContinuum createMeshContinua_Trimmer() {
    mshCont = createMeshContinuaTrimmer();
    enableSurfaceProximityRefinement(mshCont);
    enablePrismLayers(mshCont);
    sayOK();
    return mshCont;
  }

  private MeshContinuum createMeshContinuaPoly(){
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

  private MeshContinuum createMeshContinuaThinMesher() {
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

  private MeshContinuum createMeshContinuaTrimmer() {
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

  @Deprecated
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

  /**
   * Creates the Monitor and a Plot from a Report.
   *
   * @param rep given Report.
   * @param repName given Report name.
   * @param xAxisLabel given X-axis label.
   * @param yAxisLabel given Y-axis label.
   * @return The created Report Monitor.
   */
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
    monPlot = monPl;
    return repMon;
  }
  @Deprecated
  public MonitorPlot createPlotFromVectorRepMon(String plotName){
    MonitorPlot monPl = createPlot(vecRepMon, plotName);
    vecRepMon.clear();  // Clear Vector after flushing
    return monPl;
  }

  /**
   * Creates a Single Plot from the selected Report Monitors.
   *
   * @param colRP given Collection of Report Monitors.
   * @param plotName given Plot name.
   * @return The created Monitor Plot.
   */
  public MonitorPlot createPlot(Collection<ReportMonitor> colRP, String plotName){
    MonitorPlot monPl = sim.getPlotManager().createMonitorPlot();
    monPl.setPresentationName(plotName);
    monPl.getMonitors().addObjects(colRP);

    Axes axes = monPl.getAxes();
    Axis xx = axes.getXAxis();
    xx.getTitle().setText(colRP.iterator().next().getXAxisName());
    Axis yy = axes.getYAxis();
    yy.getTitle().setText(colRP.iterator().next().getMonitorDescription());

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

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air Boussinesq (for Buoyancy)
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedBoussinesqKEps2Lyr() {
    text = "Air / Steady State / Segregated Solver / Boussinesq / k-eps 2 Layers";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    double thermalExpansionAir = 0.00335;
    changePhysics_SegrFlTemp(phC);
    changePhysics_SegrFlBoussinesq(phC, thermalExpansionAir);
    changePhysics_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / k-eps 2 Layers / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_SegrFlTemp(phC);
    changePhysics_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Air
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AirSteadySegregatedIncompressibleLaminarThermal() {
    text = "Air / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentGasModel.class, text);
    changePhysics_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density Aluminum
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_AluminumSteadySegregated() {
    text = "Aluminum / Steady State / Segregated Solver";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD(noneString, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @param name given Solid name (E.g.: "mySteel").
   * @param den given Density in default Units.
   * @param k given Thermal Conductivity in default Units.
   * @param cp given Specific Heat in default Units.
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_SolidSteadySegregated(String name, double den, double k, double cp) {
    text = "Generic Solid / Steady State / Segregated Solver / Constant Properties";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD(noneString, text);
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

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Solid Steady State
   * <li> Constant Density Steel (material library: UNSG101000)
   * <li> Segregated Solver
   * <li> Thermal Conducting Solid
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_SteelSteadySegregated() {
    text = "Steel / Steady State / Segregated Solver";
    PhysicsContinuum phC = createPhysics_3D_SS_SegrSLD("UNSG101000_Solid", text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Turbulent k-epsilon 2 Layers
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleKEps2LyrThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / k-eps 2 Layer / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_SegrFlTemp(phC);
    changePhysics_TurbKEps2Lyr(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Isothermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarIsothermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Isothermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  /**
   * Creates a Physics Continua. It includes: <ul>
   * <li> 3D Fluid Steady State
   * <li> Incompressible Water
   * <li> Segregated Solver
   * <li> Laminar
   * <li> Thermal
   * </ul>
   *
   * @return The New Physics Continua.
   */
  public PhysicsContinuum createPhysics_WaterSteadySegregatedIncompressibleLaminarThermal() {
    text = "Water / Steady State / Segregated Solver / Constant Density / Laminar / Thermal";
    PhysicsContinuum phC = createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(SingleComponentLiquidModel.class, text);
    changePhysics_SegrFlTemp(phC);
    vecPhysicsContinua.add(phC);
    sayOK();
    return phC;
  }

  private PhysicsContinuum createPhysics_3D_SS_SngCmp_SegrFL_ConDen_Lam_CQR(
                                            Class singleComponentClass, String text) {
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
    setInitialCondition_P(phC, p0);
    setInitialCondition_Velocity(phC, v0[0], v0[1], v0[2], false);
    updateOrCreateNewUnits();
    return phC;
  }

  private PhysicsContinuum createPhysics_3D_SS_SegrSLD(String solidMaterial, String text) {
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
    setInitialCondition_T(sldCont, solidsT0, false);
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

  @Deprecated
  public void createRepMassAvgAndStopCriterion_StdDev(Region region, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    createReportMassAverage(region, reportNickname, var, unit);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  @Deprecated
  public void createRepMaxOnAllRegions(String reportNickname, String var, Units unit){
    createReportMaximum(getAllRegions(false), reportNickname, var, unit);
  }

  @Deprecated
  public void createRepMFlowAndStopCriterion_StdDev(Boundary bdry, String reportNickname,
                                                                double stdDev, int samples){
    createReportMassFlow(Arrays.asList(bdry), reportNickname, varP, unit_C);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  @Deprecated
  public void createRepMFlowAvgAndStopCriterion_StdDev(Boundary bdry, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    createReportMassFlowAverage(bdry, reportNickname, var, unit);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  @Deprecated
  public void createRepVolAvgAndStopCriterion_StdDev(Region region, String reportNickname,
                                        String var, Units unit, double stdDev, int samples){
    createReportVolumeAverage(region, reportNickname, var, unit);
    createStoppingCriteriaFromReportMonitor_StdDev(repMon, stdDev, samples);
  }

  @Deprecated
  public void createRepMinOnAllRegions(String reportNickname, String var, Units unit){
    createReportMinimum(getAllRegions(false), reportNickname, var, unit);
  }

  /**
   * Creates a Mass Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Region.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassAverage(Region region, String reportName, String var, Units unit) {
    createReportMassAverage(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Mass Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassAverage(Collection<Region> colReg, String reportName, String var, Units unit) {
    printAction("Creating a Mass Average Report of " + var + " on Regions");
    sayRegions(colReg);
    MassAverageReport massAvgRep = sim.getReportManager().createReport(MassAverageReport.class);
    massAvgRep.setScalar(getPrimitiveFieldFunction(var));
    massAvgRep.setUnits(unit);
    massAvgRep.setPresentationName(reportName);
    massAvgRep.getParts().setObjects(region);
    String yAxisLabel = "Mass Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(massAvgRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Mass Flow Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param bdry given Boundary.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlow(Boundary bdry, String reportName, String var, Units unit) {
    createReportMassFlow(Arrays.asList(new Boundary[] {bdry}), reportName, var, unit);
  }

  /**
   * Creates a Mass Flow Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlow(Collection<Boundary> colBdy, String reportName, String var, Units unit) {
    printAction("Creating a Mass Flow Report of " + var + " on Boundaries");
    sayBoundaries(colBdy);
    MassFlowReport mfRep = sim.getReportManager().createReport(MassFlowReport.class);
    mfRep.setPresentationName(reportName);
    mfRep.getParts().setObjects(bdry);
    String unitStr = mfRep.getUnits().getPresentationName();
    repMon = createMonitorAndPlotFromReport(mfRep, reportName, "Iteration", "Mass Flow (" + unitStr + ")");
  }

  /**
   * Creates a Mass Flow Average Report in a Boundary.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param bdry  given Boundary.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlowAverage(Boundary bdry, String reportName, String var, Units unit) {
    createReportMassFlowAverage(Arrays.asList(new Boundary[] {bdry}), reportName, var, unit);
  }

  /**
   * Creates a Mass Flow Average Report in the selected Boundaries.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colBdy given Collection of Boundaries.
   * @param reportName given Report name.
   * @param unit variable corresponding Unit.
   */
  public void createReportMassFlowAverage(Collection<Boundary> colBdy, String reportName, String var, Units unit) {
    printAction("Creating a Mass Flow Average Report of " + var + " on Boundaries");
    sayBoundaries(colBdy);
    MassFlowAverageReport mfaRep = sim.getReportManager().createReport(MassFlowAverageReport.class);
    mfaRep.setScalar(getPrimitiveFieldFunction(var));
    mfaRep.setUnits(unit);
    mfaRep.setPresentationName(reportName);
    mfaRep.getParts().setObjects(colBdy);
    String unitStr = unit.getPresentationName();
    String yAxisLabel = "Mass Flow Average of " + var + " (" + unitStr + ")";
    repMon = createMonitorAndPlotFromReport(mfaRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Maximum Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMaximum(Region region, String reportName, String var, Units unit) {
    createReportMaximum(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Maximum Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMaximum(Collection<Region> colReg, String reportName, String var, Units unit) {
    printAction("Creating a Maximum Report of " + var + " on Regions");
    sayRegions(colReg);
    MaxReport maxRep = sim.getReportManager().createReport(MaxReport.class);
    maxRep.setScalar(getPrimitiveFieldFunction(var));
    maxRep.setUnits(unit);
    maxRep.setPresentationName(reportName);
    maxRep.getParts().setObjects(colReg);
    String yAxisLabel = "Maximum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(maxRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Minimum Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMinimum(Region region, String reportName, String var, Units unit) {
    createReportMinimum(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Minimum Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportMinimum(Collection<Region> colReg, String reportName, String var, Units unit) {
    printAction("Creating a Minimum Report of " + var + " on Regions");
    MinReport minRep = sim.getReportManager().createReport(MinReport.class);
    minRep.setScalar(getPrimitiveFieldFunction(var));
    minRep.setUnits(unit);
    minRep.setPresentationName(reportName);
    minRep.getParts().setObjects(colReg);
    String yAxisLabel = "Minimum of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(minRep, reportName, "Iteration", yAxisLabel);
  }

  /**
   * Creates a Volume Average Report in a Region.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param region given Region.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportVolumeAverage(Region region, String reportName, String var, Units unit) {
    createReportVolumeAverage(Arrays.asList(new Region[] {region}), reportName, var, unit);
  }

  /**
   * Creates a Volume Average Report in the selected Regions.<p>
   * <b>Note that a Monitor and a Plot will be created too.</b> Even though this method will not
   * return anything, {@link #repMon} will still be updated.
   *
   * @param colReg given Collection of Regions.
   * @param reportName given Report name.
   * @param var given Variable.
   * @param unit variable corresponding Unit.
   */
  public void createReportVolumeAverage(Collection<Region> colReg, String reportName, String var, Units unit) {
    printAction("Creating a Volume Average Report of " + var + " on Regions");
    sayRegions(colReg);
    VolumeAverageReport volAvgRep = sim.getReportManager().createReport(VolumeAverageReport.class);
    volAvgRep.setScalar(getPrimitiveFieldFunction(var));
    volAvgRep.setUnits(unit);
    volAvgRep.setPresentationName(reportName);
    volAvgRep.getParts().setObjects(region);
    String yAxisLabel = "Volume Average of " + var + " (" + unit.getPresentationName() + ")";
    repMon = createMonitorAndPlotFromReport(volAvgRep, reportName, "Iteration", yAxisLabel);
  }

  public void createRotatingReferenceFrameForRegion(Region region, double[] axis, double[] origin, Units origUnit,
                                    double rotValue, Units rotUnit) {
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

  @Deprecated
  public Scene createScalarSceneWithObjects(Collection<NamedObject> objects, String var, Units unit,
                                            String sceneName, boolean smoothFilled) {
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

  /**
   * Creates a Geometry Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Geometry() {
    return createScene("Geometry", new Vector<NamedObject>(), true);
  }

  private Scene createScene_Geometry(boolean verboseOption) {
    return createScene("Geometry", new Vector<NamedObject>(), false);
  }

  /**
   * Creates a Geometry Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @return Created Scene.
   */
  public Scene createScene_Geometry(Collection<NamedObject> objects) {
    return createScene("Geometry", objects, true);
  }

  /**
   * Creates a Mesh Scene containing all Parts.
   *
   * @return Created Scene.
   */
  public Scene createScene_Mesh() {
    return createScene("Mesh", new Vector<NamedObject>(), true);
  }

  /**
   * Creates a Mesh Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @return Created Scene.
   */
  public Scene createScene_Mesh(Collection<NamedObject> objects) {
    return createScene("Mesh", objects, true);
  }

  /**
   * Creates a Scalar Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @param varName given function name inside the Field Functions. E.g.: Velocity,
   *                BoundaryHeatFlux, FaceFlux, etc.
   * @param unit given variable Unit. If <i>NULL</i> get Default unit.
   * @param smoothFilled Smooth Fill?
   * @return Created Scene.
   */
  public Scene createScene_Scalar(Collection<NamedObject> objects, String varName, Units unit,
                                                                            boolean smoothFilled) {
    Scene scene = createScene("Scalar", objects, true);
    ScalarDisplayer scalDisp = (ScalarDisplayer) scene.getDisplayerManager().getDisplayer("Scalar");
    if(smoothFilled){
        scalDisp.setFillMode(1);
    }
    PrimitiveFieldFunction ff = null;
    try{
        ff = getPrimitiveFieldFunction(varName);
    } catch (Exception e){ }
    if(varName.matches("^Vel.*")){
        ff = getPrimitiveFieldFunction("Velocity");
        if (varName.equalsIgnoreCase("Velocity") || varName.equalsIgnoreCase("VelMag") ||
                                                            varName.equalsIgnoreCase("Vel Mag")) {
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionMagnitude(ff));
        } else if(varName.equalsIgnoreCase("VelX") || varName.equalsIgnoreCase("Vel X")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 0));
        } else if(varName.equalsIgnoreCase("VelY") || varName.equalsIgnoreCase("Vel Y")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 1));
        } else if(varName.equalsIgnoreCase("VelZ") || varName.equalsIgnoreCase("Vel Z")){
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionComponent(ff, 2));
        }
    } else if(varName.matches("^WallShear.*")){
        ff = getPrimitiveFieldFunction("WallShearStress");
        if (varName.equalsIgnoreCase("WallShearStress") ||
                varName.equalsIgnoreCase("WallShearMag") ||
                varName.equalsIgnoreCase("WallShear Mag")) {
            scalDisp.getScalarDisplayQuantity().setFieldFunction(getFieldFuncionMagnitude(ff));
        }
    } else {
        scalDisp.getScalarDisplayQuantity().setFieldFunction(ff);
    }
    if (unit != null) {
        scalDisp.getScalarDisplayQuantity().setUnits(unit);
    }
    return scene;
  }

  /**
   * Creates a Vector Scene containing the input Objects. <b><u>Hint:</u></b> Use a Vector
   * to collect the Objects.
   *
   * @param objects given Collection of Objects.
   * @param licOption Linear Integral Convolution option? <b>True</b> or <b>False</b>
   * @return Created Scene.
   */
  public Scene createScene_Vector(Collection<NamedObject> objects, boolean licOption) {
    Scene scene = createScene("Vector", objects, true);
    VectorDisplayer vecDisp = (VectorDisplayer) scene.getDisplayerManager().getDisplayer("Vector");
    if(licOption){
        vecDisp.setDisplayMode(1);
    }
    return scene;
  }

  private Scene createScene(String sceneType, Collection<NamedObject> objects, boolean verboseOption){
    printAction("Creating a " + sceneType + " Scene", verboseOption);
    Scene scene = sim.getSceneManager().createScene(sceneType);
    createSceneDisplayer(scene, sceneType, objects, verboseOption);
    sayOK(verboseOption);
    return scene;
  }

  private Displayer createSceneDisplayer(Scene scene, String sceneType,
                            Collection<NamedObject> objects, boolean verboseOption) {
    Displayer disp = null;
    if (sceneType.equals("Geometry") || sceneType.equals("Mesh")) {
        disp = ((PartDisplayer) scene.getDisplayerManager().createPartDisplayer(sceneType));
        ((PartDisplayer) disp).setColorMode(colourByPart);
        ((PartDisplayer) disp).setOutline(false);
        ((PartDisplayer) disp).setSurface(true);
        if (sceneType.equals("Mesh")) {
            ((PartDisplayer) disp).setMesh(true);
        }
    } else if (sceneType.equals("Scalar")) {
        disp = ((ScalarDisplayer) scene.getDisplayerManager().createScalarDisplayer(sceneType));
    } else if (sceneType.equals("Vector")) {
        disp = ((VectorDisplayer) scene.getDisplayerManager().createVectorDisplayer(sceneType));
    }
    disp.initialize();
    int n = 0;
    say("Adding objects to Displayer...", verboseOption);
    if (objects.isEmpty()) {
        if(sim.getRegionManager().isEmpty()) {
            for (GeometryPart gp : getAllLeafParts(false)) {
                /*
                 * Get the Part Surfaces. Nothing is shown if getting just the Parts.
                    disp.addPart((NamedObject) gp);  ** buggy
                 */
                for (PartSurface ps : gp.getPartSurfaces()) {
                    disp.addPart((NamedObject) ps);
                    n++;
                }
            }
            say("Added All Parts: " + n, verboseOption);
        } else {
            for (Boundary bdry : getAllBoundaries(false)) {
                disp.addPart((NamedObject) bdry);
                n++;
            }
            say("Added All Boundaries: " + n, verboseOption);
        }
    } else {
        for(Object obj : objects){
            say("  " + obj.toString(), verboseOption);
            disp.addPart((NamedObject) obj);
            n++;
        }
        say("Objects added: " + n, verboseOption);
    }
    disp.setPresentationName(scene.getPresentationName());
    return disp;
  }

  /**
   * Creates a Section Plane.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param orientation  given normal orientation coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlane(double[] origin, double[] orientation){
    printAction("Creating a Section Plane");
    Vector<Object> where = (Vector) getAllRegions();
    DoubleVector vecOrient = new DoubleVector(orientation);
    DoubleVector vecOrigin = new DoubleVector(origin);
    DoubleVector vecOffsets = new DoubleVector(new double[] {0.0});
    return (PlaneSection) sim.getPartManager().createImplicitPart(where, vecOrient, vecOrigin, 0, 1, vecOffsets);
  }

  /**
   * Creates a Section Plane Normal to X direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneX(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {1., 0., 0.});
    say("Normal to X direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Section Plane Normal to Y direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneY(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 1., 0.});
    say("Normal to Y direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Section Plane Normal to Z direction.
   *
   * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
   * @param name given Plane name.
   * @return The created Section Plane.
   */
  public PlaneSection createSectionPlaneZ(double[] origin, String name){
    PlaneSection plane = createSectionPlane(origin, new double[] {0., 0., 1.});
    say("Normal to Z direction");
    plane.setPresentationName(name);
    sayOK();
    return plane;
  }

  /**
   * Creates a Simple Block.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param unit given units.
   * @param name given Block name.
   * @return The brand new Block Part.
   */
  public SimpleBlockPart createShapePartBlock(double[] coord1, double[] coord2, Units unit, String name) {
    MeshPartFactory mpf = sim.get(MeshPartFactory.class);
    SimpleBlockPart sbp = mpf.createNewBlockPart(sim.get(SimulationPartManager.class));
    LabCoordinateSystem labCSYS = sim.getCoordinateSystemManager().getLabCoordinateSystem();
    sbp.setCoordinateSystem(labCSYS);
    Coordinate coordinate_0 = sbp.getCorner1();
    Coordinate coordinate_1 = sbp.getCorner2();
    coordinate_0.setCoordinate(unit, unit, unit, new DoubleVector(coord1));
    coordinate_1.setCoordinate(unit, unit, unit, new DoubleVector(coord2));
    sbp.getTessellationDensityOption().setSelected(TessellationDensityOption.MEDIUM);
    sbp.setPresentationName(name);
    return sbp;
  }

  /**
   * Creates a Simple Cylinder Part.
   *
   * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
   * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
   * @param r1 given radius 1.
   * @param r2 given radius 2.
   * @param unit given units.
   * @param name given Cylinder name.
   * @return The brand new Cylinder Part.
   */
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

  @Deprecated
  public void createStoppingCriteriaFromReportMonitor_StdDev(ReportMonitor repMon, double stdDev, int samples){
    createStoppingCriteria(repMon, stdDev, samples, true);
  }

  /**
   * Creates a Stopping Criteria from a Report Monitor.<p>
   * For now, the type can be <b>Asymptotic</b> or <b>Standard Deviation</b>
   *
   * @param repMon given Report Monitor.
   * @param val given value.
   * @param samples how many samples (or iterations)?
   * @param stdDev <b>True</b> for Standard Deviation; <b>False</b> for Asymptotic.
   */
  public void createStoppingCriteria(ReportMonitor repMon, double val, int samples, boolean stdDev) {
    if (stdDev) {
        say("Creating a Stopping Criterion based on Standard Deviation.");
        say("  Standard Deviation: " + val + " " + repMon.getMonitoredValueUnits());
    } else {
        say("Creating a Stopping Criterion based on Asymptotic differences.");
        say("  Asymptotic: " + val + " " + repMon.getMonitoredValueUnits());
    }
    say("  Number of Samples: " + samples);
    if(val == 0 || samples == 0){
        say("Got NULL inputs. Skipping creation. Just monitoring.");
        return;
    }
    MonitorIterationStoppingCriterion monItStpCrit = repMon.createIterationStoppingCriterion();
    monItStpCrit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.AND);
    if (stdDev) {
        ((MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.STANDARD_DEVIATION);
        MonitorIterationStoppingCriterionStandardDeviationType stpCritStdDev = ((MonitorIterationStoppingCriterionStandardDeviationType) monItStpCrit.getCriterionType());
        stpCritStdDev.getStandardDeviation().setUnits(repMon.getMonitoredValueUnits());
        stpCritStdDev.getStandardDeviation().setValue(val);
        stpCritStdDev.setNumberSamples(samples);
    } else {
        ((MonitorIterationStoppingCriterionOption) monItStpCrit.getCriterionOption()).setSelected(MonitorIterationStoppingCriterionOption.ASYMPTOTIC);
        MonitorIterationStoppingCriterionAsymptoticType stpCritAsympt = ((MonitorIterationStoppingCriterionAsymptoticType) monItStpCrit.getCriterionType());
        stpCritAsympt.getMaxWidth().setUnits(repMon.getMonitoredValueUnits());
        stpCritAsympt.getMaxWidth().setValue(val);
        stpCritAsympt.setNumberSamples(samples);
    }
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
    Vector<CompositePart> vecCP = (Vector) getNthLevelCompositeParts(2);
    for(int i = 0; i < vecCP.size() - 1; i++){
        CompositePart cp_i = vecCP.get(i);
        for(int j = i+1; j < vecCP.size(); j++){
            Vector<MeshPart> vecMP = new Vector<MeshPart>();
            CompositePart cp_j = vecCP.get(j);
            printAction("Imprinting CAD Parts");
            vecMP.addAll(getMeshParts(cp_i.getPathInHierarchy().replace("|", ".") + ".*"));
            vecMP.addAll(getMeshParts(cp_j.getPathInHierarchy().replace("|", ".") + ".*"));
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
    vecMP.addAll(getMeshParts(compPart1.getPathInHierarchy().replace("|", ".") + ".*"));
    vecMP.addAll(getMeshParts(compPart2.getPathInHierarchy().replace("|", ".") + ".*"));
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

  /**
   * This method will Imprint by CAD every N'th level defined. It is mainly for debugging purposes.
   *
   * @param nthLevel given N'th level for sub-assemblies imprinting.
   */
  public void debugImprinting_EveryNthLevel(int nthLevel){
    printAction("Debugging the Parts. Imprinting every N\'th Level Sub assembly");
    for (CompositePart cp : getNthLevelCompositeParts(nthLevel)) {
        printAction("Imprinting CAD Parts");
        say("Sub assembly: " + cp.getPresentationName());
        imprintPartsByCADMethod((Vector) getMeshParts(cp.getPathInHierarchy().replace("|", ".") + ".*"));
        say("\n");
    }
  }

  /**
   * Disables the Embedded Thin Mesher.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableEmbeddedThinMesher(MeshContinuum mshCont){
    printAction("Disabling Embedded Thin Mesher");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(SolidMesherSubModel.class));
    sayOK();
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of a Region.
   *
   * @param region given Region.
   */
  public void disableFeatureCurveSizeOnRegion(Region region){
    disableFeatureCurveSizeOnRegion(region, true);
  }

  /**
   * Disable the Custom Surface Size in all Feature Curves of the Regions searched by REGEX.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void disableFeatureCurveSizeOnRegions(String regexPatt){
    printAction(String.format("Disabling Feature Curves Mesh Size on all " +
                                "Regions by REGEX pattern: \"%s\"", regexPatt));
    for (Region region : getAllRegions(regexPatt, false)) {
        disableFeatureCurveSizeOnRegion(region, false);
        sayRegion(region, true);
    }
    sayOK();
  }

  private void disableFeatureCurveSizeOnRegion(Region region, boolean verboseOption){
    printAction("Disabling Custom Feature Curve Mesh Size", verboseOption);
    sayRegion(region, verboseOption);
    Collection<FeatureCurve> colFC = region.getFeatureCurveManager().getFeatureCurves();
    for (FeatureCurve fc : colFC) {
        try{
            fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(false);
        } catch (Exception e){
            say("  Error disabling " + fc.getPresentationName(), verboseOption);
        }
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Continua from a Region.
   *
   * @param region given Region.
   */
  public void disableMeshContinua(Region region) {
    disableMeshContinua(region, true);
  }

  private void disableMeshContinua(Region region, boolean verboseOption){
    printAction("Disabling Mesh Continua", verboseOption);
    sayRegion(region, verboseOption);
    try{
        sayMeshContinua(region.getMeshContinuum(), verboseOption);
        region.getMeshContinuum().erase(region);
    } catch (Exception e){
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disables the Mesh Per Region Flag in a Mesh Continua.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableMeshPerRegion(MeshContinuum mshCont){
    printAction("Unsetting Mesh Continua as \"Per-Region Meshing\"");
    sayMeshContinua(mshCont);
    mshCont.setMeshRegionByRegion(false);
    sayOK();
  }

  private void disablePhysicsContinua(Region region, boolean verboseOption){
    printAction("Disabling Physics Continua", verboseOption);
    sayRegion(region, verboseOption);
    try{
        sayPhysicsContinua(region.getPhysicsContinuum(), verboseOption);
        region.getPhysicsContinuum().erase(region);
    } catch (Exception e){
        say("Already disabled.", verboseOption);
    }
    sayOK(verboseOption);
  }

  /**
   * Disable the Prisms Layers on a given Boundary.
   *
   * @param bdry given Boundary.
   */
  public void disablePrismLayers(Boundary bdry){
    disablePrismLayers(bdry, true);
  }

  /**
   * Disable the Prisms Layers on all Boundaries that match the search criteria.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void disablePrismLayers(String regexPatt){
    printAction(String.format("Disabling Prism Layers on all Boundaries " +
                                "by REGEX pattern: \"%s\"", regexPatt));
    printLine();
    for (Boundary bdry : getAllBoundaries(regexPatt, false, false)) {
        say("");
        disablePrismLayers(bdry, true);
        say("");
    }
    printLine();
    sayOK();
  }

  /**
   * Disables the Prism Layers.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disablePrismLayers(MeshContinuum mshCont){
    printAction("Disabling Prism Layers");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(PrismMesherModel.class));
    sayOK();
  }

  @Deprecated
  public void disablePrismsOnBoundary(Boundary bdry){
    disablePrismLayers(bdry, true);
  }

  private void disablePrismLayers(Boundary bdry, boolean verboseOption){
    say("Disabling Prism Layers on Boundary...", verboseOption);
    sayBdry(bdry);
    try {
        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.DISABLE);
        sayOK(verboseOption);
    } catch (Exception e1) {
        say("Warning! Could not disable Prism in Boundary.", verboseOption);
    }
  }

  public void disableRadiationS2S(Region region){
    printAction("Disabling Radiation Surface to Surface (S2S)");
    sayRegion(region);
    region.getConditions().get(RadiationTransferOption.class).setOptionEnabled(false);
    sayOK();
  }

  /**
   * Disables a Region, i.e., unsets its Physics and Mesh Continuas.
   *
   * @param region given Region.
   */
  public void disableRegion(Region region){
    printAction("Disabling a Region");
    sayRegion(region);
    disableMeshContinua(region, false);
    disablePhysicsContinua(region, false);
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

  /**
   * Disables the Surface Proximity Refinement.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableSurfaceProximityRefinement(MeshContinuum mshCont){
    printAction("Disabling Surface Proximity Refinement");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(false);
    sayOK();
  }

  /**
   * Disables the Surface Remesher.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableSurfaceRemesher(MeshContinuum mshCont){
    printAction("Disabling Surface Remesher");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(ResurfacerMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Remesher Automatic Repair.
   *
   * @param mshCont given Mesh Continua.
   */
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

  /**
   * Disables the Surface Remesher Project to CAD feature.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableSurfaceRemesherProjectToCAD(MeshContinuum mshCont){
    printAction("Disabling Project to CAD");
    sayMeshContinua(mshCont);
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoAlignedMeshing(false);
    mshCont.getReferenceValues().get(ProjectToCadOption.class).setProjectToCad(false);
    sayOK();
  }

  /**
   * Disables the Surface Wrapper.
   *
   * @param mshCont given Mesh Continua.
   */
  public void disableSurfaceWrapper(MeshContinuum mshCont){
    printAction("Disabling Surface Wrapper");
    sayMeshContinua(mshCont);
    mshCont.disableModel(mshCont.getModelManager().getModel(SurfaceWrapperMeshingModel.class));
    sayOK();
  }

  /**
   * Disables the Surface Wrapper GAP closure.
   *
   * @param mshCont given Mesh Continua.
   */
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
    setMeshWrapperFeatureAngle(mshCont, mshWrapperFeatureAngle);
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
    queryGeometryRepresentation().findPartPartContacts((Vector) getAllLeafPartsAsMeshParts(), tol_meters);
    sayOK();
  }

  /**
   * Freezes/Unfreezes the K-Epsilon Turbulent 2-equation Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeKeTurbSolver(boolean freezeOption){
    freezeSolver(KeTurbSolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the K-Epsilon Turbulent Viscosity Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeKeTurbViscositySolver(boolean freezeOption){
    freezeSolver(KeTurbViscositySolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Surface to Surface (S2S) Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeS2SSolver(boolean freezeOption){
    freezeSolver(S2sSolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Segregated Energy Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeSegregatedEnergySolver(boolean freezeOption){
    freezeSolver(SegregatedEnergySolver.class, freezeOption);
  }

  /**
   * Freezes/Unfreezes the Segregated Flow Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeSegregatedFlowSolver(boolean freezeOption){
    freezeSolver(SegregatedFlowSolver.class, freezeOption);
  }

  /**
   * Freeze/Unfreeze the View Factors Solver. Updates the View Factors upon unfreezing.
   *
   * @param freezeOption given option.
   */
  public void freezeViewfactorsCalculatorSolver(boolean freezeOption){
    Solver solver = freezeSolver(ViewfactorsCalculatorSolver.class, freezeOption);
    if(!freezeOption){
        ((ViewfactorsCalculatorSolver) solver).calculateViewfactors();
    }
  }

  /**
   * Freeze/Unfreeze the Wall Distance Solver.
   *
   * @param freezeOption given option.
   */
  public void freezeWallDistanceSolver(boolean freezeOption){
    freezeSolver(WallDistanceSolver.class, freezeOption);
  }

  private Solver freezeSolver(Class solverClass, boolean freezeOption){
    Solver solver = sim.getSolverManager().getSolver(solverClass);
    String msg = "Freezing ";
    if(!freezeOption){ msg = msg.replace("Free", "Unfree"); }
    printAction(msg + solver);
    solver.setFrozen(freezeOption);
    sayOK();
    return solver;
  }

  /**
   * Generates the Surface Mesh.
   */
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

  /**
   * Generates the Volume Mesh.
   */
  public void genVolumeMesh() {
    if(skipMeshGeneration){ return; }
    printAction("Generating Volume Mesh");
    sim.get(MeshPipelineController.class).generateVolumeMesh();
  }

  /**
   * Get all Boundaries from all Regions.
   *
   * @return All Boundaries.
   */
  public Collection<Boundary> getAllBoundaries() {
    return getAllBoundaries(true);
  }

  /**
   * Get all Boundaries from all Regions based on REGEX.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found.
   */
  public Collection<Boundary> getAllBoundaries(String regexPatt, boolean skipInterfaces) {
    return getAllBoundaries(regexPatt, true, skipInterfaces);
  }

  /**
   * Get all Boundaries from all Regions based on an Array of REGEX Patterns.
   *
   * @param regexPattArray given array of REGEX search patterns.
   * @param skipInterfaces skip Boundary Interfaces?
   * @return All Boundaries found.
   */
  public Collection<Boundary> getAllBoundaries(String[] regexPattArray, boolean skipInterfaces) {
    return getAllBoundaries(regexPattArray, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundaries(boolean verboseOption) {
    say("Getting all Boundaries from all Regions...", verboseOption);
    Vector<Boundary> allBdrys = new Vector<Boundary>();
    for (Region region : getAllRegions(false)) {
        Collection<Boundary> bdrys = region.getBoundaryManager().getBoundaries();
        for (Boundary bdry : bdrys) {
            allBdrys.add(bdry);
        }
    }
    say("Boundaries found: " + allBdrys.size(), verboseOption);
    return ((Collection<Boundary>) allBdrys);
  }


  private Collection<Boundary> getAllBoundaries(Region region, String regexPatt,
                                    boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Region by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    sayRegion(region, verboseOption);
    Vector<Boundary> chosenBdrys = (Vector<Boundary>) getAllBoundariesFromRegion(region, false, skipInterfaces);
    Vector<Boundary> retBdrys = (Vector<Boundary>) chosenBdrys.clone();
    for (Boundary bdry : chosenBdrys) {
        if (bdry.getRegion() != region) {
            retBdrys.remove(bdry);
        }
    }
    say("Boundaries found by REGEX: " + retBdrys.size(), verboseOption);
    return (Collection<Boundary>) retBdrys;
  }

  private Collection<Boundary> getAllBoundaries(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from all Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> chosenBdrys = new Vector<Boundary>();
    for (Boundary bdry : getAllBoundaries(false)) {
        if (isInterface(bdry)) { continue; }
        if (bdry.getPresentationName().matches(regexPatt)) {
            chosenBdrys.add(bdry);
        }
    }
    say("Boundaries found by REGEX: " + chosenBdrys.size(), verboseOption);
    return (Collection<Boundary>) chosenBdrys;
  }

  private Collection<Boundary> getAllBoundaries(String[] regexPattArray, boolean verboseOption,
                                                                            boolean skipInterfaces) {
    return getAllBoundaries(null, regexPattArray, verboseOption, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundaries(Region region, String[] regexPattArray,
                                                    boolean verboseOption, boolean skipInterfaces) {
    if (region != null){
        say("Getting all Boundaries from all Regions by an Array of REGEX pattern", verboseOption);
    } else {
        say("Getting all Boundaries from a Region by an Array of REGEX pattern", verboseOption);
        sayRegion(region, verboseOption);
    }
    Vector<Boundary> allBdrys = new Vector<Boundary>();
    for (int i = 0; i < regexPattArray.length; i++) {
        Vector<Boundary> chosenBdrys = new Vector<Boundary>();
        for (Boundary bdry : getAllBoundaries(regexPattArray[i], false, skipInterfaces)){
            if (region != null && bdry.getRegion() != region) { continue; }
            chosenBdrys.add(bdry);
        }
        say(String.format("Boundaries found by REGEX pattern: \"%s\" == %d", regexPattArray[i],
                chosenBdrys.size()), verboseOption);
        allBdrys.addAll(chosenBdrys);
        chosenBdrys.clear();
    }
    say("Overal Boundaries found by REGEX: " + allBdrys.size(), verboseOption);
    return (Collection<Boundary>) allBdrys;
  }

  /**
   * Get all boundaries from a Region.
   *
   * @param region given Region.
   * @return All Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegion(Region region) {
    return getAllBoundariesFromRegion(region, true, false);
  }

  /**
   * Get all boundaries from a Region. Option to skip Interface Boundaries.
   *
   * @param region given Region.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegion(Region region, boolean skipInterfaces) {
    return getAllBoundariesFromRegion(region, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundariesFromRegion(Region region, boolean verboseOption, boolean skipInterfaces) {
    Collection<Boundary> colBdry = region.getBoundaryManager().getBoundaries();
    Vector<Boundary> bdriesNotInterface = new Vector<Boundary>();
    for (Boundary bdry : colBdry) {
        if (isInterface(bdry)) { continue; }
        bdriesNotInterface.add(bdry);
    }
    if (skipInterfaces) {
        say("Getting all Boundaries but Skip Interfaces from Region: " + region.getPresentationName(), verboseOption);
        say("Non-Interface Boundaries found: " + bdriesNotInterface.size(), verboseOption);
        return (Collection<Boundary>) bdriesNotInterface;
    } else {
        say("Getting all Boundaries from Region: " + region.getPresentationName(), verboseOption);
        say("Boundaries found: " + colBdry.size(), verboseOption);
        return colBdry;
    }
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, false);
  }

  /**
   * Get all boundaries from Regions that matches a REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces skip interface boundaries?
   * @return Collected Boundaries.
   */
  public Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean skipInterfaces) {
    return getAllBoundariesFromRegionsByName(regexPatt, true, skipInterfaces);
  }

  private Collection<Boundary> getAllBoundariesFromRegionsByName(String regexPatt, boolean verboseOption, boolean skipInterfaces) {
    say(String.format("Getting all Boundaries from Regions searched by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> allBdrysFromRegions = new Vector<Boundary>();
    for (Region reg : getAllRegions(regexPatt, false)) {
        allBdrysFromRegions.addAll(getAllBoundariesFromRegion(reg, false, skipInterfaces));
    }
    say("Boundaries found by REGEX: " + allBdrysFromRegions.size(), verboseOption);
    return (Collection<Boundary>) allBdrysFromRegions;
  }

  /**
   * Get all Composite Parts.
   *
   * @return All Composite Parts.
   */
  public Collection<CompositePart> getAllCompositeParts() {
    return getAllCompositeParts(true);
  }

  /**
   * Get all Composite Parts base on REGEX criteria.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Matched Composite Parts.
   */
  public Collection<CompositePart> getAllCompositeParts(String regexPatt) {
    return getAllCompositeParts(regexPatt, true);
  }

  private Collection<CompositePart> getAllCompositeParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Composite Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<CompositePart> vecCP = new Vector<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (cp.getPresentationName().matches(regexPatt)) {
            say("  Found: " + cp.getPresentationName(), verboseOption);
            vecCP.add(cp);
        }
    }
    say("Composite Parts found by REGEX: " + vecReg.size(), verboseOption);
    return vecCP;
  }

  private Collection<CompositePart> getAllCompositeParts(boolean verboseOption) {
    say("Getting all Composite Parts...", verboseOption);
    Vector<CompositePart> compPrtCol = new Vector<CompositePart>();
    for (GeometryPart gp : getAllGeometryParts(false)) {
        if (!isCompositePart(gp)) { continue; }
        Vector<CompositePart> vecCompPart = new Vector<CompositePart>();
        for (CompositePart cp : getCompositeChildren((CompositePart) gp, vecCompPart, false)) {
            say("  Composite Part Found: " + cp.getPresentationName(), verboseOption);
        }
        compPrtCol.addAll(vecCompPart);
    }
    say("Composite Parts found: " + compPrtCol.size(), verboseOption);
    return compPrtCol;
  }

  /**
   * Get all Feature Curves from all Regions.
   *
   * @return All Feature Curves.
   */
  public Collection<FeatureCurve> getAllFeatureCurves(){
    return getAllFeatureCurves(true);
  }

  private Collection<FeatureCurve> getAllFeatureCurves(boolean verboseOption){
    say("Getting all Feature Curves...", verboseOption);
    Vector<FeatureCurve> vecFC = new Vector<FeatureCurve>();
    for (Region region : getAllRegions(verboseOption)) {
        vecFC.addAll(getFeatureCurves(region, false));
    }
    say("All Feature Curves: " + vecFC.size(), verboseOption);
    return vecFC;
  }

  /**
   * Get all Geometry Parts.
   *
   * @return All Geometry Parts.
   */
  public Collection<GeometryPart> getAllGeometryParts(){
    return getAllGeometryParts(true);
  }

  /**
   * Get all Geometry Parts based on REGEX.
   *
   * @param regexPatt REGEX search pattern.
   * @return All Geometry Parts found.
   */
  public Collection<GeometryPart> getAllGeometryParts(String regexPatt){
    return getAllGeometryParts(regexPatt, true);
  }

  private Collection<GeometryPart> getAllGeometryParts(boolean verboseOption){
    say("Getting all Geometry Parts...", verboseOption);
    Collection<GeometryPart> colGP = sim.get(SimulationPartManager.class).getParts();
    say("Geometry Parts found: " + colGP.size(), verboseOption);
    return colGP;
  }

  private Collection<GeometryPart> getAllGeometryParts(String regexPatt, boolean verboseOption){
    say(String.format("Getting all Geometry Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<GeometryPart> gpVec = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllGeometryParts()){
        if(gp.getPresentationName().matches(regexPatt)){
            say("  Found: " + gp.getPresentationName(), verboseOption);
            gpVec.add(gp);
        }
    }
    say("Geometry Parts found by REGEX: " + gpVec.size(), verboseOption);
    return gpVec;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public Collection<Interface> getAllInterfaces(){
    return getAllInterfaces(true);
  }

  /**
   * Get all Interfaces between 2 Regions.
   *
   * @param region0 given first Region.
   * @param region1 given second Region.
   * @return Collection of Interfaces shared between both Regions.
   */
  public Collection<Interface> getAllInterfaces(Region region0, Region region1){
    Vector<Interface> intrfVec = new Vector<Interface>();
    say("Getting all Interfacess between 2 Regions...");
    sayRegion(region0);
    sayRegion(region1);
    Integer r0 = region0.getIndex();
    Integer r1 = region1.getIndex();
    for (Interface intrfPair : getAllInterfaces(false)) {
        Integer n0 = intrfPair.getRegion0().getIndex();
        Integer n1 = intrfPair.getRegion1().getIndex();
        if (min(r0,r1) == min(n0,n1) && max(r0,r1) == max(n0,n1)) {
            intrfVec.add(intrfPair);
            say("  Interface found: " + intrfPair);
        }
    }
    say("Interfaces found: " + intrfVec.size());
    return intrfVec;
  }

  private Collection<Interface> getAllInterfaces(boolean verboseOption){
    say("Getting all Interfaces...", verboseOption);
    Collection<Interface> colIntrfcs = sim.get(InterfaceManager.class).getObjects();
    say("Interfaces found: " + colIntrfcs.size(), verboseOption);
    return colIntrfcs;
  }

  /**
   * Get all Interfaces.
   *
   * @return All Interfaces.
   */
  public Collection<Object> getAllInterfacesAsObjects(){
    return getAllInterfacesAsObjects(true);
  }

  private Collection<Object> getAllInterfacesAsObjects(boolean verboseOption){
    say("Getting all Interfaces...", verboseOption);
    Collection<Object> colIntrfcs = sim.getInterfaceManager().getChildren();
    say("Interfaces found: " + colIntrfcs.size(), verboseOption);
    return colIntrfcs;
  }

  /**
   * Get all Leaf Mesh Parts in the model.
   *
   * @return Collection of Leaf Mesh Parts.
   */
  public Collection<LeafMeshPart> getAllLeafMeshParts(){
    return getAllLeafMeshParts(true);
  }

  /**
   * Get all Leaf Mesh Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Leaf Mesh Parts.
   */
  public Collection<LeafMeshPart> getAllLeafMeshParts(String regexPatt) {
    return getAllLeafMeshParts(regexPatt, true);
  }

  private Collection<LeafMeshPart> getAllLeafMeshParts(boolean verboseOption){
    say("Getting all Leaf Mesh Parts...", verboseOption);
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    for(GeometryPart gp : colLP){
        if (isLeafMeshPart(gp)) {
            say("Found: " + gp.getPresentationName(), verboseOption);
            lmpVec.add((LeafMeshPart) gp);
        }
    }
    say("Leaf Mesh Parts found: " + lmpVec.size(), verboseOption);
    return lmpVec;
  }

  private Collection<LeafMeshPart> getAllLeafMeshParts(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Leaf Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<LeafMeshPart> lmpVec = new Vector<LeafMeshPart>();
    for (LeafMeshPart lmp : getAllLeafMeshParts(false)) {
        if (lmp.getPresentationName().matches(regexPatt)) {
            say("Found: " + lmp.getPresentationName(), verboseOption);
            lmpVec.add(lmp);
        }
    }
    say("Leaf Mesh Parts found by REGEX: " + lmpVec.size(), verboseOption);
    return lmpVec;
  }

  /**
   * Get all Leaf Parts in the model.
   *
   * @return Collection of Geometry Parts.
   */
  public Collection<GeometryPart> getAllLeafParts(){
    return getAllLeafParts(true);
  }

  /**
   * Get all Leaf Parts in the model by REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Geometry Parts.
   */
  public Collection<GeometryPart> getAllLeafParts(String regexPatt){
    return getAllLeafParts(regexPatt, true);
  }

  private Collection<GeometryPart> getAllLeafParts(boolean verboseOption){
    say("Getting all Leaf Parts...", verboseOption);
    Collection<GeometryPart> colLP = sim.get(SimulationPartManager.class).getLeafParts();
    say("Leaf Parts found: " + colLP.size(), verboseOption);
    return colLP;
  }

  private Collection<GeometryPart> getAllLeafParts(String regexPatt, boolean verboseOption){
    say(String.format("Getting all Leaf Parts by REGEX pattern: \"%s\"", regexPatt));
    Vector<GeometryPart> vecGP = new Vector<GeometryPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        if(gp.getPathInHierarchy().matches(regexPatt)){
            say("  Found: " + gp.getPathInHierarchy(), verboseOption);
            vecGP.add(gp);
        }
    }
    say("Leaf Parts found by REGEX: " + vecGP.size(), verboseOption);
    return vecGP;
  }

  private Collection<CadPart> getAllLeafPartsAsCadParts(){
    Vector<CadPart> vecCP = new Vector<CadPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        vecCP.add((CadPart) gp);
    }
    return vecCP;
  }

  private Collection<MeshPart> getAllLeafPartsAsMeshParts(){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    for(GeometryPart gp : getAllLeafParts(false)){
        vecMP.add((MeshPart) gp);
    }
    return vecMP;
  }

  /**
   * Get all Part Surfaces from the model.
   *
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(){
    return getAllPartSurfaces(".*", true);
  }

  /**
   * Get all Part Surfaces from a Geometry Part.
   *
   * @param gp given Geometry Part.
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(GeometryPart gp){
    return gp.getPartSurfaces();
  }

  /**
   * Get all Part Surfaces from a Geometry Part by REGEX search.
   *
   * @param lmp given Geometry Part.
   * @param regexPatt given REGEX search pattern.
   * @return Collection of Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(GeometryPart gp, String regexPatt){
    return getAllPartSurfaces(gp, regexPatt, false);
  }

  /**
   * Get all Part Surfaces from a Leaf Mesh Part.
   *
   * @param lmp given Leaf Mesh Part.
   * @return All Part Surfaces.
   */
  public Collection<PartSurface> getAllPartSurfaces(LeafMeshPart lmp){
    return lmp.getPartSurfaces();
  }

  private Collection<PartSurface> getAllPartSurfaces(String regexPatt, boolean verboseOption){
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    if (regexPatt.matches(".*")) {
        say("Getting all Part Surfaces from All Leaf Parts", verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    }
    for (GeometryPart gp : getAllLeafParts(false)) {
        psVec.addAll(getAllPartSurfaces(gp, regexPatt, false));
    }
    say("Total Part Surfaces found: " + psVec.size(), verboseOption);
    if (!regexPatt.matches(".*")) {
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }

  //private Collection<PartSurface> getAllPartSurfaces(LeafMeshPart lmp, String regexPatt, boolean verboseOption){
  private Collection<PartSurface> getAllPartSurfaces(GeometryPart gp, String regexPatt, boolean verboseOption){
    Vector<PartSurface> psVec = new Vector<PartSurface>();
    sayPart(gp, verboseOption);
    if (regexPatt.equals(".*")) {
        say("Getting all Part Surfaces from Part...", verboseOption);
        psVec.addAll(gp.getPartSurfaces());
        say("Part Surfaces found: " + psVec.size(), verboseOption);
    } else {
        say(String.format("Getting all Part Surfaces by REGEX pattern: \"%s\"", regexPatt), verboseOption);
        for(PartSurface ps : gp.getPartSurfaces()){
            if(ps.getPresentationName().matches(regexPatt)){
                say("  Found: " + ps.getPresentationName(), verboseOption);
                psVec.add(ps);
            }
        }
        say("Part Surfaces found by REGEX: " + psVec.size(), verboseOption);
    }
    return psVec;
  }

  /**
   * Get all Mesh Continuas.
   *
   * @return All Mesh Continuas.
   */
  public Collection<MeshContinuum> getAllMeshContinuas(){
    return getAllMeshContinuas(true);
  }

  private Collection<MeshContinuum> getAllMeshContinuas(boolean verboseOption) {
    say("Getting all Mesh Continuas...", verboseOption);
    Vector<MeshContinuum> vecMC = new Vector<MeshContinuum>();
    for (Continuum cont : sim.getContinuumManager().getObjects()) {
        if(cont.getBeanDisplayName().equals("MeshContinum")) {
            vecMC.add((MeshContinuum) cont);
        }
    }
    say("Mesh Continuas found: " + vecMC.size(), verboseOption);
    return vecMC;
  }

  /**
   * Get all Physics Continuas.
   *
   * @return All Physics Continuas.
   */
  public Collection<PhysicsContinuum> getAllPhysicsContinuas() {
    return getAllPhysicsContinuas(true);
  }

  private Collection<PhysicsContinuum> getAllPhysicsContinuas(boolean verboseOption){
    say("Getting all Physics Continuas...", verboseOption);
    Vector<PhysicsContinuum> vecPC = new Vector<PhysicsContinuum>();
    for(Continuum cont : sim.getContinuumManager().getObjects()){
        if(cont.getBeanDisplayName().equals("PhysicsContinum")){
            vecPC.add((PhysicsContinuum) cont);
        }
    }
    say("All Physics Continuas: " + vecPC.size(), verboseOption);
    return vecPC;
  }

  /**
   * Get all Regions.
   *
   * @return All regions.
   */
  public Collection<Region> getAllRegions() {
    return getAllRegions(true);
  }

  /**
   * Get all Regions searched by REGEX pattern.
   *
   * @param regexPatt search REGEX pattern.
   * @return Found regions.
   */
  public Collection<Region> getAllRegions(String regexPatt) {
    return getAllRegions(regexPatt, true);
  }

  private Collection<Region> getAllRegions(boolean verboseOption) {
    say("Getting all Regions...", verboseOption);
    Collection<Region> colReg = sim.getRegionManager().getRegions();
    say("All Regions: " + colReg.size(), verboseOption);
    return colReg;
  }

  private Collection<Region> getAllRegions(String regexPatt, boolean verboseOption) {
    say(String.format("Getting all Regions by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Region> vecReg = new Vector<Region>();
    for (Region reg : getAllRegions(false)) {
        if (reg.getPresentationName().matches(regexPatt)) {
            say("  Found: " + reg.getPresentationName(), verboseOption);
            vecReg.add(reg);
        }
    }
    say("Regions found by REGEX: " + vecReg.size(), verboseOption);
    return vecReg;
  }

  /**
   * Get all Regions that has a Remeshed Surface Representation.
   *
   * @return All Remeshed Regions.
   */
  public Collection<SurfaceRepRegion> getAllRemeshedRegions(){
    return getAllRemeshedRegions(true);
  }

  private Collection<SurfaceRepRegion> getAllRemeshedRegions(boolean verboseOption){
    say("Getting all Remeshed Regions...", verboseOption);
    SurfaceRep srfRep = queryRemeshedSurface();
    Collection<SurfaceRepRegion> colRemshReg = srfRep.getSurfaceRepRegionManager().getObjects();
    say("All Remeshed Regions: " + colRemshReg.size(), verboseOption);
    return colRemshReg;
  }

  /**
   * Get all Regions that has a Wrapped Surface Representation.
   *
   * @return All Wrapped Regions.
   */
  public Collection<SurfaceRepRegion> getAllWrappedRegions(){
    return getAllWrappedRegions(true);
  }

  private Collection<SurfaceRepRegion> getAllWrappedRegions(boolean verboseOption){
    say("Getting all Wrapped Regions...", verboseOption);
    SurfaceRep srfRep = queryWrappedSurface();
    Collection<SurfaceRepRegion> colWrappedReg = srfRep.getSurfaceRepRegionManager().getObjects();
    say("All Wrapped Regions: " + colWrappedReg.size(), verboseOption);
    return colWrappedReg;
  }

  /**
   * Loop in all Boundaries and returns the first match given the REGEX pattern. <b>Note:</b> It
   * will skip Interface Boundaries.
   *
   * @param regexPatt REGEX search pattern.
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt){
    return getBoundary(regexPatt, true, true);
  }

  /**
   * Loop in all Boundaries and returns the first match given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return The Boundary found. If nothing, returns NULL.
   */
  public Boundary getBoundary(String regexPatt, boolean skipInterfaces){
    return getBoundary(regexPatt, true, skipInterfaces);
  }

  private Boundary getBoundary(String regexPatt, boolean verboseOption, boolean skipInterfaces){
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(regexPatt, false, skipInterfaces);
    if (vecBdry.size() > 0) {
        say("Got by REGEX: " + vecBdry.firstElement().getPresentationName(), verboseOption);
        return vecBdry.firstElement();
    } else {
        say("Got NULL.\n");
        return null;
    }
  }


  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   * <b>Note:</b> It will skip Interface Boundaries.
   *
   * @param region given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region region, String regexPatt){
    return getBoundary(region, regexPatt, true, true);
  }

  /**
   * Loop over all Boundaries in Region and returns the first match given the REGEX pattern.
   *
   * @param region given Region.
   * @param regexPatt given REGEX search pattern. Straight boundary name will work as well.
   * @param skipInterfaces Skip Boundary Interfaces?
   * @return Found Boundary.
   */
  public Boundary getBoundary(Region region, String regexPatt, boolean skipInterfaces){
    return getBoundary(region, regexPatt, true, skipInterfaces);
  }

  private Boundary getBoundary(Region region, String regexPatt, boolean verboseOption, boolean skipInterfaces){
    sayRegion(region);
    try {
        Boundary foundBdry = region.getBoundaryManager().getBoundary(regexPatt);
        if (foundBdry != null) {
            say("Found: " + foundBdry.getPresentationName(), verboseOption);
            return foundBdry;
        }
    } catch (Exception e) { }
    Vector<Boundary> vecBdry = (Vector) getAllBoundaries(region, regexPatt, false, skipInterfaces);
    return getBoundary(vecBdry, regexPatt, verboseOption, skipInterfaces);
  }

  private Boundary getBoundary(Vector<Boundary> vecBdry, String regexPatt, boolean verboseOption,
                                                                            boolean skipInterfaces){
    say(String.format("Getting Boundary by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    if (vecBdry.size() > 0) {
        say("Got by REGEX: " + vecBdry.firstElement().getPresentationName(), verboseOption);
        return vecBdry.firstElement();
    } else {
        say("Got NULL.", verboseOption);
        return null;
    }
  }

  /**
   * Get a single CAD Part.
   *
   * @param name given name of the CAD Part.
   * @return CAD Part.
   */
  public CadPart getCadPart(String name){
    return getCadPart(name, true);
  }

  private CadPart getCadPart(String name, boolean verboseOption){
    say("Getting CadPart by name match...", verboseOption);
    for (GeometryPart gp : getAllLeafParts(false)) {
        if (gp.getPresentationName().equals(name)) {
            say("Found: " + name);
            return (CadPart) gp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Get all the children of a Composite Part.
   *
   * @param compPrt given Composite Part.
   * @param vecCP given Vector of Composite Part.
   * @return Vector of the children Composite Parts.
   */
  public Vector<CompositePart> getCompositeChildren(CompositePart compPrt, Vector<CompositePart> vecCP){
    return getCompositeChildren(compPrt, vecCP, true);
  }

  private Vector<CompositePart> getCompositeChildren(CompositePart compPrt, Vector<CompositePart> vecCP, boolean verboseOption){
    for(GeometryPart gp : compPrt.getChildParts().getParts()){
        if(!isCompositePart(gp)) { continue; }
        say("Child Part: " + ((CompositePart) gp).getPathInHierarchy(), verboseOption);
        vecCP.add((CompositePart) gp);
        getCompositeChildren((CompositePart) gp, vecCP, verboseOption);
    }
    return vecCP;
  }

  @Deprecated
  private CompositePart getCompositeParentPart(String name){
    return (CompositePart) sim.get(SimulationPartManager.class).getPart(name);
  }

  /**
   * Loop over all Composite Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Composite Part.
   */
  public CompositePart getCompositePart(String regexPatt){
    return getCompositePart(regexPatt, true);
  }

  private CompositePart getCompositePart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Composite Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (cp.getPresentationName().matches(regexPatt)) {
            say("Got by REGEX: " + cp.getPresentationName(), verboseOption);
            return cp;
        }
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Gets in which level of hierarchy is the given Composite Part.
   *
   * @param compPart given Composite Part.
   * @return Level of hierarchy. (E.g.: Master assembly is level 1).
   */
  public int getCompositePartLevel(CompositePart compPart){
    say("Composite Part: " + compPart.getPathInHierarchy());
    int level = getCompositePartParentLevel(compPart, 1, true);
    say(compPart.getPresentationName() + " is level " + level + ".");
    return level;
  }

  private int getCompositePartParentLevel(CompositePart compPart, int level, boolean verboseOption){
    try{
        CompositePart parent = (CompositePart) compPart.getParentPart();
        say("  Level " + level + ". Parent: " + parent.getPresentationName(), verboseOption);
        level++;
        level = getCompositePartParentLevel(parent, level, verboseOption);
    } catch (Exception e) { }
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

  /**
   * Get all the Feature Curves from a Region.
   *
   * @param region given Region.
   * @return All Feature Curves from that Region.
   */
  public Collection<FeatureCurve> getFeatureCurves(Region region){
    return getFeatureCurves(region, true);
  }

  private Collection<FeatureCurve> getFeatureCurves(Region region, boolean verboseOption){
    say("Getting Feature Curves...", verboseOption);
    sayRegion(region, verboseOption);
    Vector<FeatureCurve> vecFC = (Vector<FeatureCurve>) region.getFeatureCurveManager().getFeatureCurves();
    say("All Feature Curves: " + vecFC.size(), verboseOption);
    return vecFC;
  }

  private VectorComponentFieldFunction getFieldFuncionComponent(PrimitiveFieldFunction ff, int i){
    return (VectorComponentFieldFunction) ff.getComponentFunction(i);
  }

  private VectorMagnitudeFieldFunction getFieldFuncionMagnitude(PrimitiveFieldFunction ff){
    return (VectorMagnitudeFieldFunction) ff.getMagnitudeFunction();
  }

  /**
   * Loop over all Geometry Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt  REGEX search pattern.
   * @return Geometry Part.
   */
  public GeometryPart getGeometryPart(String regexPatt){
    say(String.format("Getting Geometry Part by REGEX pattern: \"%s\"", regexPatt), true);
    GeometryPart gp = getAllGeometryParts(regexPatt, false).iterator().next();
    sayPart(gp);
    return gp;
  }

  @Deprecated
  public Collection<String> getInterfacesBetweenLeafMeshParts(LeafMeshPart lmp1, LeafMeshPart lmp2,
                                                        String renameTo, boolean combinePartFaces){
    Vector<String> vecIntrfc = new Vector<String>();
    printLine();
    say("Getting Interfaces (or common names) between Two Leaf Mesh Parts:");
    printLine();
    say("  Part 1: " + lmp1.getPresentationName());
    say("  Part 2: " + lmp2.getPresentationName());
    Vector<PartSurface> vecPS1 = new Vector<PartSurface>();
    Vector<PartSurface> vecPS2 = new Vector<PartSurface>();
    for(PartSurface ps1 : getAllPartSurfaces(lmp1)){
        for(PartSurface ps2 : getAllPartSurfaces(lmp2)){
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
    say("");
    if(combinePartFaces && vecPS1.size() > 0){
        say("Combining Faces on Both Parts");
        say("  Working on Part: " + lmp1.getPresentationName());
        lmp1.combinePartSurfaces(vecPS1);
        say("  Working on Part: " + lmp2.getPresentationName());
        lmp2.combinePartSurfaces(vecPS2);
    }
    say("\n");
    return vecIntrfc;
  }

  /**
   * Loop over all Leaf Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Leaf Mesh Part.
   */
  public LeafMeshPart getLeafMeshPart(String regexPatt){
    return getLeafMeshPart(regexPatt, true);
  }

  private LeafMeshPart getLeafMeshPart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Leaf Mesh Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (LeafMeshPart lmp : getAllLeafMeshParts(regexPatt, false)) {
        say("Got by REGEX: " + lmp.getPresentationName(), verboseOption);
        return lmp;
    }
    say("Got NULL.");
    return null;
  }

  /**
   * Loop over all Leaf Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt given REGEX search pattern.
   * @return A Geometry Part.
   */
  public GeometryPart getLeafPart(String regexPatt){
    return getLeafPart(regexPatt, true);
  }

  private GeometryPart getLeafPart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Leaf Part by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for(GeometryPart gp : getAllLeafParts(regexPatt, false)){
        say("Got by REGEX: " + gp.getPathInHierarchy(), verboseOption);
        return gp;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  public MeshContinuum getMeshContinua(String name) {
      return ((MeshContinuum) sim.getContinuumManager().getContinuum(name));
  }

  /**
   * Loop over all Mesh Parts and returns the first match, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Mesh Part.
   */
  public MeshPart getMeshPart(String regexPatt){
    return getMeshPart(regexPatt, true);
  }

  private MeshPart getMeshPart(String regexPatt, boolean verboseOption){
    say(String.format("Getting Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    Vector<MeshPart> vecMP = (Vector) getMeshParts(regexPatt, false);
    if (vecMP.size() > 0) {
        say("Got by REGEX: " + vecMP.firstElement().getPathInHierarchy(), verboseOption);
        return vecMP.firstElement();
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Returns all Mesh Parts based on REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return Collection of Mesh Parts.
   */
  public Collection<MeshPart> getMeshParts(String regexPatt){
    return getMeshParts(regexPatt, true);
  }

  private Collection<MeshPart> getMeshParts(String regexPatt, boolean verboseOption){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    say(String.format("Getting Mesh Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    for (MeshPart mp : getAllLeafPartsAsMeshParts()) {
        if(mp.getPathInHierarchy().matches(regexPatt)){
            say("  Found: " + mp.getPathInHierarchy(), verboseOption);
            vecMP.add(mp);
        }
    }
    say("Mesh Parts found: " + vecMP.size(), verboseOption);
    return vecMP;
  }

  /**
   * Get all the N'th level Composite Parts. <p>
   * E.g.: Master Assembly == Level 1; Sub Assembly == Level 2; etc...
   *
   * @param nthLevel given wanted level.
   * @return Collection of all Composite Parts at N'th level.
   */
  public Collection<CompositePart> getNthLevelCompositeParts(int nthLevel){
    Vector<CompositePart> vecNthLevel = new Vector<CompositePart>();
    for (CompositePart cp : getAllCompositeParts(false)) {
        if (getCompositePartLevel(cp) == nthLevel) {
            vecNthLevel.add(cp);
        }
    }
    return vecNthLevel;
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

  /**
   * Gets a Part Surface from a Geometry Part.
   *
   * @param gp given Geometry Part.
   * @param name given name.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String name) {
    for (PartSurface ps : gp.getPartSurfaces()) {
        if(ps.getPresentationName().equals(name)) {
            return ps;
        }
    }
    return null;
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops in all Part Surfaces from Part and returns
   * the first it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String rangeType, String what, double tol) {
    return queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion(), ".*").get(0);
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops into specific Part Surfaces according
   * to REGEX criteria and returns the first it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(GeometryPart gp, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion(), regexPatt).get(0);
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops in all Part Surfaces from Part and returns
   * all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what, double tol) {
    return queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion(), ".*");
  }

  /**
   * Gets a Part Surface based on a Geometric Range. Loops into specific Part Surfaces according
   * to REGEX criteria and returns all it can find.
   *
   * @param gp given Geometry Part. All its Part Surfaces will be used.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(GeometryPart gp, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(gp.getPartSurfaces(), rangeType, what, tol * defUnitLength.getConversion(), regexPatt);
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops in them and returns the first it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(Collection<PartSurface> colPS, String rangeType, String what, double tol) {
    return queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion(), ".*").get(0);
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops into specific Part Surfaces according to REGEX criteria and returns the first it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surface.
   */
  public PartSurface getPartSurface(Collection<PartSurface> colPS, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion(), regexPatt).get(0);
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops in them and returns all it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(Collection<PartSurface> colPS, String rangeType, String what, double tol) {
    return queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion(), ".*");
  }

  /**
   * Gets a Part Surface based on a Geometric Range of the Collection of Part Surfaces provided.
   * Loops into specific Part Surfaces according to REGEX criteria and returns all it can find.
   *
   * @param colPS given Collection of Part Surfaces.
   * @param rangeType given type. Can be <b>Min</b> or <b>Max</b>.
   * @param what what will be queried? Can be <b>X</b>, <b>Y</b>, <b>Z</b> or <b>AREA</b>.
   * @param tol given absolute tolerance for searching in default units (see {@link #defUnitLength}). E.g.: 1mm.
   * @param regexPatt given search pattern.
   * @return The Part Surfaces.
   */
  public Vector<PartSurface> getPartSurfaces(Collection<PartSurface> colPS, String rangeType, String what,
                                                                    double tol, String regexPatt) {
    return queryPartSurfaces(colPS, rangeType, what, tol * defUnitLength.getConversion(), regexPatt);
  }

  @Deprecated
  public PartSurface getPartSurface(CadPart cp, String name){
    return cp.getPartSurfaceManager().getPartSurface(name);
  }

  @Deprecated
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
    for(Object obj : sim.getContinuumManager().getChildren()){
        try{
            PhysicsContinuum phc = (PhysicsContinuum) obj;
            if(phc.getPresentationName().matches(regexPatt)){
                say("Found: " + phc.getPresentationName());
                return phc;
            }
        } catch (Exception e){ continue; }
    }
    say("Found NULL.");
    return null;
  }

  /**
   * Returns the first match of Region, given the REGEX pattern.
   *
   * @param regexPatt REGEX search pattern.
   * @return A Region.
   */
  public Region getRegion(String regexPatt){
    return getRegion(regexPatt, true);
  }

  private Region getRegion(String regexPatt, boolean verboseOption) {
    say(String.format("Getting Region by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Region region = null;
    try {
        region = sim.getRegionManager().getRegion(regexPatt);
        say("Got: " + region.getPresentationName());
        return region;
    } catch (Exception e) { }
    for (Region reg : getAllRegions(regexPatt, false)) {
        say("Got by REGEX: " + reg.getPresentationName(), verboseOption);
        return reg;
    }
    say("Got NULL.", verboseOption);
    return null;
  }

  /**
   * Get the local time.
   *
   * @return String with local time.
   */
  public String getTime() {
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
  }

  /**
   * Gets the STAR-CCM+ version.
   *
   * @return The code version in a x.yy.zzz string format.
   */
  public String getVersion() {
    return sim.getStarVersion().getString("ReleaseNumber");
  }

  @Deprecated
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

  /**
   * Does the Region have Deleted Cells due Remove Invalid Cells?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasDeletedCells(Region region) {
    return hasDeletedCells(region, true);
  }

  private boolean hasDeletedCells(Region region, boolean verboseOption){
    sayRegion(region, verboseOption);
    say("Has came from Remove Invalid Cells command?", verboseOption);
    if (region.getPresentationName().matches("^Cells deleted.*")) {
        sayAnswerYes(verboseOption);
        return true;
    }
    sayAnswerNo(verboseOption);
    return false;
  }

  @Deprecated           //-- Use isInterface() instead.
  public boolean hasInterface(Boundary bdry){
      if (bdry.getDependentInterfaces().size() > 0) {
          return true;
      }
    return false;
  }

  /**
   * Does the current Simulation have Geometry Parts?
   *
   * @return True or False.
   */
  public boolean hasGeometryParts() {
    return hasGeometryParts(true);
  }

  private boolean hasGeometryParts(boolean verboseOption) {
    saySimName(verboseOption);
    say("Has Geometry Parts?", verboseOption);
    if (sim.getGeometryPartManager().isEmpty()) {
        sayAnswerNo(verboseOption);
        return false;
    }
    sayAnswerYes(verboseOption);
    return true;
  }

  /**
   * Does the Region have a Polyhedral mesh?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasPolyMesh(Region region){
    if (hasDeletedCells(region, false) ){ return false; }
    if (region.getMeshContinuum() == null) { return false; }
    if (isPoly(region.getMeshContinuum())) {
        sayRegion(region);
        say(" Has Poly mesh.");
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

  /**
   * Does the Region have a Trimmer mesh?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean hasTrimmerMesh(Region region){
    if (hasDeletedCells(region)) { return false; }
    if (region.getMeshContinuum() == null) { return false; }
    if (isTrimmer(region.getMeshContinuum())) {
        sayRegion(region);
        say(" Has Trimmer mesh.");
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

  /**
   * Imports a CAD file using the MEDIUM tesselation density.
   *
   * @param part given CAD file with extension. <i>E.g.: machine.prt</i>
   *
   */
  public void importCADPart(String part) {
    importCADPart(part, 3, true);
  }

  /**
   * Imports a CAD file using the chose tesselation density.
   *
   * @param part given CAD file with extension. <i>E.g.: machine.prt</i>
   * @param tesselationOption given choice:
   *    1 - Very Coarse,
   *    2 - Coarse,
   *    3 - Medium,
   *    4 - Fine and
   *    5 - Very Fine.
   *
   */
  public void importCADPart(String part, int tesselationOption) {
    importCADPart(part, tesselationOption, true);
  }

  private void importCADPart(String part, int tessOpt, boolean verboseOption) {
    printAction("Importing CAD Part", verboseOption);
    String fName = resolvePath((new File(cadPath, part)).toString());
    say("File: " + fName, verboseOption);
    PartImportManager prtImpMngr = sim.get(PartImportManager.class);
    int type = TessellationDensityOption.MEDIUM;
    switch (tessOpt) {
        case 1:
            type = TessellationDensityOption.VERY_COARSE;
            break;
        case 2:
            type = TessellationDensityOption.COARSE;
            break;
        case 3:
            type = TessellationDensityOption.MEDIUM;
            break;
        case 4:
            type = TessellationDensityOption.FINE;
            break;
        case 5:
            type = TessellationDensityOption.VERY_FINE;
            break;
        default:
            break;
    }
    if(fineTesselationOnImport){
        type = TessellationDensityOption.FINE;
    }
    prtImpMngr.importCadPart(fName, "SharpEdges", mshSharpEdgeAngle, type, false, false);
    sayOK(verboseOption);
  }

  /**
   * Get All CAD Parts in the model and Imprint them using the CAD method.
   */
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

  /**
   * Intersects Two Objects by Discrete Mode.
   *
   * Usually, the Object is a Geometry Part.
   *
   * @param obj1 object 1.
   * @param obj2 object 2.
   * @param renameTo the returned Leaf Mesh Part will be renamed to this.
   * @return A brand new Leaf Mesh Part.
   */
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

  /** Is this Geometry Part a CAD Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCadPart(GeometryPart gp){
    return isBeanDisplayName(gp, "CAD Part");
  }

  /** Is this Geometry Part a Composite Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isCompositePart(GeometryPart gp){
    return isCompositePart(gp, true);
  }

  private boolean isCompositePart(GeometryPart gp, boolean verboseOption){
    boolean isCP = isBeanDisplayName(gp, "Composite Part");
    if (isCP) {
        say("Geometry Part is a Composite Part", verboseOption);
    }
    return isCP;
  }

  /** Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isBlockPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Block Part");
  }

  /** Is this Geometry Part a Simple Cylinder Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleCylinderPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Cylinder Part");
  }

  /**
   * Does this Boundary belong a Direct Boundary Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "DirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Direct Boundary Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isDirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Direct Boundary Interface");
  }

  /**
   * Does this Boundary belongs to Fluid Region?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isFluid(Boundary bdry){
    return isFluid(bdry.getRegion());
  }

  /**
   * Is this a Fluid Region?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean isFluid(Region region){
    if(region.getRegionType().toString().equals("Fluid Region")){
        return true;
    }
    return false;
  }

  /**
   * Is this a Fluid-Solid (Region-Region) Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isFluidSolidInterface(Interface intrfPair){
    Region reg0 = intrfPair.getRegion0();
    Region reg1 = intrfPair.getRegion1();
    if(isFluid(reg0) && isSolid(reg1)) { return true; }
    if(isFluid(reg1) && isSolid(reg0)) { return true; }
    return false;
  }

  /**
   * Is this an Indirect Boundary Interface?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Boundary bdry){
    return isBeanDisplayName(bdry, "IndirectBoundaryInterfaceBoundary");
  }

  /**
   * Is this a Mapped Indirect Interface?
   *
   * @param intrfPair given interface pair.
   * @return True or False.
   */
  public boolean isIndirectBoundaryInterface(Interface intrfPair){
    return isBeanDisplayName(intrfPair, "Indirect Boundary Interface");
  }

  /**
   * Is this an Interface Boundary?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isInterface(Boundary bdry){
    if(isDirectBoundaryInterface(bdry) || isIndirectBoundaryInterface(bdry)){
        return true;
    }
    return false;
  }

  /**
   * Is this Geometry Part a Leaf Mesh Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isLeafMeshPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Leaf Mesh Part");
  }

  /**
   * Does this Boundary belong to a meshing Region?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isMeshing(Boundary bdry){
    return isMeshing(bdry, false);
  }

  private boolean isMeshing(Boundary bdry, boolean verboseOption){
    sayBdry(bdry, verboseOption);
    if(bdry.getRegion().isMeshing()){
        return true;
    }
    say("Region not meshing. Skipping...\n", verboseOption);
    return false;
  }

  /**
   * Is this Geometry Part a Mesh Operation Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isMeshOperationPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Mesh Operation Part");
  }

  /**
   * Is this a Poly Mesh Continua?
   *
   * @param mshCont given Mesh Continua.
   * @return True or False.
   */
  public boolean isPoly(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.dualmesher.DualMesherModel")){
        return true;
    }
    return false;
  }

  /**
   * Has this Mesh Continua a Remesher Model?
   *
   * @param mshCont given Mesh Continua.
   * @return True or False.
   */
  public boolean isRemesh(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.resurfacer.ResurfacerMeshingModel")){
        return true;
    }
    return false;
  }

  /**
   * Is this Geometry Part a Simple Block Part?
   *
   * @param gp given Geometry Part.
   * @return True or False.
   */
  public boolean isSimpleBlockPart(GeometryPart gp){
    return isBeanDisplayName(gp, "Block Part");
  }

  /**
   * Does this Boundary belong to a Solid Region?
   *
   * @param bdry given Boundary.
   * @return True or False.
   */
  public boolean isSolid(Boundary bdry){
    return isSolid(bdry.getRegion());
  }

  /**
   * Is this a Solid Region?
   *
   * @param region given Region.
   * @return True or False.
   */
  public boolean isSolid(Region region){
    if(region.getRegionType().toString().equals("Solid Region")){
        return true;
    }
    return false;
  }

  /**
   * Is this a Trimmer Mesh Continua?
   *
   * @param mshCont given Mesh Continua.
   * @return True or False.
   */
  public boolean isTrimmer(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.trimmer.TrimmerMeshingModel")){
        return true;
    }
    return false;
  }

  /**
   * Is this a Wrapper Mesh Continua?
   *
   * @param mshCont given Mesh Continua.
   * @return True or False.
   */
  public boolean isWrapper(MeshContinuum mshCont){
    if(mshCont.getEnabledModels().containsKey("star.surfacewrapper.SurfaceWrapperMeshingModel")){
        return true;
    }
    return false;
  }

  /**
   * Is this a Wall Boundary?
   *
   * @param bdry given boundary.
   * @return True or False.
   */
  public boolean isWallBoundary(Boundary bdry){
    String t1 = "Wall";
    String t2 = "Contact Interface Boundary";
    String type = bdry.getBoundaryType().toString();
    if(type.equals(t1) || type.equals(t2)){ return true; }
    return false;
  }

  private int max(int a, int b) {
    return Math.max(a,b);
  }

  /**
   * Creates a Mesh Operation of Subtraction between a set of Geometry Parts.
   *
   * @param colGP given Collection of Geometry Parts.
   * @param tgtGP given target  Geometry Part.
   * @return The Mesh Operation Part.
   */
  public MeshOperationPart meshOperationSubtractParts(Collection<GeometryPart> colGP, GeometryPart tgtGP) {
    return meshOperationSubtractParts(colGP, tgtGP, true);
  }

  private MeshOperationPart meshOperationSubtractParts(Collection<GeometryPart> colGP, GeometryPart tgtGP, boolean verboseOption) {
    printAction("Doing a Mesh Operation: Subtract", verboseOption);
    sayParts(colGP, verboseOption);
    SubtractPartsOperation spo = (SubtractPartsOperation) sim.get(MeshOperationManager.class).createSubtractPartsOperation();
    spo.getInputGeometryObjects().setObjects(colGP);
    spo.setTargetPart((MeshPart) tgtGP);
    spo.execute();
//    say(tgtGP.getBeanDisplayName());
//    say(spo.getChildren().toString());
    String opName = retStringBetweenBrackets(spo.getOutputPartNames());
//    say(spo.getOutputParts().getChildren().toString());
//    say(spo.getChildren().iterator().next());
    return ((MeshOperationPart) sim.get(SimulationPartManager.class).getPart(opName));
//    return ((MeshOperationPart) spo.getChildren().iterator().next());
  }

  private int min(int a, int b) {
    return Math.min(a,b);
  }

  /**
   * Opens a Monitor Plot in STAR-CCM+ GUI.
   *
   * @param monPl given Monitor Plot.
   */
  public void openPlot(MonitorPlot monPl) {
    StarPlot sp = sim.getPlotManager().getPlot(monPl.getPresentationName());
    sp.serverOpen();
  }

  /**
   * Plays a JAVA Macro.
   *
   * @param macro filename to be played without extension.
   */
  public void playMacro(String macro) {
    String macroFile = macro;
    if (!macro.matches(".*java$")) {
        macroFile = macro + ".java";
    }
    printAction("MACRO: " + macroFile);
    new StarScript(sim, new java.io.File(resolvePath(macroFile))).play();
  }

  /**
   * Prints an action with a fancy frame.
   *
   * @param text message to be printed.
   */
  public void printAction(String text) {
    printAction(text, true);
  }

  private void printAction(String text, boolean verboseOption){
    updateSimVar();
    say("");
    printLine();
    say("+ " + getTime());
    say("+ " + text);
    printLine();
  }

  /**
   * Prints a fancier frame with text in it.
   *
   * @param text message to be printed.
   */
  public void printFrame(String text){
    updateSimVar();
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

  /**
   * Prints a line.
   */
  public void printLine(){
    printLine(1);
  }

  /**
   * Prints a line 'n' times.
   *
   * @param n how many times the line will be printed.
   */
  public void printLine(int n){
    for (int i = 1; i <= n; i++) {
        printLine("-");
        //say("+-----------------------------------------------------------------------------");
    }
  }

  private void printLine(String _char) {
    String cc = _char;
    if ( _char.equals("-") ) {
        cc = "+";
    }
    say(cc + new String(new char[80]).replace("\0", _char));
  }

  /**
   * An overview of the mesh parameters.
   */
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

  /*
   * An overview of the Prism Mesh Parameters.
   */
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

  /*
   * An overview of the Solver Parameters.
   */
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

  /**
   * Get the Geometry Part Representation.
   *
   * @return Geometry Part Representation.
   */
  public PartRepresentation queryGeometryRepresentation(){
      return ((PartRepresentation) sim.getRepresentationManager().getObject("Geometry"));
  }

  /**
   * Get the Import Surface Representation.
   *
   * @return Import Surface Representation.
   */
  public SurfaceRep queryImportRepresentation(){
      return querySurfaceRep("Import");
  }

  /**
   * Get the Initial Surface Representation.
   *
   * @return Initial Surface Representation.
   */
  public SurfaceRep queryInitialSurface(){
    return querySurfaceRep("Initial Surface");
  }

  /** When querying by AREA it will give the PSs always based on first element (0). **/
  private Vector<PartSurface> queryPartSurfaces(Collection<PartSurface> colPS, String rangeType,
                                                    String what, double tol, String regexPatt) {
    //--
    if (colPS.size() == 0) {
        say("No Part Surfaces Provided for Querying. Returning NULL!");
        return null;
    }
    GeometryPart gp = colPS.iterator().next().getPart();
    //-- Some declarations first.
    final String rangeOpts[] = {"MIN", "MAX"};
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    Vector<Double> vecArea = new Vector<Double>();
    //Vector<Integer> vecIDs = new Vector<Integer>();
    Vector choices = new Vector(Arrays.asList(xyzCoord));
    DoubleVector labMinXYZ = null, labMaxXYZ = null, psMinXYZ = null, psMaxXYZ = null;
    int rangeChoice = -1, whatChoice = -1;
    boolean proceed = true;
    //--
    //-- Headers
    printLine(2);
    printAction("Querying Part Surfaces: " + rangeType + " " + what);
    printLine(2);
    sayPart(gp);
    //--
    //-- Just some checkings before moving on
    try {
        rangeChoice = Arrays.asList(rangeOpts).indexOf(rangeType.toUpperCase());
    } catch (Exception e) {
        proceed = false;
    }
    try {
        choices.add("AREA");
        whatChoice = choices.indexOf(what.toUpperCase());
    } catch (Exception e) {
        proceed = false;
    }
    if (!proceed) {
        say("Got NULL!");
        return null;
    }
    //--
    //-- Init Widget
    scene = createScene_Geometry(false);
    PartSurfaceMeshWidget psmw = queryGeometryRepresentation().startSurfaceMeshWidget(scene);
    psmw.setActiveParts(new NeoObjectVector(new Object[] {gp}));
    queryPartSurfaces_initPartSurfaceMeshWidget(psmw);
    //--
    //-- Add the Part Surfaces
    NeoObjectVector psObjs = new NeoObjectVector(colPS.toArray());
    if (isSimpleCylinderPart(gp)) {
        ((SimpleCylinderPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
    } else if (isSimpleBlockPart(gp)) {
        ((SimpleBlockPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
    } else if (isMeshOperationPart(gp)) {
        ((MeshOperationPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
    } else if (isCadPart(gp)) {
        ((CadPart) gp).getPartSurfacesSharingPatches(psmw, psObjs, emptyNeoObjVec);
    }
    //--
    //-- Init Query
    SurfaceMeshWidgetSelectController smwsc = psmw.getControllers().getController(SurfaceMeshWidgetSelectController.class);
    smwsc.selectPartSurfaces(psObjs);
    SurfaceMeshWidgetQueryController smwqc = psmw.getControllers().getController(SurfaceMeshWidgetQueryController.class);
    NeoProperty retRange = null;
    NeoProperty retArea = null;
    //--
    //-- Overall Stats
    printLine(4);
    say("Global Info: " + gp.getPresentationName());
    printLine(2);
    if(what.equals("AREA")) {
        retArea = smwqc.queryFaceArea();
    } else {
        retRange = smwqc.queryFaceGeometricRange();
        labMinXYZ = retRange.getDoubleVector("LabMinRange");
        labMaxXYZ = retRange.getDoubleVector("LabMaxRange");
    }
    printLine(4);
    //--
    double val = 0.0;
    for (PartSurface ps : getAllPartSurfaces(gp, regexPatt, false)) {
        //--
        smwsc.clearSelected();
        smwsc.selectPartSurface(ps);
        printLine();
        sayPartSurface(ps);
        printLine();
        //--
        if(what.equals("AREA")) {
            retArea = smwqc.queryFaceArea();
            val = retArea.getDouble("TotalFaceArea");
            if (vecPS.isEmpty()) {
                vecPS.add(ps);
                vecArea.add(val);
            } else {
                int i = 0;
                for (Iterator<Double> it = ((Vector) vecArea.clone()).iterator(); it.hasNext();) {
                    Double storedVal = it.next();
                    if (val > storedVal) {
                        vecPS.insertElementAt(ps, i);
                        vecArea.insertElementAt(val, i);
                    } else if (!it.hasNext()) {
                        vecPS.add(ps);
                        vecArea.add(val);
                    } //-- Higher AREAs go first.
                    i++;
                }
            }
        } else {
            boolean isLocal = false;
            boolean isGlobal = false;
            retRange = smwqc.queryFaceGeometricRange();
            psMinXYZ = retRange.getDoubleVector("LabMinRange");
            psMaxXYZ = retRange.getDoubleVector("LabMaxRange");
            isLocal = retDiff(psMinXYZ.get(whatChoice), psMaxXYZ.get(whatChoice), tol);
            if (isLocal) {
                say("Found Local " + what + " = " + psMinXYZ.get(whatChoice));
            }
            //say("Range Choice: " + rangeChoice);
            switch (rangeChoice) {
                case 0:
                    isGlobal = retDiff(psMinXYZ.get(whatChoice), labMinXYZ.get(whatChoice), tol);
                    val = psMinXYZ.get(whatChoice);
                    break;
                case 1:
                    isGlobal = retDiff(psMaxXYZ.get(whatChoice), labMaxXYZ.get(whatChoice), tol);
                    val = psMaxXYZ.get(whatChoice);
                    break;
            }
            if (isLocal && isGlobal) {
                say(String.format("Found Global %s %s = ", rangeOpts[rangeChoice], choices.get(whatChoice)) + val);
                vecPS.add(ps);
            }
        }
        say("");
    }
    if(what.equals("AREA") && rangeType.equals("MIN")) {
        Collections.reverse(vecPS);
    }
    psmw.stop();
    sim.getSceneManager().deleteScene(scene);
    printLine();
    say(String.format("Found %d Part Surfaces matching %s %s.", vecPS.size(), rangeOpts[rangeChoice], choices.get(whatChoice)));
    printLine();
    return vecPS;
  }

  private void queryPartSurfaces_initPartSurfaceMeshWidget(PartSurfaceMeshWidget psmw) {
    psmw.startSurfaceMeshDiagnostics();
    psmw.startSurfaceMeshRepair();
    psmw.startMergeImprintController();
    psmw.startIntersectController();
    psmw.startLeakFinderController();
    psmw.startSurfaceMeshQueryController();
    SurfaceMeshWidgetDiagnosticsController smwdc = psmw.getControllers().getController(SurfaceMeshWidgetDiagnosticsController.class);
    smwdc.setCheckPiercedFaces(false);
    smwdc.setPiercedFacesActive(false);
    smwdc.setCheckPoorQualityFaces(false);
    smwdc.setPoorQualityFacesActive(false);
    smwdc.setMinimumFaceQuality(0.01);
    smwdc.setCheckCloseProximityFaces(false);
    smwdc.setCloseProximityFacesActive(false);
    smwdc.setMinimumFaceProximity(0.05);
    smwdc.setCheckFreeEdges(false);
    smwdc.setFreeEdgesActive(false);
    smwdc.setCheckNonmanifoldEdges(false);
    smwdc.setNonmanifoldEdgesActive(false);
    smwdc.setCheckNonmanifoldVertices(false);
    smwdc.setNonmanifoldVerticesActive(false);
    smwdc.setCheckFeatureNumberEdges(true);
    smwdc.setCheckFeatureLength(false);
    smwdc.setCheckFeatureAngle(true);
    smwdc.setCheckFeatureOpenCurve(true);
    smwdc.setCheckFeatureSmallRegions(false);
    smwdc.setMinNumberEdges(5);
    smwdc.setMinFeatureLength(1.0E-4);
    smwdc.setMaxFeatureAngle(121.0);
    smwdc.setMinFeatureRegionArea(1.0E-4);
    smwdc.setCheckSoftFeatureErrors(false);
    smwdc.setSoftFeatureErrorsActive(false);
    smwdc.setCheckHardFeatureErrors(false);
    smwdc.setHardFeatureErrorsActive(false);
    //smwdc.changeDisplayedThresholds(new NeoObjectVector(new Object[] {}), true);
  }

  /**
   * Get the Remeshed Surface Representation.
   *
   * @return Remeshed Surface Representation.
   */
  public SurfaceRep queryRemeshedSurface(){
    return querySurfaceRep("Remeshed Surface");
  }

  private SurfaceRep querySurfaceRep(String name){
    if(sim.getRepresentationManager().has(name)){
        return (SurfaceRep) sim.getRepresentationManager().getObject(name);
    }
    return null;
  }

  /**
   * Get the Wrapped Surface Representation.
   *
   * @return Wrapped Surface Representation.
   */
  public SurfaceRep queryWrappedSurface(){
    return querySurfaceRep("Wrapped Surface");
  }

  private Units queryUnit(String unitString){
    try{
        Units unit = ((Units) sim.getUnitsManager().getObject(unitString));
        say("Unit read: " + unit.toString());
        return unit;
    } catch (Exception e) {
        say("Unit not read: " + unitString);
        return null;
    }
  }

  /**
   * Get the Finite Volume Representation.
   *
   * @return Finite Volume Representation.
   */
  public FvRepresentation queryVolumeMesh(){
    if(sim.getRepresentationManager().has("Volume Mesh")){
        return (FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh");
    }
    return null;
  }

  private void rebuildCompositeChildren(CompositePart cp, String splitChar){
    CompositePart newCP = null;
    say("Looking in: " + cp.getPresentationName());
    String splitChar0 = splitChar;
    if(splitChar.equals("\\") || splitChar.equals("|")){
        splitChar = "\\" + splitChar;
    }
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
            say("Creating Composite Part: " + name0);
            newCP = cp.getChildParts().createCompositePart();
            newCP.setPresentationName(name0);
        }
        gp.setPresentationName(gpNewName);
            say("Parenting: ");
            say("  + " + newCP.getPresentationName());
            say("  |---+ " + gp.getPresentationName());
            sayOldNameNewName(name, gpNewName);
            gp.reparent(newCP.getChildParts());
            rebuildCompositeChildren(newCP, splitChar0);
    }
  }

  /**
   * Tries to rebuild the hierarchy of a Composite Part based on a split character.
   *
   * @param compPart given Composite Part.
   * @param splitChar given split character. E.g.: |, +, etc...
   */
  public void rebuildCompositeHierarchy(CompositePart compPart, String splitChar){
    printAction("Rebuilding Composite Assembly Hierarchy based on a split character");
    say("Composite Part: " + compPart.getPresentationName());
    rebuildCompositeChildren(compPart, splitChar);
    sayOK();
  }

  /**
   * Rebuilds all interfaces in the model.
   */
  public void rebuildAllInterfaces() {
    printAction("Rebuilding all Interfaces of the Model");
    Vector vecBdryD = new Vector();
    Vector vecBdryD_tol = new Vector();
    Vector vecBdryI = new Vector();
    Vector vecBdryI_tol = new Vector();
    Vector<Boundary> vecBdryD0 = new Vector<Boundary>();
    Vector<Boundary> vecBdryD1 = new Vector<Boundary>();
    Vector<Boundary> vecBdryI0 = new Vector<Boundary>();
    Vector<Boundary> vecBdryI1 = new Vector<Boundary>();
    Boundary b0, b1;
    DirectBoundaryInterface dbi;
    IndirectBoundaryInterface ibi;
    say("Looping over all interfaces and reading data...");
    for(Interface intrf : getAllInterfaces(false)){
        String name = intrf.getPresentationName();
        String beanName = intrf.getBeanDisplayName();
        say(String.format("  Reading: %-40s[%s]", name, beanName));
        if (isDirectBoundaryInterface(intrf)) {
            dbi = (DirectBoundaryInterface) intrf;
            vecBdryD.add(name);
            vecBdryD_tol.add(dbi.getValues().get(InterfaceToleranceCondition.class).getTolerance());
            vecBdryD0.add(dbi.getParentBoundary0());
            vecBdryD1.add(dbi.getParentBoundary1());
        }
        if (isIndirectBoundaryInterface(intrf)) {
            ibi = (IndirectBoundaryInterface) intrf;
            vecBdryI.add(name);
            vecBdryI_tol.add(ibi.getValues().get(MappedInterfaceToleranceCondition.class).getProximityTolerance());
            vecBdryI0.add(ibi.getParentBoundary0());
            vecBdryI1.add(ibi.getParentBoundary1());
        }
    }
    clearInterfaces(false);
    say("Recreating " + vecBdryD.size() + " Direct Interfaces...");
    for (int i = 0; i < vecBdryD.size(); i++) {
        String name = (String) vecBdryD.elementAt(i);
        double tol = (Double) vecBdryD_tol.elementAt(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = vecBdryD0.elementAt(i);
        b1 = vecBdryD1.elementAt(i);
        dbi = sim.getInterfaceManager().createDirectInterface(b0, b1);
        dbi.getValues().get(InterfaceToleranceCondition.class).setTolerance(tol);
        dbi.setPresentationName(name);
    }
    say("Recreating " + vecBdryI.size() + " Indirect Interfaces...");
    for (int i = 0; i < vecBdryI.size(); i++) {
        String name = (String) vecBdryI.elementAt(i);
        double tol = (Double) vecBdryI_tol.elementAt(i);
        say(String.format("  %3d: %s", (i+1), name));
        b0 = vecBdryI0.elementAt(i);
        b1 = vecBdryI1.elementAt(i);
        ibi = sim.getInterfaceManager().createIndirectInterface(b0, b1);
        ibi.getValues().get(MappedInterfaceToleranceCondition.class).setProximityTolerance(tol);
        ibi.setPresentationName(name);
    }
    sayOK();
  }

  /**
   * Removes all Part Contacts.
   */
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

  /**
   * Removes a CAD part. <i>Note that it must belong to a Composite Part.</i>
   *
   * @param name part name
   */
  public void removeCadPart(String name){
    printAction("Removing Cad Part: " + name);
    CadPart cadPrt = getCadPart(name);
    CompositePart compPart = (CompositePart) cadPrt.getParentPart();
    compPart.getChildParts().removePart(cadPrt);
    say("Removed: " + cadPrt.getPathInHierarchy());
    if(cadPrt == null){
        say("CadPart not found: " + name);
    }
    sayOK();
  }

  /**
   * Removes a Composite Part.
   *
   * @param compPart given Composite Part.
   */
  public void removeCompositePart(CompositePart compPart){
    removeCompositePart(compPart, true);
  }

  private int removeCompositePart(CompositePart compPart, boolean verboseOption){
    printAction("Removing a Composite Part", verboseOption);
    try{
        CompositePart parent = ((CompositePart) compPart.getParentPart());
        say("Removing Composite: " + compPart.getPresentationName());
        parent.getChildParts().remove(compPart);
        sayOK(verboseOption);
        return 0;
    } catch (Exception e){
        say("ERROR! Could not remove Composite Part.");
    }
    return 1;
  }

  /**
   * Removes a group of Composite Parts.
   *
   * @param colCompParts given Collection of Composite Parts.
   */
  public void removeCompositeParts(Collection<CompositePart> colCompParts){
    removeCompositeParts(colCompParts, true);
  }

  /**
   * Removes a group of Composite Parts based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void removeCompositeParts(String regexPatt) {
    printAction(String.format("Removing Composite Parts based on REGEX pattern: \"%s\"", regexPatt ));
    int n = 0;
    for (CompositePart cP : getAllCompositeParts(regexPatt, false)){
        if (removeCompositePart(compPart, false) == 0) { n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK();
  }

  private void removeCompositeParts(Collection<CompositePart> colCompParts, boolean verboseOption){
    printAction("Removing Composite Parts", verboseOption);
    int n = 0;
    say("Composite Parts to be removed: " + colCompParts.size());
    for (CompositePart cp : colCompParts) {
        int ret = removeCompositePart(cp, false);
        if (ret == 0) { n++; }
    }
    say("Composite Parts removed: " + n);
    sayOK(verboseOption);
  }

  /**
   * Removes all the Feature Curves of a Region. Useful when using the Surface Wrapper.
   *
   * @param region given Region.
   */
  public void removeFeatureCurves(Region region) {
    printAction("Removing all Feature Curves");
    sayRegion(region);
    region.getFeatureCurveManager().deleteChildren(region.getFeatureCurveManager().getFeatureCurves());
    sayOK();
  }

  /**
   * Removes a Geometry Part.
   *
   * @param gp given Geometry Part.
   */
  public void removeGeometryPart(GeometryPart gp) {
    removeGeometryPart(gp, true);
  }

  private void removeGeometryPart(GeometryPart gp, boolean verboseOption) {
    printAction("Removing a Geometry Part", verboseOption);
    say("Geometry Part: " + gp.getPresentationName());
    if (isCompositePart(gp)) {
        removeCompositePart((CompositePart) gp, false);
    } else {
        try{
            sim.get(SimulationPartManager.class).remove(gp);
            say("Geometry Part removed.");
        } catch(Exception e){
            say("ERROR! Could not remove Geometry Part.");
        }
    }
  }

  public void removeGeometryParts(String regexPatt){
    printAction(String.format("Removing Geometry Parts based on REGEX criteria: \"%s\"", regexPatt));
    for (GeometryPart gp : getAllGeometryParts(regexPatt, false)) {
        removeGeometryPart(gp, false);
    }
    sayOK();
  }

  /**
   * Removes the Invalid Cells in the model.
   */
  public void removeInvalidCells(){
    Vector<Region> regionsPoly = new Vector<Region>();
    Vector<Region> regionsTrimmer = new Vector<Region>();
    Vector<Region> fvRegions = new Vector<Region>();
    for (Region region : getAllRegions(false)) {
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
    /* Removing From fvRepresentation */
    if (fvRegions.size() > 0) {
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

  /**
   * Removes a Leaf Mesh Part.
   *
   * @param lmp given Leaf Mesh Part.
   */
  public void removeLeafMeshPart(LeafMeshPart lmp){
    removeLeafMeshPart(lmp, true);
  }

  private void removeLeafMeshPart(LeafMeshPart lmp, boolean verboseOption) {
    String name = lmp.getPresentationName();
    printAction("Removing Leaf Mesh Part: " + name, verboseOption);
    sim.get(SimulationPartManager.class).removePart(lmp);
    say("Removed: " + name);
  }

  /**
   * Removes a group of Leaf Mesh Parts.
   *
   * @param colLMP given Collection of Leaf Mesh Parts.
   */
  public void removeLeafMeshParts(Collection<LeafMeshPart> colLMP){
    removeLeafMeshParts(colLMP, true);
  }

  /**
   * Removes all Leaf Mesh Parts given REGEX search pattern.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void removeLeafMeshParts(String regexPatt){
    printAction(String.format("Removing Leaf Meshs Part by REGEX pattern: \"%s\"", regexPatt));
    removeLeafMeshParts(getAllLeafMeshParts(regexPatt, false), true);
  }

  private void removeLeafMeshParts(Collection<LeafMeshPart> colLMP, boolean verboseOption){
    printAction("Removing Leaf Mesh Parts", verboseOption);
    say("Leaf Mesh Parts to be removed: " + colLMP.size(), verboseOption);
    for (LeafMeshPart lmp : colLMP) {
        removeLeafMeshPart(lmp, false);
    }
    sayOK(verboseOption);
  }

  /**
   * Removes all Leaf Parts given REGEX search pattern.
   *
   * @param regexPatt given REGEX pattern.
   */
  public void removeLeafParts(String regexPatt){
    removeLeafParts(regexPatt, true);
  }

  private void removeLeafParts(String regexPatt, boolean verboseOption){
    printAction(String.format("Remove Leaf Parts by REGEX pattern: \"%s\"", regexPatt), verboseOption);
    Collection<GeometryPart> colLP = getAllLeafParts(regexPatt, false);
    say("Leaf Parts to be removed: " + colLP.size());
    for (GeometryPart gp : colLP) {
        removeGeometryPart(gp, false);
    }
    sayOK(verboseOption);
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

  /**
   * Removes a Geometry Part that belongs to a Composite.
   *
   * @param gp
   */
  public void removeGeometryPartInComposite(GeometryPart gp) {
    removeGeometryPartInComposite(gp, true);
  }

  /**
   * Removes Geometry Parts that belongs to Composite Parts according to REGEX search.
   *
   * @param regexPatt given search criteria.
   */
  public void removeGeometryPartInComposite(String regexPatt) {
    printAction("Removing Geometry Parts that belong to Composite Parts");
    for (GeometryPart gp : getAllLeafParts(regexPatt, false)) {
        say("Removing: " + gp.getPathInHierarchy());
        removeGeometryPartInComposite(gp, false);
    }
    sayOK();
  }

  private void removeGeometryPartInComposite(GeometryPart gp, boolean verboseOption) {
    printAction("Removing a Geometry Part which belongs to a Composite Part", verboseOption);
    say("Removing: " + gp.getPathInHierarchy(), verboseOption);
    ((CompositePart) gp.getParentPart()).getChildParts().removePart(gp);
    sayOK(verboseOption);
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
            String newName = retStringBetweenBrackets(name);
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

  /**
   * Renames Part Surfaces based on 2 REGEX search patterns. <p>
   * It looks for all Part Surfaces that has both search Strings and rename it accordingly.
   *
   * @param hasString1 given REGEX search pattern 1.
   * @param hasString2 given REGEX search pattern 2.
   * @param renameTo given new name of the found Part Surfaces.
   */
  public void renamePartSurfaces(String hasString1, String hasString2, String renameTo){
    printAction("Renaming Part Surface(s)");
    for (PartSurface ps : getAllPartSurfaces(".*", false)) {
        String name = ps.getPresentationName();
        if (name.matches(hasString1) && name.matches(hasString2)) {
            sayOldNameNewName(name, renameTo);
            ps.setPresentationName(renameTo);
        }
    }
    sayOK();
  }

  /**
   * Searches for a Boundary in given Region and rename it to a new name.
   *
   * @param region given Region.
   * @param regexPatt given REGEX search pattern.
   * @param renameTo given new Boundary name.
   */
  public void renameBoundary(Region region, String regexPatt, String renameTo) {
    renameBoundary(region, regexPatt, renameTo, true);
  }

  private void renameBoundary(Region region, String regexPatt, String renameTo, boolean verboseOption) {
    printAction("Renaming Boundary", verboseOption);
    sayRegion(region);
    Boundary bdry = getBoundary(region, regexPatt, false);
    sayOldNameNewName(bdry.getPresentationName(), renameTo);
    bdry.setPresentationName(renameTo);
    sayOK(verboseOption);
  }

  /**
   * When the Boundary names are preceeded by the Region name, this method will remove the prefix from
   * all Boundary names.
   *
   * @param region given Region.
   */
  public void resetBoundaryNames(Region region){
    printAction("Removing Prefixes from Boundary Names");
    sayRegion(region);
    for (Boundary bdry : region.getBoundaryManager().getBoundaries()) {
        String name = bdry.getPresentationName();
        String newName = name.replace(region.getPresentationName() + ".", "");
        sayOldNameNewName(name, newName);
        bdry.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets the Interface names to show the dependent Region names and types. <p>
   * E.g.: <i>F-S: Pipe <-> Metal</i> means that is a Fluid-Solid Interface between Pipe and Metal
   * Regions.
   */
  public void resetInterfaceNames() {
    printAction("Resetting Interface Names...");
    Collection<Interface> colInt = getAllInterfaces(false);
    say("Number of Interfaces: " + colInt.size());
    for (Interface intrf : colInt) {
        String name0 = intrf.getRegion0().getPresentationName();
        String type0 = "F";
        if (isSolid(intrf.getRegion0())) {
            type0 = "S";
        }
        String name1 = intrf.getRegion1().getPresentationName();
        String type1 = "F";
        if (isSolid(intrf.getRegion1())) {
            type1 = "S";
        }
        String newName = String.format("%s-%s: %s <-> %s", type0, type1, name0, name1);
        sayOldNameNewName(intrf.getPresentationName(), newName);
        intrf.setPresentationName(newName);
    }
    sayOK();
  }

  /**
   * Resets all Solver Settings to the default values.
   */
  public void resetSolverSettings() {
    printAction("Resetting All URFs...");
    maxIter = maxIter0;
    urfP = urfP0;
    urfVel = urfVel0;
    urfKEps = urfKEps0;
    urfFluidEnrgy = urfFluidEnrgy0;
    urfSolidEnrgy = urfSolidEnrgy0;
    updateSolverSettings();
  }


  /**
   * Reset the Surface Remesher to default conditions.
   *
   * @param mshCont
   */
  public void resetSurfaceRemesher(MeshContinuum mshCont){
    enableSurfaceRemesher(mshCont);
    enableSurfaceProximityRefinement(mshCont);
    enableSurfaceRemesherAutomaticRepair(mshCont);
    enableSurfaceRemesherProjectToCAD(mshCont);
  }

  /**
   * Retesselate the CAD Part to Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToFine(CadPart part){
    printAction("ReTesselating a Part To Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.FINE);
    sayOK();
  }

  /**
   * Retesselate the CAD Part to Very Fine triangulation.
   *
   * @param part given CAD Part.
   */
  public void reTesselateCadPartToVeryFine(CadPart part){
    printAction("ReTesselating a Part To Very Fine Mesh");
    reTesselateCadPart(part, TessellationDensityOption.VERY_FINE);
    sayOK();
  }

  private void reTesselateCadPart(CadPart part, int type){
    sayPart(part);
    part.getTessellationDensityOption().setSelected(type);
    part.getCadPartEdgeOption().setSelected(CadPartEdgeOption.SHARP_EDGES);
    part.setSharpEdgeAngle(mshSharpEdgeAngle);
    part.tessellate();
    sayOK();
  }

  /** Returns whether the absolute difference between 2 doubles is within the tolerance. */
  private boolean retDiff(double d1, double d2, double tol) {
    if (Math.abs(d1 - d2) <= tol) {
        return true;
    }
    return false;
  }

  /** Returns whether the relative error between 2 doubles is within the tolerance. */
  private boolean retError(double d1, double d2, double tol) {
    double diff = d1 - d2;
    double div = d2;
    if (diff == 0.) {
        div = 1;                // Any number
    } else if (d1*d2 == 0.) {
        div = d1 + d2;          // Any of the two
    }
    double error = diff/div;
    //say("d1 = " + d1);
    //say("d2 = " + d2);
    //say("Error: " + error);
    if (Math.abs(error) <= tol) {
        return true;
    }
    return false;
  }

  private String retMinTargetString(double min, double tgt){
    String minS = String.format("%.2f%%", min);
    String tgtS = String.format("%.2f%%", tgt);
    return "Min/Target = " + minS + "/" + tgtS;
  }

  private String retStringBetweenBrackets(String text) {
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

  private String retStringBoundaryAndRegion(Boundary bdry){
    Region region = bdry.getRegion();
    return region.getPresentationName() + "\\" + bdry.getPresentationName();
  }

  private String retString(int number){
      return String.valueOf(number);
  }

  private String retTemp(double T){
    return "Temperature: " + T + defUnitTemp.getPresentationName();
  }

  /**
   * Run the case.
   */
  public void runCase(){
    runCase(0);
  }

  /**
   * Run/Step the case for many iterations.
   *
   * @param n If n > 0: step n iterations; If n == 0: just run.
   */
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

  /**
   * Print something to output/log file.
   *
   * @param msg message to be printed
   */
  public void say(String msg){
    say(msg, true);
  }

  private void say(String msg, boolean verboseOption){
    if(!verboseOption){ return; }
    sim.println(sayPreffixString + " " + msg);
  }

  private void sayAnswerNo(boolean verboseOption) {
    say("  NO", verboseOption);
  }

  private void sayAnswerYes(boolean verboseOption) {
    say("  YES", verboseOption);
  }

  private void sayBdry(Boundary bdry) {
    sayBdry(bdry, true);
  }

  private void sayBdry(Boundary bdry, boolean verboseOption) {
    sayBdry(bdry, true, true);
  }

  private void sayBdry(Boundary bdry, boolean verboseOption, boolean beanDisplayOption){
    String b = bdry.getPresentationName();
    String r = bdry.getRegion().getPresentationName();
    say("Boundary: " + b + "\t[Region: " + r + "]", verboseOption);
    if(!beanDisplayOption){ return; }
    say("Bean Display Name is \"" + bdry.getBeanDisplayName() + "\".", verboseOption);
  }

  private void sayBoundaries(Collection<Boundary> boundaries) {
    say("Number of Boundaries: " + boundaries.size());
    for (Boundary bdry : boundaries) {
        sayBdry(bdry, true, false);
    }
  }

  private void sayInterface(Interface intrfPair) {
    sayInterface(intrfPair, true, true);
  }

  private void sayInterface(Interface intrfPair, boolean verboseOption, boolean beanDisplayOption) {
    say("Interface: " + intrfPair.getPresentationName(), verboseOption);
    if(beanDisplayOption){
        say("Bean Display Name is \"" + intrfPair.getBeanDisplayName() + "\".", verboseOption);
    }
    sayInterfaceSides(intrfPair, verboseOption);
  }

  private void sayInterfaceSides(Interface intrfPair) {
    sayInterfaceSides(intrfPair, true);
  }

  private void sayInterfaceSides(Interface intrfPair, boolean verboseOption) {
    if(!verboseOption){ return; }
    String side1 = null, side2 = null;
    if(isDirectBoundaryInterface(intrfPair)){
        side1 = retStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((DirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    if(isIndirectBoundaryInterface(intrfPair)){
        side1 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary0());
        side2 = retStringBoundaryAndRegion(((IndirectBoundaryInterface) intrfPair).getParentBoundary1());
    }
    say("  Side1: " + side1);
    say("  Side2: " + side2);
  }

  /**
   * Print something in Capital Letters.
   *
   * @param msg message to be said.
   */
  public void sayLoud(String msg) {
    printLine("#");
    printLine("#");
    say("#  " + msg.toUpperCase());
    printLine("#");
    printLine("#");
  }

  private void sayMeshContinua(MeshContinuum mshCont) {
    sayMeshContinua(mshCont, true);
  }

  private void sayMeshContinua(MeshContinuum mshCont, boolean verboseOption) {
    say("Mesh Continua: " + mshCont.getPresentationName(), verboseOption);
  }

  private void saySimName(boolean verboseOption) {
    say("Simulation Name: " + sim.getPresentationName(), verboseOption);
  }

  /**
   * Outputs a simple simulation overview.
   */
  private void saySimOverview() {
    saySimName(true);
    say("Simulation File: " + simFile.toString());
    say("Simulation Path: " + simPath);
  }

  /**
   * It's OK!
   */
  public void sayOK(){
    sayOK(true);
  }

  /**
   * It's OK!
   *
   * @param verboseOption should I really output that?
   */
  public void sayOK(boolean verboseOption){
    say("OK!\n", verboseOption);
  }

  private void sayOldNameNewName(String name, String newName){
    say("  Old name: " + name);
    say("  New name: " + newName);
    say("");
  }

  private void sayPart(GeometryPart gp) {
    sayPart(gp, true);
  }

  private String sayPart(GeometryPart gp, boolean verboseOption) {
    String toSay = "Part: ";
    if(isCadPart(gp)){
        toSay += ((CadPart) gp).getPathInHierarchy();
    }
    if(isLeafMeshPart(gp)){
        toSay += ((LeafMeshPart) gp).getPathInHierarchy();
    }
    if(isSimpleBlockPart(gp)){
        toSay += ((SimpleBlockPart) gp).getPathInHierarchy();
    }
    say(toSay, verboseOption);
    return toSay;
  }

  private void sayParts(Collection<GeometryPart> colGP, boolean verboseOption) {
    say("Number of Parts: " + colGP.size(), verboseOption);
    for (GeometryPart gp : colGP) {
        say("  " + sayPart(gp, false), verboseOption);
    }
  }

  private void sayPartSurface(PartSurface ps) {
    say("Part Surface: " + ps.getPresentationName());
    if(isCadPart(ps.getPart())){
        say("CAD Part: " + ((CadPart) ps.getPart()).getPathInHierarchy());
    }
    if(isLeafMeshPart(ps.getPart())){
        say("Leaf Mesh Part: " + ((LeafMeshPart) ps.getPart()).getPathInHierarchy());
    }
  }

  private void sayPhysicsContinua(PhysicsContinuum physCont){
    sayPhysicsContinua(physCont, true);
  }

  private void sayPhysicsContinua(PhysicsContinuum physCont, boolean verboseOption){
    say("Physics Continua: " + physCont.getPresentationName(), verboseOption);
  }

  private void sayRegion(Region region){
    sayRegion(region, true);
  }

  private void sayRegion(Region region, boolean verboseOption){
    say("Region: " + region.getPresentationName(), verboseOption);
  }

  private void sayRegions(Collection<Region> regions){
    say("Number of Regions: " + regions.size());
    for (Region reg : regions) {
        say("  " + region.getPresentationName());
    }
  }

  /**
   * Prints the current STAR-CCM+ version.
   */
  public void sayVersion() {
    String version = getVersion();
    say("Version: " + version);
    //String[] versionDetails = version.split("\\.");
    //int IV = Integer.parseInt(versionDetails[0]) * 100 + Integer.parseInt(versionDetails[1]);
    //say("Version: " + IV);
  }

  /**
   * Saves the simulation file using the current name.
   */
  public void saveSim(){
    saveSim(sim.getPresentationName());
  }

  /**
   * Saves the simulation file using a custom name.
   *
   * @param name given name.
   */
  public void saveSim(String name){
    String newName = name + ".sim";
    printAction("Saving: " + newName);
    sim.saveState(new File(simPath, newName).toString());
  }

  /**
   * Saves the simulation file appending a suffix. <p>
   *
   * The basic name is given using the {@link #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   */
  public void saveSimWithSuffix(String suffix){
    if(!saveIntermediates){ return; }
    String newName = simTitle + "_" + suffix;
    saveSim(newName);
    savedWithSuffix++;
  }

  /**
   * Saves the simulation file appending a suffix with the option to force saving
   * intermediate files. <p>
   *
   * The basic name is given using the {@link #simTitle} variable and a suffix.
   *
   * @param suffix given suffix.
   * @param forceOption save intermediate simulation files as well?
   *                    Depends on {@link #saveIntermediates}
   */
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

  /**
   * Sets the Wall Boundary as Constant Temperature.
   *
   * @param bdry given Boundary.
   * @param T given Temperature in default units. See {@link #defUnitTemp}.
   */
  public void setBC_ConstantTemperatureWall(Boundary bdry, double T){
    printAction("Setting BC as Constant Temperature Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.TEMPERATURE);
    setBC_StaticTemperature(bdry, T);
    sayOK();
  }

  /**
   * Sets the Wall Boundary as Convection Heat Transfer type.
   *
   * @param bdry given Boundary.
   * @param T given Ambient Temperature in default units. See {@link #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@link #defUnitHTC}.
   */
  public void setBC_ConvectionWall(Boundary bdry, double T, double htc){
    setBC_ConvectionWall(bdry, T, htc, true);
  }

  /**
   * Sets the Wall Boundary as Environment Heat Transfer type.
   *
   * @param bdry given Boundary.
   * @param T given Ambient Temperature in default units. See {@link #defUnitTemp}.
   * @param htc given Heat Transfer Coefficient in default units. See {@link #defUnitHTC}.
   * @param emissivity given Emissivity dimensionless.
   * @param transmissivity given Transmissivity dimensionless.
   * @param externalEmissivity given External Emissivity dimensionless.
   */
  public void setBC_EnvironmentWall(Boundary bdry, double T, double htc, double emissivity,
                                                double transmissivity, double externalEmissivity){
    printAction("Setting BC as an Environment Wall");
    sayBdry(bdry);
    setBC_ConvectionWall(bdry, T, htc, false);
    if(hasRadiationBC(bdry)){
        //say("  External Emissivity: " + externalEmissivity);
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

  /**
   * Sets the Wall Boundary as Free Slip.
   *
   * @param bdry given Boundary.
   */
  public void setBC_FreeSlipWall(Boundary bdry){
    printAction("Setting BC as a Free Slip Wall");
    sayBdry(bdry);
    bdry.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.SLIP);
    sayOK();
  }

  @Deprecated
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

  /**
   * Sets a Boundary as Mass Flow Rate Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param mfr given Mass Flow Rate in default units. See {@link #defUnitMFR}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_MassFlowRateInlet(Boundary bdry, double mfr, double T, double ti, double tvr){
    printAction("Setting BC as Mass Flow Rate Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(MassFlowBoundary.class);
    setBC_MassFlowRate(bdry, mfr);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Pressure Outlet. Assumes a default (null) Static Pressure. <p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  @Deprecated
  public void setBC_PressureOutlet(Boundary bdry, double T, double ti, double tvr){
    printAction("Setting BC as Pressure Outlet");
    sayBdry(bdry);
    bdry.setBoundaryType(PressureBoundary.class);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Pressure Outlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param P given Static Pressure in default units. See {@link #defUnitTemp}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_PressureOutlet(Boundary bdry, double P, double T, double ti, double tvr){
    printAction("Setting BC as Pressure Outlet");
    sayBdry(bdry);
    bdry.setBoundaryType(PressureBoundary.class);
    setBC_StaticPressure(bdry, P);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Stagnation Inlet. Assumes a default (null) Static Pressure. <p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param T given Total Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  @Deprecated
  public void setBC_StagnationInlet(Boundary bdry, double T, double ti, double tvr){
    printAction("Setting BC as Stagnation Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(StagnationBoundary.class);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Stagnation Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param P given Total Pressure in default units. See {@link #defUnitPress}.
   * @param T given Total Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_StagnationInlet(Boundary bdry, double P, double T, double ti, double tvr){
    printAction("Setting BC as Stagnation Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(StagnationBoundary.class);
    setBC_TotalTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Symmetry.
   *
   * @param bdry given Boundary.
   */
  public void setBC_Symmetry(Boundary bdry){
    printAction("Setting BC as Symmetry");
    sayBdry(bdry);
    bdry.setBoundaryType(SymmetryBoundary.class);
    sayOK();
  }

  @Deprecated
  public void setBC_VelocityInlet(Boundary bdry, double T, double vel, double ti, double tvr){
    printAction("Setting BC as Velocity Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(InletBoundary.class);
    setBC_VelocityMagnitude(bdry, vel);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  /**
   * Sets a Boundary as Velocity Inlet.<p>
   * <i>Note when running Laminar or Isothermal models, the other parameters are ignored</p>.
   *
   * @param bdry given Boundary.
   * @param vel given Velocity Magnitude in default units. See {@link #defUnitVel}.
   * @param T given Static Temperature in default units. See {@link #defUnitTemp}.
   * @param ti given Turbulent Intensity dimensionless.
   * @param tvr given Turbulent Viscosity Ratio dimensionless.
   */
  public void setBC_VelocityMagnitudeInlet(Boundary bdry, double vel, double T, double ti, double tvr){
    printAction("Setting BC as Velocity Inlet");
    sayBdry(bdry);
    bdry.setBoundaryType(InletBoundary.class);
    setBC_VelocityMagnitude(bdry, vel);
    setBC_StaticTemperature(bdry, T);
    setBC_TI_and_TVR(bdry, ti, tvr);
    sayOK();
  }

  private void setBC_ConvectionWall(Boundary bdry, double T, double htc, boolean verboseOption){
    printAction("Setting BC as Convection Wall", verboseOption);
    sayBdry(bdry, verboseOption);
    bdry.getConditions().get(WallThermalOption.class).setSelected(WallThermalOption.CONVECTION);
    AmbientTemperatureProfile atp = bdry.getValues().get(AmbientTemperatureProfile.class);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    atp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
    HeatTransferCoefficientProfile htcp = bdry.getValues().get(HeatTransferCoefficientProfile.class);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitHTC);
    htcp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(htc);
    sayOK(verboseOption);
  }

  private void setBC_MassFlowRate(Boundary bdry, double mfr){
    if (!bdry.getValues().has("Mass Flow Rate")) {
      return;
    }
    MassFlowRateProfile mfrp = bdry.getValues().get(MassFlowRateProfile.class);
    mfrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitMFR);
    mfrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(mfr);
  }

  private void setBC_StaticPressure(Boundary bdry, double P){
    if (!bdry.getValues().has("Pressure")) {
      return;
    }
    StaticPressureProfile spp = bdry.getValues().get(StaticPressureProfile.class);
    spp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
    spp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(P);
  }

  private void setBC_StaticTemperature(Boundary bdry, double T){
    if (!bdry.getValues().has("Static Temperature")) {
      return;
    }
    StaticTemperatureProfile stp = bdry.getValues().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  private void setBC_TI_and_TVR(Boundary bdry, double ti, double tvr){
    if (!bdry.getValues().has("Turbulence Intensity") && !bdry.getValues().has("Turbulent Viscosity Ratio")) {
      return;
    }
    TurbulenceIntensityProfile tip = bdry.getValues().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti);
    TurbulentViscosityRatioProfile tvrp = bdry.getValues().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr);
  }

  private void setBC_TotalPressure(Boundary bdry, double P){
    if (!bdry.getValues().has("Total Pressure")) {
      return;
    }
    TotalPressureProfile tpp = bdry.getValues().get(TotalPressureProfile.class);
    tpp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitPress);
    tpp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(P);
  }

  private void setBC_TotalTemperature(Boundary bdry, double T){
    if (!bdry.getValues().has("Total Temperature")) {
      return;
    }
    TotalTemperatureProfile ttp = bdry.getValues().get(TotalTemperatureProfile.class);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    ttp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(T);
  }

  private void setBC_VelocityMagnitude(Boundary bdry, double vel){
    if (!bdry.getValues().has("Velocity Magnitude")) {
      return;
    }
    VelocityMagnitudeProfile vmp = bdry.getValues().get(VelocityMagnitudeProfile.class);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitVel);
    vmp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(vel);
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

  private void setInitialCondition_P(PhysicsContinuum phC, double P0){
    InitialPressureProfile ipp = phC.getInitialConditions().get(InitialPressureProfile.class);
    ipp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(p0);
  }

  private void setInitialCondition_T(PhysicsContinuum phC, double T0, boolean verboseOption){
    printAction("Setting Initial Conditions for Temperature", verboseOption);
    sayPhysicsContinua(phC, verboseOption);
    say(retTemp(T0), verboseOption);
    StaticTemperatureProfile stp = phC.getInitialConditions().get(StaticTemperatureProfile.class);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    stp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(fluidT0);
  }

  /**
   * Set Initial Conditions for Temperature.
   *
   * @param phC given Physics Continua
   * @param T0 initial Temperature in default unit.
   */
  public void setInitialCondition_Temperature(PhysicsContinuum phC, double T0){
    setInitialCondition_T(phC, T0, true);
    sayOK();
  }

  /**
   * Set Initial Conditions for Turbulence (RANS 2-equation models).
   *
   * @param phC given Physics Continua
   * @param tvs0 initial Turbulent Velocity Scale.
   * @param ti0 initial Turbulent Intensity.
   * @param tvr0 initial Turbulent Viscosity Ratio.
   */
  public void setInitialCondition_Turbulence(PhysicsContinuum phC, double tvs0, double ti0, double tvr0){
    setInitialCondition_TVS_TI_TVR(phC, tvs0, ti0, tvr0, true);
    sayOK();
  }

  private void setInitialCondition_TVS_TI_TVR(PhysicsContinuum phC, double tvs0, double ti0, double tvr0, boolean verboseOption){
    printAction("Setting Initial Conditions for Turbulence", verboseOption);
    sayPhysicsContinua(phC, verboseOption);
    say("Turbulent Velocity Scale: " + tvs0, verboseOption);
    say("Turbulent Intensity: " + ti0, verboseOption);
    say("Turbulent Viscosity Ratio: " + tvr0, verboseOption);
    TurbulentVelocityScaleProfile tvs = phC.getInitialConditions().get(TurbulentVelocityScaleProfile.class);
    tvs.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvs0);
    TurbulenceIntensityProfile tip = phC.getInitialConditions().get(TurbulenceIntensityProfile.class);
    tip.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(ti0);
    TurbulentViscosityRatioProfile tvrp = phC.getInitialConditions().get(TurbulentViscosityRatioProfile.class);
    tvrp.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(tvr0);
  }

  /**
   * Set Initial Conditions for Turbulence (RANS 2-equation models).
   *
   * @param phC given Physics Continua
   * @param tvs0 initial Turbulent Velocity Scale.
   * @param ti0 initial Turbulent Intensity.
   * @param tvr0 initial Turbulent Viscosity Ratio.
   */
  public void setInitialCondition_Velocity(PhysicsContinuum phC, double Vx, double Vy, double Vz){
    setInitialCondition_Velocity(phC, Vx, Vy, Vz, true);
    sayOK();
  }

  private void setInitialCondition_Velocity(PhysicsContinuum phC, double Vx, double Vy, double Vz, boolean verboseOption){
    printAction("Setting Initial Conditions for Velocity Components", verboseOption);
    sayPhysicsContinua(phC, verboseOption);
    say("Velocity X: " + Vx, verboseOption);
    say("Velocity Y: " + Vy, verboseOption);
    say("Velocity Z: " + Vz, verboseOption);
    VelocityProfile vp = phC.getInitialConditions().get(VelocityProfile.class);
    vp.getMethod(ConstantVectorProfileMethod.class).getQuantity().setComponents(Vx, Vy, Vz);
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

  @Deprecated
  public void setMeshBaseSize(MeshContinuum mshCont, double baseSize, Units unit) {
    setMeshBaseSizes(mshCont, baseSize, unit);
  }

  /**
   * Specify the Reference Mesh Size for a Mesh Continuum.
   *
   * @param mshCont given Mesh Continua.
   * @param baseSize reference size.
   * @param unit given units.
   */
  public void setMeshBaseSizes(MeshContinuum mshCont, double baseSize, Units unit) {
    printAction("Setting Mesh Continua Base Size");
    sayMeshContinua(mshCont);
    say("  Base Size: " + baseSize + unit.toString());
    mshCont.getReferenceValues().get(BaseSize.class).setUnits(unit);
    mshCont.getReferenceValues().get(BaseSize.class).setValue(baseSize);
    sayOK();
  }

  public void setMeshBoundaryPrismSizes(Boundary bdry, int numLayers, double stretch, double relSize) {
    if(tempMeshSizeSkip){ return; }
    printAction("Setting Custom Boundary Prism Layer");
    if(!isMeshing(bdry, true)){ return; }
    setMeshPrismsParameters(bdry, numLayers, stretch, relSize);
//    try{
//        bdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
//        bdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
//        bdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
//        PrismThickness prismThick = bdry.get(MeshValueManager.class).get(PrismThickness.class);
//        ((GenericRelativeSize) prismThick.getRelativeSize()).setPercentage(relSize);
//        sayOK();
//    } catch (Exception e1) {
//        say("ERROR! Please review settings. Skipping this Boundary.");
//        say(e1.getMessage() + "\n");
//    }
  }

  @Deprecated
  public void setMeshBoundarySurfaceSizes(Boundary bdry, double min, double tgt){
    if(tempMeshSizeSkip){ return; }
    printAction("Setting Custom Surface Mesh Size");
    setMeshSurfaceSizes(bdry, min, tgt, true);
  }

  @Deprecated
  public void setMeshDirectBoundaryInterfaceSurfaceSizes(DirectBoundaryInterface intrfPair, double min, double tgt){
    printAction("Custom Surface Mesh Size for Interface");
    sayInterface(intrfPair);
    say("  " + retMinTargetString(min, tgt));
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

  /**
   * Set a custom Mesh Surface Size for a Feature Curve.
   *
   * @param fc given Feature Curve.
   * @param min given Minimum size.
   * @param tgt give Target size.
   */
  public void setMeshFeatureCurveSizes(FeatureCurve fc, double min, double tgt){
    say("Custom Feature Curve Mesh Size: " + fc.getPresentationName());
    sayRegion(fc.getRegion());
    try {
        fc.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = fc.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, true);
    } catch (Exception e) {
        say("ERROR! Please review settings. Skipping this Feature Curve.");
        say(e.getMessage());
        return;
    }
    sayOK();
  }

  /**
   * Set a custom Mesh Surface Size all Feature Curves based on REGEX search.
   *
   * @param regexPatt given REGEX search pattern.
   * @param min given Minimum size.
   * @param tgt give Target size.
   */
  public void setMeshFeatureCurvesSizes(String regexPatt, double min, double tgt){
    printAction(String.format("Setting Mesh Feature Curves by REGEX pattern: \"%s\"", regexPatt));
    int n = 0;
    for (Region reg : getAllRegions(regexPatt, false)) {
        for (FeatureCurve fc : getFeatureCurves(reg, false)) {
            setMeshFeatureCurveSizes(fc, min, tgt);
            n++;
        }
    }
    say("Feature Curves changed: " + n);
    printLine();
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
        intrfBdry.get(MeshConditionManager.class).get(CustomizeBoundaryPrismsOption.class).setSelected(CustomizeBoundaryPrismsOption.CUSTOM_VALUES);
        intrfBdry.get(MeshValueManager.class).get(NumPrismLayers.class).setNumLayers(numLayers);
        intrfBdry.get(MeshValueManager.class).get(PrismLayerStretching.class).setStretching(stretch);
        setMeshPrismsThickness(intrfBdry.get(MeshValueManager.class).get(PrismThickness.class), relSize);
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

  /**
   * Specify Surface Mesh Sizes for a Boundary.
   *
   * @param bdry given Boundary.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Boundary bdry, double min, double tgt){
    setMeshSurfaceSizes(bdry, min, tgt, true);
  }

  /**
   * Loop through all boundaries in the Region and specify Surface Mesh Sizes for them.
   *
   * @param region given Region.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Region region, double min, double tgt) {
    printAction("Setting Mesh Sizes in a Region");
    sayRegion(region);
    say("  " + retMinTargetString(min, tgt));
    printLine();
    for (Boundary bdry : getAllBoundariesFromRegion(region, false, false)) {
        if (!setMeshSurfaceSizes(bdry, min, tgt, false)) {
            say("Skipped!  " + bdry.getPresentationName());
            continue;
        }
        say("OK!  " + bdry.getPresentationName());
    }
    printLine();
    sayOK();
  }

  /**
   * Specify Surface Mesh Sizes for an Interface.
   *
   * @param intrfPair given Interface.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(Interface intrfPair, double min, double tgt) {
    sayInterface(intrfPair);
    if (isDirectBoundaryInterface(intrfPair)) {
        DirectBoundaryInterface dbi = (DirectBoundaryInterface) intrfPair;
        dbi.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrfPair.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, true);
    } else {
        say("Not a Direct Boundary Interface. Skipping...");
    }
    sayOK();
  }


  private boolean setMeshSurfaceSizes(Boundary bdry, double min, double tgt, boolean verboseOption){
    sayBdry(bdry, verboseOption, true);
    if (!isMeshing(bdry, verboseOption)) {
        say("Region has no Mesh Continua. Skipping...", verboseOption);
        return false;
    } else if (isIndirectBoundaryInterface(bdry)) {
        say("Skipping...", verboseOption);
        return false;
    } else if(isDirectBoundaryInterface(bdry)) {
        DirectBoundaryInterfaceBoundary intrfBdry = (DirectBoundaryInterfaceBoundary) bdry;
        DirectBoundaryInterface intrfPair = intrfBdry.getDirectBoundaryInterface();
        intrfPair.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = intrfPair.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, verboseOption);
    } else {
        bdry.get(MeshConditionManager.class).get(SurfaceSizeOption.class).setSurfaceSizeOption(true);
        SurfaceSize srfSize = bdry.get(MeshValueManager.class).get(SurfaceSize.class);
        setMeshSurfaceSizes(srfSize, min, tgt, verboseOption);
    }
    sayOK(verboseOption);
    return true;
  }

  private void setMeshSurfaceSizes(SurfaceSize srfSize, double min, double tgt, boolean verboseOption) {
    say("  " + retMinTargetString(min, tgt), verboseOption);
    srfSize.getRelativeMinimumSize().setPercentage(min);
    srfSize.getRelativeTargetSize().setPercentage(tgt);
  }

  public void setMeshCurvatureNumberOfPoints(MeshContinuum mshCont, double numPoints){
    printAction("Setting Mesh Continua Surface Curvature");
    sayMeshContinua(mshCont);
    say("  Points/Curve: " + numPoints);
    mshCont.getReferenceValues().get(SurfaceCurvature.class).getSurfaceCurvatureNumPts().setNumPointsAroundCircle(numPoints);
    sayOK();
  }

  /**
   * Specify Surface Mesh Sizes for a Mesh Continuum.
   *
   * @param mshCont given Mesh Continua.
   * @param min minimum relative size (%).
   * @param tgt target relative size (%).
   */
  public void setMeshSurfaceSizes(MeshContinuum mshCont, double min, double tgt){
    printAction("Setting Mesh Continua Surface Sizes");
    sayMeshContinua(mshCont);
    SurfaceSize srfSize = mshCont.getReferenceValues().get(SurfaceSize.class);
    setMeshSurfaceSizes(srfSize, min, tgt, true);
    sayOK();
  }

  /**
   * Sets a Mesh Volume Growth Factor for the Poly Mesh.
   *
   * @param mshCont given Mesh Continua.
   * @param growthFactor given Growth Factor.
   */
  public void setMeshTetPolyGrowthRate(MeshContinuum mshCont, double growthFactor){
    printAction("Setting Mesh Volume Growth Factor");
    sayMeshContinua(mshCont);
    say("  Growth Factor: " + growthFactor);
    mshCont.getReferenceValues().get(VolumeMeshDensity.class).setGrowthFactor(growthFactor);
    sayOK();
  }

  /**
   * Sets the Mesh Trimmer Size To Prism Thickness Ratio.
   *
   * @param mshCont given Mesh Continua.
   * @param sizeThicknessRatio given Size Thickness Ratio.
   */
  public void setMeshTrimmerSizeToPrismThicknessRatio(MeshContinuum mshCont, double sizeThicknessRatio){
    printAction("Setting Mesh Trimmer Size To Prism Thickness Ratio...");
    sayMeshContinua(mshCont);
    MaxTrimmerSizeToPrismThicknessRatio ptr = mshCont.getReferenceValues().get(MaxTrimmerSizeToPrismThicknessRatio.class);
    ptr.setLimitCellSizeByPrismThickness(true);
    say("  Size Thickness Ratio: " + sizeThicknessRatio);
    ptr.getSizeThicknessRatio().setNeighboringThicknessMultiplier(sizeThicknessRatio);
    sayOK();
  }

  /**
   * Sets the Surface Wrapper Feature Angle.
   *
   * @param mshCont given Mesh Continua.
   * @param scaleFactor given Feature Angle. E.g.: 35 degrees.
   */
  public void setMeshWrapperFeatureAngle(MeshContinuum mshCont, double featAngle) {
    printAction("Setting Wrapper Feature Angle");
    sayMeshContinua(mshCont);
    say("Feature Angle: " + featAngle + " deg");
    mshCont.getReferenceValues().get(GeometricFeatureAngle.class).setGeometricFeatureAngle(featAngle);
    sayOK();
  }


  /**
   * Sets the Surface Wrapper Scale Factor.
   *
   * @param mshCont given Mesh Continua.
   * @param scaleFactor given Scale Factor. E.g.: 70.
   */
  public void setMeshWrapperScaleFactor(MeshContinuum mshCont, double scaleFactor) {
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
//    catch (Exception e){
//        say("ERROR! Radiation Settings not available. Skipping...");
//        say(e.getMessage());
//    }
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
    say("  Radiation " + retTemp(temperature));
    try{
        RadiationTemperatureProfile rtP = bdry.getValues().get(RadiationTemperatureProfile.class);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(temperature);
        rtP.getMethod(ConstantScalarProfileMethod.class).getQuantity().setUnits(defUnitTemp);
    } catch (Exception e){
        say("ERROR! Radiation Temperature not applicable on: " + bdry.getPresentationName());
    }
    sayOK();
  }

  /**
   * Update the Scene Update Frequency.
   *
   * @param scene given Scene.
   * @param iter given update frequency iterations.
   */
  public void setSceneUpdateFrequency(Scene scene, int iter) {
    printAction("Setting Scene Update Frequency");
    say("Iteration Frequency: " + iter);
    scene.getSceneUpdate().getIterationUpdateFrequency().setIterations(iter);
    sayOK();
  }

  /**
   * Set the maximum number of iterations in Simulation.
   *
   * @param n given number of iterations.
   */
  public void setSimMaxIterations(int n){
    setSimMaxIterations(n, true);
  }

  private void setSimMaxIterations(int n, boolean verboseOption){
    printAction("Setting Maximum Number of Iterations", verboseOption);
    say("Max Iterations: " + n, verboseOption);
    maxIter = n;
    ((StepStoppingCriterion)
        sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps")).setMaximumNumberSteps(n);
    sayOK(verboseOption);
  }

  /**
   * Updates the Simulation name.
   *
   * @param newSimTitle given new name.
   */
  public void setSimTitle(String newSimTitle) {
    simTitle = newSimTitle;
  }

  /**
   * Splits all the Part Surfaces on all Leaf Parts of the Composite by a given angle.
   *
   * @param compPrt given Composite Part.
   * @param splitAngle given Split Angle.
   */
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

  /**
   * Splits all the Part Surfaces on the Part by a given angle.
   *
   * @param geomPrt given Geometry Part.
   * @param splitAngle given Split Angle.
   */
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

  /**
   * Splits a Part Surface by a given angle.
   *
   * @param ps given Part Surface.
   * @param splitAngle given Split Angle.
   */
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
    if(isBlockPart(ps.getPart())){
        SimpleBlockPart sbp = (SimpleBlockPart) ps.getPart();
        sbp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if(isSimpleCylinderPart(ps.getPart())){
        SimpleCylinderPart scp = (SimpleCylinderPart) ps.getPart();
        scp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if(isLeafMeshPart(ps.getPart())){
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    if(isMeshOperationPart(ps.getPart())){
        MeshOperationPart mop = (MeshOperationPart) ps.getPart();
        mop.getPartSurfaceManager().splitPartSurfacesByAngle(vecPS, splitAngle);
    }
    sayOK();
  }

  /**
   * Splits a Part Surface into Non-Contiguous pieces.
   *
   * @param ps give Part Surface.
   * @return Collection of the new splitted Part Surfaces.
   */
  public Collection<PartSurface> splitPartSurfacesNonContiguous(PartSurface ps){
    return splitPartSurfacesNonContiguous(ps, true);
  }

  private Collection<PartSurface> splitPartSurfacesNonContiguous(PartSurface ps, boolean verboseOption) {
    printAction("Splitting Non Contiguous Part Surface", verboseOption);
    sayPartSurface(ps);
    String name0 = ps.getPresentationName();
    String mySplit = "__splitFrom__" + name0;
    ps.setPresentationName(mySplit);
    Vector<PartSurface> vecPS = new Vector<PartSurface>();
    Object[] objArr = {ps};
    if (isCadPart(ps.getPart())) {
        CadPart cp = (CadPart) ps.getPart();
        cp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    if (isLeafMeshPart(ps.getPart())) {
        LeafMeshPart lmp = (LeafMeshPart) ps.getPart();
        lmp.getPartSurfaceManager().splitNonContiguousPartSurfaces(new NeoObjectVector(objArr));
    }
    for(PartSurface ps1 : ps.getPart().getPartSurfaces()) {
        if (ps == ps1) {
            vecPS.insertElementAt(ps1, 0);
        }
        if (ps1.getPresentationName().matches(mySplit + ".*")) {
            vecPS.add(ps1);
        }
    }
    for (Iterator<PartSurface> it = vecPS.iterator(); it.hasNext();) {
        it.next().setPresentationName(name0);
    }
    sayOK(verboseOption);
    return vecPS;
  }

  /**
   * Splits a group of Part Surfaces based on REGEX search criteria into Non-Contiguous pieces.
   *
   * @param regexPatt given REGEX search pattern.
   */
  public void splitPartSurfacesNonContiguous(String regexPatt){
    printAction("Splitting Non Contiguous Part Surfaces");
    int n = 0;
    for(PartSurface ps : getAllPartSurfaces(regexPatt, false)){
        splitPartSurfacesNonContiguous(ps, false);
        n++;
    }
    say("Part Surfaces splitted: " + n);
    printLine();
  }

  /**
   * Split a Non-Contiguous region.
   *
   * @param region given Region.
   */
  public void splitRegionNonContiguous(Region region){
    printAction("Splitting Non Contiguous Regions");
    MeshContinuum mshc = null;
    PhysicsContinuum phc = null;
    sayRegion(region);
    Object[] objArr = {region};
    sim.getMeshManager().splitNonContiguousRegions(new NeoObjectVector(objArr), minConFaceAreas);
    // Loop into the generated Regions created by Split
    for (Region reg : getAllRegions("^" + region.getPresentationName() + " \\d{1,2}", false)){
        if (region == reg) { continue; }
        mshc = reg.getMeshContinuum();
        if (mshc != null) { mshc.erase(reg); }
        phc = reg.getPhysicsContinuum();
        if (phc != null) { phc.erase(reg); }
        say("  Disabled Region: " + reg.getPresentationName());
    }
    sayOK();
  }

  @Deprecated
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

  /**
   * Converts a given string into a wildcard REGEX pattern.
   *
   * @param text given string or text.
   * @return The returned string will be <b>.*text.*</b>.
   */
  public String str2regex(String text){
    return ".*" + text + ".*";
  }

  /**
   * Shows a warning dialog.
   *
   * @param message given text to be displayed.
   */
  public void showWarning(String message) {
    JOptionPane.showMessageDialog(null, message, "WARNING!", JOptionPane.WARNING_MESSAGE);
  }

  @Deprecated
  public void turnOffRegion(Region region){
    disableRegion(region);
  }

  @Deprecated
  public void unsetMeshPerRegionFlag(MeshContinuum mshCont){
    disableMeshPerRegion(mshCont);
  }

  public void updateOrCreateNewUnits(){
    printAction("Updating/Creating Units");
    unit_C = queryUnit("C");
    unit_Dimensionless = queryUnit("Dimensionless");
    unit_F = queryUnit("F");
    unit_K = queryUnit("K");
    unit_m = queryUnit("m");
    unit_mm = queryUnit("mm");
    unit_kph = queryUnit("kph");    // km/h
    unit_kgps = queryUnit("kg/s");
    unit_mps = queryUnit("m/s");
    unit_rpm = queryUnit("rpm");
    unit_Pa = queryUnit("Pa");
    unit_Pa_s = queryUnit("Pa-s");
    unit_radps = queryUnit("radian/s");
    unit_Wpm2K = queryUnit("W/m^2-K");
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
    defUnitHTC = unit_Wpm2K;
    defUnitLength = unit_mm;
    defUnitMFR = unit_kgps;
    defUnitPress = unit_Pa;
    defUnitVel = unit_mps;
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
    say("  Refence " + retTemp(Tref));
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

  private void updateSimVar(){
    if(sim != null){ return; }
    sim = getActiveSimulation();
    updateSolverSettings();
  }

  /**
   * Updates all Solver Settings. E.g.: Relaxation Factors, Linear Ramps, etc... Currently, the
   * following are being used:<p>
   * {@link #maxIter}, {@link #urfVel}, {@link #urfP}, {@link #urfFluidEnrgy}, {@link #urfSolidEnrgy} and
   * {@link #rampURF}.
   *
   */
  public void updateSolverSettings(){
    printAction("Updating Solver Settings");
    setSimMaxIterations(maxIter, false);
    printSolverSettings();

    try{
        SegregatedFlowSolver flowSolv = ((SegregatedFlowSolver) sim.getSolverManager().getSolver(SegregatedFlowSolver.class));
        flowSolv.getVelocitySolver().setUrf(urfVel);
        PressureSolver pSolv = flowSolv.getPressureSolver();
        pSolv.setUrf(urfP);
    } catch (Exception e){}

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

  /**********************************************************
   **********************************************************
   *     G L O B A L    V A R I A B L E S    A R E A        *
   **********************************************************
   **********************************************************/

  /***************************************************
   * Global definitions
   ***************************************************/
  boolean autoCloseFreeEdges = true;
  boolean colorByRegion = true;
  boolean checkMeshQualityUponSurfaceMeshGeneration = false;
  boolean checkMeshQualityUponSurfaceMeshImport = false;
  boolean createMeshSceneUponSurfaceMeshGeneration = false;
  boolean fineTesselationOnImport = false;
  /** Save intermediate files when troubleshooting a macro? */
  boolean saveIntermediates = true;
  boolean singleBoundary = false;
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
  int autoSaveMaxFiles = 2;
  int autoSaveFrequencyIter = 1000;
  int colourByPart = 4;
  int colourByRegion = 2;
  int partColouring = colourByPart;
  int picResX = 800;
  int picResY = 600;
  int savedWithSuffix = 0;
  /** Some useful Global Variables: Geometry Parts. */
  GeometryPart geomPrt = null, geomPrt1 = null, geomPrt2 = null, geomPrt3 = null;
  /** Some useful Global Variables: Interfaces. */
  Interface intrf = null, intrf1 = null, intrf2 = null, intrf3 = null;
  /** Some useful Global Variables: Leaf Mesh Parts. */
  LeafMeshPart leafMshPrt = null, leafMshPrt1 = null, leafMshPrt2 = null, leafMshPrt3 = null;
  /** Some useful Global Variables: Mesh Continuas. */
  MeshContinuum mshCont = null, mshCont1 = null, mshCont2 = null;
  /** Some useful Global Variables: Mesh Operation Parts. */
  MeshOperationPart mshOpPrt = null, mshOpPrt1 = null, mshOpPrt2 = null, mshOpPrt3 = null;
  /** Some useful Global Variables: Monitor Plots. */
  MonitorPlot monPlot = null, monPlot1 = null, monPlot2 = null;
  /** Some useful Global Variables: Part Surfaces. */
  PartSurface partSrf = null, partSrf1 = null, partSrf2 = null, partSrf3 = null;
  /** Some useful Global Variables: Planes. */
  PlaneSection plane = null, plane1 = null, plane2 = null;
  /** Some useful Global Variables: Physics Continuas. */
  PhysicsContinuum physCont = null, physCont1 = null, physCont2 = null, physCont3 = null;
  /** Some useful Global Variables: Regions. */
  Region region = null, region1 = null, region2 = null, region3 = null;
  /** Some useful Global Variables: Report Monitors. */
  ReportMonitor repMon = null, repMon1 = null, repMon2 = null, repMon3 = null;
  /** Some useful Global Variables: Scenes. */
  Scene scene = null, scene1 = null, scene2 = null, scene3 = null;
  /** Some useful Global Variables: Simple Block Parts. */
  SimpleBlockPart simpleBlkPrt = null, simpleBlkPrt1 = null, simpleBlkPrt2 = null, simpleBlkPrt3 = null;
  /** Some useful Global Variables: Simple Cylinder Parts. */
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
  /**
   * The current simulation name and other useful declarations.
   */
  String bcBottom = "bottom";
  String bcChannel = "channel";
  String bcCold = "cold";
  /** STAR-CCM+ likes to call Part Surfaces as <b>Faces</b>, right? */
  String bcFaces = "Faces";
  String bcHot = "hot";
  String bcInlet = "inlet";
  String bcOutlet = "outlet";
  String bcSym = "symmetry";
  String bcSym1 = "symmetry1";
  String bcSym2 = "symmetry2";
  String bcTop = "top";
  String bcWall = "wall";
  String bcWalls = "walls";
  String simTitle = null;
  String simPath = null;
  String string = "", string1 = "", string2 = "", string3 = "", text = "";
  String inlet = bcInlet;
  String outlet = bcOutlet;
  String wall = bcWall;
  String walls = bcWalls;
  Simulation sim = null;
  /***************************************************
   * Physics
   ***************************************************/
  /** Default unit of Temperature, when using {@link .}. */
  Units defUnitTemp = null;
  /** Default unit of Heat Transfer Coefficient, when using {@link .}. */
  Units defUnitHTC = null;
  /** Default unit of Length, when using {@link .}. */
  Units defUnitLength = null;
  /** Default unit of Mass Flow Rate, when using {@link .}. */
  Units defUnitMFR = null;
  /** Default unit of Pressure, when using {@link .}. */
  Units defUnitPress = null;
  /** Default unit of Velocity, when using {@link .}. */
  Units defUnitVel = null;
  /** Celsius unit (Temperature). */
  Units unit_C = null;
  /** CentiPoise unit (Viscosity). */
  Units unit_cP = null;
  /** Centimeter of Water unit (Pressure). */
  Units unit_cmH2O = null;
  /** Dimensionless unit. */
  Units unit_Dimensionless = null;
  /** Fahrenheit unit (Temperature). */
  Units unit_F = null;
  /** Dyne per Square Centimeter unit (Pressure). */
  Units unit_dynepcm2 = null;
  /** Gram unit (Mass). */
  Units unit_g = null;
  /** Gram per Minute unit (Mass/Time). */
  Units unit_gpmin = null;
  /** Gram per Second unit (Mass/Time). */
  Units unit_gps = null;
  /** Kelvin unit (Temperature). */
  Units unit_K = null;
  /** Kilometer per Hour unit (Velocity). */
  Units unit_kph = null;
  /** Kilogram per Hour unit (Mass/Time). */
  Units unit_kgph = null;
  /** Kilogram per Second unit (Mass/Time). */
  Units unit_kgps = null;
  /** Kilogram per Minute unit (Mass/Time). */
  Units unit_kgpmin = null;
  /** Liter per Hour unit (Volume/Time). */
  Units unit_lph = null;
  /** Liter per Minute unit (Volume/Time). */
  Units unit_lpmin = null;
  /** Liter per Second unit (Volume/Time). */
  Units unit_lps = null;
  /** Meter unit (Length). */
  Units unit_m = null;
  /** Millimeter unit (Length). */
  Units unit_mm = null;
  /** Millimeter of Water unit (Pressure). */
  Units unit_mmH2O = null;
  /** Meter per Second unit (Velocity). */
  Units unit_mps = null;
  /** Poise unit (Viscosity). */
  Units unit_P = null;
  /** Pascal unit (Pressure). */
  Units unit_Pa = null;
  /** Pascal x Second unit (Viscosity). */
  Units unit_Pa_s = null;
  /** Radian per Second unit (Angular Velocity). */
  Units unit_radps = null;
  /** Rotation per Minute unit (Angular Velocity). */
  Units unit_rpm = null;
  /** Watt per Square Meter x Kelvin unit (Heat Transfer Coefficient). */
  Units unit_Wpm2K = null;
  /***************************************************
   * Physics
   ***************************************************/
  double[] gravity = {0., -9.81, 0.};           // m/s^2
  /** Initial Pressure value in default units. */
  double p0 = 0.0;                              // Pa
  /** Initial Turbulent Intensity for RANS Models. */
  double ti0 = 0.05;
  /** Initial Turbulent Viscosity Ratio for RANS Models. */
  double tvr0 = 10.0;
  /** Initial Velocity Scale for RANS Turbulence Models in default units. */
  double tvs0 = 0.5;
  /** Initial Velocity Array in default units. */
  double[] v0 = {0., 0., 0.};
  /** Minimum clipping Temperature in default units. */
  double clipMinT = -50;
  /** Maximum clipping Temperature in default units. */
  double clipMaxT = 3000;
  /** Initial Temperature value for Fluids in default units. */
  double fluidT0 = 22.;
  /** Initial Temperature value for Solids in default units. */
  double solidsT0 = 60.;
  /** Reference Pressure value in default units. */
  double Pref = 101325.;
  /** Reference Temperature value in default units. */
  double Tref = 22.;
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
  /** Minimum Feature Curve Relative Size (<b>%</b>).*/
  double featCurveMeshMin = 25;
  /** Target Feature Curve Relative Size (<b>%</b>).*/
  double featCurveMeshTgt = 100;
  /** Mesh Base (<i>Reference</i>) Size in default units. */
  double mshBaseSize = 3.0;                     // mm
  /** Mesh Growth Factor for Tets/Polys. ( <i>default = 1.0</i> ) */
  double mshGrowthFactor = 1.0;
  double mshOpsTol = 1e-4;                      // m
  double mshProximityPointsInGap = 2.0;
  double mshProximitySearchFloor = 0.0;         // mm
  double mshSrfCurvNumPoints = 36;              // points / curve
  /** Minimum Surface Relative Size (<b>%</b>). */
  double mshSrfSizeMin = 25;
  /** Target Surface Relative Size (<b>%</b>). */
  double mshSrfSizeTgt = 100;
  /** Maximum Trimmer Relative Size (<b>%</b>). */
  double mshTrimmerMaxCelSize = 10000;
  /** Surface Wrapper Feature Angle (<b>deg</b>). */
  double mshWrapperFeatureAngle = 30.;
  double mshWrapperScaleFactor = 100.;          // (%)
  double prismsLyrChoppPerc = 25.0;             // (%)
  double prismsMinThickn = 5.0;                 // (%)
  /** Prism Layers Near Core Aspect Ratio (NCLAR). Default = 0.0. */
  double prismsNearCoreAspRat = 0.5;
  /** Prism Layers Relative Size (<b>%</b>). */
  double prismsRelSizeHeight = 30.;             // (%)
  /** Prism Stretch Ratio. Default = 1.5. */
  double prismsStretching = 1.2;
  int intrfInt = 3;
  int maxInterfaces = 10000;
  /** How many Prism Layers? Default = 2.*/
  int prismsLayers = 2;
  /** How many layers for the Thin Mesher? Default = 2.*/
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
  /** Default Maximum Iterations */
  final int maxIter0 = 1000;
  /** Default URF for Velocity */
  final double urfVel0 = 0.7;
  /** Default URF for Pressure */
  final double urfP0 = 0.3;
  /** Default URF for Fluid Energy */
  final double urfFluidEnrgy0 = 0.9;
  /** Default URF for Solid Energy */
  final double urfSolidEnrgy0 = 0.99;
  /** Default URF for K-Epsilon */
  final double urfKEps0 = 0.8;
  /** Ramp URF? */
  boolean rampURF = false;
  /** Maximum Iterations */
  int maxIter = maxIter0;
  /** URF for Velocity */
  double urfVel = urfVel0;
  /** URF for Pressure */
  double urfP = urfP0;
  /** URF for Fluid Energy */
  double urfFluidEnrgy = urfFluidEnrgy0;
  /** URF for Solid Energy */
  double urfSolidEnrgy = urfSolidEnrgy0;
  /** URF for K-Epsilon */
  double urfKEps = urfKEps0;
  /** Initial URF for Ramping Fluid Energy */
  double urfRampFlBeg = 0.6;
  /** Initial URF for Ramping Solid Energy */
  double urfRampSldBeg = 0.7;
  /** Initial Iteration for Ramping URF in Fluid Energy */
  int urfRampFlIterBeg = 100;
  /** Final Iteration for Ramping URF in Fluid Energy */
  int urfRampFlIterEnd = 1000;
  /** Initial Iteration for Ramping URF in Solid Energy */
  int urfRampSldIterBeg = 100;
  /** Final Iteration for Ramping URF in Solid Energy */
  int urfRampSldIterEnd = 1000;
  /***************************************************
   * Immutable definitions
   ***************************************************/
  /** Origin coordinates (0, 0, 0). */
  final double[] coord0 = {0., 0., 0.};
  /** Just an empty Vector of Objects. Useful somewhere. */
  private final Object[] emptyObj = new Object[] {};
  /** Just an empty NeoObjectVector. Useful somewhere. */
  private final NeoObjectVector emptyNeoObjVec = new NeoObjectVector(emptyObj);
  /** Pressure variable name inside STAR-CCM+. */
  final String varP = "Pressure";
  /** Temperature variable name inside STAR-CCM+. */
  final String varT = "Temperature";
  /** Velocity variable name inside STAR-CCM+. */
  final String varVel = "Velocity";
  /** XYZ Coordinates as a String Array (X, Y, Z). Immutable. */
  final String[] xyzCoord = {"X", "Y", "Z"};
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
  /** Global Vector where all Mesh Continuas are stored when {@link .} is initialized. */
  Vector<MeshContinuum> vecMeshContinua = new Vector<MeshContinuum>();
  Vector<PartCurve> vecPartCrv = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv1 = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv2 = new Vector<PartCurve>();
  Vector<PartCurve> vecPartCrv3 = new Vector<PartCurve>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<PartSurface> vecPartSrf = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf1 = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf2 = new Vector<PartSurface>();
  Vector<PartSurface> vecPartSrf3 = new Vector<PartSurface>();
  /** Global Vector where a Physic Continua is stored every time one is created. */
  Vector<PhysicsContinuum> vecPhysicsContinua = new Vector<PhysicsContinuum>();
  /** Useful Global Vector Variables for storing Regions. */
  Vector<Region> vecReg = new Vector<Region>();
  /** Useful Global Vector Variables for storing Report Monitors. */
  Vector<ReportMonitor> vecRepMon = new Vector<ReportMonitor>();
  /** Useful Global Vector Variables for storing Objects. */
  Vector vecObj = new Vector(), vecObj2 = new Vector();
  @Deprecated
  Vector objVec = new Vector();
  MeshContinuum mshContPoly = null;
  MeshContinuum mshContTrimmer = null;
  FilenameFilter dbsFilter = new FilenameFilter() {
    public boolean accept(File dir, String name) {
        // Return files ending with dbs -- with ignore case group (?i)
        return name.matches("(?i).*dbs");
    }
  };

}
