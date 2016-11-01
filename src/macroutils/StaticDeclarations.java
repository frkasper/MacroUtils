package macroutils;

import java.awt.*;
import star.base.neo.*;
import star.common.*;
import star.meshing.*;
import star.trimmer.*;

/**
 * Low-level class for storing static declarations (immutable) to be used with MacroUtils, such as:
 * <ul>
 * <li>{@link Axis}</li>
 * <li>{@link BoundaryMode}</li>
 * <li>{@link Colormaps}</li>
 * <li>{@link Colors}</li>
 * <li>{@link DefaultURFs}</li>
 * <li>{@link Density}</li>
 * <li>{@link Energy}</li>
 * <li>{@link FeatureCurveMode}</li>
 * <li>{@link Fonts}</li>
 * <li>{@link GrowthRate}</li>
 * <li>{@link InterfaceMode}</li>
 * <li>{@link Logic}</li>
 * <li>{@link Material}</li>
 * <li>{@link Meshers}</li>
 * <li>{@link Operation}</li>
 * <li>{@link Operator}</li>
 * <li>{@link RegionMode}</li>
 * <li>{@link Solver}</li>
 * <li>{@link Solvers}</li>
 * <li>{@link Space}</li>
 * <li>{@link StopCriteria}</li>
 * <li>{@link Tessellation}</li>
 * <li>{@link Time}</li>
 * <li>{@link Vars}</li>
 * <li>{@link Viscous}</li>
 * </ul>
 *
 * @since February of 2016
 * @author Fabio Kasper
 */
public class StaticDeclarations {

    //--
    //-- double's
    //--
    /**
     * A very high positive number.
     */
    public static final double BIG_NUMBER = 1E20;

    /**
     * A very small positive number.
     */
    public static final double SMALL_NUMBER = 1E-20;

    //--
    //-- double[]'s
    //--
    /**
     * Origin coordinates (0, 0, 0).
     */
    public static final double[] COORD0 = {0., 0., 0.};

    //--
    //-- enums
    //--
    /**
     * Common axis options w.r.t Direction:
     * <ul>
     * <li>{@link #X}</li>
     * <li>{@link #Y}</li>
     * <li>{@link #Z}</li>
     * </ul>
     */
    public static enum Axis {

        /**
         * X axis direction.
         */
        X,
        /**
         * Y axis direction.
         */
        Y,
        /**
         * Z axis direction.
         */
        Z,

    }

    /**
     * Special MacroUtils types when creating Regions w.r.t Boundaries:
     * <ul>
     * <li>{@link #ONE_FOR_EACH_PART_SURFACE} (Preferred)</li>
     * <li>{@link #ONE_FOR_ALL_PARTS}</li>
     * <li>{@link #ONE_FOR_EACH_PART}</li>
     * </ul>
     */
    public static enum BoundaryMode {

        /**
         * One Boundary for every Part Surface provided (Preferred).
         */
        ONE_FOR_EACH_PART_SURFACE("OneBoundaryPerPartSurface"),
        /**
         * One Boundary for every Geometry Part provided.
         */
        ONE_FOR_ALL_PARTS("OneBoundary"),
        /**
         * One Boundary for every Geometry Part provided.
         */
        ONE_FOR_EACH_PART("OneBoundaryPerPart"),;

        private final String s;

        private BoundaryMode(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual string expected by STAR-CCM+ API.
         *
         * @return The mode.
         */
        public String getMode() {
            return this.s;
        }

    }

    /**
     * Some standard colormap names shipped with STAR-CCM+, such as::
     * <ul>
     * <li>{@link #BLUE_RED}</li>
     * <li>{@link #BLUE_RED_BALANCED}</li>
     * <li>{@link #BLUE_RED_BRIGHT}</li>
     * <li>{@link #BLUE_RED_HIGHPASS}</li>
     * <li>{@link #BLUE_RED_LOWPASS}</li>
     * </ul>
     */
    public static enum Colormaps {

        /**
         * Regular blue-red colormap.
         */
        BLUE_RED("blue-red"),
        /**
         * Blue-red balanced colormap.
         */
        BLUE_RED_BALANCED("blue-red balanced"),
        /**
         * Blue-red bright colormap.
         */
        BLUE_RED_BRIGHT("blue-red bright"),
        /**
         * Blue-red high-pass colormap.
         */
        BLUE_RED_HIGHPASS("blue-red-highpass"),
        /**
         * Blue-red low-pass colormap.
         */
        BLUE_RED_LOWPASS("blue-red-lowpass"),
        /**
         * Kelvin Temperature colormap.
         */
        KELVIN_TEMPERATURE("Kelvin temperature"),
        /**
         * Land elevation colormap.
         */
        LAND_ELEVATION("land elevation"),
        /**
         * Land sea elevation colormap.
         */
        LAND_SEA_ELEVATION("land sea elevation"),
        /**
         * Orchid green colormap.
         */
        ORCHID_GREEN("orchid-green"),;

