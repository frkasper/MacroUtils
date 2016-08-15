package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.common.*;

/**
 * Low-level class for getting Regions with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetRegions {

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
        ArrayList<Region> ar = new ArrayList(_sim.getRegionManager().getRegions());
        _tmpl.print.getAll("Regions", new ArrayList(ar), vo);
        return ar;
    }

    /**
     * Gets all Regions that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Regions.
     */
    public ArrayList<Region> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList(_get.objects.allByREGEX(regexPatt, "Regions", new ArrayList(all(false)), true));
    }

    /**
     * Gets the Region that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Region.
     */
    public Region byREGEX(String regexPatt, boolean vo) {
        return (Region) _get.objects.allByREGEX(regexPatt, "Region", new ArrayList(all(false)), true).get(0);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _tmpl = _mu.templates;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
