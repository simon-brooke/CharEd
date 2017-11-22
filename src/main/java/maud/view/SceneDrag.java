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
package maud.view;

import com.jme3.math.FastMath;
import com.jme3.math.Line;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;
import jme3utilities.debug.AxesVisualizer;
import jme3utilities.math.MyVector3f;
import jme3utilities.wes.Pose;
import maud.Maud;
import maud.model.EditableMap;
import maud.model.EditorModel;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedBone;
import maud.model.cgm.SelectedPhysics;
import maud.model.option.scene.AxesDragEffect;
import maud.model.option.scene.AxesOptions;
import maud.model.option.scene.AxesSubject;

/**
 * Drag state for scene views.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SceneDrag {
    // *************************************************************************
    // constants and loggers

    /**
     * dummy axis index used to indicate that no axis is being dragged
     */
    final private static int noAxis = -1;
    /**
     * number of coordinate axes
     */
    final private static int numAxes = 3;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SceneDrag.class.getName());
    // *************************************************************************
    // fields

    /**
     * which direction the dragged axis is pointing (true &rarr; away from
     * camera, false &rarr; toward camera)
     */
    private static boolean dragFarSide;
    /**
     * which C-G model is being manipulated (true &rarr; source, false &rarr;
     * target)
     */
    private static boolean dragSourceCgm;
    /**
     * length of the axis arrow at previous update (in local units, &gt;0)
     */
    private static float previousLength;
    /**
     * index of the axis being dragged (&ge;0, &lt;numAxes) or noAxis for none
     */
    private static int dragAxisIndex = noAxis;
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SceneDrag() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Terminate axis dragging.
     */
    public static void clear() {
        dragAxisIndex = noAxis;
        assert !isActive();
    }

    /**
     * Read the index of the axis being dragged.
     *
     * @return axis index (&ge;0, &lt;numAxes)
     */
    static int getAxisIndex() {
        assert isActive();
        assert dragAxisIndex >= 0 : dragAxisIndex;
        assert dragAxisIndex < numAxes : dragAxisIndex;
        return dragAxisIndex;
    }

    /**
     * Access the C-G model that's being manipulated.
     *
     * @return the pre-existing instance
     */
    public static Cgm getCgm() {
        assert isActive();

        EditorModel model = Maud.getModel();
        Cgm result;
        if (dragSourceCgm) {
            result = model.getSource();
        } else {
            result = model.getTarget();
        }

        return result;
    }

    /**
     * Test whether axis dragging is active.
     *
     * @return true if selected, otherwise false
     */
    public static boolean isActive() {
        if (dragAxisIndex == noAxis) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the axis being dragged points away from the camera.
     *
     * @return true if pointing away from camera, otherwise false
     */
    static boolean isFarSide() {
        assert isActive();
        return dragFarSide;
    }

    /**
     * Start dragging the specified axis.
     *
     * @param axisIndex which axis to drag: 0&rarr;X, 1&rarr;Y, 2&rarr;Z
     * @param initialLength length of axis in local units (&gt;0)
     * @param cgm which C-G model (not null)
     * @param farSideFlag true &rarr; drag on the far side of the axis origin,
     * false to drag on near side
     */
    static void start(int axisIndex, float initialLength, Cgm cgm,
            boolean farSideFlag) {
        assert axisIndex >= 0 && axisIndex < numAxes : axisIndex;
        assert initialLength > 0f : initialLength;
        assert cgm != null;

        dragAxisIndex = axisIndex;
        previousLength = initialLength;
        dragFarSide = farSideFlag;
        if (cgm == Maud.getModel().getSource()) {
            dragSourceCgm = true;
        } else {
            dragSourceCgm = false;
        }
        assert isActive();
    }

    /**
     * Toggle which direction the dragged axis is pointing.
     */
    public static void toggleSide() {
        dragFarSide = !dragFarSide;
    }

    /**
     * While dragging an axis, update the subject in the MVC model.
     *
     * @param visualizer (not null, unaffected)
     * @param worldRay (not null, unaffected)
     */
    static void updateSubject(AxesVisualizer visualizer, Line worldLine) {
        /*
         * Calculate the old axis direction in world coordinates.
         */
        Spatial spatial = visualizer.getSpatial();
        Vector3f axesOrigin = spatial.getWorldTranslation();
        Vector3f oldTipWorld = visualizer.tipLocation(dragAxisIndex);
        Vector3f oldDirWorld = oldTipWorld.subtract(axesOrigin);
        float oldLengthWorld = oldDirWorld.length();

        Line axisLine;
        Vector3f newTipWorld;
        AxesOptions options = Maud.getModel().getScene().getAxes();
        AxesDragEffect effect = options.getDragEffect();
        switch (effect) {
            case None:
                break;

            case Rotate:
                /*
                 * Calculate the new axis direction in world,
                 * then local coordinates.
                 */
                newTipWorld = MyVector3f.lineMeetsSphere(worldLine, axesOrigin,
                        oldLengthWorld, dragFarSide);
                Vector3f newDirWorld = newTipWorld.subtract(axesOrigin);
                Vector3f newDirLocal = MyVector3f.localizeDirection(newDirWorld,
                        spatial, null);

                Vector3f oldDirLocal = MyVector3f.localizeDirection(oldDirWorld,
                        spatial, null);
                Vector3f cross = oldDirLocal.cross(newDirLocal);
                if (!MyVector3f.isZero(cross)) {
                    rotateSubject(cross);
                }
                break;

            case ScaleAll:
                /*
                 * Calculate the new axis length in local units.
                 */
                axisLine = new Line(axesOrigin, oldDirWorld);
                newTipWorld = MyVector3f.lineMeetsLine(axisLine, worldLine);
                if (newTipWorld != null) {
                    Vector3f newTipLocal
                            = spatial.worldToLocal(newTipWorld, null);
                    float newLengthLocal = newTipLocal.length();

                    float scaleFactor
                            = FastMath.abs(newLengthLocal / previousLength);
                    if (scaleFactor > 0f) {
                        scaleSubject(scaleFactor);
                        previousLength = newLengthLocal;
                    }
                }
                break;

            case Translate:
                /*
                 * Calculate the axis displacement in local coordinates.
                 */
                axisLine = new Line(axesOrigin, oldDirWorld);
                newTipWorld = MyVector3f.lineMeetsLine(axisLine, worldLine);
                if (newTipWorld != null) {
                    Vector3f newTipLocal
                            = spatial.worldToLocal(newTipWorld, null);
                    Vector3f oldTipLocal
                            = spatial.worldToLocal(oldTipWorld, null);
                    Vector3f displacement = newTipLocal.subtract(oldTipLocal);
                    
                    translateSubject(displacement);
                }
                break;

            default:
                throw new IllegalStateException();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Access the editable C-G model that's being manipulated.
     *
     * @return the pre-existing instance
     */
    private static EditableCgm getEditableCgm() {
        assert isActive();

        EditableCgm result = null;
        if (!dragSourceCgm) {
            result = Maud.getModel().getTarget();
        }

        return result;
    }

    /**
     * Rotate the visualized bone using the specified quaternion.
     *
     * @param rotation quaternion (not null, norm=1)
     */
    private static void rotateBone(Quaternion rotation) {
        Cgm cgm = getCgm();
        SelectedBone bone = cgm.getBone();
        int boneIndex = bone.getIndex();
        assert boneIndex != -1;
        EditorModel model = Maud.getModel();
        EditableMap map = model.getMap();
        EditableCgm target = model.getTarget();
        Pose pose = cgm.getPose().get();
        Quaternion oldUserRotation = pose.userRotation(boneIndex, null);

        Quaternion newUserRotation = null;
        if (bone.shouldEnableControls()) {
            /*
             * Apply the rotation to the selected bone in the displayed pose.
             */
            newUserRotation = oldUserRotation.mult(rotation);
            newUserRotation.normalizeLocal();
            pose.setRotation(boneIndex, newUserRotation);

        } else if (cgm == target
                && cgm.getAnimation().isRetargetedPose()
                && map.isBoneMappingSelected()) {
            /*
             * Apply the rotation to the target bone in the displayed pose.
             */
            newUserRotation = oldUserRotation.mult(rotation);
            pose.setRotation(boneIndex, newUserRotation);
        }

        if (newUserRotation != null && !bone.shouldEnableControls()) {
            assert target.getAnimation().isRetargetedPose();
            assert map.isBoneMappingSelected();
            /*
             * Infer a new effective twist for the selected bone mapping.
             */
            Quaternion sourceMo
                    = model.getSource().getBone().modelOrientation(null);
            Quaternion targetMo
                    = target.getBone().modelOrientation(null);
            Quaternion invSourceMo = sourceMo.inverse();
            Quaternion newEffectiveTwist = invSourceMo.mult(targetMo);
            map.setTwist(newEffectiveTwist);
        }
    }

    /**
     * Rotate the subject using the specified cross product.
     *
     * @param cross cross product of two unit vectors in local coordinates (not
     * null, length&gt;0, length&le;1)
     */
    private static void rotateSubject(Vector3f cross) {
        /*
         * Convert the cross product to a rotation quaternion.
         */
        double lengthSquared = MyVector3f.lengthSquared(cross);
        double dLength = Math.sqrt(lengthSquared);
        float fLength = (float) dLength;
        assert fLength > 0f : fLength;
        assert fLength <= 1f : fLength;
        Vector3f rotationAxis = cross.divide(fLength);
        float rotationAngle = (float) Math.asin(dLength);
        Quaternion rotation = new Quaternion();
        rotation.fromAngleNormalAxis(rotationAngle, rotationAxis);

        EditableCgm editableCgm = getEditableCgm();
        /*
         * Determine which MVC-model object the control is visualizing,
         * and rotate that object.
         */
        AxesSubject subject = Maud.getModel().getScene().getAxes().getSubject();
        switch (subject) {
            case ModelRoot:
                /*
                 * Apply the Y-axis rotation to the world transform.
                 */
                Cgm cgm = getCgm();
                CgmTransform cgmTransform = cgm.getSceneView().getTransform();
                float angle = FastMath.asin(cross.y * fLength);
                cgmTransform.rotateY(angle);
                break;

            case SelectedBone:
                rotateBone(rotation);
                break;

            case SelectedPhysics:
                if (editableCgm != null) {
                    SelectedPhysics physics = editableCgm.getPhysics();
                    if (physics.isRotatable()) {
                        /*
                         * Rotate the selected physics object.
                         */
                        Quaternion orientation = physics.orientation(null);
                        orientation.multLocal(rotation);
                        orientation.normalizeLocal();
                        editableCgm.setPhysicsOrientation(orientation);
                    }
                }
                break;

            case SelectedSpatial:
                if (editableCgm != null) {
                    /*
                     * Rotate the selected spatial.
                     */
                    Quaternion localRotation
                            = editableCgm.getSpatial().localRotation(null);
                    localRotation.multLocal(rotation);
                    localRotation.normalizeLocal();
                    editableCgm.setSpatialRotation(localRotation);
                }
                break;

            case World: // ignore attempts to drag the world axes
                break;

            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Scale the subject uniformly by the specified factor.
     *
     * @param factor scale factor (&gt;0, 1 &rarr; no effect)
     */
    private static void scaleSubject(float factor) {
        assert factor > 0f : factor;

        Cgm cgm = getCgm();
        /*
         * Determine which MVC-model object the control is visualizing,
         * and scale that object.
         */
        AxesSubject subject = Maud.getModel().getScene().getAxes().getSubject();
        switch (subject) {
            case ModelRoot:
                /*
                 * Scale the world transform.
                 */
                CgmTransform cgmTransform = cgm.getSceneView().getTransform();
                cgmTransform.scale(factor);
                break;

            case SelectedBone:
                int boneIndex = cgm.getBone().getIndex();
                if (cgm.getBone().shouldEnableControls()) {
                    /*
                     * Scale the selected bone in the displayed pose.
                     */
                    Pose pose = cgm.getPose().get();
                    Vector3f userScale = pose.userScale(boneIndex, null);
                    userScale.multLocal(factor);
                    pose.setScale(boneIndex, userScale);
                }
                break;

            case SelectedPhysics:
                /*
                 * Ignore attempts to scale the physics object directly
                 * -- user should scale its shape instead.
                 */
                break;

            case SelectedSpatial:
                EditableCgm editableCgm = getEditableCgm();
                if (editableCgm != null) {
                    /*
                     * Scale the selected spatial.
                     */
                    Vector3f localScale = cgm.getSpatial().localScale(null);
                    localScale.multLocal(factor);
                    editableCgm.setSpatialScale(localScale);
                }
                break;

            case World: // ignore attempts to drag the world axes
                break;

            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Translate the subject by the specified offset.
     *
     * @param offset (in local coordinates, not null, unaffected)
     */
    private static void translateSubject(Vector3f offset) {
        EditableCgm editableCgm = getEditableCgm();
        /*
         * Determine which MVC-model object the control is visualizing,
         * and translate that object.
         */
        AxesSubject subject = Maud.getModel().getScene().getAxes().getSubject();
        switch (subject) {
            case ModelRoot: // ignore attempts to translate the model root
                break;

            case SelectedBone: // TODO
                break;

            case SelectedPhysics:
                SelectedPhysics physics = editableCgm.getPhysics();
                if (physics.isRotatable()) {
                    /*
                     * Translate the selected physics object.
                     */
                    Vector3f location = physics.location(null);
                    location.addLocal(offset);
                    editableCgm.setPhysicsLocation(location);
                }
                break;

            case SelectedSpatial:
                if (editableCgm != null) {
                    /*
                     * Translate the selected spatial.
                     */
                    Vector3f localTranslation
                            = editableCgm.getSpatial().localTranslation(null);
                    localTranslation.addLocal(offset);
                    editableCgm.setSpatialTranslation(localTranslation);
                }
                break;

            case World: // ignore attempts to drag the world axes
                break;

            default:
                throw new IllegalStateException();
        }
    }
}
