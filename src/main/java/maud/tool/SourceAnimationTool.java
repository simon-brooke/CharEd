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

import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.SliderTransform;
import jme3utilities.nifty.Tool;
import maud.DescribeUtil;
import maud.CharEd;
import maud.model.cgm.Cgm;
import maud.model.cgm.LoadedAnimation;
import maud.model.cgm.PlayOptions;
import maud.model.cgm.SelectedAnimControl;

/**
 * The controller for the "Source-Animation" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SourceAnimationTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SourceAnimationTool.class.getName());
    /**
     * transform for the speed slider
     */
    final private static SliderTransform speedSt = SliderTransform.None;
    /**
     * transform for the time slider
     */
    final private static SliderTransform timeSt = SliderTransform.None;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    SourceAnimationTool(GuiScreenController screenController) {
        super(screenController, "sourceAnimation");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's check boxes.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listCheckBoxes() {
        List<String> result = super.listCheckBoxes();
        result.add("loopSource");
        result.add("pinSource");
        result.add("pongSource");

        return result;
    }

    /**
     * Enumerate this tool's sliders.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listSliders() {
        List<String> result = super.listSliders();
        result.add("sSpeed");
        result.add("sourceTime");

        return result;
    }

    /**
     * Update the MVC model based on a check-box event.
     *
     * @param name the name (unique id prefix) of the checkbox
     * @param isChecked the new state of the checkbox (true&rarr;checked,
     * false&rarr;unchecked)
     */
    @Override
    public void onCheckBoxChanged(String name, boolean isChecked) {
        Cgm cgm = CharEd.getModel().getSource();
        PlayOptions play = cgm.getPlay();

        switch (name) {
            case "loopSource":
                play.setContinue(isChecked);
                break;

            case "pinSource":
                play.setPinned(isChecked);
                break;

            case "pongSource":
                play.setReverse(isChecked);
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Update the MVC model based on the sliders.
     *
     * @param sliderName the name (unique id prefix) of the slider (not null)
     */
    @Override
    public void onSliderChanged(String sliderName) {
        Cgm cgm = CharEd.getModel().getSource();

        float duration = cgm.getAnimation().duration();
        if (duration > 0f) {
            float speed = readSlider("sSpeed", speedSt);
            cgm.getPlay().setSpeed(speed);
        }

        boolean moving = cgm.getAnimation().isMoving();
        if (!moving) {
            float fraction = readSlider("sourceTime", timeSt);
            float time = fraction * duration;
            cgm.getPlay().setTime(time);
        }
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateControlIndex();
        updateIndex();
        updateLooping();
        updateName();
        updateSpeed();
        updateTrackTime();
        updateTrackCounts();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the anim control index status and previous/next/select buttons.
     */
    private void updateControlIndex() {
        String indexStatus;
        String selectButton = "";
        String nextButton = "";
        String previousButton = "";

        Cgm cgm = CharEd.getModel().getSource();
        int numAnimControls = cgm.countAnimationControls();
        if (numAnimControls > 0) {
            selectButton = "Select animControl";
            SelectedAnimControl animControl = cgm.getAnimControl();
            if (animControl.isSelected()) {
                int selectedIndex = animControl.findIndex();
                indexStatus = DescribeUtil.index(selectedIndex,
                        numAnimControls);
                if (numAnimControls > 1) {
                    nextButton = "+";
                    previousButton = "-";
                }
            } else {
                if (numAnimControls == 1) {
                    indexStatus = "one AnimControl";
                } else {
                    indexStatus
                            = String.format("%d AnimControls", numAnimControls);
                }
            }

        } else if (cgm.isLoaded()) {
            indexStatus = "not animated";

        } else {
            indexStatus = "no model loaded";
        }

        setButtonText("sourceAnimControlPrevious", previousButton);
        setStatusText("sourceAnimControlIndex", indexStatus);
        setButtonText("sourceAnimControlNext", nextButton);
        setButtonText("sourceAnimControlSelect", selectButton);
    }

    /**
     * Update the index status and previous/next/load buttons.
     */
    private void updateIndex() {
        String indexStatus;
        String loadButton = "";
        String nextButton = "";
        String previousButton = "";

        Cgm cgm = CharEd.getModel().getSource();
        SelectedAnimControl animControl = cgm.getAnimControl();
        if (animControl.isSelected()) {
            loadButton = "Load animation";
            int numAnimations = animControl.countRealAnimations();
            if (cgm.getAnimation().isReal()) {
                int selectedIndex = cgm.getAnimation().findIndex();
                indexStatus = DescribeUtil.index(selectedIndex, numAnimations);
                if (numAnimations > 1) {
                    nextButton = "+";
                    previousButton = "-";
                }
            } else {
                if (numAnimations == 0) {
                    indexStatus = "no animations";
                } else if (numAnimations == 1) {
                    indexStatus = "one animation";
                } else {
                    indexStatus = String.format("%d animations", numAnimations);
                }
            }
        } else if (cgm.isLoaded()) {
            indexStatus = "not selected";
        } else {
            indexStatus = "no model";
        }

        setButtonText("sourceAnimationLoad", loadButton);
        setButtonText("sourceAnimationNext", nextButton);
        setButtonText("sourceAnimationPrevious", previousButton);
        setStatusText("sourceAnimationIndex", indexStatus);
    }

    /**
     * Update the loop/pin/pong check boxes and the pause button label.
     */
    private void updateLooping() {
        Cgm cgm = CharEd.getModel().getSource();
        PlayOptions play = cgm.getPlay();
        boolean pinned = play.isPinned();
        setChecked("pinSource", pinned);

        boolean looping = play.willContinue();
        setChecked("loopSource", looping);
        boolean ponging = play.willReverse();
        setChecked("pongSource", ponging);

        String pauseButton = "";
        float duration = cgm.getAnimation().duration();
        if (duration > 0f) {
            boolean paused = play.isPaused();
            if (paused) {
                pauseButton = "Resume";
            } else {
                pauseButton = "Pause";
            }
        }
        setButtonText("togglePauseSource", pauseButton);
    }

    /**
     * Update the name status.
     */
    private void updateName() {
        String nameText;

        Cgm cgm = CharEd.getModel().getSource();
        if (cgm.isLoaded()) {
            String name = cgm.getAnimation().name();
            if (cgm.getAnimation().isReal()) {
                nameText = MyString.quote(name);
            } else {
                nameText = name;
            }
        } else {
            nameText = "";
        }

        setStatusText("sourceAnimationName", " " + nameText);
    }

    /**
     * Update the speed slider and its status label.
     */
    private void updateSpeed() {
        Cgm cgm = CharEd.getModel().getSource();

        float duration = cgm.getAnimation().duration();
        setSliderEnabled("sSpeed", duration > 0f);

        float speed = cgm.getPlay().getSpeed();
        setSlider("sSpeed", speedSt, speed);
        updateSliderStatus("sSpeed", speed, "x");
    }

    /**
     * Update the track counts.
     */
    private void updateTrackCounts() {
        LoadedAnimation animation = CharEd.getModel().getSource().getAnimation();
        int numBoneTracks = animation.countBoneTracks();
        String boneTracksText = Integer.toString(numBoneTracks);
        setStatusText("sourceBoneTracks", boneTracksText);

        int numSpatialTracks = animation.countSpatialTracks();
        String spatialTracksText = Integer.toString(numSpatialTracks);
        setStatusText("sourceSpatialTracks", spatialTracksText);

        int numTracks = animation.countTracks();
        int numOtherTracks = numTracks - numBoneTracks - numSpatialTracks;
        String otherTracksText = String.format("%d", numOtherTracks);
        setStatusText("sourceOtherTracks", otherTracksText);
    }

    /**
     * Update the track-time slider and its status label.
     */
    private void updateTrackTime() {
        Cgm cgm = CharEd.getModel().getSource();
        LoadedAnimation animation = cgm.getAnimation();
        float duration = animation.duration();
        /*
         * slider
         */
        boolean moving = animation.isMoving();
        setSliderEnabled("sourceTime", duration != 0f && !moving);

        float fraction;
        float trackTime;
        if (duration == 0f) {
            trackTime = 0f;
            fraction = 0f;
        } else {
            trackTime = cgm.getPlay().getTime();
            fraction = trackTime / duration;
        }
        setSlider("sourceTime", timeSt, fraction);
        /*
         * status label
         */
        String statusText;
        if (cgm.isLoaded() && animation.isReal()) {
            statusText = String.format("time = %.3f / %.3f sec", trackTime,
                    duration);
        } else {
            statusText = "time = n/a";
        }
        setStatusText("sourceTrackTime", statusText);
    }
}
