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

public class LoadXML extends Activity {

    private static boolean isxml = true;
    static String utf = "UTF-8";
    private static boolean needtoloadextra = false;

    // This bit loads the lyrics from the required file
    static void loadXML(Context c, Preferences preferences, ListSongFiles listSongFiles, StorageAccess storageAccess) throws IOException {

        FullscreenActivity.isPDF = false;
        FullscreenActivity.isSong = true;
        FullscreenActivity.isImage = false;
        FullscreenActivity.isSlide = false;
        FullscreenActivity.isScripture = false;
        FullscreenActivity.isImageSlide = false;

        // Clear the heading default
        FullscreenActivity.songSection_holder = "";

        // Set the song load status to false (helps check if it didn't load
        Preferences.loadSongPrep();

        // Just in case
        setNotFound(c);

        needtoloadextra = false;
        FullscreenActivity.myXML = null;
        FullscreenActivity.myXML = "";

        // Get the android version
        String filetype = "SONG";
        isxml = true;
        if (FullscreenActivity.songfilename.toLowerCase().endsWith(".pdf")) {
            FullscreenActivity.isPDF = true;
            filetype = "PDF";
            isxml = false;
        } else if (FullscreenActivity.songfilename.toLowerCase().endsWith(".doc") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".docx")) {
            filetype = "DOC";
            isxml = false;
        } else if (FullscreenActivity.songfilename.toLowerCase().endsWith(".jpg") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".jpeg") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".png") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".gif") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".bmp")) {
            filetype = "IMG";
            FullscreenActivity.isImage = true;
            isxml = false;
        }

        String where = "Songs";
        String folder = FullscreenActivity.whichSongFolder;
        if (FullscreenActivity.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
            where = "";
        }
        // Determine the file encoding
        Uri uri = storageAccess.getUriForItem(c, preferences, where, folder,
                FullscreenActivity.songfilename);
        if (filetype.equals("SONG") && !FullscreenActivity.songfilename.equals("Welcome to OpenSongApp")) {
            utf = storageAccess.getUTFEncoding(c, uri);
        }

        if (FullscreenActivity.songfilename.equals("Welcome to OpenSongApp")) {
            setWelcome(c);
        }

        if (!filetype.equals("PDF") && !filetype.equals("DOC") && (!filetype.equals("IMG")) &&
                !FullscreenActivity.songfilename.equals("Welcome to OpenSongApp")) {
            // Initialise all the xml tags a song should have
            initialiseSongTags(c);

            // Try to read the file as an xml file, if it isn't, then read it in as text
            isxml = true;
            if (!FullscreenActivity.songfilename.endsWith(".sqlite3") && !FullscreenActivity.songfilename.endsWith(".preferences") &&
                    !FullscreenActivity.songfilename.equals("Welcome to OpenSongApp")) {
                try {
                    grabOpenSongXML(c, preferences);
                } catch (Exception e) {
                    e.printStackTrace();
                    setNotFound(c);
                    isxml = false;
                }
            } else {
                setNotFound(c);
            }

            if (isxml && !FullscreenActivity.myLyrics.equals("ERROR!")) {
                // Song was loaded correctly and was xml format
                Preferences.loadSongSuccess();
            } else {
                Log.d("LoadXML", "Song wasn't loaded");
            }

            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            if (FullscreenActivity.mLyrics == null || FullscreenActivity.mLyrics.isEmpty()) {
                isxml = false;
            }

            // If the file hasn't been read properly, or mLyrics is empty, read it in as a text file
            if (!isxml) {
                Log.d("LoadXML", "not xml");
                try {
                    //NEW
                    uri = storageAccess.getUriForItem(c, preferences, where, folder,
                            FullscreenActivity.songfilename);
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    if (validReadableFile(c, storageAccess, uri)) {
                        FullscreenActivity.myXML = storageAccess.readTextFileToString(inputStream);
                    } else {
                        FullscreenActivity.myXML = "";
                    }
                    FullscreenActivity.mLyrics = FullscreenActivity.myXML;
                    inputStream.close();
                    bufferedReader.close();
                    // Set the song load status to true:
                    Preferences.loadSongSuccess();

                } catch (java.io.FileNotFoundException e) {
                    e.printStackTrace();
                    setNotFound(c);
                }

                // If the song is OnSong format - try to import it
                if (FullscreenActivity.songfilename.contains(".onsong")) {
                    // Run the OnSongConvert script
                    OnSongConvert onSongConvert = new OnSongConvert();
                    if (!onSongConvert.doExtract(c, preferences)) {
                        Log.d("LoadXML", "Problem converting OnSong");
                    }
                    listSongFiles.getAllSongFiles(c, preferences, storageAccess);

                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c, preferences);
                    } catch (Exception e) {
                        Log.d("LoadXML", "Error performing grabOpenSongXML()");
                    }

                    // If the song is usr format - try to import it
                } else if (FullscreenActivity.songfilename.contains(".usr")
                        || FullscreenActivity.myXML.contains("[File]")
                        || FullscreenActivity.myXML.contains("Type=")
                        || FullscreenActivity.myXML.contains("Words=")) {
                    // Run the UsrConvert script
                    UsrConvert usrConvert = new UsrConvert();
                    if (!usrConvert.doExtract(c, preferences)) {
                        Log.d("LoadXML", "Problem extracting usr file");
                    }

                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c, preferences);
                    } catch (Exception e) {
                        Log.d("LoadXML", "Error performing grabOpenSongXML()");
                    }

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
                        FullscreenActivity.songfilename.toLowerCase().contains(".pro") ||
                        FullscreenActivity.songfilename.toLowerCase().contains(".chopro") ||
                        FullscreenActivity.songfilename.toLowerCase().contains(".chordpro")) {
                    // Run the ChordProConvert script
                    ChordProConvert chordProConvert = new ChordProConvert();
                    if (!chordProConvert.doExtract(c, preferences)) {
                        Log.d("LoadXML", "Problem extracting chordpro");
                    }

                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c, preferences);
                    } catch (Exception e) {
                        Log.d("LoadXML", "Error performing grabOpenSongXML()");
                    }
                }
            }

            // Fix all the rogue code
            FullscreenActivity.mLyrics = ProcessSong.parseLyrics(FullscreenActivity.mLyrics, c);

            // Just in case we have improved the song, prepare the improved xml
            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            // Write what is left to the mLyrics field just incase the file is badly formatted
            if (!FullscreenActivity.myXML.contains("<lyrics")) {
                // Need to add a space to the start of each line
                String[] lines = FullscreenActivity.myXML.split("\n");
                StringBuilder text = new StringBuilder();
                for (int z=0; z<lines.length; z++) {
                    if (lines[z].indexOf("[")!=0 && lines[z].indexOf(".")!=0 && lines[z].indexOf(";")!=0 && lines[z].indexOf("---")!=0 && lines[z].indexOf(" ")!=0) {
                        lines[z] = " " + lines[z];
                    }
                    text.append(lines[z]).append("\n");
                }
                FullscreenActivity.mLyrics = text.toString();
            }

            FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.mLyrics,c);
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Very Fast", "140");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Fast", "120");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Moderate", "100");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Slow", "80");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Very Slow", "60");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replaceAll("[\\D]", "");

            if (!FullscreenActivity.mDuration.isEmpty()) {
                try {
                    FullscreenActivity.autoScrollDuration = Integer.parseInt(FullscreenActivity.mDuration.replaceAll("[\\D]",""));
                } catch (Exception e) {
                    FullscreenActivity.autoScrollDuration = -1;
                }
            }

            if (!FullscreenActivity.mPreDelay.isEmpty()) {
                try {
                    FullscreenActivity.autoScrollDelay = Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]",""));
                } catch (Exception e) {
                    FullscreenActivity.autoScrollDelay = -1;
                }
            }

        } else {
            if (filetype.equals("PDF")) {
                FullscreenActivity.isPDF = true;
                FullscreenActivity.isSong = false;
                FullscreenActivity.isImage = false;
                FullscreenActivity.isImageSlide = false;
                FullscreenActivity.isSlide = false;
            } else if (filetype.equals("IMG")) {
                FullscreenActivity.isPDF = false;
                FullscreenActivity.isSong = false;
                FullscreenActivity.isImage = true;
                FullscreenActivity.isImageSlide = false;
                FullscreenActivity.isSlide = false;
            }
            // Initialise the variables
            initialiseSongTags(c);
        }

        String loc = "";
        if (FullscreenActivity.whichSongFolder!=null) {
            loc = FullscreenActivity.whichSongFolder;
        }

        if (loc.contains("../Images")) {
            FullscreenActivity.isImageSlide = true;
        } else if (loc.contains("../Scripture")) {
            FullscreenActivity.isScripture = true;
        } else if (loc.contains("../Slides")) {
            FullscreenActivity.isSlide = true;
        } else if (loc.contains("../Variations")) {
            FullscreenActivity.isSong = true;
        }

        FullscreenActivity.thissong_scale = FullscreenActivity.toggleYScale;
    }

    private static void setNotFound(Context c) {
        FullscreenActivity.myLyrics = FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "\n\n" +
                c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        FullscreenActivity.mLyrics = FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "\n\n" +
                c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        FullscreenActivity.myXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<title>Welcome to OpenSongApp</title>\n" +
                "<author>Gareth Evans</author>\n<lyrics>"
                + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "\n\n"
                + c.getResources().getString(R.string.songdoesntexist) + "\n\n" + "</lyrics>";
        FullscreenActivity.mLyrics = FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "\n\n"
                + c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        FullscreenActivity.myLyrics = "ERROR!";
    }

    private static void setWelcome(Context c) {
        FullscreenActivity.thissong_scale = "W";
        FullscreenActivity.mTitle = "Welcome to OpenSongApp";
        FullscreenActivity.mAuthor = "Gareth Evans";
        FullscreenActivity.mLinkWeb = "http://www.opensongapp.com";
        FullscreenActivity.mLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.myXML = "<?xml><song><title>" + FullscreenActivity.mTitle + "</title>\n" +
                "<author>" + FullscreenActivity.mAuthor + "</author>\n" +
                "<lyrics>" + FullscreenActivity.mLyrics + "\n</lyrics></song>";
    }

    static void prepareLoadCustomReusable(Context c, Preferences preferences, ListSongFiles listSongFiles, StorageAccess storageAccess, String what) {

        String temp_myXML = FullscreenActivity.myXML;
        String temp_songfilename = FullscreenActivity.songfilename;
        String temp_whichSongFolder = FullscreenActivity.whichSongFolder;
        CharSequence temp_mTitle = FullscreenActivity.mTitle;
        CharSequence temp_mAuthor = FullscreenActivity.mAuthor;
        String temp_mUser1 = FullscreenActivity.mUser1;
        String temp_mUser2 = FullscreenActivity.mUser2;
        String temp_mUser3 = FullscreenActivity.mUser3;
        String temp_mAka = FullscreenActivity.mAka;
        String temp_mKeyLine = FullscreenActivity.mKeyLine;
        String temp_mHymnNumber = FullscreenActivity.mHymnNumber;
        String temp_mLyrics = FullscreenActivity.mLyrics;

        String[] tempfile = what.split("/");
        if (tempfile.length>0) {
            FullscreenActivity.songfilename = tempfile[tempfile.length - 1];
        }
        // Get the new whichSongFolder
        if (what.contains(c.getResources().getString(R.string.note)+"/")) {
            FullscreenActivity.whichSongFolder = "../Notes";
            FullscreenActivity.whattodo = "customreusable_note";
        } else if (what.contains(c.getResources().getString(R.string.slide)+"/")) {
            FullscreenActivity.whichSongFolder = "../Slides";
            FullscreenActivity.whattodo = "customreusable_slide";
        } else if (what.contains(c.getResources().getString(R.string.scripture)+"/")) {
            FullscreenActivity.whichSongFolder = "../Scripture";
            FullscreenActivity.whattodo = "customreusable_scripture";
        } else if (what.contains(c.getResources().getString(R.string.image)+"/")) {
            FullscreenActivity.whichSongFolder = "../Images";
            FullscreenActivity.whattodo = "customreusable_image";
        } else if (what.contains(c.getResources().getString(R.string.variation)+"/")) {
            FullscreenActivity.whichSongFolder = "../Variations";
            FullscreenActivity.whattodo = "customreusable_variation";
        }

        // Load up the XML
        try {
            loadXML(c, preferences, listSongFiles, storageAccess);
        } catch (Exception e) {
            Log.d("LoadXML", "Error performing loadXML()");
        }

        // Put the values in
        FullscreenActivity.customslide_title = FullscreenActivity.mTitle.toString();
        FullscreenActivity.customimage_time = FullscreenActivity.mUser1;
        FullscreenActivity.customimage_loop = FullscreenActivity.mUser2;
        FullscreenActivity.customimage_list = FullscreenActivity.mUser3;
        FullscreenActivity.customslide_content = FullscreenActivity.mLyrics;

        // Reset the main song variables back to their former glory
        FullscreenActivity.myXML = temp_myXML;
        FullscreenActivity.songfilename = temp_songfilename;
        FullscreenActivity.whichSongFolder = temp_whichSongFolder;
        FullscreenActivity.mTitle = temp_mTitle;
        FullscreenActivity.mAuthor = temp_mAuthor;
        FullscreenActivity.mUser1 = temp_mUser1;
        FullscreenActivity.mUser2 = temp_mUser2;
        FullscreenActivity.mUser3 = temp_mUser3;
        FullscreenActivity.mAka = temp_mAka;
        FullscreenActivity.mKeyLine = temp_mKeyLine;
        FullscreenActivity.mHymnNumber = temp_mHymnNumber;
        FullscreenActivity.mLyrics = temp_mLyrics;
    }

    static void initialiseSongTags(Context c) {
        FullscreenActivity.mTitle = FullscreenActivity.songfilename;
        FullscreenActivity.mAuthor = "";
        FullscreenActivity.mCopyright = "";
        FullscreenActivity.mPresentation = "";
        FullscreenActivity.mHymnNumber = "";
        FullscreenActivity.mCapo = "";
        FullscreenActivity.mCapoPrint = "false";
        FullscreenActivity.mTempo = "";
        FullscreenActivity.mTimeSig = "";
        FullscreenActivity.mDuration = "";
        FullscreenActivity.mPreDelay = "";
        FullscreenActivity.mCCLI = "";
        FullscreenActivity.mTheme = "";
        FullscreenActivity.mAltTheme = "";
        FullscreenActivity.mUser1 = "";
        FullscreenActivity.mUser2 = "";
        FullscreenActivity.mUser3 = "";
        FullscreenActivity.mKey = "";
        FullscreenActivity.mAka = "";
        FullscreenActivity.mKeyLine = "";
        FullscreenActivity.mBooks = "";
        FullscreenActivity.mMidi = "";
        FullscreenActivity.mMidiIndex = "";
        FullscreenActivity.mPitch = "";
        FullscreenActivity.mRestrictions = "";
        FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
        FullscreenActivity.mNotes = "";
        FullscreenActivity.mStyle = "";
        FullscreenActivity.mLinkedSongs = "";
        FullscreenActivity.mPadFile = "";
        FullscreenActivity.mCustomChords = "";
        FullscreenActivity.mLinkYouTube = "";
        FullscreenActivity.mLinkWeb = "";
        FullscreenActivity.mLinkAudio = "";
        FullscreenActivity.mLoopAudio = "false";
        FullscreenActivity.mLinkOther = "";
        FullscreenActivity.mExtraStuff1 = "";
        FullscreenActivity.mExtraStuff2 = "";
    }

    private static void grabOpenSongXML(Context c, Preferences preferences) throws Exception {
        // Extract all of the key bits of the song
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        // Just in case use the Welcome to OpenSongApp file
        initialiseSongTags(c);

        // Get the uri and stream of the file
        StorageAccess storageAccess = new StorageAccess();

        String where = "Songs";
        String folder = FullscreenActivity.whichSongFolder;
        if (FullscreenActivity.whichSongFolder.startsWith("../")) {
            folder = folder.replace("../", "");
            where = "";
        }

        Uri uri = storageAccess.getUriForItem(c, preferences, where, folder,
                FullscreenActivity.songfilename);

        InputStream inputStream = storageAccess.getInputStream(c,uri);
        InputStreamReader lineReader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(lineReader);

        isxml = false;
        utf = "UTF-8";

        String line;
        try {
            line = buffreader.readLine();
            if (line.contains("<?xml")) {
                isxml=true;
            }
            if (line.contains("encoding=\"")) {
                int startpos = line.indexOf("encoding=\"")+10;
                int endpos = line.indexOf("\"",startpos);
                String enc = line.substring(startpos,endpos);
                if (enc.length() > 0) {
                    utf = enc.toUpperCase();
                }
            }
        } catch (Exception e) {
            utf = "UTF-8";
        }

        // Keep a note of this encoding incase we resave the song!
        FullscreenActivity.mEncoding = utf;

        // read every line of the file into the line-variable, on line at the time
        inputStream.close();
        inputStream = storageAccess.getInputStream(c,uri);

        xpp.setInput(inputStream, utf);

        int eventType;
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "author":
                        try {
                            FullscreenActivity.mAuthor = parseFromHTMLEntities(xpp.nextText());
                            isxml=true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            // Try to read in the xml
                            FullscreenActivity.mAuthor = fixXML(c, preferences, "author");
                        }
                        break;
                    case "copyright":
                        FullscreenActivity.mCopyright = parseFromHTMLEntities(xpp.nextText());
                        isxml=true;
                        break;
                    case "title":
                        String testthetitle = parseFromHTMLEntities(xpp.nextText());
                        if (testthetitle != null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                            FullscreenActivity.mTitle = parseFromHTMLEntities(testthetitle);
                        } else if (testthetitle != null && testthetitle.equals("")) {
                            FullscreenActivity.mTitle = FullscreenActivity.songfilename;
                        }
                        isxml=true;
                        break;
                    case "lyrics":
                        try {
                            FullscreenActivity.mLyrics = ProcessSong.fixStartOfLines(parseFromHTMLEntities(xpp.nextText()));
                            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
                        } catch (Exception e) {
                            // Try to read in the xml
                            e.printStackTrace();
                            FullscreenActivity.mLyrics = fixXML(c, preferences, "lyrics");
                        }
                        break;
                    case "ccli":
                        FullscreenActivity.mCCLI = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "theme":
                        FullscreenActivity.mTheme = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "alttheme":
                        FullscreenActivity.mAltTheme = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "presentation":
                        FullscreenActivity.mPresentation = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "hymn_number":
                        FullscreenActivity.mHymnNumber = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user1":
                        FullscreenActivity.mUser1 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user2":
                        FullscreenActivity.mUser2 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user3":
                        FullscreenActivity.mUser3 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "key":
                        FullscreenActivity.mKey = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "aka":
                        FullscreenActivity.mAka = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "key_line":
                        FullscreenActivity.mKeyLine = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "capo":
                        if (xpp.getAttributeCount() > 0) {
                            FullscreenActivity.mCapoPrint = xpp.getAttributeValue(0);
                        }
                        FullscreenActivity.mCapo = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "tempo":
                        FullscreenActivity.mTempo = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "time_sig":
                        FullscreenActivity.mTimeSig = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "duration":
                        FullscreenActivity.mDuration = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "predelay":
                        FullscreenActivity.mPreDelay = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "books":
                        FullscreenActivity.mBooks = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "midi":
                        FullscreenActivity.mMidi = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "midi_index":
                        FullscreenActivity.mMidiIndex = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "pitch":
                        FullscreenActivity.mPitch = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "restrictions":
                        FullscreenActivity.mRestrictions = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "notes":
                        FullscreenActivity.mNotes = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "linked_songs":
                        FullscreenActivity.mLinkedSongs = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "pad_file":
                        FullscreenActivity.mPadFile = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "custom_chords":
                        FullscreenActivity.mCustomChords = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_youtube":
                        FullscreenActivity.mLinkYouTube = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_web":
                        FullscreenActivity.mLinkWeb = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_audio":
                        FullscreenActivity.mLinkAudio = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "loop_audio":
                        FullscreenActivity.mLoopAudio = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_other":
                        FullscreenActivity.mLinkOther = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "abcnotation":
                        FullscreenActivity.mNotation = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "style":
                        // Simplest way to get this is to load the file in line by line as asynctask
                        needtoloadextra = true;
                        break;
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
            } catch (Exception e) {
                Log.d("LoadXML", "Not xml so exiting");
                e.printStackTrace();
                eventType = XmlPullParser.END_DOCUMENT;
            }
        }

        if (FullscreenActivity.songfilename.equals("Welcome to OpenSongApp")) {
            FullscreenActivity.mTitle = "Welcome to OpenSongApp";
            FullscreenActivity.mAuthor = "Gareth Evans";
            FullscreenActivity.mLinkWeb = "http://www.opensongapp.com";
            FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
            FullscreenActivity.myLyrics = c.getString(R.string.user_guide_lyrics);
        }

        // If we really have to load extra stuff, lets do it as an asynctask
        if (needtoloadextra) {
            inputStream = storageAccess.getInputStream(c,uri);
            SideTask loadextra = new SideTask(c, inputStream, uri);
            loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        FullscreenActivity.myXML = FullscreenActivity.mLyrics;
    }

    static String[] getCCLILogInfo(Context c, Preferences preferences, String folder, String filename) {
        StorageAccess storageAccess = new StorageAccess();

        String[] vals = new String[4];

        // Get the android version
        boolean fileisxml = true;
        if (filename.toLowerCase().endsWith(".pdf") ||
                filename.toLowerCase().endsWith(".doc") ||
                filename.toLowerCase().endsWith(".docx") ||
                filename.toLowerCase().endsWith(".jpg") ||
                filename.toLowerCase().endsWith(".jpeg") ||
                filename.toLowerCase().endsWith(".png") ||
                filename.toLowerCase().endsWith(".gif") ||
                filename.toLowerCase().endsWith(".bmp")) {
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
            }
        } catch (Exception e) {
            // Ooops
        }
        return vals;
    }

    static String parseFromHTMLEntities(String val) {
        //Fix broken stuff
        if (val==null) {
            val = "";
        }
        val = val.replace("&amp;apos;","'");
        val = val.replace("&amp;quote;","\"");
        val = val.replace("&amp;quot;","\"");
        val = val.replace("&amp;lt;","<");
        val = val.replace("&amp;gt;",">");
        val = val.replace("&amp;","&");
        val = val.replace("&lt;","<");
        val = val.replace("&gt;",">");
        val = val.replace("&apos;","'");
        val = val.replace("&quote;","\"");
        val = val.replace("&quot;","\"");
        return val;
    }

    static void getPDFPageCount(Context c, Preferences preferences, StorageAccess storageAccess) {
        // This only works for post Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, FullscreenActivity.songfilename);
            // FileDescriptor for file, it allows you to close file when you are done with it
            ParcelFileDescriptor mFileDescriptor;
            PdfRenderer mPdfRenderer;
            try {
                mFileDescriptor = c.getContentResolver().openFileDescriptor(uri, "r");
                if (mFileDescriptor != null) {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    FullscreenActivity.pdfPageCount = mPdfRenderer.getPageCount();
                    Preferences.loadSongSuccess();
                }
            } catch (IOException e) {
                e.printStackTrace();
                FullscreenActivity.pdfPageCount = 0;
            }
        }
    }

    static boolean validReadableFile(Context c, StorageAccess storageAccess, Uri uri) {
        boolean isvalid = false;
        // Get length of file in Kb
        float filesize = storageAccess.getFileSizeFromUri(c, uri);
        String filename = FullscreenActivity.songfilename;
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

    static String fixXML(Context c, Preferences preferences, String section) {

        // Error in the xml - tell the user we're trying to fix it!
        StorageAccess storageAccess = new StorageAccess();
        FullscreenActivity.myToastMessage = c.getString(R.string.fix);
        ShowToast.showToast(c);
        StringBuilder newXML = new StringBuilder();
        String tofix;
        // If an XML file has unencoded ampersands or quotes, fix them
        try {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder,
                    FullscreenActivity.songfilename);
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
                isxml=true;
                return newXML.substring(start,end);
            } else {
                isxml = false;
                return newXML.toString();
            }


        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    static String getTempFileLocation(Context c, String folder, String file) {
        String where = folder + "/" + file;
        if (folder.equals(FullscreenActivity.mainfoldername)) {
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
        if (nextsong.toLowerCase().endsWith(".pdf") ||
                nextsong.toLowerCase().endsWith(".doc") ||
                nextsong.toLowerCase().endsWith(".docx") ||
                nextsong.toLowerCase().endsWith(".jpg") ||
                nextsong.toLowerCase().endsWith(".jpeg") ||
                nextsong.toLowerCase().endsWith(".png") ||
                nextsong.toLowerCase().endsWith(".gif") ||
                nextsong.toLowerCase().endsWith(".bmp")) {
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ooops
        }

        return nextkey;
    }

    static String doFix(String tofix) {
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

    @SuppressLint("StaticFieldLeak")
    private static class SideTask extends AsyncTask<String, Void, String> {

        InputStream inputStream;
        StorageAccess storageAccess;
        Uri uri;
        Context c;

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
                    FullscreenActivity.mExtraStuff1 = result.substring(style_start, style_end + 8);
                }
                int backgrounds_start = result.indexOf("<backgrounds");
                int backgrounds_end = result.indexOf("</backgrounds>");
                if (backgrounds_end < 0) {
                    backgrounds_end = result.indexOf("/>", backgrounds_start) + 2;
                } else {
                    backgrounds_end += 14;
                }
                if (backgrounds_end > backgrounds_start && backgrounds_start > -1) {
                    FullscreenActivity.mExtraStuff2 = result.substring(backgrounds_start, backgrounds_end);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}