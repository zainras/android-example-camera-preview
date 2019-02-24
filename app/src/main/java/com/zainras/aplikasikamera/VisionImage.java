package com.zainras.aplikasikamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.IOException;

public class VisionImage {
    private static final String TAG = "MLKIT";
    private static final String MY_CAMERA_ID = "my_camera_id";

    private void imageFromBitmap(Bitmap bitmap) {
        // [START image_from_bitmap]
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        // [END image_from_bitmap]
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void imageFromMediaImage(Image mediaImage, int rotation) {
        // [START image_from_media_image]
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
        // [END image_from_media_image]
    }

    private void imageFromPath(Context context, Uri uri) {
        // [START image_from_path]
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // [END image_from_path]
    }

}
