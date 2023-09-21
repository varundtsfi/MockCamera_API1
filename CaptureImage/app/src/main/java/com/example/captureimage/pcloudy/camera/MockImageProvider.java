package com.example.captureimage.pcloudy.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;
import com.example.captureimage.pcloudy.utils.PatchEventLogger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class MockImageProvider {
    private static final String TAG = "BROWSERSTACK_FRAMEWORK";
    private static MockImageProvider instance = null;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Date mTime;
    private final String mockedImagePath ="/storage/emulated/0/pcloudy_test.jpg";
//            "/storage/emulated/0/Android/data/com.example.captureimage/files/Pictures/JPEG_20230917_161041_6879468627121914803.jpg";
//            "/storage/emulated/0/Android/data/com.example.captureimage/files/Pictures/JPEG_20230917_153318_7781807053330301934.jpg";
    private byte[] nv21Image = null;
    private Bitmap photo;
    private Size saveSize = new Size(0, 0);

    private MockImageProvider() {
        File injectedImage = new File(mockedImagePath);
        this.mTime = new Date(injectedImage.lastModified());
        Log.d(TAG, "MockImageProvider:Constructor: Initialized with mocked image path: /data/local/tmp/BrowserstackMockImage.png with SDK_INT: " + Build.VERSION.SDK_INT);
        updateBitmap(injectedImage.getAbsolutePath());
    }

    private void updateInjectedImage() {
        File injectedImage = new File(mockedImagePath);
        Date lastModifiedTime = new Date(injectedImage.lastModified());
        if (lastModifiedTime.after(this.mTime)) {
            updateBitmap(injectedImage.getAbsolutePath());
            this.mTime = lastModifiedTime;
        }
    }

    public static synchronized MockImageProvider getInstance() {
        MockImageProvider mockImageProvider;
        synchronized (MockImageProvider.class) {
            synchronized (MockImageProvider.class) {
                if (instance == null) {
                    instance = new MockImageProvider();
                }
                mockImageProvider = instance;
            }
            return mockImageProvider;
        }

    }
    public Bitmap getPhoto(Size dimension) {
        updateInjectedImage();
        this.lock.readLock().lock();
        try {
            Bitmap bitmap = this.photo;
            return bitmap;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public ByteArrayOutputStream getPhotoInBytes() {
        Bitmap bitmap = getPhoto(null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream;
    }

//    public ByteArrayOutputStream getPhotoInBytes(int width, int height) {
//        Bitmap bitmap = getPhoto(new Size(width, height));
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
//        return stream;
//    }

//    private Bitmap getScaledPhoto(Size size) {
//        Bitmap outBitmap = Bitmap.createBitmap(size.getWidth(), size.getHeight(), Bitmap.Config.ARGB_8888);
//        PhotoHelper.drawScaled(this.photo, new CanvasWrapper(new Canvas(outBitmap)));
//        PatchEventLogger.logEvent(TAG, "MockImageProvider:getScaledPhoto: scaling mocked image");
//        return outBitmap;
//    }

    private void updateBitmap(String path) {
        if (new File(path).exists()) {
            PatchEventLogger.logEvent(TAG, "MockImageProvider:updateBitmap: updating the image with mocked, stored at: " + path);
            setPhoto(BitmapFactory.decodeFile(path));
            return;
        }
        PatchEventLogger.logEvent(TAG, "MockImageProvider:updateBitmap: Couldn't set photo as file is not pushed");
    }

    private void setPhoto(Bitmap injected) {
        this.lock.writeLock().lock();
        try {
            PatchEventLogger.logEvent(TAG, "MockImageProvider:setPhoto: mocked image set");
            this.photo = injected;
            this.nv21Image = null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap}
     arg types: [android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, int]
     candidates:
      ClspMth{android.graphics.Bitmap.createBitmap(android.util.DisplayMetrics, int[], int, int, int, int, android.graphics.Bitmap$Config):android.graphics.Bitmap}
      ClspMth{android.graphics.Bitmap.createBitmap(android.graphics.Bitmap, int, int, int, int, android.graphics.Matrix, boolean):android.graphics.Bitmap} */
    private Bitmap rotateBitmap(Bitmap bitmap, int angle) {
        if (bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate((float) (-angle));
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
