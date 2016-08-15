package macroutils.setter;

import macroutils.*;
import star.vis.*;

/**
 * Low-level class for setting MacroUtils user defaults defined in {@link UserDeclarations}.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetDefaults {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetDefaults(MacroUtils m) {
        _mu = m;
    }

    private void _setting(String what, String opt) {
        _io.say.action(String.format("Setting Default %s", what), true);
        _io.say.msg(true, "%s: \"%s\".", what, opt);
        _io.say.ok(true);
    }

    /**
     * Sets the default Camera View (VisView) used by MacroUtils.
     *
     * @param vv given VisView.
     */
    public void cameraView(VisView vv) {
        _setting("Camera View", vv.toString());
        _ud.defCamView = vv;
    }

    /**
     * Sets the default Colormap used by MacroUtils.
     *
     * @param opt given option. See {@link StaticDeclarations.Colormaps} for options.
     */
    public void colormap(StaticDeclarations.Colormaps opt) {
        _setting("Colormap", opt.getName());
        _ud.defColormap = _get.objects.colormap(opt);
    }

    /**
     * Sets the default picture path/folder when saving pictures with MacroUtils.
     *
     * @param path given path for storing pictures.
     */
    public void picturePath(String path) {
        _setting("Picture Path", path);
        _ud.picPath = _io.createFolder(path).toString();
    }

    /**
     * Sets the default resolution when saving pictures with MacroUtils.
     *
     * @param resx given resolution in x. This will change {@link UserDeclarations#picResX} variable.
     * @param resy given resolution in y. This will change {@link UserDeclarations#picResY} variable.
     */
    public void pictureResolution(int resx, int resy) {
        _setting("Picture resolution", String.format("%d x %d pixels", resx, resy));
        _ud.picResX = resx;
        _ud.picResY = resy;
    }

    /**
     * Sets the default Tessellation option used by MacroUtils.
     *
     * @param opt given option. See {@link StaticDeclarations.Tessellation} for options.
     */
    public void tessellation(StaticDeclarations.Tessellation opt) {
        _setting("Tessellation", opt.toString());
        _ud.defTessOpt = opt;
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
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
