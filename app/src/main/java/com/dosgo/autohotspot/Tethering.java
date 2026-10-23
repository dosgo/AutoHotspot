package com.dosgo.autohotspot;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.Handler;
import com.android.dx.stock.ProxyBuilder;
import java.lang.reflect.InvocationHandler;
import android.util.Log;

public class Tethering {

    private static final String TAG = Tethering.class.getSimpleName();
    private static final int WIFI_AP_STATE_ENABLED = 13;
    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static final String EXTRA_WIFI_AP_STATE = "wifi_state";

    public static void ensureHotspotEnabled(Context context, boolean useRoot) {
        if (isHotspotEnabled(context)) {
            Log.d(TAG, "热点已开启，无需处理");
            return;
        }

        if (useRoot && RootShell.isAvailable()) {
            // Android 11 的 TetheringManager 接受 WRITE_SETTINGS。Root 模式先授予对应
            // AppOps，再调用系统热点接口；新系统另外尝试 connectivity shell 命令。
            RootShell.execute("appops set " + context.getPackageName()
                    + " android:write_settings allow");
            RootShell.execute("cmd connectivity tether start wifi");
        }
        startTethering(context);
    }

    public static boolean isHotspotEnabled(Context context) {
        try {
            Intent state = context.registerReceiver(null, new IntentFilter(WIFI_AP_STATE_CHANGED_ACTION));
            if (state != null
                    && state.getIntExtra(EXTRA_WIFI_AP_STATE, -1) == WIFI_AP_STATE_ENABLED) {
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "无法从广播读取热点状态", e);
        }

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            Method getWifiApState = wifiManager.getClass().getDeclaredMethod("getWifiApState");
            getWifiApState.setAccessible(true);
            Object state = getWifiApState.invoke(wifiManager);
            return state instanceof Integer && (Integer) state == WIFI_AP_STATE_ENABLED;
        } catch (Exception e) {
            Log.d(TAG, "无法通过 WifiManager 读取热点状态", e);
            return false;
        }
    }

    public static void startTethering(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            File outputDir = mContext.getCodeCacheDir();
            Class classOnStartTetheringCallback = Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
            Method startTethering = connectivityManager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, classOnStartTetheringCallback);
            Object proxy = ProxyBuilder.forClass(classOnStartTetheringCallback).dexCache(outputDir).handler(new InvocationHandler() {
                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    return null;
                }
            }).build();
            startTethering.invoke(connectivityManager, 0, false, proxy);
        } catch (Exception e) {
            Log.e(TAG, "打开热点失败");
            e.printStackTrace();
        }
    }

    public boolean enableTetheringNew(Context mContext) {
        File outputDir = mContext.getCodeCacheDir();
        Object proxy = new Object();
        try {
            proxy = ProxyBuilder.forClass(classOnStartTetheringCallback())
                    .dexCache(outputDir).handler(new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            switch (method.getName()) {
                                case "onTetheringStarted":
                                    Log.d(TAG, "onTetheringStarted");
                                    break;
                                case "onTetheringFailed":
                                    Log.d(TAG, "onTetheringFailed");
                                    break;
                                default:
                                    ProxyBuilder.callSuper(proxy, method, args);
                            }
                            return null;
                        }

                    }).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConnectivityManager manager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        Method method = null;
        try {
            method = manager.getClass().getDeclaredMethod("startTethering", int.class, boolean.class, classOnStartTetheringCallback(),Handler.class);
            if (method == null) {
                Log.e(TAG, "startTetheringMethod is null");
            } else {
                method.invoke(manager, 0, false, proxy, null);
            }
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Class classOnStartTetheringCallback() {
        try {
            return Class.forName("android.net.ConnectivityManager$OnStartTetheringCallback");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stopTethering(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Method stopTethering = connectivityManager.getClass().getDeclaredMethod("stopTethering", int.class);
            stopTethering.invoke(connectivityManager,0);
        } catch (Exception e) {
            Log.e(TAG,"关闭热点失败");
            e.printStackTrace();
        }
    }
}
