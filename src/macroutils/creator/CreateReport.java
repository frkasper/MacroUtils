package macroutils.creator;

import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.base.report.AreaAverageReport;
import star.base.report.ExpressionReport;
import star.base.report.MaxReport;
import star.base.report.MinReport;
import star.base.report.Report;
import star.base.report.ReportMonitor;
import star.base.report.ScalarReport;
import star.base.report.SumReport;
import star.base.report.SurfaceIntegralReport;
import star.base.report.SurfaceUniformityReport;
import star.base.report.VolumeAverageReport;
import star.common.Axis;
import star.common.Cartesian2DAxis;
import star.common.Cartesian2DAxisManager;
import star.common.Dimensions;
import star.common.FieldFunction;
import star.common.MonitorPlot;
import star.common.ScalarPhysicalQuantity;
import star.common.Simulation;
import star.common.Units;
import star.energy.PressureDropReport;
import star.flow.ForceCoefficientReport;
import star.flow.ForceReport;
import star.flow.ForceReportForceOption;
import star.flow.MassAverageReport;
import star.flow.MassFlowAverageReport;
import star.flow.MassFlowReport;
import star.vis.FrontalAreaReport;

/**
 * Low-level class for creating Reports with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateReport {

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
    public CreateReport(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Report based on an Expression.
     *
     * @param name given Report name.
     * @param u    given Units.
     * @param dim  given dimensions.
     * @param def  given Expression definition. E.g.: "$Time".
     * @param vo   given verbose option. False will not print anything.
     * @return The Expression Report.
     */
    public ExpressionReport expression(String name, Units u, Dimensions dim, String def,
            boolean vo) {
        _io.say.action("Creating an Expression Report", vo);
        if (_hasReport(name, false) != null) {
            return (ExpressionReport) _hasReport(name, vo);
        }
        ExpressionReport er = (ExpressionReport) _createReport(ExpressionReport.class, name);
        er.setDimensions(dim);
        er.setDefinition(def);
        er.setUnits(u);
        _io.say.msg(vo, "%s == %s (%s)", name, def, _getString(u));
        _io.say.unit(u, vo);
        _io.say.dimension(dim, vo);
        _io.say.created(er, vo);
        return er;
    }

    /**
     * Creates a Force Report using the default Units for the selected Boundaries. A Monitor and
     * Plot will be also created.
     *
     * @param ano       given ArrayList of NamedObjects, if applicable. E.g.: boundaries,
     *                  interfaces, planes, etc...
     * @param name      given Report name.
     * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0,
     *                  0}.
     * @param vo        given verbose option. False will not print anything.
     * @return The Force Report.
     */
    public ForceReport force(ArrayList<? extends NamedObject> ano, String name, double[] direction,
            boolean vo) {
        _io.say.action("Creating a Force Report", vo);
        if (_hasReport(name, false) != null) {
            return (ForceReport) _hasReport(name, vo);
        }
        ForceReport fr = (ForceReport) _createReport(ForceReport.class, name);
        fr.getReferencePressure().setUnits(_ud.unit_Pa);
        fr.getReferencePressure().setValue(0.0);
        fr.setUnits(_ud.defUnitForce);
        fr.getParts().setObjects(ano);
        fr.getDirection().setComponents(direction[0], direction[1], direction[2]);
        _io.say.objects(ano, "Objects", vo);
        _io.say.unit(fr.getUnits(), vo);
        _io.say.created(fr, vo);
        monitorAndPlot(fr, null, String.format("Force (%s)", _getString(fr.getUnits())), vo);
        return fr;
    }

    /**
     * Creates a Force Coefficient Report using the default Units for the selected Boundaries. A
     * Monitor and Plot will be also created.
     *
     * The Pressure Coefficient variable will be updated based on the supplied values.
     *
     * @param ano       given ArrayList of NamedObjects, if applicable. E.g.: boundaries,
     *                  interfaces, planes, etc...
     * @param name      given Report name.
     * @param refP      given Reference Pressure in default unit. See
     *                  {@link UserDeclarations#defUnitPress}.
     * @param refDen    given Reference Density in default unit. See
     *                  {@link UserDeclarations#defUnitDen}.
     * @param refVel    given Reference Velocity in default unit. See
     *                  {@link UserDeclarations#defUnitVel}.
     * @param refArea   given Reference Area in default unit. See
     *                  {@link UserDeclarations#defUnitArea}.
     * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0,
     *                  0}.
     * @param vo        given verbose option. False will not print anything.
     * @return The Force Coefficient Report.
     */
    public ForceCoefficientReport forceCoefficient(ArrayList<? extends NamedObject> ano,
            String name, double refP, double refDen, double refVel, double refArea,
            double[] direction, boolean vo) {
        _io.say.action("Creating a Force Coefficient Report", vo);
        if (_hasReport(name, false) != null) {
            return (ForceCoefficientReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Objects", vo);
        ForceCoefficientReport fcr
                = (ForceCoefficientReport) _createReport(ForceCoefficientReport.class, name);
        _setSPQ(fcr.getReferencePressure(), refP, _ud.defUnitPress, "Reference Pressure", vo);
        _setSPQ(fcr.getReferenceDensity(), refDen, _ud.defUnitDen, "Reference Density", vo);
        _setSPQ(fcr.getReferenceVelocity(), refVel, _ud.defUnitVel, "Reference Velocity", vo);
        _setSPQ(fcr.getReferenceArea(), refArea, _ud.defUnitArea, "Reference Area", vo);
        fcr.getForceOption().setSelected(ForceReportForceOption.Type.PRESSURE_AND_SHEAR);
        fcr.getDirection().setComponents(direction[0], direction[1], direction[2]);
        fcr.getParts().setObjects(ano);
        //-- Pressure Coefficient update. Will use refP = 0.
        _set.object.fieldFunctionPressureCoefficient(refDen, 0., refVel, vo);
        _io.say.created(fcr, vo);
        monitorAndPlot(fcr, null, String.format("Force Coefficient", _getString(fcr.getUnits())),
                vo);
        return fcr;
    }

    /**
     * Creates a Frontal Area Report for the selected Boundaries.
     *
     * @param ano       given ArrayList of NamedObjects, if applicable. E.g.: boundaries,
     *                  interfaces, planes, etc...
     * @param name      given Report name.
     * @param viewUp    a 3-component array of the screen View Up. E.g.: if Y is showing up in the
     *                  screen, then {0, 1, 0}.
     * @param direction a 3-component array of the given direction of flow. E.g.: in X it is {1, 0,
     *                  0}.
     * @param vo        given verbose option. False will not print anything.
     * @return The Frontal Area Report.
     */
    public Report frontalArea(ArrayList<? extends NamedObject> ano, String name, double[] viewUp,
            double[] direction, boolean vo) {
        _io.say.action("Creating a Frontal Area Report", vo);
        if (_hasReport(name, false) != null) {
            return (ForceCoefficientReport) _hasReport(name, vo);
        }
        FrontalAreaReport far = (FrontalAreaReport) _createReport(FrontalAreaReport.class, name);
        _io.say.objects(ano, "Objects", vo);
        _io.say.value("View Up", new DoubleVector(viewUp), vo);
        _io.say.value("Flow Direction", new DoubleVector(direction), vo);
        far.getViewUpCoordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength,
                _ud.defUnitLength, new DoubleVector(viewUp));
        far.getNormalCoordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength,
                _ud.defUnitLength, new DoubleVector(direction));
        far.getParts().setObjects(ano);
        far.setUnits(_ud.defUnitArea);
        _io.say.created(far, vo);
        return far;
    }

    /**
     * Creates a Mass Average Report for the selected Regions.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects. Just make sure it is 3D.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return Mass Average Report.
     */
    public MassAverageReport massAverage(ArrayList<? extends NamedObject> ano, String name,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Mass Average Report", vo);
        if (_hasReport(name, false) != null) {
            return (MassAverageReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        MassAverageReport mar = (MassAverageReport) _createReport(MassAverageReport.class, name);
        _setSR(mar, ano, ff, u, vo);
        monitorAndPlot(mar, null, String.format("Mass Average of %s (%s)", ff.getPresentationName(),
                _getString(u)), vo);
        return mar;
    }

    /**
     * Creates a Mass Flow Report for the selected Boundary.
     *
     * @param no   given NamedObject, if applicable. E.g.: boundaries, interfaces, planes, etc...
     * @param name given Report name.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Mass Flow Report.
     */
    public MassFlowReport massFlow(NamedObject no, String name, Units u, boolean vo) {
        return massFlow(_get.objects.arrayList(no), name, u, vo);
    }

    /**
     * Creates a Mass Flow Report for the selected Boundaries.
     *
     * @param ano  given ArrayList of NamedObjects, if applicable. E.g.: boundaries, interfaces,
     *             planes, etc...
     * @param name given Report name.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Mass Flow Report.
     */
    public MassFlowReport massFlow(ArrayList<? extends NamedObject> ano, String name, Units u,
            boolean vo) {
        _io.say.action("Creating a Mass Flow Report", vo);
        if (_hasReport(name, false) != null) {
            return (MassFlowReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Objects", vo);
        _io.say.unit(u, vo);
        MassFlowReport mfr = (MassFlowReport) _createReport(MassFlowReport.class, name);
        _setSR(mfr, ano, null, u, vo);
        monitorAndPlot(mfr, null, String.format("Mass Flow (%s)", _getString(u)), vo);
        return mfr;
    }

    /**
     * Creates a Mass Flow Average Report for the selected Boundary.
     *
     * @param no   given NamedObject, if applicable. E.g.: boundaries, interfaces, planes, etc...
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Mass Flow Average Report.
     */
    public MassFlowAverageReport massFlowAverage(NamedObject no, String name, FieldFunction ff,
            Units u, boolean vo) {
        return massFlowAverage(_get.objects.arrayList(no), name, ff, u, vo);
    }

    /**
     * Creates a Mass Flow Average Report for the selected Boundaries.
     *
     * @param ano  given ArrayList of NamedObjects, if applicable. E.g.: boundaries, interfaces,
     *             planes, etc...
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Mass Flow Average Report.
     */
    public MassFlowAverageReport massFlowAverage(ArrayList<? extends NamedObject> ano, String name,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Mass Flow Average Report", vo);
        if (_hasReport(name, false) != null) {
            return (MassFlowAverageReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Objects", vo);
        _io.say.scalar(ff, u, vo);
        MassFlowAverageReport mfa
                = (MassFlowAverageReport) _createReport(MassFlowAverageReport.class, name);
        _setSR(mfa, ano, ff, u, vo);
        monitorAndPlot(mfa, null,
                String.format("Mass Flow Average of %s (%s)", ff.getPresentationName(),
                        _getString(u)), vo);
        return mfa;
    }

    /**
     * Creates a Maximum Report for the selected STAR-CCM+ Object.
     *
     * @param no   given STAR-CCM+ Object.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Maximum Report.
     */
    public MaxReport maximum(NamedObject no, String name, FieldFunction ff, Units u, boolean vo) {
        return maximum(_get.objects.arrayList(no), name, ff, u, vo);
    }

    /**
     * Creates a Maximum Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Maximum Report.
     */
    public MaxReport maximum(ArrayList<? extends NamedObject> ano, String name, FieldFunction ff,
            Units u, boolean vo) {
        _io.say.action("Creating a Maximum Report", vo);
        if (_hasReport(name, false) != null) {
            return (MaxReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        MaxReport mr = (MaxReport) _createReport(MaxReport.class, name);
        _setSR(mr, ano, ff, u, vo);
        monitorAndPlot(mr, null,
                String.format("Maximum of %s (%s)", ff.getPresentationName(), _getString(u)), vo);
        return mr;
    }

    /**
     * Creates a Minimum Report for the selected STAR-CCM+ Object.
     *
     * @param no   given STAR-CCM+ Object.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Minimum Report.
     */
    public MinReport minimum(NamedObject no, String name, FieldFunction ff, Units u, boolean vo) {
        return minimum(_get.objects.arrayList(no), name, ff, u, vo);
    }

    /**
     * Creates a Minimum Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Minimum Report.
     */
    public MinReport minimum(ArrayList<? extends NamedObject> ano, String name, FieldFunction ff,
            Units u, boolean vo) {
        _io.say.action("Creating a Minimum Report", vo);
        if (_hasReport(name, false) != null) {
            return (MinReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        MinReport mr = (MinReport) _createReport(MinReport.class, name);
        _setSR(mr, ano, ff, u, vo);
        monitorAndPlot(mr, null,
                String.format("Minimum of %s (%s)", ff.getPresentationName(), _getString(u)), vo);
        return mr;
    }

    /**
     * Creates a Monitor and a Plot from a Report. Reports starting with an underscore will be
     * skipped.
     *
     * @param r  given Report.
     * @param xl given X-axis label for the Plot. NULL it will be ignored.
     * @param yl given Y-axis label for the Plot. NULL it will be ignored.
     * @param vo given verbose option. False will not print anything.
     * @return The Report Monitor.
     */
    public ReportMonitor monitorAndPlot(Report r, String xl, String yl, boolean vo) {
        if (r.getPresentationName().startsWith("_")) {
            return null;
        }
        if (r instanceof ScalarReport) {
            ScalarReport sr = (ScalarReport) r;
            if (_isFieldFunctionOnlyValidFor2D(sr.getFieldFunction())) {
                return null;
            }
        }
        ReportMonitor rm = r.createMonitor();
        MonitorPlot mp = _sim.getPlotManager().createMonitorPlot();
        rm.setPresentationName(r.getPresentationName());
        mp.setPresentationName(r.getPresentationName());
        mp.setTitle(r.getPresentationName());
        mp.getDataSetManager().addDataProvider(rm);
        Cartesian2DAxisManager cam = (Cartesian2DAxisManager) mp.getAxisManager();
        _setTitle(cam.getAxis("Bottom Axis"), xl);
        _setTitle(cam.getAxis("Left Axis"), yl);
        _io.say.created(rm, vo);
        _io.say.created(mp, vo);
        return rm;
    }

    /**
     * Creates a Pressure Drop Report. Definition: dP = P1 - P2.
     *
     * @param no1  given NamedObject 1, if applicable. High Pressure. E.g.: boundaries, interfaces,
     *             planes, etc...
     * @param no2  given NamedObject 2, if applicable. Low Pressure. E.g.: boundaries, interfaces,
     *             planes, etc...
     * @param name given Report name.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Pressure Drop Report.
     */
    public PressureDropReport pressureDrop(NamedObject no1, NamedObject no2, String name,
            Units u, boolean vo) {
        _io.say.action("Creating a Pressure Drop Report", vo);
        if (_hasReport(name, false) != null) {
            return (PressureDropReport) _hasReport(name, vo);
        }
        _io.say.value("High Pressure", no1.getPresentationName(), true, vo);
        _io.say.value("Low Pressure", no2.getPresentationName(), true, vo);
        _io.say.unit(u, vo);
        PressureDropReport pdr = (PressureDropReport) _createReport(PressureDropReport.class, name);
        pdr.getParts().setObjects(no1);
        pdr.getLowPressureParts().setObjects(no2);
        pdr.setUnits(u);
        _io.say.created(pdr, vo);
        monitorAndPlot(pdr, null, "Pressure Drop", vo);
        return pdr;
    }

    /**
     * Creates a Sum Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Sum Report.
     */
    public SumReport sum(ArrayList<? extends NamedObject> ano, String name, FieldFunction ff,
            Units u, boolean vo) {
        _io.say.action("Creating a Sum Report", vo);
        if (_hasReport(name, false) != null) {
            return (SumReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        SumReport sr = (SumReport) _createReport(SumReport.class, name);
        _setSR(sr, ano, ff, u, vo);
        monitorAndPlot(sr, null,
                String.format("Sum of %s (%s)", ff.getPresentationName(), _getString(u)), vo);
        return sr;
    }

    /**
     * Creates a Surface Average Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Surface Average Report.
     */
    public AreaAverageReport surfaceAverage(ArrayList<? extends NamedObject> ano, String name,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Surface Average Report", vo);
        if (_hasReport(name, false) != null) {
            return (AreaAverageReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        AreaAverageReport aar = (AreaAverageReport) _createReport(AreaAverageReport.class, name);
        _setSR(aar, ano, ff, u, vo);
        monitorAndPlot(aar, null,
                String.format("Surface Average of %s (%s)", ff.getPresentationName(),
                        _getString(u)), vo);
        return aar;
    }

    /**
     * Creates a Surface Integral Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Surface Integral Report.
     */
    public SurfaceIntegralReport surfaceIntegral(ArrayList<? extends NamedObject> ano, String name,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Surface Integral Report", vo);
        if (_hasReport(name, false) != null) {
            return (SurfaceIntegralReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        SurfaceIntegralReport sir
                = (SurfaceIntegralReport) _createReport(SurfaceIntegralReport.class, name);
        _setSR(sir, ano, ff, u, vo);
        monitorAndPlot(sir, null,
                String.format("Surface Integral of %s (%s)", ff.getPresentationName(),
                        _getString(u)), vo);
        return sir;
    }

    /**
     * Creates a Surface Uniformity Report for the selected STAR-CCM+ Objects.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Surface Uniformity Report.
     */
    public SurfaceUniformityReport surfaceUniformity(ArrayList<? extends NamedObject> ano,
            String name, FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Surface Uniformity Report", vo);
        if (_hasReport(name, false) != null) {
            return (SurfaceUniformityReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        SurfaceUniformityReport sur
                = (SurfaceUniformityReport) _createReport(SurfaceUniformityReport.class, name);
        _setSR(sur, ano, ff, u, vo);
        monitorAndPlot(sur, null,
                String.format("Surface Uniformity of %s (%s)", ff.getPresentationName(),
                        _getString(u)), vo);
        return sur;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    /**
     * Creates a Volume Average Report for the selected Regions.
     *
     * @param ano  given ArrayList of STAR-CCM+ Objects. Just make sure it is 3D.
     * @param name given Report name.
     * @param ff   given Field Function.
     * @param u    given Units.
     * @param vo   given verbose option. False will not print anything.
     * @return The Volume Average Report.
     */
    public VolumeAverageReport volumeAverage(ArrayList<? extends NamedObject> ano, String name,
            FieldFunction ff, Units u, boolean vo) {
        _io.say.action("Creating a Volume Average Report", vo);
        if (_hasReport(name, false) != null) {
            return (VolumeAverageReport) _hasReport(name, vo);
        }
        _io.say.objects(ano, "Parts", vo);
        _io.say.scalar(ff, u, vo);
        VolumeAverageReport var
                = (VolumeAverageReport) _createReport(VolumeAverageReport.class, name);
        _setSR(var, ano, ff, u, vo);
        monitorAndPlot(var, null,
                String.format("Volume Average of %s (%s)", ff.getPresentationName(),
                        _getString(u)), vo);
        return var;
    }

    private <T extends Report> Report _createReport(Class<T> clz, String name) {
        Report r = _sim.getReportManager().createReport(clz);
        if (name == null) {
            name = r.getBeanDisplayName();
        }
        r.setPresentationName(name);
        return r;
    }

    private String _getString(Units u) {
        return _get.strings.fromUnit(u);
    }

    private Report _hasReport(String name, boolean vo) {
        if (_sim.getReportManager().has(name)) {
            _io.say.value("Skipping... Report already exists", name, true, vo);
            return _sim.getReportManager().getReport(name);
        }
        return null;
    }

    private boolean _isFieldFunctionOnlyValidFor2D(FieldFunction ff) {
        return ff.getFunctionName().matches("FaceQuality|FreeEdges");
    }

    private void _setSPQ(ScalarPhysicalQuantity spq, double val, Units u, String text, boolean vo) {
        _set.object.physicalQuantity(spq, val, u, text, vo);
    }

    private ScalarReport _setSR(ScalarReport sr, ArrayList<? extends NamedObject> ano,
            FieldFunction ff, Units u, boolean vo) {
        if (_isFieldFunctionOnlyValidFor2D(ff)) {
            sr.setRepresentation(_get.geometries.representation());
        }
        if (ff != null) {
            sr.setFieldFunction(ff);
        }
        if (u == null) {
            u = _ud.unit_Dimensionless;
        }
        sr.setUnits(u);
        sr.getParts().setObjects(ano);
        _io.say.created(sr, vo);
        return sr;
    }

    private void _setTitle(Axis axis, String title) {
        if (title == null) {
            return;
        }
        ((Cartesian2DAxis) axis).getTitle().setText(title);
    }

}
