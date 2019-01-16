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

// This class is used to index all of the songs in the user's folder
// It builds the search index and prepares the required stuff for the song menus (name, author, key)
// It relies on all of the songs to be added to the FullscreenActivity.songIds variable
// It then goes through them one at a time and extracts the required information

class IndexSongs {

    private boolean errorsencountered = false;

    interface MyInterface {
        void indexingDone();
    }
    MyInterface mListener;

    private XmlPullParser xpp;
    private StringBuilder errmsg = new StringBuilder(), log = new StringBuilder();
    InputStream inputStream;
    Uri uri;
    private String title, author, lyrics, theme, key, hymnnumber, copyright, alttheme, aka,
            user1, user2, user3, ccli, filename, folder, utf;
    private float filesize;

    // This is called if the user specifically requests a full rebuild of the index
    private void completeRebuildIndex(Context c, StorageAccess storageAccess, Preferences preferences,
                                      SongXML songXML, ChordProConvert chordProConvert,
                                      UsrConvert usrConvert, OnSongConvert onSongConvert,
                                      TextSongConvert textSongConvert) throws XmlPullParserException {

        initialiseIndexStuff();
        for (int w = 0; w<FullscreenActivity.songIds.size(); w++) {
            doIndexThis(c, storageAccess, preferences, songXML, chordProConvert, usrConvert, onSongConvert, textSongConvert, w);
        }
        completeLog();
        getSongDetailsFromIndex();
    }

    // This one prepares the search index log text
    void initialiseIndexStuff() throws XmlPullParserException {
        FullscreenActivity.safetosearch = false;
        FullscreenActivity.search_database = null;
        FullscreenActivity.search_database = new ArrayList<>();
        FullscreenActivity.search_database.clear();
        FullscreenActivity.indexlog = "";
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

    // This one prepares the end of the search index log text
    void completeLog() {
        if (errorsencountered) {
            FullscreenActivity.indexlog += "\n\nErrors in importing files\n\nThese songs are either not XML or have invalid XML\n\n" + errmsg;
        }
        int totalsongsindexed = FullscreenActivity.search_database.size();

        FullscreenActivity.indexlog += "\n\nTotal songs indexed=" + totalsongsindexed + "\n\n";
        FullscreenActivity.indexlog += log.toString();

        FullscreenActivity.safetosearch = true;
    }

    // This is the code to index the specific song (by sending the array index w)
    boolean doIndexThis(Context c, StorageAccess storageAccess, Preferences preferences,
                        SongXML songXML, ChordProConvert chordProConvert, UsrConvert usrConvert,
                        OnSongConvert onSongConvert, TextSongConvert textSongConvert, int w) {

        boolean hadtoconvert = false;

        String id = FullscreenActivity.songIds.get(w);
        Log.d("IndexSongs", "original songIds=" + id);
        initialiseSongTags();
        setFileAndUri(c, storageAccess, id);
        if (isDir(id)) {
            key = c.getString(R.string.songsinfolder);
        }
        title = filename;
        folder = extractFolderFromId(c, id);
        filesize = getFileSize(c, storageAccess, uri);

        if (storageAccess.isXML(uri)) {
            // This tries to extract the file contents as XML, but if that throws an error,
            // It looks for .chordpro, .onsong, or .txt.
            utf = storageAccess.getUTFEncoding(c, uri);
            hadtoconvert = getXMLStuff(c, storageAccess, preferences, chordProConvert, onSongConvert,
                    usrConvert, textSongConvert, songXML, id, w);

        }
        parseIndexedDetails(c);

        return hadtoconvert;
    }

    // Determine if the current songId is a directory or a file
    private boolean isDir(String id) {
        return id.endsWith("/");
    }

    // Before indexing a song, set all the tags to blank values
    private void initialiseSongTags() {
        author = "";
        lyrics = "";
        theme = "";
        key = "";
        hymnnumber = "";
        copyright = "";
        alttheme = "";
        aka = "";
        user1 = "";
        user2 = "";
        user3 = "";
        ccli = "";
    }

    // Prepare the song uri, inputstream and filename
    private void setFileAndUri(Context c, StorageAccess storageAccess, String id) {
        uri = storageAccess.getUriFromId(FullscreenActivity.uriTree, id);
        inputStream = storageAccess.getInputStream(c, uri);
        filename = uri.getLastPathSegment();
        if (storageAccess.lollipopOrLater() && filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf("/") + 1);
        }
    }

