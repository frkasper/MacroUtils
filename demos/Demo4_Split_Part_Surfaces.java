
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * Demo on how to split Part Surfaces and identify them based on geometric ranges. Very useful when
 * automating processes.
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo4_Split_Part_Surfaces extends StarMacro {

    private MacroUtils mu;
    private UserDeclarations ud;

    @Override
    public void execute() {

        mu = new MacroUtils(getActiveSimulation());

        ud = mu.userDeclarations;

        ud.simTitle = "Demo4_Split_Part_Surfaces";

        importGeometryAndSplitPartSurfaces();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void importGeometryAndSplitPartSurfaces() {

        mu.add.geometry.importPart("radial_impeller.stp");

        ud.geomPrt = mu.get.geometries.byREGEX(".*", true);
        mu.get.partSurfaces.byREGEX(ud.geomPrt, ".*", true).setPresentationName("Faces");

        //--
        //-- First split by angle and rename the first two surfaces
        //--
        mu.set.geometry.splitPartSurfacesByAngle(
                mu.get.partSurfaces.all(ud.geomPrt, true),
                85, true);

        ud.partSrf = mu.get.partSurfaces.byRangeMin(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true),
                StaticDeclarations.Axis.Y, 1.);
        ud.partSrf.setPresentationName("bottom");

        ud.partSrf = mu.get.partSurfaces.byAreaMin(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true));
        ud.partSrf.setPresentationName("shaft");

        //--
        //-- Then split blades by the Part Curve and rename two more surfaces
        //--
        mu.set.geometry.splitPartSurfacesByPartCurves(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true),
                mu.get.partCurves.allByREGEX(ud.geomPrt, ".*", true),
                true);

        ud.partSrf = mu.get.partSurfaces.byRangeMin(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true),
                StaticDeclarations.Axis.Y, 20.);
        ud.partSrf.setPresentationName("ext tip");

        ud.partSrf = mu.get.partSurfaces.byAreaMax(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true));
        ud.partSrf.setPresentationName("blades");

        //--
        //-- Finally combine the remaining surfaces and rename it.
        //--
        ud.partSrf = mu.set.geometry.combinePartSurfaces(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true), true);
        ud.partSrf.setPresentationName("blades tips");

        ud.defCamView = mu.io.read.cameraView("cam|-2.343460e-03,4.096864e-02,2.864518e-02"
                + "|-1.684336e-02,5.075395e-01,3.809093e-01|1.234549e-02,6.027526e-01,-7.978326e-01"
                + "|9.030182e-02|1", true);
        mu.add.scene.geometry().open();

    }

}
