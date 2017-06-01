package com.zealous.utils;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.zealous.R;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * @author null-pointer
 */
@SuppressWarnings("WeakerAccess")
public class Config {

    public static final String APP_PREFS = "prefs";
    private static final String TAG = Config.class.getSimpleName();

    private static final String ENV_PROD = "prod";
    private static final String ENV_DEV = "dev";
    public static final String ENV = getEnvironment();
    private static final String logMessage = "calling getApplication when init has not be called";
    private static final String detailMessage = "application is null. Did you forget to call Config.init()?";
    private static String APP_NAME = "Zealous";
    private static Application application;

    private static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static void init(Application pairApp) {
        Config.application = pairApp;
        setUpDirs();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void setUpDirs() {
        if (isExternalStorageAvailable()) {
            //no need to worry calling this several times
            //if the file is already a directory it will fail silently
            getAppBinFilesBaseDir().mkdirs();
            getTempDir().mkdirs();
        } else {
            PLog.w(TAG, "This is strange! no sdCard available on this device");
        }

    }


    public static Context getApplicationContext() {
        if (application == null) {
            warnAndThrow(logMessage, detailMessage);
        }
        return application.getApplicationContext();
    }

    public static Application getApplication() {
        if (application == null) {
            warnAndThrow(logMessage, detailMessage);
        }
        return Config.application;
    }

    private static void warnAndThrow(String msg, String detailMessage) {
        PLog.w(TAG, msg);
        throw new IllegalStateException(detailMessage);
    }

    private static String getEnvironment() {
        if (isEmulator()) {
            return ENV_DEV;
        } else {
            return ENV_PROD;
        }
    }

    public static boolean isEmulator() {
        return Build.HARDWARE.contains("goldfish")
                || Build.PRODUCT.equals("sdk") // sdk
                || Build.PRODUCT.endsWith("_sdk") // google_sdk
                || Build.PRODUCT.startsWith("sdk_") // sdk_x86
                || Build.FINGERPRINT.contains("generic");
    }

    public static SharedPreferences getApplicationWidePrefs() {
        if (application == null) {
            throw new IllegalStateException("application is null,did you forget to call init(Context) ?");
        }
        return getPreferences(Config.APP_PREFS);
    }

    public static File getAppBinFilesBaseDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(APP_NAME), getApplicationContext()
                .getString(R.string.folder_name_files));

        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                PLog.f(TAG, "failed to create files dir");
            }
        }
        return file;
    }

    public static File getTempDir() {
        File file = new File(
                getExternalStoragePublicDirectory(APP_NAME), "TMP");
        if (!file.isDirectory()) {
            if (!file.mkdirs()) {
                PLog.f(TAG, "failed to create tmp dir");
            }
        }
        return file;
    }

    public static SharedPreferences getPreferences(String s) {
        return getApplicationContext().getSharedPreferences(s, Context.MODE_PRIVATE);
    }

    public static File getBackupDir() {
        return new File(getAppBinFilesBaseDir(), "backup");
    }

    public static void enableComponent(Class clazz) {
        PLog.d(TAG, "enabling " + clazz.getSimpleName());
        ComponentName receiver = new ComponentName(Config.getApplication(), clazz);

        PackageManager pm = Config.getApplication().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

//    public static void disableComponent(Class clazz) {
//        PLog.d(TAG, "disabling " + clazz.getSimpleName());
//        ComponentName receiver = new ComponentName(Config.getApplication(), clazz);
//
//        PackageManager pm = Config.getApplication().getPackageManager();
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                PackageManager.DONT_KILL_APP);
//    }
}
