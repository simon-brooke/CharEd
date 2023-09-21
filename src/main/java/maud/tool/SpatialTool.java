/*
 Copyright (c) 2017-2021, Stephen Gold
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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;
import maud.Maud;
import maud.model.cgm.SelectedSpatial;

/**
 * The controller for the "Spatial" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SpatialTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SpatialTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    SpatialTool(GuiScreenController screenController) {
        super(screenController, "spatial");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateChildren();
        updateMaterial();
        updateMesh();
        updateName();
        updateParent();
        updateSiblings();
        updateTransform();
        updateTreePosition();
        updateType();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the display of the spatial's children.
     */
    private void updateChildren() {
        String childrenText;

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.isNode()) {
            int numChildren = spatial.countChildren();
            if (numChildren == 0) {
                childrenText = "no children (a leaf node is selected)";
            } else if (numChildren == 1) {
                String childName = spatial.getChildName(0);
                childrenText = MyString.quote(childName);
            } else {
                childrenText = String.format("%d children", numChildren);
            }
        } else {
            childrenText = "no children (a geometry is selected)";
        }

        setButtonText("spatialChildren", childrenText);
    }

    /**
     * Update the display of the spatial's material, if any.
     */
    private void updateMaterial() {
        String materialText;

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.hasMaterial()) {
            String materialName = spatial.getMaterialName();
            if (materialName == null) {
                materialText = "nameless";
            } else {
                materialText = MyString.quote(materialName);
            }
        } else if (spatial.isNode()) {
            materialText = "no material (a node is selected)";
        } else {
            materialText = "no material";
        }

        setButtonText("spatialMaterial", materialText);
    }

    /**
     * Update the description of the spatial's Mesh.
     */
    private void updateMesh() {
        String meshText;

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.hasMesh()) {
            int numElements = spatial.countElements();
            meshText = Integer.toString(numElements);
            if (spatial.hasAnimatedMesh()) {
                meshText += " animated,";
            } else {
                meshText += " non-animated,";
            }
            if (spatial.hasIndexedMesh()) {
                meshText += " indexed ";
            } else {
                meshText += " non-indexed ";
            }
            Mesh.Mode mode = spatial.getMeshMode();
            meshText += mode.toString();

        } else if (spatial.isNode()) {
            meshText = "no mesh (a node is selected)";

        } else {
            meshText = "no mesh";
        }

        setButtonText("spatialMesh", meshText);
    }

    /**
     * Update the display of the spatial's name.
     */
    private void updateName() {
        String nameText;

        String name = Maud.getModel().getTarget().getSpatial().getName();
        if (name == null) {
            nameText = "null";
        } else {
            nameText = MyString.quote(name);
        }

        setStatusText("spatialName", " " + nameText);
    }

    /**
     * Update the display of the spatial's parent.
     */
    private void updateParent() {
        String parentText;

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.isCgmRoot()) {
            parentText = "no parent (the model root is selected)";
        } else {
            String name = spatial.getParentName();
            if (name == null) {
                parentText = "null";
            } else {
                parentText = MyString.quote(name);
            }
        }

        setButtonText("spatialParent", " " + parentText);
    }

    /**
     * Update the display of the spatial's siblings.
     */
    private void updateSiblings() {
        String first = "";
        String previous = "";
        String select = "";
        String next = "";
        String last = "";

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        int numSiblings = spatial.countSiblings(); // count includes self
        if (numSiblings > 1) {
            int siblingIndex = spatial.siblingIndex();
            if (siblingIndex > 0) {
                first = "First";
                previous = "Previous";
            }
            if (siblingIndex < numSiblings - 1) {
                next = "Next";
                last = "Last";
            }
            select = "Select";
        }

        setButtonText("spatialFirstSibling", first);
        setButtonText("spatialPreviousSibling", previous);
        setButtonText("spatialSelectSibling", select);
        setButtonText("spatialNextSibling", next);
        setButtonText("spatialLastSibling", last);
    }

    /**
     * Update the display of the spatial's position in the model's scene graph.
     */
    private void updateTreePosition() {
        String positionText;

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.isCgmRoot()) {
            positionText = "model root";
        } else {
            positionText = spatial.toString();
        }

        setStatusText("spatialTreePosition", positionText);
    }

    /**
     * Update the display of the spatial's Transform.
     */
    private void updateTransform() {
        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();

        String buttonText;
        if (spatial.containsMeshes()) {
            buttonText = "Apply";
        } else {
            buttonText = "";
        }
        setButtonText("spatialApplyTransform", buttonText);

        String transformText;
        if (spatial.isTransformIgnored()) {
            transformText = "Ignored";

        } else if (spatial.isAnimationTarget()) {
            transformText = "Controlled by a spatial track";

        } else if (spatial.isAttachmentsNode()) {
            transformText = "Controlled by a skeleton";

        } else {
            StringBuilder notes = new StringBuilder(20);
            Vector3f translation = spatial.localTranslation(null);
            if (!MyVector3f.isZero(translation)) {
                notes.append("Tra[ ");
                if (translation.x != 0f) {
                    notes.append('x');
                }
                if (translation.y != 0f) {
                    notes.append('y');
                }
                if (translation.z != 0f) {
                    notes.append('z');
                }
                notes.append(" ]");
            }
            Quaternion rotation = spatial.localRotation(null);
            if (!MyQuaternion.isRotationIdentity(rotation)) {
                if (notes.length() > 0) {
                    notes.append("  ");
                }
                notes.append("Rot");
            }
            Vector3f scale = spatial.localScale(null);
            if (!MyVector3f.isScaleIdentity(scale)) {
                if (notes.length() > 0) {
                    notes.append("  ");
                }
                notes.append("Sca[ ");
                if (scale.x != 1f) {
                    notes.append('x');
                }
                if (scale.y != 1f) {
                    notes.append('y');
                }
                if (scale.z != 1f) {
                    notes.append('z');
                }
                notes.append(" ]");
            }
            if (notes.length() == 0) {
                notes.append("Identity");
            }
            transformText = notes.toString();
        }

        setStatusText("spatialTransform", " " + transformText);
    }

    /**
     * Update the display of the spatial's type.
     */
    private void updateType() {
        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        String typeText = spatial.describeType();
        setStatusText("spatialType", typeText);
    }
}
