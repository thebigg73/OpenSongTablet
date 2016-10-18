package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

public class LyricsDisplay extends Activity {

	static String tempPresentationOrder;

	// This bit parses the lyrics
	public static void parseLyrics() {
		Log.d("LyricsDisplay","LyricsDisplay activity running");
		// Keep myLyrics and mLyrics variables the same
		FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
		FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.mLyrics);

		// Does the user want to use the custom presentation order?
		// If so, parse the song into an appropriate format first
		// Only do this if this isn't a scripture - as it starts with numbers!
		if (!FullscreenActivity.whichSongFolder.contains(FullscreenActivity.scripture)) {
			parseToPresentationOrder();
		}

		// If the user doesn't want to use a custom presentation order, or it is blank,
		// replace it back with the original
		if (!FullscreenActivity.usePresentationOrder || FullscreenActivity.mPresentation.isEmpty() || FullscreenActivity.mPresentation.equals("")) {
			FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.mLyrics);
		}

		FullscreenActivity.myLyrics = FullscreenActivity.myLyrics.replace("\n \n","\n\n");

		// Now create an array from the myLyrics variable, using the new line as
		// a break point."
		//This gets rid of the tabs and stuff to make the display look better
		FullscreenActivity.myParsedLyrics = FullscreenActivity.myLyrics.split("\n");
		// Get the number of rows to write!
		FullscreenActivity.numrowstowrite = FullscreenActivity.myParsedLyrics.length;

		// Go through the lines and remove underscores if the line isn't an image location
		for (int l=0;l<FullscreenActivity.numrowstowrite;l++) {
			if (FullscreenActivity.myParsedLyrics[l].contains("_")) {
				if (l>0 && !FullscreenActivity.myParsedLyrics[l].contains("["+FullscreenActivity.image+"_") && !FullscreenActivity.myParsedLyrics[l-1].contains("["+FullscreenActivity.image+"_")) {
					if (!FullscreenActivity.showChords) {
						FullscreenActivity.myParsedLyrics[l] = FullscreenActivity.myParsedLyrics[l].replace("_","");
					} else {
						FullscreenActivity.myParsedLyrics[l] = FullscreenActivity.myParsedLyrics[l].replace("_"," ");
					}
				} else if (l==0 && !FullscreenActivity.myParsedLyrics[l].contains("["+FullscreenActivity.image+"_")) {
					if (!FullscreenActivity.showChords) {
						FullscreenActivity.myParsedLyrics[l] = FullscreenActivity.myParsedLyrics[l].replace("_","");
					} else {
						FullscreenActivity.myParsedLyrics[l] = FullscreenActivity.myParsedLyrics[l].replace("_"," ");
					}
				}
			}
		}
	}

	public static void replaceLyricsCode() {

		// Set the size of the whatisthisline and whatisthisblock arrays
		FullscreenActivity.whatisthisblock = new String[FullscreenActivity.numrowstowrite];
		FullscreenActivity.whatisthisline = new String[FullscreenActivity.numrowstowrite];

        // THIS BIT MAKES EACH LINE THE SAME LENGTH
        // FIRST COUNT THE CHARS IN EACH LINE AND IF IT IS BIGGER THAN PREVIOUS, CHANGE IT
        // SET TO 0 to start
        FullscreenActivity.maxcharsinline = 0;

		// Set holders for the whatisthisblock as it should only change if it is
		// declared
		String holder_whatisthisblock = "lyrics";
		for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {


            // If this isn't a chord line, replace lyric codings.  This means chord lines can have bar lines in them
            if (FullscreenActivity.myParsedLyrics[x].indexOf(".")!=0) {
                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("||", " ");

                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("|", " ");

                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("---", "");

                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("-!!", "");
            }

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
			} else if (((FullscreenActivity.myParsedLyrics[x].indexOf("[V") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[v") == 0) && FullscreenActivity.myParsedLyrics[x].indexOf("]") > 1 && FullscreenActivity.myParsedLyrics[x].indexOf("]") < 5) ||
					(FullscreenActivity.myParsedLyrics[x].toLowerCase(FullscreenActivity.locale).contains("["+FullscreenActivity.tag_verse.toLowerCase(FullscreenActivity.locale)))) {
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
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("V", FullscreenActivity.tag_verse+" ");
				}
						
			} else if (((FullscreenActivity.myParsedLyrics[x].indexOf("[T") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[t") == 0) && FullscreenActivity.myParsedLyrics[x].indexOf("]") > 1 && FullscreenActivity.myParsedLyrics[x].indexOf("]") < 5) ||
					(FullscreenActivity.myParsedLyrics[x].toLowerCase(FullscreenActivity.locale).contains("["+FullscreenActivity.tag_tag.toLowerCase(FullscreenActivity.locale)))) {
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
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("T", FullscreenActivity.tag_tag+" ");
				}


            } else if (FullscreenActivity.myParsedLyrics[x].indexOf("["+FullscreenActivity.image+"_") == 0) {
                FullscreenActivity.whatisthisline[x] = "imagetitle";
                FullscreenActivity.whatisthisblock[x] = "image";
                holder_whatisthisblock = "image";
                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("[", "");
                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x]
                        .replace("]", "");
                FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].trim();



            } else if (((FullscreenActivity.myParsedLyrics[x].indexOf("[C") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[c") == 0) && FullscreenActivity.myParsedLyrics[x].indexOf("]") > 1 && FullscreenActivity.myParsedLyrics[x].indexOf("]") < 5) ||
					(FullscreenActivity.myParsedLyrics[x].toLowerCase(FullscreenActivity.locale).contains("["+FullscreenActivity.tag_chorus.toLowerCase(FullscreenActivity.locale)))) {
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
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("C", FullscreenActivity.tag_chorus+" ");
				}
				
			} else if (((FullscreenActivity.myParsedLyrics[x].indexOf("[B") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[b") == 0) && FullscreenActivity.myParsedLyrics[x].indexOf("]") > 1 && FullscreenActivity.myParsedLyrics[x].indexOf("]") < 5) ||
					(FullscreenActivity.myParsedLyrics[x].toLowerCase(FullscreenActivity.locale).contains("["+FullscreenActivity.tag_bridge.toLowerCase(FullscreenActivity.locale)))) {
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
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("B", FullscreenActivity.tag_bridge+" ");
				}
				
			} else if (((FullscreenActivity.myParsedLyrics[x].indexOf("[P") == 0 || FullscreenActivity.myParsedLyrics[x].indexOf("[p") == 0) && FullscreenActivity.myParsedLyrics[x].indexOf("]") > 1 && FullscreenActivity.myParsedLyrics[x].indexOf("]") < 5) ||
					(FullscreenActivity.myParsedLyrics[x].toLowerCase(FullscreenActivity.locale).contains("["+FullscreenActivity.tag_prechorus.toLowerCase(FullscreenActivity.locale)))) {
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
					FullscreenActivity.myParsedLyrics[x] = FullscreenActivity.myParsedLyrics[x].replace("P", FullscreenActivity.tag_prechorus+" ");
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

			// Get the length of the line
			int charsinthisline = FullscreenActivity.myParsedLyrics[x].length();
			if (charsinthisline > FullscreenActivity.maxcharsinline) {
				// Set the new biggest line size
				FullscreenActivity.maxcharsinline = charsinthisline;
			}

		}

		// NOW WE HAVE THE LONGEST LINE, GO THROUGH EACH ONE AND MAKE IT THIS LONG
		if (!FullscreenActivity.whichSongFolder.contains("../Images/")) {
            for (int x = 0; x < FullscreenActivity.numrowstowrite; x++) {
                // Get the length of the line
                int charsinthisline = FullscreenActivity.myParsedLyrics[x].length();
                if (charsinthisline < FullscreenActivity.maxcharsinline) {
                    // Ok, it isn't as long as the others.  Add spaces
                    int numspacesneeded = (FullscreenActivity.maxcharsinline - charsinthisline);
                    for (int i = 0; i < numspacesneeded; i++) {
                        FullscreenActivity.myParsedLyrics[x] += " ";
                    }
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

	public static void parseToPresentationOrder() {
        Log.d("d","parseToPresentationOrder() is called");
		// The presentation order is separated by spaces.  One issue is that custom tags might have spaces in them
		// Go through the song and look for all tag.  Make a temp lyrics string
		//FullscreenActivity.mLyrics = FullscreenActivity.myLyrics;
		String[] lookfortagslyrics = FullscreenActivity.myLyrics.split("\n");
		ArrayList<String> temp_title = new ArrayList<> ();
		ArrayList<String> temp_content = new ArrayList<> ();
		String gathercontent = "";
		int tagnum = -1;
		int numtemplines = lookfortagslyrics.length;
		// Have to deal with  multiple line verses, choruses, bridges, tags 1,2,3,4,5,6,7,8,9 and matching chords, etc. seperately
		boolean multilpleverselines = false;
        boolean multiplechoruslines = false;
		boolean currentlyworkingthroughmultilineverse = false;
		String verse1contents = "";
		String verse2contents = "";
		String verse3contents = "";
		String verse4contents = "";
		String verse5contents = "";
		String verse6contents = "";
		String verse7contents = "";
		String verse8contents = "";
		String verse9contents = "";
		for (int z=0;z<numtemplines;z++) {
			if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("1")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("1"," ");
				// If line above has chords, add them
				if (z>0) {
					if (lookfortagslyrics[(z-1)].indexOf(".")==0) {
						verse1contents = verse1contents + lookfortagslyrics[(z-1)] + "\n";
					}
				}
				verse1contents = verse1contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";
			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("2")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("2"," ");
				// If line above has chords, add them
				if (z>1) {
					if (lookfortagslyrics[(z-2)].indexOf(".")==0) {
						verse2contents = verse2contents + lookfortagslyrics[(z-2)] + "\n";
					}
				}
				verse2contents = verse2contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";

			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("3")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("3"," ");
				// If line above has chords, add them
				if (z>2) {
					if (lookfortagslyrics[(z-3)].indexOf(".")==0) {
						verse3contents = verse3contents + lookfortagslyrics[(z-3)] + "\n";
					}
				}
				verse3contents = verse3contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";


			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("4")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("4"," ");
				// If line above has chords, add them
				if (z>3) {
					if (lookfortagslyrics[(z-4)].indexOf(".")==0) {
						verse4contents = verse4contents + lookfortagslyrics[(z-4)] + "\n";
					}
				}
				verse4contents = verse4contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";


			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("5")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("5"," ");
				// If line above has chords, add them
				if (z>4) {
					if (lookfortagslyrics[(z-5)].indexOf(".")==0) {
						verse5contents = verse5contents + lookfortagslyrics[(z-5)] + "\n";
					}
				}
				verse5contents = verse5contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";

			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("6")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("6"," ");
				// If line above has chords, add them
				if (z>5) {
					if (lookfortagslyrics[(z-6)].indexOf(".")==0) {
						verse6contents = verse6contents + lookfortagslyrics[(z-6)] + "\n";
					}
				}
				verse6contents = verse6contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";

			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("7")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("7"," ");
				// If line above has chords, add them
				if (z>6) {
					if (lookfortagslyrics[(z-7)].indexOf(".")==0) {
						verse7contents = verse7contents + lookfortagslyrics[(z-7)] + "\n";
					}
				}
				verse7contents = verse7contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";

			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("8")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("8"," ");
				// If line above has chords, add them
				if (z>7) {
					if (lookfortagslyrics[(z-8)].indexOf(".")==0) {
						verse8contents = verse8contents + lookfortagslyrics[(z-8)] + "\n";
					}
				}
				verse8contents = verse8contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";

			} else if (lookfortagslyrics[z].length()>1 && lookfortagslyrics[z].indexOf("9")==0) {
				multilpleverselines = true;
				currentlyworkingthroughmultilineverse = true;
				lookfortagslyrics[z] = lookfortagslyrics[z].replaceFirst("9"," ");
				// If line above has chords, add them
				if (z>8) {
					if (lookfortagslyrics[(z-9)].indexOf(".")==0) {
						verse9contents = verse9contents + lookfortagslyrics[(z-9)] + "\n";
					}
				}
				verse9contents = verse9contents + lookfortagslyrics[z] + "\n";
				// Now empty this line
				lookfortagslyrics[z] = "__REMOVED__";


			} else if (lookfortagslyrics[z].length()>1 &&
					((lookfortagslyrics[z].contains("[V]") || lookfortagslyrics[z].contains("[v]") || lookfortagslyrics[z].contains("[Verse]")))) {
				// Remove the starting [V] tag and replace it with __MULTIPLEVERSES__
				// Only if the next (or next again line starts with 1
				if ((z+1)<numtemplines) {
					try {
						if (lookfortagslyrics[(z + 1)].indexOf("1") == 0) {
							lookfortagslyrics[z] = "__MULTIPLEVERSES__";
						}
					} catch (Exception e) {
						// There was a problem!
					}
				}

				if ((z+2)<numtemplines) {
					// Allows for z+1 being a chord line
                    try {
                        if (lookfortagslyrics[(z + 2)].indexOf("1") == 0) {
                            lookfortagslyrics[z] = "__MULTIPLEVERSES__";
                        }
                    } catch (Exception e) {
                        // There was a problem!
                    }
				}

            } else if (lookfortagslyrics[z].length()>1 &&
                    ((lookfortagslyrics[z].contains("[C]") || lookfortagslyrics[z].contains("[c]") || lookfortagslyrics[z].contains("[Chorus]")))) {
                // Remove the starting [V] tag and replace it with __MULTIPLECHORUSES__
                // Only if the next (or next again line starts with 1
                if ((z+1)<numtemplines) {
                    try {
                        if (lookfortagslyrics[(z + 1)].indexOf("1") == 0) {
                            lookfortagslyrics[z] = "__MULTIPLECHORUSES__";
                            multiplechoruslines = true;
                        }
                    } catch (Exception e) {
                        // There was a problem!
                    }
                }

                if ((z+2)<numtemplines) {
                    // Allows for z+1 being a chord line
                    try {
                        if (lookfortagslyrics[(z + 2)].indexOf("1") == 0) {
                            lookfortagslyrics[z] = "__MULTIPLECHORUSES__";
                            multiplechoruslines = true;
                        }
                    } catch (Exception e) {
                        // There was a problem!
                    }
                }
            }

		}

		String newText = "";
		String improvedText = "";
		// OK, add the lines back together, but removing the lines that equal __REMOVED__
		// Then add the verses to where the line is __MULTIPLEVERSES__ or __MULTIPLECHORUSES__
		for (int s = 0; s < numtemplines;s++) {
			if (lookfortagslyrics[s].equals("__MULTIPLEVERSES__") || lookfortagslyrics[s].equals("__MULTIPLECHORUSES__")) {
				if (!verse1contents.isEmpty() && !verse1contents.equals("")) {
					newText = newText + verse1contents + "\n";
				}
				if (!verse2contents.isEmpty() && !verse2contents.equals("")) {
					newText = newText + verse2contents + "\n";
				}
				if (!verse3contents.isEmpty() && !verse3contents.equals("")) {
					newText = newText + verse3contents + "\n";
				}
				if (!verse4contents.isEmpty() && !verse4contents.equals("")) {
					newText = newText + verse4contents + "\n";
				}
				if (!verse5contents.isEmpty() && !verse5contents.equals("")) {
					newText = newText + verse5contents + "\n";
				}
				if (!verse6contents.isEmpty() && !verse6contents.equals("")) {
					newText = newText + verse6contents + "\n";
				}
				if (!verse7contents.isEmpty() && !verse7contents.equals("")) {
					newText = newText + verse7contents + "\n";
				}
				if (!verse8contents.isEmpty() && !verse8contents.equals("")) {
					newText = newText + verse8contents + "\n";
				}
				if (!verse9contents.isEmpty() && !verse9contents.equals("")) {
					newText = newText + verse9contents + "\n";
				}

				lookfortagslyrics[s] = newText;

            }

            if (!lookfortagslyrics[s].equals("__REMOVED__")) {
			improvedText = improvedText + lookfortagslyrics[s] + "\n";
			}
		}

		// Ok, now reprocess the improved version with multiple verse lines sorted
		lookfortagslyrics = improvedText.split("\n");
		numtemplines = lookfortagslyrics.length;
		// Go through lines and look for tags
		for (int z=0;z<numtemplines;z++) {
			if (lookfortagslyrics[z].indexOf("[")==0) {
				// Add the content to the arraylist before getting ready for the next one
				if (tagnum>-1 && !currentlyworkingthroughmultilineverse) {
					temp_content.add(tagnum, gathercontent);
				}
				gathercontent = "";
				tagnum ++;
				lookfortagslyrics[z] = lookfortagslyrics[z].replace("[","");
				lookfortagslyrics[z] = lookfortagslyrics[z].replace("]","");
				temp_title.add(tagnum,lookfortagslyrics[z]);

			} else {
					gathercontent = gathercontent + lookfortagslyrics[z] + "\n";
					currentlyworkingthroughmultilineverse = false;
			}
		}


		// Add the last content to the arraylist
		if (tagnum>-1 && !currentlyworkingthroughmultilineverse) {
			temp_content.add(tagnum, gathercontent);
		}


		// Add on the multilineverses
		if (multilpleverselines) {
			if (!verse1contents.isEmpty() && !verse1contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C1");

                } else {
                    temp_title.add(tagnum, "V1");
                }
				temp_content.add(tagnum, verse1contents);
			}
			if (!verse2contents.isEmpty() && !verse2contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C2");

                } else {
                    temp_title.add(tagnum, "V2");
                }
				temp_content.add(tagnum, verse2contents);
			}
			if (!verse3contents.isEmpty() && !verse3contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C3");

                } else {
                    temp_title.add(tagnum, "V3");
                }
				temp_content.add(tagnum, verse3contents);
			}
			if (!verse4contents.isEmpty() && !verse4contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C4");

                } else {
                    temp_title.add(tagnum, "V4");
                }
				temp_content.add(tagnum, verse4contents);
			}
			if (!verse5contents.isEmpty() && !verse5contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C5");

                } else {
                    temp_title.add(tagnum, "V5");
                }
				temp_content.add(tagnum, verse5contents);
			}
			if (!verse6contents.isEmpty() && !verse6contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C6");

                } else {
                    temp_title.add(tagnum, "V6");
                }
				temp_content.add(tagnum, verse6contents);
			}
			if (!verse7contents.isEmpty() && !verse7contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C7");

                } else {
                    temp_title.add(tagnum, "V7");
                }
				temp_content.add(tagnum, verse7contents);
			}
			if (!verse8contents.isEmpty() && !verse8contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C8");

                } else {
                    temp_title.add(tagnum, "V8");
                }
				temp_content.add(tagnum, verse8contents);
			}
			if (!verse9contents.isEmpty() && !verse9contents.equals("")) {
				tagnum ++;
                if (multiplechoruslines) {
                    temp_title.add(tagnum, "C9");

                } else {
                    temp_title.add(tagnum, "V9");
                }
				temp_content.add(tagnum, verse9contents);
			}
		}


		// Now look for the tags in the presentation order.  To fix the space stuff, split the orders with ___ rather than spaces
		tempPresentationOrder = FullscreenActivity.mPresentation+ " ";
		for (int w=0;w<temp_title.size();w++) {
			if (tempPresentationOrder.contains((temp_title.get(w))+ " ")) {
				// Replace this if the next character isn't a space
				tempPresentationOrder = tempPresentationOrder.replace(temp_title.get(w)+" ", (temp_title.get(w)+"___"));
			}
		}

		// Now split the tempPresentationOrder by the ___ delimiter
		String[] parsedTempPresentationOrder = tempPresentationOrder.split("___");

		// Now go through the parsedTempPresentationOrder and add the gathered text to the parsed lyrics
		String newimprovedlyrics = "";
		for (int y=0;y<parsedTempPresentationOrder.length;y++) {
			// Look for a matching temp_title
			// Iterate through the temp_title arraylist
			// Trim out extra whitespace
			parsedTempPresentationOrder[y] = parsedTempPresentationOrder[y].trim();
			for (int u=0;u<temp_title.size();u++) {
				if (temp_title.get(u).equals(parsedTempPresentationOrder[y])) {
					// Add the title
					newimprovedlyrics = newimprovedlyrics + "[" + temp_title.get(u) + "]\n";
					// Add the contents
					newimprovedlyrics = newimprovedlyrics + temp_content.get(u) + "\n";
				}
			}
		}

		String missingSectionInPresentationOrder = "";
		String missingPresentationOrderItemInSong = "";

		// Go through newImprovedLyrics and check that each section is there
		for (int r=0;r<temp_title.size();r++) {
			if (!tempPresentationOrder.contains(temp_title.get(r)+"___")) {
				missingSectionInPresentationOrder = missingSectionInPresentationOrder + "'" + temp_title.get(r) + "'" + " not listed in presentation order\n";
			}
		}

		for (String aParsedTempPresentationOrder : parsedTempPresentationOrder) {
			if (!newimprovedlyrics.contains("[" + aParsedTempPresentationOrder + "]")) {
				missingPresentationOrderItemInSong = missingPresentationOrderItemInSong + "'" + aParsedTempPresentationOrder + "'" + " not found in song\n";
			}
		}

		// Replace the lyrics for processing
		while (newimprovedlyrics.contains("\n\n\n")) {
			newimprovedlyrics = newimprovedlyrics.replace("\n\n\n","\n\n");
		}
		FullscreenActivity.myLyrics = newimprovedlyrics;
		FullscreenActivity.foundSongSections_heading = temp_title;
		FullscreenActivity.foundSongSections_content = temp_content;
	}
}