package com.cromarmot.androidofficialtutorial;

import android.app.AlertDialog;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

import static android.content.pm.PackageManager.GET_META_DATA;

class AndroidUtils {
    static final String TAG = AndroidUtils.class.getName();

    public static int fetchVersion() {
        return Build.VERSION.SDK_INT;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static List<AppInfo> fetchAppsGTO(final Context context) {
        if (!checkAppUsagePermission(context)) {
            Log.e(TAG, "Require permission");
            requestAppUsagePermissionOb(context)
                    .subscribe();
            return new ArrayList<>();
        }
        List<AppInfo> appInfos = new ArrayList<>();
        final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
        try {
            List<ApplicationInfo> pkgs = context.getPackageManager().getInstalledApplications(GET_META_DATA);
            for (ApplicationInfo pkg : pkgs) {
                String packageName = pkg.packageName; //"com.google.android.youtube";
                PackageManager pm = context.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
                StorageStats storageStats = storageStatsManager.queryStatsForUid(ai.storageUuid, ai.uid);
                AppInfo appInfo = new AppInfo(packageName);
                appInfo.label = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, pm.GET_META_DATA));
                appInfo.cacheSize = storageStats.getCacheBytes();
                appInfo.dataSize = storageStats.getDataBytes();
                appInfo.appSize = storageStats.getAppBytes();
                appInfos.add(appInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appInfos;
    }

    // TODO not Implemented
    public static List<AppInfo> fetchAppsLO(Context context) {
        PackageManager pm = context.getPackageManager();
        List<AppInfo> appInfos = new ArrayList<>();
        Method getPackageSizeInfo = null;
        try {
            getPackageSizeInfo = pm.getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        List<ApplicationInfo> apps = pm.getInstalledApplications(GET_META_DATA);
        if (getPackageSizeInfo != null) {
            for (ApplicationInfo ai : apps) {
                try {
                    getPackageSizeInfo.invoke(pm, ai.packageName, new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            if (succeeded && pStats.cacheSize > 0) {
                                // Log.i("AppInfo", appInfo.name + "\t" + appInfo.packageName + "\t" + appInfo.loadLabel(pmInMethod).toString());
                                AppInfo rai = new AppInfo(ai.packageName);
                                rai.dataSize = pStats.dataSize;
                                rai.cacheSize = pStats.cacheSize;
                                rai.appSize = pStats.codeSize;
                            }
                        }
                    });
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return appInfos;
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

    public static Observable<Integer> handleRxJavaDialog(Context context, String Message) {
        return Observable.create(emitter -> {
            final AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage(Message)
                    .setPositiveButton("确认", (dialog1, which) -> {
                        Log.e(TAG, "confirm" + which);
                        emitter.onNext(which);
                        emitter.onComplete();
                    })
                    .setNegativeButton("取消", (dialog1, which) -> {
                        Log.e(TAG, "cancel" + which);
                        emitter.onNext(which);
                        emitter.onComplete();
                    })
                    .create();
            dialog.show();
            // 当被取消订阅，unsubscribed 时调用的方法
            emitter.setCancellable(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            });
        });
    }

    public static Observable<Boolean> requestAppUsagePermissionOb(Context context) {
        return handleRxJavaDialog(context, "需要 获取应用使用大小 的权限")
                .map((pick) -> {
                    if (pick == DialogInterface.BUTTON_POSITIVE) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                });
    }

    public static String humanBytes(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return size / 1024.0 + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / 1024 / 1024.0 + " MB";
        } else {
            return size / 1024 / 1024 / 1024.0 + " GB";
        }
    }

    public static List<AppInfo> fetchApps(Context context) {
        List<AppInfo> appInfos;
        // Android O 不再支持
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfos = fetchAppsGTO(context);
        } else {
            appInfos = fetchAppsLO(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfos.sort((ai0, ai1) -> {
                if (ai0.totalSize() == ai1.totalSize()) {
                    return 0;
                }
                return ai0.totalSize() < ai1.totalSize() ? 1 : -1;
            });
        } else {
            Toast toast = new Toast(context);
            toast.setText(Build.VERSION.SDK_INT + " not support sort");
            toast.show();
        }
        return appInfos;
    }
}
