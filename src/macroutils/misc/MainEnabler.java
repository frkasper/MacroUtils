package macroutils.misc;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.ClientServerObject;
import star.common.AbortFileStoppingCriterion;
import star.common.Boundary;
import star.common.ExpertDriverCoupledSolver;
import star.common.ExpertDriverOption;
import star.common.ExpertInitManager;
import star.common.ExpertInitOption;
import star.common.GridSequencingInit;
import star.common.ImplicitUnsteadyModel;
import star.common.PhysicsContinuum;
import star.common.PisoUnsteadyModel;
import star.common.Simulation;
import star.common.SolverStoppingCriterion;
import star.common.SteadyModel;
import star.common.StepStoppingCriterion;
import star.coupledflow.CoupledImplicitSolver;
import star.meshing.AutoMeshOperation;
import star.meshing.CustomMeshControlManager;
import star.meshing.CustomMeshControlValueManager;
import star.meshing.PartsWakeRefinementValuesManager;
import star.meshing.RelativeOrAbsoluteOption;
import star.meshing.SurfaceCustomMeshControl;
import star.metrics.CellQualityRemediationModel;
import star.trimmer.PartsGrowthRateOption;
import star.trimmer.PartsTrimmerWakeRefinementOption;
import star.trimmer.PartsTrimmerWakeRefinementSet;
import star.trimmer.WRTrimmerAnisotropicSize;
import star.turbulence.synturb.PseudoTurbulenceSpecOption;

