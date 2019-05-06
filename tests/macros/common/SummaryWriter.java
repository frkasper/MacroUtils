package common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import star.base.report.Monitor;
import star.base.report.MonitorManager;
import star.base.report.ReportMonitor;
import star.common.Boundary;
import star.common.FvRepresentation;
import star.common.GeometryPart;
import star.common.HistogramAxisType;
import star.common.HistogramPlot;
import star.common.MonitorPlot;
import star.common.PartSurface;
import star.common.PlotManager;
import star.common.Region;
import star.common.ResidualPlot;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.XYPlot;
import star.common.YAxisType;
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
        int n = printCollecting("Geometry");
        List<String> parts = mu.get.geometries.all(true).stream()
                .map(part -> getPartInfo(part))
                .collect(Collectors.toList());
        INFORMATION.addAll(parts);
        printCollected(n);
    }

    /**
     * Collect all information concerning mesh.
     */
    public void collectMesh() {
        int n = printCollecting("Mesh");
        if (mu.check.has.volumeMesh()) {
            collectVolumeMeshInfo();
        } else {
            collectSurfaceMeshInfo();
        }
        printCollected(n);
    }

    /**
     * Collect information concerning ReportMonitors.
     */
    public void collectMonitors() {

        final MonitorManager mm = sim.getMonitorManager();

        int n = printCollecting("Monitors");

        mm.getObjectsOf(ReportMonitor.class).stream().forEach(rm -> collectMonitor(rm));

        printCollected(n);

    }

    /**
     * Collect information concerning a specific Plot.
     *
     * @param sp given StarPlot
     */
    public void collectPlot(StarPlot sp) {

        final boolean isXYPlot = sp instanceof XYPlot;
        final boolean isResidual = sp instanceof ResidualPlot;
        final boolean isHistogram = sp instanceof HistogramPlot;

        final boolean collectDataSets = !(isResidual || isHistogram);

        if (isXYPlot) {  // Internal datasets only in XYPlots
            collectXYPlot((XYPlot) sp);
        } else if (isHistogram) {
            collectHistogram((HistogramPlot) sp);
        }

        if (collectDataSets) {
            sp.getDataSetManager().getDataSets().stream().forEach(ds -> collectDataSet(sp, ds));
        }

    }

    /**
     * Collect information concerning MonitorPlots and XYPlots.
     */
    public void collectPlots() {

        final PlotManager pm = sim.getPlotManager();

        int n = printCollecting("Plots");

        List<StarPlot> selectedPlots = new ArrayList<>();

        selectedPlots.addAll(pm.getObjectsOf(HistogramPlot.class));
        selectedPlots.addAll(pm.getObjectsOf(XYPlot.class));
        selectedPlots.addAll(pm.getObjectsOf(MonitorPlot.class));

        selectedPlots.stream().forEach(plot -> collectPlot(plot));

        printCollected(n);

    }

    /**
     * Collect all information concerning Regions and its Boundaries.
     */
    public void collectRegions() {
        int n = printCollecting("Region");
        sim.getRegionManager().getRegions().stream().forEach(r -> collectRegion(r));
        printCollected(n);
    }

    /**
     * Collect information concerning Reports.
     */
    public void collectReports() {
        int n = printCollecting("Reports");
        List<String> reports = sim.getReportManager().getObjects().stream()
                .map(r -> getPair(mu.get.strings.information(r), r.getReportMonitorValue()))
                .collect(Collectors.toList());
        INFORMATION.addAll(reports);
        printCollected(n);
    }

    /**
     * Collect information concerning Scenes and its dependents.
     */
    public void collectScenes() {
        int n = printCollecting("Scenes");
        Collection<Scene> allScenes = sim.getSceneManager().getObjects();
        allScenes.forEach(scene -> scene.open());
        allScenes.forEach(scene -> collectDisplayers(scene));
        printCollected(n);
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

        final boolean isMonitorPlot = sp instanceof MonitorPlot;
        final String info = mu.get.strings.information(sp);

        String key = isMonitorPlot ? info : info + " -> " + ds.getPresentationName();
        Arrays.stream(ds.getSeriesLabels()).forEach(sl -> collectDataSetSeries(key, ds, sl));

    }

    private void collectDataSetSeries(String key, DataSet ds, String seriesName) {

        int i = Arrays.asList(ds.getSeriesLabels()).indexOf(seriesName);

        final double[] xx = ds.getXSeries(i);
        final double[] yy = ds.getYSeries(i);

        if (xx.length > 50) {
            INFORMATION.add(getPair(key + " -> " + seriesName + " -> Samples", xx.length));
        } else {
            INFORMATION.add(getPair(key + " -> " + seriesName + " -> X values", xx));
            INFORMATION.add(getPair(key + " -> " + seriesName + " -> Y values", yy));
        }

    }

    private void collectDataSets(StarPlot sp, YAxisType yat) {
        yat.getDataSetManager().getObjects().stream().forEach(ds -> collectDataSet(sp, ds));
    }

    private void collectDisplayers(Scene scene) {
        List<String> displayers = scene.getDisplayerManager().getObjects().stream()
                .map(displayer -> getDisplayerInfo(displayer))
                .collect(Collectors.toList());
        INFORMATION.addAll(displayers);
    }

    private void collectHistogram(HistogramPlot hp) {

        final String info = mu.get.strings.information(hp);
        final HistogramAxisType hat = hp.getXAxisType();

        final String function = hat.getBinFunction().getFieldFunction().getPresentationName();
        final int bins = hat.getNumberOfBin();

        INFORMATION.add(getPair(info + " -> Function", function));
        INFORMATION.add(getPair(info + " -> Bins", bins));

    }

    private void collectMonitor(Monitor m) {

        String key = mu.get.strings.information(m);

        if (m instanceof ReportMonitor) {
            ReportMonitor rm = (ReportMonitor) m;
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

    private void collectXYPlot(XYPlot xyp) {

        xyp.getYAxes().getObjects().stream()
                .map(YAxisType.class::cast)
                .forEach(yat -> collectDataSets(xyp, yat));

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
        if (displayer instanceof ScalarDisplayer) {
            return ((ScalarDisplayer) displayer).getLegend();
        } else if (displayer instanceof VectorDisplayer) {
            return ((VectorDisplayer) displayer).getLegend();
        } else if (displayer instanceof StreamDisplayer) {
            return ((StreamDisplayer) displayer).getLegend();
        } else {
            return null;
        }
    }

    private <T extends DisplayQuantity> double[] getMinMax(T t) {
        if (t instanceof ScalarDisplayQuantity) {
            ScalarDisplayQuantity sdq = (ScalarDisplayQuantity) t;
            return new double[]{ sdq.getRangeMin(), sdq.getRangeMax() };
        } else {
            VectorDisplayQuantity vdq = (VectorDisplayQuantity) t;
            return new double[]{ vdq.getMinMagnitude(), vdq.getMaxMagnitude() };
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

    private void printCollected(int initialSize) {
        int net = INFORMATION.size() - initialSize;
        mu.io.print.msg(true, "Collected %d entr%s.", net, net > 1 ? "ies" : "y");
    }

    private int printCollecting(String key) {
        mu.io.print.action("Collecting " + key + " data", true);
        return INFORMATION.size();
    }

    private void writeInformation() {
        mu.io.print.action("Writing summary file", true);
        File file = new File(sim.getSessionDir(), summaryFileName);
        mu.io.write.data(file, new ArrayList<>(INFORMATION), true);
    }

}
