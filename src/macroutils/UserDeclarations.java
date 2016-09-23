package macroutils;

import java.awt.*;
import java.io.*;
import java.util.*;
import macroutils.creator.*;
import macroutils.misc.*;
import star.base.neo.*;
import star.base.report.*;
import star.cadmodeler.*;
import star.common.*;
import star.meshing.*;
import star.post.*;
import star.surfacewrapper.*;
import star.vis.*;

/**
 * Low-level class for storing User Declarations (mutable) to be used with MacroUtils.
 *
 * @since April of 2016
 * @author Fabio Kasper
 */
public class UserDeclarations {

    /**
     * Main constructor for this class.
     *
     * @param m given MacroUtils object.
     */
    public UserDeclarations(MacroUtils m) {
    }

    /**
     * This method is called automatically by {@link MacroUtils} class. It is internal to MacroUtils.
     */
    public void updateInstances() {
    }

    //--
    //-- double[]'s
    //--
    /**
     * Useful predefined variable for storing Coordinates.
     */
    public double[] coord1 = {0., 0., 0.};

    /**
     * Useful predefined variable for storing Coordinates.
     */
    public double[] coord2 = {0., 0., 0.};

    /**
     * Useful predefined variable for storing Coordinates.
     */
    public double[] coord3 = {0., 0., 0.};

    /**
     * Useful predefined variable for storing Coordinates.
     */
    public double[] coord4 = {0., 0., 0.};

    //--
    //-- Public Physics variables that can be changed.
    //--
    /**
     * Gravity vector defined as double. Change accordingly. Default = {0, -9.81, 0} [m/s^2]
     */
    public double[] gravity = {0., -9.81, 0.};

    /**
     * Initial Pressure value in default units. See {@link #defUnitPress}. Default is 0Pa.
     */
    public double p0 = 0.0;

    /**
     * Initial Temperature value in default units. See {@link #defUnitTemp}. Default is 300K.
     */
    public double t0 = 300.0;

    /**
     * Initial Turbulent Intensity for RANS Models. Default = 0.05.
     */
    public double ti0 = 0.05;

    /**
     * Initial Turbulent Viscosity Ratio for RANS Models. Default = 10.
     */
    public double tvr0 = 10.0;

    /**
     * Initial Velocity Scale for RANS Turbulence Models in default units. See {@link #defUnitVel}.
     */
    public double tvs0 = 0.5;

    /**
     * Initial Velocity Array in default units. See {@link #defUnitVel}.
     */
    public double[] v0 = {0., 0., 0.};

    /**
     * Minimum clipping Temperature in default units.
     */
    public double clipMinT = -50;

    /**
     * Maximum clipping Temperature in default units.
     */
    public double clipMaxT = 3000;

    /**
     * Density of Air in default units. See {@link #defUnitDen}
     */
    public double denAir = 1.18415;

    /**
     * Density of Water in default units. See {@link #defUnitDen}
     */
    public double denWater = 997.561;

    /**
     * Reference Altitude array of values in default units. See {@link #defUnitLength}.
     */
    public double[] refAlt = new double[]{0, 0, 0};

    /**
     * Reference Density value in default units. See {@link #defUnitDen}.
     */
    public double refDen = denAir;

    /**
     * Reference Pressure value in default units. See {@link #defUnitPress}. Default is 101325Pa.
     */
    public double refP = 101325.;

    /**
     * Reference Temperature value in default units. See {@link #defUnitTemp}.
     */
    public double refT = t0;

    /**
     * Radiation Emissivity. Default = 0.8.
     */
    public double radEmissivity = 0.8;

    /**
     * Radiation Transmissivity. Default = 0.
     */
    public double radTransmissivity = 0.;

    /**
     * Radiation Sharp Angle. Default = 150.
     */
    public double radSharpAngle = 150.;

    /**
     * Radiation Patch Proportion of Faces. Default = 100%.
     */
    public double radPatchProp = 100.;

    /**
     * Viscosity of Air in default units. See {@link #defUnitVisc}
     */
    public double viscAir = 1.85508E-5;

    /**
     * Viscosity of Water in default units. See {@link #defUnitVisc}
     */
    public double viscWater = 8.8871E-4;

    //--
    //-- Public API variables from STAR-CCM+.
    //--
    /**
     * Some useful Global Variables: Auto Mesh Operations.
     */
    public AutoMeshOperation autoMshOp = null, autoMshOp1 = null, autoMshOp2 = null, autoMshOp3 = null;

    /**
     * A Global Boundary variable. Useful somewhere.
     */
    public Boundary bdry = null, bdry1 = null, bdry2 = null, bdry3 = null;

    /**
     * A Global Boundary Interface variable. Useful somewhere.
     */
    public BoundaryInterface bdryIntrf = null, bdryIntrf1 = null, bdryIntrf2 = null, bdryIntrf3 = null;

