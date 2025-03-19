package macroutils.creator;

import java.util.ArrayList;
import java.util.List;
import macroutils.MacroUtils;
import star.base.neo.NamedObject;
import star.common.Cartesian2DPlot;
import star.common.FieldFunction;
import star.common.PartGroupDataSet;
import star.common.Simulation;
import star.common.Units;
import star.common.WeightingMode;
import star.coremodule.ui.plotsetup.PlotAxisInput;
import star.coremodule.ui.plotsetup.SimDataSourceType;
import star.coremodule.ui.plotsetup.VariableType;
import star.coremodule.ui.plotsetup.XyPlotTypeEnum;
import star.coremodule.ui.plotsetup.dscfg.HistogramDataSeriesConfig;
import star.coremodule.ui.plotsetup.dscfg.XyDataSeriesConfig;

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
     * Creates an empty plot.
     *
     * @return The Cartesian2DPlot
     */
    public Cartesian2DPlot empty() {
        _io.say.action("Creating an empty Plot", true);
        Cartesian2DPlot plot = _sim.getPlotManager().createCartesian2DPlot("", List.of());
        _io.say.created(plot, true);
        return plot;
    }

    /**
     * Creates a Histogram Plot from the selected Objects.
     *
     * @param ano given ArrayList of STAR-CCM+ Objects
     * @param ff  given Field Function for the bin
     *
     * @return The HistogramPlot
     */
    public Cartesian2DPlot histogram(ArrayList<NamedObject> ano, FieldFunction ff) {

        _io.say.action("Creating a Histogram Plot", true);
        _io.say.objects(ano, "Parts", true);
        _io.say.object(ff, true);

        HistogramDataSeriesConfig config = new HistogramDataSeriesConfig(SimDataSourceType.PARTS,
                ano, null,
                new PlotAxisInput(VariableType.SCALAR, new PlotAxisInput.ScalarFunctionInput(ff)),
                new PlotAxisInput(VariableType.SCALAR), WeightingMode.FREQUENCY);
        Cartesian2DPlot plot = _sim.getPlotManager().createCartesian2DPlot("",
                List.of(XyPlotTypeEnum.HISTOGRAM), config);

        _io.say.created(plot, true);
        return plot;

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
    public Cartesian2DPlot xy(ArrayList<NamedObject> ano, FieldFunction ffx, Units ux, FieldFunction ffy,
            Units uy) {

        _io.say.action("Creating a XY Plot", true);
        _io.say.objects(ano, "Parts", true);
        _io.say.value("X-Axis", ffx.getPresentationName(), true, true);
        _io.say.value("Y-Axis", ffy.getPresentationName(), true, true);

        XyDataSeriesConfig config = new XyDataSeriesConfig(SimDataSourceType.PARTS,
                ano, null,
                new PlotAxisInput(VariableType.SCALAR, new PlotAxisInput.ScalarFunctionInput(ffx)),
                new PlotAxisInput(VariableType.SCALAR, new PlotAxisInput.ScalarFunctionInput(ffy)));
        Cartesian2DPlot plot = _sim.getPlotManager().createCartesian2DPlot("",
                List.of(XyPlotTypeEnum.BASIC), config);
        PartGroupDataSet ds = (PartGroupDataSet) plot.getDataSeriesOrder().getFirst();
        ds.getAxisTypeManager().getAxisType("Bottom Axis Data").getScalarFunction().setUnits(ux);
        ds.getAxisTypeManager().getAxisType("Left Axis Data").getScalarFunction().setUnits(uy);

        _io.say.created(plot, true);
        return plot;

    }

}
