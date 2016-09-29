package macroutils.setter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.energy.*;
import star.flow.*;

/**
 * Low-level class for setting Boundary parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetBoundaries {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetBoundaries(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private ScalarProfile _getSP(Boundary b, StaticDeclarations.Vars var) {
        return _get.objects.scalarProfile(b.getValues(), var.getVar(), false);
    }

    private void _setEnergyWall(Boundary b, WallThermalOption.Type wtoType, String wallEnrgy) {
        _io.say.action(String.format("Setting BC as %s...", wallEnrgy), b, true);
        b.setBoundaryType(WallBoundary.class);
        b.getConditions().get(WallThermalOption.class).setSelected(wtoType);
    }

    private void _setType(Boundary b, Class clz, String what) {
        _io.say.action(String.format("Setting BC as %s...", what), b, true);
        b.setBoundaryType(clz);
    }

    private void _setValues(Boundary b, StaticDeclarations.Vars var, double val) {
        Units u = _ud.unit_Dimensionless;
        String vn = var.getVar();
        _io.say.msgDebug("values():");
        _io.say.msgDebug("  - Var: %s", var.getVar());
        if (vn.contains("Temperature")) {
            u = _ud.defUnitTemp;
            _io.say.msgDebug("  - Contains Temperature");
        } else if (vn.contains("Velocity")) {
            u = _ud.defUnitVel;
            _io.say.msgDebug("  - Contains Velocity");
        } else if (vn.contains("Pressure")) {
            u = _ud.defUnitPress;
            _io.say.msgDebug("  - Contains Pressure");
        } else if (var.equals(StaticDeclarations.Vars.HTC)) {
            u = _ud.defUnitHTC;
        }
        _io.say.msgDebug("  - Units: %s", _get.strings.fromUnit(u));
        _setValues(b, vn, val, u, true, false);
    }

    private void _setValues(Boundary b, String name, double val, Units u, boolean vo, boolean prtAct) {
        _setValues(b, name, val, null, u, vo, prtAct);
    }

    private void _setValues(Boundary b, String name, double[] vals, Units u, boolean vo, boolean prtAct) {
        _setValues(b, name, 0.0, vals, u, vo, prtAct);
    }

    private void _setValues(Boundary b, String name, double val, double[] vals, Units u, boolean vo, boolean prtAct) {
        if (!b.getValues().has(name)) {
            _io.say.msgDebug("b.getValues().has() no \"%s\"", name);
            return;
        }
        _io.say.action("Setting a Constant Boundary Value", b, prtAct);
        if (vals == null) {
            ScalarProfile sp = _get.objects.scalarProfile(b.getValues(), name, vo);
            _set.object.profile(sp, val, u);
        } else {
            VectorProfile vp = _get.objects.vectorProfile(b.getValues(), name, vo);
            _set.object.profile(vp, vals, u);
        }
        _io.say.ok(prtAct);
    }

    /**
     * Sets a Boundary as Wall and define Heat Transfer as Convection type.
     *
     * @param b given Boundary.
     * @param T given Ambient Temperature in default units. See {@link UserDeclarations#defUnitTemp}.
     * @param htc given Heat Transfer Coefficient in default units. See {@link UserDeclarations#defUnitHTC}.
     */
    public void asConvectionWall(Boundary b, double T, double htc) {
        _setEnergyWall(b, WallThermalOption.Type.CONVECTION, "Convection Wall");
        _setValues(b, StaticDeclarations.Vars.AMBIENT_T, T);
        _setValues(b, StaticDeclarations.Vars.HTC, htc);
        _io.say.ok(true);
    }

    /**
     * Sets the Boundary as a Free Slip Wall.
     *
     * @param b given Boundary.
     */
    public void asFreeSlipWall(Boundary b) {
        _setType(b, WallBoundary.class, "Free Slip Wall");
        b.getConditions().get(WallShearStressOption.class).setSelected(WallShearStressOption.Type.SLIP);
        _io.say.ok(true);
    }

    /**
     * Sets the Boundary as Free Stream.
     *
     * @param b given Boundary.
     * @param dir given 3-components direction of the flow. E.g., in X: {1, 0, 0}.
     * @param mach given Mach number.
     * @param P given Pressure in default units. See {@link UserDeclarations#defUnitPress}.
     * @param T given Static Temperature in default units. See {@link UserDeclarations#defUnitTemp}.
     * @param ti given Turbulent Intensity dimensionless, if applicable.
     * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
     */
    public void asFreeStream(Boundary b, double[] dir, double mach, double P, double T, double ti, double tvr) {
        _setType(b, FreeStreamBoundary.class, "Free Stream");
        _setValues(b, "Flow Direction", dir, _ud.unit_Dimensionless, true, false);
        _setValues(b, StaticDeclarations.Vars.MACH, mach);
        _setValues(b, StaticDeclarations.Vars.P, P);
        _setValues(b, StaticDeclarations.Vars.STATIC_T, T);
        _setValues(b, StaticDeclarations.Vars.TI, ti);
        _setValues(b, StaticDeclarations.Vars.TVR, tvr);
        _io.say.ok(true);
    }

    /**
     * Sets a Boundary as Pressure Outlet. When running Laminar or Isothermal other parameters are ignored.
     *
     * @param b given Boundary.
     * @param P given Static Pressure in default units. See {@link UserDeclarations#defUnitPress}.
     * @param T given Static Temperature in default units. See {@link UserDeclarations#defUnitTemp}, if applicable.
     * @param ti given Turbulent Intensity dimensionless, if applicable.
     * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
     */
    public void asPressureOutlet(Boundary b, double P, double T, double ti, double tvr) {
        _setType(b, PressureBoundary.class, "Pressure Outlet");
        _setValues(b, StaticDeclarations.Vars.P, P);
        _setValues(b, StaticDeclarations.Vars.STATIC_T, T);
        _setValues(b, StaticDeclarations.Vars.TI, ti);
        _setValues(b, StaticDeclarations.Vars.TVR, tvr);
        _io.say.ok(true);
    }

    /**
     * Sets a Boundary as Symmetry.
     *
     * @param b given Boundary.
     */
    public void asSymmetry(Boundary b) {
        _setType(b, SymmetryBoundary.class, "Symmetry");
        _io.say.ok(true);
    }

    /**
     * Sets a Boundary as Velocity Inlet. When running Laminar or Isothermal other parameters are ignored.
     *
     * @param b given Boundary.
     * @param vel given Velocity Magnitude in default units. See {@link UserDeclarations#defUnitVel}.
     * @param T given Static Temperature in default units. See {@link UserDeclarations#defUnitTemp}, if applicable.
     * @param ti given Turbulent Intensity dimensionless, if applicable.
     * @param tvr given Turbulent Viscosity Ratio dimensionless, if applicable.
     */
    public void asVelocityInlet(Boundary b, double vel, double T, double ti, double tvr) {
        _setType(b, InletBoundary.class, "Velocity Inlet");
        _setValues(b, StaticDeclarations.Vars.VEL_MAG, vel);
        _setValues(b, StaticDeclarations.Vars.STATIC_T, T);
        _setValues(b, StaticDeclarations.Vars.TI, ti);
        _setValues(b, StaticDeclarations.Vars.TVR, tvr);
        _io.say.ok(true);
    }

    /**
     * Combine several boundaries.
     *
     * @param ab given ArrayList of Boundaries. Make sure they all belong to the same Region.
     * @return The combined Boundary.
     */
    public Boundary combine(ArrayList<Boundary> ab) {
        _io.say.action("Combining Boundaries", true);
        _io.say.objects(ab, "Current Boundaries", true);
        Boundary b0 = ab.get(0);
        Region r = b0.getRegion();
        _io.say.object(r, true);
        if (ab.size() < 2) {
            _io.say.msg("Not enough boundaries to combine.");
            return b0;
        }
        if (_chk.has.volumeMesh()) {
            _io.say.msg(true, "Found a Volume Mesh. Will combine using legacy method.");
            _sim.getMeshManager().combineBoundaries(new Vector(ab));
        } else {
            ArrayList<PartSurface> alp = new ArrayList();
            for (Boundary b : ab) {
                _io.say.object(b, true);
                ArrayList<PartSurface> alpb = new ArrayList(b.getPartSurfaceGroup().getObjects());
                alp.addAll(alpb);
                b.getPartSurfaceGroup().removeObjects(alpb);
            }
            ab.remove(b0);
            b0.getRegion().getBoundaryManager().removeObjects(ab);
            b0.getPartSurfaceGroup().addObjects(alp);
        }
        _io.say.objects(new ArrayList(r.getBoundaryManager().getObjects()), "Boundaries after Combination", true);
        _io.say.ok(true);
        return b0;
    }

    /**
     * Applies a definition to a value in a Boundary.
     *
     * @param b given Boundary.
     * @param var given predefined variable defined in {@link StaticDeclarations} class.
     * @param def given definition.
     */
    public void definition(Boundary b, StaticDeclarations.Vars var, String def) {
        _io.say.action("Setting a Boundary Definition", b, true);
        _set.object.profile(_getSP(b, var), def);
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
    }

    /**
     * Applies a Physics Value to a Boundary using a constant given value.
     *
     * @param b given Boundary.
     * @param var given predefined variable defined in {@link StaticDeclarations} class.
     * @param val given value.
     * @param u given Units.
     */
    public void values(Boundary b, StaticDeclarations.Vars var, double val, Units u) {
        _setValues(b, var.getVar(), val, u, true, true);
    }

    /**
     * Sets the Boundary values according to elements given in a Table.
     * <p>
     * <b>Notes:</b>
     * <ul>
     * <li> The columns to be mapped must have the same name as are given the Physics Values tree. E.g.: "Velocity
     * Magnitude", "Turbulent Viscosity Ratio", etc...;
     * <li> Current method is limited to Scalar values and XYZ tables only.
     * </ul>
     *
     * @param b given Boundary.
     * @param t given Table.
     */
    public void values(Boundary b, Table t) {
        _io.say.action("Setting Boundary Values from a Table", true);
        _io.say.object(b, true);
        _io.say.object(t, true);
        _io.say.objects(new ArrayList(b.getValues().getObjects()), "Physics Values", true);
        _io.say.objects(new ArrayList(t.getColumnDescriptors()), "Columns", true);
        ArrayList<String> cols = new ArrayList();
        for (ColumnDescriptor cd : t.getColumnDescriptors()) {
            cols.add(cd.getColumnName());
        }
        for (ClientServerObject cso : b.getValues().getObjects()) {
            String name = cso.getPresentationName();
            if (cols.contains(name)) {
                _io.say.msg("Setting to Table (x,y,z): " + name);
                ScalarProfile p = (ScalarProfile) cso;
                p.setMethod(XyzTabularScalarProfileMethod.class);
                p.getMethod(XyzTabularScalarProfileMethod.class).setTable(t);
                p.getMethod(XyzTabularScalarProfileMethod.class).setData(name);
            }
        }
        _io.say.ok(true);
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
