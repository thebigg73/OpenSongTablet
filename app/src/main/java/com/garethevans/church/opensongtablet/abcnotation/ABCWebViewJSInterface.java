package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.garethevans.church.opensongtablet.customviews.InlineAbcWebView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.secondarydisplay.SecondaryDisplay;

public class ABCWebViewJSInterface {

    private final MainActivityInterface mainActivityInterface;
    private final String TAG = "ABCWebViewJSInterface";

    public ABCWebViewJSInterface(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    @JavascriptInterface
    public void returnSize(int webViewItem, int width, int height) {
        Log.d(TAG, "returning size for song(" + webViewItem + "):" + width + "x" + height + "  count:" + mainActivityInterface.getInlineAbcWebViews().size());
        boolean isfinished = true;
        for (int x = 0; x < mainActivityInterface.countInlineAbcWebViews(); x++) {
            InlineAbcWebView inlineAbcWebView = mainActivityInterface.getInlineAbcWebViews().get(x);
            Log.d(TAG, "inlineAbcWebView.getWebViewItem():" + inlineAbcWebView.getWebViewItem() + "  webViewItem(" + webViewItem + "):" + width + "x" + height);
            if (height > 0 && inlineAbcWebView.getWebViewItem() == webViewItem) {
                inlineAbcWebView.setWebViewMeasured(true);
                inlineAbcWebView.setWebViewWidth(width);
                inlineAbcWebView.setWebViewHeight(height);
            }
            if (inlineAbcWebView.getWebViewHeight() <= 1 || !inlineAbcWebView.getWebViewMeasured()) {
                isfinished = false;
            }
        }
        if (isfinished) {
            // Now pass the abcWebViewProperties to the performance fragment
            if (mainActivityInterface.getPerformanceValid()) {
                // All good, so sending to Performance Fragment after a short delay for final measurements
                mainActivityInterface.getMainHandler().postDelayed(() -> mainActivityInterface.getPerformanceFragment().inlineAbcWebViewsDrawn(), 200);
            }
        }
    }

    @JavascriptInterface
    public void returnSizeSecondary(int webViewItem, int width, int height) {
        Log.d(TAG, "returnSizeSecondary(" + webViewItem + "):" + width + "x" + height);
        boolean isfinished = true;
        for (int x = 0; x < mainActivityInterface.countInlineAbcWebViewsSecondary(); x++) {
            InlineAbcWebView inlineAbcWebView = mainActivityInterface.getInlineAbcWebViewsSecondary().get(x);
            if (height > 0 && inlineAbcWebView.getWebViewItem() == webViewItem) {
                inlineAbcWebView.setWebViewMeasured(true);
                inlineAbcWebView.setWebViewWidth(width);
                inlineAbcWebView.setWebViewHeight(height);
            }
            if (inlineAbcWebView.getWebViewHeight() <= 1 || !inlineAbcWebView.getWebViewMeasured()) {
                isfinished = false;
            }
        }
        Log.d(TAG, "isfinished:" + isfinished);
        if (isfinished) {
            // Send this to the secondary display if possible
            for (SecondaryDisplay secondaryDisplay : mainActivityInterface.getSecondaryDisplays()) {
                mainActivityInterface.getMainHandler().postDelayed(secondaryDisplay::inlineAbcWebViewsDrawnSecondary, 200);
            }
        }
    }
}

