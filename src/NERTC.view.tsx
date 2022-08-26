import React, {Component} from 'react';
import {
    requireNativeComponent,
    ViewProps,
    Platform, 
    View,
} from 'react-native';

const NERTcVideoViewManager = Platform.select<any>({
    ios: View,  // UIView
    android: requireNativeComponent('RCTNERtcVideoView')
});

export interface NERtcViewProps extends ViewProps {
  isMediaOverlay?: boolean
}

export class NERtcView extends Component<NERtcViewProps, {}> {
  render() {
      return (
          <NERTcVideoViewManager {...this.props}/>
      )
  }
}