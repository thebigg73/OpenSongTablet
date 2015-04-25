package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.util.Log;

public class LyricsDisplay extends Activity {

	// This bit parses the lyrics
	public static void parseLyrics() {
		Log.d("LyricsDisplay","LyricsDisplay activity running");
		// Keep myLyrics and mLyrics variables the same
		FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
		// If chords aren't being shown, get rid of underscore spaces
		if (FullscreenActivity.showChords.equals("N")) {
			FullscreenActivity.myLyrics = FullscreenActivity.myLyrics.replace("_", "");
		} else {
			FullscreenActivity.myLyrics = FullscreenActivity.myLyrics.replace("_", " ");
		}
		// Now create an array from the myLyrics variable, using the new line as
		// a break point."
		//This gets rid of the tabs and stuff to make the display look better
		FullscreenActivity.myParsedLyrics = FullscreenActivity.myLyrics.split("\n");
		// Get the number of rows to write!
		FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;
	}

	public static void replaceLyricsCode() {

		// Set the size of the whatisthisline and whatisthisblock arrays
		FullscreenActivity.whatisthisblock = new String[FullscreenActivity.numrowstowrite];
		FullscreenActivity.whatisthisline = new String[FullscreenActivity.numrowstowrite];

		// Set holders for the whatisthisblock as it should only change if it is
		// declared
		String holder_whatisthisblock = "lyrics";
		for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {

			// Set the defaults
			FullscreenActivity.whatisthisline[x] = "lyrics";
			FullscreenActivity.whatisthisblock[x] = holder_whatisthisblock;
			
			if (FullscreenActivity.myParsedLyrics[x].indexOf(".") == 0) {
				FullscreenActivity.whatisthisline[x] = "chords";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replaceFirst(".", "");  // Just the first occurence allowing ...... to be used on chord lines
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf(" ") == 0 && FullscreenActivity.myParsedLyrics[x].length()>0) {
				FullscreenActivity.whatisthisline[x] = "lyrics";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.substring(1);
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[V") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[v") == 0) {
				FullscreenActivity.whatisthisline[x] = "versetitle";
				FullscreenActivity.whatisthisblock[x] = "verse";
				holder_whatisthisblock = "verse";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();
				if (FullscreenActivity.myParsedLyrics[x].equals("V")
						|| FullscreenActivity.myParsedLyrics[x].equals("V1")
						|| FullscreenActivity.myParsedLyrics[x].equals("V2")
						|| FullscreenActivity.myParsedLyrics[x].equals("V3")
						|| FullscreenActivity.myParsedLyrics[x].equals("V4")
						|| FullscreenActivity.myParsedLyrics[x].equals("V5")
						|| FullscreenActivity.myParsedLyrics[x].equals("V6")
						|| FullscreenActivity.myParsedLyrics[x].equals("V7")
						|| FullscreenActivity.myParsedLyrics[x].equals("V8")
						|| FullscreenActivity.myParsedLyrics[x].equals("V9")
						|| FullscreenActivity.myParsedLyrics[x].equals("V10")) {
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("V", "Verse ");
				}
						
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[T") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[t") == 0) {
				FullscreenActivity.whatisthisline[x] = "tagtitle";
				FullscreenActivity.whatisthisblock[x] = "tag";
				holder_whatisthisblock = "tag";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();
				if (FullscreenActivity.myParsedLyrics[x].equals("T")
						|| FullscreenActivity.myParsedLyrics[x].equals("T1")
						|| FullscreenActivity.myParsedLyrics[x].equals("T2")
						|| FullscreenActivity.myParsedLyrics[x].equals("T3")
						|| FullscreenActivity.myParsedLyrics[x].equals("T4")
						|| FullscreenActivity.myParsedLyrics[x].equals("T5")
						|| FullscreenActivity.myParsedLyrics[x].equals("T6")
						|| FullscreenActivity.myParsedLyrics[x].equals("T7")
						|| FullscreenActivity.myParsedLyrics[x].equals("T8")
						|| FullscreenActivity.myParsedLyrics[x].equals("T9")
						|| FullscreenActivity.myParsedLyrics[x].equals("T10")) {
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("T", "Tag ");
				}
				
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[C") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[c") == 0) {
				FullscreenActivity.whatisthisline[x] = "chorustitle";
				FullscreenActivity.whatisthisblock[x] = "chorus";
				holder_whatisthisblock = "chorus";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();
				if (FullscreenActivity.myParsedLyrics[x].equals("C")
						|| FullscreenActivity.myParsedLyrics[x].equals("C1")
						|| FullscreenActivity.myParsedLyrics[x].equals("C2")
						|| FullscreenActivity.myParsedLyrics[x].equals("C3")
						|| FullscreenActivity.myParsedLyrics[x].equals("C4")
						|| FullscreenActivity.myParsedLyrics[x].equals("C5")
						|| FullscreenActivity.myParsedLyrics[x].equals("C6")
						|| FullscreenActivity.myParsedLyrics[x].equals("C7")
						|| FullscreenActivity.myParsedLyrics[x].equals("C8")
						|| FullscreenActivity.myParsedLyrics[x].equals("C9")
						|| FullscreenActivity.myParsedLyrics[x].equals("C10")) {
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("C", "Chorus ");
				}
				
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[B") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[b") == 0) {
				FullscreenActivity.whatisthisline[x] = "bridgetitle";
				FullscreenActivity.whatisthisblock[x] = "bridge";
				holder_whatisthisblock = "bridge";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();
				if (FullscreenActivity.myParsedLyrics[x].equals("B")
						|| FullscreenActivity.myParsedLyrics[x].equals("B1")
						|| FullscreenActivity.myParsedLyrics[x].equals("B2")
						|| FullscreenActivity.myParsedLyrics[x].equals("B3")
						|| FullscreenActivity.myParsedLyrics[x].equals("B4")
						|| FullscreenActivity.myParsedLyrics[x].equals("B5")
						|| FullscreenActivity.myParsedLyrics[x].equals("B6")
						|| FullscreenActivity.myParsedLyrics[x].equals("B7")
						|| FullscreenActivity.myParsedLyrics[x].equals("B8")
						|| FullscreenActivity.myParsedLyrics[x].equals("B9")
						|| FullscreenActivity.myParsedLyrics[x].equals("B10")) {
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("B", "Bridge ");
				}
				
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[P") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[p") == 0) {
				FullscreenActivity.whatisthisline[x] = "prechorustitle";
				FullscreenActivity.whatisthisblock[x] = "prechorus";
				holder_whatisthisblock = "prechorus";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();
				if (FullscreenActivity.myParsedLyrics[x].equals("P")
						|| FullscreenActivity.myParsedLyrics[x].equals("P1")
						|| FullscreenActivity.myParsedLyrics[x].equals("P2")
						|| FullscreenActivity.myParsedLyrics[x].equals("P3")
						|| FullscreenActivity.myParsedLyrics[x].equals("P4")
						|| FullscreenActivity.myParsedLyrics[x].equals("P5")
						|| FullscreenActivity.myParsedLyrics[x].equals("P6")
						|| FullscreenActivity.myParsedLyrics[x].equals("P7")
						|| FullscreenActivity.myParsedLyrics[x].equals("P8")
						|| FullscreenActivity.myParsedLyrics[x].equals("P9")
						|| FullscreenActivity.myParsedLyrics[x].equals("P10")) {
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("P", "Prechorus ");
				}

			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("[") == 0) {  // This is added for custom user defined tags
				FullscreenActivity.whatisthisline[x] = "customtitle";
				FullscreenActivity.whatisthisblock[x] = "custom";
				holder_whatisthisblock = "custom";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("[", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace("]", "");
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();

			} else if (FullscreenActivity.myParsedLyrics[x].indexOf(";") == 0) {
				FullscreenActivity.whatisthisline[x] = "comment";
				FullscreenActivity.whatisthisblock[x] = "comment";
				//holder_whatisthisblock = "comment";
				FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
						.replace(";", "");
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("1") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "1. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("2") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "2. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("3") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "3. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("4") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "4. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("5") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "5. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("6") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "6. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("7") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "7. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("8") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "8. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			} else if (FullscreenActivity.myParsedLyrics[x].indexOf("9") == 0) {
				FullscreenActivity.myParsedLyrics[x] = "9. " + FullscreenActivity.myParsedLyrics[x].substring(1);
				if (x>0) {
					if (FullscreenActivity.whatisthisline[x-1].equals("chords")) {
						FullscreenActivity.myParsedLyrics[x-1] = "   " + FullscreenActivity.myParsedLyrics[x-1];
					}
				}
			}


			FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
					.replace("||", " ");

			FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
					.replace("|", " ");

			FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
					.replace("---", "");

			FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
					.replace("-!!", "");

		}
		
		// THIS BIT MAKES EACH LINE THE SAME LENGTH
		// FIRST COUNT THE CHARS IN EACH LINE AND IF IT IS BIGGER THAN PREVIOUS, CHANGE IT
		// SET TO 0 to start
		FullscreenActivity.maxcharsinline = 0;
		for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {
			// Get the length of the line
			int charsinthisline = FullscreenActivity.myParsedLyrics[x].length();
			if (charsinthisline > FullscreenActivity.maxcharsinline) {
				// Set the new biggest line size
				FullscreenActivity.maxcharsinline = charsinthisline;
			}
		}
		
		// NOW WE HAVE THE LONGEST LINE, GO THROUGH EACH ONE AND MAKE IT THIS LONG
		for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {
			// Get the length of the line
			int charsinthisline = FullscreenActivity.myParsedLyrics[x].length();
			if (charsinthisline < FullscreenActivity.maxcharsinline) {
				// Ok, it isn't as long as the others.  Add spaces
				int numspacesneeded = (FullscreenActivity.maxcharsinline - charsinthisline);
				for (int i=0;i<numspacesneeded;i++) {
					FullscreenActivity.myParsedLyrics[x] += " ";
				}				
			}
		}

	}
	
	public static void lookForSplitPoints() {
		// Script to determine 2 columns split details
		int halfwaypoint = Math.round(FullscreenActivity.numrowstowrite / 2);
		// Look for nearest split point before halfway
		int splitpoint_1sthalf = 0;
		boolean gotityet = false;
		for (int scan = halfwaypoint; scan > 0; scan--) {
			if (!gotityet) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet = true;
					splitpoint_1sthalf = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet = true;
					splitpoint_1sthalf = scan + 1;
				}
			}
		}
 
