
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * Demo on how to split Part Surfaces and identify them based on geometric ranges. Very useful when automating
 * processes.
 *
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo4_Split_Part_Surfaces extends StarMacro {

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
        mu.set.geometry.splitPartSurfacesByAngle(mu.get.partSurfaces.all(ud.geomPrt, true), 70, true);

        ud.partSrf = mu.get.partSurfaces.byRangeMin(mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true),
                StaticDeclarations.Axis.Y, 1.);
        ud.partSrf.setPresentationName("bottom");

        //--
        //-- Note: Importing the same geometry in v8.06 produces a tiny triangle within the
        //--    blade. In order to fix this, the macro was rearranged to address this extra
        //--    Part Surface. It is combined afterwards into the blades.
        //--
        ud.partSrf2 = mu.get.partSurfaces.byAreaMin(mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true));
        ud.partSrf2.setPresentationName("tiny triangle");
        ud.partSurfaces.add(ud.partSrf2);

        ud.partSrf = mu.get.partSurfaces.byAreaMin(mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true));
        ud.partSrf.setPresentationName("ext tip");

        ud.partSrf = mu.get.partSurfaces.byAreaMax(mu.get.partSurfaces.allByREGEX(ud.geomPrt, "Faces.*", true));
        ud.partSrf.setPresentationName("blade tips");

        ud.partSrf = mu.get.partSurfaces.byREGEX("Faces.*", true);
        ud.partSrf.setPresentationName("shaft");

        mu.set.geometry.splitPartSurfacesByPartCurves(
                mu.get.partSurfaces.allByREGEX(ud.geomPrt, ".*tips.*", true),
                mu.get.partCurves.allByREGEX(ud.geomPrt, ".*", true),
                true);

        ud.partSrf = mu.get.partSurfaces.byAreaMax(mu.get.partSurfaces.allByREGEX(ud.geomPrt, ".*tips.*", true));
        ud.partSurfaces.add(ud.partSrf);

        //--
        //-- Combining procedure.
        //--
        ud.partSrf = mu.set.geometry.combinePartSurfaces(ud.partSurfaces, true);
        ud.partSrf.setPresentationName("blades");

        ud.partSurfaces2.addAll(mu.get.partSurfaces.allByREGEX(ud.geomPrt, ".*tips.*", true));
        mu.set.geometry.combinePartSurfaces(ud.partSurfaces2, true).setPresentationName("blade tips");

        ud.defCamView = mu.io.read.cameraView("cam|-2.343460e-03,4.096864e-02,2.864518e-02|-1.684336e-02,5.075395e-01,3.809093e-01|1.234549e-02,6.027526e-01,-7.978326e-01|9.030182e-02|1", true);
        mu.add.scene.geometry().open();
    }

    private MacroUtils mu;
    private UserDeclarations ud;

}
