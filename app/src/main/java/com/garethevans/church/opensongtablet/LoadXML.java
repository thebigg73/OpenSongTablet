package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.os.Build;

public class LoadXML extends Activity {

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

	// This bit loads the lyrics from the required file
	public static void loadXML() throws XmlPullParserException, IOException {
        System.gc();
        FullscreenActivity.myXML = null;
        FullscreenActivity.myXML = "";

        // Get the android version
        int androidapi = Build.VERSION.SDK_INT;
        String filetype = "SONG";
        if (FullscreenActivity.songfilename.contains(".pdf") || FullscreenActivity.songfilename.contains(".PDF")) {
            filetype = "PDF";
        }
        if (FullscreenActivity.songfilename.contains(".doc") || FullscreenActivity.songfilename.contains(".DOC")) {
            filetype = "DOC";
        }

        if (FullscreenActivity.songfilename.contains(".jpg") || FullscreenActivity.songfilename.contains(".JPG") ||
                FullscreenActivity.songfilename.contains(".png") || FullscreenActivity.songfilename.contains(".PNG") ||
                FullscreenActivity.songfilename.contains(".gif") || FullscreenActivity.songfilename.contains(".GIF")) {
            filetype = "IMG";
        }

        if (androidapi > 20 || !filetype.equals("PDF") && (!filetype.equals("DOC") && (!filetype.equals("IMG")))) {
            // Test if file exists
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.file = new File(FullscreenActivity.dir + "/"
                        + FullscreenActivity.songfilename);
            } else {
                FullscreenActivity.file = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                        + FullscreenActivity.songfilename);
            }

            FileInputStream inputStream = new FileInputStream(FullscreenActivity.file);

            try {
                //NEW
                InputStreamReader streamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                FullscreenActivity.myXML = readTextFile(inputStream);
                FullscreenActivity.mExtraStuff1 = "";
                FullscreenActivity.mExtraStuff2 = "";
                // Remove the extra stuff not needed (between the <style> and </style> tags
                int start_extrastuff = FullscreenActivity.myXML.indexOf("<style");
                int end_extrastuff = FullscreenActivity.myXML.indexOf("</style>") + 8;
                // Add this to the appropriate string variable
                if (start_extrastuff != -1 && end_extrastuff != -1 && end_extrastuff > start_extrastuff) {
                    FullscreenActivity.mExtraStuff1 += FullscreenActivity.myXML.substring(start_extrastuff, end_extrastuff);
                    FullscreenActivity.myXML = FullscreenActivity.myXML.substring(0, start_extrastuff) + FullscreenActivity.myXML.substring(end_extrastuff);
                }
                // Remove the extra stuff not needed (between the <backgrounds> and </song> tags
                start_extrastuff = FullscreenActivity.myXML.indexOf("<backgrounds");
                end_extrastuff = FullscreenActivity.myXML.indexOf(">", start_extrastuff) + 1;
                // Add this to the appropriate string variable
                if (start_extrastuff != -1 && end_extrastuff != -1 && end_extrastuff > start_extrastuff) {
                    FullscreenActivity.mExtraStuff2 = FullscreenActivity.myXML.substring(start_extrastuff, end_extrastuff);
                    FullscreenActivity.myXML = FullscreenActivity.myXML.substring(0, start_extrastuff) + FullscreenActivity.myXML.substring(end_extrastuff);
                }

                inputStream.close();
                bufferedReader.close();

                inputStream.close(); // close the file
            } catch (java.io.FileNotFoundException e) {
                // file doesn't exist
                // Alert the user

                FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                        + FullscreenActivity.songdoesntexist + "</lyrics>";
                FullscreenActivity.myLyrics = "ERROR!";
            }

