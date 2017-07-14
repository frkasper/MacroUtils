package macroutils.creator;

import macroutils.MacroUtils;

/**
 * Main class for creating STAR-CCM+ objects with MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class MainCreator {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainCreator(MacroUtils m) {
        _mu = m;
        contact = new CreatePartContact(m);
        derivedPart = new CreateDerivedPart(m);
        geometry = new CreateGeometry(m);
        intrf = new CreateInterface(m);
        meshOperation = new CreateMeshOperation(m);
        physicsContinua = new CreatePhysicsContinua(m);
        plot = new CreatePlot(m);
        region = new CreateRegion(m);
        report = new CreateReport(m);
        solver = new CreateSolver(m);
        scene = new CreateScene(m);
        tools = new CreateTools(m);
        units = new CreateUnits(m);
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Adds all custom modifications created automatically by MacroUtils, such as:
     * <ul>
     * <li> Custom Units;
     * <li> Global Parameters.
     * </ul>
     */
    public void all() {
        if (!_mu.getIntrusiveOption()) {
            _io.print.msg("Intrusive Option is Disabled. Skipping...");
            return;
        }
        customUnits();
        globalParameters();
    }

    /**
     * Adds custom Global Parameters that are <b>not</b> shipped with STAR-CCM+. Those are created within MacroUtils.
     */
    public void globalParameters() {
        _add.tools.parameter_Scalar("PI", Math.PI, _ud.unit_Dimensionless);
    }

    /**
     * Adds custom units that are <b>not</b> shipped with STAR-CCM+. Those are created within MacroUtils.
     */
    public void customUnits() {
        if (!_mu.getIntrusiveOption()) {
            _io.print.msgDebug("Intrusive Option is Disabled. Skipping add.customUnits()");
            return;
        }
        _upd.customUnits(true);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        contact.updateInstances();
        derivedPart.updateInstances();
        geometry.updateInstances();
        intrf.updateInstances();
        meshOperation.updateInstances();
        physicsContinua.updateInstances();
        plot.updateInstances();
        region.updateInstances();
        report.updateInstances();
        scene.updateInstances();
        solver.updateInstances();
        tools.updateInstances();
        units.updateInstances();
        _add = _mu.add;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
        _upd = _mu.update;
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.misc.MainUpdater _upd = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * This class is responsible for creating Part Contacts.
     */
    public CreatePartContact contact = null;

    /**
     * This class is responsible for creating Derived Parts.
     */
    public CreateDerivedPart derivedPart = null;

    /**
     * This class is responsible for creating Geometries in general.
     */
    public CreateGeometry geometry = null;

    /**
     * This class is responsible for creating Interfaces in general with MacroUtils.
     */
    public CreateInterface intrf = null;

    /**
     * This class is responsible for creating Mesh Operations.
     */
    public CreateMeshOperation meshOperation = null;

    /**
     * This class is responsible for creating Physics Continuas.
     */
    public CreatePhysicsContinua physicsContinua = null;

    /**
     * This class is responsible for creating Plots.
     */
    public CreatePlot plot = null;

    /**
     * This class is responsible for creating Regions.
     */
    public CreateRegion region = null;

    /**
     * This class is responsible for creating Reports.
     */
    public CreateReport report = null;

    /**
     * This class is responsible for creating Scenes.
     */
    public CreateScene scene = null;

    /**
     * This class is responsible for creating Solver related objects.
     */
    public CreateSolver solver = null;

    /**
     * This class is responsible for creating Tools related node in general.
     */
    public CreateTools tools = null;

    /**
     * This class is responsible for creating/adding units to STAR-CCM+.
     */
    public CreateUnits units = null;

}
