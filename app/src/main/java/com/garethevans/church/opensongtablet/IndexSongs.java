package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

// This class is used to index all of the songs in the user's folder
// It builds the search index and prepares the required stuff for the song menus (name, author, key)
// It updates the entries in the user sqlite database

// Bits to extract from the song files
class IndexSongs {

    private String filename;
    private String folder;
    private String title;
    private String author;
    private String copyright;
    private String lyrics;
    private String hymnnum;
    private String ccli;
    private String theme;
    private String alttheme;
    private String user1;
    private String user2;
    private String user3;
    private String key;
    private String aka;
    private String timesig;
    private String tempo;
    private InputStream inputStream;
    private Uri uri;
    private String utf;

    private void initialiseValues() {

        title = "";
        folder = "";
        author = "";
        copyright = "";
        lyrics = "";
        hymnnum = "";
        ccli = "";
        theme = "";
        alttheme = "";
        user1 = "";
        user2 = "";
        user3 = "";
        key = "";
        aka = "";
        timesig = "";
        tempo = "";
        inputStream = null;
        uri = null;
        utf = "UTF-8";
    }

    void fullIndex(Context c, Preferences preferences, StorageAccess storageAccess,
                   SQLiteHelper sqLiteHelper, SongXML songXML,
                   ChordProConvert chordProConvert, OnSongConvert onSongConvert,
                   TextSongConvert textSongConvert, UsrConvert usrConvert) {

        // The basic database was created on boot.
        // Now comes the time consuming bit that fully indexes the songs into the database

        try (SQLiteDatabase db = sqLiteHelper.getDB(c)) {
            StringBuilder log = new StringBuilder();
            log.append("Search index progress.\n\n" +
                    "If the last song shown in this list is not the last song in your directory, there was an error indexing it.\n" +
                    "Please manually check that the file is a correctly formatted OpenSong file.\n\n\n");

            // Go through each entry in the database and get the folder and filename.
            // Then load the file and write the values into the sql table
            String altquery = "SELECT " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_FILENAME +
                    " FROM " + SQLite.TABLE_NAME;

            Cursor cursor = db.rawQuery(altquery, null);
            cursor.moveToFirst();
            do {
                initialiseValues();

                // Get the folder and filename
                if (cursor.getColumnIndex(SQLite.COLUMN_FOLDER) > -1) {
                    folder = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER));
                    filename = cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME));

