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
package maud.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import maud.DescribeUtil;
import maud.CharEd;
import maud.tool.HistoryTool;

/**
 * Edit history for Maud. Note: not checkpointed!
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class History {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(History.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;add a checkpoint before each edit/unload, false&rarr; add a
     * checkpoint only when the user manually requests one
     */
    private static boolean autoAddFlag = true;
    /**
     * index of the next checkpoint slot to use
     */
    private static int nextIndex = 0;
    /**
     * list of checkpoint slots
     */
    final private static List<Checkpoint> checkpoints = new ArrayList<>(8);
    /**
     * list of events (C-G model edits, loads, and saves) since the last
     * checkpoint
     */
    final private static List<String> eventDescriptions = new ArrayList<>(20);
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private History() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Create a checkpoint in the next slot, discarding any checkpoints beyond
     * that slot so that the new one is also the last.
     *
     * @return index of the new checkpoint (&ge;0)
     */
    public static int addCheckpoint() {
        String message;
        while (hasVulnerable()) {
            int lastIndex = checkpoints.size() - 1;
            checkpoints.remove(lastIndex);
            message = "discard checkpoint" + DescribeUtil.index(lastIndex);
            logger.info(message);
        }

        Checkpoint newbie = new Checkpoint(eventDescriptions);
        checkpoints.add(newbie);
        eventDescriptions.clear();

        String id = DescribeUtil.index(nextIndex);
        message = "add new checkpoint" + id;
        logger.info(message);

        enforceLimit();

        int result = nextIndex;
        ++nextIndex;
        assert checkpoints.size() == nextIndex;

        return result;
    }

    /**
     * Record an event: an edit/load/save of the map or the target C-G model.
     *
     * @param description (not null, not empty)
     */
    public static void addEvent(String description) {
        Validate.nonEmpty(description, "description");

        logger.info(description);
        eventDescriptions.add(description);
    }

    /**
     * Create a checkpoint if auto-add mode is enabled.
     */
    public static void autoAdd() {
        if (autoAddFlag) {
            addCheckpoint();
        }
    }

    /**
     * Clear the history.
     */
    public static void clear() {
        nextIndex = 0;
        checkpoints.clear();
        eventDescriptions.clear();
    }

    /**
     * Count the available checkpoints.
     *
     * @return count (&ge;0)
     */
    public static int countCheckpoints() {
        int count = checkpoints.size();
        return count;
    }

    /**
     * Enforce the configured limit on the number of checkpoints. This is
     * invoked each time a checkpoint is added or the limit is reconfigured.
     */
    public static void enforceLimit() {
        int maxCheckpoints = CharEd.getModel().getMisc().maxCheckpoints();
        while (countCheckpoints() > maxCheckpoints) {
            checkpoints.remove(0);

            String message = "discard checkpoint" + DescribeUtil.index(0);
            logger.info(message);

            --nextIndex;
        }
    }

    /**
     * Access the indexed checkpoint.
     *
     * @param index (&ge;0)
     * @return the pre-existing instance (not null)
     */
    public static Checkpoint getCheckpoint(int index) {
        Checkpoint result = checkpoints.get(index);
        return result;
    }

    /**
     * Read the index of the next slot to use.
     *
     * @return index (&ge;0)
     */
    public static int getNextIndex() {
        return nextIndex;
    }

    /**
     * Test whether any recent checkpoints would be discarded by
     * {@link #addCheckpoint()}.
     *
     * @return true if any checkpoint is vulnerable, otherwise false
     */
    public static boolean hasVulnerable() {
        int numVulnerable = checkpoints.size() - nextIndex;
        assert numVulnerable >= 0 : numVulnerable;
        if (numVulnerable > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether to add checkpoints automatically.
     *
     * @return true to add automatically, otherwise false
     */
    public static boolean isAutoAdd() {
        return autoAddFlag;
    }

    /**
     * Enumerate all events since the previous checkpoint.
     *
     * @return a new list of descriptions
     */
    public static List<String> listRecentEvents() {
        List<String> result = new ArrayList<>(eventDescriptions);
        return result;
    }

    /**
     * If the next slot has a checkpoint, restore that checkpoint and increment
     * the index.
     */
    public static void redo() {
        String message;
        if (checkpoints.size() > nextIndex) {
            Checkpoint next = checkpoints.get(nextIndex);
            next.restore();
            eventDescriptions.clear();
            message = "redo to checkpoint" + DescribeUtil.index(nextIndex);
            logger.info(message);
            ++nextIndex;
        } else {
            message = "There is nothing to redo!";
            warn(message);
        }

        setAutoScroll();
    }

    /**
     * Restore the final checkpoint and update the index.
     */
    public static void redoAll() {
        String message;
        if (checkpoints.size() > nextIndex) {
            int lastIndex = checkpoints.size() - 1;
            Checkpoint last = checkpoints.get(lastIndex);
            last.restore();
            eventDescriptions.clear();
            nextIndex = checkpoints.size();

            message = "redo to checkpoint" + DescribeUtil.index(lastIndex);
            logger.info(message);

        } else {
            message = "There is nothing to redo!";
            warn(message);
        }

        setAutoScroll();
    }

    /**
     * Alter whether to add checkpoints automatically.
     *
     * @param newSetting true &rarr; add automatically, false &rarr; add
     * manually only
     */
    public static void setAutoAdd(boolean newSetting) {
        autoAddFlag = newSetting;
    }

    /**
     * Configure the History Tool to scroll to "you are here" on its next
     * update.
     */
    public static void setAutoScroll() {
        HistoryTool tool = (HistoryTool) CharEd.gui.findTool("history");
        tool.setAutoScroll();
    }

    /**
     * If a previous slot exists, restore its checkpoint and decrement the
     * index. If there are no vulnerable checkpoints, add one.
     */
    public static void undo() {
        String message;
        boolean noneVulnerable = !hasVulnerable();
        if (nextIndex > 1 || noneVulnerable && nextIndex > 0) {
            if (noneVulnerable) {
                Checkpoint newbie = new Checkpoint(eventDescriptions);
                checkpoints.add(newbie);

                String id = DescribeUtil.index(nextIndex);
                message = "add precautionary checkpoint" + id;
                logger.info(message);

                enforceLimit();

            } else {
                --nextIndex;
            }
            int getIndex = nextIndex - 1;
            Checkpoint previous = checkpoints.get(getIndex);
            previous.restore();
            eventDescriptions.clear();

            message = "undo to checkpoint" + DescribeUtil.index(getIndex);
            logger.info(message);

        } else {
            message = "There is nothing to undo!";
            warn(message);
        }

        setAutoScroll();
    }
    // *************************************************************************
    // private methods

    /**
     * Issue a warning to the user.
     *
     * @param message (not null, not empty)
     */
    private static void warn(String message) {
        assert message != null;
        assert !message.isEmpty();

        logger.warning(message);
        CharEd.getModel().getMisc().setStatusMessage(message);
    }
}
