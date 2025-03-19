package macroutils.setter;

import macroutils.MacroUtils;
import star.common.BinningDescriptor;
import star.common.Cartesian2DAxis;
import star.common.Cartesian2DPlot;
import star.common.PartGroupDataSet;
import star.common.RangedData;
import star.common.StarPlot;

/**
 * Low-level class for setting Plot parameters with MacroUtils.
 *
 * @since April of 2019
 * @author Fabio Kasper
 */
public class SetPlots {

    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private final MacroUtils _mu;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetPlots(MacroUtils m) {
        _mu = m;
    }

    /**
     * Sets the axes names for a Plot.
     *
     * @param sp    given StarPlot
     * @param xName given x-axis titles
     * @param yName given y-axis titles
     * @param vo    given verbose option. False will not print anything
     */
    public void axesTitles(StarPlot sp, String xName, String yName, boolean vo) {

        _io.say.action("Setting Plot Axes Titles", vo);
        _io.say.object(sp, vo);

        _axisTitle(_get.plots.axisX(sp), "X-Axis", xName);
        _axisTitle(_get.plots.axisY(sp), "Y-Axis", yName);

        _io.say.ok(vo);

    }

    /**
     * Sets the bins number for a Histogram Plot.
     *
     * @param hp    given StarPlot
     * @param bins  given number of bins
     * @param vo    given verbose option. False will not print anything
     */
    public void bins(StarPlot hp, int bins, boolean vo) {
        binsParameters(hp, bins, new double[]{}, vo);
    }

    /**
     * Sets the bins parameters for a Histogram Plot.
     *
     * @param hp    given StarPlot
     * @param bins  given number of bins
     * @param range given range for the bins
     * @param vo    given verbose option. False will not print anything
     */
    public void binsParameters(StarPlot hp, int bins, double[] range, boolean vo) {

        _io.say.action("Setting Histogram Plot range on bins", vo);
        _io.say.object(hp, vo);

        if (_chk.is.histogram(hp) && hp instanceof Cartesian2DPlot plot) {
            plot.getDataSeriesOrder().stream()
                    .filter(PartGroupDataSet.class::isInstance)
                    .map(PartGroupDataSet.class::cast)
                    .forEach(dataSet -> _setBinsParameters(dataSet, bins, range, vo));
            _io.say.ok(vo);
        } else {
            _io.say.msg("Skipped! Plot is not supported", vo);
        }

    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
    }

    private void _axisTitle(Cartesian2DAxis axis, String key, String name) {
        axis.getTitle().setText(name);
        _io.say.value(key, axis.getTitle().getText(), true, true);
    }

    private void _setBinsParameters(PartGroupDataSet ds, int bins, double[] range, boolean vo) {
        _io.say.object(ds, vo);
        _io.say.value("Number of bins", bins, vo);
        _io.say.value("Bin range", range, vo);
        BinningDescriptor descriptor = _get.plots.axisX(ds).hasBinningDescriptor();
        descriptor.setNumberOfBins(bins);
        if (range.length == 2) {
            descriptor.setDataRangeMode(RangedData.RangeMode.Manual);
            descriptor.setManualDataExtentsMin(range[0]);
            descriptor.setManualDataExtentsMax(range[1]);
        }
    }

}
