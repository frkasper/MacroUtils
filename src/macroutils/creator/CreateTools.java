package macroutils.creator;

import java.awt.*;
import java.io.*;
import java.util.*;
import macroutils.*;
import macroutils.getter.*;
import star.base.neo.*;
import star.base.report.*;
import star.common.*;
import star.motion.*;
import star.vis.*;

/**
 * Low-level class for creating Tools objects with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateTools {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateTools(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private DeltaMonitorUpdateEvent _createDeltaMonitorUE(PlotableMonitor pm, double delta,
            Units u, String type, boolean acum) {
        _createUE_Type(type);
        DeltaMonitorUpdateEvent dm = _sim.getUpdateEventManager().createUpdateEvent(DeltaMonitorUpdateEvent.class);
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

    private FrequencyMonitorUpdateEvent _createFreqMonitorUE(PlotableMonitor pm, int freq, int start, String type) {
        _createUE_Type(type);
        Class<FrequencyMonitorUpdateEvent> cl = FrequencyMonitorUpdateEvent.class;
        FrequencyMonitorUpdateEvent ev = _sim.getUpdateEventManager().createUpdateEvent(cl);
        ev.setMonitor(pm);
        ev.setSampleFrequency(freq);
        ev.setStartCount(start);
        _io.say.created(ev, true);
        return ev;
    }

    private RangeMonitorUpdateEvent _createRangeMonitorUE(PlotableMonitor pm, StaticDeclarations.Operator opt,
            double range) {
        _createUE_Type("Range");
        RangeMonitorUpdateEvent ev = _sim.getUpdateEventManager().createUpdateEvent(RangeMonitorUpdateEvent.class);
        ev.setMonitor(pm);
        ev.getRangeOption().setSelected(_getOperator(opt));
        ev.getRangeQuantity().setValue(range);
        _io.say.created(ev, true);
        _io.say.ok(true);
        return ev;
    }

    private void _createUE_Type(String type) {
        _io.say.action("Creating an Update Event", true);
        _io.say.msg(true, "Type: \"%s\".", type);
    }

    private ArrayList<Double> _getArrayList(int n, double val) {
        ArrayList<Double> ad = new ArrayList();
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
        _io.say.msg(true, "Logic type: \"%s\".", opt.toString());
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
                _io.say.msg(true, "Invalid operator: \"%s\".", opt.toString());
                return null;
        }
        _io.say.msg(true, "Operator type: \"%s\".", opt.toString());
        return uero;
    }

    /**
     * Creates a Time Report Annotation with a custom decimal formatter. See {@link String#format}.
     *
     * @param fmt given time format.
     * @return The ReportAnnotation.
     */
    public ReportAnnotation annotation_Time(String fmt) {
        Report r = _add.report.expression("Time", _ud.unit_s, _ud.dimTime, "$Time", false);
        _io.say.action("Creating a Time Report Annotation", true);
        if (_sim.getAnnotationManager().has(r.getPresentationName())) {
            _io.say.msg(true, "\"%s\" Report Annotation already exists...", r.getPresentationName());
            return (ReportAnnotation) _sim.getAnnotationManager().getObject(r.getPresentationName());
        }
        ReportAnnotation ra = _sim.getAnnotationManager().createReportAnnotation(r);
        _io.say.created(ra, true);
        return ra;
    }

    /**
     * Creates a Cylindrical Coordinate System using default length units. See {@link UserDeclarations#defUnitLength}.
     *
     * @param org given 3-components double[] array for the Origin. E.g.: {0, 0, 0}.
     * @param b1 given 3-components double[] array for the Basis1. E.g.: {0, 1, 0}.
     * @param b2 given 3-components double[] array for the Basis2. E.g.: {0, 0, 1}.
     * @return The CylindricalCoordinateSystem.
     */
    public CylindricalCoordinateSystem coordinateSystem_Cylindrical(double[] org, double[] b1, double[] b2) {
        _io.say.action("Creating a Cylindrical Coordinate System", true);
        LocalCoordinateSystemManager lcsm = _ud.lab0.getLocalCoordinateSystemManager();
        CylindricalCoordinateSystem ccsys = lcsm.createLocalCoordinateSystem(CylindricalCoordinateSystem.class);
        Coordinate c = ccsys.getOrigin();
        c.setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength, new DoubleVector(org));
        ccsys.setBasis0(new DoubleVector(b1));
        ccsys.setBasis1(new DoubleVector(b2));
        _io.say.created(ccsys, true);
        return ccsys;
    }

    /**
     * Creates a custom Colormap.
     *
     * @param name given Colormap/LookupTable name.
     * @param ac given ArrayList of {@link java.awt.Color}.
     * @param ad given ArrayList of opacities. If null is given, all colors will be opaque.
     * @param cs given color space code, as in {@link StaticDeclarations.ColorSpace}.
     * @return The LookupTable, i.e., the colormap object as in STAR-CCM+.
     */
    public LookupTable colormap(String name, ArrayList<Color> ac, ArrayList<Double> ad,
            StaticDeclarations.ColorSpace cs) {
        _io.say.action("Creating a Colormap", true);
        if (_sim.get(LookupTableManager.class).has(name)) {
            _io.say.msg(true, "Colormap already exists: \"%s\".", name);
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
        _io.say.msg(true, "Colors: %s", ac.toString());
        _io.say.msg(true, "Opacities: %s", _get.strings.withinTheBrackets(ad.toString()));
        _io.say.msg(true, "Color Space: \"%s\".", cs.toString());
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
     * Creates a User Defined Field Function.
     *
     * @param name given Field Function name.
     * @param def given Field Function definition.
     * @param dim given Dimensions.
     * @param type given type.
     * @return The FieldFunction.
     */
    public FieldFunction fieldFunction(String name, String def, Dimensions dim, FieldFunctionTypeOption.Type type) {
        _io.say.action("Creating a Field Function", true);
        FieldFunction ff = _get.objects.fieldFunction(name, false);
        if (ff != null) {
            _io.say.msg(true, "\"%s\" already exists...", name);
            return ff;
        }
        UserFieldFunction uff = _sim.getFieldFunctionManager().createFieldFunction();
        uff.setPresentationName(name);
        uff.setFunctionName(name.replaceAll("( |\\(|\\)|)", ""));
        uff.setDefinition(def);
        uff.getTypeOption().setSelected(type);
        _io.say.value("Type", uff.getTypeOption().getSelectedElement().getPresentationName(), true, true);
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
     * @param def given 3-components Motion definition. E.g.: [1, 0, 0].
     * @param name given name.
     * @return The TranslatingMotion.
     */
    public TranslatingMotion motion_Translation(String def, String name) {
        _io.say.action("Creating a Translation Motion", true);
        _io.say.msg(true, "Definition: \"%s\".", def);
        TranslatingMotion tm = _sim.get(MotionManager.class).createMotion(TranslatingMotion.class, name);
        tm.getTranslationVelocity().setDefinition(def);
        tm.getTranslationVelocity().setUnits(_ud.defUnitVel);
        _io.say.created(tm, true);
        return tm;
    }

    /**
     * Creates a Simple Transform for Visualization, i.e., a Vis Transform.
     * <ul>
     * <li> When providing values to Method, <b>null</b> values are ignored. Units used are
     * {@link UserDeclarations#defUnitLength} and {@link UserDeclarations#defUnitAngle}.
     * </ul>
     *
     * @param org given 3-components array with origin coordinates (<b>null</b> is ignored). E.g.: {0, 0, -10}.
     * @param rot given 3-components array with the rotation axis (<b>null</b> is ignored). E.g.: {0, 1, 0}.
     * @param angle given rotation angle in {@link UserDeclarations#defUnitAngle}.
     * @param tran given 3-components array with translation coordinates (<b>null</b> is ignored). E.g.: {12, 0, -5}.
     * @param scale given 3-components array with scaling factors (<b>null</b> is ignored). E.g.: {1, 2, 1}.
     * @return The SimpleTransform.
     */
    public SimpleTransform transform_Simple(double[] org, double[] rot, double angle, double[] tran, double[] scale) {
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
        _set.object.physicalQuantity(st.getRotationAngleQuantity(), angle, null, _ud.defUnitAngle, "Angle", true);
        _io.say.value("Translation", tc.getValue(), tc.getUnits0(), true);
        _io.say.value("Scale", st.getScale(), true);
        _io.say.created(st, true);
        return st;
    }

    /**
     * Creates a File Table to be used inside STAR-CCM+.
     *
     * @param filename given file which must be in {@link UserDeclarations#simPath} folder.
     * @return The FileTable.
     */
    public FileTable table(String filename) {
        _io.say.action("Creating a File Table", true);
        FileTable ft = (FileTable) _sim.getTableManager().createFromFile(new File(_ud.simPath, filename).toString());
        _io.say.created(ft, true);
        return ft;
    }

    /**
     * Creates an Update Event based on Delta Time.
     *
     * @param delta given Delta Time Frequency.
     * @param u given Units.
     * @param acum <u>true</u> for Accumulated (Default) or <u>false</u> for Non-Accumulated.
     * @return The DeltaMonitorUpdateEvent.
     */
    public DeltaMonitorUpdateEvent updateEvent_DeltaTime(double delta, Units u, boolean acum) {
        return _createDeltaMonitorUE(_get.monitors.physicalTime(), delta, u, "Delta Time Frequency", acum);
    }

    /**
     * Creates an Update Event based on Iteration Frequency.
     *
     * @param freq given Iteration Frequency.
     * @param start given Start Iteration.
     * @return The FrequencyMonitorUpdateEvent.
     */
    public FrequencyMonitorUpdateEvent updateEvent_Iteration(int freq, int start) {
        return _createFreqMonitorUE(_get.monitors.iteration(), freq, start, "Iteration Frequency");
    }

    /**
     * Creates a single Logic Update Event.
     *
     * @param opt given Logic Option. See {@link StaticDeclarations.Logic} for options.
     * @return The LogicUpdateEvent.
     */
    public LogicUpdateEvent updateEvent_Logic(StaticDeclarations.Logic opt) {
        _createUE_Type("Logic");
        LogicUpdateEvent lue = _sim.getUpdateEventManager().createUpdateEvent(LogicUpdateEvent.class);
        lue.getLogicOption().setSelected(_getLogic(opt));
        _io.say.created(lue, true);
        return lue;
    }

    /**
     * Creates an Logic Update Event with the given Update Events.
     *
     * @param ues given Array of Update Events.
     * @param opt given Logic Option. See {@link StaticDeclarations.Logic} for options.
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
     * Creates an Update Event within another Logic Update Event by copying an existing Update Event.
     *
     * @param lue given Logic Update Event to be populated.
     * @param ue given Update Event to be added to the Logic Update Event.
     * @param opt given Logic Option. See {@link StaticDeclarations.Logic} for options.
     */
    public void updateEvent_Logic(LogicUpdateEvent lue, UpdateEvent ue, StaticDeclarations.Logic opt) {
        _io.say.action("Adding an Update Event to a Logic Update Event", true);
        _io.say.msg(true, "From: %s. To: %s.", ue.getPresentationName(), lue.getPresentationName());
        UpdateEvent u = lue.getUpdateEventManager().createUpdateEvent(ue.getClass());
        lue.getLogicOption().setSelected(_getLogic(opt));
        u.copyProperties(ue);
        u.setPresentationName(ue.getPresentationName());
        _io.say.created(u, true);
    }

    /**
     * Creates an Update Event based on a Range.
     *
     * @param pm given Plotable Monitor. Use with {@link GetMonitors#iteration} or {@link GetMonitors#physicalTime}
     * method.
     * @param opt given Operator Option. See {@link StaticDeclarations.Operator} for options.
     * @param range given Range.
     * @return The Range Monitor Update Event variable.
     */
    public RangeMonitorUpdateEvent updateEvent_Range(PlotableMonitor pm, StaticDeclarations.Operator opt,
            double range) {
        return _createRangeMonitorUE(pm, opt, range);
    }

    /**
     * Creates an Update Event based on Time Step Frequency.
     *
     * @param freq given Time Step Frequency.
     * @param start given Start Time Step.
     * @return FrequencyMonitorUpdateEvent.
     */
    public FrequencyMonitorUpdateEvent updateEvent_TimeStep(int freq, int start) {
        return _createFreqMonitorUE(_get.monitors.physicalTime(), freq, start, "Time Step Frequency");
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _add = _mu.add;
        _get = _mu.get;
        _set = _mu.set;
        _tmpl = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainCreator _add = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.templates.MainTemplates _tmpl = null;
    private macroutils.io.MainIO _io = null;

}
