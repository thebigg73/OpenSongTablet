package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

public class LoadXML extends Activity {

    private static String utf = "UTF-8";

    // This bit loads the lyrics from the required file
    static void loadXML(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong) throws IOException {

        FullscreenActivity.isSong = false;
        FullscreenActivity.isPDF = false;
        FullscreenActivity.isImage = false;
        FullscreenActivity.isSlide = false;
        FullscreenActivity.isScripture = false;
        FullscreenActivity.isImageSlide = false;

        // Clear the heading default
        StaticVariables.songSection_holder = "";

        // Set the song load status to false (helps check if it didn't load)
        preferences.setMyPreferenceBoolean(c,"songLoadSuccess",false);

        String where = "Songs";
        String folder = StaticVariables.whichSongFolder;
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
            where = "";
        }

        Uri uri = storageAccess.getUriForItem(c, preferences, where, folder,
                StaticVariables.songfilename);

        if (StaticVariables.songfilename.equals("") ||
                StaticVariables.songfilename.equals("Welcome to OpenSongApp") ||
                !storageAccess.uriExists(c, uri)) {
            FullscreenActivity.isSong = true;
            if (StaticVariables.songfilename.equals("Welcome to OpenSongApp") &&
                    ((StaticVariables.whichMode.equals("Performance") || StaticVariables.whichMode.equals("Stage")))) {
                setWelcome(c);
            } else {
                setNotFound(c);
            }
        } else {
            // IV - We have a file to load
            String songfilename = StaticVariables.songfilename.toLowerCase(Locale.ROOT);
            if (songfilename.endsWith(".pdf")) {
                FullscreenActivity.isPDF = true;
            } else if (songfilename.endsWith(".jpg") ||
                    songfilename.endsWith(".jpeg") ||
                    songfilename.endsWith(".png") ||
                    songfilename.endsWith(".gif") ||
                    songfilename.endsWith(".bmp")) {
                FullscreenActivity.isImage = true;
            } else {
                FullscreenActivity.isSong = true;
            }

            if (FullscreenActivity.isSong) {
                // Determine the file encoding
                utf = storageAccess.getUTFEncoding(c, uri);

                try {
                    // Try to read the file as OpenSong xml. If it isn't, read as text and maybe convert to and open as OpenSong xml!
                    if (!StaticVariables.songfilename.endsWith(".sqlite3") && !StaticVariables.songfilename.endsWith(".preferences")) {
                        grabOpenSongXML(c, preferences, storageAccess);
                    }

                    if (StaticVariables.mLyrics == null || StaticVariables.mLyrics.isEmpty()) {
                        // If the file hasn't been read properly, or mLyrics is empty, read it in as a text file
                        InputStream inputStream = storageAccess.getInputStream(c, uri);
                        InputStreamReader streamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(streamReader);
                        if (validReadableFile(c, storageAccess, uri)) {
                            FullscreenActivity.myXML = storageAccess.readTextFileToString(inputStream);
                        } else {
                            FullscreenActivity.myXML = "";
                        }
                        StaticVariables.mLyrics = FullscreenActivity.myXML;
                        inputStream.close();
                        streamReader.close();
                        bufferedReader.close();

                        // If the song is OnSong format - try to import it
                        if (StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".onsong")) {
                            // Run the OnSongConvert script
                            OnSongConvert onSongConvert = new OnSongConvert();
                            SongXML songXML = new SongXML();
                            ChordProConvert chordProConvert = new ChordProConvert();
                            // TODO check this works
                            onSongConvert.convertTextToTags(c, storageAccess, preferences, songXML, chordProConvert, uri, FullscreenActivity.myXML);
                            grabOpenSongXML(c, preferences, storageAccess);

                            // If the song is usr format - try to import it
                        } else if (StaticVariables.songfilename.contains(".usr")
                                || FullscreenActivity.myXML.contains("[File]")
                                || FullscreenActivity.myXML.contains("Type=")
                                || FullscreenActivity.myXML.contains("Words=")) {
                            // Run the UsrConvert script
                            UsrConvert usrConvert = new UsrConvert();
                            SongXML songXML = new SongXML();
                            ChordProConvert chordProConvert = new ChordProConvert();
                            usrConvert.convertTextToTags(c, storageAccess, preferences, songXML, chordProConvert, uri, FullscreenActivity.myXML);
                            grabOpenSongXML(c, preferences, storageAccess);

                            // If the song is in ChordPro format - try to import it
                        } else if (FullscreenActivity.myXML.contains("{title") ||
                                FullscreenActivity.myXML.contains("{t:") ||
                                FullscreenActivity.myXML.contains("{t :") ||
                                FullscreenActivity.myXML.contains("{subtitle") ||
                                FullscreenActivity.myXML.contains("{st:") ||
                                FullscreenActivity.myXML.contains("{st :") ||
                                FullscreenActivity.myXML.contains("{comment") ||
                                FullscreenActivity.myXML.contains("{c:") ||
                                FullscreenActivity.myXML.contains("{new_song") ||
                                FullscreenActivity.myXML.contains("{ns") ||
                                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".pro") ||
                                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".chopro") ||
                                StaticVariables.songfilename.toLowerCase(Locale.ROOT).contains(".chordpro")) {
                            // Run the ChordProConvert script
                            ChordProConvert chordProConvert = new ChordProConvert();
                            SongXML songXML = new SongXML();
                            //TODO check this works
                            chordProConvert.convertTextToTags(c, storageAccess, preferences, songXML, uri, FullscreenActivity.myXML);
                            grabOpenSongXML(c, preferences, storageAccess);
                            // If it is not a supported file type
                        } else if (!storageAccess.checkFileExtensionValid(uri)) {
                            FullscreenActivity.myXML = "";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (FullscreenActivity.myXML != null && !FullscreenActivity.myXML.isEmpty()) {
                    // Fix all the rogue code
                    StaticVariables.mLyrics = processSong.parseLyrics(StaticVariables.mLyrics, c);

                    // Always prepare xml (needed for nearby)
                    PopUpEditSongFragment.prepareSongXML();
                    FullscreenActivity.myXML = FullscreenActivity.mynewXML;

                    // Write what is left to the mLyrics field just incase the file is badly formatted
                    if (!FullscreenActivity.myXML.contains("<lyrics")) {
                        StaticVariables.mLyrics = processSong.fixStartOfLines(FullscreenActivity.myXML);
                    }

                    StaticVariables.mTempo = StaticVariables.mTempo
                            .replace("Very Fast", "140")
                            .replace("Fast", "120")
                            .replace("Moderate", "100")
                            .replace("Slow", "80")
                            .replace("Very Slow", "60")
                            .replace("[\\D]", "");

                    if (!StaticVariables.mDuration.isEmpty()) {
                        try {
                            StaticVariables.autoScrollDuration = Integer.parseInt(StaticVariables.mDuration.replaceAll("[\\D]", ""));
                        } catch (Exception e) {
                            StaticVariables.autoScrollDuration = -1;
                        }
                    }

                    if (!StaticVariables.mPreDelay.isEmpty()) {
                        try {
                            StaticVariables.autoScrollDelay = Integer.parseInt(StaticVariables.mPreDelay.replaceAll("[\\D]", ""));
                        } catch (Exception e) {
                            StaticVariables.autoScrollDelay = -1;
                        }
                    }

                    // Set the song load status to true:
                    preferences.setMyPreferenceBoolean(c, "songLoadSuccess", true);
                } else {
                    if (!storageAccess.checkFileExtensionValid(uri)) {
                        setUnknown(c);
                    } else {
                        setNotFound(c);
                    }
                    Log.d("LoadXML", "Song wasn't loaded");
                }
            } else {
                // Initialise all the xml tags a song should have
                initialiseSongTags();

                // Try to load in any details from the NonOpenSongDatabase
                try {
                    NonOpenSongSQLite nonOpenSongSQLite;
                    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(c);
                    String songid = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
                    nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(c, storageAccess, preferences, songid);
                    if (nonOpenSongSQLite == null) {
                        nonOpenSongSQLiteHelper.createBasicSong(c, storageAccess, preferences, StaticVariables.whichSongFolder, StaticVariables.songfilename);
                        nonOpenSongSQLite = nonOpenSongSQLiteHelper.getSong(c, storageAccess, preferences, songid);
                    }
                    updateNonOpenSongDetails(nonOpenSongSQLite);
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        // IV - Presentation handles various special content. These are handled as song with content for other modes.
        if (StaticVariables.whichSongFolder!=null && StaticVariables.whichMode.equals("Presentation")) {
            String loc;
            loc = StaticVariables.whichSongFolder;
            FullscreenActivity.isImageSlide = loc.contains("../Images");
            FullscreenActivity.isScripture = loc.contains("../Scripture");
            FullscreenActivity.isSlide = loc.contains("../Slides");
        }

        StaticVariables.thisSongScale = preferences.getMyPreferenceString(c,"songAutoScale","W");
    }

    private static void updateNonOpenSongDetails(NonOpenSongSQLite nonOpenSongSQLite) {
        if (nonOpenSongSQLite!=null) {
            StaticVariables.mAka = nonOpenSongSQLite.getAka();
            StaticVariables.mAltTheme = nonOpenSongSQLite.getAlttheme();
            StaticVariables.mAuthor = nonOpenSongSQLite.getAuthor();
            StaticVariables.mPreDelay = nonOpenSongSQLite.getAutoscrolldelay();
            StaticVariables.mDuration = nonOpenSongSQLite.getAutoscrollLength();
            StaticVariables.mCCLI = nonOpenSongSQLite.getCcli();
            StaticVariables.mCopyright = nonOpenSongSQLite.getCopyright();
            StaticVariables.mHymnNumber = nonOpenSongSQLite.getHymn_num();
            StaticVariables.mKey = nonOpenSongSQLite.getKey();
            StaticVariables.mLyrics = nonOpenSongSQLite.getLyrics();
            StaticVariables.mTempo = nonOpenSongSQLite.getMetronomebpm();
            StaticVariables.mTimeSig = nonOpenSongSQLite.getMetronomeSig();
            StaticVariables.mTheme = nonOpenSongSQLite.getTheme();
            StaticVariables.mTitle = nonOpenSongSQLite.getTitle();
            StaticVariables.mUser1 = nonOpenSongSQLite.getUser1();
            StaticVariables.mUser2 = nonOpenSongSQLite.getUser2();
            StaticVariables.mUser3 = nonOpenSongSQLite.getUser3();
            StaticVariables.mPadFile = nonOpenSongSQLite.getPadfile();
            StaticVariables.mMidi = nonOpenSongSQLite.getMidi();
            StaticVariables.mMidiIndex = nonOpenSongSQLite.getMidiindex();
            StaticVariables.mCapo = nonOpenSongSQLite.getCapo();
            StaticVariables.mNotes = nonOpenSongSQLite.getNotes();
            StaticVariables.mNotation = nonOpenSongSQLite.getAbc();
            StaticVariables.mLinkYouTube = nonOpenSongSQLite.getLinkyoutube();
            StaticVariables.mLinkWeb = nonOpenSongSQLite.getLinkweb();
            StaticVariables.mLinkAudio = nonOpenSongSQLite.getLinkaudio();
            StaticVariables.mLinkOther = nonOpenSongSQLite.getLinkother();
            StaticVariables.mPresentation = nonOpenSongSQLite.getPresentationorder();

            if (!StaticVariables.mDuration.isEmpty()) {
                try {
                    StaticVariables.autoScrollDuration = Integer.parseInt(StaticVariables.mDuration.replaceAll("[\\D]", ""));
                } catch (Exception e) {
                    StaticVariables.autoScrollDuration = -1;
                }
            }

            if (!StaticVariables.mPreDelay.isEmpty()) {
                try {
                    StaticVariables.autoScrollDelay = Integer.parseInt(StaticVariables.mPreDelay.replaceAll("[\\D]", ""));
                } catch (Exception e) {
                    StaticVariables.autoScrollDelay = -1;
                }
            }
        }
    }

    private static void setNotFound(Context c) {
        StaticVariables.thisSongScale = "W";
        initialiseSongTags();
        // IV - 2 sections for presentation and one for Performance and Stage
        if (StaticVariables.whichMode.equals("Presentation")) {
            StaticVariables.mLyrics = "[" + StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename + "]\n\n _\n " +
                    c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        } else {
            StaticVariables.mLyrics = "[" + StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename + "]\n _\n " +
                    c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        }
        FullscreenActivity.myXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<title>Welcome to OpenSongApp</title>\n" +
                "<author>Gareth Evans</author>\n<lyrics>" +
                "<aka>ERROR!</aka>" +
                StaticVariables.mLyrics + "</lyrics>";
        StaticVariables.currentSection = 0;
        FullscreenActivity.pdfPageCurrent = 0;
    }

    private static void setUnknown(Context c) {
        StaticVariables.thisSongScale = "W";
        initialiseSongTags();
        // IV - 2 sections for presentation and one for Performance and Stage
        if (StaticVariables.whichMode.equals("Presentation")) {
            StaticVariables.mLyrics = "[" + StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename + "]\n\n _\n " +
                    c.getResources().getString(R.string.file_type) + ": " + c.getResources().getString(R.string.file_type_unknown) + "\n\n";
        } else {
            StaticVariables.mLyrics = "[" + StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename + "]\n _\n " +
                    c.getResources().getString(R.string.file_type) + ": " + c.getResources().getString(R.string.file_type_unknown) + "\n\n";
        }
        FullscreenActivity.myXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<title>Welcome to OpenSongApp</title>\n" +
                "<author>Gareth Evans</author>\n<lyrics>" +
                "<aka>ERROR!</aka>" +
                StaticVariables.mLyrics + "</lyrics>";
        StaticVariables.currentSection = 0;
        FullscreenActivity.pdfPageCurrent = 0;
    }

    private static void setWelcome(Context c) {
        StaticVariables.thisSongScale = "W";
        initialiseSongTags();
        StaticVariables.mTitle = "Welcome to OpenSongApp";
        StaticVariables.mAuthor = "Gareth Evans";
        StaticVariables.mLinkWeb = "http://www.opensongapp.com";
        StaticVariables.mCopyright = "1996 NewLife Music Limited";
        StaticVariables.mTimeSig = "4/4";
        StaticVariables.mKey = "G";
        StaticVariables.mTempo = "72";
        StaticVariables.mDuration = "100";
        StaticVariables.mPreDelay = "10";
        StaticVariables.mLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.myXML = "<?xml><song><title>" + StaticVariables.mTitle + "</title>\n" +
                "<author>" + StaticVariables.mAuthor + "</author>\n" +
                "<lyrics>" + StaticVariables.mLyrics + "\n</lyrics></song>";
        StaticVariables.currentSection = 0;
        FullscreenActivity.pdfPageCurrent = 0;
    }

    static void prepareLoadCustomReusable(Context c, Preferences preferences,
                                          StorageAccess storageAccess, ProcessSong processSong, String what) {

        String temp_myXML = FullscreenActivity.myXML;
        String temp_songfilename = StaticVariables.songfilename;
        String temp_whichSongFolder = StaticVariables.whichSongFolder;
        String temp_mTitle = StaticVariables.mTitle;
        String temp_mAuthor = StaticVariables.mAuthor;
        String temp_mUser1 = StaticVariables.mUser1;
        String temp_mUser2 = StaticVariables.mUser2;
        String temp_mUser3 = StaticVariables.mUser3;
        String temp_mAka = StaticVariables.mAka;
        String temp_mKeyLine = StaticVariables.mKeyLine;
        String temp_mHymnNumber = StaticVariables.mHymnNumber;
        String temp_mLyrics = StaticVariables.mLyrics;

        String[] tempfile = what.split("/");
        if (tempfile.length>0) {
            StaticVariables.songfilename = tempfile[tempfile.length - 1];
        }
        // Get the new whichSongFolder
        if (what.contains(c.getResources().getString(R.string.note)+"/")) {
            StaticVariables.whichSongFolder = "../Notes";
            FullscreenActivity.whattodo = "customreusable_note";
        } else if (what.contains(c.getResources().getString(R.string.slide)+"/")) {
            StaticVariables.whichSongFolder = "../Slides";
            FullscreenActivity.whattodo = "customreusable_slide";
        } else if (what.contains(c.getResources().getString(R.string.scripture)+"/")) {
            StaticVariables.whichSongFolder = "../Scripture";
            FullscreenActivity.whattodo = "customreusable_scripture";
        } else if (what.contains(c.getResources().getString(R.string.image)+"/")) {
            StaticVariables.whichSongFolder = "../Images";
            FullscreenActivity.whattodo = "customreusable_image";
        } else if (what.contains(c.getResources().getString(R.string.variation)+"/")) {
            StaticVariables.whichSongFolder = "../Variations";
            FullscreenActivity.whattodo = "customreusable_variation";
        }

        // Load up the XML
        try {
            loadXML(c, preferences, storageAccess, processSong);
        } catch (Exception e) {
            Log.d("LoadXML", "Error performing loadXML()");
        }

        // Put the values in
        FullscreenActivity.customslide_title = StaticVariables.mTitle;
        FullscreenActivity.customimage_time = StaticVariables.mUser1;
        FullscreenActivity.customimage_loop = StaticVariables.mUser2;
        FullscreenActivity.customimage_list = StaticVariables.mUser3;
        FullscreenActivity.customslide_content = StaticVariables.mLyrics;

        // Reset the main song variables back to their former glory
        FullscreenActivity.myXML = temp_myXML;
        StaticVariables.songfilename = temp_songfilename;
        StaticVariables.whichSongFolder = temp_whichSongFolder;
        preferences.setMyPreferenceString(c,"whichSongFolder",temp_whichSongFolder);
        StaticVariables.mTitle = temp_mTitle;
        StaticVariables.mAuthor = temp_mAuthor;
        StaticVariables.mUser1 = temp_mUser1;
        StaticVariables.mUser2 = temp_mUser2;
        StaticVariables.mUser3 = temp_mUser3;
        StaticVariables.mAka = temp_mAka;
        StaticVariables.mKeyLine = temp_mKeyLine;
        StaticVariables.mHymnNumber = temp_mHymnNumber;
        StaticVariables.mLyrics = temp_mLyrics;
    }

    private static void initialiseSongTags() {
        StaticVariables.mTitle = StaticVariables.songfilename;
        StaticVariables.mAuthor = "";
        StaticVariables.mCopyright = "";
        StaticVariables.mPresentation = "";
        StaticVariables.mHymnNumber = "";
        StaticVariables.mCapo = "";
        StaticVariables.mCapoPrint = "false";
        StaticVariables.mTempo = "";
        StaticVariables.mTimeSig = "";
        StaticVariables.mDuration = "";
        StaticVariables.mPreDelay = "";
        StaticVariables.mCCLI = "";
        StaticVariables.mTheme = "";
        StaticVariables.mAltTheme = "";
        StaticVariables.mUser1 = "";
        StaticVariables.mUser2 = "";
        StaticVariables.mUser3 = "";
        StaticVariables.mKey = "";
        StaticVariables.mAka = "";
        StaticVariables.mKeyLine = "";
        StaticVariables.mBooks = "";
        StaticVariables.mMidi = "";
        StaticVariables.mMidiIndex = "";
        StaticVariables.mPitch = "";
        StaticVariables.mRestrictions = "";
        StaticVariables.mNotes = "";
        StaticVariables.mLyrics = "";
        StaticVariables.mStyle = "";
        StaticVariables.mLinkedSongs = "";
        StaticVariables.mPadFile = "";
        StaticVariables.mCustomChords = "";
        StaticVariables.mLinkYouTube = "";
        StaticVariables.mLinkWeb = "";
        StaticVariables.mLinkAudio = "";
        StaticVariables.mLoopAudio = "false";
        StaticVariables.mLinkOther = "";
        StaticVariables.mExtraStuff1 = "";
        StaticVariables.mExtraStuff2 = "";
        StaticVariables.mNotation = "";
        StaticVariables.mEncoding = "UTF-8";
        StaticVariables.mAka = "";
   }

    private static void grabOpenSongXML(Context c, Preferences preferences, StorageAccess storageAccess) throws Exception {
        // Extract all of the key bits of the song
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        initialiseSongTags();
        boolean needToLoadExtra = false;  // Will be set to true if backgrounds/styles found.

        // Get the uri and stream of the file
        String where = "Songs";
        String folder = StaticVariables.whichSongFolder;
        if (StaticVariables.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
            where = "";
        }

        Uri uri = storageAccess.getUriForItem(c, preferences, where, folder,
                StaticVariables.songfilename);

        if (storageAccess.uriIsFile(c,uri)) {
            InputStream inputStream = storageAccess.getInputStream(c, uri);
            InputStreamReader lineReader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(lineReader);

            utf = "UTF-8";

            String line;
            try {
                line = buffreader.readLine();
                if (line.contains("encoding=\"")) {
                    int startpos = line.indexOf("encoding=\"") + 10;
                    int endpos = line.indexOf("\"", startpos);
                    String enc = line.substring(startpos, endpos);
                    if (enc.length() > 0) {
                        utf = enc.toUpperCase();
                    }
                }
                buffreader.close();
                lineReader.close();
            } catch (Exception e) {
                utf = "UTF-8";
            }

            // Keep a note of this encoding incase we resave the song!
            StaticVariables.mEncoding = utf;

            // read every line of the file into the line-variable, one line at the time
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
                                StaticVariables.mAuthor = parseFromHTMLEntities(xpp.nextText());
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Try to read in the xml
                                StaticVariables.mAuthor = fixXML(c, preferences, "author");
                            }
                            break;
                        case "copyright":
                            StaticVariables.mCopyright = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "title":
                            String testthetitle = parseFromHTMLEntities(xpp.nextText());
                            if (!testthetitle.equals("") && !testthetitle.isEmpty()) {
                                StaticVariables.mTitle = parseFromHTMLEntities(testthetitle);
                            } else if (testthetitle.equals("")) {
                                StaticVariables.mTitle = StaticVariables.songfilename;
                            }
                            break;
                        case "lyrics":
                            try {
                                StaticVariables.mLyrics = parseFromHTMLEntities(xpp.nextText());
                                FullscreenActivity.myLyrics = StaticVariables.mLyrics;
                            } catch (Exception e) {
                                // Try to read in the xml
                                e.printStackTrace();
                                StaticVariables.mLyrics = fixXML(c, preferences, "lyrics");
                            }
                            break;
                        case "ccli":
                            StaticVariables.mCCLI = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "theme":
                            StaticVariables.mTheme = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "alttheme":
                            StaticVariables.mAltTheme = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "presentation":
                            StaticVariables.mPresentation = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "hymn_number":
                            StaticVariables.mHymnNumber = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "user1":
                            StaticVariables.mUser1 = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "user2":
                            StaticVariables.mUser2 = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "user3":
                            StaticVariables.mUser3 = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "key":
                            StaticVariables.mKey = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "aka":
                            StaticVariables.mAka = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "key_line":
                            StaticVariables.mKeyLine = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "capo":
                            if (xpp.getAttributeCount() > 0) {
                                StaticVariables.mCapoPrint = xpp.getAttributeValue(0);
                            }
                            StaticVariables.mCapo = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "tempo":
                            StaticVariables.mTempo = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "time_sig":
                            StaticVariables.mTimeSig = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "duration":
                            StaticVariables.mDuration = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "predelay":
                            StaticVariables.mPreDelay = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "books":
                            StaticVariables.mBooks = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "midi":
                            StaticVariables.mMidi = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "midi_index":
                            StaticVariables.mMidiIndex = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "pitch":
                            StaticVariables.mPitch = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "restrictions":
                            StaticVariables.mRestrictions = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "notes":
                            StaticVariables.mNotes = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "linked_songs":
                            StaticVariables.mLinkedSongs = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "pad_file":
                            StaticVariables.mPadFile = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "custom_chords":
                            StaticVariables.mCustomChords = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "link_youtube":
                            StaticVariables.mLinkYouTube = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "link_web":
                            StaticVariables.mLinkWeb = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "link_audio":
                            StaticVariables.mLinkAudio = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "loop_audio":
                            StaticVariables.mLoopAudio = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "link_other":
                            StaticVariables.mLinkOther = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "abcnotation":
                            StaticVariables.mNotation = parseFromHTMLEntities(xpp.nextText());
                            break;
                        case "style":
                        case "backgrounds":
                            //FullscreenActivity.mExtraStuff2 = xpp.nextText();
                            // Simplest way to get this is to load the file in line by line as asynctask
                            // If we really have to load extra stuff, lets do it as an asynctask

                            // GE do it after closing the song input stream
                            needToLoadExtra = true;
                            break;
                    }
                }
                // If it isn't an xml file, an error is about to be thrown
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    Log.d("LoadXML", "Not xml so exiting");
                    eventType = XmlPullParser.END_DOCUMENT;
                }
            }

            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FullscreenActivity.myXML = StaticVariables.mLyrics;
        } else {
            FullscreenActivity.myXML = "";
            StaticVariables.mLyrics = "";
        }
        if (needToLoadExtra && StaticVariables.mLyrics!=null && !StaticVariables.mLyrics.isEmpty()) {
            InputStream extraInputStream = storageAccess.getInputStream(c, uri);
            SideTask loadextra = new SideTask(c, extraInputStream, uri);
            loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    static String[] getCCLILogInfo(Context c, Preferences preferences, String folder, String filename) {
        StorageAccess storageAccess = new StorageAccess();

        String[] vals = new String[4];

        // Get the android version
        boolean fileisxml = true;
        if (filename.toLowerCase(Locale.ROOT).endsWith(".pdf") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".doc") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".docx") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".jpg") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".jpeg") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".png") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".gif") ||
                filename.toLowerCase(Locale.ROOT).endsWith(".bmp")) {
            fileisxml = false;
        }

        String fileutf = null;

        Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", folder, filename);
        if (fileisxml) {
            fileutf = storageAccess.getUTFEncoding(c,uri);
        }

        try {
            if (fileisxml && fileutf!=null && !fileutf.equals("")) {
                // Extract all of the ccli bits of the song
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();

                vals[0] = ""; // Song title
                vals[1] = ""; // Author
                vals[2] = ""; // Copyright
                vals[3] = ""; // CCLI

                InputStream inputStream = storageAccess.getInputStream(c, uri);
                xpp.setInput(inputStream, fileutf);

                int eventType;
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "title":
                                vals[0] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "author":
                                vals[1] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "copyright":
                                vals[2] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "ccli":
                                vals[3] = parseFromHTMLEntities(xpp.nextText());
                                break;
                        }
                    }
                    try {
                        eventType = xpp.next();
                    } catch (Exception e) {
                        //Ooops!
                    }
                }
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Ooops
        }
        return vals;
    }

    static String parseFromHTMLEntities(String val) {
        if (val == null) return "";

        //Fix broken stuff
        val = val
                .replace("&amp;apos;","'")
                .replace("&amp;quote;","\"")
                .replace("&amp;quot;","\"")
                .replace("&amp;lt;","<")
                .replace("&amp;gt;",">")
                .replace("&amp;","&")
                .replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&apos;","'")
                .replace("&quote;","\"")
                .replace("&quot;","\"");
        return val;
    }

    static void getPDFPageCount(Context c, Preferences preferences, StorageAccess storageAccess) {
        // This only works for post Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);
            // FileDescriptor for file, it allows you to close file when you are done with it
            ParcelFileDescriptor mFileDescriptor;
            PdfRenderer mPdfRenderer;
            try {
                mFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                if (mFileDescriptor != null) {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    FullscreenActivity.pdfPageCount = mPdfRenderer.getPageCount();
                    preferences.setMyPreferenceBoolean(c,"songLoadSuccess",true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                FullscreenActivity.pdfPageCount = 0;
            }
        }
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

    private static String fixXML(Context c, Preferences preferences, String section) {

        // Error in the xml - tell the user we're trying to fix it!
        StorageAccess storageAccess = new StorageAccess();
        StaticVariables.myToastMessage = c.getString(R.string.fix);
        ShowToast.showToast(c);
        StringBuilder newXML = new StringBuilder();
        String tofix;
        // If an XML file has unencoded ampersands or quotes, fix them
        try {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder,
                    StaticVariables.songfilename);
            InputStream inputStream = storageAccess.getInputStream(c, uri);
            tofix = storageAccess.readTextFileToString(inputStream);
            inputStream.close();

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
                return newXML.substring(start,end);
            } else {
                return newXML.toString();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    static String getTempFileLocation(Context c, String folder, String file) {
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

    static String grabNextSongInSetKey(Context c, Preferences preferences, StorageAccess storageAccess, String nextsong) {
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
                                nextkey = parseFromHTMLEntities(xpp.nextText());
                            }
                        }
                        try {
                            eventType = xpp.next();
                        } catch (Exception e) {
                            //Ooops!
                        }
                    }
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            Log.d("LoadXML","Error trying to read XML from "+uri);
            // Ooops
        }


        return nextkey;
    }

    private static String doFix(String tofix) {
        tofix = tofix
                .replace("&amp;", "&")
                .replace("&apos;", "'")  // ' are actually fine - no need
                .replace("&quot;", "\"");

        // Get rid of doubles
        while (tofix.contains("&&")) {
            tofix = tofix.replace("&&;", "&");
        }

        // Now put them back
        tofix = tofix
                .replace("&", "$_amp_$")
                .replace("\"", "&quot;")
                .replace("$_amp_$", "&amp;");

        return tofix;
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
                if (style_start > -1 && style_end > style_start) {
                    StaticVariables.mExtraStuff1 = result.substring(style_start, style_end + 8);
                }
                int backgrounds_start = result.indexOf("<backgrounds");
                int backgrounds_end = result.indexOf("</backgrounds>");
                // GE Fix for breaking songs
                if (backgrounds_end < 0 && backgrounds_start>-1) {
                    // Self closing tag
                    backgrounds_end = result.indexOf("/>", backgrounds_start) + 2;
                } else if (backgrounds_end>-1) {
                    backgrounds_end += 14;
                }
                if (backgrounds_start > -1 && backgrounds_end > backgrounds_start) {
                    StaticVariables.mExtraStuff2 = result.substring(backgrounds_start, backgrounds_end);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}