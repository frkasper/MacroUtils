package macroutils.templates.demos;

import macroutils.*;
import star.common.*;

/**
 * Low-level class for Demo 16.
 *
 * @since October of 2016
 * @author Fabio Kasper
 */
public class Demo16 {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public Demo16(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Executes the preprocessing.
     */
    public void executePre() {
        updateCaseParameters();
        _setupPart();
        _setupPhysics();
        _setupRegion();
        _setupMesh();
        _mu.update.solverSettings();
    }

    /**
     * Executes the postprocessing.
     */
    public void executePost() {
        _setupPlot();
        _mu.templates.prettify.all();
    }

    /**
     * Prints a simple case overview.
     */
    public void printOverview() {
        _mu.io.say.loud("SIMULATION OVERVIEW");
        _mu.io.say.value("Speed of Sound", _C, _ud.unit_mps, true);
        _mu.io.say.value("Frequency", _F, _ud.unit_Hz, true);
        _mu.io.say.value("Wavelength", _W, _ud.unit_m, true);
        _mu.io.say.value("Domain Length", _L, _ud.unit_m, true);
        _mu.io.say.value("Maximum Time", _ud.trnMaxTime, _ud.unit_s, true);
    }

    /**
     * Sets the speed of sound for this case in [m/s].
     *
     * @param c given value.
     */
    public void setSpeedOfSound(double c) {
        _C = c;
    }

    /**
     * Sets the sound signal frequency in [Hz].
     *
     * @param f given value.
     */
    public void setSoundFrequency(double f) {
        _F = f;
    }

    /**
     * Updates the Simulation Parameters.
     */
    public void updateCaseParameters() {
        _io.say.action("Updating Case Parameters.", true);
        _ud.param = _mu.get.objects.parameter("F", false);
        if (_ud.param == null) {
            _ud.scalParam = _mu.add.tools.parameter_Scalar("F", _F, _ud.unit_Hz);
        } else {
            _ud.scalParam = (ScalarGlobalParameter) _ud.param;
        }
        _F = _ud.scalParam.getQuantity().getRawValue();
        _W = _C / _F;
        _L = 50.0 * _W;
        _ud.trnMaxTime = 2.0 * _L / _C;
        _io.say.ok(true);
        _mu.update.solverSettings();
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _templ = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    private void _setupMesh() {
        _ud.partSrf1 = _mu.get.partSurfaces.byREGEX(_ud.bcInlet, true);
        _ud.partSrf2 = _mu.get.partSurfaces.byREGEX(_ud.bcOutlet, true);
        int nx = (int) (_L / (_W / 100.0));
        _mu.add.meshOperation.directedMeshing_Channel(_ud.partSrf1, _ud.partSrf2, nx, 1, 1).execute();
    }

    private void _setupPart() {
        _ud.geometryParts.clear();
        _ud.cadPrt = _mu.add.geometry.block3DCAD(StaticDeclarations.COORD0,
                new double[]{_L, 0.1, 0.1}, _ud.unit_m);
        _ud.cadPrt.setPresentationName("Domain");
        _ud.geometryParts.add(_ud.cadPrt);
        _mu.get.partSurfaces.byREGEX("x0", true).setPresentationName(_ud.bcInlet);
        _mu.get.partSurfaces.byREGEX("x1", true).setPresentationName(_ud.bcOutlet);
        _mu.set.geometry.combinePartSurfaces(_mu.get.partSurfaces.allByREGEX("..", true),
                true).setPresentationName(_ud.bcSym);
    }

    private void _setupPlot() {
        _ud.namedObjects.clear();
        _ud.ff1 = _mu.get.objects.fieldFunction(StaticDeclarations.Vars.POS);
        _ud.ff2 = _mu.get.objects.fieldFunction(StaticDeclarations.Vars.P);
        _ud.namedObjects.add(_mu.get.boundaries.byREGEX(_ud.bcSym, true));
        _ud.starPlot = _mu.add.plot.xy(_ud.namedObjects, _ud.ff1.getComponentFunction(0),
                _ud.unit_m, _ud.ff2, _ud.unit_Pa);
        _ud.starPlot.setPresentationName("Pressure vs Length");
        _ud.updEvent = _mu.add.tools.updateEvent_Iteration(10, 0);
        _mu.set.object.updateEvent(_ud.starPlot, _ud.updEvent, true);
        InternalDataSet ids = (InternalDataSet) _mu.get.plots.datasets(_ud.starPlot, true).get(0);
        ids.getSymbolStyle().getSymbolShapeOption().setSelected(SymbolShapeOption.Type.FILLED_CIRCLE);
        ids.getSymbolStyle().setColor(StaticDeclarations.Colors.BLACK.getColor());
        ids.getSymbolStyle().setSize(4);
        _ud.starPlot.open();
    }

    private void _setupPhysics() {
        if (!_sim.getContinuumManager().isEmpty()) {
            _ud.physCont = _mu.get.objects.physicsContinua(".*Explicit.*", true);
            return;
        }
        _ud.v0 = new double[]{0.05 * _C, 0, 0};
        _ud.physCont = _mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.EXPLICIT_UNSTEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.INVISCID);
    }

    private void _setupRegion() {
        _ud.defUnitTemp = _ud.unit_K;
        _ud.ff = _mu.add.tools.fieldFunction("Pout", String.format("sin(2*$PI*%g*$Time)", _F),
                _ud.dimPress, FieldFunctionTypeOption.Type.SCALAR);
        //--
        _ud.region = _mu.add.region.fromAll(true);
        _ud.bdry = _mu.get.boundaries.byREGEX(_ud.bcInlet, true);
        _mu.set.boundary.asFreeStream(_ud.bdry, new double[]{1, 0, 0}, 0.05, 0, 300, 0, 0);
        _ud.bdry = _mu.get.boundaries.byREGEX(_ud.bcOutlet, true);
        _mu.set.boundary.asPressureOutlet(_ud.bdry, 0, 300, 0, 0);
        _mu.set.boundary.definition(_ud.bdry, StaticDeclarations.Vars.P, "$Pout");
        _mu.set.boundary.asSymmetry(_mu.get.boundaries.byREGEX(_ud.bcSym, true));
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.templates.MainTemplates _templ = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

    private double _C = 347.28;
    private double _F = 1000.0;
    private double _L, _W;

}
