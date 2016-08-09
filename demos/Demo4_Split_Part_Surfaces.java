package macroutils;

public class Demo4_Split_Part_Surfaces extends MacroUtils {

  public void execute() {
    _initUtils();
    simTitle = "Demo4_SplitPartSurfaces";
    importCADPart("radial_impeller.stp");
    
    geomPrt = getLeafPart(".*");
    getPartSurface(geomPrt, ".*").setPresentationName("Faces");
    splitByAngle(geomPrt.getPartSurfaces(), 70);

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Min", "Y", 1.);
    partSrf.setPresentationName("bottom");

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Min", "AREA", 0.);
    partSrf.setPresentationName("ext tip");

    partSrf = getPartSurface(getPartSurfaces(geomPrt, "Faces.*"), "Max", "AREA", 0.);
    partSrf.setPresentationName("blade tips");
    
    partSrf = getPartSurface(geomPrt, "Faces.*");
    partSrf.setPresentationName("shaft");

    splitByPartCurves(getPartSurfaces(geomPrt, ".*tips.*"), getPartCurves(geomPrt, ".*"));

    partSrf = getPartSurface(getPartSurfaces(geomPrt, ".*tips.*"), "Max", "AREA", 0.);
    partSrf.setPresentationName("blades");

    combinePartSurfaces(getPartSurfaces(geomPrt, ".*tips.*"), "blade tips");
    createScene_Geometry().openScene();
    _finalize();
  }
  
}