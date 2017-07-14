package simplehexamesher;

import macroutils.MacroUtils;
import macroutils.UserDeclarations;
import star.assistant.Task;
import star.assistant.annotation.StarAssistantTask;
import star.assistant.ui.FunctionTaskController;
import star.base.neo.DoubleVector;
import star.common.Region;
import star.common.Simulation;
import star.common.VectorGlobalParameter;
import star.vis.Annotation;
import star.vis.Scene;

/**
 * Block Mesher Task.
 *
 * @since MacroUtils v11.06.
 * @author Fabio Kasper
 */
@StarAssistantTask(
        contentPath = "html/BlockTask.xhtml",
        controller = BlockTask.MesherController.class,
        display = "Block Mesher"
)
public class BlockTask extends Task {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public BlockTask(MacroUtils m) {
        _mu = m;
    }

    /**
     * Current {@link FunctionTaskController} subclass.
     */
    public class MesherController extends FunctionTaskController {

        /**
         * Generate Mesh Task.
         */
        public void generateMesh() {
            initializeTask();
            if (!haveParameters()) {
                return;
            }
            Annotation an = _mu.get.objects.annotation(ANNOT_NAME, false);
            boolean is2D = an != null;
            _mu.templates.mesh.setBadgeFor2D(is2D);
            VectorGlobalParameter c1 = (VectorGlobalParameter) _mu.get.objects.parameter(BLOCK_C1, false);
            VectorGlobalParameter c2 = (VectorGlobalParameter) _mu.get.objects.parameter(BLOCK_C2, false);
            VectorGlobalParameter nc = (VectorGlobalParameter) _mu.get.objects.parameter(BLOCK_NCELLS, false);
            Region r = _mu.templates.mesh.hexaBlock(getDouble(c1), getDouble(c2), c1.getQuantity().getUnits(),
                    getInt(nc), "Block");
            if (is2D) {
                _ud.namedObjects.add(r);
                an.getAnnotationManager().remove(an);
                _mu.templates.mesh.setBadgeFor2D(false);
            } else {
                _ud.namedObjects.addAll(_mu.get.boundaries.all(r, false));
            }
            Scene scn = _mu.add.scene.mesh(_ud.namedObjects);
            scn.open();
            if (!is2D) {
                scn.setViewOrientation(new DoubleVector(new double[]{-1.0, 1.0, -1.0}),
                        new DoubleVector(new double[]{0.0, 1.0, 0.0}));
            }
            scn.resetCamera();
            _ud.namedObjects.clear();
            _mu.io.say.action("Generate Mesh Task is finished.", true);
        }

        /**
         * Set as Two-Dimensional Task.
         */
        public void setAs2D() {
            initializeTask();
            _mu.add.tools.annotation_Simple(ANNOT_NAME, "");
            _mu.io.say.action("Geometry will be flagged as 2D.", true);
            _mu.io.say.action("Set as Two-Dimensional Task is finished.", true);
        }

        /**
         * Set Parameters Task.
         */
        public void setParameters() {
            initializeTask();
            VectorGlobalParameter vgp = _mu.add.tools.parameter_Vector(BLOCK_C1, new double[]{0, 0, 0}, _ud.unit_m);
            _mu.add.tools.parameter_Vector(BLOCK_C2, new double[]{1, 1, 1}, _ud.unit_m);
            _mu.add.tools.parameter_Vector(BLOCK_NCELLS, new double[]{2, 4, 6}, _ud.unit_Dimensionless);
            selectNodeExclusive(vgp);
            _mu.io.say.action("Set Parameters Task is finished.", true);
        }

        private double[] getDouble(VectorGlobalParameter vgp) {
            return vgp.getQuantity().getVector().toDoubleArray();
        }

        private int[] getInt(VectorGlobalParameter vgp) {
            double[] doubleArray = getDouble(vgp);
            int[] intArray = new int[doubleArray.length];
            for (int i = 0; i < doubleArray.length; ++i) {
                intArray[i] = (int) doubleArray[i];
            }
            return intArray;
        }

        private boolean haveParameters() {
            if (_mu.get.objects.parameter(BLOCK_C1, false) == null) {
                _mu.io.say.loud("Please define Block dimensions first and try again.");
                return false;
            }
            return true;
        }

        private void initializeTask() {
            _sim = getActiveSimulation();
            _mu.setSimulation(_sim, false);
            _ud = _mu.userDeclarations;
        }

    }

    //--
    //-- Variables declaration area.
    //--
    private final MacroUtils _mu;
    private Simulation _sim;
    private UserDeclarations _ud;
    private static final String ANNOT_NAME = "is2D";
    private static final String BLOCK_C1 = "Block Coordinate1";
    private static final String BLOCK_C2 = "Block Coordinate2";
    private static final String BLOCK_NCELLS = "Block Number of Cells";

}
