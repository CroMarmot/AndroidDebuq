package com.cromarmot.debuq.apks;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import static com.cromarmot.debuq.apks.PieChartActivity.EXTRA_APP_INFO;

public class ApksActivityMainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "MESSAGE";

    private static final String TAG = ApksActivityMainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
    }

    public void fetchSize(View view) {
        Log.d(TAG, "fetchSize: CLICKED");
        List<AppInfo> appInfos = AndroidUtils.fetchApps(this);
        Log.d(TAG, "fetchSize: " + appInfos.toString());
        if (appInfos == null || appInfos.size() == 0) {

            return;
        }
        Toast.makeText(ApksActivityMainActivity.this, "Packages:" + appInfos.size(), Toast.LENGTH_SHORT).show();

        // List<String> elements = new ArrayList<>();
        // for (AppInfo ai : appInfos) {
        //     String str = ai.appName + "\t" + ai.appSize + "\t" + ai.cacheSize + "\t" + ai.dataSize;
        //     Log.i(TAG + "/AppInfo", str);
        //     elements.add(str);
        // }
        // ListView lv = findViewById(R.id.listView);
        // lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, elements));

        Intent intent = new Intent(this, PieChartActivity.class);
        intent.putExtra(PieChartActivity.EXTRA_APP_INFO, new Gson().toJson(appInfos));
        startActivity(intent);
    }

}