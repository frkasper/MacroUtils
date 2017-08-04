package macroutils.misc;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.common.Simulation;
import star.meshing.MeshPipelineController;

/**
 * Main class for "clearing" methods in MacroUtils.
 *
 * @since July of 2016
 * @author Fabio Kasper
 */
public class MainClearer {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainClearer(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * Clears all generated meshes.
     */
    public void meshes() {
        _io.say.msg("Clearing Meshes...");
        _sim.get(MeshPipelineController.class).clearGeneratedMeshes();
        _io.say.ok(true);
    }

    /**
     * Clears the Solution and all Fields are erased.
     */
    public void solution() {
        _io.say.msg("Clearing Solution...");
        _sim.getSolution().clearSolution();
        _io.say.ok(true);
    }

    /**
     * Clears only the Solution History and Fields are kept.
     */
    public void solutionHistory() {
        solution(StaticDeclarations.SolutionClear.HISTORY);
    }

    /**
     * This method gives you the ability to clears different areas of the Solution.
     *
     * @param sc given option. See {@link macroutils.StaticDeclarations.SolutionClear} for options.
     */
    public void solution(StaticDeclarations.SolutionClear... sc) {
        _io.say.action("Clearing Solution", true);
        for (StaticDeclarations.SolutionClear sc1 : sc) {
            _io.say.msg(true, "  - %s", sc1.toString());
            _sim.getSolution().clearSolution(sc1.getClear());
        }
        _io.say.ok(true);
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
