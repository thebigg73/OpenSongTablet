package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.Locale;

class TextSongConvert {

    // This class is called when indexing text songs (ending in .txt or files that aren't xml, onsong or chordpro
    String convertText(Context c, String oldtext) {
        StringBuilder newtext = new StringBuilder();

        try {
            // Split the whole thing into lines
            String[] lines = oldtext.split("\n");
            for (String l : lines) {

                // Fix lines that have tags [ ]
                l = fixTags(c, l);

                // Fix chord lines
                l = fixChordLines(l);

                // Fix tab lines
                l = fixTabLines(l);

                // Add the lines back
                newtext.append(l).append("\n");
            }
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
        String compiledtext = newtext.toString();

        // Remove any blank headings if they are redundant
        compiledtext = fixBlankHeadings(c, compiledtext);

        // Indicate after loading song (which renames it), we need to build the database and song index
        FullscreenActivity.needtorefreshsongmenu = true;

        return compiledtext;
    }

    private String fixTags(Context c, String l) {
        // Look for potential headings but get rid of rogue spaces before and after
        if (l.contains("[") && l.contains("]") && l.length() < 15) {
            l = l.trim();
        }

        // Add closing tags if missing
        if (l.startsWith("[") && !l.contains("]")) {
            l = l+"]";
        }

        boolean alreadyheading = l.startsWith("[");
        if (!alreadyheading && l.trim().length() < 15) {
            boolean containsTag = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList list = Resources.getSystem().getConfiguration().getLocales();
                for (int i = 0; i < list.size(); i++) {
                    containsTag |= stringContainsTag(c, list.get(i), l);
                }
            } else {
                //noinspection
                Resources system = Resources.getSystem();
                Locale locale;
                if (system == null) {
                    locale = Locale.getDefault();
                } else {
                    locale = system.getConfiguration().locale;
                }
                containsTag = stringContainsTag(c, locale, l);
            }
            if (containsTag) {
                // Remove any colons and white space
                l = l.replace(":", "");
                l = l.trim();
                // Add the tag braces
                l = "[" + l + "]";
                //Remove any double braces
                l = l.replace("[[", "[");
                l = l.replace("]]", "]");
            }
        }

        return l;
    }

    private boolean stringContainsTag(Context context, Locale locale, String line) {
        Resources res = getLocalizedResources(context, locale);
        return line.contains(res.getString(R.string.tag_verse)) || line.contains(res.getString(R.string.tag_chorus)) ||
                line.contains(res.getString(R.string.tag_bridge)) || line.contains(res.getString(R.string.tag_ending)) ||
                line.contains(res.getString(R.string.tag_instrumental)) || line.contains(res.getString(R.string.tag_interlude)) ||
                line.contains(res.getString(R.string.tag_intro)) || line.contains(res.getString(R.string.tag_prechorus)) ||
                line.contains(res.getString(R.string.tag_refrain)) || line.contains(res.getString(R.string.tag_tag)) ||
                line.contains(res.getString(R.string.tag_reprise));
    }

    @NonNull
    private Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    private String fixChordLines(String l) {
        // Look for chord lines
        if (!l.startsWith(".") && !l.startsWith("[") && !l.startsWith(";")) {
            // Do this by splitting the line into sections split by space
            String[] possiblechordline = l.split(" ");
            // Go through each split bit and get the length of the non empty ones.  We'll then average the lengths
            int numnonempties = 0;
            int totalsize = 0;
            for (String ch : possiblechordline) {
                if (ch.trim().length() > 0) {
                    numnonempties += 1;
                    totalsize += ch.trim().length();
                }
            }
            // Get the average length of the bits
            if (numnonempties > 0 && totalsize > 0) {
                float avlength = (float) totalsize / (float) numnonempties;
                float percentageused = ((float)totalsize/(float)l.length())*100;

                // To try and identify chords, experience shows that the average length is
                // less than 2.4 or that the percentage used is less than 25%
                // Won't identify chord lines that have 1 chord like F#m7b5!!!

                if (avlength < 2.4 || percentageused < 25) {
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
        return l;
    }

    private String fixTabLines(String l) {
        // Look for tab lines
        if (l.contains("|") && l.contains("-")) {
            Log.d("d", "Could be a tab line = " + l);

            // Does line start with string tuning?
            String b = l.trim().replaceFirst(";","");
            boolean isstring = (b.startsWith("A") || b.startsWith("B") || b.startsWith("D") ||
                    b.startsWith("E") || b.startsWith("e") || b.startsWith("G"));

            if (l.startsWith(";")) {
                l = l.replaceFirst(";","");
            }

            // Check we have a tab start of | after the string tuning and before -
            if (isstring && l.indexOf("-")<4 && l.indexOf("|")>l.indexOf("-")) {
                if (l.indexOf("-")<4) {
                    l = l.replaceFirst("-", "|-");
                } else if (l.indexOf(" ")<4) {
                    l = l.replaceFirst(" ", "|");
                }
            }

            if (l.trim().indexOf("|") < 4 || isstring) {
                String gstring = l.substring(0, l.indexOf("|")).trim();
                String tab = l.substring(l.indexOf("|")).trim();
                // make sure string is two characters long
                if (gstring.length() < 2) {
                    gstring = gstring + " ";
                }
                // add a comment tag
                if (!gstring.startsWith(";")) {
                    gstring = ";" + gstring;
                }
                Log.d("d", "gstring = " + gstring);
                Log.d("d", "tab = " + tab);

                l = gstring + tab;
            }
        }
        return l;
    }

    private String fixBlankHeadings(Context c, String compiledtext) {
        try {
            compiledtext = compiledtext.replace("[]\n[", "[");
            compiledtext = compiledtext.replace("[]\n\n[", "[");
            compiledtext = compiledtext.replace("[]\n \n[", "[");

            // If there isn't any tags declared, set up a verse tag
            if (!compiledtext.contains("[")) {
                compiledtext = "[" + c.getString(R.string.tag_verse) + "]" + "\n" + compiledtext;
            }
            return compiledtext;
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
            return "";
        }
    }
}