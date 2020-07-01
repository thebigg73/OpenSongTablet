package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
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
import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

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

    private static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    private TextView title, churchNameTextView, licenceTextView, logTextView, resetText;
    private EditText churchNameEditText,licenceEditText;
    private WebView logWebView;
    private FloatingActionButton saveMe;
    private ArrayList<String> songfile;
    private ArrayList<String> song;
    private ArrayList<String> author;
    private ArrayList<String> copyright;
    private ArrayList<String> ccli;
    private ArrayList<String> date;
    private ArrayList<String> time;
    private ArrayList<String> action;
    private Preferences preferences;

    static boolean createBlankXML(Context c, Preferences preferences) {
        String blankXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<log></log>\n";
        StorageAccess storageAccess = new StorageAccess();
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Settings", "", "ActivityLog.xml");

        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        return storageAccess.writeFileFromString(blankXML, outputStream);
    }

    private void setupChurchName() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.VISIBLE, View.GONE, View.GONE, View.GONE);

        // Set up the default values
        churchNameEditText.setText(preferences.getMyPreferenceString(getActivity(),"ccliChurchName",""));

        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"ccliChurchName",churchNameEditText.getText().toString());
                doSave();
            }
        });
    }

    private void setupLicence() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.VISIBLE, View.GONE, View.GONE);

        // Set up the default values
        licenceEditText.setText(preferences.getMyPreferenceString(getActivity(),"ccliLicence",""));

        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences.setMyPreferenceString(getActivity(),"ccliLicence",licenceEditText.getText().toString());
                doSave();
            }
        });
    }

    private void setupLog() {
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.GONE, View.VISIBLE, View.GONE);

        // Set up the default values
        buildTable(getActivity());

        // Set up save/tick listener
        saveMe.hide();
    }

    public static void addUsageEntryToLog(Context c, Preferences preferences, String fname, String song,
                                          String author, String copyright, String ccli, String usage) {
        // Do this as an async task
        StorageAccess storageAccess = new StorageAccess();
        // Check if the log exists or if we need to create it
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");
        if (!storageAccess.uriExists(c, uri)) {
            Log.d("d", "Creating blankXML=" + createBlankXML(c, preferences));
        } else {
            Log.d("d", uri + " exists");
        }
        AddUsageEntryToLog add_usage = new AddUsageEntryToLog(c, storageAccess, uri, fname, song, author, copyright, ccli, usage);
        add_usage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setviewvisiblities(int church, int licence, int log, int reset) {
        churchNameTextView.setVisibility(church);
        churchNameEditText.setVisibility(church);
        licenceTextView.setVisibility(licence);
        licenceEditText.setVisibility(licence);
        logTextView.setVisibility(log);
        logWebView.setVisibility(log);
        resetText.setVisibility(reset);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.edit_song_ccli));
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
        //static File activitylogfile;
        preferences = new Preferences();

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
        switch (StaticVariables.whattodo) {
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
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),dialog, preferences);
        }
        return V;
    }
    private static String getLogFileSize(Context c, Uri uri) {
        StorageAccess storageAccess = new StorageAccess();
        float file_size_kb = storageAccess.getFileSizeFromUri(c, uri);
        String returntext = "ActivityLog.xml ("+file_size_kb + "kb)";
        if (file_size_kb > 1024) {
            returntext = " <font color='#f00'>ActivityLog.xml ("+file_size_kb + "kb)" + "</font>";
        }
        return returntext;
    }

    private void buildTable(Context c) {
        StorageAccess storageAccess = new StorageAccess();
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

        // Check if the Log file exists and if not, create it
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");
        storageAccess.lollipopCreateFileForOutputStream(getActivity(), preferences, uri, null,
                "Settings", "", "ActivityLog.xml");
        InputStream inputStream = storageAccess.getInputStream(c, uri);
        String actfilesize = getLogFileSize(c, uri);
        BuildTable do_buildTable = new BuildTable(inputStream, actfilesize);
        do_buildTable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void doSave() {
        if (mListener!=null) {
            mListener.prepareOptionMenu();
        }
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing fragment");
        }
    }

    private void setupReset() {
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.areyousure));
        // Show what we want and hide what we don't
        setviewvisiblities(View.GONE, View.GONE, View.GONE, View.VISIBLE);

        // Set up the default values
        // Set up save/tick listener
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createBlankXML(getActivity(), preferences)) {
                    StaticVariables.myToastMessage = getString(R.string.ok);
                } else {
                    StaticVariables.myToastMessage = getString(R.string.error);
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

    @Override
    public void onCancel(DialogInterface dialog) {
        try {
            this.dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing the fragment");
        }
    }

    private static class AddUsageEntryToLog extends AsyncTask<Object, Void, String> {
        @SuppressLint("StaticFieldLeak")
        final
        Context ctx;
        final Uri uri;
        //InputStream inputStream;
        //OutputStream outputStream;
        final StorageAccess storageAccess;
        String date;
        String time;
        String fname = "";
        String song = "";
        String author = "";
        String copyright = "";
        String ccli = "";
        String usage = "";

        AddUsageEntryToLog(Context context, StorageAccess sA, Uri fileuri, String f, String s, String a, String c, String l, String u) {
            uri = fileuri;
            storageAccess = sA;
            ctx = context;
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
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                InputStream inputStream = storageAccess.getInputStream(ctx, uri);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                String myString = storageAccess.readTextFileToString(inputStream);

                Document document = null;
                Element root = null;

                String last = "Entry0";
                try {
                    document = documentBuilder.parse(new InputSource(new StringReader(myString)));
                    root = document.getDocumentElement();

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

                if (document != null) {
                    Element newItem = document.createElement("Entry" + i);

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

                    if (root != null) {
                        root.appendChild(newItem);
                    }

                    DOMSource source = new DOMSource(document);

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    OutputStream outputStream = storageAccess.getOutputStream(ctx, uri);
                    StreamResult result = new StreamResult(outputStream);
                    transformer.transform(source, result);
                } else {
                    Log.d("CCLI", "doument was null");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class BuildTable extends AsyncTask<Object, Void, String> {
        //final Uri uri;
        final InputStream inputStream;
        //OutputStream outputStream;
        final String sizeoffile;

        BuildTable(InputStream is, String filesize) {
            sizeoffile = filesize;
            inputStream = is;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                // We can proceed!
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();

                initialiseTable();

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
                return buildMyTable(sizeoffile);
            } catch (Exception e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                logWebView.loadData(s, "text/html", "UTF-8");
            } catch (Exception e) {
                Log.d("d", "Error updating the WebView");
            }
        }
    }

    private void initialiseTable() {
        songfile = new ArrayList<>();
        song = new ArrayList<>();
        author = new ArrayList<>();
        copyright = new ArrayList<>();
        ccli = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        action = new ArrayList<>();
    }

    private String buildMyTable(String sizeoffile) {
        StringBuilder table;
        if (song == null || song.size() == 0) {
            table = new StringBuilder("<html><body><h2>" + getString(R.string.edit_song_ccli) + "</h2>\n" +
                    "<h3>" + getString(R.string.ccli_church) + ": " +
                    preferences.getMyPreferenceString(getActivity(),"ccliChurchName","") + "</h3>\n" +
                    "<h3>" + getString(R.string.ccli_licence) + ": " +
                    preferences.getMyPreferenceString(getActivity(),"ccliLicence","")+ "</h3>\n" +
                    "<h4>" + sizeoffile + "</h4>\n" +
                    "</body></html>");

        } else {
            table = new StringBuilder("<html><head>\n" +
                    "<style>\n#mytable {\nborder-collapse: collapse; width: 100%;\n}\n" +
                    "#mytable td, #mytable th {\nborder: 1px solid #ddd; padding: 2px;\n}\n" +
                    "#mytable tr:nth-child(even) {\nbackground-color: #f2f2f2;\n}\n" +
                    "#mytable th {\npadding-top: 2px; padding-bottom: 2px; text-align: left; " +
                    "background-color: #4CAF50; color: white;\n}\n" +
                    "</style>\n</head><body>" +
                    "<h2>" + getString(R.string.edit_song_ccli) + "</h2>\n" +
                    "<h3>" + getString(R.string.ccli_church) + ": " +
                    preferences.getMyPreferenceString(getActivity(),"ccliChurchName","") + "</h3>\n" +
                    "<h3>" + getString(R.string.ccli_licence) + ": " +
                    preferences.getMyPreferenceString(getActivity(),"ccliLicence","")+ "</h3>\n" +
                    "<h4>" + sizeoffile + "</h4>\n" +
                    "<body><table id=\"mytable\">\n<tr>");
            table.append("<th>").append(getString(R.string.item)).append("</th>");
            table.append("<th>").append(getString(R.string.edit_song_title)).append("</th>");
            table.append("<th>").append(getString(R.string.edit_song_author)).append("</th>");
            table.append("<th>").append(getString(R.string.edit_song_copyright)).append("</th>");
            table.append("<th>").append(getString(R.string.edit_song_ccli)).append("</th>");
            table.append("<th>").append(getString(R.string.date)).append("</th>");
            table.append("<th>").append(getString(R.string.time)).append("</th>");
            table.append("<th>").append(getString(R.string.action)).append("</th>");
            table.append("</tr>\n");
            // Build the table view
            for (int x = 0; x < song.size(); x++) {
                table.append("<tr>");
                table.append("<td>").append(songfile.get(x)).append("</td>");
                table.append("<td>").append(song.get(x)).append("</td>");
                table.append("<td>").append(author.get(x)).append("</td>");
                table.append("<td>").append(copyright.get(x)).append("</td>");
                table.append("<td>").append(ccli.get(x)).append("</td>");
                table.append("<td>").append(date.get(x)).append("</td>");
                table.append("<td>").append(time.get(x)).append("</td>");
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
                        table.append("<td>").append(getString(R.string.other)).append("</td>");
                        break;
                    case "1":
                        table.append("<td>").append(getString(R.string.new_something)).append("</td>");
                        break;
                    case "2":
                        table.append("<td>").append(getString(R.string.delete)).append("</td>");
                        break;
                    case "3":
                        table.append("<td>").append(getString(R.string.edit)).append("</td>");
                        break;
                    case "4":
                    case "7":
                        table.append("<td>").append(getString(R.string.rename)).append("</td>");
                        break;
                    case "5":
                        table.append("<td>").append(getString(R.string.sendtoprojector)).append("</td>");
                        break;
                    case "6":
                        table.append("<td>").append(getString(R.string.songsheet)).append("</td>");
                        break;
                }
                table.append("<tr>\n");
            }
            table.append("</table></body></html>");
        }
        return table.toString();
    }
}