package com.dosgo.autohotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

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
            Tethering.startTethering(context);
        }
        Toast.makeText(context, "Auto Hotspot start", Toast.LENGTH_SHORT).show();
    }
}