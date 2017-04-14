package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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
    String folder;
    String filecontents;
    AsyncTask<Object, Void, String> getfolders;
    ArrayList<String> newtempfolders;
    String whatfolderselected;
    String mTitle = "";
    private MyInterface mListener;

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
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.popup_dialogtitle);
            TextView title = (TextView) getDialog().getWindow().findViewById(R.id.dialogtitle);
            title.setText(mTitle);
            FloatingActionButton closeMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.closeMe);
            closeMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        dismiss();
                    } catch (Exception e) {
                        // Error cancelling
                    }
                }
            });
            FloatingActionButton saveMe = (FloatingActionButton) getDialog().getWindow().findViewById(R.id.saveMe);
            saveMe.setVisibility(View.GONE);
        } else {
            getDialog().setTitle(mTitle);
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
        if (FullscreenActivity.whattodo.equals("chordie")) {
            mTitle = getActivity().getResources().getString(R.string.chordiesearch);
        } else {
            mTitle = getActivity().getResources().getString(R.string.ultimateguitarsearch);
        }
        getDialog().requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_findnewsongs, container, false);

        // Initialise the views
        searchtext_LinearLayout = (LinearLayout) V.findViewById(R.id.searchtext_LinearLayout);
        searchphrase_EditText = (EditText) V.findViewById(R.id.searchphrase_EditText);
        searchresults_RelativeLayout = (RelativeLayout) V.findViewById(R.id.searchresults_RelativeLayout);
        webresults_WebView = (WebView) V.findViewById(R.id.webresults_WebView);
        webBack_ImageButton = (ImageButton) V.findViewById(R.id.webBack_ImageButton);
        grabSongData_Button = (Button) V.findViewById(R.id.grabSongData_Button);
        grabSongData_ProgressBar = (ProgressBar) V.findViewById(R.id.grabSongData_ProgressBar);
        doSearch_Button = (Button) V.findViewById(R.id.doSearch_Button);
        newfileinfo_LinearLayout = (LinearLayout) V.findViewById(R.id.newfileinfo_LinearLayout);
        songfilename_EditText = (EditText) V.findViewById(R.id.songfilename_EditText);
        choosefolder_Spinner = (Spinner) V.findViewById(R.id.choosefolder_Spinner);
        saveSong_Button = (Button) V.findViewById(R.id.saveSong_Button);

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

    @SuppressLint({"SetJavaScriptEnabled", "deprecation"})
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
            case "worshipready":
                weblink = "http://www.worshipready.com/chord-charts";
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

    public void fixUGContent(String resultposted) {
        // From ultimate guitar

        grabSongData_ProgressBar.setVisibility(View.INVISIBLE);

        // Try to find the title
        // By default use the title of the page as a default

        Log.d("d", "resultposted=" + resultposted);
        String title_resultposted;
        String filenametosave = "UG Song";
        String authorname = "";
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
                    authorname = authorname.trim();
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
            authorname = author_resultposted;
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
        // Go through each line and look for chord lines
        // These have <span> in them
        int numlines = templines.length;
        String newtext = "";
        for (int q = 0; q < numlines; q++) {
            if (templines[q].contains("<span>") || templines[q].contains("<span class=\"text-chord js-tab-ch\">") ||
                    templines[q].contains("<span class=\"text-chord js-tab-ch js-tapped\">")) {
                // Identify chord lines
                templines[q] = "." + templines[q];
            }
            if (!templines[q].startsWith(".") &&
                    ((templines[q].toLowerCase(FullscreenActivity.locale).contains(FullscreenActivity.tag_verse.toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12) ||
                            (templines[q].toLowerCase(FullscreenActivity.locale).contains(FullscreenActivity.tag_chorus.toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12) ||
                            (templines[q].toLowerCase(FullscreenActivity.locale).contains(FullscreenActivity.tag_bridge.toLowerCase(FullscreenActivity.locale)) && templines[q].length() < 12))) {
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


        filecontents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<song>\n<title>" + filenametosave
                + "</title>\n<author>"
                + authorname + "</author>\n<copyright></copyright>\n<lyrics>[]\n"
                + newtext
                + "</lyrics>\n</song>";
        if (filenametosave != null && !filenametosave.equals("")) {
            filename = filenametosave.trim();
        } else {
            filename = FullscreenActivity.phrasetosearchfor;
        }

        Log.d("d", "filecontents=" + filecontents);

    }

    public void setFileNameAndFolder() {

        // Hide the searchresults_RelativeLayout
        searchresults_RelativeLayout.setVisibility(View.GONE);
        newfileinfo_LinearLayout.setVisibility(View.VISIBLE);

        // Set the file name if we know it
        if (filename == null || !filename.equals("")) {
            songfilename_EditText.setText(filename);
        } else {
            songfilename_EditText.setText(FullscreenActivity.phrasetosearchfor);
        }
    }

    public void doSaveSong() {
        FileOutputStream newFile;
        String filenameandlocation;
        String tempfile;

        if (FullscreenActivity.whattodo.equals("chordie")) {
            tempfile = filename + ".chopro";
        } else {
            tempfile = filename;
        }

        try {
            if (whatfolderselected.equals(FullscreenActivity.mainfoldername)) {
                filenameandlocation = FullscreenActivity.dir + "/"
                        + tempfile;
            } else {
                filenameandlocation = FullscreenActivity.dir + "/"
                        + whatfolderselected + "/" + tempfile;
                FullscreenActivity.whichSongFolder = whatfolderselected;
            }
            newFile = new FileOutputStream(filenameandlocation, false);
            newFile.write(filecontents.getBytes());
            newFile.flush();
            newFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FullscreenActivity.songfilename = tempfile;
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

    private class DownloadWebTextTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

        }

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
                } catch (Exception e) {
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
            String address = webresults_WebView.getUrl();
            if (address.contains("/tab-pro/") || address.contains("/chords-pro/")) {
                FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.not_allowed).toString();
                ShowToast.showToast(getActivity());
                grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
            } else if (result.contains("<textarea id=\"chordproContent\"")) {
                // Using Chordie
                fixChordieContent(result);
                setFileNameAndFolder();

            } else if (result.contains("<div class=\"tb_ct\">") || result.contains("ultimate-guitar")) {
                // Using UG
                fixUGContent(result);
                setFileNameAndFolder();

            } else {
                FullscreenActivity.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                ShowToast.showToast(getActivity());
                grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class GetFolders extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... objects) {
            ListSongFiles.getAllSongFolders();
            return null;
        }

        protected void onPostExecute(String s) {
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
        }
    }

}
