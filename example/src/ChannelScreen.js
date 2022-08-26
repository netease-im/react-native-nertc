import * as React from 'react';

import {
  StyleSheet,
  View,
  Button,
  findNodeHandle,
  Text,
  TextInput,
  ScrollView,
} from 'react-native';
import NERTC, { NERtcView } from 'react-native-nertc';

export default function ChannelScreen({
  navigation,
  showLog,
  openVideoConfig,
}) {
  const localRtcRef = React.useRef(null);
  const localSubRtcRef = React.useRef(null);
  const remote1SubRtcRef = React.useRef(null);
  const remote1RtcRef = React.useRef(null);
  const remote2SubRtcRef = React.useRef(null);
  const remote2RtcRef = React.useRef(null);
  const remote3SubRtcRef = React.useRef(null);
  const remote3RtcRef = React.useRef(null);
  const [remote1Uid, setRemote1Uid] = React.useState('');
  const [remote2Uid, setRemote2Uid] = React.useState('');
  const [remote3Uid, setRemote3Uid] = React.useState('');
  const [remoteConfig, openRemoteConfig] = React.useState(false);
  const [volumeIndication, setVolumeIndication] = React.useState(false);
  const [volumeIndicationTime, setVolumeIndicationTime] = React.useState('500');

  React.useEffect(() => {
    // example
  }, []);

  return (
    <ScrollView style={{ flex: 1 }}>
      <View style={styles.channel}>
        <View style={styles.line}>
          <NERtcView style={styles.videoCanvas} ref={localRtcRef} />
          <NERtcView style={styles.videoCanvas} ref={localSubRtcRef} />
        </View>
        <View style={styles.line}>
          <NERtcView style={styles.videoCanvas} ref={remote1RtcRef} />
          <NERtcView style={styles.videoCanvas} ref={remote1SubRtcRef} />
        </View>
        <View style={styles.line}>
          <NERtcView style={styles.videoCanvas} ref={remote2RtcRef} />
          <NERtcView style={styles.videoCanvas} ref={remote2SubRtcRef} />
        </View>
        <View style={styles.line}>
          <NERtcView style={styles.videoCanvas} ref={remote3RtcRef} />
          <NERtcView style={styles.videoCanvas} ref={remote3SubRtcRef} />
        </View>
        <View style={styles.controller}>
          <Button
            title="设置本地视频画布"
            onPress={() => {
              NERTC.setupLocalVideoCanvas({
                reactTag: findNodeHandle(localRtcRef.current),
                renderMode: 0,
                mirrorMode: 0,
                isMediaOverlay: false,
              });
            }}
          />
          <Button
            title="设置本地辅流画布"
            onPress={() => {
              NERTC.setupLocalSubStreamVideoCanvas({
                reactTag: findNodeHandle(localSubRtcRef.current),
                renderMode: 0,
                mirrorMode: 0,
                isMediaOverlay: false,
              });
            }}
          />
          <Button
            title="本地视频配置"
            onPress={() => {
              openVideoConfig();
            }}
          />
          <Button
            title="开启摄像头"
            onPress={() => {
              NERTC.enableLocalVideo(true);
            }}
          />
          <Button
            title="关闭摄像头"
            onPress={() => {
              NERTC.enableLocalVideo(false);
            }}
          />
          <Button
            title="切换摄像头"
            onPress={() => {
              NERTC.switchCamera();
            }}
          />
          <Button
            title="开启屏幕共享"
            onPress={() => {
              NERTC.startScreenCapture({
                maxProfile: 2,
                frameRate: 15,
                minFrameRate: 0,
                bitrate: 0,
                minBitrate: 0,
                contentPrefer: 0,
              });
            }}
          />
          <Button
            title="关闭屏幕共享"
            onPress={() => {
              NERTC.stopScreenCapture();
            }}
          />
          <Button
            title="远端视频配置"
            onPress={() => {
              openRemoteConfig(true);
            }}
          />
          <Button
            title="开启音频"
            onPress={() => {
              NERTC.enableLocalAudio(true);
            }}
          />
          <Button
            title="关闭音频"
            onPress={() => {
              NERTC.enableLocalAudio(false);
            }}
          />
          <Button
            title="muteAudio"
            onPress={() => {
              NERTC.muteLocalAudio(true);
            }}
          />
          <Button
            title="unmuteAudio"
            onPress={() => {
              NERTC.muteLocalAudio(false);
            }}
          />
          <Button
            title="muteVideo"
            onPress={() => {
              NERTC.muteLocalVideo(true);
            }}
          />
          <Button
            title="unmuteVideo"
            onPress={() => {
              NERTC.muteLocalVideo(false);
            }}
          />
          <Button
            title="音量回调"
            onPress={() => {
              setVolumeIndication(true);
            }}
          />
          <Button
            title="开启扬声器"
            onPress={() => {
              NERTC.setLoudspeakerMode(true);
            }}
          />
          <Button
            title="关闭扬声器"
            onPress={() => {
              NERTC.setLoudspeakerMode(false);
            }}
          />
          <Button
            title="退出"
            onPress={() => {
              NERTC.leaveChannel(true);
              navigation.goBack();
            }}
          />
          <Button
            title="打开回调日志"
            onPress={() => {
              showLog();
            }}
          />
        </View>
        {remoteConfig && (
          <View style={styles.configer}>
            <View style={styles.line}>
              <View style={styles.label}>
                <Text>远程1 uid</Text>
              </View>
              <TextInput
                style={styles.input}
                value={remote1Uid}
                keyboardType="number-pad"
                returnKeyType="done"
                onChangeText={setRemote1Uid}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="主流画布"
                onPress={() => {
                  NERTC.setupRemoteVideoCanvas({
                    userID: parseInt(remote1Uid),
                    reactTag: findNodeHandle(remote1RtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="辅流画布"
                onPress={() => {
                  NERTC.setupRemoteSubStreamVideoCanvas({
                    userID: parseInt(remote1Uid),
                    reactTag: findNodeHandle(remote1SubRtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="开启主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: true,
                    userID: parseInt(remote1Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="开启辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: true,
                    userID: parseInt(remote1Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="关闭主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: false,
                    userID: parseInt(remote1Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="关闭辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: false,
                    userID: parseInt(remote1Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <View style={styles.line}>
              <View style={styles.label}>
                <Text>远程2 uid</Text>
              </View>
              <TextInput
                style={styles.input}
                value={remote2Uid}
                keyboardType="number-pad"
                returnKeyType="done"
                onChangeText={setRemote2Uid}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="主流画布"
                onPress={() => {
                  NERTC.setupRemoteVideoCanvas({
                    userID: parseInt(remote2Uid),
                    reactTag: findNodeHandle(remote2RtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="辅流画布"
                onPress={() => {
                  NERTC.setupRemoteSubStreamVideoCanvas({
                    userID: parseInt(remote2Uid),
                    reactTag: findNodeHandle(remote2SubRtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="开启主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: true,
                    userID: parseInt(remote2Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="开启辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: true,
                    userID: parseInt(remote2Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="关闭主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: false,
                    userID: parseInt(remote2Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="关闭辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: false,
                    userID: parseInt(remote2Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <View style={styles.line}>
              <View style={styles.label}>
                <Text>远程3 uid</Text>
              </View>
              <TextInput
                style={styles.input}
                value={remote3Uid}
                keyboardType="number-pad"
                returnKeyType="done"
                onChangeText={setRemote3Uid}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="主流画布"
                onPress={() => {
                  NERTC.setupRemoteVideoCanvas({
                    userID: parseInt(remote3Uid),
                    reactTag: findNodeHandle(remote3RtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="辅流画布"
                onPress={() => {
                  NERTC.setupRemoteSubStreamVideoCanvas({
                    userID: parseInt(remote3Uid),
                    reactTag: findNodeHandle(remote3SubRtcRef.current),
                    renderMode: 0,
                    mirrorMode: 0,
                    isMediaOverlay: false,
                  });
                }}
              />
              <Button
                title="开启主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: true,
                    userID: parseInt(remote3Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="开启辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: true,
                    userID: parseInt(remote3Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <View style={styles.line}>
              <Button
                title="关闭主流"
                onPress={() => {
                  NERTC.subscribeRemoteVideo({
                    subscribe: false,
                    userID: parseInt(remote3Uid),
                    streamType: 0,
                  });
                }}
              />
              <Button
                title="关闭辅流"
                onPress={() => {
                  NERTC.subscribeRemoteSubStreamVideo({
                    subscribe: false,
                    userID: parseInt(remote3Uid),
                    streamType: 0,
                  });
                }}
              />
            </View>
            <Button
              title="关闭"
              onPress={() => {
                openRemoteConfig(false);
              }}
            />
          </View>
        )}
        {volumeIndication && (
          <View style={styles.configer}>
            <View style={styles.line}>
              <View style={styles.label}>
                <Text>回调间隔</Text>
              </View>
              <TextInput
                style={styles.input}
                value={volumeIndicationTime}
                keyboardType="number-pad"
                returnKeyType="done"
                onChangeText={setVolumeIndicationTime}
              />
            </View>
            <Button
              title="开启回调"
              onPress={() => {
                NERTC.enableAudioVolumeIndication({
                  enable: true,
                  interval: parseInt(volumeIndicationTime), //回调间隔,时间ms
                  enableVad: true,
                });
              }}
            />
            <Button
              title="关闭回调"
              onPress={() => {
                NERTC.enableAudioVolumeIndication({
                  enable: false,
                  interval: parseInt(volumeIndicationTime), //回调间隔,时间ms
                  enableVad: true,
                });
              }}
            />
            <Button
              title="关闭"
              onPress={() => {
                setVolumeIndication(false);
              }}
            />
          </View>
        )}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
  },
  channel: {
    flex: 1,
    alignItems: 'center',
  },
  videoCanvas: {
    height: 100,
    width: 100,
    backgroundColor: '#000',
  },
  line: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  controller: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  input: {
    height: 40,
    width: 100,
    margin: 12,
    borderWidth: 1,
    padding: 10,
  },
  label: {
    width: 80,
  },
  configer: {
    position: 'absolute',
    backgroundColor: '#fff',
    bottom: 0,
    padding: 20,
    alignItems: 'center',
  },
});
