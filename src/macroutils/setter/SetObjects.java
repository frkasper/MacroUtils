package macroutils.setter;

import java.io.*;
import macroutils.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.flow.*;
import star.post.*;
import star.vis.*;

/**
 * Low-level class for setting objects in general in STAR-CCM+ with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetObjects {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetObjects(MacroUtils m) {
        _mu = m;
    }

    public void _setSP(ScalarProfile sp, double val, Units u, String def) {
        if (sp == null) {
            return;
        }
        sp.setMethod(ConstantScalarProfileMethod.class);
        ScalarPhysicalQuantity spq = sp.getMethod(ConstantScalarProfileMethod.class).getQuantity();
        _setSPQ(spq, val, u, sp.getPresentationName(), def, true);
    }

    private void _setSPQ(ScalarPhysicalQuantity spq, double val, Units u, String text, String def, boolean vo) {
        spq.setValue(val);
        String s = String.valueOf(spq.getRawValue());
        if (def != null) {
            spq.setDefinition(def);
            _io.say.value(text, spq.getDefinition(), true, vo);
            return;
        }
        if (u != null) {
            spq.setUnits(u);
        }
        _io.say.value(text, s, u, vo);
    }

    private void _setVPQ(VectorPhysicalQuantity vpq, double[] vals, Units u, String text, String def, boolean vo) {
        if (text == null) {
            text = vpq.getParent().getParent().getPresentationName();
        }
        if (u != null) {
            vpq.setUnits(u);
        }
        if (def == null) {
            vpq.setConstant(vals);
            _io.say.value(text, _get.strings.fromArray(vals), vpq.getUnits(), vo);
        } else {
            vpq.setDefinition(def);
            _io.say.value(text, vpq.getDefinition(), true, vo);
        }
    }

    private void _setUE(StarUpdate su, UpdateEvent ue) {
        su.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.EVENT);
        su.getEventUpdateFrequency().setUpdateEvent(ue);
    }

    private void _setWU(WindowUpdate wu, String pp, int resx, int resy) {
        wu.setSaveAnimation(true);
        wu.setAnimationFilePath(new File(_ud.simPath, pp));
        wu.setAnimationFilenameBase("pic");
        wu.getHardcopyProperties().setUseCurrentResolution(false);
        wu.getHardcopyProperties().setOutputWidth(resx);
        wu.getHardcopyProperties().setOutputHeight(resy);
    }

    public void fieldFunctionPressureCoefficient(double refDen, double refP, double refVel, boolean vo) {
        _io.say.action("Setting Pressure Coefficient Field Function", vo);
        FieldFunction ff = _get.objects.fieldFunction(StaticDeclarations.Vars.PC.getVar(), false);
        PressureCoefficientFunction pcp = (PressureCoefficientFunction) ff;
        _setSPQ(pcp.getReferenceDensity(), refDen, _ud.defUnitDen, "Reference Density", null, vo);
        _setSPQ(pcp.getReferencePressure(), refP, _ud.defUnitPress, "Reference Pressure", null, vo);
        _setSPQ(pcp.getReferenceVelocity(), refVel, _ud.defUnitVel, "Reference Velocity", null, vo);
        _io.say.ok(vo);
    }

    /**
     * Sets a Constant Scalar Physical Quantities with this method, if applicable. It will also print something like: "Text:
     * value unit" in the output.
     *
     * @param spq given ScalarPhysicalQuantity object.
     * @param val given value.
     * @param u given Units.
     * @param text given text. <b>null</b> to ignore.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(ScalarPhysicalQuantity spq, double val, Units u, String text, boolean vo) {
        _setSPQ(spq, val, u, text, null, vo);
    }

    /**
     * Sets a Definition for a Scalar Physical Quantities with this method, if applicable. It will also print something
     * like: "Text: value unit" in the output.
     *
     * @param spq given ScalarPhysicalQuantity object.
     * @param def given definition.
     * @param text given text. <b>null</b> to ignore.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(ScalarPhysicalQuantity spq, String def, String text, boolean vo) {
        _setSPQ(spq, 0.0, null, text, def, vo);
    }

    /**
     * Sets a Constant Vector Physical Quantities with this method, if applicable. It will also print something like:
     * "Text: val1, val2, val3 unit".
     *
     * @param vpq given VectorPhysicalQuantity object.
     * @param vals given array of constant values.
     * @param u given Units.
     * @param text given text. <b>null</b> to use its name as in the GUI.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(VectorPhysicalQuantity vpq, double[] vals, Units u, String text, boolean vo) {
        _setVPQ(vpq, vals, u, text, null, vo);
    }

    /**
     * Sets a Definition for the Vector Physical Quantities with this method, if applicable. It will also print
     * something like: "Text: val1, val2, val3".
     *
     * @param vpq given VectorPhysicalQuantity object.
     * @param def given definition.
     * @param text given text. <b>null</b> to use its name as in the GUI.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(VectorPhysicalQuantity vpq, String def, String text, boolean vo) {
        _setVPQ(vpq, StaticDeclarations.COORD0, null, text, def, vo);
    }

    /**
     * Sets the axes names for a Plot.
     *
     * @param sp given StarPlot.
     * @param xName given x-axis titles.
     * @param yName given y-axis titles.
     * @param vo given verbose option. False will not print anything.
     */
    public void plotAxesTitles(StarPlot sp, String xName, String yName, boolean vo) {
        _io.say.action("Setting Plot Axes Titles", vo);
        _io.say.object(sp, vo);
        Cartesian2DAxis xax = _get.plots.axisX(sp);
        Cartesian2DAxis yax = _get.plots.axisY(sp);
        xax.getTitle().setText(xName);
        yax.getTitle().setText(yName);
        _io.say.value("X-Axis", xax.getTitle().getText(), true, true);
        _io.say.value("Y-Axis", yax.getTitle().getText(), true, true);
        _io.say.ok(vo);
    }

    /**
     * Sets a constant Scalar Profile for a STAR-CCM+ object.
     *
     * @param sp given ScalarProfile.
     * @param val given constant value.
     * @param u given Units.
     */
    public void profile(ScalarProfile sp, double val, Units u) {
        _setSP(sp, val, u, null);
    }

    /**
     * Sets a constant Scalar Profile for a STAR-CCM+ object based on a definition.
     *
     * @param sp given ScalarProfile.
     * @param def given definition.
     */
    public void profile(ScalarProfile sp, String def) {
        _setSP(sp, 0, null, def);
    }

    /**
     * Sets a constant Vector Profile for a STAR-CCM+ object.
     *
     * @param vp given VectorProfile.
     * @param vals given array of constant values.
     * @param u given Units.
     */
    public void profile(VectorProfile vp, double[] vals, Units u) {
        if (vp == null) {
            return;
        }
        vp.setMethod(ConstantVectorProfileMethod.class);
        VectorPhysicalQuantity vpq = vp.getMethod(ConstantVectorProfileMethod.class).getQuantity();
        _setVPQ(vpq, vals, u, null, null, true);
    }

    /**
     * Sets a Plot or Scene to save a PNG picture with a given resolution. Pictures will be saved on
     * {@link UserDeclarations#simPath} under a folder called <b>pics_<i>ObjectName</i></b>.
     *
     * @param no given STAR-CCM+ NamedObject. It can be a Plot or Scene.
     * @param resx given width pixel resolution.
     * @param resy given height pixel resolution.
     * @param vo given verbose option. False will not print anything.
     */
    public void saveToFile(NamedObject no, int resx, int resy, boolean vo) {
        _io.say.action("Setting Save To File", vo);
        _io.say.object(no, vo);
        String picsPath = "pics_" + _get.strings.friendlyFilename(no.getPresentationName());
        if (no instanceof Scene) {
            _setWU(((Scene) no).getSceneUpdate(), picsPath, resx, resy);
        } else if (no instanceof StarPlot) {
            _setWU(((UpdatePlot) no).getPlotUpdate(), picsPath, resx, resy);
        }
        _io.say.ok(vo);
    }

    /**
     * Sets an Update Event to an Object, if applicable. Object can be any Plot, Monitor, Scene or Solution History.
     *
     * @param cso given STAR-CCM+ NamedObject.
     * @param ue given UpdateEvent.
     * @param vo given verbose option. False will not print anything.
     */
    public void updateEvent(ClientServerObject cso, UpdateEvent ue, boolean vo) {
        _io.say.action("Setting an Update Event", vo);
        _io.say.object(cso, vo);
        _io.say.object(ue, vo);
        if (cso instanceof Report) {
            ReportMonitor rm = _get.monitors.fromReport((Report) cso, false);
            if (rm != null) {
                cso = rm;
            }
        }
        if (cso instanceof StarPlot) {
            _setUE(((UpdatePlot) cso).getPlotUpdate(), ue);
        } else if (cso instanceof Scene) {
            _setUE(((Scene) cso).getSceneUpdate(), ue);
        } else if (cso instanceof Monitor) {
            _setUE(((Monitor) cso).getStarUpdate(), ue);
        } else if (cso instanceof SolutionHistory) {
            _setUE(((SolutionHistory) cso).getUpdate(), ue);
        }
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.UserDeclarations _ud = null;

}
