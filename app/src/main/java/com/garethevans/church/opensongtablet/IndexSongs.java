package com.garethevans.church.opensongtablet;

import android.app.Activity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

// This class is called asynchronously
public class IndexSongs extends Activity {

    public static void doIndex() throws XmlPullParserException, IOException {
        FullscreenActivity.safetosearch = false;
        FullscreenActivity.search_database = null;
        FullscreenActivity.search_database = new ArrayList<>();
        System.gc();

        // Get all the folders that are available
        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        // Need to add MAIN folder still......
        ArrayList<File> fixedfolders = new ArrayList<>();
        fixedfolders.add(FullscreenActivity.dir);
        for (File temp:tempmyitems) {
            if (temp.isDirectory()) {
                fixedfolders.add(temp);
            }
        }

        // Prepare the xml pull parser
        XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
        xppf.setNamespaceAware(true);
        XmlPullParser xpp = xppf.newPullParser();
        String filename;
        String folder;
        String title;
        String author;
        String lyrics;
        String theme;
        String key;
        String hymnnumber;

        // Now go through each folder and load each song in turn and then add it to the array
        for (File currfolder : fixedfolders) {
            // Removes start bit for subfolders
            String foldername = currfolder.toString().replace(songfolder.toString()+"/", "");
            // If in the main folder
            if (foldername.equals(songfolder.toString())) {
                foldername = FullscreenActivity.mainfoldername;
            }
            File files[] = currfolder.listFiles();
            // Go through each file
            for (File file : files) {
                if (file.isFile() && file.exists() && file.canRead() && !file.toString().contains(".")) {
                    filename = file.toString().replace(currfolder.toString() + "/", "");
                    folder = foldername;
                    title = "";
                    author = "";
                    lyrics = "";
                    theme = "";
                    key = "";
                    hymnnumber = "";

                    FileInputStream fis = new FileInputStream(file);
                    xpp.setInput(fis, null);

                    // Extract the title, author, key, lyrics, theme
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("author")) {
                                author = xpp.nextText();
                            } else if (xpp.getName().equals("title")) {
                                title = xpp.nextText();
                            } else if (xpp.getName().equals("lyrics")) {
                                lyrics = xpp.nextText();
                            } else if (xpp.getName().equals("key")) {
                                key = xpp.nextText();
                            } else if (xpp.getName().equals("theme")) {
                                theme = xpp.nextText();
                            } else if (xpp.getName().equals("hymn_number")) {
                                hymnnumber = xpp.nextText();
                            }
                        }
                        try {
                            eventType = xpp.next();
                        } catch (Exception e) {
                            eventType = XmlPullParser.END_DOCUMENT;
                        }
                    }

                    // Remove chord lines, empty lines and setions in lyrics (to save memory) - only line that start with " "
                    String lyricslines[] = lyrics.split("\n");
                    String shortlyrics = "";
                    for (String line : lyricslines) {
                        if (line.startsWith(" ")) {
                            shortlyrics = shortlyrics + line;
                        }
                    }
                    shortlyrics = shortlyrics.trim();
                    while (shortlyrics.contains("  ") ||shortlyrics.contains("_") ||shortlyrics.contains("|")) {
                        shortlyrics = shortlyrics.replace("  ", " ");
                        shortlyrics = shortlyrics.replace("_", "");
                        shortlyrics = shortlyrics.replace("|", " ");
                    }

                    shortlyrics = filename.trim() + " " + folder.trim() + " " + title.trim() + " " + author.trim() + " " +
                            key.trim() + " " + theme.trim() + " " + hymnnumber.trim() + " " + shortlyrics;

                    String item_to_add = filename + " _%%%_ " + folder + " _%%%_ " + title + " _%%%_ " + author + " _%%%_ " + shortlyrics + " _%%%_ " +
                            theme + " _%%%_ " + key + " _%%%_ " + hymnnumber;
                    FullscreenActivity.search_database.add(item_to_add);

                }
            }
        }
        System.gc();
        FullscreenActivity.safetosearch = true;
    }
}