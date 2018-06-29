import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * This test is to prevent bug filed at Mar 29, 2018 from happening again (issue #19).
 *
 * @since MacroUtils v13.04
 * @author Fabio Kasper
 */
public class BugWritingFilenamesTest extends StarMacro {

    @Override
    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        UserDeclarations ud = mu.userDeclarations;

        ud.simTitle = "Block 95.2 mm^3";

        mu.add.geometry.block(StaticDeclarations.COORD0, new double[] {3.4, 4.0, 7.0}, ud.unit_mm);
        
        ud.scene = mu.add.scene.geometry();

        ud.scene.resetCamera();
        
        ud.scene.setPresentationName("Block 3.4 x 4.0 x 7.0 mm^3");

        mu.io.write.picture(ud.scene, true);

        mu.saveSim();

    }

}
