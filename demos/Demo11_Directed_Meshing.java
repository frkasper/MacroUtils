import macroutils.*;
import star.common.*;
import star.meshing.*;
import star.vis.*;

/**
 * Example of automation involving 3D-CAD models, Mesh Operation and Directed Meshing.
 *
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
 */
public class Demo11_Directed_Meshing extends StarMacro {

    public void execute() {

        initMacro();

        pre();

        post();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo11_Directed_Meshing";
    }

    void pre() {
        mu.io.read.cameraView("Scalar|2.579937e-02,2.282902e-02,-8.461343e-03|2.110428e-01,3.145835e-01,-1.134373e-01|-6.846060e-01,5.891221e-01,4.292433e-01|7.413223e-02|1", true);
        mu.io.read.cameraView("Mesh|4.807398e-02,4.265680e-02,3.831531e-02|3.986206e-01,3.932035e-01,3.888620e-01|0.000000e+00,1.000000e+00,0.000000e+00|1.646111e-01|1", true);
        //--
        ud.defTessOpt = StaticDeclarations.Tessellation.FINE;
        ud.dmSmooths = 10;
        ud.mshBaseSize = 10;
        ud.prismsLayers = 2;
        //--
        int n = 1;
        double[] coords = {-0.65, 0, 0.65};
        for (int i = 0; i < coords.length; i++) {
            double xx = x * coords[i];
            for (int j = 0; j < coords.length; j++) {
                String cyl = "Cylinder" + n;
                ud.strings.add(cyl);
                double zz = z * coords[j];
                double[] origin = new double[]{xx, -y, zz};
                ud.cadPrt2 = mu.add.geometry.cylinder3DCAD(r, 2 * y, origin, ud.unit_mm, StaticDeclarations.Axis.Y);
                ud.cadPrt2.setPresentationName(cyl);
                ud.geometryParts.add(ud.cadPrt2);
                ud.region = mu.add.region.fromPart(ud.cadPrt2,
                        StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                        StaticDeclarations.InterfaceMode.CONTACT,
                        StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
                ud.region.setPresentationName(cyl);
                ud.csys = mu.add.tools.coordinateSystem_Cylindrical(origin, new double[]{1, 0, 0},
                        new double[]{0, 0, 1});
                ud.partSrf1 = mu.get.partSurfaces.byREGEX(ud.cadPrt2, "y0", true);
                ud.partSrf2 = mu.get.partSurfaces.byREGEX(ud.cadPrt2, "y1", true);
                mu.add.meshOperation.directedMeshing_Pipe(ud.partSrf1, ud.partSrf2, 10, 5, 40, 0.7, ud.csys);
                n++;
            }
        }
        //--
        ud.cadPrt = mu.add.geometry.block3DCAD(new double[]{-x, -y, -z}, new double[]{x, y, z}, ud.unit_mm);
        ud.geometryParts.add(ud.cadPrt);
        //--
        ud.mshOpPrt = mu.add.meshOperation.subtract(ud.geometryParts, ud.cadPrt);
        ud.geometryParts.add(ud.mshOpPrt);
        ud.geometryParts2.add(ud.mshOpPrt);
        ud.geometryParts.remove(ud.cadPrt);
        ud.mshOp = mu.add.meshOperation.imprint(ud.geometryParts, 1,
                ImprintMergeImprintMethodOption.Type.DISCRETE_IMPRINT,
                ImprintResultingMeshTypeOption.Type.CONFORMAL);
        //--
        //-- Note even though imprinting is conformal,
        //-- mesh will be non-conformal because different
        //-- algorithms are being applied in this case.
        //--
        ud.mshOp.execute();
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                StaticDeclarations.FeatureCurveMode.ONE_FOR_ALL, true);
        //--
        ud.mshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.POLY_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.mshOp.setPresentationName("My Mesh");
        //--
        ud.geometryObjects.addAll(mu.get.partSurfaces.allByREGEX(ud.mshOpPrt, ".*y0|.*y1", true));
        ud.mshCtrl = mu.add.meshOperation.surfaceControl((AutoMeshOperation) ud.mshOp, ud.geometryObjects, 0, 0, 0);
        ud.mshCtrl.setPresentationName("Disable Prisms");
        //--
        mu.update.volumeMesh();
        ud.scene = mu.add.scene.mesh();
        ud.scene.open();
    }

    void post() {
        ud.defColormap = mu.get.objects.colormap(StaticDeclarations.Colormaps.KELVIN_TEMPERATURE);
        ud.plane = mu.add.derivedPart.sectionPlaneY(StaticDeclarations.COORD0);
        ud.plane2 = mu.add.derivedPart.sectionPlaneX(StaticDeclarations.COORD0);
        ud.namedObjects.add(ud.plane);
        ud.namedObjects.add(ud.plane2);
        ud.cellSrf = mu.add.derivedPart.cellSurface(ud.namedObjects);
        ud.cellSrf.setPresentationName("Cell Surface");
        ud.namedObjects2.add(ud.cellSrf);

        ud.scene = mu.add.scene.scalar(ud.namedObjects2, mu.get.objects.fieldFunction("Region Index", true),
                ud.unit_Dimensionless, false);
        ud.disp = mu.get.scenes.displayerByREGEX(ud.scene, ".*", true);
        ((ScalarDisplayer) ud.disp).setDisplayMeshBoolean(true);
        ((ScalarDisplayer) ud.disp).getLegend().setVisible(false);
        ud.scene.open();
    }

    double x = 100, y = 100, z = 100;
    double r = 25;

    private MacroUtils mu;
    private Simulation sim;
    private UserDeclarations ud;

}
