package macroutils.creator;

import macroutils.MacroUtils;
import star.common.Boundary;
import star.common.BoundaryInterface;
import star.common.InterfaceConfigurationOption;
import star.common.Simulation;

/**
 * Low-level class for creating Interfaces with MacroUtils.
 *
 * @since September of 2016
 * @author Fabio Kasper
 */
public class CreateInterface {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateInterface(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private BoundaryInterface _createInterface(Boundary b1, Boundary b2) {
        _io.say.action("Creating a Boundary Interface", true);
        _io.say.object(b1, true);
        _io.say.object(b2, true);
        BoundaryInterface bi = _sim.getInterfaceManager().createBoundaryInterface(b1, b2, "Interface");
        return bi;
    }

    /**
     * Creates a Boundary Interface.
     *
     * @param b1 given Boundary 1.
     * @param b2 given Boundary 2.
     * @return The BoundaryInterface.
     */
    public BoundaryInterface boundaryInterface(Boundary b1, Boundary b2) {
        BoundaryInterface bi = _createInterface(b1, b2);
        _io.say.created(bi, true);
        return bi;
    }

    /**
     * Creates a Boundary Interface.
     *
     * @param b1 given Boundary 1.
     * @param b2 given Boundary 2.
     * @param type given InterfaceConfigurationOption type;
     * @return The BoundaryInterface.
     */
    public BoundaryInterface boundaryInterface(Boundary b1, Boundary b2, InterfaceConfigurationOption.Type type) {
        BoundaryInterface bi = _createInterface(b1, b2);
        _io.say.created(bi, true);
        bi.getTopology().setSelected(type);
        _io.say.value("Interface Type", type.getPresentationName(), true, true);
        _io.say.created(bi, true);
        return bi;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
