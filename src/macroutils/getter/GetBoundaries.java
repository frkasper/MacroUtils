package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.common.*;

/**
 * Low-level class for getting Boundaries with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetBoundaries {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetBoundaries(MacroUtils m) {
        _mu = m;
    }

    /**
     * Gets all Boundaries from all Regions available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Boundaries.
     */
    public ArrayList<Boundary> all(boolean vo) {
        ArrayList<Boundary> ab = new ArrayList();
        _io.say.msg(vo, "Getting all Boundaries from all Regions...");
        for (Region r : _get.regions.all(false)) {
            ab.addAll(r.getBoundaryManager().getBoundaries());
        }
        _io.say.msg(vo, "Boundaries found: %d", ab.size());
        return ab;
    }

    /**
     * Gets all Boundaries that matches the REGEX search pattern from all Regions available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Boundaries.
     */
    public ArrayList<Boundary> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList(_get.objects.allByREGEX(regexPatt, "all Boundaries", new ArrayList(all(false)), true));
    }

    /**
     * Gets a Boundary that matches the REGEX search pattern among all Boundaries available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Boundary. Null if nothing is found.
     */
    public Boundary byREGEX(String regexPatt, boolean vo) {
        return (Boundary) _get.objects.byREGEX(regexPatt, "Boundary", new ArrayList(all(false)), vo);
    }

    /**
     * Gets a Boundary within a Region that matches the REGEX search pattern.
     *
     * @param r given Region.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Boundary. Null if nothing is found.
     */
    public Boundary byREGEX(Region r, String regexPatt, boolean vo) {
        return (Boundary) _get.objects.byREGEX(regexPatt, "Boundary",
                new ArrayList(r.getBoundaryManager().getBoundaries()), vo);
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
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
