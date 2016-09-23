import java.io.*;
import java.util.*;
import macroutils.*;
import macroutils.templates.*;
import star.common.*;
import star.flow.*;

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
public class Demo14_GCI extends StarMacro {

    final double R = 25;                  //-- Pipe Radius in mm
    final double L = R / 2;               //-- Pipe length in mm
    final double den = 1000.;             //-- Density in kg/m^3
    final double visc = 0.001;            //-- Viscosity in Pa.s

    public void execute() {

        initMacro();

        preRun();

        solveGrids();

        assessGCI();

        mu.saveSim();
        
    }

    public void assessGCI() {
        ud.starPlot = mu.get.plots.byREGEX(plotName, true);
        mu.templates.gci.evaluate(ud.starPlot, ud.files);
    }

    private void initMacro() {
        mu = new MacroUtils(getActiveSimulation());
        ud = mu.userDeclarations;
        ud.simTitle = "Demo14_GCI";
    }

    public void preRun() {
        ud.defCamView = mu.io.read.cameraView("cam1|-2.733933e-04,-2.870785e-04,2.535976e-03|9.205652e-02,1.539672e-02,1.080102e-01|1.614315e-02,9.868431e-01,-1.608729e-01|2.689607e-02|1", true);
        if (mu.check.has.volumeMesh()) {
            mu.io.say.loud("Volume Mesh Found. Skipping Prep");
            return;
        }
        setupPhysics();
        setupRegion();
        setupMesh();
        setupBCs();
        setupPost();
    }

    public void solveGrids() {
        int n = 1;
        ArrayList<Double> sizes = new ArrayList();
        ArrayList<Double> dps = new ArrayList();
        ArrayList<String> grids = new ArrayList();
        mu.templates.prettify.all();
        mu.set.solver.aggressiveSettings();
        mu.open.allPlots(true);
        while (true) {
            //--
            double baseSize = baseSize0 / Math.pow(gridRefFactor, n - 1);
            mu.set.mesh.baseSize(ud.mshOp, baseSize, ud.defUnitLength, true);
            //--
            String gridN = String.format("Grid%03d", n);
            mu.io.say.loud(String.format("Solving %s for base size %g[%s].",
                    gridN, baseSize, ud.defUnitLength.getPresentationName()));
            mu.update.volumeMesh();
            //--
            mu.run();
            mu.io.write.all(ud.simTitle + "_" + gridN);
            File f = new File(ud.simPath, String.format("%s_%s.sim", ud.simTitle, gridN));
            mu.saveSim(f.toString());
            ud.files.add(f);
            //--
            TemplateGCI gci = mu.templates.gci;
            sizes.add(gci.getGridSize());
            dps.add(ud.rep1.getReportMonitorValue());
            grids.add(mu.getSimulation().getPresentationName());
            gci.evaluate(sizes, dps, grids);
            //--
            if (n >= maxGrids || mu.get.mesh.fvr().getCellCount() > maxGridSize) {
                mu.io.say.loud("Finishing run. Limits reached.");
                mu.io.say.value("Case", gridN, true, true);
                mu.io.say.cellCount();
                break;
            }
            n++;
            mu.set.solver.maxIterations(mu.get.solver.iteration() + ud.maxIter, true);
        }
    }

    private void setupBCs() {
        MomentumUserSourceOption muso = ud.region.getConditions().get(MomentumUserSourceOption.class);
        muso.setSelected(MomentumUserSourceOption.Type.SPECIFIED);
        MomentumUserSource mus = ud.region.getValues().get(MomentumUserSource.class);
        mu.set.object.physicalQuantity(mus.getMethod(ConstantVectorProfileMethod.class).getQuantity(),
                "[0.0, 0.0, $dPdL]", "Momentum Source", true);
        ud.bdryIntrf = mu.add.intrf.boundaryInterface(mu.get.boundaries.byREGEX("z0", true),
                mu.get.boundaries.byREGEX("z1", true), InterfaceConfigurationOption.Type.PERIODIC);
    }

    private void setupPlotData(InternalDataSet ids, SymbolShapeOption.Type type, StaticDeclarations.Colors color) {
        ids.getSymbolStyle().getSymbolShapeOption().setSelected(type);
        ids.getSymbolStyle().setColor(color.getColor());
    }

