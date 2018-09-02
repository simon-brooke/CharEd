/*
 Copyright (c) 2017-2018, Stephen Gold
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

import com.jme3.animation.Bone;
import com.jme3.material.RenderState;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.shader.VarType;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiScreenController;
import maud.DescribeUtil;
import maud.Maud;
import maud.menu.WhichSpatials;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedMatParam;
import maud.model.cgm.SelectedSpatial;
import maud.model.cgm.WhichParams;

/**
 * The controller for the "Material" tool in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class MaterialTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MaterialTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    MaterialTool(GuiScreenController screenController) {
        super(screenController, "material");
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
        result.add("matDepthTest");
        result.add("matWireframe");

        return result;
    }

    /**
     * Update the MVC model based on a check-box event.
     *
     * @param name the name (unique id prefix) of the check box
     * @param isChecked the new state of the check box (true&rarr;checked,
     * false&rarr;unchecked)
     */
    @Override
    public void onCheckBoxChanged(String name, boolean isChecked) {
        EditableCgm target = Maud.getModel().getTarget();
        switch (name) {
            case "matDepthTest":
                target.setDepthTest(isChecked);
                break;

            case "matWireframe":
                target.setWireframe(isChecked);
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Callback to update this tool prior to rendering. (Invoked once per render
     * pass while this tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        updateNames();
        updateRenderState();
        updateParameterIndex();
        updateParameterName();
        updateParameterValue();
        updateSelect();
    }
    // *************************************************************************
    // private methods

    /**
     * Update the definition name, material name, add/rename buttons, and use
     * count.
     */
    private void updateNames() {
        String defText, materialText;
        String usesText = "";
        String addButton = "";
        String editButton = "";
        String renameButton = "";

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.hasMaterial()) {
            String defName = spatial.getMaterialDefName();
            if (defName == null) {
                defText = "nameless";
            } else {
                defText = MyString.quote(defName);
            }

            String materialName = spatial.getMaterialName();
            if (materialName == null) {
                materialText = "nameless";
            } else {
                materialText = MyString.quote(materialName);
            }

            List<String> undefined
                    = spatial.listMatParamNames("", WhichParams.Undefined);
            if (!undefined.isEmpty()) {
                addButton = "Add parameter";
            }

            editButton = "Edit";
            renameButton = "Rename";

            int uses = spatial.countMaterialUses();
            usesText = Integer.toString(uses);

        } else if (spatial.isNode()) {
            defText = "(a node is selected)";
            materialText = "(a node is selected)";

        } else {
            defText = "(no material)";
            materialText = "(no material)";
        }

        setStatusText("matDef", " " + defText);
        setStatusText("matName", " " + materialText);
        setStatusText("matUses", usesText);
        setButtonText("mpAdd", addButton);
        setButtonText("matEdit", editButton);
        setButtonText("matRename", renameButton);
    }

    /**
     * Update the additional render state information.
     */
    private void updateRenderState() {
        String faceCullButton = "";

        SelectedSpatial spatial = Maud.getModel().getTarget().getSpatial();
        if (spatial.hasMaterial()) {
            RenderState state = spatial.copyAdditionalRenderState();
            boolean depthTest = state.isDepthTest();
            setChecked("matDepthTest", depthTest);
            boolean wireframe = state.isWireframe();
            setChecked("matWireframe", wireframe);
            RenderState.FaceCullMode faceCullMode = state.getFaceCullMode();
            faceCullButton = faceCullMode.toString();
        } else {
            disableCheckBox("matDepthTest");
            disableCheckBox("matWireframe");
        }

        setButtonText("matFaceCull", faceCullButton);
    }

    /**
     * Update the parameter-index status and next/previous/select-button labels.
     */
    private void updateParameterIndex() {
        String indexStatus;
        String nextButton = "", previousButton = "", selectButton = "";

        Cgm target = Maud.getModel().getTarget();
        SelectedSpatial spatial = target.getSpatial();
        List<String> defined
                = spatial.listMatParamNames("", WhichParams.Defined);
        int numDefined = defined.size();
        if (numDefined > 1) {
            selectButton = "Select parameter";
        }

        int selectedIndex = target.getMatParam().findNameIndex();
        if (selectedIndex >= 0) {
            indexStatus = DescribeUtil.index(selectedIndex, numDefined);
            if (numDefined > 1) {
                nextButton = "+";
                previousButton = "-";
            }
        } else { // no parameter selected
            if (numDefined == 0) {
                indexStatus = "none";
            } else if (numDefined == 1) {
                indexStatus = "one parameter";
            } else {
                indexStatus = String.format("%d parameters", numDefined);
            }
        }

        setStatusText("mpIndex", indexStatus);
        setButtonText("mpNext", nextButton);
        setButtonText("mpPrevious", previousButton);
        setButtonText("mpSelect", selectButton);
    }

    /**
     * Update the parameter-name/type statuses and delete-button label.
     */
    private void updateParameterName() {
        String dButton, nameText, typeText;

        SelectedMatParam param = Maud.getModel().getTarget().getMatParam();
        if (param.isSelected()) {
            dButton = "Delete";
            String name = param.getName();
            nameText = MyString.quote(name);
            VarType varType = param.getVarType();
            typeText = varType.toString();
        } else {
            dButton = "";
            nameText = "none (no parameter selected)";
            typeText = "";
        }

        setStatusText("mpName", " " + nameText);
        setStatusText("mpType", typeText);
        setButtonText("mpDelete", dButton);
    }

    /**
     * Update the parameter-value button.
     */
    private void updateParameterValue() {
        String valueButton = "";

        SelectedMatParam param = Maud.getModel().getTarget().getMatParam();
        if (param.isSelected()) {
            if (param.isOverridden()) {
                valueButton = "(overridden)";

            } else {
                Object value = param.getValue(); // TODO create utility method
                if (value == null || value instanceof String) {
                    String string = (String) value;
                    valueButton = MyString.quote(string);

                } else if (value instanceof Bone) {
                    Bone bone = (Bone) value;
                    valueButton = bone.getName();

                } else if (value instanceof Matrix3f) {
                    Matrix3f m = (Matrix3f) value;
                    StringBuilder builder = new StringBuilder(100);
                    Vector3f row = new Vector3f();
                    for (int i = 0; i < 3; i++) {
                        m.getRow(i, row);
                        builder.append(row);
                    }
                    valueButton = builder.toString();

                } else if (value instanceof Matrix3f[]) {
                    Matrix3f[] ma = (Matrix3f[]) value;
                    valueButton = String.format("length=%d", ma.length);

                } else if (value instanceof Matrix4f) {
                    Matrix4f m = (Matrix4f) value;
                    valueButton = String.format("(%f %f %f %f)(%f %f %f %f)"
                            + "(%f %f %f %f)(%f %f %f %f)",
                            m.m00, m.m01, m.m02, m.m03,
                            m.m10, m.m11, m.m12, m.m13,
                            m.m20, m.m21, m.m22, m.m23,
                            m.m30, m.m31, m.m32, m.m33);

                } else if (value instanceof Matrix4f[]) {
                    Matrix4f[] ma = (Matrix4f[]) value;
                    valueButton = String.format("length=%d", ma.length);

                } else {
                    valueButton = value.toString();
                }
            }
        }

        setButtonText("mpValue", valueButton);
    }

    /**
     * Update the material-select button.
     */
    private void updateSelect() {
        String selectButton;

        Cgm target = Maud.getModel().getTarget();
        List<String> names
                = target.listSpatialNames("", WhichSpatials.Geometries);
        if (names.isEmpty()) {
            selectButton = "";
        } else {
            selectButton = "Select";
        }

        setButtonText("matSelect", selectButton);
    }
}
