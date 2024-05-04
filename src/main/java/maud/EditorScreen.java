/*
 Copyright (c) 2017-2023, Stephen Gold
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
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.MyAsset;
import jme3utilities.MyCamera;
import jme3utilities.MySpatial;
import jme3utilities.debug.PerformanceAppState;
import jme3utilities.math.MyMath;
import jme3utilities.mesh.RectangleMesh;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;
import jme3utilities.ui.InputMode;
import maud.action.EditorInputMode;
import maud.mesh.Lozenge;
import maud.model.EditorModel;
import maud.model.cgm.Cgm;
import maud.model.cgm.LoadedAnimation;
import maud.model.cgm.PlayOptions;
import maud.model.cgm.Pov;
import maud.model.option.MiscOptions;
import maud.model.option.PerformanceMode;
import maud.model.option.scene.AxesOptions;
import maud.model.option.scene.CameraOptions;
import maud.tool.EditorTools;
import maud.view.Drag;
import maud.view.EditorView;
import maud.view.Selection;
import maud.view.ViewType;
import maud.view.scene.CgmTransform;
import maud.view.scene.SceneDrag;
import maud.view.scene.SceneView;
import org.lwjgl.input.Mouse;

/**
 * The screen controller for Maud's editor screen. The GUI includes a menu bar,
 * numerous tool windows, a drag handle, and a status bar.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class EditorScreen extends GuiScreenController {
    // *************************************************************************
    // constants and loggers

    /**
     * color of the boundary's drag handle
     */
    final private static ColorRGBA dragHandleColor
            = new ColorRGBA(0f, 0.6f, 0f, 1f);
    /**
     * squared distance limit for most selections (in pixels squared)
     */
    final private static float maxDSquared = 1600f;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EditorScreen.class.getName());
    /**
     * mesh to render the boundary's drag handle
     */
    final private static Mesh dragHandleMesh = new Lozenge(15f, 150f, 1f);
    /**
     * name of the spatial to render the boundary's drag handle
     */
    final private static String dragHandleName = "handle";
    /**
     * name of the signal that rotates the model counter-clockwise around +Y
     */
    final private static String modelCCWSignalName = "modelLeft";
    /**
     * name of the signal that rotates the model clockwise around +Y
     */
    final private static String modelCWSignalName = "modelRight";
    /**
     * name of the signal that controls POV movement
     */
    final private static String povSignalName = "moveCamera";
    // *************************************************************************
    // fields

    /**
     * input mode for this screen
     */
    final EditorInputMode inputMode = new EditorInputMode();
    /**
     * controllers for tool windows
     */
    final public EditorTools tools = new EditorTools(this);
    /**
     * previewer for the selected Texture
     */
    private Geometry texturePreviewer;
    /**
     * POV that's being dragged, or null for none
     */
    private Pov dragPov = null;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized, disabled screen that will not be enabled
     * during initialization.
     */
    EditorScreen() {
        super("editor", "Interface/Nifty/huds/editor.xml",
                InitialState.Disabled);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Activate the "Bind" screen.
     */
    public void goBindScreen() {
        closeAllPopups();
        texturePreviewer.removeFromParent();
        CharEd.bindScreen.activate(inputMode);
    }

    /**
     * Activate the "DisplaySettings" screen.
     */
    public void goDisplaySettingsScreen() {
        closeAllPopups();
        texturePreviewer.removeFromParent();
        CharEd.getDisplaySettingsScreen().activate();
    }

    /**
     * Select a loaded C-G model (source or target) based on the screen location
     * of the mouse pointer.
     *
     * @return a pre-existing instance, or null if none applies
     */
    public Cgm mouseCgm() {
        Cgm source = CharEd.getModel().getSource();
        Cgm target = CharEd.getModel().getTarget();
        ViewPort sScene = source.getSceneView().getViewPort();
        ViewPort sScore = source.getScoreView().getViewPort();
        ViewPort tScene = target.getSceneView().getViewPort();
        ViewPort tScore = target.getScoreView().getViewPort();

        Vector2f screenXY = inputManager.getCursorPosition();
        List<ViewPort> viewPorts
                = MyCamera.listViewPorts(renderManager, screenXY);
        Cgm cgm = null;
        for (ViewPort vp : viewPorts) {
            if (vp.isEnabled()) {
                if (vp == sScene || vp == sScore) {
                    cgm = source;
                    break;
                } else if (vp == tScene || vp == tScore) {
                    cgm = target;
                    break;
                }
            }
        }

        return cgm;
    }

    /**
     * Select a POV based on the screen location of the mouse pointer.
     *
     * @return the pre-existing instance, or null if none applies
     */
    public Pov mousePov() {
        Cgm source = CharEd.getModel().getSource();
        Cgm target = CharEd.getModel().getTarget();
        EditorView sScene = source.getSceneView();
        EditorView sScore = source.getScoreView();
        EditorView tScene = target.getSceneView();
        EditorView tScore = target.getScoreView();

        Vector2f screenXY = inputManager.getCursorPosition();
        List<ViewPort> viewPorts
                = MyCamera.listViewPorts(renderManager, screenXY);
        Pov result = null;
        for (ViewPort vp : viewPorts) {
            if (vp.isEnabled()) {
                if (vp == sScene.getViewPort()) {
                    result = source.getScenePov();
                    break;
                } else if (vp == sScore.getViewPort()) {
                    result = source.getScorePov();
                    break;
                } else if (vp == tScene.getViewPort()) {
                    result = target.getScenePov();
                    break;
                } else if (vp == tScore.getViewPort()) {
                    result = target.getScorePov();
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Select a view based on the screen location of the mouse pointer.
     *
     * @return the pre-existing instance, or null if none applies
     */
    public EditorView mouseView() {
        Cgm source = CharEd.getModel().getSource();
        Cgm target = CharEd.getModel().getTarget();
        EditorView sScene = source.getSceneView();
        EditorView sScore = source.getScoreView();
        EditorView tScene = target.getSceneView();
        EditorView tScore = target.getScoreView();

        Vector2f screenXY = inputManager.getCursorPosition();
        List<ViewPort> viewPorts
                = MyCamera.listViewPorts(renderManager, screenXY);
        EditorView result = null;
        for (ViewPort vp : viewPorts) {
            if (vp.isEnabled()) {
                if (vp == sScene.getViewPort()) {
                    result = sScene;
                    break;
                } else if (vp == sScore.getViewPort()) {
                    result = sScore;
                    break;
                } else if (vp == tScene.getViewPort()) {
                    result = tScene;
                    break;
                } else if (vp == tScore.getViewPort()) {
                    result = tScore;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Select a view type based on the screen location of the mouse pointer.
     *
     * @return an enum value, or null if neither applies
     */
    public ViewType mouseViewType() {
        ViewType result = null;
        EditorView view = mouseView();
        if (view != null) {
            result = view.getType();
        }

        return result;
    }

    /**
     * Select an axis, bone, boundary, gnomon, or keyframe based on the screen
     * coordinates of the mouse pointer.
     */
    public void pickAny() {
        Cgm mouseCgm = mouseCgm();
        EditorView mouseView = mouseView();
        if (mouseCgm != null && mouseView != null) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            Selection selection = new Selection(mouseXY, maxDSquared);
            mouseView.considerAxes(selection);
            mouseView.considerBones(selection);
            mouseView.considerBoundaries(selection);
            mouseView.considerGnomons(selection);
            mouseView.considerKeyframes(selection);
            mouseView.considerTracks(selection);
            selection.select();
        }
    }

    /**
     * Select a bone based on the screen coordinates of the mouse pointer.
     */
    public void pickBone() {
        Cgm mouseCgm = mouseCgm();
        EditorView mouseView = mouseView();
        if (mouseCgm != null && mouseView != null) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            Selection selection = new Selection(mouseXY, maxDSquared);
            mouseView.considerBones(selection);
            selection.select();
        }
    }

    /**
     * Select a gnomon based on the screen coordinates of the mouse pointer.
     */
    public void pickGnomon() {
        Cgm mouseCgm = mouseCgm();
        EditorView mouseView = mouseView();
        if (mouseCgm != null && mouseView != null) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            Selection selection = new Selection(mouseXY, Float.MAX_VALUE);
            mouseView.considerGnomons(selection);
            selection.select();
        }
    }

    /**
     * Select a keyframe based on the screen coordinates of the mouse pointer.
     */
    public void pickKeyframe() {
        Cgm mouseCgm = mouseCgm();
        EditorView mouseView = mouseView();
        if (mouseCgm != null && mouseView != null) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            Selection selection = new Selection(mouseXY, maxDSquared);
            mouseView.considerKeyframes(selection);
            selection.select();
        }
    }

    /**
     * Select a vertex based on the screen coordinates of the mouse pointer.
     */
    public void pickVertex() {
        Cgm mouseCgm = mouseCgm();
        EditorView mouseView = mouseView();
        if (mouseCgm != null && mouseView != null) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            Selection selection = new Selection(mouseXY, maxDSquared);
            mouseView.considerVertices(selection);
            selection.select();
        }
    }

    /**
     * Alter which Texture is previewed.
     *
     * @param newTexture the desired Texture, or null for none
     */
    public void setPreviewedTexture(Texture newTexture) {
        Texture oldTexture = getPreviewTexture();
        if (oldTexture != newTexture) {
            Material material = texturePreviewer.getMaterial();
            /*
             * Suppress warnings about the fact that the ColorMap parameter
             * doesn't specify a color space.
             */
            Logger matLogger = Logger.getLogger(Material.class.getName());
            Level oldLevel = matLogger.getLevel();
            matLogger.setLevel(Level.SEVERE);

            material.setTexture("ColorMap", newTexture);
            matLogger.setLevel(oldLevel);
        }
    }
    // *************************************************************************
    // GuiScreenController methods

    /**
     * A callback that Nifty invokes the first time the screen is displayed.
     *
     * @param nifty (not null)
     * @param screen (not null)
     */
    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);

        CharEd maud = CharEd.getApplication();
        maud.startup2();
    }

    /**
     * Initialize this controller prior to its first update.
     *
     * @param stateManager (not null)
     * @param application application that owns this screen (not null)
     */
    @Override
    public void initialize(
            AppStateManager stateManager, Application application) {
        if (isEnabled()) {
            throw new IllegalStateException("shouldn't be enabled yet");
        }

        InputMode.getActiveMode().setEnabled(false);
        inputMode.setEnabled(true);
        inputMode.influence(this);
        setListener(inputMode);

        super.initialize(stateManager, application);

        // Initialize the texture previewer to a square, unshaded and all white.
        RectangleMesh square = new RectangleMesh();
        texturePreviewer = new Geometry("texture preview", square);
        Material material = MyAsset.createUnshadedMaterial(assetManager);
        texturePreviewer.setMaterial(material);
    }

    /**
     * Callback to update the editor screen prior to rendering. (Invoked once
     * per frame.)
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        if (findTool("spatial") == null) {
            return;
        }

        // Check whether further initialization remains to be done.
        EditorModel model = CharEd.getModel();
        Cgm target = model.getTarget();
        if (!target.isLoaded()) {
            CharEd.getApplication().startup3();
        }

        MiscOptions options = model.getMisc();
        float x = options.submenuWarpX();
        float y = options.submenuWarpY();
        setSubmenuWarp(x, y);

        updatePerformanceMode();
        Drag.updateBoundary();
        updateBoundaryHandle();
        EditorViewPorts.update();
        updateBars();

        // Update the loaded animations.
        Cgm source = model.getSource();
        if (source.getAnimation().isMoving()) {
            updateTrackTime(source, tpf);
        }
        if (target.getAnimation().isMoving()) {
            updateTrackTime(target, tpf);
        } else if (target.getAnimation().isRetargetedPose()) {
            target.getPose().setToAnimation();
        }

        ViewType viewType = mouseViewType();
        if (viewType == ViewType.Scene) {
            /*
             * Based on the mouse-pointer location, select a loaded C-G model
             * to rotate around its Y-axis.
             */
            Cgm cgmToRotate = mouseCgm();
            if (cgmToRotate != null) {
                CgmTransform cgmTransform
                        = cgmToRotate.getSceneView().getTransform();
                if (getSignals().test(modelCCWSignalName)) {
                    cgmTransform.rotateY(tpf);
                }
                if (getSignals().test(modelCWSignalName)) {
                    cgmTransform.rotateY(-tpf);
                }
            }

            if (SceneDrag.isActive()) {
                Cgm dragCgm = SceneDrag.getCgm();
                SceneView sceneView = dragCgm.getSceneView();
                sceneView.dragAxis();
            }

        } else if (viewType == ViewType.Score) {
            Drag.updateGnomon();
        }
        updateDragPov();
        updateTexturePreviewer();

        // Update the views.
        source.getSceneView().update(null, tpf);
        target.getSceneView().update(null, tpf);
        source.getScoreView().update(source, tpf);
        target.getScoreView().update(target, tpf);
    }
    // *************************************************************************
    // private methods

    /**
     * Access the Texture that's being previewed.
     *
     * @return the pre-existing instance, or null if none
     */
    private Texture getPreviewTexture() {
        Material material = texturePreviewer.getMaterial();
        MatParamTexture mpt = material.getTextureParam("ColorMap");
        Texture result = null;
        if (mpt != null) {
            result = mpt.getTextureValue();
        }

        return result;
    }

    /**
     * Update the menu bar and status bar.
     */
    private void updateBars() {
        EditorModel model = CharEd.getModel();
        MiscOptions misc = model.getMisc();

        // Update visibility of both bars.
        boolean visible = misc.isMenuBarVisible();
        Element menuBar = getScreen().findElementById("menu bar");
        menuBar.setVisible(visible);
        Element statusBar = getScreen().findElementById("status bar");
        statusBar.setVisible(visible);

        if (visible) {
            /*
             * Update the description of axis-dragging options at the left end
             * of the status bar.
             */
            String description = "";
            ViewType viewType = mouseViewType();
            if (viewType == ViewType.Scene) {
                AxesOptions axesOptions = model.getScene().getAxes();
                description = axesOptions.describe();
            }
            setStatusText("statusLeft", " " + description);

            // Copy the status message to the center.
            String message = misc.statusMessage();
            setStatusText("statusCenter", message);

            // Update the description of camera options at the right end.
            description = "";
            if (viewType == ViewType.Scene) {
                CameraOptions options = model.getScene().getCamera();
                description = options.describe();
            }
            setStatusText("statusRight", description + " ");
        }
    }

    /**
     * Update the drag handle for the boundary.
     */
    private void updateBoundaryHandle() {
        int height = cam.getHeight(); // in pixels
        int width = cam.getWidth(); // in pixels
        float boundaryX = width * CharEd.getModel().getMisc().xBoundary();

        ViewPort boundaryViewPort = renderManager.getMainView("Boundary");
        List<Spatial> boundaryScenes = boundaryViewPort.getScenes();
        Node scene = (Node) boundaryScenes.get(0);
        Spatial handleSpatial = MySpatial.findNamed(scene, dragHandleName);
        if (handleSpatial == null) {
            handleSpatial = new Geometry(dragHandleName, dragHandleMesh);
            Material handleMaterial = MyAsset.createUnshadedMaterial(
                    assetManager, dragHandleColor);
            handleSpatial.setMaterial(handleMaterial);
            scene.attachChild(handleSpatial);
            boundaryViewPort.setEnabled(true);
        }

        handleSpatial.setCullHint(Spatial.CullHint.Always);
        boolean isSplit = EditorViewPorts.isSplitScreen();
        if (isSplit) {
            Vector2f mouseXY = inputManager.getCursorPosition();
            float mouseX = mouseXY.x;
            float dSquared = FastMath.sqr(mouseX - boundaryX);
            if (dSquared < maxDSquared) {
                handleSpatial.setCullHint(Spatial.CullHint.Never);
                handleSpatial.setLocalTranslation(boundaryX, height / 2f, 0f);
            }
        }
    }

    /**
     * If a POV is being dragged, update it.
     */
    private void updateDragPov() {
        if (getSignals().test(povSignalName)) { // dragging a POV
            if (dragPov == null) { // a brand-new drag
                dragPov = mousePov();
            } else {
                float dx = Mouse.getDX();
                if (dx != 0f) {
                    dragPov.moveLeft(dx / 1024f);
                }
                float dy = Mouse.getDY();
                if (dy != 0f) {
                    dragPov.moveUp(-dy / 1024f);
                }
            }
        } else {
            dragPov = null;
        }
    }

    /**
     * Enable/disable the performance-monitoring app states.
     */
    private void updatePerformanceMode() {
        PerformanceAppState pas
                = stateManager.getState(PerformanceAppState.class);
        StatsAppState sas = stateManager.getState(StatsAppState.class);

        PerformanceMode mode = CharEd.getModel().getMisc().performanceMode();
        switch (mode) {
            case DebugPas:
                pas.setEnabled(true);
                sas.setDisplayFps(false);
                sas.setDisplayStatView(false);
                break;

            case JmeStats:
                pas.setEnabled(false);
                sas.setDisplayFps(true);
                sas.setDisplayStatView(true);
                break;

            case Off:
                pas.setEnabled(false);
                sas.setDisplayFps(false);
                sas.setDisplayStatView(false);
                break;

            default:
                logger.log(Level.SEVERE, "mode={0}", mode);
                throw new IllegalStateException("invalid performance mode");
        }
    }

    /**
     * Update the texture previewer.
     */
    private void updateTexturePreviewer() {
        Tool textureTool = CharEd.gui.findTool("texture");
        boolean enabled = textureTool.isEnabled();
        boolean visible = CharEd.getModel().getMisc().isTexturePreviewVisible();
        if (visible && enabled) {
            guiNode.attachChild(texturePreviewer);
            /*
             * Scale the Geometry so that its width matches
             * that of the Tool.
             */
            Element toolElement = textureTool.getElement();
            float width = toolElement.getWidth();
            texturePreviewer.setLocalScale(width);
            /*
             * Translate the Geometry so that its lower left corner matches
             * that of the Tool.
             */
            float x = toolElement.getX();
            int toolHeight = toolElement.getHeight();
            int toolY = toolElement.getY();
            int guiHeight = guiViewPort.getCamera().getHeight();
            float y = guiHeight - toolY - toolHeight;
            texturePreviewer.setLocalTranslation(x, y, 0f);
        } else {
            texturePreviewer.removeFromParent();
        }
    }

    /**
     * Update the animation time.
     *
     * @param cgm (not null)
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    private static void updateTrackTime(Cgm cgm, float tpf) {
        LoadedAnimation animation = cgm.getAnimation();
        assert animation.isMoving();
        PlayOptions play = cgm.getPlay();
        float speed = play.getSpeed();
        assert speed != 0f;

        float time = play.getTime();
        time += speed * tpf;

        boolean cont = play.willContinue();
        boolean reverse = play.willReverse();
        float duration = animation.duration();
        float upperLimit = Math.min(play.getUpperLimit(), duration);
        float lowerLimit = play.getLowerLimit();
        float range = upperLimit - lowerLimit;
        assert range >= 0f : range;
        if (range == 0f) { // nowhere to go
            time = lowerLimit;
        } else if (time <= lowerLimit && speed < 0f
                || time >= upperLimit && speed > 0f) {
            if (cont && !reverse) { // wrap around
                time = lowerLimit + MyMath.modulo(time - lowerLimit, range);
            } else {
                time = FastMath.clamp(time, lowerLimit, upperLimit);
                if (reverse) { // reverse direction
                    play.setSpeed(-speed);
                }
                if (!cont) { // pause
                    play.setPaused(true);
                    if (!reverse) { // wrap to the other limit
                        time = upperLimit + lowerLimit - time;
                    }
                }
            }
        }

        play.setTime(time);
    }
}
