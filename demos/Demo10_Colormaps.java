package macroutils;

import java.awt.Color;

public class Demo10_Colormaps extends MacroUtils {

  public void execute() {

    _initUtils();
    
    simTitle = "Demo10_Colormaps";
    
    vecColor.add(Color.YELLOW);
    vecColor.add(Color.black);
    vecColor.add(Color.blue);
    addColormap("myColormap", vecColor);
    vecColor.add(Color.blue);
    vecColor.add(Color.black);
    vecColor.add(Color.YELLOW);
    addColormap("myColormap2", vecColor);
    vecColor.clear();
    vecColor.add(Color.black);
    vecColor.add(Color.white);
    vecColor.add(Color.black);
    vecColor.add(Color.white);
    vecColor.add(Color.black);
    vecColor.add(Color.white);
    vecColor.add(Color.black);
    vecColor.add(Color.white);
    addColormap("myZebra", vecColor);
    
    simpleSphPrt = createShapePartSphere(coord0, 100, unit_mm, "Sphere");
    region = assignAllPartsToRegion();
    
    defUnitLength = unit_mm;
    mshBaseSize = 10;
    createMeshContinua_PolyOnly();
    genVolumeMesh();
    
    vecObj.addAll(getAllBoundaries());
    createScene_Scalar(vecObj, getFieldFunction("Centroid"), unit_mm, true).openScene();
    
    saveSim(simTitle);
    
  }
  
  
}
