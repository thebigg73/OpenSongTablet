package com.garethevans.church.opensongtablet.webserver;

public class SongIdentifier {

    // A simple class that can be received from the Webserver that identifies a song
    // It has two main values: folder and filename
    // We will in future, also have the option of sending other actions to the server:
    //    - cue: Just load a song
    //    - autoscroll: Toggle starting/stopping the autoscroll
    //    - metronome: Toggle starting/stopping the metronome
    //    - pad: Toggle starting/stopping the pad
    //    - nextsection: Select the next section in the song
    //    - previoussection: Select the previous section in the song
    //    - next: Move to the next song (in the set list, or song menu)
    //    - previous: Move to the previous song (in the set list, or song menu)

    private String folder;
    private String filename;
    private String action = "cue";

    public SongIdentifier(String folder, String filename) {
        this.folder = folder;
        this.filename = filename;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getFolder() {
        return folder;
    }
    public String getFilename() {
        return filename;
    }
    public String getAction() {
        return action;
    }

}
