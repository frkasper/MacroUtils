package macroutils.templates;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NeoObjectVector;
import star.base.report.Report;
import star.common.ExternalDataSet;
import star.common.FileTable;
import star.common.InternalDataSet;
import star.common.Simulation;
import star.common.StarPlot;
import star.common.SymbolShapeOption;
import star.common.SymbolStyle;
import star.common.Units;
import star.common.XYPlot;
import star.common.YAxisType;
import star.common.graph.DataSet;

/**
 * Low-level class for assessing the Grid Convergence Index metric with MacroUtils.
 *
 * @since September of 2016
 * @author Fabio Kasper
 */
public class TemplateGCI {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplateGCI(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private XYPlot _createDuplicatedPlot(XYPlot xyp, FileTable ft) {
        XYPlot xyp2 = _sim.getPlotManager().createXYPlot();
        //--
        String s = "GCI23";
        xyp2.copyProperties(xyp);
        xyp2.setPresentationName(xyp.getPresentationName() + " - " + s);
        xyp2.setTitle("");
        ExternalDataSet eds = _getNewExternalDataSet(xyp2, ft);
        eds.setXValuesName("X");
        eds.setYValuesName(s);
        eds.setPresentationName(s);
        eds.setSeriesName(s);
        eds.getSymbolStyle().setColor(StaticDeclarations.Colors.SLATE_GRAY_DARK.getColor());
        eds.getSymbolStyle().setSize(_mu.templates.prettify.getSymbolSize() + 2);
        eds.getSymbolStyle().getSymbolShapeOption().setSelected(SymbolShapeOption.Type.STAR);
        return xyp2;
    }

    private XYPlot _createPlot(String name, FileTable ft, String[] xx, String[] yy, XYPlot xypOrig) {
        XYPlot xyp = _sim.getPlotManager().createXYPlot();
        xyp.setPresentationName(name);
        xyp.setTitle("");
        for (int i = 0; i < yy.length; i++) {
            String s = yy[i].trim();
            ExternalDataSet eds = _getNewExternalDataSet(xyp, ft);
            eds.setXValuesName(xx[i].trim());
            eds.setYValuesName(s);
            eds.setPresentationName(s);
            eds.setSeriesName(s);
            SymbolStyle ss = eds.getSymbolStyle();
            ss.getSymbolShapeOption().setSelected(SymbolShapeOption.Type.FILLED_CIRCLE);
            switch (i) {
                case 0:
                    eds.getSymbolStyle().setColor(StaticDeclarations.Colors.NAVY.getColor());
                    break;
                case 1:
                    eds.getSymbolStyle().setColor(StaticDeclarations.Colors.DARK_GREEN.getColor());
                    break;
                case 2:
                    eds.getSymbolStyle().setColor(StaticDeclarations.Colors.DARK_ORANGE.getColor());
                    break;
                case 3:
                    eds.getSymbolStyle().setColor(Color.RED);
                    break;
            }
        }
        _set.object.plotAxesTitles(xyp, _get.plots.axisX(xypOrig).getTitle().getText(),
                _get.plots.axisY(xypOrig).getTitle().getText(), false);
        _io.say.created(xyp, true);
        return xyp;
    }

    private void _evaluate(StarPlot sp, ArrayList<File> simFiles, boolean vo) {
        _io.say.action("Performing Roache's GCI calculation on a Plot", vo);
        if (!_isXYPlot(sp)) {
            _io.say.msg("Currently limited to X-Y Plots only.");
            return;
        }
        //-- F1 to F3 == Coarse to Fine
        double[] x1 = null, y1 = null, x2 = null, y2 = null, x3 = null, y3 = null, y1p = null, y2p = null;
        ArrayList<String> als = new ArrayList();
        ArrayList<Double> hs = new ArrayList();
        als.add(_sim.getPresentationName());
        hs.add(_getGridSize(_sim));
        String pltName = sp.getPresentationName();
        sp = evaluate_preExport(_sim, sp);
        //-- Export CSVs
        _exportPlot(sp, _sim.getPresentationName() + ".csv", true);
        for (File sf : new File[]{simFiles.get(1), simFiles.get(0)}) {
            _io.say.msg("Working on File: " + sf.getAbsoluteFile(), vo);
            //--
            Simulation sim2 = new Simulation(sf.toString());
            hs.add(0, _getGridSize(sim2));
            StarPlot sp2 = sim2.getPlotManager().getPlot(pltName);
            als.add(0, sim2.getPresentationName());
            sp2 = evaluate_preExport(sim2, sp2);
            _exportPlot(sp2, sim2.getPresentationName() + ".csv", true);
            sim2.kill();
            //--
        }
        //-- Read CSVs
        for (String name : als) {
            String csv = name + ".csv";
            int n = als.indexOf(name);
            String[] data = _getData(new File(_ud.simPath, csv));
            _io.say.msg(true, "Reading F%d: %s", (n + 1), csv);
            switch (n) {
                case 0:
                    x1 = _getVals(data, columnForAxisX);
                    y1 = _getVals(data, columnForAxisY);
                    break;
                case 1:
                    x2 = _getVals(data, columnForAxisX);
                    y2 = _getVals(data, columnForAxisY);
                    break;
                case 2:
                    x3 = _getVals(data, columnForAxisX);
                    y3 = _getVals(data, columnForAxisY);
                    break;
            }
        }
        //-- Project coarser data into finer Grid
        _io.say.msg("Projecting data...");
        y1p = _getProjectedData(x3, x1, y1);
        y2p = _getProjectedData(x3, x2, y2);
        //-- GCI
        _io.say.msg("Calculating GCI...");
        String[] grids = als.toArray(new String[als.size()]);
        double[] hss = {hs.get(0), hs.get(1), hs.get(2)};       //-- Ugly ArrayList to double[].
        double[] gci12 = new double[y3.length], gci23 = new double[y3.length],
                e12_a = new double[y3.length], e23_a = new double[y3.length],
                gciP = new double[y3.length], gciExtr = new double[y3.length];
        double p_sum = 0.;
        nOscillatoryConvergence = 0;
        for (int i = 0; i < y3.length; i++) {
            _io.say.msg(true, "Station %4d / %d. X = %g.", (i + 1), y3.length, x3[i]);
            double[] gci = evaluate(hss, new double[]{y1p[i], y2p[i], y3[i]}, grids, false);
            gciP[i] = gci[2];
            p_sum += gciP[i];
            e12_a[i] = gci[4];
            e23_a[i] = gci[5];
        }
        double p_ave = p_sum / y3.length;
        _io.say.msg("Reweighting GCI...");
        for (int i = 0; i < y3.length; i++) {
            double r12 = hss[0] / hss[1], r23 = hss[1] / hss[2];
            gci12[i] = _getGCI(1.25, e12_a[i], r12, p_ave);
            gci23[i] = _getGCI(1.25, e23_a[i], r23, p_ave);
            gciExtr[i] = _getExtrapolation(y3[i], y2p[i], r23, p_ave);
            if (Math.abs(gci23[i]) > GCI_LIMIT) {
                _io.say.msg("Overflow detected. Exact will be assumed as F3");
                gciExtr[i] = y3[i];
            }
        }
        if (nOscillatoryConvergence > 0) {
            _io.say.line(true);
            _io.say.msg(true, "Oscillatory convergence found in %d Stations (%.0f%%).",
                    nOscillatoryConvergence, (100. * nOscillatoryConvergence / y3.length));
            _io.say.line(true);
        }
        FileTable ft = _writeAbsoluteCSV(x3, y3, gci23);
        _setupPlots(ft, sp, x1, x2, x3, y1, y2, y3, y1p, y2p, gci12, gci23, gciP, e12_a, e23_a, gciExtr);
        _io.say.ok(true);
    }

    private double[] _evaluate2(double[] h, double[] f, String[] grids, boolean vo) {
        double f1 = f[0], f2 = f[1], f3 = f[2];
        double e21 = f2 - f1, e32 = f3 - f2;
        double r21 = h[1] / h[0], r32 = h[2] / h[1];
        double p = _getP(r21, r32, e32, e21, false);
        double r21p = Math.pow(r21, p);
        double r32p = Math.pow(r32, p);
        double f21_extr = (r21p * f1 - f2) / (r21p - 1.0);
        double e21_a = _get.info.relativeError(f1, f2, true);
        double e32_a = _get.info.relativeError(f2, f3, true);
        double e21_extr = _get.info.relativeError(f21_extr, f1, true);
        double gci21 = 1.25 * e21_a / (r21p - 1.0);
        double gci32 = 1.25 * e32_a / (r32p - 1.0);
        if (Double.isNaN(gci21) || Double.isNaN(gci32)) {
            _io.say.msg("WARNING!!! NaN caught in GCI calculation...");
            String fmt = "%12s, %12s, %12s, %12s, %12s, %12s, %12s";
            _io.say.msg(String.format(fmt, "F1", "F2", "F3", "GCI21", "GCI32", "p", "F21Extrapolated"));
            _io.say.msg(String.format(fmt.replaceAll("s", "g"), f1, f2, f3, gci21, gci32, p, f21_extr));
        }
        _io.say.action("GCI Overview (Fine to Coarse -- Paper original)", vo);
        _io.say.msg(String.format("F1: %12g --> %s (Fine)", f1, grids[0]), vo);
        _io.say.msg(String.format("F2: %12g --> %s (Medium)", f2, grids[1]), vo);
        _io.say.msg(String.format("F3: %12g --> %s (Coarse)", f3, grids[2]), vo);
        _io.say.value("Grid Sizes", new DoubleVector(h), vo);
        _io.say.line(vo);
        String fmt = "%-30s = %12g";
        String fmtP = fmt.replace("g", ".2f%%");
        _io.say.msg(String.format(fmtP, "GCI21", gci21), vo);
        _io.say.msg(String.format(fmtP, "GCI32", gci32), vo);
        _io.say.msg(String.format(fmt, "Apparent Order", p), vo);
        _io.say.msg(String.format(fmt, "Extrapolated (f21_extr)", f21_extr), vo);
        _io.say.msg(String.format(fmtP, "Approximate Error (E21_a)", e21_a), vo);
        _io.say.msg(String.format(fmtP, "Extrapolated Error (E21_extr)", e21_extr), vo);
        _io.say.line(vo);
        return new double[]{gci21, gci32, p, f21_extr};
    }

    /**
     * Exports the Plot as CSV with option to sort data.
     *
     * @param sp given StarPlot.
     * @param csvName given CSV name.
     */
    private void _exportPlot(StarPlot sp, String csvName, boolean sortData) {
        String name = csvName;
        if (!name.toLowerCase().contains(".csv")) {
            name += ".csv";
        }
        if (sortData && _isXYPlot(sp)) {
            XYPlot p = (XYPlot) sp;
            YAxisType y = (YAxisType) p.getYAxes().getDefaultAxis();
            InternalDataSet id = (InternalDataSet) y.getDataSetManager().getDataSets().iterator().next();
            id.setNeedsSorting(true);
        }
        sp.export(new File(_ud.simPath, name), ",");
        _io.say.msg("CSV Exported: " + name);
    }

    private double _getBeta(double r21, double r32, double e32, double e21, double p) {
        return e32 / e21 * (Math.pow(r21, p) - 1.0) / (Math.pow(r32, p) - 1.0);
    }

    private String[] _getData(File file) {
        String data = _io.read.data(file, false);
        return data.split("\\n");
    }

    private Double[] _getDouble(double[] array) {
        Double[] nd = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            nd[i] = array[i];
        }
        return nd;
    }

