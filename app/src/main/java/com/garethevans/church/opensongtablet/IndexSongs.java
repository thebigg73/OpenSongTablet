package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

class IndexSongs {

    private boolean errorsencountered = false;

    interface MyInterface {
        void indexingDone();
    }
    MyInterface mListener;

    private ArrayList<String> songMenuInfo;
    private XmlPullParser xpp;
    private StringBuilder errmsg = new StringBuilder(), log = new StringBuilder();

    private void completeRebuildIndex(Context c, StorageAccess storageAccess) throws XmlPullParserException{
        initialiseIndexStuff();
        for (int w=0;w<FullscreenActivity.songIds.size();w++) {
            doIndexThis(c,storageAccess,w);
        }
        completeLog();
        getSongDetailsFromIndex();
    }

    void initialiseIndexStuff() throws XmlPullParserException {
        FullscreenActivity.safetosearch = false;
        FullscreenActivity.search_database = null;
        FullscreenActivity.search_database = new ArrayList<>();
        FullscreenActivity.search_database.clear();
        FullscreenActivity.indexlog = "";
        songMenuInfo = new ArrayList<>();
        songMenuInfo.clear();
        XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
        xppf.setNamespaceAware(true);
        xpp = xppf.newPullParser();
        log.append("Search index progress.\n\n" +
                "If the last song shown in this list is not the last song in your directory, there was an error indexing it.\n" +
                "Please manually check that the file is a correctly formatted OpenSong file.\n\n\n");

        // Sort the ids
        Collator collator = Collator.getInstance(FullscreenActivity.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(FullscreenActivity.songIds, collator);
    }

    void doIndexThis(Context c, StorageAccess storageAccess, int w) {
        String ids = FullscreenActivity.songIds.get(w);
        Uri uri = storageAccess.getUriFromId(FullscreenActivity.uriTree, ids);
        InputStream inputStream = storageAccess.getInputStream(c, uri);
        String filename = uri.getLastPathSegment();
        if (storageAccess.lollipopOrLater() && filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
        String author = "";
        String lyrics = "";
        String theme = "";
        String key = "";
        String hymnnumber = "";
        String copyright = "";
        String alttheme = "";
        String aka = "";
        String user1 = "";
        String user2 = "";
        String user3 = "";
        String ccli = "";
        // Set the title as the filename by default in case this isn't an OpenSong xml
        String title = filename;

        int startoffolder = ids.indexOf("OpenSong/Songs/");
        int endoffolder = ids.indexOf(title);
        String folder;
        if (startoffolder > 0 && endoffolder > 0 && endoffolder > startoffolder) {
            folder = ids.substring(startoffolder + 15, endoffolder);
        } else {
            folder = c.getString(R.string.mainfoldername);
        }

        if (folder.equals("")) {
            folder = c.getString(R.string.mainfoldername);
        }

        // Try to get the file length
        long filesize;
        try {
            filesize = storageAccess.getFileSizeFromUri(c, uri);
        } catch (Exception e) {
            filesize = 1000000;
        }

        if (storageAccess.isXML(uri)) {
            String utf = storageAccess.getUTFEncoding(c, uri);
            int eventType;
            try {
                xpp.setInput(inputStream, utf);
                // Extract the title, author, key, lyrics, theme
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "author":
                                author = xpp.nextText();
                                break;

                            case "title":
                                title = xpp.nextText();
                                break;

                            case "lyrics":
                                lyrics = xpp.nextText();
                                break;

                            case "key":
                                key = xpp.nextText();
                                break;

                            case "theme":
                                theme = xpp.nextText();
                                break;

                            case "copyright":
                                copyright = xpp.nextText();
                                break;

                            case "ccli":
                                ccli = xpp.nextText();
                                break;

                            case "alttheme":
                                alttheme = xpp.nextText();
                                break;

                            case "user1":
                                user1 = xpp.nextText();
                                break;

                            case "user2":
                                user1 = xpp.nextText();
                                break;

                            case "user3":
                                user1 = xpp.nextText();
                                break;

                            case "aka":
                                aka = xpp.nextText();
                                break;

                            case "hymn_number":
                                hymnnumber = xpp.nextText();
                                break;
                        }
                    }
                    try {
                        eventType = xpp.next();
                    } catch (Exception e) {
                        Log.d("d", "Not XML OpenSong format: issue with " + uri);
                        //e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                int start = ids.indexOf("OpenSong/Songs/");
                if (start > 0 && ids.length() > 15) {
                    ids = ids.substring(start + 15);
                }
                if (!ids.endsWith("/")) {
                    // If this isn't a directory ending with / it is an error file
                    errmsg.append("File with error = ").append(ids).append("\n");
                    errorsencountered = true;
                    // Error in the xml, so import the text
                    if (filesize < 250 && storageAccess.isTextFile(uri)) {
                        lyrics = storageAccess.readTextFileToString(inputStream);
                    }
                } else {
                    lyrics = "";
                }

            }
        } else {
            if (filesize < 250 && storageAccess.isTextFile(uri)) {
                lyrics = storageAccess.readTextFileToString(inputStream);
            }
        }

        // Remove chord lines, empty lines and setions in lyrics (to save memory) - only line that start with " "
        String lyricslines[] = lyrics.split("\n");
        StringBuilder shortlyrics = new StringBuilder();
        for (String line : lyricslines) {
            if (!line.startsWith(".") && !line.startsWith("[") && !line.equals("")) {
                if (line.startsWith(";")) {
                    line = line.substring(1);
                }
                shortlyrics.append(line);
            }
        }

        shortlyrics = new StringBuilder(filename.trim() + " " + folder.trim() + " " + title.trim() + " " + author.trim() + " " +
                c.getString(R.string.edit_song_key) + " " + key.trim() + " " + copyright.trim() + " " + ccli.trim() + " " +
                user1.trim() + " " + user2.trim() + " " + user3.trim() + " " + alttheme.trim() + " " + aka.trim() + " " +
                theme.trim() + " " + hymnnumber.trim() + " " + shortlyrics.toString().trim());

        // Replace unwanted symbols
        shortlyrics = new StringBuilder(ProcessSong.removeUnwantedSymbolsAndSpaces(shortlyrics.toString()));

        String item_to_add = filename + " _%%%_ " + folder + " _%%%_ " + title + " _%%%_ " + author + " _%%%_ " + shortlyrics + " _%%%_ " +
                theme + " _%%%_ " + key + " _%%%_ " + hymnnumber;

        FullscreenActivity.search_database.add(item_to_add);
        songMenuInfo.add(folder +"_&&_"+ filename +"_&&_"+ author +"_&&_"+ key);

        String line_to_add = folder + "/" + filename + "\n";
        line_to_add = line_to_add.replace("//", "/");

        if (!line_to_add.endsWith("/\n")) {
            log.append(line_to_add);
        }
    }

    void completeLog() {
        if (errorsencountered) {
            FullscreenActivity.indexlog += "\n\nErrors in importing files\n\nThese songs are either not XML or have invalid XML\n\n" + errmsg;
        }
        int totalsongsindexed = FullscreenActivity.search_database.size();

        FullscreenActivity.indexlog += "\n\nTotal songs indexed=" + totalsongsindexed + "\n\n";
        FullscreenActivity.indexlog += log.toString();

        FullscreenActivity.safetosearch = true;
    }

    void getSongDetailsFromIndex() {
        // Set the details for the menu
        FullscreenActivity.allSongDetailsForMenu = new String[songMenuInfo.size()][4];

        for (int x=0; x<songMenuInfo.size(); x++) {
            String[] bits = songMenuInfo.get(x).split("_&&_");
            if (bits.length>0 && bits[0] != null) {
                if (bits[0].endsWith("/")) {
                    bits[0] = bits[0].substring(0,bits[0].lastIndexOf("/"));
                }
                FullscreenActivity.allSongDetailsForMenu[x][0] = bits[0];
            } else {
                FullscreenActivity.allSongDetailsForMenu[x][0] = "";
            }
            if (bits.length>1 && bits[1] != null) {
                FullscreenActivity.allSongDetailsForMenu[x][1] = bits[1];
            } else {
                FullscreenActivity.allSongDetailsForMenu[x][1] = "";
            }
            if (bits.length>2 && bits[2] != null) {
                FullscreenActivity.allSongDetailsForMenu[x][2] = bits[2];
            } else {
                FullscreenActivity.allSongDetailsForMenu[x][2] = "";
            }
            if (bits.length>3 && bits[3] != null) {
                FullscreenActivity.allSongDetailsForMenu[x][3] = bits[3];
            } else {
                FullscreenActivity.allSongDetailsForMenu[x][3] = "";
            }
        }
    }

    void indexMySongs(final Context c, final StorageAccess storageAccess) {
        // This indexes songs using a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                errorsencountered = false;
                //storageAccess.listSongs(c);
                String val;
                try {
                    //doIndex(c,storageAccess);
                    completeRebuildIndex(c,storageAccess);
                    val = "ok";
                } catch (Exception e) {
                    e.printStackTrace();
                    val = "error";
                }
                if (errorsencountered) {
                    val = "error";
                }

                try {
                    if (val.equals("error")) {
                        FullscreenActivity.myToastMessage = c.getString(R.string.search_index_error)+"\n"+
                                c.getString(R.string.search_log);
                        ((Activity) c).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowToast.showToast(c);
                            }
                        });
                        FullscreenActivity.safetosearch = true;
                        SharedPreferences indexSongPreferences = c.getSharedPreferences("indexsongs",MODE_PRIVATE);
                        SharedPreferences.Editor editor_index = indexSongPreferences.edit();
                        editor_index.putBoolean("buildSearchIndex", true);
                        editor_index.apply();
                    } else {
                        FullscreenActivity.myToastMessage = c.getString(R.string.search_index_end);
                        ((Activity) c).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ShowToast.showToast(c);
                            }
                        });
                        if (mListener!=null) {
                            mListener.indexingDone();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}