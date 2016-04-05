package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.os.Bundle;

public class BibleSelect extends Activity {

	// This is the bible viewing app
	// This lets users view passages of the bible on their device (like a song)
	// It will allow users to store notes with their bibles

	// Gets the data repository in write mode
	
	// Not done anything with it yet though!!!!
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the user preferences
		Preferences.loadPreferences();
		// Set the screen and title
		setContentView(R.layout.bible_view);
		
		try {
			loadBible();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	public void loadBible() throws XmlPullParserException, IOException {
		// Load the bible up if the bibleFile exists
		File myFile = new File(FullscreenActivity.dirbibles+"/"+FullscreenActivity.bibleFile);	
		if (myFile.exists()) {
			// Load in the bible file if it isn't loaded already
			if (!FullscreenActivity.bibleLoaded || FullscreenActivity.bibleFileContents.isEmpty()) {
				try {
					FileInputStream inputStream = new FileInputStream(new File(
							FullscreenActivity.dirbibles + "/"
									+ FullscreenActivity.bibleFile));

					if (inputStream != null) {
						InputStreamReader streamReader = new InputStreamReader(inputStream);
						BufferedReader bufferedReader = new BufferedReader(streamReader);
						String l;
						int count = 0;
						while ((l = bufferedReader.readLine()) != null) {
							// do what you want with the line
							FullscreenActivity.bibleFileContents = FullscreenActivity.bibleFileContents + l
									+ "\n";
							count = count + 1;
						}
						inputStream.close();
						bufferedReader.close();
					}
					inputStream.close(); // close the file
				} catch (java.io.FileNotFoundException e) {
					// file doesn't exist
					// Alert the user
					FullscreenActivity.bibleFileContents = "NO BIBLE CONTENTS";
				}
			}
			
		} else {
			FullscreenActivity.bibleFileContents = "NO BIBLE CONTENTS";
		}
			
		// Extract all of the key bits of the bible
		XmlPullParserFactory factory = null;
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = null;
		xpp = factory.newPullParser();
		xpp.setInput(new StringReader(FullscreenActivity.bibleFileContents));
		int eventType = 0;
		eventType = xpp.getEventType();
				 
		// Initialise arrays and strings
		 int booknum  = 0;
		 int chaptnum = 0;
		 int versenum = 0;
		 String bookname = "";	 

		 while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("b")) {
					if (xpp.getAttributeCount()>0) {
						booknum++;
						chaptnum = 0;
						versenum = 0;
						bookname = xpp.getAttributeValue(0);
					}
				} else if (xpp.getName().equals("c")) {
					if (xpp.getAttributeCount()>0) {
						chaptnum = Integer.parseInt(xpp.getAttributeValue(0));
						versenum = 0;
					}
				} else if (xpp.getName().equals("v")) {
					if (xpp.getAttributeCount()>0) {
						versenum = Integer.parseInt(xpp.getAttributeValue(0));
					}
					FullscreenActivity.bibleVerse[booknum][chaptnum][versenum] = xpp.nextText();
				}
				eventType = xpp.next();
			}
		 }
	}
}

