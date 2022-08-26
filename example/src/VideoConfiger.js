import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  ScrollView,
  TextInput,
} from 'react-native';
import NERTC from 'react-native-nertc';

export default function VideoConfinger({ closeConfig }) {
  const [maxProfile, setMaxProfile] = React.useState('3');
  const [frameRate, setFrameRate] = React.useState('15');
  const [minFrameRate, setMinFrameRate] = React.useState('0');
  const [bitrate, setBitrate] = React.useState('0');
  const [minBitrate, setMinBiterate] = React.useState('0');
  const [width, setWidth] = React.useState('0');
  const [height, setHeight] = React.useState('0');
  const [cropMode, setCropMode] = React.useState('0');
  const [degradationPreference, setDegradationPreference] = React.useState('0');
  const [mirrorMode, setMirrorMode] = React.useState('0');
  const [orientationMode, setOrientationMode] = React.useState('0');

  return (
    <View style={styles.VideoConfiger}>
      <View style={styles.line}>
        <Button
          title="确认"
          onPress={() => {
            let localVideoConfig = {
              maxProfile: parseInt(maxProfile),
              frameRate: parseInt(frameRate),
              minFrameRate: parseInt(minFrameRate),
              bitrate: parseInt(bitrate),
              minBitrate: parseInt(minBitrate),
              width: parseInt(width),
              height: parseInt(height),
              cropMode: parseInt(cropMode),
              degradationPreference: parseInt(degradationPreference),
              mirrorMode: parseInt(mirrorMode),
              orientationMode: parseInt(orientationMode),
            };
            NERTC.setLocalVideoConfig(localVideoConfig).then((res) => {
              console.log('setLocalVideoConfig: ', localVideoConfig, res);
              closeConfig();
            });
          }}
        />
        <Button
          title="关闭"
          onPress={() => {
            closeConfig();
          }}
        />
      </View>
      <ScrollView style={{ height: 280, paddingLeft: 10, width: '100%' }}>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>maxProfile</Text>
          </View>
          <TextInput
            style={styles.input}
            value={maxProfile}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setMaxProfile}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>frameRate</Text>
          </View>
          <TextInput
            style={styles.input}
            value={frameRate}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setFrameRate}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>minFrameRate</Text>
          </View>
          <TextInput
            style={styles.input}
            value={minFrameRate}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setMinFrameRate}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>bitrate</Text>
          </View>
          <TextInput
            style={styles.input}
            value={bitrate}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setBitrate}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>minBiterate</Text>
          </View>
          <TextInput
            style={styles.input}
            value={minBitrate}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setMinBiterate}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>width</Text>
          </View>
          <TextInput
            style={styles.input}
            value={width}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setWidth}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>height</Text>
          </View>
          <TextInput
            style={styles.input}
            value={height}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setHeight}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>cropMode</Text>
          </View>
          <TextInput
            style={styles.input}
            value={cropMode}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setCropMode}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>degradationPreference</Text>
          </View>
          <TextInput
            style={styles.input}
            value={degradationPreference}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setDegradationPreference}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>mirrorMode</Text>
          </View>
          <TextInput
            style={styles.input}
            value={mirrorMode}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setMirrorMode}
          />
        </View>
        <View style={styles.line}>
          <View style={styles.label}>
            <Text>orientationMode</Text>
          </View>
          <TextInput
            style={styles.input}
            value={orientationMode}
            keyboardType="number-pad"
            returnKeyType="done"
            onChangeText={setOrientationMode}
          />
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  VideoConfiger: {
    width: '100%',
    backgroundColor: '#fff',
    position: 'absolute',
    bottom: 0,
    height: 400,
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
    width: 150,
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