    /**
     * A Global 3D-CAD Body variable. Useful somewhere.
     */
    public Body cadBody = null, cadBody1 = null, cadBody2 = null, cadBody3 = null;

    /**
     * A Global CadModel variable. Useful somewhere.
     */
    public CadModel cadModel = null, cadModel1 = null, cadModel2 = null, cadModel3 = null;

    /**
     * A Global CadPart variable. Useful somewhere.
     */
    public CadPart cadPrt = null, cadPrt1 = null, cadPrt2 = null, cadPrt3 = null;

    /**
     * A Global CellSurfacePart variable. Useful somewhere.
     */
    public CellSurfacePart cellSrf = null, cellSrf1 = null, cellSrf2 = null, cellSrf3 = null;

    /**
     * A Global CompositePart variable. Useful somewhere.
     */
    public CompositePart compPart = null, compPart1 = null, compPart2 = null, compPart3 = null;

    /**
     * The Original Laboratory Coordinate System. Useful somewhere.
     */
    public CoordinateSystem csys0 = null;

    /**
     * Some useful Global Variables: Coordinate System.
     */
    public CoordinateSystem csys = null, csys1 = null, csys2 = null, csys3 = null;

    /**
     * Some useful Global Variables: Delta Monitor Update Event.
     */
    public DeltaMonitorUpdateEvent updEventDelta = null, updEventDelta1 = null, updEventDelta2 = null,
            updEventDelta3 = null;

    /**
     * Some useful Global Variables: Displayer.
     */
    public Displayer disp = null, disp1 = null, disp2 = null, disp3 = null;

    /**
     * Just an empty DoubleVector. Useful somewhere.
     */
    public DoubleVector dv0 = new DoubleVector(StaticDeclarations.COORD0);

    /**
     * A Global Feature Curve variable. Useful somewhere.
     */
    public FeatureCurve featCrv = null;

    /**
     * Some useful Global Variables: Field Function.
     */
    public FieldFunction ff = null, ff1 = null, ff2 = null, ff3 = null;

    /**
     * Some useful Global Variables: Frequency Monitor Update Event.
     */
    public FrequencyMonitorUpdateEvent updEventFreq = null, updEventFreq1 = null, updEventFreq2 = null,
            updEventFreq3 = null;

    /**
     * Some useful Global Variables: Geometry Parts.
     */
    public GeometryPart geomPrt = null, geomPrt1 = null, geomPrt2 = null, geomPrt3 = null;

    /**
     * A Global Interface variable. Useful somewhere.
     */
    public Interface intrf = null, intrf1 = null, intrf2 = null, intrf3 = null;

    /**
     * The Original Laboratory Coordinate System. Useful somewhere.
     */
    public LabCoordinateSystem lab0 = null;

    /**
     * Some useful Global Variables: Leaf Mesh Parts.
     */
    public LeafMeshPart leafMshPrt = null, leafMshPrt1 = null, leafMshPrt2 = null, leafMshPrt3 = null;

    /**
     * Some useful Global Variables: Lines.
     */
    public LinePart line = null, line1 = null, line2 = null;

    /**
     * Some useful Global Variables: Logic Update Event.
     */
    public LogicUpdateEvent updEventLogic = null, updEventLogic1 = null, updEventLogic2 = null, updEventLogic3 = null;

    /**
     * Default Colormap for Scalar Displayers.
     */
    public LookupTable defColormap = null;

    /**
     * Some useful Global Variables: Mesh Operation Custom Mesh Controls.
     */
    public CustomMeshControl mshCtrl = null, mshCtrl1 = null, mshCtrl2 = null, mshCtrl3 = null;

    /**
     * Some useful Global Variables: Mesh Operations.
     */
    public MeshOperation mshOp = null, mshOp1 = null, mshOp2 = null, mshOp3 = null;

    /**
     * Some useful Global Variables: Mesh Operation Parts.
     */
    public MeshOperationPart mshOpPrt = null, mshOpPrt1 = null, mshOpPrt2 = null, mshOpPrt3 = null;

    /**
     * Some useful Global Variables: Monitors.
     */
    public Monitor mon = null, mon1 = null, mon2 = null;

    /**
     * Global MacroUtils variable for storing a Monitor Plot. Useful when creating a Report.
     */
    public MonitorPlot monPlot = null;

    /**
     * Some useful Global Variables: Planes.
     */
    public PlaneSection plane = null, plane1 = null, plane2 = null;

    /**
     * Some useful Global Variables: Part Curves.
     */
    public PartCurve partCrv = null, partCrv1 = null, partCrv2 = null, partCrv3 = null;

    /**
     * Some useful Global Variables: Part Surfaces.
     */
    public PartSurface partSrf = null, partSrf1 = null, partSrf2 = null, partSrf3 = null;

    /**
     * Some useful Global Variables: Points.
     */
    public PointPart point = null, point1 = null, point2 = null, point3 = null, point4 = null, point5 = null;

