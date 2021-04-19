/*
package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PopUpFindNewSongsFragment extends DialogFragment {

    private LinearLayout searchtext_LinearLayout, newfileinfo_LinearLayout;
    private EditText searchphrase_EditText, songfilename_EditText;
    private RelativeLayout searchresults_RelativeLayout;
    private WebView webresults_WebView;
    private Button saveSong_Button;
    private ProgressBar grabSongData_ProgressBar;
    private Spinner choosefolder_Spinner;
    private String weblink = "https://www.ultimate-guitar.com/";
    private String filename;
    private String authorname;
    private String filecontents;
    private String newtext;
    private String whatfolderselected;
    private ArrayList<String> newtempfolders;
    private MyInterface mListener;
    private boolean downloadcomplete = false;
    private TextSongConvert textSongConvert;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private SongFolders songFolders;
    private Uri downloadedFile;

    static PopUpFindNewSongsFragment newInstance() {
        PopUpFindNewSongsFragment frag;
        frag = new PopUpFindNewSongsFragment();
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyInterface) context;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String mTitle;
        switch (StaticVariables.whattodo) {
            case "chordie":
                mTitle = requireActivity().getResources().getString(R.string.chordiesearch);
                break;
            case "songselect":
                mTitle = requireActivity().getResources().getString(R.string.songselect);
                break;
            case "worshiptogether":
                mTitle = requireActivity().getResources().getString(R.string.worshiptogether);
                break;
            case "ukutabs":
                mTitle = requireActivity().getResources().getString(R.string.ukutabs);
                break;
            case "worshipready":
                mTitle = requireActivity().getResources().getString(R.string.worshipready);
                break;
            case "holychords":
                mTitle = requireActivity().getResources().getString(R.string.holychords);
                break;
            default:
                mTitle = requireActivity().getResources().getString(R.string.ultimateguitarsearch);
                break;
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_findnewsongs, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(mTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            try {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            } catch (Exception e) {
                // Error cancelling
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        textSongConvert = new TextSongConvert();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();

        // Initialise the views
        searchtext_LinearLayout = V.findViewById(R.id.searchtext_LinearLayout);
        searchphrase_EditText = V.findViewById(R.id.searchphrase_EditText);
        searchresults_RelativeLayout = V.findViewById(R.id.searchresults_RelativeLayout);
        webresults_WebView = V.findViewById(R.id.webresults_WebView);
        ImageButton webBack_ImageButton = V.findViewById(R.id.webBack_ImageButton);
        Button grabSongData_Button = V.findViewById(R.id.grabSongData_Button);
        grabSongData_ProgressBar = V.findViewById(R.id.grabSongData_ProgressBar);
        Button doSearch_Button = V.findViewById(R.id.doSearch_Button);
        newfileinfo_LinearLayout = V.findViewById(R.id.newfileinfo_LinearLayout);
        songfilename_EditText = V.findViewById(R.id.songfilename_EditText);
        choosefolder_Spinner = V.findViewById(R.id.choosefolder_Spinner);
        saveSong_Button = V.findViewById(R.id.saveSong_Button);

        // Set the text if it exists
        searchphrase_EditText.setText(FullscreenActivity.phrasetosearchfor);

        // Set the folder spinner
        AsyncTask<Object, Void, String> getfolders = new GetFolders();
        try {
            getfolders.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d", "Probably closed popup before folders listed\n" + e);
        }
        choosefolder_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                whatfolderselected = newtempfolders.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // By default, hide the searchresults_RelativeLayout section
        searchresults_RelativeLayout.setVisibility(View.GONE);
        searchtext_LinearLayout.setVisibility(View.VISIBLE);
        newfileinfo_LinearLayout.setVisibility(View.GONE);

        // Listen for the buttons

        doSearch_Button.setOnClickListener(view -> {
            String searchtext = searchphrase_EditText.getText().toString();
            if (!searchtext.equals("")) {
                FullscreenActivity.phrasetosearchfor = searchtext;
                doSearch(searchtext);
            }
        });

        webBack_ImageButton.setOnClickListener(view -> {
            try {
                webresults_WebView.goBack();
            } catch (Exception e) {
                // Error going back in the web view
            }
        });
        grabSongData_Button.setOnClickListener(view -> grabchordpro());

        saveSong_Button.setOnClickListener(view -> doSaveSong());

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @SuppressLint({"SetJavaScriptEnabled", "deprecation", "AddJavascriptInterface"})
    private void doSearch(String searchtext) {

        searchresults_RelativeLayout.setVisibility(View.VISIBLE);
        searchtext_LinearLayout.setVisibility(View.GONE);

        switch (StaticVariables.whattodo) {
            case "chordie":
                weblink = "https://www.chordie.com/results.php?q=" + searchtext + "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                break;
            case "ultimate-guitar":
                weblink = "https://www.ultimate-guitar.com/search.php?page=1&tab_type_group=text&app_name=ugt&order=myweight&type=300&title=" + searchtext;
                //weblink = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=" + searchtext;
                break;
            case "worshiptogether":
                weblink = "https://worship-songs-resources.worshiptogether.com/search?w=" + searchtext;
                break;
            case "worshipready":
                weblink = "https://www.worshipready.com/chord-charts";
                //weblink = "https://www.worshipready.com/index.php?option=com_zoo&Itemid=199&app_id=2&controller=zoofilter&lang=en&search_id=74286&task=dosearch";
                break;
            case "songselect":
                weblink = "https://songselect.ccli.com/Search/Results?SearchText=" + searchtext;
                break;
            case "ukutabs":
                weblink = "https://ukutabs.com/?s=" + searchtext;
                break;
            case "holychords":
                weblink = "https://holychords.com/search/?id=" + searchtext;
                break;
        }

        webresults_WebView.setWebChromeClient(new WebChromeClient());
        webresults_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        //String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        String newUA = "Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0";
        //String oldUA = "Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
        webresults_WebView.getSettings().setUserAgentString(newUA);
        webresults_WebView.getSettings().getJavaScriptEnabled();
        webresults_WebView.getSettings().setJavaScriptEnabled(true);
        webresults_WebView.getSettings().setDomStorageEnabled(true);
        webresults_WebView.getSettings().setLoadWithOverviewMode(true);
        webresults_WebView.getSettings().setUseWideViewPort(true);
        webresults_WebView.getSettings().setSupportZoom(true);
        webresults_WebView.getSettings().setBuiltInZoomControls(true);
        webresults_WebView.getSettings().setDisplayZoomControls(false);
        webresults_WebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webresults_WebView.setScrollbarFadingEnabled(false);
        webresults_WebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        try {
            requireActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Log.d("d","Error registering download complete listener");
        }
        webresults_WebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            if (FullscreenActivity.whattodo.equals("songselect") && (filename.endsWith(".pdf")||filename.endsWith(".PDF"))) {

                try {
                    // Hide the WebView
                    searchresults_RelativeLayout.setVisibility(View.GONE);
                    StaticVariables.myToastMessage = "Downloading...";
                    saveSong_Button.setEnabled(false);
                    ShowToast.showToast(getActivity());

                    String cookie = CookieManager.getInstance().getCookie(url);

                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                    downloadedFile = Uri.fromFile(file);
                    DownloadManager dm = (DownloadManager) requireActivity().getSystemService(DOWNLOAD_SERVICE);
                    request.addRequestHeader("Cookie", cookie);
                    if (dm != null) {
                        dm.enqueue(request);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        webresults_WebView.loadUrl(weblink);
    }

    private final BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            downloadcomplete = true;
            saveSong_Button.setEnabled(true);
            // If the song save section isn't visible, make it so
            // This is because there was no chordpro, but pdf is here
            if (newfileinfo_LinearLayout.getVisibility()!=View.VISIBLE) {
                setFileNameAndFolder();
            }
            try {
                requireActivity().unregisterReceiver(onComplete);
            } catch (Exception e) {
                Log.d("d","Error unregistering receiver");
            }
        }
    };

    @JavascriptInterface
    public void processHTML(String html) {
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener != null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }







    private void setFileNameAndFolder() {

        // Hide the searchresults_RelativeLayout
        try {
            searchresults_RelativeLayout.setVisibility(View.GONE);
        } catch (Exception e){
            Log.d("d","Error hiding the search results");
        }
        try {
            newfileinfo_LinearLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Log.d("d","Error showing the new file info");
        }

        if (FullscreenActivity.phrasetosearchfor==null) {
            FullscreenActivity.phrasetosearchfor = "Untitled";
        }

        // Set the file name if we know it
        if (songfilename_EditText!=null) {
            if (filename != null && !filename.equals("")) {
                songfilename_EditText.setText(filename);
            } else {
                songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
            }

            if (filecontents == null && (newtext==null || newtext.equals(""))) {
                songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
            }
        }
    }

    private void doSaveSong() {

        // Get the name of the file we want to use
        String nameoffile = songfilename_EditText.getText().toString();
        if (nameoffile.equals("")) {
            nameoffile = filename;
        }

        // Fix the filename
        String nameofpdffile = nameoffile.replace(".pdf","")+".pdf";   // Gets rid of multilple .pdf extensions

        nameoffile = nameoffile.replace(".pdf","");
        if ((StaticVariables.whattodo.equals("chordie") || StaticVariables.whattodo.equals("songselect") ||
                StaticVariables.whattodo.equals("worshiptogether")) && !nameoffile.endsWith(".chopro")) {
            // Fix the title line in the lyrics

            if (filecontents == null) {
                filecontents = "";
            }
            String[] lines = filecontents.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String l:lines) {
                if (l.contains("{title:")) {
                    l = "{title:"+nameoffile+"}";
                }
                sb.append(l).append("\n");
            }
            filecontents = sb.toString();
            nameoffile = nameoffile + ".chopro";

        } else {
            // Last check of best practice OpenSong formatting
            newtext = textSongConvert.convertText(getActivity(),newtext);

            filecontents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>\n<title>" + PopUpEditSongFragment.parseToHTMLEntities(filename)
                    + "</title>\n<author>"
                    + PopUpEditSongFragment.parseToHTMLEntities(authorname) + "</author>\n<copyright></copyright>\n<lyrics>"
                    + PopUpEditSongFragment.parseToHTMLEntities(newtext) //Issues cause with &, so fix
                    + "</lyrics>\n</song>";
        }

        // Get the uri for the new file
        Uri uri_newfile = storageAccess.getUriForItem(getActivity(), preferences, "Songs", whatfolderselected, nameoffile);
        Uri uri_newpdffile;

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri_newfile, null,
                "Songs", whatfolderselected, nameoffile);

        // Get the outputstream
        OutputStream outputStream = storageAccess.getOutputStream(getActivity(),uri_newfile);
        OutputStream outputStreamPDF = null;

        // Get the inputstream of the downloaded file song select
        InputStream inputStream = null;

        // Get the song select pdf file stuff
        if (StaticVariables.whattodo.equals("songselect") && downloadcomplete) {
            uri_newpdffile = storageAccess.getUriForItem(getActivity(), preferences, "Songs", whatfolderselected, nameofpdffile);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri_newpdffile, null,
                    "Songs", whatfolderselected, nameofpdffile);

            outputStreamPDF = storageAccess.getOutputStream(getActivity(),uri_newpdffile);
            inputStream = storageAccess.getInputStream(getActivity(),downloadedFile);
        }

        // Get the database ready
        SQLiteHelper sqLiteHelper = new SQLiteHelper(getActivity());

        try {
            if (filecontents!=null && !filecontents.equals("")) {
                storageAccess.writeFileFromString(filecontents,outputStream);
                // Add song to the database
                sqLiteHelper.createSong(getActivity(),whatfolderselected,nameoffile);
            }

            if (StaticVariables.whattodo.equals("songselect") && downloadcomplete && outputStreamPDF!=null && inputStream!=null) {
                // Copy the orignal pdf file
                storageAccess.copyFile(inputStream,outputStreamPDF);
                // Add song to the database
                sqLiteHelper.createSong(getActivity(),whatfolderselected,nameofpdffile);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the folder and song to the one we've set here
        StaticVariables.whichSongFolder = whatfolderselected;
        if (filecontents!=null) {
            StaticVariables.songfilename = nameoffile;
        } else if (StaticVariables.whattodo.equals("songselect") && downloadcomplete) {
            StaticVariables.songfilename = nameofpdffile;
        }

        // Indicate after loading song (which renames it), we need to build the database and song index
        FullscreenActivity.needtorefreshsongmenu = true;

        if (mListener != null) {
            mListener.loadSong();
            // IV - Moved after load to better report details of the song
            // If we are autologging CCLI information
            if (preferences.getMyPreferenceBoolean(getActivity(),"ccliAutomaticLogging",false)) {
                PopUpCCLIFragment.addUsageEntryToLog(getActivity(), preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                    StaticVariables.songfilename, StaticVariables.mAuthor,
                    StaticVariables.mCopyright, StaticVariables.mCCLI, "1"); // Created
            }
            dismiss();
        }
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void loadSong();
    }


    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                newtempfolders = songFolders.prepareSongFolders(getActivity(),preferences);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String s) {
            try {
                // The song folder
                ArrayAdapter<String> folders = new ArrayAdapter<>(requireActivity(), R.layout.my_spinner, newtempfolders);
                folders.setDropDownViewResource(R.layout.my_spinner);
                choosefolder_Spinner.setAdapter(folders);

                // Select the current folder as the preferred one - i.e. rename into the same folder
                choosefolder_Spinner.setSelection(0);
                for (int w = 0; w < newtempfolders.size(); w++) {
                    if (FullscreenActivity.currentFolder.equals(newtempfolders.get(w)) ||
                            FullscreenActivity.currentFolder.equals("(" + newtempfolders.get(w) + ")")) {
                        choosefolder_Spinner.setSelection(w);
                        FullscreenActivity.newFolder = newtempfolders.get(w);
                    }
                }
            } catch (Exception e) {
                // Oops error
            }
        }
    }





}*/
