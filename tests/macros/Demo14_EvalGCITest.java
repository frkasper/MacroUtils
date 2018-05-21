
import common.SummaryWriter;
import java.io.File;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * This test is assess the GCI metrics in a unit-test fashion.
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class Demo14_EvalGCITest extends StarMacro {

    private static final String PLOT_NAME = "Numerical vs Analytical Solutions";
    private static final String PLOT_NAME_GCI = "Numerical vs Analytical Solutions - GCI23";

    @Override
    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        UserDeclarations ud = mu.userDeclarations;

        ud.simTitle = "Demo14_GCI";

        for (int i = 1; i <= 3; i++) {
            ud.files.add(new File(ud.simPath, ud.simTitle + "_Grid00" + i + ".sim"));
        }

        mu.templates.gci.evaluate(mu.get.plots.byREGEX(PLOT_NAME, true), ud.files);

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

        writeSummary(mu);

    }

    private void writeSummary(MacroUtils mu) {
        SummaryWriter sw = new SummaryWriter(mu, false);
        sw.collectReports();
        sw.collectPlot(mu.get.plots.byREGEX(PLOT_NAME_GCI, true));
        sw.execute();
    }

}
