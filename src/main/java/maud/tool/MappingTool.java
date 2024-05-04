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
import jme3utilities.nifty.Tool;
import maud.DescribeUtil;
import maud.CharEd;
import maud.model.EditableMap;
import maud.model.EditorModel;
import maud.model.LoadedMap;
import maud.model.cgm.Cgm;
import maud.model.cgm.SelectedBone;
import maud.model.cgm.SelectedSkeleton;

/**
 * The controller for the "Mapping" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class MappingTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MappingTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    MappingTool(GuiScreenController screenController) {
        super(screenController, "mapping");
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
        result.add("invertRma2");

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
        LoadedMap map = CharEd.getModel().getMap();
        switch (name) {
            case "invertRma2":
                map.setInvertMap(isChecked);
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateAsset();
        updateFeedback();
        updateIndex();
        updateSelected();
        updateSource();
        updateTarget();
        /*
         * the "use inverse" checkbox
         */
        EditorModel model = CharEd.getModel();
        boolean invertFlag = model.getMap().isInvertingMap();
        setChecked("invertRma2", invertFlag);
        /*
         * the "Show retargeted pose" button
         */
        String mButton;
        if (model.getTarget().getAnimation().isRetargetedPose()
                || !model.getSource().isLoaded()
                || !model.getTarget().getSkeleton().isSelected()) {
            mButton = "";
        } else {
            mButton = "Show retargeted pose";
        }
        setButtonText("loadRetargetedPose", mButton);
    }
    // *************************************************************************
    // private methods

    /**
     * Update the asset status.
     */
    private void updateAsset() {
        /*
         * asset-path status
         */
        EditableMap map = CharEd.getModel().getMap();
        String assetPath = map.getAssetPath();
        String assetDesc;
        if (assetPath.isEmpty()) {
            assetDesc = "unknown";
        } else {
            assetDesc = MyString.quote(assetPath);
        }
        setStatusText("mapAssetPath", " " + assetDesc);
        /*
         * pristine/edited status
         */
        String pristineDesc;
        int editCount = map.getEditState().countUnsavedEdits();
        if (editCount == 0) {
            pristineDesc = "pristine";
        } else if (editCount == 1) {
            pristineDesc = "one edit";
        } else {
            pristineDesc = String.format("%d edits", editCount);
        }
        setStatusText("mapPristine", pristineDesc);
    }

    /**
     * Update the feedback line.
     */
    private void updateFeedback() {
        String feedback;

        LoadedMap map = CharEd.getModel().getMap();
        Cgm source = CharEd.getModel().getSource();
        Cgm target = CharEd.getModel().getTarget();
        if (!target.getSkeleton().isSelected()) {
            feedback = "select the target skeleton";
        } else if (!source.isLoaded()) {
            feedback = "load the source model";
        } else if (!source.getSkeleton().isSelected()) {
            feedback = "select the source skeleton";
        } else if (map.isEmpty()) {
            feedback = "no bone mappings - load map or add";
        } else {
            float matchesSource = map.matchesSource();
            float matchesTarget = map.matchesTarget();
            if (matchesTarget >= 0.9995f) {
                if (matchesSource >= 0.9995f) {
                    feedback = "";
                } else if (matchesSource < 0.0005f) {
                    feedback = "doesn't match the source skeleton";
                } else {
                    feedback = String.format(
                            "%.1f%% matches the source skeleton",
                            100f * matchesSource);
                }

            } else if (matchesSource >= 0.9995f) {
                feedback = String.format(
                        "%.1f%% matches the target skeleton",
                        100f * matchesTarget);
            } else if (matchesSource < 0.0005f && matchesTarget < 0.0005f) {
                feedback = "doesn't match either skeleton";
            } else {
                feedback = String.format(
                        "%.1f%% matches source, %.1f%% matches target",
                        100f * matchesSource, 100f * matchesTarget);
            }
        }

        setStatusText("mappingFeedback", feedback);
    }

    /**
     * Update the index status and previous/next buttons.
     */
    private void updateIndex() {
        String indexStatus;
        String nextButton;
        String previousButton;

        LoadedMap map = CharEd.getModel().getMap();
        int numBoneMappings = map.countMappings();
        if (map.isBoneMappingSelected()) {
            int index = map.findIndex();
            indexStatus = DescribeUtil.index(index, numBoneMappings);
            nextButton = "+";
            previousButton = "-";

        } else {
            if (numBoneMappings == 0) {
                indexStatus = "no mappings";
            } else if (numBoneMappings == 1) {
                indexStatus = "one mapping";
            } else {
                indexStatus = String.format("%d mappings", numBoneMappings);
            }
            nextButton = "";
            previousButton = "";
        }

        setStatusText("mappingIndex", indexStatus);
        setButtonText("mappingNext", nextButton);
        setButtonText("mappingPrevious", previousButton);
    }

    /**
     * Update map/unmap buttons.
     */
    private void updateSelected() {
        String mButton = "";
        String uButton = "";

        if (CharEd.getModel().getMap().isBoneMappingSelected()) {
            uButton = "Unmap";
        } else if (CharEd.getModel().getSource().getBone().isSelected()
                && CharEd.getModel().getTarget().getBone().isSelected()) {
            mButton = "Map";
        }

        setButtonText("addMapping", mButton);
        setButtonText("deleteMapping", uButton);
    }

    /**
     * Update the source-bone description and select button.
     */
    private void updateSource() {
        /*
         * description
         */
        Cgm source = CharEd.getModel().getSource();
        String sourceBoneDesc;
        if (source.getBone().isSelected()) {
            String sourceName = source.getBone().name();
            sourceBoneDesc = MyString.quote(sourceName);
            String target = CharEd.getModel().getMap().targetBoneName(sourceName);
            if (target != null) {
                sourceBoneDesc += String.format("  -> %s", target);
            }
        } else if (source.isLoaded()) {
            sourceBoneDesc = SelectedSkeleton.noBone;
        } else {
            sourceBoneDesc = "( no model )";
        }
        setStatusText("sourceBone", " " + sourceBoneDesc);
        /*
         * select button
         */
        String sButton;
        if (source.isLoaded()) {
            sButton = "Select";
        } else {
            sButton = "";
        }
        setButtonText("selectSourceBone", sButton);
    }

    /**
     * Update the target-bone description.
     */
    private void updateTarget() {
        String targetBoneDesc;

        SelectedBone bone = CharEd.getModel().getTarget().getBone();
        if (bone.isSelected()) {
            String targetName = bone.name();
            targetBoneDesc = MyString.quote(targetName);
            String source = CharEd.getModel().getMap().sourceBoneName(targetName);
            if (source != null) {
                targetBoneDesc += String.format("  <- %s", source);
            }
        } else {
            targetBoneDesc = SelectedSkeleton.noBone;
        }

        setStatusText("targetBone", " " + targetBoneDesc);
    }
}
