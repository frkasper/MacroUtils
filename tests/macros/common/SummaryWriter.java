package common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import star.base.report.Monitor;
import star.base.report.ReportMonitor;
import star.base.report.graph.MultiAxisMonitorDataSet;
import star.common.Boundary;
import star.common.Cartesian2DPlot;
import star.common.FvRepresentation;
import star.common.GeometryPart;
import star.common.PartAxisType;
import star.common.PartGroupDataSet;
import star.common.PartSurface;
import star.common.Region;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.Units;
import star.common.graph.DataSet;
import star.meshing.CurrentDescriptionSource;
import star.vis.DisplayQuantity;
import star.vis.Displayer;
import star.vis.Legend;
import star.vis.ScalarDisplayQuantity;
import star.vis.ScalarDisplayer;
import star.vis.Scene;
import star.vis.StreamDisplayer;
import star.vis.VectorDisplayQuantity;
import star.vis.VectorDisplayer;

/**
 * Write an overview of the Simulation -- focus is on quantitative data.
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public final class SummaryWriter {

    private final boolean collectAll;
    private final MacroUtils mu;
    private final Simulation sim;
    private final String summaryFileName;
    private static final List<String> INFORMATION = new ArrayList<>();

    /**
     * Typical constructor.
     *
     * @param mu given MacroUtils instance
     */
    public SummaryWriter(MacroUtils mu) {
        this(mu, true);
    }

    /**
     * Typical constructor but with a custom summary file naming.
     *
     * @param mu       given MacroUtils instance
     * @param fileName given summary file name with extension
     */
    public SummaryWriter(MacroUtils mu, String fileName) {
        this(mu, fileName, true);
    }

    /**
     * Constructor with an option to not collect all information on execution step.
     *
     * @param mu         given MacroUtils instance
     * @param collectAll option to collect all information on {@link #execute} step
     */
    public SummaryWriter(MacroUtils mu, boolean collectAll) {
        this(mu, "Summary_" + mu.getSimulation().getPresentationName() + ".ref", collectAll);
    }

    /**
     * Constructor with an option to not collect all information on execution step and with a custom
     * summary file naming.
     *
     * @param mu         given MacroUtils instance
     * @param fileName   given summary file name with extension
     * @param collectAll option to collect all information on {@link #execute} step
     */
    public SummaryWriter(MacroUtils mu, String fileName, boolean collectAll) {
        this.mu = mu;
        this.sim = mu.getSimulation();
        this.collectAll = collectAll;
        this.summaryFileName = fileName;
        INFORMATION.clear();
    }

    /**
     * Collect information concerning geometry Parts.
     */
    public void collectGeometry() {
        collect("Geometry", () -> INFORMATION.addAll(mu.get.geometries.all(true).stream()
                .map(part -> getPartInfo(part))
                .collect(Collectors.toList())));
    }

    /**
     * Collect all information concerning mesh.
     */
    public void collectMesh() {
        collect("Mesh", () -> {
            if (mu.check.has.volumeMesh()) {
                collectVolumeMeshInfo();
            } else {
                collectSurfaceMeshInfo();
            }
        });
    }

    /**
     * Collect information concerning ReportMonitors.
     */
    public void collectMonitors() {
        collect("Monitors", () -> sim.getMonitorManager().getMonitors().stream()
                .filter(ReportMonitor.class::isInstance)
                .map(ReportMonitor.class::cast)
                .forEach(monitor -> collectMonitor(monitor)));
    }

    /**
     * Collect information concerning a specific Plot.
     *
     * @param sp given StarPlot
     */
    public void collectPlot(StarPlot sp) {
        if (sp instanceof Cartesian2DPlot plot) {
            plot.getDataSeriesOrder().stream().forEach(ds -> collectDataSet(sp, ds));
        }
    }

    /**
     * Collect information concerning MonitorPlots and XYPlots.
     */
    public void collectPlots() {
        collect("Plots", () -> sim.getPlotManager().getPlots().stream()
                .filter(Cartesian2DPlot.class::isInstance)
                .map(Cartesian2DPlot.class::cast)
                .forEach(plot -> collectPlot(plot)));
    }

    /**
     * Collect all information concerning Regions and its Boundaries.
     */
    public void collectRegions() {
        collect("Regions", () -> sim.getRegionManager().getRegions().stream()
                .forEach(r -> collectRegion(r)));
    }

    /**
     * Collect information concerning Reports.
     */
    public void collectReports() {
        collect("Reports", () -> INFORMATION.addAll(sim.getReportManager().getObjects().stream()
                .map(r -> getPair(mu.get.strings.information(r), r.getReportMonitorValue()))
                .collect(Collectors.toList())));
    }

    /**
     * Collect information concerning Scenes and its dependents.
     */
    public void collectScenes() {
        collect("Scenes", () -> sim.getSceneManager().getScenes()
                .forEach(scene -> collectDisplayers(scene)));
    }

    /**
     * Collect information concerning Solution.
     */
    public void collectSolution() {
        if (mu.check.is.unsteady()) {
            INFORMATION.add(getPair("Time", mu.get.solver.physicalTime()));
        }
        INFORMATION.add(getPair("Iteration", mu.get.solver.iteration()));
    }

    /**
     * Execute this class -- call is mandatory.
     */
    public void execute() {
        if (collectAll) {
            collectAll();
        }
        mu.io.say.msg(INFORMATION.toString());
        writeInformation();
    }

    private void collect(String key, Collector lambda) {
        int size = INFORMATION.size();
        mu.io.print.action("Collecting " + key + " data", true);
        lambda.collect();
        int added = INFORMATION.size() - size;
        mu.io.print.msg(true, "Collected %d new %s.", added, added > 1 ? "entries" : "entry");
    }

    private void collectAll() {
        collectGeometry();
        collectMesh();
        collectRegions();
        collectSolution();
        collectReports();
        collectMonitors();
        collectPlots();
        collectScenes();
    }

    private void collectBoundary(Boundary b) {
        String key = mu.get.strings.information(b.getRegion());
        INFORMATION.add(key + " -> " + mu.get.strings.information(b));
    }

    private void collectDataSet(StarPlot sp, DataSet ds) {


        String dataSetName = ds.getPresentationName();
        String info = mu.get.strings.information(sp);
        boolean monitor = ds instanceof MultiAxisMonitorDataSet;
        String key = monitor ? info : info + " -> " + ds.getPresentationName();
        double[] xx = ds.getXValues();
        double[] yy = ds.getYValues();
        int samples = ds.getRowCount();

        if (mu.check.is.histogram(sp) && ds instanceof PartGroupDataSet pgds) {
            PartAxisType xAxis = mu.get.plots.axisX(pgds);
            String function = xAxis.getScalarFunction().getFieldFunction().getPresentationName();
            int bins = mu.get.plots.axisX(pgds).hasBinningDescriptor().getNumberOfBins();
            INFORMATION.add(getPair(info + " -> Function", function));
            INFORMATION.add(getPair(info + " -> Bins", bins));
        } else if (samples > 50) {
            INFORMATION.add(getPair(key + " -> " + dataSetName + " -> Samples", samples));
        } else {
            INFORMATION.add(getPair(key + " -> " + dataSetName + " -> X values", xx));
            INFORMATION.add(getPair(key + " -> " + dataSetName + " -> Y values", yy));
        }

    }

    private void collectDisplayers(Scene scene) {
        List<String> displayers = scene.getDisplayerManager().getDisplayers().stream()
                .map(displayer -> getDisplayerInfo(displayer))
                .collect(Collectors.toList());
        INFORMATION.addAll(displayers);
    }

    private void collectMonitor(Monitor m) {

        String key = mu.get.strings.information(m);

        if (m instanceof ReportMonitor rm) {
            INFORMATION.add(getPair(key + " -> Samples", rm.getAllYValues().length));
            INFORMATION.add(getPair(key + " -> Report", rm.getReport().getPresentationName()));
        }

    }

    private void collectRegion(Region r) {
        r.getBoundaryManager().getBoundaries().stream().forEach(b -> collectBoundary(b));
    }

    private void collectSurfaceMeshInfo() {
        CurrentDescriptionSource latestSurface = mu.get.mesh.latestSurfaceDescriptionSource();
        INFORMATION.add(getPair("Face Count", latestSurface.getFaceCount()));
        INFORMATION.add(getPair("Vertex Count", latestSurface.getVertexCount()));
    }

    private void collectVolumeMeshInfo() {
        FvRepresentation fvr = mu.get.mesh.fvr();
        INFORMATION.add(getPair("Cell Count", fvr.getCellCount()));
        INFORMATION.add(getPair("Face Count", fvr.getInteriorFaceCount()));
        INFORMATION.add(getPair("Vertex Count", fvr.getVertexCount()));
    }

    private String getDisplayerInfo(Displayer displayer) {
        String key = mu.get.strings.information(displayer);
        List<String> info = new ArrayList<>();
        info.add(getPair(key + " Opacity", displayer.getOpacity()));
        Legend leg = getLegend(displayer);
        if (leg != null) {
            double[] minMax = getMinMax(displayer);
            info.add(getPair(key + " MIN", minMax[0]));
            info.add(getPair(key + " MAX", minMax[1]));
            info.add(getPair(key + " Units", getUnits(displayer).getPresentationName()));
            info.add(getPair(key + " Color Map", leg.getLookupTable().getPresentationName()));
            info.add(getPair(key + " Color Map Levels", leg.getLevels()));
            info.add(getPair(key + " Legend Position", leg.getPositionCoordinate().toString()));
            info.add(getPair(key + " Legend Label Format", leg.getLabelFormat(), true));
            info.add(getPair(key + " Legend Number of Labels", leg.getNumberOfLabels()));
        } else {
            info.add(getPair(key, "N/A"));
        }
        return String.join("\n", info);
    }

    private Legend getLegend(Displayer displayer) {
        switch (displayer) {
            case ScalarDisplayer scalarDisplayer -> {
                return scalarDisplayer.getLegend();
            }
            case VectorDisplayer vectorDisplayer -> {
                return vectorDisplayer.getLegend();
            }
            case StreamDisplayer streamDisplayer -> {
                return streamDisplayer.getLegend();
            }
            default -> {
                return null;
            }
        }
    }

    private <T extends DisplayQuantity> double[] getMinMax(T t) {
        switch (t) {
            case ScalarDisplayQuantity sdq -> {
                return new double[]{ sdq.getRangeMin(), sdq.getRangeMax() };
            }
            case VectorDisplayQuantity vdq -> {
                return new double[]{ vdq.getMinimumValue().getRawValue(),
                    vdq.getMaximumValue().getRawValue() };
            }
            default -> {
                return null;
            }
        }
    }

    private double[] getMinMax(Displayer displayer) {
        if (displayer instanceof ScalarDisplayer) {
            return getMinMax(((ScalarDisplayer) displayer).getScalarDisplayQuantity());
        } else if (displayer instanceof StreamDisplayer) {
            return getMinMax(((StreamDisplayer) displayer).getScalarDisplayQuantity());
        } else {
            return getMinMax(((VectorDisplayer) displayer).getVectorDisplayQuantity());
        }
    }

    private String getPair(String key, String value) {
        return getPair(key, value, false);
    }

    private String getPair(String key, String value, boolean doubleQuoted) {
        String fmt = "%s: " + ((doubleQuoted) ? "\"%s\"" : "%s");
        return String.format(fmt, key, value);
    }

    private String getPair(String key, double value) {
        return getPair(key, getString(value));
    }

    private String getPair(String key, double[] values) {
        List<String> ls = Arrays.stream(values)
                .mapToObj(d -> getString(d))
                .collect(Collectors.toList());
        return getPair(key, ls.toString());
    }

    private String getPair(String key, int value) {
        return getPair(key, String.format("%d", value));
    }

    private String getPartInfo(GeometryPart gp) {
        String key = mu.get.strings.information(gp);
        List<PartSurface> allPS = mu.get.partSurfaces.all(gp, true);
        int nps = allPS.size();
        return getPair(key, String.format("%d Part Surface%s", nps, nps > 1 ? "s" : ""));
    }

    private String getString(double value) {
        return String.format("%.6e", value);
    }

    private Units getUnits(Displayer displayer) {
        if (displayer instanceof ScalarDisplayer) {
            return ((ScalarDisplayer) displayer).getScalarDisplayQuantity().getUnits();
        } else if (displayer instanceof StreamDisplayer) {
            return ((StreamDisplayer) displayer).getScalarDisplayQuantity().getUnits();
        } else {
            return ((VectorDisplayer) displayer).getVectorDisplayQuantity().getUnits();
        }
    }

    private void writeInformation() {
        mu.io.print.action("Writing summary file", true);
        File file = new File(sim.getSessionDir(), summaryFileName);
        mu.io.write.data(file, new ArrayList<>(INFORMATION), true);
    }

    /**
     * Lambda function.
     */
    @FunctionalInterface
    private interface Collector {

        public void collect();

    }

}
