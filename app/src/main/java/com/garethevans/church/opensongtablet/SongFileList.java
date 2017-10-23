package com.garethevans.church.opensongtablet;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Created by James on 10/22/17.
 */

/* final class - uninheritable - indicates to compiler how to allocate memory
* defined with new keyword.  Private member folderList accessible by getter
* getFolderlist which initialises the folderList if it is null, and then
* populates it with the folderList, which is parsed to remove the path prefix.
* After initialisation, the getter returns the cached variable list.
* You can then reinitialise the function in case a folder has been added.  This often
* involves adding folder watchers and other contraptions.
* The benefit of having an encapsulated class is that you don't need to return and rerun
* the functions each time the folder is opened.  Often, programs do this by keeping
* an abstraction that doesn't take up much room, and can be accessed quickly, and only
* adding or subtracting to the abstraction (the array or list etc) rather than
* rebuilding the entire list.
* This class uses a recursive function that allows a complete list to be built.
* Recursion is a powerful way to traverse tree structures.  It involves the fact that like
* a class, a function is an object that is stored in memory.  That object has paths that lead
* to it, that are stored at the same time.  The function, when called is placed on memory, and
* the values of its internal local variables are stored as well (lookup the word scope).  Then
* inside the function, the same function is called, pushed onto the stack in the process with a
* 'path' back to the original copy of the function that called it.  This then allows the function
* to be called internally as many times as necessary, and each time modifying the local variables
* before adding to the class level variable (folderList) and then being discarded from memory as
* the previous function is popped of the stack, its local variables examined etc etc.  This process
* of recursing down a tree and then moving back up it is memory efficient as the compiler is tuned
* to doing this sort of memory allocation task.  It tends to be very quick.  If you're interested
* look up, depth first and vs breadth first recursion.
* I've stubbed out a getter function for the getSongList function.  This doesn't need recursion
* but if you create the list at the beginning then it takes less time, is easier on automatic
* memory management and can be accessed, added to and/or rebuilt when a song is added for instance,
* if necessary.  Whilst xml files offer some benefits, and with good use of xmlobjects can be
* strong and safe to use, a database would be better for this app, as the songs can then be
* tagged with user defined tags such as genre and emotional content, or perhaps in the case
* of bible study, message appropriateness or thematic moral analogy.  Second guessing
* the tag names for an author is so restrictive as to be useless, so designing a good database
* schema and then letting the author make their own tags is a great idea.
* If you enjoy programming, then learn database design.  Programming in all instances, is about
* the storage and manipulation of data.
* */

public final class SongFileList
{
    private ArrayList<String> folderList;
    private ArrayList<String> currentFileList;

    // constructor
    public SongFileList()
    {
        folderList = new ArrayList<String>();
        currentFileList = new ArrayList<String>();
    }

    /*getters and setters*/
    /*getFolderList - package private, returns Array of String
    * creates list of folders and caches it in private class variable
    * which it then returns*/
    @NonNull
    String[] getFolderList()
    {
        if (folderList != null)
        {
            // initialize toArray[T] with empty array vs size -> https://shipilev.net/blog/2016/arrays-wisdom-ancients/
            return folderList.toArray(new String[folderList.size()].clone());
        }
        else
        {
            String topLevelFilePath = FullscreenActivity.dir.getAbsolutePath();
            folderList = new ArrayList<String>();
            initialiseFolderList(new File(topLevelFilePath));
            postprocessListPath(topLevelFilePath);
            return folderList.toArray(new String[folderList.size()]).clone();
        }
    }

    /*getSongFileList() - package private, returns array of String
    * returns an array of the file names of the currently chosen folder
    * */
    String[] getSongFileListasArray()
    {
        fileList();
        return currentFileList.toArray(new String[currentFileList.size()]).clone();
    }

    List<String> getSongFileListasList()
    {
        fileList();
        return currentFileList;
    }

    private void fileList()
    {
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }
        // TODO: 10/23/17 add filter to cut out directories before add to file list 
        com.annimon.stream.Stream.of(foldertoindex.list()).filterforEach(s->{currentFileList.add(s);});
    }

    private void postprocessListPath(final String topLevelFilePath)
    {
        //lambda function used by adding retrolambda dependency
        UnaryOperator<String> unaryComp = i->i.substring(topLevelFilePath.length());
        folderList.replaceAll(unaryComp);
    }

    private void initialiseFolderList(File rfile)
    {
        if((rfile.listFiles() != null) && (rfile.listFiles().length > 0))
        {
            for (File file : rfile.listFiles())
            {
                if(file.isDirectory())
                {
                    folderList.add(file.toString());
                    initialiseFolderList(file);
                }
            }
        }
    }

    private void getSongList()
    {

    }
}