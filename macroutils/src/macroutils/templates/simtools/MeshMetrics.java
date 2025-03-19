package macroutils.templates.simtools;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import macroutils.creator.MainCreator;
import macroutils.getter.MainGetter;
import macroutils.setter.MainSetter;
import star.base.report.Monitor;
import star.common.Cartesian2DPlot;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.UserTag;

/**
 * This class will create some post processing objects to evaluate Mesh Metrics in current
 * Simulation.
 *
 * The following will be created:
 * <ul>
 * <li>Histogram Plots covering important mesh metrics;
 * </ul>
 *
 * @since April of 2019
 * @author Fabio Kasper
 */
public class MeshMetrics {

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
    public MeshMetrics(MacroUtils m) {

        _mu = m;
        _sim = m.getSimulation();
        _ud = _mu.userDeclarations;
        _add = _mu.add;
        _get = _mu.get;
        _set = _mu.set;

        _tag = _add.tools.tag("Mesh Metrics");

    }

    /**
     * Execute this class.
     */
    public void execute() {
        createHistograms();
        _mu.templates.prettify.plots();
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

    private void createHistogram(Metric metric) {

        String name;

        _ud.namedObjects.clear();
        _ud.ff = _get.objects.fieldFunction(metric.getFunction(), true);

        if (metric instanceof VolumeMeshMetric) {
            _ud.namedObjects.addAll(_get.regions.all(false));
            name = metric.getFunction() + " on All Regions";
        } else {
            _ud.namedObjects.addAll(_get.partSurfaces.all(false));
            name = metric.getFunction() + " Part Surfaces";
        }

        Cartesian2DPlot hp = _add.plot.histogram(_ud.namedObjects, _ud.ff);
        hp.setPresentationName(name);
        hp.setTitle(name);
        hp.open();

        _set.object.tag(hp, _tag, true);

    }

    private void createHistograms() {

        Arrays.stream(SurfaceMeshMetric.values()).forEach(metric -> createHistogram(metric));
        Arrays.stream(VolumeMeshMetric.values()).forEach(metric -> createHistogram(metric));

    }

    private interface Metric {

        public String getFunction();

    }

    private enum SurfaceMeshMetric implements Metric {

        FACE_VALIDITY("Face Validity");

        private final String function;

        private SurfaceMeshMetric(final String function) {
            this.function = function;
        }

        @Override
        public String getFunction() {
            return function;
        }

    }

    private enum VolumeMeshMetric implements Metric {

        CELL_QUALITY("Cell Quality"),
        SKEWNESS_ANGLE("Skewness Angle"),
        VOLUME_CHANGE("Volume Change");

        private final String function;

        private VolumeMeshMetric(final String function) {
            this.function = function;
        }

        @Override
        public String getFunction() {
            return function;
        }

    }

}
