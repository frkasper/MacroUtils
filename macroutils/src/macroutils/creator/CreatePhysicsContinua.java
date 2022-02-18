package macroutils.creator;

import java.util.ArrayList;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.CompositeArrayProfileMethod;
import star.common.EulerianPhase;
import star.common.ExplicitUnsteadyModel;
import star.common.FieldFunction;
import star.common.FunctionScalarProfileMethod;
import star.common.FunctionVectorProfileMethod;
import star.common.ImplicitUnsteadyModel;
import star.common.PhysicsContinuum;
import star.common.ScalarProfile;
import star.common.Simulation;
import star.common.SteadyModel;
import star.coupledflow.CoupledEnergyModel;
import star.coupledflow.CoupledFlowModel;
import star.coupledflow.CoupledSolidEnergyModel;
import star.flow.ConstantDensityModel;
import star.flow.ConstantDensityProperty;
import star.flow.FlowUpwindOption;
import star.flow.GravityModel;
import star.flow.IdealGasModel;
import star.flow.InitialPressureProfile;
import star.flow.InviscidModel;
import star.flow.LaminarModel;
import star.flow.VelocityProfile;
import star.flow.VolumeFractionProfile;
import star.keturb.KEpsilonTurbulence;
import star.keturb.KeHighYplusWallTreatment;
import star.keturb.KeTwoLayerAllYplusWallTreatment;
import star.keturb.RkeTurbModel;
import star.keturb.RkeTwoLayerTurbModel;
import star.keturb.SkeTurbModel;
import star.kwturb.KOmegaTurbulence;
import star.kwturb.KwAllYplusWallTreatment;
import star.kwturb.KwTurbFreeShearOption;
import star.kwturb.KwTurbRealizabilityOption;
import star.kwturb.SkwTurbModel;
import star.kwturb.SstKwTurbDesModel;
import star.kwturb.SstKwTurbModel;
import star.material.EulerianMultiPhaseModel;
import star.material.SingleComponentGasModel;
import star.material.SingleComponentLiquidModel;
import star.material.SinglePhaseGasModel;
import star.material.SinglePhaseLiquidModel;
import star.material.SolidModel;
import star.metrics.ThreeDimensionalModel;
import star.metrics.TwoDimensionalModel;
import star.mixturemultiphase.SegregatedVolumeFluxBasedFlowModel;
import star.segregatedenergy.SegregatedFluidTemperatureModel;
import star.segregatedenergy.SegregatedSolidEnergyModel;
import star.segregatedflow.SegregatedFlowModel;
import star.turbulence.DesFormulationOption;
import star.turbulence.DesTurbulenceModel;
import star.turbulence.RansTurbulenceModel;
import star.turbulence.TurbulentModel;
import star.vof.FlatVofWave;
import star.vof.SegregatedVofModel;
import star.vof.VofWaveModel;

