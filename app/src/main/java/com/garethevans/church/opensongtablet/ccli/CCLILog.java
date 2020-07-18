package com.garethevans.church.opensongtablet.ccli;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

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

    String thisdate;
    String thistime;

    private ArrayList<String> songfile, song, author, copyright, ccli, date, time, action;

    public void addEntry(Context c, Preferences preferences, StorageAccess storageAccess, String usageType) {

        // Check if the log exists or if we need to create it
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");
        if (!storageAccess.uriExists(c, uri)) {
            Log.d("d", "Creating blankXML=" + createBlankXML(c, preferences, storageAccess, uri));
        } else {
            Log.d("d", uri + " exists");
        }

        // Set the date and time
        setTheDateAndTime();

        doTheSaving(c, storageAccess, uri, usageType);
    }

    public boolean createBlankXML(Context c, Preferences preferences, StorageAccess storageAccess, Uri uri) {
        String blankXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<log>\n</log>\n";

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Settings", "", "ActivityLog.xml");

        OutputStream outputStream = storageAccess.getOutputStream(c, uri);
        return storageAccess.writeFileFromString(blankXML, outputStream);
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

    private void doTheSaving(Context c, StorageAccess storageAccess, Uri uri, String usageType) {
        try {
            InputStream inputStream = storageAccess.getInputStream(c, uri);
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
                a_date.appendChild(document.createTextNode(thisdate));
                newItem.appendChild(a_date);

                Element a_time = document.createElement("Time");
                a_time.appendChild(document.createTextNode(thistime));
                newItem.appendChild(a_time);

                Element a_usage = document.createElement("Description");
                a_usage.appendChild(document.createTextNode(usageType));
                newItem.appendChild(a_usage);

                Element a_fname = document.createElement("FileName");
                a_fname.appendChild(document.createTextNode(StaticVariables.songfilename));
                newItem.appendChild(a_fname);

                Element a_song = document.createElement("title");
                a_song.appendChild(document.createTextNode(StaticVariables.mTitle));
                newItem.appendChild(a_song);

                Element a_author = document.createElement("author");
                a_author.appendChild(document.createTextNode(StaticVariables.mAuthor));
                newItem.appendChild(a_author);

                Element a_copyright = document.createElement("copyright");
                a_copyright.appendChild(document.createTextNode(StaticVariables.mCopyright));
                newItem.appendChild(a_copyright);

                Element a_ccli = document.createElement("ccli");
                a_ccli.appendChild(document.createTextNode(StaticVariables.mCCLI));
                newItem.appendChild(a_ccli);

                if (root != null) {
                    root.appendChild(newItem);
                }

                DOMSource source = new DOMSource(document);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                OutputStream outputStream = storageAccess.getOutputStream(c, uri);
                StreamResult result = new StreamResult(outputStream);
                transformer.transform(source, result);
            } else {
                Log.d("CCLI", "doument was null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLogFileSize(Context c, StorageAccess storageAccess, Uri uri) {
        // Set the uri if it isn't already done
        float file_size_kb = storageAccess.getFileSizeFromUri(c, uri);
        file_size_kb = Math.round(file_size_kb * 100);
        file_size_kb = file_size_kb / 100.0f;
        String returntext = "ActivityLog.xml ("+ file_size_kb + "kb)";
        if (file_size_kb > 1024) {
            returntext = " <font color='#f00'>ActivityLog.xml ("+file_size_kb + "kb)" + "</font>";
        }
        return returntext;
    }

    private void initialiseTables() {
        songfile = new ArrayList<>();
        song = new ArrayList<>();
        author = new ArrayList<>();
        copyright = new ArrayList<>();
        ccli = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        action = new ArrayList<>();
    }

    void getCurrentEntries(Context c, StorageAccess storageAccess, Uri uri) {

        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();

            initialiseTables();
            InputStream inputStream = storageAccess.getInputStream(c, uri);

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
            initialiseTables();
        }
    }

    String buildMyTable(Context c, Preferences preferences, String sizeoffile) {
        if (song == null || song.size() == 0) {
            return "<html><body><h2>" + c.getResources().getString(R.string.edit_song_ccli) + "</h2>\n" +
                    "<h3>" + c.getResources().getString(R.string.ccli_church) + ": " +
                    preferences.getMyPreferenceString(c,"ccliChurchName","") + "</h3>\n" +
                    "<h3>" + c.getResources().getString(R.string.ccli_licence) + ": " +
                    preferences.getMyPreferenceString(c,"ccliLicence","")+ "</h3>\n" +
                    "<h4>" + sizeoffile + "</h4>\n" +
                    "</body></html>";

        } else {
            StringBuilder table = new StringBuilder("<html><head>\n" +
                    "<style>\n#mytable {\nborder-collapse: collapse; width: 100%;\n}\n" +
                    "#mytable td, #mytable th {\nborder: 1px solid #ddd; padding: 2px;\n}\n" +
                    "#mytable tr:nth-child(even) {\nbackground-color: #f2f2f2;\n}\n" +
                    "#mytable th {\npadding-top: 2px; padding-bottom: 2px; text-align: left; " +
                    "background-color: #4CAF50; color: white;\n}\n" +
                    "</style>\n</head><body>" +
                    "<h2>" + c.getResources().getString(R.string.edit_song_ccli) + "</h2>\n" +
                    "<h3>" + c.getResources().getString(R.string.ccli_church) + ": " +
                    preferences.getMyPreferenceString(c,"ccliChurchName","") + "</h3>\n" +
                    "<h3>" + c.getResources().getString(R.string.ccli_licence) + ": " +
                    preferences.getMyPreferenceString(c,"ccliLicence","")+ "</h3>\n" +
                    "<h4>" + sizeoffile + "</h4>\n" +
                    "<body><table id=\"mytable\">\n<tr>");
            table.append("<th>").append(c.getResources().getString(R.string.item)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.edit_song_title)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.edit_song_author)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.edit_song_copyright)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.edit_song_ccli)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.date)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.time)).append("</th>");
            table.append("<th>").append(c.getResources().getString(R.string.action)).append("</th>");
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
                    default:
                        table.append("<td>").append(c.getResources().getString(R.string.other)).append("</td>");
                        break;
                    case "1":
                        table.append("<td>").append(c.getResources().getString(R.string.new_something)).append("</td>");
                        break;
                    case "2":
                        table.append("<td>").append(c.getResources().getString(R.string.delete)).append("</td>");
                        break;
                    case "3":
                        table.append("<td>").append(c.getResources().getString(R.string.edit)).append("</td>");
                        break;
                    case "4":
                    case "7":
                        table.append("<td>").append(c.getResources().getString(R.string.rename)).append("</td>");
                        break;
                    case "5":
                        table.append("<td>").append(c.getResources().getString(R.string.sendtoprojector)).append("</td>");
                        break;
                    case "6":
                        table.append("<td>").append(c.getResources().getString(R.string.songsheet)).append("</td>");
                        break;
                }
                table.append("<tr>\n");
            }
            table.append("</table></body></html>");
            return table.toString();
        }
    }

}
