import macroutils.*;
import star.common.*;

/**
 * Runs a simplified sound propagation problem inside a constant cross section pipe. Boundary condition is a sinusoid
 * unity Pressure (1Pa) and the domain length is 50 wavelengths. Total time is 2 flows through. Flow is inviscid ideal
 * gas run with the coupled explicit scheme of STAR-CCM+.
 *
 * @since MacroUtils v11.04.
 * @author Fabio Kasper
 */
public class Demo16_1D_Sound_Propagation extends StarMacro {

    /**
     * Frequency in Hertz.
     */
    public static final double F = 1000.0;

    /**
     * Speed of Sound at 300K.
     */
    public static final double C = 347.28;

    /**
     * Wavelength.
     */
    public static final double W = C / F;

    /**
     * Domain length.
     */
    public static final double L = 50 * W;

    public void execute() {

        initializeMacro();

        pre();

        post();

        mu.run();

        mu.saveSim();
    }

    public void initializeMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo16_1D_Sound_Propagation";
        ud.defUnitTemp = ud.unit_K;
        ud.v0 = new double[]{0.05 * C, 0, 0};
        ud.trnMaxTime = 2.0 * L / C;
    }

    public void pre() {
        setupPart();
        setupPhysics();
        setupRegion();
        setupMesh();
        mu.update.solverSettings();
    }

    public void post() {
        setupPlot();
    }

    private void setupMesh() {
        ud.partSrf1 = mu.get.partSurfaces.byREGEX(ud.bcInlet, true);
        ud.partSrf2 = mu.get.partSurfaces.byREGEX(ud.bcOutlet, true);
        mu.add.meshOperation.directedMeshing_Channel(ud.partSrf1, ud.partSrf2, (int) (L / (W / 100)), 1, 1).execute();
    }

    private void setupPart() {
        ud.cadPrt = mu.add.geometry.block3DCAD(StaticDeclarations.COORD0, new double[]{L, 0.1, 0.1}, ud.unit_m);
        ud.cadPrt.setPresentationName("Domain");
        ud.geometryParts.add(ud.cadPrt);
        mu.get.partSurfaces.byREGEX("x0", true).setPresentationName(ud.bcInlet);
        mu.get.partSurfaces.byREGEX("x1", true).setPresentationName(ud.bcOutlet);
        mu.set.geometry.combinePartSurfaces(mu.get.partSurfaces.allByREGEX("..", true),
                true).setPresentationName(ud.bcSym);
    }

    private void setupPlot() {
        ud.ff1 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.POS);
        ud.ff2 = mu.get.objects.fieldFunction(StaticDeclarations.Vars.P);
        ud.namedObjects.add(mu.get.boundaries.byREGEX(ud.bcSym, true));
        ud.starPlot = mu.add.plot.xy(ud.namedObjects, ud.ff1.getComponentFunction(0), ud.unit_m, ud.ff2, ud.unit_Pa);
        ud.starPlot.setPresentationName("Pressure vs Length");
        ud.updEvent = mu.add.tools.updateEvent_Iteration(10, 0);
        mu.set.object.updateEvent(ud.starPlot, ud.updEvent, true);
        ud.starPlot.open();
    }

    private void setupPhysics() {
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.EXPLICIT_UNSTEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.INVISCID);
    }

    private void setupRegion() {
        ud.ff = mu.add.tools.fieldFunction("Pout", String.format("sin(2*%.8f*%g*$Time)", Math.PI, F),
                ud.dimPress, FieldFunctionTypeOption.Type.SCALAR);
        //--
        ud.region = mu.add.region.fromAll(true);
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcInlet, true);
        mu.set.boundary.asFreeStream(ud.bdry, new double[]{1, 0, 0}, 0.05, 0, 300, 0, 0);
        ud.bdry = mu.get.boundaries.byREGEX(ud.bcOutlet, true);
        mu.set.boundary.asPressureOutlet(ud.bdry, 0, 300, 0, 0);
        mu.set.boundary.definition(ud.bdry, StaticDeclarations.Vars.P, "$Pout");
        mu.set.boundary.asSymmetry(mu.get.boundaries.byREGEX(ud.bcSym, true));
    }

    public MacroUtils mu;
    public Simulation sim;
    public UserDeclarations ud;

}
