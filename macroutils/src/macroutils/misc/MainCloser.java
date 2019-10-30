package macroutils.misc;

import macroutils.MacroUtils;
import star.base.neo.ClientServerObject;
import star.common.Simulation;
import star.common.StarPlot;
import star.vis.Scene;

/**
 * Main class for "closing" methods in MacroUtils.
 *
 * @since May of 2016
 * @author Fabio Kasper
 */
public class MainCloser {

    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainCloser(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Closes all Plots and Scenes in the model.
     */
    public void all() {
        allPlots(true);
        allScenes(true);
    }

    /**
     * Closes all Plots in the model.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void allPlots(boolean vo) {
        _closingAll("Plots", vo);
        for (StarPlot sp : _sim.getPlotManager().getObjects()) {
            _closing(sp, vo);
            sp.close();
        }
        _io.say.ok(vo);
    }

    /**
     * Closes all Scenes in the model.
     *
     * @param vo given verbose option. False will not print anything.
     */
    public void allScenes(boolean vo) {
        _closingAll("Scenes", vo);
        for (Scene scn : _sim.getSceneManager().getObjects()) {
            _closing(scn, vo);
            scn.close(true);
        }
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName()
                + " instances updated succesfully.");
    }

    private void _closing(ClientServerObject cso, boolean vo) {
        _io.say.value("Closing", cso.getPresentationName(), true, vo);
    }

    private void _closingAll(String what, boolean vo) {
        _io.say.action(String.format("Closing All %s...", what), vo);
    }

}
