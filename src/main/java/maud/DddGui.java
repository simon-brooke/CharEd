/*
 Copyright (c) 2017, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package maud;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeVersion;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.RadioButtonStateChangedEvent;
import de.lessvoid.nifty.controls.Slider;
import de.lessvoid.nifty.controls.SliderChangedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Misc;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.debug.DebugVersion;
import jme3utilities.math.MyMath;
import jme3utilities.nifty.DialogController;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.LibraryVersion;
import jme3utilities.nifty.WindowController;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.UiVersion;

/**
 * The screen controller for the GUI portion of Maud's "3D View" screen. The GUI
 * includes a menu bar, numerous tool windows, and a status bar.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class DddGui extends GuiScreenController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            DddGui.class.getName());
    /**
     * name of signal that rotates the model counter-clockwise around +Y
     */
    final private static String modelCCWSignalName = "modelLeft";
    /**
     * name of signal that rotates the model clockwise around +Y
     */
    final private static String modelCWSignalName = "modelRight";
    // *************************************************************************
    // fields

    /**
     * input mode for this screen
     */
    final DddInputMode inputMode = new DddInputMode();
    /**
     * menus for this screen
     */
    final DddMenus menus = new DddMenus();
    /*
     * controllers for tool windows
     */
    final public AnimationTool animation = new AnimationTool(this);
    final AxesTool axes = new AxesTool(this);
    final BoneTool bone = new BoneTool(this);
    final BoneAngleTool boneAngle = new BoneAngleTool(this);
    final BoneOffsetTool boneOffset = new BoneOffsetTool(this);
    final BoneScaleTool boneScale = new BoneScaleTool(this);
    final CameraTool camera = new CameraTool(this);
    final CullHintTool cullHint = new CullHintTool(this);
    final CursorTool cursor = new CursorTool(this);
    final KeyframeTool keyframe = new KeyframeTool(this);
    final ModelTool model = new ModelTool(this);
    final RenderTool render = new RenderTool(this);
    final RetargetTool retarget = new RetargetTool(this);
    final ShadowModeTool shadowMode = new ShadowModeTool(this);
    final SkeletonTool skeleton = new SkeletonTool(this);
    final SpatialTool spatial = new SpatialTool(this);
    final SkyTool sky = new SkyTool(this);
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized, disabled display that will be enabled
     * during initialization.
     */
    DddGui() {
        super("3D View", "Interface/Nifty/huds/3DView.xml", true);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Handle an "About Maud" action.
     */
    void aboutMaud() {
        String niftyVersion = nifty.getVersion();
        String text = "Maud, by Stephen Gold\n\nYou are c"
                + "urrently using Maud, a jMonkeyEngine application for edit"
                + "ing animated models.\n\nThe version you are using includes "
                + "the following libraries:";
        text += String.format("\n   jme3-core version %s",
                MyString.quote(JmeVersion.FULL_NAME));
        text += String.format("\n   nifty version %s",
                MyString.quote(niftyVersion));
        text += String.format("\n   SkyControl version %s",
                MyString.quote(Misc.getVersionShort()));
        text += String.format("\n   jme3-utilities-debug version %s",
                MyString.quote(DebugVersion.getVersionShort()));
        text += String.format("\n   jme3-utilities-ui version %s",
                MyString.quote(UiVersion.getVersionShort()));
        text += String.format("\n   jme3-utilities-nifty version %s\n\n",
                MyString.quote(LibraryVersion.getVersionShort()));
        closeAllPopups();
        showInfoDialog("About Maud", text);
    }

    /**
     * Display the "copy animation" dialog.
     */
    void copyAnimation() {
        closeAllPopups();
        String fromName = Maud.model.animation.getName();
        DialogController controller = new AnimationNameDialog("Copy");
        showTextEntryDialog("Enter name for copied animation:",
                fromName, "", DddInputMode.copyAnimationPrefix,
                controller);
    }

    /**
     * Handle a "copy pose" action with arguments.
     *
     * @param argument action argument (not null)
     */
    void copyAnimation(String argument) {
        Maud.model.animation.newCopy(argument);
        Maud.model.animation.load(argument);
    }

    /**
     * Handle the "load asset path" menu item.
     */
    void loadModelAsset() {
        Maud.gui.closeAllPopups();
        String basePath = Maud.model.cgm.getAssetPath();
        String extension = Maud.model.cgm.getExtension();
        String assetPath = String.format("%s.%s", basePath, extension);
        List<String> modelExts = new ArrayList<>(4);
        modelExts.add(".blend");
        modelExts.add(".j3o");
        modelExts.add(".mesh.xml");
        AssetDialog controller = new AssetDialog("Load", modelExts,
                assetManager);
        Maud.gui.showTextEntryDialog("Enter asset path for model:",
                assetPath, "", DddInputMode.loadModelAssetPrefix,
                controller);
    }

    /**
     * Display the "new pose" dialog.
     */
    void newPose() {
        closeAllPopups();
        DialogController controller = new AnimationNameDialog("Create");
        showTextEntryDialog("Enter a name for the new animation:", "pose", "",
                DddInputMode.newPosePrefix, controller);
    }

    /**
     * Handle a "new pose" action with arguments.
     *
     * @param argument action argument (not null)
     */
    void newPose(String argument) {
        Maud.model.animation.newPose(argument);
        Maud.model.animation.load(argument);
    }

    /**
     * Callback that Nifty invokes after a check box changes.
     *
     * @param boxId Nifty element id of the check box (not null)
     * @param event details of the event (not null)
     */
    @NiftyEventSubscriber(pattern = ".*CheckBox")
    public void onCheckBoxChanged(final String boxId,
            final CheckBoxStateChangedEvent event) {
        Validate.nonNull(boxId, "check box id");
        Validate.nonNull(event, "event");

        if (!hasStarted()) {
            return;
        }

        boolean isChecked = event.isChecked();

        switch (boxId) {
            case "3DCursorCheckBox":
                Maud.model.cursor.setVisible(isChecked);
                break;
            case "axesAutoCheckBox":
                Maud.model.axes.setAutoSizing(isChecked);
                break;
            case "axesDepthTestCheckBox":
                Maud.model.axes.setDepthTestFlag(isChecked);
                break;
            case "loopCheckBox":
                Maud.model.animation.setContinue(isChecked);
                break;
            case "pongCheckBox":
                Maud.model.animation.setReverse(isChecked);
                break;
            case "shadowsCheckBox":
                Maud.model.misc.setShadowsRendered(isChecked);
                break;
            case "skeletonCheckBox":
                Maud.model.skeleton.setVisible(isChecked);
                break;
            case "skyCheckBox":
                Maud.model.misc.setSkyRendered(isChecked);
                break;
            default:
                logger.log(Level.WARNING, "unknown check box with id={0}",
                        MyString.quote(boxId));
        }
    }

    /**
     * Callback that Nifty invokes after a radio button changes.
     *
     * @param buttonId Nifty element id of the radio button (not null)
     * @param event details of the event (not null)
     */
    @NiftyEventSubscriber(pattern = ".*RadioButton")
    public void onRadioButtonChanged(final String buttonId,
            final RadioButtonStateChangedEvent event) {
        Validate.nonNull(buttonId, "button id");
        Validate.nonNull(event, "event");

        if (!hasStarted() || !event.isSelected()) {
            return;
        }

        switch (buttonId) {
            case "boneAxesRadioButton":
                Maud.model.axes.setMode("bone");
                break;
            case "modelAxesRadioButton":
                Maud.model.axes.setMode("model");
                break;
            case "worldAxesRadioButton":
                Maud.model.axes.setMode("world");
                break;
            case "hideAxesRadioButton":
                Maud.model.axes.setMode("none");
                break;

            case "flyingRadioButton":
                Maud.model.camera.setMode("fly");
                break;
            case "orbitingRadioButton":
                Maud.model.camera.setMode("orbit");
                break;

            case "cullInheritRadioButton":
                Maud.model.cgm.setHint(Spatial.CullHint.Inherit);
                break;
            case "cullDynamicRadioButton":
                Maud.model.cgm.setHint(Spatial.CullHint.Dynamic);
                break;
            case "cullAlwaysRadioButton":
                Maud.model.cgm.setHint(Spatial.CullHint.Always);
                break;
            case "cullNeverRadioButton":
                Maud.model.cgm.setHint(Spatial.CullHint.Never);
                break;

            case "shadowOffRadioButton":
                Maud.model.cgm.setMode(RenderQueue.ShadowMode.Off);
                break;
            case "shadowCastRadioButton":
                Maud.model.cgm.setMode(RenderQueue.ShadowMode.Cast);
                break;
            case "shadowReceiveRadioButton":
                Maud.model.cgm.setMode(RenderQueue.ShadowMode.Receive);
                break;
            case "shadowCastAndReceiveRadioButton":
                Maud.model.cgm.setMode(RenderQueue.ShadowMode.CastAndReceive);
                break;
            case "shadowInheritRadioButton":
                Maud.model.cgm.setMode(RenderQueue.ShadowMode.Inherit);
                break;

            default:
                logger.log(Level.WARNING, "unknown radio button with id={0}",
                        MyString.quote(buttonId));
        }
    }

    /**
     * Callback that Nifty invokes after a slider changes.
     *
     * @param sliderId Nifty element id of the slider (not null)
     * @param event details of the event (not null, ignored)
     */
    @NiftyEventSubscriber(pattern = ".*Slider")
    public void onSliderChanged(final String sliderId,
            final SliderChangedEvent event) {
        Validate.nonNull(sliderId, "slider id");
        Validate.nonNull(event, "event");

        if (!hasStarted()) {
            return;
        }

        switch (sliderId) {
            case "speedSlider":
            case "timeSlider":
                animation.onSliderChanged();
                break;

            case "axesLengthSlider":
            case "axesLineWidthSlider":
                axes.onSliderChanged();
                break;

            case "xAngSlider":
            case "yAngSlider":
            case "zAngSlider":
                boneAngle.onSliderChanged();
                break;

            case "offMasterSlider":
            case "xOffSlider":
            case "yOffSlider":
            case "zOffSlider":
                boneOffset.onSliderChanged();
                break;

            case "xScaSlider":
            case "yScaSlider":
            case "zScaSlider":
                boneScale.onSliderChanged();
                break;

            case "cursorRSlider":
            case "cursorGSlider":
            case "cursorBSlider":
                cursor.onSliderChanged();
                break;

            case "skeletonLineWidthSlider":
            case "skeletonPointSizeSlider":
            case "skeRSlider":
            case "skeGSlider":
            case "skeBSlider":
                skeleton.onSliderChanged();
                break;

            default:
                logger.log(Level.WARNING, "unknown slider with id={0}",
                        MyString.quote(sliderId));
        }
    }

    /**
     * Read a bank of 3 sliders that control a rotation.
     *
     * @param prefix unique id prefix of the bank (not null)
     * @return rotation indicated by the sliders (new instance)
     */
    Quaternion readAngleBank(String prefix) {
        assert prefix != null;

        float x = readSlider("x" + prefix);
        float y = readSlider("y" + prefix);
        float z = readSlider("z" + prefix);
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(x, y, z);

        return rotation;
    }

    /**
     * Read a bank of 3 sliders that control a color.
     *
     * @param prefix unique id prefix of the bank (not null)
     * @return color indicated by the sliders (new instance)
     */
    ColorRGBA readColorBank(String prefix) {
        assert prefix != null;

        float r = readSlider(prefix + "R");
        float g = readSlider(prefix + "G");
        float b = readSlider(prefix + "B");
        ColorRGBA color = new ColorRGBA(r, g, b, 1f);

        return color;
    }

    /**
     * Read a bank of 3 sliders that control a vector.
     *
     * @param infix unique id infix of the bank (not null)
     * @return vector indicated by the sliders (new instance)
     */
    Vector3f readVectorBank(String infix) {
        assert infix != null;

        float x = readSlider("x" + infix);
        float y = readSlider("y" + infix);
        float z = readSlider("z" + infix);
        Vector3f vector = new Vector3f(x, y, z);

        return vector;
    }

    /**
     * Display the "rename animation" dialog.
     */
    void renameAnimation() {
        if (!Maud.model.animation.isBindPoseLoaded()) {
            closeAllPopups();
            String oldName = Maud.model.animation.getName();
            DialogController controller = new AnimationNameDialog("Rename");
            showTextEntryDialog("Enter new name for the animation:", oldName,
                    "", DddInputMode.renameAnimationPrefix, controller);
        }
    }

    /**
     * Display the "rename bone" dialog.
     */
    void renameBone() {
        if (Maud.model.bone.isBoneSelected()) {
            closeAllPopups();
            String oldName = Maud.model.bone.getName();
            DialogController controller = new BoneRenameDialog("Rename");
            showTextEntryDialog("Enter new name for the bone:", oldName, "",
                    DddInputMode.renameBonePrefix, controller);
        }
    }

    /**
     * Handle a "retarget animation" action without arguments.
     */
    void retargetAnimation() {
        closeAllPopups();
        String oldName = Maud.model.retarget.getTargetAnimationName();
        if (oldName == null) {
            oldName = "";
        }
        DialogController controller = new AnimationNameDialog("Retarget");
        showTextEntryDialog("Enter a name for the new animation:", oldName, "",
                DddInputMode.retargetAnimationPrefix, controller);
    }

    /**
     * Handle a "retarget animation" action with arguments.
     *
     * @param argument action argument (not null)
     */
    void retargetAnimation(String argument) {
        Maud.model.retarget.setTargetAnimationName(argument);
        Maud.model.retarget.retargetAndAdd();
        Maud.model.animation.load(argument);
    }

    /**
     * Handle a "select rma" action with no argument.
     */
    void selectRetargetMapAsset() {
        closeAllPopups();
        String assetPath = Maud.model.retarget.getMappingAssetPath();
        List<String> modelExts = new ArrayList<>(1);
        modelExts.add(".j3o");
        AssetDialog controller = new AssetDialog("Select", modelExts,
                assetManager);
        showTextEntryDialog("Enter asset path for skeleton mapping:", assetPath,
                "", DddInputMode.selectRetargetMapAssetPrefix, controller);
    }

    /**
     * Handle a "select rsca" action with no argument.
     */
    void selectRetargetSourceCgmAsset() {
        closeAllPopups();
        String assetPath = Maud.model.retarget.getSourceCgmAssetPath();
        List<String> modelExts = new ArrayList<>(4);
        modelExts.add(".blend");
        modelExts.add(".j3o");
        modelExts.add(".mesh.xml");
        AssetDialog controller = new AssetDialog("Select", modelExts,
                assetManager);
        showTextEntryDialog("Enter asset path for source model:", assetPath, "",
                DddInputMode.selectRetargetSourceCgmAssetPrefix, controller);
    }

    /**
     * Handle a "select spatialChild" action with arguments.
     *
     * @param argument action argument (not null)
     */
    void selectSpatialChild(String argument) {
        String[] words = argument.split(" ");
        String firstWord = words[0];
        assert firstWord.startsWith("#") : firstWord;
        String numberText = firstWord.substring(1);
        int number = Integer.parseInt(numberText);
        Maud.model.spatial.selectChild(number - 1);
    }

    /**
     * Handle a "select tool" action.
     *
     * @param toolName which tool to select (not null)
     * @return true if the action is handled, otherwise false
     */
    boolean selectTool(String toolName) {
        WindowController controller = null;
        switch (toolName) {
            case "animation":
                controller = animation;
                break;
            case "axes":
                controller = axes;
                break;
            case "bone":
                controller = bone;
                break;
            case "boneAngle":
                controller = boneAngle;
                break;
            case "boneOffset":
                controller = boneOffset;
                break;
            case "boneScale":
                controller = boneScale;
                break;
            case "camera":
                controller = camera;
                break;
            case "cullHint":
                controller = cullHint;
                break;
            case "cursor":
                controller = cursor;
                break;
            case "model":
                controller = model;
                break;
            case "render":
                controller = render;
                break;
            case "retarget":
                controller = retarget;
                break;
            case "shadowMode":
                controller = shadowMode;
                break;
            case "skeleton":
                controller = skeleton;
                break;
            case "spatial":
                controller = spatial;
                break;
            case "sky":
                controller = sky;
                break;
        }
        if (controller == null) {
            return false;
        } else {
            controller.select();
            return true;
        }
    }

    /**
     * Set a bank of 3 sliders that control a color.
     *
     * @param prefix unique id prefix of the bank (not null)
     * @return color indicated by the sliders (new instance)
     */
    void setColorBank(String prefix, ColorRGBA color) {
        assert prefix != null;

        Slider slider = Maud.gui.getSlider(prefix + "R");
        slider.setValue(color.r);
        Maud.gui.updateSliderStatus(prefix + "R", color.r, "");

        slider = Maud.gui.getSlider(prefix + "G");
        slider.setValue(color.g);
        Maud.gui.updateSliderStatus(prefix + "G", color.g, "");

        slider = Maud.gui.getSlider(prefix + "B");
        slider.setValue(color.b);
        Maud.gui.updateSliderStatus(prefix + "B", color.b, "");
    }

    /**
     * Update the status bar.
     *
     * @param message what to display (not null)
     */
    void setStatus(String message) {
        assert message != null;
        setStatusText("messageLabel", message);
    }

    /**
     * Update a bank of 3 sliders that control a color.
     *
     * @param prefix unique id prefix of the bank (not null)
     * @return color indicated by the sliders (new instance)
     */
    ColorRGBA updateColorBank(String prefix) {
        assert prefix != null;

        float r = updateSlider(prefix + "R", "");
        float g = updateSlider(prefix + "G", "");
        float b = updateSlider(prefix + "B", "");
        ColorRGBA color = new ColorRGBA(r, g, b, 1f);

        return color;
    }
    // *************************************************************************
    // AppState methods

    /**
     * Initialize this controller prior to its 1st update.
     *
     * @param stateManager (not null)
     * @param application application which owns this screen (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        if (isEnabled()) {
            throw new IllegalStateException("shouldn't be enabled yet");
        }

        InputMode.getActiveMode().setEnabled(false);
        inputMode.setEnabled(true);
        inputMode.influence(this);
        setListener(inputMode);
        super.initialize(stateManager, application);

        Maud.model.cgm.loadModelNamed("Elephant");
    }

    /**
     * Callback to update this state prior to rendering. (Invoked once per
     * render pass.)
     *
     * @param tpf time interval between render passes (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        camera.updateCamera();
        cursor.updateCursor();
        render.updateShadowFilter();
        skeleton.updateSdc();
        sky.updateSkyControl();
        /*
         * Update animation even if the animation tool is disabled.
         */
        if (Maud.model.animation.isMoving()) {
            updateTrackTime(tpf);
        }
        Maud.viewState.updatePose();
        axes.updateAxesControl();
        /*
         * Rotate the view's CG model around the Y-axis.
         */
        if (signals.test(modelCCWSignalName)) {
            Maud.viewState.rotateY(tpf);
        }
        if (signals.test(modelCWSignalName)) {
            Maud.viewState.rotateY(-tpf);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Update the track time.
     *
     * @param tpf time interval between render passes (in seconds, &ge;0)
     */
    private void updateTrackTime(float tpf) {
        assert Maud.model.animation.isMoving();

        float speed = Maud.model.animation.getSpeed();
        float time = Maud.model.animation.getTime();
        time += speed * tpf;

        boolean cont = Maud.model.animation.willContinue();
        boolean reverse = Maud.model.animation.willReverse();
        float duration = Maud.model.animation.getDuration();
        if (cont && !reverse) {
            time = MyMath.modulo(time, duration); // wrap
        } else {
            float freeTime = time;
            time = FastMath.clamp(time, 0f, duration);
            if (time != freeTime) { // reached a limit
                if (reverse) {
                    Maud.model.animation.setSpeed(-speed); // pong
                } else {
                    time = duration - time; // wrap
                }
                Maud.model.animation.setPaused(!cont);
            }
        }
        Maud.model.animation.setTime(time);
    }
}
