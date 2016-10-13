package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.material.*;
import star.vis.*;

/**
 * Low-level class for retrieving STAR-CCM+ objects in general in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class GetObjects {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetObjects(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private ArrayList<NamedObject> _getFFs() {
        return new ArrayList(_sim.getFieldFunctionManager().getObjects());
    }

    private ArrayList<PhysicsContinuum> _getPCs() {
        ArrayList<PhysicsContinuum> apc = new ArrayList();
        for (Continuum pc : _sim.getContinuumManager().getObjects()) {
            if (pc instanceof PhysicsContinuum) {
                apc.add((PhysicsContinuum) pc);
            }
        }
        return apc;
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param ano given ArrayList of NamedObjects.
     * @param what what kind of object? E.g: Plane, Report, Scene, etc...
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of NamedObjects.
     */
    public ArrayList<NamedObject> allByREGEX(String regexPatt, String what, ArrayList<NamedObject> ano, boolean vo) {
        ArrayList<NamedObject> arr = new ArrayList();
        _io.print.msg(vo, "Getting %s by REGEX search pattern: \"%s\".", what, regexPatt);
        if (ano.isEmpty()) {
            _io.print.msg(vo, "Input ArrayList is empty.");
        } else {
            for (NamedObject no : ano) {
                String name = no.getPresentationName();
                boolean hasMatch = name.matches(regexPatt);
                if ((no instanceof FieldFunction) && !hasMatch) {
                    name = ((FieldFunction) no).getFunctionName();
                    hasMatch = name.matches(regexPatt);
                }
                if (!hasMatch) {
                    continue;
                }
                _io.say.msg(vo, "  - Match: \"%s\"", name, vo);
                arr.add(no);
            }
            String s = "";
            if (arr.size() > 1) {
                s = "s";
            }
            _io.say.msg(vo, "Found %d item%s.", arr.size(), s);
        }
        if (arr.isEmpty()) {
            _io.print.msg(vo, "Returning an ArrayList with a null value inside.");
            arr.add(null);
        }
        return arr;
    }

    /**
     * Gets all Physics Continuas available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of PhysicsContinuum.
     */
    public ArrayList<PhysicsContinuum> allPhysicsContinua(boolean vo) {
        ArrayList<PhysicsContinuum> apc = _getPCs();
        _io.say.objects(apc, "Getting all Physics Continuas", vo);
        return apc;
    }

    /**
     * Gets an Annotation that matches the REGEX search pattern among all Annotations available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Annotation. Null if nothing is found.
     */
    public star.vis.Annotation annotation(String regexPatt, boolean vo) {
        return (star.vis.Annotation) byREGEX(regexPatt, "Annotation",
                new ArrayList(_sim.getAnnotationManager().getObjects()), vo);
    }

    /**
     * Gets an ArrayList from an object. Casting to the desired variable may be necessary.
     *
     * @param obj given JAVA Object.
     * @return An ArrrayList.
     */
    public ArrayList arrayList(Object obj) {
        return new ArrayList(Arrays.asList(new Object[]{obj}));
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param ano given ArrayList of NamedObjects.
     * @param vo given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is found.
     */
    public NamedObject byREGEX(String regexPatt, ArrayList<NamedObject> ano, boolean vo) {
        String s = ano.get(0).getParent().getBeanDisplayName();
        return byREGEX(regexPatt, s, ano, vo);
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param ano given ArrayList of NamedObjects.
     * @param what what kind of object? E.g: Plane, Report, Scene, etc...
     * @param vo given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is found.
     */
    public NamedObject byREGEX(String regexPatt, String what, ArrayList<NamedObject> ano, boolean vo) {
        return allByREGEX(regexPatt, what, ano, vo).get(0);
    }

    /**
     * Gets a standard colormap shipped with STAR-CCM+.
     *
     * @param opt given Volume Mesher choice. See {@link StaticDeclarations.Colormaps} for options.
     * @return The LookupTable.
     */
    public LookupTable colormap(StaticDeclarations.Colormaps opt) {
        return _sim.get(LookupTableManager.class).getObject(opt.getName());
    }

    /**
     * Gets a constant material property object to be manipulated by MacroUtils.
     *
     * @param m given {@link star.common.Model}.
     * @param clz given material property Class. E.g.: {@link star.flow.ConstantDensityProperty}, etc...
     * @return The ConstantMaterialPropertyMethod.
     */
    public ConstantMaterialPropertyMethod constantMaterialProperty(Model m, Class clz) {
        if (m instanceof SingleComponentMaterialModel) {
            SingleComponentMaterialModel scmm = (SingleComponentMaterialModel) m;
            MaterialPropertyManager mpp = scmm.getMaterial().getMaterialProperties();
            return (ConstantMaterialPropertyMethod) mpp.getMaterialProperty(clz).getMethod();
        }
        _io.say.msg("ConstantMaterialPropertyMethod is NULL.");
        return null;
    }

    /**
     * Gets a DoubleVector from values.
     *
     * @param vals given values separated by comma.
     * @return The DoubleVector.
     */
    public DoubleVector doubleVector(double... vals) {
        return new DoubleVector(vals);
    }

    /**
     * Loops over all Field Functions and returns the first match based on the REGEX search pattern. The search will be
     * done on Function Name first and then on its name on GUI, i.e., the PresentationName.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(String regexPatt, boolean vo) {
        return (FieldFunction) allByREGEX(regexPatt, "Field Function", _getFFs(), vo).get(0);
    }

    /**
     * Gets a Field Function based on the strings previously defined.
     *
     * @param var given predefined variable defined in {@link StaticDeclarations} class.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(StaticDeclarations.Vars var) {
        FieldFunction ff = fieldFunction(var.getVar(), false);
        _io.say.value("Asked for Field Function", var.getVar(), true, true);
        if (ff == null) {
            _io.say.msg(true, "Returning NULL.");
            return null;
        }
        _io.say.value("Returning", ff.getPresentationName(), true, true);
        if (var.equals(StaticDeclarations.Vars.VEL_MAG)) {
            return ff.getMagnitudeFunction();
        }
        return ff;
    }

    /**
     * Loops over all Field Functions and return all matches based on the REGEX search pattern. The search will be done
     * on Function Name first and then on its name on GUI, i.e., the PresentationName.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of FieldFunctions.
     */
    public ArrayList<FieldFunction> fieldFunctions(String regexPatt, boolean vo) {
        return new ArrayList(allByREGEX(regexPatt, "Field Functions", _getFFs(), vo));
    }

    /**
     * Gets the HardcopyProperties from a NamedObject.
     *
     * @param no given NamedObject. It can be a Plot or Scene.
     * @param vo given verbose option. False will not print anything.
     * @return The HardcopyProperties. Null if nothing is found.
     */
    public HardcopyProperties hardcopyProperties(NamedObject no, boolean vo) {
        if (no instanceof Scene) {
            return ((Scene) no).getSceneUpdate().getHardcopyProperties();
        } else if (no instanceof StarPlot) {
            return ((UpdatePlot) no).getPlotUpdate().getHardcopyProperties();
        }
        _io.say.msg(vo, "'%s' does not have a HardcopyProperties. Returning NULL...", no.getPresentationName());
        return null;
    }

    /**
     * Gets a Global Parameter that matches the REGEX search pattern among all Parameters available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The GlobalParameterBase. Null if nothing is found.
     */
    public GlobalParameterBase parameter(String regexPatt, boolean vo) {
        return (GlobalParameterBase) byREGEX(regexPatt, "Global Parameter",
                new ArrayList(_sim.get(GlobalParameterManager.class).getObjects()), vo);
    }

    /**
     * Gets a Physics Continua that matches the REGEX search pattern among all Continuas available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The PhysicsContinuum. Null if nothing is found.
     */
    public PhysicsContinuum physicsContinua(String regexPatt, boolean vo) {
        return (PhysicsContinuum) byREGEX(regexPatt, "Physics Continua", new ArrayList(_getPCs()), vo);
    }

    /**
     * Gets the Profile object.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object name.
     * @param vo given verbose option. False will not print anything.
     * @return The Profile. Null if nothing is found.
     */
    public Profile profile(ClientServerObjectManager csom, String name, boolean vo) {
        if (!csom.has(name)) {
            _io.say.msg(vo, "'%s' does not have a '%s' value. Returning NULL...", csom.getPresentationName(), name);
            return null;
        }
        return (Profile) csom.getObject(name);
    }

    /**
     * Gets a ScalarProfile, if applicable.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object name.
     * @param vo given verbose option. False will not print anything.
     * @return The ScalarProfile. Null if nothing is found.
     */
    public ScalarProfile scalarProfile(ClientServerObjectManager csom, String name, boolean vo) {
        return (ScalarProfile) profile(csom, name, vo);
    }

    /**
     * Gets a Transform that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The VisTransform.
     */
    public VisTransform transform(String regexPatt, boolean vo) {
        return (VisTransform) _get.objects.byREGEX(regexPatt, "Transform",
                new ArrayList(_sim.getTransformManager().getObjects()), vo);
    }

    /**
     * Gets a VectorProfile, if applicable.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object name.
     * @param vo given verbose option. False will not print anything.
     * @return The VectorProfile. Null if nothing is found.
     */
    public VectorProfile vectorProfile(ClientServerObjectManager csom, String name, boolean vo) {
        return (VectorProfile) profile(csom, name, vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