    private double _getExtrapolation(double f2, double f1, double r, double p) {
        double rp = Math.pow(r, p);
        return (rp * f2 - f1) / (rp - 1.0);
    }

    private ArrayList<File> _getFiles(String[] files) {
        ArrayList<File> af = new ArrayList();
        for (String file : files) {
            af.add(new File(_ud.simPath, file));
        }
        return af;
    }

    private double _getGCI(double fs, double e_a, double r, double p) {
        return fs * e_a / (Math.pow(r, p) - 1.0);
    }

    private double _getGridSize(Simulation s) {
        double h = 0;
        _io.say.action("Getting current grid size", true);
        if (s.equals(_sim)) {
            h = _getGridSizeThisSim();
        } else {
            h = _getGridSizeOtherSim(s, _ud.defUnitLength.getPresentationName());
        }
        _io.say.value("Grid size", h, _ud.defUnitLength, true);
        _io.say.ok(true);
        return h;
    }

    private double _getGridSizeOtherSim(Simulation s, String defUnitLengthName) {
        MacroUtils mu2 = new MacroUtils(s, false);
        UserDeclarations ud2 = mu2.userDeclarations;
        double cc = mu2.get.mesh.fvr().getCellCount();
        Report r = mu2.add.report.sum(new ArrayList(mu2.get.regions.all(false)), "_sumVolumeCells",
                mu2.get.objects.fieldFunction(StaticDeclarations.Vars.VOL.getVar(), false),
                ud2.unit_m3, false);
        double sumVC = r.getReportMonitorValue();
        Units defUnitLength2 = s.getUnitsManager().getObject(defUnitLengthName);
        double convFactor = ud2.unit_m.getConversion() / defUnitLength2.getConversion();
        return Math.cbrt(sumVC / cc) * convFactor;
    }

