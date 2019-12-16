package org.torproject.torservices;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.torproject.jni.TorService;

/**
 * Receives various broadcasts that signal that the device has completed booting.
 * This does not need to verify the {@link Intent} action because it only sends
 * an {@code Intent} whose receiver is publicly exported.
 * <p>
 * Since this is just triggering an {@link TorService#ACTION_START}
 * {@link Intent} to be sent, and any app can send that {@code Intent},
 * there is no need to verify the {@code Intent} that started this. That
 * then means there is only one place to list the supported {@code actions}:
 * {@code <receiver android:name=".OnBootReceiver">} in {@code AndroidManifest.xml}.
 */
public class OnBootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean("pref_start_on_boot", false)) {
            TorService.start(context);
        }
    }
}
