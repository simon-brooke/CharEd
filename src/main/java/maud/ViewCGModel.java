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
package maud;

import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;
import jme3utilities.MySkeleton;
import jme3utilities.MySpatial;
import jme3utilities.Validate;
import jme3utilities.debug.SkeletonDebugControl;

/**
 * Encapsulate a view's copy of the loaded CG model in Maud's "3D View" screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ViewCGModel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            ViewCGModel.class.getName());
    // *************************************************************************
    // fields

    /**
     * the application's asset manager
     */
    final private AssetManager assetManager;
    /**
     * the parent node of the CG model, used for rotation
     */
    final private Node parentNode;
    /**
     * the skeleton of this view's copy of the CG model
     */
    private Skeleton skeleton = null;
    /**
     * the skeleton control in this view's copy of the CG model
     */
    private SkeletonControl skeletonControl = null;
    /**
     * the skeleton debug control in this view's copy of the CG model
     */
    private SkeletonDebugControl skeletonDebugControl = null;
    /**
     * the root spatial in this view's copy of the CG model
     */
    private Spatial cgModelRoot;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new view.
     *
     * @param assetManager the asset manager (not null)
     * @param parentNode parent node in the scene graph (not null)
     * @param cgmRoot root spatial of the CG model (may be null)
     */
    ViewCGModel(AssetManager assetManager, Node parentNode, Spatial cgmRoot) {
        assert assetManager != null;
        assert parentNode != null;

        this.assetManager = assetManager;
        this.parentNode = parentNode;
        cgModelRoot = cgmRoot;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Calculate the location of an indexed bone, for selection.
     *
     * @param boneIndex which bone to locate
     * @return a new vector (in world coordinates)
     */
    Vector3f boneLocation(int boneIndex) {
        Bone bone = skeleton.getBone(boneIndex);
        Vector3f modelLocation = bone.getModelSpacePosition();
        Vector3f location = cgModelRoot.localToWorld(modelLocation, null);

        return location;
    }

    /**
     * Create a duplicate copy of this view, for checkpointing.
     */
    ViewCGModel createCopy() {
        Spatial cgmClone = cgModelRoot.clone();
        ViewCGModel result = new ViewCGModel(assetManager, parentNode,
                cgmClone);

        return result;
    }

    /**
     * Access the skeleton debug control for the CG model.
     *
     * @return the pre-existing instance (not null)
     */
    SkeletonDebugControl getSkeletonDebugControl() {
        assert skeletonDebugControl != null;
        return skeletonDebugControl;
    }

    /**
     * Access the root spatial of the CG model.
     *
     * @return the pre-existing instance (not null)
     */
    Spatial getSpatial() {
        assert cgModelRoot != null;
        return cgModelRoot;
    }

    /**
     * Replace the CG model with a newly loaded one.
     *
     * @param loadedRoot (not null)
     */
    public void loadModel(Spatial loadedRoot) {
        /*
         * Detach the old spatial (if any) from the scene.
         */
        if (cgModelRoot != null) {
            parentNode.detachChild(cgModelRoot);
        }
        cgModelRoot = loadedRoot;

        prepareForEditing();
    }

    /**
     * Copy a saved state to this one.
     *
     * @param savedState (not null)
     */
    void restore(ViewCGModel savedState) {
        assert cgModelRoot != null;
        /*
         * Detach the old spatial from the scene graph.
         */
        parentNode.detachChild(cgModelRoot);

        Spatial sp = savedState.getSpatial();
        cgModelRoot = sp.clone();
        parentNode.attachChild(cgModelRoot);

        skeletonControl = cgModelRoot.getControl(SkeletonControl.class);
        assert skeletonControl != null;

        skeleton = skeletonControl.getSkeleton();
        assert skeleton != null;

        skeletonDebugControl = cgModelRoot.getControl(
                SkeletonDebugControl.class);
        assert skeletonDebugControl != null;
    }

    /**
     * Rotate the CG model around +Y by the specified angle.
     *
     * @param angle in radians
     */
    void rotateY(float angle) {
        parentNode.rotate(0f, angle, 0f);
    }

    /**
     * Alter the cull hint of the selected spatial.
     *
     * @param newHint new value for cull hint (not null)
     */
    public void setHint(Spatial.CullHint newHint) {
        Validate.nonNull(newHint, "cull hint");

        Spatial spatial = Maud.model.spatial.findSpatial(cgModelRoot);
        spatial.setCullHint(newHint);
    }

    /**
     * Alter the shadow mode of the selected spatial.
     *
     * @param newMode new value for shadow mode (not null)
     */
    public void setMode(RenderQueue.ShadowMode newMode) {
        Validate.nonNull(newMode, "shadow mode");

        Spatial spatial = Maud.model.spatial.findSpatial(cgModelRoot);
        spatial.setShadowMode(newMode);
    }

    /**
     * Alter the local rotation of the selected spatial.
     *
     * @param rotation (not null, unaffected)
     */
    public void setSpatialRotation(Quaternion rotation) {
        Validate.nonNull(rotation, "rotation");

        Spatial spatial = Maud.model.spatial.findSpatial(cgModelRoot);
        spatial.setLocalRotation(rotation);
    }

    /**
     * Alter the local scale of the selected spatial.
     *
     * @param scale (not null, unaffected)
     */
    public void setSpatialScale(Vector3f scale) {
        Validate.nonNull(scale, "scale");

        Spatial spatial = Maud.model.spatial.findSpatial(cgModelRoot);
        spatial.setLocalScale(scale);
    }

    /**
     * Alter the local translation of the selected spatial.
     *
     * @param translation (not null, unaffected)
     */
    public void setSpatialTranslation(Vector3f translation) {
        Validate.nonNull(translation, "translation");

        Spatial spatial = Maud.model.spatial.findSpatial(cgModelRoot);
        spatial.setLocalTranslation(translation);
    }

    /**
     * Update the user transforms of all bones from the MVC model.
     */
    void updatePose() {
        int boneCount = Maud.model.cgm.countBones();
        int numTransforms = Maud.model.pose.countTransforms();
        assert numTransforms == boneCount : numTransforms;

        Transform transform = new Transform();
        Vector3f translation = new Vector3f();
        Quaternion rotation = new Quaternion();
        Vector3f scale = new Vector3f();

        for (int boneIndex = 0; boneIndex < boneCount; boneIndex++) {
            Maud.model.pose.copyTransform(boneIndex, transform);
            transform.getTranslation(translation);
            transform.getRotation(rotation);
            transform.getScale(scale);

            Bone bone = skeleton.getBone(boneIndex);
            bone.setUserTransforms(translation, rotation, scale);
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Alter a newly loaded CG model to prepare it for viewing and editing.
     */
    private void prepareForEditing() {
        /*
         * Attach the CG model to the scene graph and enable user control
         * for all bones.
         */
        parentNode.attachChild(cgModelRoot);
        MySkeleton.setUserControl(cgModelRoot, true);
        /*
         * Update references to skeleton control and skeleton.
         */
        skeletonControl = cgModelRoot.getControl(SkeletonControl.class);
        if (skeletonControl == null) {
            throw new IllegalArgumentException(
                    "expected the model's root to have a SkeletonControl");
        }
        skeleton = skeletonControl.getSkeleton();
        /*
         * Disable hardward skinning so that the raycast in
         * CursorTool.findContact() will work.
         */
        skeletonControl.setHardwareSkinningPreferred(false);
        /*
         * Configure the camera, cursor, and platform based on the range
         * of Y coordinates in the CG model.
         */
        float maxY = MySpatial.getMaxY(cgModelRoot);
        float minY = MySpatial.getMinY(cgModelRoot);
        float height = maxY - minY;
        assert height > 0f : height; // no 2D models!
        Vector3f baseLocation = new Vector3f(0f, minY, 0f);

        Maud.model.cursor.setLocation(baseLocation);
        Maud.model.misc.setPlatformLocation(baseLocation);
        Maud.model.misc.setPlatformDiameter(2f * height);

        Maud.model.camera.setScale(height);
        Vector3f cameraLocation = new Vector3f(-2.4f, 1f, 1.6f);
        cameraLocation.multLocal(height);
        cameraLocation.addLocal(baseLocation);
        Maud.model.camera.setLocation(cameraLocation);
        /*
         * Add a new SkeletonDebugControl.
         */
        skeletonDebugControl = new SkeletonDebugControl(assetManager);
        cgModelRoot.addControl(skeletonDebugControl);
        skeletonDebugControl.setEnabled(true);
    }
}
