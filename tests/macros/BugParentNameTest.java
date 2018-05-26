import common.SummaryWriter;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * This test is to prevent bug filed at Apr 28, 2018 from happening again (issue #20).
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class BugParentNameTest extends StarMacro {

    @Override
    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        UserDeclarations ud = mu.userDeclarations;

        mu.add.geometry.block(StaticDeclarations.COORD0, new double[] {1, 1, 1}, ud.unit_mm)
                .setPresentationName("My Block");
        
        ud.scene = mu.add.scene.geometry();
        
        ud.scene.setPresentationName("My Scene");

        mu.get.scenes.displayerByREGEX(ud.scene, ".*", true).setPresentationName("My Displayer");

        ud.region = mu.add.region.fromAll(true);

        ud.region.setPresentationName("My Region");

        mu.get.boundaries.byREGEX(".*", true).setPresentationName("My Boundary");

        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.POS).getComponentFunction(0);

        mu.add.report.minimum(ud.region, "My Report", ud.ff, ud.unit_mm, true);

        mu.saveSim("BugParentNameTest");
        
        new SummaryWriter(mu).execute();

    }

}
