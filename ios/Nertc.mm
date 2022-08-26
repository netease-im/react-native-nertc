#import "Nertc.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNNertcSpec.h"
#endif

#import <NERtcSDK/NERtcSDK.h>
#import <React/RCTConvert.h>

@interface Nertc ()<NERtcEngineDelegateEx, NERtcEngineMediaStatsObserver>

@property (nonatomic, assign) BOOL hasListeners;

@end

@implementation Nertc
RCT_EXPORT_MODULE()

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeNertcSpecJSI>(params);
}
#endif

- (void)startObserving {
    self.hasListeners = YES;
}

- (void)stopObserving {
    self.hasListeners = NO;
}

#pragma mark - NERtcSDK JS Bridge

RCT_REMAP_METHOD(setupEngineWithContext,
                 setupEngineWithContextWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSString *appKey = [RCTConvert NSString:params[@"appKey"]];
    NSString *logDir = [RCTConvert NSString:params[@"logDir"]];
    
    NSNumber *logLevelObj = [RCTConvert NSNumber:params[@"logLevel"]];
    NERtcLogLevel logLevel = logLevelObj ? (NERtcLogLevel)[logLevelObj integerValue] : kNERtcLogLevelWarning;
    
    NERtcLogSetting *logSetting = [[NERtcLogSetting alloc] init];
    logSetting.logDir = (logDir && logDir.length > 0) ? logDir : nil;
    logSetting.logLevel = logLevel;
    
    NERtcEngineContext *context = [[NERtcEngineContext alloc] init];
    context.appKey = appKey;
    context.logSetting = logSetting;
    context.engineDelegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        int result = [[NERtcEngine sharedEngine] setupEngineWithContext:context];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(destroyEngine,
                 destroyEngineWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        int result = [NERtcEngine destroyEngine];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(joinChannel,
                 joinChannelWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSString *token = [RCTConvert NSString:params[@"token"]];
    NSString *channelName = [RCTConvert NSString:params[@"channelName"]];
    
    NSNumber *myUidObj = [RCTConvert NSNumber:params[@"myUid"]];
    uint64_t myUid = myUidObj ? [myUidObj unsignedLongLongValue] : 0;
    
    int result = kNERtcNoError;
    result = [[NERtcEngine sharedEngine] joinChannelWithToken:token channelName:channelName myUid:myUid completion:^(NSError * _Nullable error, uint64_t channelId, uint64_t elapesd, uint64_t uid) {
        NSMutableDictionary *resultInfo = [NSMutableDictionary dictionary];
        [resultInfo setObject:error.description ?: @"" forKey:@"error"];
        [resultInfo setObject:@(channelId) forKey:@"channelId"];
        [resultInfo setObject:@(elapesd) forKey:@"elapesd"];
        [resultInfo setObject:@(uid) forKey:@"uid"];
        
        if (self.hasListeners) {
            [self sendEventWithName:@"onJoinChannel" body:resultInfo];
        }
    }];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(leaveChannel,
                 leaveChannelWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] leaveChannel];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(setupLocalVideoCanvas,
                 setupLocalVideoCanvasWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *reactTagObj = [RCTConvert NSNumber:params[@"reactTag"]];
    NSNumber *renderModeObj = [RCTConvert NSNumber:params[@"renderMode"]];
    NSNumber *mirrorModeObj = [RCTConvert NSNumber:params[@"mirrorMode"]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NERtcVideoCanvas *canvas = [[NERtcVideoCanvas alloc] init];
        if (reactTagObj) {
            UIView *container = [self.bridge.uiManager viewForReactTag:reactTagObj];
            canvas.container = container;
        } else {
            canvas.container = nil;
        }
        if (renderModeObj) {
            canvas.renderMode = (NERtcVideoRenderScaleMode)[renderModeObj unsignedIntegerValue];
        }
        if (mirrorModeObj) {
            canvas.mirrorMode = (NERtcVideoMirrorMode)[mirrorModeObj unsignedIntegerValue];
        }
        
        int result = [[NERtcEngine sharedEngine] setupLocalVideoCanvas:canvas];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(setupRemoteVideoCanvas,
                 setupRemoteVideoCanvasWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *userIDObj = [RCTConvert NSNumber:params[@"userID"]];
    if (!userIDObj) {
        resolver(@(kNERtcErrInvalidParam));
        
        return;
    }
    
    NSNumber *reactTagObj = [RCTConvert NSNumber:params[@"reactTag"]];
    NSNumber *renderModeObj = [RCTConvert NSNumber:params[@"renderMode"]];
    NSNumber *mirrorModeObj = [RCTConvert NSNumber:params[@"mirrorMode"]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NERtcVideoCanvas *canvas = [[NERtcVideoCanvas alloc] init];
        if (reactTagObj) {
            UIView *container = [self.bridge.uiManager viewForReactTag:reactTagObj];
            canvas.container = container;
        } else {
            canvas.container = nil;
        }
        if (renderModeObj) {
            canvas.renderMode = (NERtcVideoRenderScaleMode)[renderModeObj unsignedIntegerValue];
        }
        if (mirrorModeObj) {
            canvas.mirrorMode = (NERtcVideoMirrorMode)[mirrorModeObj unsignedIntegerValue];
        }
        
        uint64_t userID = [userIDObj unsignedLongLongValue];
        
        int result = [[NERtcEngine sharedEngine] setupRemoteVideoCanvas:canvas forUserID:userID];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(setupLocalSubStreamVideoCanvas,
                 setupLocalSubStreamVideoCanvasWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *reactTagObj = [RCTConvert NSNumber:params[@"reactTag"]];
    NSNumber *renderModeObj = [RCTConvert NSNumber:params[@"renderMode"]];
    NSNumber *mirrorModeObj = [RCTConvert NSNumber:params[@"mirrorMode"]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NERtcVideoCanvas *canvas = [[NERtcVideoCanvas alloc] init];
        if (reactTagObj) {
            UIView *container = [self.bridge.uiManager viewForReactTag:reactTagObj];
            canvas.container = container;
        } else {
            canvas.container = nil;
        }
        if (renderModeObj) {
            canvas.renderMode = (NERtcVideoRenderScaleMode)[renderModeObj unsignedIntegerValue];
        }
        if (mirrorModeObj) {
            canvas.mirrorMode = (NERtcVideoMirrorMode)[mirrorModeObj unsignedIntegerValue];
        }
        
        int result = [[NERtcEngine sharedEngine] setupLocalSubStreamVideoCanvas:canvas];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(setupRemoteSubStreamVideoCanvas,
                 setupRemoteSubStreamVideoCanvasWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *userIDObj = [RCTConvert NSNumber:params[@"userID"]];
    if (!userIDObj) {
        resolver(@(kNERtcErrInvalidParam));
        
        return;
    }
    
    NSNumber *reactTagObj = [RCTConvert NSNumber:params[@"reactTag"]];
    NSNumber *renderModeObj = [RCTConvert NSNumber:params[@"renderMode"]];
    NSNumber *mirrorModeObj = [RCTConvert NSNumber:params[@"mirrorMode"]];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NERtcVideoCanvas *canvas = [[NERtcVideoCanvas alloc] init];
        if (reactTagObj) {
            UIView *container = [self.bridge.uiManager viewForReactTag:reactTagObj];
            canvas.container = container;
        } else {
            canvas.container = nil;
        }
        if (renderModeObj) {
            canvas.renderMode = (NERtcVideoRenderScaleMode)[renderModeObj unsignedIntegerValue];
        }
        if (mirrorModeObj) {
            canvas.mirrorMode = (NERtcVideoMirrorMode)[mirrorModeObj unsignedIntegerValue];
        }
        
        uint64_t userID = [userIDObj unsignedLongLongValue];
        
        int result = [[NERtcEngine sharedEngine] setupRemoteSubStreamVideoCanvas:canvas forUserID:userID];
        
        resolver(@(result));
    });
}

RCT_REMAP_METHOD(startPreview,
                 startPreviewWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] startPreview];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(stopPreview,
                 stopPreviewWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] stopPreview];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(setLocalVideoConfig,
                 setLocalVideoConfigWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *maxProfileObj = [RCTConvert NSNumber:params[@"maxProfile"]];
    NSNumber *frameRateObj = [RCTConvert NSNumber:params[@"frameRate"]];
    NSNumber *minFrameRateObj = [RCTConvert NSNumber:params[@"minFrameRate"]];
    NSNumber *bitrateObj = [RCTConvert NSNumber:params[@"bitrate"]];
    NSNumber *minBitrateObj = [RCTConvert NSNumber:params[@"minBitrate"]];
    NSNumber *widthObj = [RCTConvert NSNumber:params[@"width"]];
    NSNumber *heightObj = [RCTConvert NSNumber:params[@"height"]];
    NSNumber *cropModeObj = [RCTConvert NSNumber:params[@"cropMode"]];
    NSNumber *degradationPreferenceObj = [RCTConvert NSNumber:params[@"degradationPreference"]];
    NSNumber *mirrorModeObj = [RCTConvert NSNumber:params[@"mirrorMode"]];
    NSNumber *orientationModeObj = [RCTConvert NSNumber:params[@"orientationMode"]];
    
    NERtcVideoEncodeConfiguration *encodeConfig = [[NERtcVideoEncodeConfiguration alloc] init];
    if (maxProfileObj) {
        encodeConfig.maxProfile = (NERtcVideoProfileType)[maxProfileObj unsignedIntegerValue];
    }
    if (frameRateObj) {
        encodeConfig.frameRate = (NERtcVideoFrameRate)[frameRateObj unsignedIntegerValue];
    }
    if (minFrameRateObj) {
        encodeConfig.minFrameRate = [minFrameRateObj integerValue];
    }
    if (bitrateObj) {
        encodeConfig.bitrate = [bitrateObj integerValue];
    }
    if (minBitrateObj) {
        encodeConfig.minBitrate = [minBitrateObj integerValue];
    }
    if (widthObj) {
        encodeConfig.width = [widthObj intValue];
    }
    if (heightObj) {
        encodeConfig.height = [heightObj intValue];
    }
    if (cropModeObj) {
        encodeConfig.cropMode = (NERtcVideoCropMode)[cropModeObj unsignedIntegerValue];
    }
    if (degradationPreferenceObj) {
        encodeConfig.degradationPreference = (NERtcDegradationPreference)[degradationPreferenceObj unsignedIntegerValue];
    }
    if (mirrorModeObj) {
        encodeConfig.mirrorMode = (NERtcVideoMirrorMode)[mirrorModeObj unsignedIntegerValue];
    }
    if (orientationModeObj) {
        encodeConfig.orientationMode = (NERtcVideoOutputOrientationMode)[orientationModeObj unsignedIntegerValue];
    }
    
    int result = [[NERtcEngine sharedEngine] setLocalVideoConfig:encodeConfig];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(setLoudspeakerMode,
                 setLoudspeakerMode:(BOOL)enable
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] setLoudspeakerMode:enable];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(enableLocalVideo,
                 enableLocalVideo:(BOOL)enable
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] enableLocalVideo:enable];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(enableLocalAudio,
                 enableLocalAudio:(BOOL)enable
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] enableLocalAudio:enable];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(muteLocalVideo,
                 muteLocalVideo:(BOOL)muted
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] muteLocalVideo:muted];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(muteLocalAudio,
                 muteLocalAudio:(BOOL)muted
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] muteLocalAudio:muted];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(switchCamera,
                 switchCameraWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] switchCamera];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(startScreenCapture,
                 startScreenCaptureWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *maxProfileObj = [RCTConvert NSNumber:params[@"maxProfile"]];
    NSNumber *frameRateObj = [RCTConvert NSNumber:params[@"frameRate"]];
    NSNumber *minFrameRateObj = [RCTConvert NSNumber:params[@"minFrameRate"]];
    NSNumber *bitrateObj = [RCTConvert NSNumber:params[@"bitrate"]];
    NSNumber *minBitrateObj = [RCTConvert NSNumber:params[@"minBitrate"]];
    NSNumber *contentPreferObj = [RCTConvert NSNumber:params[@"contentPrefer"]];
    
    NERtcVideoSubStreamEncodeConfiguration *encodeConfig = [[NERtcVideoSubStreamEncodeConfiguration alloc] init];
    if (maxProfileObj) {
        encodeConfig.maxProfile = (NERtcVideoProfileType)[maxProfileObj unsignedIntegerValue];
    }
    if (frameRateObj) {
        encodeConfig.frameRate = (NERtcVideoFrameRate)[frameRateObj unsignedIntegerValue];
    }
    if (minFrameRateObj) {
        encodeConfig.minFrameRate = [minFrameRateObj integerValue];
    }
    if (bitrateObj) {
        encodeConfig.bitrate = [bitrateObj integerValue];
    }
    if (minBitrateObj) {
        encodeConfig.minBitrate = [minBitrateObj integerValue];
    }
    if (contentPreferObj) {
        encodeConfig.contentPrefer = (NERtcSubStreamContentPrefer)[contentPreferObj unsignedIntegerValue];
    }
    
    int result = [[NERtcEngine sharedEngine] startScreenCapture:encodeConfig];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(stopScreenCapture,
                 stopScreenCaptureWithResolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = [[NERtcEngine sharedEngine] stopScreenCapture];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(setExternalVideoSource,
                 setExternalVideoSourceWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    BOOL enable = [RCTConvert BOOL:params[@"enable"]];
    BOOL isScreen = [RCTConvert BOOL:params[@"isScreen"]];
    
    int result = [[NERtcEngine sharedEngine] setExternalVideoSource:enable isScreen:isScreen];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(subscribeRemoteVideo,
                 subscribeRemoteVideoWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *userIDObj = [RCTConvert NSNumber:params[@"userID"]];
    NSNumber *streamTypeObj = [RCTConvert NSNumber:params[@"streamType"]];
    if (!userIDObj || !streamTypeObj) {
        resolver(@(kNERtcErrInvalidParam));
        
        return;
    }
    
    BOOL subscribe = [RCTConvert BOOL:params[@"subscribe"]];
    uint64_t userID = [userIDObj unsignedLongLongValue];
    NERtcRemoteVideoStreamType streamType = (NERtcRemoteVideoStreamType)[streamTypeObj unsignedIntegerValue];
    
    int result = [[NERtcEngine sharedEngine] subscribeRemoteVideo:subscribe forUserID:userID streamType:streamType];
    
    resolver(@(result));
}

//RCT_REMAP_METHOD(subscribeRemoteAudio,
//                 subscribeRemoteAudioWithParams:(NSDictionary*)params
//                 resolver:(RCTPromiseResolveBlock)resolver
//                 rejecter:(RCTPromiseRejectBlock)rejecter)
//{
//    NSLog(@"%s", __func__);
//
//    BOOL subscribe = [RCTConvert BOOL:params[@"subscribe"]];
//    uint64_t userID = [[RCTConvert NSNumber:params[@"userID"]] unsignedLongLongValue];
//
//    int result = [[NERtcEngine sharedEngine] subscribeRemoteAudio:subscribe forUserID:userID];
//
//    resolver(@(result));
//}

RCT_REMAP_METHOD(subscribeRemoteSubStreamVideo,
                 subscribeRemoteSubStreamVideoWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *userIDObj = [RCTConvert NSNumber:params[@"userID"]];
    if (!userIDObj) {
        resolver(@(kNERtcErrInvalidParam));
        
        return;
    }
    
    BOOL subscribe = [RCTConvert BOOL:params[@"subscribe"]];
    uint64_t userID = [userIDObj unsignedLongLongValue];
    
    int result = [[NERtcEngine sharedEngine] subscribeRemoteSubStreamVideo:subscribe forUserID:userID];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(enableAudioVolumeIndication,
                 enableAudioVolumeIndicationWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSNumber *enableObj = [RCTConvert NSNumber:params[@"enable"]];
    NSNumber *intervalObj = [RCTConvert NSNumber:params[@"interval"]];
    NSNumber *enableVadObj = [RCTConvert NSNumber:params[@"enableVad"]];
    
    if (!enableObj) {
        resolver(@(kNERtcErrInvalidParam));
        
        return;
    }
    
    BOOL enable = [enableObj boolValue];
    uint64_t interval = intervalObj ? [intervalObj unsignedLongLongValue] : 200;
    BOOL enableVad = enableVadObj ? [enableVadObj boolValue] : YES;
    
    int result = [[NERtcEngine sharedEngine] enableAudioVolumeIndication:enable interval:interval vad:enableVad];
    
    resolver(@(result));
}

RCT_REMAP_METHOD(setStatsObserver,
                 setStatsObserver:(BOOL)enable
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    int result = kNERtcNoError;
    if (enable) {
        result = [[NERtcEngine sharedEngine] addEngineMediaStatsObserver:self];
    } else {
        result = [[NERtcEngine sharedEngine] removeEngineMediaStatsObserver:self];
    }
    
    resolver(@(result));
}

#pragma mark - NERtcEngineDelegateEx

- (void)onNERtcEngineDidError:(NERtcError)errCode {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(errCode) forKey:@"errorCode"];
    
    [self sendEventWithName:@"onError" body:info];
}

- (void)onNERtcEngineDidWarning:(NERtcWarning)warnCode msg:(NSString *)msg {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(warnCode) forKey:@"code"];
    [info setObject:msg ?: @"" forKey:@"message"];
    
    [self sendEventWithName:@"onWarning" body:info];
}

- (void)onNERtcEngineRejoinChannel:(NERtcError)result {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(result) forKey:@"result"];
    
    [self sendEventWithName:@"onRejoinChannel" body:info];
}

- (void)onNERtcEngineDidLeaveChannelWithResult:(NERtcError)result {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(result) forKey:@"result"];
    
    [self sendEventWithName:@"onLeaveChannel" body:info];
}

