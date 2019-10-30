package macroutils.creator;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.base.neo.NamedObject;
import star.common.AxisType;
import star.common.AxisTypeMode;
import star.common.FieldFunction;
import star.common.HistogramPlot;
import star.common.Simulation;
import star.common.Units;
import star.common.XYPlot;
import star.common.YAxisType;

/**
 * Low-level class for creating Plots with MacroUtils.
 *
 * @since August of 2016
 * @author Fabio Kasper
 */
public class CreatePlot {

    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreatePlot(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    /**
     * Creates a Histogram Plot from the selected Objects.
     *
     * @param ano given ArrayList of STAR-CCM+ Objects
     * @param ff  given Field Function for the bin
     *
     * @return The HistogramPlot
     */
    public HistogramPlot histogram(ArrayList<NamedObject> ano, FieldFunction ff) {

        _io.say.action("Creating a Histogram Plot", true);
        _io.say.objects(ano, "Parts", true);
        _io.say.object(ff, true);

        HistogramPlot hp = _sim.getPlotManager().createPlot(HistogramPlot.class);
        hp.setTitle("");
        hp.getParts().setObjects(ano);
        hp.getXAxisType().getBinFunction().setFieldFunction(ff);

        _io.say.created(hp, true);
        return hp;

    }

    /**
     * Creates a Single XY Plot type from the selected Objects.
     *
     * @param ano given ArrayList of STAR-CCM+ Objects.
     * @param ffx given Field Function for the x-axis.
     * @param ux  given units for the Field Function in the x-axis.
     * @param ffy given Field Function for the y-axis.
     * @param uy  given units for the Field Function in the y-axis.
     *
     * @return The created XY Plot.
     */
    public XYPlot xy(ArrayList<NamedObject> ano, FieldFunction ffx, Units ux, FieldFunction ffy,
            Units uy) {
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

    private void _setAxisType(AxisType at, FieldFunction ff, Units u) {
        at.setMode(AxisTypeMode.SCALAR);
        at.getScalarFunction().setFieldFunction(ff);
        at.getScalarFunction().setUnits(u);
    }

}
