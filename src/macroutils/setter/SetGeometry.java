package macroutils.setter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for setting Geometry parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetGeometry {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetGeometry(MacroUtils m) {
        _mu = m;
    }

    /**
     * Combine several Part Surfaces.
     *
     * @param aps given Part Surfaces. Make sure they all belong to the same Part.
     * @param vo given verbose option. False will not print anything.
     * @return The combined Part Surface.
     */
    public PartSurface combinePartSurfaces(ArrayList<PartSurface> aps, boolean vo) {
        _io.say.action("Combining Part Surfaces", vo);
        _io.say.objects(aps, "Part Surfaces", vo);
        if (aps.size() == 1) {
            _io.say.msg("Nothing to combine.", vo);
            return aps.get(0);
        }
        GeometryPart gp = aps.get(0).getPart();
        //-- Combine faces
        if (gp instanceof CadPart) {
            ((CadPart) gp).combinePartSurfaces(aps);
        } else if (gp instanceof SimpleBlockPart) {
            ((SimpleBlockPart) gp).combinePartSurfaces(aps);
        } else if (gp instanceof SimpleCylinderPart) {
            ((SimpleCylinderPart) gp).combinePartSurfaces(aps);
        } else if (gp instanceof SolidModelPart) {
            ((SolidModelPart) gp).combinePartSurfaces(aps);
            //-- Leave Leaf Part for last.
        } else if (gp instanceof LeafMeshPart) {
            ((LeafMeshPart) gp).combinePartSurfaces(aps);
        }
        PartSurface ps = aps.get(0);
        _io.say.value("Combined into Part Surface", ps.getPresentationName(), true, vo);
        _io.say.ok(vo);
        return ps;
    }

    /**
     * Splits Part Surfaces by Part Curves from a given Part.
     *
     * @param gp given GeometryPart.
     * @param vo given verbose option. False will not print anything.
     */
    public void splitPartSurfacesByPartCurves(GeometryPart gp, boolean vo) {
        splitPartSurfacesByPartCurves(new ArrayList(gp.getPartSurfaces()), new ArrayList(gp.getPartCurves()), vo);
    }

    /**
     * Splits the given Part Surfaces by the given Part Curves.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @param apc given ArrayList of Part Curves.
     * @param vo given verbose option. False will not print anything.
     */
    public void splitPartSurfacesByPartCurves(ArrayList<PartSurface> aps, ArrayList<PartCurve> apc, boolean vo) {
        _io.say.action("Splitting Part Surfaces by Part Curves", vo);
        _io.say.objects(aps, "Part Surfaces", vo);
        _io.say.objects(apc, "Part Curves", vo);
        if (!(_chk.is.withinSamePart(new ArrayList(aps)) && _chk.is.withinSamePart(new ArrayList(apc)))) {
            _io.say.msg(vo, "Objects do not share the same Geometry Part. Returning NULL!");
            return;
        }
        for (PartSurface ps : aps) {
            _get.partSurfaces.manager(ps).splitPartSurfacesByPartCurves(new NeoObjectVector(aps.toArray()),
                    new NeoObjectVector(apc.toArray()));
        }
        _io.say.ok(vo);
    }

    /**
     * Splits a Part Surface by a given angle.
     *
     * @param ps given Part Surface.
     * @param angle given Split Angle.
     * @param vo given verbose option. False will not print anything.
     */
    public void splitPartSurfaceByAngle(PartSurface ps, double angle, boolean vo) {
        splitPartSurfacesByAngle(_get.objects.arrayList(ps), angle, true);
    }

    /**
     * Splits the given Part Surfaces by an angle.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @param angle given Split Angle.
     * @param vo given verbose option. False will not print anything.
     */
    public void splitPartSurfacesByAngle(ArrayList<PartSurface> aps, double angle, boolean vo) {
        _io.say.action("Splitting Part Surfaces by Angle", vo);
        _io.say.objects(aps, "Part Surfaces", vo);
        _io.say.msg(vo, "Split Angle: %g.", angle);
        for (PartSurface ps : aps) {
            _get.partSurfaces.manager(ps).splitPartSurfacesByAngle(new NeoObjectVector(aps.toArray()), angle);
        }
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
