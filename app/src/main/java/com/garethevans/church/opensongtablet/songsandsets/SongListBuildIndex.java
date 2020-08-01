package com.garethevans.church.opensongtablet.songsandsets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SongListBuildIndex {

    // This class is used to index all of the songs in the user's folder
    // It builds the search index and prepares the required stuff for the song menus (name, author, key)
    // It updates the entries in the user sqlite database


    private SQLite sqLite;
    private CommonSQL commonSQL;


    private String songid;
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
        private String presentationorder;
        private InputStream inputStream;
        private Uri uri;
        private String utf;



        private SQLite updateSQLite(SQLite sqLite) {
            sqLite.setSongid(escaped(songid));
        }

        private String escaped(String string) {
            return commonSQL.escapedSQL(string);
        }
        private void initialiseValues(SQLite sqLite) {
            //id;
            songid = "";
            title = "";
            folder = "";
            filename = "";
            author = "";
            copyright = "";
            lyrics = "";
            capo = "";
            customchords = "";
            hymnnum = "";
            ccli = "";
            theme = "";
            alttheme = "";
            user1 = "";
            user2 = "";
            user3 = "";
            key = "";
            aka = "";
            tempo = "";
            timesig = "";
            duration = "";
            delay = "";
            presentationorder = "";
            midi = "";
            midiindex = "";
            notes = "";
            notation = "";
            padfile = "";
            padloop = "";
            linkaudio = "";
            linkweb = "";
            linkyoutube = "";
            linkother = "";
            filetype = "";


            inputStream = null;
            uri = null;
            utf = "UTF-8";

        }

        public void fullIndex(Context c, Preferences preferences, StorageAccess storageAccess,
                              SQLiteHelper sqLiteHelper, NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper,
                              CommonSQL commonSQL, SongXML songXML, ProcessSong processSong,
                              ConvertChoPro convertChoPro, ConvertOnSong convertOnSong,
                              ConvertTextSong textSongConvert, ShowToast showToast) {

            // The basic database was created on boot.
            // Now comes the time consuming bit that fully indexes the songs into the database

            try (SQLiteDatabase db = sqLiteHelper.getDB(c)) {
                // Go through each entry in the database and get the folder and filename.
                // Then load the file and write the values into the sql table
                String altquery = "SELECT " + SQLite.COLUMN_ID + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_FILENAME +
                        " FROM " + SQLite.TABLE_NAME;

                Cursor cursor = db.rawQuery(altquery, null);
                cursor.moveToFirst();

                // We now iterate through each song in turn!
                do {
                    SQLite sqLite = new SQLite();

                    // Get the folder and filename
                    if (cursor.getColumnIndex(SQLite.COLUMN_ID) > -1) {
                        sqLite.setId(cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)));
                        sqLite.setFolder(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
                        sqLite.setFilename(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)));

                        // Now we have the info to open the file and extract what we need
                        if (!sqLite.getFilename().isEmpty()) {
                            // Get the uri, utf and inputStream for the file
                            uri = storageAccess.getUriForItem(c, preferences, "Songs",
                                    commonSQL.unescapedSQL(sqLite.getFolder()), commonSQL.unescapedSQL(sqLite.getFilename()));
                            utf = storageAccess.getUTFEncoding(c, uri);
                            inputStream = storageAccess.getInputStream(c, uri);

                            // Now try to get the file as an xml.  If it encounters an error, it is treated in the catch statements
                            if (filenameIsOk(filename)) {
                                try {
                                    // All going well all the other details for sqLite are now set!
                                    getXMLStuff();

                                } catch (Exception e) {
                                    // OK, so this wasn't an XML file.  Try to extract as something else
                                    ArrayList<String> bits = tryToFixSong(c, storageAccess, 
                                            preferences, processSong, sqLiteHelper, songXML, 
                                            convertChoPro, convertOnSong, textSongConvert, uri);

                                    if (bits==null || bits.size()==0) {
                                        sqLite.setTitle(sqLite.getFilename());

                                    } else {
                                        sqLite.setFilename(escaped(bits.get(0));
                                        sqLite.setTitle(bits.get(1));
                                        sqLite.setAuthor(bits.get(2));
                                        sqLite.setCopyright(bits.get(3));
                                        sqLite.setKey(bits.get(4));
                                        sqLite.setTimesig(bits.get(5));
                                        sqLite.setCcli(bits.get(6));
                                        sqLite.setLyrics(bits.get(7));
                                    }
                                }
                            }

                            // Update the database entry
                            commonSQL.updateSong(db,sqLite);

                            // If the file is a PDF or IMG file, then we need to check it is in the persistent DB
                            // If not, add it.  Call update, if it fails (no match), the method catches it and creates the entry
                            if (sqLite.getFiletype().equals("PDF") || sqLite.getFiletype().equals("IMG")) {
                                nonOpenSongSQLiteHelper.updateSong(c, commonSQL, storageAccess, preferences, sqLite);
                            }
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError oom) {
                showToast.doIt(c,"Out of memory: "+folder+"/"+filename);
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

        private void getXMLStuff() throws Exception {
            try {
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();
                xpp.setInput(inputStream, utf);
                int eventType;

                // Extract all of the stuff we need
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
                        }
                    }
                    // This next line will throw an error if the song isn't xml
                    eventType = xpp.next();
                }

                // If we got this far, the song information was extracted!
            } catch (OutOfMemoryError e1) {
                e1.printStackTrace();
            }
        }

        private ArrayList<String> tryToFixSong(Context c, StorageAccess storageAccess, Preferences preferences,
                                               ProcessSong processSong, SQLiteHelper sqLiteHelper,
                                               SongXML songXML, ConvertChoPro convertChoPro,
                                               ConvertOnSong convertOnSong,
                                               ConvertTextSong textSongConvert, Uri uri) {

            ArrayList<String> bits = new ArrayList<>();

            if (uri != null) {
                if (isChordPro(filename)) {
                    // This is a chordpro file
                    // Load the current text contents
                    try {
                        String filecontents = storageAccess.readTextFileToString(inputStream);
                        bits = convertChoPro.convertTextToTags(c, storageAccess, preferences,
                                processSong, sqLiteHelper, commonSQL, songXML, uri, filecontents);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (filename.toLowerCase(Locale.ROOT).endsWith(".onsong")) {
                    try {
                        String filecontents = storageAccess.readTextFileToString(inputStream);
                        bits = convertOnSong.convertTextToTags(c, storageAccess, preferences, 
                                processSong, songXML, convertChoPro, sqLiteHelper,
                                uri, filecontents);

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
    }
