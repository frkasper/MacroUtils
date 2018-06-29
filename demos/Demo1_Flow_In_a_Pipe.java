import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * Simple Demo of a 3D laminar isothermal flow in a pipe.
 *
 * Geometry:
 *                                       L
 *      +---------------------------------------------------------------+
 *      |                                                               |
 *    r * O(0,0,0)                                                      |
 *      |                                                               |
 *      +---------------------------------------------------------------+
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo1_Flow_In_a_Pipe extends StarMacro {

    private final double length = 500;
    private MacroUtils mu;
    private final double radius = 20;
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
        ud.simTitle = "Demo1_Flow_In_a_Pipe";
    }

    private void prep1_createPart() {
        ud.defCamView = mu.io.read.cameraView("myCam|2.498938e-01,-1.450833e-02,-2.222717e-02"
                + "|2.498938e-01,-1.450833e-02,9.690091e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|1.463440e-01|1", true);
        ud.defTessOpt = StaticDeclarations.Tessellation.FINE;
        ud.cadPrt = mu.add.geometry.cylinder3DCAD(radius, length, StaticDeclarations.COORD0,
                ud.unit_mm, StaticDeclarations.Axis.X);
    }

    private void prep2_createRegion() {
        mu.get.partSurfaces.byREGEX("x0", true).setPresentationName(ud.bcInlet);
        mu.get.partSurfaces.byREGEX("x1", true).setPresentationName(ud.bcOutlet);
        mu.get.partSurfaces.byREGEX("Def.*", true).setPresentationName(ud.bcWall);
        ud.region = mu.add.region.fromAll(true);
    }

    private void prep3_BCsAndMesh() {
        //-- Mesh settings
        ud.mshBaseSize = radius / 8;
        ud.prismsLayers = 3;
        ud.mshSrfSizeMin = 75.;
        ud.prismsRelSizeHeight = 30;
        //--
        ud.geometryParts.add(mu.get.geometries.byREGEX(ud.cadPrt.getPresentationName(), true));
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.POLY_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("My Mesh");
        mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.LIQUID,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.LAMINAR);
        mu.set.solver.aggressiveSettings();
        //--
        mu.set.boundary.asVelocityInlet(mu.get.boundaries.byREGEX(ud.bcInlet, true),
                0.1, 0.0, 0.0, 0.0);
        mu.set.boundary.asPressureOutlet(mu.get.boundaries.byREGEX(ud.bcOutlet, true),
                0.0, 0.0, 0.0, 0.0);
        //--
        mu.update.volumeMesh();
    }

    private void prep4_setPost() {
        //-- Contour Plot
        ud.plane = mu.add.derivedPart.sectionPlaneZ(StaticDeclarations.COORD0);
        ud.namedObjects.add(ud.plane);
        ud.ff1 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), true);
        mu.add.scene.scalar(ud.namedObjects, ud.ff1, ud.defUnitVel, true);
        //-- Stopping Criteria
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcInlet, true);
        ud.ff2 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.P.getVar(), true);
        ud.rep = mu.add.report.massFlowAverage(ud.bdry, "Pressure Inlet", ud.ff2, ud.unit_Pa, true);
        ud.mon = mu.get.monitors.byREGEX(ud.rep.getPresentationName(), true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.ASYMPTOTIC,
                0.001, 50);
        mu.open.all();
    }

}
