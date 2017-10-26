package macroutils;

import java.io.File;
import java.util.Locale;
import macroutils.checker.MainChecker;
import macroutils.creator.MainCreator;
import macroutils.getter.MainGetter;
import macroutils.io.MainIO;
import macroutils.misc.MainClearer;
import macroutils.misc.MainCloser;
import macroutils.misc.MainDisabler;
import macroutils.misc.MainEnabler;
import macroutils.misc.MainOpener;
import macroutils.misc.MainRemover;
import macroutils.misc.MainResetter;
import macroutils.misc.MainUpdater;
import macroutils.setter.MainSetter;
import macroutils.templates.MainTemplates;
import star.common.LabCoordinateSystem;
import star.common.Simulation;
import star.common.SimulationIterator;

/**
 * MacroUtils is a library that can be used for writing macros, Simulation Assistants and other third party applications
 * related to STAR-CCM+.
 * <p>
 * <b>Requires:</b>
 * <ul>
 * <li> STAR-CCM+ v12.06 libraries. <u>It may not run in other versions</u>;
 * </ul>
 *
 * @since STAR-CCM+ v7.02, May of 2012
 * @author Fabio Kasper
 * @version v12.06, September 26, 2017.
 */
public final class MacroUtils {

    private final String MACROUTILS_VERSION = "MacroUtils version 12.06 (build 1)";

    /**
     * Initialize MacroUtils in intrusive mode by providing a Simulation object.
     *
     * <p>
     * This method is <b>mandatory</b>. It all begins here.
     *
     * @param s given Simulation.
     */
    public MacroUtils(Simulation s) {
        setSimulation(s, true);
    }

    /**
     * Initialize MacroUtils by providing a Simulation object with the option of intrusive mode.
     *
     * <p>
     * This method is <b>mandatory</b>. It all begins here.
     *
     * @param s given Simulation.
     * @param intrusiveMode given intrusive mode option. This will change your simulation file with recommended
     * MacroUtils settings.
     */
    public MacroUtils(Simulation s, boolean intrusiveMode) {
        setSimulation(s, intrusiveMode);
    }

    private void _initialize() {
        if (!_isInitialized) {
            io = new MainIO(this, _debug);
            _isInitialized = true;
        }
        add = new MainCreator(this);
        check = new MainChecker(this);
        clear = new MainClearer(this);
        close = new MainCloser(this);
        disable = new MainDisabler(this);
        enable = new MainEnabler(this);
        get = new MainGetter(this);
        open = new MainOpener(this);
        remove = new MainRemover(this);
        reset = new MainResetter(this);
        set = new MainSetter(this);
        templates = new MainTemplates(this);
        update = new MainUpdater(this);
        userDeclarations = new UserDeclarations(this);
        _updateInstances();
        io.print.action(String.format("Initializing %s", MACROUTILS_VERSION), true);
        _initialize_defaults();
        io.print.action(String.format("%s initialized!", MACROUTILS_VERSION), true);
    }

    private void _initialize_defaults() {
        _sim.loadMeshing();
        Locale.setDefault(Locale.ENGLISH);
        userDeclarations.simFullPath = _sim.getSessionPath();
        userDeclarations.simFile = new File(userDeclarations.simFullPath);
        userDeclarations.simTitle = _sim.getPresentationName();
        userDeclarations.simPath = _sim.getSessionDir();
        userDeclarations.cadPath = new File(userDeclarations.simPath, "CAD");
        userDeclarations.dbsPath = new File(userDeclarations.simPath, "DBS");
        userDeclarations.picPath = userDeclarations.simPath;
        io.say.value("Simulation Name", userDeclarations.simTitle, true, true);
        io.say.value("Simulation File", userDeclarations.simFile.toString(), true, true);
        io.say.value("Simulation Path", userDeclarations.simPath, true, true);
        userDeclarations.csys0 = _sim.getCoordinateSystemManager().getLabCoordinateSystem();
        userDeclarations.lab0 = (LabCoordinateSystem) userDeclarations.csys0;
        userDeclarations.defColormap = get.objects.colormap(StaticDeclarations.Colormaps.BLUE_RED_BALANCED);
        update.defaultUnits(true);
        add.all();
    }

    private void _step(int n) {
        SimulationIterator si = _sim.getSimulationIterator();
        String s = "iterations";
        io.say.action("Running the case", true);
        io.say.cellCount();
        if (!check.has.volumeMesh()) {
            io.say.msg("No volume mesh found. Skipping run.");
            return;
        }
        if (check.is.unsteady()) {
            s = "timesteps";
        }
        set.suggestedPreRun();
        if (n > 0) {
            io.say.msg(true, "Running for %d %s...", n, s);
            si.step(n);
            return;
        }
        si.run();
    }

