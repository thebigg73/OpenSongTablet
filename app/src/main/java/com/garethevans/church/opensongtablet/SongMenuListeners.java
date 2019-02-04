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
        final SetActions setActions = new SetActions();
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
                try {
                    if (FullscreenActivity.songDetails.length > i) {
                        if (FullscreenActivity.songDetails[i][2].equals(c.getString(R.string.songsinfolder))) {
                            String s = FullscreenActivity.songDetails[i][0];
                            if (s.startsWith("/")) {
                                s = s.replaceFirst("/", "");
                            }
                            if (s.endsWith("/")) {
                                s = s.substring(0, s.lastIndexOf("/"));
                            }

                            if (FullscreenActivity.whichSongFolder.equals(c.getString(R.string.mainfoldername)) ||
                                    FullscreenActivity.whichSongFolder.equals("")) {
                                FullscreenActivity.whichSongFolder = s;
                            } else {
                                // Add subdirectory on to the current whichsongfolder
                                FullscreenActivity.whichSongFolder = FullscreenActivity.whichSongFolder + "/" + s;
                            }

                            mListener.prepareSongMenu();
                        } else {
                            if (FullscreenActivity.mSongFileNames.length > i && FullscreenActivity.mSongFileNames[i] != null) {
                                FullscreenActivity.songfilename = FullscreenActivity.mSongFileNames[i];
                            } else {
                                FullscreenActivity.songfilename = "";
                            }
                            if (FullscreenActivity.setView && FullscreenActivity.setSize > 0) {
                                // Get the name of the song to look for (including folders if need be)
                                setActions.getSongForSetWork(c);

                                if (FullscreenActivity.mySet.contains(FullscreenActivity.whatsongforsetwork)) {
                                    // Song is in current set.  Find the song position in the current set and load it (and next/prev)
                                    FullscreenActivity.previousSongInSet = "";
                                    FullscreenActivity.nextSongInSet = "";
                                    setActions.prepareSetList();
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
                } catch (Exception e) {
                    e.printStackTrace();
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