    private double _getGridSizeThisSim() {
        double cc = _get.mesh.fvr().getCellCount();
        Report r = _add.report.sum(new ArrayList(_get.regions.all(false)), "_sumVolumeCells",
                _get.objects.fieldFunction(StaticDeclarations.Vars.VOL.getVar(), false),
                _ud.unit_m3, false);
        double sumVC = r.getReportMonitorValue();
        double convFactor = _ud.unit_m.getConversion() / _ud.defUnitLength.getConversion();
        return Math.cbrt(sumVC / cc) * convFactor;
    }

    private ExternalDataSet _getNewExternalDataSet(XYPlot xyp, FileTable ft) {
        ArrayList<DataSet> datasetsOld = new ArrayList(xyp.getDataSetManager().getExternalDataSets());
        xyp.getDataSetManager().addDataProviders(new NeoObjectVector(new Object[]{ft}));
        ArrayList<DataSet> datasetsNew = new ArrayList(xyp.getDataSetManager().getExternalDataSets());
        datasetsNew.removeAll(datasetsOld);
        return (ExternalDataSet) datasetsNew.get(0);
    }

    private double _getP(double r21, double r32, double e32, double e21, boolean vo) {
        double om = 0.5;
        double p = _getBeta(r21, r32, e32, e21, 1);
        boolean hadNaN = false;
        for (int i = 1; i <= 50; i++) {
            double beta = _getBeta(r21, r32, e32, e21, p);
            if (e32 / e21 < 0 || beta < 0) {
                beta = Math.abs(beta);
            }
            double p1 = om * p + (1.0 - om) * Math.log(beta) / Math.log(r21);
            if (Double.isNaN(beta) || Double.isNaN(p1)) {
                hadNaN = true;
                _io.say.msg("WARNING!!! NaN caught in GCI Apparent Order calculation (p)...");
                String fmt = "%12s, %12s, %12s, %12s, %12s, %12s, %12s";
                _io.say.msg(String.format(fmt, "r21", "r32", "e32", "e21", "beta", "p", "p1"));
                _io.say.msg(String.format(fmt.replaceAll("s", "g"), r21, r32, e32, e21, beta, p, p1));
                vo = true;
            }
            _io.say.msg(vo, "Iter %02d:", i);
            _io.say.msg(vo, "  p  = %g", p);
            _io.say.msg(vo, "  p1 = %g", p1);
            double tol = Math.abs((p1 - p) / p);
            p = p1;
            if (tol <= 1e-5 || hadNaN) {
                break;
            }
        }
        return p;
    }

