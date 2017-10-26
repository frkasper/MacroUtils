package macroutils.templates;

import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.cadmodeler.Body;
import star.cadmodeler.CadModel;
import star.cadmodeler.CanonicalSketchPlane;
import star.cadmodeler.CircleSketchPrimitive;
import star.cadmodeler.ExtrusionMerge;
import star.cadmodeler.Face;
import star.cadmodeler.LengthDimension;
import star.cadmodeler.LineSketchPrimitive;
import star.cadmodeler.PointSketchPrimitive;
import star.cadmodeler.RadiusDimension;
import star.cadmodeler.ScalarQuantityDesignParameter;
import star.cadmodeler.Sketch;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.TransformSketchPlane;
import star.common.Simulation;
import star.common.Units;

/**
 * Low-level class for some templated geometries with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class TemplateGeometry {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplateGeometry(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private Body _createExtrusion(CadModel cm, Sketch sketch, double l, Units u) {
        ExtrusionMerge em = cm.getFeatureManager().createExtrusionMerge(sketch);
        ScalarQuantityDesignParameter emv = em.getDistance().createDesignParameter("Lz");
        em.setDirectionOption(0);
        em.setExtrudedBodyTypeOption(0);
        emv.getQuantity().setUnits(u);
        emv.getQuantity().setValue(l);
        em.setDistanceOption(0);
        em.setCoordinateSystemOption(0);
        em.setDraftOption(0);
        em.setCoordinateSystemOption(0);
        em.setFace(null);
        em.setBody(null);
        em.setSketch(sketch);
        em.setPostOption(1);
        em.setExtrusionOption(0);
        cm.getFeatureManager().execute(em);
        return cm.getBodyManager().getBodies().iterator().next();
    }

    private void _createDesignParameter(Sketch sketch, String dpName, String lineName, double x0, double x1, Units u) {
        LineSketchPrimitive lsp = (LineSketchPrimitive) sketch.getSketchPrimitive(lineName);
        LengthDimension ld = sketch.createLengthDimension(lsp, x1 - x0, u);
        ScalarQuantityDesignParameter sqd = ld.getLength().createDesignParameter(dpName);
    }

    /**
     * Creates a Block/Channel 3D-CAD model and creates a Part with 6 Part Surfaces inside, i.e., x0, x1, y0, y1, z0 and
     * z1. The default Tessellation option is used. See {@link UserDeclarations#defTessOpt}.
     *
     * @param c1 given 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param c2 given 3-components array with coordinates. E.g.: {1, 1, 1}.
     * @param u given Units.
     * @param name given Cad Body name.
     * @param vo given verbose option. False will not print anything.
     * @return The Cad Body.
     */
    public Body block(double[] c1, double[] c2, Units u, String name, boolean vo) {
        _io.say.action("Creating a Block 3D-CAD Model", vo);
        _io.say.value("Coordinate 1", _get.strings.fromArray(c1), u, vo);
        _io.say.value("Coordinate 2", _get.strings.fromArray(c2), u, vo);
        CadModel cm = _sim.get(SolidModelManager.class).createSolidModel();
        cm.setPresentationName(name + " 3D-CAD Model");
        CanonicalSketchPlane csp = ((CanonicalSketchPlane) cm.getFeatureManager().getObject("XY"));
        TransformSketchPlane newPlane = cm.getFeatureManager().createPlaneByTransformation(csp);
        newPlane.getTranslationVector().setComponents(0., 0., c1[2]);
        newPlane.getTranslationVector().setUnits(u);
        cm.getFeatureManager().execute(newPlane);
        Sketch sketch = cm.getFeatureManager().createSketch(newPlane);
        cm.getFeatureManager().startSketchEdit(sketch);
        double f = u.getConversion();
        sketch.createRectangle(_get.objects.doubleVector(c1[0] * f, c1[1] * f),
                _get.objects.doubleVector(c2[0] * f, c2[1] * f));
        _createDesignParameter(sketch, "Lx", "Line 2", c1[0] * f, c2[0] * f, u);
        _createDesignParameter(sketch, "Ly", "Line 1", c1[1] * f, c2[1] * f, u);
        cm.getFeatureManager().stopSketchEdit(sketch, true);
        cm.getFeatureManager().rollForwardToEnd();
        Body body = _createExtrusion(cm, sketch, (c2[2] - c1[2]), u);
        body.setPresentationName(name);
        ((Face) body.getFaceManager().getObject("Face 1")).setNameAttribute("x0");
        ((Face) body.getFaceManager().getObject("Face 3")).setNameAttribute("x1");
        ((Face) body.getFaceManager().getObject("Face 4")).setNameAttribute("y0");
        ((Face) body.getFaceManager().getObject("Face 2")).setNameAttribute("y1");
        ((Face) body.getFaceManager().getObject("Face 6")).setNameAttribute("z0");
        ((Face) body.getFaceManager().getObject("Face 5")).setNameAttribute("z1");
        _sim.get(SolidModelManager.class).endEditCadModel(cm);
        _io.say.msg("Creating a Part...", vo);
        cm.createParts(_get.objects.arrayList(body), "SharpEdges", 30.0, _ud.defTessOpt.getValue(), false, 1.0E-5);
        _io.say.ok(vo);
        return body;
    }

    /**
     * Creates a Cylinder using the 3D-CAD model and creates a Part using the default Tessellation option. See
     * {@link UserDeclarations#defTessOpt}.
     *
     * @param r given Radius.
     * @param l given Length.
     * @param org given origin as a 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param u given Units.
     * @param ax given extrusion direction. See {@link macroutils.StaticDeclarations.Axis} for options.
     * @param name given Cylinder name.
     * @param vo given verbose option. False will not print anything.
     * @return The Cad Body.
     */
    public Body cylinder(double r, double l, double[] org, Units u, StaticDeclarations.Axis ax,
            String name, boolean vo) {
        _io.say.action("Creating a Cylinder 3D-CAD Model", vo);
        _io.say.value("Radius", r, u, vo);
        _io.say.value(ax.toString() + " Length", l, u, vo);
        double rC = r * u.getConversion();
        double[] offsetPlane = {0., 0., 0.};
        double[] sketchCircle = {0., 0.};
        CadModel cm = _sim.get(SolidModelManager.class).createSolidModel();
        cm.setPresentationName(name + " 3D-CAD Model");
        String pln = CAD_DIRECTIONS.replace(ax.toString(), "");
        switch (ax) {
            case X:
                offsetPlane[2] = org[0];
                sketchCircle[0] = org[1] * u.getConversion();
                sketchCircle[1] = org[2] * u.getConversion();
                break;
            case Y:
                offsetPlane[2] = org[1];
                sketchCircle[0] = org[2] * u.getConversion();
                sketchCircle[1] = org[0] * u.getConversion();
                pln = "ZX";
                break;
            case Z:
                offsetPlane[2] = org[2];
                sketchCircle[0] = org[0] * u.getConversion();
                sketchCircle[1] = org[1] * u.getConversion();
                break;
        }
        CanonicalSketchPlane csp = ((CanonicalSketchPlane) cm.getFeatureManager().getObject(pln));
        TransformSketchPlane newPlane = cm.getFeatureManager().createPlaneByTransformation(csp);
        newPlane.getTranslationVector().setComponents(offsetPlane[0], offsetPlane[1], offsetPlane[2]);
        newPlane.getTranslationVector().setUnits(u);
        cm.getFeatureManager().execute(newPlane);
        Sketch sketch = cm.getFeatureManager().createSketch(newPlane);
        cm.getFeatureManager().startSketchEdit(sketch);
        CircleSketchPrimitive circle = sketch.createCircle(new DoubleVector(sketchCircle), rC);
        PointSketchPrimitive pt = ((PointSketchPrimitive) sketch.getSketchPrimitiveManager().getObject("Point 1"));
        sketch.createFixationConstraint(pt);
        RadiusDimension rd = sketch.createRadiusDimension(circle, rC, u);
        rd.getRadius().createDesignParameter("Radius");
        cm.getFeatureManager().stopSketchEdit(sketch, true);
        cm.getFeatureManager().rollForwardToEnd();
        Body body = _createExtrusion(cm, sketch, l, u);
        body.setPresentationName(name);
        Face f0 = ((Face) body.getFaceManager().getObject("Face 3"));
        f0.setNameAttribute(ax.toString().toLowerCase() + "0");
        Face f1 = ((Face) body.getFaceManager().getObject("Face 2"));
        f1.setNameAttribute(ax.toString().toLowerCase() + "1");
        _sim.get(SolidModelManager.class).endEditCadModel(cm);
        _io.say.msg(vo, "Creating Part: %s...", cm.getPresentationName());
        cm.createParts(_get.objects.arrayList(body), "SharpEdges", 30.0, _ud.defTessOpt.getValue(), false, 1.0E-5);
        _io.say.ok(vo);
        return body;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private static final String CAD_DIRECTIONS = "XYZ";

    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;
    private UserDeclarations _ud = null;

}