    /**
     * Some useful Global Variables: Physics Continuas.
     */
    public PhysicsContinuum physCont = null, physCont1 = null, physCont2 = null, physCont3 = null;

    /**
     * Some useful Global Variables: Range Monitor Update Event.
     */
    public RangeMonitorUpdateEvent updEventRange = null, updEventRange1 = null, updEventRange2 = null,
            updEventRange3 = null;

    /**
     * Some useful Global Variables: Recorded Solution View.
     */
    public RecordedSolutionView recSolView = null, recSolView1 = null, recSolView2 = null, recSolView3 = null;

    /**
     * Some useful Global Variables: Regions.
     */
    public Region region = null, region1 = null, region2 = null, region3 = null;

    /**
     * Some useful Global Variables: Reports.
     */
    public Report rep = null, rep1 = null, rep2 = null, rep3 = null;

    /**
     * Global MacroUtils variable for storing a Report Monitor. Useful when creating a Report.
     */
    public ReportMonitor repMon = null, repMon1 = null, repMon2 = null, repMon3 = null;

    /**
     * Some useful Global Variables: Scenes.
     */
    public Scene scene = null, scene1 = null, scene2 = null, scene3 = null;

    /**
     * Some useful Global Variables: Simple Block Parts.
     */
    public SimpleBlockPart simpleBlkPrt = null, simpleBlkPrt1 = null, simpleBlkPrt2 = null, simpleBlkPrt3 = null;

    /**
     * Some useful Global Variables: Simple Cylinder Parts.
     */
    public SimpleCylinderPart simpleCylPrt = null, simpleCylPrt1 = null, simpleCylPrt2 = null, simpleCylPrt3 = null;

    /**
     * Some useful Global Variables: Simple Sphere Parts.
     */
    public SimpleSpherePart simpleSphPrt = null, simpleSphPrt1 = null, simpleSphPrt2 = null, simpleSphPrt3 = null;

    /**
     * A Global SolidModelPart variable. Useful somewhere.
     */
    public SolidModelPart sldPrt = null, sldPrt1 = null, sldPrt2 = null, sldPrt3 = null;

    /**
     * Some useful Global Variables: Solution History.
     */
    public SolutionHistory solHist = null, solHist1 = null, solHist2 = null, solHist3 = null;

    /**
     * Some useful Global Variables: Solution Representation.
     */
    public SolutionRepresentation solRepr = null, solRepr1 = null, solRepr2 = null, solRepr3 = null;

    /**
     * Some useful Global Variables: Solution View.
     */
    public SolutionView solView = null, solView1 = null, solView2 = null, solView3 = null;

    /**
     * Some useful Global Variables: Star Plots.
     */
    public StarPlot starPlot = null, starPlot1 = null, starPlot2 = null;

    /**
     * Some useful Global Variables: Surface Wrapper Mesh Operation.
     */
    public SurfaceWrapperAutoMeshOperation wrapMshOp = null, wrapMshOp1 = null, wrapMshOp2 = null, wrapMshOp3 = null;

    /**
     * Some useful Global Variables: Tables.
     */
    public Table table = null, table1 = null, table2 = null, table3 = null;

    /**
     * Some useful Global Variables: Update Event.
     */
    public UpdateEvent updEvent = null, updEvent1 = null, updEvent2 = null, updEvent3 = null;

    /**
     * Some useful Global Variables: Visualization View (Camera View).
     */
    public VisView camView = null, camView1 = null, camView2 = null, camView3 = null;

    /**
     * Default Camera when creating new Scenes.
     */
    public VisView defCamView = null;

    /**
     * Some useful Global Variables: Visualization View (Camera View).
     */
    public VisView vv = null, vv1 = null, vv2 = null, vv3 = null, vv4 = null, vv5 = null, vv6 = null;

    //--
    //-- Public Mesh Parameters
    //--
    /**
     * MacroUtils variable for setting the default Tessellation method. Default is
     * {@link StaticDeclarations.GrowthRate#MEDIUM}.
     */
    public StaticDeclarations.Tessellation defTessOpt = StaticDeclarations.Tessellation.MEDIUM;

    /**
     * Directed Mesh Construction points subdivisions in O-Grid, when using
     * {@link CreateMeshOperation#directedMeshing_Pipe}. Change with care. Default = 0.
     */
    public int dmDiv = 0;

    /**
     * Directed Mesh O-Grid Factor, when using {@link CreateMeshOperation#directedMeshing_Pipe}. Change with care.
     * Default = 1. / 1.10.
     */
    public double dmOGF = 1. / 1.10;

    /**
     * Directed Mesh Smooths in Patch mesh, when using {@link CreateMeshOperation#directedMeshing_Pipe}. Change with
     * care. Default = 0.
     */
    public int dmSmooths = 0;

    /**
     * Default Mesh Feature Angle.
     */
    public double mshSharpEdgeAngle = 30;

    /**
     * Mesh Base Reference Size in default units. See {@link #defUnitLength}.
     */
    public double mshBaseSize = 3.0;

