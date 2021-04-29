package org.torproject.torservices;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import org.torproject.jni.TorService;

import androidx.core.app.NotificationManagerCompat;

public class SettingsActivity extends Activity {

    static final String PREF_ABOUT = "pref_about";
    static final String PREF_USE_PERSISTENT_NOTIFICATIONS = "pref_use_persistent_notifications";
    static final String PREF_ALLOW_BACKGROUND_STARTS = "pref_allow_background_starts";
    static final String PREF_START_ON_BOOT = "pref_start_on_boot";

    public static class MySettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            String about = String.format("v%s with tor v%s", BuildConfig.VERSION_NAME, TorService.VERSION_NAME);
            findPreference(PREF_ABOUT).setTitle(about);

            SwitchPreference usePersistentNotification = (SwitchPreference) findPreference(
                    PREF_USE_PERSISTENT_NOTIFICATIONS);
            usePersistentNotification.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object enabled) {
                    // TODO put up/take down notification, e.g. set foreground
                    if (Build.VERSION.SDK_INT < 26) {
                        if ((Boolean) enabled) {
                            App.startTorServiceForeground(getActivity());
                        } else {
                            NotificationManagerCompat.from(getActivity()).cancelAll();
                        }
                    }
                    return true;
                }
            });
            if (Build.VERSION.SDK_INT >= 26) {
                usePersistentNotification.setChecked(true);
                usePersistentNotification.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MySettingsFragment())
                .commit();
    }
}
