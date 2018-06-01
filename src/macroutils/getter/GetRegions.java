package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.common.Region;
import star.common.Simulation;

/**
 * Low-level class for getting Regions with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetRegions {

    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetRegions(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Regions available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Regions.
     */
    public ArrayList<Region> all(boolean vo) {
        ArrayList<Region> ar = new ArrayList<>(_sim.getRegionManager().getRegions());
        _io.say.objects(ar, "Getting all Regions", vo);
        return ar;
    }

    /**
     * Gets all Regions that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Regions.
     */
    public ArrayList<Region> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(
                _get.objects.allByREGEX(regexPatt, "Regions", new ArrayList<>(all(false)), vo));
    }

    /**
     * Gets the Region that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The Region.
     */
    public Region byREGEX(String regexPatt, boolean vo) {
        return (Region) _get.objects.allByREGEX(regexPatt, "Region",
                new ArrayList<>(all(false)), vo).get(0);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

}