    /**
     * Mesh Growth Factor for Tets/Polys. Default = 1.0
     */
    public double mshGrowthFactor = 1.0;

    /**
     * Volume Mesh Number of Optimization cycles. Default = 1.
     */
    public int mshOptCycles = 1;

    /**
     * Number of Points in Gap when using Mesh Proximity.
     */
    public double mshProximityPointsInGap = 2.0;

    /**
     * Search floor in default units for Mesh Proximity. See {@link #defUnitLength}.
     */
    public double mshProximitySearchFloor = 0.0;

    /**
     * Volume Mesh Quality Threshold. Default = 0.4
     */
    public double mshQualityThreshold = 0.4;

    /**
     * Surface Mesh Number of Points per Circle. Default = 36.
     */
    public int mshSrfCurvNumPoints = 36;

    /**
     * Surface Mesh Growth Rate. Default = 1.3.
     */
    public double mshSrfGrowthRate = 1.3;

    /**
     * Surface Mesh Minimum Relative Size. Default = 25%.
     */
    public double mshSrfSizeMin = 25;

    /**
     * Surface Mesh Target Relative Size. Default = 100%.
     */
    public double mshSrfSizeTgt = 100;

    /**
     * Maximum Trimmer Relative Size. Default = 10000%.
     */
    public double mshTrimmerMaxCellSize = 10000;

    /**
     * Trimmer Volume Growth Rate when meshing. Default is {@link StaticDeclarations.GrowthRate#MEDIUM}.
     */
    public StaticDeclarations.GrowthRate mshTrimmerGrowthRate = StaticDeclarations.GrowthRate.MEDIUM;

    /**
     * Surface Wrapper Feature Angle. Default = 30deg.
     */
    public double mshWrapperFeatureAngle = 30.;

    /**
     * Surface Wrapper Scale factor. Default = 100%.
     */
    public double mshWrapperScaleFactor = 100.;

    /**
     * Prism Layers Gap Fill Percentage. Default is 25%.
     */
    public double prismsGapFillPerc = 25;

    /**
     * Prism Layers Chopping Percentage. Default is 50% in STAR-CCM+. In Macro Utils is 10%.
     */
    public double prismsLyrChoppPerc = 10.0;

    /**
     * Prism Layers Minimum Thickness. Default is 10% in STAR-CCM+. In Macro Utils is 1%.
     */
    public double prismsMinThickn = 1.0;

    /**
     * Prism Layers Near Core Aspect Ratio (NCLAR). Default is 0.0 in STAR-CCM+. In Macro Utils is 0.5.
     */
    public double prismsNearCoreAspRat = 0.5;

    /**
     * Prism Layers Relative Size. If 0%, prism layers are disabled. Default = 30%.
     */
    public double prismsRelSizeHeight = 30.;

    /**
     * Prism Stretch Ratio. Default = 1.5.
     */
    public double prismsStretching = 1.5;

    /**
     * Number of Prism Layers. If 0, prism layers are disabled. Default = 2.
     */
    public int prismsLayers = 2;

    /**
     * Number of layers for the Thin Mesher. Default is 2.
     */
    public int thinMeshLayers = 2;

    /**
     * Maximum Thickness for the Thin Mesher. Default in STAR-CCM+ is 0%. In MacroUtils is 100%.
     */
    public double thinMeshMaxThickness = 100.;

    //--
    //-- Public Solver Parameters
    //--
    /**
     * Courant Number for the Coupled Solver. Default = 5.
     */
    public double CFL = 5;

    /**
     * Maximum Iterations. Default 1000.
     */
    public int maxIter = 1000;

    /**
     * Maximum Turbulent Viscosity Ratio. Default 1e5.
     */
    public double maxTVR = 1e5;

    /**
     * Second Order discretization on time when Unsteady? Default = false.
     */
    public boolean trn2ndOrder = false;

    /**
     * Maximum Inner Iterations when using Unsteady.
     */
    public int trnInnerIter = 15;

    /**
     * Maximum Physical time when using Unsteady. See {@link #defUnitTime}.
     */
    public double trnMaxTime = 10.;

    /**
     * Physical time step when using Unsteady. See {@link #defUnitTime}.
     */
    public double trnTimestep = 0.001;

    /**
     * URF for Energy. Use this value for changing both Fluid and Solid URFs simultaneously. Otherwise, use
     * {@link #urfFluidEnrgy} and/or {@link #urfSolidEnrgy} individually. Default = 0 (unused).
     */
    public double urfEnergy = 0.;

    /**
     * URF for Fluid Energy. Default: {@link StaticDeclarations.DefaultURFs#FLUID_ENERGY}.
     */
    public double urfFluidEnrgy = StaticDeclarations.DefaultURFs.FLUID_ENERGY.getValue();

