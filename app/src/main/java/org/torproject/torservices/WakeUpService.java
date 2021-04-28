package org.torproject.torservices;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

import org.torproject.jni.TorService;

import androidx.annotation.Nullable;

/**
 * This is needed as a shim between the Android background {@link android.app.Service}
 * restrictions, and the {@link TorService#ACTION_START} {@link Intent}s that
 * come from any app requesting to use Tor.
 * <p>
 * The {@link Messenger} interface is just a placeholder, just to get the
 * benefits of a Bound {@link android.app.Service}. The documentation about
 * background service restrictions says: "Note: These rules don't affect bound
 * services in any way. If your app defines a bound service, other components
 * can bind to that service whether or not your app is in the foreground."
 *
 * @see <a href="https://developer.android.com/about/versions/oreo/background#services">Android 26 "O" Background Service Limitations</a>
 * @see <a href="https://developer.android.com/guide/components/bound-services#Messenger">Bound services with Messenger</a>
 */
public class WakeUpService extends IntentService {

    private static final String TAG = "WakeUpService";

    public WakeUpService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        StartReceiver.start(this);
    }

    /**
     * This is a do nothing {@link IBinder} instance since binding here is just
     * used to start this app no matter what background restrictions are active.
     */
    private static final IBinder I_BINDER = new Messenger(new Handler()).getBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return I_BINDER;
    }
}