- (void)onNERtcEngineDidDisconnectWithReason:(NERtcError)reason {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(reason) forKey:@"reason"];
    
    [self sendEventWithName:@"onDisconnect" body:info];
}

- (void)onNERtcEngineConnectionStateChangeWithState:(NERtcConnectionStateType)state reason:(NERtcReasonConnectionChangedType)reason {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(state) forKey:@"state"];
    [info setObject:@(reason) forKey:@"reason"];
    
    [self sendEventWithName:@"onConnectionStateChanged" body:info];
}

- (void)onNERtcEngineNetworkConnectionTypeChanged:(NERtcNetworkConnectionType)newConnectionType {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(newConnectionType) forKey:@"connectionType"];
    
    [self sendEventWithName:@"onConnectionTypeChanged" body:info];
}

- (void)onNERtcEngineUserDidJoinWithUserID:(uint64_t)userID userName:(NSString *)userName {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    [info setObject:userName ?: @"" forKey:@"userName"];
    
    [self sendEventWithName:@"onUserJoin" body:info];
}

- (void)onNERtcEngineUserDidLeaveWithUserID:(uint64_t)userID reason:(NERtcSessionLeaveReason)reason {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    [info setObject:@(reason) forKey:@"reason"];
    
    [self sendEventWithName:@"onUserLeave" body:info];
}

