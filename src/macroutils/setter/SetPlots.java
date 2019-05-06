package macroutils.setter;

import macroutils.MacroUtils;
import star.base.neo.DoubleVector;
import star.common.Cartesian2DAxis;
import star.common.HistogramAxisType;
import star.common.HistogramBinMode;
import star.common.HistogramPlot;
import star.common.StarPlot;

/**
 * Low-level class for setting Plot parameters with MacroUtils.
 *
 * @since April of 2019
 * @author Fabio Kasper
 */
public class SetPlots {

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

        axisTitle(_get.plots.axisX(sp), "X-Axis", xName);
        axisTitle(_get.plots.axisY(sp), "Y-Axis", yName);

        _io.say.ok(vo);

    }

    /**
     * Sets the bin parameters for a Histogram Plot.
     *
     * @param hp     given HistogramPlot
     * @param number given number of bins
     * @param range  given range for the bins
     * @param vo     given verbose option. False will not print anything
     */
    public void binParameters(HistogramPlot hp, int number, double[] range, boolean vo) {

        _io.say.action("Setting Histogram Plot range on bins", vo);
        _io.say.object(hp, vo);

        HistogramAxisType axisType = hp.getXAxisType();
        axisType.setNumberOfBin(number);
        axisType.setBinMode(HistogramBinMode.MANUAL);
        axisType.getHistogramRange().setRange(new DoubleVector(range));

        _io.say.value("Number of bins", number, vo);
        _io.say.value("Bin rang", range, vo);

        _io.say.ok(vo);

    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    private void axisTitle(Cartesian2DAxis axis, String key, String name) {
        axis.getTitle().setText(name);
        _io.say.value(key, axis.getTitle().getText(), true, true);
    }

}
