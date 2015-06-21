package com.garethevans.church.opensongtablet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.view.View;

public class SetActions extends Activity {

//	public static void updateOptionListSets(Activity act, View view) {
	public static void updateOptionListSets() {
		// Load up the songs in the Sets folder
		File[] tempmyFiles = FullscreenActivity.dirsets.listFiles();
		// Go through this list and check if the item is a directory or a file.
		// Add these to the correct array
		int tempnumfiles = 0;
		if (tempmyFiles!=null) {
			tempnumfiles = tempmyFiles.length;
		}
		int numactualfiles = 0;
		int numactualdirs = 0;
		for (int x = 0; x < tempnumfiles; x++) {
			if (tempmyFiles[x].isFile()) {
				numactualfiles++;
			} else {
				numactualdirs++;
			}
		}
		// Now set the size of the arrays
		FullscreenActivity.mySetsFileNames = new String[numactualfiles];
		FullscreenActivity.mySetsFiles = new File[numactualfiles];
		FullscreenActivity.mySetsFolderNames = new String[numactualdirs];
		FullscreenActivity.mySetsDirectories = new File[numactualdirs];

		// Go back through these items and add them to the file names
		// whichset is an integer that goes through the mySetsFileNames array
		// whichsetfolder is an integer that goes through the mySetsFolderNames
		// array
		int whichset = 0;
		int whichsetfolder = 0;
		for (int x = 0; x < tempnumfiles; x++) {
			if (tempmyFiles[x].isFile()) {
				FullscreenActivity.mySetsFileNames[whichset] = tempmyFiles[x].getName();
				FullscreenActivity.mySetsFiles[whichset] = tempmyFiles[x];
				whichset++;
			} else if (tempmyFiles[x].isDirectory()) {
				FullscreenActivity.mySetsFolderNames[whichsetfolder] = tempmyFiles[x].getName();
				FullscreenActivity.mySetsDirectories[whichsetfolder] = tempmyFiles[x];
				whichsetfolder++;
			}
		}

		// Make the array in the setList list these sets
		// Set the variable setView to be true
		FullscreenActivity.showingSetsToLoad = true;
		// The above line isn't needed anymore
	}

	public static void prepareSetList() {

		FullscreenActivity.mSet = null;
		FullscreenActivity.mSetList = null;
		
		// Remove any blank set entries that shouldn't be there
		FullscreenActivity.mySet = FullscreenActivity.mySet.replace("$**__**$", "");
		
		// Add a delimiter between songs		
		FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$$**_",	"_**$%%%$**_");
		
		// Break the saved set up into a new String[]
		FullscreenActivity.mSet = FullscreenActivity.mySet.split("%%%");
		FullscreenActivity.mSetList = FullscreenActivity.mSet;
		
		// Restore the set back to what it was
		FullscreenActivity.mySet = FullscreenActivity.mySet.replace("_**$%%%$**_", "_**$$**_");

		FullscreenActivity.setSize = FullscreenActivity.mSetList.length;

		// Get rid of tags before and after folder/filenames
		for (int x = 0; x < FullscreenActivity.mSetList.length; x++) {
			FullscreenActivity.mSetList[x] = FullscreenActivity.mSetList[x]
					.replace("$**_", "");
			FullscreenActivity.mSetList[x] = FullscreenActivity.mSetList[x]
					.replace("_**$", "");
		}
	}

