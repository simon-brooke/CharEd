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
 * Stephen Gold's name may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEPHEN GOLD BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package maud;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import java.util.logging.Logger;
import jme3utilities.Misc;
import jme3utilities.sky.Updater;

/**
 * The controller for the "Render Tool" window in Maud's "3D View" screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class RenderTool extends WindowController {
    // *************************************************************************
    // constants and loggers

    /**
     * multiplier for ambient light
     */
    final private static float ambientMultiplier = 1f;
    /**
     * multiplier for main light
     */
    final private static float mainMultiplier = 4f;
    /**
     * width and height of rendered shadow maps (pixels per side, &gt;0)
     */
    final private static int shadowMapSize = 4_096;
    /**
     * number of shadow map splits (&gt;0)
     */
    final private static int shadowMapSplits = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            RenderTool.class.getName());
    // *************************************************************************
    // fields

    /*
     * ambient light source for 3D view
     */
    final private AmbientLight ambientLight = new AmbientLight();
    /*
     * directional light source for 3D view
     */
    final private DirectionalLight mainLight = new DirectionalLight();
    /**
     * shadow filter for 3D view
     */
    private DirectionalLightShadowFilter dlsf = null;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized controller.
     *
     * @param screenController
     */
    public RenderTool(DddGui screenController) {
        super(screenController, "renderTool", false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Configure SkyControl's updater.
     *
     * @param updater (not null)
     */
    void configureUpdater(Updater updater) {
        assert isInitialized();

        updater.setAmbientLight(ambientLight);
        updater.setMainLight(mainLight);
        updater.addShadowFilter(dlsf);
        updater.setAmbientMultiplier(ambientMultiplier);
        updater.setMainMultiplier(mainMultiplier);
    }

    /**
     * Update after a change.
     */
    void update() {
        assert isInitialized();

        boolean enable = Maud.gui.isChecked("shadows");
        dlsf.setEnabled(enable);
    }
    // *************************************************************************
    // AppState methods

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
        /*
         * Light the scene.
         */
        rootNode.addLight(ambientLight);
        rootNode.addLight(mainLight);
        /*
         * Add a shadow filter.
         */
        dlsf = new DirectionalLightShadowFilter(assetManager, shadowMapSize,
                shadowMapSplits);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCF8);
        dlsf.setLight(mainLight);
        Misc.getFpp(viewPort, assetManager).addFilter(dlsf);

        update();
    }
}
