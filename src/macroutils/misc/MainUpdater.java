package macroutils.misc;

import macroutils.*;
import star.common.*;
import star.meshing.*;

/**
 * Main class for "updating" methods in MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class MainUpdater {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainUpdater(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Updates all units in memory. This method is called automatically by MacroUtils.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void allUnits(boolean vo) {
        defaultUnits(vo);
        customUnits(vo);
    }

    /**
     * Updates the custom units that are <b>not</b> shipped with STAR-CCM+. Those are created within MacroUtils.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void customUnits(boolean vo) {
        if (!_mu.getIntrusiveOption()) {
            _io.print.msgDebug("Intrusive Option is Disabled. Skipping customUnits()");
            return;
        }
        _io.print.action("Adding/Updating Custom Units", vo);
        _ud.dimDensity.setMass(1);
        _ud.dimDensity.setVolume(-1);
        _ud.dimForce.setForce(1);
        _ud.dimLength.setLength(1);
        _ud.dimMass.setMass(1);
        _ud.dimMassFlow.setMass(1);
        _ud.dimMassFlow.setTime(-1);
        _ud.dimMolFlow.setQuantity(1);
        _ud.dimMolFlow.setTime(-1);
        _ud.dimPress.setPressure(1);
        _ud.dimTime.setTime(1);
        _ud.dimVel.setVelocity(1);
        _ud.dimVisc.setPressure(1);
        _ud.dimVisc.setTime(1);
        _ud.dimVolFlow.setVolume(1);
        _ud.dimVolFlow.setTime(-1);
        /*    DENSITY UNITS [M/V]    */
        _ud.unit_gpcm3 = _add.units.custom("g/cm^3", "gram per cubic centimeter", 1000, _ud.dimDensity, vo);
        /*    FORCE UNITS [M*L/T^2]    */
        _ud.unit_kN = _add.units.custom("kN", "kilonewton", 1000, _ud.dimForce, vo);
        /*    MASS UNITS [M]    */
        _ud.unit_g = _add.units.custom("g", "gram", 0.001, _ud.dimMass, vo);
        /*    MASS FLOW UNITS [M/T]    */
        _ud.unit_kgph = _add.units.custom("kg/h", "kilogram per hour", 1. / 3600, _ud.dimMassFlow, vo);
        _ud.unit_kgpmin = _add.units.custom("kg/min", "kilogram per minute", 1. / 60, _ud.dimMassFlow, vo);
        _ud.unit_gpmin = _add.units.custom("g/min", "gram per minute", 0.001 / 60, _ud.dimMassFlow, vo);
        _ud.unit_gps = _add.units.custom("g/s", "gram per second", 0.001, _ud.dimMassFlow, vo);
        /*    MOLECULAR FLOW UNITS [Mol/T]    */
        _ud.unit_kmolps = _add.units.custom("kmol/s", "kilogram-mol per second", 1.0, _ud.dimMolFlow, vo);
        /*    PRESSURE UNITS [P]    */
        //--- http://www.sensorsone.co.uk/pressure-units-conversion.html
        //--- http://www.onlineconversion.com/pressure.htm
        _ud.unit_cmH2O = _add.units.custom("cmH2O", "centimeter of water", 98.0665, _ud.dimPress, vo);
        _ud.unit_cmHg = _add.units.custom("cmHg", "centimeter of mercury", 1333.2239, _ud.dimPress, vo);
        _ud.unit_dynepcm2 = _add.units.custom("dyne/cm^2", "dyne per square centimeter", 0.1, _ud.dimPress, vo);
        _ud.unit_kPa = _add.units.custom("kPa", "kilopascal", 1000, _ud.dimPress, vo);
        _ud.unit_mbar = _add.units.custom("mbar", "millibar", 100, _ud.dimPress, vo);
        _ud.unit_mmH2O = _add.units.custom("mmH2O", "millimeter of water", 9.80665, _ud.dimPress, vo);
        _ud.unit_mmHg = _add.units.custom("mmHg", "millimeter of mercury", 133.32239, _ud.dimPress, vo);
        _ud.unit_uPa = _add.units.custom("uPa", "micropascal", 1e-6, _ud.dimPress, vo);
        /*    TIME UNITS [T]    */
        _ud.unit_ms = _add.units.custom("ms", "milliseconds", 0.001, _ud.dimTime, vo);
        /*    VELOCITY UNITS [L/T]    */
        _ud.unit_kt = _add.units.custom("kt", "knot", 1852. / 3600., _ud.dimVel, vo);
        _ud.unit_mmps = _add.units.custom("mm/s", "millimeter per second", 0.001, _ud.dimVel, vo);
        _ud.unit_umps = _add.units.custom("um/s", "micrometer per second", 1e-6, _ud.dimVel, vo);
        /*    VISCOSITY UNITS [P*T]    */
        _ud.unit_P = _add.units.custom("P", "Poise", 0.1, _ud.dimVisc, vo);
        _ud.unit_cP = _add.units.custom("cP", "centipoise", 0.001, _ud.dimVisc, vo);
        /*    VOLUMETRIC FLOW RATE UNITS [V/T]    */
        _ud.unit_galps = _add.units.custom("gal/s", "gallons per second", 0.00378541, _ud.dimVolFlow, vo);
        _ud.unit_galpmin = _add.units.custom("gal/min", "gallons per minute", 0.00378541 / 60, _ud.dimVolFlow, vo);
        _ud.unit_lph = _add.units.custom("l/h", "liter per hour", 0.001 / 3600, _ud.dimVolFlow, vo);
        _ud.unit_lpmin = _add.units.custom("l/min", "liter per minute", 0.001 / 60, _ud.dimVolFlow, vo);
        _ud.unit_lps = _add.units.custom("l/s", "liter per second", 0.001, _ud.dimVolFlow, vo);
        _ud.unit_m3ph = _add.units.custom("m^3/h", "cubic meter per hour", 1. / 3600, _ud.dimVolFlow, vo);
        _io.say.ok(vo);
    }

    private Units defaultUnit(Units u1, Units u2, String descr, boolean vo) {
        String defDescr = _get.strings.fromUnit(u2);
        Units defUnit = u1;
        if (u1 instanceof Units) {
            defDescr = _get.strings.fromUnit(u1);
        } else {
            defUnit = u2;
        }
        _io.print.msg(vo, "Default Unit %s: %s", descr, defDescr);
        return defUnit;
    }

    /**
     * Updates default units that are shipped with STAR-CCM+.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void defaultUnits(boolean vo) {
        _io.print.action("Updating Default Units", vo);
        //--
        //-- Default units shipped with STAR-CCM+.
        //--
        _ud.unit_atm = queryUnit("atm", vo);
        _ud.unit_bar = queryUnit("bar", vo);
        _ud.unit_C = queryUnit("C", vo);
        _ud.unit_deg = queryUnit("deg", vo);
        _ud.unit_Dimensionless = queryUnit("", vo);
        _ud.unit_F = queryUnit("F", vo);
        _ud.unit_K = queryUnit("K", vo);
        _ud.unit_gal = queryUnit("gal", vo);
        _ud.unit_kg = queryUnit("kg", vo);
        _ud.unit_h = queryUnit("hr", vo);
        _ud.unit_m = queryUnit("m", vo);
        _ud.unit_m2 = queryUnit("m^2", vo);
        _ud.unit_m3 = queryUnit("m^3", vo);
        _ud.unit_min = queryUnit("min", vo);
        _ud.unit_mm = queryUnit("mm", vo);
        _ud.unit_mm2 = queryUnit("mm^2", vo);
        _ud.unit_mm3 = queryUnit("mm^3", vo);
        _ud.unit_N = queryUnit("N", vo);
        _ud.unit_kph = queryUnit("kph", vo);
        _ud.unit_kgpm3 = queryUnit("kg/m^3", vo);
        _ud.unit_kgps = queryUnit("kg/s", vo);
        _ud.unit_kmol = queryUnit("kmol", vo);
        _ud.unit_mps = queryUnit("m/s", vo);
        _ud.unit_mps2 = queryUnit("m/s^2", vo);
        _ud.unit_rpm = queryUnit("rpm", vo);
        _ud.unit_Pa = queryUnit("Pa", vo);
        _ud.unit_Pa_s = queryUnit("Pa-s", vo);
        _ud.unit_radps = queryUnit("radian/s", vo);
        _ud.unit_s = queryUnit("s", vo);
        _ud.unit_W = queryUnit("W", vo);
        _ud.unit_Wpm2K = queryUnit("W/m^2-K", vo);
        //--
        //-- Default units to be used with MacroUtils.
        //--
        _ud.defUnitAccel = defaultUnit(_ud.defUnitAccel, _ud.unit_mps2, "Acceleration", vo);
        _ud.defUnitAngle = defaultUnit(_ud.defUnitAngle, _ud.unit_deg, "Angle", vo);
        _ud.defUnitArea = defaultUnit(_ud.defUnitArea, _ud.unit_m2, "Area", vo);
        _ud.defUnitDen = defaultUnit(_ud.defUnitDen, _ud.unit_kgpm3, "Density", vo);
        _ud.defUnitForce = defaultUnit(_ud.defUnitForce, _ud.unit_N, "Force", vo);
        _ud.defUnitHTC = defaultUnit(_ud.defUnitHTC, _ud.unit_Wpm2K, "Heat Transfer Coefficient", vo);
        _ud.defUnitLength = defaultUnit(_ud.defUnitLength, _ud.unit_mm, "Length", vo);
        _ud.defUnitMFR = defaultUnit(_ud.defUnitMFR, _ud.unit_kgps, "Mass Flow Rate", vo);
        _ud.defUnitPress = defaultUnit(_ud.defUnitPress, _ud.unit_Pa, "Pressure", vo);
        _ud.defUnitTemp = defaultUnit(_ud.defUnitTemp, _ud.unit_C, "Temperature", vo);
        _ud.defUnitTime = defaultUnit(_ud.defUnitTime, _ud.unit_s, "Time", vo);
        _ud.defUnitVel = defaultUnit(_ud.defUnitVel, _ud.unit_mps, "Velocity", vo);
        _ud.defUnitVisc = defaultUnit(_ud.defUnitVisc, _ud.unit_Pa_s, "Viscosity", vo);
        _ud.defUnitVolume = defaultUnit(_ud.defUnitVolume, _ud.unit_m3, "Volume", vo);
        _io.say.ok(vo);
    }

    private Units queryUnit(String unitString, boolean vo) {
        Units u = _get.units.byName(unitString, false);
        if (u != null) {
            _io.print.msg(vo, _uf, "Unit read", unitString, u.getDescription());
            return u;
        }
        _io.print.msg(vo, _uf, "Unit not read", unitString, "");
        return null;
    }

    /**
     * Updates the {@link UserDeclarations#simTitle} global variable.
     */
    public void simTitle() {
        _ud.simTitle = _sim.getPresentationName();
    }

    /**
     * Updates the Solver settings. This is the same method as {@link macroutils.setter.SetSolver#settings}.
     */
    public void solverSettings() {
        _set.solver.settings();
    }

    /**
     * Updates the Surface Mesh.
     */
    public void surfaceMesh() {
        _io.say.action("Generating Surface Mesh", true);
        _sim.get(MeshPipelineController.class).generateSurfaceMesh();
    }

    /**
     * Updates the Volume Mesh.
     */
    public void volumeMesh() {
        _io.say.action("Generating Volume Mesh", true);
        _sim.get(MeshPipelineController.class).generateVolumeMesh();
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _add = _mu.add;
        _get = _mu.get;
        _set = _mu.set;
        _tmpl = _mu.templates;
        _ud = _mu.userDeclarations;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private final String _uf = StaticDeclarations.UNIT_FMT;

    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
