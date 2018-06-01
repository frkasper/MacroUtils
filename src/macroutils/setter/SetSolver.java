package macroutils.setter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.combustion.PpdfCombustionSolver;
import star.common.ImplicitUnsteadySolver;
import star.common.InnerIterationStoppingCriterion;
import star.common.Model;
import star.common.PartitioningSolver;
import star.common.PhysicalTimeStoppingCriterion;
import star.common.PhysicsContinuum;
import star.common.PisoUnsteadySolver;
import star.common.Region;
import star.common.ScalarPhysicalQuantity;
import star.common.ScalarSolverBase;
import star.common.Simulation;
import star.common.Solver;
import star.common.StepStoppingCriterion;
import star.common.TimeDiscretizationOption;
import star.common.Units;
import star.cosimulation.onedcoupling.OneDSolver;
import star.coupledflow.CoupledFlowModel;
import star.coupledflow.CoupledImplicitSolver;
import star.coupledflow.CoupledSolver;
import star.flow.FlowUpwindOption;
import star.keturb.KeTurbSolver;
import star.keturb.KeTurbViscositySolver;
import star.kwturb.KwTurbSolver;
import star.kwturb.KwTurbViscositySolver;
import star.metrics.GradientsModel;
import star.metrics.LimiterMethodOption;
import star.mixturemultiphase.SegregatedMmpSolver;
import star.multiphase.GranularTemperatureTransportSolver;
import star.rsturb.RsTurbSolver;
import star.rsturb.RsTurbViscositySolver;
import star.segregatedenergy.SegregatedEnergySolver;
import star.segregatedflow.PressureSolver;
import star.segregatedflow.SegregatedFlowModel;
import star.segregatedflow.SegregatedFlowSolver;
import star.segregatedflow.VelocitySolver;
import star.segregatedmultiphase.MultiPhasePressureSolver;
import star.segregatedmultiphase.MultiPhaseVelocitySolver;
import star.segregatedmultiphase.SegregatedMultiPhaseSolver;
import star.segregatedmultiphase.VolumeFractionSolver;
import star.segregatedspecies.SegregatedSpeciesSolver;
import star.sixdof.DofMotionSolver;
import star.sixdof.SixDofSolver;
import star.turbulence.TurbViscositySolver;
import star.vof.VofWaveZoneDistanceSolver;
import star.walldistance.WallDistanceSolver;

