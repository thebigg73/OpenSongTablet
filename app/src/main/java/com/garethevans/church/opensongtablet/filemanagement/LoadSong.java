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
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private Uri uri;
    private ArrayList<Song> songsToFix;
    private boolean importingFile = false;

    public LoadSong(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
    }

    // The song object is sent to get the folder/filename, but it is returned after adding to it
    // Since this could be an indexing action, getting the next key or an actual load, 
    // we just receive the song object (can't assume mainActivityInterface.getSong()
    public Song doLoadSong(Song thisSong, boolean indexing) {
        // If we have finished song indexing, we get the song from the SQL database.
        // Unless, of course it is a custom file.
        // If not, we load up from the xml file
        // We also load from the file if it is a custom file (pdf and images are dealt with separately)

        // Get the song folder/filename from the sent song object
        String folder = thisSong.getFolder();
        String filename = thisSong.getFilename();

        if (filename.equals(c.getString(R.string.welcome))) {
            filename = "Welcome to OpenSongApp";
            thisSong.setFilename(filename);
        }

        // Clear the song object then add the folder filename back
        thisSong = new Song();
        thisSong.setFolder(folder);
        thisSong.setFilename(filename);

        // Set the currently loading status.  Switched off when displayed to the screen
        thisSong.setCurrentlyLoading(true);

        // We will add to this song and then return it to the MainActivity object
        if (!mainActivityInterface.getSongListBuildIndex().getIndexComplete() ||
                mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing() ||
                thisSong.getFolder().contains("**") || thisSong.getFolder().startsWith("../") ||
                thisSong.getFilename().endsWith(".txt") || thisSong.getFilename().endsWith(".onsong") ||
                thisSong.getFilename().endsWith(".cho") ||
                thisSong.getFiletype().equals("TXT") || thisSong.getFiletype().equals("CHO") || thisSong.getFiletype().equals("iOS")) {
            // This is set to true once the index is completed, so we either haven't finished indexing
            // or this is a custom slide/note as identified by the folder (which aren't indexed)
            return doLoadSongFile(thisSong, indexing);
        } else {
            Log.d(TAG, "Loading from the database");
            if (thisSong.getFilename().equals("Welcome to OpenSongApp") || thisSong.getFilename().endsWith(c.getString(R.string.welcome))) {
                return mainActivityInterface.getSong().showWelcomeSong(c, thisSong);
            } else {
                thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                        thisSong.getFolder(), thisSong.getFilename());
                // IV - A Database song still needs cleaning -odd?
                // Fix all the rogue code
                if (thisSong.getLyrics()==null || thisSong.getLyrics().isEmpty()) {
                    thisSong.setLyrics(" ");
                }
                thisSong.setLyrics(mainActivityInterface.getProcessSong().parseLyrics(mainActivityInterface.getLocale(), thisSong));

                sortLoadingSuccessful(thisSong);

                // Get the detected chord format
                mainActivityInterface.getTranspose().checkChordFormat(thisSong);

                return thisSong;
            }
        }
    }

    public Song doLoadSongFile(Song thisSong, boolean indexing) {

        // This extracts what it can from the song, and returning an updated song object.
        // Once indexing has finished, we load from the database instead
        // This is only for indexing and impatient users or if we are loading a PDF/IMG or importing

        if (thisSong.getFolder() == null || thisSong.getFolder().isEmpty()) {
            thisSong.setFolder(c.getString(R.string.mainfoldername));
        }
        if (thisSong.getFilename() == null || thisSong.getFilename().isEmpty()) {
            thisSong.setFilename("Welcome to OpenSongApp");
        }

        // Set the song load status to false (helps check if it didn't load).  This is set to true after success
        if (!indexing) {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songLoadSuccess", false);
        }

        // When getting the uri, it runs a check for custom folders **Variation, etc.
        String where = "Songs";

        // Determine the filetype by extension - the best songs are xml (OpenSong formatted).
        // If we are importing and ost file, we have already set this if appropriate to XML
        // Normally this is bad, but not as we are importing from a temp location
        if (!importingFile) {
            thisSong.setFiletype(getFileTypeByExtension(thisSong.getFilename()));
        }

        // Get the uri for the song - we know it exists as we found it!
        uri = mainActivityInterface.getStorageAccess().getUriForItem(
                where, thisSong.getFolder(), thisSong.getFilename());

        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            // If this is an image or a PDF (or DOC or ZIP), we don't load a song object from the file
            // Instead we use the database, but the user will have to wait!

            if (!thisSong.getFiletype().equals("PDF") && !thisSong.getFiletype().equals("IMG") &&
                    !thisSong.getFiletype().equals("DOC") && !thisSong.getFilename().equals("ZIP")) {

                String utf;
                // Go through our options one at a time
                if (thisSong.getFilename().equals("Welcome to OpenSongApp")) {
                    // 1. We are using the default song
                    thisSong = thisSong.showWelcomeSong(c, thisSong);
                    // Don't update the songLoadSuccess as this isn't what the user really wants

                } else if (thisSong.getFiletype().equals("XML")) {
                    Log.d(TAG,"loading xml");
                    // 2. We have an XML file (likely)
                    utf = getUTF(thisSong.getFolder(),
                            thisSong.getFilename(), thisSong.getFiletype());
                    thisSong = readFileAsXML(thisSong, where, uri, utf);
                }


                // If the file wasn't read as an xml file and might be text based, we need to deal with it in another way
                // This isn't added as an else statement to the above as one of those methods might have been tried
                // If they find an issue, they send the song back with a better guessed filetype
                if (!thisSong.getFiletype().equals("XML")) {
                    // This will try to import text, chordpro or onsong and update the lyrics field
                    thisSong.setLyrics(getSongAsText(where, thisSong.getFolder(), thisSong.getFilename()));
                    thisSong.setTitle(thisSong.getFilename());
                    if (thisSong.getLyrics() != null && !thisSong.getLyrics().isEmpty()) {
                        // Success (although we'll maybe process it below)
                        mainActivityInterface.getPreferences().setMyPreferenceBoolean("songLoadSuccess", true);
                    }
                }

                if (thisSong.getFiletype().equals("iOS")) {
                    // 3.  Run the OnSongConvert script (which converts then resaves)
                    mainActivityInterface.getConvertOnSong().convertTextToTags(uri, thisSong);

                    // Now read in the proper OpenSong xml file
                    try {
                        readFileAsXML(thisSong, where, uri, "UTF-8");
                    } catch (Exception e) {
                        Log.d(TAG, "Error performing grabOpenSongXML()");
                    }
                } else if (thisSong.getFiletype().equals("CHO") || lyricsHaveChoProTags(thisSong.getLyrics())) {
                    // 4.  Run the ChordProConvert script (which converts then resaves)
                    thisSong = mainActivityInterface.getConvertChoPro().convertTextToTags(uri, thisSong);

                    // Now read in the proper OpenSong xml file
                    try {
                        readFileAsXML(thisSong, where, uri, "UTF-8");
                    } catch (Exception e) {
                        Log.d(TAG, "Error performing grabOpenSongXML()");
                    }
                } else if (thisSong.getFiletype().equals("TXT")) {
                    // 5.  Run the text convert script
                    Log.d(TAG,"thisSong.getLyrics():"+thisSong.getLyrics());
                    thisSong.setLyrics(mainActivityInterface.getConvertTextSong().convertText(thisSong.getLyrics()));
                    // Save the new song
                    String oldname = thisSong.getFilename();
                    String newname = oldname.replace(".txt","");
                    thisSong.setTitle(newname);
                    thisSong.setFilename(newname);
                    Uri olduri = uri;
                    Log.d(TAG,"thisSong.getLyrics():"+thisSong.getLyrics());
                    uri = mainActivityInterface.getStorageAccess().getUriForItem(where,thisSong.getFolder(),newname);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,uri,null,where,thisSong.getFolder(),newname);
                    //String newSongContent = mainActivityInterface.getProcessSong().getXML(thisSong);
                    if (mainActivityInterface.getStorageAccess().doStringWriteToFile(where,thisSong.getFolder(),newname,thisSong.getSongXML())) {
                        mainActivityInterface.getStorageAccess().deleteFile(olduri);
                    }
                    // Now read in the proper OpenSong xml file
                    try {
                        readFileAsXML(thisSong, where, uri, "UTF-8");
                    } catch (Exception e) {
                        Log.d(TAG, "Error performing grabOpenSongXML()");
                    }
                }

                // Fix all the rogue code
                if (thisSong.getLyrics()==null || thisSong.getLyrics().isEmpty()) {
                    thisSong.setLyrics(" ");
                }
                thisSong.setLyrics(mainActivityInterface.getProcessSong().parseLyrics(mainActivityInterface.getLocale(), thisSong));

            } else {
                thisSong.setTitle(thisSong.getFilename());

            }
        } else if (thisSong.getFilename().equals("Welcome to OpenSongApp")) {
            thisSong = mainActivityInterface.getSong().showWelcomeSong(c,thisSong);
        } else {
            // Not found.  This will get the default 'not found' from the database query
            thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(thisSong.getFolder(),thisSong.getFilename());
        }

        // Update the songLoadSuccess and references to the working file if it did work if we aren't indexing
        if (!indexing) {
            sortLoadingSuccessful(thisSong);

            // Get the detected chord format - don't do while indexing!
            mainActivityInterface.getTranspose().checkChordFormat(thisSong);
        }

        // Send the song back with all of its children populated!
        return thisSong;
    }

    private void sortLoadingSuccessful(Song thisSong) {
        // Check if the song has been loaded (will now have a lyrics value)
        if (thisSong.getFiletype()!=null && (thisSong.getFiletype().equals("PDF") || thisSong.getFiletype().equals("IMG")) &&
        thisSong.getLyrics()==null) {
            // A basic PDF/IMG without custom info
            thisSong.setLyrics("");
        }

        if (!thisSong.getFilename().toLowerCase(Locale.ROOT).equals("welcome to opensongapp") &&
                thisSong.getLyrics() != null) {
            // Song was loaded correctly and was xml format
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("songLoadSuccess", true);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", thisSong.getFilename());
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", thisSong.getFolder());
        } else {
            // Something was wrong, so set the welcome song
            thisSong.setFilename("Welcome to OpenSongApp");
            thisSong.setFolder(c.getString(R.string.mainfoldername));
            mainActivityInterface.getSong().showWelcomeSong(c,thisSong);
            mainActivityInterface.getPreferences().setMyPreferenceString("songFilename", "Welcome to OpenSongApp");
            mainActivityInterface.getPreferences().setMyPreferenceString("songFolder", c.getString(R.string.mainfoldername));
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
            // No extension, so hopefully okay
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
        } else if (filename.endsWith(".zip")) {
            return "ZIP";
        } else if (mainActivityInterface.getStorageAccess().badFileExtension(filename)) {
            return "BAD";
        } else {
            // Assume we are good to go!
            return "XML";
        }
    }

    private String getUTF(String folder, String filename, String filetype) {
        // Determine the file encoding
        String where = "Songs";
        if (folder.startsWith("../")) {
            where = folder.replace("../", "");
            folder = where;
            if (where.contains("/")) {
                where = where.substring(0,where.indexOf("/"));
                folder = folder.replace(where,"");
            } else {
                folder = "";
            }
        }
        uri = mainActivityInterface.getStorageAccess().getUriForItem(where, folder, filename);
        if (mainActivityInterface.getStorageAccess().uriExists(uri)) {
            if (filetype.equals("XML") && !filename.equals("Welcome to OpenSongApp")) {
                return mainActivityInterface.getStorageAccess().getUTFEncoding(uri);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Song readFileAsXML(Song thisSong, String where, Uri uri, String utf) {

        // Don't do this if we have an unrecognised song format
        // Check for the import pass go allowance
        if ((importingFile && thisSong.getFiletype().equals("XML")) || !mainActivityInterface.getStorageAccess().badFileExtension(thisSong.getFilename())) {
            // Extract all of the key bits of the song
            if (mainActivityInterface.getStorageAccess().uriIsFile(uri)) {
                try {
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
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
                                        thisSong.setAuthor(fixXML(thisSong, "author", where));
                                    }
                                    break;
                                case "copyright":
                                    thisSong.setCopyright(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                    break;
                                case "title":
                                    String testthetitle = mainActivityInterface.getProcessSong().parseHTML(xpp.nextText());
                                    if (testthetitle != null && !testthetitle.isEmpty()) {
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
                                        thisSong.setLyrics(fixXML(thisSong, "lyrics", where));
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
                                case "beatbuddysong":
                                    thisSong.setBeatbuddysong(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                    break;
                                case "beatbuddykit":
                                    thisSong.setBeatbuddykit(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                    break;
                                case "key":
                                    thisSong.setKey(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
                                    break;
                                case "keyoriginal":
                                    thisSong.setKeyOriginal(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
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
                                case "abctranspose":
                                    thisSong.setAbcTranspose(mainActivityInterface.getProcessSong().parseHTML(xpp.nextText()));
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
                            Log.d(TAG, "Not an straightforward xml file: " + thisSong.getFolder() + "/" + thisSong.getFilename() + "  filetype:" + thisSong.getFilename());
                            e.printStackTrace();
                            if (thisSong.getFiletype().equals("XML")) {
                                // This was an XML file and it wasn't a simple issue
                                // Sometimes songs get weird extra bits added after the closing tag
                                // Try to remove that after we are finished indexing
                                if (songsToFix == null) {
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
                        thisSong = thisSong.showWelcomeSong(c, thisSong);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            thisSong.setFiletype("BAD");
        }
        return thisSong;
    }

    public void resetSongsToFix() {
        if (songsToFix!=null) {
            if (songsToFix.size()>0) {
                // We need to rebuild the song index as we fixed stuff!
                mainActivityInterface.indexSongs();
            }
            songsToFix.clear();

        }
        songsToFix = null;
    }

    public void fixSongs() {
        if (songsToFix!=null && songsToFix.size()>0) {
            for (Song thisSong:songsToFix) {
                if (thisSong.getFiletype().equals("XML")) {
                    Log.d(TAG, "songToFix:" + thisSong.getFolder() + "/" + thisSong.getFilename() + "  " + thisSong.getFiletype());
                    mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " Fix Songs/" + thisSong.getFolder() + "/" + thisSong.getFilename() + "  deleteOld=true");
                    Uri thisSongUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", thisSong.getFolder(), thisSong.getFilename());
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(thisSongUri);
                    String content = mainActivityInterface.getStorageAccess().readTextFileToString(inputStream);
                    boolean success = false;
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // If we have empty lines between tags, fix that. e.g.
                    // <pad_file></pad_file>
                    //
                    // <backgrounds resize="screen" keep_aspect="false" link="false" background_as_text="false"/>
                    // All < and > in the content are already encoded, so these are definitely tags!
                    content = content.replace(">\n\n<",">\n<");
                    content = content.replace(">\n  \n<",">\n<");
                    content = content.replace(">\n\n  <",">\n  <");
                    content = content.replace(">\n  \n  <",">\n  <");

                    // Corrupted XML files with </song> before the end
                    if (content.contains("</song>") && (content.indexOf("</song>") + 7) < content.length()) {
                        content = content.substring(0, content.indexOf("</song>")) + "</song>";
                        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG + " fixSongs doStringWriteToFile Songs/" + thisSong.getFolder() + "/" + thisSong.getFilename() + " with: " + content);
                        success = mainActivityInterface.getStorageAccess().doStringWriteToFile(
                                "Songs", thisSong.getFolder(), thisSong.getFilename(), content);
                        Log.d(TAG, "fixSong: " + success);

                    } else if (thisSong.getFiletype().equals("XML") || thisSong.getFiletype().equals("TXT")) {
                        thisSong.setLyrics(content);
                        String oldname = thisSong.getFilename();
                        Uri oldUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", thisSong.getFolder(), thisSong.getFilename());
                        String newname = oldname.replace(".txt", "");
                        thisSong.setFilename(newname);
                        thisSong.setTitle(newname);
                        String xml = mainActivityInterface.getProcessSong().getXML(thisSong);
                        success = mainActivityInterface.getStorageAccess().doStringWriteToFile(
                                "Songs", thisSong.getFolder(), newname, xml);
                        Log.d(TAG,"oldname:"+oldname+"  newname:"+newname+"  success:"+success);
                        if (success && !newname.equals(oldname)) {
                            // Remove the obsolete file
                            mainActivityInterface.getStorageAccess().deleteFile(oldUri);
                        }
                    }
                    Log.d(TAG, "fixSong: " + success);
                    if (success) {
                        mainActivityInterface.getSQLiteHelper().updateSong(thisSong);
                    }
                }
            }
        }
        resetSongsToFix();
    }
    public String getExtraStuff(Song thisSong) {
        // This is only called if we save/edit a song and it has extra stuff marked
        // In which case we load it in as extracted text and add it back to the XML file as a returned string
        String extraStuff = "";
        if (thisSong.getHasExtraStuff()) {
            // This method will be called in an new thread from the calling activity
            String filename = thisSong.getFilename();
            Uri extraUri = mainActivityInterface.getStorageAccess().getUriForItem(
                    "Songs",thisSong.getFolder(),thisSong.getFilename());
            InputStream extraIinputStream = mainActivityInterface.getStorageAccess().getInputStream(extraUri);
                String full_text;
                try {
                    if (validReadableFile(extraUri, filename)) {
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

    private boolean validReadableFile(Uri uri, String filename) {
        boolean isvalid = false;
        // Get length of file in Kb
        float filesize = mainActivityInterface.getStorageAccess().getFileSizeFromUri(uri);
        /* Rather than check for each bad extension as users add random stuff,
           Just look for a . and not pdf/image */
        if (!filename.contains(".") || filename.toLowerCase().endsWith(".xml") ||
                mainActivityInterface.getStorageAccess().isIMGorPDF(filename)) {
            isvalid = true;
        } else if (filesize < 2000) {
            // Less than 2Mb
            isvalid = true;
        }

        if (!filename.contains(".") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".xml") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".txt") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".onsong") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".crd") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".chopro") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".chordpro") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".usr") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".pro")) {
            isvalid = true;
        }
        // Removed this stuff as small invalid files break the indexing
        // } else if (filesize < 2000) {
            // Less than 2Mb
        //    isvalid = true;
        //}

        return isvalid;
    }

    private String fixXML(Song thisSong, String section, String where) {
        // Error in the xml as this is run from a catch block - tell the user we're trying to fix it!
        mainActivityInterface.getShowToast().doIt(c.getString(R.string.fix));
        String toFix;

        // Try to get this section
        toFix = getSongAsText(where,thisSong.getFolder(),thisSong.getFilename());

        Log.d(TAG,"toFix:"+toFix);
        if (!section.isEmpty() && toFix.contains("<"+section+">") && toFix.contains("</"+section+">")) {
            int start = toFix.indexOf("<"  + section + ">") + section.length() + 2;
            int end   = toFix.indexOf("</" + section + ">");
            Log.d(TAG,"start:"+start+"  end:"+end);
            if (start>-1 && end>start) {
                String origExtracted = toFix.substring(start,end);
                String newExtracted  = origExtracted.replace("<","&lt;");
                newExtracted = newExtracted.replace(">","&gt;");

                toFix = toFix.replace(origExtracted,newExtracted).replace("<>","");

                // Now save the song again (output stream is closed in the write file method)
                OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(uri);
                mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" fixXML writeFileFromString "+uri+" with: "+toFix);
                mainActivityInterface.getStorageAccess().writeFileFromString(toFix,outputStream);

                Log.d(TAG,"fixed "+section+" :"+newExtracted);
                return newExtracted;
            } else {
                toFix = "";
            }
        } else {
            toFix = "";
        }
        Log.d(TAG,"fixed "+section+" :"+toFix);
        return toFix;
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

    private String getSongAsText(String where, String folder, String filename) {
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(where, folder,filename);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
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

    public String getTempFileLocation(String folder, String file) {
        String where = folder + "/" + file;
        if (folder.equals(c.getString(R.string.mainfoldername)) || folder.equals("MAIN") || folder.equals("")) {
            where = file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.note)) || folder.contains("**Note")) {
            where = "../Notes/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.image)) || folder.contains("**Image")) {
            where = "../Images/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.scripture)) || folder.contains("**Scripture")) {
            where = "../Scripture/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.slide)) || folder.contains("**Slide")) {
            where = "../Slides/_cache/" + file;
        } else if (folder.contains("**" + c.getResources().getString(R.string.variation)) || folder.contains("**Variation")) {
            where = "../Variations/" + file;
        }
        return where;
    }

    public String loadKeyOfSong(String folder, String filename) {
        String nextkey = "";

        // If the indexing is done and the song is there,

        // Get the android version
        String lowerfilename = "";
        if (filename!=null && !filename.isEmpty()) {
            lowerfilename = filename.toLowerCase(Locale.ROOT);
        }

        boolean nextisxml = !lowerfilename.endsWith(".pdf") &&
                !lowerfilename.endsWith(".doc") &&
                !lowerfilename.endsWith(".docx") &&
                !lowerfilename.endsWith(".jpg") &&
                !lowerfilename.endsWith(".jpeg") &&
                !lowerfilename.endsWith(".png") &&
                !lowerfilename.endsWith(".gif") &&
                !lowerfilename.endsWith(".bmp");

        String nextutf = null;

        Uri uri = null;
        String subfolder = "";
        if (nextisxml) {
            if (folder.contains("**") || folder.contains("../")) {
                subfolder = folder;
            }
            uri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", subfolder, filename);
            nextutf = mainActivityInterface.getStorageAccess().getUTFEncoding(uri);
        }

        try {
            if (nextisxml && nextutf != null && !nextutf.equals("")) {
                // Extract all of the key bits of the song
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                nextkey = "";

                InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
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

    public void setImportingFile(boolean importingFile) {
        this.importingFile = importingFile;
    }
    public boolean getImportingFile() {
        return importingFile;
    }
}
