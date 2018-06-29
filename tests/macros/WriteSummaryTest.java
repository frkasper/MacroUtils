import common.SummaryWriter;
import macroutils.MacroUtils;
import star.common.StarMacro;

/**
 * Write an overview of the Simulation -- focus is on quantitative data.
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class WriteSummaryTest extends StarMacro {

    @Override
    public void execute() {

        new SummaryWriter(new MacroUtils(getSimulation(), false)).execute();

    }

}
