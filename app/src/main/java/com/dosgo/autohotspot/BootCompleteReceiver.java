package com.dosgo.autohotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            Toast.makeText(context, "Auto Hotspot start", Toast.LENGTH_SHORT).show();
            SharedPreferences editor = PreferenceManager.getDefaultSharedPreferences(context);
            boolean hotspotSwitch=editor.getBoolean("hotspotSwitch",false);
            if(hotspotSwitch) {
                Tethering.startTethering(context);
            }
        }
    }
}