                    if (!filename.equals("")) {
                        // Get the uri, utf and inputStream for the file
                        uri = storageAccess.getUriForItem(c, preferences, "Songs",
                                folder.replaceAll("''", "'"), filename.replaceAll("''", "'"));
                        utf = storageAccess.getUTFEncoding(c, uri);
                        inputStream = storageAccess.getInputStream(c, uri);

                        // Now try to get the file as an xml.  If it encounters an error, it is treated in the catch statements
                        if (filenameIsOk(filename)) {
                            try {
                                getXMLStuff();
                                log.append(folder).append("/").append(filename).append("\n");

                            } catch (Exception e) {
                                // OK, so this wasn't an XML file.  Try to extract as something else
                                ArrayList<String> bits = tryToFixSong(c, storageAccess, preferences, songXML, chordProConvert, onSongConvert,
                                        usrConvert, textSongConvert, uri);
                                // Remove the original filename from the database
                                if (bits!=null) {
                                    String songIdtoRemove = folder + "/" + filename;
                                    //Log.d("IndexSong", "songIdtoRemove=" + songIdtoRemove);
                                    //sqLiteHelper.deleteSong(c, songIdtoRemove);
                                }

                                if (bits==null || bits.size()==0) {
                                    title = filename;
                                    author = "";
                                    copyright = "";
                                    key = "";
                                    timesig = "";
                                    ccli = "";
                                    lyrics = "";
                                } else {
                                    filename = bits.get(0);
                                    title = bits.get(1);
                                    author = bits.get(2);
                                    copyright = bits.get(3);
                                    key = bits.get(4);
                                    timesig = bits.get(5);
                                    ccli = bits.get(6);
                                    lyrics = bits.get(7);
                                }
                            }
                        }

                        // Make the lyrics nicer for the database (remove chord lines and headings) - take this out to allow PDFs to be generated on the fly
                        /*String[] lines = lyrics.split("\n");
                        StringBuilder sb = new StringBuilder();
                        for (String l : lines) {
                            if (!l.startsWith("[") && !l.startsWith(".")) {
                                sb.append(l).append("\n");
                            }
                        }
                        lyrics = sb.toString();*/

                        String songid = folder.replaceAll("'", "''") + "/" + filename.replaceAll("'", "''");
                        // Now we have the song info, update the table row
                        String updateQuery = "UPDATE " + SQLite.TABLE_NAME + " " + "SET " +
                                SQLite.COLUMN_FOLDER + "='" + folder.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_FILENAME + "='" + filename.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_TITLE + "='" + title.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_AUTHOR + "='" + author.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_COPYRIGHT + "='" + copyright.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_LYRICS + "='" + lyrics.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_KEY + "='" + key.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_THEME + "='" + theme.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_ALTTHEME + "='" + alttheme.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_AKA + "='" + aka.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_USER1 + "='" + user1.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_USER2 + "='" + user2.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_USER3 + "='" + user3.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_HYMNNUM + "='" + hymnnum.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_CCLI + "='" + ccli.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_TEMPO + "='" + tempo.replaceAll("'", "''") + "', " +
                                SQLite.COLUMN_TIMESIG + "='" + timesig.replaceAll("'", "''") + "' " +
                                "WHERE " + SQLite.COLUMN_SONGID + "='" + songid + "';";
                        try {
                            db.execSQL(updateQuery);
                        } catch (OutOfMemoryError | Exception e) {
                            e.printStackTrace();
                        }
                        // close the db connection
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oom) {
            StaticVariables.myToastMessage = "Out of memory: "+folder+"/"+filename;
            ShowToast.showToast(c);
        }

    }

    private boolean filenameIsOk(String f) {
        f = f.toLowerCase(Locale.ROOT);
        if (f.contains(".")) {
            f = f.substring(f.lastIndexOf("."));
            String badendings = ".pdf.png.jpg.jpeg.gif.jpeg.doc.docx.sqlite.db";
            return !badendings.contains(f);
        }
        return true;
    }

    private boolean isChordPro(String f) {
        f = f.toLowerCase(Locale.ROOT);
        if (f.contains(".")) {
            f = f.substring(f.lastIndexOf("."));
            String chordproendings = ".chopro.crd.chordpro.pro.cho";
            return chordproendings.contains(f);
        }
        return false;
    }

    private void getXMLStuff() throws Exception{
        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();
            xpp.setInput(inputStream, utf);
            int eventType;

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
                            user2 = xpp.nextText();
                            break;

                        case "user3":
                            user3 = xpp.nextText();
                            break;

                        case "aka":
                            aka = xpp.nextText();
                            break;

                        case "hymn_number":
                            hymnnum = xpp.nextText();
                            break;

                        case "tempo":
                            tempo = xpp.nextText();
                            break;

                        case "time_sig":
                            timesig = xpp.nextText();
                            break;
                    }
                }
                // This next line will throw an error if the song isn't xml
                eventType = xpp.next();
            }

            // If we got this far, the song information was extracted!
        } catch (OutOfMemoryError e1) {
            e1.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> tryToFixSong(Context c, StorageAccess storageAccess, Preferences preferences,
                                           SongXML songXML, ChordProConvert chordProConvert,
                                           OnSongConvert onSongConvert, UsrConvert usrConvert,
                                           TextSongConvert textSongConvert, Uri uri) {

        ArrayList<String> bits = new ArrayList<>();

        if (uri != null) {
            if (isChordPro(filename)) {
                // This is a chordpro file
                // Load the current text contents
                try {
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = chordProConvert.convertTextToTags(c, storageAccess, preferences, songXML, uri, filecontents);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (filename.toLowerCase(Locale.ROOT).endsWith(".onsong")) {
                try {
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = onSongConvert.convertTextToTags(c, storageAccess, preferences, songXML, chordProConvert,
                            uri, filecontents);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (filename.toLowerCase(Locale.ROOT).endsWith(".usr")) {
                try {
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    bits = usrConvert.convertTextToTags(c, storageAccess, preferences, songXML,
                            chordProConvert, uri, filecontents);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (storageAccess.isTextFile(uri)) {
                try {
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

    interface MyInterface {
    }
}