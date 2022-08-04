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

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

class GenericWebViewClient extends WebViewClient {

    private int requestCounter;

    private MainActivity mainActivity;

    public GenericWebViewClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        requestCounter = 0;
    }

    interface RequestCounterListener {
        void countChanged(int requestCount);
    }

    private volatile RequestCounterListener requestCounterListener = null;

    public void setRequestCounterListener(RequestCounterListener requestCounterListener) {
        this.requestCounterListener = requestCounterListener;
    }

}