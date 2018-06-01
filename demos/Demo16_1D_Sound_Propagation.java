import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import macroutils.templates.demos.Demo16;
import star.common.StarMacro;

/**
 * Runs a simplified sound propagation problem inside a constant cross section channel.
 *
 * Boundary condition is a sinusoid unity Pressure (1Pa) and the domain length is 50 wavelengths.
 * Total time is 2 flows through. Flow is inviscid ideal gas run with the coupled explicit scheme of
 * STAR-CCM+.
 *
 * @since MacroUtils v11.04.
 * @author Fabio Kasper
 */
public class Demo16_1D_Sound_Propagation extends StarMacro {

    @Override
    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        UserDeclarations ud = mu.userDeclarations;

        ud.simTitle = "Demo16_1D_Sound_Propagation";

        Demo16 demo16 = mu.templates.demos.demo16;

        demo16.updateCaseParameters();

        demo16.executePre();

        demo16.executePost();

        demo16.printOverview();

        mu.run();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

}
