package com.cromarmot.androidofficialtutorial;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static com.cromarmot.androidofficialtutorial.PieChartActivity.EXTRA_APP_INFO;

public class AppUsageActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "MESSAGE";

    private static final String TAG = AppUsageActivity.class.getName();

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
        Log.i(TAG, "Android API Version: " + AndroidUtils.fetchVersion());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fetchSize(View view) {
        List<AppInfo> appInfos = AndroidUtils.fetchApps(this);
        Toast toast = new Toast(this);
        toast.setText("Packages:" + appInfos.size());
        toast.show();

        List<String> elements = new ArrayList<>();
        for (AppInfo ai : appInfos) {
            String str = ai.appName + "\t" + ai.apkSize + "\t" + ai.cacheSize + "\t" + ai.dataSize;
            Log.i(TAG + "/AppInfo", str);
            elements.add(str);
        }
        ListView lv = findViewById(R.id.listView);
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, elements));

        Intent intent = new Intent(this, PieChartActivity.class);
        intent.putExtra(EXTRA_APP_INFO, new Gson().toJson(appInfos));
        startActivity(intent);
    }

}