package com.dosgo.autohotspot;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
public class SettingsActivity extends AppCompatActivity {
    private final MyOnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener =
            new MyOnSharedPreferenceChangeListener();
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Context directBootContext = this.createDeviceProtectedStorageContext();
        preferences = directBootContext.getSharedPreferences(
                this.getPackageName() + "_preferences", Context.MODE_PRIVATE);
        // 注册添加设置的项数据发生变化时候的监听
        preferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        if (preferences.getBoolean("hotspotSwitch", false)) {
            HotspotScheduler.schedule(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferences != null && preferences.getBoolean("hotspotSwitch", false)) {
            HotspotScheduler.checkNow(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (preferences != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(
                    mOnSharedPreferenceChangeListener);
        }
        super.onDestroy();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setStorageDeviceProtected();
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }


    private class MyOnSharedPreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("hotspotSwitch")) {
                boolean enabled = sharedPreferences.getBoolean("hotspotSwitch", false);
                if (enabled) {
                    HotspotScheduler.schedule(SettingsActivity.this);
                    HotspotScheduler.checkNow(SettingsActivity.this);
                    if (!Settings.System.canWrite(SettingsActivity.this)) {
                        Intent intent = new Intent(
                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }
                } else {
                    HotspotScheduler.cancel(SettingsActivity.this);
                }
            }
        }
    }

}
