
package org.torproject.torservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final String INTENT_ACTION_PT_STATUS = "info.pluggabletransports.status";


    public static final void start(Context context) {
        Intent intent = new Intent(TorService.ACTION_START);
        intent.setClass(context, StartReceiver.class);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private Intent lastStartTorIntent;

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
                PluggableTransport ptInfo = getPluggableTransports(context);
                if (ptInfo != null) {

                    lastStartTorIntent = startTorIntent;
                    //wait for status/port and then add pt connection info to TorService
                    startPT(context, ptInfo.packageName);
                }
                else {
                    if (App.useForeground(context)) {
                        ContextCompat.startForegroundService(context, startTorIntent);
                    } else {
                        context.startService(startTorIntent);
                    }
                }
            } else if (!TextUtils.isEmpty(packageName)) {
                // let the requesting app know that the user has disabled starting via Intent
                Intent startsDisabledIntent = new Intent(TorService.ACTION_STATUS);
                startsDisabledIntent.putExtra(TorService.EXTRA_STATUS, STATUS_STARTS_DISABLED);
                startsDisabledIntent.setPackage(packageName);
                context.sendBroadcast(startsDisabledIntent);
            }
        }
        else if (TextUtils.equals(action, INTENT_ACTION_PT_STATUS))
        {

            //got status from PT started, now added it TorService config and go!

            String ptType = "snowflake";
            int ptPort = 9090;

            lastStartTorIntent.putExtra("pttype",ptType);
            lastStartTorIntent.putExtra("ptport",ptPort);


            if (App.useForeground(context)) {
                ContextCompat.startForegroundService(context, lastStartTorIntent);
            } else {
                context.startService(lastStartTorIntent);
            }
        }
    }

    private void startPT (Context context, String pkg) {
        Intent ptIntent = new Intent(INTENT_ACTION_PT_START);
        ptIntent.setPackage(pkg);
        ContextCompat.startForegroundService(context,ptIntent);
    }



    private PluggableTransport getPluggableTransports (Context context)
    {
        Intent intent = new Intent(INTENT_ACTION_PT_START);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);
        if(!resolveInfos.isEmpty()) {
            ResolveInfo info = resolveInfos.get(0);
            PluggableTransport pt = new PluggableTransport();
            pt.category = info.filter.getCategory(0);
            pt.packageName = info.serviceInfo.packageName;
            return pt;
        }

        return null;
    }

    class PluggableTransport
    {
        String category;
        String packageName;
    }
}
