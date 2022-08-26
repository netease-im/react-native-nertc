/**
 * 视频镜像模式
 * 0: 默认（由 SDK 决定模式）
 * 1: 启用镜像模式
 * 2: 关闭镜像模式
 */
enum MirrorMode {
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
enum RenderMode {
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
enum CropMode {
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
enum OrientationMode {
  Adaptative = 0,
  Landscape = 1,
  Portrait = 2
}

/**
 * 远端视频流画质
 * 0: 高清
 * 1: 低清
 */
enum StreamType {
  HIGH = 0,
  LOW = 1,
}

/**
 * 视频帧率
 */
enum FrameRate {
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
enum ProfileType {
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
enum DegradationPreference {
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
  enable: true
  interval: number
  enableVad: boolean
}

type AudioLayer = {
  streamType: StreamType
  sentBitrate: number
  lossRate: number
  rtt: number
  volume: number
  numChannels: number
  sentSampleRate: number
  capVolume: number
}

type RecvAudioLayer = {
  streamType: StreamType
  receivedBitrate: number
  lossRate: number
  volume: number
  totalFrozenTime: number
  frozenRate: number
}

type AudioRecvState = {
  uid: number
  audioLayers: Array<RecvAudioLayer>
}

type AudioVolumeInfo = {
  uid: number
  volume: number
}

type VideoLayer = {
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

type RecvVideoLayer = {
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

type VideoRecvState = {
  uid: number
  videoLayers: Array<RecvVideoLayer>
}

type NetWorkQuality = {
  userId: number
  upStatus: number
  downStatus: number
}

export type Callback = (res: any) => void

export interface NERtcEvents {
  onError: (error: {errorCode: number}) => void
  onWarning: (warn: {code: number, message?: string}) => void
  onJoinChannel: (result: {error: string, channelId: number, elapesd: number, uid: number}) => void
  onRejoinChannel: (result: {result: number, channelId?: number}) => void
  onLeaveChannel: (result: {result: number}) => void
  onUserJoin: (result: {uid: number, userName: string}) => void
  onUserLeave: (result: {uid: number, userName: string}) => void
  onConnectionStateChanged: (state: {state: number, reason: string}) => void
  onDisconnect: (result: {result: number}) => void
  onConnectionTypeChanged: (connectiontype: {connectiontype: number}) => void
  onUserVideoStart: (result: {uid: number, maxProfile: number}) => void
  onUserVideoStop: (result: {uid: number}) => void
  onUserAudioStart: (result: {uid: number}) => void
  onUserAudioStop: (result: {uid: number}) => void
  onUserSubStreamVideoStart: (result: {userID: number, profile: number}) => void
  onUserSubStreamVideoStop: (result: {userID: number}) => void
  onLocalAudioStats: (result: {audioLayers: Array<AudioLayer>}) => void
  onLocalVideoStats: (result: {videoLayers: Array<VideoLayer>}) => void
  onLocalAudioVolumeIndication: (result: {volume: number, vadFlag: boolean}) => void
  onRemoteAudioVolumeIndication: (result: {remoteAudioVolumeInfos: Array<AudioVolumeInfo>, totalVolume: number}) => void
  onRemoteAudioStats: (result: {audioRecvStats: Array<AudioRecvState>}) => void
  onRemoteVideoStats: (result: {videoRecvStats: Array<VideoRecvState>}) => void
  onNetworkQuality: (result: {netWorkQualityInfos: Array<NetWorkQuality>}) => void
  onAudioDeviceChanged: (result: {selected: number}) => void
  onAudioDeviceStateChange: (result: {deviceID?: number, deviceType: number, deviceState: number}) => void
}

export type EventType = keyof NERtcEvents