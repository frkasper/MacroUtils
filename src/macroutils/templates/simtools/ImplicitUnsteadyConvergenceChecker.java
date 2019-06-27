package macroutils.templates.simtools;

import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import macroutils.creator.MainCreator;
import macroutils.getter.MainGetter;
import macroutils.setter.MainSetter;
import star.base.report.Monitor;
import star.base.report.Report;
import star.base.report.ReportMonitor;
import star.common.MonitorPlot;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.StarUpdateModeOption;
import star.common.SymbolShapeOption;
import star.common.UserTag;
import star.common.graph.DataSet;

/**
 * This class will create a plot for every Report available in the Simulation.
 *
 * In addition, it will perform the following actions:
 * <ul>
 * <li>Create a ReportMonitor and set to update at every iteration;
 * <li>Create a MonitorPlot from the above and set to update at every iteration;
 * <li>Set a custom tag at every object created above;
 * </ul>
 *
 * <b>Application</b>: when performing implicit unsteady simulations, such as SIMPLE scheme
 * available in STAR-CCM+, it is very important that all timesteps are successfully converged.
 *
 * Otherwise error will build up over time.
 *
 * @since April of 2019
 * @author Fabio Kasper
 */
public class ImplicitUnsteadyConvergenceChecker {

    private final MainCreator _add;
    private final MainGetter _get;
    private final MacroUtils _mu;
    private final MainSetter _set;
    private final Simulation _sim;
    private final UserTag _tag;
    private final UserDeclarations _ud;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public ImplicitUnsteadyConvergenceChecker(MacroUtils m) {

        _mu = m;
        _sim = m.getSimulation();
        _ud = _mu.userDeclarations;
        _add = _mu.add;
        _get = _mu.get;
        _set = _mu.set;

        _tag = _add.tools.tag("Convergence Checker");

    }

    /**
     * Execute this class.
     */
    public void execute() {

        _mu.get.reports.all(true).forEach(r -> createConvergenceCheckPlot(r));

    }

    /**
     * Remove all artifacts created by this class.
     */
    public void removeArtifacts() {

        _mu.io.say.action("Removing artifacts", true);

        List<StarPlot> createdPlots = _get.plots.all(false).stream()
                .filter(plot -> plot.getTagGroup().has(_tag))
                .collect(Collectors.toList());

        List<Monitor> createdMonitors = _get.monitors.all(false).stream()
                .filter(monitor -> monitor.getTagGroup().has(_tag))
                .collect(Collectors.toList());

        _mu.io.say.msg("Removing Plots...");
        _sim.getPlotManager().removeObjects(createdPlots);

        _mu.io.say.msg("Removing Monitors...");
        _sim.getMonitorManager().removeObjects(createdMonitors);
        _mu.io.say.ok(true);

        _mu.remove.tag(_tag);

    }

    private void createConvergenceCheckPlot(Report report) {

        final String name = _tag.getPresentationName() + ": " + report.getPresentationName();

        _ud.monitors.clear();

        // Create ReportMonitor and set to update at every iteration
        ReportMonitor rm = report.createMonitor();
        rm.setPresentationName(name);
        rm.getStarUpdate().getUpdateModeOption().setSelected(StarUpdateModeOption.Type.ITERATION);
        _ud.monitors.add(rm);

        // Create MonitorPlot and set to update at every iteration
        MonitorPlot mp = _sim.getPlotManager().createMonitorPlot(_ud.monitors, name);
        mp.setXAxisMonitor(_get.monitors.iteration());
        mp.getLegend().setVisible(false);

        // Now prettify the DataSet
        DataSet ds = mp.getDataSetCollection().stream().findFirst().get();
        ds.getSymbolStyle().getSymbolShapeOption().setSelected(SymbolShapeOption.Type.STAR);

        _set.object.tag(rm, _tag, true);
        _set.object.tag(mp, _tag, true);

    }

}
