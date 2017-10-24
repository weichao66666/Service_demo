package io.weichao.service_demo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyService extends Service {
    private static String TAG = "MyService";

    private MyBinder mMyBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mMyBinder;
    }

    public class MyBinder extends Binder {
        private MyListener mMyListener;

        public void setListener(MyListener myListener) {
            mMyListener = myListener;
        }

        public void submit(final Bitmap bitmap) {
            if (bitmap != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileOutputStream out = null;
                        try {
                            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "temp.jpg";
                            File file = new File(filePath);
                            file.createNewFile();
                            out = new FileOutputStream(filePath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            out.flush();
                            if (mMyListener != null) {
                                mMyListener.onSuccess();
                            }
                        } catch (Exception e) {
                            if (mMyListener != null) {
                                mMyListener.onFailed(e.getMessage());
                            }
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();
            }
        }
    }

    public interface MyListener {
        void onSuccess();

        void onFailed(String msg);
    }
}
