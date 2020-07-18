package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.ArrayList;
import java.util.Arrays;

public class SetActions {

    public interface MyInterface {
        void doMoveSection();
        void loadSong();
    }

    public void prepareSetList(Context c, Preferences preferences) {
        try {
            StaticVariables.mSet = null;
            StaticVariables.mSetList = null;

            // Remove any blank set entries that shouldn't be there
            String setparse = preferences.getMyPreferenceString(c,"setCurrent","");
            setparse =  setparse.replace("$**__**$", "");

            // Add a delimiter between songs
            setparse = setparse.replace("_**$$**_", "_**$%%%$**_");

            // Break the saved set up into a new String[]
            StaticVariables.mSet = setparse.split("%%%");

            //Log.d("SetActions","Preparing Set list.  setparse="+setparse);
            /*for (String str:StaticVariables.mSet) {
                Log.d("SetActions","mSet item="+str);
            }*/

            // Fix any MAIN folder saved in set
            for (int s=0; s<StaticVariables.mSet.length; s++) {
                StaticVariables.mSet[s] = StaticVariables.mSet[s].replace("MAIN/","");
                StaticVariables.mSet[s] = StaticVariables.mSet[s].replace(c.getString(R.string.mainfoldername)+"/","");
            }

            StaticVariables.mSetList = StaticVariables.mSet.clone();

            StaticVariables.setSize = StaticVariables.mSetList.length;

            /*Log.d("SetActions","mSet.length="+StaticVariables.mSet.length);
            Log.d("SetActions","mSetList.length="+StaticVariables.mSetList.length);*/

            // Get rid of tags before and after folder/filenames
            for (int x = 0; x < StaticVariables.mSetList.length; x++) {
                StaticVariables.mSetList[x] = StaticVariables.mSetList[x]
                        .replace("$**_", "");
                StaticVariables.mSetList[x] = StaticVariables.mSetList[x]
                        .replace("_**$", "");
            }

            StaticVariables.mTempSetList = new ArrayList<>();
            StaticVariables.mTempSetList.addAll(Arrays.asList(StaticVariables.mSetList));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void indexSongInSet() {
        try {
            if (StaticVariables.mSet!=null && StaticVariables.mSetList!=null && StaticVariables.whatsongforsetwork!=null) {
                boolean alreadythere = false;
                if (StaticVariables.indexSongInSet > -1 && StaticVariables.indexSongInSet < StaticVariables.mSetList.length) {
                    if (StaticVariables.mSetList[StaticVariables.indexSongInSet].contains(StaticVariables.whatsongforsetwork)) {
                        alreadythere = true;
                    }
                }

                StaticVariables.setSize = StaticVariables.mSetList.length;

                if (alreadythere) {
                    if (StaticVariables.indexSongInSet > 0) {
                        StaticVariables.previousSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet - 1];
                    } else {
                        StaticVariables.previousSongInSet = "";
                    }
                    if (StaticVariables.indexSongInSet < StaticVariables.mSetList.length - 1) {
                        StaticVariables.nextSongInSet = StaticVariables.mSetList[StaticVariables.indexSongInSet + 1];
                    } else {
                        StaticVariables.nextSongInSet = "";
                    }

                } else {
                    StaticVariables.previousSongInSet = "";
                    StaticVariables.nextSongInSet = "";
                }

                // Go backwards through the setlist - this finishes with the first occurrence
                // Useful for duplicate items, otherwise it returns the last occurrence
                // Not yet tested, so left

                StaticVariables.mSet = StaticVariables.mSetList.clone();

                if (!alreadythere) {
                    for (int x = 0; x < StaticVariables.setSize; x++) {
//		for (int x = FullscreenActivity.setSize-1; x<1; x--) {
                        if (StaticVariables.mSet[x].contains(StaticVariables.whatsongforsetwork) ||
                                StaticVariables.mSet[x].contains("**" + StaticVariables.whatsongforsetwork)) {
                            StaticVariables.indexSongInSet = x;
                            if (x > 0) {
                                StaticVariables.previousSongInSet = StaticVariables.mSet[x - 1];
                            }
                            if (x != StaticVariables.setSize - 1) {
                                StaticVariables.nextSongInSet = StaticVariables.mSet[x + 1];
                            }

                        }
                    }
                }
            } else {
                StaticVariables.indexSongInSet = -1;
                StaticVariables.previousSongInSet = "";
                StaticVariables.nextSongInSet = "";
            }
            /*// Initialise variables if they are null
            if (StaticVariables.mSetList == null) {
                StaticVariables.mSetList = new String[1];
                StaticVariables.mSetList[0] = "";
            }

            if (StaticVariables.mSet == null) {
                StaticVariables.mSet = new String[1];
                StaticVariables.mSet[0] = "";
            }

            if (StaticVariables.whatsongforsetwork == null) {
                StaticVariables.whatsongforsetwork = "";
            }
            // See if we are already there!*/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String whatToLookFor(Context c, String folder, String filename) {
        String whattolookfor;
        if (folder.equals("") || folder.equals(c.getString(R.string.mainfoldername)) || folder.equals("MAIN")) {
            whattolookfor = "$**_" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.variation)) ||
                folder.startsWith("../Variations")) {
            whattolookfor = "$**_**" + c.getString(R.string.variation) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.note)) ||
                folder.startsWith("../Notes")) {
            whattolookfor = "$**_**" + c.getString(R.string.note) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.slide)) ||
                folder.startsWith("../Slides")) {
            whattolookfor = "$**_**" + c.getString(R.string.slide) + "/" + filename + "_**$";
        } else if (folder.startsWith("**"+c.getString(R.string.image_slide)) ||
                folder.startsWith("../Images")) {
            whattolookfor = "$**_**" + c.getString(R.string.image_slide) + "/" + filename + "_**$";
        } else {
            whattolookfor = "$**_" + folder + "/" + filename + "_**$";
        }
        return whattolookfor;
    }

    public String fixIsInSetSearch(String s) {
        if (s.contains("**_Variations/")) {
            s = s.replace("**_Variations/","**_**Variation/");
        } else if (s.contains("**_Variation/")) {
            s = s.replace("**_Variation/","**_**Variation/");
        }
        return s;
    }
}