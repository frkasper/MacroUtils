import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;
import star.vis.ScalarDisplayer;
import star.vis.Scene;

/**
 * Simple Demo of a 3D conduction in a channel.
 *
 * Geometry:
 *
 *         +---------------------------------------------------------------+
 *      D /                                                               /|
 *       /                               L                               / |
 *      +---------------------------------------------------------------+  |
 *      |                                                               |  +
 *    H |                                                               | /
 *      |                                                               |/
 *      *---------------------------------------------------------------+
 *      O(0,0,0)
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo2_Conduction_In_a_Channel extends StarMacro {

    private final double depth = 50;
    private final double height = 100;
    private final double length = 1000;
    private MacroUtils mu;
    private UserDeclarations ud;

    @Override
    public void execute() {

        initMacro();

        prep1_createPart();

        prep2_createRegion();

        prep3_BCsAndMesh();

        prep4_setPost();

        mu.run();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void initMacro() {
        mu = new MacroUtils(getActiveSimulation());
        ud = mu.userDeclarations;
        ud.simTitle = "Demo2_Conduction_In_a_Channel";
    }

    private void prep1_createPart() {
        ud.defCamView = mu.io.read.cameraView("myCam|5.003809e-01,1.413476e-02,4.204865e-03"
                + "|-1.256561e+00,7.844162e-01,1.422375e+00|2.630476e-01,9.462717e-01,-1.880847e-01"
                + "|2.280761e-01|1", true);
        ud.cadPrt = mu.add.geometry.block3DCAD(StaticDeclarations.COORD0,
                new double[]{ length, height, depth }, ud.unit_mm);
        ud.cadPrt.setPresentationName("Channel");
        ud.geometryParts.add(mu.get.geometries.byREGEX(".*", true));
    }

    private void prep2_createRegion() {
        mu.get.partSurfaces.byREGEX("x0", true).setPresentationName(ud.bcHot);
        mu.get.partSurfaces.byREGEX("x1", true).setPresentationName(ud.bcCold);
        mu.set.geometry.combinePartSurfaces(mu.get.partSurfaces.allByREGEX("y.*", true), true)
                .setPresentationName(ud.bcWall);
        mu.set.geometry.combinePartSurfaces(mu.get.partSurfaces.allByREGEX("z.*", true), true)
                .setPresentationName(ud.bcSym);
        ud.region = mu.add.region.fromAll(true);
    }

    private void prep3_BCsAndMesh() {
        //-- Physics/Mesh settings
        ud.mshBaseSize = depth / 5;
        ud.mshTrimmerMaxCellSize = 100;
        ud.urfSolidEnrgy = 1.0;
        //--
        ud.autoMshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts,
                StaticDeclarations.Meshers.TRIMMER_MESHER);
        ud.autoMshOp.setPresentationName("My Mesh");
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.SOLID,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.NOT_APPLICABLE);
        mu.update.solverSettings();
        //--
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcHot, true);
        mu.set.boundary.asConvectionWall(ud.bdry, 50., 50.);
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcCold, true);
        mu.set.boundary.asConvectionWall(ud.bdry, 20., 15.);
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcSym, true);
        mu.set.boundary.asSymmetry(ud.bdry);
        //--
        mu.update.volumeMesh();
    }

    private void prep4_setPost() {
        //-- Stopping Criteria
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.T.getVar(), true);
        ud.rep = mu.add.report.volumeAverage(mu.get.regions.all(false),
                StaticDeclarations.Vars.T.getVar(), ud.ff, ud.defUnitTemp, true);
        ud.mon = mu.get.monitors.byREGEX(ud.rep.getPresentationName(), true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.ASYMPTOTIC,
                0.001, 50);
        //-- Contour Plot
        ud.namedObjects.addAll(mu.get.boundaries.all(true));
        Scene scn = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.defUnitTemp, true);
        ScalarDisplayer sd = (ScalarDisplayer) mu.get.scenes.displayerByREGEX(scn, ".*", true);
        sd.setDisplayMeshBoolean(true);
        ud.updEvent = mu.add.tools.updateEvent_Iteration(50, 0);
        mu.set.scene.updateEvent(scn, ud.updEvent);
        mu.open.all();
    }

}
