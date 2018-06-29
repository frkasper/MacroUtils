package macroutils.setter;

import macroutils.MacroUtils;
import star.common.Region;
import star.motion.Motion;
import star.motion.MotionSpecification;

/**
 * Low-level class for setting Region parameters with MacroUtils.
 *
 * @since May of 2016
 * @author Fabio Kasper
 */
public class SetRegions {

    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetRegions(MacroUtils m) {
        _mu = m;
    }

    /**
     * Sets Motion to a Region.
     *
     * @param m given Motion.
     * @param r given Region.
     */
    public void motion(Motion m, Region r) {
        _io.say.action("Setting Motion to a Region", true);
        _io.say.object(m, true);
        _io.say.object(r, true);
        ((MotionSpecification) r.getValues().get(MotionSpecification.class)).setMotion(m);
        _io.say.ok(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

}
