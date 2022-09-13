/**
 * 视频镜像模式
 * 0: 默认（由 SDK 决定模式）
 * 1: 启用镜像模式
 * 2: 关闭镜像模式
 */
export enum MirrorMode {
  AUTO = 0,
  ENABLE = 1,
  DISABLE = 2
}

/**
 * 视频渲染缩放：
 * 0: 等比缩放，优先保证视频内容全部显示，未被填满的区域填充背景色
 * 1: 填满视窗
 * 2: 适配视窗，超出部分裁切
 */
 export enum RenderMode {
  Fit = 0,
  FullFill = 1,
  CropFill = 2
}

/**
 * 视频裁切模式：
 * 0: 不裁切
 * 1: 16:9
 * 2: 4:3
 * 3: 1:1
 */
 export enum CropMode {
  Origin = 0,
  Mode16_9 = 1,
  Mode4_3 = 2,
  Mode1_1 = 3
}

/**
 * 视频旋转方向
 * 0: 与采集到的视频方向一致
 * 1: 横屏
 * 2: 竖屏
 */
 export enum OrientationMode {
  Adaptative = 0,
  Landscape = 1,
  Portrait = 2
}

/**
 * 远端视频流画质
 * 0: 高清
 * 1: 低清
 */
 export enum StreamType {
  HIGH = 0,
  LOW = 1,
}

/**
 * 视频帧率
 */
 export enum FrameRate {
  Default = 0,
  Fps7 = 7,
  Fps10 = 10,
  Fps15 = 15,
  Fps24 = 24,
  Fps30 = 30,
  Fps60 = 60
}

/**
 * 视频 profile 类型
 */
 export enum ProfileType {
  Lowest = 0,
  Low = 1,
  Standard = 2,
  HD720P = 3,
  HD1080P = 4,
  MAX = 4
}

/**
 * 带宽受限制时视频编码适应性偏好
 * 0: 通信场景，使用 Balanced ； 直播场景，使用 MaintainQuantity
 * 1: 降低视频分辨率以保证编码帧率
 * 2: 降低编码帧率以保证视频分辨率
 * 3: 平衡调节
 */
 export enum DegradationPreference {
  Default = 0,
  MaintainFramerate,
  MaintainQuality,
  Balanced
}

export interface setupOptions {
  appKey: string
  logDir: string
  logLevel: number
}

export interface joinChannelOptions {
  token: string
  channelName: string
  myUid: number
}

export interface videoCanvasOptions {
  reactTag: number
  renderMode: RenderMode
  mirrorMode: MirrorMode
  isMediaOverlay?: boolean
}

export interface remoteVideoCanvasOptions extends videoCanvasOptions {
  userID: number
}

export interface videoConfigOptions {
  maxProfile: ProfileType
  frameRate: FrameRate
  minFrameRate: number
  bitrate: number
  minBitrate: number
  width: number
  height: number
  cropMode: CropMode
  degradationPreference: DegradationPreference
  mirrorMode: MirrorMode
  orientationMode: OrientationMode
}

export interface screenCaptureOptions {
  maxProfile: number
  frameRate: number
  minFrameRate: number
  bitrate: number
  minBitrate: number
  contentPrefer: number
}

export interface externalVideoSourceOptions {
  enable: boolean
  isScreen: boolean
}

export interface subscribeRemoteVideoOptions {
  subscribe: boolean
  userID: number
  streamType: StreamType
}

export interface subscribeRemoteSubVideoOptions {
  subscribe: boolean
  userID: number
}

export interface AudioVolIndicationOptions {
  enable: boolean
  interval: number
  enableVad: boolean
}

export type AudioLayer = {
  streamType: StreamType
  sentBitrate: number
  lossRate: number
  rtt: number
  volume: number
  numChannels: number
  sentSampleRate: number
  capVolume: number
}

export type RecvAudioLayer = {
  streamType: StreamType
  receivedBitrate: number
  lossRate: number
  volume: number
  totalFrozenTime: number
  frozenRate: number
}

export type AudioRecvState = {
  uid: number
  audioLayers: Array<RecvAudioLayer>
}

export type AudioVolumeInfo = {
  uid: number
  volume: number
}

export type VideoLayer = {
  layerType: number // 1: 主流，2: 辅流
  capWidth: number
  capHeight: number
  width: number
  height: number
  sendBitrate: number
  encoderOutputFrameRate: number
  captureFrameRate: number
  targetBitrate: number
  encoderBitrate: number
  sentFrameRate: number
  renderFrameRate: number
  encoderName: string
}

export type RecvVideoLayer = {
  layerType: number // 1: 主流，2: 辅流
  width: number
  height: number
  receivedBitrate: number
  fps: number
  packetLossRate: number
  decoderOutputFrameRate: number
  rendererOutputFrameRate: number
  totalFrozenTime: number
  frozenRate: number
}

export type VideoRecvState = {
  uid: number
  videoLayers: Array<RecvVideoLayer>
}

export type NetWorkQuality = {
  userId: number
  upStatus: number
  downStatus: number
}

export type Callback = (res: any) => void

