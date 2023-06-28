package macroutils.setter;

import macroutils.MacroUtils;
import star.base.neo.ClientServerObject;
import star.base.neo.NamedObject;
import star.common.ConditionManager;
import star.common.Units;
import star.meshing.AutoMeshBase;
import star.meshing.AutoMeshDefaultValuesManager;
import star.meshing.AutoMeshOperation;
import star.meshing.BaseSize;
import star.meshing.CustomMeshControl;
import star.meshing.CustomMeshControlConditionManager;
import star.meshing.MaximumCellSize;
import star.meshing.PartsCoreMeshOptimizer;
import star.meshing.PartsMinimumSurfaceSize;
import star.meshing.PartsMinimumSurfaceSizeOption;
import star.meshing.PartsTargetSurfaceSize;
import star.meshing.PartsTargetSurfaceSizeOption;
import star.meshing.SurfaceCurvature;
import star.meshing.SurfaceCustomMeshControl;
import star.meshing.SurfaceProximity;
import star.meshing.VolumeCustomMeshControl;
import star.prismmesher.CustomPrismValuesManager;
import star.prismmesher.NumPrismLayers;
import star.prismmesher.PartsCustomPrismsOption;
import star.prismmesher.PartsCustomizePrismMesh;
import star.prismmesher.PrismAutoMesher;
import star.prismmesher.PrismLayerCoreLayerAspectRatio;
import star.prismmesher.PrismLayerGapFillPercentage;
import star.prismmesher.PrismLayerMinimumThickness;
import star.prismmesher.PrismLayerReductionPercentage;
import star.prismmesher.PrismLayerStretching;
import star.prismmesher.PrismThickness;
import star.prismmesher.VolumeControlPrismsOption;
import star.resurfacer.SurfaceGrowthRate;
import star.solidmesher.ThinAutoMesher;
import star.solidmesher.ThinNumLayers;
import star.solidmesher.ThinThicknessThreshold;
import star.sweptmesher.DirectedAutoSourceMesh;
import star.sweptmesher.DirectedMeshOperation;

