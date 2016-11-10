import java.util.*;
import macroutils.*;
import simplehexamesher.*;
import star.assistant.*;
import star.assistant.annotation.*;

/**
 * Main code.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistant(desc = "Assistant", display = "SimpleHexaMesher Assistant.")
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
