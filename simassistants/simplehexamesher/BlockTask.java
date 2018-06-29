package simplehexamesher;

import macroutils.MacroUtils;
import macroutils.templates.simassistants.BlockMesher;
import star.assistant.Task;
import star.assistant.annotation.StarAssistantTask;
import star.assistant.ui.FunctionTaskController;

/**
 * Block Mesher Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        contentPath = "html/BlockTask.xhtml",
        controller = BlockTask.MesherController.class,
        display = "Block Mesher"
)
public class BlockTask extends Task {

    private final MacroUtils _mu;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public BlockTask(MacroUtils m) {
        _mu = m;
    }

    /**
     * Current {@link FunctionTaskController} subclass.
     */
    public class MesherController extends FunctionTaskController {

        /**
         * Generate Mesh Task.
         */
        public void generateMesh() {
            initializeTask();
            BlockMesher bm = new BlockMesher(_mu);
            if (bm.haveParameters()) {
                bm.generateMesh();
            } else {
                _mu.io.say.loud("Please define Block dimensions first and try again.");
            }
        }

        /**
         * Set as Two-Dimensional Task.
         */
        public void setAs2D() {
            initializeTask();
            new BlockMesher(_mu).setAs2D();
            _mu.io.say.action("Set as Two-Dimensional Task is finished.", true);
        }

        /**
         * Set Parameters Task.
         */
        public void setParameters() {
            initializeTask();
            BlockMesher bm = new BlockMesher(_mu);
            bm.setParameters(new double[]{ 0, 0, 0 }, new double[]{ 1, 1, 1 },
                    new double[]{ 2, 4, 6 }, _mu.userDeclarations.unit_m);
            selectNodeExclusive(bm.getBlockCoordinate1Parameter());
            _mu.io.say.action("Set Parameters Task is finished.", true);
        }

        private void initializeTask() {
            _mu.setSimulation(getActiveSimulation(), false);
        }

    }

}
