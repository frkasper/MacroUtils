package macroutils.misc;

import macroutils.*;
import star.base.report.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;
import star.vis.*;

/**
 * Main class for "removing" STAR-CCM+ objects with MacroUtils. Use with caution.
 *
 * @since October of 2016
 * @author Fabio Kasper
 */
public class MainRemover {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainRemover(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Removes all these objects in the following order:
     * <ol>
     * <li>Clear Solution and all generated meshes;
     * <li>Plots, Monitors and Reports;
     * <li>Regions;
     * <li>Mesh Operations;
     * <li>Parts and 3D-CAD models;
     * <li>Update Events;
     * <li>User Field Functions.
     * </ol>
     */
    public void all() {
        _clear.solution();
        _clear.meshes();
        allScenes();
        allPlots();
        allMonitorReports();
        allReports();
        allRegions();
        allMeshOperations();
        allParts();
        allUpdateEvents();
        allUserFieldFunctions();
    }

    /**
     * Removes all Mesh Operations.
     */
    public void allMeshOperations() {
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        for (MeshOperation mo : mom.getOrderedOperations()) {
            mom.remove(mo);
        }
    }

    /**
     * Removes all Monitors generated from Reports.
     */
    public void allMonitorReports() {
        for (Report r : _get.reports.all(true)) {
            _sim.getMonitorManager().remove(_get.monitors.fromReport(r, true));
        }
    }

    /**
     * Removes all Parts and 3D-CAD models.
     */
    public void allParts() {
        GeometryPartManager gpm = _sim.get(GeometryPartManager.class);
        gpm.removeObjectsSilently(gpm.getLeafParts());
        SolidModelManager smm = _sim.get(SolidModelManager.class);
        smm.removeObjects(smm.getObjects());
    }

    /**
     * Removes all Plots.
     */
    public void allPlots() {
        _sim.getPlotManager().removeObjects(_get.plots.allByREGEX("(?!^Residuals$).*", true));
    }

    /**
     * Removes all Regions.
     */
    public void allRegions() {
        RegionManager rm = _sim.getRegionManager();
        rm.removeObjects(rm.getRegions());
    }

    /**
     * Removes all Reports.
     */
    public void allReports() {
        _sim.getReportManager().removeObjects(_get.reports.all(true));
    }

    /**
     * Removes all Scenes.
     */
    public void allScenes() {
        SceneManager sm = _sim.getSceneManager();
        sm.removeObjects(sm.getScenes());
    }

    /**
     * Removes all Update Events.
     */
    public void allUpdateEvents() {
        UpdateEventManager uem = _sim.getUpdateEventManager();
        uem.removeObjects(uem.getObjects());
    }

    /**
     * Removes all User Field Functions.
     */
    public void allUserFieldFunctions() {
        FieldFunctionManager ffm = _sim.getFieldFunctionManager();
        for (FieldFunction ff : ffm.getObjects()) {
            if (ff instanceof UserFieldFunction) {
                ffm.remove(ff);
            }
        }
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _clear = _mu.clear;
        _get = _mu.get;
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.misc.MainClearer _clear = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
