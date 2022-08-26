//
//  NEScreenShareSampleHandler.h
//  NEScreenShareBroadcaster
//
//  Created by IM.NetEase on 2021/5/28.
//

#import <ReplayKit/ReplayKit.h>
@class NEScreenShareBroadcasterOptions;

NS_ASSUME_NONNULL_BEGIN
API_AVAILABLE(ios(11.0))
@interface NEScreenShareSampleHandler : NSObject

+ (instancetype)sharedInstance;

/**
 *  在Broadcast SampleHandler中`broadcastStartedWithSetupInfo:`中调用
 *  相关options参数，具体见NEScreenShareBroadcasterOptions
 */
- (void)broadcastStartedWithSetupInfo:(NEScreenShareBroadcasterOptions *)options;

/**
 *  在Broadcast SampleHandler中`broadcastPaused`中调用
 */
- (void)broadcastPaused;

/**
 *  在Broadcast SampleHandler中`broadcastPaused`中调用
 */
- (void)broadcastResumed;

/**
 *  在Broadcast SampleHandler中`broadcastResumed`中调用
 */
- (void)broadcastFinished;

/**
 Broadcast发生该回调Error时，透传给NERtcReplayKit
 */
- (void)finishBroadcastWithError:(NSError *)error;

/**
 *  在Broadcast SampleHandler中`processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType`中调用
 *  将Broadcast中系统扩展产生的数据，传递给SDK
 */
- (void)processSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType;

@end

NS_ASSUME_NONNULL_END
