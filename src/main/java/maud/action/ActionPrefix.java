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
package maud.action;

import java.util.logging.Logger;

/**
 * Action-string prefixes for Maud's "editor" screen. Each prefix describes a
 * user-interface action requiring one or more (textual) arguments. By
 * convention, action prefixes end with a space (' ').
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ActionPrefix {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ActionPrefix.class.getName());
    /**
     * argument is a name for the new animation
     */
    final public static String copyAnimation = "copy animation ";
    /**
     * argument is a URL specification
     */
    final public static String deleteAssetLocationSpec
            = "delete assetLocationSpec ";
    /**
     * argument is a number of keyframes
     */
    final public static String deleteNextKeyframes = "delete nextKeyframes ";
    /**
     * argument is a number of keyframes
     */
    final public static String deletePreviousKeyframes
            = "delete previousKeyframes ";
    /**
     * argument is the name of an animation in the target C-G model
     */
    final public static String loadAnimation = "load animation ";
    /**
     * remainder is a location index followed by an asset path to a C-G model
     */
    final public static String loadCgmAsset = "load cgm asset ";
    /**
     * argument is a URL spec or defaultLocation
     */
    final public static String loadCgmLocator = "load cgm locator ";
    /**
     * argument is the name of a C-G model on the classpath or else otherName
     */
    final public static String loadCgmNamed = "load cgm named ";
    /**
     * argument is an location index followed by an asset path to a skeleton map
     */
    final public static String loadMapAsset = "load map asset ";
    /**
     * argument is URL spec or else defaultLocation or identityForSource or
     * identityForTarget
     */
    final public static String loadMapLocator = "load map locator ";
    /**
     * argument is the name of a map asset on the classpath
     */
    final public static String loadMapNamed = "load map named ";
    /**
     * argument is the name of an animation in the source C-G model
     */
    final public static String loadSourceAnimation = "load sourceAnimation ";
    /**
     * argument is a location index followed by an asset path to a C-G model
     */
    final public static String loadSourceCgmAsset = "load sourceCgm asset ";
    /**
     * argument is a URL spec or else defaultLocation
     */
    final public static String loadSourceCgmLocator = "load sourceCgm locator ";
    /**
     * argument is the name of a C-G model on the classpath
     */
    final public static String loadSourceCgmNamed = "load sourceCgm named ";
    /**
     * arguments are 2 whichCgms and a name for the new animation
     */
    final public static String newAnimationFromChain
            = "new animation fromChain ";
    /**
     * arguments are a comma-separated list of decimal track indices and an
     * optional name for the new animation
     */
    final public static String newAnimationFromMix = "new animation fromMix ";
    /**
     * argument is a name for the new animation
     */
    final public static String newAnimationFromPose = "new animation fromPose ";
    /**
     * argument is a filesystem path to a directory/folder/JAR/ZIP
     */
    final public static String newAssetLocation = "new assetLocation ";
    /**
     * argument is a URL specification
     */
    final public static String newAssetLocationSpec = "new assetLocationSpec ";
    /**
     * argument is the name of a ShapeType
     */
    final public static String newGhostControl = "new ghostControl ";
    /**
     * arguments are the name of a Light.Type and an optional light name
     */
    final public static String newLight = "new light ";
    /**
     * arguments are the name of the VarType and an optional parameter name
     */
    final public static String newOverride = "new override ";
    /**
     * argument is the name of a ShapeType
     */
    final public static String newRbc = "new rbc ";
    /**
     * arguments are the new type and optional key
     */
    final public static String newUserKey = "new userKey ";
    /**
     * argument is a reduction factor
     */
    final public static String reduceAnimation = "reduce animation ";
    /**
     * argument is a reduction factor
     */
    final public static String reduceTrack = "reduce track ";
    /**
     * argument is the new name for the loaded animation
     */
    final public static String renameAnimation = "rename animation ";
    /**
     * argument is the new name for the selected bone
     */
    final public static String renameBone = "rename bone ";
    /**
     * argument is the new name for the light
     */
    final public static String renameLight = "rename light ";
    /**
     * argument is the new name for the material-parameter override
     */
    final public static String renameOverride = "rename override ";
    /**
     * argument is the new name for the selected spatial
     */
    final public static String renameSpatial = "rename spatial ";
    /**
     * argument is the new name for the key
     */
    final public static String renameUserKey = "rename userKey ";
    /**
     * argument is the new sample rate
     */
    final public static String resampleAnimationAtRate
            = "resample animation atRate ";
    /**
     * argument is the new sample count
     */
    final public static String resampleAnimationToNumber
            = "resample animation toNumber ";
    /**
     * argument is the new sample rate
     */
    final public static String resampleTrackAtRate = "resample track atRate ";
    /**
     * argument is the new sample count
     */
    final public static String resampleTrackToNumber
            = "resample track toNumber ";
    /**
     * argument is the name for the new animation
     */
    final public static String retargetAnimation = "retarget animation ";
    /**
     * argument is a base file path
     */
    final public static String saveCgm = "save cgm ";
    /**
     * argument is a base file path
     */
    final public static String saveCgmUnconfirmed = "save cgmUnconfirmed ";
    /**
     * argument is a base file path
     */
    final public static String saveMap = "save map ";
    /**
     * argument is a base file path
     */
    final public static String saveMapUnconfirmed = "save mapUnconfirmed ";
    /**
     * arguments are a spatial name and a deduplication index
     */
    final static String selectAnimControl = "select animControl ";
    /**
     * argument is the name of a scene-view axis drag effect
     */
    final public static String selectAxesDragEffect = "select axesDragEffect ";
    /**
     * argument is the name of a scene-view axis subject
     */
    final public static String selectAxesSubject = "select axesSubject ";
    /**
     * argument is the name of a background
     */
    final public static String selectBackground = "select background ";
    /**
     * argument is the name of a bone or a prefix thereof
     */
    final public static String selectBone = "select bone ";
    /**
     * argument is the name of a target bone, possibly preceded by "!"
     */
    final public static String selectBoneChild = "select boneChild ";
    /**
     * argument is the name of an EdgeFilteringMode value
     */
    final public static String selectEdgeFilter = "select edgeFilter ";
    /**
     * argument is a light name or noLight
     */
    final public static String selectLight = "select light ";
    /**
     * argument is the name of a geometry or a prefix thereof
     */
    final public static String selectGeometry = "select geometry ";
    /**
     * argument is the id of a physics joint or a prefix thereof
     */
    final public static String selectJoint = "select joint ";
    /**
     * argument is a pre-existing material-parameter name
     */
    final public static String selectMatParam = "select matParam ";
    /**
     * argument is the menu path of a menu item
     */
    final public static String selectMenuItem = "select menuItem ";
    /**
     * argument is the name of a movement-mode enum value
     */
    final public static String selectMovement = "select movement ";
    /**
     * argument is the name of an orbit-center enum value
     */
    final public static String selectOrbitCenter = "select orbitCenter ";
    /**
     * argument is a pre-existing parameter name
     */
    final public static String selectOverride = "select override ";
    /**
     * argument is the name of a physics object or a prefix thereof
     */
    final public static String selectPhysics = "select physics ";
    /**
     * argument is the name of a RigidBodyParameter
     */
    final public static String selectPhysicsRbp = "select physicsRbp ";
    /**
     * argument is the name of a PlatformType
     */
    final public static String selectPlatformType = "select platformType ";
    /**
     * argument is the name of a projection-mode enum value
     */
    final public static String selectProjection = "select projection ";
    /**
     * argument is the name of a ShowBones value
     */
    final public static String selectSceneBones = "select sceneBones ";
    /**
     * argument is the name of a ShowBones value
     */
    final public static String selectScoreBonesNone = "select scoreBonesNone ";
    /**
     * argument is the name of a ShowBones value
     */
    final public static String selectScoreBonesWhen = "select scoreBonesWhen ";
    /**
     * argument is an SGC name or noControl
     */
    final public static String selectSgc = "select sgc ";
    /**
     * argument is the id of a physics shape or a prefix thereof
     */
    final public static String selectShape = "select shape ";
    /**
     * argument is the name of a ShapeParameter
     */
    final public static String selectShapeParm = "select shapeParm ";
    /**
     * argument is the name of a SkeletonColors
     */
    final public static String selectSkeletonColor = "select skeletonColor ";
    /**
     * arguments are a spatial name and a de-duplication index
     */
    final static String selectSourceAnimControl = "select sourceAnimControl ";
    /**
     * argument is the name of a source bone or a prefix thereof
     */
    final public static String selectSourceBone = "select sourceBone ";
    /**
     * argument is the name of a spatial or a prefix thereof
     */
    final public static String selectSpatial = "select spatial ";
    /**
     * arguments are the quoted name of a spatial and a bracketed index
     */
    final public static String selectSpatialChild = "select spatialChild ";
    /**
     * argument is the name of a tool window
     */
    final static String selectTool = "select tool ";
    /**
     * arguments are the name of a tool window and decimal x,y coordinates
     */
    final public static String selectToolAt = "select toolAt ";
    /**
     * argument is the name of a triangle mode
     */
    final public static String selectTriangleMode = "select triangleMode ";
    /**
     * argument is the name of a quaternion interpolation technique
     */
    final public static String selectTweenRotations = "select tweenRotations ";
    /**
     * argument is the name of a vector interpolation technique
     */
    final public static String selectTweenScales = "select tweenScales ";
    /**
     * argument is the name of a vector interpolation technique
     */
    final public static String selectTweenTranslations
            = "select tweenTranslations ";
    /**
     * argument is a pre-existing user key
     */
    final public static String selectUserKey = "select userKey ";
    /**
     * argument is indexBase plus a vertex index
     */
    final public static String selectVertex = "select vertex ";
    /**
     * argument is a weight index (&ge;0, &lt;4)
     */
    final static String selectVertexBone = "select vertexBone ";
    /**
     * argument is the name of a VertexBuffer.Type
     */
    final public static String selectVertexBuffer = "select vertexBuffer ";
    /**
     * argument is the name of a view mode
     */
    final public static String selectViewMode = "select viewMode ";
    /**
     * argument is the name of a batch hint
     */
    final public static String setBatchHint = "set batchHint ";
    /**
     * argument is the decimal number of bits
     */
    final public static String setColorDepth = "set colorDepth ";
    /**
     * argument is the name of a cull hint
     */
    final public static String setCullHint = "set cullHint ";
    /**
     * argument is "true" or "false"
     */
    final public static String setDegrees = "set degrees ";
    /**
     * argument is "true" or "false"
     */
    final public static String setDiagnose = "set diagnose ";
    /**
     * arguments are the decimal width in pixels, an "x", and the decimal height
     * in pixels
     */
    final public static String setDimensions = "set dimensions ";
    /**
     * argument is a new duration in seconds
     */
    final public static String setDurationProportional
            = "set duration proportional ";
    /**
     * argument is a new duration in seconds
     */
    final public static String setDurationSame = "set duration same ";
    /**
     * argument is 0 or 1
     */
    final public static String setIndexBase = "set indexBase ";
    /**
     * argument is a decimal value
     */
    final public static String setMapSize = "set mapSize ";
    /**
     * argument is the number of samples per pixel, formatted using
     * {@link maud.MaudUtil#describeMsaaFactor(int)}
     */
    final public static String setMsaaFactor = "set msaaFactor ";
    /**
     * argument is a material-parameter value
     */
    final static String setMatParamValue = "set matParamValue ";
    /**
     * argument is a decimal value
     */
    final public static String setNumSplits = "set numSplits ";
    /**
     * argument is a material-parameter value or "null"
     */
    final static String setOverrideValue = "set overrideValue ";
    /**
     * arguments are the parameter name and a decimal value
     */
    final public static String setPhysicsRbpValue = "set physicsRbpValue ";
    /**
     * argument is a boolean value
     */
    final public static String setPhysicsRendered = "set physicsRendered ";
    /**
     * argument is the decimal refresh rate in Hertz
     */
    final public static String setRefreshRate = "set refreshRate ";
    /**
     * argument is the name of a queue bucket
     */
    final public static String setQueueBucket = "set queueBucket ";
    /**
     * argument is the name of a shadow mode
     */
    final public static String setShadowMode = "set shadowMode ";
    /**
     * argument is a boolean value
     */
    final public static String setShadowsRendered = "set shadowsRendered ";
    /**
     * arguments are the parameter name and a decimal value
     */
    final public static String setShapeParmValue = "set shapeParmValue ";
    /**
     * argument is a boolean value
     */
    final public static String setSkySimulated = "set skySimulated ";
    /**
     * argument is the new value
     */
    final public static String setUserData = "set userData ";
    /**
     * argument is the name of a license type
     */
    final public static String viewLicense = "view license ";
    /**
     * argument is the weight to apply to end-time keyframes
     */
    final public static String wrapAnimation = "wrap animation ";
    /**
     * argument is the weight to apply to the end-time keyframe
     */
    final public static String wrapTrack = "wrap track ";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ActionPrefix() {
    }
}
