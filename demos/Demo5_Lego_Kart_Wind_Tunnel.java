import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.NamedObject;
import star.common.Boundary;
import star.common.Simulation;
import star.common.StarMacro;
import star.meshing.MeshOperationPart;
import star.meshing.SurfaceCustomMeshControl;

/**
 * Complete workflow from CAD to finish on a Lego Kart.
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo5_Lego_Kart_Wind_Tunnel extends StarMacro {

    private double maxX;
    private double maxY;
    private double maxZ;
    private double minZ;
    private MacroUtils mu;
    private Simulation sim;
    private final String tunnelName = "Tunnel";
    private final double tunnelVel = 50;
    private UserDeclarations ud;
    private final String wrapName = "Kart Wrap";

    @Override
    public void execute() {

        initMacro();

        prep1_importGeometry();

        prep2_createTunnelWrapAndRegion();

        prep3_createBCsAndMesh();

        prep4_setPost();

        mu.run();

        mu.open.all();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private ArrayList<NamedObject> getCarBoundaries() {
        return new ArrayList<>(mu.get.boundaries.allByREGEX("^((?!" + tunnelName + ").)*$", false));
    }

    private Boundary getInlet() {
        return mu.get.boundaries.byREGEX(tunnelName + ".*" + ud.bcInlet, true);
    }

    private Boundary getOutlet() {
        return mu.get.boundaries.byREGEX(tunnelName + ".*" + ud.bcOutlet, true);
    }

    private void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo5_Lego_Kart_Wind_Tunnel";
    }

    private void prep1_importGeometry() {
        if (!sim.getGeometryPartManager().isEmpty()) {
            mu.io.say.loud("Geometry already created. Skipping prep1...");
            return;
        }
        ud.vv1 = mu.io.read.cameraView("cam1|-2.491101e-03,1.572050e-02,-9.156700e-03"
                + "|8.364881e-01,3.962117e-01,8.597759e-01|-2.296008e-01,9.533891e-01,-1.957871e-01"
                + "|4.813384e-02|1", true);
        ud.vv2 = mu.io.read.cameraView("cam2|-6.473358e-03,4.801700e-02,-3.333981e-02"
                + "|1.001025e+00,4.801700e-02,-3.333981e-02|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|7.945790e-02|1", true);
        ud.vv3 = mu.io.read.cameraView("cam3|-1.447091e-02,7.527911e-02,-2.683634e-01"
                + "|1.812691e+00,1.271046e+00,2.099190e+00|-2.793358e-01,9.264438e-01,-2.523359e-01"
                + "|3.358636e-01|1", true);
        mu.set.userDefault.cameraView(ud.vv1);
        mu.add.geometry.importPart("LegoKart.x_b");
        ud.scene = mu.add.scene.geometry();
        ud.scene.open();
        // mu.saveSim("prep1");
    }

    private void prep2_createTunnelWrapAndRegion() {
        if (!sim.getRegionManager().isEmpty()) {
            mu.io.say.loud("Region already created. Skipping prep2...");
            return;
        }
        //-- Save car boundaries for later, i.e., Block refinement.
        ud.geometryParts.addAll(mu.get.geometries.all(true));
        ud.partSurfaces.addAll(mu.get.partSurfaces.all(true));
        ud.geometryObjects.addAll(ud.partSurfaces);
        //-- Mesh settings w.r.t Kart
        double baseSizeKart = 3.0;      // milimeters
        double targetKart = 100.0;
        double targetTunnel = 8 * targetKart;
        double prismHeight = 30;
        int prismLayers = 1;
        ud.mshBaseSize = baseSizeKart;
        ud.mshSrfSizeMin = targetKart / 4.0;
        ud.mshSrfSizeTgt = targetKart;
        ud.prismsLayers = prismLayers;
        ud.prismsRelSizeHeight = prismHeight;
        ud.mshTrimmerGrowthRate = StaticDeclarations.GrowthRate.SLOW;
        ud.mshTrimmerMaxCellSize = targetTunnel;
        //-- Calculate Wind Tunnel Dimensions
        minZ = 200.0 * baseSizeKart;     // Flow direction is -Z
        maxZ = 0.4 * minZ;
        maxY = 50.0 * baseSizeKart;      // Height: MinY = 0.0 w.r.t. Kart body
        maxX = 1.2 * maxY;               // Span direction: MinX = MaxX
        ud.mshOpPrt = mu.add.meshOperation.boundedShape_Block(ud.geometryParts,
                new double[]{ maxX, 0.0, minZ },
                new double[]{ maxX, maxY, maxZ },
                StaticDeclarations.COORD0);
        mu.set.object.name(ud.mshOpPrt, tunnelName);
        //--
        mu.get.partSurfaces.byREGEX(ud.mshOpPrt, "MaxZ", true).setPresentationName(ud.bcInlet);
        mu.get.partSurfaces.byREGEX(ud.mshOpPrt, "MinZ", true).setPresentationName(ud.bcOutlet);
        ud.partSrf1 = mu.get.partSurfaces.byREGEX(ud.mshOpPrt, "MinY", true);
        ud.partSrf1.setPresentationName(ud.bcGround);
        ud.geometryObjects.add(ud.partSrf1);  // Will be used in Contact Prevention.
        ud.partSurfaces2.addAll(mu.get.partSurfaces.allByREGEX(ud.mshOpPrt, "M(in|ax).", true));
        ud.partSrf2 = mu.set.geometry.combinePartSurfaces(ud.partSurfaces2, true);
        ud.partSrf2.setPresentationName(ud.bcWalls);
        //--
        //-- Create Wrap Operation
        ud.wrapMshOp = mu.add.meshOperation.surfaceWrapper(mu.get.geometries.all(true), wrapName);
        mu.set.mesh.baseSize(ud.wrapMshOp, baseSizeKart - 1.0, ud.unit_mm, true);
        ud.geometryObjects2.add(ud.mshOpPrt);
        ud.mshCtrl1 = mu.add.meshOperation.surfaceControl(ud.wrapMshOp, ud.geometryObjects2,
                0.0, targetTunnel);
        ud.mshCtrl1.setPresentationName(tunnelName);
        //-- Create Contact Preventions
        mu.add.meshOperation.contactPrevention(ud.wrapMshOp, ud.geometryObjects, 0.8, ud.unit_mm);
        ud.geomPrt = mu.get.geometries.byREGEX(ud.wrapMshOp.getPresentationName(), true);
        ud.region = mu.add.region.fromPart(ud.geomPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        ud.region.setPresentationName(tunnelName);

        //-- Create Custom Bounded Shapes for Volumetric Controls
        double bsOffset = 2.0 * ud.mshBaseSize;
        ud.mshOpPrt1 = mu.add.meshOperation.boundedShape_Block(ud.geometryParts,
                new double[]{ bsOffset, 0.0, 2.0 * bsOffset },
                new double[]{ bsOffset, bsOffset, bsOffset },
                StaticDeclarations.COORD0);
        mu.set.object.name(ud.mshOpPrt1, "Mesh Refine Kart");
        //--
        ud.mshOpPrt2 = mu.add.meshOperation.boundedShape_Block(ud.geometryParts,
                new double[]{ maxX / 5.0, 0.0, minZ / 4.0 },
                new double[]{ maxX / 5.0, maxY / 5.0, maxZ / 6.0 },
                StaticDeclarations.COORD0);
        mu.set.object.name(ud.mshOpPrt2, "Mesh Refine Wake");
        //--
        ud.mshOpPrt3 = mu.add.meshOperation.boundedShape_Block(ud.geometryParts,
                new double[]{ maxX / 3.0, 0, minZ },
                new double[]{ maxX / 3.0, maxY / 2.0, maxZ / 2.0 },
                StaticDeclarations.COORD0);
        mu.set.object.name(ud.mshOpPrt3, "Mesh Refine Streamwise");
        //--
        ud.geometryParts2.add(ud.geomPrt);
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.AUTOMATIC_SURFACE_REPAIR,
                StaticDeclarations.Meshers.TRIMMER_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("Mesh of " + ud.geomPrt.getPresentationName());
        ud.geometryParts2.clear();

        //-- Create Custom Surface Controls
        double tunnelPrismHeight = targetTunnel / targetKart * prismHeight;
        ud.mshCtrl2 = mu.add.meshOperation.surfaceControl((SurfaceCustomMeshControl) ud.mshCtrl1,
                ud.mshOp);
        mu.set.mesh.prisms(ud.mshCtrl2, prismLayers + 1, 0, tunnelPrismHeight, true);
        //--
        //-- Create Custom Volume Controls
        MeshOperationPart[] volumeControls = { ud.mshOpPrt1, ud.mshOpPrt2, ud.mshOpPrt3 };
        double[] sizeFactors = { 1.0, 2.0, 4.0 };
        for (int i = 0; i < volumeControls.length; i++) {
            double relativeSize = sizeFactors[i] * targetKart;
            double prismSize = sizeFactors[i] * prismHeight;
            ud.geometryParts2.add(volumeControls[i]);
            ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts2,
                    relativeSize);
            mu.set.mesh.prisms(ud.mshCtrl, 0, 0, prismSize, true);
            ud.mshCtrl.setPresentationName(volumeControls[i].getPresentationName());
            ud.geometryParts2.clear();
        }
        //--
        ud.namedObjects.addAll(mu.get.partSurfaces.all(true));
        ud.namedObjects.removeAll(mu.get.partSurfaces.all(ud.geomPrt, true));
        ud.scene = mu.add.scene.geometry(ud.namedObjects);
        ud.scene.setPresentationName("Wind Tunnel");
        mu.get.scenes.displayerByREGEX(ud.scene, ".*", true).setOpacity(0.25);
        mu.set.scene.cameraView(ud.scene, ud.vv3, true);
        // mu.saveSim("prep2");
    }

    private void prep3_createBCsAndMesh() {
        ud.defUnitVel = ud.unit_kph;
        ud.region = mu.get.regions.all(true).get(0);
        if (mu.check.has.volumeMesh()) {
            mu.io.say.loud("Volume Mesh found. Skipping prep3...");
            return;
        }
        //-- Physics Continua
        ud.CFL = 150;
        ud.maxIter = 300;
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.RKE_HIGH_YPLUS);
        mu.enable.cellQualityRemediation(ud.physCont, true);
        mu.enable.expertInitialization(1, true);
        mu.enable.expertDriver(true);
        mu.set.physics.initialCondition(ud.physCont, StaticDeclarations.Vars.VEL.getVar(),
                new double[]{ 0, 0, -50 }, ud.defUnitVel);
        //--
        mu.set.boundary.asVelocityInlet(getInlet(), tunnelVel, 0., 0.02, 5.);
        mu.set.boundary.asPressureOutlet(getOutlet(), 0., 0., 0.02, 1.);
        //--
        mu.update.volumeMesh();
        mu.remove.invalidCells();
        // mu.saveSim("prep3");
    }

    private void prep4_setPost() {
        ud.namedObjects.clear();
        ud.namedObjects2.clear();
        ud.defUnitPress = ud.unit_atm;
        if (!sim.getReportManager().isEmpty()) {
            mu.io.say.loud("Post-processing already exists. Skipping prep4...");
            return;
        }
        //-- Mesh Scene with the Plane
        ud.coord1 = new double[]{ -7, 0, 0 };
        ud.plane = mu.add.derivedPart.sectionPlaneX(ud.coord1);
        ud.namedObjects.add(ud.plane);
        ud.namedObjects.add(mu.get.boundaries.byREGEX(tunnelName + ".*" + ud.bcGround, true));
        ud.namedObjects.addAll(getCarBoundaries());
        ud.scene = mu.add.scene.mesh(ud.namedObjects);
        ud.scene.setPresentationName("Volume Mesh");
        //-- Update Events
        ud.updEvent1 = mu.add.tools.updateEvent_Iteration(10, 0);
        ud.updEvent2 = mu.add.tools.updateEvent_Iteration(1, 20);
        //-- Contour Plots
        ud.namedObjects.remove(ud.plane);
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.PC.getVar(), true);
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_Dimensionless, true);
        ud.scene.setPresentationName("Pressure Kart");
        mu.set.scene.updateEvent(ud.scene, ud.updEvent1);
        ud.namedObjects2.add(ud.plane);
        ud.scene = mu.add.scene.scalar(ud.namedObjects2, ud.ff, ud.unit_Dimensionless, true);
        ud.scene.setPresentationName("Pressure Section");
        mu.set.scene.updateEvent(ud.scene, ud.updEvent1);
        mu.set.scene.cameraView(ud.scene, ud.vv2, true);
        ud.disp = mu.add.scene.displayer_Geometry(ud.scene, ud.namedObjects);
        ud.disp.setRepresentation(mu.get.mesh.geometry());
        //-- Vector Plot
        ud.scene2 = mu.add.scene.vector(ud.namedObjects2, true);
        ud.scene2.setPresentationName("Vector Section");
        mu.set.scene.updateEvent(ud.scene, ud.updEvent1);
        mu.set.scene.cameraView(ud.scene2, ud.vv2, true);
        ud.disp = mu.add.scene.displayer_Geometry(ud.scene2, ud.namedObjects);
        ud.disp.setRepresentation(mu.get.mesh.geometry());
        //-- Stopping Criteria
        ud.rep = mu.add.report.frontalArea(getCarBoundaries(), "Frontal Area",
                new double[]{ 0, 1, 0 }, new double[]{ 0, 0, -1 }, true);
        ud.rep1 = mu.add.report.forceCoefficient(getCarBoundaries(), "C_d", 0.0, 1.18, tunnelVel,
                ud.rep.getReportMonitorValue(), new double[]{ 0, 0, -1 }, true);
        ud.rep2 = mu.add.report.forceCoefficient(getCarBoundaries(), "C_l", 0.0, 1.18, tunnelVel,
                ud.rep.getReportMonitorValue(), new double[]{ 0, 1, 0 }, true);
        ud.mon1 = mu.get.monitors.byREGEX(ud.rep1.getPresentationName(), true);
        ud.mon2 = mu.get.monitors.byREGEX(ud.rep2.getPresentationName(), true);
        mu.set.object.updateEvent(ud.mon1, ud.updEvent2, true);
        mu.set.object.updateEvent(ud.mon2, ud.updEvent2, true);
        mu.add.solver.stoppingCriteria(ud.mon1, StaticDeclarations.StopCriteria.ASYMPTOTIC,
                0.002, 50);
        mu.add.solver.stoppingCriteria(ud.mon2, StaticDeclarations.StopCriteria.ASYMPTOTIC,
                0.002, 50);
        // mu.saveSim("prep4");
    }

}