    /**
     * Project coarser data into finer Grid. Data must be sorted first.
     *
     * @param xx0 double[] of finer x values
     * @param xx1 double[] of coarser x values
     * @param yy1 double[] of coarser y values
     * @return double[] of projected coarser y values into finer x
     */
    private double[] _getProjectedData(double[] xx0, double[] xx1, double[] yy1) {
        double[] yy1p = new double[xx0.length];
        for (int i = 0; i < xx0.length; i++) {
            Double x0 = xx0[i];
            double[] range = _getRange(x0, xx1);
            //_io.say.msg(true, "### Got Range: {%6.2f  //  %6.2f  //   %6.2f}", range[0], x0, range[1]);
            int i0 = Arrays.asList(_getDouble(xx1)).indexOf(range[0]);
            int i1 = Arrays.asList(_getDouble(xx1)).indexOf(range[1]);
            yy1p[i] = _get.info.linearRegression(new double[]{range[0], range[1]},
                    new double[]{yy1[i0], yy1[i1]}, x0, false, false);
        }
        return yy1p;
    }

    /**
     * Return the coarser data pair in range to the finer given point. Assumes sorted data.
     *
     * @param x0 finer point.
     * @param xx coarser points.
     * @return double[] range data pair.
     */
    private double[] _getRange(double x0, double[] xx) {
        //-- Eliminate Last occurrence.
        for (int i = 0; i < xx.length - 1; i++) {
            double x1 = xx[i];
            double x2 = xx[i + 1];
            //say(String.format("   Working Range (i=%2d): %6.2f <= %6.2f <= %6.2f ???", i, x1, x0, x2));
            if (x0 >= x1 && x0 <= x2) {
                return new double[]{x1, x2};
            }
            if (x0 >= xx[xx.length - 1]) {
                return new double[]{xx[xx.length - 2], xx[xx.length - 1]};
            }
        }
        return new double[]{xx[0], xx[1]};
    }

