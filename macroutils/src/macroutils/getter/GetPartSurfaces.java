package macroutils.getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NeoProperty;
import star.common.Boundary;
import star.common.GeometryPart;
import star.common.PartSurface;
import star.common.PartSurfaceManager;
import star.common.Simulation;
import star.meshing.CadPart;
import star.meshing.LeafMeshPart;
import star.meshing.MeshOperationPart;
import star.meshing.PartSurfaceMeshWidget;
import star.meshing.SimpleBlockPart;
import star.meshing.SimpleCylinderPart;
import star.meshing.SurfaceMeshWidgetDisplayController;
import star.meshing.SurfaceMeshWidgetQueryController;
import star.meshing.SurfaceMeshWidgetSelectController;
import star.vis.Scene;

/**
 * Low-level class for getting Part Surfaces with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetPartSurfaces {

    private macroutils.checker.MainChecker _chk = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetPartSurfaces(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Part Surfaces from all Geometries available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> all(boolean vo) {
        ArrayList<PartSurface> aps = new ArrayList<>();
        _get.geometries.all(false).forEach(gp -> aps.addAll(all(gp, false)));
        _io.say.objects(aps, "Getting all Part Surfaces from all Geometries", vo);
        return aps;
    }

    /**
     * Gets all Part Surfaces from the given Geometry Part.
     *
     * @param gp given GeometryPart.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> all(GeometryPart gp, boolean vo) {
        ArrayList<PartSurface> aps = new ArrayList<>(gp.getPartSurfaces());
        _io.say.objects(aps, "Getting all Part Surfaces", vo);
        return aps;
    }

    /**
     * Gets all Part Surfaces that matches the REGEX search pattern from all Geometries available in
     * the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> allByREGEX(String regexPatt, boolean vo) {
        return _get.objects.allByREGEX(regexPatt, "all Part Surfaces", all(false), vo);
    }

    /**
     * Gets all Part Surfaces that matches the REGEX search pattern from the Part Surfaces available
     * in the Geometry Part.
     *
     * @param gp        given GeometryPart.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return An ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> allByREGEX(GeometryPart gp, String regexPatt, boolean vo) {
        return _get.objects.allByREGEX(regexPatt, "all Part Surfaces", all(gp, false), vo);
    }

    /**
     * Gets the Part Surface that has the maximum area.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @return The PartSurface that has the maximum area among the given ones.
     */
    public PartSurface byAreaMax(ArrayList<PartSurface> aps) {
        return byArea(aps, RangeOpts.MAX);
    }

    /**
     * Gets the Part Surface that has the minimum area.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @return The PartSurface that has the minimum area among the given ones.
     */
    public PartSurface byAreaMin(ArrayList<PartSurface> aps) {
        return byArea(aps, RangeOpts.MIN);
    }

    /**
     * Gets a Part Surface that matches the REGEX search pattern among all Part Surfaces available
     * in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The PartSurface. Null if nothing is found.
     */
    public PartSurface byREGEX(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt, "Part Surface", all(false), vo);
    }

    /**
     * Gets a Part Surface that matches the REGEX search pattern from the Part Surfaces available in
     * the Geometry Part.
     *
     * @param gp        given GeometryPart.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The PartSurface. Null if nothing is found.
     */
    public PartSurface byREGEX(GeometryPart gp, String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt, "Part Surface", all(gp, false), vo);
    }

    /**
     * Gets a Part Surface based on a Geometric Range of the Part Surfaces provided.
     *
     * @param aps  given ArrayList of Part Surfaces.
     * @param axis what will be queried? See {@link macroutils.StaticDeclarations.Axis} for options.
     * @param tol  given absolute tolerance for searching in default units
     *             ({@link UserDeclarations#defUnitLength}).
     * @return The PartSurface that has the maximum axis within the given tolerance.
     */
    public PartSurface byRangeMax(ArrayList<PartSurface> aps, StaticDeclarations.Axis axis,
            double tol) {
        return byRange(aps, RangeOpts.MAX, axis, tol);
    }

    /**
     * Gets a Part Surface based on a Geometric Range of the Part Surfaces provided.
     *
     * @param aps  given ArrayList of Part Surfaces.
     * @param axis what will be queried? See {@link macroutils.StaticDeclarations.Axis} for options.
     * @param tol  given absolute tolerance for searching in default units
     *             ({@link UserDeclarations#defUnitLength}).
     * @return The PartSurface that has the minimum axis within the given tolerance.
     */
    public PartSurface byRangeMin(ArrayList<PartSurface> aps, StaticDeclarations.Axis axis,
            double tol) {
        return byRange(aps, RangeOpts.MIN, axis, tol);
    }

    /**
     * Get the Geometric Range from a ArrayList of Part Surfaces. Note that the resulting output
     * will be given in default length units. See {@link UserDeclarations#defUnitLength}.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @return A DoubleVector with 9 components in the following order (minX, maxX, minY, maxY,
     *         minZ, maxZ, dX, dY, dZ).
     */
    public DoubleVector extents(ArrayList<PartSurface> aps) {
        HashMap<PartSurface, NeoProperty> stats = statistics(aps);
        _io.say.action("Getting Part Surfaces Extents", true);
        DoubleVector dv = new DoubleVector();
        DoubleVector globalMin = stats.get(null).getDoubleVector("LabMinRange");
        DoubleVector globalMax = stats.get(null).getDoubleVector("LabMaxRange");
        DoubleVector globalDelta = stats.get(null).getDoubleVector("XYZComponents");
        //--
        //-- Add Order == { x0, x1, y0, y1, z0, z1, dx, dy, dz }
        //--
        for (int i = 0; i < globalMin.size(); i++) {
            dv.add(globalMin.get(i) / _ud.defUnitLength.getConversion());
            dv.add(globalMax.get(i) / _ud.defUnitLength.getConversion());
        }
        //-- Add Deltas
        for (int i = 0; i < globalMin.size(); i++) {
            dv.add(globalDelta.get(i) / _ud.defUnitLength.getConversion());
        }
        _io.say.msg(true, "Extents: x0, x1, y0, y1, z0, z1, dx, dy, dz [%s]",
                _get.strings.fromUnit(_ud.defUnitLength));
        _io.say.msg(true, dv.toString());
        _io.say.ok(true);
        return dv;
    }

    /**
     * Gets all Part Surfaces associated with Boundaries.
     *
     * @param ab given ArrayList of Boundaries.
     * @return The ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> fromBoundaries(ArrayList<Boundary> ab) {
        _io.say.action("Getting Part Surfaces", true);
        _io.say.objects(ab, "Boundaries", true);
        ArrayList<PartSurface> aps = new ArrayList<>();
        ab.forEach(b -> aps.addAll(b.getPartSurfaceGroup().getObjects()));
        _io.say.objects(aps, "Part Surfaces", true);
        _io.say.ok(true);
        return aps;
    }

    /**
     * Gets all Part Surfaces associated with a Boundary.
     *
     * @param b given Boundary.
     * @return The ArrayList of Part Surfaces.
     */
    public ArrayList<PartSurface> fromBoundary(Boundary b) {
        return fromBoundaries(_get.objects.arrayList(b));
    }

    /**
     * Gets its manager object from a Part Surface, if applicable.
     *
     * @param ps given Part Surface.
     * @return The PartSurfaceManager. Null if nothing is found.
     */
    public PartSurfaceManager manager(PartSurface ps) {
        GeometryPart gp = ps.getPart();
        if (gp instanceof CadPart) {
            return ((CadPart) gp).getPartSurfaceManager();
        }
        if (gp instanceof SimpleBlockPart) {
            return ((SimpleBlockPart) gp).getPartSurfaceManager();
        }
        if (gp instanceof SimpleCylinderPart) {
            return ((SimpleCylinderPart) gp).getPartSurfaceManager();
        }
        if (gp instanceof MeshOperationPart) {
            return ((MeshOperationPart) gp).getPartSurfaceManager();
        }
        //-- Leave Leaf for the last.
        if (gp instanceof LeafMeshPart) {
            return ((LeafMeshPart) gp).getPartSurfaceManager();
        }
        return null;
    }

    /**
     * Gets global and local statistics on the given Part Surfaces. This method is useful for
     * querying the extents of the PartSurfaces as well area. The item with a NULL item means the
     * global stats for all of them.
     *
     * @param aps the given ArrayList of Part Surfaces.
     * @return a {@link java.util.HashMap} containing properties and values.
     */
    public HashMap<PartSurface, NeoProperty> statistics(ArrayList<PartSurface> aps) {
        HashMap<PartSurface, NeoProperty> hms = new HashMap<>();
        if (aps.isEmpty()) {
            _io.say.msg("No Part Surfaces Provided for Querying. Returning NULL HashMap!");
            return null;
        }
        ArrayList<GeometryPart> agp = _get.geometries.fromPartSurfaces(aps);
        _io.say.action("Querying Part Surfaces Statistics", true);
        //-- Init Widget
        Scene scn = _sim.getSceneManager().createScene("Repair Surface");
        scn.initialize();
        PartSurfaceMeshWidget psmw = _get.geometries.representation().startSurfaceMeshWidget(scn);
        psmw.setActiveParts(agp, _get.geometries.rootDescriptionSource());
        psmw.startSurfaceRepairControllers();
        scn.setAdvancedRenderingEnabled(false);
        psmw.getControllers().getController(SurfaceMeshWidgetDisplayController.class)
                .showAllFaces();
        scn.open();
        SurfaceMeshWidgetSelectController smwsc = psmw.getControllers()
                .getController(SurfaceMeshWidgetSelectController.class);
        smwsc.selectPartSurfaces(aps);
        SurfaceMeshWidgetQueryController smwqc = psmw.getControllers()
                .getController(SurfaceMeshWidgetQueryController.class);
        //-- Global Stats
        _io.say.msg(true, "Querying Global Stats on %d Geometry Part(s)...", agp.size());
        _io.say.objects(aps, "Part Surfaces", true);
        NeoProperty npG = smwqc.queryFaceGeometricRange();
        npG.put("TotalFaceArea", smwqc.queryFaceArea().getDouble("TotalFaceArea"));
        hms.put(null, npG);
        _io.say.line(true);
        //-- Local Stats
        for (PartSurface ps : aps) {
            if (!agp.contains(ps.getPart())) {
                continue;
            }
            if (!aps.contains(ps)) {
                continue;
            }
            smwsc.clearSelected();
            smwsc.selectPartSurface(ps);
            _io.say.msg(true, "Querying Stats for a Single Part Surface...");
            _io.say.object(ps, true);
            NeoProperty npL = smwqc.queryFaceGeometricRange();
            npL.put("TotalFaceArea", smwqc.queryFaceArea().getDouble("TotalFaceArea"));
            hms.put(ps, npL);
            _io.say.line(true);
        }
        psmw.stop();
        _sim.getSceneManager().deleteScene(scn);
        _io.say.msg(true, "Returning a HashMap with %d items...", hms.size());
        return hms;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _ud = _mu.userDeclarations;
    }

    private PartSurface byArea(ArrayList<PartSurface> aps, RangeOpts opt) {
        HashMap<PartSurface, NeoProperty> stats = statistics(aps);
        //-- Remove total data from the HashMap.
        stats.remove(null);
        _io.say.action(String.format("Getting Part Surface with %s Area", opt.toString()), true);
        _io.say.objects(aps, "Part Surfaces", true);
        HashMap<PartSurface, Double> areas = new HashMap<>();
        for (Map.Entry<PartSurface, NeoProperty> entry : stats.entrySet()) {
            PartSurface ps = entry.getKey();
            NeoProperty np = entry.getValue();
            areas.put(ps, np.getDouble("TotalFaceArea"));
        }
        double minA = StaticDeclarations.BIG_NUMBER, maxA = -1 * StaticDeclarations.BIG_NUMBER;
        PartSurface minPS = null, maxPS = null;
        for (Map.Entry<PartSurface, Double> entry : areas.entrySet()) {
            PartSurface ps = entry.getKey();
            Double area = entry.getValue();
            if (area < minA) {
                minA = area;
                minPS = ps;
            }
            if (area > maxA) {
                maxA = area;
                maxPS = ps;
            }
        }
        double retA = minA;
        PartSurface retPS = minPS;
        switch (opt) {
            case MAX:
                retA = maxA;
                retPS = maxPS;
                break;
            case MIN:
                break;
        }
        _io.say.msg(true, "%s Area found: %g [m^2].", opt.toString(), retA);
        _io.say.object(retPS, true);
        _io.say.ok(true);
        return retPS;
    }

    /**
     * @param tol given Tolerance in meters.
     */
    private ArrayList<PartSurface> byRange(ArrayList<PartSurface> aps, RangeOpts opt,
            StaticDeclarations.Axis axis, double tol, boolean vo) {
        HashMap<PartSurface, NeoProperty> stats = statistics(aps);
        _io.say.action("Getting Part Surfaces Range", vo);
        DoubleVector globalMin = stats.get(null).getDoubleVector("LabMinRange");
        DoubleVector globalMax = stats.get(null).getDoubleVector("LabMaxRange");
        ArrayList<PartSurface> aps2 = new ArrayList<>();
        for (PartSurface ps : aps) {
            DoubleVector localMin = stats.get(ps).getDoubleVector("LabMinRange");
            DoubleVector localMax = stats.get(ps).getDoubleVector("LabMaxRange");
            int i = 0;
            switch (axis) {
                case X:
                    break;
                case Y:
                    i = 1;
                    break;
                case Z:
                    i = 2;
                    break;
            }
            boolean isLocal = _chk.is.differenceWithinTolerance(localMin.get(i), localMax.get(i),
                    tol);
            if (isLocal) {
                _io.say.msg(vo, "Found Local %s = %g [m].", axis.toString(), localMin.get(i));
            }
            boolean isGlobal = _chk.is.differenceWithinTolerance(localMin.get(i), globalMin.get(i),
                    tol);
            double val = localMin.get(i);
            switch (opt) {
                case MIN:
                    break;
                case MAX:
                    isGlobal = _chk.is.differenceWithinTolerance(localMax.get(i), globalMax.get(i),
                            tol);
                    val = globalMax.get(i);
                    break;
            }
            if (isLocal && isGlobal) {
                _io.say.msg(vo, "Found Global %s of %s = %g [m].", opt.toString(), axis.toString(),
                        val);
                aps2.add(ps);
            }
        }
        _io.say.line(vo);
        _io.say.msg(vo, "Part Surfaces that matches %s %s:", opt.toString(), axis.toString());
        _io.say.objects(aps2, "Part Surfaces", vo);
        _io.say.line(vo);
        return aps2;
    }

    private PartSurface byRange(ArrayList<PartSurface> aps, RangeOpts opt,
            StaticDeclarations.Axis axis, double tol) {
        PartSurface ps = byRange(aps, opt, axis, tol * _ud.defUnitLength.getConversion(), true)
                .get(0);
        _io.say.value("Found Part Surface", ps.getPresentationName(), true, true);
        return ps;
    }

    private static enum RangeOpts {
        MIN, MAX
    }

}
