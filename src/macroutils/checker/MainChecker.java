package macroutils.checker;

import macroutils.MacroUtils;

/**
 * Main class for check-type (e.g.: is/has) methods in MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class MainChecker {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainChecker(MacroUtils m) {
        _mu = m;
        has = new CheckHas(m);
        is = new CheckIs(m);
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        has.updateInstances();
        is.updateInstances();
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;

    /**
     * This class is responsible for assessing is-type comparisons.
     */
    public CheckHas has = null;

    /**
     * This class is responsible for assessing is-type comparisons.
     */
    public CheckIs is = null;

}
