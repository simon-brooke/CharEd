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
import com.jme3.asset.AssetManager;
import com.jme3.cursors.plugins.JmeCursor;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.ui.InputMode;

/**
 * Input mode for Maud's "3D View" screen. TODO rename
 *
 * @author Stephen Gold sgold@sonic.net
 */
class DddMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            DddMode.class.getName());
    /**
     * asset path to the cursor for this mode
     */
    final private static String assetPath = "Textures/cursors/default.cur";
    /*
     * action-string prefixes used by dialogs and popup menus
     */
    final static String copyAnimationPrefix = "copy animation ";
    final static String loadAnimationPrefix = "load animation ";
    final static String loadModelAssetPrefix = "load model asset ";
    final static String loadModelFilePrefix = "load model file ";
    final static String loadModelNamedPrefix = "load model named ";
    final static String openMenuPrefix = "open menu ";
    final static String renameAnimationPrefix = "rename animation ";
    final static String renameBonePrefix = "rename bone ";
    final static String saveModelAssetPrefix = "save model asset ";
    final static String saveModelFilePrefix = "save model file ";
    final static String selectBonePrefix = "select bone ";
    final static String selectBoneChildPrefix = "select boneChild ";
    final static String selectSpatialChildPrefix = "select spatialChild ";
    final static String selectToolPrefix = "select tool ";
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled, uninitialized mode.
     */
    DddMode() {
        super("3D View");
    }
    // *************************************************************************
    // ActionListener methods

    /**
     * Process an action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between render passes (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        logger.log(Level.INFO, "Got action {0} ongoing={1}", new Object[]{
            MyString.quote(actionString), ongoing
        });
        /*
         * Parse the action string and attempt to handle the action.
         */
        boolean handled = false;
        if (ongoing) {
            String firstWord = actionString.split(" ")[0];
            switch (firstWord) {
                case "copy":
                    handled = copyAction(actionString);
                    break;
                case "load":
                    handled = loadAction(actionString);
                    break;
                case "new":
                    handled = newAction(actionString);
                    break;
                case "next":
                    handled = nextAction(actionString);
                    break;
                case "open":
                    handled = openAction(actionString);
                    break;
                case "previous":
                    handled = previousAction(actionString);
                    break;
                case "rename":
                    handled = renameAction(actionString);
                    break;
                case "reset":
                    handled = resetAction(actionString);
                    break;
                case "save":
                    handled = saveAction(actionString);
                    break;
                case "select":
                    handled = selectAction(actionString);
                    break;
                case "toggle":
                    handled = toggleAction(actionString);
                    break;
                case "view":
                    handled = viewAction(actionString);
                    break;
                case "warp":
                    handled = warpAction(actionString);
            }
        }

        if (!handled) {
            /*
             * Forward the unhandled action to the application.
             */
            actionApplication.onAction(actionString, ongoing, tpf);
        }
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Hotkey bindings used if the configuration asset is missing.
     */
    @Override
    protected void defaultBindings() {
        // intentionally empty
    }

    /**
     * Initialize this (disabled) mode prior to its 1st update.
     *
     * @param stateManager (not null)
     * @param application (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        AssetManager am = application.getAssetManager();
        JmeCursor cursor = (JmeCursor) am.loadAsset(assetPath);
        setCursor(cursor);

        super.initialize(stateManager, application);
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "copy" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean copyAction(String actionString) {
        boolean handled = false;
        if (actionString.startsWith(copyAnimationPrefix)) {
            String destName = MyString.remainder(actionString,
                    copyAnimationPrefix);
            Maud.model.cgm.copyAnimation(destName);
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "load" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean loadAction(String actionString) {
        boolean handled = false;
        if (actionString.startsWith(loadAnimationPrefix)) {
            String name = MyString.remainder(actionString, loadAnimationPrefix);
            Maud.model.animation.load(name);
            handled = true;

        } else if (actionString.startsWith(loadModelAssetPrefix)) {
            String path = MyString.remainder(actionString,
                    loadModelAssetPrefix);
            Maud.model.cgm.loadModelAsset(path);
            handled = true;

        } else if (actionString.startsWith(loadModelFilePrefix)) {
            String path = MyString.remainder(actionString, loadModelFilePrefix);
            Maud.gui.loadModelFile(path);
            handled = true;

        } else if (actionString.startsWith(loadModelNamedPrefix)) {
            String name = MyString.remainder(actionString,
                    loadModelNamedPrefix);
            Maud.model.cgm.loadModelNamed(name);
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "new" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean newAction(String actionString) {
        boolean handled = false;
        if (actionString.equals("new checkpoint")) {
            History.add();
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "next" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean nextAction(String actionString) {
        boolean handled = false;
        if (actionString.equals("next checkpoint")) {
            History.redo();
            handled = true;
        }

        return handled;
    }

    /**
     * Process an "open" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean openAction(String actionString) {
        boolean handled = false;
        if (actionString.startsWith(openMenuPrefix)) {
            String menuPath = MyString.remainder(actionString, openMenuPrefix);
            handled = Maud.gui.openMenu(menuPath);
        }

        return handled;
    }

    /**
     * Process a "previous" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean previousAction(String actionString) {
        boolean handled = false;
        if (actionString.equals("previous checkpoint")) {
            History.undo();
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "rename" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean renameAction(String actionString) {
        boolean handled = false;
        String newName;
        if (actionString.equals("rename animation")) {
            Maud.gui.renameAnimation();
            handled = true;

        } else if (actionString.equals("rename bone")) {
            Maud.gui.renameBone();
            handled = true;

        } else if (actionString.startsWith(renameAnimationPrefix)) {
            newName = MyString.remainder(actionString, renameAnimationPrefix);
            Maud.model.cgm.renameAnimation(newName);
            handled = true;

        } else if (actionString.startsWith(renameBonePrefix)) {
            newName = MyString.remainder(actionString, renameBonePrefix);
            Maud.model.cgm.renameBone(newName);
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "reset" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean resetAction(String actionString) {
        boolean handled = false;
        switch (actionString) {
            case "reset bone ang anim":
                Maud.gui.boneAngle.setToAnimation();
                handled = true;
                break;
            case "reset bone ang bind":
                Maud.gui.boneAngle.reset();
                handled = true;
                break;
            case "reset bone off anim":
                Maud.gui.boneOffset.setToAnimation();
                handled = true;
                break;
            case "reset bone off bind":
                Maud.gui.boneOffset.reset();
                handled = true;
                break;
            case "reset bone sca anim":
                Maud.gui.boneScale.setToAnimation();
                handled = true;
                break;
            case "reset bone sca bind":
                Maud.gui.boneScale.reset();
                handled = true;
        }

        return handled;
    }

    /**
     * Process a "save" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean saveAction(String actionString) {
        boolean handled = false;
        if (actionString.startsWith(saveModelAssetPrefix)) {
            String path = MyString.remainder(actionString,
                    saveModelAssetPrefix);
            Maud.model.cgm.writeModelToAsset(path);
            handled = true;

        } else if (actionString.startsWith(saveModelFilePrefix)) {
            String path = MyString.remainder(actionString, saveModelFilePrefix);
            Maud.model.cgm.writeModelToFile(path);
            handled = true;
        }

        return handled;
    }

    /**
     * Process a "select" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean selectAction(String actionString) {
        boolean handled = false;
        switch (actionString) {
            case "select boneChild":
                Maud.gui.selectBoneChild();
                handled = true;
                break;
            case "select boneParent":
                Maud.model.bone.selectParent();
                handled = true;
                break;
            case "select spatialChild":
                Maud.gui.selectSpatialChild();
                handled = true;
                break;
            case "select spatialParent":
                Maud.model.spatial.selectParent();
                handled = true;
        }

        if (!handled) {
            String arg;
            if (actionString.startsWith(selectBonePrefix)) {
                arg = MyString.remainder(actionString, selectBonePrefix);
                Maud.gui.selectBone(arg);
                handled = true;

            } else if (actionString.startsWith(selectBoneChildPrefix)) {
                arg = MyString.remainder(actionString, selectBoneChildPrefix);
                Maud.gui.selectBoneChild(arg);
                handled = true;

            } else if (actionString.startsWith(selectSpatialChildPrefix)) {
                arg = MyString.remainder(actionString,
                        selectSpatialChildPrefix);
                Maud.gui.selectSpatialChild(arg);
                handled = true;
            }
        }

        if (!handled && actionString.startsWith(selectToolPrefix)) {
            String toolName = MyString.remainder(actionString,
                    selectToolPrefix);
            handled = Maud.gui.selectTool(toolName);
        }

        return handled;
    }

    /**
     * Process a "toggle" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean toggleAction(String actionString) {
        boolean handled = false;
        switch (actionString) {
            case "toggle degrees":
                Maud.model.misc.toggleAnglesInDegrees();
                handled = true;
                break;
            case "toggle pause":
                Maud.model.animation.togglePaused();
                handled = true;
        }

        return handled;
    }

    /**
     * Process a "view" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean viewAction(String actionString) {
        boolean handled = false;
        switch (actionString) {
            case "view horizontal":
                Maud.model.camera.goHorizontal();
                handled = true;
                break;
        }

        return handled;
    }

    /**
     * Process a "warp" action.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private boolean warpAction(String actionString) {
        boolean handled = false;
        switch (actionString) {
            case "warp cursor":
                Maud.gui.cursor.warpCursor();
                handled = true;
                break;
        }

        return handled;
    }
}