/**
 * Low-level class for creating Physics Continua with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreatePhysicsContinua {

    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private boolean _isOK = true;
    private MacroUtils _mu = null;
    private PhysicsContinuum _pc = null;
    private ArrayList<String> _s = new ArrayList<>();
    private macroutils.setter.MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.misc.MainUpdater _upd = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreatePhysicsContinua(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Flat VOF Wave. Useful for Initial Conditions using the VOF model.
     *
     * @param pc      given Physics Continua.
     * @param ptLvl   given Point on Level. A 3-components array in Length dimensions. See
     *                {@link UserDeclarations#defUnitLength}.
     * @param vertDir given Vertical Direction. A 3-components array.
     * @param curr    given Current. A 3-component array in Velocity dimensions.
     *                {@link UserDeclarations#defUnitVel}.
     * @param wind    given Wind. A 3-component array in Velocity dimensions.
     *                {@link UserDeclarations#defUnitVel}.
     * @return The FlatVofWave.
     */
    public FlatVofWave createWave(PhysicsContinuum pc, double[] ptLvl, double[] vertDir,
            double[] curr, double[] wind) {
        _io.say.action("Creating a Flat VOF Wave", true);
        VofWaveModel wm = pc.getModelManager().getModel(VofWaveModel.class);
        FlatVofWave fvw = wm.getVofWaveManager().createVofWave(FlatVofWave.class, "FlatVofWave");
        _set.object.physicalQuantity(fvw.getPointOnLevel(), ptLvl, _ud.defUnitLength,
                "Point On Level", true);
        _set.object.physicalQuantity(fvw.getVerticalDirection(), vertDir, null,
                "Vertical Direction", true);
        _set.object.physicalQuantity(fvw.getCurrent(), curr, _ud.defUnitVel, "Current", true);
        _set.object.physicalQuantity(fvw.getWind(), wind, _ud.defUnitVel, "Wind", true);
        //--
        //-- Try to Set Light and Heavy Fluids Automatically
        //-- Match the data defined in the Physics Continua
        _io.say.msg("Matching Eulerian Phase Densities...");
        ArrayList<Double> dens = new ArrayList<>(
                _getPhases(pc).stream()
                        .map(ep -> _getDensity(ep))
                        .collect(Collectors.toList()));
        _set.object.physicalQuantity(fvw.getLightFluidDensity(), Math.min(dens.get(0), dens.get(1)),
                _ud.unit_kgpm3, "Light Fluid Density", true);
        _set.object.physicalQuantity(fvw.getHeavyFluidDensity(), Math.max(dens.get(0), dens.get(1)),
                _ud.unit_kgpm3, "Heavy Fluid Density", true);
        //--
        //-- Apply Wave Initial Conditions
        FieldFunction ffp = _get.objects.fieldFunction("Hydrostatic Pressure.*", true);
        FieldFunction ffvfh = _get.objects.fieldFunction("Volume Fraction.*Heavy.*", true);
        FieldFunction ffvfl = _get.objects.fieldFunction("Volume Fraction.*Light.*", true);
        FieldFunction ffvel = _get.objects.fieldFunction("Velocity of .*Wave.*", true);
        InitialPressureProfile ipp = pc.getInitialConditions().get(InitialPressureProfile.class);
        ipp.setMethod(FunctionScalarProfileMethod.class);
        ipp.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(ffp);
        VolumeFractionProfile vfp = pc.getInitialConditions().get(VolumeFractionProfile.class);
        vfp.setMethod(CompositeArrayProfileMethod.class);
        ScalarProfile sp0 = vfp.getMethod(CompositeArrayProfileMethod.class).getProfile(0);
        sp0.setMethod(FunctionScalarProfileMethod.class);
        ScalarProfile sp1 = vfp.getMethod(CompositeArrayProfileMethod.class).getProfile(1);
        sp1.setMethod(FunctionScalarProfileMethod.class);
        sp0.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(ffvfh);
        sp1.getMethod(FunctionScalarProfileMethod.class).setFieldFunction(ffvfl);
        VelocityProfile vpp = pc.getInitialConditions().get(VelocityProfile.class);
        vpp.setMethod(FunctionVectorProfileMethod.class);
        vpp.getMethod(FunctionVectorProfileMethod.class).setFieldFunction(ffvel);
        //--
        _io.say.created(fvw, true);
        return fvw;
    }

    /**
     * Creates a Physics Continua based on models choices. See {@link macroutils.StaticDeclarations}
     * for all options.
     *
     * @param spc   given Space model.
     * @param time  given Time model.
     * @param mat   given Material model..
     * @param slv   given Solver strategy.
     * @param eos   given Equation of State.
     * @param enrgy given Energy model.
     * @param visc  given Viscous Regime.
     * @return The New Physics Continua.
     */
    public PhysicsContinuum generic(StaticDeclarations.Space spc, StaticDeclarations.Time time,
            StaticDeclarations.Material mat, StaticDeclarations.Solver slv,
            StaticDeclarations.Density eos, StaticDeclarations.Energy enrgy,
            StaticDeclarations.Viscous visc) {
        _io.say.action("Creating Physics Continua", true);
        _resetData();
        //-- Follow the order below
        _setSpace(spc);
        _setTime(time);
        _setMaterial(mat);
        _setSolver(slv);
        _setEquationOfState(eos);
        _setEnergy(enrgy);
        _setViscous(visc);
        if (!_isOK) {
            return null;
        }
        if (time.equals(StaticDeclarations.Time.EXPLICIT_UNSTEADY)) {
            _set.solver.lowNumericalDissipation(_pc, false);
            _set.solver.spaceDiscretization(_pc, FlowUpwindOption.Type.MUSCL_3RD_ORDER, false);
        }
        //--
        _set.physics.initialConditions(_pc);
        _set.physics.gravity(_pc, _ud.gravity);
        _pc.setPresentationName(_getPCName());
        _io.say.msg("");
        _io.say.created(_pc, true);
        _upd.allUnits(false);
        _set.solver.settings();
        return _pc;
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
        _upd = _mu.update;
    }

    private void _createPhasesAirWater() {
        _upd.allUnits(false);
        EulerianMultiPhaseModel empm = _pc.getModelManager()
                .getModel(EulerianMultiPhaseModel.class);
        for (int i = 1; i <= 2; i++) {
            EulerianPhase ep = empm.createPhase();
            if (i == 1) {
                ep.setPresentationName("Water");
                ep.enable(SinglePhaseLiquidModel.class);
            } else {
                ep.setPresentationName("Air");
                ep.enable(SinglePhaseGasModel.class);
            }
            ep.enable(ConstantDensityModel.class);
        }
    }

    private void _enable(ArrayList<Class> aclz) {
        aclz.forEach(cl -> _pc.enable(cl));
    }

    private void _enableCDM() {
        _pc.enable(ConstantDensityModel.class);
        _s.add("Incompressible");
    }

    private void _enableDES_SST() {
        _pc.enable(TurbulentModel.class);
        _pc.enable(DesTurbulenceModel.class);
        _pc.enable(SstKwTurbDesModel.class);
        _pc.enable(KwAllYplusWallTreatment.class);
    }

    private void _enableKE() {
        _pc.enable(TurbulentModel.class);
        _pc.enable(RansTurbulenceModel.class);
        _pc.enable(KEpsilonTurbulence.class);
    }

    private void _enableKO() {
        _pc.enable(TurbulentModel.class);
        _pc.enable(RansTurbulenceModel.class);
        _pc.enable(KOmegaTurbulence.class);
    }

    private ArrayList<Class> _getClassesVOF() {
        ArrayList<Class> aclz = new ArrayList<>();
        aclz.add(EulerianMultiPhaseModel.class);
        aclz.add(SegregatedVofModel.class);
        aclz.add(SegregatedVolumeFluxBasedFlowModel.class);
        aclz.add(VofWaveModel.class);
        aclz.add(GravityModel.class);
        return aclz;
    }

    private double _getDensity(EulerianPhase phase) {
        return phase.getModelManager().getObjects().stream()
                .filter(m -> m.getBeanDisplayName().matches("Gas|Liquid"))
                .map(m -> _get.objects.constantMaterialProperty(m, ConstantDensityProperty.class))
                .map(cmpm -> cmpm.getQuantity().getSIValue())
                .findFirst().get();
    }

    private String _getPCName() {
        return _get.strings.withinTheBrackets(_s.toString()).replaceAll(", ", " / ");
    }

    @SuppressWarnings(value = "unchecked")
    private ArrayList<EulerianPhase> _getPhases(PhysicsContinuum pc) {
        ArrayList<EulerianPhase> phases = new ArrayList<>();
        EulerianMultiPhaseModel empm = pc.getModelManager().getModel(EulerianMultiPhaseModel.class);
        empm.getPhaseManager().getObjects().forEach((obj) -> phases.add((EulerianPhase) obj));
        return phases;
    }

    private void _resetData() {
        _pc = _sim.getContinuumManager().createContinuum(PhysicsContinuum.class);
        _s.clear();
        _io.say.msgDebug("Continuum created!");
    }

    private void _setDESType(DesFormulationOption.Type type) {
        _pc.getModelManager().getModel(SstKwTurbDesModel.class)
                .getDesFormulationOption().setSelected(type);
        _set.solver.lowNumericalDissipation(_pc, false);
    }

    private void _setEnergy(StaticDeclarations.Energy enrgy) {
        if (_chk.is.idealGas(_pc)) {
            enrgy = StaticDeclarations.Energy.THERMAL;
        }
        switch (enrgy) {
            case ISOTHERMAL:
                _s.add("Isothermal");
                break;
            case THERMAL:
                if (!_chk.is.solid(_pc)) {
                    if (_chk.is.segregated(_pc)) {
                        _pc.enable(SegregatedFluidTemperatureModel.class);
                    } else if (_chk.is.coupled(_pc)) {
                        _pc.enable(CoupledEnergyModel.class);
                    }
                }
                _s.add("Thermal");
                break;
            default:
                _io.say.msg("Invalid option for Energy. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Energy OK!");
    }

    private void _setEquationOfState(StaticDeclarations.Density eos) {
        if (_chk.has.VOF()) {
            _io.say.msgDebug("VOF. EOS skipped!");
            return;
        }
        switch (eos) {
            case CONSTANT:
                _enableCDM();
                break;
            case INCOMPRESSIBLE:
                _enableCDM();
                break;
            case IDEAL_GAS:
                _pc.enable(IdealGasModel.class);
                _s.add("Ideal Gas");
                break;
            default:
                _io.say.msg("Invalid option for Equation of State. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("EOS OK!");
    }

    private void _setMaterial(StaticDeclarations.Material mat) {
        switch (mat) {
            case GAS:
                _pc.enable(SingleComponentGasModel.class);
                _s.add("Gas");
                break;
            case LIQUID:
                _pc.enable(SingleComponentLiquidModel.class);
                _s.add("Liquid");
                break;
            case SOLID:
                _pc.enable(SolidModel.class);
                _s.add("Solid");
                break;
            case VOF:
                _s.add("VOF");
                _enable(_getClassesVOF());
                break;
            case VOF_AIR_WATER:
                _s.add("VOF Air & Water");
                _enable(_getClassesVOF());
                _createPhasesAirWater();
                break;
            default:
                _io.say.msg("Invalid option for Material model. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Material OK!");
    }

    private void _setSolver(StaticDeclarations.Solver slv) {
        if (_chk.has.VOF()) {
            _io.say.msgDebug("VOF. Solver skipped!");
            return;
        }
        switch (slv) {
            case SEGREGATED:
                if (_chk.is.solid(_pc)) {
                    _pc.enable(SegregatedSolidEnergyModel.class);
                } else {
                    _pc.enable(SegregatedFlowModel.class);
                }
                _s.add("Segregated");
                break;
            case COUPLED:
                if (_chk.is.solid(_pc)) {
                    _pc.enable(CoupledSolidEnergyModel.class);
                } else {
                    _pc.enable(CoupledFlowModel.class);
                }
                _s.add("Coupled");
                break;
            default:
                _io.say.msg("Invalid option for Solver model. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Solver OK!");
    }

    private void _setSpace(StaticDeclarations.Space spc) {
        switch (spc) {
            case TWO_DIMENSIONAL:
                _pc.enable(TwoDimensionalModel.class);
                _s.add("2D");
                break;
            case THREE_DIMENSIONAL:
                _pc.enable(ThreeDimensionalModel.class);
                _s.add("3D");
                break;
            default:
                _io.say.msg("Invalid option for Space model. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Space OK!");
    }

    private void _setTime(StaticDeclarations.Time time) {
        switch (time) {
            case STEADY:
                _pc.enable(SteadyModel.class);
                _s.add("Steady");
                break;
            case IMPLICIT_UNSTEADY:
                _pc.enable(ImplicitUnsteadyModel.class);
                _s.add("Unsteady");
                break;
            case EXPLICIT_UNSTEADY:
                _pc.enable(ExplicitUnsteadyModel.class);
                _s.add("Explicit Unsteady");
                break;
            default:
                _io.say.msg("Invalid option for Time model. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Time OK!");
    }

    private void _setViscous(StaticDeclarations.Viscous visc) {
        switch (visc) {
            case INVISCID:
                _pc.enable(InviscidModel.class);
                _s.add("Inviscid");
                break;
            case LAMINAR:
                _pc.enable(LaminarModel.class);
                _s.add("Laminar");
                break;
            case NOT_APPLICABLE:
                break;
            case KE_STD:
                _enableKE();
                _pc.enable(SkeTurbModel.class);
                _pc.enable(KeHighYplusWallTreatment.class);
                _s.add("Standard K-Epsilon high-Re");
                break;
            case RKE_2LAYER:
                _enableKE();
                _pc.enable(RkeTwoLayerTurbModel.class);
                _pc.enable(KeTwoLayerAllYplusWallTreatment.class);
                _s.add("Realizable K-Epsilon 2-layer");
                break;
            case RKE_HIGH_YPLUS:
                _enableKE();
                _pc.enable(RkeTurbModel.class);
                _pc.enable(KeHighYplusWallTreatment.class);
                _s.add("Realizable K-Epsilon high-Re");
                break;
            case KW_STD:
                _enableKO();
                _pc.enable(SkwTurbModel.class);
                _pc.enable(KwAllYplusWallTreatment.class);
                _s.add("Standard K-Omega 2-layer");
                break;
            case KW_2008:
                _enableKO();
                _pc.enable(SkwTurbModel.class);
                _pc.enable(KwAllYplusWallTreatment.class);
                SkwTurbModel skwtm = _pc.getModelManager().getModel(SkwTurbModel.class);
                skwtm.getKwTurbFreeShearOption()
                        .setSelected(KwTurbFreeShearOption.Type.CROSS_DIFFUSION);
                skwtm.setVortexStretching(true);
                skwtm.getKwTurbRealizabilityOption()
                        .setSelected(KwTurbRealizabilityOption.Type.DURBIN);
                _s.add("Revised 2008 K-Omega 2-layer");
                break;
            case SOLID:
                break;
            case SST_KW:
                _enableKO();
                _pc.enable(SstKwTurbModel.class);
                _pc.enable(KwAllYplusWallTreatment.class);
                _s.add("Menter SST");
                break;
            case DES_SST_KW_DDES:
                _enableDES_SST();
                _setDESType(DesFormulationOption.Type.DDES);
                _s.add("Menter SST DDES");
                break;
            case DES_SST_KW_IDDES:
                _enableDES_SST();
                _setDESType(DesFormulationOption.Type.IDDES);
                _s.add("Menter SST IDDES");
                break;
            default:
                _io.say.msg("Invalid option for Viscous Regime. Aborting...", true);
                _isOK = false;
                break;
        }
        _io.say.msgDebug("Viscous OK!");
    }

}
