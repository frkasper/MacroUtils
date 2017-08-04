package macroutils.setter;

import java.awt.Color;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.common.FieldFunction;
import star.common.Units;
import star.common.UpdateEvent;
import star.vis.BackgroundColorMode;
import star.vis.CameraStateInput;
import star.vis.Displayer;
import star.vis.Legend;
import star.vis.LookupTable;
import star.vis.PartColorMode;
import star.vis.PartDisplayer;
import star.vis.ScalarDisplayQuantity;
import star.vis.Scene;
import star.vis.VisTransform;
import star.vis.VisView;

/**
 * Low-level class for setting Scenes parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetScenes {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetScenes(MacroUtils m) {
        _mu = m;
    }

    /**
     * Sets the Scene Background as a Solid color.
     *
     * @param scn given Scene.
     * @param color given Color.
     * @param vo given verbose option. False will not print anything.
     */
    public void background(Scene scn, Color color, boolean vo) {
        _io.say.action("Setting Solid Background Color", vo);
        _io.say.value("Setting Solid Background Color on Scene", scn.getPresentationName(), true, vo);
        scn.setBackgroundColorMode(BackgroundColorMode.SOLID);
        scn.getSolidBackgroundColor().setColorColor(color);
        _io.say.ok(vo);
    }

    /**
     * Sets the Camera View in the Scene.
     *
     * @param scn given Scene.
     * @param vv given Camera View setup.
     * @param vo given verbose option. False will not print anything.
     */
    public void cameraView(Scene scn, VisView vv, boolean vo) {
        if (vv == null) {
            return;
        }
        scn.getCurrentView().setInput(new CameraStateInput(vv), vv.getCoordinateSystem(), true);
        scn.getCurrentView().setProjectionMode(vv.getProjectionModeEnum());
        _io.say.value("Camera View set to", vv.getPresentationName(), true, vo);
    }

    /**
     * Sets the Geometry Displayer to a custom Solid color.
     *
     * @param d given Displayer.
     * @param color given Color.
     * @param vo given verbose option. False will not print anything.
     */
    public void displayer(Displayer d, Color color, boolean vo) {
        _io.say.value("Setting a Solid Color on Displayer", d.getPresentationName(), true, vo);
        if (!(d instanceof PartDisplayer)) {
            _io.say.msg("Not a PartDisplayer.", vo);
            return;
        }
        PartDisplayer pd = (PartDisplayer) d;
        pd.setColorMode(PartColorMode.CONSTANT);
        pd.setDisplayerColorColor(color);
        _io.say.ok(vo);
    }

    /**
     * Sets some Displayer enhancements suggested by MacroUtils.
     *
     * @param d given Displayer.
     */
    public void displayerEnhancements(Displayer d) {
        Legend leg = _get.scenes.legend(d, false);
        if (leg == null) {
            return;
        }
        //--
        final String label_0dec = "%.0f";
        final String label_1dec = "%.1f";
        final String label_2dec = "%.2f";
        //--
        //-- STAR-CCM+ defaults.
        //--
        final DoubleVector defLegPos = new DoubleVector(new double[]{0.3, 0.05});
        // DoubleVector of -> Width / Height / Title Height / Label Height
        final DoubleVector defLegSettings = new DoubleVector(new double[]{0.6, 0.044, 0.0275, 0.024});
        final int defLegNumberOfLabels = 6;
        final int defLegColorLevels = 32;
        final String defLegFont = "Lucida Sans-italic";
        final String defLegFmt = "%-#6.5g";
        //--
        //-- Suggested values.
        //--
        int legNumberOfLabels = defLegNumberOfLabels;
        int legColorLevels = 128;
        String legLabelFont = "Lucida Sans Typewriter-italic";
        String legLabelFmt = "%-#6.3g";
        DoubleVector legSettings = new DoubleVector(new double[]{0.6, 0.05, 0.04, 0.03});
        DoubleVector dvLegPos = new DoubleVector(new double[]{0.225, 0.01});
        LookupTable colormap = _ud.defColormap;
        //--
        ScalarDisplayQuantity sdq = _get.scenes.scalarDisplayQuantity(d, false);
        if (sdq != null) {
            sdq.setClip(false);
            FieldFunction ff = sdq.getFieldFunction();
            String fName = ff.getPresentationName();
            Units u = sdq.getUnits();
            boolean ismmH2O = u == _ud.unit_mmH2O;
            boolean isPa = u == _ud.unit_Pa;
            boolean isPC = fName.matches(StaticDeclarations.Vars.PC.getVar());
            boolean isP = fName.matches(".*Pressure.*");
            boolean isT = fName.matches(".*Temperature.*");
            boolean isTVR = fName.matches(StaticDeclarations.Vars.TVR.getVar());
            boolean isVF = fName.matches("Volume.*Fraction.*");
            boolean isYP = ff == _get.objects.fieldFunction(StaticDeclarations.Vars.YPLUS.getVar(), false);
            if (isVF && _chk.has.VOF()) {
                legColorLevels = 16;
                legNumberOfLabels = 3;
                legSettings.set(0, 0.4);
                dvLegPos = new DoubleVector(new double[]{0.32, 0.01});
                colormap = _get.objects.colormap(StaticDeclarations.Colormaps.BLUE_RED);
            }
            if (ismmH2O) {
                legLabelFmt = label_0dec;
            } else if (isVF || isT || isTVR || isPa || isYP) {
                legLabelFmt = label_1dec;
            } else if (isPC || isP) {
                legLabelFmt = label_2dec;
            }
        }
        //--
        //--
        leg.setShadow(false);
        leg.setLookupTable(colormap);
        DoubleVector dv = new DoubleVector(new double[]{leg.getWidth(),
            leg.getHeight(), leg.getTitleHeight(), leg.getLabelHeight()});
        if (dv.equals(defLegSettings)) {
            leg.setWidth(legSettings.get(0));
            leg.setHeight(legSettings.get(1));
            leg.setTitleHeight(legSettings.get(2));
            leg.setLabelHeight(legSettings.get(3));
        }
        if (leg.getFontString().equals(defLegFont)) {
            leg.setFontString(legLabelFont);
        }
        if (leg.getLabelFormat().equals(defLegFmt)) {
            leg.setLabelFormat(legLabelFmt);
        }
        if (leg.getNumberOfLabels() == defLegNumberOfLabels) {
            leg.setNumberOfLabels(legNumberOfLabels);
        }
        if (leg.getLevels() == defLegColorLevels) {
            leg.setLevels(legColorLevels);
        }
        if (leg.getPositionCoordinate().equals(defLegPos)) {
            leg.setPositionCoordinate(dvLegPos);
        }
        if (leg.getPositionCoordinate().equals(defLegPos)) {
            leg.setPositionCoordinate(dvLegPos);
        }
    }

    /**
     * Sets a Scene to save a PNG picture with a given resolution. Pictures will be saved on
     * {@link UserDeclarations#simPath} under a folder called <b>pics_<i>ObjectName</i></b>.
     *
     * @param scn given Scene.
     * @param resx given width pixel resolution.
     * @param resy given height pixel resolution.
     */
    public void saveToFile(Scene scn, int resx, int resy) {
        _set.object.saveToFile(scn, resx, resy, true);
    }

    /**
     * Sets a Transform on a Displayer.
     *
     * @param d given Displayer.
     * @param vt given VisTransform.
     * @param vo given verbose option. False will not print anything.
     */
    public void transform(Displayer d, VisTransform vt, boolean vo) {
        if (d == null || vt == null) {
            return;
        }
        _io.say.action("Setting a Visualization Transform", vo);
        _io.say.object(d, vo);
        _io.say.object(vt, vo);
        d.setVisTransform(vt);
        _io.say.ok(vo);
    }

    /**
     * Sets an Update Event for a Scene.
     *
     * @param scn given Scene.
     * @param ue given UpdateEvent.
     */
    public void updateEvent(Scene scn, UpdateEvent ue) {
        _set.object.updateEvent(scn, ue, true);
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

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.UserDeclarations _ud = null;

}
