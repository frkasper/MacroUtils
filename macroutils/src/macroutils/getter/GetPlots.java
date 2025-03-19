package macroutils.getter;

import java.util.ArrayList;
import java.util.List;
import macroutils.MacroUtils;
import macroutils.checker.MainChecker;
import star.base.report.graph.MonitorAxisType;
import star.base.report.graph.MultiAxisMonitorDataSet;
import star.common.Cartesian2DAxis;
import star.common.Cartesian2DAxisManager;
import star.common.PartAxisType;
import star.common.PartGroupDataSet;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.TableColumnAxisType;
import star.common.TableDataSet;
import star.common.graph.DataSet;

/**
 * Low-level class for getting Plots with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetPlots {

    private MainChecker _chk = null;
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
     * Gets the X axis of a DataSet.
     *
     * @param mds given MultiAxisMonitorDataSet
     * @return The MonitorAxisType
     */
    public MonitorAxisType axisX(MultiAxisMonitorDataSet mds) {
        return mds.getAxisTypeManager().getAxisType("Bottom Axis Data");
    }

    /**
     * Gets the Y axis of a DataSet.
     *
     * @param mds given MultiAxisMonitorDataSet
     * @return The MonitorAxisType
     */
    public MonitorAxisType axisY(MultiAxisMonitorDataSet mds) {
        return mds.getAxisTypeManager().getAxisType("Left Axis Data");
    }

    /**
     * Gets the X axis of a DataSet.
     *
     * @param pgds given PartGroupDataSet
     * @return The PartAxisType
     */
    public PartAxisType axisX(PartGroupDataSet pgds) {
        String suffix = _chk.is.histogram(pgds) ? "Binned Data" : "Data";
        return pgds.getAxisTypeManager().getAxisType("Bottom Axis " + suffix);
    }

    /**
     * Gets the Y axis of a DataSet.
     *
     * @param pgds given PartGroupDataSet
     * @return The PartAxisType
     */
    public PartAxisType axisY(PartGroupDataSet pgds) {
        String suffix = _chk.is.histogram(pgds) ? "Weight Data" : "Data";
        return pgds.getAxisTypeManager().getAxisType("Left Axis " + suffix);
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
     * Gets the X axis of a DataSet.
     *
     * @param tds given TableDataSet
     * @return The TableColumnAxisType
     */
    public TableColumnAxisType axisX(TableDataSet tds) {
        return tds.getAxisTypeManager().getAxisType("Bottom Axis Data");
    }

    /**
     * Gets the Y axis of a DataSet.
     *
     * @param tds given TableDataSet
     * @return The TableColumnAxisType
     */
    public TableColumnAxisType axisY(TableDataSet tds) {
        return tds.getAxisTypeManager().getAxisType("Left Axis Data");
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
    public List<DataSet> datasets(StarPlot sp, boolean vo) {
        _io.say.object(sp, vo);
        List<DataSet> ads = sp.getDataSetCollection();
        _io.say.objects(ads, "Datasets", vo);
        return ads;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
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
