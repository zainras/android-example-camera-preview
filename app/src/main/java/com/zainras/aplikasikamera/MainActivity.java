package com.zainras.aplikasikamera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Camera mCamera;
    private MyCameraPreview mPreview;
    private Context context;
    private ImageView img2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get instance
        mCamera = getCameraInstance();

        // create our preview
        mPreview = new MyCameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        img2 = findViewById(R.id.img_preview_capture2);

        // event listener
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
    }

    // access camera
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attemp to get camera instance
            Log.d("BUKAK", "kamera berhasil");
        } catch (Exception e) {
            // camera is not available
            Log.d("BUKAK", "kamera gagal");
        }
        return c;
    }

    // check camera hardware
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }

    }

    // capture image from camera
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile =  getOutputMediaFile(MEDIA_TYPE_IMAGE);
            Log.d("NULIS", pictureFile.getPath());
            if (pictureFile == null) {
                Log.d(TAG, "Error Creating media file, check storage permission");
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(),bmOptions);

            Matrix matrix = new Matrix();
            matrix.postRotate(90); //  portrait picture
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            Log.d("MOTONG", "width: "+bw+", height: "+bh);

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bw, bh, matrix, true);
            bitmap = cropToSquare(bitmap);

            img2.setImageBitmap(bitmap);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            recognizeText(image);
//
//            try (FileOutputStream out = new FileOutputStream(pictureFile)) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // bmp is your Bitmap instance
//                // PNG is a lossless format, the compression factor (100) is ignored
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    };

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        //newHeight = (int) (newHeight * 0.10); // 10%
        int cropW = (width - height) / 2;
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height - width) / 2;
        cropH = (cropH < 0)? 0: cropH;
        //cropH = 60;
        //Log.d("MOTONG", "width: "+width+", height: "+height+" cropW:" + cropW + ", cropH: "+cropH+", newWidth: "+newWidth+", newHeight:" + newWidth);
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, 40, newWidth, newHeight/2);

        return cropImg;
    }

    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "myCameraApp");
        // This location works best if you want the created images to be shared

        if (! mediaStorageDir.exists()) {
            if (! mediaStorageDir.mkdirs()) {
                Log.d("myCameraApp", "Failed to create directory");
                return null;
            }
        }

        // Create media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            // uncoment to save image on memory
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void recognizeText(FirebaseVisionImage image) {

        // [START get_detector_default]
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        // [END get_detector_default]

        // [START run_detector]
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_text]
                                String hasil = "";
                                Log.d("HASILE", "proses");

                                for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();
                                    Log.d("HASILE", text);
                                    for (FirebaseVisionText.Line line: block.getLines()) {
                                        Log.d("HASILE", line.getText());
                                        hasil = line.getText();
                                        // looping each by line to get element
                                        for (FirebaseVisionText.Element element: line.getElements()) {

                                        }
                                    }
                                }


                                if (!hasil.equals("")) {
                                    Toast.makeText(getApplicationContext(), hasil, Toast.LENGTH_SHORT).show();
                                }

                                // [END get_text]
                                // [END_EXCLUDE]
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }

    public void convertFromFile(View view) {
        File imgFile = new  File("/storage/emulated/0/Pictures/myCameraApp/contotext.png");

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),bmOptions);

        img2.setImageBitmap(bitmap);

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        recognizeText(image);
    }

}
