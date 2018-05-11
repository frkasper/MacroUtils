package common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import star.base.neo.NamedObject;
import star.base.report.Report;
import star.common.FvRepresentation;
import star.common.GeometryPart;
import star.common.PartSurface;
import star.common.Simulation;
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

    private final MacroUtils mu;
    private final Simulation sim;
    private static final List<String> INFORMATION = new ArrayList<>();

    public SummaryWriter(MacroUtils mu) {
        this.mu = mu;
        this.sim = mu.getSimulation();
    }

    /**
     * Collect information for concerning geometry Parts.
     */
    public void collectGeometry() {
        List<String> parts = mu.get.geometries.all(true).stream()
                .map(part -> getPartInfo(part))
                .collect(Collectors.toList());
        INFORMATION.addAll(parts);
    }

    /**
     * Collect information for concerning mesh.
     */
    public void collectMesh() {
        if (mu.check.has.volumeMesh()) {
            collectVolumeMeshInfo();
        } else {
            collectSurfaceMeshInfo();
        }
    }

    /**
     * Collect information for concerning Reports.
     */
    public void collectReports() {
        List<String> reports = sim.getReportManager().getObjects().stream()
                .map(r -> getPair(r))
                .collect(Collectors.toList());
        INFORMATION.addAll(reports);
    }

    /**
     * Collect information for concerning Scenes and its dependents.
     */
    public void collectScenes() {
        Collection<Scene> allScenes = sim.getSceneManager().getObjects();
        allScenes.forEach(scene -> scene.open());
        allScenes.forEach(scene -> collectDisplayers(scene));
    }

    /**
     * Collect information for concerning Solution.
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
        collectAll();
        mu.io.say.msg(INFORMATION.toString());
        writeInformation();
    }

    private void collectAll() {
        collectGeometry();
        collectMesh();
        collectSolution();
        collectReports();
        collectScenes();
    }

    private void collectDisplayers(Scene scene) {
        List<String> displayers = scene.getDisplayerManager().getObjects().stream()
                .map(displayer -> getDisplayerInfo(displayer))
                .collect(Collectors.toList());
        INFORMATION.addAll(displayers);
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

    private String getPair(NamedObject no) {
        String info = mu.get.strings.information(no);
        if (no instanceof Report) {
            return getPair(info, ((Report) no).getReportMonitorValue());
        } else {
            return getPair(info, "Unknown value");
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
        return getPair(key, String.format("%.6e", value));
    }

    private String getPair(String key, int value) {
        return getPair(key, String.format("%d", value));
    }

    private String getPartInfo(GeometryPart gp) {
        String key = mu.get.strings.information(gp);
        List<PartSurface> allPS = mu.get.partSurfaces.all(gp, true);
        String plural = (allPS.size() > 1) ? "s" : "";
        return getPair(key, String.format("%d Part Surface%s", allPS.size(), plural));
    }

    private void writeInformation() {
        File file = new File(sim.getSessionDir(), "Summary_" + sim.getPresentationName() + ".ref");
        mu.io.write.data(file, new ArrayList<>(INFORMATION), true);
    }

}
