package macroutils.creator;

import java.util.ArrayList;
import java.util.Vector;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NeoProperty;
import star.base.neo.StringVector;
import star.common.CoordinateSystem;
import star.common.CylindricalCoordinateSystem;
import star.common.GeometryObject;
import star.common.GeometryPart;
import star.common.PartCurve;
import star.common.PartSurface;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.Units;
import star.common.Vector3;
import star.dualmesher.DualAutoMesher;
import star.dualmesher.VolumeControlDualMesherSizeOption;
import star.meshing.AutoMeshDefaultValuesManager;
import star.meshing.AutoMeshOperation;
import star.meshing.BaseSize;
import star.meshing.BoundedShapeCentroidOffset;
import star.meshing.BoundedShapeConstantFactorInflation;
import star.meshing.BoundedShapeControlsManager;
import star.meshing.BoundedShapeCreatingOperation;
import star.meshing.BoxShapeIndividualOffsetsInflation;
import star.meshing.BoxShapeInflationControl;
import star.meshing.CadTessellationOption;
import star.meshing.CustomMeshControlConditionManager;
import star.meshing.CustomMeshControlValueManager;
import star.meshing.ExtractVolumeOperation;
import star.meshing.FillHolesOperation;
import star.meshing.ImprintMergeImprintMethodOption;
import star.meshing.ImprintPartSurfaces;
import star.meshing.ImprintPartSurfacesOption;
import star.meshing.ImprintPartsOperation;
import star.meshing.ImprintResultingMeshTypeOption;
import star.meshing.MaximumCellSize;
import star.meshing.MeshOperation;
import star.meshing.MeshOperationManager;
import star.meshing.MeshOperationPart;
import star.meshing.MeshPart;
import star.meshing.PartsMinimumSurfaceSize;
import star.meshing.PartsTargetSurfaceSize;
import star.meshing.PartsTargetSurfaceSizeOption;
import star.meshing.PrepareFor2dOperation;
import star.meshing.SubtractPartsOperation;
import star.meshing.SurfaceCurvature;
import star.meshing.SurfaceCustomMeshControl;
import star.meshing.SurfaceProximity;
import star.meshing.UnitePartsOperation;
import star.meshing.VolumeControlSize;
import star.meshing.VolumeCustomMeshControl;
import star.prismmesher.CustomPrismValuesManager;
import star.prismmesher.NumPrismLayers;
import star.prismmesher.PartsCustomPrismsOption;
import star.prismmesher.PartsCustomizePrismMesh;
import star.prismmesher.PrismLayerStretching;
import star.prismmesher.PrismThickness;
import star.surfacewrapper.GeometricFeatureAngle;
import star.surfacewrapper.GlobalVolumeOfInterest;
import star.surfacewrapper.GlobalVolumeOfInterestOption;
import star.surfacewrapper.PartsContactPreventionSetManager;
import star.surfacewrapper.PartsOneGroupContactPreventionSet;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;
import star.sweptmesher.DirectedAutoSourceMesh;
import star.sweptmesher.DirectedMeshDistribution;
import star.sweptmesher.DirectedMeshDistributionManager;
import star.sweptmesher.DirectedMeshNumLayers;
import star.sweptmesher.DirectedMeshOperation;
import star.sweptmesher.DirectedMeshPartCollection;
import star.sweptmesher.DirectedMeshPartCollectionManager;
import star.sweptmesher.DirectedPatchSourceMesh;
import star.sweptmesher.DirectedSurfaceMeshBaseManager;
import star.sweptmesher.PatchCurve;
import star.sweptmesher.PatchVertex;
import star.trimmer.PartsSimpleTemplateGrowthRate;
import star.trimmer.TrimmerAnisotropicSize;
import star.trimmer.VolumeControlTrimmerSizeOption;

