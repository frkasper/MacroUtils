import macroutils.*;
import java.io.*;
import java.util.*;
import star.common.*;
import star.flow.*;
import star.meshing.AutoMeshOperation;

/**
 * Laminar fully developed flow in a pipe (Poiseuille flow).
 * 
 * This Demo shows how Grid Convergence Index can be calculated in STAR-CCM+ 
 * using a periodic flow modeling strategy.
 * 
 * This Demo was modified in Macro Utils v3.3 and it works best when using the 
 * Generalized Cylinder Mesher which (to date) is still not available as Parts 
 * Based Meshing in STAR-CCM+.
 * 
 * 
 * Geometry:
 *            L  
 *      +-----------+
 *      |           |
 *    r * O(0,0,0)  |
 *      |           |
 *      +-----------+
 * 
 * @since Macro Utils v3.1.
 * @author Fabio Kasper
*/
public class Demo14_GCI extends MacroUtils {
    
  final double R = 25;                  //-- Pipe Radius in mm
  final double L = R / 2;               //-- Pipe length in mm
  final double rho = 1000.;             //-- Density in kg/m^3
  final double mu = 0.001;              //-- Viscosity in Pa.s

  public void execute() {
    _initUtils();
    simTitle = "Demo14_GCI";
    pre();
    post();
    solveGrids();
    postGCI();
    _finalize();
  }
  
  void pre() {
    defCamView = readCameraView("cam1|-2.733933e-04,-2.870785e-04,2.535976e-03|9.205652e-02,1.539672e-02,1.080102e-01|1.614315e-02,9.868431e-01,-1.608729e-01|2.689607e-02|1");
    if (hasValidVolumeMesh()) {
        sayLoud("Volume Mesh Found. Skipping Prep");
        return;
    }
    cadBody = create3DCad_Cylinder(R, L, coord0, unit_mm, Z, "Pipe");
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    geometryParts.add(geomPrt);
    region = assignPartToRegion(geomPrt);
    //--
    mshBaseSize = baseSize0;
    mshSrfSizeMin = 80;
    prismsLayers = 3;
    prismsRelSizeHeight = 40;
    prismsNearCoreAspRat = 0.5;
    autoMshOp = createMeshOperation_AutomatedMesh(geometryParts, getMeshers(true, false, POLY, true), "My Mesh");    
    //--
    urfVel = 0.95;
    urfP = 0.15;
    maxIter = 2000;
    physCont = createPhysicsContinua(_3D, _STEADY, _LIQUID, _SEGREGATED, _INCOMPRESSIBLE, _ISOTHERMAL, _LAMINAR, false, false, true);
    setMaterialProperty(physCont, "H2O", varDen, rho, unit_kgpm3);
    setMaterialProperty(physCont, "H2O", varVisc, mu, unit_Pa_s);
    setInitialCondition(physCont, varVel, new double[] {0, 0, 0.1}, unit_mps);
    //--
    ff1 = createFieldFunction("r", "sqrt(pow($$Position[0], 2) + pow($$Position[1], 2))", dimLength);
    ff2 = createFieldFunction("dPdL", "1", null);
    ff3 = createFieldFunction("Analytical Solution", String.format("$dPdL / %g * (pow(%g, 2)-pow($r, 2))", 4*mu, R/1000.), dimVel);
    //--
    region.getConditions().get(MomentumUserSourceOption.class).setSelected(MomentumUserSourceOption.SPECIFIED);
    region.getValues().get(MomentumUserSource.class).getMethod(ConstantVectorProfileMethod.class).getQuantity().setDefinition("[0.0, 0.0, $dPdL]");
    //--
    bdry1 = getBoundary("z0");
    bdry2 = getBoundary("z1");
    bdry3 = getBoundary(bcDefault); 
    //-- Small trick to make prisms disappear in those boundaries.
    setBC_Symmetry(bdry1);
    setBC_Symmetry(bdry2);
    //--
    DirectBoundaryInterface dbi = createDirectInterfacePair(bdry1, bdry2);
    dbi.getTopology().setSelected(InterfaceConfigurationOption.PERIODIC);
    dbi.getPeriodicTransform().getPeriodicityOption().setSelected(PeriodicityOption.TRANSLATION);
    dbi.setPresentationName(intrfName);
    //--
    genVolumeMesh();
    createScene_Mesh();
  }
  
