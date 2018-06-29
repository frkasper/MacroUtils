package macroutils.getter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.templates.TemplatePost;
import star.common.Simulation;

/**
 * Low-level class for retrieving Information in general with MacroUtils.
 *
 * @since January of 2016
 * @author Fabio Kasper
 */
public class GetInfos {

    private macroutils.io.MainIO _io = null;
    private int _lastInterval;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetInfos(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Evaluates a simple Linear Regression equation in the form: <b>y = a * x + b</b>.
     *
     * @param xx      given independent variable interval. E.g.: { x0, x1 }.
     * @param yy      given dependent variable interval. E.g.: { y0, y1 }.
     * @param x       given independent variable value for y(x) to be evaluated.
     * @param clipOpt option to clip the extremes. E.g.: y0 &lt;= y(x) &lt;= y1.
     * @param vo      given verbose option. False will not print anything.
     * @return The value at y(x).
     */
    public double linearRegression(double[] xx, double[] yy, double x, boolean clipOpt,
            boolean vo) {
        _io.say.action("Evaluating a Simple Regression: y = a * x + b", vo);
        double a, b, y;
        a = (yy[1] - yy[0]) / (xx[1] - xx[0]);
        b = yy[1] - a * xx[1];
        y = a * x + b;
        _io.say.msg(vo, "xx = {%g, %g}; yy = {%g, %g}", xx[0], xx[1], yy[0], yy[1]);
        _io.say.msg(vo, "a = %g.", a);
        _io.say.msg(vo, "b = %g.", b);
        _io.say.msg(vo, "y(%g) = %g", x, y);
        if (clipOpt) {
            if (x <= xx[0]) {
                _io.say.msg(vo, "  x <= x0. Clipped to y(%g) = %g", xx[0], yy[0]);
                y = yy[0];
            }
            if (x >= xx[1]) {
                _io.say.msg(vo, "  x >= x1. Clipped to y(%g) = %g", xx[1], yy[1]);
                y = yy[1];
            }
        }
        _io.say.ok(vo);
        return y;
    }

    /**
     * Gets the relative error between 2 numbers.
     *
     * @param n1     given number 1.
     * @param n2     given number 2.
     * @param optAbs option to have absolute relative error (without sign).
     * @return The relative error.
     */
    public double relativeError(double n1, double n2, boolean optAbs) {
        double relError = (n1 - n2) / (n2 + StaticDeclarations.SMALL_NUMBER);
        if (optAbs) {
            return Math.abs(relError);
        }
        return relError;
    }

    /**
     * Gets the spline interpolation between the set of given numbers. This method is used by
     * {@link TemplatePost}.
     *
     * @param ax an ArrayList of independent values.
     * @param ay an ArrayList of dependent values.
     * @return A double[][].
     */
    public double[][] spline(ArrayList<Double> ax, ArrayList<Double> ay) {
        //----------------------------------------------------------------
        //-- CREDITS
        //----------------------------------------------------------------
        //--
        //-- This method was downloaded on December, 2012, from a website
        //-- belonging to Dr. Jon Squire, Adjunct Faculty
        //-- http://www.csee.umbc.edu/~squire/
        //--
        //-- It was quickly adapted to fit into this Macro.
        //--
        //----------------------------------------------------------------
        //--
        //-- Trick converting ArrayList<Double> to double[]
        double[] xx = new double[ax.size()];
        double[] ff = new double[ay.size()];
        for (int i = 0; i < ay.size(); i++) {
            xx[i] = ax.get(i);
            ff[i] = ay.get(i);
        }
        //--
        int n = ax.size();
        double fp1, fpn, h, p;
        _lastInterval = 0;
        double[] x = new double[n];
        double[] f = new double[n];
        double[] b = new double[n];
        double[] c = new double[n];
        double[] d = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = xx[i];
            f[i] = ff[i];
            _io.say.msgDebug("Spline data x[" + i + "]=" + x[i] + ", f[]=" + f[i]);
        }
        //-- Calculate coefficients for the tri-diagonal system: store
        //-- sub-diagonal in b, diagonal in d, difference quotient in c.
        b[0] = x[1] - x[0];
        c[0] = (f[1] - f[0]) / b[0];
        d[0] = 2.0 * b[0];
        for (int i = 1; i < n - 1; i++) {
            b[i] = x[i + 1] - x[i];
            c[i] = (f[i + 1] - f[i]) / b[i];
            d[i] = 2.0 * (b[i] + b[i - 1]);
        }
        d[n - 1] = 2.0 * b[n - 2];
        //-- Calculate estimates for the end slopes.  Use polynomials
        //-- interpolating data nearest the end.
        fp1 = c[0] - b[0] * (c[1] - c[0]) / (b[0] + b[1]);
        if (n > 3) {
            fp1 = fp1 + b[0] * ((b[0] + b[1]) * (c[2] - c[1])
                    / (b[1] + b[2]) - c[1] + c[0]) / (x[3] - x[0]);
        }
        fpn = c[n - 2] + b[n - 2] * (c[n - 2] - c[n - 3]) / (b[n - 3] + b[n - 2]);
        if (n > 3) {
            fpn = fpn + b[n - 2] * (c[n - 2] - c[n - 3] - (b[n - 3] + b[n - 2])
                    * (c[n - 3] - c[n - 4]) / (b[n - 3] + b[n - 4]))
                    / (x[n - 1] - x[n - 4]);
        }
        //--
        //-- Calculate the right-hand-side and store it in c.
        c[n - 1] = 3.0 * (fpn - c[n - 2]);
        for (int i = n - 2; i > 0; i--) {
            c[i] = 3.0 * (c[i] - c[i - 1]);
        }
        c[0] = 3.0 * (c[0] - fp1);
        //--
        //-- Solve the tridiagonal system.
        for (int k = 1; k < n; k++) {
            p = b[k - 1] / d[k - 1];
            d[k] = d[k] - p * b[k - 1];
            c[k] = c[k] - p * c[k - 1];
        }
        c[n - 1] = c[n - 1] / d[n - 1];
        for (int k = n - 2; k >= 0; k--) {
            c[k] = (c[k] - b[k] * c[k + 1]) / d[k];
        }
        //--
        //-- Calculate the coefficients defining the spline.
        h = x[1] - x[0];
        for (int i = 0; i < n - 1; i++) {
            h = x[i + 1] - x[i];
            d[i] = (c[i + 1] - c[i]) / (3.0 * h);
            b[i] = (f[i + 1] - f[i]) / h - h * (c[i] + h * d[i]);
        }
        b[n - 1] = b[n - 2] + h * (2.0 * c[n - 2] + h * 3.0 * d[n - 2]);
        _io.say.msgDebug("spline coefficients");
        return new double[][]{ f, x, b, c, d };
    }

