package com.zainras.aplikasikamera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public MyCameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // install survaceholder
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // the survace has been created
        try {
            Camera.Parameters param;
            param = mCamera.getParameters();

            Camera.Size bestSize = null;
            List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
            Log.d("HASILE", sizeList.toString());
            bestSize = sizeList.get(0);
            for(int i = 1; i < sizeList.size(); i++){
                if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                    bestSize = sizeList.get(i);
                }
            }

            List<Integer> supportedPreviewFormats = param.getSupportedPreviewFormats();
            Iterator<Integer> supportedPreviewFormatsIterator = supportedPreviewFormats.iterator();
            while(supportedPreviewFormatsIterator.hasNext()){
                Integer previewFormat =supportedPreviewFormatsIterator.next();
                if (previewFormat == ImageFormat.YV12) {
                    param.setPreviewFormat(previewFormat);
                }
            }

            param.setPreviewSize(bestSize.width, bestSize.height);

            param.setPictureSize(bestSize.width, bestSize.height);

            mCamera.setParameters(param);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error setting camera preivew");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // prosess handle camera
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.

        if (mHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.startPreview();
        } catch (Exception e) {
            // ignote : stop non exsisten preview
        }

        // set preview on any size

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera prebiew: " + e.getMessage());
        }
    }
}
