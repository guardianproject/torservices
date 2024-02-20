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

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;

import java.util.concurrent.Executor;
import androidx.appcompat.app.AppCompatActivity;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import info.guardianproject.netcipher.proxy.StatusCallback;

/**
 * A simple web page viewer that only loads the page when Tor is ready.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static BroadcastReceiver loadUrlReceiver;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final TextView statusTextView = findViewById(R.id.status);

        OrbotHelper.get(this).addStatusCallback(new StatusCallback() {
            @Override
            public void onEnabled(Intent statusIntent) {
                setProxy();
                webView.loadUrl("https://check.torproject.org/");
            }

            @Override
            public void onStarting() {
                statusTextView.setText("starting....");

            }

            @Override
            public void onStopping() {
                statusTextView.setText("stopping....");

            }

            @Override
            public void onDisabled() {
                statusTextView.setText("disable....");

            }

            @Override
            public void onStatusTimeout() {
                statusTextView.setText("timeout....");

            }

            @Override
            public void onNotYetInstalled() {
                statusTextView.setText("not installed....");

            }
        });

        webView = findViewById(R.id.webview);
        findViewById(R.id.statusButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("about:blank");
                statusTextView.setText("loading...");
                OrbotHelper.get(MainActivity.this).requestStartTor(getApplicationContext());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProxy();
                        webView.loadUrl("https://check.torproject.org/");
                    }
                });
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
                        statusTextView.setText(status);
                        if (OrbotHelper.STATUS_ON.equals(status)) {
                            setProxy();
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
        webView.loadUrl("https://check.torproject.org/");

    }

    private void setProxy() {
        ProxyConfig proxyConfig = new ProxyConfig.Builder()
                .addProxyRule("127.0.0.1:8118")
                .addDirect().build();
        ProxyController.getInstance().setProxyOverride(proxyConfig, new Executor() {
            @Override
            public void execute(Runnable command) {
                //do nothing
            }
        }, new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadUrlReceiver != null) {
            unregisterReceiver(loadUrlReceiver);
        }
    }
}