            // Initialise all the xml tags a song should have
            initialiseSongTags();

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

            }

            //Change the line breaks and Slides to better match OpenSong
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\r\n", "\n");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\r", "\n");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\t", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\\t", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\b", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\f", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\r", "");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\t", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\b", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\f", "    ");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("&#x27;", "'");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\u0092", "'");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\u0093", "'");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\u2018", "'");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replaceAll("\u2019", "'");

            if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.slide) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.image) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.note) && !FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
                FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Slide 1", "[V1]");
                FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Slide 2", "[V2]");
                FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Slide 3", "[V3]");
                FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Slide 4", "[V4]");
                FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Slide 5", "[V5]");
            }

            // Make lowercase start tags into caps
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("[v", "[V");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("[b", "[B");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("[c", "[C");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("[t", "[T");
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("[p", "[P");

            // Try to convert ISO / Windows
            FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\0x91", "'");

            boolean isxml = true;
            // Write what is left to the mLyrics field just incase the file is badly formatted
            if (!FullscreenActivity.myXML.contains("<lyrics")) {
                isxml = false;
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
            } else {
                FullscreenActivity.mLyrics = "";
            }

            // Try to spot utf encoding (only really an issue for xml files).
            String utf = "UTF-8";
            if (FullscreenActivity.myXML.contains("\ufeff") || FullscreenActivity.myXML.contains("\uFEFF") ||
                    FullscreenActivity.myXML.contains("[&#x27;]") || FullscreenActivity.myXML.contains("&#x27;") ||
                    FullscreenActivity.myXML.contains("[\\xEF]") || FullscreenActivity.myXML.contains("\\xEF") ||
                    FullscreenActivity.myXML.contains("[\\xBB]") || FullscreenActivity.myXML.contains("\\xBB") ||
                    FullscreenActivity.myXML.contains("[\\xFF]") || FullscreenActivity.myXML.contains("\\xFF")) {

                utf = "UTF-16";
            }

            if (isxml) {
                // Extract all of the key bits of the song
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();

                //xpp.setInput(new StringReader(FullscreenActivity.myXML));

                inputStream = new FileInputStream(FullscreenActivity.file);
                //if (FullscreenActivity.myXML.contains("UTF-16")) {
                if (utf.equals("UTF-16")) {
                    xpp.setInput(inputStream, "UTF-16");
                } else {
                    xpp.setInput(inputStream, "UTF-8");
                }

                int eventType;
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("author")) {
                            FullscreenActivity.mAuthor = xpp.nextText();
                        } else if (xpp.getName().equals("copyright")) {
                            FullscreenActivity.mCopyright = xpp.nextText();
                        } else if (xpp.getName().equals("title")) {
                            FullscreenActivity.mTitle = xpp.nextText();
                        } else if (xpp.getName().equals("lyrics")) {
                            FullscreenActivity.mLyrics = xpp.nextText();
                        } else if (xpp.getName().equals("ccli")) {
                            FullscreenActivity.mCCLI = xpp.nextText();
                        } else if (xpp.getName().equals("theme")) {
                            FullscreenActivity.mTheme = xpp.nextText();
                        } else if (xpp.getName().equals("alttheme")) {
                            FullscreenActivity.mAltTheme = xpp.nextText();
                        } else if (xpp.getName().equals("presentation")) {
                            FullscreenActivity.mPresentation = xpp.nextText();
                        } else if (xpp.getName().equals("hymn_number")) {
                            FullscreenActivity.mHymnNumber = xpp.nextText();
                        } else if (xpp.getName().equals("user1")) {
                            FullscreenActivity.mUser1 = xpp.nextText();
                        } else if (xpp.getName().equals("user2")) {
                            FullscreenActivity.mUser2 = xpp.nextText();
                        } else if (xpp.getName().equals("user3")) {
                            FullscreenActivity.mUser3 = xpp.nextText();
                        } else if (xpp.getName().equals("key")) {
                            FullscreenActivity.mKey = xpp.nextText();
                        } else if (xpp.getName().equals("aka")) {
                            FullscreenActivity.mAka = xpp.nextText();
                        } else if (xpp.getName().equals("key_line")) {
                            FullscreenActivity.mKeyLine = xpp.nextText();
                        } else if (xpp.getName().equals("capo")) {
                            if (xpp.getAttributeCount() > 0) {
                                FullscreenActivity.mCapoPrint = xpp.getAttributeValue(0);
                            }
                            FullscreenActivity.mCapo = xpp.nextText();
                        } else if (xpp.getName().equals("tempo")) {
                            FullscreenActivity.mTempo = xpp.nextText();
                        } else if (xpp.getName().equals("time_sig")) {
                            FullscreenActivity.mTimeSig = xpp.nextText();
                        } else if (xpp.getName().equals("duration")) {
                            FullscreenActivity.mDuration = xpp.nextText();
                        } else if (xpp.getName().equals("predelay")) {
                            FullscreenActivity.mPreDelay = xpp.nextText();
                        } else if (xpp.getName().equals("books")) {
                            FullscreenActivity.mBooks = xpp.nextText();
                        } else if (xpp.getName().equals("midi")) {
                            FullscreenActivity.mMidi = xpp.nextText();
                        } else if (xpp.getName().equals("midi_index")) {
                            FullscreenActivity.mMidiIndex = xpp.nextText();
                        } else if (xpp.getName().equals("pitch")) {
                            FullscreenActivity.mPitch = xpp.nextText();
                        } else if (xpp.getName().equals("restrictions")) {
                            FullscreenActivity.mRestrictions = xpp.nextText();
                        } else if (xpp.getName().equals("notes")) {
                            FullscreenActivity.mNotes = xpp.nextText();
                        } else if (xpp.getName().equals("linked_songs")) {
                            FullscreenActivity.mLinkedSongs = xpp.nextText();
                        } else if (xpp.getName().equals("pad_file")) {
                            FullscreenActivity.mPadFile = xpp.nextText();
                        } else if (xpp.getName().equals("custom_chords")) {
                            FullscreenActivity.mCustomChords = xpp.nextText();
                        } else if (xpp.getName().equals("link_youtube")) {
                            FullscreenActivity.mLinkYouTube = xpp.nextText();
                        } else if (xpp.getName().equals("link_web")) {
                            FullscreenActivity.mLinkWeb = xpp.nextText();
                        } else if (xpp.getName().equals("link_audio")) {
                            FullscreenActivity.mLinkAudio = xpp.nextText();
                        } else if (xpp.getName().equals("link_other")) {
                            FullscreenActivity.mLinkOther = xpp.nextText();
                        }
                    }
                    eventType = xpp.next();

                }
            }

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
	// NEW
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
			e.printStackTrace();
		}

		return outputStream.toString();

	}
	// END NEW

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
        if (what.contains("Notes/")) {
            FullscreenActivity.whichSongFolder = "../Notes";
            FullscreenActivity.whattodo = "customreusable_note";
        } else if (what.contains("Slides/")) {
            FullscreenActivity.whichSongFolder = "../Slides";
            FullscreenActivity.whattodo = "customreusable_slide";
        } else if (what.contains("Images/")) {
            FullscreenActivity.whichSongFolder = "../Images";
            FullscreenActivity.whattodo = "customreusable_image";
        }

        // Load up the XML
        try {
            loadXML();
        } catch (Exception e) {
            e.printStackTrace();
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
        Preferences.savePreferences();
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
        FullscreenActivity.mLinkOther = "";
	}
}