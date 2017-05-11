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

import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.BasicScreenController;
import jme3utilities.nifty.WindowController;

/**
 * The controller for the "Retarget Tool" window in Maud's "3D View" screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class RetargetTool extends WindowController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            RetargetTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized controller.
     *
     * @param screenController
     */
    RetargetTool(BasicScreenController screenController) {
        super(screenController, "retargetTool", false);
    }
    // *************************************************************************
    // AppState methods

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

        String sButton, sourceAssetDesc;
        String path = Maud.model.retarget.getSourceCgmAssetPath();
        if (path == null) {
            sButton = "";
            sourceAssetDesc = "(none selected)";
        } else if (Maud.model.retarget.isValidSourceCgm()) {
            sButton = "Select";
            sourceAssetDesc = MyString.quote(path);
        } else {
            sButton = "";
            sourceAssetDesc = MyString.quote(path);
        }
        Maud.gui.setStatusText("sourceAsset", " " + sourceAssetDesc);
        Maud.gui.setButtonLabel("selectSourceAnimationButton", sButton);

        String mapAssetPath = Maud.model.retarget.getMappingAssetPath();
        String mapAssetDesc;
        if (mapAssetPath == null) {
            mapAssetDesc = "(none selected)";
        } else {
            mapAssetDesc = MyString.quote(mapAssetPath);
        }
        Maud.gui.setStatusText("mapAsset", " " + mapAssetDesc);

        String sourceAnim = Maud.model.retarget.getSourceAnimationName();
        String rButton, sourceAnimDesc;
        if (sourceAnim == null) {
            rButton = "";
            sourceAnimDesc = "(none selected)";
        } else if (Maud.model.retarget.isValidMapping()) {
            rButton = "Retarget";
            sourceAnimDesc = MyString.quote(sourceAnim);
        } else {
            rButton = "";
            sourceAnimDesc = MyString.quote(sourceAnim);
        }
        Maud.gui.setButtonLabel("retargetButton", rButton);
        Maud.gui.setStatusText("sourceAnimation", " " + sourceAnimDesc);
    }
}
