/*
 * *
 *  * Created by zhujinbo on 19-3-12 下午3:04
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 19-2-15 上午11:22
 *
 */

package com.example.reactnativenertc.utils;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_SETTINGS;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuqijun on 12/25/15.
 * <p>
 * <p>
 * NRTC 运行需要的权限
 * <p>
 * 注意:
 * 1, Ringer 可能需要 android.permission.VIBRATE 权限
 * 2, Android M 开始, MODE_IN_CALL 需要新的 android.Manifest.permission.MODIFY_PHONE_STATE 权限
 * 3, Android S 开始, 蓝牙相关的需要新的 android.Manifest.permission.BLUETOOTH_CONNECT 权限
 */
public class SystemPermissionUtils {

    private static final ArrayList<String> PERMISSIONS = new ArrayList<>(16);
    static {
        PERMISSIONS.add(RECORD_AUDIO);
        PERMISSIONS.add(CAMERA);
        PERMISSIONS.add(INTERNET);
        PERMISSIONS.add(ACCESS_NETWORK_STATE);
        PERMISSIONS.add(ACCESS_WIFI_STATE);
        PERMISSIONS.add(WRITE_EXTERNAL_STORAGE);
        PERMISSIONS.add(WAKE_LOCK);
        PERMISSIONS.add(BLUETOOTH);
        PERMISSIONS.add(MODIFY_AUDIO_SETTINGS);
        PERMISSIONS.add(READ_PHONE_STATE);
        if (Compatibility.runningOnSnowConeOrHigher()) {
            PERMISSIONS.add(BLUETOOTH_CONNECT);
        }
    }

    public static boolean hasAudioOutputFeature(Context context) {
        return !Compatibility.runningOnLollipopOrHigher() ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT);
    }

    public static boolean hasMicrophoneFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public static boolean checkAudioPermission(Context context) {
        return checkPermission(context, RECORD_AUDIO);
    }

    public static boolean checkCameraPermission(Context context) {
        return checkPermission(context, CAMERA);
    }

    public static boolean checkInternetPermission(Context context) {
        return checkPermission(context, INTERNET);
    }

    public static boolean checkAccessNetworkStatePermission(Context context) {
        return checkPermission(context, ACCESS_NETWORK_STATE);
    }

    public static boolean checkAccessWifiStatePermission(Context context) {
        return checkPermission(context, ACCESS_WIFI_STATE);
    }

    public static boolean checkWriteExternalStoragePermission(Context context) {
        return checkPermission(context, WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkWakeLockPermission(Context context) {
        return checkPermission(context, WAKE_LOCK);
    }

    public static boolean checkBluetoothPermission(Context context) {
        return checkPermission(context, BLUETOOTH);
    }

    public static boolean checkModifyAudioSettingsPermission(Context context) {
        return checkPermission(context, MODIFY_AUDIO_SETTINGS);
    }

    public static boolean checkWriteSettingPermission(Context context) {
        return checkPermission(context, WRITE_SETTINGS);
    }

    public static boolean checkBluetoothConnectPermission(Context context) {
        return checkPermission(context, BLUETOOTH_CONNECT);
    }

    public static boolean checkReadPhoneState(Context context) {
        return checkPermission(context, READ_PHONE_STATE);
    }

    private static boolean checkPermission(Context context, String permission) {
        return context.checkPermission(permission,
                android.os.Process.myPid(),
                android.os.Process.myUid()) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static List<String> checkPermission(Context context) {
        List<String> list = new ArrayList<>();
        if (context == null) {
            return list;
        }
        for (String permission : PERMISSIONS) {
            if (!checkPermission(context, permission)) {
                list.add(permission);
            }
        }
        return list;
    }

}
