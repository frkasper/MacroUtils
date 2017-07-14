package macroutils.misc;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.Simulation;
import star.common.Units;
import star.meshing.MeshPipelineController;

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

    private void _updateCustomUnits(boolean vo) {
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

    private Units _updateDefaultUnit(Units u1, Units u2, String descr, boolean vo) {
        String defDescr = _get.strings.fromUnit(u2);
        Units defUnit = u1;
        if (u1 instanceof Units) {
            defDescr = _get.strings.fromUnit(u1);
        } else {
            defUnit = u2;
        }
        _io.print.value("Default Unit " + descr, "[" + defDescr + "]", false, vo);
        return defUnit;
    }

    private void _updateDefaultUnits(boolean vo) {
        _io.print.action("Updating Default Units", vo);
        //--
        //-- Default units shipped with STAR-CCM+.
        //--
        _ud.unit_atm = _updateUnit("atm", vo);
        _ud.unit_bar = _updateUnit("bar", vo);
        _ud.unit_C = _updateUnit("C", vo);
        _ud.unit_deg = _updateUnit("deg", vo);
        _ud.unit_Dimensionless = _updateUnit("", vo);
        _ud.unit_F = _updateUnit("F", vo);
        _ud.unit_K = _updateUnit("K", vo);
        _ud.unit_gal = _updateUnit("gal", vo);
        _ud.unit_kg = _updateUnit("kg", vo);
        _ud.unit_h = _updateUnit("hr", vo);
        _ud.unit_Hz = _updateUnit("Hz", vo);
        _ud.unit_m = _updateUnit("m", vo);
        _ud.unit_m2 = _updateUnit("m^2", vo);
        _ud.unit_m3 = _updateUnit("m^3", vo);
        _ud.unit_min = _updateUnit("min", vo);
        _ud.unit_mm = _updateUnit("mm", vo);
        _ud.unit_mm2 = _updateUnit("mm^2", vo);
        _ud.unit_mm3 = _updateUnit("mm^3", vo);
        _ud.unit_N = _updateUnit("N", vo);
        _ud.unit_kph = _updateUnit("kph", vo);
        _ud.unit_kgpm3 = _updateUnit("kg/m^3", vo);
        _ud.unit_kgps = _updateUnit("kg/s", vo);
        _ud.unit_kmol = _updateUnit("kmol", vo);
        _ud.unit_mps = _updateUnit("m/s", vo);
        _ud.unit_mps2 = _updateUnit("m/s^2", vo);
        _ud.unit_rpm = _updateUnit("rpm", vo);
        _ud.unit_Pa = _updateUnit("Pa", vo);
        _ud.unit_Pa_s = _updateUnit("Pa-s", vo);
        _ud.unit_radps = _updateUnit("radian/s", vo);
        _ud.unit_s = _updateUnit("s", vo);
        _ud.unit_W = _updateUnit("W", vo);
        _ud.unit_Wpm2K = _updateUnit("W/m^2-K", vo);
        //--
        //-- Default units to be used with MacroUtils.
        //--
        _ud.defUnitAccel = _updateDefaultUnit(_ud.defUnitAccel, _ud.unit_mps2, "Acceleration", vo);
        _ud.defUnitAngle = _updateDefaultUnit(_ud.defUnitAngle, _ud.unit_deg, "Angle", vo);
        _ud.defUnitArea = _updateDefaultUnit(_ud.defUnitArea, _ud.unit_m2, "Area", vo);
        _ud.defUnitDen = _updateDefaultUnit(_ud.defUnitDen, _ud.unit_kgpm3, "Density", vo);
        _ud.defUnitForce = _updateDefaultUnit(_ud.defUnitForce, _ud.unit_N, "Force", vo);
        _ud.defUnitHTC = _updateDefaultUnit(_ud.defUnitHTC, _ud.unit_Wpm2K, "Heat Transfer Coefficient", vo);
        _ud.defUnitLength = _updateDefaultUnit(_ud.defUnitLength, _ud.unit_mm, "Length", vo);
        _ud.defUnitMFR = _updateDefaultUnit(_ud.defUnitMFR, _ud.unit_kgps, "Mass Flow Rate", vo);
        _ud.defUnitPress = _updateDefaultUnit(_ud.defUnitPress, _ud.unit_Pa, "Pressure", vo);
        _ud.defUnitTemp = _updateDefaultUnit(_ud.defUnitTemp, _ud.unit_C, "Temperature", vo);
        _ud.defUnitTime = _updateDefaultUnit(_ud.defUnitTime, _ud.unit_s, "Time", vo);
        _ud.defUnitVel = _updateDefaultUnit(_ud.defUnitVel, _ud.unit_mps, "Velocity", vo);
        _ud.defUnitVisc = _updateDefaultUnit(_ud.defUnitVisc, _ud.unit_Pa_s, "Viscosity", vo);
        _ud.defUnitVolume = _updateDefaultUnit(_ud.defUnitVolume, _ud.unit_m3, "Volume", vo);
        _io.say.ok(vo);
    }

    private Units _updateUnit(String unitString, boolean vo) {
        Units u = _get.units.byName(unitString, false);
        if (u != null) {
            _io.print.msg(vo, StaticDeclarations.UNIT_FMT, "Unit read", unitString, u.getDescription());
            return u;
        }
        _io.print.value("Unit not read", unitString, true, vo);
        return null;
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
        _updateCustomUnits(vo);
    }

    /**
     * Updates default units that are shipped with STAR-CCM+.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void defaultUnits(boolean vo) {
        _updateDefaultUnits(vo);
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
        _updateDefaultUnits(false);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
