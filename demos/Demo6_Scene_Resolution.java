import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.common.StarMacro;

/**
 * How to adjust STAR-CCM+ GUI in order to get the desired resolution on Scenes. This is useful for
 * creating high quality pictures.
 *
 * @since Macro Utils v2c.
 * @author Fabio Kasper
 */
public class Demo6_Scene_Resolution extends StarMacro {

    private MacroUtils mu;
    private final int slpTime = 5;
    private UserDeclarations ud;

    @Override
    public void execute() {

        initMacro();

        createOrUpdateResolutionScene();

        while (true) {

            mu.get.objects.annotation(".*pixels", false).setText(getResolution());

            mu.io.sleep(slpTime * 1000);

            if (!mu.check.is.open(ud.scene)) {

                break;

            }
        }

        mu.getSimulation().getSceneManager().remove(ud.scene);

    }

    private void createOrUpdateResolutionScene() {
        String sceneName = "__Resolution__";
        ud.scene = mu.get.scenes.byREGEX(sceneName, true);
        if (ud.scene != null) {
            return;
        }
        ud.scene = mu.add.scene.empty();
        ud.scene.setPresentationName(sceneName);
        mu.io.sleep(250);
        mu.add.scene.annotation(ud.scene, "Scene Resolution", 0.08, new double[]{ 0.1, 0.6, 0 });
        mu.add.scene.annotation(ud.scene, "Close this Scene to stop the Macro...", 0.04,
                new double[]{ 0.1, 0.01, 0 });
        String s = String.format("Refreshing Scene in %d seconds...", slpTime);
        mu.add.scene.annotation(ud.scene, getResolution(), 0.05, new double[]{ 0.1, 0.5, 0 });
        mu.add.scene.annotation(ud.scene, s, 0.05, new double[]{ 0.1, 0.4, 0 });
        ud.scene.open();
    }

    private String getResolution() {
        int px = ud.scene.getWidth();
        int py = ud.scene.getHeight();
        return String.format("%d x %d pixels", px, py);
    }

    private void initMacro() {
        mu = new MacroUtils(getActiveSimulation());
        ud = mu.userDeclarations;
    }

}
