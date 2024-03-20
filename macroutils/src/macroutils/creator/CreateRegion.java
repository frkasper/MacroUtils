package macroutils.creator;

import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import star.common.GeometryPart;
import star.common.Region;
import star.common.RegionManager;
import star.common.Simulation;
import star.common.SimulationPartManager;

/**
 * Low-level class for creating Regions with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateRegion {

    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateRegion(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates a single Region from all Parts available in the model using one Boundary per Part
     * Surface and Contact-mode Interface (if available).
     *
     * @param vo given verbose option. False will not print anything.
     * @return The created Region.
     */
    public Region fromAll(boolean vo) {
        return fromParts(_get.geometries.all(false), StaticDeclarations.RegionMode.ONE,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                true).get(0);
    }

    /**
     * Creates a single Region from the Part provided.
     *
     * @param gp  given GeometryPart.
     * @param bm  given Boundary mode. See {@link macroutils.StaticDeclarations.BoundaryMode} for
     *            options.
     * @param im  given Interface mode. See {@link macroutils.StaticDeclarations.InterfaceMode} for
     *            options.
     * @param vo  given verbose option. False will not print anything.
     * @return The Region.
     */
    public Region fromPart(GeometryPart gp, StaticDeclarations.BoundaryMode bm, 
            StaticDeclarations.InterfaceMode im, boolean vo) {
        return fromParts(_get.objects.arrayList(gp), bm, im, vo);
    }

    /**
     * Creates a single Region from the Parts provided.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param bm  given Boundary mode. See {@link macroutils.StaticDeclarations.BoundaryMode} for
     *            options.
     * @param im  given Interface mode. See {@link macroutils.StaticDeclarations.InterfaceMode} for
     *            options.
     * @param vo  given verbose option. False will not print anything.
     * @return An ArrayList of the created Regions.
     */
    public Region fromParts(ArrayList<GeometryPart> agp, StaticDeclarations.BoundaryMode bm,
            StaticDeclarations.InterfaceMode im, boolean vo) {
        return fromParts(agp, StaticDeclarations.RegionMode.ONE, bm, im, vo).get(0);
    }

    /**
     * Creates a dedicated Region for every Part provided.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param rm  given Region mode. See {@link macroutils.StaticDeclarations.RegionMode} for
     *            options.
     * @param bm  given Boundary mode. See {@link macroutils.StaticDeclarations.BoundaryMode} for
     *            options.
     * @param im  given Interface mode. See {@link macroutils.StaticDeclarations.InterfaceMode} for
     *            options.
     * @param vo  given verbose option. False will not print anything.
     * @return An ArrayList of the created Regions.
     */
    public ArrayList<Region> fromParts(ArrayList<GeometryPart> agp, 
            StaticDeclarations.RegionMode rm, StaticDeclarations.BoundaryMode bm,
            StaticDeclarations.InterfaceMode im, boolean vo) {
        switch (rm) {
            case ONE:
                _io.say.action("Assigning Parts to a Single Region", vo);
                break;
            case ONE_PER_PART:
                _io.say.action("Assigning Parts to Different Regions", vo);
                break;
        }
        if (agp.size() == 1) {
            rm = StaticDeclarations.RegionMode.ONE_PER_PART;
        }
        _io.say.msg(vo, "Boundary Mode: %s", bm.getMode());
        _io.say.objects(agp, "Geometry Parts", vo);
        ArrayList<Region> ar0 = _get.regions.all(false);
        RegionManager rmg = _sim.getRegionManager();
        rmg.newRegionsFromParts(agp,
                rm.getMode(), null,
                bm.getMode(), null,
                im.getMode());
        ArrayList<Region> ar1 = _get.regions.all(false);
        ar1.removeAll(ar0);
        _io.say.msg(vo, "Regions created: %d", ar1.size());
        ArrayList<SimulationPartManager> amp = new ArrayList<>();
        amp.add(_sim.get(SimulationPartManager.class));
        //-- Attempt to automatically create the Interfaces.
        rmg.updateInterfacesFromPartContacts(amp, im.getMode());
        return ar1;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _get = _mu.get;
        _io = _mu.io;
    }

}
