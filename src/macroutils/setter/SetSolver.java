package macroutils.setter;

import java.util.*;
import macroutils.*;
import star.combustion.*;
import star.common.*;
import star.cosimulation.onedcoupling.*;
import star.coupledflow.*;
import star.keturb.*;
import star.kwturb.*;
import star.metrics.*;
import star.multiphase.*;
import star.rsturb.*;
import star.segregatedenergy.*;
import star.segregatedflow.*;
import star.segregatedmultiphase.*;
import star.segregatedspecies.*;
import star.sixdof.DofMotionSolver;
import star.sixdof.SixDofSolver;
import star.turbulence.*;
import star.vof.*;
import star.walldistance.*;

/**
 * Low-level class for setting Solver parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetSolver {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetSolver(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private void _freeze(Solver slv, boolean opt) {
        if (slv instanceof SegregatedFlowSolver) {
            ((SegregatedFlowSolver) slv).setFreezeFlow(opt);
        } else if (slv instanceof CoupledSolver) {
            ((CoupledSolver) slv).setFreezeFlow(opt);
        } else {
            slv.setFrozen(opt);
        }
    }

    private StaticDeclarations.Order _getOrder() {
        if (_ud.trn2ndOrder) {
            return StaticDeclarations.Order.SECOND_ORDER;
        }
        return StaticDeclarations.Order.FIRST_ORDER;
    }

    private CoupledImplicitSolver _getCIS() {
        return _sim.getSolverManager().getSolver(CoupledImplicitSolver.class);
    }

    private ImplicitUnsteadySolver _getIUS() {
        return (ImplicitUnsteadySolver) _get.solver.byClass(ImplicitUnsteadySolver.class);
    }

    private ScalarPhysicalQuantity _getTimeStep() {
        if (_chk.has.PISO()) {
            return ((PisoUnsteadySolver) _get.solver.byClass(PisoUnsteadySolver.class)).getTimeStep();
        }
        return _getIUS().getTimeStep();
    }

    /**
     * Sets aggressive under-relaxation factors (URFs) for the Segregated solvers.
     * <ul>
     * <li> Good for steady state analysis;
     * <li> If one is simulating Unsteady, it will set all URFs to unity but make sure the global CFL &lt; 1 condition
     * is respected. Fluctuations or divergence may occur otherwise.
     * </ul>
     */
    public void aggressiveURFs() {
        _io.say.msg("Setting Aggressive Under-Relaxation Factors...");
        _ud.urfSolidEnrgy = 1.0;
        _ud.urfFluidEnrgy = _ud.urfVel = _ud.urfKEps = _ud.urfKOmega = 0.9;
        _ud.urfP = 0.1;
        if (_chk.is.unsteady()) {
            _ud.urfP = 0.4;
        }
        if (_chk.has.VOF()) {
            _ud.urfVel = 0.8;
        }
//    if (_chk.has.LES()) {
//        _ud.urfVel = _ud.urfEnergy = _ud.urfKEps = _ud.urfKOmega = _ud.urfP = _ud.urfVOF = 1.0;
//    }
        settings();
    }

    /**
     * Sets the CFL number for the Coupled Implicit Solver.
     *
     * @param cfl given CFL number.
     * @param vo given verbose option. False will not print anything.
     */
    public void CFL(double cfl, boolean vo) {
        CFL(cfl, vo, false);
    }

    private void CFL(double cfl, boolean vo, boolean vo2) {
        _io.say.action("Setting CFL for the Coupled Solver", vo);
        _io.say.msg(vo, "CFL: %g.", cfl);
        if (!_chk.has.coupledImplicit()) {
            _io.say.msg(vo, "Not a Coupled Solver simulation.");
            return;
        }
        _getCIS().setCFL(cfl);
        _io.say.ok(vo);
        _io.say.msg(vo2, "CFL: %g.", _ud.CFL);
    }

    /**
     * Freezes a specific or many solvers.
     *
     * @param slv given solvers. See {@link StaticDeclarations} for all options.
     * @param opt given option.
     */
    public void freeze(StaticDeclarations.Solvers slv, boolean opt) {
        _io.say.action("Freezing Solvers", true);
        String action = "Unfreezed";
        if (opt) {
            action = "Freezed";
        }
        ArrayList<Solver> as = new ArrayList(_sim.getSolverManager().getObjects());
        switch (slv) {
            case ALL:
                for (Solver s : as) {
                    _freeze(s, opt);
                }
                break;
            case ALL_BUT_STRATEGIC:
                for (Solver s : as) {
                    boolean b1 = s instanceof ImplicitUnsteadySolver;
                    boolean b2 = s instanceof SixDofSolver;
                    boolean b3 = s instanceof DofMotionSolver;
                    boolean b4 = s instanceof WallDistanceSolver;
                    boolean b5 = s instanceof DampingBoundaryDistanceSolver;
                    boolean b6 = s instanceof PartitioningSolver;
                    boolean b7 = s instanceof OneDSolver;
                    if (b1 || b2 || b3 || b4 || b5 || b6 || b7) {
                        continue;
                    }
                    _freeze(s, opt);
                }
                break;
        }
        _io.say.msg(true, "%s: %s.", action, slv.toString());
        _io.say.ok(true);
    }

    /**
     * Sets a low numerical dissipation for the solver. This method will:
     * <ul>
     * <li>change the dissipation limiter to MinMod;</li>
     * <li>enable TVM Gradient Limiting;</li>
     * <li>change Acceptable Field Variation to 0.1.</li>
     * </ul>
     *
     * @param pc given PhysicsContinuum.
     * @param vo given verbose option. False will not print anything.
     */
    public void lowNumericalDissipation(PhysicsContinuum pc, boolean vo) {
        _io.say.action("Setting Low Numerical Dissipation", vo);
        GradientsModel gm = pc.getModelManager().getModel(GradientsModel.class);
        gm.getLimiterMethodOption().setSelected(LimiterMethodOption.Type.MINMOD);
        gm.setUseTVBGradientLimiting(true);
        gm.setAcceptableFieldVariationFactor(0.1);
        _io.say.msg(vo, "Limiter Method: \"%s\".",
                gm.getLimiterMethodOption().getSelectedElement().getPresentationName());
        _io.say.msg(vo, "TVB Gradient Limiter: \"ON\".");
        _io.say.msg(vo, "Acceptable Field Variation: %g.", gm.getAcceptableFieldVariationFactor());
        _io.say.ok(vo);
    }

    /**
     * Sets the maximum number of inner iterations for an Implicit Unsteady simulation.
     *
     * @param n given number of inner iterations.
     * @param vo given verbose option. False will not print anything.
     */
    public void maxInnerIterations(int n, boolean vo) {
        _io.say.action("Setting Maximum Number of Inner Iterations", vo);
        _io.say.msg(vo, "Max Inner Iterations: %d.", n);
        _get.solver.stoppingCriteria_MaxInnerIterations().setMaximumNumberInnerIterations(n);
        _io.say.ok(vo);
    }

    /**
     * Set the maximum number of iterations in the simulation.
     *
     * @param n given number of iterations.
     * @param vo given verbose option. False will not print anything.
     */
    public void maxIterations(int n, boolean vo) {
        _io.say.action("Setting Maximum Number of Iterations", vo);
        _io.say.msg(vo, "Max Iterations: %d.", n);
        _ud.maxIter = n;
        _get.solver.stoppingCriteria_MaxIterations().setMaximumNumberSteps(n);
        _io.say.ok(vo);
    }

    /**
     * Set the maximum physical time for an unsteady simulation.
     *
     * @param maxTime given maximum physical time.
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void maxPhysicalTime(double maxTime, Units u, boolean vo) {
        _io.say.action("Setting Maximum Physical Timestep", vo);
        PhysicalTimeStoppingCriterion sc = _get.solver.stoppingCriteria_MaxTime();
        _set.object.physicalQuantity(sc.getMaximumTime(), maxTime, null, u, "Maximum Physical Time", vo);
        _io.say.ok(vo);
    }

    /**
     * Sets the discretization order for the Unsteady solver.
     *
     * @param order given discretization order. See {@link StaticDeclarations.Order}.
     * @param vo given verbose option. False will not print anything.
     */
    public void timeDiscretization(StaticDeclarations.Order order, boolean vo) {
        if (_chk.has.PISO()) {
            //-- PISO has no discretization order option.
            return;
        }
        _io.say.action("Setting Time Discretization Order", vo);
        TimeDiscretizationOption tdo = _getIUS().getTimeDiscretizationOption();
        switch (order) {
            case FIRST_ORDER:
                tdo.setSelected(TimeDiscretizationOption.Type.FIRST_ORDER);
                break;
            case SECOND_ORDER:
                tdo.setSelected(TimeDiscretizationOption.Type.SECOND_ORDER);
                break;
        }
        _io.say.msg(vo, "Time Discretization: %s.", order.toString());
        _io.say.ok(vo);
    }

    /**
     * Sets a constant physical timestep for the Unsteady solver.
     *
     * @param val given value in default Units. See {@link UserDeclarations#defUnitTime}.
     */
    public void timestep(double val) {
        timestep(val, null, true);
    }

    /**
     * Sets a custom Physical timestep using a definition for the unsteady solver.
     *
     * @param def given timestep definition. The Unit will be reverted to SI (s).
     */
    public void timestep(String def) {
        timestep(0, def, true);
    }

    private void timestep(double val, String def, boolean vo) {
        _io.say.action("Setting Physical Timestep", vo);
        if (!_chk.is.unsteady()) {
            _tmpl.print.nothingChanged("Case is not Unsteady.", vo);
            return;
        }
        _set.object.physicalQuantity(_getTimeStep(), val, def, _ud.defUnitTime, "Timestep", vo);
        _io.say.ok(vo);
        //-- Time Discretization.
        timeDiscretization(_getOrder(), vo);
    }

    /**
     * Sets/Updates all Solver Settings. E.g.: Relaxation Factors.
     */
    public void settings() {
        settings(true);
    }

    private void settings(boolean vo) {
        if (_sim.getSolverManager().isEmpty()) {
            return;
        }
        _io.say.action("Updating Solver Settings", vo);
        if (_chk.is.unsteady()) {
            _get.solver.stoppingCriteria_MaxIterations().setIsUsed(false);
            _get.solver.stoppingCriteria_AbortFile().setInnerIterationCriterion(false);
            if (_chk.is.implicitUnsteady()) {
                maxInnerIterations(_ud.trnInnerIter, false);
                timestep(_ud.trnTimestep, null, false);
                _io.say.msg(vo, "Time Discretization: %s.", _getOrder().toString());
                _io.say.msg(vo, "Maximum Inner Iterations: %d.", _ud.trnInnerIter);
                _io.say.msg(vo, "Physical Timestep: %g %s", _ud.trnTimestep, _ud.defUnitTime.toString());
            }
            _set.solver.maxPhysicalTime(_ud.trnMaxTime, _ud.defUnitTime, false);
            _io.say.msg(vo, "Maximum Physical Time: %g %s", _ud.trnMaxTime, _ud.defUnitTime.toString());
        } else {
            maxIterations(_ud.maxIter, false);
            _io.say.msg(vo, "Maximum Number of Iterations: %d", _ud.maxIter);
        }
        CFL(_ud.CFL, false, vo);
        urfSegregatedFlow(_ud.urfVel, _ud.urfP, false, true);
        if (_ud.urfEnergy > 0.) {
            _io.say.msg("Using the same Energy URF for both Fluids and Solids...");
            _ud.urfFluidEnrgy = _ud.urfSolidEnrgy = _ud.urfEnergy;
        }
        urfSegregatedEnergy(_ud.urfFluidEnrgy, _ud.urfSolidEnrgy, false, true);
        if (_chk.has.EMP()) {
            Solver s = _get.solver.byClass(SegregatedMultiPhaseSolver.class);
            SegregatedMultiPhaseSolver emps = (SegregatedMultiPhaseSolver) s;
            MultiPhaseVelocitySolver empv = emps.getVelocitySolver();
            MultiPhasePressureSolver empp = emps.getPressureSolver();
            empv.setUrf(_ud.urfPhsCplVel);
            empp.setUrf(_ud.urfP);
            _io.say.msg(vo, "URF Phase Couple Velocity: %g", _ud.urfPhsCplVel);
            _io.say.msg(vo, "URF Pressure: %g", _ud.urfP);
        }
        settings(GranularTemperatureTransportSolver.class, "Granular Temperature", _ud.urfGranTemp, vo);
        settings(KeTurbSolver.class, "K-Epsilon Turbulence", _ud.urfKEps, vo);
        settings(KeTurbViscositySolver.class, "K-Epsilon Turbulent Viscosity", _ud.urfKEpsTurbVisc, vo);
        settings(KwTurbSolver.class, "K-Omega Turbulence", _ud.urfKOmega, vo);
        settings(KwTurbViscositySolver.class, "K-Omega Turbulent Viscosity", _ud.urfKOmegaTurbVisc, vo);
        settings(PpdfCombustionSolver.class, "PPDF Combustion", _ud.urfPPDFComb, vo);
        settings(RsTurbSolver.class, "Reynolds Stress Turbulence", _ud.urfRS, vo);
        settings(RsTurbViscositySolver.class, "Reynolds Stress Turbulent Viscosity", _ud.urfRSTurbVisc, vo);
        settings(SegregatedSpeciesSolver.class, "Segregated Species", _ud.urfSpecies, vo);
        settings(SegregatedVofSolver.class, "Segregated VOF", _ud.urfVOF, vo);
        settings(VolumeFractionSolver.class, "Volume Fraction", _ud.urfVolFrac, vo);
        _io.say.ok(vo);
    }

    private void settings(Class cl, String hasStr, double val, boolean vo) {
        Solver slv = _get.solver.byClass(cl);
        boolean b1 = slv == null;
        boolean b2 = !_sim.getSolverManager().has(hasStr);
        if (b1 || b2) {
            return;
        }
        if (hasStr.endsWith("Turbulent Viscosity")) {
            _io.say.msg(vo, "URF %s: %g", hasStr, val);
            _io.say.msg(vo, "Maximum Ratio: %g", _ud.maxTVR);
            TurbViscositySolver tvs = (TurbViscositySolver) slv;
            tvs.setViscosityUrf(val);
            tvs.setMaxTvr(_ud.maxTVR);
            return;
        }
        ScalarSolverBase ssb = (ScalarSolverBase) slv;
        _io.say.msg(vo, "URF %s: %g", hasStr, val);
        ssb.setUrf(val);
    }

    /**
     * Sets Under Relaxation Factors for the Segregated Energy.
     *
     * @param urfFld given URF for the Fluid.
     * @param urfSld given URF for the Solid.
     * @param vo given verbose option. False will not print anything.
     */
    public void urfSegregatedEnergy(double urfFld, double urfSld, boolean vo) {
        urfSegregatedEnergy(urfFld, urfSld, vo, false);
    }

    private void urfSegregatedEnergy(double urfFld, double urfSld, boolean vo, boolean vo2) {
        _io.say.action("Setting URFs for the Segregated Energy Solver", vo);
        if (!_chk.has.segregatedEnergy()) {
            _io.say.msg(vo, "Not a Segregated Energy simulation.");
            return;
        }
        SegregatedEnergySolver ses = ((SegregatedEnergySolver) _get.solver.byClass(SegregatedEnergySolver.class));
        ses.setFluidUrf(urfFld);
        ses.setSolidUrf(urfSld);
        _io.say.msg(vo, "URF Fluid Energy: %g.", ses.getFluidUrf());
        _io.say.msg(vo, "URF Solid Energy: %g.", ses.getSolidUrf());
        _io.say.ok(vo);
        _io.say.msg(vo2, "URF Fluid Energy: %g.", ses.getFluidUrf());
        _io.say.msg(vo2, "URF Solid Energy: %g.", ses.getSolidUrf());
    }

    /**
     * Sets Under Relaxation Factors for the Segregated Flow.
     *
     * @param urfVel given URF for the Velocity Solver.
     * @param urfP given URF for the Pressure Solver.
     * @param vo given verbose option. False will not print anything.
     */
    public void urfSegregatedFlow(double urfVel, double urfP, boolean vo) {
        urfSegregatedFlow(urfVel, urfP, vo, false);
    }

    private void urfSegregatedFlow(double urfVel, double urfP, boolean vo, boolean vo2) {
        _io.say.action("Setting URFs for the Segregated Flow Solver", vo);
        if (!_chk.has.segregatedFlow()) {
            _io.say.msg(vo, "Not a Segregated Flow simulation.");
            return;
        }
        SegregatedFlowSolver sfs = (SegregatedFlowSolver) _get.solver.byClass(SegregatedFlowSolver.class);
        VelocitySolver vs = sfs.getVelocitySolver();
        PressureSolver ps = sfs.getPressureSolver();
        vs.setUrf(urfVel);
        ps.setUrf(urfP);
        _io.say.msg(vo, "URF Velocity: %g.", vs.getUrf());
        _io.say.msg(vo, "URF Pressure: %g.", ps.getUrf());
        _io.say.ok(vo);
        _io.say.msg(vo2, "URF Velocity: %g.", vs.getUrf());
        _io.say.msg(vo2, "URF Pressure: %g.", ps.getUrf());
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _chk = _mu.check;
        _get = _mu.get;
        _set = _mu.set;
        _tmpl = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
