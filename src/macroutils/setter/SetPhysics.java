package macroutils.setter;

import macroutils.*;
import star.common.*;
import star.energy.*;
import star.flow.*;
import star.material.*;

/**
 * Low-level class for setting Physics parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetPhysics {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetPhysics(MacroUtils m) {
        _mu = m;
    }

    private Model _getMM(PhysicsContinuum pc) {
        ModelManager mm = pc.getModelManager();
        if (mm.has("Gas")) {
            return mm.getModel(SingleComponentGasModel.class);
        } else if (mm.has("Liquid")) {
            return mm.getModel(SingleComponentLiquidModel.class);
        } else if (mm.has("Solid")) {
            return mm.getModel(SolidModel.class);
        }
        return null;
    }

    private ConstantMaterialPropertyMethod _getCMPM(Model model, StaticDeclarations.Vars var) {
        Class clz = null;
        MaterialPropertyMethod mpm = null;
        switch (var) {
            case CP:
                clz = SpecificHeatProperty.class;
                break;
            case DEN:
                clz = ConstantDensityProperty.class;
                break;
            case K:
                clz = ThermalConductivityProperty.class;
                break;
            case PRANDTL:
                clz = TurbulentPrandtlNumberProperty.class;
                break;
            case VISC:
                clz = DynamicViscosityProperty.class;
                break;
            default:
                _io.say.value("Invalid variable for material property", var.getVar(), true, true);
                break;
        }
        if (model.getClass().getName().matches(".*SingleComponentGasModel$")) {
            SingleComponentGasModel scgm = (SingleComponentGasModel) model;
            Gas gas = (Gas) scgm.getMaterial();
            mpm = gas.getMaterialProperties().getMaterialProperty(clz).getMethod();
        }
        if (model.getClass().getName().matches(".*SingleComponentLiquidModel$")) {
            SingleComponentLiquidModel sclm = (SingleComponentLiquidModel) model;
            Liquid liq = (Liquid) sclm.getMaterial();
            mpm = liq.getMaterialProperties().getMaterialProperty(clz).getMethod();
        }
        if (model.getClass().getName().matches(".*SinglePhaseGasModel$")) {
            SinglePhaseGasModel spgm = (SinglePhaseGasModel) model;
            SinglePhaseGas spg = (SinglePhaseGas) spgm.getMaterial();
            mpm = spg.getMaterialProperties().getMaterialProperty(clz).getMethod();
        }
        if (model.getClass().getName().matches(".*SinglePhaseLiquidModel$")) {
            SinglePhaseLiquidModel splm = (SinglePhaseLiquidModel) model;
            SinglePhaseLiquid spl = (SinglePhaseLiquid) splm.getMaterial();
            mpm = spl.getMaterialProperties().getMaterialProperty(clz).getMethod();
        }
        return (ConstantMaterialPropertyMethod) mpm;
    }

    /**
     * Sets the Gravity information for the Physics Continua.
     *
     * @param pc given Physics Continua.
     * @param vals given 3-component value for the gravity. E.g: [0, -9.81, 0].
     */
    public void gravity(PhysicsContinuum pc, double[] vals) {
        _io.say.action("Setting Gravity", true);
        _io.say.object(pc, true);
        if (!pc.getReferenceValues().has("Gravity")) {
            _io.say.msg("Physics does not have a Gravity model.");
            return;
        }
        pc.getReferenceValues().get(Gravity.class).setConstant(vals);
        _io.say.ok(true);
    }

    /**
     * Sets an Initial Condition to a Scalar Variable.
     *
     * @param pc given Physics Continua.
     * @param name given Initial Condition name. E.g.: Pressure, Temperature, etc...
     * @param val given value.
     * @param u given Units.
     */
    public void initialCondition(PhysicsContinuum pc, String name, double val, Units u) {
        initialCondition(pc, name, new double[]{val}, u, true);
    }

    /**
     * Sets an Initial Condition to a Vector Variable.
     *
     * @param pc given Physics Continua.
     * @param name given Initial Condition name. E.g.: Velocity, etc...
     * @param vals given double[] array of values. E.g.: {1.2, 0, 0}.
     * @param u given Units.
     */
    public void initialCondition(PhysicsContinuum pc, String name, double[] vals, Units u) {
        initialCondition(pc, name, vals, u, true);
    }

    private void initialCondition(PhysicsContinuum pc, String name, double val, Units u, boolean vo) {
        initialCondition(pc, name, new double[]{val}, u, vo);
    }

    private void initialCondition(PhysicsContinuum pc, String name, double[] vals, Units u, boolean vo) {
        _upd.allUnits(false);
        if (_get.objects.profile(pc.getInitialConditions(), name, false) == null) {
            vo = false;
        }
        if (vals.length == 1) {
            _set.object.profile(_get.objects.scalarProfile(pc.getInitialConditions(), name, vo), vals[0], u);
            return;
        }
        _io.say.action("Setting Initial Condition", vo);
        _set.object.profile(_get.objects.vectorProfile(pc.getInitialConditions(), name, vo), vals, u);
        _io.say.ok(vo);
    }

    /**
     * Sets the Initial Conditions for a Physics Continua.
     *
     * @param pc given Physics Continua.
     */
    public void initialConditions(PhysicsContinuum pc) {
        _io.say.action("Setting Initial Conditions", true);
        Units uT = _ud.defUnitTemp;
        if (_ud.t0 == 300.0) {
            uT = _ud.unit_K;
        }
        initialCondition(pc, StaticDeclarations.Vars.P.getVar(), _ud.p0, _ud.defUnitPress, false);
        initialCondition(pc, StaticDeclarations.Vars.STATIC_T.getVar(), _ud.t0, uT, false);
        initialCondition(pc, StaticDeclarations.Vars.TI.getVar(), _ud.ti0, _ud.unit_Dimensionless, false);
        initialCondition(pc, StaticDeclarations.Vars.TVR.getVar(), _ud.tvr0, _ud.unit_Dimensionless, false);
        initialCondition(pc, StaticDeclarations.Vars.TVS.getVar(), _ud.tvs0, _ud.defUnitVel, false);
        initialCondition(pc, StaticDeclarations.Vars.VEL.getVar(), _ud.v0, _ud.defUnitVel, false);
        _io.say.ok(true);
    }

    /**
     * Sets a Constant Material Property for a given media.
     *
     * <b>Currently, only single-phase is supported.</b>
     *
     * @param pc given Physics Continua.
     * @param matName given material name. E.g.: H2O.
     * @param matProp given material property, if applicable.
     * @param val given value.
     * @param u given Units. Use <b>null</b> to use preferred units.
     */
    public void materialProperty(PhysicsContinuum pc, String matName, StaticDeclarations.Vars matProp,
            double val, Units u) {
        _io.say.action("Setting Material Property", true);
        _io.say.value("Material Name", matName, true, true);
        _set.object.physicalQuantity(_getCMPM(_getMM(pc), matProp).getQuantity(), val, u, matProp.getVar(), true);
        _io.say.ok(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
        _upd = _mu.update;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.misc.MainUpdater _upd = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;

}
