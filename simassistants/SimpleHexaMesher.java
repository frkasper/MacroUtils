
import java.util.ArrayList;
import macroutils.MacroUtils;
import simplehexamesher.BlockTask;
import simplehexamesher.IntroductionTask;
import simplehexamesher.QuestionsAndAnswersTask;
import simplehexamesher.StartOverTask;
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
        desc = "Assistant",
        display = "SimpleHexaMesher Assistant."
)
public class SimpleHexaMesher extends SimulationAssistant {

    public SimpleHexaMesher() {

        ArrayList<Task> tasks = new ArrayList();

        MacroUtils mu = new MacroUtils(null);

        tasks.add(new IntroductionTask());

        tasks.add(new BlockTask(mu));

        tasks.add(new StartOverTask(mu));

        tasks.add(new QuestionsAndAnswersTask());

        setOutline(tasks);

    }

}
