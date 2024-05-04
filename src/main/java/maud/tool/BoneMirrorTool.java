/*
 Copyright (c) 2020, Stephen Gold
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

import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;
import maud.CharEd;
import maud.model.EditorModel;
import maud.model.cgm.Cgm;

/**
 * The controller for the "Bone-Mirror" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class BoneMirrorTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(BoneMirrorTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    BoneMirrorTool(GuiScreenController screenController) {
        super(screenController, "boneMirror");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Callback to update this tool prior to rendering. (Invoked once per frame
     * while this tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        EditorModel model = CharEd.getModel();
        Cgm target = model.getTarget();
        String targetBoneName = target.getBone().name();
        setButtonText("bmTarget", targetBoneName);

        Cgm source = model.getSource();
        String sourceBoneName = source.getBone().name();
        setButtonText("bmSource", sourceBoneName);

        int axisIndex = model.getMisc().linkToolAxis();
        String axisText = MyString.axisName(axisIndex);
        setButtonText("linkAxis2", axisText);

        updateFeedback();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the feedback line and mirror button.
     */
    private void updateFeedback() {
        String feedback;
        String mButton = "";

        EditorModel model = CharEd.getModel();
        Cgm target = model.getTarget();
        Cgm source = model.getSource();
        if (!target.getBone().isSelected()) {
            feedback = "select a target bone";
        } else if (!source.isLoaded()) {
            feedback = "load a source model";
        } else if (!source.getBone().isSelected()) {
            feedback = "load a source bone";
        } else {
            feedback = "";
            mButton = "Mirror";
        }

        setStatusText("bmFeedback", feedback);
        setButtonText("bm", mButton);
    }
}
