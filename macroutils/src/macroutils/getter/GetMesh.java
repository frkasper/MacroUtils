package macroutils.getter;

import java.util.ArrayList;
import macroutils.MacroUtils;
import star.base.neo.ClientServerObject;
import star.common.FvRepresentation;
import star.common.Simulation;
import star.delaunaymesher.DelaunayAutoMesher;
import star.dualmesher.DualAutoMesher;
import star.meshing.AutoMeshOperation;
import star.meshing.BaseSize;
import star.meshing.CurrentDescriptionSource;
import star.meshing.CustomMeshControl;
import star.meshing.MeshOperation;
import star.meshing.MeshOperationManager;
import star.meshing.MeshPartDescriptionSource;
import star.meshing.PartRepresentation;
import star.meshing.PartsMinimumSurfaceSize;
import star.meshing.PartsRelativeOrAbsoluteSize;
import star.meshing.PartsTargetSurfaceSize;
import star.meshing.SimulationMeshPartDescriptionSourceManager;
import star.resurfacer.ResurfacerAutoMesher;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;

/**
 * Low-level class for getting Mesh parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class GetMesh {

    private macroutils.checker.MainChecker _chk = null;
    private MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public GetMesh(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
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
     * @param mo        given Mesh Operation.
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
                new ArrayList<>(amo.getCustomMeshControls().getObjects()), true);
    }

    /**
     * Gets a mesh description as can be seen in user interface.
     *
     * @param name given mesh description.
     * @return The MeshPartDescriptionSource.
     */
    public MeshPartDescriptionSource descriptionSource(String name) {
        return _sim.get(SimulationMeshPartDescriptionSourceManager.class).getObject(name);
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
        return (PartRepresentation) _sim.getRepresentationManager().getObject("Geometry");
    }

    /**
     * Get the Latest Surface mesh description.
     *
     * @return The CurrentDescriptionSource.
     */
    public CurrentDescriptionSource latestSurfaceDescriptionSource() {
        return (CurrentDescriptionSource) descriptionSource("Latest Surface");
    }

    /**
     * Gets the mesh Minimum Relative Size for an AutoMeshOperation, if applicable.
     *
     * @param amo given AutoMeshOperation.
     * @param vo  given verbose option. False will not print anything.
     * @return The PartsRelativeOrAbsoluteSize object. Null if not applicable.
     */
    public PartsRelativeOrAbsoluteSize minRelativeSize(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Minimum Relative Size", vo);
        _io.say.object(amo, vo);
        if (_chk.has.remesher(amo)) {
            return amo.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        }
        if (_chk.is.surfaceWrapperOperation(amo)) {
            SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) amo;
            return swamo.getDefaultValues().get(PartsMinimumSurfaceSize.class);
        }
        _checkGotNull(null, "Minimum Relative Size", vo);
        return null;
    }

    /**
     * Gets a Mesh Operation using a REGEX search pattern.
     *
     * @param regexPatt given Regular Expression (REGEX) pattern.
     * @param vo        given verbose option. False will not print anything.
     * @return The MeshOperation. Null if nothing is found.
     */
    public MeshOperation operation(String regexPatt, boolean vo) {
        return _get.objects.byREGEX(regexPatt,
                new ArrayList<>(_sim.get(MeshOperationManager.class).getObjects()), vo);
    }

    /**
     * Gets the Polyhedral Mesher object that is within a Mesh Operation.
     *
     * @param amo given AutoMeshOperation
     * @param vo  given verbose option. False will not print anything
     * @return The DualAutoMesher object; null if not applicable
     */
    public DualAutoMesher polyMesher(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Polyhedral Mesher object", vo);
        DualAutoMesher dam = null;
        if (_chk.has.polyMesher(amo)) {
            dam = (DualAutoMesher) amo.getMeshers().getObject("Polyhedral Mesher");
        }
        _io.say.object(amo, vo);
        _checkGotNull(dam, "Polyhedral Mesher", vo);
        return dam;
    }

    /**
     * Gets the Surface Remesher object that is within a Mesh Operation.
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
        _checkGotNull(ram, "Surface Remesher", vo);
        return ram;
    }

    /**
     * Gets the mesh Target Relative Size for an AutoMeshOperation, if applicable.
     *
     * @param amo given AutoMeshOperation.
     * @param vo  given verbose option. False will not print anything.
     * @return The PartsRelativeOrAbsoluteSize object. Null if not applicable.
     */
    public PartsRelativeOrAbsoluteSize targetRelativeSize(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Target Relative Size", vo);
        _io.say.object(amo, vo);
        return amo.getDefaultValues().get(PartsTargetSurfaceSize.class);
    }

    /**
     * Gets the Tetrahedral Mesher object that is within a Mesh Operation.
     *
     * @param amo given AutoMeshOperation
     * @param vo  given verbose option. False will not print anything
     * @return The DelaunayAutoMesher object; null if not applicable
     */
    public DelaunayAutoMesher tetMesher(AutoMeshOperation amo, boolean vo) {
        _io.say.action("Getting the Tetrahedral Mesher object", vo);
        DelaunayAutoMesher dam = null;
        if (_chk.has.tetMesher(amo)) {
            dam = (DelaunayAutoMesher) amo.getMeshers().getObject("Tetrahedral Mesher");
        }
        _io.say.object(amo, vo);
        _checkGotNull(dam, "Tetrahedral Mesher", vo);
        return dam;
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
    }

    private void _checkGotNull(ClientServerObject cso, String what, boolean vo) {
        if (cso != null) {
            return;
        }
        _io.say.msg(vo, "Mesh Operation does not have %s.", what);
    }

}