    /**
     * URF for Granular Temperature. Default: {@link StaticDeclarations.DefaultURFs#GRANULAR_TEMPERATURE}.
     */
    public double urfGranTemp = StaticDeclarations.DefaultURFs.GRANULAR_TEMPERATURE.getValue();

    /**
     * URF for K-Epsilon. Default: {@link StaticDeclarations.DefaultURFs#K_EPSILON}.
     */
    public double urfKEps = StaticDeclarations.DefaultURFs.K_EPSILON.getValue();

    /**
     * URF for K-Epsilon Turbulent Viscosity. Default: 1.0.
     */
    public double urfKEpsTurbVisc = 1.0;

    /**
     * URF for K-Omega. Default: {@link StaticDeclarations.DefaultURFs#K_OMEGA}.
     */
    public double urfKOmega = StaticDeclarations.DefaultURFs.K_OMEGA.getValue();

    /**
     * URF for K-Omega Turbulent Viscosity. Default: 1.0.
     */
    public double urfKOmegaTurbVisc = 1.0;

    /**
     * URF for Phase Couple Velocity. Default: {@link StaticDeclarations.DefaultURFs#PHASE_COUPLED_VELOCITY}.
     */
    public double urfPhsCplVel = StaticDeclarations.DefaultURFs.PHASE_COUPLED_VELOCITY.getValue();

    /**
     * URF for Pressure. Default: {@link StaticDeclarations.DefaultURFs#PRESSURE}.
     */
    public double urfP = StaticDeclarations.DefaultURFs.PRESSURE.getValue();

    /**
     * URF for PPDF Combustion. Default: {@link StaticDeclarations.DefaultURFs#PPDF_COMBUSTION}.
     */
    public double urfPPDFComb = StaticDeclarations.DefaultURFs.PPDF_COMBUSTION.getValue();

    /**
     * URF for Reynolds Stress Models. Default: {@link StaticDeclarations.DefaultURFs#REYNOLDS_STRESS_MODEL}.
     */
    public double urfRS = StaticDeclarations.DefaultURFs.REYNOLDS_STRESS_MODEL.getValue();

    /**
     * URF for Reynolds Stress Turbulent Viscosity. Default: 1.0.
     */
    public double urfRSTurbVisc = 1.0;

    /**
     * URF for Solid Energy. Default: {@link StaticDeclarations.DefaultURFs#SOLID_ENERGY}.
     */
    public double urfSolidEnrgy = StaticDeclarations.DefaultURFs.SOLID_ENERGY.getValue();
    ;

  /** URF for Species. Default: {@link StaticDeclarations.DefaultURFs#SPECIES}. */
  public double urfSpecies = StaticDeclarations.DefaultURFs.SPECIES.getValue();

    /**
     * URF for Velocity. Default: {@link StaticDeclarations.DefaultURFs#VELOCITY}.
     */
    public double urfVel = StaticDeclarations.DefaultURFs.VELOCITY.getValue();

    /**
     * URF for Volume Fraction. Default: {@link StaticDeclarations.DefaultURFs#VOLUME_FRACTION}.
     */
    public double urfVolFrac = StaticDeclarations.DefaultURFs.VOLUME_FRACTION.getValue();

    /**
     * URF for VOF. Default: {@link StaticDeclarations.DefaultURFs#VOLUME_FRACTION}.
     */
    public double urfVOF = StaticDeclarations.DefaultURFs.VOF.getValue();

    //--
    //-- Public ArrayList's
    //--
    /**
     * Useful Variable for storing Doubles.
     */
    public ArrayList<Double> doubles = new ArrayList(), doubles2 = new ArrayList();

    /**
     * Useful Variable for storing Files.
     */
    public ArrayList<File> files = new ArrayList(), files2 = new ArrayList();

    /**
     * Useful Variable for storing Strings.
     */
    public ArrayList<String> strings = new ArrayList(), strings2 = new ArrayList();

    //--
    //-- Public ArrayList's from STAR-CCM+ API
    //--
    /**
     * Useful Variable for storing Boundaries.
     */
    public ArrayList<Boundary> boundaries = new ArrayList(), boundaries2 = new ArrayList();

    /**
     * Useful Variable for storing Cad Bodies.
     */
    public ArrayList<Body> cadBodies = new ArrayList(), cadBodies2 = new ArrayList();

    /**
     * Useful Variable for storing Cad Models.
     */
    public ArrayList<Body> cadModels = new ArrayList(), cadModels2 = new ArrayList();

    /**
     * Useful Variable for storing Cad Parts.
     */
    public ArrayList<CadPart> cadParts = new ArrayList(), cadParts2 = new ArrayList();

    /**
     * Useful Variable for storing Colors.
     */
    public ArrayList<Color> colors = new ArrayList(), colors2 = new ArrayList();

    /**
     * Useful Variable for storing Composite Parts.
     */
    public ArrayList<CompositePart> compositeParts = new ArrayList(), compositeParts2 = new ArrayList();

