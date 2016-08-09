import macroutils.*;
import java.awt.*;
import star.vis.*;

/**
 * This method is for creating custom Colormaps. 
 * 
 * @since Macro Utils v2d.
 * @author Fabio Kasper
 */
public class Demo10_Colormaps extends MacroUtils {

  public void execute() {

    _initUtils();
    
    simTitle = "Demo10_Colormaps";
    defCamView = readCameraView("cam|7.698946e-03,-2.109472e-02,7.961378e-02|7.698946e-03,-2.109472e-02,6.679604e-01|0.000000e+00,1.000000e+00,0.000000e+00|1.288606e-01|1");
    
    colors.add(Color.YELLOW);
    colors.add(Color.black);
    colors.add(Color.blue);
    addColormap("myColormap", colors);
    colors.add(Color.blue);
    colors.add(Color.black);
    colors.add(Color.YELLOW);
    defColormap = addColormap("myColormap2", new Color[] {Color.GREEN, Color.YELLOW, Color.BLACK});
    
    simpleSphPrt = createShapePartSphere(coord0, 100, unit_mm, "Sphere");
    region = assignAllPartsToRegion();
    
    defUnitLength = unit_mm;
    mshBaseSize = 10;
    createMeshContinua_PolyOnly();
    genVolumeMesh();
    
    namedObjects.addAll(getAllBoundaries());
    scene = createScene_Scalar(namedObjects, getFieldFunction("Centroid"), unit_mm, true);
    ((ScalarDisplayer) getDisplayer(scene, ".*")).setDisplayMeshBoolean(true);
    scene.open(true);
    
    _finalize();
    
  }
  
  
}
