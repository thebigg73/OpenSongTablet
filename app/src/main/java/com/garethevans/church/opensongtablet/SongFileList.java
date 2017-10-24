package com.garethevans.church.opensongtablet;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.io.File;
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
    private ArrayList<String>   folderList;
    private ArrayList<String>   currentFileList;
    private String              topLevelFilePath;
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
        if (!folderList.isEmpty())
        {
            // initialize toArray[T] with empty array vs size -> https://shipilev.net/blog/2016/arrays-wisdom-ancients/
            return folderList.toArray(new String[folderList.size()].clone());
        }
        else
        {
            topLevelFilePath = FullscreenActivity.dir.getAbsolutePath();
            initialiseFolderList(new File(topLevelFilePath));
            postprocessListPath();
            return folderList.toArray(new String[folderList.size()]).clone();
        }
    }

    /*this function simply strips the leading prefix from the file path
    * you might want to add +1 to the substring index to remove the trailing
    * slash, but I kind of like it in the menu*/
    private void postprocessListPath()
    {
        UnaryOperator<String> unaryComp = i->i.substring(topLevelFilePath.length());
        folderList.replaceAll(unaryComp);
        folderList.add(0, FullscreenActivity.mainfoldername);
    }

    /*getSongFileList() - package private, returns array of String
    * returns an array of the file names of the currently chosen folder
    * */
    String[] getSongFileListasArray()
    {
        //todo place check here to see if new file has been added since the last file list was
        //constructed.  This saves memory.
        fileList();
        return currentFileList.toArray(new String[currentFileList.size()]).clone();
    }

    /* a getter to return a list, should it be required. */
    List<String> getSongFileListasList()
    {
        //todo place check here to see if new file has been added since the last file list was
        //constructed.  This saves memory.  Since we are doing it in two places
        //we might want to construct a class or consider using semaphores or some other
        //existing structure.  I haven't done much multithreaded stuff, but it always
        //sounds like a sensible idea!  One might at the least, consider using a folderwatcher
        //to examine the folder perhaps this:
        //https://developer.android.com/reference/android/os/FileObserver.html
        //the fileobserver class, once instantiated and for its lifecycle, watches a folder
        //it is an abstract class, so one might make a class to represent the folders in which
        //the songs are stored, that implements the FileObserver method.  Then one creates an
        //array or list of folders as a member of another class and whenever files are added to any
        //folder, the event is fired, and the folder class can rescan and reconstruct its filelist.
        //the superclass that contains the folders can recursively scan through its list of folders
        //when the app closes the superclass can be responsible for deleting it object references.
        //good memory management.  Also, if there is a folderwatcher, then I don't need to check
        //to see if the folder has changed, or like I do here, clear the currentFileList structure
        //each time I call the getter.
        fileList();
        return currentFileList;
    }

    private void fileList()
    {
        currentFileList.clear();
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }
        // I added the stream and lambda java8 functions as good practice is developing
        // to use the latest functions. Its more about staying current than anything else,
        // as when the functions are adopted in to mainstream, you understand them.
        // Not that I develop for a living so don't take my word for it, but I think that
        // that is the way it is currently.
        com.annimon.stream.Stream.of(foldertoindex.listFiles()).filter(file -> !file.isDirectory()).forEach(file -> currentFileList.add(file.getName()));
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