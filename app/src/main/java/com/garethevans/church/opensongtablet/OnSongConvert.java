package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.util.Log;

public class OnSongConvert extends Activity {

	public static boolean doExtract() throws IOException {

		// This is called when a OnSong format song has been loaded.
		// This tries to extract the relevant stuff and reformat the
		// <lyrics>...</lyrics>
		String temp = FullscreenActivity.myXML;
		String parsedlines;
		// Initialise all the xml tags a song should have
        Log.d("d","temp="+temp);
        FullscreenActivity.mTitle = FullscreenActivity.songfilename.replaceAll("[\\x0-\\x9]", "");
        // Initialise all the other tags
        LoadXML.initialiseSongTags();

        // Break the temp variable into an array split by line
		// Check line endings are \n
        temp = temp.replaceAll("[\\x0-\\x9]", "");
        //temp = temp.replace("\\x0","");
        temp = temp.replace("\0", "");
        while (temp.contains("\r\n") || temp.contains("\r") || temp.contains("\n\n\n")) {
            temp = temp.replace("\r\n","\n");
            temp = temp.replace("\r", "\n");
            temp = temp.replace("\n\n\n", "\n\n");
        }
        temp = temp.replace("\t", "    ");
		temp = temp.replace("\'", "'");
		temp = temp.replace("&quot;", "\"");
		temp = temp.replace("\\'", "'");
		temp = temp.replace("&quot;", "\"");
		String[] line = temp.split("\n");
		int numlines = line.length;
		if (numlines < 0) {
			numlines = 1;
			temp = " ";
		}

        //Go through the lines and get rid of rubbish
        for (int c=0;c<numlines;c++) {
            line[c] = line[c].replaceAll("[^\\x20-\\x7e]", "");
        }

		// Extract the metadata
		// This is all the lines before the first blank line
		int metadatastart = 0;
		int metadataend = 0;
		
		for (int z = metadatastart; z < numlines; z++) {
			Log.d("OnSong","line["+z+"]="+line[z]);
			if (line[z].isEmpty()) {
				// This is the end of the metadata
				metadataend = z;
				break;
			}
		}
		
		// Go through the metadata lines and try to extract any stuff we can
		for (int x = metadatastart; x < metadataend; x++) {
			// Homogenise all tags!
			// Make tag lines common
			
			// Meta data tags at the top
			line[x] = line[x].replace("{ns", "{new_song");
			line[x] = line[x].replace("{title :", "{title:");
			line[x] = line[x].replace("{Title:", "{title:");
			line[x] = line[x].replace("{t:", "{title:");
			line[x] = line[x].replace("{t :", "{title:");
			line[x] = line[x].replace("{T:", "{title:");
			line[x] = line[x].replace("{subtitle :", "{artist:");
			line[x] = line[x].replace("{Subtitle:", "{artist:");
			line[x] = line[x].replace("{St:", "{artist:");
			line[x] = line[x].replace("{st:", "{artist:");
			line[x] = line[x].replace("{st :", "{artist:");
			line[x] = line[x].replace("{su:", "{artist:");
			line[x] = line[x].replace("{su :", "{artist:");
			line[x] = line[x].replace("{artist :", "{artist:");
			line[x] = line[x].replace("{a:", "{artist:");
			line[x] = line[x].replace("{author :", "{author:");			
			line[x] = line[x].replace("{copyright :","{copyright:");
			line[x] = line[x].replace("{footer:","{copyright:");
			line[x] = line[x].replace("{footer :","{copyright:");
			line[x] = line[x].replace("{key :","{key:");
			line[x] = line[x].replace("{k:","{key:");
			line[x] = line[x].replace("{k :","{key:");
			line[x] = line[x].replace("{capo :","{capo:");
			line[x] = line[x].replace("{time :","{time:");
			line[x] = line[x].replace("{tempo :","{tempo:");
			line[x] = line[x].replace("{duration :","{duration:");
			line[x] = line[x].replace("{number :","{number:");
			line[x] = line[x].replace("{flow :","{flow:");
			line[x] = line[x].replace("{ccli :","{ccli:");
			line[x] = line[x].replace("{keywords :","{keywords:");
			line[x] = line[x].replace("{topic:","{keywords:");
			line[x] = line[x].replace("{topic :","{keywords:");
			line[x] = line[x].replace("{book :","{book:");
			line[x] = line[x].replace("{midi :","{midi:");
			line[x] = line[x].replace("{midi-index :","{midi-index:");
			line[x] = line[x].replace("{pitch :","{pitch:");
			line[x] = line[x].replace("{restrictions :","{restrictions:");
			
			// In line
			line[x] = line[x].replace("{comments :", "{comments:");
			line[x] = line[x].replace("{c:", "{comments:");
			line[x] = line[x].replace("{c :", "{comments:");
			line[x] = line[x].replace("{guitar_comment :", "{guitar_comment:");
			line[x] = line[x].replace("{gc:", "{guitar_comment:");
			line[x] = line[x].replace("{gc :", "{guitar_comment:");
			line[x] = line[x].replace("{sot", "{start_of_tab");
			line[x] = line[x].replace("{sob", "{start_of_bridge");
			line[x] = line[x].replace("{eot", "{end_of_tab");
			line[x] = line[x].replace("{eob", "{end_of_bridge");
			line[x] = line[x].replace("{soc", "{start_of_chorus");
			line[x] = line[x].replace("{eoc", "{end_of_chorus");
			line[x] = line[x].replace("{comment_italic :", "{comments:");
			line[x] = line[x].replace("{ci:", "{comments:");
			line[x] = line[x].replace("{ci :", "{comments:");
			line[x] = line[x].replace("{textfont :", "{textfont:");
			line[x] = line[x].replace("{textsize :", "{textsize:");
			line[x] = line[x].replace("{chordfont :", "{chordfont:");
			line[x] = line[x].replace("{chordsize :", "{chordsize:");
			line[x] = line[x].replace("{ng", "{nogrid");
			line[x] = line[x].replace("{g}", "{grid}");
			line[x] = line[x].replace("{d:", "{define:");
			line[x] = line[x].replace("{titles :", "{titles:");
			line[x] = line[x].replace("{npp", "{new_physical_page");
			line[x] = line[x].replace("{np", "{new_page");
			line[x] = line[x].replace("{columns :", "{columns:");
			line[x] = line[x].replace("{col:", "{columns:");
			line[x] = line[x].replace("{col :", "{columns:");
			line[x] = line[x].replace("{column_break :", "{column_break:");
			line[x] = line[x].replace("{colb:", "{column_break:");
			line[x] = line[x].replace("{colb :", "{column_break:");
			line[x] = line[x].replace("{pagetype :", "{pagetype:");	

			if (line[x].indexOf("{title:")==0) {
				// extract the title
				line[x] = line[x].replace("{title:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTitle = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Title:")==0) {
				// extract the title
				line[x] = line[x].replace("Title:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTitle = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{artist:")==0) {
				// extract the artist
				line[x] = line[x].replace("{artist:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mAuthor = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Artist:")==0) {
				// extract the artist
				line[x] = line[x].replace("Artist:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mAuthor = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{author:")==0) {
				// extract the author (of chart)
				line[x] = line[x].replace("{author:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mUser1 = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Author:")==0) {
				// extract the author (of chart)
				line[x] = line[x].replace("Author:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mUser1 = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{copyright:")==0) {
				// extract the copyright
				line[x] = line[x].replace("{copyright:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCopyright = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Copyright:")==0 || line[x].indexOf("Footer:")==0) {
				// extract the copyright info
				line[x] = line[x].replace("Copyright:", "");
				line[x] = line[x].replace("Footer:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCopyright = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{key:")==0) {
				// extract the key
				line[x] = line[x].replace("{key:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mKey = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Key:")==0) {
				line[x] = line[x].replace("Key:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mKey = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{capo:")==0) {
				// extract the capo
				line[x] = line[x].replace("{capo:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCapo = line[x];
				FullscreenActivity.mCapoPrint = "true";
				line[x] = "";
			}
			if (line[x].indexOf("Capo:")==0) {
				// extract the capo fret
				line[x] = line[x].replace("Capo:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCapo = line[x];
				FullscreenActivity.mCapoPrint = "true";
				line[x] = "";
			}


			if (line[x].indexOf("{time:")==0) {
				// extract the time signature
				line[x] = line[x].replace("{time:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTimeSig = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Time:")==0) {
				line[x] = line[x].replace("Time:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTimeSig = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{tempo:")==0) {
				// extract the tempo
				line[x] = line[x].replace("{tempo:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTempo = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Tempo:")==0) {
				line[x] = line[x].replace("Tempo:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTempo = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{duration:")==0) {
				// extract the duration
				line[x] = line[x].replace("{duration:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mDuration = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Duration:")==0) {
				line[x] = line[x].replace("Duration:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mDuration = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{number:")==0) {
				// extract the Hymn number
				line[x] = line[x].replace("{number:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mHymnNumber = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Number:")==0) {
				line[x] = line[x].replace("Number:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mHymnNumber = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{flow:")==0) {
				// extract the Presentation sequence
				line[x] = line[x].replace("{flow:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPresentation = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Flow:")==0) {
				line[x] = line[x].replace("Flow:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPresentation = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{ccli:")==0) {
				// extract the CCLI
				line[x] = line[x].replace("{ccli:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCCLI = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("CCLI:")==0) {
				line[x] = line[x].replace("CCLI:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mCCLI = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{keywords:")==0) {
				// extract the Themes
				line[x] = line[x].replace("{keywords:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTheme = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Keywords:")==0 || line[x].indexOf("Topic:")==0) {
				line[x] = line[x].replace("Keywords:", "");
				line[x] = line[x].replace("Topic:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mTheme = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{book:")==0) {
				// extract the books
				line[x] = line[x].replace("{book:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mBooks = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Book:")==0) {
				line[x] = line[x].replace("Book:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mBooks = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{midi:")==0) {
				// extract the midi
				line[x] = line[x].replace("{midi:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidi = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("MIDI:")==0) {
				line[x] = line[x].replace("MIDI:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidi = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{midi-index:")==0) {
				// extract the midi index
				line[x] = line[x].replace("{midi-index:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidiIndex = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("MIDI-Index:")==0) {
				line[x] = line[x].replace("MIDI-Index:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mMidiIndex = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{pitch:")==0) {
				// extract the Pitch
				line[x] = line[x].replace("{pitch:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPitch = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Pitch:")==0) {
				line[x] = line[x].replace("Pitch:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mPitch = line[x];
				line[x] = "";
			}

			if (line[x].indexOf("{restrictions:")==0) {
				// extract the restrictions
				line[x] = line[x].replace("{restrictions:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				FullscreenActivity.mRestrictions = line[x];
				line[x] = "";
			}
			if (line[x].indexOf("Restrictions:")==0) {
				line[x] = line[x].replace("Restrictions:", "");
				line[x] = line[x].trim();
				FullscreenActivity.mRestrictions = line[x];
				line[x] = "";
			}
		}
		
	if (metadataend>=0) {
		//First line is the title
		if (!line[0].isEmpty()) {
			FullscreenActivity.mTitle = line[0];
			line[0] = "";
		}
	}
	if (metadataend>=1) {
		// Second line is the author	
		if (!line[1].isEmpty()) {
			FullscreenActivity.mAuthor = line[1];
			line[1] = "";
		}
	}
	
	// Now look for the rest of the song stuff
	for (int x=0;x<numlines;x++) {
		// Change lines that start with # into comment lines
		if (line[x].indexOf("#") == 0) {
			line[x] = line[x].replaceFirst("#", ";");
			// Get rid of any extra whitespace
			line[x] = line[x].trim();			
		}
		
		// Change lines that start with backticks into chord line
		if (line[x].indexOf("`") == 0) {
			line[x] = line[x].replaceFirst("`", ".");
		}
		
		// If a line has more space than characters it is likely to be a chord line
		int getlengthofline = line[x].length();
		int linewithoutspaces = line[x].replace(" ", "").length();
		int amountofspace = getlengthofline - linewithoutspaces;
		if (amountofspace>linewithoutspaces && line[x].indexOf(".")!=0) {
			line[x] = "." + line[x];
		}

		// Change guitar comments into comment lines
		if (line[x].indexOf("{guitar_comment:") == 0) {
//			line[x] = line[x].replaceFirst("{guitar_comment:", ";");
			line[x] = line[x].replaceFirst("\\{guitar_comment:", ";");
			line[x] = line[x].replaceFirst("}", "");
			// Get rid of any extra whitespace
			line[x] = line[x].trim();			
		}

		// Get rid of formatting tags we won't use
		if (line[x].contains("{textsize") || line[x].contains("{textfont")
				|| line[x].contains("{chordsize") || line[x].contains("{chordfont")) {
			line[x] = "";
		}
		
		if (line[x].indexOf("*")==0 || line[x].indexOf("/")==0 || line[x].indexOf("!")==0) {
			if (line[x].length()>1) {
				line[x] = line[x].substring(1);
			}
		}
		
		if (line[x].indexOf("&")==0 || line[x].indexOf(">")==0) {
			int endofformattingtag = line[x].indexOf(":");
			if (endofformattingtag==-1) {
				endofformattingtag = 0;
			}
			if ((endofformattingtag+1) <= line[x].length()) {
				line[x] = line[x].substring(endofformattingtag+1);
			}
		}

		// Remove < > tags
		line[x] = line[x].replace("<", "(");
		line[x] = line[x].replace(">", ")");
		

		// Get rid of any extra whitespace
		line[x] = line[x].trim();			
	}
	
		// Go through each line and try to fix chord lines
		for (int x = metadataend; x < numlines; x++) {
			line[x] = line[x].trim();
			String tempchordline = "";

			// Look for [ and ] signifying a chord
			while (line[x].contains("[") && line[x].contains("]")) {

				// Find chord start and end pos
				int chordstart = line[x].indexOf("[");
				int chordend = line[x].indexOf("]");
				String chord;
				if (chordend > chordstart) {
					chord = line[x].substring(chordstart, chordend + 1);
					String substart = line[x].substring(0, chordstart);
					String subsend = line[x].substring(chordend + 1);
					line[x] = substart + subsend;
				} else {
					chord = "";
					line[x] = line[x].replace("[", "");
					line[x] = line[x].replace("]", "");
				}
				chord = chord.replace("[", "");
				chord = chord.replace("]", "");

				// Add the chord to the tempchordline
				if (tempchordline.length() > chordstart) {
					// We need to put the chord at the end stuff already there
					// Don't overwrite - This is because the length of the 
					// previous chord is bigger than the lyrics following it
					chordstart = tempchordline.length() + 1;
				}
				for (int z = tempchordline.length(); z < chordstart; z++) {
					tempchordline = tempchordline + " ";
				}
				// Now add the chord
				tempchordline = tempchordline + chord;

			}
			// All chords should be gone now, so remove any remaining [ and ]
			line[x] = line[x].replace("[", "");
			line[x] = line[x].replace("]", "");
			if (tempchordline.length() > 0) {
				line[x] = "." + tempchordline + "\n" + line[x];
			}
			tempchordline = "";
		}

		// Join the individual lines back up
		parsedlines = "";
		for (int x = 0; x < numlines; x++) {
			// Try to guess tags used
			if (line[x].indexOf(";")!=0) {
				line[x] = line[x].replace("Intro:", "[Intro]");
				line[x] = line[x].replace("Outro:", "[Outro]");
				line[x] = line[x].replace("V:", "[V]");
				line[x] = line[x].replace("V1:", "[V1]");
				line[x] = line[x].replace("V2:", "[V2]");
				line[x] = line[x].replace("V3:", "[V3]");
				line[x] = line[x].replace("V4:", "[V4]");
				line[x] = line[x].replace("V5:", "[V5]");
				line[x] = line[x].replace("V6:", "[V6]");
				line[x] = line[x].replace("V7:", "[V7]");
				line[x] = line[x].replace("V8:", "[V8]");
				line[x] = line[x].replace("V9:", "[V9]");
				line[x] = line[x].replace("Verse:", "[V]");
				line[x] = line[x].replace("Verse 1:", "[V1]");
				line[x] = line[x].replace("Verse 2:", "[V2]");
				line[x] = line[x].replace("Verse 3:", "[V3]");
				line[x] = line[x].replace("Verse 4:", "[V4]");
				line[x] = line[x].replace("(Verse)", "[V]");
				line[x] = line[x].replace("(Verse 1)", "[V1]");
				line[x] = line[x].replace("(Verse 2)", "[V2]");
				line[x] = line[x].replace("(Verse 3)", "[V3]");				
				line[x] = line[x].replace("(Chorus)", "[C]");
				line[x] = line[x].replace("C:", "[C]");
				line[x] = line[x].replace("C1:", "[C1]");
				line[x] = line[x].replace("C2:", "[C2]");
				line[x] = line[x].replace("C3:", "[C3]");
				line[x] = line[x].replace("C4:", "[C4]");
				line[x] = line[x].replace("C5:", "[C5]");
				line[x] = line[x].replace("C6:", "[C6]");
				line[x] = line[x].replace("C7:", "[C7]");
				line[x] = line[x].replace("C8:", "[C8]");
				line[x] = line[x].replace("C9:", "[C9]");
				line[x] = line[x].replace("Chorus:", "[C]");
				line[x] = line[x].replace("Chorus 1:", "[C1]");
				line[x] = line[x].replace("Chorus 2:", "[C2]");
				line[x] = line[x].replace("Chorus 3:", "[C3]");
				line[x] = line[x].replace("Prechorus:", "[P]");
				line[x] = line[x].replace("Prechorus 1:", "[P1]");
				line[x] = line[x].replace("Prechorus 2:", "[P2]");
				line[x] = line[x].replace("Prechorus 3:", "[P3]");
				line[x] = line[x].replace("Bridge:", "[B]");
				line[x] = line[x].replace("Tag:", "[T]");
				line[x] = line[x].replace(" Intro:", "[Intro]");
				line[x] = line[x].replace(" Outro:", "[Outro]");
				line[x] = line[x].replace(" V:", "[V]");
				line[x] = line[x].replace(" V1:", "[V1]");
				line[x] = line[x].replace(" V2:", "[V2]");
				line[x] = line[x].replace(" V3:", "[V3]");
				line[x] = line[x].replace(" V4:", "[V4]");
				line[x] = line[x].replace(" V5:", "[V5]");
				line[x] = line[x].replace(" V6:", "[V6]");
				line[x] = line[x].replace(" V7:", "[V7]");
				line[x] = line[x].replace(" V8:", "[V8]");
				line[x] = line[x].replace(" V9:", "[V9]");
				line[x] = line[x].replace(" Verse:", "[V]");
				line[x] = line[x].replace(" Verse 1:", "[V1]");
				line[x] = line[x].replace(" Verse 2:", "[V2]");
				line[x] = line[x].replace(" Verse 3:", "[V3]");
				line[x] = line[x].replace(" Verse 4:", "[V4]");
				line[x] = line[x].replace(" (Verse)", "[V]");
				line[x] = line[x].replace(" (Verse 1)", "[V1]");
				line[x] = line[x].replace(" (Verse 2)", "[V2]");
				line[x] = line[x].replace(" (Verse 3)", "[V3]");
				line[x] = line[x].replace(" (Chorus)", "[C]");
				line[x] = line[x].replace(" C:", "[C]");
				line[x] = line[x].replace(" C1:", "[C1]");
				line[x] = line[x].replace(" C2:", "[C2]");
				line[x] = line[x].replace(" C3:", "[C3]");
				line[x] = line[x].replace(" C4:", "[C4]");
				line[x] = line[x].replace(" C5:", "[C5]");
				line[x] = line[x].replace(" C6:", "[C6]");
				line[x] = line[x].replace(" C7:", "[C7]");
				line[x] = line[x].replace(" C8:", "[C8]");
				line[x] = line[x].replace(" C9:", "[C9]");
				line[x] = line[x].replace(" Chorus:", "[C]");
				line[x] = line[x].replace(" Chorus 1:", "[C1]");
				line[x] = line[x].replace(" Chorus 2:", "[C2]");
				line[x] = line[x].replace(" Chorus 3:", "[C3]");
				line[x] = line[x].replace(" Prechorus:", "[P]");
				line[x] = line[x].replace(" Prechorus 1:", "[P1]");
				line[x] = line[x].replace(" Prechorus 2:", "[P2]");
				line[x] = line[x].replace(" Prechorus 3:", "[P3]");
				line[x] = line[x].replace(" Bridge:", "[B]");
				line[x] = line[x].replace(" Tag:", "[T]");

				// Guess custom tags - will be a short line with :
				if (line[x].length()<15 && line[x].contains(":") && line[x].indexOf("{")!=0) {
					line[x] = "[" + line[x].replace(":","") + "]";
				}
			}
			parsedlines = parsedlines + line[x] + "\n";
		}

		
		// Remove start and end of tabs
		while (parsedlines.contains("{start_of_tab") && parsedlines.contains("{end_of_tab")) {
			int startoftabpos;
			int endoftabpos;
			startoftabpos = parsedlines.indexOf("{start_of_tab");
			endoftabpos = parsedlines.indexOf("{end_of_tab") + 12;
			
			if (endoftabpos > 13 && startoftabpos > -1 && endoftabpos > startoftabpos) {
				String startbit = parsedlines.substring(0, startoftabpos);
				String endbit = parsedlines.substring(endoftabpos);
				parsedlines = startbit + endbit;
			}
		}
		
		// Change start and end of chorus
		while (parsedlines.contains("{start_of_chorus")) {
			parsedlines = parsedlines.replace("{start_of_chorus}","[C]");
			parsedlines = parsedlines.replace("{start_of_chorus:}","[C]");
			parsedlines = parsedlines.replace("{start_of_chorus :}","[C]");
			parsedlines = parsedlines.replace("{start_of_chorus","[C]");
			parsedlines = parsedlines.replace(":","");
			parsedlines = parsedlines.replace("}","");
		}

		while (parsedlines.contains("{end_of_chorus")) {
			parsedlines = parsedlines.replace("{end_of_chorus}","[]");
			parsedlines = parsedlines.replace("{end_of_chorus:}","[]");
			parsedlines = parsedlines.replace("{end_of_chorus :}","[]");
			parsedlines = parsedlines.replace("{end_of_chorus","[]");
			parsedlines = parsedlines.replace(":","");
			parsedlines = parsedlines.replace("}","");
		}

		// Change start and end of bridge
		while (parsedlines.contains("{start_of_bridge")) {
			parsedlines = parsedlines.replace("{start_of_bridge}","[B]");
			parsedlines = parsedlines.replace("{start_of_bridge:}","[B]");
			parsedlines = parsedlines.replace("{start_of_bridge :}","[B]");
			parsedlines = parsedlines.replace("{start_of_bridge","[B]");
			parsedlines = parsedlines.replace(":","");
			parsedlines = parsedlines.replace("}","");
		}

		while (parsedlines.contains("{end_of_bridge")) {
			parsedlines = parsedlines.replace("{end_of_bridge}","[]");
			parsedlines = parsedlines.replace("{end_of_bridge:}","[]");
			parsedlines = parsedlines.replace("{end_of_bridge :}","[]");
			parsedlines = parsedlines.replace("{end_of_bridge","[]");
			parsedlines = parsedlines.replace(":","");
			parsedlines = parsedlines.replace("}","");
		}
		
		// Get rid of double line breaks
		while (parsedlines.contains("\n\n\n")) {
			parsedlines = parsedlines.replace("\n\n\n","\n\n");
		}

		while (parsedlines.contains(";\n\n;")) {
			parsedlines = parsedlines.replace(";\n\n;",";\n");
		}

		// Ok, go back through the parsed lines and add spaces to the beginning
		// of lines that aren't comments, chords or tags
		String[] line2 = parsedlines.split("\n");
		int numlines2 = line2.length;
		if (numlines2 < 0) {
			numlines2 = 1;
		}
		// Reset the parsed lines
		parsedlines = "";

		// Go through the lines one at a time
		// Add the fixed bit back together
		for (int x = 0; x < numlines2; x++) {
			if (line2[x].length() > 1) {
				if (line2[x].indexOf("[") != 0 && line2[x].indexOf(";") != 0
						&& line2[x].indexOf(".") != 0) {
					line2[x] = " " + line2[x];
				}
			}

			parsedlines = parsedlines + line2[x] + "\n";
		}

		FullscreenActivity.myXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<song>\n"
				+ "<title>" + FullscreenActivity.mTitle + "</title>\n"
				+ "<author>" + FullscreenActivity.mAuthor + "</author>\n"
				+ "<copyright>" + FullscreenActivity.mCopyright + "</copyright>\n"
				+ "  <presentation>" + FullscreenActivity.mPresentation + "</presentation>\n"
				+ "  <hymn_number>" + FullscreenActivity.mHymnNumber + "</hymn_number>\n"
				+ "  <capo print=\"" + FullscreenActivity.mCapoPrint + "\">" + FullscreenActivity.mCapo + "</capo>\n"
				+ "  <tempo>" + FullscreenActivity.mTempo + "</tempo>\n"
				+ "  <time_sig>" + FullscreenActivity.mTimeSig + "</time_sig>\n"
				+ "  <duration>" + FullscreenActivity.mDuration + "</duration>\n"
				+ "  <ccli>" + FullscreenActivity.mCCLI + "</ccli>\n"
				+ "  <theme>" + FullscreenActivity.mTheme + "</theme>\n"
				+ "  <alttheme></alttheme>\n"
				+ "  <user1>" + FullscreenActivity.mUser1 + "</user1>\n"
				+ "  <user2></user2>\n"
				+ "  <user3></user3>\n"
				+ "  <key>" + FullscreenActivity.mKey + "</key>\n"
				+ "  <aka></aka>\n"
				+ "  <key_line></key_line>\n"
				+ "  <books>" + FullscreenActivity.mBooks + "</books>\n"
				+ "  <midi>" + FullscreenActivity.mMidi + "</midi>\n"
				+ "  <midi_index>" + FullscreenActivity.mMidiIndex + "</midi_index>\n"
				+ "  <pitch>" + FullscreenActivity.mPitch + "</pitch>\n"
				+ "  <restrictions>" + FullscreenActivity.mRestrictions + "</restrictions>\n"
				+ "  <notes></notes>\n"
				+ "  <lyrics>" + parsedlines.trim() + "</lyrics>\n"
                + "  <linked_songs>" + FullscreenActivity.mLinkedSongs + "</linked_songs>\n"
                + "  <pad_file>" + FullscreenActivity.mPadFile + "</pad_file>\n"
                + "  <custom_chords>" + FullscreenActivity.mCustomChords + "</custom_chords>\n"
                + "  <link_youtube>" + FullscreenActivity.mLinkYouTube + "</custom_chords>\n"
                + "  <link_web>" + FullscreenActivity.mLinkWeb + "</link_web>\n"
                + "  <link_audio>" + FullscreenActivity.mLinkAudio + "</link_audio>\n"
                + "  <link_other>" + FullscreenActivity.mLinkOther + "</link_other>\n"
				+ "</song>";
		
		// Save this song in the right format!
		// Makes sure all & are replaced with &amp;
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("&amp;",
				"&");
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("&",
				"&amp;");
		
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("\'","'");
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Õ","'");
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Ó","'");
		FullscreenActivity.myXML = FullscreenActivity.myXML.replace("Ò","'");
		// Save the file
		Preferences.savePreferences();

		// Now write the modified song
		FileOutputStream overWrite;
		if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            overWrite = new FileOutputStream(FullscreenActivity.dir + "/"
                    + FullscreenActivity.songfilename, false);
        } else {
            overWrite = new FileOutputStream(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename, false);
        }
		overWrite.write(FullscreenActivity.myXML.getBytes());
		overWrite.flush();
		overWrite.close();

		// Change the name of the song to remove onsong file extension 
		// (not needed)
		String newSongTitle = FullscreenActivity.songfilename;

		// Decide if a better song title is in the file
		if (FullscreenActivity.mTitle.length() > 0) {
			newSongTitle = FullscreenActivity.mTitle.toString();
		}

		newSongTitle = newSongTitle.replace(".onsong", "");

		File from;
		File to;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            from = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + newSongTitle);
        } else {
            from = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + newSongTitle);
        }

        // IF THE FILENAME ALREADY EXISTS, REALLY SHOULD ASK THE USER FOR A NEW FILENAME
		// OR append _ to the end - STILL TO DO!!!!!
		while(to.exists()) {
			newSongTitle = newSongTitle+"_";
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                to = new File(FullscreenActivity.dir + "/" + newSongTitle);
            } else {
                to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + newSongTitle);
            }
        }
		
		// Do the renaming
		from.renameTo(to);
		FullscreenActivity.songfilename = newSongTitle;

		// Load the songs
		ListSongFiles.listSongs();

		// Get the song indexes
		ListSongFiles.getCurrentSongIndex();

		FullscreenActivity.needtorefreshsongmenu = true;
		Preferences.savePreferences();

        // Prepare the app to fix the song menu with the new file
        FullscreenActivity.converting = true;

		return true;
	}
}