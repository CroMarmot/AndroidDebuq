package com.cromarmot.androidofficialtutorial;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class DisplayMessageActivity extends AppCompatActivity {

    private static final String TAG = DisplayMessageActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        int version = this.fetchVersion();
        this.fetchApps(version);
    }

    public static boolean checkAppUsagePermission(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        // try to get app usage state in last 1 min
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 60 * 1000, currentTime);
        if (stats.size() == 0) {
            return false;
        }

        return true;
    }

    public static void requestAppUsagePermission(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int fetchVersion() {
        int whichAndroidVersion = Build.VERSION.SDK_INT;
        Log.d(TAG, "fetchVersion: " + whichAndroidVersion);
        return whichAndroidVersion;
    }

    public void fetchApps(int androidApiVersion) {
        // Android O 不再支持
        if (androidApiVersion < Build.VERSION_CODES.O) {
            this.fetchAppsLO();
        } else {
            this.fetchAppsGTO(this);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void fetchAppsGTO(final Context context) {
        if (!this.checkAppUsagePermission(context)) {
            Log.e(TAG,"Require permission");
            this.requestAppUsagePermission(context);
        }
        final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        final StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<String> filterString = ["com.oneplus", "com.android"];
        try {
            List<ApplicationInfo> pkgs = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo pkg : pkgs) {
                String packageName = pkg.packageName; //"com.google.android.youtube";
                ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
                StorageStats storageStats = storageStatsManager.queryStatsForUid(ai.storageUuid, ai.uid);
                long cacheSize = storageStats.getCacheBytes();
                long dataSize = storageStats.getDataBytes();
                long apkSize = storageStats.getAppBytes();
                if(packageName.equals("com.tencent.mm")){
                    Log.e(TAG,"Found mm");
                }
                if(packageName.contains("tencent")){
                    Log.e(TAG,packageName);
                }
                if(!packageName.startsWith("com.oneplus") && !packageName.startsWith("com.android")){
                    Log.d(TAG + "/Size", packageName + ":" + cacheSize / 1024 + "," + dataSize / 1024 + "," + apkSize / 1024);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchAppsLO() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int size = apps.size();
        Method getPackageSizeInfo = null;
        try {
            getPackageSizeInfo = pm.getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (getPackageSizeInfo != null) {
            for (int i = 0; i < size; i++) {
                final ApplicationInfo appInfo = apps.get(i);
                try {
                    getPackageSizeInfo.invoke(pm, appInfo.packageName, new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            if (succeeded && pStats.cacheSize > 0) {
                                PackageManager pmInMethod = getPackageManager();
                                Log.d("AppInfo", appInfo.name + "\t" + appInfo.packageName + "\t" + appInfo.loadLabel(pmInMethod).toString());
                                Log.d(TAG, "dataSize:" + pStats.dataSize);
                                Log.d(TAG, "cacheSize:" + pStats.cacheSize);
                                Log.d(TAG, "codeSize:" + pStats.codeSize);
                            }
                        }
                    });
                } catch (IllegalAccessException e) {
                    Log.e("DisplayMessageActivity", "IllegalAccessException");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    Log.e("DisplayMessageActivity", "InvocationTargetException");
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}