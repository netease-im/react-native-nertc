import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  TextInput,
  Button,
  findNodeHandle,
  ScrollView,
} from 'react-native';
import NERTC, { NERtcView } from 'react-native-nertc';
import CONFIG from './config';

export default function HomeScreen({ navigation, showLog, openVideoConfig }) {
  const [channel, onChangeChannel] = React.useState('225588');
  const [startPreview, setStartPreview] = React.useState(false);
  const [uid, onChangeUid] = React.useState('1234');
  const rtcRef = React.useRef(null);

  React.useEffect(() => {}, []);

  return (
    <ScrollView style={{ flex: 1 }}>
      <View style={styles.container}>
        <Button
          title="初始化引擎"
          style={styles.button}
          onPress={() => {
            NERTC.setupEngineWithContext(CONFIG.setupOptions).then((result) => {
              console.log('setupEngineWithContext: ', result);
            });
          }}
        />
        <Button
          title="销毁实例"
          onPress={() => {
            NERTC.destroyEngine();
          }}
        />
        <NERtcView style={styles.localVideo} ref={rtcRef} />
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>房间号：</Text>
          </View>
          <TextInput
            style={styles.input}
            returnKeyType="done"
            value={channel}
            onChangeText={onChangeChannel}
            placeholder="房间号"
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>uid：</Text>
          </View>
          <TextInput
            style={styles.input}
            returnKeyType="done"
            value={uid}
            onChangeText={onChangeUid}
            placeholder="uid"
          />
        </View>
        <View style={styles.controller}>
          <Button
            title="加入房间"
            onPress={() => {
              NERTC.enableLocalAudio(true).then((result) => {
                NERTC.joinChannel({
                  token: CONFIG.token,
                  channelName: channel,
                  myUid: parseInt(uid),
                }).then((result) => {
                  if (result == 0) {
                    NERTC.setStatsObserver(true);
                    navigation.navigate('Channel');
                  }
                });
              });
            }}
          />
          <Button
            title="设置视频画布"
            onPress={() => {
              NERTC.setupLocalVideoCanvas({
                reactTag: findNodeHandle(rtcRef.current),
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
            title={startPreview ? '关闭视频预览' : '开启视频预览'}
            onPress={() => {
              startPreview ? NERTC.stopPreview() : NERTC.startPreview();
              setStartPreview(!startPreview);
            }}
          />
          <Button
            title="切换摄像头"
            onPress={() => {
              NERTC.switchCamera();
            }}
          />
          <Button
            title="打开外部视频输入"
            onPress={() => {
              NERTC.setExternalVideoSource({
                enable: true,
                isScreen: true,
              }).then((res) => {
                console.log('setExternalVideoSource: ', res);
              });
            }}
          />
          <Button
            title="打开回调日志"
            onPress={() => {
              showLog();
            }}
          />
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
  },
  input: {
    height: 40,
    width: 100,
    margin: 12,
    borderWidth: 1,
    padding: 10,
  },
  line: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    width: 80,
  },
  localVideo: {
    height: 300,
    width: 300,
    backgroundColor: '#000',
  },
  controller: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
});
