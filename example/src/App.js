import * as React from 'react';

import { StyleSheet, View, Text, Button, ScrollView } from 'react-native';
import NERTC from 'react-native-nertc';

import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import ChannelScreen from './ChannelScreen';
import HomeScreen from './HomeScreen';
import VideoConfiger from './VideoConfiger';

const Stack = createNativeStackNavigator();

let eventLogStack = [];
const ERRSTACK_MAXSIZE = 500;

const eventList = [
  'onError',
  'onWarning',
  'onJoinChannel',
  'onRejoinChannel',
  'onLeaveChannel',
  'onUserJoin',
  'onUserLeave',
  'onConnectionStateChanged',
  'onDisconnect',
  'onConnectionTypeChanged',
  'onUserVideoStart',
  'onUserVideoStop',
  'onUserAudioStart',
  'onUserAudioStop',
  'onUserSubStreamVideoStart',
  'onUserSubStreamVideoStop',
  'onLocalAudioStats',
  'onLocalVideoStats',
  'onLocalAudioVolumeIndication',
  'onRemoteAudioVolumeIndication',
  'onRemoteAudioStats',
  'onRemoteVideoStats',
  'onNetworkQuality',
  'onAudioDeviceChanged',
  'onAudioDeviceStateChange',
];

export default function App() {
  const scrollViewRef = React.useRef(null);
  const navigationRef = React.useRef(null);
  const [localLogs, setLogs] = React.useState(eventLogStack);
  const [showLog, onShowLog] = React.useState(false);
  const [showConfig, onShowConfig] = React.useState(false);

  return (
    <NavigationContainer ref={navigationRef}>
      <Stack.Navigator>
        <Stack.Screen name="NERTC">
          {(props) => (
            <HomeScreen
              {...props}
              showLog={() => {
                onShowLog(true);
              }}
              openVideoConfig={() => {
                onShowConfig(true);
              }}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Channel">
          {(props) => (
            <ChannelScreen
              {...props}
              showLog={() => {
                onShowLog(true);
              }}
              openVideoConfig={() => {
                onShowConfig(true);
              }}
            />
          )}
        </Stack.Screen>
      </Stack.Navigator>
      {showLog && (
        <View style={styles.logWrapper}>
          <View style={styles.line}>
            <Button
              title="注册回调"
              onPress={() => {
                eventList.forEach((event) => {
                  NERTC.addListener(event, (res) => {
                    eventLogStack.push(`NERTC ${event} emitted`);
                    if (eventLogStack.length >= ERRSTACK_MAXSIZE) {
                      eventLogStack.shift();
                    }
                    eventLogStack = eventLogStack.slice();
                    setLogs([...eventLogStack]);
                    if (event === 'onDisconnect') {
                      navigationRef.current?.navigate('NERTC');
                    }
                  });
                });
              }}
            />
            <Button
              title="移除回调"
              onPress={() => {
                NERTC.removeAllListeners();
              }}
            />
            <Button
              style={styles.logCloser}
              title="清除"
              onPress={() => {
                eventLogStack = [];
                setLogs(eventLogStack);
              }}
            />
            <Button
              style={styles.logCloser}
              title="关闭"
              onPress={() => {
                onShowLog(false);
              }}
            />
          </View>
          <ScrollView
            ref={scrollViewRef}
            style={{ height: 280, paddingLeft: 10 }}
            onContentSizeChange={() =>
              scrollViewRef.current.scrollToEnd({ animated: true })
            }
          >
            {localLogs.map((eventLog) => (
              <Text>{eventLog}</Text>
            ))}
          </ScrollView>
        </View>
      )}
      {showConfig && (
        <VideoConfiger
          closeConfig={() => {
            onShowConfig(false);
          }}
        />
      )}
    </NavigationContainer>
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
    width: 100,
  },
  channel: {
    flex: 1,
    alignItems: 'center',
  },
  controller: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  logWrapper: {
    position: 'absolute',
    bottom: 20,
    left: 0,
    width: '100%',
    height: 300,
    backgroundColor: '#fff',
  },
});
