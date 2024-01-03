package com.garethevans.church.opensongtablet.songprocessing;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetBpmBinding;
import com.garethevans.church.opensongtablet.importsongs.MyJSInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.io.StringReader;

public class GetBPMBottomSheet extends BottomSheetDialogFragment {

    // Use tunebat: https://tunebat.com/Search?q=

    // This tries to extract the BPM, key and length of the song from songbpm.com
    // To search, the format is "https://songbpm.com/@ARTIST/TITLE"
    // Artist and title replace spaces with -

    // Once we get the search web text back, we get the start string: "Song Metrics</h3>"
    // The useful stuff ends at the first instance of "<a href=\""

    // To get the key, look for "Key</dt><dd".  Ends by next instance of "</dd>"
    // To get the duration, look for "Duration</dt><dd".  Ends by next instance of "</dd>" (formatted as min:secs)
    // To get the tempo, look for "Tempo</dt><dd".  Ends by next instance of "</dd>"

    // Open the fragment up, display the waiting progress bar, do the search and process it
    // Once details have been collected, display the options to put these values into the song

    private final EditSongFragmentFeatures openingFragment;

    public GetBPMBottomSheet() {
        // Default constructor required to avoid re-instantiation failures
        // Just close the bottom sheet
        openingFragment = null;
        dismiss();
    }

    GetBPMBottomSheet(EditSongFragmentFeatures openingFragment) {
        this.openingFragment = openingFragment;
    }

    private BottomSheetBpmBinding myView;
    private MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "GetBPMBottomSheet";
    private String webString, not_found_string="";
    private String key = "";
    private String duration = "";
    private int durationMins = -1;
    private int durationSecs = -1;
    private int tempo = -1;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetBpmBinding.inflate(inflater, container, false);

        prepareStrings();

        myView.dialogHeading.setClose(this);

        // Show the progress bar and hide the results for now
        myView.progressBar.setVisibility(View.VISIBLE);
        myView.gridLayout.setVisibility(View.GONE);