    // Extract the folder from the songId
    private String extractFolderFromId(Context c, String id) {
        int startoffolder = id.indexOf("OpenSong/Songs/");
        int endoffolder = id.indexOf(title);
        String folder;
        if (startoffolder > 0 && endoffolder > 0 && endoffolder > startoffolder) {
            folder = id.substring(startoffolder + 15, endoffolder);
        } else {
            folder = c.getString(R.string.mainfoldername);
        }

        if (folder.equals("")) {
            folder = c.getString(R.string.mainfoldername);
        }
        return folder;
    }

    // Get the file size in Kb
    private float getFileSize(Context c, StorageAccess storageAccess, Uri uri) {
        try {
            return storageAccess.getFileSizeFromUri(c, uri);
        } catch (Exception e) {
            return 1000000;
        }
    }

    // Get the stuff from the XML file
    private boolean getXMLStuff(Context c, StorageAccess storageAccess, Preferences preferences,
                                ChordProConvert chordProConvert, OnSongConvert onSongConvert,
                                UsrConvert usrConvert, TextSongConvert textSongConvert,
                                SongXML songXML, String id, int pos) {
        boolean hadtoconvert = false;

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
                    // If this is a ChordPro or OpenSong formatted Song, try to convert it and extract what we need
                    ArrayList<String> bits = tryToFixSong(c, storageAccess, preferences, songXML, chordProConvert,
                            onSongConvert, usrConvert, textSongConvert, uri, pos);
                    hadtoconvert = true;
                    filename = bits.get(0);
                    title = bits.get(1);
                    author = bits.get(2);
                    copyright = bits.get(3);
                    key = bits.get(4);
                    ccli = bits.get(5);
                    lyrics = bits.get(6);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            int start = id.indexOf("OpenSong/Songs/");
            if (start > 0 && id.length() > 15) {
                id = id.substring(start + 15);
            }
            if (!id.endsWith("/")) {
                // If this isn't a directory ending with / it is an error file
                errmsg.append("File with error = ").append(id).append("\n");
                errorsencountered = true;
                // Error in the xml, so import the text
                if (filesize < 250 && storageAccess.isTextFile(uri)) {
                    lyrics = storageAccess.readTextFileToString(inputStream);
                }
            } else {
                lyrics = "";
            }
        }
        return hadtoconvert;
    }

    private ArrayList<String> tryToFixSong(Context c, StorageAccess storageAccess, Preferences preferences,
                                           SongXML songXML, ChordProConvert chordProConvert,
                                           OnSongConvert onSongConvert, UsrConvert usrConvert,
                                           TextSongConvert textSongConvert, Uri uri, int pos) {

        ArrayList<String> bits = new ArrayList<>();

        if (uri != null) {
            String name = uri.getPath();
            String filename = storageAccess.getPartOfUri(uri, "/OpenSong/Songs");
            filename = filename.substring(filename.lastIndexOf("/"));

            if (name != null && (name.toLowerCase().endsWith(".cho") || name.toLowerCase().endsWith(".chordpro") ||
                    name.toLowerCase().endsWith(".chopro") || name.toLowerCase().endsWith(".crd"))) {
                Log.d("IndexSongs", "ChordPro, text or song without extension: " + uri);
                // Extract the stuff!
                // Load the current text contents
                try {
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = chordProConvert.convertTextToTags(c, storageAccess, preferences, songXML, uri, filecontents, pos);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (name != null && (name.toLowerCase().endsWith(".onsong"))) {
                Log.d("IndexSongs", "OnSong song:" + uri);
                try {
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = onSongConvert.convertTextToTags(c, storageAccess, preferences, songXML, chordProConvert,
                            uri, filecontents, pos);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (name != null && (name.toLowerCase().endsWith(".usr"))) {
                Log.d("IndexSongs", "Usr song:" + uri);
                try {
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = usrConvert.convertTextToTags(c, storageAccess, preferences, songXML,
                            chordProConvert, uri, filecontents, pos);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (name != null && (storageAccess.isTextFile(uri))) {
                Log.d("IndexSongs", "Try to treat as text file:" + uri);
                try {
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits.add(filename);
                    bits.add(filename);
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add(textSongConvert.convertText(c, filecontents));

                } catch (Exception e) {
                    Log.d("IndexSongs", "Can't index, so use filename only:" + uri);
                    bits.add(filename);
                    bits.add(filename);
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add("");
                    bits.add("");
                }
            } else {
                Log.d("IndexSongs", "Can't index, so use filename only:" + uri);
                bits.add(filename);
                bits.add(filename);
                bits.add("");
                bits.add("");
                bits.add("");
                bits.add("");
                bits.add("");
                bits.add("");
            }
        }
        return bits;
    }

    // Shorten the indexed stuff ready for the search database
    private void parseIndexedDetails(Context c) {
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

        String item_to_add = folder + "_%%%_" + filename + "_%%%_" + title + "_%%%_" + author + "_%%%_" +
                key + "_%%%_" + shortlyrics + "_%%%_" + theme + "_%%%_" + hymnnumber;

        if (filename != null && !filename.equals("")) {
            FullscreenActivity.search_database.add(item_to_add);
            String line_to_add = folder + "/" + filename + "\n";
            line_to_add = line_to_add.replace("//", "/");

            if (!line_to_add.endsWith("/\n")) {
                log.append(line_to_add);
            }
        }

    }

    // Once the song has been fully indexed, extract the stuff for the song menu
    // We only need the name, author and key
    void getSongDetailsFromIndex() {
        // Set the details for the menu - extract the appropriate stuff
        FullscreenActivity.allSongDetailsForMenu = new String[FullscreenActivity.search_database.size()][4];

        for (int x = 0; x < FullscreenActivity.search_database.size(); x++) {
            String[] bits = FullscreenActivity.search_database.get(x).split("_%%%_");
            FullscreenActivity.allSongDetailsForMenu[x][0] = getValue(bits, 0); // folder
            FullscreenActivity.allSongDetailsForMenu[x][1] = getValue(bits, 1); // filename (skip index 2 as it is the title)
            FullscreenActivity.allSongDetailsForMenu[x][2] = getValue(bits, 3); // author
            FullscreenActivity.allSongDetailsForMenu[x][3] = getValue(bits, 4); // key
        }
    }

    private String getValue(String[] bit, int index) {
        // If we are tring to get the folder
        if (index == 0) {
            // Get the root folder the song belongs to
            String root;
            if (bit.length > 0 && bit[0] != null) {
                root = bit[0];
                if (root.endsWith("/")) {
                    return root.substring(0, root.lastIndexOf("/"));
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }

        if (bit.length > index && bit[index] != null) {
            return bit[index];
        } else {
            return "";
        }
    }

    void indexMySongs(final Context c, final StorageAccess storageAccess, final Preferences preferences,
                      final SongXML songXML, final ChordProConvert chordProConvert, final UsrConvert usrConvert,
                      final OnSongConvert onSongConvert, final TextSongConvert textSongConvert) {
        // This indexes songs using a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                errorsencountered = false;
                //storageAccess.listSongs(c);
                String val;
                try {
                    //doIndex(c,storageAccess);
                    completeRebuildIndex(c, storageAccess, preferences, songXML, chordProConvert, usrConvert, onSongConvert, textSongConvert);
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