- (void)onNERtcEngineUserVideoDidStartWithUserID:(uint64_t)userID videoProfile:(NERtcVideoProfileType)profile {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    [info setObject:@(profile) forKey:@"maxProfile"];
    
    [self sendEventWithName:@"onUserVideoStart" body:info];
}

- (void)onNERtcEngineUserVideoDidStop:(uint64_t)userID {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    
    [self sendEventWithName:@"onUserVideoStop" body:info];
}

- (void)onNERtcEngineUserAudioDidStart:(uint64_t)userID {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    
    [self sendEventWithName:@"onUserAudioStart" body:info];
}

- (void)onNERtcEngineUserAudioDidStop:(uint64_t)userID {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"uid"];
    
    [self sendEventWithName:@"onUserAudioStop" body:info];
}

- (void)onNERtcEngineUserSubStreamDidStartWithUserID:(uint64_t)userID subStreamProfile:(NERtcVideoProfileType)profile {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"userID"];
    [info setObject:@(profile) forKey:@"profile"];
    
    [self sendEventWithName:@"onUserSubStreamVideoStart" body:info];
}

- (void)onNERtcEngineUserSubStreamDidStop:(uint64_t)userID {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(userID) forKey:@"userID"];
    
    [self sendEventWithName:@"onUserSubStreamVideoStop" body:info];
}

