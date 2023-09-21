/*
 Copyright (c) 2017-2022, Stephen Gold
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
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;
import maud.DescribeUtil;
import maud.Maud;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedUserData;

/**
 * The controller for the "User-Data" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class UserDataTool extends Tool {
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
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    UserDataTool(GuiScreenController screenController) {
        super(screenController, "userData");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateIndex();
        updateKey();
        updateType();
        updateValue();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the index status and next/previous/select buttons.
     */
    private void updateIndex() {
        String indexStatus;
        String nextButton = "";
        String previousButton = "";
        String selectButton = "";

        EditableCgm target = Maud.getModel().getTarget();
        int numKeys = target.getSpatial().countUserData();
        int selectedIndex = target.getUserData().findKeyIndex();
        if (selectedIndex >= 0) {
            indexStatus = DescribeUtil.index(selectedIndex, numKeys);
            if (numKeys > 1) {
                nextButton = "+";
                previousButton = "-";
                selectButton = "Select";
            }
        } else { // no key selected
            if (numKeys == 0) {
                indexStatus = "no keys";
            } else if (numKeys == 1) {
                indexStatus = "one key";
                selectButton = "Select";
            } else {
                indexStatus = String.format("%d keys", numKeys);
                selectButton = "Select";
            }
        }

        setStatusText("userDataIndex", indexStatus);
        setButtonText("userDataNext", nextButton);
        setButtonText("userDataPrevious", previousButton);
        setButtonText("userKeySelect", selectButton);
    }

    /**
     * Update the "selected key" label and rename button label.
     */
    private void updateKey() {
        String dButton;
        String keyText;
        String rButton;

        Cgm target = Maud.getModel().getTarget();
        String key = target.getUserData().key();
        if (key == null) {
            dButton = "";
            keyText = "(none selected)";
            rButton = "";
        } else {
            dButton = "Delete";
            keyText = MyString.quote(key);
            rButton = "Rename";
        }

        setStatusText("userKey", " " + keyText);
        setButtonText("userKeyDelete", dButton);
        setButtonText("userKeyRename", rButton);
    }

    /**
     * Update the type status.
     */
    private void updateType() {
        SelectedUserData data = Maud.getModel().getTarget().getUserData();

        String typeText = "";
        if (data.isSelected()) {
            typeText = data.describeType();
        }

        setStatusText("userDataType", " " + typeText);
    }

    /**
     * Update the value button.
     */
    private void updateValue() {
        String valueButton = "";

        SelectedUserData datum = Maud.getModel().getTarget().getUserData();
        String key = datum.key();
        if (key != null) {
            Object value = datum.getValue();
            if (value instanceof String) {
                String string = (String) value;
                valueButton = MyString.quote(string);
            } else if (value instanceof Bone) {
                Bone bone = (Bone) value;
                valueButton = bone.getName();
            } else {
                valueButton = value.toString();
            }
        }

        setButtonText("userValue", valueButton);
    }
}
