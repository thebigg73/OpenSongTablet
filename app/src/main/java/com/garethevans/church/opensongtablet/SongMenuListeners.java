package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SongMenuListeners extends Activity {

    public interface MyInterface {
        void songShortClick(String myfile, String myfolder, int myposition);
        void openSongLongClickAction(String myfile, String myfolder, int myposition);
        void prepareSongMenu();
    }

    private static MyInterface mListener;

    public static TextView.OnClickListener itemShortClickListener(final String clickedfilename,
                                                                  final String clickedkey,
                                                                  final int position) {
        final Context c =  FullscreenActivity.mContext;
        final Preferences preferences = new Preferences();

        mListener = null;
        try {
            mListener = (MyInterface) c;
        } catch (Exception e) {
            mListener = (MyInterface) c.getApplicationContext();
        }

        final String whichSongFolder = StaticVariables.whichSongFolder;

        return new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.pdfPageCurrent = 0;

                try {
                    if (clickedkey.equals(c.getString(R.string.songsinfolder))) {
                        // We clicked on a folder
                        String s = clickedfilename;
                        if (s.startsWith("/")) {
                            s = s.replaceFirst("/", "");
                        }

                        if (s.endsWith("/")) {
                            s = s.substring(0, s.lastIndexOf("/"));
                        }

                        if (whichSongFolder.equals(c.getString(R.string.mainfoldername)) || whichSongFolder.equals("")) {
                            StaticVariables.whichSongFolder =  s;
                        } else {
                            // Add subdirectory on to the current whichsongfolder
                            s = whichSongFolder + "/" + s;
                            StaticVariables.whichSongFolder =  s;
                            Log.d("SongMenuListeners","s="+s);
                        }

                        // Update the menu again as it has changed
                        preferences.setMyPreferenceString(c, "whichSongFolder", StaticVariables.whichSongFolder);
                        mListener.prepareSongMenu();
                    } else {
                        // We have clicked on a song
                        if (mListener != null) {
                            mListener.songShortClick(clickedfilename, whichSongFolder, position);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static TextView.OnLongClickListener itemLongClickListener(final String clickedfilename,
                                                                     final String clickedfolder, final int position) {

        return new TextView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Context c =  FullscreenActivity.mContext;
                mListener = (MyInterface) c;

                if (mListener != null) {
                    try {
                        mListener.openSongLongClickAction(clickedfilename,clickedfolder, position);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                return true;
            }
        };
    }

}