    /**
     * Gets the spline value based on a set of coefficients. This method is used by
     * {@link TemplatePost}.
     *
     * @param splineCoeffs given spline coefficients.
     * @param t            given interval the spline needs to be evaluated.
     * @return A double.
     */
    public double splineValue(double[][] splineCoeffs, double t) {
        int interval; // index such that t>=x[interval] and t<x[interval+1]
        double[] f = splineCoeffs[0];
        double[] x = splineCoeffs[1];
        double[] b = splineCoeffs[2];
        double[] c = splineCoeffs[3];
        double[] d = splineCoeffs[4];
        int n = f.length;
        //-- Search for correct interval for t.
        interval = _lastInterval; // heuristic
        if (t > x[n - 2]) {
            interval = n - 2;
        } else if (t >= x[_lastInterval]) {
            for (int j = _lastInterval; j < n && t >= x[j]; j++) {
                interval = j;
            }
        } else {
            for (int j = _lastInterval; t < x[j]; j--) {
                interval = j - 1;
            }
        }
        _lastInterval = interval; // class variable for next call
        //-- Evaluate cubic polynomial on [x[interval] , x[interval+1]].
        double dt = t - x[interval];
        double s = f[interval] + dt * (b[interval] + dt * (c[interval] + dt * d[interval]));
        return s;
    }

    /**
     * Gets the sum of a double array.
     *
     * @param array given double[] array.
     * @return The sum.
     */
    public double sum(double[] array) {
        double sum = 0.0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    /**
     * Gets the local time.
     *
     * @return String with local time.
     */
    public String time() {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    /**
     * Gets the STAR-CCM+ version.
     *
     * @return The code version in a x.yy.zzz string format.
     */
    public String version() {
        return _sim.getStarVersion().getString("ReleaseNumber");
    }

    /**
     * Gets the STAR-CCM+ version.
     *
     * @return The code version in a xyyzzz integer format.
     */
    public int version2() {
        double vd = Double.valueOf(version().replaceAll("\\....$", ""));
        int vi = (int) (vd * 100);
        _io.print.msg("STAR-CCM+ version in Integer format: " + vi, true);
        return vi;
    }

}
