
import macroutils.MacroUtils;
import star.common.StarMacro;
import star.vis.Scene;
import star.vis.VisView;

public class Demo6c_Read_Camera_Views extends StarMacro {

    public void execute() {

        MacroUtils mu = new MacroUtils(getActiveSimulation());

        mu.io.read.cameraViews("myCameras.txt");

        Scene scene = mu.get.scenes.byREGEX(".*", true);

        for (VisView vv : mu.get.cameras.all(true)) {

            mu.set.scene.cameraView(scene, vv, true);

            mu.io.sleep(1000);   // Wait one second.

            mu.io.write.picture(scene, "pic " + vv.getPresentationName(), 1280, 720, true);

        }
    }

}
