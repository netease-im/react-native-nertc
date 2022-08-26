package com.reactnativenertc;

import static android.app.Activity.RESULT_OK;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.view.View;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;
import com.netease.lava.api.Trace;
import com.netease.lava.nertc.sdk.LastmileProbeResult;
import com.netease.lava.nertc.sdk.NERtcCallbackEx;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcOption;
import com.netease.lava.nertc.sdk.audio.NERtcAudioStreamType;
import com.netease.lava.nertc.sdk.stats.NERtcAudioLayerRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioLayerSendStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioSendStats;
import com.netease.lava.nertc.sdk.stats.NERtcAudioVolumeInfo;
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo;
import com.netease.lava.nertc.sdk.stats.NERtcStats;
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver;
import com.netease.lava.nertc.sdk.stats.NERtcVideoLayerRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcVideoLayerSendStats;
import com.netease.lava.nertc.sdk.stats.NERtcVideoRecvStats;
import com.netease.lava.nertc.sdk.stats.NERtcVideoSendStats;
import com.netease.lava.nertc.sdk.video.NERtcEncodeConfig;
import com.netease.lava.nertc.sdk.video.NERtcRemoteVideoStreamType;
import com.netease.lava.nertc.sdk.video.NERtcScreenConfig;
import com.netease.lava.nertc.sdk.video.NERtcVideoConfig;
import com.netease.lava.nertc.sdk.video.NERtcVideoStreamType;
import com.netease.lava.nertc.sdk.video.NERtcVideoView;

@ReactModule(name = NertcModule.NAME)
public class NertcModule extends ReactContextBaseJavaModule implements NERtcCallbackEx, NERtcStatsObserver, ActivityEventListener {
    public static final String NAME = "Nertc";
    private static final String TAG = "NertcModule";

    private final ReactApplicationContext reactContext;
    private NERtcScreenConfig screenConfig;

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 10000;

