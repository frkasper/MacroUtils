
import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.NamedObject;
import star.common.PartSurface;
import star.common.Simulation;
import star.common.StarMacro;
import star.meshing.SurfaceCustomMeshControl;
import star.trimmer.PartsGrowthRateOption;

/**
 * Another complete workflow automated with Macro Utils. 3D flow over a wing.
 *
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo8_Half_Wing extends StarMacro {

    public void execute() {

        initMacro();

        prep1_importGeometryAndSplit();

        prep2_createRegionBCsAndMesh();

        prep3_setPost();

        mu.open.all();

        mu.run();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo8_Half_Wing";
        //--
        //-- Mesh settings
        ud.mshBaseSize = 30;
        ud.mshSrfSizeMin = 1. / 16. * 100.;
        ud.mshSrfSizeTgt = 64 * 100.;  // Geometric progression in a ratio of 2. This is how Trimmer grows.
        ud.mshSrfCurvNumPoints = 60;
        ud.mshSrfGrowthRate = 1.15;
        ud.prismsLayers = 3;
        ud.prismsRelSizeHeight = 30;
        ud.mshTrimmerMaxCellSize = ud.mshSrfSizeTgt;
        //--
        updateGlobalObjects();
    }

    void prep1_importGeometryAndSplit() {
        ud.vv1 = mu.io.read.cameraView("cam1|2.627790e-01,1.688267e-01,-8.768673e-02|3.042959e+00,2.949006e+00,2.692493e+00|0.000000e+00,1.000000e+00,0.000000e+00|1.257075e+00", true);
        ud.vv2 = mu.io.read.cameraView("cam2|9.022700e-02,-5.806686e-03,-2.014223e-01|7.395474e-01,6.435137e-01,4.478981e-01|0.000000e+00,1.000000e+00,0.000000e+00|2.935941e-01", true);
        ud.defCamView = ud.vv1;
        if (!sim.getGeometryPartManager().isEmpty()) {
            mu.io.say.loud("Geometry already created. Skipping prep1...");
            return;
        }
        mu.set.userDefault.tessellation(StaticDeclarations.Tessellation.VERY_FINE);
        mu.add.geometry.importPart("WING.x_b");
        updateGlobalObjects();
        //-- Split the single Part Surface by its Part Curve
        ud.geomPrt.setPresentationName("WING");
        mu.set.geometry.splitPartSurfacesByPartCurves(ud.geomPrt, true);
        updateGlobalObjects();
        //-- Get Airfoils and other Wing BCs
        //-- 50mm of tolerance should be enough for this geometry.
        ud.partSrf = mu.get.partSurfaces.byRangeMax(ud.partSurfaces, StaticDeclarations.Axis.X, 50);
        ud.partSrf.setPresentationName(bcAirfoil);
        ud.partSrf = mu.get.partSurfaces.byRangeMin(ud.partSurfaces, StaticDeclarations.Axis.X, 50);
        ud.partSrf.setPresentationName(bcAirfoil2);
        ud.partSrf = mu.get.partSurfaces.byAreaMax(ud.partSurfaces);
        ud.partSrf.setPresentationName(bcWing);
        //-- Leftover is the trail.
        ud.partSrf = mu.get.partSurfaces.byREGEX(ud.geomPrt, "Faces.*", true);
        ud.partSrf.setPresentationName(bcTrail);;
        //--
        // mu.saveSim("a_import");
    }

    void prep2_createRegionBCsAndMesh() {
        if (!sim.getRegionManager().isEmpty()) {
            mu.io.say.loud("Region already created. Skipping prep2...");
            return;
        }
        updateGlobalObjects();
        //--
        //-- How big respective to the biggest length scale (dx, dy or dz)
        double farFieldRelSize = 10;
        //--
        //-- Sphere is created relative to the biggest length scale (dx, dy or dz)
        ud.simpleSphPrt = mu.add.geometry.sphere(ud.partSurfaces, farFieldRelSize);
        ud.simpleSphPrt.setPresentationName(regionName);
        //--
        //-- Block is created relative to each length scale (dx, dy and dz)
        double[] blkCorner1 = {-2 * farFieldRelSize, -50 * farFieldRelSize, -50 * farFieldRelSize};
        double[] blkCorner2 = {-0.5, 50 * farFieldRelSize, 50 * farFieldRelSize};
        ud.simpleBlkPrt = mu.add.geometry.block(ud.partSurfaces, blkCorner1, blkCorner2);
        ud.simpleBlkPrt.setPresentationName(ud.bcSym);
        ud.mshOpPrt = mu.add.meshOperation.subtract(mu.get.geometries.all(true), ud.simpleSphPrt);
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE, StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_EACH_PART_CURVE, true);
        ud.region.setPresentationName(regionName);
        //--
        ud.geometryParts.addAll(ud.region.getPartGroup().getObjects());
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.TRIMMER_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("Mesh");
        //-- Volumetric Controls
        ArrayList<PartSurface> aps = new ArrayList(ud.geomPrt.getPartSurfaces());
        blkCorner1 = new double[]{-.1, -30, -100};
        blkCorner2 = new double[]{4, 30, 100};
        ud.simpleBlkPrt = mu.add.geometry.block(aps, blkCorner1, blkCorner2);
        ud.simpleBlkPrt.setPresentationName("VC1");
        blkCorner1 = new double[]{-.1, -5, -100};
        blkCorner2 = new double[]{1, 5, 15};
        ud.simpleBlkPrt1 = mu.add.geometry.block(aps, blkCorner1, blkCorner2);
        ud.simpleBlkPrt1.setPresentationName("VC2");
        blkCorner1 = new double[]{-.1, -1, -10};
        blkCorner2 = new double[]{0.25, 1, 0.75};
        ud.simpleBlkPrt2 = mu.add.geometry.block(aps, blkCorner1, blkCorner2);
        ud.simpleBlkPrt2.setPresentationName("VC3");
        blkCorner1 = new double[]{-.1, -.25, -3};
        blkCorner2 = new double[]{0.0625, .5, 0.125};
        ud.simpleBlkPrt3 = mu.add.geometry.block(aps, blkCorner1, blkCorner2);
        ud.simpleBlkPrt3.setPresentationName("VC4");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 16 * 100.);
        ud.mshCtrl.setPresentationName("VC1");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt1);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 0, new double[]{800, 400, 1600});
        ud.mshCtrl.setPresentationName("VC2");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt2);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 0, new double[]{400, 100, 400});
        ud.mshCtrl.setPresentationName("VC3");
        ud.geometryParts.clear();
        ud.geometryParts.add(ud.simpleBlkPrt3);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 0, new double[]{200, 25, 200});
        ud.mshCtrl.setPresentationName("VC4");
        //--
        //-- Create Physics Continua and convert to Coupled Solver
        ud.refT = 22.;
        ud.CFL = 150;
        ud.v0 = new double[]{0, 0, -wingVel};
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.RKE_2LAYER);
        mu.enable.expertInitialization(5, true);
        mu.enable.expertDriver(true);
        //****************************************
        //-- BOUNDARY CONDITIONS
        //****************************************
        //--
        //-- Free Stream
        ud.bdry = mu.get.boundaries.byREGEX(".*" + regionName + ".*", true);
        mu.set.boundary.asFreeStream(ud.bdry, flowDirection, 0.0288, 0., ud.refT, ud.ti0, ud.tvr0);
        //--
        //-- Trail
        ud.bdry = mu.get.boundaries.byREGEX(".*" + bcTrail + ".*", true);
        ud.geometryObjects.addAll(mu.get.partSurfaces.fromBoundary(ud.bdry));
        ud.mshCtrl = mu.add.meshOperation.surfaceControl(ud.mshOp, ud.geometryObjects, ud.mshSrfSizeMin, ud.mshSrfSizeMin);
        ud.mshCtrl.setPresentationName("Control Trail");
        mu.enable.trimmerWakeRefinement((SurfaceCustomMeshControl) ud.mshCtrl, 250, 5,
                flowDirection, new double[]{25, ud.mshSrfSizeMin, 25}, PartsGrowthRateOption.Type.MEDIUM);
        ud.geometryObjects.clear();
        //--
        //-- Airfoil
        ud.bdry = mu.get.boundaries.byREGEX(".*" + bcAirfoil + ".*", true);
        ud.geometryObjects.addAll(mu.get.partSurfaces.fromBoundary(ud.bdry));
        ud.mshCtrl = mu.add.meshOperation.surfaceControl(ud.mshOp, ud.geometryObjects,
                ud.mshSrfSizeMin, 4 * ud.mshSrfSizeMin);
        ud.mshCtrl.setPresentationName("Control Airfoil");
        ud.geometryObjects.clear();
        //--
        //-- Wing
        ud.bdry = mu.get.boundaries.byREGEX(".*" + bcWing + ".*", true);
        ud.geometryObjects.addAll(mu.get.partSurfaces.fromBoundary(ud.bdry));
        ud.mshCtrl = mu.add.meshOperation.surfaceControl(ud.mshOp, ud.geometryObjects,
                ud.mshSrfSizeMin, 200.);
        ud.mshCtrl.setPresentationName("Control Wing");
        ud.geometryObjects.clear();
        //--
        //-- Symmetry
        ud.bdry2 = mu.get.boundaries.byREGEX(".*" + ud.bcSym + ".*", true);
        mu.set.boundary.asSymmetry(ud.bdry2);
        //--
        mu.update.volumeMesh();
        // mu.saveSim("b_meshed");
    }

    void prep3_setPost() {
        if (!sim.getReportManager().isEmpty()) {
            mu.io.say.loud("Post-processing already exists. Skipping prep4...");
            return;
        }
        //--
        ud.namedObjects.addAll(getWingBoundaries());
        ud.namedObjects.add(mu.get.boundaries.byREGEX(".*" + ud.bcSym + ".*", true));
        //--
        //-- Mesh Scene
        ud.coord1 = new double[]{0, 0, -300};
        ud.plane = mu.add.derivedPart.sectionPlaneZ(ud.coord1);
        ud.namedObjects2.add(ud.plane);
        ud.cellSrf = mu.add.derivedPart.cellSurface(ud.namedObjects2);
        ud.cellSrf.setPresentationName("Cells on Plane Z");
        ud.namedObjects.add(ud.cellSrf);
        ud.scene = mu.add.scene.mesh(ud.namedObjects);
        ud.scene.setPresentationName("Volume Mesh");
        //-- Update Events
        ud.updEvent1 = mu.add.tools.updateEvent_Iteration(10, 0);
        ud.updEvent2 = mu.add.tools.updateEvent_Iteration(1, 20);
        //-- Contour & Vector Plots
        ud.namedObjects.remove(ud.cellSrf);
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.PC.getVar(), true);
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_Dimensionless, true);
        ud.scene.setPresentationName("Cp Wing");
        mu.set.scene.updateEvent(ud.scene, ud.updEvent1);
        ud.scene2 = mu.add.scene.vector(ud.namedObjects, true);
        ud.scene2.setPresentationName("Vector Wing");
        mu.set.scene.updateEvent(ud.scene2, ud.updEvent1);
        //--
        //-- Reports and Stopping Criterias
        int stopIter = 75;
        ud.rep1 = mu.add.report.frontalArea(getWingBoundaries(), "Frontal Area",
                new double[]{0, 1, 0}, new double[]{0, 0, -1}, true);
        ud.rep2 = mu.add.report.frontalArea(getWingBoundaries(), "Upper Area",
                new double[]{0, 0, 1}, new double[]{0, -1, 0}, true);
        //-- Cd
        ud.rep = mu.add.report.forceCoefficient(getWingBoundaries(), "C_d", 0.0, 1.196, wingVel,
                ud.rep1.getReportMonitorValue(), new double[]{0, 0, -1}, true);
        ud.mon = mu.get.monitors.byREGEX(ud.rep.getPresentationName(), true);
        mu.set.object.updateEvent(ud.mon, ud.updEvent2, true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.ASYMPTOTIC, 0.001, stopIter);
        //-- Cl
        ud.rep = mu.add.report.forceCoefficient(getWingBoundaries(), "C_l", 0.0, 1.196, wingVel,
                ud.rep2.getReportMonitorValue(), new double[]{0, 1, 0}, true);
        ud.mon = mu.get.monitors.byREGEX(ud.rep.getPresentationName(), true);
        mu.set.object.updateEvent(ud.mon, ud.updEvent2, true);
        mu.add.solver.stoppingCriteria(ud.mon, StaticDeclarations.StopCriteria.ASYMPTOTIC, 0.001, stopIter);
        // mu.saveSim("c_Ready");
    }

    ArrayList<NamedObject> getWingBoundaries() {
        return new ArrayList(mu.get.boundaries.allByREGEX(".*wing.*", false));
    }

    void updateGlobalObjects() {
        ud.geomPrt = mu.get.geometries.byREGEX(".*", true);
        ud.region = mu.get.regions.byREGEX(".*", true);
        if (ud.geomPrt == null) {
            return;
        }
        ud.partSurfaces.addAll(ud.geomPrt.getPartSurfaces());
    }

    //--
    //-- Private variables
    //--
    private MacroUtils mu;
    private Simulation sim;
    private UserDeclarations ud;
    //--
    //-- Boundary Condition Names and other Misc stuff
    private final String bcWing = "wing body";
    private final String bcAirfoil = "wing airfoil";
    private final String bcAirfoil2 = "wing airfoil NotUsed";
    private final String bcTrail = "wing trail";
    private final String regionName = "FarField";
    private final double[] flowDirection = {0, 0, -1};
    double wingVel = 10.;

}
