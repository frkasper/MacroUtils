import java.awt.Color;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;
import star.vis.ScalarDisplayer;

/**
 * This method is for creating custom Colormaps.
 *
 * @since Macro Utils v2d.
 * @author Fabio Kasper
 */
public class Demo10_Colormaps extends StarMacro {

    private MacroUtils mu;
    private UserDeclarations ud;

    @Override
    public void execute() {

        mu = new MacroUtils(getActiveSimulation());

        ud = mu.userDeclarations;

        ud.simTitle = "Demo10_Colormaps";

        ud.defCamView = mu.io.read.cameraView("cam|7.698946e-03,-2.109472e-02,7.961378e-02"
                + "|7.698946e-03,-2.109472e-02,6.679604e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|1.288606e-01|1", true);

        addColormaps();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void addColormaps() {
        ud.colors.add(Color.YELLOW);
        ud.colors.add(Color.black);
        ud.colors.add(Color.blue);
        mu.add.tools.colormap("myColormap", ud.colors, null, StaticDeclarations.ColorSpace.RGB);
        ud.colors.clear();
        ud.colors.add(Color.GREEN);
        ud.colors.add(Color.YELLOW);
        ud.colors.add(Color.BLACK);
        ud.colors.add(Color.WHITE);
        ud.defColormap = mu.add.tools.colormap("myColormap2", ud.colors, null,
                StaticDeclarations.ColorSpace.HSV);

        ud.simpleSphPrt = mu.add.geometry.sphere(StaticDeclarations.COORD0, 100, ud.unit_mm);
        ud.simpleSphPrt.setPresentationName("Sphere");
        ud.region = mu.add.region.fromAll(true);

        ud.defUnitLength = ud.unit_mm;
        ud.mshBaseSize = 7.5;
        ud.mshOp = mu.add.meshOperation.automatedMesh(mu.get.geometries.all(true),
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.POLY_MESHER);
        mu.update.volumeMesh();

        ud.namedObjects.addAll(mu.get.boundaries.all(true));
        ud.scene = mu.add.scene.scalar(ud.namedObjects,
                mu.get.objects.fieldFunction("Centroid", true), ud.unit_mm, true);
        ((ScalarDisplayer) mu.get.scenes.displayerByREGEX(ud.scene, ".*", true))
                .setDisplayMeshBoolean(true);
        ud.scene.open();
    }

}
