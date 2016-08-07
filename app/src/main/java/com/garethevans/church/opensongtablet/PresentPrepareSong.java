package com.garethevans.church.opensongtablet;

import android.util.Log;

public class PresentPrepareSong {

	// Song
	static String tempLyrics;
	static String[] tempLyricsLineByLine;
	static String[] songSections;
	static String[] songSectionsLabels;


	public static void splitSongIntoSections (String mode) {
		// Go through the song XML and add %%__SPLITHERE__%%
		// NEED TO FIX SONGS WITH MULTIPLE LINES TOGETHER STARTING WITH NUMBERS
		// NEED TO FIX EMPTY SECTIONS
		// where there are double line spaces, new page tags and section tags
		// also get rid of code we don't need like columns and new page (print only options)
		tempLyrics = FullscreenActivity.myLyrics;

		String v1 = "";
		String v2 = "";
		String v3 = "";
		String v4 = "";
		String v5 = "";
		String v6 = "";
		String v7 = "";
		String v8 = "";
		String v9 = "";
		String v10 = "";

        if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {

            //Go through lyrics line by line and extract data into verses if lines begin with number
            String[] checkForVerseLines = tempLyrics.split("\n");
            tempLyrics = "";
            for (int i = 0; i < checkForVerseLines.length; i++) {
                if (checkForVerseLines[i].indexOf("1") == 0) {
                    v1 = v1 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("2") == 0) {
                    v2 = v2 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("3") == 0) {
                    v3 = v3 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("4") == 0) {
                    v4 = v4 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("5") == 0) {
                    v5 = v5 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("6") == 0) {
                    v6 = v6 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("7") == 0) {
                    v7 = v7 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("8") == 0) {
                    v8 = v8 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("9") == 0) {
                    v9 = v9 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                } else if (checkForVerseLines[i].indexOf("10") == 0) {
                    v10 = v10 + checkForVerseLines[i].substring(1) + "\n";
                    checkForVerseLines[i] = "";
                }
                if (checkForVerseLines[i].length() > 0) {
                    tempLyrics = tempLyrics + checkForVerseLines[i] + "\n";
                }
            }
            //Add back any verses in reverse as they're going to the start
            if (v10.length() > 0) {
                tempLyrics = "[V10]\n" + v10 + tempLyrics;
            }
            if (v9.length() > 0) {
                tempLyrics = "[V9]\n" + v9 + tempLyrics;
            }
            if (v8.length() > 0) {
                tempLyrics = "[V8]\n" + v8 + tempLyrics;
            }
            if (v7.length() > 0) {
                tempLyrics = "[V7]\n" + v7 + tempLyrics;
            }
            if (v6.length() > 0) {
                tempLyrics = "[V6]\n" + v6 + tempLyrics;
            }
            if (v5.length() > 0) {
                tempLyrics = "[V5]\n" + v5 + tempLyrics;
            }
            if (v4.length() > 0) {
                tempLyrics = "[V4]\n" + v4 + tempLyrics;
            }
            if (v3.length() > 0) {
                tempLyrics = "[V3]\n" + v3 + tempLyrics;
            }
            if (v2.length() > 0) {
                tempLyrics = "[V2]\n" + v2 + tempLyrics;
            }
            if (v1.length() > 0) {
                tempLyrics = "[V1]\n" + v1 + tempLyrics;
            }
        }

		tempLyrics = tempLyrics.replace("-!!", "");
        if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
            tempLyrics = tempLyrics.replace("\n\n", "%%__SPLITHERE__%%");
            tempLyrics = tempLyrics.replace("---", "");
        } else {
            tempLyrics = tempLyrics.replace("---", "[]");
        }
		tempLyrics = tempLyrics.replace("||", "%%__SPLITHERE__%%");
		if (FullscreenActivity.presenterChords.equals("N")) {
			tempLyrics = tempLyrics.replace("|", "\n");
		} else {
            tempLyrics = tempLyrics.replace("|", " ");
        }
		// Now split the tempLyrics up into a line by line array
		tempLyricsLineByLine = tempLyrics.split("\n");
		// Go through the lines and the ones starting with tags [
		// add a splithere code
		// also remove chord lines if the presenterChords option is off
		for (int x=0;x<tempLyricsLineByLine.length;x++) {
			if (tempLyricsLineByLine[x].indexOf("[")==0 && x!=0) {
				tempLyricsLineByLine[x] = tempLyricsLineByLine[x].replace("[","%%__SPLITHERE__%%[");
			}
			if (tempLyricsLineByLine[x].indexOf(";")==0) {
				tempLyricsLineByLine[x] = "";
			}
		}
		//Get rid of a split right at the start and the end
		if (tempLyricsLineByLine[0].indexOf("%%__SPLITHERE__%%")==0) {
			tempLyricsLineByLine[0].replaceFirst("%%__SPLITHERE__%%", "");
		}
		if (tempLyricsLineByLine[tempLyricsLineByLine.length-1].equals("%%__SPLITHERE__%%")) {
			tempLyricsLineByLine[tempLyricsLineByLine.length-1] = "";
		}
		//Ok add the lines back up now
		tempLyrics = "";
		for (String aTempLyricsLineByLine : tempLyricsLineByLine) {
			//Only add it if the line isn't empty
			if ((aTempLyricsLineByLine.length() != 0 && !aTempLyricsLineByLine.equals("\n")) || FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
				tempLyrics = tempLyrics + aTempLyricsLineByLine + "\n";
			}

		}
		// Again, get rid of double line breaks and now double split points
		if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
            tempLyrics = tempLyrics.replace("\n\n", "%%__SPLITHERE__%%");
        }
		tempLyrics = tempLyrics.replace("%%__SPLITHERE__%%\n", "%%__SPLITHERE__%%");
		tempLyrics = tempLyrics.replace("\n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
		while (tempLyrics.contains("%%__SPLITHERE__%%%%__SPLITHERE__%%")) {
			tempLyrics = tempLyrics.replace("%%__SPLITHERE__%%%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
		}
		while (tempLyrics.contains("%%__SPLITHERE__%%\n%%__SPLITHERE__%%")) {
			tempLyrics = tempLyrics.replace("%%__SPLITHERE__%%\n%%__SPLITHERE__%%", "%%__SPLITHERE__%%");
		}
		// Should all be sorted now.
		// Lets split the tempLyrics up into sections
		songSections = tempLyrics.split("%%__SPLITHERE__%%");
		songSectionsLabels = tempLyrics.split("%%__SPLITHERE__%%");
		// Go through the songSectionsLabels and extract any labels for the section
		Log.d("d","FullscreenActivity.presenterChords="+FullscreenActivity.presenterChords);
		for (int x=0;x<songSections.length;x++) {
			// If not showing chords, removing the whitespace at the start of the line
			// Also get rid of _
			if (FullscreenActivity.presenterChords.equals("N")) {
				songSections[x] = songSections[x].replace("_","");
				while (songSections[x].contains("  ")) {
					songSections[x] = songSections[x].replace("  "," ");
				}
				//Break up into lines
				String[] tempLines;
				String tempSongSections = "";
				tempLines = songSections[x].split("\n");
				for (int z=0;z<tempLines.length;z++) {
					if (tempLines[z].indexOf(".")==0 && FullscreenActivity.presenterChords.equals("N")) {
						tempLines[z] = "";
					} else {
						tempLines[z] = tempLines[z].trim();
						tempSongSections = tempSongSections + tempLines[z] + "\n";
					}
				}
				songSections[x] = tempSongSections;
			}

			if (songSections[x].indexOf("[")==0) {
				int startoftag = songSections[x].indexOf("[");
				int endoftag   = songSections[x].indexOf("]");
				if (endoftag<startoftag) {
					endoftag=songSections[x].length()-1;
				}
				songSectionsLabels[x] = songSections[x].substring(startoftag+1,endoftag);
				//Remove this from the songSections
				songSections[x] = songSections[x].replace("["+songSectionsLabels[x]+"]\n", "");
				songSections[x] = songSections[x].replace("["+songSectionsLabels[x]+"]", "");
			} else {
				songSectionsLabels[x] = "";
			}
		}
		// Put the sections back into the Present Activity or the StageMode Activity
		if (mode.equals("stage")) {
			StageMode.songSections       = songSections;
			StageMode.songSectionsLabels = songSectionsLabels;
		} else {
			PresenterMode.songSections = songSections;
			PresenterMode.songSectionsLabels = songSectionsLabels;
		}
	}

}
