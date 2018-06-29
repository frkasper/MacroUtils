package macroutils.getter;

import java.util.ArrayList;
import java.util.stream.Collectors;
import macroutils.MacroUtils;
import star.cadmodeler.Body;
import star.cadmodeler.SolidModelPart;
import star.common.GeometryPart;
import star.common.PartSurface;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.meshing.CadPart;
import star.meshing.PartRepresentation;
import star.meshing.RootDescriptionSource;

/**
 * Low-level class for getting Geometry entities in general with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetGeometries {

    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

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
        ArrayList<GeometryPart> agp = new ArrayList<>(
                _sim.get(SimulationPartManager.class).getLeafParts());
        _io.say.objects(agp, "Getting all Leaf Parts", vo);
        return agp;
    }

    /**
     * Gets the Geometry Part that matches the REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The Geometry Part.
     */
    public GeometryPart byREGEX(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt, "Geometry Part", all(false), vo);
    }

    /**
     * Gets a single CAD Part from a single 3D-CAD body.
     *
     * @param bd given 3D-CAD body.
     * @param vo given verbose option. False will not print anything.
     * @return CadPart.
     */
    public CadPart cadPart(Body bd, boolean vo) {
        return (CadPart) _getSolidModelPart(bd);
    }

    /**
     * Gets the Geometry Parts contained in the given Part Surfaces.
     *
     * @param aps given ArrayList of Part Surfaces.
     * @return An ArrayList with Geometry Parts.
     */
    public ArrayList<GeometryPart> fromPartSurfaces(ArrayList<PartSurface> aps) {
        return new ArrayList<>(
                aps.stream()
                        .map(ps -> ps.getPart())
                        .collect(Collectors.toSet()));
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
     * Get the RootDescriptionSource.
     *
     * @return The RootDescriptionSource.
     */
    public RootDescriptionSource rootDescriptionSource() {
        return (RootDescriptionSource) _get.mesh.descriptionSource("Root");
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

    private SolidModelPart _getSolidModelPart(Body bd) {
        for (GeometryPart gp : all(false)) {
            if (gp instanceof SolidModelPart) {
                SolidModelPart smp = (SolidModelPart) gp;
                if (smp.getCadModel().equals(bd.getModel())) {
                    return smp;
                }
            }
        }
        return null;
    }

}
