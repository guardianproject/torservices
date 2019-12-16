package org.torproject.torservices;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import hugo.weaving.DebugLog;

public class SettingsActivity extends Activity {

    public static class MySettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MySettingsFragment())
                .commit();
    }
}
