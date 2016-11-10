package macroutils.getter;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.meshing.*;
import star.resurfacer.*;
import star.surfacewrapper.*;

/**
 * Low-level class for getting Mesh parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetMesh {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetMesh(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private void _checkGotNull(ClientServerObject cso, String what, boolean vo) {
        if (cso != null) {
            return;
        }
        _io.say.msg(vo, "Mesh Operation does not have %s.", what);
    }

    /**
     * Gets the mesh Base Size for a Mesh Operation, if applicable.
     *
     * @param mo given Mesh Operation.
     * @param vo given verbose option. False will not print anything.
     * @return The BaseSize object. Null if not applicable.
     */
    public BaseSize baseSize(MeshOperation mo, boolean vo) {
        _io.say.action("Getting the Mesh Base Size", vo);
        _io.say.object(mo, vo);
        BaseSize bs = null;
        if (_chk.is.autoMeshOperation(mo)) {
            bs = ((AutoMeshOperation) mo).getDefaultValues().get(BaseSize.class);
            _io.say.value("Base Size", bs.getRawValue(), bs.getUnits(), vo);
        }
        _checkGotNull(bs, "Base Size", vo);
        return bs;
    }

    /**
     * Gets a Custom Mesh Control from a Mesh Operation using a REGEX search pattern.
     *
     * @param mo given Mesh Operation.
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @return The CustomMeshControl. Null if nothing is found.
     */
    public CustomMeshControl customControl(MeshOperation mo, String regexPatt) {
        if (!_chk.is.autoMeshOperation(mo)) {
            _checkGotNull(null, "Custom Mesh Control", true);
            _io.say.msg("Returning NULL!");
            return null;
        }
        AutoMeshOperation amo = (AutoMeshOperation) mo;
        return (CustomMeshControl) _get.objects.byREGEX(regexPatt,
                new ArrayList(amo.getCustomMeshControls().getObjects()), true);
    }

    /**
     * Gets the Finite Volume Representation.
     *
     * @return FvRepresentation.
     */
    public FvRepresentation fvr() {
        return (FvRepresentation) _sim.getRepresentationManager().getObject("Volume Mesh");
    }

    /**
     * Gets the Geometry Part Representation.
     *
     * @return PartRepresentation.
     */
    public PartRepresentation geometry() {
        return ((PartRepresentation) _sim.getRepresentationManager().getObject("Geometry"));
    }

    /**
     * Gets the mesh Minimum Relative Size for an AutoMeshOperation, if applicable.
     *
     * @param amo given AutoMeshOperation.
     * @param vo given verbose option. False will not print anything.
     * @return The RelativeSize object in percentage. Null if not applicable.
     */
    public RelativeSize minRelativeSize(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Minimum Relative Size", vo);
        _io.say.object(amo, vo);
        RelativeSize rs = null;
        if (_chk.has.remesher(amo)) {
            PartsMinimumSurfaceSize pmss = amo.getDefaultValues().get(PartsMinimumSurfaceSize.class);
            rs = pmss.getRelativeSize();
        }
        if (_chk.is.surfaceWrapperOperation(amo)) {
            SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) amo;
            rs = swamo.getDefaultValues().get(PartsMinimumSurfaceSize.class).getRelativeSize();
        }
        _checkGotNull(rs, "Minimum Relative Size", vo);
        return rs;
    }

    /**
     * Gets a Mesh Operation using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo given verbose option. False will not print anything.
     * @return The MeshOperation. Null if nothing is found.
     */
    public MeshOperation operation(String regexPatt, boolean vo) {
        return (MeshOperation) _get.objects.byREGEX(regexPatt,
                new ArrayList(_sim.get(MeshOperationManager.class).getObjects()), vo);
    }

    /**
     * Gets the Surface Remesher object, the that is within a Mesh Operation.
     *
     * @param mo given Mesh Operation.
     * @param vo given verbose option. False will not print anything.
     * @return The ResurfacerAutoMesher object. Null if not applicable.
     */
    public ResurfacerAutoMesher remesher(MeshOperation mo, boolean vo) {
        _io.say.action("Getting the Surface Remesher object", vo);
        _io.say.object(mo, vo);
        ResurfacerAutoMesher ram = null;
        if (_chk.is.autoMeshOperation(mo)) {
            AutoMeshOperation amo = (AutoMeshOperation) mo;
            ram = (ResurfacerAutoMesher) amo.getMeshers().getObject("Surface Remesher");
        }
        _checkGotNull(ram, "Remesher", vo);
        return ram;
    }

    /**
     * Gets the mesh Target Relative Size for an AutoMeshOperation, if applicable.
     *
     * @param amo given AutoMeshOperation.
     * @param vo given verbose option. False will not print anything.
     * @return The RelativeSize object in percentage. Null if not applicable.
     */
    public RelativeSize targetRelativeSize(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Target Relative Size", vo);
        _io.say.object(amo, vo);
        return amo.getDefaultValues().get(PartsTargetSurfaceSize.class).getRelativeSize();
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
    private MainGetter _get = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.io.MainIO _io = null;
    private Simulation _sim = null;

}
