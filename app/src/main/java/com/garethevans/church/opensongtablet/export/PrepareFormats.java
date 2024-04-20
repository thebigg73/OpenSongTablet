package com.garethevans.church.opensongtablet.export;

// This class prepares all of the different export formats for the app and returns them

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class PrepareFormats {

    private final String TAG = "PrepareFormats";
    private final MainActivityInterface mainActivityInterface;

    public PrepareFormats(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    // When a user asks for a song to be exported, it is first copied to the Exports folder in the folder_filename format
    public ArrayList<Uri> makeSongExportCopies(String folder, String filename, boolean desktop,
                                               boolean ost, boolean txt, boolean chopro, boolean onsong) {
        ArrayList<Uri> uris = new ArrayList<>();

        Song thisSongSQL;
        if (folder.startsWith("../Variations")) {
            thisSongSQL = new Song();
            thisSongSQL.setFolder(folder);
            thisSongSQL.setFilename(filename);
            thisSongSQL = mainActivityInterface.getLoadSong().doLoadSongFile(thisSongSQL,false);
        } else {
            thisSongSQL = mainActivityInterface.getSQLiteHelper().getSpecificSong(folder, filename);
        }

        String newFilename = folder.replace("/","_");
        newFilename = newFilename.replace("..","");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + filename;

        if (desktop) {
            uris.add(doMakeCopy(folder, filename, newFilename));
        }
        if (ost) {
            uris.add(doMakeCopy(folder,filename,newFilename+".ost"));
        }
        if (txt && thisSongSQL !=null) {
            String text = getSongAsText(thisSongSQL);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeSongExportCopies doStringWriteToFile Export/"+newFilename+".txt with: "+text);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","",newFilename+".txt",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export","",newFilename+".txt"));
            }
        }
        if (chopro && thisSongSQL !=null) {
            String text = getSongAsChoPro(thisSongSQL);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeSongExportCopies doStringWriteToFile Export/"+newFilename+".chopro with: "+text);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","",newFilename+".chopro",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export","",newFilename+".chopro"));
            }
        }
        if (onsong && thisSongSQL !=null) {
            String text = getSongAsOnSong(thisSongSQL);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeSongExportCopies doStringWriteToFile Export/"+newFilename+".onsong with: "+text);
            if (mainActivityInterface.getStorageAccess().doStringWriteToFile("Export","",newFilename+".onsong",text)) {
                uris.add(mainActivityInterface.getStorageAccess().getUriForItem("Export","",newFilename+".onsong"));
            }
        }

        return uris;
    }
    private Uri doMakeCopy(String currentFolder, String currentFilename, String newFilename) {
        Uri targetFile = mainActivityInterface.getStorageAccess().getUriForItem("Songs",currentFolder,currentFilename);
        Uri destinationFile = mainActivityInterface.getStorageAccess().getUriForItem("Export","",newFilename);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Export/"+newFilename+"  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                destinationFile,null,"Export","",newFilename);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(targetFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(destinationFile);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doMakeCopy from Songs/"+currentFolder+"/"+currentFilename+" to Export/" + newFilename);

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
    public String getSongAsChoPro(Song thisSong) {
        // This converts an OpenSong file into a ChordPro file

        String string = "{new_song}\n" + replaceNulls("{title:", "}\n", thisSong.getTitle()) +
                replaceNulls("{artist:", "}\n", thisSong.getAuthor()) +
                replaceNulls("{key:", "}\n", thisSong.getKey()) +
                replaceNulls("{tempo:", "}\n", thisSong.getTempo()) +
                replaceNulls("{time:", "}\n", thisSong.getTimesig()) +
                replaceNulls("{copyright:", "}\n", thisSong.getCopyright()) +
                replaceNulls("{ccli:", "}\n", thisSong.getCcli()) +
                "\n\n" +
                mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(replaceNulls("\n", "", thisSong.getLyrics()));
        string = string.replace("\n\n\n", "\n\n");

        // IV - remove empty comments
        string = string.replaceAll("\\Q{c:}\\E\n", "");

        return string;
    }

    public Uri getSongAsImage(Song thisSong) {
        String newFilename = thisSong.getFolder().replace("/","_");
        if (!newFilename.endsWith("_")) {
            newFilename = newFilename + "_";
        }
        newFilename = newFilename + thisSong.getFilename() + ".png";
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",newFilename);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" Create Export/"+newFilename+"  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                uri, "application/pdf","Export","",newFilename);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" doImport writeImage Export/"+newFilename);
        try (FileInputStream fileInputStream = new FileInputStream(mainActivityInterface.getScreenshotFile())) {
            mainActivityInterface.getStorageAccess().copyFile(fileInputStream,outputStream);
        } catch (Exception e) {
           e.printStackTrace();
        }
        return uri;
    }

    public String getSongAsOnSong(Song thisSong) {
        // This converts an OpenSong file into a OnSong file

        String string = replaceNulls("", "\n", thisSong.getTitle()) +
                replaceNulls("", "\n", thisSong.getAuthor()) +
                replaceNulls("Key: ", "\n", thisSong.getKey()) +
                replaceNulls("Tempo: ", "\n", thisSong.getTempo()) +
                replaceNulls("Time signature: ", "\n", thisSong.getTimesig()) +
                replaceNulls("Copyright: ", "\n", thisSong.getCopyright()) +
                replaceNulls("CCLI: ", "\n", thisSong.getCcli()) +
                "\n\n" +
                mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(replaceNulls("\n", "", thisSong.getLyrics()));
        string = string.replace("\n\n\n", "\n\n");

        // IV - remove empty comments
        string = string.replaceAll("\\Q{c:}\\E\n", "");

        // Do final OnSong conversions
        string = mainActivityInterface.getConvertOnSong().finalFixOnSong(string);
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
