package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.report.*;
import star.common.*;

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
        ArrayList<Report> ar = new ArrayList(_sim.getReportManager().getObjects());
        _tmpl.print.getAll("Reports", new ArrayList(ar), vo);
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
        return new ArrayList(_get.objects.allByREGEX(regexPatt, "Reports", new ArrayList(all(false)), true));
    }

    /**
     * Gets the Report that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Report.
     */
    public Report byREGEX(String regexPatt, boolean vo) {
        return (Report) _get.objects.allByREGEX(regexPatt, "Report", new ArrayList(all(false)), true).get(0);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _tmpl = _mu.templates;
        _get = _mu.get;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