    private void _updateInstances() {
        /*
         Instances are needing more than 1 pass to initialize all the cross references.
         This is far from elegant. I know. :-)
         */
        for (int i = 0; i < 2; i++) {
            add.updateInstances();
            check.updateInstances();
            clear.updateInstances();
            close.updateInstances();
            disable.updateInstances();
            enable.updateInstances();
            get.updateInstances();
            io.updateInstances();
            open.updateInstances();
            remove.updateInstances();
            reset.updateInstances();
            set.updateInstances();
            templates.updateInstances();
            update.updateInstances();
            userDeclarations.updateInstances();
        }
    }

    /**
     * Gets the Debug mode in MacroUtils.
     *
     * @return True or False.
     */
    public boolean getDebugMode() {
        return _debug;
    }

    /**
     * Gets the current intrusive mode option in MacroUtils.
     *
     * @return True or False.
     */
    public boolean getIntrusiveOption() {
        return _im;
    }

    /**
     * Gets the current Simulation.
     *
     * @return The Simulation instance.
     */
    public Simulation getSimulation() {
        return _sim;
    }

    /**
     * Runs the simulation.
     */
    public void run() {
        step(0);
    }

    /**
     * Runs the simulation for a given number of Iterations or Timesteps.
     *
     * @param n given iterations. If the simulation is Unsteady it will be performed as a number of timesteps.
     */
    public void step(int n) {
        _step(n);
    }

    /**
     * Saves the simulation file using the current {@link UserDeclarations#simTitle} variable.
     */
    public void saveSim() {
        saveSim(userDeclarations.simTitle);
    }

    /**
     * Saves the simulation file using a custom name.
     *
     * @param name given Simulation name.
     */
    public void saveSim(String name) {
        saveSim(name, true);
    }

    private void saveSim(String name, boolean vo) {
        io.say.action(String.format("Saving Simulation File"), vo);
        name = get.strings.fileBasename(name);
        File f = new File(name + ".sim");
        if (!f.canWrite()) {
            f = new File(userDeclarations.simPath, f.getName());
        }
        _sim.saveState(f.toString());
        io.say.ok(vo);
    }

    /**
     * Sets the Debug mode in MacroUtils. Lots of printing will occur.
     *
     * @param opt given option. True or False.
     */
    public void setDebugMode(boolean opt) {
        _debug = opt;
        if (io == null) {
            return;
        }
        io.print.setDebug(_debug);
    }

    /**
     * Sets the intrusive mode option in MacroUtils.
     *
     * @param opt given option. True or False.
     */
    public void setIntrusiveOption(boolean opt) {
        _im = opt;
    }

    /**
     * Sets a Simulation object for the MacroUtils instance.
     *
     * @param s given Simulation.
     * @param intrusiveMode given intrusive mode option. This will change your simulation file with recommended
     * MacroUtils settings.
     */
    public void setSimulation(Simulation s, boolean intrusiveMode) {
        if (s == null || s == _sim) {
            return;
        }
        _sim = s;
        setIntrusiveOption(intrusiveMode);
        _initialize();
    }

    //--
    //-- Variables declaration area.
    //--
    private boolean _debug = false;
    private boolean _im = false;
    private boolean _isInitialized = false;
    private Simulation _sim = null;

    /**
     * This is where you can find the methods for <i>checking</i> objects with MacroUtils.
     */
    public MainChecker check = null;

    /**
     * This is where you can find the methods for <i>clearing</i> objects with MacroUtils.
     */
    public MainClearer clear = null;

    /**
     * This is where you can find the methods for <i>closing</i> objects with MacroUtils.
     */
    public MainCloser close = null;

    /**
     * This is where you can find the methods for <i>adding/creating</i> objects with MacroUtils.
     */
    public MainCreator add = null;

    /**
     * This is where you can find the methods for <i>disabling</i> objects with MacroUtils.
     */
    public MainDisabler disable = null;

    /**
     * This is where you can find the methods for <i>enabling</i> objects with MacroUtils.
     */
    public MainEnabler enable = null;

    /**
     * This is where you can find the methods for <i>getting</i> data with MacroUtils.
     */
    public MainGetter get = null;

    /**
     * This is where you can find the methods for <i>input/output</i> data with MacroUtils.
     */
    public MainIO io = null;

    /**
     * This is where you can find the methods for <i>opening</i> objects with MacroUtils.
     */
    public MainOpener open = null;

    /**
     * This is where you can find the methods for <i>removing</i> STAR-CCM+ objects with MacroUtils.
     */
    public MainRemover remove = null;

    /**
     * This is where you can find the methods for <i>resetting</i> variables with MacroUtils.
     */
    public MainResetter reset = null;

    /**
     * This is where you can find the methods for <i>setting</i> parameters with MacroUtils.
     */
    public MainSetter set = null;

    /**
     * This is where you can find the methods for useful templates with MacroUtils.
     */
    public MainTemplates templates = null;

    /**
     * This is where you can find the methods for <i>updating</i> parameters with MacroUtils.
     */
    public MainUpdater update = null;

    /**
     * This is where the public user variables can be found in MacroUtils.
     */
    public UserDeclarations userDeclarations = null;

}
