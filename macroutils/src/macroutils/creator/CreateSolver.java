package macroutils.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.NamedObject;
import star.base.report.Monitor;
import star.common.FieldFunction;
import star.common.MinimumInnerIterationStoppingCriterion;
import star.common.MonitorIterationStoppingCriterion;
import star.common.MonitorIterationStoppingCriterionAsymptoticType;
import star.common.MonitorIterationStoppingCriterionMaxLimitType;
import star.common.MonitorIterationStoppingCriterionMinLimitType;
import star.common.MonitorIterationStoppingCriterionOption;
import star.common.MonitorIterationStoppingCriterionRelativeChangeType;
import star.common.MonitorIterationStoppingCriterionStandardDeviationType;
import star.common.MonitorIterationStoppingCriterionType;
import star.common.Region;
import star.common.ScalarPhysicalQuantity;
import star.common.Simulation;
import star.common.SolutionView;
import star.common.SolverStoppingCriterion;
import star.common.SolverStoppingCriterionLogicalOption;
import star.common.SolverStoppingCriterionManager;
import star.post.RecordedSolutionView;
import star.post.SolutionHistory;
import star.post.SolutionHistoryManager;
import star.post.SolutionViewManager;

/**
 * Low-level class for creating Solver related objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateSolver {

    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private macroutils.setter.MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateSolver(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Solution History using the {@link UserDeclarations#simTitle} name under
     * {@link UserDeclarations#simPath}.
     *
     * @param ano given ArrayList of NamedObjects. E.g.: Regions, Boundaries or Planes.
     * @param aff given Array of Field Functions.
     * @return The SolutionHistory.
     */
    public SolutionHistory solutionHistory(ArrayList<NamedObject> ano,
            ArrayList<FieldFunction> aff) {
        _io.print.action("Creating a Solution History", true);
        File simhf = new File(_ud.simPath, _ud.simTitle + ".simh");
        SolutionHistory sh = _sim.get(SolutionHistoryManager.class)
                .createForFile(simhf.toString(), false);
        sh.getRegions().setObjects(ano);
        sh.setFunctions(new Vector<>(aff));
        ano.forEach(no -> _addInput(sh, no));
        _io.say.objects(new ArrayList<>(sh.getFunctions()), "Functions", true);
        _io.say.objects(new ArrayList<>(sh.getRegions().getParts()), "Regions", true);
        _io.say.objects(new ArrayList<>(sh.getInputs().getParts()), "Inputs", true);
        _io.say.created(sh, true);
        return sh;
    }

    /**
     * Creates a Solution View from a Solution History.
     *
     * @param sh given Solution History.
     * @return The SolutionView.
     */
    public SolutionView solutionView(SolutionHistory sh) {
        _io.print.action("Creating a Solution View History", true);
        SolutionViewManager svm = _sim.get(SolutionViewManager.class);
        if (svm.has(sh.getPresentationName())) {
            _io.say.msg("Already exists. Skipping...");
            return svm.getSolutionView(sh.getPresentationName());
        }
        RecordedSolutionView rsv = sh.createRecordedSolutionView();
        _io.say.created(rsv, true);
        return rsv;
    }

    /**
     * Creates a Stopping Criteria from a Monitor.
     *
     * @param mon     given Monitor.
     * @param type    given type. Choose from {@link macroutils.StaticDeclarations.StopCriteria}.
     * @param val     given value.
     * @param samples how many samples (or iterations)? If using Min/Max/Standard Deviation/Relative
     *                Change, this input is ignored.
     * @return The Stopping Criteria.
     */
    public SolverStoppingCriterion stoppingCriteria(Monitor mon,
            StaticDeclarations.StopCriteria type, double val, int samples) {
        SolverStoppingCriterion ssc;
        _io.print.action("Creating a Solver Stopping Criteria", true);
        _io.say.msg(true, "Criteria: %s.", type.getVar());
        if (type.equals(StaticDeclarations.StopCriteria.MIN_INNER)) {
            ssc = _minimumInner(type, samples);
        } else {
            ssc = _standard(mon, type, val, samples);
        }
        ssc.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.AND);
        ssc.setPresentationName(String.format("%s - %s", type.getVar(), mon.getPresentationName()));
        _io.say.created(ssc, true);
        return ssc;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    private void _addInput(SolutionHistory sh, NamedObject no) {
        if (no instanceof Region) {
            sh.getRegions().add(no);
        } else {
            sh.getInputs().add(no);
        }
    }

    private SolverStoppingCriterion _minimumInner(StaticDeclarations.StopCriteria type,
            int samples) {
        MinimumInnerIterationStoppingCriterion scmit;
        SolverStoppingCriterionManager scm = _sim.getSolverStoppingCriterionManager();
        scmit = scm.createSolverStoppingCriterion(MinimumInnerIterationStoppingCriterion.class,
                type.getVar());
        scmit.setIsUsed(true);
        scmit.setMinimumNumberInnerIterations(samples);
        return scmit;
    }

    private SolverStoppingCriterion _standard(Monitor mon, StaticDeclarations.StopCriteria type,
            double val, int samples) {
        ScalarPhysicalQuantity spq = null;
        MonitorIterationStoppingCriterion ssc = _sim.getSolverStoppingCriterionManager()
                .createIterationStoppingCriterion(mon);
        MonitorIterationStoppingCriterionOption misco = ssc.getCriterionOption();
        MonitorIterationStoppingCriterionType misct = ssc.getCriterionType();
        switch (type) {
            case ASYMPTOTIC:
                MonitorIterationStoppingCriterionAsymptoticType sca;
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.ASYMPTOTIC);
                sca = (MonitorIterationStoppingCriterionAsymptoticType) ssc.getCriterionType();
                spq = sca.getMaxWidth();
                sca.setNumberSamples(samples);
                _io.say.value("Number of Samples", samples, true);
                break;
            case MAX:
                MonitorIterationStoppingCriterionMaxLimitType scmax;
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.MAXIMUM);
                scmax = (MonitorIterationStoppingCriterionMaxLimitType) ssc.getCriterionType();
                spq = scmax.getLimit();
                break;
            case MIN:
                MonitorIterationStoppingCriterionMinLimitType scmin;
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.MINIMUM);
                scmin = (MonitorIterationStoppingCriterionMinLimitType) ssc.getCriterionType();
                spq = scmin.getLimit();
                break;
            case REL_CHANGE:
                MonitorIterationStoppingCriterionRelativeChangeType scrc;
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.RELATIVE_CHANGE);
                scrc = (MonitorIterationStoppingCriterionRelativeChangeType) ssc.getCriterionType();
                _io.say.value("Relative Change", val, true);
                scrc.setRelativeChange(val);
                break;
            case STD_DEV:
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.STANDARD_DEVIATION);
                MonitorIterationStoppingCriterionStandardDeviationType scsd
                        = (MonitorIterationStoppingCriterionStandardDeviationType) misct;
                spq = scsd.getStandardDeviation();
                break;
        }
        if (spq != null) {
            _set.object.physicalQuantity(spq, val, _get.units.fromMonitor(mon),
                    type.getVar(), true);
        }
        return ssc;
    }

}
