import common.SummaryWriter;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import macroutils.templates.simassistants.BlockMesher;
import star.common.StarMacro;

/**
 * This is an automated test for the BlockMesher simulation assistant.
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class SimAssistantBlockMesherTest extends StarMacro {

    @Override
    public void execute() {
        MacroUtils mu = new MacroUtils(getActiveSimulation());

        generateMesh(mu, false);

        generateMesh(mu, true);
    }

    private void generateMesh(MacroUtils mu, boolean is2D) {
        final UserDeclarations ud = mu.userDeclarations;
        ud.simTitle = "SimAssistantBlockMesher";

        final double[] coord1 = new double[]{ 0, 0, 0 };
        final double[] coord2 = new double[]{ 1, 2, 3 };
        final double[] nCells = new double[]{ 4, 7, 8 };

        BlockMesher bm = new BlockMesher(mu);
        bm.setParameters(coord1, coord2, nCells, ud.unit_m);
        if (is2D) {
            ud.simTitle += "_2D";
            bm.setAs2D();
        }
        bm.generateMesh();
        mu.saveSim();

        SummaryWriter sw = new SummaryWriter(mu);
        sw.collectMesh();
        sw.execute();

        mu.remove.all();
    }

}