        private final String s;

        private Colormaps(final String s) {
            this.s = s;
        }

        /**
         * Gets the colormap object.
         *
         * @return The LookUpTable.
         */
        public String getName() {
            return this.s;
        }

    }

    /**
     * Some color definitions available in MacroUtils, based on {@link java.awt.Color} class, such as:
     * <ul>
     * <li>{@link #BLACK}</li>
     * <li>{@link #BLUE}</li>
     * <li>{@link #BLUE_MEDIUM}</li>
     * <li>{@link #DARK_GREEN}</li>
     * <li>{@link #DARK_ORANGE}</li>
     * <li>{@link #DIM_GRAY}</li>
     * <li>{@link #GAINSBORO}</li>
     * <li>{@link #IVORY}</li>
     * <li>{@link #IVORY_BLACK}</li>
     * <li>{@link #LIGHT_GRAY}</li>
     * <li>{@link #LIME_GREEN}</li>
     * <li>{@link #MINT}</li>
     * <li>{@link #NAVY}</li>
     * <li>{@link #PURPLE}</li>
     * <li>{@link #SLATE_GRAY}</li>
     * <li>{@link #SLATE_GRAY_DARK}</li>
     * <li>{@link #ULTRAMARINE}</li>
     * <li>{@link #WHEAT}</li>
     * <li>{@link #WHITE}</li>
     * <li>{@link #WHITE_SMOKE}</li>
     * </ul>
     */
    public static enum Colors {

        BLACK(Color.BLACK),
        BLUE(Color.BLUE),
        BLUE_MEDIUM(new Color(0, 0, 205)),
        DARK_GREEN(new Color(0, 100, 0)),
        DARK_ORANGE(new Color(255, 140, 0)),
        DIM_GRAY(new Color(105, 105, 105)),
        GAINSBORO(new Color(220, 220, 220)),
        IVORY(new Color(255, 255, 240)),
        IVORY_BLACK(new Color(41, 36, 33)),
        LIGHT_GRAY(Color.LIGHT_GRAY),
        LIME_GREEN(new Color(50, 205, 50)),
        MINT(new Color(189, 252, 201)),
        NAVY(new Color(0, 0, 128)),
        PURPLE(new Color(160, 32, 240)),
        SLATE_GRAY(new Color(112, 128, 144)),
        SLATE_GRAY_DARK(new Color(47, 79, 79)),
        ULTRAMARINE(new Color(18, 10, 143)),
        WHEAT(new Color(245, 222, 179)),
        WHITE(Color.WHITE),
        WHITE_SMOKE(new Color(245, 245, 245)),;

        private final Color c;

        private Colors(final Color c) {
            this.c = c;
        }

        /**
         * Gets the color object.
         *
         * @return The {@link java.awt.Color} object.
         */
        public Color getColor() {
            return this.c;
        }

    }

    /**
     * The color space code when creating Colormaps, such as:
     * <ul>
     * <li>{@link #RGB}</li>
     * <li>{@link #HSV}</li>
     * <li>{@link #LAB}</li>
     * <li>{@link #DIVERGING}</li>
     * <li>{@link #HSV_WRAPPED}</li>
     * </ul>
     */
    public static enum ColorSpace {

        /**
         * RGB color space when creating Colormaps.
         */
        RGB(0),
        /**
         * HSV color space when creating Colormaps.
         */
        HSV(1),
        /**
         * Lab color space when creating Colormaps.
         */
        LAB(2),
        /**
         * Diverging color space when creating Colormaps.
         */
        DIVERGING(3),
        /**
         * HSV-Wrapped color space when creating Colormaps.
         */
        HSV_WRAPPED(4),;

        private final int val;

        private ColorSpace(final int val) {
            this.val = val;
        }

        /**
         * Gets the actual value.
         *
         * @return The integer value.
         */
        public int getValue() {
            return this.val;
        }

    }

    /**
     * Default Under Relaxation Factors used by STAR-CCM+. Some are available in MacroUtils, such as:
     * <ul>
     * <li>{@link #FLUID_ENERGY}</li>
     * <li>{@link #GRANULAR_TEMPERATURE}</li>
     * <li>{@link #K_EPSILON}</li>
     * <li>{@link #PHASE_COUPLED_VELOCITY}</li>
     * <li>{@link #PPDF_COMBUSTION}</li>
     * <li>{@link #PRESSURE}</li>
     * <li>{@link #REYNOLDS_STRESS_MODEL}</li>
     * <li>{@link #SOLID_ENERGY}</li>
     * <li>{@link #SPECIES}</li>
     * <li>{@link #VELOCITY}</li>
     * <li>{@link #VOLUME_FRACTION}</li>
     * <li>{@link #VOF}</li>
     * </ul>
     */
    public static enum DefaultURFs {

