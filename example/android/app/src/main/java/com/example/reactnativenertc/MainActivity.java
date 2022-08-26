package com.example.reactnativenertc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.reactnativenertc.utils.SystemPermissionUtils;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;

import java.util.List;

public class MainActivity extends ReactActivity {

  private static final String TAG = "MainActivity";
  private static final int PERMISSION_REQUEST_CODE = 100;
  private ScreenShareServiceConnection mServiceConnection;
  private NERtcScreenShareService mScreenService;

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "main";
  }

  /**
   * Returns the instance of the {@link ReactActivityDelegate}. There the RootView is created and
   * you can specify the rendered you wish to use (Fabric or the older renderer).
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new MainActivityDelegate(this, getMainComponentName());
  }

  public static class MainActivityDelegate extends ReactActivityDelegate {
    public MainActivityDelegate(ReactActivity activity, String mainComponentName) {
      super(activity, mainComponentName);
    }

    @Override
    protected ReactRootView createRootView() {
      ReactRootView reactRootView = new ReactRootView(getContext());
      // If you opted-in for the New Architecture, we enable the Fabric Renderer.
      reactRootView.setIsFabric(BuildConfig.IS_NEW_ARCHITECTURE_ENABLED);
      return reactRootView;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestPermissionsIfNeeded();
    bindScreenService();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindScreenService();
  }

  private void requestPermissionsIfNeeded() {
    final List<String> missedPermissions = SystemPermissionUtils.checkPermission(this);
    if (missedPermissions.size() > 0) {
      ActivityCompat.requestPermissions(this, missedPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
    }
  }


  private void bindScreenService() {
    Intent intent = new Intent();
    intent.setClass(this, NERtcScreenShareService.class);
    mServiceConnection = new ScreenShareServiceConnection();
    bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
  }

  private void unbindScreenService() {
    if (mServiceConnection != null) {
      unbindService(mServiceConnection);
    }
  }

  private class ScreenShareServiceConnection implements ServiceConnection {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {

      if (service instanceof NERtcScreenShareService.ScreenShareBinder) {
        mScreenService = ((NERtcScreenShareService.ScreenShareBinder) service).getService();
        Log.i(TAG, "onServiceConnect");
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      mScreenService = null;
    }
  }

}
