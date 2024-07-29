package com.garethevans.church.opensongtablet.bible;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.databinding.BottomSheetBibleOnlineBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class BibleGatewayBottomSheet extends BottomSheetDialogFragment {

    // This is used to query the BibleGateway site and grab the desired scripture for a new set item

    private BottomSheetBibleOnlineBinding myView;
    private MainActivityInterface mainActivityInterface;
    private CheckInternet checkInternet;
    private Bible bible;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BibleGateway";
    private String webString, website_bible_online_string="", scripture_string="",
            added_to_set_string="", mode_presenter_string="";


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
                BottomSheetBehavior.from(bottomSheet).setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetBibleOnlineBinding.inflate(inflater, container, false);
        myView.dialogHeader.setClose(this);

        prepareStrings();

        myView.dialogHeader.setWebHelp(mainActivityInterface,website_bible_online_string);

        // Set up helpers
        setupHelpers();

        // Set up webview
        setupWebView();

        // Set up the search views
        setupViews(myView.searchOptions);

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            website_bible_online_string = getString(R.string.website_bible_online);
            scripture_string = getString(R.string.scripture);
            added_to_set_string = getString(R.string.added_to_set);
            mode_presenter_string = getString(R.string.mode_presenter);
        }
    }
    private void setupHelpers() {
        checkInternet = mainActivityInterface.getCheckInternet();
        bible = mainActivityInterface.getBible();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        myView.webView.setWebViewClient(new MyClient());
        WebSettings websettings = myView.webView.getSettings();
        websettings.setDisplayZoomControls(true);
        websettings.setJavaScriptEnabled(true);
    }

    private void setupViews(View whichToShow) {
        myView.downloadWiFiOnly.setChecked(mainActivityInterface.getPreferences().getMyPreferenceBoolean("download_wifi_only",true));
        mainActivityInterface.getProcessSong().editBoxToMultiline(myView.content);
        myView.lineLength.setValue(bible.getLineLength());
        myView.lineLength.setHint(String.valueOf(bible.getLineLength()));
        myView.linesPerSlide.setValue(bible.getLinesPerSlide());
        myView.linesPerSlide.setHint(String.valueOf(bible.getLinesPerSlide()));
        myView.verseNumbers.setChecked(bible.getShowVerseNumbers());
        showView(whichToShow);
    }

    private void setupListeners() {
        myView.downloadWiFiOnly.setOnCheckedChangeListener((compoundButton, b) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean("download_wifi_only",b));
        myView.doSearch.setOnClickListener(v -> doSearch());
        myView.webBack.setOnClickListener(v -> myView.webView.goBack());
        myView.webClose.setOnClickListener(v -> showView(myView.searchOptions));
        myView.webGrabText.setOnClickListener(v -> grabText());
        myView.lineLength.addOnChangeListener((slider, value, fromUser) -> {
            bible.setLineLength((int)value);
            myView.lineLength.setHint(String.valueOf((int)value));
            stretchText();
        });
        myView.linesPerSlide.addOnChangeListener((slider, value, fromUser) -> {
            bible.setLinesPerSlide((int)value);
            myView.linesPerSlide.setHint(String.valueOf((int)value));
            stretchText();
        });
        myView.verseNumbers.setOnCheckedChangeListener((compoundButton, b) -> {
            bible.setShowVerseNumbers(b);
            stretchText();
        });
        myView.addToSet.setOnClickListener(v -> {
            // Build an array list to add to a custom slide
            ArrayList<String> scripture = new ArrayList<>();
            // The first item identifies the arraylist as a scripture
            scripture.add("scripture");
            // Now add the title
            scripture.add(myView.title.getText().toString());
            // Now the text
            scripture.add(myView.content.getText().toString());
            // Now the translation
            scripture.add(myView.translation.getText().toString());

            // Add to the set
            mainActivityInterface.getCustomSlide().buildCustomSlide(scripture);
            mainActivityInterface.getCustomSlide().addItemToSet(false);
            mainActivityInterface.getShowToast().doItBottomSheet(scripture_string+" "+added_to_set_string,myView.getRoot());
            if (!mainActivityInterface.getMode().equals(mode_presenter_string)) {
                mainActivityInterface.navHome();
            }
            dismiss();
        });
    }

    private void showView(View whichToShow) {
        myView.progressBar.setVisibility(View.GONE);
        myView.searchOptions.setVisibility(View.GONE);
        myView.searchResults.setVisibility(View.GONE);
        myView.webView.setVisibility(View.GONE);
        myView.addToSet.setVisibility(View.GONE);
        if (whichToShow == myView.webView) {
            myView.webControls.setVisibility(View.VISIBLE);
        } else {
            myView.webControls.setVisibility(View.GONE);
        }
        if (whichToShow == myView.searchResults) {
            myView.addToSet.setVisibility(View.VISIBLE);
        } else {
            myView.addToSet.setVisibility(View.GONE);
        }
        whichToShow.setVisibility(View.VISIBLE);
    }

    private void doSearch() {
        if (getContext()!=null) {
            Log.d(TAG, "isNetworkConnected:" + checkInternet.isNetworkConnected(getContext(), mainActivityInterface));
        }
        // Get the search text
        String searchText = "";
        String versionCode = "";
        if (myView.searchPhrase.getText()!=null) {
            try {
                searchText = URLEncoder.encode(myView.searchPhrase.getText().toString(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                searchText = myView.searchPhrase.getText().toString();
            }
        }
        if (myView.versionCode.getText()!=null && !myView.versionCode.getText().toString().isEmpty()) {
            try {
                versionCode = URLEncoder.encode(myView.versionCode.getText().toString(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                versionCode = myView.versionCode.getText().toString();
            }
        }
        // Prepare the web url
        String url = "https://www.biblegateway.com/quicksearch/?quicksearch=" + searchText + "&version=" + versionCode;
        myView.webView.loadUrl(url);

        myView.webGrabText.hide();
        showView(myView.webView);
    }

    private class MyClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view,url,favicon);
            myView.progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view,String Url) {
            view.loadUrl(Url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view,String url) {
            super.onPageFinished(view,url);
            myView.progressBar.setVisibility(View.GONE);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                webString = "";
                myView.webView.post(() -> {
                    try {
                        myView.webView.evaluateJavascript("javascript:document.getElementsByTagName('html')[0].innerHTML", webContent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

    private void grabText() {
        // First get the bible verse and version
        int referenceStart = webString.indexOf("<meta name=\"twitter:title\" content=\"");
        if (referenceStart>-1) {
            int referenceEnd = webString.indexOf(">", referenceStart + 36);
            if (referenceEnd>-1) {
                String reference = webString.substring(referenceStart + 36, referenceEnd);
                String[] bits = reference.split("\\(");
                myView.title.setText(bits[0]);
                if (bits.length > 1) {
                    String trans = bits[1].replace(")", "");
                    trans = trans.replace("\"","");
                    trans = trans.trim();
                    myView.translation.setText(trans);
                }
            }
        }

        int start = webString.indexOf("<div class=\"passage-content");
        if (start>-1) {
            webString = webString.substring(start);
            int end = webString.indexOf("</div");
            if (end>-1) {
                webString = webString.substring(0,end);
                // Identify the chapter numbering (so we keep)
                webString = webString.replace("<span class=\"chapternum\">","_STARTOFVERSE_");
                webString = webString.replace("&nbsp;</span>","_ENDOFVERSE_");

                // Identify the verse numbering (so we keep)
                webString = webString.replace("<sup class=\"versenum\">","_STARTOFVERSE_");
                webString = webString.replace("&nbsp;</sup>","_ENDOFVERSE_");

                // For headings, start new lines
                webString = webString.replace("<h3>","\n\n");
                webString = webString.replace("</h3>","\n");
                webString = webString.replace("<p>","\n");
                // Get rid of any other href bracket stuff
                boolean keepGoing = true;
                String trimBit;
                int trimStart;
                int trimEnd;
                webString = webString.replace("(<","<");
                webString = webString.replace(")>",">");
                while (webString.contains("<a ") && webString.contains("</a>") && keepGoing) {
                    trimStart = webString.indexOf("<a ");
                    if (trimStart>-1) {
                        trimEnd = webString.indexOf("</a>",trimStart);
                        if (trimEnd>-1) {
                            trimBit = webString.substring(trimStart,trimEnd+5);
                            webString = webString.replace(trimBit,"");
                        } else {
                            keepGoing = false;
                        }
                    } else {
                        keepGoing = false;
                    }
                }
                webString = webString.replace("()","");

                // Get rid of html tags
                webString = mainActivityInterface.getProcessSong().removeHTMLTags(webString);

                // Now replace the verse identifiers
                webString = webString.replace("_STARTOFVERSE_"," {");
                webString = webString.replace("_ENDOFVERSE_","}");

                webString = webString.trim();

                stretchText();
                showView(myView.searchResults);
            }
        }

    }

    private final ValueCallback<String> webContent = value -> {
        JsonReader reader = new JsonReader(new StringReader(value));
        reader.setLenient(true);

        try {
            if (reader.peek() == JsonToken.STRING) {
                webString = reader.nextString();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        webString = webString.replace("\r","\n");

        showSaveButton();
    };

    private void showSaveButton() {
        if (webString.contains("biblegateway.com/passage") && webString.contains("<div class=\"passage-text\">")) {
            myView.webGrabText.show();
        } else {
            myView.webGrabText.hide();
        }
    }

    private void stretchText() {
        mainActivityInterface.getProcessSong().splitTextByMaxChars(myView.content,webString,
                bible.getLineLength(),bible.getLinesPerSlide(),bible.getShowVerseNumbers());
    }
}
