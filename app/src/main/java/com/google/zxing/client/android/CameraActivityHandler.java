package com.google.zxing.client.android;

import android.os.Handler;
import android.os.Message;

import com.google.zxing.client.android.camera.CameraManager;

import io.weichao.service_demo.R;

public final class CameraActivityHandler extends Handler {
    private static final String TAG = "CameraActivityHandler";

    private CameraActivity activity;
    private final CameraManager cameraManager;

    CameraActivityHandler(CameraActivity activity, CameraManager cameraManager) {
        this.activity = activity;
        this.cameraManager = cameraManager;

        cameraManager.startPreview();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.take_picure:
                cameraManager.requestTakePicture();
                break;
        }
    }

    public void quitSynchronously() {
        activity = null;
        cameraManager.stopPreview();
    }
}
