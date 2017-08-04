
import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.DoubleVector;
import star.base.neo.NamedObject;
import star.common.Boundary;
import star.common.Simulation;
import star.common.StarMacro;
import star.meshing.SurfaceCustomMeshControl;

/**
 * Complete workflow from CAD to finish on a Lego Kart.
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo5_Lego_Kart_Wind_Tunnel extends StarMacro {

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

    void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo5_Lego_Kart_Wind_Tunnel";
    }

    void prep1_importGeometry() {
        if (!sim.getGeometryPartManager().isEmpty()) {
            mu.io.say.loud("Geometry already created. Skipping prep1...");
            return;
        }
        ud.vv1 = mu.io.read.cameraView("cam1|-2.491101e-03,1.572050e-02,-9.156700e-03|8.364881e-01,3.962117e-01,8.597759e-01|-2.296008e-01,9.533891e-01,-1.957871e-01|4.813384e-02|1", true);
        ud.vv2 = mu.io.read.cameraView("cam2|-6.473358e-03,4.801700e-02,-3.333981e-02|1.001025e+00,4.801700e-02,-3.333981e-02|0.000000e+00,1.000000e+00,0.000000e+00|7.945790e-02|1", true);
        ud.vv3 = mu.io.read.cameraView("cam3|-1.447091e-02,7.527911e-02,-2.683634e-01|1.812691e+00,1.271046e+00,2.099190e+00|-2.793358e-01,9.264438e-01,-2.523359e-01|3.358636e-01|1", true);
        mu.set.userDefault.cameraView(ud.vv1);
        mu.add.geometry.importPart("LegoKart.x_b");
        ud.scene = mu.add.scene.geometry();
        ud.scene.open();
        // mu.saveSim("prep1");
    }

    void prep2_createTunnelWrapAndRegion() {
        if (!sim.getRegionManager().isEmpty()) {
            mu.io.say.loud("Region already created. Skipping prep2...");
            return;
        }
        //-- Save car boundaries for later, i.e., Block refinement.
        ud.partSurfaces.addAll(mu.get.partSurfaces.all(true));
        ud.geometryObjects.addAll(ud.partSurfaces);
        //-- Get Car Extents and Calculate Wind Tunnel Dimensions
        DoubleVector dvExtents = mu.get.partSurfaces.extents(ud.partSurfaces);
        minX = dvExtents.get(0);
        maxX = dvExtents.get(1);
        minY = dvExtents.get(2);
        maxY = dvExtents.get(3);
        minZ = dvExtents.get(4);
        maxZ = dvExtents.get(5);
        ud.coord1 = new double[]{minX - 2 * (maxX - minX), minY + 1.25, minZ - 6 * (maxZ - minZ)};
        ud.coord2 = new double[]{maxX + 2 * (maxX - minX), maxY + 3 * (maxY - minY), maxZ + 3 * (maxZ - minZ)};
        //--
        ud.cadPrt = mu.add.geometry.block3DCAD(ud.coord1, ud.coord2, ud.unit_mm);
        ud.cadPrt.setPresentationName(tunnelName);
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "z1", true).setPresentationName(ud.bcInlet);
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "z0", true).setPresentationName(ud.bcOutlet);
        ud.partSrf1 = mu.get.partSurfaces.byREGEX(ud.cadPrt, "y0", true);
        ud.partSrf1.setPresentationName(ud.bcGround);
        ud.geometryObjects.add(ud.partSrf1);
        ud.partSurfaces2.addAll(mu.get.partSurfaces.allByREGEX(ud.cadPrt, "y.*", true));
        ud.partSurfaces2.addAll(mu.get.partSurfaces.allByREGEX(ud.cadPrt, "x.*", true));
        ud.partSrf2 = mu.set.geometry.combinePartSurfaces(ud.partSurfaces2, true);
        ud.partSrf2.setPresentationName(ud.bcWalls);
        //-- Mesh settings
        ud.mshBaseSize = 5;                            //-- Use 3 if you have a better machine.
        ud.prismsLayers = 2;
        ud.prismsRelSizeHeight = 20;
        ud.prismsNearCoreAspRat = 1.0;
        ud.mshSrfSizeMin = 12.5;
        ud.mshSrfCurvNumPoints = 48;
        ud.mshTrimmerGrowthRate = StaticDeclarations.GrowthRate.MEDIUM;
        ud.mshTrimmerMaxCellSize = 8 * ud.mshSrfSizeTgt;
        ud.wrapMshOp = mu.add.meshOperation.surfaceWrapper(mu.get.geometries.all(true), wrapName);
        mu.set.mesh.baseSize(ud.wrapMshOp, 0.6 * ud.mshBaseSize, ud.unit_mm, true);
        ud.geometryObjects2.add(ud.cadPrt);
        ud.mshCtrl1 = mu.add.meshOperation.surfaceControl(ud.wrapMshOp, ud.geometryObjects2, 0., 1600.);
        ud.mshCtrl1.setPresentationName(tunnelName);
        //-- Create Contact Preventions
        mu.add.meshOperation.contactPrevention(ud.wrapMshOp, ud.geometryObjects, 1., ud.unit_mm);
        ud.geomPrt = mu.get.geometries.byREGEX(ud.wrapMshOp.getPresentationName(), true);
        ud.region = mu.add.region.fromPart(ud.geomPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        ud.region.setPresentationName(tunnelName);
        //--
        ud.geometryParts.add(ud.geomPrt);
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.AUTOMATIC_SURFACE_REPAIR,
                StaticDeclarations.Meshers.TRIMMER_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("Mesh of " + ud.geomPrt.getPresentationName());
        ud.geometryParts.clear();
        //-- Create Custom Surface Controls
        ud.mshCtrl2 = mu.add.meshOperation.surfaceControl((SurfaceCustomMeshControl) ud.mshCtrl1, ud.mshOp);
        mu.set.mesh.prisms((SurfaceCustomMeshControl) ud.mshCtrl2, 3, 1.2, 2 * ud.mshSrfSizeTgt, true);
        //--
        //-- Create Custom Volume Controls
        ud.geomPrt = mu.add.geometry.block(ud.partSurfaces, new double[]{-0.5, 0, -6}, new double[]{0.5, 1, 1});
        ud.geomPrt.setPresentationName("Mesh Refine 1");
        ud.geometryParts.add(ud.geomPrt);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 2 * ud.mshSrfSizeTgt);
        ud.mshCtrl.setPresentationName(ud.geomPrt.getPresentationName());
        ud.geometryParts.clear();
        //--
        ud.geomPrt = mu.add.geometry.block(ud.partSurfaces, new double[]{-0.2, 0, -2.5}, new double[]{0.2, 0.25, 0.25});
        ud.geomPrt.setPresentationName("Mesh Refine 2");
        ud.geometryParts.add(ud.geomPrt);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, ud.mshSrfSizeTgt);
        ud.mshCtrl.setPresentationName(ud.geomPrt.getPresentationName());
        ud.geometryParts.clear();
        //--
        ud.geomPrt = mu.add.geometry.block(ud.partSurfaces, new double[]{-0.05, 0, -0.5}, new double[]{0.05, 0.05, 0.025});
        ud.geomPrt.setPresentationName("Mesh Refine 3");
        ud.geometryParts.add(ud.geomPrt);
        ud.mshCtrl = mu.add.meshOperation.volumetricControl(ud.mshOp, ud.geometryParts, 0.5 * ud.mshSrfSizeTgt);
        ud.mshCtrl.setPresentationName(ud.geomPrt.getPresentationName());
        //--
        ud.namedObjects.addAll(mu.get.partSurfaces.all(true));
        ud.namedObjects.removeAll(mu.get.partSurfaces.all(ud.geomPrt, true));
        ud.scene = mu.add.scene.geometry(ud.namedObjects);
        ud.scene.setPresentationName("Wind Tunnel");
        mu.get.scenes.displayerByREGEX(ud.scene, ".*", true).setOpacity(0.25);
        mu.set.scene.cameraView(ud.scene, ud.vv3, true);
        // mu.saveSim("prep2");
    }

    void prep3_createBCsAndMesh() {
        ud.defUnitVel = ud.unit_kph;
        ud.region = mu.get.regions.all(true).get(0);
        if (mu.check.has.volumeMesh()) {
            mu.io.say.loud("Volume Mesh found. Skipping prep3...");
            return;
        }
        //-- Physics Continua
        ud.CFL = 150;
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.COUPLED, StaticDeclarations.Density.INCOMPRESSIBLE,
                StaticDeclarations.Energy.ISOTHERMAL, StaticDeclarations.Viscous.RKE_HIGH_YPLUS);
        mu.enable.cellQualityRemediation(ud.physCont, true);
        mu.enable.expertInitialization(1, true);
        mu.enable.expertDriver(true);
        mu.set.physics.initialCondition(ud.physCont, StaticDeclarations.Vars.VEL.getVar(),
                new double[]{0, 0, -50}, ud.defUnitVel);
        //--
        mu.set.boundary.asVelocityInlet(getInlet(), tunnelVel, 0., 0.02, 5.);
        mu.set.boundary.asPressureOutlet(getOutlet(), 0., 0., 0.02, 1.);
        //--
        mu.update.volumeMesh();
        mu.remove.invalidCells();
        // mu.saveSim("prep3");
    }

    void prep4_setPost() {
        ud.namedObjects.clear();
        ud.namedObjects2.clear();
        ud.defUnitPress = ud.unit_atm;
        if (!sim.getReportManager().isEmpty()) {
            mu.io.say.loud("Post-processing already exists. Skipping prep4...");
            return;
        }
        //-- Mesh Scene with the Plane
        ud.coord1 = new double[]{-7, 0, 0};
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
                new double[]{0, 1, 0}, new double[]{0, 0, -1}, true);
        ud.rep1 = mu.add.report.forceCoefficient(getCarBoundaries(), "C_d", 0.0, 1.18, tunnelVel,
                ud.rep.getReportMonitorValue(), new double[]{0, 0, -1}, true);
        ud.rep2 = mu.add.report.forceCoefficient(getCarBoundaries(), "C_l", 0.0, 1.18, tunnelVel,
                ud.rep.getReportMonitorValue(), new double[]{0, 1, 0}, true);
        ud.mon1 = mu.get.monitors.byREGEX(ud.rep1.getPresentationName(), true);
        ud.mon2 = mu.get.monitors.byREGEX(ud.rep2.getPresentationName(), true);
        mu.set.object.updateEvent(ud.mon1, ud.updEvent2, true);
        mu.set.object.updateEvent(ud.mon2, ud.updEvent2, true);
        mu.add.solver.stoppingCriteria(ud.mon1, StaticDeclarations.StopCriteria.ASYMPTOTIC, 0.001, 100);
        mu.add.solver.stoppingCriteria(ud.mon2, StaticDeclarations.StopCriteria.ASYMPTOTIC, 0.001, 100);
        // mu.saveSim("prep4");
    }

    ArrayList<NamedObject> getCarBoundaries() {
        return new ArrayList(mu.get.boundaries.allByREGEX("^((?!" + tunnelName + ").)*$", false));
    }

    Boundary getInlet() {
        return mu.get.boundaries.byREGEX(tunnelName + ".*" + ud.bcInlet, true);
    }

    Boundary getOutlet() {
        return mu.get.boundaries.byREGEX(tunnelName + ".*" + ud.bcOutlet, true);
    }

    private MacroUtils mu;
    private Simulation sim;
    private UserDeclarations ud;

    double minX, maxX, minY, maxY, minZ, maxZ;
    double tunnelVel = 50;
    String tunnelName = "Tunnel";
    String wrapName = "Kart Wrap";

}
