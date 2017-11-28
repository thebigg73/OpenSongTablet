package com.garethevans.church.opensongtablet;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Predicate;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

// File created by James on 10/22/17.

//Todo refactor stream statement if necessary (performance) and use collection

/* final class - uninheritable -   Private member folderList accessible by getter
* getFolderlistasList which initialises the folderList if it is null, and then
* populates it with the folderList, which is parsed to remove the path prefix.
* */
final class SongFileList {
    private ArrayList<String>   folderList;
    private ArrayList<String>   currentFileList;
    private String              topLevelFilePath;
    Collator coll;

    // constructor
    SongFileList() {
        folderList = new ArrayList<>();
        currentFileList = new ArrayList<>();
    }

    /*getters and setters*/
    /*getFolderList - package private, returns Array of String
    * creates list of folders and caches it in private class variable
    * which it then returns*/
    @NonNull
    String[] getFolderList() {
        if (!folderList.isEmpty()) {
            // initialize toArray[T] with empty array vs size -> https://shipilev.net/blog/2016/arrays-wisdom-ancients/
            // Sort the folder list alphabetically
            try {
                coll = Collator.getInstance(FullscreenActivity.locale);
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(folderList, coll);
            } catch (Exception e) {
                // Error sorting
                Log.d("d","Error sorting");
            }
            return folderList.toArray(new String[folderList.size()].clone());
        } else {
            topLevelFilePath = FullscreenActivity.dir.getAbsolutePath();
            initialiseFolderList(new File(topLevelFilePath));
            postprocessListPath();
            try {
                coll = Collator.getInstance(FullscreenActivity.locale);
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(folderList, coll);
            } catch (Exception e) {
                // Error sorting
                Log.d("d","Error sorting");
            }
            return folderList.toArray(new String[folderList.size()]).clone();
        }
    }

    /*this function simply strips the leading prefix from the file path*/
    private void postprocessListPath() {

        //replaceAll(unaryComp) is only available for newer versions of Android.
        // Added a check and alternative for older versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UnaryOperator<String> unaryComp = new UnaryOperator<String>() {
                @Override
                public String apply(String i) {
                    return i.substring(topLevelFilePath.length() + 1);
                }
            };
            folderList.replaceAll(unaryComp);

        } else {
            for (int z=0;z<folderList.size();z++) {
                String new_string = folderList.get(z).substring(topLevelFilePath.length() + 1);
                folderList.set(z,new_string);
            }
        }
        folderList.add(0, FullscreenActivity.mainfoldername);
    }

    /*getSongFileList() - package private, returns array of String
    * returns an array of the file names of the currently chosen folder
    * */
    String[] getSongFileListasArray() {
        //todo place check here to see if new file has been added since the last file list was
        //constructed.  This saves memory.
        fileList();
        // Sort the file list
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(currentFileList, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d","Error sotrrting");
        }
        return currentFileList.toArray(new String[currentFileList.size()]).clone();
    }

    /* a getter to return a list, should it be required. */
    List<String> getSongFileListasList() {
        //todo datastructure to encapsulate currentFileList and include invalidate
        //code, perhaps event handling?
        fileList();
        // Sort the file list
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(currentFileList, coll);
        } catch (Exception e) {
            // Error sorting
            Log.d("d","Error sotrrting");
        }
        return currentFileList;
    }
    /*private function to modify currentFileList by scanning the currently selected
    * folder
    * */
    private void fileList() {
        currentFileList.clear();
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }

        // my bad, I forgot that performance is an issue - so need to refactor this
        // https://stackoverflow.com/questions/22658322/java-8-performance-of-streams-vs-collections
        // I'm not even using java8, of course, so it may be even slower.
        com.annimon.stream.Stream.of(foldertoindex.listFiles()).forEach(new Consumer<File>() {
            @Override
            public void accept(File file) {
                /*String tempfile = file.getName();
                if(file.isDirectory())
                {
                    tempfile = "Dir... " + tempfile;
                }*/
                currentFileList.add(file.getName());
            }
        });
    }

    /*intialises the folderList variable*/
    private void initialiseFolderList(File rfile) {
        if ((rfile.listFiles() != null) && (rfile.listFiles().length > 0)) {
            for (File file : rfile.listFiles()) {
                if(file.isDirectory()) {
                    folderList.add(file.toString());
                    initialiseFolderList(file);
                }
            }
        }
    }
}