/**
 * Low-level class for creating Mesh Operations with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateMeshOperation {

    private macroutils.checker.MainChecker _chk = null;
    private macroutils.misc.MainDisabler _dis = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private macroutils.setter.MainSetter _set = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateMeshOperation(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Creates an Automated Mesh Mesh Operation for the given Geometry Parts.
     *
     * @param ag given ArrayList of Geometry Parts.
     * @param am given ArrayList of Meshers. <u>Hint</u>: use with
     *           {@link macroutils.getter.GetStrings#meshers}.
     * @return The AutoMeshOperation.
     */
    public AutoMeshOperation automatedMesh(ArrayList<GeometryPart> ag, ArrayList<String> am) {
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        AutoMeshOperation amo = mom.createAutoMeshOperation(am, ag);
        _setAutomatedMesh(amo, ag, "");
        return amo;
    }

    /**
     * Creates an Automated Mesh Mesh Operation for the given Geometry Parts.
     *
     * @param ag      given ArrayList of Geometry Parts.
     * @param meshers given meshers, separated by comma. See {@link macroutils.StaticDeclarations}
     *                for options.
     * @return The AutoMeshOperation.
     */
    public AutoMeshOperation automatedMesh(ArrayList<GeometryPart> ag,
            StaticDeclarations.Meshers... meshers) {
        return automatedMesh(ag, _get.strings.meshers(meshers));
    }

    /**
     * Creates a Badge for 2D Mesh Operation on a set of Geometry Parts.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @return The PrepareFor2dOperation.
     */
    public PrepareFor2dOperation badgeFor2D(ArrayList<GeometryPart> agp) {
        _io.say.action("Creating a Badge for 2D Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        PrepareFor2dOperation p2d = (PrepareFor2dOperation) mom.createPrepareFor2dOperation(agp);
        p2d.execute();
        _io.say.created(p2d, true);
        return p2d;
    }

    /**
     * Creates a Bounded Shape Block with individual offsets.
     *
     * @param agp            given ArrayList of Geometry Parts.
     * @param negOffset      given negative 3-components offset array using
     *                       {@link UserDeclarations#defUnitLength} units.
     * @param posOffset      given positive 3-components offset array using
     *                       {@link UserDeclarations#defUnitLength} units.
     * @param centroidOffset given 3-components offset array for the centroid of supplied Geometry
     *                       Parts.
     * @return The MeshOperationPart.
     */
    public MeshOperationPart boundedShape_Block(ArrayList<GeometryPart> agp, double[] negOffset,
            double[] posOffset, double[] centroidOffset) {
        Units u = _ud.defUnitLength;
        _io.say.action("Creating a Block Bounded Shape Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        BoundedShapeCreatingOperation bsmo = _createBoundedShapeMeshOp(agp,
                BoundedShapeCreatingOperation.OutputPartType.BLOCK);
        BoxShapeInflationControl bsic = bsmo.getBoundedShapeValuesManager()
                .get(BoxShapeInflationControl.class);
        bsic.setInflationMode(BoxShapeInflationControl.InflationMode.INDIVIDUAL_OFFSETS);
        BoxShapeIndividualOffsetsInflation bsioi = bsic.getIndividualInflationOffsets();
        _set.object.physicalQuantity(bsioi.getNXOffset(), negOffset[0], u, "-X Offset", true);
        _set.object.physicalQuantity(bsioi.getNYOffset(), negOffset[1], u, "-Y Offset", true);
        _set.object.physicalQuantity(bsioi.getNZOffset(), negOffset[2], u, "-Z Offset", true);
        _set.object.physicalQuantity(bsioi.getPXOffset(), posOffset[0], u, "+X Offset", true);
        _set.object.physicalQuantity(bsioi.getPYOffset(), posOffset[1], u, "+Y Offset", true);
        _set.object.physicalQuantity(bsioi.getPZOffset(), posOffset[2], u, "+Z Offset", true);
        _setCentroidOffset(bsmo, centroidOffset);
        bsmo.execute();
        String opName = _get.strings.withinTheBrackets(bsmo.getOutputPartNames());
        _io.say.created(bsmo, true);
        return (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart(opName);
    }

    /**
     * Creates a Bounded Shape Sphere with individual offsets.
     *
     * @param agp            given ArrayList of Geometry Parts.
     * @param factor         given inflation factor w.r.t. centroid supplied as Parts.
     * @param centroidOffset given 3-components offset array for the centroid of supplied Geometry
     *                       Parts.
     * @return The MeshOperationPart.
     */
    public MeshOperationPart boundedShape_Sphere(ArrayList<GeometryPart> agp, double factor,
            double[] centroidOffset) {
        Units u = _ud.defUnitLength;
        _io.say.action("Creating a Block Bounded Shape Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        BoundedShapeCreatingOperation bsmo = _createBoundedShapeMeshOp(agp,
                BoundedShapeCreatingOperation.OutputPartType.SPHERE);
        BoundedShapeControlsManager bscm = bsmo.getBoundedShapeValuesManager();
        bscm.get(BoundedShapeConstantFactorInflation.class).setInflationFactor(factor);
        _io.say.value("Inflation Factor", factor, true);
        _setCentroidOffset(bsmo, centroidOffset);
        bsmo.execute();
        String opName = _get.strings.withinTheBrackets(bsmo.getOutputPartNames());
        _io.say.created(bsmo, true);
        return (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart(opName);
    }

    /**
     * Creates a One Group Contact Prevention with the supplied Geometry Objects.
     *
     * @param mo  given Mesh Operation.
     * @param ago given ArrayList of Geometry Objects.
     * @param val given search floor value.
     * @param u   given units for the search floor.
     * @return The PartsOneGroupContactPreventionSet.
     */
    public PartsOneGroupContactPreventionSet contactPrevention(MeshOperation mo,
            ArrayList<GeometryObject> ago, double val, Units u) {
        _io.say.action("Creating a Contact Prevention between Object Parts", true);
        _io.say.object(mo, true);
        _io.say.objects(ago, "Geometry Objects", true);
        if (!_chk.is.surfaceWrapperOperation(mo)) {
            _io.say.msg(true, "This is not a Surface Wrapper Mesh Operation. Skipping...");
            return null;
        }
        SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) mo;
        PartsContactPreventionSetManager pcpsm = swamo.getContactPreventionSet();
        PartsOneGroupContactPreventionSet cp = pcpsm.createPartsOneGroupContactPreventionSet();
        cp.getPartSurfaceGroup().setObjects(ago);
        _set.object.physicalQuantity(cp.getFloor(), val, u, "Search Floor", true);
        _io.say.ok(true);
        return cp;
    }

    /**
     * Creates a Directed Mesh Operation using an Automated 2D Mesh.
     *
     * @param src     given Source Part Surface.
     * @param tgt     given Target Part Surface.
     * @param meshers given meshers, separated by comma. See {@link macroutils.StaticDeclarations}
     *                for options.
     * @param nVol    given number of layers in volume distribution.
     * @return The DirectedMeshOperation.
     */
    public DirectedMeshOperation directedMeshing_AutoMesh(PartSurface src, PartSurface tgt,
            int nVol, StaticDeclarations.Meshers... meshers) {
        _io.say.action("Creating a Directed Mesh Operation with an Automated 2D Mesh", true);
        _io.say.object(src, true);
        _io.say.object(tgt, true);
        _io.say.msg(true, "Number of Layers: %d.", nVol);
        DirectedMeshOperation dmo = _createDirectedMeshOperation(src, tgt);
        //--
        String pn = src.getPart().getPresentationName();
        DirectedMeshPartCollectionManager dmpcm = dmo.getGuidedMeshPartCollectionManager();
        DirectedMeshPartCollection dmpc = ((DirectedMeshPartCollection) dmpcm.getObject(pn));
        DirectedSurfaceMeshBaseManager dsmbm = dmo.getGuidedSurfaceMeshBaseManager();
        dsmbm.createAutoSourceMesh(_get.strings.meshers(meshers), _get.objects.arrayList(dmpc));
        DirectedAutoSourceMesh dasm = (DirectedAutoSourceMesh) dsmbm.getObjects().iterator().next();
        _setMeshDefaults(dasm.getDefaultValues());
        _setWorkAroundAutoSourceMesh(dasm, src.getPart());
        DirectedMeshDistributionManager dmdm = dmo.getDirectedMeshDistributionManager();
        DirectedMeshDistribution dmd = dmdm
                .createDirectedMeshDistribution(new Vector<>(_get.objects.arrayList(dmpc)),
                        "Constant");
        dmd.getDefaultValues().get(DirectedMeshNumLayers.class).setNumLayers(nVol);
        dmo.execute();
        _io.say.created(dmo, true);
        return dmo;
    }

    /**
     * Creates a Directed Mesh Operation in a squared Channel.
     *
     * @param src given Source Part Surface.
     * @param tgt given Target Part Surface.
     * @param nX  given number of points in X-direction.
     * @param nY  given number of points in Y-direction.
     * @param nZ  given number of points in Z-direction.
     * @return The DirectedMeshOperation.
     */
    public DirectedMeshOperation directedMeshing_Channel(PartSurface src, PartSurface tgt,
            int nX, int nY, int nZ) {
        _io.say.action("Creating a Directed Mesh Operation in a Channel", true);
        _io.say.msg("Number of Elements:");
        _io.say.msg(true, "  - X Direction: %d;", nX);
        _io.say.msg(true, "  - Y Direction: %d;", nY);
        _io.say.msg(true, "  - Z Direction: %d.", nZ);
        DirectedMeshOperation dmo = _createDirectedMeshOperation(src, tgt);
        int isX = 0, isY = 0, isZ = 0;
        int nP1 = 2, nP2 = 2, nVol = 2;
        PatchCurve pcX = null, pcY = null, pcZ = null, pc1 = null, pc2 = null;
        //--
        String pn = src.getPart().getPresentationName();
        DirectedMeshPartCollectionManager dmpcm = dmo.getGuidedMeshPartCollectionManager();
        DirectedMeshPartCollection dmpc = ((DirectedMeshPartCollection) dmpcm.getObject(pn));
        Vector<PartSurface> vpsSrc = new Vector<>(_get.objects.arrayList(src));
        Vector<PartSurface> vpsTgt = new Vector<>(_get.objects.arrayList(tgt));
        dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc,
                vpsSrc, vpsTgt);
        //--
        DirectedPatchSourceMesh patchMsh = dmo.getGuidedSurfaceMeshBaseManager()
                .createPatchSourceMesh(vpsSrc, dmpc);
        NeoProperty np = patchMsh.autopopulateFeatureEdges();
        ArrayList<PatchCurve> pcs = _getPatchCurves(patchMsh);
        //--
        double err = 0.05;
        for (PatchCurve p : pcs) {
            DoubleVector pts = p.getPoints();
            if (_get.info.relativeError(pts.get(0), pts.get(3), true) <= err) {
                isX += 1;
                pcX = p;
                _io.say.msg(p.getPresentationName() + " is on X plane.");
            }
            if (_get.info.relativeError(pts.get(1), pts.get(4), true) <= err) {
                isY += 1;
                pcY = p;
                _io.say.msg(p.getPresentationName() + " is on Y plane.");
            }
            if (_get.info.relativeError(pts.get(2), pts.get(5), true) <= err) {
                isZ += 1;
                pcZ = p;
                _io.say.msg(p.getPresentationName() + " is on Z plane.");
            }
        }
        //_io.say.msg("X = %d; Y = %d; Z = %d.", isX, isY, isZ);
        //--
        if (isX == 4) {
            nVol = nX;
            pc1 = pcY;
            nP2 = nY;
            pc2 = pcZ;
            nP1 = nZ;
        } else if (isY == 4) {
            nVol = nY;
            pc1 = pcX;
            nP2 = nX;
            pc2 = pcZ;
            nP1 = nZ;
        } else if (isZ == 4) {
            nVol = nZ;
            pc1 = pcX;
            nP2 = nX;
            pc2 = pcY;
            nP1 = nY;
        }
        //--
        patchMsh.defineMeshPatchCurve(pc1, pc1.getStretchingFunction(), 0., 0., nP1, false, false);
        patchMsh.defineMeshPatchCurve(pc2, pc2.getStretchingFunction(), 0., 0., nP2, false, false);
        //--
        DirectedMeshDistributionManager dmdm = dmo.getDirectedMeshDistributionManager();
        DirectedMeshDistribution dmd = dmdm
                .createDirectedMeshDistribution(new Vector<>(_get.objects.arrayList(dmpc)),
                        "Constant");
        dmd.getDefaultValues().get(DirectedMeshNumLayers.class).setNumLayers(nVol);
        dmo.execute();
        _io.say.created(dmo, true);
        return dmo;
    }

    /**
     * Creates a Directed Mesh Operation in a Pipe, using an O-Grid structure.
     *
     * @param src  given Source Part Surface.
     * @param tgt  given Target Part Surface.
     * @param nT   given number of points in the circumference, i.e., Theta direction.
     * @param nR   given number of points radially.
     * @param nVol given number of points for the volume distribution.
     * @param rR   given r/R distance for the O-Grid. E.x.: 0.5;
     * @param c    given Cylindrical Coordinate System.
     * @return The DirectedMeshOperation.
     */
    public DirectedMeshOperation directedMeshing_Pipe(PartSurface src, PartSurface tgt,
            int nT, int nR, int nVol, double rR, CoordinateSystem c) {
        _io.say.action("Creating a Directed Mesh Operation in a Pipe", true);
        if (!_chk.is.cylindricalCSYS(c)) {
            _io.say.value("Warning! Not a Cylindrical Coordinate System", c.getPresentationName(),
                    true, true);
            _io.say.msg("Directed Mesh not created.");
            return null;
        }
        _io.say.msg("Number of Elements:");
        _io.say.msg(true, "  - Tangent Direction (theta): %d;", nT);
        _io.say.msg(true, "  - Radial Direction (r): %d;", nR);
        _io.say.msg(true, "  - Along Pipe (axially): %d.", nVol);
        DirectedMeshOperation dmo = _createDirectedMeshOperation(src, tgt);
        CylindricalCoordinateSystem ccs = (CylindricalCoordinateSystem) c;
        //--
        String s = src.getPart().getPresentationName();
        DirectedMeshPartCollection dmpc = dmo.getGuidedMeshPartCollectionManager().getObject(s);
        Vector<PartSurface> vpsSrc = new Vector<>(_get.objects.arrayList(src));
        Vector<PartSurface> vpsTgt = new Vector<>(_get.objects.arrayList(tgt));
        dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc,
                vpsSrc, vpsTgt);
        //--
        DirectedPatchSourceMesh patchMsh = dmo.getGuidedSurfaceMeshBaseManager()
                .createPatchSourceMesh(vpsSrc, dmpc);
        NeoProperty np = patchMsh.autopopulateFeatureEdges();
        //_io.say.msg("NeoProperty np = patchMsh.autopopulateFeatureEdges();");
        //_io.say.msg(np.getHashtable().toString());
        //--
        boolean isBackwards = _meshPipe_buildExternalPatchCurves(patchMsh, ccs, false);
        if (isBackwards) {
            _meshPipe_buildExternalPatchCurves(patchMsh, ccs, isBackwards);
        }
        ArrayList<PatchCurve> pcExts = _getPatchCurves(patchMsh);
        PatchCurve pcInt = _meshPipe_buildInternalPatchCurves(patchMsh, ccs, rR, isBackwards);
        //--
        if (pcInt == null) {
            return null;
        }
        //--
        ArrayList<PatchCurve> pcInts = new ArrayList<>();
        pcInts.add(pcInt);
        patchMsh.defineMeshMultiplePatchCurves(new Vector<>(pcExts), nT, false);
        patchMsh.defineMeshMultiplePatchCurves(new Vector<>(pcInts), nR, false);
        if (_ud.dmSmooths > 0) {
            patchMsh.smoothPatchPolygonMesh(_ud.dmSmooths, 0.25, false);
        }
        //--
        DirectedMeshDistributionManager dmdm = dmo.getDirectedMeshDistributionManager();
        DirectedMeshDistribution dmd = dmdm
                .createDirectedMeshDistribution(new Vector<>(_get.objects.arrayList(dmpc)),
                        "Constant");
        dmd.getDefaultValues().get(DirectedMeshNumLayers.class).setNumLayers(nVol);
        dmo.execute();
        _io.say.created(dmo, true);
        return dmo;
    }

    /**
     * Creates a Extract Volume Mesh Operation on the supplied geometries.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @return The ExtractVolumeOperation.
     */
    public ExtractVolumeOperation extractVolume(ArrayList<GeometryPart> agp) {
        _io.say.action("Creating a Extract Volume Operation", true);
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        MeshOperation mo = mom.createExtractVolumeOperation(agp);
        _io.say.objects(agp, "Geometry Parts", true);
        mo.execute();
        _io.say.ok(true);
        return (ExtractVolumeOperation) mo;
    }

    /**
     * Creates a Fill Holes Mesh Operation on the supplied geometry objects.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param aps given ArrayList of Part Surfaces.
     * @param apc given ArrayList of Part Curves. null is ignored.
     * @return The FillHolesOperation.
     */
    public FillHolesOperation fillHoles(ArrayList<GeometryPart> agp, ArrayList<PartSurface> aps,
            ArrayList<PartCurve> apc) {
        _io.say.action("Creating a Fill Holes Operation", true);
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        FillHolesOperation fho = (FillHolesOperation) mom.createFillHolesOperation(agp);
        _io.say.objects(agp, "Geometry Parts", true);
        _io.say.objects(aps, "Part Surfaces", true);
        fho.getEndSurfaces().setObjects(aps);
        if (apc != null) {
            _io.say.objects(aps, "Part Curves", true);
            fho.getEndCurves().setObjects(apc);
        }
        fho.execute();
        _io.say.ok(true);
        return fho;
    }

    /**
     * Creates an Imprint Mesh Operation with the given Parts.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param tol given tolerance in {@link UserDeclarations#defUnitLength} unit.
     * @param it  given Imprint Method.
     * @param mt  given Resulting Mesh Type
     * @return The ImprintPartsOperation.
     */
    public ImprintPartsOperation imprint(ArrayList<GeometryPart> agp, double tol,
            ImprintMergeImprintMethodOption.Type it, ImprintResultingMeshTypeOption.Type mt) {
        _io.say.action("Creating an Imprint Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        MeshOperation mo = mom.createImprintPartsOperation(agp);
        ImprintPartsOperation ipo = (ImprintPartsOperation) mo;
        ipo.getMergeImprintMethod().setSelected(it);
        ipo.getResultingMeshType().setSelected(mt);
        _io.say.value("Imprint Method",
                ipo.getMergeImprintMethod().getSelectedElement().getPresentationName(), true, true);
        _io.say.value("Resulting Mesh",
                ipo.getResultingMeshType().getSelectedElement().getPresentationName(), true, true);
        _set.object.physicalQuantity(ipo.getTolerance(), tol, _ud.defUnitLength, "Tolerance", true);
        if (it == ImprintMergeImprintMethodOption.Type.CAD_IMPRINT) {
            ipo.getImprintValuesManager().get(CadTessellationOption.class)
                    .getTessellationDensityOption().setSelected(_ud.defTessOpt.getType());
        }
        if (mt == ImprintResultingMeshTypeOption.Type.CONFORMAL) {
            ipo.getImprintValuesManager().get(ImprintPartSurfaces.class)
                    .getPartSurfacesOption().setSelected(ImprintPartSurfacesOption.Type.USE_INPUT);
        }
        _io.say.created(ipo, true);
        return ipo;
    }

    /**
     * Creates a Subtraction Mesh Operation between a set of Geometry Parts.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @param tgt given target Geometry Part.
     * @return The MeshOperationPart.
     */
    public MeshOperationPart subtract(ArrayList<GeometryPart> agp, GeometryPart tgt) {
        return _createBoolean(StaticDeclarations.Operation.SUBTRACT, agp, tgt);
    }

    /**
     * Creates an empty Custom Surface Control.
     *
     * @param mo given Mesh Operation.
     * @return The SurfaceCustomMeshControl.
     */
    public SurfaceCustomMeshControl surfaceControl(MeshOperation mo) {
        return _createSurfaceCustomMeshControl(mo, true);
    }

    /**
     * Copies a Custom Surface Control from another another Mesh Operation.
     *
     * @param scmc given Surface Control to copy.
     * @param mo   given Mesh Operation where will be copied too.
     * @return The SurfaceCustomMeshControl.
     */
    public SurfaceCustomMeshControl surfaceControl(SurfaceCustomMeshControl scmc,
            MeshOperation mo) {
        _io.say.action("Creating a Custom Surface Mesh Control", true);
        _io.say.object(scmc, true);
        _io.say.object(mo, true);
        SurfaceCustomMeshControl scmc2 = _createSurfaceCustomMeshControl(mo, false);
        scmc2.setPresentationName(scmc.getPresentationName());
        scmc2.copyProperties(scmc);
        _io.say.msg("Properties copied succesfully.");
        _io.say.created(scmc2, true);
        return scmc2;
    }

    /**
     * Creates a Custom Surface Control to change Surface Sizes in a Mesh Operation.
     *
     * @param mo  given Mesh Operation.
     * @param ago given ArrayList of Geometry Objects.
     * @param min minimum relative size (%). If 0, this parameter will not be customized.
     * @param tgt target relative size (%). If 0, this parameter will not be customized.
     * @return The Custom Surface Control.
     */
    public SurfaceCustomMeshControl surfaceControl(MeshOperation mo, ArrayList<GeometryObject> ago,
            double min, double tgt) {
        _io.say.action("Creating a Custom Surface Mesh Control", true);
        _io.say.object(mo, true);
        _io.say.objects(ago, "Geometry Objects", true);
        SurfaceCustomMeshControl scmc = _createSurfaceCustomMeshControl(mo, false);
        scmc.getGeometryObjects().setObjects(ago);
        _set.mesh.surfaceSizes(scmc, min, tgt, true);
        _io.say.created(scmc, true);
        return scmc;
    }

    /**
     * Creates a Custom Surface Control to change Prism Layers in a Mesh Operation.
     *
     * <b>Important:</b> If all three arguments are 0, Prisms will be disabled.
     *
     * @param amo       given Auto Mesh Operation.
     * @param ago       given ArrayList of Geometry Objects.
     * @param numLayers given number of prisms. If 0, this parameter will not be customized.
     * @param stretch   given prism stretch relation. If 0, this parameter will not be customized.
     * @param relSize   given relative size in (%). If 0, this parameter will not be customized.
     * @return The Custom Surface Control.
     */
    public SurfaceCustomMeshControl surfaceControl(AutoMeshOperation amo,
            ArrayList<GeometryObject> ago, int numLayers, double stretch, double relSize) {
        _io.say.action("Creating a Custom Surface Mesh Control", true);
        _io.say.object(amo, true);
        _io.say.objects(ago, "Geometry Objects", true);
        SurfaceCustomMeshControl scmc = amo.getCustomMeshControls().createSurfaceControl();
        scmc.getGeometryObjects().setObjects(ago);
        PartsCustomizePrismMesh pcpm = scmc.getCustomConditions()
                .get(PartsCustomizePrismMesh.class);
        if (numLayers + stretch + relSize == 0.0) {
            _dis.prismsLayers(scmc, false);
            _io.say.msg("Prism Layers DISABLED.");
        } else {
            pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
            CustomPrismValuesManager cpvm = scmc.getCustomValues()
                    .get(CustomPrismValuesManager.class);
            if (numLayers > 0) {
                pcpm.getCustomPrismControls().setCustomizeNumLayers(true);
                cpvm.get(NumPrismLayers.class).setNumLayers(numLayers);
            } else {
                pcpm.getCustomPrismControls().setCustomizeNumLayers(false);
            }
            if (stretch > 0) {
                pcpm.getCustomPrismControls().setCustomizeStretching(true);
                cpvm.get(PrismLayerStretching.class).setStretching(stretch);
            } else {
                pcpm.getCustomPrismControls().setCustomizeStretching(false);
            }
            if (relSize > 0) {
                pcpm.getCustomPrismControls().setCustomizeTotalThickness(true);
                cpvm.get(PrismThickness.class).setRelativeSize(relSize);
            } else {
                pcpm.getCustomPrismControls().setCustomizeTotalThickness(false);
            }
        }
        _io.say.created(scmc, true);
        return scmc;
    }

    /**
     * Creates a Surface Wrap Mesh Operation in a set of Geometry Parts.
     *
     * @param agp  given ArrayList of Geometry Parts.
     * @param name given name for the Operation. The Part generated will share the same name.
     * @return The SurfaceWrapperAutoMeshOperation.
     */
    public SurfaceWrapperAutoMeshOperation surfaceWrapper(ArrayList<GeometryPart> agp,
            String name) {
        _io.say.action("Creating a Surface Wrapper Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        AutoMeshOperation amo = mom.createSurfaceWrapperAutoMeshOperation(agp, name);
        SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) amo;
        AutoMeshDefaultValuesManager amdvm = swamo.getDefaultValues();
        _set.mesh.baseSize(swamo, _ud.mshBaseSize, _ud.defUnitLength, false);
        _set.mesh.surfaceSizes(swamo, _ud.mshSrfSizeMin, _ud.mshSrfSizeTgt, false);
        _set.mesh.surfaceCurvature(amdvm.get(SurfaceCurvature.class), _ud.mshSrfCurvNumPoints,
                false);
        GlobalVolumeOfInterestOption gvio = amdvm.get(GlobalVolumeOfInterest.class)
                .getVolumeOfInterestOption();
        GeometricFeatureAngle gfa = amdvm.get(GeometricFeatureAngle.class);
        gvio.setSelected(GlobalVolumeOfInterestOption.Type.LARGEST_INTERNAL);
        gfa.setGeometricFeatureAngle(_ud.mshWrapperFeatureAngle);
        _io.say.value("Volume of Interest", gvio.getSelectedElement().getPresentationName(), true,
                true);
        _io.say.value("Geometric Feature Angle", gfa.getGeometricFeatureAngle(), true);
        _io.say.created(swamo, true);
        return swamo;
    }

    /**
     * Creates an Unite Mesh Operation between a set of Geometry Parts.
     *
     * @param agp given ArrayList of Geometry Parts.
     * @return The MeshOperationPart.
     */
    public MeshOperationPart unite(ArrayList<GeometryPart> agp) {
        return _createBoolean(StaticDeclarations.Operation.UNITE, agp, null);
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _chk = _mu.check;
        _dis = _mu.disable;
        _get = _mu.get;
        _io = _mu.io;
        _set = _mu.set;
        _ud = _mu.userDeclarations;
    }

    /**
     * Creates a Custom Volumetric Control with isotropic values in a Mesh Operation.
     *
     * @param mo      given Mesh Operation.
     * @param agp     given ArrayList of Geometry Parts.
     * @param relSize relative size in (<b>%</b>). Zero is ignored.
     * @return The VolumeCustomMeshControl.
     */
    public VolumeCustomMeshControl volumetricControl(MeshOperation mo, ArrayList<GeometryPart> agp,
            double relSize) {
        return volumetricControl(mo, agp, relSize, StaticDeclarations.COORD0);
    }

    /**
     * Creates a Custom Volumetric Control in a Mesh Operation where the control can be Isotropic or
     * Anisotropic (Trimmer only).
     *
     * @param mo       given Mesh Operation.
     * @param agp      given ArrayList of Geometry Parts.
     * @param relSize  relative size in (<b>%</b>). Zero is ignored.
     * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will
     *                 be ignored.
     * @return The VolumeCustomMeshControl.
     */
    public VolumeCustomMeshControl volumetricControl(MeshOperation mo, ArrayList<GeometryPart> agp,
            double relSize, double[] relSizes) {
        _io.say.action("Creating a Custom Volume Mesh Control", true);
        _io.say.object(mo, true);
        _io.say.objects(agp, "Geometry Parts", true);
        if (!_isCustomControllable(mo)) {
            return null;
        }
        AutoMeshOperation amo = (AutoMeshOperation) mo;
        VolumeCustomMeshControl vcmc = amo.getCustomMeshControls().createVolumeControl();
        vcmc.getGeometryObjects().setObjects(agp);
        if (relSize > 0) {
            _setIsotropicSize(vcmc, relSize);
        }
        if (_chk.has.trimmerMesher(amo) && relSizes.length == 3 && _get.info.sum(relSizes) > 0) {
            _setAnisotropicSizes(vcmc, relSizes);
        }
        _io.say.created(vcmc, true);
        return vcmc;
    }

    private MeshOperationPart _createBoolean(StaticDeclarations.Operation op,
            ArrayList<GeometryPart> ag, GeometryPart tgtGP) {
        _io.say.action(String.format("Creating a %s Mesh Operation", op.toString()), true);
        _io.say.objects(ag, "Parts", true);
        MeshOperation mo = _createBooleanMeshOperation(op, tgtGP);
        mo.getInputGeometryObjects().setObjects(ag);
        mo.execute();
        String opName = _get.strings.withinTheBrackets(mo.getOutputPartNames());
        return (MeshOperationPart) _sim.get(SimulationPartManager.class).getPart(opName);
    }

    private MeshOperation _createBooleanMeshOperation(StaticDeclarations.Operation op,
            GeometryPart tgtGP) {
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        switch (op) {
            case SUBTRACT:
                SubtractPartsOperation spo = (SubtractPartsOperation) mom
                        .createSubtractPartsOperation();
                spo.setTargetPart((MeshPart) tgtGP);
                return spo;
            case UNITE:
                UnitePartsOperation upo = (UnitePartsOperation) mom.createUnitePartsOperation();
                return upo;
        }
        return null;
    }

    private BoundedShapeCreatingOperation _createBoundedShapeMeshOp(ArrayList<GeometryPart> agp,
            BoundedShapeCreatingOperation.OutputPartType type) {
        MeshOperation mo = _sim.get(MeshOperationManager.class).createBoundedShapeOperation(agp);
        BoundedShapeCreatingOperation bsco = (BoundedShapeCreatingOperation) mo;
        bsco.setOutputPartType(type);
        return bsco;
    }

    private DirectedMeshOperation _createDirectedMeshOperation(PartSurface src, PartSurface tgt) {
        _io.say.object(src.getPart(), true);
        if (!_chk.is.directedMeshable(src, tgt)) {
            return null;
        }
        MeshOperationManager mom = _sim.get(MeshOperationManager.class);
        MeshOperation mo = mom.createDirectedMeshOperation(
                new ArrayList<>(_get.objects.arrayList(src.getPart())));
        DirectedMeshOperation dmo = (DirectedMeshOperation) mo;
        dmo.getSourceSurfaceGroup().add(src);
        dmo.getTargetSurfaceGroup().add(tgt);
        return dmo;
    }

    private SurfaceCustomMeshControl _createSurfaceCustomMeshControl(MeshOperation mo, boolean vo) {
        _io.say.action("Creating a Custom Surface Mesh Control", vo);
        _io.say.object(mo, vo);
        if (!_isCustomControllable(mo)) {
            return null;
        }
        AutoMeshOperation amo = (AutoMeshOperation) mo;
        SurfaceCustomMeshControl scmc = amo.getCustomMeshControls().createSurfaceControl();
        _io.say.created(scmc, vo);
        return scmc;
    }

    private Object _getNewObject(ArrayList objOld, ArrayList objNew) {
        for (Object o : objNew) {
            if (!objOld.contains(o)) {
                return o;
            }
        }
        return null;
    }

    private ArrayList<PatchCurve> _getPatchCurves(DirectedPatchSourceMesh patchMsh) {
        return new ArrayList<>(patchMsh.getPatchCurveManager().getObjects());
    }

    private ArrayList<PatchVertex> _getPatchVertices(DirectedPatchSourceMesh patchMsh) {
        return new ArrayList<>(patchMsh.getPatchVertexManager().getObjects());
    }

    private double _getRadius(DirectedPatchSourceMesh patchMsh, CylindricalCoordinateSystem c) {
        double maxR = 0.;
        for (PatchVertex pv : _getPatchVertices(patchMsh)) {
            Vector3 xyz = _getVector3(pv, c);
            //_io.say.msg(pv.getPresentationName() + " in Cyl CSYS: " + xyz.toString());
            maxR = Math.max(maxR, xyz.x);
        }
        _io.say.value("Pipe Radius", maxR, _ud.unit_m, true);
        return maxR;
    }

    private Vector3 _getVector3(PatchVertex pv, CylindricalCoordinateSystem c) {
        return c.transformLabCoordinate(new Vector3(pv.getCoordinate().toDoubleArray()));
    }

    private boolean _isCustomControllable(MeshOperation mo) {
        if (_chk.is.autoMeshOperation(mo) || _chk.is.surfaceWrapperOperation(mo)) {
            return true;
        }
        _io.say.msg(true, "This Mesh Operation can not have Custom Controls. Skipping...");
        return false;
    }

    private boolean _meshPipe_buildExternalPatchCurves(DirectedPatchSourceMesh patchMsh,
            CylindricalCoordinateSystem c, boolean backwards) {
        double r = _getRadius(patchMsh, c);
        _io.say.msg("Erasing original Patch Curves...");
        for (PatchCurve pc : _getPatchCurves(patchMsh)) {
            patchMsh.deletePatchCurve(pc);
        }
        _io.say.msg("Rebuilding external Patch Curves...");
        //-- Building is always clock-wise (0, 90, 180, 270)
        ArrayList<PatchVertex> placedVs = new ArrayList<>();
        Vector3 p1 = c.transformCoordinate(new Vector3(r, 0., 0.));
        Vector3 p2 = c.transformCoordinate(new Vector3(r, 90. / 180. * Math.PI, 0.));
        Vector3 p3 = c.transformCoordinate(new Vector3(r, Math.PI, 0));
        Vector3 p4 = c.transformCoordinate(new Vector3(r, 270. / 180. * Math.PI, 0.));
        double[] angles = { 0, 90, 180, 270 };
        if (backwards) {
            angles = new double[]{ 0, 270, 180, 90 };
            Vector3 p90 = new Vector3(p2);
            Vector3 p180 = new Vector3(p3);
            Vector3 p270 = new Vector3(p4);
            p2 = p270;
            p3 = p180;
            p4 = p90;
            _io.say.msg("Trying angles backwards order...");
        }
        DoubleVector dv = new DoubleVector();
        PatchVertex oldPv;
        for (int i = 0; i < angles.length - 1; i++) {
            _io.say.msg(true, "   Creating Curve: %.0f to %.0f...", angles[i], angles[i + 1]);
            switch (i) {
                case 0:
                    dv = new DoubleVector(new double[]{ p1.x, p1.y, p1.z, p2.x, p2.y, p2.z });
                    patchMsh.createPatchCurve(null, null, dv,
                            new StringVector(new String[]{ "ON_FEATURE_EDGE", "ON_FEATURE_EDGE" }));
                    placedVs.addAll(_getPatchVertices(patchMsh));
                    continue;
                case 1:
                    dv = new DoubleVector(new double[]{ p3.x, p3.y, p3.z });
                    break;
                case 2:
                    dv = new DoubleVector(new double[]{ p4.x, p4.y, p4.z });
                    break;
            }
            oldPv = placedVs.get(placedVs.size() - 1);
            patchMsh.createPatchCurve(oldPv, null, dv,
                    new StringVector(new String[]{ "ON_FEATURE_EDGE" }));
            for (PatchVertex pv : _getPatchVertices(patchMsh)) {
                if (!placedVs.contains(pv)) {
                    placedVs.add(pv);
                }
            }
        }
        dv.clear();
        _io.say.msg("   Creating Curve: 270 to 0...");
        patchMsh.createPatchCurve(placedVs.get(0), placedVs.get(placedVs.size() - 1), dv,
                new StringVector(new String[]{}));
        if (placedVs.size() == 4) {
            return false;
        }
        _io.say.msg("Number of Vertices are not 4. Trying backwards...");
        return true;
    }

    private PatchCurve _meshPipe_buildInternalPatchCurves(DirectedPatchSourceMesh patchMsh,
            CylindricalCoordinateSystem c,
            double rR, boolean backwards) {
        double r = _getRadius(patchMsh, c);
        final double toRad = Math.PI / 180.;
        DoubleVector dv = new DoubleVector();
        _io.say.msg("Building internal Patch Curves...");
        //-- Building is always clock-wise (0, 90, 180, 270)
        ArrayList<PatchCurve> placedCs = _getPatchCurves(patchMsh);
        ArrayList<PatchVertex> placedVs = _getPatchVertices(patchMsh);
        _io.say.msg("   Placed Curves: " + placedCs.size());
        _io.say.msg("   Placed Vertices: " + placedVs.size());
        if (placedVs.size() != 4) {
            _io.say.msg("Number of Vertices are not 4. Please revise.");
            return null;
        }
        Vector3 p1 = c.transformCoordinate(new Vector3(r * rR, 0., 0.));
        Vector3 p2 = c.transformCoordinate(new Vector3(r * rR, 90. * toRad, 0.));
        Vector3 p3 = c.transformCoordinate(new Vector3(r * rR, 180 * toRad, 0));
        Vector3 p4 = c.transformCoordinate(new Vector3(r * rR, 270. * toRad, 0.));
        PatchVertex pv1 = placedVs.get(0);
        PatchVertex pv2 = placedVs.get(1);
        PatchVertex pv3 = placedVs.get(2);
        PatchVertex pv4 = placedVs.get(3);
        int[] angles = { 0, 90, 180, 270 };
        if (backwards) {
            _io.say.msg("Trying angles in backwards...");
            angles = new int[]{ 0, 270, 180, 90 };
            Vector3 p90 = new Vector3(p2);
            Vector3 p180 = new Vector3(p3);
            Vector3 p270 = new Vector3(p4);
            p2 = p270;
            p3 = p180;
            p4 = p90;
        }
        //_io.say.msg(  pv1.getProjectedCoordinate().toString());
        //--
        PatchVertex pv = null;
        for (int i = 1; i <= 4; i++) {
            switch (i) {
                case 1:
                    dv = new DoubleVector(new double[]{ p1.x, p1.y, p1.z });
                    pv = pv1;
                    break;
                case 2:
                    dv = new DoubleVector(new double[]{ p2.x, p2.y, p2.z });
                    pv = pv2;
                    break;
                case 3:
                    dv = new DoubleVector(new double[]{ p3.x, p3.y, p3.z });
                    pv = pv3;
                    break;
                case 4:
                    dv = new DoubleVector(new double[]{ p4.x, p4.y, p4.z });
                    pv = pv4;
                    break;
            }
            //_io.say.msg("DV = " + dv.toString());
            //_io.say.msg("PV = " + pv.getCoordinate().toString());
            //_io.say.msg("PV = " + pv.toString());
            patchMsh.createPatchCurve(pv, null, dv, new StringVector(new String[]{ "ON_SURFACE" }));
            PatchVertex p = (PatchVertex) _getNewObject(placedVs, _getPatchVertices(patchMsh));
            placedVs.add(p);
        }
        //--
        PatchCurve pcInt = (PatchCurve) _getNewObject(placedCs, _getPatchCurves(patchMsh));
        //--
        PatchVertex pv1r = placedVs.get(4);
        PatchVertex pv2r = placedVs.get(5);
        PatchVertex pv3r = placedVs.get(6);
        PatchVertex pv4r = placedVs.get(7);
        //--
        PatchVertex px = null, py = null;
        double delta = 90. / (_ud.dmDiv + 1);
        for (int deg : angles) {
            StringVector sv = new StringVector();
            dv.clear();
            _io.say.msg(true, "   Creating O-Grid angle: %d...", deg);
            //--
            for (int i = 1; i <= _ud.dmDiv; i++) {
                double newR = _ud.dmOGF * r * rR;
                double newT = deg + i * delta;
                //_io.say.msg(String.format("r = %g     Theta = %g", newR, newT));
                Vector3 v3 = new Vector3(newR, newT * toRad, 0.);
                Vector3 pxy = new Vector3(c.transformCoordinate(v3));
                dv.addAll(new DoubleVector(new double[]{ pxy.x, pxy.y, pxy.z }));
                sv.add("ON_SURFACE");
            }
            switch (deg) {
                case 0:
                    px = pv1r;
                    py = pv2r;
                    break;
                case 90:
                    px = pv2r;
                    py = pv3r;
                    break;
                case 180:
                    px = pv3r;
                    py = pv4r;
                    break;
                case 270:
                    px = pv4r;
                    py = pv1r;
                    break;
            }
            patchMsh.createPatchCurve(px, py, dv, sv);
        }
        return pcInt;
    }

    private void _setAnisotropicSizes(VolumeCustomMeshControl vcmc, double[] relSizes) {
        CustomMeshControlConditionManager vccc = vcmc.getCustomConditions();
        VolumeControlTrimmerSizeOption vctso = vccc.get(VolumeControlTrimmerSizeOption.class);
        vctso.setTrimmerAnisotropicSizeOption(true);
        TrimmerAnisotropicSize tas = vcmc.getCustomValues().get(TrimmerAnisotropicSize.class);
        if (relSizes[0] > 0) {
            tas.setXSize(true);
            _set.object.relativeSize(tas.getRelativeXSize(), "Relative Size X", relSizes[0]);
        }
        if (relSizes[1] > 0) {
            tas.setYSize(true);
            _set.object.relativeSize(tas.getRelativeYSize(), "Relative Size Y", relSizes[1]);
        }
        if (relSizes[2] > 0) {
            tas.setZSize(true);
            _set.object.relativeSize(tas.getRelativeZSize(), "Relative Size Z", relSizes[2]);
        }
    }

    private void _setAutomatedMesh(AutoMeshOperation amo, ArrayList<GeometryPart> ag, String txt) {
        _io.say.action("Creating an Automated Mesh Operation " + txt, true);
        _io.say.objects(ag, "Geometry Parts", true);
        _io.say.msg("Meshers: " + _get.strings
                .withinTheBrackets(amo.getMeshersCollection().toString()));
        _setMeshDefaults(amo.getDefaultValues());
        _set.mesh.prisms(amo, _ud.prismsLayers, _ud.prismsStretching, _ud.prismsRelSizeHeight,
                false);
        _set.mesh.thinMesher(amo, _ud.thinMeshLayers, _ud.thinMeshMaxThickness, false);
        if (_chk.has.polyMesher(amo)) {
            DualAutoMesher dam = ((DualAutoMesher) amo.getMeshers().getObject("Polyhedral Mesher"));
            dam.setTetOptimizeCycles(_ud.mshOptCycles);
            dam.setTetQualityThreshold(_ud.mshQualityThreshold);
            _io.say.value("Optimization Cycles", dam.getTetOptimizeCycles(), true);
            _io.say.value("Quality Threshold", dam.getTetQualityThreshold(), true);
        }
        _io.say.created(amo, true);
    }

    private void _setCentroidOffset(BoundedShapeCreatingOperation bsmo, double[] offset) {
        Units u = _ud.defUnitLength;
        BoundedShapeControlsManager bscm = bsmo.getBoundedShapeValuesManager();
        BoundedShapeCentroidOffset bsco = bscm.get(BoundedShapeCentroidOffset.class);
        _set.object.physicalQuantity(bsco.getXOffset(), offset[0], u, "X Centroid Offset", true);
        _set.object.physicalQuantity(bsco.getYOffset(), offset[1], u, "Y Centroid Offset", true);
        _set.object.physicalQuantity(bsco.getZOffset(), offset[2], u, "Z Centroid Offset", true);
    }

    private void _setIsotropicSize(VolumeCustomMeshControl vcmc, double relSize) {
        AutoMeshOperation amo = (AutoMeshOperation) vcmc.getManager().getMeshOperation();
        CustomMeshControlValueManager cmcvm = vcmc.getCustomValues();
        CustomMeshControlConditionManager vccc = vcmc.getCustomConditions();
        if (_chk.has.polyMesher(amo)) {
            vccc.get(VolumeControlDualMesherSizeOption.class).setVolumeControlBaseSizeOption(true);
        } else if (_chk.has.trimmerMesher(amo)) {
            vccc.get(VolumeControlTrimmerSizeOption.class).setVolumeControlBaseSizeOption(true);
        } else {
            _io.say.msg("WARNING! Impossible to set Relative size.");
            return;
        }
        _set.object.relativeSize(cmcvm.get(VolumeControlSize.class), "Relative Size", relSize);
    }

    private void _setMeshDefaults(AutoMeshDefaultValuesManager amdvm) {
        _set.object.physicalQuantity(amdvm.get(BaseSize.class), _ud.mshBaseSize, _ud.defUnitLength,
                "Base Size", true);
        _set.object.relativeSize(amdvm.get(PartsTargetSurfaceSize.class), "Target Surface Size",
                _ud.mshSrfSizeTgt);
        if (amdvm.has("Minimum Surface Size")) {
            _set.object.relativeSize(amdvm.get(PartsMinimumSurfaceSize.class),
                    "Minimum Surface Size", _ud.mshSrfSizeMin);
        }
        if (amdvm.has("Surface Curvature")) {
            _set.mesh.surfaceCurvature(amdvm.get(SurfaceCurvature.class),
                    _ud.mshSrfCurvNumPoints, false);
        }
        if (amdvm.has("Surface Proximity")) {
            _set.mesh.surfaceProximity(amdvm.get(SurfaceProximity.class),
                    _ud.mshProximityPointsInGap, _ud.mshProximitySearchFloor, false);
        }
        if (amdvm.has("Volume Growth Rate")) {
            star.trimmer.PartsGrowthRateOption.Type t = _ud.mshTrimmerGrowthRate.getType();
            amdvm.get(PartsSimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(t);
            _io.say.value("Growth Rate Type", t.getPresentationName(), true, true);
        }
        if (amdvm.has("Maximum Cell Size")) {
            _set.object.relativeSize(amdvm.get(MaximumCellSize.class), "Maximum Cell Size",
                    _ud.mshTrimmerMaxCellSize);
        }
    }

    private void _setWorkAroundAutoSourceMesh(DirectedAutoSourceMesh dasm, GeometryPart gp) {
        //-- Check later.
        // Workaround for legacy limitation of Target size not being respected in 2D meshes.
        SurfaceCustomMeshControl scmc = dasm.getCustomMeshControls().createSurfaceControl();
        scmc.getGeometryObjects().setObjects(gp);
        CustomMeshControlConditionManager cmccm = scmc.getCustomConditions();
        cmccm.get(PartsTargetSurfaceSizeOption.class)
                .setSelected(PartsTargetSurfaceSizeOption.Type.CUSTOM);
        _set.object.relativeSize(scmc.getCustomValues().get(PartsTargetSurfaceSize.class),
                "Relative Size", _ud.mshSrfSizeTgt);
        scmc.setPresentationName("Work-Around AutoSource Mesh");
    }

}
