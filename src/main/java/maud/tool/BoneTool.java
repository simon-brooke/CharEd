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

import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;
import maud.DescribeUtil;
import maud.CharEd;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.LoadedAnimation;
import maud.model.cgm.SelectedBone;
import maud.model.cgm.SelectedSkeleton;

/**
 * The controller for the "Bone" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class BoneTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(BoneTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    BoneTool(GuiScreenController screenController) {
        super(screenController, "bone");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Callback to update this tool prior to rendering. (Invoked once per frame
     * while this tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateChildren();
        updateControlIndex();
        updateHasTrack();
        updateIndex();
        updateInfluence();
        updateName();
        updateParent();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the children status and button.
     */
    private void updateChildren() {
        String button = "";
        String status = "";

        SelectedBone selectedBone = CharEd.getModel().getTarget().getBone();
        if (selectedBone.isSelected()) {
            int numChildren = selectedBone.countChildren();
            if (numChildren > 1) {
                status = String.format("%d children", numChildren);
                button = "Select child";
            } else if (numChildren == 1) {
                String childName = selectedBone.childName(0);
                status = MyString.quote(childName);
                button = "Select child";
            } else {
                status = "none";
            }
        }

        setButtonText("boneSelectChild", button);
        setStatusText("boneChildren", " " + status);
    }

    /**
     * Update the skeleton control index status and previous/next/select
     * buttons.
     */
    private void updateControlIndex() {
        String indexStatus;
        String selectButton = "";
        String nextButton = "";
        String previousButton = "";

        Cgm cgm = CharEd.getModel().getTarget();
        int numSkeletons = cgm.countSkeletons();
        if (numSkeletons > 0) {
            selectButton = "Select armature";
            SelectedSkeleton skeleton = cgm.getSkeleton();
            if (skeleton.isSelected()) {
                int selectedIndex = skeleton.findIndex();
                indexStatus
                        = DescribeUtil.index(selectedIndex, numSkeletons);
                if (numSkeletons > 1) {
                    nextButton = "+";
                    previousButton = "-";
                }
            } else {
                if (numSkeletons == 1) {
                    indexStatus = "one armature";
                } else {
                    indexStatus
                            = String.format("%d armatures", numSkeletons);
                }
            }

        } else {
            indexStatus = "not skinned";
        }

        setButtonText("skeletonPrevious", previousButton);
        setStatusText("skeletonIndex", indexStatus);
        setButtonText("skeletonNext", nextButton);
        setButtonText("skeletonSelect", selectButton);
    }

    /**
     * Update the "has track" status and "select track" button.
     */
    private void updateHasTrack() {
        String button = "";
        String status = "";

        Cgm target = CharEd.getModel().getTarget();
        SelectedBone selectedBone = target.getBone();
        if (selectedBone.isSelected()) {
            LoadedAnimation animation = target.getAnimation();
            if (animation.isRetargetedPose()) {
                String name = selectedBone.name();
                if (CharEd.getModel().getMap().isBoneMapped(name)) {
                    status = "mapped";
                } else {
                    status = "unmapped";
                }
            } else if (animation.isBindPose()) {
                status = "(bind pose is loaded)";
            } else if (selectedBone.hasTrack()) {
                status = "has track in animation";
                button = "Select track";
            } else {
                status = "no track in animation";
            }
        }

        setButtonText("boneSelectTrack", button);
        setStatusText("boneHasTrack", " " + status);
    }

    /**
     * Update the index status and previous/next/select buttons.
     */
    private void updateIndex() {
        String indexStatus;
        String nextButton;
        String previousButton;
        String selectButton;

        Cgm target = CharEd.getModel().getTarget();
        int numBones = target.getSkeleton().countBones();
        if (target.getBone().isSelected()) {
            int selectedIndex = target.getBone().index();
            indexStatus = DescribeUtil.index(selectedIndex, numBones);
            nextButton = "+";
            previousButton = "-";
        } else {
            if (numBones == 0) {
                indexStatus = "no bones";
            } else if (numBones == 1) {
                indexStatus = "one bone";
            } else {
                indexStatus = String.format("%d bones", numBones);
            }
            nextButton = "";
            previousButton = "";
        }

        if (numBones == 0) {
            selectButton = "";
        } else {
            selectButton = "Select";
        }

        setStatusText("boneIndex", indexStatus);
        setButtonText("boneNext", nextButton);
        setButtonText("bonePrevious", previousButton);
        setButtonText("boneSelect", selectButton);
    }

    /**
     * Update the influence status.
     */
    private void updateInfluence() {
        String status = "";

        SelectedBone bone = CharEd.getModel().getTarget().getBone();
        if (bone.isSelected()) {
            boolean attachmentsNode = bone.influencesAttachmentsNode();
            boolean meshVertices = bone.influencesVertices();
            if (attachmentsNode) {
                if (meshVertices) {
                    status = "attachments and mesh vertices";
                } else {
                    status = "attachments only";
                }
            } else {
                if (meshVertices) {
                    status = "mesh vertices only";
                } else {
                    status = "none";
                }
            }
        }

        setStatusText("boneInfluence", " " + status);
    }

    /**
     * Update the name status and rename button.
     */
    private void updateName() {
        String nameStatus;
        String renameButton;

        SelectedBone bone = CharEd.getModel().getTarget().getBone();
        if (bone.isSelected()) {
            String name = bone.name();
            nameStatus = MyString.quote(name);
            renameButton = "Rename";

        } else {
            nameStatus = "(none selected)";
            renameButton = "";
        }

        setStatusText("boneName", " " + nameStatus);
        setButtonText("boneRename", renameButton);
    }

    /**
     * Update the parent status and button.
     */
    private void updateParent() {
        String button = "";
        String status = "";

        EditableCgm target = CharEd.getModel().getTarget();
        SelectedBone selectedBone = target.getBone();
        if (selectedBone.isSelected()) {
            if (selectedBone.isRootBone()) {
                int numRoots = target.getSkeleton().countRootBones();
                if (numRoots == 1) {
                    status = "none (the root)";
                } else {
                    status = String.format("none (one of %d roots)", numRoots);
                }
            } else {
                String parentName = selectedBone.parentName();
                status = MyString.quote(parentName);
                button = "Select parent";
            }
        }

        setButtonText("boneSelectParent", button);
        setStatusText("boneParent", " " + status);
    }
}
