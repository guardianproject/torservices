package org.torproject.torservices;

import android.app.Application;
import org.torproject.jni.TorService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TorService.start(this);
    }
}
