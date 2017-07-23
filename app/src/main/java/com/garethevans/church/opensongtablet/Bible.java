package com.garethevans.church.opensongtablet;

class Bible {

    static boolean isYouVersionScripture(String importtext) {
        // A simple way to check if this is a scripture file from Bible
        // is to look for the last line starting with http://bible.com

        // Split the string into separate lines
        // If it is a scripture, the last line indicates so
        // The second last line is the Scripture reference
        String[] importtextparts = importtext.split("\n");
        int lines = importtextparts.length;
        String identifying_line = "http://bible.com";
        if (lines>2 && importtextparts[lines-1].contains(identifying_line)) {
            // Ok it is a scripture
            FullscreenActivity.mScripture = importtextparts[0];
            if (importtextparts[1]!=null) {
                FullscreenActivity.scripture_title = importtextparts[1];
            } else {
                FullscreenActivity.scripture_title = "";
            }
            if (importtextparts[0]!=null) {
                FullscreenActivity.mScripture = importtextparts[0];
            } else {
                FullscreenActivity.mScripture = "";
            }
            return true;
        } else {
            return false;
        }
    }
}
