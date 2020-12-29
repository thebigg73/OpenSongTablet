package com.garethevans.church.opensongtablet.filemanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class LoadSong {

    //private boolean isXML, isPDF, isIMG, isCustom,
    private boolean needtoloadextra = false;
    private Uri uri;

    public Song doLoadSong(Context c, StorageAccess storageAccess, Preferences preferences,
                           ProcessSong processSong, ShowToast showToast,
                           SQLiteHelper sqLiteHelper, CommonSQL commonSQL, Song song,
                           ConvertOnSong convertOnSong, ConvertChoPro convertChoPro, boolean indexing) {

        // If we have finished song indexing, we get the song from the SQL database.
        // If not, we load up from the xml file
        // We also load from the file if it is a custom file (pdf and images are dealt with separately)

        if (!StaticVariables.indexComplete || song.getFolder().contains("../")) {
            // This is set to true once the index is completed
            Log.d("LoadSong","Loading from the xml file");
            return doLoadSongFile(c,storageAccess,preferences,processSong,showToast,
                    sqLiteHelper,commonSQL,song,convertOnSong,convertChoPro,indexing);
        } else {
            Log.d("LoadSong","Loading from the database");
            return sqLiteHelper.getSpecificSong(c,commonSQL,song.getFolder(),song.getFilename());
        }


    }
    public Song doLoadSongFile(Context c, StorageAccess storageAccess, Preferences preferences,
                             ProcessSong processSong, ShowToast showToast,
                             SQLiteHelper sqLiteHelper, CommonSQL commonSQL, Song song,
                             ConvertOnSong convertOnSong, ConvertChoPro convertChoPro, boolean indexing) {

        // This extracts what it can from the song, and returning an updated SQLite song object.
        // If we are indexing, that's it.  If not, we update the statics with the SQL values
        // Set the song load status to false (helps check if it didn't load).  This is set to true after success

        // Once indexing has finished, we load from the database instead, so this is only for indexing and impatient users!

        if (!indexing) {
            preferences.setMyPreferenceBoolean(c,"songLoadSuccess",false);
        }

        String where = "Songs";
        if (song.getFolder().startsWith("../")) {
            where = song.getFolder();
            where = where.replace("../","");
            String folder = "";
            if (where.contains("/")) {
                folder = where.substring(where.indexOf("/"));
                where = where.substring(0,where.indexOf("/"));
                folder = folder.replace("/","");
                where = where.replace("/","");

            }
            song.setFolder(folder);
        }

        // Determine the filetype by extension - the best songs are xml (OpenSong formatted).
        song.setFiletype(getFileTypeByExtension(song.getFilename()));

        // Get the uri for the song - we know it exists as we found it!
        uri = storageAccess.getUriForItem(c,preferences,where,song.getFolder(),song.getFilename());

        // Get the file encoding (this also tests that the file exists)
        //private String filetype, utf, where = "Songs", folder, origfolder;
        String utf = getUTF(c, storageAccess, preferences, song.getFolder(), song.getFilename(), song.getFiletype());

        // Try to load the song as an xml
        if (song.getFiletype().equals("XML") && !song.getFilename().equals("Welcome to OpenSongApp")) {
            // Here we go loading the song
            // This returns an update sqLite song object if it works
            try {
                song = readFileAsXML(c, storageAccess, preferences, processSong, showToast, where, song, uri, utf, indexing);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }

        // If the file wasn't read as an xml file and might be text based, we need to deal with it in another way
        if (!song.getFiletype().equals("XML") && !song.getFiletype().equals("PDF") &&
                !song.getFiletype().equals("IMG") && !song.getFiletype().equals("DOC")) {
            // This will try to import text, chordpro or onsong and update the lyrics field
            song.setLyrics(getSongAsText(c,storageAccess,preferences, where, song.getFolder(), song.getFilename()));
            song.setTitle(song.getFilename());
            if (song.getLyrics()!=null && !song.getLyrics().isEmpty()) {
                preferences.setMyPreferenceBoolean(c,"songLoadSuccess",true);
            }
        }

        if (song.getFiletype().equals("iOS")) {
            // Run the OnSongConvert script
            convertOnSong.convertTextToTags(c,storageAccess,preferences,processSong,
                    convertChoPro,sqLiteHelper,commonSQL,uri,song);

            // Now read in the proper OpenSong xml file
            try {
                readFileAsXML(c,storageAccess,preferences,processSong,showToast, where, song, uri, utf, indexing);
            } catch (Exception e) {
                Log.d("LoadXML", "Error performing grabOpenSongXML()");
            }
        } else if (song.getFiletype().equals("CHO") || lyricsHaveChoProTags(song.getLyrics())) {
            // Run the ChordProConvert script
            song = convertChoPro.convertTextToTags(c,storageAccess,preferences,processSong,sqLiteHelper,
                    commonSQL, uri, song);

            // Now read in the proper OpenSong xml file
            try {
                readFileAsXML(c,storageAccess,preferences,processSong,showToast,where,song,uri, utf,indexing);
            } catch (Exception e) {
                Log.d("LoadXML", "Error performing grabOpenSongXML()");
            }
        }

        // Fix all the rogue code
        song.setLyrics(processSong.parseLyrics(song.getLyrics(), c));


        // Finally if we aren't indexing, set the static variables to match the SQLite object
        // Also build the XML file back incase we've updated content
        if (!indexing) {
            // Empty the XML
            StaticVariables.myNewXML = "";

            // Check if the song has been loaded (will now have a lyrics value)
            if (!song.getFilename().equals("Welcome to OpenSongApp") && song.getLyrics()!=null && !song.getLyrics().isEmpty()) {
                // Song was loaded correctly and was xml format
                preferences.setMyPreferenceBoolean(c, "songLoadSuccess", true);
                StaticVariables.songfilename = song.getFilename();
                StaticVariables.whichSongFolder = song.getFolder();

            } else {
                StaticVariables.whichSongFolder = c.getResources().getString(R.string.mainfoldername);
                StaticVariables.songfilename = "Welcome to OpenSongApp";
            }

            preferences.setMyPreferenceString(c,"songfilename",StaticVariables.songfilename);
            preferences.setMyPreferenceString(c,"whichSongFolder",StaticVariables.whichSongFolder);
        }
    return song;
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

    private String getUTF(Context c, StorageAccess storageAccess, Preferences preferences, String folder, String filename, String filetype) {
        // Determine the file encoding
        String where = "Songs";
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
        }
        uri = storageAccess.getUriForItem(c, preferences, where, folder, filename);
        if (storageAccess.uriExists(c,uri)) {
            if (filetype.equals("XML") && !StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
                return storageAccess.getUTFEncoding(c, uri);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Song readFileAsXML(Context c, StorageAccess storageAccess, Preferences preferences,
                              ProcessSong processSong, ShowToast showToast, String where, Song song, Uri uri, String utf, boolean indexing)
            throws XmlPullParserException, IOException {

        // Extract all of the key bits of the song
        if (storageAccess.uriIsFile(c,uri)) {
            InputStream inputStream = storageAccess.getInputStream(c, uri);
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
                                song.setAuthor(processSong.parseHTML(xpp.nextText()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Try to read in the xml
                                song.setAuthor(fixXML(c, preferences, storageAccess, showToast, song,"author",where));
                            }
                            break;
                        case "copyright":
                            song.setCopyright(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "title":
                            String testthetitle = processSong.parseHTML(xpp.nextText());
                            if (testthetitle != null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                                song.setTitle(processSong.parseHTML(testthetitle));
                            } else if (testthetitle != null && testthetitle.equals("")) {
                                song.setTitle(song.getFilename());
                            }
                            break;
                        case "lyrics":
                            try {
                                song.setLyrics(processSong.fixStartOfLines(processSong.parseHTML(xpp.nextText())));
                            } catch (Exception e) {
                                // Try to read in the xml
                                e.printStackTrace();
                                song.setLyrics(fixXML(c, preferences, storageAccess,showToast, song,"lyrics",where));
                            }
                            break;
                        case "ccli":
                            song.setCcli(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "theme":
                            song.setTheme(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "alttheme":
                            song.setAlttheme(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "presentation":
                            song.setPresentationorder(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "hymn_number":
                            song.setHymnnum(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "user1":
                            song.setUser1(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "user2":
                            song.setUser2(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "user3":
                            song.setUser3(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "key":
                            song.setKey(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "aka":
                            song.setAka(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "capo":
                            if (xpp.getAttributeCount() > 0) {
                                song.setCapoprint(xpp.getAttributeValue(0));
                            }
                            song.setCapo(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "tempo":
                            song.setMetronomebpm(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "time_sig":
                            song.setTimesig(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "duration":
                            song.setAutoscrolllength(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "predelay":
                            song.setAutoscrolldelay(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "midi":
                            song.setMidi(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "midi_index":
                            song.setMidiindex(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "notes":
                            song.setNotes(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "pad_file":
                            song.setPadfile(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "custom_chords":
                            song.setCustomChords(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "link_youtube":
                            song.setLinkyoutube(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "link_web":
                            song.setLinkweb(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "link_audio":
                            song.setLinkaudio(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "loop_audio":
                            song.setPadloop(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "link_other":
                            song.setLinkother(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "abcnotation":
                            song.setAbc(processSong.parseHTML(xpp.nextText()));
                            break;
                        case "style":
                        case "backgrounds":
                            // Simplest way to get this is to load the file in line by line as asynctask after this
                            needtoloadextra = true;
                            break;
                    }
                }
                // If it isn't an xml file, an error is about to be thrown
                try {
                    eventType = xpp.next();
                    song.setFiletype("XML");
                } catch (Exception e) {
                    Log.d("LoadSong", "Not xml so exiting");
                    eventType = XmlPullParser.END_DOCUMENT;
                    song.setFiletype("?");
                }
            }

            if (song.getFilename().equals("Welcome to OpenSongApp")) {
                song = setNotFound(c);
            }

            // If we really have to load extra stuff, lets do it as an asynctask
            if (needtoloadextra && !indexing) {
                inputStream = storageAccess.getInputStream(c, uri);
                SideTask loadextra = new SideTask(c, inputStream, uri, song.getFilename());
                loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        return song;
    }



    // Deal with the additional mExtraStuff1&2 - only needed if we are editing/saving the song
    @SuppressLint("StaticFieldLeak")
    private static class SideTask extends AsyncTask<String, Void, String> {

        final InputStream inputStream;
        StorageAccess storageAccess;
        String filename;
        final Uri uri;
        final Context c;

        SideTask(Context ctx, InputStream is, Uri u, String filename) {
            inputStream = is;
            c = ctx;
            uri = u;
            this.filename = filename;
        }

        @Override
        protected String doInBackground(String... params) {
            String full_text;
            storageAccess = new StorageAccess();
            try {
                if (validReadableFile(c, storageAccess, uri, filename)) {
                    full_text = storageAccess.readTextFileToString(inputStream);
                } else {
                    full_text = "";
                }
            } catch (Exception e) {
                Log.d("LoadXML", "Error reading text file");
                full_text = "";
            }

            return full_text;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                int style_start = result.indexOf("<style");
                int style_end = result.indexOf("</style>");
                if (style_end > style_start && style_start > -1) {
                    StaticVariables.mExtraStuff1 = result.substring(style_start, style_end + 8);
                }
                int backgrounds_start = result.indexOf("<backgrounds");
                int backgrounds_end = result.indexOf("</backgrounds>");
                if (backgrounds_end < 0) {
                    backgrounds_end = result.indexOf("/>", backgrounds_start) + 2;
                } else {
                    backgrounds_end += 14;
                }
                if (backgrounds_end > backgrounds_start && backgrounds_start > -1) {
                    StaticVariables.mExtraStuff2 = result.substring(backgrounds_start, backgrounds_end);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static boolean validReadableFile(Context c, StorageAccess storageAccess, Uri uri, String filename) {
        boolean isvalid = false;
        // Get length of file in Kb
        float filesize = storageAccess.getFileSizeFromUri(c, uri);
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


    private String fixXML(Context c, Preferences preferences, StorageAccess storageAccess, ShowToast showToast, Song song, String section, String where) {

        // Error in the xml - tell the user we're trying to fix it!
        showToast.doIt(c,c.getString(R.string.fix));
        StringBuilder newXML = new StringBuilder();
        String tofix;
        // If an XML file has unencoded ampersands or quotes, fix them
        try {
            tofix = getSongAsText(c,storageAccess,preferences,where,song.getFolder(),song.getFilename());

            if (tofix.contains("<")) {
                String[] sections = tofix.split("<");
                for (String bit : sections) {
                    // We are going though a section at a time
                    int postofix = bit.indexOf(">");
                    if (postofix >= 0) {
                        String startbit = "<"+bit.substring(0,postofix);
                        String bittofix = doFix(bit.substring(postofix));
                        newXML.append(startbit).append(bittofix);
                    }
                }
            } else {
                newXML.append(tofix);
            }

            // Now save the song again
            OutputStream outputStream = storageAccess.getOutputStream(c,uri);
            storageAccess.writeFileFromString(newXML.toString(),outputStream);

            // Try to extract the section we need
            if (newXML.toString().contains("<"+section+">") && newXML.toString().contains("</"+section+">")) {
                int start = newXML.indexOf("<"+section+">") + 2 + section.length();
                int end = newXML.indexOf("</"+section+">");
                song.setFiletype("XML");
                return newXML.substring(start,end);
            } else {
                song.setFiletype("?");
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

    private Song setNotFound(Context c) {
        Song song = new Song();
        StaticVariables.songfilename = "Welcome to OpenSongApp";
        song.setFilename("Welcome to OpenSongApp");
        song.setTitle("Welcome to OpenSongApp");
        song.setLyrics(c.getString(R.string.user_guide_lyrics));
        song.setAuthor("Gareth Evans");
        song.setKey("G");
        song.setLinkweb(c.getString(R.string.website));
        return song;
    }

    private String getSongAsText(Context c, StorageAccess storageAccess, Preferences preferences, String where, String folder, String filename) {
        Uri uri = storageAccess.getUriForItem(c,preferences,where, folder,filename);
        InputStream inputStream = storageAccess.getInputStream(c,uri);
        String s = storageAccess.readTextFileToString(inputStream);
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

    public String grabNextSongInSetKey(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, String nextsong) {
        String nextkey = "";

        // Get the android version
        boolean nextisxml = true;
        if (nextsong.toLowerCase(Locale.ROOT).endsWith(".pdf") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".doc") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".docx") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".jpg") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".jpeg") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".png") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".gif") ||
                nextsong.toLowerCase(Locale.ROOT).endsWith(".bmp")) {
            nextisxml = false;
        }

        String nextutf = null;

        Uri uri = null;
        String subfolder = "";
        if (nextisxml) {
            if (nextsong.contains("**") || nextsong.contains("../")) {
                subfolder = nextsong;
                nextsong = "";
            }
            uri = storageAccess.getUriForItem(c, preferences, "Songs", subfolder, nextsong);
            nextutf = storageAccess.getUTFEncoding(c, uri);
        }

        try {
            if (nextisxml && nextutf != null && !nextutf.equals("")) {
                // Extract all of the key bits of the song
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                nextkey = "";

                InputStream inputStream = storageAccess.getInputStream(c, uri);
                if (inputStream != null) {
                    xpp.setInput(inputStream, nextutf);

                    int eventType;
                    eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("key")) {
                                nextkey = processSong.parseFromHTMLEntities(xpp.nextText());
                            }
                        }
                        try {
                            eventType = xpp.next();
                        } catch (Exception e) {
                            //Ooops!
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("LoadXML","Error trying to read XML from "+uri);
            // Ooops
        }

        return nextkey;
    }

}
