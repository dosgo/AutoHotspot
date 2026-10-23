package com.dosgo.autohotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context _context, Intent intent) {
        Context context=_context;
        Context directBootContext = _context.createDeviceProtectedStorageContext();
        if(Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())){
            context=directBootContext;
        }
        SharedPreferences sharedPreferences = directBootContext.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        boolean hotspotSwitch=sharedPreferences.getBoolean("hotspotSwitch",false);
        if(hotspotSwitch) {
            HotspotScheduler.schedule(context);
            HotspotScheduler.checkNow(context);
        }
    }
}