    private double[] _getVals(String[] data, int i) {
        ArrayList<Double> ard = new ArrayList();
        Double val;
        for (String line : data) {
            if (line.contains("\"")) {
                continue;
            }
            String[] cols = line.split(",");
            try {
                val = new Double(cols[i]);
            } catch (NumberFormatException e) {
                continue;
            }
            ard.add(val);
        }
        double[] vals = new double[ard.size()];
        for (int j = 0; j < ard.size(); j++) {
            vals[j] = ard.get(j);
        }
        return vals;
    }

    private boolean _isXYPlot(StarPlot sp) {
        return sp instanceof XYPlot;
    }

    private void _setupPlots(FileTable ft, StarPlot sp, double[] x1, double[] x2, double[] x3,
            double[] y1, double[] y2, double[] y3, double[] y1p, double[] y2p,
            double[] gci12, double[] gci23, double[] gciP, double[] e12_a, double[] e23_a, double[] gciExtr) {
        ArrayList<String> data = new ArrayList();
        ArrayList<String> plots = new ArrayList();
        //-- Changing the original Plot and adding stuff.
        _io.say.msg("Changing Plot: " + sp.getPresentationName());
        XYPlot xyp = (XYPlot) sp;
        XYPlot xypGCI = _createDuplicatedPlot(xyp, ft);
        plots.add(xypGCI.getPresentationName());
        //--
        //-- Write a GCI CSV with original and projected data.
        _io.say.msg("Creating new Plots...");
        data.clear();
        data.add("X, GCI12, GCI23, Order, E12_a, E23_a, X1, F1, X2, F2, X3, F3, F1P, F2P, F3EXACT");
        String sx1, sf1, sx2, sf2;
        for (int i = 0; i < x3.length; i++) {
            try {
                sx1 = String.format("%g", x1[i]);
                sf1 = String.format("%g", y1[i]);
            } catch (Exception e) {
                sx1 = "null";
                sf1 = "null";
            }
            try {
                sx2 = String.format("%g", x2[i]);
                sf2 = String.format("%g", y2[i]);
            } catch (Exception e) {
                sx2 = "null";
                sf2 = "null";
            }
            data.add(String.format("%g,%g,%g,%g,%g,%g,%s,%s,%s,%s,%g,%g,%g,%g,%g",
                    x3[i], gci12[i], gci23[i], gciP[i], e12_a[i], e23_a[i], sx1, sf1, sx2, sf2,
                    x3[i], y3[i], y1p[i], y2p[i], gciExtr[i]));
        }
        File gciCsv = new File(_ud.simPath, "GCI.csv");
        _io.write.data(gciCsv, data, false);
        _io.say.msg("Written GCI CSV file: " + gciCsv.getName());
        FileTable ft2 = (FileTable) _sim.getTableManager().createFromFile(gciCsv.toString());
        //-- F1, F2 & F3 plots
        XYPlot xyp2 = _createPlot("Original Grids Solutions", ft2, "X1,X2,X3".split(","),
                "F1,F2,F3".split(","), xyp);
        plots.add(xyp2.getPresentationName());
        XYPlot xyp3 = _createPlot("Projected Grids Solutions", ft2, "X3,X3,X3,X3".split(","),
                "F1P,F2P,F3,F3EXACT".split(","), xyp);
        plots.add(xyp3.getPresentationName());
        _templ.prettify.plots();
        for (Object o : plots) {
            _get.plots.byREGEX((String) o, false).open();
        }
    }

