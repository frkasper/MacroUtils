package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.base.report.IterationMonitor;
import star.base.report.Monitor;
import star.base.report.PhysicalTimeMonitor;
import star.base.report.PlotableMonitor;
import star.base.report.Report;
import star.base.report.ReportMonitor;
import star.common.Simulation;

/**
 * Low-level class for getting Monitor related objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetMonitors {

    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetMonitors(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Monitors available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Monitors.
     */
    public ArrayList<Monitor> all(boolean vo) {
        ArrayList<Monitor> ar = new ArrayList<>();
        ar.addAll(_sim.getMonitorManager().getObjects());
        _io.say.objects(ar, "Getting all Monitors", vo);
        return ar;
    }

    /**
     * Gets all Monitors that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Monitors.
     */
    public ArrayList<Monitor> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(
                _get.objects.allByREGEX(regexPatt, "Monitors", new ArrayList<>(all(false)), vo));
    }

    /**
     * Gets the Monitor that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The Monitor.
     */
    public Monitor byREGEX(String regexPatt, boolean vo) {
        return (Monitor) _get.objects.allByREGEX(regexPatt, "Monitor",
                new ArrayList<>(all(false)), vo).get(0);
    }

    /**
     * Gets the Monitor that contains the given Report, if applicable.
     *
     * @param rep given Report.
     * @param vo  given verbose option. False will not print anything.
     * @return The ReportMonitor. Null if there is no Monitor associated with the Report.
     */
    public ReportMonitor fromReport(Report rep, boolean vo) {
        _io.say.action("Getting a Monitor from a Report", vo);
        for (ReportMonitor rm : _getAllReportMonitors()) {
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
        _get = _mu.get;
        _io = _mu.io;
    }

    private ArrayList<ReportMonitor> _getAllReportMonitors() {
        ArrayList<ReportMonitor> ar = new ArrayList<>();
        for (Monitor m : all(false)) {
            if (m instanceof ReportMonitor) {
                ar.add((ReportMonitor) m);
            }
        }
        return ar;
    }

    private Monitor _getMonitor(String name) {
        return _sim.getMonitorManager().getMonitor(name);
    }

}