		// Look for nearest split point past halfway
		int splitpoint_2ndhalf = FullscreenActivity.numrowstowrite;
		boolean gotityet2 = false;
		for (int scan = halfwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
			if (!gotityet2) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet2 = true;
					splitpoint_2ndhalf = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet2 = true;
					splitpoint_2ndhalf = scan + 1;
				}
			}
		}

		// Script to determine 3 columns split details
		int thirdwaypoint = Math.round(FullscreenActivity.numrowstowrite / 3);
		int twothirdwaypoint = thirdwaypoint * 2;

		// Look for nearest split point before thirdway
		int splitpoint_beforethirdway = 0;
		boolean gotityet_beforethirdway = false;
		for (int scan = thirdwaypoint; scan > 0; scan--) {
			if (!gotityet_beforethirdway) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet_beforethirdway = true;
					splitpoint_beforethirdway = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet_beforethirdway = true;
					splitpoint_beforethirdway = scan + 1;
				}
			}
		}

		// Look for nearest split point past thirdway
		int splitpoint_pastthirdway = thirdwaypoint;
		boolean gotityet_pastthirdway = false;
		for (int scan = thirdwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
			if (!gotityet_pastthirdway) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet_pastthirdway = true;
					splitpoint_pastthirdway = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet_pastthirdway = true;
					splitpoint_pastthirdway = scan + 1;
				}
			}
		}

		// Look for nearest split point before twothirdway
		int splitpoint_beforetwothirdway = thirdwaypoint;
		boolean gotityet_beforetwothirdway = false;
		for (int scan = twothirdwaypoint; scan > 0; scan--) {
			if (!gotityet_beforetwothirdway) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet_beforetwothirdway = true;
					splitpoint_beforetwothirdway = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet_beforetwothirdway = true;
					splitpoint_beforetwothirdway = scan + 1;
				}
			}
		}

		// Look for nearest split point past twothirdway
		int splitpoint_pasttwothirdway = twothirdwaypoint;
		boolean gotityet_pasttwothirdway = false;
		for (int scan = twothirdwaypoint; scan < FullscreenActivity.numrowstowrite; scan++) {
			if (!gotityet_pasttwothirdway) {
				if (FullscreenActivity.myParsedLyrics[scan].indexOf("[") == 0) {
					gotityet_pasttwothirdway = true;
					splitpoint_pasttwothirdway = scan;
				} else if (FullscreenActivity.myParsedLyrics[scan].length() == 0) {
					gotityet_pasttwothirdway = true;
					splitpoint_pasttwothirdway = scan + 1;
				}
			}
		}

		if (!gotityet_beforethirdway) {
			splitpoint_beforethirdway = 0;
		}
		if (!gotityet_pastthirdway) {
			splitpoint_pastthirdway = 0;
		}
		if (!gotityet_beforetwothirdway) {
			splitpoint_beforetwothirdway = splitpoint_beforethirdway;
		}
		if (!gotityet_pasttwothirdway) {
			splitpoint_pasttwothirdway = FullscreenActivity.numrowstowrite;
		}

		// Which is the best split point to use (closest to halfway) for 2
		// columns
		int split1stdiff = Math.abs(halfwaypoint - splitpoint_1sthalf);
		int split2nddiff = Math.abs(halfwaypoint - splitpoint_2ndhalf);

		if (split1stdiff <= split2nddiff) {
			FullscreenActivity.splitpoint = splitpoint_1sthalf;
		} else {
			FullscreenActivity.splitpoint = splitpoint_2ndhalf;
		}
		
		FullscreenActivity.botherwithcolumns = true;

		// Which is the best split point to use (closest to thirdway) for 3 columns
		int splitprethirddiff = Math.abs(thirdwaypoint - splitpoint_beforethirdway);
		int splitpastthirddiff = Math.abs(thirdwaypoint - splitpoint_pastthirdway);
		int splitpretwothirddiff = Math.abs(twothirdwaypoint - splitpoint_beforetwothirdway);
		int splitpasttwothirddiff = Math.abs(twothirdwaypoint - splitpoint_pasttwothirdway);

		if (splitprethirddiff <= splitpastthirddiff) {
			FullscreenActivity.thirdsplitpoint = splitpoint_beforethirdway;
		} else {
			FullscreenActivity.thirdsplitpoint = splitpoint_pastthirdway;
		}

		if (splitpretwothirddiff <= splitpasttwothirddiff) {
			FullscreenActivity.twothirdsplitpoint = splitpoint_beforetwothirdway;
		} else {
			FullscreenActivity.twothirdsplitpoint = splitpoint_pasttwothirdway;
		}
	}
}