    private FileTable _writeAbsoluteCSV(double[] x3, double[] y3, double[] gci23) {
        //-- Writes the Absolute GCI CSV file.
        ArrayList<String> data = new ArrayList();
        data.add(String.format("X, GCI23"));
        for (int i = 0; i < x3.length; i++) {
            if (Math.abs(gci23[i]) > GCI_LIMIT) {
                continue;
            }
            data.add(String.format("%g,%g", x3[i], (1. + Math.abs(gci23[i])) * y3[i]));
            data.add(String.format("%g,%g", x3[i], (1. - Math.abs(gci23[i])) * y3[i]));
        }
        File newCsv = new File(_ud.simPath, "AbsoluteGCI.csv");
        _io.write.data(newCsv, data, false);
        _io.say.msg("New CSV file written: " + newCsv.getName());
        return (FileTable) _sim.getTableManager().createFromFile(newCsv.toString());
    }

    /**
     * The Grid Convergence Index (GCI) method below implements a variation of Celik et al. paper, i.e., F1, F2 and F3
     * are the coarse, medium and fine solutions respectively.
     * <p>
     * For more information, see {@link #evaluate2}.
     *
     * @param h given array of doubles containing grid sizes in the <b>following order:
     * <u>coarse (F1), medium (F2) and fine (F3) grid values</u></b>.
     * @param f given array of doubles containing solution values in the same order above.
     * @param grids given array of strings containing the grid names in the same order as others.
     * @return An array with doubles in the form {GCI12, GCI23, Order (p), F23_Extrapolated, E12_a, E23_a}. More
     * information as follows:
     * <ul>
     * <li><b>GCI<i>ij</i></b> are the Grid Convergence Indexes;
     * <li><b>Order</b> is the Apparent Order;
     * <li><b>F23_Extrapolated</b> is the Exact solution according to Richardson Extrapolation;
     * <li><b>E<i>ij</i>_a</b> are the Approximate Errors (Relative Errors) between meshes <i>i</i> and <i>j</i>.
     * </ul>
     */
    public double[] evaluate(double[] h, double[] f, String[] grids) {
        return evaluate(h, f, grids, true);
    }

    private double[] evaluate(double[] h, double[] f, String[] grids, boolean vo) {
        double f1 = f[0], f2 = f[1], f3 = f[2];
        double e12 = f1 - f2, e23 = f2 - f3;
        double r12 = h[0] / h[1], r23 = h[1] / h[2];
        double beta = _getBeta(r23, r12, e12, e23, 1);
        if (e23 / e12 < 0 || beta < 0) {
            _io.say.msg("Warning! Oscillatory convergence detected in GCI calculation.");
            _io.say.msg("To avoid a NaN, beta will be used as absolute in the Apparent Order calculation (p).");
            nOscillatoryConvergence++;
        }
        double p = _getP(r23, r12, e12, e23, false);
        //_io.say.msg("p = " + p);
        double f23_extr = _getExtrapolation(f3, f2, r23, p);
        double e12_a = _get.info.relativeError(f2, f1, true);
        double e23_a = _get.info.relativeError(f3, f2, true);
        double e23_extr = _get.info.relativeError(f23_extr, f3, true);
        double gci12 = _getGCI(1.25, e12_a, r12, p);
        double gci23 = _getGCI(1.25, e23_a, r23, p);
        if (Math.abs(gci23) > 10.0) {
            _io.say.msg("Overflow detected. Being verbose...");
            String fmt = "%12s, %12s, %12s, %12s, %12s, %12s";
            _io.say.msg(String.format(fmt, "r12", "r23", "e23", "e12", "beta", "p"));
            _io.say.msg(String.format(fmt.replaceAll("s", "g"), r12, r23, e23, e12, beta, p));
            vo = true;
        }
        _io.say.action("GCI Overview (Coarse to Fine -- Paper variation)", vo);
        String fmt = "%s: %12g --> %s (%s - Grid Size = %g__)".replace("__", _ud.defUnitLength.getPresentationName());
        _io.say.msg(String.format(fmt, "F1", f1, grids[0], "Coarse", h[0]), vo);
        _io.say.msg(String.format(fmt, "F2", f2, grids[0], "Medium", h[1]), vo);
        _io.say.msg(String.format(fmt, "F3", f3, grids[0], "Fine", h[2]), vo);
        _io.say.line(vo);
        fmt = "%-30s = %12g";
        String fmtP = fmt.replace("g", ".2f%%");
        _io.say.msg(String.format(fmtP, "GCI23", gci23), vo);
        _io.say.msg(String.format(fmtP, "GCI12", gci12), vo);
        _io.say.msg(String.format(fmt, "Apparent Order", p), vo);
        _io.say.msg(String.format(fmt, "Extrapolated (E23_extr)", f23_extr), vo);
        _io.say.msg(String.format(fmtP, "Approximate Error (E23_a)", e23_a), vo);
        _io.say.msg(String.format(fmtP, "Extrapolated Error (E23_extr)", e23_extr), vo);
        _io.say.line(vo);
        return new double[]{gci12, gci23, p, f23_extr, e12_a, e23_a};
    }

