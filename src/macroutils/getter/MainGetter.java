package macroutils.getter;

import macroutils.MacroUtils;
import star.common.Simulation;

/**
 * Main class for get-type methods in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class MainGetter {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainGetter(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        boundaries = new GetBoundaries(m);
        cameras = new GetCameras(m);
        geometries = new GetGeometries(m);
        info = new GetInfos(m);
        mesh = new GetMesh(m);
        monitors = new GetMonitors(m);
        objects = new GetObjects(m);
        partCurves = new GetPartCurves(m);
        partSurfaces = new GetPartSurfaces(m);
        parts = new GetParts(m);
        plots = new GetPlots(m);
        regions = new GetRegions(m);
        reports = new GetReports(m);
        scenes = new GetScenes(m);
        solver = new GetSolver(m);
        strings = new GetStrings(m);
        units = new GetUnits(m);
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Gets the active Simulation being used by MacroUtils.
     *
     * @return the active Simulation object.
     */
    public Simulation activeSimulation() {
        return _sim;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        boundaries.updateInstances();
        cameras.updateInstances();
        geometries.updateInstances();
        info.updateInstances();
        mesh.updateInstances();
        monitors.updateInstances();
        objects.updateInstances();
        partCurves.updateInstances();
        partSurfaces.updateInstances();
        parts.updateInstances();
        plots.updateInstances();
        regions.updateInstances();
        reports.updateInstances();
        scenes.updateInstances();
        solver.updateInstances();
        strings.updateInstances();
        units.updateInstances();
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

    /**
     * This class is responsible for getting Boundaries.
     */
    public GetBoundaries boundaries = null;

    /**
     * This class is responsible for getting Camera Views (VisView).
     */
    public GetCameras cameras = null;

    /**
     * This class is responsible for getting Geometry Parts.
     */
    public GetGeometries geometries = null;

    /**
     * This class is responsible for getting other kind of information in general.
     */
    public GetInfos info = null;

    /**
     * This class is responsible for getting mesh parameters in general.
     */
    public GetMesh mesh = null;

    /**
     * This class is responsible for getting Monitors and related objects.
     */
    public GetMonitors monitors = null;

    /**
     * This class is responsible for getting STAR-CCM+ objects in general.
     */
    public GetObjects objects = null;

    /**
     * This class is responsible for getting Part Curves.
     */
    public GetPartCurves partCurves = null;

    /**
     * This class is responsible for getting Part Surfaces.
     */
    public GetPartSurfaces partSurfaces = null;

    /**
     * This class is responsible for getting Parts in general, such as Derived Parts.
     */
    public GetParts parts = null;

    /**
     * This class is responsible for getting Plots.
     */
    public GetPlots plots = null;

    /**
     * This class is responsible for getting Regions.
     */
    public GetRegions regions = null;

    /**
     * This class is responsible for getting Reports and related objects.
     */
    public GetReports reports = null;

    /**
     * This class is responsible for getting Scenes and related parameters.
     */
    public GetScenes scenes = null;

    /**
     * This class is responsible for getting solver related parameters.
     */
    public GetSolver solver = null;

    /**
     * This class is responsible for getting STAR-CCM+ strings in general.
     */
    public GetStrings strings = null;

    /**
     * This class is responsible for getting STAR-CCM+ units.
     */
    public GetUnits units = null;

}
