package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// This class is called asynchronously
public class IndexSongs extends Activity {

    public static void doIndex() throws XmlPullParserException, IOException {
        while (!FullscreenActivity.abort) {
            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            xppf.setNamespaceAware(true);
            XmlPullParser xpp = xppf.newPullParser();

            // List all songs and folders that exist
            ListSongFiles.listSongFolders();

            // Go through each song folder one at a time
            for (String mSongFolderName : FullscreenActivity.mSongFolderNames) {
                // Load each song
                for (String mSongFileName : FullscreenActivity.mSongFileNames) {
                    String title = "";
                    String author = "";
                    String key = "";
                    String lyrics = "";
                    String theme = "";
                    File songtoindex;
                    Log.d("index","mSongFolderName="+mSongFolderName);
                    Log.d("index","childSong="+mSongFileName);
                    if (mSongFolderName.equals(FullscreenActivity.mainfoldername) || mSongFolderName.equals("") || mSongFolderName.isEmpty()) {
                        songtoindex = new File(FullscreenActivity.dir + "/" + mSongFileName);
                    } else {
                        songtoindex = new File(FullscreenActivity.dir + "/" + mSongFolderName + "/" + mSongFileName);
                    }

                    // Load the song in
                    FileInputStream fis = new FileInputStream(songtoindex);
                    xpp.setInput(fis, null);

                    // Extract the title, author, key, lyrics, theme
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagname = xpp.getName();
                        String text = "";
                        switch (eventType) {
                            case XmlPullParser.END_TAG:
                                if (tagname.equalsIgnoreCase("title")) {
                                    title = xpp.getText();
                                } else if (tagname.equalsIgnoreCase("author")) {
                                    author = text;
                                } else if (tagname.equalsIgnoreCase("key")) {
                                    key = text;
                                } else if (tagname.equalsIgnoreCase("lyrics")) {
                                    lyrics = text;
                                } else if (tagname.equalsIgnoreCase("theme")) {
                                    theme = text;
                                }
                                break;

                            default:
                                break;
                        }
                        eventType = xpp.next();
                    }
                    // For now, show this as a log output
                    Log.d("index....","filename="+mSongFileName + "\nfolder="+mSongFolderName+ "\ntitle="+title + "\nauthor="+author+ "\nkey="+key + "\nlyrics="+lyrics+ "\ntheme="+theme);

                }
            }
        }
    }
}
