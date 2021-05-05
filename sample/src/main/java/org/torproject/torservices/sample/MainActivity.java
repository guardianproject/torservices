/*
 * Copyright (c) 2018 Michael Pöhn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.torproject.torservices.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import info.guardianproject.netcipher.proxy.OrbotHelper;

/**
 * A simple web page viewer that only loads the page when Tor is ready.
 */
public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

    private static BroadcastReceiver loadUrlReceiver;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        final TextView statusTextView = findViewById(R.id.status);
        findViewById(R.id.statusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("about:blank");
                statusTextView.setText("...");
                OrbotHelper.requestStartTor(getApplicationContext());
            }
        });

        loadUrlReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String status = intent.getStringExtra(OrbotHelper.EXTRA_STATUS);
                Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onReceive: " + status + " " + intent);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusTextView.setText("Tor status: " + status);
                        if (OrbotHelper.STATUS_ON.equals(status)) {
                            webView.loadUrl("https://check.torproject.org/");
                        } else {
                            webView.loadUrl("about:blank");
                        }
                    }
                });
            }
        };

        // run the BroadcastReceiver in its own thread
        HandlerThread handlerThread = new HandlerThread(loadUrlReceiver.getClass().getSimpleName());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);
        registerReceiver(loadUrlReceiver, new IntentFilter(OrbotHelper.ACTION_STATUS), null, handler);

        GenericWebViewClient webViewClient = new GenericWebViewClient(this);
        webView.setWebViewClient(webViewClient);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadUrlReceiver != null) {
            unregisterReceiver(loadUrlReceiver);
        }
    }
}
