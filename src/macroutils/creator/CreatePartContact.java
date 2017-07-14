package macroutils.creator;

import macroutils.MacroUtils;
import star.common.PartContactManager;
import star.common.PartSurface;
import star.common.Simulation;

/**
 * Low-level class for creating Part Contacts with MacroUtils.
 *
 * @since March of 2017
 * @author Fabio Kasper
 */
public class CreatePartContact {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreatePartContact(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private PartContactManager _getPCM() {
        return _sim.get(PartContactManager.class);
    }

    /**
     * Creates a Periodic contact between two Part Surfaces.
     *
     * @param psA given Part Surface side A.
     * @param psB given Part Surface side B.
     */
    public void periodic(PartSurface psA, PartSurface psB) {
        _io.say.action("Creating a Periodic Part Contact", true);
        _io.say.object(psA, true);
        _io.say.object(psB, true);
        _getPCM().createPeriodic(psA, psB);
        _io.say.ok(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
