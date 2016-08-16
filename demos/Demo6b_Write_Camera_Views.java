import macroutils.*;
import star.common.*;

public class Demo6b_Write_Camera_Views extends StarMacro {

    public void execute() {
        new MacroUtils(getActiveSimulation()).io.write.cameraViews("myCameras.txt");
    }

}
