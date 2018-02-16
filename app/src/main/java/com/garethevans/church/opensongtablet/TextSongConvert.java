package com.garethevans.church.opensongtablet;

import android.content.Context;

class TextSongConvert {

    // This class is called from the edit song window - try to convert to fix OpenSong format button
    static String convertText(Context c, String oldtext) {
        String newtext = "";

        // Split the whole thing into lines
        String[] lines = oldtext.split("\n");
        for (String l:lines) {
            // Look for headings
            boolean alreadyheading = l.startsWith("[");
            if (!alreadyheading) {
                if (l.contains(c.getString(R.string.tag_verse)) || l.contains(c.getString(R.string.tag_chorus)) ||
                        l.contains(c.getString(R.string.tag_bridge)) || l.contains(c.getString(R.string.tag_ending)) ||
                        l.contains(c.getString(R.string.tag_instrumental)) || l.contains(c.getString(R.string.tag_interlude)) ||
                        l.contains(c.getString(R.string.tag_intro)) || l.contains(c.getString(R.string.tag_prechorus)) ||
                        l.contains(c.getString(R.string.tag_refrain)) || l.contains(c.getString(R.string.tag_tag)) ||
                        l.contains(c.getString(R.string.tag_reprise))) {
                    // Remove any colons and white space
                    l = l.replace(":","");
                    l = l.trim();
                    // Add the tag braces
                    l = "[" + l + "]";
                    //Remove any double braces
                    l = l.replace("[[", "[");
                    l = l.replace("]]", "]");
                }
            }
            // Look for chord lines
            if (!l.startsWith(".") && !l.startsWith("[") && !l.startsWith(";")) {
                // Do this by splitting the line into sections split by space
                String[] possiblechordline = l.split(" ");
                // Go through each split bit and get the length of the non empty ones.  We'll then average the lengths
                int numnonempties = 0;
                int totalsize = 0;
                for (String ch : possiblechordline) {
                    if (ch.length() > 0) {
                        numnonempties += 1;
                        totalsize += ch.length();
                    }
                }
                // Get the average length of the bits
                if (numnonempties > 0 && totalsize > 0) {
                    float avlength = (float) totalsize / (float) numnonempties;
                    if (avlength < 2.6) {
                        // Likely a chord line, so add a "."
                        l = "." + l;
                    } else {
                        // Likely a lyric line, so add a " "
                        if (!l.startsWith(" ")) {
                            l = " " + l;
                        }
                    }
                }
            }
            // Add the lines back
            newtext += l+"\n";
        }
        // If there isn't any tags declared, set up a verse tag
        if (!newtext.contains("[")) {
            newtext = "["+c.getString(R.string.tag_verse)+"]";
        }
        return newtext;
    }
}