    /**
     * Evaluates the Grid Convergence Index (GCI) method below for a series of grids. For more information, see
     * {@link #evaluate(double[], double[], java.lang.String[])}.
     *
     * @param gridSizes given ArrayList of Doubles containing grid sizes in the <b>following order:
     * <u>coarse (F1), medium (F2) and fine (F3) grid values</u></b>.
     * @param vals given ArrayList of Doubles containing solution values in the same order above.
     * @param grids given ArrayList of Strings containing the grid names in the same order as others.
     */
    public void evaluate(ArrayList<Double> gridSizes, ArrayList<Double> vals, ArrayList<String> grids) {
        String fmt = "%-35s %12g %12g %12.3f %12.2f %12g";
        String fmtS = "%-35s %12s %12s %12s %12s %12s";
        ArrayList<String> toSay = new ArrayList();
        String size = String.format("Size (%s)", _ud.defUnitLength.getPresentationName());
        toSay.add(String.format(fmtS, "Grid Name", "Value", size, "GCI (%)", "ORDER", "EXACT"));
        _io.say.loud("Assessing Grid Convergence Index");
        double gci = 0., p = 0., fe = 0.;
        for (double val : vals) {
            int i = vals.indexOf(val);
            if (i >= 2) {
                double[] _sizes = {gridSizes.get(i), gridSizes.get(i - 1), gridSizes.get(i - 2)};
                double[] _vals = {vals.get(i), vals.get(i - 1), vals.get(i - 2)};
                String[] _grids = {grids.get(i), grids.get(i - 1), grids.get(i - 2)};
                double[] gciRes = evaluate(_sizes, _vals, _grids, false);
                gci = gciRes[1];
                p = gciRes[2];
                fe = gciRes[3];
            }
            toSay.add(String.format(fmt, grids.get(i), vals.get(i), gridSizes.get(i), gci, p, fe));
        }
        for (String s : toSay) {
            _io.say.msg(s);
        }
        _io.say.ok(true);
    }

    /**
     * Calculates the Grid Convergence Index for 3 sim files, using the Roache's approach.
     * <p>
     * <b>Procedure:</b> <ul>
     * <li> The finest grid must be loaded and active in STAR-CCM+;
     * <li> Pick a Plot to evaluate;
     * <li> The 2 other sim files will be loaded in background and the CSV files will be exported from the Plots;
     * <li> Coarser solutions will be projected on the CSV of the finest grid;
     * <li> GCI is then calculated and the original Plot will be changed.
     * </ul>
     *
     * <b>Notes:</b> <ul>
     * <li> This method will checkout a second license of STAR-CCM+;
     * <li> The reliability of this method depends on the accuracy of how the solution is projected;
     * <li> Method is limited to sorted data in the Plot;
     * <li> If there is more than one DataSet in the plot, only the first X-Y pair will be used;
     * <li> This method is based on {@link #evaluate(double[], double[], java.lang.String[])}.
     * </ul>
     *
     * @param sp given StarPlot.
     * @param simFiles array of sim files. Currently limited to 2 and must be ordered the same way as required by
     * {@link #evaluate}: e.g.: {"coarseGrid.sim" , "mediumGrid.sim"}. In addition, must be in the same path as
     * {@link UserDeclarations#simPath} variable. If more arguments are provided, only the first two will be used as the
     * macro will take the current simulation as the 3rd one, i.e., the fine grid.
     */
    public void evaluate(StarPlot sp, String[] simFiles) {
        _evaluate(sp, _getFiles(simFiles), true);
    }

