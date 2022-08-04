
package org.torproject.torservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.torproject.jni.TorService;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;

/**
 * Processes {@link Intent}s from apps that are requesting Tor proxying,
 * including replying to the apps when the user has disabled automatic
 * starts.  This also sanitizes incoming {@code Intent}s before
 * forwarding it to {@link TorService}.
 */
public class StartReceiver extends BroadcastReceiver {

    public static final String TAG = "StartReceiver";

    /**
     * If the user has disabled auto-starts, the requesting app will receive
     * this reply.  This matches the constant from Orbot and NetCipher.
     */
    private static final String STATUS_STARTS_DISABLED = "STARTS_DISABLED";

    private static final String INTENT_ACTION_PT_START = "info.pluggabletransports.start";

    public static final void start(Context context) {
        Intent intent = new Intent(TorService.ACTION_START);
        intent.setClass(context, StartReceiver.class);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, TorService.ACTION_START)) {
            String packageName = intent.getStringExtra(TorService.EXTRA_PACKAGE_NAME);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean(SettingsActivity.PREF_ALLOW_BACKGROUND_STARTS, true)) {
                Intent startTorIntent = new Intent(context, TorService.class);
                startTorIntent.setAction(action);
                if (packageName != null) {
                    startTorIntent.putExtra(TorService.EXTRA_PACKAGE_NAME, packageName);
                }
                String ptInfo = getPluggableTransports(context);
                if (ptInfo != null)
                    startPT(context,ptInfo);

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

    private void startPT (Context context, String pkg) {
        Intent ptIntent = new Intent(INTENT_ACTION_PT_START);
        ptIntent.setPackage(pkg);
        context.startService(ptIntent);
    }


    private String getPluggableTransports (Context context)
    {
        Intent intent = new Intent(INTENT_ACTION_PT_START);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);
        if(!resolveInfos.isEmpty()) {
            ResolveInfo info = resolveInfos.get(0);
            return info.serviceInfo.packageName;
        }

        return null;
    }
}
