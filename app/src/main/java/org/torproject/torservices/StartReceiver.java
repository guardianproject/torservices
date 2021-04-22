
package org.torproject.torservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import org.torproject.jni.TorService;

import androidx.core.content.ContextCompat;
/**
 * Processes {@link Intent}s from apps that are requesting Tor proxying,
 * including replying to the apps when the user has disabled automatic
 * starts.  This also sanitizes incoming {@code Intent}s before
 * forwarding it to {@link TorService}.
 */
public class StartReceiver extends BroadcastReceiver {

    /**
     * If the user has disabled auto-starts, the requesting app will receive
     * this reply.  This matches the constant from Orbot and NetCipher.
     */
    private static final String STATUS_STARTS_DISABLED = "STARTS_DISABLED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, TorService.ACTION_START)) {
            String packageName = intent.getStringExtra(TorService.EXTRA_PACKAGE_NAME);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("pref_allow_background_starts", true)) {
                Intent startTorIntent = new Intent(context, TorService.class);
                startTorIntent.setAction(action);
                if (packageName != null) {
                    startTorIntent.putExtra(TorService.EXTRA_PACKAGE_NAME, packageName);
                }
                if (App.useForeground(context)) {
                    ContextCompat.startForegroundService(context, startTorIntent);
                } else {
                    context.startService(startTorIntent);
                }
            } else if (!TextUtils.isEmpty(packageName)) {
                // let the requesting app know that the user has disabled starting via Intent
                Intent startsDisabledIntent = new Intent(TorService.ACTION_STATUS);
                startsDisabledIntent.putExtra(TorService.EXTRA_STATUS, STATUS_STARTS_DISABLED);
                startsDisabledIntent.setPackage(packageName);
                context.sendBroadcast(startsDisabledIntent);
            }
        }
    }
}