/**
 * Main class for "enabling" methods in MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class MainEnabler {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainEnabler(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    private void _enabling(String what, ClientServerObject cso, boolean vo) {
        _enabling(vo, what, cso, null, (Object) null);
    }

    private void _enabling(boolean vo, String what, ClientServerObject cso, String fmt2, Object... args2) {
        _io.say.action(String.format("Enabling %s...", what), vo);
        if (cso != null) {
            _io.say.object(cso, vo);
        }
        _io.say.msg(vo, fmt2, args2);
        _io.say.ok(vo);
    }

    private CoupledImplicitSolver _getCIS() {
        return _sim.getSolverManager().getSolver(CoupledImplicitSolver.class);
    }

    private ExpertInitManager _getEIM() {
        return _getCIS().getExpertInitManager();
    }

    private GridSequencingInit _getGSI() {
        return (GridSequencingInit) _getEIM().getInit();
    }

    private boolean _isCoupled(boolean vo) {
        if (_chk.has.coupledImplicit()) {
            return true;
        }
        _io.say.msg(vo, "Not a Coupled Solver simulation.");
        return false;
    }

    private void _toggleTime(PhysicsContinuum pc, Class dClz, Class eClz, boolean vo) {
        _io.say.action(String.format("Enabling %s analysis", eClz.getName()), vo);
        if (pc == null) {
            _io.say.msg("Physics Continua not provided. Skipping...", vo);
            return;
        }
        _io.say.object(pc, vo);
        pc.disableModel(pc.getModelManager().getModel(dClz));
        pc.enable(eClz);
        _io.say.ok(vo);
    }

    /**
     * Enables the Cell Quality Remediation model.
     *
     * @param pc given PhysicsContinuum.
     * @param vo given verbose option. False will not print anything.
     */
    public void cellQualityRemediation(PhysicsContinuum pc, boolean vo) {
        _enabling("Cell Quality Remediation", pc, vo);
        pc.enable(CellQualityRemediationModel.class);
    }

    /**
     * Enables the Expert Driver for the Coupled Solver.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void expertDriver(boolean vo) {
        _io.say.action("Enabling Expert Driver", vo);
        if (!_isCoupled(vo)) {
            return;
        }
        _getCIS().getSolutionDriverManager().getExpertDriverOption().setSelected(ExpertDriverOption.Type.EXPERT_DRIVER);
        ExpertDriverCoupledSolver ed = (ExpertDriverCoupledSolver) _getCIS().getSolutionDriverManager().getDriver();
        ed.setRelativeResidualCutoff(StaticDeclarations.SMALL_NUMBER);
        if (_getEIM().getExpertInitOption().getSelectedElement() == ExpertInitOption.Type.GRID_SEQ_METHOD) {
            ed.setInitialRampValue(_getGSI().getGSCfl());
            _io.say.msg(vo, "Initial Ramp CFL value set to: %g.", _getGSI().getGSCfl());
        }
        _io.say.ok(vo);
    }

    /**
     * Enables the Expert Initialization for the Coupled Solver also known as Grid Sequencing method.
     *
     * @param cfl0 given initial CFL.
     * @param vo given verbose option. False will not print anything.
     */
    public void expertInitialization(double cfl0, boolean vo) {
        if (!_isCoupled(vo)) {
            return;
        }
        _getEIM().getExpertInitOption().setSelected(ExpertInitOption.Type.GRID_SEQ_METHOD);
        _getGSI().setGSCfl(cfl0);
        _enabling(vo, "Expert Initialization", null, "Initial CFL set to: %g.", _getGSI().getGSCfl());
    }

    /**
     * Convert a Steady State simulation into Implicit Unsteady.
     *
     * @param pc given Physics Continua.
     */
    public void implicitUnsteady(PhysicsContinuum pc) {
        _toggleTime(pc, SteadyModel.class, ImplicitUnsteadyModel.class, true);
    }

    /**
     * Converts an Unsteady to a Steady State simulation .
     *
     * @param pc given Physics Continua.
     */
    public void steadyState(PhysicsContinuum pc) {
        if (_chk.has.PISO()) {
            _toggleTime(pc, PisoUnsteadyModel.class, SteadyModel.class, true);
            return;
        }
        _toggleTime(pc, ImplicitUnsteadyModel.class, SteadyModel.class, true);
    }

    /**
     * Enables the ABORT file Stopping Criteria.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void stoppingCriteriaAbortFile(boolean vo) {
        SolverStoppingCriterion stp = _get.solver.stoppingCriteria("Stop File", false);
        _enabling("Stopping Criteria", stp, vo);
        ((AbortFileStoppingCriterion) stp).setIsUsed(true);
    }

    /**
     * Enables the Maximum Steps Stopping Criteria.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void stoppingCriteriaMaximumSteps(boolean vo) {
        SolverStoppingCriterion stp = _get.solver.stoppingCriteria("Maximum Steps", false);
        _enabling("Stopping Criteria", stp, vo);
        ((StepStoppingCriterion) stp).setIsUsed(true);
    }

    /**
     * Enables Synthetic Eddy Method for a given Boundary. <b>Note:</b> only valid for LES/DES models.
     *
     * @param b given Boundary.
     */
    public void syntheticEddyMethod(Boundary b) {
        PhysicsContinuum pc = b.getRegion().getPhysicsContinuum();
        if (!(_chk.is.LES(pc) || _chk.is.DES(pc))) {
            _io.say.msg("Not a LES or DES model. Cannot enable Synthetic Eddy Method.");
            _io.say.object(b, true);
            return;
        }
        _enabling("Synthetic Eddy Method", b, true);
        PseudoTurbulenceSpecOption ptso = b.getConditions().get(PseudoTurbulenceSpecOption.class);
        ptso.setSelected(PseudoTurbulenceSpecOption.Type.INTENSITY_LENGTH_SCALE);
    }

    /**
     * Creates an Isotropic Wake Refinement in the given Boundary. <b>Only for Trimmer</b>.
     *
     * @param scmc given Surface Mesh Control.
     * @param distance given distance in default units. See {@link UserDeclarations#defUnitLength}.
     * @param angle given spread angle in degrees.
     * @param dir given 3-components direction of the refinement. E.g., in X: {1, 0, 0}.
     * @param relSize given isotropic relative size in (<b>%</b>). E.g.: 50. If any anisotropic value is used isotropic
     * size will be ignored.
     * @param gr given Growth Rate.
     */
    public void trimmerWakeRefinement(SurfaceCustomMeshControl scmc, double distance, double angle,
            double[] dir, double relSize, PartsGrowthRateOption.Type gr) {
        trimmerWakeRefinement(scmc, distance, angle, dir, relSize, null, gr);
    }

    /**
     * Creates an Anisotropic Wake Refinement in the given Boundary. <b>Only for Trimmer</b>.
     *
     * @param scmc given Surface Mesh Control.
     * @param distance given distance in default units. See {@link UserDeclarations#defUnitLength}.
     * @param angle given spread angle in degrees.
     * @param dir given 3-components direction of the refinement. E.g., in X: {1, 0, 0}.
     * @param relSizes given anisotropic 3-component relative sizes in (<b>%</b>). E.g.: {0, 25, 0}. Zeros will be
     * ignored. Use {@link macroutils.StaticDeclarations#COORD0} if convenient.
     * @param gr given Growth Rate.
     */
    public void trimmerWakeRefinement(SurfaceCustomMeshControl scmc, double distance, double angle,
            double[] dir, double[] relSizes, PartsGrowthRateOption.Type gr) {
        trimmerWakeRefinement(scmc, distance, angle, dir, 0., relSizes, gr);
    }

    private void trimmerWakeRefinement(SurfaceCustomMeshControl scmc, double distance, double angle,
            double[] dir, double relSize, double[] relSizes, PartsGrowthRateOption.Type gr) {
        _io.say.action("Enabling Trimmer Wake Refinement", true);
        CustomMeshControlManager cmcm = scmc.getParent();
        AutoMeshOperation amo = (AutoMeshOperation) cmcm.getMeshOperation();
        if (!_chk.has.trimmerMesher(amo)) {
            _io.say.msg("Not a Trimmer Mesh Operation.");
            return;
        }
        _io.say.object(amo, true);
        _io.say.object(scmc, true);
        scmc.getCustomConditions().get(PartsTrimmerWakeRefinementOption.class).setPartsWakeRefinementOption(true);
        CustomMeshControlValueManager cmcvm = scmc.getCustomValues();
        PartsWakeRefinementValuesManager pwrvm = cmcvm.get(PartsWakeRefinementValuesManager.class);
        _set.object.physicalQuantity(pwrvm.getDistance(), distance, _ud.defUnitLength, "Distance", true);
        _set.object.physicalQuantity(pwrvm.getDirection(), dir, null, "Direction", true);
        _set.object.physicalQuantity(pwrvm.getSpreadAngle(), angle, _ud.unit_deg, "Spread Angle", true);
        PartsTrimmerWakeRefinementSet ptwrs = pwrvm.get(PartsTrimmerWakeRefinementSet.class);
        PartsGrowthRateOption pgro = ptwrs.getGrowthRateOption();
        pgro.setSelected(gr);
        _io.say.msg("Growth Rate: " + gr.getPresentationName());
        if (relSize > 0.) {
            _io.say.msg("Isotropic Relative Size (%): " + relSize);
            pwrvm.getRelativeRefSize().setPercentage(relSize);
            _io.say.ok(true);
            return;
        }
        ptwrs.setWRTrimmerAnisotropicSizeOption(true);
        WRTrimmerAnisotropicSize wras = ptwrs.getWRTrimmerAnisotropicSize();
        wras.getRelativeOrAbsoluteOption().setSelected(RelativeOrAbsoluteOption.Type.RELATIVE);
        if (relSizes[0] > 0) {
            _io.say.msg("Relative Size X (%): " + relSizes[0]);
            wras.setXSize(true);
            wras.getRelativeXSize().setPercentage(relSizes[0]);
        }
        if (relSizes[1] > 0) {
            _io.say.msg("Relative Size Y (%): " + relSizes[1]);
            wras.setYSize(true);
            wras.getRelativeYSize().setPercentage(relSizes[1]);
        }
        if (relSizes[2] > 0) {
            _io.say.msg("Relative Size Z (%): " + relSizes[2]);
            wras.setZSize(true);
            wras.getRelativeZSize().setPercentage(relSizes[2]);
        }
        _io.say.ok(true);
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
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
