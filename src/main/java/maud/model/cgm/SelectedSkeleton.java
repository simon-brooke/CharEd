/*
 Copyright (c) 2017-2019, Stephen Gold
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
package maud.model.cgm;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Bone;
import com.jme3.animation.Skeleton;
import com.jme3.animation.SkeletonControl;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MySkeleton;
import jme3utilities.MySpatial;
import jme3utilities.MyString;
import jme3utilities.Validate;
import maud.InfluenceUtil;
import maud.Maud;
import maud.model.EditorModel;
import maud.model.LoadedMap;
import maud.model.option.ShowBones;
import maud.view.scene.SceneView;

/**
 * The MVC model of a selected skeleton in the Maud application.
 *
 * If the selected S-G control is a SkeletonControl or AnimControl, use that
 * control's skeleton, otherwise use the skeleton of the first SkeletonControl
 * or AnimControl in the C-G model's root spatial.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class SelectedSkeleton implements JmeCloneable {
    // *************************************************************************
    // constants and loggers

    /**
     * dummy bone index, used to indicate that no bone is selected
     */
    final public static int noBoneIndex = -1;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SelectedSkeleton.class.getName());
    /**
     * dummy bone name, used to indicate that no bone is selected
     */
    final public static String noBone = "( no bone )";
    // *************************************************************************
    // fields

    /**
     * C-G model containing the skeleton (set by {@link #setCgm(Cgm)})
     */
    private Cgm cgm = null;
    /**
     * most recent selection
     */
    private Skeleton last = null;
    // *************************************************************************
    // new methods exposed

    /**
     * Calculate the tree position of the attachments node, if any, for the
     * indexed bone.
     *
     * @param boneIndex which bone (&ge;0)
     * @return tree position, or null if none
     */
    public List<Integer> attachmentsPosition(int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        List<Integer> result = null;
        Bone bone = getBone(boneIndex);
        Node node = MySkeleton.getAttachments(bone);
        if (node != null) {
            result = cgm.findSpatial(node);
        }

        return result;
    }

    /**
     * Calculate the index of the named bone in the selected skeleton.
     *
     * @param boneName name of the bone (not null, not empty)
     * @return the bone index (&ge;0) or -1 if not found
     */
    public int boneIndex(String boneName) {
        Validate.nonEmpty(boneName, "bone name");

        Skeleton skeleton = find();
        int result = skeleton.getBoneIndex(boneName);

        return result;
    }

    /**
     * Count the bones in the selected skeleton.
     *
     * @return count (&ge;0)
     */
    public int countBones() {
        Skeleton skeleton = find();
        int count;
        if (skeleton == null) {
            count = 0;
        } else {
            count = skeleton.getBoneCount();
        }

        assert count >= 0 : count;
        return count;
    }

    /**
     * Count the root bones in the selected skeleton.
     *
     * @return count (&ge;0)
     */
    public int countRootBones() {
        int count;
        Skeleton skeleton = find();
        if (skeleton == null) {
            count = 0;
        } else {
            Bone[] roots = skeleton.getRoots();
            count = roots.length;
        }

        assert count >= 0 : count;
        return count;
    }

    /**
     * Find the selected skeleton.
     *
     * @param storeSelectedSgcFlag if not null, set the first element to true if
     * the skeleton came from the selected S-G control, false if it came from
     * the C-G model root
     * @return the pre-existing instance, or null if none
     */
    Skeleton find(boolean[] storeSelectedSgcFlag) {
        AnimControl animControl;
        boolean selectedSgcFlag;
        SkeletonControl skeletonControl;
        Skeleton skeleton = null;
        /*
         * If the selected S-G control is an AnimControl or SkeletonControl,
         * use its skeleton, if it has one.
         */
        Control selectedSgc = cgm.getSgc().get();
        if (selectedSgc instanceof AnimControl) {
            animControl = (AnimControl) selectedSgc;
            skeleton = animControl.getSkeleton();
        }
        if (skeleton == null && selectedSgc instanceof SkeletonControl) {
            skeletonControl = (SkeletonControl) selectedSgc;
            skeleton = skeletonControl.getSkeleton();
        }
        if (skeleton != null) {
            selectedSgcFlag = true;
        } else {
            selectedSgcFlag = false;
        }
        /*
         * If not, use the skeleton from the first AnimControl or
         * SkeletonControl in the C-G model's root spatial.
         */
        if (cgm.isLoaded()) {
            Spatial cgmRoot = cgm.getRootSpatial();
            if (skeleton == null) {
                animControl = cgmRoot.getControl(AnimControl.class);
                if (animControl != null) {
                    skeleton = animControl.getSkeleton();
                }
            }
            if (skeleton == null) {
                skeletonControl = cgmRoot.getControl(SkeletonControl.class);
                if (skeletonControl != null) {
                    skeleton = skeletonControl.getSkeleton();
                }
            }
        }

        if (storeSelectedSgcFlag != null) {
            storeSelectedSgcFlag[0] = selectedSgcFlag; // side-effect
        }
        return skeleton;
    }

    /**
     * Find the selected skeleton.
     *
     * @return the pre-existing instance, or null if none
     */
    Skeleton find() {
        Skeleton result = find(null);
        return result;
    }

    /**
     * Find a geometry that is animated by the selected skeleton control.
     *
     * @return the tree position, or null if none found
     */
    public List<Integer> findAnimatedGeometry() {
        List<Integer> result = null;
        Spatial spatial = findSpatial();
        Geometry geometry = MySpatial.findAnimatedGeometry(spatial);
        if (geometry != null) {
            result = cgm.findSpatial(geometry);
        }

        return result;
    }

    /**
     * Find the spatial associated with the selected skeleton.
     *
     * @return the pre-existing instance, or null if none selected
     */
    Spatial findSpatial() {
        Spatial result;

        boolean[] selectedSgcFlag = {false};
        Skeleton skeleton = find(selectedSgcFlag);
        if (skeleton == null) {
            result = null;
        } else if (selectedSgcFlag[0]) {
            result = cgm.getSgc().getControlled();
        } else {
            result = cgm.getRootSpatial();
        }

        return result;
    }

    /**
     * Find the tree position of the spatial associated with the selected
     * skeleton.
     *
     * @return the pre-existing instance, or null if none selected
     */
    public List<Integer> findSpatialPosition() {
        Spatial spatial = findSpatial();
        List<Integer> result;
        if (spatial == null) {
            result = null;
        } else {
            result = cgm.findSpatial(spatial);
        }

        return result;
    }

    /**
     * Access the indexed bone in the selected skeleton.
     *
     * @param boneIndex which bone (&ge;0)
     * @return the pre-existing instance (not null)
     */
    Bone getBone(int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        Skeleton skeleton = find();
        Bone result = skeleton.getBone(boneIndex);

        return result;
    }

    /**
     * Read the name of the indexed bone in the selected skeleton.
     *
     * @param boneIndex which bone (&ge;0)
     * @return the bone's name (may be null)
     */
    public String getBoneName(int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        Bone bone = getBone(boneIndex);
        String result = bone.getName();

        return result;
    }

    /**
     * Read the index of the indexed bone's parent in the selected skeleton.
     *
     * @param boneIndex which bone (&ge;0)
     * @return bone index (&ge;0) or noBoneIndex for a root bone
     */
    public int getParentIndex(int boneIndex) {
        Validate.nonNegative(boneIndex, "bone index");

        Skeleton skeleton = find();
        Bone bone = skeleton.getBone(boneIndex);
        Bone parent = bone.getParent();
        int result = skeleton.getBoneIndex(parent);

        return result;
    }

    /**
     * Access a skeleton control for the selected skeleton.
     */
    SkeletonControl getSkeletonControl() {
        Skeleton skeleton = find();
        SkeletonControl result = null;

        List<SkeletonControl> list = cgm.listSgcs(SkeletonControl.class);
        for (SkeletonControl sgc : list) {
            Skeleton controlSkeleton = sgc.getSkeleton();
            if (controlSkeleton == skeleton) {
                result = sgc;
                break;
            }
        }

        return result;
    }

    /**
     * Test whether the selected skeleton contains the named bone.
     *
     * @param name which bone (not null)
     * @return true if found or noBone, otherwise false
     */
    public boolean hasBone(String name) {
        boolean result;
        if (name.equals(noBone)) {
            result = true;
        } else {
            Skeleton skeleton = find();
            if (skeleton == null) {
                result = false;
            } else {
                Bone bone = skeleton.getBone(name);
                if (bone == null) {
                    result = false;
                } else {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * Test whether the named bone is a leaf bone, with no children.
     *
     * @param boneName which bone to test (not null)
     * @return true for a leaf bone, otherwise false
     */
    public boolean isLeafBone(String boneName) {
        boolean result = false;
        if (!boneName.equals(noBone)) {
            Skeleton skeleton = find();
            Bone bone = skeleton.getBone(boneName);
            if (bone != null) {
                ArrayList<Bone> children = bone.getChildren();
                result = children.isEmpty();
            }
        }

        return result;
    }

    /**
     * Test whether a skeleton is selected.
     *
     * @return true if selected, otherwise false
     */
    public boolean isSelected() {
        boolean result = false;
        if (cgm.isLoaded()) {
            Skeleton skeleton = find();
            if (skeleton != null) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Enumerate bones with attachment nodes.
     *
     * @return a new list of names, in arbitrary order
     */
    public List<String> listAttachedBones() {
        List<String> result = new ArrayList<>(5);
        Skeleton skeleton = find();
        int numBones = countBones();
        for (int boneIndex = 0; boneIndex < numBones; boneIndex++) {
            Bone bone = skeleton.getBone(boneIndex);
            Node attachmentsNode = MySkeleton.getAttachments(bone);
            if (attachmentsNode != null) {
                String name = bone.getName();
                result.add(name);
            }
        }

        return result;
    }

    /**
     * Enumerate all named bones in lexicographic order, plus noBone.
     *
     * @return a new list of names
     */
    public List<String> listBoneNames() {
        List<String> names = listBoneNamesRaw();
        Collections.sort(names);
        names.add(noBone);

        return names;
    }

    /**
     * Enumerate named bones whose names start with the specified prefix.
     *
     * @param namePrefix the input prefix (not null)
     * @return a new list of names, sorted, which may include noBone
     */
    public List<String> listBoneNames(String namePrefix) {
        Validate.nonNull(namePrefix, "name prefix");

        List<String> boneNames = listBoneNames();
        for (String name : MyString.toArray(boneNames)) {
            if (!name.startsWith(namePrefix)) {
                boneNames.remove(name);
            }
        }

        return boneNames;
    }

    /**
     * Enumerate all named bones, in numeric order.
     *
     * @return a new list of names, not including noBone
     */
    public List<String> listBoneNamesRaw() {
        int size = 1 + countBones(); // allocate an extra item for the invoker
        List<String> names = new ArrayList<>(size);

        Skeleton skeleton = find();
        if (skeleton != null) {
            int boneCount = skeleton.getBoneCount();
            for (int boneIndex = 0; boneIndex < boneCount; boneIndex++) {
                Bone bone = skeleton.getBone(boneIndex);
                String name = bone.getName();
                if (name != null && !name.isEmpty()) {
                    names.add(name);
                }
            }
        }

        return names;
    }

    /**
     * Enumerate all children of the named bone in the selected skeleton.
     *
     * @param parentName name of the parent bone
     * @return a new list of bone names
     */
    public List<String> listChildBoneNames(String parentName) {
        Skeleton skeleton = find();
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
     * Enumerate which bones are referenced by the specified selection option.
     *
     * @param showBones selection option (not null)
     * @param selectedBi the index of the selected bone, or noBoneIndex if none
     * @param storeResult (modified if not null)
     * @return a set of bone indices (either storeResult or a new instance, not
     * null)
     */
    public BitSet listShown(ShowBones showBones, int selectedBi,
            BitSet storeResult) {
        int numBones = countBones();
        BitSet result
                = (storeResult == null) ? new BitSet(numBones) : storeResult;
        assert result.size() >= numBones : result.size();

        if (numBones > 0) {
            Skeleton skeleton = find();
            EditorModel model = Maud.getModel();
            LoadedMap map = model.getMap();
            int ascentBi = selectedBi;

            switch (showBones) {
                case All:
                    result.set(0, numBones);
                    break;

                case Ancestry:
                    result.clear();
                    while (ascentBi != noBoneIndex) {
                        result.set(ascentBi);
                        ascentBi = getParentIndex(ascentBi);
                    }
                    break;

                case Family:
                    result.clear();
                    if (selectedBi != noBoneIndex) {
                        Bone bone = skeleton.getBone(selectedBi);
                        List<Bone> children = bone.getChildren();
                        for (Bone child : children) {
                            int childIndex = skeleton.getBoneIndex(child);
                            result.set(childIndex);
                        }
                    }
                    while (ascentBi != noBoneIndex) {
                        result.set(ascentBi);
                        ascentBi = getParentIndex(ascentBi);
                    }
                    break;

                case Influencers:
                    result.clear();
                    Spatial subtree = findSpatial();
                    InfluenceUtil.addAllInfluencers(subtree, skeleton, result);
                    break;

                case Leaves:
                    for (int loopBi = 0; loopBi < numBones; loopBi++) {
                        Bone bone = skeleton.getBone(loopBi);
                        int numChildren = bone.getChildren().size();
                        boolean isLeaf = (numChildren == 0);
                        result.set(loopBi, isLeaf);
                    }
                    break;

                case Mapped:
                    for (int loopBi = 0; loopBi < numBones; loopBi++) {
                        boolean isMapped;
                        if (cgm == model.getSource()) {
                            isMapped = map.isSourceBoneMapped(loopBi);
                        } else if (cgm == model.getTarget()) {
                            isMapped = map.isTargetBoneMapped(loopBi);
                        } else {
                            throw new IllegalStateException();
                        }
                        result.set(loopBi, isMapped);
                    }
                    break;

                case None:
                    result.clear();
                    break;

                case Roots:
                    result.clear();
                    Bone[] roots = skeleton.getRoots();
                    for (Bone root : roots) {
                        int loopBi = skeleton.getBoneIndex(root);
                        result.set(loopBi);
                    }
                    break;

                case Selected:
                    result.clear();
                    if (selectedBi != noBoneIndex) {
                        result.set(selectedBi);
                    }
                    break;

                case Subtree:
                    result.clear();
                    if (selectedBi != noBoneIndex) {
                        for (int loopBi = 0; loopBi < numBones; ++loopBi) {
                            boolean inSubtree = (loopBi == selectedBi)
                                    || MySkeleton.descendsFrom(loopBi,
                                            selectedBi, skeleton);
                            result.set(loopBi, inSubtree);
                        }
                    }
                    break;

                case Tracked:
                    result.clear();
                    LoadedAnimation animation = cgm.getAnimation();
                    for (int loopBi = 0; loopBi < numBones; ++loopBi) {
                        boolean tracked = animation.hasTrackForBone(loopBi);
                        result.set(loopBi, tracked);
                    }
                    break;

                case Unmapped:
                    for (int loopBi = 0; loopBi < numBones; loopBi++) {
                        boolean isMapped;
                        if (cgm == model.getSource()) {
                            isMapped = map.isSourceBoneMapped(loopBi);
                        } else if (cgm == model.getTarget()) {
                            isMapped = map.isTargetBoneMapped(loopBi);
                        } else {
                            throw new IllegalStateException();
                        }
                        result.set(loopBi, !isMapped);
                    }
                    break;

                default:
                    throw new IllegalStateException();
            }
        }

        return result;
    }

    /**
     * Enumerate the root bones in the selected skeleton.
     *
     * @return a new list of bone names (each non-empty)
     */
    public List<String> listRootBoneNames() {
        List<String> boneNames = new ArrayList<>(5);
        Skeleton skeleton = find();
        if (skeleton != null) {
            Bone[] roots = skeleton.getRoots();
            for (Bone rootBone : roots) {
                String name = rootBone.getName();
                boneNames.add(name);
            }
            boneNames.remove("");
        }

        return boneNames;
    }

    /**
     * Enumerate the root bones in the selected skeleton.
     *
     * @return a new list of bone indices
     */
    public List<Integer> listRootIndices() {
        List<Integer> result = new ArrayList<>(5);
        Skeleton skeleton = find();
        if (skeleton != null) {
            Bone[] roots = skeleton.getRoots();
            for (Bone rootBone : roots) {
                int index = skeleton.getBoneIndex(rootBone);
                result.add(index);
            }
        }

        return result;
    }

    /**
     * Update after (for instance) selecting a different spatial or S-G control.
     */
    void postSelect() {
        boolean[] selectedSgcFlag = {false};
        Skeleton foundSkeleton = find(selectedSgcFlag);
        if (foundSkeleton != last) {
            cgm.getBone().deselect();
            cgm.getPose().resetToBind(foundSkeleton);
            SceneView view = cgm.getSceneView();
            view.setSkeleton(foundSkeleton, selectedSgcFlag[0]);
            last = foundSkeleton;
        }
    }

    /**
     * Alter which C-G model contains the selected skeleton. (Invoked only
     * during initialization and cloning.)
     *
     * @param newCgm (not null, alias created)
     */
    void setCgm(Cgm newCgm) {
        assert newCgm != null;
        assert newCgm.getSkeleton() == this;

        cgm = newCgm;
    }
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Don't use this method; use a {@link com.jme3.util.clone.Cloner} instead.
     *
     * @return never
     * @throws CloneNotSupportedException always
     */
    @Override
    public SelectedSkeleton clone() throws CloneNotSupportedException {
        super.clone();
        throw new CloneNotSupportedException("use a cloner");
    }

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned instance into a deep-cloned one, using the specified
     * cloner and original to resolve copied fields.
     *
     * @param cloner the cloner currently cloning this control (not null)
     * @param original the instance from which this instance was shallow-cloned
     * (unused)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        last = cloner.clone(last);
    }

    /**
     * Create a shallow clone for the JME cloner.
     *
     * @return a new instance
     */
    @Override
    public SelectedSkeleton jmeClone() {
        try {
            SelectedSkeleton clone = (SelectedSkeleton) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
}
