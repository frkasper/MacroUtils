package simplehexamesher;

import macroutils.MacroUtils;
import star.assistant.Task;
import star.assistant.annotation.StarAssistantTask;
import star.assistant.ui.FunctionTaskController;

/**
 * Start over Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        contentPath = "html/StartOverTask.xhtml",
        controller = StartOverTask.RemoverController.class,
        display = "Start Over"
)
public class StartOverTask extends Task {

    private final MacroUtils _mu;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public StartOverTask(MacroUtils m) {
        _mu = m;
    }

    /**
     * Current {@link FunctionTaskController} subclass.
     */
    public class RemoverController extends FunctionTaskController {

        /**
         * Removes all objects.
         */
        public void removeAll() {
            _mu.setSimulation(getActiveSimulation(), false);
            _mu.remove.all();
            _mu.io.say.action("Start Over Task is finished.", true);
        }

    }

}
