package com.cromarmot.androidofficialtutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class Apk1Activity extends AppCompatActivity {

    private static final String TAG = Apk1Activity.class.getName();
    private static final int PERMISSION_STORAGE = 0;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    private void LogToast(String str) {
        Log.e(Apk1Activity.TAG, str);
        Toast.makeText(Apk1Activity.this, str, Toast.LENGTH_SHORT).show();
    }

    private void requestPermission(String permission, int requestCode) {
        LogToast("no permission:" + permission);
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            LogToast("No view");
        } else {
            Toast.makeText(getApplicationContext(), "Permission Required!", Toast.LENGTH_SHORT)
                    .show();
            ActivityCompat.requestPermissions(Apk1Activity.this, new String[]{permission}, requestCode);
        }
    }

    private @NonNull Observable<Integer> checkAndRequestPermission(String permission, int requestCode) {
        return Observable.create(emitter -> {
            int result = checkSelfPermission(permission);
            if (result != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Apk1Activity.this, new String[]{permission}, requestCode);
            }
            emitter.onNext(result);
            emitter.onComplete();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk1);

        // TODO 加个按钮 反复调用
        Intent intent = getIntent();
        Uri uriIn = intent.getData();
        String basename = (new File(uriIn.getPath())).getName();
        Log.i(TAG, "basename:" + basename);
        // file provider 可能命名随意 不是apk.1 结尾
        String dstFile = basename + ".apk";

        // requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE);
        // Log.i(TAG,"------------------------------------------------------------------------");


        checkAndRequestPermission(WRITE_EXTERNAL_STORAGE, PERMISSION_STORAGE)
                .subscribe((r) -> {
                    if (!r.equals(PERMISSION_GRANTED)) {
                        return;
                    }
                    // TODO 移动到公共目录
                    String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File fileOut = new File(downloadDir, dstFile);
                    LogToast("fileOut:" + fileOut);
                    InputStream in;
                    FileOutputStream out;
                    try {
                        // new FileInputStream(fileIn); 无法直接拿到分享的地址
                        in = getContentResolver().openInputStream(intent.getData());
                        out = new FileOutputStream(fileOut);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                    Log.i(TAG, "dst file:" + fileOut.getPath());
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FileUtils.copy(in, out);
                        } else {
                            LogToast("SDK VERSION LOW");
                        }
                    } catch (Exception e) {
                        LogToast("copy failed" + e);
                        return;
                    }
                    OpenFileUtil.installApk(Apk1Activity.this, fileOut);
                });
    }
}