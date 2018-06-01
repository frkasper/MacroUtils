package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.common.AxisType;
import star.common.Cartesian2DAxis;
import star.common.Cartesian2DAxisManager;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.XYPlot;
import star.common.YAxisType;
import star.common.graph.DataSet;

/**
 * Low-level class for getting Plots with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetPlots {

    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetPlots(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Plots available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of StarPlots.
     */
    public ArrayList<StarPlot> all(boolean vo) {
        ArrayList<StarPlot> as = new ArrayList<>(_sim.getPlotManager().getObjects());
        _io.say.objects(as, "Getting all Plots", vo);
        return as;
    }

    /**
     * Gets all Plots that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of StarPlots.
     */
    public ArrayList<StarPlot> allByREGEX(String regexPatt, boolean vo) {
        return _get.objects.allByREGEX(regexPatt, "Plots", all(false), vo);
    }

    /**
     * Gets the X axis of a Plot.
     *
     * @param sp given StarPlot.
     * @return The Cartesian2DAxis object.
     */
    public Cartesian2DAxis axisX(StarPlot sp) {
        return _axis(sp, "Bottom Axis");
    }

    /**
     * Gets the Y axis of a Plot.
     *
     * @param sp given StarPlot.
     * @return The Cartesian2DAxis object.
     */
    public Cartesian2DAxis axisY(StarPlot sp) {
        return _axis(sp, "Left Axis");
    }

    /**
     * Gets a StarPlot that matches the REGEX search pattern among all Plots available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The StarPlot. Null if nothing is found.
     */
    public StarPlot byREGEX(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt, "Plot", all(false), vo);
    }

    /**
     * Gets all DataSets available in a Plot.
     *
     * @param sp given StarPlot.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList with DataSets.
     */
    public ArrayList<DataSet> datasets(StarPlot sp, boolean vo) {
        _io.say.object(sp, vo);
        ArrayList<DataSet> ads = new ArrayList<>(sp.getDataSetManager().getDataSets());
        if (sp instanceof XYPlot) {
            XYPlot xyp = (XYPlot) sp;
            for (AxisType at : xyp.getYAxes().getObjects()) {
                if (at instanceof YAxisType) {
                    ads.addAll(((YAxisType) at).getDataSetManager().getDataSets());
                }
            }
        }
        _io.say.objects(ads, "Datasets", vo);
        return ads;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    private Cartesian2DAxis _axis(StarPlot sp, String axisName) {
        return (Cartesian2DAxis) _axisManager(sp).getAxis(axisName);
    }

    private Cartesian2DAxisManager _axisManager(StarPlot sp) {
        return (Cartesian2DAxisManager) sp.getAxisManager();
    }

}
