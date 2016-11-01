package macroutils.creator;

import java.io.*;
import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.post.*;

/**
 * Low-level class for creating Solver related objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateSolver {

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
    public SolutionHistory solutionHistory(ArrayList<NamedObject> ano, ArrayList<FieldFunction> aff) {
        _io.print.action("Creating a Solution History", true);
        File simhf = new File(_ud.simPath, _ud.simTitle + ".simh");
        SolutionHistory sh = _sim.get(SolutionHistoryManager.class).createForFile(simhf.toString(), false);
        sh.getRegions().setObjects(ano);
        sh.setFunctions(new Vector(aff));
        for (NamedObject no : ano) {
            if (no instanceof Region) {
                sh.getRegions().add(no);
                continue;
            }
            sh.getInputs().add(no);
        }
        _io.say.objects(new ArrayList(sh.getFunctions()), "Functions", true);
        _io.say.objects(new ArrayList(sh.getRegions().getParts()), "Regions", true);
        _io.say.objects(new ArrayList(sh.getInputs().getParts()), "Inputs", true);
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
     * @param mon given Monitor.
     * @param type given type. Choose from {@link macroutils.StaticDeclarations.StopCriteria}.
     * @param val given value.
     * @param samples how many samples (or iterations)? If using Min/Max, this input is ignored.
     * @return The Stopping Criteria.
     */
    public SolverStoppingCriterion stoppingCriteria(Monitor mon, StaticDeclarations.StopCriteria type,
            double val, int samples) {
        MonitorIterationStoppingCriterionAsymptoticType sca;
        MonitorIterationStoppingCriterionMaxLimitType scmax;
        MonitorIterationStoppingCriterionMinLimitType scmin;
        MonitorIterationStoppingCriterionStandardDeviationType scsd;
        MinimumInnerIterationStoppingCriterion scmit;
        SolverStoppingCriterionManager scm = _sim.getSolverStoppingCriterionManager();
        MonitorIterationStoppingCriterion isc = mon.createIterationStoppingCriterion();
        SolverStoppingCriterion ssc = isc;
        Units u = _get.units.fromMonitor(mon);
        _io.print.action("Creating a Solver Stopping Criteria", true);
        _io.say.msg(true, "Criteria: %s.", type.getVar());
        MonitorIterationStoppingCriterionOption misco = isc.getCriterionOption();
        MonitorIterationStoppingCriterionType misct = isc.getCriterionType();
        switch (type) {
            case ASYMPTOTIC:
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.ASYMPTOTIC);
                sca = (MonitorIterationStoppingCriterionAsymptoticType) isc.getCriterionType();
                _set.object.physicalQuantity(sca.getMaxWidth(), val, u, type.getVar(), true);
                _io.say.msg(true, "Number of Samples: %d.", samples);
                sca.setNumberSamples(samples);
                break;
            case MAX:
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.MAXIMUM);
                scmax = (MonitorIterationStoppingCriterionMaxLimitType) isc.getCriterionType();
                _set.object.physicalQuantity(scmax.getLimit(), val, u, type.getVar(), true);
                break;
            case MIN:
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.MINIMUM);
                scmin = (MonitorIterationStoppingCriterionMinLimitType) isc.getCriterionType();
                _set.object.physicalQuantity(scmin.getLimit(), val, u, type.getVar(), true);
                break;
            case MIN_INNER:
                scm.remove(isc);
                scmit = scm.createSolverStoppingCriterion(MinimumInnerIterationStoppingCriterion.class, type.getVar());
                ssc = scmit;
                scmit.setIsUsed(true);
                scmit.setMinimumNumberInnerIterations(samples);
                scmit.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.AND);
                break;
            case STDEV:
                misco.setSelected(MonitorIterationStoppingCriterionOption.Type.STANDARD_DEVIATION);
                scsd = (MonitorIterationStoppingCriterionStandardDeviationType) misct;
                _set.object.physicalQuantity(scsd.getStandardDeviation(), val, u, "Standard Deviation", true);
                break;
        }
        isc.getLogicalOption().setSelected(SolverStoppingCriterionLogicalOption.Type.AND);
        isc.setPresentationName(String.format("%s - %s", type.getVar(), mon.getPresentationName()));
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

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
