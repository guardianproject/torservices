package org.torproject.torservices;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import net.freehaven.tor.control.TorControlCommands;

import org.torproject.jni.TorService;

public class SettingsActivity extends AppCompatActivity {

    static final String PREF_ABOUT = "pref_about";
    static final String PREF_USE_PERSISTENT_NOTIFICATIONS = "pref_use_persistent_notifications";
    static final String PREF_ALLOW_BACKGROUND_STARTS = "pref_allow_background_starts";
    static final String PREF_START_ON_BOOT = "pref_start_on_boot";

    public static class MySettingsFragment extends PreferenceFragmentCompat {

        BroadcastReceiver broadcastReceiver;

        @Override
        public void onCreatePreferences(@Nullable Bundle bundle, @Nullable String s) {
            addPreferencesFromResource(R.xml.preferences);

            String about = String.format("v%s with tor v%s", BuildConfig.VERSION_NAME, TorService.VERSION_NAME);
            findPreference(PREF_ABOUT).setTitle(about);

            SwitchPreference usePersistentNotification = findPreference(
                    PREF_USE_PERSISTENT_NOTIFICATIONS);
            usePersistentNotification.setOnPreferenceChangeListener((preference, enabled) -> {
                // TODO put up/take down notification, e.g. set foreground
                if (Build.VERSION.SDK_INT < 26) {
                    if ((Boolean) enabled) {
                        App.startTorServiceForeground(getActivity());
                    } else {
                        NotificationManagerCompat.from(getContext()).cancelAll();
                    }
                }
                return true;
            });
            if (Build.VERSION.SDK_INT >= 26) {
                usePersistentNotification.setChecked(true);
                usePersistentNotification.setEnabled(false);
            }
            final Preference statusPreference = findPreference("pref_status");
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String data = intent.getStringExtra(intent.getAction());
                    statusPreference.setTitle(data);
                }
            };
            App.localBroadcastManager.registerReceiver(broadcastReceiver,
                    new IntentFilter(TorControlCommands.EVENT_CIRCUIT_STATUS));
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            App.localBroadcastManager.unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestNotificationPermission();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MySettingsFragment())
                .commit();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                    // nothing to do here, we just ignore if the permission wasn't granted
                }).launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
