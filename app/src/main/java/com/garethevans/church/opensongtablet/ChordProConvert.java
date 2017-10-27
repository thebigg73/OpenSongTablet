package com.garethevans.church.opensongtablet;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class ChordProConvert {

	static boolean doExtract() throws IOException {

		// This is called when a ChordPro format song has been loaded.
		// This tries to extract the relevant stuff and reformat the
		// <lyrics>...</lyrics>
		String temp = FullscreenActivity.myXML;
		String parsedlines;
		// Initialise all the xml tags a song should have
		FullscreenActivity.mTitle = FullscreenActivity.songfilename;
		LoadXML.initialiseSongTags();

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
		}

		String temptitle = "";
		String tempsubtitle;
        String tempccli = "";
        String tempauthor = "";
        String tempcopyright = "";
        String tempkey = "";
        String temptimesig = "";
        String temptempo = "";

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
				Log.d("d","temptitle="+temptitle);
				line[x] = "";
			}

			// Extract the author
            if (line[x].contains("{artist:")) {
                line[x] = line[x].replace("{artist:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                tempauthor = line[x];
                Log.d("d","tempauthor="+tempauthor);
                line[x] = "";
            }

            // Extract the copyright
            if (line[x].contains("{copyright:")) {
                line[x] = line[x].replace("{copyright:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                tempcopyright = line[x];
                Log.d("d","tempcopyright="+tempcopyright);
                line[x] = "";
            }

			// Extract the subtitles
			if (line[x].contains("{subtitle:")) {
				line[x] = line[x].replace("{subtitle:", "");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				tempsubtitle = line[x];
                Log.d("d","tempsubtitle="+tempsubtitle);
                if (tempauthor.equals("")) {
                    tempauthor = tempsubtitle;
                }
                if (tempcopyright.equals("")) {
                    tempcopyright = tempsubtitle;
                }
				line[x] = ";" + line[x];
			}

			// Extract the ccli (not really a chordpro tag, but works for songselect
			if (line[x].contains("{ccli:")) {
				line[x] = line[x].replace("{ccli:","");
				line[x] = line[x].replace("}","");
				line[x] = line[x].trim();
				tempccli = line[x];
				Log.d("d","tempccli="+tempccli);
				line[x] = "";
			}

            // Extract the key
            if (line[x].contains("{key:")) {
                line[x] = line[x].replace("{key:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                tempkey = line[x];
                Log.d("d","tempkey="+tempkey);
                line[x] = "";
            }

            // Extract the key
            if (line[x].contains("{key:")) {
                line[x] = line[x].replace("{key:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                tempkey = line[x];
                Log.d("d","tempkey="+tempkey);
                line[x] = "";
            }

            // Extract the tempo
            if (line[x].contains("{tempo:")) {
                line[x] = line[x].replace("{tempo:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                temptempo = line[x];
                Log.d("d","temptempo="+temptempo);
                line[x] = "";
            }

            // Extract the timesig
            if (line[x].contains("{time:")) {
                line[x] = line[x].replace("{time:", "");
                line[x] = line[x].replace("}", "");
                line[x] = line[x].trim();
                temptimesig = line[x];
                Log.d("d","temptimesig="+temptimesig);
                line[x] = "";
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
                Log.d("d","comments="+line[x]);
			}

			// Change comment lines
			if (line[x].contains("{comment:")) {
				line[x] = line[x].replace("{comment:","");
				line[x] = line[x].replace("}", "");
				line[x] = line[x].trim();
				line[x] = ";" + line[x];
                Log.d("d","comments="+line[x]);
			}

			// Remove < > tags
			line[x] = line[x].replace("<", "(");
			line[x] = line[x].replace(">", ")");

		}	
		
		Log.d("d","FullscreenActivity.myXML="+FullscreenActivity.myXML);
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
		}

		// Join the individual lines back up
		parsedlines = "";
		for (int x = 0; x < numlines; x++) {
			// Try to guess tags used
			if (line[x].indexOf(";")!=0) {
				line[x] = line[x].replace("Intro:", "[Intro]");
				line[x] = line[x].replace("Outro:", "[Outro]");
				line[x] = line[x].replace("Verse:", "[V]");
				line[x] = line[x].replace("VERSE:", "[V]");
				line[x] = line[x].replace("Verse 1:", "[V1]");
				line[x] = line[x].replace("Verse 2:", "[V2]");
				line[x] = line[x].replace("Verse 3:", "[V3]");
				line[x] = line[x].replace("Verse 4:", "[V4]");
				line[x] = line[x].replace("VERSE 1:", "[V1]");
				line[x] = line[x].replace("VERSE 2:", "[V2]");
				line[x] = line[x].replace("VERSE 3:", "[V3]");
				line[x] = line[x].replace("VERSE 4:", "[V4]");
				line[x] = line[x].replace("(VERSE)", "[V]");
				line[x] = line[x].replace("(Verse 1)", "[V1]");
				line[x] = line[x].replace("(Verse 2)", "[V2]");
				line[x] = line[x].replace("(Verse 3)", "[V3]");				
				line[x] = line[x].replace("(Chorus)", "[C]");
				line[x] = line[x].replace("Chorus:", "[C]");
				line[x] = line[x].replace("CHORUS:", "[C]");
				line[x] = line[x].replace("Chorus 1:", "[C1]");
				line[x] = line[x].replace("Chorus 2:", "[C2]");
				line[x] = line[x].replace("Chorus 3:", "[C3]");
				line[x] = line[x].replace("CHORUS 1:", "[C1]");
				line[x] = line[x].replace("CHORUS 2:", "[C2]");
				line[x] = line[x].replace("CHORUS 3:", "[C3]");
				line[x] = line[x].replace("Prechorus:", "[P]");
				line[x] = line[x].replace("Pre-chorus:", "[P]");
				line[x] = line[x].replace("Prechorus:", "[P]");
				line[x] = line[x].replace("Pre-Chorus 1:", "[P1]");
				line[x] = line[x].replace("PRECHORUS:", "[P]");
				line[x] = line[x].replace("Prechorus 2:", "[P2]");
				line[x] = line[x].replace("Prechorus 3:", "[P3]");
				line[x] = line[x].replace("Bridge:", "[B]");
				line[x] = line[x].replace("BRIDGE:", "[B]");
				line[x] = line[x].replace("Bridge 1:", "[B1]");
				line[x] = line[x].replace("Bridge 2:", "[B2]");
				line[x] = line[x].replace("Bridge 3:", "[B3]");
				line[x] = line[x].replace("BRIDGE 1:", "[B1]");
				line[x] = line[x].replace("BRIDGE 2:", "[B2]");
				line[x] = line[x].replace("BRIDGE 3:", "[B3]");
				line[x] = line[x].replace("Tag:", "[T]");
				line[x] = line[x].replace("[[", "[");
                line[x] = line[x].replace("]]", "]");
				if (line[x].endsWith(":") && line[x].length()<12) {
					// Likely to be another custom tag
					line[x] = "["+line[x].replace(":","")+"]";
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
				+ "  <title>" + temptitle.trim() + "</title>\n"
				+ "  <author>" + tempauthor.trim() + "</author>\n"
				+ "  <copyright>" + tempcopyright.trim() + "</copyright>\n"
				+ "  <presentation></presentation>\n"
				+ "  <hymn_number></hymn_number>\n"
				+ "  <capo print=\"false\"></capo>\n"
				+ "  <tempo>" + temptempo.trim() + "</tempo>\n"
				+ "  <time_sig>" + temptimesig.trim() + "</time_sig>\n"
				+ "  <duration></duration>\n"
				+ "  <ccli>" + tempccli.trim() + "</ccli>\n"
				+ "  <theme></theme>\n"
				+ "  <alttheme></alttheme>\n"
				+ "  <user1></user1>\n"
				+ "  <user2></user2>\n"
				+ "  <user3></user3>\n"
				+ "  <key>" + tempkey + "</key>\n"
				+ "  <aka></aka>\n"
				+ "  <key_line></key_line>\n"
				+ "  <books></books>\n"
				+ "  <midi></midi>\n"
				+ "  <midi_index></midi_index>\n"
				+ "  <pitch></pitch>\n"
				+ "  <restrictions></restrictions>\n"
				+ "  <notes></notes>\n"
				+ "  <lyrics>" + parsedlines.trim() + "</lyrics>\n"
                + "  <linked_songs></linked_songs>\n"
                + "  <pad_file></pad_file>\n"
                + "  <custom_chords></custom_chords>\n"
                + "  <link_youtube></link_youtube>\n"
                + "  <link_web></link_web>\n"
                + "  <link_audio></link_audio>\n"
                + "  <link_other></link_other>\n"
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


		newSongTitle = newSongTitle.replace(".pro", "");
        newSongTitle = newSongTitle.replace(".PRO", "");
		newSongTitle = newSongTitle.replace(".chopro", "");
		newSongTitle = newSongTitle.replace(".chordpro", "");
        newSongTitle = newSongTitle.replace(".CHOPRO", "");
        newSongTitle = newSongTitle.replace(".CHORDPRO", "");
		newSongTitle = newSongTitle.replace(".cho", "");
		newSongTitle = newSongTitle.replace(".CHO", "");
		newSongTitle = newSongTitle.replace(".txt", "");
        newSongTitle = newSongTitle.replace(".TXT", "");

        File from;
        File to;

        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            from = new File(FullscreenActivity.dir + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + newSongTitle);
        } else {
            from = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + FullscreenActivity.songfilename);
            to = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/"
                    + newSongTitle);
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
		if(!from.renameTo(to)) {
            Log.d("d","Couldn't rename");
        }
		FullscreenActivity.songfilename = newSongTitle;

		// Load the songs
		ListSongFiles.getAllSongFiles();

		// Get the song indexes
		ListSongFiles.getCurrentSongIndex();
		Preferences.savePreferences();

        // Prepare the app to fix the song menu with the new file
        FullscreenActivity.converting = true;

		return true;
	}
}