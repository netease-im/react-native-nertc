import { NativeModules, Platform, NativeEventEmitter } from 'react-native';
import type { 
  joinChannelOptions, 
  screenCaptureOptions, 
  setupOptions, 
  videoCanvasOptions, 
  videoConfigOptions, 
  externalVideoSourceOptions,
  subscribeRemoteVideoOptions,
  remoteVideoCanvasOptions,
  AudioVolIndicationOptions,
  NERtcEvents,
  EventType,
  Callback,
  subscribeRemoteSubVideoOptions
 } from './typings/index';

const NERTC_ERROR = {
  NO_INST: 'NERTC instance has not been created',
  CREATE_FAILED: 'Setup engine failed, please check your parameter'
}

const LINKING_ERROR =
  `The package 'react-native-nertc' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Nertc = NativeModules.Nertc  ? NativeModules.Nertc  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const NertcEventManager = new NativeEventEmitter(Nertc)

let instance: NERTC | undefined

export default class NERTC {

  private static _listeners = new Map<string, Set<Callback>>();

  /**
   * 获取 NERTC 实例
   * @returns {NERTC} NERTC 实例
   */
  static getInstance(): NERTC {
    if (instance) {
      return instance
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 创建 NERTC 引擎实例
   * @param {setupOptions} options
   * @returns {Promise<NERTC>} Nertc 实例
   */
  static async setupEngineWithContext(options: setupOptions): Promise<NERTC> {
    if (instance) {
      return instance
    }
    let result: number = await Nertc.setupEngineWithContext(options)
    if (result === 0) {
      instance = new NERTC()
      return instance 
    }
    throw new Error(NERTC_ERROR.CREATE_FAILED)
  }

  /**
   * 销毁 NERTC 引擎实例
   */
  static destroyEngine(): void {
    instance = undefined
    NERTC.removeAllListeners()
    Nertc.destroyEngine()
  }

  /**
   * 加入音视频房间
   * @param {joinChannelOptions} options
   * @returns {Promise<number>}
   */
  static async joinChannel(options: joinChannelOptions): Promise<number> {
    if (instance) {
      return Nertc.joinChannel(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 离开音视频房间
   * @returns {Promise<number>}
   */
  static async leaveChannel(): Promise<number> {
    if (instance) {
      return Nertc.leaveChannel()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启/关闭统计信息回调
   * @param {boolean} enable 
   * @returns {Promise<number>}
   */
  static async setStatsObserver(enable: boolean): Promise<number> {
    if (instance) {
      return Nertc.setStatsObserver(enable)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }


  /**
   * 设置本地视图
   * @param {videoCanvasOptions}options 
   * @returns {Promise<number>}
   */
  static async setupLocalVideoCanvas(options: videoCanvasOptions): Promise<number> {
    if (instance) {
      return Nertc.setupLocalVideoCanvas(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 设置远端用户视图
   * @param {remoteVideoCanvasOptions} options 
   * @returns {Promise<number>}
   */
  static async setupRemoteVideoCanvas(options: remoteVideoCanvasOptions): Promise<number> {
    if (instance) {
      if (options.userID) {
        return Nertc.setupRemoteVideoCanvas(options)
      } else {
        return Promise.reject('userID needed, please check')
      }
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启视频预览
   * @returns {Promise<number>}
   */
  static async startPreview(): Promise<number> {
    if (instance) {
      return Nertc.startPreview()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 关闭视频预览
   * @returns {Promise<number>}
   */
  static async stopPreview(): Promise<number> {
    if (instance) {
      return Nertc.stopPreview()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 设置视频编码属性
   * @param {videoConfigOptions} options 
   * @returns 
   */
  static async setLocalVideoConfig(options: videoConfigOptions): Promise<number> {
    if (instance) {
      return Nertc.setLocalVideoConfig(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启/关闭扬声器
   * @param {boolean} mode 
   * @returns {Promise<number>}
   */
  static async setLoudspeakerMode(mode: boolean): Promise<number> {
    if (instance) {
      return Nertc.setLoudspeakerMode(mode)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }
  
  /**
   * 是否开启本地视频采集
   * @param {boolean} enabel 
   * @returns {Promise<number>}
   */
  static async enableLocalVideo(enabel: boolean): Promise<number> {
    if (instance) {
      return Nertc.enableLocalVideo(enabel)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启/关闭本地音频采集和发送
   * @param {boolean} enabel 
   * @returns {Promise<number>}
   */
  static async enableLocalAudio(enabel: boolean): Promise<number> {
    if (instance) {
      return Nertc.enableLocalAudio(enabel)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 停止或继续发送本地视频流
   * @param {boolean} mute 
   * @returns {Promise<number>}
   */
  static async muteLocalVideo(mute: boolean): Promise<number> {
    if (instance) {
      return Nertc.muteLocalVideo(mute)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 停止或继续发送本地音频流
   * @param {boolean} mute 
   * @returns {Promise<number>}
   */
  static async muteLocalAudio(mute: boolean): Promise<number> {
    if (instance) {
      return Nertc.muteLocalAudio(mute)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 切换前置/后置摄像头
   * 该方法需要在相机启动后调用，例如调用 startPreview 或 joinChannel 后
   * @returns {Promise<number>}
   */
  static async switchCamera(): Promise<number> {
    if (instance) {
      return Nertc.switchCamera()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启屏幕共享，屏幕共享内容以辅流形式发送
   * @param {screenCaptureOptions} options 
   * @returns {Promise<number>}
   */
  static async startScreenCapture(options: screenCaptureOptions): Promise<number> {
    if (instance) {
      return Nertc.startScreenCapture(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 停止屏幕共享
   * @returns {Promise<number>}
   */
  static async stopScreenCapture(): Promise<number> {
    if (instance) {
      return Nertc.stopScreenCapture()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 开启/关闭外部/辅助视频辅流
   * @param {externalVideoSourceOptions} options 
   * @returns {Promise<number>}
   */
  static async setExternalVideoSource(options: externalVideoSourceOptions): Promise<number> {
    if (instance) {
      const handler = Platform.select({
        android: Promise.resolve,
        ios: Nertc.setExternalVideoSource
      })
      return handler(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 取消或恢复订阅指定远端用户视频流
   * 该方法需要在加入房间后调用
   * @param {subscribeRemoteVideoOptions} options 
   * @returns {Promise<number>}
   */
  static async subscribeRemoteVideo(options: subscribeRemoteVideoOptions): Promise<number> {
    if (instance) {
      if (options.userID) {
        return Nertc.subscribeRemoteVideo(options)
      } else {
        return Promise.reject('userID needed, please check')
      }
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 订阅或取消订阅远端的屏幕共享辅流视频，订阅之后才能接收远端的辅流视频数据
   * @param {subscribeRemoteSubVideoOptions} options 
   * @returns {Promise<number>}
   */
  static async subscribeRemoteSubStreamVideo(options: subscribeRemoteSubVideoOptions): Promise<number> {
    if (instance) {
      if (options.userID) {
        return Nertc.subscribeRemoteSubStreamVideo(options)
      } else {
        return Promise.reject('userID needed, please check')
      }
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 设置本地辅流视频画布
   * @param {videoCanvasOptions} options 
   * @returns {Promise<number>}
   */
  static async setupLocalSubStreamVideoCanvas(options: videoCanvasOptions): Promise<number> {
    if (instance) {
      return Nertc.setupLocalSubStreamVideoCanvas(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 设置远端的辅流视频画布
   * @param {remoteVideoCanvasOptions} options 
   * @returns {Promise<number>}
   */
  static async setupRemoteSubStreamVideoCanvas(options: remoteVideoCanvasOptions): Promise<number> {
    if (instance) {
      if (options.userID) {
        return Nertc.setupRemoteSubStreamVideoCanvas(options)
      } else {
        return Promise.reject('userID needed, please check')
      }
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 启用说话者音量提示
   * @param {AudioVolIndicationOptions} options 
   * @returns {Promise<number>}
   */
  static async enableAudioVolumeIndication(options: AudioVolIndicationOptions): Promise<number> {
    if (instance) {
      return Nertc.enableAudioVolumeIndication(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  /**
   * 添加事件回调
   * @param event 事件名
   * @param callback 回调方法
   */
  static addListener(event: EventType, callback: NERtcEvents[EventType]): void {
    if (event) {
      let callbackSet = NERTC._listeners.get(event)
      if (callbackSet) {
        callbackSet.add(callback)
      } else {
        NERTC._listeners.set(event, new Set([callback]))
      }
      NertcEventManager.addListener(event, callback)
    }
  }

  /**
   * 移除事件回调
   * @param event 事件名
   * @param callback 回调方法
   */
  static removeListener(event: EventType, callback: NERtcEvents[EventType]) {
    if (event) {
      let callbackSet = NERTC._listeners.get(event)
      if (callbackSet) {
        callbackSet.delete(callback)
      } else {
        NERTC._listeners.set(event, new Set([callback]))
      }
      NertcEventManager.removeListener(event, callback)
    }
  }

  /**
   * 清除事件回调
   * @param event 事件名（可选），当不填写事件名时，清除全部事件回调
   */
  static removeAllListeners(event?: EventType): void {
    if (event) {
      NertcEventManager.removeAllListeners(event)
      NERTC._listeners.delete(event)
    } else {
      NERTC._listeners.forEach((_callbackSet, eventName) => {
        NertcEventManager.removeAllListeners(eventName)
      })
      NERTC._listeners.clear()
    }
  }
}
