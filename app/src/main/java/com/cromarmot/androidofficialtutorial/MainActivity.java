package com.cromarmot.androidofficialtutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
        Intent intent = new Intent(this, AppUsageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(AppUsageActivity.EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void PieChartActivity(View view) {
        Intent intent = new Intent(this, PieChartActivity.class);
        startActivity(intent);
    }

}