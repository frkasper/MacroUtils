import macroutils.*;
import star.vis.*;

/**
 * Turbulent flow with Ideal Gas. This Demo shows how to do more advanced post processing 
 * using macros in STAR-CCM+.
 * 
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
*/
public class Demo13_Streamlines extends MacroUtils {

  public void execute() {

    _initUtils();
    simTitle = "Demo13_Streamlines";
    //--
    pre();
    solve();
    post();
    _finalize();
    
  }
  
  void pre() {
    vv1 = readCameraView("cam1|2.234138e-03,-5.793002e-04,3.969814e-02|2.234138e-03,-5.793002e-04,3.269352e-01|0.000000e+00,1.000000e+00,0.000000e+00|7.498392e-02|0");
    vv2 = readCameraView("cam2|2.873504e-04,3.784037e-03,3.752940e-02|2.873504e-04,3.784037e-03,2.115639e-01|0.000000e+00,1.000000e+00,0.000000e+00|4.543212e-02|0");
    defCamView = vv1;
    if (hasValidVolumeMesh()) {
        sayLoud("Volume Mesh Found. Skipping Prep");
        return;
    }
    double r1 = 40.;
    double r2 = 20.;
    double l1 = 4 * r1;
    cadBody = create3DCad_Cylinder(r1, l1, new double[] {-0.5 * l1, 0, 0}, unit_mm, X, "CylMain");
    geometryParts.add(getGeometryPart(cadBody.getPresentationName()));
    cadBody = create3DCad_Cylinder(r2, 2 * r1, new double[] {-0.45 * l1 + r2, -2 * r1, 0}, unit_mm, Y, "CylIn");
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    getPartSurface(geomPrt, "y0").setPresentationName(bcInlet);
    geometryParts.add(geomPrt);
    cadBody = create3DCad_Cylinder(r2, 2 * r1, new double[] {0.45 * l1 - r2, 0, 0}, unit_mm, Y, "CylOut");
    geomPrt = getGeometryPart(cadBody.getPresentationName());
    getPartSurface(geomPrt, "y1").setPresentationName(bcOutlet);
    geometryParts.add(geomPrt);
    //-- Unite bodies
    mshOp = createMeshOperation_Unite(geometryParts);
    geometryParts2.add(getGeometryPart(mshOp.getPresentationName()));
    region = assignPartToRegion(getGeometryPart(mshOp.getPresentationName()));
    //--
    mshBaseSize = r2 / 5;
    prismsLayers = 1;
    prismsRelSizeHeight = 40;
    prismsStretching = 1.2;
    meshers = getMeshers(true, true, POLY, true);
    createMeshOperation_AutomatedMesh(geometryParts2, meshers);
    //--
    physCont = createPhysics_AirSteadySegregatedIdealGasKEps2Lyr();
    //--
    setBC_VelocityMagnitudeInlet(getBoundary(".*" + bcInlet), 3, 20, 0.05, 10);
    setBC_PressureOutlet(getBoundary(".*" + bcOutlet), 0, 21, 0.05, 10);
    //--
    genVolumeMesh();
    createScene_Mesh();
  }
  
  void solve() {
    if (hasSolution()) {
        sayLoud("Solution Found. Skipping Solve");
        return;
    }
    maxIter = 200;
    setSolverAggressiveURFs();
    runCase(true);
    //saveSim(simTitle + "_SS", false);
  }
  
  void post() {
    namedObjects.addAll(getAllBoundaries());
    namedObjects2.add(getBoundary(".*" + bcInlet));
    //--
    //-- One can make it all in a single Scenes. 
    scene = createScene_Scalar(namedObjects, getFieldFunction(varP), unit_Pa, true);
    scene.getAxes().setAxesVisible(false);
    sd1 = (ScalarDisplayer) getDisplayer(scene, ".*");
    sd1.setOpacity(0);
    sd1.getLegend().setVisible(false);
    
    pd1 = (PartDisplayer) createSceneDisplayer(scene, "Geometry", namedObjects);
    pd1.setOpacity(1);
    pd1.setColorMode(1);
    pd1.setDisplayerColorColor(colorLightGray);
    pd1.setRepresentation(queryGeometryRepresentation());
    pd2 = (PartDisplayer) createSceneDisplayer(scene, "Mesh", namedObjects);
    pd2.setOpacity(0);

    postFlyOverAndSavePics(scene, null, null, act1);
    postFlyOverAndSavePics(scene, null, null, act2);
    updateDeltaAngle(spr);
    startSpinning();
    postFlyOverAndSavePics(scene, null, null, act3);
    postFlyOverAndSavePics(scene, null, null, act4);
    stopSpinning();
    postFlyOverAndSavePics(scene, null, null, act5);
    //--
    //-- Animated Streamlines
    postStreamlinesTubesWidth = 0.0005;
    std = createSceneDisplayer_Streamline(scene, namedObjects2, true);
    std.getScalarDisplayQuantity().setRange(new double[] {0, 3.0});
    std.getAnimationManager().setMode(1);
    updateDeltaAngle(3 * spr);                  //-- This time it will take 3x longer the spin.
    int preSpin = 8 * fps;
    int frames = preSpin + 1 * spr * fps;       //-- spr is updated along with delta Angle.
    ad = scene.getAnimationDirector();
    ad.setFramesPerSecond(fps);
    StreamAnimationSettings sas = std.getAnimationManager().getSettings();
    sas.setCycleTime(0.75 * frames / fps);      //-- Adjust the coefficient to get the speed right. Smaller is faster.
    sas.setTracerDelay(1.0);
    sas.setTailTime(0.2);
    sas.setHeadSize(0.0001);
    ad.setIsPlaying(true);
    ad.start();
    ad.pause();
    postFlyOverAndSavePics(scene, null, null, preSpin);
    startSpinning();
    postFlyOverAndSavePics(scene, null, null, frames - preSpin);
    ad.stop(true);
  }

