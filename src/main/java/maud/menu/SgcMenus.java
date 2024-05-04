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
package maud.menu;

import java.util.logging.Logger;
import jme3utilities.MyString;
import maud.CharEd;
import maud.action.ActionPrefix;
import maud.dialog.EditorDialogs;
import maud.model.cgm.SelectedSpatial;
import maud.tool.EditorTools;

/**
 * Menus in Maud's editor screen that deal with scene-graph controls.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SgcMenus {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SgcMenus.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SgcMenus() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Build an SGC menu.
     *
     * @param builder the menu builder to use (not null, modified)
     */
    static void buildSgcMenu(MenuBuilder builder) {
        builder.addTool("Tool");
        builder.addSubmenu("Select");
        builder.addSubmenu("Add new");
        if (CharEd.getModel().getTarget().getSgc().isSelected()) {
            builder.addDialog("Delete"); // user must confirm
            builder.add("Deselect");
        }
    }

    /**
     * Handle a "select menuItem" action from the SGC menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean menuSgc(String remainder) {
        boolean handled = true;
        String addPrefix = "Add new" + EditorMenus.menuPathSeparator;
        if (remainder.startsWith(addPrefix)) {
            String arg = MyString.remainder(remainder, addPrefix);
            handled = menuSgcAdd(arg);

        } else {
            switch (remainder) {
                case "Add new":
                    ShowMenus.addNewSgc();
                    break;

                case "Delete":
                    EditorDialogs.deleteSgc();
                    break;

                case "Deselect":
                    CharEd.getModel().getTarget().getSgc().selectNone();
                    break;

                case "Select":
                    ShowMenus.selectSgc();
                    break;

                case "Tool":
                    EditorTools.select("sgc");
                    break;

                default:
                    handled = false;
            }
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Handle a "select menuItem" action from the "SGC -> Add new" menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuSgcAdd(String remainder) {
        boolean handled = true;
        SelectedSpatial spatial = CharEd.getModel().getTarget().getSpatial();
        switch (remainder) {
            case "Anim":
                spatial.addAnimControl();
                break;

            case "BetterCharacter":
                spatial.addBetterCharacterControl();
                break;

            case "Character":
                PhysicsMenus.showShapeTypeMenu(ActionPrefix.newMcc, false);
                break;

            case "DynamicAnim":
                spatial.addRagdollControl();
                break;

            case "Ghost":
                PhysicsMenus.showShapeTypeMenu(ActionPrefix.newGhostControl,
                        true);
                break;

            case "RigidBody":
                PhysicsMenus.showShapeTypeMenu(ActionPrefix.newRbc, true);
                break;

            case "Skeleton":
                spatial.addSkeletonControl();
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
