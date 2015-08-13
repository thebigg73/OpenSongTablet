package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.util.Log;

public class ExportPreparer extends Activity {

	static String setxml = "";
	static String settext = "";
	static String songtext = "";
	static String song_title = "";
	static String song_author = "";
	static String song_copyright = "";
	static String song_hymnnumber = "";
	static String song_key = "";
	static String song_lyrics_withchords = "";
	static String song_lyrics_withoutchords = "";
	static File songfile = null;
	static ArrayList<String> filesinset = new ArrayList<>();

	public static void songParser() throws XmlPullParserException, IOException {

		songfile = null;
		if (!FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            songfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
        } else {
			songfile = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
		}
		getSongData();
		songtext = "";
		if (!song_title.isEmpty()) {
			songtext = songtext + song_title + "\n";
		} else {
			songtext = songtext + songfile.toString();
		}
		if (!song_author.isEmpty()) {
			songtext = songtext + song_author + "\n";
		}
		if (!song_copyright.isEmpty()) {
			songtext = songtext + song_copyright + "\n";
		}
		songtext = songtext + "\n\n\n" + song_lyrics_withchords;
		songtext = songtext + "\n\n\n\n_______________________________\n\n\n\n" + song_lyrics_withoutchords;

		FullscreenActivity.emailtext = songtext;
	}

