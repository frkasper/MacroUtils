package macroutils.templates.simassistants;

import macroutils.MacroUtils;
import star.base.neo.DoubleVector;
import star.common.Region;
import star.common.Units;
import star.common.VectorGlobalParameter;
import star.vis.Annotation;
import star.vis.Scene;

/**
 * A simple and efficient hexa mesher in a block.
 *
 * Use the Parameters under Tools node to control settings.
 *
 * @since June of 2018
 * @author Fabio Kasper
 */
public final class BlockMesher extends SimpleHexaMesher {

    private static final String ANNOT_NAME = "is2D";
    private static final String BLOCK_C1 = "Block Coordinate1";
    private static final String BLOCK_C2 = "Block Coordinate2";
    private static final String BLOCK_NCELLS = "Block Number of Cells";

    public BlockMesher(MacroUtils m) {
        super(m);
    }

    /**
     * Generate the mesh using current parameters.
     */
    public void generateMesh() {
        _mu.templates.mesh.setBadgeFor2D(is2D());

        VectorGlobalParameter c1 = getParameter(BLOCK_C1);
        VectorGlobalParameter c2 = getParameter(BLOCK_C2);
        VectorGlobalParameter nc = getParameter(BLOCK_NCELLS);

        Region r = _mesher.hexaBlock(getDouble(c1), getDouble(c2),
                c1.getQuantity().getUnits(), getInt(nc), "Block");

        if (is2D()) {
            _ud.namedObjects.add(r);
            removeAnnotation();
            _mesher.setBadgeFor2D(false);
        } else {
            _ud.namedObjects.addAll(_get.boundaries.all(r, false));
        }

        Scene scn = _add.scene.mesh(_ud.namedObjects);
        scn.open();
        if (!is2D()) {
            scn.setViewOrientation(new DoubleVector(new double[]{ -1.0, 1.0, -1.0 }),
                    new DoubleVector(new double[]{ 0.0, 1.0, 0.0 }));
        }
        scn.resetCamera();

        _ud.namedObjects.clear();
    }

    /**
     * The block first coordinate Parameter.
     *
     * @return A VectorGlobalParameter
     */
    public VectorGlobalParameter getBlockCoordinate1Parameter() {
        return getParameter(BLOCK_C1);
    }

    /**
     * The block second coordinate Parameter.
     *
     * @return A VectorGlobalParameter
     */
    public VectorGlobalParameter getBlockCoordinate2Parameter() {
        return getParameter(BLOCK_C2);
    }

    /**
     * The block number of i x j x k cells Parameter.
     *
     * @return A VectorGlobalParameter
     */
    public VectorGlobalParameter getBlockNumberOfCellsParameter() {
        return getParameter(BLOCK_NCELLS);
    }

    /**
     * Are the parameters in place?
     *
     * @return A boolean
     */
    public boolean haveParameters() {
        return getBlockCoordinate1Parameter() != null;
    }

    /**
     * Set the block to be two-dimensional.
     */
    public void setAs2D() {
        _add.tools.annotation_Simple(ANNOT_NAME, "");
        _io.say.action("Geometry will be flagged as 2D.", true);
    }

    /**
     * Set block mesh parameters.
     *
     * @param coord1 given array of coordinates
     * @param coord2 given array of coordinates
     * @param nCells given number of i x j x k hexahedral cells
     * @param u      given Units
     */
    public void setParameters(double[] coord1, double[] coord2, double[] nCells, Units u) {
        _add.tools.parameter_Vector(BLOCK_C1, coord1, u);
        _add.tools.parameter_Vector(BLOCK_C2, coord2, u);
        _add.tools.parameter_Vector(BLOCK_NCELLS, nCells, u);
    }

    private Annotation getAnnotation() {
        return _get.objects.annotation(ANNOT_NAME, false);
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

    private VectorGlobalParameter getParameter(String name) {
        return (VectorGlobalParameter) _get.objects.parameter(name, false);
    }

    private boolean is2D() {
        return getAnnotation() != null;
    }

    private void removeAnnotation() {
        if (getAnnotation() != null) {
            _sim.getAnnotationManager().remove(getAnnotation());
        }
    }

}
