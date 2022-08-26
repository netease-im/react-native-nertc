//
//  ScreenShareBridge.m
//  NertcExample
//
//  Created by zhangchenliang on 2022/8/2.
//

#import "ScreenShareBridge.h"

#import <React/RCTConvert.h>
#import <ReplayKit/ReplayKit.h>

#define IPHONE_OS_VERSION_iOS12 [UIDevice currentDevice].systemVersion.floatValue>=12.0f

API_AVAILABLE(ios(12.0))
@interface ScreenShareBridge ()

#ifdef IPHONE_OS_VERSION_iOS12
@property (nonatomic, strong) RPSystemBroadcastPickerView* broadPickerView;
#endif

@end

@implementation ScreenShareBridge

RCT_EXPORT_MODULE()

#pragma mark - ScreenShare JS Bridge

RCT_REMAP_METHOD(setupScreenShareButton,
                 setupScreenShareButtonWithParams:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolver
                 rejecter:(RCTPromiseRejectBlock)rejecter)
{
    NSLog(@"%s, self = %p", __func__, self);
  
    NSNumber *reactTagObj = [RCTConvert NSNumber:params[@"reactTag"]];
    if (!reactTagObj) {
        resolver(@(-1));
        
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([UIDevice currentDevice].systemVersion.floatValue >= 13.1f){
            if (@available(iOS 13.1, *)){
                self.broadPickerView = [[RPSystemBroadcastPickerView alloc] initWithFrame:CGRectMake(0, 0, 60, 60)];
//  #if DEBUG
                self.broadPickerView.preferredExtension = @"com.netease.nmc.NERtcSample-ScreenShare-iOS-Objective-C.Broadcast";
//                self.broadPickerView.preferredExtension = nil;
//  #else
//                self.broadPickerView.preferredExtension = @"com.netease.nmc.NRTC.demoG2.Broadcast";
//  #endif
                self.broadPickerView.showsMicrophoneButton = NO;
                
                UIView *container = [self.bridge.uiManager viewForReactTag:reactTagObj];
                [container addSubview:self.broadPickerView];
            }
        }
        
        resolver(@(0));
    });
}

#pragma mark - RCTEventEmitter

- (NSArray<NSString *> *)supportedEvents
{
    return @[];
}

@end
