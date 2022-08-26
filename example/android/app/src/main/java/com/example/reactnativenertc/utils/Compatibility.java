/*
 * *
 *  * Created by zhujinbo on 19-3-12 下午3:04
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 19-2-15 上午11:22
 *
 */

package com.example.reactnativenertc.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Field;

/**
 * Android版本
 */
public class Compatibility {


    private static boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isTabletScreen(Context ctxt) {
        boolean isTablet = false;
        if (!isCompatible(4)) {
            return false;
        }
        Configuration cfg = ctxt.getResources().getConfiguration();
        int screenLayoutVal = 0;
        try {
            Field f = Configuration.class.getDeclaredField("screenLayout");
            screenLayoutVal = (Integer) f.get(cfg);
        } catch (Exception e) {
            return false;
        }
        int screenLayout = (screenLayoutVal & 0xF);
        if (screenLayout == 0x3 || screenLayout == 0x4) {
            isTablet = true;
        }
        return isTablet;
    }

    public static boolean runningOnEmulator() {

        if (!TextUtils.isEmpty(Build.MODEL) &&
                Build.MODEL.toLowerCase().contains("sdk")) {
            return true;
        }

        if (!TextUtils.isEmpty(Build.MANUFACTURER) &&
                Build.MANUFACTURER.toLowerCase().contains("unknown")) {
            return true;
        }

        if (!TextUtils.isEmpty(Build.DEVICE) &&
                Build.DEVICE.toLowerCase().contains("generic")) {
            return true;
        }

        return false;

    }


    public static boolean runningOnGingerBreadOrHigher() {
        // November 2010: Android 2.3, API Level 9.
        return isCompatible(Build.VERSION_CODES.GINGERBREAD);
    }

    public static boolean runningOnJellyBeanOrHigher() {
        // June 2012: Android 4.1, API Level 16.
        return isCompatible(Build.VERSION_CODES.JELLY_BEAN);
    }

    public static boolean runningOnJellyBeanMR1OrHigher() {
        // November 2012: Android 4.2, API Level 17.
        return isCompatible(Build.VERSION_CODES.JELLY_BEAN_MR1);
    }

    public static boolean runningOnJellyBeanMR2OrHigher() {
        // July 24, 2013: Android 4.3, API Level 18.
        return isCompatible(Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

    public static boolean runningOnKitkatOrHigher() {
        // October 2013: Android 4.4, API Level 19.
        return isCompatible(Build.VERSION_CODES.KITKAT);
    }

    public static boolean runningOnKitkatWatchOrHigher() {
        // June 2014: Android 4.4W, API Level 20.
        return isCompatible(Build.VERSION_CODES.KITKAT_WATCH);
    }

    public static boolean runningOnLollipopOrHigher() {
        // November 2014: Android 5.0, API Level 21.
        return isCompatible(Build.VERSION_CODES.LOLLIPOP);
    }

    public static boolean runningOnLollipopMR1OrHigher() {
        // March 2015: Android 5.1.1, API Level 22.
        return isCompatible(Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public static boolean runningOnMarshmallowOrHigher() {
        // October 2015: Android 6.0, API Level 23.
        return isCompatible(Build.VERSION_CODES.M);
    }

    public static boolean runningOnNougatOrHigher() {
        // August 2016: Android 7.0, API Level 24.
        return isCompatible(Build.VERSION_CODES.N);
    }

    public static boolean runningOnNougatMR1OrHigher() {
        // December 2016: Android 7.1, API Level 25.
        return isCompatible(Build.VERSION_CODES.N_MR1);
    }

    public static boolean runningOnOreoOrHigher() {
        // August 2017: Android 8.0, API Level 26.
        return isCompatible(Build.VERSION_CODES.O);
    }

    public static boolean runningOnQOrHigher() {
        // 2019 : Android 10.0, API Level 29.
        return isCompatible(Build.VERSION_CODES.Q);
    }

    public static boolean runningOnSnowConeOrHigher() {
        return isCompatible(Build.VERSION_CODES.S);
    }

    public static boolean runningOnOreoMR1OrHigher() {
        return isCompatible(Build.VERSION_CODES.O_MR1);
    }

    public static boolean runningOnPieOrHigher() {
        return isCompatible(Build.VERSION_CODES.P);
    }
}
