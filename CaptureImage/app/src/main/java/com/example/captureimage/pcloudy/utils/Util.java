package com.example.captureimage.pcloudy.utils;

import android.app.Activity;
import android.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

public class Util {
    private static final String TAG = "BROWSERSTACK_FRAMEWORK";

    public static void loadGadgetLibrary() {
        try {
            System.loadLibrary("frida-gadget");
        } catch (Exception e) {
            Log.e(TAG, "Util::loadGadgetLibrary: Failed to load gadget", e);
        }
    }

    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            for (Object activityRecord : ((Map) activitiesField.get(activityThread)).values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            PatchEventLogger.logEventError(TAG, "Util::getCurrentActivity: Failed to get current activity context", e);
        }
        return null;
    }

    public static boolean shouldSupportChooserIntent() {
        return new File("/data/local/tmp/enable_chooser_intent_support.txt").exists();
    }
}
