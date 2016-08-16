package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.vis.*;

/**
 * Low-level class for getting Camera Views (VisView) with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetCameras {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetCameras(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private double _getIncrement(double v1, double v2, int step, int totalSteps) {
        double delta = 1.0 * (v2 - v1) / totalSteps;
        return v1 + step * delta;
    }

    private DoubleVector _getIncrement(DoubleVector dv1, DoubleVector dv2, int step, int totalSteps) {
        DoubleVector dv = (DoubleVector) dv1.clone();
        for (int i = 0; i < dv.size(); i++) {
            double d1 = dv1.get(i);
            double delta = (dv2.get(i) - dv1.get(i)) / totalSteps;
            dv.setElementAt(d1 + step * delta, i);
        }
        return dv;
    }

    /**
     * Gets all Camera Views available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Camera Views.
     */
    public ArrayList<VisView> all(boolean vo) {
        ArrayList<VisView> avv = new ArrayList(_sim.getViewManager().getObjects());
        _tmpl.print.getAll("Camera Views", new ArrayList(avv), vo);
        return avv;
    }

    /**
     * Gets all Camera Views that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Camera Views.
     */
    public ArrayList<VisView> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList(_get.objects.allByREGEX(regexPatt, "Camera Views", new ArrayList(all(false)), vo));
    }

    /**
     * Gets a Camera View by its Presentation Name.
     *
     * @param name given camera name.
     * @return The VisView. Null if nothing is found.
     */
    public VisView byName(String name) {
        return byName(name, true);
    }

    /**
     * Gets a Camera View by its Presentation Name.
     *
     * @param name given camera name.
     * @param vo given verbose option. False will not print anything.
     * @return The Unit. Null if nothing is found.
     */
    public VisView byName(String name, boolean vo) {
        return (VisView) _get.objects.byName(name, "Camera View", new ArrayList(all(false)), vo);
    }

    /**
     * Performs a linear interpolation between 2 cameras and generate the intermediate views.
     *
     * @param v1 given Camera 1.
     * @param v2 given Camera 2.
     * @param nSteps given number of wanted Cameras in between.
     * @param vo given verbose option. False will not print anything.
     * @return The ordered Vector of the transition Cameras, from Cam1 to Cam2. Vector size is nSteps + 2.
     */
    public ArrayList<VisView> inBetween_Linear(VisView v1, VisView v2, int nSteps, boolean vo) {
        _io.say.action("Linear Interpolation between 2 Camera Views", vo);
        _io.say.msg(vo, "Camera 1: \"%s\".", v1.getPresentationName());
        _io.say.msg(vo, "Camera 2: \"%s\".", v2.getPresentationName());
        _io.say.msg(vo, "Number of Steps: %d.", nSteps);
        ArrayList<VisView> av = new ArrayList();
        nSteps = Math.max(nSteps, 2);
        for (int i = 1; i <= nSteps; i++) {
            VisView v = _sim.getViewManager().createView();
            v.copyProperties(v1);
            v.setPresentationName(String.format("%s_%s_%s_%d_%04d", StaticDeclarations.TMP_CAM_NAME,
                    v1.getPresentationName(), v2.getPresentationName(), nSteps, i));
            _io.say.msg(vo, "Generating: \"%s\"...", v.getPresentationName());
            DoubleVector dv1 = _getIncrement(v1.getFocalPoint(), v2.getFocalPoint(), i, nSteps);
            v.setFocalPoint(dv1);
            DoubleVector dv2 = _getIncrement(v1.getPosition(), v2.getPosition(), i, nSteps);
            v.setPosition(dv2);
            DoubleVector dv3 = _getIncrement(v1.getViewUp(), v2.getViewUp(), i, nSteps);
            v.setViewUp(dv3);
            double ps = _getIncrement(v1.getParallelScale(), v2.getParallelScale(), i, nSteps);
            v.setParallelScale(ps);
            av.add(v);
        }
        av.add(0, v1);
        av.add(v2);
        _io.say.msg(vo, "Returning %d Camera Views.", av.size());
        _io.say.ok(vo);
        return av;
    }

    /**
     * Performs a spline interpolation between the given cameras and generate the intermediate views.
     *
     * @param avv given ArrayList of Cameras.
     * @param nSteps given number of wanted Cameras in between.
     * @param vo given verbose option. False will not print anything.
     * @return The ordered Vector of the transition Cameras. Vector size is nSteps + 1.
     */
    public ArrayList<VisView> inBetween_Spline(ArrayList<VisView> avv, int nSteps, boolean vo) {
        _io.say.action("Interpolating Camera Views", vo);
        _io.say.objects(avv, "Cameras", vo);
        if (avv.size() == 2) {
            _io.say.msg(vo, "Calling Linear Interpolator...");
            return inBetween_Linear(avv.get(0), avv.get(1), nSteps, vo);
        }
        _io.say.msg(vo, "Spline Interpolator...");
        //--
        ArrayList<VisView> av = new ArrayList();
        ArrayList<Double> _x = new ArrayList(), _f = new ArrayList();
        DoubleVector dvFP = avv.get(0).getFocalPoint();
        DoubleVector dvPos = avv.get(0).getPosition();
        DoubleVector dvVU = avv.get(0).getViewUp();
        //--
        int n_delta = nSteps / (avv.size() - 1);
        //--
        for (int k = 0; k <= nSteps; k++) {
            //-- Create Temporary Cameras
            VisView v = _sim.getViewManager().createView();
            v.copyProperties(avv.get(0));
            String cn = String.format("%s_Spline_%dcams_%04d", StaticDeclarations.TMP_CAM_NAME, avv.size(), k);
            v.setPresentationName(cn);
            _io.say.msg(vo, "Generating: \"%s\"...", v.getPresentationName());
            _io.say.msg(vo, "Processing VisView data ID...");
            for (int j = 0; j < 3; j++) {
                //--
                _x.clear();
                _f.clear();
                for (int i = 0; i < avv.size(); i++) {
                    _x.add((double) i * n_delta);
                    _f.add(avv.get(i).getFocalPoint().get(j));
                }
                double[][] splFPs = _get.info.spline(_x, _f);
                dvFP.setElementAt(_get.info.splineValue(splFPs, k), j);
                //--
                _x.clear();
                _f.clear();
                for (int i = 0; i < avv.size(); i++) {
                    _x.add((double) i * n_delta);
                    _f.add(avv.get(i).getPosition().get(j));
                }
                double[][] splPos = _get.info.spline(_x, _f);
                dvPos.setElementAt(_get.info.splineValue(splPos, k), j);
                //--
                _x.clear();
                _f.clear();
                for (int i = 0; i < avv.size(); i++) {
                    _x.add((double) i * n_delta);
                    _f.add(avv.get(i).getViewUp().get(j));
                }
                double[][] splVUs = _get.info.spline(_x, _f);
                dvVU.setElementAt(_get.info.splineValue(splVUs, k), j);
            }
            //--
            _x.clear();
            _f.clear();
            for (int i = 0; i < avv.size(); i++) {
                _x.add((double) i * n_delta);
                _f.add(avv.get(i).getParallelScale());
            }
            double[][] splPS = _get.info.spline(_x, _f);
            double newPS = _get.info.splineValue(splPS, k);
            v.setFocalPoint(dvFP);
            v.setPosition(dvPos);
            v.setViewUp(dvVU);
            v.setParallelScale(newPS);
            av.add(v);
        }
        _io.say.msg(vo, "Cameras processed: %d.", av.size());
        _io.say.ok(vo);
        return av;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
        _tmpl = _mu.templates;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.templates.MainTemplates _tmpl = null;
    private Simulation _sim = null;

}
