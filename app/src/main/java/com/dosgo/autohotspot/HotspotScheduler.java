package com.dosgo.autohotspot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public final class HotspotScheduler {
    public static final long CHECK_INTERVAL_MILLIS = 15 * 60 * 1000L;
    private static final int REQUEST_CODE = 1001;
    private static final String ACTION_CHECK = "com.dosgo.autohotspot.CHECK_HOTSPOT";

    private HotspotScheduler() {
    }

    public static void schedule(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        long firstCheck = SystemClock.elapsedRealtime() + CHECK_INTERVAL_MILLIS;
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstCheck,
                CHECK_INTERVAL_MILLIS,
                getPendingIntent(context));
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(context));
        }
    }

    public static void checkNow(Context context) {
        Intent intent = new Intent(context, HotspotCheckReceiver.class).setAction(ACTION_CHECK);
        context.sendBroadcast(intent);
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, HotspotCheckReceiver.class).setAction(ACTION_CHECK);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