/**
 * Low-level class for setting Mesh parameters with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class SetMesh {

    private macroutils.checker.MainChecker _chk = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private MainSetter _set = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public SetMesh(MacroUtils m) {
        _mu = m;
    }

    /**
     * Sets the mesh Base Size for a Mesh Operation, if applicable.
     *
     * @param no  given Named Object, if applicable.
     * @param val reference size.
     * @param u   given Units.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void baseSize(NamedObject no, double val, Units u, boolean vo) {
        _io.say.action("Setting the Mesh Base Size", no, vo);
        if ((no instanceof DirectedMeshOperation) && _getDirectedAutoSourceMesh(no) != null) {
            no = _getDirectedAutoSourceMesh(no);
        }
        if (no instanceof AutoMeshBase) {
            AutoMeshBase amb = (AutoMeshBase) no;
            BaseSize bs = amb.getDefaultValues().get(BaseSize.class);
            _set.object.physicalQuantity(bs, val, u, "Base Size", true);
            _io.say.ok(vo);
            return;
        }
        _io.say.msg("Mesh Operation does not have a Base Size.");
    }

    /**
     * Set the core mesh optimizer parameters.
     *
     * @param amo              given AutoMeshOperation
     * @param optCycles        given optimization cycles
     * @param qualityThreshold given quality threshold
     * @param vo given verbose option. False will not print anything
     */
    public void coreMeshOptimizer(AutoMeshOperation amo, int optCycles, double qualityThreshold,
            boolean vo) {
        _io.say.action("Setting the Core Mesh Optimizer settings", vo);
        if (_chk.has.tetMesher(amo) || _chk.has.polyMesher(amo)) {
            PartsCoreMeshOptimizer pcmo = amo.getDefaultValues().get(PartsCoreMeshOptimizer.class);
            pcmo.setOptimizeCycles(optCycles);
            pcmo.setQualityThreshold(qualityThreshold);
            _io.say.value("Core Mesh Optimization Cycles", pcmo.getOptimizeCycles(), true);
            _io.say.value("Core Mesh Quality Threshold", pcmo.getQualityThreshold(), true);
        } else {
            _io.say.msg("Not a Trimmer Mesh Operation", vo);
        }
        _io.say.ok(vo);
    }

    /**
     * Sets the maximum cell size.
     *
     * @param amo given AutoMeshOperation.
     * @param val given value.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void maxCellSize(AutoMeshOperation amo, double val, boolean vo) {
        _io.say.action("Setting the Maximum Cell Size", vo);
        _io.say.object(amo, vo);
        if (!_chk.has.trimmerMesher(amo)) {
            _io.say.msg("Not a Trimmer Mesh Operation", vo);
            return;
        }
        AutoMeshDefaultValuesManager amdvm = amo.getDefaultValues();
        _set.object.relativeSize(amdvm.get(MaximumCellSize.class), "Maximum Cell Size", val);
        _io.say.ok(vo);
    }

    /**
     * Sets the number of Prism Layers.
     *
     * @param npl given NumPrismLayers.
     * @param n   given number of layers.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void numPrismLayers(NumPrismLayers npl, int n, boolean vo) {
        _io.say.action("Setting the Number of Prism Layers", vo);
        if (n < 1) {
            _io.say.value("Invalid Number of Prism Layers", n, true);
            return;
        }
        npl.setNumLayers(n);
        _io.say.value("Number of Prism Layers", npl.getNumLayersValue().getValue(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets the Prism Layer Stretching.
     *
     * @param pls given PrismLayerStretching.
     * @param val given value.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void prismLayerStretching(PrismLayerStretching pls, double val, boolean vo) {
        _io.say.action("Setting Prism Layer Stretching", vo);
        if (val < 1.0) {
            _io.say.value("Invalid Prism Layer Stretching", val, true);
            return;
        }
        pls.setStretching(val);
        _io.say.value("Prism Layer Stretching", pls.getStretchingQuantity(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets the Prism Layer Total Thickness.
     *
     * @param pt  given PrismThickness.
     * @param val given value.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void prismLayerTotalThickness(PrismThickness pt, double val, boolean vo) {
        _io.say.action("Setting Prism Layer Total Thickness", vo);
        if (val <= 0) {
            _io.say.value("Invalid Prism Layer Total Thickness", val, true);
            return;
        }
        _mu.set.object.relativeSize(pt, "Prism Layer Total Thickness", val);
        _io.say.ok(vo);
    }

    /**
     * Sets Default Prism Mesh parameters for a Mesh Operation.
     *
     * @param amo            given Automated Mesh Operation.
     * @param numLayers      given number of prisms.
     * @param stretch        given prism stretch relation.
     * @param totalThickness given total thickness size in (%).
     * @param vo             given verbose option. False will only print necessary data.
     */
    public void prisms(AutoMeshOperation amo, int numLayers, double stretch,
            double totalThickness, boolean vo) {
        _io.say.action("Setting Mesh Prism Parameters", amo, vo);
        if (!_chk.has.prismLayerMesher(amo)) {
            _io.say.msg("Mesh Operation does not have Prism Layers.", vo);
            return;
        }
        AutoMeshDefaultValuesManager amdvm = amo.getDefaultValues();
        PrismLayerMinimumThickness mt = amdvm.get(PrismLayerMinimumThickness.class);
        PrismLayerGapFillPercentage gfp = amdvm.get(PrismLayerGapFillPercentage.class);
        PrismLayerReductionPercentage rp = amdvm.get(PrismLayerReductionPercentage.class);
        PrismLayerCoreLayerAspectRatio clar = amdvm.get(PrismLayerCoreLayerAspectRatio.class);
        mt.setValue(_ud.prismsMinThickn);
        gfp.setValue(_ud.prismsGapFillPerc);
        rp.setValue(_ud.prismsLyrChoppPerc);
        clar.setValue(_ud.prismsNearCoreAspRat);
        prisms(amdvm, numLayers, stretch, totalThickness, false);
        _io.say.value("Prism Layer Minimum Thickness",
                mt.getMinimumThicknessPercentageInput().getValue(), true);
        _io.say.percentage("Prism Layer Gap Fill Percentage",
                gfp.getGapFillPercentageInput().getValue(), true);
        _io.say.percentage("Prism Layer Chopping Percentage",
                rp.getReductionPercentageInput().getValue(), true);
        _io.say.value("Prism Layer Near Core Aspect Ratio",
                clar.getCoreLayerAspectRatioInput().getValue(), true);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Mesh Operation.
     *
     * @param cm             given Condition Manager.
     * @param numLayers      given number of prisms.
     * @param stretch        given prism stretch relation.
     * @param totalThickness given total thickness size in (%).
     * @param vo             given verbose option. False will only print necessary data.
     */
    public void prisms(ConditionManager cm, int numLayers, double stretch,
            double totalThickness, boolean vo) {
        _settingPrisms(cm, vo);
        numPrismLayers(cm.get(NumPrismLayers.class), numLayers, false);
        prismLayerStretching(cm.get(PrismLayerStretching.class), stretch, false);
        _set.object.relativeSize(cm.get(PrismThickness.class), "Prism Layer Total Thickness",
                totalThickness);
        _io.say.ok(vo);
    }

    /**
     * Sets Prism Mesh parameters for a Custom Mesh Control -- Surface or Volumetric.
     *
     * @param cmc            given Custom Mesh Control.
     * @param numLayers      given number of prisms. Zero is ignored.
     * @param stretch        given prism stretch relation. Zero is ignored.
     * @param totalThickness given total thickness size in (%). Zero is ignored.
     * @param vo             given verbose option. False will only print necessary data.
     */
    public void prisms(CustomMeshControl cmc, int numLayers, double stretch,
            double totalThickness, boolean vo) {
        _settingPrisms(cmc, vo);
        PartsCustomizePrismMesh pcpm;
        VolumeControlPrismsOption vcpo;
        boolean hasNumLayers = numLayers > 0;
        boolean hasStretching = stretch > 0;
        boolean hasTotalThickness = totalThickness > 0;
        if (!(hasNumLayers || hasStretching || hasTotalThickness)) {
            _io.say.msg("Nothing to change.", vo);
            return;
        }
        if (cmc instanceof SurfaceCustomMeshControl) {
            pcpm = cmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
            pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
            pcpm.getCustomPrismControls().setCustomizeNumLayers(hasNumLayers);
            pcpm.getCustomPrismControls().setCustomizeStretching(hasStretching);
            pcpm.getCustomPrismControls().setCustomizeTotalThickness(hasTotalThickness);
        } else if (cmc instanceof VolumeCustomMeshControl) {
            vcpo = cmc.getCustomConditions().get(VolumeControlPrismsOption.class);
            vcpo.setCustomizeNumLayers(hasNumLayers);
            vcpo.setCustomizeStretching((hasStretching));
            vcpo.setCustomizeTotalThickness(hasTotalThickness);
        }
        CustomPrismValuesManager cpvm = cmc.getCustomValues().get(CustomPrismValuesManager.class);
        if (hasNumLayers) {
            numPrismLayers(cpvm.get(NumPrismLayers.class), numLayers, false);
        }
        if (hasStretching) {
            prismLayerStretching(cpvm.get(PrismLayerStretching.class), stretch, false);
        }
        if (hasTotalThickness) {
            prismLayerTotalThickness(cpvm.get(PrismThickness.class), totalThickness, false);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Curvature parameters.
     *
     * @param sc given SurfaceCurvature.
     * @param n  given points per curve value.
     * @param vo given verbose option. False will only print necessary data.
     */
    public void surfaceCurvature(SurfaceCurvature sc, double n, boolean vo) {
        _io.say.action("Setting Surface Curvature", (NamedObject) sc.getParent().getParent(), vo);
        sc.setNumPointsAroundCircle(n);
        _io.say.value("Surface Curvature Points/Curve", sc.getNumPointsAroundCircle(), true);
        _io.say.ok(vo);
    }

    /**
     * Specifies a custom Surface Growth parameter.
     *
     * @param amo given AutoMeshOperation
     * @param gr  given growth rate value
     * @param vo  given verbose option; false will only print necessary data
     */
    public void surfaceGrowthRate(AutoMeshOperation amo, double gr, boolean vo) {
        _io.say.action("Setting Surface Growth Rate", amo, vo);
        SurfaceGrowthRate sgr = amo.getDefaultValues().get(SurfaceGrowthRate.class);
        sgr.setGrowthRateOption(SurfaceGrowthRate.GrowthRateOption.USER_SPECIFIED);
        sgr.getGrowthRateScalar().setValue(gr);
        _io.say.value("Surface Growth Rate", sgr.getGrowthRateScalar(), true);
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
        _set.object.physicalQuantity(sp.getFloor(), sf, _ud.defUnitLength,
                "Proximity Search Floor", true);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for an Auto Mesh Operation.
     *
     * @param amo given AutoMeshOperation.
     * @param min minimum relative size (%).
     * @param tgt target relative size (%).
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void surfaceSizes(AutoMeshOperation amo, double min, double tgt, boolean vo) {
        _io.say.action("Setting Mesh Surface Sizes", amo, vo);
        _set.object.relativeSize(_get.mesh.targetRelativeSize(amo, false),
                "Target Surface Size", tgt);
        _set.object.relativeSize(_get.mesh.minRelativeSize(amo, false),
                "Minimum Surface Size", min);
        _io.say.ok(vo);
    }

    /**
     * Specifies Surface Mesh Sizes for a Custom Mesh Control.
     *
     * @param cmc given Custom Mesh Control.
     * @param min minimum relative size (%) must be greater than zero. Otherwise it assumes the
     *            parent value.
     * @param tgt target relative size (%) must be greater than zero. Otherwise it assumes the
     *            parent value.
     * @param vo  given verbose option. False will only print necessary data.
     */
    public void surfaceSizes(CustomMeshControl cmc, double min, double tgt, boolean vo) {
        _io.say.action("Setting Custom Surface Sizes", cmc.getManager().getMeshOperation(), vo);
        _io.say.object(cmc, vo);
        CustomMeshControlConditionManager cm = cmc.getCustomConditions();
        if (min > 0) {
            cm.get(PartsMinimumSurfaceSizeOption.class)
                    .setSelected(PartsMinimumSurfaceSizeOption.Type.CUSTOM);
            _set.object.relativeSize(cmc.getCustomValues().get(PartsMinimumSurfaceSize.class),
                    "Minimum Surface Size", min);
        }
        if (tgt > 0) {
            cm.get(PartsTargetSurfaceSizeOption.class)
                    .setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
            _set.object.relativeSize(cmc.getCustomValues().get(PartsTargetSurfaceSize.class),
                    "Target Surface Size", tgt);
        }
        _io.say.ok(vo);
    }

    /**
     * Specifies Thin Mesher parameters.
     *
     * @param amo       given Automated Mesh Operation.
     * @param numLayers given number of prisms.
     * @param thicknThr given Custom Thickness Threshold Size in (%).
     * @param vo        given verbose option. False will only print necessary data.
     */
    public void thinMesher(AutoMeshOperation amo, int numLayers, double thicknThr, boolean vo) {
        _io.say.action("Setting Thin Mesh Layers", vo);
        _io.say.object(amo, vo);
        if (!_chk.has.thinMesher(amo)) {
            _io.say.msg("Mesh Operation does not have Thin Mesher.", vo);
            return;
        }
        AutoMeshDefaultValuesManager amdvm = amo.getDefaultValues();
        ThinAutoMesher tam = (ThinAutoMesher) amo.getMeshers().getObject("Thin Mesher");
        tam.setCustomizeThicknessOption(true);
        ThinNumLayers tnl = amdvm.get(ThinNumLayers.class);
        tnl.setLayers(Math.max(2, numLayers));
        _io.say.value("Number of Thin Layers", tnl.getLayers(), true);
        _set.object.relativeSize(amdvm.get(ThinThicknessThreshold.class),
                "Thin Layer Thickness Threshold", thicknThr);
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

    private DirectedAutoSourceMesh _getDirectedAutoSourceMesh(NamedObject no) {
        DirectedMeshOperation dmo = (DirectedMeshOperation) no;
        for (Object o : dmo.getGuidedSurfaceMeshBaseManager().getObjects()) {
            if (o instanceof DirectedAutoSourceMesh) {
                return (DirectedAutoSourceMesh) o;
            }
        }
        return null;
    }

    private void _settingPrisms(ClientServerObject cso, boolean vo) {
        _io.say.action("Setting Prism Layers", vo);
        _io.say.object(cso, vo);
    }

}
