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
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

public class LoadSong {

    private boolean isXML, isPDF, isIMG, isCustom, needtoloadextra = false;
    private String filetype, utf, where = "Songs", folder, origfolder;
    private Uri uri;

    public void doLoadSong(Context c, StorageAccess storageAccess, Preferences preferences,
                           SongXML songXML, ProcessSong processSong, ShowToast showToast,
                           SQLiteHelper sqLiteHelper, ConvertOnSong convertOnSong,
                           ConvertChoPro convertChoPro) {
        // Set the song load status to false (helps check if it didn't load).  This is set to true after success
        preferences.setMyPreferenceBoolean(c,"songLoadSuccess",false);

        // Empty the XML
        StaticVariables.myNewXML = "";

        // First up, determine the type of song we have.
        // The best songs are xml (OpenSong formatted).
        filetype = getFileTypeByExtension();

        // Get the file encoding (this also tests that the file exists)
        utf = getUTF(c,storageAccess,preferences);

        // Initialise the Song tags
        songXML.initialiseSongTags();

        // Try to load the song as an xml
        boolean readAsXML = false;
        if (filetype.equals("SONG") && !StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
            // This returns true if successful, false if not.
            try {
                readAsXML = readFileAsXML(c, storageAccess, preferences, processSong, showToast);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        } else if (StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
            songXML.showWelcomeSong(c);
            isXML = true;
            readAsXML = true;
        }

        if (isXML && readAsXML) {
            // Song was loaded correctly and was xml format
            preferences.setMyPreferenceBoolean(c, "songLoadSuccess", true);
            preferences.setMyPreferenceString(c, "songfilename", StaticVariables.songfilename);
            if (isCustom) {
                StaticVariables.whichSongFolder = origfolder;
            } else {
                StaticVariables.whichSongFolder = folder;
            }
            preferences.setMyPreferenceString(c,"whichSongFolder",StaticVariables.whichSongFolder);
        } else {
            Log.d("LoadSong", "Song wasn't loaded");
        }

        if (StaticVariables.mLyrics == null || StaticVariables.mLyrics.isEmpty()) {
            isXML = false;
        }

        // If the file wasn't read as an xml file, we need to deal with it in another way
        if ((!readAsXML || !isXML) && !isIMG && !isPDF) {
            StaticVariables.myNewXML = getSongAsText(c,storageAccess,preferences);
            StaticVariables.mLyrics = StaticVariables.myNewXML;
            StaticVariables.mTitle = StaticVariables.songfilename;
            if (StaticVariables.mLyrics!=null && !StaticVariables.mLyrics.isEmpty()) {
                preferences.setMyPreferenceBoolean(c,"songLoadSuccess",true);
                preferences.setMyPreferenceString(c,"songfilename",StaticVariables.songfilename);
                preferences.setMyPreferenceString(c,"whichSongFolder",StaticVariables.whichSongFolder);
            }
        }

        // If the song is OnSong or ChordPro format - try to import it
        if (!readAsXML && StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".onsong")) {
            // Run the OnSongConvert script
            convertOnSong.convertTextToTags(c,storageAccess,preferences,processSong, songXML,
                    convertChoPro,sqLiteHelper,uri,StaticVariables.myNewXML);

            // Now read in the proper OpenSong xml file
            try {
                readFileAsXML(c,storageAccess,preferences,processSong,showToast);
            } catch (Exception e) {
                Log.d("LoadXML", "Error performing grabOpenSongXML()");
            }

        } else if (!readAsXML && isChoPro()) {
            // Run the ChordProConvert script
            convertChoPro.convertTextToTags(c,storageAccess,preferences,processSong,sqLiteHelper,
                    songXML,uri,StaticVariables.myNewXML);

            // Now read in the proper OpenSong xml file
            try {
                readFileAsXML(c, storageAccess,preferences,processSong,showToast);
            } catch (Exception e) {
                Log.d("LoadXML", "Error performing grabOpenSongXML()");
            }
        }

        // Fix all the rogue code
        StaticVariables.mLyrics = processSong.parseLyrics(StaticVariables.mLyrics, c);

        // Just in case we have improved the song, prepare the improved xml
        songXML.getXML(processSong);
    }

