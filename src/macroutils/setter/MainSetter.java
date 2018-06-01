package macroutils.setter;

import macroutils.MacroUtils;
import star.common.AutoSave;
import star.common.Simulation;
import star.common.UpdateEvent;

/**
 * Main class for set-type methods in MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class MainSetter {

    /**
     * This class is responsible for setting Boundary conditions (BCs) parameters.
     */
    public SetBoundaries boundary = null;

    /**
     * This class is responsible for setting Geometry parameters.
     */
    public SetGeometry geometry = null;

    /**
     * This class is responsible for setting mesh parameters.
     */
    public SetMesh mesh = null;

    /**
     * This class is responsible for setting objects in general.
     */
    public SetObjects object = null;

    /**
     * This class is responsible for setting physics parameters.
     */
    public SetPhysics physics = null;

    /**
     * This class is responsible for setting Region parameters.
     */
    public SetRegions region = null;

    /**
     * This class is responsible for setting Scene parameters.
     */
    public SetScenes scene = null;
    /**
     * This class is responsible for setting solver parameters.
     */
    public SetSolver solver = null;

    /**
     * This class is responsible for setting MacroUtils defaults.
     */
    public SetDefaults userDefault = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.misc.MainDisabler _dis = null;
    private macroutils.misc.MainEnabler _ena = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainSetter(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        boundary = new SetBoundaries(m);
        mesh = new SetMesh(m);
        object = new SetObjects(m);
        geometry = new SetGeometry(m);
        physics = new SetPhysics(m);
        region = new SetRegions(m);
        scene = new SetScenes(m);
        solver = new SetSolver(m);
        userDefault = new SetDefaults(m);
        _chk = m.check;
        _dis = m.disable;
        _ena = m.enable;
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Set the Auto Save functionality based on an Update Event.
     *
     * @param ue            given Update Event.
     * @param maxSavedFiles given number of Simulation files to keep.
     */
    public void autoSave(UpdateEvent ue, int maxSavedFiles) {
        _io.say.action("Setting Auto Save", true);
        _io.say.value("Update Event", ue.getPresentationName(), true, true);
        AutoSave as = _sim.getSimulationIterator().getAutoSave();
        as.setMaxAutosavedFiles(maxSavedFiles);
        object.updateEvent(as, ue, false);
        _io.say.ok(true);
    }

    /**
     * Sets suggested parameters prior for a run, based on experience, such as:
     * <ul>
     * <li>Make sure ABORT Stopping Criteria is enabled;</li>
     * <li>Make sure Maximum Steps Stopping Criteria is disabled when running unsteady;</li>
     * </ul>
     */
    public void suggestedPreRun() {
        _io.say.msg("Setting Suggested Pre-Run Parameters:", true);
        //--
        _io.say.msg("  - Making sure ABORT Stopping Criteria is enabled...");
        _ena.stoppingCriteriaAbortFile(false);
        if (_chk.is.unsteady()) {
            _io.say.msg("  - Making sure Maximum Steps is disabled...");
            _dis.stoppingCriteriaAbortFile(false);
        }
        _io.say.ok(true);
        if (_mu.getIntrusiveOption()) {
            _mu.templates.prettify.all();
        }
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        boundary.updateInstances();
        geometry.updateInstances();
        mesh.updateInstances();
        object.updateInstances();
        physics.updateInstances();
        region.updateInstances();
        scene.updateInstances();
        solver.updateInstances();
        userDefault.updateInstances();
        _chk.updateInstances();
        _dis.updateInstances();
        _ena.updateInstances();
        _io = _mu.io;
        _io.print.msgDebug("" + this.getClass().getSimpleName()
                + " instances updated succesfully.");
    }

}