- (void)onNERtcEngineAudioDeviceRoutingDidChange:(NERtcAudioOutputRouting)routing {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(routing) forKey:@"selected"];
    
    [self sendEventWithName:@"onAudioDeviceChanged" body:info];
}

- (void)onNERtcEngineAudioDeviceStateChangeWithDeviceID:(NSString *)deviceID deviceType:(NERtcAudioDeviceType)deviceType deviceState:(NERtcAudioDeviceState)deviceState {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:deviceID ?: @"" forKey:@"deviceID"];
    [info setObject:@(deviceType) forKey:@"deviceType"];
    [info setObject:@(deviceState) forKey:@"deviceState"];
    
    [self sendEventWithName:@"onAudioDeviceStateChange" body:info];
}

#pragma mark - NERtcEngineMediaStatsObserver

- (void)onLocalAudioStat:(NERtcAudioSendStats *)stat {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *layerArray = [NSMutableArray array];
    for (NERtcAudioLayerSendStats *audioLayer in stat.audioLayers) {
        NSMutableDictionary *layerInfo = [NSMutableDictionary dictionary];
        [layerInfo setObject:@(audioLayer.streamType) forKey:@"streamType"];
        [layerInfo setObject:@(audioLayer.sentBitrate) forKey:@"sentBitrate"];
        [layerInfo setObject:@(audioLayer.lossRate) forKey:@"lossRate"];
        [layerInfo setObject:@(audioLayer.rtt) forKey:@"rtt"];
        [layerInfo setObject:@(audioLayer.volume) forKey:@"volume"];
        [layerInfo setObject:@(audioLayer.capVolume) forKey:@"capVolume"];
        [layerInfo setObject:@(audioLayer.numChannels) forKey:@"numChannels"];
        [layerInfo setObject:@(audioLayer.sentSampleRate) forKey:@"sentSampleRate"];
        
        [layerArray addObject:layerInfo];
    }
    [info setObject:layerArray forKey:@"audioLayers"];
    
    [self sendEventWithName:@"onLocalAudioStats" body:info];
}

