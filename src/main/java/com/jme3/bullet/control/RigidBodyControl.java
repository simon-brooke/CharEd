/*
 * Copyright (c) 2009-2018 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.bullet.control;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.io.IOException;
import java.util.logging.Logger;
import jme3utilities.MySpatial;
import maud.PhysicsUtil;

/**
 * A physics control to link a PhysicsRigidBody to a spatial.
 * <p>
 * This class is shared between JBullet and Native Bullet.
 *
 * @author normenhansen
 */
public class RigidBodyControl extends PhysicsRigidBody
        implements PhysicsControl, JmeCloneable {

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(RigidBodyControl.class.getName());
    /**
     * local copy of {@link com.jme3.math.Quaternion#IDENTITY}
     */
    final private static Quaternion rotateIdentity = new Quaternion();
    /**
     * local copy of {@link com.jme3.math.Vector3f#ZERO}
     */
    final private static Vector3f translateIdentity = new Vector3f(0f, 0f, 0f);

    protected Spatial spatial;
    protected boolean enabled = true;
    protected boolean added = false;
    protected PhysicsSpace space = null;
    protected boolean kinematicSpatial = true;

    /**
     * No-argument constructor needed by SavableClassUtil. Do not invoke
     * directly!
     */
    public RigidBodyControl() {
    }

    /**
     * When using this constructor, the CollisionShape for the RigidBody is
     * generated automatically when the control is added to a spatial.
     *
     * @param mass When not 0, a HullCollisionShape is generated, otherwise a
     * MeshCollisionShape is used. For geometries with box or sphere meshes the
     * proper box or sphere collision shape is used.
     */
    public RigidBodyControl(float mass) {
        this.mass = mass;
    }

    /**
     * Create a new control with mass=1 and the specified collision shape.
     *
     * @param shape the desired shape (not null, alias created)
     */
    public RigidBodyControl(CollisionShape shape) {
        super(shape);
    }

    /**
     * Create a new control with the specified collision shape and mass.
     *
     * @param shape the desired shape (not null, alias created)
     * @param mass the desired mass (&ge;0)
     */
    public RigidBodyControl(CollisionShape shape, float mass) {
        super(shape, mass);
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        RigidBodyControl control = new RigidBodyControl(collisionShape, mass);
        control.setAngularFactor(getAngularFactor());
        control.setAngularSleepingThreshold(getAngularSleepingThreshold());
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setDamping(getLinearDamping(), getAngularDamping());
        control.setFriction(getFriction());
        control.setGravity(getGravity());
        control.setKinematic(isKinematic());
        control.setKinematicSpatial(isKinematicSpatial());
        control.setLinearSleepingThreshold(getLinearSleepingThreshold());
        control.setPhysicsLocation(getPhysicsLocation(null));
        control.setPhysicsRotation(getPhysicsRotationMatrix(null));
        control.setRestitution(getRestitution());

        if (mass > 0) {
            control.setAngularVelocity(getAngularVelocity());
            control.setLinearVelocity(getLinearVelocity());
        }
        control.setApplyPhysicsLocal(isApplyPhysicsLocal());
        return control;
    }

    @Override
    public Object jmeClone() {
        RigidBodyControl control = new RigidBodyControl(collisionShape, mass);
        control.setAngularFactor(getAngularFactor());
        control.setAngularSleepingThreshold(getAngularSleepingThreshold());
        control.setCcdMotionThreshold(getCcdMotionThreshold());
        control.setCcdSweptSphereRadius(getCcdSweptSphereRadius());
        control.setCollideWithGroups(getCollideWithGroups());
        control.setCollisionGroup(getCollisionGroup());
        control.setDamping(getLinearDamping(), getAngularDamping());
        control.setFriction(getFriction());
        control.setGravity(getGravity());
        control.setKinematic(isKinematic());
        control.setKinematicSpatial(isKinematicSpatial());
        control.setLinearSleepingThreshold(getLinearSleepingThreshold());
        control.setPhysicsLocation(getPhysicsLocation(null));
        control.setPhysicsRotation(getPhysicsRotationMatrix(null));
        control.setRestitution(getRestitution());

        if (mass > 0) {
            control.setAngularVelocity(getAngularVelocity());
            control.setLinearVelocity(getLinearVelocity());
        }
        control.setApplyPhysicsLocal(isApplyPhysicsLocal());
        control.spatial = this.spatial;
        control.setEnabled(isEnabled());

        return control;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.spatial = cloner.clone(spatial);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        setUserObject(spatial);
        if (spatial == null) {
            return;
        }
        if (collisionShape == null) {
            createCollisionShape();
            rebuildRigidBody();
        }
        setPhysicsLocation(getSpatialTranslation());
        setPhysicsRotation(getSpatialRotation());
    }

    protected void createCollisionShape() {
        if (spatial == null) {
            return;
        }
        if (spatial instanceof Geometry) {
            Geometry geom = (Geometry) spatial;
            Mesh mesh = geom.getMesh();
            if (mesh instanceof Sphere) {
                collisionShape = new SphereCollisionShape(((Sphere) mesh).getRadius());
                return;
            } else if (mesh instanceof Box) {
                collisionShape = new BoxCollisionShape(new Vector3f(((Box) mesh).getXExtent(), ((Box) mesh).getYExtent(), ((Box) mesh).getZExtent()));
                return;
            }
        }
        if (mass > 0) {
            collisionShape = CollisionShapeFactory.createDynamicMeshShape(spatial);
        } else {
            collisionShape = CollisionShapeFactory.createMeshShape(spatial);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (space != null) {
            if (enabled && !added) {
                if (spatial != null) {
                    setPhysicsLocation(getSpatialTranslation());
                    setPhysicsRotation(getSpatialRotation());
                }
                space.addCollisionObject(this);
                added = true;
            } else if (!enabled && added) {
                space.removeCollisionObject(this);
                added = false;
            }
        }
    }

    /**
     * Test whether this control is enabled.
     *
     * @return true if enabled, otherwise false
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Test whether this control is in kinematic mode.
     *
     * @return true if the spatial location and rotation are applied to the
     * rigid body, otherwise false
     */
    public boolean isKinematicSpatial() {
        return kinematicSpatial;
    }

    /**
     * Enable or disable kinematic mode. In kinematic mode, the spatial's
     * location and rotation will be applied to the rigid body.
     *
     * @param kinematicSpatial true&rarr;kinematic, false&rarr;dynamic or static
     */
    public void setKinematicSpatial(boolean kinematicSpatial) {
        this.kinematicSpatial = kinematicSpatial;
    }

    /**
     * Test whether physics location and rotation should match the spatial's
     * local transform.
     *
     * @return true if matching local transform, false if matching world
     * transform
     */
    public boolean isApplyPhysicsLocal() {
        return motionState.isApplyPhysicsLocal();
    }

    /**
     * Alter whether physics location and rotation should match the spatial's
     * local transform.
     *
     * @param applyPhysicsLocal true&rarr;match local transform,
     * false&rarr;match world transform (default is false)
     */
    public void setApplyPhysicsLocal(boolean applyPhysicsLocal) {
        motionState.setApplyPhysicsLocal(applyPhysicsLocal);
    }

    /**
     * Access whichever spatial translation corresponds to the physics location.
     *
     * @return the pre-existing vector (not null)
     */
    private Vector3f getSpatialTranslation() {
        if (MySpatial.isIgnoringTransforms(spatial)) {
            return translateIdentity;
        } else if (motionState.isApplyPhysicsLocal()) {
            return spatial.getLocalTranslation();
        } else {
            return spatial.getWorldTranslation();
        }
    }

    /**
     * Access whichever spatial rotation corresponds to the physics rotation.
     *
     * @return the pre-existing quaternion (not null)
     */
    private Quaternion getSpatialRotation() {
        if (MySpatial.isIgnoringTransforms(spatial)) {
            return rotateIdentity;
        } else if (motionState.isApplyPhysicsLocal()) {
            return spatial.getLocalRotation();
        } else {
            return spatial.getWorldRotation();
        }
    }

    private Vector3f getSpatialScale() {
        if (MySpatial.isIgnoringTransforms(spatial)) {
            return new Vector3f(1f, 1f, 1f);
        } else if (motionState.isApplyPhysicsLocal()) {
            return spatial.getLocalScale();
        }
        return spatial.getWorldScale();
    }

    /**
     * Update this control. Invoked once per frame, during the logical-state
     * update, provided the control is added to a scene.
     *
     * @param tpf the time interval between updates (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        if (enabled && spatial != null) {
            if (isKinematic() && kinematicSpatial) {
                super.setPhysicsLocation(getSpatialTranslation());
                super.setPhysicsRotation(getSpatialRotation());
                Vector3f newScale = getSpatialScale();
                if (PhysicsUtil.canScale(collisionShape, newScale)) {
                    Vector3f oldScale = collisionShape.getScale();
                    if (!newScale.equals(oldScale)) {
                        // assuming single-use shape
                        collisionShape.setScale(newScale);
                        setCollisionShape(collisionShape);
                    }
                }
            } else {
                getMotionState().applyTransform(spatial);
            }
        }
    }

    /**
     * Render this control. Invoked once per view port per frame, provided the
     * control is added to a scene. Should be invoked only by a subclass or by
     * the RenderManager.
     *
     * @param rm the render manager (not null)
     * @param vp the view port to render (not null)
     */
    @Override
    public void render(RenderManager rm, ViewPort vp) {
    }

    /**
     * Add this control's body to the specified physics space and remove it from
     * any space it's currently in.
     *
     * @param space where to add, or null to simply remove
     */
    @Override
    public void setPhysicsSpace(PhysicsSpace space) {
        if (space == null) {
            if (this.space != null) {
                this.space.removeCollisionObject(this);
                added = false;
            }
        } else {
            if (this.space == space) {
                return;
            }
            // if this object isn't enabled, it will be added when it will be enabled.
            if (isEnabled()) {
                space.addCollisionObject(this);
                added = true;
            }
        }
        this.space = space;
    }

    /**
     * Access the physics space to which the body is added.
     *
     * @return the pre-existing space, or null for none
     */
    @Override
    public PhysicsSpace getPhysicsSpace() {
        return space;
    }

    /**
     * Serialize this control, for example when saving to a J3O file.
     *
     * @param ex exporter (not null)
     * @throws IOException from exporter
     */
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(enabled, "enabled", true);
        oc.write(motionState.isApplyPhysicsLocal(), "applyLocalPhysics", false);
        oc.write(kinematicSpatial, "kinematicSpatial", true);
        oc.write(spatial, "spatial", null);
    }

    /**
     * De-serialize this control, for example when loading from a J3O file.
     *
     * @param im importer (not null)
     * @throws IOException from importer
     */
    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);
        enabled = ic.readBoolean("enabled", true);
        kinematicSpatial = ic.readBoolean("kinematicSpatial", true);
        spatial = (Spatial) ic.readSavable("spatial", null);
        motionState.setApplyPhysicsLocal(ic.readBoolean("applyLocalPhysics", false));
        setUserObject(spatial);
    }
}
