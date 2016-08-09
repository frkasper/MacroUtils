import macroutils.*;
import star.vis.*;

/**
 * How to adjust STAR-CCM+ GUI in order to get the desired resolution on Scenes. This is useful
 * for creating high quality pictures.
 * 
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo6_Scene_Resolution extends MacroUtils {

  public void execute() {
    _initUtils();
    createOrUpdateResolutionScene();
    while (true) {
        getAnnotation(".*pixels").setText(getResolution());
        sleep(slpTime * 1000);
        if (!isOpen(scene)) break;
    }
    sim.getSceneManager().remove(scene);
  }
  
  void createOrUpdateResolutionScene() {
    String sceneName = "__Resolution__";
    scene = getScene(sceneName);
    if (scene != null) {
        return;
    }
    scene = createScene_Empty();
    scene.setPresentationName(sceneName);
    sleep(250);
    createAnnotation_Text(scene, "Scene Resolution", 0.08, new double[] {0.1, 0.6, 0});
    createAnnotation_Text(scene, "Close this Scene to stop the Macro...", 0.04, new double[] {0.1, 0.01, 0});
    String updS = String.format("Refreshing Scene in %d seconds...", slpTime);
    res = createAnnotation_Text(scene, getResolution(), 0.05, new double[] {0.1, 0.5, 0});
    createAnnotation_Text(scene, updS, 0.05, new double[] {0.1, 0.4, 0});
    scene.open(true);
  }
  
  String getResolution() {
    int px = scene.getWidth();
    int py = scene.getHeight();
    return String.format("%d x %d pixels", px, py);
  }
        
  FixedAspectAnnotationProp res;
  int slpTime = 5;
  
}
