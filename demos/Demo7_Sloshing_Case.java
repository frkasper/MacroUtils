import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.FieldFunctionTypeOption;
import star.common.Region;
import star.common.Simulation;
import star.common.StarMacro;
import star.vis.ReportAnnotation;
import star.vis.ScalarDisplayer;
import star.vis.ScalarFillMode;
import star.vof.SegregatedVofModel;

/**
 * Simple pseudo 2D Demo of a sloshing case in a tank.
 *
 * Geometry in XY plane:
 *                  L
 *      +-----------------------+
 *      |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *    H |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *      |                       |
 *      *-----------------------+
 *      O(0,0,0)
 *
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo7_Sloshing_Case extends StarMacro {

    private final double L = 100;
    private final double H = L;
    private double W;
    private MacroUtils mu;
    private Simulation sim;
    private UserDeclarations ud;

    @Override
    public void execute() {

        initMacro();

        prep1_createRegion();

        prep2_PhysicsAndMesh();

        prep3_MotionAndPost();

        mu.saveSim(ud.simTitle + "_preRun");

        mu.run();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private Region getRegion() {
        return mu.get.regions.byREGEX(".*", true);
    }

    private void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo7_Sloshing_Case";
        //-- Physics/Solver/Mesh settings
        ud.mshBaseSize = 2.;
        W = ud.mshBaseSize;
        ud.mshTrimmerMaxCellSize = 100;
        ud.trnInnerIter = 6;
        ud.trnTimestep = 0.01;
        ud.trnMaxTime = 8.;
    }

    private void prep1_createRegion() {
        ud.defCamView = mu.io.read.cameraView("myView|4.478788e-02,4.233814e-02,1.889347e-03"
                + "|4.478788e-02,4.233814e-02,2.732051e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|5.817755e-02|1", true);
        ud.cadPrt = mu.add.geometry.block3DCAD(StaticDeclarations.COORD0,
                new double[]{ L, H, W }, ud.unit_mm);
        ud.partSrf = mu.set.geometry.combinePartSurfaces(
                mu.get.partSurfaces.allByREGEX("z.*", true), true);
        ud.partSrf.setPresentationName(ud.bcSym);
        ud.partSurfaces.addAll(mu.get.partSurfaces.allByREGEX("x.*", true));
        ud.partSurfaces.addAll(mu.get.partSurfaces.allByREGEX("y.*", true));
        mu.set.geometry.combinePartSurfaces(ud.partSurfaces, true).setPresentationName(ud.bcWalls);
        ud.region = mu.add.region.fromAll(true);
        mu.set.boundary.asSymmetry(mu.get.boundaries.byREGEX(ud.bcSym, true));
    }

    private void prep2_PhysicsAndMesh() {
        ud.geometryParts.addAll(getRegion().getPartGroup().getObjects());
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts,
                StaticDeclarations.Meshers.TRIMMER_MESHER);
        ud.mshOp.setPresentationName("Mesh");
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.IMPLICIT_UNSTEADY, StaticDeclarations.Material.VOF_AIR_WATER,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.RKE_HIGH_YPLUS);
        ud.physCont.getModelManager().getModel(SegregatedVofModel.class).setSharpeningFactor(0.1);
        mu.set.solver.aggressiveSettings();
        //--
        mu.add.physicsContinua.createWave(ud.physCont, new double[]{ 0, 0.5 * L, 0 },
                new double[]{ 0, 1, 0 }, StaticDeclarations.COORD0, StaticDeclarations.COORD0);
        //--
        mu.update.volumeMesh();
        mu.add.scene.mesh();
    }

    private void prep3_MotionAndPost() {
        mu.add.tools.parameter_Scalar("Period", 0.5, ud.unit_s);
        mu.add.tools.parameter_Scalar("Omega", "2 * $PI / $Period");
        mu.add.tools.fieldFunction("Amplitude", "($Time <= 4) ? 0.01 : 0",
                ud.dimDimensionless, FieldFunctionTypeOption.Type.SCALAR);
        ud.ff1 = mu.add.tools.fieldFunction("MotionVel",
                "-$Amplitude * $Omega * sin($Omega * ($Time - 0.25 * $Period))",
                ud.dimDimensionless, FieldFunctionTypeOption.Type.SCALAR);
        ud.ff2 = mu.add.tools.fieldFunction("MotionDispl",
                "$Amplitude * cos($Omega * ($Time - 0.25 * $Period))",
                ud.dimDimensionless, FieldFunctionTypeOption.Type.SCALAR);
        mu.set.solver.timestep("($Time <= 5) ? 0.00125 : 0.0025");
        mu.set.region.motion(
                mu.add.tools.motion_Translation("[$MotionVel, 0, 0]", "Periodic Motion"),
                getRegion());
        //-- Some cool Reports/Plots
        mu.templates.post.unsteadyReports();
        mu.add.report.maximum(ud.region, "MotionVel", ud.ff1, ud.defUnitVel, true);
        mu.add.report.maximum(ud.region, "MotionDispl", ud.ff2, ud.defUnitLength, true);
        //-- Scene setup
        ud.namedObjects.add(mu.get.boundaries.byREGEX(ud.bcSym, true));
        ud.ff = mu.get.objects.fieldFunction("Volume Frac.*Air", true);
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_Dimensionless, true);
        ud.scene.setPresentationName("Demo7_VOF");
        mu.add.scene.annotation(ud.scene,
                (ReportAnnotation) mu.get.objects.annotation("Time", true), 0,
                "%3.1f", new double[]{ 0.01, 0.5 });
        ScalarDisplayer sd = (ScalarDisplayer) mu.get.scenes.displayerByREGEX(ud.scene, ".*", true);
        sd.setDisplayMeshBoolean(true);
        sd.setFillMode(ScalarFillMode.NATIVE);
        //-- Change Update Frequency / Save Pictures
        ud.updEvent = mu.add.tools.updateEvent_DeltaTime(0.005, ud.unit_s, false);
        mu.set.object.updateEvent(ud.scene, ud.updEvent, true);
        mu.set.scene.saveToFile(ud.scene, 1280, 720);
    }

}
