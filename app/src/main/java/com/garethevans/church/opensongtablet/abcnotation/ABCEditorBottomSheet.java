package com.garethevans.church.opensongtablet.abcnotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.BottomSheetAbcEditorBinding;
import com.garethevans.church.opensongtablet.customviews.BottomSheetCommon;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentFeatures;
import com.garethevans.church.opensongtablet.songprocessing.EditSongFragmentLyrics;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ABCEditorBottomSheet extends BottomSheetCommon {

    private final String TAG = "ABCEditorBS";
    private MainActivityInterface mainActivityInterface;
    private BottomSheetAbcEditorBinding myView;
    private final String what;
    private final Fragment frag;

    private WebView webView;
    private final Song song;

    public ABCEditorBottomSheet(Fragment frag, Song song, String what) {
        this.frag = frag;
        this.what = what;
        this.song = song;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetAbcEditorBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Set up views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        if (getContext()!=null) {
            webView = myView.abcWebView;
            if (what.equals("abc")) {
                myView.copyInlineAbc.setVisibility(View.GONE);
            } else {
                myView.copyAbc.setVisibility(View.GONE);
            }
            setupWebView();
        }
    }

    private void setupListeners() {
        myView.copyAbc.setOnClickListener(view -> {
            Log.d(TAG,"clicking copyAbc()");
            webView.evaluateJavascript("getABC()", resultValue -> {
                // evaluateJavascript returns the result as a JSON-encoded string (e.g., "\"X:1...\"")
                if (resultValue != null && !resultValue.equals("null")) {
                    try {
                        // Unquote the result to get the raw ABC text string
                        String finalAbc = new org.json.JSONTokener(resultValue).nextValue().toString().trim();

                        Log.d(TAG,"finalAbc:"+finalAbc);

                        try {
                            ((EditSongFragmentFeatures) frag).updateAbc(finalAbc);
                            dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        myView.copyInlineAbc.setOnClickListener(view -> {
            Log.d(TAG,"clicking copyInlineAbc()");
            webView.evaluateJavascript("getABC()", resultValue -> {
                // evaluateJavascript returns the result as a JSON-encoded string (e.g., "\"X:1...\"")
                if (resultValue != null && !resultValue.equals("null")) {
                    try {
                        // Unquote the result to get the raw ABC text string
                        String finalAbc = new org.json.JSONTokener(resultValue).nextValue().toString().trim();

                        Log.d(TAG,"finalAbc:"+finalAbc);

                        try {
                            ((EditSongFragmentLyrics) frag).insertInlineAbc(finalAbc);
                            dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (getContext() != null) {
            ABCWebViewJSInterface abcWebViewJSInterface = new ABCWebViewJSInterface(getContext());

            // For logging
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    android.util.Log.d("WebView", consoleMessage.message());
                    return true;
                }
            });

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d(TAG,"Passing in current abc notation:"+mainActivityInterface.getTempSong().getAbc());

                    // Get the correct headers if they exist for the song
                    String tempo = DrumCalculations.getFixedTempoString(mainActivityInterface.getTempSong().getTempo(), true);
                    String timesig = DrumCalculations.getFixedTimeSignatureString(mainActivityInterface.getTempSong().getTimesig(), true);
                    String val = timesig.endsWith("/8") ? "3/8" : "1/4";
                    String key = song.getKey()==null ? "" : song.getKey();

                    String titleHeader = "T:" + song.getTitle() + "\n";
                    String tempoHeader = "Q:" + val + "=" + tempo + "\n";
                    String keyHeader = "K:" + key + "\n";
                    String timeSigHeader = "M:" + timesig + "\n";

                    String currentAbc = song.getAbc();
                    if (currentAbc==null) {
                        currentAbc = "";
                    }

                    if (what.equals("abc")) {
                        // Add the headers if we don't already have them

                        if (!currentAbc.contains("Q:")) {
                            currentAbc = tempoHeader + currentAbc;
                        }
                        if (!currentAbc.contains("M:")) {
                            currentAbc = timeSigHeader + currentAbc;
                        }
                        if (!currentAbc.contains("K:") && !key.isEmpty()) {
                            currentAbc = keyHeader + currentAbc;
                        }
                        if (!currentAbc.contains("T:")) {
                            currentAbc = titleHeader + currentAbc;
                        }
                    } else {
                        currentAbc = "";
                        if (!key.isEmpty()) {
                            currentAbc = keyHeader + currentAbc;
                        }
                        if (!timesig.isEmpty()) {
                            currentAbc = timeSigHeader + currentAbc;
                        }
                    }

                    // Properly JSON-encode the string to handle quotes and backslashes safely
                    String safeAbcJson = org.json.JSONObject.quote(currentAbc);

                    // Call the JS function once the page is fully loaded
                    webView.evaluateJavascript("loadABC(" + safeAbcJson + ");", null);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    Log.d(TAG, "error:"+error);
                }

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                    Log.d("WebView", "crash");
                    destroyWebView();
                    setupWebView();
                    return true; // The app continues executing.
                }
            });

            webView.getSettings().getJavaScriptEnabled();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(false);

            webView.addJavascriptInterface(abcWebViewJSInterface,"AndroidBridge");

            String darkPref = "&dark=";
            if (mainActivityInterface.getMyThemeColors().getThemeName().equals("dark") ||
            mainActivityInterface.getMyThemeColors().getThemeName().equals("custom1")) {
                darkPref = darkPref + "1";
            } else {
                darkPref = darkPref + "0";
            }
            webView.loadUrl("https://abceditor.justchords.app/?hideHeader=1"+darkPref);
        }
    }

    // Separate additional overrides not dealt with in BottomSheetCommon
    @Override
    public void onStart() {
        super.onStart();
        // Find the standard design bottom sheet container
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Force the layout height to match parent (full screen)
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                // Configure the behavior
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // Prevents snapping to a half-expanded state
                behavior.setFitToContents(true);
            }
        }
    }

    public void destroyWebView() {
        try {
            if (webView!=null) {
                webView.clearHistory();

                // NOTE: clears RAM cache, if you pass true, it will also clear the disk cache.
                // Probably not a great idea to pass true if you have other WebViews still alive.
                webView.clearCache(true);

                // Loading a blank page is optional, but will ensure that the WebView isn't doing anything when you destroy it.
                webView.loadUrl("about:blank");

                webView.onPause();
                webView.removeAllViews();

                // NOTE: This pauses JavaScript execution for ALL WebViews,
                // do not use if you have other WebViews still alive.
                // If you create another WebView after calling this,
                // make sure to call mWebView.resumeTimers().
                webView.pauseTimers();

                // NOTE: This can occasionally cause a segfault below API 17 (4.2)
                webView.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Null out the reference so that you don't end up re-using it.
        webView = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
