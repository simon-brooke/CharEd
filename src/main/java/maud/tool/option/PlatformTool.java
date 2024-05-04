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
package maud.tool.option;

import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.SliderTransform;
import jme3utilities.nifty.Tool;
import maud.CharEd;
import maud.model.WhichCgm;
import maud.model.option.scene.PlatformType;
import maud.model.option.scene.SceneOptions;

/**
 * The controller for the "Platform" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class PlatformTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PlatformTool.class.getName());
    /**
     * transform for the diameter sliders
     */
    final private static SliderTransform diameterSt = SliderTransform.Log10;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    public PlatformTool(GuiScreenController screenController) {
        super(screenController, "platform");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's sliders.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listSliders() {
        List<String> result = super.listSliders();
        result.add("sourcePlatformDiameter");
        result.add("targetPlatformDiameter");

        return result;
    }

    /**
     * Update the MVC model based on the sliders.
     *
     * @param sliderName the name (unique id prefix) of the slider (not null)
     */
    @Override
    public void onSliderChanged(String sliderName) {
        SceneOptions options = CharEd.getModel().getScene();

        float sourceDiameter = readSlider("sourcePlatformDiameter", diameterSt);
        options.setPlatformDiameter(WhichCgm.Source, sourceDiameter);

        float targetDiameter = readSlider("targetPlatformDiameter", diameterSt);
        options.setPlatformDiameter(WhichCgm.Target, targetDiameter);
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        SceneOptions options = CharEd.getModel().getScene();

        PlatformType type = options.getPlatformType();
        String tButton = type.toString();
        setButtonText("platformType", tButton);

        float sourceDiameter = options.getPlatformDiameter(WhichCgm.Source);
        setSlider("sourcePlatformDiameter", diameterSt, sourceDiameter);
        updateSliderStatus("sourcePlatformDiameter", sourceDiameter, " wu");

        float targetDiameter = options.getPlatformDiameter(WhichCgm.Target);
        setSlider("targetPlatformDiameter", diameterSt, targetDiameter);
        updateSliderStatus("targetPlatformDiameter", targetDiameter, " wu");
    }
}
