package macroutils.checker;

import java.io.File;
import java.util.ArrayList;
import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import star.base.neo.NamedObject;
import star.base.report.Monitor;
import star.base.report.ReportMonitor;
import star.cadmodeler.SolidModelPart;
import star.common.Boundary;
import star.common.CoordinateSystem;
import star.common.CylindricalCoordinateSystem;
import star.common.FieldFunction;
import star.common.FieldFunctionTypeOption;
import star.common.FluidRegion;
import star.common.GeometryPart;
import star.common.ModelManager;
import star.common.PartCurve;
import star.common.PartGroupDataSet;
import star.common.PartSurface;
import star.common.PhysicsContinuum;
import star.common.PlottingMode;
import star.common.Region;
import star.common.ResidualMonitor;
import star.common.ResidualPlot;
import star.common.Simulation;
import star.common.SolidRegion;
import star.common.StarPlot;
import star.common.Units;
import star.common.WallBoundary;
import star.meshing.AutoMeshOperation;
import star.meshing.AutoMeshOperation2d;
import star.meshing.CadPart;
import star.meshing.MeshOperation;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;
import star.vis.Displayer;
import star.vis.ScalarDisplayer;
import star.vis.Scene;
import star.vis.StreamDisplayer;
import star.vis.VectorDisplayer;

