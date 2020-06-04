package com.fanfan.test;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.util.Log;

public class CameraTestActivity extends Activity
{
    private static final String TAG = "CameraTestActivity";
    private static final int CAMERA_VIDEO_WIDTH  = 640;
    private static final int CAMERA_VIDEO_HEIGHT = 480;
    private byte[]        mPreviewBuf = new byte[CAMERA_VIDEO_WIDTH * CAMERA_VIDEO_HEIGHT * 12 / 8];
    private SurfaceView   mPreview;
    private Camera        mCamDev ;
    private H264HwEncoder mH264Enc;
    private TcpServer     mServer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mPreview = (SurfaceView)findViewById(R.id.camera_preview_view);
        mPreview.getHolder().addCallback(mPreviewSurfaceHolderCallback);

        mCamDev  = Camera.open(0);
        Camera.Parameters params = mCamDev.getParameters();
        params .setPreviewSize(CAMERA_VIDEO_WIDTH, CAMERA_VIDEO_HEIGHT);
        mCamDev.setParameters(params);

        mH264Enc = new H264HwEncoder();
        mH264Enc.init(CAMERA_VIDEO_WIDTH, CAMERA_VIDEO_HEIGHT, 25, 256*1024);
        mServer  = new TcpServer(8000, mH264Enc);
        mServer.start();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mServer.close();
        mH264Enc.free();
        mCamDev.release();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
//          Log.d(TAG, "onPreviewFrame");
            camera.addCallbackBuffer(mPreviewBuf);
            mH264Enc.enqueueInputBuffer(data, 0, 1000 * 1000);
        }
    };

    private SurfaceHolder.Callback mPreviewSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
            try {
                mCamDev.stopPreview();
                mCamDev.setPreviewDisplay(holder);
                mCamDev.addCallbackBuffer(mPreviewBuf);
                mCamDev.setPreviewCallbackWithBuffer(mPreviewCallback);
                mCamDev.startPreview();
            } catch (Exception e) {}
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            mCamDev.stopPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.d(TAG, "surfaceChanged");
        }
    };
}




