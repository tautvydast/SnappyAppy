package com.example.tautvydas.snappyappy;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Tautvydas on 2016-10-19.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraView(Context context, Camera camera){
        super(context);

        mCamera = camera;
        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

        // ENABLE AUTOFOCUS
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean arg0, Camera arg1) {
                    // TODO Auto-generated method stub
                    //buttonTakePicture.setEnabled(true);
                };
                Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {
                        // TODO Auto-generated method stub
                    };
                };
            });


        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if(mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            mCamera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        mCamera.release();
    }
}