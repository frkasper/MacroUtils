package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.base.report.Report;
import star.common.Simulation;

/**
 * Low-level class for getting Report related objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetReports {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetReports(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Reports available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Reports.
     */
    public ArrayList<Report> all(boolean vo) {
        ArrayList<Report> ar = new ArrayList<>(_sim.getReportManager().getObjects());
        _io.say.objects(ar, "Getting all Reports", vo);
        return ar;
    }

    /**
     * Gets all Reports that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Reports.
     */
    public ArrayList<Report> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(_get.objects.allByREGEX(regexPatt, "Reports", new ArrayList<>(all(false)), vo));
    }

    /**
     * Gets the Report that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Report.
     */
    public Report byREGEX(String regexPatt, boolean vo) {
        return (Report) _get.objects.allByREGEX(regexPatt, "Report", new ArrayList<>(all(false)), vo).get(0);
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
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