- (void)onLocalVideoStat:(NERtcVideoSendStats *)stat {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *layerArray = [NSMutableArray array];
    for (NERtcVideoLayerSendStats *videoLayer in stat.videoLayers) {
        NSMutableDictionary *layerInfo = [NSMutableDictionary dictionary];
        [layerInfo setObject:@(videoLayer.layerType) forKey:@"layerType"];
        [layerInfo setObject:@(videoLayer.width) forKey:@"width"];
        [layerInfo setObject:@(videoLayer.height) forKey:@"height"];
        [layerInfo setObject:@(videoLayer.captureWidth) forKey:@"capWidth"];
        [layerInfo setObject:@(videoLayer.captureHeight) forKey:@"capHeight"];
        [layerInfo setObject:@(videoLayer.sendBitrate) forKey:@"sendBitrate"];
        [layerInfo setObject:@(videoLayer.encoderOutputFrameRate) forKey:@"encoderOutputFrameRate"];
        [layerInfo setObject:@(videoLayer.captureFrameRate) forKey:@"captureFrameRate"];
        [layerInfo setObject:@(videoLayer.targetBitrate) forKey:@"targetBitrate"];
        [layerInfo setObject:@(videoLayer.encoderBitrate) forKey:@"encoderBitrate"];
        [layerInfo setObject:@(videoLayer.sentFrameRate) forKey:@"sentFrameRate"];
        [layerInfo setObject:@(videoLayer.renderFrameRate) forKey:@"renderFrameRate"];
        [layerInfo setObject:videoLayer.encoderName ?: @"" forKey:@"encoderName"];
        
        [layerArray addObject:layerInfo];
    }
    [info setObject:layerArray forKey:@"videoLayers"];
    
    [self sendEventWithName:@"onLocalVideoStats" body:info];
}

