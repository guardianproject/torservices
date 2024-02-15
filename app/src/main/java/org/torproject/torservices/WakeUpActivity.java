package org.torproject.torservices;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.torproject.jni.TorService;

/**
 * This is needed as a shim between the Android background {@link android.app.Service}
 * restrictions, and the {@link TorService#ACTION_START} {@link Intent}s that
 * come from any app requesting to use Tor.  It is easy to start an
 * another app's {@link AppCompatActivity} if running in an {@code Activity}.
 *
 * @see <a href="https://developer.android.com/about/versions/oreo/background">Android 26 "O" Background Execution Limits</a>
 * @see <a href="https://developer.android.com/guide/components/activities/background-starts">Restrictions on starting activities from the background</a>
 */
public class WakeUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartReceiver.start(this);
        finish();
    }
}
