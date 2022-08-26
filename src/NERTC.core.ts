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

  static getInstance(): NERTC {
    if (instance) {
      return instance
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

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

  static destroyEngine(): void {
    instance = undefined
    NERTC.removeAllListeners()
    Nertc.destroyEngine()
  }

  static async joinChannel(options: joinChannelOptions): Promise<number> {
    if (instance) {
      return Nertc.joinChannel(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async leaveChannel(): Promise<number> {
    if (instance) {
      return Nertc.leaveChannel()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async setStatsObserver(enable: boolean): Promise<number> {
    if (instance) {
      return Nertc.setStatsObserver(enable)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async setupLocalVideoCanvas(options: videoCanvasOptions): Promise<number> {
    if (instance) {
      return Nertc.setupLocalVideoCanvas(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

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

  static async startPreview(): Promise<number> {
    if (instance) {
      return Nertc.startPreview()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async stopPreview(): Promise<number> {
    if (instance) {
      return Nertc.stopPreview()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async setLocalVideoConfig(options: videoConfigOptions): Promise<number> {
    if (instance) {
      return Nertc.setLocalVideoConfig(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async setLoudspeakerMode(mode: boolean): Promise<number> {
    if (instance) {
      return Nertc.setLoudspeakerMode(mode)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }
  
  static async enableLocalVideo(enabel: boolean): Promise<number> {
    if (instance) {
      return Nertc.enableLocalVideo(enabel)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async enableLocalAudio(enabel: boolean): Promise<number> {
    if (instance) {
      return Nertc.enableLocalAudio(enabel)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async muteLocalVideo(mute: boolean): Promise<number> {
    if (instance) {
      return Nertc.muteLocalVideo(mute)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async muteLocalAudio(mute: boolean): Promise<number> {
    if (instance) {
      return Nertc.muteLocalAudio(mute)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async switchCamera(): Promise<number> {
    if (instance) {
      return Nertc.switchCamera()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async startScreenCapture(options: screenCaptureOptions): Promise<number> {
    if (instance) {
      return Nertc.startScreenCapture(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

  static async stopScreenCapture(): Promise<number> {
    if (instance) {
      return Nertc.stopScreenCapture()
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

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

  static async setupLocalSubStreamVideoCanvas(options: videoCanvasOptions): Promise<number> {
    if (instance) {
      return Nertc.setupLocalSubStreamVideoCanvas(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

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

  static async enableAudioVolumeIndication(options: AudioVolIndicationOptions): Promise<number> {
    if (instance) {
      return Nertc.enableAudioVolumeIndication(options)
    }
    throw new Error(NERTC_ERROR.NO_INST)
  }

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