/**
 * Low-level class for setting Solver parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetSolver {

    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetSolver(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Sets the CFL number for the Coupled Implicit Solver.
     *
     * @param cfl given CFL number.
     * @param vo  given verbose option. False will not print anything.
     */
    public void CFL(double cfl, boolean vo) {
        _io.say.action("Setting CFL for the Coupled Solver", vo);
        if (!_chk.has.coupledImplicit()) {
            _io.say.msg(vo, "Not a Coupled Solver simulation.");
            return;
        }
        CoupledImplicitSolver cis = _get.solver.byClass(CoupledImplicitSolver.class);
        cis.setCFL(cfl);
        _io.say.value("CFL Number", cis.getCFL(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets aggressive under-relaxation factors (URFs) for the Segregated solvers.
     * <ul>
     * <li> Good for steady state analysis;
     * <li> If one is simulating Unsteady, it will set all URFs to unity but make sure the global
     * CFL &lt; 1 condition is respected. Fluctuations or divergence may occur otherwise.
     * </ul>
     */
    public void aggressiveSettings() {
        _io.say.action("Setting Aggressive Solver Settings", true);
        _ud.urfSolidEnrgy = 0.9999;
        _ud.urfFluidEnrgy = _ud.urfVel = _ud.urfKEps = _ud.urfKOmega = 0.9;
        _ud.urfP = 0.1;
        if (_chk.is.unsteady()) {
            _ud.urfP = 0.4;
        }
        if (_chk.has.VOF()) {
            _ud.urfVel = 0.8;
        }
        if (_hasLES()) {
            _ud.urfP = 0.8;
        } else if (_hasDES()) {
            _ud.urfP = 0.6;
        }
        _updateURFs();
        _updateIterations();
        _io.say.ok(true);
    }

    /**
     * Freezes a specific or many solvers.
     *
     * @param slv given solvers. See {@link macroutils.StaticDeclarations} for all options.
     * @param opt given option.
     */
    public void freeze(StaticDeclarations.Solvers slv, boolean opt) {
        _io.say.action("Freezing Solvers", true);
        String action = "Unfreezed";
        if (opt) {
            action = "Freezed";
        }
        ArrayList<Solver> as = new ArrayList<>(_sim.getSolverManager().getObjects());
        switch (slv) {
            case ALL:
                for (Solver s : as) {
                    _freeze(s, opt);
                }
                break;
            case ALL_BUT_STRATEGIC:
                for (Solver s : as) {
                    if (s instanceof ImplicitUnsteadySolver
                            || s instanceof SixDofSolver
                            || s instanceof DofMotionSolver
                            || s instanceof WallDistanceSolver
                            || s instanceof VofWaveZoneDistanceSolver
                            || s instanceof PartitioningSolver
                            || s instanceof OneDSolver) {
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
        _io.say.value("Limiter",
                gm.getLimiterMethodOption().getSelectedElement().getPresentationName(), true, true);
        _io.say.value("TVB Gradient Limiter", "ON", true, true);
        _io.say.value("Aceptable Field Variation", gm.getAcceptableFieldVariationFactor(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets the maximum number of inner iterations for an Implicit Unsteady simulation.
     *
     * @param n  given number of inner iterations.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void maxInnerIterations(int n, boolean vo) {
        _io.say.action("Setting Maximum Number of Inner Iterations", vo);
        if (!_isImplicitUnsteady(vo)) {
            return;
        }
        InnerIterationStoppingCriterion iisc = _get.solver.stoppingCriteria_MaxInnerIterations();
        iisc.setMaximumNumberInnerIterations(n);
        _io.say.value("Maximum Number of Inner Iterations",
                iisc.getMaximumNumberInnerIterations(), true);
        _io.say.ok(vo);
    }

    /**
     * Set the maximum number of iterations in the simulation.
     *
     * @param n  given number of iterations.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void maxIterations(int n, boolean vo) {
        _io.say.action("Setting Maximum Number of Iterations", vo);
        StepStoppingCriterion ssc = _get.solver.stoppingCriteria_MaxIterations();
        ssc.setMaximumNumberSteps(n);
        _io.say.value("Maximum Iterations", ssc.getMaximumNumberSteps(), true);
        _ud.maxIter = ssc.getMaximumNumberSteps();
        _io.say.ok(vo);
    }

    /**
     * Set the maximum physical time for an unsteady simulation.
     *
     * @param maxTime given maximum physical time.
     * @param u       given Units.
     * @param vo      given verbose option. False will only print necessary data.
     */
    public void maxPhysicalTime(double maxTime, Units u, boolean vo) {
        _io.say.action("Setting Maximum Physical Timestep", vo);
        if (!_isUnsteady(vo)) {
            return;
        }
        PhysicalTimeStoppingCriterion ptsc = _get.solver.stoppingCriteria_MaxTime();
        _set.object.physicalQuantity(ptsc.getMaximumTime(), maxTime, u,
                "Maximum Physical Time", true);
        _io.say.ok(vo);
    }

    /**
     * Sets/Updates all Solver Settings. E.g.: Relaxation Factors.
     */
    public void settings() {
        if (_sim.getSolverManager().isEmpty()) {
            return;
        }
        _io.say.action("Updating Solver Settings", true);
        _updateIterations();
        _updateURFs();
        _io.say.ok(true);
    }

    /**
     * Sets the discretization discretization scheme that is used for evaluating face values for
     * convection and diffusion fluxes.
     *
     * @param pc    given PhysicsContinuum.
     * @param order given discretization scheme, if applicable.
     * @param vo    given verbose option. False will only print necessary data.
     */
    public void spaceDiscretization(PhysicsContinuum pc, FlowUpwindOption.Type order, boolean vo) {
        _io.say.action("Setting Discretization Scheme", vo);
        FlowUpwindOption fuo = _getFlowUpwindOption(pc);
        if (fuo == null) {
            return;
        }
        fuo.setSelected(order);
        _io.say.value("Discretization Scheme", fuo.getSelectedElement().getPresentationName(),
                true, true);
        _io.say.ok(vo);
    }

    /**
     * Sets the discretization order for the Unsteady solver.
     *
     * @param order given time discretization order.
     * @param vo    given verbose option. False will only print necessary data.
     */
    public void timeDiscretization(TimeDiscretizationOption.Type order, boolean vo) {
        if (_chk.has.PISO()) {
            //-- PISO has no discretization order option.
            return;
        }
        _io.say.action("Setting Time Discretization Order", vo);
        TimeDiscretizationOption tdo = _getImplicitUnsteadySolver().getTimeDiscretizationOption();
        tdo.setSelected(order);
        _io.say.value("Time Discretization", order.toString(), true, true);
        _io.say.ok(vo);
    }

    /**
     * Sets a constant physical timestep for the Unsteady solver.
     *
     * @param val given value in default Units. See {@link UserDeclarations#defUnitTime}.
     */
    public void timestep(double val) {
        _setTimestep(val, null, true);
    }

    /**
     * Sets a custom Physical timestep using a definition for the unsteady solver.
     *
     * @param def given timestep definition. The Unit will be reverted to SI (s).
     */
    public void timestep(String def) {
        _setTimestep(0, def, true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    /**
     * Sets Under Relaxation Factors for the Segregated Energy.
     *
     * @param urfFld given URF for the Fluid.
     * @param urfSld given URF for the Solid.
     * @param vo     given verbose option. False will not print anything.
     */
    public void urfSegregatedEnergy(double urfFld, double urfSld, boolean vo) {
        _io.say.action("Setting URFs for the Segregated Energy Solver", vo);
        if (!_chk.has.segregatedEnergy()) {
            _io.say.msg(vo, "Not a Segregated Energy simulation.");
            return;
        }
        SegregatedEnergySolver ses = _get.solver.byClass(SegregatedEnergySolver.class);
        ses.setFluidUrf(urfFld);
        ses.setSolidUrf(urfSld);
        _io.say.value("URF Fluid Energy", ses.getFluidUrf(), true);
        _io.say.value("URF Solid Energy", ses.getSolidUrf(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets Under Relaxation Factors for the Segregated Flow.
     *
     * @param urfVel given URF for the Velocity Solver.
     * @param urfP   given URF for the Pressure Solver.
     * @param vo     given verbose option. False will not print anything.
     */
    public void urfSegregatedFlow(double urfVel, double urfP, boolean vo) {
        _io.say.action("Setting URFs for the Segregated Flow Solver", vo);
        if (!_chk.has.segregatedFlow()) {
            _io.say.msg(vo, "Not a Segregated Flow simulation.");
            return;
        }
        SegregatedFlowSolver sfs = _get.solver.byClass(SegregatedFlowSolver.class);
        VelocitySolver vs = sfs.getVelocitySolver();
        PressureSolver ps = sfs.getPressureSolver();
        vs.setUrf(urfVel);
        ps.setUrf(urfP);
        _io.say.value("URF Velocity", vs.getUrf(), true);
        _io.say.value("URF Pressure", ps.getUrf(), true);
        _io.say.ok(vo);
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

    private FlowUpwindOption _getFlowUpwindOption(PhysicsContinuum pc) {
        for (Model m : pc.getModelManager().getObjects()) {
            if (m instanceof CoupledFlowModel) {
                return ((CoupledFlowModel) m).getUpwindOption();
            }
            if (m instanceof SegregatedFlowModel) {
                return ((SegregatedFlowModel) m).getUpwindOption();
            }
        }
        _io.say.object(pc, true);
        _io.say.msg("Warning! Has no Space Discretization option.");
        return null;
    }

    private ImplicitUnsteadySolver _getImplicitUnsteadySolver() {
        return _get.solver.byClass(ImplicitUnsteadySolver.class);
    }

    private ArrayList<Region> _getRegionsWithPhysicsContinua() {
        ArrayList<Region> ar = new ArrayList<>();
        for (Region r : _get.regions.all(false)) {
            if (r.getPhysicsContinuum() == null) {
                continue;
            }
            ar.add(r);
        }
        return ar;
    }

    private ScalarPhysicalQuantity _getTimeStep() {
        if (_chk.has.PISO()) {
            return _get.solver.byClass(PisoUnsteadySolver.class).getTimeStep();
        }
        return _getImplicitUnsteadySolver().getTimeStep();
    }

    private boolean _hasDES() {
        for (Region r : _getRegionsWithPhysicsContinua()) {
            if (_chk.is.DES(r.getPhysicsContinuum())) {
                return true;
            }
        }
        return false;
    }

    private boolean _hasLES() {
        for (Region r : _getRegionsWithPhysicsContinua()) {
            if (_chk.is.LES(r.getPhysicsContinuum())) {
                return true;
            }
        }
        return false;
    }

    private boolean _isImplicitUnsteady(boolean vo) {
        if (_chk.is.implicitUnsteady()) {
            return true;
        }
        _io.say.msg("Case is not Implicit Unsteady.", vo);
        return false;
    }

    private boolean _isUnsteady(boolean vo) {
        if (_chk.is.unsteady()) {
            return true;
        }
        _io.say.msg("Case is not Unsteady.", vo);
        return false;
    }

    private void _setTimestep(double val, String def, boolean vo) {
        _io.say.action("Setting Physical Timestep", vo);
        if (!_isImplicitUnsteady(vo)) {
            return;
        }
        if (def != null) {
            _set.object.physicalQuantity(_getTimeStep(), def, "Timestep", true);
        } else {
            _set.object.physicalQuantity(_getTimeStep(), val, _ud.defUnitTime, "Timestep", true);
        }
        _io.say.ok(vo);
        //-- Time Discretization.
        if (_ud.trn2ndOrder) {
            timeDiscretization(TimeDiscretizationOption.Type.SECOND_ORDER, vo);
        } else {
            timeDiscretization(TimeDiscretizationOption.Type.FIRST_ORDER, vo);
        }
    }

    private void _updateIterations() {
        if (!_chk.is.unsteady()) {
            maxIterations(_ud.maxIter, false);
            return;
        }
        _get.solver.stoppingCriteria_MaxIterations().setIsUsed(false);
        _get.solver.stoppingCriteria_AbortFile().setInnerIterationCriterion(false);
        _io.say.msg(true, "Maximum Iterations Stopping Criteria disabled.");
        _io.say.msg(true, "ABORT file Inner Iteration Criteria disabled.");
        maxInnerIterations(_ud.trnInnerIter, false);
        _setTimestep(_ud.trnTimestep, null, false);
        _set.solver.maxPhysicalTime(_ud.trnMaxTime, _ud.defUnitTime, false);
    }

    private void _updateURFs() {
        _updateURFs_EMP();
        _updateURFs_SegregatedSolver();
        _updateURFs_OtherSolvers();
    }

    private void _updateURFs_EMP() {
        if (!_chk.has.EMP()) {
            return;
        }
        SegregatedMultiPhaseSolver emps = _get.solver.byClass(SegregatedMultiPhaseSolver.class);
        MultiPhaseVelocitySolver empv = emps.getVelocitySolver();
        MultiPhasePressureSolver empp = emps.getPressureSolver();
        empv.setUrf(_ud.urfPhsCplVel);
        empp.setUrf(_ud.urfP);
        _io.say.value("URF Phase Couple Velocity", empv.getUrf(), true);
        _io.say.value("URF Pressure", empp.getUrf(), true);
    }

    private void _updateURFs_OtherSolvers() {
        for (Solver s : _sim.getSolverManager().getObjects()) {
            double urf = 0.8;
            if (s instanceof GranularTemperatureTransportSolver) {
                urf = _ud.urfGranTemp;
            } else if (s instanceof KeTurbSolver) {
                urf = _ud.urfKEps;
            } else if (s instanceof KwTurbSolver) {
                urf = _ud.urfKOmega;
            } else if (s instanceof PpdfCombustionSolver) {
                urf = _ud.urfPPDFComb;
            } else if (s instanceof RsTurbSolver) {
                urf = _ud.urfRS;
            } else if (s instanceof SegregatedSpeciesSolver) {
                urf = _ud.urfSpecies;
            } else if (s instanceof SegregatedMmpSolver) {
                urf = _ud.urfVOF;
            } else if (s instanceof VolumeFractionSolver) {
                urf = _ud.urfVolFrac;
            } else if (s instanceof RsTurbViscositySolver) {
                urf = _ud.urfRSTurbVisc;
            } else if (s instanceof KeTurbViscositySolver) {
                urf = _ud.urfKEpsTurbVisc;
            } else if (s instanceof KwTurbViscositySolver) {
                urf = _ud.urfKOmegaTurbVisc;
            }
            if (s instanceof TurbViscositySolver) {
                TurbViscositySolver tvs = (TurbViscositySolver) s;
                tvs.setViscosityUrf(urf);
                _io.say.value("URF Turbulence Viscosity", tvs.getViscosityUrf(), true);
                tvs.setMaxTvr(_ud.maxTVR);
                _io.say.value("Maximum Turbulence Viscosity Ratio", tvs.getMaxTvr(), true);
                continue;
            }
            if (!(s instanceof ScalarSolverBase)) {
                continue;
            }
            ScalarSolverBase ssb = (ScalarSolverBase) s;
            ssb.setUrf(urf);
            _io.say.value("URF " + ssb.getPresentationName(), ssb.getUrf(), true);
        }
    }

    private void _updateURFs_SegregatedSolver() {
        CFL(_ud.CFL, false);
        urfSegregatedFlow(_ud.urfVel, _ud.urfP, false);
        if (_ud.urfEnergy > 0.) {
            _io.say.msg("Using the same Energy URF for both Fluids and Solids...");
            _ud.urfFluidEnrgy = _ud.urfSolidEnrgy = _ud.urfEnergy;
        }
        urfSegregatedEnergy(_ud.urfFluidEnrgy, _ud.urfSolidEnrgy, false);
    }

}
