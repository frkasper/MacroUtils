package macroutils.templates;

import macroutils.MacroUtils;
import macroutils.templates.demos.Demo16;

/**
 * Main class for assessing some legacy MacroUtils demos in template mode.
 *
 * @since October of 2016
 * @author Fabio Kasper
 */
public class TemplateDemos {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplateDemos(MacroUtils m) {
        _mu = m;
        demo16 = new Demo16(m);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        demo16.updateInstances();
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;

    /**
     * This class is responsible for playing Demo 16.
     */
    public Demo16 demo16 = null;

}
