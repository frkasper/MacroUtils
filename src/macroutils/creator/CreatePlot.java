package macroutils.creator;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;

/**
 * Low-level class for creating Plots with MacroUtils.
 *
 * @since August of 2016
 * @author Fabio Kasper
 */
public class CreatePlot {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreatePlot(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private void _setAxisType(AxisType at, FieldFunction ff, Units u) {
        at.setMode(AxisTypeMode.SCALAR);
        at.getScalarFunction().setFieldFunction(ff);
        at.getScalarFunction().setUnits(u);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

  /**
   * Creates a Single XY Plot type from the selected Objects.
   *
   * @param ano given ArrayList of STAR-CCM+ Objects.
   * @param ffx given Field Function for the x-axis.
   * @param ux given units for the Field Function in the x-axis.
   * @param ffy given Field Function for the y-axis.
   * @param uy given units for the Field Function in the y-axis.
   * @return The created XY Plot.
   */
  public XYPlot xy(ArrayList<NamedObject> ano, FieldFunction ffx, Units ux, FieldFunction ffy, Units uy) {
    _io.say.action("Creating a XY Plot", true);
    _io.say.objects(ano, "Parts", true);
    _io.say.value("X-Axis", ffx.getPresentationName(), true, true);
    _io.say.value("Y-Axis", ffy.getPresentationName(), true, true);
    XYPlot xyp = _sim.getPlotManager().createXYPlot();
    xyp.setTitle("");
    xyp.getParts().setObjects(ano);
    _setAxisType(xyp.getXAxisType(), ffx, ux);
    _setAxisType(((YAxisType) xyp.getYAxes().getDefaultAxis()), ffy, uy);
    _io.say.created(xyp, true);
    return xyp;
  }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
