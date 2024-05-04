/*
 Copyright (c) 2018-2019, Stephen Gold
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

import com.jme3.math.ColorRGBA;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.SliderTransform;
import jme3utilities.nifty.Tool;
import maud.CharEd;
import maud.model.cgm.SelectedLight;

/**
 * The controller for the "Light-Color" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class LightColorTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LightColorTool.class.getName());
    /**
     * transform for the color sliders
     */
    final private static SliderTransform colorSt = SliderTransform.Reversed;
    /**
     * transform for the level slider
     */
    final private static SliderTransform levelSt = SliderTransform.Log10;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    LightColorTool(GuiScreenController screenController) {
        super(screenController, "lightColor");
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
        result.add("lcR");
        result.add("lcG");
        result.add("lcB");
        result.add("lightLevel");

        return result;
    }

    /**
     * Update the MVC model based on the sliders.
     *
     * @param sliderName the name (unique id prefix) of the slider (not null)
     */
    @Override
    public void onSliderChanged(String sliderName) {
        ColorRGBA color = readColorBank("lc", colorSt, null);
        float level = readSlider("lightLevel", levelSt);
        color.r *= level;
        color.g *= level;
        color.b *= level;

        SelectedLight light = CharEd.getModel().getTarget().getLight();
        light.setColor(color);
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        String resetButton = "";
        float r = 1f;
        float g = 1f;
        float b = 1f;
        float level = 1f;

        SelectedLight light = CharEd.getModel().getTarget().getLight();
        boolean selected = light.isSelected();
        if (selected) {
            resetButton = "Reset";
            ColorRGBA color = light.color();
            level = MyMath.max(color.r, color.g, color.b);
            r = color.r / level;
            g = color.g / level;
            b = color.b / level;
            updateSliderStatus("lcR", color.r, "");
            updateSliderStatus("lcG", color.g, "");
            updateSliderStatus("lcB", color.b, "");
        } else {
            setStatusText("lcRSliderStatus", "");
            setStatusText("lcGSliderStatus", "");
            setStatusText("lcBSliderStatus", "");
        }

        setButtonText("resetLightColor", resetButton);
        setSlider("lcR", colorSt, r);
        setSlider("lcG", colorSt, g);
        setSlider("lcB", colorSt, b);
        setSlider("lightLevel", levelSt, level);
        setSlidersEnabled(selected);
    }
}
