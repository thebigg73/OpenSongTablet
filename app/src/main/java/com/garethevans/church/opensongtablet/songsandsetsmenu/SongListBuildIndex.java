package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ConvertTextSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.InputStream;
import java.util.Locale;

public class SongListBuildIndex {

    // This class is used to index all of the songs in the user's folder
    // It builds the search index and prepares the required stuff for the song menus (name, author, key)
    // It updates the entries in the user sqlite database


    private Song song;
    private InputStream inputStream;
    private boolean indexRequired;
    private boolean indexComplete;

    public void setIndexRequired(boolean indexRequired) {
        this.indexRequired = indexRequired;
    }
    public boolean getIndexRequired() {
        return indexRequired;
    }
    public void setIndexComplete(boolean indexComplete) {
        this.indexComplete = indexComplete;
    }
    public boolean getIndexComplete() {
        return indexComplete;
    }

    public void fullIndex(Context c,
                          Preferences preferences, StorageAccess storageAccess,
                          SQLiteHelper sqLiteHelper, NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper,
                          CommonSQL commonSQL, ProcessSong processSong,
                          ConvertChoPro convertChoPro, ConvertOnSong convertOnSong,
                          ConvertTextSong textSongConvert, ShowToast showToast, LoadSong loadSong) {

        MainActivityInterface mainActivityInterface = (MainActivityInterface) c;
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
                Song song = new Song();

                // Set the folder and filename from the database entry
                if (cursor.getColumnIndex(SQLite.COLUMN_ID) > -1) {
                    song.setId(cursor.getInt(cursor.getColumnIndex(SQLite.COLUMN_ID)));
                    song.setFolder(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FOLDER)));
                    song.setFilename(cursor.getString(cursor.getColumnIndex(SQLite.COLUMN_FILENAME)));

                    // Now we have the info to open the file and extract what we need
                    if (!song.getFilename().isEmpty()) {
                        // Get the uri, utf and inputStream for the file
                        Uri uri = storageAccess.getUriForItem(c, preferences, "Songs",
                                commonSQL.unescapedSQL(song.getFolder()), commonSQL.unescapedSQL(song.getFilename()));
                        String utf = storageAccess.getUTFEncoding(c, uri);
                        inputStream = storageAccess.getInputStream(c, uri);

                        // Now try to get the file as an xml.  If it encounters an error, it is treated in the catch statements
                        if (filenameIsOk(song.getFilename())) {
                            try {
                                // All going well all the other details for sqLite are now set!
                                loadSong.readFileAsXML(c, mainActivityInterface,storageAccess,
                                        preferences, processSong, song,showToast, "Songs",
                                        uri, utf, true);

                            } catch (Exception e) {
                                // OK, so this wasn't an XML file.  Try to extract as something else
                                song = tryToFixSong(c, storageAccess, preferences, processSong,
                                        sqLiteHelper, commonSQL,
                                        convertChoPro, convertOnSong, textSongConvert, uri);
                            }
                        }

                        // Update the database entry
                        song.setSongid(commonSQL.getAnySongId(song.getFolder(), song.getFilename()));
                        commonSQL.updateSong(db, song);

                        // If the file is a PDF or IMG file, then we need to check it is in the persistent DB
                        // If not, add it.  Call update, if it fails (no match), the method catches it and creates the entry
                        if (song.getFiletype().equals("PDF") || song.getFiletype().equals("IMG")) {
                            nonOpenSongSQLiteHelper.updateSong(c, commonSQL, storageAccess, preferences, song);
                        }
                        inputStream.close();
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError oom) {
            showToast.doIt(c, "Out of memory: " + song.getFolder() + "/" + song.getFilename());
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

    private Song tryToFixSong(Context c, StorageAccess storageAccess, Preferences preferences,
                              ProcessSong processSong, SQLiteHelper sqLiteHelper,
                              CommonSQL commonSQL, ConvertChoPro convertChoPro,
                              ConvertOnSong convertOnSong, ConvertTextSong textSongConvert, Uri uri) {

        if (uri != null) {
            if (isChordPro(song.getFilename())) {
                song.setFiletype("CHO");
                // This is a chordpro file
                // Load the current text contents
                try {
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    song.setLyrics(filecontents);
                    song = convertChoPro.convertTextToTags(c, storageAccess, preferences,
                            processSong, sqLiteHelper, commonSQL, uri, song);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (song.getFilename().toLowerCase(Locale.ROOT).endsWith(".onsong")) {
                try {
                    song.setFiletype("iOS");
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    song.setLyrics(filecontents);
                    song = convertOnSong.convertTextToTags(c, storageAccess, preferences,
                            processSong, convertChoPro, sqLiteHelper, commonSQL,
                            uri, song);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (storageAccess.isTextFile(uri)) {
                try {
                    song.setFiletype("TXT");
                    String filecontents = storageAccess.readTextFileToString(inputStream);
                    song.setTitle(song.getFilename());
                    song.setAuthor("");
                    song.setCopyright("");
                    song.setKey("");
                    song.setTimesig("");
                    song.setCcli("");
                    song.setLyrics(textSongConvert.convertText(c, filecontents));


                } catch (Exception e) {
                    song.setTitle(song.getFilename());
                    song.setAuthor("");
                    song.setCopyright("");
                    song.setKey("");
                    song.setTimesig("");
                    song.setCcli("");
                    song.setLyrics("");

                }
            } else {
                song.setTitle(song.getFilename());
                song.setAuthor("");
                song.setCopyright("");
                song.setKey("");
                song.setTimesig("");
                song.setCcli("");
                song.setLyrics("");
            }
        }
        return song;
    }
}