/**
 * Low-level class for is-type methods in MacroUtils.
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class CheckIs {

    private macroutils.io.MainIO _io = null;
    private MacroUtils _mu = null;
    private Simulation _sim = null;
    private macroutils.UserDeclarations _ud = null;

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public CheckIs(MacroUtils m) {
        _mu = m;
        _sim = m.getSimulation();
    }

    /**
     * Is this a Detached Eddy Simulation Physics Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean DES(PhysicsContinuum pc) {
        return pc.getModelManager().has("Detached Eddy Simulation");
    }

    /**
     * Is this a Large Eddy Simulation Physics Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean LES(PhysicsContinuum pc) {
        return pc.getModelManager().has("Large Eddy Simulation");
    }

    /**
     * Is this an Automated Mesh Operation?
     *
     * @param mo given Mesh Operation.
     * @return True or False.
     */
    public boolean autoMeshOperation(MeshOperation mo) {
        return mo instanceof AutoMeshOperation;
    }

    /**
     * Is this an Automated Mesh Operation (2D)?
     *
     * @param mo given Mesh Operation.
     * @return True or False.
     */
    public boolean autoMeshOperation2D(MeshOperation mo) {
        return mo instanceof AutoMeshOperation2d;
    }

    /**
     * Is this Geometry Part a CAD Part?
     *
     * @param gp given Geometry Part.
     * @return True or False.
     */
    public boolean cadPart(GeometryPart gp) {
        return gp instanceof CadPart;
    }

    /**
     * Is this a Coupled Solver Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean coupled(PhysicsContinuum pc) {
        ModelManager mm = pc.getModelManager();
        return mm.has("Coupled Solid Energy") || mm.has("Coupled Flow");
    }

    /**
     * Is this a Cylindrical Coordinate System?
     *
     * @param c given CoordinateSystem.
     * @return True or False.
     */
    public boolean cylindricalCSYS(CoordinateSystem c) {
        return c instanceof CylindricalCoordinateSystem;
    }

    /**
     * Gets whether the absolute difference between 2 arguments is within a tolerance.
     *
     * @param d1  given double.
     * @param d2  given double.
     * @param tol given tolerance.
     * @return True or False.
     */
    public boolean differenceWithinTolerance(double d1, double d2, double tol) {
        if (Math.abs(d1 - d2) <= tol) {
            return true;
        }
        return false;
    }

    /**
     * Are the Source and Target Par Surfaces Directed Meshable. See Directed Meshing in User Guide
     * for more information.
     *
     * @param src given Source Part Surface.
     * @param tgt given Target Part Surface.
     * @return True or False.
     */
    public boolean directedMeshable(PartSurface src, PartSurface tgt) {
        boolean b = true;
        if (src.getPart() != tgt.getPart()) {
            _io.say.msg("Source and Target Part Surfaces must be on the same Part. Skipping...",
                    true);
            b = false;
        }
        if (!(cadPart(src.getPart()) || solidModelPart(src.getPart()))) {
            _io.say.msg("Limited to CAD Parts only. Skipping...", true);
            b = false;
        }
        if (!b) {
            _io.say.object(src.getPart(), true);
            _io.say.object(tgt.getPart(), true);
            _io.say.msg("Not Directed Meshable.", true);
        }
        return b;
    }

    /**
     * Is this an Explicit Unsteady simulation?
     *
     * @return True or False.
     */
    public boolean explicitUnsteady() {
        return _sim.getSolverManager().has("Explicit Unsteady");
    }

    /**
     * Is this a Fluid Region?
     *
     * @param r given Region.
     * @return True or False.
     */
    public boolean fluid(Region r) {
        return r.getRegionType() instanceof FluidRegion;
    }

    /**
     * Is this a Histogram DataSet?
     *
     * @param pgds given DataSet.
     * @return True or False.
     */
    public boolean histogram(PartGroupDataSet pgds) {
        return pgds.getPlottingMode().equals(PlottingMode.HISTOGRAM);
    }

    /**
     * Is this a Histogram Plot?
     *
     * @param sp given Plot.
     * @return True or False.
     */
    public boolean histogram(StarPlot sp) {
        return sp.getDataSetCollection().stream()
                .filter(PartGroupDataSet.class::isInstance)
                .map(PartGroupDataSet.class::cast)
                .filter(ds -> histogram(ds))
                .findAny()
                .isPresent();
    }

    /**
     * Is this an Ideal Gas Physics Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean idealGas(PhysicsContinuum pc) {
        return pc.getModelManager().has("Ideal Gas");
    }

    /**
     * Is this an Implicit Unsteady simulation?
     *
     * @return True or False.
     */
    public boolean implicitUnsteady() {
        return _sim.getSolverManager().has("Implicit Unsteady");
    }

    /**
     * Is the Scene open and currently selected in the GUI?
     *
     * @param scn given Scene.
     * @return True or False.
     */
    public boolean open(Scene scn) {
        return scn.isShowing();
    }

    /**
     * Is this a Position Function?
     *
     * @param ff given Field Function.
     * @return True or False.
     */
    public boolean position(FieldFunction ff) {
        return ff.getTypeOption().getSelectedElement()
                .equals(FieldFunctionTypeOption.Type.POSITION);
    }

    /**
     * Is this a Report Monitor?
     *
     * @param m given Monitor.
     * @return True or False.
     */
    public boolean report(Monitor m) {
        return m instanceof ReportMonitor;
    }

    /**
     * Is this a Residual Monitor?
     *
     * @param m given Monitor.
     * @return True or False.
     */
    public boolean residual(Monitor m) {
        return m instanceof ResidualMonitor;
    }

    /**
     * Is this a Residual Plot?
     *
     * @param sp given Plot.
     * @return True or False.
     */
    public boolean residual(StarPlot sp) {
        return sp instanceof ResidualPlot;
    }

    /**
     * Is this a Scalar Displayer?
     *
     * @param d given Displayer.
     * @return True or False.
     */
    public boolean scalar(Displayer d) {
        return d instanceof ScalarDisplayer;
    }

    /**
     * Is this a Segregated Solver Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean segregated(PhysicsContinuum pc) {
        ModelManager mm = pc.getModelManager();
        return mm.has("Segregated Solid Energy") || mm.has("Segregated Flow");
    }

    /**
     * Is this filename a simulation file in the current {@link UserDeclarations#simPath}?
     *
     * @param filename given file name. Extension is optional.
     * @return True or False.
     */
    public boolean simFile(String filename) {
        File f = new File(_ud.simPath, filename.replace(".sim", "") + ".sim");
        return f.exists();
    }

    /**
     * Is this a Solid Physics Continua?
     *
     * @param pc given PhysicsContinuum.
     * @return True or False.
     */
    public boolean solid(PhysicsContinuum pc) {
        return pc.getModelManager().has("Solid");
    }

    /**
     * Is this a Solid Region?
     *
     * @param r given Region.
     * @return True or False.
     */
    public boolean solid(Region r) {
        return r.getRegionType() instanceof SolidRegion;
    }

    /**
     * Is this Geometry Part a Solid Model Part?
     *
     * @param gp given Geometry Part.
     * @return True or False.
     */
    public boolean solidModelPart(GeometryPart gp) {
        return gp instanceof SolidModelPart;
    }

    /**
     * Is this a Streamline Displayer?
     *
     * @param d given Displayer.
     * @return True or False.
     */
    public boolean streamline(Displayer d) {
        return d instanceof StreamDisplayer;
    }

    /**
     * Is this a Surface Wrap Mesh Operation?
     *
     * @param mo given Mesh Operation.
     * @return True or False.
     */
    public boolean surfaceWrapperOperation(MeshOperation mo) {
        return mo instanceof SurfaceWrapperAutoMeshOperation;
    }

    /**
     * Is this an Unit?
     *
     * @param no given STAR-CCM+ NamedObject.
     * @return True or False.
     */
    public boolean unit(NamedObject no) {
        return (no instanceof Units);
    }

    /**
     * Is this an Unsteady simulation?
     *
     * @return True or False.
     */
    public boolean unsteady() {
        return implicitUnsteady() || explicitUnsteady();
    }

    /**
     * This method is called automatically by {@link MacroUtils}.
     */
    public void updateInstances() {
        _io = _mu.io;
        _ud = _mu.userDeclarations;
    }

    /**
     * Is this a Vector Displayer?
     *
     * @param d given Displayer.
     * @return True or False.
     */
    public boolean vector(Displayer d) {
        return d instanceof VectorDisplayer;
    }

    /**
     * Is this a Vector Field Function?
     *
     * @param ff given Field Function.
     * @return True or False.
     */
    public boolean vector(FieldFunction ff) {
        return ff.getTypeOption().getSelectedElement().equals(FieldFunctionTypeOption.Type.VECTOR);
    }

    /**
     * Is this a Wall Boundary?
     *
     * @param b given Boundary.
     * @return True or False.
     */
    public boolean wall(Boundary b) {
        return b.getBoundaryType() instanceof WallBoundary;
    }

    /**
     * Is this a Windows machine?
     *
     * @return True or False.
     */
    public boolean windows() {
        return StaticDeclarations.OS.contains("win");
    }

    /**
     * Do these objects belong the same Geometry Part?
     *
     * @param ano given ArrayList of Named Objects. It can be Part Surfaces or Curves.
     * @return True or False.
     */
    public boolean withinSamePart(ArrayList<NamedObject> ano) {
        ArrayList<GeometryPart> agp = new ArrayList<>();
        for (NamedObject no : ano) {
            GeometryPart gp;
            if (no instanceof PartSurface) {
                gp = ((PartSurface) no).getPart();
            } else if (no instanceof PartCurve) {
                gp = ((PartCurve) no).getPart();
            } else {
                continue;
            }
            if (agp.contains(gp)) {
                return false;
            }
            agp.add(gp);
        }
        return true;
    }

}
