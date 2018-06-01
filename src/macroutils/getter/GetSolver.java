package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.common.AbortFileStoppingCriterion;
import star.common.InnerIterationStoppingCriterion;
import star.common.PhysicalTimeStoppingCriterion;
import star.common.Simulation;
import star.common.SolutionView;
import star.common.Solver;
import star.common.SolverStoppingCriterion;
import star.common.StepStoppingCriterion;
import star.post.SolutionHistory;
import star.post.SolutionHistoryManager;
import star.post.SolutionViewManager;

/**
 * Low-level class for getting solver parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetSolver {

    private macroutils.checker.MainChecker _chk = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetSolver(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets a specific Solver from STAR-CCM+ API.
     *
     * @param <T>    any Class that extends from Solver object in STAR-CCM+.
     * @param solver given Solver Class name.
     * @return The Solver. Casting the variable might be necessary. Returns <b>null</b> if it is not
     *         available.
     */
    public <T extends Solver> T byClass(Class<T> solver) {
        try {
            return _sim.getSolverManager().getSolver(solver);
        } catch (Exception e) {
            _io.say.msgDebug("Getting a Solver by class \"%s\" returned NULL.", solver.getName());
            return null;
        }
    }

    /**
     * Gets the current Iteration.
     *
     * @return The current iteration number.
     */
    public int iteration() {
        return _sim.getSimulationIterator().getCurrentIteration();
    }

    /**
     * Gets the current physical time, in seconds.
     *
     * @return The current physical time.
     */
    public double physicalTime() {
        return _sim.getSolution().getPhysicalTime();
    }

    /**
     * Returns the first match of a Solution History by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The SolutionHistory.
     */
    public SolutionHistory solutionHistory(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt,
                new ArrayList<>(_sim.get(SolutionHistoryManager.class).getObjects()), vo);
    }

    /**
     * Returns the first match of a Solution View by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The SolutionView.
     */
    public SolutionView solutionView(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt,
                new ArrayList<>(_sim.get(SolutionViewManager.class).getObjects()), vo);
    }

    /**
     * Returns the first match of a Stopping Criteria by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The SolverStoppingCriterion object.
     */
    public SolverStoppingCriterion stoppingCriteria(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt,
                new ArrayList<>(_sim.getSolverStoppingCriterionManager().getObjects()), vo);
    }

    /**
     * Gets the abort file stopping criteria for a simulation.
     *
     * @return The AbortFileStoppingCriterion.
     */
    public AbortFileStoppingCriterion stoppingCriteria_AbortFile() {
        return (AbortFileStoppingCriterion) stoppingCriteria("Stop File", false);
    }

    /**
     * Gets the maximum inner iterations stopping criteria for an unsteady simulation.
     *
     * @return The InnerIterationStoppingCriterion.
     */
    public InnerIterationStoppingCriterion stoppingCriteria_MaxInnerIterations() {
        if (!_chk.is.unsteady()) {
            _io.say.msg("Simulation is not Unsteady.");
            return null;
        }
        return (InnerIterationStoppingCriterion) stoppingCriteria("Maximum Inner Iterations",
                false);
    }

    /**
     * Gets the maximum iteration stopping criteria for a simulation.
     *
     * @return The StepStoppingCriterion.
     */
    public StepStoppingCriterion stoppingCriteria_MaxIterations() {
        return (StepStoppingCriterion) stoppingCriteria("Maximum Steps", false);
    }

    /**
     * Gets the maximum physical time stopping criteria for an unsteady simulation.
     *
     * @return The PhysicalTimeStoppingCriterion.
     */
    public PhysicalTimeStoppingCriterion stoppingCriteria_MaxTime() {
        if (!_chk.is.unsteady()) {
            _io.say.msg("Simulation is not Unsteady.");
            return null;
        }
        return (PhysicalTimeStoppingCriterion) stoppingCriteria("Maximum Physical Time", false);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
    }

}
