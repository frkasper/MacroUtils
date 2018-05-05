
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
import star.common.StarMacro;
import star.meshing.CurrentDescriptionSource;
import star.vis.Displayer;
import star.vis.ScalarDisplayer;
import star.vis.Scene;
import star.vis.StreamDisplayer;
import star.vis.VectorDisplayer;

/**
 * Write an overview of the Simulation -- focus is on quantitative data.
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class WriteSummaryTest extends StarMacro {

    private MacroUtils mu;
    private Simulation sim;
    private static final List<String> INFORMATION = new ArrayList<>();

    @Override
    public void execute() {

        sim = getSimulation();

        mu = new MacroUtils(sim, false);

        collectGeometryInfo();

        collectMeshInfo();

        collectSolutionInfo();

        collectReports();

        collectScenes();

        writeInformation();

    }

    private void collectDisplayers(Scene scene) {
        List<String> displayers = scene.getDisplayerManager().getObjects().stream()
                .map(displayer -> getDisplayerInfo(displayer))
                .collect(Collectors.toList());
        INFORMATION.addAll(displayers);
    }

    private void collectGeometryInfo() {
        List<String> parts = mu.get.geometries.all(true).stream()
                .map(part -> getPartInfo(part))
                .collect(Collectors.toList());
        INFORMATION.addAll(parts);
    }

    private void collectMeshInfo() {
        if (mu.check.has.volumeMesh()) {
            collectVolumeMeshInfo();
        } else {
            collectSurfaceMeshInfo();
        }
    }

    private void collectReports() {
        List<String> reports = sim.getReportManager().getObjects().stream()
                .map(r -> getPair(r))
                .collect(Collectors.toList());
        INFORMATION.addAll(reports);
    }

    private void collectScenes() {
        Collection<Scene> allScenes = sim.getSceneManager().getObjects();
        allScenes.stream().forEach(scene -> scene.open());
        allScenes.stream().forEach(scene -> collectDisplayers(scene));
    }

    private void collectSolutionInfo() {
        if (mu.check.is.unsteady()) {
            INFORMATION.add(getPair("Time", mu.get.solver.physicalTime()));
        }
        INFORMATION.add(getPair("Iteration", mu.get.solver.iteration()));
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
        if (displayer instanceof ScalarDisplayer) {
            ScalarDisplayer sd = (ScalarDisplayer) displayer;
            info.add(getPair(key + " MIN", sd.getScalarDisplayQuantity().getRangeMin()));
            info.add(getPair(key + " MAX", sd.getScalarDisplayQuantity().getRangeMax()));
        } else if (displayer instanceof VectorDisplayer) {
            VectorDisplayer vd = (VectorDisplayer) displayer;
            info.add(getPair(key + " MIN", vd.getVectorDisplayQuantity().getMinMagnitude()));
            info.add(getPair(key + " MAX", vd.getVectorDisplayQuantity().getMaxMagnitude()));
        } else if (displayer instanceof StreamDisplayer) {
            StreamDisplayer sd = (StreamDisplayer) displayer;
            info.add(getPair(key + " MIN", sd.getScalarDisplayQuantity().getRangeMin()));
            info.add(getPair(key + " MAX", sd.getScalarDisplayQuantity().getRangeMax()));
        } else {
            info.add(getPair(key, "N/A"));
        }
        return String.join("\n", info);
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
        return String.format("%s: %s", key, value);
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
