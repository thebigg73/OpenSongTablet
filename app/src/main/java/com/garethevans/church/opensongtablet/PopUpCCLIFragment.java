package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// This deals with all aspects of the CCLI stuff

public class PopUpCCLIFragment extends DialogFragment {

    static PopUpCCLIFragment newInstance() {
        PopUpCCLIFragment frag;
        frag = new PopUpCCLIFragment();
        return frag;
    }

    public interface MyInterface {
        void prepareOptionMenu();
    }

    public static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    /*@Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }*/

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    TextView title, churchNameTextView, licenceTextView, logTextView, resetText;
    EditText churchNameEditText,licenceEditText;
    WebView logWebView;
    FloatingActionButton saveMe;
    ArrayList<String> songfile;
    ArrayList<String> song;
    ArrayList<String> author;
    ArrayList<String> copyright;
    ArrayList<String> ccli;
    ArrayList<String> date;
    ArrayList<String> time;
    ArrayList<String> action;
    static File activitylogfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()==null) {
            dismiss();
        }

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View V = inflater.inflate(R.layout.popup_ccli, container, false);

        // Set the title based on the whattodo

        title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.edit_song_ccli));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                doSave();
            }
        });
        saveMe = V.findViewById(R.id.saveMe);

        // Set the log file
        activitylogfile = new File(FullscreenActivity.dirsettings,"ActivityLog.xml");

        // Initialise the views
        churchNameTextView = V.findViewById(R.id.churchNameTextView);
        churchNameEditText = V.findViewById(R.id.churchNameEditText);
        licenceTextView = V.findViewById(R.id.licenceTextView);
        licenceEditText = V.findViewById(R.id.licenceEditText);
        logTextView = V.findViewById(R.id.logTextView);
        logWebView = V.findViewById(R.id.logWebView);
        resetText = V.findViewById(R.id.resetText);
        logWebView.getSettings().setBuiltInZoomControls(true);
        logWebView.getSettings().setDisplayZoomControls(false);
        logWebView.clearCache(true);
        logWebView.setInitialScale(100);

        // Set up the views required
        switch (FullscreenActivity.whattodo) {
            case "ccli_church":
                setupChurchName();
                break;

            case "ccli_licence":
                setupLicence();
                break;

            case "ccli_view":
                setupLog();
                break;

            case "ccli_reset":
                setupReset();
                break;
        }
        Dialog dialog = getDialog();
        if (dialog!=null && getActivity()!=null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog);
        }
        return V;
    }

    public void setupChurchName() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.VISIBLE, View.GONE, View.GONE, View.GONE);

        // Set up the default values
        churchNameEditText.setText(FullscreenActivity.ccli_church);

        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.ccli_church = churchNameEditText.getText().toString();
                doSave();
            }
        });
    }

    public void setupLicence() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.VISIBLE, View.GONE, View.GONE);

        // Set up the default values
        licenceEditText.setText(FullscreenActivity.ccli_licence);

        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.ccli_licence = licenceEditText.getText().toString();
                doSave();
            }
        });
    }

    public void setupLog() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.GONE, View.VISIBLE, View.GONE);

        // Set up the default values
        buildTable();

        // Set up save/tick listener
        saveMe.setVisibility(View.GONE);
    }

    public void setupReset() {
        title.setText(getActivity().getResources().getString(R.string.areyousure));
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.GONE, View.GONE, View.VISIBLE);

        // Set up the default values
        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createBlankXML()) {
                    FullscreenActivity.myToastMessage = getString(R.string.ok);
                } else {
                    FullscreenActivity.myToastMessage = getString(R.string.error);
                }
                ShowToast.showToast(getActivity());
                try {
                    dismiss();
                } catch (Exception e) {
                    Log.d("d", "Error closing the fragment");
                }
            }
        });
    }

    public void setviewvisiblities(int church, int licence, int log, int reset) {
        churchNameTextView.setVisibility(church);
        churchNameEditText.setVisibility(church);
        licenceTextView.setVisibility(licence);
        licenceEditText.setVisibility(licence);
        logTextView.setVisibility(log);
        logWebView.setVisibility(log);
        resetText.setVisibility(reset);
    }

    public void buildTable() {
        // Info is stored in ActivityLog.xml file inside Settings folder

        // <Entry1>
        // <Date>2016-11-28</Date>
        // <Time>20:29:02</Time>
        // <Description>1</Description>
        // <FileName>( Main )/Early on one Christmas morn</FileName>
        // <title>Early on one Christmas morn</title>
        // <author/>
        // <ccli/>
        // <HasChords>false</HasChords></Entry1>

        // Run this as an async task
        BuildTable do_buildTable = new BuildTable();
        do_buildTable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static String getLogFileSize(File logfile) {

        int file_size_kb = Integer.parseInt(String.valueOf(logfile.length()/1024));
        String returntext = logfile.getName() + " ("+file_size_kb + "kb)";
        if (file_size_kb > 1024) {
            returntext = " <font color='#f00'>" + logfile.getName() + " ("+file_size_kb + "kb)" + "</font>";
        }
        return returntext;
    }

    @SuppressLint("StaticFieldLeak")
    private class BuildTable extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            try {
                // If the xml file doesn't exist, create a blank one
                boolean success = true;
                // Get the size of the file

                if (!activitylogfile.exists()) {
                    success = createBlankXML();
                }
                if (success) {
                    try {
                        // We can proceed!
                        XmlPullParserFactory factory;
                        factory = XmlPullParserFactory.newInstance();

                        factory.setNamespaceAware(true);
                        XmlPullParser xpp;
                        xpp = factory.newPullParser();

                        songfile = new ArrayList<>();
                        song = new ArrayList<>();
                        author = new ArrayList<>();
                        copyright = new ArrayList<>();
                        ccli = new ArrayList<>();
                        date = new ArrayList<>();
                        time = new ArrayList<>();
                        action = new ArrayList<>();

                        InputStream inputStream = new FileInputStream(activitylogfile);
                        xpp.setInput(inputStream, "UTF-8");

                        int eventType;
                        String curr_file = "";
                        String curr_song = "";
                        String curr_author = "";
                        String curr_copy = "";
                        String curr_ccli = "";
                        String curr_date = "";
                        String curr_time = "";
                        String curr_action = "";

                        eventType = xpp.getEventType();
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xpp.getName().startsWith("Entry")) {
                                    // If the song isn't blank (first time), extract them
                                    if (!curr_song.equals("")) {
                                        songfile.add(curr_file);
                                        song.add(curr_song);
                                        author.add(curr_author);
                                        copyright.add(curr_copy);
                                        ccli.add(curr_ccli);
                                        date.add(curr_date);
                                        time.add(curr_time);
                                        action.add(curr_action);

                                        // Reset the tags
                                        curr_file = "";
                                        curr_song = "";
                                        curr_author = "";
                                        curr_copy = "";
                                        curr_ccli = "";
                                        curr_date = "";
                                        curr_time = "";
                                        curr_action = "";
                                    }
                                } else if (xpp.getName().equals("FileName")) {
                                    curr_file = xpp.nextText();
                                } else if (xpp.getName().equals("title")) {
                                    curr_song = xpp.nextText();
                                } else if (xpp.getName().equals("author")) {
                                    curr_author = xpp.nextText();
                                } else if (xpp.getName().equals("copyright")) {
                                    curr_copy = xpp.nextText();
                                } else if (xpp.getName().equals("ccli")) {
                                    curr_ccli = xpp.nextText();
                                } else if (xpp.getName().equals("Date")) {
                                    curr_date = xpp.nextText();
                                } else if (xpp.getName().equals("Time")) {
                                    curr_time = xpp.nextText();
                                } else if (xpp.getName().equals("Description")) {
                                    curr_action = xpp.nextText();
                                }
                            }
                            try {
                                eventType = xpp.next();
                            } catch (Exception e) {
                                //Ooops!
                                e.printStackTrace();
                            }
                        }

                        // Add the last item
                        if (!curr_song.equals("")) {
                            songfile.add(curr_file);
                            song.add(curr_song);
                            author.add(curr_author);
                            copyright.add(curr_copy);
                            ccli.add(curr_ccli);
                            date.add(curr_date);
                            time.add(curr_time);
                            action.add(curr_action);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                String table;
                if (song == null || song.size() == 0) {
                    table = "<html><body><h2>" + getString(R.string.edit_song_ccli) + "</h2>\n" +
                            "<h3>" + getString(R.string.ccli_church) + ": " + FullscreenActivity.ccli_church + "</h3>\n" +
                            "<h3>" + getString(R.string.ccli_licence) + ": " + FullscreenActivity.ccli_licence + "</h3>\n" +
                            "<h4>" + getLogFileSize(activitylogfile) + "</h4>\n" +
                            "</body></html>";

                } else {
                    table = "<html><head>\n" +
                            "<style>\n#mytable {\nborder-collapse: collapse; width: 100%;\n}\n" +
                            "#mytable td, #mytable th {\nborder: 1px solid #ddd; padding: 2px;\n}\n" +
                            "#mytable tr:nth-child(even) {\nbackground-color: #f2f2f2;\n}\n" +
                            "#mytable th {\npadding-top: 2px; padding-bottom: 2px; text-align: left; " +
                            "background-color: #4CAF50; color: white;\n}\n" +
                            "</style>\n</head><body>" +
                            "<h2>" + getString(R.string.edit_song_ccli) + "</h2>\n" +
                            "<h3>" + getString(R.string.ccli_church) + ": " + FullscreenActivity.ccli_church + "</h3>\n" +
                            "<h3>" + getString(R.string.ccli_licence) + ": " + FullscreenActivity.ccli_licence + "</h3>\n" +
                            "<h4>" + getLogFileSize(activitylogfile) + "</h4>\n" +
                            "<body><table id=\"mytable\">\n<tr>";
                    table += "<th>" + getString(R.string.item) + "</th>";
                    table += "<th>" + getString(R.string.edit_song_title) + "</th>";
                    table += "<th>" + getString(R.string.edit_song_author) + "</th>";
                    table += "<th>" + getString(R.string.edit_song_copyright) + "</th>";
                    table += "<th>" + getString(R.string.edit_song_ccli) + "</th>";
                    table += "<th>" + getString(R.string.date) + "</th>";
                    table += "<th>" + getString(R.string.time) + "</th>";
                    table += "<th>" + getString(R.string.action) + "</th>";
                    table += "</tr>\n";
                    // Build the table view
                    for (int x = 0; x < song.size(); x++) {
                        table += "<tr>";
                        table += "<td>" + songfile.get(x) + "</td>";
                        table += "<td>" + song.get(x) + "</td>";
                        table += "<td>" + author.get(x) + "</td>";
                        table += "<td>" + copyright.get(x) + "</td>";
                        table += "<td>" + ccli.get(x) + "</td>";
                        table += "<td>" + date.get(x) + "</td>";
                        table += "<td>" + time.get(x) + "</td>";
                        switch (action.get(x)) {
                        /*
                        1 Created - when importing or clicking on the new
                        2 Deleted - when clicking on the delete options
                        3 Edited - when saving an edit (only from the edit page though)
                        4 Moved - when renaming a file and the folder changes
                        5 Presented - when dual screen work is called
                        6 Printed - This is the akward one - when a song is added to a set
                        7 Renamed - when rename is called
                        8 Copied - when duplicate is called
                        */
                            default:
                                table += "<td>" + getString(R.string.options_other) + "</td>";
                                break;
                            case "1":
                                table += "<td>" + getString(R.string.options_song_new) + "</td>";
                                break;
                            case "2":
                                table += "<td>" + getString(R.string.options_set_delete) + "</td>";
                                break;
                            case "3":
                                table += "<td>" + getString(R.string.options_set_edit) + "</td>";
                                break;
                            case "4":
                            case "7":
                                table += "<td>" + getString(R.string.options_song_rename) + "</td>";
                                break;
                            case "5":
                                table += "<td>" + getString(R.string.sendtoprojector) + "</td>";
                                break;
                            case "6":
                                table += "<td>" + getString(R.string.songsheet) + "</td>";
                                break;
                        }
                        table += "<tr>\n";
                    }
                    table += "</table></body></html>";
                }
                return table;
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                logWebView.loadData(s, "text/html", "UTF-8");
            } catch (Exception e) {
                Log.d("d","Error updating the WebView");
            }
        }
    }

    public void doSave() {
        Preferences.savePreferences();
        if (mListener!=null) {
            mListener.prepareOptionMenu();
        }
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing fragment");
        }
    }

    public static boolean createBlankXML() {
        boolean success = false;
        String blankXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "  <log></log>\n";
        try {
            FileOutputStream overWrite = new FileOutputStream(activitylogfile, false);
            overWrite.write(blankXML.getBytes());
            overWrite.flush();
            overWrite.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing the fragment");
        }
    }

    public static void addUsageEntryToLog(String fname, String song,
                                          String author, String copyright, String ccli, String usage) {
        // Do this as an async task
        AddUsageEntryToLog add_usage = new AddUsageEntryToLog(fname, song, author, copyright, ccli, usage);
        add_usage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private static class AddUsageEntryToLog extends AsyncTask<Object, Void, String> {

        String date;
        String time;
        String fname = "";
        String song = "";
        String author = "";
        String copyright = "";
        String ccli = "";
        String usage = "";

        AddUsageEntryToLog (String f, String s, String a, String c, String l, String u){
            if (f!=null) {
                fname = f;
            }if (s!=null) {
                song = s;
            }
            if (a!=null) {
                author = a;
            }
            if (c!=null) {
                copyright = c;
            }
            if (l!=null) {
                ccli = l;
            }
            if (u!=null) {
                usage = u;
            }
        }

        @Override
        protected void onPreExecute() {
            // Get today's date
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            @SuppressLint("SimpleDateFormat") DateFormat tf = new SimpleDateFormat("HH:mm:ss");
            Date d = new Date();
            date = df.format(d);
            time = tf.format(d);
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            activitylogfile = new File(FullscreenActivity.dirsettings, "ActivityLog.xml");
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                if (!activitylogfile.exists()) {
                    createBlankXML();
                }
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(activitylogfile);
                Element root = document.getDocumentElement();

                String last = "Entry0";
                try {
                    if (root!=null && root.getLastChild()!=null && root.getLastChild().getNodeName()!=null) {
                        last = root.getLastChild().getNodeName();
                        // Repeat this a max of 5 times, or until we find the EntryX tag
                        boolean found = false;
                        int attempts = 0;
                        Node n = root.getLastChild();
                        while (!found) {
                            n = n.getPreviousSibling();
                            last = n.getNodeName();
                            if (last.contains("Entry")) {
                                found = true;
                            } else {
                                attempts ++;
                            }
                            if (attempts>6) {
                                last = "";
                                found = true;
                            }
                        }
                    }
                    last = last.replace("Entry", "");
                } catch (Exception e) {
                    e.printStackTrace();
                        e.printStackTrace();
                        last = "0";
                }

                // Try to get the last entry number
                int i;
                try {
                    i = Integer.parseInt(last.replaceAll("[\\D]", "")) + 1;
                } catch (Exception e) {
                    Log.d("d","No integer found, so will use 1");
                    i = 1;
                }

                Element newItem = document.createElement("Entry"+i);

                Element a_date = document.createElement("Date");
                a_date.appendChild(document.createTextNode(date));
                newItem.appendChild(a_date);

                Element a_time = document.createElement("Time");
                a_time.appendChild(document.createTextNode(time));
                newItem.appendChild(a_time);

                Element a_usage = document.createElement("Description");
                a_usage.appendChild(document.createTextNode(usage));
                newItem.appendChild(a_usage);

                Element a_fname = document.createElement("FileName");
                a_fname.appendChild(document.createTextNode(fname));
                newItem.appendChild(a_fname);

                Element a_song = document.createElement("title");
                a_song.appendChild(document.createTextNode(song));
                newItem.appendChild(a_song);

                Element a_author = document.createElement("author");
                a_author.appendChild(document.createTextNode(author));
                newItem.appendChild(a_author);

                Element a_copyright = document.createElement("copyright");
                a_copyright.appendChild(document.createTextNode(copyright));
                newItem.appendChild(a_copyright);

                Element a_ccli = document.createElement("ccli");
                a_ccli.appendChild(document.createTextNode(ccli));
                newItem.appendChild(a_ccli);

                root.appendChild(newItem);

                DOMSource source = new DOMSource(document);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                StreamResult result = new StreamResult(activitylogfile);
                transformer.transform(source, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}