/*
 Copyright (c) 2017-2019, Stephen Gold
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

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.asset.AssetConfig;
import com.jme3.audio.openal.ALAudioRenderer;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.debug.DebugTools;
import com.jme3.scene.plugins.bvh.BVHLoader;
import com.jme3.system.AppSettings;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3_ext_xbuf.XbufLoader;
import jme3utilities.Misc;
import jme3utilities.MyString;
import jme3utilities.StringLoader;
import jme3utilities.UncachedKey;
import jme3utilities.Validate;
import jme3utilities.ViewPortAppState;
import jme3utilities.debug.PerformanceAppState;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.nifty.GuiApplication;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.DisplaySettings;
import jme3utilities.ui.DisplaySizeLimits;
import jme3utilities.ui.InputMode;
import maud.dialog.QuitDialog;
import maud.model.EditState;
import maud.model.EditorModel;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.view.scene.SceneView;

/**
 * GUI application to edit jMonkeyEngine animated C-G models. The application's
 * main entry point is in this class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Maud extends GuiApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Maud.class.getName());
    /**
     * path to the properties asset used to configure hotkey bindings
     */
    final private static String hotkeyBindingsAssetPath
            = "Interface/bindings/editor.properties";
    /**
     * path to the script asset evaluated at startup
     */
    final public static String startupScriptAssetPath = "Scripts/startup.js";
    // *************************************************************************
    // fields

    /**
     * Nifty screen for editing hotkey bindings
     */
    final static BindScreen bindScreen = new BindScreen();
    /**
     * true once {@link #startup1()} has completed, until then false
     */
    private boolean didStartup1 = false;
    /**
     * true to load startup script, false to skip it
     */
    private static boolean loadStartupScript = true;
    /**
     * Nifty screen for editing display settings
     */
    private static DsScreen displaySettingsScreen;
    /**
     * MVC model for the editor screen (live copy)
     */
    private static EditorModel editorModel = new EditorModel();
    /**
     * controller for the editor screen
     */
    final public static EditorScreen gui = new EditorScreen();
    /**
     * application instance, set by {@link #main(java.lang.String[])}
     */
    private static Maud application;
    /**
     * dumper for scene dumps
     */
    final private static PhysicsDumper dumper = new PhysicsDumper();
    /**
     * application name for the window's title bar
     */
    final private static String applicationName = "Maud version 1.0.0-alpha.7";
    /**
     * path to logo image for the settings dialog
     */
    final private static String logoAssetPath
            = "Textures/icons/Maud-settings.png";
    // *************************************************************************
    // new methods exposed

    /**
     * Access the application.
     *
     * @return the pre-existing instance (not null)
     */
    public static Maud getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the display-settings screen.
     *
     * @return the pre-existing instance (not null)
     */
    public static DsScreen getDisplaySettingsScreen() {
        assert displaySettingsScreen != null;
        return displaySettingsScreen;
    }

    /**
     * Access the live MVC model for the editor screen.
     *
     * @return the pre-existing instance (not null)
     */
    public static EditorModel getModel() {
        assert editorModel != null;
        return editorModel;
    }

    /**
     * Test whether the startup script has been customized.
     *
     * @return true if customized, otherwise false
     */
    public static boolean isStartupScriptCustomized() {
        String assetPath = Maud.startupScriptAssetPath;
        String filePath = ActionApplication.filePath(assetPath);
        File file = new File(filePath);
        boolean result = file.exists();

        return result;
    }

    /**
     * Main entry point for Maud.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String os = System.getProperty("os.name").toLowerCase();
        String renderer;
        if (os.contains("windows")) {
            renderer = AppSettings.LWJGL_OPENGL2;
        } else {
            renderer = AppSettings.LWJGL_OPENGL33;
        }
        boolean forceDialog = false;
        /*
         * Process any command-line arguments.
         */
        for (String arg : arguments) {
            switch (arg) {
                case "-2":
                case "--openGL2":
                    renderer = AppSettings.LWJGL_OPENGL2;
                    break;

                case "-3":
                case "--openGL33":
                    renderer = AppSettings.LWJGL_OPENGL33;
                    break;

                case "-f":
                case "--forceDialog":
                    forceDialog = true;
                    break;

                case "-s":
                case "--skipStartup":
                    loadStartupScript = false;
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }

        application = new Maud();
        DisplaySizeLimits dsl = new DisplaySizeLimits(
                640, 480, // min width, height
                2_048, 1_080 // max width, height
        );
        final String r = renderer;
        DisplaySettings displaySettings
                = new DisplaySettings(application, applicationName, dsl) {
            @Override
            protected void applyOverrides(AppSettings settings) {
                super.applyOverrides(settings);
                settings.setRenderer(r);
                settings.setSettingsDialogImage(logoAssetPath);
            }
        };

        displaySettingsScreen = new DsScreen(displaySettings);
        if (forceDialog) {
            displaySettings.setForceDialog(true);
        }
        /*
         * Mute the chatty loggers found in certain packages.
         */
        Misc.setLoggingLevels(Level.WARNING);
        Logger.getLogger(ALAudioRenderer.class.getName())
                .setLevel(Level.SEVERE);
        //logger.setLevel(Level.INFO);
        Logger.getLogger(AssetConfig.class.getName())
                .setLevel(Level.SEVERE);

        AppSettings appSettings = displaySettings.initialize();
        if (appSettings != null) {
            application.setSettings(appSettings);
            /*
             * Don't pause on lost focus.  This simplifies debugging by
             * permitting the application to run while minimized.
             */
            //application.setPauseOnLostFocus(false);
            /*
             * If the settings dialog should be shown, it was already shown
             * by DisplaySettings.initialize().
             */
            application.setShowSettings(false);
            application.start();
            /*
             * ... and onward to Maud.guiInitializeApplication()!
             */
        }
    }

    /**
     * Perform the action described by the specified action string using the
     * editor screen's input mode. Invoked via reflection, from scripts.
     *
     * @param actionString (not null)
     */
    public static void perform(String actionString) {
        gui.perform(actionString);
    }

    /**
     * Revert the startup script to the default by removing the custom startup
     * script (if any) in the "Written Assets" folder.
     */
    public static void revertStartupScript() {
        String assetPath = Maud.startupScriptAssetPath;
        String filePath = ActionApplication.filePath(assetPath);
        File file = new File(filePath);
        file.delete();
    }

    /**
     * Alter which MVC model for the editor screen is live. Invoked only when
     * restoring a checkpoint.
     *
     * @param savedState saved MVC model state to make live (not null)
     */
    public static void setModel(EditorModel savedState) {
        Validate.nonNull(savedState, "saved state");

        editorModel.preMakeLive();
        editorModel = savedState;
        editorModel.postMakeLive();
    }

    /**
     * Initialization performed the 1st time the editor screen is displayed.
     */
    void startup2() {
        logger.info("");
        /*
         * Attach controllers for windows in the editor screen.
         */
        gui.tools.attachAll(stateManager);
        /*
         * Disable flyCam.
         */
        flyCam.setEnabled(false);
        /*
         * Capture a screenshot each time the SYSRQ hotkey is pressed.
         */
        ScreenshotAppState screenShotState = new ScreenshotAppState();
        boolean success = stateManager.attach(screenShotState);
        assert success;
        /**
         * Manage latency statistics.
         */
        PerformanceAppState pas = new PerformanceAppState();
        success = stateManager.attach(pas);
        assert success;
        /**
         * Manage view-port updating.
         */
        ViewPortAppState viewPortState = new ViewPortAppState();
        success = stateManager.attach(viewPortState);
        assert success;
    }

    /**
     * Initialization performed after the editor-screen tools have initialized.
     */
    void startup3() {
        logger.info("");
        if (loadStartupScript) {
            /*
             * Evaluate the startup script.
             */
            UncachedKey key = new UncachedKey(startupScriptAssetPath);
            assetManager.loadAsset(key);
        }
        /*
         * If no target model is loaded, load Jaime as a fallback.
         */
        EditableCgm target = Maud.getModel().getTarget();
        if (!target.isLoaded()) {
            boolean success = target.loadNamed("Jaime");
            assert success;
        }
    }
    // *************************************************************************
    // GuiApplication methods

    /**
     * Callback invoked when an ongoing action isn't handled.
     *
     * @param actionString textual description of the action (not null)
     */
    @Override
    public void didntHandle(String actionString) {
        super.didntHandle(actionString);

        String message = String.format("unimplemented feature (action = %s)",
                MyString.quote(actionString));
        editorModel.getMisc().setStatusMessage(message);
    }

    /**
     * Initialize this application and warn of any configuration issues.
     */
    @Override
    public void guiInitializeApplication() {
        logger.info("");

        if (!Misc.areAssertionsEnabled()) {
            String message = "Assertions are disabled.";
            logger.warning(message);
            editorModel.getMisc().setStatusMessage(message);
        }

        int buttonCount = context.getMouseInput().getButtonCount();
        if (buttonCount < 3) {
            String message = String.format("Number of mouse buttons = %d.",
                    buttonCount);
            logger.warning(message);
            editorModel.getMisc().setStatusMessage(message);
        }

        StartScreen startScreen = new StartScreen();
        stateManager.attach(startScreen);
    }

    /**
     * Process an action (from the GUI or keyboard) that wasn't handled by the
     * input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        boolean handled = false;
        if (ongoing) {
            logger.log(Level.INFO, "Got ongoing action {0}",
                    MyString.quote(actionString));

            handled = true;
            switch (actionString) {
                case "dump physicsSpace":
                    dumpPhysicsSpace();
                    break;
                case "dump renderer":
                    dumpRenderer();
                    break;
                case "dump scene":
                    dumpScene();
                    break;
                case "edit bindings":
                    InputMode activeInputMode = InputMode.getActiveMode();
                    bindScreen.activate(activeInputMode);
                    break;
                case "quit":
                    quitUnconfirmed();
                    break;
                default:
                    handled = false;
            }
        }

        if (!handled) {
            /*
             * Forward unhandled action to the superclass.
             */
            super.onAction(actionString, ongoing, tpf);
        }
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);

        if (!didStartup1) {
            startup1();
            didStartup1 = true;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "dump physicsSpace" action.
     */
    private void dumpPhysicsSpace() {
        Cgm cgm = gui.mouseCgm();
        SceneView sceneView = cgm.getSceneView();
        PhysicsSpace space = sceneView.getPhysicsSpace();
        dumper.dump(space);
    }

    /**
     * Process a "dump renderer" action.
     */
    private void dumpRenderer() {
        //dumper.setDumpBucket(true);
        //dumper.setDumpCull(true);
        //dumper.setDumpMatParam(true);
        //dumper.setDumpOverride(true);
        dumper.setDumpShadow(true);
        dumper.setDumpTransform(true);
        //dumper.setDumpUser(true);
        dumper.dump(renderManager);
    }

    /**
     * Process a "dump scene" action.
     */
    private void dumpScene() {
        //dumper.setDumpBucket(true);
        //dumper.setDumpCull(true);
        //dumper.setDumpMatParam(true);
        //dumper.setDumpOverride(true);
        dumper.setDumpShadow(true);
        dumper.setDumpTransform(true);
        //dumper.setDumpUser(true);
        dumper.dump(rootNode);
    }

    /**
     * If confirmed, terminate the application.
     */
    private void quitUnconfirmed() {
        EditState cgmEditState = editorModel.getTarget().getEditState();
        int cgmEdits = cgmEditState.countUnsavedEdits();

        EditState mapEditState = editorModel.getMap().getEditState();
        int mapEdits = mapEditState.countUnsavedEdits();

        String message;
        if (cgmEdits + mapEdits == 0) {
            message = "Quit Maud?";
        } else {
            message = "You've made ";
            if (cgmEdits > 0) {
                message += String.format(
                        "%d unsaved edit%s to the target model", cgmEdits,
                        cgmEdits == 1 ? "" : "s");
            }
            if (cgmEdits > 0 && mapEdits > 0) {
                message += " and ";
            }
            if (mapEdits > 0) {
                message += String.format(
                        "%d unsaved edit%s to the skeleton map", mapEdits,
                        mapEdits == 1 ? "" : "s");
            }
            message += ".\nReally quit Maud?";
        }

        QuitDialog controller = new QuitDialog();
        gui.showConfirmDialog(message, "", SimpleApplication.INPUT_MAPPING_EXIT,
                controller);
    }

    /**
     * Initialization performed during the 1st invocation of
     * {@link #simpleUpdate(float)}.
     */
    private void startup1() {
        logger.info("");
        /*
         * Register loaders for BVH, JavaScript, txt, font, and Xbuf assets.
         */
        assetManager.registerLoader(BVHLoader.class, "bvh", "BVH");
        assetManager.registerLoader(ScriptLoader.class, "js");
        assetManager.registerLoader(StringLoader.class, "txt");
        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        assetManager.registerLoader(XbufLoader.class, "xbuf");

        DebugTools.setStartupMessageEnabled(false);
        EditorViewPorts.startup1();
        /*
         * Attach screen controllers for the 3 major screens.
         */
        stateManager.attachAll(gui, bindScreen, displaySettingsScreen);
        /*
         * Configure and attach input mode for the editor screen.
         */
        gui.inputMode.setConfigPath(hotkeyBindingsAssetPath);
        stateManager.attach(gui.inputMode);
        /*
         * Disable the JME statistic displays.
         * These can be re-enabled by pressing the F5 hotkey.
         */
        setDisplayFps(false);
        setDisplayStatView(false);
    }
}