        /**
         * Default URF for Fluid Energy. Default = 0.9.
         */
        FLUID_ENERGY(0.9),
        /**
         * Default URF for Granular Temperature. Default = 0.6.
         */
        GRANULAR_TEMPERATURE(0.6),
        /**
         * Default URF for K-Omega models. Default = 0.8.
         */
        K_EPSILON(0.8),
        /**
         * Default URF for K-Epsilon models. Default = 0.8.
         */
        K_OMEGA(0.8),
        /**
         * Default URF for Phase Coupled Velocity. Default = 0.7.
         */
        PHASE_COUPLED_VELOCITY(0.7),
        /**
         * Default URF for PPDF Combustion. Default = 0.8.
         */
        PPDF_COMBUSTION(0.8),
        /**
         * Default URF for Pressure. Default = 0.3.
         */
        PRESSURE(0.3),
        /**
         * Default URF for the Reynolds Stress Model. Default = 0.6.
         */
        REYNOLDS_STRESS_MODEL(0.6),
        /**
         * Default URF for Solid Energy. Default = 0.99.
         */
        SOLID_ENERGY(0.99),
        /**
         * Default URF for Species. Default = 0.9.
         */
        SPECIES(0.9),
        /**
         * Default URF for Velocity. Default = 0.7.
         */
        VELOCITY(0.7),
        /**
         * Default URF for Volume Fraction.
         */
        VOLUME_FRACTION(0.5),
        /**
         * Default URF for Volume of Fluid (VOF). Default = 0.9.
         */
        VOF(0.9),;

        private final double val;

        private DefaultURFs(final double val) {
            this.val = val;
        }

