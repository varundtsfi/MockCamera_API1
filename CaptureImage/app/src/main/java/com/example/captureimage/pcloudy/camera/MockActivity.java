package com.example.captureimage.pcloudy.camera;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import com.example.captureimage.pcloudy.utils.PatchEventLogger;
import com.example.captureimage.pcloudy.utils.Util;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockActivity extends Activity {
    private static final String TAG = "BROWSERSTACK_FRAMEWORK";
    private static Set<Integer> chooserCaptureRequests = new HashSet();
    private static Set<Integer> imageCaptureRequests = new HashSet();
    private static Uri outputFileUri = null;
    private static MockImageProvider photoProvider = MockImageProvider.getInstance();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageCaptureRequests = new HashSet();
        photoProvider = MockImageProvider.getInstance();
     //   setContext(getApplication());
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        Log.d(TAG, "MockActivity:startActivityForResult: Starting with intent and requestCode");
        captureIntent(intent, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        Log.d(TAG, "MockActivity:startActivityForResult: Starting with intent, requestCode and bunldeOptions");
        captureIntent(intent, requestCode);
        super.startActivityForResult(intent, requestCode, options);
    }

    public static void captureIntent(Intent intent, int requestCode) {
        Log.d(TAG, "MockActivity:captureIntent: called with intent: " + intent + " requestCode: " + requestCode);
        if (intent == null) {
            return;
        }
        if ("android.media.action.IMAGE_CAPTURE".equals(intent.getAction())) {
            PatchEventLogger.logEvent(TAG, "MockActivity:captureIntent: Identified this intent as IMAGE_CAPTURE");
            imageCaptureRequests.add(Integer.valueOf(requestCode));
            if (intent.hasExtra("output")) {
                outputFileUri = (Uri) intent.getExtras().get("output");
                Log.d(TAG, "MockActivity:captureIntent: Stored intent content uri " + outputFileUri);
            }
        } else if ("android.intent.action.CHOOSER".equals(intent.getAction())) {
            PatchEventLogger.logEvent(TAG, "MockActivity:captureIntent: Identified this intent as android.intent.action.CHOOSER");
            List<Intent> extraIntents = new ArrayList<>();
            try {
                if (intent.getParcelableExtra("android.intent.extra.INTENT") != null) {
                    extraIntents.add((Intent) intent.getParcelableExtra("android.intent.extra.INTENT"));
                }
            } catch (Exception exception) {
                PatchEventLogger.logEventError(TAG, "Failed to add EXTRA_INTENT", exception);
            }
            PatchEventLogger.logEvent(TAG, "Extra initial    intents" + Arrays.toString((Parcelable[]) intent.getExtras().get("android.intent.extra.INITIAL_INTENTS")));
            try {
                if (intent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS") != null) {
                    for (Parcelable parcelable : intent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS")) {
                        extraIntents.add((Intent) parcelable);
                    }
                }
            } catch (Exception exception2) {
                PatchEventLogger.logEventError(TAG, "Failed to add EXTRA_INITIAL_INTENT", exception2);
            }
            for (Intent currentIntent : extraIntents) {
                PatchEventLogger.logEvent(TAG, "MockActivity:captureIntent: Extra CHOOSER Intent: " + currentIntent);
                if ("android.media.action.IMAGE_CAPTURE".equals(currentIntent.getAction())) {
                    PatchEventLogger.logEvent(TAG, "MockActivity:captureIntent: Identified this extra CHOOSER intent as IMAGE_CAPTURE");
                    if (Util.shouldSupportChooserIntent()) {
                        chooserCaptureRequests.add(Integer.valueOf(requestCode));
                        if (currentIntent.hasExtra("output")) {
                            outputFileUri = (Uri) currentIntent.getExtras().get("output");
                            Log.d(TAG, "MockActivity:captureIntent: Stored extra CHOOSER intent content uri " + outputFileUri);
                            return;
                        }
                        return;
                    }
                    return;
                }
            }
        }
    }

    public static void onMockActivityResult(Object object, int requestCode, int resultCode, Intent data) {
        PatchEventLogger.logEvent(TAG, "MockActivity:onMockActivityResult: mocking onActivityResult with data: " + data);
        if (imageCaptureRequests.contains(Integer.valueOf(requestCode))) {
            Log.d(TAG, "MockActivity:onMockActivityResult: Inside Image Capture Requests");
            if (outputFileUri == null) {
                Log.d(TAG, "MockActivity:onMockActivityResult: Calling replaceResult");
                replaceResult(data);
            } else {
                Log.d(TAG, "MockActivity:onMockActivityResult: Calling replaceUriContent");
                replaceUriContent(object);
                outputFileUri = null;
            }
        } else if (chooserCaptureRequests.contains(Integer.valueOf(requestCode))) {
            if (resultCode == -1) {
                Log.d(TAG, "MockActivity:onMockActivityResult: Inside CHOOSER Capture Requests");
                if (outputFileUri == null && data != null && data.getData() == null && data.getParcelableExtra("data") != null) {
                    Log.d(TAG, "MockActivity:onMockActivityResult: Calling replaceResult");
                    replaceResult(data);
                } else if (outputFileUri != null && (data == null || (data.getData() == null && data.getParcelableExtra("data") == null))) {
                    Log.d(TAG, "MockActivity:onMockActivityResult: Calling replaceUriContent");
                    replaceUriContent(object);
                }
            }
            outputFileUri = null;
        }
        imageCaptureRequests.remove(Integer.valueOf(requestCode));
        chooserCaptureRequests.remove(Integer.valueOf(requestCode));
    }

    private static void replaceResult(Intent data) {
        MockImageProvider instance = MockImageProvider.getInstance();
        photoProvider = instance;
        data.putExtra("data", instance.getPhoto(null));
        Log.d(TAG, "MockActivity:replaceResult: successfully replaced image in data intent");
    }

    private static void replaceUriContent(Object object) {
        ContentResolver contentResolver;
        Class<?> superClass = object.getClass().getSuperclass();
        if (superClass == null) {
            Log.d(TAG, "MockActivity:onMockActivityResult: No super class detected for " + object.getClass().getCanonicalName());
            return;
        }
        if ((object instanceof Context) || superClass.getName().endsWith("Activity")) {
            contentResolver = ((Context) object).getContentResolver();
        } else {
            try {
                contentResolver = ((Fragment) object).getContext().getContentResolver();
            } catch (NoClassDefFoundError e) {
                try {
                    contentResolver = ((android.app.Fragment) object).getActivity().getContentResolver();
                } catch (NoClassDefFoundError ex) {
                    Log.e(TAG, "MockActivity:onMockActivityResult: Unsupported fragment " + object.getClass().toString(), ex);
                    return;
                } catch (Exception error) {
                    Log.e(TAG, "MockActivity:onMockActivityResult: unknown exception for" + object.getClass().toString(), error);
                    return;
                }
            } catch (Exception e2) {
                Log.e(TAG, "MockActivity:onMockActivityResult: unknown exception for" + object.getClass().toString(), e2);
                return;
            }
        }
        MockImageProvider instance = MockImageProvider.getInstance();
        photoProvider = instance;
        ByteArrayOutputStream stream = instance.getPhotoInBytes();
        try {
            OutputStream outputStream = contentResolver.openOutputStream(outputFileUri);
            if (outputStream == null) {
                Log.e(TAG, "MockActivity:replaceUriContent: outputStream is null");
                return;
            }
            stream.writeTo(outputStream);
            outputStream.close();
            stream.close();
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "MockActivity:replaceUriContent: Failed to open output stream", e3);
        } catch (IOException e4) {
            Log.e(TAG, "MockActivity:replaceUriContent: Failed IO Operations", e4);
        }
    }
}
