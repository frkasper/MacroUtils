package simassistants.demo16;

import macroutils.*;
import star.assistant.*;
import star.assistant.annotation.*;
import star.assistant.ui.*;
import star.common.*;

/**
 * Build/Update Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(display = "Task", contentPath = "html/Demo16.xhtml", controller = BuilderTask.BuilderController.class)
public class BuilderTask extends Task {

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
            _ud.scalParam = (ScalarGlobalParameter) _mu.get.objects.parameter("F", false);
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
            _demo16 = _mu.templates.demos.demo16;
        }

    }

    //--
    //-- Variables declaration area.
    //--
    private final MacroUtils _mu;
    private macroutils.templates.demos.Demo16 _demo16 = null;
    private macroutils.UserDeclarations _ud = null;

}
