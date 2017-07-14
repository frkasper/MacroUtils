package macroutils.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.common.Simulation;
import star.common.StarPlot;
import star.vis.Scene;
import star.vis.VisView;

/**
 * Low-level class for writing data in general with MacroUtils.
 *
 * @since May of 2016
 * @author Fabio Kasper
 */
public class Write {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public Write(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private String _getNewName(String s) {
        return s.replace(": ", " - ").replace(" ", "_").replace("=", "").replace("/", "").replace("#", "");
    }

    private void _writeObjects(String what, String prefix, ArrayList<NamedObject> ano) {
        _io.say.action(String.format("Writing %s", what), true);
        _io.say.objects(ano, "Objects", true);
        for (NamedObject no : ano) {
            _writePic(no, String.format("%s_%s", prefix, no.getPresentationName()), _ud.picResX, _ud.picResY, false);
        }
        _io.say.ok(true);
    }

    private boolean _tryWritePic(NamedObject no, String name, int resx, int resy, boolean vo) {
        if (name == null) {
            name = no.getPresentationName();
        } else {
            name = _get.strings.fileBasename(name);
        }
        _io.say.object(no, vo);
        File f = new File(_ud.picPath, String.format("%s.%s", _getNewName(name), StaticDeclarations.PIC_EXT));
        _io.say.msgDebug("Trying to write: %s", f.toString());
        if (no instanceof Scene) {
            Scene scn = (Scene) no;
            scn.printAndWait(f, 1, resx, resy, _ud.picAntiAliasing, _ud.picTransparentBackground);
        } else if (no instanceof StarPlot) {
            StarPlot sp = (StarPlot) no;
            sp.encode(f.toString(), StaticDeclarations.PIC_EXT, resx, resy, true);
        }
        if (f.isFile()) {
            _io.say.value("Written", f.getName(), true, true);
            _io.say.ok(vo);
            return true;
        }
        _io.say.value("Picture not written", f.getName(), true, true);
        return false;
    }

    private void _writePic(NamedObject no, String name, int resx, int resy, boolean vo) {
        int nTries = 3;
        for (int n = 1; n <= nTries; n++) {
            boolean picWritten = _tryWritePic(no, name, resx, resy, vo);
            if (picWritten) {
                break;
            }
            if (n == nTries) {
                _io.say.msg("  - Giving up!", true);
                break;
            }
            _io.say.msg("  - Will try again in a second...", true);
            _io.sleep(1000);
        }
    }

    /**
     * Writes all Scenes and Plots as pictures using the default picture resolution. See
     * {@link UserDeclarations#picResX} and {@link UserDeclarations#picResY}. Use
     * {@link macroutils.setter.SetDefaults#pictureResolution} for setting custom values.
     */
    public void all() {
        plots();
        scenes();
    }

    /**
     * Writes all Plots and Scenes as pictures using the default picture resolution. See
     * {@link UserDeclarations#picResX} and {@link UserDeclarations#picResY}. Use
     * {@link macroutils.setter.SetDefaults#pictureResolution} for setting custom values.
     *
     * @param prefix given prefix for the files. E.g.: simulation name.
     */
    public void all(String prefix) {
        ArrayList<NamedObject> ano = new ArrayList();
        ano.addAll(_sim.getPlotManager().getPlots());
        ano.addAll(_sim.getSceneManager().getScenes());
        _writeObjects("all Plots and Scenes", prefix, ano);
    }

    /**
     * Writes data to a file.
     *
     * @param f given {@link java.io.File}.
     * @param als given ArrayList of Strings.
     * @param vo given verbose option. False will not print anything.
     */
    public void data(File f, ArrayList<String> als, boolean vo) {
        _io.say.action("Writing Data", true);
        _io.say.msg(vo, "Writing %d lines to a file...", als.size());
        _io.say.value("File", f.getAbsolutePath(), true, vo);
        BufferedWriter fileWriter = null;
        try {
            if (f.exists()) {
                _io.say.msg("Already exists. Overwriting...", vo);
            }
            fileWriter = new BufferedWriter(new FileWriter(f));
            for (String s : als) {
                fileWriter.write(s);
                fileWriter.newLine();
            }
            fileWriter.close();
            _io.say.msg("Saved.", vo);
        } catch (IOException ex) {
            _io.say.msg("Could not save file. Exiting...", vo);
        } finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
                _io.say.msg("Could not close file. It might be corrupt or unusable.", vo);
            }
        }
        _io.say.ok(vo);
    }

    /**
     * Writes all camera views (VisView) available in the model. Cameras are stored using the following format:
     * <ul>
     * <li>Name|FocalPointVector|PositionVector|ViewUpVector|ParallelScale</li>
     * </ul>
     *
     * @param filename given name.
     */
    public void cameraViews(String filename) {
        _io.say.action("Writing Camera Views", true);
        ArrayList<VisView> av = new ArrayList(_sim.getViewManager().getObjects());
        _io.say.objects(av, "Camera Views", true);
        ArrayList<String> als = new ArrayList();
        for (VisView v : _sim.getViewManager().getViews()) {
            String name = v.getPresentationName();
            DoubleVector fp = v.getFocalPoint();
            DoubleVector pos = v.getPosition();
            DoubleVector vu = v.getViewUp();
            double ps = v.getParallelScale().getValue();
            int pm = v.getProjectionModeEnum().getValue();
            String cam = String.format(StaticDeclarations.CAM_FORMAT,
                    name, fp.get(0), fp.get(1), fp.get(2), pos.get(0), pos.get(1), pos.get(2),
                    vu.get(0), vu.get(1), vu.get(2), ps, pm);
            _io.say.msg(cam);
            als.add(cam);
        }
        data(new File(_ud.simPath, filename), als, true);
    }

    /**
     * Writes a picture from a Scene or a Plot to the default picture path. See {@link UserDeclarations#picPath}.
     *
     * @param no given NamedObject. E.g.: a Scene or StarPlot.
     * @param name given name for the picture. If null it will revert to current object name.
     * @param resx given resolution in x.
     * @param resy given resolution in y.
     * @param vo given verbose option. False will not print anything.
     */
    public void picture(NamedObject no, String name, int resx, int resy, boolean vo) {
        _io.say.action("Writing a Picture", vo);
        _io.say.object(no, vo);
        _writePic(no, name, resx, resy, vo);
    }

    /**
     * Writes all Plots as pictures using the default picture resolution. See {@link UserDeclarations#picResX} and
     * {@link UserDeclarations#picResY}. Use {@link macroutils.setter.SetDefaults#pictureResolution} for setting custom
     * values.
     */
    public void plots() {
        _writeObjects("all Plots", "Plot", new ArrayList(_sim.getPlotManager().getPlots()));
    }

    /**
     * Writes all Scenes as pictures using the default picture resolution. See {@link UserDeclarations#picResX} and
     * {@link UserDeclarations#picResY}. Use {@link macroutils.setter.SetDefaults#pictureResolution} for setting custom
     * values.
     */
    public void scenes() {
        _writeObjects("all Scenes", "Scene", new ArrayList(_sim.getSceneManager().getScenes()));
    }

    /**
     * This method is called automatically by {@link MainIO} class. It is internal to MacroUtils.
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
    private Simulation _sim = null;

}
