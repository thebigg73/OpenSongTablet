package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;

class SongForSet {

    String getSongForSet(Context c, String folder, String filename) {
        if (folder.equals(c.getString(R.string.mainfoldername)) || folder.equals("MAIN") || folder.equals("")) {
            return "$**_" + filename + "_**$";
        } else {
            return "$**_" + folder + "/" + filename + "_**$";
        }
    }

    void addToSet(Context c, Preferences preferences, SetActions setActions, String folder, String filename) {
        StaticVariables.currentSet = StaticVariables.currentSet + getSongForSet(c,
                folder, filename);
        preferences.setMyPreferenceString(c,"setCurrent",StaticVariables.currentSet);
        setActions.prepareSetList(c,preferences);
        ShowToast.showToast(c,filename + ": " + c.getResources().getString(R.string.add_song_to_set));
    }

    void removeFromSet(Context c, Preferences preferences, SetActions setActions, String folder, String filename) {
        StaticVariables.currentSet = StaticVariables.currentSet.replace(getSongForSet(c,
                folder, filename),"");
        preferences.setMyPreferenceString(c,"setCurrent",StaticVariables.currentSet);
        setActions.prepareSetList(c,preferences);
        ShowToast.showToast(c,filename + ": " + c.getResources().getString(R.string.removedfromset));
    }

    void addOrRemoveFromSet(Context c, Preferences preferences, SetActions setActions, String folder, String filename) {
        if (StaticVariables.currentSet.contains(getSongForSet(c,folder, filename))) {
            removeFromSet(c,preferences,setActions,folder,filename);
        } else {
            addToSet(c,preferences,setActions,folder,filename);
        }
    }
}
