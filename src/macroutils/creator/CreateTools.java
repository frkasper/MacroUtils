package macroutils.creator;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import macroutils.getter.GetMonitors;
import macroutils.setter.SetObjects;
import star.base.neo.DoubleVector;
import star.base.report.PlotableMonitor;
import star.base.report.Report;
import star.common.Coordinate;
import star.common.CylindricalCoordinateSystem;
import star.common.DeltaMonitorUpdateEvent;
import star.common.Dimensions;
import star.common.FieldFunction;
import star.common.FieldFunctionTypeOption;
import star.common.FileTable;
import star.common.FrequencyMonitorUpdateEvent;
import star.common.GlobalParameterBase;
import star.common.GlobalParameterManager;
import star.common.LocalCoordinateSystemManager;
import star.common.LogicUpdateEvent;
import star.common.RangeMonitorUpdateEvent;
import star.common.ScalarGlobalParameter;
import star.common.Simulation;
import star.common.Units;
import star.common.UpdateEvent;
import star.common.UpdateEventDeltaOption;
import star.common.UpdateEventLogicOption;
import star.common.UpdateEventRangeOption;
import star.common.UserFieldFunction;
import star.common.VectorGlobalParameter;
import star.motion.MotionManager;
import star.motion.TranslatingMotion;
import star.vis.AnnotationManager;
import star.vis.ColorMap;
import star.vis.LookupTable;
import star.vis.LookupTableManager;
import star.vis.ReportAnnotation;
import star.vis.SimpleAnnotation;
import star.vis.SimpleTransform;
import star.vis.UserLookupTable;

