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
        _tmpl.print.getByREGEX(what, regexPatt, vo);
        if (ano.isEmpty()) {
            _tmpl.print.isEmpty(vo);
        } else {
            for (NamedObject no : ano) {
                if (!no.getPresentationName().matches(regexPatt)) {
                    continue;
                }
                _io.say.msg(vo, "  - Match: \"%s\"", no.getPresentationName(), vo);
                arr.add(no);
            }
            _io.say.msg(vo, "Found %d items.", arr.size());
        }
        if (arr.isEmpty()) {
            return new ArrayList(arrayList(null));
        }
        return arr;
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
     * Gets a STAR-CCM+ NamedObject by matching its Presentation Name.
     *
     * @param name given object name.
     * @param ano given ArrayList of NamedObjects.
     * @param vo given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is found.
     */
    public NamedObject byName(String name, ArrayList<NamedObject> ano, boolean vo) {
        String s = ano.get(0).getParent().getBeanDisplayName();
        return byName(name, s, ano, vo);
    }

    /**
     * Gets a STAR-CCM+ NamedObject by matching its Presentation Name.
     *
     * @param name given object name.
     * @param ano given ArrayList of NamedObjects.
     * @param what what kind of object? E.g: Plane, Report, Scene, etc...
     * @param vo given verbose option. False will not print anything.
     * @return The NamedObject. Null if nothing is found.
     */
    public NamedObject byName(String name, String what, ArrayList<NamedObject> ano, boolean vo) {
        _tmpl.print.getByName(what, name, vo);
        if (ano.isEmpty()) {
            _tmpl.print.gotNull(vo);
        } else {
            for (NamedObject no : ano) {
                if (no.getPresentationName().equals(name)) {
                    _tmpl.print.gotByName(no, vo);
                    return no;
                }
            }
        }
        return null;
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
        _tmpl.print.getByREGEX(what, regexPatt, vo);
        if (ano.isEmpty()) {
            _tmpl.print.gotNull(vo);
        } else {
            for (NamedObject no : ano) {
                if (no.getPresentationName().matches(regexPatt)) {
                    _tmpl.print.gotByREGEX(what, no, vo);
                    return no;
                }
            }
        }
        return null;
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
        _tmpl.print.getByREGEX("Field Function", regexPatt, vo);
        for (FieldFunction ff : _sim.getFieldFunctionManager().getObjects()) {
            if (ff.getFunctionName().matches(regexPatt)) {
                _io.say.msg(vo, "Got by REGEX: \"%s\".", ff.getFunctionName());
                return ff;
            }
            if (ff.getPresentationName().matches(regexPatt)) {
                _io.say.msg(vo, "Got by REGEX: \"%s\".", ff.getPresentationName());
                return ff;
            }
        }
        _io.say.msg("Got NULL.", vo);
        return null;
    }

    /**
     * Gets a Field Function based on the ones defined in {@link StaticDeclarations} class.
     *
     * @param var given predefined variable enum.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(StaticDeclarations.Vars var) {
        FieldFunction ff = fieldFunction(var.getVar(), false);
        _io.say.msg(true, "Asked for Field Function: \"%s\".", var.getVar());
        if (ff == null) {
            _io.say.msg(true, "Returning NULL.");
            return null;
        }
        _io.say.msg(true, "Returning: \"%s\".", ff.getPresentationName());
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
        _tmpl.print.getByREGEX("Field Functions", regexPatt, vo);
        ArrayList<FieldFunction> af = new ArrayList();
        for (FieldFunction ff : _sim.getFieldFunctionManager().getObjects()) {
            if (ff.getFunctionName().matches(regexPatt)) {
                _io.say.msg(vo, "   - Found: \"%s\".", ff.getFunctionName());
                af.add(ff);
                continue;
            }
            if (ff.getPresentationName().matches(regexPatt)) {
                _io.say.msg(vo, "   - Found: \"%s\".", ff.getPresentationName());
                af.add(ff);
            }
        }
        _io.say.msg(vo, "Field Functions found: %d", af.size());
        return af;
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
        _tmpl = _mu.templates;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
