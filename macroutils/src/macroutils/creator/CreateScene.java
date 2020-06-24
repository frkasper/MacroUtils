package macroutils.creator;

import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.common.FieldFunction;
import star.common.Region;
import star.common.Simulation;
import star.common.Units;
import star.vis.Annotation;
import star.vis.AnnotationProp;
import star.vis.AnnotationPropManager;
import star.vis.DisplayLocationMode;
import star.vis.Displayer;
import star.vis.DisplayerManager;
import star.vis.FixedAspectAnnotationProp;
import star.vis.PartColorMode;
import star.vis.PartDisplayer;
import star.vis.ReportAnnotation;
import star.vis.ScalarDisplayQuantity;
import star.vis.ScalarDisplayer;
import star.vis.ScalarFillMode;
import star.vis.Scene;
import star.vis.SimpleAnnotation;
import star.vis.StreamDisplayer;
import star.vis.StreamDisplayerMode;
import star.vis.StreamPart;
import star.vis.VectorDisplayMode;
import star.vis.VectorDisplayer;

/**
 * Low-level class for creating Scenes with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateScene {

    private MainCreator _add = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private macroutils.setter.MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateScene(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Report Annotation in a Scene.
     *
     * @param scn    given Scene.
     * @param ra     given ReportAnnotation.
     * @param height given font height. If 0, it will be ignored.
     * @param fmt    given string format. E.g.: new double[] {0.5, 0.2, 0}.
     * @param pos    given 3-components of position. E.g.: new double[] {0.5, 0.2, 0}.
     * @return The FixedAspectAnnotationProp within the Scene.
     */
    public FixedAspectAnnotationProp annotation(Scene scn, ReportAnnotation ra, double height,
            String fmt, double[] pos) {
        _creatingAnnot(scn, "Report Annotation", fmt);
        ra.setNumberFormat(fmt);
        if (scn == null) {
            return null;
        }
        AnnotationPropManager apm = scn.getAnnotationPropManager();
        if (apm.hasPropForAnnotation(ra)) {
            _io.say.msg(true, "Annotation already exists. Skipping creation...");
            return (FixedAspectAnnotationProp) apm.getAnnotationProp(ra.getPresentationName());
        }
        return _createAnnot(scn, ra, height, pos);
    }

    /**
     * Creates a Text Annotation in a Scene.
     *
     * @param scn    given Scene.
     * @param text   given text.
     * @param height given font height. If 0, it will be ignored.
     * @param pos    given 3-components of position. E.g.: new double[] {0.5, 0.2, 0}.
     * @return The FixedAspectAnnotationProp within the Scene.
     */
    public FixedAspectAnnotationProp annotation(Scene scn, String text, double height,
            double[] pos) {
        _io.say.action("Creating a Simple Annotation text in a Scene", true);
        _io.say.object(scn, true);
        _io.say.value("String", text, true, true);
        String ns = text.replace(" ", "");
        if (_sim.getAnnotationManager().has(ns)) {
            _io.say.msg(true, "Annotation already exists...");
            return (FixedAspectAnnotationProp) scn.getAnnotationPropManager().getAnnotationProp(ns);
        }
        SimpleAnnotation annot = _sim.getAnnotationManager().createSimpleAnnotation();
        annot.setText(text);
        annot.setPresentationName(ns);
        return _createAnnot(scn, annot, height, pos);
    }

    /**
     * Creates a Time annotation and adds it into a Scene.
     *
     * @param scn given Scene.
     * @param u   given time Units.
     * @param fmt given time format string.
     * @param pos 2-components position array. E.g.: new double[] {0.4, 0.8}
     */
    public void annotation_Time(Scene scn, Units u, String fmt, double[] pos) {
        annotation(scn, _add.tools.annotation_Time(fmt), 0, fmt, pos);
    }

    /**
     * Adds a new Displayer into a Scene.
     *
     * @param scn given Scene.
     * @param dt  given Displayer Type. See {@link macroutils.StaticDeclarations.Displayer} for
     *            options.
     * @param ano given ArrayList of NamedObjects.
     * @param ff  given Field Function, if applicable.
     * @param u   given variable Unit, if applicable.
     * @return The Displayer.
     */
    public Displayer displayer(Scene scn, StaticDeclarations.Displayer dt,
            ArrayList<NamedObject> ano, FieldFunction ff, Units u) {
        return _createDisplayer(scn, dt, ano, ff, u, true);
    }

    /**
     * Creates a Geometry Displayer containing all Parts in a given Scene.
     *
     * @param scn given Scene.
     * @return The PartDisplayer.
     */
    public PartDisplayer displayer_Geometry(Scene scn) {
        return _createDisplayer_Part(scn, _getGeometryObjects(), true);
    }

    /**
     * Creates a Geometry Displayer containing the given Parts for a given Scene.
     *
     * @param scn given Scene.
     * @param ano given ArrayList of NamedObjects.
     * @return The PartDisplayer.
     */
    public PartDisplayer displayer_Geometry(Scene scn, ArrayList<NamedObject> ano) {
        return _createDisplayer_Part(scn, ano, true);
    }

    /**
     * Creates a Streamline Displayer for a given Scene and with the given Objects, such as:
     * <ul>
     * <li> Objects that are 3D in Space will be assigned as Input Parts. E.g.: Regions or Parts;
     * <li> Objects that are 2D in Space will be assigned as Seed Parts. E.g.: Boundaries or Part
     * Surfaces;
     * <li> The Streamline will be based on the Velocity field;
     * </ul>
     *
     * @param scn     given Scene.
     * @param ano     given ArrayList of NamedObjects.
     * @param tubeOpt use tubes to represent the Streamlines.
     * @return The StreamDisplayer.
     */
    public StreamDisplayer displayer_Streamline(Scene scn, ArrayList<NamedObject> ano,
            boolean tubeOpt) {
        StreamPart sp = _add.derivedPart.streamline_PartSeed(ano);
        StreamDisplayer sd = _createDisplayer_Streamline(scn, _get.objects.arrayList(sp),
                _getVelocity(), _ud.defUnitVel, true);
        sd.setFieldFunction(sp.getFieldFunction().getMagnitudeFunction());
        if (tubeOpt) {
            sd.setMode(StreamDisplayerMode.TUBES);
            sd.setWidth(_ud.postStreamlinesTubesWidth);
        }
        return sd;
    }

    /**
     * Creates an empty Scene with no Displayers.
     *
     * @return The Scene.
     */
    public Scene empty() {
        return _createScene(StaticDeclarations.Scene.EMPTY, new ArrayList<>(), null, null, true);
    }

    /**
     * Creates a Geometry Scene containing all Parts.
     *
     * @return The Scene.
     */
    public Scene geometry() {
        return _createScene(StaticDeclarations.Scene.GEOMETRY, new ArrayList<>(), null, null, true);
    }

    /**
     * Creates a Geometry Scene containing the given input Objects.
     *
     * @param ano given ArrayList of NamedObjects.
     * @return The Scene.
     */
    public Scene geometry(ArrayList<NamedObject> ano) {
        return _createScene(StaticDeclarations.Scene.GEOMETRY, ano, null, null, true);
    }

    /**
     * Creates a Mesh Scene containing all Parts.
     *
     * @return The Scene.
     */
    public Scene mesh() {
        return _createScene(StaticDeclarations.Scene.MESH, new ArrayList<>(), null, null, true);
    }

    /**
     * Creates a Mesh Scene containing the given input Objects.
     *
     * @param ano given ArrayList of NamedObjects.
     * @return The Scene.
     */
    public Scene mesh(ArrayList<NamedObject> ano) {
        return _createScene(StaticDeclarations.Scene.MESH, ano, null, null, true);
    }

    /**
     * Creates a Scalar Scene containing the given input Objects.
     *
     * @param ano given ArrayList of NamedObjects.
     * @param ff  given Field Function.
     * @param u   given variable Unit.
     * @param sf  Smooth Fill Displayer?
     * @return The Scene.
     */
    public Scene scalar(ArrayList<NamedObject> ano, FieldFunction ff, Units u, boolean sf) {
        Scene scn = _createScene(StaticDeclarations.Scene.SCALAR, _getScalarObjects(ano), ff, u, true);
        if (sf) {
            ScalarDisplayer sd = (ScalarDisplayer) _get.scenes.displayerByREGEX(scn,
                    "Scalar.*", false);
            sd.setFillMode(ScalarFillMode.NODE_FILLED);
        }
        return scn;
    }

    /**
     * Creates a Streamline Scene of Seed Part type with the given Objects, such as:
     * <ul>
     * <li> Objects that are 3D in Space will be assigned as Input Parts. E.g.: Regions or Parts;
     * <li> Objects that are 2D in Space will be assigned as Seed Parts. E.g.: Boundaries or Part
     * Surfaces;
     * <li> The Streamline will be based on the Velocity field;
     * <li> For the 3D objects, a Part Displayer will be automatically created using a Geometry
     * Representation and with an Opacity of 0.2 and the default Color. See
     * {@link UserDeclarations#defColor}.
     * </ul>
     *
     * @param ano     given ArrayList of NamedObjects. E.g.: a Region and an Inlet Boundary.
     * @param tubeOpt use tubes to represent the Streamlines.
     * @return The Scene.
     */
    public Scene streamline(ArrayList<NamedObject> ano, boolean tubeOpt) {
        ArrayList<NamedObject> asp = new ArrayList<>();
        StreamPart sp = _add.derivedPart.streamline_PartSeed(ano);
        asp.add(sp);
        Scene scn = _createScene(StaticDeclarations.Scene.STREAMLINE, asp, _getVelocity(),
                _ud.defUnitVel, tubeOpt);
        StreamDisplayer sd = (StreamDisplayer) _mu.get.scenes.displayerByREGEX(scn, ".*", false);
        PartDisplayer pd = _createDisplayer_Part(scn, _getInputPartsChildren(sp), true);
        pd.setRepresentation(_get.mesh.geometry());
        pd.setOpacity(0.2);
        pd.setColorMode(PartColorMode.CONSTANT);
        pd.setDisplayerColorColor(_ud.defColor);
        if (tubeOpt) {
            sd.setMode(StreamDisplayerMode.TUBES);
            sd.setWidth(_ud.postStreamlinesTubesWidth);
        }
        return scn;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    /**
     * Creates a Vector Scene containing the given input Objects.
     *
     * @param ano    given ArrayList of NamedObjects.
     * @param licOpt Linear Integral Convolution option?
     * @return The Scene.
     */
    public Scene vector(ArrayList<NamedObject> ano, boolean licOpt) {
        Scene scn = _createScene(StaticDeclarations.Scene.VECTOR, ano,
                _get.objects.fieldFunction(StaticDeclarations.Vars.VEL), _ud.defUnitVel, true);
        if (licOpt) {
            VectorDisplayer vd = (VectorDisplayer) _get.scenes.displayerByREGEX(scn,
                    "Vector", false);
            vd.setDisplayMode(VectorDisplayMode.VECTOR_DISPLAY_MODE_LIC);
        }
        return scn;
    }

    private FixedAspectAnnotationProp _createAnnot(Scene scn, Annotation an, double h,
            double[] pos) {
        an.setFont(StaticDeclarations.Fonts.DEFAULT.getFont());
        if (h != 0.) {
            an.setDefaultHeight(h);
            _io.say.value("Height", an.getDefaultHeight(), true);
        }
        an.setDefaultPosition(new DoubleVector(pos));
        _io.say.value("Position", an.getDefaultPosition(), true);
        _io.say.object(an, true);
        _io.say.ok(true);
        return (FixedAspectAnnotationProp) scn.getAnnotationPropManager()
                .createPropForAnnotation(an);
    }

    private Displayer _createDisplayer(Scene scn, StaticDeclarations.Displayer type,
            ArrayList<NamedObject> ano, FieldFunction ff, Units u, boolean vo) {
        _creatingDisplayer(null, ano, ff, u, vo);
        switch (type) {
            case GEOMETRY:
                return _createDisplayer_Part(scn, ano, vo);
            case SCALAR:
                return _createDisplayer_Scalar(scn, ano, ff, u, vo);
            case STREAMLINE:
                return _createDisplayer_Streamline(scn, ano, ff, u, vo);
            case VECTOR:
                return _createDisplayer_Vector(scn, ano, ff, u, vo);
            default:
                return null;
        }
    }

    private PartDisplayer _createDisplayer_Part(Scene scn, ArrayList<NamedObject> ano, boolean vo) {
        PartDisplayer pd = _getDM(scn)
                .createPartDisplayer(StaticDeclarations.Displayer.GEOMETRY.getType());
        pd.initialize();
        pd.setColorMode(PartColorMode.DP);
        pd.setOutline(false);
        pd.setSurface(true);
        pd.addParts(ano);
        _createdDisplayer(pd, ano, vo);
        return pd;
    }

    private ScalarDisplayer _createDisplayer_Scalar(Scene scn, ArrayList<NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        ScalarDisplayer sd = _getDM(scn)
                .createScalarDisplayer(StaticDeclarations.Displayer.SCALAR.getType());
        sd.initialize();
        _setSDQ(sd.getScalarDisplayQuantity(), ff);
        if (u != null) {
            sd.getScalarDisplayQuantity().setUnits(u);
        }
        sd.addParts(ano);
        _createdDisplayer(sd, ano, vo);
        return sd;
    }

    private StreamDisplayer _createDisplayer_Streamline(Scene scn, ArrayList<NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        StreamDisplayer sd = _getDM(scn)
                .createStreamDisplayer(StaticDeclarations.Displayer.STREAMLINE.getType());
        sd.initialize();
        _setSDQ(sd.getScalarDisplayQuantity(), ff);
        if (u != null) {
            sd.getScalarDisplayQuantity().setUnits(u);
        }
        //-- Here, ano is a collection of StreamPart's.
        sd.addParts(ano);
        _createdDisplayer(sd, ano, vo);
        return sd;
    }

    private VectorDisplayer _createDisplayer_Vector(Scene scn, ArrayList<NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        VectorDisplayer vd = _getDM(scn).createVectorDisplayer("Vector");
        vd.initialize();
        if (!_chk.is.vector(ff)) {
            _io.say.value("Field Function is not a Vector. Type",
                    ff.getType().getSelected().name(), true, true);
        }
        vd.getVectorDisplayQuantity().setFieldFunction(ff);
        if (u != null) {
            vd.getVectorDisplayQuantity().setUnits(u);
        }
        vd.addParts(ano);
        _createdDisplayer(vd, ano, vo);
        return vd;
    }

    private Scene _createScene(StaticDeclarations.Scene type, ArrayList<NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        Scene scn = _initScene(type, vo);
        if (ano.isEmpty()) {
            ano.addAll(_getGeometryObjects());
        }
        switch (type) {
            case EMPTY:
                break;
            case GEOMETRY:
                PartDisplayer geometry = _add.scene._createDisplayer_Part(scn, ano, vo);
                geometry.setRepresentation(_get.mesh.latestSurfaceRepresentation());
                break;
            case MESH:
                PartDisplayer mesh = _createDisplayer_Part(scn, ano, vo);
                mesh.setMesh(true);
                break;
            case SCALAR:
                _createDisplayer_Scalar(scn, ano, ff, u, vo);
                break;
            case STREAMLINE:
                _createDisplayer_Streamline(scn, ano, ff, u, vo);
                break;
            case VECTOR:
                _createDisplayer_Vector(scn, ano, ff, u, vo);
                break;
        }
        _finalizeScene(scn, vo);
        return scn;
    }

    private void _createdDisplayer(Displayer d, ArrayList<NamedObject> ano, boolean vo) {
        d.setPresentationName(d.getPresentationName().split(" ")[0]);
        _io.say.objects(ano, "Objects in Displayer", vo);
        _io.say.created(d, vo);
    }

    private void _creatingAnnot(Scene scn, String what, String fmt) {
        _io.say.action(String.format("Creating a %s in a Scene", what), true);
        _io.say.object(scn, true);
        _io.say.value("Format", fmt, false, true);
    }

    private void _creatingDisplayer(Displayer d, ArrayList<NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action(String.format("Creating a %s Displayer", d.getPresentationName()), vo);
        _io.say.objects(ano, "Parts", vo);
        if (ff != null) {
            _io.say.object(ff, vo);
            _io.say.object(u, vo);
        }
    }

    private void _finalizeScene(Scene scn, boolean vo) {
        _set.scene.cameraView(scn, _ud.defCamView, vo);
        //--
        //-- Automatic camera assignment based on Scene name.
        _set.scene.cameraView(scn, _get.cameras.byREGEX(scn.getPresentationName(), false), false);
        _io.say.created(scn, vo);
    }

    private DisplayerManager _getDM(Scene scn) {
        return scn.getDisplayerManager();
    }

    private ArrayList<NamedObject> _getGeometryObjects() {
        boolean isEmpty = _sim.getRegionManager().isEmpty();
        return new ArrayList<>(isEmpty ? _get.partSurfaces.all(false) : _get.boundaries.all(false));
    }

    private ArrayList<NamedObject> _getInputPartsChildren(StreamPart sp) {
        return _get.objects.children(new ArrayList<>(sp.getInputPartsCollection()), false);
    }

    private ArrayList<NamedObject> _getScalarObjects(ArrayList<NamedObject> ano) {
        ArrayList<NamedObject> ano2 = new ArrayList<>();
        for (NamedObject no : ano) {
            if (no instanceof Region) {
                Region r = (Region) no;
                // Insert Regions when in 2D.
                if (r.getPhysicsContinuum().getModelManager().has("Two Dimensional")) {
                    return ano;
                }
                // Insert Boundaries otherwise.
                ano2.addAll(r.getBoundaryManager().getBoundaries());
                continue;
            }
            ano2.add(no);
        }
        return ano2;
    }

    private FieldFunction _getVelocity() {
        return _get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), false);
    }

    private Scene _initScene(StaticDeclarations.Scene type, boolean vo) {
        _io.say.action("Creating a Scene", vo);
        _io.say.value("Type", type.getType(), true, vo);
        Scene scn = _sim.getSceneManager().createScene();
        scn.setPresentationName(type.getType());
        ((PartDisplayer) scn.getCreatorDisplayer()).initialize();
        scn.initializeAndWait();
        scn.resetCamera();
        _get.objects.hardcopyProperties(scn, false).setUseCurrentResolution(false);
        scn.setDepthPeel(false);
        AnnotationProp ap = scn.getAnnotationPropManager().getAnnotationProp("Logo");
        ((FixedAspectAnnotationProp) ap).setLocation(DisplayLocationMode.FOREGROUND);
        return scn;
    }

    private void _setSDQ(ScalarDisplayQuantity sdq, FieldFunction ff) {
        if (_chk.is.vector(ff) || _chk.is.position(ff)) {
            sdq.setFieldFunction(ff.getMagnitudeFunction());
            return;
        }
        sdq.setFieldFunction(ff);
    }

}
