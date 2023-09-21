/*
 Copyright (c) 2017-2023, Stephen Gold
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
package maud.menu;

import com.jme3.light.Light;
import com.jme3.scene.control.Control;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.Validate;
import maud.Maud;
import maud.action.ActionPrefix;
import maud.model.cgm.Cgm;
import maud.model.cgm.EditableCgm;
import maud.model.cgm.SelectedLight;
import maud.model.cgm.SelectedOverride;
import maud.model.cgm.SelectedSgc;
import maud.model.cgm.SelectedSpatial;
import maud.model.cgm.SelectedTexture;
import maud.model.cgm.WhichParams;

/**
 * Display simple menus in Maud's editor screen.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class ShowMenus {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of items in a menu, derived from the minimum display
     * height of 720 pixels TODO calculate based on actual height
     */
    final public static int maxItems = 28;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ShowMenus.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ShowMenus() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Display a menu for adding an undefined material parameter using the "new
     * matParam " action prefix.
     *
     * @param namePrefix (not null)
     */
    public static void addNewMatParam(String namePrefix) {
        Validate.nonNull(namePrefix, "name prefix");

        EditableCgm target = Maud.getModel().getTarget();
        SelectedSpatial spatial = target.getSpatial();
        List<String> matParamNames
                = spatial.listMatParamNames(namePrefix, WhichParams.Undefined);
        if (matParamNames.contains(namePrefix)) {
            target.addMatParam(namePrefix);
            return;
        }

        // Build a reduced menu.
        List<String> reducedList = new ArrayList<>(matParamNames);
        MyString.reduce(reducedList, maxItems);
        Collections.sort(reducedList);
        MenuBuilder builder = new MenuBuilder();
        for (String listItem : reducedList) {
            if (matParamNames.contains(listItem)) {
                builder.addEdit(listItem);
            } else {
                builder.addEllipsis(listItem);
            }
        }

        builder.show(ActionPrefix.newMatParam);
    }

    /**
     * Display an "SGC -&gt; Add new" menu.
     */
    public static void addNewSgc() {
        MenuBuilder builder = new MenuBuilder();

        builder.addEdit("Anim");
        builder.addEdit("BetterCharacter");
        SelectedSpatial ss = Maud.getModel().getTarget().getSpatial();
        if (ss.hasSkeletonControls()) {
            builder.addEdit("DynamicAnim");
        }
        builder.addEdit("Ghost");
        builder.addEdit("Character");
        builder.addEdit("RigidBody");
        builder.addEdit("Skeleton");

        builder.show("select menuItem SGC -> Add new -> ");
    }

    /**
     * Display a "Settings -> Remove asset location" menu.
     */
    static void removeAssetLocation() {
        MenuBuilder builder = new MenuBuilder();
        List<String> specs = Maud.getModel().getLocations().listAll();
        for (String spec : specs) {
            builder.addFile(spec);
        }
        builder.show(ActionPrefix.deleteAssetLocationSpec);
    }

    /**
     * Display a menu of files or zip entries.
     *
     * @param names the list of names (not null, unaffected)
     * @param actionPrefix common prefix of the menu's action strings (not null,
     * usually the final character will be a space)
     */
    static void selectFile(List<String> names, String actionPrefix) {
        assert names != null;
        assert actionPrefix != null;

        MenuBuilder builder = new MenuBuilder();
        builder.addFiles(names, maxItems);
        builder.show(actionPrefix);
    }

    /**
     * Display a "select light" menu.
     */
    public static void selectLight() {
        MenuBuilder builder = new MenuBuilder();

        Cgm target = Maud.getModel().getTarget();
        String selectedName = target.getLight().name();
        List<String> names = target.listLightNames(Light.class);
        for (String name : names) {
            if (!name.equals(selectedName)) {
                builder.add(name);
            }
        }
        builder.add(SelectedLight.noLight);

        builder.show(ActionPrefix.selectLight);
    }

    /**
     * Display a menu for selecting a defined material parameter using the
     * "select matParam " action prefix.
     *
     * @param namePrefix (not null)
     */
    public static void selectMatParam(String namePrefix) {
        Validate.nonNull(namePrefix, "name prefix");

        EditableCgm target = Maud.getModel().getTarget();
        SelectedSpatial spatial = target.getSpatial();
        List<String> matParamNames
                = spatial.listMatParamNames(namePrefix, WhichParams.Defined);
        String currentName = target.getMatParam().getName();
        matParamNames.remove(currentName);
        if (matParamNames.contains(namePrefix)) {
            target.getMatParam().select(namePrefix);
            return;
        }

        // Build a reduced menu.
        List<String> reducedList = new ArrayList<>(matParamNames);
        MyString.reduce(reducedList, maxItems);
        Collections.sort(reducedList);
        MenuBuilder builder = new MenuBuilder();
        for (String listItem : reducedList) {
            if (matParamNames.contains(listItem)) {
                builder.add(listItem);
            } else {
                builder.addEllipsis(listItem);
            }
        }

        builder.show(ActionPrefix.selectMatParam);
    }

    /**
     * Display a menu for selecting a material-parameter override using the
     * "select override " action prefix.
     */
    public static void selectOverride() {
        MenuBuilder builder = new MenuBuilder();

        EditableCgm target = Maud.getModel().getTarget();
        List<String> nameList = target.getSpatial().listOverrideNames();
        String selectedName = target.getOverride().parameterName();
        for (String name : nameList) {
            if (!name.equals(selectedName)) {
                builder.add(name);
            }
        }
        builder.add(SelectedOverride.noParam);

        builder.show(ActionPrefix.selectOverride);
    }

    /**
     * Display a "select sgc" menu.
     */
    public static void selectSgc() {
        MenuBuilder builder = new MenuBuilder();

        Cgm target = Maud.getModel().getTarget();
        String selectedName = target.getSgc().name();
        List<String> names = target.listSgcNames(Control.class);
        for (String name : names) {
            if (!name.equals(selectedName)) {
                builder.add(name);
            }
        }
        builder.add(SelectedSgc.noControl);

        builder.show(ActionPrefix.selectSgc);
    }

    /**
     * Display a menu for selecting a texture reference using the "select
     * texture " action prefix.
     */
    public static void selectTexture() {
        SelectedTexture selection = Maud.getModel().getTarget().getTexture();
        boolean isSelected = selection.isSelected();
        List<String> descList = selection.listSelectables("");
        int numSelectables = descList.size();
        if (numSelectables == 1) {
            if (isSelected) {
                selection.deselectAll();
            } else {
                String name = descList.get(0);
                selection.select(name);
            }
            return;
        }

        String selectedDesc;
        if (!isSelected) {
            selectedDesc = "";
        } else if (selection.isNull()) {
            selectedDesc = selection.describeFirstRef();
            assert descList.contains(selectedDesc);
        } else {
            selectedDesc = selection.describe();
            assert descList.contains(selectedDesc);
        }

        MenuBuilder builder = new MenuBuilder();
        for (String desc : descList) {
            if (!desc.equals(selectedDesc)) {
                builder.addTexture(desc);
            }
        }

        builder.show(ActionPrefix.selectTexture);
    }

    /**
     * Display a menu for selecting a user key using the "select userKey "
     * action prefix.
     */
    public static void selectUserKey() {
        MenuBuilder builder = new MenuBuilder();

        EditableCgm target = Maud.getModel().getTarget();
        List<String> keyList = target.getSpatial().listUserKeys();
        String selectedKey = target.getUserData().key();
        for (String key : keyList) {
            if (!key.equals(selectedKey)) {
                builder.add(key);
            }
        }

        builder.show(ActionPrefix.selectUserKey);
    }
}
