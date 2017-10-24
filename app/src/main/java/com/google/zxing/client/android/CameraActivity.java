package com.google.zxing.client.android;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;

import io.weichao.service_demo.MyService;
import io.weichao.service_demo.R;

public final class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "CameraActivity";

    private SurfaceView mSurfaceView;

    private CameraManager mCameraManager;
    private CameraActivityHandler mCameraActivityHandler;

    private boolean mHasSurface;

    private MyService.MyBinder mMyBinder;
    private ServiceConnection mServiceConnection;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_camera);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        findViewById(R.id.capture_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPreview();
    }

    @Override
    protected void onPause() {
        stopPreview();
        super.onPause();
    }

    private void startPreview() {
        mCameraManager = new CameraManager(this);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    private void stopPreview() {
        if (mCameraActivityHandler != null) {
            mCameraActivityHandler.quitSynchronously();
            mCameraActivityHandler = null;
        }
        mCameraManager.closeDriver();
        if (!mHasSurface) {
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * 初始化 Camera，初始化　Handler。
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            mCameraActivityHandler = new CameraActivityHandler(this, mCameraManager);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    private void takePicture() {
        if (mCameraActivityHandler != null) {
            Message message = Message.obtain(mCameraActivityHandler, R.id.take_picure);
            message.sendToTarget();
        }
    }

    public void submit(final Bitmap bitmap) {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mMyBinder = (MyService.MyBinder) service;
                mMyBinder.submit(bitmap);
                CameraActivity.this.finish();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }
}
