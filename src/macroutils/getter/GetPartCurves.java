package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for getting Part Curves with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetPartCurves {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetPartCurves(MacroUtils m) {
        _mu = m;
    }

    /**
     * Gets all Part Curves from all Geometries available in the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Curves.
     */
    public ArrayList<PartCurve> all(boolean vo) {
        ArrayList<PartCurve> apc = new ArrayList<>();
        _io.say.msg(vo, "Getting all Part Curves from all Geometries...");
        for (GeometryPart gp : _get.geometries.all(false)) {
            apc.addAll(gp.getPartCurves());
        }
        _io.say.msg(vo, "Part Curves found: %d", apc.size());
        return apc;
    }

    /**
     * Gets all Part Curves from the given Geometry Part.
     *
     * @param gp given GeometryPart.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Curves.
     */
    public ArrayList<PartCurve> all(GeometryPart gp, boolean vo) {
        return allByREGEX(gp, ".*", vo);
    }

    /**
     * Gets all Part Curves that matches the REGEX search pattern from all Geometries available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Curves.
     */
    public ArrayList<PartCurve> allByREGEX(String regexPatt, boolean vo) {
        return new ArrayList<>(_get.objects.allByREGEX(regexPatt, "all Part Curves", new ArrayList<>(all(false)), true));
    }

    /**
     * Gets all Part Curves that matches the REGEX search pattern from the Part Curves available in the Geometry Part.
     *
     * @param gp given GeometryPart.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList of Part Curves.
     */
    public ArrayList<PartCurve> allByREGEX(GeometryPart gp, String regexPatt, boolean vo) {
        return new ArrayList<>(_get.objects.allByREGEX(regexPatt, "all Part Curves",
                new ArrayList<>(gp.getPartCurves()), true));
    }

    /**
     * Gets a Part Curve that matches the REGEX search pattern among all Part Curves available in the model.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The PartCurve. Null if nothing is found.
     */
    public PartCurve byREGEX(String regexPatt, boolean vo) {
        return (PartCurve) _get.objects.byREGEX(regexPatt, "Part Curve", new ArrayList<>(all(false)), vo);
    }

    /**
     * Gets a Part Curve that matches the REGEX search pattern from the Part Curves available in the Geometry Part.
     *
     * @param gp given GeometryPart.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The PartCurve. Null if nothing is found.
     */
    public PartCurve byREGEX(GeometryPart gp, String regexPatt, boolean vo) {
        return (PartCurve) _get.objects.byREGEX(regexPatt, "Part Curve", new ArrayList<>(gp.getPartCurves()), vo);
    }

    /**
     * Gets its manager object from a Part Curve, if applicable.
     *
     * @param pc given Part Curve.
     * @return The PartCurveManager. Null if nothing is found.
     */
    public PartCurveManager manager(PartCurve pc) {
        GeometryPart gp = pc.getPart();
        if (gp instanceof CadPart) {
            return ((CadPart) gp).getPartCurveManager();
        }
        if (gp instanceof SimpleBlockPart) {
            return ((SimpleBlockPart) gp).getPartCurveManager();
        }
        if (gp instanceof SimpleCylinderPart) {
            return ((SimpleCylinderPart) gp).getPartCurveManager();
        }
        if (gp instanceof MeshOperationPart) {
            return ((MeshOperationPart) gp).getPartCurveManager();
        }
        //-- Leave Leaf for the last.
        if (gp instanceof LeafMeshPart) {
            return ((LeafMeshPart) gp).getPartCurveManager();
        }
        return null;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
