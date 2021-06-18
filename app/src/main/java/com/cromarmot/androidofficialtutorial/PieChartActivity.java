package com.cromarmot.androidofficialtutorial;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.cromarmot.androidofficialtutorial.AndroidUtils.humanBytes;

public class PieChartActivity extends ChartBaseActivity implements OnChartValueSelectedListener {
    public static final String EXTRA_APP_INFO = "APP_INFO";
    private static final String TAG = PieChartActivity.class.getName();

    private ArrayList<AppInfo> ais;
    private ArrayList<AppInfo> displayAis;

    private PieChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_piechart);

        // setTitle("PieChartActivity");

        chart = findViewById(R.id.chart1);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(0, 0, 0, 0);

        chart.setDragDecelerationFrictionCoef(2f);


        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(50);

        chart.setHoleRadius(80f);
        chart.setTransparentCircleRadius(120f);

        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);


        chart.animateY(1400, Easing.EaseInOutQuad);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        // l.setDrawInside(false);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(Color.BLACK);
        chart.setEntryLabelTextSize(6f);

        // Get Passed Data
        Type type = new TypeToken<ArrayList<AppInfo>>() {
        }.getType();
        ArrayList<AppInfo> appinfos = new Gson().fromJson(getIntent().getStringExtra(EXTRA_APP_INFO), type);
        if (appinfos != null) {
            ais = appinfos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                displayAis = appinfos = appInfosFilter(appinfos);
            }
            setData(appInfos2PieEntries(appinfos));

            List<String> elements = new ArrayList<>();
            for (AppInfo ai : appInfos) {
                String str = ai.appName + "\t" + ai.appSize + "\t" + ai.cacheSize + "\t" + ai.dataSize;
                Log.i(TAG + "/AppInfo", str);
                elements.add(str);
            }
            ListView lv = findViewById(R.id.listView);
            lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, elements));
        } else {
            setData(getRandomPieEntries(10, 10));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList<AppInfo> appInfosFilter(ArrayList<AppInfo> appInfos) {
        // TODO sqlite + room(orm) + rxjava
        // TODO filters
        List<String> nameFilters = Arrays.asList("oneplus", "com.android");
        // 1mb
        int sizeFilter = 256 * 1024 * 1024;
        // ----------------------SIZE FILTER-------------------------------------------
        // TODO merge small packages in one
        AppInfo aiSmall = new AppInfo("Small packages");
        List<AppInfo> smallApps = appInfos.stream()
                .filter((ai) -> ai.totalSize() <= sizeFilter)
                .collect(Collectors.toList());
        smallApps.forEach(appInfo -> {
            Log.d(TAG, "appInfosFilter::Small packages" + appInfo.appName + "\t" + appInfo.totalSize());
            // TODO provide function
            aiSmall.cacheSize += appInfo.cacheSize;
            aiSmall.appSize += appInfo.appSize;
            aiSmall.dataSize += appInfo.dataSize;
        });
        aiSmall.appName += "(<" + (sizeFilter / 1024 / 1024) + "MB," + smallApps.size() + "个)";

        appInfos = (ArrayList<AppInfo>) appInfos
                .stream()
                .filter((ai) -> ai.totalSize() > sizeFilter)
                .collect(Collectors.toList());
        // ----------------------NAME FILTER-------------------------------------------
        AppInfo aiNameFiltered = new AppInfo("Name filtered packages");
        appInfos.stream()
                .filter((ai) -> nameFilters.stream()
                        .filter(str -> str.contains(ai.appName))
                        .collect(Collectors.toList())
                        .size() > 0)
                .collect(Collectors.toList())
                .forEach(appInfo -> {
                    // TODO provide function
                    Log.d(TAG, "appInfosFilter::Name filtered packages" + appInfo.appName + "\t" + appInfo.totalSize());
                    aiNameFiltered.cacheSize += appInfo.cacheSize;
                    aiNameFiltered.appSize += appInfo.appSize;
                    aiNameFiltered.dataSize += appInfo.dataSize;
                });
        appInfos = (ArrayList<AppInfo>) appInfos
                .stream()
                .filter((ai) -> nameFilters.stream()
                        .filter(str -> str.contains(ai.appName))
                        .collect(Collectors.toList())
                        .size() == 0)
                .collect(Collectors.toList());
        // -----------------------------------------------------------------
        AppInfo aiEmpty = new AppInfo("Empty Space");
        StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
        aiEmpty.dataSize = sf.getAvailableBytes();

        Log.i(TAG, "appInfosFilter::getAvailableBytes():" + sf.getAvailableBytes());
        Log.i(TAG, "appInfosFilter::getFreeBytes():" + sf.getFreeBytes());
        Log.i(TAG, "appInfosFilter::getTotalBytes():" + sf.getTotalBytes());
        // -----------------------------------------------------------------

        appInfos.add(aiSmall);
        appInfos.add(aiNameFiltered);
        appInfos.add(aiEmpty);

        // TODO add Empty part
        return appInfos;
    }

    private ArrayList<PieEntry> appInfos2PieEntries(ArrayList<AppInfo> appInfos) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (AppInfo ai : appInfos) {
            // entries.add(new PieEntry(ai.totalSize(), ai.label + ai.appName, getResources().getDrawable(R.drawable.star)));
            entries.add(new PieEntry(ai.totalSize(), ai.label, getResources().getDrawable(R.drawable.star)));
        }
        return entries;
    }

    private ArrayList<PieEntry> getRandomPieEntries(int count, float range) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < count; i++) {
            // 值 名字
            entries.add(new PieEntry((float) ((Math.random() * range) + range / 5),
                    " " + i,
                    getResources().getDrawable(R.drawable.star)));
        }
        return entries;
    }

    private void setData(ArrayList<PieEntry> pieEntries) {
        PieDataSet dataSet = new PieDataSet(pieEntries, "Packages(" + pieEntries.size() + ")");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(0f);
        dataSet.setIconsOffset(new MPPointF(0, 0));
        dataSet.setSelectionShift(10f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        dataSet.setColors(colors);
        // dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f); // remove
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "PieChartActivity");
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        // Log.i("VAL SELECTED", "Value: " + e.getY() + ", index: " + h.getX() + ", DataSet index: " + h.getDataSetIndex());
        int index = (int) h.getX();
        TextView tv1 = findViewById(R.id.package_name);
        TextView tv_apk_size = findViewById(R.id.apk_size);
        TextView tv_cache_size = findViewById(R.id.cache_size);
        TextView tv_data_size = findViewById(R.id.data_size);
        TextView tv_app_label = findViewById(R.id.app_label);
        if (displayAis == null) return;
        tv1.setText(displayAis.get(index).appName);
        tv_apk_size.setText("App:" + humanBytes(displayAis.get(index).appSize));
        tv_cache_size.setText("Cache:" + humanBytes(displayAis.get(index).cacheSize));
        tv_data_size.setText("Data:" + humanBytes(displayAis.get(index).dataSize));
        tv_app_label.setText(displayAis.get(index).label);
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }
}
