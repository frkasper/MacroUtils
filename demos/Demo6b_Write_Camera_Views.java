import macroutils.MacroUtils;
import star.common.StarMacro;

public class Demo6b_Write_Camera_Views extends StarMacro {

    @Override
    public void execute() {

        new MacroUtils(getActiveSimulation()).io.write.cameraViews("myCameras.txt");

    }

}
