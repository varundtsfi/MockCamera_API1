package com.example.captureimage.pcloudy.utils;

import android.util.Log;

public class PatchEventLogger {
    public static void logEvent(String tag, String message) {
        Log.d(tag, message);
    }

    public static void logEventError(String tag, String message, Throwable error) {
        Log.e(tag, message, error);
    }
}
