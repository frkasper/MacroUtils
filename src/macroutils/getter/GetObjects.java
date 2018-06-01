package macroutils.getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.base.neo.ClientServerObjectManager;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.common.ContinuumManager;
import star.common.FieldFunction;
import star.common.GeometryPart;
import star.common.GlobalParameterBase;
import star.common.GlobalParameterManager;
import star.common.HardcopyProperties;
import star.common.Model;
import star.common.PartSurface;
import star.common.PhysicsContinuum;
import star.common.Profile;
import star.common.Region;
import star.common.ScalarProfile;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.UpdatePlot;
import star.common.VectorProfile;
import star.material.ConstantMaterialPropertyMethod;
import star.material.MaterialProperty;
import star.material.MaterialPropertyManager;
import star.material.SingleComponentMaterialModel;
import star.vis.Annotation;
import star.vis.LookupTable;
import star.vis.LookupTableManager;
import star.vis.Scene;
import star.vis.VisTransform;

/**
 * Low-level class for retrieving STAR-CCM+ objects in general in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class GetObjects {

    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetObjects(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param <T>       any Class that extends from NamedObject in STAR-CCM+.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param alt       given ArrayList of T.
     * @param key       key kind of object? E.g: Plane, Report, Scene, etc...
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Ts.
     */
    public <T extends NamedObject> ArrayList<T> allByREGEX(String regexPatt, String key,
            ArrayList<T> alt, boolean vo) {
        ArrayList<T> found = new ArrayList<>();
        _io.print.msg(vo, "Getting %s by REGEX search pattern: \"%s\".", key, regexPatt);
        if (alt.isEmpty()) {
            _io.print.msg(vo, "Input ArrayList is empty.");
        } else {
            found.addAll(alt.stream()
                    .filter(t -> _matches(t, regexPatt, vo))
                    .collect(Collectors.toList()));
        }
        _io.say.msg(vo, "Found %d item%s.", found.size(), found.size() > 1 ? "s" : "");
        return found;
    }

    /**
     * Gets all Physics Continuas available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of PhysicsContinuum.
     */
    public ArrayList<PhysicsContinuum> allPhysicsContinua(boolean vo) {
        ContinuumManager cm = _sim.getContinuumManager();
        ArrayList<PhysicsContinuum> apc = new ArrayList<>(cm.getObjectsOf(PhysicsContinuum.class));
        _io.say.objects(apc, "Getting all Physics Continuas", vo);
        return apc;
    }

    /**
     * Gets an Annotation that matches the REGEX search pattern among all Annotations available in
     * the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The Annotation. Null if nothing is all.
     */
    public Annotation annotation(String regexPatt, boolean vo) {
        return byREGEX(regexPatt, "Annotation",
                new ArrayList<>(_sim.getAnnotationManager().getObjects()), vo);
    }

    /**
     * Gets an ArrayList from a STAR-CCM+ object.
     *
     * @param <T> given type of Array.
     * @param no  given NamedObject.
     * @return An ArrrayList.
     */
    public <T extends NamedObject> ArrayList<T> arrayList(T no) {
        return new ArrayList<>(Arrays.asList(no));
    }

    /**
     * Gets an ArrayList from a STAR-CCM+ object.
     *
     * @param <T> given type of Array.
     * @param ano given ArrayList of NamedObjects.
     * @return An ArrrayList.
     */
    public <T extends NamedObject> ArrayList<T> arrayList(ArrayList<T> ano) {
        return new ArrayList<>(ano);
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param <T>       any Class that extends from NamedObject in STAR-CCM+.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param alt       given ArrayList of T.
     * @param vo        given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is all.
     */
    public <T extends NamedObject> T byREGEX(String regexPatt, ArrayList<T> alt, boolean vo) {
        return byREGEX(regexPatt, _get.strings.parentName(alt.get(0)), alt, vo);
    }

    /**
     * Gets a STAR-CCM+ NamedObject by using a REGEX search pattern.
     *
     * @param <T>       any Class that extends from NamedObject in STAR-CCM+.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param alt       given ArrayList of T.
     * @param key       key kind of object? E.g: Plane, Report, Scene, etc...
     * @param vo        given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is all.
     */
    public <T extends NamedObject> T byREGEX(String regexPatt, String key, ArrayList<T> alt,
            boolean vo) {
        ArrayList<T> found = allByREGEX(regexPatt, key, alt, vo);
        if (found.isEmpty()) {
            _io.print.msg(vo, "Input ArrayList is empty.");
            _io.print.msg(vo, "Returning null.");
            return null;
        } else {
            return found.get(0);
        }
    }

    /**
     * Gets all the children objects related to the given ArrayList of parent objects, when
     * applicable.
     *
     * @param <T> any Class that extends from NamedObject in STAR-CCM+.
     * @param alt given ArrayList of STAR-CCM+ objects. E.g.: Regions, Boundaries, Parts,
     *            PlaneSections, etc...
     * @param vo  given verbose option. False will not print anything.
     * @return An ArrayList of children NamedObjects, when applicable.
     */
    @SuppressWarnings(value = "unchecked")
    public <T extends NamedObject> ArrayList<T> children(ArrayList<T> alt, boolean vo) {
        _io.say.objects(alt, "given Original Objects", vo);
        ArrayList<T> altChildren = new ArrayList<>();
        alt.forEach((t) -> {
            if (t instanceof Region) {
                ((Region) t).getBoundaryManager().getBoundaries()
                        .forEach(b -> altChildren.add((T) b));
            } else if (t instanceof GeometryPart) {
                ((PartSurface) t).getPartSurfaces()
                        .forEach(ps -> altChildren.add((T) ps));
            } else {
                altChildren.add(t);
            }
        });
        _io.say.objects(altChildren, "Children Objects found", vo);
        return altChildren;
    }

    /**
     * Gets a standard colormap shipped with STAR-CCM+.
     *
     * @param opt given Volume Mesher choice. See {@link macroutils.StaticDeclarations.Colormaps}
     *            for options.
     * @return The LookupTable.
     */
    public LookupTable colormap(StaticDeclarations.Colormaps opt) {
        return _sim.get(LookupTableManager.class).getObject(opt.getName());
    }

    /**
     * Gets a constant material property object to be manipulated by MacroUtils.
     *
     * @param <T> any Class that extends from MaterialProperty object in STAR-CCM+.
     * @param m   given {@link star.common.Model}.
     * @param clz given material property Class. E.g.: {@link star.flow.ConstantDensityProperty},
     *            etc...
     * @return The ConstantMaterialPropertyMethod.
     */
    public <T extends MaterialProperty> ConstantMaterialPropertyMethod constantMaterialProperty(
            Model m, Class<T> clz) {
        if (m instanceof SingleComponentMaterialModel) {
            SingleComponentMaterialModel scmm = (SingleComponentMaterialModel) m;
            MaterialPropertyManager mpp = scmm.getMaterial().getMaterialProperties();
            return (ConstantMaterialPropertyMethod) mpp.getMaterialProperty(clz).getMethod();
        }
        _io.say.msg("ConstantMaterialPropertyMethod is NULL.");
        return null;
    }

    /**
     * Gets a double[] array from an List of doubles.
     *
     * @param list given list of Doubles
     * @return The double[] array
     */
    public double[] doubleArray(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
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
     * Loops over all Field Functions and returns the first match based on the REGEX search pattern.
     * The search will be done on Function Name first and then on its pName on GUI, i.e., the
     * PresentationName.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(String regexPatt, boolean vo) {
        return byREGEX(regexPatt, "Field Function", _allFieldFunctions(), vo);
    }

    /**
     * Gets a Field Function based on the strings previously defined.
     *
     * @param var given predefined variable defined in {@link macroutils.StaticDeclarations} class.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(StaticDeclarations.Vars var) {
        FieldFunction ff = fieldFunction(var.getVar(), false);
        _io.say.value("Asked for Field Function", var.getVar(), true, true);
        if (ff == null) {
            _io.say.msg(true, "Returning NULL.");
        } else {
            _io.say.value("Returning", ff.getPresentationName(), true, true);
            if (var.equals(StaticDeclarations.Vars.VEL_MAG)) {
                ff = ff.getMagnitudeFunction();
            }
        }
        return ff;
    }

    /**
     * Loops over all Field Functions and return all matches based on the REGEX search pattern. The
     * search will be done on Function Name first and then on its pName on GUI, i.e., the
     * PresentationName.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of FieldFunctions.
     */
    public ArrayList<FieldFunction> fieldFunctions(String regexPatt, boolean vo) {
        return allByREGEX(regexPatt, "Field Functions", _allFieldFunctions(), vo);
    }

    /**
     * Gets the HardcopyProperties from a NamedObject.
     *
     * @param no given NamedObject. It can be a Plot or Scene.
     * @param vo given verbose option. False will not print anything.
     * @return The HardcopyProperties. Null if nothing is all.
     */
    public HardcopyProperties hardcopyProperties(NamedObject no, boolean vo) {
        if (no instanceof Scene) {
            return ((Scene) no).getSceneUpdate().getHardcopyProperties();
        } else if (no instanceof StarPlot) {
            return ((UpdatePlot) no).getPlotUpdate().getHardcopyProperties();
        }
        _io.say.msg(vo, "'%s' does not have a HardcopyProperties. Returning NULL...",
                no.getPresentationName());
        return null;
    }

    /**
     * Gets a Global Parameter that matches the REGEX search pattern among all Parameters available
     * in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The GlobalParameterBase. Null if nothing is all.
     */
    public GlobalParameterBase parameter(String regexPatt, boolean vo) {
        return byREGEX(regexPatt, "Global Parameter",
                new ArrayList<>(_sim.get(GlobalParameterManager.class).getObjects()), vo);
    }

    /**
     * Gets a Physics Continua that matches the REGEX search pattern among all Continuas available
     * in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The PhysicsContinuum. Null if nothing is all.
     */
    public PhysicsContinuum physicsContinua(String regexPatt, boolean vo) {
        return byREGEX(regexPatt, "Physics Continua", allPhysicsContinua(false), vo);
    }

    /**
     * Gets the Profile object.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object pName.
     * @param vo   given verbose option. False will not print anything.
     * @return The Profile. Null if nothing is all.
     */
    public Profile profile(ClientServerObjectManager csom, String name, boolean vo) {
        if (!csom.has(name)) {
            _io.say.msg(vo, "'%s' does not have a '%s' value. Returning NULL...",
                    csom.getPresentationName(), name);
            return null;
        }
        return (Profile) csom.getObject(name);
    }

    /**
     * Gets a ScalarProfile, if applicable.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object pName.
     * @param vo   given verbose option. False will not print anything.
     * @return The ScalarProfile. Null if nothing is all.
     */
    public ScalarProfile scalarProfile(ClientServerObjectManager csom, String name, boolean vo) {
        return (ScalarProfile) profile(csom, name, vo);
    }

    /**
     * Gets a Transform that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The VisTransform.
     */
    public VisTransform transform(String regexPatt, boolean vo) {
        return (VisTransform) _get.objects.byREGEX(regexPatt, "Transform",
                new ArrayList<>(_sim.getTransformManager().getObjects()), vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    /**
     * Gets a VectorProfile, if applicable.
     *
     * @param csom given ClientServerObjectManager.
     * @param name given object pName.
     * @param vo   given verbose option. False will not print anything.
     * @return The VectorProfile. Null if nothing is all.
     */
    public VectorProfile vectorProfile(ClientServerObjectManager csom, String name, boolean vo) {
        return (VectorProfile) profile(csom, name, vo);
    }

    private ArrayList<FieldFunction> _allFieldFunctions() {
        return new ArrayList<>(_sim.getFieldFunctionManager().getObjects());
    }

    private <T extends NamedObject> boolean _matches(T t, String regexPatt, boolean vo) {
        boolean matches = t.getPresentationName().matches(regexPatt);
        if (t instanceof FieldFunction) {
            FieldFunction ff = (FieldFunction) t;
            matches = ff.getFunctionName().matches(regexPatt) || matches;
        }
        if (matches) {
            _io.say.msg(vo, "  - Match: \"%s\".", t.getPresentationName(), vo);
        }
        return matches;
    }

}