        // Now try the search and extract in a new thread
        searchAndExtract();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            not_found_string = getString(R.string.not_available);
        }
    }

    private void searchAndExtract() {
        // We have already checked for connection, so set up the webView
        mainActivityInterface.getThreadPoolExecutor().execute(this::setupWebView);

    }


    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (getContext() != null) {
            WebView webView = myView.webView;
            myView.webView.post(() -> {
                myView.webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                return false;
                            }

                            @Override
                            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                super.onPageStarted(view, url, favicon);
                            }

                            @Override
                            public void onPageFinished(WebView view, String url) {
                                super.onPageFinished(view, url);
                                // Run a check for the desired content
                                webView.evaluateJavascript("javascript:document.getElementsByTagName('html')[0].innerHTML", webContent);
                            }

                            @Override
                            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                                super.onReceivedError(view, request, error);
                            }

                            @Override
                            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                                Log.d("WebView", "crash");
                                return true; // The app continues executing.
                            }
                        });
                webView.getSettings().getJavaScriptEnabled();
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.addJavascriptInterface(new MyJSInterface(getContext(), this), "HTMLOUT");

                String webSearchFull = "https://tunebat.com/Search?q=";
                if (!mainActivityInterface.getTempSong().getAuthor().isEmpty()) {
                    webSearchFull += mainActivityInterface.getTempSong().getAuthor().replace(" ","+") + "+";
                }
                if (!mainActivityInterface.getTempSong().getTitle().isEmpty()) {
                    webSearchFull += mainActivityInterface.getTempSong().getTitle().replace(" ","+");
                }

                Log.d(TAG,"webSearchFull:"+webSearchFull);
                webView.loadUrl(webSearchFull);
            });
        }
    }

    private final ValueCallback<String> webContent = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            //Log.d(TAG,"value:"+value);
            JsonReader reader = new JsonReader(new StringReader(value));
            reader.setLenient(true);

            // Get a handler for the UI updates/queries
            Handler handler = new Handler(Looper.getMainLooper());

            try {
                if (reader.peek() == JsonToken.STRING) {
                    webString = reader.nextString();
                    //Log.d(TAG,"webString:"+webString);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            webString = webString.replace("\r","\n");
            webString = webString.replace("><",">\n<");

            for (String s:webString.split("\n")) {
                Log.d(TAG,"s:"+s);
            }

            // Now try to extract what we need...
            // Trim to extract the song metrics
            if (webString.contains("Song Metrics</h3>")) {
                webString = webString.substring(webString.indexOf("Song Metrics</h3>"));
                // Extract up to the first link
                if (webString.contains("<a href=\"")) {
                    webString = webString.substring(0,webString.indexOf("<a href=\""));
                }
            }

            // Look for the key
            if (webString.contains("Key</dt><dd") && webString.contains("</dd>")) {
                int start = webString.indexOf("Key</dt><dd");
                int end = webString.indexOf("</dd>",start);
                if (end>start) {
                    key = webString.substring(start,end);
                    key = mainActivityInterface.getProcessSong().removeHTMLTags(key);
                    // If key contains "/" it could have flat/sharp.  Get the first one
                    if (key.contains("/")) {
                        key = key.substring(0,key.indexOf("/"));
                    }
                    // Now get the user's preferred key
                    key = mainActivityInterface.getTranspose().convertToPreferredChord(key);
                }
            }

            // Look for the song duration
            if (webString.contains("Duration</dt><dd") && webString.contains("</dd>")) {
                int start = webString.indexOf("Duration</dt><dd");
                int end = webString.indexOf("</dd>",start);
                if (end>start) {
                    duration = webString.substring(start,end);
                    duration = mainActivityInterface.getProcessSong().removeHTMLTags(duration);
                    if (duration.contains(":")) {
                        String[] durationBits = duration.split(":");
                        if (durationBits.length==2) {
                            durationBits[0] = durationBits[0].replaceAll("\\D","");
                            if (!durationBits[0].isEmpty()) {
                                durationMins = Integer.parseInt(durationBits[0]);
                            }
                            durationBits[1] = durationBits[1].replaceAll("\\D","");
                            if (!durationBits[1].isEmpty()) {
                                durationSecs = Integer.parseInt(durationBits[1]);
                            }
                        }
                    }
                }
            }


            // Look for the tempo
            if (webString.contains("Tempo</dt><dd") && webString.contains("</dd>")) {
                int start = webString.indexOf("Tempo</dt><dd");
                int end = webString.indexOf("</dd>",start);
                if (end>start) {
                    String tempoString = webString.substring(start,end);
                    tempoString = mainActivityInterface.getProcessSong().removeHTMLTags(tempoString);
                    tempoString = tempoString.replaceAll("\\D","");
                    if (!tempoString.isEmpty()) {
                        tempo = Integer.parseInt(tempoString);
                    }
                }
            }

            // Now write the found values and hide the non-found
            boolean keyFound = !key.isEmpty();
            boolean durationFound = durationSecs>-1;
            boolean tempoFound = tempo>-1;

            if (durationFound && durationMins==-1) {
                durationMins = 0;
            }
            // Do this with the UI handler
            handler.post(() -> {
                if (myView!=null) {
                    myView.keyInfo.setHint(keyFound ? key : not_found_string);
                    myView.durationInfo.setHint(durationFound ? duration : not_found_string);
                    myView.tempoInfo.setHint(keyFound ? String.valueOf(tempo) : not_found_string);
                    myView.keyButton.setVisibility(keyFound ? View.VISIBLE : View.GONE);
                    myView.durationButton.setVisibility(durationFound ? View.VISIBLE : View.GONE);
                    myView.tempoButton.setVisibility(tempoFound ? View.VISIBLE : View.GONE);

                    myView.keyButton.setOnClickListener((button) -> updateKey());
                    myView.tempoButton.setOnClickListener((button) -> updateTempo());
                    myView.durationButton.setOnClickListener((button) -> updateDuration());

                    myView.progressBar.setVisibility(View.GONE);
                    myView.gridLayout.setVisibility(View.VISIBLE);
                }
            });
        }
    };

    private void updateKey() {
        Log.d(TAG,"updateKey:"+key);
        if (openingFragment!=null) {
            openingFragment.updateKey(key);
        }
    }

    private void updateTempo() {
        Log.d(TAG,"updateTempo:"+tempo);
        if (openingFragment!=null) {
            openingFragment.updateTempo(tempo);
        }
    }

    private void updateDuration() {
        Log.d(TAG,"updateDuration:"+durationMins+":"+durationSecs);
        if (openingFragment!=null) {
            openingFragment.updateDuration(durationMins, durationSecs);
        }
    }
}
