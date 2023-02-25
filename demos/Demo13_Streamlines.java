import macroutils.MacroUtils;
import macroutils.StaticDeclarations;
import macroutils.UserDeclarations;
import macroutils.templates.TemplatePost;
import star.common.Simulation;
import star.common.StarMacro;
import star.vis.AnimationDirector;
import star.vis.DisplayerVisibilityOverride;
import star.vis.PartColorMode;
import star.vis.PartDisplayer;
import star.vis.ScalarDisplayer;
import star.vis.SimpleTransform;
import star.vis.StreamAnimationSettings;
import star.vis.StreamDisplayer;
import star.vis.StreamDisplayerAnimationMode;

/**
 * Turbulent flow with Ideal Gas. This Demo shows how to do more advanced post processing using
 * macros in STAR-CCM+.
 *
 * @since Macro Utils v3.0.
 * @author Fabio Kasper
 */
public class Demo13_Streamlines extends StarMacro {

    private AnimationDirector ad;
    private final boolean debug = false;
    private double deltaAngle;
    private final int fps = 36;                //-- Frames per second
    private final int act5 = 1 * fps;
    private final int act2 = 2 * fps;          //-- Second act...
    private final int act1 = 2 * fps;          //-- First act: 2 seconds == 72 pictures (frames)
    private final int acts12 = act1 + act2;
    private boolean isSpinning = false;
    private MacroUtils mu;
    private PartDisplayer pd1, pd2;
    private ScalarDisplayer scd;
    private Simulation sim;

    private double spinAngle;
    private double spinAngleOld;
    private int spr = 6;                       //-- Seconds per revolution when spinning
    private final int act4 = 1 * spr * fps;    //-- Fourth act...
    private final int act3 = 1 * spr * fps;    //-- Third act... == 1 revolution == 6 seconds
    private final int acts13 = acts12 + act3;  //-- Acts 1 to 3
    private final int acts14 = acts13 + act4;  //-- Acts 1 to 4, etc...
    private final int acts15 = acts14 + act5;
    private SimpleTransform st;
    private StreamDisplayer std;
    private UserDeclarations ud;

    @Override
    public void execute() {

        initMacro();

        pre();

        solve();

        post();

        mu.saveSim();

        mu.io.write.all(ud.simTitle);

    }

    private void initMacro() {
        sim = getActiveSimulation();
        mu = new MacroUtils(sim);
        ud = mu.userDeclarations;
        ud.simTitle = "Demo13_Streamlines";
        ud.vv1 = mu.io.read.cameraView("cam1|2.234138e-03,-5.793002e-04,3.969814e-02"
                + "|2.234138e-03,-5.793002e-04,3.269352e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|7.498392e-02|0", true);
        ud.vv2 = mu.io.read.cameraView("cam2|2.873504e-04,3.784037e-03,3.752940e-02"
                + "|2.873504e-04,3.784037e-03,2.115639e-01|0.000000e+00,1.000000e+00,0.000000e+00"
                + "|4.543212e-02|0", true);
        ud.defCamView = ud.vv1;
    }

