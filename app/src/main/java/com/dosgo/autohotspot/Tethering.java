package com.dosgo.autohotspot;

import android.net.ConnectivityManager;
import android.content.Context;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.os.Handler;
import com.android.dx.stock.ProxyBuilder;
import java.lang.reflect.InvocationHandler;
import android.util.Log;

public class Tethering {

    private static final String TAG = Tethering.class.getSimpleName();

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