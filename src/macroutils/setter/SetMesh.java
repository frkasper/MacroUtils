package macroutils.setter;

import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.meshing.*;
import star.prismmesher.*;
import star.solidmesher.*;
import star.sweptmesher.*;

/**
 * Low-level class for setting Mesh parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetMesh {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetMesh(MacroUtils m) {
        _mu = m;
    }

    private AutoMeshDefaultValuesManager _getAMDVM(AutoMeshBase amb) {
        return amb.getDefaultValues();
    }

    private CustomPrismValuesManager _getCPVM(SurfaceCustomMeshControl scmc) {
        return scmc.getCustomValues().get(CustomPrismValuesManager.class);
    }

    private DirectedAutoSourceMesh _getDirectedAutoSourceMesh(NamedObject no) {
        DirectedMeshOperation dmo = (DirectedMeshOperation) no;
        for (Object o : dmo.getGuidedSurfaceMeshBaseManager().getObjects()) {
            if (o instanceof DirectedAutoSourceMesh) {
                return (DirectedAutoSourceMesh) o;
            }
        }
        return null;
    }

    private void _setRelativeSize(PartsRelativeOrAbsoluteSize rs, String what, double perc) {
        if (rs == null) {
            return;
        }
        rs.setRelativeSize(perc);
        _io.say.percentage(what, rs.getRelativeSizeValue(), true);
    }

    private void _setRelativeSize(RelativeOrAbsoluteSize rs, String what, double perc) {
        if (rs == null) {
            return;
        }
        rs.setRelativeSize(perc);
        _io.say.percentage(what, rs.getRelativeSizeValue().getPercentage(), true);
    }

    private void _settingPrisms(ClientServerObject cso, boolean vo) {
        _io.say.action("Setting Prism Layers", vo);
        _io.say.object(cso, vo);
    }

    /**
     * Sets the mesh Base Size for a Mesh Operation, if applicable.
     *
     * @param no given Named Object, if applicable.
     * @param val reference size.
     * @param u given Units.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void baseSize(NamedObject no, double val, Units u, boolean vo) {
        _io.say.action("Setting the Mesh Base Size", no, vo);
        if ((no instanceof DirectedMeshOperation) && _getDirectedAutoSourceMesh(no) != null) {
            no = _getDirectedAutoSourceMesh(no);
        }
        if (no instanceof AutoMeshBase) {
            BaseSize bs = _getAMDVM((AutoMeshBase) no).get(BaseSize.class);
            _set.object.physicalQuantity(bs, val, u, "Base Size", true);
            _io.say.ok(vo);
            return;
        }
        _io.say.msg("Mesh Operation does not have a Base Size.");
    }

    /**
     * Sets the maximum cell size.
     *
     * @param amo given AutoMeshOperation.
     * @param val given value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void maxCellSize(AutoMeshOperation amo, double val, boolean vo) {
        _io.say.action("Setting the Maximum Cell Size", vo);
        _io.say.object(amo, vo);
        if (!_chk.has.trimmerMesher(amo)) {
            _io.say.msg("Not a Trimmer Mesh Operation", vo);
            return;
        }
        _setRelativeSize(_getAMDVM(amo).get(MaximumCellSize.class), "Maximum Cell Size", val);
        _io.say.ok(vo);
    }

    /**
     * Sets the number of Prism Layers.
     *
     * @param npl given NumPrismLayers.
     * @param n given number of layers.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void numPrismLayers(NumPrismLayers npl, int n, boolean vo) {
        _io.say.action("Setting the Number of Prism Layers", vo);
        npl.setNumLayers(n);
        _io.say.value("Number of Prism Layers", npl.getNumLayers(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets the Prism Layer Stretching.
     *
     * @param pls given PrismLayerStretching.
     * @param val given value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void prismLayerStretching(PrismLayerStretching pls, double val, boolean vo) {
        _io.say.action("Setting Prism Layer Stretching", vo);
        pls.setStretching(val);
        _io.say.value("Prism Layer Stretching", pls.getStretching(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets the Prism Layer Total Thickness.
     *
     * @param pt given PrismThickness.
     * @param val given value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void prismLayerTotalThickness(PrismThickness pt, double val, boolean vo) {
        _io.say.action("Setting Prism Layer Total Thickness", vo);
        pt.setRelativeSize(val);
        _io.say.percentage("Prism Layer Total Thickness", pt.getRelativeSizeValue(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets Default Prism Mesh parameters for a Mesh Operation.
     *
     * @param amo given Automated Mesh Operation.
     * @param numLayers given number of prisms.
     * @param stretch given prism stretch relation.
     * @param relSize given relative size in (%).
     * @param vo given verbose option. False will only print necessary data.
     */
    public void prisms(AutoMeshOperation amo, int numLayers, double stretch, double relSize, boolean vo) {
        _io.say.action("Setting Mesh Prism Parameters", amo, vo);
        if (!_chk.has.prismLayerMesher(amo)) {
            _io.say.msg("Mesh Operation does not have Prism Layers.", vo);
            return;
        }
        PrismAutoMesher pam = ((PrismAutoMesher) amo.getMeshers().getObject("Prism Layer Mesher"));
        pam.setMinimumThickness(_ud.prismsMinThickn);
        pam.setGapFillPercentage(_ud.prismsGapFillPerc);
        pam.setLayerChoppingPercentage(_ud.prismsLyrChoppPerc);
        pam.setNearCoreLayerAspectRatio(_ud.prismsNearCoreAspRat);
        prisms(_getAMDVM(amo), numLayers, stretch, relSize, false);
        _io.say.value("Prism Layer Minimum Thickness", pam.getMinimumThickness(), true);
        _io.say.percentage("Prism Layer Gap Fill Percentage", pam.getGapFillPercentage(), true);
        _io.say.percentage("Prism Layer Chopping Percentage", pam.getLayerChoppingPercentage(), true);
        _io.say.value("Prism Layer Near Core Aspect Ratio", pam.getNearCoreLayerAspectRatio(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Mesh Operation.
     *
     * @param cm given Condition Manager.
     * @param numLayers given number of prisms.
     * @param stretch given prism stretch relation.
     * @param relSize given relative size in (%).
     * @param vo given verbose option. False will only print necessary data.
     */
    public void prisms(ConditionManager cm, int numLayers, double stretch, double relSize, boolean vo) {
        _settingPrisms(cm, vo);
        numPrismLayers(cm.get(NumPrismLayers.class), numLayers, false);
        prismLayerStretching(cm.get(PrismLayerStretching.class), stretch, false);
        _setRelativeSize(cm.get(PrismThickness.class), "Prism Layer Total Thickness", relSize);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Custom Surface Mesh Control.
     *
     * @param scmc given Surface Custom Mesh Control.
     * @param numLayers given number of prisms. Zero is ignored.
     * @param stretch given prism stretch relation. Zero is ignored.
     * @param relSize given relative size in (%). Zero is ignored.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void prisms(SurfaceCustomMeshControl scmc, int numLayers, double stretch, double relSize, boolean vo) {
        _settingPrisms(scmc, vo);
        PartsCustomizePrismMesh pcpm = scmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
        pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
        if (numLayers > 0) {
            pcpm.getCustomPrismControls().setCustomizeNumLayers(true);
            numPrismLayers(_getCPVM(scmc).get(NumPrismLayers.class), numLayers, false);
        } else {
            pcpm.getCustomPrismControls().setCustomizeNumLayers(false);
        }
        if (stretch > 0) {
            pcpm.getCustomPrismControls().setCustomizeStretching(true);
            prismLayerStretching(_getCPVM(scmc).get(PrismLayerStretching.class), stretch, false);
        } else {
            pcpm.getCustomPrismControls().setCustomizeStretching(false);
        }
        if (relSize > 0) {
            pcpm.getCustomPrismControls().setCustomizeTotalThickness(true);
            _getCPVM(scmc).get(PrismThickness.class).setRelativeSize(relSize);
            _setRelativeSize(_getCPVM(scmc).get(PrismThickness.class), "Prism Layer Total Thickness", relSize);
        } else {
            pcpm.getCustomPrismControls().setCustomizeTotalThickness(false);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Curvature parameters.
     *
     * @param sc given SurfaceCurvature.
     * @param n given points per curve value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void surfaceCurvature(SurfaceCurvature sc, double n, boolean vo) {
        _io.say.action("Setting Surface Curvature", (NamedObject) sc.getParent().getParent(), vo);
        sc.setNumPointsAroundCircle(n);
        _io.say.value("Surface Curvature Points/Curve", sc.getNumPointsAroundCircle(), true);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Proximity parameters.
     *
     * @param sp given SurfaceProximity.
     * @param np given number of points in gap.
     * @param sf given search floor in default units. number of points in gap.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void surfaceProximity(SurfaceProximity sp, double np, double sf, boolean vo) {
        _io.say.action("Setting Surface Proximity", (NamedObject) sp.getParent().getParent(), vo);
        sp.setNumPointsInGap(np);
        _io.say.value("Number of Points in Gap", sp.getNumPointsInGap(), true);
        _set.object.physicalQuantity(sp.getFloor(), sf, _ud.defUnitLength, "Proximity Search Floor", true);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for an Auto Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     * @param min minimum relative size (%).
     * @param tgt target relative size (%).
     * @param vo given verbose option. False will only print necessary data.
     */
    public void surfaceSizes(AutoMeshOperation amo, double min, double tgt, boolean vo) {
        _io.say.action("Setting Mesh Surface Sizes", amo, vo);
        _setRelativeSize(_get.mesh.targetRelativeSize(amo, false), "Target Surface Size", tgt);
        _setRelativeSize(_get.mesh.minRelativeSize(amo, false), "Minimum Surface Size", min);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for a Custom Mesh Control.
     *
     * @param cmc given Custom Mesh Control.
     * @param min minimum relative size (%) must be greater than zero. Otherwise it assumes the parent value.
     * @param tgt target relative size (%) must be greater than zero. Otherwise it assumes the parent value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void surfaceSizes(CustomMeshControl cmc, double min, double tgt, boolean vo) {
        _io.say.action("Setting Custom Surface Sizes", cmc.getManager().getMeshOperation(), vo);
        _io.say.object(cmc, vo);
        CustomMeshControlConditionManager cm = cmc.getCustomConditions();
        if (min > 0) {
            cm.get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            _setRelativeSize(cmc.getCustomValues().get(PartsMinimumSurfaceSize.class), "Minimum Surface Size", min);
        }
        if (tgt > 0) {
            cm.get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            _setRelativeSize(cmc.getCustomValues().get(PartsTargetSurfaceSize.class), "Target Surface Size", tgt);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Thin Mesher parameters.
     *
     * @param amo given Automated Mesh Operation.
     * @param numLayers given number of prisms.
     * @param thicknThr given Custom Thickness Threshold Size in (%).
     * @param vo given verbose option. False will only print necessary data.
     */
    public void thinMesher(AutoMeshOperation amo, int numLayers, double thicknThr, boolean vo) {
        _io.say.action("Setting Thin Mesh Layers", vo);
        _io.say.object(amo, vo);
        if (!_chk.has.thinMesher(amo)) {
            _io.say.msg("Mesh Operation does not have Thin Mesher.", vo);
            return;
        }
        ThinAutoMesher tam = ((ThinAutoMesher) amo.getMeshers().getObject("Thin Mesher"));
        tam.setCustomizeThicknessOption(true);
        ThinNumLayers tnl = _getAMDVM(amo).get(ThinNumLayers.class);
        tnl.setLayers(Math.max(2, numLayers));
        _io.say.value("Number of Thin Layers", tnl.getLayers(), true);
        _setRelativeSize(_getAMDVM(amo).get(ThinThicknessThreshold.class), "Thin Layer Thickness Threshold", thicknThr);
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.UserDeclarations _ud = null;

}
