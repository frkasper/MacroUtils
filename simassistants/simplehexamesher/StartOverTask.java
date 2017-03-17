package simplehexamesher;

import macroutils.*;
import star.assistant.*;
import star.assistant.annotation.*;
import star.assistant.ui.*;

/**
 * Start over Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        display = "Start Over",
        contentPath = "html/StartOverTask.xhtml",
        controller = StartOverTask.RemoverController.class
)
public class StartOverTask extends Task {

    private final MacroUtils _mu;

    public StartOverTask(MacroUtils m) {
        _mu = m;
    }

    public class RemoverController extends FunctionTaskController {

        public void removeAll() {
            _mu.setSimulation(getActiveSimulation(), false);
            _mu.remove.all();
            _mu.io.say.action("Start Over Task is finished.", true);
        }

    }

}
