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

import de.lessvoid.nifty.controls.Slider;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.BasicScreenController;
import jme3utilities.nifty.WindowController;
import maud.Maud;
import maud.model.LoadedAnimation;
import maud.model.LoadedCgm;

/**
 * The controller for the "Animation Tool" window in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class AnimationTool extends WindowController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            AnimationTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized controller.
     *
     * @param screenController
     */
    AnimationTool(BasicScreenController screenController) {
        super(screenController, "animationTool", false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Update the MVC model based on the sliders.
     */
    void onSliderChanged() {
        LoadedCgm target = Maud.getModel().getTarget();
        float duration = target.animation.getDuration();
        float speed;
        if (duration > 0f) {
            Slider slider = Maud.gui.getSlider("speed");
            speed = slider.getValue();
            target.animation.setSpeed(speed);
        }

        boolean moving = target.animation.isMoving();
        if (!moving) {
            Slider slider = Maud.gui.getSlider("time");
            float fraction = slider.getValue();
            float time = fraction * duration;
            target.animation.setTime(time);
        }
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
        Maud.gui.setIgnoreGuiChanges(true);

        LoadedCgm target = Maud.getModel().getTarget();
        String hasTrackText;
        if (!target.bone.isSelected()) {
            hasTrackText = "no bone";
        } else if (!target.animation.isReal()) {
            hasTrackText = "";
        } else {
            if (target.bone.hasTrack()) {
                hasTrackText = "has track";
            } else {
                hasTrackText = "no track";
            }
        }
        Maud.gui.setStatusText("animationHasTrack", " " + hasTrackText);

        updateControlIndex();
        updateIndex();
        updateLooping();
        updateName();
        updateSpeed();
        updateTrackTime();
        updateTrackCounts();

        Maud.gui.setIgnoreGuiChanges(false);
    }
    // *************************************************************************
    // private methods

    /**
     * Update the control index status and previous/next/select buttons.
     */
    private void updateControlIndex() {
        String indexText;
        String sButton = "";
        String nButton = "";
        String pButton = "";

        LoadedCgm target = Maud.getModel().getTarget();
        if (target.countAnimControls() > 0) {
            sButton = "Select AnimControl";
            int numAnimControls = target.countAnimControls();
            if (target.isAnimControlSelected()) {
                int selectedIndex = target.findAnimControlIndex();
                indexText = String.format("#%d of %d", selectedIndex + 1,
                        numAnimControls);
                nButton = "+";
                pButton = "-";
            } else {
                if (numAnimControls == 0) {
                    indexText = "no AnimControls";
                } else if (numAnimControls == 1) {
                    indexText = "one AnimControl";
                } else {
                    indexText = String.format("%d AnimControls",
                            numAnimControls);
                }
            }

        } else {
            indexText = "not animated";
        }

        Maud.gui.setButtonLabel("animControlPreviousButton", pButton);
        Maud.gui.setStatusText("animControlIndex", indexText);
        Maud.gui.setButtonLabel("animControlNextButton", nButton);
        Maud.gui.setButtonLabel("animControlSelectButton", sButton);
    }

    /**
     * Update the index status and previous/next buttons.
     */
    private void updateIndex() {
        String indexText;
        String lButton = "";
        String nButton = "";
        String pButton = "";

        LoadedCgm target = Maud.getModel().getTarget();
        if (target.isAnimControlSelected()) {
            lButton = "Load";
            int numAnimations = target.countAnimations();
            if (target.animation.isReal()) {
                int selectedIndex = target.animation.findIndex();
                indexText = String.format("#%d of %d", selectedIndex + 1,
                        numAnimations);
                nButton = "+";
                pButton = "-";

            } else {
                if (numAnimations == 0) {
                    indexText = "no animations";
                } else if (numAnimations == 1) {
                    indexText = "one animation";
                } else {
                    indexText = String.format("%d animations", numAnimations);
                }
            }
        } else {
            indexText = "not selected";
        }

        Maud.gui.setStatusText("animationIndex", indexText);
        Maud.gui.setButtonLabel("animationNextButton", nButton);
        Maud.gui.setButtonLabel("animationPreviousButton", pButton);
        Maud.gui.setButtonLabel("animationLoadButton", lButton);
    }

    /**
     * Update the freeze/loop/pin/pong check boxes and the pause button label.
     */
    private void updateLooping() {
        LoadedCgm target = Maud.getModel().getTarget();
        boolean frozen = target.pose.isFrozen();
        Maud.gui.setChecked("freeze", frozen);
        boolean looping = target.animation.willContinue();
        Maud.gui.setChecked("loop", looping);
        boolean pinned = target.animation.isPinned();
        Maud.gui.setChecked("pin", pinned);
        boolean ponging = target.animation.willReverse();
        Maud.gui.setChecked("pong", ponging);

        String pButton = "";
        float duration = target.animation.getDuration();
        if (duration > 0f) {
            boolean paused = target.animation.isPaused();
            if (paused) {
                pButton = "Resume";
            } else {
                pButton = "Pause";
            }
        }
        Maud.gui.setButtonLabel("togglePauseButton", pButton);
    }

    /**
     * Update the name label and rename button label.
     */
    private void updateName() {
        String nameText, rButton;
        String name = Maud.getModel().getTarget().animation.getName();
        if (Maud.getModel().getTarget().animation.isReal()) {
            nameText = MyString.quote(name);
            rButton = "Rename";
        } else {
            nameText = name;
            rButton = "";
        }
        Maud.gui.setStatusText("animationName", " " + nameText);
        Maud.gui.setButtonLabel("animationRenameButton", rButton);
    }

    /**
     * Update the speed slider and its status label.
     */
    private void updateSpeed() {
        LoadedAnimation animation = Maud.getModel().getTarget().animation;
        float duration = animation.getDuration();
        Slider slider = Maud.gui.getSlider("speed");
        if (duration > 0f) {
            slider.enable();
        } else {
            slider.disable();
        }

        float speed = animation.getSpeed();
        slider.setValue(speed);
        Maud.gui.updateSliderStatus("speed", speed, "x");
    }

    /**
     * Update the track counts.
     */
    private void updateTrackCounts() {
        LoadedAnimation animation = Maud.getModel().getTarget().animation;
        int numBoneTracks = animation.countBoneTracks();
        String boneTracksText = String.format("%d", numBoneTracks);
        Maud.gui.setStatusText("boneTracks", " " + boneTracksText);

        int numTracks = animation.countTracks();
        int numOtherTracks = numTracks - numBoneTracks;
        String otherTracksText = String.format("%d", numOtherTracks);
        Maud.gui.setStatusText("otherTracks", " " + otherTracksText);
    }

    /**
     * Update the track-time slider and its status label.
     */
    private void updateTrackTime() {
        LoadedAnimation animation = Maud.getModel().getTarget().animation;
        /*
         * slider
         */
        boolean moving = animation.isMoving();
        float duration = animation.getDuration();
        Slider slider = Maud.gui.getSlider("time");
        if (duration == 0f || moving) {
            slider.disable();
        } else {
            slider.enable();
        }

        float trackTime;
        if (duration == 0f) {
            trackTime = 0f;
            slider.setValue(0f);
        } else {
            trackTime = animation.getTime();
            float fraction = trackTime / duration;
            slider.setValue(fraction);
        }
        /*
         * status label
         */
        String statusText;
        if (animation.isReal()) {
            statusText = String.format("time = %.3f / %.3f sec",
                    trackTime, duration);
        } else {
            statusText = "time = n/a";
        }
        Maud.gui.setStatusText("trackTime", statusText);
    }
}