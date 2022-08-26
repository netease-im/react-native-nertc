//
//  ScreenShareHelper.h
//  NertcExample
//
//  Created by zhangchenliang on 2022/8/2.
//

#import <Foundation/Foundation.h>

@interface ScreenShareHelper : NSObject

+ (instancetype)sharedInstance;

- (void)setupScreenShareKit;

@end
