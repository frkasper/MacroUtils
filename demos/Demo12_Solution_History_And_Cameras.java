import macroutils.*;
import macroutils.templates.*;
import star.common.*;
import star.meshing.*;
import star.post.*;
import star.vis.*;

/**
 * Pseudo 2D Laminar Vortex Shedding under Reynolds of 500.
 *
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
 */
public class Demo12_Solution_History_And_Cameras extends StarMacro {

    public void execute() {

        initMacro();

        solveSS();

        solveTRN();

        postTRN();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo12_Solution_History_And_Cameras";
        ud.vv1 = mu.io.read.cameraView("cam1|1.516797e-01,-4.188893e-03,-6.132604e-03|1.516797e-01,-4.188893e-03,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|1.165985e-01|1", true);
        ud.vv2 = mu.io.read.cameraView("cam2|5.502414e-02,3.902467e-04,-1.586686e-04|5.502414e-02,3.902467e-04,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|4.309526e-02|1", true);
        ud.vv3 = mu.io.read.cameraView("cam3|3.000000e-01,3.902467e-04,-1.586686e-04|3.000000e-01,3.902467e-04,1.101325e+00|0.000000e+00,1.000000e+00,0.000000e+00|4.309526e-02|1", true);
        ud.defCamView = ud.vv1;
    }

    void pre() {
        ud.mshBaseSize = 5;
        ud.prismsLayers = 4;
        ud.prismsRelSizeHeight = 25;
        ud.mshSrfSizeMin = 6.25;
        ud.mshTrimmerMaxCellSize = 100.;
        //--
        ud.denAir = 1.0;
        ud.viscAir = 1e-5;
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.TWO_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.CONSTANT,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.LAMINAR);
        mu.set.physics.materialProperty(ud.physCont, "Air", StaticDeclarations.Vars.VISC, ud.viscAir, ud.unit_Pa_s);
        mu.set.physics.materialProperty(ud.physCont, "Air", StaticDeclarations.Vars.DEN, ud.denAir, ud.unit_kgpm3);

        //--
        String s = "Channel";
        double w = 2 * ud.mshBaseSize;
        ud.cadPrt = mu.add.geometry.block3DCAD(new double[]{-150, -75, 0}, new double[]{400, 75, w}, ud.unit_mm);
        ud.cadPrt.setPresentationName(s);
        ud.geometryParts.add(ud.cadPrt);

        double r = 10., l = 2 * w;
        ud.defTessOpt = StaticDeclarations.Tessellation.VERY_FINE;
        ud.cadPrt2 = mu.add.geometry.cylinder3DCAD(r, l, new double[]{0, 0, -l / 2}, ud.unit_mm,
                StaticDeclarations.Axis.Z);
        ud.geometryParts.add(ud.cadPrt2);

        ud.mshOpPrt = mu.add.meshOperation.subtract(ud.geometryParts, ud.cadPrt);
        ud.geometryParts2.add(ud.mshOpPrt);
        ud.mshOp = mu.add.meshOperation.badgeFor2D(ud.geometryParts2);
        ud.mshOp.setPresentationName("My 2D Badging Operation");
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        ud.autoMshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.TRIMMER_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.autoMshOp.setPresentationName("My Mesh");
        ud.geometryObjects.addAll(mu.get.partSurfaces.allByREGEX(ud.mshOpPrt, s + ".*", true));
        ud.mshCtrl = mu.add.meshOperation.surfaceControl((AutoMeshOperation) ud.autoMshOp, ud.geometryObjects, 0, 0, 0);
        ud.mshCtrl.setPresentationName("No Prisms");

        double vel = Re * ud.viscAir / (ud.denAir * 2 * r * ud.unit_mm.getConversion());
        for (Boundary b : mu.get.boundaries.all(false)) {
            String name = b.getPresentationName();
            if (name.matches(".*y.")) {
                mu.set.boundary.asFreeSlipWall(b);
            }
            if (name.matches(".*z.")) {
                mu.set.boundary.asSymmetry(b);
            }
            if (name.matches(".*x0")) {
                mu.set.boundary.asVelocityInlet(b, vel, 0., 0., 0.);
            }
            if (name.matches(".*x1")) {
                mu.set.boundary.asPressureOutlet(b, 0., 0., 0., 0.);
            }
        }

