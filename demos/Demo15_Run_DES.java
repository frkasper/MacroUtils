import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.report.Monitor;
import star.common.Dimensions;
import star.common.FieldFunctionTypeOption;
import star.common.StarMacro;
import star.keturb.KeTurbSpecOption;
import star.kwturb.KwTurbSolver;
import star.vis.LookupTable;
import star.vis.PartDisplayer;
import star.vis.ScalarDisplayer;
import star.vis.Scene;
import star.vis.ThresholdMode;
import star.vis.ThresholdPart;
import star.vis.VisView;

/**
 * Runs the SST K-Omega DES model with IDDES formulation on a square channel under Re_L ~ 10k.
 *
 * Notes:
 * <ul>
 * <li> For a mesh size of 2mm the simulation takes roughly 5 hours in 12 cores running in double
 * precision (R8);
 * <li> If machine power is available run this macro with a mesh size of 1mm while adjusting
 * timestep;
 * <li> Residence time is around 0.1s. This macro will solve for 5 flows through;
 * <li> Some inlet values are taken from a table ran in fully developed flow condition (periodic).
 * </ul>
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
public class Demo15_Run_DES extends StarMacro {

    private final String meshOpName = "Polys And Prisms";
    private MacroUtils mu;
    private UserDeclarations ud;

    private final double L = 50;
    final double H = 5.0 * L;
    final double V = 1.0 * L;
    private final double bsDES = 2.5;        //-- Mesh Size in mm for DES run.
    private final double bsRANS = 6.;        //-- Mesh Size in mm for RANS run.
    private final double dtDES = 1e-4;       //-- Timestep size in seconds for DES run.
    private final double mu_g = 1.855e-05;   //-- Viscosity.
    private final double tau = 0.1;          //-- Approximate Residence Time in seconds.
    private final double maxTime = 3 * tau;  //-- Maximum Physical Time to be solved.

    @Override
    public void execute() {

        initMacro();

        runRANS();

        runDES();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void fixScalarDisplayer(Scene scn, boolean msh, double[] range, double w,
            String fmt, LookupTable cmap, DoubleVector dv, VisView cam) {
        mu.io.say.action("Fixing Scalar Displayer for Scene: " + scn.getPresentationName(), true);
        ScalarDisplayer sd = (ScalarDisplayer) mu.get.scenes
                .displayerByREGEX(scn, "Scalar.*", true);
        sd.setDisplayMeshBoolean(msh);
        sd.getLegend().setLabelFormat(fmt);
        sd.getLegend().setLookupTable(cmap);
        if (range != null) {
            int i = (int) range[1];
            if (i == 1) {
                i++;
            }
            sd.getLegend().setNumberOfLabels(i + 1);
        }
        mu.set.scene.cameraView(scn, cam, true);
        if (range != null) {
            sd.getScalarDisplayQuantity().setAutoRange(false);
            sd.getScalarDisplayQuantity().setRange(range);
        }
        if (w > 0.) {
            sd.getLegend().setWidth(w);
            sd.getLegend().setPositionCoordinate(dv);
        }
    }

    private void initMacro() {
        mu = new MacroUtils(getActiveSimulation());
        ud = mu.userDeclarations;
        ud.simTitle = "Demo15_Run_DES";
        ud.vv1 = mu.io.read.cameraView("cam1|5.530468e-02,-1.190976e-02,-7.199782e-02"
                + "|4.282329e-01,2.739476e-01,5.776558e-01|-1.501948e-01,9.338231e-01,-3.246781e-01"
                + "|8.359997e-02|1", true);
        ud.vv2 = mu.io.read.cameraView("cam2|1.841383e-02,2.223453e-02,-3.292230e-05"
                + "|1.841383e-02,2.223453e-02,7.419417e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|4.180352e-02|1", true);
        ud.defCamView = ud.vv1;
    }

    private void runDES() {
        ud.mshOp = mu.get.mesh.operation(meshOpName, true);
        ud.region = mu.get.regions.byREGEX(".*", true);
        setupPreDES();
        setupPostDES();
        mu.saveSim(ud.simTitle + "_DES_Ready");
        mu.run();
    }

    private void runRANS() {
        if (mu.check.has.volumeMesh()) {
            return;
        }
        setupPreRANS();
        setupPostRANS();
        mu.run();
        mu.saveSim(ud.simTitle + "_SS");
        mu.io.write.all(getActiveSimulation().getPresentationName());
    }

    private void setupPartDisplayer(Scene scn) {
        PartDisplayer pd = mu.add.scene.displayer_Geometry(scn,
                new ArrayList<>(mu.get.boundaries.all(true)));
        pd.setPresentationName("Channel");
        pd.setOpacity(0.1);
    }

    private void setupPostDES() {
        //-- Useful Reports and Update Events.
        mu.templates.post.unsteadyReports();
        ud.updEvent1 = mu.add.tools.updateEvent_DeltaTime(5 * dtDES, ud.unit_s, true);
        ud.updEvent1.setPresentationName("Pics");
        ud.updEvent2 = mu.add.tools.updateEvent_Range(mu.get.monitors.physicalTime(),
                StaticDeclarations.Operator.GREATER_THAN_OR_EQUALS, 2 * tau);
        ud.updEvent2.setPresentationName("Monitors");
        ud.updEvent3 = mu.add.tools.updateEvent_TimeStep(1000, 0);
        ud.updEvent3.setPresentationName("AutoSave");
        //--
        mu.set.object.updateEvent(mu.get.monitors.byREGEX("Pressure Drop", true),
                ud.updEvent1, true);
        ud.ff1 = mu.add.tools.fieldFunction("My Q Criterion", "$Qcriterion * (1 - $MenterKwTurbF1)",
                new Dimensions(), FieldFunctionTypeOption.Type.SCALAR);
        ud.ff2 = mu.add.tools.fieldFunction("Courant Number on LES Portion",
                "$CourantNumber * (1 - $SaTurbDesFd)", new Dimensions(),
                FieldFunctionTypeOption.Type.SCALAR);
        ud.namedObjects.clear();
        ud.namedObjects.add(mu.get.parts.byREGEX("Plane", true));
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff2, ud.unit_Dimensionless, false);
        ud.scene.setPresentationName("LES Timestep Assessment");
        //--
        //-- In order to use the above additional storage is needed.
        KwTurbSolver kw = (KwTurbSolver) mu.get.solver.byClass(KwTurbSolver.class);
        kw.setLeaveTemporaryStorage(true);
        //--
        //-- If one is using a finer mesh (<= 2mm) it is better to tune the post processing.
        double isoVal = 5e4;
        double[] tvrRange = new double[]{ 0.0, 5.0 };
        if (mu.get.mesh.baseSize(ud.mshOp, true).getRawValue() <= 2.0) {
            isoVal = 1e5;
            tvrRange = new double[]{ 0.0, 2.0 };
        }
        //--
        //-- Pressure Probe
        ud.namedObjects.clear();
        ud.namedObjects.addAll(mu.get.regions.all(true));
        ud.point = mu.add.derivedPart.point(ud.namedObjects, new double[]{ 5, 10, 0.0 });
        mu.add.report.maximum(ud.point, "P1",
                mu.get.objects.fieldFunction(StaticDeclarations.Vars.P), ud.unit_Pa, true);
        //--
        //-- Ideally, the number of cells in LES region where CFL > 1 should be zero
        ThresholdPart thrp = mu.add.derivedPart.threshold(ud.namedObjects, ud.ff2,
                new double[] {0, 1}, ThresholdMode.ABOVE_TAG);
        thrp.setPresentationName("Cells of CFL > 1 on LES Portion");
        ud.namedObjects2.clear();
        ud.namedObjects2.add(thrp);
        ud.rep = mu.add.report.elementCount(ud.namedObjects2, thrp.getPresentationName(),
                ud.unit_Dimensionless, true);
        mu.add.report.monitorAndPlot(ud.rep, null, null, true);
        //--
        //-- A few more Scenes
        ud.namedObjects2.clear();
        ud.namedObjects2.add(
                mu.add.derivedPart.isosurface(ud.namedObjects, ud.ff1, isoVal,
                        ud.unit_Dimensionless));
        Scene scnStructures = mu.add.scene.scalar(ud.namedObjects2,
                mu.get.objects.fieldFunction(StaticDeclarations.Vars.TVR.getVar(), true),
                ud.defUnitPress, true);
        scnStructures.setPresentationName("Structures");
        setupPartDisplayer(scnStructures);
        ud.scenes.add(scnStructures);
        fixScalarDisplayer(scnStructures, false, tvrRange, 0, "%.0f",
                mu.get.objects.colormap(StaticDeclarations.Colormaps.LAND_ELEVATION),
                ud.dv0, ud.vv1);
        ud.namedObjects2.clear();
        ud.namedObjects2.add(mu.get.boundaries.byREGEX(ud.bcWalls, true));
        PartDisplayer pd = mu.add.scene.displayer_Geometry(ud.scene, ud.namedObjects2);
        pd.setPresentationName("Channel");
        pd.setOpacity(0.1);
        //--
        ud.ff2 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.YPLUS.getVar(), true);
        Scene scnYPlus = mu.add.scene.scalar(ud.namedObjects2, ud.ff2,
                ud.unit_Dimensionless, false);
        scnYPlus.setPresentationName(ud.ff2.getPresentationName());
        ud.scenes.add(scnYPlus);
        fixScalarDisplayer(scnYPlus, false, new double[]{ 0, 2 }, 0, "%.1f",
                mu.get.objects.colormap(StaticDeclarations.Colormaps.BLUE_RED_BALANCED),
                ud.dv0, ud.vv1);
        //--
        //-- Instantaneous and Mean Velocity Scalar Scenes
        ud.ff3 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), true);
        mu.add.derivedPart.fieldMeanMonitor(new ArrayList<>(mu.get.regions.all(true)),
                ud.ff3.getMagnitudeFunction(), ud.updEvent2);
        ud.namedObjects.clear();
        ud.namedObjects.add(mu.get.parts.byREGEX("Plane", true));
        String vel = StaticDeclarations.Vars.VEL.getVar();
        String tvr = StaticDeclarations.Vars.TVR.getVar();
        String[] vars = { vel, "Mean.*" + vel + ".*", tvr, "LES" };
        for (String var : vars) {
            double[] range = { 0, 6 };
            String fmt = "%.0f";
            LookupTable cmap = mu.get.objects.colormap(StaticDeclarations.Colormaps.ORCHID_GREEN);
            if (var.equals("LES")) {
                ud.scene = mu.get.scenes.byREGEX(var + ".*", true);
                range[1] = 1;
                fmt = "%.1f";
            } else {
                ud.ff = mu.get.objects.fieldFunction(var, true);
                ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_mps, false);
                ud.scene.setPresentationName(ud.ff.getPresentationName());
                if (var.equals(tvr)) {
                    range[1] = tvrRange[1] + 1;
                }
                if (!var.contains("Mean")) {
                    ud.scenes.add(ud.scene);
                }
            }
            ud.scene.setAxesVisible(false);
            DoubleVector dv = new DoubleVector(new double[]{ 0.42, 0.025 });
            fixScalarDisplayer(ud.scene, true, range, 0.55, fmt, cmap, dv, ud.vv2);
        }
        //--
        //-- Final makeup.
        for (Scene scn : ud.scenes) {
            scn.setPresentationName(ud.simTitle + "_" + scn.getPresentationName());
            mu.set.scene.updateEvent(scn, ud.updEvent1);
            mu.set.scene.saveToFile(scn, ud.picResX, ud.picResY);
            mu.add.scene.annotation_Time(scn, ud.unit_s, "%6.3f", new double[]{ 0.415, 0.92, 0.0 });
        }
        for (Monitor m : mu.get.monitors.all(true)) {
            if (!mu.check.is.report(m)) {
                continue;
            }
            mu.set.object.updateEvent(m, ud.updEvent1, true);
        }
        mu.set.autoSave(ud.updEvent3, 3);
    }

    private void setupPostRANS() {
        ud.rep = mu.add.report.pressureDrop(mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true),
                mu.get.boundaries.byREGEX(".*" + ud.bcOutlet, true),
                "Pressure Drop", ud.defUnitPress, true);
        ud.repMon = mu.get.monitors.fromReport(ud.rep, true);
        mu.add.solver.stoppingCriteria(ud.repMon, StaticDeclarations.StopCriteria.ASYMPTOTIC,
                0.01, 50);
        ud.namedObjects.addAll(mu.get.boundaries.all(true));
        ud.ff1 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.P);
        Scene scn1 = mu.add.scene.scalar(ud.namedObjects, ud.ff1, ud.defUnitPress, true);
        scn1.setPresentationName(ud.ff1.getPresentationName());
        ud.plane = mu.add.derivedPart.sectionPlaneZ(StaticDeclarations.COORD0);
        ud.plane.setPresentationName("Plane");
        ud.namedObjects2.add(ud.plane);
        Scene scn2 = mu.add.scene.vector(ud.namedObjects2, false);
        setupPartDisplayer(scn2);
    }

    private void setupPreDES() {
        mu.get.solver.stoppingCriteria(".*Pressure.*", true).setIsUsed(false);
        mu.clear.solutionHistory();
        //--
        mu.set.mesh.baseSize(ud.mshOp, bsDES, ud.unit_mm, true);
        mu.update.volumeMesh();
        ud.trnTimestep = dtDES;
        ud.trn2ndOrder = true;
        ud.trnInnerIter = 6;
        ud.trnMaxTime = maxTime;
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.IMPLICIT_UNSTEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.DES_SST_KW_IDDES);
        mu.set.physics.materialProperty(ud.physCont, "Air", StaticDeclarations.Vars.VISC,
                mu_g, ud.unit_Pa_s);
        ud.region.setPhysicsContinuum(ud.physCont);
        mu.set.solver.ultraAggressiveSettings();
        mu.set.solver.linearSolverConvergenceTolerance(1E-4, true);
        //-- Activate Synthetic Eddy Generation at Inlet if desired (at a higher CPU cost).
        ud.bdry = mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true);
        mu.enable.syntheticEddyMethod(mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true));
    }

    private void setupPreRANS() {
        ud.cadPrt = mu.add.geometry.block3DCAD(new double[]{ -L, -V, -L / 2 },
                new double[]{ 0, L, L / 2 }, ud.unit_mm);
        ud.cadPrt.setPresentationName("Vertical");
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "y0", true).setPresentationName(ud.bcInlet);
        ud.geometryParts.add(ud.cadPrt);
        ud.cadPrt = mu.add.geometry.block3DCAD(new double[]{ 0, 0, -L / 2 },
                new double[]{ H, L, L / 2 }, ud.unit_mm);
        ud.cadPrt.setPresentationName("Horizontal");
        ud.geometryParts.add(ud.cadPrt);
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "x1", true).setPresentationName(ud.bcOutlet);
        //-- Unite bodies
        ud.mshOpPrt = mu.add.meshOperation.unite(ud.geometryParts);
        ud.geometryParts2.add(ud.mshOpPrt);
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                true);
        //--
        ud.mshBaseSize = bsRANS;
        ud.mshOptCycles = 4;
        ud.mshQualityThreshold = 0.7;
        ud.prismsLayers = 6;
        ud.prismsRelSizeHeight = 60;
        ud.prismsNearCoreAspRat = 0.5;
        ud.autoMshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.POLY_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.autoMshOp.setPresentationName(meshOpName);
        //--
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.RKE_2LAYER);
        mu.set.physics.materialProperty(ud.physCont, "Air", StaticDeclarations.Vars.VISC,
                mu_g, ud.unit_Pa_s);
        mu.set.solver.aggressiveSettings();
        //--
        ud.bdry = mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true);
        mu.set.boundary.asVelocityInlet(ud.bdry, 1.0, 20.0, 0.05, 10.0);
        ud.bdry.getConditions().get(KeTurbSpecOption.class)
                .setSelected(KeTurbSpecOption.Type.INTENSITY_LENGTH_SCALE);
        ud.table = mu.add.tools.table("TableFromPeriodicRun.csv");
        mu.set.boundary.values(ud.bdry, ud.table);
        //--
        mu.set.boundary.asPressureOutlet(mu.get.boundaries.byREGEX(".*" + ud.bcOutlet, true),
                0.0, 21.0, 0.05, 10.0);
        mu.set.boundary.combine(mu.get.boundaries.allByREGEX(".*\\...", false))
                .setPresentationName(ud.bcWalls);
        //--
        mu.update.volumeMesh();
        ud.scene = mu.add.scene.mesh();
    }

}