	public static void loadASet(View view) throws XmlPullParserException, IOException{
		// Get the linkclicked and load the set into memory
		// Update the savedpreferences to include this set
		// This means reformatting
		
		// Check the OpenSong Scripture _cache Directory exists
		if (FullscreenActivity.dirbibleverses.exists()) {
			// Scripture folder exists, do nothing other than clear it!
			for(File scripfile: FullscreenActivity.dirbibleverses.listFiles()) {
				scripfile.delete(); 
			}
		} else {
			// Tell the user we're creating the OpenSong Scripture _cache directory
			FullscreenActivity.dirbibleverses.mkdirs();
		}

		// Check the Slides _cache Directory exists
		if (FullscreenActivity.dircustomslides.exists()) {
			// Slides folder exists, do nothing other than clear it!
			for(File slidesfile: FullscreenActivity.dircustomslides.listFiles()) {
				slidesfile.delete();
			}
		} else {
			// Tell the user we're creating the Slides _cache directory
			FullscreenActivity.dircustomslides.mkdirs();
		}

		FullscreenActivity.mySetXML = null;
		FullscreenActivity.mySetXML = "";
		FullscreenActivity.myParsedSet = null;

		// Test if file exists - the settoload is the link clicked so is still the set name
		FullscreenActivity.setfile = new File(FullscreenActivity.dirsets + "/"
				+ FullscreenActivity.settoload);
		if (!FullscreenActivity.setfile.exists()) {
			return;
		}


		try {
			FileInputStream inputStream = new FileInputStream(new File(
					FullscreenActivity.dirsets + "/"
							+ FullscreenActivity.settoload));

			if (inputStream != null) {
				InputStreamReader streamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(streamReader);

				String l;

				int count = 0;
				while ((l = bufferedReader.readLine()) != null) {
					// do what you want with the line
					FullscreenActivity.mySetXML = FullscreenActivity.mySetXML
							+ l + "\n";
					count = count + 1;
				}

				inputStream.close();
				bufferedReader.close();

			}

			inputStream.close(); // close the file
		} catch (java.io.FileNotFoundException e) {
			// file doesn't exist
		}
		
		
		// Ok parse the set XML file and extract the stuff needed (slides and bible verses to be kept)
		// Stuff will be saved in mySet string.
		// Songs identified by $**_XXXX_**$
		// Slide contents identified by $**CN_XXXX_CL_XXXX_CT_XXXX_CS_XXXX_**$
		// Scripture contents identified by $**SC_XXXX_SL**$
		
		// Reset any current set
		FullscreenActivity.mySet = null;
		FullscreenActivity.mySet = "";

		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();

		factory.setNamespaceAware(true);
		XmlPullParser xpp;
		xpp = factory.newPullParser();

		xpp.setInput(new StringReader(FullscreenActivity.mySetXML));
		int eventType;
		String scripture_title = "";
		String scripture_translation;
		String scripture_text = "";
		String slide_name = "";
		String slide_title = "";
		String slide_subtitle = "";
		String slide_seconds = "";
		String slide_loop = "";
		String slide_transition = "";
		String slide_notes = "";
		String slide_text = "";

		eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			scripture_translation="";
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("slide_group")) {
					if (xpp.getAttributeValue(1).equals("song")) {
							FullscreenActivity.mySet = FullscreenActivity.mySet 
									+ "$**_" + xpp.getAttributeValue(3) + xpp.getAttributeValue(0) + "_**$";							
					} else if (xpp.getAttributeValue(0).equals("scripture")) {
						// Ok parse this bit seperately
						boolean scripture_finished=false;
						while (!scripture_finished) {
							if (xpp.getName().equals("slide")) {
							//scripture_text = scripture_text + "\n[]\n";
							} else if (xpp.getName().equals("title")) {
								scripture_title = xpp.nextText();
							} else if (xpp.getName().equals("body")) {
								scripture_text = scripture_text + "\n[]\n"+xpp.nextText();
							} else if (xpp.getName().equals("subtitle")) {
								scripture_translation = xpp.nextText();
							}
							eventType = xpp.nextTag();
							if (xpp.getName().equals("notes")) {
								scripture_finished=true;
							}
						}
						scripture_finished=false;
						// Create a new file for each of these entries
						// Filename is title with Scripture/
						
						// Break the scripture_text up into small manageable chunks
						// First up, start each new verse on a new line
						// The verses are in the titles
						//Replace all spaces (split points) with \n
						scripture_text = scripture_text.replace(" ", "\n");
						
						//Split the verses up into an array by new lines
						String[] temp_text = scripture_text.split("\n");
						
						String[] add_text = new String[100];
						int array_line = 0;
						//Add all the array back together and make sure no line goes above 40 characters
						scripture_text = "";
						for (int x=0;x<temp_text.length;x++) {
							if (add_text[array_line]==null) {
								add_text[array_line]="";
							}
							int check;
							check = add_text[array_line].length();
							if (check>40 || temp_text[x].contains("[]")) {
								array_line++;
								if (temp_text[x].contains("[]")) {
									add_text[array_line] = "[]\n ";
								} else {
									add_text[array_line] = " " + temp_text[x];
								}
							} else {
								add_text[array_line] = add_text[array_line]+" "+temp_text[x];
							}
							
						}
						
						// Ok go back through the array and add the non-empty lines back up
						for (int x=0;x<add_text.length;x++) {
							if (add_text[x]!=null && !add_text[x].equals("") ) {
								if (add_text[x].contains("[]")) {
									scripture_text = scripture_text + "\n" + add_text[x];
								} else {
									scripture_text = scripture_text + "\n " + add_text[x];
								}
							}
							
						}
						while (scripture_text.contains("\\n\\n")) {
							scripture_text = scripture_text.replace("\\n\\n","\\n");							
						}
						
						FullscreenActivity.dirbibleverses.mkdirs();
						File temp = new File(FullscreenActivity.dirbibleverses + "/" + scripture_title);
						FileOutputStream overWrite = new FileOutputStream(temp,false);
						// Prepare the new XML file
						String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
						myNEWXML += "<song>\n";
						myNEWXML += "  <title>" + scripture_title + "</title>\n";
						myNEWXML += "  <author>" + scripture_translation + "</author>\n";
						myNEWXML += "  <lyrics>" + scripture_text.trim() + "</lyrics>\n";
						myNEWXML += "</song>";
						overWrite.write(myNEWXML.getBytes());
						overWrite.flush();
						overWrite.close();												
						FullscreenActivity.mySet = FullscreenActivity.mySet
								+ "$**_" +  "Scripture/" + scripture_title + "_**$";
						scripture_text="";
					
					} else if (xpp.getAttributeValue(0).equals("custom") || xpp.getAttributeValue(1).equals("custom")) {
					// Ok parse this bit seperately
					boolean slide_finished=false;
					while (!slide_finished) {
						if (xpp.getAttributeCount()>1) {
						if (xpp.getAttributeName(0).equals("name")) {
						    slide_name = xpp.getAttributeValue(0);
						} else 	if (xpp.getAttributeName(1).equals("name")) {
						    slide_name = xpp.getAttributeValue(1);
						}
						}
						if (xpp.getAttributeCount()>3) {
						if (xpp.getAttributeName(3).equals("seconds")) {
						    slide_seconds = xpp.getAttributeValue(3);
						} 
						}
						if (xpp.getAttributeCount()>4) {
						if (xpp.getAttributeName(4).equals("loop")) {
						    slide_loop = xpp.getAttributeValue(4);
						}
						}
						if (xpp.getAttributeCount()>5) {
						if (xpp.getAttributeName(5).equals("transition")) {
						    slide_transition = xpp.getAttributeValue(5);
						} 
						}
						
						if (xpp.getName().equals("slide")) {
						//slide_text = slide_text + "\n[]\n";
						} else if (xpp.getName().equals("title")) {
							slide_title = xpp.nextText();
						} else if (xpp.getName().equals("subtitle")) {
							slide_subtitle = xpp.nextText();
						} else if (xpp.getName().equals("notes")) {
							slide_notes = xpp.nextText();
						} else if (xpp.getName().equals("body")) {
							slide_text = slide_text + "\n[]\n"+xpp.nextText();
						} else if (xpp.getName().equals("subtitle")) {
							slide_subtitle = xpp.nextText();
						}
						eventType = xpp.nextTag();
						if (xpp.getName().equals("slides")&&xpp.getEventType()==XmlPullParser.END_TAG) {
							slide_finished=true;
						}
					}
					slide_finished=false;
					// Create a new file for each of these entries
					// Filename is title with Scripture/
					
					// Break the scripture_text up into small manageable chunks
					// First up, start each new verse on a new line
					// The verses are in the titles
					//Replace all spaces (split points) with \n
					slide_text = slide_text.replace(" ", "\n");
					
					//Split the verses up into an array by new lines
					String[] temp_text = slide_text.split("\n");
					
					String[] add_text = new String[100];
					int array_line = 0;
					//Add all the array back together and make sure no line goes above 40 characters
					slide_text = "";
					for (int x=0;x<temp_text.length;x++) {
						if (add_text[array_line]==null) {
							add_text[array_line]="";
						}
						int check;
						check = add_text[array_line].length();
						if (check>40 || temp_text[x].contains("[]")) {
							array_line++;
							if (temp_text[x].contains("[]")) {
								add_text[array_line] = "[]\n ";
							} else {
								add_text[array_line] = " " + temp_text[x];
							}
						} else {
							add_text[array_line] = add_text[array_line]+" "+temp_text[x];
						}
						
					}
					
					// Ok go back through the array and add the non-empty lines back up
					for (int x=0;x<add_text.length;x++) {
						if (add_text[x]!=null && !add_text[x].equals("")) {
							if (add_text[x].contains("[]")) {
								slide_text = slide_text + "\n" + add_text[x];
							} else {
								slide_text = slide_text + "\n " + add_text[x];
							}
						}
						
					}
					while (slide_text.contains("\\n\\n")) {
						slide_text = slide_text.replace("\\n\\n","\\n");							
					}
					
					FullscreenActivity.dircustomslides.mkdirs();
					File temp = new File(FullscreenActivity.dircustomslides + "/" + slide_name);
					FileOutputStream overWrite = new FileOutputStream(temp,false);
					// Prepare the new XML file
					String myNEWXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
					myNEWXML += "<song>\n";
					myNEWXML += "  <title>" + slide_title + "</title>\n";
					myNEWXML += "  <author>" + slide_subtitle + "</author>\n";
					myNEWXML += "  <user1>" + slide_seconds + "</user1>\n";
					myNEWXML += "  <user2>" + slide_loop + "</user2>\n";
					myNEWXML += "  <user3>" + slide_transition + "</user3>\n";
					myNEWXML += "  <aka>" + slide_name + "</aka>\n";
					myNEWXML += "  <key_line>" + slide_notes + "</key_line>\n";
					myNEWXML += "  <lyrics>" + slide_text.trim() + "</lyrics>\n";
					myNEWXML += "</song>";
					overWrite.write(myNEWXML.getBytes());
					overWrite.flush();
					overWrite.close();												
					FullscreenActivity.mySet = FullscreenActivity.mySet
							+ "$**_" +  "Slide/" + slide_name + "_**$";
					scripture_text="";
				}
				}
			}
			eventType = xpp.next();						
			
		}

	}

	public static void indexSongInSet() {
		FullscreenActivity.setSize = FullscreenActivity.mSetList.length;
		FullscreenActivity.previousSongInSet = "";
		FullscreenActivity.nextSongInSet = "";

		// Go backwards through the setlist - this finishes with the first occurrence
		// Useful for duplicate items, otherwise it returns the last occurrence
		// Not yet tested, so left
		for (int x = 0; x < FullscreenActivity.setSize; x++) {
//		for (int x = FullscreenActivity.setSize-1; x<1; x--) {
			if (FullscreenActivity.mSet[x].equals(FullscreenActivity.whatsongforsetwork)) {
				FullscreenActivity.indexSongInSet = x;
				if (x>0) {
					FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[x - 1];
				}
				if (x != FullscreenActivity.setSize - 1) {
					FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[x + 1];
				}
			}
		}
	}

	public static void getSongForSetWork() {
		if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)
				|| FullscreenActivity.whichSongFolder.equals("") ) {
			FullscreenActivity.whatsongforsetwork = FullscreenActivity.songfilename;
		} else {
			FullscreenActivity.whatsongforsetwork = FullscreenActivity.whichSongFolder + "/"
					+ FullscreenActivity.songfilename;
		}
	}

	public static boolean isSongInSet() {
        if (FullscreenActivity.setSize > 0) {
            // Get the name of the song to look for (including folders if need be)
            getSongForSetWork();

            if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                // The first song has an index of 6 (the 7th item as the rest are menu items)
                FullscreenActivity.setView = "Y";
                FullscreenActivity.previousSongInSet = "";
                FullscreenActivity.nextSongInSet = "";

                // Get the song index
                indexSongInSet();
                return true;

            } else {
                // Song isn't in the set, so just show the song
                // Switch off the set view (buttons in action bar)
                FullscreenActivity.setView = "N";
                return false;
            }
        } else {
            // User wasn't in set view, or the set was empty
            // Switch off the set view (buttons in action bar)
            FullscreenActivity.setView = "N";
            return false;
        }
    }
}
