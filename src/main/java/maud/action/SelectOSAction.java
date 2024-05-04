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
package maud.action;

import com.jme3.renderer.queue.RenderQueue;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyString;
import maud.CharEd;
import maud.menu.AnimationMenus;
import maud.menu.BoneMenus;
import maud.menu.EnumMenus;
import maud.menu.PhysicsMenus;
import maud.menu.ShowMenus;
import maud.menu.SpatialMenus;
import maud.menu.WhichSpatials;
import maud.model.EditorModel;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedSgc;
import maud.model.cgm.SelectedSpatial;
import maud.model.option.PerformanceMode;
import maud.model.option.RigidBodyParameter;
import maud.model.option.RotationDisplayMode;
import maud.model.option.ShapeParameter;
import maud.model.option.ShowBones;
import maud.model.option.scene.OrbitCenter;
import maud.model.option.scene.PlatformType;
import maud.model.option.scene.ProjectionMode;
import maud.model.option.scene.SkeletonColors;
import maud.tool.EditorTools;

/**
 * Process actions that start with the word "select" and a letter in the o-s
 * range.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SelectOSAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SelectOSAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SelectOSAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "select" and a letter
     * in the o-z range.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean process(String actionString) {
        boolean handled = true;

        EditorModel model = CharEd.getModel();
        Cgm target = model.getTarget();
        SelectedSpatial spatial = target.getSpatial();

        ShowBones currentOption;
        switch (actionString) {
            case Action.selectOrbitCenter:
                EnumMenus.selectOrbitCenter();
                break;

            case Action.selectOverride:
                ShowMenus.selectOverride();
                break;

            case Action.selectPco:
                PhysicsMenus.selectPco(target);
                break;

            case Action.selectPcoParm:
                PhysicsMenus.selectRbp("");
                break;

            case Action.selectPcoShape:
                if (target.getPco().isSelected()) {
                    target.getShape().selectPcoShape();
                    EditorTools.select("shape");
                }
                break;

            case Action.selectPlatformType:
                EnumMenus.selectPlatformType();
                break;

            case Action.selectQueueBucket:
                EnumMenus.selectQueueBucket();
                break;

            case Action.selectSceneBones:
                currentOption = model.getScene().getSkeleton().getShowBones();
                EnumMenus.selectShowBones(ActionPrefix.selectSceneBones,
                        currentOption);
                break;

            case Action.selectScoreBonesNone:
                currentOption = model.getScore().getShowNoneSelected();
                EnumMenus.selectShowBones(ActionPrefix.selectScoreBonesNone,
                        currentOption);
                break;

            case Action.selectScoreBonesWhen:
                currentOption = model.getScore().getShowWhenSelected();
                EnumMenus.selectShowBones(ActionPrefix.selectScoreBonesWhen,
                        currentOption);
                break;

            case Action.selectSgc:
                ShowMenus.selectSgc();
                break;

            case Action.selectSgcPco:
                SelectedSgc sgc = target.getSgc();
                String pcoName = sgc.pcoName();
                if (!pcoName.isEmpty() && sgc.isEnabled()) {
                    target.getPco().select(pcoName);
                    EditorTools.select("pco");
                }
                break;

            case Action.selectSgcSpatial:
                spatial.selectControlled();
                EditorTools.select("spatial");
                break;

            case Action.selectShadowMode:
                EnumMenus.selectShadowMode();
                break;

            case Action.selectShape:
                PhysicsMenus.selectShape(target);
                break;

            case Action.selectShapeChild:
                PhysicsMenus.selectShapeChild();
                break;

            case Action.selectShapeParm:
                PhysicsMenus.selectShapeParameter();
                break;

            case Action.selectShapeUser:
                PhysicsMenus.selectShapeUser();
                break;

            case Action.selectSkeleton:
                BoneMenus.selectSkeleton(model.getTarget());
                break;

            case Action.selectSkeletonColor:
                EnumMenus.selectSkeletonColor();
                break;

            case Action.selectSourceAnimControl:
                AnimationMenus.selectAnimControl(model.getSource());
                break;

            case Action.selectSourceBone:
                BoneMenus.selectSourceBone();
                break;

            case Action.selectSpatialChild:
                SpatialMenus.selectSpatialChild("");
                break;

            case Action.selectSpatialFirstSibling:
                spatial.selectSibling(0);
                break;

            case Action.selectSpatialLastSibling:
                int numSiblings = spatial.countSiblings();
                spatial.selectSibling(numSiblings - 1);
                break;

            case Action.selectSpatialNextSibling: {
                int siblingIndex = spatial.siblingIndex();
                spatial.selectSibling(siblingIndex + 1);
            }
            break;

            case Action.selectSpatialParent:
                spatial.selectParent();
                break;

            case Action.selectSpatialPreviousSibling: {
                int siblingIndex = spatial.siblingIndex();
                spatial.selectSibling(siblingIndex - 1);
            }
            break;

            case Action.selectSpatialSibling:
                SpatialMenus.selectSpatialSibling("");
                break;

            default:
                handled = testForPrefixes(actionString);
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process an ongoing action that starts with the word "select" and a letter
     * in the o-s range -- 2nd part: test for prefixes.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean testForPrefixes(String actionString) {
        boolean handled = true;

        EditorModel model = CharEd.getModel();
        EditableCgm target = model.getTarget();
        String arg;
        if (actionString.startsWith(ActionPrefix.selectOrbitCenter)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectOrbitCenter);
            OrbitCenter oc = OrbitCenter.parse(arg);
            model.getScene().getCamera().setMode(oc);

        } else if (actionString.startsWith(ActionPrefix.selectOverride)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectOverride);
            target.getOverride().select(arg);

        } else if (actionString.startsWith(
                ActionPrefix.selectPerformanceMode)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectPerformanceMode);
            PerformanceMode mode = PerformanceMode.valueOf(arg);
            model.getMisc().selectPerformanceMode(mode);

        } else if (actionString.startsWith(ActionPrefix.selectPco)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectPco);
            target.getPco().select(arg);

        } else if (actionString.startsWith(ActionPrefix.selectPcoParm)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectPcoParm);
            RigidBodyParameter rbp; // TODO utility in ParseUtil
            try {
                rbp = RigidBodyParameter.valueOf(arg);
            } catch (IllegalArgumentException exception) {
                rbp = null;
                PhysicsMenus.selectRbp(arg);
            }
            if (rbp != null) {
                model.getMisc().selectRbp(rbp);
            }

        } else if (actionString.startsWith(ActionPrefix.selectPlatformType)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectPlatformType);
            PlatformType type = PlatformType.valueOf(arg);
            model.getScene().setPlatformType(type);

        } else if (actionString.startsWith(ActionPrefix.selectProjection)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectProjection);
            ProjectionMode mode = ProjectionMode.valueOf(arg);
            model.getScene().getCamera().setMode(mode);

        } else if (actionString.startsWith(ActionPrefix.selectQueueBucket)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectQueueBucket);
            RenderQueue.Bucket value = RenderQueue.Bucket.valueOf(arg);
            target.setQueueBucket(value);

        } else if (actionString.startsWith(
                ActionPrefix.selectRotationDisplay)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectRotationDisplay);
            RotationDisplayMode mode = RotationDisplayMode.valueOf(arg);
            model.getMisc().selectRotationDisplay(mode);

        } else if (actionString.startsWith(ActionPrefix.selectSgc)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectSgc);
            target.getSgc().select(arg);

        } else if (actionString.startsWith(ActionPrefix.selectSceneBones)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSceneBones);
            ShowBones value = ShowBones.valueOf(arg);
            model.getScene().getSkeleton().setShowBones(value);

        } else if (actionString.startsWith(ActionPrefix.selectScoreBonesNone)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectScoreBonesNone);
            ShowBones value = ShowBones.valueOf(arg);
            model.getScore().setShowNoneSelected(value);

        } else if (actionString.startsWith(ActionPrefix.selectScoreBonesWhen)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectScoreBonesWhen);
            ShowBones value = ShowBones.valueOf(arg);
            model.getScore().setShowWhenSelected(value);

        } else if (actionString.startsWith(ActionPrefix.selectShadowMode)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectShadowMode);
            RenderQueue.ShadowMode value = RenderQueue.ShadowMode.valueOf(arg);
            target.setShadowMode(value);

        } else if (actionString.startsWith(ActionPrefix.selectShape)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectShape);
            int colonPosition = arg.indexOf(':');
            if (colonPosition == -1) {
                handled = false;
            } else {
                target.getShape().select(arg);
            }

        } else if (actionString.startsWith(ActionPrefix.selectShapeParm)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectShapeParm);
            ShapeParameter parameter = ShapeParameter.valueOf(arg);
            model.getMisc().selectShapeParameter(parameter);

        } else if (actionString.startsWith(ActionPrefix.selectShapeUser)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectShapeUser);
            PhysicsMenus.selectShapeUser(arg);

        } else if (actionString.startsWith(ActionPrefix.selectSkeleton)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectSkeleton);
            target.getSkeleton().select(arg);

        } else if (actionString.startsWith(ActionPrefix.selectSkeletonColor)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSkeletonColor);
            SkeletonColors editColor = SkeletonColors.valueOf(arg);
            model.getScene().getSkeleton().selectEditColor(editColor);

        } else if (actionString.startsWith(
                ActionPrefix.selectSourceAnimControl)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSourceAnimControl);
            model.getSource().getAnimControl().select(arg);

        } else if (actionString.startsWith(ActionPrefix.selectSourceBone)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSourceBone);
            BoneMenus.selectSourceBone(arg);

        } else if (actionString.startsWith(ActionPrefix.selectSpatial)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectSpatial);
            String whichName = arg.split(" ")[0];
            WhichSpatials which = WhichSpatials.valueOf(whichName);
            String name = "";
            if (arg.contains(" ")) {
                name = MyString.remainder(arg, whichName + " ");
            }
            SpatialMenus.selectSpatial(name, which);

        } else if (actionString.startsWith(ActionPrefix.selectSpatialChild)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSpatialChild);
            SelectedSpatial spatial = target.getSpatial();
            List<String> children = spatial.listNumberedChildren();
            int childIndex = children.indexOf(arg);
            if (childIndex >= 0) { // complete name+index
                spatial.selectChild(childIndex);
            } else { // prefix of name+index
                SpatialMenus.selectSpatialChild(arg);
            }

        } else if (actionString.startsWith(ActionPrefix.selectSpatialSibling)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.selectSpatialSibling);
            SelectedSpatial spatial = target.getSpatial();
            List<String> siblings = spatial.listNumberedSiblings();
            int siblingIndex = siblings.indexOf(arg);
            if (siblingIndex >= 0) { // complete name+index
                spatial.selectSibling(siblingIndex);
            } else { // prefix of name+index
                SpatialMenus.selectSpatialSibling(arg);
            }

        } else {
            handled = false;
        }

        return handled;
    }
}
