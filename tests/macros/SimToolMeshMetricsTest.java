import common.SummaryWriter;
import macroutils.MacroUtils;
import macroutils.templates.simtools.MeshMetrics;
import star.common.StarMacro;

/**
 * This is an automated test for the MeshMetrics simulation tool.
 *
 * @since MacroUtils 2019.2
 * @author Fabio Kasper
 */
public class SimToolMeshMetricsTest extends StarMacro {

    @Override
    public void execute() {

        final MacroUtils mu = new MacroUtils(getSimulation());
        final String prefix = "SimToolMeshMetrics_";
        final MeshMetrics mm = new MeshMetrics(mu);

        new SummaryWriter(mu, prefix + "0_Original.ref").execute();

        mm.execute();

        new SummaryWriter(mu, prefix + "1_Artifacts_Created.ref").execute();

        mm.removeArtifacts();

        new SummaryWriter(mu, prefix + "2_Artifacts_Removed.ref").execute();

    }

}
