package macroutils.templates;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.meshing.*;

/**
 * Low-level class for output/printing templates with MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class TemplatePrint {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplatePrint(MacroUtils m) {
        _mu = m;
    }

    /**
     * Prints an Action followed by the Object name.
     *
     * @param something some action to be printed.
     * @param no given STAR-CCM+ NamedObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void actionAndObject(String something, NamedObject no, boolean vo) {
        _io.say.action(something, vo);
        _io.say.object(no, vo);
    }

    /**
     * Prints an overview of the base mesh parameters, i.e., Base Size, Min and Target.
     *
     * @param amo given AutoMeshOperation.
     * @param vo given verbose option. False will not print anything.
     */
    public void baseMeshParameters(AutoMeshOperation amo, boolean vo) {
        BaseSize bs = _get.mesh.baseSize(amo, false);
        _io.say.value("Base Size", bs.getRawValue(), bs.getUnits(), vo);
        double min = 0.;
        if (_get.mesh.minRelativeSize(amo, false) != null) {
            min = _get.mesh.minRelativeSize(amo, false).getPercentage();
        }
        minTarget(min, _get.mesh.targetRelativeSize(amo, false).getPercentage(), vo);
    }

    /**
     * Prints a "Created something" followed by an "Ok" message.
     *
     * @param cso given Client Server Object.
     * @param vo given verbose option. False will not print anything.
     */
    public void created(ClientServerObject cso, boolean vo) {
        _io.say.msg(vo, "Created %s: \"%s\".", _get.strings.parentName(cso), cso.getPresentationName());
        _io.say.ok(vo);
    }

    /**
     * Prints a custom "doing something" message.
     *
     * @param what doing what?
     * @param cso given Client Server Object, which will have the name printed.
     * @param vo given verbose option. False will not print anything.
     */
    public void doing(String what, ClientServerObject cso, boolean vo) {
        _io.say.msg(vo, "%s: \"%s\".", what, cso.getPresentationName());
    }

    /**
     * Prints a "Getting all of-something" message.
     *
     * @param something what will be looked for.
     * @param ano given ArrayList of STAR-CCM+ NamedObject's.
     * @param vo given verbose option. False will not print anything.
     */
    public void getAll(String something, ArrayList<NamedObject> ano, boolean vo) {
        _io.say.msg(vo, "Getting all %s...", something);
        _io.say.msg(vo, "%s found: %d.", something, ano.size());
    }

    /**
     * Prints a "Getting something by name" message.
     *
     * @param something what will be looked for.
     * @param name given name.
     * @param vo given verbose option. False will not print anything.
     */
    public void getByName(String something, String name, boolean vo) {
        _io.print.msg(vo, "Getting %s by name match: \"%s\".", something, name);
    }

    /**
     * Prints a "Getting something by REGEX pattern" message.
     *
     * @param something what will be looked for.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     */
    public void getByREGEX(String something, String regexPatt, boolean vo) {
        _io.print.msg(vo, "Getting %s by REGEX pattern: \"%s\".", something, regexPatt);
    }

    /**
     * Prints a "Got" message.
     *
     * @param no given STAR-CCM+ NamedObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void got(NamedObject no, boolean vo) {
        _io.print.msg(vo, "Got: \"%s\".", no.getPresentationName());
    }

    /**
     * Prints a "Got by name" message.
     *
     * @param no given STAR-CCM+ NamedObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void gotByName(NamedObject no, boolean vo) {
        _io.print.msg(vo, "Got by name: \"%s\".", no.getPresentationName());
    }

    /**
     * Prints a "Got by REGEX pattern" message.
     *
     * @param what what kind of object? E.g: Plane, Report, Scene, etc...
     * @param no given STAR-CCM+ NamedObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void gotByREGEX(String what, NamedObject no, boolean vo) {
        _io.print.msg(vo, "%s got by REGEX: \"%s\".", what, no.getPresentationName());
    }

    /**
     * Prints a "Got Null" message.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void gotNull(boolean vo) {
        _io.print.msg("Got NULL.", vo);
    }

    /**
     * Prints a message followed by "returned Null".
     *
     * @param msg given prefix message.
     * @param vo given verbose option. False will not print anything.
     */
    public void gotNull(String msg, boolean vo) {
        _io.print.msg(vo, "%s returned NULL.", msg, vo);
    }

    /**
     * Prints a "Is empty statement" message.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void isEmpty(boolean vo) {
        _io.print.msg(vo, "ArrayList is empty.");
    }

    /**
     * Prints a "Min/Target = values" percentage statement.
     *
     * @param min given minimum value.
     * @param tgt given target value.
     * @param vo given verbose option. False will not print anything.
     */
    public void minTarget(double min, double tgt, boolean vo) {
        _io.print.msg(vo, "Minimum / Target sizes (%%) = %.2f / %.2f [%%].", min, tgt);
    }

    /**
     * Prints an "Nothing changed" followed by a custom message.
     *
     * @param msg given extra message.
     * @param vo given verbose option. False will not print anything.
     */
    public void nothingChanged(String msg, boolean vo) {
        _io.print.msg(vo, "Nothing changed! %s", msg);
    }

    /**
     * Prints an "OK!" message.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void ok(boolean vo) {
        _io.print.msg("OK!\n", vo);
    }

    /**
     * Prints an overview of Prisms parameters.
     *
     * @param numLayers given number of prisms.
     * @param stretch given prism stretch relation.
     * @param relSize given relative size in (%).
     * @param vo given verbose option. False will not print anything.
     */
    public void prismsParameters(int numLayers, double stretch, double relSize, boolean vo) {
        _io.print.msg(vo, "Prism Parameters:", numLayers);
        if (numLayers > 0) {
            _io.print.msg(vo, "  Number of Layers: %d.", numLayers);
        }
        if (stretch > 0) {
            _io.print.msg(vo, "  Stretch Factor: %.2f.", stretch);
        }
        if (relSize > 0) {
            _io.print.msg(vo, "  Height Relative Size: %.2f%%.", relSize);
        }
    }

    /**
     * Prints an overview of Prisms parameters.
     *
     * @param numLayers given number of prisms.
     * @param thicknThr given Custom Thickness Threshold Size in (%).
     * @param vo given verbose option. False will not print anything.
     */
    public void thinMeshParameters(int numLayers, double thicknThr, boolean vo) {
        _io.print.msg(vo, "Thin Mesher Parameters:", numLayers);
        _io.print.msg(vo, "  Number of Thin Layers: %d.", numLayers);
        _io.print.msg(vo, "  Custom Thickness Threshold Size: %.2f%%.", thicknThr);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
