package demo16;

import macroutils.MacroUtils;
import macroutils.templates.demos.Demo16;
import star.assistant.Task;
import star.assistant.annotation.StarAssistantTask;
import star.assistant.ui.FunctionTaskController;
import star.common.ScalarGlobalParameter;

/**
 * Build/Update Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        display = "Task",
        contentPath = "html/Demo16.xhtml",
        controller = BuilderTask.BuilderController.class
)
public class BuilderTask extends Task {

    private macroutils.templates.demos.Demo16 _demo16 = null;
    private final MacroUtils _mu;
    private macroutils.UserDeclarations _ud = null;

    public BuilderTask(MacroUtils m) {
        _mu = m;
    }

    /**
     * The Builder Controller class.
     */
    public class BuilderController extends FunctionTaskController {

        /**
         * Builds or updates the case.
         */
        public void buildCase() {
            initializeMacro();
            cleanUpCase();
            _demo16.updateCaseParameters();
            _demo16.executePre();
            _demo16.executePost();
            _demo16.printOverview();
        }

        /**
         * Runs the case.
         */
        public void runCase() {
            _mu.run();
        }

        /**
         * Updates the Pressure signal frequency used to build the case.
         */
        public void updateSignalFrequency() {
            initializeMacro();
            _demo16.updateCaseParameters();
            _ud.scalParam = (ScalarGlobalParameter) _mu.get.objects.scalarParameter("F", false);
            selectNode(_ud.scalParam);
            _demo16.printOverview();
        }

        private void cleanUpCase() {
            if (_mu.get.regions.all(false).isEmpty()) {
                return;
            }
            _mu.remove.allScenes();
            _mu.remove.allPlots();
            _mu.remove.allMonitors();
            _mu.remove.allReports();
            _mu.remove.allRegions();
            _mu.remove.allMeshOperations();
            _mu.remove.allParts();
            _mu.remove.allSolidModelParts();
            _mu.remove.allContinuas();
            _mu.remove.allUpdateEvents();
            _mu.remove.allUserFieldFunctions();
        }

        private void initializeMacro() {
            _mu.setSimulation(getActiveSimulation(), true);
            _mu.setDebugMode(true);
            _ud = _mu.userDeclarations;
            _demo16 = new Demo16(_mu);
        }

    }

}
