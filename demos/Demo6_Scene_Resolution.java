package macroutils;

import star.vis.*;

public class Demo6_Scene_Resolution extends MacroUtils {

  public void execute() {
    _initUtils();
    createOrUpdateResolutionScene();
    //--
    int slpTime = 5;
    String updS = String.format("Refreshing Scene in %d seconds...", slpTime);
    FixedAspectAnnotationProp res = null;
    //--
    while (true) {
        int px = scene.getWidth();
        int py = scene.getHeight();
        String resS = String.format("%d x %d pixels", px, py);
        if (res == null) {
            res = createAnnotation_Text(scene, resS, 0.05, new double[] {0.1, 0.5, 0});
            createAnnotation_Text(scene, updS, 0.05, new double[] {0.1, 0.4, 0});
        } else {
            getAnnotation(".*pixels").setText(resS);
        }
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
    scene.open(true);
    sleep(250);
    createAnnotation_Text(scene, "Scene Resolution", 0.08, new double[] {0.1, 0.6, 0});
    createAnnotation_Text(scene, "Close this Scene to stop the Macro...", 0.04, new double[] {0.1, 0.01, 0});
  }
  
}
