package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ABCNotation {

    private final String TAG = "ABCNotation";

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebView(WebView webView, MainActivityInterface mainActivityInterface,
                           boolean edit) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.getSettings().getJavaScriptEnabled();
                webView.getSettings().setJavaScriptEnabled(true);
                webView.setInitialScale(1);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setLoadWithOverviewMode(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setDisplayZoomControls(false);
                webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
                webView.setScrollbarFadingEnabled(false);
                webView.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        return super.onConsoleMessage(consoleMessage);
                    }
                });
                webView.setWebViewClient(new MyWebViewClient(mainActivityInterface,edit) {

                });
                webView.loadUrl("file:///android_asset/ABC/abc.html");
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {
        MainActivityInterface mainActivityInterface;
        boolean edit;
        MyWebViewClient(MainActivityInterface mainActivityInterface, boolean edit) {
            this.mainActivityInterface = mainActivityInterface;
            this.edit = edit;
        }
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);

            if (mainActivityInterface.getSong().getAbc().isEmpty()) {
                updateContent(mainActivityInterface,webView,getSongInfo(mainActivityInterface),edit);
            } else {
                updateContent(mainActivityInterface,webView,mainActivityInterface.getSong().getAbc(),edit);
            }

            if (edit) {
                webView.loadUrl("javascript:displayAndEdit();");
            } else {
                webView.loadUrl("javascript:displayOnly();");
            }
        }
    }

    String getSongInfo(MainActivityInterface mainActivityInterface) {
        String info = "";
        // Add the song time signature
        if (mainActivityInterface.getSong().getTimesig().isEmpty()) {
            info += "M:4/4\n";
        } else {
            info += "M:" + mainActivityInterface.getSong().getTimesig() + "\n";
        }
        // Add the note length
        info += "L:1/8\n";

        // Add the song key
        if (mainActivityInterface.getSong().getKey().isEmpty()) {
            info += "K:C treble %treble or bass clef\n";
        } else {
            info += "K: " + mainActivityInterface.getSong().getKey() + " %treble or bass clef\n";
        }
        info += "|";
        return info;
    }

    private void updateContent(MainActivityInterface mainActivityInterface,
                               WebView webView, String newContent, boolean edit) {
        try {
            newContent = Uri.encode(newContent, "UTF-8");
        } catch  (Exception e) {
            e.printStackTrace();
        }

        String notation = String.format("#%08X", (mainActivityInterface.getMyThemeColors().getLyricsTextColor()));
        String page = String.format("#%08X", (mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor()));

        Log.d(TAG,"notation: "+notation+"   page: "+page);

        if (edit) {
            webView.loadUrl("javascript:displayOnly();");
            webView.loadUrl("javascript:setWidth("+(int)(mainActivityInterface.getDisplayMetrics()[0] *
                            mainActivityInterface.getPreferences().getMyPreferenceFloat("abcPopupWidth",0.95f))+");");
        } else {
            webView.loadUrl("javascript:displayAndEdit();");
            webView.loadUrl("javascript:setWidth("+mainActivityInterface.getDisplayMetrics()[0]+");");
        }

        webView.evaluateJavascript("javascript:updateABC('"+newContent+"');",null);

    }
}