- (void)onLocalAudioVolumeIndication:(int)volume withVad:(BOOL)enableVad {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    [info setObject:@(volume) forKey:@"volume"];
    [info setObject:@(enableVad) forKey:@"vadFlag"];
    
    [self sendEventWithName:@"onLocalAudioVolumeIndication" body:info];
}

- (void)onRemoteAudioVolumeIndication:(NSArray<NERtcAudioVolumeInfo *> *)speakers totalVolume:(int)totalVolume {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *volumeArray = [NSMutableArray array];
    for (NERtcAudioVolumeInfo *volumeInfo in speakers) {
        NSMutableDictionary *volumeInfoDic = [NSMutableDictionary dictionary];
        [volumeInfoDic setObject:@(volumeInfo.uid) forKey:@"uid"];
        [volumeInfoDic setObject:@(volumeInfo.volume) forKey:@"volume"];
        
        [volumeArray addObject:volumeInfoDic];
    }
    [info setObject:volumeArray forKey:@"remoteAudioVolumeInfos"];
    [info setObject:@(totalVolume) forKey:@"totalVolume"];
    
    [self sendEventWithName:@"onRemoteAudioVolumeIndication" body:info];
}

- (void)onRemoteAudioStats:(NSArray<NERtcAudioRecvStats *> *)stats {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *statArray = [NSMutableArray array];
    for (NERtcAudioRecvStats *stat in stats) {
        NSMutableDictionary *statInfo = [NSMutableDictionary dictionary];
        NSMutableArray *layerArray = [NSMutableArray array];
        for (NERtcAudioLayerRecvStats *audioLayer in stat.audioLayers) {
            NSMutableDictionary *layerInfo = [NSMutableDictionary dictionary];
            [layerInfo setObject:@(audioLayer.streamType) forKey:@"streamType"];
            [layerInfo setObject:@(audioLayer.receivedBitrate) forKey:@"receivedBitrate"];
            [layerInfo setObject:@(audioLayer.audioLossRate) forKey:@"lossRate"];
            [layerInfo setObject:@(audioLayer.volume) forKey:@"volume"];
            [layerInfo setObject:@(audioLayer.totalFrozenTime) forKey:@"totalFrozenTime"];
            [layerInfo setObject:@(audioLayer.frozenRate) forKey:@"frozenRate"];
            
            [layerArray addObject:layerInfo];
        }
        [statInfo setObject:layerArray forKey:@"audioLayers"];
        [statInfo setObject:@(stat.uid) forKey:@"uid"];
        
        [statArray addObject:statInfo];
    }
    [info setObject:statArray forKey:@"audioRecvStats"];
    
    [self sendEventWithName:@"onRemoteAudioStats" body:info];
}

