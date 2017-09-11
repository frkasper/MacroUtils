package macroutils.getter;

import macroutils.*;
import star.base.report.*;
import star.common.*;

/**
 * Low-level class for getting Units with MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class GetUnits {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetUnits(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets a unit by its Presentation Name.
     *
     * @param name given unit name.
     * @return The Unit. Null if nothing is found.
     */
    public Units byName(String name) {
        return byName(name, true);
    }

    /**
     * Gets a unit by its Presentation Name.
     *
     * @param name given unit name.
     * @param vo given verbose option. False will not print anything.
     * @return The Unit. Null if nothing is found.
     */
    public Units byName(String name, boolean vo) {
        UnitsManager um = _sim.getUnitsManager();
        if (name.equals(_unitDimensionless)) {
            return um.getObject(_unitDimensionless);
        }
        _io.print.value("Getting Unit by exact match", name, true, vo);
        for (Units u : um.getObjects()) {
            if (_get.strings.fromUnit(u).equals(name) || u.getDescription().equals(name)) {
                _io.say.value("Found", _get.strings.fromUnit(u), true, vo);
                return u;
            }
        }
        _io.say.msg("Nothing found. Returning NULL!", vo);
        return null;
    }

    /**
     * Gets the corresponding dimensions from the unit.
     *
     * @param u given unit.
     * @return The Dimensions.
     */
    public Dimensions dimensions(Units u) {
        return (u != null) ? u.getDimensions() : new Dimensions();
    }

    /**
     * Gets the corresponding unit associated with a Monitor.
     *
     * @param m given Monitor.
     * @return The Unit. Null if nothing is found.
     */
    public Units fromMonitor(Monitor m) {
        if (m instanceof ResidualMonitor) {
            return byName(_unitDimensionless, false);
        } else {
            return (m instanceof ReportMonitor) ? ((ReportMonitor) m).getReport().getUnits() : null;
        }
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private final String _s = StaticDeclarations.UNIT_DIMENSIONLESS;
    private final String _unitDimensionless = _s;

    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
