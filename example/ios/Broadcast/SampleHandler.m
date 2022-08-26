//
//  SampleHandler.m
//  Broadcast
//
//  Created by zhangchenliang on 2022/8/2.
//


#import "SampleHandler.h"

#import <NERtcReplayKit/NERtcReplayKit.h>

@implementation SampleHandler

- (void)broadcastStartedWithSetupInfo:(NSDictionary<NSString *,NSObject *> *)setupInfo {
    // User has requested to start the broadcast. Setup info from the UI extension can be supplied but optional.
    NEScreenShareBroadcasterOptions *options = [[NEScreenShareBroadcasterOptions alloc] init];
//#if DEBUG
//    options.appGroup = @"group.com.netease.NERtcDemo.yunxin";
    options.enableDebug = YES;
//#else
//    options.appGroup = @"group.com.netease.nmc.NERtcDemoScreenShare";
//#endif
    options.frameRate = 15;
    options.targetFrameSize = CGSizeMake(360, 640);
    options.needAudioSampleBuffer = NO;
    [[NEScreenShareSampleHandler sharedInstance] broadcastStartedWithSetupInfo:options];
}

- (void)broadcastPaused {
    // User has requested to pause the broadcast. Samples will stop being delivered.
    [[NEScreenShareSampleHandler sharedInstance] broadcastPaused];
}

- (void)broadcastResumed {
    // User has requested to resume the broadcast. Samples delivery will resume.
    [[NEScreenShareSampleHandler sharedInstance] broadcastResumed];
}

- (void)broadcastFinished {
    // User has requested to finish the broadcast.
    [[NEScreenShareSampleHandler sharedInstance] broadcastFinished];
}

- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType {
    [[NEScreenShareSampleHandler sharedInstance] processSampleBuffer:sampleBuffer withType:sampleBufferType];
}

@end
