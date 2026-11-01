package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class ScoreToAbcExtractor {

    private static final String TAG = "ScoreToAbcExtractor";
    private static final String WEB_APP_URL = "https://thebigg73.github.io/GP2ABCStudio/";

    public interface ExtractionCallback {
        void onSuccess(String abcNotation);
        void onError(String errorMessage);
    }

    private final Context context;
    private WebView headlessWebView;
    private boolean isReady = false;
    private ExtractionCallback currentCallback;

    public ScoreToAbcExtractor(Context context) {
        this.context = context.getApplicationContext();
        initEngine();
    }

    private void initEngine() {
        new Handler(Looper.getMainLooper()).post(() -> {
            headlessWebView = new WebView(context);
            WebSettings settings = headlessWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);

            // Forward Javascript console output to Logcat
            headlessWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d(TAG, "WebView JS: " + consoleMessage.message() + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                    return super.onConsoleMessage(consoleMessage);
                }
            });

            // Register bridge interface matching the web app's expected window.AndroidBridge
            headlessWebView.addJavascriptInterface(new JSBridge(), "AndroidBridge");

            headlessWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    isReady = true;
                    Log.d(TAG, "Connected to hosted web app at: " + WEB_APP_URL);
                }
            });

            // Load directly from your live GitHub Pages URL
            headlessWebView.loadUrl(WEB_APP_URL);
        });
    }

    public void extractFromStream(InputStream inputStream, String fileName, ExtractionCallback callback) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int nRead;
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();

                Log.d(TAG, "Sending " + fileName + " (" + bytes.length + " bytes) to hosted web app...");
                String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (!isReady) {
                        Log.e(TAG, "Extraction requested before web app page finished loading");
                        callback.onError("Web app is still loading...");
                        return;
                    }
                    this.currentCallback = callback;
                    // Directly execute function built into web app
                    headlessWebView.evaluateJavascript("window.convertFileForAndroid('" + base64 + "', '" + fileName + "');", null);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to read score file stream: " + e.getLocalizedMessage(), e);
                callback.onError("Failed to read score file: " + e.getLocalizedMessage());
            }
        }).start();
    }

    private class JSBridge {
        @JavascriptInterface
        public void onAbcExtracted(String abcNotation) {
            Log.d(TAG, "=== EXTRACTED ABC RETURNED FROM HOSTED WEB APP ===");
            Log.d(TAG, abcNotation);
            Log.d(TAG, "=================================================");

            if (currentCallback != null) {
                new Handler(Looper.getMainLooper()).post(() -> currentCallback.onSuccess(abcNotation));
            }
        }

        @JavascriptInterface
        public void onError(String error) {
            Log.e(TAG, "Extraction failed: " + error);
            if (currentCallback != null) {
                new Handler(Looper.getMainLooper()).post(() -> currentCallback.onError(error));
            }
        }
    }
}