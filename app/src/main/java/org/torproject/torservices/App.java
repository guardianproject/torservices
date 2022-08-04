package org.torproject.torservices;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import org.torproject.jni.TorService;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class App extends Application {

    private static final String TAG = "App";

    static TorService torService;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent startTorIntent = new Intent(this, TorService.class);
        startTorIntent.setAction(TorService.ACTION_START);
        if (useForeground(this)) {
            ContextCompat.startForegroundService(this, startTorIntent);
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(startTorIntent);
        }

        bindService(
                startTorIntent,
                new ServiceConnection() {

                    private IBinder iBinder;

                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        this.iBinder = iBinder;
                        if (!(iBinder instanceof TorService.LocalBinder)) {
                            return;
                        }
                        torService = ((TorService.LocalBinder) iBinder).getService();
                        if (useForeground(torService)) {
                            startTorServiceForeground(torService);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        if (iBinder instanceof TorService.LocalBinder) {
                            NotificationManagerCompat.from(App.this).cancelAll();
                        }
                    }
                },
                0);
    }

    public static void startTorServiceForeground(Context context) {
        if (torService != null) {
            torService.startForeground(0xc0feefee, getNotification(torService));
        }
    }

    public static boolean useForeground(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            return true;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(SettingsActivity.PREF_USE_PERSISTENT_NOTIFICATIONS, false);
    }

    public static Notification getNotification(Context context) {
        final String packageName = context.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent exitIntent = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            exitIntent = PendingIntent.getActivity(context,
                    0, intent, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            exitIntent = PendingIntent.getActivity(context,
                    0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, packageName)
                .setContentTitle(context.getString(R.string.notification_title))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(exitIntent)
                .setOngoing(true);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    packageName,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_MIN);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(channel);
        }
        return builder.build();
    }
}