    private void post() {
        ud.namedObjects.addAll(mu.get.boundaries.all(true));
        //--
        //-- One can make it all in a single Scenes.
        ud.ff = mu.get.objects.fieldFunction(StaticDeclarations.Vars.P.getVar(), true);
        ud.scene = mu.add.scene.scalar(ud.namedObjects, ud.ff, ud.unit_Pa, true);
        ud.scene.getAxes().setAxesVisible(false);

        scd = (ScalarDisplayer) mu.get.scenes.displayerByREGEX(ud.scene, "Scalar", true);
        scd.setOpacity(0);
        scd.getLegend().setVisible(false);

        pd1 = mu.add.scene.displayer_Geometry(ud.scene);
        pd1.setOpacity(1);
        pd1.setColorMode(PartColorMode.DEFAULT);

        pd2 = (PartDisplayer) mu.add.scene.displayer_Geometry(ud.scene);
        pd2.copyProperties(pd1);
        pd2.setMesh(true);
        pd2.setPresentationName("Mesh");
        pd2.setOpacity(0);

        ud.namedObjects2.add(mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true));
        ud.namedObjects2.addAll(mu.get.regions.all(true));
        ud.postStreamlinesTubesWidth = 0.0005;
        std = mu.add.scene.displayer_Streamline(ud.scene, ud.namedObjects2, true);
        mu.templates.prettify.all();
        std.getScalarDisplayQuantity().setRange(new double[]{ 0, 3.0 });
        std.getAnimationManager().setMode(StreamDisplayerAnimationMode.TRACER);
        std.setLegendPosition(scd.getLegend().getPositionCoordinate());
        std.setVisibilityOverrideMode(DisplayerVisibilityOverride.HIDE_ALL_PARTS);
        //--
        //-- Animated Part and Scalar Displayers
        TemplatePostOverride tpo = new TemplatePostOverride(mu);
        tpo.flyOver(ud.scene, null, null, act1);
        tpo.flyOver(ud.scene, null, null, act2);
        updateDeltaAngle(spr);
        startSpinning();
        tpo.flyOver(ud.scene, null, null, act3);
        tpo.flyOver(ud.scene, null, null, act4);
        stopSpinning();
        tpo.flyOver(ud.scene, null, null, act5);
        //--
        //-- Animated Streamline Displayer
        std.setVisibilityOverrideMode(DisplayerVisibilityOverride.SHOW_ALL_PARTS);
        //-- This time the spin will take 3x longer.
        updateDeltaAngle(3 * spr);
        int preSpin = 8 * fps;
        //-- spr is updated along with delta Angle.
        int frames = preSpin + 1 * spr * fps;
        ad = ud.scene.getAnimationDirector();
        ad.setFramesPerSecond(fps);
        StreamAnimationSettings sas = std.getAnimationManager().getSettings();
        //-- Adjust the coefficient to get the speed right. Smaller is faster.
        sas.setCycleTime(0.75 * frames / fps);
        sas.setTracerDelay(1.0);
        sas.setTailTime(0.2);
        sas.setHeadSize(0.0001);
        ad.setIsPlaying(true);
        ad.start();
        ad.pause();
        tpo.flyOver(ud.scene, null, null, preSpin);
        startSpinning();
        tpo.flyOver(ud.scene, null, null, frames - preSpin);
        ad.stop(true);
    }

    private void pre() {
        if (mu.check.has.volumeMesh()) {
            return;
        }
        double r1 = 40.;
        double r2 = 20.;
        double l1 = 4 * r1;
        ud.cadPrt = mu.add.geometry.cylinder3DCAD(r1, l1, new double[]{ -0.5 * l1, 0, 0 },
                ud.unit_mm, StaticDeclarations.Axis.X);
        ud.cadPrt.setPresentationName("CylMain");
        ud.geometryParts.add(ud.cadPrt);
        ud.cadPrt = mu.add.geometry.cylinder3DCAD(r2, 2 * r1,
                new double[]{ -0.45 * l1 + r2, -2 * r1, 0 },
                ud.unit_mm, StaticDeclarations.Axis.Y);
        ud.cadPrt.setPresentationName("CylIn");
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "y0", true).setPresentationName(ud.bcInlet);
        ud.geometryParts.add(ud.cadPrt);
        ud.cadPrt = mu.add.geometry.cylinder3DCAD(r2, 2 * r1,
                new double[]{ 0.45 * l1 - r2, 0, 0 },
                ud.unit_mm, StaticDeclarations.Axis.Y);
        ud.cadPrt.setPresentationName("CylOut");
        mu.get.partSurfaces.byREGEX(ud.cadPrt, "y1", true).setPresentationName(ud.bcOutlet);
        ud.geometryParts.add(ud.cadPrt);
        //-- Unite bodies
        ud.mshOpPrt = mu.add.meshOperation.unite(ud.geometryParts);
        ud.geometryParts2.add(ud.mshOpPrt);
        ud.region = mu.add.region.fromPart(ud.mshOpPrt,
                StaticDeclarations.BoundaryMode.ONE_FOR_EACH_PART_SURFACE,
                StaticDeclarations.InterfaceMode.CONTACT,
                true);
        //--
        ud.mshBaseSize = r2 / 5;
        ud.prismsLayers = 1;
        ud.prismsRelSizeHeight = 40;
        ud.prismsStretching = 1.2;
        ud.autoMshOp = mu.add.meshOperation.automatedMesh(ud.geometryParts2,
                StaticDeclarations.Meshers.SURFACE_REMESHER,
                StaticDeclarations.Meshers.POLY_MESHER,
                StaticDeclarations.Meshers.PRISM_LAYER_MESHER);
        ud.autoMshOp.setPresentationName("My Mesh");
        //--
        ud.physCont = mu.add.physicsContinua.generic(StaticDeclarations.Space.THREE_DIMENSIONAL,
                StaticDeclarations.Time.STEADY, StaticDeclarations.Material.GAS,
                StaticDeclarations.Solver.SEGREGATED, StaticDeclarations.Density.IDEAL_GAS,
                StaticDeclarations.Energy.THERMAL, StaticDeclarations.Viscous.RKE_HIGH_YPLUS);
        //--
        mu.set.boundary.asVelocityInlet(mu.get.boundaries.byREGEX(".*" + ud.bcInlet, true),
                3.0, 20.0, 0.05, 10.0);
        mu.set.boundary.asPressureOutlet(mu.get.boundaries.byREGEX(".*" + ud.bcOutlet, true),
                0.0, 21.0, 0.05, 10.0);
        //--
        mu.update.volumeMesh();
        ud.scene = mu.add.scene.mesh();
    }

    private void solve() {
        if (mu.check.has.solution()) {
            return;
        }
        ud.maxIter = 200;
        mu.set.solver.aggressiveSettings();
        mu.run();
        mu.saveSim(ud.simTitle + "_SS");
    }

    private void startSpinning() {
        mu.io.say.msg("Spinning Started...");
        isSpinning = true;
        spinAngle = 0.;
        spinAngleOld = 0.;
        String trName = "Spinner";
        if (sim.getTransformManager().has(trName)) {
            st = (SimpleTransform) mu.get.objects.transform(trName, true);
        } else {
            st = mu.add.tools.transform_Simple(StaticDeclarations.COORD0,
                    new double[]{ 0, -1, 0 }, 0, null, null);
            st.setPresentationName(trName);
        }
        mu.set.scene.transform(pd1, st, true);
        mu.set.scene.transform(pd2, st, true);
        mu.set.scene.transform(scd, st, true);
        mu.set.scene.transform(std, st, true);
    }

    private void stopSpinning() {
        mu.io.say.msg("Spinning Stopped at angle: " + spinAngle);
        isSpinning = false;
        mu.reset.transform(pd1);
        mu.reset.transform(pd2);
        mu.reset.transform(scd);
        mu.reset.transform(std);
    }

    private void updateDeltaAngle(int x) {
        deltaAngle = 360. / (x * fps);
        spr = x;
    }

    private void updateSpinAngle() {
        if (!isSpinning) {
            return;
        }
        spinAngle += deltaAngle;
        mu.io.say.msg("Spin Angle Updated: " + spinAngle, debug);
        st.getRotationAngleQuantity().setValue(spinAngle);
    }

    class TemplatePostOverride extends TemplatePost {

        public TemplatePostOverride(MacroUtils m) {
            super(m);
            this.updateInstances();
        }

        @Override
        public void flyOver_prePrintPicture() {
            super.flyOver_prePrintPicture();
            double fadeIn = 1, fadeOut = 1;
            double[] xx = {}, yy = {};

            if (getCurrentFrame() >= act1 && getCurrentFrame() <= acts12) {
                if (getCurrentFrame() == act1) {
                    mu.io.say.loud("Act2 -- Fade Out & In events");
                }
                //-- Transition in half second
                xx = new double[]{ act1, act1 + 0.5 * fps };
                yy = new double[]{ 1.0, 0.0 };
                fadeOut = mu.get.info.linearRegression(xx, yy, getCurrentFrame(), true, false);
                pd1.setOpacity(fadeOut);
                //--
                xx = new double[]{ xx[1] - 1, xx[1] + 0.5 * fps - 1 };
                yy = new double[]{ 0.0, 1.0 };
                fadeIn = mu.get.info.linearRegression(xx, yy, getCurrentFrame(), true, false);
                pd2.setOpacity(fadeIn);
            }

            if (getCurrentFrame() >= acts12 && getCurrentFrame() <= acts13) {
                if (getCurrentFrame() == acts12) {
                    mu.io.say.loud("Act3 -- Spinning");
                }
            }

            if (getCurrentFrame() >= acts13 && getCurrentFrame() <= acts14) {
                if (getCurrentFrame() == acts13) {
                    mu.io.say.loud("Act4 -- Spinning & Change to Scalar Scene");
                }
                //-- Transition in half second
                xx = new double[]{ acts13, acts13 + 0.5 * fps };
                yy = new double[]{ 1.0, 0.0 };
                fadeOut = mu.get.info.linearRegression(xx, yy, getCurrentFrame(), true, false);
                pd2.setOpacity(fadeOut);
                //--
                xx = new double[]{ xx[1] - 2, xx[1] + 0.5 * fps - 2 };
                yy = new double[]{ 0.0, 1.0 };
                fadeIn = mu.get.info.linearRegression(xx, yy, getCurrentFrame(), true, false);
                if (fadeIn > 0.8) {
                    scd.getLegend().setVisible(true);
                }
                scd.setOpacity(fadeIn);
            }

            if (getCurrentFrame() >= acts14 && getCurrentFrame() <= acts15) {
                if (getCurrentFrame() == acts14) {
                    mu.io.say.loud("Act5 -- Stop Spinning & Change back to Geometry");
                }
                //-- Transition in half second
                xx = new double[]{ acts15 - fps, acts15 - 0.5 * fps };
                yy = new double[]{ 1.0, 0.0 };
                fadeOut = mu.get.info.linearRegression(xx, yy, getCurrentFrame(), true, false);
                scd.setOpacity(fadeOut);
                //--
                if (fadeOut >= 0.6) {
                    fadeIn = 0.0;
                } else {
                    scd.getLegend().setVisible(false);
                    fadeIn = 0.1;
                }
                pd1.setOpacity(fadeIn);
            }

            if (getCurrentFrame() >= acts15) {
                ad.step(1);
            }

            updateSpinAngle();

        }

        @Override
        public void flyOver_postPrintPicture() {
            super.flyOver_postPrintPicture();
            spinAngleOld += deltaAngle * getNumberOfFrames();
            spinAngle = spinAngleOld;
        }

    }

}
