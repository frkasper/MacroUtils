package macroutils;

import star.vis.*;

public class Demo6c_Read_Camera_Views extends MacroUtils {

  public void execute() {
    _initUtils();
    readCameraViews("myCameras.txt");
    scene = getScene(".*");
    for (VisView vv : getCameraViews(".*")) {
        setSceneCameraView(scene, vv);
        sleep(1000);   // Wait a second.
        //hardCopyPicture(scene, "pic " + vv.getPresentationName(), 1280, 720);
    }
  }
  
}
