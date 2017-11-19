package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
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
import android.support.design.widget.FloatingActionButton;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.DOWNLOAD_SERVICE;

public class PopUpFindNewSongsFragment extends DialogFragment {

    LinearLayout searchtext_LinearLayout;
    EditText searchphrase_EditText;
    RelativeLayout searchresults_RelativeLayout;
    WebView webresults_WebView;
    ImageButton webBack_ImageButton;
    Button grabSongData_Button;
    ProgressBar grabSongData_ProgressBar;
    Button doSearch_Button;
    LinearLayout newfileinfo_LinearLayout;
    EditText songfilename_EditText;
    Spinner choosefolder_Spinner;
    Button saveSong_Button;
    String weblink = "http://www.ultimate-guitar.com/";
    String response;
    String filename;
    String author;
    String authorname;
    String folder;
    String filecontents;
    String newtext;
    AsyncTask<Object, Void, String> getfolders;
    ArrayList<String> newtempfolders;
    String whatfolderselected;
    String mTitle = "";
    private MyInterface mListener;
    String originaltemppdffile;
    boolean downloadcomplete = false;

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
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switch (FullscreenActivity.whattodo) {
            case "chordie":
                mTitle = getActivity().getResources().getString(R.string.chordiesearch);
                break;
            case "songselect":
                mTitle = getActivity().getResources().getString(R.string.songselect);
                break;
            case "worshiptogether":
                mTitle = getActivity().getResources().getString(R.string.worshiptogether);
                break;
            default:
                mTitle = getActivity().getResources().getString(R.string.ultimateguitarsearch);
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
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        searchtext_LinearLayout = V.findViewById(R.id.searchtext_LinearLayout);
        searchphrase_EditText = V.findViewById(R.id.searchphrase_EditText);
        searchresults_RelativeLayout = V.findViewById(R.id.searchresults_RelativeLayout);
        webresults_WebView = V.findViewById(R.id.webresults_WebView);
        webBack_ImageButton = V.findViewById(R.id.webBack_ImageButton);
        grabSongData_Button = V.findViewById(R.id.grabSongData_Button);
        grabSongData_ProgressBar = V.findViewById(R.id.grabSongData_ProgressBar);
        doSearch_Button = V.findViewById(R.id.doSearch_Button);
        newfileinfo_LinearLayout = V.findViewById(R.id.newfileinfo_LinearLayout);
        songfilename_EditText = V.findViewById(R.id.songfilename_EditText);
        choosefolder_Spinner = V.findViewById(R.id.choosefolder_Spinner);
        saveSong_Button = V.findViewById(R.id.saveSong_Button);

        // Set the text if it exists
        searchphrase_EditText.setText(FullscreenActivity.phrasetosearchfor);

        // Set the folder spinner
        getfolders = new GetFolders();
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

        return V;
    }

