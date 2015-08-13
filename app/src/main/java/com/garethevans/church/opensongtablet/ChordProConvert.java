package com.garethevans.church.opensongtablet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;

public class ChordProConvert extends Activity {

	public static boolean doExtract() throws IOException {

		// This is called when a ChordPro format song has been loaded.
		// This tries to extract the relevant stuff and reformat the
		// <lyrics>...</lyrics>
		String temp = FullscreenActivity.myXML;
		String parsedlines;
		// Initialise all the xml tags a song should have
		FullscreenActivity.mTitle = FullscreenActivity.songfilename;
		FullscreenActivity.mAuthor = "";
		FullscreenActivity.mCopyright = "";
		FullscreenActivity.mPresentation = "";
		FullscreenActivity.mHymnNumber = "";
		FullscreenActivity.mCapo = "";
		FullscreenActivity.mCapoPrint = "false";
		FullscreenActivity.mTempo = "";
		FullscreenActivity.mTimeSig = "";
		FullscreenActivity.mCCLI = "";
		FullscreenActivity.mTheme = "";
		FullscreenActivity.mAltTheme = "";
		FullscreenActivity.mUser1 = "";
		FullscreenActivity.mUser2 = "";
		FullscreenActivity.mUser3 = "";
		FullscreenActivity.mKey = "";
		FullscreenActivity.mAka = "";
		FullscreenActivity.mKeyLine = "";
		FullscreenActivity.mLyrics = "";
		FullscreenActivity.mStyle = "";
		FullscreenActivity.mStyleIndex = "default_style";

		// Break the temp variable into an array split by line
		// Check line endings are \n
		temp = temp.replace("\r\n", "\n");
		temp = temp.replace("\r", "\n");
		temp = temp.replace("\n\n\n", "\n\n");
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

		String temptitle = "";
		String tempsubtitle = "";

		
		// Go through individual lines and fix simple stuff
		for (int x = 0; x < numlines; x++) {

			// Get rid of any extra whitespace
			line[x] = line[x].trim();
			
			
			// Make tag lines common
			line[x] = line[x].replace("{ns", "{new_song");
			line[x] = line[x].replace("{title :", "{title:");
			line[x] = line[x].replace("{Title:", "{title:");
			line[x] = line[x].replace("{t:", "{title:");
			line[x] = line[x].replace("{t :", "{title:");
			line[x] = line[x].replace("{T:", "{title:");
			line[x] = line[x].replace("{subtitle :", "{subtitle:");
			line[x] = line[x].replace("{Subtitle:", "{subtitle:");
			line[x] = line[x].replace("{St:", "{subtitle:");
			line[x] = line[x].replace("{st:", "{subtitle:");
			line[x] = line[x].replace("{st :", "{subtitle:");
			line[x] = line[x].replace("{su:", "{subtitle:");
			line[x] = line[x].replace("{su :", "{subtitle:");
			line[x] = line[x].replace("{comments :", "{comments:");
			line[x] = line[x].replace("{c:", "{comments:");
			line[x] = line[x].replace("{c :", "{comments:");
			line[x] = line[x].replace("{sot", "{start_of_tab");
			line[x] = line[x].replace("{sob", "{start_of_tab");
			line[x] = line[x].replace("{eot", "{end_of_tab");
			line[x] = line[x].replace("{eob", "{end_of_tab");
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
			
			// Remove directive lines we don't need
			if (line[x].contains("{new_song")
					|| line[x].contains("{define")
					|| line[x].contains("{textfont")
					|| line[x].contains("{textsize")
					|| line[x].contains("{chordfont")
					|| line[x].contains("{chordsize")
					|| line[x].contains("{nogrid")
					|| line[x].contains("{grid")
					|| line[x].contains("{titles")
					|| line[x].contains("{new_physical_page")
					|| line[x].contains("{new_page")
					|| line[x].contains("{columns")
					|| line[x].contains("{column_break")
					|| line[x].contains("{pagetype")) {
				line[x] = "";
			}
			
			// Extract the title
			if (line[x].contains("{title:")) {
				line[x] = line[x].replace("{title:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				temptitle = line[x];
				line[x] = "";
			}

			// Extract the subtitles
			if (line[x].contains("{subtitle:")) {
				line[x] = line[x].replace("{subtitle:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				tempsubtitle = line[x];
				line[x] = ";" + line[x];
			}

			// Change lines that start with # into comment lines
			if (line[x].indexOf("#") == 0) {
				line[x] = line[x].replaceFirst("#", ";");
			}
			
			// Change comment lines
			if (line[x].contains("{comments:")) {
				line[x] = line[x].replace("{comments:","");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				line[x] = ";" + line[x];			
			}

			// Change comment lines
			if (line[x].contains("{comment:")) {
				line[x] = line[x].replace("{comment:","");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				line[x] = ";" + line[x];			
			}

			// Remove < > tags
			line[x] = line[x].replace("<", "(");
			line[x] = line[x].replace(">", ")");

		}	
		
		
		// Go through each line and try to fix chord lines
		for (int x = 0; x < numlines; x++) {
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
			}
			parsedlines = parsedlines + line[x] + "\n";
		}

		
		// Remove start and end of tabs
		while (parsedlines.contains("{start_of_tab") && parsedlines.contains("{end_of_tab")) {
			int startoftabpos = -1;
			int endoftabpos = -1;
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

		FullscreenActivity.myXML = "<song>\r\n"
				+ "<title>" + temptitle.trim() + "</title>\r\n"
				+ "<author>" + tempsubtitle.trim() + "</author>\r\n" 
				+ "<copyright>" + tempsubtitle.trim() + "</copyright>\r\n"
				+ "  <presentation></presentation>\r\n"
				+ "  <hymn_number></hymn_number>\r\n"
				+ "  <capo print=\"false\"></capo>\r\n"
				+ "  <tempo></tempo>\r\n"
				+ "  <time_sig></time_sig>\r\n"
				+ "  <duration></duration>\r\n"
				+ "  <ccli></ccli>\r\n"
				+ "  <theme></theme>\r\n"
				+ "  <alttheme></alttheme>\r\n"
				+ "  <user1></user1>\r\n"
				+ "  <user2></user2>\r\n"
				+ "  <user3></user3>\r\n"
				+ "  <key></key>\r\n"
				+ "  <aka></aka>\r\n"
				+ "  <key_line></key_line>\r\n"
				+ "  <books></books>\r\n"
				+ "  <midi></midi>\r\n"
				+ "  <midi_index></midi_index>\r\n"
				+ "  <pitch></pitch>\r\n"
				+ "  <restrictions></restrictions>\r\n"
				+ "  <notes></notes>\r\n"
				+ "<lyrics>" + parsedlines.trim() + "</lyrics>\r\n"
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

		// Change the name of the song to remove chordpro file extension 
		// (not needed)
		String newSongTitle = FullscreenActivity.songfilename;

		// Decide if a better song title is in the file
		if (temptitle.length() > 0) {
			newSongTitle = temptitle;
		}

		
	
		newSongTitle = newSongTitle.replace(".chordpro", "");
		newSongTitle = newSongTitle.replace(".chopro", "");
		newSongTitle = newSongTitle.replace(".cho", "");
		newSongTitle = newSongTitle.replace(".crd", "");

        File from;
        File to;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            from = new File(FullscreenActivity.dir + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + newSongTitle);
        } else {
            from = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + newSongTitle);
        }

        // IF THE FILENAME ALREADY EXISTS, REALLY SHOULD ASK THE USER FOR A NEW FILENAME
		// OR append _ to the end - STILL TO DO!!!!!
		while(to.exists()) {
			newSongTitle = newSongTitle+"_";
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                to = new File(FullscreenActivity.dir + "/" + newSongTitle);
            } else {
                to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + newSongTitle);
            }
		}
		
		// Do the renaming
		from.renameTo(to);
		FullscreenActivity.songfilename = newSongTitle;

		// Load the songs
		ListSongFiles.listSongs();

		// Get the song indexes
		ListSongFiles.getCurrentSongIndex();

		Preferences.savePreferences();
		
		return true;
	}
}