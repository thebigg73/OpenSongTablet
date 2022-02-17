package com.garethevans.church.opensongtablet.export;

// This class prepares all of the different export formats for the app and returns them

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PrepareFormats {

    // When a user asks for a song to be exported, it is first copied to the Exports folder in the folder_filename format
    public ArrayList<Uri> makeSongExportCopies(Context c, MainActivityInterface mainActivityInterface, String folder, String filename,
                                               boolean desktop, boolean ost, boolean txt, boolean chopro, boolean onsong) {
        ArrayList<Uri> uris = new ArrayList<>();

        Song thisSongSQL;
        if (folder.startsWith("../Variations")) {
            thisSongSQL = new Song();
            thisSongSQL.setFolder(folder);
            thisSongSQL.setFilename(filename);
            thisSongSQL = mainActivityInterface.getLoadSong().doLoadSongFile(c,mainActivityInterface, thisSongSQL,false);
        } else {
            thisSongSQL = mainActivityInterface.getSQLiteHelper().getSpecificSong(c, mainActivityInterface, folder, filename);
        }

        String newFilename = folder.replace("/","_");
        newFilename = newFilename.replace("..","");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + filename;

        if (desktop) {
            uris.add(doMakeCopy(c, mainActivityInterface, folder, filename, newFilename));
        }
        if (ost) {
            uris.add(doMakeCopy(c,mainActivityInterface,folder,filename,newFilename+".ost"));
        }
        if (txt && thisSongSQL !=null) {
            String text = getSongAsText(thisSongSQL);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile(c,mainActivityInterface,"Export","",newFilename+".txt",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Export","",newFilename+".txt"));
            }
        }
        if (chopro && thisSongSQL !=null) {
            String text = getSongAsChoPro(c,mainActivityInterface, thisSongSQL);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile(c,mainActivityInterface,"Export","",newFilename+".chopro",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Export","",newFilename+".chopro"));
            }
        }
        if (onsong && thisSongSQL !=null) {
            String text = getSongAsOnSong(c,mainActivityInterface, thisSongSQL);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile(c,mainActivityInterface,"Export","",newFilename+".onsong",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Export","",newFilename+".onsong"));
            }
        }

        return uris;
    }
    private Uri doMakeCopy(Context c, MainActivityInterface mainActivityInterface, String currentFolder, String currentFilename, String newFilename) {
        Uri targetFile = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Songs",currentFolder,currentFilename);
        Uri destinationFile = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Export","",newFilename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,mainActivityInterface,true,destinationFile,null,"Export","",newFilename);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c,targetFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c,destinationFile);
        if (mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream)) {
            return destinationFile;
        } else {
            return null;
        }
    }

    public String getSongAsText(Song thisSong) {

        String string = replaceNulls("", "\n", thisSong.getTitle()) +
                replaceNulls("", "\n", thisSong.getAuthor()) +
                replaceNulls("Key: ", "\n", thisSong.getKey()) +
                replaceNulls("Tempo: ", "\n", thisSong.getTempo()) +
                replaceNulls("Time signature: ", "\n", thisSong.getTimesig()) +
                replaceNulls("Copyright: ", "\n", thisSong.getCopyright()) +
                replaceNulls("CCLI: ", "\n", thisSong.getCcli()) +
                "\n\n" +
                replaceNulls("\n", "", thisSong.getLyrics());
        string = string.replace("\n\n\n", "\n\n");

        // IV - remove empty comments
        string = string.replaceAll("\\Q{c:}\\E\n", "");

        return string;
    }
    public String getSongAsChoPro(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // This converts an OpenSong file into a ChordPro file

        String string = "{new_song}\n" + replaceNulls("{title:", "}\n", thisSong.getTitle()) +
                replaceNulls("{artist:", "}\n", thisSong.getAuthor()) +
                replaceNulls("{key:", "}\n", thisSong.getKey()) +
                replaceNulls("{tempo:", "}\n", thisSong.getTempo()) +
                replaceNulls("{time:", "}\n", thisSong.getTimesig()) +
                replaceNulls("{copyright:", "}\n", thisSong.getCopyright()) +
                replaceNulls("{ccli:", "}\n", thisSong.getCcli()) +
                "\n\n" +
                mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(c, mainActivityInterface, replaceNulls("\n", "", thisSong.getLyrics()));
        string = string.replace("\n\n\n", "\n\n");

        // IV - remove empty comments
        string = string.replaceAll("\\Q{c:}\\E\n", "");

        return string;
    }

    public Uri getSongAsImage(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        String newFilename = thisSong.getFolder().replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + thisSong.getFilename() + ".png";
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Export","",newFilename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(c,mainActivityInterface,true, uri, "application/pdf","Export","",newFilename);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c,uri);
        mainActivityInterface.getStorageAccess().writeImage(outputStream,mainActivityInterface.getScreenshot());
        return uri;
    }

    public String getSongAsOnSong(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // This converts an OpenSong file into a OnSong file

        String string = replaceNulls("", "\n", thisSong.getTitle()) +
                replaceNulls("", "\n", thisSong.getAuthor()) +
                replaceNulls("Key: ", "\n", thisSong.getKey()) +
                replaceNulls("Tempo: ", "\n", thisSong.getTempo()) +
                replaceNulls("Time signature: ", "\n", thisSong.getTimesig()) +
                replaceNulls("Copyright: ", "\n", thisSong.getCopyright()) +
                replaceNulls("CCLI: ", "\n", thisSong.getCcli()) +
                "\n\n" +
                mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(c, mainActivityInterface, replaceNulls("\n", "", thisSong.getLyrics()));
        string = string.replace("\n\n\n", "\n\n");

        // IV - remove empty comments
        string = string.replaceAll("\\Q{c:}\\E\n", "");

        return string;
    }
    private String replaceNulls(String start, String end, String s) {
        if (s==null) {
            return "";
        } else {
            return start+s+end;
        }
    }

}