    @SuppressLint({"SetJavaScriptEnabled", "deprecation", "AddJavascriptInterface"})
    public void doSearch(String searchtext) {

        searchresults_RelativeLayout.setVisibility(View.VISIBLE);
        searchtext_LinearLayout.setVisibility(View.GONE);

        switch (FullscreenActivity.whattodo) {
            case "chordie":
                weblink = "http://www.chordie.com/results.php?q=" + searchtext + "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                break;
            case "ultimate-guitar":
                weblink = "http://www.ultimate-guitar.com/search.php?page=1&tab_type_group=text&app_name=ugt&order=myweight&type=300&title=" + searchtext;
                //weblink = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=" + searchtext;
                break;
            case "worshiptogether":
                weblink = "http://worship-songs-resources.worshiptogether.com/search?w=" + searchtext;
                break;
            case "worshipready":
                weblink = "http://www.worshipready.com/chord-charts";
                break;
            case "songselect":
                weblink = "https://songselect.ccli.com/Search/Results?SearchText=" + searchtext;
                break;
        }

        webresults_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        String newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
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
        webresults_WebView.loadUrl(weblink);
        webresults_WebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        try {
            getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
                    // Hide the WebView
                    searchresults_RelativeLayout.setVisibility(View.GONE);
                    FullscreenActivity.myToastMessage = "Downloading...";
                    saveSong_Button.setEnabled(false);
                    ShowToast.showToast(getActivity());

                    String cookie = CookieManager.getInstance().getCookie(url);

                    DownloadManager.Request request = new DownloadManager.Request(
                            Uri.parse(url));

                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                    originaltemppdffile = file.toString();
                    DownloadManager dm = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                    request.addRequestHeader("Cookie", cookie);
                    dm.enqueue(request);
                }

            }
        });
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            downloadcomplete = true;
            saveSong_Button.setEnabled(true);
            Log.d("d","Download complete");
            // If the song save section isn't visible, make it so
            // This is because there was no chordpro, but pdf is here
            if (newfileinfo_LinearLayout.getVisibility()!=View.VISIBLE) {
                setFileNameAndFolder();
            }
            try {
                getActivity().unregisterReceiver(onComplete);
            } catch (Exception e) {
                Log.d("d","Error unregistering receiver");
            }
        }
    };

    @JavascriptInterface
    public void processHTML(String html) {
        if (html == null) {
            Log.d("d","html is null");
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener != null) {
            mListener.pageButtonAlpha("");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public void grabchordpro() {
        // Need to run a async task to grab html text
        grabSongData_ProgressBar.setVisibility(View.VISIBLE);
        weblink = webresults_WebView.getUrl();
        FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.chordproprogress).toString();
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

    public void fixChordieContent(String resultposted) {
        // Song is from Chordie!
        String temptitle = FullscreenActivity.phrasetosearchfor;

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
        // Find the position of the start of this section
        int getstart = resultposted.indexOf("<textarea id=\"chordproContent\"");
        int startpos = resultposted.indexOf("\">", getstart) + 2;
        if (startpos < 1) {
            startpos = 0;
        }
        // Remove everything before this position
        resultposted = resultposted.substring(startpos);

        // Find the position of the end of the form
        int endpos = resultposted.indexOf("</textarea>");
        if (endpos < 0) {
            endpos = resultposted.length();
        }
        resultposted = resultposted.substring(0, endpos);

        //Replace all \r with \n
        resultposted = resultposted.replace("\r", "\n");
        resultposted = resultposted.replace("\'", "'");
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

    public void fixWTContent(String resultposted) {
        // From Worship Together

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        String title_resultposted;
        String filenametosave = "WT Song";
        authorname = "";
        String copyright = "";
        String bpm = "";
        String ccli = "";
        String lyrics = "";
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
        endpos = resultposted.indexOf("<div id=\"song_taxonomy_nav\">");
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            // Extract the song taxonomy so we can edit this bit quickly
            song_taxonomy = resultposted.substring(startpos,endpos);

            // Try to get the author data
            startpos = song_taxonomy.indexOf("Writer(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                authorname = song_taxonomy.substring(startpos+10,endpos);
                authorname = authorname.replace("<strong>","");
                authorname = authorname.replace("</strong>","");
                authorname = authorname.replace("</p>","");
                authorname = authorname.trim();
            }

            // Try to get the copyright data
            startpos = song_taxonomy.indexOf("Ministry(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                copyright = song_taxonomy.substring(startpos+12,endpos);
                copyright = copyright.replace("<strong>","");
                copyright = copyright.replace("</strong>","");
                copyright = copyright.replace("</p>","");
                copyright = copyright.replace("</a>","");
                while (copyright.contains("<a href") && copyright.contains("'>")) {
                    // Remove the hypertext references
                    startpos = copyright.indexOf("<a href");
                    endpos = copyright.indexOf("'>");
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String bittoremove = copyright.substring(startpos,endpos+2);
                        copyright = copyright.replace(bittoremove,"");
                    } else {
                        // Problem, so just get rid of it all
                        copyright = "";
                    }
                }
                copyright = copyright.trim();
            }

            // Try to get the bpm data
            startpos = song_taxonomy.indexOf("BPM:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                bpm = song_taxonomy.substring(startpos+4,endpos);
                bpm = bpm.replace("<strong>","");
                bpm = bpm.replace("</strong>","");
                bpm = bpm.replace("</p>","");
                bpm = bpm.replace("</a>","");
                bpm = bpm.trim();
            }

            // Try to get the ccli data
            startpos = song_taxonomy.indexOf("CCLI #:");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                ccli = song_taxonomy.substring(startpos+7,endpos);
                ccli = ccli.replace("<strong>","");
                ccli = ccli.replace("</strong>","");
                ccli = ccli.replace("</p>","");
                ccli = ccli.replace("</a>","");
                while (copyright.contains("<a href") && copyright.contains("'>")) {
                    // Remove the hypertext references
                    startpos = copyright.indexOf("<a href");
                    endpos = copyright.indexOf("'>");
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String bittoremove = copyright.substring(startpos,endpos+2);
                        copyright = copyright.replace(bittoremove,"");
                    } else {
                        // Problem, so just get rid of it all
                        copyright = "";
                    }
                }
                ccli = ccli.trim();
            }

            // Try to get the key data
            startpos = song_taxonomy.indexOf("Original Key(s):");
            endpos = song_taxonomy.indexOf("</div>",startpos);
            if (startpos > -1 && endpos > -1 && startpos < endpos) {
                key = song_taxonomy.substring(startpos+16,endpos);
                key = key.replace("<strong>","");
                key = key.replace("</strong>","");
                key = key.replace("</p>","");
                key = key.replace("</a>","");
                while (key.contains("<a href") && key.contains("'>")) {
                    // Remove the hypertext references
                    startpos = key.indexOf("<a href");
                    endpos = key.indexOf("'>");
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String bittoremove = key.substring(startpos,endpos+2);
                        key = key.replace(bittoremove,"");
                    } else {
                        // Problem, so just get rid of it all
                        key = "";
                    }
                }
                key = key.trim();
            }

        }

        // Now try to get the chordpro file contents
        startpos = resultposted.indexOf("<div class=\"chord-pro-disp\"");
        endpos = resultposted.indexOf("<h5>",startpos);
        if (startpos > -1 && endpos > -1 && startpos < endpos) {
            lyrics = resultposted.substring(startpos,endpos);

            // Split the lines up
            String[] lines = lyrics.split("\n");
            String newline = "";
            lyrics = "";
            // Go through each line and do what we need
            for (String l : lines) {
                l = l.trim();

                boolean emptystuff = false;
                if (l.equals("</div") || l.contains("<div class='chord-pro-br'>") ||
                        l.contains("<div class='chord-pro-segment'>") || l.contains("<div class=\"inner_col")) {
                    emptystuff = true;
                }

                if (!emptystuff && l.contains("<div class=\"chord-pro-disp\"")) {
                    // Start section, so initialise the newline and lyrics
                    lyrics = "";
                    newline = "";

                } else if (!emptystuff && l.contains("<div class='chord-pro-line'>")) {
                    // Starting a new line, so add the previous newline to the lyrics text
                    lyrics += "\n" + newline;
                    newline ="";

                } else if (!emptystuff && l.contains("<div class='chord-pro-note'>")) {
                    // This is a chord
                    startpos = l.indexOf("<div class='chord-pro-note'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        String chordbit = l.substring(startpos+2,endpos);
                        if (!chordbit.isEmpty() && !chordbit.equals("")) {
                            newline += "[" + l.substring(startpos + 2, endpos) + "]";
                        }
                    }


                } else if (!emptystuff && l.contains("<div class='chord-pro-lyric'>")) {
                    // This is lyrics
                    startpos = l.indexOf("<div class='chord-pro-lyric'>");
                    startpos = l.indexOf("'>",startpos);
                    endpos = l.indexOf("</div>",startpos);
                    if (startpos > -1 && endpos > -1 && startpos < endpos) {
                        newline += l.substring(startpos+2,endpos);
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
        filecontents += lyrics.trim();

        if (lyrics.trim().isEmpty() || lyrics.trim().equals("")) {
            filecontents = null;
        }
        Log.d("d","filecontents="+filecontents);
    }

    public void fixUGContent(String resultposted) {
        // From ultimate guitar

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        Log.d("d", "resultposted=" + resultposted);
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
            filenametosave = title_resultposted;
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
                endpos = title_resultposted.indexOf(",\n");
                if (endpos < 0) {
                    endpos = 0;
                }
                //Bit with song title is in here hopefully
                if (endpos > 5) {
                    filenametosave = title_resultposted.substring(5, endpos);
                    filenametosave = filenametosave.replace("\"", "");
                    filenametosave = filenametosave.trim();
                } else {
                    filenametosave = "*temp*";
                }
            }
        }

        // Mobile site
        startpos = resultposted.indexOf("song_name:") + 12;
        endpos = resultposted.indexOf("',", startpos);
        if (startpos != 0 && endpos < (startpos + 40)) {
            title_resultposted = resultposted.substring(startpos, endpos);
            filenametosave = title_resultposted;
        }

        // Look for a better author
        // Desktop site
        startpos = resultposted.indexOf("artist:");
        if (startpos > -1) {

            // Remove everything before this position
            if (startpos != 0) {
                author_resultposted = resultposted.substring(startpos);
                endpos = author_resultposted.indexOf(",\n");
                if (endpos < 0) {
                    endpos = 0;
                }
                //Bit with song author is in here hopefully
                if (endpos > 6) {
                    authorname = author_resultposted.substring(6, endpos);
                    authorname = authorname.replace("\"", "");
                    authorname = PopUpEditSongFragment.parseToHTMLEntities(authorname.trim());
                } else {
                    authorname = "";
                }
            }
        }

        // Mobile site
        startpos = resultposted.indexOf("artist_name:") + 14;
        endpos = resultposted.indexOf("',", startpos);
        if (startpos != 0 && endpos < (startpos + 80)) {
            author_resultposted = resultposted.substring(startpos, endpos);
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
                filenametosave = title_resultposted;
            }
        }

        Log.d("d", "filenametosave=" + filenametosave);
        Log.d("d", "authorname=" + authorname);

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
        if (startpos > -1 || startpos < 500) {
            // Remove everything before this position
            resultposted = resultposted.substring(startpos + 5);
        }

        // For the mobile version
        startpos = resultposted.indexOf("<pre class=\"js-tab-content\">");
        if (startpos >= 0) {
            resultposted = resultposted.substring(startpos + 28);
        }

        // Find the position of the end of the form
        endpos = resultposted.indexOf("</pre>");
        if (endpos < 0) {
            endpos = resultposted.length();
        }
        resultposted = resultposted.substring(0, endpos);

        //Replace all \r with \n
        resultposted = resultposted.replace("\r", "\n");
        resultposted = resultposted.replace("\'", "'");

        // Split into lines
        String[] templines = resultposted.split("\n");
        if (templines==null) {
            templines = new String[1];
            templines[0] = resultposted;
        }
        // Go through each line and look for chord lines
        // These have <span> in them
        int numlines = templines.length;
        newtext = "";
        for (int q = 0; q < numlines; q++) {
            if (templines[q].contains("<span>") || templines[q].contains("<span class=\"text-chord js-tab-ch\">") ||
                    templines[q].contains("<span class=\"text-chord js-tab-ch js-tapped\">")) {
                // Identify chord lines
                templines[q] = "." + templines[q];
            }
            if (FullscreenActivity.locale==null) {
                FullscreenActivity.locale = Locale.getDefault();
            }
            if (FullscreenActivity.locale!=null && templines[q]!=null && !templines[q].startsWith(".") &&
                    ((templines[q].toLowerCase(FullscreenActivity.locale).contains(getActivity().getResources().getString(R.string.tag_verse).toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12) ||
                            (templines[q].toLowerCase(FullscreenActivity.locale).contains(getActivity().getResources().getString(R.string.tag_chorus).toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12) ||
                            (templines[q].toLowerCase(FullscreenActivity.locale).contains(getActivity().getResources().getString(R.string.tag_bridge).toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12))) {
                // Looks like a tag
                templines[q] = "[" + templines[q].trim() + "]";
            }
            if (templines[q].indexOf("[") != 0 && templines[q].indexOf(".") != 0) {
                // Identify lyrics lines
                templines[q] = " " + templines[q];
            }
            newtext = newtext + templines[q] + "\n";
        }
        // Ok remove all html tags
        newtext = newtext.replace("<span>", "");
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
        newtext = newtext.replace("&", "&amp;");
        newtext = TextUtils.htmlEncode(newtext);


        if (filenametosave != null && !filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }

    }

    @SuppressWarnings("deprecation")
    public String extractSongSelectChordPro(String s, String temptitle) {

        int start;
        int end;
        String title = temptitle;
        String key = "";
        String author = "";
        String tempo = "";
        String timesig = "";
        String ccli = "";
        String copyright = "";
        String lyrics = "";

        // Extract the title
        start = s.indexOf("<span class=\"cproTitle\">");
        end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String t = s.substring(start+24,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                t = Html.fromHtml(t, 0).toString();
            } else {
                t = Html.fromHtml(t).toString();
            }
            title = "{title:" + t + "}\n";
            Log.d("SongSelect", "title="+title);
            filename = t;
        }

        // Extract the key
        start = s.indexOf("<code class=\"cproSongKey\"");
        end = s.indexOf("</code></span>",start);
        if (start>-1 && end>-1 && end>start) {
            // Fine tine the start
            int newstart = s.indexOf(">",start);
            if (newstart<0) {
                newstart = start;
            }
            key = "{key:" + s.substring(newstart+1,end).trim() + "}\n";
            Log.d("SongSelect", "key="+key);
        }

        // Extract the author
        start = s.indexOf("<span class=\"cproAuthors\">");
        end = s.indexOf("</span>",start);
        if (start>-1 && end>-1 && end>start) {
            String a = s.substring(start+26,end);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                a = Html.fromHtml(a, 0).toString();
            } else {
                a = Html.fromHtml(a).toString();
            }
            author = "{artist:" + a + "}\n";
            Log.d("SongSelect", "author="+author);
        }

        // Extract the tempo and time signature
        start = s.indexOf("<span class=\"cproTempoTimeWrapper\">");
        end = s.indexOf("</span>",start);
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
                tempo = "{tempo:" + t + "}\n";
                Log.d("SongSelect", "tempo=" + tempo);
            }
            if (bits.length>1) {
                String t = bits[1].replace("Time","");
                t = t.replace("-","");
                t = t.trim();
                timesig = "{time:" + t + "}\n";
                Log.d("SongSelect", "timesig="+timesig);
            }
        }

        // Extract the CCLI song number
        start = s.indexOf("CCLI Song #");
        end = s.indexOf("</p>",start);
        if (start>-1 && end>-1 && end>start) {
            ccli = "{ccli:" + s.substring(start+11,end).trim() + "}\n";
            Log.d("SongSelect", "ccli="+ccli);
        }

        // Extract the Copyright info
        start = s.indexOf("<ul class=\"copyright\">");
        start = s.indexOf("<li>",start);
        end = s.indexOf("</li>",start);
        if (start>-1 && end>-1 && end>start) {
            copyright = "{copyright:" + s.substring(start+4,end).trim() + "}\n";
            Log.d("SongSelect", "copyright="+copyright);
        }

        // Extract the lyrics
        start = s.indexOf("<pre class=\"cproSongBody\">");
        end = s.indexOf("</pre>",start);
        if (start>-1 && end>-1 && end>start) {
            lyrics = s.substring(start+26,end);

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
            while (lyrics.contains("<span class=\"chordWrapper\"><code ")) {
                start = lyrics.indexOf("<span class=\"chordWrapper\"><code ");
                int newstart = lyrics.indexOf(">",start);
                newstart = lyrics.indexOf(">",newstart+1);
                end = lyrics.indexOf("</code>",start);
                if (start>-1 && newstart>-1 && end>-1 && end>newstart) {
                    String chordfound = lyrics.substring(newstart+1,end);
                    String bittoremove = lyrics.substring(start,end+7);
                    lyrics = lyrics.replace(bittoremove,"["+chordfound+"]");
                }
            }

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

            // Finally, trim the lyrics
            lyrics = lyrics.trim();
        }

        // Return the ChordPro version of the song
        Log.d("d","lyrics="+lyrics);
        if (lyrics.equals("")) {
            return null;
        } else {
            return title + author + copyright + ccli + key + tempo + timesig + "\n" + lyrics;
        }
    }

    public String extractSongSelectUsr(String s, String temptitle) {
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
                Log.d("d","title="+title);
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
                    Log.d("d","line="+line);
                    // Is this the CCLI line?
                    if (line.contains("CCLI Song #")) {
                        line = line.replace("CCLI Song #","");
                        line = line.trim();
                        ccli = "{ccli:" + line + "}\n";
                        Log.d("d","ccli="+ccli);

                    // Is this the copyright line?
                    } else if (line.contains("opyright") || line.contains("&#169;") || line.contains("©")) {
                        copyright = "{copyright:" + line.trim() + "}\n";
                        Log.d("d","copyright="+copyright);

                    // Is this the author line?
                    } else if (!line.contains("For use solely") && !line.contains("Note:") && !line.contains("Licence No")) {
                        author = "{artist:" + line.trim() + "}\n";
                        Log.d("d","author="+author);
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

    public void setFileNameAndFolder() {

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

        // Set the file name if we know it
        if (filename == null || !filename.equals("")) {
            songfilename_EditText.setText(filename);
        } else {
            songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
        }

        if (filecontents==null) {
            songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
        }
    }

    public void doSaveSong() {
        if (!songfilename_EditText.getText().toString().equals("")) {
            filename = songfilename_EditText.getText().toString();
        }
        FileOutputStream newFile;
        String filenameandlocation;
        String tempfile;
        String temppdffile;
        temppdffile = filename.replace(".pdf","") + ".pdf";  // Gets rid of multiple .pdf extensions
        String pdffilenameandlocation;

        if (FullscreenActivity.whattodo.equals("chordie") || FullscreenActivity.whattodo.equals("songselect") ||
                FullscreenActivity.whattodo.equals("worshiptogether")) {
            tempfile = filename + ".chopro";
        } else {
            tempfile = filename;
            filecontents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>\n<title>" + filename
                    + "</title>\n<author>"
                    + authorname + "</author>\n<copyright></copyright>\n<lyrics>[]\n"
                    + PopUpEditSongFragment.parseToHTMLEntities(newtext) //Issues cause with &, so fix
                    + "</lyrics>\n</song>";
        }

        try {
            if (whatfolderselected.equals(FullscreenActivity.mainfoldername)) {
                filenameandlocation = FullscreenActivity.dir + "/"
                        + tempfile;
                pdffilenameandlocation = FullscreenActivity.dir + "/"
                        + temppdffile;
            } else {
                filenameandlocation = FullscreenActivity.dir + "/"
                        + whatfolderselected + "/" + tempfile;
                pdffilenameandlocation = FullscreenActivity.dir + "/"
                        + whatfolderselected + "/" + temppdffile;
                FullscreenActivity.whichSongFolder = whatfolderselected;
            }
            Log.d("d","filecontents check = "+filecontents);
            if (filecontents!=null && !filecontents.equals("")) {
                newFile = new FileOutputStream(filenameandlocation, false);
                newFile.write(filecontents.getBytes());
                newFile.flush();
                newFile.close();
                Log.d("d","tried to write chopro");
            }

            if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
                InputStream in;
                OutputStream out;
                try {
                    in = new FileInputStream(originaltemppdffile);
                    out = new FileOutputStream(pdffilenameandlocation);

                    File f = new File(originaltemppdffile);
                    if (f.exists()) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        in.close();

                        // write the output file
                        out.flush();
                        out.close();

                        // delete the original file
                        if(!f.delete()) {
                            Log.d("d","Problem deleting the original");
                        }
                    }
                }

                catch (Exception e) {
                    Log.e("tag", e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set the folder and song to the one we've set here
        FullscreenActivity.whichSongFolder = whatfolderselected;
        if (filecontents!=null) {
            FullscreenActivity.songfilename = tempfile;
        } else if (FullscreenActivity.whattodo.equals("songselect") && downloadcomplete) {
            FullscreenActivity.songfilename = temppdffile;
        }
        Preferences.savePreferences();
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
            response = "";
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
                        response += "\n" + s;
                        if (s.contains("<div class=\"fb-meta\">") ||
                                s.contains("<div class=\"plus-minus\">") ||
                                s.contains("<section class=\"ugm-ad ugm-ad__bottom\">")) {
                            // Force s to be null as we've got all we need!
                            break;
                        }
                    }
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
            //Now look to see if the webcontent has the ChordPro text in it
            // Check we aren't trying to use the tab-pro page!
            try {
                String address = webresults_WebView.getUrl();
                if (address != null && (address.contains("/tab-pro/") || address.contains("/chords-pro/"))) {
                    FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.not_allowed).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                } else if (result != null && result.contains("<textarea id=\"chordproContent\"")) {
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

                } else if (result!=null && result.contains("CCLI")) {
                    // Using SongSelect chord page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("<div id=\"LyricsText\"")) {
                    // Using SongSelect USR page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else {
                    FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                if (getActivity()!=null) {
                    FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
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
                ListSongFiles.getAllSongFolders();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String s) {
            try {
                // The song folder
                newtempfolders = new ArrayList<>();
                if (FullscreenActivity.mainfoldername != null) {
                    newtempfolders.add(FullscreenActivity.mainfoldername);
                }
                for (int e = 0; e < FullscreenActivity.mSongFolderNames.length; e++) {
                    if (FullscreenActivity.mSongFolderNames[e] != null &&
                            !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername)) {
                        newtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
                    }
                }
                ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, newtempfolders);
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

        String html = "";
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
            Log.d("d","filecontents="+filecontents);
            if (filecontents!=null && !filecontents.equals("")) {
                setFileNameAndFolder();
            } else {
                if (downloadcomplete) {
                    FullscreenActivity.myToastMessage = getActivity().getString(R.string.pdfonly);
                } else {
                    FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                }

                ShowToast.showToast(getActivity());
                grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }
}