        /**
         * Gets the actual value.
         *
         * @return The double value.
         */
        public double getValue() {
            return this.val;
        }

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Density/Equation Of State:
     * <ul>
     * <li>{@link #INCOMPRESSIBLE} or {@link #INCOMPRESSIBLE}</li>
     * <li>{@link #IDEAL_GAS}</li>
     * </ul>
     */
    public static enum Density {

        /**
         * Constant Density material.
         */
        CONSTANT,
        /**
         * Constant Density material.
         */
        INCOMPRESSIBLE,
        /**
         * Ideal Gas fluid.
         */
        IDEAL_GAS,

    }

    /**
     * Special MacroUtils types for Displayers:
     * <ul>
     * <li>{@link #GEOMETRY}</li>
     * <li>{@link #SCALAR}</li>
     * <li>{@link #STREAMLINE}</li>
     * <li>{@link #VECTOR}</li>
     * </ul>
     */
    public static enum Displayer {

        /**
         * A Geometry/Mesh Displayer.
         */
        GEOMETRY("Part"),
        /**
         * A Scalar Displayer.
         */
        SCALAR("Scalar"),
        /**
         * A Streamline Displayer.
         */
        STREAMLINE("Streamline"),
        /**
         * A Vector Displayer.
         */
        VECTOR("Vector"),;

        private final String s;

        private Displayer(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual variable name.
         *
         * @return The variable name.
         */
        public String getType() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Energy:
     * <ul>
     * <li>{@link #ISOTHERMAL}</li>
     * <li>{@link #THERMAL}</li>
     * </ul>
     */
    public static enum Energy {

        /**
         * Isothermal material.
         */
        ISOTHERMAL,
        /**
         * Material solving for Heat Transfer.
         */
        THERMAL,

    }

    /**
     * Special MacroUtils types when creating Regions w.r.t Feature Curves:
     * <ul>
     * <li>{@link #ONE_FOR_EACH_PART_CURVE} (Preferred)</li>
     * <li>{@link #ONE_FOR_ALL}</li>
     * </ul>
     */
    public static enum FeatureCurveMode {

        /**
         * One Feature Curve for every Part Curve available (Preferred).
         */
        ONE_FOR_EACH_PART_CURVE("OneFeatureCurvePerPartCurve"),
        /**
         * One Feature Curve all Part Curves available.
         */
        ONE_FOR_ALL("OneFeatureCurve"),;

        private final String s;

        private FeatureCurveMode(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual string expected by STAR-CCM+ API.
         *
         * @return The mode.
         */
        public String getMode() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils fonts related to different items, according to {@link java.awt.Font}.
     */
    public static enum Fonts {

        /**
         * Default Font used in STAR-CCM+.
         */
        DEFAULT(new Font("Lucida Sans Typewriter", Font.ITALIC, 24)),
        /**
         * Font used for Titles, when {@link macroutils.templates.TemplatePrettifier} is used.
         */
        TITLE(new Font("Lucida Sans Typewriter", Font.PLAIN, 20)),
        /**
         * Font used for everywhere else, when {@link macroutils.templates.TemplatePrettifier} is used.
         */
        OTHER(new Font("Lucida Sans Typewriter", Font.PLAIN, 16)),
        /**
         * Font used for Report Annotations, when {@link macroutils.templates.TemplatePrettifier} is used.
         */
        REPORT_ANNOTATIONS(new Font("Lucida Sans Typewriter", Font.PLAIN, 24)),
        /**
         * Font used for Text Annotations, when {@link macroutils.templates.TemplatePrettifier} is used.
         */
        SIMPLE_ANNOTATIONS(REPORT_ANNOTATIONS.getFont()),;

        private final Font f;

        private Fonts(final Font f) {
            this.f = f;
        }

        /**
         * Gets the actual Font.
         *
         * @return The {@link java.awt.Font}.
         */
        public Font getFont() {
            return this.f;
        }

    }

    /**
     * Special MacroUtils types for Global Parameters:
     * <ul>
     * <li>{@link #SCALAR}</li>
     * <li>{@link #VECTOR}</li>
     * </ul>
     */
    public static enum GlobalParameter {

        /**
         * A Scalar Parameter.
         */
        SCALAR("Scalar"),
        /**
         * A Vector Parameter.
         */
        VECTOR("Vector"),;

        private final String s;

        private GlobalParameter(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual variable name.
         *
         * @return The variable name.
         */
        public String getType() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils variable to select the Volume Growth Rate for the meshers:
     * <ul>
     * <li>{@link #FAST}</li>
     * <li>{@link #MEDIUM}</li>
     * <li>{@link #SLOW}</li>
     * <li>{@link #VERY_SLOW}</li>
     * </ul>
     */
    public static enum GrowthRate {

        /**
         * Fast Volume Growth Rate.
         */
        FAST(PartsGrowthRateOption.Type.FAST),
        /**
         * Medium Volume Growth Rate.
         */
        MEDIUM(PartsGrowthRateOption.Type.MEDIUM),
        /**
         * Slow Volume Growth Rate.
         */
        SLOW(PartsGrowthRateOption.Type.SLOW),
        /**
         * Very Slow Volume Growth Rate.
         */
        VERY_SLOW(PartsGrowthRateOption.Type.VERYSLOW),;

        private final PartsGrowthRateOption.Type t;

        private GrowthRate(final PartsGrowthRateOption.Type t) {
            this.t = t;
        }

        /**
         * Gets the actual option used by STAR-CCM+.
         *
         * @return The PartsGrowthRateOption Type.
         */
        public PartsGrowthRateOption.Type getType() {
            return this.t;
        }

        /**
         * Gets the actual Integer value based on the option used by STAR-CCM+.
         *
         * @return The integer based on the TessellationDensityOption Type.
         */
        public int getValue() {
            return this.t.getValue();
        }

    }

    /**
     * Special MacroUtils types when creating Regions w.r.t to Interfaces:
     * <ul>
     * <li>{@link #CONTACT} (Preferred)</li>
     * <li>{@link #BOUNDARY}</li>
     * </ul>
     */
    public static enum InterfaceMode {

        /**
         * Contact Interface Mode. This is a newer mode available with the addition of Parts Based Meshing (Preferred).
         */
        CONTACT(RegionManager.CreateInterfaceMode.CONTACT),
        /**
         * Boundary Interface Mode. This is a legacy mode available in STAR-CCM+.
         */
        BOUNDARY(RegionManager.CreateInterfaceMode.BOUNDARY),;

        private final RegionManager.CreateInterfaceMode i;

        private InterfaceMode(final RegionManager.CreateInterfaceMode i) {
            this.i = i;
        }

        /**
         * Gets the actual {@link star.common.RegionManager.CreateInterfaceMode} expected by STAR-CCM+ API.
         *
         * @return The mode.
         */
        public RegionManager.CreateInterfaceMode getMode() {
            return this.i;
        }

    }

    /**
     * Special MacroUtils types w.r.t logic options:
     * <ul>
     * <li>{@link #AND}</li>
     * <li>{@link #OR}</li>
     * <li>{@link #XOR}</li>
     * </ul>
     */
    public static enum Logic {

        /**
         * AND logic option.
         */
        AND,
        /**
         * OR logic option.
         */
        OR,
        /**
         * XOR logic option.
         */
        XOR,

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Material/Media:
     * <ul>
     * <li>{@link #GAS}</li>
     * <li>{@link #LIQUID}</li>
     * <li>{@link #SOLID}</li>
     * <li>{@link #VOF}</li>
     * <li>{@link #VOF_AIR_WATER}</li>
     * </ul>
     */
    public static enum Material {

        /**
         * Single Component Gas.
         */
        GAS,
        /**
         * Single Component Liquid.
         */
        LIQUID,
        /**
         * Single Component Solid.
         */
        SOLID,
        /**
         * Volume of Fluid (VOF). Phases need to be created later.
         */
        VOF,
        /**
         * Volume of Fluid (VOF) with Air and Water.
         */
        VOF_AIR_WATER,

    }

    /**
     * The meshers available in STAR-CCM+, such as::
     * <ul>
     * <li>{@link #AUTOMATIC_SURFACE_REPAIR}</li>
     * <li>{@link #POLY_MESHER}</li>
     * <li>{@link #POLY_MESHER_2D}</li>
     * <li>{@link #PRISM_LAYER_MESHER}</li>
     * <li>{@link #SURFACE_REMESHER}</li>
     * <li>{@link #THIN_LAYER_MESHER}</li>
     * <li>{@link #TRIMMER_MESHER}</li>
     * </ul>
     */
    public static enum Meshers {

        /**
         * Automatic Surface Repair for the Remesher. Useful when Surface Wrapping.
         */
        AUTOMATIC_SURFACE_REPAIR("star.resurfacer.AutomaticSurfaceRepairAutoMesher"),
        /**
         * Polyhedral Mesher.
         */
        POLY_MESHER("star.dualmesher.DualAutoMesher"),
        /**
         * The 2D Polygonal Mesher.
         */
        POLY_MESHER_2D("star.twodmesher.DualAutoMesher2d"),
        /**
         * Prism Layer Mesher.
         */
        PRISM_LAYER_MESHER("star.prismmesher.PrismAutoMesher"),
        /**
         * Surface Remesher.
         */
        SURFACE_REMESHER("star.resurfacer.ResurfacerAutoMesher"),
        /**
         * Thin Layer Mesher. Useful when meshing Solids.
         */
        THIN_LAYER_MESHER("star.solidmesher.ThinAutoMesher"),
        /**
         * Trimmer Mesher.
         */
        TRIMMER_MESHER("star.trimmer.TrimmerAutoMesher"),;

        private final String s;

        private Meshers(final String s) {
            this.s = s;
        }

        /**
         * Gets the mesher as string.
         *
         * @return The string.
         */
        public String getMesher() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types w.r.t Operations:
     * <ul>
     * <li>{@link #SUBTRACT}</li>
     * <li>{@link #UNITE}</li>
     * </ul>
     */
    public static enum Operation {

        SUBTRACT,
        UNITE,

    }

    /**
     * Special MacroUtils types w.r.t operator:
     * <ul>
     * <li>{@link #LESS_THAN_OR_EQUALS}</li>
     * <li>{@link #GREATER_THAN_OR_EQUALS}</li>
     * </ul>
     */
    public static enum Operator {

        /**
         * &lt;= logic option.
         */
        LESS_THAN_OR_EQUALS,
        /**
         * &gt;= logic option.
         */
        GREATER_THAN_OR_EQUALS,

    }

    /**
     * Special MacroUtils types when creating Regions:
     * <ul>
     * <li>{@link #ONE_PER_PART} (Preferred)</li>
     * <li>{@link #ONE}</li>
     * </ul>
     */
    public static enum RegionMode {

        /**
         * One Region for each Part provided (Preferred).
         */
        ONE_PER_PART("OneRegionPerPart"),
        /**
         * One Region for all Parts.
         */
        ONE("OneRegion"),;

        private final String s;

        private RegionMode(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual string expected by STAR-CCM+ API.
         *
         * @return The mode.
         */
        public String getMode() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types for Scenes:
     * <ul>
     * <li>{@link #EMPTY}</li>
     * <li>{@link #GEOMETRY}</li>
     * <li>{@link #MESH}</li>
     * <li>{@link #SCALAR}</li>
     * <li>{@link #STREAMLINE}</li>
     * <li>{@link #VECTOR}</li>
     * </ul>
     */
    public static enum Scene {

        /**
         * An empty Scene.
         */
        EMPTY("Empty"),
        /**
         * A Geometry Scene.
         */
        GEOMETRY("Geometry"),
        /**
         * A Mesh Scene.
         */
        MESH("Mesh"),
        /**
         * A Scalar Scene.
         */
        SCALAR("Scalar"),
        /**
         * A Streamline Scene.
         */
        STREAMLINE("Streamline"),
        /**
         * A Vector Scene.
         */
        VECTOR("Vector"),;

        private final String s;

        private Scene(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual variable name.
         *
         * @return The variable name.
         */
        public String getType() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types when clearing the Solution:
     * <ul>
     * <li>{@link #ADJOINT_FLOW}</li>
     * <li>{@link #FIELDS}</li>
     * <li>{@link #HISTORY}</li>
     * <li>{@link #LAGRANGIAN_DEM}</li>
     * <li>{@link #MESH}</li>
     * </ul>
     */
    public static enum SolutionClear {

        /**
         * Adjoint Flow results.
         */
        ADJOINT_FLOW(Solution.Clear.AdjointFlow),
        /**
         * All fields results.
         */
        FIELDS(Solution.Clear.Fields),
        /**
         * Solution History.
         */
        HISTORY(Solution.Clear.History),
        /**
         * Lagrangian and/or DEM results.
         */
        LAGRANGIAN_DEM(Solution.Clear.LagrangianDem),
        /**
         * Reset Mesh.
         */
        MESH(Solution.Clear.Mesh),;

        private final Solution.Clear c;

        private SolutionClear(final Solution.Clear c) {
            this.c = c;
        }

        /**
         * Gets the actual {@link star.common.Solution.Clear} value expected by STAR-CCM+ API.
         *
         * @return The mode.
         */
        public Solution.Clear getClear() {
            return this.c;
        }

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Solver:
     * <ul>
     * <li>{@link #SEGREGATED}</li>
     * <li>{@link #COUPLED}</li>
     * </ul>
     */
    public static enum Solver {

        /**
         * Segregated Solver.
         */
        SEGREGATED,
        /**
         * Coupled Solver.
         */
        COUPLED,

    }

    /**
     * Special MacroUtils types when working with STAR-CCM+ Solvers, e.g. freezing, such as:
     * <ul>
     * <li>{@link #ALL}</li>
     * <li>{@link #ALL_BUT_STRATEGIC}</li>
     * </ul>
     */
    public static enum Solvers {

        /**
         * All solvers.
         */
        ALL,
        /**
         * All solvers except strategic ones, such as Time, Motion, Partitioning and CoSim solvers.
         */
        ALL_BUT_STRATEGIC,

//   * <li>{@link #FLOW}</li>
//   * <li>{@link #ENERGY}</li>
//   * <li>{@link #TURBULENCE}</li>
//    /** Flow solvers. */
//    FLOW,
//
//    /** Energy solver. */
//    ENERGY,
//
//    /** Turbulence solver. */
//    TURBULENCE,
    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Space:
     * <ul>
     * <li>{@link #THREE_DIMENSIONAL}</li>
     * <li>{@link #TWO_DIMENSIONAL}</li>
     * </ul>
     */
    public static enum Space {

        /**
         * 3D case.
         */
        THREE_DIMENSIONAL,
        /**
         * 2D case.
         */
        TWO_DIMENSIONAL,

    }

    /**
     * Special MacroUtils types for Stopping Criteria:
     * <ul>
     * <li>{@link #ASYMPTOTIC}</li>
     * <li>{@link #MAX}</li>
     * <li>{@link #MIN}</li>
     * <li>{@link #MIN_INNER}</li>
     * <li>{@link #STDEV}</li>
     * </ul>
     */
    public static enum StopCriteria {

        /**
         * Based on asymptotic fluctuation of samples.
         */
        ASYMPTOTIC("Asymptotic limit"),
        /**
         * Based on a maximum value.
         */
        MAX("Maximum value"),
        /**
         * Based on a minimum value.
         */
        MIN("Minimum value"),
        /**
         * Based on a minimum number of inner iterations in an unsteady simulation.
         */
        MIN_INNER("Minimum Inner Iterations"),
        /**
         * Based on a standard deviation of the samples.
         */
        STDEV("Standard Deviation"),;

        private final String s;

        private StopCriteria(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual variable name.
         *
         * @return The variable name.
         */
        public String getVar() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types for Tessellation when working with Parts. Based on {@link TessellationDensityOption}:
     * <ul>
     * <li>{@link #COARSE}</li>
     * <li>{@link #FINE}</li>
     * <li>{@link #MEDIUM}</li>
     * <li>{@link #VERY_COARSE}</li>
     * <li>{@link #VERY_FINE}</li>
     * <li>{@link #DISTANCE_BIASED}</li>
     * </ul>
     */
    public static enum Tessellation {

        /**
         * Coarse Tessellation.
         */
        COARSE(TessellationDensityOption.Type.COARSE),
        /**
         * Fine Tessellation.
         */
        FINE(TessellationDensityOption.Type.FINE),
        /**
         * Medium Tessellation.
         */
        MEDIUM(TessellationDensityOption.Type.MEDIUM),
        /**
         * Very Coarse Tessellation.
         */
        VERY_COARSE(TessellationDensityOption.Type.VERY_COARSE),
        /**
         * Very Fine Tessellation.
         */
        VERY_FINE(TessellationDensityOption.Type.VERY_FINE),
        /**
         * Distance Biased Tessellation.
         */
        DISTANCE_BIASED(TessellationDensityOption.Type.DISTANCE_BIASED),;

        private final TessellationDensityOption.Type t;

        private Tessellation(final TessellationDensityOption.Type t) {
            this.t = t;
        }

        /**
         * Gets the actual option used by STAR-CCM+.
         *
         * @return The TessellationDensityOption Type.
         */
        public TessellationDensityOption.Type getType() {
            return this.t;
        }

        /**
         * Gets the actual Integer value based on the option used by STAR-CCM+.
         *
         * @return The integer based on the TessellationDensityOption Type.
         */
        public int getValue() {
            return this.t.getValue();
        }

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Time:
     * <ul>
     * <li>{@link #STEADY}</li>
     * <li>{@link #IMPLICIT_UNSTEADY}</li>
     * <li>{@link #EXPLICIT_UNSTEADY}</li>
     * </ul>
     */
    public static enum Time {

        /**
         * Steady state conditions.
         */
        STEADY,
        /**
         * Implicit unsteady.
         */
        IMPLICIT_UNSTEADY,
        /**
         * Explicit unsteady.
         */
        EXPLICIT_UNSTEADY,

    }

    /**
     * Common STAR-CCM+ variables.
     */
    public static enum Vars {

        /**
         * <u>Ambient Temperature</u> variable name inside STAR-CCM+.
         */
        AMBIENT_T("Ambient Temperature"),
        /**
         * <u>Convective Courant Number</u> (CFL) variable name inside STAR-CCM+.
         */
        CFL("Convective Courant Number"),
        /**
         * <u>Specific Heat</u> variable name inside STAR-CCM+.
         */
        CP("Specific Heat"),
        /**
         * <u>Density</u> variable name inside STAR-CCM+.
         */
        DEN("Density"),
        /**
         * <u>Heat Transfer Coefficient</u> variable name inside STAR-CCM+.
         */
        HTC("Heat Transfer Coefficient"),
        /**
         * <u>Thermal Conductivity</u> variable name inside STAR-CCM+.
         */
        K("Thermal Conductivity"),
        /**
         * <u>Mach Number</u> variable name inside STAR-CCM+.
         */
        MACH("Mach Number"),
        /**
         * <u>Pressure variable</u> name inside STAR-CCM+.
         */
        P("Pressure"),
        /**
         * <u>Position variable</u> name inside STAR-CCM+.
         */
        POS("Position"),
        /**
         * <u>Turbulent Prandtl Number</u> variable name inside STAR-CCM+.
         */
        PRANDTL("Turbulent Prandtl Number"),
        /**
         * <u>Pressure Coefficient</u> variable name inside STAR-CCM+.
         */
        PC("Pressure Coefficient"),
        /**
         * <u>Static Pressure</u> variable name inside STAR-CCM+.
         */
        STATIC_P("Static Pressure"),
        /**
         * <u>Static Temperature</u> variable name inside STAR-CCM+.
         */
        STATIC_T("Static Temperature"),
        /**
         * <u>Temperature</u> variable name inside STAR-CCM+.
         */
        T("Temperature"),
        /**
         * <u>Total Temperature</u> variable name inside STAR-CCM+.
         */
        TI("Turbulence Intensity"),
        /**
         * <u>Total Temperature</u> variable name inside STAR-CCM+.
         */
        TOTAL_T("Total Temperature"),
        /**
         * <u>Turbulence Intensity</u> variable name inside STAR-CCM+. Note this is not a Field Function.
         */
        TVR("Turbulent Viscosity Ratio"),
        /**
         * <u>Turbulent Velocity Scale</u> variable name inside STAR-CCM+. Note this is not a Field Function.
         */
        TVS("Turbulent Velocity Scale"),
        /**
         * <u>Velocity</u> variable name inside STAR-CCM+.
         */
        VEL("Velocity"),
        /**
         * <u>Velocity Magnitude</u> variable name inside STAR-CCM+.
         */
        VEL_MAG("Velocity Magnitude"),
        /**
         * <u>Volume</u> variable name inside STAR-CCM+.
         */
        VOL("Volume"),
        /**
         * <u>Dynamic Viscosity</u> variable name inside STAR-CCM+.
         */
        VISC("Dynamic Viscosity"),
        /**
         * <u>Wall Y+</u> variable name inside STAR-CCM+.
         */
        YPLUS("WallYplus"),;

        private final String s;

        private Vars(final String s) {
            this.s = s;
        }

        /**
         * Gets the actual variable name.
         *
         * @return The variable name.
         */
        public String getVar() {
            return this.s;
        }

    }

    /**
     * Special MacroUtils types for creating a Physics Continua w.r.t Turbulence:
     * <ul>
     * <li>{@link #DES_SST_KW_DDES}</li>
     * <li>{@link #DES_SST_KW_IDDES}</li>
     * <li>{@link #INVISCID}</li>
     * <li>{@link #LAMINAR}</li>
     * <li>{@link #NOT_APPLICABLE}</li>
     * <li>{@link #KE_STD}</li>
     * <li>{@link #RKE_2LAYER}</li>
     * <li>{@link #RKE_HIGH_YPLUS}</li>
     * <li>{@link #KW_2008}</li>
     * <li>{@link #KW_STD}</li>
     * <li>{@link #SOLID}</li>
     * <li>{@link #SST_KW}</li>
     * </ul>
     */
    public static enum Viscous {

        /**
         * Inviscid regime.
         */
        INVISCID,
        /**
         * Laminar viscous regime (pseudo-DNS).
         */
        LAMINAR,
        /**
         * N/A viscous regime when solving for solids, for instance.
         */
        NOT_APPLICABLE,
        /**
         * RANS with Realizable K-Epsilon model and high y+ wall formulation.
         */
        KE_STD,
        /**
         * RANS with Realizable K-Epsilon model and all y+ wall formulation.
         */
        RKE_2LAYER,
        /**
         * RANS with Realizable K-Epsilon model and high y+ wall formulation.
         */
        RKE_HIGH_YPLUS,
        /**
         * RANS with Standard K-Omega model 2008 version and all y+ wall formulation.
         */
        KW_2008,
        /**
         * RANS with Standard K-Omega model and all y+ wall formulation.
         */
        KW_STD,
        /**
         * Viscous regime when solving for solids.
         */
        SOLID,
        /**
         * RANS with SST K-Omega model and all y+ wall formulation.
         */
        SST_KW,
        /**
         * Detached Eddy Simulation with SST K-Omega DDES formulation and all y+ wall model.
         */
        DES_SST_KW_DDES,
        /**
         * Detached Eddy Simulation with SST K-Omega IDDES formulation and all y+ wall model.
         */
        DES_SST_KW_IDDES,

    }

    //--
    //-- Objects in general.
    //--
    /**
     * An empty object array. Useful somewhere.
     */
    public static final Object[] EMPTY_OBJECT = new Object[]{};

    /**
     * An empty STAR-CCM+ NeoObjectVector. Useful somewhere.
     */
    public static final NeoObjectVector EMPTY_NEO_OBJECT_VECTOR = new NeoObjectVector(EMPTY_OBJECT);

  //--
    //-- Integers.
    //--
    //--
    //-- Strings.
    //--
    /**
     * The character used to split Camera Views.
     */
    public static final String CAM_SPLIT_CHAR_CAMS = ";";

    /**
     * The character used to split Camera Views.
     */
    public static final String CAM_SPLIT_CHAR_FIELDS = "|";

    private static final String CAM_VEC = "%.6e,%.6e,%.6e %.6e,%.6e,%.6e %.6e,%.6e,%.6e ";

    /**
     * The format for reading/writing Camera Views with MacroUtils.
     */
    public static final String CAM_FORMAT = ("%s " + CAM_VEC + "%.6e %d").replaceAll(" ", CAM_SPLIT_CHAR_FIELDS);

    /**
     * The prefix used by MacroUtils when printing something to the output/console.
     */
    public static final String MSG_PREFIX = "[*]";

    /**
     * The prefix used by MacroUtils when printing something to the output/console while in debug mode.
     */
    public static final String MSG_DEBUG_PREFIX = "[!]";

    /**
     * A string defined as "none".
     */
    public static final String NONE_STRING = "none";

    /**
     * The default picture type in MacroUtils is PNG.
     */
    public static final String PIC_EXT = "png";

    /**
     * Current Operating System.
     */
    public static final String OS = System.getProperty("os.name").toLowerCase();

    /**
     * The REGEX wildcard for getting everything ".*".
     */
    public static final String REGEX_ALL = ".*";

    /**
     * Temporary camera name used by MacroUtils.
     */
    public static final String TMP_CAM_NAME = "_tmpCam";

    /**
     * How a dimensionless unit is treated.
     */
    public static final String UNIT_DIMENSIONLESS = "";

    /**
     * A string formatting when dealing with Units.
     */
    public static final String UNIT_FMT = "%s: [%s] \"%s\".";

}
