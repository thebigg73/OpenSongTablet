package com.garethevans.church.opensongtablet.setmenu;

public class SetItemInfo {
    public String songtitle="";        // The title (name) of the song - requires loading/database
    public String songfilename="";     // The filename - saved in the set entry
    public String songfolder="";       // The folder
    public String songfoldernice="";   // Replacing ../ with **Variations (translated), etc.
    public String songicon="";         // Which type of icon based on the filetype
    public int songitem=-1;            // Numbered position in the set
    public String songkey="";          // The key of the song
    public String songforsetwork;   // The code for the song in the set preference
    public void setItem(String songfolder, String songfilename, String songtitle, String songkey,
                        int songitem, String songforsetwork, String songicon) {
        this.songfolder = songfolder;
        this.songfoldernice = songfolder;
        this.songfilename = songfilename;
        this.songtitle = songtitle;
        this.songkey = songkey;
        this.songforsetwork = songforsetwork;
        this.songitem = songitem;
        this.songicon = songicon;
    }
}