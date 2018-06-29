package macroutils.setter;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.common.Model;
import star.common.PhysicsContinuum;
import star.common.Units;
import star.energy.SpecificHeatProperty;
import star.energy.ThermalConductivityProperty;
import star.energy.TurbulentPrandtlNumberProperty;
import star.flow.ConstantDensityProperty;
import star.flow.DynamicViscosityProperty;
import star.flow.Gravity;
import star.material.ConstantMaterialPropertyMethod;
import star.material.Material;
import star.material.MaterialPropertyManager;
import star.material.MaterialPropertyMethod;
import star.material.SingleComponentMaterialModel;

/**
 * Low-level class for setting Physics parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetPhysics {

    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.misc.MainUpdater _upd = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetPhysics(MacroUtils m) {
        _mu = m;
    }

    /**
     * Sets the Gravity information for the Physics Continua.
     *
     * @param pc   given Physics Continua.
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
     * @param pc   given Physics Continua.
     * @param name given Initial Condition name. E.g.: Pressure, Temperature, etc...
     * @param val  given value.
     * @param u    given Units.
     */
    public void initialCondition(PhysicsContinuum pc, String name, double val, Units u) {
        initialCondition(pc, name, new double[]{ val }, u, true);
    }

    /**
     * Sets an Initial Condition to a Vector Variable.
     *
     * @param pc   given Physics Continua.
     * @param name given Initial Condition name. E.g.: Velocity, etc...
     * @param vals given double[] array of values. E.g.: {1.2, 0, 0}.
     * @param u    given Units.
     */
    public void initialCondition(PhysicsContinuum pc, String name, double[] vals, Units u) {
        initialCondition(pc, name, vals, u, true);
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
        initialCondition(pc, StaticDeclarations.Vars.P.getVar(),
                _ud.p0, _ud.defUnitPress, false);
        initialCondition(pc, StaticDeclarations.Vars.STATIC_T.getVar(),
                _ud.t0, uT, false);
        initialCondition(pc, StaticDeclarations.Vars.TI.getVar(),
                _ud.ti0, _ud.unit_Dimensionless, false);
        initialCondition(pc, StaticDeclarations.Vars.TVR.getVar(),
                _ud.tvr0, _ud.unit_Dimensionless, false);
        initialCondition(pc, StaticDeclarations.Vars.TVS.getVar(),
                _ud.tvs0, _ud.defUnitVel, false);
        initialCondition(pc, StaticDeclarations.Vars.VEL.getVar(),
                _ud.v0, _ud.defUnitVel, false);
        _io.say.ok(true);
    }

    /**
     * Sets a Constant Material Property for a given media.
     *
     * <b>Currently, only single-phase is supported.</b>
     *
     * @param pc      given Physics Continua.
     * @param matName given material name. E.g.: H2O.
     * @param matProp given material property, if applicable.
     * @param val     given value.
     * @param u       given Units. Use <b>null</b> to use preferred units.
     */
    public void materialProperty(PhysicsContinuum pc, String matName, 
            StaticDeclarations.Vars matProp, double val, Units u) {
        _io.say.action("Setting Material Property", true);
        _io.say.value("Material Name", matName, true, true);
        _set.object.physicalQuantity(_getCMPM(_getModel(pc), matProp).getQuantity(),
                val, u, matProp.getVar(), true);
        _io.say.ok(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
        _upd = _mu.update;
    }

    private ConstantMaterialPropertyMethod _getCMPM(Model model, StaticDeclarations.Vars var) {
        MaterialPropertyMethod mpm;
        if (!(model instanceof SingleComponentMaterialModel)) {
            _io.say.msg("Currently limited to Single Component materials!");
            return null;
        }
        Material mat = ((SingleComponentMaterialModel) model).getMaterial();
        MaterialPropertyManager mpmgr = mat.getMaterialProperties();
        switch (var) {
            case CP:
                mpm = mpmgr.getMaterialProperty(SpecificHeatProperty.class).getMethod();
                break;
            case DEN:
                mpm = mpmgr.getMaterialProperty(ConstantDensityProperty.class).getMethod();
                break;
            case K:
                mpm = mpmgr.getMaterialProperty(ThermalConductivityProperty.class).getMethod();
                break;
            case PRANDTL:
                mpm = mpmgr.getMaterialProperty(TurbulentPrandtlNumberProperty.class).getMethod();
                break;
            case VISC:
                mpm = mpmgr.getMaterialProperty(DynamicViscosityProperty.class).getMethod();
                break;
            default:
                _io.say.value("Invalid variable for material property", var.getVar(), true, true);
                return null;
        }
        return (ConstantMaterialPropertyMethod) mpm;
    }

    private Model _getModel(PhysicsContinuum pc) {
        if (pc.getModelManager().getObjectsOf(SingleComponentMaterialModel.class).size() > 0) {
            return pc.getModelManager().getObjectsOf(SingleComponentMaterialModel.class).get(0);
        }
        return null;
    }

    private void initialCondition(PhysicsContinuum pc, String name, double val, Units u,
            boolean vo) {
        initialCondition(pc, name, new double[]{ val }, u, vo);
    }

    private void initialCondition(PhysicsContinuum pc, String name, double[] vals, Units u,
            boolean vo) {
        _upd.allUnits(false);
        if (_get.objects.profile(pc.getInitialConditions(), name, false) == null) {
            vo = false;
        }
        if (vals.length == 1) {
            _set.object.profile(_get.objects.scalarProfile(pc.getInitialConditions(), name, vo),
                    vals[0], u);
            return;
        }
        _io.say.action("Setting Initial Condition", vo);
        _set.object.profile(_get.objects.vectorProfile(pc.getInitialConditions(), name, vo),
                vals, u);
        _io.say.ok(vo);
    }

}
