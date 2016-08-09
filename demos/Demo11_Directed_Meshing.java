import macroutils.*;
import star.vis.*;

/**
 * Example of automation involving 3D-CAD models, Mesh Operation and Directed Meshing.
 * 
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
*/
public class Demo11_Directed_Meshing extends MacroUtils {

  public void execute() {

    _initUtils();
    simTitle = "Demo11_Directed_Meshing";
    pre();
    post();
    _finalize();
    
  }
  
  void pre() {
    readCameraView("Scalar|2.579937e-02,2.282902e-02,-8.461343e-03|2.110428e-01,3.145835e-01,-1.134373e-01|-6.846060e-01,5.891221e-01,4.292433e-01|7.413223e-02|1");
    readCameraView("Mesh|4.807398e-02,4.265680e-02,3.831531e-02|3.986206e-01,3.932035e-01,3.888620e-01|0.000000e+00,1.000000e+00,0.000000e+00|1.646111e-01|1");
    //--
    defTessOpt = TESSELATION_FINE;
    dmSmooths = 10;
    mshBaseSize = 10;
    prismsLayers = 2;
    mshCont = createMeshContinua_PolyOnly();
    enablePrismLayers(mshCont);
    //--
    cadBody = create3DCad_Block(new double[] {-x, -y, -z}, new double[] {x, y, z}, unit_mm);
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    geometryParts.add(geomPrt);

    int n = 1;
    double[] coords = {-0.65, 0, 0.65};
    for (int i = 0; i < coords.length; i++) {
        double xx = x * coords[i];
        for (int j = 0; j < coords.length; j++) {
            String cyl = "Cylinder" + n;
            strings.add(cyl);
            double zz = z * coords[j];
            double[] origin = new double[] {xx, -y, zz};
            cadBody2 = create3DCad_Cylinder(r, 2 * y, origin, unit_mm, Y, cyl);
            geomPrt2 = getGeometryPart(cadBody2.getPresentationName());
            geometryParts.add(geomPrt2);
            region = assignPartToRegion(geomPrt2, false);
            region.setPresentationName(cyl);
            csys = createCoordinateSystem_Cylindrical(origin, new double[] {1, 0, 0}, new double[] {0, 0, 1});
            partSrf1 = getPartSurface(geomPrt2, "y0");
            partSrf2 = getPartSurface(geomPrt2, "y1");
            createMeshOperation_DM_Pipe(partSrf1, partSrf2, 10, 5, 40, 0.7, csys);
            n++;
        }
    }
    
    mshOpPrt = meshOperationSubtractParts(geometryParts, geomPrt);
    assignPartToRegion(mshOpPrt, false);
    
    for (String cyl : strings) {
        region = getRegion(cyl);
        bdry1 = getBoundary(region, "Def.*");
        bdry2 = getBoundary(cyl + ".Def.*");
        intrf = createDirectInterfacePair(bdry1, bdry2);
        intrf.setPresentationName(cyl);
    }
      
    genVolumeMesh();
    createScene_Mesh().open(true);
    
  }
  
  void post() {    
    defColormap = getColormap("red-2-yellow");
    plane = createSectionPlaneY(coord0, "Plane");
    plane2 = createSectionPlaneX(coord0, "Plane2");
    namedObjects.add(plane);
    namedObjects.add(plane2);
    cellSet = createCellSet(namedObjects, "CellSet");
    namedObjects2.add(cellSet);
    
    scene = createScene_Scalar(namedObjects2, getFieldFunction("Region Index"), unit_Dimensionless, false);
    disp = getDisplayer(scene, ".*");
    ((ScalarDisplayer) disp).setDisplayMeshBoolean(true);
    ((ScalarDisplayer) disp).getLegend().setVisible(false);
    scene.open(true);
  }

double x = 100, y = 100, z = 100;
double r = 25;
  
}
