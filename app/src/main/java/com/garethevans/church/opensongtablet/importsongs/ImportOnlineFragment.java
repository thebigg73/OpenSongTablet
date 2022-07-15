package com.garethevans.church.opensongtablet.importsongs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.RenderProcessGoneDetail;
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
import java.util.ArrayList;

public class ImportOnlineFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsImportOnlineBinding myView;
    private final String TAG = "ImportOnline";
    private final String[] sources = new String[]{"UltimateGuitar", "Chordie", "SongSelect", "WorshipTogether", "UkuTabs", "HolyChords"};
    private final String[] address = new String[]{"https://www.ultimate-guitar.com/search.php?search_type=title&value=",
            "https://www.chordie.com/results.php?q=", "https://songselect.ccli.com/Search/Results?SearchText=",
            "https://www.worshiptogether.com/search-results/#?cludoquery=", "https://ukutabs.com/?s=",
            "https://holychords.com/search?name="};
    private String webSearchFull, webAddressFinal, source, webString, userAgentDefault;
    private Song newSong;
    private UltimateGuitar ultimateGuitar;
    private Chordie chordie;
    private SongSelect songSelect;
    private WorshipTogether worshipTogether;
    private UkuTabs ukuTabs;
    private HolyChords holyChords;
    private WebView webView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportOnlineBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.import_basic) + " " + getString(R.string.online));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_song_online));

        // Setup helper
        setupHelpers();

        // Setup views
        setupViews();

        // Listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupHelpers() {
        newSong = new Song();
        ultimateGuitar = new UltimateGuitar(requireContext());
        worshipTogether = new WorshipTogether();
        chordie = new Chordie();
        songSelect = new SongSelect();
        ukuTabs = new UkuTabs();
        holyChords = new HolyChords();
    }

    private void setupViews() {
        webView = myView.webView;
        myView.searchLayout.setVisibility(View.VISIBLE);
        myView.webLayout.setVisibility(View.GONE);
        myView.saveLayout.setVisibility(View.GONE);

        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, sources);
        myView.onlineSource.setAdapter(exposedDropDownArrayAdapter);
        // Set the position in the list to the chosen value
        exposedDropDownArrayAdapter.keepSelectionPosition(myView.onlineSource, sources);
        if (mainActivityInterface.getCheckInternet().getSearchPhrase() != null) {
            myView.searchPhrase.setText(mainActivityInterface.getCheckInternet().getSearchPhrase());
        }
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
        } else {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.GONE));
        }
        if (save) {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.VISIBLE));
        } else {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.GONE));
        }

    }

    private void setupListeners() {
        myView.searchButton.setOnClickListener(v -> checkConnection());
        myView.closeSearch.setOnClickListener(v -> changeLayouts(true, false, false));
        myView.backButton.setOnClickListener(v -> goBackBrowser());
        myView.saveButton.setOnClickListener(v -> processContent());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        if (webView==null) {
            webView = new WebView(requireContext());
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
        if (userAgentDefault==null) {
            userAgentDefault = webView.getSettings().getUserAgentString();
            Log.d(TAG,"userAgentString="+userAgentDefault);
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
        webView.addJavascriptInterface(new MyJSInterface(requireContext(),this), "HTMLOUT");

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            // Show the progressBar
            showDownloadProgress(true);
            changeLayouts(false, false, false);

            if (url!=null) {
                if (url.startsWith("blob")) {
                    webView.loadUrl(MyJSInterface.getBase64StringFromBlobUrl(url));
                } else {
                    webView.loadUrl(MyJSInterface.doNormalDownLoad(url));
                }
            }
        });
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
        // Check we have an internet connection
        mainActivityInterface.getCheckInternet().checkConnection(this, R.id.importOnlineFragment, mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        Log.d(TAG, "connected=" + connected);
        // Received back from the MainActivity after being told if we have a valid internet connection or not
        if (connected) {
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
                webSearchFull = webAddress + mainActivityInterface.getCheckInternet().getSearchPhrase() + extra;
                webView.post(() -> webView.loadUrl(webSearchFull));
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
        new Thread(() -> {
            webString = "";
            webView.post(() -> {
                try {
                    webView.evaluateJavascript("javascript:document.getElementsByTagName('html')[0].innerHTML", webContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
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
            showSaveButton();
        }
    };


    private void showSaveButton() {
        boolean show = false;
        if (webString==null) {
            webString = "";
        }

        /*String[] lines = webString.split("\n");
        for (String line:lines) {
            Log.d(TAG,"line: "+line);
        }*/

        switch (source) {
            case "UltimateGuitar":
                if (webString.contains("<div class=\"ugm-b-tab--content js-tab-content\">") ||
                        (webString.contains("<div class=\"js-page js-global-wrapper\">") &&
                                webString.contains("<span class=\"_3rlxz\">"))) {
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
                if (webString.contains("<span class=\"cproTitleLine\">")) {
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
        }
        if (show) {
            myView.saveButton.post(() -> {
                myView.saveButton.show();
                mainActivityInterface.getCustomAnimation().pulse(requireContext(),myView.saveButton);
                mainActivityInterface.getShowCase().singleShowCase(requireActivity(),myView.saveButton,
                        null,getString(R.string.text_extract_website),false,"textWebsite");
            });

        } else {
            myView.saveButton.post(() -> {
                myView.saveButton.hide();
                myView.saveButton.clearAnimation();
            });

        }
        showDownloadProgress(false);
    }

    private void processContent() {
        showDownloadProgress(true);
        newSong = new Song();

        Log.d(TAG,"source:"+source);
        switch (source) {
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
                newSong = songSelect.processContent(mainActivityInterface,newSong,webString);
                break;
            case "UkuTabs":
                Log.d("ImportOnline", "getting here UkuTabs");
                newSong = ukuTabs.processContent(newSong,webString);
                break;
            case "HolyChords":
                Log.d(TAG, "getting here HolyChords");
                newSong = holyChords.processContent(newSong,webString);
                break;
        }
        showDownloadProgress(false);

        // Set up the save layout
        setupSaveLayout();
    }

    public void finishedDownloadPDF(Uri uri) {
        // This is sent from MainActivity after a pdf file was downloaded
        // Fix the views
        if (uri!=null) {
            showDownloadProgress(false);
            changeLayouts(false, false, true);
            // Firstly use what has been entered into the name box by parsing the song
            String filename = myView.saveFilename.getText().toString();
            // If we don't have a good name, try to base it on the filename
            if (filename.isEmpty() && uri.getLastPathSegment() != null) {
                filename = uri.getLastPathSegment();
            }
            setupSaveLayout();
            myView.saveFilename.setText(filename);
            myView.saveSong.setOnClickListener(v -> copyPDF(uri));
        }
    }

    private void setupSaveLayout() {
        // Set up the save layout
        myView.saveFilename.post(() -> myView.saveFilename.setText(newSong.getTitle()));
        // Get the folders available
        ArrayList<String> availableFolders = mainActivityInterface.getStorageAccess().getSongFolders(
                mainActivityInterface.getStorageAccess().listSongs(), true, null);
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item,availableFolders);
        myView.folderChoice.setAdapter(exposedDropDownArrayAdapter);
        myView.folderChoice.setText(mainActivityInterface.getPreferences().
                getMyPreferenceString("whichSongFolder",getString(R.string.mainfoldername)));
        // Set the position in the list to the chosen value
        exposedDropDownArrayAdapter.keepSelectionPosition(myView.folderChoice,availableFolders);
        changeLayouts(false,false,true);
        myView.saveSong.setOnClickListener(v -> saveTheSong());
        showDownloadProgress(false);
    }

    // This is the save function for downloaded PDF files
    private void copyPDF(Uri inputUri) {
        // Prepare the output file
        String filename = "SongSelect.pdf";
        String folder = mainActivityInterface.getPreferences().getMyPreferenceString("whichSongFolder",getString(R.string.mainfoldername));
        if (myView.folderChoice.getText()!=null) {
            folder = myView.folderChoice.getText().toString();
        }
        if (!myView.saveFilename.getText().toString().isEmpty()) {
            filename = myView.saveFilename.getText().toString();
        }
        if (!filename.toLowerCase().endsWith(".pdf")) {
            filename = filename + ".pdf";
        }

        // Update the song pdf values
        newSong.setTitle(filename);
        newSong.setFilename(filename);
        newSong.setFolder(folder);
        newSong.setFiletype("PDF");

        Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",folder,filename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, outputUri,null,"Songs",folder,filename);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(inputUri);
        try {
            // Copy the file
            mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);

            // Update the current song
            mainActivityInterface.getPreferences().setMyPreferenceString("whichSongFolder",folder);
            mainActivityInterface.getPreferences().setMyPreferenceString("songfilename",filename);

            // Update the main and nonopensong databases
            mainActivityInterface.getSQLiteHelper().createSong(folder,filename);
            mainActivityInterface.getSQLiteHelper().updateSong(newSong);
            mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(folder,filename);
            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(newSong);

            // Add a record to the CCLI log if we are automatically logging activity
            if (mainActivityInterface.getPreferences().getMyPreferenceBoolean(
                    "ccliAutomaticLogging",false)) {
                mainActivityInterface.getCCLILog().addEntry(newSong,"1");
            }

            // Let the user know and show the song
            mainActivityInterface.getShowToast().doIt(getString(R.string.success));
            mainActivityInterface.navHome();

        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getShowToast().doIt(getString(R.string.error));
        }
    }

    private void saveTheSong() {
        // Update the newSong values from what the user chose
        String getName = newSong.getTitle();
        String getFolder = getString(R.string.mainfoldername);
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
            AreYouSureBottomSheet areYouSureBottomSheet = new AreYouSureBottomSheet(
                    "onlineSongOverwrite",getString(R.string.overwrite),null,"importOnlineFragment",this,newSong);
            areYouSureBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"AreYouSure");
        }
    }

    public void continueSaving() {
        if (mainActivityInterface.getSaveSong().
                doSave(newSong)) {
            // Update the songid file (used later)
            mainActivityInterface.getStorageAccess().writeSongIDFile(
                    mainActivityInterface.getStorageAccess().getSongIDsFromFile());

            // Set the current song to this
            mainActivityInterface.getPreferences().
                    setMyPreferenceString("whichSongFolder", newSong.getFolder());
            mainActivityInterface.getPreferences().
                    setMyPreferenceString("songfilename", newSong.getFilename());

            // Send an instruction to update the song menu (no need for full reindex)
            mainActivityInterface.updateSongMenu(newSong);
            mainActivityInterface.getShowToast().doIt(getString(R.string.success));

            mainActivityInterface.navHome();
        } else {
            mainActivityInterface.getShowToast().doIt(getString(R.string.error));
        }
    }

    private void grabchordpro(String content) {
        // Need to run a async task to grab html text


       /* // If we are in songselect, trigger the download to keep the stats live
        if (StaticVariables.whattodo.equals("songselect")) {
            try {
                // Trigger the download of the pdf
                webresults_WebView.loadUrl("javascript:document.getElementById('chordSheetDownloadButton').click()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
