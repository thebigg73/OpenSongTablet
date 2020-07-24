package com.garethevans.church.opensongtablet.export;

// This class prepares all of the different export formats for the app and returns them

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PrepareFormats {

    SQLite thisSongSQL;

    // When a user asks for a song to be exported, it is first copied to the Exports folder in the folder_filename format
    public ArrayList<Uri> makeSongExportCopies(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong,
                                             SQLiteHelper sqLiteHelper, ConvertChoPro convertChoPro, String folder, String filename,
                                             boolean desktop, boolean ost, boolean txt, boolean chopro, boolean onsong) {
        ArrayList<Uri> uris = new ArrayList<>();
        thisSongSQL = sqLiteHelper.getSpecificSong(c,folder,filename);

        String newFilename = folder.replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + filename;

        if (desktop) {
            uris.add(doMakeCopy(c, preferences, storageAccess, folder, filename, newFilename));
        }
        if (ost) {
            uris.add(doMakeCopy(c,preferences,storageAccess,folder,filename,newFilename+".ost"));
        }
        if (txt && thisSongSQL!=null) {
            String text = getSongAsText(thisSongSQL);
            if (storageAccess.doStringWriteToFile(c,preferences,"Export","",newFilename+".txt",text)) {
                uris.add(storageAccess.getUriForItem(c,preferences,"Export","",newFilename+".txt"));
            }
        }
        if (chopro && thisSongSQL!=null) {
            String text = getSongAsChoPro(c,processSong,thisSongSQL,convertChoPro);
            if (storageAccess.doStringWriteToFile(c,preferences,"Export","",newFilename+".chopro",text)) {
                uris.add(storageAccess.getUriForItem(c,preferences,"Export","",newFilename+".chopro"));
            }
        }
        if (onsong && thisSongSQL!=null) {
            String text = getSongAsOnSong(c,processSong,thisSongSQL,convertChoPro);
            if (storageAccess.doStringWriteToFile(c,preferences,"Export","",newFilename+".onsong",text)) {
                uris.add(storageAccess.getUriForItem(c,preferences,"Export","",newFilename+".onsong"));
            }
        }

        return uris;
    }
    private Uri doMakeCopy(Context c, Preferences preferences, StorageAccess storageAccess, String currentFolder, String currentFilename, String newFilename) {
        Uri targetFile = storageAccess.getUriForItem(c,preferences,"Songs",currentFolder,currentFilename);
        Uri destinationFile = storageAccess.getUriForItem(c,preferences,"Export","",newFilename);
        storageAccess.lollipopCreateFileForOutputStream(c,preferences,destinationFile,null,"Export","",newFilename);
        InputStream inputStream = storageAccess.getInputStream(c,targetFile);
        OutputStream outputStream = storageAccess.getOutputStream(c,destinationFile);
        if (storageAccess.copyFile(inputStream,outputStream)) {
            return destinationFile;
        } else {
            return null;
        }
    }

    private String getSongAsText(SQLite thisSongSQL) {
        String title = replaceNulls(thisSongSQL.getTitle());
        String key = replaceNulls(thisSongSQL.getKey());
        String author = replaceNulls(thisSongSQL.getAuthor());

        if (!key.isEmpty()) {
            key = " (" + key + ")";
        }
        if (!author.isEmpty()) {
            author = "\n" + author + "\n\n";
        } else {
            author = "\n\n";
        }
        return title + key + author + replaceNulls(thisSongSQL.getLyrics());
    }
    private String getSongAsChoPro(Context c, ProcessSong processSong, SQLite thisSongSQL, ConvertChoPro convertChoPro) {
        // This converts an OpenSong file into a ChordPro file
        String string = "{ns}\n" + "{t:" + replaceNulls(thisSongSQL.getTitle()) + "}\n";

        if (thisSongSQL.getAuthor()!=null && !thisSongSQL.getAuthor().isEmpty()) {
            string = string + "{st:" + replaceNulls(thisSongSQL.getAuthor()) + "}\n";
        }
        string = string + "\n" + convertChoPro.fromOpenSongToChordPro(c, processSong, replaceNulls(thisSongSQL.getLyrics()));
        string = string.replace("\n\n\n", "\n\n");
        return string;
    }
    private String getSongAsOnSong(Context c, ProcessSong processSong, SQLite thisSongSQL, ConvertChoPro convertChoPro) {
        // This converts an OpenSong file into a OnSong file
        String string = replaceNulls(thisSongSQL.getTitle()) + "\n";

        if (thisSongSQL.getAuthor()!=null && !thisSongSQL.getAuthor().isEmpty()) {
            string = string + thisSongSQL.getAuthor() + "\n";
        }

        if (thisSongSQL.getCopyright()!=null && !thisSongSQL.getCopyright().isEmpty()) {
            string = string + "Copyright: " + thisSongSQL.getCopyright() + "\n";
        }

        if (thisSongSQL.getKey()!=null && !thisSongSQL.getKey().isEmpty()) {
            string = string + "Key: " + thisSongSQL.getKey() + "\n\n";
        } else {
            string = string + "\n";
        }

        string = string + convertChoPro.fromOpenSongToChordPro(c,processSong,replaceNulls(thisSongSQL.getLyrics()));

        string = string.replace("\n\n\n", "\n\n");
        return string;
    }
    private String replaceNulls(String s) {
        if (s==null) {
            return "";
        } else {
            return s;
        }
    }

}
