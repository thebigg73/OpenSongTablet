package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class LoadSong {

    private final String TAG = "LoadSong";
    private Uri uri;
    private ArrayList<Song> songsToFix;

    // The song object is sent to get the folder/filename, but it is returned after adding to it
    // Since this could be an indexing action, getting the next key or an actual load, 
    // we just receive the song object (can't assume mainActivityInterface.getSong()
    public Song doLoadSong(Context c, MainActivityInterface mainActivityInterface, Song thisSong, boolean indexing) {
        // If we have finished song indexing, we get the song from the SQL database.
        // Unless, of course it is a custom file.
        // If not, we load up from the xml file
        // We also load from the file if it is a custom file (pdf and images are dealt with separately)

        // Get the song folder/filename from the sent song object
        String folder = thisSong.getFolder();
        String filename = thisSong.getFilename();

        Log.d(TAG,"folder="+folder+"  filename="+filename);

        // Clear the song object then add the folder filename back
        thisSong = new Song();
        thisSong.setFolder(folder);
        thisSong.setFilename(filename);

        // We will add to this song and then return it to the MainActivity object
        if (!mainActivityInterface.getSongListBuildIndex().getIndexComplete() ||
                mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing() ||
                thisSong.getFolder().contains("**")) {
            // This is set to true once the index is completed, so we either haven't finished indexing
            // or this is a custom slide/note as identified by the folder (which aren't indexed)
            return doLoadSongFile(c, mainActivityInterface, thisSong, indexing);
        } else {
            Log.d(TAG, "Loading from the database");
            if (thisSong.getFilename().equals("Welcome to OpenSongApp")) {
                return mainActivityInterface.getSong().showWelcomeSong(c, thisSong);
            } else {
                thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(c, mainActivityInterface,
                        thisSong.getFolder(), thisSong.getFilename());
                sortLoadingSuccessful(c,mainActivityInterface,thisSong);
                return thisSong;
            }
        }
    }


    public Song doLoadSongFile(Context c, MainActivityInterface mainActivityInterface,
                               Song thisSong, boolean indexing) {

        // This extracts what it can from the song, and returning an updated song object.
        // Once indexing has finished, we load from the database instead
        // This is only for indexing and impatient users or if we are loading a PDF/IMG!

        if (thisSong.getFolder() == null || thisSong.getFolder().isEmpty()) {
            thisSong.setFolder(c.getString(R.string.mainfoldername));
        }
        if (thisSong.getFilename() == null || thisSong.getFilename().isEmpty()) {
            thisSong.setFilename("Welcome to OpenSongApp");
        }

        // Set the song load status to false (helps check if it didn't load).  This is set to true after success
        if (!indexing) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, "songLoadSuccess", false);
        }

        // When getting the uri, it runs a check for custom folders **Variations, etc.
        String where = "Songs";

        // Determine the filetype by extension - the best songs are xml (OpenSong formatted).
        thisSong.setFiletype(getFileTypeByExtension(thisSong.getFilename()));

        // Get the uri for the song - we know it exists as we found it!
        uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface,
                where, thisSong.getFolder(), thisSong.getFilename());

        // If this is an image or a PDF (or DOC), we don't load a song object from the file
        // Instead we use the databse, but the user will have to wait!
        if (!thisSong.getFiletype().equals("PDF") && !thisSong.getFiletype().equals("IMG") &&
                !thisSong.getFiletype().equals("DOC")) {

            String utf;
            // Go through our options one at a time
            if (thisSong.getFilename().equals("Welcome to OpenSongApp")) {
                // 1. We are using the default song
                thisSong = thisSong.showWelcomeSong(c, thisSong);
                // Don't update the songLoadSuccess as this isn't what the user really wants

            } else if (thisSong.getFiletype().equals("XML")) {
                // 2. We have an XML file (likely)
                utf = getUTF(c, mainActivityInterface, thisSong.getFolder(),
                        thisSong.getFilename(), thisSong.getFiletype());
                thisSong = readFileAsXML(c, mainActivityInterface, thisSong, where, uri, utf);
            }


            // If the file wasn't read as an xml file and might be text based, we need to deal with it in another way
            // This isn't added as an else statement to the above as one of those methods might have been tried
            // If they find an issue, they send the song back with a better guessed filetype
            if (!thisSong.getFiletype().equals("XML")) {
                // This will try to import text, chordpro or onsong and update the lyrics field
                thisSong.setLyrics(getSongAsText(c, mainActivityInterface, where, thisSong.getFolder(), thisSong.getFilename()));
                thisSong.setTitle(thisSong.getFilename());
                if (thisSong.getLyrics() != null && !thisSong.getLyrics().isEmpty()) {
                    // Success (although we'll maybe process it below)
                    mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, "songLoadSuccess", true);
                }
            }

            if (thisSong.getFiletype().equals("iOS")) {
                // 3.  Run the OnSongConvert script (which converts then resaves)
                mainActivityInterface.getConvertOnSong().convertTextToTags(c, mainActivityInterface, uri, thisSong);

                // Now read in the proper OpenSong xml file
                try {
                    readFileAsXML(c, mainActivityInterface, thisSong, where, uri, "UTF-8");
                } catch (Exception e) {
                    Log.d(TAG, "Error performing grabOpenSongXML()");
                }
            } else if (thisSong.getFiletype().equals("CHO") || lyricsHaveChoProTags(thisSong.getLyrics())) {
                // 4.  Run the ChordProConvert script (which converts then resaves)
                thisSong = mainActivityInterface.getConvertChoPro().convertTextToTags(c, mainActivityInterface, uri, thisSong);

                // Now read in the proper OpenSong xml file
                try {
                    readFileAsXML(c, mainActivityInterface, thisSong, where, uri, "UTF-8");
                } catch (Exception e) {
                    Log.d(TAG, "Error performing grabOpenSongXML()");
                }
            }

            // Fix all the rogue code
            thisSong.setLyrics(mainActivityInterface.getProcessSong().parseLyrics(c, mainActivityInterface.getLocale(), thisSong));

            // Update the songLoadSuccess and references to the working file if it did work if we aren't indexing
            if (!indexing) {
                sortLoadingSuccessful(c,mainActivityInterface,thisSong);
            }
        }
        // Send the song back with all of its children populated!
        return thisSong;
    }

    private void sortLoadingSuccessful(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // Check if the song has been loaded (will now have a lyrics value)
        if (!thisSong.getFilename().equals("Welcome to OpenSongApp") &&
                thisSong.getLyrics() != null && !thisSong.getLyrics().isEmpty()) {
            // Song was loaded correctly and was xml format
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(c, "songLoadSuccess", true);
            mainActivityInterface.getPreferences().setMyPreferenceString(c, "songfilename", thisSong.getFilename());
            mainActivityInterface.getPreferences().setMyPreferenceString(c, "whichSongFolder", thisSong.getFolder());
        } else {
            // Something was wrong, so set the welcome song
            thisSong.setFilename("Welcome to OpenSongApp");
            thisSong.setFolder(c.getString(R.string.mainfoldername));
            mainActivityInterface.getPreferences().setMyPreferenceString(c, "songfilename", "Welcome to OpenSongApp");
            mainActivityInterface.getPreferences().setMyPreferenceString(c, "whichSongFolder", c.getString(R.string.mainfoldername));
        }
    }

    private boolean lyricsHaveChoProTags(String lyrics) {
        return lyrics.contains("{title") ||
                lyrics.contains("{t:") ||
                lyrics.contains("{t :") ||
                lyrics.contains("{subtitle") ||
                lyrics.contains("{st:") ||
                lyrics.contains("{st :") ||
                lyrics.contains("{comment") ||
                lyrics.contains("{c:") ||
                lyrics.contains("{new_song") ||
                lyrics.contains("{ns");
    }

    private String getFileTypeByExtension(String filename) {
        filename = filename.toLowerCase(Locale.ROOT);
        if (!filename.contains(".")) {
            // No extension, so hopefully ok
            return "XML";
        } else if (filename.endsWith(".pdf")) {
            return "PDF";
        } else if (filename.endsWith(".doc") ||
                filename.endsWith(".docx")) {
            return "DOC";
        } else if (filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".png") ||
                filename.endsWith(".gif") ||
                filename.endsWith(".bmp")) {
            return "IMG";
        } else if (filename.endsWith(".cho") ||
                filename.endsWith(".crd") ||
                filename.endsWith(".chopro") ||
                filename.contains(".pro")) {
            return "CHO";
        } else if (filename.endsWith(".onsong")) {
            return "iOS";
        } else if (filename.endsWith(".txt")) {
            return "TXT";
        } else {
            // Assume we are good to go!
            return "XML";
        }
    }

    private String getUTF(Context c, MainActivityInterface mainActivityInterface,
                          String folder, String filename, String filetype) {
        // Determine the file encoding
        String where = "Songs";
        if (folder.startsWith("../")) {
            folder = folder.replace("../", "");
        }
        uri = mainActivityInterface.getStorageAccess().getUriForItem(c, 
                mainActivityInterface, where, folder, filename);
        if (mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
            if (filetype.equals("XML") && !filename.equals("Welcome to OpenSongApp")) {
                return mainActivityInterface.getStorageAccess().getUTFEncoding(c, uri);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Song readFileAsXML(Context c, MainActivityInterface mainActivityInterface, Song thisSong,
                              String where, Uri uri, String utf) {

        // Extract all of the key bits of the song
        if (mainActivityInterface.getStorageAccess().uriIsFile(c, uri)) {
            try {
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);
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
                                try {
                                    thisSong.setAuthor(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // Try to read in the xml
                                    thisSong.setAuthor(fixXML(c, mainActivityInterface, thisSong, "author", where));
                                }
                                break;
                            case "copyright":
                                thisSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "title":
                                String testthetitle = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                                if (testthetitle != null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                                    thisSong.setTitle(mainActivityInterface.getProcessSong().parseHTML(testthetitle));
                                } else if (testthetitle != null && testthetitle.equals("")) {
                                    thisSong.setTitle(thisSong.getFilename());
                                }
                                break;
                            case "lyrics":
                                try {
                                    thisSong.setLyrics(mainActivityInterface.getProcessSong().fixStartOfLines(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText())));
                                } catch (Exception e) {
                                    // Try to read in the xml
                                    e.printStackTrace();
                                    thisSong.setLyrics(fixXML(c, mainActivityInterface, thisSong, "lyrics", where));
                                }
                                break;
                            case "ccli":
                                thisSong.setCcli(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "theme":
                                thisSong.setTheme(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "alttheme":
                                thisSong.setAlttheme(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "presentation":
                                thisSong.setPresentationorder(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "hymn_number":
                                thisSong.setHymnnum(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "user1":
                                thisSong.setUser1(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "user2":
                                thisSong.setUser2(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "user3":
                                thisSong.setUser3(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "key":
                                thisSong.setKey(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "aka":
                                thisSong.setAka(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "capo":
                                if (xpp.getAttributeCount() > 0) {
                                    thisSong.setCapoprint(xpp.getAttributeValue(0));
                                }
                                thisSong.setCapo(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "tempo":
                                thisSong.setTempo(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "time_sig":
                                thisSong.setTimesig(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "duration":
                                thisSong.setAutoscrolllength(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "predelay":
                                thisSong.setAutoscrolldelay(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "midi":
                                thisSong.setMidi(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "midi_index":
                                thisSong.setMidiindex(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "notes":
                                thisSong.setNotes(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "pad_file":
                                thisSong.setPadfile(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "custom_chords":
                                thisSong.setCustomChords(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "link_youtube":
                                thisSong.setLinkyoutube(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "link_web":
                                thisSong.setLinkweb(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "link_audio":
                                thisSong.setLinkaudio(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "loop_audio":
                                thisSong.setPadloop(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "link_other":
                                thisSong.setLinkother(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "abcnotation":
                                thisSong.setAbc(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                break;
                            case "style":
                            case "backgrounds":
                                // Simplest way to get this is to keep a record that it exists
                                // That way we can extracted it if we need it (for saving edits)
                                thisSong.setHasExtraStuff(true);
                                break;
                        }
                    }
                    // If it isn't an xml file, an error is about to be thrown
                    try {
                        eventType = xpp.next();
                        thisSong.setFiletype("XML");

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (thisSong.getFiletype().equals("XML")) {
                            // This was an XML file and it wasn't a simple issue
                            // Sometimes songs get weird extra bits added after the closing tag
                            // Try to remove that after we are finished indexing
                            if (songsToFix==null) {
                                songsToFix = new ArrayList<>();
                            }
                            songsToFix.add(thisSong);

                        } else {
                            Log.d(TAG, uri + ":  Not xml so exiting");
                            thisSong.setFiletype("?");
                        }
                        eventType = XmlPullParser.END_DOCUMENT;
                    }
                }
                inputStream.close();

                if (thisSong.getFilename().equals("Welcome to OpenSongApp")) {
                    thisSong = thisSong.showWelcomeSong(c,thisSong);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return thisSong;
    }

    public ArrayList<Song> getSongsToFix() {
        return songsToFix;
    }

    public void resetSongsToFix() {
        if (songsToFix!=null) {
            songsToFix.clear();
        }
        songsToFix = null;
    }

    public void fixSongs(Context c, MainActivityInterface mainActivityInterface) {
        if (songsToFix!=null && songsToFix.size()>0) {
            for (Song thisSong:songsToFix) {
                Log.d(TAG, "Fixing rogue ending: " + thisSong.getFilename());
                Uri thisSongUri = mainActivityInterface.getStorageAccess().getUriForItem(c,
                        mainActivityInterface,"Songs",thisSong.getFolder(),thisSong.getFilename());
                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, thisSongUri);
                String content = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                Log.d(TAG, "thisSongUri:" + thisSongUri);
                if (content.contains("</song>") && (content.indexOf("</song>") + 7) < content.length()) {
                    content = content.substring(0, content.indexOf("</song>")) + "</song>";
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c, thisSongUri);
                    Log.d(TAG, "success saving? " + mainActivityInterface.getStorageAccess().writeFileFromString(content, outputStream));
                    InputStream inputStream2 = mainActivityInterface.getStorageAccess().getInputStream(c, thisSongUri);
                    String content2 = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream2);
                    Log.d(TAG, "loadedContentafterFix:" + content2);
                }
            }
        }
        resetSongsToFix();
    }
    public String getExtraStuff(Context c, MainActivityInterface mainActivityInterface, Song thisSong) {
        // This is only called if we save/edit a song and it has extra stuff marked
        // In which case we load it in as extracted text and add it back to the XML file as a returned string
        String extraStuff = "";
        if (thisSong.getHasExtraStuff()) {
            // This method will be called in an new thread from the calling activity
            String filename = thisSong.getFilename();
            Uri extraUri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,
                    "Songs",thisSong.getFolder(),thisSong.getFilename());
            InputStream extraIinputStream = mainActivityInterface.getStorageAccess().getInputStream(c, extraUri);
                String full_text;
                try {
                    if (validReadableFile(c, mainActivityInterface, extraUri, filename)) {
                        full_text = mainActivityInterface.getStorageAccess().readTextFileToString(extraIinputStream);
                    } else {
                        full_text = "";
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error reading text file");
                    full_text = "";
                }

                try {
                    int style_start = full_text.indexOf("<style");
                    int style_end = full_text.indexOf("</style>");
                    if (style_end > style_start && style_start > -1) {
                        extraStuff += full_text.substring(style_start, style_end + 8) + "/n";
                    }
                    int backgrounds_start = full_text.indexOf("<backgrounds");
                    int backgrounds_end = full_text.indexOf("</backgrounds>");
                    if (backgrounds_end < 0) {
                        backgrounds_end = full_text.indexOf("/>", backgrounds_start) + 2;
                    } else {
                        backgrounds_end += 14;
                    }
                    if (backgrounds_end > backgrounds_start && backgrounds_start > -1) {
                        extraStuff += full_text.substring(backgrounds_start, backgrounds_end) + "/n";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    extraIinputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return extraStuff;
    }

    private boolean validReadableFile(Context c, MainActivityInterface mainActivityInterface, Uri uri, String filename) {
        boolean isvalid = false;
        // Get length of file in Kb
        float filesize = mainActivityInterface.getStorageAccess().getFileSizeFromUri(c, uri);
        if (filename.endsWith(".txt") || filename.endsWith(".TXT") ||
                filename.endsWith(".onsong") || filename.endsWith(".ONSONG") ||
                filename.endsWith(".crd") || filename.endsWith(".CRD") ||
                filename.endsWith(".chopro") || filename.endsWith(".CHOPRO") ||
                filename.endsWith(".chordpro") || filename.endsWith(".CHORDPRO") ||
                filename.endsWith(".usr") || filename.endsWith(".USR") ||
                filename.endsWith(".pro") || filename.endsWith(".PRO")) {
            isvalid = true;
        } else if (filesize < 2000) {
            // Less than 2Mb
            isvalid = true;
        }
        return isvalid;
    }

    private String fixXML(Context c, MainActivityInterface mainActivityInterface, Song thisSong, String section, String where) {
        // Error in the xml as this is run from a catch block - tell the user we're trying to fix it!
        mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.fix));
        StringBuilder newXML = new StringBuilder();
        String tofix;

        Log.d(TAG,"running fixXML: filename="+thisSong.getFilename()+"  section="+section+ "  where="+where);
        // If an XML file has unencoded ampersands or quotes, fix them
        try {
            tofix = getSongAsText(c,mainActivityInterface,where,thisSong.getFolder(),thisSong.getFilename());
            if (tofix.contains("<")) {
                String[] sections = tofix.split("<");
                for (String bit : sections) {
                    // We are going though a section at a time
                    int postofix = bit.indexOf(">");
                    if (postofix >= 0) {
                        String startbit = "<"+bit.substring(0,postofix);
                        String bittofix = doFix(bit.substring(postofix));
                        newXML.append(startbit).append(bittofix);
                    } else {
                        // No closing tag identifier, so add it
                        newXML.append("<").append(bit).append(">");
                    }
                }
            } else {
                newXML.append(tofix);
            }

            // Now save the song again (output stream is closed in the write file method)
            OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(c,uri);
            mainActivityInterface.getStorageAccess().writeFileFromString(newXML.toString(),outputStream);

            // Try to extract the section we need
            if (newXML.toString().contains("<"+section+">") && newXML.toString().contains("</"+section+">")) {
                int start = newXML.indexOf("<"+section+">") + 2 + section.length();
                int end = newXML.indexOf("</"+section+">");
                thisSong.setFiletype("XML");
                return newXML.substring(start,end);
            } else {
                return newXML.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String doFix(String tofix) {
        tofix = tofix.replace("&amp;", "&");
        tofix = tofix.replace("&apos;", "'");  // ' are actually fine - no need
        tofix = tofix.replace("&quot;", "\"");

        // Get rid of doubles
        while (tofix.contains("&&")) {
            tofix = tofix.replace("&&;", "&");
        }

        // Now put them back
        tofix = tofix.replace("&", "$_amp_$");
        tofix = tofix.replace("\"", "&quot;");
        tofix = tofix.replace("$_amp_$", "&amp;");

        return tofix;
    }

    private String getSongAsText(Context c, MainActivityInterface  mainActivityInterface, String where, String folder, String filename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,where, folder,filename);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c,uri);
        String s = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
        try {
            if (inputStream!=null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public String getTempFileLocation(Context c, String folder, String file) {
        String where = folder + "/" + file;
        if (folder.equals(c.getString(R.string.mainfoldername)) || folder.equals("MAIN") || folder.equals("")) {
            where = file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.note))) {
            where = "../Notes/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.image))) {
            where = "../Images/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.scripture))) {
            where = "../Scripture/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.slide))) {
            where = "../Slides/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.variation))) {
            where = "../Variations/" + file;
        }
        return where;
    }

    public String loadKeyOfSong(Context c, MainActivityInterface mainActivityInterface, String folder, String filename) {
        String nextkey = "";

        // If the indexing is done and the song is there,

        // Get the android version
        boolean nextisxml = filename != null && !filename.isEmpty() &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".pdf") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".doc") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".docx") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".jpg") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".jpeg") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".png") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".gif") &&
                !filename.toLowerCase(Locale.ROOT).endsWith(".bmp");

        String nextutf = null;

        Uri uri = null;
        String subfolder = "";
        if (nextisxml) {
            if (folder.contains("**") || folder.contains("../")) {
                subfolder = folder;
            }
            uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Songs", subfolder, filename);
            nextutf = mainActivityInterface.getStorageAccess().getUTFEncoding(c, uri);
        }

        try {
            if (nextisxml && nextutf != null && !nextutf.equals("")) {
                // Extract all of the key bits of the song
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                nextkey = "";

                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, uri);
                if (inputStream != null) {
                    xpp.setInput(inputStream, nextutf);

                    int eventType;
                    eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("key")) {
                                nextkey = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                            }
                        }
                        try {
                            eventType = xpp.next();
                        } catch (Exception e) {
                            //Ooops!
                        }
                    }
                }
                if (inputStream!=null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"Error trying to read XML from "+uri);
            // Ooops
        }

        return nextkey;
    }

}
