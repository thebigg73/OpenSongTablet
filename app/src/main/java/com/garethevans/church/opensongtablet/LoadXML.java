package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadXML extends Activity {

    static boolean isxml = true;
    static String temp_myXML;
    static String temp_songfilename;
    static String temp_whichSongFolder;
    static CharSequence temp_mTitle;
    static CharSequence temp_mAuthor;
    static String temp_mUser1;
    static String temp_mUser2;
    static String temp_mUser3;
    static String temp_mAka;
    static String temp_mKeyLine;
    static String temp_mHymnNumber;
    static String temp_mLyrics;
    static String utf = "UTF-8";
    static boolean needtoloadextra = false;

    // This bit loads the lyrics from the required file
    public static void loadXML() throws XmlPullParserException, IOException {

        // Set the song load status to false (helps check if it didn't load
        Preferences.loadSongPrep();

        // Just in case
        FullscreenActivity.myLyrics = FullscreenActivity.songdoesntexist + "\n\n";
        FullscreenActivity.mLyrics = FullscreenActivity.songdoesntexist + "\n\n";

        needtoloadextra = false;
        System.gc();
        FullscreenActivity.myXML = null;
        FullscreenActivity.myXML = "";

        // Get the android version
        int androidapi = Build.VERSION.SDK_INT;
        String filetype = "SONG";
        if (FullscreenActivity.songfilename.endsWith(".pdf") || FullscreenActivity.songfilename.endsWith(".PDF")) {
            filetype = "PDF";
            isxml = false;
        }
        if (FullscreenActivity.songfilename.endsWith(".doc") || FullscreenActivity.songfilename.endsWith(".DOC") ||
                FullscreenActivity.songfilename.endsWith(".docx") || FullscreenActivity.songfilename.endsWith(".docx")) {
            filetype = "DOC";
            isxml = false;
        }
        if (FullscreenActivity.songfilename.endsWith(".jpg") || FullscreenActivity.songfilename.endsWith(".JPG") ||
                FullscreenActivity.songfilename.endsWith(".png") || FullscreenActivity.songfilename.endsWith(".PNG") ||
                FullscreenActivity.songfilename.endsWith(".gif") || FullscreenActivity.songfilename.endsWith(".GIF")) {
            filetype = "IMG";
            isxml = false;
        }

        // Determine the file encoding
        utf = getUTFEncoding(FullscreenActivity.file);

        if (androidapi > 20 || !filetype.equals("PDF") && (!filetype.equals("DOC") && (!filetype.equals("IMG")))) {
            // Identify the file location
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.file = new File(FullscreenActivity.dir + "/"
                        + FullscreenActivity.songfilename);
            } else {
                FullscreenActivity.file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                        + FullscreenActivity.songfilename);
            }



            // Initialise all the xml tags a song should have
            initialiseSongTags();

            // Try to read the file as an xml file, if it isn't, then read it in as text
            isxml = true;
            if (!FullscreenActivity.songfilename.endsWith(".sqlite3") && !FullscreenActivity.songfilename.endsWith(".preferences")) {
                try {
                    grabOpenSongXML();
                } catch (Exception e) {
                    Log.d("d", "Error performing grabOpenSongXML()");
                }
            } else {
                FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                        + FullscreenActivity.songdoesntexist + "\n\n" + "</lyrics>";
                FullscreenActivity.myLyrics = "ERROR!";
            }

            if (isxml && !FullscreenActivity.myLyrics.equals("ERROR!")) {
                // Song was loaded correctly and was xml format
                Preferences.loadSongSuccess();
            }

            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            if (FullscreenActivity.mLyrics==null ||
                    FullscreenActivity.mLyrics.isEmpty() ||
                    FullscreenActivity.mLyrics.equals("")) {
                isxml = false;
            }

            // If the file hasn't been read properly, or mLyrics is empty, read it in as a text file
            if (!isxml) {

                try {
                    //NEW
                    InputStream inputStream = new FileInputStream(FullscreenActivity.file);
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    if (validReadableFile()) {
                        FullscreenActivity.myXML = readTextFile(inputStream);
                    } else {
                        FullscreenActivity.myXML = "";
                    }
                    FullscreenActivity.mLyrics = FullscreenActivity.myXML;
                    inputStream.close();
                    bufferedReader.close();
                    // Set the song load status to true:
                    Preferences.loadSongSuccess();

                } catch (java.io.FileNotFoundException e) {
                    // file doesn't exist
                    FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                            + FullscreenActivity.songdoesntexist + "\n\n" + "</lyrics>";
                    FullscreenActivity.myLyrics = "ERROR!";
                }

                // If the song is OnSong format - try to import it
                if (FullscreenActivity.songfilename.contains(".onsong")) {
                    // Run the ChordProConvert script
                    OnSongConvert.doExtract();
                    ListSongFiles.listSongs();
                    if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/"
                                + FullscreenActivity.songfilename);
                    } else {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                                + FullscreenActivity.songfilename);
                    }
                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML();
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                }

                // If the song is usr format - try to import it
                if (FullscreenActivity.songfilename.contains(".usr")
                        || FullscreenActivity.myXML.contains("[File]")
                        || FullscreenActivity.myXML.contains("Type=")
                        || FullscreenActivity.myXML.contains("Words=")) {
                    // Run the UsrConvert script
                    UsrConvert.doExtract();
                    ListSongFiles.listSongs();
                    if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/"
                                + FullscreenActivity.songfilename);
                    } else {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                                + FullscreenActivity.songfilename);
                    }
                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML();
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                }

                // If the song is in ChordPro format - try to import it
                if (FullscreenActivity.myXML.contains("{title") ||
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
                    ChordProConvert.doExtract();
                    ListSongFiles.listSongs();
                    if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/"
                                + FullscreenActivity.songfilename);
                    } else {
                        FullscreenActivity.file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                                + FullscreenActivity.songfilename);
                    }
                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML();
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                }
            }

            // Fix all the rogue code
            FullscreenActivity.mLyrics = ProcessSong.parseLyrics(FullscreenActivity.mLyrics);

            // Just in case we have improved the song, prepare the improved xml
            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            // Write what is left to the mLyrics field just incase the file is badly formatted
            if (!FullscreenActivity.myXML.contains("<lyrics")) {
                // Need to add a space to the start of each line
                String[] lines = FullscreenActivity.myXML.split("\n");
                String text = "";
                for (int z=0;z<lines.length;z++) {
                    if (lines[z].indexOf("[")!=0 && lines[z].indexOf(".")!=0  && lines[z].indexOf(";")!=0 && lines[z].indexOf(" ")!=0) {
                        lines[z] = " " + lines[z];
                    }
                    text += lines[z] + "\n";
                }
                FullscreenActivity.mLyrics = text;
            }

            FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.mLyrics);
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
            }
            // Initialise the variables
            initialiseSongTags();
        }
    }

    public static String getUTFEncoding(File filetoload) {
        // Try to determine the BOM for UTF encoding
        try {
            FileInputStream fis = new FileInputStream(filetoload);
            UnicodeBOMInputStream ubis = new UnicodeBOMInputStream(fis);
            utf = ubis.getBOM().toString();
        } catch (Exception e) {
            Log.d("d","Error getting BOM");
            FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                    + FullscreenActivity.songdoesntexist + "\n\n" + "</lyrics>";
            FullscreenActivity.myLyrics = "ERROR!";
        }

        return utf;
    }

    public static String readTextFile(InputStream inputStream) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("d","Error reading text file");
        }
        return outputStream.toString();
    }

    public static void prepareLoadCustomReusable(String what) {

        temp_myXML = FullscreenActivity.myXML;
        temp_songfilename = FullscreenActivity.songfilename;
        temp_whichSongFolder = FullscreenActivity.whichSongFolder;
        temp_mTitle = FullscreenActivity.mTitle;
        temp_mAuthor = FullscreenActivity.mAuthor;
        temp_mUser1 = FullscreenActivity.mUser1;
        temp_mUser2 = FullscreenActivity.mUser2;
        temp_mUser3 = FullscreenActivity.mUser3;
        temp_mAka = FullscreenActivity.mAka;
        temp_mKeyLine = FullscreenActivity.mKeyLine;
        temp_mHymnNumber = FullscreenActivity.mHymnNumber;
        temp_mLyrics = FullscreenActivity.mLyrics;

        String[] tempfile = what.split("/");
        if (tempfile.length>0) {
            FullscreenActivity.songfilename = tempfile[tempfile.length - 1];
        }
        // Get the new whichSongFolder
        if (what.contains(FullscreenActivity.text_note+"/")) {
            FullscreenActivity.whichSongFolder = "../Notes";
            FullscreenActivity.whattodo = "customreusable_note";
        } else if (what.contains(FullscreenActivity.text_slide+"/")) {
            FullscreenActivity.whichSongFolder = "../Slides";
            FullscreenActivity.whattodo = "customreusable_slide";
        } else if (what.contains(FullscreenActivity.text_scripture+"/")) {
            FullscreenActivity.whichSongFolder = "../Scripture";
            FullscreenActivity.whattodo = "customreusable_scripture";
        } else if (what.contains(FullscreenActivity.image+"/")) {
            FullscreenActivity.whichSongFolder = "../Images";
            FullscreenActivity.whattodo = "customreusable_image";
        } else if (what.contains(FullscreenActivity.text_variation+"/")) {
            FullscreenActivity.whichSongFolder = "../Variations";
            FullscreenActivity.whattodo = "customreusable_variation";
        }

        // Load up the XML
        try {
            loadXML();
        } catch (Exception e) {
            Log.d("d","Error performing loadXML()");
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
        //Preferences.savePreferences();
    }

    public static void initialiseSongTags() {
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
        FullscreenActivity.mLyrics = "";
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

    public static void grabOpenSongXML() throws Exception {
        // Extract all of the key bits of the song

        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        // Just in case
        FullscreenActivity.myLyrics = FullscreenActivity.songdoesntexist + "\n\n";
        FullscreenActivity.mLyrics = FullscreenActivity.songdoesntexist + "\n\n";

        InputStream inputStream = new FileInputStream(FullscreenActivity.file);
        xpp.setInput(inputStream, utf);

        int eventType;
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("author")) {
                    FullscreenActivity.mAuthor = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("copyright")) {
                    FullscreenActivity.mCopyright = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("title")) {
                    String testthetitle = parseFromHTMLEntities(xpp.nextText());
                    if (testthetitle!=null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                        FullscreenActivity.mTitle = parseFromHTMLEntities(testthetitle);
                    }
                } else if (xpp.getName().equals("lyrics")) {
                    FullscreenActivity.mLyrics = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("ccli")) {
                    FullscreenActivity.mCCLI = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("theme")) {
                    FullscreenActivity.mTheme = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("alttheme")) {
                    FullscreenActivity.mAltTheme = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("presentation")) {
                    FullscreenActivity.mPresentation = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("hymn_number")) {
                    FullscreenActivity.mHymnNumber = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("user1")) {
                    FullscreenActivity.mUser1 = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("user2")) {
                    FullscreenActivity.mUser2 = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("user3")) {
                    FullscreenActivity.mUser3 = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("key")) {
                    FullscreenActivity.mKey = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("aka")) {
                    FullscreenActivity.mAka = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("key_line")) {
                    FullscreenActivity.mKeyLine = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("capo")) {
                    if (xpp.getAttributeCount() > 0) {
                        FullscreenActivity.mCapoPrint = xpp.getAttributeValue(0);
                    }
                    FullscreenActivity.mCapo = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("tempo")) {
                    FullscreenActivity.mTempo = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("time_sig")) {
                    FullscreenActivity.mTimeSig = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("duration")) {
                    FullscreenActivity.mDuration = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("predelay")) {
                    FullscreenActivity.mPreDelay = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("books")) {
                    FullscreenActivity.mBooks = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("midi")) {
                    FullscreenActivity.mMidi = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("midi_index")) {
                    FullscreenActivity.mMidiIndex = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("pitch")) {
                    FullscreenActivity.mPitch = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("restrictions")) {
                    FullscreenActivity.mRestrictions = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("notes")) {
                    FullscreenActivity.mNotes = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("linked_songs")) {
                    FullscreenActivity.mLinkedSongs = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("pad_file")) {
                    FullscreenActivity.mPadFile = parseFromHTMLEntities(xpp.nextText());
                 } else if (xpp.getName().equals("custom_chords")) {
                    FullscreenActivity.mCustomChords = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("link_youtube")) {
                    FullscreenActivity.mLinkYouTube = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("link_web")) {
                    FullscreenActivity.mLinkWeb = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("link_audio")) {
                    FullscreenActivity.mLinkAudio = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("loop_audio")) {
                    FullscreenActivity.mLoopAudio = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("link_other")) {
                    FullscreenActivity.mLinkOther = parseFromHTMLEntities(xpp.nextText());
                } else if (xpp.getName().equals("style")) {
                    // Simplest way to get this is to load the file in line by line as asynctask
                    needtoloadextra = true;
                } else if (xpp.getName().equals("backgrounds")) {
                    //FullscreenActivity.mExtraStuff2 = xpp.nextText();
                    // Simplest way to get this is to load the file in line by line as asynctask
                    needtoloadextra = true;
                }
            }
            try {
                eventType = xpp.next();
            } catch (Exception e) {
                //Ooops!
                Log.d("d","error in file, or not xml");
                isxml = false;
            }
        }

        // If we really have to load extra stuff, lets do it as an asynctask
        if (needtoloadextra) {
            SideTask loadextra = new SideTask();
            loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        FullscreenActivity.myXML = FullscreenActivity.mLyrics;
    }

    public static String parseFromHTMLEntities(String val) {
        //Fix broken stuff
        val = val.replace("&amp;apos;","'");
        val = val.replace("&amp;quote;","\"");
        val = val.replace("&amp;lt;","<");
        val = val.replace("&amp;gt;",">");
        val = val.replace("&amp;","&");
        val = val.replace("&lt;","<");
        val = val.replace("&gt;",">");
        val = val.replace("&apos;","'");
        val = val.replace("&quote;","\"");
        return val;
    }

    private static class SideTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String full_text;
            try {
                InputStream inputStream = new FileInputStream(FullscreenActivity.file);
                if (validReadableFile()) {
                    full_text = readTextFile(inputStream);
                } else {
                    full_text = "";
                }
            } catch (Exception e) {
                Log.d("d","Error reading text file");
                full_text = "";
            }

            return full_text;
        }

        @Override
        protected void onPostExecute(String result) {
            int style_start = result.indexOf("<style");
            int style_end   = result.indexOf("</style>");
            if (style_end>style_start && style_start>-1 && style_end>-1) {
                FullscreenActivity.mExtraStuff1 = result.substring(style_start,style_end+8);
            }
            int backgrounds_start = result.indexOf("<backgrounds");
            int backgrounds_end   = result.indexOf("</backgrounds>");
            if (backgrounds_end<0) {
                backgrounds_end = result.indexOf("/>",backgrounds_start)+2;
            } else {
                backgrounds_end += 14;
            }
            if (backgrounds_end>backgrounds_start && backgrounds_start>-1 && backgrounds_end>-1) {
                FullscreenActivity.mExtraStuff2 = result.substring(backgrounds_start,backgrounds_end);
            }
        }
    }

    public static boolean validReadableFile() {
        boolean isvalid = false;
        // Get length of file in bytes
        long filesize = FullscreenActivity.file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        filesize = filesize / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        filesize = filesize / 1024;
        String filename = FullscreenActivity.file.toString();
        if (filename.endsWith(".txt") || filename.endsWith(".TXT") ||
                filename.endsWith(".onsong") || filename.endsWith(".ONSONG") ||
                filename.endsWith(".crd") || filename.endsWith(".CRD") ||
                filename.endsWith(".chopro") || filename.endsWith(".CHOPRO") ||
                filename.endsWith(".chordpro") || filename.endsWith(".CHORDPRO") ||
                filename.endsWith(".usr") || filename.endsWith(".USR") ||
                filename.endsWith(".pro") || filename.endsWith(".pro")) {
            isvalid = true;
        } else if (filesize<2) {
            // Less than 2Mb
            isvalid = true;
        }
        return isvalid;
    }

}