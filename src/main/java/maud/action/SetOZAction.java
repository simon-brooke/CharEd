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

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.shader.VarType;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.math.MyColor;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;
import jme3utilities.nifty.dialog.AllowNull;
import jme3utilities.nifty.dialog.VectorDialog;
import maud.CharEd;
import maud.dialog.EditorDialogs;
import maud.model.EditorModel;
import maud.model.WhichCgm;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.PlayOptions;
import maud.model.cgm.PlayTimes;
import maud.model.cgm.SelectedOverride;
import maud.model.option.RigidBodyParameter;
import maud.model.option.RotationDisplayMode;
import maud.model.option.ShapeParameter;
import maud.model.option.scene.SkeletonColors;

/**
 * Process actions that start with the word "set" and a letter in the o-z range.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SetOZAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SetOZAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SetOZAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "set" and a letter in
     * the o-z range.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean process(String actionString) {
        boolean handled = true;

        EditorModel model = CharEd.getModel();
        EditableCgm target = model.getTarget();
        switch (actionString) {
            case Action.setOverrideValue:
                SelectedOverride override = target.getOverride();
                if (override.isSelected()) {
                    String parameterName = override.parameterName();
                    VarType varType = override.varType();
                    Object oldValue = override.getValue();
                    EditorDialogs.setMatParamValue(parameterName, varType,
                            oldValue, AllowNull.Yes,
                            ActionPrefix.setOverrideValue);
                }
                break;

            case Action.setPcoParmValue:
                RigidBodyParameter rbp = model.getMisc().rbParameter();
                EditorDialogs.setPhysicsRbpValue(rbp);
                break;

            case Action.setShapeParmValue:
                ShapeParameter shapeParameter
                        = model.getMisc().shapeParameter();
                EditorDialogs.setShapeParameterValue(shapeParameter);
                break;

            case Action.setSmartNodeTranslation:
                EditorDialogs.setSpatialTranslation(
                        ActionPrefix.setSmartNodeTranslation);
                break;

            case Action.setSpatialAngleCardinal:
                target.getSpatial().cardinalizeRotation();
                break;

            case Action.setSpatialAngleSnapX:
                target.getSpatial().snapRotation(PhysicsSpace.AXIS_X);
                break;

            case Action.setSpatialAngleSnapY:
                target.getSpatial().snapRotation(PhysicsSpace.AXIS_Y);
                break;

            case Action.setSpatialAngleSnapZ:
                target.getSpatial().snapRotation(PhysicsSpace.AXIS_Z);
                break;

            case Action.setSpatialRotation:
                EditorDialogs.setSpatialRotation();
                break;

            case Action.setSpatialScale:
                EditorDialogs.setSpatialScale();
                break;

            case Action.setSpatialTranslation:
                EditorDialogs.setSpatialTranslation(
                        ActionPrefix.setSpatialTranslation);
                break;

            case Action.setTextureClone:
                target.getTexture().cloneFirstRef();
                break;

            case Action.setTextureNull:
                target.getTexture().nullifyFirst();
                break;

            case Action.setTimeLimitLower:
                Cgm cgm = CharEd.gui.mouseCgm();
                if (cgm != null) {
                    PlayOptions play = cgm.getPlay();
                    float currentTime = play.getTime();
                    if (currentTime <= play.getUpperLimit()) {
                        play.setLowerLimit(currentTime);
                    }
                }
                break;

            case Action.setTimeLimitUpper:
                cgm = CharEd.gui.mouseCgm();
                if (cgm != null) {
                    PlayOptions play = cgm.getPlay();
                    float currentTime = play.getTime();
                    if (currentTime >= play.getLowerLimit()) {
                        play.setUpperLimit(currentTime);
                    }
                }
                break;

            case Action.setTrackRotationAll:
                target.getTrack().setRotationAll();
                break;

            case Action.setTrackScaleAll:
                target.getTrack().setScaleAll();
                break;

            case Action.setTrackTranslationAll:
                target.getTrack().setTranslationAll();
                break;

            case Action.setTwistCardinal:
                model.getMap().cardinalizeTwist();
                break;

            case Action.setTwistSnapX:
                model.getMap().snapTwist(PhysicsSpace.AXIS_X);
                break;

            case Action.setTwistSnapY:
                model.getMap().snapTwist(PhysicsSpace.AXIS_Y);
                break;

            case Action.setTwistSnapZ:
                model.getMap().snapTwist(PhysicsSpace.AXIS_Z);
                break;

            case Action.setUserData:
                EditorDialogs.setUserData();
                break;

            default:
                handled = testForPrefixes(actionString);
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process an ongoing action that starts with the word "set" and a letter in
     * the o-z range -- 2nd part: test for prefixes.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean testForPrefixes(String actionString) {
        boolean handled = true;

        EditorModel model = CharEd.getModel();
        EditableCgm target = model.getTarget();
        String arg;
        if (actionString.startsWith(ActionPrefix.setOverrideValue)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setOverrideValue);
            target.getOverride().setValue(arg);

        } else if (actionString.startsWith(ActionPrefix.setPcoParmValue)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setPcoParmValue);
            String[] args = arg.split(" ");
            if (args.length == 2) {
                RigidBodyParameter parm = RigidBodyParameter.valueOf(args[0]);
                float value = Float.parseFloat(args[1]);
                target.getPco().setParameter(parm, value);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setPlatformDiameter)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setPlatformDiameter);
            String[] args = arg.split(" ");
            if (args.length == 2) {
                WhichCgm which = WhichCgm.valueOf(args[0]);
                float diameter = Float.parseFloat(args[1]);
                model.getScene().setPlatformDiameter(which, diameter);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setShapeParmValue)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setShapeParmValue);
            String[] args = arg.split(" ");
            if (args.length == 2) {
                ShapeParameter parm = ShapeParameter.valueOf(args[0]);
                float value = Float.parseFloat(args[1]);
                target.getShape().setParameter(parm, value);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setSkeletonColor)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSkeletonColor);
            String[] args = arg.split(" ");
            if (args.length >= 2) {
                SkeletonColors use = SkeletonColors.valueOf(args[0]);
                String colorText = MyString.remainder(arg, args[0] + " ");
                ColorRGBA color = MyColor.parse(colorText);
                model.getScene().getSkeleton().setColor(use, color);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setSkeletonLineWidth)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSkeletonLineWidth);
            float width = Float.parseFloat(arg);
            model.getScene().getSkeleton().setLineWidth(width);

        } else if (actionString.startsWith(ActionPrefix.setSkeletonPointSize)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSkeletonPointSize);
            float pointSize = Float.parseFloat(arg);
            model.getScene().getSkeleton().setPointSize(pointSize);

        } else if (actionString.startsWith(
                ActionPrefix.setSmartNodeTranslation)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSmartNodeTranslation);
            Vector3f newValue = (Vector3f) VectorDialog.parseVector(arg);
            target.translateSmartNode(newValue);

        } else if (actionString.startsWith(ActionPrefix.setSpatialRotation)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSpatialRotation);
            Vector3f vector = (Vector3f) VectorDialog.parseVector(arg);
            Quaternion q = new Quaternion();
            RotationDisplayMode mode = model.getMisc().rotationDisplayMode();
            switch (mode) {
                case Degrees:
                    vector.multLocal(FastMath.DEG_TO_RAD);
                    q.fromAngles(vector.x, vector.y, vector.z);
                    break;
                case Radians:
                    q.fromAngles(vector.x, vector.y, vector.z);
                    break;
                case QuatCoeff:
                    double ssq = MyVector3f.lengthSquared(vector);
                    if (ssq > 1.0) {
                        q.set(vector.x, vector.y, vector.z, 0f);
                        MyQuaternion.normalizeLocal(q);
                    } else {
                        double w = Math.sqrt(1.0 - ssq);
                        q.set(vector.x, vector.y, vector.z, (float) w);
                    }
                    break;
                default:
                    throw new IllegalStateException("mode = " + mode);
            }
            target.setSpatialRotation(q);

        } else if (actionString.startsWith(ActionPrefix.setSpatialScale)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSpatialScale);
            Vector3f newValue = (Vector3f) VectorDialog.parseVector(arg);
            target.setSpatialScale(newValue);

        } else if (actionString.startsWith(
                ActionPrefix.setSpatialTranslation)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setSpatialTranslation);
            Vector3f newValue = (Vector3f) VectorDialog.parseVector(arg);
            target.setSpatialTranslation(newValue);

        } else if (actionString.startsWith(ActionPrefix.setSubmenuWarp)) {
            arg = MyString.remainder(actionString, ActionPrefix.setSubmenuWarp);
            String[] args = arg.split(" ");
            if (args.length == 2) {
                float x = Float.parseFloat(args[0]);
                float y = Float.parseFloat(args[1]);
                model.getMisc().setSubmenuWarp(x, y);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setTime)) {
            arg = MyString.remainder(actionString, ActionPrefix.setTime);
            String[] args = arg.split(" ");
            if (args.length < 2) {
                handled = false;
            } else {
                WhichCgm whichCgm = WhichCgm.valueOf(args[0]);
                PlayTimes whichTime = PlayTimes.valueOf(args[1]);
                if (args.length == 2) {
                    EditorDialogs.setTime(whichCgm, whichTime);
                } else if (args.length == 3) {
                    float newValue = Float.parseFloat(args[2]);
                    Cgm cgm = model.getCgm(whichCgm);
                    cgm.getPlay().setTime(whichTime, newValue);
                } else {
                    handled = false;
                }
            }

        } else if (actionString.startsWith(ActionPrefix.setTimeToFrame)) {
            arg = MyString.remainder(actionString, ActionPrefix.setTimeToFrame);
            String[] args = arg.split(" ");
            if (args.length < 2) {
                handled = false;
            } else {
                WhichCgm whichCgm = WhichCgm.valueOf(args[0]);
                PlayTimes whichTime = PlayTimes.valueOf(args[1]);
                if (args.length == 2) {
                    EditorDialogs.setTimeToKeyframe(whichCgm, whichTime);
                } else if (args.length == 3) {
                    int indexBase = model.getMisc().indexBase();
                    int index = Integer.parseInt(args[2]) - indexBase;
                    Cgm cgm = model.getCgm(whichCgm);
                    float newValue = cgm.getTrack().keyframeTime(index);
                    cgm.getPlay().setTime(whichTime, newValue);
                } else {
                    handled = false;
                }
            }

        } else if (actionString.startsWith(ActionPrefix.setUserData)) {
            arg = MyString.remainder(actionString, ActionPrefix.setUserData);
            target.getUserData().setValue(arg);

        } else if (actionString.startsWith(ActionPrefix.setVertexData)) {
            arg = MyString.remainder(actionString, ActionPrefix.setVertexData);
            String[] args = arg.split(" ");
            int componentIndex = Integer.parseInt(args[0]);
            if (args.length == 1) {
                EditorDialogs.setVertexData(componentIndex);
            } else if (args.length == 2) {
                float value = Float.parseFloat(args[1]);
                target.getVertex().setComponent(componentIndex, value);
            } else {
                handled = false;
            }

        } else if (actionString.startsWith(ActionPrefix.setVertexPointSize)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setVertexPointSize);
            float size = Float.parseFloat(arg);
            model.getScene().getVertex().setPointSize(size);

        } else if (actionString.startsWith(
                ActionPrefix.setVertexPositionTolerance)) {
            arg = MyString.remainder(actionString,
                    ActionPrefix.setVertexPositionTolerance);
            float tolerance = Float.parseFloat(arg);
            model.getMisc().setVertexPositionTolerance(tolerance);

        } else if (actionString.startsWith(ActionPrefix.setXBoundary)) {
            arg = MyString.remainder(actionString, ActionPrefix.setXBoundary);
            float position = Float.parseFloat(arg);
            model.getMisc().setXBoundary(position);

        } else {
            handled = false;
        }

        return handled;
    }
}
