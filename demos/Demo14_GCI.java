import macroutils.*;
import star.common.*;

/**
 * Laminar fully developed flow in a pipe (Poiseuille flow).
 *
 * This Demo shows how Grid Convergence Index can be calculated in STAR-CCM+
 * using a periodic flow modeling strategy.
 *
 * This Demo was modified in Macro Utils v3.3 and it works best when using the
 * Generalized Cylinder Mesher which (to date) is still not available as Parts
 * Based Meshing in STAR-CCM+.
 *
 *
 * Geometry:
 *            L
 *      +-----------+
 *      |           |
 *    r * O(0,0,0)  |
 *      |           |
 *      +-----------+
 *
 * @since Macro Utils v3.1.
 * @author Fabio Kasper
 */
public class Demo14_GCI extends StarMacro {

  public void execute() {

    initMacro();

    //
    //
    // Removed until the Generalized Cylinder Mesher is migrated into Parts Based Meshing.
    //
    //

  }

  void initMacro() {
    mu = new MacroUtils(getActiveSimulation());
    ud = mu.userDeclarations;
    ud.simTitle = "Demo14_GCI";
  }

  private MacroUtils mu;
  private UserDeclarations ud;

}