    private void setupMesh() {
        ud.mshBaseSize = baseSize0;
        ud.mshSrfSizeMin = 80;
        ud.prismsLayers = 3;
        ud.prismsRelSizeHeight = 40;
        ud.prismsNearCoreAspRat = 0.5;
        ud.mshOp = mu.add.meshOperation.directedMeshing_AutoMesh(mu.get.partSurfaces.byREGEX("z0", true),
                mu.get.partSurfaces.byREGEX("z1", true), 5, StaticDeclarations.Meshers.POLY_MESHER_2D,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.scene = mu.add.scene.mesh();
        ud.scene.open(true);
    }

    private void setupPhysics() {
        ud.urfVel = 0.95;
        ud.urfP = 0.15;
        ud.maxIter = 3000;
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.LIQUID,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.LAMINAR);
        mu.set.physics.materialProperty(ud.physCont, "H2O", StaticDeclarations.Vars.DEN, den, ud.unit_kgpm3);
        mu.set.physics.materialProperty(ud.physCont, "H2O", StaticDeclarations.Vars.VISC, visc, ud.unit_Pa_s);
        mu.set.physics.initialCondition(ud.physCont, StaticDeclarations.Vars.VEL.getVar(),
                new double[]{0, 0, 0.1}, ud.unit_mps);
        //--
        ud.ff1 = mu.add.tools.fieldFunction("r", "sqrt(pow($$Position[0], 2) + pow($$Position[1], 2))",
                ud.dimLength, FieldFunctionTypeOption.Type.SCALAR);
        ud.ff2 = mu.add.tools.fieldFunction("dPdL", "1", ud.dimDimensionless, FieldFunctionTypeOption.Type.SCALAR);
        ud.ff3 = mu.add.tools.fieldFunction("Analytical Solution",
                String.format("$dPdL / %g * (pow(%g, 2)-pow($r, 2))", 4 * visc, R / 1000.),
                ud.dimVel, FieldFunctionTypeOption.Type.SCALAR);
    }

    private void setupPost() {
        FieldFunction fx, fVz;
        fx = mu.get.objects.fieldFunction(StaticDeclarations.Vars.POS.getVar(), true).getComponentFunction(0);
        fVz = mu.get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), true).getComponentFunction(2);
        ud.bdry = ud.bdryIntrf.getInterfaceBoundary0();
        ud.rep1 = mu.add.report.massFlowAverage(ud.bdry, repMean, fVz, ud.defUnitVel, true);
        ud.rep2 = mu.add.report.maximum(ud.bdry, repMax, fVz, ud.defUnitVel, true);
        ud.mon = mu.get.monitors.byREGEX("Z-momentum", true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.MIN, 1e-6, 0);
        //-- Setup Plot
        ud.namedObjects.addAll(mu.get.regions.all(true));
        ud.namedObjects2.add(mu.add.derivedPart.line(ud.namedObjects,
                new double[]{-0.98 * R, 0, L / 2}, new double[]{0.98 * R, 0, L / 2}, 20));
        XYPlot plot = mu.add.plot.xy(ud.namedObjects2, fx, ud.defUnitLength, fVz, ud.defUnitVel);
        plot.setPresentationName(plotName);
        ud.updEvent = mu.add.tools.updateEvent_Iteration(100, 0);
        mu.set.object.updateEvent(plot, ud.updEvent, true);
        YAxisType yxN = plot.getYAxes().getDefaultAxis();
        YAxisType yxA = plot.getYAxes().createAxisType();
        InternalDataSet idsN = (InternalDataSet) yxN.getDataSetManager().getDataSets().iterator().next();
        InternalDataSet idsA = (InternalDataSet) yxA.getDataSetManager().getDataSets().iterator().next();
        setupPlotData(idsN, SymbolShapeOption.Type.EMPTY_CIRCLE, StaticDeclarations.Colors.BLACK);
        setupPlotData(idsA, SymbolShapeOption.Type.FILLED_TRIANGLE, StaticDeclarations.Colors.DARK_GREEN);
        yxA.getScalarFunction().setFieldFunction(ud.ff3);
        idsN.setSeriesName("Numerical");
        idsA.setSeriesName("Analytical");
        mu.set.object.plotAxesTitles(plot,
                String.format("Radius [%s]", ud.defUnitLength.getPresentationName()),
                String.format("Axial Velocity [%s]", ud.defUnitVel.getPresentationName()), true);
    }

    private void setupRegion() {
        ud.cadPrt = mu.add.geometry.cylinder3DCAD(R, L, StaticDeclarations.COORD0, ud.unit_mm, StaticDeclarations.Axis.Z);
        ud.geometryParts.add(ud.cadPrt);
        ud.region = mu.add.region.fromPart(ud.cadPrt, StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT, StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
    }

    private MacroUtils mu;
    private UserDeclarations ud;
    private final double baseSize0 = 5;             //-- Initial Mesh Size in mm.
    private final double gridRefFactor = 2;         //-- Refinement factor between grids.
    private final double gridSafetyFactor = 1.25;
    private final double maxGridSize = 1e7;
    private final int maxGrids = 3;
    private final String plotName = "Numerical vs Analytical Solutions";
    private final String repMean = "Vmean";
    private final String repMax = "Vmax";

}
