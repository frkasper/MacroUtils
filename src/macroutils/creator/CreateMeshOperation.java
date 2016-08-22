package macroutils.creator;

import java.util.*;
import macroutils.*;
import star.base.neo.*;
import star.common.*;
import star.dualmesher.*;
import star.meshing.*;
import star.prismmesher.*;
import star.surfacewrapper.*;
import star.sweptmesher.*;
import star.trimmer.*;

/**
 * Low-level class for creating Mesh Operations with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class CreateMeshOperation {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CreateMeshOperation(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    private MeshOperationPart _createBoolean(StaticDeclarations.Operation op, ArrayList<GeometryPart> ag,
            GeometryPart tgtGP) {
        _io.say.action(String.format("Creating a %s Mesh Operation", op.toString()), true);
        _io.say.objects(ag, "Parts", true);
        MeshOperation mo = _createBooleanMeshOperation(op, tgtGP);
        mo.getInputGeometryObjects().setObjects(ag);
        mo.execute();
        String opName = _get.strings.withinTheBrackets(mo.getOutputPartNames());
        return ((MeshOperationPart) _sim.get(SimulationPartManager.class).getPart(opName));
    }

    private MeshOperation _createBooleanMeshOperation(StaticDeclarations.Operation op, GeometryPart tgtGP) {
        switch (op) {
            case SUBTRACT:
                SubtractPartsOperation spo = (SubtractPartsOperation) _getMOM().createSubtractPartsOperation();
                spo.setTargetPart((MeshPart) tgtGP);
                return spo;
            case UNITE:
                UnitePartsOperation upo = (UnitePartsOperation) _getMOM().createUnitePartsOperation();
                return upo;
        }
        return null;
    }

    private boolean _createDM_Pipe_buildExternalPatchCurves(DirectedPatchSourceMesh patchMsh,
            CylindricalCoordinateSystem c, boolean backwards) {
        double r = _getRadius(patchMsh, c);
        _io.say.msg("Erasing original Patch Curves...");
        for (PatchCurve pc : _getPCs(patchMsh)) {
            patchMsh.deletePatchCurve(pc);
        }
        _io.say.msg("Rebuilding external Patch Curves...");
        //-- Building is always clock-wise (0, 90, 180, 270)
        ArrayList<PatchVertex> placedVs = new ArrayList();
        Vector3 p1 = c.transformCoordinate(new Vector3(r, 0., 0.));
        Vector3 p2 = c.transformCoordinate(new Vector3(r, 90. / 180. * Math.PI, 0.));
        Vector3 p3 = c.transformCoordinate(new Vector3(r, Math.PI, 0));
        Vector3 p4 = c.transformCoordinate(new Vector3(r, 270. / 180. * Math.PI, 0.));
        double[] angles = {0, 90, 180, 270};
        if (backwards) {
            angles = new double[]{0, 270, 180, 90};
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
                    dv = new DoubleVector(new double[]{p1.x, p1.y, p1.z, p2.x, p2.y, p2.z});
                    patchMsh.createPatchCurve(null, null, dv,
                            new StringVector(new String[]{"ON_FEATURE_EDGE", "ON_FEATURE_EDGE"}));
                    placedVs.addAll(_getPVs(patchMsh));
                    continue;
                case 1:
                    dv = new DoubleVector(new double[]{p3.x, p3.y, p3.z});
                    break;
                case 2:
                    dv = new DoubleVector(new double[]{p4.x, p4.y, p4.z});
                    break;
            }
            oldPv = placedVs.get(placedVs.size() - 1);
            patchMsh.createPatchCurve(oldPv, null, dv, new StringVector(new String[]{"ON_FEATURE_EDGE"}));
            for (PatchVertex pv : _getPVs(patchMsh)) {
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

    private PatchCurve _createDM_Pipe_buildInternalPatchCurves(DirectedPatchSourceMesh patchMsh,
            CylindricalCoordinateSystem c,
            double rR, boolean backwards) {
        double r = _getRadius(patchMsh, c);
        final double toRad = Math.PI / 180.;
        DoubleVector dv = new DoubleVector();
        _io.say.msg("Building internal Patch Curves...");
        //-- Building is always clock-wise (0, 90, 180, 270)
        ArrayList<PatchCurve> placedCs = _getPCs(patchMsh);
        ArrayList<PatchVertex> placedVs = _getPVs(patchMsh);
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
        int[] angles = {0, 90, 180, 270};
        if (backwards) {
            _io.say.msg("Trying angles in backwards...");
            angles = new int[]{0, 270, 180, 90};
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
                    dv = new DoubleVector(new double[]{p1.x, p1.y, p1.z});
                    pv = pv1;
                    break;
                case 2:
                    dv = new DoubleVector(new double[]{p2.x, p2.y, p2.z});
                    pv = pv2;
                    break;
                case 3:
                    dv = new DoubleVector(new double[]{p3.x, p3.y, p3.z});
                    pv = pv3;
                    break;
                case 4:
                    dv = new DoubleVector(new double[]{p4.x, p4.y, p4.z});
                    pv = pv4;
                    break;
            }
            //_io.say.msg("DV = " + dv.toString());
            //_io.say.msg("PV = " + pv.getCoordinate().toString());
            //_io.say.msg("PV = " + pv.toString());
            patchMsh.createPatchCurve(pv, null, dv, new StringVector(new String[]{"ON_SURFACE"}));
            PatchVertex p = (PatchVertex) _getNewObject(placedVs, _getPVs(patchMsh));
            placedVs.add(p);
        }
        //--
        PatchCurve pcInt = (PatchCurve) _getNewObject(placedCs, _getPCs(patchMsh));
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
                dv.addAll(new DoubleVector(new double[]{pxy.x, pxy.y, pxy.z}));
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

    private DirectedMeshOperation _createDMO(PartSurface src, PartSurface tgt, boolean vo) {
        _io.say.object(src.getPart(), vo);
        if (!_chk.is.directedMeshable(src, tgt)) {
            return null;
        }
        ArrayList a = _get.objects.arrayList(src.getPart());
        MeshOperation mo = _getMOM().createDirectedMeshOperation(a);
        DirectedMeshOperation dmo = (DirectedMeshOperation) mo;
        dmo.getSourceSurfaceGroup().add(src);
        dmo.getTargetSurfaceGroup().add(tgt);
        return dmo;
    }

    private SurfaceCustomMeshControl _createSC(MeshOperation mo, boolean vo) {
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

    private MeshOperationManager _getMOM() {
        return _sim.get(MeshOperationManager.class);
    }

    private NeoObjectVector _getNOV1(ClientServerObject cso) {
        return new NeoObjectVector(new ClientServerObject[]{cso});
    }

    private NeoObjectVector _getNOV2(ArrayList al) {
        return new NeoObjectVector(al.toArray());
    }

    private Object _getNewObject(ArrayList objOld, ArrayList objNew) {
        for (Object o : objNew) {
            if (!objOld.contains(o)) {
                return o;
            }
        }
        return null;
    }

    private ArrayList<PatchCurve> _getPCs(DirectedPatchSourceMesh patchMsh) {
        return new ArrayList(patchMsh.getPatchCurveManager().getObjects());
    }

    private ArrayList<PatchVertex> _getPVs(DirectedPatchSourceMesh patchMsh) {
        return new ArrayList(patchMsh.getPatchVertexManager().getObjects());
    }

    private double _getRadius(DirectedPatchSourceMesh patchMsh, CylindricalCoordinateSystem c) {
        double maxR = 0.;
        for (PatchVertex pv : _getPVs(patchMsh)) {
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

    private void _setAutomatedMesh(AutoMeshOperation amo, ArrayList<GeometryPart> ag, String txt) {
        _io.say.action("Creating an Automated Mesh Operation " + txt, true);
        _io.say.objects(ag, "Geometry Parts", true);
        _io.say.msg("Meshers: " + _get.strings.withinTheBrackets(amo.getMeshersCollection().toString()));
        _set.mesh.baseSize(amo, _ud.mshBaseSize, _ud.defUnitLength, false);
        _set.mesh.surfaceSizes(amo, _ud.mshSrfSizeMin, _ud.mshSrfSizeTgt, false);
        _set.mesh.prisms(amo, _ud.prismsLayers, _ud.prismsStretching, _ud.prismsRelSizeHeight, false);
        _set.mesh.thinMesher(amo, _ud.thinMeshLayers, _ud.thinMeshMaxThickness, false);
        AutoMeshDefaultValuesManager amodv = amo.getDefaultValues();
        if (_chk.has.polyMesher(amo)) {
            _set.mesh.surfaceCurvature(amodv.get(SurfaceCurvature.class), _ud.mshSrfCurvNumPoints, false);
            _set.mesh.surfaceProximity(amodv.get(SurfaceProximity.class), _ud.mshProximityPointsInGap,
                    _ud.mshProximitySearchFloor, false);
        }
        if (_chk.has.polyMesher(amo)) {
            DualAutoMesher dam = ((DualAutoMesher) amo.getMeshers().getObject("Polyhedral Mesher"));
            dam.setTetOptimizeCycles(_ud.mshOptCycles);
            dam.setTetQualityThreshold(_ud.mshQualityThreshold);
            _io.say.value("Optimization Cycles", dam.getTetOptimizeCycles(), true);
            _io.say.value("Quality Threshold", dam.getTetQualityThreshold(), true);
            
        }
        if (_chk.has.trimmerMesher(amo)) {
            star.trimmer.PartsGrowthRateOption.Type t = _ud.mshTrimmerGrowthRate.getType();
            amodv.get(PartsSimpleTemplateGrowthRate.class).getGrowthRateOption().setSelected(t);
            _io.say.value("Growth Rate Type", t.getPresentationName(), true, true);
            _set.mesh.maxCellSize(amo, _ud.mshTrimmerMaxCellSize, false);
        }
        _io.say.created(amo, true);
    }

    /**
     * Creates an Automated Mesh Mesh Operation for the given Geometry Parts.
     *
     * @param ag given ArrayList of Geometry Parts.
     * @param am given ArrayList of Meshers. <u>Hint</u>: use with {@link macroutils.getter.GetStrings#meshers}.
     * @return The AutoMeshOperation.
     */
    public AutoMeshOperation automatedMesh(ArrayList<GeometryPart> ag, ArrayList<String> am) {
        AutoMeshOperation amo = _getMOM().createAutoMeshOperation(am, ag);
        _setAutomatedMesh(amo, ag, "");
        return amo;
    }

    /**
     * Creates an Automated Mesh Mesh Operation for the given Geometry Parts.
     *
     * @param ag given ArrayList of Geometry Parts.
     * @param meshers given meshers, separated by comma. See {@link StaticDeclarations} for options.
     * @return The AutoMeshOperation.
     */
    public AutoMeshOperation automatedMesh(ArrayList<GeometryPart> ag, StaticDeclarations.Meshers... meshers) {
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
        PrepareFor2dOperation p2d = (PrepareFor2dOperation) _getMOM().createPrepareFor2dOperation(agp);
        p2d.execute();
        _io.say.created(p2d, true);
        return p2d;
    }

    /**
     * Creates a One Group Contact Prevention with the supplied Geometry Objects.
     *
     * @param mo given Mesh Operation.
     * @param ago given ArrayList of Geometry Objects.
     * @param val given search floor value.
     * @param u given units for the search floor.
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
        _set.object.physicalQuantity(cp.getFloor(), val, null, u, "Search Floor", true);
        _io.say.ok(true);
        return cp;
    }

    /**
     * Creates a Directed Mesh Operation in a Pipe, using an O-Grid structure.
     *
     * @param src given Source Part Surface.
     * @param tgt given Target Part Surface.
     * @param nT given number of points in the circumference, i.e., Theta direction.
     * @param nR given number of points radially.
     * @param nVol given number of points for the volume distribution.
     * @param rR given r/R distance for the O-Grid. E.x.: 0.5;
     * @param c given Cylindrical Coordinate System.
     * @return The Directed Mesh Operation.
     */
    public DirectedMeshOperation directedMeshing_Pipe(PartSurface src, PartSurface tgt, int nT, int nR, int nVol,
            double rR, CoordinateSystem c) {
        _io.say.action("Creating a Directed Mesh Operation in a Pipe", true);
        if (!_chk.is.cylindricalCSYS(c)) {
            _io.say.msg(true, "Not a Cylindrical Coordinate System: \"%s\".", c.getPresentationName());
            _io.say.msg("Directed Mesh not created.");
            return null;
        }
        DirectedMeshOperation dmo = _createDMO(src, tgt, true);
        CylindricalCoordinateSystem ccs = (CylindricalCoordinateSystem) c;

        String s = src.getPart().getPresentationName();
        DirectedMeshPartCollection dmpc = dmo.getGuidedMeshPartCollectionManager().getObject(s);
        NeoObjectVector srcPSs = new NeoObjectVector(new Object[]{src});
        NeoObjectVector tgtPSs = new NeoObjectVector(new Object[]{tgt});
        dmo.getGuidedSurfaceMeshBaseManager().validateConfigurationForPatchMeshCreation(dmpc, srcPSs, tgtPSs);

        DirectedPatchSourceMesh patchMsh = dmo.getGuidedSurfaceMeshBaseManager().createPatchSourceMesh(srcPSs, dmpc);
        NeoProperty np = patchMsh.autopopulateFeatureEdges();
        //_io.say.msg("NeoProperty np = patchMsh.autopopulateFeatureEdges();");
        //_io.say.msg(np.getHashtable().toString());

        boolean isBackwards = _createDM_Pipe_buildExternalPatchCurves(patchMsh, ccs, false);
        if (isBackwards) {
            _createDM_Pipe_buildExternalPatchCurves(patchMsh, ccs, isBackwards);
        }
        ArrayList<PatchCurve> pcExts = _getPCs(patchMsh);
        PatchCurve pcInt = _createDM_Pipe_buildInternalPatchCurves(patchMsh, ccs, rR, isBackwards);

        if (pcInt == null) {
            return null;
        }

        patchMsh.defineMeshMultiplePatchCurves(_getNOV2(pcExts), nT, false);
        patchMsh.defineMeshMultiplePatchCurves(_getNOV1(pcInt), nR, false);
        if (_ud.dmSmooths > 0) {
            patchMsh.smoothPatchPolygonMesh(_ud.dmSmooths, 0.25, false);
        }

        DirectedMeshDistributionManager dmdm = dmo.getDirectedMeshDistributionManager();
        DirectedMeshDistribution dmd = dmdm.createDirectedMeshDistribution(_getNOV1(dmpc), "Constant");
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
        MeshOperation mo = _getMOM().createExtractVolumeOperation(agp);
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
        FillHolesOperation fho = (FillHolesOperation) _getMOM().createFillHolesOperation(agp);
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
     * @param it given Imprint Method.
     * @param mt given Resulting Mesh Type
     * @return The ImprintPartsOperation.
     */
    public ImprintPartsOperation imprint(ArrayList<GeometryPart> agp, double tol,
            ImprintMergeImprintMethodOption.Type it, ImprintResultingMeshTypeOption.Type mt) {
        _io.say.action("Creating an Imprint Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        MeshOperation mo = _getMOM().createImprintPartsOperation(agp);
        ImprintPartsOperation ipo = (ImprintPartsOperation) mo;
        ipo.getMergeImprintMethod().setSelected(it);
        ipo.getResultingMeshType().setSelected(mt);
        _io.say.msg(true, "Imprint Method: \"%s\".",
                ipo.getMergeImprintMethod().getSelectedElement().getPresentationName());
        _io.say.msg(true, "Resulting Mesh: \"%s\".",
                ipo.getResultingMeshType().getSelectedElement().getPresentationName());
        _set.object.physicalQuantity(ipo.getTolerance(), tol, null, _ud.defUnitLength, "Tolerance", true);
        if (it == ImprintMergeImprintMethodOption.Type.CAD_IMPRINT) {
            CadTessellationOption cto = ipo.getImprintValuesManager().get(CadTessellationOption.class);
            cto.getTessellationDensityOption().setSelected(_ud.defTessOpt.getType());
        }
        if (mt == ImprintResultingMeshTypeOption.Type.CONFORMAL) {
            ImprintPartSurfaces ips = ipo.getImprintValuesManager().get(ImprintPartSurfaces.class);
            ips.getPartSurfacesOption().setSelected(ImprintPartSurfacesOption.Type.USE_INPUT);
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
        return _createSC(mo, true);
    }

    /**
     * Copies a Custom Surface Control from another another Mesh Operation.
     *
     * @param scmc given Surface Control to copy.
     * @param mo given Mesh Operation where will be copied too.
     * @return The SurfaceCustomMeshControl.
     */
    public SurfaceCustomMeshControl surfaceControl(SurfaceCustomMeshControl scmc, MeshOperation mo) {
        _io.say.action("Creating a Custom Surface Mesh Control", true);
        _io.say.object(scmc, true);
        _io.say.object(mo, true);
        _io.say.msg(true, "Copying \"%s\" from \"%s\"...", scmc.getPresentationName(), mo.getPresentationName());
        SurfaceCustomMeshControl scmc2 = _createSC(mo, false);
        scmc2.setPresentationName(scmc.getPresentationName());
        scmc2.copyProperties(scmc);
        _io.say.created(scmc2, true);
        return scmc2;
    }

    /**
     * Creates a Custom Surface Control to change Surface Sizes in a Mesh Operation.
     *
     * @param mo given Mesh Operation.
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
        SurfaceCustomMeshControl scmc = _createSC(mo, false);
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
     * @param amo given Auto Mesh Operation.
     * @param ago given ArrayList of Geometry Objects.
     * @param numLayers given number of prisms. If 0, this parameter will not be customized.
     * @param stretch given prism stretch relation. If 0, this parameter will not be customized.
     * @param relSize given relative size in (%). If 0, this parameter will not be customized.
     * @return The Custom Surface Control.
     */
    public SurfaceCustomMeshControl surfaceControl(AutoMeshOperation amo, ArrayList<GeometryObject> ago,
            int numLayers, double stretch, double relSize) {
        _io.say.action("Creating a Custom Surface Mesh Control", true);
        _io.say.object(amo, true);
        _io.say.objects(ago, "Geometry Objects", true);
        SurfaceCustomMeshControl scmc = amo.getCustomMeshControls().createSurfaceControl();
        scmc.getGeometryObjects().setObjects(ago);
        PartsCustomizePrismMesh pcpm = scmc.getCustomConditions().get(PartsCustomizePrismMesh.class);
        if (numLayers + stretch + relSize == 0.0) {
            _dis.prismsLayers(scmc, false);
            _io.say.msg(true, "Prism Layers DISABLED.");
        } else {
            pcpm.getCustomPrismOptions().setSelected(PartsCustomPrismsOption.Type.CUSTOMIZE);
            CustomPrismValuesManager cpvm = scmc.getCustomValues().get(CustomPrismValuesManager.class);
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
     * @param agp given ArrayList of Geometry Parts.
     * @param name given name for the Operation. The Part generated will share the same name.
     * @return The SurfaceWrapperAutoMeshOperation.
     */
    public SurfaceWrapperAutoMeshOperation surfaceWrapper(ArrayList<GeometryPart> agp, String name) {
        _io.say.action("Creating a Surface Wrapper Mesh Operation", true);
        _io.say.objects(agp, "Geometry Parts", true);
        AutoMeshOperation amo = _getMOM().createSurfaceWrapperAutoMeshOperation(agp, name);
        SurfaceWrapperAutoMeshOperation swamo = (SurfaceWrapperAutoMeshOperation) amo;
        AutoMeshDefaultValuesManager amdvm = swamo.getDefaultValues();
        _set.mesh.baseSize(swamo, _ud.mshBaseSize, _ud.defUnitLength, false);
        _set.mesh.surfaceSizes(swamo, _ud.mshSrfSizeMin, _ud.mshSrfSizeTgt, false);
        _set.mesh.surfaceCurvature(amdvm.get(SurfaceCurvature.class), _ud.mshSrfCurvNumPoints, false);
        GlobalVolumeOfInterestOption gvio = amdvm.get(GlobalVolumeOfInterest.class).getVolumeOfInterestOption();
        GeometricFeatureAngle gfa = amdvm.get(GeometricFeatureAngle.class);
        gvio.setSelected(GlobalVolumeOfInterestOption.Type.LARGEST_INTERNAL);
        gfa.setGeometricFeatureAngle(_ud.mshWrapperFeatureAngle);
        _io.say.value("Volume of Interest", gvio.getSelectedElement().getPresentationName(), true, true);
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
        _io = _mu.io;
        _chk = _mu.check;
        _dis = _mu.disable;
        _get = _mu.get;
        _set = _mu.set;
        _tmpl = _mu.templates;
        _ud = _mu.userDeclarations;
    }

    /**
     * Creates a Custom Volumetric Control with isotropic values in a Mesh Operation.
     *
     * @param mo given Mesh Operation.
     * @param agp given ArrayList of Geometry Parts.
     * @param relSize relative size in (<b>%</b>). Zero is ignored.
     * @return The VolumeCustomMeshControl.
     */
    public VolumeCustomMeshControl volumetricControl(MeshOperation mo, ArrayList<GeometryPart> agp, double relSize) {
        return volumetricControl(mo, agp, relSize, StaticDeclarations.COORD0);
    }

    /**
     * Creates a Custom Volumetric Control in a Mesh Operation where the control can be Isotropic or Anisotropic
     * (Trimmer only).
     *
     * @param mo given Mesh Operation.
     * @param agp given ArrayList of Geometry Parts.
     * @param relSize relative size in (<b>%</b>). Zero is ignored.
     * @param relSizes given 3-component relative sizes in (<b>%</b>). E.g.: {0, 50, 0}. Zeros will be ignored.
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
        CustomMeshControlConditionManager vccc = vcmc.getCustomConditions();
        if (_chk.has.polyMesher(amo) && relSize > 0) {
            vccc.get(VolumeControlDualMesherSizeOption.class).setVolumeControlBaseSizeOption(true);
            vcmc.getCustomValues().get(VolumeControlSize.class).getRelativeSize().setPercentage(relSize);
        }
        if (_chk.has.trimmerMesher(amo)) {
            vccc.get(VolumeControlTrimmerSizeOption.class).setTrimmerAnisotropicSizeOption(true);
            TrimmerAnisotropicSize tas = vcmc.getCustomValues().get(TrimmerAnisotropicSize.class);
            if (relSize > 0) {
                _io.say.msg(true, "Isotropic Relative Size (%%): %g.", relSize);
                vccc.get(VolumeControlTrimmerSizeOption.class).setVolumeControlBaseSizeOption(true);
                vcmc.getCustomValues().get(VolumeControlSize.class).getRelativeSize().setPercentage(relSize);
            }
            if (relSizes[0] > 0) {
                _io.say.msg(true, "Relative Size X (%%): %g.", relSizes[0]);
                tas.setXSize(true);
                tas.getRelativeXSize().setPercentage(relSizes[0]);
            }
            if (relSizes[1] > 0) {
                _io.say.msg(true, "Relative Size Y (%%): %g.", relSizes[1]);
                tas.setYSize(true);
                tas.getRelativeYSize().setPercentage(relSizes[1]);
            }
            if (relSizes[2] > 0) {
                _io.say.msg(true, "Relative Size Z (%%): %g.", relSizes[2]);
                tas.setZSize(true);
                tas.getRelativeZSize().setPercentage(relSizes[2]);
            }
        }
        _io.say.created(vcmc, true);
        return vcmc;
    }

    //--
    //-- Variables declaration area.
    //--
    private Simulation _sim = null;
    private MacroUtils _mu = null;
    private macroutils.UserDeclarations _ud = null;
    private macroutils.checker.MainChecker _chk = null;
    private macroutils.io.MainIO _io = null;
    private macroutils.getter.MainGetter _get = null;
    private macroutils.misc.MainDisabler _dis = null;
    private macroutils.setter.MainSetter _set = null;
    private macroutils.templates.MainTemplates _tmpl = null;

}
