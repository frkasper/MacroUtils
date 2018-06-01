import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * Simple pseudo 2D Demo of the classic Backward Facing Step test case.
 *
 * Geometry in XY plane:
 *                                         L_t
 *      +---------------------------------------------------------------+
 *    h |                                                               |
 *      +-----------+                                                   | H
 *                  |                                                   |
 *      * O(0,0,0)  +---------------------------------------------------+
 *                                         L_c
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo3_Backward_Facing_Step extends StarMacro {

    private final double H = 100;
    private final double L_c = 500;
    private final double L_t = 700;
    private final double Re_h = 189;  //-- Reynolds Number f(h)

    private final String bcStep = "step";
    private final double depth = 0.25;
    private final double h = 50;
    private MacroUtils mu;
    private final String regionName = "Channel";
    private UserDeclarations ud;

    @Override
    public void execute() {

        initMacro();

        prep1_createParts();

        prep2_BCsAndMesh();

        prep3_setPost();

        mu.run();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void initMacro() {
        mu = new MacroUtils(getActiveSimulation());
        ud = mu.userDeclarations;
        ud.simTitle = "Demo3_Backward_Facing_Step";
    }

    private void prep1_createParts() {
        ud.coord1 = new double[]{ L_t, H, depth };
        ud.cadPrt1 = mu.add.geometry.block3DCAD(StaticDeclarations.COORD0, ud.coord1, ud.unit_mm);
        ud.cadPrt1.setPresentationName("Block1");
        mu.get.partSurfaces.byREGEX(ud.cadPrt1, "x0", true).setPresentationName(ud.bcInlet);
        mu.get.partSurfaces.byREGEX(ud.cadPrt1, "x1", true).setPresentationName(ud.bcOutlet);
        mu.get.partSurfaces.byREGEX(ud.cadPrt1, "y1", true).setPresentationName(ud.bcTop);
        mu.get.partSurfaces.byREGEX(ud.cadPrt1, "y0", true).setPresentationName(ud.bcBottom);
        mu.set.geometry.combinePartSurfaces(
                mu.get.partSurfaces.allByREGEX(ud.cadPrt1, "z.*", true), true)
                .setPresentationName(ud.bcSym);
        //--
        ud.coord2 = new double[]{ L_t - L_c, H - h, depth };
        ud.cadPrt2 = mu.add.geometry.block3DCAD(StaticDeclarations.COORD0, ud.coord2, ud.unit_mm);
        ud.cadPrt2.setPresentationName("Block2");
        mu.set.geometry.combinePartSurfaces(
                mu.get.partSurfaces.allByREGEX(ud.cadPrt2, "x1|y1", true), true)
                .setPresentationName(bcStep);
        //--
        ud.geometryParts.add(ud.cadPrt1);
        ud.geometryParts.add(ud.cadPrt2);
        //--
        //-- Mesh Operations
        ud.mshOpPrt = mu.add.meshOperation.subtract(ud.geometryParts, ud.cadPrt1);
        ud.mshOpPrt.setPresentationName(regionName);
        //--
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
    }

    private void prep2_BCsAndMesh() {
        //-- Mesh settings
        ud.mshBaseSize = depth * 10.;
        ud.mshSrfSizeMin = 100;
        ud.mshSrfSizeTgt = 100;
        ud.mshTrimmerMaxCellSize = 100;
        ud.prismsLayers = 3;
        ud.prismsRelSizeHeight = 50;
        //--
        ud.geometryParts2.addAll(ud.region.getPartGroup().getObjects());
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.TRIMMER_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("My Mesh");
        mu.disable.surfaceProximityRefinement(ud.mshOp);
        mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.RKE_2LAYER);
        mu.set.solver.aggressiveSettings();
        //--
        ud.bdry1 = mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true);
        double vel = Re_h * 1.855e-5 / (1.184 * h * ud.defUnitLength.getConversion());
        mu.set.boundary.asVelocityInlet(ud.bdry1, vel, 0, 0.01, 1);
        //--
        ud.bdry = mu.get.boundaries.byREGEX(".*" + ud.bcOutlet, true);
        mu.set.boundary.asPressureOutlet(ud.bdry, 0, 0, 0.01, 1);
        //--
        ud.bdry = mu.get.boundaries.byREGEX(".*" + ud.bcSym, true);
        mu.set.boundary.asSymmetry(ud.bdry);
        //--
        mu.update.volumeMesh();
    }

    private void prep3_setPost() {
        ud.defCamView = mu.io.read.cameraView("myCam|3.502958e-01,3.866198e-02,-8.724052e-04"
                + "|3.502958e-01,3.866198e-02,1.366150e+00|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|1.986988e-01|1", true);
        //-- Stopping Criteria
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.P.getVar(), true);
        ud.rep = mu.add.report.massFlowAverage(ud.bdry1, "P_in", ud.ff, ud.defUnitPress, true);
        ud.mon = mu.get.monitors.byREGEX(ud.rep.getPresentationName(), true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.ASYMPTOTIC, 1e-5, 50);
        ud.updEvent1 = mu.add.tools.updateEvent_Iteration(1, 10);
        mu.set.object.updateEvent(ud.mon, ud.updEvent1, true);
        //-- Contour Plot
        ud.namedObjects.add(mu.get.boundaries.byREGEX(".*" + ud.bcSym, true));
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.TVR.getVar(), true);
        ud.scene1 = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_Dimensionless, true);
        ud.scene2 = mu.add.scene.vector(ud.namedObjects, true);
        ud.updEvent2 = mu.add.tools.updateEvent_Iteration(10, 0);
        mu.set.object.updateEvent(ud.scene1, ud.updEvent2, true);
        mu.set.object.updateEvent(ud.scene2, ud.updEvent2, true);
        mu.open.all();
    }

}