  @Override
  public void postFlyOverAndSavePics_prePrintPicture() {
    super.postFlyOverAndSavePics_prePrintPicture(); 
    double fadeIn = 1, fadeOut = 1;

    if (postCurFrame >= act1 && postCurFrame <= acts12) {
        if (postCurFrame == act1) {
            sayLoud("Act2 -- Fade Out & In events");
        }
        xx = new double[] {act1, act1 + 0.5 * fps};         //-- Transition in half second
        yy = new double[] {1.0, 0.0};
        fadeOut = evalLinearRegression(xx, yy, postCurFrame, false);
        pd1.setOpacity(fadeOut);
        //--
        xx = new double[] {xx[1] - 1, xx[1] + 0.5 * fps - 1};
        yy = new double[] {0.0, 1.0};
        fadeIn = evalLinearRegression(xx, yy, postCurFrame, false);
        pd2.setOpacity(fadeIn);
    }
        
    if (postCurFrame >= acts12 && postCurFrame <= acts13) {
        if (postCurFrame == acts12) {
            sayLoud("Act3 -- Spinning");
        }
    }
        
    if (postCurFrame >= acts13 && postCurFrame <= acts14) {
        if (postCurFrame == acts13) {
            sayLoud("Act4 -- Spinning & Change to Scalar Scene");
        }
        xx = new double[] {acts13, acts13 + 0.5 * fps};             //-- Transition in half second
        yy = new double[] {1.0, 0.0};
        fadeOut = evalLinearRegression(xx, yy, postCurFrame, false);
        pd2.setOpacity(fadeOut);
        //--
        xx = new double[] {xx[1] - 2, xx[1] + 0.5 * fps - 2};
        yy = new double[] {0.0, 1.0};
        fadeIn = evalLinearRegression(xx, yy, postCurFrame, false);
        if (fadeIn > 0.8) {
            sd1.getLegend().setVisible(true);
        }
        sd1.setOpacity(fadeIn);
    }
        
    if (postCurFrame >= acts14 && postCurFrame <= acts15) {
        if (postCurFrame == acts14) {
            sayLoud("Act5 -- Stop Spinning & Change back to Geometry");
        }
        xx = new double[] {acts15 - fps, acts15 - 0.5 * fps};       //-- Transition in half second
        yy = new double[] {1.0, 0.0};
        fadeOut = evalLinearRegression(xx, yy, postCurFrame, false);
        sd1.setOpacity(fadeOut);
        //--
        if (fadeOut >= 0.6) {
            fadeIn = 0.0;
        } else {
            sd1.getLegend().setVisible(false);
            fadeIn = 0.1;
        }
        pd1.setOpacity(fadeIn);
    }

    if (postCurFrame >= acts15) {
        ad.step(1);
    }
    
    updateSpinAngle();
    
  }

  @Override
  public void postFlyOverAndSavePics_postPrintPicture() {
    super.postFlyOverAndSavePics_postPrintPicture(); 
    spinAngleOld += deltaAngle * postFrames;
    spinAngle = spinAngleOld;
  }
  
  void startSpinning() {
    say("Spinning Started...");
    isSpinning = true;
    spinAngle = 0.;
    spinAngleOld = 0.;
    String trName = "Spinner";
    if (sim.getTransformManager().has(trName)) {
        st = (SimpleTransform) getVisTransform(trName);
    } else {
        st = createVisTransform_Simple(coord0, new double[] {0, -1, 0}, 0, null, null, trName);
    }
    setVisTransform(pd1, st);
    setVisTransform(pd2, st);
    setVisTransform(sd1, st);
    setVisTransform(std, st);
  }
  
  void stopSpinning() {
    say("Spinning Stopped at angle: " + spinAngle);
    isSpinning = false;
    resetVisTransform(pd1);
    resetVisTransform(pd2);
    resetVisTransform(sd1);
    resetVisTransform(std);
  }

  void updateDeltaAngle(int x) {
    deltaAngle = 360. / (x * fps);
    spr = x;
  }
  
  void updateSpinAngle() {
    if (!isSpinning) {
        return;
    }
    spinAngle += deltaAngle;
    say("Spin Angle Updated: " + spinAngle, debug);
    st.getRotationAngleQuantity().setValue(spinAngle);
  }
  
  AnimationDirector ad = null;
  PartDisplayer pd1, pd2;
  SimpleTransform st;
  ScalarDisplayer sd1;
  StreamDisplayer std;
  
  int fps = 36;                     //-- Frames per second
  int spr = 6;                      //-- Seconds per revolution when spinning
  int act1 = 2 * fps;               //-- First act: 2 seconds == 72 pictures (frames)
  int act2 = 2 * fps;               //-- Second act...
  int act3 = 1 * spr * fps;         //-- Third act... == 1 revolution == 6 seconds
  int act4 = 1 * spr * fps;         //-- Fourth act...
  int act5 = 1 * fps;
  
  int acts12 = act1 + act2;
  int acts13 = acts12 + act3;       //-- Acts 1 to 3
  int acts14 = acts13 + act4;       //-- Acts 1 to 4, etc...
  int acts15 = acts14 + act5;

  boolean debug = false;
  boolean isSpinning = false;
  double deltaAngle;
  double spinAngle, spinAngleOld;
  
}
