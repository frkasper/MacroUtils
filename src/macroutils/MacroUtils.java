/*
 * MACRO Utils v1a
 * Programmer: Fabio Kasper
 * Last Revision: May 14, 2012.
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
import star.energy.*;
import star.flow.*;
import star.meshing.*;
import star.prismmesher.*;
import star.resurfacer.*;
import star.trimmer.*;
import star.turbulence.*;
import star.vis.*;

public class MacroUtils extends StarMacro {
  /***************************************************
   *
   * Global definitions
   *
   ***************************************************/
  boolean verbose = true;
  Boundary bdry = null;
  CadPart cadPrt = null;
  CellSurfacePart cellSet = null;
  CompositePart compPart = null;
  DirectBoundaryInterface intrfPair = null;
  double mshSharpEdgeAngle = 30;
  File simFile = null;
  GeometryPart geomPrt = null;
  Interface intrf = null;
  MeshContinuum mshCont = null;
  PlaneSection plane = null;
  Region region = null;
  ReportMonitor repMon = null;
  String noneString = "none";
  String sayPreffixString = "[*]";
  String simName = null;
  String simTitle = null;
  String simPath = null;
  String string = "";
  Simulation sim = null;
  Units unit_C = null;
  Units unit_m = null;
  Units unit_mm = null;
  Vector objVec = new Vector();

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
    unit_C = ((Units) sim.getUnitsManager().getObject("C"));
    unit_m = ((Units) sim.getUnitsManager().getObject("m"));
    unit_mm = ((Units) sim.getUnitsManager().getObject("mm"));
    say("Simulation File: " + simFile.toString());
    say("Simulation Name: " + simTitle);
    say("Simulation Path: " + simPath);
  }

  public void finalize(){
      printAction("DONE!");
  }

  /****************************************************
   *
   * Useful Methods Area
   *
   ****************************************************/

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

  public void checkFreeEdgesAndNonManifoldsOnAllSurfaceMeshedRegions() {
    printAction("Checking For Free Edges and Non-Manifold Edges & Vertices");
    say("Checking Surface Mesh in All regions");

    Scene scene_1 = sim.getSceneManager().createScene("Repair Surface");
    scene_1.initializeAndWait();

    SurfaceMeshWidget srfMshWidget = queryRemeshedSurface().startSurfaceMeshWidget(scene_1);
    setVerboseOff();
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

  public void findAllPartsContacts(double tol_meters){
    printAction("Finding All Part Contacts");
    say("Tolerance (m): " + tol_meters);
    queryGeometryRepresentation().findPartPartContacts(getAllLeafParts2(), tol_meters);
    sayOK();
  }

  public void genSurfaceMesh() {
    printAction("Generating Surface Mesh");
    sim.get(MeshPipelineController.class).generateSurfaceMesh();
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
        lmpVec.add((LeafMeshPart) gp);
    }
    sayA("Leaf Mesh Parts found: " + lmpVec.size());
    return lmpVec;
  }

  public Collection<LeafMeshPart> getAllLeafMeshPartsByName(String regexPatt){
    sayA(String.format("Getting all Regions by REGEX pattern: \"%s\"", regexPatt));
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

  public Vector<MeshPart> getAllLeafParts2(){
    Vector<MeshPart> vecMP = new Vector<MeshPart>();
    for(GeometryPart gp : getAllLeafParts()){
        vecMP.add((MeshPart) gp);
    }
    return vecMP;
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

  public CompositePart getCompositePartByName(String regexPatt){
    /*
     * Loop in all Composite Parts and returns the first match, given the name pattern
     */
    sayA(String.format("Getting Composite Part by REGEX pattern: \"%s\"", regexPatt));
    setVerboseOff();
    CompositePart foundCP = null;
    for(CompositePart cp : getAllCompositeParts()){
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
    for(MeshPart mp : getAllLeafParts2()){
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
    for(MeshPart mp : getAllLeafParts2()){
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

  public String getTime() {
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    Date date = new Date();
    return dateFormat.format(date);
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

  public void imprintAllPartsByCADMethod(){
    printAction("Imprinting All Parts by CAD Method");
    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafParts2(), "CAD", 1.0E-4);
    sayOK();
  }

  public void imprintAllPartsByDiscreteMethod(double tolerance){
    printAction("Imprinting All Parts by Discrete Method");
    sim.get(MeshActionManager.class).imprintDiscreteParts(getAllLeafParts2(), "Discrete", tolerance);
    sayOK();
  }

  public void imprintPartsByCADMethod(Vector<MeshPart> vecMP){
    printAction("Imprinting Parts by CAD Method");
    say("Parts: ");
    for(MeshPart mp : vecMP){
        say("  " + mp.getPathInHierarchy());
    }
    sim.get(MeshActionManager.class).imprintDiscreteParts(vecMP, "CAD", 1.0E-4);
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

  public SurfaceRep queryRemeshedSurface(){
    try{
        return ((SurfaceRep) sim.getRepresentationManager().getObject("Remeshed Surface"));
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

  public void removeLeafMeshPart(LeafMeshPart lmp){
    printAction("Removing Leaf Mesh Part: " + lmp.getPresentationName());
    sim.get(SimulationPartManager.class).removePart(lmp);
    sayOK();
    //sim.get(SimulationPartManager.class).removeParts(new NeoObjectVector(new Object[] {lmp}));
  }

  public void removeLeafMeshPartsByName(String regexPatt){
    printAction(String.format("Remove Leaf Mesh Part by REGEX pattern: \"%s\"", regexPatt));
    Collection<LeafMeshPart> colLMP = getAllLeafMeshPartsByName(regexPatt);
    say("Leaf Mesh Parts to be removed: " + colLMP.size());
    sim.get(SimulationPartManager.class).removeParts(colLMP);
    say("Removed");
    say("");
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
      String newName = simTitle + "_" + suffix;
      saveSim(newName);
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

  public void setMeshProximityRefinement(MeshContinuum mshCont){
    mshCont.getModelManager().getModel(ResurfacerMeshingModel.class).setDoProximityRefinement(true);
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

  public void setVerboseOff(){
    verbose = false;
  }

  public void setVerboseOn(){
    verbose = true;
  }

  public String str2regex(String text){
    return ".*" + text + ".*";
  }

}
