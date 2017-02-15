package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SongMenuListeners extends Activity {

    public interface MyInterface {
        void songShortClick(int mychild);
        void openFragment();
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

                mListener = (MyInterface) c;

                // Get the songfilename from the position we clicked on
                if (position!=-1 && FullscreenActivity.mSongFileNames!=null && FullscreenActivity.mSongFileNames.length>=position) {
                    FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[position];
                    // Now open the longpress fragement
                    FullscreenActivity.whattodo = "songlongpress";
                    if (mListener != null) {
                        try {
                            mListener.openFragment();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                return true;
            }
        };
    }

}