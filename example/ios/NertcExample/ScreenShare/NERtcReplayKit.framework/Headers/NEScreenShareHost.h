//
//  NEScreenShareHost.h
//  NEScreenShareKit
//
//  Created by IM.NetEase on 2021/5/13.
//  Copyright © 2021 NetEase. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NEScreenShareHostOptions.h"
#import "NEScreenSharePublicDefine.h"

NS_ASSUME_NONNULL_BEGIN

@interface NEScreenShareHost : NSObject

/**
 *  单例接口，在Host App中使用
 */
+ (instancetype)sharedInstance NS_EXTENSION_UNAVAILABLE_IOS("NEScreenShareHost is not supported in extensions");

/**
 *  设置屏幕共享相关参数，具体见`NEScreenShareHostOptions`
 */
- (void)setupScreenshareOptions:(NEScreenShareHostOptions *)options NS_EXTENSION_UNAVAILABLE_IOS("NEScreenShareHost is not supported in extensions");

/**
 *   开启录制插件，iOS12以后可用
 */
//- (void)launchBroadcaster API_AVAILABLE(ios(12.0)) NS_EXTENSION_UNAVAILABLE_IOS("NEScreenShareHost is not supported in extensions");

/**
 *   停止共享
 */
- (void)stopBroadcaster NS_EXTENSION_UNAVAILABLE_IOS("NEScreenShareHost is not supported in extensions");

/**
 *   拉取音频共享数据
 *   如在`<NERtcReplayKit/NEScreenShareBroadcasterOptions.h>`中开启`needAudioSampleBuffer`
 *   SDK内部将会缓存系统的音频数据，数据长度为500ms
 *   业务侧可以通过此接口，向SDK拉取缓存的音频数据并混音到SDK中，达到共享系统音频的目的
 *   具体参见使用文档或SampleCode
 */
- (BOOL)pullAudioData:(void *_Nonnull*_Nonnull)destBuffer length:(int)destLength sampleRate:(int*)sampleRate channels:(int*)channels;


@end

NS_ASSUME_NONNULL_END
