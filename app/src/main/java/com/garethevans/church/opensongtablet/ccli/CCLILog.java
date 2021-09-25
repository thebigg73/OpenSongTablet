package com.garethevans.church.opensongtablet.ccli;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class CCLILog {

    public CCLILog() {}

    /*
    1 Created - when importing or clicking on the new
    2 Deleted - when clicking on the delete options
    3 Edited - when saving an edit (only from the edit page though)
    4 Moved - when renaming a file and the folder changes
    5 Presented - when dual screen work is called
    6 Printed - This is the akward one - when a song is added to a set
    7 Renamed - when rename is called
    8 Copied - when duplicate is called

    Info is stored in ActivityLog.xml file inside Settings folder
     <Entry1>
     <Date>2016-11-28</Date>
     <Time>20:29:02</Time>
     <Description>1</Description>
     <FileName>( Main )/Early on one Christmas morn</FileName>
     <title>Early on one Christmas morn</title>
     <author/>
     <ccli/>
     <HasChords>false</HasChords></Entry1>
    */

    private String thisdate;
    private String thistime;

    private ArrayList<String> songfile, title, author, copyright, ccli, date, time, action;

    public void addEntry(Context c, MainActivityInterface mainActivityInterface, Song thisSong, String usageType) {

        // Check if the log exists or if we need to create it
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Settings", "", "ActivityLog.xml");
        if (!mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
            Log.d("d", "Creating blankXML=" + createBlankXML(c, mainActivityInterface, uri));
        } else {
            Log.d("d", uri + " exists");
        }

        // Set the date and time
        setTheDateAndTime();

        doTheSaving(c, mainActivityInterface, thisSong, uri, usageType);
    }

    public boolean createBlankXML(Context c, MainActivityInterface mainActivityInterface, Uri uri) {
        String blankXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<log>\n</log>\n";

        // Delete the old file
        mainActivityInterface.getStorageAccess().deleteFile(c,uri);

        // Write the new file
        return mainActivityInterface.getStorageAccess().doStringWriteToFile(c,mainActivityInterface,"Settings","","ActivityLog.xml",blankXML);
    }

    @SuppressLint("SimpleDateFormat")
    private void setTheDateAndTime() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat tf = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date();
        thisdate = df.format(d);
        thistime = tf.format(d);
        thisdate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void doTheSaving(Context c, MainActivityInterface mainActivityInterface, Song thisSong, Uri uri, String usageType) {
        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            String myString = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);

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
                a_date.appendChild(document.createTextNode(thisdate));
                newItem.appendChild(a_date);

                Element a_time = document.createElement("Time");
                a_time.appendChild(document.createTextNode(thistime));
                newItem.appendChild(a_time);

                Element a_usage = document.createElement("Description");
                a_usage.appendChild(document.createTextNode(usageType));
                newItem.appendChild(a_usage);

                Element a_fname = document.createElement("FileName");
                a_fname.appendChild(document.createTextNode(thisSong.getFilename()));
                newItem.appendChild(a_fname);

                Element a_title = document.createElement("title");
                a_title.appendChild(document.createTextNode(thisSong.getTitle()));
                newItem.appendChild(a_title);

                Element a_author = document.createElement("author");
                a_author.appendChild(document.createTextNode(thisSong.getAuthor()));
                newItem.appendChild(a_author);

                Element a_copyright = document.createElement("copyright");
                a_copyright.appendChild(document.createTextNode(thisSong.getCopyright()));
                newItem.appendChild(a_copyright);

                Element a_ccli = document.createElement("ccli");
                a_ccli.appendChild(document.createTextNode(thisSong.getCcli()));
                newItem.appendChild(a_ccli);

                if (root != null) {
                    root.appendChild(newItem);
                }

                DOMSource source = new DOMSource(document);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, uri);
                StreamResult result = new StreamResult(outputStream);
                transformer.transform(source, result);
            } else {
                Log.d("CCLI", "document was null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLogFileSize(Context c, MainActivityInterface mainActivityInterface, Uri uri, TextView logFileSize) {
        // Set the uri if it isn't already done
        float file_size_kb = mainActivityInterface.getStorageAccess().getFileSizeFromUri(c, uri);
        file_size_kb = Math.round(file_size_kb * 100);
        file_size_kb = file_size_kb / 100.0f;
        String returntext = "ActivityLog.xml ("+ file_size_kb + "kb)";
        logFileSize.setText(returntext);
        if (file_size_kb > 1024) {
            logFileSize.setTextColor(0xffff0000);
        }
    }

    private void initialiseTables() {
        songfile = new ArrayList<>();
        title = new ArrayList<>();
        author = new ArrayList<>();
        copyright = new ArrayList<>();
        ccli = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        action = new ArrayList<>();
    }

    public void getCurrentEntries(Context c, MainActivityInterface mainActivityInterface, Uri uri) {

        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();

            initialiseTables();
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);

            xpp.setInput(inputStream, "UTF-8");

            int eventType;
            String curr_file = "";
            String curr_title = "";
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
                        if (!curr_title.equals("")) {
                            songfile.add(curr_file);
                            title.add(curr_title);
                            author.add(curr_author);
                            copyright.add(curr_copy);
                            ccli.add(curr_ccli);
                            date.add(curr_date);
                            time.add(curr_time);
                            action.add(curr_action);

                            // Reset the tags
                            curr_file = "";
                            curr_title = "";
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
                        curr_title = xpp.nextText();
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
            if (!curr_title.equals("")) {
                songfile.add(curr_file);
                title.add(curr_title);
                author.add(curr_author);
                copyright.add(curr_copy);
                ccli.add(curr_ccli);
                date.add(curr_date);
                time.add(curr_time);
                action.add(curr_action);
            }
        } catch (Exception e) {
            initialiseTables();
        }
    }

    public TableLayout getTableLayout(Context c) {
        TableLayout tableLayout = new TableLayout(c);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
        tableLayout.setLayoutParams(layoutParams);

        // If there are no entries, sort that, otherwise add the correct data
        if (songfile.size() != 0) {
            // Add the headers
            String[] headers = new String[]{c.getString(R.string.item), c.getString(R.string.title),
                    c.getString(R.string.author), c.getString(R.string.copyright),
                    c.getString(R.string.ccli), c.getString(R.string.date),
                    c.getString(R.string.time), c.getString(R.string.action)};
            tableLayout.addView(getRow(c, headers, true));

            // Add the rows
            for (int x = 0; x < songfile.size(); x++) {
                String[] rowVals = new String[]{songfile.get(x), title.get(x), author.get(x), copyright.get(x),
                        ccli.get(x), date.get(x), time.get(x), getActionText(c, action.get(x))};
                tableLayout.addView(getRow(c, rowVals, false));
            }
        } else {
            // Empty
            tableLayout.addView(getRow(c,new String[]{c.getString(R.string.empty),"","","","","","",""},false));
        }
        return tableLayout;
    }

    public TableRow getRow(Context c, String[] vals, boolean isHeader) {
        TableRow tableRow = new TableRow(c);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(layoutParams);
        for (String val : vals) {
            TextView textView = new TextView(c);
            textView.setSingleLine(false);
            TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,1.0f);
            textView.setLayoutParams(layoutParams2);
            textView.setText(val);
            textView.setPadding(8,0,8,0);
            if (isHeader) {
                textView.setTextSize(16.0f);
            } else {
                textView.setTextSize(12.0f);
            }
            tableRow.addView(textView);
        }
        colorRowColor(c,tableRow);
        return tableRow;
    }

    private String getActionText(Context c, String action) {
        String actionText;
        switch (action) {
            default:
                actionText = c.getString(R.string.other);
                break;
            case "1":
                actionText = c.getString(R.string.new_something);
                break;
            case "2":
                actionText = c.getString(R.string.delete);
                break;
            case "3":
                actionText = c.getString(R.string.edit);
                break;
            case "4":
            case "7":
                actionText = c.getString(R.string.rename);
                break;
            case "5":
                actionText = c.getString(R.string.project);
                break;
            case "6":
                actionText = c.getString(R.string.songsheet);
                break;
        }
        return actionText;
    }

    boolean colorRow = true;
    private void colorRowColor(Context c, TableRow tableRow) {
        colorRow = !colorRow;
        if (colorRow) {
            tableRow.setBackgroundColor(ColorStateList.valueOf(c.getResources().getColor(R.color.colorSecondary)).getDefaultColor());
        }
    }

}
