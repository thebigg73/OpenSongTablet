package com.garethevans.church.opensongtablet.importsongs;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportOnlineFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsImportOnlineBinding myView;
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipboardManagerListener;
    private final String TAG = "ImportOnline";
    private String clipboardText = "";
    private Uri downloadedFile = null;
    private boolean isPDF;
    private final String[] sources = new String[]{"UltimateGuitar", "Chordie", "SongSelect",
            "WorshipTogether", "UkuTabs", "HolyChords", "La Boîte à chansons", "eChords", "Google"};
    private final String[] address = new String[]{"https://www.ultimate-guitar.com/search.php?search_type=title&value=",
            "https://www.chordie.com/results.php?q=", "https://songselect.ccli.com/Search/Results?SearchText=",
            "https://www.worshiptogether.com/search-results/#?cludoquery=", "https://ukutabs.com/?s=",
            "https://holychords.pro/search?name=", "https://www.boiteachansons.net/recherche/",
            "https://www.e-chords.com/search-all/","https://www.google.com/search?q="};
    private String webSearchFull, webAddressFinal, source, webString, userAgentDefault,
            import_basic_string="", online_string="", website_song_online_string="",
            text_extract_check_string="", text_extract_website_string="", mainfoldername_string="",
            success_string="", error_string="", overwrite_string="", song_name_already_taken_string="";
    private Song newSong;
    private UltimateGuitar ultimateGuitar;
    private Chordie chordie;
    private SongSelect songSelect;
    private WorshipTogether worshipTogether;
    private UkuTabs ukuTabs;
    private HolyChords holyChords;
    private Boiteachansons boiteachansons;
    private EChords eChords;
    private WebView webView;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
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

        mainActivityInterface.updateToolbar(import_basic_string + " " + online_string);
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
    }

    private void setupViews() {
        webView = myView.webView;
        myView.searchLayout.setVisibility(View.VISIBLE);
        myView.webLayout.setVisibility(View.GONE);
        myView.saveLayout.setVisibility(View.GONE);
        myView.grabText.setVisibility(View.GONE);

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
                myView.clipboardInstructions.setVisibility(
                        myView.onlineSource.getText().toString().equals("Google") ? View.VISIBLE:View.GONE);
            }
        });

        if (mainActivityInterface.getCheckInternet().getSearchSite() != null) {
            myView.onlineSource.setText(mainActivityInterface.getCheckInternet().getSearchSite());
        }

        setupWebView();
    }

    private void changeLayouts(boolean search, boolean web, boolean save) {
        if (search) {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.VISIBLE));
        } else {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.GONE));
        }
        if (web) {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.VISIBLE));
            myView.grabText.post(() -> myView.grabText.setVisibility(myView.onlineSource.getText().toString().equals("Google")? View.GONE:View.VISIBLE));
        } else {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.GONE));
        }
        if (save) {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.VISIBLE));
            myView.saveButton.post(() -> myView.saveButton.setVisibility(myView.onlineSource.getText().toString().equals("Google")?View.GONE:View.VISIBLE));
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
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (getContext() != null) {
            if (webView == null) {
                webView = new WebView(getContext());
                myView.webViewHolder.addView(webView);
            }
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d(TAG, consoleMessage.message() + " -- From line " +
                            consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
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
                    webView.post(() -> webAddressFinal = webView.getUrl());
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
            if (userAgentDefault == null) {
                userAgentDefault = webView.getSettings().getUserAgentString();
                Log.d(TAG, "userAgentString=" + userAgentDefault);
            }
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
                        webView.loadUrl(MyJSInterface.doNormalDownLoad(url,filename));
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
        mainActivityInterface.getCheckInternet().checkConnection(this, R.id.importOnlineFragment, mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        Log.d(TAG, "connected=" + connected);
        // Received back from the MainActivity after being told if we have a valid internet connection or not
        if (connected) {
            // This listener is to grab text saved to the clipboard manager
            if (getContext() != null) {
                if (myView.onlineSource.getText()!=null && myView.onlineSource.getText().toString().equals("Google")) {
                    source = "Google";
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
                webView.post(() -> webView.getSettings().setUserAgentString(userAgentDefault));
                if (source.equals("Chordie")) {
                    extra = "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                } else if (source.equals("UltimateGuitar")) {
                    String newUA = "Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0";
                    webView.post(() -> webView.getSettings().setUserAgentString(newUA));
                } else if (source.equals("Google")) {
                    extra = " chords lyrics";
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
                myView.grabText.setVisibility(myView.onlineSource.getText().toString().equals("Google")? View.GONE:View.VISIBLE);
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
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            webString = "";
            webView.post(() -> {
                try {
                    webView.evaluateJavascript("javascript:document.getElementsByTagName('html')[0].innerHTML", webContent);
                } catch (Exception e) {
                        e.printStackTrace();
                }
            });
        });
    }

    private final ValueCallback<String> webContent = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            Log.d(TAG,"value:"+value);
            JsonReader reader = new JsonReader(new StringReader(value));
            reader.setLenient(true);

            try {
                if (reader.peek() == JsonToken.STRING) {
                    webString = reader.nextString();
                    Log.d(TAG,"webString:"+webString);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            webString = webString.replace("\r","\n");
            showSaveButton();
        }
    };


    private void showSaveButton() {
        boolean show = false;
        if (webString==null) {
            webString = "";
        }

        String[] lines = webString.split("\n");
        for (String line:lines) {
            Log.d(TAG,"line: "+line);
        }

        switch (source) {
            case "UltimateGuitar":
                if (webString.contains("<div class=\"ugm-b-tab--content js-tab-content\">") ||
                        (webString.contains("<div class=\"js-page js-global-wrapper\">") &&
                                webString.contains("<span class=\"y68er\">"))) {
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
                if (webString.contains("<div id=\"LyricsText\"") ||
                        webString.contains("<span class=\"cproTitleLine\">") ||
                        webString.contains(" id=\"sheetMusicDownloadButton\"")) {
                    show = true;
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
        }
        if (show) {
            myView.saveButton.post(() -> {
                if (getContext()!=null) {
                    myView.grabText.hide();
                    if (!myView.onlineSource.getText().toString().equals("Google")) {
                        myView.saveButton.show();
                        mainActivityInterface.getCustomAnimation().pulse(getContext(), myView.saveButton);
                        mainActivityInterface.getShowCase().singleShowCase(getActivity(), myView.saveButton,
                                null, text_extract_website_string, false, "textWebsite");
                    }
                }
            });

        } else {
            myView.saveButton.post(() -> {
                myView.saveButton.hide();
                myView.saveButton.clearAnimation();
                if (!myView.onlineSource.getText().toString().equals("Google")) {
                    myView.grabText.show();
                }
            });

        }
        showDownloadProgress(false);
    }

    private void processContent() {
        showDownloadProgress(true);
        newSong = new Song();

        Log.d(TAG,"source:"+source);
        isPDF = false;
        boolean songSelectDownload = false;
        switch (source) {
            case "Google":
                newSong.setTitle(myView.searchPhrase.getText().toString());
                newSong.setLyrics(clipboardText);
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
                Log.d(TAG,"getting here SongSelect");
                if (webString.contains("<div id=\"LyricsText\"")) {
                    newSong = songSelect.processContentLyricsText(mainActivityInterface, newSong, webString);
                } else  if (webString.contains("<span class=\"cproTitleLine\">")) {
                    newSong = songSelect.processContentChordPro(mainActivityInterface, newSong, webString);
                }
                Log.d(TAG,"newSong:"+newSong.getLyrics());
                // Trigger the clicking of the download buttons
                String what="";
                if (webString.contains("id=\"downloadLyrics\"")) {
                    // Click on the downloadLyrics button
                    what = "downloadLyrics";
                    isPDF = false;
                } else if (webString.contains("id=\"chordSheetDownloadButton\"")) {
                    // Click on the PDF chord sheet download
                    what = "chordSheetDownloadButton";
                    isPDF = true;
                } else if (webString.contains("id=\"sheetMusicDownloadButton\"")) {
                    // Click on the PDF sheet download
                    what = "sheetMusicDownloadButton";
                    isPDF = true;
                }
                if (!what.isEmpty()) {
                    String webLink = webView.getUrl();
                    songSelectDownload = true;
                    try {
                        // Trigger the download of the pdf
                        webView.loadUrl("javascript:document.getElementById('" + what + "').click()");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "UkuTabs":
                Log.d(TAG, "getting here UkuTabs");
                newSong = ukuTabs.processContent(newSong,webString);
                break;
            case "HolyChords":
                Log.d(TAG, "getting here HolyChords");
                newSong = holyChords.processContent(newSong,webString);
                break;
            case "La Boîte à chansons":
                Log.d(TAG,"getting here La Boîte à chansons");
                newSong = boiteachansons.processContent(mainActivityInterface,newSong,webString);
                break;
            case "eChords":
                Log.d(TAG,"getting here eChords");
                newSong = eChords.processContent(newSong,webString);
                break;
        }
        if (!songSelectDownload) {
            showDownloadProgress(false);
            setupSaveLayout();
        }
    }

    public void finishedDownloadPDF(Uri uri,String filename) {
        // This is sent from MainActivity after a pdf file was downloaded
        // Fix the views
        filename = filename.replace(".txt","");
        newSong.setFilename(newSong.getFilename().replace(".txt",""));
        newSong.setTitle(newSong.getTitle().replace(".txt",""));
        Log.d(TAG,"finishedDownloadPDF uri:"+uri);
        if (uri!=null) {
            showDownloadProgress(false);
            changeLayouts(false, false, true);
            // Firstly use what has been entered into the name box by parsing the song
            String currfilename = myView.saveFilename.getText().toString();
            // If we don't have a good name, try to base it on the filename
            if (currfilename.isEmpty() && uri.getLastPathSegment() != null) {
                currfilename = filename;
            }
            setupSaveLayout();
            myView.saveFilename.setText(currfilename);
            String finalCurrfilename = currfilename;
            myView.saveSong.setOnClickListener(v -> copyPDF(uri, finalCurrfilename));
        }
    }

    private void setupSaveLayout() {
        // Set up the save layout
        myView.saveFilename.post(() -> myView.saveFilename.setText(newSong.getTitle()));
        // Get the folders available
        ArrayList<String> availableFolders = mainActivityInterface.getStorageAccess().getSongFolders(
                mainActivityInterface.getStorageAccess().listSongs(), true, null);
        if (getContext()!=null) {
            ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(getContext(),
                    myView.folderChoice, R.layout.view_exposed_dropdown_item, availableFolders);
            myView.folderChoice.setAdapter(exposedDropDownArrayAdapter);
        }
        myView.folderChoice.setText(mainActivityInterface.getPreferences().
                getMyPreferenceString("songFolder",mainfoldername_string));
        changeLayouts(false,false,true);
        myView.saveSong.setOnClickListener(v -> saveTheSong());
        showDownloadProgress(false);
    }

    // This is the save function for downloaded PDF files
    private void copyPDF(Uri inputUri, String filename) {
        // Prepare the output file
        String folder = mainActivityInterface.getPreferences().getMyPreferenceString("songFolder",mainfoldername_string);
        if (myView.folderChoice.getText()!=null) {
            folder = myView.folderChoice.getText().toString();
        }
        if (!myView.saveFilename.getText().toString().isEmpty()) {
            filename = myView.saveFilename.getText().toString();
        }

        // Set the folder
        newSong.setFolder(folder);

        // If we have a pdfSong with extracted lyrics, save that too
        if (isPDF && newSong.getLyrics()!=null && !newSong.getLyrics().trim().isEmpty()) {
            String nonPDFFilename = filename.replace(".pdf","").replace(".PDF","");
            newSong.setFilename(nonPDFFilename);
            newSong.setTitle(nonPDFFilename);
            newSong.setFiletype("XML");
            // Set the main song otherwise it gets overwritten!
            mainActivityInterface.getSong().setFilename(nonPDFFilename);
            mainActivityInterface.getSong().setFolder(folder);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " write OpenSongSong from PDF " + filename + " to Songs/" + folder + "/" + nonPDFFilename);
            mainActivityInterface.getSaveSong().doSave(newSong);
            // Add to the database
            mainActivityInterface.getSQLiteHelper().createSong(folder,nonPDFFilename);
            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
        }

        // Update the song pdf values
        if (isPDF) {
            if (!filename.toLowerCase().endsWith(".pdf")) {
                filename = filename + ".pdf";
            }
            newSong.setTitle(filename);
            newSong.setFilename(filename);
        }
        newSong.setFiletype("PDF");

        Log.d(TAG,"newSong filename:"+newSong.getFilename()+"  folder:"+newSong.getFolder()+"  lyrics:"+newSong.getLyrics());
        try {
            // Copy the file
            if (isPDF) {
                Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,filename);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" CopyPDF Songs/"+folder+"/"+filename+"  deleteOld=false");
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, outputUri,null,"Songs",folder,filename);
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " copyPDF copyFile from " + inputUri + " to Songs/" + folder + "/" + filename);
                mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
            } else {
                // A text based song, so just create the song in OpenSong format
                newSong.setFiletype("XML");
                folder = newSong.getFolder();
                filename = newSong.getFilename();
                mainActivityInterface.getSong().setFolder(folder);
                mainActivityInterface.getSong().setFilename(filename);
                mainActivityInterface.getSaveSong().doSave(newSong);
            }
            // Update the current song for loading up
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder",folder);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename",filename);

            // Update the main and nonopensong databases
            mainActivityInterface.getSQLiteHelper().createSong(folder,filename);
            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
            if (isPDF) {
                mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder, filename);
                mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(newSong);
            }

            // Add a record to the CCLI log if we are automatically logging activity
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                    "ccliAutomaticLogging",false)) {
                mainActivityInterface.getCCLILog().addEntry(newSong,"1");
            }

            // Let the user know and show the song
            mainActivityInterface.getShowToast().doIt(success_string);
            mainActivityInterface.navHome();

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
        newSong.setTitle(getName);
        newSong.setFilename(getName);
        newSong.setFolder(getFolder);
        newSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(getFolder,getName));

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
        // Set the current song to a this one as the song save process checks for a change of filename/folder
        // As this would be detected, it would try to delete the old (currently viewed) file, which we don't want
        mainActivityInterface.getSong().setFolder(newSong.getFolder());
        mainActivityInterface.getSong().setFilename(newSong.getFilename());

        if (mainActivityInterface.getSaveSong().
                doSave(newSong)) {
            // Update the songid file (used later)
            mainActivityInterface.getStorageAccess().writeSongIDFile(
                    mainActivityInterface.getStorageAccess().getSongIDsFromFile());

            // Set the current song to this
            mainActivityInterface.getPreferences().
                    setMyPreferenceString("songFolder", newSong.getFolder());
            mainActivityInterface.getPreferences().
                    setMyPreferenceString("songFilename", newSong.getFilename());

            // Send an instruction to update the song menu (no need for full reindex)
            mainActivityInterface.updateSongMenu(newSong);
            mainActivityInterface.getShowToast().doIt(success_string);

            mainActivityInterface.navHome();
        } else {
            mainActivityInterface.getShowToast().doIt(error_string);
        }
    }

    private void showDownloadProgress(final boolean waiting) {
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
        webView.post(() -> webView.setEnabled(!waiting));
    }


    public void destroyWebView() {

        // Make sure you remove the WebView from its parent view before doing anything.
        myView.webViewHolder.removeAllViews();

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

            // Null out the reference so that you don't end up re-using it.
            webView = null;
        }
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
