package com.cromarmot.androidofficialtutorial;

import java.io.Serializable;

class AppInfo implements Serializable {
    public String appName;
    public String label;
    public long dataSize;
    public long apkSize;
    public long cacheSize;

    public AppInfo(String packageName) {
        appName = packageName;
    }

    public long totalSize() {
        return dataSize + apkSize + cacheSize;
    }
}