    /**
     * Useful Variable for storing Double Vectors.
     */
    public DoubleVector doubleVector = new DoubleVector(), doubleVector2 = new DoubleVector();

    /**
     * Useful Variable for storing Field Functions.
     */
    public ArrayList<FieldFunction> fieldFunctions = new ArrayList(), fieldFunctions2 = new ArrayList();

    /**
     * Useful Variable for storing Geometry Objects (STAR-CCM+ Geometry Object).
     */
    public ArrayList<GeometryObject> geometryObjects = new ArrayList(), geometryObjects2 = new ArrayList();

    /**
     * Useful Variable for storing Geometry Parts.
     */
    public ArrayList<GeometryPart> geometryParts = new ArrayList(), geometryParts2 = new ArrayList();

    /**
     * Useful Variable for storing Leaf Mesh Parts.
     */
    public ArrayList<LeafMeshPart> leafMeshParts = new ArrayList(), leafMeshParts2 = new ArrayList();

    /**
     * Useful Variable for storing Meshers.
     */
    public ArrayList<String> meshers = new ArrayList(), meshers2 = new ArrayList();

    /**
     * Useful Variable for storing Mesh Parts.
     */
    public ArrayList<MeshPart> meshParts = new ArrayList(), meshParts2 = new ArrayList();

    /**
     * Useful Variable for storing Monitors.
     */
    public ArrayList<Monitor> monitors = new ArrayList(), monitors2 = new ArrayList();

    /**
     * Useful Variable for storing Named Objects (STAR-CCM+ Object).
     */
    public ArrayList<NamedObject> namedObjects = new ArrayList(), namedObjects2 = new ArrayList();

    /**
     * Useful Variable for storing Objects.
     */
    public ArrayList<Object> objects = new ArrayList(), objects2 = new ArrayList();

    /**
     * Useful Variable for storing Part Curves.
     */
    public ArrayList<PartCurve> partCurves = new ArrayList(), partCurves2 = new ArrayList();

    /**
     * Useful Variable for storing Part Surfaces.
     */
    public ArrayList<PartSurface> partSurfaces = new ArrayList(), partSurfaces2 = new ArrayList();

    /**
     * Useful Variable for storing Planes.
     */
    public ArrayList<PlaneSection> planes = new ArrayList(), planes2 = new ArrayList();

    /**
     * Useful Variable for storing Regions.
     */
    public ArrayList<Region> regions = new ArrayList(), regions2 = new ArrayList();

    /**
     * Useful Variable for storing Reports.
     */
    public ArrayList<Report> reports = new ArrayList(), reports2 = new ArrayList();

    /**
     * Useful Variable for storing Scenes.
     */
    public ArrayList<Scene> scenes = new ArrayList(), scenes2 = new ArrayList();

    /**
     * Useful Variable for storing Camera Views.
     */
    public ArrayList<VisView> cameras = new ArrayList(), cameras2 = new ArrayList();

    //--
    //-- Strings
    //--
    /**
     * Useful name for a Boundary Condition.
     */
    public String bcBottom = "bottom";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcChannel = "channel";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcCold = "cold";

    /**
     * STAR-CCM+ likes to call Part Surfaces coming from 3D-CAD Parts as <b>Default</b>, right?
     */
    public String bcDefault = "Default";

    /**
     * STAR-CCM+ likes to call Part Surfaces as <b>Faces</b>, right?
     */
    public String bcFaces = "Faces";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcFloor = "floor";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcGround = "ground";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcHot = "hot";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcInlet = "inlet";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcOutlet = "outlet";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcSym = "symmetry";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcSym1 = "symmetry1";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcSym2 = "symmetry2";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcTop = "top";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcWall = "wall";

    /**
     * Useful name for a Boundary Condition.
     */
    public String bcWalls = "walls";

    /**
     * Simulation name/title. It is used when saving. See {@link macroutils.MacroUtils#saveSim}.
     */
    public String simTitle = null;

    /**
     * Full Simulation path with file and extension. E.g.: <i>/home/user/mySimFile.sim</i>.
     */
    public String simFullPath = null;

    /**
     * Simulation path. It is used when saving. See {@link macroutils.MacroUtils#saveSim}.
     */
    public String simPath = null;

    /**
     * Just strings. Useful somewhere.
     */
    public String string = "", string1 = "", string2 = "", string3 = "", text = "";

    //--
    //-- Dimensions
    //--
    /**
     * Dimensions of Density. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimDensity = new Dimensions();

    /**
     * Dimensionless dimensions. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimDimensionless = new Dimensions();

    /**
     * Dimensions of Force. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimForce = new Dimensions();

    /**
     * Dimensions of Length. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimLength = new Dimensions();

    /**
     * Dimensions of Mass. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimMass = new Dimensions();

    /**
     * Dimensions of Mass Flow. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimMassFlow = new Dimensions();

    /**
     * Dimensions of Molecular Flow. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimMolFlow = new Dimensions();

    /**
     * Dimensions of Pressure. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimPress = new Dimensions();

    /**
     * Dimensions of Time. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimTime = new Dimensions();

    /**
     * Dimensions of Velocity. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimVel = new Dimensions();

    /**
     * Dimensions of Viscosity. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimVisc = new Dimensions();

    /**
     * Dimensions of Volumetric Flow. Definition in {@link MainUpdater#customUnits}.
     */
    public Dimensions dimVolFlow = new Dimensions();

