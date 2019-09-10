package macroutils.misc;

import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.common.Simulation;
import star.vis.Displayer;

/**
 * Main class for "resetting" objects or variables in MacroUtils.
 *
 * @since July of 2016
 * @author Fabio Kasper
 */
public class MainResetter {

    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainResetter(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Resets internal MacroUtils variable:
     * <ul>
     * <li>{@link UserDeclarations#picPath}
     * </ul>
     */
    public void picPath() {
        _ud.picPath = _sim.getSessionDir();
    }

    /**
     * Resets internal MacroUtils variable:
     * <ul>
     * <li>{@link UserDeclarations#simPath}
     * </ul>
     */
    public void simPath() {
        _ud.simPath = _sim.getSessionDir();
    }

    /**
     * Resets the Transform on a Displayer.
     *
     * @param d given Displayer.
     */
    public void transform(Displayer d) {
        if (d == null) {
            return;
        }
        d.setVisTransform(_get.objects.transform("Identity", false));
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
        _io.print.msgDebug("" + this.getClass().getSimpleName()
                + " instances updated succesfully.");
    }

}
