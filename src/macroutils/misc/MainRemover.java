package macroutils.misc;

import java.util.*;
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
     * <li>Solution and all generated meshes;
     * <li>Plots, Monitors and Reports;
     * <li>Regions;
     * <li>Mesh Operations and Continuas;
     * <li>Parts and 3D-CAD models;
     * <li>Coordinate Systems;
     * <li>Update Events;
     * <li>User Field Functions;
     * <li>Global Parameters;
     * <li>Annotations.
     * </ol>
     */
    public void all() {
        ContinuumManager cm = _sim.getContinuumManager();
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        if (!(cm.isEmpty() && mom.isEmpty())) {
            _clear.solution();
            _clear.meshes();
        }
        allScenes();
        allPlots();
        allMonitors();
        allReports();
        allRegions();
        allMeshOperations();
        allParts();
        allSolidModelParts();
        allContinuas();
        allCoordinateSystems();
        allUpdateEvents();
        allUserFieldFunctions();
        allGlobalParameters();
        allAnnotations();
    }

    private void _removing(String what, int n) {
        String sufix = "s";
        if (n < 2) {
            sufix = "";
        }
        _io.say.msg(true, "Removing %d %s%s...", n, what, sufix);
    }

    /**
     * Removes all Annotations.
     */
    public void allAnnotations() {
        AnnotationManager am = _sim.getAnnotationManager();
        ArrayList<Annotation> aa = new ArrayList();
        for (Annotation a : am.getObjects()) {
            if (a.canDestroy()) {
                aa.add(a);
            }
        }
        if (aa.isEmpty()) {
            return;
        }
        _removing("Annotation", aa.size());
        am.removeObjects(aa);
    }

    /**
     * Removes all Continuas.
     */
    public void allContinuas() {
        ContinuumManager cm = _sim.getContinuumManager();
        if (cm.isEmpty()) {
            return;
        }
        _removing("Continua", cm.getObjects().size());
        cm.removeObjects(cm.getObjects());
    }

    /**
     * Removes all Coordinate Systems.
     */
    public void allCoordinateSystems() {
        LocalCoordinateSystemManager lcsm = _ud.lab0.getLocalCoordinateSystemManager();
        if (lcsm.isEmpty()) {
            return;
        }
        _removing("Coordinate System", lcsm.getObjects().size());
        lcsm.removeObjects(lcsm.getObjects());
    }

    /**
     * Removes all Global Parameters.
     */
    public void allGlobalParameters() {
        GlobalParameterManager gpm = _sim.get(GlobalParameterManager.class);
        if (gpm.isEmpty()) {
            return;
        }
        _removing("Global Parameter", gpm.getObjects().size());
        gpm.removeObjects(gpm.getObjects());
    }

    /**
     * Removes all Mesh Operations.
     */
    public void allMeshOperations() {
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        if (mom.isEmpty()) {
            return;
        }
        _removing("Mesh Operation", mom.getOrderedOperations().size());
        for (MeshOperation mo : mom.getOrderedOperations()) {
            mom.remove(mo);
        }
    }

    /**
     * Removes all Monitors.
     */
    public void allMonitors() {
        MonitorManager mm = _sim.getMonitorManager();
        ArrayList<Monitor> am = new ArrayList();
        for (Monitor m : mm.getObjects()) {
            boolean isRM = m instanceof ReportMonitor;
            boolean isSFAM = m instanceof SingleFieldAnalysisMonitor;
            if (isRM || isSFAM) {
                am.add(m);
            }
        }
        if (am.isEmpty()) {
            return;
        }
        _removing("Monitor", am.size());
        mm.removeObjects(am);
    }

    /**
     * Removes all Parts.
     */
    public void allParts() {
        GeometryPartManager gpm = _sim.get(GeometryPartManager.class);
        if (gpm.isEmpty()) {
            return;
        }
        _removing("Part", gpm.getLeafParts().size());
        gpm.removeObjects(gpm.getLeafParts());
    }

    /**
     * Removes all Plots.
     */
    public void allPlots() {
        PlotManager pm = _sim.getPlotManager();
        if (pm.isEmpty()) {
            return;
        }
        ArrayList<StarPlot> asp = _get.plots.allByREGEX("(?!^Residuals$).*", false);
        _removing("Plot", asp.size());
        pm.removeObjects(asp);
    }

    /**
     * Removes all Regions.
     */
    public void allRegions() {
        RegionManager rm = _sim.getRegionManager();
        if (rm.isEmpty()) {
            return;
        }
        _removing("Region", rm.getRegions().size());
        rm.removeObjects(rm.getRegions());
    }

    /**
     * Removes all Reports.
     */
    public void allReports() {
        ReportManager rm = _sim.getReportManager();
        if (rm.isEmpty()) {
            return;
        }
        _removing("Report", _get.reports.all(false).size());
        rm.removeObjects(_get.reports.all(false));
    }

    /**
     * Removes all Scenes.
     */
    public void allScenes() {
        SceneManager sm = _sim.getSceneManager();
        if (sm.isEmpty()) {
            return;
        }
        _removing("Scene", sm.getScenes().size());
        sm.removeObjects(sm.getScenes());
    }

    /**
     * Removes all 3D-CAD models.
     */
    public void allSolidModelParts() {
        SolidModelManager smm = _sim.get(SolidModelManager.class);
        if (smm.isEmpty()) {
            return;
        }
        _removing("3D-CAD Model", smm.getObjects().size());
        smm.removeObjects(smm.getObjects());
    }

    /**
     * Removes all Update Events.
     */
    public void allUpdateEvents() {
        UpdateEventManager uem = _sim.getUpdateEventManager();
        if (uem.isEmpty()) {
            return;
        }
        _removing("Update Event", uem.getObjects().size());
        uem.removeObjects(uem.getObjects());
    }

    /**
     * Removes all User Field Functions.
     */
    public void allUserFieldFunctions() {
        FieldFunctionManager ffm = _sim.getFieldFunctionManager();
        ArrayList<UserFieldFunction> auff = new ArrayList();
        for (FieldFunction ff : ffm.getObjects()) {
            if (ff instanceof UserFieldFunction) {
                auff.add((UserFieldFunction) ff);
            }
        }
        if (auff.isEmpty()) {
            return;
        }
        _removing("User Field Function", auff.size());
        ffm.removeObjects(auff);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _clear = _mu.clear;
        _get = _mu.get;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.misc.MainClearer _clear = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
