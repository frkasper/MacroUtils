package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.common.*;
import star.vis.*;

/**
 * Low-level class for getting Scenes with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetScenes {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetScenes(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private void _gotNull(String what, Displayer d, boolean vo) {
        _io.say.value(what, d.getPresentationName(), true, vo);
        _io.say.msg("Nothing found. Returning NULL!", vo);
    }

    /**
     * Gets all Scenes available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Scenes.
     */
    public ArrayList<Scene> all(boolean vo) {
        ArrayList<Scene> as = new ArrayList<>(_sim.getSceneManager().getScenes());
        _io.say.objects(as, "Getting all Scenes", vo);
        return as;
    }

    /**
     * Gets all Scenes that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Scenes.
     */
    public ArrayList<Scene> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(_get.objects.allByREGEX(regexPatt, "Scenes", new ArrayList<>(all(false)), vo));
    }

    /**
     * Gets all Displayers from all Scenes available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Displayers.
     */
    public ArrayList<Displayer> allDisplayers(boolean vo) {
        ArrayList<Displayer> ad = new ArrayList<>();
        for (Scene scn : all(false)) {
            ad.addAll(scn.getDisplayerManager().getObjects());
        }
        _io.say.objects(ad, "Getting all Displayers", vo);
        return ad;
    }

    /**
     * Gets all Displayers from a given Scene.
     *
     * @param scn given Scene.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Displayers.
     */
    public ArrayList<Displayer> allDisplayers(Scene scn, boolean vo) {
        String sn = scn.getPresentationName();
        ArrayList<Displayer> ad = new ArrayList<>(scn.getDisplayerManager().getObjects());
        _io.say.objects(ad, String.format("Getting all Displayers from Scene \"%s\"", scn.getPresentationName()), vo);
        return ad;
    }

    /**
     * Gets all Displayers that matches the REGEX search pattern among all Scenes available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Displayers.
     */
    public ArrayList<Displayer> allDisplayersByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(_get.objects.allByREGEX(regexPatt, "Displayers",
                new ArrayList<>(allDisplayers(false)), vo));
    }

    /**
     * Gets a Scene that matches the REGEX search pattern among all Scenes available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Scene. Null if nothing is found.
     */
    public Scene byREGEX(String regexPatt, boolean vo) {
        return (Scene) _get.objects.byREGEX(regexPatt, "Scene", new ArrayList<>(all(false)), vo);
    }

    /**
     * Gets a Displayer that matches the REGEX search pattern within the given Scene.
     *
     * @param scn given Scene.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Scene. Null if nothing is found.
     */
    public Displayer displayerByREGEX(Scene scn, String regexPatt, boolean vo) {
        return (Displayer) _get.objects.byREGEX(regexPatt, "Scene", new ArrayList<>(allDisplayers(scn, false)), vo);
    }

    /**
     * Get the Legend from a Displayer, if applicable.
     *
     * @param d given Displayer. E.g.: Scalar, Vector or Streamline Displayer.
     * @param vo given verbose option. False will not print anything.
     * @return The Legend.
     */
    public Legend legend(Displayer d, boolean vo) {
        if (_chk.is.scalar(d)) {
            return ((ScalarDisplayer) d).getLegend();
        } else if (_chk.is.streamline(d)) {
            return ((StreamDisplayer) d).getLegend();
        } else if (_chk.is.vector(d)) {
            return ((VectorDisplayer) d).getLegend();
        }
        _gotNull("Legend on Displayer", d, vo);
        return null;
    }

    /**
     * Get the Scalar Display Quantity from a Displayer, if applicable.
     *
     * @param d given Displayer. E.g.: Scalar or Streamline Displayer.
     * @param vo given verbose option. False will not print anything.
     * @return The ScalarDisplayQuantity.
     */
    public ScalarDisplayQuantity scalarDisplayQuantity(Displayer d, boolean vo) {
        if (_chk.is.scalar(d)) {
            return ((ScalarDisplayer) d).getScalarDisplayQuantity();
        } else if (_chk.is.streamline(d)) {
            return ((StreamDisplayer) d).getScalarDisplayQuantity();
        }
        _gotNull("ScalarDisplayQuantity on Displayer", d, vo);
        return null;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
