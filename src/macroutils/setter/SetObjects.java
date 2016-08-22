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

    private void _setSPQ(ScalarPhysicalQuantity spq, double val, Units u, String text, boolean vo) {
        physicalQuantity(spq, val, null, u, text, vo);
    }

    private void _setUE(StarUpdate su, UpdateEvent ue) {
        if ((su.getParent() instanceof Scene) || (su.getParent() instanceof AutoSave)) {
            _setWorkAround(su, ue);
            return;
        }
        su.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.EVENT);
        su.getEventUpdateFrequency().setUpdateEvent(ue);
    }

    private void _setWorkAround(StarUpdate su, UpdateEvent ue) {
        //-- Check later.
        // Workaround for the error below found in v11.04.
        //    error: Server Error
        //    expression: _starUpdate
        //    file: Monitor.cpp
        //    line: 331
        // StarUpdate unexpectedly missing.
        // Command: RunSimulation
        su.setEnabled(true);
        String sn = su.getParent().getPresentationName();
        _io.say.msg(true, "Workaround on Update Event for \"%s\":", sn);
        String s2 = "  - Reverted to \"%s\" Update Mode.";
        String s3 = "  - Was not reverted. Risk of StarUpdate error.";
        if (ue instanceof FrequencyMonitorUpdateEvent) {
            FrequencyMonitorUpdateEvent fmue = (FrequencyMonitorUpdateEvent) ue;
            if (fmue.getMonitor() instanceof IterationMonitor) {
                su.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.ITERATION);
                IterationUpdateFrequency iuf = su.getIterationUpdateFrequency();
                iuf.setIterations(fmue.getSampleFrequency());
                iuf.setStart(fmue.getStartCount());
                _io.say.msg(true, s2, iuf.getBeanDisplayName());
            } else if (fmue.getMonitor() instanceof PhysicalTimeMonitor) {
                su.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.TIMESTEP);
                TimeStepUpdateFrequency tuf = su.getTimeStepUpdateFrequency();
                tuf.setTimeSteps(fmue.getSampleFrequency());
                tuf.setStart(fmue.getStartCount());
                _io.say.msg(true, s2, tuf.getBeanDisplayName());
            } else {
                _io.say.msg(true, s3);
            }
        } else if (ue instanceof DeltaMonitorUpdateEvent) {
            DeltaMonitorUpdateEvent dmue = (DeltaMonitorUpdateEvent) ue;
            if (dmue.getMonitor() instanceof PhysicalTimeMonitor) {
                su.getUpdateModeOption().setSelected(StarUpdateModeOption.Type.DELTATIME);
                DeltaTimeUpdateFrequency dtuf = su.getDeltaTimeUpdateFrequency();
                dtuf.setDeltaTimeInput(dmue.getDeltaThresholdInput());
                _io.say.msg(true, s2, dtuf.getBeanDisplayName());
            }
        } else {
            _io.say.msg(true, s3);
        }
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
        _setSPQ(pcp.getReferenceDensity(), refDen, _ud.defUnitDen, "Reference Density", vo);
        _setSPQ(pcp.getReferencePressure(), refP, _ud.defUnitPress, "Reference Pressure", vo);
        _setSPQ(pcp.getReferenceVelocity(), refVel, _ud.defUnitVel, "Reference Velocity", vo);
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
        if (sp == null) {
            return;
        }
        sp.setMethod(ConstantScalarProfileMethod.class);
        ScalarPhysicalQuantity spq = sp.getMethod(ConstantScalarProfileMethod.class).getQuantity();
        physicalQuantity(spq, val, null, u, sp.getPresentationName(), true);
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
        physicalQuantity(vpq, vals, u, null, true);
    }

    /**
     * Sets a Scalar Physical Quantities with this method, if applicable. It will also print something like: "Text:
     * value unit" in the output.
     *
     * @param spq given ScalarPhysicalQuantity object.
     * @param val given value.
     * @param def given definition. <b>null</b> to ignore.
     * @param u given Units
     * @param text given text. <b>null</b> to ignore.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(ScalarPhysicalQuantity spq, double val, String def, Units u, String text, boolean vo) {
        spq.setValue(val);
        String s = String.valueOf(spq.getRawValue());
        if (def != null) {
            spq.setDefinition(def);
            s = spq.getDefinition();
        }
        spq.setUnits(u);
        _io.say.msgDebug("physicalQuantity():");
        _io.say.msgDebug("  - val: %g", val);
        _io.say.msgDebug("  - def: %s", def);
        _io.say.msgDebug("  - Units: %s", _get.strings.fromUnit(u));
        _io.say.value(text, s, u, vo);
    }

    /**
     * Sets a constant Vector Physical Quantities with this method, if applicable. It will also print something like:
     * "Text: val1, val2, val3 unit".
     *
     * @param vpq given VectorPhysicalQuantity object.
     * @param vals given array of constant values.
     * @param u given Units.
     * @param text given text. <b>null</b> to use its name as in the GUI.
     * @param vo given verbose option. False will not print anything.
     */
    public void physicalQuantity(VectorPhysicalQuantity vpq, double[] vals, Units u, String text, boolean vo) {
        vpq.setConstant(vals);
        if (u != null) {
            vpq.setUnits(u);
        }
        if (text == null) {
            text = vpq.getParent().getParent().getPresentationName();
        }
        _io.say.value(text, _get.strings.fromArray(vals), u, vo);
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
        _io = _mu.io;
        _get = _mu.get;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.getter.MainGetter _get = null;

}