    private boolean isChoPro() {
        return StaticVariables.myNewXML.contains("{title") ||
                StaticVariables.myNewXML.contains("{t:") ||
                StaticVariables.myNewXML.contains("{t :") ||
                StaticVariables.myNewXML.contains("{subtitle") ||
                StaticVariables.myNewXML.contains("{st:") ||
                StaticVariables.myNewXML.contains("{st :") ||
                StaticVariables.myNewXML.contains("{comment") ||
                StaticVariables.myNewXML.contains("{c:") ||
                StaticVariables.myNewXML.contains("{new_song") ||
                StaticVariables.myNewXML.contains("{ns") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".pro") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".chopro") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".chordpro");
    }

    private String getFileTypeByExtension() {
        String filetype = "SONG";
        isXML = true;
        if (StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            isPDF = true;
            filetype = "PDF";
            StaticVariables.fileType="PDF";
            isXML = false;
        } else if (StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".doc") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            filetype = "DOC";
            StaticVariables.fileType="DOC";
            isXML = false;
        } else if (StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".jpg") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".jpeg") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".png") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".gif") ||
                StaticVariables.songfilename.toLowerCase(Locale.ROOT).endsWith(".bmp")) {
            filetype = "IMG";
            StaticVariables.fileType="IMG";
            isIMG = true;
            isXML = false;
        }
        return filetype;
    }

    private String getUTF(Context c, StorageAccess storageAccess, Preferences preferences) {
        // Determine the file encoding
        where = "Songs";
        folder = StaticVariables.whichSongFolder;
        origfolder = StaticVariables.whichSongFolder;
        isCustom = false;
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
            isCustom = true;
            where = "";
        }
        uri = storageAccess.getUriForItem(c, preferences, where, folder, StaticVariables.songfilename);
        if (storageAccess.uriExists(c,uri)) {
            if (filetype.equals("SONG") && !StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
                utf = storageAccess.getUTFEncoding(c, uri);
            } else {
                utf = null;
            }
        } else {
            utf = null;
        }
        return utf;
    }

    private boolean readFileAsXML(Context c, StorageAccess storageAccess, Preferences preferences,
                                  ProcessSong processSong, ShowToast showToast) throws XmlPullParserException, IOException {
        boolean success = false;
        // Extract all of the key bits of the song
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        if (storageAccess.uriIsFile(c,uri)) {
            InputStream inputStream = storageAccess.getInputStream(c, uri);
            InputStreamReader lineReader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(lineReader);

            isXML = false;

            String line;
            try {
                line = buffreader.readLine();
                if (line.contains("<?xml")) {
                    isXML = true;
                }
                if (line.contains("encoding=\"")) {
                    int startpos = line.indexOf("encoding=\"") + 10;
                    int endpos = line.indexOf("\"", startpos);
                    String enc = line.substring(startpos, endpos);
                    if (enc.length() > 0) {
                        utf = enc.toUpperCase();
                    }
                }
            } catch (Exception e) {
                utf = "UTF-8";
            }

            // Keep a note of this encoding incase we resave the song!
            StaticVariables.mEncoding = utf;

            // read every line of the file into the line-variable, on line at the time
            inputStream.close();
            inputStream = storageAccess.getInputStream(c, uri);

            xpp.setInput(inputStream, utf);

            int eventType;
            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xpp.getName()) {
                        case "author":
                            try {
                                StaticVariables.mAuthor = processSong.parseHTML(xpp.nextText());
                                isXML = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Try to read in the xml
                                StaticVariables.mAuthor = fixXML(c, preferences, storageAccess, showToast, "author");
                            }
                            break;
                        case "copyright":
                            StaticVariables.mCopyright = processSong.parseHTML(xpp.nextText());
                            isXML = true;
                            break;
                        case "title":
                            String testthetitle = processSong.parseHTML(xpp.nextText());
                            if (testthetitle != null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                                StaticVariables.mTitle = processSong.parseHTML(testthetitle);
                            } else if (testthetitle != null && testthetitle.equals("")) {
                                StaticVariables.mTitle = StaticVariables.songfilename;
                            }
                            isXML = true;
                            break;
                        case "lyrics":
                            try {
                                StaticVariables.mLyrics = processSong.fixStartOfLines(processSong.parseHTML(xpp.nextText()));
                            } catch (Exception e) {
                                // Try to read in the xml
                                e.printStackTrace();
                                StaticVariables.mLyrics = fixXML(c, preferences, storageAccess,showToast,"lyrics");
                            }
                            break;
                        case "ccli":
                            StaticVariables.mCCLI = processSong.parseHTML(xpp.nextText());
                            break;
                        case "theme":
                            StaticVariables.mTheme = processSong.parseHTML(xpp.nextText());
                            break;
                        case "alttheme":
                            StaticVariables.mAltTheme = processSong.parseHTML(xpp.nextText());
                            break;
                        case "presentation":
                            StaticVariables.mPresentation = processSong.parseHTML(xpp.nextText());
                            break;
                        case "hymn_number":
                            StaticVariables.mHymnNumber = processSong.parseHTML(xpp.nextText());
                            break;
                        case "user1":
                            StaticVariables.mUser1 = processSong.parseHTML(xpp.nextText());
                            break;
                        case "user2":
                            StaticVariables.mUser2 = processSong.parseHTML(xpp.nextText());
                            break;
                        case "user3":
                            StaticVariables.mUser3 = processSong.parseHTML(xpp.nextText());
                            break;
                        case "key":
                            StaticVariables.mKey = processSong.parseHTML(xpp.nextText());
                            break;
                        case "aka":
                            StaticVariables.mAka = processSong.parseHTML(xpp.nextText());
                            break;
                        case "key_line":
                            StaticVariables.mKeyLine = processSong.parseHTML(xpp.nextText());
                            break;
                        case "capo":
                            if (xpp.getAttributeCount() > 0) {
                                StaticVariables.mCapoPrint = xpp.getAttributeValue(0);
                            }
                            StaticVariables.mCapo = processSong.parseHTML(xpp.nextText());
                            break;
                        case "tempo":
                            StaticVariables.mTempo = processSong.parseHTML(xpp.nextText());
                            break;
                        case "time_sig":
                            StaticVariables.mTimeSig = processSong.parseHTML(xpp.nextText());
                            break;
                        case "duration":
                            StaticVariables.mDuration = processSong.parseHTML(xpp.nextText());
                            break;
                        case "predelay":
                            StaticVariables.mPreDelay = processSong.parseHTML(xpp.nextText());
                            break;
                        case "books":
                            StaticVariables.mBooks = processSong.parseHTML(xpp.nextText());
                            break;
                        case "midi":
                            StaticVariables.mMidi = processSong.parseHTML(xpp.nextText());
                            break;
                        case "midi_index":
                            StaticVariables.mMidiIndex = processSong.parseHTML(xpp.nextText());
                            break;
                        case "pitch":
                            StaticVariables.mPitch = processSong.parseHTML(xpp.nextText());
                            break;
                        case "restrictions":
                            StaticVariables.mRestrictions = processSong.parseHTML(xpp.nextText());
                            break;
                        case "notes":
                            StaticVariables.mNotes = processSong.parseHTML(xpp.nextText());
                            break;
                        case "linked_songs":
                            StaticVariables.mLinkedSongs = processSong.parseHTML(xpp.nextText());
                            break;
                        case "pad_file":
                            StaticVariables.mPadFile = processSong.parseHTML(xpp.nextText());
                            break;
                        case "custom_chords":
                            StaticVariables.mCustomChords = processSong.parseHTML(xpp.nextText());
                            break;
                        case "link_youtube":
                            StaticVariables.mLinkYouTube = processSong.parseHTML(xpp.nextText());
                            break;
                        case "link_web":
                            StaticVariables.mLinkWeb = processSong.parseHTML(xpp.nextText());
                            break;
                        case "link_audio":
                            StaticVariables.mLinkAudio = processSong.parseHTML(xpp.nextText());
                            break;
                        case "loop_audio":
                            StaticVariables.mLoopAudio = processSong.parseHTML(xpp.nextText());
                            break;
                        case "link_other":
                            StaticVariables.mLinkOther = processSong.parseHTML(xpp.nextText());
                            break;
                        case "abcnotation":
                            StaticVariables.mNotation = processSong.parseHTML(xpp.nextText());
                            break;
                        case "style":
                        case "backgrounds":
                            //FullscreenActivity.mExtraStuff2 = xpp.nextText();
                            // Simplest way to get this is to load the file in line by line as asynctask
                            needtoloadextra = true;
                            break;
                    }
                }
                // If it isn't an xml file, an error is about to be thrown
                try {
                    eventType = xpp.next();
                    success = true;
                    StaticVariables.fileType="XML";
                } catch (Exception e) {
                    Log.d("LoadSong", "Not xml so exiting");
                    eventType = XmlPullParser.END_DOCUMENT;
                }
            }

            if (StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
                setNotFound(c);
            }

            // If we really have to load extra stuff, lets do it as an asynctask
            if (needtoloadextra) {
                inputStream = storageAccess.getInputStream(c, uri);
                SideTask loadextra = new SideTask(c, inputStream, uri);
                loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        return success;
    }

    private static boolean validReadableFile(Context c, StorageAccess storageAccess, Uri uri) {
        boolean isvalid = false;
        // Get length of file in Kb
        float filesize = storageAccess.getFileSizeFromUri(c, uri);
        String filename = StaticVariables.songfilename;
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

    @SuppressLint("StaticFieldLeak")
    private static class SideTask extends AsyncTask<String, Void, String> {

        final InputStream inputStream;
        StorageAccess storageAccess;
        final Uri uri;
        final Context c;

        SideTask(Context ctx, InputStream is, Uri u) {
            inputStream = is;
            c = ctx;
            uri = u;
        }

        @Override
        protected String doInBackground(String... params) {
            String full_text;
            storageAccess = new StorageAccess();
            try {
                if (validReadableFile(c, storageAccess, uri)) {
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
    private String fixXML(Context c, Preferences preferences, StorageAccess storageAccess, ShowToast showToast, String section) {

        // Error in the xml - tell the user we're trying to fix it!
        showToast.doIt(c,c.getString(R.string.fix));
        StringBuilder newXML = new StringBuilder();
        String tofix;
        // If an XML file has unencoded ampersands or quotes, fix them
        try {
            tofix = getSongAsText(c,storageAccess,preferences);

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
                isXML = true;
                return newXML.substring(start,end);
            } else {
                isXML = false;
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

    private void setNotFound(Context c) {
        StaticVariables.mAuthor = "Gareth Evans";
        StaticVariables.mLinkWeb = "https://www.opensongapp,com";
        StaticVariables.mTitle = "Welcome to OpenSongApp";
        if (StaticVariables.songfilename.equals("Welcome to OpenSongApp")) {
            StaticVariables.mLyrics = c.getString(R.string.user_guide_lyrics);
        } else {
            StaticVariables.mLyrics = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename +
                    c.getString(R.string.nofound);
        }

    }

    private String getSongAsText(Context c, StorageAccess storageAccess, Preferences preferences) {
        Uri uri = storageAccess.getUriForItem(c,preferences,where, folder,StaticVariables.songfilename);
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
