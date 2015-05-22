package com.garethevans.church.opensongtablet;

import android.app.Activity;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class ListSongFiles extends Activity {

	static Collator coll;

	public static void listSongFolders() {
		File songfolder = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs");
		File[] tempmyitems = null;
		if (songfolder.isDirectory() && songfolder!=null) {
				tempmyitems = songfolder.listFiles();
		}
		// Go through this list and check if the item is a directory or a file.
		int tempnumitems=0;
		if (tempmyitems != null && tempmyitems.length>0) {
			tempnumitems = tempmyitems.length;
		} else {
			tempnumitems = 0;
		}
		int numactualdirs  = 0;
		for (int x=0; x<tempnumitems; x++) {
			if (tempmyitems[x] != null && tempmyitems[x].isDirectory()){
				numactualdirs ++;
			}
		}
		
		//Now set the size of the temp arrays
		ArrayList<String> tempProperDirectories = new ArrayList<String>();

		//Now read the stuff into the temp array
		for (int x=0; x<tempnumitems; x++) {
			if (tempmyitems[x] != null && tempmyitems[x].isDirectory()) {
				tempProperDirectories.add(tempmyitems[x].getName());
			}
		}

		//Sort these arrays
		// Add locale sort
		coll = Collator.getInstance(FullscreenActivity.locale);
		coll.setStrength(Collator.SECONDARY);
		Collections.sort(tempProperDirectories,coll);
		//Collections.sort(tempProperDirectories, String.CASE_INSENSITIVE_ORDER);
		
		FullscreenActivity.mSongFolderNames = new String[numactualdirs+1];
		FullscreenActivity.mSongFolderNames = tempProperDirectories.toArray(FullscreenActivity.mSongFolderNames);

		// Now go through each folder and add the file names to these arrays
		FullscreenActivity.childSongs = new String[numactualdirs+1][];

		// Add the MAIN folder first
		File[] temp_mainfiles = songfolder.listFiles();
		int main_numfiles = 0;
		if (songfolder.isDirectory() && temp_mainfiles != null) {
			main_numfiles = temp_mainfiles.length;			
		}

		// Go through this list and check if the item is a directory or a file.
		if (temp_mainfiles != null && main_numfiles>0 && songfolder.isDirectory()) {
				main_numfiles = temp_mainfiles.length;
			} else {
				main_numfiles = 0;
			}
		//Now set the size of the temp arrays
		ArrayList<String> tempMainProperFiles= new ArrayList<String>();
		int temp_mainnumfilescount = 0;
		for (int x=0; x<main_numfiles; x++) {
			if (temp_mainfiles[x] != null && !temp_mainfiles[x].isDirectory() && temp_mainfiles[x].isFile()){
				tempMainProperFiles.add(temp_mainfiles[x].getName());
				temp_mainnumfilescount++;
			}
		}
			
		//Sort these arrays
		// Add locale sort
		Collections.sort(tempMainProperFiles,coll);
		//Collections.sort(tempMainProperFiles, String.CASE_INSENSITIVE_ORDER);

		FullscreenActivity.childSongs[0] = new String[temp_mainnumfilescount];
		FullscreenActivity.childSongs[0] = tempMainProperFiles.toArray(FullscreenActivity.childSongs[0]);
		
		
		for (int w=0;w<numactualdirs;w++) {
			File currsongfolder = new File(FullscreenActivity.root.getAbsolutePath() + "/documents/OpenSong/Songs/"+FullscreenActivity.mSongFolderNames[w]);
			File[] tempmyfiles = currsongfolder.listFiles();		
			// Go through this list and check if the item is a directory or a file.
			int tempnumfiles=0;
			if (tempmyfiles != null && tempmyfiles.length>0) {
				tempnumfiles = tempmyfiles.length;
			} else {
				tempnumfiles = 0;
			}
			int numactualfiles  = 0;
			for (int x=0; x<tempnumfiles; x++) {
				if (tempmyfiles[x] != null && tempmyfiles[x].isFile()){
					numactualfiles ++;
				}
			}
			
			//Now set the size of the temp arrays
			ArrayList<String> tempProperFiles= new ArrayList<String>();

			//Now read the stuff into the temp array
			for (int x=0; x<numactualfiles; x++) {
				if (tempmyfiles[x] != null && tempmyfiles[x].isFile()) {
					tempProperFiles.add(tempmyfiles[x].getName());
				}
			}

			//Sort these arrays
			// Add locale sort
			Collections.sort(tempProperFiles,coll);
			//Collections.sort(tempProperFiles, String.CASE_INSENSITIVE_ORDER);

			FullscreenActivity.childSongs[w+1] = new String[numactualfiles];
			FullscreenActivity.childSongs[w+1] = tempProperFiles.toArray(FullscreenActivity.childSongs[w+1]);

		}
	}
	
	public static void listSongs() {
		// A temporary array to put the file names into.  Do this, sort the array
		// In the proper array mSongFileNames make it one item larger
		// Set item 0 to be the menu showing which folder is being browsed
		// Then add all the temp array items into the new one in a position higher by 1

		// List the items in the main storage location into a temporary array.
		// What song folder is being viewed?
		// If it is MAIN then it is the main one
        if (FullscreenActivity.whichSongFolder==null) {
            FullscreenActivity.whichSongFolder = "";
        }
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
			FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath()
					+ "/documents/OpenSong/Songs");				
		} else {
			FullscreenActivity.dir = new File(FullscreenActivity.root.getAbsolutePath()
					+ "/documents/OpenSong/Songs/" + FullscreenActivity.whichSongFolder);
		}
		File[] tempmyFiles = FullscreenActivity.dir.listFiles();		
		// Go through this list and check if the item is a directory or a file.
		// Add these to the correct array
		int tempnumfiles=0;
		if (tempmyFiles != null && tempmyFiles.length>0) {
			tempnumfiles = tempmyFiles.length;
		} else {
			tempnumfiles = 0;
		}
		int numactualfiles = 0;
		int numactualdirs  = 0;
		for (int x=0; x<tempnumfiles; x++) {
			if (tempmyFiles[x] != null && tempmyFiles[x].isFile()) {
				numactualfiles ++;
			} else if (tempmyFiles[x] != null){
				numactualdirs ++;
			}
		}
		
			//Now set the size of the temp arrays
			ArrayList<String> tempProperSongFiles = new ArrayList<String>();
			ArrayList<String> tempProperDirectories = new ArrayList<String>();

		

		//Now read the stuff into the temp arrays
		for (int x=0; x<tempnumfiles; x++) {
			if (tempmyFiles[x] != null && tempmyFiles[x].isFile()) {
				tempProperSongFiles.add(tempmyFiles[x].getName());
			} else if (tempmyFiles[x] != null && tempmyFiles[x].isDirectory()) {
				tempProperDirectories.add(tempmyFiles[x].getName());
			}
		}

		//Sort these arrays
		// Add locale sort
		coll = Collator.getInstance(FullscreenActivity.locale);
		coll.setStrength(Collator.SECONDARY);
		Collections.sort(tempProperSongFiles, coll);
		Collections.sort(tempProperDirectories,coll);
		//Collections.sort(tempProperSongFiles, String.CASE_INSENSITIVE_ORDER);
		//Collections.sort(tempProperDirectories, String.CASE_INSENSITIVE_ORDER);
		
		//Make the arrays for the song id search
		// NOT WORKING YET
		//FullscreenActivity.search_songid = new String[tempnumfiles];
		//FullscreenActivity.search_content = new String[tempnumfiles];
		//FullscreenActivity.search_songid = tempProperSongFiles.toArray(FullscreenActivity.search_songid);
		

		//Add folder name to first item of songlist
		tempProperSongFiles.add(0, "(" + FullscreenActivity.whichSongFolder + ")");
		tempProperDirectories.add(0, "(" + FullscreenActivity.mainfoldername + ")");
		
		//Make the real arrays one bigger
		FullscreenActivity.mSongFileNames = new String[numactualfiles+1];
		FullscreenActivity.mSongFolderNames = new String[numactualdirs+1];
		
		FullscreenActivity.mSongFileNames = tempProperSongFiles.toArray(FullscreenActivity.mSongFileNames);
		FullscreenActivity.mSongFolderNames = tempProperDirectories.toArray(FullscreenActivity.mSongFolderNames);
	}
	
	public static void getCurrentSongIndex() {
		// Find the current song index from the song filename
		// Set them all to 1
		FullscreenActivity.currentSongIndex = 1;
		FullscreenActivity.nextSongIndex = 1;
		FullscreenActivity.previousSongIndex = 1;
		
		// Go through the array
		for (int s = 1; s < FullscreenActivity.mSongFileNames.length; s++) {
			if (FullscreenActivity.mSongFileNames[s].equals(FullscreenActivity.songfilename)) {
				FullscreenActivity.currentSongIndex = s;
				if (s > 1) {
					FullscreenActivity.previousSongIndex = s-1;
				} else {
					FullscreenActivity.previousSongIndex = s;
				}
				if (s < FullscreenActivity.mSongFileNames.length -1) {
					FullscreenActivity.nextSongIndex = s+1;
				} else {
					FullscreenActivity.nextSongIndex = s;
				}
			}
		}
		
		
	}
}