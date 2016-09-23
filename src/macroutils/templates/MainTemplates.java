package macroutils.templates;

import macroutils.*;

/**
 * Main class for templates methods in MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class MainTemplates {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public MainTemplates(MacroUtils m) {
        _mu = m;
        gci = new TemplateGCI(m);
        geometry = new TemplateGeometry(m);
        prettify = new TemplatePrettifier(m);
        post = new TemplatePost(m);
        m.io.say.msgDebug("Class loaded: %s...", this.getClass().getSimpleName());
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        gci.updateInstances();
        geometry.updateInstances();
        prettify.updateInstances();
        post.updateInstances();
        _io.print.msgDebug("" + this.getClass().getSimpleName() + " instances updated succesfully.");
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;

    /**
     * This class is responsible for assessing the Grid Convergence Index (GCI) metric.
     */
    public TemplateGCI gci = null;

    /**
     * This class is responsible for some templated geometries.
     */
    public TemplateGeometry geometry = null;

    /**
     * This class is responsible for prettifying the simulation.
     */
    public TemplatePrettifier prettify = null;

    /**
     * This class is responsible for creating useful postprocessing objects in general.
     */
    public TemplatePost post = null;

}
