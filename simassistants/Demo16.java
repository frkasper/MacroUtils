
import demo16.BuilderTask;
import java.util.ArrayList;
import macroutils.MacroUtils;
import star.assistant.SimulationAssistant;
import star.assistant.Task;
import star.assistant.annotation.StarAssistant;

/**
 * Main code.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistant(
        desc = "Simulation Assistant",
        display = "Demo16 Simulation Assistant."
)
public class Demo16 extends SimulationAssistant {

    public Demo16() {

        ArrayList<Task> tasks = new ArrayList<>();

        MacroUtils mu = new MacroUtils(null);

        tasks.add(new BuilderTask(mu));

        setOutline(tasks);

    }

}
