package com.cromarmot.androidofficialtutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.reactivex.rxjava3.core.Flowable;

public class MainActivity extends AppCompatActivity {
    static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO remove RxJava Demo
        Flowable.just("Hello world").subscribe((d) -> Log.i(TAG, d));
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
    }

    public void startMain2(View v) {
        startActivity(new Intent(this, com.example.aotm1.MainActivity.class));
    }


    public void startApks(View view) {
        startActivity(new Intent(this,com.cromarmot.debuq.apks.ApksActivityMainActivity.class));
    }

    public void startMlTool(View view) {
        startActivity(new Intent(this,com.example.mltool.MltoolActivityMainActivity.class));
    }
}