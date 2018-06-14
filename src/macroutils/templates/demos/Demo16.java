package macroutils.templates.demos;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.common.Cartesian2DAxis;
import star.common.Cartesian2DAxisManager;
import star.common.FieldFunctionTypeOption;
import star.common.InternalDataSet;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.SymbolShapeOption;

/**
 * Low-level class for Demo 16.
 *
 * @since October of 2016
 * @author Fabio Kasper
 */
public class Demo16 {

    private double _C = 347.28;
    private double _F = 1000.0;
    private double _L;
    private double _W;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private macroutils.setter.MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.templates.MainTemplates _templ = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.misc.MainUpdater _upd = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public Demo16(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _templ = _mu.templates;
        _ud = _mu.userDeclarations;
        _upd = _mu.update;
    }

    /**
     * Executes the postprocessing.
     */
    public void executePost() {
        _setupPlot();
        _templ.prettify.all();
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
        _upd.solverSettings();
    }

    /**
     * Prints a simple case overview.
     */
    public void printOverview() {
        _io.say.loud("SIMULATION OVERVIEW");
        _io.say.value("Speed of Sound", _C, _ud.unit_mps, true);
        _io.say.value("Frequency", _F, _ud.unit_Hz, true);
        _io.say.value("Wavelength", _W, _ud.unit_m, true);
        _io.say.value("Domain Length", _L, _ud.unit_m, true);
        _io.say.value("Maximum Time", _ud.trnMaxTime, _ud.unit_s, true);
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
     * Sets the speed of sound for this case in [m/s].
     *
     * @param c given value.
     */
    public void setSpeedOfSound(double c) {
        _C = c;
    }

    /**
     * Updates the Simulation Parameters.
     */
    public void updateCaseParameters() {
        _io.say.action("Updating Case Parameters.", true);
        _ud.param = _get.objects.parameter("F", false);
        if (_ud.param == null) {
            _ud.scalParam = _add.tools.parameter_Scalar("F", _F, _ud.unit_Hz);
        } else {
            _ud.scalParam = (ScalarGlobalParameter) _ud.param;
        }
        _F = _ud.scalParam.getQuantity().getRawValue();
        _W = _C / _F;
        _L = 10.0 * _W;
        _ud.defUnitTemp = _ud.unit_K;
        _ud.trnMaxTime = 2.0 * _L / _C;
        _ud.v0 = new double[]{ 1E-4 * _C, 0, 0 };
        _io.say.ok(true);
        _upd.solverSettings();
    }

    private void _setupMesh() {
        _ud.partSrf1 = _get.partSurfaces.byREGEX(_ud.bcInlet, true);
        _ud.partSrf2 = _get.partSurfaces.byREGEX(_ud.bcOutlet, true);
        int nPointsPerWavelength = 100;
        int nx = (int) Math.ceil(_L / _W * nPointsPerWavelength);
        _add.meshOperation.directedMeshing_Channel(_ud.partSrf1, _ud.partSrf2, nx, 1, 1).execute();
    }

    private void _setupPart() {
        _ud.geometryParts.clear();
        _ud.cadPrt = _add.geometry.block3DCAD(StaticDeclarations.COORD0,
                new double[]{ _L, 0.1, 0.1 }, _ud.unit_m);
        _ud.cadPrt.setPresentationName("Domain");
        _ud.geometryParts.add(_ud.cadPrt);
        _get.partSurfaces.byREGEX("x0", true).setPresentationName(_ud.bcInlet);
        _get.partSurfaces.byREGEX("x1", true).setPresentationName(_ud.bcOutlet);
        _set.geometry.combinePartSurfaces(_get.partSurfaces.allByREGEX("..", true),
                true).setPresentationName(_ud.bcSym);
    }

    private void _setupPhysics() {
        if (!_sim.getContinuumManager().isEmpty()) {
            _ud.physCont = _get.objects.physicsContinua(".*Explicit.*", true);
            return;
        }
        _ud.physCont = _add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.EXPLICIT_UNSTEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.INVISCID);
    }

    private void _setupPlot() {
        _ud.namedObjects.clear();
        _ud.ff1 = _get.objects.fieldFunction(StaticDeclarations.Vars.POS);
        _ud.ff2 = _get.objects.fieldFunction(StaticDeclarations.Vars.P);
        _ud.namedObjects.add(_get.boundaries.byREGEX(_ud.bcSym, true));
        _ud.starPlot = _add.plot.xy(_ud.namedObjects, _ud.ff1.getComponentFunction(0),
                _ud.unit_m, _ud.ff2, _ud.unit_Pa);
        _ud.starPlot.setPresentationName("Pressure vs Length");
        _ud.updEvent = _add.tools.updateEvent_Iteration(10, 0);
        _set.object.updateEvent(_ud.starPlot, _ud.updEvent, true);
        InternalDataSet ids = (InternalDataSet) _get.plots.datasets(_ud.starPlot, true).get(0);
        ids.getSymbolStyle().getSymbolShapeOption()
                .setSelected(SymbolShapeOption.Type.FILLED_CIRCLE);
        ids.getSymbolStyle().setColor(StaticDeclarations.Colors.BLACK.getColor());
        ids.getSymbolStyle().setSize(4);
        Cartesian2DAxisManager cam = (Cartesian2DAxisManager) _ud.starPlot.getAxisManager();
        Cartesian2DAxis cla = (Cartesian2DAxis) cam.getAxis("Left Axis");
        cla.setMinimum(-1.2);
        cla.setMaximum(1.2);
        _ud.starPlot.open();
    }

    private void _setupRegion() {
        _ud.ff = _add.tools.fieldFunction("Pout", String.format("sin(2*$PI*%g*$Time)", _F),
                _ud.dimPress, FieldFunctionTypeOption.Type.SCALAR);
        _ud.region = _add.region.fromAll(true);
        _ud.bdry = _get.boundaries.byREGEX(_ud.bcInlet, true);
        _set.boundary.asFreeStream(_ud.bdry, new double[]{ 1, 0, 0 }, _ud.v0[0] / _C,
                0, _ud.t0, 0, 0);
        _ud.bdry = _get.boundaries.byREGEX(_ud.bcOutlet, true);
        _set.boundary.asPressureOutlet(_ud.bdry, 0, _ud.t0, 0, 0);
        _set.boundary.definition(_ud.bdry, StaticDeclarations.Vars.P, "$Pout");
        _set.boundary.asSymmetry(_get.boundaries.byREGEX(_ud.bcSym, true));
    }

}