- (void)onRemoteVideoStats:(NSArray<NERtcVideoRecvStats *> *)stats {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *statArray = [NSMutableArray array];
    for (NERtcVideoRecvStats *stat in stats) {
        NSMutableDictionary *statInfo = [NSMutableDictionary dictionary];
        NSMutableArray *layerArray = [NSMutableArray array];
        for (NERtcVideoLayerRecvStats *videoLayer in stat.videoLayers) {
            NSMutableDictionary *layerInfo = [NSMutableDictionary dictionary];
            [layerInfo setObject:@(videoLayer.layerType) forKey:@"layerType"];
            [layerInfo setObject:@(videoLayer.width) forKey:@"width"];
            [layerInfo setObject:@(videoLayer.height) forKey:@"height"];
            [layerInfo setObject:@(videoLayer.receivedBitrate) forKey:@"receivedBitrate"];
            [layerInfo setObject:@(videoLayer.fps) forKey:@"fps"];
            [layerInfo setObject:@(videoLayer.packetLossRate) forKey:@"packetLossRate"];
            [layerInfo setObject:@(videoLayer.decoderOutputFrameRate) forKey:@"decoderOutputFrameRate"];
            [layerInfo setObject:@(videoLayer.rendererOutputFrameRate) forKey:@"rendererOutputFrameRate"];
            [layerInfo setObject:@(videoLayer.totalFrozenTime) forKey:@"totalFrozenTime"];
            [layerInfo setObject:@(videoLayer.frozenRate) forKey:@"frozenRate"];
            
            [layerArray addObject:layerInfo];
        }
        [statInfo setObject:layerArray forKey:@"videoLayers"];
        [statInfo setObject:@(stat.uid) forKey:@"uid"];
        
        [statArray addObject:statInfo];
    }
    [info setObject:statArray forKey:@"videoRecvStats"];
    
    [self sendEventWithName:@"onRemoteVideoStats" body:info];
}

