package macroutils.templates;

import macroutils.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for some templated ready to go meshes with MacroUtils.
 *
 * @since October of 2016
 * @author Fabio Kasper
 */
public class TemplateMesh {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public TemplateMesh(MacroUtils m) {
        _mu = m;
    }

    /**
     * This method will do the following:
     * <ol>
     * <li> Creates a Block Part with 6 Part Surfaces inside, i.e., x0, x1, y0, y1, z0 and z1. The default Tessellation
     * option is used. See {@link UserDeclarations#defTessOpt};
     * <li> Assigns the Part to a Region;
     * <li> Creates a Directed Mesh Operation on the Block.
     * </ol>
     *
     * @param c1 given 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param c2 given 3-components array with coordinates. E.g.: {1, 1, 1}.
     * @param u given Units.
     * @param nCells given 3-components array with Directed Mesher cells in X, Y and Z, respectively. E.g.: {10, 5, 4}.
     * @param name given name for the Part and Region.
     * @return The generated Region.
     */
    public Region hexaBlock(double[] c1, double[] c2, Units u, int[] nCells, String name) {
        Body b = _templ.geometry.block(c1, c2, u, name, true);
        GeometryPart gp = _get.geometries.cadPart(b, false);
        PartSurface src = _get.partSurfaces.byREGEX(gp, "x0", false);
        PartSurface tgt = _get.partSurfaces.byREGEX(gp, "x1", false);
        if (_badgeFor2D) {
            _add.meshOperation.badgeFor2D(_get.objects.arrayList(gp));
        }
        Region reg = _add.region.fromPart(gp, StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT, StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        _add.meshOperation.directedMeshing_Channel(src, tgt, nCells[0], nCells[1], nCells[2]).execute();
        return reg;
    }

    /**
     * This method will do the following:
     * <ol>
     * <li> Creates a Cylinder Part with 3 Part Surfaces inside, i.e., x0, x1, and Default, assuming the extrusion
     * happened in the X axis. The default Tessellation option is used. See {@link UserDeclarations#defTessOpt};
     * <li> Assigns the Part to a Region;
     * <li> Creates a Directed Mesh Operation on the Cylinder using an O-Grid topology.
     * </ol>
     *
     * @param r given Radius.
     * @param l given Length.
     * @param org given origin as a 3-components array with coordinates. E.g.: {0, -1, -10}.
     * @param u given Units.
     * @param ax given extrusion direction. See {@link macroutils.StaticDeclarations.Axis} for options.
     * @param nCells given 3-components array with Directed Mesher cells in Theta, Radius and Length, respectively.
     * E.g.: {5, 10, 20}.
     * @param rR given r/R distance for the O-Grid. E.x.: 0.5;
     * @param name given name for the Part and Region.
     * @param vo given verbose option. False will not print anything.
     * @return The generated Region.
     */
    public Region hexaCylinder(double r, double l, double[] org, Units u, StaticDeclarations.Axis ax,
            int[] nCells, double rR, String name, boolean vo) {
        Body b = _templ.geometry.cylinder(r, l, org, u, ax, name, true);
        GeometryPart gp = _get.geometries.cadPart(b, false);
        PartSurface src = _get.partSurfaces.byREGEX(gp, ".0", false);
        PartSurface tgt = _get.partSurfaces.byREGEX(gp, ".1", false);
        Region reg = _add.region.fromPart(gp, StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT, StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        if (_badgeFor2D) {
            _add.meshOperation.badgeFor2D(_get.objects.arrayList(gp));
        }
        CylindricalCoordinateSystem ccs = _add.tools.coordinateSystem_Cylindrical(ax);
        _add.meshOperation.directedMeshing_Pipe(src, tgt, nCells[0], nCells[1], nCells[2], rR, ccs).execute();
        return reg;
    }

    /**
     * When using this template, use this option to Badge it as 2D before generating the mesh. Default = false.
     *
     * @param opt given option.
     */
    public void setBadgeFor2D(boolean opt) {
        _badgeFor2D = opt;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _add = _mu.add;
        _get = _mu.get;
        _templ = _mu.templates;
    }

    //--
    //-- Variables declaration area.
    //--
    private boolean _badgeFor2D = false;
    private MacroUtils _mu = null;
    private macroutils.creator.MainCreator _add = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.templates.MainTemplates _templ = null;

}