    /**
     * Calculates the Grid Convergence Index for 3 sim files, using the Roache's approach.
     * <p>
     * <b>Procedure:</b> <ul>
     * <li> The finest grid must be loaded and active in STAR-CCM+;
     * <li> Pick a Plot to evaluate;
     * <li> The 2 other sim files will be loaded in background and the CSV files will be exported from the Plots;
     * <li> Coarser solutions will be projected on the CSV of the finest grid;
     * <li> GCI is then calculated and the original Plot will be changed.
     * </ul>
     *
     * <b>Notes:</b> <ul>
     * <li> This method will checkout a second license of STAR-CCM+;
     * <li> The reliability of this method depends on the accuracy of how the solution is projected;
     * <li> Method is limited to sorted data in the Plot;
     * <li> If there is more than one DataSet in the plot, only the first X-Y pair will be used;
     * <li> This method is based on {@link #evaluate(double[], double[], java.lang.String[])}.
     * </ul>
     *
     * @param sp given StarPlot.
     * @param simFiles ArrayList of @{see java.io.File} containing the sim files. Currently limited to 2 and the coarse
     * grid must have index 0. If more items are inside the ArrayList, only the first 2 will be used as the macro will
     * take the current simulation as the 3rd one, i.e., the fine grid.
     */
    public void evaluate(StarPlot sp, ArrayList<File> simFiles) {
        _evaluate(sp, simFiles, true);
    }

    /**
     * This method is in conjunction with {@link #evaluate(star.common.StarPlot, java.util.ArrayList)} and it only works
     * along with an @Override call. Useful for adding some code before exporting the CSV file.
     *
     * It is invoked prior to exporting the CSV file from the Plot.
     *
     * @param s given Simulation.
     * @param sp given StarPlot.
     * @return A StarPlot. In case one needs to create a brand new on the fly.
     */
    public StarPlot evaluate_preExport(Simulation s, StarPlot sp) {
        //-- Use it with @override
        return sp;
    }

    /**
     * The GCI method below implements exactly as it is given in the Paper, i.e., F1, F2 and F3 are the fine, medium and
     * coarse solutions respectively.
     * <p>
     * <b>References</b>:
     * <p>
     * Celik, et al., 2008. <u>Procedure for Estimation and Reporting of Uncertainty Due to Discretization in CFD
     * Applications</u>. Journal of Fluids Engineering. Vol. 130.
     * <p>
     * Roache, P. J., 1997. <u>Quantification of Uncertainty in Computational Fluid Dynamics.</u>. Annu. Rev. Fluid.
     * Mech. 29:123-60.
     *
     * @param h given array of doubles containing grid sizes in the <b>following order:
     * <u>fine (F1), medium (F2) and coarse (F3) grid values</u></b>.
     * @param f given array of doubles containing solution values in the same order above.
     * @param grids given array of strings containing the grid names in the same order as others.
     * @return An array with doubles in the form {GCI21, GCI32, Order (p), F21_Extrapolated}.
     */
    public double[] evaluate2(double[] h, double[] f, String[] grids) {
        return _evaluate2(h, f, grids, true);
    }

    /**
     * Gets the current grid size for the GCI calculation. Only useful with {@link #evaluate}.
     *
     * @return The grid size in the {@link UserDeclarations#defUnitLength} unit.
     */
    public double getGridSize() {
        return _getGridSize(_sim);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _templ = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private static final double GCI_LIMIT = 10.0;       //-- 1000% error should be skipped.

    private int nOscillatoryConvergence = 0;
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.templates.MainTemplates _templ = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

    /**
     * Default reading X column in a CSV when performing GCI calculations with {@link #evaluate}. Default = 0.
     */
    public int columnForAxisX = 0;

    /**
     * Default reading Y column in a CSV when performing GCI calculations with {@link #evaluate}. Default = 1.
     */
    public int columnForAxisY = 1;

}
