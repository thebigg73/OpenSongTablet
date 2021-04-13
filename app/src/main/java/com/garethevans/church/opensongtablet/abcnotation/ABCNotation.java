package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class ABCNotation {

    @SuppressLint("SetJavaScriptEnabled")
    public void setWebView(WebView webView, Song song, boolean edit) {
        Log.d("d","song.getAbc()="+song.getAbc());
        webView.post(new Runnable() {
            @Override
            public void run() {
                //String newUA = "Mozilla/5.0 (X11; Linux x86_64; rv:54.0) Gecko/20100101 Firefox/54.0";
                //webView.getSettings().setUserAgentString(newUA);
                webView.getSettings().getJavaScriptEnabled();
                webView.getSettings().setJavaScriptEnabled(true);
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
                        Log.d("MyApplication", consoleMessage.message() + " -- From line "
                                + consoleMessage.lineNumber() + " of "
                                + consoleMessage.sourceId());
                        return super.onConsoleMessage(consoleMessage);
                    }
                });
                webView.setWebViewClient(new MyWebViewClient(song,edit) {

                });
                webView.loadUrl("file:///android_asset/ABC/abc.html");
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {
        Song song;
        boolean edit;
        MyWebViewClient(Song song, boolean edit) {
            this.song = song;
            this.edit = edit;
        }
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);

            if (song.getAbc().isEmpty()) {
                updateContent(webView,getSongInfo(song),edit);
            } else {
                updateContent(webView,song.getAbc(),edit);
            }

            if (edit) {
                webView.loadUrl("javascript:displayAndEdit();");
            } else {
                webView.loadUrl("javascript:displayOnly();");
            }
        }
    }

    String getSongInfo(Song song) {
        String info = "";
        // Add the song time signature
        if (song.getTimesig().isEmpty()) {
            info += "M:4/4\n";
        } else {
            info += "M:" + song.getTimesig() + "\n";
        }
        // Add the note length
        info += "L:1/8\n";

        // Add the song key
        if (song.getKey().isEmpty()) {
            info += "K:C treble %treble or bass clef\n";
        } else {
            info += "K: " + song.getKey() + " %treble or bass clef\n";
        }
        info += "|";
        return info;
    }

    private void updateContent(WebView webView, String newContent, boolean edit) {
        try {
            newContent = Uri.encode(newContent, "UTF-8");
        } catch  (Exception e) {
            Log.d("d","Error encoding");
        }
        Log.d("d","newContent="+newContent);
        webView.evaluateJavascript("javascript:updateABC('"+newContent+"');",null);
        if (edit) {
            webView.loadUrl("javascript:displayOnly();");
        } else {
            webView.loadUrl("javascript:displayAndEdit();");
        }
    }
}
