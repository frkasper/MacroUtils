import common.SummaryWriter;
import macroutils.MacroUtils;
import macroutils.templates.simtools.ImplicitUnsteadyConvergenceChecker;
import star.common.StarMacro;

/**
 * This is an automated test for the ImplicitUnsteadyConvergenceChecker simulation tool.
 *
 * @since MacroUtils 2019.2
 * @author Fabio Kasper
 */
public class SimToolImplicitUnsteadyConvergenceCheckerTest extends StarMacro {

    @Override
    public void execute() {

        final MacroUtils mu = new MacroUtils(getSimulation());
        final String prefix = "SimToolConvergenceChecker_";
        final ImplicitUnsteadyConvergenceChecker cc = new ImplicitUnsteadyConvergenceChecker(mu);

        new SummaryWriter(mu, prefix + "0_Original.ref").execute();

        cc.execute();

        new SummaryWriter(mu, prefix + "1_Artifacts_Created.ref").execute();

        cc.removeArtifacts();

        new SummaryWriter(mu, prefix + "2_Artifacts_Removed.ref").execute();

    }

}
