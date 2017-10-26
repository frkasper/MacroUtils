package macroutils.templates;

import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.FieldFunction;
import star.common.Region;
import star.common.Simulation;
import star.post.RecordedSolutionView;
import star.vis.Scene;
import star.vis.VisView;

/**
 * Low-level class for creating useful post in general with MacroUtils.
 *
 * @since May of 2016
 * @author Fabio Kasper
 */
public class TemplatePost {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplatePost(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private void _flyOver(Scene scn, VisView v1, VisView v2, int frames,
            ArrayList<VisView> avv, RecordedSolutionView rsv) {
        _frames = frames;
        int maxState = 10000;
        if (rsv != null) {
            maxState = rsv.getMaxStateIndex();
        }
        String cam1 = "null";
        String cam2 = "null";
        int nOldFrame = _currentFrame;
        if (_currentFrame > maxState) {
            _io.say.msg("Finished frames.");
            return;
        }
        if (v1 != null) {
            _set.scene.cameraView(scn, v1, false);
            cam1 = v1.getPresentationName();
        }
        if (v1 != null && v2 != null) {
            cam2 = v2.getPresentationName();
            avv.addAll(_get.cameras.inBetween_Linear(v1, v2, frames, false));
        }
        _ud.picPath = _io.createFolder("pics_" + _ud.simTitle).toString();
        for (int i = 0; i < frames; i++) {
            int picNumber = i + nOldFrame;
            if (rsv != null) {
                _io.say.lineDebug();
                _io.say.msgDebug("Setting: \"%s\".", rsv.getStateName());
                _io.say.msgDebug("State Index: %d.", rsv.getStateIndex());
                rsv.setStateIndex(rsv.getStateIndex() + 1);
            }
            if (!avv.isEmpty()) {
                _set.scene.cameraView(scn, avv.get(i + 1), false);
            }
            String picName = String.format("pic%04d_Cam_%s_to_%s.png", picNumber, cam1, cam2);
            _io.say.msgDebug("Saving: \"%s\"...", picName);
            flyOver_prePrintPicture();
            _io.write.picture(scn, picName, _ud.picResX, _ud.picResY, false);
            _currentFrame++;
        }
        flyOver_postPrintPicture();
        _currentFrame = nOldFrame + frames;
        _removeTemporaryCameraViews(false);
        _reset.picPath();
    }

    private boolean _isUnsteady() {
        if (_chk.is.unsteady()) {
            return true;
        }
        _io.say.msg("Simulation is not Unsteady.", true);
        return false;
    }

    private void _removeTemporaryCameraViews(boolean vo) {
        ArrayList<VisView> avv = _get.cameras.allByREGEX(StaticDeclarations.TMP_CAM_NAME + ".*", false);
        int n = avv.size();
        if (avv.size() > 0 && avv.get(0) != null) {
            _io.say.msg("Removing Temporary Cameras", vo);
            _sim.getViewManager().removeObjects(avv);
            _io.say.msg(vo, "Temporary Cameras Removed: %d.", n);
        }
    }

    /**
     * Flies over between two cameras and print pictures in between. The current picture number can be accessed by
     * {@link TemplatePost#getCurrentFrame}. Picture resolution is controlled by {@link UserDeclarations#picResX} and
     * and {@link UserDeclarations#picResX}.
     *
     * @param scn given Scene.
     * @param v1 given Camera 1.
     * @param v2 given Camera 2.
     * @param frames given number of frames to be generated.
     */
    public void flyOver(Scene scn, VisView v1, VisView v2, int frames) {
        _flyOver(scn, v1, v2, frames, new ArrayList<>(), null);
    }

    /**
     * Flies over between two cameras and print pictures in between. The current picture number can be accessed by
     * {@link TemplatePost#getCurrentFrame}. Picture resolution is controlled by {@link UserDeclarations#picResX} and
     * and {@link UserDeclarations#picResX}.
     *
     * @param scn given Scene.
     * @param v1 given Camera 1.
     * @param v2 given Camera 2.
     * @param frames given number of frames to be generated.
     * @param rsv given Recorded Solution View.
     */
    public void flyOver(Scene scn, VisView v1, VisView v2, int frames, RecordedSolutionView rsv) {
        _flyOver(scn, v1, v2, frames, new ArrayList<>(), rsv);
    }

    public void flyOver(Scene scn, ArrayList<VisView> avv) {
        _flyOver(scn, null, null, avv.size() - 1, avv, null);
    }

    /**
     * This method is in conjunction with {@link #flyOver} and it only works along with an @Override. Useful for
     * changing Displayer opacities, colors and other local stuff.
     *
     * It is invoked within the loop prior to printing the pictures.
     */
    public void flyOver_prePrintPicture() {
        //-- Use with @override
    }

    /**
     * This method is in conjunction with {@link #flyOver} and it only works along with an @Override. Useful for
     * changing global stuff.
     *
     * It is invoked outside the loop after the last printed picture.
     */
    public void flyOver_postPrintPicture() {
        //-- Use with @override
    }

    /**
     * Gets the current frame number when using the {@link #flyOver} method.
     *
     * @return An integer.
     */
    public int getCurrentFrame() {
        return _currentFrame;
    }

    /**
     * Gets the number of frames when using the {@link #flyOver} method.
     *
     * @return An integer.
     */
    public int getNumberOfFrames() {
        return _frames;
    }

    /**
     * Creates some useful Unsteady Reports and Annotations, such as:
     * <ul>
     * <li>Maximum CFL Report on all Fluid Regions</li>
     * <li>Average CFL Report on all Fluid Regions</li>
     * <li><i>Time</i> Annotation</li>
     * </ul>
     */
    public void unsteadyReports() {
        _io.say.action("Creating useful Unsteady Reports", true);
        if (!_isUnsteady()) {
            return;
        }
        _io.say.msg("Creating AVG and MAX CFL Reports...");
        ArrayList<Region> ar = _get.regions.all(false);
        FieldFunction ff = _get.objects.fieldFunction(StaticDeclarations.Vars.CFL.getVar(), false);
        _add.report.volumeAverage(ar, "CFL_avg", ff, _ud.unit_Dimensionless, false);
        _add.report.maximum(new ArrayList<>(ar), "CFL_max", ff, _ud.unit_Dimensionless, false);
        _io.say.msg("Creating Time Report Annotation...");
        _add.tools.annotation_Time("6.2f");
        _io.say.ok(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _reset = _mu.reset;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.misc.MainResetter _reset = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

    /**
     * Variable for controlling current frame number.
     */
    private int _currentFrame = 0;

    /**
     * Variable for controlling the number of wanted frames.
     */
    private int _frames = 0;

}
