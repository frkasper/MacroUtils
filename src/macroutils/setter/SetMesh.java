package macroutils.setter;

import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.meshing.*;
import star.prismmesher.*;
import star.solidmesher.*;

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

    private CustomPrismValuesManager _getCPVM(SurfaceCustomMeshControl scmc) {
        return scmc.getCustomValues().get(CustomPrismValuesManager.class);
    }

    private void _settingPrisms(ClientServerObject cso, boolean vo) {
        _io.say.action("Setting Prism Layers", vo);
        _io.say.object(cso, vo);
    }

    /**
     * Sets the mesh Base Size for a Mesh Operation, if applicable.
     *
     * @param mo given Mesh Operation.
     * @param val reference size.
     * @param u given Units.
     * @param vo given verbose option. False will not print anything.
     */
    public void baseSize(MeshOperation mo, double val, Units u, boolean vo) {
        _tmpl.print.actionAndObject("Setting the Mesh Base Size", mo, vo);
        BaseSize bs = _get.mesh.baseSize(mo, false);
        if (bs == null) {
            _io.say.msg("Mesh Operation does not have a Base Size.");
            return;
        }
        _set.object.physicalQuantity(bs, val, null, u, "Base Size", vo);
        _io.say.ok(vo);
    }

    /**
     * Sets Default Prism Mesh parameters for a Mesh Operation.
     *
     * @param amo given Automated Mesh Operation.
     * @param numLayers given number of prisms.
     * @param stretch given prism stretch relation.
     * @param relSize given relative size in (%).
     * @param vo given verbose option. False will not print anything.
     */
    public void prisms(AutoMeshOperation amo, int numLayers, double stretch, double relSize, boolean vo) {
        _tmpl.print.actionAndObject("Setting Mesh Prism Parameters", amo, vo);
        if (!_chk.has.prismLayerMesher(amo)) {
            _io.say.msg("Mesh Operation does not have Prism Layers.", vo);
            return;
        }
        PrismAutoMesher pam = ((PrismAutoMesher) amo.getMeshers().getObject("Prism Layer Mesher"));
        pam.setMinimumThickness(_ud.prismsMinThickn);
        pam.setGapFillPercentage(_ud.prismsGapFillPerc);
        pam.setLayerChoppingPercentage(_ud.prismsLyrChoppPerc);
        pam.setNearCoreLayerAspectRatio(_ud.prismsNearCoreAspRat);
        prisms(amo.getDefaultValues(), numLayers, stretch, relSize, false);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Mesh Operation.
     *
     * @param cm given Condition Manager.
     * @param numLayers given number of prisms.
     * @param stretch given prism stretch relation.
     * @param relSize given relative size in (%).
     * @param vo given verbose option. False will not print anything.
     */
    public void prisms(ConditionManager cm, int numLayers, double stretch, double relSize, boolean vo) {
        _settingPrisms(cm, vo);
        NumPrismLayers npl = cm.get(NumPrismLayers.class);
        PrismLayerStretching pls = cm.get(PrismLayerStretching.class);
        RelativeSize prs = cm.get(PrismThickness.class).getRelativeSize();
        npl.setNumLayers(numLayers);
        pls.setStretching(stretch);
        prs.setPercentage(relSize);
        _tmpl.print.prismsParameters(npl.getNumLayers(), pls.getStretching(), prs.getPercentage(), vo);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Custom Surface Mesh Control.
     *
     * @param scmc given Surface Custom Mesh Control.
     * @param numLayers given number of prisms. Zero is ignored.
     * @param stretch given prism stretch relation. Zero is ignored.
     * @param relSize given relative size in (%). Zero is ignored.
     * @param vo given verbose option. False will not print anything.
     */
    public void prisms(SurfaceCustomMeshControl scmc, int numLayers, double stretch, double relSize, boolean vo) {
        _settingPrisms(scmc, vo);
        PartsCustomizePrismMesh pcpm = scmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
        pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
        if (numLayers > 0) {
            pcpm.getCustomPrismControls().setCustomizeNumLayers(true);
            _getCPVM(scmc).get(NumPrismLayers.class).setNumLayers(numLayers);
        } else {
            pcpm.getCustomPrismControls().setCustomizeNumLayers(false);
        }
        if (stretch > 0) {
            pcpm.getCustomPrismControls().setCustomizeStretching(true);
            _getCPVM(scmc).get(PrismLayerStretching.class).setStretching(stretch);
        } else {
            pcpm.getCustomPrismControls().setCustomizeStretching(false);
        }
        if (relSize > 0) {
            pcpm.getCustomPrismControls().setCustomizeTotalThickness(true);
            _getCPVM(scmc).get(PrismThickness.class).setRelativeSize(relSize);
        } else {
            pcpm.getCustomPrismControls().setCustomizeTotalThickness(false);
        }
        _tmpl.print.prismsParameters(numLayers, stretch, relSize, vo);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Curvature parameters.
     *
     * @param sc given SurfaceCurvature.
     * @param n given points per curve value.
     * @param vo given verbose option. False will not print anything.
     */
    public void surfaceCurvature(SurfaceCurvature sc, double n, boolean vo) {
        _tmpl.print.actionAndObject("Setting Surface Curvature", (NamedObject) sc.getParent().getParent(), vo);
        SurfaceCurvatureNumPts scnp = sc.getSurfaceCurvatureNumPts();
        scnp.setNumPointsAroundCircle(n);
        _io.say.value("Surface Curvature Points/Curve", scnp.getNumPointsAroundCircle(), vo);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Proximity parameters.
     *
     * @param sp given SurfaceProximity.
     * @param np given number of points in gap.
     * @param sf given search floor in default units. number of points in gap.
     * @param vo given verbose option. False will not print anything.
     */
    public void surfaceProximity(SurfaceProximity sp, double np, double sf, boolean vo) {
        _tmpl.print.actionAndObject("Setting Surface Proximity", (NamedObject) sp.getParent().getParent(), vo);
        sp.setNumPointsInGap(np);
        _io.say.value("Number of Points in Gap", sp.getNumPointsInGap(), vo);
        _set.object.physicalQuantity(sp.getFloor(), sf, null, _ud.defUnitLength, "Proximity Search Floor", vo);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for an Auto Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     * @param min minimum relative size (%).
     * @param tgt target relative size (%).
     * @param vo given verbose option. False will not print anything.
     */
    public void surfaceSizes(AutoMeshOperation amo, double min, double tgt, boolean vo) {
        _tmpl.print.actionAndObject("Setting Mesh Surface Sizes", amo, vo);
        _tmpl.print.minTarget(min, tgt, vo);
        _get.mesh.targetRelativeSize(amo, false).setPercentage(tgt);
        RelativeSize rsm = _get.mesh.minRelativeSize(amo, false);
        if (rsm != null) {
            rsm.setPercentage(min);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for a Custom Mesh Control.
     *
     * @param cmc given Custom Mesh Control.
     * @param min minimum relative size (%) must be greater than zero. Otherwise it assumes the parent value.
     * @param tgt target relative size (%) must be greater than zero. Otherwise it assumes the parent value.
     * @param vo given verbose option. False will not print anything.
     */
    public void surfaceSizes(CustomMeshControl cmc, double min, double tgt, boolean vo) {
        _tmpl.print.actionAndObject("Setting Custom Surface Sizes", cmc.getManager().getMeshOperation(), vo);
        _io.say.object(cmc, vo);
        _tmpl.print.minTarget(min, tgt, vo);
        CustomMeshControlConditionManager cm = cmc.getCustomConditions();
        if (min > 0) {
            cm.get(PartsMinimumSurfaceSizeOption.class).setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            PartsMinimumSurfaceSize pmss = cmc.getCustomValues().get(PartsMinimumSurfaceSize.class);
            ((GenericRelativeSize) pmss.getRelativeSize()).setPercentage(min);
        }
        if (tgt > 0) {
            cm.get(PartsTargetSurfaceSizeOption.class).setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            PartsTargetSurfaceSize ptss = cmc.getCustomValues().get(PartsTargetSurfaceSize.class);
            ((GenericRelativeSize) ptss.getRelativeSize()).setPercentage(tgt);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Thin Mesher parameters.
     *
     * @param amo given Automated Mesh Operation.
     * @param numLayers given number of prisms.
     * @param thicknThr given Custom Thickness Threshold Size in (%).
     * @param vo given verbose option. False will not print anything.
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
        ThinThicknessThreshold ttt = amo.getDefaultValues().get(ThinThicknessThreshold.class);
        ttt.getRelativeSize().setPercentage(thicknThr);
        ThinNumLayers tnl = amo.getDefaultValues().get(ThinNumLayers.class);
        tnl.setLayers(Math.max(2, numLayers));
        _tmpl.print.thinMeshParameters(tnl.getLayers(), ttt.getRelativeSize().getPercentage(), vo);
        _io.say.ok(vo);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _chk = _mu.check;
        _get = _mu.get;
        _set = _mu.set;
        _tmpl = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    //--
    //-- Variables declaration area.
    //--
    private MacroUtils _mu = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.templates.MainTemplates _tmpl = null;
    private MainSetter _set = null;
    private macroutils.getter.MainGetter _get = null;

}