  void solveGrids() {
    int gridN = 1;
    ArrayList<Double> sizes = new ArrayList();
    ArrayList<Double> dps = new ArrayList();
    ArrayList<String> grids = new ArrayList();
    prettifyMe();
    setMonitorsNormalizationOFF();
    openAllPlots();
    while (true) {
        //--
        double baseSize = baseSize0 / Math.pow(gridRefFactor, gridN-1);
        autoMshOp = (AutoMeshOperation) getMeshOperation(".*");
        setMeshBaseSize(autoMshOp, baseSize, defUnitLength);
        sayLoud("Solving " + getGridNumber(gridN) + " for base size " + baseSize + defUnitLength.getPresentationName());
        genVolumeMesh();
        //--
        runCase(50);                //-- To avoid premature activation of Stopping Criteria.
        runCase();
        String s = getGridNumber(gridN).replaceAll(" ", "0");
        hardCopyPictures(1280, 720, "", "_" + s);
        saveSim(s + "_" + simTitle, false);
        files.add(new File(sim.getSessionDirFile(), sim.getPresentationName() + ".sim"));
        //--
        rep = getReport(repMean);
        sizes.add(evalGCI_getGridSize());
        dps.add(rep.getReportMonitorValue());
        grids.add(sim.getPresentationName());
        evalGCI(sizes, dps, grids);
        //--
        if (gridN >= maxGrids || queryVolumeMesh().getCellCount() > maxGridSize) {
            sayLoud("Finishing run");
            say(getGridNumber(gridN));
            say("Cell count: " + queryVolumeMesh().getCellCount());
            break;
        }
        gridN++;
        setSolverMaxIterations(getIteration() + maxIter);
    }
  }
  
  void post() {
    updEvent = createUpdateEvent_Iteration(100, 0);
    ff = getFieldFunction(varVel).getComponentFunction(2);
    bdry1 = getDirectBoundaryInterfaceByName(intrfName).getInterfaceBoundary0();
    createReportMassFlowAverage(bdry1, repMean, ff, defUnitVel);
    setUpdateEvent(monPlot, updEvent);
    createStoppingCriteria(repMon, "Asymptotic", 0.0001, 125);
    createReportMaximum(bdry1, repMax, ff, defUnitVel);
    setUpdateEvent(monPlot, updEvent);
    createStoppingCriteria(repMon, "Asymptotic", 0.0001, 125);
    //-- Line Probe
    namedObjects.addAll(getAllRegions());
    namedObjects2.add(createDerivedPart_Line(namedObjects, 
            new double[] {-0.98 * R, 0, L / 2}, new double[] {0.98 * R, 0, L / 2}, 20));
    XYPlot plot = createPlot_XY(namedObjects2, getFieldFunction("Position").getComponentFunction(0), 
            defUnitLength, ff, defUnitVel);
    plot.setPresentationName(plotName);
    setUpdateEvent(plot, updEvent);
    YAxisType yxN = plot.getYAxes().getDefaultAxis();
    YAxisType yxA = plot.getYAxes().createAxisType();
    InternalDataSet idsN = (InternalDataSet) yxN.getDataSets().getDataSets().iterator().next();
    InternalDataSet idsA = (InternalDataSet) yxA.getDataSets().getDataSets().iterator().next();
    idsA.getSymbolStyle().setStyle(3);
    idsA.getSymbolStyle().setSize(12);
    idsA.getSymbolStyle().setColor(colorDarkGreen);
    yxA.setFieldFunction(ff3);
    idsN.setSeriesName("Numerical");
    idsA.setSeriesName("Analytical");
    //--
  }

  void postGCI() {
    starPlot = getPlot(plotName);
    evalGCI(starPlot, files);
  }
  
  private String getGridNumber(int n) {
      return String.format("Grid %2d", n);
  }

  double baseSize0 = 5;                 //-- Initial Mesh Size in mm.
  double gridRefFactor = 2;             //-- Refinement factor between grids.
  double gridSafetyFactor = 1.25;
  double maxGridSize = 1e7;
  int maxGrids = 3;
  String plotName = "Numerical vs Analytical Solutions";
  String repMean = "Vmean";
  String repMax = "Vmax";
  
}
