package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.common.Part;
import star.common.Simulation;

/**
 * Low-level class for getting Parts in general, e.g.: Derived Parts, with MacroUtils.
 *
 * @since August of 2016
 * @author Fabio Kasper
 */
public class GetParts {

    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetParts(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Parts available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Parts.
     */
    public ArrayList<Part> all(boolean vo) {
        _io.say.msg(vo, "Getting all Parts...");
        ArrayList<Part> ap = new ArrayList<>(_sim.getPartManager().getObjects());
        _io.say.msg(vo, "Parts found: %d", ap.size());
        return ap;
    }

    /**
     * Gets all Parts that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Parts.
     */
    public ArrayList<Part> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(
                _get.objects.allByREGEX(regexPatt, "all Parts", new ArrayList<>(all(false)), vo));
    }

    /**
     * Gets a Part that matches the REGEX search pattern among all Parts available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The Part. Null if nothing is found.
     */
    public Part byREGEX(String regexPatt, boolean vo) {
        return (Part) _get.objects.byREGEX(regexPatt, "Part Surface",
                new ArrayList<>(all(false)), vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

}
