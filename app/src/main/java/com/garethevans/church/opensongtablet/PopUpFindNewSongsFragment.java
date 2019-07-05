package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

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
    TextSongConvert textSongConvert;
    StorageAccess storageAccess;
    Preferences preferences;
    private SongFolders songFolders;
    private Uri downloadedFile;

    static PopUpFindNewSongsFragment newInstance() {
        PopUpFindNewSongsFragment frag;
        frag = new PopUpFindNewSongsFragment();
        return frag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
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
        switch (FullscreenActivity.whattodo) {
            case "chordie":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.chordiesearch);
                break;
            case "songselect":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.songselect);
                break;
            case "worshiptogether":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.worshiptogether);
                break;
            case "ukutabs":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.ukutabs);
                break;
            case "worshipready":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.worshipready);
                break;
            case "holychords":
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.holychords);
                break;
            default:
                mTitle = Objects.requireNonNull(getActivity()).getResources().getString(R.string.ultimateguitarsearch);
                break;
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_findnewsongs, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(mTitle);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    CustomAnimations.animateFAB(closeMe,getActivity());
                    closeMe.setEnabled(false);
                    dismiss();
                } catch (Exception e) {
                    // Error cancelling
                }
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

        doSearch_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchtext = searchphrase_EditText.getText().toString();
                if (!searchtext.equals("")) {
                    FullscreenActivity.phrasetosearchfor = searchtext;
                    doSearch(searchtext);
                }
            }
        });

        webBack_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    webresults_WebView.goBack();
                } catch (Exception e) {
                    // Error going back in the web view
                }
            }
        });
        grabSongData_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grabchordpro();
            }
        });

        saveSong_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSaveSong();
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @SuppressLint({"SetJavaScriptEnabled", "deprecation", "AddJavascriptInterface"})
    private void doSearch(String searchtext) {

        searchresults_RelativeLayout.setVisibility(View.VISIBLE);
        searchtext_LinearLayout.setVisibility(View.GONE);

        switch (FullscreenActivity.whattodo) {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            webresults_WebView.getSettings().setUserAgentString(newUA);
        } else {
            webresults_WebView.getSettings().setUserAgentString(newUA);
        }
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
            Objects.requireNonNull(getActivity()).registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Log.d("d","Error registering download complete listener");
        }
        webresults_WebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
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
                        DownloadManager dm = (DownloadManager) Objects.requireNonNull(getActivity()).getSystemService(DOWNLOAD_SERVICE);
                        request.addRequestHeader("Cookie", cookie);
                        if (dm != null) {
                            dm.enqueue(request);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        webresults_WebView.loadUrl(weblink);
    }

    private BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            downloadcomplete = true;
            saveSong_Button.setEnabled(true);
            // If the song save section isn't visible, make it so
            // This is because there was no chordpro, but pdf is here
            if (newfileinfo_LinearLayout.getVisibility()!=View.VISIBLE) {
                setFileNameAndFolder();
            }
            try {
                Objects.requireNonNull(getActivity()).unregisterReceiver(onComplete);
            } catch (Exception e) {
                Log.d("d","Error unregistering receiver");
            }
        }
    };

    @JavascriptInterface
    public void processHTML(String html) {
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener != null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void grabchordpro() {
        // Need to run a async task to grab html text
        grabSongData_ProgressBar.setVisibility(View.VISIBLE);
        weblink = webresults_WebView.getUrl();
        StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getText(R.string.chordproprogress).toString();
        ShowToast.showToast(getActivity());
        DownloadWebTextTask task = new DownloadWebTextTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, weblink);

        // If we are in songselect, trigger the download to keep the stats live
        if (FullscreenActivity.whattodo.equals("songselect")) {
            try {
                // Trigger the download of the pdf
                webresults_WebView.loadUrl("javascript:document.getElementById('chordSheetDownloadButton').click()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void fixChordieContent(String resultposted) {
        // Song is from Chordie!
        String temptitle = FullscreenActivity.phrasetosearchfor;

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
        // Find the position of the start of this section
        int getstart = resultposted.indexOf("<textarea id=\"chordproContent\"");
        int startpos = resultposted.indexOf("\">", getstart) + 2;
        if (getstart < 1) {
            // We are using the other version of the file content
            getstart = resultposted.indexOf("<h1 class=\"titleLeft\"");
            startpos = resultposted.indexOf(">",getstart) + 1;

            // Remove everything before this position
            resultposted = resultposted.substring(startpos);

            // The title is everything up to &nbsp; or <span>
            int endpos = resultposted.indexOf("&nbsp;");
            if (endpos>100) {
                endpos = resultposted.indexOf("<span>");
            }
            if (endpos>0) {
                temptitle = resultposted.substring(0, endpos);
            }

            // The author is between the first <span> and </span>
            startpos = resultposted.indexOf("<span>")+6;
            endpos = resultposted.indexOf("</span>");
            if (endpos>startpos && startpos>-1 && endpos<200) {
                authorname = resultposted.substring(startpos,endpos);
            }

            // Get the rest of the song content
            startpos = resultposted.indexOf("<div");
            endpos = resultposted.indexOf("<div id=\"importantBox\"");

            if (startpos>-1 && startpos<endpos) {
                resultposted = resultposted.substring(startpos,endpos);
            }

            StringBuilder contents = new StringBuilder();
            // Split into lines
            String[] lines = resultposted.split("\n");
            for (String line:lines) {
                line = line.replaceAll("<(.*?)>", "");
                line = line.replaceAll("<(.*?)\n", "");
                line = line.replaceFirst("(.*?)>", "");
                line = line.replaceAll("&nbsp;", " ");
                line = line.replaceAll("&amp;", "&");

                if (!line.equals("")) {
                    contents.append(line);
                    contents.append("\n");
                }
            }
            filename = temptitle.trim();
            filecontents = contents.toString();

        } else {
            // Remove everything before this position - using the desktop version
            resultposted = resultposted.substring(startpos);

            // Find the position of the end of the form
            int endpos = resultposted.indexOf("</textarea>");
            if (endpos < 0) {
                endpos = resultposted.length();
            }
            resultposted = resultposted.substring(0, endpos);

            //Replace all \r with \n
            resultposted = resultposted.replace("\r", "\n");
            resultposted = resultposted.trim();

            // Try to get the title of the song from the metadata
            int startpostitle = resultposted.indexOf("{t:");
            int endpostitle = resultposted.indexOf("}", startpostitle);
            if (startpostitle >= 0 && startpostitle < endpostitle && endpostitle < startpostitle + 50) {
                temptitle = resultposted.substring(startpostitle + 3, endpostitle);
            }
            filename = temptitle.trim();
            filecontents = resultposted;
        }
    }

    private void fixWTContent(String resultposted) {
        // From Worship Together

        String[] linest = resultposted.split("\n");
        for (String l:linest) {
            Log.d("FindNewSongs",l);
        }

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted = null;
        String filenametosave = "WT Song";
        authorname = "";
        String copyright = "";
        String bpm = "";
        String ccli = "";
        StringBuilder lyrics = new StringBuilder();
        String key = "";

        // Get the song title
        int startpos = resultposted.indexOf("<title>");
        int endpos = resultposted.indexOf("</title>");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            title_resultposted = resultposted.substring(startpos + 7, endpos);
            title_resultposted = title_resultposted.trim();
            int pos_of_extra = title_resultposted.indexOf(" - ");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            pos_of_extra = title_resultposted.indexOf("Lyrics and Chords");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            title_resultposted = title_resultposted.replace("|","");

            pos_of_extra = title_resultposted.indexOf("Worship Together");
            if (pos_of_extra>-1) {
                title_resultposted = title_resultposted.substring(0,pos_of_extra);
            }
            filenametosave = title_resultposted.trim();
            filename = filenametosave;
        }

        String song_taxonomy;
        startpos = resultposted.indexOf("<div class=\"song_taxonomy\">");
        endpos = resultposted.indexOf("<div class=\"t-setlist-details__related-list\">");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            // Extract the song taxonomy so we can edit this bit quickly
            song_taxonomy = resultposted.substring(startpos,endpos);

            // Try to get the author data
            startpos = song_taxonomy.indexOf("Writer(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                authorname = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+10,endpos));
            }

            // Try to get the copyright data
            startpos = song_taxonomy.indexOf("Ministry(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                copyright = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+12,endpos));
            }

            // Try to get the bpm data
            startpos = song_taxonomy.indexOf("BPM:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                bpm = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+4,endpos));
            }

            // Try to get the ccli data
            startpos = song_taxonomy.indexOf("CCLI #:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                ccli = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+7,endpos));
            }

            // Try to get the key data
            startpos = song_taxonomy.indexOf("Original Key(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                key = getRidOfExtraCodeWT(song_taxonomy.substring(startpos+16,endpos));
            }

        }

        Log.d("FindNewSongs","title_resultposted="+title_resultposted);
        Log.d("FindNewSongs","author="+authorname);
        Log.d("FindNewSongs","copyright="+copyright);

        // Now try to get the chordpro file contents
        startpos = resultposted.indexOf("<div class='chord-pro-line'");
        endpos = resultposted.indexOf("<div class=\"song_taxonomy\">",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            lyrics = new StringBuilder(resultposted.substring(startpos, endpos));

            // Split the lines up
            String[] lines = lyrics.toString().split("\n");
            StringBuilder newline = new StringBuilder();
            lyrics = new StringBuilder();
            // Go through each line and do what we need
            for (String l : lines) {
                l = l.trim();
                Log.d("FindNewSongs",l);
                boolean emptystuff = false;
                if (l.equals("</div") || l.contains("<div class='chord-pro-br'>") ||
                        l.contains("<div class='chord-pro-segment'>") || l.contains("<div class=\"inner_col")) {
                    emptystuff = true;
                }

                if (!emptystuff && l.contains("<div class=\"chord-pro-disp\"")) {
                    // Start section, so initialise the newline and lyrics
                    lyrics = new StringBuilder();
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-line'>")) {
                    // Starting a new line, so add the previous newline to the lyrics text
                    lyrics.append("\n").append(newline);
                    newline = new StringBuilder();

                } else if (!emptystuff && l.contains("<div class='chord-pro-note'>")) {
                    // This is a chord
                    startpos = l.indexOf("<div class='chord-pro-note'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String chordbit = l.substring(startpos+2,endpos);
                        if (!chordbit.isEmpty()) {
                            newline.append("[").append(l, startpos + 2, endpos).append("]");
                            //newline.append("[").append(l.substring(startpos + 2, endpos)).append("]");
                        }
                    }


                } else if (!emptystuff && l.contains("<div class='chord-pro-lyric'>")) {
                    // This is lyrics
                    startpos = l.indexOf("<div class='chord-pro-lyric'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        newline.append(l, startpos + 2, endpos);
                        //newline.append(l.substring(startpos + 2, endpos));
                    }
                }

            }

        }

        // Build the chordpro file contents:
        filecontents  = "{title:"+filenametosave+"}\n";
        filecontents += "{artist:"+authorname+"}\n";
        filecontents += "{copyright:"+copyright+"}\n";
        filecontents += "{ccli:"+ccli+"}\n";
        filecontents += "{key:"+key+"}\n";
        filecontents += "{tempo:"+bpm+"}\n\n";
        filecontents += lyrics.toString().trim();

        if (lyrics.toString().trim().isEmpty() || lyrics.toString().trim().equals("")) {
            filecontents = null;
        }
    }

    private String getRidOfExtraCodeWT(String s) {
        s = s.replace("<strong>","");
        s = s.replace("</strong>","");
        s = s.replace("<p>","");
        s = s.replace("</p>","");
        s = s.replace("</a>","");
        s = s.replace("<a>","");
        s = s.replace("<span>","");
        s = s.replace("</span>","");
        while (s.contains("<a href")) {
            // Remove the hypertext references
            int startpos = s.indexOf("<a href");
            int endpos = s.indexOf("'>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                String bittoremove = s.substring(startpos,endpos+2);
                s = s.replace(bittoremove,"");
            } else {
                // Problem, so just get rid of it all
                s = "";
            }
        }
        s = s.replace("\n","");
        s = s.trim();
        return s;
    }

    private void fixUGContent(String resultposted) {
        // From ultimate guitar

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted;
        String filenametosave = "UG Song";
        authorname = "";
        String author_resultposted;


        int startpos = resultposted.indexOf("<title>");
        int endpos = resultposted.indexOf("</title>");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            title_resultposted = resultposted.substring(startpos + 7, endpos);
            title_resultposted = title_resultposted.replace("\r", "");
            title_resultposted = title_resultposted.replace("\n", "");
            title_resultposted = title_resultposted.trim();
            title_resultposted = title_resultposted.replace("&amp;","&");
            title_resultposted = title_resultposted.replace("&","&amp;");
            filenametosave = title_resultposted.replace("&amp","");
            filenametosave = filenametosave.replace(" @ Ultimate-Guitar.Com", "");
            filenametosave = filenametosave.replace(" Chords", "");
            int authstart = filenametosave.indexOf(" by ");
            if (authstart > -1) {
                authorname = filenametosave.substring(authstart + 4);
                filenametosave = filenametosave.substring(0, authstart);
            }
        }

        // Look for a better title
        // Normal site
        startpos = resultposted.indexOf("song:");
        if (startpos > -1) {
            // Remove everything before this position
            if (startpos != 0) {
                title_resultposted = resultposted.substring(startpos);
                title_resultposted = title_resultposted.replace("&amp;","&");
                title_resultposted = title_resultposted.replace("&","&amp;");
                endpos = title_resultposted.indexOf(",\n");
                if (endpos < 0) {
                    endpos = 0;
                }
                //Bit with song title is in here hopefully
                if (endpos > 5) {
                    filenametosave = title_resultposted.substring(5, endpos);
                    filenametosave = filenametosave.replace("\"", "");
                    filenametosave = filenametosave.replace("&amp;","");
                    filenametosave = filenametosave.trim();
                } else {
                    filenametosave = "*temp*";
                }
            }
        }

        // Mobile site
        startpos = resultposted.indexOf("song_name:") + 12;
        endpos = resultposted.indexOf("',", startpos);
        if (endpos < startpos + 40) {
            title_resultposted = resultposted.substring(startpos, endpos);
            title_resultposted = title_resultposted.replace("&amp;","&");
            title_resultposted = title_resultposted.replace("&","&amp;");
            filenametosave = title_resultposted.replace("&amp;","");
        }

        // Look for a better author
        // Desktop site
        startpos = resultposted.indexOf("artist:");
        if (startpos > -1) {

            // Remove everything before this position
            if (startpos != 0) {
                author_resultposted = resultposted.substring(startpos);
                author_resultposted = author_resultposted.replace("&amp;","&");
                author_resultposted = author_resultposted.replace("&","&amp;");
                endpos = author_resultposted.indexOf(",\n");
                if (endpos < 0) {
                    endpos = 0;
                }
                //Bit with song author is in here hopefully
                if (endpos > 6) {
                    authorname = author_resultposted.substring(6, endpos);
                    authorname = authorname.replace("\"", "");
                    authorname = authorname.replace("&amp;","&");
                    authorname = authorname.replace("&","&amp;");
                    authorname = PopUpEditSongFragment.parseToHTMLEntities(authorname.trim());
                } else {
                    authorname = "";
                }
            }
        }

        // Mobile site
        startpos = resultposted.indexOf("artist_name:") + 14;
        endpos = resultposted.indexOf("',", startpos);
        if (endpos < startpos + 80) {
            author_resultposted = resultposted.substring(startpos, endpos);
            author_resultposted = author_resultposted.replace("&amp;","&");
            author_resultposted = author_resultposted.replace("&","&amp;");
            authorname = PopUpEditSongFragment.parseToHTMLEntities(author_resultposted);
        }

        // Try to find the title of the song from the keywords
        startpos = resultposted.indexOf("<meta name=\"keywords\" content=\"");
        endpos = resultposted.indexOf("\">", startpos + 31);
        if (startpos > -1 && endpos > startpos) {
            String tempkeywords = resultposted.substring(startpos + 31, endpos);
            // Split the keywords by commas
            String[] keywords = tempkeywords.split(",");
            if (keywords.length > 0) {
                title_resultposted = keywords[0];
                title_resultposted = title_resultposted.replace("&amp;","&");
                title_resultposted = title_resultposted.replace("&","&amp;");
                title_resultposted = title_resultposted.replace(" @ Ultimate-Guitar.Com", "");
                title_resultposted = title_resultposted.replace(" Chords", "");
                filenametosave = title_resultposted.replace("&amp;","");
            }
        }

        // Find the position of the start of this section
        startpos = resultposted.indexOf("<div class=\"tb_ct\">");
        if (startpos < 0) {
            startpos = 0;
        }
        // Remove everything before this position
        resultposted = resultposted.substring(startpos);

        // Find the ultimate guitar promo text start
        startpos = resultposted.indexOf("<pre class=\"print-visible\">");
        if (startpos < 0) {
            startpos = 0;
        }
        // Remove everything before this position
        resultposted = resultposted.substring(startpos + 27);

        // Mobile version
        startpos = resultposted.indexOf("<div class=\"ugm-b-tab--content js-tab-content\">");
        if (startpos < 0) {
            startpos = 0;
        }
        // Remove everything before this position
        resultposted = resultposted.substring(startpos + 47);

        // Find the text start
        startpos = resultposted.indexOf("<pre>");
        if (startpos > -1 && startpos < 500) {
            // Remove everything before this position
            resultposted = resultposted.substring(startpos + 5);
        }

        // For the mobile version
        startpos = resultposted.indexOf("<pre class=\"js-tab-content\">");
        if (startpos >= 0) {
            resultposted = resultposted.substring(startpos + 28);
        }

        // For the mobile version
        startpos = resultposted.indexOf("<pre class=\"js-tab-content\">");
        if (startpos >= 0) {
            resultposted = resultposted.substring(startpos + 28);
        }

        // Alternative start point
        startpos = resultposted.indexOf("<pre class=\"\">");
        if (startpos >= 0) {
            resultposted = resultposted.substring(startpos + 14);
        }

        // Find the position of the end of the form
        endpos = resultposted.indexOf("</pre>");
        if (endpos < 0) {
            endpos = resultposted.length();
        }
        resultposted = resultposted.substring(0, endpos);

        //Replace all \r with \n
        resultposted = resultposted.replace("\r", "\n");

        // Split into lines
        String[] templines = resultposted.split("\n");

        // Go through each line and look for chord lines
        // These have <span> in them
        int numlines = templines.length;
        StringBuilder sb = new StringBuilder();

        for (int q = 0; q < numlines; q++) {
            if (templines[q].contains("<span>") || templines[q].contains("<span class=\"text-chord js-tab-ch\">") ||
                    templines[q].contains("<span class=\"text-chord js-tab-ch js-tapped\">")) {
                // Identify chord lines
                templines[q] = "." + templines[q];
            }
            if (StaticVariables.locale==null) {
                StaticVariables.locale = Locale.getDefault();
            }
            if (templines[q] != null && !templines[q].startsWith(".") && (templines[q].toLowerCase(StaticVariables.locale).contains(Objects.requireNonNull(getActivity()).getResources().getString(R.string.tag_verse).toLowerCase(StaticVariables.locale)) && templines[q].length() < 12 || templines[q].toLowerCase(StaticVariables.locale).contains(getActivity().getResources().getString(R.string.tag_chorus).toLowerCase(StaticVariables.locale)) && templines[q].length() < 12 || templines[q].toLowerCase(StaticVariables.locale).contains(getActivity().getResources().getString(R.string.tag_bridge).toLowerCase(StaticVariables.locale)) && templines[q].length() < 12)) {
                // Looks like a tag
                templines[q] = "[" + templines[q].trim() + "]";
            }
            if (templines[q]!=null && templines[q].indexOf("[") != 0 && templines[q].indexOf(".") != 0) {
                // Identify lyrics lines
                templines[q] = " " + templines[q];
            }
            sb.append(templines[q]).append("\n");
        }

        newtext = sb.toString();

        // Ok remove all html tags
        newtext = newtext.replace("<span>", "");
        // The desktop version of the site has chords inside [ch] [/ch] sections
        newtext = newtext.replace("[ch]","");
        newtext = newtext.replace("[/ch]","");
        newtext = newtext.replace("<div class=\"text-tab js-tab-tab\">","");
        newtext = newtext.replace("</div>","");
        newtext = newtext.replace("<span class=\"text-chord js-tab-ch\">", "");
        newtext = newtext.replace("<span class=\"text-chord js-tab-ch js-tapped\">", "");
        newtext = newtext.replace("</span>", "");
        newtext = newtext.replace("[[", "[");
        newtext = newtext.replace("]]", "]");
        newtext = newtext.replace("\n [", "\n[");
        newtext = newtext.replace("]\n \n", "]\n");
        newtext = newtext.replace("<i>", "");
        newtext = newtext.replace("</i>", "");
        newtext = newtext.replace("<b>", "");
        newtext = newtext.replace("</b>", "");
        newtext = newtext.replace("</", "");
        newtext = newtext.replace("/>", "");
        newtext = newtext.replace("<", "");
        newtext = newtext.replace(">", "");
        newtext = newtext.replace("&#039;","'");
        newtext = newtext.replace("&amp;","&");
        newtext = newtext.replace("&quot;","\"");
        newtext = TextUtils.htmlEncode(newtext);

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }

    private void fixUkutabsContent(String resultposted) {
        // From UkuTabs.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default
        String title_resultposted;
        String filenametosave = "UkuTabs Song";
        authorname = "";
        newtext = "";

        int start;
        int end;

        resultposted = resultposted.replace("&quot;","'");
        resultposted = resultposted.replace("&amp;","&");
        resultposted = resultposted.replace("&#39;","'");
        resultposted = resultposted.replace("&#039;","'");

        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            start = resultposted.indexOf("<title>") + 7;
            end = resultposted.indexOf("</title>");
            if (start>-1 && end>-1 && end>start) {
                String meta = resultposted.substring(start, end);
                if (meta.contains(" by ")) {
                    String[] bits = meta.split(" by ");
                    if (bits[0]!=null) {
                        if (bits[0].startsWith("'")) {
                            bits[0] = bits[0].substring(1);
                        }
                        if (bits[0].endsWith("'")) {
                            bits[0] = bits[0].substring(0,bits[0].length()-1);
                        }
                        filenametosave = bits[0];
                    }
                    if (bits[1]!=null) {
                        if (bits[1].startsWith("'")) {
                            bits[1] = bits[1].substring(1);
                        }
                        if (bits[1].endsWith("'")) {
                            bits[1] = bits[1].substring(0,bits[1].indexOf("'")-1);
                        }
                        authorname = bits[1];
                    }
                }
            }
        }

        // The meta stuff begins after the last typeof="v:Breadcrumb" text
        if (resultposted.contains("v:Breadcrumb") && resultposted.contains("post-meta")) {
            start = resultposted.lastIndexOf("v:Breadcrumb");
            end = resultposted.indexOf("post-meta",start);
            if (start>-1 && end>-1 && end>start) {
                String metadata = resultposted.substring(start,end);

                // Remove the rubbish and get the author
                start = metadata.indexOf("v:title");
                end = metadata.indexOf("</a>", start);
                if (start>-1 && end>-1 && end>start) {
                    metadata = metadata.substring(start);
                    start = metadata.indexOf(">") + 1;
                    authorname = metadata.substring(start, end);
                }

                // Remove the rubbish and get the title
                start = metadata.indexOf("breadcrumb_last\">");
                end = metadata.indexOf("</strong>",start);
                if (start>-1 && end>-1 && end>start) {
                    start = start + 17;
                    title_resultposted = metadata.substring(start, end);
                    title_resultposted = title_resultposted.replace("&apos;","'");
                    filenametosave = title_resultposted;
                }
            }
        }

        // Now try to extract the lyrics
        start = resultposted.indexOf("<pre class=\"qoate-code\">");
        end = resultposted.indexOf("</pre>");
        String templyrics = "";
        if (start>-1 && end>-1 && end>start) {
            templyrics = resultposted.substring(start + 24, end);
        }

        StringBuilder sb = new StringBuilder();

        // Split the lyrics into lines
        String[] lines = templyrics.split("\n");
        for (String l:lines) {

            // Remove the stuff we don't want
            l = l.replace("<span>","");
            l = l.replace("</span>","");

            // Try to sort the tags
            l = l.replace("<strong>","[");
            l = l.replace("</strong>","]");
            l = l.replace("]:","]");

            // Identify the chord lines
            boolean chordline = false;
            if (l.contains("<a")) {
                chordline = true;
            }

            // Remove any hyperlinks
            while (l.contains("<a")) {
                start = l.indexOf("<a");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</a>")) {
                l = l.replace("</a>","");
            }
            while (l.contains("<a>")) {
                l = l.replace("<a>","");
            }

            if (chordline) {
                l = "." + l;
            }

            // If we have tags and chords, split them
            if (l.startsWith(".") && l.contains("[") && l.contains("]")) {
                l = l.replace(".[", "[");
                l = l.replace("]","]\n.");
            }

            // Remove italics
            while (l.contains("<i ")) {
                start = l.indexOf("<i ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }
            while (l.contains("</i>")) {
                l = l.replace("</i>","");
            }

            // Remove images
            while (l.contains("<img ")) {
                start = l.indexOf("<img ");
                end = l.indexOf(">", start);
                String remove = l.substring(start, end);
                l = l.replace(remove,"");
            }

            l = l.replaceAll("<(.*?)>", "");
            l = l.replaceAll("<(.*?)\n", "");
            l = l.replaceFirst("(.*?)>", "");
            l = l.replaceAll("&nbsp;", " ");
            l = l.replaceAll("&amp;", "&");

            l = l.replace(">","");
            l = l.replace("<","");

            // Add a blank space to the beginning of lyrics lines
            if (!l.startsWith(".") && !l.startsWith("[")) {
                l = " " + l;
            }

            sb.append(l).append("\n");
        }

        newtext = TextUtils.htmlEncode(sb.toString());

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }

    private void fixHolyChordsContent(String resultposted) {
        // from holychords.com
        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        String[] lines = resultposted.split("\n");
        for (String line:lines) {
            Log.d("FindNewSongs","line: "+line);
        }
        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted = "HolyChords Song";
        String filenametosave;
        authorname = "";
        int startpos;
        int endpos;

        // Try to get the best title
        // First up, use the page title
        if (resultposted.contains("<title>") && resultposted.contains("</title>")) {
            startpos = resultposted.indexOf("<title>") + 7;
            endpos = resultposted.indexOf("</title>");
            if (endpos>startpos) {
                title_resultposted = resultposted.substring(startpos,endpos);
                authorname = title_resultposted;
            }
        }

        // Fix author and title (if it was copied as the title);
        String text = authorname.replace("|","___");
        String[] titlebits = text.split("___");
        if (titlebits.length>1) {
            title_resultposted = titlebits[0].trim();
            authorname = titlebits[1].trim();
        }

        // If there is the title tag, use this instead
        startpos = resultposted.indexOf("<meta property=\"og:site_name\" content=\"") + 39;
        endpos = resultposted.indexOf(">",startpos);
        if (startpos>-1 && endpos>-1 && endpos>startpos) {
            title_resultposted = resultposted.substring(startpos,endpos);
            title_resultposted = title_resultposted.replace("/","");
            title_resultposted = title_resultposted.replace("/","");
            title_resultposted = title_resultposted.replace("\"","");
            title_resultposted = title_resultposted.trim();
        }

        filenametosave = title_resultposted;

        // Everything is found inside the <pre  and </pre> tags
        startpos = resultposted.indexOf("<pre");
        startpos = resultposted.indexOf(">",startpos) + 1;
        // Remove everything before this
        resultposted = resultposted.substring(startpos);
        // Get everything in the <pre> section
        endpos = resultposted.indexOf("</pre");
        if (endpos>0) {
            resultposted = resultposted.substring(0,endpos);
        }

        newtext = resultposted.replace("<br>","\n");
        newtext = newtext.replace("<br />","");

        newtext = PopUpEditSongFragment.parseToHTMLEntities(newtext);

        if (!filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }
    }


    // Song Select Code
    private String extractSongSelectChordPro(String s, String temptitle) {
        // Get the title
        String title = getTitleSongSelectChordPro(s, temptitle);

        // Extract the key
        String key = getKeySongSelectChordPro(s);

        // Extract the author
        String author = getAuthorSongSelectChordPro(s);

        // Extract the tempo and time signature
        String tempo = getTempoSongSelectChordPro(s);
        String timesig = getTimeSigSongSelectChordPro(s);

        // Extract the CCLI song number
        String ccli = getCCLISongSelectChordPro(s);

        // Extract the Copyright info
        String copyright = getCopyrightSongSelectChordPro(s);

        // Extract the lyrics
        String lyrics =  getLyricsSongSelectChordPro(s);

        // Return the ChordPro version of the song
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + key + tempo + timesig + "\n" + lyrics;
        }
    }
    private String getTitleSongSelectChordPro(String s, String temptitle) {
        // Extract the title
        int start = s.indexOf("<span class=\"cproTitle\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String t = s.substring(start+24,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                t = Html.fromHtml(t, 0).toString();
            } else {
                t = Html.fromHtml(t).toString();
            }
            filename = t;
            return "{title:" + t + "}\n";
        } else {
            return temptitle;
        }
    }
    private String getKeySongSelectChordPro(String s) {
        int start = s.indexOf("<code class=\"cproSongKey\"");
        int end = s.indexOf("</code></span>",start);
        if (start>-1 && end>-1 && end>start) {
            // Fine tine the start
            int newstart = s.indexOf(">",start);
            if (newstart<0) {
                newstart = start;
            }
            return "{key:" + s.substring(newstart+1,end).trim() + "}\n";
        } else {
            return "";
        }
    }
    private String getAuthorSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproAuthors\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String a = s.substring(start+26,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                a = Html.fromHtml(a, 0).toString();
            } else {
                a = Html.fromHtml(a).toString();
            }
            return "{artist:" + a + "}\n";
        } else {
            return "";
        }
    }
    private String getCCLISongSelectChordPro(String s) {
        int start = s.indexOf("CCLI Song #");
        int end = s.indexOf("</p>",start);
        if (start>-1 && end>-1 && end>start) {
            return "{ccli:" + s.substring(start+11,end).trim() + "}\n";
        } else {
            return "";
        }
    }
    private String getCopyrightSongSelectChordPro(String s) {
        int start = s.indexOf("<ul class=\"copyright\">");
        start = s.indexOf("<li>",start);
        int end = s.indexOf("</li>",start);
        if (start>-1 && end>-1 && end>start) {
            return "{copyright:" + s.substring(start+4,end).trim() + "}\n";
        } else {
            return "";
        }
    }
    private String getTempoSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String both = s.substring(start+35,end);
            String[] bits = both.split("\\|");
            if (bits.length>0) {
                String t = bits[0].replace("Tempo", "");
                t = t.replace("-", "");
                t = t.replace("bpm", "");
                t = t.replace("BPM", "");
                t = t.replace("Bpm", "");
                t = t.trim();
                return "{tempo:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }
    private String getTimeSigSongSelectChordPro(String s) {
        int start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        int end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String both = s.substring(start+35,end);
            String[] bits = both.split("\\|");
            if (bits.length>1) {
                String t = bits[1].replace("Time","");
                t = t.replace("-","");
                t = t.trim();
                return "{time:" + t + "}\n";
            } else {
                return "";
            }
        }
        return "";
    }
    private String getLyricsSongSelectChordPro(String s) {
        int start = s.indexOf("<pre class=\"cproSongBody\">");
        int end = s.indexOf("</pre>",start);
        if (start>-1 && end>-1 && end>start) {
            String lyrics = s.substring(start+26,end);

            // Look at the lyrics
            String[] lines = lyrics.split("\n");
            for (String line:lines) {
                Log.d("FindNewSongs","line: "+line);
            }

            // Fix the song section headers
            while (lyrics.contains("<span class=\"cproSongSection\"><span class=\"cproComment\">")) {
                start = lyrics.indexOf("<span class=\"cproSongSection\"><span class=\"cproComment\">");
                end = lyrics.indexOf("</span>",start);
                String sectiontext;
                if (start>-1 && end>-1 && end>start) {
                    sectiontext = lyrics.substring(start+56,end);
                    lyrics = lyrics.replace("<span class=\"cproSongSection\"><span class=\"cproComment\">"+sectiontext+"</span>",sectiontext.trim()+":");
                }
            }

            // Fix the chords
            // Chords are found in a bit like this:
            // <span class="chordWrapper"><code class="chord" data-chordname="D<sup>2</sup>">D<sup>2</sup></code>
            // We wand the last D<sup>2</sup> bit (<sup> removed later).

            while (lyrics.contains("<span class=\"chordWrapper\"><code ")) {
                start = lyrics.indexOf("<span class=\"chordWrapper\"><code ");
                int newstart = lyrics.indexOf(">",start); // Move to bit before <
                newstart = lyrics.indexOf("\">",newstart)+2; // Go to bit after chordname="....">
                end = lyrics.indexOf("</code>",newstart);
                if (start>-1 && newstart>-1 && end>-1 && end>newstart) {
                    String chordfound = lyrics.substring(newstart,end);
                    String bittoremove = lyrics.substring(start,end+7);
                    lyrics = lyrics.replace(bittoremove,"["+chordfound+"]");
                }
            }

            // Get rid of code that we don't need
            return getRidOfRogueCode(lyrics);
        }
        return "";
    }

    private String extractSongSelectUsr(String s, String temptitle) {
        String title = temptitle;
        String author = "";
        String copyright = "";
        String ccli = "";
        String lyrics = "";

        int start;
        int end;

        start = s.indexOf("<div id=\"LyricsText\" style=\"display: none;\">");
        end = s.indexOf("</div>", start);
        if (start > -1 && end > -1 && end > start) {
            int newstart = s.indexOf(">", start);
            if (newstart < 0) {
                newstart = start;
            }
            String text = s.substring(newstart + 1, end).trim();

            // The first line is the title normally
            end = text.indexOf("\n");
            if (end > -1) {
                title = "{title:" + text.substring(0, end).trim() + "}\n";
                filename = text.substring(0, end).trim();
                text = text.substring(end).trim();
            }

            // Get the bottom bit
            String bottombit;
            start = text.indexOf("CCLI Song");
            if (start>-1) {
                bottombit = text.substring(start);
                // Remove this from the text (leaving the lyrics)
                text = text.replace(bottombit,"");

                // Now look for the stuff we want
                // Break it into lines
                String[] bottomlines = bottombit.split("\n");
                for (String line:bottomlines) {
                    // Is this the CCLI line?
                    if (line.contains("CCLI Song #")) {
                        line = line.replace("CCLI Song #","");
                        line = line.trim();
                        ccli = "{ccli:" + line + "}\n";

                    // Is this the copyright line?
                    } else if (line.contains("opyright") || line.contains("&#169;") || line.contains("©")) {
                        copyright = "{copyright:" + line.trim() + "}\n";

                    // Is this the author line?
                    } else if (!line.contains("For use solely") && !line.contains("Note:") && !line.contains("Licence No")) {
                        author = "{artist:" + line.trim() + "}\n";
                    }
                }

            }

            lyrics = text;
        }
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + "\n" + lyrics;
        }
    }

    private String getRidOfRogueCode(String lyrics) {
        // Get rid of lyric indications
        lyrics = lyrics.replace("<span class=\"chordLyrics\">","");

        // Get rid of the new line indications
        lyrics = lyrics.replace("<span class=\"cproSongLine\">","");

        // Get rid of the chord line only indications
        lyrics = lyrics.replace("<span class=\"cproSongLine chordsOnly\">","");

        // Get rid of directions indicators
        lyrics = lyrics.replace("<span class=\"cproDirectionWrapper\">","");
        lyrics = lyrics.replace("<span class=\"cproDirection\">","");

        // Get rid of any remaining close spans
        lyrics = lyrics.replace("</span>","");

        // Get rid of any superscripts or subscripts
        lyrics = lyrics.replace("<sup>","");
        lyrics = lyrics.replace("</sup>","");

        // Look at the lyrics
        String[] lines = lyrics.split("\n");
        for (String line:lines) {
            Log.d("FindNewSongs","fixed: "+line);
        }

        // Finally, trim the lyrics
        return lyrics.trim();
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
        if ((FullscreenActivity.whattodo.equals("chordie") || FullscreenActivity.whattodo.equals("songselect") ||
                FullscreenActivity.whattodo.equals("worshiptogether")) && !nameoffile.endsWith(".chopro")) {
            // Fix the title line in the lyrics
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
        if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
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

            if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete && outputStreamPDF!=null && inputStream!=null) {
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
        } else if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
            StaticVariables.songfilename = nameofpdffile;
        }

        // If we are autologging CCLI information
        if (preferences.getMyPreferenceBoolean(getActivity(),"ccliAutomaticLogging",false)) {
            PopUpCCLIFragment.addUsageEntryToLog(getActivity(), preferences, StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename,
                    StaticVariables.songfilename, "",
                    "", "", "1"); // Created
        }

        // Indicate after loading song (which renames it), we need to build the database and song index
        FullscreenActivity.needtorefreshsongmenu = true;

        if (mListener != null) {
            mListener.loadSong();
            dismiss();
        }
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void loadSong();
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadWebTextTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... addresses) {
            String response = "";
            StringBuilder sb = new StringBuilder();
            for (String address : addresses) {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(address);

                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        sb.append("\n").append(s);
                        if (s.contains("<div class=\"fb-meta\">") ||
                                s.contains("<div class=\"plus-minus\">") ||
                                s.contains("<section class=\"ugm-ad ugm-ad__bottom\">")) {
                            // Force s to be null as we've got all we need!
                            break;
                        }
                    }
                    response = sb.toString();
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Split the result into lines
            //Now look to see if the webcontent has the ChordPro text in it
            // Check we aren't trying to use the tab-pro page!
            try {
                String address = webresults_WebView.getUrl();
                if (address != null && (address.contains("/tab-pro/") || address.contains("/chords-pro/"))) {
                    StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getText(R.string.not_allowed).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                } else if (result != null && (result.contains("<textarea id=\"chordproContent\"") ||
                result.contains("<h1 class=\"titleLeft\""))) {
                    // Using Chordie
                    fixChordieContent(result);
                    setFileNameAndFolder();

                } else if (result != null && (result.contains("<div class=\"tb_ct\">") || result.contains("ultimate-guitar"))) {
                    // Using UG
                    fixUGContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("http://worship-songs-resources.worshiptogether.com/")) {
                    // Using WorshipTogether
                    fixWTContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("UkuTabs")) {
                    // Using UkuTabs.com
                    fixUkutabsContent(result);
                    setFileNameAndFolder();

                } else if (result!=null && result.contains("CCLI")) {
                    // Using SongSelect chord page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("<div id=\"LyricsText\"")) {
                    // Using SongSelect USR page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("holychords.com")) {
                    // Using Holychords page
                    fixHolyChordsContent(result);
                    setFileNameAndFolder();

                } else {
                    StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                if (getActivity()!=null) {
                    StaticVariables.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFolders extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                newtempfolders = songFolders.prepareSongFolders(getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String s) {
            try {
                // The song folder
                ArrayAdapter<String> folders = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, newtempfolders);
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

    private class MyJavaScriptInterface {
        @SuppressWarnings("unused")
        @JavascriptInterface
        public void processHTML(final String html) {
            GetSourceCode getsource = new GetSourceCode(html);
            getsource.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetSourceCode extends AsyncTask<Object, String, String> {

        String html;
        GetSourceCode(String s) {
            html = s;
        }

        @Override
        protected String doInBackground(Object... objects) {
            if (html.contains("<div id=\"LyricsText\"")) {
                filecontents = extractSongSelectUsr(html, FullscreenActivity.phrasetosearchfor);
            } else {
                filecontents = extractSongSelectChordPro(html, FullscreenActivity.phrasetosearchfor);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity()!=null) {
                if (filecontents != null && !filecontents.equals("")) {
                    setFileNameAndFolder();
                } else {
                    if (downloadcomplete) {
                        StaticVariables.myToastMessage = getActivity().getString(R.string.pdfonly);
                    } else {
                        StaticVariables.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                    }

                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
