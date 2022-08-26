package com.reactnativenertc;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

public class RCTNERtcVideoViewManager extends SimpleViewManager<RCTNERtcVideoView> {
    @NonNull
    @Override
    public String getName() {
        return "RCTNERtcVideoView";
    }

    @NonNull
    @Override
    protected RCTNERtcVideoView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RCTNERtcVideoView(reactContext);
    }
}