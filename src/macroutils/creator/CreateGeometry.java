package macroutils.creator;

import java.io.*;
import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for creating Geometry with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateGeometry {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateGeometry(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a Simple Block based on the relative dimensions of the given Part Surfaces. This method collects Minimums
     * and Maximums X, Y and Z and then computes Deltas to be used for creating a block based on the extents of the
     * supplied Part Surfaces.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @param crs1 given 3-components array, relative to the collection. E.g.: {-1, 0, 0.5} will create the following
     * coordinates: [minX - 1 * dX, minY, minZ + 0.5 * dZ].
     * @param crs2 given 3-components array, relative to the collection. E.g.: {1, 2, 3} will create the following
     * coordinates: [maxX + 1 * dX, maxY + 2 * dY, maxZ + 3 * dX].
     * @return The brand new Block Part.
     */
    public SimpleBlockPart block(ArrayList<PartSurface> aps, double[] crs1, double[] crs2) {
        DoubleVector dvExtents = _get.partSurfaces.extents(aps);
        double minX = dvExtents.get(0);
        double maxX = dvExtents.get(1);
        double minY = dvExtents.get(2);
        double maxY = dvExtents.get(3);
        double minZ = dvExtents.get(4);
        double maxZ = dvExtents.get(5);
        double dx = dvExtents.get(6);
        double dy = dvExtents.get(7);
        double dz = dvExtents.get(8);
        double[] c1 = new double[]{minX + crs1[0] * dx, minY + crs1[1] * dy, minZ + crs1[2] * dz};
        double[] c2 = new double[]{maxX + crs2[0] * dx, maxY + crs2[1] * dy, maxZ + crs2[2] * dz};
        return block(c1, c2, _ud.defUnitLength);
    }

    /**
     * Creates a Simple Block with the default Tessellation option. See {@link UserDeclarations#defTessOpt}.
     *
     * @param coord1 given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
     * @param coord2 given 3-components array. <i>E.g.: new double[] {1, 1, 1}</i>
     * @param u given units.
     * @return The SimpleBlockPart.
     */
    public SimpleBlockPart block(double[] coord1, double[] coord2, Units u) {
        _io.say.action("Creating a Simple Block Part", true);
        MeshPartFactory mpf = _sim.get(MeshPartFactory.class);
        SimpleBlockPart sbp = mpf.createNewBlockPart(_sim.get(SimulationPartManager.class));
        sbp.setCoordinateSystem(_ud.lab0);
        sbp.getCorner1().setCoordinate(u, u, u, new DoubleVector(coord1));
        sbp.getCorner2().setCoordinate(u, u, u, new DoubleVector(coord2));
        sbp.getTessellationDensityOption().setSelected(_ud.defTessOpt.getType());
        _io.say.created(sbp, true);
        return sbp;
    }

    /**
     * Creates a Block/Channel 3D-CAD model and creates a Part with 6 Part Surfaces inside, i.e., x0, x1, y0, y1, z0 and
     * z1. The default Tessellation option is used. See {@link UserDeclarations#defTessOpt}.
     *
     * <p>
     * This method exactly the one defined on {@link macroutils.templates.TemplateGeometry#block}.
     *
     * @param c1 given 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param c2 given 3-components array with coordinates. E.g.: {1, 1, 1}.
     * @param u given Units.
     * @return The Cad Body.
     */
    public CadPart block3DCAD(double[] c1, double[] c2, Units u) {
        Body bd = _tmpl.geometry.block(c1, c2, u, "Block", true);
        return _get.geometries.cadPart(bd, false);
    }

    /**
     * Creates a Cylinder using the 3D-CAD model and creates a Part using the default Tessellation option. See
     * {@link UserDeclarations#defTessOpt}.
     *
     * <p>
     * This method exactly the one defined on {@link macroutils.templates.TemplateGeometry#cylinder}.
     *
     * @param r given Radius.
     * @param l given Length.
     * @param org given origin as a 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param u given Units.
     * @param ax given extrusion direction. See {@link StaticDeclarations.Axis} for options.
     * @return The Cad Body.
     */
    public CadPart cylinder3DCAD(double r, double l, double[] org, Units u, StaticDeclarations.Axis ax) {
        Body bd = _tmpl.geometry.cylinder(r, l, org, u, ax, "Cylinder", true);
        return _get.geometries.cadPart(bd, false);
    }

    /**
     * Find Part/Part contacts within a given tolerance specified in the default units. See
     * {@link UserDeclarations#defUnitLength}.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param tol given tolerance in {@link UserDeclarations#defUnitLength} unit.
     */
    public void contacts(ArrayList<GeometryPart> agp, double tol) {
        _io.say.action("Finding Part/Part Contacts", true);
        PartRepresentation pr = _get.geometries.representation();
        NeoObjectVector nov = new NeoObjectVector(agp.toArray());
        double tol_m = tol * _ud.defUnitLength.getConversion();
        _io.say.value("Tolerance", tol_m, _ud.unit_m, true);
        pr.findPartPartContacts(new NeoObjectVector(agp.toArray()), tol_m);
        _io.say.ok(true);
    }

    /**
     * Imports a CAD file using the default Tessellation option. See {@link UserDeclarations#defTessOpt}. It assumes the
     * file is inside {@link UserDeclarations#simPath}. Informing the Path might be necessary.
     *
     * @param part given CAD file with extension. E.g.: "CAD\\machine.prt"
     */
    public void importPart(String part) {
        importPart(new File(_ud.simPath, part));
    }

    /**
     * Imports a CAD file using the default Tessellation option. See {@link UserDeclarations#defTessOpt}.
     *
     * @param cadFile given File in {@link java.io.File} format.
     */
    public void importPart(File cadFile) {
        _io.say.action("Importing CAD Part", true);
        _io.say.value("File", cadFile.toString(), true, true);
        if (!cadFile.exists()) {
            _io.say.msg("File not found!");
        }
        String sfn = cadFile.toString();
        PartImportManager pim = _sim.get(PartImportManager.class);
        if (_get.strings.fileExtension(sfn).toLowerCase().equals("dbs")) {
            pim.importDbsPart(sfn, "OneSurfacePerPatch", "OnePartPerFile", true, _ud.unit_m, 1);
        } else {
            pim.importCadPart(sfn, "SharpEdges", _ud.mshSharpEdgeAngle, _ud.defTessOpt.getValue(), false, false);
        }
        _io.say.ok(true);
    }

    /**
     * Creates a Simple Sphere Part with the default Tessellation option. See {@link UserDeclarations#defTessOpt}. The
     * origin is located in the Centroid of the given Part Surfaces.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @param relSize the radius of sphere is given relative to the max(dx, dy, dz). E.g.: 5, is equivalent to 5 *
     * max(dx, dy, dz).
     * @return The SimpleSpherePart.
     */
    public SimpleSpherePart sphere(ArrayList<PartSurface> aps, double relSize) {
        DoubleVector dvExtents = _get.partSurfaces.extents(aps);
        double minX = dvExtents.get(0);
        double minY = dvExtents.get(2);
        double minZ = dvExtents.get(4);
        double dx = dvExtents.get(6);
        double dy = dvExtents.get(7);
        double dz = dvExtents.get(8);
        double[] coord = new double[]{minX + 0.5 * dx, minY + 0.5 * dy, minZ + 0.5 * dz};
        double radius = relSize * Math.max(Math.max(dx, dy), dz);
        return sphere(coord, radius, _ud.defUnitLength);
    }

    /**
     * Creates a Simple Sphere Part with the default Tessellation option. See {@link UserDeclarations#defTessOpt}.
     *
     * @param coord given 3-components array. <i>E.g.: new double[] {0, 0, 0}</i>
     * @param r given radius.
     * @param u given unit.
     * @return The SimpleSpherePart.
     */
    public SimpleSpherePart sphere(double[] coord, double r, Units u) {
        MeshPartFactory mpf = _sim.get(MeshPartFactory.class);
        SimpleSpherePart ssp = mpf.createNewSpherePart(_sim.get(SimulationPartManager.class));
        LabCoordinateSystem labCSYS = _sim.getCoordinateSystemManager().getLabCoordinateSystem();
        ssp.setCoordinateSystem(labCSYS);
        ssp.getOrigin().setCoordinate(u, u, u, new DoubleVector(coord));
        _set.object.physicalQuantity(ssp.getRadius(), r, u, "Radius", true);
        ssp.getTessellationDensityOption().setSelected(_ud.defTessOpt.getType());
        return ssp;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
        _tmpl = _mu.templates;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.templates.MainTemplates _tmpl = null;
    private macroutils.io.MainIO _io = null;

}
