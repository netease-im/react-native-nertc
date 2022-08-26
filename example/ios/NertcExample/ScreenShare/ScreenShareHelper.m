//
//  ScreenShareHelper.m
//  NertcExample
//
//  Created by zhangchenliang on 2022/8/2.
//

#import "ScreenShareHelper.h"

#import <NERtcSDK/NERtcSDK.h>
#import <NERtcReplayKit/NERtcReplayKit.h>

@interface ScreenShareHelper ()<NEScreenShareHostDelegate>

@end

@implementation ScreenShareHelper

#pragma mark - Public

+ (instancetype)sharedInstance {
    static ScreenShareHelper *helper = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        helper = [[ScreenShareHelper alloc] init];
    });
    
    return helper;
}

- (void)setupScreenShareKit {
    NEScreenShareHostOptions *options = [[NEScreenShareHostOptions alloc] init];
//#if DEBUG
//    options.appGroup = @"group.com.netease.NERtcDemo.yunxin";
    options.enableDebug = YES;
//#else
//    options.appGroup = @"group.com.netease.nmc.NERtcDemoScreenShare";
//#endif
    options.delegate = self;
    
    [[NEScreenShareHost sharedInstance] setupScreenshareOptions:options];
}

#pragma mark - NEScreenShareHostDelegate

- (void)onReceiveVideoFrame:(NEScreenShareVideoFrame *)videoFrame {
    NERtcVideoFrame *frame = [[NERtcVideoFrame alloc] init];
    frame.format = kNERtcVideoFormatI420;
    frame.width = videoFrame.width;
    frame.height = videoFrame.height;
    frame.buffer = (void *)[videoFrame.videoData bytes];
    frame.timestamp = videoFrame.timeStamp;
    frame.rotation = (NERtcVideoRotationType)videoFrame.rotation;
    int ret = [NERtcEngine.sharedEngine pushExternalVideoFrame:frame];
    if (ret != 0 && ret != kNERtcErrFatal) {
        NSLog(@"发送视频流失败: %@", NERtcErrorDescription(ret));
    }
}

@end
