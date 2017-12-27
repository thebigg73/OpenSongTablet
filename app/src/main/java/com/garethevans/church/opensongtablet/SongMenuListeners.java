package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class SongMenuListeners extends Activity {

    public interface MyInterface {
        void songShortClick(int mychild);
        void openFragment();
        void prepareSongMenu();
    }

    public static MyInterface mListener;

    public static TextView.OnClickListener itemShortClickListener(final int i) {
        final Context c =  FullscreenActivity.mContext;
        mListener = null;
        try {
            mListener = (MyInterface) c;
        } catch (Exception e) {
            mListener = (MyInterface) c.getApplicationContext();
        }
        return new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.pdfPageCurrent = 0;
                if (FullscreenActivity.mSongFileNames.length>i) {
                    if (FullscreenActivity.songDetails[i][2].equals(c.getString(R.string.songsinfolder))) {
                        String s = FullscreenActivity.songDetails[i][1];
                        if (s.startsWith("/")) {
                            s = s.replaceFirst("/","");
                        }
                        FullscreenActivity.whichSongFolder = s;
                        mListener.prepareSongMenu();
                    } else {
                        FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[i];
                        if (FullscreenActivity.setView && FullscreenActivity.setSize > 0) {
                            // Get the name of the song to look for (including folders if need be)
                            SetActions.getSongForSetWork(c);

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
                        if (mListener != null) {
                            mListener.songShortClick(i);
                        }
                    }
                }
            }
        };
    }

    public static TextView.OnLongClickListener itemLongClickListener(final int i) {
        return new TextView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Context c =  FullscreenActivity.mContext;
                mListener = (MyInterface) c;

                // Save the preferences (to keep the folder and filename)
                Preferences.savePreferences();

                // Get the songfilename from the position we clicked on
                if (i!=-1 && FullscreenActivity.mSongFileNames!=null && FullscreenActivity.mSongFileNames.length>=i) {
                    FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[i];
                    // Now open the longpress fragment
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