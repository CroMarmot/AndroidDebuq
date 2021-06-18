package com.cromarmot.debuq.apks;

import java.io.Serializable;

class AppInfo implements Serializable {
    public String appName;
    public String label;
    public long dataSize;
    // Return the size of app. This includes {@code APK} files, optimized
    // compiler output, and unpacked native libraries.
    public long appSize;
    public long cacheSize;

    public AppInfo(String packageName) {
        appName = label = packageName;
    }

    public long totalSize() {
        return dataSize + appSize + cacheSize;
    }
}
