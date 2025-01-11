package com.garethevans.church.opensongtablet.abcnotation;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class ABCWebViewJSInterface {

    // This is triggered from the abc.html file after the WebView has drawn and measured
    private final MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ABCWebViewJSInterface";

    public ABCWebViewJSInterface(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    @JavascriptInterface
    public void returnSize(int webViewItem, int width, int height, String abcSvgText, boolean isPopup) {
        boolean isfinished = true;
        if (isPopup) {
            // Is a popup window, so deal with that via the abcNotation fragment
            // We don't use the object arrays for inline objects
            mainActivityInterface.getAbcNotation().allowPopupToContinue(width,height);

        } else {
            for (int x = 0; x < mainActivityInterface.getAbcNotation().countInlineAbcObjects(); x++) {
                InlineAbcObject inlineAbcObject = mainActivityInterface.getAbcNotation().getInlineAbcObjects().get(x);
                if (inlineAbcObject.getIsPopup()) {
                    isPopup = true;
                }
                if (height > 0 && inlineAbcObject.getAbcItem() == webViewItem) {
                    inlineAbcObject.setAbcWidth(width);
                    inlineAbcObject.setAbcHeight(height);
                    inlineAbcObject.setAbcSvgText(abcSvgText);
                    inlineAbcObject.setAbcMeasured(true);
                }
                if (inlineAbcObject.getAbcHeight() <= 1 || !inlineAbcObject.getAbcMeasured()) {
                    isfinished = false;
                }

            }
            if (isfinished) {
                // Now pass the abcWebViewProperties to the performance fragment
                // If this is from the exportFragment, send info back there
                boolean wasExport = mainActivityInterface.getAbcNotation().getExportFragment()!=null;
                if (wasExport) {
                    try {
                        mainActivityInterface.getMainHandler().postDelayed(() -> {
                                mainActivityInterface.getAbcNotation().setAbcWebViewsDrawn(true);
                                mainActivityInterface.getAbcNotation().getExportFragment().abcFinished();
                    }, 200);
                    } catch (Exception e) {
                        wasExport = false;
                    }
                }
                if (!wasExport && mainActivityInterface.getPerformanceValid()) {
                    // All good, so sending to Performance Fragment after a short delay for final measurements
                    mainActivityInterface.getMainHandler().postDelayed(() -> mainActivityInterface.getPerformanceFragment().inlineAbcWebViewsDrawn(), 200);
                }
            }
        }
    }

}
