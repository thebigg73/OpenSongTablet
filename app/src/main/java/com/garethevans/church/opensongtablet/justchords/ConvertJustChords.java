package com.garethevans.church.opensongtablet.justchords;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.drummer.DrumCalculations;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConvertJustChords {

    // This is used to convert between OpenSong and JustChords format
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ConvertJustSong", set_string;
    private final String extension = ".justchords";
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<Song> songs;
    private final String abc_start = "{start_of_abc}", abc_end = "{end_of_abc}";
    private final String abc_start_alt1 = "{start_of_abcnotation}", abc_end_alt1 = "{end_of_abcnotation}";
    private final String abc_start_alt2 = "{start_of_abctablature}", abc_end_alt2 = "{end_of_abctablature}";

    // Instatiate the class
    public ConvertJustChords(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        set_string = c.getString(R.string.set);
        resetVariables();
    }

    // Reset the songs arrraylist
    public void resetVariables() {
        if (songs==null) {
            songs = new ArrayList<>();
        } else {
            songs.clear();
        }
    }

    // Called from ExportFragment - Create a single justchords song file
    public Uri buildJustChordsSingleSong(Song thisSong) {
        Uri uri = null;
        if (thisSong!=null) {
            JustChordsSongObject justChordsSongObject = buildJustChordsSongObject(thisSong);
            JustChordsObject justChordsObject = buildJustChordsObject(new JustChordsSongObject[]{justChordsSongObject});
            String justChordsObjectString = getJustChordsObjectString(justChordsObject);
            uri = saveTheJustChordsObject(thisSong.getFilename(), justChordsObjectString);
        }
        return uri;
    }

    public String getJustChordsLyrics(Song thisSong) {
        String lyrics = "";
        if (thisSong!=null) {
            JustChordsSongObject justChordsSongObject = buildJustChordsSongObject(thisSong);
            lyrics = justChordsSongObject.rawData;
        }
        return lyrics;
    }

    // Called from ExportFragment - Add a song to the songs array in order to build a justchords set file
    public void addOpenSongToArray(Song thisSong) {
        if (songs==null) {
            resetVariables();
        }
        if (thisSong!=null) {
            songs.add(thisSong);
        }
    }

    // Called from ExportFragment - Using the songs array, build a justchords set file
    public Uri buildJustChordsSet(String setname) {
        Uri uri = null;
        // Use the songs array
        if (songs!=null && !songs.isEmpty()) {
            JustChordsSongObject[] justChordsSongObjects = new JustChordsSongObject[songs.size()];
            for (int i=0; i<songs.size(); i++) {
                justChordsSongObjects[i] = buildJustChordsSongObject(songs.get(i));
                // Clear the song from the array to minimise memory leaking
                songs.set(i,null);
            }
            JustChordsObject justChordsObject = buildJustChordsObject(justChordsSongObjects);
            String justChordsObjectString = getJustChordsObjectString(justChordsObject);
            uri = saveTheJustChordsObject(setname + " ("+set_string+")", justChordsObjectString);
        }
        return uri;
    }

    // Deal with producing JustChords files
    // Convert OpenSong formatted song to a JustChordsSongObject
    private JustChordsSongObject buildJustChordsSongObject(Song thisSong) {
        // Create the song object
        JustChordsSongObject justChordsSongObject = new JustChordsSongObject();

        // UUID - a random UUID
        String songuuid = UUID.randomUUID().toString().toUpperCase();

        // Add the compatible values
        justChordsSongObject.setTitle(thisSong.getTitle());
        justChordsSongObject.setArtist(thisSong.getAuthor());
        if (thisSong.getAutoscrolllength()!=null && !thisSong.getAutoscrolllength().isEmpty()) {
            try {
                justChordsSongObject.setDuration(mainActivityInterface.getTimeTools().timeFormatFixer(Integer.parseInt(thisSong.getAutoscrolllength())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        justChordsSongObject.setTempo(DrumCalculations.getFixedTempoString(thisSong.getTempo(),false));
        justChordsSongObject.setTimeSignature(DrumCalculations.getFixedTimeSignatureString(thisSong.getTimesig(),false));
        justChordsSongObject.setNotes(thisSong.getNotes());
        justChordsSongObject.setCopyright(thisSong.getCopyright());
        //justChordsSongObject.setTimeSignature(mainActivityInterface.getDrumViewModel().fixInvalidTimeSignature(thisSong.getTimesig(),true));
        justChordsSongObject.setCcli(thisSong.getCcli());
        justChordsSongObject.setId(songuuid);

        // Add the incompatible, but required values
        justChordsSongObject.setChordBeatsRaw(null);

        Map<String,Object> keyChord = new HashMap<>();
        // The key needs to be split into 2 parts, key and minor
    if (thisSong.getKey()!=null && thisSong.getKey().contains("m")) {
            keyChord.put("key", thisSong.getKey().replace("m",""));
            keyChord.put("minor", true);
        } else {
            keyChord.put("key", thisSong.getKey());
            keyChord.put("minor", false);
        }
        justChordsSongObject.setKeyChord(keyChord);

        // Build the lyrics!
        String alllyrics = mainActivityInterface.getProcessSong().parseLyrics(mainActivityInterface.getLocale(),thisSong,false);
        // If there is a popup abc code, insert that too as part of the lyrics
        if (thisSong.getAbc()!=null && !thisSong.getAbc().isEmpty()) {
            alllyrics = abc_start + "\n" + thisSong.getAbc().replaceAll("\\n","\n") + "\n" + abc_end + "\n" + alllyrics;
        }
        alllyrics = alllyrics.replace("||","");
        String[] lyriclines = alllyrics.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line: lyriclines) {
            // Replace the heading lines with lines that end with :
            if (line.startsWith("[")) {
                line = line.replace("[","").replace("]","");
                line = mainActivityInterface.getProcessSong().beautifyHeading(line);
                line = line + ":";
            }
            if (line.trim().startsWith(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator())) {
                // This is inline abc notation, so convert that to abc notation
                line = line.trim().replace(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator(),"");
                line = abc_start + "\n" + line.replace("\\n","\n") + "\n" + abc_end;
            }
            stringBuilder.append(line).append("\n");
        }

        // Convert the lines to ChordPro format
        justChordsSongObject.setRawData(mainActivityInterface.getConvertChoPro().fromOpenSongToChordPro(stringBuilder.toString()));

        return justChordsSongObject;
    }

    // Build the JustChordsObject (the entire file) from the JustChordsSongObject
    private JustChordsObject buildJustChordsObject(JustChordsSongObject[] justChordsSongObjects) {
        // setUUID - a random UUID
        String setuuid = UUID.randomUUID().toString().toUpperCase();
        JustChordsObject justChordsObject = new JustChordsObject();
        justChordsObject.setPlaylists(null);
        justChordsObject.setDataVersion(setuuid);
        justChordsObject.setIdentity(setuuid);
        justChordsObject.setSongs(justChordsSongObjects);
        justChordsObject.setTags(null);
        return justChordsObject;
    }

    // Get a string representation for the JSON file of the JustChordsObject
    private String getJustChordsObjectString(JustChordsObject justChordsObject) {
        Gson gson = new Gson();
        return gson.toJson(justChordsObject);
    }

    // Save the justChordsObject as a string using Gson and return the uri location
    private Uri saveTheJustChordsObject(String filename, String content) {
        // Get a Gson version of the justChordsObject
        Uri uri = null;
        if (filename!=null && content!=null && !content.isEmpty()) {
            try {
                mainActivityInterface.getStorageAccess().writeFileFromString("Export","",filename+".justchords",content);
                uri = mainActivityInterface.getStorageAccess().getUriForItem("Export","",filename+".justchords");
                //mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true,uri,null,"Export","",filename+".justchords");
                //OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                //mainActivityInterface.getStorageAccess().writeFileFromString(content, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    // Deal with parsing a JustChords file
    public JustChordsObject getJustChordsObjectFromImportUri() {
        return getJustChordsObjectFromUri(mainActivityInterface.getImportUri());
    }

    public JustChordsObject getJustChordsObjectFromUri(Uri uri) {
        // Get the uri into a string
        if (uri!=null) {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            String content = "";
            if (inputStream!=null) {
                content = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
            }

            // Update the justChordsObject
            Gson gson = new Gson();
            return gson.fromJson(content, JustChordsObject.class);
        } else {
            return new JustChordsObject();
        }
    }

    public JustChordsSongObject getJustChordsSongObject(JustChordsObject justChordsObject, int itemNumber) {
        if (justChordsObject!=null && justChordsObject.getSongs()!=null && justChordsObject.getSongs().length>itemNumber) {
            return justChordsObject.getSongs()[itemNumber];
        } else {
          return null;
        }
    }

    @SuppressWarnings("IOStreamConstructor")
    public void createSongsFromImportedSet() {
        // Get the JustChordsObject
        JustChordsObject justChordsObject = getJustChordsObjectFromImportUri();
        // Get the songs objects
        if (justChordsObject!=null) {
            JustChordsSongObject[] justChordsSongObjects = justChordsObject.getSongs();
            if (justChordsSongObjects!=null && justChordsSongObjects.length>0) {
                // Clear the temp folder of old files
                File extractFolder = mainActivityInterface.getStorageAccess().getAppSpecificFile("SetBundle","justChords","");
                mainActivityInterface.getStorageAccess().emptyFileFolder(extractFolder);
                // Now go through each object and create an OpenSong song
                for (JustChordsSongObject justChordsSongObject: justChordsSongObjects) {
                    Song song = getOpenSongFromJustChordsSong(justChordsSongObject);
                    File tempFile = new File(extractFolder,song.getTitle());
                    try {
                        mainActivityInterface.getStorageAccess().writeFileFromString(song.getSongXML(), new FileOutputStream(tempFile));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mainActivityInterface.getCurrentSet().addItemToSet(song);
                }
                // Get a setXML file
                mainActivityInterface.getCurrentSet().setSetCurrentLastName(mainActivityInterface.getImportFilename().replace(extension,""));

                mainActivityInterface.getSetActions().setUseThisLastModifiedDate(mainActivityInterface.getTimeTools().getNowIsoTime());
                String setXML = mainActivityInterface.getSetActions().createSetXML(mainActivityInterface.getCurrentSet());
                mainActivityInterface.getSetActions().setUseThisLastModifiedDate(null);

                // Save the set file
                File setFile = new File(extractFolder,mainActivityInterface.getImportFilename().replace(extension,"")+".osts");
                try {
                    mainActivityInterface.getStorageAccess().writeFileFromString(setXML, new FileOutputStream(setFile));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Clear the currentSet as we populate in the import page
                mainActivityInterface.getSetActions().clearCurrentSet();
            }
        }
    }

    public Song getOpenSongFromJustChordsSong(JustChordsSongObject justChordsSongObject) {
        Song song = new Song();
        song.setTitle(justChordsSongObject.getTitle());
        song.setFilename(justChordsSongObject.getTitle());
        song.setFolder(mainActivityInterface.getMainfoldername());
        song.setAuthor(justChordsSongObject.getArtist());
        song.setAutoscrolllength(String.valueOf(mainActivityInterface.getTimeTools().getTotalSecsFromColonTimes(justChordsSongObject.getDuration())));
        song.setTempo(justChordsSongObject.getTempo());
        song.setUuid(justChordsSongObject.getId());
        song.setNotes(justChordsSongObject.getNotes());
        song.setCopyright(justChordsSongObject.getCopyright());
        song.setTimesig(justChordsSongObject.getTimeSignature());
        song.setCcli(justChordsSongObject.getCcli());
        Map<String,Object> keyChord = justChordsSongObject.getKeyChord();
        String key = "";
        String minor = "";
        if (keyChord!=null) {
            Object keyObject;
            Object minorObject;
            if (keyChord.containsKey("key")) {
                keyObject = keyChord.get("key");
                if (keyObject != null) {
                    key = keyObject.toString();
                }
            }
            if (keyChord.containsKey("minor")) {
                minorObject = keyChord.get("minor");
                if (minorObject != null && (boolean)minorObject) {
                    minor = "m";
                }
            }
        }
        song.setKey(key + minor);
        song.setLyrics(getOpenSongLyrics(justChordsSongObject.getRawData()));
        String songXML = mainActivityInterface.getProcessSong().getXML(song);
        song.setSongXML(songXML);
        return song;
    }

    public String getOpenSongLyrics(String justChordLyrics) {
        String lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(justChordLyrics);
        // Go through each line and replace headings with []
        // Get abc notation lines into inlineABC for OpenSongApp
        boolean containsAbc = lyrics.contains(abc_start) && lyrics.contains(abc_end);
        while (containsAbc) {
            int indexStart = lyrics.indexOf(abc_start);
            int indexEnd = lyrics.indexOf(abc_end,indexStart);
            containsAbc = indexStart>=0 && indexEnd>=0 && indexEnd>indexStart;
            if (containsAbc) {
                String extractedBit = getExtractedBit(lyrics, indexStart, indexEnd, abc_end.length());
                // Now put it back in place of the old bit
                lyrics = lyrics.substring(0, indexStart) + extractedBit + "\n" + lyrics.substring(indexEnd + abc_end.length());
            }
        }

        // Repeat for older abc identifiers
        boolean containsAbcAlt1 = lyrics.contains(abc_start_alt1) && lyrics.contains(abc_end_alt1);
        while (containsAbcAlt1) {
            int indexStart = lyrics.indexOf(abc_start_alt1);
            int indexEnd = lyrics.indexOf(abc_end_alt1,indexStart);
            containsAbcAlt1 = indexStart>=0 && indexEnd>=0 && indexEnd>indexStart;
            if (containsAbcAlt1) {
                String extractedBit = getExtractedBit(lyrics, indexStart, indexEnd, abc_end_alt1.length());
                // Now put it back in place of the old bit
                lyrics = lyrics.substring(0, indexStart) + extractedBit + "\n" + lyrics.substring(indexEnd + abc_end_alt1.length());
            }
        }

        // Repeat for older abc identifiers
        boolean containsAbcAlt2 = lyrics.contains(abc_start_alt2) && lyrics.contains(abc_end_alt2);
        while (containsAbcAlt2) {
            int indexStart = lyrics.indexOf(abc_start_alt2);
            int indexEnd = lyrics.indexOf(abc_end_alt2,indexStart);
            containsAbcAlt2 = indexStart>=0 && indexEnd>=0 && indexEnd>indexStart;
            if (containsAbcAlt2) {
                String extractedBit = getExtractedBit(lyrics, indexStart, indexEnd, abc_end_alt2.length());
                // Now put it back in place of the old bit
                lyrics = lyrics.substring(0, indexStart) + extractedBit + "\n" + lyrics.substring(indexEnd + abc_end_alt2.length());
            }
        }

        String[] lyriclines = lyrics.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (String line: lyriclines) {
            // Replace the heading lines with lines that end with :
            if (line.endsWith(":")) {
                line = "[" + line.trim() + "]";
                line = line.replace(":]", "]");
            }
            // Make sure inline abc starts at the beginning of the line without a space
            if (line.trim().startsWith(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator())) {
                line = line.trim();
                line = line.replace(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator()+" ",
                        mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator());
            }

            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private String getExtractedBit(String lyrics, int indexStart, int indexEnd, int abc_end_length) {
        String extractedBit = lyrics.substring(indexStart, indexEnd + abc_end_length);
        // Process the extractedBit
        extractedBit = extractedBit.replace(abc_start, "").replace(abc_end, "");
        extractedBit = extractedBit.replace(abc_start_alt1, "").replace(abc_end_alt1, "");
        extractedBit = extractedBit.replace(abc_start_alt2, "").replace(abc_end_alt2, "");
        extractedBit = extractedBit.replace("\n", "\\n");
        extractedBit = extractedBit.replace("\\n ", "\\n");
        extractedBit = extractedBit.replace(" \\n", "\\n");
        extractedBit = extractedBit.replace("\\n\\n","\\n");
        extractedBit = mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator() + extractedBit.trim();
        return extractedBit;
    }

    public String getExtension() {
        return extension;
    }

}