- (void)onNetworkQuality:(NSArray<NERtcNetworkQualityStats *> *)stats {
    if (!self.hasListeners) {
        return;
    }
    
    NSMutableDictionary *info = [NSMutableDictionary dictionary];
    NSMutableArray *statArray = [NSMutableArray array];
    for (NERtcNetworkQualityStats *stat in stats) {
        NSMutableDictionary *networkInfo = [NSMutableDictionary dictionary];
        [networkInfo setObject:@(stat.userId) forKey:@"userId"];
        [networkInfo setObject:@(stat.txQuality) forKey:@"upStatus"];
        [networkInfo setObject:@(stat.rxQuality) forKey:@"downStatus"];
        
        [statArray addObject:networkInfo];
    }
    [info setObject:statArray forKey:@"netWorkQualityInfos"];
    
    [self sendEventWithName:@"onNetworkQuality" body:info];
}

#pragma mark - RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents
{
    return @[
        @"onError",
        @"onWarning",
        @"onJoinChannel",
        @"onRejoinChannel",
        @"onLeaveChannel",
        @"onUserJoin",
        @"onUserLeave",
        @"onConnectionStateChanged",
        @"onDisconnect",
        @"onConnectionTypeChanged",
        @"onUserVideoStart",
        @"onUserVideoStop",
        @"onUserAudioStart",
        @"onUserAudioStop",
        @"onUserSubStreamVideoStart",
        @"onUserSubStreamVideoStop",
        @"onAudioDeviceChanged",
        @"onAudioDeviceStateChange",
        @"onLocalAudioStats",
        @"onLocalVideoStats",
        @"onLocalAudioVolumeIndication",
        @"onRemoteAudioVolumeIndication",
        @"onRemoteAudioStats",
        @"onRemoteVideoStats",
        @"onNetworkQuality"
    ];
}

@end
