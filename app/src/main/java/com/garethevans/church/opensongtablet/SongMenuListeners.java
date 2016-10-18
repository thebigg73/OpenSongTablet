package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SongMenuListeners extends Activity {

    public static int mychild;
    public interface MyInterface {
        void songLongClick(int mychild);
        void songShortClick(int mychild);
    }

    public static MyInterface mListener;

    public static ListView.OnItemClickListener myShortClickListener(final Context c) {
        return new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener = (MyInterface) c;

                FullscreenActivity.pdfPageCurrent = 0;

                if (!FullscreenActivity.addingtoset) {
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[i];

                    if (FullscreenActivity.setView && FullscreenActivity.setSize > 0) {
                        // Get the name of the song to look for (including folders if need be)
                        SetActions.getSongForSetWork();

                        if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                            // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                            // The first song has an index of 6 (the 7th item as the rest are menu items)

                            FullscreenActivity.previousSongInSet = "";
                            FullscreenActivity.nextSongInSet = "";
                            SetActions.prepareSetList();
                            //setupSetButtons();

                        } else {
                            // Song isn't in the set, so just show the song
                            // Switch off the set view (buttons in action bar)
                            FullscreenActivity.setView = false;
                            // Re-enable the disabled button

                        }
                    } else {
                        // User wasn't in set view, or the set was empty
                        // Switch off the set view (buttons in action bar)
                        FullscreenActivity.setView = false;
                    }

                    // Now save the preferences
                    Preferences.savePreferences();

                    // Now tell the activity to fix the options menu and close the drawers
                    mListener.songShortClick(i);

                } else {
                    FullscreenActivity.addingtoset = false;
                }
            }
        };
    }

    public static ListView.OnItemLongClickListener myLongClickListener(final Context c) {

        return new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.addingtoset = true;
                // Vibrate to indicate something has happened
                Vibrator vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                vb.vibrate(200);

                // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
                // This is done by viewing it (avoids issues with file extension renames)

                // Just in case users running older than lollipop, we don't want to open the file
                // In this case, store the current song as a string so we can go back to it
                String currentsong = FullscreenActivity.songfilename;

                FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[position];

                // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
                // This is done by viewing it (avoids issues with file extension renames)
                if (FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".pro") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".chopro") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".cho") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".chordpro") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".onsong") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".txt")) {

                    // Don't add song yet, but tell the user
                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.convert_song);
                    ShowToast.showToast(c);
                    FullscreenActivity.songfilename = currentsong;
                } else if (FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".doc") ||
                        FullscreenActivity.songfilename.toLowerCase(FullscreenActivity.locale).endsWith(".docx")) {
                    // Don't add song yet, but tell the user it is unsupported
                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.file_type_unknown);
                    ShowToast.showToast(c);
                    FullscreenActivity.songfilename = currentsong;
                } else {
                    // Set the appropriate song filename
                    FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[position];

                    if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.songfilename + "_**$";
                    } else {
                        FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename + "_**$";
                    }

                    // Allow the song to be added, even if it is already there
                    FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

                    // Tell the user that the song has been added.
                    FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" " + c.getResources().getString(R.string.addedtoset);
                    ShowToast.showToast(c);

                    // Save the set and other preferences
                    Preferences.savePreferences();

                    if (mListener != null) {
                        mListener.songLongClick(mychild);
                    }
                }
                return false;
            }
        };
    }

}