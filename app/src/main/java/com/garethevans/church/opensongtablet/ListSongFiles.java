package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class ListSongFiles extends Activity {

    static Collator coll;

    // OLD ONE
    public static void listSongFolders() {
        FullscreenActivity.allfilesforsearch.clear();
        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        // Go through this list and check if the item is a directory or a file.
        int tempnumitems;
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
        ArrayList<String> tempProperDirectories = new ArrayList<>();

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
        Collections.sort(tempProperDirectories, coll);

        // Because the MAIN folder doesn't exist as a listed folder, it is just the root,
        // We need to add it as the first folder manually.
        // All other folders go as an index 1 higher
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
        ArrayList<String> tempMainProperFiles= new ArrayList<>();
        int temp_mainnumfilescount = 0;
        for (int x=0; x<main_numfiles; x++) {
            if (temp_mainfiles[x] != null && !temp_mainfiles[x].isDirectory() && temp_mainfiles[x].isFile()){
                tempMainProperFiles.add(temp_mainfiles[x].getName());
                FullscreenActivity.allfilesforsearch.add(temp_mainfiles[x].getName() + " %%% " + FullscreenActivity.mainfoldername);
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
            File currsongfolder = new File(FullscreenActivity.dir.getAbsolutePath() + "/"+FullscreenActivity.mSongFolderNames[w]);
            File[] tempmyfiles = currsongfolder.listFiles();
            // Go through this list and check if the item is a directory or a file.
            int tempnumfiles;
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
            ArrayList<String> tempProperFiles= new ArrayList<>();

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

            for (int f=0; f<tempProperFiles.size(); f++) {
                try {
                    FullscreenActivity.allfilesforsearch_folder.add(FullscreenActivity.mSongFolderNames[w]);
                    FullscreenActivity.allfilesforsearch_song.add(tempProperFiles.get(f));
                    FullscreenActivity.allfilesforsearch.add(tempProperFiles.get(f) + " %%% " + FullscreenActivity.mSongFolderNames[w]);
                } catch (Exception e) {
                    //Something went wrong here
                    e.printStackTrace();
                }
            }
        }
    }

    public static void getAllSongFolders() {
        FullscreenActivity.allfilesforsearch.clear();

        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        //Now set the size of the temp arrays
        ArrayList<String> firstleveldirectories = new ArrayList<>();
        ArrayList<String> secondleveldirectories = new ArrayList<>();
        ArrayList<String> tempProperDirectories = new ArrayList<>();

        //Now read the folder names for the first level directories
        if (tempmyitems!=null) {
            for (File tempmyitem : tempmyitems) {
                if (tempmyitem != null && tempmyitem.isDirectory()) {
                    firstleveldirectories.add(tempmyitem.getName());
                }
            }
        }

        //Now go through the firstlevedirectories and look for subfolders
        for (int x = 0; x < firstleveldirectories.size(); x++) {
            File folder = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + firstleveldirectories.get(x));
            File[] subfoldersearch = folder.listFiles();
            if (subfoldersearch!=null) {
                for (File aSubfoldersearch : subfoldersearch) {
                    if (aSubfoldersearch != null && aSubfoldersearch.isDirectory()) {
                        secondleveldirectories.add(firstleveldirectories.get(x) + "/" + aSubfoldersearch.getName());
                    }
                }
            }
        }

        // Now combine the two arrays and save them as a string array
        tempProperDirectories.addAll(firstleveldirectories);
        tempProperDirectories.addAll(secondleveldirectories);
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempProperDirectories,coll);

        // Add the main directory - +1 to add the MAIN folder as position 0
        FullscreenActivity.mSongFolderNames = new String[tempProperDirectories.size()+1];
        FullscreenActivity.mSongFolderNames[0] = FullscreenActivity.mainfoldername;
        for (int z=0; z<tempProperDirectories.size(); z++) {
            FullscreenActivity.mSongFolderNames[z+1] = tempProperDirectories.get(z);
        }
    }

    public static void getAllSongFiles() {
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }
        File[] tempmyFiles = foldertoindex.listFiles();

        ArrayList<String> tempProperSongFiles = new ArrayList<>();
        if (tempmyFiles!=null) {
            for (File tempmyFile : tempmyFiles) {
                if (tempmyFile != null && tempmyFile.isFile()) {
                    tempProperSongFiles.add(tempmyFile.getName());
                }
            }
        }

        // Sort the files alphabetically using locale
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempProperSongFiles, coll);

        FullscreenActivity.mSongFileNames = new String[tempProperSongFiles.size()];

        FullscreenActivity.mSongFileNames = tempProperSongFiles.toArray(FullscreenActivity.mSongFileNames);
    }

    // OLD ONE
    public static void listSongs() {
        // A temporary array to put the file names into.  Do this, sort the array
        // List the items in the main storage location into a temporary array.
        // What song folder is being viewed?
        // If it is MAIN then it is the main one
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }
        File[] tempmyFiles = foldertoindex.listFiles();
        // Go through this list and check if the item is a directory or a file.
        // Add these to the correct array
        int tempnumfiles;
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
        ArrayList<String> tempProperSongFiles = new ArrayList<>();
        ArrayList<String> tempProperDirectories = new ArrayList<>();

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

        //Add folder name to first item of songlist
        tempProperSongFiles.add(0, FullscreenActivity.whichSongFolder);
        tempProperDirectories.add(0, FullscreenActivity.mainfoldername);

        //Make the real arrays one bigger
        FullscreenActivity.mSongFileNames = new String[numactualfiles+1];
        //FullscreenActivity.mSongFileNames = new String[numactualfiles];
        FullscreenActivity.mSongFolderNames = new String[numactualdirs+1];
        //FullscreenActivity.mSongFolderNames = new String[numactualdirs];

        FullscreenActivity.mSongFileNames = tempProperSongFiles.toArray(FullscreenActivity.mSongFileNames);
        FullscreenActivity.mSongFolderNames = tempProperDirectories.toArray(FullscreenActivity.mSongFolderNames);
    }

    public static void getCurrentSongIndex() {
        // Find the current song index from the song filename
        // Set them all to 0
        FullscreenActivity.currentSongIndex = 0;
        FullscreenActivity.nextSongIndex = 0;
        FullscreenActivity.previousSongIndex = 0;


        // Go through the array
        if (FullscreenActivity.mSongFileNames!=null && FullscreenActivity.songfilename!=null) {
            for (int s = 0; s < FullscreenActivity.mSongFileNames.length; s++) {
                if (FullscreenActivity.mSongFileNames != null &&
                        FullscreenActivity.songfilename != null &&
                        FullscreenActivity.mSongFileNames[s] != null &&
                        FullscreenActivity.mSongFileNames[s].equals(FullscreenActivity.songfilename)) {
                    FullscreenActivity.currentSongIndex = s;
                    if (s > 1) {
                        FullscreenActivity.previousSongIndex = s - 1;
                    } else {
                        FullscreenActivity.previousSongIndex = s;
                    }
                    if (s < FullscreenActivity.mSongFileNames.length - 1) {
                        FullscreenActivity.nextSongIndex = s + 1;
                    } else {
                        FullscreenActivity.nextSongIndex = s;
                    }
                }
            }
        }
    }

    public static void deleteSong(Context c) {
        FullscreenActivity.setView = false;
        String setFileLocation;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
        } else {
            setFileLocation = FullscreenActivity.dir + "/" +
                    FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
        }
        File filetoremove = new File(setFileLocation);
        if (filetoremove.delete()) {
            FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" "
                    + c.getString(R.string.songhasbeendeleted);
        } else {
            FullscreenActivity.myToastMessage = c.getString(R.string.deleteerror_start)
                    + " \"" + FullscreenActivity.songfilename + "\" "
                    + c.getString(R.string.deleteerror_end_song);
        }
    }
}