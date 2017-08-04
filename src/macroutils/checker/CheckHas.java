package macroutils.checker;

import macroutils.MacroUtils;
import star.common.Simulation;
import star.meshing.AutoMeshOperation;

/**
 * Low-level class for has-type methods in MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class CheckHas {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CheckHas(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Does the simulation have the Coupled Implicit Solver?
     *
     * @return True or False.
     */
    public boolean coupledImplicit() {
        return _sim.getSolverManager().has("Coupled Implicit");
    }

    /**
     * Does the simulation have the Eulerian Multiphase Solver (EMP)?
     *
     * @return True or False.
     */
    public boolean EMP() {
        return _sim.getSolverManager().has("Multiphase Segregated Flow");
    }

    /**
     * Does the simulation have the PISO Unsteady Solver?
     *
     * @return True or False.
     */
    public boolean PISO() {
        return _sim.getSolverManager().has("PISO Unsteady");
    }

    /**
     * Does the Automated Mesh Operation have the Polyhedral Mesher?
     *
     * @param amo given Automated Mesh Operation.
     * @return True or False.
     */
    public boolean polyMesher(AutoMeshOperation amo) {
        return amo.getMeshers().has("Polyhedral Mesher");
    }

    /**
     * Does the Automated Mesh Operation have the Prism Layer Mesher?
     *
     * @param amo given Automated Mesh Operation.
     * @return True or False.
     */
    public boolean prismLayerMesher(AutoMeshOperation amo) {
        return amo.getMeshers().has("Prism Layer Mesher");
    }

    /**
     * Does the Automated Mesh Operation have the Surface Remesher?
     *
     * @param amo given Automated Mesh Operation.
     * @return True or False.
     */
    public boolean remesher(AutoMeshOperation amo) {
        return amo.getMeshers().has("Surface Remesher");
    }

    /**
     * Does the simulation have the Segregated Energy Solver?
     *
     * @return True or False.
     */
    public boolean segregatedEnergy() {
        return _sim.getSolverManager().has("Segregated Energy");
    }

    /**
     * Does the simulation have the Segregated Flow Solver?
     *
     * @return True or False.
     */
    public boolean segregatedFlow() {
        return _sim.getSolverManager().has("Segregated Flow");
    }

    /**
     * Does the Simulation have a Solution, i.e., Fields were Initialized?
     *
     * @return True or False.
     */
    public boolean solution() {
        return _sim.getSolution().isInitialized() || _get.solver.iteration() > 0;
    }

    /**
     * Does the Automated Mesh Operation have the Thin Layer Mesher?
     *
     * @param amo given Automated Mesh Operation.
     * @return True or False.
     */
    public boolean thinMesher(AutoMeshOperation amo) {
        return amo.getMeshers().has("Thin Mesher");
    }

    /**
     * Does the Automated Mesh Operation have the Trimmer Mesher?
     *
     * @param amo given Automated Mesh Operation.
     * @return True or False.
     */
    public boolean trimmerMesher(AutoMeshOperation amo) {
        return amo.getMeshers().has("Trimmed Cell Mesher");
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
    }

    /**
     * Does the simulation have the Volume of Fluid Solver (VOF)?
     *
     * @return True or False.
     */
    public boolean VOF() {
        return _sim.getSolverManager().has("Segregated VOF");
    }

    /**
     * Does the Simulation have a Valid Volume Mesh?
     *
     * @return True or False.
     */
    public boolean volumeMesh() {
        return _sim.getRepresentationManager().has("Volume Mesh");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private Simulation _sim = null;

}
