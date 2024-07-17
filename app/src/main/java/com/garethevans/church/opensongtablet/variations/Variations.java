package com.garethevans.church.opensongtablet.variations;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Variations {

    // This class handles all of the logic of variation files (and temporary key variations)

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "Variations";
    private Context c;
    private final MainActivityInterface mainActivityInterface;

    // These strings are held here, but get updated on resume from MainActivity in case of language change
    private final String variationFolder = "../Variations/";
    private final String folderVariations = "Variations";
    private String niceVariation = "";
    private final String underscorereplacement = "-u-";
    private final String keyTextInFilename = "_K-";
    private final String cache = "_cache";
    private final String customLocStart = "**";
    private final String customLocBasic = "../";
    @SuppressWarnings("FieldCanBeLocal")
    private final String keyStart = "_***", keyEnd = "***_";

    // Set up the helper class
    public Variations(Context c) {
        this.mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
        updateStrings(c);
    }
    public void updateStrings(Context c) {
        this.c = c;
        niceVariation = c.getString(R.string.variation);
    }

    // Checks if this is a normal variation (optional methods)
    public boolean getIsNormalVariation(Song song) {
        return getIsNormalVariation(song.getFolder(),song.getFilename());
    }
    public boolean getIsNormalVariation(String folder, String filename) {
        return (folder.contains(customLocStart) || folder.contains(customLocBasic)) &&
                (folder.contains(niceVariation) || folder.contains("Variation")) &&
                !folder.contains("_cache") &&
                filename.contains("_");
    }

    // Checks if this is a key variation (optional methods)
    public boolean getIsKeyVariation(Song song) {
        return getIsKeyVariation(song.getFolder(),song.getFilename());
    }
    public boolean getIsKeyVariation(String folder, String filename) {
        return (folder.contains(customLocStart) || folder.contains(customLocBasic)) &&
                (folder.contains(niceVariation) || folder.contains("Variation")) &&
                folder.contains("_cache") &&
                filename.contains("_") &&
                filename.contains(keyTextInFilename);
    }

    // Check if either a normal or key variation (optional methods)
    public boolean getIsNormalOrKeyVariation(Song song) {
        return getIsNormalOrKeyVariation(song.getFolder(),song.getFilename());
    }
    public boolean getIsNormalOrKeyVariation(String folder, String filename) {
        return (folder.contains(customLocStart) || folder.contains(customLocBasic)) &&
                (folder.contains(niceVariation) || folder.contains("Variation")) &&
                filename.contains("_");
    }


    // Get the original folder and filename of any variation file
    public String[] getPreVariationInfo(Song song) {
        return getPreVariationInfo(song.getFolder(),song.getFilename(),song.getKey());
    }
    public String[] getPreVariationInfo(SetItemInfo setItemInfo) {
        return getPreVariationInfo(setItemInfo.songfolder,setItemInfo.songfilename,setItemInfo.songkey);
    }
    public String[] getPreVariationInfo(String foldertocheck, String filenametocheck, @Nullable String key) {
        String folder = foldertocheck;
        String filename = filenametocheck;

        // If this is a variation, then proceed with extraction
        if (getIsNormalOrKeyVariation(foldertocheck,filenametocheck)) {

            // First remove the key part completely
            if (filename.contains(keyTextInFilename)) {
                key = filename.substring(filename.lastIndexOf(keyTextInFilename)).replace(keyTextInFilename,"");
                filename = filename.substring(0, filename.indexOf(keyTextInFilename));
            }
            // Now get the folder information
            if (filename.contains("_")) {
                folder = filename.substring(0, filename.lastIndexOf("_"));
                filename = filename.replace(folder + "_", "");
                folder = folder.replace("_", "/");
            } else {
                folder = mainActivityInterface.getMainfoldername();
            }
        } else {
            if (key == null) {
                key = "";
            }
        }
        folder = folder.replace(underscorereplacement, "_");
        filename = filename.replace(underscorereplacement, "_");

        return new String[]{folder,filename,key};
    }


    // Get the new variation filename
    public String getKeyVariationFilename(String folder, String filename, String key) {
        // Replace the folder bits as required
        if (folder != null && (folder.contains(customLocStart) || folder.contains(customLocBasic))) {
            folder = "";
        }

        // If the filename already has underscores in it, but isn't a key variation name, make the underscores safe
        if (filename != null && !filename.isEmpty() && !filename.contains(keyTextInFilename)) {
            filename = filename.replace("_", underscorereplacement);
        }

        // If there is already key information in the filename, strip it out.
        if (filename != null && filename.contains(keyTextInFilename)) {
            filename = filename.substring(0, filename.lastIndexOf(keyTextInFilename));
        }

        if (folder != null && !folder.isEmpty()) {
            folder = folder.replace("/_cache", "XCACHEX");
            folder = folder.replace("_", underscorereplacement);
            folder = folder.replace("XCACHEX", "/_cache");
            folder = folder + "_";
        }

        return (folder + filename + keyTextInFilename + key)
                .replace("//", "/")
                .replace("/", "_");
    }

    public Uri getKeyVariationUri(String variationFilename) {
        return mainActivityInterface.getStorageAccess().getUriForItem(folderVariations, cache, variationFilename);
    }

    // Make a custom slide xml from the variotion song
    public StringBuilder buildVariation(Song tempSong) {
        StringBuilder sb = new StringBuilder();

        // The variation is loaded to a new, temp song object
        // The entire song is copied to the notes as an encrypted string, and a simplified version is copied to the text

        String slide_lyrics = tempSong.getLyrics();
        try {
            byte[] data = mainActivityInterface.getProcessSong().getXML(tempSong).getBytes(tempSong.getEncoding());
            slide_lyrics = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prepare the slide contents so it remains compatible with the desktop app
        // Split the lyrics into individual lines
        String[] lyrics_lines = tempSong.getLyrics().split("\n");
        StringBuilder currentslide = new StringBuilder();
        ArrayList<String> newslides = new ArrayList<>();

        for (String thisline:lyrics_lines) {
            if (!thisline.isEmpty() && !thisline.startsWith(".") && !thisline.startsWith("[") && !thisline.startsWith(";")) {
                // Add the current line into the new slide
                // Replace any new line codes | with \n
                thisline = thisline.replace("||","\n");
                thisline = thisline.replace("---","\n");
                thisline = thisline.replace("|","\n");
                currentslide.append(thisline.trim()).append("\n");
            } else if (thisline.startsWith("[")) {
                // Save the current slide and create a new one
                currentslide = new StringBuilder(currentslide.toString().trim());
                newslides.add(currentslide.toString());
                currentslide = new StringBuilder();
            }
        }
        // Check the filename has a folder, but if not, add one
        if (!tempSong.getFilename().contains("_")) {
            tempSong.setFilename(mainActivityInterface.getMainfoldername()+"_"+tempSong.getFilename());
        }
        tempSong.setTitle(tempSong.getFilename());
        newslides.add(currentslide.toString());
        // Now go back through the currentslides and write the slide text
        StringBuilder slidetexttowrite = new StringBuilder();
        for (int z=0; z<newslides.size();z++) {
            if (!newslides.get(z).isEmpty()) {
                slidetexttowrite.append("      <slide>\n")
                        .append("        ")
                        .append(mainActivityInterface.getSetActions().emptyTagCheck("body",newslides.get(z).trim()))
                        .append("\n")
                        .append("      </slide>\n");
            }
        }

        sb.append("  <slide_group name=\"# ")
                .append(mainActivityInterface.getProcessSong().parseToHTMLEntities(c.getString(R.string.variation)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\"")
                .append(" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\" prefKey=\"")
                .append(tempSong.getKey())
                .append("\">\n")
                .append("    ")
                .append(mainActivityInterface.getSetActions().emptyTagCheck("title",tempSong.getTitle()))
                .append("\n    ")
                .append(mainActivityInterface.getSetActions().emptyTagCheck("subtitle",tempSong.getAuthor()))
                .append("\n    ")
                .append(mainActivityInterface.getSetActions().emptyTagCheck("notes",slide_lyrics))
                .append("\n    <slides>\n")
                .append(slidetexttowrite)
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }


    // Make the variation file from the set item position
    public void makeVariation(int position) {
        // Takes the chosen song and copies it to a temporary file in the Variations folder
        // If the key is different to the song file key, it will be transposed
        // This also updates the set menu to point to the new temporary item
        // This allows the user to freely edit the variation object
        // This is also called after an item is clicked in a set with a different key to the default
        // If this happens, the variation is transposed by the required amount, but is a temporary variation
        // Temporary variations keep the original song folder in the currentSet.getFolder() variable, but change in the song.getFolder()

        // The set may have been edited and then the user clicks on a song, so save the set to preferences first
        mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getCurrentSet().getSetCurrent());

        // Get the uri of the original file (if it exists)
        // We receive a song object as it isn't necessarily the one loaded to MainActivity
        // Get the current set item
        SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
        String existingFolder = setItemInfo.songfolder;
        String newFilename = setItemInfo.songfilename;

        // Get the original file uri
        Uri uriOriginal = mainActivityInterface.getStorageAccess().getUriForItem("Songs", existingFolder, newFilename);

        // IV - When a received song - use the stored received song filename
        if (setItemInfo.songfilename.equals("ReceivedSong")) {
            setItemInfo.songfilename = mainActivityInterface.getNearbyConnections().getReceivedSongFilename();
        }

        // Build a new filename based on the folder name
        if (existingFolder!=null && !existingFolder.isEmpty() && !existingFolder.contains(customLocStart)) {
            existingFolder = existingFolder.replace("/","_");
            newFilename = (existingFolder + "_" + newFilename).replace("__","_");
        } else if (existingFolder!=null && !existingFolder.contains(customLocStart)){
            newFilename = mainActivityInterface.getMainfoldername() + "_" + newFilename;
        }
        newFilename = newFilename.replace("__","_");

        setItemInfo.songfolder = customLocStart + niceVariation;
        setItemInfo.songfoldernice = customLocStart + niceVariation;
        setItemInfo.songfilename = newFilename;

        // Fix the item in the set
        mainActivityInterface.getCurrentSet().setSetItemInfo(position,setItemInfo);

        // Get the uri of the new variation file (Variations/filename)
        Uri uriVariation = mainActivityInterface.getStorageAccess().getUriForItem(folderVariations, "", newFilename);

        // As long as the original and target uris are different, do the copy
        if (!uriOriginal.equals(uriVariation)) {
            // Make sure there is a file to write the output to (remove any existing first)
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeVariation Create Variations/"+newFilename+" deleteOld=true");
            mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, uriVariation, null, folderVariations, "", newFilename);

            // Get an input/output stream reference and copy (streams are closed in copyFile())
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uriOriginal);
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uriVariation);
            mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" makeVariation copyFile from "+uriOriginal+" to Variations/"+setItemInfo.songfilename);
            boolean success = mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
            Log.d(TAG,"Variation created:"+success);
        }
    }

    // Make the key variation file.  This is done if the file doesn't exist, or has been edited
    public Song makeKeyVariation(Song songToTranspose, String setKey, boolean requireUpdate, boolean createTheSong) {
        String originalKey = songToTranspose.getKey();
        // Transpose the lyrics in the song file
        // Get the number of transpose times
        int transposeTimes = mainActivityInterface.getTranspose().getTransposeTimes(songToTranspose.getKey(), setKey);

        // Why transpose up 11 times, when you can just transpose down once.
        // Giving the option as it makes it easier for the user to select new key
        if (transposeTimes > 6) {
            // 7>-5  8>-4 9>-3 10>-2 11>-1 12>0
            transposeTimes = transposeTimes - 12;
        } else if (transposeTimes < -6) {
            // -7>5 -8>4 -9>3 -10>2 -11>1 -12>0
            transposeTimes = 12 + transposeTimes;
        }

        // Get the transpose direction
        String transposeDirection;
        if (transposeTimes >= 0) {
            transposeDirection = "+1";
        } else {
            transposeDirection = "-1";
        }

        transposeTimes = Math.abs(transposeTimes);

        songToTranspose.setKey(originalKey); // This will be transposed in the following...

        songToTranspose.setLyrics(mainActivityInterface.getTranspose().doTranspose(songToTranspose,
                transposeDirection, transposeTimes, songToTranspose.getDetectedChordFormat(),
                songToTranspose.getDesiredChordFormat()).getLyrics());

        // Get the song XML
        String songXML = mainActivityInterface.getProcessSong().getXML(songToTranspose);

        if (createTheSong) {
            // Now we need to write the transposed file
            // If this is a standard variation (not a key variation)
            String targetFilename = getKeyVariationFilename(songToTranspose.getFolder(), songToTranspose.getFilename(), setKey);
            Uri targetUri = getKeyVariationUri(targetFilename);

            if (!mainActivityInterface.getStorageAccess().uriExists(targetUri) || requireUpdate) {
                // We need to create the new key variation file for writing to
                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,
                        targetUri, null, folderVariations,
                        cache, targetFilename);
                requireUpdate = true;
            }

            if (targetUri != null && requireUpdate) {
                // Write the transposed file
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(targetUri);
                mainActivityInterface.getStorageAccess().writeFileFromString(songXML, outputStream);
            }
        }
        return songToTranspose;
    }

    public String getKeyVariationsFolder() {
        return variationFolder + cache;
    }

    public String getVariationsFolder() {
        return variationFolder;
    }

    public String getVariationNice() {
        return customLocStart + niceVariation;
    }

    public String getKeyStart() {
        return keyStart;
    }
    public String getKeyEnd() {
        return keyEnd;
    }

}
