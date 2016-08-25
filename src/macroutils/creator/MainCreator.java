package macroutils.creator;

import macroutils.*;

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
        derivedPart = new CreateDerivedPart(m);
        geometry = new CreateGeometry(m);
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
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        derivedPart.updateInstances();
        geometry.updateInstances();
        meshOperation.updateInstances();
        physicsContinua.updateInstances();
        plot.updateInstances();
        region.updateInstances();
        report.updateInstances();
        scene.updateInstances();
        solver.updateInstances();
        tools.updateInstances();
        units.updateInstances();
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;

    /**
     * This class is responsible for creating Derived Parts.
     */
    public CreateDerivedPart derivedPart = null;

    /**
     * This class is responsible for creating Geometries in general.
     */
    public CreateGeometry geometry = null;

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
