/*
 Copyright (c) 2018, Stephen Gold
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

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.GuiWindowController;
import maud.EditorScreen;

/**
 * A controller for a tool window in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class Tool extends GuiWindowController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Tool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized controller.
     *
     * @param screenController the controller of the screen that contains the
     * tool (not null)
     * @param name the name (unique id prefix) of the tool (not null)
     */
    Tool(GuiScreenController screenController, String name) {
        super(screenController, name + "Tool", false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Enumerate the tool's sliders.
     *
     * @return a new list of names (unique id prefixes)
     */
    List<String> listSliders() {
        List<String> result = new ArrayList<>(5);
        return result;
    }

    /**
     * Update the MVC model based on the tool's sliders, if any.
     */
    public void onSliderChanged() {
        // meant to be overridden
    }

    /**
     * Callback to update the tool prior to rendering. (Invoked once per render
     * pass while the tool is displayed.)
     */
    abstract void toolUpdate();
    // *************************************************************************
    // GuiWindowController methods

    /**
     * Initialize this controller prior to its 1st update.
     *
     * @param stateManager (not null)
     * @param application application which owns the window (not null)
     */
    @Override
    public void initialize(AppStateManager stateManager,
            Application application) {
        super.initialize(stateManager, application);

        EditorScreen screen = (EditorScreen) getScreenController();
        List<String> sliderNames = listSliders();
        for (String name : sliderNames) {
            screen.mapSlider(name, this);
        }
    }

    /**
     * Callback to update this tool prior to rendering. (Invoked once per render
     * pass while the tool is displayed.)
     *
     * @param elapsedTime time interval between render passes (in seconds,
     * &ge;0)
     */
    @Override
    final public void update(float elapsedTime) {
        super.update(elapsedTime);
        EditorScreen screen = (EditorScreen) getScreenController();
        screen.setIgnoreGuiChanges(true);
        toolUpdate();
        screen.setIgnoreGuiChanges(false);
    }
}
