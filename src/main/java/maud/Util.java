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
 * Stephen Gold's name may not be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL STEPHEN GOLD BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package maud;

import com.jme3.animation.BoneTrack;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * Utility methods for the Maud application. All methods should be static.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class Util {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Util.class.getName());
    /**
     * local copy of {@link com.jme3.math.Vector3f#UNIT_XYZ}
     */
    final private static Vector3f identityScale = new Vector3f(1f, 1f, 1f);
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Util() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Calculate the bone transform for the specified track and time, using
     * linear interpolation with no blending.
     *
     * @param track (not null)
     * @param time
     * @param storeResult (modified if not null)
     * @return transform (either storeResult or a new instance)
     */
    public static Transform boneTransform(BoneTrack track, float time,
            Transform storeResult) {
        if (storeResult == null) {
            storeResult = new Transform();
        }
        float[] times = track.getTimes();
        int lastFrame = times.length - 1;
        assert lastFrame >= 0 : lastFrame;

        Vector3f[] translations = track.getTranslations();
        Quaternion[] rotations = track.getRotations();
        Vector3f[] scales = track.getScales();

        if (time <= 0f || lastFrame == 0) {
            /*
             * Copy the transform of the first frame.
             */
            storeResult.setTranslation(translations[0]);
            storeResult.setRotation(rotations[0]);
            if (scales == null) {
                storeResult.setScale(identityScale);
            } else {
                storeResult.setScale(scales[0]);
            }

        } else if (time >= times[lastFrame]) {
            /*
             * Copy the transform of the last frame.
             */
            storeResult.setTranslation(translations[lastFrame]);
            storeResult.setRotation(rotations[lastFrame]);
            if (scales == null) {
                storeResult.setScale(identityScale);
            } else {
                storeResult.setScale(scales[lastFrame]);
            }

        } else {
            /*
             * Interpolate between two successive frames.
             */
            interpolateTransform(time, times, translations, rotations, scales,
                    storeResult);
        }

        return storeResult;
    }

    /**
     * Open the specified web page in a new browser or browser tab.
     *
     * @param startUriString URI of the web page (not null)
     * @return true if successful, otherwise false
     */
    public static boolean browseWeb(String startUriString) {
        Validate.nonNull(startUriString, "start uri");

        boolean success = false;
        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                URI startUri = new URI(startUriString);
                Desktop.getDesktop().browse(startUri);
                success = true;
            } catch (IOException | URISyntaxException exception) {
            }
        }

        return success;
    }

    /**
     * Interpolate linearly between keyframes of a bone track.
     *
     * @param time
     * @param times (not null, unaffected)
     * @param translations (not null, unaffected)
     * @param rotations (not null, unaffected)
     * @param scales (may be null, unaffected)
     * @param storeResult (modified if not null)
     * @return transform (either storeResult or a new instance)
     */
    static void interpolateTransform(float time, float[] times,
            Vector3f[] translations, Quaternion[] rotations, Vector3f[] scales,
            Transform storeResult) {
        if (storeResult == null) {
            storeResult = new Transform();
        }

        int lastFrame = times.length - 1;
        int startFrame = -1;
        for (int iFrame = 0; iFrame < lastFrame; iFrame++) {
            if (time >= times[iFrame] && time <= times[iFrame + 1]) {
                startFrame = iFrame;
                break;
            }
        }
        assert startFrame >= 0 : startFrame;
        int endFrame = startFrame + 1;
        float frameDuration = times[endFrame] - times[startFrame];
        assert frameDuration > 0f : frameDuration;
        float fraction = (time - times[startFrame]) / frameDuration;

        Vector3f translation = storeResult.getTranslation();
        translation.interpolateLocal(translations[startFrame],
                translations[endFrame], fraction);

        Quaternion rotation = storeResult.getRotation();
        rotation.set(rotations[startFrame]);
        rotation.nlerp(rotations[endFrame], fraction);

        if (scales == null) {
            storeResult.setScale(identityScale);
        } else {
            Vector3f scale = storeResult.getScale();
            scale.interpolateLocal(scales[startFrame], scales[endFrame],
                    fraction);
        }
    }

    /**
     * Test the specified quaternion for exact identity.
     *
     * @param quaternion which quaternion to test (not null, unaffected)
     * @return true if exactly equal to
     * {@link com.jme3.math.Quaternion#IDENTITY}, otherwise false
     */
    public static boolean isIdentity(Quaternion quaternion) {
        return quaternion.getW() == 1f && quaternion.getX() == 0f
                && quaternion.getY() == 0f && quaternion.getZ() == 0f;
    }

    /**
     * Test the specified transform for exact identity.
     *
     * @param transform which transform to test (not null, unaffected)
     * @return true if exactly equal to
     * {@link com.jme3.math.Transform#IDENTITY}, otherwise false
     */
    public static boolean isIdentity(Transform transform) {
        boolean result = false;

        Vector3f translation = transform.getTranslation();
        if (MyVector3f.isZero(translation)) {
            Quaternion rotation = transform.getRotation();
            if (isIdentity(rotation)) {
                Vector3f scale = transform.getScale();
                result = (scale.x == 1f && scale.y == 1f && scale.z == 1f);
            }
        }

        return result;
    }
}
