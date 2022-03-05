package com.garethevans.church.opensongtablet.songmenu;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.SQLite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class SongListBuildIndex {

    // This class is used to index all of the songs in the user's folder
    // It builds the search index and prepares the required stuff for the song menus (name, author, key)
    // It updates the entries in the user sqlite database
    // It loads contents into a temp song on the mainactivity called indexingSong;

    private boolean indexRequired;
    private boolean indexComplete;
    private boolean currentlyIndexing = false;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    public SongListBuildIndex(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    public void setIndexRequired(boolean indexRequired) {
        this.indexRequired = indexRequired;
    }
    public boolean getIndexRequired() {
        return indexRequired;
    }
    public void setCurrentlyIndexing(boolean currentlyIndexing) {
        this.currentlyIndexing = currentlyIndexing;
    }
    public boolean getCurrentlyIndexing() {
        return currentlyIndexing;
    }
    public void setIndexComplete(boolean indexComplete) {
        this.indexComplete = indexComplete;
    }
    public boolean getIndexComplete() {
        return indexComplete;
    }

    public void buildBasicFromFiles() {
        // This creates a basic database from the song files
        ArrayList<String> songIds = mainActivityInterface.getStorageAccess().listSongs();
        mainActivityInterface.getStorageAccess().writeSongIDFile(songIds);
        mainActivityInterface.getSQLiteHelper().resetDatabase();
        mainActivityInterface.getSQLiteHelper().insertFast();
    }

    public String fullIndex() {
        // The basic database was created on boot.
        // Now comes the time consuming bit that fully indexes the songs into the database
        currentlyIndexing = true;
        StringBuilder returnString = new StringBuilder();
        try (SQLiteDatabase db = mainActivityInterface.getSQLiteHelper().getDB()) {
            // Go through each entry in the database and get the folder and filename.
            // Then load the file and write the values into the sql table
            String altquery = "SELECT " + SQLite.COLUMN_ID + ", " + SQLite.COLUMN_FOLDER + ", " + SQLite.COLUMN_FILENAME +
                    " FROM " + SQLite.TABLE_NAME;

            Cursor cursor = db.rawQuery(altquery, null);

            if (cursor.getCount()>0) {
                cursor.moveToFirst();

                // We now iterate through each song in turn!
                do {
                    mainActivityInterface.setIndexingSong(new Song());

                    // Set the folder and filename from the database entry
                    int indexId = cursor.getColumnIndex(SQLite.COLUMN_ID);
                    int indexFolder = cursor.getColumnIndex(SQLite.COLUMN_FOLDER);
                    int indexFilename = cursor.getColumnIndex(SQLite.COLUMN_FILENAME);

                    if (indexId >= 0 && indexFolder >= 0 && indexFilename >= 0) {
                        mainActivityInterface.getIndexingSong().setId(cursor.getInt(indexId));
                        mainActivityInterface.getIndexingSong().setFolder(cursor.getString(indexFolder));
                        mainActivityInterface.getIndexingSong().setFilename(cursor.getString(indexFilename));

                        // Now we have the info to open the file and extract what we need
                        if (!mainActivityInterface.getIndexingSong().getFilename().isEmpty()) {
                            // Get the uri, utf and inputStream for the file
                            Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs",
                                    mainActivityInterface.getIndexingSong().getFolder(),
                                    mainActivityInterface.getIndexingSong().getFilename());
                            String utf = mainActivityInterface.getStorageAccess().getUTFEncoding(uri);

                            // Now try to get the file as an xml.  If it encounters an error, it is treated in the catch statements
                            if (filenameIsOk(mainActivityInterface.getIndexingSong().getFilename())) {
                                try {
                                    // All going well all the other details for sqLite are now set!
                                    // Assume XML for now!
                                    mainActivityInterface.getIndexingSong().setFiletype("XML");

                                    mainActivityInterface.getLoadSong().readFileAsXML(mainActivityInterface.getIndexingSong(), "Songs",
                                            uri, utf);

                                } catch (Exception e) {
                                    // OK, so this wasn't an XML file.  Try to extract as something else
                                    mainActivityInterface.setIndexingSong(tryToFixSong(mainActivityInterface.getIndexingSong(), uri));
                                }
                            } else {
                                // Look for data in the nonopensong persistent database import
                                mainActivityInterface.setIndexingSong(mainActivityInterface.getNonOpenSongSQLiteHelper().getSpecificSong(mainActivityInterface.getIndexingSong().getFolder(), mainActivityInterface.getIndexingSong().getFilename()));
                                if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf", mainActivityInterface.getIndexingSong().getFilename())) {
                                    // This is a PDF
                                    mainActivityInterface.getIndexingSong().setFiletype("PDF");
                                } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("image", mainActivityInterface.getIndexingSong().getFilename())) {
                                    // This is an Image
                                    mainActivityInterface.getIndexingSong().setFiletype("IMG");
                                } else {
                                    // Unknown
                                    mainActivityInterface.getIndexingSong().setFiletype("?");
                                }

                            }

                            // Update the database entry
                            mainActivityInterface.getIndexingSong().setSongid(mainActivityInterface.getCommonSQL().getAnySongId(
                                    mainActivityInterface.getIndexingSong().getFolder(), mainActivityInterface.getIndexingSong().getFilename()));
                            mainActivityInterface.getCommonSQL().updateSong(db, mainActivityInterface.getIndexingSong());

                            // If the file is a PDF or IMG file, then we need to check it is in the persistent DB
                            // If not, add it.  Call update, if it fails (no match), the method catches it and creates the entry
                            if (mainActivityInterface.getIndexingSong().getFiletype().equals("PDF") ||
                                    mainActivityInterface.getIndexingSong().getFiletype().equals("IMG")) {
                                mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(mainActivityInterface.getIndexingSong());
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            indexRequired = false;
            indexComplete = true;
            mainActivityInterface.getSetActions().checkMissingKeys();
            returnString.append(c.getString(R.string.search_index_end)).append("\n");

        } catch (Exception e) {
            e.printStackTrace();
            returnString.append(c.getString(R.string.search_index_error)).append("\n");
        } catch (OutOfMemoryError oom) {
            mainActivityInterface.getShowToast().doIt("Out of memory: " +
                    mainActivityInterface.getIndexingSong().getFolder() + "/" +
                    mainActivityInterface.getIndexingSong().getFilename());
            returnString.append(c.getString(R.string.search_index_error)).append(": ").
                    append(mainActivityInterface.getIndexingSong().getFolder()).append("/").
                    append(mainActivityInterface.getIndexingSong().getFilename()).append("\n");
        }
        currentlyIndexing = false;
        // Any songs with rogue endings would've been logged, so fix if needed
        mainActivityInterface.getLoadSong().fixSongs();
        return returnString.toString();
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

    private Song tryToFixSong(Song thisSong, Uri uri) {
        if (uri != null) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            if (isChordPro(thisSong.getFilename())) {
                thisSong.setFiletype("CHO");
                // This is a chordpro file
                // Load the current text contents
                try {
                    String filecontents = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    thisSong.setLyrics(filecontents);
                    thisSong = mainActivityInterface.getConvertChoPro().convertTextToTags(uri, thisSong);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (thisSong.getFilename().toLowerCase(Locale.ROOT).endsWith(".onsong")) {
                try {
                    thisSong.setFiletype("iOS");
                    String filecontents = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    thisSong.setLyrics(filecontents);
                    thisSong = mainActivityInterface.getConvertOnSong().convertTextToTags(uri, thisSong);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (mainActivityInterface.getStorageAccess().isTextFile(uri)) {
                try {
                    thisSong.setFiletype("TXT");
                    String filecontents = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    thisSong.setTitle(thisSong.getFilename());
                    thisSong.setAuthor("");
                    thisSong.setCopyright("");
                    thisSong.setKey("");
                    thisSong.setTimesig("");
                    thisSong.setCcli("");
                    thisSong.setLyrics(mainActivityInterface.getConvertTextSong().convertText(filecontents));


                } catch (Exception e) {
                    thisSong.setTitle(thisSong.getFilename());
                    thisSong.setAuthor("");
                    thisSong.setCopyright("");
                    thisSong.setKey("");
                    thisSong.setTimesig("");
                    thisSong.setCcli("");
                    thisSong.setLyrics("");

                }
            } else {
                thisSong.setTitle(thisSong.getFilename());
                thisSong.setAuthor("");
                thisSong.setCopyright("");
                thisSong.setKey("");
                thisSong.setTimesig("");
                thisSong.setCcli("");
                thisSong.setLyrics("");
            }
            // Just in case there was an error, clear the inputStream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return thisSong;
    }
}
