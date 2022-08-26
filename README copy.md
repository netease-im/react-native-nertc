# react-native-nertc
NeRTC SDK for React Native
## Installation

```sh
npm install react-native-nertc
```

## Usage

```js
import NERTC from "react-native-nertc";

// 初始化引擎
NERTC.setupEngineWithContext({
  appKey: 'your appKey', // appKey
  logDir: '', // 日志路径
  logLevel: 3, // 日志级别
})

// 加入聊天室
NERTC.joinChannel({
  token: '', // 用户 token
  channelName: '' // 聊天室房间号 string,
  myUid: uid, // 用户 uid
})

// 添加回调 
//event 为事件名
NERTC.addListener('event', (res) => {
  console.log('response: ', res)
})

//...
```

## token

已获取的 NERTC Token

安全模式下必须设置为获取到的 Token 。若未传入正确的 Token 将无法进入房间。推荐使用安全模式。
调试模式下可设置为 null。安全性不高，建议在产品正式上线前在云信控制台中将鉴权方式恢复为默认的安全模式。

token 获取参考： [ token 获取 ](https://doc.yunxin.163.com/docs/jcyOTA0ODM/TQ0MTI2ODQ?platformId=50002)

## 可用接口列表

|  Api 列表   | 说明 |
|  ----  | ---- |
| setupEngineWithContext | 初始化 SDK 引擎 |
| destroyEngine | 销毁 SDK 引擎 |
| joinChannel | 加入音视频房间 |
| leaveChannel | 离开音视频房间 |
| setupLocalVideoCanvas | 设置本地视图 |
| setupRemoteVideoCanvas | 设置远端用户视图 |
| startPreview | 开启视频预览 |
| stopPreview | 停止视频预览 |
| setLocalVideoConfig | 设置视频编码属性 |
| setLoudspeakerMode | 开启/关闭扬声器 |
| enableLocalVideo | 是否开启本地视频采集 |
| enableLocalAudio | 是否开启本地音频采集 |
| muteLocalVideo | 开关本地视频发送 |
| muteLocalAudio | 开关本地音频发送 |
| switchCamera | 切换摄像头 |
| startScreenCapture | 开启屏幕捕捉 |
| stopScreenCapture | 关闭屏幕捕捉 |
| setExternalVideoSource | 开启/关闭外部视频辅流 |
| subscribeRemoteVideo | 取消或恢复订阅指定远端用户视频流 |
| subscribeRemoteSubStreamVideo | 取消或恢复订阅指定远端用户音频辅流 |
| setupLocalSubStreamVideoCanvas | 设置本地辅图 |
| setupRemoteSubStreamVideoCanvas | 设置远端辅图 |
| enableAudioVolumeIndication | 启用说话者音量提示 |
| addListener | 添加回调 |
| removeListener | 移除回调 |
| removeAllListeners | 清除全部回调 |
| setStatsObserver | 开启统计信息回调 |


接口参数可参考 typings 中的接口定义
## 可用回调

|  可用回调  | 说明 |
|  ----  | ---- |
| onError | 引擎异常 |
| onWarning | 警告 |
| onJoinChannel | 加入房间 |
| onRejoinChannel | 断线后重新加入房间 |
| onLeaveChannel | 离开房间 |
| onUserJoin | 有用户加入房间 |
| onUserLeave | 有用户离开房间 |
| onConnectionStateChanged | 网络连接状态变更 |
| onDisconnect | 断开连接 |
| onConnectionTypeChanged | 网络连接类型变更 |
| onUserVideoStart | 远端用户开启视频 |
| onUserVideoStop | 远端用户关闭视频回 |
| onUserAudioStart | 远端用户开启音频 |
| onUserAudioStop | 远端用户关闭音频 |
| onUserSubStreamVideoStart | 远端用户开启辅流 |
| onUserSubStreamVideoStop | 远端用户关闭辅流 |
| onLocalAudioStats | 本地音频状态统计信息 |
| onLocalVideoStats | 本地视频状态统计信息 |
| onLocalAudioVolumeIndicati' | 本地音频音量变化 |
| onRemoteAudioVolumeIndicatn' | 远端用户音频音量变化 |
| onRemoteAudioStats | 远端用户音频状态统计信息 |
| onRemoteVideoStats | 远端用户视频状态统计信息 |
| onNetworkQuality | 网络质量统计信息 |
| onAudioDeviceChanged | 音频设备改变 |
| onAudioDeviceStateChange | 音频设备状态改变 |


回调参数可参考 typings 中的 NERtcEvents 

## License

ISC

---
