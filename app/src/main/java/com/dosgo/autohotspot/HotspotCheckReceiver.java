package com.dosgo.autohotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotspotCheckReceiver extends BroadcastReceiver {
    private static final String TAG = "HotspotCheckReceiver";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();
        Context storageContext = appContext.createDeviceProtectedStorageContext();
        SharedPreferences preferences = storageContext.getSharedPreferences(
                appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        if (!preferences.getBoolean("hotspotSwitch", false)) {
            HotspotScheduler.cancel(appContext);
            return;
        }

        boolean useRoot = preferences.getBoolean("rootForceSwitch", true);
        PendingResult pendingResult = goAsync();
        EXECUTOR.execute(() -> {
            try {
                Tethering.ensureHotspotEnabled(appContext, useRoot);
            } catch (Exception e) {
                Log.e(TAG, "检查热点失败", e);
            } finally {
                pendingResult.finish();
            }
        });
    }
}
