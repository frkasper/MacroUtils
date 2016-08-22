package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;

/**
 * Low-level class for getting Geometry entities in general with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetGeometries {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetGeometries(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Gets all Geometry Parts from the model.
     *
     * @param vo given verbose option. False will not print anything.
     * @return An ArrayList with Geometry Parts.
     */
    public ArrayList<GeometryPart> all(boolean vo) {
        _io.say.msg(vo, "Getting all Leaf Parts...");
        ArrayList<GeometryPart> agp = new ArrayList(_sim.get(SimulationPartManager.class).getLeafParts());
        _io.say.msg(vo, "Leaf Parts found: %d", agp.size());
        return agp;
    }

    /**
     * Gets the Geometry Part that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The Geometry Part.
     */
    public GeometryPart byREGEX(String regexPatt, boolean vo) {
        return (GeometryPart) _get.objects.allByREGEX(regexPatt, "Geometry Part", new ArrayList(all(false)), vo).get(0);
    }

    /**
     * Gets a single CAD Part from a single 3D-CAD body.
     *
     * @param bd given 3D-CAD body.
     * @param vo given verbose option. False will not print anything.
     * @return CadPart.
     */
    public CadPart cadPart(Body bd, boolean vo) {
        return (CadPart) byREGEX(bd.getPresentationName(), vo);
    }

    /**
     * Gets the Geometry Parts contained in the given Part Surfaces.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @return An ArrayList with Geometry Parts.
     */
    public ArrayList<GeometryPart> fromPartSurfaces(ArrayList<PartSurface> aps) {
        ArrayList<GeometryPart> agp = new ArrayList();
        for (PartSurface ps : aps) {
            if (agp.contains(ps.getPart())) {
                continue;
            }
            agp.add(ps.getPart());
        }
        return agp;
    }

    /**
     * Gets the Geometry Representation.
     *
     * @return The PartRepresentation.
     */
    public PartRepresentation representation() {
        return (PartRepresentation) _sim.getRepresentationManager().getObject("Geometry");
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _get = _mu.get;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;

}
