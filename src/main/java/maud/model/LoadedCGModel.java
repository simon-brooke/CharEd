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
package maud.model;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.animation.Bone;
import com.jme3.animation.BoneTrack;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.animation.Track;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetManager;
import com.jme3.asset.ModelKey;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.plugins.ogre.MeshLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyAnimation;
import jme3utilities.MySkeleton;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.ui.ActionApplication;
import maud.Maud;
import maud.Util;

/**
 * The MVC model of the loaded CG model in the Maud application: tracks all
 * edits made to the CG model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class LoadedCGModel implements Cloneable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(
            LoadedCGModel.class.getName());
    /**
     * dummy bone name used to indicate that no bone is selected
     */
    final public static String noBone = "( no bone )";
    // *************************************************************************
    // fields

    /**
     * asset manager for loading CG models (set by constructor}
     */
    private AssetManager assetManager = null;
    /**
     * count of unsaved edits to the CG model (&ge;0)
     */
    private int editCount = 0;
    /**
     * the root spatial in the MVC model's copy of the CG model
     */
    private Spatial rootSpatial = null;
    /**
     * asset path of the loaded model, less extension
     */
    private String loadedModelAssetPath = null;
    /**
     * extension of the loaded model
     */
    private String loadedModelExtension = null;
    /**
     * filesystem path of the loaded model, less extension
     */
    private String loadedModelFilePath = null;
    /**
     * name of the loaded model
     */
    private String modelName = null;
    // *************************************************************************
    // constructors

    /**
     * Instantiate with the specified asset manager.
     *
     * @param assetManager (not null)
     */
    public LoadedCGModel(AssetManager assetManager) {
        Validate.nonNull(assetManager, "asset manager");
        this.assetManager = assetManager;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a new animation to the model.
     *
     * @param newAnimation (not null, name not in use)
     */
    void addAnimation(Animation newAnimation) {
        Validate.nonNull(newAnimation, "new animation");
        assert !hasAnimation(newAnimation.getName());

        AnimControl control = getAnimControl();
        control.addAnim(newAnimation);
        setEdited();
    }

    /**
     * Count the bones in the loaded model.
     *
     * @return count (&ge;0)
     */
    public int countBones() {
        Skeleton skeleton = getSkeleton();
        int count = skeleton.getBoneCount();

        assert count >= 0 : count;
        return count;
    }

    /**
     * Count the root bones in the loaded model.
     *
     * @return count (&ge;0)
     */
    public int countRootBones() {
        Skeleton skeleton = getSkeleton();
        Bone[] roots = skeleton.getRoots();
        int count = roots.length;

        assert count >= 0 : count;
        return count;
    }

    /**
     * Count unsaved edits.
     *
     * @return count (&ge;0)
     */
    public int countUnsavedEdits() {
        return editCount;
    }

    /**
     * Delete the loaded animation. If successful, the caller should immediately
     * load a different animation.
     *
     * @return true if successful, otherwise false
     */
    public boolean deleteAnimation() {
        if (Maud.model.animation.isBindPoseLoaded()) {
            logger.log(Level.WARNING, "cannot delete bind pose");
            return false;
        }
        AnimControl animControl = getAnimControl();
        Animation anim = Maud.model.animation.getLoadedAnimation();
        animControl.removeAnim(anim);
        setEdited();

        return true;
    }

    /**
     * Access the named animation.
     *
     * @param name (not null)
     * @return the pre-existing instance, or null if not found
     */
    Animation getAnimation(String name) {
        Validate.nonNull(name, "animation name");

        AnimControl animControl = getAnimControl();
        Animation anim = animControl.getAnim(name);

        return anim;
    }

    /**
     * Access the AnimControl of the loaded model.
     *
     * @return the pre-existing instance, or null if none
     */
    AnimControl getAnimControl() {
        AnimControl animControl = rootSpatial.getControl(AnimControl.class);
        if (animControl == null) {
            String message = String.format(
                    "expected model %s to have an AnimControl",
                    MyString.quote(modelName));
            throw new IllegalArgumentException(message);
            //TODO add a new control
        }

        return animControl;
    }

    /**
     * Read the asset path of the loaded model, less extension.
     *
     * @return path, or "" if not known (not null)
     */
    public String getAssetPath() {
        assert loadedModelAssetPath != null;
        return loadedModelAssetPath;
    }

    /**
     * Read the duration of the named animation.
     *
     * @param animationName (not null)
     * @return duration (in seconds, &ge;0)
     */
    public float getDuration(String animationName) {
        Validate.nonNull(animationName, "animation name");

        float result;
        if (animationName.equals(LoadedAnimation.bindPoseName)) {
            result = 0f;
        } else {
            Animation anim = getAnimation(animationName);
            if (anim == null) {
                logger.log(Level.WARNING, "no bone named {0}",
                        MyString.quote(animationName));
                result = 0f;
            } else {
                result = anim.getLength();
            }
        }

        assert result >= 0f : result;
        return result;
    }

    /**
     * Read the extension of the loaded model.
     *
     * @return extension (not null)
     */
    public String getExtension() {
        assert loadedModelExtension != null;
        return loadedModelExtension;
    }

    /**
     * Read the filesystem path of the loaded model, less extension.
     *
     * @return path, or "" if not known (not null)
     */
    public String getFilePath() {
        assert loadedModelFilePath != null;
        return loadedModelFilePath;
    }

    /**
     * Read the name of the loaded model.
     *
     * @return name, or "" if not known (not null)
     */
    public String getName() {
        assert modelName != null;
        return modelName;
    }

    /**
     * Access the root spatial of the loaded model.
     *
     * @return the pre-existing instance (not null)
     */
    Spatial getRootSpatial() {
        assert rootSpatial != null;
        return rootSpatial;
    }

    /**
     * Access the skeleton of the loaded model.
     *
     * @return the pre-existing instance (not null)
     */
    Skeleton getSkeleton() {
        Skeleton skeleton = MySkeleton.getSkeleton(rootSpatial);
        assert skeleton != null;
        return skeleton;
    }

    /**
     * Test whether the animation controller contains the named animation.
     *
     * @param name (not null)
     * @return true if found or bindPose, otherwise false
     */
    public boolean hasAnimation(String name) {
        Validate.nonNull(name, "name");

        Animation animation = getAnimation(name);
        if (animation == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the skeleton contains the named bone.
     *
     * @param name (not null)
     * @return true if found or noBone, otherwise false
     */
    public boolean hasBone(String name) {
        if (name.equals(noBone)) {
            return true;
        }
        Bone b = MySkeleton.getBone(rootSpatial, name);
        if (b == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Test whether the named bone is a leaf bone, with no children.
     *
     * @param boneName which bone to test (not null)
     * @return true for a leaf bone, otherwise false
     */
    public boolean isLeafBone(String boneName) {
        if (boneName.equals(noBone)) {
            return false;
        }
        Bone b = MySkeleton.getBone(rootSpatial, boneName);
        if (b == null) {
            return false;
        }
        ArrayList<Bone> children = b.getChildren();
        if (children.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enumerate all known animations and poses for the loaded model.
     *
     * @return a new collection of names
     */
    public Collection<String> listAnimationNames() {
        Collection<String> names = MyAnimation.listAnimations(rootSpatial);
        names.add(LoadedAnimation.bindPoseName);

        return names;
    }

    /**
     * Enumerate all bones in the loaded model.
     *
     * @return a new list of names
     */
    public List<String> listBoneNames() {
        List<String> boneNames = MySkeleton.listBones(rootSpatial);
        boneNames.remove("");
        boneNames.add(noBone);

        return boneNames;
    }

    /**
     * Enumerate all bones in the loaded model having names that start with the
     * specified prefix.
     *
     * @param namePrefix the input prefix
     * @return a new list of names
     */
    public List<String> listBoneNames(String namePrefix) {
        List<String> boneNames = listBoneNames();
        for (String name : MyString.toArray(boneNames)) {
            if (!name.startsWith(namePrefix)) {
                boneNames.remove(name);
            }
        }

        return boneNames;
    }

    /**
     * Enumerate all children of the named bone.
     *
     * @param parentName name of the parent bone
     * @return a new list of bone names
     */
    public List<String> listChildBoneNames(String parentName) {
        Skeleton skeleton = getSkeleton();
        Bone parent = skeleton.getBone(parentName);
        List<Bone> children = parent.getChildren();
        List<String> boneNames = new ArrayList<>(children.size());
        for (Bone b : children) {
            String name = b.getName();
            boneNames.add(name);
        }
        boneNames.remove("");

        return boneNames;
    }

    /**
     * Enumerate root bones in the loaded model.
     *
     * @return a new list of names
     */
    public List<String> listRootBoneNames() {
        Skeleton skeleton = getSkeleton();
        Bone[] roots = skeleton.getRoots();
        List<String> boneNames = new ArrayList<>(5);
        for (Bone b : roots) {
            String name = b.getName();
            boneNames.add(name);
        }
        boneNames.remove("");

        return boneNames;
    }

    /**
     * Enumerate all meshes in the loaded model.
     *
     * @return a new list
     */
    List<Mesh> listMeshes() {
        List<Mesh> result = new ArrayList<>(8);

        Node node = (Node) rootSpatial;
        for (Spatial child : node.getChildren()) {
            if (child instanceof Geometry) {
                Geometry geometry = (Geometry) child;
                Mesh mesh = geometry.getMesh();
                result.add(mesh);
            }
        }

        return result;
    }

    /**
     * Unload the current model, if any, and load the specified asset.
     *
     * @param assetPath path to the model asset to load (not null)
     * @return true if successful, otherwise false
     */
    public boolean loadModelAsset(String assetPath) {
        Validate.nonNull(assetPath, "asset path");

        Spatial loaded = loadModelFromAsset(assetPath, false);
        if (loaded == null) {
            return false;
        } else {
            postLoad(loaded);
            return true;
        }
    }

    /**
     * Unload the current model, if any, and load the specified file.
     *
     * @param filePath path to the model file to load (not null)
     * @return true if successful, otherwise false
     */
    public boolean loadModelFile(File filePath) {
        String canonicalPath;
        try {
            canonicalPath = filePath.getCanonicalPath();
        } catch (IOException e) {
            return false;
        }

        Spatial loaded = loadModelFromFile(canonicalPath);
        if (loaded == null) {
            return false;
        } else {
            postLoad(loaded);
            return true;
        }
    }

    /**
     * Unload the current model, if any, and load the named one.
     *
     * @param name name of model to load (not null)
     * @return true if successful, otherwise false
     */
    public boolean loadModelNamed(String name) {
        String extension;
        if (name.equals("Jaime")) {
            extension = "j3o";
        } else {
            extension = "mesh.xml";
        }
        String assetPath = String.format("Models/%s/%s.%s",
                name, name, extension);

        Spatial loaded = loadModelFromAsset(assetPath, false);
        if (loaded == null) {
            return false;
        } else {
            modelName = name;
            postLoad(loaded);
            return true;
        }
    }

    /**
     * Rename the loaded animation.
     *
     * @param newName new name (not null, not empty, not bindPoseName, not in
     * use)
     */
    public void renameAnimation(String newName) {
        Validate.nonEmpty(newName, "new name");
        assert !newName.equals(LoadedAnimation.bindPoseName) : newName;
        assert !Maud.model.cgm.hasAnimation(newName) : newName;
        assert !Maud.model.animation.isBindPoseLoaded();

        Maud.model.animation.newCopy(newName);
        deleteAnimation();
        Maud.model.animation.rename(newName);
    }

    /**
     * Rename the selected bone.
     *
     * @param newName new name (not null)
     * @return true if successful, otherwise false
     */
    public boolean renameBone(String newName) {
        Validate.nonNull(newName, "bone name");

        if (!Maud.model.bone.isBoneSelected()) {
            logger.log(Level.WARNING, "Rename failed: no bone selected.",
                    MyString.quote(newName));
            return false;

        } else if (newName.equals(noBone) || newName.isEmpty()) {
            logger.log(Level.WARNING, "Rename failed: {0} is a reserved name.",
                    MyString.quote(newName));
            return false;

        } else if (hasBone(newName)) {
            logger.log(Level.WARNING,
                    "Rename failed: a bone named {0} already exists.",
                    MyString.quote(newName));
            return false;
        }

        Bone selectedBone = Maud.model.bone.getBone();
        boolean success = MySkeleton.setName(selectedBone, newName);
        setEdited();

        return success;
    }

    /**
     * Alter the user rotation of the selected bone.
     *
     * @param rotation (not null, unaffected)
     */
    public void setBoneRotation(Quaternion rotation) {
        Validate.nonNull(rotation, "rotation");
        assert Maud.model.bone.isBoneSelected();

        int boneIndex = Maud.model.bone.getIndex();
        Maud.model.pose.setRotation(boneIndex, rotation);
    }

    /**
     * Alter the user scale of the selected bone.
     *
     * @param scale (not null, unaffected)
     */
    public void setBoneScale(Vector3f scale) {
        Validate.nonNull(scale, "scale");
        assert Maud.model.bone.isBoneSelected();

        int boneIndex = Maud.model.bone.getIndex();
        Maud.model.pose.setScale(boneIndex, scale);
    }

    /**
     * Alter the user translation of the selected bone.
     *
     * @param translation (not null, unaffected)
     */
    public void setBoneTranslation(Vector3f translation) {
        Validate.nonNull(translation, "translation");
        assert Maud.model.bone.isBoneSelected();

        int boneIndex = Maud.model.bone.getIndex();
        Maud.model.pose.setTranslation(boneIndex, translation);
    }

    /**
     * Alter the cull hint of the selected spatial.
     *
     * @param newHint new value for cull hint (not null)
     */
    public void setHint(Spatial.CullHint newHint) {
        Validate.nonNull(newHint, "cull hint");

        Spatial modelSpatial = Maud.model.spatial.findSpatial(rootSpatial);
        Spatial.CullHint oldHint = modelSpatial.getLocalCullHint();
        if (oldHint != newHint) {
            modelSpatial.setCullHint(newHint);
            setEdited();
            Maud.viewState.setHint(newHint);
        }
    }

    /**
     * Alter the shadow mode of the selected spatial.
     *
     * @param newMode new value for shadow mode (not null)
     */
    public void setMode(RenderQueue.ShadowMode newMode) {
        Validate.nonNull(newMode, "shadow mode");

        Spatial modelSpatial = Maud.model.spatial.findSpatial(rootSpatial);
        RenderQueue.ShadowMode oldMode = modelSpatial.getLocalShadowMode();
        if (oldMode != newMode) {
            modelSpatial.setShadowMode(newMode);
            setEdited();
            Maud.viewState.setMode(newMode);
        }
    }

    /**
     * Alter all keyframes in the selected bone track.
     *
     * @param times array of keyframe times (not null, not empty)
     * @param translations array of keyframe translations (not null)
     * @param rotations array of keyframe rotations (not null)
     * @param scales array of keyframe scales (may be null)
     */
    void setKeyframes(float[] times, Vector3f[] translations,
            Quaternion[] rotations, Vector3f[] scales) {
        assert times != null;
        assert times.length > 0 : times.length;
        assert translations != null;
        assert rotations != null;

        BoneTrack track = Maud.model.bone.findTrack();
        track.setKeyframes(times, translations, rotations, scales);
        setEdited();
    }

    /**
     * Write the loaded model to an asset.
     *
     * @param baseAssetPath asset path without any extension (not null)
     * @return true if successful, otherwise false
     */
    public boolean writeModelToAsset(String baseAssetPath) {
        Validate.nonNull(baseAssetPath, "asset path");

        String baseFilePath = ActionApplication.filePath(baseAssetPath);
        boolean success = writeModelToFile(baseFilePath);
        if (success) {
            loadedModelAssetPath = baseAssetPath;
        }

        return success;
    }

    /**
     * Write the loaded model to a file.
     *
     * @param baseFilePath file path without any extension (not null)
     * @return true if successful, otherwise false
     */
    public boolean writeModelToFile(String baseFilePath) {
        Validate.nonNull(baseFilePath, "file path");

        String filePath = baseFilePath + ".j3o";
        File file = new File(filePath);
        BinaryExporter exporter = BinaryExporter.getInstance();

        boolean success = true;
        try {
            exporter.save(rootSpatial, file);
        } catch (IOException exception) {
            success = false;
        }
        if (success) {
            loadedModelAssetPath = "";
            loadedModelExtension = "j3o";
            loadedModelFilePath = baseFilePath;
            setPristine();
            logger.log(Level.INFO, "Wrote model to file {0}",
                    MyString.quote(filePath));
        } else {
            logger.log(Level.SEVERE,
                    "I/O exception while writing model to file {0}",
                    MyString.quote(filePath));
        }

        return success;
    }
    // *************************************************************************
    // Object methods

    /**
     * Create a copy of this object.
     *
     * @return a new object, equivalent to this one
     * @throws CloneNotSupportedException if superclass isn't cloneable
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        LoadedCGModel clone = (LoadedCGModel) super.clone();
        clone.rootSpatial = rootSpatial.clone();

        return clone;
    }
    // *************************************************************************
    // private methods

    /**
     * Quietly load a CG model asset from persistent storage without adding it
     * to the scene. If successful, set {@link #loadedModelAssetPath}.
     *
     * @param assetPath (not null)
     * @param useCache true to look in the asset manager's cache, false to force
     * a fresh load from persistent storage
     * @return an orphaned spatial, or null if the asset was not found
     */
    private Spatial loadModelFromAsset(String assetPath, boolean useCache) {
        ModelKey key = new ModelKey(assetPath);
        if (!useCache) {
            /*
             * Delete the model's key from the asset manager's cache, to force a
             * fresh load from persistent storage.
             */
            assetManager.deleteFromCache(key);
        }
        /*
         * Load the model quietly.
         */
        Spatial loaded = Util.loadCgmQuietly(assetManager, assetPath);
        if (loaded == null) {
            logger.log(Level.SEVERE, "Failed to load model from asset {0}",
                    MyString.quote(assetPath));
        } else {
            logger.log(Level.INFO, "Loaded model from asset {0}",
                    MyString.quote(assetPath));

            loadedModelExtension = key.getExtension();
            int extLength = loadedModelExtension.length();
            if (extLength == 0) {
                loadedModelAssetPath = assetPath;
            } else {
                int pathLength = assetPath.length() - extLength - 1;
                loadedModelAssetPath = assetPath.substring(0, pathLength);
            }
            loadedModelFilePath = "";
            modelName = loaded.getName();
        }

        return loaded;
    }

    /**
     * Quietly load a model file without adding it to the scene. If successful,
     * set {@link #loadedModelFilePath}.
     *
     * @param filePath (not null)
     * @return an orphaned spatial, or null if an error occurred
     */
    private Spatial loadModelFromFile(String filePath) {
        ModelKey key = new ModelKey(filePath);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            return null;
        }
        /*
         * Temporarily hush loader warnings about vertices with >4 weights.
         */
        Logger mlLogger = Logger.getLogger(MeshLoader.class.getName());
        Level oldLevel = mlLogger.getLevel();
        mlLogger.setLevel(Level.SEVERE);

        Spatial loaded;
        try {
            loaded = assetManager.loadAssetFromStream(key, inputStream);
        } catch (AssetLoadException e) {
            loaded = null;
        }
        /*
         * Restore logging levels.
         */
        mlLogger.setLevel(oldLevel);

        if (loaded == null) {
            logger.log(Level.SEVERE, "Failed to load model from file {0}",
                    MyString.quote(filePath));
        } else {
            logger.log(Level.INFO, "Loaded model from file {0}",
                    MyString.quote(filePath));

            loadedModelAssetPath = "";
            loadedModelExtension = key.getExtension();
            int extLength = loadedModelExtension.length();
            if (extLength == 0) {
                loadedModelFilePath = filePath;
            } else {
                int pathLength = filePath.length() - extLength - 1;
                loadedModelFilePath = filePath.substring(0, pathLength);
            }
            modelName = loaded.getName();
        }

        return loaded;
    }

    /**
     * Invoked after successfully loading a CG model.
     *
     * @param modelRoot (not null)
     */
    private void postLoad(Spatial modelRoot) {
        assert modelRoot != null;

        setPristine();
        repairModel(modelRoot);
        validateModel(modelRoot);
        rootSpatial = modelRoot.clone();
        Maud.viewState.loadModel(modelRoot);

        Maud.model.bone.selectNoBone();
        Maud.model.spatial.selectModelRoot();
        Maud.model.animation.loadBindPose();
    }

    /**
     * Repair minor issues with a CG model, such as repetitious keyframes.
     *
     * @param modelRoot model to correct (not null)
     */
    private void repairModel(Spatial modelRoot) {
        boolean madeRepairs = false;

        int numTracksEdited = 0;
        AnimControl animControl = modelRoot.getControl(AnimControl.class);
        Collection<String> names = animControl.getAnimationNames();
        for (String animationName : names) {
            Animation animation = animControl.getAnim(animationName);
            numTracksEdited += Util.removeRepeats(animation);
        }
        if (numTracksEdited > 0) {
            String message = "removed repeat keyframe(s) from ";
            if (numTracksEdited == 1) {
                message += "one track";
            } else {
                message += String.format("%d tracks", numTracksEdited);
            }
            logger.warning(message);
            madeRepairs = true;
        }

        if (madeRepairs) {
            setEdited();
        }
    }

    /**
     * Increment the count of unsaved edits.
     */
    private void setEdited() {
        ++editCount;
    }

    /**
     * Mark the model as pristine.
     */
    private void setPristine() {
        editCount = 0;
    }

    /**
     * Test for issues with a CG model.
     *
     * @param modelRoot (not null)
     * @return false if issues found, otherwise true
     */
    private boolean validateModel(Spatial modelRoot) {
        assert modelRoot != null;

        SkeletonControl skeletonControl = modelRoot.getControl(
                SkeletonControl.class);
        if (skeletonControl == null) {
            logger.warning("lacks a skeleton control");
            return false;
        }
        Skeleton skeleton = skeletonControl.getSkeleton();
        if (skeleton == null) {
            logger.warning("lacks a skeleton");
            return false;
        }
        int numBones = skeleton.getBoneCount();
        if (numBones > 255) {
            logger.warning("too many bones");
            return false;
        }
        if (numBones < 0) {
            logger.warning("bone count is negative");
            return false;
        }
        Set<String> nameSet = new TreeSet<>();
        for (int boneIndex = 0; boneIndex < numBones; boneIndex++) {
            Bone bone = skeleton.getBone(boneIndex);
            if (bone == null) {
                logger.warning("bone is null");
                return false;
            }
            String name = bone.getName();
            if (name == null) {
                logger.warning("bone name is null");
                return false;
            }
            if (name.length() == 0) {
                logger.warning("bone name is empty");
                return false;
            }
            if (name.equals(noBone)) {
                logger.warning("bone has reserved name");
                return false;
            }
            if (nameSet.contains(name)) {
                logger.warning("duplicate bone name");
            }
            nameSet.add(name);
        }
        AnimControl animControl = modelRoot.getControl(
                AnimControl.class);
        if (animControl == null) {
            logger.warning("model lacks an animation control");
            return false;
        }
        Skeleton skeleton2 = animControl.getSkeleton();
        if (skeleton2 != skeleton) {
            logger.warning("model has two skeletons");
            return false;
        }
        Collection<String> animNames = animControl.getAnimationNames();
        if (animNames.isEmpty()) {
            logger.warning("model has no animations");
            return false;
        }
        nameSet = new TreeSet<>();
        for (String name : animNames) {
            if (name == null) {
                logger.warning("animation name is null");
                return false;
            }
            if (name.length() == 0) {
                logger.warning("animation name is empty");
                return false;
            }
            if (name.equals(LoadedAnimation.bindPoseName)) {
                logger.warning("animation has reserved name");
                return false;
            }
            if (nameSet.contains(name)) {
                logger.warning("duplicate animation name");
                return false;
            }
            nameSet.add(name);
            Animation anim = animControl.getAnim(name);
            if (anim == null) {
                logger.warning("animation is null");
                return false;
            }
            float duration = anim.getLength();
            if (duration < 0f) {
                logger.warning("animation has negative length");
                return false;
            }
            Track[] tracks = anim.getTracks();
            if (tracks == null) {
                logger.warning("animation has no track data");
                return false;
            }
            int numTracks = tracks.length;
            if (numTracks == 0) {
                logger.warning("animation has no tracks");
                return false;
            }
            for (Track track : tracks) {
                float[] times = track.getKeyFrameTimes();
                if (times == null) {
                    logger.warning("track has no keyframe data");
                    return false;
                }
                int numFrames = times.length;
                if (numFrames == 0) {
                    logger.warning("track has no keyframes");
                    return false;
                }
                if (times[0] != 0f) {
                    logger.warning("first keyframe not at t=0");
                    return false;
                }
                float prev = -1f;
                for (int frameIndex = 0; frameIndex < numFrames; frameIndex++) {
                    float time = times[frameIndex];
                    if (time < prev) {
                        logger.warning("keyframes out of order");
                        return false;
                    } else if (time == prev) {
                        logger.log(Level.WARNING,
                                "multiple keyframes for t={0} in {1}",
                                new Object[]{time, MyString.quote(name)});
                    } else if (time > duration) {
                        logger.warning("keyframe past end of animation");
                        return false;
                    }
                    prev = time;
                }
                if (track instanceof BoneTrack) {
                    BoneTrack boneTrack = (BoneTrack) track;
                    int targetBoneIndex = boneTrack.getTargetBoneIndex();
                    if (targetBoneIndex >= numBones) {
                        logger.warning("track for non-existant bone");
                        return false;
                    }
                    Vector3f[] translations = boneTrack.getTranslations();
                    if (translations == null) {
                        logger.warning("bone track lacks translation data");
                        return false;
                    }
                    int numTranslations = translations.length;
                    if (numTranslations != numFrames) {
                        logger.warning("translation data have wrong length");
                        return false;
                    }
                    Quaternion[] rotations = boneTrack.getRotations();
                    if (rotations == null) {
                        logger.warning("bone track lacks rotation data");
                        return false;
                    }
                    int numRotations = rotations.length;
                    if (numRotations != numFrames) {
                        logger.warning("rotation data have wrong length");
                        return false;
                    }
                    Vector3f[] scales = boneTrack.getScales();
                    if (scales == null) { // JME3 allows this
                        logger.warning("bone track lacks scale data");
                        return false;
                    }
                    int numScales = scales.length;
                    if (numScales != numFrames) {
                        logger.warning("scale data have wrong length");
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