    public NertcModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Set up any upstream listeners or background tasks as necessary
    }
    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }

    @ReactMethod
    public void setupEngineWithContext(final ReadableMap params, Promise promise) {
        try {
            String appKey = params.getString("appKey");
            String logDir = params.getString("logDir");
            int logLevel = params.getInt("logLevel");

            Trace.i(TAG, String.format("setupEngineWithContext appKey=******, logDir=%s, logLevel=%d", logDir, logLevel));

            NERtcOption option = new NERtcOption();
            option.logDir = logDir;
            option.logLevel = logLevel;

            NERtcEx.getInstance().init(getReactApplicationContext(), appKey, this, option);
            reactContext.addActivityEventListener(this);
            promise.resolve(NERtcConstants.ErrorCode.OK);
        } catch (Exception e) {
            promise.resolve(NERtcConstants.ErrorCode.ENGINE_ERROR_FATAL);
        }
    }

    @ReactMethod
    public void destroyEngine(Promise promise) {
        Trace.i(TAG, "destroyEngine");
        NERtcEx.getInstance().release();
        promise.resolve(NERtcConstants.ErrorCode.OK);
    }

    @ReactMethod
    public void setStatsObserver(boolean enable, Promise promise) {
        Trace.i(TAG, "setStatsObserver " + enable);
        if (enable) {
            NERtcEx.getInstance().setStatsObserver(this);
        } else {
            NERtcEx.getInstance().setStatsObserver(null);
        }

        promise.resolve(NERtcConstants.ErrorCode.OK);
    }

    @ReactMethod
    public void joinChannel(final ReadableMap params, Promise promise) {
        String token = params.getString("token");
        String channelName = params.getString("channelName");
        long uid = params.getInt("myUid");
        Trace.i(TAG, String.format("joinChannel token=******, channelName=%s, myUid=%d", channelName, uid));
        int ret = NERtcEx.getInstance().joinChannel(token, channelName, uid);
        promise.resolve(ret);
    }

    @ReactMethod
    public void leaveChannel(Promise promise) {
        Trace.i(TAG, "leaveChannel");
        int ret = NERtcEx.getInstance().leaveChannel();
        promise.resolve(ret);
    }

    @ReactMethod
    public void setupLocalVideoCanvas(final ReadableMap view, Promise promise) {
        Trace.i(TAG, "setupLocalVideoCanvas " + view);
        final int viewTag = view.getInt("reactTag");
        final boolean isMediaOverlay = view.getBoolean("isMediaOverlay");
        final int mirrorMode = view.getInt("mirrorMode");
        final int renderMode = view.getInt("renderMode");
        final UIManagerModule uiMgr = this.reactContext.getNativeModule(UIManagerModule.class);
        uiMgr.addUIBlock(nativeViewHierarchyManager -> {
            RCTNERtcVideoView nativeView = (RCTNERtcVideoView) nativeViewHierarchyManager.resolveView(viewTag);
            NERtcVideoView canvas = null;

            if (nativeView != null) {
                canvas = nativeView.getView();
            } else {
                Trace.w(TAG, "setupLocalVideoCanvas nativeView is null");
            }

            if (canvas != null) {
                canvas.clearImage();
                canvas.setZOrderMediaOverlay(isMediaOverlay);
                setMirrorMode(canvas, mirrorMode);
                canvas.setScalingType(renderMode);
            } else {
                Trace.w(TAG, "setupLocalVideoCanvas canvas is null");
            }

            int ret = NERtcEx.getInstance().setupLocalVideoCanvas(canvas);
            promise.resolve(ret);
        });
    }

    @ReactMethod
    public void setupRemoteVideoCanvas(final ReadableMap view, Promise promise) {
        Trace.i(TAG, "setupRemoteVideoCanvas " + view);
        final int viewTag = view.getInt("reactTag");
        final boolean isMediaOverlay = view.getBoolean("isMediaOverlay");
        final int mirrorMode = view.getInt("mirrorMode");
        final int renderMode = view.getInt("renderMode");
        final int uid = view.getInt("userID");
        final UIManagerModule uiMgr = this.reactContext.getNativeModule(UIManagerModule.class);
        uiMgr.addUIBlock(nativeViewHierarchyManager -> {
            RCTNERtcVideoView nativeView = (RCTNERtcVideoView) nativeViewHierarchyManager.resolveView(viewTag);
            NERtcVideoView canvas = null;

            if (nativeView != null) {
                canvas = nativeView.getView();
            } else {
                Trace.w(TAG, "setupRemoteVideoCanvas nativeView is null");
            }

            if (canvas != null) {
                canvas.clearImage();
                canvas.setZOrderMediaOverlay(isMediaOverlay);
                setMirrorMode(canvas, mirrorMode);
                canvas.setScalingType(renderMode);
            } else {
                Trace.w(TAG, "setupRemoteVideoCanvas canvas is null");
            }

            int ret = NERtcEx.getInstance().setupRemoteVideoCanvas(canvas, uid);
            promise.resolve(ret);
        });
    }

    @ReactMethod
    public void startPreview(Promise promise) {
        Trace.i(TAG, "startPreview");
        int ret = NERtcEx.getInstance().startVideoPreview();
        promise.resolve(ret);
    }

    @ReactMethod
    public void stopPreview(Promise promise) {
        Trace.i(TAG, "stopPreview");
        int ret = NERtcEx.getInstance().stopVideoPreview();
        promise.resolve(ret);
    }

    private NERtcEncodeConfig.NERtcVideoFrameRate getNERtcVideoFrameRate(int val) {
        switch (val) {
            case 0:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_DEFAULT;
            case 7:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_7;
            case 10:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_10;
            case 15:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
            case 24:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_24;
            case 30:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_30;
            default:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_DEFAULT;

        }
    }

    private NERtcVideoConfig.NERtcDegradationPreference getNERtcDegradationPreference(int val) {
        switch (val) {
            case 0:
                return NERtcVideoConfig.NERtcDegradationPreference.DEGRADATION_DEFAULT;
            case 1:
                return NERtcVideoConfig.NERtcDegradationPreference.DEGRADATION_MAINTAIN_FRAMERATE;
            case 2:
                return NERtcVideoConfig.NERtcDegradationPreference.DEGRADATION_MAINTAIN_QUALITY;
            case 3:
                return NERtcVideoConfig.NERtcDegradationPreference.DEGRADATION_BALANCED;
            default:
                return NERtcVideoConfig.NERtcDegradationPreference.DEGRADATION_DEFAULT;
        }
    }

    private NERtcVideoConfig.NERtcVideoMirrorMode getNERtcVideoMirrorMode(int val) {
        switch (val) {
            case 0:
                return NERtcVideoConfig.NERtcVideoMirrorMode.VIDEO_MIRROR_MODE_AUTO;
            case 1:
                return NERtcVideoConfig.NERtcVideoMirrorMode.VIDEO_MIRROR_MODE_ENABLED;
            case 2:
                return NERtcVideoConfig.NERtcVideoMirrorMode.VIDEO_MIRROR_MODE_DISABLED;
            default:
                return NERtcVideoConfig.NERtcVideoMirrorMode.VIDEO_MIRROR_MODE_AUTO;
        }
    }

    private NERtcVideoConfig.NERtcVideoOutputOrientationMode getNERtcVideoOrientationMode(int val) {
        switch (val) {
            case 0:
                return NERtcVideoConfig.NERtcVideoOutputOrientationMode.VIDEO_OUTPUT_ORIENTATION_MODE_ADAPTATIVE;
            case 1:
                return NERtcVideoConfig.NERtcVideoOutputOrientationMode.VIDEO_OUTPUT_ORIENTATION_MODE_FIXED_LANDSCAPE;
            case 2:
                return NERtcVideoConfig.NERtcVideoOutputOrientationMode.VIDEO_OUTPUT_ORIENTATION_MODE_FIXED_PORTRAIT;
            default:
                return NERtcVideoConfig.NERtcVideoOutputOrientationMode.VIDEO_OUTPUT_ORIENTATION_MODE_ADAPTATIVE;
        }
    }

    @ReactMethod
    public void setLocalVideoConfig(final ReadableMap params, Promise promise) {
        Trace.i(TAG, "setLocalVideoConfig " + params);
        final int maxProfile = params.getInt("maxProfile");
        final int frameRate = params.getInt("frameRate");
        final int minFrameRate = params.getInt("minFrameRate");
        final int bitrate = params.getInt("bitrate");
        final int minBitrate = params.getInt("minBitrate");
        final int width = params.getInt("width");
        final int height = params.getInt("height");
        final int cropMode = params.getInt("cropMode");
        final int degradationPreference = params.getInt("degradationPreference");
        final int mirrorMode = params.getInt("mirrorMode");
        final int orientationMode = params.getInt("orientationMode");

        NERtcVideoConfig config = new NERtcVideoConfig();
        config.videoProfile = maxProfile;
        config.frameRate = getNERtcVideoFrameRate(frameRate);
        config.minFramerate = minFrameRate;
        config.bitrate = bitrate;
        config.minBitrate = minBitrate;
        config.width = width;
        config.height = height;
        config.videoCropMode = cropMode;
        config.degradationPrefer = getNERtcDegradationPreference(degradationPreference);
        config.mirrorMode = getNERtcVideoMirrorMode(mirrorMode);
        config.orientationMode = getNERtcVideoOrientationMode(orientationMode);
        int ret = NERtcEx.getInstance().setLocalVideoConfig(config);
        promise.resolve(ret);
    }

    @ReactMethod
    public void setLoudspeakerMode(boolean enable, Promise promise) {
        Trace.i(TAG, "setLoudspeakerMode " + enable);
        int ret = NERtcEx.getInstance().setSpeakerphoneOn(enable);
        promise.resolve(ret);
    }

    @ReactMethod
    public void enableLocalVideo(boolean enable, Promise promise) {
        Trace.i(TAG, "enableLocalVideo " + enable);
        int ret = NERtcEx.getInstance().enableLocalVideo(enable);
        promise.resolve(ret);
    }

    @ReactMethod
    public void enableLocalAudio(boolean enable, Promise promise) {
        Trace.i(TAG, "enableLocalAudio " + enable);
        int ret = NERtcEx.getInstance().enableLocalAudio(enable);
        promise.resolve(ret);
    }

    @ReactMethod
    public void muteLocalAudio(boolean mute, Promise promise) {
        Trace.i(TAG, "muteLocalAudio " + mute);
        int ret = NERtcEx.getInstance().muteLocalAudioStream(mute);
        promise.resolve(ret);
    }

    @ReactMethod
    public void muteLocalVideo(boolean mute, Promise promise) {
        Trace.i(TAG, "muteLocalVideo " + mute);
        int ret = NERtcEx.getInstance().muteLocalVideoStream(mute);
        promise.resolve(ret);
    }

    @ReactMethod
    public void switchCamera(Promise promise) {
        Trace.i(TAG, "switchCamera");
        int ret = NERtcEx.getInstance().switchCamera();
        promise.resolve(ret);
    }

    @ReactMethod
    public void setupLocalSubStreamVideoCanvas(final ReadableMap view, Promise promise) {
        Trace.i(TAG, "setupLocalSubStreamVideoCanvas " + view);
        final int viewTag = view.getInt("reactTag");
        final boolean isMediaOverlay = view.getBoolean("isMediaOverlay");
        final int renderMode = view.getInt("renderMode");
        final int mirrorMode = view.getInt("mirrorMode");

        final UIManagerModule uiMgr = this.reactContext.getNativeModule(UIManagerModule.class);
        uiMgr.addUIBlock(nativeViewHierarchyManager -> {
            RCTNERtcVideoView nativeView = (RCTNERtcVideoView) nativeViewHierarchyManager.resolveView(viewTag);
            NERtcVideoView canvas = null;

            if (nativeView != null) {
                canvas = nativeView.getView();
            } else {
                Trace.w(TAG, "setupLocalSubStreamVideoCanvas nativeView is null");
            }

            if (canvas != null) {
                canvas.clearImage();
                setMirrorMode(canvas, mirrorMode);
                canvas.setZOrderMediaOverlay(isMediaOverlay);
                canvas.setScalingType(renderMode);
            } else {
                Trace.w(TAG, "setupLocalSubStreamVideoCanvas canvas is null");
            }

            int ret = NERtcEx.getInstance().setupLocalSubStreamVideoCanvas(canvas);
            promise.resolve(ret);
        });
    }

    @ReactMethod
    public void setupRemoteSubStreamVideoCanvas(final ReadableMap view, Promise promise) {
        Trace.i(TAG, "setupRemoteSubStreamVideoCanvas " + view);
        long uid =  view.getInt("userID");
        final int viewTag = view.getInt("reactTag");
        final boolean isMediaOverlay = view.getBoolean("isMediaOverlay");
        final int renderMode = view.getInt("renderMode");
        final int mirrorMode = view.getInt("mirrorMode");
        final UIManagerModule uiMgr = this.reactContext.getNativeModule(UIManagerModule.class);

        uiMgr.addUIBlock(nativeViewHierarchyManager -> {
            RCTNERtcVideoView nativeView = (RCTNERtcVideoView) nativeViewHierarchyManager.resolveView(viewTag);
            NERtcVideoView canvas = null;

            if (nativeView != null) {
                canvas = nativeView.getView();
            } else {
                Trace.w(TAG, "setupRemoteSubStreamVideoCanvas nativeView is null");
            }

            if (canvas != null) {
                canvas.clearImage();
                setMirrorMode(canvas, mirrorMode);
                canvas.setZOrderMediaOverlay(isMediaOverlay);
                canvas.setScalingType(renderMode);
            } else {
                Trace.w(TAG, "setupRemoteSubStreamVideoCanvas canvas is null");
            }

            int ret = NERtcEx.getInstance().setupRemoteSubStreamVideoCanvas(canvas, uid);
            promise.resolve(ret);
        });
    }

    private void setMirrorMode(NERtcVideoView view, int mirrorMode) {
        if (mirrorMode == 1) {
            view.setMirror(true);
        } else if (mirrorMode == 2) {
            view.setMirror(false);
        }
    }

    @ReactMethod
    public void startScreenCapture(final ReadableMap params, Promise promise) {
        Trace.i(TAG, "startScreenCapture " + params);
        int maxProfile = params.getInt("maxProfile");
        int frameRate = params.getInt("frameRate");
        int minFrameRate = params.getInt("minFrameRate");
        int bitrate = params.getInt("bitrate");
        int minBitrate = params.getInt("minBitrate");
        int contentPrefer = params.getInt("contentPrefer");

        screenConfig = new NERtcScreenConfig();
        screenConfig.videoProfile = maxProfile;
        screenConfig.frameRate = getVideoFrameRate(frameRate);
        screenConfig.minFramerate = minFrameRate;
        screenConfig.bitrate = bitrate;
        screenConfig.minBitrate = minBitrate;
        screenConfig.contentPrefer = getSubStreamContentPrefer(contentPrefer);
        int ret = requestScreenCapture();
        promise.resolve(ret);
    }

    NERtcEncodeConfig.NERtcVideoFrameRate getVideoFrameRate(int fps) {
        switch (fps) {
            case 0:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_DEFAULT;
            case 7:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_7;
            case 10:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_10;
            case 15:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_15;
            case 24:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_24;
            case 30:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_30;
            default:
                return NERtcEncodeConfig.NERtcVideoFrameRate.FRAME_RATE_FPS_DEFAULT;
        }
    }

    NERtcScreenConfig.NERtcSubStreamContentPrefer getSubStreamContentPrefer(int prefer) {
        switch (prefer) {
            case 0:
                return NERtcScreenConfig.NERtcSubStreamContentPrefer.CONTENT_PREFER_MOTION;
            case 1:
                return NERtcScreenConfig.NERtcSubStreamContentPrefer.CONTENT_PREFER_DETAILS;
            default:
                return NERtcScreenConfig.NERtcSubStreamContentPrefer.CONTENT_PREFER_MOTION;
        }
    }

    @ReactMethod
    public void stopScreenCapture(Promise promise) {
        Trace.i(TAG, "stopScreenCapture");
        NERtcEx.getInstance().stopScreenCapture();
        screenConfig = null;
        promise.resolve(NERtcConstants.ErrorCode.OK);
    }

    @ReactMethod
    public void setExternalVideoSource(boolean enable, Promise promise) {
        Trace.i(TAG, "setExternalVideoSource " + enable);
        int ret = NERtcEx.getInstance().setExternalVideoSource(enable);
        promise.resolve(ret);
    }

    @ReactMethod
    public void subscribeRemoteVideo(final ReadableMap params, Promise promise) {
        Trace.i(TAG, "subscribeRemoteVideo " + params);
        boolean subscribe = params.getBoolean("subscribe");
        long uid = params.getInt("userID");
        int streamType = params.getInt("streamType");

        int ret = NERtcEx.getInstance().subscribeRemoteVideoStream(uid, getVideoStreamType(streamType), subscribe);
        promise.resolve(ret);
    }

    @ReactMethod
    public void subscribeRemoteSubStreamVideo(final ReadableMap params, Promise promise) {
        Trace.i(TAG, "subscribeRemoteSubStreamVideo " + params);
        boolean subscribe = params.getBoolean("subscribe");
        long uid = params.getInt("userID");
        int ret = NERtcEx.getInstance().subscribeRemoteSubStreamVideo(uid, subscribe);
        promise.resolve(ret);
    }

    @ReactMethod
    public void enableAudioVolumeIndication(final ReadableMap params, Promise promise) {
        Trace.i(TAG, "enableAudioVolumeIndication " + params);
        boolean enable = params.getBoolean("enable");
        int interval = params.getInt("interval");
        boolean enableVad = params.getBoolean("enableVad");
        int ret = NERtcEx.getInstance().enableAudioVolumeIndication(enable, interval, enableVad);
        promise.resolve(ret);
    }

    private NERtcRemoteVideoStreamType getVideoStreamType(int type) {
        switch (type) {
            case 0:
                return NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh;
            case 1:
                return NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeLow;
            default:
                return NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh;
        }
    }

    /**
     * @param uid        indicates the ID of a remote user.
     * @param maxProfile indicates the resolution of the remote video. For more information, see {@link NERtcConstants.VideoProfile}.
     * @if English
     * Occurs when a remote user enables screen sharing substream channel.
     * @endif
     * @if Chinese
     * 远端用户开启屏幕共享辅流通道的回调。
     * @endif
     */
    @Override
    public void onUserSubStreamVideoStart(long uid, int maxProfile) {
        WritableMap args = Arguments.createMap();
        args.putInt("userID", (int) uid);
        args.putInt("profile", maxProfile);
        sendEvent("onUserSubStreamVideoStart", args);
    }

    /**
     * @param uid indicates the ID of a remote user.
     * @if English
     * Occurs when a remote user stops screen sharing substream channel.
     * @endif
     * @if Chinese
     * 远端用户停止屏幕共享辅流通道的回调。
     * @endif
     */
    @Override
    public void onUserSubStreamVideoStop(long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("userID", (int) uid);
        sendEvent("onUserSubStreamVideoStop", args);
    }

    /**
     * @param uid   indicates the ID of the user whose audio streams are sent.
     * @param muted specifies whether to stop sending audio streams.
     *              - true: The user stops sending audio streams.
     *              - false: The user resumes sending audio streams.
     * @if English
     * Occurs when a remote user stops/resumes sending audio streams.
     * @endif
     * @if Chinese
     * 远端用户暂停或恢复发送音频流的回调。
     * @note 该回调由远端用户调用 muteLocalAudioStream 方法开启或关闭音频发送触发。
     * @endif
     */
    @Override
    public void onUserAudioMute(long uid, boolean muted) {

    }

    /**
     * @param uid   indicates the ID of the user whose video streams are sent.
     * @param muted specifies whether to stop sending video streams.
     *              - true: The user stops sending video streams.
     *              - false: The user resumes sending video streams.
     * @if English
     * Occurs when a remote user stops/resumes sending video streams.
     * @endif
     * @if Chinese
     * 远端用户暂停或恢复发送视频流回调。
     * @note 当远端用户调用 muteLocalVideoStream 取消或者恢复发布视频流时，SDK会触发该回调向本地用户报告远程用户的发流状况。
     * @endif
     */
    @Override
    public void onUserVideoMute(long uid, boolean muted) {
    }

    /**
     * @param uid indicates the ID of a remote user whose audio streams are sent.
     * @if English
     * Occurs when the first audio frame from a remote user is received.
     * @endif
     * @if Chinese
     * 已接收到远端音频首帧回调。
     * @endif
     */
    @Override
    public void onFirstAudioDataReceived(long uid) {

    }

    /**
     * @param uid indicates the ID of a remote user whose audio streams are sent.
     * @if English
     * Occurs when the first video frame from a remote user is received.
     * <br>If the first video frame from a remote user is displayed in the view, the callback is triggered.
     * @endif
     * @if Chinese
     * 已显示首帧远端视频回调。
     * <br>第一帧远端视频显示在视图上时，触发此回调。
     * @endif
     */
    @Override
    public void onFirstVideoDataReceived(long uid) {

    }

    /**
     * @param userID indicates the ID of a remote user whose audio streams are sent.
     * @if English
     * Occurs when the first audio frame from a remote user is decoded.
     * @endif
     * @if Chinese
     * 已解码远端音频首帧的回调。
     * @endif
     */
    @Override
    public void onFirstAudioFrameDecoded(long userID) {

    }

    /**
     * @param userID indicates the ID of a remote user whose video streams are sent.
     * @param width  indicates the width of the first video frame. Unit: px.
     * @param height indicates the height of the first video frame. Unit: px.
     * @if English
     * Occurs when the first video frame from a remote user is received.
     * <br>If the engine receives the first frame of remote video streams, the callback is triggered. The callback allows the app to set the video canvas.
     * @endif
     * @if Chinese
     * 已显示首帧远端视频回调。
     * <br>引擎收到第一帧远端视频流并解码成功时，触发此调用。 App 可在此回调中设置该用户的视频画布。
     * @endif
     */
    @Override
    public void onFirstVideoFrameDecoded(long userID, int width, int height) {

    }

    /**
     * @param uid        indicates the ID of a remote user.
     * @param maxProfile sets the video parameters. For more information, see {@link NERtcConstants.VideoProfile}.
     * @if English
     * Occurs when the profile of the video streams from a remote user is updated.
     * @endif
     * @if Chinese
     * 远端用户视频编码配置已更新回调。
     * @endif
     */
    @Override
    public void onUserVideoProfileUpdate(long uid, int maxProfile) {

    }

    /**
     * @param selected indicates the selected device. For more information, see {@link NERtcConstants.AudioDevice}.
     * @if English
     * Occurs when the audio device is changed.
     * @endif
     * @if Chinese
     * 语音播放设备已改变回调。
     * @endif
     */
    @Override
    public void onAudioDeviceChanged(int selected) {
        WritableMap args = Arguments.createMap();
        args.putInt("selected", selected);
        sendEvent("onAudioDeviceChanged", args);
    }

    /**
     * @param deviceType  indicates the type of the device. For more information, see {@link NERtcConstants.AudioDeviceType}.
     * @param deviceState indicates the state of the audio device. For more information, see {@link NERtcConstants.AudioDeviceState}.
     * @if English
     * Occurs when the state of the audio device is changed.
     * @endif
     * @if Chinese
     * 音频设备状态已改变回调。
     * @endif
     */
    @Override
    public void onAudioDeviceStateChange(int deviceType, int deviceState) {
        WritableMap args = Arguments.createMap();
        args.putInt("deviceType", deviceType);
        args.putInt("deviceState", deviceState);
        sendEvent("onAudioDeviceStateChange", args);
    }

    /**
     * @param deviceState indicates the state of the audio device. For more information, see {@link NERtcConstants.VideoDeviceState}.
     * @if English
     * Occurs when the state of the video device is changed.
     * <br>The callback returns that the state of the video device changes. For example, the device is unplugged or removed. If an external camera that the device uses is unplugged, the video streaming is interrupted.
     * @endif
     * @if Chinese
     * 视频设备状态已改变回调。
     * <br>该回调提示系统视频设备状态发生改变，比如被拔出或移除。如果设备已使用外接摄像头采集，外接摄像头被拔开后，视频会中断。
     * @endif
     */
    @Override
    public void onVideoDeviceStageChange(int deviceState) {
        WritableMap args = Arguments.createMap();
        args.putInt("deviceState", deviceState);
        sendEvent("onVideoDeviceStageChange", args);
    }

    /**
     * @param newConnectionType indicates the current local network type. For more information, see {@link NERtcConstants.ConnectionType}.
     * @if English
     * Occurs when the local network type is changed.
     * <br>If the connection type of the local network changes, the callback is triggered and returns the network connection type that is being used.
     * @endif
     * @if Chinese
     * 本地网络类型已改变回调。
     * <br>本地网络连接类型发生改变时，SDK 会触发该回调，并在回调中声明当前正在使用的网络连接类型。
     * @endif
     */
    @Override
    public void onConnectionTypeChanged(int newConnectionType) {
        WritableMap args = Arguments.createMap();
        args.putInt("connectionType", newConnectionType);
        sendEvent("onConnectionTypeChanged", args);
    }

    /**
     * @if English
     * Occurs when reconnection starts.
     * <br>If a client is disconnected from the server, the SDK starts reconnecting. The callback is triggered when the reconnection starts. For more information about reconnection, see onReJoinChannel and onDisconnect.
     * @endif
     * @if Chinese
     * 重连开始回调。
     * <br>客户端和服务器断开连接时，SDK 会进行重连，重连开始时触发此回调。重连结果请参考 onReJoinChannel、onDisconnect。
     * @endif
     * @see NERtcCallbackEx#onReJoinChannel(int, long)
     * @see NERtcCallbackEx#onDisconnect(int)
     * @see NERtcCallbackEx#onReJoinChannel(int, long)
     * @see NERtcCallbackEx#onDisconnect(int)
     */
    @Override
    public void onReconnectingStart() {

    }

    /**
     * @param result    A value of 0 indicates success. Other values indicate that the user fails to rejoin the room. For more information about error codes, see {@link NERtcConstants.ErrorCode}.
     * @param channelId indicates the ID of the room that the client joins.
     * @if English
     * Occurs when a user rejoins a room.
     * <br>If a client is disconnected from the server due to poor network quality, the SDK starts reconnecting. If the client and server are reconnected, the callback is triggered.
     * @endif
     * @if Chinese
     * 重新加入房间回调。
     * <br>在弱网环境下，若客户端和服务器失去连接，SDK 会自动重连。自动重连成功后触发此回调方法。
     * @endif
     */
    @Override
    public void onReJoinChannel(int result, long channelId) {
        WritableMap args = Arguments.createMap();
        args.putInt("result", result);
        args.putInt("channelId", (int) channelId);
        sendEvent("onRejoinChannel", args);
    }

    /**
     * @param reason {@link NERtcConstants.AudioMixingError#AUDIO_MIXING_FINISH} indicates the music file finishes playing. Other status codes indicate that playing the music file fails. For more information, see {@link NERtcConstants.AudioMixingError}.
     * @if English
     * Occurs when the state of the operation on a local music file is changed.
     * <br>If you call the startAudioMixing method to play a mixing music file and the state of the playing operation changes, the callback is triggered.
     * @endif
     * @if Chinese
     * 本地用户的音乐文件播放状态改变回调。
     * <br>调用 startAudioMixing 播放混音音乐文件后，当音乐文件的播放状态发生改变时，会触发该回调。
     * @endif
     */
    @Override
    public void onAudioMixingStateChanged(int reason) {

    }

    /**
     * @param timestampMs indicates the progress of the music file playing. Unit: milliseconds.
     * @if English
     * Occurs when the timestamp of a playing music file is updated.
     * <br>If you call the startAudioMixing method to play a mixing music file and the progress of the playing operation changes, the callback is triggered.
     * @endif
     * @if Chinese
     * 本地用户的音乐文件播放进度回调。
     * <br>调用 startAudioMixing 播放混音音乐文件后，当音乐文件的播放进度改变时，会触发该回调。
     * @endif
     */
    @Override
    public void onAudioMixingTimestampUpdate(long timestampMs) {

    }

    /**
     * @param effectId indicates the ID of the specified audio effect. Each audio effect has a unique ID.
     * @if English
     * <br>Occurs when a music file finishes playing.
     * @endif
     * @if Chinese
     * <br>本地音效文件播放已结束回调。
     * @endif
     */
    @Override
    public void onAudioEffectFinished(int effectId) {

    }

    /**
     * @param volume indicates the audio volume. Value range: 0 to 100.
     * @if English
     * Occurs when the system prompts the current local audio volume.
     * <br>By default, the callback is disabled. You can enable the callback by calling the enableAudioVolumeIndication method. After the callback is enabled, if a local user speaks, the SDK triggers the callback based on the time interval specified in the enableAudioVolumeIndication method.
     * @endif
     * @if Chinese
     * 提示房间内本地用户瞬时音量的回调。
     * <br>该回调默认为关闭状态。可以通过 enableAudioVolumeIndication 方法开启。开启后，本地用户说话，SDK 会按 enableAudioVolumeIndication 方法中设置的时间间隔触发该回调。
     * @endif
     */
    @Override
    public void onLocalAudioVolumeIndication(int volume) {

    }

    /**
     * @param volume  indicates the audio volume. Value range: 0 to 100.
     * @param vadFlag voice activity detected.
     * @if English
     * Occurs when the system prompts the current local audio volume.
     * <br>By default, the callback is disabled. You can enable the callback by calling the enableAudioVolumeIndication method. After the callback is enabled, if a local user speaks, the SDK triggers the callback based on the time interval specified in the enableAudioVolumeIndication method.
     * @endif
     * @if Chinese
     * 提示房间内本地用户瞬时音量的回调。
     * <br>该回调默认为关闭状态。可以通过 {@link NERtcEx#enableAudioVolumeIndication()} 方法开启。开启后，本地用户说话，SDK 会按 {@link NERtcEx#enableAudioVolumeIndication()} 方法中设置的时间间隔触发该回调。
     * <br>如果本地用户将自己静音（调用了 {@link NERtcEx#muteLocalAudioStream()}），SDK 将音量设置为 0 后回调给应用层。
     * @endif
     * @since V4.6.10
     */
    @Override
    public void onLocalAudioVolumeIndication(int volume, boolean vadFlag) {
        WritableMap args = Arguments.createMap();
        args.putInt("volume", volume);
        args.putBoolean("vadFlag", vadFlag);
        sendEvent("onLocalAudioVolumeIndication", args);
    }

    /**
     * @param volumeArray indicates the array that contains the information about user IDs and volumes. For more information, see {@link stats.NERtcAudioVolumeInfo}.
     * @param totalVolume indicates the volume of mixed audio. Value range: 0 to 100.
     * @if English
     * Occurs when the system prompts the active speaker and the audio volume.
     * <br>By default, the callback is disabled. You can enable the callback by calling the enableAudioVolumeIndication method. After the callback is enabled, if a local user speaks, the SDK triggers the callback based on the time interval specified in the enableAudioVolumeIndication method.
     * <br>In the returned array:
     * - If a uid is contained in the array returned in the last response but not in the array returned in the current time. The remote user with the uid does not speak.
     * - If the volume is 0, the user does not speak.
     * - If the array is empty, the remote user does not speak.
     * @endif
     * @if Chinese
     * 提示房间内谁正在说话及说话者瞬时音量的回调。
     * <br>该回调默认为关闭状态。可以通过 enableAudioVolumeIndication 方法开启。开启后，无论房间内是否有人说话，SDK 都会按 enableAudioVolumeIndication 方法中设置的时间间隔触发该回调。
     * <br>在返回的数组中：
     * - 如果有 uid 出现在上次返回的数组中，但不在本次返回的数组中，则默认该 uid 对应的远端用户没有说话。
     * - 如果 volume 为 0，表示该用户没有说话。
     * - 如果数组为空，则表示此时远端没有人说话。
     * @endif
     */
    @Override
    public void onRemoteAudioVolumeIndication(NERtcAudioVolumeInfo[] volumeArray, int totalVolume) {
        WritableMap args = Arguments.createMap();
        WritableArray remoteAudioVolumeInfos = Arguments.createArray();
        args.putInt("totalVolume", totalVolume);
        for (NERtcAudioVolumeInfo volumeInfo : volumeArray) {
            WritableMap info = Arguments.createMap();
            info.putInt("uid", (int) volumeInfo.uid);
            info.putInt("volume", volumeInfo.volume);
            remoteAudioVolumeInfos.pushMap(info);
        }
        args.putArray("remoteAudioVolumeInfos", remoteAudioVolumeInfos);
        sendEvent("onRemoteAudioVolumeIndication", args);
    }

    /**
     * @param taskId    indicates the ID of the push task.
     * @param pushUrl   indicates the URL of the push task.
     * @param liveState indicates the state of live streams. For more information, see {@link NERtcConstants.LiveStreamState}.
     * @if English
     * Occurs when the state of live streams is changed.
     * @endif
     * @if Chinese
     * 推流状态已改变回调。
     * @endif
     */
    @Override
    public void onLiveStreamState(String taskId, String pushUrl, int liveState) {

    }

    /**
     * @param state  indicates the channel connection state. For more information, see {@link NERtcConstants.ConnectionState}.
     * @param reason indicates the reason why the channel state changes. For more information, see {@link NERtcConstants.ConnectionStateChangeReason}.
     * @if English
     * Occurs when the channel connection state is changed.
     * <br>The callback is triggered when the channel connection state is changed. The callback returns the current channel connection state and the reason why the state changes.
     * @endif
     * @if Chinese
     * 房间连接状态已改变回调。
     * <br>该回调在房间连接状态发生改变的时候触发，并告知用户当前的房间连接状态和引起房间状态改变的原因。
     * @endif
     */
    @Override
    public void onConnectionStateChanged(int state, int reason) {
        WritableMap args = Arguments.createMap();
        args.putInt("state", state);
        args.putInt("reason", reason);
        sendEvent("onConnectionStateChanged", args);
    }

    /**
     * @param rect indicates the new focus position.
     * @if English
     * Occurs when the camera focus position changes.
     * <br>The callback indicates that the camera focus position changes.
     * <br>The callback is triggered if a local user calls the setCameraFocusPosition method to change focus position.
     * @endif
     * @if Chinese
     * 摄像头对焦区域已改变回调。
     * <br>该回调表示相机的对焦区域发生了改变。
     * <br>该回调是由本地用户调用 setCameraFocusPosition 方法改变对焦位置触发的。
     * @endif
     */
    @Override
    public void onCameraFocusChanged(Rect rect) {

    }

    /**
     * @param rect indicates the new exposure position.
     * @if English
     * Occurs when the camera exposure position changes.
     * <br>The callback is triggered if a local user calls the setCameraExposurePosition method to change the exposure position.
     * @endif
     * @if Chinese
     * 摄像头曝光区域已改变回调。
     * <br>该回调是由本地用户调用 setCameraExposurePosition 方法改变曝光位置触发的。
     * @endif
     */
    @Override
    public void onCameraExposureChanged(Rect rect) {

    }

    /**
     * @param userID indicates the ID of the user that sends SEI.
     * @param seiMsg indicates the message that contains SEI.
     * @if English
     * Occurs when the content of remote Supplemental Enhancement Information (SEI) is received.
     * <br>After a remote client successfully sends SEI, the local client receives a message returned by the callback.
     * @endif
     * @if Chinese
     * 收到远端流的 SEI 内容回调。
     * <br>当远端成功发送 SEI 后，本端会收到此回调。
     * @endif
     * @see NERtcEx#sendSEIMsg(String)
     * @see NERtcEx#sendSEIMsg(String)
     */
    @Override
    public void onRecvSEIMsg(long userID, String seiMsg) {

    }

    /**
     * @param code     indicates the status code of the audio recording. For more information, see {@link NERtcConstants.AudioRecordingCode}.
     * @param filePath indicates the path based on which the recording file is stored.
     * @if English
     * Occurs when displaying the state of audio recording.
     * @endif
     * @if Chinese
     * 音频录制状态回调。
     * @endif
     */
    @Override
    public void onAudioRecording(int code, String filePath) {

    }

    /**
     * @param code {@link NERtcConstants.RuntimeError}
     * @if English
     * Occurs when an error occurs.
     * <br>The callback is triggered to report an error related to network or media during SDK runtime.
     * <br>In most cases, the SDK cannot fix the issue and resume running. The SDK requires the app to take action or informs the user about the issue.
     * @endif
     * @if Chinese
     * 发生错误回调。
     * <br>该回调方法表示 SDK 运行时出现了网络或媒体相关的错误。
     * <br>通常情况下，SDK上报的错误意味着 SDK 无法自动恢复，需要 App 干预或提示用户。
     * @endif
     */
    @Override
    public void onError(int code) {
        WritableMap args = Arguments.createMap();
        args.putInt("errorCode", code);
        sendEvent("onError", args);
    }

    /**
     * @param code indicates the waring code. {@link NERtcConstants.WarningCode}
     * @if English
     * Reports an error.
     * <br>The callback is triggered to report a warning related to network or media during SDK runtime.
     * <br>In most cases, the app ignores the warning message and the SDK resumes running.
     * @endif
     * @if Chinese
     * 发生警告回调。
     * <br>该回调方法表示 SDK 运行时出现了网络或媒体相关的警告。
     * <br>通常情况下，App 可以忽略 SDK 上报的警告信息，SDK 会自动恢复。
     * @endif
     */
    @Override
    public void onWarning(int code) {
        WritableMap args = Arguments.createMap();
        args.putInt("code", code);
        sendEvent("onWarning", args);
    }

    /**
     * @param state       indicates the state of the media stream relay. For more information, see {@link NERtcConstants.ChannelMediaRelayState}.
     * @param channelName the name of the destination room where the media streams are forwarded.
     * @if English
     * Occurs when the state of the media stream relay changes.
     * @endif
     * @if Chinese
     * 跨房间媒体流转发状态发生改变回调。
     * @endif
     */
    @Override
    public void onMediaRelayStatesChange(int state, String channelName) {

    }

    /**
     * @param event       The media stream relay event. For more information, see {@link NERtcConstants.ChannelMediaRelayEvent}.
     * @param code        indicates an error code. For more information, see {@link NERtcConstants.ChannelMediaRelayState}.
     * @param channelName the name of the destination room where the media streams are forwarded.
     * @if English
     * Reports events during the media stream relay.
     * @endif
     * @if Chinese
     * 跨房间媒体流转发状态发生改变回调。
     * @endif
     */
    @Override
    public void onMediaRelayReceiveEvent(int event, int code, String channelName) {

    }

    /**
     * @param isFallback indicates that the locally published stream falls back to the audio-only mode or switches back to audio and video stream.
     *                   - true: The locally published stream falls back to the audio-only mode due to poor upstream network conditions.
     *                   - false: The audio stream switches back to the audio and video stream after the upstream network conditions improve.
     * @param streamType indicates the type of the video stream, such as substream and substream.
     * @if English
     * Occurs when the published local media stream falls back to an audio-only stream due to poor network conditions or switches back to audio and video stream after the network conditions improve.
     * If you call setLocalPublishFallbackOption and set the option to AUDIO_ONLY, this callback is triggered when the locally published stream falls back to the audio-only mode due to poor upstream network conditions, or when the audio stream switches back to the audio and video stream after the upstream network conditions improve.
     * @endif
     * @if Chinese
     * 本地发布流已回退为音频流、或已恢复为音视频流回调。
     * 如果您调用了设置本地推流回退选项 setLocalPublishFallbackOption 接口，并将 option 设置为 AUDIO_ONLY 后，当上行网络环境不理想、本地发布的媒体流回退为音频流时，或当上行网络改善、媒体流恢复为音视频流时，会触发该回调。
     * @endif
     * @since V4.3.0
     * @since V4.3.0
     */
    @Override
    public void onLocalPublishFallbackToAudioOnly(boolean isFallback, NERtcVideoStreamType streamType) {

    }

    /**
     * @param uid        indicates the ID of a remote user.
     * @param isFallback indicates that the subscribed remote media stream falls back to the audio-only mode or switches back to the audio and video stream.
     *                   - true: The subscribed remote media stream falls back to the audio-only mode due to poor downstream network conditions.
     *                   - false: The subscribed remote media stream switches back to the audio and video stream after the downstream network conditions improve.
     * @param streamType indicates the type of the video stream, such as mainstream and substream.
     * @if English
     * Occurs when the subscribed remote media stream falls back to audio-only stream due to poor network conditions or switches back to video stream after the network conditions improve.
     * <br>If you call setRemoteSubscribeFallbackOption and set the option to AUDIO_ONLY, this callback is triggered when the subscribed remote media stream falls back to the audio-only mode due to poor downstream network conditions, or when the subscribed remote media stream switches back to the audio and video stream after the downstream network conditions improve.
     * @endif
     * @if Chinese
     * 订阅的远端流已回退为音频流、或已恢复为音视频流回调。
     * <br>如果你调用了设置远端订阅流回退选项 setRemoteSubscribeFallbackOption 接口并将 option 设置 AUDIO_ONLY 后，当下行网络环境不理想、仅接收远端音频流时，或当下行网络改善、恢复订阅音视频流时，会触发该回调。
     * @endif
     * @since V4.3.0
     * @since V4.3.0
     */
    @Override
    public void onRemoteSubscribeFallbackToAudioOnly(long uid, boolean isFallback, NERtcVideoStreamType streamType) {

    }

    /**
     * @param quality The last mile network quality.
     *                - QUALITY_UNKNOWN(0)：Unknown network quality.
     *                - QUALITY_EXCELLENT(1)：Excellent network quality.
     *                - QUALITY_GOOD(2)：Good network quality is close to the excellent level but has the bitrate is lower an excellent network.
     *                - QUALITY_POOR(3)：Poor network does not affect communication.
     *                - QUALITY_BAD(4)：Users can communicate with each other without smoothness.
     *                - QUALITY_VBAD(5)：The network quality is very poor. Basically users are unable to communicate.
     *                - QUALITY_DOWN(6)：Users are unable to communicate with each other.
     * @if English
     * Reports the last mile network quality of the local user once every two seconds before the user joins the channel.
     * <br> After the application calls the startLastmileProbeTest method, this callback reports once every five seconds the uplink and downlink last mile network conditions of the local user before the user joins the channel.
     * @endif
     * @if Chinese
     * 通话前网络上下行 last mile 质量状态回调。
     * <br>该回调描述本地用户在加入房间前的 last mile 网络探测的结果，以打分形式描述上下行网络质量的主观体验，您可以通过该回调预估本地用户在音视频通话中的网络体验。
     * <br>在调用 startLastmileProbeTest 之后，SDK 会在约 5 秒内返回该回调。
     * @endif
     * @since V4.5.0
     * @since V4.5.0
     */
    @Override
    public void onLastmileQuality(int quality) {

    }

    /**
     * @param result The uplink and downlink last-mile network probe test result. For more information, see {@link LastmileProbeResult}.
     * @if English
     * Reports the last-mile network probe result.
     * <br>This callback describes a detailed last-mile network detection report of a local user before joining a channel. The report provides objective data about the upstream and downstream network quality, including network jitter and packet loss rate.  You can use the report to objectively predict the network status of local users during an audio and video call.
     * <br>The SDK triggers this callback within 30 seconds after the app calls the startLastmileProbeTest method.
     * @endif
     * @if Chinese
     * 通话前网络上下行 Last mile 质量探测报告回调。
     * <br>该回调描述本地用户在加入房间前的 last mile 网络探测详细报告，报告中通过客观数据反馈上下行网络质量，包括网络抖动、丢包率等数据。您可以通过该回调客观预测本地用户在音视频通话中的网络状态。
     * <br>在调用 startLastmileProbeTest 之后，SDK 会在约 30 秒内返回该回调。
     * @endif
     * @since V4.5.0
     * @since V4.5.0
     */
    @Override
    public void onLastmileProbeResult(LastmileProbeResult result) {

    }

    /**
     * @param isAudioBannedByServer
     * @param isVideoBannedByServer - true: banned
     *                              - false unbanned
     * @if English
     * Audio/Video Callback when banned by server.
     * @endif
     * @if Chinese
     * 服务端禁言音视频权限变化回调。
     * @endif
     * @since v4.6.0
     * @since v4.6.0
     */
    @Override
    public void onMediaRightChange(boolean isAudioBannedByServer, boolean isVideoBannedByServer) {

    }

    /**
     * @param enabled Whether the virtual background is successfully enabled:
     *                - true: The virtual background is successfully enabled.
     *                - false: The virtual background is not successfully enabled.
     * @param reason  The reason why the virtual background is not successfully enabled or the message that confirms success:
     *                {@link NERtcConstants.NERtcVirtualBackgroundSourceStateReason}
     * @if English
     * Reports whether the virtual background is successfully enabled.
     * @endif
     * @if Chinese
     * 通知虚拟背景是否成功开启的回调。
     * <br> 调用 {@link NERtcEx.enableVirtualBackground} 接口启用虚拟背景功能后，SDK 会触发此回调。
     * @note 如果自定义虚拟背景是 PNG 或 JPG 格式的图片，SDK 会在读取图片后才会触发此回调，因此可能存在一定延时。
     * @endif
     * @since V4.6.10
     */
    @Override
    public void onVirtualBackgroundSourceEnabled(boolean enabled, int reason) {

    }

    /**
     * @param uid 远端用户 ID。
     * @if Chinese
     * 远端用户开启音频辅流回调。
     * @endif
     * @since V4.6.10
     */
    @Override
    public void onUserSubStreamAudioStart(long uid) {

    }

    /**
     * @param uid 远端用户 ID。
     * @if Chinese
     * 远端用户停用音频辅流回调。
     * @endif
     * @since V4.6.10
     */
    @Override
    public void onUserSubStreamAudioStop(long uid) {

    }

    /**
     * @param uid   用户 ID，提示是哪个用户的音频辅流。
     * @param muted 是否停止发送音频辅流。
     *              - true：该用户已暂停发送音频辅流。
     *              - false：该用户已恢复发送音频辅流。
     * @if Chinese
     * 远端用户暂停或恢复发送音频辅流的回调。
     * @endif
     * @see NERtcEx#enableLocalSubStreamAudio(boolean)
     * @since V4.6.10
     */
    @Override
    public void onUserSubStreamAudioMute(long uid, boolean muted) {

    }

    /**
     * @param videoStreamType 对应的视频流类型，即主流或辅流。详细信息请参考 {@link video.NERtcVideoStreamType}。
     * @param state           水印状态。详细信息请参考 {@link NERtcConstants.NERtcLocalVideoWatermarkState}。
     * @if Chinese
     * 本地视频水印生效结果回调。
     * @endif
     * @see NERtcEx#setLocalVideoWatermarkConfigs(NERtcVideoStreamType, NERtcVideoWatermarkConfig)
     * @since V4.6.10
     */
    @Override
    public void onLocalVideoWatermarkState(NERtcVideoStreamType videoStreamType, int state) {

    }

    /**
     * @param result    A value of 0 indicates that a user joins a room. Otherwise, the user fails to join a room. For more information, see {@link NERtcConstants.ErrorCode}.
     * @param channelId The ID of the room that the client joins.
     * @param elapsed   The time consumed from calling the joinChannel method to the time when the event occurs. Unit: milliseconds.
     * @param uid       User ID.
     * @if English
     * Occurs when a user joins a room. The callback indicates that the client has already signed in.
     * @endif
     * @if Chinese
     * 加入房间回调，表示客户端已经登入服务器。
     * @endif
     */
    @Override
    public void onJoinChannel(int result, long channelId, long elapsed, long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("result", result);
        args.putInt("channelId", (int) channelId);
        args.putInt("elapsed", (int) elapsed);
        args.putInt("uid", (int) uid);
        sendEvent("onJoinChannel", args);
    }

    /**
     * @param result 0 indicates success. Other values indicate failure. For more information about error codes, see {@link NERtcConstants.ErrorCode}.
     *               When a user switches rooms, the code parameter takes NERtcConstants.ErrorCode#LEAVE_CHANNEL_FOR_SWITCH.
     * @if English
     * Occurs when a user leaves a room.
     * <br>After an app calls the leaveChannel method, SDK prompts whether the app successfully exits the room.
     * @endif
     * @if Chinese
     * 退出房间回调。
     * <br>App 调用 leaveChannel 方法后，SDK 提示 App 退出房间是否成功。
     * @endif
     */
    @Override
    public void onLeaveChannel(int result) {
        WritableMap args = Arguments.createMap();
        args.putInt("result", result);
        sendEvent("onLeaveChannel", args);
    }

    /**
     * @param uid indicates the ID of the user that joins the room.
     * @if English
     * Occurs when a remote user joins the current room.
     * <br>The callback function prompts that a remote user joins the room and returns the ID of the user that joins the room. If the user ID already exists, the remote user also receives a message that the user already joins the room, which is returned by the callback function.
     * <br>The callback function will be triggered in the following cases:
     * - A remote user joins the room by calling the joinChannel method.
     * - A remote user rejoins the room after the client is disconnected.
     * @endif
     * @if Chinese
     * 远端用户（通信场景）/主播（直播场景）加入当前频道回调。
     * - 通信场景下，该回调提示有远端用户加入了频道，并返回新加入用户的 ID；如果加入之前，已经有其他用户在频道中了，新加入的用户也会收到这些已有用户加入频道的回调
     * - 直播场景下，该回调提示有主播加入了频道，并返回该主播的用户 ID。如果在加入之前，已经有主播在频道中了，新加入的用户也会收到已有主播加入频道的回调。
     * <p>
     * 该回调在如下情况下会被触发：
     * - 远端用户调用 joinChannel 方法加入房间。
     * - 远端用户网络中断后重新加入房间。
     * @note 直播场景下：
     * - 主播间能相互收到新主播加入频道的回调，并能获得该主播的用户 ID。
     * - 观众也能收到新主播加入频道的回调，并能获得该主播的用户 ID。
     * - 当 Web 端加入直播频道时，只要 Web 端有推流，SDK 会默认该 Web 端为主播，并触发该回调。
     * @endif
     */
    @Override
    public void onUserJoined(long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        sendEvent("onUserJoin", args);
    }

    /**
     * @param uid    indicates the ID of the user that leaves the room.
     * @param reason The reason why user leaves the channel.
     * @if English
     * Occurs when a remote user leaves a room.
     * <br>Prompts that a remote user leaves the room or becomes disconnected.
     * <br>A user leaves a room due to the following reasons: the user exit the room or connections time out.
     * - If a user exit the room, the user will receive a message that the user leaves the room.
     * - If connections time out, the user does not receive any data packets for a period of 40 to 50 seconds, then the user becomes disconnected.
     * @endif
     * @if Chinese
     * 远端用户离开当前房间回调。
     * <br>提示有远端用户离开了房间（或掉线）。
     * <br>用户离开房间有两个原因，即正常离开和超时掉线：
     * - 正常离开的时候，远端用户会收到消息提示，判断用户离开房间。
     * - 超时掉线的依据是，在一定时间内（40~50s），用户没有收到对方的任何数据包，则判定为对方掉线。
     * @endif
     */
    @Override
    public void onUserLeave(long uid, int reason) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        args.putInt("reason", reason);
        sendEvent("onUserLeave", args);
    }

    /**
     * @param uid indicates the ID of the remote user.
     * @if English
     * Occurs when a remote user enables audio.
     * @endif
     * @if Chinese
     * 远端用户开启音频回调。
     * @note 该回调由远端用户调用 enableLocalAudio 方法开启音频采集和发送触发。
     * @endif
     */
    @Override
    public void onUserAudioStart(long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        sendEvent("onUserAudioStart", args);
    }

    /**
     * @param uid indicates the ID of the remote user.
     * @if English
     * Occurs when a remote user disables audio.
     * @endif
     * @if Chinese
     * 远端用户停用音频回调。
     * @note 该回调由远端用户调用 enableLocalAudio 方法关闭音频采集和发送触发。
     * @endif
     */
    @Override
    public void onUserAudioStop(long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        sendEvent("onUserAudioStop", args);
    }

    /**
     * @param uid        indicates the ID of the user that sends the video streams.
     * @param maxProfile sets the video parameters. For more information, see {@link NERtcConstants.VideoProfile}.
     * @if English
     * Occurs when a remote user enables video.
     * @endif
     * @if Chinese
     * 远端用户开启视频回调。
     * <br> 启用后，用户可以进行视频通话或直播。
     * @endif
     */
    @Override
    public void onUserVideoStart(long uid, int maxProfile) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        args.putInt("maxProfile", maxProfile);
        sendEvent("onUserVideoStart", args);
    }

    /**
     * @param uid indicates the ID of a remote user.
     * @if English
     * Occurs when a remote user disables video.
     * @endif
     * @if Chinese
     * 远端用户停用视频回调。
     * <br> 关闭后，用户只能进行语音通话或者直播。
     * @endif
     */
    @Override
    public void onUserVideoStop(long uid) {
        WritableMap args = Arguments.createMap();
        args.putInt("uid", (int) uid);
        sendEvent("onUserVideoStop", args);
    }

    /**
     * @param reason indicates the reason for network disconnection. For more information, see {@link NERtcConstants.ErrorCode}.
     * @if English
     * Indicates that connection breaks down and the SDK fails to connect to the server three consecutive times.
     * @note - The callback function is triggered if the SDK fails to connect to the server three consecutive times after the joinChannel is successfully called.
     * - If the SDK fails to connect to the server three consecutive times, the SDK stops retries.
     * @endif
     * @if Chinese
     * 网络连接中断，且 SDK 连续 3 次重连服务器失败。
     * <br><b>注意</b>：
     * - SDK 在调用 joinChannel 加入房间成功后，如果和服务器失去连接且连续 3 次重连失败，就会触发该回调。
     * - 如果 SDK 在断开连接后，连续 3 次重连失败，SDK 会停止尝试重连。
     * @endif
     */
    @Override
    public void onDisconnect(int reason) {
        WritableMap args = Arguments.createMap();
        args.putInt("result", reason);
        sendEvent("onDisconnect", args);
    }

    /**
     * @param oldRole indicates the role before the change. For more information, see {@link NERtcConstants.UserRole}.
     * @param newRole indicates the role after the change. For more information, see {@link NERtcConstants.UserRole}.
     * @if English
     * Occurs when a user changes the role in live streaming.
     * <br>After the user joins a room, the user can call the setClientRole method to change roles. Then, the callback is triggered. For example, switching from host to audience, or from audience to host.
     * <br>@note
     * <br>In live streaming, if you join a room and successfully call this method to change roles, the following callback functions are triggered.
     * - If the role is changed from host to audience, the onClientRoleChange callback is locally triggered, and the onUserLeave callback is remotely triggered.
     * - If the role is changed from audience to host, the onClientRoleChange callback is locally triggered, and the onUserJoined callback is remotely triggered.
     * @endif
     * @if Chinese
     * 直播场景下用户角色已切换回调。
     * <br>用户加入房间后，通过 {@link NERtcEx#setClientRole()} 切换用户角色后会触发此回调。例如从主播切换为观众、从观众切换为主播。
     * <br><b>注意</b>：
     * <br>直播场景下，如果您在加入房间后调用该方法切换用户角色，调用成功后，会触发以下回调：
     * - 主播切观众，本端触发 onClientRoleChange 回调，远端触发 {@link NERtcCallback#onUserLeave()} 回调。
     * - 观众切主播，本端触发 onClientRoleChange 回调，远端触发 {@link NERtcCallback#onUserJoined()} 回调。
     * @endif
     */
    @Override
    public void onClientRoleChange(int oldRole, int newRole) {

    }

    // Stats Observer

    /**
     * @param stats {@link stats.NERtcStats}
     * @if English
     * Current audio statistics callback.
     * <br>The SDK triggers this callback every two seconds to report audio statistics.
     * @endif
     * @if Chinese
     * 当前通话统计回调。
     * <br>SDK 定期向 App 报告当前通话的统计信息，每 2 秒触发一次。
     * @endif
     */
    @Override
    public void onRtcStats(NERtcStats stats) {
        WritableMap args = Arguments.createMap();
        args.putInt("txBytes", (int) stats.txBytes);
        args.putInt("rxBytes", (int) stats.rxBytes);
        args.putInt("cpuAppUsage", stats.cpuAppUsage);
        args.putInt("cpuTotalUsage", stats.cpuTotalUsage);
        args.putInt("memoryAppUsageRatio", stats.memoryAppUsageRatio);
        args.putInt("memoryTotalUsageRatio", stats.memoryTotalUsageRatio);
        args.putInt("memoryAppUsageInKBytes", (int) stats.memoryAppUsageInKBytes);
        args.putInt("totalDuration", (int) stats.totalDuration);
        args.putInt("txAudioBytes", (int) stats.txAudioBytes);
        args.putInt("txVideoBytes", (int) stats.txVideoBytes);
        args.putInt("rxAudioBytes", (int) stats.rxAudioBytes);
        args.putInt("rxVideoBytes", (int) stats.rxVideoBytes);
        args.putInt("rxAudioKBitRate", stats.rxAudioKBitRate);
        args.putInt("rxVideoKBitRate", stats.rxVideoKBitRate);
        args.putInt("txAudioKBitRate", stats.txAudioKBitRate);
        args.putInt("txVideoKBitRate", stats.txVideoKBitRate);
        args.putInt("upRtt", (int) stats.upRtt);
        args.putInt("downRtt", (int) stats.downRtt);
        args.putInt("txAudioPacketLossRate", stats.txAudioPacketLossRate);
        args.putInt("txVideoPacketLossRate", stats.txVideoPacketLossRate);
        args.putInt("txAudioPacketLossSum", stats.txAudioPacketLossSum);
        args.putInt("txVideoPacketLossSum", stats.txVideoPacketLossSum);
        args.putInt("txAudioJitter", stats.txAudioJitter);
        args.putInt("txVideoJitter", stats.txVideoJitter);
        args.putInt("rxAudioPacketLossRate", stats.rxAudioPacketLossRate);
        args.putInt("rxVideoPacketLossRate", stats.rxVideoPacketLossRate);
        args.putInt("rxAudioPacketLossSum", (int) stats.rxAudioPacketLossSum);
        args.putInt("rxVideoPacketLossSum", stats.rxVideoPacketLossSum);
        args.putInt("rxAudioJitter", stats.rxAudioJitter);
        args.putInt("rxVideoJitter", stats.rxVideoJitter);
        sendEvent("onRtcStats", args);
    }

    /**
     * @param stats {@link stats.NERtcAudioSendStats}
     * @if English
     * Local audio statistics callback.
     * @endif
     * @if Chinese
     * 本地音频流统计信息回调。
     * @endif
     */
    @Override
    public void onLocalAudioStats(NERtcAudioSendStats stats) {
        WritableMap args = Arguments.createMap();
        WritableArray audioLayers = Arguments.createArray();
        for (NERtcAudioLayerSendStats layer : stats.audioLayers) {
            WritableMap stat = Arguments.createMap();
            stat.putInt("streamType", getAudioStreamType(layer.streamType));
            stat.putInt("sentBitrate", layer.kbps);
            stat.putInt("lossRate", layer.lossRate);
            stat.putInt("rtt", (int) layer.rtt);
            stat.putInt("volume", layer.volume);
            stat.putInt("numChannels", layer.numChannels);
            stat.putInt("sentSampleRate", layer.sentSampleRate);
            stat.putInt("capVolume", layer.capVolume);
            audioLayers.pushMap(stat);
        }
        args.putArray("audioLayers", audioLayers);
        sendEvent("onLocalAudioStats", args);
    }

    /**
     * @param statsArray {@link stats.NERtcAudioRecvStats}
     * @if English
     * Statistics callback of remote audio streams array.
     * @endif
     * @if Chinese
     * 通话中远端音频流的统计信息回调数组。
     * @endif
     */
    @Override
    public void onRemoteAudioStats(NERtcAudioRecvStats[] statsArray) {
        WritableMap args = Arguments.createMap();
        WritableArray audioRecvStats = Arguments.createArray();
        for (NERtcAudioRecvStats stats : statsArray) {
            WritableMap userStat = Arguments.createMap();
            WritableArray audioLayers = Arguments.createArray();
            userStat.putInt("uid", (int) stats.uid);
            for (NERtcAudioLayerRecvStats layer : stats.layers) {
                WritableMap stat = Arguments.createMap();
                stat.putInt("streamType", getAudioStreamType(layer.streamType));
                stat.putInt("sendBitrate", layer.kbps);
                stat.putInt("lossRate", layer.lossRate);
                stat.putInt("volume", layer.volume);
                stat.putInt("totalFrozenTime", (int) layer.totalFrozenTime);
                stat.putInt("frozenRate", layer.frozenRate);
                audioLayers.pushMap(stat);
            }
            userStat.putArray("audioLayers", audioLayers);
            audioRecvStats.pushMap(userStat);
        }
        args.putArray("audioRecvStats", audioRecvStats);
        sendEvent("onRemoteAudioStats", args);
    }

    private int getAudioStreamType(NERtcAudioStreamType type) {
        switch (type) {
            case kNERtcAudioStreamTypeMain:
                return 0;
            case kNERtcAudioStreamTypeSub:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * @param stats {@link stats.NERtcVideoSendStats}
     * @if English
     * Local video stream statistics callback.
     * @endif
     * @if Chinese
     * 本地视频流统计信息回调。
     * @endif
     */
    @Override
    public void onLocalVideoStats(NERtcVideoSendStats stats) {
        WritableMap args = Arguments.createMap();
        WritableArray videoLayers = Arguments.createArray();
        for (NERtcVideoLayerSendStats layer : stats.videoLayers) {
            WritableMap stat = Arguments.createMap();
            stat.putInt("layerType", layer.layerType);
            stat.putInt("capWidth", layer.capWidth);
            stat.putInt("capHeight", layer.capHeight);
            stat.putInt("width", layer.width);
            stat.putInt("height", layer.height);
            stat.putInt("sendBitrate", layer.sendBitrate);
            stat.putInt("encoderOutputFrameRate", layer.encoderOutputFrameRate);
            stat.putInt("captureFrameRate", layer.captureFrameRate);
            stat.putInt("targetBitrate", layer.targetBitrate);
            stat.putInt("encoderBitrate", layer.encoderBitrate);
            stat.putInt("sentFrameRate", layer.sentFrameRate);
            stat.putInt("renderFrameRate", layer.renderFrameRate);
            stat.putString("encoderName", layer.encoderName);
            videoLayers.pushMap(stat);
        }
        args.putArray("videoLayers", videoLayers);
        sendEvent("onLocalVideoStats", args);
    }

    /**
     * @param statsArray {@link stats.NERtcVideoRecvStats}
     * @if English
     * Statistics callback of remote video streams array.
     * @endif
     * @if Chinese
     * 通话中远端视频流的统计信息回调数组。
     * @endif
     */
    @Override
    public void onRemoteVideoStats(NERtcVideoRecvStats[] statsArray) {
        WritableMap args = Arguments.createMap();
        WritableArray videoRecvStats = Arguments.createArray();
        for (NERtcVideoRecvStats stats : statsArray) {
            WritableMap userStat = Arguments.createMap();
            WritableArray videoLayers = Arguments.createArray();
            userStat.putInt("uid", (int) stats.uid);
            for (NERtcVideoLayerRecvStats layer : stats.layers) {
                WritableMap stat = Arguments.createMap();
                stat.putInt("layerType", layer.layerType);
                stat.putInt("width", layer.width);
                stat.putInt("height", layer.height);
                stat.putInt("receivedBitrate", layer.receivedBitrate);
                stat.putInt("fps", layer.fps);
                stat.putInt("packetLossRate", layer.packetLossRate);
                stat.putInt("decoderOutputFrameRate", layer.decoderOutputFrameRate);
                stat.putInt("rendererOutputFrameRate", layer.rendererOutputFrameRate);
                stat.putInt("totalFrozenTime", (int) layer.totalFrozenTime);
                stat.putInt("frozenRate", layer.frozenRate);
                videoLayers.pushMap(stat);
            }
            userStat.putArray("videoLayers", videoLayers);
            videoRecvStats.pushMap(userStat);
        }
        args.putArray("videoRecvStats", videoRecvStats);
        sendEvent("onRemoteVideoStats", args);
    }

    /**
     * @param statsArray {@link stats.NERtcNetworkQualityInfo} {@link NERtcConstants.NetworkStatus}
     * @if English
     * Internet quality callback of all users during the call.
     * @endif
     * @if Chinese
     * 通话中所有用户的网络状态回调。
     * @endif
     */
    @Override
    public void onNetworkQuality(NERtcNetworkQualityInfo[] statsArray) {
        WritableMap args = Arguments.createMap();
        WritableArray netWorkQualityInfos = Arguments.createArray();
        for (NERtcNetworkQualityInfo stats : statsArray) {
            WritableMap stat = Arguments.createMap();
            stat.putInt("userId", (int) stats.userId);
            stat.putInt("upStatus", stats.upStatus);
            stat.putInt("downStatus", stats.downStatus);
            netWorkQualityInfos.pushMap(stat);
        }
        args.putArray("netWorkQualityInfos", netWorkQualityInfos);
        sendEvent("onNetworkQuality", args);
    }

    //ActivityEventListener
    /**
     * Called when host (activity/service) receives an {@link Activity#onActivityResult} call.
     *
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Trace.i(TAG, "onActivityResult " + requestCode);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            int ret = NERtcConstants.ErrorCode.OK;
            if (resultCode == RESULT_OK) {
                ret = startScreenCapture(data);
            } else {
                Trace.w(TAG, "onActivityResult startScreenCapture permission denied");
                ret = NERtcConstants.ErrorCode.RESERVE_ERROR_NO_PERMISSION;
            }
            WritableMap args = Arguments.createMap();
            args.putInt("result", ret);
            sendEvent("onStartScreenCapture", args);
        }
    }

    /**
     * Called when a new intent is passed to the activity
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private int startScreenCapture(Intent mediaProjectionPermissionResultData) {
        final MediaProjection.Callback mediaProjectionCallback = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                super.onStop();
            }
        };

        if (screenConfig == null) {
            Trace.w(TAG, "startScreenCapture screenConfig is null!");
            screenConfig = new NERtcScreenConfig();
        }

        // 开启屏幕共享
        return NERtcEx.getInstance().startScreenCapture(screenConfig,
            mediaProjectionPermissionResultData, // 屏幕录制请求返回的Intent
            mediaProjectionCallback);
    }

    private int requestScreenCapture() {
        int ret = NERtcConstants.ErrorCode.OK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reactContext.startActivityForResult(createScreenCaptureIntent(reactContext), REQUEST_CODE_SCREEN_CAPTURE, null);
        } else {
            Trace.w(TAG, "requestScreenCapture sdk version " + Build.VERSION.SDK_INT + "lower than L");
            ret = NERtcConstants.ErrorCode.ENGINE_ERROR_NOT_SUPPORTED;
        }

        return ret;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Intent createScreenCaptureIntent(Context context) {
        MediaProjectionManager manager =
            (MediaProjectionManager) context.getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
        return manager.createScreenCaptureIntent();
    }
}
