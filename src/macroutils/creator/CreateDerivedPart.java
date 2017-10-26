package macroutils.creator;

import java.util.ArrayList;
import java.util.Vector;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.base.neo.NeoObjectVector;
import star.base.report.FieldMeanMonitor;
import star.common.Boundary;
import star.common.FieldFunction;
import star.common.GeometryPart;
import star.common.PartSurface;
import star.common.Region;
import star.common.Simulation;
import star.common.Units;
import star.common.UpdateEvent;
import star.vis.CellSurfacePart;
import star.vis.ImplicitPart;
import star.vis.IsoMode;
import star.vis.IsoPart;
import star.vis.LinePart;
import star.vis.PlaneSection;
import star.vis.PointPart;
import star.vis.StreamPart;

/**
 * Low-level class for creating Derived Parts with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateDerivedPart {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateDerivedPart(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private PlaneSection _createPlane(double[] origin, double[] normal, String axis) {
        PlaneSection pln = _sectionPlane(new ArrayList<>(_get.regions.all(false)), origin, normal, axis);
        pln.setPresentationName("Plane " + axis);
        return pln;
    }

    private void _creating(ArrayList<NamedObject> ano, String what) {
        _io.say.action("Creating a " + what, true);
        if (ano == null) {
            return;
        }
        _io.say.objects(ano, "Parts", true);
    }

    private PlaneSection _sectionPlane(ArrayList<NamedObject> ano, double[] origin, double[] orientation, String axis) {
        _creating(ano, "Section Plane");
        if (axis != null) {
            _io.say.msg(true, "Normal to %s direction.", axis);
        }
        NeoObjectVector where = new NeoObjectVector(ano.toArray());
        DoubleVector vecOrient = new DoubleVector(orientation);
        DoubleVector vecOrigin = new DoubleVector(origin);
        DoubleVector vecOffsets = new DoubleVector(new double[]{0.0});
        ImplicitPart ip = _sim.getPartManager().createImplicitPart(where, vecOrient, vecOrigin, 0, 1, vecOffsets);
        PlaneSection ps = (PlaneSection) ip;
        ps.getOriginCoordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength, vecOrigin);
        _io.say.created(ps, true);
        return ps;
    }

    private StreamPart _streamlinePartSeed(ArrayList<NamedObject> anoIP, ArrayList<NamedObject> anoSP, boolean vo) {
        _io.say.objects(anoIP, "Input Parts", vo);
        _io.say.objects(anoSP, "Seed Parts", vo);
        FieldFunction ff = _get.objects.fieldFunction(StaticDeclarations.Vars.VEL.getVar(), false);
        StreamPart sp = _sim.getPartManager().createStreamPart(new NeoObjectVector(anoIP.toArray()),
                new NeoObjectVector(anoSP.toArray()), ff,
                _ud.postStreamlineResolution, _ud.postStreamlineResolution, 0);
        _io.say.object(sp.getFieldFunction(), true);
        _io.say.created(sp, true);
        return sp;
    }

    /**
     * Creates a Cell Surface for postprocessing.
     *
     * @param ano given ArrayList of NamedObjects.
     * @return The CellSurfacePart.
     */
    public CellSurfacePart cellSurface(ArrayList<NamedObject> ano) {
        _creating(ano, "Cell Surface");
        CellSurfacePart c = _sim.getPartManager().createCellSurfacePart(new Vector<>(ano));
        _io.say.created(c, true);
        return c;
    }

    /**
     * Creates a Field Mean Monitor with the selected Objects.
     *
     * @param ano given ArrayList of NamedObjects.
     * @param ff given Field Function.
     * @param ue given Update Event for triggering the monitor.
     * @return The FieldMeanMonitor.
     */
    public FieldMeanMonitor fieldMeanMonitor(ArrayList<NamedObject> ano, FieldFunction ff, UpdateEvent ue) {
        _creating(ano, "Cell a Field Mean Monitor");
        FieldMeanMonitor fmm = _sim.getMonitorManager().createMonitor(FieldMeanMonitor.class);
        _io.say.object(ff, true);
        fmm.setObjects(ano);
        fmm.setFieldFunction(ff);
        _set.object.updateEvent(fmm, ue, true);
        _io.say.created(fmm, true);
        return fmm;
    }

    /**
     * Creates a single-value Isosurface.
     *
     * @param ano given ArrayList of NamedObjects.
     * @param ff given Field Function.
     * @param val given Isosurface value.
     * @param u given unit.
     * @return The IsoPart.
     */
    public IsoPart isosurface(ArrayList<NamedObject> ano, FieldFunction ff, double val, Units u) {
        _creating(ano, "Isosurface");
        _io.say.object(ff, true);
        IsoPart ip = _sim.getPartManager().createIsoPart(new NeoObjectVector(ano.toArray()), ff);
        ip.setMode(IsoMode.ISOVALUE_SINGLE);
        _set.object.physicalQuantity(ip.getSingleIsoValue().getValueQuantity(), val, u, "Iso Value", true);
        _io.say.created(ip, true);
        return ip;
    }

    /**
     * Creates a Line Probe.
     *
     * @param ano given ArrayList of NamedObjects.
     * @param c1 given coordinates using {@link UserDeclarations#defUnitLength}. E.g.: new double[] {0., 0., 0.}
     * @param c2 given coordinates using {@link UserDeclarations#defUnitLength}. E.g.: new double[] {0., 1., 0.}
     * @param res given resolution for the line, i.e., the number of points.
     * @return The created Line.
     */
    public LinePart line(ArrayList<NamedObject> ano, double[] c1, double[] c2, int res) {
        _creating(ano, "Line Probe");
        NeoObjectVector where = new NeoObjectVector(ano.toArray());
        DoubleVector from = new DoubleVector(c1);
        DoubleVector to = new DoubleVector(c2);
        LinePart lp = _sim.getPartManager().createLinePart(where, from, to, res);
        lp.getPoint1Coordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength, from);
        lp.getPoint2Coordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength, to);
        _io.say.created(lp, true);
        return lp;
    }

    /**
     * Creates a Point Probe.
     *
     * @param ano given ArrayList of NamedObjects.
     * @param c given coordinates using {@link UserDeclarations#defUnitLength}. E.g.: new double[] {0, 0, 0}.
     * @return The PointPart.
     */
    public PointPart point(ArrayList<NamedObject> ano, double[] c) {
        _creating(ano, "Point Probe");
        PointPart pp = _sim.getPartManager().createPointPart(new NeoObjectVector(ano.toArray()), new DoubleVector(c));
        pp.getPointCoordinate().setCoordinate(_ud.defUnitLength, _ud.defUnitLength, _ud.defUnitLength,
                new DoubleVector(c));
        _io.say.created(pp, true);
        return pp;
    }

    /**
     * Creates a Section Plane using All Regions.
     *
     * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
     * @param orientation given normal orientation coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
     * @return The PlaneSection.
     */
    public PlaneSection sectionPlane(double[] origin, double[] orientation) {
        return _sectionPlane(new ArrayList<>(_get.regions.all(false)), origin, orientation, null);
    }

    /**
     * Creates a Section Plane with based on a list of objects, which can be Parts, Regions, Boundaries, etc...
     *
     * @param ano given ArrayList of NamedObjects.
     * @param origin given coordinates using {@link UserDeclarations#defUnitLength}. E.g.: new double[] {0., 0., 0.}
     * @param orientation given coordinates. E.g.: Normal to X is new double[] {1., 0., 0.}
     * @return The PlaneSection.
     */
    public PlaneSection sectionPlane(ArrayList<NamedObject> ano, double[] origin, double[] orientation) {
        return _sectionPlane(ano, origin, orientation, null);
    }

    /**
     * Creates a Section Plane Normal to X direction using All Regions.
     *
     * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
     * @return The PlaneSection.
     */
    public PlaneSection sectionPlaneX(double[] origin) {
        return _createPlane(origin, _X, "X");
    }

    /**
     * Creates a Section Plane Normal to Y direction using All Regions.
     *
     * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
     * @return The PlaneSection.
     */
    public PlaneSection sectionPlaneY(double[] origin) {
        return _createPlane(origin, _Y, "Y");
    }

    /**
     * Creates a Section Plane Normal to Z direction using All Regions.
     *
     * @param origin given origin coordinates. E.g.: new double[] {0., 0., 0.}
     * @return The PlaneSection.
     */
    public PlaneSection sectionPlaneZ(double[] origin) {
        return _createPlane(origin, _Z, "Z");
    }

    /**
     * Creates a Part Seed type Streamline with the given Objects, such as:
     * <ul>
     * <li> Objects that are 3D in Space will be assigned as Input Parts. E.g.: Regions or Parts;
     * <li> Objects that are 2D in Space will be assigned as Seed Parts. E.g.: Boundaries or Part Surfaces;
     * <li> The Streamline will be based on the Velocity field;
     * </ul>
     *
     * @param ano given ArrayList of NamedObjects.
     * @return The StreamPart.
     */
    public StreamPart streamline_PartSeed(ArrayList<NamedObject> ano) {
        _creating(null, "Streamline Derived Part");
        ArrayList<NamedObject> a3d = new ArrayList<>();
        ArrayList<NamedObject> a2d = new ArrayList<>();
        for (NamedObject no : ano) {
            String info = _get.strings.information(no);
            if ((no instanceof Region) || (no instanceof GeometryPart)) {
                _io.say.value(info, "added as a 3D object", false, true);
                a3d.add(no);
            } else if ((no instanceof Boundary) || (no instanceof PartSurface)) {
                _io.say.value(info, "added as a 2D object", false, true);
                a2d.add(no);
            } else {
                _io.say.value("Warning! Object is not 3D or 2D to be used for Streamline",
                        no.getBeanDisplayName(), true, true);
            }
        }
        return _streamlinePartSeed(a3d, a2d, false);
    }

    /**
     * Creates a Part Seed type Streamline on all Regions based on a list of objects.
     *
     * @param anoIP given ArrayList of NamedObjects for the Input Parts. E.g.: Regions or Parts.
     * @param anoSP given ArrayList of NamedObjects for the Seed Parts. E.g.: Part Surfaces or Boundaries.
     * @return The StreamPart.
     */
    public StreamPart streamline_PartSeed(ArrayList<NamedObject> anoIP, ArrayList<NamedObject> anoSP) {
        _creating(null, "Streamline Derived Part");
        return _streamlinePartSeed(anoIP, anoSP, true);
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

    //--
    //-- Variables declaration area.
    //--
    private final double[] _X = new double[]{1., 0., 0.};
    private final double[] _Y = new double[]{0., 1., 0.};
    private final double[] _Z = new double[]{0., 0., 1.};

    private MacroUtils _mu = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;
    private Simulation _sim = null;

}
