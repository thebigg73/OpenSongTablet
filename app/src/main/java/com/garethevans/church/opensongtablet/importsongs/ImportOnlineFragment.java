package com.garethevans.church.opensongtablet.importsongs;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsImportOnlineBinding;
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ImportOnlineFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsImportOnlineBinding myView;
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipboardManagerListener;
    private final String TAG = "ImportOnline";
    private String clipboardText = "";
    private final String[] sources = new String[]{"UltimateGuitar", "Chordie", "SongSelect",
            "WorshipTogether", "UkuTabs", "HolyChords", "La Boîte à chansons", "eChords", "Google",
            "DuckDuckGo"};
    private final String[] address = new String[]{"https://www.ultimate-guitar.com/search.php?search_type=title&value=",
            "https://www.chordie.com/results.php?q=", "https://songselect.ccli.com/search/results?search=",
            "https://www.worshiptogether.com/search-results/#?cludoquery=", "https://ukutabs.com/?s=",
            "https://holychords.pro/search?name=", "https://www.boiteachansons.net/recherche/",
            "https://www.e-chords.com/search-all/","https://www.google.com/search?q=",
            "https://duckduckgo.com/?va=n&t=hv&q="};
    private String webSearchFull, webAddressFinal, source, webString, userAgentDefault, userAgentDesktop,
            import_basic_string="", online_string="", website_song_online_string="", unknown_string="",
            text_extract_check_string="", text_extract_website_string="", mainfoldername_string="",
            success_string="", error_string="", overwrite_string="", song_name_already_taken_string="",
            not_connected_string="";
    private Song newSong;
    private UltimateGuitar ultimateGuitar;
    private Chordie chordie;
    private SongSelect songSelect;
    private WorshipTogether worshipTogether;
    private UkuTabs ukuTabs;
    private HolyChords holyChords;
    private Boiteachansons boiteachansons;
    private EChords eChords;
    private Chordinator chordinator;
    private WebView webView;
    private String webAddress;
    private String songSelectAutoDownload;
    private Uri downloadUri;
    private String downloadFilename;
    private boolean webViewDesktop;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(import_basic_string + " " + online_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportOnlineBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_song_online_string;

        // Setup helper
        setupHelpers();

        // Setup views
        setupViews();

        // Listeners
        setupListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            import_basic_string = getString(R.string.import_basic);
            online_string = getString(R.string.online);
            website_song_online_string = getString(R.string.website_song_online);
            text_extract_check_string = getString(R.string.text_extract_check);
            text_extract_website_string = getString(R.string.text_extract_website);
            mainfoldername_string = getString(R.string.mainfoldername);
            success_string = getString(R.string.success);
            error_string = getString(R.string.error);
            overwrite_string = getString(R.string.overwrite);
            song_name_already_taken_string = getString(R.string.song_name_already_taken);
            unknown_string = getString(R.string.unknown);
            not_connected_string = getString(R.string.requires_internet);
            webViewDesktop = mainActivityInterface.getPreferences().getMyPreferenceBoolean("webViewDesktop",false);
        }
    }

    private void setupHelpers() {
        if (getContext() != null) {
            ultimateGuitar = new UltimateGuitar(getContext());
        }
        newSong = new Song();
        worshipTogether = new WorshipTogether();
        chordie = new Chordie();
        songSelect = new SongSelect();
        ukuTabs = new UkuTabs();
        holyChords = new HolyChords();
        boiteachansons = new Boiteachansons();
        eChords = new EChords();
        chordinator = new Chordinator();
    }

    private void setupViews() {
        webView = myView.webView;
        myView.searchLayout.setVisibility(View.VISIBLE);
        myView.webLayout.setVisibility(View.GONE);
        myView.saveLayout.setVisibility(View.GONE);
        myView.grabText.setVisibility(View.GONE);
        myView.webViewDesktop.setChecked(webViewDesktop);

        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.onlineSource, R.layout.view_exposed_dropdown_item, sources);
            myView.onlineSource.setAdapter(exposedDropDownArrayAdapter);
        }

        if (mainActivityInterface.getCheckInternet().getSearchPhrase() != null) {
            myView.searchPhrase.setText(mainActivityInterface.getCheckInternet().getSearchPhrase());
        }
        // If Google, provide instructions
        myView.onlineSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                myView.googleInfo.setVisibility(
                        myView.onlineSource.getText().toString().equals("Google") ||
                                myView.onlineSource.getText().toString().equals("DuckDuckGo")
                                ? View.VISIBLE:View.GONE);
                myView.webViewDesktop.setVisibility(
                                myView.onlineSource.getText().toString().equals("UltimateGuitar")
                                ? View.VISIBLE:View.GONE);
                mainActivityInterface.getCheckInternet().setSearchSite(myView.onlineSource.getText().toString());
            }
        });

        if (mainActivityInterface.getCheckInternet().getSearchSite() != null) {
            myView.onlineSource.setText(mainActivityInterface.getCheckInternet().getSearchSite());
        }

        setupWebView();
    }

    private void changeUserAgent() {
        // If we are about to use UG and have switch on request desktop option, set that agent
        // If not, set the device default user agent
        if (webViewDesktop && myView.onlineSource.getText().toString().equals("UltimateGuitar")) {
            webView.post(() -> myView.webView.getSettings().setUserAgentString(userAgentDesktop));
        } else {
            webView.post(() -> myView.webView.getSettings().setUserAgentString(userAgentDefault));
        }
    }

    private void changeLayouts(boolean search, boolean web, boolean save) {
        if (search) {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.VISIBLE));
        } else {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.GONE));
        }
        if (web) {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.VISIBLE));
            myView.grabText.post(() -> myView.grabText.setVisibility(View.VISIBLE));
        } else {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.GONE));
        }
        if (save) {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.VISIBLE));
            myView.saveButton.post(() -> myView.saveButton.setVisibility(View.VISIBLE));
        } else {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.GONE));
        }

    }

    private void setupListeners() {
        myView.searchButton.setOnClickListener(v -> checkConnection());
        myView.closeSearch.setOnClickListener(v -> changeLayouts(true, false, false));
        myView.backButton.setOnClickListener(v -> goBackBrowser());
        myView.grabText.setOnClickListener(v -> extractContent());
        myView.saveButton.setOnClickListener(v -> processContent());
        myView.webViewDesktop.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("webViewDesktop",b);
            webViewDesktop = b;
            changeUserAgent();
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (getContext() != null) {
            if (webView == null) {
                webView = new WebView(getContext());
                myView.webViewHolder.addView(webView);
            }

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
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    webView.post(() -> {
                        webAddressFinal = webView.getUrl();
                        Log.d(TAG,"webAddressFinal:"+webAddressFinal);
                    });
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // Run a check for the desired content
                    extractContent();
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    Log.d(TAG, "error");
                }

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                    Log.d("WebView", "crash");
                    destroyWebView();
                    setupWebView();
                    return true; // The app continues executing.
                }
            });

            // This spoofs a desktop browser required for UG if we have set that option
            userAgentDefault = myView.webView.getSettings().getUserAgentString();
            userAgentDesktop = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:123.0) Gecko/20100101 Firefox/123.0";
            changeUserAgent();

            Log.d(TAG,"default:"+userAgentDefault);
            Log.d(TAG,"desktop:"+userAgentDesktop);

            webView.getSettings().getJavaScriptEnabled();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(false);
            webView.addJavascriptInterface(new MyJSInterface(getContext(), this), "HTMLOUT");
            webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                // Show the progressBar
                showDownloadProgress(true);
                changeLayouts(false, false, false);

                String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
                if (url != null) {
                    if (url.startsWith("blob")) {
                        Log.d(TAG,"blob download");
                        webView.loadUrl(MyJSInterface.getBase64StringFromBlobUrl(url));
                    } else {
                        Log.d(TAG,"normal download");
                        MyJSInterface.setDownload(url,filename);
                    }
                }
            });
        }
    }

    private void goBackBrowser() {
        try {
            webView.post(() -> webView.goBack());
        } catch (Exception e) {
            // Error going back in the web view
            e.printStackTrace();
        }
    }

    private void checkConnection() {
        mainActivityInterface.getWindowFlags().hideKeyboard();
        // Check we have an internet connection
        mainActivityInterface.getCheckInternet().checkConnection(getContext(), this, R.id.importOnlineFragment, mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        Log.d(TAG, "connected=" + connected);
        // Received back from the MainActivity after being told if we have a valid internet connection or not
        if (connected) {
            // This listener is to grab text saved to the clipboard manager
            if (getContext() != null) {
                if (myView.onlineSource.getText()!=null && myView.onlineSource.getText().toString().equals("Google")) {
                    clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManagerListener = () -> {
                        ClipData clipData = clipboardManager.getPrimaryClip();
                        if (clipData != null) {
                            ClipData.Item item = clipData.getItemAt(0);
                            if (item != null) {
                                CharSequence charSequence = item.getText();
                                if (charSequence != null) {
                                    clipboardText = charSequence.toString();
                                    if (!clipboardText.isEmpty()) {
                                        clipboardText = mainActivityInterface.getConvertTextSong().convertText(clipboardText);
                                        // Remove this listener otherwise it keeps going!
                                        clipboardManager.removePrimaryClipChangedListener(clipboardManagerListener);
                                        processContent();
                                    }
                                }
                            }
                        }
                    };
                    if (clipboardManager != null) {
                        clipboardManager.addPrimaryClipChangedListener(clipboardManagerListener);
                    }
                }
            }
            // Get the search string and build the web address
            String webAddress = "";
            String extra = "";
            mainActivityInterface.getCheckInternet().
                        setSearchPhrase(myView.searchPhrase.getText().toString());

            if (myView.onlineSource.getText() != null) {
                mainActivityInterface.getCheckInternet().
                            setSearchSite(myView.onlineSource.getText().toString());
                source = myView.onlineSource.getText().toString();
                switch (source) {
                    case "Chordie":
                        extra = "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                        break;
                    case "UltimateGuitar":
                        //String newUA = "Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0";
                        break;
                    case "Google":
                    case "DuckDuckGo":
                        extra = " chords lyrics";
                        break;
                }
                for (int x = 0; x < sources.length; x++) {
                    if (sources[x].equals(source)) {
                        webAddress = address[x];
                    }
                }
            }
            if (mainActivityInterface.getCheckInternet().getSearchPhrase() != null &&
                    !mainActivityInterface.getCheckInternet().getSearchPhrase().isEmpty() &&
                    !webAddress.isEmpty()) {
                changeLayouts(false, true, false);
                myView.grabText.setVisibility(View.VISIBLE);
                if (getActivity()!=null && myView.grabText.getVisibility()==View.VISIBLE) {
                    myView.grabText.post(() -> mainActivityInterface.getShowCase().singleShowCase(getActivity(), myView.grabText, null, text_extract_check_string, false, "onlineTextSearch"));
                }
                webSearchFull = webAddress + mainActivityInterface.getCheckInternet().getSearchPhrase() + extra;
                String justaddress = webAddress;
                webView.post(() -> {
                    if (source.equals("La Boîte à chansons")) {
                        try {
                            String postData = "inpRecherche=" + URLEncoder.encode(mainActivityInterface.getCheckInternet().getSearchPhrase(), "UTF-8");
                            webView.postUrl(justaddress,postData.getBytes());
                        } catch (Exception e) {
                            e.printStackTrace();
                            webView.loadUrl(webSearchFull);
                        }

                    } else {
                        webView.loadUrl(webSearchFull);
                    }
                });
            }
        } else {
            mainActivityInterface.getShowToast().doIt(not_connected_string);
        }
    }

    private void extractContent() {
        Log.d(TAG, "Extract content");

        try {
            showDownloadProgress(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the rest in a new thread
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            webString = "";

            webView.post(() -> {
                try {
                    String script;
                    if (source.equals("SongSelect")) {
                        MyJSInterface.resetFlattenedWebString();
                        // IV - SongSelect viewers use shadow DOM content, allow time to fully populate and then extract
                        if (webView.getOriginalUrl().endsWith("/viewchordsheet") || webView.getOriginalUrl().endsWith("/viewlyrics")) {
                            script = MyJSInterface.flattenShadowRoot();
                            String finalScript = script;
                            new Handler().postDelayed(() -> webView.evaluateJavascript(finalScript, webContent), 5000);
                        } else {
                            script = "javascript:document.getElementsByTagName('html')[0].innerHTML;";
                            webView.evaluateJavascript(script, webContent);
                        }
                    } else {
                        script = "javascript:document.getElementsByTagName('html')[0].innerHTML;";
                        webView.evaluateJavascript(script, webContent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private final ValueCallback<String> webContent = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
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
            webString = webString.replace("</div></div>","</div>\n</div>");

            String[] lines = webString.split("\n");
            for (String line:lines) {
                Log.d(TAG,"line:"+line);
            }
            showSaveButton();
        }
    };


    private void showSaveButton() {
        boolean show = false;
        songSelectAutoDownload = "";
        downloadFilename = "";
        if (webString==null) {
            webString = "";
        }

        Log.d(TAG,"source:"+source);

        switch (source) {
            case "UltimateGuitar":
                if ((webString.contains("<div class=\"ugm-b-tab--content js-tab-content\">") ||
                        webString.contains("<div class=\"js-tab-content-wrapper\">") ||
                        (webString.contains("<div class=\"js-page js-global-wrapper ug-page") ||
                                (webString.contains("<pre class=\"tK8GG")) &&
                                (webString.contains("<span class=\"y68er") ||
                                        webString.contains("<span class=\"fsG7q"))))) {
                    show = true;
                }
                break;
            case "WorshipTogether":
                if (webString.contains("<div class=\"chord-pro-line\">") && webString.contains("<div class=\"chord-pro-lyric\">")) {
                    show = true;
                }
                break;
            case "Chordie":
                if (webString.contains("<textarea id=\"chordproContent\"")) {
                    show = true;
                }
                break;
            case "SongSelect":
                // Get the flattened content
                webString = MyJSInterface.getFlattenedWebString();

                if (webString.contains("class=\"music-sheet\"")) {
                    newSong = songSelect.processContentLyricsText(mainActivityInterface, newSong, webString);
                    show = true;
                } else if (webString.contains("<span class=\"cproTitleLine\">")) {
                    newSong = songSelect.processContentChordPro(getContext(), mainActivityInterface, newSong, webString);
                    show = true;
                } else {
                    // IV - Set a song name for possible use with SongSelect web page download buttons
                    newSong.setFilename(songSelect.getTitle(webString));
                }
                break;
            case "UkuTabs":
                if (webString.contains("class=\"ukutabschord\"") ||
                webString.contains("id=\"autoscrollerbox\"")) {
                    show = true;
                }
                break;
            case "HolyChords":
                if (webString.contains("<pre id=\"music_text\"")) {
                    show = true;
                }
                break;
            case "La Boîte à chansons":
                if (webString.contains("<div class=\"dEntetePartition\">")) {
                    show = true;
                }
                break;
            case "eChords":
                if (webString.contains("<pre id=\"core\"")) {
                    show = true;
                }
                break;
            case "Google":
            case "DuckDuckGo":
                if ((!webView.getUrl().contains("google") || (!webView.getUrl().contains("duckduckgo"))) &&
                        !webView.getUrl().contains("songselect")) {
                    chordinator.setTitle(myView.searchPhrase.getText().toString());
                    chordinator.setArtist(unknown_string);
                    chordinator.processHTML(this,mainActivityInterface,webString);
                }
                // show is processed on a separate thread and doSave called later
                break;
        }
        doShowSaveButton(show);
        showDownloadProgress(false);
    }

    public void doShowSaveButton(boolean show) {
        if (show && myView!=null) {
            myView.saveButton.post(() -> {
                if (getContext() != null) {
                    myView.grabText.hide();
                    myView.saveButton.show();
                    mainActivityInterface.getCustomAnimation().pulse(getContext(), myView.saveButton);
                    mainActivityInterface.getShowCase().singleShowCase(getActivity(), myView.saveButton,
                            null, text_extract_website_string, false, "textWebsite");
                }
            });

        } else if (myView!=null){
            myView.saveButton.post(() -> {
                myView.saveButton.hide();
                myView.saveButton.clearAnimation();
                myView.grabText.show();
            });

        }
    }
    private void processContent() {
        showDownloadProgress(true);
        switch (source) {
            case "Google":
            case "DuckDuckGo":
                if (chordinator.getTitle()!=null && !chordinator.getTitle().isEmpty()) {
                    newSong.setTitle(chordinator.getTitle());
                } else {
                    newSong.setTitle(myView.searchPhrase.getText().toString());
                }
                if (chordinator.getArtist()!=null && !chordinator.getArtist().isEmpty()) {
                    newSong.setAuthor(chordinator.getArtist());
                }
                if (clipboardText!=null) {
                    newSong.setLyrics(clipboardText);
                }
                break;
            case "UltimateGuitar":
                newSong = ultimateGuitar.processContent(newSong,webString);
                break;
            case "WorshipTogether":
                newSong = worshipTogether.processContent(mainActivityInterface,newSong,webString);
                break;
            case "Chordie":
                newSong = chordie.processContent(mainActivityInterface,newSong,webString);
                break;
            case "SongSelect":
                // IV - Setup for download.  Download code will handle save of any XML extract and the downloaded file
                if (!mainActivityInterface.getCheckInternet().getSearchPhrase().startsWith("?")) {
                    if (webString.contains("id=\"lyricsDownloadButton\"")) {
                        songSelectAutoDownload = "lyricsDownloadButton";
                    } else if (webString.contains("id=\"chordSheetDownloadButton\"")) {
                        songSelectAutoDownload = "chordSheetDownloadButton";
                    }
                }
                break;
            case "UkuTabs":
                newSong = ukuTabs.processContent(newSong,webString);
                break;
            case "HolyChords":
                newSong = holyChords.processContent(newSong,webString);
                break;
            case "La Boîte à chansons":
                newSong = boiteachansons.processContent(mainActivityInterface,newSong,webString);
                break;
            case "eChords":
                newSong = eChords.processContent(mainActivityInterface,newSong,webString);
                break;
        }

        // Trim whitespace
        newSong.setTitle(newSong.getTitle().trim());
        newSong.setAuthor(newSong.getAuthor().trim());
        newSong.setCopyright(newSong.getCopyright().trim());
        newSong.setFilename(newSong.getFilename().trim());
        newSong.setKey(newSong.getKey().trim());
        newSong.setKeyOriginal(newSong.getKeyOriginal().trim());
        newSong.setCapo(newSong.getCapo().trim());
        newSong.setLyrics(newSong.getLyrics().trim());
        newSong.setTheme(newSong.getTheme().trim());
        newSong.setAlttheme(newSong.getAlttheme().trim());
        newSong.setAka(newSong.getAka().trim());
        // Make sure the tempo is a whole number!
        if (!newSong.getTempo().isEmpty() && !newSong.getTempo().replaceAll("\\D","").isEmpty()) {
            try {
                newSong.setTempo(String.valueOf(Math.round(Float.parseFloat(newSong.getTempo()))));
            } catch (Exception e) {
                e.printStackTrace();
                newSong.setTempo("");
            }
        }
        newSong.setTempo(newSong.getTempo().trim());
        newSong.setTimesig(newSong.getTimesig().trim());
        newSong.setCcli(newSong.getCcli().trim());
        Log.d(TAG,"title:"+newSong.getTitle());
        Log.d(TAG,"author:"+newSong.getAuthor());
        Log.d(TAG,"key:"+newSong.getKey());
        Log.d(TAG,"lyrics:"+newSong.getLyrics());

        if (songSelectAutoDownload.equals("")) {
            setupSaveLayout();
        } else {
            try {
                // Click the SongSelect download button
                webView.loadUrl("javascript:document.getElementById('" + songSelectAutoDownload + "').click()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Called from Chordinator via MainActivity
    public void setClipboardText(String textFromChordinator) {
        clipboardText = textFromChordinator;
    }

    public void finishedDownload(Uri uri,String filename) {
        // This is called from MainActivity after a SongSelect file is downloaded
        downloadUri = uri;
        downloadFilename = filename;
        setupSaveLayout();
    }

    private void setupSaveLayout() {
        if (myView!=null) {
            showDownloadProgress(false);

            // Set up the save layout
            myView.saveFilename.post(() -> myView.saveFilename.setText(newSong.getFilename()));
            // Get the folders available
            ArrayList<String> availableFolders = mainActivityInterface.getStorageAccess().getSongFolders(
                    mainActivityInterface.getStorageAccess().listSongs(), true, null);
            if (getContext() != null) {
                ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                        myView.folderChoice, R.layout.view_exposed_dropdown_item, availableFolders);
                myView.folderChoice.setAdapter(exposedDropDownArrayAdapter);
            }
            String folder = mainActivityInterface.getPreferences().getMyPreferenceString("songFolder", mainfoldername_string);
            if (folder.isEmpty()) {
                folder = mainfoldername_string;
            }
            myView.folderChoice.setText(folder);
            changeLayouts(false, false, true);
            myView.saveSong.setOnClickListener(v -> saveTheSong());
        }
    }

    // This is the save function for downloaded PDF files
    private void copyPDF(Uri inputUri, String filename) {
        // Prepare the output file
        String folder = mainActivityInterface.getPreferences().getMyPreferenceString("songFolder",mainfoldername_string);
        if (folder.isEmpty()) {
            folder = mainfoldername_string;
        }
        if (myView.folderChoice.getText()!=null) {
            folder = myView.folderChoice.getText().toString();
        }
        if (!myView.saveFilename.getText().toString().isEmpty()) {
            filename = myView.saveFilename.getText().toString();
        }

        // Set the folder
        newSong.setFolder(folder);

        // Update the song pdf values
        if (!filename.toLowerCase().endsWith(".pdf")) {
            filename = filename + ".pdf";
        }

        newSong.setTitle(filename);
        newSong.setFilename(filename);
        newSong.setFiletype("PDF");

        try {
            newSong.setTitle(filename);
            newSong.setFilename(filename);
            newSong.setFiletype("PDF");
            newSong.setLyrics("");
            Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,filename);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" CopyPDF Songs/"+folder+"/"+filename+"  deleteOld=false");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, outputUri,null,"Songs",folder,filename);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " copyPDF copyFile from " + inputUri + " to Songs/" + folder + "/" + filename);
            mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);

            // Send an instruction to update the song menu (no need for full reindex)
            mainActivityInterface.updateSongMenu(newSong);
            // Update the main and nonopensong databases
            mainActivityInterface.getSQLiteHelper().createSong(folder,filename);
            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
            mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder, filename);
            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(newSong);

            // Add a record to the CCLI log if we are automatically logging activity
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                    "ccliAutomaticLogging",false)) {
                mainActivityInterface.getCCLILog().addEntry(newSong,"1");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getShowToast().doIt(error_string);
        }
    }

    private void saveTheSong() {
        // Update the newSong values from what the user chose
        String getName = newSong.getTitle();
        String getFolder = mainfoldername_string;
        if (myView.saveFilename.getText()!=null) {
            getName = myView.saveFilename.getText().toString();
        }
        if (myView.folderChoice.getText()!=null) {
            getFolder = myView.folderChoice.getText().toString();
        }
        newSong.setTitle(getName.trim());
        newSong.setFilename(getName.trim());
        newSong.setFolder(getFolder.trim());
        newSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(getFolder.trim(),getName.trim()));

        // Check if this song already exists
        boolean exists = mainActivityInterface.getStorageAccess().uriExists(
                mainActivityInterface.getStorageAccess().getUriForItem("Songs",getFolder,getName));

        if (!exists) {
            // We can proceed with saving
            continueSaving();
        } else {
            // Alert the user with the bottom sheet are you sure
            String message = overwrite_string+"\n\n"+song_name_already_taken_string;
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet(
                    "onlineSongOverwrite",message,null,"importOnlineFragment",this,newSong);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSure");
        }
    }

    public void continueSaving() {
        if (newSong.getLyrics().equals("")) {
            // IV - If we do not have an extracted song, save any SongSelect PDF download
            if (source.equals("SongSelect") && downloadFilename.toLowerCase().endsWith(".pdf")) {
                copyPDF(downloadUri, downloadFilename);

                // Set the current song to this
                mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", newSong.getFolder());
                mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", newSong.getFilename());
                // Send an instruction to update the song menu (no need for full reindex)
                mainActivityInterface.updateSongMenu(newSong);
            }
        } else {
            mainActivityInterface.getSong().setFolder(newSong.getFolder());
            mainActivityInterface.getSong().setFilename(newSong.getFilename());

            if (mainActivityInterface.getSaveSong().doSave(newSong)) {
                // Update the songid file (used later)
                mainActivityInterface.getStorageAccess().writeSongIDFile(
                        mainActivityInterface.getStorageAccess().getSongIDsFromFile());

                // Set the current song to this
                mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", newSong.getFolder());
                mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", newSong.getFilename());

                // Send an instruction to update the song menu (no need for full reindex)
                mainActivityInterface.updateSongMenu(newSong);
            } else {
                mainActivityInterface.getShowToast().doIt(error_string);
                return;
            }
            // For SongSelect we handle PDF files. TXT and ChordPro files are not saved as we have extracted XML
            if (source.equals("SongSelect") && downloadFilename.toLowerCase().endsWith(".pdf")) {
                copyPDF(downloadUri, downloadFilename);

                // Send an instruction to update the song menu (no need for full reindex)
                mainActivityInterface.updateSongMenu(newSong);
            }
        }
        // Let the user know and show the song
        mainActivityInterface.navHome();
        mainActivityInterface.getShowToast().doIt(success_string);
    }

    private void showDownloadProgress(final boolean waiting) {
        if (myView!=null) {
            webView.post(() -> webView.setEnabled(!waiting));
            final int visibility;
            if (waiting) {
                visibility = View.VISIBLE;
            } else {
                visibility = View.GONE;
            }
            myView.progressBar.post(() -> myView.progressBar.setVisibility(visibility));
            myView.saveButton.post(() -> myView.saveButton.setEnabled(!waiting));
            myView.backButton.post(() -> myView.backButton.setEnabled(!waiting));
            myView.closeSearch.post(() -> myView.closeSearch.setEnabled(!waiting));
        }
    }

    public void destroyWebView() {
        try {
            // Make sure you remove the WebView from its parent view before doing anything.
            myView.webViewHolder.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                webView.destroyDrawingCache();

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
        try {
            if (clipboardManager!=null && clipboardManagerListener!=null) {
                clipboardManager.removePrimaryClipChangedListener(clipboardManagerListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
