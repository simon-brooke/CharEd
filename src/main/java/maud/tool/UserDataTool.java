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
package maud.tool;

import com.jme3.animation.Bone;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.BasicScreenController;
import jme3utilities.nifty.WindowController;
import maud.Maud;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedSpatial;

/**
 * The controller for the "User-Data Tool" window in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class UserDataTool extends WindowController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(UserDataTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized controller.
     *
     * @param screenController
     */
    UserDataTool(BasicScreenController screenController) {
        super(screenController, "userDataTool", false);
    }
    // *************************************************************************
    // WindowController methods

    /**
     * Callback to update this window prior to rendering. (Invoked once per
     * render pass.)
     *
     * @param elapsedTime time interval between render passes (in seconds,
     * &ge;0)
     */
    @Override
    public void update(float elapsedTime) {
        super.update(elapsedTime);
        Maud.gui.setIgnoreGuiChanges(true);

        updateIndex();
        updateKey();
        updateValue();

        Maud.gui.setIgnoreGuiChanges(false);
    }
    // *************************************************************************
    // private methods

    /**
     * Update the index status and next/previous/select buttons.
     */
    private void updateIndex() {
        String indexText;
        String nButton = "", pButton = "", sButton = "";

        EditableCgm target = Maud.getModel().getTarget();
        int numKeys = target.getSpatial().countUserData();
        int selectedIndex = target.getUserData().findKeyIndex();
        if (selectedIndex >= 0) {
            int indexBase = Maud.getModel().getMisc().getIndexBase();
            indexText = String.format("#%d of %d", selectedIndex + indexBase,
                    numKeys);
            if (numKeys > 1) {
                nButton = "+";
                pButton = "-";
                sButton = "Select";
            }
        } else { // no key selected
            if (numKeys == 0) {
                indexText = "no keys";
            } else if (numKeys == 1) {
                indexText = "one key";
                sButton = "Select";
            } else {
                indexText = String.format("%d keys", numKeys);
                sButton = "Select";
            }
        }

        Maud.gui.setStatusText("userDataIndex", indexText);
        Maud.gui.setButtonText("userDataNext", nButton);
        Maud.gui.setButtonText("userDataPrevious", pButton);
        Maud.gui.setButtonText("userKeySelect", sButton);
    }

    /**
     * Update the "selected key" label and rename button label.
     */
    private void updateKey() {
        String dButton, keyText, rButton;

        Cgm target = Maud.getModel().getTarget();
        String key = target.getUserData().getKey();
        if (key == null) {
            dButton = "";
            keyText = "(none selected)";
            rButton = "";
        } else {
            dButton = "Delete";
            keyText = MyString.quote(key);
            rButton = "Rename";
        }

        Maud.gui.setStatusText("userKey", " " + keyText);
        Maud.gui.setButtonText("userKeyDelete", dButton);
        Maud.gui.setButtonText("userKeyRename", rButton);
    }

    /**
     * Update the value label.
     */
    private void updateValue() {
        String eButton, valueText;

        Cgm target = Maud.getModel().getTarget();
        String key = target.getUserData().getKey();
        if (key == null) {
            eButton = "";
            valueText = "";
        } else {
            eButton = "Alter";
            SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
            Object data = spatial.getUserData(key);
            if (data instanceof String) {
                String string = (String) data;
                valueText = MyString.quote(string);
            } else if (data instanceof Bone) {
                Bone bone = (Bone) data;
                valueText = bone.getName();
            } else {
                valueText = data.toString();
            }
        }

        Maud.gui.setStatusText("userValue", " " + valueText);
        Maud.gui.setButtonText("userDataEdit", eButton);
    }
}
