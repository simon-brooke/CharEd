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

import com.jme3.math.Vector3f;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.SliderTransform;
import jme3utilities.nifty.Tool;
import maud.Maud;
import maud.model.cgm.SelectedLight;

/**
 * The controller for the "Light-Direction" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class LightDirectionTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * number of coordinate axes
     */
    final private static int numAxes = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LightDirectionTool.class.getName());
    /**
     * transform for the axis sliders
     */
    final private static SliderTransform axisSt = SliderTransform.Reversed;
    /**
     * names of the coordinate axes
     */
    final private static String[] axisNames = {"x", "y", "z"};
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    LightDirectionTool(GuiScreenController screenController) {
        super(screenController, "lightDirection");
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
        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            String sliderName = axisNames[iAxis] + "LDir";
            result.add(sliderName);
        }

        return result;
    }

    /**
     * Update the MVC model based on the sliders.
     *
     * @param sliderName the name (unique id prefix) of the slider (not null)
     */
    @Override
    public void onSliderChanged(String sliderName) {
        Vector3f direction = readVectorBank("LDir", axisSt, null);
        direction.normalizeLocal();

        SelectedLight light = Maud.getModel().getTarget().getLight();
        light.setDirection(direction);
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        String resetButton = "";
        String reverseButton = "";
        String snapButton = "";

        SelectedLight light = Maud.getModel().getTarget().getLight();
        boolean enabled = light.canDirect();
        if (enabled) {
            resetButton = "Reset";
            reverseButton = "Reverse";
            snapButton = "Snap";
            setSlidersToDirection();
        } else {
            clear();
        }

        setButtonText("resetLightDir", resetButton);
        setButtonText("reverseLightDir", reverseButton);
        setButtonText("snapLightDir", snapButton);
        setSlidersEnabled(enabled);
    }
    // *************************************************************************
    // private methods

    /**
     * Reset all 3 sliders and clear the status labels.
     */
    private void clear() {
        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            String sliderName = axisNames[iAxis] + "LDir";
            setSlider(sliderName, axisSt, 0f);
            setStatusText(sliderName + "SliderStatus", "");
        }
    }

    /**
     * Set all 3 sliders (and their status labels) based on the direction of the
     * selected light.
     */
    private void setSlidersToDirection() {
        SelectedLight light = Maud.getModel().getTarget().getLight();
        Vector3f dir = light.direction();
        float[] cosines = dir.toArray(null);
        for (int iAxis = 0; iAxis < numAxes; iAxis++) {
            String sliderName = axisNames[iAxis] + "LDir";
            float cos = cosines[iAxis];
            setSlider(sliderName, axisSt, cos);
            updateSliderStatus(sliderName, cos, "");
        }
    }
}