export interface NERtcEvents {
  /**
   * 引擎发生了运行时的错误，需要用户干预
   */
  onError: (error: {errorCode: number}) => void
  /**
   * 发生警告回调
   */
  onWarning: (warn: {code: number, message?: string}) => void
  /**
   * 加入房间回调，表示客户端已经登入服务器
   * error:	加入房间出错的错误描述
   * channelId:	客户端加入的房间 ID。
   * elapsed:	从 joinChannel 开始到发生此事件过去的时间，单位为毫秒。
   * uid:	用户 ID。如果在 joinChannel 方法中指定了 uid，此处会返回指定的 ID; 如果未指定 uid（joinChannel 时uid=0），此处将返回云信服务器自动分配的 ID。
   */
  onJoinChannel: (result: {error: string, channelId: number, elapesd: number, uid: number}) => void
  /**
   * 重新加入房间回调
   * 在弱网环境下，若客户端和服务器失去连接，SDK 会自动重连。自动重连成功后触发此回调方法
   */
  onRejoinChannel: (result: {result: number, channelId?: number}) => void
  /**
   * 离开房间，即挂断或退出通话。
   * 结束通话时，必须调用leaveChannel结束通话，否则无法开始下一次通话
   * result: 操作返回值，成功则返回 0
   */
  onLeaveChannel: (result: {result: number}) => void
  /**
   * 远端用户（通信场景）/主播（直播场景）加入当前频道回调。
   * - 通信场景下，该回调提示有远端用户加入了频道，并返回新加入用户的 ID；如果加入之前，已经有其他用户在频道中了，新加入的用户也会收到这些已有用户加入频道的回调
   * - 直播场景下，该回调提示有主播加入了频道，并返回该主播的用户 ID。如果在加入之前，已经有主播在频道中了，新加入的用户也会收到已有主播加入频道的回调。
   * 
   * 该回调在如下情况下会被触发：
   * - 远端用户调用 joinChannel 方法加入房间。
   * - 远端用户网络中断后重新加入房间。
   * uid: 远端用户 id
   * userName: 远端用户名称
   */
  onUserJoin: (result: {uid: number, userName: string}) => void
  /**
   * 远端用户离开当前房间回调
   * 
   * uid: 离开房间的远端用户 ID
   * reason: 离开原因
   */
  onUserLeave: (result: {uid: number, reason: string}) => void
  /**
   * 房间连接状态已改变回调
   * state: 当前房间的连接状态
   * reason: 引起当前房间连接状态发生改变的原因
   */
  onConnectionStateChanged: (state: {state: number, reason: string}) => void
  /**
   * 网络连接中断，且 SDK 连续 3 次重连服务器失败
   * reason	网络连接中断原因
   */
  onDisconnect: (result: {reason: number}) => void
  /**
   * 本地网络类型已改变回调
   * connectionType	当前的本地网络类型
   */
  onConnectionTypeChanged: (connectiontype: {connectiontype: number}) => void
  /**
   * 远端用户开启视频回调
   * uid	用户 ID
   * maxProfile	视频编码配置
   */
  onUserVideoStart: (result: {uid: number, maxProfile: number}) => void
  /**
   * 远端用户停用视频回调
   */
  onUserVideoStop: (result: {uid: number}) => void
  /**
   * 远端用户开启音频回调
   */
  onUserAudioStart: (result: {uid: number}) => void
  /**
   * 远端用户停用音频回调
   */
  onUserAudioStop: (result: {uid: number}) => void
  /**
   * 远端用户开启屏幕共享辅流通道的回调
   * uid	远端用户 ID
   * profile	远端视频分辨率等级
   */
  onUserSubStreamVideoStart: (result: {userID: number, profile: number}) => void
  /**
   * 远端用户停止屏幕共享辅流通道的回调
   */
  onUserSubStreamVideoStop: (result: {userID: number}) => void
  /**
   * 本地音频流统计信息回调
   */
  onLocalAudioStats: (result: {audioLayers: Array<AudioLayer>}) => void
  /**
   * 本地视频流统计信息回调
   */
  onLocalVideoStats: (result: {videoLayers: Array<VideoLayer>}) => void
  /**
   * 提示房间内本地用户瞬时音量的回调
   */
  onLocalAudioVolumeIndication: (result: {volume: number, vadFlag: boolean}) => void
  /**
   * 提示房间内谁正在说话及说话者瞬时音量的回调
   */
  onRemoteAudioVolumeIndication: (result: {remoteAudioVolumeInfos: Array<AudioVolumeInfo>, totalVolume: number}) => void
  /**
   * 通话中远端音频流的统计信息回调数组
   */
  onRemoteAudioStats: (result: {audioRecvStats: Array<AudioRecvState>}) => void
  /**
   * 通话中远端视频流的统计信息回调数组
   */
  onRemoteVideoStats: (result: {videoRecvStats: Array<VideoRecvState>}) => void
  /**
   * 通话中所有用户的网络状态回调
   */
  onNetworkQuality: (result: {netWorkQualityInfos: Array<NetWorkQuality>}) => void
  /**
   * 语音播放设备已改变回调
   */
  onAudioDeviceChanged: (result: {selected: number}) => void
  /**
   * 音频设备状态已改变回调
   */
  onAudioDeviceStateChange: (result: {deviceID?: number, deviceType: number, deviceState: number}) => void
}

export type EventType = keyof NERtcEvents