	public static boolean setParser() throws IOException, XmlPullParserException {

        settext = "";
        FullscreenActivity.exportsetfilenames.clear();

		// First up, load the set
		File settoparse = new File(FullscreenActivity.dirsets + "/" + FullscreenActivity.settoload);
		if (!settoparse.isFile() || !settoparse.exists()) {
			return false;
		}

		try {
			FileInputStream inputStreamSet = new FileInputStream(settoparse);
			if (inputStreamSet != null) {
				InputStreamReader streamReaderSet = new InputStreamReader(inputStreamSet);
				BufferedReader bufferedReaderSet = new BufferedReader(streamReaderSet);
				setxml = readTextFile(inputStreamSet);
				inputStreamSet.close();
				bufferedReaderSet.close();
			}
			inputStreamSet.close(); // close the file
		} catch (java.io.FileNotFoundException e) {
			// file doesn't exist
		} catch (IOException e) {
			e.printStackTrace();
		}

		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();

		factory.setNamespaceAware(true);
		XmlPullParser xpp;
		xpp = factory.newPullParser();

		xpp.setInput(new StringReader(setxml));
		int eventType;

		eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("slide_group")) {
					if (xpp.getAttributeValue(1).equals("song")) {
						songfile = null;
                        String thisline = "";
						songfile = new File(FullscreenActivity.homedir + "/Songs/" + xpp.getAttributeValue(3) + xpp.getAttributeValue(0));
						if (xpp.getAttributeValue(3).equals("")) {
                            thisline = "/" + xpp.getAttributeValue(0);
                        } else {
                            thisline = xpp.getAttributeValue(3) + xpp.getAttributeValue(0);
                        }
                        filesinset.add(thisline);

                        song_title = xpp.getAttributeValue(0);
						song_author = "";
						song_hymnnumber = "";
						song_key = "";
						if (songfile.exists() && songfile.isFile()) {
							// Read in the song title, author, copyright, hymnnumber, key
							getSongData();
						}
						settext = settext + FullscreenActivity.song + " : " + song_title;
						if (!song_author.isEmpty()) {
							settext = settext + ", " + song_author;
						}
						if (!song_hymnnumber.isEmpty()) {
							settext = settext + ", #" + song_hymnnumber;
						}
						if (!song_key.isEmpty()) {
							settext = settext + " (" + song_key + ")";
						}
						settext = settext + "\n";
					} else if (xpp.getAttributeValue(0).equals("scripture")) {
						settext = settext + FullscreenActivity.scripture.toUpperCase(Locale.getDefault()) + " : " + xpp.getAttributeValue(1) + "\n";

					} else if (xpp.getAttributeValue(1).equals("scripture")) {
						settext = settext + FullscreenActivity.scripture.toUpperCase(Locale.getDefault()) + " : " + xpp.getAttributeValue(0) + "\n";

					} else if (xpp.getAttributeValue(0).equals("custom")) {
						settext = settext + FullscreenActivity.slide.toUpperCase(Locale.getDefault()) + " : " + xpp.getAttributeValue(1) + "\n";

					} else if (xpp.getAttributeValue(1).equals("custom")) {
						settext = settext + FullscreenActivity.slide.toUpperCase(Locale.getDefault()) + " : " + xpp.getAttributeValue(0) + "\n";
					}
				}
			}
			eventType = xpp.next();		
		}

		// Send the settext back to the FullscreenActivity as emailtext
		FullscreenActivity.emailtext = settext;
        FullscreenActivity.exportsetfilenames = filesinset;
		return true;
	}

	public static void getSongData() throws XmlPullParserException, IOException {
		// Parse the song xml.
		// Grab the title, author, lyrics_withchords, lyrics_withoutchords, copyright, hymnnumber, key

		// Initialise all the xml tags a song should have that we want
		String songxml = "";
		song_title = "";
		song_author = "";
		song_lyrics_withchords = "";
		song_lyrics_withoutchords = "";
		song_copyright = "";
		song_hymnnumber = "";
		song_key = "";

		try {
			FileInputStream inputStreamSong = new FileInputStream(songfile);
			if (inputStreamSong != null) {
				InputStreamReader streamReaderSong = new InputStreamReader(inputStreamSong);
				BufferedReader bufferedReaderSong = new BufferedReader(streamReaderSong);
				songxml = readTextFile(inputStreamSong);
				inputStreamSong.close();
				bufferedReaderSong.close();
			}
			inputStreamSong.close(); // close the file
		} catch (java.io.FileNotFoundException e) {
			// file doesn't exist
			//song_title = songfile.toString();
		} catch (IOException e) {
			e.printStackTrace();
			//song_title = songfile.toString();
		}

		//Change the line breaks and Slides to better match OpenSong
		songxml = songxml.replaceAll("\r\n", "\n");
		songxml = songxml.replaceAll("\r", "\n");
		songxml = songxml.replaceAll("\t", "    ");
		songxml = songxml.replaceAll("\\t", "    ");
		songxml = songxml.replaceAll("\b", "    ");
		songxml = songxml.replaceAll("\f", "    ");
		songxml = songxml.replace("\r", "");
		songxml = songxml.replace("\t", "    ");
		songxml = songxml.replace("\b", "    ");
		songxml = songxml.replace("\f", "    ");
        songxml = songxml.replace("&#0;","");

		// Extract all of the key bits of the song
		XmlPullParserFactory factorySong;
		factorySong = XmlPullParserFactory.newInstance();

		factorySong.setNamespaceAware(true);
		XmlPullParser xppSong;
		xppSong = factorySong.newPullParser();

		xppSong.setInput(new StringReader(songxml));

		int eventType;
		eventType = xppSong.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xppSong.getName().equals("author")) {
					song_author = xppSong.nextText();
				} else if (xppSong.getName().equals("copyright")) {
					song_copyright = xppSong.nextText();
				} else if (xppSong.getName().equals("title")) {
					song_title = xppSong.nextText();
				} else if (xppSong.getName().equals("lyrics")) {
					song_lyrics_withchords = xppSong.nextText();
				} else if (xppSong.getName().equals("hymn_number")) {
					song_hymnnumber = xppSong.nextText();
				} else if (xppSong.getName().equals("key")) {
					song_key = xppSong.nextText();
				}
			}
			eventType = xppSong.next();
		}
		// Remove the chord lines from the song lyrics
		String[] templyrics = song_lyrics_withchords.split("\n");
		// Only add the lines that don't start with a .
		int numlines = templyrics.length;
		if (numlines>0) {
            for (String templyric : templyrics) {
                if (!templyric.startsWith(".")) {
                    song_lyrics_withoutchords = song_lyrics_withoutchords + templyric + "\n";
                }
            }
		}
	}

	private static String readTextFile(InputStream inputStream) {
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

}
