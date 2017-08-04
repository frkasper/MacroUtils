package macroutils.misc;

import macroutils.MacroUtils;
import star.base.neo.ClientServerObject;
import star.common.Simulation;
import star.common.StarPlot;
import star.vis.Scene;

/**
 * Main class for "opening" methods in MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class MainOpener {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainOpener(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    private void _openingAll(String what, boolean vo) {
        _io.say.action(String.format("Opening All %s", what), vo);
    }

    private void _opening(ClientServerObject cso, boolean vo) {
        _io.say.value("Opening", cso.getPresentationName(), true, vo);
    }

    /**
     * Opens all Plots and Scenes in the model.
     */
    public void all() {
        allPlots(true);
        allScenes(true);
    }

    /**
     * Opens all Plots in the model.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void allPlots(boolean vo) {
        _openingAll("Plots", vo);
        for (StarPlot sp : _sim.getPlotManager().getObjects()) {
            _opening(sp, vo);
            sp.open();
        }
        _io.say.ok(vo);
    }

    /**
     * Opens all Scenes in the model.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void allScenes(boolean vo) {
        _openingAll("Scenes", vo);
        for (Scene scn : _sim.getSceneManager().getObjects()) {
            _opening(scn, vo);
            scn.open();
        }
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
