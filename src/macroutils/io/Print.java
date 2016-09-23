package macroutils.io;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.vis.*;

/**
 * Low-level class for console/log outputs in general in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class Print {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public Print(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private String _getFMT(String format, Object... args) {
        return new Formatter().format(format, args).toString();
    }

    private String _getName(ClientServerObject cso) {
        if (cso instanceof GeometryPart) {
            return ((GeometryPart) cso).getPathInHierarchy();
        } else if (cso instanceof ColumnDescriptor) {
            return ((ColumnDescriptor) cso).getColumnName();
        }
        return cso.getPresentationName();
    }

    private void _say(String msg) {
        if (_sim == null) {
            System.out.println(msg);
            return;
        }
        _sim.println(msg);
    }

    /**
     * Prints a text action within a fancy frame.
     *
     * @param text message to be printed.
     * @param vo given verbose option. False will not print anything.
     */
    public void action(String text, boolean vo) {
        msg("", vo);
        line(vo);
        msg("+ " + _get.info.time(), vo);
        msg("+ " + text, vo);
        line(vo);
    }

    /**
     * Prints an Action followed by the Object name.
     *
     * @param something some action to be printed.
     * @param cso given STAR-CCM+ ClientServerObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void action(String something, ClientServerObject cso, boolean vo) {
        action(something, vo);
        object(cso, vo);
    }

    /**
     * Prints the details of a Camera View (VisView).
     *
     * @param cam given camera (VisView).
     * @param vo given verbose option. False will not print anything.
     */
    public void camera(VisView cam, boolean vo) {
        msg("Camera Overview: " + cam.getPresentationName(), vo);
        value("  - Focal Point", cam.getFocalPoint(), vo);
        value("  - Position", cam.getPosition(), vo);
        value("  - View Up", cam.getViewUp(), vo);
        value("  - Parallel Scale", cam.getParallelScale().getValue(), vo);
        value("  - Projection Mode", cam.getProjectionModeEnum().getPresentationName(), true, vo);
        msg("", vo);
    }

    /**
     * Prints the current cell count.
     */
    public void cellCount() {
        if (!_chk.has.volumeMesh()) {
            return;
        }
        String fmt = "  - %s: %,d.";
        msg("Mesh summary:", true);
        msg(true, fmt, "Cell Count", _get.mesh.fvr().getCellCount());
        msg(true, fmt, "Face Count", _get.mesh.fvr().getInteriorFaceCount());
        msg(true, fmt, "Vertex Count", _get.mesh.fvr().getVertexCount());
    }

    /**
     * Prints that something has been created followed by an "Ok" message.
     *
     * @param cso given ClientServerObject.
     * @param vo given verbose option. False will not print anything.
     */
    public void created(ClientServerObject cso, boolean vo) {
        value("Created " + _get.strings.parentName(cso), cso.getPresentationName(), true, vo);
        ok(vo);
    }

    /**
     * Prints the current Dimension.
     *
     * @param dim given Dimensions.
     * @param vo given verbose option. False will not print anything.
     */
    public void dimension(Dimensions dim, boolean vo) {
        msg(vo, "Dimension: %s.", dim.toString());
    }

    /**
     * Prints a line.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void line(boolean vo) {
        line(1, vo);
    }

    /**
     * Prints a line by a number of times.
     *
     * @param n given number of times the line will be repeated.
     * @param vo given verbose option. False will not print anything.
     */
    public void line(int n, boolean vo) {
        for (int i = 1; i <= n; i++) {
            line("-", vo);
        }
    }

    /**
     * Prints a line with a custom character.
     *
     * @param c given character to be printed as a line.
     * @param vo given verbose option. False will not print anything.
     */
    public void line(String c, boolean vo) {
        String cc = c;
        if (c.equals("-")) {
            cc = "+";
        }
        msg(cc + _get.strings.repeated(c, 120), vo);
    }

    /**
     * Prints a line when in debug mode.
     */
    public void lineDebug() {
        if (!_dbg) {
            return;
        }
        line(true);
    }

    /**
     * Prints something in capital letters.
     *
     * @param msg given message to be printed.
     */
    public void loud(String msg) {
        line("#", true);
        line("#", true);
        msg("#  " + msg.toUpperCase(), true);
        msg("#  " + _get.info.time(), true);
        line("#", true);
        line("#", true);
    }

    /**
     * Prints something in the console/output.
     *
     * @param msg given message to be printed.
     */
    public void msg(String msg) {
        msg(msg, true);
    }

    /**
     * Prints something in the console/output.
     *
     * @param msg given message to be printed.
     * @param vo given verbose option. False will not print anything.
     */
    public void msg(String msg, boolean vo) {
        String prefix = _msgPrefix;
        if (_dbg) {
            if (!vo) {
                prefix += _msgDbgPrefix;
            }
            vo = true;
        }
        if (!vo) {
            return;
        }
        _say(prefix + " " + msg);
    }

    /**
     * Prints something in the console/output using formatted strings.
     *
     * @param vo given verbose option. False will not print anything.
     * @param format given format using the {@link String} syntax.
     * @param args given arguments that must be tied to the given format.
     */
    public void msg(boolean vo, String format, Object... args) {
        if (format == null) {
            return;
        }
        msg(_getFMT(format, args), vo);
    }

    /**
     * Prints a line when in debug mode.
     *
     * @param msg given message to be printed.
     */
    public void msgDebug(String msg) {
        if (!_dbg) {
            return;
        }
        _say(_msgDbgPrefix + " " + msg);
    }

    /**
     * Prints something when in debug mode using formatted strings.
     *
     * @param format given format using the {@link String} syntax.
     * @param args given arguments that must be tied to the given format.
     */
    public void msgDebug(String format, Object... args) {
        String s = new Formatter().format(format, args).toString();
        msgDebug(s);
    }

    /**
     * Prints the STAR-CCM+ object name in the console/output.
     *
     * @param cso given STAR-CCM+ Client Server Object.
     * @param vo given verbose option. False will not print anything.
     */
    public void object(ClientServerObject cso, boolean vo) {
        String parentName = "NULL", csoName = "NULL";
        if (cso != null) {
            parentName = _get.strings.parentName(cso);
            csoName = cso.getPresentationName();
        }
        value(parentName + " name", csoName, true, vo);
    }

    /**
     * Prints the STAR-CCM+ object names in the console/output.
     *
     * @param aos given {@link ArrayList} containing objects in general.
     * @param what given type of the object. E.g.: "Boundary, Report, Scenes, etc...
     * @param vo given verbose option. False will not print anything.
     */
    public void objects(ArrayList aos, String what, boolean vo) {
        if (aos == null) {
            aos = new ArrayList();
        }
        msg(vo, "Number of %s: %d.", what, aos.size(), vo);
        for (Object o : aos) {
            String s = o.toString();
            if (o instanceof ClientServerObject) {
                s = _getName((ClientServerObject) o);
            }
            msg(vo, "  - \"%s\"", s);
        }
    }

    /**
     * Prints an "OK!".
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void ok(boolean vo) {
        msg("OK!\n", vo);
    }

    /**
     * Prints something with percentage values.
     *
     * @param what given String of what will be print.
     * @param val given percentage value.
     * @param vo given verbose option. False will not print anything.
     */
    public void percentage(String what, double val, boolean vo) {
        value(what, String.format("%g%%", val), false, vo);
    }

    /**
     * Prints the Scalar name and its Units, if applicable.
     *
     * @param ff given Field Function.
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void scalar(FieldFunction ff, Units u, boolean vo) {
        msg(vo, "Scalar: %s [%s].", ff.getPresentationName(), _get.strings.fromUnit(u));
    }

    /**
     * Sets the debug mode for console/output printing. Useful for extra messages.
     *
     * @param opt given option. True to enable.
     */
    public void setDebug(boolean opt) {
        String s = "Disabled";
        if (opt) {
            s = "Enabled";
        }
        //-- Initialization issues.
        try {
            action(String.format("Debug Mode %s!", s), true);
        } catch (Exception e) {
        }
        msgDebug("Debug mode enabled!");
        _dbg = opt;
    }

    /**
     * Prints the current Unit.
     *
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void unit(Units u, boolean vo) {
        msg(vo, "Unit: [%s].", _get.strings.fromUnit(u));
    }

    /**
     * This method is called automatically by {@link MainIO} class. It is internal to MacroUtils.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
    }

    /**
     * Prints something with Values.
     *
     * @param what given String of what will be print.
     * @param val given value.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, double val, boolean vo) {
        value(what, String.format("%g", val), false, vo);
    }

    /**
     * Prints something with Values.
     *
     * @param what given String of what will be print.
     * @param val given value.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, int val, boolean vo) {
        value(what, String.format("%d", val), false, vo);
    }

    /**
     * Prints something with Values.
     *
     * @param what given String of what will be print.
     * @param dv given values in DoubleVector format.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, DoubleVector dv, boolean vo) {
        value(what, _get.strings.withinTheBrackets(dv.toString()), false, vo);
    }

    /**
     * Prints something with Values and Units.
     *
     * @param what given String of what will be print.
     * @param val given value.
     * @param vo given verbose option. False will not print anything.
     * @param u given Units.
     */
    public void value(String what, double val, Units u, boolean vo) {
        value(what, String.format("%g", val), u, vo);
    }

    /**
     * Prints something with Values and Units.
     *
     * @param what given String of what will be print.
     * @param sval given value as string.
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, String sval, Units u, boolean vo) {
        if (what == null) {
            return;
        }
        msg(vo, "%s: %s [%s].", what, sval, _get.strings.fromUnit(u));
    }

    /**
     * Prints something with Values and Units.
     *
     * @param what given String of what will be print.
     * @param dv given values in DoubleVector format.
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, DoubleVector dv, Units u, boolean vo) {
        value(what, _get.strings.withinTheBrackets(dv.toString()), u, vo);
    }

    /**
     * Prints something with values double-quoted. E.g.: Variable: "Pressure".
     *
     * @param what given String of what will be print.
     * @param sval given value that will be print.
     * @param dq option to double quote the value.
     * @param vo given verbose option. False will not print anything.
     */
    public void value(String what, String sval, boolean dq, boolean vo) {
        if (what == null) {
            return;
        }
        if (dq) {
            msg(vo, "%s: \"%s\".", what, sval);
            return;
        }
        msg(vo, "%s: %s.", what, sval);
    }

    /**
     * Prints the current STAR-CCM+ version.
     */
    public void version() {
        msg(true, "STAR-CCM+ Version: %s.", _get.info.version());
    }

    //--
    //-- Variables declaration area.
    //--
    private final String _msgPrefix = StaticDeclarations.MSG_PREFIX;
    private final String _msgDbgPrefix = StaticDeclarations.MSG_DEBUG_PREFIX;

    private boolean _dbg = false;
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;

}
