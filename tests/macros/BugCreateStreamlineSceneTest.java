import common.SummaryWriter;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.common.StarMacro;

/**
 * This test is to prevent bug filed at Feb 14, 2017 from happening again (issue #14).
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class BugCreateStreamlineSceneTest extends StarMacro {

    @Override
    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        createStreamlineScene(mu);

        writeSummary(mu);

    }

    private void createStreamlineScene(MacroUtils mu) {
        UserDeclarations ud = mu.userDeclarations;
        ud.postStreamlinesTubesWidth = 0.001;
        ud.namedObjects.add(mu.get.boundaries.byREGEX("inlet", true));
        ud.namedObjects.addAll(mu.get.regions.all(true));
        ud.scene = mu.add.scene.streamline(ud.namedObjects, true);
        ud.scene.open();
        ud.scene.getCurrentView().setInput(
                new DoubleVector(new double[] {0.24779246893912843, -0.010367, -0.005769}),
                new DoubleVector(new double[] {-0.6836124057786044, 0.332600, 0.494371}),
                new DoubleVector(new double[] {0.2016193914075902, 0.941481, -0.270117}),
                0.090454, 1);
        ud.string = "Bug_" + ud.simTitle;
        mu.saveSim(ud.string);
        mu.io.write.picture(ud.scene, ud.string, 1280, 720, true);
    }

    private void writeSummary(MacroUtils mu) {
        SummaryWriter sw = new SummaryWriter(mu);
        sw.collectScenes();
        sw.execute();
    }

}