    //--
    //-- Units
    //--
    /**
     * Default unit of Acceleration, when using MacroUtils. Default is {@link #unit_mps2}.
     */
    public Units defUnitAccel = null;

    /**
     * Default unit of Angle, when using MacroUtils. Default is {@link #unit_deg}.
     */
    public Units defUnitAngle = null;

    /**
     * Default unit of Area, when using MacroUtils. Default is {@link #unit_m2}.
     */
    public Units defUnitArea = null;

    /**
     * Default unit of Density, when using MacroUtils. Default is {@link #unit_kgpm3}.
     */
    public Units defUnitDen = null;

    /**
     * Default unit of Force, when using MacroUtils. Default is {@link #unit_N}.
     */
    public Units defUnitForce = null;

    /**
     * Default unit of Heat Transfer Coefficient, when using MacroUtils. Default is {@link #unit_Wpm2K}.
     */
    public Units defUnitHTC = null;

    /**
     * Default unit of Length, when using MacroUtils. Default is {@link #unit_mm}.
     */
    public Units defUnitLength = null;

    /**
     * Default unit of Mass Flow Rate, when using MacroUtils. Default is {@link #unit_kgps}.
     */
    public Units defUnitMFR = null;

    /**
     * Default unit of Pressure, when using MacroUtils. Default is {@link #unit_Pa}.
     */
    public Units defUnitPress = null;

    /**
     * Default unit of Temperature, when using MacroUtils. Default is {@link #unit_C}.
     */
    public Units defUnitTemp = null;

    /**
     * Default unit of Time, when using MacroUtils. Default is {@link #unit_s}.
     */
    public Units defUnitTime = null;

    /**
     * Default unit of Velocity, when using MacroUtils. Default is {@link #unit_mps}.
     */
    public Units defUnitVel = null;

    /**
     * Default unit of Viscosity, when using MacroUtils. Default is {@link #unit_Pa_s}.
     */
    public Units defUnitVisc = null;

    /**
     * Default unit of Volume, when using MacroUtils. Default is {@link #unit_m3}.
     */
    public Units defUnitVolume = null;

    /**
     * Atmosphere unit (Pressure).
     */
    public Units unit_atm = null;

    /**
     * Bar unit (Pressure).
     */
    public Units unit_bar = null;

    /**
     * Celsius unit (Temperature).
     */
    public Units unit_C = null;

    /**
     * CentiPoise unit (Viscosity).
     */
    public Units unit_cP = null;

    /**
     * Centimeter of Water unit (Pressure).
     */
    public Units unit_cmH2O = null;

    /**
     * Centimeter of Mercury unit (Pressure).
     */
    public Units unit_cmHg = null;

    /**
     * Dimensionless unit.
     */
    public Units unit_Dimensionless = null;

    /**
     * Degree unit (Angle).
     */
    public Units unit_deg = null;

    /**
     * Dyne per Square Centimeter unit (Pressure).
     */
    public Units unit_dynepcm2 = null;

    /**
     * Fahrenheit unit (Temperature).
     */
    public Units unit_F = null;

    /**
     * Gallon unit (Volume).
     */
    public Units unit_gal = null;

    /**
     * Gallons per Second unit (Volume/Time).
     */
    public Units unit_galps = null;

    /**
     * Gallons per Minute unit (Volume/Time).
     */
    public Units unit_galpmin = null;

    /**
     * Gram unit (Mass).
     */
    public Units unit_g = null;

    /**
     * Gram per Minute unit (Mass/Time).
     */
    public Units unit_gpmin = null;

    /**
     * Gram per Second unit (Mass/Time).
     */
    public Units unit_gps = null;

    /**
     * Kelvin unit (Temperature).
     */
    public Units unit_K = null;

    /**
     * kiloNewton (Force).
     */
    public Units unit_kN = null;

    /**
     * kiloPascal unit (Pressure).
     */
    public Units unit_kPa = null;

    /**
     * Hour unit (Time).
     */
    public Units unit_h = null;

    /**
     * Kilogram unit (Mass).
     */
    public Units unit_kg = null;

    /**
     * Kilogram per Hour unit (Mass/Time).
     */
    public Units unit_kgph = null;

    /**
     * Gram per Cubic Centimeter unit (Density).
     */
    public Units unit_gpcm3 = null;

    /**
     * Kilogram per Cubic Meter unit (Density).
     */
    public Units unit_kgpm3 = null;

    /**
     * Kilogram per Second unit (Mass/Time).
     */
    public Units unit_kgps = null;

