
import macroutils.MacroUtils;
import star.common.StarMacro;

/**
 * Prettify Scenes, Plots, Annotations and Monitors with this very useful method.
 *
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo9_Make_Me_Pretty extends StarMacro {

    public void execute() {
        MacroUtils mu = new MacroUtils(getActiveSimulation());
        mu.templates.prettify.all();
    }

}
