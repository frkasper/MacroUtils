import java.util.*;
import macroutils.*;
import simassistants.demo16.*;
import star.assistant.*;
import star.assistant.annotation.*;

/**
 * Main code.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistant(desc = "Simulation Assistant", display = "Demo16 Simulation Assistant.")
public class Demo16 extends SimulationAssistant {

    public Demo16() {

        ArrayList<Task> tasks = new ArrayList();

        MacroUtils mu = new MacroUtils(null);

        tasks.add(new BuilderTask(mu));

        setOutline(tasks);

    }

}