    /**
     * Kilogram per Minute unit (Mass/Time).
     */
    public Units unit_kgpmin = null;

    /**
     * Kilogram-mole (Quantity).
     */
    public Units unit_kmol = null;

    /**
     * Kilogram-mole per second (Quantity/Time).
     */
    public Units unit_kmolps = null;

    /**
     * Kilometer per Hour unit (Velocity).
     */
    public Units unit_kph = null;

    /**
     * Knot unit (Velocity).
     */
    public Units unit_kt = null;

    /**
     * Liter per Hour unit (Volume/Time).
     */
    public Units unit_lph = null;

    /**
     * Liter per Minute unit (Volume/Time).
     */
    public Units unit_lpmin = null;

    /**
     * Liter per Second unit (Volume/Time).
     */
    public Units unit_lps = null;

    /**
     * Meter unit (Length).
     */
    public Units unit_m = null;

    /**
     * Square Meter unit (Area).
     */
    public Units unit_m2 = null;

    /**
     * Cubic Meter unit (Volume).
     */
    public Units unit_m3 = null;

    /**
     * Cubic Meter per Hour unit (Volume/Time).
     */
    public Units unit_m3ph = null;

    /**
     * Millibar unit (Pressure).
     */
    public Units unit_mbar = null;

    /**
     * Minute unit (Time).
     */
    public Units unit_min = null;

    /**
     * Millimeter unit (Length).
     */
    public Units unit_mm = null;

    /**
     * Square Millimeter unit (Area).
     */
    public Units unit_mm2 = null;

    /**
     * Cubic Millimeter unit (Volume).
     */
    public Units unit_mm3 = null;

    /**
     * Millimeter of Water unit (Pressure).
     */
    public Units unit_mmH2O = null;

    /**
     * Millimeter of Mercury unit (Pressure).
     */
    public Units unit_mmHg = null;

    /**
     * Milliseconds (Time).
     */
    public Units unit_ms = null;

    /**
     * Meter per Second unit (Velocity).
     */
    public Units unit_mps = null;

    /**
     * Micrometer per Second unit (Velocity).
     */
    public Units unit_umps = null;

    /**
     * Millimeter per Second unit (Velocity).
     */
    public Units unit_mmps = null;

    /**
     * Meter per Square Second unit (Velocity / Time).
     */
    public Units unit_mps2 = null;

    /**
     * Newton (Force).
     */
    public Units unit_N = null;

    /**
     * Poise unit (Viscosity).
     */
    public Units unit_P = null;

    /**
     * Pascal unit (Pressure).
     */
    public Units unit_Pa = null;

    /**
     * Pascal x Second unit (Viscosity).
     */
    public Units unit_Pa_s = null;

    /**
     * Radian per Second unit (Angular Velocity).
     */
    public Units unit_radps = null;

    /**
     * Rotation per Minute unit (Angular Velocity).
     */
    public Units unit_rpm = null;

    /**
     * Second unit (Time).
     */
    public Units unit_s = null;

    /**
     * microPascal unit (Pressure).
     */
    public Units unit_uPa = null;

    /**
     * Watt unit (Power).
     */
    public Units unit_W = null;

    /**
     * Watt per Square Meter x Kelvin unit (Heat Transfer Coefficient).
     */
    public Units unit_Wpm2K = null;

    //--
    //-- java.io.File's
    //--
    /**
     * A path containing CAD files.
     */
    public File cadPath = null;

    /**
     * A path containing DBS files according {@link java.io.File} definition.
     */
    public File dbsPath = null;

    /**
     * Current simulation File according {@link java.io.File} definition.
     */
    public File simFile = null;

    //--
    //-- Pictures settings
    //--
    /**
     * Use anti aliasing when writing Scenes to pictures. Default is false.
     */
    public boolean picAntiAliasing = false;

    /**
     * Default picture path/folder for writing pictures with MacroUtils. Default is {@link #simPath}.
     */
    public String picPath = simPath;

    /**
     * X Resolution for writing pictures with MacroUtils. Default resolution is 1280x720.
     */
    public int picResX = 1280;

    /**
     * X Resolution for writing pictures with MacroUtils. Default resolution is 1280x720.
     */
    public int picResY = 720;

    /**
     * Use transparent background when writing Scenes to pictures. Default is false.
     */
    public boolean picTransparentBackground = false;

    //--
    //-- Postprocessing settings in general
    //--
    /**
     * Default Color when required by MacroUtils.
     */
    public Color defColor = StaticDeclarations.Colors.LIGHT_GRAY.getColor();

    /**
     * MacroUtils variable for controlling the Tubes width when using a Streamline Displayer. Default = 0.005.
     */
    public double postStreamlinesTubesWidth = 0.005;

    /**
     * MacroUtils variable for controlling the Streamline Resolution: n x n. Default = 5.
     */
    public int postStreamlineResolution = 5;

}
