package macroutils.creator;

import macroutils.*;
import star.common.*;

/**
 * Low-level class for creating/updating Units with MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class CreateUnits {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateUnits(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a custom Unit.
     *
     * @param name given Unit name.
     * @param desc given Unit description.
     * @param conv given Unit conversion factor.
     * @param dim given Dimensions.
     * @return The created Unit.
     */
    public Units custom(String name, String desc, double conv, Dimensions dim) {
        return custom(name, desc, conv, dim, true);
    }

    /**
     * Creates a custom Unit.
     *
     * @param name given Unit name.
     * @param desc given Unit description.
     * @param conv given Unit conversion factor.
     * @param dim given Dimensions.
     * @param vo given verbose option. False will not print anything.
     * @return The created Unit.
     */
    public Units custom(String name, String desc, double conv, Dimensions dim, boolean vo) {
        Units u = _get.units.byName(name, false);
        if (u == null) {
            _io.print.msg(vo, _uf, "Creating Unit", name, desc);
            UserUnits uu = _sim.getUnitsManager().createUnits("Units");
            uu.setPresentationName(name);
            uu.setDescription(desc);
            uu.setConversion(conv);
            uu.setDimensions(dim);
            return uu;
        }
        _io.print.msg(vo, _uf, "Unit already exists", name, u.getDescription());
        return u;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
    }

    //--
    //-- Variables declaration area.
    //--
    private final String _uf = StaticDeclarations.UNIT_FMT;

    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
