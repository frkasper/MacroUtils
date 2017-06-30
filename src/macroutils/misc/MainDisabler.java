package macroutils.misc;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.meshing.*;
import star.metrics.*;
import star.prismmesher.*;
import star.solidmesher.*;

/**
 * Main class for "disabling" methods in MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class MainDisabler {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainDisabler(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    private void _disable(PhysicsContinuum pc, Class clz) {
        Model m = pc.getModelManager().getModel(clz);
        if (pc.getModelManager().has(m)) {
            pc.disableModel(m);
        }
    }

    private void _disabling(String what, ClientServerObject cso, boolean vo) {
        _io.say.action(String.format("Disabling %s", what), vo);
        if (cso != null) {
            _io.say.object(cso, vo);
        }
        _io.say.ok(vo);
    }

    /**
     * Enables the Cell Quality Remediation model.
     *
     * @param pc given PhysicsContinuum.
     * @param vo given verbose option. False will not print anything.
     */
    public void cellQualityRemediation(PhysicsContinuum pc, boolean vo) {
        SolverStoppingCriterion stp = _get.solver.stoppingCriteria("Stop File", false);
        _disabling("Cell Quality Remediation", stp, vo);
        _disable(pc, CellQualityRemediationModel.class);
    }

    /**
     * Disables Prism Layers in a Custom Surface Mesh Control.
     *
     * @param scmc given SurfaceCustomMeshControl.
     * @param vo given verbose option. False will not print anything.
     */
    public void prismsLayers(SurfaceCustomMeshControl scmc, boolean vo) {
        _disabling("Prism Layers", scmc, vo);
        PartsCustomizePrismMesh pcpm = scmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
        pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.DISABLE);
    }

    /**
     * Disables the Prism Layers for all Solid Regions belonging to the Automated Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     */
    public void prismsLayersOnSolids(AutoMeshOperation amo) {
        _io.say.action(String.format("Disabling Prism Layers on Solid Regions"), true);
        _io.say.object(amo, true);
        for (Region r : _sim.getRegionManager().getRegions()) {
            if (!_chk.is.solid(r)) {
                continue;
            }
            ArrayList<GeometryObject> ago = new ArrayList(r.getPartGroup().getObjects());
            SurfaceCustomMeshControl scmc = amo.getCustomMeshControls().createSurfaceControl();
            scmc.getGeometryObjects().setObjects(ago);
            scmc.setPresentationName(String.format("Prism Layers on %s", r.getPresentationName()));
            prismsLayers(scmc, false);
            _io.say.created(scmc, true);
        }
    }

    /**
     * Disables the Normalization Option for all Residual Monitors.
     */
    public void residualMonitorsNormalization() {
        _disabling("Disabling Residual Monitors Normalization", null, true);
        for (Monitor mon : _sim.getMonitorManager().getObjects()) {
            if (mon instanceof ResidualMonitor) {
                ((ResidualMonitor) mon).getNormalizeOption().setSelected(MonitorNormalizeOption.Type.OFF);
            }
        }
    }

    /**
     * Disables the ABORT file Stopping Criteria.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void stoppingCriteriaAbortFile(boolean vo) {
        SolverStoppingCriterion stp = _get.solver.stoppingCriteria("Stop File", false);
        _disabling("Stopping Criteria", stp, vo);
        ((AbortFileStoppingCriterion) stp).setIsUsed(false);
    }

    /**
     * Disables the Maximum Steps Stopping Criteria.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void stoppingCriteriaMaximumSteps(boolean vo) {
        SolverStoppingCriterion stp = _get.solver.stoppingCriteria("Maximum Steps", false);
        _disabling("Stopping Criteria", stp, vo);
        ((StepStoppingCriterion) stp).setIsUsed(false);
    }

    /**
     * Disables the Surface Proximity Refinement.
     *
     * @param mo given Mesh Operation.
     */
    public void surfaceProximityRefinement(MeshOperation mo) {
        _disabling("surfaceProximityRefinement", null, true);
        _get.mesh.remesher(mo, false).setDoProximityRefinement(false);
    }

    /**
     * Disables the Thin Layer Mesher for all Fluid Regions belonging to the Automated Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     */
    public void thinLayersOnFluids(AutoMeshOperation amo) {
        _io.say.action(String.format("Disabling Thin Layers on Fluid Regions"), true);
        _io.say.object(amo, true);
        for (Region r : _sim.getRegionManager().getRegions()) {
            if (_chk.is.solid(r)) {
                continue;
            }
            ArrayList<GeometryObject> ago = new ArrayList(r.getPartGroup().getObjects());
            PartCustomMeshControl pcmc = amo.getCustomMeshControls().createPartControl();
            pcmc.getGeometryObjects().setObjects(ago);
            pcmc.setPresentationName(String.format("Thin Layers on %s", r.getPresentationName()));
            PartsCustomizeThinMesh pctm = pcmc.getCustomConditions().get(PartsCustomizeThinMesh.class);
            pctm.getCustomThinOptions().setSelected(PartsCustomThinOption.Type.DISABLE);
            _io.say.created(pcmc, true);
        }
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
