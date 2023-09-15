package com.example.captureimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.captureimage.pcloudy.camera.MockActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MainActivityNew extends AppCompatActivity {
    private File photoFile;
    private final int CAPTURE_IMAGE_REQUEST = 1;

    private String mCurrentPhotoPath;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.button_capture)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View it) {
                captureImage();
            }
        });
    }

    private final void captureImage() {
        if (ContextCompat.checkSelfPermission((Context) this, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions((Activity) this, new String[]{"android.permission.CAMERA",
                    "android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
                try {
                    this.photoFile = createImageFile();
                    if (photoFile != null) {

                        Uri photoURI = FileProvider.getUriForFile
                                (this, "com.example.captureimage.fileprovider", photoFile);


                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        MockActivity.captureIntent(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception var3) {
                    displayMessage(this, String.valueOf(var3.getMessage()));
                }
            } else {
                displayMessage(this, "Null");
            }
        }

    }

    private final File createImageFile() throws IOException {
        String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        //Intrinsics.checkExpressionValueIsNotNull(image, "image");
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private final void displayMessage(Context context, String message) {
        Toast.makeText(context, (CharSequence) message, Toast.LENGTH_LONG).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        MockActivity.onMockActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.CAPTURE_IMAGE_REQUEST &&
                resultCode == Activity.RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            ((ImageView) findViewById(R.id.imageView)).setImageBitmap(myBitmap);
        } else {
            displayMessage(this, "Request cancelled or something went wrong.");
        }
    }
}