import macroutils.*;

/**
 * Demo on how to split Part Surfaces and identify them based on geometric ranges. Very useful when
 * automating processes.
 * 
 * @since Macro Utils v2b.
 * @author Fabio Kasper
 */
public class Demo4_Split_Part_Surfaces extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo4_Split_Part_Surfaces";
    importCADPart("radial_impeller.stp");
    
    geomPrt = getLeafPart(".*");
    getPartSurface(geomPrt, ".*").setPresentationName("Faces");
    splitByAngle(getPartSurfaces(geomPrt), 70);

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Min", "Y", 1.);
    partSrf.setPresentationName("bottom");
    //--
    //-- Note: Importing the same geometry in v8.06 produces a tiny triangle within the
    //--    blade. In order to fix this, the macro was rearranged to address this extra
    //--    Part Surface. It is combined afterwards into the blades.
    //--
    partSrf2 = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Min", "AREA", 0.);
    partSrf2.setPresentationName("tiny triangle");
    partSurfaces.add(partSrf2);

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Min", "AREA", 0.);
    partSrf.setPresentationName("ext tip");

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Max", "AREA", 0.);
    partSrf.setPresentationName("blade tips");
    
    partSrf = getPartSurface(geomPrt, "Faces.*");
    partSrf.setPresentationName("shaft");

    splitByPartCurves(getPartSurfaces(geomPrt, ".*tips.*"), getPartCurves(geomPrt, ".*"));

    partSrf = getPartSurface(getPartSurfaces(geomPrt, ".*tips.*"), "Max", "AREA", 0.);
    partSurfaces.add(partSrf);
    //--
    //-- Combining procedure.
    //--
    partSrf = combinePartSurfaces(partSurfaces);
    partSrf.setPresentationName("blades");
    
    combinePartSurfaces(getPartSurfaces(geomPrt, ".*tips.*"), "blade tips");
    defCamView = readCameraView("cam|-2.343460e-03,4.096864e-02,2.864518e-02|-1.684336e-02,5.075395e-01,3.809093e-01|1.234549e-02,6.027526e-01,-7.978326e-01|9.030182e-02|1");
    createScene_Geometry().open(true);
    _finalize();
  }
  
}