        ud.simpleBlkPrt1 = mu.add.geometry.block(new double[]{-5 * r, -4 * r, -l},
                new double[]{200 * r, 4 * r, l}, ud.unit_mm);
        ud.simpleBlkPrt1.setPresentationName("Level1");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt1);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.autoMshOp, ud.geometryParts, 50.);
        ud.mshCtrl.setPresentationName("Level1");
        ud.simpleBlkPrt2 = mu.add.geometry.block(new double[]{-3 * r, -2.5 * r, -l},
                new double[]{25 * r, 2.5 * r, l}, ud.unit_mm);
        ud.simpleBlkPrt2.setPresentationName("Level2");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt2);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.autoMshOp, ud.geometryParts, 25.);
        ud.mshCtrl.setPresentationName("Level2");
        ud.simpleBlkPrt3 = mu.add.geometry.block(new double[]{-0.25 * r, -1.5 * r, -l},
                new double[]{4 * r, 1.5 * r, l}, ud.unit_mm);
        ud.simpleBlkPrt3.setPresentationName("Level3");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt3);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.autoMshOp, ud.geometryParts, 12.5);
        ud.mshCtrl.setPresentationName("Level3");

        mu.update.volumeMesh();
    }

    void solveSS() {
        if (mu.check.has.volumeMesh()) {
            return;
        }
        pre();
        ud.maxIter = 100;
        mu.set.solver.aggressiveSettings();
        mu.run();
        postSS();
    }

    void solveTRN() {
        updateVariables();
        if (mu.check.is.unsteady() && mu.get.solver.physicalTime() > 0) {
            return;
        }
        //--
        mu.clear.solutionHistory();
        //--
        ud.trn2ndOrder = true;
        ud.trnInnerIter = 6;
        ud.trnTimestep = 0.005;
        ud.trnMaxTime = 12.;
        mu.enable.implicitUnsteady(ud.physCont);
        mu.set.solver.aggressiveSettings();
        mu.templates.post.unsteadyReports();
        mu.disable.residualMonitorsNormalization();
        ud.namedObjects.addAll(mu.get.boundaries.allByREGEX("Cyl.*", false));
        ud.rep1 = mu.add.report.force(ud.namedObjects, "Fx", new double[]{1, 0, 0}, true);
        ud.rep2 = mu.add.report.force(ud.namedObjects, "Fy", new double[]{0, 1, 0}, true);
        mu.templates.prettify.all();
        ud.updEvent1 = mu.add.tools.updateEvent_DeltaTime(0.01, ud.unit_s, false);
        ud.updEvent2 = mu.add.tools.updateEvent_Range(mu.get.monitors.physicalTime(),
                StaticDeclarations.Operator.GREATER_THAN_OR_EQUALS, 0.1);
        ud.updEvent = mu.add.tools.updateEvent_Logic(new UpdateEvent[]{ud.updEvent1, ud.updEvent2},
                StaticDeclarations.Logic.AND);
        ud.namedObjects.clear();
        ud.namedObjects.add(ud.region);
        ud.fieldFunctions.add(ud.ff.getMagnitudeFunction());
        ud.solHist = mu.add.solver.solutionHistory(ud.namedObjects, ud.fieldFunctions);
        //--
        //-- Set Update Events
        mu.set.object.updateEvent(mu.get.monitors.fromReport(ud.rep1, true), ud.updEvent, true);
        mu.set.object.updateEvent(mu.get.monitors.fromReport(ud.rep2, true), ud.updEvent, true);
        mu.set.object.updateEvent(ud.solHist, ud.updEvent, true);
        //--
        mu.run();
        mu.saveSim(ud.simTitle + "_TRN");
    }

    void postSS() {
        updateVariables();
        ud.namedObjects.add(ud.region);
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_mps, true);
        //ud.scene.open(true);
        mu.saveSim(ud.simTitle + "_SS");
    }

    void postTRN() {
        updateVariables();
        ScalarDisplayer sd = (ScalarDisplayer) mu.get.scenes.displayerByREGEX(ud.scene, ".*", true);
        sd.getLegend().setLabelFormat("%.2f");
        sd.getScalarDisplayQuantity().setClip(false);
        sd.getScalarDisplayQuantity().setRange(new double[]{0., 0.4});
        //--
        ud.solHist = mu.get.solver.solutionHistory(".*", true);
        ud.recSolView = (RecordedSolutionView) mu.add.solver.solutionView(ud.solHist);
        sd.setRepresentation(ud.recSolView.getRepresentation());
        mu.io.say.action("Saving Pictures in several camera views", true);
        TemplatePost tp = mu.templates.post;
        tp.flyOver(ud.scene, ud.vv1, null, 5 * fps, ud.recSolView);
        tp.flyOver(ud.scene, ud.vv1, ud.vv2, 4 * fps, ud.recSolView);
        tp.flyOver(ud.scene, null, null, 4 * fps, ud.recSolView);
        sd.setDisplayMeshBoolean(true);
        tp.flyOver(ud.scene, null, null, 4 * fps, ud.recSolView);
        tp.flyOver(ud.scene, ud.vv2, ud.vv3, 6 * fps, ud.recSolView);
        tp.flyOver(ud.scene, null, null, 4 * fps, ud.recSolView);
        tp.flyOver(ud.scene, ud.vv3, ud.vv1, 5 * fps, ud.recSolView);
        tp.flyOver(ud.scene, null, null, 5 * fps, ud.recSolView);
        sd.setDisplayMeshBoolean(false);
        tp.flyOver(ud.scene, null, null, ud.recSolView.getMaxStateIndex() - tp.getCurrentFrame() + 1, ud.recSolView);
    }

    private void updateVariables() {
        ud.scene = mu.get.scenes.byREGEX("Scalar", true);
        ud.region = mu.get.regions.byREGEX(".*", true);
        ud.physCont = ud.region.getPhysicsContinuum();
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), true);
        ud.namedObjects.clear();
        ud.fieldFunctions.clear();
    }

    private MacroUtils mu;
    private Simulation sim;
    private UserDeclarations ud;

    int curFrame;
    int fps = 24;
    double Re = 500;

}