/**
 * Low-level class for creating Tools objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateTools {

    private MainCreator _add = null;
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
    public CreateTools(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Simple Text Annotation.
     *
     * @param name  given name.
     * @param value given value for the Annotation.
     * @return The SimpleAnnotation.
     */
    public SimpleAnnotation annotation_Simple(String name, String value) {
        _io.say.action("Creating a Simple Annotation", true);
        AnnotationManager am = _sim.getAnnotationManager();
        if (am.has(name)) {
            if (am.getObject(name) instanceof SimpleAnnotation) {
                _io.say.value("Annotation already exists", name, true, true);
                return (SimpleAnnotation) am.getObject(name);
            }
        }
        SimpleAnnotation sa = am.createSimpleAnnotation();
        sa.setText(value);
        sa.setPresentationName(name);
        _io.say.created(sa, true);
        return sa;
    }

    /**
     * Creates a Time Report Annotation with a custom decimal formatter. See
     * {@link java.lang.String#format}.
     *
     * @param fmt given time format.
     * @return The ReportAnnotation.
     */
    public ReportAnnotation annotation_Time(String fmt) {
        Report r = _add.report.expression("Time", _ud.unit_s, _ud.dimTime, "$Time", false);
        AnnotationManager am = _sim.getAnnotationManager();
        _io.say.action("Creating a Time Report Annotation", true);
        if (am.has(r.getPresentationName())) {
            if (am.getObject(r.getPresentationName()) instanceof ReportAnnotation) {
                _io.say.value("Report Annotation already exists", r.getPresentationName(), true,
                        true);
                return (ReportAnnotation) am.getObject(r.getPresentationName());
            }
        }
        ReportAnnotation ra = am.createReportAnnotation(r);
        _io.say.created(ra, true);
        return ra;
    }

    /**
     * Creates a custom Colormap.
     *
     * @param name given Colormap/LookupTable name.
     * @param ac   given ArrayList of {@link java.awt.Color}.
     * @param ad   given ArrayList of opacities. If null is given, all colors will be opaque.
     * @param cs   given color space code, as in {@link macroutils.StaticDeclarations.ColorSpace}.
     * @return The LookupTable, i.e., the colormap object as in STAR-CCM+.
     */
    public LookupTable colormap(String name, ArrayList<Color> ac, ArrayList<Double> ad,
            StaticDeclarations.ColorSpace cs) {
        _io.say.action("Creating a Colormap", true);
        if (_sim.get(LookupTableManager.class).has(name)) {
            _io.say.value("Colormap already exists", name, true, true);
            return _sim.get(LookupTableManager.class).getObject(name);
        }
        if (ac.size() < 2) {
            _io.say.msg("Please provide at least two colors.");
            _io.say.msg("Returning NULL.");
            return null;
        }
        if (ad == null) {
            ad = _getArrayList(ac.size(), 1.0);
        }
        _io.say.value("Colors", ac.toString(), true, true);
        _io.say.value("Opacities", _get.strings.withinTheBrackets(ad.toString()), true, true);
        _io.say.value("Color Space", cs.toString(), true, true);
        UserLookupTable ult = _sim.get(LookupTableManager.class).createLookupTable();
        DoubleVector dvC = new DoubleVector();
        DoubleVector dvO = new DoubleVector();
        for (int i = 0; i < ac.size(); i++) {
            double pos = (double) i / (ac.size() - 1);
            dvC.add(pos);
            dvC.addAll(new DoubleVector(ac.get(i).getColorComponents(null)));
            dvO.add(pos);
            dvO.add(ad.get(i));
        }
        ColorMap cm = new ColorMap(dvC, dvO, cs.getValue());
        ult.setColorMap(cm);
        ult.setPresentationName(name);
        _io.say.created(ult, true);
        return ult;
    }

    /**
     * Creates a Cylindrical Coordinate System aligned at the global origin, i.e., at [0, 0, 0] and
     * based on a direction axis. So depending on the given axis, <b>r</b>, <b>theta</b> and
     * <b>axial</b> directions will be:
     * <ul>
     * <li> X: Y, Z and X, respectively;
     * <li> Y: X, Z and Y, respectively;
     * <li> Z: X, Y and Z, respectively;
     * </ul>
     *
     * @param ax given extrusion direction. See {@link macroutils.StaticDeclarations.Axis} for
     *           options.
     * @return The CylindricalCoordinateSystem.
     */
    public CylindricalCoordinateSystem coordinateSystem_Cylindrical(StaticDeclarations.Axis ax) {
        double[] b1 = {}, b2 = {};
        switch (ax) {
            case X:
                b1 = new double[]{ 0, 1, 0 };
                b2 = new double[]{ 0, 0, 1 };
                break;
            case Y:
                b1 = new double[]{ 1, 0, 0 };
                b2 = new double[]{ 0, 0, 1 };
                break;
            case Z:
                b1 = new double[]{ 1, 0, 0 };
                b2 = new double[]{ 0, 1, 0 };
                break;
        }
        return coordinateSystem_Cylindrical(StaticDeclarations.COORD0, b1, b2);
    }

    /**
     * Creates a Cylindrical Coordinate System using default length units. See
     * {@link UserDeclarations#defUnitLength}.
     *
     * @param org given 3-components double[] array for the Origin. E.g.: {0, 0, 0}.
     * @param b1  given 3-components double[] array for the radial direction. E.g.: {0, 1, 0}.
     * @param b2  given 3-components double[] array for the tangential direction. E.g.: {0, 0, 1}.
     * @return The CylindricalCoordinateSystem.
     */
    public CylindricalCoordinateSystem coordinateSystem_Cylindrical(double[] org, double[] b1,
            double[] b2) {
        _io.say.action("Creating a Cylindrical Coordinate System", true);
        LocalCoordinateSystemManager lcsm = _ud.lab0.getLocalCoordinateSystemManager();
        CylindricalCoordinateSystem ccsys = lcsm
                .createLocalCoordinateSystem(CylindricalCoordinateSystem.class);
        Coordinate c = ccsys.getOrigin();
        c.setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength,
                new DoubleVector(org));
        ccsys.setBasis0(new DoubleVector(b1));
        ccsys.setBasis1(new DoubleVector(b2));
        _io.say.created(ccsys, true);
        return ccsys;
    }

    /**
     * Creates a User Defined Field Function.
     *
     * @param name given Field Function name.
     * @param def  given Field Function definition.
     * @param dim  given Dimensions.
     * @param type given type.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(String name, String def, Dimensions dim,
            FieldFunctionTypeOption.Type type) {
        _io.say.action("Creating a Field Function", true);
        FieldFunction ff = _get.objects.fieldFunction(name, false);
        if (ff != null) {
            _io.say.value("Field Function already exists", name, true, true);
            return ff;
        }
        UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
        uff.setPresentationName(name);
        uff.setFunctionName(name.replaceAll("( |\\(|\\)|)", ""));
        uff.setDefinition(def);
        uff.getTypeOption().setSelected(type);
        _io.say.value("Type", uff.getTypeOption().getSelectedElement().getPresentationName(), true,
                true);
        _io.say.value("Definition", uff.getDefinition(), true, true);
        if (dim != null || dim != _ud.dimDimensionless) {
            uff.setDimensions(dim);
        }
        _io.say.created(uff, true);
        return uff;
    }

    /**
     * Creates a Translation Motion with the Translation Velocity given in default units. See
     * {@link UserDeclarations#defUnitVel}.
     *
     * @param def  given 3-components Motion definition as String. E.g.: "[1, 0, 0]".
     * @param name given name.
     * @return The TranslatingMotion.
     */
    public TranslatingMotion motion_Translation(String def, String name) {
        _io.say.action("Creating a Translation Motion", true);
        TranslatingMotion tm = _sim.get(MotionManager.class).createMotion(TranslatingMotion.class,
                name);
        _set.object.physicalQuantity(tm.getTranslationVelocity(), def, null, true);
        _io.say.created(tm, true);
        return tm;
    }

    /**
     * Creates a Global Parameter.
     *
     * @param type given type. See {@link macroutils.StaticDeclarations.GlobalParameter} for
     *             options.
     * @param name given name.
     * @return The GlobalParameterBase. Use {@link SetObjects#physicalQuantity} to change it.
     */
    public GlobalParameterBase parameter(String name, StaticDeclarations.GlobalParameter type) {
        return _createParameter(name, type, true);
    }

    /**
     * Creates a Scalar Global Parameter.
     *
     * @param name given name.
     * @param val  given value.
     * @param u    given Units.
     * @return The ScalarGlobalParameter.
     */
    public ScalarGlobalParameter parameter_Scalar(String name, double val, Units u) {
        return _createParameterScalar(name, null, val, u);
    }

    /**
     * Creates a Scalar Global Parameter.
     *
     * @param name given name.
     * @param def  given definition.
     * @return The ScalarGlobalParameter.
     */
    public ScalarGlobalParameter parameter_Scalar(String name, String def) {
        return _createParameterScalar(name, def, 0, _ud.unit_Dimensionless);
    }

    /**
     * Creates a Vector Global Parameter.
     *
     * @param name given name.
     * @param vals given 3-components array with values. E.g.: {1, 2, 3.5}.
     * @param u    given Units.
     * @return The VectorGlobalParameter.
     */
    public VectorGlobalParameter parameter_Vector(String name, double[] vals, Units u) {
        return _createParameterVector(name, vals, u);
    }

    /**
     * Creates a File Table to be used inside STAR-CCM+.
     *
     * @param filename given file which must be in {@link UserDeclarations#simPath} folder.
     * @return The FileTable.
     */
    public FileTable table(String filename) {
        _io.say.action("Creating a File Table", true);
        FileTable ft = (FileTable) _sim.getTableManager()
                .createFromFile(new File(_ud.simPath, filename).toString());
        _io.say.created(ft, true);
        return ft;
    }

    /**
     * Creates a Simple Transform for Visualization, i.e., a Vis Transform.
     * <ul>
     * <li> When providing values to Method, <b>null</b> values are ignored. Units used are
     * {@link UserDeclarations#defUnitLength} and {@link UserDeclarations#defUnitAngle}.
     * </ul>
     *
     * @param org   given 3-components array with origin coordinates (<b>null</b> is ignored). E.g.:
     *              {0, 0, -10}.
     * @param rot   given 3-components array with the rotation axis (<b>null</b> is ignored). E.g.:
     *              {0, 1, 0}.
     * @param angle given rotation angle in {@link UserDeclarations#defUnitAngle}.
     * @param tran  given 3-components array with translation coordinates (<b>null</b> is ignored).
     *              E.g.: {12, 0, -5}.
     * @param scale given 3-components array with scaling factors (<b>null</b> is ignored). E.g.:
     *              {1, 2, 1}.
     * @return The SimpleTransform.
     */
    public SimpleTransform transform_Simple(double[] org, double[] rot, double angle,
            double[] tran, double[] scale) {
        _io.say.action("Creating a Simple Transform", true);
        SimpleTransform st = _sim.getTransformManager().createSimpleTransform();
        Units u = _ud.defUnitLength;
        Coordinate roi = st.getRotationOriginCoordinate();
        Coordinate rac = st.getRotationAxisCoordinate();
        Coordinate tc = st.getTranslationCoordinate();
        if (org != null) {
            roi.setCoordinate(u, u, u, new DoubleVector(org));
        }
        if (rot != null) {
            rac.setCoordinate(u, u, u, new DoubleVector(rot));
        }
        if (tran != null) {
            tc.setCoordinate(u, u, u, new DoubleVector(tran));
        }
        if (scale != null) {
            st.setScale(new DoubleVector(scale));
        }
        _io.say.value("Origin", roi.getValue(), roi.getUnits0(), true);
        _io.say.value("Rotation Axis", rac.getValue(), rac.getUnits0(), true);
        _set.object.physicalQuantity(st.getRotationAngleQuantity(), angle, _ud.defUnitAngle,
                "Angle", true);
        _io.say.value("Translation", tc.getValue(), tc.getUnits0(), true);
        _io.say.value("Scale", st.getScale(), true);
        _io.say.created(st, true);
        return st;
    }

    /**
     * Creates an Update Event based on Delta Time.
     *
     * @param delta given Delta Time Frequency.
     * @param u     given Units.
     * @param acum  <u>true</u> for Accumulated (Default) or <u>false</u> for Non-Accumulated.
     * @return The DeltaMonitorUpdateEvent.
     */
    public DeltaMonitorUpdateEvent updateEvent_DeltaTime(double delta, Units u, boolean acum) {
        return _createDeltaMonitorUE(_get.monitors.physicalTime(), delta, u, "Delta Time Frequency",
                acum);
    }

    /**
     * Creates an Update Event based on Iteration Frequency.
     *
     * @param freq  given Iteration Frequency.
     * @param start given Start Iteration.
     * @return The FrequencyMonitorUpdateEvent.
     */
    public FrequencyMonitorUpdateEvent updateEvent_Iteration(int freq, int start) {
        return _createFreqMonitorUE(_get.monitors.iteration(), freq, start, "Iteration Frequency");
    }

    /**
     * Creates a single Logic Update Event.
     *
     * @param opt given Logic Option. See {@link macroutils.StaticDeclarations.Logic} for options.
     * @return The LogicUpdateEvent.
     */
    public LogicUpdateEvent updateEvent_Logic(StaticDeclarations.Logic opt) {
        _createUE_Type("Logic");
        LogicUpdateEvent lue = _sim.getUpdateEventManager()
                .createUpdateEvent(LogicUpdateEvent.class);
        lue.getLogicOption().setSelected(_getLogic(opt));
        _io.say.created(lue, true);
        return lue;
    }

    /**
     * Creates an Logic Update Event with the given Update Events.
     *
     * @param ues given Array of Update Events.
     * @param opt given Logic Option. See {@link macroutils.StaticDeclarations.Logic} for options.
     * @return The LogicUpdateEvent.
     */
    public LogicUpdateEvent updateEvent_Logic(UpdateEvent[] ues, StaticDeclarations.Logic opt) {
        _createUE_Type("Logic");
        LogicUpdateEvent lue = updateEvent_Logic(opt);
        for (UpdateEvent ue : ues) {
            _io.say.msg("Adding: " + ue.getPresentationName());
            updateEvent_Logic(lue, ue, opt);
        }
        _io.say.created(lue, true);
        return lue;
    }

    /**
     * Creates an Update Event within another Logic Update Event by copying an existing Update
     * Event.
     *
     * @param lue given Logic Update Event to be populated.
     * @param ue  given Update Event to be added to the Logic Update Event.
     * @param opt given Logic Option. See {@link macroutils.StaticDeclarations.Logic} for options.
     */
    public void updateEvent_Logic(LogicUpdateEvent lue, UpdateEvent ue,
            StaticDeclarations.Logic opt) {
        _io.say.action("Adding an Update Event to a Logic Update Event", true);
        _io.say.value("From", ue.getPresentationName(), true, true);
        _io.say.value("To", lue.getPresentationName(), true, true);
        UpdateEvent u = lue.getUpdateEventManager().createUpdateEvent(ue.getClass());
        lue.getLogicOption().setSelected(_getLogic(opt));
        u.copyProperties(ue);
        u.setPresentationName(ue.getPresentationName());
        _io.say.created(u, true);
    }

    /**
     * Creates an Update Event based on a Range.
     *
     * @param pm    given Plotable Monitor. Use with {@link GetMonitors#iteration} or
     *              {@link GetMonitors#physicalTime} method.
     * @param opt   given Operator Option. See {@link macroutils.StaticDeclarations.Operator} for
     *              options.
     * @param range given Range.
     * @return The Range Monitor Update Event variable.
     */
    public RangeMonitorUpdateEvent updateEvent_Range(PlotableMonitor pm,
            StaticDeclarations.Operator opt, double range) {
        return _createRangeMonitorUE(pm, opt, range);
    }

    /**
     * Creates an Update Event based on Time Step Frequency.
     *
     * @param freq  given Time Step Frequency.
     * @param start given Start Time Step.
     * @return FrequencyMonitorUpdateEvent.
     */
    public FrequencyMonitorUpdateEvent updateEvent_TimeStep(int freq, int start) {
        return _createFreqMonitorUE(_get.monitors.physicalTime(), freq, start,
                "Time Step Frequency");
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    private DeltaMonitorUpdateEvent _createDeltaMonitorUE(PlotableMonitor pm, double delta,
            Units u, String type, boolean acum) {
        _createUE_Type(type);
        DeltaMonitorUpdateEvent dm = _sim.getUpdateEventManager()
                .createUpdateEvent(DeltaMonitorUpdateEvent.class);
        dm.setMonitor(pm);
        if (acum) {
            dm.getDeltaOption().setSelected(UpdateEventDeltaOption.Type.ACCUMULATED);
        } else {
            dm.getDeltaOption().setSelected(UpdateEventDeltaOption.Type.NONACCUMULATED);
        }
        dm.getDeltaThreshold().setValue(delta);
        dm.getDeltaThreshold().setUnits(u);
        return dm;
    }

    private FrequencyMonitorUpdateEvent _createFreqMonitorUE(PlotableMonitor pm, int freq,
            int start, String type) {
        _createUE_Type(type);
        Class<FrequencyMonitorUpdateEvent> cl = FrequencyMonitorUpdateEvent.class;
        FrequencyMonitorUpdateEvent ev = _sim.getUpdateEventManager().createUpdateEvent(cl);
        ev.setMonitor(pm);
        ev.setSampleFrequency(freq);
        ev.setStartCount(start);
        _io.say.created(ev, true);
        return ev;
    }

    private GlobalParameterBase _createParameter(String name,
            StaticDeclarations.GlobalParameter type, boolean vo) {
        _io.say.action(String.format("Creating a %s Global Parameter", type.getType()), true);
        GlobalParameterManager gpm = _sim.get(GlobalParameterManager.class);
        if (gpm.has(name)) {
            _io.say.value("Skipping... Parameter already exists", name, true, vo);
            return gpm.getObject(name);
        }
        GlobalParameterBase gpb;
        switch (type) {
            case SCALAR:
                gpb = gpm.createGlobalParameter(ScalarGlobalParameter.class, type.getType());
                break;
            case VECTOR:
                gpb = gpm.createGlobalParameter(VectorGlobalParameter.class, type.getType());
                break;
            default:
                gpb = gpm.createGlobalParameter(ScalarGlobalParameter.class, type.getType());
                break;
        }
        gpb.setPresentationName(name);
        _io.say.created(gpb, vo);
        return gpb;
    }

    private ScalarGlobalParameter _createParameterScalar(String name, String def, double val,
            Units u) {
        ScalarGlobalParameter sgp = (ScalarGlobalParameter) _createParameter(name,
                StaticDeclarations.GlobalParameter.SCALAR, false);
        sgp.setDimensions(_get.units.dimensions(u));
        if (def == null) {
            _set.object.physicalQuantity(sgp.getQuantity(), val, u, name, true);
        } else {
            _set.object.physicalQuantity(sgp.getQuantity(), def, name, true);
        }
        _io.say.created(sgp, true);
        return sgp;
    }

    private VectorGlobalParameter _createParameterVector(String name, double[] vals, Units u) {
        VectorGlobalParameter vgp = (VectorGlobalParameter) _createParameter(name,
                StaticDeclarations.GlobalParameter.VECTOR, false);
        vgp.setDimensions(_get.units.dimensions(u));
        _set.object.physicalQuantity(vgp.getQuantity(), vals, u, name, true);
        _io.say.created(vgp, true);
        return vgp;
    }

    private RangeMonitorUpdateEvent _createRangeMonitorUE(PlotableMonitor pm,
            StaticDeclarations.Operator opt, double range) {
        _createUE_Type("Range");
        RangeMonitorUpdateEvent ev = _sim.getUpdateEventManager()
                .createUpdateEvent(RangeMonitorUpdateEvent.class);
        ev.setMonitor(pm);
        ev.getRangeOption().setSelected(_getOperator(opt));
        ev.getRangeQuantity().setValue(range);
        _io.say.created(ev, true);
        _io.say.ok(true);
        return ev;
    }

    private void _createUE_Type(String type) {
        _io.say.action("Creating an Update Event", true);
        _io.say.value("Type", type, true, true);
    }

    private ArrayList<Double> _getArrayList(int n, double val) {
        ArrayList<Double> ad = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ad.add(val);
        }
        return ad;
    }

    private UpdateEventLogicOption.Type _getLogic(StaticDeclarations.Logic opt) {
        UpdateEventLogicOption.Type uelo = UpdateEventLogicOption.Type.AND;
        switch (opt) {
            case OR:
                uelo = UpdateEventLogicOption.Type.OR;
                break;
            case XOR:
                uelo = UpdateEventLogicOption.Type.XOR;
                break;
        }
        _io.say.value("Logic type", opt.toString(), true, true);
        return uelo;
    }

    private UpdateEventRangeOption.Type _getOperator(StaticDeclarations.Operator opt) {
        UpdateEventRangeOption.Type uero = UpdateEventRangeOption.Type.LESS_THAN_OR_EQUALS;
        switch (opt) {
            case GREATER_THAN_OR_EQUALS:
                uero = UpdateEventRangeOption.Type.GREATER_THAN_OR_EQUALS;
                break;
            case LESS_THAN_OR_EQUALS:
                break;
            default:
                _io.say.value("Invalid operator", opt.toString(), true, true);
                return null;
        }
        _io.say.value("Operator type", opt.toString(), true, true);
        return uero;
    }

}
