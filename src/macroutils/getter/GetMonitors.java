package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.report.*;
import star.common.*;

/**
 * Low-level class for getting Monitor related objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetMonitors {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetMonitors(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private Monitor _getMonitor(String name) {
        return _sim.getMonitorManager().getMonitor(name);
    }

    /**
     * Gets all Monitors available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Monitors.
     */
    public ArrayList<ReportMonitor> all(boolean vo) {
        ArrayList<ReportMonitor> ar = new ArrayList();
        for (Monitor m : _sim.getMonitorManager().getObjects()) {
            if (m instanceof ReportMonitor) {
                ar.add((ReportMonitor) m);
            }
        }
        _io.say.objects(ar, "Getting all Report Monitors", vo);
        return ar;
    }

    /**
     * Gets all Monitors that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Monitors.
     */
    public ArrayList<ReportMonitor> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList(_get.objects.allByREGEX(regexPatt, "Monitors", new ArrayList(all(false)), vo));
    }

    /**
     * Gets the Monitor that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The ReportMonitor.
     */
    public ReportMonitor byREGEX(String regexPatt, boolean vo) {
        return (ReportMonitor) _get.objects.allByREGEX(regexPatt, "Monitor", new ArrayList(all(false)), vo).get(0);
    }

    /**
     * Gets the Monitor that contains the given Report, if applicable.
     *
     * @param rep given Report.
     * @param vo given verbose option. False will not print anything.
     * @return The ReportMonitor. Null if there is no Monitor associated with the Report.
     */
    public ReportMonitor fromReport(Report rep, boolean vo) {
        _io.say.action("Getting a Monitor from a Report", vo);
        for (ReportMonitor rm : all(false)) {
            if (rm.getReport() == rep) {
                _io.say.value("Found", rm.getPresentationName(), true, vo);
                return rm;
            }
        }
        _io.say.msg("Nothing found. Returning NULL!");
        return null;
    }

    /**
     * Gets the Iteration Monitor which is a type of {@link PlotableMonitor}.
     *
     * @return The Iteration Monitor.
     */
    public IterationMonitor iteration() {
        return ((IterationMonitor) _getMonitor("Iteration"));
    }

    /**
     * Gets the Physical Time Monitor which is a type of {@link PlotableMonitor}.
     *
     * @return The Physical Time Monitor.
     */
    public PhysicalTimeMonitor physicalTime() {
        return ((PhysicalTimeMonitor) _getMonitor("Physical Time"));
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
        _tmpl = _mu.templates;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
