package io.weichao.service_demo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.client.android.CameraActivity;

public class MainActivity extends AppCompatActivity implements MyService.MyListener {
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,  // 读取存储
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 写入存储
            Manifest.permission.CAMERA                  // 拍照
    };

    private MyService.MyBinder mMyBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMyBinder = (MyService.MyBinder) service;
            mMyBinder.setListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMyBinder.setListener(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        super.onDestroy();
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, PERMISSIONS[2]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, 1);
            } else {
                onPermissionsGot();
            }
        } else {
            onPermissionsGot();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED
                    || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                MainActivity.this.finish();
            } else {
                onPermissionsGot();
            }
        }
    }

    private void onPermissionsGot() {
        bindService();
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });
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

    @Override
    public void onSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFailed(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "failed!" + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
